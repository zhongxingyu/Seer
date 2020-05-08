 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.web.controller.group;
 
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.service.GroupService;
 import org.jtalks.poulpe.web.controller.SelectedEntity;
 import org.jtalks.poulpe.web.controller.WindowManager;
 import org.jtalks.poulpe.web.controller.zkutils.BindUtilsWrapper;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 import org.zkoss.zul.ListModelList;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.*;
 
 /**
  * @author Leonid Kazancev
  */
 public class UserGroupVmTest {
     @SuppressWarnings("JpaQlInspection")
     private static final String SEARCH_STRING = "searchString", SHOW_DELETE_CONFIRM_DIALOG = "showDeleteConfirmDialog";
 
     @Mock
     private GroupService groupService;
     @Mock
     private WindowManager windowManager;
     @Mock
     private List<PoulpeBranch> branches;
 
     private UserGroupVm viewModel;
     private SelectedEntity<Group> selectedEntity;
     private Group selectedGroup;
     private ListModelList<Group> groups;
     private BindUtilsWrapper bindWrapper = spy(new BindUtilsWrapper());
 
 
     @BeforeMethod
     public void beforeMethod() {
         MockitoAnnotations.initMocks(this);
         viewModel = spy(new UserGroupVm(groupService, selectedEntity, windowManager));
         selectedEntity = new SelectedEntity<Group>();
         selectedGroup = new Group();
        groups = new ListModelList<Group>();
         viewModel.setGroups(groups);
         viewModel.setSelectedGroup(selectedGroup);
         viewModel.setBranches(branches);
         viewModel.setBindWrapper(bindWrapper);
     }
 
     @Test(dataProvider = "provideRandomGroupsList")
     public void testUpdateView(List<Group> groupList) {
         groups.add(new Group("1"));
         doReturn(groupList).when(groupService).getAll();
 
         viewModel.updateView();
         assertEquals(groups.size(), 2);
         assertSame(groups.get(0), groupList.get(0));
         assertSame(groups.get(1), groupList.get(1));
     }
 
     @Test
     public void testSearchGroup() {
         viewModel.setSearchString(SEARCH_STRING);
         viewModel.searchGroup();
         verify(groupService).getByName(SEARCH_STRING);
     }
 
     @Test
     public void testShowGroupMemberEditWindow() {
         viewModel.showGroupMemberEditWindow();
         verify(windowManager).open(EditGroupMembersVm.EDIT_GROUP_MEMBERS_URL);
     }
 
     @Test
     public void testDeleteNotModeratorGroup() {
         doNothing().when(groupService).deleteGroup(any(Group.class));
        doNothing().when(bindWrapper).postNotifyChange(any(UserGroupVm.class),eq(SHOW_DELETE_CONFIRM_DIALOG));
         viewModel.deleteGroup();
        verify(bindWrapper).postNotifyChange(any(UserGroupVm.class),eq(SHOW_DELETE_CONFIRM_DIALOG));
         verify(groupService).deleteGroup(selectedGroup);
         assertTrue(viewModel.getSelectedGroup() == null);
         verify(viewModel).updateView();
         verify(viewModel).closeDialog();
 
     }
 
     @Test(dataProvider = "provideRandomGroupsList")
     public void testDeleteModeratorGroup(List notEmptyList) {
         doReturn(notEmptyList).when(groupService).getModeratedBranches(selectedGroup);
         viewModel.deleteGroup();
         verify(groupService, times(0)).deleteGroup(selectedGroup);
     }
 
     @Test
     public void testShowDeleteDialogWithNotModeratorGroup() {
         viewModel.setSelectedGroup(null);
         viewModel.showGroupDeleteConfirmDialog();
         assertTrue(viewModel.getModeratedBranches() == null);
         assertFalse(viewModel.isShowDeleteModeratorGroupDialog());
     }
 
     @Test(dataProvider = "provideRandomGroupsList")
     public void testShowDeleteDialogWithModeratorGroup(List notEmptyList) {
         doReturn(notEmptyList).when(groupService).getModeratedBranches(selectedGroup);
         viewModel.showGroupDeleteConfirmDialog();
         assertEquals(viewModel.getBranches(), notEmptyList);
         assertEquals(viewModel.getSelectedModeratorGroup(), selectedGroup);
         assertTrue(viewModel.isShowDeleteModeratorGroupDialog());
     }
 
     @Test
     public void testShowNewGroupDialog() {
         viewModel.setSelectedGroup(null);
         viewModel.showNewGroupDialog();
         assertTrue(viewModel.isShowGroupDialog());
         assertNotNull(viewModel.getSelectedGroup());
     }
 
     @Test
     public void testShowDeleteConfirmDialog() {
         viewModel.showGroupDeleteConfirmDialog();
         assertTrue(viewModel.isShowDeleteConfirmDialog());
     }
 
     @Test(dataProvider = "provideRandomBranchesList")
     public void testSaveModeratorForBranchesWithoutChanges(List BranchesList) {
         doReturn(BranchesList).when(groupService).getModeratedBranches(selectedGroup);
         viewModel.setBranches(viewModel.getModeratedBranches());
         viewModel.setSelectedModeratorGroup(selectedGroup);
         viewModel.saveModeratorForBranches();
         for (PoulpeBranch branch : viewModel.getBranches()) {
             verify(branch, times(0)).setModeratorsGroup(any(Group.class));
         }
     }
 
     @Test(dataProvider = "provideRandomBranchesList")
     public void testSaveModeratorForBranchesWithChanges(List BranchesList) {
         doReturn(BranchesList).when(groupService).getModeratedBranches(selectedGroup);
         Group moderatorGroup = new Group("moderator");
         viewModel.setBranches(viewModel.getModeratedBranches());
         viewModel.setSelectedModeratorGroup(moderatorGroup);
         viewModel.saveModeratorForBranches();
         for (PoulpeBranch branch : viewModel.getBranches()) {
             verify(branch).setModeratorsGroup(moderatorGroup);
         }
     }
 
     @Test
     public void testAddToMap() {
         PoulpeBranch keyBranch = someBranch();
         Group valueGroup = someGroup();
         viewModel.addToMap(keyBranch, valueGroup);
         assertEquals(viewModel.getBranchesMap().get(keyBranch), valueGroup);
         assertTrue(viewModel.getBranchesMap().size() == 1);
     }
 
     @Test(dataProvider = "provideRandomBranchesList")
     public void testSaveModeratorForCurrentBranch(List BranchesList) {
         doReturn(BranchesList).when(groupService).getModeratedBranches(selectedGroup);
         Group oldModeratorGroup = someGroup();
         Group wantBecameModeratorGroup = someGroup();
         viewModel.setSelectedGroup(oldModeratorGroup);
         PoulpeBranch keyBranch = someBranch();
         keyBranch.setModeratorsGroup(oldModeratorGroup);
         viewModel.addToMap(keyBranch, wantBecameModeratorGroup);
         viewModel.saveModeratorForCurrentBranch(keyBranch);
 
         verify(keyBranch).setModeratorsGroup(wantBecameModeratorGroup);
         verify(viewModel.getBranches()).remove(keyBranch);
         assertFalse(viewModel.isShowDeleteModeratorGroupDialog());
         assertTrue(viewModel.getBranchesMap().size() == 0);
     }
 
     @Test
     public void testSaveGroup() throws Exception {
         Group group = new Group();
         viewModel.setSelectedGroup(group);
         viewModel.saveGroup();
         verify(groupService).saveGroup(group);
         verify(viewModel).updateView();
         assertFalse(viewModel.isShowGroupDialog());
         assertFalse(viewModel.isShowDeleteConfirmDialog());
     }
 
     @Test
     public void testOpenDialog() {
         viewModel.showNewGroupDialog();
         assertTrue(viewModel.isShowGroupDialog());
     }
 
     @Test
     public void testCloseDialog() {
         viewModel.showNewGroupDialog();
         viewModel.closeDialog();
         assertFalse(viewModel.isShowGroupDialog());
         assertFalse(viewModel.isShowDeleteConfirmDialog());
     }
 
     @Test
     public void testIsShowGroupDialog() {
         viewModel.showNewGroupDialog();
         assertTrue(viewModel.isShowGroupDialog());
         assertFalse(viewModel.isShowGroupDialog());
     }
 
     @Test
     public void testGetGroups() {
         assertEquals(viewModel.getGroups(), groups);
         verify(viewModel).updateView();
     }
 
     @Test
     public void testShowEditDialog() {
         viewModel.showEditDialog();
         assertTrue(viewModel.isShowGroupDialog());
     }
 
 
     @DataProvider
     public Object[][] provideRandomGroupsList() {
         return new Object[][]{{Arrays.asList(new Group("2"), new Group("3"))}};
     }
 
     @DataProvider
     public Object[][] provideRandomBranchesList() {
         return new Object[][]{{Arrays.asList(spy(new PoulpeBranch("2")), spy(new PoulpeBranch("3")))}};
     }
 
     private Group someGroup() {
         return new Group("someGroup");
     }
 
     private PoulpeBranch someBranch() {
         return spy(new PoulpeBranch("someBranch"));
     }
 
 
 }
