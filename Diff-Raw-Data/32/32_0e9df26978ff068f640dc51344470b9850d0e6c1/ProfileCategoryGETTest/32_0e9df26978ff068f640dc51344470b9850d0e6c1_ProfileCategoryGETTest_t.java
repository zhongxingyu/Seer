 package gc.carbon.test.profile.v2;
 
 import org.testng.annotations.Test;
 import org.restlet.data.Status;
 import org.restlet.data.Form;
 import org.restlet.resource.DomRepresentation;
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.ISOPeriodFormat;
 import org.w3c.dom.Document;
 import gc.carbon.test.profile.BaseProfileCategoryTest;
 import com.jellymold.utils.domain.UidGen;
 
 /**
  * This file is part of AMEE.
  * <p/>
  * AMEE is free software; you can redistribute it and/or mo
  * dify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 public class ProfileCategoryGETTest extends BaseProfileCategoryTest {
 
     private static String before_and_after;
     private static String before_and_inside;
     private static String inside_and_after;
     private static String outside_and_before;
     private static String inside;
     private static String after_and_outside;
     private static String before_and_ongoing;
     private static String inside_and_ongoing;
     private static String after_and_ongoing;
 
     private static DateTime startDate;
     private static DateTime endDate;
     private static Days duration;
 
     private static boolean initialised = false;
 
     private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mmZ");
 
     public ProfileCategoryGETTest(String name) throws Exception {
         super(name);
         setUpProfileItems();
     }
 
     private void setUpProfileItems() throws Exception {
         if (initialised)
             return;
 
         initDB();
 
         startDate = new DateTime();
         duration = Days.days(15);
         endDate = startDate.plus(duration);
 
         before_and_after = create(startDate.minusDays(1),endDate.plusDays(1), null);
         before_and_inside = create(startDate.minusDays(1),endDate.minusDays(1), null);
         inside_and_after = create(startDate.plusDays(1),endDate.plusDays(1), null);
         outside_and_before = create(startDate.minusDays(2),startDate.minusDays(1), null);
         inside = create(startDate.plusDays(2),endDate.minusDays(2), null);
         after_and_outside = create(endDate.plusDays(1),endDate.plusDays(2), null);
         before_and_ongoing = create(startDate.minusDays(1),null, null);
         inside_and_ongoing = create(startDate.plusDays(1),null, null);
         after_and_ongoing = create(endDate.plusDays(1),null, null);
 
         initialised = true;
     }
 
     private String create(DateTime startDate, DateTime endDate, String name) throws Exception {
         Form data = new Form();
         data.add("startDate",startDate.toString(fmt));
         if (endDate != null)
             data.add("endDate",endDate.toString(fmt));
         if (name != null) {
             data.add("name", name);
         } else {
             data.add("name", UidGen.getUid());  
         }
         data.add("distance","1000");
         data.add("dataItemUid",DATA_CATEGORY_UID);
 
         return client.createProfileItem(data);
     }
 
     @Test
     public void testStartDateAfterAllEndDates() throws Exception {
         client.addQueryParameter("startDate", startDate.plusDays(100).toString(fmt));
 
         DomRepresentation rep = client.get().getEntityAsDom();
         rep.write(System.out);
         Document doc = rep.getDocument();
         assertXpathExists("//ProfileItem[@uid='" + before_and_ongoing + "']", doc);
         assertXpathExists("//ProfileItem[@uid='" + inside_and_ongoing + "']", doc);
         assertXpathExists("//ProfileItem[@uid='" + after_and_ongoing + "']", doc);
         assertXpathEvaluatesTo("3","count(//ProfileItem)",doc);
     }
 
     @Test
     public void testStartDateBeforeAllEndDates() throws Exception {
         client.addQueryParameter("startDate", startDate.toString(fmt));
 
         DomRepresentation rep = client.get().getEntityAsDom();
         Document doc = rep.getDocument();
         assertXpathNotExists("//ProfileItem[@uid='" + outside_and_before + "']", doc);
         assertXpathEvaluatesTo("8","count(//ProfileItem)",doc);
     }
 
     @Test
     public void testStartDateAndEndDate() throws Exception {
         client.addQueryParameter("startDate", startDate.toString(fmt));
         client.addQueryParameter("endDate", endDate.toString(fmt));
 
         DomRepresentation rep = client.get().getEntityAsDom();
         Document doc = rep.getDocument();
         assertXpathNotExists("//ProfileItem[@uid='" + outside_and_before + "']", doc);
         assertXpathNotExists("//ProfileItem[@uid='" + after_and_outside + "']", doc);
         assertXpathNotExists("//ProfileItem[@uid='" + after_and_ongoing + "']", doc);
         assertXpathEvaluatesTo("6","count(//ProfileItem)",doc);
     }
 
     @Test
     public void testStartDateAndDuration() throws Exception {
         client.addQueryParameter("startDate",startDate.toString(fmt));
         client.addQueryParameter("duration",ISOPeriodFormat.standard().print(duration));
 
         DomRepresentation rep = client.get().getEntityAsDom();
         Document doc = rep.getDocument();
         assertXpathNotExists("//ProfileItem[@uid='" + outside_and_before + "']", doc);
         assertXpathNotExists("//ProfileItem[@uid='" + after_and_outside + "']", doc);
         assertXpathNotExists("//ProfileItem[@uid='" + after_and_ongoing + "']", doc);
         assertXpathEvaluatesTo("6","count(//ProfileItem)",doc);
     }
 
     @Test
     public void testSelectByStart() throws Exception {
        client.addQueryParameter("selectby","start");
         client.addQueryParameter("startDate",startDate.toString(fmt));
 
         DomRepresentation rep = client.get().getEntityAsDom();
         rep.write(System.out);
         Document doc = rep.getDocument();
         assertXpathNotExists("//ProfileItem[@uid='" + before_and_after + "']", doc);
         assertXpathNotExists("//ProfileItem[@uid='" + before_and_inside + "']", doc);
         assertXpathNotExists("//ProfileItem[@uid='" + outside_and_before + "']", doc);
     }
 
     @Test
     public void testSelectByEnd() throws Exception {
        client.addQueryParameter("selectby","end");
         client.addQueryParameter("startDate",startDate.toString(fmt));
 
         DomRepresentation rep = client.get().getEntityAsDom();
         Document doc = rep.getDocument();
         assertXpathNotExists("//ProfileItem[@uid='" + after_and_outside + "']", doc);
         assertXpathNotExists("//ProfileItem[@uid='" + after_and_ongoing + "']", doc);
     }    
 
     @Test
     public void testInValidProfileDateRequest() throws Exception {
         client.addQueryParameter("profileDate","201004");
 
         Status status = client.get().getStatus();
         assertEquals("Should be Bad Request",400,status.getCode());
     }
     @Test
     public void testSupercededNotReturned() throws Exception {
 
         String before_and_ongoing_named = create(startDate.minusDays(2), null, "test");
         String before_and_ongoing2_named = create(startDate.minusDays(1), null, "test");
         String on_and_ongoing_named = create(startDate, null, "test");
         String inside_and_ongoing_named = create(startDate.plusDays(1), null, "test");
         String inside_and_ongoing2_named = create(startDate.plusDays(2), null, "test");
 
         System.out.println("before_and_ongoing_named  : " + before_and_ongoing_named);
         System.out.println("before_and_ongoing1_named : " + before_and_ongoing2_named);
         System.out.println("on_and_ongoing_named      : " + on_and_ongoing_named);
         System.out.println("inside_and_ongoing_named  : " + inside_and_ongoing_named);
         System.out.println("inside_and_ongoing2_named : " + inside_and_ongoing2_named);
 
         client.addQueryParameter("startDate", startDate.toString(fmt));
 
         DomRepresentation rep = client.get().getEntityAsDom();
         Document doc = rep.getDocument();
         assertXpathNotExists("//ProfileItem[@uid='" + before_and_ongoing_named + "']", doc);
         assertXpathNotExists("//ProfileItem[@uid='" + before_and_ongoing2_named + "']", doc);
         assertXpathExists("//ProfileItem[@uid='" + on_and_ongoing_named + "']", doc);
         assertXpathExists("//ProfileItem[@uid='" + inside_and_ongoing_named + "']", doc);
         assertXpathExists("//ProfileItem[@uid='" + inside_and_ongoing2_named + "']", doc);
     }   
 }
