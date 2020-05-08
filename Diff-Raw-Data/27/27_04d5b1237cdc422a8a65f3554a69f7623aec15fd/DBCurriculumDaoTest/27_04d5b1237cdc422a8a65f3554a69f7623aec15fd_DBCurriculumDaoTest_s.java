 package at.ac.tuwien.sepm.dao.hsqldb;
 
 import at.ac.tuwien.sepm.entity.Curriculum;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.io.IOException;
 
 /**
  * @author Markus MUTH
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"/applicationContext-test.xml"})
 public class DBCurriculumDaoTest {
 
     @Autowired
     DBCurriculumDao dao;
 
     @Before
     public void setUp() throws Exception {
         TestHelper.drop();
         TestHelper.create();
     }
 
     @Test
     public void testCreate() throws Exception {
         Curriculum e0 = new Curriculum();
         e0.setId(11234);
         e0.setStudyNumber("033 533");
         e0.setName("Data Engineering & Statistics");
         e0.setDescription("Data Engineering und so weiter");
         e0.setAcademicTitle("Bachelor");
         e0.setEctsChoice(10);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(30);
 
         assert(dao.create(e0));
         Curriculum e1 = dao.readById(0);
         e0.setId(0);
         assert(e1.equals(e0));
         assert(e1.getModules().size()==0);
 
     }
 
     @Test
     public void testCreateNull() throws Exception {
         assert(!dao.create(null));
     }
 
     @Test(expected = IOException.class)
     public void testCreateTooLongStudyNumber() throws Exception {
         String s = "aa";
         for(int i=0; i<10; i++) {
             s=s.concat(s);
         }
 
         Curriculum e0 = new Curriculum();
         e0.setId(11234);
         e0.setStudyNumber(s);
         e0.setName("Data Engineering & Statistics");
         e0.setDescription("Data Engineering und so weiter");
         e0.setAcademicTitle("Bachelor");
         e0.setEctsChoice(10);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(30);
 
        dao.create(e0);
     }
 
     @Test(expected = IOException.class)
     public void testCreateTooLongName() throws Exception {
         String s = "aa";
         for(int i=0; i<10; i++) {
             s=s.concat(s);
         }
 
         Curriculum e0 = new Curriculum();
         e0.setId(11234);
         e0.setStudyNumber("033 533");
         e0.setName(s);
         e0.setDescription("Data Engineering und so weiter");
         e0.setAcademicTitle("Bachelor");
         e0.setEctsChoice(10);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(30);
 
         dao.create(e0);
     }
 
     @Test(expected = IOException.class)
     public void testCreateTooLongDescription() throws Exception {
         String s = "aa";
         for(int i=0; i<10; i++) {
             s=s.concat(s);
         }
 
         Curriculum e0 = new Curriculum();
         e0.setId(11234);
         e0.setStudyNumber("033 533");
         e0.setName("Data Engineering & Statistics");
         e0.setDescription(s);
         e0.setAcademicTitle("Bachelor");
         e0.setEctsChoice(10);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(30);
 
         dao.create(e0);
     }
 
     @Test(expected = IOException.class)
     public void testCreateTooLongAcademicTitle() throws Exception {
         String s = "aa";
         for(int i=0; i<10; i++) {
             s=s.concat(s);
         }
 
         Curriculum e0 = new Curriculum();
         e0.setId(11234);
         e0.setStudyNumber("033 533");
         e0.setName("Data Engineering & Statistics");
         e0.setDescription("Data Engineering und so weiter");
         e0.setAcademicTitle(s);
         e0.setEctsChoice(10);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(30);
 
         dao.create(e0);
     }
 
     @Test(expected = DataAccessException.class)
     public void testCreateStudyNumberIsNull() throws Exception {
         Curriculum e0 = new Curriculum();
         e0.setId(11234);
         e0.setStudyNumber(null);
         e0.setName("Data Engineering & Statistics");
         e0.setDescription("Data Engineering und so weiter");
         e0.setAcademicTitle("Bachelor");
         e0.setEctsChoice(10);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(30);
 
         dao.create(e0);
     }
 
     @Test(expected = DataAccessException.class)
     public void testCreateNameIsNull() throws Exception {
         Curriculum e0 = new Curriculum();
         e0.setId(11234);
         e0.setStudyNumber("033 533");
         e0.setName(null);
         e0.setDescription("Data Engineering und so weiter");
         e0.setAcademicTitle("Bachelor");
         e0.setEctsChoice(10);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(30);
 
         dao.create(e0);
     }
 
    @Test(expected = DataAccessException.class)
     public void testCreateDescriptionIsNull() throws Exception {
         Curriculum e0 = new Curriculum();
         e0.setId(11234);
         e0.setStudyNumber("033 533");
         e0.setName("Data Engineering & Statistics");
         e0.setDescription(null);
         e0.setAcademicTitle("Bachelor");
         e0.setEctsChoice(10);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(30);
 
         dao.create(e0);
     }
 
     @Test(expected = DataAccessException.class)
     public void testCreateAcademicTitleIsNull() throws Exception {
         Curriculum e0 = new Curriculum();
         e0.setId(11234);
         e0.setStudyNumber("033 533");
         e0.setName("Data Engineering & Statistics");
         e0.setDescription("Data Engineering und so weiter");
         e0.setAcademicTitle(null);
         e0.setEctsChoice(10);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(30);
 
         dao.create(e0);
     }
 
     @Test(expected = DataAccessException.class)
     public void testCreateEctsChoiceIsNull() throws Exception {
         Curriculum e0 = new Curriculum();
         e0.setId(11234);
         e0.setStudyNumber("033 533");
         e0.setName("Data Engineering & Statistics");
         e0.setDescription("Data Engineering und so weiter");
         e0.setAcademicTitle("Bachelor");
         e0.setEctsChoice(null);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(30);
 
         dao.create(e0);
     }
 
     @Test(expected = DataAccessException.class)
     public void testCreateEctsFreeIsNull() throws Exception {
         Curriculum e0 = new Curriculum();
         e0.setId(11234);
         e0.setStudyNumber("033 533");
         e0.setName("Data Engineering & Statistics");
         e0.setDescription("Data Engineering und so weiter");
         e0.setAcademicTitle("Bachelor");
         e0.setEctsChoice(10);
         e0.setEctsFree(null);
         e0.setEctsSoftskill(30);
 
         dao.create(e0);
     }
 
     @Test(expected = DataAccessException.class)
     public void testCreateEctsSoftskillIsNull() throws Exception {
         Curriculum e0 = new Curriculum();
         e0.setId(11234);
         e0.setStudyNumber("033 533");
         e0.setName("Data Engineering & Statistics");
         e0.setDescription("Data Engineering und so weiter");
         e0.setAcademicTitle("Bachelor");
         e0.setEctsChoice(10);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(null);
 
         dao.create(e0);
     }
 
     @Test
     public void testReadAll () throws Exception {
         TestHelper.insert(0);
         assert(dao.readAll().size()==3);
     }
 
     @Test
     public void testReadAllButNothingExists () throws Exception {
         assert(dao.readAll().size()==0);
     }
 
     /**
      * There is only tested of the the amount of the returned modules is correct, not if the data of the returned
      * modules is correct because the method <code>readById()</code> calls internal the method <code>readByCurriculum()</code>
      * in <code>DBModuleDao</code>
      */
     @Test
     public void testReadById() throws Exception {
         TestHelper.insert(0);
 
         Curriculum e0 = new Curriculum();
         e0.setId(0);
         e0.setStudyNumber("033 533");
         e0.setName("Data Engineering & Statistics");
         e0.setDescription("Data Engineering und so weiter");
         e0.setAcademicTitle("Bachelor");
         e0.setEctsChoice(10);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(30);
 
         Curriculum e1 = dao.readById(0);
         assert(e1.equals(e0));
         assert(e1.getModules().size()==2);
     }
 
     @Test
     public void testReadByNotExistingId() throws Exception {
         TestHelper.insert(0);
         assert(dao.readById(-1)==null);
     }
 
     /**
      * There is only tested of the the amount of the returned modules is correct, not if the data of the returned
      * modules is correct because the method <code>readById()</code> calls internal the method <code>readByCurriculum()</code>
      * in <code>DBModuleDao</code>
      */
     @Test
     public void testReadByStudynumber() throws Exception {
         TestHelper.insert(0);
 
         Curriculum e0 = new Curriculum();
         e0.setId(0);
         e0.setStudyNumber("033 533");
         e0.setName("Data Engineering & Statistics");
         e0.setDescription("Data Engineering und so weiter");
         e0.setAcademicTitle("Bachelor");
         e0.setEctsChoice(10);
         e0.setEctsFree(20);
         e0.setEctsSoftskill(30);
 
         Curriculum e1 = dao.readByStudynumber("033 533");
         assert(e1.equals(e0));
         assert(e1.getModules().size()==2);
     }
 
     @Test
     public void testReadByStudynumberIsNull() throws Exception {
         TestHelper.insert(0);
         assert(dao.readByStudynumber(null)==null);
 
     }
 
     @Test
     public void testReadByNotExistingStudynumber() throws Exception {
         TestHelper.insert(0);
         assert(dao.readByStudynumber("invalid")==null);
     }
 
     @Test
     public void testUpdate() throws Exception {
         TestHelper.insert(0);
         Curriculum e0 = new Curriculum();
         e0.setId(0);
         e0.setStudyNumber("abc xyz");
         e0.setName("New Name");
         e0.setDescription("New Description");
         e0.setAcademicTitle("New Title");
         e0.setEctsChoice(-10);
         e0.setEctsFree(-20);
         e0.setEctsSoftskill(-30);
 
         assert(dao.update(e0));
         Curriculum e1 = dao.readById(0);
         assert(e1.equals(e0));
         assert(e1.getModules().size()==2);
     }
 
     @Test
     public void testUpdateNull () throws Exception {
         TestHelper.insert(0);
         assert(!dao.update(null));
     }
 
     @Test(expected = IOException.class)
     public void testUpdateTooLongStudyNumber() throws Exception {
         TestHelper.insert(0);
 
         String s = "aa";
         for(int i=0; i<10; i++) {
             s=s.concat(s);
         }
 
         Curriculum e0 = new Curriculum();
         e0.setId(0);
         e0.setStudyNumber(s);
         e0.setName("New Name");
         e0.setDescription("New Description");
         e0.setAcademicTitle("New Title");
         e0.setEctsChoice(-10);
         e0.setEctsFree(-20);
         e0.setEctsSoftskill(-30);
 
         dao.update(e0);
     }
 
     @Test(expected = IOException.class)
     public void testUpdateTooLongName() throws Exception {
         TestHelper.insert(0);
 
         String s = "aa";
         for(int i=0; i<10; i++) {
             s=s.concat(s);
         }
 
         Curriculum e0 = new Curriculum();
         e0.setId(0);
         e0.setStudyNumber("abc xyz");
         e0.setName(s);
         e0.setDescription("New Description");
         e0.setAcademicTitle("New Title");
         e0.setEctsChoice(-10);
         e0.setEctsFree(-20);
         e0.setEctsSoftskill(-30);
 
         dao.update(e0);
     }
 
     @Test(expected = IOException.class)
     public void testUpdateTooLongDescription() throws Exception {
         TestHelper.insert(0);
 
         String s = "aa";
         for(int i=0; i<10; i++) {
             s=s.concat(s);
         }
 
         Curriculum e0 = new Curriculum();
         e0.setId(0);
         e0.setStudyNumber("abc xyz");
         e0.setName("New Name");
         e0.setDescription(s);
         e0.setAcademicTitle("New Title");
         e0.setEctsChoice(-10);
         e0.setEctsFree(-20);
         e0.setEctsSoftskill(-30);
 
         dao.update(e0);
     }
 
     @Test(expected = IOException.class)
     public void testUpdateTooLongAcademicTitle() throws Exception {
         TestHelper.insert(0);
 
         String s = "aa";
         for(int i=0; i<10; i++) {
             s=s.concat(s);
         }
 
         Curriculum e0 = new Curriculum();
         e0.setId(0);
         e0.setStudyNumber("abc xyz");
         e0.setName("New Name");
         e0.setDescription("New Description");
         e0.setAcademicTitle(s);
         e0.setEctsChoice(-10);
         e0.setEctsFree(-20);
         e0.setEctsSoftskill(-30);
 
         dao.update(e0);
     }
 
     @Test
     public void testDelete() throws Exception {
         TestHelper.insert(0);
         assert(dao.readById(0)!=null);
         assert(dao.delete(0));
         assert(dao.readById(0)==null);
     }
 
     @Test
     public void testDeleteNotExistingCurriculum() throws Exception {
         TestHelper.insert(0);
         assert(!dao.delete(-1));
     }
 }
