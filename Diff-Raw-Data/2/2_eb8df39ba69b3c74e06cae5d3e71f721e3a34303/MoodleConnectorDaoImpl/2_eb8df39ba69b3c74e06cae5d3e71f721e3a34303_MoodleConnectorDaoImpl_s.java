 package de.thm.arsnova.connector.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 
 import de.thm.arsnova.connector.model.Course;
 import de.thm.arsnova.connector.model.Membership;
 import de.thm.arsnova.connector.model.UserRole;
 
 public class MoodleConnectorDaoImpl implements ConnectorDao {
 
 	private static final String TYPE = "moodle";
 	private static final int MOODLE_COURSE_MANAGER = 1;
 	private static final int MOODLE_COURSE_GUEST = 6;
 
 	@Autowired
 	private DataSource dataSource;
 
 	public List<String> getCourseUsers(final String courseid) {
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		return jdbcTemplate.query(
 				"SELECT username FROM mdl_user "
 				+ "JOIN mdl_user_enrolments ON (mdl_user.id = mdl_user_enrolments.userid) "
 				+ "JOIN mdl_enrol ON (mdl_user_enrolments.enrolid = mdl_enrol.id) "
 				+ "WHERE mdl_enrol.courseid = ?;",
 				new String[] {courseid},
 				new RowMapper<String>() {
 					public String mapRow(ResultSet resultSet, int row) throws SQLException {
 						return resultSet.getString("username");
 					}
 				}
 		);
 	}
 
 	public Membership getMembership(final String username, final String courseid) {
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		List<Membership> results = jdbcTemplate.query(
 				"SELECT mdl_user.username, mdl_role_assignments.roleid FROM mdl_enrol "
 				+ "JOIN mdl_user_enrolments ON (mdl_enrol.id = mdl_user_enrolments.enrolid) "
 				+ "JOIN mdl_user ON (mdl_user_enrolments.userid = mdl_user.id) "
 				+ "JOIN mdl_role_assignments ON (mdl_role_assignments.userid = mdl_user_enrolments.userid) "
 				+ "JOIN mdl_context ON (mdl_context.instanceid = mdl_enrol.courseid AND mdl_context.id = mdl_role_assignments.contextid) "
 				+ "WHERE mdl_context.contextlevel = 50 AND mdl_enrol.courseid = ? AND mdl_user.username = ? "
 				+ "ORDER BY roleid DESC;",
 				new String[] {courseid, username},
 				new RowMapper<Membership>() {
 					public Membership mapRow(ResultSet resultSet, int row) throws SQLException {
 						Membership membership = new Membership();
 						if (resultSet.wasNull()) {
 							membership.setMember(false);
 							return membership;
 						}
 						membership.setMember(true);
 						membership.setUserrole(getMembershipRole(resultSet.getInt("roleid")));
 						return membership;
 					}
 				}
 		);
 		if (results.size() != 1) {
 			return new Membership();
 		}
 		return results.get(0);
 	}
 
 	public List<Course> getMembersCourses(final String username) {
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		return jdbcTemplate.query(
 				"SELECT mdl_course.id, mdl_course.fullname, mdl_course.shortname FROM mdl_course "
 				+ "JOIN mdl_enrol ON (mdl_enrol.courseid = mdl_course.id) "
 				+ "JOIN mdl_user_enrolments ON (mdl_enrol.id = mdl_user_enrolments.enrolid) "
 				+ "JOIN mdl_user ON (mdl_user_enrolments.userid = mdl_user.id) "
				+ "WHERE mdl_user.username = ? AND mdl_enrol.roleid >= 2 AND mdl_enrol.roleid <= 6;",
 				new String[] {username},
 				new RowMapper<Course>() {
 					public Course mapRow(ResultSet resultSet, int row) throws SQLException {
 						Course course = new Course();
 						course.setId(resultSet.getString("id"));
 						course.setFullname(resultSet.getString("fullname"));
 						course.setShortname(resultSet.getString("shortname"));
 						course.setType(TYPE);
 						course.setMembership(getMembership(username, resultSet.getString("id")));
 						return course;
 					}
 				}
 		);
 	}
 
 	private UserRole getMembershipRole(final int moodleRoleId) {
 		if (moodleRoleId == MOODLE_COURSE_MANAGER) {
 			return UserRole.MANAGER;
 		} else if (moodleRoleId == MOODLE_COURSE_GUEST) {
 			return UserRole.OTHER;
 		}
 
 		// User is course member, may be with right to manage the course
 		return UserRole.MEMBER;
 	}
 }
