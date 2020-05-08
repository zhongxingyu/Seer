 package db;
 
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
 	
 public static int addTask(int accountID, String name, String question, float accuracy, 
 			int media_type, int annotation_type, int input_type, int max_labels, long expiryTime, 
 			List<String> answerList){
 		Connection c;
 		try {
 			c = DbAdaptor.connect();
 		}
 		catch (ClassNotFoundException e) {
 			System.err.println("Error connecting to DB on add Task: PSQL driver not present");
 		  	return -1;
 		} catch (SQLException e) {
 		  	System.err.println("SQL Error on add Task");
 		  	return -1;
 		}
 		String answerChoice = "";
 		for (String thisChoice : answerList) {
 			answerChoice += thisChoice + "/";
 		}
 		//get rid of trailing '/'
 		answerChoice = answerChoice.substring(0, answerChoice.length()-1);
         long currentTime = (new Date()).getTime();
 		PreparedStatement insertTask;
         try {
         	insertTask = c.prepareStatement("INSERT INTO tasks VALUES(DEFAULT,?,?,?,?,?,?,?,?,?,?,?) RETURNING id");
 			insertTask.setInt(1, accountID);
 			insertTask.setString(2, name);
 			insertTask.setString(3, question);
 			insertTask.setFloat(4, accuracy);
 			insertTask.setInt(5, media_type);
 			insertTask.setInt(6, annotation_type);
 			insertTask.setInt(7, input_type);
 			insertTask.setString(8, answerChoice);
 			insertTask.setInt(9, max_labels);
 			insertTask.setTimestamp(10, new Timestamp(expiryTime));
 			insertTask.setTimestamp(11, new Timestamp(currentTime));
 			insertTask.execute();
 			ResultSet rs = insertTask.getResultSet();
 			rs.next();
 			return rs.getInt(1);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return -1;
 		}
 	}
 
 	/*public static int getSubTaskId(String name){
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
 	}*/
 
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
 				int id = resultSet.getInt("id");
 				String name = resultSet.getString("name");
 				String question = resultSet.getString("question");
 				int media_type = resultSet.getInt("media_type");
 				int annotation_type = resultSet.getInt("annotation_type");
 				int input_type = resultSet.getInt("input_type");
 				String answersString = resultSet.getString("answers");
 				String[] answers = answersString.split("/");
 				int accuracy = resultSet.getInt("accuracy");
 				if(annotation_type == 1) {
 					thisTask = new BinaryTask(id, name, question, accuracy, media_type, input_type, answers);
 				}
 				if(annotation_type == 2) {
 						//thisTask = new SingleContinuousTask(id, name, question, accuracy);
 				}							
 				if(annotation_type == 3) {
 						thisTask = new MultiValueTask(id, name, question, accuracy, media_type, input_type, answers);
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
 		sql.append("SELECT * FROM tasks ");
 		sql.append("WHERE tasks.ex_time > NOW()");
 		PreparedStatement preparedStatement;
 		List<Task> tasks = new ArrayList<Task>();
 		try {
 			preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 //			preparedStatement.setInt(1, id);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			while(resultSet.next()) {
 				tasks.add(map(resultSet));
 			}
 		}
 	    catch (ClassNotFoundException e) {
 	    	System.err.println("Error connecting to DB on get tasks for id: PSQL driver not present");
 	    } catch (SQLException e) {
 	      	System.err.println("SQL Error on get tasks for crowdid");
 	    }
 		return tasks;
 	}
 
 	public static List<Task> getTasksForClientId(int id) {
 		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM tasks WHERE submitter = ?");
 		List<Task> tasks = new ArrayList<Task>();
 		PreparedStatement preparedStatement;
 		try {
 			preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 			preparedStatement.setInt(1, id);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			while(resultSet.next()) {
 				tasks.add(map(resultSet));
 			}
 		}
 		catch (ClassNotFoundException e) {
 			System.err.println("Error connecting to DB on get Task: PSQL driver not present");
 		} catch (SQLException e) {
 			System.err.println("SQL Error on get Tasks for crowdid");
 		}
 		return tasks;
 	}
 	
 }
