 package com.clrvynt.controller;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.clrvynt.domain.User;
 import com.clrvynt.domain.UserAttachment;
 import com.clrvynt.dto.LoginDto;
 import com.clrvynt.dto.ResponseDto;
 import com.clrvynt.dto.SaveUserDto;
 import com.clrvynt.service.AppService;
 
 @Controller
 public class AppController {
 
     @Autowired
     private AppService appService;
 
     /* *********************************************************************
      * This is a conventional login method. Once the user is logged in, it is
      * your responsibility to devise a strategy to store this loggedInUser with
      * a token somewhere. This could be session or cache. For a distributed
      * environment that needs horizontal scaling, a cache such as Memcache would
      * be ideal. This example uses the session.
      * *********************************************************************
      */
     @RequestMapping(value = "/login.do", method = RequestMethod.POST)
     public String login(@Valid @ModelAttribute LoginDto loginDto, BindingResult result, Model model, HttpSession session) {
 
 	if (result.hasErrors()) {
 	    model.addAttribute("errors", translateErrorsIntoMap(result.getFieldErrors()));
 	    return "index";
 	}
 
 	try {
 	    logger.log(Level.INFO, "Received login call for email " + loginDto.getEmail() + " at " + new Date());
 	    User user = appService.login(loginDto);
 
 	    // Create a session here to indicate successful login.
 	    session.setAttribute("loggedInUser", user);
 
 	} catch (Exception e) {
 	    ExceptionUtils.printRootCauseStackTrace(e);
 	    model.addAttribute("errorMessage", e.getMessage());
 	    return "index";
 	}
 
 	return "redirect:/auth/home.html";
     }
 
     /* *********************************************************************
      * This is a call to the default welcome-file from web.xml. In this call, we
      * should check if the user is logged in and redirect him to his logged in
      * state. This could happen if the user logs in and then navigates to the
      * root page.
      * *********************************************************************
      */
     @RequestMapping(value = "/home.html", method = RequestMethod.GET)
     public String goHome(HttpSession session) {
 	if (session.getAttribute("loggedInUser") != null)
 	    return "redirect:/auth/home.html";
 
 	return "index";
     }
 
     /* *********************************************************************
      * This method demonstrates the ability to download files via the Spring MVC
      * framework. Having access to the response, we are simply able to write to
      * the response outputstream.
      * *********************************************************************
      */
     @RequestMapping(value = "/auth/getAttachment.do", method = RequestMethod.GET)
     public void getImage(@RequestParam String attachmentId, HttpServletResponse response) {
 	try {
 	    logger.log(Level.INFO, "Received call for getAttachment with id " + attachmentId + " at " + new Date());
 	    UserAttachment attachment = appService.getAttachmentById(attachmentId);
 	    InputStream is = new ByteArrayInputStream(attachment.getAttachmentBytes());
 	    response.setContentType(attachment.getContentType());
 	    IOUtils.copy(is, response.getOutputStream());
 	    response.flushBuffer();
 
 	} catch (Exception e) {
 	    ExceptionUtils.printRootCauseStackTrace(e);
 	}
     }
 
     /* *********************************************************************
      * A POST method that updates an existing entity. It also includes a file
      * upload feature. The DTO has annotations and this takes care of all
      * validations. It also includes a custom validator for a file upload to
      * restrict the size of the file to a certain limit.
      * *********************************************************************
      * Another important feature illustrated here is the Post/Redirect/Get and
      * using redirectAttrs to add a FlashAttribute and send to the home page.
      * This way, the successMessage will be displayed when a save successfully
      * happens but wont be displayed if the user clicks refresh on that page.
      * *********************************************************************
      */
     @RequestMapping(value = "/auth/saveUser.do", method = RequestMethod.POST)
     public String saveUser(@Valid @ModelAttribute SaveUserDto saveUserDto, BindingResult result, Model model,
 	    HttpSession session, final RedirectAttributes redirectAttrs) {
 
 	if (result.hasErrors()) {
 	    model.addAttribute("userDto", saveUserDto);
 	    model.addAttribute("errors", translateErrorsIntoMap(result.getFieldErrors()));
 	    return "/auth/home";
 	}
 
 	try {
 	    logger.log(Level.INFO, "Received login call for saveUser with email " + saveUserDto.getEmail() + " at "
 		    + new Date());
 	    User user = appService.saveUser(saveUserDto);
 
 	    // Update session / cache etc.
 	    session.setAttribute("loggedInUser", user);
 	    redirectAttrs.addFlashAttribute("successMessage", "User saved");
 
 	} catch (Exception e) {
 	    ExceptionUtils.printRootCauseStackTrace(e);
 	    model.addAttribute("userDto", saveUserDto);
 	    model.addAttribute("errorMessage", e.getMessage());
 	    return "/auth/home";
 	}
 
 	return "redirect:/auth/home.html";
     }
 
     /* *********************************************************************
      * The home page call. Remember, the loggedInUser is already in session and
      * home.jsp simply displays its details. If you are using cache instead,
      * remember to place the loggedInUser into the Model
      * *********************************************************************
      */
     @RequestMapping(value = "/auth/home.html", method = RequestMethod.GET)
     public String goAuthorizedHome(Model model, HttpSession session) {
 	model.addAttribute("userDto", session.getAttribute("loggedInUser"));
 	return "auth/home";
     }
 
     /* *********************************************************************
      * This is an example of a JSON response using @ResponseBody. Instead of
      * returning a view name from the method, we simply annotate it with
      * @ResponseBody and return a POJO. This will automatically get converted to
      * JSON. Make sure to include jackson* jars in your POM
      * *********************************************************************
      */
     @RequestMapping(value = "/auth/getAllUsers.do", method = RequestMethod.GET)
     public @ResponseBody
    ResponseDto getRecentPress() {
 	ResponseDto response = new ResponseDto();
 	try {
 	    // Get recent press
 	    List<User> allUsers = appService.getAllUsers();
 	    response.setUsers(allUsers);
 	} catch (Exception e) {
 	    ExceptionUtils.printRootCauseStackTrace(e);
 	    response.setActionFailed(true);
 	    response.setErrorMessage(e.getMessage());
 	}
 
 	return response;
     }
 
     /* *********************************************************************
      * A simple demonstration of a scheduler that also gets set up when this
      * controller gets loaded. This scheduler simply runs every 2 minutes and
      * logs the message below. The Scheduled annotation also supports cron type
      * values which lets you configure more complex scenarios.
      * *********************************************************************
      */
     @Scheduled(fixedRate = 120000)
     public void pingService() {
 	logger.log(Level.INFO, "#### PING SERVICE AT " + new Date() + " ####");
     }
 
     /* *********************************************************************
      * This is an example of a long-running process triggered by user action. It
      * uses Spring's Async annotation to run the process via Executor threads.
      * You can combine this alongwith long-polling from the client side to even
      * update status if necessary. This example simply triggers the call and
      * moves on.
      * *********************************************************************
      */
     @RequestMapping(value = "/auth/triggerLongProcess.do", method = RequestMethod.GET)
     public String triggerLongProcess(Model model, HttpSession session, final RedirectAttributes redirectAttrs) {
 	try {
 	    logger.log(Level.INFO, "Received call to trigger long running process");
 	    appService.runLongProcess();
 	    redirectAttrs.addFlashAttribute("successMessage", "Process triggered");
 	} catch (Exception e) {
 	    ExceptionUtils.printRootCauseStackTrace(e);
 	    redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
 	}
 
 	return "redirect:/auth/home.html";
     }
 
     /* *********************************************************************
      * Logout method. In this case, we are invalidating the session, but if you
      * were using a cache, expire the cache item and redirect the user to the
      * logged out page (or the login page)
      * *********************************************************************
      */
     @RequestMapping(value = "/auth/logout.html", method = RequestMethod.GET)
     public String logout(HttpServletRequest request) {
 	request.getSession().invalidate();
 	return "redirect:/";
     }
 
     /* *********************************************************************
      * A convenience method to take a list of fieldErrors returned as part of
      * validation and converting it into a Map of field and error messages.This
      * makes it easier to access in the jsp.
      * *********************************************************************
      */
     private Map<String, String> translateErrorsIntoMap(List<FieldError> fieldErrors) {
 	Map<String, String> returnMap = new HashMap<String, String>();
 	for (FieldError error : fieldErrors) {
 	    returnMap.put(error.getField(), error.getDefaultMessage());
 	}
 	return returnMap;
     }
 
     private static Logger logger = Logger.getLogger(AppController.class);
 
 }
