 package models;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import database.DBFactory;
 import database.InvalidModelException;
 import database.Model;
 
 public class Teacher extends Model
 {
 
 	private Department department;
 	private String department_id;
 	private String first_name;
 	private String id;
 	private String last_name;
 	private String rmpId;
 	private School school;
 	private String school_id;
 	private ArrayList<School> schools;
 
 	public Teacher(ResultSet resultSet) throws SQLException
 	{
 		this(resultSet.getString("id"), resultSet.getString("first_name"),
 				resultSet.getString("last_name"),
 				resultSet.getString("rmp_id"), resultSet
 						.getString("department_id"), resultSet
 						.getString("school_id"), false, false);
 	}
 
 	public Teacher(String first_name, String last_name, String rmp_id,
 			String department_id, String school_id)
 	{
 		this(null, first_name, last_name, rmp_id, department_id, school_id,
 				true, true);
 	}
 
 	public Teacher(String id, String first_name, String last_name,
 			String rmp_id, String department_id, String school_id,
 			Boolean dirty, Boolean fresh)
 	{
 		super("teachers", new ArrayList<String>()
 		{
 
 			{
 				this.add("id");
 				this.add("department_id");
 				this.add("first_name");
 				this.add("last_name");
 				this.add("rmp_id");
 				this.add("school_id");
 			}
 		}, new ArrayList<String>()
 		{
 
 			{
 			}
 		}, dirty, fresh);
 		this.setId(id);
 		this.setFirst_name(first_name);
 		this.setLast_name(last_name);
 		this.setRmpId(rmp_id);
 		this.setDepartment(this.department);
 		this.setSchool(this.school);
 	}
 
 	public static ArrayList<Teacher> findAll()
 	{
 		System.out.println("find all teachers");
 		ArrayList<Teacher> teachers = new ArrayList<>();
 
 		Connection connection = DBFactory.getConnection();
 		Statement statement = null;
 		ResultSet resultSet = null;
 
 		try
 		{
 
 			String query = "SELECT id, department_id, first_name, last_name, rmp_id, school_id FROM teachers";
 			statement = connection.createStatement();
 			resultSet = statement.executeQuery(query);
 
 			while (resultSet.next())
 			{
 				Teacher teacher = new Teacher(resultSet);
 				teachers.add(teacher);
 			}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 		finally
 		{
 			try
 			{
 				resultSet.close();
 				statement.close();
 				DBFactory.closeConnection(connection);
 			}
 			catch (Exception exception)
 			{
 				exception.printStackTrace();
 			}
 		}
 		return teachers;
 	}
 
 	public static Teacher findById(String id)
 	{
 		Teacher teacher = null;
 
 		Connection connection = DBFactory.getConnection();
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 
 		try
 		{
 
 			String query = "SELECT id, department_id, first_name, last_name, rmp_id, school_id FROM teachers WHERE id = ? LIMIT 1";
 			statement = connection.prepareStatement(query);
 			statement.setString(1, id);
 			resultSet = statement.executeQuery();
 
 			while (resultSet.next())
 			{
 				teacher = new Teacher(resultSet);
 			}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 		finally
 		{
 			try
 			{
 				resultSet.close();
 				statement.close();
 				DBFactory.closeConnection(connection);
 			}
 			catch (Exception exception)
 			{
 				exception.printStackTrace();
 			}
 		}
 		return teacher;
 	}
 
 	public static ArrayList<Teacher> findBySchool(String school_id)
 	{
 		System.out.println("find all teachers");
 		ArrayList<Teacher> teachers = new ArrayList<>();
 
 		Connection connection = DBFactory.getConnection();
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 
 		try
 		{
 
 			String query = "SELECT id, department_id, first_name, last_name, rmp_id, school_id FROM teachers WHERE school_id=?";
 			statement = connection.prepareStatement(query);
 			statement.setString(1, school_id);
			resultSet = statement.executeQuery();
 
 			while (resultSet.next())
 			{
 				Teacher teacher = new Teacher(resultSet);
 				teachers.add(teacher);
 			}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 		finally
 		{
 			try
 			{
 				resultSet.close();
 				statement.close();
 				DBFactory.closeConnection(connection);
 			}
 			catch (Exception exception)
 			{
 				exception.printStackTrace();
 			}
 		}
 		return teachers;
 	}
 
 	public static Teacher findOrCreate(String first_name, String last_name,
 			String rmp_id, Department department, School school)
 			throws InvalidModelException
 	{
 		String findQuery = "SELECT id, first_name, last_name, rmp_id, department_id, school_id FROM teachers WHERE first_name like ? AND last_name like ? AND rmp_id like ? AND department_id = ? AND school_id = ? LIMIT 1";
 		Connection connection = DBFactory.getConnection();
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		try
 		{
 			statement = connection.prepareStatement(findQuery);
 			statement.setString(1, first_name);
 			statement.setString(2, last_name);
 			statement.setString(3, rmp_id);
 			statement.setString(4, department.getId());
 			statement.setString(5, school.getId());
 
 			resultSet = statement.executeQuery();
 			if (resultSet.next())
 			{
 				return new Teacher(resultSet);
 			}
 			else
 			{
 				Teacher teacher = new Teacher(first_name, last_name, rmp_id,
 						department.getId(), school.getId());
 				teacher.save();
 				return teacher;
 			}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 		finally
 		{
 			try
 			{
 				statement.close();
 				resultSet.close();
 				DBFactory.closeConnection(connection);
 			}
 			catch (SQLException e)
 			{
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 		return null;
 	}
 
 	public Department getDepartment()
 	{
 		// TODO perform a lazy load of department if department_id exists and
 		// department is null
 		return this.department;
 	}
 
 	public String getDepartment_id()
 	{
 		return this.department_id;
 	}
 
 	public String getFirst_name()
 	{
 		return this.first_name;
 	}
 
 	public String getId()
 	{
 		return this.id;
 	}
 
 	public String getLast_name()
 	{
 		return this.last_name;
 	}
 
 	public String getRmpId()
 	{
 		return this.rmpId;
 	}
 
 	public School getSchool()
 	{
 		return this.school;
 	}
 
 	public String getSchool_id()
 	{
 		return this.school_id;
 	}
 
 	public ArrayList<School> getSchools()
 	{
 		return this.schools;
 	}
 
 	@Override
 	public String isValid()
 	{
 		String valid = null;
 		if (!this.isFresh())
 		{
 			valid = (this.getId() != null) ? null
 					: "An existing teacher needs an ID";
 		}
 		if (valid == null)
 		{
 			valid = (this.getDepartment_id() != null) ? null
 					: "A teacher needs a department";
 		}
 		if (valid == null)
 		{
 			valid = (this.getFirst_name() != null) ? null
 					: "A teacher needs a first name";
 		}
 		if (valid == null)
 		{
 			valid = (this.getLast_name() != null) ? null
 					: "A teacher needs a last name";
 		}
 		if (valid == null)
 		{
 			valid = (this.getRmpId() != null) ? null
 					: "A teacher needs a rmp id";
 		}
 		return valid;
 	}
 
 	@Override
 	public boolean save() throws InvalidModelException
 	{
 		int result = 0;
 
 		String error = this.isValid();
 		if (error == null)
 		{
 			Connection connection = DBFactory.getConnection();
 			PreparedStatement insertNewTeacher = null;
 			ResultSet resultSet = null;
 			PreparedStatement selectId = null;
 			if (this.isFresh())
 			{
 				String insertQuery = "INSERT INTO teachers (department_id, first_name, last_name, rmp_id, school_id) VALUES (?, ?, ?, ?, ?)";
 				try
 				{
 					insertNewTeacher = connection.prepareStatement(insertQuery);
 					insertNewTeacher.setString(1, this.getDepartment_id());
 					insertNewTeacher.setString(2, this.getFirst_name());
 					insertNewTeacher.setString(3, this.getLast_name());
 					insertNewTeacher.setString(4, this.getRmpId());
 					insertNewTeacher.setString(5, this.getSchool_id());
 					result = insertNewTeacher.executeUpdate();
 					if (result > 0)
 					{
 						String idQuery = "SELECT id FROM teachers WHERE department_id = ? AND first_name like ? AND last_name like ? AND rmp_id like ? AND school_id like ? LIMIT 1";
 						selectId = connection.prepareStatement(idQuery);
 						selectId.setString(1, this.getDepartment_id());
 						selectId.setString(2, this.getFirst_name());
 						selectId.setString(3, this.getLast_name());
 						selectId.setString(4, this.getRmpId());
 						selectId.setString(5, this.getSchool_id());
 						resultSet = selectId.executeQuery();
 
 						if (resultSet.next())
 						{
 							this.id = resultSet.getString(1);
 							this.setId(this.id);
 							this.fresh = false;
 							this.dirty = false;
 							return true;
 						}
 						else
 						{
 							System.err.println("Error getting teachers ID");
 							return false;
 						}
 					}
 				}
 				catch (SQLException e)
 				{
 					System.err.println(e.getSQLState() + ", "
 							+ e.getErrorCode());
 					e.printStackTrace();
 					System.exit(1);
 				}
 				finally
 				{
 					try
 					{
 						insertNewTeacher.close();
 						selectId.close();
 						resultSet.close();
 						DBFactory.closeConnection(connection);
 					}
 					catch (SQLException e)
 					{
 						e.printStackTrace();
 						System.exit(1);
 					}
 				}
 
 			}
 			else
 			{
 				String updateQuery = "UPDATE teachers SET department_id = ?, first_name=?, last_name=?, rmp_id=?, school_id=? WHERE id=?";
 				PreparedStatement updateTeacher = null;
 				try
 				{
 					updateTeacher = connection.prepareStatement(updateQuery);
 					updateTeacher.setString(1, this.getDepartment_id());
 					updateTeacher.setString(2, this.getFirst_name());
 					updateTeacher.setString(3, this.getLast_name());
 					updateTeacher.setString(4, this.getRmpId());
 					updateTeacher.setString(5, this.getSchool_id());
 					updateTeacher.setString(6, this.getId());
 					result = updateTeacher.executeUpdate();
 					if (result > 0)
 					{
 						this.dirty = false;
 						return true;
 					}
 				}
 				catch (SQLException e)
 				{
 					e.printStackTrace();
 					System.exit(1);
 				}
 				finally
 				{
 					try
 					{
 						updateTeacher.close();
 						DBFactory.closeConnection(connection);
 					}
 					catch (SQLException e)
 					{
 						e.printStackTrace();
 						System.exit(1);
 					}
 				}
 			}
 		}
 		else
 		{
 			throw new InvalidModelException(error);
 		}
 		return false;
 	}
 
 	public void setDepartment(Department department)
 	{
 		this.department = department;
 		this.setDepartment_id(department.getId());
 	}
 
 	public void setDepartment_id(String department_id)
 	{
 		this.department_id = department_id;
 	}
 
 	public void setFirst_name(String first_name)
 	{
 		this.first_name = first_name;
 	}
 
 	public void setId(String id)
 	{
 		this.id = id;
 	}
 
 	public void setLast_name(String last_name)
 	{
 		this.last_name = last_name;
 	}
 
 	public void setRmpId(String rmpId)
 	{
 		this.rmpId = rmpId;
 	}
 
 	public void setSchool(School school)
 	{
 		this.setSchool_id(school.getId());
 		this.school = school;
 	}
 
 	public void setSchool_id(String school_id)
 	{
 		this.school_id = school_id;
 	}
 
 	public void setSchools(ArrayList<School> schools)
 	{
 		this.schools = schools;
 	}
 
 }
