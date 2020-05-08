 package org.jichigo.sample;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 
     private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 
    private AtomicLong counter = new AtomicLong(1);

     /**
      * Simply selects the home view to render by returning its name.
      */
     @RequestMapping(value = "/", method = RequestMethod.GET)
     public String home(Locale locale, Model model) {
         logger.info("Welcome home! the client locale is " + locale.toString());
 
         Date date = new Date();
         DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
 
         String formattedDate = dateFormat.format(date);
 
         model.addAttribute("serverTime", formattedDate);
 
        if (counter.getAndIncrement() % 2 == 0) {
             throw new IllegalArgumentException("hoge");
         }

         return "home";
     }
 
     @ExceptionHandler({ IllegalArgumentException.class, NullPointerException.class })
    public String handleRuntimeException(RuntimeException e, HttpServletRequest req, HttpServletResponse res) {
         return "home";
     }
 
 }
