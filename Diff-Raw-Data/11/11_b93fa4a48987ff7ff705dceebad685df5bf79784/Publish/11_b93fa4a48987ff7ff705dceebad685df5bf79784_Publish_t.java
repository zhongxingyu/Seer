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
 
 		response.setCharacterEncoding("UTF-8");
 		request.setCharacterEncoding("UTF-8");
 		
 		DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
 		//Set the size threshold.
 		fileItemFactory.setSizeThreshold(1*1024*1024); //1mb
 		
 		//Set the temporary upload directory
 		fileItemFactory.setRepository(tmpDir);
 		
 		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
 		try{
 			HttpSession userSession = request.getSession();
 			boolean fieldsEmpty = false;
 			boolean validContent=true;
 			boolean overSize=false;
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
 					paramMap.put(item.getFieldName(), item.getString());
 
 					if ((item.getString().isEmpty()) && (item.getFieldName().equals("description"))){
 						fieldsEmpty=true;
 					}
 				}else{
 					if (item.getName().isEmpty()){
 						fieldsEmpty=true;
 					}
 					String contentType=item.getContentType();
 					String permittedContentType="application/pdf";
 					if (!contentType.contentEquals(permittedContentType)){
 						validContent=false;
 					}
 					
 					if (item.getSize()>10000000){
 						overSize=true;
 					}
 					if (fieldsEmpty==true){
 						userSession.setAttribute("uploadFieldsEmpty", "1");
 					}else if (validContent==false){
 						userSession.setAttribute("notValidContent", "1");
 					}else if(overSize==true){
 						userSession.setAttribute("overSize", "1");
 					}else{
 						// Handle uploaded file
 						// Write file to ultimate destination
 						File file = new File("/opt/uploads", item.getName());
 						item.write(file);
 						userSession.setAttribute("fileUpload", "1");
 						//Write to database
						String isoTitle=paramMap.get("title");
						//Convert to UTF-8
						String title=new String(isoTitle.getBytes("ISO8859_1"),"UTF-8");
						String isoDescription=paramMap.get("description");
						//Convert to UTF-8
						String description=new String(isoDescription.getBytes("ISO8859_1"),"UTF-8");
 						String location="/opt/uploads/"+item.getName();
 						String userid=userSession.getAttribute("id").toString();
 						System.out.println("Title: "+title+"\nDesc: "+description+"\nlocation: "+location+"\nUser id: "+userid);
 						DatabaseMethods dbPoint = new DatabaseMethods();
						int result=dbPoint.uploadTable(title,description,location,userid);
 						if (result==0){
 							userSession.setAttribute("dbFailure", "1");
 						}
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
