 import java.sql.*;
 
 import javax.sql.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 class DatabaseConnector {
 	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
 	private static final String DB_URL = "jdbc:mysql://localhost/vcf_analyzer";
 
 	static final String USER = "vcf_user";
 	static final String PASS = "vcf";
 
 	public static final ArrayList<String> EntryFixedInfo = new ArrayList<String>(
 			Arrays.asList("CHROM", "FILTER", "ID", "POS", "REF", "QUAL", "ALT"));
 
 	private Connection conn;
 	private Statement stmt;
 	private ArrayList<Statement> stmtList;
 
 	public DatabaseConnector() throws SQLException, ClassNotFoundException {
 
 		try {
 			this.conn = null;
 			this.stmt = null;
 
 			// ########### What is this supposed to do?
 			Class.forName(JDBC_DRIVER);
 
 			conn = DriverManager.getConnection(DB_URL, USER, PASS);
 			stmt = conn.createStatement();
 		} catch (Exception e) {
 			throw new SQLException("Could not connect to database");
 		}
 
 	}
 
 	public long getVcfId(String vcfName) throws IllegalArgumentException,
 			SQLException {
 		String sql = "";
 		try {
 			sql = "SELECT `VcfId` FROM `vcf_analyzer`.`Vcf` WHERE `VcfName` = '"
 					+ vcfName + "'";
 			ResultSet rs = stmt.executeQuery(sql);
 
 			if (rs.next()) {
 				long id = Long.parseLong(rs.getString("VcfId"));
 				rs.close();
 				return id;
 			}
 
 			throw new IllegalArgumentException("VCF: " + vcfName + " not found");
 
 		} catch (SQLException se) {
 			throw new SQLException("Invalid Query " + sql);
 		}
 	}
 
 	public String getVcfHeader(long vcfId) throws IllegalArgumentException,
 			SQLException {
 		String sql = "";
 		try {
 			sql = "SELECT `Header` FROM `vcf_analyzer`.`VcfHeader` WHERE `VcfId` = '"
 					+ vcfId + "'";
 			ResultSet rs = stmt.executeQuery(sql);
 
 			if (rs.next()) {
 				String result = rs.getString("Header");
 				rs.close();
 				return result;
 			}
 
 			throw new IllegalArgumentException("VCF header for: " + vcfId
 					+ " not found");
 
 		} catch (SQLException se) {
 			throw new SQLException("Invalid Query: " + sql);
 		}
 	}
 
 	public int createFilter(String filterName) throws SQLException,
 			ClassNotFoundException {
 		String sql = null;
 		try {
 			sql = String.format(
 					"INSERT into `Filter` VALUES (NULL, '%s', '0');",
 					filterName);
 			this.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
 
 			ResultSet rs = this.stmt.getGeneratedKeys();
 			rs.next();
 			return rs.getInt(1);
 
 		} catch (SQLException se) {
 			throw new SQLException(se.getMessage());
 		}
 	}
 
 	public int createFilterEntry(int filterID, int operator, String infoName,
 			String[] operands) throws SQLException {
 		String sql = null;
 		try {
 			if (operands.length == 1) {
 				sql = String
 						.format("INSERT INTO `vcf_analyzer`.`FilterEntry` VALUES (NULL, '%s', '%s', '%s', '%s', NULL);",
 								filterID, infoName, operator, operands[0]);
 			} else if (operands.length == 2) {
 				sql = String
 						.format("INSERT INTO `vcf_analyzer`.`FilterEntry` VALUES (NULL, '%s', '%s', '%s', '%s', '%s');",
 								filterID, infoName, operator, operands[0],
 								operands[1]);
 			} else if (operands.length == 0) {
 				System.out.println("No operands given");
 				return 0;
 			} else {
 				System.out.println("Too many operands given");
 				return 0;
 			}
 			this.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
 
 			ResultSet rs = this.stmt.getGeneratedKeys();
 			rs.next();
 			int filterEntryID = rs.getInt(1);
 
 			return filterEntryID;
 		} catch (SQLException se) {
 			throw new SQLException(se.getMessage());
 		}
 	}
 
 	public int createFilterIndividual(int filterID, int operator,
 			String genoName, String[] operands, String limit) throws SQLException {
 		String sql = null;
 		try {
 			if (operands.length == 1) {
 				sql = String
 						.format("INSERT INTO `vcf_analyzer`.`FilterIndividual` VALUES (NULL, '%s', '%s', '%s', '%s', NULL, '%s');",
 								filterID, genoName, operator, operands[0], limit);
 			} else if (operands.length == 2) {
 				sql = String
 						.format("INSERT INTO `vcf_analyzer`.`FilterIndividual` VALUES (NULL, '%s', '%s', '%s', '%s', '%s', '%s');",
 								filterID, genoName, operator, operands[0], operands[1], limit);
 			} else if (operands.length == 0) {
 				System.out.println("No operands given");
 				return 0;
 			} else {
 				System.out.println("Too many operands given");
 				return 0;
 			}
 			this.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
 
 			ResultSet rs = this.stmt.getGeneratedKeys();
 			rs.next();
 			int filterEntryID = rs.getInt(1);
 
 			return filterEntryID;
 		} catch (SQLException se) {
 			throw new SQLException(se.getMessage());
 		}
 	}
 
 	public int getFilterID(String filterName) throws IllegalArgumentException,
 			SQLException {
 		String sql = "";
 		try {
 			sql = "SELECT `FilID` FROM `vcf_analyzer`.`Filter` WHERE `FilName` = '"
 					+ filterName + "'";
 			ResultSet rs = stmt.executeQuery(sql);
 
 			if (rs.next()) {
 				int id = Integer.parseInt(rs.getString("FilId"));
 				rs.close();
 				return id;
 			}
 
 			throw new IllegalArgumentException("Filter: " + filterName
 					+ " not found");
 		} catch (SQLException se) {
 			throw new SQLException("Invalid Query " + sql);
 		}
 	}
 
 	public ArrayList<FilterParameter> getFilterEntries(int FilId) throws SQLException
 	{
 		ArrayList<FilterParameter> filterEntries = new ArrayList<FilterParameter>();
 		String sql = "";
 		try {
 			sql = String.format("SELECT * FROM `vcf_analyzer`.`FilterEntry` WHERE `FilId`='%d'", FilId);
 			System.out.println(sql);
 			ResultSet rs = stmt.executeQuery(sql);
 			while(rs.next()) {
 				String tableName = rs.getString("InfoName");
 				int comparison = rs.getInt("Comparison");
 				String comparator = rs.getString("Comparator");
 				String comparator2 = rs.getString("Comparator2");
				int failureAllow = rs.getInt("FailureAllow");
 				
 				FilterParameter temp = new FilterParameter(tableName, comparison, comparator, comparator2, 0,0);
 				filterEntries.add(temp);
 			}
 			System.out.println("Entry params" + filterEntries.size());
 		} catch (SQLException se) {
 			throw new SQLException(se.getMessage());
 		}
 		return filterEntries;
 	}
 	
 	public ArrayList<FilterParameter> getFilterIndividuals(int FilId) throws SQLException
 	{
 		ArrayList<FilterParameter> filterIndividuals = new ArrayList<FilterParameter>();
 		String sql = "";
 		try {
 			sql = String.format("SELECT * FROM `vcf_analyzer`.`FilterIndividual` WHERE `FilId`='%d'", FilId);
 			ResultSet rs = stmt.executeQuery(sql);
 			while(rs.next()) {
 				String tableName = rs.getString("GenoName");
 				int comparison = rs.getInt("Comparison");
 				String comparator = rs.getString("Comparator");
 				String comparator2 = rs.getString("Comparator2");
 				int failureAllow = rs.getInt("FailureAllow");
 				int passExactly = rs.getInt("PassExactly");
 				
 				FilterParameter temp = new FilterParameter(tableName, comparison, comparator,
 															comparator2, failureAllow, passExactly);
 				filterIndividuals.add(temp);
 			}
 		} catch (SQLException se) {
 			throw new SQLException(se.getMessage());
 		}
 		return filterIndividuals;
 	}
 	
 	public int getInfoDataType( String infoName ) throws SQLException
 	{
 		String sql = "";
 		try {
 			sql = "SELECT * FROM `vcf_analyzer`.`InfoTable` WHERE `InfoName` = '"
 					+ infoName + "'";
 			ResultSet rs = stmt.executeQuery(sql);
 			if ( rs.next() )
 			{
 				int type = Integer.parseInt(rs.getString("Type"));
 				rs.close();
 				return type;
 			}
 			throw new SQLException("Invalid INFO name " + infoName);
 
 		} catch (SQLException se) {
 			throw new SQLException("Invalid Query " + sql);
 		}
 	}
 	
 	public int getGenoTypeDataType( String genoName ) throws SQLException
 	{
 		String sql = "";
 		try {
 			sql = "SELECT * FROM `vcf_analyzer`.`GenotypeTable` WHERE `GenoName` = '"
 					+ genoName + "'";
 			ResultSet rs = stmt.executeQuery(sql);
 			if ( rs.next() )
 			{
 				int type = Integer.parseInt(rs.getString("Type"));
 				rs.close();
 				return type;
 			}
 			throw new SQLException("Invalid genotype name " + genoName);
 
 		} catch (SQLException se) {
 			throw new SQLException("Invalid Query " + sql);
 		}
 	}	
 	
 	public ResultSet getVcfEntries(long vcfId) throws SQLException {
 		String sql = "";
 		try {
 			sql = "SELECT * FROM `vcf_analyzer`.`VcfEntry` WHERE `VcfId` = '"
 					+ vcfId + "' ORDER BY `EntryId` ASC";
 			ResultSet rs = stmt.executeQuery(sql);
 
 			return rs;
 
 		} catch (SQLException se) {
 			throw new SQLException("Invalid Query " + sql);
 		}
 	}
 	
 	public ArrayList<String> getInfoTableNames() throws SQLException {
 		String sql = "";
 		ArrayList<String> tables = new ArrayList<String>();
 		try {
 			sql = "SELECT `InfoName` FROM `vcf_analyzer`.`InfoTable` ORDER BY `InfoName` ASC";
 			ResultSet rs = this.stmt.executeQuery(sql);
 			while (rs.next()) {
 				String infoName = rs.getString("InfoName");
 				if (!EntryFixedInfo.contains(infoName)) {
 					tables.add(infoName);
 				}
 			}
 			rs.close();
 			return tables;
 		} catch (SQLException se) {
 			throw new SQLException(se.getMessage());
 		}
 	}
 
 	public ArrayList<String> getAllEntryTableNames() throws SQLException {
 		String sql = "";
 		ArrayList<String> tables = new ArrayList<String>();
 		try {
 			sql = "SELECT `InfoName` FROM `vcf_analyzer`.`InfoTable` ORDER BY `InfoName` ASC";
 			ResultSet rs = this.stmt.executeQuery(sql);
 			while (rs.next()) {
 				String infoName = rs.getString("InfoName");
 				tables.add(infoName);
 			}
 			rs.close();
 			return tables;
 		} catch (SQLException se) {
 			throw new SQLException(se.getMessage());
 		}
 	}	
 	
 	public ArrayList<String> getGenotypeTableNames() throws SQLException {
 		String sql = "";
 		ArrayList<String> tables = new ArrayList<String>();
 		try {
 			sql = "SELECT `GenoName` FROM `vcf_analyzer`.`GenotypeTable` ORDER BY `GenoName` ASC";
 			ResultSet rs = this.stmt.executeQuery(sql);
 			while (rs.next()) {
 				String genoName = rs.getString("GenoName");
 				if (!EntryFixedInfo.contains(genoName)) {
 					tables.add(genoName);
 				}
 			}
 			rs.close();
 			return tables;
 		} catch (SQLException se) {
 			throw new SQLException(se.getMessage());
 		}
 	}
 
 	public ResultSet getInfoDatum(long entryId, String infoTableName)
 			throws SQLException {
 		String sql = "";
 		try {
 
 			if (!EntryFixedInfo.contains(infoTableName)) {
 				sql = String
 						.format("SELECT * FROM `vcf_analyzer`.`%s` WHERE `EntryId` = '%d'",
 								infoTableName, entryId);
 				ResultSet infoSet = this.stmt.executeQuery(sql);
 
 				return infoSet;
 			}
 			return null;
 		} catch (SQLException se) {
 			throw new SQLException("Invalid Query " + sql);
 		}
 	}
 
 	public ResultSet getIndividuals(long entryId) throws SQLException {
 
 		String sql = "";
 		try {
 			sql = "SELECT * FROM `vcf_analyzer`.`IndividualEntry` WHERE `EntryId` = '"
 					+ entryId + "' ORDER BY `IndID` ASC";
 			ResultSet rs = this.stmt.executeQuery(sql);
 
 			return rs;
 
 		} catch (SQLException se) {
 			throw new SQLException("Invalid Query " + sql);
 		}
 
 	}
 
 	public ResultSet getIndividualDatum(long indId, String genotypeTableName)
 			throws SQLException {
 
 		String sql = "";
 		try {
 			sql = String.format(
 					"SELECT * FROM `vcf_analyzer`.`%s` WHERE `IndID` = '%d'",
 					genotypeTableName, indId);
 			ResultSet infoSet = this.stmt.executeQuery(sql);
 			/*
 			 * if (!infoSet.isBeforeFirst()) { // not empty return infoSet; }
 			 */
 			// return null;
 			return infoSet;
 		} catch (SQLException se) {
 			throw new SQLException("Invalid Query " + sql);
 		}
 	}
 
 	public void CloseConnection() {
 		
 		try
 		{
 			if (this.conn != null) {
 				this.conn.close();
 			}
 			if (this.stmt != null) {
 				this.stmt.close();
 			}
 	
 			if (this.stmtList != null) {
 				for (Statement state : this.stmtList) {
 					state.close();
 				}
 			}
 		}
 		catch( SQLException e)
 		{
 			//do nothing
 		}
 	}
 
 	private boolean hasOpenStatementAndConnection() throws SQLException {
 		return !this.conn.isClosed() && !this.stmt.isClosed();
 	}
 
 	private void reopenConnectionAndStatement() throws SQLException,
 			ClassNotFoundException {
 		if (this.conn == null || this.conn.isClosed())
 			this.conn = DriverManager.getConnection(DB_URL, USER, PASS);
 		if (this.stmt == null || this.stmt.isClosed())
 			this.stmt = this.conn.createStatement();
 	}
 
 	
 
 	/**
 	 * TODO: Finish Testing
 	 * 
 	 * @param tableName
 	 *            , String idName
 	 * 
 	 * @return
 	 * @throws SQLException
 	 * @throws ClassNotFoundException
 	 */
 	private int getHighestId(String tableName, String idName)
 			throws ClassNotFoundException, SQLException {
 		if (!hasOpenStatementAndConnection())
 			reopenConnectionAndStatement();
 		String sql = String.format(
 				"SELECT %s FROM %s ORDER BY %s desc LIMIT 0,1;", idName,
 				tableName, idName);
 		ResultSet rs = this.stmt.executeQuery(sql);
 		return Integer.parseInt(rs.getString("DivID"));
 	}
 
 	protected int upload(String sqlCommand) throws ClassNotFoundException,
 			SQLException {
 		if (!hasOpenStatementAndConnection())
 			reopenConnectionAndStatement();
 		int rs = this.stmt.executeUpdate(sqlCommand);
 		return rs;
 	}
 
 	public void insertEntryPass(int filterId, long entryId, char pass)
 			throws SQLException, ClassNotFoundException {
 		if (!hasOpenStatementAndConnection())
 			reopenConnectionAndStatement();
 
 		String sql = String
 				.format("INSERT into `vcf_analyzer`.`FilterEntryPass` (`FilId`, `EntryId`, `Pass`) VALUES ('%d','%d','%d');",
 						filterId, entryId, pass);
 		this.stmt.executeUpdate(sql);
 	}
 
 	public void insertIndividualPass(int filterId, long entryId, char pass)
 			throws SQLException, ClassNotFoundException {
 		if (!hasOpenStatementAndConnection())
 			reopenConnectionAndStatement();
 
 		String sql = String
 				.format("INSERT into `vcf_analyzer`.`FilterIndividualPass` (`FilId`, `IndID`, `Pass`) VALUES ('%d','%d','%d');",
 						filterId, entryId, pass);
 		this.stmt.executeUpdate(sql);
 	}
 	
 	public ResultSet executeQuery(String sql) throws ClassNotFoundException, SQLException{
 		if (!hasOpenStatementAndConnection())
 			reopenConnectionAndStatement();
 		return this.stmt.executeQuery(sql);
 	}
 	
 }
