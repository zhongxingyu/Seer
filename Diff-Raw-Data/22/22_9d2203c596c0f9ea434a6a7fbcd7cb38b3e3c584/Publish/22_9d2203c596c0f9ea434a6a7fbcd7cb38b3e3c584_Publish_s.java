 package myservs;
 
 import java.io.*;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
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
 			Map<String,String> paramMap = new HashMap<String,String>();
 			
 			//Parse the request
 			@SuppressWarnings("rawtypes")
 			List items = uploadHandler.parseRequest(request);
 			@SuppressWarnings("rawtypes")
 			Iterator itr = items.iterator();
 			while (itr.hasNext()) {
 				FileItem item = (FileItem) itr.next();
 				boolean isFormField = item.isFormField();
 				// Handle form fields
 				if (isFormField) {
 					System.out.println(item.getFieldName());
 
 					if ((item.getString().isEmpty()) && (item.getFieldName().equals("description"))){
 						System.out.println("Empty Field");
 						fieldsEmpty=true;
 					}
 				}else{
 					System.out.println("File Name: "+item.getName());
 					System.out.println("File Type: "+item.getContentType());
 
 					if (item.getName().isEmpty()){
 						fieldsEmpty=true;
 					}
 					
					if (fieldsEmpty==false){
 						System.out.println("File Handling");
 						// Handle uploaded file
 						// Write file to ultimate destination
 						System.out.println(item.getName());
 						File file = new File("/opt/uploads", item.getName());
 						item.write(file);
 
						userSession.setAttribute("fileUpload", "1");
					}else{
						userSession.setAttribute("uploadFieldsEmpty", "1");
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
