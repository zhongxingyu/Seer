 package drinkcounter;
 
 import drinkcounter.authentication.CurrentUser;
import drinkcounter.authentication.AuthenticationChecks;
import drinkcounter.authentication.NotLoggedInException;
 import drinkcounter.authentication.NotEnoughRightsException;
 import drinkcounter.model.Party;
 import java.util.ArrayList;
 import java.util.List;
 import drinkcounter.model.User;
 import drinkcounter.authentication.AuthenticationChecks;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 /**
  *
  * @author murgo
  */
 public class AuthenticationCheckTest {
 
     AuthenticationChecks authenticationChecks;
     
     String openId = "asd";
     User user;
     User user2;
     User user3;
     
     @Before
     public void setUp() {
         authenticationChecks = new AuthenticationChecks();
         
         user = new User();
         user.setId(1);
 
         user2 = new User();
         user2.setId(2);
         
         user3 = new User();
         user3.setId(3);
 
         Party party = new Party();
         party.setId(1);
         party.addParticipant(user);
         party.addParticipant(user2);
         List<Party> parties = new ArrayList<Party>();
         parties.add(party);
         user.setParties(parties);
         user2.setParties(parties);
         
         Party party2 = new Party();
         party2.setId(2);
         party2.addParticipant(user3);
         List<Party> parties2 = new ArrayList<Party>();
         parties2.add(party2);
         user3.setParties(parties2);
         CurrentUser currentUser = mock(CurrentUser.class);
         when(currentUser.getUser()).thenReturn(user);
         authenticationChecks.setCurrentUser(currentUser);
     }
     
     @Test
     public void testCheckRightsForParty() {
         authenticationChecks.checkRightsForParty(1);
         
         Exception e = null;
         try {
             authenticationChecks.checkRightsForParty(2);
         } catch (NotEnoughRightsException ex) {
             e = ex;
         }
         assertNotNull(e);
     }
     
     @Test
     public void testCheckHighLevelRightsToUser() {
         authenticationChecks.checkHighLevelRightsToUser( 1);
         authenticationChecks.checkHighLevelRightsToUser(2);
         
         Exception e = null;
         try {
             authenticationChecks.checkHighLevelRightsToUser(3);
         } catch (NotEnoughRightsException ex) {
             e = ex;
         }
         assertNotNull(e);
     }
     
     @Test
     public void testCheckLowLevelRightsToUser() {
         authenticationChecks.checkLowLevelRightsToUser(1);
         
         Exception e = null;
         try {
             authenticationChecks.checkLowLevelRightsToUser(3);
         } catch (NotEnoughRightsException ex) {
             e = ex;
         }
         assertNotNull(e);
         
         e = null;
         try {
             authenticationChecks.checkLowLevelRightsToUser(2);
         } catch (NotEnoughRightsException ex) {
             e = ex;
         }
         assertNotNull(e);
     }
 }
