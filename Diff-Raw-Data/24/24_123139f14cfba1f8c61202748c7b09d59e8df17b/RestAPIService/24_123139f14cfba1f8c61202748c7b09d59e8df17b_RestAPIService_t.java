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
 
 	static final Logger logger = LoggerFactory.getLogger(RestHelpRepoService.class);
 
 	@Override
 	public Response getParagraph(@PathParam("docPath") String docPath, @PathParam("parID") String parID, @Context UriInfo ui) {
 		return getParagraph(docPath, parID, "", ui);
 	}
 
 	@Override
 	public Response getParagraph(@PathParam("docPath") String docPath, @PathParam("parID") String parID,
 			@PathParam("fieldPars") String fieldPars, @Context UriInfo ui) {
 
 		Session session = null;
 		Repository repository = null;
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
 
 			InitialContext initialContext = new InitialContext();
 			repository = (Repository) initialContext.lookup("java:jcr/local");
 			session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
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
 				language = "en";
 			}
 			if (reseller == null) {
 				reseller = "";
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
 
 //			res = null;
 //			it = null;
 
 		} catch (Exception ex) {
 			error = true;
 			ex.printStackTrace();
 		} finally {
 			if (session != null)
 				session.logout();
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
 
 	// public Response getDocuments(@PathParam("id") String id) {
 	// Session session = null;
 	// Repository repository = null;
 	// boolean error = false;
 	// InputStream input = null;
 	// OutputStream output = null;
 	// String result = null;
 	// System.out.println("POZVANA METODA getDocuments");
 	// try {
 	// InitialContext initialContext = new InitialContext();
 	// repository = (Repository) initialContext.lookup("java:jcr/local");
 	// session = repository.login(new SimpleCredentials("admin",
 	// "admin".toCharArray()));
 	//
 	// Node en = session.getNode("/help/pp/service/1/en");
 	// Node target = session.getNodeByIdentifier(en.getIdentifier());
 	//
 	// printChildren(session.getNode("/"));
 	// Node content = target.getNode("jcr:content");
 	// input = content.getProperty("jcr:data").getBinary().getStream();
 	// result = RestAPIService.getStringFromInputStream(input).toString();
 	//
 	// } catch (Exception ex) {
 	// error = true;
 	// ex.printStackTrace();
 	// } finally {
 	// if (session != null)
 	// session.logout();
 	// closeStreams(input, output);
 	// }
 	//
 	// if (!error) {
 	// return Response.status(Response.Status.OK).entity(result).build();
 	// } else {
 	// return
 	// Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error").build();
 	// }
 	// }
 
 	// za staro drvo
 	// @Override
 	public Response getDoc(@PathParam("rPath") String rPath) {
 		Session session = null;
 		Repository repository = null;
 		boolean error = false;
 		InputStream input = null;
 		OutputStream output = null;
 		String result = null;
 		System.out.println("POZVANA METODA getDoc");
 		try {
 			InitialContext initialContext = new InitialContext();
 			repository = (Repository) initialContext.lookup("java:jcr/local");
 			session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
 
 			System.out.println("rpath " + rPath);
 			Node target = session.getNode("/" + rPath);
 			input = target.getProperty("jcr:data").getBinary().getStream();
 			result = RestAPIService.getStringFromInputStream(input).toString();
 
 		} catch (Exception ex) {
 			error = true;
 			ex.printStackTrace();
 		} finally {
 			if (session != null)
 				session.logout();
 			closeStreams(input, output);
 		}
 
 		if (!error) {
 			return Response.status(Response.Status.OK).entity(result).build();
 		} else {
 			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error").build();
 		}
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
 
 	private void printChildren(Node node) throws RepositoryException {
 		if (node.hasNodes()) {
 			NodeIterator it = node.getNodes();
 			while (it.hasNext()) {
 				Node child = it.nextNode();
 				printChildren(child);
 			}
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
 		Repository repository = null;
 		boolean error = false;
 		InputStream input = null;
 		OutputStream output = null;
		String result = "";
 		StringTokenizer stringTokenizer = null;
 		Map<String, Object> mapParameters = new HashMap<String, Object>();
 		String reseller = null;
 		String language = null;
		boolean hasChild = false;
 		boolean langAndReseller = false;
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
 
 			InitialContext initialContext = new InitialContext();
 			repository = (Repository) initialContext.lookup("java:jcr/local");
 			session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
 			if (!fieldPars.equals("")) {
 				stringTokenizer = new StringTokenizer(fieldPars, "&=");
 				System.out.println("FIELD PARAMS " + fieldPars);
 				while (stringTokenizer.hasMoreTokens()) {
 					String first = stringTokenizer.nextToken();
 					String second = stringTokenizer.nextToken();
 					if ("reseller".equalsIgnoreCase(first)) {
 						reseller = second;
 						langAndReseller = true;
 						continue;
 					}
 					if ("language".equalsIgnoreCase(first)) {
 						language = second;
 						langAndReseller = true;
 						continue;
 					}
 					System.out.println(first + " " + second);
 					mapParameters.put(first, second);
 				}
 			}
 
 			if (language == null) {
 				language = "en";
 			}
 
 			if (language.equals("")) {
 				language = "en";
 			}
 
 			if (reseller == null) {
 				reseller = "";
 			}
 
 			System.out.println("jezik= " + language + " preprodavac= " + reseller);
 
 			if (language == null || "null".equals(language)) {
 				language = "en";
 				System.out.println("language je null " + language);
 			}
 			if (reseller == null || "null".equals(reseller)) {
 				reseller = "1";
 				System.out.println("reseller je null " + reseller);
 			}
 
 			System.out.println("ispis mape");
 			for (Map.Entry<String, Object> entry : mapParameters.entrySet()) {
 				System.out.println(entry.getKey() + " = " + entry.getValue());
 			}
 
 			if (!docPath.startsWith("/")) {
 				docPath = "/" + docPath;
 			}
 
 			// StringBuffer bufferPath = new StringBuffer("");
 			// StringTokenizer st = new StringTokenizer(docPath,">");
 			//
 			// while(st.hasMoreTokens()){
 			// bufferPath.append("/"+st.nextToken());
 			// }
 
 			Workspace ws = session.getWorkspace();
 			QueryManager qm = ws.getQueryManager();
 			// System.out.println("query " +
 			// "SELECT * FROM [mix:title]  WHERE [my:lang] = '"+language+
 			// "' and [my:reseller] = '" +reseller+"'");
 			// Query query =
 			// qm.createQuery("SELECT * FROM [mix:title]  WHERE [my:lang] = '"+language+
 			// "' and [my:reseller] = '" +reseller+"'", Query.JCR_SQL2);
 			Query query = qm.createQuery("SELECT * FROM [mix:title]  WHERE [my:lang] = '" + language
 					+ "' and [my:reseller] = '" + reseller + "' and ISCHILDNODE([" + docPath + "])", Query.JCR_SQL2);
 			QueryResult res = query.execute();
 			NodeIterator it = res.getNodes();
 
 			//System.out.println(""+it.getSize());
 			
 			Node node = null;
 			if (it.hasNext()) {
 				node = it.nextNode();
 				//System.out.println("path " + node.getPath());
 				Node content = node.getNode("jcr:content");
 				input = content.getProperty("jcr:data").getBinary().getStream();
 				result = RestAPIService.getStringFromInputStream(input).toString();
 
 				if (!mapParameters.isEmpty()) {
 					FreeMarker fm = new FreeMarker();
 					result = fm.process(mapParameters, result);
 				}
 			} else {
 
 				System.out.println("else page " + langAndReseller + " " + docPath);
 				if (!langAndReseller) {
 					node = session.getNode(docPath);
 					System.out.println("trazi samo path " + docPath);
 					NodeIterator nodeIt = node.getNodes();
 					Node node2 = null;
 					while (nodeIt.hasNext()) {
 						node2 = nodeIt.nextNode();
 						if (node2.isNodeType(NodeType.NT_FILE)) {
							hasChild = true;
 							break;
 						}
 					}
					if(hasChild){
						Node content = node2.getNode("jcr:content");
						input = content.getProperty("jcr:data").getBinary().getStream();
						result = RestAPIService.getStringFromInputStream(input).toString();

						if (!mapParameters.isEmpty()) {
							FreeMarker fm = new FreeMarker();
							result = fm.process(mapParameters, result);
						}
 					}
 				}else{
 					node = session.getNode(docPath);
 					System.out.println("trazi samo path " + docPath);
 					NodeIterator nodeIt = node.getNodes();
 					Node node2 = null;
 					while (nodeIt.hasNext()) {
 						node2 = nodeIt.nextNode();
 						if (node2.isNodeType(NodeType.NT_FILE)) {
 							break;
 						}
 					}
 				
 					Node content = node2.getNode("jcr:content");
 					input = content.getProperty("jcr:data").getBinary().getStream();
 					result = RestAPIService.getStringFromInputStream(input).toString();
 					
 					if (!mapParameters.isEmpty()) {
 						FreeMarker fm = new FreeMarker();
 						result = fm.process(mapParameters, result);
 					}
 					
 				}
 				// error = true;
 //<<<<<<< Upstream, based on branch 'master' of https://github.com/dimce011/campus201305.git
 //
 //=======
 //>>>>>>> b67f029 Rest API v7 DocumentCvorWrapper renamed RestHelpRepoService changed a bit about DocumentCvorWrapper
 			}
 
 			res = null;
 			it = null;
 
 		} catch (Exception ex) {
 			error = true;
 			ex.printStackTrace();
 		} finally {
 			if (session != null)
 				session.logout();
 			closeStreams(input, output);
 		}
 
 		if (!error) {
 			return Response.status(Response.Status.OK).entity(result).build();
 		} else {
 			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error").build();
 		}
 	}
 
 	@Override
 	public Response delDocument(@PathParam("docPath") String docPath, @QueryParam("language") String language, @QueryParam("reseller") String reseller, @Context UriInfo ui) {
 		Session session = null;
 		Repository repository = null;
 		boolean error = false;
 		InputStream input = null;
 		OutputStream output = null;
 		String result = null;
 		StringTokenizer stringTokenizer = null;
 		boolean notEmpty = false;
 		boolean notFound = false;
 		// StringBuffer bufferPath = new StringBuffer("");
 		String errorString = "error";
 		System.out.println("POZVANA METODA delDocument");
 		try {
 //			System.out.println("field par uri " + fieldPars);
 
 //			if (fieldPars == null) {
 //				error = true;
 //				fieldPars = "";
 //			}
 
 //			/
 
 			InitialContext initialContext = new InitialContext();
 			repository = (Repository) initialContext.lookup("java:jcr/local");
 			session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
 //			if (!fieldPars.equals("")) {
 //				stringTokenizer = new StringTokenizer(fieldPars, "?&=");
 //				System.out.println("FIELD PARAMS " + fieldPars);
 //				while (stringTokenizer.hasMoreTokens()) {
 //					String first = stringTokenizer.nextToken();
 //					String second = stringTokenizer.nextToken();
 //					if ("reseller".equalsIgnoreCase(first)) {
 //						reseller = second;
 //						continue;
 //					}
 //					if ("language".equalsIgnoreCase(first)) {
 //						language = second;
 //						continue;
 //					}
 //				}
 //			}
 
 			if (language == null && reseller == null) {
 				language = "";
 				reseller = "";
 			}
 
 			if (!docPath.startsWith("/")) {
 				docPath = "/" + docPath;
 			}
 
 			// bufferPath = new StringBuffer("");
 			// StringTokenizer st = new StringTokenizer(docPath,">");
 			//
 			// while(st.hasMoreTokens()){
 			// bufferPath.append("/"+st.nextToken());
 			// }
 
 			// path = "/help/" + app + "/" + topic;
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
 					}else{
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
 
 		}catch (PathNotFoundException ex) {
 			notFound = true;
 			errorString = "Not found!";
 		}
 		catch (Exception ex) {
 			error = true;
 			ex.printStackTrace();
 		} finally {
 			if (session != null)
 				session.logout();
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
 
 //	@Override
 //	public Response delDocument(@PathParam("docPath") String docPath) {
 //		return delDocument(docPath, "");
 //	}
 //	
 
 	@Override
 	public Response getJSON(@PathParam("docPath") String docPath, @QueryParam("language") String language,
 			@QueryParam("reseller") String reseller, @Context UriInfo ui) {
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
 			
 			
 			
 			// Map<String, List<String>> callParameters =
 			// ui.getQueryParameters();
 			//
 			//
 			//
 			// for (Map.Entry<String, List<String>> entry :
 			// callParameters.entrySet()) {
 			// if (entry.getKey().equals("language")) {
 			// language = entry.getValue().get(0);
 			// continue;
 			// }
 			// if (entry.getKey().equals("reseller")) {
 			// reseller = entry.getValue().get(0);
 			// continue;
 			// }
 			//
 			// // callParameters.put(entry.getKey(), entry.getValue().get(0);
 			// fieldPars += entry.getKey() + "=" + entry.getValue().get(0) +
 			// "&";
 			// }
 
 			if (fieldPars.endsWith("&")) {
 				// fieldPars = fieldPars.substring(0, fieldPars.length() - 1);
 			}
 
 			if (fieldPars.startsWith("&")) {
 				// fieldPars = "&"+ fieldPars;
 			}
 
 			System.out.println("get jason " + docPath + " fp " + fieldPars);
 
 			if (language == null) {
 				language = "en";
 				// fieldPars += "&language=en";
 			}
 			if (reseller == null) {
 				reseller = "";
 				// fieldPars += "&reseller=1";
 			}
 //
 //			if (language != null) {
 //				langResel += "language=" + language;
 //			}
 //			if (reseller != null) {
 //				langResel += (language == null ? "reseller=" + reseller : "&reseller=" + reseller);
 //			}
 
 			openSession();
 			// result = (String) getDocument(docPath, "language=" + language +
 			// "&reseller=" + reseller+fieldPars, ui).getEntity();
 			result = (String) getDocument(docPathShort, fieldPars, ui).getEntity();
 
 			System.out.println("get jason novi " + result);
 
 			if (result == null)
 				result = "";
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
 			// DocumentCvor dc = new DocumentCvor(node.getName(),
 			// niz[1].toUpperCase(), ui.getBaseUri().toString()
 			// + "/documents" + node.getPath(), ui.getBaseUri().toString() +
 			// "/documents"
 			// + node.getParent().getPath());
 
 			// DocumentCvor dc = new DocumentCvor();
 System.out.println("LANGUAGE >>> " + language + " RESELLER >>> " + reseller);
 			response = RestHelpRepoService.getJsonMapper().defaultPrettyPrintingWriter()
 					.writeValueAsString(getDocumentCvor("/" + docPathShort, paragraphs, "/" + fieldPars, ui,language,reseller));
 //<<<<<<< Upstream, based on branch 'master' of https://github.com/dimce011/campus201305.git
 //					.writeValueAsString(getDocumentCvor("/" + docPath, paragraphs, langResel, ui, language, reseller));
 //=======
 //					.writeValueAsString(getDocumentCvor("/" + docPathShort, paragraphs, "/" + fieldPars, ui));
 //>>>>>>> b67f029 Rest API v7 DocumentCvorWrapper renamed RestHelpRepoService changed a bit about DocumentCvorWrapper
 
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
 		}
 		return Response.status(Response.Status.OK).entity(response).build();
 	}
 
 	Session session = null;
 	Repository repository = null;
 	InitialContext initialContext;
 
 	public void openSession() {
 		try {
 			initialContext = new InitialContext();
 			repository = (Repository) initialContext.lookup("java:jcr/local");
 			session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
 			logger.info("SESSION OPENED");
 		} catch (NamingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (LoginException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RepositoryException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void closeSession() {
 		session.logout();
 	}
 
 	public DocumentCvor getDocumentCvor(String parent, Map<String, String> paragraphs, String fieldPars, UriInfo ui,
 			String language, String reseller) {
 		openSession();
 		logger.info("PARENT: {}", parent);
 		Node node = null;
 		DocumentCvor dnl = null;
 		try {
 			node = session.getNode(parent);
 			String[] niz = node.getPath().split("/");
 			String baseUri = ui.getBaseUri().toString().substring(0, ui.getBaseUri().toString().length() - 1);
 //<<<<<<< Upstream, based on branch 'master' of https://github.com/dimce011/campus201305.git
 
 			// ubacivanje title-a
 			String title = null;
 
 			if (reseller==null) {
 				reseller = "centili";
 			}
 			if (language==null) {
 				language = "en";
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
 		} finally {
 			if (session != null)
 				closeSession();
 		}
 		return dnl;
 
 	}
 
 	public boolean hasFolder(Node node) throws RepositoryException {
 		openSession();
 		for (NodeIterator nodeIterator = node.getNodes(); nodeIterator.hasNext();) {
 			Node subNode = nodeIterator.nextNode();
 			if (subNode.getPrimaryNodeType().getName().equals("nt:folder")) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean hasFiles(Node node) throws RepositoryException {
 		openSession();
 		for (NodeIterator nodeIterator = node.getNodes(); nodeIterator.hasNext();) {
 			Node subNode = nodeIterator.nextNode();
 			if (subNode.getPrimaryNodeType().getName().equals("nt:file")) {
 				return true;
 			}
 		}
 		return false;
 
 	}
 
 }
