 package net.devmask.tuit;
 
 import net.devmask.tuit.models.Tuit;
 import net.devmask.tuit.models.User;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import javax.validation.Valid;
 import java.util.List;
 
 /**
  * @author <a href="mailto:jonathan@devmask.net"> Jonathan Garay </a>
  *         07/02/12 Creado
  */
 
 @Controller
 @RequestMapping("/user")
 public class UsersController extends BaseController{
     
     @Autowired
     private TuitSession tuitSession;
     
     @RequestMapping(method = RequestMethod.GET)
     public String newUser(Model model){
         model.addAttribute(new User());
         return "accounts/new";
     }
 
 
     @RequestMapping(method = RequestMethod.POST)
     public String create(@Valid User user, BindingResult result,RedirectAttributes redirectAttributes){
         if(result.hasErrors()){ return "accounts/new"; }

         persist(user);
        redirectAttributes.addAttribute("username",user.getUsername());
         tuitSession.login(user);
 
         return "redirect:/user/dashboard";
     }
 
     @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
     public String dashboard(Model model){
         model.addAttribute(new Tuit());
         model.addAttribute("timeline",getTuits());
         return "users/dashboard";
     }
 
 
 
     @RequestMapping(value = "/following",method = RequestMethod.GET)
     public @ResponseBody List<User> following(){
         return tuitSession.getCurrentUser().getFollowing();
     }
 
 
     @RequestMapping(value = "{username}/follow",method = RequestMethod.GET)
     public @ResponseBody List<User> toFollow(@RequestParam("username") String username){
         return tuitSession.getCurrentUser().getFollowing();
     }
 
 
     @RequestMapping(value = "/tuit", method = RequestMethod.POST)
     public @ResponseBody Tuit newTuit(@Valid Tuit tuit, BindingResult result, Model model){
         tuit.setUser(tuitSession.getCurrentUser());
         persist(tuit);
         return tuit;
     }
 
     @RequestMapping(value = "/tuits", method = RequestMethod.GET)
     public @ResponseBody List<Tuit> tuits(Model model){
         return getTuits();
     }
 
     protected List<Tuit> getTuits(){
         return getEntityManager().createNamedQuery("user.timeline")
                 .setParameter("user",tuitSession.getCurrentUser())
                 .getResultList();
     }
 }
