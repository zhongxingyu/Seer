 package com.gardin.piazza.dao.users;
 
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StringUtils;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.gardin.piazza.domain.users.Address;
 import com.gardin.piazza.domain.users.User;
 import com.gardin.piazza.test.AbstractDBTest;
 
 /**
  * Tests for the HibernateUserDAO class.
  * 
  * @author Pierre Gardin
  * 
  */
 public class HibernateUserDAOTests extends AbstractDBTest {
 
     private static final String USER_COUNTRY = "France";
     private static final String USER_EMAIL = "romain.legrand@orange.fr";
     private static final String USER_FIRST_NAME = "Romain";
 
     private static final String USER_BUILDING = "13E";
     private static final String USER_CITY = "Lyon";
     private static final int USER_FLOOR = 5;
     private static final String USER_STREET_NUMBER = "5bis";
     private static final String USER_STREET = "Rue du Maréchal Juin";
     private static final String USER_ZIP_CODE = "38047";
 
     private static final String USER_EMAIL_2 = "alexis.legrand@orange.fr";
     private static final String USER_FIRST_NAME_2 = "Alexis";
 
     private static final String USER_INSTITUTION = "Université de Caen";
     private static final String USER_PASSWORD = "rgf45F65gDgnT7";
     private static final String USER_SURNAME = "Legrand";
 
     @Test
     @Transactional
     public void testSaveAndGetUser() {
         // Create test data
         User user = new User();
         user.setCountry(USER_COUNTRY);
         user.setEmail(USER_EMAIL);
         user.setFirstName(USER_FIRST_NAME);
         user.setSurname(USER_SURNAME);
         user.setInstitution(USER_INSTITUTION);
         user.setPassword(USER_PASSWORD);
 
         Address address = new Address();
         address.setBuilding(USER_BUILDING);
         address.setCity(USER_CITY);
         address.setFloor(USER_FLOOR);
        address.setStreetNumber(USER_STREET_NUMBER);
         address.setStreet(USER_STREET);
         address.setZipCode(USER_ZIP_CODE);
 
         user.setAddress(address);
 
         Integer id = userDAO.save(user);
 
         // Test retrieved user
         User retrievedUser = userDAO.getByID(id);
         Assert.assertNotNull(retrievedUser);
         Assert.assertEquals(retrievedUser.getCountry(), USER_COUNTRY);
         Assert.assertEquals(retrievedUser.getEmail(), USER_EMAIL);
         Assert.assertEquals(retrievedUser.getFirstName(), USER_FIRST_NAME);
         Assert.assertEquals(retrievedUser.getSurname(), USER_SURNAME);
         Assert.assertEquals(retrievedUser.getInstitution(), USER_INSTITUTION);
         Assert.assertEquals(retrievedUser.getPassword(), USER_PASSWORD);
 
         // Test retrieved address
         Address retrievedAddress = retrievedUser.getAddress();
         Assert.assertEquals(retrievedAddress.getBuilding(), USER_BUILDING);
         Assert.assertEquals(retrievedAddress.getCity(), USER_CITY);
         Assert.assertEquals(retrievedAddress.getFloor(), USER_FLOOR);
         Assert.assertEquals(retrievedAddress.getStreetNumber(), USER_STREET_NUMBER);
         Assert.assertEquals(retrievedAddress.getStreet(), USER_STREET);
         Assert.assertEquals(retrievedAddress.getZipCode(), USER_ZIP_CODE);
 
         Assert.assertEquals(retrievedUser.getAddress(), retrievedAddress,
                 "The User should have a reference to the Address object.");
     }
 
     @Test
     @Transactional
     public void testSaveOrUpdateUser() {
         // Create test data
         Assert.assertEquals(userDAO.getAll().size(), 0);
 
         User user = new User();
         user.setCountry(USER_COUNTRY);
         user.setEmail(USER_EMAIL);
         user.setFirstName(USER_FIRST_NAME);
         user.setSurname(USER_SURNAME);
         user.setInstitution(USER_INSTITUTION);
         user.setPassword(USER_PASSWORD);
         userDAO.saveOrUpdate(user);
         Assert.assertEquals(userDAO.getAll().size(), 1);
 
         User user2 = new User();
         user2.setCountry(USER_COUNTRY);
         user2.setEmail(USER_EMAIL_2);
         user2.setFirstName(USER_FIRST_NAME_2);
         user2.setSurname(USER_SURNAME);
         user2.setInstitution(USER_INSTITUTION);
         user2.setPassword(USER_PASSWORD);
         userDAO.saveOrUpdate(user2);
         Assert.assertEquals(userDAO.getAll().size(), 2);
 
         // Change password
         User userToChange = userDAO.getByEmail(USER_EMAIL);
         final String newPassword = StringUtils.capitalize(USER_PASSWORD);
         userToChange.setPassword(newPassword);
         userDAO.saveOrUpdate(userToChange);
         Assert.assertEquals(userDAO.getAll().size(), 2);
 
         // Test change password
         User retrievedUser = userDAO.getByEmail(USER_EMAIL);
         Assert.assertEquals(retrievedUser.getPassword(), newPassword);
     }
 }
