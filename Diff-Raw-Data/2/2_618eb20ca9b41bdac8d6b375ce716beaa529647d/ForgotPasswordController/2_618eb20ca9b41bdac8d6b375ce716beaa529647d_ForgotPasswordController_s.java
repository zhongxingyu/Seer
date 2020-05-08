 package com.ubs.opi.web.controller.login;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.ubs.opi.domain.Individual;
 import com.ubs.opi.domain.KeyValuePair;
 import com.ubs.opi.web.AbstractOpiForm;
 
 /**
  * Controller to handle the login view.
  *
  * @author Ana Velez
  */
 public class ForgotPasswordController extends AbstractOpiForm {
   //~ Methods ------------------------------------------------------------------
 
    public ForgotPasswordController() {
       setCommandName("keyvaluepair");
       // need a session to hold the formBackingObject
       setSessionForm(true);
    }
 
    @Override
   protected Object formBackingObject(HttpServletRequest request)
                               throws Exception {
     logger.debug("formBackingObject ...");
 
     return new KeyValuePair();
   }
 
 
   /** Method inserts a new <code>Advisor</code>. */
   protected ModelAndView onSubmit(
         HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
         throws Exception {
 
      String key = ServletRequestUtils.getStringParameter(request, "key");
      boolean required = false;
      if (key == null || !StringUtils.hasText(key.toString())) {
         required = true;
         errors.rejectValue("key", null, "Must Select one of Username or Email");        
      }
      
      String value = ServletRequestUtils.getStringParameter(request, "value");
      if (value == null || !StringUtils.hasText(value.toString())) {
         required = true;
         errors.rejectValue("value", null, "Must Supply a search Value");        
      }
 
      if (required)
         return showForm(request, response, errors);
 
      Individual individual = this.getOpi().findIndividual(key, value);
      if (individual != null) {
         getOpi().myMailManager().resendUserNameandPassword(individual);
         logger.info("forwarding to "+this.getSuccessView());
         return new ModelAndView(getSuccessView());
      } else {     
         request.setAttribute("login_error", "error");
         errors.rejectValue("value", null, "Failed to find user with those details");
         return showForm(request, response, errors);
      }
 
   }
 
   protected ModelAndView handleInvalidSubmit(HttpServletRequest request, HttpServletResponse response)
         throws Exception {
      return disallowDuplicateFormSubmission(request, response);
   }
 
   //~ Static fields/initializers -----------------------------------------------
 
   private static final Log logger = LogFactory.getLog(ForgotPasswordController.class);
 }
