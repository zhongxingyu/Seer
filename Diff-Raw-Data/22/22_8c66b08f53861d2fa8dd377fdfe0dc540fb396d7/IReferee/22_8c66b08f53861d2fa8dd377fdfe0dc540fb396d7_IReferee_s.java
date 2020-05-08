 package final_project.model;
 
 import java.util.*;
 
 public interface IReferee {
     boolean getReffing();
     IReferee setReffing(boolean isReffing);
     IReferee setPhoneNumber(String phoneNumber);
    IReferee setNetwork(String network);
     IReferee setGroup(String group);
     IReferee addWatched(int watched);
     IReferee removeWatched(int watched);
     IReferee clearWatched();
     IHasClub addClub(int club);
     IHasClub removeClub(int club);
     IHasClub clearClubs();
 }
