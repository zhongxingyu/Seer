 package sample.controllers;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpSession;
 import java.util.Map;
 
 @Controller
 public class UserController {
     public final SimpleJdbcTemplate db;
 
     @Autowired
     public UserController(SimpleJdbcTemplate db) {this.db = db;}
 
     @RequestMapping(value = "/", method = RequestMethod.GET)
     public String loginForm() {
         return "index";
     }
 
     @RequestMapping(value = "/login", method = RequestMethod.POST)
     public ModelAndView login(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               HttpSession session) {
 
         ModelAndView mv = new ModelAndView("/index");
         long userID;
         try {
             Map<String, Object> userData = db.queryForMap("select id, email, password from users where email=?",
                                                           email);
 
             // add md5 function for password check
             if (!userData.get("password").equals(password)) {
                 mv.addObject("message", "Invalid password.");
                 return mv;
             }
             userID = (Integer) userData.get("id");
             session.setAttribute("email", email);
             session.setAttribute("userID", userID);
         } catch (EmptyResultDataAccessException e) {
             mv.addObject("message", "No such user exists!");
         }
        mv.setViewName("redirect:twimini/posts.json");
         return mv;
     }
 
     @RequestMapping(value = "/logout")
     public String logout(HttpSession session) {
         session.invalidate();
         return "redirect:/";
     }
 }
