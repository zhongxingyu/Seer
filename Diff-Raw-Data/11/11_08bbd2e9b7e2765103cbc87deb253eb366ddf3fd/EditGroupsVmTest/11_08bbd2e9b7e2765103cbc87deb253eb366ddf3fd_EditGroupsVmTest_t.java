 package org.jtalks.poulpe.web.controller.users;
 
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.common.service.exceptions.NotFoundException;
 import org.jtalks.poulpe.model.entity.PoulpeUser;
 import org.jtalks.poulpe.service.GroupService;
 import org.jtalks.poulpe.service.UserService;
 import org.mockito.Mock;
import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 /**
  * @author Leonid Kazancev
  */
 public class EditGroupsVmTest {
     @Mock
     private UsersVm usersVm;
     @Mock
     private UserService userService;
     @Mock
     private GroupService groupService;
 
     private List<Group> allGroups;
     private List<Group> userGroups;
 
     /*sut*/
     private EditGroupsVm vm;
 
     @BeforeMethod
     public void init() {
         initMocks(this);
         vm = new EditGroupsVm(usersVm, userService, groupService);
         allGroups = new ArrayList<Group>(5);
         Group group1 = new Group("A");
         Group group2 = new Group("B");
         Group group3 = new Group("C");
         Group group4 = new Group("D");
         Group group5 = new Group("E");
         allGroups.add(group1);
         allGroups.add(group2);
         allGroups.add(group3);
         allGroups.add(group4);
         allGroups.add(group5);
         when(groupService.getAll()).thenReturn(allGroups);
         userGroups = new ArrayList<Group>(2);
         userGroups.add(group1);
         userGroups.add(group3);
         PoulpeUser user = new PoulpeUser();
         user.setGroups(userGroups);
         when(usersVm.getSelectedUser()).thenReturn(user);
     }
 
     @Test
     public void editGroupsTest() throws NotFoundException {
         int expectedSizeGroupsToShow = allGroups.size();
         vm.editGroups();
         Assert.assertTrue(vm.isChosen());
         Assert.assertTrue(vm.isNotChosen());
         Assert.assertTrue(vm.isShowDialog());
         Assert.assertEquals(expectedSizeGroupsToShow, vm.getGroups().size());
         for (GroupBooleanPair groupPair : vm.getGroups()) {
             Assert.assertEquals(userGroups.contains(groupPair.getGroup()), groupPair.isEnable());
         }
 
         Assert.assertEquals(expectedSizeGroupsToShow, vm.getGroupsToShow().size());
         for (GroupBooleanPair groupPair : vm.getGroupsToShow()) {
             Assert.assertTrue(allGroups.contains(groupPair.getGroup()));
         }
     }
 
     @Test
     public void filterTestNotChosenFalse() throws NotFoundException {
         int expectedSizeGroupsToShow = userGroups.size();
         int expectedSizeGroups = allGroups.size();
         vm.editGroups();
         vm.setNotChosen(false);
         vm.filterGroups();
 
         Assert.assertEquals(expectedSizeGroupsToShow, vm.getGroupsToShow().size());
         for (GroupBooleanPair groupPair : vm.getGroupsToShow()) {
             Assert.assertEquals(userGroups.contains(groupPair.getGroup()), groupPair.isEnable());
         }
         Assert.assertEquals(expectedSizeGroups, vm.getGroups().size());
         for (GroupBooleanPair groupPair : vm.getGroups()) {
             Assert.assertTrue(allGroups.contains(groupPair.getGroup()));
         }
     }
 
     @Test
     public void filterTestChosenFalse() throws NotFoundException {
         int expectedSizeGroupsToShow = allGroups.size() - userGroups.size();
         int expectedSizeGroups = allGroups.size();
         vm.editGroups();
         vm.setChosen(false);
         vm.filterGroups();
 
         Assert.assertEquals(expectedSizeGroupsToShow, vm.getGroupsToShow().size());
         for (GroupBooleanPair groupPair : vm.getGroupsToShow()) {
             Assert.assertEquals(userGroups.contains(groupPair.getGroup()), groupPair.isEnable());
         }
         Assert.assertEquals(expectedSizeGroups, vm.getGroups().size());
         for (GroupBooleanPair groupPair : vm.getGroups()) {
             Assert.assertTrue(allGroups.contains(groupPair.getGroup()));
         }
     }
 
     @Test
     public void filterTestBothFiltersFalse() throws NotFoundException {
         int expectedSizeGroupsToShow = 0;
         int expectedSizeGroups = allGroups.size();
         vm.editGroups();
         vm.setChosen(false);
         vm.setNotChosen(false);
         vm.filterGroups();
 
         Assert.assertEquals(expectedSizeGroupsToShow, vm.getGroupsToShow().size());
 
         Assert.assertEquals(expectedSizeGroups, vm.getGroups().size());
         for (GroupBooleanPair groupPair : vm.getGroups()) {
             Assert.assertTrue(allGroups.contains(groupPair.getGroup()));
         }
     }
 
     @Test
     public void saveChangesTest() throws NotFoundException {
         int expectedSizeAllGroups = allGroups.size();
         int expectedSizeUserGroups = 3;
         vm.editGroups();
         //disable group "A"
         vm.getGroups().get(0).setEnable(false);
         //enable group "B" and "E"
         vm.getGroups().get(1).setEnable(true);
         vm.getGroups().get(4).setEnable(true);
 
         vm.saveChanges();
 
         Assert.assertTrue(vm.isChosen());
         Assert.assertTrue(vm.isNotChosen());
         Assert.assertEquals(expectedSizeUserGroups, userGroups.size());
         Assert.assertEquals(expectedSizeAllGroups, allGroups.size());
         Assert.assertNotSame("A", userGroups.get(0).getName());
 
         verify(userService).updateUser((PoulpeUser) vm.getUserToEdit());
         verify(groupService).saveGroup(vm.getGroups().get(0).getGroup());
         verify(groupService).saveGroup(vm.getGroups().get(1).getGroup());
         verify(groupService, times(0)).saveGroup(vm.getGroups().get(2).getGroup());
         verify(groupService, times(0)).saveGroup(vm.getGroups().get(3).getGroup());
         verify(groupService).saveGroup(vm.getGroups().get(4).getGroup());
     }
 
     @Test
     public void saveChangesWithNoChangesTest() throws NotFoundException {
         int expectedSizeAllGroups = allGroups.size();
         int expectedSizeUserGroups = userGroups.size();
         vm.editGroups();
 
         vm.saveChanges();
 
         Assert.assertEquals(expectedSizeUserGroups, userGroups.size());
         Assert.assertEquals(expectedSizeAllGroups, allGroups.size());
         Assert.assertEquals("A", userGroups.get(0).getName());
         Assert.assertEquals("C", userGroups.get(1).getName());
 
 
         verify(userService, times(0)).updateUser(any(PoulpeUser.class));
         verify(groupService, times(0)).saveGroup(any(Group.class));
 
     }
 
     @Test
     public void executeMethodsWithoutLogic() {
         vm.isChosen();
         vm.isNotChosen();
     }
 }
