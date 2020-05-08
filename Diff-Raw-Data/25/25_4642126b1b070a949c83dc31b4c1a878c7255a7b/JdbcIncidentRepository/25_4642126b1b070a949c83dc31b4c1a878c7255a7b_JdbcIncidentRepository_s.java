 package com.github.kolorobot.icm.incident;
 
 import com.github.kolorobot.icm.support.date.DateConverter;
 import com.google.common.collect.Lists;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.inject.Inject;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 @Repository
 public class JdbcIncidentRepository implements IncidentRepository {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(JdbcIncidentRepository.class);
 
     private JdbcTemplate jdbcTemplate;
 
     @Inject
     public JdbcIncidentRepository(JdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 
     @Override
     public List<Incident> findAll() {
         String sql = findAllQuery();
         LOGGER.debug("Running SQL query: " + sql);
         List<Incident> incidents = jdbcTemplate.query(sql, new IncidentMapper());
         return incidents == null ? Lists.<Incident>newArrayList() : incidents;
     }
 
     @Override
     public List<Incident> findAllByStatus(Incident.Status status) {
         String sql = findAllQuery().concat(" where incident.status = :status order by incident.id desc");
         LOGGER.debug("Running SQL query: " + sql);
         List<Incident> incidents = jdbcTemplate.query(sql, new Object[] {status.ordinal()}, new IncidentMapper());
         return incidents == null ? Lists.<Incident>newArrayList() : incidents;
     }
 
     private String findAllQuery() {
         return "select incident.id as incident_id, incident.created, incident.incident_type, incident.description, incident.status, incident.creator_id, incident.assignee_id, address.id as address_id, address.address_line, address.city_line " +
                 "from incident inner join address on incident.address_id = address.id";
     }
 
     public List<Incident> search(String query) {
         String sql = "select incident.id as incident_id, incident.created, incident.incident_type, incident.description, incident.status, incident.creator_id, incident.assignee_id, address.id as address_id, address.address_line, address.city_line " +
                "from incident join address on incident.address_id = address.id left outer join audit on incident_id = audit.incident_id " +
                "where incident.incident_type like ? or incident.description like ? or audit.description like ? " +
                 "order by incident.id desc";
        List<Incident> result = jdbcTemplate.query(sql, new Object[]{query, query, query}, new IncidentMapper());
         return result == null ? Lists.<Incident>newArrayList() : result;
     }
 
     @Override
     public Incident findOne(Long id) {
         String sql = "select incident.id as incident_id, incident.created, incident.incident_type, incident.description, incident.status, incident.creator_id, incident.assignee_id, address.id as address_id, address.address_line, address.city_line " +
                 "from incident inner join address on incident.address_id = address.id where incident.id = ?";
         LOGGER.debug("Running SQL query: " + sql);
         return jdbcTemplate.queryForObject(sql, new Object[]{id}, new IncidentMapper());
     }
 
     @Override
     public Incident findOneByIdAndCreatorId(Long id, Long accountId) {
         String sql = "select incident.id as incident_id, incident.created, incident.incident_type, incident.description, incident.status, incident.creator_id, incident.assignee_id, address.id as address_id, address.address_line, address.city_line " +
                 "from incident inner join address on incident.address_id = address.id where incident.id = ? and incident.creator_id = ?";
         LOGGER.debug("Running SQL query: " + sql);
         return jdbcTemplate.queryForObject(sql, new Object[]{id, accountId}, new IncidentMapper());
     }
 
     @Override
     public Incident findOneByIdAndAssigneeIdOrCreatorId(Long id, Long accountId) {
         String sql = "select incident.id as incident_id, incident.created, incident.incident_type, incident.description, incident.status, incident.creator_id, incident.assignee_id, address.id as address_id, address.address_line, address.city_line " +
                 "from incident inner join address on incident.address_id = address.id where incident.id = ? and (incident.assignee_id = ? or incident.creator_id = ?)";
         LOGGER.debug("Running SQL query: " + sql);
         return jdbcTemplate.queryForObject(sql, new Object[]{id, accountId, accountId}, new IncidentMapper());
     }
 
     @Override
     @Transactional
     public Incident save(Incident incident) {
         int addressId = jdbcTemplate.queryForInt("select max(id) from address") + 1;
 
         jdbcTemplate.update("insert into address (id, address_line, city_line) values (?, ?, ?)",
                 new Object[]{addressId, incident.getAddress().getAddressLine(), incident.getAddress().getCityLine()}
         );
 
         int incidentId = jdbcTemplate.queryForInt("select max(id) from incident") + 1;
         String sql = "insert into incident (id, incident_type, address_id, description, created, status, creator_id) values (?, ?, ?, ?, ?, ?, ?)";
         LOGGER.debug("Running SQL query: " + sql);
         jdbcTemplate.update(sql,
                 new Object[]{incidentId, incident.getIncidentType(), addressId, incident.getDescription(), DateConverter.toDateString(incident.getCreated()), incident.getStatus().ordinal(), incident.getCreatorId()});
 
         incident.getAddress().setId(Long.valueOf(addressId));
         incident.setId(Long.valueOf(incidentId));
 
         return incident;
     }
 
     @Override
     public Incident update(Incident incident) {
         String sql = "update incident set status = ?, assignee_id = ? where incident.id = ?";
         LOGGER.debug("Running SQL query: " + sql);
 
         jdbcTemplate.update(sql,
                 new Object[]{incident.getStatus().ordinal(), incident.getAssigneeId(), incident.getId()});
         return incident;
     }
 
     private static class IncidentMapper implements org.springframework.jdbc.core.RowMapper<Incident> {
         @Override
         public Incident mapRow(ResultSet resultSet, int i) throws SQLException {
             Address address = new Address();
             address.setId(resultSet.getLong("address_id"));
             address.setAddressLine(resultSet.getString("address_line"));
             address.setCityLine(resultSet.getString("city_line"));
 
             Incident incident = new Incident();
             incident.setAddress(address);
             incident.setId(resultSet.getLong("incident_id"));
             incident.setCreated(DateConverter.toDate(resultSet.getString("created")));
             incident.setIncidentType(resultSet.getString("incident_type"));
             incident.setDescription(resultSet.getString("description"));
             incident.setStatus(Incident.Status.valueOf(resultSet.getInt("status")));
             incident.setCreatorId(resultSet.getLong("creator_id"));
             incident.setAssigneeId(resultSet.getLong("assignee_id"));
             return incident;
         }
 
         ;
     }
 }
