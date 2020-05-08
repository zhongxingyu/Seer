 package de.sportschulApp.server.databanker;
 
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import de.sportschulApp.shared.Member;
 
 public class DataBankerMember implements DataBankerMemberInterface {
 	/**
 	 * Legt einen neues Mitglied an.
 	 * 
 	 * @param ein
 	 *            Objekt des Typs Member
 	 * 
 	 * @return "member created" wenn ein neues Mitglied angelegt wurde, "error"
 	 *         wenn das anlegen nicht funktioniert hat und
 	 *         "barcode_id already used", wenn Barcodenummer schon vorhanden
 	 *         ist.
 	 */
 	public String createMember(Member member) {
 		DataBankerConnection dbc = new DataBankerConnection();
 		try {
 			ResultSet rs = null;
 			Statement stmt2 = dbc.getStatement();
 			String query = "SELECT Member_id FROM Member WHERE barcode_id='"
 					+ member.getBarcodeID() + "'";
 
 			rs = stmt2.executeQuery(query);
 			while (rs.next()) {
 				if (rs.getInt(1) > 0) {
 					return "barcode_id already used";
 				}
 			}
 			rs.close();
 			stmt2.close();
 
 			PreparedStatement stmt = dbc
 
 					.getConnection()
 					.prepareStatement(
 							"INSERT INTO Member(barcode_id, forename, surname, zipcode, city, street, phone, mobilephone, fax, email, homepage, birthDay, birthMonth, birthYear, picture, diseases, beltsize, note, trainingunits, course_01, course_02, course_03, course_04, course_05, course_06, course_07, course_08, course_09, course_10, graduation_01, graduation_02, graduation_03, graduation_04, graduation_05, graduation_06, graduation_07, graduation_08, graduation_09, graduation_10) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
 			stmt.setInt(1, member.getBarcodeID());
 			stmt.setString(2, member.getForename());
 			stmt.setString(3, member.getSurname());
 			stmt.setInt(4, member.getZipcode());
 			stmt.setString(5, member.getCity());
 			stmt.setString(6, member.getStreet());
 			stmt.setString(7, member.getPhone());
 			stmt.setString(8, member.getMobilephone());
 			stmt.setString(9, member.getFax());
 			stmt.setString(10, member.getEmail());
 			stmt.setString(11, member.getHomepage());
 			stmt.setString(12, member.getBirthDay());
 			stmt.setString(13, member.getBirthMonth());
 			stmt.setString(14, member.getBirthYear());
 			stmt.setString(15, member.getPicture());
 			stmt.setString(16, member.getDiseases());
 			stmt.setString(17, member.getBeltsize());
 			stmt.setString(18, member.getNote());
 			stmt.setInt(19, member.getTrainingunits());
 
 			// Hinzuf�gen der ArrayList Courses
 			int size = member.getCourses().size();
 			try {
 				for (int i = 0; i < size; i++) {
 					stmt.setInt(i + 20, member.getCourses().get(i));
 				}
 			} catch (IndexOutOfBoundsException e) {
 				System.out.println(e);
 				System.out.println("courses out of bounds");
 			}
 			// Auff�llen mit Nullen, wenn Courses<10
 			if (size < 10) {
 				for (int i = 20 + size; i < 30; i++) {
 					stmt.setInt(i, 0);
 				}
 			}
 
 			// Hinzuf�gen der ArrayList Graduations
 			size = member.getGraduations().size();
 			try {
 				for (int i = 0; i < size; i++) {
 					stmt.setInt(i + 30, member.getGraduations().get(i));
 				}
 			} catch (IndexOutOfBoundsException e) {
 				System.out.println(e);
 				System.out.println("graduations out of bounds");
 			}
 			// Auff�llen mit Nullen, wenn Graduations<10
 			if (size < 10) {
 				for (int i = 30 + size; i < 40; i++) {
 					stmt.setInt(i, 0);
 				}
 			}
 			stmt.executeUpdate();
 
 			dbc.close();
 			stmt.close();
 
 			return "member created";
 
 		} catch (SQLException e) {
 			System.out.println(e);
 			return "error";
 		}
 	}
 
 	/**
 	 * L�scht einen Mitgliedereintrag
 	 * 
 	 * @param memberID
 	 *            eines Mitgliedes
 	 * 
 	 * @return true bei erfolg, false bei scheitern
 	 */
 	public boolean deleteMember(int memberID) {
 
 		DataBankerConnection dbc = new DataBankerConnection();
 		Statement stmt = dbc.getStatement();
 
 		String query = "DELETE FROM Member WHERE Member_id='" + memberID + "'";
 
 		try {
 			stmt.executeUpdate(query);
 			dbc.close();
 			stmt.close();
 			dbc.closeStatement();
 
 		} catch (SQLException e) {
 			System.out.println(e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * �ndert einen Mitgliedereintrag
 	 * 
 	 * @param ein
 	 *            Objekt des Typs Member
 	 * 
 	 * @return "Mitglied gespeichert" wenn Mitglied ge�ndert wurde,
 	 *         "Fehler beim Speichern" wenn das �ndern nicht funktioniert hat
 	 *         und "Barcodenummer schon vorhanden", wenn Barcodenummer schon
 	 *         vorhanden ist.
 	 */
 	public String updateMember(Member member) {
 		// TODO Update �ndern, damit primery key zur�ck gesetzt wird
 
 		DataBankerConnection dbc = new DataBankerConnection();
 		try {
 			ResultSet rs = null;
 			Statement stmt2 = dbc.getStatement();
 			String query = "SELECT Member_id FROM Member WHERE barcode_id='"
 					+ member.getBarcodeID() + "'";
 
 			rs = stmt2.executeQuery(query);
 			while (rs.next()) {
 				if (rs.getInt(1) > 0) {
 					return "Barcodenummer schon vorhanden";
 				}
 			}
 			rs.close();
 			stmt2.close();
 			int id = member.getMemberID();
 			deleteMember(id);
 
 			PreparedStatement stmt = dbc
 
 					.getConnection()
 					.prepareStatement(
 							"INSERT INTO Member(Member_id, barcode_id, forename, surname, zipcode, city, street, phone, mobilephone, fax, email, homepage, birthDay, birthMonth, birthYear, picture, diseases, beltsize, note, trainingunits, course_01, course_02, course_03, course_04, course_05, course_06, course_07, course_08, course_09, course_10, graduation_01, graduation_02, graduation_03, graduation_04, graduation_05, graduation_06, graduation_07, graduation_08, graduation_09, graduation_10) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
 			stmt.setInt(1, id);
 			stmt.setInt(2, member.getBarcodeID());
 			stmt.setString(3, member.getForename());
 			stmt.setString(4, member.getSurname());
 			stmt.setInt(5, member.getZipcode());
 			stmt.setString(6, member.getCity());
 			stmt.setString(7, member.getStreet());
 			stmt.setString(8, member.getPhone());
 			stmt.setString(9, member.getMobilephone());
 			stmt.setString(10, member.getFax());
 			stmt.setString(11, member.getEmail());
 			stmt.setString(12, member.getHomepage());
 			stmt.setString(13, member.getBirthDay());
 			stmt.setString(14, member.getBirthMonth());
 			stmt.setString(15, member.getBirthYear());
 			stmt.setString(16, member.getPicture());
 			stmt.setString(17, member.getDiseases());
 			stmt.setString(18, member.getBeltsize());
 			stmt.setString(19, member.getNote());
 			stmt.setInt(20, member.getTrainingunits());
 
 			// Hinzuf�gen der ArrayList Courses
 			int size = member.getCourses().size();
 			try {
 				for (int i = 0; i < size; i++) {
 					stmt.setInt(i + 21, member.getCourses().get(i));
 				}
 			} catch (IndexOutOfBoundsException e) {
 				System.out.println(e);
 				System.out.println("courses out of bounds");
 			}
 			// Auff�llen mit Nullen, wenn Courses<10
 			if (size < 10) {
 				for (int i = 21 + size; i < 21; i++) {
 					stmt.setInt(i, 0);
 				}
 			}
 
 			// Hinzuf�gen der ArrayList Graduations
 			size = member.getGraduations().size();
 			try {
 				for (int i = 0; i < size; i++) {
 					stmt.setInt(i + 21, member.getGraduations().get(i));
 				}
 			} catch (IndexOutOfBoundsException e) {
 				System.out.println(e);
 				System.out.println("graduations out of bounds");
 			}
 			// Auff�llen mit Nullen, wenn Graduations<10
 			if (size < 10) {
 				for (int i = 31 + size; i < 41; i++) {
 					stmt.setInt(i, 0);
 				}
 			}
 			stmt.executeUpdate();
 
 			dbc.close();
 			stmt.close();
 		} catch (SQLException e) {
 			System.out.println(e);
 			return "Fehler beim Speichern";
 		}
 		return "Mitglied gespeichert";
 
 	}
 
 	/**
 	 * Liefert einen Mitgliedereintrag
 	 * 
 	 * @param barcodeID
 	 *            eines Mitglieds
 	 * 
 	 * @return Objekt des typs Member
 	 */
 
 	public Member getMember(int barcodeID) {
 
 		Member member = new Member();
 		ResultSet rs = null;
 
 		DataBankerConnection dbc = new DataBankerConnection();
 		Statement stmt = dbc.getStatement();
 		String query = "SELECT * FROM Member WHERE barcode_id='" + barcodeID
 				+ "'";
 		try {
 			rs = stmt.executeQuery(query);
 			if (rs.wasNull()) {
 			}
 			while (rs.next()) {
 				member.setMemberID(rs.getInt("Member_id"));
 				member.setBarcodeID(rs.getInt("barcode_id"));
 				member.setForename(rs.getString("forename"));
 				member.setSurname(rs.getString("surname"));
 				member.setZipcode(rs.getInt("zipcode"));
 				member.setCity(rs.getString("city"));
 				member.setStreet(rs.getString("street"));
 				member.setPhone(rs.getString("phone"));
 				member.setMobilephone(rs.getString("mobilephone"));
 				member.setFax(rs.getString("fax"));
 				member.setEmail(rs.getString("email"));
 				member.setHomepage(rs.getString("homepage"));
 				member.setBirthDay(rs.getString("birthDay"));
 				member.setBirthMonth(rs.getString("birthMonth"));
 				member.setBirthYear(rs.getString("birthYear"));
 				member.setPicture(rs.getString("picture"));
 				member.setDiseases(rs.getString("diseases"));
 				member.setBeltsize(rs.getString("beltsize"));
 				member.setNote(rs.getString("note"));
 				member.setTrainingunits(rs.getInt("trainingunits"));
 
 				ArrayList<Integer> courses = new ArrayList<Integer>();
 				courses.add(rs.getInt("course_01"));
 				courses.add(rs.getInt("course_02"));
 				courses.add(rs.getInt("course_03"));
 				courses.add(rs.getInt("course_04"));
 				courses.add(rs.getInt("course_05"));
 				courses.add(rs.getInt("course_06"));
 				courses.add(rs.getInt("course_07"));
 				courses.add(rs.getInt("course_08"));
 				courses.add(rs.getInt("course_09"));
 				courses.add(rs.getInt("course_10"));
				member.setCourses(courses);
 
 				ArrayList<Integer> graduation = new ArrayList<Integer>();
 				graduation.add(rs.getInt("graduation_01"));
 				graduation.add(rs.getInt("graduation_02"));
 				graduation.add(rs.getInt("graduation_03"));
 				graduation.add(rs.getInt("graduation_04"));
 				graduation.add(rs.getInt("graduation_05"));
 				graduation.add(rs.getInt("graduation_06"));
 				graduation.add(rs.getInt("graduation_07"));
 				graduation.add(rs.getInt("graduation_08"));
 				graduation.add(rs.getInt("graduation_09"));
 				graduation.add(rs.getInt("graduation_10"));
				member.setGraduations(graduation);
 			}
 			rs.close();
 			dbc.close();
 			stmt.close();
 			dbc.closeStatement();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		return member;
 	}
 
 	/**
 	 * Liefert einen Mitgliedereintrag
 	 * 
 	 * @param barcodeID
 	 *            eines Mitglieds
 	 * 
 	 * @return Objekt des typs Member
 	 */
 
 	public ArrayList<Member> getMemberList() {
 
 		ArrayList<Member> memberList = new ArrayList<Member>();
 
 		ResultSet rs = null;
 
 		DataBankerConnection dbc = new DataBankerConnection();
 		Statement stmt = dbc.getStatement();
 		String query = "SELECT * FROM Member";
 		try {
 			rs = stmt.executeQuery(query);
 			if (rs.wasNull()) {
 			}
 			while (rs.next()) {
 				Member member = new Member();
 				member.setMemberID(rs.getInt("Member_id"));
 				member.setBarcodeID(rs.getInt("barcode_id"));
 				member.setForename(rs.getString("forename"));
 				member.setSurname(rs.getString("surname"));
 				member.setZipcode(rs.getInt("zipcode"));
 				member.setCity(rs.getString("city"));
 				member.setStreet(rs.getString("street"));
 				member.setPhone(rs.getString("phone"));
 				member.setMobilephone(rs.getString("mobilephone"));
 				member.setFax(rs.getString("fax"));
 				member.setEmail(rs.getString("email"));
 				member.setHomepage(rs.getString("homepage"));
 				member.setBirthDay(rs.getString("birthDay"));
 				member.setBirthMonth(rs.getString("birthMonth"));
 				member.setBirthYear(rs.getString("birthYear"));
 				member.setPicture(rs.getString("picture"));
 				member.setDiseases(rs.getString("diseases"));
 				member.setBeltsize(rs.getString("beltsize"));
 				member.setNote(rs.getString("note"));
 
 				ArrayList<Integer> courses = new ArrayList<Integer>();
 				courses.add(rs.getInt("course_01"));
 				courses.add(rs.getInt("course_02"));
 				courses.add(rs.getInt("course_03"));
 				courses.add(rs.getInt("course_04"));
 				courses.add(rs.getInt("course_05"));
 				courses.add(rs.getInt("course_06"));
 				courses.add(rs.getInt("course_07"));
 				courses.add(rs.getInt("course_08"));
 				courses.add(rs.getInt("course_09"));
 				courses.add(rs.getInt("course_10"));
 
 				ArrayList<Integer> graduation = new ArrayList<Integer>();
 				graduation.add(rs.getInt("graduation_01"));
 				graduation.add(rs.getInt("graduation_02"));
 				graduation.add(rs.getInt("graduation_03"));
 				graduation.add(rs.getInt("graduation_04"));
 				graduation.add(rs.getInt("graduation_05"));
 				graduation.add(rs.getInt("graduation_06"));
 				graduation.add(rs.getInt("graduation_07"));
 				graduation.add(rs.getInt("graduation_08"));
 				graduation.add(rs.getInt("graduation_09"));
 				graduation.add(rs.getInt("graduation_10"));
 
 				memberList.add(member);
 			}
 			rs.close();
 			dbc.close();
 			stmt.close();
 			dbc.closeStatement();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		return memberList;
 	}
 
 	/**
 	 * Liefert einen Mitgliedereintrag
 	 * 
 	 * @param memberID
 	 *            eines Mitglieds
 	 * 
 	 * @return Objekt des typs Member
 	 */
 	public Member getMemberWithMemberID(int memberID) {
 
 		Member member = null;
 		ResultSet rs = null;
 		DataBankerConnection dbc = new DataBankerConnection();
 
 		Statement stmt = dbc.getStatement();
 		String query = "SELECT barcode_id FROM Member WHERE Member_id='"
 				+ memberID + "'";
 
 		try {
 			rs = stmt.executeQuery(query);
 
 			while (rs.next()) {
 				member = getMember(rs.getInt(1));
 			}
 			rs.close();
 			dbc.close();
 			stmt.close();
 			dbc.closeStatement();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		return member;
 	}
 
 	/**
 	 * speichert die Trainingsanwesenheit eines Mitglieds
 	 * 
 	 * @param barcodeId
 	 *            eines Mitglieds, int day, int month, int year
 	 * 
 	 * @return true bei erfolg, false bei fehler
 	 */
 	public boolean setTrainingsPresence(int barcodeID, int day, int month,
 			int year) {
 
 		DataBankerConnection dbc = new DataBankerConnection();
 		try {
 
 			ResultSet rs = null;
 			Statement stmt = dbc.getStatement();
 
 			String query = "SELECT COUNT(*), barcode_id, day, month, year FROM TrainingPresence WHERE day='"
 					+ day
 					+ "' AND month ='"
 					+ month
 					+ "' AND year = '"
 					+ year
 					+ "' AND barcode_id = '" + barcodeID + "'";
 
 			rs = stmt.executeQuery(query);
 			rs.next();
 
 			if (rs.getInt(1) == 0) {
 				// Datum f�r dieses Mitglied noch nicht eingetragen
 				Statement stmt2 = dbc.getStatement();
 
 				String query2 = "INSERT INTO TrainingPresence(barcode_id, day,month,year) VALUES ('"
 						+ barcodeID
 						+ "', '"
 						+ day
 						+ "', '"
 						+ month
 						+ "','"
 						+ year + "')";
 				stmt2.executeUpdate(query2);
 
 				stmt.close();
 				stmt2.close();
 				rs.close();
 				dbc.closeStatement();
 				dbc.close();
 
 				return true;
 
 			} else {
 
 				// Datum f�r dieses Mitglied schon eingetragen
 				stmt.close();
 				rs.close();
 				dbc.closeStatement();
 				dbc.close();
 				return false;
 
 			}
 
 		} catch (SQLException e) {
 			System.out.println(e);
 		}
 		return false;
 
 	}
 
 	/**
 	 * liefert die Trainingsteilnahme eines Mitglieds f�r einen speziellen Monat
 	 * und Jahr in einem 2 Dimensionalen int Array
 	 * 
 	 * @param barcodeId
 	 *            eines Mitglieds, gew�nschter Monat und Jahr
 	 * 
 	 * @return ArrayList<String> presence
 	 */
 	public ArrayList<int[]> getTrainingsPresence(int barcodeID, int month,
 			int year) {
 
 		ArrayList<int[]> presence = new ArrayList<int[]>();
 
 		DataBankerConnection dbc = new DataBankerConnection();
 		try {
 			ResultSet rs = null;
 			Statement stmt = dbc.getStatement();
 			// String query =
 			// "SELECT COUNT(*), day, month, year FROM test WHERE Member_id='"
 			// + memberID + "'";
 
 			String query = "SELECT barcode_id, day, month, year FROM TrainingPresence WHERE month ='"
 					+ month
 					+ "' AND barcode_id = '"
 					+ barcodeID
 					+ "' AND year = '" + year + "'";
 
 			rs = stmt.executeQuery(query);
 			while (rs.next()) {
 
 				int[] test = new int[3];
 				test[0] = rs.getInt(2);
 				test[1] = rs.getInt(3);
 				test[2] = rs.getInt(4);
 				presence.add(test);
 
 			}
 
 		} catch (SQLException e) {
 			System.out.println(e);
 		}
 		if (presence.isEmpty()) {
 			System.out.println("Presence ist leer");
 			return null;
 		}
 		return presence;
 
 	}
 
 	/**
 	 * liefert die Anzahl der Trainingsteilnahmen eines Mitglieds f�r einen
 	 * speziellen Monat
 	 * 
 	 * @param barcodeId
 	 *            eines Mitglieds, gew�nschter Monat und Jahr
 	 * 
 	 * @return int
 	 */
 	public int getTrainingsPresenceInt(int barcodeID, int month, int year) {
 		DataBankerConnection dbc = new DataBankerConnection();
 		try {
 			ResultSet rs = null;
 			Statement stmt = dbc.getStatement();
 
 			String query = "SELECT count(*) FROM TrainingPresence WHERE month ='"
 					+ month
 					+ "' AND barcode_id = '"
 					+ barcodeID
 					+ "' AND year = '" + year + "'";
 
 			rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				return rs.getInt(1);
 			}
 
 		} catch (SQLException e) {
 			System.out.println(e);
 			return 0;
 		}
 		return 0;
 	}
 
 	/**
 	 * l�scht die Trainingsanwesenheit an einen bestimmten Datum
 	 * 
 	 * @param barcodeId
 	 *            eines Mitglieds, gew�nschter Monat
 	 * 
 	 * @return true bei Erfolg, false bei Scheitern oder leerem Monat
 	 */
 	public boolean deleteTrainingsPresence(int barcodeID, int day, int month,
 			int year) {
 		DataBankerConnection dbc = new DataBankerConnection();
 
 		String delete = "DELETE FROM TrainingPresence WHERE barcode_id='"
 				+ barcodeID + "' AND day = '" + day + "' AND month = '" + month
 				+ "' AND year = '" + year + "'";
 
 		Statement stmt = dbc.getStatement();
 		try {
 			stmt.executeUpdate(delete);
 			stmt.close();
 		} catch (SQLException e) {
 			System.out.println(e);
 			return false;
 		}
 		return true;
 	}
 }
