 package it.unibz.controller;
 
 import it.unibz.dao.FavouritesDAO;
 import it.unibz.dao.FtpConnectionDAO;
 import it.unibz.model.FavouriteBean;
 import it.unibz.model.FtpConnectionBean;
 import it.unibz.model.UserBean;
 import it.unibz.util.FTPConnectionManager;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 
 /**
  * Class responsible to delegate requests for Data retrieval and fetching
  * information using DAO and Managers
  * 
  */
 public class FtpConnectionsServlet extends HttpServlet
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8868246447873132050L;
 
 	/**
 	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
 	 * methods.
 	 * 
 	 * @param request
 	 *          servlet request
 	 * @param response
 	 *          servlet response
 	 * @throws IOException
 	 * @throws ServletException
 	 * @throws Exception
 	 *           if no ftp connection
 	 */
 	protected void processRequest(HttpServletRequest request,
 			HttpServletResponse response) throws IOException, ServletException {
 		HttpSession s = request.getSession();
 		// check if request is Multipart= contains files to upload
 		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 		if (isMultipart) {
 			handleMultipartRequest(request, response, s);
 		} else {
 
 			// Check if user logged in
 			UserBean user = null;
 			if (s != null && s.getAttribute("currentSessionUser") != null)
 				user = (UserBean) s.getAttribute("currentSessionUser");
 			else
 				return;
 			// Check if activity parameter is passed
 			if (request.getParameter("activity") != null) {
 				handleActivityRequest(request, response, user,
 						(String) request.getParameter("activity"));
 			}
 
 		}
 
 	}
 
 	/**
 	 * MEthod handels request which are an "Activity request"
 	 * 
 	 * @param request
 	 *          servlet request
 	 * @param response
 	 *          servlet response
 	 * @param user
 	 *          Current session user
 	 * @param activity
 	 *          Activity to perform
 	 * @throws ServletException
 	 * @throws IOException
 	 *           if no ftp connection
 	 */
 	@SuppressWarnings("rawtypes")
 	private void handleActivityRequest(HttpServletRequest request,
 			HttpServletResponse response, UserBean user, String activity)
 			throws ServletException, IOException {
 
 		// Retrieve all Connections
 		if (activity.equals("getall")) {
 			response.setContentType("text/html;charset=UTF-8");
 
 			FtpConnectionDAO catalog = new FtpConnectionDAO();
 			ArrayList catalogItems = null;
 			if (user != null)
 				catalogItems = catalog.getItems(user.getID());
 			else
 				catalogItems = new ArrayList();
 
 			String callback = request.getParameter("callback");
 			request.setAttribute("ftpConnections", catalogItems);
 			request.setAttribute("callback", callback);
 
 			RequestDispatcher dispatcher = request
 					.getRequestDispatcher("ftpConnectionsJSon.jsp");
 			dispatcher.include(request, response);
 		}
 
 		// Create or edit connection
 		if ((activity.equals("create") || activity.equals("edit")) && user != null) {
 			FtpConnectionBean cb = new FtpConnectionBean();
 			cb.setHost(request.getParameter("host"));
 			cb.setPassword(request.getParameter("password"));
 			cb.setConnectionname(request.getParameter("connectionname"));
 			cb.setPort(request.getParameter("port").length() > 0 ? Integer
 					.parseInt(request.getParameter("port")) : 21);
 			cb.setUsername(request.getParameter("username"));
 			cb.setUserID(user.getID());
 			FtpConnectionDAO dao = new FtpConnectionDAO();
 			response.getOutputStream().println(
 					(activity.equals("create") ? dao.createConnection(cb) : dao
 							.editConnection(cb)) ? "success" : "fail");
 
 		}
 		// Remove Connection
 		if (activity.equals("removeConnection")) {
 			FtpConnectionDAO dao = new FtpConnectionDAO();
 			FtpConnectionBean cb = new FtpConnectionBean();
 			cb.setConnectionname(request.getParameter("connectionname"));
 			cb.setUserID(user.getID());
 			response.getOutputStream().println(
 					dao.removeConnection(cb) ? "success" : "fail");
 
 		}
 		// Retrieve all folders. Additional connection checks needed because of
 		// continuous folder
 		// jumping using Favourites
 		if (activity.equals("getfolders")) {
 			FtpConnectionDAO dao = new FtpConnectionDAO();
 			String connectionname = request.getParameter("connectionname");
 			HttpSession ss = request.getSession();
 			FTPConnectionManager ftpconmgr = null;
 			// First check if no connectionname
 			if (ss.getAttribute("connectionmanager") == null) {
 				FtpConnectionBean cb = dao.getItem(user.getID(), connectionname);
 				ftpconmgr = new FTPConnectionManager(connectionname);
 				try {
 					ftpconmgr.doConnection(cb.getUsername(), cb.getPassword(),
 							cb.getHost(), cb.getPort());
					ss.setAttribute("connectionmanager", ftpconmgr);
					ss.setAttribute("currentfolder", request.getParameter("currentfolder"));
 				} catch (Exception e) {
					response.getOutputStream().println(
"fail");
					return;
 				}
 			} else {
 				ftpconmgr = (FTPConnectionManager) ss.getAttribute("connectionmanager");
 			}
 			// now check if connection still the same
 			if (ftpconmgr.getConnectionname().equals(connectionname)
 					|| connectionname.equals("/")) {
 				ss.setAttribute(
 						"currentfolder",
 						request.getParameter("currentfolder") == "" ? "/" : request
 								.getParameter("currentfolder"));
 			} else {
 				// Disconnect and create new
 				ftpconmgr.removeConnection();
 				FtpConnectionBean cb = dao.getItem(user.getID(), connectionname);
 				// ftpconmgr= new FTPConnectionManager(connectionname);
 				ftpconmgr.setConnectionname(connectionname);
 				try {
 					ftpconmgr.doConnection(cb.getUsername(), cb.getPassword(),
 							cb.getHost(), cb.getPort());
					ss.setAttribute("connectionmanager", ftpconmgr);
					ss.setAttribute("currentfolder", request.getParameter("currentfolder"));
 				} catch (Exception e) {
					response.getOutputStream().println(
"fail");
					return;
 				}

 			}
 
 			ArrayList catalogItems = null;
 			if (user != null) {
 				catalogItems = ftpconmgr.getFileList((String) ss
 						.getAttribute("currentfolder"));
 			} else
 				catalogItems = new ArrayList();
 			// ftpconmgr.removeConnection();
 
 			String callback = request.getParameter("callback");
 			request.setAttribute("foldercontent", catalogItems);
 			request.setAttribute("callback", callback);
 
 			RequestDispatcher dispatcher = request
 					.getRequestDispatcher("ftpFolderJSon.jsp");
 			dispatcher.include(request, response);
 
 		}
 		// Remove file/folder
 		if (activity.equals("removeItem")) {
 			FTPConnectionManager ftpconmgr = (FTPConnectionManager) request
 					.getSession().getAttribute("connectionmanager");
 			String filename = (String) request.getParameter("itemname");
 			boolean isDir = ((String) request.getParameter("itemtype")).equals("1");
 			boolean ret=ftpconmgr.deleteItem(filename, isDir);
 			// If folder remove check if we have to remove also from favourites
 			if (ret&&isDir) {
 				FavouriteBean cb = new FavouriteBean();
 				cb.setFolderPATH(filename);
 				cb.setUserID(user.getID());
 				cb.setConnectionNAME(ftpconmgr.getConnectionname());
 				FavouritesDAO dao = new FavouritesDAO();
 				if (dao.checkFavourite(cb))
 					dao.removeFavoriteByConnectionname(cb);
 			}
 			response.getOutputStream().println(
 					ret ? "success" : "fail");
 		}
 		// Make new Directory
 		if (activity.equals("makedir")) {
 			FTPConnectionManager ftpconmgr = (FTPConnectionManager) request
 					.getSession().getAttribute("connectionmanager");
 			response.getOutputStream().println(
 					ftpconmgr.makeDirectory((String) request
 							.getParameter("currentfolder")
 							+ "/"
 							+ ((String) request.getParameter("dirname"))) ? "success"
 							: "fail");
 		}
 		// Rename file/directory
 		if (activity.equals("renamename")) {
 			FTPConnectionManager ftpconmgr = (FTPConnectionManager) request
 					.getSession().getAttribute("connectionmanager");
 			
 			//Check if folder a favourite
 			boolean isDir = ((String) request.getParameter("itemtype")).equals("1");
 			// If folder remove check if we have to remove also from favourites
 			boolean ret=ftpconmgr.renameFileOrDir(
 					(String) request.getParameter("currentfolder"),
 					(String) request.getParameter("oldname"),
 					(String) request.getParameter("renamename"));
 					
 			if (ret&&isDir) {
 				FavouriteBean cb = new FavouriteBean();
 				cb.setFolderPATH((String) request.getParameter("currentfolder")+"/"+(String) request.getParameter("oldname"));
 				cb.setUserID(user.getID());
 				cb.setConnectionNAME(ftpconmgr.getConnectionname());
 				FavouritesDAO dao = new FavouritesDAO();
 				if (dao.checkFavourite(cb))
 					dao.changeFavouritePath(cb,(String) request.getParameter("currentfolder")+"/"+(String) request.getParameter("renamename"));
 			}
 			response.getOutputStream()
 			.println(
 					ret ? "success"
 							: "fail");
 		}
 		// Download file
 		if (activity.equals("downloadfile")) {
 			FTPConnectionManager ftpconmgr = (FTPConnectionManager) request
 					.getSession().getAttribute("connectionmanager");
 
 			String filename = (String) request.getParameter("filename");
 			response
 					.getOutputStream()
 					.println(
 							ftpconmgr.downloadFile(
 									(String) request.getParameter("currentfolder"),
 									filename,
 									getServletContext().getRealPath("temp")
 											+ (getServletContext().getRealPath("temp").contains("/") ? "/"
 													: "\\")) ? "success" : "fail");
 
 		}
 		// Return all favourite folders
 		if (activity.equals("getallfavourites")) {
 
 			response.setContentType("text/html;charset=UTF-8");
 
 			FavouritesDAO catalog = new FavouritesDAO();
 			ArrayList catalogItems = null;
 			if (user != null)
 				catalogItems = catalog.getItems(user.getID());
 			else
 				catalogItems = new ArrayList();
 
 			String callback = request.getParameter("callback");
 			request.setAttribute("favourites", catalogItems);
 			request.setAttribute("callback", callback);
 
 			RequestDispatcher dispatcher = request
 					.getRequestDispatcher("favouritesJSon.jsp");
 			dispatcher.include(request, response);
 		}
 		// Remove a favourite Folder
 		if (activity.equals("removeFavourite")) {
 			FavouritesDAO dao = new FavouritesDAO();
 			FavouriteBean cb = new FavouriteBean();
 			try {
 				cb.setConnectionID(Integer.parseInt(request
 						.getParameter("connectionid")));
 			} catch (Exception e) {
 				response.getOutputStream().println("fail");
 			}
 			cb.setUserID(user.getID());
 			cb.setFolderPATH(request.getParameter("currentFolder"));
 			response.getOutputStream().println(
 					dao.removeFavorite(cb) ? "success" : "fail");
 
 		}
 		// Remove favourite from folder property dialog
 		if (activity.equals("removeInlineFavourite")) {
 			FavouritesDAO dao = new FavouritesDAO();
 			FavouriteBean cb = new FavouriteBean();
 			FTPConnectionManager ftpconmgr = (FTPConnectionManager) request
 					.getSession().getAttribute("connectionmanager");
 			cb.setConnectionNAME(ftpconmgr.getConnectionname());
 			cb.setUserID(user.getID());
 			cb.setFolderPATH(request.getParameter("filename"));
 			response.getOutputStream().println(
 					dao.removeFavoriteByConnectionname(cb) ? "success" : "fail");
 		}
 		// Add a favourite
 		if (activity.equals("addFavourite")) {
 			FavouriteBean cb = new FavouriteBean();
 			cb.setFolderPATH(request.getParameter("itemname"));
 			cb.setUserID(user.getID());
 			FTPConnectionManager ftpconmgr = (FTPConnectionManager) request
 					.getSession().getAttribute("connectionmanager");
 			cb.setConnectionNAME(ftpconmgr.getConnectionname());
 			FavouritesDAO dao = new FavouritesDAO();
 			response.getOutputStream().println(
 					dao.addFavourite(cb) ? "success" : "fail");
 		}
 		// Check if folder a favourite
 		if (activity.equals("isfavourite")) {
 			FavouriteBean cb = new FavouriteBean();
 			cb.setFolderPATH(request.getParameter("filename"));
 			cb.setUserID(user.getID());
 			FTPConnectionManager ftpconmgr = (FTPConnectionManager) request
 					.getSession().getAttribute("connectionmanager");
 			cb.setConnectionNAME(ftpconmgr.getConnectionname());
 			FavouritesDAO dao = new FavouritesDAO();
 			response.getOutputStream().println(dao.checkFavourite(cb) ? "yes" : "no");
 		}
 	}
 
 	/**
 	 * Handle File upload Requests
 	 * 
 	 * @param request
 	 *          servlet request
 	 * @param response
 	 *          servlet response
 	 * @param s
 	 * @throws IOException
 	 */
 	@SuppressWarnings("rawtypes")
 	private void handleMultipartRequest(HttpServletRequest request,
 			HttpServletResponse response, HttpSession s) throws IOException {
 		ServletFileUpload servletFileUpload = new ServletFileUpload(
 				new DiskFileItemFactory());
 		String ret = "";
 
 		// Extract file from request
 		try {
 			List fileItemsList = servletFileUpload.parseRequest(request);
 			String optionalFileName = "";
 			FileItem fileItem = null;
 
 			Iterator it = fileItemsList.iterator();
 			while (it.hasNext()) {
 				FileItem fileItemTemp = (FileItem) it.next();
 				if (fileItemTemp.isFormField()) {
 					if (fileItemTemp.getFieldName().equals("filename"))
 						optionalFileName = fileItemTemp.getString();
 				} else
 					fileItem = fileItemTemp;
 			}
 
 			if (fileItem != null) {
 				String fileName = fileItem.getName();
 
 				/* Save the uploaded file if its size is greater than 0. */
 				if (fileItem.getSize() > 0) {
 					if (optionalFileName.trim().equals(""))
 						fileName = FilenameUtils.getName(fileName);
 					else
 						fileName = optionalFileName;
 
 					String path = getServletContext().getRealPath("temp")
 							+ (getServletContext().getRealPath("temp").contains("/") ? "/"
 									: "\\");
 					File saveTo = new File(path + fileName);
 					// Store file to temppath
 					fileItem.write(saveTo);
 
 					FTPConnectionManager ftpconmgr = (FTPConnectionManager) request
 							.getSession().getAttribute("connectionmanager");
 
 					// Upload file to server
 					boolean result = ftpconmgr.uploadFile(s.getAttribute("currentfolder")
 							.toString(), fileName, saveTo.getAbsolutePath());
 					fileItem.delete();
 					// Return message to IFRAME
 					if (result) {
 						ret += "<div id=\"status\">success</div>";
 						ret += "<div id=\"message\">Successfully Uploaded</div>";
 						// return the upload file
 						ret += "<div id=\"uploadedfile\">" + fileName + "</div>";
 					} else {
 						ret += "<div id=\"status\">failed</div>";
 						ret += "<div id=\"message\">There is already such a File</div>";
 					}
 
 				}
 			}
 		} catch (Exception e) {
 			ret += "<div id=\"status\">failed</div>";
 			ret += "<div id=\"message\">" + e.getMessage() + "</div>";
 
 		} finally {
 			response.getOutputStream().println(ret);
 		}
 	}
 
 	// <editor-fold defaultstate="collapsed"
 	// desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
 	/**
 	 * Handles the HTTP <code>GET</code> method.
 	 * 
 	 * @param request
 	 *          servlet request
 	 * @param response
 	 *          servlet response
 	 * @throws ServletException
 	 *           if a servlet-specific error occurs
 	 * @throws IOException
 	 *           if an I/O error occurs
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 	/**
 	 * Handles the HTTP <code>POST</code> method.
 	 * 
 	 * @param request
 	 *          servlet request
 	 * @param response
 	 *          servlet response
 	 * @throws ServletException
 	 *           if a servlet-specific error occurs
 	 * @throws IOException
 	 *           if an I/O error occurs
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 }
