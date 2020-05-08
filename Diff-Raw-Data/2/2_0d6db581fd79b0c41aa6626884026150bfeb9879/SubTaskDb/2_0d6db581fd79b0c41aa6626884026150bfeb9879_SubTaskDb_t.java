 package db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import crowdtrust.Bee;
 import crowdtrust.Response;
 import crowdtrust.Estimate;
 import crowdtrust.Task;
 public class SubTaskDb {
 
 	public static boolean close(int id) {
 			StringBuilder sql = new StringBuilder();
 		      sql.append("UPDATE subtasks (active) ");
 		      sql.append("VALUES(FALSE)");
 		      try {
 		        PreparedStatement preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 		        preparedStatement.execute();
 		      }
 		      catch (ClassNotFoundException e) {
 		      	  System.err.println("Error connecting to DB on subtask close: PSQL driver not present");
 		      	  return false;
 		        } catch (SQLException e) {
 		      	  System.err.println("SQL Error on subtask close");
 		      	  return false;
 		        }
 		      return true;
 	}
 
 	public static Task getTask(int id) {
 		StringBuilder sql = new StringBuilder();
       sql.append("SELECT * FROM tasks JOIN subtasks ON tasks.id = subtasks.task");
       sql.append("WHERE subtasks.id = ?");
       try {
         PreparedStatement preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
         preparedStatement.setString(1, Integer.toString(id));
         ResultSet resultSet = preparedStatement.executeQuery();
         if(!resultSet.next() || !resultSet.isLast()) {
 	      //task does not exist, grave error TODO log it
         	System.err.println("Subtask: " + id + " doesn't exist");
         	return null;
 	    }
         return TaskDb.map(resultSet);
       } catch (ClassNotFoundException e) {
       	  System.err.println("Error connecting to DB on get Subtask: PSQL driver not present");
       	  return null;
       } catch (SQLException e) {
       	  System.err.println("SQL Error on get Subtask");
       	  return null;
       }
 	}
 	
 	public static Map<Bee, Response> getBinaryResponses(int id, Bee[] annotators) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public static Task getRandomSubTask() {
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT task FROM subtasks");
 		PreparedStatement preparedStatement;
 
 	    try {
 	    preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 	    }
 	    catch (ClassNotFoundException e) {
 	    	System.err.println("Error connecting to DB on check finished: PSQL driver not present");
 	      	return null;
 	    } catch (SQLException e) {
 	      	System.err.println("SQL Error on check finished");
 	      	return null;
 	    }
 			try {
 				ResultSet rs = preparedStatement.executeQuery();
 				int taskId = rs.getInt("task");
 				Task task = getTask(taskId);
 				return task;
 			}
 			catch(SQLException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 	   public static List<String> getImageSubtasks() {
 		StringBuilder sql = new StringBuilder();
 	      sql.append("SELECT tasks.name, subtasks.file_name, tasks.date_created FROM tasks JOIN subtasks ON tasks.id = subtasks.task ");
 	      sql.append("WHERE subtasks.file_name LIKE '%.jpg' OR subtasks.file_name LIKE '%.png' ORDER BY tasks.date_created");
 	      List<String> list = new LinkedList<String>();
 	      PreparedStatement preparedStatement;
 	      try {
 	        preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 	      }
 	      catch (ClassNotFoundException e) {
 	      	  System.err.println("Error connecting to DB on get Subtask: PSQL driver not present");
 	      	  return list;
 	      } catch (SQLException e) {
 	      	  System.err.println("SQL Error on connection during get image subtask");
 	      	  return list;
 	      }		
 	      try {
 			preparedStatement.execute();
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	      ResultSet resultSet;
 	      try{
 	        resultSet = preparedStatement.getResultSet();
 	      } catch (SQLException e) {
 	    	  System.err.println("problem executing stement");
 	    	  return list;
 	      }
 	      try{
 	        if(!resultSet.next() || !resultSet.isLast()) {
 		      //task does not exist, grave error TODO log it
 		    }
 	        for (int i = 0 ; !resultSet.isLast() && i < 5 ; i++) {
 	        	String subtask = resultSet.getString(2);
 	        	String task = resultSet.getString(1);
 	        	list.add(task + "/" + subtask);
 	        	resultSet.next();
 	        }
 	      } catch(SQLException e) {
 	    	  System.err.println("problem with result set");
 	      }
 	      return list;
 	}
 	
 	public static boolean addSubtask(String filename, int taskID) {
 	        String insertQuery = "INSERT INTO subtasks VALUES (DEFAULT,?,?,?)";
 	        PreparedStatement stmt;
 	        try {
 				stmt = DbAdaptor.connect().prepareStatement(insertQuery);
 				stmt.setInt(1, taskID);
 		        stmt.setString(2, filename);
 		        stmt.setBoolean(3,  true);
 		        stmt.execute();
 		    } catch (SQLException e1) {
 				System.err.println("some error with task fields: taskID not valid?");
 				System.err.println("taskID: " + taskID + ", filename: " + filename);
 				return false;
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return false;
 			}
         return true;
 	}
 
 	public static Map<Bee, Response> getMultiValueResponses(int id,
 			Bee[] annotators) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public static Map<Bee, Response> getContinuousResponses(int id,
 			Bee[] annotators) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public static int getSubTaskId(String name){
  		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT id FROM subtasks\n");
		sql.append("WHERE file_name = ?");
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
 
 }
