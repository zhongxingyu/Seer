 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.trazability.rest.identity;
 
 /*
  * #%L
  * Web Archetype
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
 
 import com.abada.springframework.security.authentication.dni.DniAuthenticationDao;
 import com.abada.springframework.web.servlet.view.JsonView;
 import com.abada.trazability.entity.view.Views;
 import java.security.Principal;
 import javax.annotation.Resource;
 import javax.annotation.security.RolesAllowed;
 import javax.servlet.http.HttpServletRequest;
 import org.springframework.context.ApplicationContext;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  *
  * @author katsu
  */
 @Controller
 public class IdentityController {
 
     @Resource
     private ApplicationContext context;
     
     /**
      * Returns all roles of logged users.
      *
      * @param request Do nothing.
      * @return Return all roles of logged users.
      */
     @RequestMapping(value = "/roles/list", method = RequestMethod.GET)
     public void getRoles(HttpServletRequest request, Model model) {
         Principal p = request.getUserPrincipal();
         if (p instanceof UsernamePasswordAuthenticationToken) {
             UsernamePasswordAuthenticationToken u = (UsernamePasswordAuthenticationToken) p;
             if (u.getPrincipal() instanceof UserDetails) {
                 UserDetails user = (UserDetails) u.getPrincipal();
                 model.addAttribute(JsonView.JSON_VIEW_RESULT, user.getAuthorities());
                 model.addAttribute(JsonView.JSON_VIEW_CLASS, Views.Public.class);
             }
         }
     }
 
     /**
      * Returns all roles for a user by dni
      *
      * @param dni Dni
      * @return Returns all roles for a user by dni
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_DNI_CONSULT"})
     @RequestMapping(value = "/dni/roles/list", method = RequestMethod.GET)
     public void getRolesByDni(String dni, Model model) throws Exception {
         DniAuthenticationDao dniAuthenticationDao = context.getBean(DniAuthenticationDao.class);
         if (dniAuthenticationDao != null) {
             UserDetails userDetails = dniAuthenticationDao.getUserByDNI(dni);
             if (userDetails != null) {
                 model.addAttribute(JsonView.JSON_VIEW_RESULT, userDetails.getAuthorities());
                 model.addAttribute(JsonView.JSON_VIEW_CLASS, Views.Public.class);
             } else {
                 throw new Exception("User with dni " + dni + " not found.");
             }
         } else {
             throw new Exception("No DniAuthenticationDao bean found.");
         }
     }
 }
