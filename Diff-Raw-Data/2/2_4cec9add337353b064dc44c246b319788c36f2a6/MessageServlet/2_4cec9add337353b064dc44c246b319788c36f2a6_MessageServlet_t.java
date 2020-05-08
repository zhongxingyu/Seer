 package com.dateengine.controllers;
 
 import com.dateengine.models.Profile;
 import com.dateengine.models.Message;
 import com.dateengine.PMF;
 import com.google.appengine.api.users.User;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import javax.servlet.RequestDispatcher;
 import javax.jdo.PersistenceManager;
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.JDOFatalUserException;
 import javax.jdo.Query;
 import java.io.IOException;
 import java.util.List;
 
 public class MessageServlet extends HttpServlet {
    private static final String NEW_MESSAGE_TEMPLATE = "/WEB-INF/templates/messages/new_message.jsp";
    private static final String INBOX_TEMPLATE = "/WEB-INF/templates/messages/inbox.jsp";
 
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       String action = request.getPathInfo().toString();
 
       if (action.equals("/send")) {
          doSendMessage(request, response);
       } else {
          // Some default handler
       }
    }
 
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       String action = request.getPathInfo().toString();
 
       if (action.equals("/new")) {
          doNewMessage(request, response);
       } else if (action.equals("/inbox")) {
          doInbox(request, response);
       } else {
          // Some default handler
       }
    }
 
    private void doNewMessage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       String recipientId = request.getParameter("key");
       // TODO: Extract this to a DAO
 
       PersistenceManager pm = PMF.get().getPersistenceManager();
       Profile recipient;
 
 
       try {
          recipient = pm.getObjectById(Profile.class, recipientId);
          request.setAttribute("recipient", recipient);
 
          RequestDispatcher dispatcher = request.getRequestDispatcher(NEW_MESSAGE_TEMPLATE);
          dispatcher.forward(request, response);
 
       } catch (JDOObjectNotFoundException e) {
          // Render a 404 or some page that says profile not found
          response.sendError(404, "Profile not found");
       } catch (JDOFatalUserException e) { // This means we have a bad key
          response.sendError(404, "Profile not found");
       } finally {
          pm.close();
       }
    }
 
    @SuppressWarnings("unchecked")
    private void doInbox(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       // IMPLEMENT ME
       User currentUser = (User) request.getAttribute("user");
 
       Profile recipient = Profile.findForUser(currentUser);
 
       PersistenceManager pm = PMF.get().getPersistenceManager();
       Query query = pm.newQuery(Message.class);
       List<Message> messages;
       try {
          messages = (List<Message>) query.execute();
          request.setAttribute("messages", messages);
       } finally {
          query.closeAll();
       }
 
       RequestDispatcher dispatcher = request.getRequestDispatcher(INBOX_TEMPLATE);
       dispatcher.forward(request, response);
 
    }
 
    // TODO: Force the User to make a profile before allowing this
    private void doSendMessage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
       String recipientId = request.getParameter("message.recipientKey");
       Profile recipient;
 
       User currentUser = (User) request.getAttribute("user");
 
       Profile sender = Profile.findForUser(currentUser);
 
       PersistenceManager pm = PMF.get().getPersistenceManager();
 
       try {
          recipient = pm.getObjectById(Profile.class, recipientId);
          Message message = new Message();
          message.setBody(request.getParameter("message.body"));
          message.setRecipient(recipient);
          message.setSender(sender);
 
          pm.makePersistent(message);
       } finally {
          pm.close();
       }
 
      response.sendRedirect("/messages/inbox");
    }
 }
