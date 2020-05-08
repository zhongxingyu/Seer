 package net.pdp7.tvguide.dao;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import net.pdp7.commons.util.MapUtils;
 
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 
 public class EpgDao {
 
 	protected final SimpleJdbcTemplate jdbcTemplate;
 
 	public EpgDao(SimpleJdbcTemplate jdbcTemplate) {
 		this.jdbcTemplate = jdbcTemplate;
 	}
 	
 	public void upsertChannel(String channelName) {
 		jdbcTemplate.update("insert into channels (channel) (select ? as channel where not exists (select 1 from channels where channel = ?))", channelName, channelName);
 	}
 
 	public void insertProgram(String channel, String program, String subProgram, Date beginsAt, Date endsAt) {
 		// FIXME Transaction
 		jdbcTemplate.update(
 				"delete from programs " +
 				"where  channel = :channel " +
 				"and    begins_at < :ends_at " +
 				"and    ends_at > :begins_at ", 
 				MapUtils.<String,Object>build("channel", channel)
 					.put("begins_at", beginsAt)
 					.put("ends_at", endsAt)
 					.map);
 		jdbcTemplate.update("insert into programs(channel, program, sub_program, begins_at, ends_at) values (?,?,?,?,?)", channel, program, subProgram, beginsAt, endsAt);
 	}
 	
 	public List<Map<String, Object>> getPrograms(String[] channels, Date begin, Date end, Long userId) {
 		return jdbcTemplate.queryForList(
 				"select   programs.*, " +
 				"         begins_at < :begin as started " +
 				"from     programs " +
 				"where    channel in (:channels) " +
 				"and      ends_at > :begin " +
 				"and      begins_at < :end " +
 				(userId != null ? "and not exists (select 1 from user_ignored_programs where programs.program = user_ignored_programs.program and user_id = :user_id) " : "") +
				"order by begins_at;",
 				MapUtils
 					.<String,Object>build("channels", Arrays.asList(channels))
 					.put("begin", begin)
 					.put("end", end)
 					.put("user_id", userId)
 					.map);
 	}
 
 }
