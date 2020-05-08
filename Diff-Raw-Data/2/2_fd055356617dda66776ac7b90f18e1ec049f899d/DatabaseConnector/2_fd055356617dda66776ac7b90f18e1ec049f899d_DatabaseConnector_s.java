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
 
 	private static final ArrayList<String> EntryFixedInfo = new ArrayList<String>(
 			Arrays.asList("CHROM", "FILTER", "ID", "POS", "REF", "QUAL", "ALT"));
 
 	private Connection conn;
 	private Statement stmt;
 
 	public DatabaseConnector() throws SQLException, ClassNotFoundException {
 		
 		try{
 			this.conn = null;
 			this.stmt = null;
 	
 			Class.forName(JDBC_DRIVER);
 	
 			conn = DriverManager.getConnection(DB_URL, USER, PASS);
 			stmt = conn.createStatement();
 		}
 		catch (Exception e) {
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
 			throw new SQLException("Invalid Query" + sql);
 		}
 	}
 
 	public String getVcfHeader(long vcfId) throws IllegalArgumentException,
 			SQLException {
 		String sql = "";
 		try {
			sql = "SELECT `VcfHeader` FROM `vcf_analyzer`.`Vcf` WHERE `VcfId` = '"
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
 			throw new SQLException("Invalid Query" + sql);
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
 			throw new SQLException("Invalid Query" + sql);
 		}
 	}
 
 	public void getInfoData(long entryId, ArrayList<String> infoTableName,
 			ArrayList<ResultSet> infoData) throws SQLException {
 		String sql = "";
 		try {
 			sql = "SELECT `InfoName` FROM `vcf_analyzer`.`InfoTable` ORDER BY `InfoName` ASC";
 			ResultSet rs = this.stmt.executeQuery(sql);
 			ArrayList<String> names = new ArrayList<String>();
 			while (rs.next()) {
 				String infoName = rs.getString("InfoName");
 				if (!EntryFixedInfo.contains(infoName)) {
 					sql = String
 							.format("SELECT * FROM `vcf_analyzer`.`%s` WHERE `entryId` = '%d'",
 									infoName, entryId);
 					ResultSet infoSet = this.stmt.executeQuery(sql);
 					if (!infoSet.isBeforeFirst()) {
 						// not empty
 						infoData.add(infoSet);
 						infoTableName.add(infoName);
 					}
 				}
 
 			}
 
 		} catch (SQLException se) {
 			throw new SQLException("Invalid Query" + sql);
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
 			throw new SQLException("Invalid Query" + sql);
 		}
 
 	}
 
 	public ArrayList<ResultSet> getIndividualData(long indId,
 			ArrayList<String> genotypeTableName) throws SQLException {
 		ArrayList<ResultSet> genotypeData = new ArrayList<ResultSet>();
 
 		String sql = "";
 		try {
 			for (String tableName : genotypeTableName) {
 				sql = String
 						.format("SELECT * FROM `vcf_analyzer`.`%s` WHERE `IndID` = '%d'",
 								tableName, indId);
 				ResultSet infoSet = this.stmt.executeQuery(sql);
 				if (!infoSet.isBeforeFirst()) {
 					// not empty
 					genotypeData.add(infoSet);
 				} else {
 					genotypeData.add(null);
 				}
 			}
 			return genotypeData;
 		} catch (SQLException se) {
 			throw new SQLException("Invalid Query" + sql);
 		}
 	}
 
 	public void CloseConnection() throws SQLException {
 		if (this.conn != null) {
 			this.conn.close();
 		}
 		if (this.stmt != null) {
 			this.stmt.close();
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
 	 * 
 	 * TODO consider refactoring to one general upload method
 	 * 
 	 * @param name
 	 * 
 	 * @param chromosome
 	 * @param position
 	 * @param divValue
 	 * @throws ClassNotFoundException
 	 * @throws SQLException
 	 */
 	protected void uploadDivergence(String name, String chromosome,
 			int position, int divValue) throws ClassNotFoundException,
 			SQLException {
 		if (!hasOpenStatementAndConnection())
 			reopenConnectionAndStatement();
 		String sql = String
 				.format("INSERT into `Divergence` (`DivName`, `Chromosome`, `Position`, `DivValue`) VALUES ('%s','%s','%i','%i');",
 						name, chromosome, position, divValue);
 		ResultSet rs = this.stmt.executeQuery(sql);
 		while (rs.next()) {
 			System.out.println(rs.toString());
 		}
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
 
 	/**
 	 * TODO consider refactoring to one general upload method
 	 * 
 	 * @param annoName
 	 * @throws ClassNotFoundException
 	 * @throws SQLException
 	 */
 	protected void uploadAnnotation(String annoName, String chromosome,
 			int startPosition, int endPosition, String geneName, String geneDirection) throws ClassNotFoundException,
 			SQLException {
 		if (!hasOpenStatementAndConnection())
 			reopenConnectionAndStatement();
 		String sql = String
 				.format("INSERT into `Annotation` (`Chromosome`, `StartPosition`, `EndPosition`, `GeneName`, `GeneDirection`, `AnnoName`) VALUES ('%s','%i','%i','%s','%s','%s');",
 						chromosome, startPosition, endPosition,geneName, geneDirection,annoName);
 		ResultSet rs = this.stmt.executeQuery(sql);
 		while (rs.next()) {
 			System.out.println(rs.toString());
 		}
 	}
 
 
 
 }
