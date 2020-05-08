 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.manager;
 
 import com.abada.springframework.web.servlet.menu.MenuEntry;
 import java.util.Arrays;
 import javax.annotation.security.RolesAllowed;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.ui.Model;
 
 /**
  *
  * @author david
  */
 @Controller
 public class RoleController {
 
     /**
      * js role load
      *
      * @param model
      * @return
      */
     @RequestMapping(value = "/manager/role.htm")
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_ADMINISTRATIVE"})
    @MenuEntry(icon = "manager/image/rol.png", menuGroup = "Manager", order = 2, text = "Role")
     public String gridrole(Model model) {
         model.addAttribute("js", Arrays.asList("manager/js/common/gridrole.js", "manager/js/common/gridroleexpander.js", 
                 "manager/js/role.js", "manager/js/common/griduser.js","manager/js/manager-utils.js"));
         return "dynamic/main";
     }
 }
