 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.manager;
 
 import com.abada.springframework.web.servlet.menu.MenuEntry;
 import java.util.Arrays;
 import javax.annotation.security.RolesAllowed;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 /**
  *
  * @author mmartin
  */
 @Controller
 public class IdtypeController {
 
     /**
      * js idtype load
      *
      * @param model
      * @return
      */
     @RequestMapping(value = "/patient/idtype.htm")
    @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_ADMINISTRATIVE"})
     @MenuEntry(icon = "manager/image/identificacion.png", menuGroup = "Manager", order = 0, text = "Identificación Usuarios")
     public String gridIdtype(Model model) {
         model.addAttribute("js", Arrays.asList("manager/js/common/gridIdtype.js", "manager/js/Idtype.js","manager/js/manager-utils.js"));
         return "dynamic/main";
     }
 }
