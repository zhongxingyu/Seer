 package com.thoughtworks.twu.controller;
 
 import org.junit.Test;
 import org.springframework.web.servlet.ModelAndView;
 
 import static org.testng.Assert.*;
 
 public class FavoriteTimesheetControllerTest {
 
 
     @Test
     public void shouldBeAbleToGetViewNameOfController() throws Exception{
        FavoriteTimesheetController controller = new FavoriteTimesheetController();
       ModelAndView modelAndView = new ModelAndView("ui/timesheet/favorite/new_form");
       assertEquals("ui/timesheet/favorite/new_form", modelAndView.getViewName());
     }
 
    @Test
 
 }
