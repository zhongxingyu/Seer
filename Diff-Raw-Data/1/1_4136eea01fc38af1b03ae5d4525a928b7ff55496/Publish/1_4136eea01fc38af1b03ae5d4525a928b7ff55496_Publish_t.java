 package myservs;
 
 import java.io.*;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 import com.oreilly.servlet.multipart.*;
 
 /**
  * Servlet implementation class Publish
  */
 public class Publish extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static final String TMP_DIR_PATH = "/tmp";
 	private File tmpDir;
 	private static final String DESTINATION_DIR_PATH = "/uploads";
 	private File destinationDir;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Publish() {
         super();
         // TODO Auto-generated constructor stub
        
         tmpDir = new File(TMP_DIR_PATH);
         if (!tmpDir.isDirectory()){
         	System.out.println(TMP_DIR_PATH + "is not a directory");
         }
         /*String realPath=getServletContext().getContextPath();
         System.out.println("Context Path: "+realPath);
         System.out.println(realPath);
         destinationDir = new File(realPath);
         if (!destinationDir.isDirectory()){
         	System.out.println(DESTINATION_DIR_PATH + "is not a directory");
         }*/
     }
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		
 		request.setCharacterEncoding("UTF-8");
 		response.setCharacterEncoding("UTF-8");
 		
 		DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
 		//Set the size threshold.
 		fileItemFactory.setSizeThreshold(1*1024*1024); //1mb
 		
 		//Set the temporary upload directory
 		fileItemFactory.setRepository(tmpDir);
 		
 		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
 		try{
 			HttpSession userSession = request.getSession();
 			boolean fieldsEmpty = false;
 			//Check the form fields
			
 			String title=request.getParameter("title");
 			String description=request.getParameter("description");
 			System.out.println("Title param: "+title);
 			System.out.println("Description param: "+description);
 			if (title.isEmpty() || description.isEmpty()){
 				userSession.setAttribute("uploadFieldsEmpty", "1");
 				fieldsEmpty = true;
 			}
 			//Parse the request
 			if (fieldsEmpty!=true){
 				@SuppressWarnings("rawtypes")
 				List items = uploadHandler.parseRequest(request);
 				@SuppressWarnings("rawtypes")
 				Iterator itr = items.iterator();
 				while (itr.hasNext()) {
 					FileItem item = (FileItem) itr.next();
 					boolean isFormField = item.isFormField();
 					// Handle form fields
 					if (isFormField) {
 						System.out.println("Regular Field");
 					} else {
 						// Handle uploaded file
 						// Write file to ultimate destination
 						File file = new File("/opt/uploads", item.getName());
 						item.write(file);
 
 						userSession.setAttribute("fileUpload", "1");
 					}
 				}
 			}
 		}catch(FileUploadException e){
 			e.printStackTrace();
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			RequestDispatcher rd=getServletContext().getRequestDispatcher("/publish.jsp");
 			if(rd!=null){
 				rd.forward(request, response);
 			}
 		}
 	}
 
 }
