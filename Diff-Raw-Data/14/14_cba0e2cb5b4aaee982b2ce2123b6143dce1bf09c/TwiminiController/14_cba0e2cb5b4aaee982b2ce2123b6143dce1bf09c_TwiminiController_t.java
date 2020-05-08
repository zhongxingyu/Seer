 package sample.controllers;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 import sample.model.Post;
 import sample.service.TwiminiStore;
 
 import java.util.Hashtable;
 import java.util.List;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: dushyant
  * Date: 18/7/12
  * Time: 1:50 PM
  * To change this template use File | Settings | File Templates.
  */
 
 @Controller
@RequestMapping("/twimini")
 public class TwiminiController {
     TwiminiStore tStore;
 
     @Autowired
     public TwiminiController(TwiminiStore tStore){
         this.tStore = tStore;
     }
 
     @RequestMapping("/index")
     ModelAndView indexPage(){
         ModelAndView mv = new ModelAndView("index");
         return mv;
     }
 
     @RequestMapping(value = "/register", method = RequestMethod.POST)
     @ResponseBody
     Hashtable<String, String> registerJson(@RequestParam String name, @RequestParam String email, @RequestParam String password){
         tStore.addUser(name, email, password);
         Hashtable hs = new Hashtable<String, String>();
         hs.put("status","success");
         return hs;
     }
 
     @RequestMapping(value = "/newpost.json", method = RequestMethod.POST)
     @ResponseBody
     Hashtable<String, String> newPostJson(@RequestParam String post){
         Hashtable hs = new Hashtable<String, String>();
         tStore.addPost(post);
         hs.put("status","success");
         return hs;
     }
 
     @RequestMapping(value = "/newfollower.json", method = RequestMethod.POST)
     @ResponseBody
     Hashtable<String, String> newFollowerJson(@RequestParam int following){
         Hashtable hs = new Hashtable<String, String>();
         tStore.addFollower(following);
         hs.put("status","success");
         return hs;
     }
 
     @RequestMapping(value = "/posts.json", method = RequestMethod.GET)
     @ResponseBody
     List<Post> getPostsJson(){
         return tStore.getPosts();
     }
 }
