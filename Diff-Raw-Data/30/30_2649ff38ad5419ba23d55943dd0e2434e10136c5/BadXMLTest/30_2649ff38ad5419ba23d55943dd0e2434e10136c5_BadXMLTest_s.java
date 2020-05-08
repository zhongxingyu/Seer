 package edu.sjsu.cinequest.comm;
 
 import java.util.Vector;
 
 import edu.sjsu.cinequest.comm.cinequestitem.MobileItem;
 import edu.sjsu.cinequest.comm.cinequestitem.ProgramItem;
 import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
 import edu.sjsu.cinequest.comm.cinequestitem.Section;
 import edu.sjsu.cinequest.javase.JavaSEPlatform;
 import junit.framework.TestCase;
 
 public class BadXMLTest extends TestCase {
     private QueryManager mgr;
     protected void setUp() throws Exception
     {
         Platform.setInstance(new JavaSEPlatform());
         mgr = new QueryManager();
     }
 
     public void testAllTitles() {
         TestCallback callback = new TestCallback();
         mgr.getAllPrograms(callback);
         @SuppressWarnings("unchecked") Vector<ProgramItem> items = (Vector<ProgramItem>) callback.getResult();
         for (ProgramItem item : items)
         {
         	TestCallback callback2 = new TestCallback();
         	mgr.getProgramItem(item.getId(), callback2);
         	ProgramItem pi = (ProgramItem) callback2.getResult();
         	assertNotNull(pi.getTitle());
         	assertNotNull(pi.getDescription());
         }
 
     }
     
     public void testIhome() {
     	TestCallback callback = new TestCallback();
         mgr.getSpecialScreen("ihome", callback);
         @SuppressWarnings("unchecked") Vector<Section> sections = (Vector<Section>) callback.getResult();
         for (Section section : sections)
         {
         	@SuppressWarnings("unchecked") Vector<MobileItem> items = (Vector<MobileItem>) section.getItems();
         	for (MobileItem item : items)
         	{
         		int id = item.getLinkId();
        		if (id != 0) {
		        	TestCallback callback2 = new TestCallback();
		        	mgr.getMobileItem(id, callback2);
		        	ProgramItem pi = (ProgramItem) callback2.getResult();
		        	assertNotNull(pi.getTitle());
		        	assertNotNull(pi.getDescription());
        		}
         	}
         }
     }
     
     public void testForums() {
     	TestCallback callback = new TestCallback();
         mgr.getEventSchedules("forums", callback);
         @SuppressWarnings("unchecked") Vector<Schedule> schedules = (Vector<Schedule>) callback.getResult();
         for (Schedule schedule : schedules)
         {
         	TestCallback callback2 = new TestCallback();
         	mgr.getProgramItem(schedule.getItemId(), callback2);
         	ProgramItem pi = (ProgramItem) callback2.getResult();
         	assertNotNull(pi.getTitle());
         	assertNotNull(pi.getDescription());
         }    	
     }
 
     public void testEvents() {
     	TestCallback callback = new TestCallback();
         mgr.getEventSchedules("events", callback);
         @SuppressWarnings("unchecked") Vector<Schedule> schedules = (Vector<Schedule>) callback.getResult();
         for (Schedule schedule : schedules)
         {
         	TestCallback callback2 = new TestCallback();
         	mgr.getProgramItem(schedule.getItemId(), callback2);
         	ProgramItem pi = (ProgramItem) callback2.getResult();
         	assertNotNull(pi.getTitle());
         	assertNotNull(pi.getDescription());
         }    	
     }
 }
