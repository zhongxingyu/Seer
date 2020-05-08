 package db;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import crowdtrust.*;
 
 public class TaskDb {
 	
 	private static final String TASKS_DIRECTORY = "/vol/project/2012/362/g1236218/TaskFiles/";
 	
 	public static boolean addTask(int accountID, String name, String question, double accuracy, int type, long expiryTime){
 		StringBuilder sql = new StringBuilder();
 		sql.append("INSERT INTO tasks (name, question, type, accuracy)\n");
 		sql.append("VALUES(?, ?, ?, ?)");
 		Connection c;
 		try {
 			c = DbAdaptor.connect();
 		}
 		catch (ClassNotFoundException e) {
 			System.err.println("Error connecting to DB on add Task: PSQL driver not present");
 		  	return false;
 		} catch (SQLException e) {
 		  	System.err.println("SQL Error on add Task");
 		  	return false;
 		}
         long currentTime = (new Date()).getTime();
 		PreparedStatement insertTask;
         try {
         	insertTask = c.prepareStatement("INSERT INTO tasks VALUES(DEFAULT,?,?,?,?,?,?,?)");
 			insertTask.setInt(1, accountID);
 			insertTask.setString(2, name);
 			insertTask.setString(3, question);
 			insertTask.setDouble(4, accuracy);
 			insertTask.setInt(5, type);
 			insertTask.setTimestamp(6, new Timestamp(expiryTime));
 			insertTask.setTimestamp(7, new Timestamp(currentTime));
 			insertTask.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
         File taskFolder = new File(TASKS_DIRECTORY + accountID + "/" + name);
         if(taskFolder.isDirectory()) {
         	try {
 				insertTask.cancel();
 			} catch (SQLException e) {
 				e.printStackTrace();
 				return false;
 			}	        	
         } else {
         	taskFolder.mkdirs();
         }
         return true;
 	}
 	
 	public static int getSubTaskId(String name){
  		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT id FROM subtasks\n");
 		sql.append("WHERE name = ?");
  		PreparedStatement preparedStatement;
 		try {
 			preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 		}
 		catch (ClassNotFoundException e) {
 		  	System.err.println("Error connecting to DB on get Task: PSQL driver not present");
 		  	return -1;
 		} catch (SQLException e) {
 		  	System.err.println("SQL Error on get Task");
 		  	return -1;
 		}
 		try {
 		    preparedStatement.setString(1, name);
 		    ResultSet resultSet = preparedStatement.executeQuery();
 	    	resultSet.next();
 		    return resultSet.getInt(1);
 		} catch (SQLException e) {
 		  	System.err.println("SELECT task query invalid");
 		  	return -1;
 
 		}
 	}
 	
 	public static Task getTask(String name){
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT * FROM tasks\n");
 		sql.append("WHERE name = ?");
 		PreparedStatement preparedStatement;
 		try {
 			preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 		}
 		catch (ClassNotFoundException e) {
 		  	System.err.println("Error connecting to DB on get Task: PSQL driver not present");
 		  	return null;
 		} catch (SQLException e) {
 		  	System.err.println("SQL Error on get Task");
 		  	return null;
 		}
 		try {
 		    preparedStatement.setString(1, name);
 		    ResultSet resultSet = preparedStatement.executeQuery();
 		    return TaskDb.map(resultSet);
 		} catch (SQLException e) {
 		  	System.err.println("SELECT task query invalid");
 		  	return null;
 		}
 	}
 	
 	public static int getTaskId(String name){
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT id FROM tasks\n");
 		sql.append("WHERE name = ?");
 		PreparedStatement preparedStatement;
 		try {
 			preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 		}
 		catch (ClassNotFoundException e) {
 		  	System.err.println("Error connecting to DB on get Task: PSQL driver not present");
 		  	return -1;
 		} catch (SQLException e) {
 		  	System.err.println("SQL Error on get Task");
 		  	return -1;
 		}
 		try {
 		    preparedStatement.setString(1, name);
 		    ResultSet resultSet = preparedStatement.executeQuery();
 		    resultSet.next();
 		    return resultSet.getInt(1);
 		} catch (SQLException e) {
 		  	System.err.println("SELECT task query invalid");
 		  	return -1;
 		}
 	}
 	
 	public static boolean isPresent(int taskID, int accountID) {
     	PreparedStatement checkTask;
 		try {
 			checkTask = DbAdaptor.connect().prepareStatement("SELECT id FROM tasks WHERE id = ? AND submitter = ?");
 			checkTask.setInt(1, taskID);
 			checkTask.setInt(2, accountID);
 	    	checkTask.execute();
 	    	ResultSet rs = checkTask.getResultSet();
 	    	rs.next();
 	    	return taskID == rs.getInt(1);
 		} catch (SQLException e) { 
 			e.printStackTrace();
 			return false;
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public static Task map(ResultSet resultSet) {
 		Task thisTask = null;
 		try {
 			while(resultSet.next()) {
 				int id = resultSet.getInt("id");
 				String name = resultSet.getString("name");
 				String question = resultSet.getString("question");
 				int type = resultSet.getInt("type");
 				int accuracy = resultSet.getInt("accuracy");
 				if(type == 1) {
 					thisTask = new BinaryTask(id, name, question, accuracy);
 				}
 				if(type == 2) {
 						//thisTask = new SingleContinuousTask(id, name, question, accuracy);
 				}							
 				if(type == 3) {
 						thisTask = new MultiValueTask(id, name, question, accuracy);
 				}					
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return thisTask;
 	}
 
 	public static boolean checkFinished(int id) throws SQLException {
 		StringBuilder sql = new StringBuilder();
 	    sql.append("SELECT * FROM tasks JOIN subtasks ON tasks.id = subtasks.task ");
 	    sql.append("WHERE tasks.id = ?");
 	    PreparedStatement preparedStatement;
 	    try {
 	    preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 	    }
 	    catch (ClassNotFoundException e) {
 	    	System.err.println("Error connecting to DB on check finished: PSQL driver not present");
 	      	return true;
 	    } catch (SQLException e) {
 	      	System.err.println("SQL Error on check finished");
 	      	return true;
 	    }
 	    preparedStatement.setString(1, Integer.toString(id));
 	    ResultSet resultSet = preparedStatement.executeQuery();
 	    if(!resultSet.next() || !resultSet.isLast()) {
 		      //task does not exist, grave error TODO log it
 		  return true;
 		}
 	    return false;    
 
 	}
 	
 	public static List<Task> getTasksForCrowdId(int id) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT tasks.name FROM tasks ");
 		sql.append("WHERE tasks.ex_time > NOW()");
 		PreparedStatement preparedStatement;
 		List<Task> tasks = new ArrayList<Task>();
 		try {
 			preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 //			preparedStatement.setInt(1, id);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			while(resultSet.next()) {
				String taskName = resultSet.getString("task.name");
 				Task task = getTask(taskName);
 				tasks.add(task);
 			}
 		}
 	    catch (ClassNotFoundException e) {
 	    	System.err.println("Error connecting to DB on get tasks for id: PSQL driver not present");
 	    } catch (SQLException e) {
 	      	System.err.println("SQL Error on get tasks for id");
 	    }
 		return tasks;
 	}
 	
 }
