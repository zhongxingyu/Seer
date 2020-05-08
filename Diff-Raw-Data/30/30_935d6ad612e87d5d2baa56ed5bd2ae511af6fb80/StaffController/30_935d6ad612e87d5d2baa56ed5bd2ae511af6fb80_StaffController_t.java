 package org.motechproject.ghana.national.web;
 
 import org.motechproject.ghana.national.domain.StaffType;
 import org.motechproject.ghana.national.service.StaffService;
 import org.motechproject.ghana.national.web.form.StaffForm;
 import org.motechproject.ghana.national.web.helper.StaffHelper;
 import org.motechproject.mrs.exception.UserAlreadyExistsException;
 import org.motechproject.mrs.model.MRSPerson;
 import org.motechproject.mrs.model.MRSUser;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.motechproject.openmrs.services.OpenMRSUserAdapter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 @Controller
 @RequestMapping(value = "/admin/staffs")
 public class StaffController {
     public static final String REQUESTED_STAFFS = "requestedStaffs";
     private StaffService staffService;
     private MessageSource messageSource;
 
     private StaffHelper staffHelper;
     public static final String STAFF_ID = "userId";
     public static final String STAFF_SEQUENTIAL_ID = "Id";
     public static final String STAFF_NAME = "userName";
     public static final String EMAIL = "newEmail";
     public static final String STAFF_FORM = "staffForm";
     public static final String STAFF_ALREADY_EXISTS = "user_email_already_exists";
     public static final String NEW_STAFF_URL = "staffs/new";
     public static final String SEARCH_STAFF = "staffs/search";
     public static final String EDIT_STAFF_URL = "staffs/edit";
 
     public StaffController() {
     }
 
     @Autowired
     public StaffController(StaffService staffService, MessageSource messageSource, StaffHelper staffHelper) {
         this.staffService = staffService;
         this.messageSource = messageSource;
         this.staffHelper = staffHelper;
     }
 
     @ApiSession
     @RequestMapping(value = "new", method = RequestMethod.GET)
     public String newStaff(ModelMap modelMap) {
         modelMap.addAttribute(STAFF_FORM, new StaffForm());
         staffHelper.populateRoles(modelMap, staffService.fetchAllRoles());
         return NEW_STAFF_URL;
     }
 
     @ApiSession
     @RequestMapping(value = "create", method = RequestMethod.POST)
     public String create(@Valid StaffForm staffForm, BindingResult bindingResult, ModelMap modelMap) {
         try {
             MRSUser openMRSUser = staffService.saveUser(staffForm.createUser());
             modelMap.put(STAFF_ID, openMRSUser.getSystemId());
             modelMap.put(STAFF_NAME, openMRSUser.getPerson().getFullName());
             modelMap.put("successMessage", "Staff created successfully.Email with login credentials sent (to admin users only).");
             staffHelper.populateRoles(modelMap, staffService.fetchAllRoles());
             return staffHelper.getStaffForId(modelMap, openMRSUser);
         } catch (UserAlreadyExistsException e) {
             handleUserAlreadyExistsError(modelMap, bindingResult);
             return NEW_STAFF_URL;
         }
     }
 
     @ApiSession
     @RequestMapping(value = "edit", method = RequestMethod.GET)
     public String edit(ModelMap modelMap, HttpServletRequest httpServletRequest) {
         String staffId = httpServletRequest.getParameter(STAFF_SEQUENTIAL_ID);
         staffHelper.populateRoles(modelMap, staffService.fetchAllRoles());
         return staffHelper.getStaffForId(modelMap, staffService.getUserByEmailIdOrMotechId(staffId));
     }
 
     @ApiSession
     @RequestMapping(value = "update", method = RequestMethod.POST)
     public String update(@Valid StaffForm staffForm, BindingResult bindingResult, ModelMap modelMap) {
         MRSUser mrsUser = staffForm.createUser();
        MRSUser existingStaffWithSameEmail = staffService.getUserByEmailIdOrMotechId(staffForm.getNewEmail());
        if (existingStaffWithSameEmail != null && !existingStaffWithSameEmail.getId().equals(staffForm.getId())) {
             handleUserAlreadyExistsError(modelMap, bindingResult);
             return EDIT_STAFF_URL;
         }
 
         Map userData = staffService.updateUser(mrsUser);
 
         final MRSUser openMRSUser = (MRSUser) userData.get(OpenMRSUserAdapter.USER_KEY);
         if (StaffType.Role.isAdmin(staffForm.getNewRole()) && !staffForm.getNewRole().equals(staffForm.getCurrentRole())) {
             staffService.changePasswordByEmailId(staffForm.getNewEmail());
         }
         staffHelper.populateRoles(modelMap, staffService.fetchAllRoles());
         staffHelper.getStaffForId(modelMap, openMRSUser);
         modelMap.put("successMessage", "Staff edited successfully.Email with login credentials sent (to admin users only).");
         return EDIT_STAFF_URL;
     }
 
     @ApiSession
     @RequestMapping(value = "search", method = RequestMethod.GET)
     public String search(ModelMap modelMap) {
         modelMap.put(STAFF_FORM, new StaffForm());
         staffHelper.populateRoles(modelMap, staffService.fetchAllRoles());
         return SEARCH_STAFF;
     }
 
     @ApiSession
     @RequestMapping(value = "searchStaffs", method = RequestMethod.POST)
     public String search(@Valid final StaffForm staffForm, ModelMap modelMap) {
         final List<MRSUser> mrsUsers = staffService.searchStaff(staffForm.getStaffId(), staffForm.getFirstName(),
                 staffForm.getMiddleName(), staffForm.getLastName(), staffForm.getPhoneNumber(), staffForm.getNewRole());
 
         final ArrayList<StaffForm> staffForms = new ArrayList<StaffForm>();
         for (MRSUser mrsUser : mrsUsers) {
             MRSPerson mrsPerson = mrsUser.getPerson();
             staffForms.add(new StaffForm(mrsUser.getId(), mrsUser.getSystemId(), mrsPerson.getFirstName(), mrsPerson.getMiddleName(), mrsPerson.getLastName(),
                     staffHelper.getEmail(mrsUser), staffHelper.getPhoneNumber(mrsUser), staffHelper.getRole(mrsUser), null, null));
         }
         modelMap.put(STAFF_FORM, new StaffForm());
         modelMap.put(REQUESTED_STAFFS, staffForms);
         staffHelper.populateRoles(modelMap, staffService.fetchAllRoles());
         return SEARCH_STAFF;
     }
 
     private void handleUserAlreadyExistsError(ModelMap modelMap, BindingResult bindingResult) {
         bindingResult.addError(new FieldError(STAFF_FORM, EMAIL, messageSource.getMessage(STAFF_ALREADY_EXISTS, null, Locale.getDefault())));
         staffHelper.populateRoles(modelMap, staffService.fetchAllRoles());
         modelMap.mergeAttributes(bindingResult.getModel());
     }
 }
