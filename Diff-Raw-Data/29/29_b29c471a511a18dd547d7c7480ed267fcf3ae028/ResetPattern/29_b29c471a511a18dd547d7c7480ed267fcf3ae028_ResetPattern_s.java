 package cyberprime.servlets;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.tomcat.util.http.fileupload.FileItem;
 import org.apache.tomcat.util.http.fileupload.FileUploadException;
 import org.apache.tomcat.util.http.fileupload.RequestContext;
 import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
 import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
 import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
 
 import cyberprime.entities.ChangePattern;
 import cyberprime.entities.Clients;
 import cyberprime.entities.dao.ClientsDAO;
 import cyberprime.util.Algorithms;
 import cyberprime.util.Constants;
 import cyberprime.util.EmailSender;
 import cyberprime.util.EmailValidator;
 import cyberprime.util.FileMethods;
 import cyberprime.util.ImageEncryption;
 import cyberprime.util.ImageValidator;
 import cyberprime.util.RandomString;
 
 /**
  * Servlet implementation class ResetPattern
  */
 @WebServlet("/ResetPattern")
 public class ResetPattern extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ResetPattern() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String pattern = request.getParameter("pattern");
 		String userId = request.getParameter("userId");
 		Clients client = new Clients();
 		client.setUserId(userId);
 		String activation = ClientsDAO.getActivation(client);
 		if(activation.equalsIgnoreCase("Reset")){
 			client.setDBPattern(pattern);
 			client.setActivation("Active");
 			ClientsDAO.changePattern(client);
 			ClientsDAO.activateClients(client);
 			request.getRequestDispatcher("templateLogin.jsp").forward(request, response);
 			return;
 		}
 		
 		else if(activation.equalsIgnoreCase("Active")){
 			Object obj = new Object();
			obj = "<p style='color:red'>*You have already reset your pattern</p>";
 			request.setAttribute("loginResult", obj);
 			request.getRequestDispatcher("templateLogin.jsp").forward(request, response);
 			return;
 		}
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		HttpSession session = request.getSession();
 		Clients c = (Clients)session.getAttribute("c");
 		String repos = Constants.DEANE_PATH;
 		Clients client = new Clients();
 		ImageEncryption en = null;
 		File repo = new File(repos);
 		DiskFileItemFactory factory = new DiskFileItemFactory();
 		String saveFileName = "";
 
 			try{
 			factory.setSizeThreshold(2*1024*1024);
 			factory.setRepository(repo);
 	//		if(repo != null)
 	//		System.out.println("Repository :"+repo.getAbsolutePath());
 			ServletFileUpload upload = new ServletFileUpload(factory);
 			RequestContext req = new ServletRequestContext(request);
 			List<FileItem> items = upload.parseRequest(req);
 			Iterator<FileItem> iterator =  items.iterator();
 			
 			while(iterator.hasNext()){
 			FileItem item = iterator.next();
 				if(item.isFormField()){
 					String fieldName = item.getFieldName();
 					if(fieldName.equalsIgnoreCase("email")){
 						client.setEmail(item.getString());
 					}
 					
 
 					EmailValidator ev = new EmailValidator();
 					if(!ev.validate(client.getEmail())){
 						Object obj = new Object();
 						obj = "<p style='color:red'>*Invalid email address</p>";
 						request.setAttribute("resetResult", obj);
 						request.getRequestDispatcher("templateResetPattern.jsp").forward(request, response);
 						return;
 					}
 					
 					else{
 						System.out.println("Email validated");
 					}
 					
 
 				}
 				
 				else{
 					String fileName = item.getName();
 					saveFileName = repos + fileName;
 					File uploadedFile = new File(saveFileName);
 					
 					if(!fileName.isEmpty()){
 						
 
 						ImageValidator iv = new ImageValidator();
 						
 						if(iv.validate(fileName)){
 							try{
 								item.write(uploadedFile);
 								
 							}catch(Exception e){
 								Object obj = new Object();
 								obj = "<p style='color:red'>*Access denied</p>";
 								request.setAttribute("resetResult", obj);
 								request.getRequestDispatcher("templateResetPattern.jsp").forward(request, response);
 								return;
 							}
 
 								en = new ImageEncryption(uploadedFile.getAbsolutePath());
 							
 							client.setImageHash(en.getHash());
 							client.setImageSize(en.getSize());
 							client.setImageExtension(en.getExtension());
 
 						}
 						else if(!iv.validate(fileName)){
 							Object obj = new Object();
 							obj = "<p style='color:red'>*Invalid email address</p>";
 							request.setAttribute("resetResult", obj);
 							request.getRequestDispatcher("templateResetPattern.jsp").forward(request, response);
 							return;
 						}
 
 
 					}
 
 					else{
 						Object obj = new Object();
 						obj = "<p style='color:red'>*Please choose a file</p>";
 						request.setAttribute("resetResult", obj);
 						request.getRequestDispatcher("templateResetPattern.jsp").forward(request, response);
 						return;
 					}
 
 
 				}
 			}
 				response.reset();
 				response.setHeader("Content-Disposition", "inline");
 				response.setHeader("Cache-Control", "no-cache");
 				response.setHeader("Expires", "0");
 				response.setContentType("image/jpg");
 				
 			}catch(FileUploadException e){
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}finally{
 				System.gc();
 				FileMethods.fileDelete(saveFileName);
 			}
 				try{
 					c = ClientsDAO.retrieveClient(client);
 					if(c.getImageHash()==null){
 							Object obj = new Object();
 							obj = "<p style='color:red'>*Pattern reset failed as there is no record of the uploaded image with our database</p>";
 							request.setAttribute("resetResult", obj);
 							request.getRequestDispatcher("templateResetPattern.jsp").forward(request, response);
 							return;
 					}
 					
 					else{
 						if(client.getEmail().equalsIgnoreCase(c.getEmail())){
 							ChangePattern cp = new ChangePattern();
 							try {
 								c.setPattern(cp.getChangedPattern());
 							} catch (NoSuchAlgorithmException e1) {
 								e1.printStackTrace();
 							}
 							String oldPattern = ClientsDAO.changePattern(c);
 							c.setActivation("Reset");
 							ClientsDAO.activateClients(c);
 							EmailSender es = new EmailSender(c);
 							es.sendResetPattern(cp.getContent(),oldPattern);
 							Object obj = new Object();
 							obj = "<p style='color:lime'>*Pattern has been sent to your email</p>";
 							request.setAttribute("resetResult", obj);
 							request.getRequestDispatcher("templateLogin.jsp").forward(request, response);
 							return;
 						
 						}
 						
 						else{
 							Object obj = new Object();
 							obj = "<p style='color:red'>*Email does not correspond to the image uploaded</p>";
 							request.setAttribute("resetResult", obj);
 							request.getRequestDispatcher("templateResetPattern.jsp").forward(request, response);
 							return;
 						}
 					}
 					
 					}catch(Exception e){
 					e.printStackTrace();
 				}
 }
 
 }
