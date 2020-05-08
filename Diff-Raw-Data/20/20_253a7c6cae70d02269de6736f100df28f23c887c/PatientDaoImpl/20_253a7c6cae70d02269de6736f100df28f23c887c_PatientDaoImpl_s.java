 package com.worthsoln.repository.impl;
 
 import com.worthsoln.patientview.logon.PatientLogonWithTreatment;
 import com.worthsoln.patientview.model.Patient;
 import com.worthsoln.patientview.model.Patient_;
 import com.worthsoln.patientview.model.Specialty;
 import com.worthsoln.patientview.unit.UnitUtils;
 import com.worthsoln.repository.AbstractHibernateDAO;
 import com.worthsoln.repository.PatientDao;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 @Repository(value = "patientDao")
 public class PatientDaoImpl extends AbstractHibernateDAO<Patient> implements PatientDao {
 
     private JdbcTemplate jdbcTemplate;
 
     @Inject
     private DataSource dataSource;
 
     @PostConstruct
     public void init() {
         jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     @Override
     public Patient get(String nhsno, String unitcode) {
 
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Patient> criteria = builder.createQuery(Patient.class);
         Root<Patient> from = criteria.from(Patient.class);
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         wherePredicates.add(builder.equal(from.get(Patient_.nhsno), nhsno));
         wherePredicates.add(builder.equal(from.get(Patient_.centreCode), unitcode));
 
         buildWhereClause(criteria, wherePredicates);
 
         try {
             return getEntityManager().createQuery(criteria).getSingleResult();
         } catch (Exception e) {
             return null;
         }
     }
 
     @Override
     public void delete(String nhsno, String unitcode) {
 
         if (nhsno == null || nhsno.length() == 0 || unitcode == null || unitcode.length() == 0) {
             throw new IllegalArgumentException("Required parameters nhsno and unitcode to delete patient");
         }
 
         Patient patient = get(nhsno, unitcode);
 
         if (patient != null) {
             delete(patient);
         }
     }
 
     @Override
     public List<Patient> get(String centreCode) {
 
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Patient> criteria = builder.createQuery(Patient.class);
         Root<Patient> from = criteria.from(Patient.class);
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         wherePredicates.add(builder.equal(from.get(Patient_.centreCode), centreCode));
 
         buildWhereClause(criteria, wherePredicates);
         return getEntityManager().createQuery(criteria).getResultList();
     }
 
     @Override
     public List getUnitPatientsWithTreatmentDao(String unitcode, String nhsno, String name, boolean showgps,
                                                 Specialty specialty) {
         String sql = "SELECT "
                 + "user.username,  user.password, user.name, user.email, usermapping.nhsno, usermapping.unitcode, "
                 + "user.firstlogon, patient.treatment "
                 + "FROM user, specialtyuserrole, usermapping "
                 + "LEFT JOIN patient ON usermapping.nhsno = patient.nhsno AND usermapping.unitcode = patient.centreCode "
                 + "WHERE specialtyuserrole.role = 'patient' "
                 + "AND user.username = usermapping.username "
                 + "AND user.id = specialtyuserrole.user_id "
                 + "AND usermapping.unitcode <> '" + UnitUtils.PATIENT_ENTERS_UNITCODE + "' ";
 
         if (!"".equals(unitcode)) {
             sql += "AND usermapping.unitcode = ? ";
         }
 
        sql += "AND usermapping.nhsno LIKE ? "
                + "AND user.name LIKE ? ";
 
         if (!showgps) {
             sql += "AND user.name NOT LIKE '%-GP' ";
         }
 
         sql += "AND specialtyuserrole.specialty_id = ? ORDER BY user.name ASC ";
 
         List<Object> params = new ArrayList<Object>();
 
         if (!"".equals(unitcode)) {
             params.add(unitcode);
         }
 
        params.add(nhsno);
         params.add(specialty.getId());
        params.add(name);
 
         return jdbcTemplate.query(sql, params.toArray(), new PatientLogonWithTreatmentMapper());
     }
 
     @Override
     public List<PatientLogonWithTreatment> getUnitPatientsAllWithTreatmentDao(String unitcode, Specialty specialty) {
         String sql = "SELECT " +
                 "   user.username,  " +
                 "   user.password, " +
                 "   user.name, " +
                 "   user.email, " +
                 "   usermapping.nhsno, " +
                 "   usermapping.unitcode, " +
                 "   user.firstlogon, " +
                 "   patient.treatment " +
                 "FROM " +
                 "   user, " +
                 "   specialtyuserrole, " +
                 "   usermapping " +
                 "LEFT JOIN " +
                 "   patient ON usermapping.nhsno = patient.nhsno " +
                 "WHERE " +
                 "   usermapping.username = user.username " +
                 "AND " +
                 "   user.id = specialtyuserrole.user_id " +
                 "AND " +
                 "   usermapping.unitcode = ? " +
                 "AND " +
                 "   specialtyuserrole.role = 'patient' " +
                 "AND " +
                 "   user.name NOT LIKE '%-GP' " +
                 "AND " +
                 "   specialtyuserrole.specialty_id = ? " +
                 "ORDER BY " +
                 "   user.name ASC";
 
         List<Object> params = new ArrayList<Object>();
 
         params.add(unitcode);
         params.add(specialty.getId());
 
         return jdbcTemplate.query(sql, params.toArray(), new PatientLogonWithTreatmentMapper());
     }
 
     @Override
     public List<Patient> getUktPatients() {
 
         String sql = "SELECT DISTINCT patient.nhsno, patient.surname, patient.forename, " +
                 " patient.dateofbirth, patient.postcode FROM patient, user, usermapping " +
                 " WHERE patient.nhsno REGEXP '^[0-9]{10}$' AND patient.nhsno = usermapping.nhsno " +
                 "AND user.username = usermapping.username " +
                 " AND usermapping.username NOT LIKE '%-GP' AND user.dummypatient = 0";
 
         return jdbcTemplate.query(sql, new PatientMapper());
     }
 
     private class PatientMapper implements RowMapper<Patient> {
 
         @Override
         public Patient mapRow(ResultSet resultSet, int i) throws SQLException {
 
             Patient patient = new Patient();
             patient.setNhsno(resultSet.getString("nhsno"));
             patient.setSurname(resultSet.getString("surname"));
             patient.setForename(resultSet.getString("forename"));
             patient.setDateofbirth(resultSet.getString("dateofbirth"));
             patient.setPostcode(resultSet.getString("postcode"));
 
             return patient;
         }
     }
 
     private class PatientLogonWithTreatmentMapper implements RowMapper<PatientLogonWithTreatment> {
         @Override
         public PatientLogonWithTreatment mapRow(ResultSet resultSet, int i) throws SQLException {
             PatientLogonWithTreatment patientLogonWithTreatment = new PatientLogonWithTreatment();
 
             patientLogonWithTreatment.setUsername(resultSet.getString("username"));
             patientLogonWithTreatment.setPassword(resultSet.getString("password"));
             patientLogonWithTreatment.setName(resultSet.getString("name"));
             patientLogonWithTreatment.setEmail(resultSet.getString("email"));
             patientLogonWithTreatment.setNhsno(resultSet.getString("nhsno"));
             patientLogonWithTreatment.setFirstlogon(resultSet.getBoolean("firstlogon"));
             patientLogonWithTreatment.setUnitcode(resultSet.getString("unitcode"));
             patientLogonWithTreatment.setTreatment(resultSet.getString("treatment"));
 
             return patientLogonWithTreatment;
         }
     }
 }
