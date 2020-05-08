 package com.drexelexp.review;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import com.drexelexp.baseDAO.JdbcBaseDAO;
 import com.drexelexp.course.Course;
 import com.drexelexp.professor.Professor;
 
 /**
  * Implementation of ProfessorDAO in JDBC style.
  * 
  * @author
  * 
  */
 public class JdbcReviewDAO extends JdbcBaseDAO<Review> {
 	protected String getTableName() {
 		return "reviews";
 	}
 
 	protected String getIdColumnName() {
 		return "REVIEW_ID";
 	}
 
 	protected int getId(Review instance) {
 		return instance.getId();
 	}
 
 	protected Review parseResultSetRow(ResultSet rs) throws SQLException {
 		return new Review(
 				rs.getInt("REVIEW_ID"),
 				rs.getString("TEXT"),
 				rs.getFloat("RATING"),
 				rs.getTimestamp("TIMESTAMP"),
 				rs.getInt("USER_ID"),
 				rs.getInt("PROF_ID"),
 				rs.getInt("COURSE_ID"));
 	}
 
 	protected Map<String, Object> getColumnMap(Review instance) {
 		Map<String,Object> map = new Hashtable<String,Object>();
 		
 		map.put("REVIEW_ID",instance.getId());
		map.put("TEXT",instance.getContent());
 		map.put("RATING",instance.getRating());
 		map.put("USER_ID",instance.getUser().getId());
 		map.put("PROF_ID",instance.getProfessor().getId());
 		map.put("COURSE_ID",instance.getCourse().getId());
 		
 		return map;
 	}
 	
 	public List<Review> getReviews(Course course){
 		Map<String,Object> conditions = new Hashtable<String,Object>();
 		
 		conditions.put("COURSE_ID",course.getId());
 		
 		return getWhere(conditions);
 	}
 	
 	public List<Review> getReviews(Professor professor){
 		Map<String,Object> conditions = new Hashtable<String,Object>();
 		
 		conditions.put("PROF_ID",professor.getId());
 		
 		return getWhere(conditions);
 	}
 	
 	private float getRating(String column, int id){
 		String sql = "SELECT AVG(RATING) AS RATING FROM "+getTableName()+" WHERE "+column+" = ?";
 		Connection conn = null;
 
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 
 			ps.setInt(1, id);
 			
 			ResultSet rs = ps.executeQuery();
 
 			rs.next();
 			
 			float rating = rs.getFloat("RATING");
 
 			rs.close();
 			ps.close();
 
 			return rating;
 
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {
 				}
 			}
 		}
 	}
 	
 	public float getRating(Professor professor){
 		return getRating("PROF_ID",professor.getId());
 	}
 	
 	public float getRating(Course course){
 		return getRating("COURSE_ID",course.getId());
 	}
 }
