 package controller;
 
 import static utils.CloseableUtil.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.Part;
 
 import org.apache.commons.lang.StringUtils;
 
 import service.UserService;
 import utils.StreamUtil;
 import beans.User;
 import exception.NoRowsUpdatedRuntimeException;
 
 @WebServlet(urlPatterns = { "/settings" })
 @MultipartConfig(maxFileSize = 100000)
 public class SettingsServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		HttpSession session = request.getSession();
 		User loginUser = (User) session.getAttribute("loginUser");
 
 		if (session.getAttribute("editUser") == null) {
 			User editUser = new UserService().getUser(loginUser.getId());
 			session.setAttribute("editUser", editUser);
 		}
 
 		request.getRequestDispatcher("settings.jsp").forward(request, response);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		List<String> messages = new ArrayList<String>();
 
 		HttpSession session = request.getSession();
 
 		User editUser = getEditUser(request);
 		session.setAttribute("editUser", editUser);
 
 		if (isValid(request, messages) == true) {
 
 			try {
 				new UserService().update(editUser);
 			} catch (NoRowsUpdatedRuntimeException e) {
 				session.removeAttribute("editUser");
 				messages.add("他の人によって更新されています。最新のデータを表示しました。データを確認してください。");
 				session.setAttribute("errorMessages", messages);
 				response.sendRedirect("settings");
 			}
 
 			session.setAttribute("loginUser", editUser);
 			session.removeAttribute("editUser");
 
 			response.sendRedirect("./");
 		} else {
 			session.setAttribute("errorMessages", messages);
 			response.sendRedirect("settings");
 		}
 	}
 
 	private User getEditUser(HttpServletRequest request)
 			throws IOException, ServletException {
 
 		HttpSession session = request.getSession();
 		User editUser = (User) session.getAttribute("editUser");
 
 		editUser.setName(request.getParameter("name"));
 		editUser.setAccount(request.getParameter("account"));
 		editUser.setPassword(request.getParameter("password"));
 		editUser.setEmail(request.getParameter("email"));
 		editUser.setDescription(request.getParameter("description"));
		editUser.setIcon(getIcon(request));
 		return editUser;
 	}
 
 	private byte[] getIcon(HttpServletRequest request) throws IOException,
 			ServletException {
 
 		Part part = request.getPart("icon");
 		byte[] icon = null;
 		if (part.getSize() == 0) {
 			return icon;
 		}
 
 		InputStream inputStream = null;
 		try {
 			inputStream = part.getInputStream();
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			StreamUtil.copy(inputStream, baos);
 			icon = baos.toByteArray();
 			return icon;
 		} finally {
 			close(inputStream);
 		}
 	}
 
 	private boolean isValid(HttpServletRequest request, List<String> messages) {
 
 		String account = request.getParameter("account");
 		String password = request.getParameter("password");
 
 		if (StringUtils.isEmpty(account) == true) {
 			messages.add("アカウント名を入力してください");
 		}
 		if (StringUtils.isEmpty(password) == true) {
 			messages.add("パスワードを入力してください");
 		}
 		// TODO アカウントが既に利用されていないか、メールアドレスが既に登録されていないかなどの確認も必要
 		if (messages.size() == 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 }
