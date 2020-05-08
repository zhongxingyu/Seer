 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package CalmPortal.web;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import org.springframework.ui.ModelMap;
 
 /**
  *
  * @author Jinn
  */
 public abstract class BaseController{
     protected String imagePhysPath;
     protected String avaPhyPath;
     
     public void RequestInit(HttpServletRequest request)
     {
         this.imagePhysPath = request.getSession().getServletContext().getRealPath("/images");
         this.avaPhyPath = request.getSession().getServletContext().getRealPath("/images/ava");
     }
 
     public boolean CheckAuthentication(HttpServletRequest request, ModelMap modelmap)
     {
         try
         {
             HttpSession session = request.getSession();
 
             modelmap.addAttribute("islogged", false);
             OnLoad(request, modelmap);
             return true;
         }catch(Exception ex)
         {
             modelmap.addAttribute("message", "An error has been found!");
         }
         return false;
     }
 
     //public User GetCurrentUser(HttpServletRequest request)
     //{
     //    HttpSession session = request.getSession();
     //    return (User)session.getAttribute("user");
     //}
 
     //should be filter or soemthing (equivalent from .net)
     public void OnLoad(HttpServletRequest request, ModelMap modelmap)
     {
        
         modelmap.addAttribute("message", "Hello");
     }
 
 }
