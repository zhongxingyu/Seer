 package db;
 
 import java.io.UnsupportedEncodingException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import crowdtrust.BinaryR;
 import crowdtrust.ContinuousR;
 import crowdtrust.ContinuousSubTask;
 import crowdtrust.MultiValueR;
 import crowdtrust.MultiValueSubTask;
 import crowdtrust.Response;
 import crowdtrust.Estimate;
 import crowdtrust.BinarySubTask;
 import crowdtrust.Result;
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
       Connection c = null;
       try {
     	c = DbAdaptor.connect();
         PreparedStatement preparedStatement = c.prepareStatement(sql.toString());
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
       } finally {
     	  try {
 			c.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
       }
 	}
 
 	public static SubTask getRandomSubTask(int task, int annotator) {
 		
 		String sql = "SELECT tasks.annotation_type AS type, " +
 				"subtasks.id AS sid, tasks.accuracy AS acc, " +
 				"tasks.max_labels AS ml, COUNT(responses.id) AS c," +
 				"subtasks.file_name AS f, start, finish, p " +
 				"FROM subtasks JOIN tasks ON subtasks.task = tasks.id " +
 				"LEFT JOIN responses ON responses.subtask = subtasks.id " +
 				"LEFT JOIN ranged ON tasks.id = ranged.task " +
 				"WHERE tasks.id = ? AND subtasks.active " +
 				"AND NOT EXISTS " +
 				"(SELECT * FROM responses answered " +
 				"WHERE answered.subtask = subtasks.id " +
 				"AND answered.account = ?) " +
 				"GROUP BY sid, acc, ml, f, start, finish, p, type " +
 				"ORDER BY random() " +
 				"LIMIT 1";
 		
 		PreparedStatement preparedStatement;
 	    try {
 	    preparedStatement = DbAdaptor.connect().prepareStatement(sql);
 	    preparedStatement.setInt(1, task);
 	    preparedStatement.setInt(2, annotator);
 	    }
 	    catch (ClassNotFoundException e) {
 	    	System.err.println("Error connecting to DB on check finished: PSQL driver not present");
 	      	e.printStackTrace();
 	    	return null;
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	      	System.err.println("SQL Error on check finished");
 	      	e.printStackTrace();
 	      	return null;
 	    }
 		try {
 			ResultSet rs = preparedStatement.executeQuery();
 			if(rs.next()){
 				int type = rs.getInt("type");
 				return mapSubTask(rs, type);
 			}
 			return null;
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static List<String> getImageSubtasks() {
 		StringBuilder sql = new StringBuilder();
 	      sql.append("SELECT tasks.id, subtasks.file_name, tasks.date_created, tasks.submitter FROM tasks JOIN subtasks ON tasks.id = subtasks.task ");
 	      sql.append("WHERE tasks.media_type=1 ORDER BY tasks.date_created DESC");
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
 
 	public static SubTask getSubtask(int subTaskId) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT tasks.annotator_type AS type, " +
 				"subtasks.id AS sid, tasks.accuracy AS acc,");
 		sql.append("tasks.max_labels AS ml, ranged.finish AS finish, " +
 				"ranged.start AS start, ranged.p AS p, " +
 				"COUNT(responses.id) AS c");
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
 			int type = rs.getInt("type");
 			return mapSubTask(rs, type);
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private static Collection<Estimate> getEstimates(int id, int type) {
 		String sql = "SELECT estimate, confidence, frequency " +
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
 				String serialised = rs.getString("estimate");
 				double c = rs.getFloat("confidence");
 				int f = rs.getInt("frequency");
 				state.add(new Estimate(mapResponse(serialised, type),c,f));
 			}
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return state;
 	}
 
 	public static void updateEstimates(Collection<Estimate> state, int id) {
 
 			String query = "UPDATE estimates SET (confidence, frequency) = (?,?) " +
 					"WHERE subtask_id = ? AND estimate = ?";
 			
 			PreparedStatement preparedStatement;
 	        
 			try {
 		    	preparedStatement = DbAdaptor.connect().prepareStatement(query);
 		    	for (Estimate e : state){
 		    		preparedStatement.setFloat(1, (float) e.getConfidence());
 		    		preparedStatement.setInt(2, e.getFrequency());
 		    		preparedStatement.setInt(3, id);
 					preparedStatement.setString(4, e.getR().serialise());
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
 	
 	public static Map<Integer, Response> getMappedResults(int taskId){
 		String sql = "SELECT t.annotation_type AS type, " +
 				"e.subtask_id AS sid, e.estimate AS est " +
 				"FROM estimates e JOIN(" +
 				"SELECT subtasks.id, estimates.confidence, " +
 				"MAX(estimates.frequency) AS f FROM " +
 				"tasks JOIN subtasks ON subtasks.task = tasks.id " +
 				"JOIN estimates ON subtasks.id = estimates.subtask_id " +
 				"WHERE tasks.id = ? " +
 				"AND estimates.confidence IN(" +
 				"SELECT MAX(confidence) " +
 				"FROM estimates best " +
 				"WHERE best.subtask_id = estimates.subtask_id " +
 				"GROUP BY best.subtask_id) " +
 				"GROUP BY subtasks.id, confidence " +
 				") AS foo  " +
 				"ON e.subtask_id = foo.id " +
 				"AND e.confidence = foo.confidence " +
 				"AND e.frequency = foo.f " +
 				"JOIN subtasks s ON e.subtask_id = s.id " +
 				"JOIN tasks t ON s.task = t.id";
 		
 		PreparedStatement preparedStatement;
 		
 		Map<Integer,Response> results = new HashMap<Integer,Response>();
 		
 	    try {
 	    	preparedStatement = DbAdaptor.connect().prepareStatement(sql);
 	    	preparedStatement.setInt(1, taskId);
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
 				int s = rs.getInt("sid");
 				int type = rs.getInt("type");
 				String e = rs.getString("est");
 				results.put(s, mapResponse(e, type));
 			}
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return results;
 	}
 
 	public static Collection<Result> getResults(int taskId){
 		String sql = "SELECT t.annotation_type AS type, " +
 				"t.accuracy AS acc, " +
 				"e.subtask_id AS sid, e.estimate AS est, " +
 				"t.max_labels AS ml, start, finish, p, " +
 				"e.confidence AS conf, COUNT(res.id) AS c, " +
 				"s.file_name AS f " +
 				"FROM estimates e " +
 				"JOIN( " +
 				"SELECT subtasks.id, estimates.confidence, " +
 				"MAX(estimates.frequency) AS f FROM " +
 				"tasks JOIN subtasks ON subtasks.task = tasks.id " +
 				"JOIN estimates ON subtasks.id = estimates.subtask_id " +
 				"WHERE tasks.id = 1 AND estimates.confidence IN( " +
 				"SELECT MAX(confidence) " +
 				"FROM estimates best " +
 				"WHERE best.subtask_id = estimates.subtask_id " +
 				"GROUP BY best.subtask_id) " +
 				"GROUP BY subtasks.id, confidence ) AS foo " +
 				"ON e.subtask_id = foo.id " +
 				"AND e.confidence = foo.confidence " +
 				"AND e.frequency = foo.f " +
 				"JOIN subtasks s ON e.subtask_id = s.id " +
 				"JOIN tasks t ON s.task = t.id " +
 				"LEFT JOIN ranged r ON t.id = r.task " +
 				"JOIN responses res ON res.subtask = e.subtask_id " +
 				"GROUP BY type, sid, est, ml, start, finish, p, conf;)";
 		
 		PreparedStatement preparedStatement;
 		
 		Collection<Result> results = new ArrayList<Result>();
 		
 	    try {
 	    	preparedStatement = DbAdaptor.connect().prepareStatement(sql);
 	    	preparedStatement.setInt(1, taskId);
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
 				int s = rs.getInt("subtask_id");
 				int type = rs.getInt("type");
 				String e = rs.getString("estimate");
 				results.add(new Result(mapSubTask(rs, type), mapResponse(e, type)));
 			}
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	
 	private static SubTask mapSubTask(ResultSet rs, int type) throws SQLException {
 		SubTask s = null;
 		int taskAccuracy = rs.getInt("acc");
 		int id = rs.getInt("sid");
 		int responses = rs.getInt("c");
 		int maxLabels = rs.getInt("ml");
 		String finish = rs.getString("finish");
 		String start = rs.getString("start");
 		float precision = rs.getFloat("p");
 		String fileName = rs.getString("f");
 		switch(type){
 		case 1:
 			s = new BinarySubTask(id, taskAccuracy,
 					responses, maxLabels);
 			break;
 		case 2:
 			s = new MultiValueSubTask(id, taskAccuracy, responses, 
 					maxLabels, Integer.parseInt(finish));
 			break;
 		case 3:
 			s = ContinuousSubTask.makeSubtask(id, taskAccuracy, responses, 
 					maxLabels, start, finish, precision);
 			break;
 		}
 		s.addFileName(fileName);
 		return s;
 	}
 
 	public static void addEstimate(Estimate est, int id) {
 		String query = "INSERT INTO estimates VALUES (DEFAULT,?,?,?,?)";
 		
 		PreparedStatement preparedStatement;
         
 		try {
 	    	preparedStatement = DbAdaptor.connect().prepareStatement(query);
 			preparedStatement.setInt(1, id);
 			preparedStatement.setString(2, est.getR().serialise());
 	    	preparedStatement.setFloat(3, (float) est.getConfidence());
 	    	preparedStatement.setInt(4, est.getFrequency());
 			preparedStatement.execute();
 	    }	    catch (ClassNotFoundException e) {
 	    	System.err.println("Error connecting to DB on check finished: PSQL driver not present");
 	    	e.printStackTrace();
 	    } catch (SQLException e) {
 	      	System.err.println("SQL Error on check finished");
 	      	e.printStackTrace();
 	    }
 	}
 	
 	private static Response mapResponse (String s, int type){
 		Response r = null;
 		try {
 			switch (type){
 			case 1:
 				r = new BinaryR(s);
 				break;
 			case 2:
 				r = new MultiValueR(s);
 				break;
 			case 3:
 				r = new ContinuousR(s);
 				break;
 			}
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return r;
 	}
 
 	public static Collection<Estimate> getBinaryEstimates(int id) {
 		return getEstimates(id, 1);
 	}
 
 }
