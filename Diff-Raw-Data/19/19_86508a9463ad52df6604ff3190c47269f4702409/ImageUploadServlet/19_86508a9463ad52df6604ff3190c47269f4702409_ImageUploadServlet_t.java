 package com.soup.content;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.DiskFileUpload;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUpload;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang3.StringUtils;
 
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
 import com.soup.login.User;
 import com.soup.util.DatabaseHelper;
 import com.soup.util.FilenameGenerator;
 
 /**
  * Servlet implementation class ImageUpload
  */
 @WebServlet("/upload")
 public class ImageUploadServlet extends HttpServlet
 {
 	private static final long serialVersionUID = 1L;
 	private File repo;
 	private FilenameGenerator generator = new FilenameGenerator();
 	private static String[] validExtensions = {"jpg", "jpeg", "gif", "png", "bmp",};
 	
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public ImageUploadServlet()
 	{
 		super();
 		// TODO Auto-generated constructor stub
 	}
 	
 	
 
 	@Override
 	public void init() throws ServletException
 	{
 		super.init();
 		repo = new File(getServletContext().getRealPath("/") + "pics");
 	}
 
 
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException
 	{
 		handleRequest(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException
 	{
 		handleRequest(request, response);
 	}
 
 	private void handleRequest(HttpServletRequest request,
 			HttpServletResponse response)
 	{
 		HttpSession session = request.getSession();
 		User user = (User) session.getAttribute("user");
 		JdbcPooledConnectionSource cs = DatabaseHelper.getDBConnection();
 
 		try
 		{
 			// store all files to the disk, sizeThreshold = 0
 			DiskFileItemFactory factory = new DiskFileItemFactory();
 			factory.setSizeThreshold(1024*1024);
 			ServletFileUpload  sfu = new ServletFileUpload (factory);
 			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 			// Create a new file upload handler
 			System.out.println(isMultipart);
 
 			// Parse the request
 			List<FileItem> items = sfu.parseRequest(request);
 
 			for (FileItem item : items)
 			{
 				if (item.isFormField())
 				{
 //					System.out.println("its a field");
 					continue;
 				}
 
 //				System.out.println("its a file");
 				
 				// create random file name
 				String extension = FilenameUtils.getExtension(item.getName());
 				if(!isValidExtension(extension))
 				{
 					response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
 				}
				String url_name = generator.generateFilename();
 				// save file to disk
				File tosave = new File(repo, url_name + "." + extension);
 				item.write(tosave);
 				
 				Dao<Picture, Integer> picDAO = DaoManager.createDao(cs, Picture.class);
 				Picture pic = new Picture();
 				pic.setUrlName(url_name);
				pic.setFilename(url_name + "." + extension);
 				pic.setUser(user);
 				picDAO.create(pic);
				
				// until detail servlet/page is working redirect to home
				response.sendRedirect("/soup");
 			}
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private boolean isValidExtension(String ext)
 	{
 		for(String str : validExtensions)
 		{
 			if(StringUtils.equalsIgnoreCase(ext, str))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
