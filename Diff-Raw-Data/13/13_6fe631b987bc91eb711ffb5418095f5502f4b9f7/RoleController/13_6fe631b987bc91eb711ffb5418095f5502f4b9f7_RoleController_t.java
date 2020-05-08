 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.manager;
 
 /*
  * #%L
  * Cleia
  * %%
  * Copyright (C) 2013 Abada Servicios Desarrollo (investigacion@abadasoft.com)
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
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
     @MenuEntry(icon = "manager/image/rol.png", menuGroup = "Manager", order = 2, text = "Roles")
     public String gridrole(Model model) {
        model.addAttribute("js", Arrays.asList("manager/js/common/gridrole.js","manager/js/role.js", 
                "manager/js/common/griduser.js","manager/js/manager-utils.js"));
         return "dynamic/main";
     }
 }
