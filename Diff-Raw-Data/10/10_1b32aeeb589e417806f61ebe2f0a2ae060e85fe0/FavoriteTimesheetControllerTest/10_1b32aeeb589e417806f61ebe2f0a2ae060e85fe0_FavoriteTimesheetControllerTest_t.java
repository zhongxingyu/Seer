 package com.thoughtworks.twu.controller;
 
 import org.junit.Test;
 import org.springframework.web.servlet.ModelAndView;
 
 import static org.testng.Assert.*;
 
 public class FavoriteTimesheetControllerTest {
 
 
     @Test
     public void shouldBeAbleToGetViewNameOfController() throws Exception{
        FavoriteTimesheetController controller = new FavoriteTimesheetController();
       assertEquals("ui/timesheet/favorite/new_form", controller.newFavorite().getViewName());
     }
 
//    @Test
//    public void shouldBeAbleToCreateNewFavoriteTimeSheet() throws  Exception{
//
//
//    }
 
 }
