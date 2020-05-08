 /**
  * 
  */
 package de.aidger.utils.reports;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.math.BigDecimal;
 import java.sql.Date;
 import java.util.List;
 import java.util.Vector;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Contract;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.Employment;
 import de.aidger.model.models.FinancialCategory;
 import de.aidger.model.reports.BalanceCourse;
 import de.aidger.model.reports.BalanceFilter;
 import de.aidger.model.reports.BalanceCourse.BudgetCost;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.ICourse;
 
 /**
  * @author Phil
  * 
  */
 public class BalanceHelperTest {
 
     private Course course = null;
 
     private Course course2 = null;
 
     private Course course3 = null;
 
     private Course course4 = null;
 
     private Assistant assistant = null;
 
     private Employment employment1 = null;
 
     private Employment employment2 = null;
 
     private Contract contract = null;
 
     private FinancialCategory financialCategory = null;
 
     private BalanceHelper balanceHelper = null;
 
     private BalanceCourse balanceCourse = null;
 
     private BalanceFilter balanceFilter = null;
 
     public BalanceHelperTest() throws AdoHiveException {
     }
 
     @After
     public void cleanUp() throws AdoHiveException {
 
         course.remove();
 
         course2.remove();
 
         course3.remove();
 
         course4.remove();
 
         assistant.remove();
 
         employment1.remove();
 
         employment2.remove();
 
         contract.remove();
 
         financialCategory.remove();
     }
 
     /**
      * Sets up the Test of the class BalanceHelper.
      * 
      * @throws AdoHiveException
      */
     @Before
     public void setUp() throws AdoHiveException {
         de.aidger.model.Runtime.getInstance().initialize();
 
         financialCategory = new FinancialCategory();
         financialCategory.setBudgetCosts(new int[] { 1000 });
         financialCategory.setFunds(new int[] { 10000000 });
         financialCategory.setName("Test Category");
         financialCategory.setYear((short) 2010);
         financialCategory.save();
 
         course = new Course();
         course.setAdvisor("Tester");
         course.setDescription("Description");
         course.setFinancialCategoryId(financialCategory.getId());
         course.setGroup("2");
         course.setLecturer("Test Tester");
         course.setNumberOfGroups(3);
         course.setPart('a');
         course.setRemark("Remark");
         course.setScope("Sniper Scope");
         course.setSemester("SS09");
         course.setTargetAudience("Testers");
         course.setUnqualifiedWorkingHours(100);
         course.save();
 
         course2 = course.clone();
         course2.setLecturer("Test Tester 2");
         course2.setNew(true);
         course2.save();
 
         course3 = course.clone();
         course3.setTargetAudience("Testers 2");
         course3.setNew(true);
         course3.save();
 
         course4 = course.clone();
         course4.setGroup("Test group 2");
         course4.setNew(true);
         course4.save();
 
         assistant = new Assistant();
         assistant.setEmail("test@example.com");
         assistant.setFirstName("Test");
         assistant.setLastName("Tester");
         assistant.setQualification("g");
         assistant.save();
 
         contract = new Contract();
         contract.setNew(true);
         contract.setStartDate(new Date(1970, 1, 1));
         contract.setCompletionDate(new Date(1970, 1, 3));
         contract.setConfirmationDate(new Date(1970, 1, 2));
         contract.setEndDate(new Date(1970, 1, 4));
         contract.setDelegation(true);
         contract.setType("Test type");
         contract.save();
 
         employment1 = new Employment();
         employment1.setAssistantId(assistant.getId());
         employment1.setCourseId(course.getId());
         employment1.setFunds(1);
         employment1.setHourCount(10.0);
         employment1.setContractId(contract.getId());
         employment1.setCostUnit("Test unit");
         employment1.setMonth((byte) 1);
         employment1.setQualification("g");
         employment1.setRemark("Test remark");
         employment1.setYear((short) 1970);
         employment1.setNew(true);
         employment1.save();
 
         employment2 = new Employment();
         employment2.setAssistantId(assistant.getId());
         employment2.setCourseId(course.getId());
         employment2.setFunds(2);
         employment2.setHourCount(10.0);
         employment2.setContractId(contract.getId());
         employment2.setCostUnit("Test unit");
         employment2.setMonth((byte) 1);
         employment2.setQualification("g");
         employment2.setRemark("Test remark");
         employment2.setYear((short) 1970);
         employment2.setNew(true);
         employment2.save();
 
         balanceCourse = new BalanceCourse();
         balanceCourse.setTitle("Description");
         balanceCourse.setLecturer("Test Tester");
         balanceCourse.setBasicAWS(course.getNumberOfGroups()
                 * course.getUnqualifiedWorkingHours());
         balanceCourse.setPart('a');
         balanceCourse.setPlannedAWS(employment1.getHourCount()
                 + employment2.getHourCount());
         balanceCourse.setTargetAudience("Testers");
         balanceCourse.addBudgetCost(employment1.getFunds(), employment1
             .getCostUnit(), 120);
         balanceCourse.addBudgetCost(employment2.getFunds(), employment2
             .getCostUnit(), 120);
     }
 
     /**
      * Tests the constructor of the class BalanceHelper.
      */
     @Test
     public void testConstructor() {
         System.out.println("Constructor");
 
         balanceHelper = new BalanceHelper();
     }
 
     /**
      * Test the method filterCourses() of class BalanceHelper.
      * 
      * @throws AdoHiveException
      */
     @Test
     public void testFilterCourses() throws AdoHiveException {
         System.out.println("filterCourses()");
 
         balanceHelper = new BalanceHelper();
 
         List<ICourse> courses = (new Course()).getAll();
 
         /*
          * The course should exist in the course list.
          */
         boolean resultBoolean = false;
         for (ICourse listCourse : courses) {
             if (new Course(listCourse).equals(course)) {
                 System.out.println("----------");
                 System.out.println(listCourse);
                 System.out.println(course);
                 resultBoolean = true;
             }
         }
         assertTrue(resultBoolean);
 
         List<ICourse> filteredCourses = balanceHelper.filterCourses(courses,
             null);
 
         /*
          * The course should exist, after filtering with a null filter.
          */
         resultBoolean = false;
         for (ICourse listCourse : filteredCourses) {
             if (new Course(listCourse).equals(course)) {
                 resultBoolean = true;
             }
         }
         assertTrue(resultBoolean);
 
         balanceFilter = new BalanceFilter();
 
         filteredCourses = balanceHelper.filterCourses(courses, balanceFilter);
 
         /*
          * The course should exist after filtering without any filters.
          */
         resultBoolean = false;
         for (ICourse listCourse : filteredCourses) {
             if (new Course(listCourse).equals(course)) {
                 resultBoolean = true;
             }
         }
         assertTrue(resultBoolean);
 
         balanceFilter = new BalanceFilter();
         balanceFilter.addGroup("Test filter");
 
         filteredCourses = balanceHelper.filterCourses(courses, balanceFilter);
 
         /*
          * The course should not exist after filtering with a filter that it
          * doesn't contain.
          */
         resultBoolean = false;
         for (ICourse listCourse : filteredCourses) {
             if (new Course(listCourse).equals(course)) {
                 resultBoolean = true;
             }
         }
         assertTrue(!resultBoolean);
 
         balanceFilter = new BalanceFilter();
         balanceFilter.addLecturer("Test filter");
 
         filteredCourses = balanceHelper.filterCourses(courses, balanceFilter);
 
         /*
          * The course should not exist after filtering with a filter that it
          * doesn't contain.
          */
         resultBoolean = false;
         for (ICourse listCourse : filteredCourses) {
             if (new Course(listCourse).equals(course)) {
                 resultBoolean = true;
             }
         }
         assertTrue(!resultBoolean);
 
         balanceFilter = new BalanceFilter();
         balanceFilter.addTargetAudience("Test filter");
 
         filteredCourses = balanceHelper.filterCourses(courses, balanceFilter);
 
         /*
          * The course should not exist after filtering with a filter that it
          * doesn't contain.
          */
         resultBoolean = false;
         for (ICourse listCourse : filteredCourses) {
             if (new Course(listCourse).equals(course)) {
                 resultBoolean = true;
             }
         }
         assertTrue(!resultBoolean);
 
         balanceFilter = new BalanceFilter();
         balanceFilter.addGroup(course.getGroup());
         balanceFilter.addGroup(course2.getGroup());
         balanceFilter.addGroup(course3.getGroup());
 
         balanceFilter.addLecturer(course.getLecturer());
         balanceFilter.addLecturer(course2.getLecturer());
         balanceFilter.addLecturer(course3.getLecturer());
 
         balanceFilter.addTargetAudience(course.getTargetAudience());
         balanceFilter.addTargetAudience(course2.getTargetAudience());
         balanceFilter.addTargetAudience(course3.getTargetAudience());
 
         filteredCourses = balanceHelper.filterCourses(courses, balanceFilter);
 
         /*
          * The first course should exist after filtering with filters from its
          * course data.
          */
         resultBoolean = false;
         for (ICourse listCourse : filteredCourses) {
             if (new Course(listCourse).equals(course)) {
                 resultBoolean = true;
             }
         }
         assertTrue(resultBoolean);
 
         /*
          * The second course should exist after filtering with filters from its
          * course data.
          */
         resultBoolean = false;
         for (ICourse listCourse : filteredCourses) {
             if (new Course(listCourse).equals(course2)) {
                 resultBoolean = true;
             }
         }
         assertTrue(resultBoolean);
 
         /*
          * The third course should exist after filtering with filters from its
          * course data.
          */
         resultBoolean = false;
         for (ICourse listCourse : filteredCourses) {
             if (new Course(listCourse).equals(course3)) {
                 resultBoolean = true;
             }
         }
         assertTrue(resultBoolean);
     }
 
     /**
      * Tests the method getYearSemesters() of class BalanceHelper.
      */
     @Test
     public void testGetYearSemesters() {
         System.out.println("getYearSemesters()");
         balanceHelper = new BalanceHelper();
 
         String[] years = balanceHelper.getYearSemesters(2000);
 
         assertEquals("2000", years[0]);
         assertEquals("WS9900", years[1]);
         assertEquals("SS00", years[2]);
         assertEquals("WS0001", years[3]);
 
         years = balanceHelper.getYearSemesters(2001);
 
         assertEquals("2001", years[0]);
         assertEquals("WS0001", years[1]);
         assertEquals("SS01", years[2]);
         assertEquals("WS0102", years[3]);
 
         years = balanceHelper.getYearSemesters(2009);
 
         assertEquals("2009", years[0]);
         assertEquals("WS0809", years[1]);
         assertEquals("SS09", years[2]);
         assertEquals("WS0910", years[3]);
 
         years = balanceHelper.getYearSemesters(2010);
 
         assertEquals("2010", years[0]);
         assertEquals("WS0910", years[1]);
         assertEquals("SS10", years[2]);
         assertEquals("WS1011", years[3]);
 
         years = balanceHelper.getYearSemesters(2011);
 
         assertEquals("2011", years[0]);
         assertEquals("WS1011", years[1]);
         assertEquals("SS11", years[2]);
         assertEquals("WS1112", years[3]);
 
         years = balanceHelper.getYearSemesters(2099);
 
         assertEquals("2099", years[0]);
         assertEquals("WS9899", years[1]);
         assertEquals("SS99", years[2]);
         assertEquals("WS9900", years[3]);
     }
 
     /**
      * Tests the method getBalanceCourse() of class BalanceHelper.
      */
     @Test
     public void testGetBalanceCourse() {
         System.out.println("getBalanceCourse()");
 
         balanceHelper = new BalanceHelper();
 
        BalanceCourse result = balanceHelper.getBalanceCourse(course);
 
         assertNotNull(result);
 
         Object[] resultCourseObject = balanceCourse.getCourseObject();
         for (int i = 0; i < resultCourseObject.length - 2; i++) {
             assertEquals(balanceCourse.getCourseObject()[i],
                 resultCourseObject[i]);
         }
         Vector<BudgetCost> resultBudgetCosts = (Vector<BudgetCost>) resultCourseObject[resultCourseObject.length - 1];
         assertEquals(employment1.getFunds(), resultBudgetCosts.get(0).getId());
         assertEquals(employment1.getCostUnit(), resultBudgetCosts.get(0)
             .getName());
         assertEquals(new BigDecimal(120.0).setScale(2), resultBudgetCosts
             .get(0).getValue());
         assertEquals(employment2.getFunds(), resultBudgetCosts.get(1).getId());
         assertEquals(employment2.getCostUnit(), resultBudgetCosts.get(1)
             .getName());
         assertEquals(new BigDecimal(120.0).setScale(2), resultBudgetCosts
             .get(1).getValue());
     }
 
     /**
      * Tests the method getYears().
      * 
      * @throws AdoHiveException
      */
     @Test
     public void testGetYears() throws AdoHiveException {
         System.out.println("getYears()");
 
         balanceHelper = new BalanceHelper();
 
         Course course2 = course.clone();
         course2.setSemester("WS0910");
         course2.setNew(true);
         course2.save();
 
         Course course3 = course.clone();
         course3.setSemester("2008");
         course3.setNew(true);
         course3.save();
 
         Vector years = balanceHelper.getYears();
 
         /*
          * The years specified above should be available.
          */
         assertNotNull(years);
         assertTrue(years.contains(2008));
         assertTrue(years.contains(2009));
         assertTrue(years.contains(2010));
 
         course.clearTable();
 
         /*
          * Without any courses, there shouldn't be any years.
          */
         years = balanceHelper.getYears();
         assertTrue(years.isEmpty());
     }
 
     /**
      * Tests the method getSemesters().
      */
     @Test
     public void testGetSemesters() {
         System.out.println("getSemesters()");
 
         balanceHelper = new BalanceHelper();
 
         Vector semesters = balanceHelper.getSemesters();
 
         assertNotNull(semesters);
     }
 
     /**
      * Tests the method courseExists() of class BalanceHelper.
      */
     @Test
     public void testCourseExists() {
         System.out.println("courseExists()");
 
         balanceHelper = new BalanceHelper();
 
         assertTrue(balanceHelper.courseExists(course.getSemester(), null));
 
         assertTrue(!balanceHelper.courseExists("Test semester", null));
     }
 }
