 package com.thoughtworks.twu.controller;
 
import org.junit.Test;
 import org.springframework.web.servlet.ModelAndView;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class DashboardControllerTest {
 
     @Test
     public void shouldShowDashboard(){
         DashboardController dashboardController = new DashboardController();
 
         ModelAndView modelAndView = dashboardController.show();
 
         assertThat(modelAndView.getViewName(), is(dashboardController.DASHBOARD_PAGE));
     }
 }
