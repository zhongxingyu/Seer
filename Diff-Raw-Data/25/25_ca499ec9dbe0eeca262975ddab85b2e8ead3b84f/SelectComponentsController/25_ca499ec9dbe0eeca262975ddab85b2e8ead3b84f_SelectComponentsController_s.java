 /**
  * Fonti is a web application for billing and budgeting
  * Copyright (C) 2009  Carlos Fernandez
  *
  * This file is part of Fonti.
  *
  * Fonti is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Fonti is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.carlos.projects.billing.ui.controllers;
 
 import com.carlos.projects.billing.dao.ComponentDAO;
 import com.carlos.projects.billing.domain.Component;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.ParameterizableViewController;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static org.apache.commons.lang.StringUtils.isEmpty;
 import static org.apache.commons.lang.StringUtils.isNumeric;
 
 /**
  * @author: Carlos Fernandez
  * @date: 02 Aug 2010
  */
 public class SelectComponentsController extends ParameterizableViewController {
 
     private ComponentDAO componentDAO;
 
     public SelectComponentsController() {
         super();
     }
 
     public SelectComponentsController(ComponentDAO componentDAO) {
         this.componentDAO = componentDAO;
     }
 
     public ComponentDAO getComponentDAO() {
         return componentDAO;
     }
 
     public void setComponentDAO(ComponentDAO componentDAO) {
         this.componentDAO = componentDAO;
     }
 
     @Override
     protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                  HttpServletResponse response) throws Exception {
         ModelAndView mav = super.handleRequestInternal(request, response);
         mav.getModelMap().addAttribute("components",
                 getComponents((Map<String, String[]>) request.getParameterMap()));
         mav.getModelMap().addAttribute("familyName", request.getParameter("familyName"));
 
         String documentId = request.getParameter("documentId");
         if (!isEmpty(documentId) && isNumeric(documentId)) {
             mav.getModelMap().addAttribute("documentId", Long.parseLong(documentId));
         }
         return mav;
     }
 
     private List<Component> getComponents(Map<String, String[]> requestParameters) {
         List<Component> components = new ArrayList<Component>();
         for (String key : requestParameters.keySet()) {
            if (!"familyName".equalsIgnoreCase(key)) {
                 components.add(componentDAO.getById(Component.class, key));
             }
         }
         return components;
     }
 
 }
