 package data.db.repositories;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import log.ILogger;
 import log.SimpleLoggerFactory;
 
 import data.contracts.IDataBaseService;
 import data.contracts.repositories.IGroupsRepository;
 import data.contracts.repositories.RepositoryException;
 import data.db.IDBConnectionFactory;
 import data.db.Repository;
 import data.orm.Group;
 import data.orm.Mark;
 import data.orm.ORMObjectException;
 import data.orm.Student;
 
 public class GroupsRepository extends Repository implements IGroupsRepository {
 
 
 	public GroupsRepository(IDataBaseService dataBaseService,
 			IDBConnectionFactory connectionFactory) {
 		super(dataBaseService, connectionFactory);
 	}
 
 	@Override
 	public Collection<Group> getAll() throws RepositoryException {
 		Connection c = super.getConnection();
 		Collection<Group> groups = null;
 
 		try {
 			String query = "select * from groups;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ResultSet key = ps.executeQuery();
 			groups = new ArrayList<Group>();
 			while (key.next()) {
 
 				Group group = new Group();
 				int id = key.getInt("id");
 				String name = key.getString("name");
 				group.setID(id);
 				group.setName(name);
 				group.setDb(dataBaseService);
 				groups.add(group);
 			}
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 		return groups;
 	}
 
 	@Override
 	public Group getByID(int ID) throws RepositoryException {
 		Connection c = super.getConnection();
 		Group group = null;
 
 		try {
 			String query = "select * from groups where id = ? LIMIT 1;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setInt(1, ID);
 			ResultSet key = ps.executeQuery();
 			if (key.next()) {
 				group = new Group();
 				int id = key.getInt("id");
 				String name = key.getString("name");
 				group.setID(id);
 				group.setName(name);
 				group.setDb(dataBaseService);
 
 			}
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 		return group;
 	}
 
 	@Override
 	public void insert(Group obj) throws RepositoryException {
 		Connection c = super.getConnection();
 		try {
 			String query = "insert into groups (name) values (?) returning id;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setString(1, obj.getName());
 			ResultSet key = ps.executeQuery();
 			if (key.next()) {
 				int id = key.getInt("id");
 				obj.setID(id);
 				obj.setDb(dataBaseService);
 
 			}
 			super.commit(c);
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 	}
 
 	@Override
 	public void remove(int ID) throws RepositoryException {
 		Connection c = super.getConnection();
 		try {
 			String query = "delete from groups where id = ?;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setInt(1, ID);
 			ps.execute();
 			super.commit(c);
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 
 	}
 
 	@Override
 	public void update(Group obj) throws RepositoryException {
 		Connection c = super.getConnection();
 		try {
 			String query = "update groups set name = ? where id = ?;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setString(1, obj.getName());
 			ps.setInt(2, obj.getID());
 			ps.execute();
 			super.commit(c);
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 
 	}
 
 	@Override
 	public Group getByName(String name) throws RepositoryException {
 		Connection c = super.getConnection();
 		Group group = null;
 
 		try {
 			String query = "select * from groups where name = ?;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setString(1, name);
 			ResultSet key = ps.executeQuery();
 			if (key.next()) {
 				group = new Group();
 				int id = key.getInt("id");
 				group.setID(id);
				group.setName(name);
 				group.setDb(dataBaseService);
 			}
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 		return group;
 	}
 
 	@Override
 	public double getAverageMark(int groupID) throws
 			RepositoryException {
 		Connection c = super.getConnection();
 		double avg = 0.0;
 		try {
 			String query = "select avg(mark) as avg_mark from marks "
 					+ "join group_students on group_students.stud_id = marks.student_id "
 					+ "where group_id = ?";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setInt(1, groupID);
 			ResultSet key = ps.executeQuery();
 			if (key.next()) {
 				avg = key.getDouble("avg_mark");
 			}
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 		return avg;
 	}
 
 	@Override
 	public Collection<Student> getAllByGroup(Group newGroup) throws RepositoryException {
 		Connection c = super.getConnection();
 		Collection <Student> students = null;
 
 		try {
 			String query = "select students.name, students.surname, groups.name as group_name from students, groups"
 					+ "join group_students on group_students.group_id = groups.id "
 					+  "where groups.id = ?;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setInt(1, newGroup.getID());
 			ResultSet key = ps.executeQuery();
 			students = new ArrayList<Student>();
 			while (key.next()) {
 				Student student = new Student();
 				Group group = new Group();
 				int id = key.getInt("id");
 				String group_name = key.getString("group_name");
 				String name = key.getString("name");
 				String surname = key.getString("surname");
 				student.setID(id);
 				student.setName(name);
 				student.setSurname(surname);
 				group.setName(group_name);
 				group.setDb(dataBaseService);
 				students.add(student);
 			}
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 		return students;
 		
 	}
 
 	@Override
 	public void removeStudent(Group group, Student student) throws RepositoryException {
 		Connection c = super.getConnection();
 		try {
 			String query = "delete from group_students"
 					+"join groups on group_students.group_id = groups.id"
 					+ "join students on group_students.stud_id = students.id"
 					+ "where group_students.group_id = ? and group_students.stud_id = ?; ";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setInt(1, group.getID());
 			ps.setInt(2, student.getID());
 			ps.execute();
 			super.commit(c);
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 
 	}
 
 	@Override
 	public void addStudent(Group group, Student student)
 			throws RepositoryException {
 		Connection c = super.getConnection();
 		try {
 			String query = "insert into group_students (group_id, stud_id) values (?,?) returning id;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setInt(1, group.getID());
 			ps.setInt(2, student.getID());
 			ps.execute();
 			super.commit(c);
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 
 	}
 
 }
