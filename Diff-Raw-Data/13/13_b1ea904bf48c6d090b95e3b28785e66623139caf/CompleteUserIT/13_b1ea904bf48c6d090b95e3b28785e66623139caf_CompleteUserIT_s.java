 package org.osiam.client;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osiam.client.update.UpdateUser;
 import org.osiam.resources.scim.Address;
 import org.osiam.resources.scim.Email;
 import org.osiam.resources.scim.Entitlement;
 import org.osiam.resources.scim.Extension;
 import org.osiam.resources.scim.Im;
 import org.osiam.resources.scim.MultiValuedAttribute;
 import org.osiam.resources.scim.Name;
 import org.osiam.resources.scim.PhoneNumber;
 import org.osiam.resources.scim.Photo;
 import org.osiam.resources.scim.Role;
 import org.osiam.resources.scim.SCIMSearchResult;
 import org.osiam.resources.scim.User;
 import org.osiam.resources.scim.X509Certificate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseOperation;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import com.github.springtestdbunit.annotation.DatabaseTearDown;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/context.xml")
 @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
         DbUnitTestExecutionListener.class })
 @DatabaseSetup("/database_seed_complete_user.xml")
 @DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
 public class CompleteUserIT extends AbstractIntegrationTestBase {
 
     private static final String VALID_USER_ID = "d83c0f36-4e77-407d-94c9-2ca7e4cb7cf1";
     private static final String EXTENSION_URN = "extension";
 
     @Test
     public void create_complete_user_works() {
         User newUser = initializeUserWithAllAttributes();
         User retUser = oConnector.createUser(newUser, accessToken);
         User dbUser = oConnector.getUser(retUser.getId(), accessToken);
         assertThatNewUserAndReturnUserAreEqual(newUser, dbUser, true);
     }
 
     @Test
     public void update_all_attributes_of_one_user_works() {
         User oldUser = oConnector.getUser(VALID_USER_ID, accessToken);
         User expectedUser = createUserWithUpdatedField();
         UpdateUser updateUser = createUpdateUser(oldUser, expectedUser);
         User dbUser = oConnector.updateUser(VALID_USER_ID, updateUser, accessToken);
         assertThatNewUserAndReturnUserAreEqual(expectedUser, dbUser, false);
     }
 
     @Test
     public void delete_User_who_has_all_attributes(){
         oConnector.deleteUser(VALID_USER_ID, accessToken);
     }
     
     @Test
     public void replace_user_with_has_all_attributes(){
        User patchdUser = new User.Builder(createUserWithUpdatedField()).setId(VALID_USER_ID).build();
        User updatedUser = oConnector.replaceUser(patchdUser, accessToken);
        assertThatNewUserAndReturnUserAreEqual(patchdUser, updatedUser, true);
     }
     
     @Test
     public void search_for_user_by_complex_query() {
         String query = getCompletUserQueryString();
         SCIMSearchResult<User> queryResult = oConnector.searchUsers("filter=" + query, accessToken);
         assertThat(queryResult.getTotalResults(), is(equalTo(1L)));
     }
     
     private String getCompletUserQueryString() {
         return encodeExpected("active eq \"true\""
                 + " and addresses.country eq \"Germany\""
                 + " and addresses.formatted eq \"formatted\""
                 + " and addresses.locality eq \"Berlin\""
                 + " and addresses.postalCode eq \"12345\""
                 + " and addresses.primary eq \"true\""
                 + " and addresses.region eq \"Berlin\""
                 + " and addresses.streetAddress eq \"Voltastr. 5\""
                 + " and addresses.type eq \"" + Address.Type.WORK + "\""
                 + " and displayname eq \"displayName\""
                 + " and emails eq \"test@tarent.de\""
                 + " and emails.primary eq \"true\""
                 + " and emails.type eq \"" + Email.Type.WORK + "\""
                 + " and entitlements eq \"entitlement\""
                 + " and entitlements.primary eq \"false\""
                 + " and entitlements.type eq \"irrelevant\""
                 + " and extension.gender eq \"male\""
                 + " and extension.age eq \"18\""
                 + " and externalId eq \"externalId\""
                 + " and groups eq \"ddf772b2-a864-44d7-a58b-3fdd6e647b7b\""
                 + " and groups.display eq \"group1\""
                 + " and ims eq \"aim\""
                 + " and ims.primary eq \"false\""
                 + " and ims.type eq \"" + Im.Type.AIM + "\""
                 + " and locale eq \"de_DE\""
                 + " and meta.created eq \"" + dateAsString(2014, 0, 27, 14, 32, 11, 648) + "\""
                 + " and meta.lastmodified eq \"" + dateAsString(2014, 0, 27, 14, 32, 11, 648) + "\""
                 + " and name.familyname sw \"fam\""
                 + " and name.formatted eq \"formatted\""
                 + " and name.givenName co \"am\""
                 + " and name.honorificPrefix co \"r.\""
                 + " and name.honorificSuffix sw \"M\""
                 + " and name.middleName eq \"middleName\""
                 + " and nickname eq \"nickname\""
                 + " and phoneNumbers eq \"03012345678\""
                 + " and phoneNumbers.type eq \"" + PhoneNumber.Type.WORK + "\""
                 + " and phoneNumbers.primary eq \"true\""
                 + " and photos co \"name\""
                 + " and photos.primary eq \"true\""
                 + " and not (photos.type eq \"" + Photo.Type.THUMBNAIL + "\")"
                 + " and preferredLanguage eq \"german\""
                 + " and profileurl eq \"/user/username\""
                 + " and roles eq \"user_role\""
                 + " and roles.primary eq \"true\""
                 + " and timezone eq \"DE\""
                 + " and title eq \"title\""
                 + " and userName sw \"user\""
                 + " and x509Certificates eq \"x509Certificate\"");
     }
 
     private User createUserWithUpdatedField() {
         List<Address> addresses = new ArrayList<Address>();
         Address address = new Address.Builder().setCountry("USA")
                 .setFormatted("formattedAddress").setLocality("Houston")
                 .setPostalCode("ab5781").setPrimary(false).setRegion("Texas")
                 .setStreetAddress("Main Street. 22").setType(Address.Type.HOME)
                 .build();
         addresses.add(address);
         List<Email> emails = new ArrayList<Email>();
         Email email = new Email.Builder().setPrimary(true)
                 .setValue("my@mail.com").setType(Email.Type.HOME).build();
         emails.add(email);
         List<Entitlement> entitlements = new ArrayList<Entitlement>();
         Entitlement entitlement = new Entitlement.Builder().setPrimary(true)
                 .setType(new Entitlement.Type("not irrelevant"))
                 .setValue("some entitlement").build();
         entitlements.add(entitlement);
         List<Im> ims = new ArrayList<Im>();
         Im im = new Im.Builder().setPrimary(true).setType(Im.Type.GTALK)
                 .setValue("gtalk").build();
         ims.add(im);
         Name name = new Name.Builder().setFamilyName("Simpson")
                 .setFormatted("formatted").setGivenName("Homer")
                 .setHonorificPrefix("Dr.").setHonorificSuffix("Mr.")
                 .setMiddleName("J").build();
         List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
         PhoneNumber phoneNumber = new PhoneNumber.Builder().setPrimary(true)
                 .setType(PhoneNumber.Type.WORK).setValue("03012345678").build();
         phoneNumbers.add(phoneNumber);
         List<Photo> photos = new ArrayList<Photo>();
         Photo photo = new Photo.Builder().setPrimary(true)
                 .setType(Photo.Type.PHOTO).setValue("username.jpg").build();
         photos.add(photo);
         List<Role> roles = new ArrayList<Role>();
         Role role = new Role.Builder().setPrimary(true).setValue("user_role")
                 .build();
         roles.add(role);
         List<X509Certificate> x509Certificates = new ArrayList<X509Certificate>();
         X509Certificate x509Certificat = new X509Certificate.Builder()
                 .setPrimary(true).setValue("x509Certificat").build();
         x509Certificates.add(x509Certificat);
         Extension extension = new Extension(EXTENSION_URN);
         extension.addOrUpdateField("gender", "female");
         extension.addOrUpdateField("age", new BigInteger("18"));
         return new User.Builder("complete_add_user").setActive(true)
                 .setAddresses(addresses).setDisplayName("displayName")
                 .setEmails(emails).setEntitlements(entitlements)
                 .setExternalId("externalId").setIms(ims).setLocale("de_DE")
                 .setName(name).setNickName("nickname").setPassword("password")
                 .setPhoneNumbers(phoneNumbers).setPhotos(photos)
                 .setPreferredLanguage("german").setProfileUrl("/user/username")
                 .setRoles(roles).setTimezone("DE").setTitle("title")
                 .setX509Certificates(x509Certificates)
                 .addExtension(extension)
                 .build();
     }
 
     private User initializeUserWithAllAttributes() {
         List<Address> addresses = new ArrayList<Address>();
         Address address = new Address.Builder().setCountry("USA")
                 .setFormatted("formattedAddress").setLocality("Houston")
                 .setPostalCode("ab5781").setPrimary(false).setRegion("Texas")
                 .setStreetAddress("Main Street. 22").setType(Address.Type.HOME)
                 .build();
         addresses.add(address);
         List<Email> emails = new ArrayList<Email>();
         Email email = new Email.Builder().setPrimary(true)
                 .setValue("my@mail.com").setType(Email.Type.HOME).build();
         emails.add(email);
         List<Entitlement> entitlements = new ArrayList<Entitlement>();
         Entitlement entitlement = new Entitlement.Builder().setPrimary(true)
                 .setType(new Entitlement.Type("not irrelevant"))
                 .setValue("some entitlement").build();
         entitlements.add(entitlement);
         List<Im> ims = new ArrayList<Im>();
         Im im = new Im.Builder().setPrimary(true).setType(Im.Type.GTALK)
                 .setValue("gtalk").build();
         ims.add(im);
         Name name = new Name.Builder().setFamilyName("Simpson")
                 .setFormatted("formatted").setGivenName("Homer")
                 .setHonorificPrefix("Dr.").setHonorificSuffix("Mr.")
                 .setMiddleName("J").build();
         List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
         PhoneNumber phoneNumber = new PhoneNumber.Builder().setPrimary(true)
                 .setType(PhoneNumber.Type.WORK).setValue("03012345678").build();
         phoneNumbers.add(phoneNumber);
         List<Photo> photos = new ArrayList<Photo>();
         Photo photo = new Photo.Builder().setPrimary(true)
                 .setType(Photo.Type.PHOTO).setValue("username.jpg").build();
         photos.add(photo);
         List<Role> roles = new ArrayList<Role>();
         Role role = new Role.Builder().setPrimary(true).setValue("user_role")
                 .build();
         roles.add(role);
         List<X509Certificate> x509Certificates = new ArrayList<X509Certificate>();
         X509Certificate x509Certificate = new X509Certificate.Builder()
                 .setPrimary(true).setValue("x509Certificat").build();
         x509Certificates.add(x509Certificate);
         Extension extension = new Extension(EXTENSION_URN);
         extension.addOrUpdateField("gender", "female");
         extension.addOrUpdateField("age", new BigInteger("18"));
         return new User.Builder("complete_add_user")
                 .setActive(true)
                 .setAddresses(addresses)
                 .setDisplayName("displayName")
                 .setEmails(emails)
                 .setEntitlements(entitlements)
                 .setExternalId("externalId2")
                 .setIms(ims)
                 .setLocale("de_DE")
                 .setName(name)
                 .setNickName("nickname")
                 .setPassword("password")
                 .setPhoneNumbers(phoneNumbers)
                 .setPhotos(photos)
                 .setPreferredLanguage("german")
                 .setProfileUrl("/user/username")
                 .setRoles(roles)
                 .setTimezone("DE")
                 .setTitle("title")
                 .setX509Certificates(x509Certificates)
                 .addExtension(extension)
                 .build();
     }
 
     private UpdateUser createUpdateUser(User oldUser, User expectedUser) {
         UpdateUser.Builder updateUserBuilder = new UpdateUser.Builder();
 
         updateUserBuilder.updateActive(expectedUser.isActive());
         updateUserBuilder.updateAddress(oldUser.getAddresses().get(0), expectedUser.getAddresses().get(0));
         updateUserBuilder.updateEmail(oldUser.getEmails().get(0), expectedUser.getEmails().get(0));
         updateUserBuilder.updateExternalId(expectedUser.getExternalId());
         updateUserBuilder.updateName(expectedUser.getName());
         updateUserBuilder.updatePhoneNumber(oldUser.getPhoneNumbers().get(0), expectedUser.getPhoneNumbers().get(0));
         updateUserBuilder.updatePreferredLanguage(expectedUser.getPreferredLanguage());
         updateUserBuilder.updateRole(oldUser.getRoles().get(0), expectedUser.getRoles().get(0));
         updateUserBuilder.updateX509Certificate(oldUser.getX509Certificates().get(0), expectedUser
                 .getX509Certificates().get(0));
         updateUserBuilder.updateEntitlement(oldUser.getEntitlements().get(0), expectedUser.getEntitlements().get(0));
         updateUserBuilder.updateIms(oldUser.getIms().get(0), expectedUser.getIms().get(0));
         updateUserBuilder.updatePhotos(oldUser.getPhotos().get(0), expectedUser.getPhotos().get(0));
         updateUserBuilder.updateUserName(expectedUser.getUserName());
 
         return updateUserBuilder.build();
     }
 
     private void assertThatNewUserAndReturnUserAreEqual(User expectedUser, User actualUser, boolean checkExtensions) {
         assertThatAddressesAreEqual(expectedUser.getAddresses(), actualUser.getAddresses());
         if (checkExtensions) {
             assertEquals(expectedUser.getAllExtensions(), actualUser.getAllExtensions());
         }
         assertEquals(expectedUser.getDisplayName(), actualUser.getDisplayName());
         assertThatEmailsAreEqual(expectedUser.getEmails(), actualUser.getEmails());
         assertThatEntitlementsAreEqual(expectedUser.getEntitlements(), actualUser.getEntitlements());
         assertEquals(expectedUser.getExternalId(), actualUser.getExternalId());
         assertThatImsAreEqual(expectedUser.getIms(), actualUser.getIms());
         assertEquals(expectedUser.getLocale(), actualUser.getLocale());
         assertThatNamesAreEqual(expectedUser.getName(), actualUser.getName());
         assertEquals(expectedUser.getNickName(), actualUser.getNickName());
         assertThatPhoneNumbersAreEqual(expectedUser.getPhoneNumbers(), actualUser.getPhoneNumbers());
         assertThatPhotosAreEqual(expectedUser.getPhotos(), actualUser.getPhotos());
         assertEquals(expectedUser.getPreferredLanguage(), actualUser.getPreferredLanguage());
         assertEquals(expectedUser.getProfileUrl(), actualUser.getProfileUrl());
         assertThatRolesAreEqual(expectedUser.getRoles(), actualUser.getRoles());
         assertEquals(expectedUser.getTimezone(), actualUser.getTimezone());
         assertEquals(expectedUser.getTitle(), actualUser.getTitle());
         assertEquals(expectedUser.getUserName(), actualUser.getUserName());
         assertEquals(expectedUser.getUserType(), actualUser.getUserType());
         assertThatX509CertificatesAreEqual(expectedUser.getX509Certificates(), actualUser.getX509Certificates());
         assertEquals(expectedUser.isActive(), actualUser.isActive());
     }
 
     private void assertThatRolesAreEqual(List<Role> expected, List<Role> actual) {
         assertEquals(expected.size(), actual.size());
         ensureListSizeIsOne(expected);
         Role expectedValue = expected.get(0);
         Role actualValue = actual.get(0);
 
         assertEquals(expectedValue.getValue(), actualValue.getValue());
         assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
     }
 
     private void assertThatNamesAreEqual(Name expected, Name actual) {
         assertEquals(expected.getFamilyName(), actual.getFamilyName());
         assertEquals(expected.getFormatted(), actual.getFormatted());
         assertEquals(expected.getGivenName(), actual.getGivenName());
         assertEquals(expected.getHonorificPrefix(), actual.getHonorificPrefix());
         assertEquals(expected.getHonorificSuffix(), actual.getHonorificSuffix());
         assertEquals(expected.getMiddleName(), actual.getMiddleName());
     }
 
     private void assertThatX509CertificatesAreEqual(List<X509Certificate> expected, List<X509Certificate> actual) {
         assertEquals(expected.size(), actual.size());
         ensureListSizeIsOne(expected);
         X509Certificate expectedValue = expected.get(0);
         X509Certificate actualValue = actual.get(0);
 
         assertEquals(expectedValue.getValue(), actualValue.getValue());
         assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
     }
 
     private void assertThatPhotosAreEqual(List<Photo> expected, List<Photo> actual) {
         assertEquals(expected.size(), actual.size());
         ensureListSizeIsOne(expected);
         Photo expectedValue = expected.get(0);
         Photo actualValue = actual.get(0);
 
         assertEquals(expectedValue.getType(), actualValue.getType());
         assertEquals(expectedValue.getValue(), actualValue.getValue());
         assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
     }
 
     private void assertThatPhoneNumbersAreEqual(List<PhoneNumber> expected, List<PhoneNumber> actual) {
         assertEquals(expected.size(), actual.size());
         ensureListSizeIsOne(expected);
         PhoneNumber expectedValue = expected.get(0);
         PhoneNumber actualValue = actual.get(0);
 
         assertEquals(expectedValue.getType(), actualValue.getType());
         assertEquals(expectedValue.getValue(), actualValue.getValue());
         assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
     }
 
     private void assertThatEmailsAreEqual(List<Email> expected, List<Email> actual) {
         assertEquals(expected.size(), actual.size());
         ensureListSizeIsOne(expected);
         Email expectedValue = expected.get(0);
         Email actualValue = actual.get(0);
 
         assertEquals(expectedValue.getType(), actualValue.getType());
         assertEquals(expectedValue.getValue(), actualValue.getValue());
         assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
     }
 
     private void assertThatImsAreEqual(List<Im> expected, List<Im> actual) {
         assertEquals(expected.size(), actual.size());
         ensureListSizeIsOne(expected);
         Im expectedValue = expected.get(0);
         Im actualValue = actual.get(0);
 
         assertEquals(expectedValue.getType(), actualValue.getType());
         assertEquals(expectedValue.getValue(), actualValue.getValue());
         assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
     }
 
     private void assertThatEntitlementsAreEqual(List<Entitlement> expected, List<Entitlement> actual) {
         assertEquals(expected.size(), actual.size());
         ensureListSizeIsOne(expected);
         Entitlement expectedValue = expected.get(0);
         Entitlement actualValue = actual.get(0);
 
         assertEquals(expectedValue.getType(), actualValue.getType());
         assertEquals(expectedValue.getValue(), actualValue.getValue());
         assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
     }
 
     private void assertThatAddressesAreEqual(List<Address> expected, List<Address> actual) {
         assertEquals(expected.size(), actual.size());
         ensureListSizeIsOne(expected);
         Address expectedValue = expected.get(0);
         Address actualValue = actual.get(0);
 
         assertEquals(expectedValue.getType(), actualValue.getType());
         assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
         assertEquals(expectedValue.getCountry(), actualValue.getCountry());
         assertEquals(expectedValue.getFormatted(), actualValue.getFormatted());
         assertEquals(expectedValue.getLocality(), actualValue.getLocality());
         assertEquals(expectedValue.getPostalCode(), actualValue.getPostalCode());
         assertEquals(expectedValue.getRegion(), actualValue.getRegion());
         assertEquals(expectedValue.getStreetAddress(), actualValue.getStreetAddress());
     }
 
     private <T extends MultiValuedAttribute> void ensureListSizeIsOne(List<T> expected) {
         assertTrue("At the moment only lists of the size of one are suported", expected.size() == 1);
     }
 }
