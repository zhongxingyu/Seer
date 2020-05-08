 package org.patientview.radar.dao.impl;
 
 
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.radar.dao.generic.DiseaseGroupDao;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.List;
 
 public class DiseaseGroupDaoImpl extends BaseDaoImpl implements DiseaseGroupDao {
     private static final Logger LOGGER = LoggerFactory.getLogger(DiseaseGroupDaoImpl.class);
 
     public List<DiseaseGroup> getAll() {
          List<DiseaseGroup> diseaseGroups = jdbcTemplate.query("SELECT * FROM unit WHERE sourceType='radargroup'",
                 new DiseaseGroupRowMapper());
         Collections.sort(diseaseGroups);
         return diseaseGroups;
     }
 
     public DiseaseGroup getById(String id) {
        List<DiseaseGroup> list =
                jdbcTemplate.query("SELECT * FROM unit WHERE sourceType='radargroup' AND unitcode = ?",
                 new Object[]{id}, new DiseaseGroupRowMapper());
        return CollectionUtils.isEmpty(list)?null:list.get(0);
   }
 
     private class DiseaseGroupRowMapper implements RowMapper<DiseaseGroup> {
         public DiseaseGroup mapRow(ResultSet resultSet, int i) throws SQLException {
             DiseaseGroup diseaseGroup = new DiseaseGroup();
             diseaseGroup.setId(resultSet.getString("unitcode"));
             diseaseGroup.setName(resultSet.getString("name"));
             diseaseGroup.setShortName(resultSet.getString("shortName"));
             return diseaseGroup;
         }
     }
 }
