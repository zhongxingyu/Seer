 package drinkcounter.authentication;
 
 import drinkcounter.DrinkCounterService;
 import drinkcounter.model.Party;
 import drinkcounter.model.User;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  *
  * @author murgo
  */
 public class AuthenticationChecks {
     
     @Autowired
     private CurrentUser currentUser;
     
     @Autowired
     private DrinkCounterService drinkCounterService;
     
     public void setCurrentUser(CurrentUser currentUser) {
         this.currentUser = currentUser;
     }
 
     public void setDrinkCounterService(DrinkCounterService drinkCounterService) {
         this.drinkCounterService = drinkCounterService;
     }
     
     public void checkRightsForParty(int partyId) {
         User user = currentUser.getUser();
         if (user == null){
             throw new NotLoggedInException();
         }
         
         if(!drinkCounterService.isUserParticipant(partyId, user.getId())){
             throw new NotEnoughRightsException();
         }
     }
 
     public void checkHighLevelRightsToUser(int userId) {
         
         User user = currentUser.getUser();
         if (user == null){
             throw new NotLoggedInException();
         }
         
         if (user.getId() == userId) return;
         
         //TODO optimize by query
         List<Party> parties = user.getParties();
         for (Party p : parties) {
             List<User> participants = p.getParticipants();
             for (User u : participants) {
                 if (u.getId() == userId)
                     return;
             }
         }
         throw new NotEnoughRightsException();
     }
 
     public void checkLowLevelRightsToUser(int userId) {
         User user = currentUser.getUser();
         if(user == null){
             throw new NotLoggedInException();
         }
         
         if (user.getId() == userId) return;
         throw new NotEnoughRightsException();
     }
 }
