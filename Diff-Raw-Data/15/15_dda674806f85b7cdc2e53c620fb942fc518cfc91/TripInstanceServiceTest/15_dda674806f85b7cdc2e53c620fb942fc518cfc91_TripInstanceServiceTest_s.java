 package be.kdg.groepi.service;
 
 import be.kdg.groepi.model.*;
 import be.kdg.groepi.utils.CompareUtil;
 import be.kdg.groepi.utils.DateUtil;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static be.kdg.groepi.utils.DateUtil.dateToLong;
 import static org.junit.Assert.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Gregory
  * Date: 12/02/13
  * Time: 15:01
  * To change this template use File | Settings | File Templates.
  */
 public class TripInstanceServiceTest {
 
     private TripInstance tripinstance;
     private Trip trip;
     private User user;
     //private Long tripId;
 
     @Before
     public void beforeEachTest() {
         user = new User("TIMMEH", "TIM@M.EH", "hemmit", dateToLong(4, 5, 2011, 15, 32, 0));
         UserService.createUser(user);
         trip = new Trip("Onze eerste trip", "Hopelijk is deze niet te saai!", true, true, user);// trip aanmaken
         TripService.createTrip(trip);
         long startDate = DateUtil.dateToLong(27, 02, 2013, 16, 00, 00);
         long endDate = DateUtil.dateToLong(27, 02, 2013, 20, 00, 00);
         tripinstance = new TripInstance("Bachelor feestje", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance);
        tripinstance.addParticipantToTripInstance(user);
        TripInstanceService.updateTripInstance(tripinstance);
     }
 
     @After
     public void afterEachTest() {
         tripinstance = null;
         for (TripInstance tempTripInstance : TripInstanceService.getAllTripInstances()) {
             TripInstanceService.deleteTripInstance(tempTripInstance);
         }
     }
 
     @Test
     public void createTripInstance() {
         assertEquals("createTripInstance: ", tripinstance, TripInstanceService.getTripInstanceById(tripinstance.getId()));
     }
 
     @Test
     public void updateTripInstances() {
         tripinstance.setAvailable(Boolean.FALSE);
         tripinstance.setDescription("Ho-ho-ho edited");
 
         TripInstanceService.updateTripInstance(tripinstance);
         assertEquals("updateTripInstances: ", tripinstance, TripInstanceService.getTripInstanceById(tripinstance.getId()));
     }
 
     @Test
     public void deleteTripInstance() {
         assertNotNull("deleteTripInstance: Trip found", TripInstanceService.getTripInstanceById(tripinstance.getId()));
         TripInstanceService.deleteTripInstance(tripinstance);
         assertNull("deleteTripInstance: TripInstance not found", TripInstanceService.getTripInstanceById(tripinstance.getId()));
     }
 
     @Test
     public void addAndRemoveUserToTripInstance() {
         assertTrue("TripInstance: tripInstance should have no participants", tripinstance.getParticipants().isEmpty());
         tripinstance.addParticipantToTripInstance(user);
         assertFalse("TripInstance: tripinstance should have participants", tripinstance.getParticipants().isEmpty());
         tripinstance.removeParticipantFromTripInstance(user);
         assertTrue("TripInstance: tripinstance should have no participants", tripinstance.getParticipants().isEmpty());
     }
 
     @Test
     public void addAndRemoveCostToTripInstance() {
         //assertTrue("TripInstance: tripInstance should have no costs", tripinstance.getCosts().isEmpty());
         Cost cost = new Cost("BEN's cost", 35.53, tripinstance, user);
         CostService.createCost(cost);
         tripinstance.addCostToTripInstance(cost);
         assertFalse("TripInstance: tripinstance should have costs", tripinstance.getCosts().isEmpty());
         tripinstance.removeCostFromTripInstance(cost);
         assertTrue("TripInstance: tripinstance should have no costs", tripinstance.getCosts().isEmpty());
     }
 
     @Test
     public void addAndRemoveRequirementInstanceToTripInstance() {
         assertTrue("TripInstance: tripInstance should have no requirementInstances", tripinstance.getRequirementInstances().isEmpty());
         RequirementInstance requirementInstance = new RequirementInstance("BEN", 5, "descri", tripinstance);
         RequirementInstanceService.createRequirementInstance(requirementInstance);
         tripinstance.addRequirementInstanceToTripInstance(requirementInstance);
         assertFalse("TripInstance: tripinstance should have requirementInstances", tripinstance.getRequirementInstances().isEmpty());
         tripinstance.removeRequirementInstanceFromTripInstance(requirementInstance);
         assertTrue("TripInstance: tripinstance should have no requirementInstances", tripinstance.getRequirementInstances().isEmpty());
     }
 
     @Test
     public void addAndRemoveMessageToTripInstance() {
         assertTrue("TripInstance: tripinstance should have no messages", tripinstance.getMessages().isEmpty());
         Message message = new Message("BEN's message", dateToLong(12, 10, 1990, 8, 17, 35), tripinstance, user);
         MessageService.createMessage(message);
         tripinstance.addMessageToTripInstance(message);
         assertFalse("TripInstance: tripInstance should have messages", tripinstance.getMessages().isEmpty());
         tripinstance.removeMessageFromTripInstance(message);
         assertTrue("TripInstance: tripinstance should have no messages", tripinstance.getMessages().isEmpty());
     }
 
     @Test
     public void getAllTripInstances() {
         long startDate = DateUtil.dateToLong(27, 02, 2013, 16, 00, 00);
         long endDate = DateUtil.dateToLong(27, 02, 2013, 20, 00, 00);
         TripInstance tripinstance1 = new TripInstance("Bachelor feestje 1", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance1);
         TripInstance tripinstance2 = new TripInstance("Bachelor feestje 2", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance2);
         TripInstance tripinstance3 = new TripInstance("Bachelor feestje 3", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance3);
         List<TripInstance> tripList = new ArrayList<>();
         tripList.add(tripinstance1);
         tripList.add(tripinstance2);
         tripList.add(tripinstance3);
         assertFalse("TripInstance: tripList does not contain the tripInstances it should", CompareUtil.compareList(TripInstanceService.getAllTripInstances(), tripList));
     }
 
     @Test
     public void getAllTripInstancesByTripId() {
         long startDate = DateUtil.dateToLong(27, 02, 2013, 16, 00, 00);
         long endDate = DateUtil.dateToLong(27, 02, 2013, 20, 00, 00);
         TripInstance tripinstance1 = new TripInstance("Bachelor feestje 1", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance1);
         TripInstance tripinstance2 = new TripInstance("Bachelor feestje 2", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance2);
         TripInstance tripinstance3 = new TripInstance("Bachelor feestje 3", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance3);
         List<TripInstance> tripList = new ArrayList<>();
         tripList.add(tripinstance1);
         tripList.add(tripinstance2);
         tripList.add(tripinstance3);
         assertFalse("TripInstance: tripList does not contain the tripInstances it should", CompareUtil.compareList(TripInstanceService.getAllTripInstancesByTripId(trip.getId()), tripList));
     }
 
     @Test
     public void getTripInstancesByUserId() {
         List<User> users = UserService.getAllUsers();
         User testUser = users.get(0);
         List<TripInstance> trips = TripInstanceService.getTripInstancesByUserId(testUser.getId());
        assertTrue("There should be trips", !trips.isEmpty());
     }
 
     @Test
     public void testCompareTripInstance() {
         long startDate = DateUtil.dateToLong(27, 02, 2013, 16, 00, 00);
         long endDate = DateUtil.dateToLong(27, 02, 2013, 20, 00, 00);
         TripInstance tripinstance2 = new TripInstance("Bachelor feestje", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance2);
         assertTrue("Stops should be the same", tripinstance.equals(tripinstance2));
     }
 
     @Test
     public void testCompareTripInstanceNullObject() {
         TripInstance tripinstance2 = null;
         assertFalse("Stops should not be the same", tripinstance.equals(tripinstance2));
     }
 
     @Test
     public void testCompareTripInstanceTitle() {
         long startDate = DateUtil.dateToLong(27, 02, 2013, 16, 00, 00);
         long endDate = DateUtil.dateToLong(27, 02, 2013, 20, 00, 00);
         TripInstance tripinstance2 = new TripInstance("Bachelor feestje2", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance2);
         assertFalse("Stops should  not be the same", tripinstance.equals(tripinstance2));
     }
 
     @Test
     public void testCompareTripInstanceDescription() {
         long startDate = DateUtil.dateToLong(27, 02, 2013, 16, 00, 00);
         long endDate = DateUtil.dateToLong(27, 02, 2013, 20, 00, 00);
         TripInstance tripinstance2 = new TripInstance("Bachelor feestje", "Iemand gaat trouwen, 2bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance2);
         assertFalse("Stops should  not be the same", tripinstance.equals(tripinstance2));
     }
 
     @Test
     public void testCompareTripInstanceAvailable() {
         long startDate = DateUtil.dateToLong(27, 02, 2013, 16, 00, 00);
         long endDate = DateUtil.dateToLong(27, 02, 2013, 20, 00, 00);
         TripInstance tripinstance2 = new TripInstance("Bachelor feestje", "Iemand gaat trouwen, bier en vrouwen ole", true, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance2);
         assertFalse("Stops should  not be the same", tripinstance.equals(tripinstance2));
     }
 
     @Test
     public void testCompareTripInstanceStartDate() {
         long startDate = DateUtil.dateToLong(27, 02, 2013, 16, 10, 00);
         long endDate = DateUtil.dateToLong(27, 02, 2013, 20, 00, 00);
         TripInstance tripinstance2 = new TripInstance("Bachelor feestje", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance2);
         assertFalse("Stops should  not be the same", tripinstance.equals(tripinstance2));
     }
 
     @Test
     public void testCompareTripInstanceEndDate() {
         long startDate = DateUtil.dateToLong(27, 02, 2013, 16, 00, 00);
         long endDate = DateUtil.dateToLong(27, 02, 2013, 22, 00, 00);
         TripInstance tripinstance2 = new TripInstance("Bachelor feestje", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(tripinstance2);
         assertFalse("Stops should  not be the same", tripinstance.equals(tripinstance2));
     }
 
     @Test
     public void testCompareTripInstanceOrganiser() {
         long startDate = DateUtil.dateToLong(27, 02, 2013, 16, 00, 00);
         long endDate = DateUtil.dateToLong(27, 02, 2013, 20, 00, 00);
         TripInstance tripinstance2 = new TripInstance("Bachelor feestje", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, null, trip);
         TripInstanceService.createTripInstance(tripinstance2);
         assertFalse("Stops should  not be the same", tripinstance.equals(tripinstance2));
     }
 
     @Test
     public void testCompareTripInstanceTrip() {
         long startDate = DateUtil.dateToLong(27, 02, 2013, 16, 00, 00);
         long endDate = DateUtil.dateToLong(27, 02, 2013, 20, 00, 00);
         TripInstance tripinstance2 = new TripInstance("Bachelor feestje", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, null);
         TripInstanceService.createTripInstance(tripinstance2);
         assertFalse("Stops should  not be the same", tripinstance.equals(tripinstance2));
     }
 
     @Test
     public void getPublicTripInstances() {
         int size = TripInstanceService.getPublicTripInstances().size();
         long startDate = DateUtil.dateToLong(27, 2, 2013, 16, 0, 0);
         long endDate = DateUtil.dateToLong(27, 2, 2013, 20, 0, 0);
         TripInstance newFalseTripInstance = new TripInstance("falsetripInstance", "mdahnebzalem", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(newFalseTripInstance);
         TripInstance newTrueTripInstance = new TripInstance("truetripInstance", "first false tripInstance in test", true, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(newTrueTripInstance);
         List<TripInstance> tripInstances = TripInstanceService.getPublicTripInstances();
         assertTrue("getPublicTripInstances: didn't receive public-only tripInstances", tripInstances.size() == size + 1);
     }
 
     @Test
     public void getTripInstancesByOrganiserId() {
         int size = TripInstanceService.getTripInstancesByOrganiserId(user.getId()).size();
         long startDate = DateUtil.dateToLong(27, 2, 2013, 16, 0, 0);
         long endDate = DateUtil.dateToLong(27, 2, 2013, 20, 0, 0);
         TripInstance oldOrganiserTripInstance = new TripInstance("Bachelor feestje", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, user, trip);
         TripInstanceService.createTripInstance(oldOrganiserTripInstance);
 
         User newUser = new User("newUser", "new@us.er", "yitfluyfkytfglkyu", DateUtil.dateToLong(15, 7, 1992, 0, 0, 0));
         UserService.createUser(newUser);
         TripInstance newOrganiserTripInstance = new TripInstance("Bachelor feestje", "Iemand gaat trouwen, bier en vrouwen ole", false, startDate, endDate, newUser, trip);
         TripInstanceService.createTripInstance(newOrganiserTripInstance);
 
         List<TripInstance> tripInstances = TripInstanceService.getTripInstancesByOrganiserId(user.getId());
         assertTrue("getPublicTripInstances: didn't receive user-only tripInstances", tripInstances.size() == size + 1);
     }
 }
