 package org.patientview.radar.dao;
 
 import org.patientview.model.Centre;
 import org.patientview.model.Clinician;
 import org.patientview.model.Country;
 import org.patientview.model.Ethnicity;
 import org.patientview.radar.model.Consultant;
 import org.patientview.radar.model.Relative;
 import org.patientview.radar.model.DiagnosisCode;
 import org.patientview.radar.model.filter.ConsultantFilter;
 
 import java.util.List;
 import java.util.Map;
 
 public interface UtilityDao {
 
     Centre getCentre(long id);
 
     List<Centre> getCentres();
 
     Consultant getConsultant(long id);
 
     void deleteUnit(String unitCode);
 
     void deletePatientViewUser(String username);
 
     void deletePatientViewMapping(String username);
 
     void createUnit(String unitCode);
 
     List<Consultant> getConsultants(ConsultantFilter filter, int page, int numberPerPage);
 
     List<Consultant> getConsultantsByCentre(Centre centre);
 
     void saveConsultant(Consultant consultant) throws Exception;
 
     void deleteConsultant(Consultant consultant) throws Exception;
 
     Country getCountry(long id);
 
     List<Country> getCountries();
 
     Ethnicity getEthnicityByCode(String ethnicityCode);
 
     List<Ethnicity> getEthnicities();
 
     Relative getRelative(long id);
 
     List<Relative> getRelatives();
 
     Map<Long, Integer> getPatientCountPerUnitByDiagnosisCode(DiagnosisCode diagnosisCode);
 
     int getPatientCountByUnit(Centre centre);
 
     Clinician getClinician(Long id);
 
     List<Clinician> getClinicians(Centre centre);
 
     Centre getCentre(String unitCode);
 
     List<Centre> getCentres(String nhsNo);
 
     String getUserName(String nhsNo);
 
     boolean isGroupAdmin(String username);
 
     String getUserName(Long id);
 
    List<Centre> getRenalUnitCentre(String nhsNo);
 
     void deletePatient(String nshNo);
 
     void deletePatientForRadar(Long radarId);
 }
