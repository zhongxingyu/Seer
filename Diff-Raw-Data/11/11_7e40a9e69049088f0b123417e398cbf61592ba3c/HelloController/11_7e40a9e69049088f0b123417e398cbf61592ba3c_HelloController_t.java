 package com.crowdstore.web.hello;
 
 import com.crowdstore.common.annotations.InjectLogger;
 import com.crowdstore.models.context.AppContext;
 import com.crowdstore.models.role.GlobalRole;
 import com.crowdstore.models.role.StoreRole;
 import com.crowdstore.models.security.GlobalAuthorization;
 import com.crowdstore.models.users.AuthenticatedUser;
 import com.crowdstore.models.users.UserIdentity;
 import com.crowdstore.service.hello.HelloService;
 import com.crowdstore.web.common.annotations.PublicSpace;
 import com.crowdstore.web.common.annotations.RequireGlobalAuthorizations;
 import com.crowdstore.web.common.result.RequestResult;
 import com.crowdstore.web.common.session.SessionHolder;
 import org.slf4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.text.SimpleDateFormat;
import java.util.Arrays;
 import java.util.Date;
 import java.util.Locale;
 
 /**
  * @author fcamblor
  */
 @Controller
 @RequestMapping("/hello")
 public class HelloController {
 
     @InjectLogger
     private Logger LOG;
 
     @Inject
     HelloService helloService;
 
     @Inject
     AppContext appContext;
 
     @PublicSpace
     @RequestMapping(value = "/sayHello/{whom}", method = RequestMethod.GET)
     // @PathVariable stands for url path parameters
     public ModelAndView sayHello(@PathVariable String whom) {
         String sentence = helloService.sayHello(whom);
 
         // When returning a ModelAndView, spring will forward current request to
         // a jsp in WEB-INF/jsp/<view name>.jsp (see app-servlet.xml)
         ModelAndView mav = new ModelAndView("hello/sayHello");
         mav.addObject("sentence", sentence);
         return mav;
     }
 
     @PublicSpace
     @RequestMapping(value = "/calculate", method = RequestMethod.POST)
     public
     @ResponseBody
         // For @RequestBody to be correctly processed here, you will have to :
         // - Provide JSON bean in request body, something like this : { "leftOperand":1,  "rightOperand":3 }
         // - Set Content-Type header to application/json
         // @Validated will call bean validation, ensuring object is valid when we are in method implementation
     Result calculatePost(@RequestBody @Validated Query query) {
         LOG.info("In calculate() ...");
         Result r = new Result();
         r.value = helloService.calculate(Operator.plus.apply(query.leftOperand, query.rightOperand)).getValue();
         return r;
     }
 
     @PublicSpace
     @RequestMapping(value = "/calculate", method = RequestMethod.GET)
     public
     @ResponseBody
         // For a "simple" Object mapping based on query parameters, you will have to call current url
         // with ?leftOperand=1&rightOperand=3, which will be filling the "query" object automatically
     Result calculateGet(@Validated Query query) {
         LOG.info("In calculate() ...");
         Result r = new Result();
         r.value = helloService.calculate(Operator.plus.apply(query.leftOperand, query.rightOperand)).getValue();
         return r;
     }
 
     // TODO: IMPORTANT : Remove this controller impl because it represents a security flaw,
     // opening a session without any credentials
     @PublicSpace
     @RequestMapping(value = "/authenticate/{globalRole}", method = RequestMethod.GET)
     // Exceptionnaly, we're passing HttpServletRequest & Response to a spring method, in order to be able to store
     // authenticated user in the session
     public String authenticate(HttpServletRequest request, HttpServletResponse response, @PathVariable String globalRole) {
         AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                 new UserIdentity(1l).setEmail("foo@bar.com")
                         .setFirstName("Foo bar " + new SimpleDateFormat("HHmmss").format(new Date()))
                         .setLastName("toto")
         ).setLocale(Locale.FRANCE)
                 .setGlobalRole(GlobalRole.valueOf(globalRole))
                .setStoreRoles(
                        Arrays.asList(new AuthenticatedUser.UserStoreRole[]{
                                new AuthenticatedUser.UserStoreRole("4sh store", StoreRole.ADMIN),
                                new AuthenticatedUser.UserStoreRole("4sh soccer5", StoreRole.CUSTOMER)
                        })
                );
 
         // Storing authenticatedUser into the session
         SessionHolder.setAuthenticatedUser(request, response, authenticatedUser);
 
         // Similar to new ModelAndView("hello/authenticationOk")
         return "hello/authenticationOk";
     }
 
     @RequestMapping(value = "authenticatedUser", method = RequestMethod.GET)
     public
     @ResponseBody
     AuthenticatedUser authenticatedUser() {
         return appContext.getAuthenticatedUser();
     }
 
     @RequestMapping(value = "logout", method = RequestMethod.GET)
     public String logout(HttpServletRequest request, HttpServletResponse response) {
         SessionHolder.setAuthenticatedUser(request, response, null);
         return "hello/logoutOk";
     }
 
     /**
      * You should test to authenticate with /authenticate/ADMIN and /authenticate/SIMPLE_USER
      * then test this url to see if, in the first case, authenticated user is allowed, and in the second case,
      * he isn't allowed
      */
     @RequireGlobalAuthorizations(GlobalAuthorization.CREATE_USER)
     @RequestMapping(value = "testAuthorizations", method = RequestMethod.GET)
     public
     @ResponseBody
     RequestResult testAuthorizations() {
         return RequestResult.ok;
     }
 }
