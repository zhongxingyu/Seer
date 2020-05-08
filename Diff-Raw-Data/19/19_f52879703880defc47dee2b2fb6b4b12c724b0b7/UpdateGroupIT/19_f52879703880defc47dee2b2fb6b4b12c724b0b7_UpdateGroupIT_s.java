 package org.osiam.client;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osiam.client.exception.ConflictException;
 import org.osiam.client.exception.NoResultException;
 import org.osiam.client.update.UpdateGroup;
import org.osiam.client.update.UpdateUser;
 import org.osiam.resources.scim.Group;
 import org.osiam.resources.scim.MemberRef;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseOperation;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import com.github.springtestdbunit.annotation.DatabaseTearDown;
 
import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.equalTo;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/context.xml")
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
         DbUnitTestExecutionListener.class})
 @DatabaseSetup("/database_seed.xml")
 @DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
 public class UpdateGroupIT extends AbstractIntegrationTestBase {
 
     private static final String IRRELEVANT = "irrelevant";
     private static String idExistingGroup = "69e1a5dc-89be-4343-976c-b5541af249f4";
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
         assertTrue(returnGroup.getMembers().isEmpty());
     }
 
     @Test(expected = NoResultException.class)
     public void try_update_with_wrong_id_raises_exception() {
         getOriginalGroup();
         createUpdateUserWithUpdateFields();
         idExistingGroup = UUID.randomUUID().toString();
         updateGroup();
         fail("Exception expected");
     }
 
     @Test(expected = NoResultException.class)
     public void try_update_with_user_id_raises_exception() {
         getOriginalGroup();
         createUpdateUserWithUpdateFields();
         idExistingGroup = ID_USER_HSIMPSON;
         updateGroup();
         fail("Exception expected");
     }
 
     @Test
     public void set_display_name_to_empty_string_is_ignored() {
         getOriginalGroup();
        createUpdateUserWithEmptyDisplayName();
 
         updateGroup();
 
         assertThat(returnGroup.getDisplayName(), is(equalTo(IRRELEVANT)));
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
 
     @Test(expected = NoResultException.class)
     public void add_invalid_mebers() {
         getOriginalGroup();
         createUpdateGroupWithAddingInvalidMembers();
         updateGroup();
         fail("Exception expected");
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
     public void delete_all_members_and_add_one_member() {
         getOriginalGroup();
         createUpdateGroupWithDeleteAllMembersAndAddingOneMember();
         updateGroup();
         assertNotNull(returnGroup.getMembers());
         assertEquals(1, returnGroup.getMembers().size());
         MemberRef value = getSingleMember(returnGroup.getMembers(), ID_USER_HSIMPSON);
         assertNotNull(value);
     }
 
     @Test (expected = ConflictException.class)
     public void updating_the_displayname_to_existing_displayname_raises_exception() {
         createUpdateGroupWithNewDisplayName("test_group06");
 
         updateGroup();
     }
 
     private MemberRef getSingleMember(Set<MemberRef> multiValues, Object value) {
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
 
     private void getOriginalGroup() {
         Group.Builder groupBuilder = new Group.Builder().setDisplayName(IRRELEVANT);
 
         MemberRef member01 = new MemberRef.Builder().setValue(ID_USER_BTHOMSON).build();
         MemberRef member02 = new MemberRef.Builder().setValue(ID_USER_CMILLER).build();
         MemberRef member03 = new MemberRef.Builder().setValue(ID_GROUP_01).build();
         Set<MemberRef> members = new HashSet<>();
         members.add(member01);
         members.add(member02);
         members.add(member03);
 
         groupBuilder
                 .setMembers(members)
                 .setExternalId(IRRELEVANT)
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
 
    private void createUpdateGroupWithNewDisplayName(String username) {
        updateGroup = new UpdateGroup.Builder().updateDisplayName(username)
                 .build();
     }
 
 }
