 package controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import repository.DiagramDAO;
 import controller.comparer.xmi.XmiClassDiagramComparer;
 import controller.upload.FileInfo;
 import domain.Diagram;
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
 	// Requests
 	private final static String REQUEST_REFRESH = "Refresh";
 	private final static String REQUEST_CONSOLIDATE = "Consolidate";
 	private final static String REQUEST_ADD = "Add";
 	private final static String REQUEST_BREAK = "Break";
 	private final static String REQUEST_COMPARE = "Compare";
 	private final static String REQUEST_NEXT = "Next";
 	
 	// Comparer object
 	private final static String COMPARE_OBJECT = "CompareObject";
 	private XmiClassDiagramComparer comparer;
 	
 	// Diagrams
 	private final static String DIAGRAM1_IMAGE = "DiagramPath1";
 	private final static String DIAGRAM2_IMAGE = "DiagramPath2";
 	
 	private final static int INACTIVE_INTERVAL = 15 * 60; // in seconds (15 minutes)
 	
 	public ClassMergeComunicator() {
 		super();
 	}
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response)
 		    throws ServletException, IOException {
 		doPost(request, response);
 	}
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)
 			    throws ServletException, IOException {
 		
 		// Get session to retrieve comparer object
 		HttpSession session = request.getSession(true);
 		
 		JSONObject obj,reqobj;
 		RequestDispatcher dispatcher;
 		
 		String reqString = request.getParameter("request");
 		reqobj = (JSONObject) JSONValue.parse(reqString);
 		String reqType = (String) reqobj.get("Request");
 		
 		// Print to console
 		System.out.println(reqType + " = " + reqobj.toJSONString());
 		
 		// retrieve comparer
 		comparer = (XmiClassDiagramComparer) session.getAttribute(COMPARE_OBJECT);
 		
 		// Create our comparer if it doesn't exist yet
 		if (comparer == null) {
 			// Initialize comparer using diagram IDs
 			int cd1ID = Integer.parseInt((String) reqobj.get("Diagram1"));
 			int cd2ID = Integer.parseInt((String) reqobj.get("Diagram2"));
 			Diagram cd1 = DiagramDAO.getDiagram(cd1ID);
 			Diagram cd2 = DiagramDAO.getDiagram(cd2ID);
 
 			// get root webapp path
 			String contextPath = request.getSession().getServletContext().getRealPath("/");
 
 			// to get the .uml file names:
 			String cd1UmlFileName = cd1.getFilePath().substring(
 					cd1.getFilePath().lastIndexOf("/") + 1,
 					cd1.getFilePath().length());
 			String cd2UmlFileName = cd2.getFilePath().substring(
 					cd2.getFilePath().lastIndexOf("/") + 1,
 					cd2.getFilePath().length());
 			
 			// to get the UML path without the file names:
 			String cd1UmlPath = cd1.getFilePath().substring(0,
 					cd1.getFilePath().lastIndexOf("/") + 1);
 			String cd2UmlPath = cd2.getFilePath().substring(0,
 					cd2.getFilePath().lastIndexOf("/") + 1);
 			
 			List<FileInfo> lfi1 = new ArrayList<FileInfo>();
 			List<FileInfo> lfi2 = new ArrayList<FileInfo>();
 			
 			String notationFilePath1 = cd1.getNotationFilePath().replaceAll("\\\\", "/");
 			String notationFilePath2 = cd2.getNotationFilePath().replaceAll("\\\\", "/");
 		
 			
			FileInfo fi1_not = new FileInfo(contextPath, cd1.getNotationFileName(), "");
 			FileInfo fi1_uml = new FileInfo(contextPath + cd1UmlPath, cd1UmlFileName, "");
 			FileInfo fi2_not = new FileInfo(contextPath + cd2.getNotationFilePath(), cd2.getNotationFileName(), "");
 			FileInfo fi2_uml = new FileInfo(contextPath + cd2UmlPath, cd2UmlFileName, "");
 			lfi1.add(fi1_not); lfi1.add(fi1_uml);
 			lfi2.add(fi2_not); lfi2.add(fi2_uml);
 			
 			// instantiate and store the comparer in the session
 			comparer = new XmiClassDiagramComparer(lfi1, lfi2);
 			System.out.println("contextPath " + contextPath);
 			System.out.println("FilePath " + cd1.getNotationFilePath().replaceAll("\\\\", "/"));
 			System.out.println("FileName " + cd1.getDiagramName().replaceAll("\\\\", "/"));
 			session.setAttribute(COMPARE_OBJECT, comparer);
 			session.setMaxInactiveInterval(INACTIVE_INTERVAL);	// 10 minutes
 			
 			String diagramImage1 = cd1.getDiagramName().replaceAll("\\\\", "/");
 			String diagramImage2 = cd2.getDiagramName().replaceAll("\\\\", "/");
 			session.setAttribute(DIAGRAM1_IMAGE, notationFilePath1 + diagramImage1);
 			session.setAttribute(DIAGRAM2_IMAGE, notationFilePath2 + diagramImage2);
 		}
 		
 		// Get diagram IDs from checked boxes
 		//String[] checked = (String[]) request.getParameterValues("check");
 		//int diagramId1 = Integer.parseInt(checked[0]);
 		//int diagramId2 = Integer.parseInt(checked[1]);
 
 		switch (reqType) 
 		{
 			case REQUEST_COMPARE:
 				obj=comparer.action(reqobj);
 				request.setAttribute("response", obj);
 				System.out.println("compare response: " + obj.toString());
 				dispatcher = request.getRequestDispatcher("/WEB-INF/JSP/mergeClass.jsp");
 				dispatcher.forward(request, response);
 				break;
 			case REQUEST_REFRESH:
 				//obj=comparer.action(reqobj); // action() doesn't work
 				obj=comparer.action(reqobj);
 				System.out.println("refresh response: " + obj.toString());
 				request.setAttribute("response", obj);
 				
 				dispatcher = request.getRequestDispatcher("/WEB-INF/JSP/selectClass.jsp");
 				dispatcher.forward(request, response);
 				break;		
 			case REQUEST_CONSOLIDATE:
 				obj=comparer.action(reqobj);
 				System.out.println("consolidate response: " + obj.toString());
 				request.setAttribute("response", obj);
 				dispatcher = request.getRequestDispatcher("/WEB-INF/JSP/selectClass.jsp");
 				dispatcher.forward(request, response);
 				/*
 				// Now do a Refresh
 				String refreshString= "{\"Request\":\"Refresh\"}";
 				reqobj = (JSONObject) JSONValue.parse(refreshString);
 				obj = comparer.action(reqobj);
 				request.setAttribute("response", obj);
 				dispatcher = request.getRequestDispatcher("/WEB-INF/JSP/selectClass.jsp");
 				dispatcher.forward(request, response);
 				*/
 				break;	
 			case REQUEST_ADD:
 				obj=comparer.action(reqobj);
 				System.out.println("add response: " + obj.toString());
 				request.setAttribute("response", obj);
 				dispatcher = request.getRequestDispatcher("/WEB-INF/JSP/refineClass.jsp");
 				dispatcher.forward(request, response);
 				break;
 			case REQUEST_NEXT:
 				obj=comparer.action(reqobj);
 				System.out.println("next response: " + obj.toString());
 				request.setAttribute("response", obj);
 				dispatcher = request.getRequestDispatcher("/WEB-INF/JSP/relationClass.jsp");
 				dispatcher.forward(request, response);
 				break;
 			case REQUEST_BREAK:
 				obj=comparer.action(reqobj);
 				System.out.println("break response: " + obj.toString());
 				request.setAttribute("response", obj);
 				dispatcher = request.getRequestDispatcher("/WEB-INF/JSP/selectClass.jsp");
 				dispatcher.forward(request, response);
 				break;
 			}
 		}
 	
 		
 	public static void main(String[] args) {
 		
 		JSONObject obj,reqobj;
 		
 		// test Refresh
 		int cd1ID = 64;
 		int cd2ID = 65;
 		String reqString= "{\"Request\":\"Refresh\"}";
 		reqobj = (JSONObject) JSONValue.parse(reqString);
 		
 		Diagram cd1 = DiagramDAO.getDiagram(cd1ID);
 		Diagram cd2 = DiagramDAO.getDiagram(cd2ID);
 		
 		// to get the .uml file names:
 		String cd1UmlFileName = cd1.getFilePath().substring(
 				cd1.getFilePath().lastIndexOf("/") + 1,
 				cd1.getFilePath().length());
 		String cd2UmlFileName = cd2.getFilePath().substring(
 				cd2.getFilePath().lastIndexOf("/") + 1,
 				cd2.getFilePath().length());
 		
 		// to get the UML path without the file names:
 		String cd1UmlPath = cd1.getFilePath().substring(0,
 				cd1.getFilePath().lastIndexOf("/") + 1);
 		String cd2UmlPath = cd2.getFilePath().substring(0,
 				cd2.getFilePath().lastIndexOf("/") + 1);
 		
 		List<FileInfo> lfi1 = new ArrayList<FileInfo>();
 		List<FileInfo> lfi2 = new ArrayList<FileInfo>();
 		FileInfo fi1_not = new FileInfo(cd1.getNotationFilePath(), cd1.getNotationFileName(), "");
 		FileInfo fi1_uml = new FileInfo(cd1UmlPath, cd1UmlFileName, "");
 		FileInfo fi2_not = new FileInfo(cd2.getNotationFilePath(), cd2.getNotationFileName(), "");
 		FileInfo fi2_uml = new FileInfo(cd2UmlPath, cd2UmlFileName, "");
 		lfi1.add(fi1_not); lfi1.add(fi1_uml);
 		lfi2.add(fi2_not); lfi2.add(fi2_uml);
 		
 		XmiClassDiagramComparer testComparer = new XmiClassDiagramComparer(lfi1, lfi2);
 		
 		obj = testComparer.action(reqobj);
 		
 		// DEBUG
 		System.out.println(obj.toJSONString());
 		
 		// test Compare
 		String reqString2= "{\"Class1\":\"Vehicle\",\"Class2\":\"Vehicle\",\"Request\":\"Compare\"}";
 		reqobj = (JSONObject) JSONValue.parse(reqString2);
 		obj = testComparer.action(reqobj);
 		System.out.println(obj.toJSONString());
 		
 		// test Consolidate
 		String reqString3 = "{\"Request\":\"Consolidate\",\"Class1\":{\"Class\":\"Vehicle\",\"Attributes\":[],\"Operations\":[\"Start()\"]},\"Class2\":{\"Class\":\"Vehicle\",\"Attributes\":[\"<Undefined> Color\"],\"Operations\":[]},\"Same\":{\"Attributes\":[],\"Operations\":[]},\"Name\":\"Vehicle_Vehicle\"}";
 		reqobj = (JSONObject) JSONValue.parse(reqString3);
 		obj = testComparer.action(reqobj);
 		System.out.println(obj.toJSONString());
 	}
 
 }
 
