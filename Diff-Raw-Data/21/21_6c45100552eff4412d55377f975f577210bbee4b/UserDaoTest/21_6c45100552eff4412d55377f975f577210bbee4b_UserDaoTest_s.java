 package org.imirsel.nema.dao;
 
 //import org.imirsel.nema.model.Address;
 
 import org.imirsel.nema.Constants;
 import org.imirsel.nema.dao.BaseDaoTestCase;
 import org.imirsel.nema.dao.PreferenceValueDao;
 import org.imirsel.nema.dao.RoleDao;
 import org.imirsel.nema.dao.UserDao;
 import org.imirsel.nema.model.PreferenceValue;
 import org.imirsel.nema.model.Role;
 import org.imirsel.nema.model.User;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.DataIntegrityViolationException;
 import junit.framework.Assert;
 
 public class UserDaoTest extends BaseDaoTestCase {
     private UserDao dao = null;
     private RoleDao rdao = null;
     private PreferenceValueDao pvalueDao = null;
     
     public void setUserDao(UserDao dao) {
         this.dao = dao;
     }
     
     public void setRoleDao(RoleDao rdao) {
         this.rdao = rdao;
     }
 
     public void setPreferenceValueDao(PreferenceValueDao pvalueDao){
     	this.pvalueDao = pvalueDao;
     }
     
     public void testGetUserInvalid() throws Exception {
         try {
             dao.get(1000L);
             fail("'badusername' found in database, failing test...");
         } catch (DataAccessException d) {
             assertTrue(d != null);
         }
     }
 
     public void testGetUser() throws Exception {
         User user = dao.get(-1L);
 
         assertNotNull(user);
         assertEquals(1, user.getRoles().size());
         assertTrue(user.isEnabled());
     }
 
     public void testGetUserPassword() throws Exception {
         User user = dao.get(-1L);
         String password = dao.getUserPassword(user.getUsername());
         assertNotNull(password);
         log.debug("password: " + password);
     }
 
     public void testUpdateUser() throws Exception {
         User user = dao.get(-1L);
 
         user.addPreference("emailPref", "true");
         dao.saveUser(user);
         flush();
 
         user = dao.get(-1L);
         assertEquals("true", user.getPreference("emailPref"));
  
         user.setId(null);
 
         endTransaction();
 
         try {
             dao.saveUser(user);
             flush();
             fail("saveUser didn't throw DataIntegrityViolationException");
         } catch (DataIntegrityViolationException e) {
             assertNotNull(e);
             log.debug("expected exception: " + e.getMessage());
         }
     }
 
     public void testAddUserRole() throws Exception {
         User user = dao.get(-1L);
         assertEquals(1, user.getRoles().size());
 
         Role role = rdao.getRoleByName(Constants.ADMIN_ROLE);
         user.addRole(role);
         user = dao.saveUser(user);
         flush();
 
         user = dao.get(-1L);
         assertEquals(2, user.getRoles().size());
 
         //add the same role twice - should result in no additional role
         user.addRole(role);
         dao.saveUser(user);
         flush();
 
         user = dao.get(-1L);
         assertEquals("more than 2 roles", 2, user.getRoles().size());
 
         user.getRoles().remove(role);
         dao.saveUser(user);
         flush();
 
         user = dao.get(-1L);
         assertEquals(1, user.getRoles().size());
     }
 
     public void testAddAndRemoveUser() throws Exception {
         User user = new User("testuser");
         user.setPassword("testpass");
         user.setFirstName("Test");
         user.setLastName("Last");
         /*Address address = new Address();
         address.setCity("Denver");
         address.setProvince("CO");
         address.setCountry("USA");
         address.setPostalCode("80210");
 */
 //        user.setAddress(address);
         user.setEmail("testuser@appfuse.org");
  //       user.setWebsite("http://raibledesigns.com");
         
         Role role = rdao.getRoleByName(Constants.USER_ROLE);
         assertNotNull(role.getId());
         user.addRole(role);
 
         user = dao.saveUser(user);
         flush();
 
         assertNotNull(user.getId());
         user = dao.get(user.getId());
         assertEquals("testpass", user.getPassword());
 
         dao.remove(user.getId());
         flush();
         
         try {
             dao.get(user.getId());
             fail("getUser didn't throw DataAccessException");
         } catch (DataAccessException d) {
             assertNotNull(d);
         }
     }
     
     public void testUserExists() throws Exception {
         boolean b = dao.exists(-1L);
         assertTrue(b);
     }
     
     public void testUserNotExists() throws Exception {
         boolean b = dao.exists(111L);
         assertFalse(b);
     }
     
     public void testAddRemoveAndUpdateUserPreference(){
     	 User user = dao.get(-1L);
    	 assertEquals(1, user.getPreferences().size());
     	 user.addPreference("testPreference","value of Preference");
     	 user = dao.saveUser(user);
          flush();
         assertEquals(2, user.getPreferences().size());
          // saving the preference again -should keep the count same
          user.addPreference("testPreference","value of Preference");
     	 user = dao.saveUser(user);
          flush();
         assertEquals(2, user.getPreferences().size());
          
          
          
          boolean success=user.getPreferences().remove(new PreferenceValue("testPreference","value of Preference"));//.removePreference("testPreference");
          user= dao.saveUser(user);
          flush();
          assertTrue(success);
         assertEquals(1, user.getPreferences().size());
       
          user.addPreference("testPreference","changed value of Preference");
          dao.saveUser(user);
          flush();
          
          
          user.updatePreference("testPreference", "new value");
          user = dao.saveUser(user);
          flush();
         assertEquals(2, user.getPreferences().size());
          assertEquals("new value",user.getPreference("testPreference"));
       
          
          
          
          
     }
 }
