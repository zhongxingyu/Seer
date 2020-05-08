 package org.motechproject.whp.reports.export.query.dao;
 
 import org.apache.commons.lang3.StringUtils;
 import org.motechproject.whp.reports.export.query.model.DateRange;
 import org.motechproject.whp.reports.export.query.model.PatientReportRequest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PatientSummaryQueryBuilder {
 
     public static final String PATIENT_SUMMARY_SELECT_SQL = "select p.first_name, p.last_name, p.gender, p.patient_id, " +
             "treatment.tb_id, treatment.provider_id, treatment.provider_district, therapy.treatment_category, " +
             "treatment.start_date as tb_registration_date, therapy.start_date as treatment_start_date, therapy.disease_class, treatment.patient_type, " +
             "therapy.ip_pills_taken, therapy.ip_total_doses, therapy.cp_pills_taken, " +
             "therapy.cp_total_doses, therapy.cumulative_missed_doses, " +
             "treatment.treatment_outcome, treatment.end_date as treatment_closing_date, treatment.pretreatment_result as pre_treatment_sputum_result, " +
             "treatment.pretreatment_weight as pre_treatment_weight, therapy.patient_age as age, address.village from whp_reports.patient p " +
             "join whp_reports.patient_therapy therapy on p.patient_pk=therapy.patient_fk " +
             "join whp_reports.patient_treatment treatment on therapy.therapy_pk = treatment.therapy_fk " +
             "join whp_reports.patient_address address on p.patient_address_fk = address.address_pk " ;
 
     public static final String PATIENT_SUMMARY_SORT_SQL = " order by treatment_start_date";
     public static final String WHERE_CLAUSE = "where";
 
     private PatientReportRequest patientReportRequest;
 
     public PatientSummaryQueryBuilder(PatientReportRequest patientReportRequest) {
         this.patientReportRequest = patientReportRequest;
     }
 
     public String build() {
         return PATIENT_SUMMARY_SELECT_SQL + buildPredicate() + PATIENT_SUMMARY_SORT_SQL;
     }
 
     private String buildPredicate() {
         List<String> predicates = new ArrayList<>();
 
        if(StringUtils.isNotEmpty(patientReportRequest.getDistrict())){
             predicates.add(String.format(" provider_district = '%s'", patientReportRequest.getDistrict()));
         }
 
         DateRange dateRange = new DateRange(patientReportRequest.getFrom(), patientReportRequest.getTo());
         predicates.add(tbRegistrationDateRangePredicate(dateRange.getStartDate(), dateRange.getEndDate()));
         return  WHERE_CLAUSE + StringUtils.join(predicates, " AND");
     }
 
     private String tbRegistrationDateRangePredicate(String from, String to) {
         return String.format(" treatment.start_date between '%s' AND '%s'",
                 from,
                 to);
     }
 }
