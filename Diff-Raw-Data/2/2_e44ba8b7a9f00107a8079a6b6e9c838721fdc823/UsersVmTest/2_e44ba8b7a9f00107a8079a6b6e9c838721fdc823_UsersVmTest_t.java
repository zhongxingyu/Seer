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
 package org.jtalks.poulpe.web.controller.users;
 
 import static org.jtalks.poulpe.web.controller.users.UsersVm.EDIT_USER_DIALOG;
 import static org.jtalks.poulpe.web.controller.users.UsersVm.CHANGE_PASSWORD_DIALOG;
 import static org.jtalks.poulpe.web.controller.users.UsersVm.EDIT_USER_URL;
 import static org.jtalks.poulpe.web.controller.users.UsersVm.CHANGE_PASSWORD_URL;
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.jtalks.poulpe.model.entity.PoulpeUser;
 import org.jtalks.poulpe.service.UserService;
 import org.jtalks.poulpe.web.controller.ZkHelper;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.WrongValueException;
 import org.zkoss.zul.Textbox;
 import org.zkoss.zul.Window;
 
 public class UsersVmTest {
     // sut
     UsersVm usersVm;
     
     // dependencies
     @Mock UserService userService;
     @Mock ZkHelper zkHelper;
     @Mock Window userDialog;
     @Mock Component component;
     @Mock Textbox searchTextBox;
     @Mock PoulpeUser selectedUser;
     
     final String searchString = "searchString";
 
     @BeforeMethod
     public void setUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         usersVm = new UsersVm(userService);
         usersVm.setZkHelper(zkHelper);
     }
 
     @Test
     public void init_componentsWired() {
         usersVm.init(component, zkHelper);
         verify(zkHelper).wireComponents(component, usersVm);
     }
     
     @Test
     public void init_firtsPageShown() {
         usersVm.init(component, zkHelper);
         verifyFirstPageShown(UsersVm.NO_FILTER_SEARCH_STRING);
     }
 
     private void verifyFirstPageShown(String searchString) {
         verifyNthPageShown(searchString, 1);
     }
 
     private void verifyNthPageShown(String searchString, int page) {
         userService.findUsersPaginated(searchString, page, usersVm.getItemsPerPage());
     }
     
     @Test 
     public void init_searchStringIsEmpty() {
         usersVm.init(component, zkHelper);
         assertEquals(usersVm.getSearchString(), UsersVm.NO_FILTER_SEARCH_STRING);
     }
     
     @Test 
     public void init_activePageIs0() {
         usersVm.init(component, zkHelper);
         assertEquals(usersVm.getActivePage(), 0);
     }
     
     @Test 
     public void init_usersFromFirstPageBound() {
         List<PoulpeUser> users = givenPageWithData(UsersVm.NO_FILTER_SEARCH_STRING, 0);
         usersVm.init(component, zkHelper);
         assertUsersBound(users);
     }
 
     private void assertUsersBound(List<PoulpeUser> users) {
         assertEquals(usersVm.getUsers(), users);
     }
 
     private List<PoulpeUser> givenPageWithData(String searchString, int page) {
         List<PoulpeUser> result = Arrays.asList(new PoulpeUser(), new PoulpeUser());
         when(userService.findUsersPaginated(searchString, page, usersVm.getItemsPerPage())).thenReturn(result);
         return result;
     }
     
     @Test
     public void getTotalSize_allRecords() {
         usersVm.getTotalSize();
         verify(userService).countUsernameMatches(UsersVm.NO_FILTER_SEARCH_STRING);
     }
     
     @Test
     public void getTotalSize_withSearchString() {
         usersVm.searchUsers(searchString);
         usersVm.getTotalSize();
         verify(userService).countUsernameMatches(searchString);
     }
     
     @Test
     public void setActivePage_contentChanged() {
         int activePage = 1; 
         usersVm.setActivePage(activePage);
         
         verifyNthPageShown(UsersVm.NO_FILTER_SEARCH_STRING, activePage);
     }
     
     @Test
     public void setActivePage_pageChanged() {
         int activePage = 2; 
         usersVm.setActivePage(activePage);
         assertActivePageIs(activePage);
     }
 
     private void assertActivePageIs(int activePage) {
         assertEquals(usersVm.getActivePage(), activePage);
     }
     
     @Test
     public void setActive_usersBound() {
         int activePage = 2; 
         List<PoulpeUser> users = givenPageWithData(UsersVm.NO_FILTER_SEARCH_STRING, activePage);
         
         usersVm.setActivePage(activePage);
         
         assertUsersBound(users);
     }
     
     @Test
     public void searchUsers_firstPageRequested() {
         usersVm.searchUsers(searchString);
         verifyFirstPageShown(searchString);
     }
     
     @Test
     public void searchUsers_firstPageShown() {
         usersVm.searchUsers(searchString);
         verifyFirstPageShown(searchString);
         assertActivePageIs(0);
     }
     
     @Test
     public void clearSearch_firstPageWithNoFilterRequested() {
         givenSearchStringInSeachbox();
         usersVm.clearSearch(searchTextBox);
         verifyFirstPageShown(UsersVm.NO_FILTER_SEARCH_STRING);
     }
 
     private void givenSearchStringInSeachbox() {
         when(searchTextBox.getValue()).thenReturn(searchString);
     }
     
     @Test
     public void clearSearch_firstPageShown() {
         givenSearchStringInSeachbox();
         usersVm.clearSearch(searchTextBox);
         assertActivePageIs(0);
     }
     
     @Test
     public void clearSearch_searchStringCleared() {
         givenSearchStringInSeachbox();
         usersVm.clearSearch(searchTextBox);
         verify(searchTextBox).setValue("");
     }
     
     @Test
     public void clearSearch_usersFromFirstPageBound() {
         givenSearchStringInSeachbox();
         List<PoulpeUser> users = givenPageWithData(UsersVm.NO_FILTER_SEARCH_STRING, 0);
         
         usersVm.clearSearch(searchTextBox);
         assertUsersBound(users);
     }
     
     @Test
     public void clearSearch_withEmptyString_nothingCalled() {
         givenNoSearchStringInSeachbox();
         usersVm.clearSearch(searchTextBox);
         verify(searchTextBox, never()).setValue(anyString());
         verify(userService, never()).findUsersPaginated(anyString(), anyInt(), anyInt());
     }
     
     private void givenNoSearchStringInSeachbox() {
         when(searchTextBox.getValue()).thenReturn(UsersVm.NO_FILTER_SEARCH_STRING);
     }
     
     @Test
     public void testEditUser() throws Exception {
         usersVm.setSelectedUser(new PoulpeUser());
         usersVm.editUser();
         verify(zkHelper).wireToZul(EDIT_USER_URL);
     }
 
     @Test
     public void testUpdateUser() throws Exception {
         initEditUserDialog();
         PoulpeUser user = new PoulpeUser();
 
         usersVm.saveUser(user);
         
         verify(userService).updateUser(user);
         verify(userDialog).detach();
     }
 
     private void initEditUserDialog() {
         when(zkHelper.findComponent(EDIT_USER_DIALOG)).thenReturn(userDialog);
     }
 
     @Test
     public void cancelEdit_shouldDetachDialog() {
         initEditUserDialog();
         usersVm.cancelEdit();
         verify(userDialog).detach();
     }
 
     @Test
     public void testGetMD5Hash() {
         assertEquals(usersVm.getMD5Hash("admin"), "21232f297a57a5a743894a0e4a801fc3");
     }
 
     @Test
     public void testChangePassword() {
         usersVm.setSelectedUser(selectedUser);
         Component mockComponent = mock(Component.class);
         when(zkHelper.findComponent(CHANGE_PASSWORD_DIALOG)).thenReturn(mockComponent);
         usersVm.changePassword("admin","admin");
         verify(selectedUser).setPassword("21232f297a57a5a743894a0e4a801fc3");
     }
 
     @Test(expectedExceptions = WrongValueException.class)
    public void passwordChangeShouldFailBecauseTheyDontMatch() {
         usersVm.changePassword("1", "2");
     }
 
     @Test
     public void testCloseChangePasswordDialog() {
         Component mockComponent = mock(Component.class);
         when(zkHelper.findComponent(CHANGE_PASSWORD_DIALOG)).thenReturn(mockComponent);
         usersVm.closeChangePasswordDialog();
         verify(mockComponent).detach();
     }
 
     @Test
     public void testCancelChangePassword() {
         Component mockComponent = mock(Component.class);
         when(zkHelper.findComponent(CHANGE_PASSWORD_DIALOG)).thenReturn(mockComponent);
         usersVm.cancelChangePassword();
         verify(mockComponent).detach();
     }
     
     @Test
     public void testShowChangePasswordWindow() {
         usersVm.showChangePasswordWindow();
         verify(zkHelper).wireToZul(CHANGE_PASSWORD_URL);
     }
 }
