 package org.infobip.mpayments.help.repo.rest;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 
 import javax.ejb.Stateless;
 import javax.jcr.LoginException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.Property;
 import javax.jcr.Repository;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.SimpleCredentials;
 import javax.jcr.Value;
 import javax.jcr.Workspace;
 import javax.jcr.nodetype.NodeType;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.infobip.mpayments.help.dto.DocumentCvor;
 import org.infobip.mpayments.help.dto.DocumentCvorWrapper;
 import org.infobip.mpayments.help.dto.Paragraph;
 import org.infobip.mpayments.help.freemarker.FreeMarker;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Stateless
 public class RestAPIService implements RestAPI {
 
 	// private Session session = null;
 	private Repository repository = null;
 	private InitialContext initialContext = null;
 	private final static String jcrLocal = "java:jcr/local";
 	private final static SimpleCredentials cred;
 
 	static {
 		cred = new SimpleCredentials("admin", "admin".toCharArray());
 	}
 
 	private static final String defualtLanguage = "en";
 	private static final String defualtReseller = "";
 
 	static final Logger logger = LoggerFactory.getLogger(RestHelpRepoService.class);
 	private static ObjectMapper jsonMapper = new ObjectMapper();
 
 	/***************** REST API METHODS *******************/
 
 	@Override
 	public Response getParagraph(@PathParam("docPath") String docPath, @PathParam("parID") String parID,
 			@Context UriInfo ui) {
 		return getParagraph(docPath, parID, "", ui);
 	}
 
 	@Override
 	public Response getParagraph(@PathParam("docPath") String docPath, @PathParam("parID") String parID,
 			@PathParam("fieldPars") String fieldPars, @Context UriInfo ui) {
 
 		Session session = null;
 		boolean error = false;
 		InputStream input = null;
 		OutputStream output = null;
 		String result = null;
 		StringTokenizer stringTokenizer = null;
 		Map<String, Object> mapParameters = new HashMap<String, Object>();
 		String reseller = null;
 		String language = null;
 		String noSuchID = "No such ID!";
 		StringBuffer returnResult = new StringBuffer();
 		System.out.println("POZVANA METODA getParagraph");
 
 		try {
 			System.out.println("field par uri " + fieldPars);
 
 			if (fieldPars == null) {
 				error = true;
 				fieldPars = "";
 			}
 
 			if (fieldPars.startsWith("?"))
 				fieldPars = fieldPars.substring(1);
 
 			session = makeSession();
 
 			if (!fieldPars.equals("")) {
 				stringTokenizer = new StringTokenizer(fieldPars, "&=");
 				System.out.println("FIELD PARAMS " + fieldPars);
 				while (stringTokenizer.hasMoreTokens()) {
 					String first = stringTokenizer.nextToken();
 					String second = stringTokenizer.nextToken();
 					if ("reseller".equalsIgnoreCase(first)) {
 						reseller = second;
 						continue;
 					}
 					if ("language".equalsIgnoreCase(first)) {
 						language = second;
 						continue;
 					}
 					mapParameters.put(first, second);
 				}
 			}
 
 			if (language == null) {
 				language = defualtLanguage;
 			}
 			if (reseller == null) {
 				reseller = defualtReseller;
 			}
 
 			System.out.println("ispis mape");
 			for (Map.Entry<String, Object> entry : mapParameters.entrySet()) {
 				System.out.println(entry.getKey() + " = " + entry.getValue());
 			}
 
 			if (!docPath.startsWith("/")) {
 				docPath = "/" + docPath;
 			}
 
 			result = getDocument(docPath, fieldPars, ui).getEntity().toString();
 
 			if (!mapParameters.isEmpty()) {
 				FreeMarker fm = new FreeMarker();
 				result = fm.process(mapParameters, result);
 			}
 
 		} catch (Exception ex) {
 			error = true;
 			ex.printStackTrace();
 		} finally {
 			closeSession(session);
 			closeStreams(input, output);
 		}
 
 		if (!error) {
 			Document doc = Jsoup.parse(result);
 			Elements divs = doc.getElementsByTag("div");
 			for (Element elem : divs) {
 				if (elem.id().equals(parID)) {
 					// if (elem.className().equals(parID)) {
 					returnResult.append(elem.toString());
 				}
 			}
 			if ("".equals(returnResult.toString())) {
 				return Response.status(Response.Status.OK).entity(noSuchID).build();
 			} else {
 				return Response.status(Response.Status.OK).entity(returnResult.toString()).build();
 			}
 		} else {
 			return Response.status(Response.Status.NOT_FOUND).entity("error").build();
 		}
 	}
 
 	@Override
 	public Response getDocument(@PathParam("docPath") String docPath, @Context UriInfo ui) {
 		return getDocument(docPath, "", ui);
 	}
 
 	@Override
 	public Response getDocument(@PathParam("docPath") String docPath, @PathParam("fieldPars") String fieldPars,
 			@Context UriInfo ui) {
 		Session session = null;
 		boolean error = false;
 		InputStream input = null;
 		// OutputStream output = null;
 		String result = "";
 		StringTokenizer stringTokenizer = null;
 		Map<String, Object> mapParameters = new HashMap<String, Object>();
 		String reseller = null;
 		String language = null;
 		System.out.println("POZVANA METODA getDocument");
 
 		try {
 			System.out.println("field par uri " + fieldPars + " lang " + language + " res " + reseller);
 			System.out.println("docPath" + docPath);
 
 			if (fieldPars == null) {
 				error = true;
 				fieldPars = "";
 			}
 
 			if (fieldPars.startsWith("?"))
 				fieldPars = fieldPars.substring(1);
 
 			session = makeSession();
 
 			if (!fieldPars.equals("")) {
 				stringTokenizer = new StringTokenizer(fieldPars, "&=");
 				System.out.println("FIELD PARAMS " + fieldPars);
 				while (stringTokenizer.hasMoreTokens()) {
 					String first = stringTokenizer.nextToken();
 					String second = stringTokenizer.nextToken();
 					if ("reseller".equalsIgnoreCase(first)) {
 						reseller = second;
 						continue;
 					}
 					if ("language".equalsIgnoreCase(first)) {
 						language = second;
 						continue;
 					}
 					//System.out.println(first + " " + second);
 					mapParameters.put(first, second);
 				}
 			}
 
 			if (language == null || "null".equals(language)) {
 				language = defualtLanguage;
 				System.out.println("language je null " + language);
 			}
 			if (reseller == null || "null".equals(reseller)) {
 				reseller = defualtReseller;
 				System.out.println("reseller je null " + reseller);
 			}
 
 			if (language.equals("")) {
 				language = defualtLanguage;
 			}
 			
 			if (reseller.equals("")) {
 				reseller = defualtReseller;
 			}
 
 			System.out.println("language = " + language + " reseller = " + reseller +".");
 			
 			System.out.println("ispis mape");
 			for (Map.Entry<String, Object> entry : mapParameters.entrySet()) {
 				System.out.println(entry.getKey() + " = " + entry.getValue());
 			}
 
 			if (!docPath.startsWith("/")) {
 				docPath = "/" + docPath;
 			}
 
 			Workspace ws = session.getWorkspace();
 			QueryManager qm = ws.getQueryManager();
 			Query query = null;
 			QueryResult res = null;
 			NodeIterator it = null;
 			Node node = null;
 
 			if (!language.equals(defualtLanguage) && !reseller.equals(defualtReseller)) {
 				// Query for the specified reseller and language...
 				query = qm.createQuery("SELECT * FROM [mix:title]  WHERE [my:lang] = '" + language
 								+ "' and [my:reseller] = '" + reseller + "' and ISCHILDNODE([" + docPath + "])",Query.JCR_SQL2);
 				res = query.execute();
 				it = res.getNodes();
 				System.out.println("path 1 " + it.getSize());
 				if (it.hasNext()) {
 					node = it.nextNode();
 				}
 			} else {
 				
 				if (!reseller.equals(defualtReseller)) {
 					// Query for the specified reseller and default language.
 					query = qm.createQuery("SELECT * FROM [mix:title]  WHERE [my:lang] = '" + defualtLanguage
 									+ "' and [my:reseller] = '" + reseller + "' and ISCHILDNODE([" + docPath + "])",Query.JCR_SQL2);
 					res = query.execute();
 					it = res.getNodes();
 					System.out.println("path 2");
 					if (it.hasNext()) {
 						node = it.nextNode();
 					}
 				}
 				
 				if (!language.equals(defualtLanguage) && node == null) {
 					// Query for default reseller and the specified language.
 					query = qm.createQuery("SELECT * FROM [mix:title]  WHERE [my:lang] = '" + language
 							+ "' and [my:reseller] = '" + defualtReseller + "' and ISCHILDNODE([" + docPath + "])",Query.JCR_SQL2);
 					res = query.execute();
 					it = res.getNodes();
 					System.out.println("path 3");
 					if (it.hasNext()) {
 						node = it.nextNode();
 					}
 				} 
 				
 				if (node == null) {
 					// Query for default reseller and default language.
 					query = qm.createQuery("SELECT * FROM [mix:title]  WHERE [my:lang] = '" + defualtLanguage
 							+ "' and [my:reseller] = '" + defualtReseller + "' and ISCHILDNODE([" + docPath + "])",Query.JCR_SQL2);
 					res = query.execute();
 					it = res.getNodes();
 					System.out.println("path 4");
 					if (it.hasNext()) {
 						node = it.nextNode();
 					} else {
 						return Response.status(Response.Status.NOT_FOUND).entity("Not Found!").build();
 					}
 				}
 			}
 
 			if (node != null) {
 				Node content = node.getNode("jcr:content");
 				input = content.getProperty("jcr:data").getBinary().getStream();
 				result = RestAPIService.getStringFromInputStream(input).toString();
 			}
 
 			if (!result.equals("") && !mapParameters.isEmpty()) {
 				FreeMarker fm = new FreeMarker();
 				result = fm.process(mapParameters, result);
 			}
 
 		} catch (Exception ex) {
 			error = true;
 			ex.printStackTrace();
 		} finally {
 			closeSession(session);
 			closeStreams(input, null);
 		}
 
 		if (!error) {
 			return Response.status(Response.Status.OK).entity(result).build();
 		} else {
 			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error").build();
 		}
 	}
 
 	@Override
 	public Response delDocument(@PathParam("docPath") String docPath, @QueryParam("language") String language,
 			@QueryParam("reseller") String reseller, @Context UriInfo ui) {
 
 		Session session = null;
 		boolean error = false;
 		InputStream input = null;
 		OutputStream output = null;
 		boolean notEmpty = false;
 		boolean notFound = false;
 		String errorString = "error";
 		System.out.println("POZVANA METODA delDocument");
 		
 		try {
 			session = makeSession();
 
 			if (language == null && reseller == null) {
 				language = "";
 				reseller = "";
 			}
 
 			if (!docPath.startsWith("/")) {
 				docPath = "/" + docPath;
 			}
 
 			Workspace ws = session.getWorkspace();
 			QueryManager qm = ws.getQueryManager();
 			Query query = qm.createQuery("SELECT * FROM [mix:title]  WHERE [my:lang] = '" + language
 					+ "' and [my:reseller] = '" + reseller + "' and ISCHILDNODE([" + docPath + "])", Query.JCR_SQL2);
 
 			QueryResult res = query.execute();
 			NodeIterator it = res.getNodes();
 
 			Node node = null;
 			if (it.hasNext()) {
 
 				node = it.nextNode();
 				Node parent = node.getParent();
 				node.remove();
 
 				if (!parent.hasNodes()) {
 					parent.remove();
 				}
 
 			} else {
 				if (language.equals("") && reseller.equals("")) {
 					node = session.getNode(docPath);
 					if (!node.hasNodes()) {
 						node = session.getNode(docPath);
 						node.remove();
 					} else {
 						notEmpty = true;
 						errorString = "Folder not empty!";
 					}
 				} else {
 					notFound = true;
 					errorString = "Not found!";
 				}
 			}
 
 			session.save();
 
 			res = null;
 			it = null;
 
 		} catch (PathNotFoundException ex) {
 			notFound = true;
 			errorString = "Not found!";
 		} catch (Exception ex) {
 			error = true;
 			ex.printStackTrace();
 		} finally {
 			closeSession(session);
 			closeStreams(input, output);
 		}
 
 		if (notFound) {
 			return Response.status(Response.Status.NOT_FOUND).entity(errorString).build();
 		}
 		if (notEmpty) {
 			return Response.status(Response.Status.BAD_REQUEST).entity(errorString).build();
 		}
 		if (!error) {
 			return Response.status(Response.Status.OK).entity("Deleted node!").build();
 		} else {
 			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorString).build();
 		}
 
 	}
 
 	@Override
 	public Response getJSON(@PathParam("docPath") String docPath, @QueryParam("language") String language,
 			@QueryParam("reseller") String reseller, @Context UriInfo ui) {
 
 		Session session = null;
 		String response = null;
 		InputStream input = null;
 		String result = null;
 		String langResel = "";
 		String fieldPars = "";
 		Map<String, String> mapParameters = new HashMap<String, String>();
 		String docPathShort;
 		try {
 
 			if (docPath.contains("=")) {
 				fieldPars = docPath.substring(docPath.lastIndexOf("/") + 1, docPath.length());
 				docPathShort = docPath.substring(0, docPath.lastIndexOf("/"));
 			} else {
 				fieldPars = "";
 				docPathShort = docPath;
 			}
 
 			if (!fieldPars.equals("")) {
 				StringTokenizer stringTokenizer = new StringTokenizer(fieldPars, "&=");
 				System.out.println("FIELD PARAMS " + fieldPars);
 				while (stringTokenizer.hasMoreTokens()) {
 					String first = stringTokenizer.nextToken();
 					String second = stringTokenizer.nextToken();
 					if ("reseller".equalsIgnoreCase(first)) {
 						reseller = second;
 						continue;
 					}
 					if ("language".equalsIgnoreCase(first)) {
 						language = second;
 						continue;
 					}
 					System.out.println(first + " " + second);
 					mapParameters.put(first, second);
 				}
 			}
 
 			System.out.println("get jason " + docPath + " fp " + fieldPars);
 
 			if (language == null) {
 				language = defualtLanguage;
 				// fieldPars += "&language=en";
 			}
 			if (reseller == null) {
 				reseller = defualtReseller;
 				// fieldPars += "&reseller=1";
 			}
 
 			session = makeSession();
 			
 			result = (String) getDocument(docPathShort, fieldPars, ui).getEntity();
 			if (result == null)
 				result = "";
 			System.out.println("get jason novi " + result);
 
 			Document doc = Jsoup.parse(result);
 			Elements divs = doc.getElementsByTag("div");
 			Map<String, String> paragraphs = new TreeMap<String, String>();
 			for (Element elem : divs) {
 				if (!elem.id().isEmpty()) {
 					paragraphs.put(elem.id(), ui.getBaseUri().toString() + "documents/" + docPathShort
 							+ "/content/paragraph/" + elem.id() + "/" + fieldPars);
 
 				}
 			}
 
 			Node node = session.getNode("/" + docPathShort);
 			String[] niz = node.getPath().split("/");
 			System.out.println("LANGUAGE >>> " + language + " RESELLER >>> " + reseller);
 			
 			response = jsonMapper.defaultPrettyPrintingWriter().writeValueAsString(
 					getDocumentCvor("/" + docPathShort, paragraphs, "/" + fieldPars, ui, language, reseller));
 
 		} catch (JsonGenerationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (PathNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RepositoryException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NamingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally {
 			closeSession(session);
 		}
 		return Response.status(Response.Status.OK).entity(response).build();
 	}
 
 	@Override
 	public Response getChildrenLinksJSON(@PathParam("parent") String parentPath, @PathParam("fieldPars") String fieldPars,
 			@QueryParam("language") String language, @QueryParam("reseller") String reseller, @Context UriInfo ui) {
 
 		// openSession();
 		Session session = null;
 		Map<String, String> mapParameters = new HashMap<String, String>();
 		System.out.println("getChildrenLinksJSON " + fieldPars);
 
 		if (!fieldPars.equals("")) {
 			StringTokenizer stringTokenizer = new StringTokenizer(fieldPars, "&=");
 			System.out.println("FIELD PARAMS " + fieldPars);
 			while (stringTokenizer.hasMoreTokens()) {
 				String first = stringTokenizer.nextToken();
 				String second = stringTokenizer.nextToken();
 				if ("reseller".equalsIgnoreCase(first)) {
 					reseller = second;
 					continue;
 				}
 				if ("language".equalsIgnoreCase(first)) {
 					language = second;
 					continue;
 				}
 				System.out.println(first + " " + second);
 
 				mapParameters.put(first, second);
 			}
 		}
 
 		if (reseller == null) {
 			reseller = defualtReseller;
 		}
 		if (language == null) {
 			language = defualtLanguage;
 		}
 
 		String response = null;
 		Node node = null;
 		ArrayList<DocumentCvor> children_list = new ArrayList<DocumentCvor>();
 
		try {
 			session = makeSession();
 			node = session.getNode("/" + parentPath);
 		
			System.out.println("LANGUAGE >>> " + language + " RESELLER >>> " + reseller);
 			if (node.hasNodes()) {
 				for (NodeIterator nodeIterator = node.getNodes(); nodeIterator.hasNext();) {
 					Node subNode = nodeIterator.nextNode();
 
 					if (!subNode.isNodeType("nt:file")) {
 						//children_list.add(getDocumentCvor(subNode.getPath(), fieldPars, language, reseller, ui));
 						String  result = (String) getDocument(subNode.getPath(), fieldPars, ui).getEntity();
 
 						//System.out.println("get jason novi " + result);
 
 						if (result == null)
 							result = "";
 
 						Document doc = Jsoup.parse(result);
 						Elements divs = doc.getElementsByTag("div");
 						Map<String, String> paragraphs = new TreeMap<String, String>();
 						for (Element elem : divs) {
 							if (!elem.id().isEmpty()) {
 								paragraphs.put(elem.id(), ui.getBaseUri().toString() + "documents" + subNode.getPath()
 										+ "/content/paragraph/" + elem.id() + "/" + fieldPars);
 
 							}
 						}
 						children_list.add(getDocumentCvor(subNode.getPath(), paragraphs, "/" + fieldPars, ui, language, reseller));										
 					}
 					// children_list.add(getDocumentCvor(subNode.getPath(),
 					// language, reseller, ui));
 				}
 			}
 			DocumentCvorWrapper dcw = new DocumentCvorWrapper();
 			dcw.documents = children_list;
 			response = jsonMapper.defaultPrettyPrintingWriter().writeValueAsString(dcw);
 
 		} catch (PathNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (RepositoryException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (JsonGenerationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NamingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			closeSession(session);
 		}
 		return Response.status(Response.Status.OK).entity(response).build();
 	}
 
 	@Override
 	public Response getChildrenLinksJSON2(@PathParam("parent") String parent, @QueryParam("language") String language,
 			@QueryParam("reseller") String reseller, @Context UriInfo ui) {
 		return getChildrenLinksJSON(parent, "", language, reseller, ui);
 	}
 
 	// @Override
 	// public Response delDocument(@PathParam("docPath") String docPath) {
 	// return delDocument(docPath, "");
 	// }
 	//
 	/******************* UTIL METHODS *************************/
 	private void printChildren(Node node) throws RepositoryException {
 		if (node.hasNodes()) {
 			NodeIterator it = node.getNodes();
 			while (it.hasNext()) {
 				Node child = it.nextNode();
 				printChildren(child);
 			}
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private String readFile(File file) throws IOException {
 		StringBuilder fileContents = new StringBuilder((int) file.length());
 		Scanner scanner = new Scanner(file);
 		String lineSeparator = System.getProperty("line.separator");
 		try {
 			while (scanner.hasNextLine()) {
 				fileContents.append(scanner.nextLine() + lineSeparator);
 			}
 			return fileContents.toString();
 		} finally {
 			scanner.close();
 		}
 	}
 
 	private void closeStreams(InputStream input, OutputStream output) {
 		if (input != null)
 			try {
 				input.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		if (output != null)
 			try {
 				output.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	}
 
 	/*
 	 * public void openSession() { try { if(session == null){
 	 * logger.info("start OPEN SESSION in RestAPIService."); initialContext =
 	 * new InitialContext(); repository = (Repository)
 	 * initialContext.lookup("java:jcr/local"); session = repository.login(new
 	 * SimpleCredentials("admin", "admin".toCharArray()));
 	 * logger.info("SESSION OPENED in RestAPIService."); } } catch
 	 * (NamingException e) { // TODO Auto-generated catch block
 	 * e.printStackTrace(); } catch (LoginException e) { // TODO Auto-generated
 	 * catch block e.printStackTrace(); } catch (RepositoryException e) { //
 	 * TODO Auto-generated catch block e.printStackTrace(); } }
 	 * 
 	 * public void closeSession() { if(session != null ){ session.logout(); //
 	 * release memory session = null; repository = null; initialContext = null;
 	 * logger.info("SESSION CLOSED in RestAPIService."); } }
 	 */
 
 	public DocumentCvor getDocumentCvor(String parent, Map<String, String> paragraphs, String fieldPars, UriInfo ui,
 			String language, String reseller) {
 
 		logger.info("PARENT: {}", parent);
 		Node node = null;
 		Session session = null;
 		DocumentCvor dnl = null;
 		try {
 
 			session = makeSession();
 
 			node = session.getNode(parent);
 			String[] niz = node.getPath().split("/");
 			String baseUri = ui.getBaseUri().toString().substring(0, ui.getBaseUri().toString().length() - 1);
 
 			// ubacivanje title-a
 			String title = null;
 
 			if (reseller == null) {
 				reseller = defualtReseller;
 			}
 			if (language == null) {
 				language = defualtLanguage;
 			}
 
 			if (node.hasProperty("my:title")) {
 				Property p = node.getProperty("my:title");
 				Value[] v = p.getValues();
 				for (Value value : v) {
 					String[] mtitle = value.getString().split("#");
 
 					if (mtitle[1].equals(reseller)) {
 						if (mtitle[0].equals(language)) {
 							title = mtitle[2];
 						} else if (mtitle[0].equals("en")) {
 							title = mtitle[2];
 						}
 					} else if (mtitle[1].equals("centili")) {
 						if (mtitle[0].equals(language)) {
 							title = mtitle[2];
 						} else if (mtitle[0].equals("en")) {
 							title = mtitle[2];
 						}
 					}
 				}
 			}
 
 			if (title == null) {
 				System.out.println("Node " + node.getName() + " nema odgovarajuci properti");
 				title = node.getName();
 			}
 
 			dnl = new DocumentCvor(title, niz[1].toUpperCase(), ui.getBaseUri().toString() + "documents"
 					+ node.getPath() + fieldPars, ui.getBaseUri().toString() + "documents" + node.getParent().getPath());
 
 			List<Paragraph> lista = new ArrayList<Paragraph>();
 			Paragraph p = null;
 			for (Map.Entry<String, String> entry : paragraphs.entrySet()) {
 				p = new Paragraph();
 				p.setKey(entry.getKey());
 				p.setLink(entry.getValue());
 				lista.add(p);
 			}
 			dnl.setParagraphs(lista);
 			if (node.hasNodes()) {
 				if (hasFolder(node)) {
 					String children_href = ui.getBaseUri().toString() + "documents" + node.getPath() + "/children";
 
 					dnl.setChildren_href(children_href + fieldPars);
 				} else {
 					dnl.setChildren_href("");
 				}
 
 				if (hasFiles(node)) {
 					String content_href = ui.getBaseUri().toString() + "documents" + node.getPath() + "/content";
 					dnl.setContent_href(content_href + fieldPars);
 				} else {
 					dnl.setContent_href("");
 				}
 			} else {
 				dnl.setChildren_href("");
 				dnl.setContent_href("");
 			}
 
 		} catch (PathNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (RepositoryException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (NamingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			closeSession(session);
 		}
 		return dnl;
 
 	}
 
 	public DocumentCvor getDocumentCvor(String parent, String fieldPars, String language, String reseller,
 			@Context UriInfo ui) {
 
 		Session session = null;
 		Node node = null;
 		DocumentCvor dnl = null;
 		try {
 
 			session = makeSession();
 
 			node = session.getNode(parent);
 			String title = null;
 
 			if (reseller == null) {
 				reseller = defualtReseller;
 			}
 			if (language == null) {
 				language = defualtLanguage;
 			}
 
 			System.out.println("fp u getDocumentCvor " + fieldPars);
 
 			if (node.hasProperty("my:title")) {
 				Property p = node.getProperty("my:title");
 				Value[] v = p.getValues();
 				for (Value value : v) {
 					String[] mtitle = value.getString().split("#");
 
 					if (mtitle[1].equals(reseller)) {
 						if (mtitle[0].equals(language)) {
 							title = mtitle[2];
 						} else if (mtitle[0].equals("en")) {
 							title = mtitle[2];
 						}
 					} else if (mtitle[1].equals("centili")) {
 						if (mtitle[0].equals(language)) {
 							title = mtitle[2];
 						} else if (mtitle[0].equals("en")) {
 							title = mtitle[2];
 						}
 
 					}
 				}
 			}
 
 			if (title == null) {
 				System.out.println("Node " + node.getName() + " nema odgovarajuci properti");
 				title = node.getName();
 			}
 
 			String[] niz = node.getPath().split("/");
 
 			dnl = new DocumentCvor(title, niz[1].toUpperCase(), ui.getBaseUri().toString() + "documents"
 					+ node.getPath() + "/" + fieldPars, ui.getBaseUri().toString() + "documents"
 					+ node.getParent().getPath());
 
 			if (node.hasNodes()) {
 				if (hasFolder(node)) {
 					String children_href = node.getPath() + "/children/" + fieldPars;
 					dnl.setChildren_href(ui.getBaseUri().toString() + "documents" + children_href);
 				} else {
 					dnl.setChildren_href("");
 				}
 
 				if (hasFiles(node)) {
 					String content_href = node.getPath() + "/content/" + fieldPars;
 					dnl.setContent_href(ui.getBaseUri().toString() + "documents" + content_href);
 				} else {
 					dnl.setContent_href("");
 				}
 			} else {
 				dnl.setChildren_href("");
 				dnl.setContent_href("");
 			}
 
 		} catch (PathNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (RepositoryException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (NamingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			closeSession(session);
 		}
 		return dnl;
 
 	}
 
 	public boolean hasFolder(Node node) throws RepositoryException, NamingException {
 		Session session = makeSession();
 		for (NodeIterator nodeIterator = node.getNodes(); nodeIterator.hasNext();) {
 			Node subNode = nodeIterator.nextNode();
 			if (subNode.getPrimaryNodeType().getName().equals("nt:folder")) {
 				return true;
 			}
 		}
 
 		closeSession(session);
 		return false;
 	}
 
 	public static StringBuilder getStringFromInputStream(InputStream is) {
 		BufferedReader br = null;
 		StringBuilder sb = new StringBuilder();
 		String line;
 		try {
 			br = new BufferedReader(new InputStreamReader(is));
 			while ((line = br.readLine()) != null) {
 				sb.append(line);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return sb;
 	}
 
 	public boolean hasFiles(Node node) throws RepositoryException, NamingException {
 
 		Session session = makeSession();
 		for (NodeIterator nodeIterator = node.getNodes(); nodeIterator.hasNext();) {
 			Node subNode = nodeIterator.nextNode();
 			if (subNode.getPrimaryNodeType().getName().equals("nt:file")) {
 				return true;
 			}
 		}
 
 		closeSession(session);
 		return false;
 	}
 
 	private Session makeSession() throws NamingException, LoginException, RepositoryException {
 		InitialContext initialContext = new InitialContext();
 		Repository repository = (Repository) initialContext.lookup(jcrLocal);
 		Session session = repository.login(cred);
 		return session;
 	}
 
 	private Session closeSession(Session session) {
 		if (session != null) {
 			session.logout();
 			return null;
 		}
 		return session;
 	}
 
 }
