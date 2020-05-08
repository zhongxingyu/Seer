 package at.ac.tuwien.sepm.service.impl;
 
 import at.ac.tuwien.sepm.entity.DateEntity;
 import at.ac.tuwien.sepm.service.DateService;
 import at.ac.tuwien.sepm.service.Semester;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.service.TimeFrame;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * Author: Flo
  */
 public class DateServiceTest {
     private DateEntity validDateEntity;
     private DateService dateService;
 
     @Before
     public void setUp() {
         dateService = new DateServiceImpl();
         validDateEntity = new DateEntity();
         validDateEntity.setName("valid name");
         validDateEntity.setDescription("valid description");
         validDateEntity.setId(3);
         validDateEntity.setIntersectable(true);
         validDateEntity.setTime(new TimeFrame(new DateTime(2000, 1, 1, 1, 1), new DateTime(2002, 2, 2, 2, 2)));
     }
 
     @Test(expected = ServiceException.class)
     public void testCreateDateINVALID() throws Exception {
         dateService.createDate(null);
     }
 
     @Test(expected = ServiceException.class)
     public void testUpdateDateINVALID() throws Exception {
         dateService.updateDate(null);
     }
 
     @Test(expected = ServiceException.class)
     public void testDeleteDateINVALID() throws Exception {
         dateService.deleteDate(-1);
     }
 
     @Test(expected = ServiceException.class)
     public void testReadDateByIdINVALID() throws Exception {
         dateService.readDateById(-1);
     }
 
     @Test(expected = ServiceException.class)
     public void testReadDateInTimeframeINVALID() throws Exception {
         dateService.readDateInTimeframe(null, null);
     }
 
     @Test
     public void testValidateDateEntityVALID() throws Exception {
         dateService.validateDateEntity(validDateEntity);
     }
 
     @Test(expected = ServiceException.class)
     public void testValidateDateEntityINVALIDid() throws Exception {
         validDateEntity.setId(-1);
        dateService.validateDateEntity(validDateEntity);
     }
 
     @Test(expected = ServiceException.class)
     public void testValidateDateEntityINVALIDname() throws Exception {
         validDateEntity.setName(null);
         dateService.validateDateEntity(validDateEntity);
     }
 
     @Test(expected = ServiceException.class)
     public void testValidateDateEntityINVALIDdescription() throws Exception {
         validDateEntity.setDescription(null);
         dateService.validateDateEntity(validDateEntity);
     }
 
     @Test(expected = ServiceException.class)
     public void testValidateDateEntityINVALIDintersectable() throws Exception {
         validDateEntity.setIntersectable(null);
         dateService.validateDateEntity(validDateEntity);
     }
 
     @Test(expected = ServiceException.class)
     public void testValidateDateEntityINVALIDstop() throws Exception {
         validDateEntity.setTime(null);
         dateService.validateDateEntity(validDateEntity);
     }
 
     @Test
     public void testValidateIdVALID() throws Exception {
         dateService.validateId(1);
     }
 
     @Test(expected = ServiceException.class)
     public void testValidateIdINVALID() throws Exception {
         dateService.validateId(-1);
     }
 
     @Test
     public void testValidateDateVALID() throws Exception {
         dateService.validateDates(validDateEntity.getStart(), validDateEntity.getStop());
     }
 
     @Test(expected = ServiceException.class)
     public void testValidateDateINVALIDswitched() throws Exception {
         dateService.validateDates(validDateEntity.getStop(), validDateEntity.getStart());
     }
 
     @Test(expected = ServiceException.class)
     public void testValidateDateINVALIDfrom() throws Exception {
         dateService.validateDates(null, validDateEntity.getStop());
     }
 
     @Test(expected = ServiceException.class)
     public void testValidateDateINVALIDto() throws Exception {
         dateService.validateDates(validDateEntity.getStart(), null);
     }
     @Test
     public void checkIfYearIsGreaterZero() throws Exception{
         assertTrue(dateService.getCurrentYear() > 0);
     }
     @Test
     public void checkIfSemesterIsSOrW() throws Exception{
         assertTrue(dateService.getCurrentSemester().equals(Semester.W)|| dateService.getCurrentSemester().equals(Semester.S));
     }
 
 
 }
