 package org.bloodtorrent.util;
 
 import org.bloodtorrent.BloodTorrentConstants;
 import org.bloodtorrent.dto.SuccessStory;
 import org.bloodtorrent.dto.User;
 import org.hamcrest.CoreMatchers;
 import org.junit.Test;
 
 import javax.xml.validation.Validator;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 4. 3
  * Time: 오전 9:54
  * To change this template use File | Settings | File Templates.
  */
 public class BloodTorrentValidatorTest {
 
     private Validator validator;
 
     @Test
     public void shouldProvideFirstErrorMessage() {
         BloodTorrentValidator<SuccessStory> validator = new BloodTorrentValidator<SuccessStory>();
         SuccessStory vo = new SuccessStory();
         vo.setTitle(null);
         TestUtil.setDummyString(vo, "summary", 251);
 
         String errorMessage = validator.getFirstValidationMessage(vo);
         assertThat(errorMessage, notNullValue());
         assertThat(errorMessage, is("Please fill out title."));
     }
 
     @Test
     public void shouldCheckValidationInValidationTypeOrder() {
         BloodTorrentValidator<User> validator = new BloodTorrentValidator<User>();
         User user = new User();
         user.setId(null);
 
         String errorMessage = validator.getFirstValidationMessage(user);
         assertThat(errorMessage, is(BloodTorrentConstants.PLEASE_FILL_OUT_ALL_THE_MANDATORY_FIELDS));
 
     }
 
     @Test
     public void shouldReturnTrueWhenValidationCheckIsFailed() {
         BloodTorrentValidator<User> validator = new BloodTorrentValidator<User>();
         User testUser = createTestUser();
         testUser.setFirstName(null);
 
         Boolean isInvalid = validator.isInvalid(testUser);
         assertThat(isInvalid, is(true));
     }
 
     @Test
     public void shouldReturnFalseWhenValidationCheckIsSucceeded() {
         BloodTorrentValidator<User> validator = new BloodTorrentValidator<User>();
         User testUser = createTestUser();
 
         Boolean isInvalid = validator.isInvalid(testUser);
         assertThat(isInvalid, is(false));
     }
 
     @Test
     public void shouldCheckValidationInGivenOrder ()  {
         BloodTorrentValidator<User> validator = new BloodTorrentValidator<User>();
         validator.setFieldNames("birthday", "distance", "state");
 
         User testUser = createTestUser();
         testUser.setBirthday("18031910");
         TestUtil.setDummyString(testUser, "distance", 1);
         TestUtil.setDummyString(testUser, "state", 1);
 
         String errorMessage = validator.getFirstValidationMessage(testUser);
        assertThat(errorMessage, is(BloodTorrentConstants.PLEASE_CHECK + "Date of birth"));
     }
 
     private User createTestUser() {
         User testUser = new User();
         testUser.setId("dummy@dummy.dummy");
         testUser.setPassword("dummy000");
         testUser.setRole("donor");
         testUser.setFirstName("Blood");
         testUser.setLastName("Torrent");
         testUser.setCellPhone("0123456789");
         testUser.setGender("male");
         testUser.setBloodGroup("A+");
         testUser.setAnonymous(false);
         testUser.setAddress("BR Mehta Ln");
         testUser.setCity("New Delhi");
         testUser.setState("Andhra Pradesh");
         testUser.setDistance("10");
         testUser.setBirthday("18-03-1910");
         testUser.setLatitude(17.458418734757736);
         testUser.setLongitude(78.33536359287109);
         return testUser;
     }
 
 }
