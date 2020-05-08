 package at.ac.tuwien.sepm.dao.hsqldb;
 
 import at.ac.tuwien.sepm.dao.PendingRegistrationDao;
 import at.ac.tuwien.sepm.entity.TissExam;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.List;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"/applicationContext-test.xml"})
 public class DBPendingRegistrationDaoTest {
 
     @Autowired
     PendingRegistrationDao pendingRegistrationDao;
 
    private TissExam tissExam;

     @Before
     public void setUp() throws Exception {
         TestHelper.drop();
         TestHelper.create();
 
        tissExam = new TissExam();
         tissExam.setLvanr("123.123");
         tissExam.setName("wat");
         tissExam.setMode("Schriftlich");
         tissExam.setStartRegistration(new DateTime(10));
         tissExam.setEndRegistration(new DateTime(20));
    }
 
    @Test
    public void testCreateValid() throws Exception {
         pendingRegistrationDao.create(tissExam);
     }
 
     @Test
     public void testCreateInvalid() throws Exception {
         assertFalse(pendingRegistrationDao.create(null));
     }
 
 
     @Test
     public void testReadAllTissExams() throws Exception {
        pendingRegistrationDao.create(tissExam);
         List<TissExam> tissExamList = pendingRegistrationDao.readAllTissExams();
         assertFalse(tissExamList.isEmpty());
         assertTrue(tissExamList.size() == 1);
         assertTrue(tissExamList.get(0).getName().equals("wat"));
     }
 
     @Test
     public void testDelete() throws Exception {
        pendingRegistrationDao.create(tissExam);
         assertTrue(pendingRegistrationDao.delete(0));
         assertFalse(pendingRegistrationDao.delete(0));
     }
 }
