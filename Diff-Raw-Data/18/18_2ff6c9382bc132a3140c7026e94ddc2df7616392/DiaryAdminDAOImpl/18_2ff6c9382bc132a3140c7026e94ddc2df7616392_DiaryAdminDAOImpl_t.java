 package com.zotyo.diary.admin.persistence;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
 import org.springframework.jdbc.core.namedparam.SqlParameterSource;
 import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
 import org.springframework.stereotype.Repository;
 
 import com.zotyo.diary.admin.persistence.mappers.DayMapper;
 import com.zotyo.diary.admin.persistence.mappers.EventMapper;
 import com.zotyo.diary.pojos.Day;
 import com.zotyo.diary.pojos.Event;
 
 @Repository
 public class DiaryAdminDAOImpl extends SimpleJdbcDaoSupport implements DiaryAdminDAO {
 	
 	private static Logger logger = Logger.getLogger(DiaryAdminDAOImpl.class);
 	
 	@Autowired
 	private DataSource dataSource; 
 	
 	@Override
 	public List<Day> getAllDays() {
 		String sql = "select * from days order by id desc";
 		
 		List<Day> days = getSimpleJdbcTemplate().query(sql, new DayMapper());
 
 		return days;
 	}
 	
 	@Override
 	public List<Event> getAllEvents() {
 		String sql = "select * from events order by id desc";
 		
 		List<Event> events = getSimpleJdbcTemplate().query(sql, new EventMapper());
 
 		return events;
 	}
 	
 	@Override
 	public Day getDayById(Integer dayId) {
 		String sql = "select * from days where id = ?";
 		
 		Day day = getSimpleJdbcTemplate().queryForObject(sql, new DayMapper(), dayId);
 		
 		return day;
 	}
 
 	@Override
 	public Day getDayByDate(Date day) {
 		try {
 			String sql = "select * from days where the_day = ?";
 			
 			Day result = getSimpleJdbcTemplate().queryForObject(sql, new DayMapper(), day);
 			
 			return result;
 		}
 		catch (DataAccessException e) {
 			logger.warn("No matching days found for " + day);
 			return null;
 		}
 	}
 	
 	@Override
 	public boolean updateDay(Day d) {
 		SqlParameterSource sqlParams = new BeanPropertySqlParameterSource(d);
 		String sql = "update days set text = :descriptionOfTheDay where id = :id";
 		int rows = getSimpleJdbcTemplate().update(sql, sqlParams);
 		if (rows == 1) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public List<Event> getEventsByDay(Integer dayId) {
 		String sql = "select * from events where day_id = ?";
 		
 		List<Event> events = getSimpleJdbcTemplate().query(sql, new EventMapper(), dayId);
 
 		return events;
 
 	}
 
 	@Override
 	public boolean updateEvent(Event e) {
 		SqlParameterSource sqlParams = new BeanPropertySqlParameterSource(e);
 		String sql = "update events set text = :description, duration = :duration, starttime = :startTime where id = :id";
 		int rows = getSimpleJdbcTemplate().update(sql, sqlParams);
 		if (rows == 1) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void deleteEvent(Integer eventId) {
		String sql = "delete from events where id = ?";
		getSimpleJdbcTemplate().update(sql, eventId);
 	}
 
 	@Override
 	public void deleteDay(Integer dayId) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@PostConstruct
 	private void initDataSource() {
 		super.setDataSource(dataSource);
 	}
 
 }
