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
 import org.jtalks.poulpe.service.GroupService;
 import org.jtalks.poulpe.web.controller.DialogManager;
 import org.jtalks.poulpe.web.controller.SelectedEntity;
 import org.jtalks.poulpe.web.controller.WindowManager;
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
 
     private static final String SEARCH_STRING = "searchString";
    
     @Mock
     private GroupService groupService;
     @Mock
     private WindowManager windowManager;
     @Mock
     private DialogManager.Performable preformable;
 
     private UserGroupVm viewModel;
     private SelectedEntity<Group> selectedEntity;
     private Group selectedGroup;
     private ListModelList<Group> groups;
 
     @BeforeMethod
     public void beforeMethod() {
         MockitoAnnotations.initMocks(this);
        viewModel = spy (new UserGroupVm(groupService, selectedEntity, windowManager));
         selectedEntity = new SelectedEntity<Group>();
         selectedGroup = new Group();
         groups = new ListModelList<Group>();
         viewModel.setGroups(groups);
         viewModel.setSelectedGroup(selectedGroup);
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
         verify(groupService).getAllMatchedByName(SEARCH_STRING);
     }
 
     @Test
     public void testShowGroupMemberEditWindow() {
         viewModel.showGroupMemberEditWindow();
         verify(windowManager).open(EditGroupMembersVm.EDIT_GROUP_MEMBERS_URL);
     }
 
     @Test
     public void testDeleteGroup() {
         doNothing().when(groupService).deleteGroup(any(Group.class));
         viewModel.deleteGroup();
         verify(groupService).deleteGroup(selectedGroup);
        verify(viewModel).updateView();
         assertFalse((viewModel.isShowDeleteDialog()) && (viewModel.isShowEditDialog()) && (viewModel.isShowNewDialog()));
     }
 
     @Test
     public void testShowNewGroupDialog() {
         viewModel.showNewGroupDialog();
        assertTrue(viewModel.isShowNewDialog());
     }
 
     @Test
     public void testSaveGroup() throws Exception {
         Group group = new Group();
         viewModel.saveGroup(group);
         verify(groupService).saveGroup(group);
        verify(viewModel).updateView();
         assertFalse((viewModel.isShowDeleteDialog()) && (viewModel.isShowEditDialog()) && (viewModel.isShowNewDialog()));
     }
 
     @Test
     public void testCloseDialog(){
        viewModel.showNewGroupDialog();
         viewModel.closeDialog();
         assertFalse((viewModel.isShowDeleteDialog()) && (viewModel.isShowEditDialog()) && (viewModel.isShowNewDialog()));
     }
    @Test
    public void testIsShowNewDialog(){
        viewModel.showNewGroupDialog();
        assertTrue(viewModel.isShowNewDialog());
        assertFalse(viewModel.isShowNewDialog());
    }
 
     @DataProvider
     public Object[][] provideRandomGroupsList(){
         return new Object[][]{{Arrays.asList(new Group("2"), new Group("3"))}};
     }
 
 }
