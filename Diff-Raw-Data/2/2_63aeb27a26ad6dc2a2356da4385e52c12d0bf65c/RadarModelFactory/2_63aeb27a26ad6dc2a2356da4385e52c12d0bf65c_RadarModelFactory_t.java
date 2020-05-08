 package com.solidstategroup.radar.web.models;
 
 
 import com.solidstategroup.radar.model.Diagnosis;
 import com.solidstategroup.radar.model.DiagnosisCode;
 import com.solidstategroup.radar.model.sequenced.ClinicalData;
 import com.solidstategroup.radar.service.ClinicalDataManager;
 import com.solidstategroup.radar.service.DemographicsManager;
 import com.solidstategroup.radar.service.DiagnosisManager;
 import com.solidstategroup.radar.web.panels.DiagnosisPanel;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 public class RadarModelFactory {
 
 
     public static IModel<Diagnosis> getDiagnosisModel(final IModel<Long> radarNumberModel,
                                                       final DiagnosisManager diagnosisManager) {
         return new AbstractReadOnlyModel<Diagnosis>() {
 
             @Override
             public Diagnosis getObject() {
                     Diagnosis diagnosis = null;
                     Long radarNumber;
                     if (radarNumberModel.getObject() != null) {
                         try {
                             radarNumber = radarNumberModel.getObject();
                         } catch (ClassCastException e) {
                             Object obj = radarNumberModel.getObject();
                             radarNumber = Long.parseLong((String) obj);
                         }
                         diagnosis = diagnosisManager.getDiagnosisByRadarNumber(radarNumber);
                     }
 
                 return diagnosis;
             }
         };
     }
 
     public static IModel<Boolean> getIsSrnsModel(final IModel radarNumberModel, final
     DiagnosisManager diagnosisManager) {
         return new AbstractReadOnlyModel<Boolean>() {
 
             // cache the result as this model will be used on isVisible methods which are called
             // many times and querying the db will slow down the system too much
             private DiagnosisCode diagnosisCode = null;
 
             @Override
             public Boolean getObject() {
                 if (diagnosisCode == null) {
                     if (radarNumberModel.getObject() != null) {
                         diagnosisCode = getDiagnosisCodeModel(radarNumberModel, diagnosisManager).getObject();
                     }
                 }
                 if (diagnosisCode != null) {
                     return diagnosisCode.getId().equals(DiagnosisPanel.SRNS_ID);
                 }
                 return false;
             }
         };
     }
 
     public static IModel<ClinicalData> getFirstClinicalDataModel(final IModel<Long> radarNumberModel,
                                                                  final ClinicalDataManager clinicalDataManager) {
         return new AbstractReadOnlyModel<ClinicalData>() {
             @Override
             public ClinicalData getObject() {
                 ClinicalData clinicalData = null;
                     Long radarNumber;
                     if (radarNumberModel.getObject() != null) {
                         try {
                             radarNumber = radarNumberModel.getObject();
                         } catch (ClassCastException e) {
                             Object obj = radarNumberModel.getObject();
                             radarNumber = Long.parseLong((String) obj);
                         }
                         List<ClinicalData> clinicalDatas = clinicalDataManager.
                                 getClinicalDataByRadarNumber(radarNumber);
                         if (!clinicalDatas.isEmpty()) {
                             clinicalData = clinicalDatas.get(0);
                         }
                     }
                 return clinicalData;
             }
         };
     }
 
     public static IModel<DiagnosisCode> getDiagnosisCodeModel(final IModel<Long> radarNumberModel,
                                                               final DiagnosisManager diagnosisManager) {
         return new LoadableDetachableModel<DiagnosisCode>() {
             @Override
             public DiagnosisCode load() {
 
                 Long radarNumber = null;
                 if (radarNumberModel.getObject() != null) {
                     try {
                         radarNumber = radarNumberModel.getObject();
                     } catch (ClassCastException e) {
                         Object obj = radarNumberModel.getObject();
                         radarNumber = Long.parseLong((String) obj);
                     }
 
                     Diagnosis diagnosis = diagnosisManager.getDiagnosisByRadarNumber(radarNumber);
                     if (diagnosis != null) {
                         return diagnosis.getDiagnosisCode();
                     } else {
                         return null;
                     }
 
                 } else {
                     return null;
                 }
 
             }
         };
     }
 
     public static IModel getFirstNameModel(final IModel<Long> radarNumberModel, final DemographicsManager
             demographicsManager) {
         return new Model() {
             @Override
             public Serializable getObject() {
                 try {
                     return radarNumberModel.getObject() != null ? demographicsManager.getDemographicsByRadarNumber(
                             radarNumberModel.getObject()).getForename() : null;
                 } catch (ClassCastException e) {
                     Object obj = radarNumberModel.getObject();
                     return obj != null ? demographicsManager.getDemographicsByRadarNumber(Long.parseLong((String) obj)).
                             getForename() : null;
                 }
             }
         };
     }
 
     public static IModel getSurnameModel(final IModel<Long> radarNumberModel, final DemographicsManager
             demographicsManager) {
         return new Model() {
             @Override
             public Serializable getObject() {
                 try {
                     return radarNumberModel.getObject() != null ? demographicsManager.getDemographicsByRadarNumber(
                             radarNumberModel.getObject()).getSurname() : null;
                 } catch (ClassCastException e) {
                     Object obj = radarNumberModel.getObject();
                     return obj != null ? demographicsManager.getDemographicsByRadarNumber(Long.parseLong((String) obj)).
                             getSurname() : null;
                 }
             }
         };
     }
 
     public static IModel getDobModel(final IModel<Long> radarNumberModel, final DemographicsManager
             demographicsManager) {
         return new Model() {
             @Override
             public Serializable getObject() {
                 try {
                     return radarNumberModel.getObject() != null ? demographicsManager.getDemographicsByRadarNumber(
                             radarNumberModel.getObject()).getDateOfBirth() : null;
                 } catch (ClassCastException e) {
                     Object obj = radarNumberModel.getObject();
                     return obj != null ? demographicsManager.getDemographicsByRadarNumber(Long.parseLong((String) obj)).
                             getDateOfBirth() : null;
                 }
             }
 
         };
     }
 
     public static IModel<String> getHospitalNumberModel(final IModel<Long> radarNumberModel,
                                                         final DemographicsManager demographicsManager) {
         return new Model<String>() {
             @Override
             public String getObject() {
                 try {
                     return radarNumberModel.getObject() != null ? demographicsManager.getDemographicsByRadarNumber(
                             radarNumberModel.getObject()).getHospitalNumber() : null;
                 } catch (ClassCastException e) {
                     Object obj = radarNumberModel.getObject();
                     return obj != null ? demographicsManager.getDemographicsByRadarNumber(Long.parseLong((String) obj)).
                             getHospitalNumber() : null;
                 }
             }
         };
     }
 
 
     public static IModel getSuccessMessageModel(final Form form) {
         return new LoadableDetachableModel() {
             @Override
             protected Object load() {
                return form.hasError() ? "" : "Save was successful: " + new SimpleDateFormat("H:m:s").
                         format(new Date());
             }
         };
     }
 
     public static PageNumberModel getPageNumberModel(final int pageNo, IModel radarNumberModel, DiagnosisManager
             diagnosisManager) {
 
         final IModel<Boolean> isSrnsModel = getIsSrnsModel(radarNumberModel, diagnosisManager);
         return new PageNumberModel(pageNo) {
             @Override
             public String getObject() {
                 return getPageNumber() + " " + (isSrnsModel.getObject() ? "B" : "A");
             }
         };
     }
 }
