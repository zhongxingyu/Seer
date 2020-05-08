 package serverMonitoring.controller.employeePages;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 import serverMonitoring.logic.service.AdminService;
 import serverMonitoring.logic.service.EmployeeService;
 import serverMonitoring.model.ServerEntity;
 
 import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 import java.util.Collection;
 
 /**
  * Handles and retrieves /WEB-INF/ftl/employee/monitoring.ftl
  */
 @Controller
 @Secured("ROLE_USER")
 @RequestMapping(value = "/employee/monitoring")
 public class ServerMonitoringController extends AbstractEmployeeController {
 
     protected static Logger logger = Logger.getLogger(ServerMonitoringController.class);
 
     @Autowired
     private EmployeeService employeeService;
 
     @Autowired
     private AdminService adminService;
 
     /**
      * @return list of servers available for user with provided id
      *         - show full list of servers for user with admin role
      */
     @RequestMapping(method = RequestMethod.GET)
    public ModelAndView loadPage(HttpServletRequest request, HttpServletResponse response) {
 
         showRequestLog("monitoring");
 
         /**
          * redirecting by role
          */
         if (isUserIsAdmin(request)) {
             return new ModelAndView("employee/monitoring", "availableServers",
                     adminService.getAllServers());
         } else {
             Collection<ServerEntity> resultsList = employeeService.getServerListById(getUserId());
 
             /**
              * redirecting by result size
              */
             if (resultsList.size() > 1) {
                 // multiple result
                 return new ModelAndView("employee/monitoring", "availableServers", resultsList);
             } else {
                 // one result
                 return new ModelAndView("employee/serv_details", "targetServer", resultsList);
             }
         }
     }
 }
