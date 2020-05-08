 package org.iucn.sis.server.extensions.images;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
 import org.apache.commons.compress.archivers.zip.ZipFile;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.hibernate.Session;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.io.TaxonIO;
 import org.iucn.sis.server.api.persistance.SISPersistentManager;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.shared.api.data.ManagedImageData;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonImage;
 import org.restlet.Context;
 import org.restlet.data.CharacterSet;
 import org.restlet.data.Language;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.ext.fileupload.RestletFileUpload;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ResourceException;
 
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.CSVTokenizer;
 import com.solertium.util.TrivialExceptionHandler;
 import com.solertium.vfs.NotFoundException;
 import com.solertium.vfs.VFS;
 import com.solertium.vfs.VFSPath;
 
 public class ImageRestlet extends BaseServiceRestlet {
 
 	private AtomicBoolean running;
 	private final VFS vfs;
 	
 	public ImageRestlet(Context context) {
 		super(context);
 		running = new AtomicBoolean(false);
 		vfs = SIS.get().getVFS();
 	}
 	
 	@Override
 	public void definePaths() {
 		paths.add("/images");
 		paths.add("/images/{taxonId}");
 	}
 	
 	private Taxon getTaxon(Request request, Session session) throws ResourceException {
 		return getTaxon((String)request.getAttributes().get("taxonId"), session);
 	}
 	
 	private Taxon getTaxon(String taxonID, Session session) throws ResourceException {
 		TaxonIO taxonIO = new TaxonIO(session);
 		try {
 			return taxonIO.getTaxon(Integer.valueOf(taxonID));
 		} catch (Exception e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
 		}
 	}
 	
 	@Override
 	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
 		final String taxonId = (String) request.getAttributes().get("taxonId");
 		if (taxonId == null || request.getResourceRef().getLastSegment().equals("upload"))
 			return buildUploadHTML();
 		
 		final Taxon taxon = getTaxon(request, session);
 		
 		final StringBuilder builder = new StringBuilder();
 		builder.append("<images>");
 		for (TaxonImage image : taxon.getImages())
 			builder.append(image.toXML());
 		builder.append("</images>");
 		
 		return new StringRepresentation(builder.toString(), MediaType.TEXT_XML);
 	}
 	
 	@Override
 	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		final String taxonId = (String) request.getAttributes().get("taxonId");
 		
 		final RestletFileUpload fileUploaded = new RestletFileUpload(new DiskFileItemFactory());
 		
 		final List<FileItem> list;
 		try {
 			list = fileUploaded.parseRequest(request);
 		} catch (FileUploadException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
 		}
 		
 		FileItem file = null;
 		
 		for (int i = 0; i < list.size() && file == null; i++) {
 			FileItem item = list.get(i);
 			if (!item.isFormField()) {
 				file = item;
 			}
 		}
 
 		if (file == null) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No file uploaded.");
 		}
 		else {
 			String encoding = file.getContentType();
 			Debug.println("Uploading image {0} encoded as {1}", file.getName(), file.getContentType());
 			String extension = "";
 			if (encoding.equals("image/jpeg"))
 				extension = "jpg";
 			else if (encoding.equals("image/gif"))
 				extension = "gif";
 			else if (encoding.equals("image/png"))
 				extension = "png";
 			else if (encoding.equals("image/tiff"))
 				extension = "tif";
 			else if (encoding.equals("image/bmp"))
 				extension = "bmp";
 			else if (taxonId == null || taxonId.equals("batch"))
 				extension = "zip";
 			else
 				throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, "Encoding of type " + encoding + " is not supported.");
 
 			final int id;
 			try {
 				id = writeFile(file.get(), extension);
 			} catch (IOException e) {
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 			}
 				
 			if (taxonId == null || taxonId.equals("batch")) {
 				if (!running.getAndSet(true)) {
 					try {
 						handleBatchUpload(id, response, session);
 					} finally {
 						running.set(false);
 					}
 				} else {
 					response.setEntity(buildUploadHTML());
 					response.setStatus(Status.SUCCESS_OK);
 				}
 			} else {
 				final Taxon taxon = getTaxon(request, session);
 				
 				TaxonImage image = new TaxonImage();
 				image.setEncoding(encoding);
 				image.setIdentifier(id+"");
 				image.setPrimary(taxon.getImages().isEmpty());
 				image.setRating(0.0F);
 				image.setWeight(0);
 				image.setTaxon(taxon);
 				
 				try {
 					SISPersistentManager.instance().saveObject(session, image);
 				} catch (PersistentException e) {
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 				}
 			}
 		}
 	}
 
 	private int writeFile(byte[] data, String encoding) throws IOException {
 		Random r = new Random(new Date().getTime());
 		
 		int id;
 		VFSPath randomImagePath;
 		
 		// check for file already existing
 		while (vfs.exists(randomImagePath = 
 			new VFSPath("/images/bin/" + (id = r.nextInt(Integer.MAX_VALUE)) + "." + encoding)));
 		
 		final OutputStream outStream = vfs.getOutputStream(randomImagePath);
 		
 		try {
 			outStream.write(data);
 		} catch (IOException e) {
 			throw e;
 		} finally {
 			try {
 				outStream.close();
 			} catch (Exception f) {
 				TrivialExceptionHandler.ignore(this, f);
 			}
 		}
 			
 		return id;
 	}
 
 	private int writeFile(InputStream is, String encoding) throws IOException {
 		Random r = new Random(new Date().getTime());
 		
 		int id;
 		VFSPath randomImagePath;
 		
 		// check for file already existing
 		while (vfs.exists(randomImagePath = 
 			new VFSPath("/images/bin/" + (id = r.nextInt(Integer.MAX_VALUE)) + "." + encoding)));
 		
 		final OutputStream outStream = vfs.getOutputStream(randomImagePath);
 		try {
 			while(is.available()>0)
 				outStream.write(is.read());
 		} catch (IOException e) {
 			throw e;
 		} finally {
 			try {
 				outStream.close();
 			} catch (IOException f) {
 				TrivialExceptionHandler.ignore(this, f);
 			}
 		}
 			
 		return id;
 	}
 	
 	@Override
 	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		final Taxon taxon = getTaxon(request, session);
 		final Map<String, TaxonImage> map = new HashMap<String, TaxonImage>();
 		for (TaxonImage image : taxon.getImages())
 			map.put(image.getIdentifier(), image);
 		
 		final NativeDocument document = getEntityAsNativeDocument(entity);
 		final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName(TaxonImage.ROOT_TAG);
 		for (int i = 0; i < nodes.getLength(); i++) {
 			TaxonImage source = TaxonImage.fromXML(nodes.elementAt(i));
 			TaxonImage target = map.remove(source.getIdentifier());
 			if (target == null) {
 				source.setTaxon(taxon);
 				try {
 					SISPersistentManager.instance().saveObject(session, source);
 				} catch (PersistentException e) {
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 				}
 			}
 			else {
 				target.setCaption(source.getCaption());
				target.setCredit(target.getCredit());
 				target.setPrimary(source.getPrimary());
 				target.setRating(source.getRating());
 				target.setShowRedList(source.getShowRedList());
 				target.setShowSIS(source.getShowSIS());
 				target.setSource(source.getSource());
 				target.setWeight(source.getWeight());
 				
 				try {
 					SISPersistentManager.instance().updateObject(session, target);
 				} catch (PersistentException e) {
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 				}
 			}
 		}
 		
 		for (TaxonImage image : map.values()) {
 			taxon.getImages().remove(image);
 			try {
 				SISPersistentManager.instance().deleteObject(session, image);
 			} catch (PersistentException e) {
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 			}
 		}
 		
 		response.setStatus(Status.SUCCESS_CREATED);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void handleBatchUpload(int id, Response response, Session session) throws ResourceException {
 		StringBuilder xml = new StringBuilder();
 		xml.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=cp1252\">" +
 				"</head><body><div>");
 		
 		File tempFile;
 		try {
 			tempFile = vfs.getTempFile(new VFSPath("/images/bin/" + id + ".zip"));
 		} catch (NotFoundException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
 		} catch (IOException e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 		}
 			
 		final ZipFile zip;
 		try {
 			zip = new org.apache.commons.compress.archivers.zip.ZipFile(tempFile, "Cp1252");
 		} catch (IOException e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 		}
 		
 		final Enumeration entries = zip.getEntries();
 			
 		ZipArchiveEntry csv = null;
 		
 		final HashMap<String, Integer> filenames = new HashMap<String, Integer>();
 		final HashMap<String, String> encodings = new HashMap<String, String>();
 			
 		while (entries.hasMoreElements()) {
 			ZipArchiveEntry entry = (ZipArchiveEntry)entries.nextElement();
 			if (entry.getName().endsWith(".csv")) 
 				csv = entry;
 			else {
 				String filename = entry.getName().toLowerCase();
 				String ext = filename.substring(filename.lastIndexOf(".")+1);
 				
 				final InputStream stream;
 				try {
 					stream = zip.getInputStream(entry);
 				} catch (IOException e) {
 					e.printStackTrace();
 					continue;
 				}
 				
 				String encoding = "image/"+ext;
 				if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
 					encoding = "image/jpeg";
 					ext = "jpg"; //Force it to the three letter extension
 				} else if (ext.equalsIgnoreCase("tif") || ext.equalsIgnoreCase("tiff")) {
 					encoding="image/tiff";
 					ext = "tif";
 				}
 				
 				try {
 					filenames.put(filename.replaceAll("[^a-zA-Z0-9]", ""), writeFile(stream, ext));
 					encodings.put(filename.replaceAll("[^a-zA-Z0-9]", ""), encoding);
 				} catch (IOException e) {
 					e.printStackTrace();
 					continue;
 				}
 			}
 		}
 			
 		if (!csv.equals(null)) {
 			final HashMap<String, ManagedImageData> map;
 			try {
 				map = parseCSV(zip.getInputStream(csv));
 			} catch (IOException e) {
 				response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
 				return;
 			}
 			
 			for (String key : map.keySet()) {
 				String result = null;
 				String taxonID = map.get(key).getField("sp_id");
 				Taxon taxon = null;
 				try {
 					taxon = getTaxon(taxonID, session);
 				} catch (ResourceException e) {
 					result = "Taxon " + taxonID + " not found.";
 				}
 				
 				if (result == null)
 					result = writeXML(taxon, String.valueOf(filenames.get(key)), encodings.get(key), map.get(key), session);
 				
 				if (result == null)
 					xml.append("<div>"+map.get(key).getField("filename")+": Success</div><br/>");
 				else
 					xml.append("<div>"+map.get(key).getField("filename")+": Failure -- " + result + "</div><br/>");
 			}
 				
 			xml.append("</div></body></html>");
 		
 			response.setEntity(new StringRepresentation(xml.toString(), MediaType.TEXT_HTML, Language.DEFAULT, CharacterSet.UTF_8));
 			response.setStatus(Status.SUCCESS_OK);
 		}
 	}
 	
 	private HashMap<String, ManagedImageData> parseCSV(InputStream csvStream) throws IOException {
 		HashMap<String, ManagedImageData> data = new HashMap<String, ManagedImageData>();
 		
 		BufferedReader lineReader = new BufferedReader(new InputStreamReader(csvStream, Charset.forName("Cp1252")));
 		while(lineReader.ready()){
 			String line = lineReader.readLine();
 			
 			if (line.contains("sp_id")){
 				continue; //ignore xsl export headers
 			}
 			CSVTokenizer tokenizer = new CSVTokenizer(line);
 			
 			String filename = tokenizer.nextToken().toLowerCase();
 			ManagedImageData img = new ManagedImageData();
 			img.setField("sp_id", tokenizer.nextToken());
 			
 			img.setField("credit", tokenizer.nextToken());
 			img.setField("source", tokenizer.nextToken());
 			img.setField("caption", tokenizer.nextToken());
 			
 			img.setField("genus", tokenizer.nextToken());
 			img.setField("species", tokenizer.nextToken());
 			
 			img.setField("showRedlist", "true");
 			img.setField("showSIS", "true");
 			img.setField("filename", filename);
 			
 			data.put(filename.replaceAll("[^a-zA-Z0-9]", ""), img);
 		}	
 		
 		return data;
 	}
 	
 	private Representation buildUploadHTML() {
 		StringBuilder sb = new StringBuilder();
 		if (running.get()) {
 			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>" +
 					"A batch upload is currently running. Please try again later.</body></html>");
 		} else {
 			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'><form method=\"post\" enctype=\"multipart/form-data\">");
 			sb.append("Select .zip to upload: ");
 			sb.append("<input type=\"file\" name=\"dem\" size=\"60\" style='font-family:Verdana; font-size:x-small'/>");
 			sb.append("<p><input type=\"submit\" onclick=\"this.disabled=true;\" style='font-family:Verdana; font-size:x-small'/>");
 			sb.append("<div>After selecting Submit, do NOT close or navigate away from this tab until you see a ");
 			sb.append("final report. Depending on the size of your zip file, this could take significant time.</div>");
 			sb.append("</form>");
 			sb.append("</body></html>");
 		}
 		
 		return new StringRepresentation(sb, MediaType.TEXT_HTML);
 	}
 
 	private String writeXML(Taxon taxon, String id, String encoding, ManagedImageData data, Session session) throws ResourceException {
 		final TaxonImage image = new TaxonImage();
 		image.setIdentifier(id);
 		image.setEncoding(encoding);
 		image.setPrimary(taxon.getImages().isEmpty());
 		image.setRating(0.0F);
 		image.setWeight(0);
 		image.setTaxon(taxon);
 		
 		taxon.getImages().add(image);
 		
 		if (data != null) {
 			/*
 			 * FIXME: using attributes to store captions or 
 			 * any other user-entered data doesn't sound 
 			 * like a recipe for success.  Should probably  
 			 * wrap this information in a child CDATA node.
 			 */
 			if (data.containsField("caption"))
 				image.setCaption(data.getField("caption"));
 			if (data.containsField("credit")) 
 				image.setCredit(data.getField("credit"));
 			if (data.containsField("source")) 
 				image.setSource(data.getField("source"));
 			if (data.containsField("showRedlist")) 
 				image.setShowRedList("true".equals(data.getField("showRedlist")));
 			if (data.containsField("showSIS"))
 				image.setShowSIS("true".equals(data.getField("showSIS")));
 		}
 
 		try {
 			SISPersistentManager.instance().saveObject(session, image);
 		} catch (PersistentException e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 		}
 		
 		return null;
 	}
 }
