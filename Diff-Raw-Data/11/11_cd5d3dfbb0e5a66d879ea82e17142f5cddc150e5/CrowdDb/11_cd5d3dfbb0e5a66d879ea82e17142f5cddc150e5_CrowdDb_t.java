 package db;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.ArrayList;
 
 import crowdtrust.AccuracyRecord;
 import crowdtrust.Bee;
 import crowdtrust.BinaryAccuracy;
 import crowdtrust.BinaryResponse;
 import crowdtrust.ContinuousResponse;
 import crowdtrust.MultiValueResponse;
 import crowdtrust.SingleAccuracy;
 import crowdtrust.Account;
 
 import java.io.UnsupportedEncodingException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class CrowdDb {
 
 	/* Adds a response to the response table in the database */
 	public static void addResponse(int account, String string, int subtask) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("INSERT INTO responses (account, subtask, response)");
 		sql.append("VALUES(?, ?, ?)");
 		Connection c;
     try {
       c = DbAdaptor.connect();
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  e.printStackTrace();
 	  return;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  e.printStackTrace();
 	  return;
     }
 		try {
 			PreparedStatement preparedStatement = c.prepareStatement(sql.toString());
 			preparedStatement.setInt(1, account);
 			preparedStatement.setInt(2, subtask);
 			preparedStatement.setString(3, string);
 			preparedStatement.execute();
 			c.close();
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/* gets binary accuracy given an account id */
 	public static BinaryAccuracy getBinaryAccuracy(int id) {
 		PreparedStatement preparedStatement;
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT truePositive, trueNegative, numPositive, numNegative FROM binaryaccuracies\n");
 		sql.append("WHERE account = ?");
     try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return null;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return null;
     }
 		try {
 			preparedStatement.setInt(1, id);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			BinaryAccuracy accuracy;
 			if(resultSet.next())
 				accuracy = mapBinaryAccuracy(resultSet);
 			else{
 				accuracy = new BinaryAccuracy(0.5, 0.5, 0, 0);
 			  insertBinaryAccuracy(id, accuracy);
 			}
 			return accuracy;
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/* adds binary accuracy given the accuracy and account id */
 	public static boolean insertBinaryAccuracy(int id, BinaryAccuracy accuracy) {
 		PreparedStatement preparedStatement;
 		String sql = "INSERT INTO binaryaccuracies (account, truePositive, " +
 				"trueNegative, numPositive, numNegative) " +
 				"VALUES(?,?,?,?,?)";
 		
 		try {
 			preparedStatement = DbAdaptor.connect().prepareStatement(sql);
 			double truePositive = accuracy.getTruePositive();
 			double trueNegative = accuracy.getTrueNegative();
       if(truePositive < 0 || truePositive > 1) {
         return false;
       }
       if(trueNegative < 0 || trueNegative > 1) {
         return false;
       }
 			int positiveN = accuracy.getPositiveN();
 			int negativeN = accuracy.getNegativeN();
 			preparedStatement.setInt(1, id);
 			preparedStatement.setDouble(2, truePositive);
 			preparedStatement.setDouble(3, trueNegative);
 			preparedStatement.setInt(4, positiveN);
 			preparedStatement.setInt(5, negativeN);
 			preparedStatement.execute();
       return true;
 		} catch (SQLException e1) {
 			return false;
 		} catch (ClassNotFoundException e1) {
 			return false;
 		}		
 	}
 
 	/* gets the account ids for a given subtask id */
 	public static Collection<Bee> getAnnotators(int subtask_id) {
 		PreparedStatement preparedStatement;
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT account FROM responses WHERE subtask = ?");
     try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return null;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return null;
     }
 		try {
 			preparedStatement.setInt(1, subtask_id);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			Collection<Bee> bees = new ArrayList<Bee>();
 			int account;
 			while(resultSet.next()) {
 				account = resultSet.getInt(1);
 				bees.add(new Bee(account));
 			}
 			return bees;
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/* gets binary accuracies for a collection of account ids */
 	public static Collection<AccuracyRecord> getBinaryAccuracies(Collection<Bee> annotators) {
 		Collection<AccuracyRecord> records = new ArrayList<AccuracyRecord>();
 		for(Bee b : annotators) {
 			int id = b.getId();
 			BinaryAccuracy accuracy = getBinaryAccuracy(id);
 			AccuracyRecord record = new AccuracyRecord(b, accuracy);
 			records.add(record);
 		}
 		return records;
 	}
 
 	/* gets multivalue accuracy for a given account id */
 	public static SingleAccuracy getMultiValueAccuracy(int id) {
 		PreparedStatement preparedStatement;
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT accuracy, total FROM multivalueaccuracies WHERE account = ?");
     try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return null;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return null;
     }
 		try {
 			preparedStatement.setInt(1, id);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			SingleAccuracy singleAccuracy = null;
 			double accuracy = 0.5;
 			if(resultSet.next()){
 				accuracy = resultSet.getDouble("accuracy");
 				int total = resultSet.getInt("total");
 				singleAccuracy = new SingleAccuracy(accuracy, total);
 			}else{
 				singleAccuracy = new SingleAccuracy(accuracy, 0);
 				insertMVAccuracy(id, singleAccuracy);
 			}
 			
 			return singleAccuracy;
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/* inserts a multivalue accuracy given account id and accuracy. */
 	public static boolean insertMVAccuracy(int id, SingleAccuracy accuracy) {
 		PreparedStatement preparedStatement;
 		String sql = "INSERT INTO multivalueaccuracies (account, accuracy, total) " +
 				"VALUES(?,?,?)";
 		
 		try {
 			preparedStatement = DbAdaptor.connect().prepareStatement(sql);
 			double a = accuracy.getAccuracy();
 			if(a < 0 || a > 1) {
 				return false;
 			}
 			preparedStatement.setInt(1, id);
 			preparedStatement.setDouble(2, a);
 			preparedStatement.setInt(3, accuracy.getN());
 			preparedStatement.execute();
       return true;
 		} catch (SQLException e1) {
 			return false;
 		} catch (ClassNotFoundException e1) {
 			return false;
 		}	
 		
 	}
 	
 	/* gets continuous accuracy given account id */
 	public static SingleAccuracy getContinuousAccuracy(int id) {
 		PreparedStatement preparedStatement;
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT accuracy, total FROM continuousaccuracies WHERE account = ?");
     try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return null;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return null;
     }
 		try {
 			preparedStatement.setInt(1, id);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			SingleAccuracy singleAccuracy = null;
 			double accuracy = 0.5;
 			if(resultSet.next()){
 				accuracy = resultSet.getDouble("accuracy");
 				int total = resultSet.getInt("total");
 				singleAccuracy = new SingleAccuracy(accuracy, total);
 			}else{
 				singleAccuracy = new SingleAccuracy(accuracy, 0);
 				insertContinuousAccuracy(id, singleAccuracy);
 			}
 			
 			return singleAccuracy;
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/* inserts continuous accuracy given account id and accuracy */
 	public static boolean insertContinuousAccuracy(int id, SingleAccuracy accuracy) {
 		PreparedStatement preparedStatement;
 		String sql = "INSERT INTO continuousaccuracies (account, accuracy, total) " +
 				"VALUES(?,?,?)";
 		
 		try {
 			preparedStatement = DbAdaptor.connect().prepareStatement(sql);
 			double a = accuracy.getAccuracy();
 			if(a < 0 || a > 1) {
 				return false;
 			}
 			preparedStatement.setInt(1, id);
 			preparedStatement.setDouble(2, a);
 			preparedStatement.setInt(3, accuracy.getN());
 			preparedStatement.execute();
       return true;
 		} catch (SQLException e1) {
 			return false;
 		} catch (ClassNotFoundException e1) {
 			return false;
 		}	
 		
 	}
 
 	/* update a collection of binary accuracies given a collection of account ids and accuracies */
 	public static boolean updateBinaryAccuracies(Collection<AccuracyRecord> accuracies) {
 		PreparedStatement preparedStatement;
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE binaryaccuracies\n");
 		sql.append("SET truePositive = ?, trueNegative = ?, numPositive = ?, numNegative = ?\n");
 		sql.append("WHERE account = ?");
     try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return false;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return false;
     }
 		try {
 			for(AccuracyRecord record : accuracies) {
 				Bee bee = record.getBee();
 				int id = bee.getId();
 				BinaryAccuracy accuracy = (BinaryAccuracy) record.getAccuracy();
 				double truePositive = accuracy.getTruePositive();
 				double trueNegative = accuracy.getTrueNegative();
         if(truePositive < 0 || truePositive > 1) {
           return false;
         }
         if(trueNegative < 0 || trueNegative > 1) {
           return false;
         }
 				int positiveN = accuracy.getPositiveN();
 				int negativeN = accuracy.getNegativeN();
 				preparedStatement.setDouble(1, truePositive);
 				preparedStatement.setDouble(2, trueNegative);
 				preparedStatement.setInt(3, positiveN);
 				preparedStatement.setInt(4, negativeN);
 				preparedStatement.setInt(5, id);
 				preparedStatement.executeUpdate();
 			}
       return true;
 		}
 		catch (SQLException e) {
 			return false;
 		}
 	}
 
 	/* gets a collection of multivalue accuracies given a collection of account ids */
 	public static Collection<AccuracyRecord> getMultiValueAccuracies(Collection<Bee> annotators) {
 		Collection<AccuracyRecord> records = new ArrayList<AccuracyRecord>();
 		for(Bee bee : annotators) {
 			int id = bee.getId();
 			SingleAccuracy accuracy = getMultiValueAccuracy(id);
 			AccuracyRecord record = new AccuracyRecord(bee, accuracy);
 			records.add(record);
 		}
 		return records;	
 	}
 
 	/* updates multivalue accuracies given a collection of account ids and accuracies */
 	public static boolean updateMultiValueAccuracies(Collection<AccuracyRecord> accuracies) {
 		PreparedStatement preparedStatement;
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE multivalueaccuracies\n");
 		sql.append("SET accuracy = ?, total = ?\n");
 		sql.append("WHERE account = ?");
     try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return false;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return false;
     }
 		try {
 			for(AccuracyRecord record : accuracies) {
 				Bee bee = record.getBee();
 				int id = bee.getId();
 				SingleAccuracy accuracy = (SingleAccuracy) record.getAccuracy();
 				double a = accuracy.getAccuracy();
 				if (a < 0 || a > 1) {
 					return false;
 				} 
 				preparedStatement.setDouble(1, a);
 				preparedStatement.setInt(1, accuracy.getN());
 				preparedStatement.setInt(3, id);
 				preparedStatement.executeUpdate();
 			}
       return true;
 		}
 		catch (SQLException e) {
 			return false;
 		}
 	}
 
 	/* gets a collection continuous accuracies given a collection of account ids */
 	public static Collection<AccuracyRecord> getContinuousAccuracies(Collection<Bee> annotators) {
 		Collection<AccuracyRecord> records = new ArrayList<AccuracyRecord>();
 		for(Bee bee : annotators) {
 			int id = bee.getId();
 			SingleAccuracy accuracy = getContinuousAccuracy(id);
 			AccuracyRecord record = new AccuracyRecord(bee, accuracy);
 			records.add(record);
 		}
 		return records;	
 	}
 
 	/* updates continuous accuracies given a collection of account ids and accuracies */
 	public static boolean updateContinuousAccuracies(Collection<AccuracyRecord> accuracies) {
 		PreparedStatement preparedStatement;
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE continuousaccuracies\n");
 		sql.append("SET accuracy = ?, total = ?\n");
 		sql.append("WHERE account = ?");
     try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return false;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return false;
     }
 		try {
 			for(AccuracyRecord record : accuracies) {
 				Bee bee = record.getBee();
 				int id = bee.getId();
 				SingleAccuracy accuracy = (SingleAccuracy) record.getAccuracy();
 				double a = accuracy.getAccuracy();
 				if (a < 0 || a > 1) {
 					return false;
 				} 
 				preparedStatement.setDouble(1, a);
				preparedStatement.setInt(2, accuracy.getN());
				preparedStatement.setInt(3, id);
 				preparedStatement.executeUpdate();
 			}
       return true;
 		}
 		catch (SQLException e) {
 			return false;
 		}
 	}
 
 	public static void updateBinaryExperts(Collection<Bee> experts) {
 		updateExperts(experts, 1);
 	}
 
 	public static void updateContinuousExperts(Collection<Bee> experts) {
 		updateExperts(experts, 2);
 	}
 
 	public static void updateMultiValueExperts(Collection<Bee> experts) {
 		updateExperts(experts, 3);		
 	}
 
 	public static void updateBinaryBots(Collection<Bee> bots) {
 		updateBots(bots, 1);		
 	}
 
 	public static void updateContinuousBots(Collection<Bee> bots) {
 		updateBots(bots, 2);
 	}
 
 	public static void updateMultiValueBots(Collection<Bee> bots) {
 		updateBots(bots, 3);
 	}
 
 	/* maps a resultset to a binary accuracy */
 	private static BinaryAccuracy mapBinaryAccuracy(ResultSet resultSet) {
 		try {
 		double truePositive = resultSet.getDouble("truePositive");
 		double trueNegative = resultSet.getDouble("trueNegative");
 		int numPositive = resultSet.getInt("numPositive");
 		int numNegative = resultSet.getInt("numNegative");
 		BinaryAccuracy accuracy = new BinaryAccuracy(truePositive, trueNegative, numPositive, numNegative);
 		return accuracy;
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/* updates the experts list given a collection of experts and the annotation type */
 	private static void updateExperts(Collection<Bee> experts, int type) {
 		PreparedStatement preparedStatement;
 		StringBuilder sql = new StringBuilder();
 		sql.append("INSERT INTO experts (accounts, type) VALUES (?, ?)");
     try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return;
     }
 		try {
 			Bee[] bees = (Bee []) experts.toArray(new Bee[experts.size()]);
 			for(int i = 0; i < bees.length; i++) {
 				Bee bee = bees[i];
 				int id = bee.getId();
 				preparedStatement.setInt(1, id);
 				preparedStatement.setInt(2, type);
 				preparedStatement.executeUpdate();
 			}		
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/* updates bot list given a collection of bots and annotation types */
 	private static void updateBots(Collection<Bee> bots, int type) {
 		PreparedStatement preparedStatement;
 		StringBuilder sql = new StringBuilder();
 		sql.append("INSERT INTO bots (accounts, type) VALUES (?, ?)");
     try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return;
     }
 		try {
 			Bee[] bees = (Bee []) bots.toArray(new Bee[bots.size()]);
 			for(int i = 0; i < bees.length; i++) {
 				Bee bee = bees[i];
 				int id = bee.getId();
 				preparedStatement.setInt(1, id);
 				preparedStatement.setInt(2, type);
 				preparedStatement.executeUpdate();
 			}		
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/* returns a list of all of the experts */
 	public static List<Account> getAllExperts() {
 		PreparedStatement preparedStatement;
 		String sql = "SELECT account FROM experts";
 		try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql);
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return null;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return null;
     }
 		try {
 			ResultSet rs = preparedStatement.executeQuery();
 			List<Account> experts = new ArrayList<Account>();
 			while(rs.next()) {
 				int id = rs.getInt("id");
 				experts.add(LoginDb.getAccount(id));
 			}
 			return experts; 
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/* returns a list of all of the bots */
 	public static List<Account> getAllBots() {
 		PreparedStatement preparedStatement;
 		String sql = "SELECT account FROM bots";
 		try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql);
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return null;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return null;
     }
 		try {
 			ResultSet rs = preparedStatement.executeQuery();
 			List<Account> experts = new ArrayList<Account>();
 			while(rs.next()) {
 				int id = rs.getInt("id");
 				experts.add(LoginDb.getAccount(id));
 			}
 			return experts; 
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/* returns a collection of all of the binary annotators for a given subtask */
 	public static Collection<AccuracyRecord> getBinaryAnnotators(int id) {
 		PreparedStatement preparedStatement;
 		String sql = "SELECT responses.account, response, truePositive, " +
 				"trueNegative, numPositive, numNegative " +
 				"FROM responses JOIN binaryaccuracies " +
 				"ON responses.account = binaryaccuracies.account " +
 				"WHERE subtask = ?";
     try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql);
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return null;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return null;
     }
 		try {
 			preparedStatement.setInt(1, id);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			Collection<AccuracyRecord> as = new ArrayList<AccuracyRecord>();
 			int account;
 			while(resultSet.next()) {
 				account = resultSet.getInt(1);
 				
 				BinaryAccuracy accuracy;
 				accuracy = mapBinaryAccuracy(resultSet);
 				AccuracyRecord record = new AccuracyRecord(new Bee(account), accuracy);
 				record.setMostRecent(new BinaryResponse(resultSet.getString("response")));
 				as.add(record);
 			}
 			return as;
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/* returns a list of all of the multi value annotators for a given subtask */
 	public static Collection<AccuracyRecord> getMultiValueAnnotators(int id) {
 		PreparedStatement preparedStatement;
 		String sql = "SELECT responses.account, response, accuracy, total " +
 				"FROM responses JOIN multivalueaccuracies " +
 				"ON responses.account = multivalueaccuracies.account " +
 				"WHERE subtask = ?";
     try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql);
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return null;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return null;
     }
 		try {
 			preparedStatement.setInt(1, id);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			Collection<AccuracyRecord> as = new ArrayList<AccuracyRecord>();
 			int account;
 			while(resultSet.next()) {
 				account = resultSet.getInt(1);
 				
 				SingleAccuracy accuracy;
 				accuracy = new SingleAccuracy(resultSet.getFloat("accuracy"), 
 						resultSet.getInt("total"));
 				AccuracyRecord record = new AccuracyRecord(new Bee(account), accuracy);
 				record.setMostRecent(new MultiValueResponse(resultSet.getString("response")));
 				as.add(record);
 			}
 			return as;
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		catch(NumberFormatException ne){
 			ne.printStackTrace();
 		}
 		catch(UnsupportedEncodingException ue){
 			ue.printStackTrace();
 		}
 		return null;
 	}
 	
 	/* returns a collection of all continuous annotators for a given subtask */
 	public static Collection<AccuracyRecord> getContinuousAnnotators(int id) {
 		PreparedStatement preparedStatement;
 		String sql = "SELECT responses.account, response, accuracy, total " +
 				"FROM responses JOIN continuousaccuracies " +
 				"ON responses.account = continuousaccuracies.account " +
 				"WHERE subtask = ?";
     try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql);
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return null;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return null;
     }
 		try {
 			preparedStatement.setInt(1, id);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			Collection<AccuracyRecord> as = new ArrayList<AccuracyRecord>();
 			int account;
 			while(resultSet.next()) {
 				account = resultSet.getInt(1);
 				
 				SingleAccuracy accuracy;
 				accuracy = new SingleAccuracy(resultSet.getFloat("accuracy"), 
 						resultSet.getInt("total"));
 				AccuracyRecord record = new AccuracyRecord(new Bee(account), accuracy);
 				record.setMostRecent(new ContinuousResponse(resultSet.getString("response")));
 				as.add(record);
 			}
 			return as;
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/* checks all continuous task experts */
 	public static boolean checkContinuousAccuraciesForExperts() {
 		PreparedStatement preparedStatement;
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT continuousaccuracies.accuracy ");
 		sql.append("FROM continuousaccuracies JOIN experts ");
 		sql.append("ON experts.account = continuousaccuracies.account ");
 		sql.append("WHERE experts.type = 3");
 		try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return false;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return false;
     }
 		try {
 			ResultSet resultSet = preparedStatement.executeQuery();
 			if(resultSet.next()) {
 				while(resultSet.next()) {
 					double accuracy = resultSet.getDouble(1);
 					if(accuracy < 0.85) {
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/* checks all category task experts */
 	public static boolean checkMultiValueAccuraciesForExperts() {
 		PreparedStatement preparedStatement;
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT multivalueaccuracies.accuracy ");
 		sql.append("FROM multivalueaccuracies JOIN experts ");
 		sql.append("ON experts.account = multivalueaccuracies.account ");
 		sql.append("WHERE experts.type = 2");
 		try {
       preparedStatement = DbAdaptor.connect().prepareStatement(sql.toString());
     }
     catch (ClassNotFoundException e) {
   	  System.err.println("Error connecting to DB on Crowd: PSQL driver not present");
   	  return false;
     } catch (SQLException e) {
   	  System.err.println("SQL Error on Crowd");
   	  return false;
     }
 		try {
 			ResultSet resultSet = preparedStatement.executeQuery();
 			if(resultSet.next()) {
 				while(resultSet.next()) {
 					double accuracy = resultSet.getDouble(1);
 					if(accuracy < 0.85) {
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 }
