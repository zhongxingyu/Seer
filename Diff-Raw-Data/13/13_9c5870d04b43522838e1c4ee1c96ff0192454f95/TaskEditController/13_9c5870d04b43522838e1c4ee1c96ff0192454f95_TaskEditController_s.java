 package knowledgeTest.controller.adminPages.task;
 
 import knowledgeTest.controller.adminPages.AdminAbstractController;
 import knowledgeTest.logic.service.AdminService;
 import knowledgeTest.logic.service.UserService;
 import knowledgeTest.model.Task;
 import knowledgeTest.web.validation.TaskValidation;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Handles and retrieves admin page responsible for edit/ creation of Task
  * User must be authenticated as Admin to see this page
  */
 @Controller
 @Secured("ROLE_ADMIN")
 @RequestMapping("/admin/edit_task/{id}")
 public class TaskEditController extends AdminAbstractController {
 
     protected static Logger logger = Logger.getLogger(TaskEditController.class);
 
     private TaskValidation validation;
 
     @Autowired
     public void setValidation(TaskValidation validation) {
         this.validation = validation;
     }
 
     @Autowired
     private AdminService adminService;
 
     @Autowired
     private UserService userService;
 
     /**
      * Handles and retrieves /WEB-INF/jsp/content/admin/task/edit_task.jsp
      *
      * @return ModelAndView object
      */
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView getTaskEditPage(@PathVariable("id") Long id) {
         logger.info("adminHome.jsp");
 
         ModelAndView model = new ModelAndView("edit_task");
         model.addObject("task", userService.findTaskById(id));
         return model;
     }
 
     /**
      * Handles and retrieves /WEB-INF/jsp/content/admin/task/edit_task.jsp
      *
      * @return ModelAndView object
      */
     @RequestMapping(method = RequestMethod.POST)
     public ModelAndView onTaskEditsubmit(@PathVariable("id") Long id,
                                          @ModelAttribute("task")Task task, BindingResult result,
                                          SessionStatus status, HttpServletRequest request) {
 
         logger.info("adminHome.jsp");

         validation.validate(task, result);
 
         if (!result.hasErrors()) {
             adminService.updateTask(task);
             return new ModelAndView("redirect:/admin/taskManager");
         } else {
             return new ModelAndView("edit_task", "task", task);
         }
     }
 }
