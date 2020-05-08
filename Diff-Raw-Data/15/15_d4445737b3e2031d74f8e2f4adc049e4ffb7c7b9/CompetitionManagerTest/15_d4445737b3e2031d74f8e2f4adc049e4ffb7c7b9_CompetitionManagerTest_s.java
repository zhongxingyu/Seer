 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controller;
 
 
 import Domaene.CompetitionManager;
 import Persistence.ClubMember;
 import Persistence.Competition;
 import Persistence.PersistenceManager;
 import Persistence.Rule;
 import Persistence.Team;
 import java.util.List;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.Ignore;
 
 public class CompetitionManagerTest {
     
    
     
     public CompetitionManagerTest () {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
     /**
      * Test of save method, of class PersistenceManager.
      */
 
     @Test
     public void testGetCompetitinById(){
          int id = 1;
          CompetitionManager mng = new CompetitionManager();
          Competition testComp = mng.getCompetitinById(id);
          System.out.println();
          System.out.println("Running test on getCompetition by ID");
          System.out.println("Getting Competition with id: " + id);
          System.out.println("Returne Competition with id: " + testComp.getId());
          System.out.println();
         
     }
     
     @Test
     public void testGetCompetitinByName(){
          String name = "Weltmeisterschaft";
          CompetitionManager mng = new CompetitionManager();
          List<Object> testComp = mng.getCompetitionsByName(name);
          Competition comp = (Competition) testComp.get(0);
          System.out.println();
          System.out.println("Running test on getCompetition by Name");
          System.out.println("eturned Competition with name: " + comp.getName());
          System.out.println();
         
     }
     
     @Test
     public void testgetCompetitionByDepartmentId(){
          int departmentId = 3;
          CompetitionManager mng = new CompetitionManager();
         //List<Object> testComp = mng.getCompetitionByDepartmentId(departmentId);
         mng.getCompetitionByDepartmentId(departmentId);
         //Competition comp = (Competition) testComp.get(0);
          System.out.println();
          System.out.println("Running test on getCompetition over Departmentid");
         //System.out.println("eturned Competition with name: " + comp.getId());
          System.out.println();
         
     }
 }
