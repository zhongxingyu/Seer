 package com.orgsync.api.integration;
 
 import static org.junit.Assert.assertThat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hamcrest.core.IsCollectionContaining;
 import org.junit.AfterClass;
import org.junit.Ignore;
 import org.junit.Test;
 
 import com.orgsync.api.Resources;
 import com.orgsync.api.TimesheetsResource;
 import com.orgsync.api.model.timesheets.Timesheet;
 import com.typesafe.config.Config;
 
 public class TimesheetsIntegrationTest extends BaseIntegrationTest<TimesheetsResource> {
 
     private static final List<? extends Config> timesheetsConfig = DbTemplate.getList("timesheets");
 
     public TimesheetsIntegrationTest() {
         super(Resources.TIMESHEETS);
     }
 
     @AfterClass
     public static void cleanup() {
         BaseIntegrationTest.cleanup(Resources.TIMESHEETS);
     }
 
     @Test
     public void testGetTimesheets() throws Exception {
         List<Timesheet> result = getResult(getResource().getTimesheets());
 
         testContainsIds(result, timesheetsConfig);
     }
 
     @Test
     public void testGetAccountTimesheets() throws Exception {
         Config userConfig = DbTemplate.getList("users").get(0);
         int userId = userConfig.getInt("id");
 
         List<Timesheet> result = getResult(getResource().getAccountTimesheets(userId));
 
         List<Integer> expectedIds = new ArrayList<Integer>();
         for (Config config : timesheetsConfig) {
             if (config.getString("account_username").equals(userConfig.getString("username"))) {
                 expectedIds.add(config.getInt("id"));
             }
         }
 
         List<Integer> ids = getIdsForObjects(result);
 
         assertThat(ids, IsCollectionContaining.hasItems(expectedIds.toArray(new Integer[expectedIds.size()])));
     }
 
     @Test
     public void testGetOrgTimesheets() throws Exception {
         Config portalConfig = DbTemplate.getList("portals").get(1);
 
         List<Timesheet> result = getResult(getResource().getOrgTimesheets(portalConfig.getInt("id")));
 
         List<Integer> expectedIds = new ArrayList<Integer>();
         for (Config config : timesheetsConfig) {
             if (config.getString("portal_short_name").equals(portalConfig.getString("short_name"))) {
                 expectedIds.add(config.getInt("id"));
             }
         }
 
         List<Integer> ids = getIdsForObjects(result);
 
         assertThat(ids, IsCollectionContaining.hasItems(expectedIds.toArray(new Integer[expectedIds.size()])));
     }
 
     @Test
    @Ignore("TODO after events...")
     public void testGetEventTimesheets() throws Exception {
 
     }
 }
