 package models;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import database.DBFactory;
 import database.InvalidModelException;
 import database.Model;
 import database.ModelList;
 
 public class SchoolClass extends Model
 {
 
 	private Department department;
 	private String department_id;
 	private String id;
 	private String level;
 
 	public SchoolClass(Department department, String level)
 	{
 		this(null, department.getId(), level, true, true);
 	}
 
 	public SchoolClass(ResultSet resultSet) throws SQLException
 	{
 		this(resultSet.getString("id"), resultSet.getString("department_id"),
 				resultSet.getString("level"), false, false);
 	}
 
 	public SchoolClass(String id, String departmentId, String level,
 			Boolean dirty, Boolean fresh)
 	{
 		super("classes", new ArrayList<String>()
 		{
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = -8655573260566651854L;
 
 			{
 				this.add("id");
 				this.add("department_id");
 				this.add("level");
 			}
 		}, new ArrayList<String>()
 		{
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 4206528093354073916L;
 
 			{
 			}
 		}, dirty, fresh);
 		this.setId(id);
 		this.setDepartment_id(departmentId);
 		this.setLevel(level);
 	}
 
 	public static ModelList<SchoolClass> findByTeacher(String teacher_id)
 	{
 		System.out.println("find classes by teacher");
 		ModelList<SchoolClass> schoolClasses = new ModelList<>();
 
 		Connection connection = DBFactory.getConnection();
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 
 		try
 		{
 
			String query = "SELECT id, department_id, level FROM classes JOIN teachers ON teachers.department_id=classes.department_id WHERE teachers.id=?";
 			statement = connection.prepareStatement(query);
 			statement.setString(1, teacher_id);
 			resultSet = statement.executeQuery();
 
 			while (resultSet.next())
 			{
 				SchoolClass schoolClass = new SchoolClass(resultSet);
 				schoolClasses.add(schoolClass);
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
 		return schoolClasses;
 	}
 
 	public static SchoolClass findOrCreate(Department department, String level)
 			throws InvalidModelException
 	{
 		String findQuery = "SELECT id, department_id, level FROM classes WHERE department_id = ? AND level = ? LIMIT 1";
 		Connection connection = DBFactory.getConnection();
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		try
 		{
 			statement = connection.prepareStatement(findQuery);
 			statement.setString(1, department.getId());
 			statement.setString(2, level);
 
 			resultSet = statement.executeQuery();
 			if (resultSet.next())
 			{
 				return new SchoolClass(resultSet);
 			}
 			else
 			{
 				SchoolClass schoolClass = new SchoolClass(department, level);
 				schoolClass.save();
 				return schoolClass;
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
 		return this.department;
 	}
 
 	public String getDepartment_id()
 	{
 		return this.department_id;
 	}
 
 	public String getId()
 	{
 		return this.id;
 	}
 
 	public String getLevel()
 	{
 		return this.level;
 	}
 
 	@Override
 	public String isValid()
 	{
 		String valid = null;
 		if (!this.isFresh())
 		{
 			valid = (this.getId() != null) ? null
 					: "An existing class needs an ID";
 		}
 		if (valid == null)
 		{
 			valid = (this.getLevel() != null) ? null : "A class needs a level";
 		}
 		if (valid == null)
 		{
 			valid = (this.getDepartment_id() != null) ? null
 					: "A class requires a department";
 			System.out.println(this);
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
 			PreparedStatement insertNewSchoolClass = null;
 			ResultSet resultSet = null;
 			PreparedStatement selectId = null;
 			if (this.isFresh())
 			{
 				System.out.println("saving new school class");
 				String insertQuery = "INSERT INTO classes (department_id, level) VALUES (?, ?)";
 				try
 				{
 					insertNewSchoolClass = connection
 							.prepareStatement(insertQuery);
 					insertNewSchoolClass.setString(1, this.getDepartment_id());
 					insertNewSchoolClass.setString(2, this.getLevel());
 					result = insertNewSchoolClass.executeUpdate();
 					if (result > 0)
 					{
 						String idQuery = "SELECT id FROM classes WHERE department_id=? AND level=?";
 						selectId = connection.prepareStatement(idQuery);
 						selectId.setString(1, this.getDepartment_id());
 						selectId.setString(2, this.getLevel());
 						resultSet = selectId.executeQuery();
 
 						if (resultSet.next())
 						{
 							this.id = resultSet.getString(1);
 							System.out.println("save get id: " + this.id);
 							this.setId(this.id);
 							this.fresh = false;
 							this.dirty = false;
 							return true;
 						}
 						else
 						{
 							System.err.println("Error getting class ID");
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
 						insertNewSchoolClass.close();
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
 				String updateQuery = "UPDATE classes SET department_id=?, level=? WHERE id=?";
 				PreparedStatement updateSchoolClass = null;
 				try
 				{
 					updateSchoolClass = connection
 							.prepareStatement(updateQuery);
 					updateSchoolClass.setString(1, this.getDepartment_id());
 					updateSchoolClass.setString(2, this.getLevel());
 					updateSchoolClass.setString(3, this.getId());
 					result = updateSchoolClass.executeUpdate();
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
 						updateSchoolClass.close();
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
 		this.setDepartment_id(department.getId());
 		this.department = department;
 	}
 
 	public void setDepartment_id(String department_id)
 	{
 		this.department_id = department_id;
 	}
 
 	public void setId(String id)
 	{
 		this.id = id;
 	}
 
 	public void setLevel(String level)
 	{
 		this.level = level;
 	}
 
 }
