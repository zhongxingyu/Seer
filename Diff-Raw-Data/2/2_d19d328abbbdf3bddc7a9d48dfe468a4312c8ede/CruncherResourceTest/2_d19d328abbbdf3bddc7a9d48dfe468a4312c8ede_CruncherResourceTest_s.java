 /*
  * Copyright 2013 SURFnet bv, The Netherlands
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.surfnet.cruncher.resource;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Response;
 
 import org.joda.time.LocalDate;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.surfnet.cruncher.config.SpringConfiguration;
 import org.surfnet.cruncher.model.LoginData;
 import org.surfnet.cruncher.model.SpStatistic;
 
 @SuppressWarnings("unchecked")
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = SpringConfiguration.class)
 public class CruncherResourceTest {
 
   @Inject
   private CruncherResource cruncherResource;
 
   @Inject
   private JdbcTemplate jdbcTemplate;
   
   private HttpServletRequest request = null; //currently never really used
   
   @Test
   public void getUniqueLogins() {
     LocalDate start = new LocalDate(0L);
     LocalDate end = new LocalDate(System.currentTimeMillis());
     Response response = cruncherResource.getUniqueLogins(request, start.toDate().getTime(), end.toDate().getTime(), "idp1", "sp1");
     
     List<LoginData> result = (List<LoginData>) response.getEntity();
     assertNotNull(result);
    assertEquals(13, result.size());
 
     response = cruncherResource.getUniqueLogins(request, start.toDate().getTime(), end.toDate().getTime(), "idp1", "unknown");
     result = (List<LoginData>) response.getEntity();
     assertNotNull(result);
     assertEquals(0, result.size());
 
     LocalDate startDate = new LocalDate(2013, 1, 1);
     LocalDate endDate = new LocalDate(2013, 1, 4);
     response = cruncherResource.getUniqueLogins(request, startDate.toDate().getTime(), endDate.toDate().getTime(), "idp1", null);
     result = (List<LoginData>) response.getEntity();
     assertNotNull(result);
     assertEquals(8, result.size());
     LoginData first = result.get(0);
     assertEquals(20, first.getTotal());
     assertEquals("idp1", first.getIdpEntityId());
   }
 
   @Test
   public void getLogins() {
     LocalDate start = new LocalDate(2013, 1, 1);
     LocalDate end = new LocalDate(2013, 1, 12);
     Response response = cruncherResource.getLoginsPerInterval(request, start.toDate().getTime(), end.toDate().getTime(), "idp1", "sp1");
     List<LoginData> result = (List<LoginData>) response.getEntity();
     assertNotNull(result);
     assertEquals(1, result.size());
     LoginData data = result.get(0);
     checkSp1Entry(data);
   }
 
   @Test
   public void getMultipleLogins() {
     LocalDate start = new LocalDate(2013, 1, 1);
     LocalDate end = new LocalDate(2013, 1, 12);
     Response response = cruncherResource.getLoginsPerInterval(request, start.toDate().getTime(), end.toDate().getTime(), "idp1", null);
     List<LoginData> result = (List<LoginData>) response.getEntity();
     assertNotNull(result);
     assertEquals(2, result.size());
     LoginData first = result.get(0);
     LoginData second = result.get(1);
     if (first.getSpEntityId().equals("sp1")) {
       checkSp1Entry(first);
     } else {
       checkSp1Entry(second);
     }
   }
 
   private void checkSp1Entry(LoginData data) {
     assertEquals(240, data.getTotal());
     assertEquals(12, data.getData().size());
     assertEquals(20, (int) data.getData().get(0));
     assertEquals(20, (int) data.getData().get(6));
     assertEquals(20, (int) data.getData().get(11));
   }
 
   @Test
   public void testIllegalArguments() {
     
     try {
       cruncherResource.getLoginsPerInterval(request, 0L, 0L, null, null);
       fail("Should have received an illegal argument exception");
     } catch (IllegalArgumentException iae) {
       // expected
     }
   }
   
   @Test
   public void testResponseWithZeros() {
     LocalDate start = new LocalDate(2013, 1, 10);
     LocalDate end = new LocalDate(2013, 1, 20);
     Response response = cruncherResource.getLoginsPerInterval(request, start.toDate().getTime(), end.toDate().getTime(), "idp1", "sp1");
     List<LoginData> result = (List<LoginData>) response.getEntity();
     
     assertNotNull(result);
     assertEquals(1, result.size());
     LoginData loginData = result.get(0);
     assertEquals(11, loginData.getData().size());
     assertEquals(20, (int)loginData.getData().get(2));
     assertEquals(0, (int)loginData.getData().get(3));
     assertEquals(0, (int)loginData.getData().get(4));
     assertEquals(0, (int)loginData.getData().get(10));
   }
   
   @Test
   public void getActiveServices() {
     Response response = cruncherResource.getRecentLoginsForUser(request, "user_1", "idp2");
     List<SpStatistic> result = (List<SpStatistic>) response.getEntity();
     assertNotNull(result);
     assertEquals(2, result.size());
     SpStatistic currentStat = result.get(0);
     if (currentStat.getSpEntityId().equals("sp2")) {
       checkStatistics(result.get(0));
     } else {
       checkStatistics(result.get(1));
     }
   }
 
   private void checkStatistics(SpStatistic spStatistic) {
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     java.util.Date entryDate = null;
     try {
       entryDate = sdf.parse("2012-04-19 11:48:41");
     } catch (ParseException e) {
       e.printStackTrace();
     }
     assertEquals(entryDate.getTime(), spStatistic.getEntryTime());
   }
 }
