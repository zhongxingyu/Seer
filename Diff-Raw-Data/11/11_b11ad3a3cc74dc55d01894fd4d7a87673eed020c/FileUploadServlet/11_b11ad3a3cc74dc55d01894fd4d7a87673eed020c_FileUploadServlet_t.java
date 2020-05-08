 package se.ixanon.filmhandler.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 
 import se.ixanon.filmhandler.server.objects.MyProgressListener;
 
 @SuppressWarnings("serial")
 public class FileUploadServlet extends HttpServlet {
 
	//TODO Fix this path
	private static final String UPLOAD_DIRECTORY = "filmer";
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		super.doGet(req, resp);
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException
 	{
 		if(ServletFileUpload.isMultipartContent(req))
 		{
 			FileItemFactory factory = new DiskFileItemFactory();
 			ServletFileUpload upload = new ServletFileUpload(factory);
 			
 			MyProgressListener listener = new MyProgressListener();
 			upload.setProgressListener(listener);
 			
 			HttpSession session = req.getSession();
 			session.setAttribute("ProgressListener", listener);
 			
 			try
 			{
 				List<FileItem> items = upload.parseRequest(req);
 				for (FileItem item : items)
 				{
 		
 					if(item.isFormField())
 					{
 						continue;
 					}
 					
 					String fileName = item.getName();
 					
 					if(fileName != null)
 					{
 						fileName = FilenameUtils.getName(fileName);
 					}
 					
 					File uploadedFile = new File(UPLOAD_DIRECTORY, fileName);
 					if (uploadedFile.createNewFile())
 					{
 						item.write(uploadedFile);
 						resp.setStatus(HttpServletResponse.SC_CREATED);
 						resp.getWriter().print("The file was created Successfully");
 						resp.flushBuffer();
 					}
 					else
 					{
 						throw new IOException("File already exists!"); 
 					}
 				}
 				
 			}
 			catch (Exception e)
 			{
 				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 						"Error occured while creating file : " + e.getMessage()); 
 			}
 		}
 		else
 		{
 			resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
 					"Request contents type is not supported by the servlet.");
 		}
 		
 	}
 
 	
 }
