 package il.ac.shenkar.studentdata;
 
 
 
 import java.awt.List;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.activation.MimetypesFileTypeMap;
 import javax.jws.soap.SOAPBinding.Use;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.swing.text.html.HTMLDocument.Iterator;
 
 import org.apache.commons.collections.list.SetUniqueList;
 import org.apache.derby.tools.sysinfo;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 import org.apache.tomcat.util.buf.UDecoder;
 import org.apache.tomcat.util.http.fileupload.FileItem;
 import org.apache.tomcat.util.http.fileupload.FileItemFactory;
 import org.apache.tomcat.util.http.fileupload.FileUploadException;
 import org.apache.tomcat.util.http.fileupload.RequestContext;
 import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
 import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
 
 import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;
 @WebServlet("/StudentData/*")
 //@WebServlet("/*")
 public class StudentDataController extends HttpServlet 
 {
 	final  String localPath = "C:\\Users\\Jacob\\workspaceEE\\StudentData\\Files\\";
 	final String prefix = "http://localhost:8080/StudentData/StudentData/";
 	final String prefixTest="http://localhost:8080/StudentData/StudentData/";
 	static Logger logger = Logger.getLogger(StudentDataController.class.getName());
 	private RequestParser parser;
 	private FileItemFactory factory;
 	
 	public StudentDataController() 
 	{
 		parser = new RequestParser();
 		factory = new DiskFileItemFactory();
 		//configure the logger OB
 		BasicConfigurator.configure();
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
 	{
 		
 		
 		String reqAction = parser.getRequestedAction(req.getRequestURI());
 		if (reqAction.equals("Login.jsp"))
 		{
 			try {
 				getServletContext().getRequestDispatcher("/Login.jsp").forward(req, resp);
 			} catch (ServletException e) {
 				// TODO Auto-generated catch block  
 				e.printStackTrace();
 			}
 		}
 		
 		switch (reqAction)
 		{
 		case "register.jsp":
 		{
 			// get the Register Ob 
 			Register register = new Register();
 			register.getLists();
 			// attached the register OB to the Session OB
 			req.getSession().setAttribute("register", register);
 			// Forward the User to register.jsp
 			logger.info("Forword request to register.jsp");
 			getServletContext().getRequestDispatcher("/register.jsp").forward(req, resp);
 			return;
 		
 		}
 		case "add_to_cart=true":
 		{
 			this.cartTransaction(req, resp, Cart.ADD);
 			return;
 		}
 		case "upload.jsp":
 		{
 			try {
 				//TODO: check if the session didn't timeout
 				Register register = new Register();
 				register.getLists();
 				// attached the register OB to the Session OB
 				req.getSession().setAttribute("register", register);
 				// add log comment
 				logger.info("register object attached to the Session OB \nforward to upload.jsp");
 				getServletContext().getRequestDispatcher("/upload.jsp").forward(req, resp);
 				return;
 			} catch (Exception e) {
 				// TODO: handle exception
 				logger.error("faild to forword to /upload.jsp");
 				return;
 			}
 			
 			
 		}
 		case "cart.jsp":
 		{
 			try {
 				if (this.IsSessionValidate(req, resp))
 				{
 					HttpSession session = req.getSession();
 					User user = (User) session.getAttribute("user");										// get the user OB from the Session
 					logger.info("call getFileList with files to view" );
 					java.util.List<String> filesList1 = parser.getFileList(user.getFilesToView());			//get the files list to display the cart 
 					session.setAttribute("cart",filesList1);												// attached the File List to the Session
 					getServletContext().getRequestDispatcher("/cart.jsp").forward(req, resp);				// forward the request to cart.jsp
 				}
 				else
 				{
 					//mean the user need t login first. the Session is timeout
 					logger.info("Session timeout or new session, forword the user to login");
 					getServletContext().getRequestDispatcher("/Login.jsp").forward(req, resp);
 				}
 			} catch (ServletException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				
 			}
 			return;
 			
 			
 			
 			
 		}
 		case "history.jsp":
 		{
 			try {
 				if (this.IsSessionValidate(req, resp))
 				{
 					HttpSession session = req.getSession();
 					User user = (User) session.getAttribute("user");									// get the user OB from the Session
 					logger.info("call getFileList with files history");
 					java.util.List<String> filesHistory = parser.getFileList(user.getFilesHistory());	//get the files list to display the user file history 
 					session.setAttribute("history",filesHistory);										// attached the File List to the Session
 					getServletContext().getRequestDispatcher("/history.jsp").forward(req, resp);		// forward the request to history.jsp
 				}
 				else
 				{
 					//mean the user need t login first. the Session is timeout
 					logger.info("Session timeout or new session, forword the user to login");
 					getServletContext().getRequestDispatcher("/Login.jsp").forward(req, resp);
 				}
 			} catch (ServletException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return;
 		}
 		case "about.jsp":
 		{
 			//TODO: to return something to cadan
 			return;
 		}
 		case "homePage.jsp":
 		{
 			try
 			{
 				if (this.IsSessionValidate(req, resp))
 				{
 					//forword to After Login
 					logger.info("Forward to /AfterLogin.jsp");
 					getServletContext().getRequestDispatcher("/AfterLogin.jsp").forward(req, resp);
 					return;
 				}
 				//mean the user need t login first. the Session is timeout
 				logger.info("Session timeout or new session, forword the user to login");
 				getServletContext().getRequestDispatcher("/Login.jsp").forward(req, resp);
 				return;
 			}catch (ServletException e)
 			{
 				logger.error("Cannot Forward to, requesr failed");
 				return;
 			}
 		}
 		case "get_files=true":
 		{
 			try {
 				if (this.IsSessionValidate(req, resp))
 				{
 					// get the course name
 					logger.info("Calling the paser with uri: "+ req.getRequestURI());
 					String course = parser.getCourseFromPath(req.getRequestURI());
 					
 					//get the Session
 					HttpSession currSession = req.getSession();
 					java.util.List userFiles = (java.util.List) currSession.getAttribute("userFiles");
 					ArrayList<FileRecord> courseFilesList = new FileRecord().getFilesWithCourse((ArrayList<FileRecord>) userFiles, course);
 					//attache the Objects to the Session Object
 					currSession.setAttribute("courseFiles", courseFilesList);
 					currSession.setAttribute("massage", course);
 					
 					// forward to File pages 
 					logger.info("forward to File pages");
 					getServletContext().getRequestDispatcher("/FilesPage.jsp").forward(req, resp);
 					return;
 				}
 				}catch (ServletException e)
 				{
 					logger.error("Cannot Forward to, requesr failed");
 					return;
 				}
 		}
 		case "remove_from_cart=true":	
 			this.cartTransaction(req, resp, Cart.REMOVED);
 			break;
 		case "logout=true":
 		{
 			// here i will invalidate the Session and redirect to the login page
 			req.getSession(true).invalidate();
			resp.sendRedirect(prefix+"/StudentData/Login.jsp");
 			return;
 			
 		}
 		case "download":
 		{
 			//get the session
 			HttpSession session = req.getSession();
 			
 			// check if the session is new & if the "user" atribute is attached to the object
 			if (session.isNew() || (session.getAttribute("user")==null))
 			{
 				try {
 					req.setAttribute("massage","Please Login First");
 					getServletContext().getRequestDispatcher("/Login.jsp").forward(req, resp);
 					return;
 				} catch (ServletException | IOException e) {
 					e.printStackTrace();
 				}
 			}
 			String filename = parser.getFilePath(req.getRequestURI());			// get the fool filename 
 			File file =  FileSystemHandler.getInstande().getFile(filename);		// get the file from the file system
 		    resp.setContentLength((int)file.length());							// set the content length
 			// set the file's mime type
 		    resp.setContentType(Files.probeContentType(file.toPath()));
 		    
 		    //send the file to the client
 		    FileInputStream in = new FileInputStream(file);
 	        OutputStream out = resp.getOutputStream();
 	        
 	        // Copy the contents of the file to the output stream
 	        byte[] buf = new byte[1024];
 	        int count = 0;
 	        while ((count = in.read(buf)) >= 0) 
 	        {
 	            out.write(buf, 0, count);
 	        }
 	        in.close();
 	        out.close();
 		}
 		default:
 			break;
 		}
 	}
 
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException 
 	{
 		
 		
 		String reqAction = parser.getRequestedAction(req.getRequestURI());
 		
 		switch (reqAction) 
 		{
 		case "loginto":
 		{
 			// the user entered username and password and we need to check if the user exist in the DB and to manage his session
 			User user = (User) UserDAO.getInstance().getRecord(req.getParameter("email"));
 			if (user != null)
 			{
 				if (!user.isPasswordOK(req.getParameter("password")))
 				{
 					//mean the isPasswordOK method return false and the user did not loged in sucssesfuly
 					// we need to forword it to the same page again so he will try again;
 					getServletContext().getRequestDispatcher("/Login.jsp").forward(req, resp);
 					return;
 				}
 				else
 				{
 					// mean the user logged in successfuly and we need to get the user data from the DB
 					//TODO to pass cadan the object and the data he need in order to create the user UI
 					String[] columns = {"university", "trend","year"};
 					String[] conditionls = {user.getUniversity(), user.getTrend(), user.getYear()};
 					java.util.List filesList = FileRecordDAO.getInstance().getRecordsWhere(columns, conditionls);
 					ArrayList<String> subjectList = FileRecordDAO.getInstance().getSubjectList(filesList);
 					
 					// attached the list & the user to the Session
 					HttpSession currSession = req.getSession();
 					currSession.setAttribute("userFiles", filesList);
 					currSession.setAttribute("user", user);
 					currSession.setAttribute("subjects", subjectList);
 					
 					java.util.List<String> filesList1 = parser.getFileList(user.getFilesToView());
 					currSession.setAttribute("cart",filesList1);
 					
 					java.util.List<String> filesHistory = parser.getFileList(user.getFilesHistory());
 					currSession.setAttribute("history", filesHistory);
 					currSession.setAttribute("prefix", prefixTest);
 					
 					getServletContext().getRequestDispatcher("/AfterLogin.jsp").forward(req, resp);
 					return;
 				}
 			}
 			else
 			{
 				//user does not exists
 				req.setAttribute("message", "Email does not exists \nPlease try again");
 				getServletContext().getRequestDispatcher("/Login.jsp").forward(req, resp);
 				return;
 			}
 		}
 		case "register":
 		{
 			//first i'll check if the user is already exists
 			if (UserDAO.getInstance().isExist(req.getParameter("email")))
 			{
 				// user is already exists
 				req.setAttribute("massage","User "+ req.getParameter("email") +" is allready exists");
 				logger.info("User "+ req.getParameter("email") +" is allready exists");
 				getServletContext().getRequestDispatcher("/Login.jsp").forward(req, resp);
 				return;
 			}
 			logger.info("User does not exists in the DB, Creating new User");
 			// the user entered his details and we need to register him to the system
 			//TODO all of the validation of the form are performed in the client side, here i do not dial with it
 			User newUser = new User();
 			newUser.setEmail(req.getParameter("email"));
 			newUser.setSubject("none");
 			newUser.setPassword(req.getParameter("password"));
 			newUser.setTrend(req.getParameter("trend"));
 			newUser.setUserName(req.getParameter("username"));
 			newUser.setUniversity(req.getParameter("uni"));
 			newUser.setYear(req.getParameter("year"));
 			newUser.setFilesHistory("none");
 			newUser.setFilesToView("none");
 			logger.info(newUser.toString());
 			// add the user to the DB
 			
 			if (UserDAO.getInstance().addRecord(newUser) == 1)
 			{
 				//get the current session and place the user OB to the current session 
 				HttpSession userSession = req.getSession();
 				userSession.setAttribute("user", newUser);
 				//TODO here we can determine the Session parameters like timeout...
 				
 				//TODO get all of the user document and pass it to the client side
 				java.util.List<String> filesList = parser.getFileList(newUser.getFilesToView());
 				userSession.setAttribute("cart",filesList);
 				
 				java.util.List<String> filesHistory = parser.getFileList(newUser.getFilesHistory());
 				userSession.setAttribute("history", filesHistory);
 				
 				String[] columns = {"university", "trend","year"};
 				String[] conditionls = {newUser.getUniversity(), newUser.getTrend(), newUser.getYear()};
 				java.util.List userFiles = FileRecordDAO.getInstance().getRecordsWhere(columns, conditionls);
 				// attached the list & the user to the Session
 				HttpSession currSession = req.getSession();
 				currSession.setAttribute("userFiles", userFiles);
 
 				ArrayList<String> subjectList = FileRecordDAO.getInstance().getSubjectList( userFiles);
 				currSession.setAttribute("subjects",subjectList);
 				
 				req.getServletContext().getRequestDispatcher("/AfterLogin.jsp").forward(req, resp);
 				return;
 			}
 			else
 			{
 				// mean the insert failed 
 				getServletContext().getRequestDispatcher("/newuser.jsp").forward(req, resp);
 				return;
 			}	
 		}
 		case "addUnis":
 		{
 			// get the unies string from the form
 			String uniString = req.getParameter("unis");
 			if (uniString == null)
 			{
 				resp.sendRedirect(req.getHeader("referer"));
 			}
 			// split the string into Unis
 			String [] unies = uniString.split(",");
 			UniRecord record;
 			for (int j =0; j<unies.length; j++)
 			{
 				record = new UniRecord();
 				record.setUniname(unies[j]);
 				UniDAO.getInstance().addRecord(record);
 			}
 			resp.sendRedirect(req.getHeader("referer"));
 			return;
 		}
 		case "addTrends":
 		{
 			// get the trends string from the form
 			String trendString = req.getParameter("trends");
 			if (trendString == null)
 			{
 				resp.sendRedirect(req.getHeader("referer"));
 				logger.info("input was empty");
 			}
 			// split the string into Trends
 			String [] trends = trendString.split(",");
 			Trend record;
 			for (int j =0; j<trends.length; j++)
 			{
 				record = new Trend();
 				record.setTrendName(trends[j]);
 				TrendDAO.getInstance().addRecord(record);
 			}
 			resp.sendRedirect(req.getHeader("referer"));
 			return;
 		}
 		case "addCourses":
 		{
 			// get the trends string from the form
 			String courseString = req.getParameter("courses");
 			if (courseString == null)
 			{
 				resp.sendRedirect(req.getHeader("referer"));
 				logger.info("input was empty");
 			}
 			// split the string into Trends
 			String [] courses = courseString.split(",");
 			CourseRecord record;
 			for (int j =0; j<courses.length; j++)
 			{
 				record = new CourseRecord();
 				record.setCourseName(courses[j]);
 				CourseRecordDAO.getInstance().addRecord(record);
 			}
 			resp.sendRedirect(req.getHeader("referer"));
 			return;
 		}
 		case "upload=true":
 		{
 			HttpSession session = req.getSession();
 			// first i need to check if the session is valid
 			if (session.isNew())
 			{
 				// mean the session is new
 				req.setAttribute("massage","Please Login First");
 				getServletContext().getRequestDispatcher("/Login.jsp").forward(req, resp);
 				return;
 			}
 			User user = (User) session.getAttribute("user");
 			if (user == null)
 			{
 				// mean the session is new
 				req.setAttribute("massage","Please Login First");
 				getServletContext().getRequestDispatcher("/Login.jsp").forward(req, resp);
 				return;
 			}
 			
 			if (!ServletFileUpload.isMultipartContent(req))
 			{
 				//file not uploaded
 				req.setAttribute("massage","Upload Failed");
 				resp.sendRedirect(req.getHeader("referer"));  /*this will redirect the request back to the page who sent the request*/
 				return;
 			}
 		
 		
 			
 			try {
 				
 				ServletFileUpload upload = new ServletFileUpload(factory);
 				java.util.List<FileItem> items = upload.parseRequest((RequestContext) req);
 				
 				//create the file
 				FileRecord record = new FileRecord();
 				record.setDescription(items.get(1).getString());
 				record.setSubject(items.get(5).getString());
 				record.setTrend(items.get(4).getString());
 				record.setUniversity( items.get(2).getString());
 				record.setYear(items.get(3).getString());
 				//get the file path 
 				/*	path look like this uni/trend/year/course*/
 				String path = record.builtPath();
 				// add the file name to the path
 				path = path + items.get(0).getName();
 				record.setPath(path);
 				
 			
 				//save the file to the server file system
 				if (FileSystemHandler.getInstande().saveFile(items.get(0),path) == 1)
 				{	
 					//after the file is saved to the file system we need to add the record to the DB	
 					FileRecordDAO.getInstance().addRecord(record);
 					// attached a massage 
 					req.setAttribute("massage","File: " +items.get(0).getName() +" was upload sucssesfuly!");
 					
 					if (record.getYear().equals(user.getYear()) && record.getTrend().equals(user.getTrend()) && record.getUniversity().equals(user.getUniversity()))
 					{
 						//mean the file should be in the user files so we will update it
 						//get the files list from the Session
 						java.util.List filesList = (java.util.List) session.getAttribute("userFiles");
 						//add the current file to the files list
 						filesList.add(record);
 						ArrayList<String> subjectList = FileRecordDAO.getInstance().getSubjectList(filesList);
 						
 						//attached the object i modified to the session again
 						session.setAttribute("subjects", subjectList);
 						session.setAttribute("userFiles", filesList);
 					}
 					//Forward the request to after login again
 					req.getServletContext().getRequestDispatcher("/AfterLogin.jsp").forward(req, resp);
 					return;
 				}
 				
 			} catch (FileUploadException e) 
 			{
 				e.printStackTrace();
 				//file not uploaded
 				req.setAttribute("massage","Upload Failed");
 				resp.sendRedirect(req.getHeader("referer"));  /*this will redirect the request back to the page who sent the request*/
 				return;
 			}
 			
 			
 		}
 
 		default:
 			break;
 		}
 		
 		
 		
 		
 /*		String path = "uni/trend/year/course";
 		boolean isMultipart = ServletFileUpload.isMultipartContent(req);
 		System.out.println("request: "+req);
 		if (!isMultipart)
 		{
 			System.out.println("File Not Uploaded");
 		} 
 		else 
 		{
 			FileItemFactory factory = new DiskFileItemFactory();
 			ServletFileUpload upload = new ServletFileUpload(factory);
 			java.util.List<FileItem> items = null;
 			try
 			{
 				items = upload.parseRequest(req);
 				System.out.println("items: "+items);
 				
 				path += "/"+ items.get(0).getName();
 				if (FileSystemHandler.getInstande().saveFile(items.get(0),path) == 1)
 				{	
 					FileRecord record = new FileRecord(0, "uni", "trend", "year", "course", path , 0);
 					FileRecordDAO.getInstance().addRecord(record);
 					getServletContext().getRequestDispatcher("/it_works.jsp").forward(req, resp);
 				}
 
 			} catch (FileUploadException e) 
 			{
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			}
 		*/
 		}
 	public boolean IsSessionValidate(HttpServletRequest req, HttpServletResponse resp)
 	{
 		//get the session
 		HttpSession session = req.getSession();
 		
 		// check if the session is new & if the "user" atribute is attached to the object
 		if (session.isNew() || (session.getAttribute("user")==null))
 		{
 			req.setAttribute("massage","Please Login First");
 			return false;
 		}
 		return true;
 	}
 	
 	public void cartTransaction(HttpServletRequest req, HttpServletResponse resp,Cart direction) throws ServletException, IOException
 	{
 		//get the session
 		HttpSession session = req.getSession();
 		
 		// check if the session is new & if the "user" atribute is attached to the object
 		if (session.isNew() || (session.getAttribute("user")==null))
 		{
 			try {
 				req.setAttribute("massage","Session Time out, Please login again");
 				getServletContext().getRequestDispatcher("/Login.jsp").forward(req, resp);
 				return;
 			} catch (ServletException | IOException e) {
 				e.printStackTrace();
 			}
 		}
 		User user = (User) session.getAttribute("user");				// get the user
 		String filename = parser.getFilePath(req.getRequestURI());		// get the fool filename 
 		if (direction == Cart.ADD)
 		{
 			//check if the file name exists in the cart list already
 			if (user.isExistInCart(filename))
 			{
 				session.setAttribute("fileExistInTheCartMassage","File: " + filename+ " already exists in the cart");
 				logger.info("File: " + filename+ " already exists in the cart");
 				//forward the request to FilesPage.jsp
 				resp.sendRedirect(req.getHeader("referer"));  /*this will redirect the request back to the page who sent the request*/
 				return;
 			}
 			user.addToCart(filename);										// adding to the cart
 			// update the cart list and add it to the session
 			java.util.List<String> filesList1 = parser.getFileList(user.getFilesToView());
 			session.setAttribute("cart",filesList1);
 			session.setAttribute("addFileSuccsedMessage","File: " + filename+ " was added to the cart list");
 			logger.info("File: " + filename+ " was added to the cart list");
 			// update the DB
 			UserDAO.getInstance().updateRecord(user);
 			//forward the request to FilesPage.jsp 
 			resp.sendRedirect(req.getHeader("referer"));  /*this will redirect the request back to the page who sent the request*/
 		}
 		else
 		{
 			//remove from cart & adding to history
 			user.removeFromCart(filename);								
 			
 			// create new cart files object and attached them to the Session 
 			java.util.List<String> cartFiles = parser.getFileList(user.getFilesToView());
 			session.setAttribute("cart", cartFiles);
 			
 			// create new Files History file and attached it to the Session
 			java.util.List<String> filesHistory = parser.getFileList(user.getFilesHistory());
 			session.setAttribute("history", filesHistory);
 			session.setAttribute("massage","File: " + filename+ " was remove from cart and added to the history list");
 			logger.info("File: " + filename+ " was remove from cart and added to the history list");
 			// update the DB
 			UserDAO.getInstance().updateRecord(user);
 			//forward the request to FilesPage.jsp
 			resp.sendRedirect(req.getHeader("referer"));  /*this will redirect the request back to the page who sent the request*/
 		}
 		return;
 	}
 	
 	public enum Cart
 	{
 		ADD,REMOVED
 	}
 		
 }
