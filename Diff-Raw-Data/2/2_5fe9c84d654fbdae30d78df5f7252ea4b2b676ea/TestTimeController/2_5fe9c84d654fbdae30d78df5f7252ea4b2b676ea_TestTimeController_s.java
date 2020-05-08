 package ch.hsr.waktu.controller;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.trolltech.qt.core.QDate;
 
 import ch.hsr.waktu.controller.datacontroller.ProjectController;
 import ch.hsr.waktu.controller.datacontroller.ProjectControllerXml;
 import ch.hsr.waktu.controller.datacontroller.UserController;
 import ch.hsr.waktu.controller.datacontroller.UserControllerXml;
 import ch.hsr.waktu.controller.datacontroller.WorkPackageController;
 import ch.hsr.waktu.controller.datacontroller.WorkPackageControllerXml;
 import ch.hsr.waktu.controller.datacontroller.WorkSessionController;
 import ch.hsr.waktu.controller.datacontroller.WorkSessionControllerXml;
 import ch.hsr.waktu.domain.Project;
 import ch.hsr.waktu.domain.Usr;
 import ch.hsr.waktu.domain.WorkPackage;
 import ch.hsr.waktu.services.WaktuException;
 
 public class TestTimeController {
 	
 	private UserController previousUserController;
 	private WorkSessionController previousWorkSessionController;
 	private ProjectController previousProjectController;
 	private WorkPackageController previousWorkPackageController;
 	
 	@Before
 	public void before() {
 		storePreviousControllers();
 		setFakeControllers();
 	}
 	
 	private void storePreviousControllers() {
 		previousUserController = UserController.getInstance();
 		previousWorkSessionController = WorkSessionController.getInstance();
 		previousProjectController = ProjectController.getInstance();
 		previousWorkPackageController = WorkPackageController.getInstance();
 	}
 	
 	private void setFakeControllers() {
 		UserController.setInstance(new UserControllerXml());
 		WorkSessionController.setInstance(new WorkSessionControllerXml());
 		ProjectController.setInstance(new ProjectControllerXml());
 		WorkPackageController.setInstance(new WorkPackageControllerXml());
 	}
 	
 	@After
 	public void restorePreviousControllers() {
 		UserController.setInstance(previousUserController);
 		WorkSessionController.setInstance(previousWorkSessionController);
 		ProjectController.setInstance(previousProjectController);
 		WorkPackageController.setInstance(previousWorkPackageController);
 	}
 	
 	@Test
     public void calculateWorktimeForUser() throws WaktuException {
         Usr user = UserController.getInstance().getUser("patriziaheer");
         double time = TimeController.calculateWorktime(user, null, null,  null, null);
         assertEquals(11.0 , time, 0.01);
     }
 	
 	@Test
 	public void calculateWorktimeForProjectForUser() throws WaktuException {
 		Project project = ProjectController.getInstance().getProject("Waktu");
 		Usr user = UserController.getInstance().getUser("patriziaheer");
 		double time = TimeController.calculateWorktime(user, project, null,  null, null);
 		assertEquals(6.0 , time, 0.01);
 	}
 	
 	@Test
     public void calculateWorktimeForWorkPackageForUser() throws WaktuException {
         WorkPackage workPackage = WorkPackageController.getInstance().getWorkPackage("Domain");
         Usr user = UserController.getInstance().getUser("patriziaheer");
         double time = TimeController.calculateWorktime(user, null, workPackage,  null, null);
         assertEquals(3.0 , time, 0.01);
     }
 	
 	@Test
     public void calculateWorktimeForWorkPackage() throws WaktuException {
         WorkPackage workPackage = WorkPackageController.getInstance().getWorkPackage("Domain");
         double time = TimeController.calculateWorktime(null, null, workPackage,  null, null);
         assertEquals(10.0 , time, 0.01);
     }
 	
 	@Test
     public void calculateWorktimeForProjectForWorkPackageForUser_workPackageNotPartOfProject() throws WaktuException {
         WorkPackage workPackage = WorkPackageController.getInstance().getWorkPackage("Domain");
         Project project = ProjectController.getInstance().getProject("SBB");
         Usr user = UserController.getInstance().getUser("patriziaheer");
         double time = TimeController.calculateWorktime(user, project, workPackage,  null, null);
         assertEquals(0.0 , time, 0.01);
     }
 	
 	@Test
     public void calculateWorktimeForProject() throws WaktuException {
         Project project = ProjectController.getInstance().getProject("Waktu");
         double time = TimeController.calculateWorktime(null, project, null,  null, null);
         assertEquals(31.0 , time, 0.01);
     }
 	
 	@Test
     public void calculateWorktime_illegalInput() throws WaktuException {
         double time = TimeController.calculateWorktime(null, null, null,  null, null);
         assertEquals(0.0 , time, 0.01);
     }
 	
 	@Test
     public void calculateWorktimeForWeek() throws WaktuException {
         Usr user = UserController.getInstance().getUser("patriziaheer");
         double time = TimeController.calculateWorktimeForWeek(user, new QDate(2011, 05, 17));
         assertEquals(8.0 , time, 0.01);
     }
 	
 	@Test
     public void calculateWorktimeForMonth() throws WaktuException {
         Usr user = UserController.getInstance().getUser("patriziaheer");
         double time = TimeController.calculateWorktimeForMonth(user, new QDate(2011, 06, 17));
         assertEquals(3.0 , time, 0.01);
     }
 	
 	@Test
     public void calculateWorktimeForTimespan() throws WaktuException {
         Usr user = UserController.getInstance().getUser("patriziaheer");
         double time = TimeController.calculateWorktime(user, null, null, new QDate(2011, 05, 17), new QDate(2011, 06, 18));
         assertEquals(10.0 , time, 0.01);
     }
 	
 	@Test
     public void getPlannedTime_5Weeks() throws WaktuException {
         Usr user = UserController.getInstance().getUser("patriziaheer");
         double time = TimeController.getPlannedTime(user, new QDate(2011, 05, 16), new QDate(2011, 06, 19));
         assertEquals(TimeController.HOURS_PER_WORKDAY * 25 * user.getPensum()/100 , time, 0.01);
     }
 	
 	@Test
     public void getPlannedTimeForMonth_monthWith4Weekends() throws WaktuException {
         Usr user = UserController.getInstance().getUser("patriziaheer");
         double time = TimeController.getPlannedTime(user, new QDate(2011, 06, 19));
         assertEquals(TimeController.HOURS_PER_WORKDAY * 22 * user.getPensum()/100 , time, 0.01);
     }
 	
 	@Test  
 	public void getPlannedTimeForMonth_monthWith5Weekends() throws WaktuException {
 	    Usr user = UserController.getInstance().getUser("patriziaheer");
 	    double time = TimeController.getPlannedTime(user, new QDate(2011, 05, 19));
	    assertEquals(TimeController.HOURS_PER_WORKDAY * 25 * user.getPensum()/100 , time, 0.01);
 	}
 	
 	@Test  
     public void calculateOvertime_noOvertime() throws WaktuException {
         Usr user = UserController.getInstance().getUser("patriziaheer");
         QDate fromDate = new QDate(2011, 05, 16);
         QDate toDate = new QDate(2011, 05, 16);
         double time = TimeController.calculateOvertime(user, fromDate,  toDate);
         assertEquals(0.0 , time, 0.01);
     }
 	
 	@Test  
     public void calculateOvertime_withOvertime() throws WaktuException {
         Usr user = UserController.getInstance().getUser("simonstaeheli");
         QDate fromDate = new QDate(2011, 10, 17);
         QDate toDate = new QDate(2011, 10, 17);
         double time = TimeController.calculateOvertime(user, fromDate,  toDate);
         assertEquals(18.0 - TimeController.getPlannedTime(user, fromDate, toDate) , time, 0.01);
     }
 }
