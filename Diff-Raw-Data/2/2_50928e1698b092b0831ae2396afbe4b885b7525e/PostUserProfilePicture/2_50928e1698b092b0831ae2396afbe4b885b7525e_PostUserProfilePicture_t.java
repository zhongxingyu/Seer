 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlets.actions.post;
 
 import health.database.DAO.UserDAO;
 import health.database.models.UserAvatar;
 import health.database.models.Users;
 import health.database.models.merge.UserInfo;
 import health.input.jsonmodels.JsonUserInfo;
 import health.input.util.DBtoJsonUtil;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 import org.hibernate.Session;
 
 import server.exception.ReturnParser;
 import servlets.util.PermissionFilter;
 import util.AllConstants;
 import util.HibernateUtil;
 import util.ServerConfigUtil;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.stream.JsonWriter;
 
 /**
  * 
  * @author Leon
  */
 public class PostUserProfilePicture extends HttpServlet {
 
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
 	public void processRequest(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException,
 			UnsupportedEncodingException, IOException {
 		response.setContentType("application/json");
 		response.setCharacterEncoding("UTF-8");
 		PrintWriter out = response.getWriter();
 		Users accessUser = null;
 		PermissionFilter filter = new PermissionFilter();
 		String loginID = filter.checkAndGetLoginFromToken(request, response);
 		UserDAO userDao = new UserDAO();
 		if (loginID == null) {
 			if (filter.getCheckResult().equalsIgnoreCase(
 					filter.INVALID_LOGIN_TOKEN_ID)) {
 				ReturnParser.outputErrorException(response,
 						AllConstants.ErrorDictionary.Invalid_login_token_id,
 						null, null);
 				return;
 			} else if (filter.getCheckResult().equalsIgnoreCase(
 					AllConstants.ErrorDictionary.login_token_expired)) {
 				return;
 			} else {
 				ReturnParser.outputErrorException(response,
 						AllConstants.ErrorDictionary.Invalid_login_token_id,
 						null, null);
 				return;
 			}
 		} else {
 			accessUser = userDao.getLogin(loginID);
 		}
 		try {
 			DiskFileItemFactory factory = new DiskFileItemFactory();
 			ServletFileUpload upload = new ServletFileUpload(factory);
 			factory.setRepository(new File(ServerConfigUtil.getConfigValue(AllConstants.ServerConfigs.UserAvatarLocation)));
 			List uploadedItems = null;
 			FileItem fileItem = null;
 			uploadedItems = upload.parseRequest(request);
 			Iterator i = uploadedItems.iterator();
 			String filePath = ServerConfigUtil.getConfigValue(AllConstants.ServerConfigs.UserAvatarLocation);
 			if (!new File(filePath).exists()) {
 				new File(filePath).mkdirs();
 				//filePath = "F:/healthProfilePictures/";
 			}
 			while (i.hasNext()) {
 				fileItem = (FileItem) i.next();
 				if (!fileItem.isFormField()) {
 					if (fileItem.getSize() > 0) {
 						String myFileName = FilenameUtils.getName(fileItem
 								.getName());				
 						
 						System.out.println("debug----post file name:"+myFileName);
 						int mid = myFileName.lastIndexOf(".");
 						String ext = myFileName.substring(mid + 1,
 								myFileName.length());
 						UserAvatar avatar = accessUser.getUserAvatar();
 						if (avatar == null) {
 							avatar = new UserAvatar();
 							UUID uuid = UUID.randomUUID();
 							avatar.setId(uuid.toString());
 							avatar.setUsers(accessUser);
 							accessUser.setUserAvatar(avatar);
 							File existingAvatar=new File(filePath+avatar.getUrl());
 							if(existingAvatar.exists())
 							{
 								existingAvatar.delete();
 							}
 						}
 					//	uploadedFile = File.createTempFile("upload-", ext);
 						String filePathAndName=filePath
 								+ accessUser.getLoginID() + "." + ext;
 						File uploadedFile = new File(filePathAndName);
 						if(uploadedFile.exists())
 						{
 							if(uploadedFile.delete())
 							{
 								System.out.println("Deleted");
 							}
 							else{
 								System.out.println("not Exist or delete problem");
 							}
 						}
 						// System.out.println("final file name:"+myFileName+"."
 						// + ext);
 						fileItem.write(uploadedFile);	
 						avatar.setUrl(accessUser.getLoginID() + "."
								+ ext);
 						Session session = HibernateUtil.beginTransaction();
 						session.update(accessUser);
 						session.saveOrUpdate(accessUser.getUserAvatar());
 						HibernateUtil.commitTransaction();
 						DBtoJsonUtil dbtoJUtil = new DBtoJsonUtil();
 						UserInfo userinfo = userDao.getUserInfo(accessUser
 								.getLoginID());
 						JsonUserInfo juserinfo = dbtoJUtil
 								.convert_a_userinfo(userinfo);
 						Gson gson = new Gson();
 						JsonElement je = gson.toJsonTree(juserinfo);
 						JsonObject jo = new JsonObject();
 						jo.addProperty(AllConstants.ProgramConts.result,
 								AllConstants.ProgramConts.succeed);
 						jo.add("userinfo", je);
 						JsonWriter jwriter = new JsonWriter(
 								response.getWriter());
 						gson.toJson(jo, jwriter);
 					}
 					else{
 						ReturnParser.outputErrorException(response,
 								AllConstants.ErrorDictionary.Input_file_format_error, null,
 								"");
 						return;
 					}
 				}
 			}
 
 		} catch (Exception e) {
 			// TODO: handle exception
 			e.printStackTrace();
 			ReturnParser.outputErrorException(response,
 					AllConstants.ErrorDictionary.Internal_Fault, null,
 					e.getMessage());
 		} finally {
 			out.close();
 		}
 	}
 
 	// <editor-fold defaultstate="collapsed"
 	// desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
 
 	/**
 	 * Handles the HTTP <code>GET</code> method.
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
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
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
 
 	/**
 	 * Returns a short description of the servlet.
 	 * 
 	 * @return a String containing servlet description
 	 */
 	@Override
 	public String getServletInfo() {
 		return "Short description";
 	}// </editor-fold>
 }
