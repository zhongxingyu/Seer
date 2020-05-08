 package de.hswt.hrm.catalog.dao.jdbc;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Collection;
 
 import org.junit.Test;
 import de.hswt.hrm.catalog.model.Current;
 import de.hswt.hrm.catalog.model.Target;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.test.database.AbstractDatabaseTest;
 
 public class CurrentDaoTest extends AbstractDatabaseTest {
 
     private void compareCurrentFields(final Current expected, final Current actual) {
         assertEquals("Name not set correctly.", expected.getName(), actual.getName());
         assertEquals("Text not set correctly.", expected.getText(), actual.getText());
 
     }
 
     @Test
     public void testInsertCurrent() throws ElementNotFoundException, DatabaseException {
         final String name = "CurrentName";
         final String text = "Get outta here ...";
 
         Current expected = new Current(name, text);
 
         // Check return value from insert
         TargetDao targetDao = new TargetDao();
         CurrentDao currentDao = new CurrentDao(targetDao);
         Current parsed = currentDao.insert(expected);
         compareCurrentFields(expected, parsed);
         assertTrue("ID not set correctly.", parsed.getId() >= 0);
 
         // Request from database
         Current requested = currentDao.findById(parsed.getId());
         compareCurrentFields(expected, requested);
         assertEquals("Requested object does not equal parsed one.", parsed, requested);
     }
 
     @Test
     public void testUpdateCurrent() throws ElementNotFoundException, DatabaseException {
         Current cur1 = new Current("FirstCurrent", "FirstText");
 
         TargetDao targetDao = new TargetDao();
         CurrentDao currentDao = new CurrentDao(targetDao);
         Current parsed = currentDao.insert(cur1);
 
         // We add another current to ensure that the update affects just one row.
         Current cur2 = new Current("SecondCurrent", "SecondText");
         currentDao.insert(cur2);
 
         parsed.setText("Some Test");
         parsed.setName("Some Name");
         currentDao.update(parsed);
 
         Current requested = currentDao.findById(parsed.getId());
         compareCurrentFields(parsed, requested);
         assertEquals("Requested object does not equal updated one.", parsed, requested);
     }
 
     @Test
     public void testFindAllCurrent() throws ElementNotFoundException, DatabaseException {
         Current cur1 = new Current("FirstCurrent", "FirstText");
         Current cur2 = new Current("SecondCurrent", "SecondText");
 
         TargetDao targetDao = new TargetDao();
         CurrentDao currentDao = new CurrentDao(targetDao);
         currentDao.insert(cur1);
         currentDao.insert(cur2);
 
         Collection<Current> current = currentDao.findAll();
         assertEquals("Count of retrieved currents does not match.", 2, current.size());
     }
 
     @Test
     public void testFindByIdCurrent() throws ElementNotFoundException, DatabaseException {
         Current expected = new Current("FirstCurrent", "FirstText");
         TargetDao targetDao = new TargetDao();
         CurrentDao currentDao = new CurrentDao(targetDao);
         Current parsed = currentDao.insert(expected);
 
         Current requested = currentDao.findById(parsed.getId());
         compareCurrentFields(expected, requested);
     }
     
     @Test
     public void testConnectTargetAndCurrentState() throws SaveException {
         TargetDao targetDao = new TargetDao();
         CurrentDao currentDao = new CurrentDao(targetDao);
         Current current = new Current("FirstCurrent", "FirstText");
         Target target = new Target("FirstTarget", "Some Text");
         
         currentDao.addToTarget(target, current);
         
         // FIXME: check if could retrieve the added connection
     }
     
     @Test
    public void testFindByTargetState() {
        fail();
     }
 
 }
