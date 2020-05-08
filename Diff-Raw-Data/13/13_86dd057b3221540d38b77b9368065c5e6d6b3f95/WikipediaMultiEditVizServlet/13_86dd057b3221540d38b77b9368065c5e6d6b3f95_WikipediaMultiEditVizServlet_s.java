 package edu.mit.cci.wikipedia.vizservlet;
 
 import edu.mit.cci.wikipedia.collector.GetRevisions;
 import edu.mit.cci.wikipedia.util.NetworkGenerator;
 import edu.mit.cci.wikipedia.util.Processing;
 import edu.mit.cci.wikipedia.vizservlet.PMF;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.servlet.ServletException;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 public class WikipediaMultiEditVizServlet extends HttpServlet {
 
 	private static final Logger log = Logger.getLogger(WikipediaMultiEditVizServlet.class.getName());
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 	throws IOException, ServletException {
 		try {
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			log.info("Start doGet");
 			ServletContext context = this.getServletContext();
 			String responseStr = "";
 
 			//文字コードとMIMEタイプを指定する
 			response.setContentType("text/html;charset=utf-8");
 			//log.info("LOG@HelloServlet:: characterEncoding " + response.getCharacterEncoding());
 			PrintWriter out = response.getWriter();
 			String eol = "\n";
 
 			responseStr += "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" + eol;
 			responseStr += "<html>" + eol;
 			responseStr += "<head>" + eol;
 			responseStr += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" + eol;
 			responseStr += "<title>Get TOP10 Contributers</title>" + eol;
 			responseStr += "<meta http-equiv=\"Content-Style-Type\" content=\"text/css\">" + eol;
 			responseStr += "<meta http-equiv=\"Content-Script-Type\" content=\"text/javascript\">" + eol;
 			responseStr += "<link href=\"css/flexgrid.css\" rel=\"stylesheet\" type=\"text/css\">" + eol;
 			responseStr += "<script type=\"text/javascript\" src=\"js/jquery-1.2.1.js\"></script>" + eol;
 			responseStr += "<script type=\"text/javascript\" src=\"js/treemap.js\"></script>" + eol;
 			responseStr += "<script type=\"text/javascript\" src=\"js/processing.js\"></script>" + eol;
 			responseStr += "<script type=\"text/javascript\" src=\"js/init.js\"></script>" + eol;
 			responseStr += "<script type=\"text/javascript\">" + eol;
 
 			String tableContents = "";
 			String code = "";
 			
 			request.setCharacterEncoding("UTF-8");
 			// Get arguments
 			String pageTitleStr = request.getParameter("name"); // WikiPedia article title
 			String[] pageTitles;
 			if (pageTitleStr == null){
 				pageTitles = null;
 			}
			else if (pageTitleStr.indexOf("\\|") > 0)
				pageTitles = pageTitleStr.split("\\\\|");
 			else {
 				pageTitles = new String[1];
 				pageTitles[0] = pageTitleStr;
 			}
 			boolean CACHE = true;
 			if (request.getParameter("cache") != null) {
 				if (request.getParameter("cache").equals("true"))
 					CACHE = true;
 				else
 					CACHE = false;
 			}
 			// # of edits (limit x 500 edits)
 			String REV_LIMIT = "1"; // default: last 500 edits
 			if (request.getParameter("limit") != null) {
 				REV_LIMIT = request.getParameter("limit");
 			}
 			// # of nodes 
 			String NODE_LIMIT = "10"; // default: 10 nodes
 			if (request.getParameter("nodelimit") != null) {
 				NODE_LIMIT = request.getParameter("nodelimit");
 			}
 			// Canvas size
 			String CANVAS_SIZE = "300";
 			if (request.getParameter("size") != null) {
 				CANVAS_SIZE = request.getParameter("size");
 			}
 			// Language
 			String LANG = "en";
 			if (request.getParameter("lang") != null) {
 				LANG = request.getParameter("lang");
 			}
 			
 			GetRevisions gr = new GetRevisions();
 			 // User name
 			Set<String> dataSet = new HashSet<String>();
 			
 			if (pageTitleStr == null) {
 				//出力ストリームを取得する
 				out = response.getWriter();
 				out.write("Plsease set page_name (name) splited by '|'.");
 				out.close();
 			} else {
 			for (String pageTitle:pageTitles) {
 				if (pageTitle != null) {
 					log.info("name " + pageTitle);
 					
 					String data = "";
 					// Searching cache
 					PersistenceManager pm = PMF.get().getPersistenceManager();
 					
 					String query = "select from " + ArticleCache.class.getName() + " where pageTitle==\'" + pageTitle.replaceAll("\'", "\\\\\'") + "\'";
 					List<ArticleCache> articleCaches = (List<ArticleCache>)pm.newQuery(query).execute();
 					
 					// No cache or Don't use cache
 					if (articleCaches.isEmpty() || (!articleCaches.isEmpty() && !CACHE)) {
 						if (!articleCaches.isEmpty()) {
 							// Clear cached data
 							for(ArticleCache ac:articleCaches) {
 								pm.deletePersistent(ac);
 							}
 						}
 						// Get # of edits on the pageTitle
 						String download = gr.getArticleRevisions(LANG, pageTitle, REV_LIMIT);
 						String[] lines = download.split("\n");
 						for (String line : lines) {
 							log.info(line);
 							String[] arr = line.split("\t");
 							//arr[0] pageTitle, arr[1] userName, arr[2] timestamp, arr[3] minor, arr[4] size
 							String timestamp = arr[2];
 							timestamp = timestamp.replaceAll("T", " ");
 							timestamp = timestamp.replaceAll("Z", "");
 							data += arr[0] + "\t" + arr[1] + "\t" + timestamp + "\t0\t" + arr[4] + "\n";
 							
 							// Storing data to cache
 							ArticleCache articleCache = new ArticleCache(arr[0],arr[1],df.parse(timestamp),Integer.parseInt(arr[4]));
 							PersistenceManager pmWriter = PMF.get().getPersistenceManager();
 							try {
 								pmWriter.makePersistent(articleCache);
 							} finally {
 								pmWriter.close();
 							}
 						}
 					}
 					// Already cached and using cache
 					else if (!articleCaches.isEmpty() && CACHE){
 						log.info("Cashed");
 						for(ArticleCache ac:articleCaches) {
 							data += ac.getPageTitle() + "\t" + ac.getAuthor() + "\t" + df.format(ac.getDate()) + "\t0\t" + String.valueOf(ac.getSize()) + "\n";
 						}
 					}
 					pm.close();
 					dataSet.add(data);
 				}
 			}
 			// Process dataSet<data>
 			List<String> nodeList = new ArrayList<String>();
 			// nodeMap key:node name, value: list of articles
 			Map<String,Set<String>> nodeMap = new HashMap<String,Set<String>>();
 			// nodeEditMap key:node name, value: sum # of edits
 			Map<String,Integer> nodeEditMap = new HashMap<String,Integer>();
 			for (String data:dataSet) {
 				String[] lines = data.split("\n");
 				for (String line:lines) {
 					//log.info(line);
 					String pageTitle = line.split("\t")[0];
 					String editorName = line.split("\t")[1];
 					
 					if (nodeMap.containsKey(editorName)) {
 						Set<String> set = nodeMap.get(editorName);
 						if (!set.contains(pageTitle)) {
 							set.add(pageTitle);
 							nodeMap.put(editorName, set);
 						}
 						int v = nodeEditMap.get(editorName);
 						v++;
 						nodeEditMap.put(editorName, v);
 					} else {
 						nodeList.add(editorName);
 						Set<String> set = new HashSet<String>();
 						set.add(pageTitle);
 						nodeMap.put(editorName, set);
 						nodeEditMap.put(editorName, 1);
 					}
 				}
 			}
 			
 			
 			// Get absolute path to skeleton file
 			String pathToSkelton = context.getRealPath("/skelton/skelton_spring.js");
 
 			String nodes = "";
 			
 			for (String editorName:nodeMap.keySet()) {
 				int numOfArticles = nodeMap.get(editorName).size();
 				int numOfEdits = nodeEditMap.get(editorName);
 				nodes += editorName + "\t" + numOfEdits + "\t" + numOfArticles + "\n";
 			}
 			
 			log.info("\nNode_name\tTotal edits\tTotal articles\n" + nodes);
 			// Get edge data
 			// From editor \t To editor \t weight
 			//log.info(data);
 			
 			int[][] matrix = new int[nodeList.size()][nodeList.size()];
 			
 			for (String data:dataSet) {
 				String[] edges = new NetworkGenerator().getSeqColabNetworkFromRevisions(data).split("\n"); // Get network data from revision history data
 				for (String edge:edges) {
 					String name1 = edge.split("\t")[0];
 					String name2 = edge.split("\t")[1];
 					int tie = Integer.parseInt(edge.split("\t")[2]);
 					matrix[nodeList.indexOf(name1)][nodeList.indexOf(name2)] += tie;
 				}
 			}
 			String edges = "";
 			for (int i = 0; i < matrix.length; i++) {
 				for (int j = i+1; j < matrix.length; j++) {
 					int tie = matrix[i][j] + matrix[j][i]; // undirect
 					if (tie > 0)
 						edges += nodeList.get(i) + "\t" + nodeList.get(j) + "\t" + tie + "\n";
 				}
 			}
 			log.info(edges);
 			if (edges.length() > 0) {
 				//log.info(nodes);
 				//log.info(edges);
 				code = new Processing().processingCode(nodes, edges, pathToSkelton, CANVAS_SIZE);
 				//log.info("Processing code\n" + code);
 			}
 
 			//出力ストリームを取得する
 			out = response.getWriter();
 
 			// processingの起動
 			responseStr += "window.onload=function(){" + eol;
 			
 			responseStr += "var canvas = document.getElementsByTagName(\'canvas\')[0];" + eol;
 			// Processingのソースコードが書かれた要素を参照
 			responseStr += "var codeElement = document.getElementById(\'processing-code\');" + eol;
 			responseStr += "var code = codeElement.textContent || codeElement.innerText;" + eol;
 			responseStr += "Processing(canvas,code);" + eol;
 			
 			responseStr += "};" + eol;
 			responseStr += "</script>" + eol;
 
 			responseStr += "<script id=\"processing-code\" type=\"application/processing\">" + eol;
 			// ここにProcessingのコードを挿入
 			responseStr += code;
 			responseStr += "</script>" + eol;
 
 			responseStr += "</head>" + eol;
 			responseStr += "<body>" + eol;
 
 			responseStr += "<div><canvas width=\"" + CANVAS_SIZE + "\" height=\"" + CANVAS_SIZE + "\"></canvas></div>" + eol;
 
 			responseStr += tableContents;
 
 			responseStr += "</body>" + eol;
 			responseStr += "</html>" + eol;
 			
 			out.println(responseStr);
 			out.close();
 			}
 		}
 		catch(Exception e){
 			e.printStackTrace();
 			throw new ServletException(e);
 		}
 	}
 }
