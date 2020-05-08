 package test;
 
 import sql.*;
 import db.DBAccess;
 
 public class Test_User extends Test_Helper {
     static User _user = null;
     /**
      * _group[0]: regular queries only
      * _group[1]: update then query
      * _group[2]: set and get settings
      * _group[3]: 'is' testing for existence
      * _group[4]: create or remove rows
      */
     static int[] _group = {0, 0, 1, 0, 0};
     
     public Test_User(DBAccess source) {
         super();
         _user = new User(source);
         _test = _user;
         _counter = 1;
              
         if (_group[0] == 1) {
             
             display(_user.getUserInfo(_result));
 
             display(_user.getUserInfo(_result, "bli64"));
 
             display(_user.getUserInfo(_result, 1, 5));
 
             display(_user.getUserInfo(_result, 2, 5));
 
             display(_user.getSaltAndHash(_result, "non_ldap5"));
 
             display(_user.getSalt(_result, "non_ldap1"));
 
             display(_user.getHash(_result, "non_ldap4"));
 
             display(_user.getName(_result, "non_ldap1"));
 
             display(_user.getLastName(_result, "non_ldap1"));
 
             display(_user.getNickName(_result, "bo.li"));
 
             display(_user.getRoleInfo(_result, "bo.li"));
 
             display(_user.getDepartment(_result, "bli64"));
 
             display(_user.getIsActive(_result, "bli64"));
 
             display(_user.getIsBannedFromSection(_result, "bli64", "OOP344", "A", "201305"));
 
             display(_user.getIsBannedFromSystem(_result, "bli64"));
 
             display(_user.getComment(_result, "bli64"));
 
             //display(_user.getIsDepartmentAdmin(_result, "bli64", "ABC"));
             
             //display(_user.getIsDepartmentAdmin(_result, "bli64"));
 
             display(_user.getIsSuperAdmin(_result, "fardad.soleimanloo"));
             
             //display(_user.getUsersLike(_result, "bli64"));
         }
         
         if (_group[1] == 1) {
 
             display(_user.setSalt("non_ldap2", "some salt"));
 
             display(_user.setHash("non_ldap2", "some hash"));
             
             display(_user.getSaltAndHash(_result, "non_ldap2"));
             
             display(_user.setName("non_ldap1", "Wing"));
 
             display(_user.setLastName("non_ldap1", "Chen"));
 
             display(_user.setNickName("non_ldap1", "New"));
 
             display(_user.setRoleName("employee", "Emp"));
 
             display(_user.getUserInfo(_result));
 
             display(_user.setRoleName("Emp", "employee"));
             
             display(_user.setLastLogin("bli64"));
             
             display(_user.getUserInfo(_result));
             
             display(_user.setActive("bli64", false));
             
             display(_user.getIsActive(_result, "bli64"));
             
             display(_user.setActive("bli64", true));
             
             display(_user.getIsActive(_result, "bli64"));
             
             display(_user.setBannedFromSystem("bli64", true));
             
             display(_user.getIsBannedFromSystem(_result, "bli64"));
             
             display(_user.setBannedFromSection("bli64", "OOP344", "A", "201305", true));
             
             display(_user.getIsBannedFromSection(_result, "bli64", "OOP344", "A", "201305"));
             
             display(_user.setComment("bli64", "New comment"));
             
             display(_user.getComment(_result, "bli64"));
             
             display(_user.setDepartmentAdmin("bli64", "ICT", true));
             
             //display(_user.getIsDepartmentAdmin(_result, "bli64", "ICT"));
             
             display(_user.setSuperAdmin("bli64", true));
 
             display(_user.getIsSuperAdmin(_result, "bli64"));
             
             display(_user.setUserRole("bli64", 3));
             
             display(_user.getRoleInfo(_result, "bli64"));
         }
         
         if (_group[2] == 1) {
             
             display(_user.getUserSetting(_hm, "bli64"));
 
             display(_user.getUserMeetingSetting(_hm, "bli64"));
             
             display(_user.getUserMeetingSetting(_hm, "bo.li"));
 
             //display(_user.getSectionSetting(_hm, "bo.li", "INT222", "A", "201305"));
 
             //display(_user.getSectionSetting(_hm, "fardad.soleimanloo", "OOP344", "B", "201305"));
             
             display(_user.setUserSetting(_bu_map, "bli64"));
             
             display(_user.getUserSetting(_hm, "bli64"));
             
             display(_user.setUserMeetingSetting(_meeting_map, "bli64"));
             
             display(_user.getUserMeetingSetting(_hm, "bli64"));
             
             //display(_user.setSectionSetting(_section_map, "bo.li", "INT222", "A", "201305"));
             
             //display(_user.getSectionSetting(_hm, "bo.li", "INT222", "A", "201305"));
             
             display(_user.defaultUserSetting("bli64"));
             
             display(_user.getUserSetting(_hm, "bli64"));
             
             //display(_user.defaultUserMeetingSetting("bli64"));
             
             display(_user.getUserMeetingSetting(_hm, "bli64"));
             
             //display(_user.defaultSectionSetting("bo.li", "INT222", "A", "201305"));
             
             //display(_user.defaultSectionSetting("bo.li", "INT222", "B", "201305"));
             
             //display(_user.getSectionSetting(_hm, "bo.li", "INT222", "A", "201305"));
             
             display(_user.getUserRoleSetting(_hm, 1));
             
             display(_user.getUserRoleSetting(_hm, 2));
             
             display(_user.getUserRoleSetting(_hm, 3));
             
             display(_user.setUserRoleSetting(_role_map, 3));
             
             display(_user.getUserRoleSetting(_hm, 3));
             
             display(_user.defaultUserRoleSetting(3));
             
             display(_user.getUserRoleSetting(_hm, 3));
             
             display(_user.getDefaultUserSetting(_hm));
             
             display(_user.getDefaultMeetingSetting(_hm));
             
             display(_user.getDefaultSectionSetting(_hm));
             
         }
         
         if (_group[3] == 1) {
             
             display(_user.isProfessor(_bool, "bli64"));
             
             display(_user.isProfessor(_bool, "bo.li"));
             
             display(_user.isUser(_bool, "bli64"));
 
             display(_user.isUser(_bool, "bli6"));
     
             display(_user.isUser(_bool, "bo.li"));
        
         }
 
         if (_group[4] == 1) {
             
            display(_user.createUser("test10", "", true, 1));
 
             display(_user.getUserInfo(_result, "test10"));
 
            display(_user.createUser("nutest1", "comment", false, 3));
             
             display(_user.createNonLdapUser("nutest1", "some", "name", "xsdwe", "sdsd", "nu_email@gmail.com"));
 
             display(_user.getUserInfo(_result, "nutest1"));
             
             //display(_user.removeUser("nutest1"));
             
         }
     }
 }
