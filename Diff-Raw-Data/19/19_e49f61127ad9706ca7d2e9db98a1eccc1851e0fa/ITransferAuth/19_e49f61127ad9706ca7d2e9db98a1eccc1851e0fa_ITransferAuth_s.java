 package svm.logic.abstraction.transferobjects;
 
 /**
  * ProjectTeam: Team C
  * Date: 30.10.12
  */
 public interface ITransferAuth extends ITransfer {
 
     String getUsername();
 
     String getFirstName();
 
     String getLastName();
 
     String getTitle();
 
     // SEARCHING
     boolean isAllowedForSearching();
 
 
     // MEMBER
     boolean isAllowedForMemberViewing();
 
     boolean isAllowedForMemberChanging();
 
     boolean isAllowedForMemberDeleting();
 
     boolean isAllowedForMemberAdding();
 
     boolean isAllowedForMemberAddingPrivileges();
 
 
     // CONTEST
     boolean isAllowedForContestViewing();
 
     boolean isAllowedForContestDetailsChanging();
 
     boolean isAllowedForContestDeleting();
 
     boolean isAllowedForContestAdding();
 
     boolean isAllowedForContestResultChanging();
 
    boolean isAllowedForContestMatchChanging();
 
     boolean isAllowedForContestTeamsChanging();
 
     boolean isAllowedForContestSubTeamChanging();
 }
