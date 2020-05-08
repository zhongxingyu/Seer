 package org.osiam.client;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseOperation;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import com.github.springtestdbunit.annotation.DatabaseTearDown;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osiam.client.exception.ConflictException;
 import org.osiam.client.exception.NotFoundException;
 import org.osiam.client.update.UpdateGroup;
 import org.osiam.resources.scim.Address;
 import org.osiam.resources.scim.Group;
 import org.osiam.resources.scim.MemberRef;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import static org.junit.Assert.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/context.xml")
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
         DbUnitTestExecutionListener.class})
 @DatabaseSetup("/database_seed.xml")
 @DatabaseTearDown(value = "/database_seed.xml", type = DatabaseOperation.DELETE_ALL)
@Ignore("Ignored until PATCH works again")
 public class UpdateGroupIT extends AbstractIntegrationTestBase {
 
     private static String idExistingGroup = "7d33bcbe-a54c-43d8-867e-f6146164941e";
     private static String ID_USER_BTHOMSON = "618b398c-0110-43f2-95df-d1bc4e7d2b4a";
     private static String ID_USER_CMILLER = "ac3bacc9-915d-4bab-9145-9eb600d5e5bf";
     private static String ID_USER_HSIMPSON = "7d33bcbe-a54c-43d8-867e-f6146164941e";
     private static String ID_GROUP_01 = "69e1a5dc-89be-4343-976c-b5541af249f4";
     private static String ID_GROUP_02 = "d30a77eb-d7cf-4cd1-9fb3-cc640ef09578";
     private UpdateGroup updateGroup;
     private Group returnGroup;
     private Group originalGroup;
 
     @Test
     public void update_all_single_values() {
         getOriginalGroup();
         createUpdateUserWithUpdateFields();
         updateGroup();
         assertEquals("DisplayName", returnGroup.getDisplayName());
         assertEquals("ExternalId", returnGroup.getExternalId());
     }
 
     @Test
     public void delete_all_single_values() {
         getOriginalGroup();
         createUpdateGroupWithDeleteFields();
         updateGroup();
         assertNull(returnGroup.getExternalId());
     }
 
     @Test
     public void delete_all_members() {
         getOriginalGroup();
         createUpdateGroupWithDeletedMembers();
         updateGroup();
         assertNotNull(originalGroup.getMembers());
        assertNull(returnGroup.getMembers());
     }
 
     @Test(expected = NotFoundException.class)
     public void try_update_with_wrong_id_raises_exception() {
         getOriginalGroup();
         createUpdateUserWithUpdateFields();
         idExistingGroup = UUID.randomUUID().toString();
         updateGroup();
     }
 
     @Test(expected = NotFoundException.class)
     public void try_update_with_user_id_raises_exception() {
         getOriginalGroup();
         createUpdateUserWithUpdateFields();
         idExistingGroup = ID_USER_HSIMPSON;
         updateGroup();
     }
 
     @Test(expected = ConflictException.class)
    @Ignore //not exception is thrown at the moment
     public void set_display_name_to_empty_string_to_raise_exception() {
         getOriginalGroup();
         createUpdateUserWithEmptyDisplayName();
         updateGroup();
         fail("exception expected");
     }
 
     @Test
     public void add_new_mebers() {
         getOriginalGroup();
         createUpdateGroupWithAddingMembers();
         updateGroup();
         assertEquals(originalGroup.getMembers().size() + 2, returnGroup.getMembers().size());
         MemberRef value = getSingleMember(returnGroup.getMembers(), ID_USER_HSIMPSON);
         assertNotNull(value);
         value = getSingleMember(returnGroup.getMembers(), ID_GROUP_02);
         assertNotNull(value);
     }
 
     @Test(expected = NotFoundException.class)
     public void add_invalid_mebers() {
         getOriginalGroup();
         createUpdateGroupWithAddingInvalidMembers();
         updateGroup();
     }
 
     @Test
     public void delete_one_user_and_one_group_member() {
         getOriginalGroup();
         createUpdateGroupWithDeleteOneMembers();
         updateGroup();
         assertEquals(originalGroup.getMembers().size() - 2, returnGroup.getMembers().size());
         MemberRef value = getSingleMember(returnGroup.getMembers(), ID_USER_HSIMPSON);
         assertNull(value);
         value = getSingleMember(returnGroup.getMembers(), ID_GROUP_01);
         assertNull(value);
     }
 
     @Test
    @Ignore //the new member is not been added at the moment
     public void delete_all_members_and_add_one_member() {
         getOriginalGroup();
         createUpdateGroupWithDeleteAllMembersAndAddingOneMember();
         updateGroup();
         assertNotNull(returnGroup.getMembers());
         assertEquals(1, returnGroup.getMembers().size());
         MemberRef value = getSingleMember(returnGroup.getMembers(), ID_USER_HSIMPSON);
         assertNotNull(value);
     }
 
     public boolean isValuePartOfMultivalueList(List<MemberRef> list, String value) {
         if (list != null) {
             for (MemberRef actAttribute : list) {
                 if (actAttribute.getValue().equals(value)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public MemberRef getSingleMultiValueAttribute(Set<MemberRef> multiValues, Object value) {
         if (multiValues != null) {
             for (MemberRef actMemberRef : multiValues) {
                 if (actMemberRef.getValue().equals(value.toString())) {
                     return actMemberRef;
                 }
             }
         }
         return null;
     }
 
     public MemberRef getSingleMember(Set<MemberRef> multiValues, Object value) {
         if (multiValues != null) {
             for (MemberRef actMemberRef : multiValues) {
                 if (actMemberRef.getValue().equals(value.toString())) {
                     return actMemberRef;
                 }
             }
         }
         return null;
     }
 
     private void createUpdateGroupWithDeleteFields() {
         updateGroup = new UpdateGroup.Builder()
                 .deleteExternalId()
                 .build();
     }
 
     private void createUpdateGroupWithDeletedMembers() {
         updateGroup = new UpdateGroup.Builder()
                 .deleteMembers()
                 .build();
     }
 
     private void createUpdateGroupWithAddingMembers() {
         updateGroup = new UpdateGroup.Builder()
                 .addMember(ID_USER_HSIMPSON)
                 .addMember(ID_GROUP_02)
                 .build();
     }
 
     private void createUpdateGroupWithAddingInvalidMembers() {
         updateGroup = new UpdateGroup.Builder()
                 .addMember(UUID.randomUUID().toString())
                 .build();
     }
 
     private void createUpdateGroupWithDeleteOneMembers() {
         updateGroup = new UpdateGroup.Builder()
                 .deleteMember(ID_USER_CMILLER)
                 .deleteMember(ID_GROUP_01)
                 .build();
     }
 
     private void createUpdateGroupWithDeleteAllMembersAndAddingOneMember() {
         updateGroup = new UpdateGroup.Builder()
                 .deleteMembers()
                 .addMember(ID_USER_HSIMPSON)
                 .build();
     }
 
     public void getOriginalGroup() {
         Group.Builder groupBuilder = new Group.Builder().setDisplayName("irgendwas");
 
         MemberRef member01 = new MemberRef.Builder().setValue(ID_USER_BTHOMSON).build();
         MemberRef member02 = new MemberRef.Builder().setValue(ID_USER_CMILLER).build();
         MemberRef member03 = new MemberRef.Builder().setValue(ID_GROUP_01).build();
         Set<MemberRef> members = new HashSet<>();
         members.add(member01);
         members.add(member02);
         members.add(member03);
 
         groupBuilder
                 .setMembers(members)
                 .setExternalId("irgendwas")
         ;
         Group newGroup = groupBuilder.build();
 
         originalGroup = oConnector.createGroup(newGroup, accessToken);
         idExistingGroup = originalGroup.getId();
     }
 
     private void createUpdateUserWithUpdateFields() {
         updateGroup = new UpdateGroup.Builder()
                 .updateDisplayName("DisplayName")
                 .updateExternalId("ExternalId")
                 .build();
     }
 
     private void createUpdateUserWithEmptyDisplayName() {
         updateGroup = new UpdateGroup.Builder()
                 .updateDisplayName("")
                 .build();
     }
 
     private void updateGroup() {
         returnGroup = oConnector.updateGroup(idExistingGroup, updateGroup, accessToken);
     }
 
     public Address getAddress(List<Address> addresses, String formated) {
         if (addresses != null) {
             for (Address actAddress : addresses) {
                 if (actAddress.getFormatted().equals(formated)) {
                     return actAddress;
                 }
             }
         }
         fail("The address with the formated part of " + formated + " could not be found");
         return null; //Can't be reached
     }
 
 }
