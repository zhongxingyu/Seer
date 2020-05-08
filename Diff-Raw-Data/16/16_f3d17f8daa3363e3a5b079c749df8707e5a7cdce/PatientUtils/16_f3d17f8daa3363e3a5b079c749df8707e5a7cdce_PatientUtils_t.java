 package com.worthsoln.patientview;
 
 import com.worthsoln.patientview.model.UserMapping;
 import com.worthsoln.patientview.model.Patient;
 import com.worthsoln.patientview.model.User;
 import com.worthsoln.patientview.unit.UnitUtils;
 import com.worthsoln.patientview.user.UserUtils;
 import com.worthsoln.utils.LegacySpringUtils;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class PatientUtils {
 
     public static String retrieveNhsNo(HttpServletRequest request) {
         User user = UserUtils.retrieveUser(request);
         UserMapping userMapping = UserUtils.retrieveUserMappingsPatientEntered(user);
         return userMapping.getNhsno();
     }
 
     public static Patient retrievePatient(HttpServletRequest request) {
         String nhsno = PatientUtils.retrieveNhsNo(request);
         String unitcode = UnitUtils.retrieveUnitcode(request);
         return retrievePatient(nhsno, unitcode);
     }
 
    public static Patient retrieveBestGuessPatient(HttpServletRequest request) {
        String nhsno = PatientUtils.retrieveNhsNo(request);

        User user = UserUtils.retrieveUser(request);
        String unitCode = LegacySpringUtils.getUserManager().getUsersRealUnitcodeBestGuess(user.getUsername());

        return retrievePatient(nhsno, unitCode);
    }

     public static Patient retrievePatient(String nhsno, String unitcode) {
         return LegacySpringUtils.getPatientManager().get(nhsno, unitcode);
     }
 }
