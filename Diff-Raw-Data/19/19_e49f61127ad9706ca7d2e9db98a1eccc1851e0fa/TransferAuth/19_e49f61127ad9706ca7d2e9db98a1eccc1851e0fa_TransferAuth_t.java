 package svm.logic.implementation.tranferobjects;
 
 import svm.domain.abstraction.exception.DomainParameterCheckException;
 import svm.domain.abstraction.modelInterfaces.IMember;
 import svm.domain.abstraction.modelInterfaces.IUserPrivilege;
 import svm.logic.abstraction.exception.IllegalGetInstanceException;
 import svm.logic.abstraction.transferobjects.IHasModel;
 import svm.logic.abstraction.transferobjects.ITransferAuth;
 
 /**
  * ProjectTeam: Team C
  * Date: 31.10.12
  */
 public class TransferAuth implements ITransferAuth, IHasModel<IMember> {
     private IMember member;
     private String firstName;
     private String userName;
     private String lastName;
     private String title;
 
     private boolean isAllowedForSearching = false;
     private boolean isAllowedForMemberViewing = false;
     private boolean isAllowedForMemberChanging = false;
     private boolean isAllowedForMemberDeleting = false;
     private boolean isAllowedForMemberAdding = false;
     private boolean isAllowedForMemberAddingPrivileges = false;
     private boolean isAllowedForContestViewing = false;
     private boolean isAllowedForContestDetailsChanging = false;
     private boolean isAllowedForContestTeamsChanging = false;
    private boolean isAllowedForContestMatchAdding = false;
     private boolean isAllowedForContestResultChanging = false;
     private boolean isAllowedForContestDeleting = false;
     private boolean isAllowedForContestAdding = false;
     private boolean isAllowedForContestSubTeamChanging = false;
 
     @Override
     public IMember getModel() {
         return this.member;
     }
 
     @Override
     public String getUsername() {
         return userName;
     }
 
     @Override
     public String getFirstName() {
         return firstName;
     }
 
     @Override
     public String getLastName() {
         return lastName;
     }
 
     @Override
     public String getTitle() {
         return title;
     }
 
     @Override
     public boolean isAllowedForSearching() {
         return isAllowedForSearching;
     }
 
     @Override
     public boolean isAllowedForMemberViewing() {
         return isAllowedForMemberViewing;
     }
 
     @Override
     public boolean isAllowedForMemberChanging() {
         return isAllowedForMemberChanging;
     }
 
     @Override
     public boolean isAllowedForMemberDeleting() {
         return isAllowedForMemberDeleting;
     }
 
     @Override
     public boolean isAllowedForMemberAdding() {
         return isAllowedForMemberAdding;
     }
 
     @Override
     public boolean isAllowedForMemberAddingPrivileges() {
         return isAllowedForMemberAddingPrivileges;
     }
 
     @Override
     public boolean isAllowedForContestViewing() {
         return isAllowedForContestViewing;
     }
 
     @Override
     public boolean isAllowedForContestDetailsChanging() {
         return isAllowedForContestDetailsChanging;
     }
 
     @Override
     public boolean isAllowedForContestDeleting() {
         return isAllowedForContestDeleting;
     }
 
     @Override
     public boolean isAllowedForContestAdding() {
         return isAllowedForContestAdding;
     }
 
     @Override
     public boolean isAllowedForContestResultChanging() {
         return isAllowedForContestResultChanging;
     }
 
     @Override
    public boolean isAllowedForContestMatchAdding() {
        return isAllowedForContestMatchAdding;
     }
 
     @Override
     public boolean isAllowedForContestTeamsChanging() {
         return isAllowedForContestTeamsChanging;
     }
 
     @Override
     public boolean isAllowedForContestSubTeamChanging() {
         return isAllowedForContestSubTeamChanging;
     }
 
     @Override
     public void setObject(Object o) throws IllegalGetInstanceException, DomainParameterCheckException {
         this.member = (IMember) o;
         this.firstName = member.getFirstName();
         this.userName = member.getUserName();
         this.lastName = member.getLastName();
         this.title = member.getTitle();
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_MANAGER) || member.isIn(IUserPrivilege.Privileges.MEMBER_MANAGER) || member.isIn(IUserPrivilege.Privileges.VIEW_ONLY) || member.isIn(IUserPrivilege.Privileges.CONTEST_MATCH_MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_TEAM_MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_SUBTEAM_MANAGER))
             isAllowedForSearching = true;
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.MEMBER_MANAGER))
             isAllowedForMemberViewing = true;
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.MEMBER_MANAGER))
             isAllowedForMemberChanging = true;
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.MEMBER_MANAGER))
             isAllowedForMemberDeleting = true;
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.MEMBER_MANAGER))
             isAllowedForMemberAdding = true;
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN))
             isAllowedForMemberAddingPrivileges = true;
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_MANAGER) || member.isIn(IUserPrivilege.Privileges.MEMBER_MANAGER) || member.isIn(IUserPrivilege.Privileges.VIEW_ONLY) || member.isIn(IUserPrivilege.Privileges.CONTEST_MATCH_MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_TEAM_MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_SUBTEAM_MANAGER))
             isAllowedForContestViewing = true;
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_MANAGER))
             isAllowedForContestDetailsChanging = true;
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_MATCH_MANAGER))
             isAllowedForContestResultChanging = true;
 
        if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_MANAGER))
            isAllowedForContestMatchAdding = true;
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_TEAM_MANAGER))
             isAllowedForContestTeamsChanging = true;
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_SUBTEAM_MANAGER))
             isAllowedForContestSubTeamChanging = true;
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_MANAGER))
             isAllowedForContestDeleting = true;
 
         if (member.isIn(IUserPrivilege.Privileges.ADMIN) || member.isIn(IUserPrivilege.Privileges.MANAGER) || member.isIn(IUserPrivilege.Privileges.CONTEST_MANAGER))
             isAllowedForContestAdding = true;
 
     }
 
     @Override
     public String toString() {
         return this.member.getFirstName() + " " + this.member.getLastName();
     }
 }
