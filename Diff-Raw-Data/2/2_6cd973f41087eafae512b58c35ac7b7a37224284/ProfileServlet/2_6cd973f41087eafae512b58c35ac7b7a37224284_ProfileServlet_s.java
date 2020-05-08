 package com.dateengine.controllers;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.dateengine.models.Profile;
 import com.dateengine.PMF;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import javax.servlet.RequestDispatcher;
 import javax.jdo.PersistenceManager;
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.JDOFatalUserException;
 import java.io.IOException;
 
 public class ProfileServlet extends HttpServlet {
    private static final String VIEW_PROFILE_TEMPLATE   = "/WEB-INF/templates/profile/view_profile.jsp";
    private static final String CREATE_PROFILE_TEMPLATE = "/WEB-INF/templates/profile/create_your_profile.jsp";
    private static final String EDIT_PROFILE_TEMPLATE   = "/WEB-INF/templates/profile/edit_profile.jsp";
 
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       String action = request.getPathInfo().toString();
 
       if(action.equals("/edit")) {
          doSaveProfile(request, response);
       } else {
          // Some default handler
       }
    }
 
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       String action = request.getPathInfo().toString();
 
       if(action.equals("/view")) {
          doViewProfile(request, response);
       } else if (action.equals("/mine")) {
          doViewMyProfile(request, response);
       } else if (action.equals("/edit")) {
          doEditProfile(request, response);
       } else {
          // Some default handler
       }
 
 
    }
 
    private void doSaveProfile(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException {
 
       User currentUser = (User) request.getAttribute("user");
 
       Profile profile = Profile.findForUser(currentUser);
       if(profile == null) {
          profile = new Profile();
         Key key = KeyFactory.createKey(Profile.class.getSimpleName(), currentUser.getEmail());
          profile.setKey(key);
       }
 
       profile.setAboutMe(request.getParameter("profile.aboutMe"));
       profile.setUsername(request.getParameter("profile.username"));
       profile.setPets(request.getParameterValues("profile.pet"));
       profile.setGender(request.getParameter("profile.gender"));
       profile.setMaritalStatus(request.getParameter("profile.maritalStatus"));
 
       PersistenceManager pm = PMF.get().getPersistenceManager();
 
       try {
          pm.makePersistent(profile);         
       } finally {
          pm.close();
       }
 
       response.sendRedirect("/profile/mine");
    }
 
    private void doEditProfile(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException {
 
       User currentUser = (User) request.getAttribute("user");
 
       Profile profile = Profile.findForUser(currentUser);
       request.setAttribute("profile", profile);
 
       RequestDispatcher dispatcher = request.getRequestDispatcher(EDIT_PROFILE_TEMPLATE);
       dispatcher.forward(request, response);
    }
 
 
 
    private void doViewProfile(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException {
       String key = request.getParameter("id");
 
       PersistenceManager pm = PMF.get().getPersistenceManager();
       Profile profile = null;
 
       try {
          profile = pm.getObjectById(Profile.class, key);
 
       } catch (JDOObjectNotFoundException e) {
          // Render a 404 or some page that says profile not found
          response.sendError(404, "Profile not found");
       } catch (JDOFatalUserException e) { // This means we have a bad key
          response.sendError(404, "Profile not found");
       } finally {
          pm.close();
       }
 
       request.setAttribute("profile", profile);
       
       RequestDispatcher dispatcher = request.getRequestDispatcher(VIEW_PROFILE_TEMPLATE);
       dispatcher.forward(request, response);
    }
 
    private void doViewMyProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
 
       User currentUser = (User) request.getAttribute("user");
 
       Profile profile = Profile.findForUser(currentUser);
       request.setAttribute("profile", profile);
 
       if(profile != null) {
          RequestDispatcher dispatcher = request.getRequestDispatcher(VIEW_PROFILE_TEMPLATE);
          dispatcher.forward(request, response);
       } else {
          RequestDispatcher dispatcher = request.getRequestDispatcher(CREATE_PROFILE_TEMPLATE);
          dispatcher.forward(request, response);
       }
    }
 }
