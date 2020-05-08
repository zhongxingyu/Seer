 package didproject;
 
 import java.sql.*;
 import java.util.*;
 
 //import DBaccess;
 
 //******************************************************************************
 class DBManager {
 
 	private String host, port, user, password, dbname;
 	int n;
 
 	public ArrayList<String> getIds(String tblName, String tblID) {
 		ArrayList<String> result = new ArrayList<String>();
 		ResultSet rs;
 		try {
 			DBaccess.connect(host, port, user, password, dbname);
 			rs = DBaccess.retrieve("SELECT `" + tblID + "` FROM `" + tblName
 					+ "`");
 			while (rs.next()) {
 				result.add(rs.getString(tblID));
 			}
 		} catch (Exception e) {
 		} finally {
 			DBaccess.disconnect();
 			return result;
 		}
 
 	}
 
 	public int exists(String returnField, String tblName, String field,
 			String value) {
 		int id = -1;
 
 		ResultSet rs;
 		try {
 			DBaccess.connect(host, port, user, password, dbname);
 			rs = DBaccess.retrieve("SELECT `" + returnField + "` FROM `"
 					+ tblName + "` WHERE " + field + "='" + value + "'");
 			if (!rs.first()) {
 				// System.err.println("<exists> ERROR: No results.");
 				id = -1;
 			} else {
 				id = Integer.parseInt(rs.getString(returnField));
 			}
 		} catch (Exception e) {
 			System.err.println("Error parsing ID from <exists>");
 		} finally {
 			DBaccess.disconnect();
 			return id;
 		}
 	}
 
 	public String returnStringField(String returnField, String tblName,
 			String field, String value) {
 		String id = "";
 
 		ResultSet rs;
 		try {
 			DBaccess.connect(host, port, user, password, dbname);
 			rs = DBaccess.retrieve("SELECT `" + returnField + "` FROM `"
 					+ tblName + "` WHERE " + field + "='" + value + "'");
 			if (!rs.first()) {
 				// System.err.println("<exists> ERROR: No results.");
 				id = "";
 			} else {
 				id = rs.getString(returnField);
 			}
 		} catch (Exception e) {
 			System.err.println("Error parsing ID from <exists>");
 		} finally {
 			DBaccess.disconnect();
 			return id;
 		}
 	}
 
 	public void updateEpisode(int castawayID, String field, String value) {
 		String q = "UPDATE episode SET " + field + " = " + value
 				+ " WHERE castawayID = '" + castawayID + "'";
 		DBaccess.connect(host, port, user, password, dbname);
 		DBaccess.update(q);
 		System.err.println("update Age per episode: " + q);
 		DBaccess.disconnect();
 	}
 
 	public void addClassifiedIn(int castawayID, int categoryID) {
 		DBaccess.connect(host, port, user, password, dbname);
 		DBaccess.update("INSERT INTO classifiedIn (castawayID,categoryID) VALUES ("
 				+ castawayID + "," + categoryID + ")");
 		DBaccess.disconnect();
 	}
 	
 	public void addGenreOf(int genreID, int recordID) {
 		DBaccess.connect(host, port, user, password, dbname);
 		DBaccess.update("INSERT INTO recordOf (genreID,recordID) VALUES ("
 				+ genreID + "," + recordID + ")");
 		DBaccess.disconnect();
 	}
 
 	public void addWorksAs(int castawayID, int occupationID) {
 		DBaccess.connect(host, port, user, password, dbname);
 		DBaccess.update("INSERT INTO worksAs (castawayID,occupationID) VALUES ("
 				+ castawayID + "," + occupationID + ")");
 		DBaccess.disconnect();
 	}
 
 	public void addCategory(String name) {
 		DBaccess.connect(host, port, user, password, dbname);
 		DBaccess.update("INSERT INTO category (name) VALUES ('" + name + "')");
 		DBaccess.disconnect();
 	}
 
 	public void addGenre(String name) {
 		DBaccess.connect(host, port, user, password, dbname);
 		DBaccess.update("INSERT INTO genre (name) VALUES ('" + name + "')");
 		DBaccess.disconnect();
 	}
 	
 	public void addOccupation(String name) {
 		DBaccess.connect(host, port, user, password, dbname);
 		DBaccess.update("INSERT INTO occupation (name) VALUES ('" + name + "')");
 		DBaccess.disconnect();
 	}
 
 	public void flagCastaway(int id, String field, int value) {
 		String q = "UPDATE castaway SET " + field + " = " + value
 				+ " WHERE castawayID = '" + id + "'";
 		DBaccess.connect(host, port, user, password, dbname);
 		DBaccess.update(q);
 		System.err.println("UPDATE: " + q);
 		DBaccess.disconnect();
 	}
 
 	
 	public void updateRecord(int id, String releasedOn, String artistURI,
 			String songURI, String artistComment, String gender,
 			double genderRatio, String categories_record,
 			String categories_artist, int bound) {
 		String q = "UPDATE record SET releasedOn = " + releasedOn
 				+ ", artistURI=" + artistURI + ", songURI=" + songURI
 				+ ", artistComment=" + artistComment + ", gender=" + gender
 				+ ",genderRatio=" + genderRatio + ", categories_record="
 				+ categories_record + ", categories_artist="
 				+ categories_artist + ", bound = "+bound+" WHERE recordID = '" + id + "'";
 		DBaccess.connect(host, port, user, password, dbname);
 		DBaccess.update(q);
 		System.err.println("UPDATE record "+id+":" + q);
 		DBaccess.disconnect();
 	}
 
 	public void addDoBCastaway(int id, String field, int value) {
 		String q = "UPDATE castaway SET " + field + " = " + value
 				+ " WHERE castawayID = '" + id + "'";
 		DBaccess.connect(host, port, user, password, dbname);
 		DBaccess.update(q);
 		DBaccess.disconnect();
 	}
 
 	public ArrayList<String> getCastaway(String tblName, String id) {
 		ArrayList<String> result = new ArrayList<String>();
 		ResultSet rs;
 
 		try {
 			DBaccess.connect(host, port, user, password, dbname);
 			rs = DBaccess.retrieve("SELECT * FROM `" + tblName + "` WHERE `"
 					+ tblName + "ID` = '" + id + "'");
 
 			if (!rs.first()) {
 				System.err.println("TS_ERROR: Castaway does not exist.");
 				return null;
 			}
 
 			result.add(rs.getString("castawayID"));
 			result.add(rs.getString("name"));
 			result.add(rs.getString("link"));
 			result.add(rs.getString("gender"));
 			result.add(rs.getString("occupation"));
 
 		} catch (Exception e) {
 			System.err.println("ERROR: Get Castaway e.getm: " + e.getMessage());
 		} finally {
 			DBaccess.disconnect();
 			return result;
 		}
 
 	}
 
 	public ArrayList<String> getEpisode(String tblName, String id) {
 		ArrayList<String> result = new ArrayList<String>();
 		ResultSet rs;
 
 		try {
 			DBaccess.connect(host, port, user, password, dbname);
 			rs = DBaccess.retrieve("SELECT * FROM `" + tblName + "` WHERE `"
 					+ tblName + "ID` = '" + id + "'");
 
 			if (!rs.first()) {
 				System.err.println("TS_ERROR: Episode does not exist.");
 				return null;
 			}
 
 			result.add(rs.getString("episodeID"));
 			result.add(rs.getString("castawayID"));
 			result.add(rs.getString("luxuryID"));
 			result.add(rs.getString("dateOfBroadcast"));
 			result.add(rs.getString("age"));
 			result.add(rs.getString("occupations"));
 
 		} catch (Exception e) {
 			System.err.println("ERROR: Get Episode e.getm: " + e.getMessage());
 		} finally {
 			DBaccess.disconnect();
 			return result;
 		}
 
 	}
 
 	public ArrayList<String> getRecord(String tblName, String id) {
 		ArrayList<String> result = new ArrayList<String>();
 		ResultSet rs;
 
 		try {
 			DBaccess.connect(host, port, user, password, dbname);
 			rs = DBaccess.retrieve("SELECT * FROM `" + tblName + "` WHERE `"
 					+ tblName + "ID` = '" + id + "'");
 
 			if (!rs.first()) {
 				System.err.println("TS_ERROR: Episode does not exist.");
 				return null;
 			}
 
 			result.add(rs.getString("recordID"));
 			result.add(rs.getString("artist"));
 			result.add(rs.getString("title"));
 			result.add(rs.getString("part_of"));
 			result.add(rs.getString("composer"));
 			result.add(rs.getString("releasedOn"));
 			result.add(rs.getString("artistURI"));
 			result.add(rs.getString("songURI"));
 			result.add(rs.getString("artistComment"));
 			result.add(rs.getString("genderRatio"));
			result.add(rs.getString("gender"));
			result.add(rs.getString("categories_record"));
			result.add(rs.getString("categories_artist"));
 			result.add(rs.getString("bound"));
 
 		} catch (Exception e) {
 			System.err.println("ERROR: Get Record e.getm: " + e.getMessage());
 		} finally {
 			DBaccess.disconnect();
 			return result;
 		}
 
 	}
 
 
 
 	// //
 	// // public int size(String tblName) {
 	// try {
 	// DBaccess.connect(host, port, user, password, dbname);
 	// ResultSet rs = DBaccess.retrieve("SELECT COUNT(*) FROM `" + tblName
 	// + "`");
 	// rs.first();
 	// n = Integer.parseInt(rs.getString("COUNT(*)"));
 	// System.err.println("TS____ N= " + n);
 	// } catch (SQLException ex) {
 	// System.err.println("TS_SQL_ERR_INS = " + ex.getMessage());
 	// System.err.println("SELECT COUNT(*) FROM \"" + tblName + "\"");
 	// } catch (NumberFormatException ex) {
 	// System.err.println("TS_SQL_NUMFORMAT_INS = " + ex.getMessage());
 	// } finally {
 	// DBaccess.disconnect();
 	// return n;
 	// }
 	//
 	// }
 
 }
