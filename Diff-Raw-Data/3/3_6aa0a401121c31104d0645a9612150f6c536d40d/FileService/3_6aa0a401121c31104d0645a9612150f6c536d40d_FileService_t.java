 
 package eu.dm2e.ws.services.file;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.io.IOUtils;
 import org.glassfish.jersey.media.multipart.FormDataBodyPart;
 import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
 import org.glassfish.jersey.media.multipart.FormDataParam;
import org.joda.time.DateTime;
 
 import eu.dm2e.logback.LogbackMarkers;
 import eu.dm2e.utils.PojoUtils;
 import eu.dm2e.ws.Config;
 import eu.dm2e.ws.ConfigProp;
 import eu.dm2e.ws.DM2E_MediaType;
 import eu.dm2e.ws.ErrorMsg;
 import eu.dm2e.ws.NS;
 import eu.dm2e.ws.api.FilePojo;
 import eu.dm2e.ws.api.WebservicePojo;
 import eu.dm2e.ws.api.json.OmnomJsonSerializer;
 import eu.dm2e.ws.grafeo.GResource;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 import eu.dm2e.ws.grafeo.jena.SparqlUpdate;
 import eu.dm2e.ws.services.AbstractRDFService;
 
 /**
  * The service includes all necessary methods to upload a new file (no matter
  * which mimetype) to a file storage and/or create a new file reference in the
  * system for a file which is already stored in a given place.
  * 
  * @author Konstantin Baierer
  * @author Robert Meusel
  * 
  */
 @Path("/file")
 public class FileService extends AbstractRDFService {
 
 	
 	@Override
 	public WebservicePojo getWebServicePojo() {
 		WebservicePojo ws = super.getWebServicePojo();
 		ws.setLabel("Default File Service");
 		return ws;
 	}
 
     public FileService() { }
 
 	/**
 	 * GET /{id}
 	 *  Retrieve metadata/file data for a locally stored file
 	 * 
 	 * @param fileId
 	 * @return
 	 */
 	@GET
 	@Path("{id}")
 	public Response getFileById() {
 		URI uri = getRequestUriWithoutQuery();
 		return getFileByUri(uri);
 	}
 
 	/**
 	 * GET /	{*}
 	 * @return
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getFileListBaseAlias() {
 		return Response.seeOther(appendPath(getRequestUriWithoutQuery(), "list")).build();
 	}
 	/**
 	 * GET /list
 	 *  Retrieve metadata/file data for a locally stored file
 	 * 
 	 * @param fileId
 	 * @return
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("list")
 	public Response getFileList() {
 		List<FilePojo> fileList = new ArrayList<FilePojo>();
         Grafeo g = new GrafeoImpl();
         log.info(Config.get(ConfigProp.ENDPOINT_QUERY));
         g.readTriplesFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), null, NS.RDF.PROP_TYPE, g.resource(NS.OMNOM.CLASS_FILE));
         for (GResource fileUri : g.findByClass(NS.OMNOM.CLASS_FILE)) {
         	log.info("Resource: " + fileUri.getUri());
         	if (null == fileUri.getUri()) {
         		log.warn("There is a blank node file without an ID in the triplestore.");
         	}
         	// FIXME possibly very inefficient
         	g.load(fileUri.getUri());
         	FilePojo fp = new FilePojo();
         	fp.loadFromURI(fileUri.getUri());
         	fileList.add(fp);
         }
         String jsonStr = OmnomJsonSerializer.serializeToJSON(fileList, FilePojo.class);
 		return Response.ok(jsonStr).build();
 	}
 	
 	/**
 	 * POST /empty
 	 * 
 	 * Posts an empty file
 	 * 
 	 * TODO This is meant to be used for placeholders, so that a service can
 	 * register a file and put it at the location later.
 	 * @return
 	 */
 	@POST
 	@Path("empty")
 	@Consumes(MediaType.MULTIPART_FORM_DATA)
 	public Response postEmptyFile(
 			@FormDataParam("meta") FormDataBodyPart metaPart
 			) {
 		URI uri = appendPath(popPath(getRequestUriWithoutQuery()), createUniqueStr());
 		Grafeo g = new GrafeoImpl();
 		Grafeo newG = new GrafeoImpl();
 		
 		FilePojo filePojo = new FilePojo();
 		try {
 			filePojo = storeAndDescribeFile(IOUtils.toInputStream(""), g, uri);
 		} catch (IOException e) {
 			return throwServiceError(e);
 		}
 		
 		FilePojo newFilePojo = null;
 		boolean metaPartIsEmpty = (metaPart == null) ? true : metaPart.getValueAs(String.class).equals("");
 		if (! metaPartIsEmpty) {
 			newG.readHeuristically(metaPart.getValueAs(String.class));
 			GResource res = newG.findTopBlank(NS.OMNOM.CLASS_FILE);
 			if (res == null) {
 				return throwServiceError(NS.OMNOM.CLASS_FILE, ErrorMsg.NO_TOP_BLANK_NODE);
 			}
 			res.rename(uri.toString());
 			newFilePojo = newG.getObjectMapper().getObject(FilePojo.class, uri);
 		}
 		
 		if (null != newFilePojo) {
 			try {
 				PojoUtils.copyProperties(filePojo, newFilePojo);
 			} catch (IllegalAccessException | InvocationTargetException e) {
 				throwServiceError(e);
 			}
 		}
 		
 		// save it
 		// set the status of the file to waiting
 		filePojo.setFileStatus(FileStatus.WAITING.toString());
 		filePojo.getGrafeo().putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), uri.toString());
 		
 		return Response.created(uri).entity(getResponseEntity(filePojo.getGrafeo())).build();
 	}
 	
 	/**
 	 * PUT /
 	 * TODO
 	 * @param metaPart
 	 * @param filePart
 	 * @param uriStr
 	 * @return
 	 */
 	@PUT
 	@Consumes(MediaType.MULTIPART_FORM_DATA)
 	public Response putFileAndOrMetadata(
 			@FormDataParam("meta") FormDataBodyPart metaPart,
 			@FormDataParam("file") FormDataBodyPart filePart,
 			@QueryParam("uri") String uriStr) {
 		URI uri;
 		try {
 			uri = getUriForString(uriStr);
 		} catch (URISyntaxException e) {
 			return throwServiceError(e);
 		}
 		boolean metaPartIsEmpty = (metaPart == null) ? true : metaPart.getValueAs(String.class).equals("");
 		boolean filePartIsEmpty = (filePart == null) ? true : filePart.getValueAs(String.class).equals("");
 			
 		FilePojo filePojo = new FilePojo();
 		try {
 			filePojo.loadFromURI(uriStr);
 		} catch (Exception e) {
 			log.error("Could reload job pojo." + e);
 			throwServiceError(e);
 		}
 		GrafeoImpl g = new GrafeoImpl();
 		
 		// if file data: store file at location
 		if (! filePartIsEmpty) {
 			InputStream fileInStream = filePart.getEntityAs(InputStream.class);
 			try {
 				filePojo = storeAndDescribeFile(fileInStream, g, uri);
 				filePojo.setFileStatus(FileStatus.AVAILABLE.toString());
 			} catch (IOException e) {
 				return throwServiceError("Couldn't write out file "+e);
 			}
 		}
 		
 		// if metadata: delete old data, create new data
 		if (! metaPartIsEmpty) {
 			
 			// build a model from the input
 			GrafeoImpl newG = new GrafeoImpl(metaPart.getValueAs(InputStream.class));
 			if (newG.getModel().isEmpty()) {
 				return throwServiceError(ErrorMsg.BAD_RDF);
 			}
 			
 			// rename the top blank node to uri
 			GResource res = newG.findTopBlank();
 			if (null != res) {
 				res.rename(uriStr);
 			} else {
 				res = newG.resource(uriStr);
 			}
 			
 			// instantiate new file pojo
 			FilePojo newFilePojo = newG.getObjectMapper().getObject(FilePojo.class, uriStr);
 			
 			// create SPARQL update query
 			SparqlUpdate sparul = new SparqlUpdate.Builder()
 				.graph(uriStr)
 				.delete("?s ?p ?o.")
 				.insert(newFilePojo.getNTriples())
 				.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
 				.build();
 			
 			log.info("About to replace: " + sparul.toString());
 			
 			// save the data
 			sparul.execute();
 		}
 		
 		return Response.ok().location(uri).build();
 	}
 	
 	/**
 	 * POST /
 	 *  A file can be uploaded and/or meta-data for the/a file can be saved to
 	 * the storage system. If no file is given, the metadata have to include the
 	 * location of the file where it can be found.
 	 * 
 	 * @todo If a client sends a multipart/form-data without multipart
 	 *       boundaries, this method is never reached and a silent 400 is sent
 	 *       to the client
 	 * 
 	 * @param metaPart
 	 *            the part with the metadata in RDF (either an upload or a
 	 *            simple field)
 	 * @param metaDisposition
 	 *            the content disposition of meta if meta is an upload
 	 * @param filePart
 	 *            the part with the file data (should be an upload)
 	 * @param fileDisposition
 	 *            the content disposition of meta if meta is an upload
 	 * @return
 	 * @return The complete RDF data about the file, which are stored by this
 	 *         service are returned.
 	 */
 	@POST
 	@Consumes(MediaType.MULTIPART_FORM_DATA)
 	public Response uploadFile(
 			@FormDataParam("meta") FormDataBodyPart metaPart,
 			@FormDataParam("meta") FormDataContentDisposition metaDisposition,
 			@FormDataParam("file") FormDataBodyPart filePart,
 			@FormDataParam("file") FormDataContentDisposition fileDisposition) {
         log.info("A new file is to be stored here.");
 		// The model for this resource
 		GrafeoImpl g = new GrafeoImpl();
 		// Identifier for this resource (used in URI generation and for filename)
 		// name of the new resource
 		URI uri = appendPath(getRequestUriWithoutQuery(), createUniqueStr());
 		log.info("Its URI will be: " + uri);
 		FilePojo filePojo = new FilePojo();
 		boolean metaPartIsEmpty = (metaPart == null) ? true : metaPart.getValueAs(String.class).equals("");
 		boolean filePartIsEmpty = (filePart == null) ? true : filePart.getValueAs(String.class).equals("");
 		
 		//
 		// Sanity check
 		//
 		if (filePartIsEmpty && metaPartIsEmpty) {
 			log.warn(ErrorMsg.NO_FILE_AND_NO_METADATA.toString());
 			return throwServiceError(ErrorMsg.NO_FILE_AND_NO_METADATA);
 		}
 
         log.info("Everything seems to be sane so far...");
 		// metadata is present
 		if (! metaPartIsEmpty) {
 			
 			String metaStr = metaPart.getValueAs(String.class);
 			
 			// try to read the metadata
 			try {
 				g.readHeuristically(metaStr);
 			} catch (RuntimeException e) {
 				return throwServiceError(ErrorMsg.BAD_RDF + "\n" + e);
 			}
 			
 			// rename top blank node to the newly minted URI if it exists
 			GResource uriRes = g.findTopBlank(NS.OMNOM.CLASS_FILE);
 			if (null == uriRes) {
 				Iterator<GResource> iter = g.findByClass(NS.OMNOM.CLASS_FILE).iterator();
 				if (iter.hasNext()) {
 					uriRes = iter.next();
 				}
 				else {
 					return throwServiceError(NS.OMNOM.CLASS_FILE, ErrorMsg.NO_RESOURCE_OF_CLASS);
 				}
 			}
 			else {
 				uriRes.rename(uri.toString());
 			}
 			
 			FilePojo f;
 			try {
 				f = g.getObjectMapper().getObject(FilePojo.class, uri);
 			} catch (Exception e) {
 				return throwServiceError(e);
 			}
 			// if the file part is null, make sure that a
 			// dm2e:file_retrieval_uri is provided in meta
 			if (filePartIsEmpty && null == f.getFileRetrievalURI()) {
 				log.error(ErrorMsg.NO_FILE_RETRIEVAL_URI + f.getId());
 				return throwServiceError(f.getTurtle(), ErrorMsg.NO_FILE_RETRIEVAL_URI);
 			}
 		}
 
         log.info("Metadata is processed.");
 
 		// There **is** a file to be processed
 		if (! filePartIsEmpty) { 
 			try {
                 log.info("We start to read the file...");
 				InputStream fileInStream = filePart.getEntityAs(InputStream.class);
 				// store and describe file
 				FilePojo newFilePojo = storeAndDescribeFile(fileInStream, g, uri);
 				PojoUtils.copyProperties(filePojo, newFilePojo);
 				filePojo.setFileStatus(FileStatus.AVAILABLE.toString());
 				log.error(filePojo.getFileStatus());
 
 				if (!filePart.isSimple()) {
 					// TODO this is wrong most of the time
 					filePojo.setFormat(filePart.getMediaType().toString());
 					filePojo.setOriginalName(fileDisposition.getFileName());
 				}
 			} catch (IOException | IllegalAccessException | InvocationTargetException e) {
                 log.error("An exception occured during file reading: " + e);
 				return throwServiceError(e);
 			}
 		}
 
         log.info("File is hopefully stored.");
 
         g.getObjectMapper().addObject(filePojo);
         log.debug(LogbackMarkers.DATA_DUMP, "Final RDF to be stored for this file: {}", g.getTurtle());
         
 		// store RDF data
 		g.putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), uri);
 		return Response.created(uri).entity(getResponseEntity(g)).build();
 	}
 	
 	/**
 	 * GET /dataByURI?uri=...
 	 * Returns the file. If the file is not stored by the file storage the
 	 * request is redirected. Otherwise the internal file is returned directly.
 	 * 
 	 * @param uriObject
 	 *            the identifier of the file.
 	 * @return the file or redirected to the location of the file.
 	 */
 	@GET
 	@Path("/dataByURI")
 	public Response getFileDataByUri(@QueryParam("uri") URI uri) {
 
 		// create model from graph at uri
 		GrafeoImpl g = new GrafeoImpl();
 		try {
 			g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), uri);
 		} catch (RuntimeException t) {
 			return throwServiceError(ErrorMsg.BAD_RDF, t);
 		}
 		
 		URI baseUri = popPath(getRequestUriWithoutQuery());
 
 		// Unless the URI is prefixed with the URI prefix the @POST methods uses
 		// i.e. if the URI is external, do a redirect
 		if ( ! uri.toString().startsWith(baseUri.toString())) {
 			// redirect
 			return Response.temporaryRedirect(uri).build();
 		}
 //		String path;
 //		try {
 //			path = g.firstMatchingObject(uri, PROP_DM2E_FILE_LOCATION).toString();
 //		} catch (NullPointerException e) {
 //			return throwServiceError("File location of this file is unknown.");
 //		}
 //		GLiteral contentTypeNode = (GLiteral) g.firstMatchingObject(uri, "dct:format");
 //		GLiteral originalNameNode = (GLiteral) g.firstMatchingObject(uri, "dm2e:original_name");
 //		String contentType = contentTypeNode != null ? contentTypeNode.toString() : "text/plain";
 //		String originalName = originalNameNode != null ? originalNameNode.toString() : "rdf_file_info";
 		
 		FilePojo filePojo = g.getObjectMapper().getObject(FilePojo.class, uri);
 		if (null == filePojo || null == filePojo.getInternalFileLocation()) {
 			return Response.status(404).entity(uri + " was not found.").build();
 		}
 		
 		FileInputStream fis;
 		try {
 			log.info(filePojo.getInternalFileLocation());
 			fis = new FileInputStream(filePojo.getInternalFileLocation());
 		} catch (FileNotFoundException e) {
 			log.info(e.toString());
 			return Response.status(404).entity(
 					"File '" + filePojo.getInternalFileLocation() + "' not found on the server. " + e.toString()).build();
 		}
 		return Response
 			.ok(fis)
 			.header("Content-Type", filePojo.getFormat())
 			.header("Content-Disposition", "attachment; filename=" + filePojo.getOriginalName())
 			.build();
 	}
 	
 	/**
 	 * DELETE /?uri=...
 	 * Delete a file from the metadata by marking it with dm2e:fileStatus @{link
 	 * {@link FileStatus} DELETED
 	 * 
 	 * @param uriStr
 	 *            The URI to delete
 	 * @return
 	 */
 	@DELETE
 	public Response deleteFileByUri(@QueryParam("uri") String uriStr) {
 
 		URI uri;
 		try {
 			uri = getUriForString(uriStr);
 		} catch (URISyntaxException e) {
 			return throwServiceError(e);
 		}
 
 		// replace all fileStatus statements
 //		@formatter:off
 		SparqlUpdate s = new SparqlUpdate.Builder()
 				.graph(uri.toString())
 				.delete(String.format(
 						"<%s> <%s> ?oldStatus",
 						uri,
 						NS.OMNOM.PROP_FILE_STATUS))
 				.insert(String.format(
 						"<%s> <%s> \"%s\"", 
 						uri, 
 						NS.OMNOM.PROP_FILE_STATUS,
 						FileStatus.DELETED.toString()))
 				.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
 				.build();
 //		@formatter:on
 		log.info(s.toString());
 		s.execute();
 
 		// return the metadata live from the store (g is not up-to-date anymore)
 		return getFileMetaDataByUri(uri);
 	}
 
 	/**
 	 * DELETE /{id}
 	 * Delete a locally stored file. TODO Only wraps deleteFileByUri for now
 	 * without doing on-disk deletion.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	@DELETE
 	@Path("{id}")
 	public Response deleteFileById(@PathParam("id") String id) {
 
 		URI uri = getRequestUriWithoutQuery();
 
 		/**
 		 * TODO the actual deletion of files on disk. The current storage
 		 * solution isn't secure, so this is commented out for now.
 		 */
 		// String path = g.firstMatchingObject(uri,
 		// PROP_DM2E_FILE_LOCATION).toString();
 		// File file = new File(path);
 		// file.delete();
 
 		return deleteFileByUri(uri.toString());
 	}
 
 	/**
 	 * POST /{id}/patch
 	 * Replace statements about a file with new statements.
 	 * @param uriStr
 	 * @param bodyInputStream
 	 * @return
 	 */
 	@POST
 	@Path("{id}/patch")
 	public Response updateStatements(InputStream bodyInputStream) throws Exception {
 		
 		// Check if the data is of a RDF content type
 		if (DM2E_MediaType.noRdfRequest(headers)) {
 			return throwServiceError("The request must be of a RDF type", 406);
 		}
 		
 		URI uri = getRequestUriWithoutQuery();
 		{
 			String uriStr = uri.toString().replaceFirst("/patch$", "");
 			uri = getUriForString(uriStr);
 		}
 		
 		// instantiate old filepojo
 		FilePojo filePojo = new FilePojo();
 		filePojo.loadFromURI(uri);
 		
 		// load posted RDF
 		GrafeoImpl g = new GrafeoImpl(bodyInputStream);
 		// rename the top blank node to uri if it exists
 		if (g.findTopBlank(NS.OMNOM.CLASS_FILE) != null) {
 			g.findTopBlank().rename(uri.toString());
 		}			
 		
 		// keep original filepojo
 		FilePojo origFilePojo = new FilePojo();
 		try {
 			PojoUtils.copyProperties(origFilePojo, filePojo);
 		} catch (IllegalAccessException | InvocationTargetException e) {
 			return throwServiceError(e);
 		}
 		
 		// build patch filepojo
 		FilePojo patchFilePojo = g.getObjectMapper().getObject(FilePojo.class, uri); 
 		// copy the information over
 		try {
 			PojoUtils.copyProperties(filePojo, patchFilePojo);
 		} catch (IllegalAccessException | InvocationTargetException e) {
 			return throwServiceError(e);
 		}
 		
 		// create SPARQL update query
 		SparqlUpdate sparul = new SparqlUpdate.Builder()
 		.graph(uri.toString())
 		.delete("?s ?p ?o.")
 		.insert(filePojo.getNTriples())
 		.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
 		.build();
 			
 		log.info(LogbackMarkers.DATA_DUMP, "About to replace: {}" + sparul.toString());
 			
 		// save the data
 		sparul.execute();
 		
 		// return the updated version to the client
 		return getFileMetaDataByUri(uri);
 	}
 	
 	/**
 	 * GET /byURI?uri=...
 	 *  Retrieve metadata/file data by passing a 'uri' parameter
 	 * Decides whether to fire the get method for file data or metadata.
 	 * 
 	 * @param uriStr
 	 * @return
 	 */
 	@GET
     @Path("byURI")
 	public Response getFileByUri(@QueryParam("uri") URI uri) {
 
         log.info("File requested: " + uri);
 		// if the accept header is a RDF type, send metadata, otherwise data
 		if (expectsMetadataResponse()) {
 			log.info("METADATA will be sent");
 			return getFileMetaDataByUri(uri);
 		} else {
             log.info("FILE will be sent");
             return getFileDataByUri(uri);
 		}
 	}
 	
 	/**
 	 * GET /metadataByUri?uri=...
 	 * Returns the metadata of a file identified by the given uri as either RDF or JSON.
 	 */
 	@GET
 	@Path("/metadataByUri")
 	public Response getFileMetaDataByUri(@QueryParam("uri") URI uri) {
 		if (expectsJsonResponse()) {
 			return getFileMetaDataAsJsonByUri(uri);
 		} else if (expectsRdfResponse()) {
 			return getFileMetaDataAsRdfByUri(uri);
 		} else {
 			log.warn("No suitable metadata type could be determined, defaulting to RDF.");
 			return getFileMetaDataAsRdfByUri(uri);
 		}
 	}
 	
 	/**
 	 * GET /jsonByUri?uri=...
 	 * Returns the metadata of a file identified by the given uri as RDF.
 	 * 
 	 * @param uri
 	 *            the file identifier
 	 * @return all stored meta-data about the file
 	 */
 	@GET
 	@Path("/jsonByUri")
 	public Response getFileMetaDataAsJsonByUri(@QueryParam("uri") URI uri) {
 		Grafeo g = new GrafeoImpl();
 		try {
 			g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), uri);
 		} catch (Exception e) {
 			log.info("Failed to read from " + Config.get(ConfigProp.ENDPOINT_QUERY));
 			return throwServiceError(e);
 //			throw new WebApplicationException(e);
 		}
 		if (!g.containsResource(uri)) {
 //			throw new WebApplicationException(404);
 			return Response.status(404).entity("No such file in the triplestore: " + uri).build();
 		}
 		FilePojo filePojo = g.getObjectMapper().getObject(FilePojo.class, uri);
 		return  Response.ok().entity(filePojo.toJson()).build();
 	}
 
 	/**
 	 * GET /rdfByUri?uri=...
 	 * Returns the metadata of a file identified by the given uri as RDF.
 	 * 
 	 * @param uri
 	 *            the file identifier
 	 * @return all stored meta-data about the file
 	 */
 	@GET
 	@Path("/rdfByUri")
 	public Response getFileMetaDataAsRdfByUri(@QueryParam("uri") URI uri) {
 		Grafeo g = new GrafeoImpl();
 		try {
 			g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), uri);
 		} catch (Exception e) {
 			log.info(Config.get(ConfigProp.ENDPOINT_QUERY));
 			return throwServiceError(e);
 		}
 		if (!g.containsResource(uri)) {
 			return Response.status(404).entity("No such file in the triplestore: " + uri).build();
 		}
 		FilePojo filePojo = g.getObjectMapper().getObject(FilePojo.class, uri);
 		Grafeo outG = new GrafeoImpl();
 		outG.getObjectMapper().addObject(filePojo);
 		return getResponse(outG);
 	}
 
     /**
 	 * Saves the sent file to disk and stores derived metadata in the graph
 	 * {@code uri} of Grafeo {@code g}
 	 * 
 	 * @para FileInputStream 
 	 *            The file as an InputStream
 	 * @param oldG
 	 *            The graph this file should be stored in
 	 * @param uri
 	 *            The URI that represents this file
 	 * @return 
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 * @throws NoSuchAlgorithmException
 	 * 
 	 */
 	private FilePojo storeAndDescribeFile(
 			InputStream fileInStream,
 			Grafeo oldG,
 			URI uri) throws FileNotFoundException, IOException {
 		// store the file
 		// TODO think about where to store
 		MessageDigest md = null;
         byte[] mdBytes = null;
 		try {
 			md = MessageDigest.getInstance("MD5");
             mdBytes = md.digest();
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException("MD5 algorithm not available: " + e, e);
 		}
 
 		DigestInputStream fileDigestInStream = new DigestInputStream(fileInStream, md);
 		// @formatter:off
 		// the file name will be the URI with everything non-alpha-numeric deleted
 		String uriStr = uri.toString();
 		String fileName = uriStr.replaceAll("[^A-Za-z0-9_]", "_");
 		fileName = fileName.replaceAll("__+", "_");
 		File f = new File(String.format(
 				"%s/%s",
 				Config.get(ConfigProp.FILE_STOREDIR),
 				fileName));
         if (!f.getParentFile().exists()) {
             f.getParentFile().mkdirs();
         }
 		log.info("Store file as: {}", f.getAbsolutePath());
 		IOUtils.copy(fileDigestInStream, new FileOutputStream(f));
 		// @formatter:on
 
 		// calculate and format checksum
 
 		StringBuilder mdStrBuilder = new StringBuilder();
         for (byte mdByte : mdBytes) {
             mdStrBuilder.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
         }
 		String mdStr = mdStrBuilder.toString();
 
 		// TODO add right predicates here
 		// store file-based/implicit metadata
 		FilePojo filePojo = new FilePojo();
 		filePojo.setMd5(mdStr);
 		filePojo.setInternalFileLocation(f.getAbsolutePath()); // TODO not a good "solution"
 		filePojo.setId(uri.toString());
 		filePojo.setFileRetrievalURI(uri.toString());
 		filePojo.setExtent(f.length());
		filePojo.setModified(DateTime.now());
 
 		// these are only available if this is an upload field and not just a
 		// form field
 		oldG.getObjectMapper().addObject(filePojo);
 		
 		return filePojo;
 	}
 }
