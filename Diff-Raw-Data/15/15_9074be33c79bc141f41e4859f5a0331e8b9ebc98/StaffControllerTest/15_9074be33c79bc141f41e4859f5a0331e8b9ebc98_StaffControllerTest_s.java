 package org.motechproject.ghana.national.web;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.StaffType;
 import org.motechproject.ghana.national.service.StaffService;
 import org.motechproject.ghana.national.web.form.StaffForm;
 import org.motechproject.ghana.national.web.helper.StaffHelper;
 import org.motechproject.mrs.exception.UserAlreadyExistsException;
 import org.motechproject.mrs.model.Attribute;
 import org.motechproject.mrs.model.MRSPerson;
 import org.motechproject.mrs.model.MRSUser;
 import org.motechproject.openmrs.services.OpenMRSUserAdapter;
 import org.springframework.context.MessageSource;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static ch.lambdaj.Lambda.*;
 import static java.util.Arrays.asList;
 import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.national.domain.Constants.*;
 
 public class StaffControllerTest {
 
     StaffController controller;
     @Mock
     StaffService mockStaffService;
     @Mock
     MessageSource mockMessageSource;
     @Mock
     BindingResult mockBindingResult;
 
     StaffHelper staffHelper;
 
     @Before
     public void setUp() {
         initMocks(this);
         staffHelper = new StaffHelper();
         controller = new StaffController(mockStaffService, mockMessageSource, staffHelper);
         mockBindingResult = mock(BindingResult.class);
     }
 
     @Test
     public void shouldReturnNewUserForm() {
         ModelMap model = mock(ModelMap.class);
         Map<String, String> roles = new HashMap<String, String>();
         when(mockStaffService.fetchAllRoles()).thenReturn(roles);
 
         String view = controller.newStaff(model);
 
         assertEquals(StaffController.NEW_STAFF_URL, view);
         ArgumentCaptor<StaffForm> captor = ArgumentCaptor.forClass(StaffForm.class);
         verify(model).addAttribute("roles", roles);
         verify(model).addAttribute(eq(StaffController.STAFF_FORM), captor.capture());
         StaffForm captured = captor.getValue();
         assertNotNull(captured);
     }
 
     @Test
     public void shouldAddNewAdminUser() throws UserAlreadyExistsException {
         final String email = "jack@daniels.com";
         StaffForm form = new StaffForm().setNewEmail(email).setFirstName("Jack").setMiddleName("H").setLastName("Daniels")
                 .setPhoneNumber("1234").setNewRole(StaffType.Role.CALL_CENTER_ADMIN.key());
 
         final MRSUser openMRSUser = form.createUser().systemId("1234");
         BindingResult bindingResult = mock(BindingResult.class);
         ModelMap model = mock(ModelMap.class);
         when(mockStaffService.saveUser(any(MRSUser.class))).thenReturn(openMRSUser);
         when(mockStaffService.getUserByEmailIdOrMotechId(openMRSUser.getSystemId())).thenReturn(openMRSUser);
 
         String view = controller.create(form, bindingResult, model);
 
         assertEquals(StaffController.EDIT_STAFF_URL, view);
         verify(model).put("userId", "1234");
         verify(model).put("userName", "Jack H Daniels");
 
         ArgumentCaptor<MRSUser> captor = ArgumentCaptor.forClass(MRSUser.class);
         verify(mockStaffService).saveUser(captor.capture());
         MRSUser captured = captor.getValue();
         assertEquals(form.getNewRole(), getAttrValue(captured, Constants.PERSON_ATTRIBUTE_TYPE_STAFF_TYPE));
         assertEquals(form.getNewEmail(), getAttrValue(captured, Constants.PERSON_ATTRIBUTE_TYPE_EMAIL));
         assertEquals(form.getPhoneNumber(), getAttrValue(captured, Constants.PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER));
         assertEquals(email, captured.getUserName());
     }
 
     @Test
     public void shouldAddNewNonAdminUser() throws UserAlreadyExistsException {
         String userId = "1234";
         StaffForm form = new StaffForm().setNewEmail("jack@daniels.com").setFirstName("Jack").setMiddleName("H").setLastName("Daniels")
                 .setPhoneNumber("1234").setStaffId(userId).setNewRole(StaffType.Role.COMMUNITY_HEALTH_OPERATOR.key());
         ModelMap model = mock(ModelMap.class);
         final MRSUser openMRSuser = form.createUser();
         when(mockStaffService.saveUser(any(MRSUser.class))).thenReturn(openMRSuser);
 
         String view = controller.create(form, mockBindingResult, model);
 
         assertEquals(StaffController.EDIT_STAFF_URL, view);
         verify(model).put("userId", userId);
         verify(model).put("userName", "Jack H Daniels");
 
         ArgumentCaptor<MRSUser> captor = ArgumentCaptor.forClass(MRSUser.class);
         verify(mockStaffService).saveUser(captor.capture());
         MRSUser captured = captor.getValue();
         assertEquals(userId, captured.getSystemId());
         assertEquals(form.getNewRole(), getAttrValue(captured, Constants.PERSON_ATTRIBUTE_TYPE_STAFF_TYPE));
         assertEquals(form.getNewEmail(), getAttrValue(captured, Constants.PERSON_ATTRIBUTE_TYPE_EMAIL));
         assertEquals(form.getPhoneNumber(), getAttrValue(captured, Constants.PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER));
     }
 
     private String getAttrValue(MRSUser mrsUser, String name) {
         for (Attribute userAttribute : mrsUser.getPerson().getAttributes())
             if (userAttribute.name().equalsIgnoreCase(name))
                 return userAttribute.value();
         return null;
     }
 
     @Test
     public void shouldReturnErrorMessageIfUserAlreadyExists() throws UserAlreadyExistsException {
         StaffForm form = new StaffForm();
         form.setNewEmail("jack@daniels.com");
         form.setFirstName("Jack");
         form.setMiddleName("H");
         form.setLastName("Daniels");
         form.setPhoneNumber("1234");
         form.setNewRole(StaffType.Role.CALL_CENTER_ADMIN.key());
 
 
         ModelMap model = mock(ModelMap.class);
         Map<String, String> roles = new HashMap<String, String>();
         roles.put("role1", "role1");
         Map<String, Object> boundModel = new HashMap<String, Object>();
 
         when(mockStaffService.saveUser(any(MRSUser.class))).thenThrow(new UserAlreadyExistsException());
         when(mockBindingResult.getModel()).thenReturn(boundModel);
         when(mockStaffService.fetchAllRoles()).thenReturn(roles);
 
         String view = controller.create(form, mockBindingResult, model);
 
         assertEquals(StaffController.NEW_STAFF_URL, view);
         verify(model).mergeAttributes(boundModel);
         verify(model).addAttribute("roles", roles);
         verify(mockBindingResult).addError(any(FieldError.class));
     }
 
     @Test
     public void shouldSearchForUser() {
         String staffID = "1";
         String firstName = "TEST";
         String middleName = "TEST";
         String lastName = "TEST";
         String email = "TEST@gmail.COM";
         String phoneNumber = "0987654321";
         String role = "System Developer";
 
         StaffForm staffForm = new StaffForm();
         staffForm.setStaffId(staffID);
         staffForm.setFirstName(firstName);
         staffForm.setMiddleName(middleName);
         staffForm.setLastName(lastName);
         staffForm.setPhoneNumber(phoneNumber);
         staffForm.setNewRole(role);
 
         ModelMap modelMap = new ModelMap();
         MRSPerson mrsPerson = new MRSPerson().addAttribute(new Attribute(Constants.PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER, phoneNumber))
                 .addAttribute(new Attribute(Constants.PERSON_ATTRIBUTE_TYPE_EMAIL, email))
                 .addAttribute(new Attribute(Constants.PERSON_ATTRIBUTE_TYPE_STAFF_TYPE, role))
                 .firstName(firstName).middleName(middleName).lastName(lastName).id(staffID);
 
         MRSUser user1 = new MRSUser().systemId(staffID).person(mrsPerson);
 
         when(mockStaffService.searchStaff(staffID, firstName, middleName, lastName, phoneNumber, role)).thenReturn(Arrays.asList(user1));
 
         controller.search(staffForm, modelMap);
 
         verify(mockStaffService).searchStaff(staffForm.getStaffId(), staffForm.getFirstName(), staffForm.getMiddleName(),
                 staffForm.getLastName(), staffForm.getPhoneNumber(), staffForm.getNewRole());
 
         final List<StaffForm> actualRequestedUsers = (List<StaffForm>) modelMap.get(StaffController.REQUESTED_STAFFS);
         assertEquals(1, actualRequestedUsers.size());
         final StaffForm form = actualRequestedUsers.get(0);
         assertEquals(form.getStaffId(), staffID);
         assertEquals(form.getNewEmail(), email);
         assertEquals(form.getFirstName(), firstName);
         assertEquals(form.getLastName(), lastName);
         assertEquals(form.getMiddleName(), middleName);
         assertEquals(form.getPhoneNumber(), phoneNumber);
         assertEquals(form.getNewRole(), role);
     }
 
     @Test
     public void shouldReturnDetailsOfStaffToEdit() {
         String staffId = "123";
         String firstName = "fname";
         String middleName = "maname";
         String lastName = "lname";
         String email = null;
         String phoneNumber = "0987654321";
         String role = "CHV";
 
         ModelMap modelMap = new ModelMap();
         modelMap.addAttribute("Id", staffId);
 
         MRSPerson mrsPerson = new MRSPerson().firstName(firstName).middleName(middleName).lastName(lastName).attributes(
                 asList(new Attribute(PERSON_ATTRIBUTE_TYPE_EMAIL, email),
                         new Attribute(PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER, phoneNumber), new Attribute(PERSON_ATTRIBUTE_TYPE_STAFF_TYPE, role)));
         MRSUser mrsUser = new MRSUser().systemId(staffId).securityRole(role).person(mrsPerson);
         when(mockStaffService.getUserByEmailIdOrMotechId(staffId)).thenReturn(mrsUser);
 
         MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
         mockHttpServletRequest.setParameter("Id", staffId);
         String editFormName = controller.edit(modelMap, mockHttpServletRequest);
 
         assertThat(editFormName, is(StaffController.EDIT_STAFF_URL));
         StaffForm expectedStaffForm = createStaffForm(staffId, firstName, middleName, lastName, email, phoneNumber, role);
         StaffForm staffForm = (StaffForm) modelMap.get(StaffController.STAFF_FORM);
 
         assertStaffForm(expectedStaffForm, staffForm);
     }
 
     private void assertStaffForm(StaffForm expectedStaffForm, StaffForm staffForm) {
         assertThat(staffForm.getFirstName(), is(equalTo(expectedStaffForm.getFirstName())));
         assertThat(staffForm.getLastName(), is(equalTo(expectedStaffForm.getLastName())));
         assertThat(staffForm.getMiddleName(), is(equalTo(expectedStaffForm.getMiddleName())));
         assertThat(staffForm.getNewRole(), is(equalTo(expectedStaffForm.getNewRole())));
         assertThat(staffForm.getStaffId(), is(equalTo(expectedStaffForm.getStaffId())));
         assertThat(staffForm.getNewEmail(), is(equalTo(expectedStaffForm.getNewEmail())));
     }
 
     private StaffForm createStaffForm(String staffId, String firstName, String middleName, String lastName, String email, String phoneNumber, String role) {
         StaffForm staffForm = new StaffForm();
         staffForm.setStaffId(staffId);
         staffForm.setFirstName(firstName);
         staffForm.setMiddleName(middleName);
         staffForm.setLastName(lastName);
         staffForm.setNewEmail(email);
         staffForm.setPhoneNumber(phoneNumber);
         staffForm.setNewRole(role);
         return staffForm;
     }
 
     @Test
     public void shouldUpdateUserAndSendEmailIfUpdatedToAdmin() throws UserAlreadyExistsException {
         String id = "1";
         String staffId = "112";
         String first = "first";
         String last = "last";
         String phoneNumber = "0123456789";
         String role = "Super Admin";
         String email = "";
         StaffForm staffForm = new StaffForm(id, staffId, first, "", last, email, phoneNumber, role, "HPO", "");
         ModelMap modelMap = new ModelMap();
         MRSUser mockMRSUser = staffForm.createUser().systemId("1234");
         when(mockStaffService.getUserByEmailIdOrMotechId(staffId)).thenReturn(mockMRSUser);
         when(mockStaffService.changePasswordByEmailId(email)).thenReturn(Constants.EMAIL_SUCCESS);
         final HashMap userData = new HashMap();
         userData.put(OpenMRSUserAdapter.USER_KEY, mockMRSUser);
         when(mockStaffService.updateUser(Matchers.<MRSUser>any())).thenReturn(userData);
 
         String result = controller.update(staffForm, mock(BindingResult.class), modelMap);
 
         ArgumentCaptor<MRSUser> captor = ArgumentCaptor.forClass(MRSUser.class);
         verify(mockStaffService).updateUser(captor.capture());
         assertThat(result, is(StaffController.EDIT_STAFF_URL));
         verify(mockStaffService).changePasswordByEmailId(email);
 
         MRSUser actualUser = captor.getValue();
         assertThat(actualUser.getId(), is(id));
         assertThat(actualUser.getSystemId(), is(staffId));
         MRSPerson mrsPerson = actualUser.getPerson();
         assertThat(mrsPerson.getFirstName(), is(first));
         assertThat(mrsPerson.getLastName(), is(last));
         assertThat(mrsPerson.getAttributes().size(), is(3));
         assertThat(attrValue(mrsPerson.getAttributes(), PERSON_ATTRIBUTE_TYPE_EMAIL), is(email));
         assertThat(attrValue(mrsPerson.getAttributes(), PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER), is(phoneNumber));
         assertThat(attrValue(mrsPerson.getAttributes(), PERSON_ATTRIBUTE_TYPE_STAFF_TYPE), is(role));
 
         assertNotNull(modelMap.get("roles"));
         assertNotNull(modelMap.get(StaffController.STAFF_FORM));
     }
 
     @Test
     public void shouldUpdateUserAndNotSendEmailIfRoleIsNotChanged() throws UserAlreadyExistsException {
         String id = "1";
         String staffId = "112";
         String first = "first";
         String last = "last";
         String phoneNumber = "0123456789";
         String role = "Super Admin";
         String email = "";
         StaffForm staffForm = new StaffForm(id, staffId, first, "", last, email, phoneNumber, role, role, email);
         ModelMap modelMap = new ModelMap();
         MRSUser mockMRSUser = new MRSUser().userName(email).person(new MRSPerson().firstName(first).middleName("").lastName(last));
 
         when(mockStaffService.changePasswordByEmailId(email)).thenReturn(Constants.EMAIL_SUCCESS);
         final HashMap userData = new HashMap();
         userData.put(OpenMRSUserAdapter.USER_KEY, mockMRSUser);
         when(mockStaffService.updateUser(Matchers.<MRSUser>any())).thenReturn(userData);
         when(mockStaffService.getUserByEmailIdOrMotechId(staffId)).thenReturn(mockMRSUser);
 
         String result = controller.update(staffForm, mock(BindingResult.class), modelMap);
 
         ArgumentCaptor<MRSUser> captor = ArgumentCaptor.forClass(MRSUser.class);
         verify(mockStaffService).updateUser(captor.capture());
         assertThat(result, is(StaffController.EDIT_STAFF_URL));
         verify(mockStaffService, never()).changePasswordByEmailId(email);
     }
 
     @Test
     public void shouldUpdateUserAndNotSendEmailIfRoleNotChangedToAdmin() throws UserAlreadyExistsException {
         String id = "1";
         String staffId = "112";
         String first = "first";
         String last = "last";
         String phoneNumber = "0123456789";
         String role = "MMA";
         String email = "";
         StaffForm staffForm = new StaffForm(id, staffId, first, "", last, email, phoneNumber, role, "HPO", "");
         ModelMap modelMap = new ModelMap();
 
         MRSUser mrsUser = staffForm.createUser();
         MRSPerson mockMRSPerson = mock(MRSPerson.class);
         when(mockMRSPerson.getFirstName()).thenReturn(first);
         when(mockMRSPerson.getMiddleName()).thenReturn("");
         when(mockMRSPerson.getLastName()).thenReturn(last);
 
         when(mockStaffService.getUserByEmailIdOrMotechId(staffId)).thenReturn(mrsUser);
         when(mockStaffService.changePasswordByEmailId(email)).thenReturn(Constants.EMAIL_SUCCESS);
         final HashMap userData = new HashMap();
         userData.put(OpenMRSUserAdapter.USER_KEY, mrsUser);
         when(mockStaffService.updateUser(Matchers.<MRSUser>any())).thenReturn(userData);
 
         String result = controller.update(staffForm, mock(BindingResult.class), modelMap);
 
         ArgumentCaptor<MRSUser> captor = ArgumentCaptor.forClass(MRSUser.class);
         verify(mockStaffService).updateUser(captor.capture());
         assertThat(result, is(StaffController.EDIT_STAFF_URL));
         verify(mockStaffService, never()).changePasswordByEmailId(email);
 
         MRSUser actualUser = captor.getValue();
         MRSPerson mrsPerson = actualUser.getPerson();
         assertThat(actualUser.getId(), is(id));
         assertThat(actualUser.getSystemId(), is(staffId));
         assertThat(mrsPerson.getFirstName(), is(first));
         assertThat(mrsPerson.getLastName(), is(last));
         assertThat(mrsPerson.getAttributes().size(), is(3));
         assertThat(attrValue(mrsPerson.getAttributes(), PERSON_ATTRIBUTE_TYPE_EMAIL), is(email));
         assertThat(attrValue(mrsPerson.getAttributes(), PERSON_ATTRIBUTE_TYPE_PHONE_NUMBER), is(phoneNumber));
         assertThat(attrValue(mrsPerson.getAttributes(), PERSON_ATTRIBUTE_TYPE_STAFF_TYPE), is(role));
 
         assertNotNull(modelMap.get("roles"));
         assertNotNull(modelMap.get(StaffController.STAFF_FORM));
     }
 
     @Test
     public void shouldThrowUserAlreadyFoundExceptionIfEmailIdIsUpdatedToAnExistingUserId() {
         String id = "1";
         String staffId = "112";
         String first = "first";
         String last = "last";
         String phoneNumber = "0123456789";
         String role = "MMA";
         String email = "";
         StaffForm staffForm = new StaffForm(id, staffId, first, "", last, email, phoneNumber, role, "HPO", "newemail@e.com");
         ModelMap modelMap = new ModelMap();
         MRSUser mockMRSUser = mock(MRSUser.class);
         when(mockStaffService.getUserByEmailIdOrMotechId(email)).thenReturn(mockMRSUser);
 
         final BindingResult mockBindingResult = mock(BindingResult.class);
         final String result = controller.update(staffForm, mockBindingResult, modelMap);
        assertThat(result, is(equalTo(StaffController.NEW_STAFF_URL)));
         verify(mockStaffService, never()).updateUser(mockMRSUser);
         final ArgumentCaptor<FieldError> captor = ArgumentCaptor.forClass(FieldError.class);
         verify(mockBindingResult).addError(captor.capture());
         final FieldError actualError = captor.getValue();
 
         assertThat(StaffController.EMAIL, is(actualError.getField()));
         assertThat(StaffController.STAFF_FORM, is(actualError.getObjectName()));
     }
 
     private String attrValue(List<Attribute> attributes, String key) {
         List<Attribute> filteredItems = select(attributes, having(on(Attribute.class).name(), equalTo(key)));
         return isNotEmpty(filteredItems) ? filteredItems.get(0).value() : null;
     }
 }
 
 
