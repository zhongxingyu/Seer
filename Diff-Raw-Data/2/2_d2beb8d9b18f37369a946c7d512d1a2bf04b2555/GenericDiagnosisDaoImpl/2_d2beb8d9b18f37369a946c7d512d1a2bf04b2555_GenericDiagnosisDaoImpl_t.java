 package com.solidstategroup.radar.dao.impl;
 
 import com.solidstategroup.radar.dao.generic.GenericDiagnosisDao;
 import com.solidstategroup.radar.model.generic.DiseaseGroup;
 import com.solidstategroup.radar.model.generic.GenericDiagnosis;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.List;
 
 
 public class GenericDiagnosisDaoImpl extends BaseDaoImpl implements GenericDiagnosisDao {
     private static final Logger LOGGER = LoggerFactory.getLogger(GenericDiagnosisDaoImpl.class);
 
     public List<GenericDiagnosis> getAll() {
         return jdbcTemplate.query("SELECT * FROM rdr_prd_code, rdr_diagnosis_mapping" +
                 " WHERE rdr_prd_code.ERA_EDTA_PRD_code = rdr_diagnosis_mapping.PRDCode",
                 new GenericDiagnosisRowMapper());
     }
 
     public List<GenericDiagnosis> getByDiseaseGroup(DiseaseGroup diseaseGroup) {
         List<GenericDiagnosis> genericDiagnosises = jdbcTemplate.query("SELECT * FROM rdr_prd_code, " +
                 "rdr_diagnosis_mapping" +
                 " WHERE rdr_prd_code.ERA_EDTA_PRD_code = rdr_diagnosis_mapping.PRDCode" +
                 " AND rdr_diagnosis_mapping.workingGroup = ?", new Object[]{diseaseGroup.getId()},
                 new GenericDiagnosisRowMapper());
         Collections.sort(genericDiagnosises);
         return genericDiagnosises;
     }
 
     public GenericDiagnosis getById(String id) {
         try {
            return jdbcTemplate.queryForObject("SELECT DISTINCT * FROM rdr_prd_code, rdr_diagnosis_mapping" +
                     " WHERE rdr_prd_code.ERA_EDTA_PRD_code = rdr_diagnosis_mapping.PRDCode" +
                     " AND ERA_EDTA_PRD_code = ?", new Object[]{id}, new GenericDiagnosisRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.error("generic diagnosis with id " + id + "not found" + e);
             return null;
         }
 
     }
 
     private class GenericDiagnosisRowMapper implements RowMapper<GenericDiagnosis> {
         public GenericDiagnosis mapRow(ResultSet resultSet, int i) throws SQLException {
             GenericDiagnosis genericDiagnosis = new GenericDiagnosis();
             genericDiagnosis.setId(resultSet.getString("rdr_prd_code.ERA_EDTA_PRD_code"));
             genericDiagnosis.setTerm(resultSet.getString("rdr_prd_code.ERA_EDTA_primaryRenalDiagnosisTerm"));
             Integer order = getIntegerWithNullCheck("ordering", resultSet);
             genericDiagnosis.setOrder(order != null ? order : 0);
             return genericDiagnosis;
         }
     }
 }
