 package db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import crowdtrust.Bee;
 import crowdtrust.MultiValueSubTask;
 import crowdtrust.Response;
 import crowdtrust.Estimate;
 import crowdtrust.BinarySubTask;
 import crowdtrust.SubTask;
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
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT account, response");
 		sql.append("FROM responses");
 		sql.append("WHERE subtask = ?");
 		PreparedStatement preparedStatement;
 		try {
 		    preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 		    preparedStatement.setInt(1, id);
 		    }
 		    catch (ClassNotFoundException e) {
 		    	System.err.println("Error connecting to DB on check finished: PSQL driver not present");
 		      	return null;
 		    } catch (SQLException e) {
 		      	System.err.println("SQL Error on check finished");
 		      	return null;
 		    }
 		
 		//FINISH THIS!
 		return null;
 	}
 
 	public static SubTask getRandomBinarySubTask(int task) {
 		StringBuilder sql = new StringBuilder();
		sql.append("SELECT subtasks.id AS s, tasks.accuracy AS a,");
		sql.append("tasks.max_labels AS m, COUNT(responses.id) AS r");
 		sql.append("FROM subtasks JOIN tasks ON subtasks.task = tasks.id");
 		sql.append("LEFT JOIN responses ON responses.subtask = subtasks.id");
 		sql.append("WHERE tasks.id = ?");
 		sql.append("GROUP BY s,a,m");
 		sql.append("ORDER BY random()");
 		sql.append("LIMIT 1");
 		PreparedStatement preparedStatement;
 	    try {
 	    preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 	    preparedStatement.setInt(1, task);
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
 			int taskAccuracy = rs.getInt("a");
 			int id = rs.getInt("s");
 			int responses = rs.getInt("r");
 			int maxLabels = rs.getInt("m");
 			return new BinarySubTask(id, taskAccuracy, responses, maxLabels);
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static List<String> getImageSubtasks() {
 		StringBuilder sql = new StringBuilder();
 	      sql.append("SELECT tasks.name, subtasks.file_name, tasks.date_created, tasks.submitter FROM tasks JOIN subtasks ON tasks.id = subtasks.task ");
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
 	        	int submitter = resultSet.getInt(1);
 	        	list.add(submitter + "/" + task + "/" + subtask);
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
 		} catch (ClassNotFoundException e) {
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
 
 	public static BinarySubTask getBinarySubTask(int subTaskId) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT subtasks.id AS s, tasks.accuracy AS a,");
 		sql.append("tasks.max_labels AS m, COUNT(responses.id) AS r");
 		sql.append("FROM subtasks JOIN tasks ON subtasks.task = tasks.id");
 		sql.append("LEFT JOIN responses ON responses.id");
 		sql.append("WHERE subtasks.id = ?");
 		sql.append("GROUP BY s,a,m");
 		PreparedStatement preparedStatement;
 	    try {
 	    	preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 	    	preparedStatement.setInt(1, subTaskId);
 	    }	    catch (ClassNotFoundException e) {
 	    	System.err.println("Error connecting to DB on check finished: PSQL driver not present");
 	      	return null;
 	    } catch (SQLException e) {
 	      	System.err.println("SQL Error on check finished");
 	      	return null;
 	    }
 		try {
 			ResultSet rs = preparedStatement.executeQuery();
 			int taskAccuracy = rs.getInt("a");
 			int id = rs.getInt("s");
 			int responses = rs.getInt("r");
 			int maxLabels = rs.getInt("m");
 			return new BinarySubTask(id, taskAccuracy, responses, maxLabels);
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	    
 	    
 	}
 
 	public static MultiValueSubTask getMultiValueSubtask(int subTaskId) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT subtasks.id AS s, tasks.accuracy AS a,");
 		sql.append("tasks.max_labels AS m, ranged.finish AS o, COUNT(responses.id) AS r");
 		sql.append("FROM subtasks JOIN tasks ON subtasks.task = tasks.id");
 		sql.append("LEFT JOIN ranged ON subtasks.id = ranged.id");
 		sql.append("LEFT JOIN responses ON responses.id");
 		sql.append("WHERE subtasks.id = ?");
 		sql.append("GROUP BY s,a,m,o");
 		PreparedStatement preparedStatement;
 	    try {
 	    	preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 	    	preparedStatement.setInt(1, subTaskId);
 	    }	    catch (ClassNotFoundException e) {
 	    	System.err.println("Error connecting to DB on check finished: PSQL driver not present");
 	      	return null;
 	    } catch (SQLException e) {
 	      	System.err.println("SQL Error on check finished");
 	      	return null;
 	    }
 		try {
 			ResultSet rs = preparedStatement.executeQuery();
 			int taskAccuracy = rs.getInt("a");
 			int id = rs.getInt("s");
 			int responses = rs.getInt("r");
 			int maxLabels = rs.getInt("m");
 			int options = rs.getInt("o");
 			return new MultiValueSubTask(id, taskAccuracy, responses, maxLabels, options);
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 
 }
