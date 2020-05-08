 package org.alt60m.crs.logic;
 
 import java.sql.*;
 import java.io.*;
 import java.util.Hashtable;
 import java.util.Date;
 import java.util.ArrayList;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /*******************************************************************************
  * This code was written by David Bowdoin, 7/2002
  * 
  * On table export, a table is created in access to hold the results of the
  * desired query. On person import, only fields in the SQL crs_Person table get
  * imported back in, all other get ignored.
  ******************************************************************************/
 
 public class CRSImportExport {
 	private static Map instances = new HashMap();
 	
 	private int verboseLevel = 1; //1 notification and sever error only, 2
 
 	// includes errors, 3 , 4 , 5 everything
 
 	String output = "";
 
 	Hashtable returnVal;
 
 	//Be sure to include the final closing \\ in the path
 	private final String templatePath = "\\database\\template\\";
 
 	private final String downloadPath = "\\database\\download\\";
 
 	private final String uploadPath = "\\database\\upload\\";
 
 	private final int baseNewId = 5000000;
 
 	private String basePath = "";
 
 	//    private String sqlUser = "";
 	//    private String sqlPassword = "";
 
 	private final String accessUrl = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb); DBQ="; //Append
 
 	// Filename
 	// to
 	// use
 
 	private Connection sqlConn;
 
 	private Connection accessConn;
 
 	//Be careful when using these global statments, as a statement can only
 	// handel one resultSet at a time.
 	private Statement sqlStatement;
 
 	private Statement accessStatement;
 
 	//********************************************************************************//
 	//Test Code - How to run this test code, while in c:\ade3, after running
 	// setenv
 	//    javac -d C:\ade3\classes
 	// C:\ade3\controlled-src\services-src\source\org\alt60m\crs\logic\CRSImportExport.java
 	//    java org.alt60m.crs.logic.CRSImportExport
 	public static void main(String[] args) throws Exception {
 		System.out.println("----Start of Import/Export Text----");
 		CRSImportExport test = new CRSImportExport(
 				"Replace me with a real path for me to work");
 
 		//        test.exportToAccess("18","SW","Blank.mdb");
 		//System.out.println(test.importFromAccess("Conference18.mdb", "18"));
 
 		System.out.println("----End of of Import/Export Text----");
 	}
 
 	//********************************************************************************//
 	// Public functions
 	//********************************************************************************//
 	private CRSImportExport(String basePath) {
 		this.basePath = basePath;
 	}
 
 	public static CRSImportExport getInstance(String basePath) {
 		CRSImportExport instance = (CRSImportExport)instances.get(basePath);
 		if ( instance == null ) {
 			instance = new CRSImportExport(basePath);
 			instances.put(basePath, instance);
 		}
 		return instance;
 	}
 
 	public String getImportDirectory() {
 		return basePath + uploadPath;
 	}
 
 	public void setVerboseLevel(int newValue) {
 		verboseLevel = newValue;
 	}
 
 	public void initFolders() {
 		new File(basePath + downloadPath).mkdir();
 		new File(basePath + uploadPath).mkdir();
 	}
 
 	//    public void setSQLUserPwd(String username, String password) {
 	//        sqlUser = username;
 	//        sqlPassword = password;
 	//    }
 	
 	/*  Since we don't use this, I didn't take the time to fix the queries it uses
 	 * to grab email and phone.  It used to get them from person, but they don't exist there
 	 * anymore.
 	public Hashtable importFromAccess(String fileName, String conferenceId)
 			throws Exception {
 		int newPeople = 0;
 		returnVal = new Hashtable();
 		try {
 			openDatabases(basePath + uploadPath + fileName);
 			newPeople = importNewPeople("Registrants", "ministry_Person",
 					"personID", conferenceId);
 			closeDatabases();
 			new File(basePath + uploadPath + fileName).delete(); //Deletes the
 			// database, as
 			// it is no
 			// longer
 			// needed
 		} catch (Exception e) {
 			e.printStackTrace();
 			returnVal.put("Status", "Fail");
 		}
 		returnVal.put("Status", "Success, " + newPeople
 				+ " new people inserted into the master database.");
 		return returnVal;
 	}
 	*/
 	public synchronized Hashtable exportToCSV(String conferenceID) throws Exception {
 		returnVal = new Hashtable();
 		String fileName = "Conference" + conferenceID + ".csv";
 		String tempDatabaseName = basePath + downloadPath + "temp"
 				+ conferenceID + ".mdb";
 		FileWriter file = null;
 		try {
 			file = new FileWriter(basePath + downloadPath + fileName);
 			copyFile(basePath + templatePath + "blank.mdb", tempDatabaseName);
 
 			openDatabases(tempDatabaseName);
 			/* export the conference Table */
 			exportTable(
 					"Conference",
 					"SELECT name, conferenceID, createDate, theme, region, contactName, contactEmail, contactPhone,"
 					+" contactAddress1, contactAddress2, contactCity, contactState, contactZip, splashPageURL, confImageId,"
 					+" fontFace, backgroundColor, foregroundColor, highlightColor,"
 					+" acceptVisa, acceptMasterCard, acceptDiscover, acceptAmericanExpress, checkPayableTo, "
 					+" preRegStart, preRegEnd, masterDefaultDateArrive, masterDefaultDateLeave FROM crs_Conference WHERE conferenceID="
 							+ conferenceID);
 			/* export the RegistrationType Table */
 			exportTable(
 					"RegistrationTypes",
 					"SELECT label, description,defaultDateArrive, defaultDateLeave,preRegStart, preRegEnd,"+
 					"acceptCreditCards, acceptChecks, acceptEChecks, acceptMinistryAcctTransfer, acceptStaffAcctTransfer, acceptScholarships,"+
 					"singlePreRegDeposit, singleOnsiteCost, singleCommuteCost, singleDiscountFullPayment,singleDiscountEarlyReg, singleDiscountEarlyRegDate," +
 					"marriedPreRegDeposit, marriedOnsiteCost, marriedCommuteCost, marriedDiscountFullPayment,marriedDiscountEarlyReg, marriedDiscountEarlyRegDate,"+ 	
 					"askChildren, askSpouse registrationTypeID FROM crs_RegistrationType WHERE fk_conferenceID="
 							+ conferenceID);
 			/* export all registrants */
 			exportTable(
 					"Registrants",
 					"SELECT person.personID, curr.email, person.firstName, person.lastName, person.middleName, person.accountNo," +
 					" IF(SUM(pmt.debit),SUM(pmt.debit),0) AS TotalCharge, IF(SUM(pmt.credit),SUM(pmt.credit),0) AS TotalPaid," +
 					" IF(SUM(pmt.debit),SUM(pmt.debit),0) - IF(SUM(pmt.credit),SUM(pmt.credit), 0) AS AmtDue, person.campus, curr.homePhone," +
 					" reg.registrationDate," +
 					" regType.label," +
 					" reg.preRegistered," +
 					" person.birthDate, person.yearInSchool, person.graduationDate, person.greekAffiliation, person.gender," +
 					" curr.address1, curr.address2, curr.city, curr.state, curr.zip, curr.country," +
 					" perm.address1 AS permanentAddress1, perm.address2 AS permanentAddress2," +
 					" perm.city AS permanentCity, perm.state AS permanentState, perm.zip AS permanentZip," +
 					" perm.homePhone AS permanentPhone, perm.country AS permanentCountry," +
 					" IF(DERIVEDTBL.numberOfKids,DERIVEDTBL.numberOfKids, 0) AS numberOfkids, IF(person.maritalStatus,person.maritalStatus, '') AS mStatus, reg.registrationID" +
 					" FROM  crs_RegistrationType regType, crs_Registration reg" +
 					" INNER JOIN ministry_Person person ON reg.fk_PersonID = person.personID" +
 					" INNER JOIN ministry_NewAddress curr ON person.personID = curr.fk_PersonID" +
 					" INNER JOIN ministry_NewAddress perm ON person.personID = perm.fk_PersonID" +
 					" LEFT OUTER JOIN crs_Payment pmt ON reg.registrationID = pmt.fk_RegistrationID" +
 					" LEFT OUTER JOIN" +
 						" (SELECT COUNT(*) AS numberOfKids, reg.registrationID FROM crs_ChildRegistration creg" +
 						" INNER JOIN crs_Registration reg ON creg.fk_RegistrationID = reg.registrationID" +
 						" GROUP BY creg.fk_RegistrationID, reg.registrationID)" +
 					" DERIVEDTBL ON reg.registrationID = DERIVEDTBL.registrationID WHERE (reg.fk_ConferenceID = '"
 					+ conferenceID
 					+ "') and reg.fk_RegistrationTypeID = regType.registrationTypeID AND curr.addressType = 'current' AND perm.addressType = 'permanent'" +
 							" GROUP BY person.maritalStatus, person.personID, curr.email, person.firstName, person.lastName, person.middleName, person.accountNo, person.birthDate, person.campus, person.yearInSchool, person.graduationDate, person.greekAffiliation, person.gender, curr.address1, curr.address2, curr.city, curr.state, curr.zip, curr.homePhone, curr.country, perm.address1, perm.address2, perm.city, perm.state, perm.zip, perm.homePhone, perm.country, DERIVEDTBL.numberOfKids, reg.registrationID, reg.registrationDate," +
 					//" reg.registrationType," +
 					" regType.label," +
 					" reg.preRegistered");
 			/* export all questions WRONG!!! */
 			//FIXME what does this report?
 			exportQuestions(conferenceID); //Flattens the database out a bit,
 			// for ease of use.
 
 			exportTable(
 					"Payments",
 					"SELECT paymentID, '' AS bagID, paymentDate, debit, credit, type, authCode, businessUnit, operatingUnit, dept AS departmentID, project AS projectID, accountNo, crs_Payment.comment, posted, postedDate, fk_RegistrationID, fk_PersonID FROM crs_Payment, crs_Registration WHERE registrationID=fk_RegistrationID AND fk_ConferenceID="
 							+ conferenceID);
 			exportTable(
 					"ChildRegistration",
					"SELECT childRegistrationID, FLOOR(DATEDIFF(NOW(), birthDate)/365) AS age, firstName, lastName, gender, crs_Registration.arriveDate, birthDate, crs_Registration.leaveDate, inChildCare, fk_RegistrationID FROM crs_ChildRegistration, crs_Registration WHERE registrationID=fk_RegistrationID AND fk_ConferenceID="
 							+ conferenceID);
 
 			file.write("Conference Info\n");
 			writeTableToFile("Conference", file);
 
 			file.write("\n\nRegistration Types\n");
 			writeTableToFile("RegistrationTypes", file);
 
 			file.write("\n\nRegistrants\n");
 			writeTableToFile("Registrants", file);
 
 			file.write("\n\nPayments\n");
 			writeTableToFile("Payments", file);
 
 			file.write("\n\nChild Registrations\n");
 			writeTableToFile("ChildRegistration", file);
 
 			closeDatabases();
 			file.close();
 			new File(tempDatabaseName).delete();
 			returnVal.put("Status", "Success");
 		} catch (Exception e) {
 			writeOutput(2, e.toString());
 			e.printStackTrace();
 			file.close();
 			new File(basePath + downloadPath + fileName).delete();
 			new File(tempDatabaseName).delete();
 			returnVal.put("Status", "Error");
 		}
 		returnVal.put("FileName", fileName);
 		returnVal.put("Output", output);
 		return returnVal;
 	}
 
 	public synchronized Hashtable exportToAccess(String conferenceID, String region,
 			String template) throws Exception {
 		try {
 			returnVal = new Hashtable();
 			String fileName = "Conference" + conferenceID + ".mdb";
 			returnVal.put("FileName", fileName);
 			copyFile(basePath + templatePath + template, basePath
 					+ downloadPath + fileName);
 			openDatabases(basePath + downloadPath + fileName);
 
 			exportTable(
 					"Conference",
 					"SELECT conferenceID, createDate, name, theme, region, contactName, contactEmail, contactPhone, contactAddress1, contactAddress2, contactCity, contactState, contactZip, splashPageURL, confImageId, fontFace, backgroundColor, foregroundColor, highlightColor, acceptCreditCards, acceptEChecks, acceptScholarships, preRegStart, preRegEnd, defaultDateStaffArrive, defaultDateStaffLeave, onsiteCost, commuterCost, preRegDeposit, discountFullPayment, discountEarlyReg, discountEarlyRegDate, checkPayableTo FROM crs_Conference WHERE conferenceID="
 							+ conferenceID);
 			exportTable(
 					"Registrants",
 					"SELECT ministry_Person.personID, crs_Registration.registrationID, crs_Registration.registrationDate, crs_RegistrationType.label AS registrationType," +
 					" crs_Registration.preRegistered, curr.email, ministry_Person.dateCreated," +
 					" ministry_Person.firstName, ministry_Person.lastName, ministry_Person.middleName, ministry_Person.birthDate, ministry_Person.graduationDate," +
 					" ministry_Person.greekAffiliation, ministry_Person.yearInSchool,  ministry_Person.campus, ministry_Person.gender, curr.address1," +
 					" curr.address2, curr.city,  curr.state, curr.zip, curr.homePhone, curr.country," +
 					" ministry_Person.maritalStatus, perm.country AS permanentCountry, perm.zip AS permanentZip," +
 					"  perm.city AS permanentCity, perm.address2 AS permanentAddress2, perm.address1 AS permanentAddress1,  perm.state AS permanentState," +
 					" perm.homePhone AS permanentPhone, ministry_Person.accountNo, crs_Registration.additionalRooms, crs_Registration.leaveDate," +
 					" crs_Registration.arriveDate, ministry_Person.fk_spouseID AS spouseID, crs_Registration.spouseComing," +
 					" crs_Registration.spouseRegistrationID, crs_Registration.registeredFirst, crs_Registration.isOnsite," +
 					" DERIVEDTBL.numberOfKids" +
 					" FROM crs_Registration INNER JOIN ministry_Person ON crs_Registration.fk_PersonID = ministry_Person.personID" +
 					" INNER JOIN ministry_NewAddress curr ON ministry_Person.personID = curr.fk_PersonID" +
 					" INNER JOIN ministry_NewAddress perm ON ministry_Person.personID = perm.fk_PersonID" +
 					" INNER JOIN crs_RegistrationType ON crs_Registration.fk_RegistrationTypeID = crs_RegistrationType.registrationTypeID" +
 					" LEFT OUTER JOIN (SELECT COUNT(*) AS numberOfKids, crs_Registration.registrationID FROM crs_ChildRegistration" +
 					" INNER JOIN crs_Registration ON crs_ChildRegistration.fk_RegistrationID = crs_Registration.registrationID" +
 					" GROUP BY crs_ChildRegistration.fk_RegistrationID, crs_Registration.registrationID) DERIVEDTBL" +
 					" ON  crs_Registration.registrationID = DERIVEDTBL.registrationID" +
 					" WHERE curr.addressType = 'current' AND perm.addressType = 'permanent'" +
 					" AND crs_Registration.fk_ConferenceID = "
 							+ conferenceID);
 			
 			exportTable(
 					"RegistrationTypes",
 					"SELECT registrationTypeID, label, description, defaultDateArrive, defaultDateLeave, preRegStart, preRegEnd, singlePreRegDeposit, singleOnsiteCost, singleCommuteCost, singleDiscountFullPayment, singleDiscountEarlyReg, singleDiscountEarlyRegDate, marriedPreRegDeposit, marriedOnsiteCost, marriedCommuteCost, marriedDiscountFullPayment, marriedDiscountEarlyReg, marriedDiscountEarlyRegDate, acceptEChecks, acceptScholarships, acceptStaffAcctTransfer, acceptMinistryAcctTransfer, acceptCreditCards, askChildren, askSpouse, allowCommute, displayOrder, profileNumber, profileReqNumber, registrationType, fk_ConferenceID, acceptChecks from crs_RegistrationType WHERE fk_ConferenceID = "
 							+ conferenceID);
 			
 			exportTable(
 					"Payments",
 					"SELECT paymentID, '' AS bagID, paymentDate, debit, credit, type, authCode, businessUnit, operatingUnit, dept AS departmentID, project AS projectID, accountNo, crs_Payment.comment, posted, postedDate, fk_RegistrationID, fk_PersonID FROM crs_Payment, crs_Registration WHERE registrationID=fk_RegistrationID AND fk_ConferenceID="
 							+ conferenceID);
 			exportTable(
 					"ChildRegistration",
					"SELECT childRegistrationID, FLOOR(DATEDIFF(NOW(), birthDate)/365) AS age, firstName, lastName, gender, crs_Registration.arriveDate, birthDate, crs_Registration.leaveDate, inChildCare, fk_RegistrationID FROM crs_ChildRegistration, crs_Registration WHERE registrationID=fk_RegistrationID AND fk_ConferenceID="
 							+ conferenceID);
 			if (!region.equals("NC")) {
 				exportTable(
 						"Schools",
 						"SELECT DISTINCT ta.name AS schoolName, ta.state, ml.lane, ml.name AS teamName FROM ministry_LocalLevel ml INNER JOIN ministry_Activity ma ON ml.teamID = ma.fk_teamID RIGHT OUTER JOIN ministry_TargetArea ta ON ma.fk_targetAreaID = ta.TargetAreaID WHERE ta.region = '"
 								+ region
 								+ "' GROUP BY ta.name, ta.state, ml.lane, ml.name");
 			} else {
 				exportTable(
 						"Schools",
 						"SELECT DISTINCT ta.name AS schoolName, ta.state, ml.lane, ml.name AS teamName FROM ministry_LocalLevel ml INNER JOIN ministry_Activity ma ON ml.teamID = ma.fk_teamID RIGHT OUTER JOIN ministry_TargetArea ta ON ma.fk_targetAreaID = ta.TargetAreaID WHERE ta.region in ('GL','GP','MA','MS','NE','NW','RR','SE','SW','UM') GROUP BY ta.name, ta.state, ml.lane, ml.name");
 			}
 
 			appendCustomTable();
 			exportQuestions(conferenceID); //Flattens the database out a bit,
 			// for ease of use.
 
 			closeDatabases();
 			returnVal.put("Status", "Success");
 		} catch (Exception e) {
 			writeOutput(2, e.toString());
 			e.printStackTrace();
 			closeDatabases();
 			returnVal.put("Status", "Error");
 		}
 		returnVal.put("Output", output);
 		return returnVal; //Filename of created database
 	}
 
 	//********************************************************************************//
 	// Private functions
 	//********************************************************************************//
 
 	// Pass this function any query, and it will dump it into a new table in the
 	// access database
 	private void exportTable(String destinationTable, String sourceQuery)
 			throws Exception {
 		writeOutput(1, "Exporting: " + destinationTable);
 		createTable(sourceQuery, destinationTable);
 		copyTable(sourceQuery, destinationTable);
 	}
 
 	//Given a query, createTable creates a new table to accommodate the data
 	private void createTable(String sourceQuery, String destinationTable)
 			throws Exception {
 		System.out.println(sourceQuery);
 		ResultSet rs = sqlStatement.executeQuery("SELECT * FROM ("
 				+ sourceQuery + ") A WHERE 1=2"); // WHERE 1=2 is always false,
 		// so no records are actualy
 		// loaded
 		ResultSetMetaData rsmd = rs.getMetaData();
 		int count = rsmd.getColumnCount();
 		boolean identityFound = false;
 
 		String query = "CREATE TABLE " + destinationTable + " (";
 		for (int i = 1; i <= count; i++) {
 			int type = rsmd.getColumnType(i);
 			switch (type) {
 			case -5: // bigint???
 			case 5: //smallint
 			case 4: //int, the first integer found becomes the primary key
				//System.out.println(rsmd.getTableName(i));
				//System.out.println(rsmd.getColumnTypeName(i) + " " + rsmd.getColumnType(i));
 				// tables primary key
 				if (!identityFound) { //identity
 					identityFound = true;
 					query += rsmd.getColumnName(i) + " COUNTER("
 							+ (new Integer(baseNewId + 1)).toString()
 							+ ") PRIMARY KEY"; //All New records start with
 					// this ID
 				} else
 					query += rsmd.getColumnName(i) + " INT";
 				break;
 			case 1: //char
 			case 12: //varchar
 				//System.out.println(rsmd.getColumnTypeName(i) + " " + rsmd.getColumnType(i));
 				if (rsmd.getColumnDisplaySize(i) < 256)
 					query += rsmd.getColumnName(i) + " TEXT("
 							+ rsmd.getColumnDisplaySize(i) + ")";
 				else
 					query += rsmd.getColumnName(i) + " MEMO";
 				break;
 			case 6: //float
 			case 8: //float
 			case 93: //datetime
 				//System.out.println(rsmd.getColumnTypeName(i) + " " + rsmd.getColumnType(i));
 				query += rsmd.getColumnName(i) + " "
 						+ rsmd.getColumnTypeName(i);
 				break;
 			default:
 				System.err.println("--- ERROR in SQL -->" + rsmd.getColumnName(i)
 						+ " " + rsmd.getColumnTypeName(i) + " " + rsmd.getColumnType(i)
 						+ " --> NOT HANDLED YET!");
 			}
 			if (i != count)
 				query += ", ";
 		}
 		query += ")";
 		try {
 			accessStatement.execute("DROP TABLE " + destinationTable);
 		} catch (Exception e) { /*
 								 * We don't care if this query throws an
 								 * exception, it will most of the time... when
 								 * the table does not exsist
 								 */
 		}
 		System.out.println(query);
 		accessStatement.execute(query);
 	}
 
 	//Given a query, copyTable copies the data from the query to the table.
 	private void copyTable(String sourceQuery, String destinationTable)
 			throws Exception {
 		ResultSet rsSQL = sqlStatement.executeQuery(sourceQuery);
 		ResultSetMetaData rsmd = rsSQL.getMetaData();
 		int colCount = rsmd.getColumnCount();
 
 		while (rsSQL.next()) {
 			String baseQuery = "INSERT INTO " + destinationTable + " (";
 			String valuesQuery = ") VALUES (";
 
 			for (int i = 1; i <= colCount; i++) {
 				switch (rsmd.getColumnType(i)) {
				case -5: // bigint
 				case 5: //smallint
 				case 4: //int
 					baseQuery += rsmd.getColumnName(i) + ", ";
 					valuesQuery += rsSQL.getInt(rsmd.getColumnName(i)) + ", ";
 					break;
 				case 1: //char
 				case 12: //varchar
 					if (rsSQL.getString(i) != null) {
 						baseQuery += rsmd.getColumnName(i) + ", ";
 						valuesQuery += "'" + escapeString(rsSQL.getString(i))
 								+ "', ";
 					}
 					break;
 				case 6: //float
 				case 8: //float
 					baseQuery += rsmd.getColumnName(i) + ", ";
 					valuesQuery += rsSQL.getFloat(rsmd.getColumnName(i)) + ", ";
 					break;
 				case 93: //datetime
 					if (rsSQL.getDate(rsmd.getColumnName(i)) != null) {
 						baseQuery += rsmd.getColumnName(i) + ", ";
 						valuesQuery += "'"
 								+ new SimpleDateFormat("MM/dd/yyyy")
 										.format(rsSQL.getDate(rsmd
 												.getColumnName(i))) + "', ";
 					}
 					break;
 				default:
 					System.err.println("--- ERROR -->" + rsmd.getColumnName(i)
 							+ " " + rsmd.getColumnTypeName(i)
 							+ " --> NOT HANDLED YET!");
 				}
 			}
 			try {
 				baseQuery = baseQuery.substring(0, baseQuery.length() - 2);
 				valuesQuery = valuesQuery
 						.substring(0, valuesQuery.length() - 2)
 						+ ")";
 				//                writeOutput(5,baseQuery + valuesQuery);
 				accessStatement.executeUpdate(baseQuery + valuesQuery);
 			} catch (Exception e) {
 				writeOutput(2, "Exception below --- ERROR in SQL -->"
 						+ baseQuery + valuesQuery);
 				writeOutput(2, e.toString());
 				e.printStackTrace();
 			}
 		}
 	}
 
 	//Ignores all questions of type: Divider, Info, hide
 	private void exportQuestions(String confID) throws Exception {
 		writeOutput(1, "Exporting Custom Question Answers");
 		Hashtable fieldMapping = new Hashtable();
 
 		//Alter Registrants to include customAnswers
 		ResultSet rs = sqlStatement
 				.executeQuery("SELECT DISTINCT crs_QuestionText.body AS question, crs_Question.fk_QuestionTextID AS questionNumber, crs_RegistrationType.label as RegistrationType, crs_QuestionText.status "
 						+ "FROM crs_RegistrationType, crs_Question INNER JOIN crs_QuestionText ON "
 						+ "crs_Question.fk_QuestionTextID = crs_QuestionText.questionTextID "
 						+ "WHERE (crs_Question.fk_RegistrationTypeID=registrationTypeID) AND (crs_Question.fk_ConferenceID = "
 						+ confID
 						+ ") AND (crs_QuestionText.answerType NOT LIKE 'Divider') AND (crs_QuestionText.answerType NOT LIKE 'Info')  "
 						+ " AND (crs_QuestionText.answerType NOT LIKE 'hide')");
 		String query = "ALTER TABLE Registrants ADD ";
 		int i = 0;
 		while (rs.next()) {
 			String fieldName = rs.getString(1).trim();
 			Integer qNumber = new Integer(rs.getInt(2));
 			String regType = rs.getString(3);
 			String qType = rs.getString(4);
 
 			if (fieldName.length() > 0) {
 				if (fieldName.length() > 62)
 					fieldName = fieldName.substring(0, 61);
 				fieldName = fieldName.replace('\n', ' ').replace('\r', ' ').replace('[', '|')
 						.replace(']', '|').replace('(', '_').replace(')', '_')
 						.replace('!', ' ').replace('.', '_').replace('-', '_')
 						.replace('`', '_').replace('"', '_').replace('\'', '_')
 						.replace('?', '_').replace(',', '_');
 				if ("custom".equals(qType)) {
 					if ((fieldName + "_" + regType).length() > 61) {
 						fieldName = fieldName.substring(0, (60 - regType.length()));
 					}
 					regType = regType.replace('\n', ' ').replace('[', '|')
 						.replace(']', '|').replace('(', '_').replace(')', '_')
 						.replace('!', ' ').replace('.', '_').replace('-', '_')
 						.replace('`', '_').replace('"', '_').replace('\'', '_')
 						.replace('?', '_').replace(',', '_');
 					fieldName += "_" + regType;
 				}
 				if (query.indexOf(fieldName) > 0) {
 					fieldName += String.valueOf(++i);
 				}
 				if (!fieldMapping.containsKey(qNumber)) {
 					fieldMapping.put(qNumber, fieldName); // Store
 					// the
 					// mapping
 					// of
 					// ID's
 					// to
 					// names
 					query += "[" + fieldName + "] MEMO, ";
 				}
 			}
 		}
 		rs.close();
 		if (query.length() > 30) { //Don't run, if there were no custom
 			// questions.
 			query = query.substring(0, query.length() - 2);
 			writeOutput(5, query);
 			try {
 				accessStatement.execute(query);
 			} catch (Exception e) {
 				writeOutput(2, "Exception below --- ERROR in SQL -->" + query);
 				writeOutput(2, e.toString());
 				e.printStackTrace();
 			}
 
 			//Insert customAnswers into Registrants
 			// don't include "info","divider","hide" question types (if answers exist for some reason) 
 			rs = sqlStatement
 					.executeQuery("SELECT crs_Registration.fk_PersonID, crs_Question.fk_QuestionTextID, crs_Answer.body FROM crs_Answer INNER JOIN crs_Registration ON crs_Answer.fk_RegistrationID = crs_Registration.registrationID INNER JOIN crs_Question ON crs_Answer.fk_QuestionID = crs_Question.questionID INNER JOIN crs_QuestionText ON crs_Question.fk_QuestionTextID = crs_QuestionText.questionTextID WHERE crs_Registration.fk_ConferenceID = "
 							+ confID 
 							+" AND (crs_QuestionText.answerType NOT LIKE 'Divider') AND (crs_QuestionText.answerType NOT LIKE 'Info') AND (crs_QuestionText.answerType NOT LIKE 'hide')");
 			while (rs.next()) {
 				int personID = rs.getInt(1);
 				Integer questionTextID= new Integer(rs.getInt(2));
 				String value = rs.getString(3);
 				
 				
 				if (value.matches("Y|N|T|F")) {
 					if (value.matches("Y|N"))
 						value = "Y".equals(value) ? "Yes" : "No";
 					if (value.matches("T|F"))
 						value = "T".equals(value) ? "Yes" : "No";
 				}
 				if (value != null) {
 					query = "UPDATE Registrants SET Registrants.["
 							+ fieldMapping.get(questionTextID)
 							+ "] = '" + escapeString(value)
 							+ "' WHERE personID=" + personID;
 					writeOutput(5, query);
 					try {
 						accessStatement.execute(query);
                     } catch (SQLException e) { 
                     	System.err.println(e+" --- ERROR inSQL -->" + query + "\n\tQuestionTextID:" + questionTextID);
                     	throw e;
                     }
 				}
 			}
 			rs.close();
 		}
 	}
 
 	private void appendCustomTable() throws Exception {
 		try {
 			ResultSet rs = accessStatement
 					.executeQuery("SELECT * FROM Registrants_Custom");
 			ResultSetMetaData rsmd = rs.getMetaData();
 			int count = rsmd.getColumnCount();
 
 			//Alter Registrants to include Registrants_Custom
 			String query = "ALTER TABLE Registrants ADD ";
 			for (int i = 1; i <= count; i++) {
 				query += "[" + rsmd.getColumnName(i) + "] "
 						+ rsmd.getColumnTypeName(i) + ", ";
 			}
 			rs.close();
 			query = query.substring(0, query.length() - 2);
 			writeOutput(5, query);
 			accessStatement.execute(query);
 			writeOutput(1,
 					"Appended the table Registrants_Custom to Registrants");
 		} catch (Exception e) {
 			writeOutput(
 					1,
 					"Registrants_Custom table not found, continuing with database creation.");
 		}
 	}
 
 	//********************************************************************************//
 	private void writeTableToFile(String sourceTable, FileWriter outputFile)
 			throws Exception {
 		writeOutput(1, "Writing CSV File");
 		ResultSet rsSQL = accessStatement.executeQuery("Select * FROM "
 				+ sourceTable);
 		ResultSetMetaData rsmd = rsSQL.getMetaData();
 		int colCount = rsmd.getColumnCount();
 		String lineBuffer = "";
 		String tempString;
 		Date tempDate;
 
 		//Write Out Header Line
 		for (int i = 1; i <= colCount; i++)
 			lineBuffer += rsmd.getColumnName(i) + "\",\"";
 		outputFile.write("\"" + lineBuffer + "\"\n");
 
 		//Write out Data
 		while (rsSQL.next()) {
 			lineBuffer = "";
 			for (int i = 1; i <= colCount; i++) {
 				switch (rsmd.getColumnType(i)) {
 				case 93: //datetime
 					tempDate = rsSQL.getDate(rsmd.getColumnName(i));
 					if (tempDate != null)
 						lineBuffer += new SimpleDateFormat("MM/dd/yyyy")
 								.format(tempDate);
 					break;
 				default:
 					tempString = rsSQL.getString(i);
 					if (tempString != null)
 						lineBuffer += escapeString(tempString);
 				}
 				lineBuffer += "\",\"";
 			}
 			outputFile.write("\"" + lineBuffer + "\"\n");
 		}
 	}
 
 	//********************************************************************************//
 
 	/*
 	private int importNewPeople(String sourceTable, String destTable,
 			String primaryKey, String conferenceId) throws Exception {
 		int peopleImported = 0;
 		Statement stmt = sqlConn.createStatement(
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 		//CRASH NOTE: If you are getting an error around here, make sure that
 		// the tomcat temp dir exsists.
 		//When tomcat starts-->: Using CATALINA_TMPDIR: tomcat\temp
 		ResultSet rsSQL = stmt.executeQuery("SELECT * FROM " + destTable);
 		ResultSetMetaData rsmd = rsSQL.getMetaData();
 		int colCount = rsmd.getColumnCount();
 
 		String query = "SELECT * FROM " + sourceTable + " WHERE " + primaryKey
 				+ " > " + (new Integer(baseNewId)).toString();
 		ResultSet rsAccess = accessStatement.executeQuery(query);
 
 		while (rsAccess.next()) {
 			// Check to see if a walk-on exsists already, if so, we ignore.
 			String key = rsAccess.getString(primaryKey);
 			String personId = "";
 			if (!checkForPersonMatch(key)) {
 				writeOutput(5, "Adding this walk-on person");
 				rsSQL.moveToInsertRow();
 				for (int i = 1; i <= colCount; i++) {
 					if (!(rsmd.getColumnType(i) == 4 && rsmd.getColumnTypeName(
 							i).length() == 12)) { // not the primary key
 						try {
 							Object obj = rsAccess.getObject(rsmd
 									.getColumnName(i));
 							if ((rsmd.getColumnName(i).equals("username"))
 									&& obj == null) {
 								rsSQL.updateObject(i, "CRS Import");
 							} else {
 								rsSQL.updateObject(i, obj);
 							}
 						} catch (SQLException e) {
 							writeOutput(
 									2,
 									"Feild "
 											+ rsmd.getColumnName(i)
 											+ " does not exsist in the access database, skipping");
 						}
 					}
 				}
 				rsSQL.insertRow();
 				rsSQL = stmt.executeQuery("SELECT * FROM " + destTable
 						+ " ORDER BY " + primaryKey + " DESC");
 				rsSQL.next();
 				personId = rsSQL.getString(primaryKey);
 				importNewRegistration(sourceTable, "crs_Registration",
 						primaryKey, key, personId, conferenceId);
 				peopleImported++;
 			} else {
 				personId = getPersonMatch(key);
 				importNewRegistration(sourceTable, "crs_Registration",
 						primaryKey, key, personId, conferenceId);
 				peopleImported++; //This really isn't inserting a new Person
 				// per se, however this counter is for the
 				// user. What we're really counting is the
 				// number of registrants imported for this
 				// conference.
 			}
 		}
 		rsSQL.close();
 		rsAccess.close();
 		stmt.close();
 		return peopleImported;
 	}
 
 	private boolean checkForPersonMatch(String newPersonID) throws Exception {
 		//Load up the record from the access database
 		String query = "SELECT * FROM Registrants WHERE personID = "
 				+ newPersonID;
 		ResultSet rsAccess = accessConn.createStatement().executeQuery(query);
 
 		rsAccess.next();
 		//Build a query for the SQL Database
 		String ssn = rsAccess.getString("ssn");
 		String email = rsAccess.getString("email");
 		String firstName = rsAccess.getString("firstName");
 		String lastName = rsAccess.getString("lastName");
 		String homePhone = rsAccess.getString("homePhone");
 		Date birthDate = rsAccess.getDate("birthDate");
 		String whereQuery = "1=2";
 
 		//If SSN matches
 		if (ssn != null && ssn.length() > 0)
 			whereQuery += " OR ( ssn like '" + ssn + "')";
 
 		//If email AND firstName
 		if (email != null && firstName != null && email.length() > 0
 				&& firstName.length() > 0)
 			whereQuery += " OR (deprecated_email like '" + email
 					+ "' AND firstName like '" + firstName + "')";
 
 		//If (firstName AND lastName) AND (birthDate OR homePhone)
 		if ((firstName != null && lastName != null && firstName.length() > 0 && lastName
 				.length() > 0)
 				&& (homePhone != null || birthDate != null))
 			whereQuery += " OR ((firstName like '" + firstName
 					+ "' AND lastName like '" + lastName
 					+ "') AND (deprecated_phone like '" + homePhone
 					+ "' OR birthDate like '" + birthDate + "'))";
 
 		query = "SELECT COUNT(*) FROM ministry_Person WHERE " + whereQuery;
 		ResultSet rsCheck = sqlConn.createStatement().executeQuery(query);
 		rsCheck.next();
 		int recordCount = rsCheck.getInt(1);
 		rsCheck.close();
 		return (recordCount > 0);
 	}
 
 	private String getPersonMatch(String newPersonID) throws Exception {
 		//Load up the record from the access database
 		String query = "SELECT * FROM Registrants WHERE personID = "
 				+ newPersonID;
 		ResultSet rsAccess = accessConn.createStatement().executeQuery(query);
 
 		rsAccess.next();
 		//Build a query for the SQL Database
 		String ssn = rsAccess.getString("ssn");
 		String email = rsAccess.getString("email");
 		String firstName = rsAccess.getString("firstName");
 		String lastName = rsAccess.getString("lastName");
 		String homePhone = rsAccess.getString("homePhone");
 		Date birthDate = rsAccess.getDate("birthDate");
 		String whereQuery = "1=2";
 
 		//If SSN matches
 		if (ssn != null && ssn.length() > 0)
 			whereQuery += " OR ( ssn like '" + ssn + "')";
 
 		//If email AND firstName
 		if (email != null && firstName != null && email.length() > 0
 				&& firstName.length() > 0)
 			whereQuery += " OR (deprecated_email like '" + email
 					+ "' AND firstName like '" + firstName + "')";
 
 		//If (firstName AND lastName) AND (birthDate OR homePhone)
 		if ((firstName != null && lastName != null && firstName.length() > 0 && lastName
 				.length() > 0)
 				&& (homePhone != null || birthDate != null))
 			whereQuery += " OR ((firstName like '" + firstName
 					+ "' AND lastName like '" + lastName
 					+ "') AND (deprecated_phone like '" + homePhone
 					+ "' OR birthDate like '" + birthDate + "'))";
 
 		query = "SELECT personID FROM ministry_Person WHERE " + whereQuery;
 		ResultSet rsCheck = sqlConn.createStatement().executeQuery(query);
 		rsCheck.next();
 		String personID = String.valueOf(rsCheck.getInt(1));
 		rsCheck.close();
 		return personID;
 	}
 
 	private void importNewRegistration(String sourceTable, String destTable,
 			String primaryKey, String sourcePersonId, String destPersonId,
 			String conferenceId) throws Exception {
 		Statement stmt = sqlConn.createStatement(
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 		Statement accessStatement2 = accessConn.createStatement();
 		ResultSet rsSQL = stmt.executeQuery("SELECT * FROM " + destTable);
 		ResultSetMetaData rsmd = rsSQL.getMetaData();
 		int colCount = rsmd.getColumnCount();
 
 		String query = "SELECT * FROM " + sourceTable + " WHERE " + primaryKey
 				+ " = " + sourcePersonId;
 		ResultSet rsAccess = accessStatement2.executeQuery(query);
 
 		rsAccess.next();
 		writeOutput(5, "Adding this walk-on registration");
 		rsSQL.moveToInsertRow();
 		for (int i = 1; i <= colCount; i++) {
 			if (!(rsmd.getColumnType(i) == 4 && rsmd.getColumnTypeName(i)
 					.length() == 12)) { // not the primary key
 				try {
 					String columnName = rsmd.getColumnName(i);
 					if (columnName.equals("fk_PersonID")) {
 						rsSQL.updateObject(i, destPersonId);
 					} else if (columnName.equals("fk_ConferenceID")) {
 						rsSQL.updateObject(i, conferenceId);
 					} else {
 						rsSQL.updateObject(i, rsAccess.getObject(columnName));
 					}
 				} catch (SQLException e) {
 					e.printStackTrace();
 					writeOutput(
 							2,
 							"Field "
 									+ rsmd.getColumnName(i)
 									+ " does not exsist in the access database, skipping");
 				}
 			}
 		}
 		rsSQL.insertRow();
 		rsSQL = stmt.executeQuery("SELECT * FROM " + destTable
 				+ " WHERE fk_PersonID = " + destPersonId
 				+ " AND fk_ConferenceID = " + conferenceId);
 		rsSQL.next();
 		String registrationId = rsSQL.getString("registrationID");
 		rsSQL.close();
 		rsAccess.close();
 		accessStatement2.close();
 		stmt.close();
 		importNewCustomAnswers(sourceTable, "crs_Answer", primaryKey,
 				sourcePersonId, registrationId, conferenceId);
 	}
 
 	private void importNewCustomAnswers(String sourceTable, String destTable,
 			String primaryKey, String personId, String registrationId,
 			String conferenceId) throws Exception {
 		Statement stmt = sqlConn.createStatement(
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 		Statement stmt2 = sqlConn.createStatement(
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 		Statement accessStatement2 = accessConn.createStatement();
 		String query = "SELECT DISTINCT crs_QuestionText.body AS question, crs_Question.fk_QuestionTextID AS questionNumber, crs_Question.registrationType, crs_QuestionText.status, crs_Question.questionID AS questionID "
 				+ "FROM crs_Question INNER JOIN crs_QuestionText ON "
 				+ "crs_Question.fk_QuestionTextID = crs_QuestionText.questionTextID "
 				+ "WHERE (crs_Question.fk_ConferenceID = "
 				+ conferenceId
 				+ ") AND (crs_QuestionText.answerType NOT LIKE 'Divider') AND (crs_QuestionText.answerType NOT LIKE 'Info')  "
 				+ " AND (crs_QuestionText.answerType NOT LIKE 'hide')";
 		ResultSet rsSQLQuestions = stmt.executeQuery(query);
 		ResultSet rsSQLAnswers = stmt2.executeQuery("SELECT * FROM "
 				+ destTable);
 		query = "SELECT * FROM " + sourceTable + " WHERE " + primaryKey + " = "
 				+ personId;
 		ResultSet rsAccess = accessStatement2.executeQuery(query);
 		rsAccess.next();
 		ArrayList fieldsAskedFor = new ArrayList();
 		int i = 0;
 		while (rsSQLQuestions.next()) {
 			rsSQLAnswers.moveToInsertRow();
 			String question = rsSQLQuestions.getObject("question").toString();
 			String fieldName = rsSQLQuestions.getString(1).trim();
 			String regType = rsSQLQuestions.getString(3);
 			String qType = rsSQLQuestions.getString(4);
 			if (fieldName.length() > 0) {
 				if (fieldName.length() > 62)
 					fieldName = fieldName.substring(0, 61);
 				fieldName = fieldName.replace('\n', ' ').replace('[', '|')
 						.replace(']', '|').replace('(', '_').replace(')', '_')
 						.replace('!', ' ').replace('.', '_').replace('-', '_')
 						.replace('`', '_').replace('"', '_').replace('\'', '_')
 						.replace('?', '_').replace(',', '_');
 				if ("custom".equals(qType)) {
 					if ((fieldName + "_" + regType).length() > 61)
 						fieldName = fieldName.substring(0, (60 - regType
 								.length()));
 					fieldName += "_" + regType;
 				}
 				if (fieldsAskedFor.contains(fieldName)) {
 					fieldName += String.valueOf(++i);
 				}
 				fieldsAskedFor.add(fieldName);
 			}
 			System.out.println("--" + fieldName + "--");
 			try {
 				rsSQLAnswers.moveToInsertRow();
 				rsSQLAnswers.updateObject("fk_QuestionId", rsSQLQuestions
 						.getObject("questionID"));
 				rsSQLAnswers
 						.updateObject("body", rsAccess.getObject(fieldName));
 				rsSQLAnswers.updateObject("fk_RegistrationID", registrationId);
 				rsSQLAnswers.insertRow();
 			} catch (Exception e) {
 				if (e.getMessage().equals("Column not found")) {
 					writeOutput(2, e.getMessage());
 				} else {
 					throw e;
 				}
 			}
 		}
 		rsSQLAnswers.close();
 		rsSQLQuestions.close();
 		rsAccess.close();
 		stmt.close();
 		stmt2.close();
 		accessStatement2.close();
 	}
 	*/
 	//********************************************************************************//
 	private void openDatabases(String accessFileName) throws Exception {
 		writeOutput(5, "Opening up the access Database: " + accessFileName);
 
 		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
 		accessConn = DriverManager.getConnection(accessUrl + accessFileName);
 		accessStatement = accessConn.createStatement();
 
 		sqlConn = org.alt60m.util.DBConnectionFactory.getDatabaseConn();
 		sqlStatement = sqlConn.createStatement();
 	}
 
 	private void closeDatabases() throws Exception {
 		if (!sqlConn.isClosed())
 			sqlConn.close();
 		if (!accessConn.isClosed())
 			accessConn.close();
 	}
 
 	//********************************************************************************//
 	private void writeOutput(int priority, String sourceString) {
 		if (priority <= verboseLevel) {
 			output += sourceString + "\n";
 			System.out.println(sourceString);
 		}
 	}
 
 	//********************************************************************************//
 	private void copyFile(String sourceFile, String destinationFile)
 			throws Exception {
 		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
 				sourceFile));
 		BufferedOutputStream out = new BufferedOutputStream(
 				new FileOutputStream(destinationFile));
 		byte[] buf = new byte[1024];
 		int len;
 		while ((len = in.read(buf)) >= 0) {
 			out.write(buf, 0, len);
 		}
 		in.close();
 		out.close();
 	}
 
 	private String escapeString(String inputString) throws Exception {
 		int len = inputString.length();
 		char sourceChar[] = inputString.toCharArray();
 		char destChar[] = new char[len + 100];
 		int s = 0, d = 0;
 		while (s < len) {
 			if (sourceChar[s] == 10 || sourceChar[s] == 13)
 				sourceChar[s] = ' ';
 			if (sourceChar[s] == '\'')
 				destChar[d++] = '\'';
 			if (sourceChar[s] == '#')
 				sourceChar[s] = ' ';
 			destChar[d++] = sourceChar[s++];
 		}
 		return new String(destChar, 0, d);
 	}
 
 }
