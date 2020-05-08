 package dk.nsi.sdm4.core.persistence;
 
 import dk.nsi.sdm4.core.parser.Parser;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.PostConstruct;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 @Repository
 public class ImportStatusRepositoryJdbcImpl implements ImportStatusRepository {
 	@Autowired
 	private JdbcTemplate jdbcTemplate;
 
 	@Autowired
 	private Parser parser;
 	private String statusTableName;
 
 	@PostConstruct
 	private void constructTableNameFromParser() {
 		statusTableName = parser.getHome() + "ImportStatus";
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRES_NEW)
 	public void importStartedAt(DateTime startTime) {
 		jdbcTemplate.update("INSERT INTO " + statusTableName + " (StartTime) values (?)", startTime.toDate());
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.MANDATORY)
 	public void importEndedAt(DateTime endTime, ImportStatus.Outcome outcome) {
		Long newestOpenId = jdbcTemplate.queryForLong("SELECT Id from " + statusTableName + " ORDER BY StartTime DESC LIMIT 1");
 		jdbcTemplate.update("UPDATE " + statusTableName + " SET EndTime=?, Outcome=? WHERE Id=?", endTime.toDate(), outcome.toString(), newestOpenId);
 	}
 
 	@Override
 	public ImportStatus getLatestStatus() {
 		try {
 			return jdbcTemplate.queryForObject("SELECT * from " + statusTableName + " ORDER BY StartTime DESC LIMIT 1", new ImportStatusRowMapper());
 		} catch (EmptyResultDataAccessException ignored) {
 			// that's not a problem, we just don't have any statuses
 			return null;
 		}
 	}
 
 	@Override
 	public boolean isOverdue() {
 		return false;
 	}
 
 
 	private class ImportStatusRowMapper implements RowMapper<ImportStatus> {
 
 		@Override
 		public ImportStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
 			ImportStatus status = new ImportStatus();
 			status.setStartTime(new DateTime(rs.getTimestamp("StartTime")));
 			status.setEndTime(new DateTime(rs.getTimestamp("EndTime")));
 			String dbOutcome = rs.getString("Outcome");
 			if (dbOutcome != null) {
 				status.setOutcome(ImportStatus.Outcome.valueOf(dbOutcome));
 			}
 
 			return status;
 		}
 	}
 }
