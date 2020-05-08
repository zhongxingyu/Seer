 package univ_angers.virtuose.webapp;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
 import javax.servlet.*;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.*;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.document.Document;
 
 import univ_angers.virtuose.rendu.Extract;
 import univ_angers.virtuose.search.Search;
 import univ_angers.virtuose.utils.Writer;
 import univ_angers.virtuose.utils.XmlToHtml;
 
 @WebServlet("/controller")
 @MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
 public class Controller extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static Logger log = Logger.getLogger(Controller.class);
 	private static String saved_map_folder = "src/ressources/saved_map/";
 	
 	public Controller() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		RequestDispatcher disp = null;
 		HttpSession session = request.getSession();
 
 		String action = request.getParameter("action");
 		String keywords = "";
 
 		if (action == null) {
 
 		}else if(action.equals("show")) {
 			int id = Integer.parseInt((String) request.getParameter("id"));
 			Map<String,String> listMap = (Map<String, String>) session.getAttribute("listMap");
 			int i = 0;
 			for (Map.Entry<String, String> entry : listMap.entrySet()) {
 			    String key = entry.getKey();
 			    String value = entry.getValue();
 			    log.debug("show" + value);
 			    if(i == id) {
 			    	session.setAttribute("map", value);
 			    }
 			    i++;
 			}
 			disp = request.getRequestDispatcher("show.jsp");
 			disp.forward(request, response);
 		
 		} else if (action.equals("search")) {
 			// get access to file that is uploaded from client
 			Part p1 = request.getPart("map");
 			InputStream is = p1.getInputStream();
 
 			// read filename which is sent as a part
 			Part p2 = request.getPart("keywords");
 			Scanner s = new Scanner(p2.getInputStream());
 			keywords = s.nextLine(); // read filename from stream

 			// get filename to use on the server
 			String fileName = getFileName(p1);
 		    log.info("File name : " + fileName);
 			String outputfile = saved_map_folder+fileName; // get path on the server
 			createDirectoryIfNeeded(saved_map_folder);
 			
 			if(mapNameExists(outputfile)) {
 				session.setAttribute("mapExists", "La carte existe déjà dans la base.");
 			} else {
 				FileOutputStream os = new FileOutputStream(outputfile);
 
 				// write bytes taken from uploaded file to target file
 				int ch = is.read();
 				while (ch != -1) {
 					os.write(ch);
 					ch = is.read();
 				}
 				
 				// on traite le .mm
 				Search.index(outputfile);
 				Writer.proceed(outputfile);
 				os.close();
 			}
 			s.close();
 
 			
 			// 2. query
 			String querystr = keywords;
 			ArrayList<Document> docs = Search.search(querystr);
 			ArrayList<String> xmls = new ArrayList<String>();
 			/**
 			 * Generate all xml documents according to the lucene doc that match
 			 * the request
 			 */
 			// Document d = docs.get(0);
 			for (Document d : docs) {
 				String tmp = "";
 				try {
 					tmp = Extract.extract(d.get("document"), d.get("id"));
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				xmls.add(tmp);
 			}
 
 			Map<String, String> listMap = new HashMap();
 			
 			for (String xml : xmls) {
 				String result = xml;
 
 				log.debug("Controller:result: " + result);
 				XmlToHtml.start(result);
 				String title = XmlToHtml.title;
 				String map = XmlToHtml.result;
 				//log.debug("title: "+ title);
 				//log.debug("map: "+ map);
 				listMap.put(title, map);
 			}
             
 			session.setAttribute("listMap", listMap);
 			session.setAttribute("keywords", keywords);
 			disp = request.getRequestDispatcher("result.jsp");
 			disp.forward(request, response);
 		}
 	}
 	
 	private String getFileName(Part part) {
 	    String partHeader = part.getHeader("content-disposition");
 	    //log.info("Part Header = " + partHeader);
 	    for (String cd : part.getHeader("content-disposition").split(";")) {
 	      if (cd.trim().startsWith("filename")) {
 	        return cd.substring(cd.indexOf('=') + 1).trim()
 	            .replace("\"", "");
 	      }
 	    }
 	    return null;
 
 	  }
 	
 	private void createDirectoryIfNeeded(String directoryName)
 	{
 	  File theDir = new File(directoryName);
 
 	  // if the directory does not exist, create it
 	  if (!theDir.exists())
 	  {
 	    log.info("creating directory: " + directoryName);
 	    theDir.mkdir();
 	  }
 	}
 	
 	private boolean mapNameExists(String filePath) {
 		File file = new File(filePath);
 		return file.exists();
 	}
 
 }
