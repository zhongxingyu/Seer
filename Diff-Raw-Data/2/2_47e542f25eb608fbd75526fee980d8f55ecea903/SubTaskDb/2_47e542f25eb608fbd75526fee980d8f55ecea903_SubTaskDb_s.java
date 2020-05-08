 package db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 
 import crowdtrust.Bee;
 import crowdtrust.BinaryR;
 import crowdtrust.MultiValueSubTask;
 import crowdtrust.Response;
 import crowdtrust.Estimate;
 import crowdtrust.BinarySubTask;
 import crowdtrust.SubTask;
 import crowdtrust.Task;
 
 public class SubTaskDb {
 
 	public static boolean close(int id) {
 			String sql = "UPDATE subtasks SET active = FALSE WHERE subtasks.id = ?";
 		      try {
 		        PreparedStatement preparedStatement = DbAdaptor.connect().prepareStatement(sql);
 		        preparedStatement.setInt(1, id);
 		        preparedStatement.execute();
 		      }
 		      catch (ClassNotFoundException e) {
 		      	  System.err.println("Error connecting to DB on subtask close: PSQL driver not present");
 		      	  e.printStackTrace();
 		      	  return false;
 		        } catch (SQLException e) {
 		      	  System.err.println("SQL Error on subtask close");
 		      	e.printStackTrace();
 		      	  return false;
 		        }
 		      return true;
 	}
 
 	public static Task getTask(int id) {
 		StringBuilder sql = new StringBuilder();
       sql.append("SELECT * FROM tasks JOIN subtasks ON tasks.id = subtasks.task ");
       sql.append("WHERE subtasks.id = ?");
       try {
         PreparedStatement preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
         preparedStatement.setInt(1, id);
         ResultSet resultSet = preparedStatement.executeQuery();
         if(!resultSet.next()) {
 	      //task does not exist, grave error TODO log it
         	System.err.println("Subtask: " + id + " doesn't exist");
         	return null;
 	    }
         return TaskDb.map(resultSet);
       } catch (ClassNotFoundException e) {
       	  System.err.println("Error connecting to DB on get Subtask: PSQL driver not present");
       	e.printStackTrace();
       	  return null;
       } catch (SQLException e) {
       	  System.err.println("SQL Error on get Subtask");
       	e.printStackTrace();
       	  return null;
       }
 	}
 	
 	public static Map<Integer, Response> getBinaryResponses(int id) {
 		HashMap<Integer,Response> responses = new HashMap <Integer,Response>();
 		
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
 		    	e.printStackTrace();
 		    	return null;
 		    } catch (SQLException e) {
 		      	System.err.println("SQL Error on check finished");
 		      	e.printStackTrace();
 		      	return null;
 		    }
 		ResultSet resultSet;
 		try {
 			resultSet = preparedStatement.executeQuery();
 			while (resultSet.next()){
 				BinaryR br = new BinaryR(resultSet.getBytes("response"));
 				responses.put(resultSet.getInt("account"), br);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//FINISH THIS!
 		return null;
 	}
 
 	public static SubTask getRandomBinarySubTask(int task) {
 		
 		String sql = "SELECT subtasks.id AS s, tasks.accuracy AS a, tasks.max_labels AS m, " +
 				"COUNT(responses.id) AS r FROM subtasks JOIN tasks ON subtasks.task = tasks.id " +
 				"LEFT JOIN responses ON responses.subtask = subtasks.id WHERE tasks.id = ? " +
 				"GROUP BY s,a,m ORDER BY random() LIMIT 1";
 		
 		PreparedStatement preparedStatement;
 	    try {
 	    preparedStatement = DbAdaptor.connect().prepareStatement(sql);
 	    preparedStatement.setInt(1, task);
 	    }
 	    catch (ClassNotFoundException e) {
 	    	System.err.println("Error connecting to DB on check finished: PSQL driver not present");
 	      	e.printStackTrace();
 	    	return null;
 	    } catch (SQLException e) {
 	      	System.err.println("SQL Error on check finished");
 	      	e.printStackTrace();
 	      	return null;
 	    }
 		try {
 			ResultSet rs = preparedStatement.executeQuery();
 			rs.next();
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
 	      sql.append("SELECT tasks.id, subtasks.file_name, tasks.date_created, tasks.submitter FROM tasks JOIN subtasks ON tasks.id = subtasks.task ");
	      sql.append("WHERE tasks.media_type=1 ORDER BY tasks.date_created DES");
 	      List<String> list = new LinkedList<String>();
 	      PreparedStatement preparedStatement;
 	      try {
 	        preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 	      }
 	      catch (ClassNotFoundException e) {
 	      	  System.err.println("Error connecting to DB on get Subtask: PSQL driver not present");
 	      	e.printStackTrace();
 	      	  return list;
 	      } catch (SQLException e) {
 	      	  System.err.println("SQL Error on connection during get image subtask");
 	      	e.printStackTrace();
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
 	    	  e.printStackTrace();
 	    	  return list;
 	      }
 	      try{
 	        for (int i = 0 ; resultSet.next() && i < 5 ; i++) {
 	        	String subtask = resultSet.getString(2);
 	        	int task = resultSet.getInt(1);
 	        	int submitter = resultSet.getInt(1); // may be useful to display uname of uploader
 	        	list.add(task + "/" + subtask);
 	        	resultSet.next();
 	        }
 	      } catch(SQLException e) {
 	    	  System.err.println("problem with result set");
 	    	  e.printStackTrace();
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
 				e1.printStackTrace();
 				return false;
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return false;
 			}
         return true;
 	}
 
 	public static Map<Integer, Response> getMultiValueResponses(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public static Map<Integer, Response> getContinuousResponses(int id) {
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
 		  	e.printStackTrace();
 		  	return -1;
 		} catch (SQLException e) {
 		  	System.err.println("SQL Error on get Task");
 		  	e.printStackTrace();
 		  	return -1;
 		}
 		try {
 		    preparedStatement.setString(1, name);
 		    ResultSet resultSet = preparedStatement.executeQuery();
 	    	resultSet.next();
 		    return resultSet.getInt(1);
 		} catch (SQLException e) {
 		  	System.err.println("SELECT task query invalid");
 		  	e.printStackTrace();
 		  	return -1;
 		}
 	}
 
 	public static BinarySubTask getBinarySubTask(int subTaskId) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT subtasks.id AS s, tasks.accuracy AS a,");
 		sql.append("tasks.max_labels AS m, COUNT(responses.id) AS r ");
 		sql.append("FROM subtasks JOIN tasks ON subtasks.task = tasks.id ");
 		sql.append("LEFT JOIN responses ON responses.id ");
 		sql.append("WHERE subtasks.id = ? ");
 		sql.append("GROUP BY s,a,m ");
 		PreparedStatement preparedStatement;
 	    try {
 	    	preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
 	    	preparedStatement.setInt(1, subTaskId);
 	    }	    catch (ClassNotFoundException e) {
 	    	System.err.println("Error connecting to DB on check finished: PSQL driver not present");
 	    	e.printStackTrace();
 	    	return null;
 	    } catch (SQLException e) {
 	      	System.err.println("SQL Error on check finished");
 	      	e.printStackTrace();
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
 	    	e.printStackTrace();
 	    	return null;
 	    } catch (SQLException e) {
 	      	System.err.println("SQL Error on check finished");
 	      	e.printStackTrace();
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
 
 	public static Collection<Estimate> getBinaryEstimates(int id) {
 		String sql = "SELECT estimate, confidence " +
 				"FROM estimates " +
 				"WHERE subtask_id = ?";
 		
 		PreparedStatement preparedStatement;
 		
 		ArrayList<Estimate> state = new ArrayList<Estimate>();
 		
 	    try {
 	    	preparedStatement = DbAdaptor.connect().prepareStatement(sql);
 	    	preparedStatement.setInt(1, id);
 	    }	    catch (ClassNotFoundException e) {
 	    	System.err.println("Error connecting to DB on check finished: PSQL driver not present");
 	    	e.printStackTrace();
 	    	return null;
 	    } catch (SQLException e) {
 	      	System.err.println("SQL Error on check finished");
 	      	e.printStackTrace();
 	      	return null;
 	    }
 		try {
 			ResultSet rs = preparedStatement.executeQuery();
 			while(rs.next()){
 				BinaryR r = new BinaryR(rs.getBytes("estimate"));
 				double c = rs.getFloat("confidence");
 				state.add(new Estimate(r,c));
 			}
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return state;
 	}
 
 	public static void updateBinaryEstimates(Collection<Estimate> state, int id) {
 
 			String query = "UPDATE estimates SET confidence = ? " +
 					"WHERE subtask_id = ? AND estimate = ?";
 			
 			PreparedStatement preparedStatement;
 	        
 			try {
 		    	preparedStatement = DbAdaptor.connect().prepareStatement(query);
 		    	for (Estimate e : state){
 		    		preparedStatement.setFloat(1, (float) e.getConfidence());
 		    		preparedStatement.setInt(2, id);
 					preparedStatement.setString(3, e.getR().serialise());
 					preparedStatement.addBatch();
 				}  
 		    	preparedStatement.executeBatch();
 		    }	    catch (ClassNotFoundException e) {
 		    	System.err.println("Error connecting to DB on check finished: PSQL driver not present");
 		    	e.printStackTrace();
 		    } catch (SQLException e) {
 		      	System.err.println("SQL Error on check finished");
 		      	e.printStackTrace();
 		      	System.out.println("-------------------");
 		      	e.getNextException().printStackTrace();
 		    }
 			
 	}
 
 	public static void addBinaryEstimate(Estimate est, int id) {
 		String query = "INSERT INTO estimates VALUES (DEFAULT,?,?,?)";
 		
 		PreparedStatement preparedStatement;
         
 		try {
 	    	preparedStatement = DbAdaptor.connect().prepareStatement(query);
 	    	preparedStatement.setFloat(1, (float) est.getConfidence());
 			preparedStatement.setString(2, est.getR().serialise());
 			preparedStatement.setInt(3, id);
 			preparedStatement.execute();
 	    }	    catch (ClassNotFoundException e) {
 	    	System.err.println("Error connecting to DB on check finished: PSQL driver not present");
 	    	e.printStackTrace();
 	    } catch (SQLException e) {
 	      	System.err.println("SQL Error on check finished");
 	      	e.printStackTrace();
 	    }
 		
 	}
 	
 
 }
