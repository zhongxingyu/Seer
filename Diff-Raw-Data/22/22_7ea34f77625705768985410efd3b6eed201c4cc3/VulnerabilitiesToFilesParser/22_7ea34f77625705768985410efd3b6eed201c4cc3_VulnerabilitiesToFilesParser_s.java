 package edu.rit.se.history.tomcat.parse;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import org.chaoticbits.devactivity.DBUtil;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import com.mysql.jdbc.Connection;
 
 public class VulnerabilitiesToFilesParser {
 
 	public void parse(DBUtil dbUtil, File csv) throws SQLException, IOException {
 		Connection conn = dbUtil.getConnection();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO CVENonSVNFix(CVE,Filepath,TomcatRelease) VALUES (?,?,?)");
 		String line[];
 		CSVReader reader = new CSVReader(new FileReader(csv));
 		reader.readNext(); // skip the header
 		while ((line = reader.readNext()) != null) {
 			ps.setString(1, line[0]); // CVE
 			ps.setString(2, line[1]); // Filepath
			ps.setString(3, "TODO"); // This hasn't been filled out in the GoogleDoc yet
 			ps.addBatch();
 		}
 		ps.executeBatch();
 		conn.close();
 	}
 
 }
