 package org.wv.stepsovc.commcare.gateway;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.velocity.app.VelocityEngine;
import org.motechproject.context.Context;
import org.motechproject.http.client.constants.EventSubjects;
import org.motechproject.http.client.listener.HttpClientEventListener;
 import org.motechproject.http.client.service.HttpClientService;
import org.motechproject.server.event.EventListenerRegistry;
import org.motechproject.server.event.annotations.EventAnnotationBeanPostProcessor;
import org.motechproject.server.event.annotations.MotechListenerEventProxy;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.util.ReflectionUtils;
 import org.wv.stepsovc.commcare.domain.Group;
 import org.wv.stepsovc.commcare.factories.GroupFactory;
 import org.wv.stepsovc.commcare.repository.AllGroups;
 import org.wv.stepsovc.commcare.repository.AllUsers;
 import org.wv.stepsovc.commcare.vo.BeneficiaryInformation;
 import org.wv.stepsovc.commcare.vo.CaregiverInformation;
 import org.wv.stepsovc.commcare.vo.CaseOwnershipInformation;
 import org.wv.stepsovc.commcare.vo.FacilityInformation;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 
 @Component
 public class CommcareGateway {
 
     public static final String BENEFICIARY_CASE_FORM_TEMPLATE_PATH = "/templates/beneficiary-case-form.xml";
     public static final String OWNER_UPDATE_FORM_TEMPLATE_PATH = "/templates/update-owner-form.xml";
     public static final String USER_REGISTRATION_FORM_TEMPLATE_PATH = "/templates/user-registration-form.xml";
     public static final String FACILITY_REGISTRATION_FORM_TEMPLATE_PATH = "/templates/facility-registration-form.xml";
     public static final String FACILITY_CASE_FORM_TEMPLATE_PATH = "/templates/facility-case-form.xml";
     public static final String OWNERSHIP_CASE_REGISTER_FORM_TEMPLATE_PATH = "/templates/ownership-case-register-form.xml";
     public static final String BENEFICIARY_FORM_KEY = "beneficiary";
     public static final String CASE_OWNERSHIP_FORM_KEY = "caseOwnershipRequest";
     public static final String CARE_GIVER_FORM_KEY = "caregiver";
     public static final String FACILITY_FORM_KEY = "facility";
     public static final String OWNER_ID_KEY = "ownerId";
     public static final String ALL_USERS_GROUP = "ALL_USERS";
 
     public static final String STEPSOVC = "stepsovc";
 
     @Value("#{stepovcProperties['commcare.receiver']}")
     private String COMMCARE_RECIEVER_URL;
     @Autowired
     private VelocityEngine velocityEngine;
     @Autowired
     private AllGroups allGroups;
     @Autowired
     private AllUsers allUsers;
     @Autowired
     private HttpClientService httpClientService;
    private HttpClientEventListener httpClientEventListener = new HttpClientEventListener();

    @Autowired
    public CommcareGateway(EventAnnotationBeanPostProcessor eventAnnotationBeanPostProcessor) {
        if(eventAnnotationBeanPostProcessor != null)
            eventAnnotationBeanPostProcessor.postProcessAfterInitialization(httpClientEventListener, "httpClientEventListener");
    }
 
     public String getUserId(String name) {
         return allUsers.getUserByName(name) == null ? null : allUsers.getUserByName(name).getId();
     }
 
     private String getGroupId(String name) {
         return allGroups.getGroupByName(name) == null ? null : allGroups.getGroupByName(name).getId();
     }
 
     public void createOrUpdateGroup(String groupName, String[] commcareUserIds) {
 
         Group existingGroup = allGroups.getGroupByName(groupName);
         if (existingGroup != null) {
             existingGroup.setUsers((String[]) ArrayUtils.addAll(existingGroup.getUsers(), commcareUserIds));
             allGroups.update(existingGroup);
         } else {
             Group newGroup = GroupFactory.createGroup(groupName, commcareUserIds);
             allGroups.add(newGroup);
         }
     }
 
     public void addGroupOwnership(CaseOwnershipInformation caseOwnershipInformation, String groupName) {
         caseOwnershipInformation.setOwnerId(caseOwnershipInformation.getUserId() + "," + getGroupId(groupName));
         postOwnerUpdate(caseOwnershipInformation);
     }
 
     public void removeGroupOwnership(CaseOwnershipInformation caseOwnershipInformation, String groupName) {
         int indexOfGroupId = caseOwnershipInformation.getOwnerId().indexOf(getGroupId(groupName));
         if (indexOfGroupId != -1) {
             caseOwnershipInformation.setOwnerId(removeGroupIdFromOwnerId(getGroupId(groupName), caseOwnershipInformation.getOwnerId(), indexOfGroupId));
         }
         postOwnerUpdate(caseOwnershipInformation);
     }
 
     public void addUserOwnership(CaseOwnershipInformation caseOwnershipInformation, String userId) {
         caseOwnershipInformation.setOwnerId(caseOwnershipInformation.getUserId() + "," + userId);
         postOwnerUpdate(caseOwnershipInformation);
     }
 
     private String removeGroupIdFromOwnerId(String groupId, String ownerId, int indexOfGroupId) {
         return ownerId.substring(0, indexOfGroupId - 1) + ownerId.substring(indexOfGroupId + groupId.length());
     }
 
     public void createCase(BeneficiaryInformation beneficiaryInformation) {
         Map<String, Object> model = new HashMap<String, Object>();
         model.put(BENEFICIARY_FORM_KEY, beneficiaryInformation);
         httpClientService.post(COMMCARE_RECIEVER_URL, getXmlFromObject(BENEFICIARY_CASE_FORM_TEMPLATE_PATH, model));
     }
 
     public void registerCaregiver(CaregiverInformation careGiverInformation) {
         Map<String, Object> model = new HashMap<String, Object>();
         model.put(CARE_GIVER_FORM_KEY, careGiverInformation);
         httpClientService.post(COMMCARE_RECIEVER_URL, getXmlFromObject(USER_REGISTRATION_FORM_TEMPLATE_PATH, model));
     }
 
     void postOwnerUpdate(CaseOwnershipInformation caseOwnershipInformation) {
         Map<String, Object> model = new HashMap<String, Object>();
         model.put(CASE_OWNERSHIP_FORM_KEY, caseOwnershipInformation);
         httpClientService.post(COMMCARE_RECIEVER_URL, getXmlFromObject(OWNER_UPDATE_FORM_TEMPLATE_PATH, model));
     }
 
     public String getXmlFromObject(String templatePath, Map model) {
         return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templatePath, "UTF-8", model);
     }
 
     public String getCOMMCARE_RECIEVER_URL() {
         return COMMCARE_RECIEVER_URL;
     }
 
     public void registerFacilityUser(FacilityInformation facilityInformation) {
         Map<String, Object> model = new HashMap<String, Object>();
         model.put(FACILITY_FORM_KEY, facilityInformation);
         httpClientService.post(COMMCARE_RECIEVER_URL, getXmlFromObject(FACILITY_REGISTRATION_FORM_TEMPLATE_PATH, model));
     }
 
     public void createFacilityCase(FacilityInformation facilityInformation) {
         String facilityCaseDocId = UUID.randomUUID().toString();
         facilityInformation.setId(facilityCaseDocId);
         Map<String, Object> model = new HashMap<String, Object>();
         model.put(FACILITY_FORM_KEY, facilityInformation);
         httpClientService.post(COMMCARE_RECIEVER_URL, getXmlFromObject(FACILITY_CASE_FORM_TEMPLATE_PATH, model));
     }
 
     public void createOwnershipCase() {
         Group allUsersGroup = allGroups.getGroupByName(CommcareGateway.ALL_USERS_GROUP);
         Map<String, Object> model = new HashMap<String, Object>();
         model.put(OWNER_ID_KEY, allUsersGroup.getId());
         httpClientService.post(COMMCARE_RECIEVER_URL, getXmlFromObject(OWNERSHIP_CASE_REGISTER_FORM_TEMPLATE_PATH, model));
     }
 }
