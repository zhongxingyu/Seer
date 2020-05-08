 package com.worthsoln.patientview;
 
 import com.worthsoln.actionutils.ActionUtils;
 import com.worthsoln.database.DatabaseDAO;
 import com.worthsoln.database.action.DatabaseAction;
 import com.worthsoln.patientview.edtacode.EdtaCodeUtils;
 import com.worthsoln.patientview.logon.LogonUtils;
 import com.worthsoln.patientview.news.NewsUtils;
 import com.worthsoln.utils.LegacySpringUtils;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.DynaActionForm;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.List;
 
 public class PatientDetailsAction extends DatabaseAction {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
         NewsUtils.putAppropriateNewsForViewingInRequest(request);
 
         DatabaseDAO dao = getDao(request);
 
         List<PatientDetails> patientDetails = PatientDetailsUtils.buildPatientDetails(request, dao);
 
         request.setAttribute("patientDetails", patientDetails);
 
        if (patientDetails != null && patientDetails.size() > 0) {
             // add the editable ibd only patient details
             DynaActionForm dynaForm = (DynaActionForm) form;
             // let's just store this info against the first patient object if there are many for this nhsno
             dynaForm.set("patientId", patientDetails.get(0).getPatient().getId());
             dynaForm.set("otherConditions", patientDetails.get(0).getPatient().getOtherConditions());
             dynaForm.set("email", LegacySpringUtils.getUserManager().getLoggedInUser().getEmail());
         }
 
         EdtaCodeUtils.addEdtaCodeToRequest("static", "staticLinks", dao, request);
         ActionUtils.setUpNavLink(mapping.getParameter(), request);
         return LogonUtils.logonChecks(mapping, request);
     }
 
     public String getDatabaseName() {
         return "patientview";
     }
 
     public String getIdentifier() {
         return "edtaCode";
     }
 }
