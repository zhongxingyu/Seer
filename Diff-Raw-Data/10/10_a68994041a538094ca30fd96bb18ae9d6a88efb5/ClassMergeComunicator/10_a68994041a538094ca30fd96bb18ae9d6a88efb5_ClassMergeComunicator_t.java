 package controller;
 
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import repository.DiagramDAO;
 
 import controller.comparer.xmi.XmiClassDiagramComparer;
 import controller.upload.FileInfo;
 import domain.Diagram;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 /**
  * @author Yanwu shen
  */
 
 public class ClassMergeComunicator extends HttpServlet {
 	 /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	/**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request
      * 			servlet request
      * @param response
      * 			servlet response
      * @throws ServletException
      * 			If a servlet-specific error occurs
      * @throws IOException
      * 			If an I/O error occurs
      */
 	private final static String REQUEST_REFRESH = "Refresh";
 	private final static String REQUEST_CONSOLIDATE = "Consolidate";
 	private final static String REQUEST_ADD = "Add";
 	private final static String REQUEST_BREAK = "Break";
 	private final static String REQUEST_COMPARE = "Compare";
 		
 	private XmiClassDiagramComparer comparer;
 	
 	public ClassMergeComunicator() {
 		super();
 	}
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response)
 		    throws ServletException, IOException {
 		doPost(request, response);
 	}
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)
 			    throws ServletException, IOException {
 		
		// Get diagram IDs from checked boxes
		//String[] checked = (String[]) request.getParameterValues("check");
		//int diagramId1 = Integer.parseInt(checked[0]);
		//int diagramId2 = Integer.parseInt(checked[1]);
 		
 		JSONObject obj,reqobj;
 		RequestDispatcher dispatcher;
 		
 		String jsonString= request.getParameter("request");
 		// test
		//System.out.print("JSON request:"+jsonString);
 		// test
 		reqobj = (JSONObject) JSONValue.parse(jsonString);
		
 		
 		
 		
 		switch (jsonString) 
 		{
 			case REQUEST_COMPARE:
 				dispatcher = request.getRequestDispatcher("/WEB-INF/JSP/mergeClass.jsp");
 				dispatcher.forward(request, response);
 				break;
 			case REQUEST_REFRESH:
 				int cd1ID = Integer.parseInt((String) reqobj.get("Diagram1"));
 				int cd2ID = Integer.parseInt((String) reqobj.get("Diagram2"));
 				Diagram cd1 = DiagramDAO.getDiagram(cd1ID);
 				Diagram cd2 = DiagramDAO.getDiagram(cd2ID);
 				List<FileInfo> lfi1 = new ArrayList<FileInfo>();
 				List<FileInfo> lfi2 = new ArrayList<FileInfo>();
 				FileInfo fi1_not = new FileInfo(cd1.getNotationFilePath(), cd1.getNotationFileName(), "");
 				FileInfo fi1_uml = new FileInfo(cd1.getFilePath(), cd1.getDiagramName(), "");
 				FileInfo fi2_not = new FileInfo(cd2.getNotationFilePath(), cd2.getNotationFileName(), "");
 				FileInfo fi2_uml = new FileInfo(cd2.getFilePath(), cd2.getDiagramName(), "");
 				lfi1.add(fi1_not); lfi1.add(fi1_uml);
 				lfi2.add(fi2_not); lfi2.add(fi2_uml);
 				
 				comparer = new XmiClassDiagramComparer(lfi1, lfi2);
 				
 				obj=comparer.action(reqobj);
 				request.setAttribute("response", obj);
 				
 				dispatcher = request.getRequestDispatcher("/WEB-INF/JSP/selectClass.jsp");
 				dispatcher.forward(request, response);
 				break;		
 			case REQUEST_CONSOLIDATE:
 				dispatcher = request.getRequestDispatcher("/WEB-INF/JSP/selectClass.jsp");
 				dispatcher.forward(request, response);
 				break;	
 			case REQUEST_ADD:
 				dispatcher = request.getRequestDispatcher("/WEB-INF/JSP/refineClass.jsp");
 				dispatcher.forward(request, response);
 				break;
 			case REQUEST_BREAK:
 				dispatcher = request.getRequestDispatcher("/WEB-INF/JSP/selectClass.jsp");
 				dispatcher.forward(request, response);
 				break;
 			}
 		}
 	
 		
 	}
 
