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
 import org.hamcrest.Matcher;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasItems;
 import static org.hamcrest.Matchers.nullValue;
 import static org.hamcrest.core.Is.is;
 import static org.mockito.Mockito.when;
 
 /**
  * @author: Carlos Fernandez
  *
  * @date: 02 Aug 2010
  *
  * Unit tests for @link{SelectComponentsController}
  */
 
 @RunWith(MockitoJUnitRunner.class)
 public class SelectComponentsControllerTest {
 
     SelectComponentsController controller;
 
     @Mock
     HttpServletRequest request;
     @Mock
     HttpServletResponse response;
     @Mock
     private ComponentDAO componentDAO;
 
     @Before
     public void setUp() {
         controller = new SelectComponentsController(componentDAO);
         when(request.getMethod()).thenReturn("POST");
     }
 
     @Test
    public void shouldForwardToNewDocumentPageWithComponentsAndFamilyInModel() throws Exception {
         //Given
         controller.setViewName("selectComponents");
 
         String componentId1 = "componentId1";
         Component component1 = createComponent(componentId1);
         String componentId2 = "componentId2";
         Component component2 = createComponent(componentId2);
 
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put("componentId1", new String[]{"valueComponentId1"});
         parameters.put("componentId2", new String[]{"valueComponentId2"});
        parameters.put("familyName", new String[]{"familyNameValue"});
 
         when(request.getParameterMap()).thenReturn(parameters);
        when(request.getParameter("familyName")).thenReturn("familyNameValue");
         when(componentDAO.getById(Component.class, componentId1)).thenReturn(component1);
         when(componentDAO.getById(Component.class, componentId2)).thenReturn(component2);
 
         //When
         ModelAndView modelAndView = controller.handleRequest(request, response);
 
         //Then
         assertThat("The view name is wrong", modelAndView.getViewName(), is("selectComponents"));
         assertThat("The size of the list is wrong", ((List<Component>) modelAndView.getModelMap().get("components")).size(), is(2));
         assertThat("The list of components is wrong", (List<Component>) modelAndView.getModelMap().get("components"),
                 hasItems(component1, component2));
        assertThat("The family name is wrong", (String) modelAndView.getModelMap().get("familyName"), is("familyNameValue"));
     }
 
     @Test
     public void shouldAddDocumentIdToModelIfItIsPresentInRequest() throws Exception {
         //Given
         Long documentId = 123L;
         when(request.getParameter("documentId")).thenReturn(documentId.toString());
 
         //When
         ModelAndView modelAndView = controller.handleRequest(request, response);
 
         //Then
         assertThat("The document id is wrong", (Long) modelAndView.getModel().get("documentId"), is(documentId));
     }
 
     @Test
     public void shouldNotAddDocumentIdToModelIfItIsPresentInRequestButItIsNotNumeric() throws Exception {
         //Given
         String documentId = "123a";
         when(request.getParameter("documentId")).thenReturn(documentId);
 
         //When
         ModelAndView modelAndView = controller.handleRequest(request, response);
 
         //Then
         assertThat("The document id is wrong", modelAndView.getModel().get("documentId"), is(nullValue()));
     }
 
     private Component createComponent(String componentId) {
         Component component1 = new Component();
         component1.setCode(componentId);
         return component1;
     }
 
 }
