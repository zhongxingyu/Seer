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
 
import org.apache.commons.io.FilenameUtils;
 import org.apache.tomcat.util.http.fileupload.FileItem;
 import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
 import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
 
 import repository.DiagramDAO;
 import controller.upload.UploadProcessor;
 import controller.upload.UploadProcessorFactory;
 import domain.Diagram;
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
 	private String newFolder;
 	private ServletContext context;
 	private String id_file_date;
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
 
 		context = getServletContext();
 		
 		tmpDir = new File(context.getRealPath(TMP_DIR_PATH));
 
 		//destinationDir = new File(context.getRealPath(DESTINATION_DIR_PATH));
 
 		libDir = new File(context.getRealPath(LIB_DIR_PATH));
 		
 		fileList = new ArrayList<FileInfo>();
 		
 		DiskFileItemFactory dfif = new DiskFileItemFactory();
 		dfif.setSizeThreshold(1 * 1024 * 1024);// 1MB
 		dfif.setRepository(tmpDir);
 		String filename = "";
 		
 		ServletFileUpload uploadHandler = new ServletFileUpload(dfif);
 		try {
			ServletRequestContext src = new ServletRequestContext(request);
			List<?> items = uploadHandler.parseRequest(src);
 			destinationDir = createDir(id)	;											// file
 			
 			Iterator<?> itr = items.iterator();
 			while (itr.hasNext()) {
 				FileItem item = (FileItem) itr.next();
 				
				
 				if (item.getName().isEmpty()) {
 					// Skip if there is no name for the file
 					continue;
 				}
 				
 				filename = item.getName();
				if (filename != null) {
			        filename = FilenameUtils.getName(filename);
			    }
				
 
 				if ((!item.isFormField()) && (!item.getName().equals(""))
 						&& (!id.equals(""))) {// check if item is a file
 					//String newName = renameFile(id, item.getName());// rename
 					File file = new File(destinationDir, filename);		
 					item.write(file);
 					String absolutePath = destinationDir + "\\";
 					String relativePath = context.getContextPath()
 							+ newFolder;
 					String libPath = libDir + "\\";
 					logging.Log.LogCreate().Info("absolutePath " + absolutePath);		
 					request.setAttribute("originalFileName", filename);
 					request.setAttribute("newFileName", filename);
 					request.setAttribute("size", item.getSize());
 					request.setAttribute("absolutePath", absolutePath + filename);
 					request.setAttribute("relativePath", relativePath + filename);
 					request.setAttribute("javaFile", relativePath + filename
 							+ ".java");
 					fileList.add(new FileInfo(absolutePath,filename,libPath));
 					//Log.LogCreate().Info(" File list " + absolutePath  +"  "  + newName + " "  + libPath);
 					
 					if (isFileType(filename,"ecore")){
 						String image_file_name = filename + ".png";	
 						String folder = "uploads/" + id_file_date + "/" + filename;
 						this.storeDatabase(folder, image_file_name,
 								Integer.parseInt(id));
 					}
 				}
 			}
 
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 
 		}
 
 		// Obtains upload processor to perform parsing and file 
 		// generations		
 		if (!fileList.isEmpty()) {
 			String folderPath = "uploads/" + id_file_date ;
 			UploadProcessor processor = UploadProcessorFactory
 					.getUploadProcessorMethod(filename  ,fileList, folderPath, Integer.parseInt(id));
 			logging.Log.LogCreate().Info("got processor filename =" + filename);
 			if (processor != null){
 			    logging.Log.LogCreate().Info("calling process ");
 				processor.process();
 			}
 		}	
 		RequestDispatcher rd = request.getRequestDispatcher("Display");
 		rd.forward(request, response);
 
 	}
 
 	private File createDir(String id) {
 		id_file_date = new SimpleDateFormat("yyyy-MM-dd_HHmmss") .format(new java.util.Date());
 		newFolder = context.getRealPath(DESTINATION_DIR_PATH) + "/" + id_file_date ;
 		File dir = new File(newFolder);
 		logging.Log.LogCreate().Info("Creating folder = " + dir.toString());
 		if(!dir.mkdirs()  )
 		{
 			logging.Log.LogCreate().Info("Failed to create folder " + newFolder);
 		}
 
 		return dir;
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
 			diagramObj.setFilePath(path);
 			diagramObj.setMerged(0);
 			diagramObj.setUserId(userID);
 			diagramObj.setProjectId(2);
 			diagramObj.setDiagramType("Ecore");
 			//diagramObj.setDiFileName("baseFileName" + ".di");
 			//diagramObj.setNotationFileName("baseFileName" + ".notation");
 			//diagramObj.setDiFilepath("folder");
 			//diagramObj.setNotationFilePath("folder");
 			//diagramObj.setDiagramType("diagramType");
 			DiagramDAO.addDiagram(diagramObj);
 			
 			/*
 			EditingHistory editObj = new EditingHistory();
 			editObj.setDiagramId(diagramObj.getDiagramId());
 			editObj.setUserId(userID);
 
 			EditingHistoryDAO.addHistory(editObj);
 			*/
 		} catch (IllegalArgumentException e) {
 			System.out.println("error" + e.getMessage());
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
 
 	private boolean isFileType(String fileName, String fileType) {
 		// Retrieve file extension
 		String extension = fileName.substring(
 					fileName.lastIndexOf(".") + 1, fileName.length());
 		return (extension.equals(fileType) ? true : false);	
 	}
 }
