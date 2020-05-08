 package org.infobip.mpayments.help.repo.rest;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 
 import javax.ejb.Stateless;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.Repository;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.SimpleCredentials;
 import javax.jcr.Workspace;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 import javax.naming.InitialContext;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Response;
 
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
 	public Response getParagraph(@PathParam("docPath") String docPath, @PathParam("parID") String parID) {
 		return getParagraph(docPath, parID, "");
 	}
 
 	@Override
 	public Response getParagraph(@PathParam("docPath") String docPath, @PathParam("parID") String parID,
 			@PathParam("fieldPars") String fieldPars) {
 
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
 				stringTokenizer = new StringTokenizer(fieldPars, "?&=");
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
 				reseller = "1";
 			}
 
 			System.out.println("ispis mape");
 			for (Map.Entry<String, Object> entry : mapParameters.entrySet()) {
 				System.out.println(entry.getKey() + " = " + entry.getValue());
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
 			while (it.hasNext()) {
 				node = it.nextNode();
 			}
 
 			System.out.println("path " + node.getPath());
 			Node content = node.getNode("jcr:content");
 			input = content.getProperty("jcr:data").getBinary().getStream();
 			result = RestAPIService.getStringFromInputStream(input).toString();
 
 			if (!mapParameters.isEmpty()) {
 				FreeMarker fm = new FreeMarker();
 				result = fm.process(mapParameters, result);
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
 	public Response getDocuments(@PathParam("id") String id) {
 		Session session = null;
 		Repository repository = null;
 		boolean error = false;
 		InputStream input = null;
 		OutputStream output = null;
 		String result = null;
 		System.out.println("POZVANA METODA getDocuments");
 		try {
 			InitialContext initialContext = new InitialContext();
 			repository = (Repository) initialContext.lookup("java:jcr/local");
 			session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
 
 			Node en = session.getNode("/help/pp/service/1/en");
 			Node target = session.getNodeByIdentifier(en.getIdentifier());
 
 			printChildren(session.getNode("/"));
 			Node content = target.getNode("jcr:content");
 			input = content.getProperty("jcr:data").getBinary().getStream();
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
 
 	private static StringBuilder getStringFromInputStream(InputStream is) {
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
 	public Response getDocument(@PathParam("docPath") String docPath) {
 		return getDocument(docPath, "");
 	}
 
 	@Override
 	public Response getDocument(@PathParam("docPath") String docPath, @PathParam("fieldPars") String fieldPars) {
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
 		System.out.println("POZVANA METODA getDocument");
 		try {
 			System.out.println("field par uri " + fieldPars);
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
 				stringTokenizer = new StringTokenizer(fieldPars, "?&=");
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
 				reseller = "1";
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
 
 			Node node = null;
 			if (it.hasNext()) {
 				node = it.nextNode();
 				// System.out.println("path " + node.getPath());
 				Node content = node.getNode("jcr:content");
 				input = content.getProperty("jcr:data").getBinary().getStream();
 				result = RestAPIService.getStringFromInputStream(input).toString();
 
 				if (!mapParameters.isEmpty()) {
 					FreeMarker fm = new FreeMarker();
 					result = fm.process(mapParameters, result);
 				}
 			} else {
 				error = true;
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
 	public Response delDocument(@PathParam("docPath") String docPath, @PathParam("fieldPars") String fieldPars) {
 		Session session = null;
 		Repository repository = null;
 		boolean error = false;
 		InputStream input = null;
 		OutputStream output = null;
 		String result = null;
 		StringTokenizer stringTokenizer = null;
 		String reseller = null;
 		String language = null;
 		boolean notFound = false;
 		// StringBuffer bufferPath = new StringBuffer("");
 		String errorString = "error";
 		System.out.println("POZVANA METODA delDocument");
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
 				stringTokenizer = new StringTokenizer(fieldPars, "?&=");
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
 				}
 			}
 
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
 					}
 				} else {
 					notFound = true;
 					errorString = "Not found!";
 				}
 			}
 
 			session.save();
 
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
 
 		if (notFound) {
 			return Response.status(Response.Status.NOT_FOUND).entity(errorString).build();
 		}
 		if (!error) {
 			return Response.status(Response.Status.OK).entity("Deleted node with path: " + docPath + " .").build();
 		} else {
 			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorString).build();
 		}
 
 	}
 
 	@Override
 	public Response delDocument(@PathParam("docPath") String docPath) {
 		return delDocument(docPath, "");
 	}
 
 }
