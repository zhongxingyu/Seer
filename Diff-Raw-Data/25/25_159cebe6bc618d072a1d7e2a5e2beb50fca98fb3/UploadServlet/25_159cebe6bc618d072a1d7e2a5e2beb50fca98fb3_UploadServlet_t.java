 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.tomcat.util.http.fileupload.FileItem;
 import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
 import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
 
 import repository.DiagramDAO;
 import repository.EditingHistoryDAO;
 import controller.upload.UploadProcessor;
 import controller.upload.UploadProcessorFactory;
 import domain.Diagram;
 import domain.EditingHistory;
 import logging.Log;
 import java.util.ArrayList;
 import java.util.List;
 import controller.upload.FileInfo;
 /**
  * 
  * @author wintor12
  */
 
 /**
  * Information class that contains all the features of one UploadServlet @ doc
  * author Rui Hou
  */
 
 public class UploadServlet extends HttpServlet {
 
 	private static final String TMP_DIR_PATH = "/uploads/";
 	private static final String DESTINATION_DIR_PATH = "/uploads/";
 	private static final String LIB_DIR_PATH = "/lib/";
 	private static final long serialVersionUID = 1L;
 	private File tmpDir;
 	private File destinationDir;
 	private File libDir;
 	private List<FileInfo> fileList;
 	
 	public UploadServlet() {
		
 	}
 
 	/**
 	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
 	 * methods.
 	 * 
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 * @throws ServletException
 	 *             if a servlet-specific error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	protected void processRequest(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		HttpSession session = request.getSession();
 		// Set id properly
 		String id = session.getAttribute("userId").toString();
 
 		ServletContext context = getServletContext();
 		
 		tmpDir = new File(context.getRealPath(TMP_DIR_PATH));
 
 		destinationDir = new File(context.getRealPath(DESTINATION_DIR_PATH));
 
 		libDir = new File(context.getRealPath(LIB_DIR_PATH));
		
		fileList = new ArrayList<FileInfo>();
		
 		DiskFileItemFactory dfif = new DiskFileItemFactory();
 		dfif.setSizeThreshold(1 * 1024 * 1024);// 1MB
 		dfif.setRepository(tmpDir);
		String filename = "";
		
 		ServletFileUpload uploadHandler = new ServletFileUpload(dfif);
 		try {
 			List<?> items = uploadHandler.parseRequest(request);
 
 			Iterator<?> itr = items.iterator();
 			while (itr.hasNext()) {
 				FileItem item = (FileItem) itr.next();
				
				if (item.getName().isEmpty()) {
					// Skip if there is no name for the file
					continue;
				}
				
 				filename = item.getName();
 
 				if ((!item.isFormField()) && (!item.getName().equals(""))
 						&& (!id.equals(""))) {// check if item is a file
 					String newName = renameFile(id, item.getName());// rename
 																	// file
 					File file = new File(destinationDir, newName);
 					item.write(file);
 					String absolutePath = destinationDir + "\\";
 					String relativePath = context.getContextPath()
 							+ DESTINATION_DIR_PATH;
 					String libPath = libDir + "\\";
 
 					request.setAttribute("originalFileName", item.getName());
 					request.setAttribute("newFileName", newName);
 					request.setAttribute("size", item.getSize());
 					request.setAttribute("absolutePath", absolutePath + newName);
 					request.setAttribute("relativePath", relativePath + newName);
 					request.setAttribute("javaFile", relativePath + newName
 							+ ".java");
 					fileList.add(new FileInfo(absolutePath,newName,libPath));
 					//Log.LogCreate().Info(" File list " + absolutePath  +"  "  + newName + " "  + libPath);
 					String ecoreFileName = "uploads/" + newName;
 					newName += ".png";
 					this.storeDatabase(ecoreFileName, newName,
 							Integer.parseInt(id));
 				}
 			}
 
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 
 		}
 
 		// Obtains upload processor to perform parsing and file
 		// generations		
 		if (!fileList.isEmpty()) {
 			UploadProcessor processor = UploadProcessorFactory
 					.getUploadProcessorMethod(filename,fileList );
 			if (processor != null){
 
 				processor.process();
 			}
 		}	
 		RequestDispatcher rd = request.getRequestDispatcher("Display");
 		rd.forward(request, response);
 
 	}
 
 	/**
 	 * Prefixes user ID and time for to file name. Used to create a unique file
 	 * name.
 	 * 
 	 * @param userId
 	 * @param originalFileName
 	 * @return new file name with prefixse
 	 */
 	private String renameFile(String userId, String originalFileName) {
 		String time = new SimpleDateFormat("yyyy-MM-dd_HHmmss")
 				.format(new java.util.Date());
 		String newName = userId + "_" + time + "_" + originalFileName;
 		return newName;
 	}
 
 	/*
 	 * function to store upload diagram information into database.
 	 */
 	private void storeDatabase(String path, String fileName, int userID) {
 		try {
 			Diagram diagramObj = new Diagram();
 			diagramObj.setDiagramName(fileName);
 			diagramObj.setEcoreFilePath(path);
 			diagramObj.setInEdition(false);
 			diagramObj.setOwnerId(userID);
 			diagramObj.setProjectId(2);
 			DiagramDAO.addDiagram(diagramObj);
 
 			EditingHistory editObj = new EditingHistory();
 			editObj.setDiagramId(diagramObj.getDiagramId());
 			editObj.setUserId(userID);
 
 			EditingHistoryDAO.addHistory(editObj);
 		} catch (IllegalArgumentException e) {
 		}
 	}
 
 	/**
 	 * Handles the HTTP <code>POST</code> method.
 	 * 
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 * @throws ServletException
 	 *             if a servlet-specific error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 }
