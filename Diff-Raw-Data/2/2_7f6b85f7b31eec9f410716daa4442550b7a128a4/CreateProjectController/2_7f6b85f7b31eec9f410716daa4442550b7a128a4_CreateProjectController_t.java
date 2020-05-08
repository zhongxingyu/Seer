 package com.ted.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.ted.domain.Project;
 import com.ted.domain.User;
 import com.ted.service.ProjectService;
 import com.ted.service.ServiceExDBFailure;
 import com.ted.service.ServiceExProjectExists;
 import com.ted.service.UserService;
 import com.ted.validators.Validators;
 
 @WebServlet("/createproject")
 public class CreateProjectController extends Controller {
 
 	private static final long serialVersionUID = -3120249482338819138L;
 	private static final String SUCCESS = "Το project δημιουργήθηκε επιτυχώς";
 	private UserService userService = new UserService();
 	private ProjectService projectService = new ProjectService();
 
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// if (request.getAttribute("initialized") == null) {
 		helperProjectArrays(request, null, new ArrayList<String>());
 		// }
 		sc.getRequestDispatcher(CREATE_PROJECT_JSP).forward(request, response);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// get form parameters
 		String name = null, description = null, publik = null, manager = null, staffMember = null;
 		String[] addedStaff1 = request.getParameterValues("added");
 		List<String> addedStaff = new ArrayList<>();
 		if (addedStaff1 != null) {
 			// if addedStaff1 were null a null pointer exception would be thrown
			addedStaff.addAll(Arrays.asList(addedStaff1));
 		}
 		name = request.getParameter("name");
 		description = request.getParameter("description");
 		publik = request.getParameter("publik");
 		manager = request.getParameter("manager");
 		staffMember = request.getParameter("staffMember");
 		// if "Create project" was pressed
 		if (request.getParameter("createProject") != null) {
 			boolean error = false;
 			// validation
 			// check if name is empty
 			if (Validators.isNullOrEmpty(name)) {
 				request.setAttribute("emptyName", true);
 				error = true;
 			} else try {
 				if (new ProjectService().isProjectNameDuplicate(name)) {
 					request.setAttribute("duplicateProjectName", true);
 					error = true;
 				}
 			} catch (ServiceExDBFailure e1) {
 				e1.printStackTrace();
 				request.setAttribute("ErrorString", e1.getMessage());
 				error = true;
 			}
 			// check if description is empty
 			if (Validators.isNullOrEmpty(description)) {
 				request.setAttribute("emptyDescription", true);
 				error = true;
 			}
 			// check if publik is empty
 			if (Validators.isNullOrEmpty(publik)) {
 				request.setAttribute("emptyPublik", true);
 				error = true;
 			}
 			// check if manager is empty
 			if (Validators.isNullOrEmpty(manager)) {
 				request.setAttribute("emptyManager", true);
 				error = true;
 			}
 			// check if addedStaff is empty
 			if (addedStaff.isEmpty()) {
 				request.setAttribute("emptyStaff", true);
 				error = true;
 			}
 			// if there is no error persist the project
 			if (!error) {
 				Project proj = new Project();
 				proj.setName(name);
 				proj.setDescription(description);
 				proj.setPublik("private".equals(publik) ? false : true);
 				try {
 					proj.setManager(userService.getUserWithUsername(manager));
 					List<User> staff = getUsersFromUsernames(addedStaff);
 					proj.setStaff(staff);
 					projectService.createProject(proj);
 					String rand = messageKey(request, SUCCESS);
 					response.sendRedirect(PROJECTLIST_SERVLET + "?r=" + rand);
 					return;
 				} catch (ServiceExDBFailure e) {
 					log.debug("CreateProjectController::doPost", e);
 					request.setAttribute("ErrorString", e.getMessage());
 				} catch (ServiceExProjectExists e) {
 					log.debug("CreateProjectController::doPost", e);
 					request.setAttribute("duplicateProjectName", e.getMessage());
 				}
 			}
 		}
 		request.setAttribute("name", name);
 		request.setAttribute("description", description);
 		request.setAttribute("publik", publik);
 		request.setAttribute("selectedManager", manager);
 		helperProjectArrays(request, staffMember, addedStaff);
 		RequestDispatcher rd = sc.getRequestDispatcher(CREATE_PROJECT_JSP);
 		rd.forward(request, response);
 		return;
 	}
 
 	private void helperProjectArrays(HttpServletRequest request,
 			String staffMember, List<String> addedStaff) {
 		try {
 			// AN PATH8HKE TO PROS8HKH STAFF
 			// 8etw allManagers kai allStaff attributes
 			if (request.getParameter("addStaff") != null) {
 				if (staffMember != null) {
 					addedStaff.add(staffMember);
 				}
 			}
 			request.setAttribute("addedStaff", addedStaff);
 			List<String> allStaff = new ArrayList<>();
 			List<User> allStaffUsers = userService.allStaff();
 			for (User user : allStaffUsers) {
 				allStaff.add(user.getUsername());
 			}
 			allStaff.removeAll(addedStaff);
 			request.setAttribute("allStaff", allStaff);
 			List<String> allManagers = new ArrayList<>();
 			List<User> allManagersUsers = userService.allManagers();
 			for (User user : allManagersUsers) {
 				allManagers.add(user.getUsername());
 			}
 			request.setAttribute("allManagers", allManagers);
 		} catch (ServiceExDBFailure e) {
 			log.debug("CreateProjectController::helperProjectArrays", e);
 			request.setAttribute("ErrorString", e.getMessage());
 		}
 	}
 
 	private List<User> getUsersFromUsernames(List<String> usernames)
 			throws ServiceExDBFailure {
 		List<User> users = new ArrayList<>();
 		for (String username : usernames) {
 			users.add(userService.getUserWithUsername(username));
 		}
 		return users;
 	}
 }
