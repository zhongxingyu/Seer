 package serverMonitoring.controller.employeePages;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 import serverMonitoring.logic.service.EmployeeService;
 
 /**
  * Handles and retrieves /WEB-INF/ftl/employee/serv_details.ftl
  */
 @Controller
 @Secured("ROLE_USER")
 @RequestMapping(value = "/employee/serv_details")
 public class ServerDetailsController extends AbstractEmployeeController {
 
     protected static Logger logger = Logger.getLogger(ServerDetailsController.class);
 
     @Autowired
     private EmployeeService employeeService;
 
     /**
     * server detail page with provided name of server
      */
    @RequestMapping(value = "/{serverName}")
    public ModelAndView loadPage(@PathVariable("serverName") String serverName) {
         showRequestLog("serv_details");
         return new ModelAndView("employee/serv_details",
                "server", employeeService.getServerByName(serverName));
     }
 }
