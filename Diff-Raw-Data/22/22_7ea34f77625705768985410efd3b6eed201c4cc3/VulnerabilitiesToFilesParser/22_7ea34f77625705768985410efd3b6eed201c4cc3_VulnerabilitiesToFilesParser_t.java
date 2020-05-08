 package edu.rit.se.history.tomcat.parse;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
import java.sql.Types;
 
 import org.chaoticbits.devactivity.DBUtil;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import com.mysql.jdbc.Connection;
 
 public class VulnerabilitiesToFilesParser {
 
 	public void parse(DBUtil dbUtil, File csv) throws SQLException, IOException {
 		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO CVENonSVNFix(CVE,Filepath,Component,UtilComponent,SLOCAdded,SLOCDeleted) "
				+ "VALUES (?,?,?,?,?,?)");
 		String line[];
 		CSVReader reader = new CSVReader(new FileReader(csv));
 		reader.readNext(); // skip the header
 		while ((line = reader.readNext()) != null) {
 			ps.setString(1, line[0]); // CVE
 			ps.setString(2, line[1]); // Filepath
			ps.setString(3, line[2]); // Component
			ps.setString(4, line[3]); // UtilComponent (if it exists)
			if (line.length > 4) {
				setIntOrNull(ps, 5, line[4]); // SLOCAdded
				setIntOrNull(ps, 6, line[5]); // SLOCDeleted
			}
 			ps.addBatch();
 		}
 		ps.executeBatch();
 		conn.close();
 	}
 
	private void setIntOrNull(PreparedStatement ps, int psI, String str) throws SQLException {
		try {
			ps.setInt(psI, Integer.valueOf(str)); // SLOCAdded
		} catch (NumberFormatException e) {
			ps.setNull(psI, Types.INTEGER);
		}
	}

 }
