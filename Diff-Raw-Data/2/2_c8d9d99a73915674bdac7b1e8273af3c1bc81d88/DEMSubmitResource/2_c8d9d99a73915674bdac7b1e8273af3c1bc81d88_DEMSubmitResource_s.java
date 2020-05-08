 package org.iucn.sis.server.extensions.demimport;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.naming.NamingException;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.hibernate.Session;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.persistance.SISPersistentManager;
 import org.iucn.sis.server.api.restlets.TransactionResource;
 import org.iucn.sis.server.api.utils.DocumentUtils;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.ext.fileupload.RestletFileUpload;
 import org.restlet.representation.OutputRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.ResourceException;
 
 import com.solertium.db.DBSessionFactory;
 import com.solertium.util.RandString;
 
 @SuppressWarnings("deprecation")
 public class DEMSubmitResource extends TransactionResource {
 	
 	public static final List<String> getPaths() {
 		List<String> paths = new ArrayList<String>();
 		paths.add("/database");
 		paths.add("/database/{file}");
 		
 		return paths;
 	}
 
 	public DEMSubmitResource(final Context context, final Request request, final Response response) {
 		super(context, request, response);
 		setModifiable(true);
 		
 		getVariants().add(new Variant(MediaType.TEXT_HTML));
 		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
 	}
 	
 	@Override
 	protected boolean shouldOpenTransaction(Request request, Response response) {
 		return false;
 	}
 	
 	@Override
 	public Representation represent(Variant variant, Session session) throws ResourceException {
 		final String file = (String)getRequest().getAttributes().get("file");
 		
 		StringBuilder sb = new StringBuilder();
 		if (DEMImport.isRunning()) {
 			sb.append("<html><head><title>DEM Import</title>" +
 				"<style type=\"text/css\"><!--\n" + 
 				"body {font-family:Verdana; font-size:x-small; }\n" +
 				"input {font-family:Verdana; font-size:x-small; }\n" + 
 				"--></style></head>" +
 				"<body>A DEM Import is currently running.  " +
 				"Only one DEM can be imported into SIS at a time.  " + "Please wait.</body></html>");
 			
 			return new StringRepresentation(sb, MediaType.TEXT_HTML);
 		} else if (file == null) {
 			sb.append("<html><head><title>DEM Import Form</title>" +
 				"<style type=\"text/css\"><!--\n" + 
 				"body {font-family:Verdana; font-size:x-small; }\n" + 
 				"--></style></head>" +
 				"<body>" +
 				"<form method=\"POST\" enctype=\"multipart/form-data\">");
 			sb.append("<span>Attach DEM database: </span>");
 			sb.append("<input type=\"file\" name=\"dem\" size=\"60\"/>");
 			sb.append("<p><span>Allow Upper-Level Taxa Creation (Legacy Behavior)?</span><input type=\"checkbox\" name=\"allowCreate\" /></p>");
 			sb.append("<p><span>Allow Assessment Import?</span><input type=\"checkbox\" name=\"allowAssessment\" checked/></p>");
 			sb.append("<p><input type=\"submit\" value=\"Submit\"/></p>");
 			sb.append("</form>");
 			sb.append(generatePriorImportsStatus());
 			sb.append("</body></html>");
 			
 			return new StringRepresentation(sb, MediaType.TEXT_HTML);
 		} else {
 			return doImport(file); 
 		}
 	}
 	
 	private Representation doImport(String demName) throws ResourceException {
 		try {
 			if (DEMImport.isRunning()) {
 				throw new ResourceException(Status.CLIENT_ERROR_LOCKED, "A DEM Import is currently running.  "
 						+ "Only one DEM can be imported into SIS at a time.  "
 						+ "Please click the DEM Import tab to refresh the status of your import.");
 			}
 			
 			File folder = File.createTempFile("none", "txt").getParentFile();
 			File file = new File(folder, demName);
 						
 			if (!file.exists())
 				throw new ResourceException(Status.CLIENT_ERROR_GONE, "This file no longer exists: " + demName + ". Please try upload again.");
 			
 			DBSessionFactory.registerDataSource(demName, 
 				"jdbc:access:///" + file.getAbsolutePath(), "com.hxtt.sql.access.AccessDriver", "", "");
 			
 			final PipedInputStream inputStream = new PipedInputStream(); 
 			final Representation representation = new OutputRepresentation(MediaType.TEXT_PLAIN) {
 				public void write(OutputStream out) throws IOException {
 					byte[] b = new byte[8];
 					int read;
 					while ((read = inputStream.read(b)) != -1) {
 						out.write(b, 0, read);
 						out.flush();
 					}
 				}
 			};
 			
 			final PrintWriter writer;
 			try {
 				writer = new PrintWriter(new OutputStreamWriter(new PipedOutputStream(inputStream)), true);
 			} catch (IOException e) {
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 			}
 			
 			final DEMImport demImport;
 			try {
 				demImport = new DEMImport(getRequest().getChallengeResponse().getIdentifier(), 
 						demName, SISPersistentManager.instance().openSession());
 				demImport.setAllowCreateUpperLevelTaxa(demName.contains("taxon-all"));
 				demImport.setAllowAssessments(demName.contains("asm-on"));
 			} catch (Exception e) {
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 			}
 			demImport.setOutputStream(writer, "\r\n");
 			demImport.println("DEM file was received and is being processed...");
 			
 			new Thread(demImport).start();
 				
 			return representation;
 		} catch (IOException ix) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Storage failed: " + ix.getMessage(), ix);
 		} catch (NamingException e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not register database: " + e.getMessage(), e);
 		}
 	}
 	
 	@Override
 	public void acceptRepresentation(Representation entity, Session session) throws ResourceException {
 		DiskFileItemFactory factory = new DiskFileItemFactory();
 		RestletFileUpload upload = new RestletFileUpload(factory);
 		try {
 			if (DEMImport.isRunning()) {
 				throw new ResourceException(Status.CLIENT_ERROR_LOCKED, "A DEM Import is currently running.  "
 						+ "Only one DEM can be imported into SIS at a time.  "
 						+ "Please click the DEM Import tab to refresh the status of your import.");
 			}
 			
 			FileItem demFile = null;
 			boolean allowCreate = false;
 			boolean allowAssessments = false;
 			for (FileItem item : upload.parseRequest(getRequest())) {
 				if (item.isFormField()) {
 					if ("allowCreate".equals(item.getFieldName()))
 						allowCreate = "on".equals(item.getString()) || "true".equals(item.getString());
 					else if ("allowAssessment".equals(item.getFieldName()))
 						allowAssessments = "on".equals(item.getString()) || "true".equals(item.getString());
 				}
 				else
 					demFile = item;
 			}
 			
 			if (demFile == null)
 				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No file found to upload.");
 			
 			String taxonMode = allowCreate ? "taxon-all" : "taxon-family";
 			String asmMode = allowAssessments ? "asm-on" : "asm-off";
 			String demName = "dem_" + taxonMode + "_" + asmMode + "_" + RandString.getString(8);
 			
 			File temp = File.createTempFile(demName, ".mdb");
 			try {
 				demFile.write(temp);
 			} catch (Exception x) {
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not write item to " + temp.getPath());
 			}
 			
			getResponse().redirectSeeOther(getRequest().getResourceRef() + "/" + temp.getName());
 		} catch (FileUploadException fx) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Upload failed: " + fx.getMessage(), fx);
 		} catch (IOException ix) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Storage failed: " + ix.getMessage(), ix);
 		}
 	}
 	
 	private String generatePriorImportsStatus() {
 		String ret = "<span><p><b>DEMImport Logging Information</b> -- ";
 		ret += "<a target=\"_blank\" href=\"/raw" + DEMImportInformation.logURL
 				+ "\">(Download full status log)</a><br>";
 
 		String htmlLog = DocumentUtils.getVFSFileAsString(DEMImportInformation.htmlLogURL, SIS.get().getVFS());
 
 		if (htmlLog == null || htmlLog.equals(""))
 			ret += "There is no DEMImport logging information available.";
 		else
 			ret += htmlLog;
 
 		ret += "</p></span>";
 
 		return ret;
 	}
 	
 }
