 package org.esupportail.ecm.intranets;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.*;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nuxeo.common.utils.URIUtils;
 import org.nuxeo.ecm.core.api.Blob;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.NuxeoPrincipal;
 import org.nuxeo.ecm.core.api.model.Property;
 import org.nuxeo.ecm.core.api.model.PropertyException;
 import org.nuxeo.ecm.core.api.PathRef;
 import org.nuxeo.ecm.core.rest.DocumentRoot;
 import org.nuxeo.ecm.platform.ui.web.auth.NXAuthConstants;
 import org.nuxeo.ecm.platform.ui.web.auth.NuxeoAuthenticationFilter;
 import org.nuxeo.ecm.platform.ui.web.auth.service.PluggableAuthenticationService;
 import org.nuxeo.ecm.webengine.WebException;
 import org.nuxeo.ecm.webengine.model.*;
 import org.nuxeo.ecm.webengine.model.exceptions.*;
 import org.nuxeo.ecm.webengine.model.impl.*;
 import org.nuxeo.runtime.api.Framework;
 
 @WebObject(type = "esupintranets")
 @Produces("text/html; charset=UTF-8")
 public class Main extends ModuleRoot {
 
 	public static final Log log = LogFactory.getLog(Main.class);
	protected String[] sectionPaths;
 
 	public Main() {
 		Map<Object, Object> intranetsProperties = new HashMap<Object, Object>();
 		Properties p = new Properties();
 		URL url = Main.class.getClassLoader().getResource(
 				"OSGI-INF/esup-intranets.properties");
 		InputStream in = null;
 		try {
 			in = url.openStream();
 			p.load(in);
 			intranetsProperties.putAll(p);
 		} catch (IOException e) {
 			throw new Error("Failed to load mime types");
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		String sections = (String) intranetsProperties.get("section.path");
 		sectionPaths = sections.split(":");
 	}	
 
 	/**
 	 *	Get repository
 	 */
 	@Path("repository/{index}/")
 	public Object getRepository(@PathParam("index") int index) {
 		String sectionPath = sectionPaths[index];
 		ctx.setProperty("sectionPath", sectionPath);
 		ctx.setProperty("index", index);
 		return new DocumentRoot(ctx, sectionPath);
 	}
 
 	/**
 	 *	Get file
 	 */
 	@GET
 	@Path("file/{index}/{path:.*}")
 	public Object getFile(@PathParam("index") int index, @PathParam("path") String path) 
 			throws PropertyException, ClientException {
 		Object requestedObject;
 		Property propertyFile = null;
 		String requestedFilename = "";
 		Blob requestedBlob = null;
 		String sectionPath = sectionPaths[index];
 		path = sectionPath + path;
 		CoreSession session = ctx.getCoreSession();
 		DocumentRef docRef = new PathRef(path);
 		DocumentModel doc = null;
 		//DocumentModel versionDoc = null;
 		ctx.setProperty("sectionPath", sectionPath);
 		log.error("getFile");
 		log.error("path :: " + path);
 		//doc = session.getDocument(docRef);
 		//propertyFile = doc.getProperty("file:content");
 		DocumentModelList proxies = session.getProxies(docRef, null);
 		if (proxies.size() > 0) {
 			doc = proxies.get(0);
 		}
 		else {
 			doc = session.getDocument(docRef);			
 		}
 		if (doc.getType().equals("Picture")) {
 			List<Property> list = (List<Property>) doc.getProperty("picture:views");
 			Property property = list.get(0);
 			propertyFile = property.get("content");
 		}
 		else {
 			propertyFile = doc.getProperty("file:content");			
 		}
 		if (propertyFile != null) {
 			requestedBlob = (Blob) propertyFile.getValue();
 			if (requestedBlob == null) {
 				throw new WebResourceNotFoundException(
 						"No attached file at " + "file:content");
 			}
 			requestedFilename = requestedBlob.getFilename();
 			if (requestedFilename == null) {
 				propertyFile = propertyFile.getParent();
 				if (propertyFile.isComplex()) {
 					try {
 						requestedFilename = (String) propertyFile
 								.getValue("filename");
 					} catch (PropertyException e) {
 						requestedFilename = "Unknown";
 					}
 				}
 			}
 			requestedObject = Response.ok(requestedBlob).header(
 					"Content-Disposition",
 					"inline; filename=" + requestedFilename).type(
 							requestedBlob.getMimeType()).build();
 			return requestedObject;
 		}
 		return this;
 	}
 
 	/**
 	 * @see org.nuxeo.ecm.webengine.model.impl.ModuleRoot#handleError(javax.ws.rs.WebApplicationException)
 	 */
 	@Override
 	public Object handleError(WebApplicationException e) {
 		if (e instanceof WebSecurityException) {
 			CoreSession coreSession = ctx.getCoreSession();
 			NuxeoPrincipal user = (NuxeoPrincipal) coreSession.getPrincipal();
 			if (user.isAnonymous()) {
 				Map<String, String> urlParameters = new HashMap<String, String>();
 				urlParameters.put(NXAuthConstants.SECURITY_ERROR, "true");
 				urlParameters.put(NXAuthConstants.FORCE_ANONYMOUS_LOGIN, "true");
 				if (ctx.getRequest().getAttribute(NXAuthConstants.REQUESTED_URL) != null) {
 					urlParameters.put(NXAuthConstants.REQUESTED_URL,(String) ctx.getRequest().getAttribute(NXAuthConstants.REQUESTED_URL));
 				}
 				else {
 					urlParameters.put(NXAuthConstants.REQUESTED_URL, NuxeoAuthenticationFilter.getRequestedUrl(ctx.getRequest()));
 				}
 				String baseURL = "";
 				try {
 					baseURL = initAuthenticationService().getBaseURL(ctx.getRequest())+ NXAuthConstants.LOGOUT_PAGE;
 				} 
 				catch (ClientException a) {
 					throw WebException.wrap(a);					
 				}
 				ctx.getRequest().setAttribute(NXAuthConstants.DISABLE_REDIRECT_REQUEST_KEY, true);
 				baseURL = URIUtils.addParametersToURIQuery(baseURL, urlParameters);
 				log.debug("baseURL = " + baseURL);
 				ResponseBuilder responseBuilder;
 				try {
 					responseBuilder = Response.seeOther(new URI(baseURL));
 				} catch (URISyntaxException e2) {
 					throw WebException.wrap(e2);
 				}
 				Response requestedObject = responseBuilder.build();
 				return requestedObject;
 			}
 		}
 		return super.handleError(e);
 	}
 
 	/**
 	 * Default view
 	 */
 	@GET
 	@Path("/")
 	public Object doGet() {
 		List<String> paths = new ArrayList<String>();
 		for (String section : sectionPaths) {
 			paths.add(section);
 		}
 		ctx.setProperty("paths", paths);
 		return getView("index");
 	}
 
 	/**
 	 * search form
 	 */
 	@GET
 	@Path("form/{index}/")
 	public Object getViewForm(@PathParam("index") int index) {
 		ctx.setProperty("index", index);
 		return getView("form");
 	}
 
 	/**
 	 *	Get tree view
 	 */
 	@GET
 	@Path("tree/{index}/")
 	public Object getViewTree(@PathParam("index") int index) {
 		String sectionPath = sectionPaths[index];
 		ctx.setProperty("sectionPath", sectionPath);
 		ctx.setProperty("index", index);
 		return getView("tree").arg("doc", new DocumentRoot(ctx, sectionPath));
 	}
 
 	/**
 	 *	Search result
 	 */
 	@GET
 	@Path("@search")
 	public Object search() {
 		int index = Integer.parseInt(ctx.getRequest().getParameter("index"));
 		String sectionPath = sectionPaths[index];
 		ctx.setProperty("sectionPath", sectionPath);
 		ctx.setProperty("index", index);
 		String query = ctx.getRequest().getParameter("query");
 		if (query == null) {
 			String fullText = ctx.getRequest().getParameter("fullText");
 			if (fullText == null) {
 				throw new IllegalParameterException("Expecting a query or a fullText parameter");
 			}
 			String orderBy = ctx.getRequest().getParameter("orderBy");
 			String orderClause = "";
 			if (orderBy != null) {
 				orderClause = " ORDER BY " + orderBy;
 			}
 //			query = "SELECT * FROM Document WHERE (ecm:fulltext = \"" + fullText
 //					+ "\") OR (ecm:name LIKE \"%" + fullText 
 //					+ "%\") AND (ecm:isCheckedInVersion = 0) AND (ecm:path STARTSWITH \"" + sectionPath + "\")" + orderClause;
 			query = "SELECT * FROM Document WHERE (ecm:fulltext = \"" + fullText
 					+ "\") AND (ecm:isCheckedInVersion = 0) AND (ecm:path STARTSWITH \"" + sectionPath + "\")" + orderClause;
 		}
 		try {
 			DocumentModelList docs = ctx.getCoreSession().query(query);
 			return getView("search").arg("query", query).arg("result", docs);
 		} catch (ClientException e) {
 			throw WebException.wrap(e);
 		}
 	}
 
 	/**
 	 *      get latests documents
 	 */
 	@GET
 	@Path("news/{index}/")
 	public Object getNews(@PathParam("index") int index) {
 		String sectionPath = sectionPaths[index];
 		ctx.setProperty("sectionPath", sectionPath);
 		ctx.setProperty("index", index);
 		String query = "SELECT * FROM Document WHERE (ecm:path STARTSWITH \"" + sectionPath + "\")"
 				+ " AND (ecm:primaryType = 'File') ORDER BY dc:modified DESC ";
 		try {
 			DocumentModelList docs = ctx.getCoreSession().query(query, 10);
 			return getView("news").arg("query", query).arg("result", docs);
 		} catch (ClientException e) {
 			throw WebException.wrap(e);
 		}
 	}
 
 	/**
 	 *      rss feed
 	 */
 	@GET
 	@Path("rss/{index}/")
 	public Object getRSS(@PathParam("index") int index) {
 		String sectionPath = sectionPaths[index];
 		ctx.setProperty("sectionPath", sectionPath);
 		ctx.setProperty("index", index);
 		String query = "SELECT * FROM Document WHERE (ecm:path STARTSWITH \"" + sectionPath + "\")"
 				+ " AND (ecm:primaryType = 'File') ORDER BY dc:modified DESC ";
 		try {
 			DocumentModelList docs = ctx.getCoreSession().query(query, 10);
 			return getView("rss").arg("query", query).arg("result", docs);
 		} catch (ClientException e) {
 			throw WebException.wrap(e);
 		}
 	}
 
 	protected PluggableAuthenticationService initAuthenticationService() throws ClientException {
 		PluggableAuthenticationService service = (PluggableAuthenticationService) Framework.getRuntime().getComponent(PluggableAuthenticationService.NAME);
 		if (service == null) {
 			log.error("Unable to get Service " + PluggableAuthenticationService.NAME);
 			throw new ClientException("Can't initialize Nuxeo Pluggable Authentication Service");
 		}
 		return service;
 	}
 }
 
