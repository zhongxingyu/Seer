 package com.ems.controller.priv;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 import com.ems.dao.EventDao;
 import com.ems.dao.GroupDao;
 import com.ems.dao.UserDao;
 import com.ems.model.Group;
 import com.ems.model.User;
 
 
 /**
  * Servlet implementation class GroupController
  */
 @WebServlet(urlPatterns = {
 		"/private/groupList.html", 
 		"/private/group.jsp", 
 		"/private/groupAdd",
 		"/private/groupDelete"
 		})
 public class GroupController extends HttpServlet {
 	
 	// commons logging references
 	static Logger log = Logger.getLogger(GroupController.class.getName());
 	
 	private static final long serialVersionUID = 1L;
     private static String INSERT_OR_EDIT = "/group.jsp";
     private static String LIST_USER = "/groupList.jsp";
     private static String UNAUTHORIZED_PAGE = "/WEB-INF/jsp/private/errors/unauthorized.jsp";
 
     /**
 	 * @uml.property  name="dao"
 	 * @uml.associationEnd  multiplicity="(1 1)"
 	 */
     private GroupDao dao;   
 	
     /**
      * @see HttpServlet#HttpServlet()
      */
     public GroupController() {
         super();
         log.debug("###################################");
     	log.trace("START");
 		dao = new GroupDao();
         log.debug("Dao object instantiated");
         log.trace("END");
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	log.trace("START");
     	
     	log.debug("id_event: " + request.getParameter("id_event"));
     	
 		UserDao ud = new UserDao();
 		User  systemUser = ud.getUserByEmail(request.getUserPrincipal().getName());
 		
 		HttpSession session = request.getSession(true);
 		session.removeAttribute("systemUser");
 		session.setAttribute("systemUser",systemUser);
 		
     	int id_event = 0;
     	if (request.getParameter("id_event") != null){
     		id_event = Integer.parseInt(request.getParameter("id_event").toString());
     	}
     	EventDao evnDao = new EventDao();
     	request.setAttribute("id_event", evnDao.getRecordById(id_event).getName());
     	
     	String forward="";
         String action = request.getParameter("action");
         if (action == null){
         	log.debug("action is NULL");
         	action="";
         }
 
 // #########################################################################################        
         if (action.equalsIgnoreCase("delete")){
             log.debug("action: DELETE - " + action);
             int id = Integer.parseInt(request.getParameter("id"));
             dao.deleteRecord(id);
             forward = LIST_USER;
             request.setAttribute("records", dao.getAllRecordsById_event(id_event));  
             EventDao ed = new EventDao();
             request.setAttribute("events", ed.getAllRecords());
         }
 // #########################################################################################        
         else if (action.equalsIgnoreCase("edit")){
             log.debug("action: EDIT - " + action);
             forward = INSERT_OR_EDIT;
             int id = Integer.parseInt(request.getParameter("id"));
             Group record = dao.getRecordById(id);
             request.setAttribute("record", record);
             List<User> listOfGroup_mng = ud.getAllRecordWithRole("group_mng");
             session.setAttribute("listOfGroup_mng", listOfGroup_mng);
         }
 // #########################################################################################        
         else if (action.equalsIgnoreCase("insert")){
             log.debug("action: INSERT - " + action);
             request.removeAttribute("record");
             forward = INSERT_OR_EDIT;
             List<User> listOfGroup_mng = ud.getAllRecordWithRole("group_mng");
             session.setAttribute("listOfGroup_mng", listOfGroup_mng);
         }
 // #########################################################################################        
         else if (action.equalsIgnoreCase("listRecord")){
             log.debug("action: " + action);
             if (systemUser.getRole().equals("admin")){
                 log.debug("admin");
                 forward = LIST_USER;
                 request.setAttribute("records", dao.getAllRecordsById_event(id_event));
                 EventDao ed = new EventDao();
                 request.setAttribute("events", ed.getAllRecords());
             }
             else if (systemUser.getRole().equals("event_mng")){
                 log.debug("event_mng");
                 forward = LIST_USER;
                 request.setAttribute("records", dao.getAllRecordsById_manager(systemUser.getId(), id_event));
                 EventDao ed = new EventDao();
                 request.setAttribute("events", ed.getRecordsById_event_mng(systemUser.getId()));
             }
             else if (systemUser.getRole().equals("group_mng")){
                 forward = LIST_USER;
                 request.setAttribute("records", dao.getAllRecordsById_group_referent(systemUser.getId()));
             }
         } else {
             log.debug("action: ELSE - " + action);
             if (systemUser.getRole().equals("admin")){
                 log.debug("admin");
                 forward = LIST_USER;
                 request.setAttribute("records", dao.getAllRecords());
                 EventDao ed = new EventDao();
                 request.setAttribute("events", ed.getAllRecords());
             }
             else if (systemUser.getRole().equals("event_mng")){
                 log.debug("event_mng");
                 forward = LIST_USER;
                 request.setAttribute("records", dao.getAllRecordsById_manager(systemUser.getId(), id_event));
                 EventDao ed = new EventDao();
                 request.setAttribute("events", ed.getRecordsById_event_mng(systemUser.getId()));
             }
             else if (systemUser.getRole().equals("group_mng")){
                 forward = LIST_USER;
                 request.setAttribute("records", dao.getAllRecordsById_group_referent(systemUser.getId()));
             }
         }
     	
         log.debug("forward: " + forward);
         log.debug("action: " + action);
         
         log.debug("######################");
         log.debug("systemUser.getRole(): " + systemUser.getRole().toString());
         
 		if (systemUser.getRole().equals("admin") 
 				|| systemUser.getRole().equals("event_mng")
 				|| systemUser.getRole().equals("group_mng")){
 	        log.debug("systemUser is an admin");
 		    forward = "/WEB-INF/jsp/private" + forward;
 		}
 		else {
 			forward = UNAUTHORIZED_PAGE;
 		}
         
 		try {
 			getServletConfig().getServletContext().getRequestDispatcher(forward).forward(request, response);
 			} 
 		catch (Exception ex) {
 				ex.printStackTrace();
 			}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	log.debug("START");
     	Group record = new Group();
     	
 		UserDao ud = new UserDao();
 		User  systemUser = ud.getUserByEmail(request.getUserPrincipal().getName());
 		
 		HttpSession session = request.getSession(true);
 		session.removeAttribute("systemUser");
 		session.setAttribute("systemUser",systemUser);
     	
     	int id_event = 0;    	    	
     	log.debug("id_event: " + request.getParameter("id_event"));
     	
     	if (request.getParameter("id_event") != null){
     		id_event = Integer.parseInt(request.getParameter("id_event").toString());
     	}
        log.debug("id_event>>>: " + id_event);
 		record.setId_event(id_event);
     	record.setId_group_referent(Integer.parseInt(request.getParameter("id_group_referent")));
     	record.setName(request.getParameter("name"));
     	record.setMax_group_number(Integer.parseInt(request.getParameter("max_group_number")));
     	record.setBlocked(Boolean.parseBoolean(request.getParameter("blocked")));
         
     	String id = request.getParameter("id");
         
     	log.debug("id: " + id);
    	log.debug("id_event: " + id_event);	
     	
         if(id == null || id.isEmpty()) {
         	log.debug("INSERT");
             dao.addRecord(id_event, record);
             request.setAttribute("id_event", id_event);
         }
         else
         {
         	log.debug("UPDATE");
         	record.setId(Integer.parseInt(id));
             dao.updateRecord(record);
             request.setAttribute("id_event", id_event);
         }
         
 		//Load event list available for user
         EventDao ed = new EventDao();
         if (systemUser.getRole().equals("admin")){
             log.debug("admin");
             request.setAttribute("records", dao.getAllRecords());
             request.setAttribute("events", ed.getAllRecords());
         }
         else if (systemUser.getRole().equals("event_mng")){
             log.debug("event_mng");
            log.debug("id_event: " + id_event);
             request.setAttribute("records", dao.getAllRecordsById_manager(systemUser.getId(), id_event));
             request.setAttribute("events", ed.getRecordsById_event_mng(systemUser.getId()));
         }
         else if (systemUser.getRole().equals("group_mng")){
            log.debug("group_mng");
             request.setAttribute("records", dao.getAllRecordsById_group_referent(systemUser.getId()));
         }
         
         
         
        log.debug(id_event);
         String forward =  "groupList.html?action=listRecord&id_event=" + id_event;
         log.debug("forward: " + forward);
 
         response.sendRedirect(forward);
         
         /*
         String forward = "/WEB-INF/jsp/private" + LIST_USER;
         log.debug("forward: " + forward);
         
 		try {
 			getServletConfig().getServletContext().getRequestDispatcher(forward).forward(request, response);
 			} 
 		catch (Exception ex) {
 				ex.printStackTrace();
 			}
          */
     	log.trace("END");
 	}
 }
