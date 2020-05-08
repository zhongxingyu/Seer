 package data.db.repositories;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import data.contracts.repositories.IStudentsRepository;
 import data.contracts.repositories.RepositoryException;
 import data.db.DataBaseService;
 import data.db.IDBConnectionFactory;
 import data.db.Repository;
 import data.orm.Group;
 import data.orm.Mark;
 import data.orm.ORMObjectException;
 import data.orm.Student;
 
 public class StudentsRepository extends Repository implements IStudentsRepository
 {
 
 	public StudentsRepository(DataBaseService dataBaseService,
 			IDBConnectionFactory connectionFactory) {
 		super(dataBaseService, connectionFactory);
 	}
 
 	@Override
 	public Collection<Student> getAll() throws RepositoryException {
 		Connection c = super.getConnection();
 		Collection <Student> students = null;
 		try {
 			String query = "select * from students;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ResultSet key = ps.executeQuery();
 			students = new ArrayList<Student>();
 			while (key.next()) {
 
 				Student student = new Student();
 				int id = key.getInt("id");
 				String name = key.getString("name");
 				String surname = key.getString("surname");
 				student.setID(id);
 				student.setName(name);
 				student.setSurname(surname);
 				student.setDb(dataBaseService);
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
 	public Student getByID(int ID) throws RepositoryException {
 		Connection c = super.getConnection();
 		Student student = null;
 
 		try {
 			String query = "select * from students where id = ? LIMIT 1;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setInt(1, ID);
 			ResultSet key = ps.executeQuery();
 			if (key.next()) {
 				student = new Student();
 				int id = key.getInt("id");
 				String name = key.getString("name");
 				String surname = key.getString("surname");
 				student.setID(id);
 				student.setName(name);
 				student.setSurname(surname);
 				student.setDb(dataBaseService);
 			}
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 		return student;
 	}
 
 	@Override
 	public void insert(Student obj) throws RepositoryException {
 		Connection c = super.getConnection();
 		try {
 			String query = "insert into students (name, surname) values (?, ?) returning id;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setString(1, obj.getName());
 			ps.setString(2, obj.getSurname());
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
 			String query = "delete from students where id = ?;";
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
 	public void update(Student obj) throws RepositoryException {
 		Connection c = super.getConnection();
 		try {
 			String query = "update students set name = ?, surname = ? where id = ?;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setString(1, obj.getName());
 			ps.setString(2, obj.getSurname());
 			ps.setInt(3, obj.getID());
 			ps.execute();
 			super.commit(c);
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}	
 	}
 
 	@Override
 	public Collection<Mark> GetMarks(Student student) throws RepositoryException {
 		Connection c = super.getConnection();
 		Collection <Mark> marks = null;
 		try {
 			String query = "select * from marks where student_id = ?;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ResultSet key = ps.executeQuery();
 			ps.setInt(1, student.getID());
 			marks = new ArrayList<Mark>();
 			while (key.next()) {
 				Mark mark = new Mark();
 				int id = key.getInt("id");
 				student.setID(id);
 				mark.setDb(dataBaseService);
 				marks.add(mark);
 			}
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 		return marks;
 	}
 
 	@Override
 	public void AddMark(Mark mark) throws RepositoryException {
 		Connection c = super.getConnection();
 		try {
 			String query = "insert into marks (mark, student_id, subject_id) values (?,?,?) returning id;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setInt(1, mark.getMark());
 			ps.setInt(2, mark.getStudent_id());
 			ps.setInt(3, mark.getSubject_id());
 			ResultSet key = ps.executeQuery();
 			if (key.next()) {
 				int id = key.getInt("id");
 				mark.setID(id);
 				mark.setDb(dataBaseService);
 			}
 			super.commit(c);
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 		
 	}
 
 	@Override
 	public void RemoveMark(int ID) throws RepositoryException {
 		Connection c = super.getConnection();
 		try {
 			String query = "delete from marks where id = ?;";
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
 	public double getAverageMark(Student student) throws ORMObjectException, RepositoryException {
 		Connection c = super.getConnection();
 		double avg = 0.0;
 		try {
 			String query = "select avg(mark) as avg_mark from marks "
 					+ "where student_id = ?";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setInt(1, student.getID());
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
 	public Student getByName(String name, String surname)
 			throws RepositoryException {
 		Connection c = super.getConnection();
 		Student student = null;
 
 		try {
 			String query = "select * from students where name = ? AND surname = ?;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setString(1, name);
 			ps.setString(2, surname);
 			ResultSet key = ps.executeQuery();
 			if (key.next()) {
 				student = new Student();
 				int id = key.getInt("id");
 				student.setID(id);
 				student.setName(name);
				student.setSurname(surname);
 				student.setDb(dataBaseService);
 			}
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 		return student;
 	}
 
 	@Override
 	public Collection<Mark> GetAllMarks() throws RepositoryException {
 		Connection c = super.getConnection();
 		Collection<Mark> marks = null;
 
 		try {
 			String query = "select * from marks;";
 			PreparedStatement ps = c.prepareStatement(query);
 			ResultSet key = ps.executeQuery();
 			marks = new ArrayList<Mark>();
 			while (key.next()) {
 
 				Mark mark = new Mark();
 				int id = key.getInt("id");
 				mark.setID(id);
 				mark.setMark(mark.getID());
 				mark.setStudent_id(mark.getStudent_id());
 				mark.setSubject_id(mark.getSubject_id());
 				mark.setDb(dataBaseService);
 				marks.add(mark);
 			}
 		} catch (SQLException e) {
 			super.throwable(e, RepositoryException.err_enum.c_sql_err);
 		} finally {
 			super.closeConnection(c);
 		}
 		return marks;
 	}
 
 }
