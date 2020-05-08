 package Hospital.Schedules.Constraints.Implementation;
 
 import Hospital.Schedules.ScheduleGroups.ScheduleGroup;
 import Hospital.World.Time;
 import Hospital.Exception.Command.CannotDoException;
 import Hospital.Schedules.AppointmentCommand;
 import Hospital.Schedules.Constraints.Priority.HighLowPriority;
 import Hospital.Schedules.ScheduleGroups.SingleSchedulableGroup;
 import java.util.Arrays;
 import Hospital.Exception.Arguments.ArgumentConstraintException;
 import Hospital.Exception.Arguments.ArgumentIsNullException;
 import Hospital.Exception.NoPersonWithNameAndRoleException;
 import Hospital.Patient.Patient;
 import Hospital.People.Nurse;
 import Hospital.Treatments.Medication;
 import Hospital.Treatments.Treatment;
 import Hospital.World.BasicWorld;
 import Hospital.World.World;
 import Hospital.Schedules.TimeFrame;
 import Hospital.WareHouse.ItemInfo;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author rupsbant
  */
 public class UnmovableConstraintTest {
 
     World w = BasicWorld.getWorldForTesting();
     Patient ruben;
     Patient jeroen;
     Nurse nurse;
 
     public UnmovableConstraintTest() throws ArgumentConstraintException, ArgumentIsNullException, NoPersonWithNameAndRoleException {
         this.ruben = new Patient("Ruben");
         this.jeroen = new Patient("Jeroen");
         this.nurse = w.getPersonByName(Nurse.class, "Nurse Joy");
     }
 
     @Before
     public void setUp() throws ArgumentIsNullException, CannotDoException {
         Treatment t = new Medication("Medication1", Boolean.TRUE, new ItemInfo[0]);
         ScheduleGroup scheduleGroup = new SingleSchedulableGroup(ruben);
         AppointmentCommand appC = new AppointmentCommand(w, t, Arrays.asList(scheduleGroup), new HighLowPriority(true));
         appC.execute();
 
         scheduleGroup = new SingleSchedulableGroup(ruben);
         t = new Medication("Medication2", Boolean.TRUE, new ItemInfo[0]);
         appC = new AppointmentCommand(w, t, Arrays.asList(scheduleGroup), new HighLowPriority(true));
         appC.execute();
 
         scheduleGroup = new SingleSchedulableGroup(jeroen);
         t = new Medication("Medication3", Boolean.TRUE, new ItemInfo[0]);
         appC = new AppointmentCommand(w, t, Arrays.asList(scheduleGroup), new HighLowPriority(true));
         appC.execute();
 
         scheduleGroup = new SingleSchedulableGroup(jeroen);
         t = new Medication("Medication4", Boolean.TRUE, new ItemInfo[0]);
         appC = new AppointmentCommand(w, t, Arrays.asList(scheduleGroup), new HighLowPriority(true));
         appC.execute();
     }
 
     /**
      * Test of setValidSchedulable method, of class UnmovableConstraint.
      */
     @Test
     public void testSetValidSchedulableNurse() throws ArgumentIsNullException, ArgumentConstraintException {
         UnmovableConstraint instance = new UnmovableConstraint(nurse);
 
         TimeFrame tf = new TimeFrame(new Time(2011, 11, 8, 9, 0), 20);
         instance.resetValid();
         instance.setValidSchedulable(nurse);
         instance.setValidTimeFrame(tf);
         assertTrue("Can always move instantly on campus", instance.acceptAll());
 
         tf = new TimeFrame(new Time(2011, 11, 8, 9, 1), 20);
         instance.resetValid();
         instance.setValidSchedulable(nurse);
         instance.setValidTimeFrame(tf);
         assertTrue("Can always move instantly on campus", instance.acceptAll());
 
         tf = new TimeFrame(new Time(2011, 11, 8, 9, 19), 20);
         instance.resetValid();
         instance.setValidSchedulable(nurse);
         instance.setValidTimeFrame(tf);
         assertTrue("Can always move instantly on campus", instance.acceptAll());
 
         tf = new TimeFrame(new Time(2011, 11, 8, 9, 20), 20);
         instance.resetValid();
         instance.setValidSchedulable(nurse);
         instance.setValidTimeFrame(tf);
         assertTrue("Can always move instantly on campus", instance.acceptAll());
 
         tf = new TimeFrame(new Time(2011, 11, 8, 9, 21), 20);
         instance.resetValid();
         instance.setValidSchedulable(nurse);
         instance.setValidTimeFrame(tf);
         assertTrue("Can always move instantly on campus", instance.acceptAll());
     }
 
     @Test
     public void testSetValidSchedulableRuben() throws ArgumentIsNullException, ArgumentConstraintException {
         UnmovableConstraint instance = new UnmovableConstraint(nurse);
         TimeFrame tf = new TimeFrame(new Time(2011, 11, 8, 9, 0), 20);
         instance.resetValid();
         instance.setValidSchedulable(ruben);
         instance.setValidTimeFrame(tf);
         assertTrue("Can always move instantly on campus", instance.acceptAll());
 
         tf = new TimeFrame(new Time(2011, 11, 8, 9, 1), 20);
         instance.resetValid();
         instance.setValidSchedulable(ruben);
         instance.setValidTimeFrame(tf);
         assertTrue("Can always move instantly on campus", instance.acceptAll());
 
         tf = new TimeFrame(new Time(2011, 11, 8, 9, 19), 20);
         instance.resetValid();
         instance.setValidSchedulable(ruben);
         instance.setValidTimeFrame(tf);
         assertTrue("Can always move instantly on campus", instance.acceptAll());
 
         tf = new TimeFrame(new Time(2011, 11, 8, 9, 20), 20);
         instance.resetValid();
         instance.setValidSchedulable(ruben);
         instance.setValidTimeFrame(tf);
         assertTrue("Can always move instantly on campus", instance.acceptAll());
 
         tf = new TimeFrame(new Time(2011, 11, 8, 9, 21), 20);
         instance.resetValid();
         instance.setValidSchedulable(ruben);
         instance.setValidTimeFrame(tf);
         assertTrue("Can always move instantly on campus", instance.acceptAll());
     }
 
     @Test
     public void testSetValidSchedulableJeroen() throws ArgumentIsNullException, ArgumentConstraintException {
         UnmovableConstraint instance = new UnmovableConstraint(nurse);
         TimeFrame tf = new TimeFrame(new Time(2011, 11, 8, 9, 21), 20);
         instance.resetValid();
         instance.setValidSchedulable(jeroen);
         instance.setValidTimeFrame(tf);
         assertFalse("Can not move instantly between campuses", instance.acceptAll());
         
         tf = new TimeFrame(new Time(2011, 11, 8, 9, 55), 20);
         instance.resetValid();
         instance.setValidSchedulable(jeroen);
         instance.setValidTimeFrame(tf);
         assertTrue("Enough time...", instance.acceptAll());
 
         tf = new TimeFrame(new Time(2011, 11, 8, 9, 54), 20);
         instance.resetValid();
         instance.setValidSchedulable(jeroen);
         instance.setValidTimeFrame(tf);
         assertFalse("Not enough time...", instance.acceptAll());
     }
 
     /**
      * Test of resetValid method, of class UnmovableConstraint.
      */
     @Test
     public void testResetValid() {
         UnmovableConstraint instance = new UnmovableConstraint(null);
         instance.resetValid();
         assertEquals(true, instance.acceptAll());
     }
 }
