 package de.hswt.hrm.catalog.dao.jdbc;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import static org.mockito.Mockito.*;
 
 import java.util.Collection;
 
 import org.junit.Test;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.catalog.dao.core.IActivityDao;
 import de.hswt.hrm.catalog.dao.core.ICurrentDao;
 import de.hswt.hrm.catalog.dao.core.ITargetDao;
 import de.hswt.hrm.catalog.dao.jdbc.ActivityDao;
 import de.hswt.hrm.catalog.model.Activity;
 import de.hswt.hrm.catalog.model.Current;
 import de.hswt.hrm.test.database.AbstractDatabaseTest;
 
 public class ActivityDaoTest extends AbstractDatabaseTest {
 
     private void compareActivityFields(final Activity expected, final Activity actual) {
         assertEquals("Name not set correctly.", expected.getName(), actual.getName());
         assertEquals("Text not set correctly.", expected.getText(), actual.getText());
     }
 
     @Test
     public void testInsertActivity() throws ElementNotFoundException, DatabaseException {
         final String name = "ActivityName";
         final String text = "Get outta here ...";
         ITargetDao targetDao = mock(ITargetDao.class);
         ICurrentDao currentDao = new CurrentDao(targetDao);
 
         Activity expected = new Activity(name, text);
 
         // Check return value from insert
         ActivityDao dao = new ActivityDao(currentDao);
         Activity parsed = dao.insert(expected);
         compareActivityFields(expected, parsed);
         assertTrue("ID not set correctly.", parsed.getId() >= 0);
 
         // Request from database
         Activity requested = dao.findById(parsed.getId());
         compareActivityFields(expected, requested);
         assertEquals("Requested object does not equal parsed one.", parsed, requested);
     }
 
     @Test
     public void testUpdateActivity() throws ElementNotFoundException, DatabaseException {
 
         Activity act1 = new Activity("FirstActivity", "FirstText");
         ITargetDao targetDao = mock(ITargetDao.class);
         ICurrentDao currentDao = new CurrentDao(targetDao);
 
         ActivityDao dao = new ActivityDao(currentDao);
         Activity parsed = dao.insert(act1);
 
         // We add another contact to ensure that the update affects just one row.
         Activity act2 = new Activity("SecondActivity", "SecondText");
         dao.insert(act2);
 
         parsed.setText("Some City");
         parsed.setName("someone@example.com");
         dao.update(parsed);
 
         Activity requested = dao.findById(parsed.getId());
         compareActivityFields(parsed, requested);
         assertEquals("Requested object does not equal updated one.", parsed, requested);
 
     }
 
     @Test
     public void testFindAllActivity() throws ElementNotFoundException, DatabaseException {
         Activity act1 = new Activity("FirstActivity", "FirstText");
         Activity act2 = new Activity("SecondActivity", "SecondText");
         ITargetDao targetDao = mock(ITargetDao.class);
         ICurrentDao currentDao = new CurrentDao(targetDao);
 
         ActivityDao dao = new ActivityDao(currentDao);
         dao.insert(act1);
         dao.insert(act2);
 
         Collection<Activity> activity = dao.findAll();
         assertEquals("Count of retrieved activities does not match.", 2, activity.size());
     }
 
     @Test
     public void testFindByIdActivity() throws ElementNotFoundException, DatabaseException {
         Activity expected = new Activity("FirstActivity", "FirstText");
         ITargetDao targetDao = mock(ITargetDao.class);
         ICurrentDao currentDao = new CurrentDao(targetDao);
         ActivityDao dao = new ActivityDao(currentDao);
         Activity parsed = dao.insert(expected);
 
         Activity requested = dao.findById(parsed.getId());
         compareActivityFields(expected, requested);
     }
     
     @Test
    public void testFindByActivityState() throws DatabaseException {
     	ITargetDao targetDao = mock(ITargetDao.class);
         ICurrentDao currentDao = new CurrentDao(targetDao);
         IActivityDao activityDao = new ActivityDao(currentDao);
         Current current = new Current("FirstCurrent", "Some text..");
         Activity activity1 = new Activity("Activity1", "Some Text..");
         Activity activity2 = new Activity("Activity2", "Some other text...");
         
         current = currentDao.insert(current);
         activityDao.addToCurrent(current, activity1);
         activityDao.addToCurrent(current, activity2);
         
         Collection<Activity> activityStates = activityDao.findByCurrent(current);
         assertEquals("Wrong number of current states returned.", 2, activityStates.size());        
     }
 
     @Test
     public void testConnectActivityAndCurrentState() throws SaveException {
     	ITargetDao targetDao = mock(ITargetDao.class);
     	ICurrentDao currentDao = new CurrentDao(targetDao);
     	ActivityDao activityDao = new ActivityDao(currentDao);
         Current current = new Current("FirstCurrent", "FirstText");
         Activity activity = new Activity("Some Activity", "Some Text...");
         
         activityDao.addToCurrent(current, activity);
         
         // FIXME: check if could retrieve the added connection
     }
     
     @Test
     public void testDisconnectActivityAndCurrentState() throws DatabaseException {
     	ITargetDao targetDao = mock(ITargetDao.class);
     	ICurrentDao currentDao = new CurrentDao(targetDao);
     	ActivityDao activityDao = new ActivityDao(currentDao);
         Current current = new Current("FirstCurrent", "FirstText");
         current = currentDao.insert(current);
         Activity activity = new Activity("Some Activity", "Some Text...");
         activity = activityDao.insert(activity);
         activityDao.addToCurrent(current, activity);
 
         activityDao.removeFromCurrent(current, activity);
     }
 }
