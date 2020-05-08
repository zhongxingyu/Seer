 package edu.northwestern.bioinformatics.studycalendar.dao;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 
 /**
  * @author Rhett Sutphin
  */
 public class PlannedEventDaoTest extends ContextDaoTestCase<PlannedEventDao> {
     public void testGetById() throws Exception {
         PlannedEvent loaded = getDao().getById(-12);
 
         assertEquals("Wrong id", -12, (int) loaded.getId());
         assertEquals("Wrong day number", new Integer(4), loaded.getDay());
         assertNotNull("Period not loaded", loaded.getPeriod());
         assertEquals("Wrong period", -300L, (long) loaded.getPeriod().getId());
         assertNotNull("Activity not loaded", loaded.getActivity());
         assertEquals("Wrong activity", -200L, (long) loaded.getActivity().getId());
     }
 
     public void testPeriodBidirectional() throws Exception {
         PlannedEvent loaded = getDao().getById(-12);
         assertTrue(loaded.getPeriod().getPlannedEvents().contains(loaded));
     }
 
     public void testSaveDetached() throws Exception {
         Integer id;
         {
             PlannedEvent plannedEvent = new PlannedEvent();
             plannedEvent.setDay(5);
             getDao().save(plannedEvent);
             assertNotNull("not saved", plannedEvent.getId());
             id = plannedEvent.getId();
         }
 
         interruptSession();
 
         PlannedEvent loaded = getDao().getById(id);
         assertNotNull("Could not reload", loaded);
         assertEquals("Wrong event loaded", 5, (int) loaded.getDay());
     }
 }
