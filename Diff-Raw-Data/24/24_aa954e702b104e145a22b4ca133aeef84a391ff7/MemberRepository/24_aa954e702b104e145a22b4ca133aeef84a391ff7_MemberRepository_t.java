 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.repository;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import lombok.Cleanup;
 import lombok.val;
 import tk.c4se.halt.ih31.nimunimu.config.DBConnector;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.model.Member;
 import tk.c4se.halt.ih31.nimunimu.model.MemberAuthority;
 
 /**
  * @author ne_Sachirou
  */
 public class MemberRepository implements java.io.Serializable {
 	private static final long serialVersionUID = 1L;
 
 	protected final int perPage = 20;
 
 	public MemberRepository() {
 	}
 
 	/**
 	 * 
 	 * @param id
 	 * @return
 	 * @throws DBAccessException
 	 */
 	public Member find(String id) throws DBAccessException {
 		if (id == null || id.isEmpty()) {
 			return null;
 		}
 		val sql = "select * from account where id = ?";
 		Member member = null;
 		try (val connection = DBConnector.getConnection()) {
 			@Cleanup
 			PreparedStatement statement = connection.prepareStatement(sql);
 			statement.setString(1, id);
 			@Cleanup
 			val result = statement.executeQuery();
 			if (result.next()) {
 				member = new Member();
 				setProperties(member, result);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new DBAccessException();
 		}
 		return member;
 	}
 
 	/**
 	 * 
 	 * @param page
 	 * @return
 	 * @throws DBAccessException
 	 */
 	public List<Member> all(int page) throws DBAccessException {
 		val sql = "select * from account where rownum between ? and ?";
 		List<Member> members = new ArrayList<>();
 		try (val connection = DBConnector.getConnection()) {
			@Cleanup
 			PreparedStatement statement = connection.prepareStatement(sql);
 			statement.setInt(1, perPage * (page - 1) + 1);
 			statement.setInt(2, perPage * page);
 			@Cleanup
 			val result = statement.executeQuery();
 			while (result.next()) {
 				Member member = new Member();
 				setProperties(member, result);
 				members.add(member);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new DBAccessException();
 		}
 		return members;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws DBAccessException
 	 */
 	public List<Member> all() throws DBAccessException {
 		return all(1);
 	}
 
 	/**
 	 * 
 	 * @param member
 	 * @throws DBAccessException
 	 */
 	public void insert(Member member) throws DBAccessException {
 		val sql = "insert into account(id, name, password, is_password_resetted, authority) values (?, ?, ?, ?, ?)";
 		Connection connection = null;
 		try {
 			connection = DBConnector.getConnection();
 			PreparedStatement statement = connection.prepareStatement(sql);
 			statement.setString(1, member.getId());
 			statement.setString(2, member.getName());
 			statement.setString(3, member.getPassword());
 			statement.setInt(4, member.getIsPasswordReseted() ? 1 : 0);
 			statement.setString(5, member.getAuthority().toString());
 			statement.executeUpdate();
 		} catch (SQLException e) {
 			if (connection != null) {
 				try {
 					connection.rollback();
 				} catch (SQLException e1) {
 					throw new DBAccessException();
 				}
 			}
 			throw new DBAccessException();
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					throw new DBAccessException();
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param member
 	 * @throws DBAccessException
 	 */
 	public void update(Member member) throws DBAccessException {
 		val sql = "update account set name = ?, password = ?, is_password_resetted = ?, authority = ? where id = ?";
 		Connection connection = null;
 		try {
 			connection = DBConnector.getConnection();
 			PreparedStatement statement = connection.prepareStatement(sql);
 			statement.setString(1, member.getName());
 			statement.setString(2, member.getPassword());
 			statement.setInt(3, member.getIsPasswordReseted() ? 1 : 0);
 			statement.setString(4, member.getAuthority().toString());
 			statement.setString(5, member.getId());
 			statement.executeUpdate();
 		} catch (SQLException e) {
 			if (connection != null) {
 				try {
 					connection.rollback();
 				} catch (SQLException e1) {
 					throw new DBAccessException();
 				}
 			}
 			throw new DBAccessException();
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					throw new DBAccessException();
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param member
 	 * @throws DBAccessException
 	 */
 	public void delete(Member member) throws DBAccessException {
 		val sql = "delete from account where id = ?";
 		Connection connection = null;
 		try {
 			connection = DBConnector.getConnection();
 			PreparedStatement statement = connection.prepareStatement(sql);
 			statement.setString(1, member.getId());
 			statement.executeUpdate();
 		} catch (SQLException e) {
 			if (connection != null) {
 				try {
 					connection.rollback();
 				} catch (SQLException e1) {
 					throw new DBAccessException();
 				}
 			}
 			throw new DBAccessException();
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					throw new DBAccessException();
 				}
 			}
 		}
 	}
 
 	private void setProperties(Member member, ResultSet result)
 			throws SQLException {
 		member.setId(result.getString("id"));
 		member.setName(result.getString("name"));
 		member.setPassword(result.getString("password"));
 		member.setIsPasswordReseted(result.getInt("is_password_resetted") != 0);
 		member.setAuthority(MemberAuthority.valueOf(result
 				.getString("authority")));
 	}
 }
