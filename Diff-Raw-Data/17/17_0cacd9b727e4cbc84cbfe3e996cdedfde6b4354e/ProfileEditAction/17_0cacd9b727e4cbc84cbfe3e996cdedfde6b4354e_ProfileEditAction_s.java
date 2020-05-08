 package edu.cmu.cs15437.clubwebsite.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.mybeans.dao.DAOException;
 import org.mybeans.form.FormBeanException;
 import org.mybeans.form.FormBeanFactory;
 
 import edu.cmu.cs15437.clubwebsite.databeans.UserBean;
 import edu.cmu.cs15437.clubwebsite.formbeans.ProfileEditForm;
 import edu.cmu.cs15437.clubwebsite.model.Model;
 import edu.cmu.cs15437.clubwebsite.model.UserDAO;
 
 public class ProfileEditAction extends Action {
 	private FormBeanFactory< ProfileEditForm > profileEditFormBeanFactory = FormBeanFactory.getInstance(ProfileEditForm.class);
 	private UserDAO userDAO;
 	
 	public ProfileEditAction(Model model) {
 		userDAO = model.getUserDAO();
 	}
 	
 	public String getName() {
 		return "profileEdit.do";
 	}
 	
 	public String perform(HttpServletRequest request) {
 		List< String > errors = new ArrayList< String >();
 		request.setAttribute("errors", errors);
 		
 		try {
 			ProfileEditForm form = profileEditFormBeanFactory.create(request);
 			
 			// No form is passed in; let the user try again
 			if (! form.isPresent()) {
				System.out.println("No form is passed in");
 				return "profileEdit.jsp";
 			}
			System.out.println(form.getFirstName());
			System.out.println(form.getLastName());
			System.out.println(form.getSex());
			System.out.println(form.getUserName());
 			
 			if (form.getButton().equals("Change Password")) {
 				// User wants to change password
 				return "changePassword.jsp";
 			}
 			
 			errors.addAll(form.getValidationErrors());
 			// If there is any error; let the user try again
 			if (errors.size() > 0) {
 				return "profileEdit.jsp";
 			}
 			
 			// Update User Info
 			UserBean user = (UserBean) request.getSession().getAttribute("user");
 			userDAO.updateFirstName(user.getUserId(), form.getFirstName());
 			userDAO.updateLastName(user.getUserId(), form.getLastName());
 			userDAO.updateSex(user.getUserId(), form.getSex());
 			userDAO.updateUserName(user.getUserId(), form.getUserName());
 			user = userDAO.lookupWithUserId(user.getUserId());
 			
 			// Remember the user
 			HttpSession session = request.getSession();
 			session.setAttribute("user", user);
 			
 			return "profile.jsp";
 		} catch (DAOException e) {
 			errors.add(e.getMessage());
 			return "profileEdit.jsp";
 		} catch (FormBeanException e) {
 			errors.add(e.getMessage());
 			return "profileEdit.jsp";
 		}
 	}
 }
 
