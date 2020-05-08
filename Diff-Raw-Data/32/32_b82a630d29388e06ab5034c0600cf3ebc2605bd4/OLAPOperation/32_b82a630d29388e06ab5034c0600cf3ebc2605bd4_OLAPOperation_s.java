 package ca.awesome;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 
 public class OLAPOperation {
 	private ArrayList<String> queryResult;
 
 	public OLAPOperation() {
 		this.queryResult = new ArrayList<String>();
 	}
 
 	public void setQueryResult(ArrayList<String> result) {
 		queryResult = result;
 	}
 
 	public ArrayList<String> getQueryResult() {
 		return queryResult;
 	}
 	
 	public boolean setCubeTable (DatabaseConnection connection) {
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
			statement.executeUpdate(
				"drop table temp"
			);
			statement.executeUpdate(
				"drop table OLAPcube"
			);
 			statement.executeUpdate(
 				"create table temp as select R.patient_name, R.test_type,"
 				+" to_char(R.test_date, 'yyyy') as year,"
 				+" to_char(R.test_date, 'mm') as month,"
 				+" to_char(R.test_date, 'ww') as week,"
 				+" I.image_id from radiology_record R,"
 				+" pacs_images I where R.record_id = I.record_id"
 			);
 			statement.executeUpdate(
 				"create table OLAPcube as"
 				+" select patient_name, test_type, year, month, week,"
 				+" count(image_id) as image_count from temp"
 				+" group by cube (patient_name, test_type, year, month, week)"
 			);
 			connection.commit();
 			return true;
 		} catch (SQLException e) {
 			connection.rollback();
 			throw new RuntimeException("failed to setCubeTable()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public int getTotal (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select image_count from OLAPcube"
 				+" where patient_name is NULL and test_type is NULL"
 				+" and year is NULL and month is NULL and week is NULL"
 			);
 			results.next();
 			int totalCount = results.getInt(1);
 			return totalCount;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to getTotal()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byPatient (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select patient_name, image_count from OLAPcube"
 				+" where patient_name is not NULL and test_type is NULL"
 				+" and year is NULL and month is NULL and week is NULL"
 				+" order by patient_name"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(" ");
 				//month
 				table.add(" ");
 				//week
 				table.add(" ");
 				//patient
 				table.add(results.getString(1));
 				//test type
 				table.add(" ");
 				//image count
 				table.add(String.valueOf(results.getInt(2)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byPatient()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byPatientYear (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select year, patient_name, image_count from OLAPcube"
 				+" where patient_name is not NULL and test_type is NULL"
 				+" and year is not NULL and month is NULL and week is NULL"
 				+" order by year, patient_name"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(String.valueOf(results.getInt(1)));
 				//month
 				table.add(" ");
 				//week
 				table.add(" ");
 				//patient
 				table.add(results.getString(2));
 				//test type
 				table.add(" ");
 				//image count
 				table.add(String.valueOf(results.getInt(3)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byPatientYear()", e);
 		} finally {
 			connection.close();
 		}
 	}
 	
 	public ArrayList<String> byPatientMonth (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select year, month, patient_name, image_count from OLAPcube"
 				+" where patient_name is not NULL and test_type is NULL"
 				+" and year is not NULL and month is not NULL"
 				+" and week is NULL order by year, month, patient_name"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(String.valueOf(results.getInt(1)));
 				//month
 				table.add(String.valueOf(results.getInt(2)));
 				//week
 				table.add(" ");
 				//patient
 				table.add(results.getString(3));
 				//test type
 				table.add(" ");
 				//image count
 				table.add(String.valueOf(results.getInt(4)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byPatientMonth()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byPatientWeek (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select year, month, week, patient_name, image_count"
 				+" from OLAPcube where patient_name is not NULL and"
 				+" test_type is NULL and year is not NULL and"
 				+" month is not NULL and week is not NULL"
 				+" order by year, month, week, patient_name"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(String.valueOf(results.getInt(1)));
 				//month
 				table.add(String.valueOf(results.getInt(2)));
 				//week
 				table.add(String.valueOf(results.getInt(3)));
 				//patient
 				table.add(results.getString(4));
 				//test type
 				table.add(" ");
 				//image count
 				table.add(String.valueOf(results.getInt(5)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byPatientWeek()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byTestType (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select test_type, image_count from OLAPcube"
 				+" where patient_name is NULL and test_type is not NULL"
 				+" and year is NULL and month is NULL and week is NULL"
 				+" order by test_type"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(" ");
 				//month
 				table.add(" ");
 				//week
 				table.add(" ");
 				//patient name				
 				table.add(" ");
 				//test type
 				table.add(results.getString(1));
 				//image count
 				table.add(String.valueOf(results.getInt(2)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byTestType()", e);
 		} finally {
 			connection.close();
 		}
 	}
 	
 	public ArrayList<String> byTestTypeYear (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select year, test_type, image_count from OLAPcube"
 				+" where patient_name is NULL and test_type is not NULL"
 				+" and year is not NULL and month is NULL and week is NULL"
 				+" order by year, test_type"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(String.valueOf(results.getInt(1)));
 				//month
 				table.add(" ");
 				//week
 				table.add(" ");
 				//patient name				
 				table.add(" ");
 				//test type
 				table.add(results.getString(2));
 				//image count
 				table.add(String.valueOf(results.getInt(3)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byTestTypeYear()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byTestTypeMonth (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select year, month, test_type, image_count from OLAPcube"
 				+" where patient_name is NULL and test_type is not NULL"
 				+" and year is not NULL and month is not NULL"
 				+" and week is NULL order by year, month, test_type"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(String.valueOf(results.getInt(1)));
 				//month
 				table.add(String.valueOf(results.getInt(2)));
 				//week
 				table.add(" ");
 				//patient name				
 				table.add(" ");
 				//test type
 				table.add(results.getString(3));
 				//image count
 				table.add(String.valueOf(results.getInt(4)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byTestTypeMonth()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byTestTypeWeek (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select year, month, week, test_type, image_count"
 				+" from OLAPcube where patient_name is NULL and"
 				+" test_type is not NULL and year is not NULL and"
 				+" month is not NULL and week is not NULL"
 				+" order by year, month, week, test_type"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(String.valueOf(results.getInt(1)));
 				//month
 				table.add(String.valueOf(results.getInt(2)));
 				//week
 				table.add(String.valueOf(results.getInt(3)));
 				//patient name				
 				table.add(" ");
 				//test type
 				table.add(results.getString(4));
 				//image count
 				table.add(String.valueOf(results.getInt(5)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byTestTypeWeek()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byBoth (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select patient_name, test_type, image_count from OLAPcube"
 				+" where year is NULL and month is NULL and week is NULL"
 				+" and patient_name is not NULL and test_type is not NULL"
 				+" order by patient_name, test_type"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(" ");				
 				//month
 				table.add(" ");
 				//week
 				table.add(" ");
 				//patient name
 				table.add(results.getString(1));
 				//test type
 				table.add(results.getString(2));
 				//image count
 				table.add(String.valueOf(results.getInt(3)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byBoth()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byBothYear (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select year, patient_name, test_type, image_count"
 				+" from OLAPcube where year is not NULL and month is NULL"
 				+" and week is NULL and patient_name is not NULL and"
 				+" test_type is not NULL order by year, patient_name, test_type"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(String.valueOf(results.getInt(1)));
 				//month
 				table.add(" ");
 				//week
 				table.add(" ");
 				//patient name
 				table.add(results.getString(2));
 				//test type
 				table.add(results.getString(3));
 				//image count
 				table.add(String.valueOf(results.getInt(4)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byBothYear()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byBothMonth (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select year, month, patient_name, test_type, image_count"
 				+" from OLAPcube where year is not NULL and month is not NULL"
 				+" and week is NULL and patient_name is not NULL and"
 				+" test_type is not NULL"
 				+" order by year, month, patient_name, test_type"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(String.valueOf(results.getInt(1)));
 				//month
 				table.add(String.valueOf(results.getInt(2)));
 				//week
 				table.add(" ");
 				//patient name
 				table.add(results.getString(3));
 				//test type
 				table.add(results.getString(4));
 				//image count
 				table.add(String.valueOf(results.getInt(5)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byBothMonth()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byBothWeek (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select year, month, week, patient_name, test_type, image_count"
 				+" from OLAPcube where year is not NULL and month is not NULL"
 				+" and week is not NULL and patient_name is not NULL"
 				+" and test_type is not NULL"
 				+" order by year, month, week, patient_name, test_type"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(String.valueOf(results.getInt(1)));
 				//month
 				table.add(String.valueOf(results.getInt(2)));
 				//week
 				table.add(String.valueOf(results.getInt(3)));
 				//patient name
 				table.add(results.getString(4));
 				//test type
 				table.add(results.getString(5));
 				//image count
 				table.add(String.valueOf(results.getInt(6)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byBothWeek()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byYear (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select year, image_count from OLAPcube"
 				+" where year is not NULL and month is NULL and week is NULL"
 				+" and patient_name is NULL and test_type is NULL order by year"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(String.valueOf(results.getInt(1)));
 				//month
 				table.add(" ");
 				//week
 				table.add(" ");
 				//patient name
 				table.add(" ");
 				//test type
 				table.add(" ");
 				//image count
 				table.add(String.valueOf(results.getInt(2)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byYear()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byMonth (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select year, month, image_count from OLAPcube"
 				+" where year is not NULL and month is not NULL and"
 				+" week is NULL and patient_name is NULL and test_type is NULL"
 				+" order by year, month"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(String.valueOf(results.getInt(1)));
 				//month
 				table.add(String.valueOf(results.getInt(2)));
 				//week
 				table.add(" ");
 				//patient name
 				table.add(" ");
 				//test type
 				table.add(" ");
 				//image count
 				table.add(String.valueOf(results.getInt(3)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byMonth()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	public ArrayList<String> byWeek (DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select year, month, week, image_count from OLAPcube"
 				+" where year is not NULL and month is not NULL and"
 				+" week is not NULL and patient_name is NULL and"
 				+" test_type is NULL order by year, month, week"
 			);
 			ArrayList<String> table = new ArrayList<String>();	
 			
 			while (results != null && results.next()) {
 				//year
 				table.add(String.valueOf(results.getInt(1)));
 				//month
 				table.add(String.valueOf(results.getInt(2)));
 				//week
 				table.add(String.valueOf(results.getInt(3)));
 				//patient name
 				table.add(" ");
 				//test type
 				table.add(" ");
 				//image count
 				table.add(String.valueOf(results.getInt(4)));
 			}
 			return table;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to byWeek()", e);
 		} finally {
 			connection.close();
 		}
 	}
	
 }
 
