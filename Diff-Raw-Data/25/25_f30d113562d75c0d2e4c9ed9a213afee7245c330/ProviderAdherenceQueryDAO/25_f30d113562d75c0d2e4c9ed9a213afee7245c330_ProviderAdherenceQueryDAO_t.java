 package org.motechproject.whp.reports.dao;
 
 import org.joda.time.LocalDate;
 import org.motechproject.whp.reports.contract.adherence.ProviderAdherenceSummary;
 import org.motechproject.whp.reports.date.WHPDate;
 import org.motechproject.whp.reports.model.AdherenceStatus;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 
 import javax.sql.DataSource;
 import java.sql.Date;
 import java.util.List;
 
 import static org.motechproject.util.DateUtil.today;
 import static org.motechproject.whp.reports.domain.TreatmentWeekInstance.week;
 
 @Repository
 public class ProviderAdherenceQueryDAO {
     private JdbcTemplate jdbcTemplate;
 
     ProviderAdherenceQueryDAO() {
     }
 
     @Autowired
     public ProviderAdherenceQueryDAO(DataSource dataSource) {
         jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     private static final String PROVIDER_CURRENT_ADHERENCE_SUMMARY_QUERY = "select provider.provider_id, " +
             "provider.primary_mobile, provider.secondary_mobile,provider.tertiary_mobile, " +
             "case when exists " +
             "(select 1 from whp_reports.adherence_record where provider.provider_id = adherence_record.provider_id " +
             "and pill_date between ? and ?) then '1' else '0' end as adherence_given " +
             "from whp_reports.provider where exists (select 1 from  whp_reports.patient_therapy therapy " +
             "inner join whp_reports.patient_treatment treatment on treatment.therapy_fk = therapy.therapy_pk " +
             "where treatment.provider_id = provider.provider_id " +
             "and treatment.is_current_treatment = 'Y' " +
             "and therapy.status != 'Closed' " +
             "and therapy.start_date <= ? " +
             "and treatment.end_date is  null) " +
             "and provider.district = ?";
 
     public List<ProviderAdherenceSummary> getProviderAdherenceSummaries(String district) {
         LocalDate today = today();
         LocalDate treatmentWeekStartDate = week(today).startDate();
         LocalDate treatmentWeekEndDate = week(today).endDate();
 
         Date startDate = WHPDate.toSqlDate(treatmentWeekStartDate);
         Date endDate = WHPDate.toSqlDate(treatmentWeekEndDate);
 
         return jdbcTemplate.query(PROVIDER_CURRENT_ADHERENCE_SUMMARY_QUERY,
                 new BeanPropertyRowMapper(ProviderAdherenceSummary.class), startDate, endDate, startDate, district);
     }
 
 
     private static final String PROVIDER_ADHERENCE_AUDIT_QUERY = "select provider.provider_id, " +
             "case when exists(select 1 from whp_reports.adherence_audit_log " +
             "where provider.provider_id = adherence_audit_log.user_id " +
            "and adherence_audit_log.creation_time  -  interval '1 week' between ? and ?) " +
             "then '1' else '0' end as adherence_given " +
             "from whp_reports.provider " +
             "where provider.district = ? and exists( " +
             "select all_treatment.provider_id " +
             "from  whp_reports.patient_therapy all_therapy " +
             "inner join whp_reports.patient_treatment all_treatment " +
             "on all_treatment.therapy_fk = all_therapy.therapy_pk " +
             "where " +
             "(all_therapy.start_date >= all_treatment.start_date and all_therapy.start_date <=  ? and (all_therapy.close_date is null or  all_therapy.close_date >= ?) " +
             "OR  (all_therapy.start_date < all_treatment.start_date and all_treatment.start_date <=  ? and  (all_treatment.end_date is  null or all_treatment.end_date >= ?))) " +
             "and " +
             "exists (select 1 from  whp_reports.patient_therapy therapy " +
             "inner join whp_reports.patient_treatment treatment on treatment.therapy_fk = therapy.therapy_pk " +
             "where treatment.provider_id = provider.provider_id and treatment.provider_id = all_treatment.provider_id " +
             "and therapy.status != 'Closed' and treatment.end_date is  null))";
 
     public List<AdherenceStatus> getAdherenceGivenStatus(String district, Date startDate, Date endDate) {
         return jdbcTemplate.query(PROVIDER_ADHERENCE_AUDIT_QUERY,
                 new BeanPropertyRowMapper(AdherenceStatus.class),
                 startDate, endDate, district, startDate, endDate, startDate, endDate);
     }
 }
