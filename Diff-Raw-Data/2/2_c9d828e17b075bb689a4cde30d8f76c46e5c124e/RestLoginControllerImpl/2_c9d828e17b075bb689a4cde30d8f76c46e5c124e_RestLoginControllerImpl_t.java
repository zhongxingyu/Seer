 package no.niths.application.rest.auth;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import no.niths.application.rest.RESTConstants;
 import no.niths.application.rest.auth.interfaces.RestLoginController;
 import no.niths.application.rest.exception.UnvalidEmailException;
 import no.niths.common.constants.AdminConstantNames;
 import no.niths.common.constants.SecurityConstants;
 import no.niths.common.helpers.LazyFixer;
 import no.niths.domain.school.Student;
 import no.niths.security.SessionToken;
 import no.niths.services.auth.interfaces.AuthenticationService;
 
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.client.HttpClientErrorException;
 
 /**
  * 
  * Login controller for the API
  * <p>
  * To log in, a access token from Google must be provided
  * </p>
  * 
  */
 @Controller
 @RequestMapping(AdminConstantNames.AUTH)
 public class RestLoginControllerImpl implements RestLoginController{
     
     Logger logger = org.slf4j.LoggerFactory
             .getLogger(RestLoginControllerImpl.class);
 
     @Autowired
     private AuthenticationService service;
 
     /**
      * Authorize the user. Use the returned session token for future requests
      * 
      * @param token The token issued from google
      * @remove: @return encrypted session token valid for
      *   (See AppNames.SESSION_VALID_TIME)
      * 
      */
     @Override
     @RequestMapping(
             value   = { "login" },
             method  = RequestMethod.POST,
             headers = RESTConstants.ACCEPT_HEADER)
     @ResponseBody
     public Student login(
             @RequestBody SessionToken token, HttpServletRequest req,
             HttpServletResponse res) {
         Student authenticatedStudent = null;
 
         if (token != null) {
             logger.debug(
                     "A user wants to be authenticated with token: " + token);
 
             SessionParcel sessionParcel =
                     service.authenticateAtGoogle(token.getToken());
             SessionToken newToken = sessionParcel.getSessionToken();
             res.setHeader("session-token", newToken.getToken());
             res.setHeader("student-id", newToken.getStudentId() + "");
             logger.debug("Authentication success");
 
             authenticatedStudent = sessionParcel.getAuthenticatedStudent();
             LazyFixer<Student> fix = new LazyFixer<Student>();
             fix.clearSubRelations(authenticatedStudent);
         }
 
         return authenticatedStudent;
     }
     
     /**
      * Logs out the student
     * @param studentId id of the student to log out
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR +
             " or (hasRole('ROLE_STUDENT') and principal.studentId == #studentId)")
     @RequestMapping(
     		value   = { "logout/{studentId}" },
     		method  = RequestMethod.POST,
     		headers = RESTConstants.ACCEPT_HEADER)
     @ResponseStatus(value = HttpStatus.OK, reason = "Student logged out")
     public void logout(@PathVariable Long studentId) {
     	service.logout(studentId);	
     }
     
     
     
     @ExceptionHandler(UnvalidEmailException.class)
     @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
     public void notAuthorized(HttpServletResponse res) {
         res.setHeader("error", "Email not valid");
     }
     
     @ExceptionHandler(HttpClientErrorException.class)
     @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
     public void notAuthorized(HttpClientErrorException e, HttpServletResponse res) {
         res.setHeader("error", "Token not valid");
     }
 
 }
