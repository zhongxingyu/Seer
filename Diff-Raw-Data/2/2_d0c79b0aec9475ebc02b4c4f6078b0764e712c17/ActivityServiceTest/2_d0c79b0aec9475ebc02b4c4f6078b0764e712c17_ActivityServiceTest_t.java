 package com.thoughtworks.twu.service;
 
 import com.thoughtworks.twu.domain.Activity;
 import org.json.JSONArray;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 public class ActivityServiceTest {
 
     private ActivityService service;
 
     @Before
     public void setUp() throws Exception {
        service = new ActivityService();
 
     }
 
     @Test
     public void shouldFetchListOfAllActivities() {
         List<Activity> activities = service.getAllActivities();
         assertEquals(11151, activities.size());
     }
 
     @Test
     public void shouldReturnListOfActivitiesContainingTWU() {
         JSONArray activities = service.getActivities("TWU");
         assertEquals(7, activities.length());
     }
 
     @Test
     public void shouldReturnListOfActivitiesContainingCaseInsensitiveTWU() {
 
         JSONArray  activities = service.getActivities("twu");
 
         assertEquals(7, activities.length());
     }
 
     @Test
     public void shouldReturnEmptyListForAAA() {
 
         JSONArray  activities = service.getActivities("AAA");
 
         assertEquals(0, activities.length());
     }
 
     @Test
     @Ignore
     public void shouldReturnActivityListWhenUnderscoreIsUsed() {
 
         JSONArray  activities = service.getActivities("ser_e");
 
        assertEquals(4, activities.length());
     }
 
 
     @Test
     @Ignore
     public void shouldPerformAndWhenSearchCriteriaContainsModulus() throws Exception {
 
         JSONArray activities = service.getActivities("rorz%twu");
 
         assertEquals(5, activities.length());
 
 
     }
 }
 
 
