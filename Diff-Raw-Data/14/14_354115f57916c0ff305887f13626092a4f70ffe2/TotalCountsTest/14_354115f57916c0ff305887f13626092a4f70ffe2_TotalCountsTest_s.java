 package edu.rit.se.history.tomcat;
 
 import static org.junit.Assert.assertEquals;
 
 import java.sql.ResultSet;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.mysql.jdbc.Connection;
 
 public class TotalCountsTest {
 
 	private static final int TOTAL_CVES = 52;
 	private static RebuildHistory history;
 
 	@BeforeClass
 	public static void initDB() throws Exception {
 		history = new RebuildHistory();
 		history.run(); // only if we think we need to reset
 	}
 
 	@Test
 	public void groundedTheoryCounts() throws Exception {
 		Connection conn = history.getDbUtil().getConnection();
 		ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM CVEGroundedTheory");
 		rs.next();
 		assertEquals("Grounded theory includes all CVEs", TOTAL_CVES, rs.getInt(1));
 		conn.close();
 	}
 
 	@Test
 	public void cvesCounts() throws Exception {
 		Connection conn = history.getDbUtil().getConnection();
 		ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM CVE");
 		rs.next();
 		assertEquals("CVE table includes all CVEs", TOTAL_CVES, rs.getInt(1));
 		conn.close();
 	}
 
 	@Test
 	public void cveAnalysisCounts() throws Exception {
 		Connection conn = history.getDbUtil().getConnection();
 		ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM CVEResults");
 		rs.next();
 		assertEquals("CVE table includes all CVEs", TOTAL_CVES, rs.getInt(1));
 		conn.close();
 	}
 
 	@Test
 	public void allCVEsTraced() throws Exception {
 		Connection conn = history.getDbUtil().getConnection();
 		ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(DISTINCT CVE) FROM CVENonSVNFix");
 		rs.next();
 		assertEquals("CVE table includes all CVEs", TOTAL_CVES, rs.getInt(1));
 		conn.close();
 	}
 
 	@Test
 	public void vulnerableFilesAtReleaseAccountedFor() throws Exception {
 		Connection conn = history.getDbUtil().getConnection();
 		ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM cvenonsvnfix WHERE filepath NOT IN (SELECT filepath FROM filepaths)");
 		rs.next();
 		int actualCount = rs.getInt(1);
 		conn.close();
 		assertEquals("Only one file was added post-release", 1, actualCount);
 
 		// Query to debug this one:
 		// SELECT * FROM CVENonSVNFix cf LEFT OUTER JOIN Filepaths f ON cf.filepath=f.filepath ORDER BY
 		// cf.cve ASC
 		// Another query to debug:
 		// SELECT cve,filepath FROM cvenonsvnfix WHERE filepath NOT IN (SELECT filepath FROM filepaths)
 	}
 
 	@Test
 	public void allSLOCAccountedFor() throws Exception {
 		Connection conn = history.getDbUtil().getConnection();
 		ResultSet rs = conn.createStatement().executeQuery(
 				"SELECT COUNT(*) FROM filepaths WHERE SLOC IS NULL AND (Filepath LIKE '%.java' OR Filepath LIKE '%.c' OR Filepath LIKE '%.h')");
 		rs.next();
 		int actualCount = rs.getInt(1);
 		conn.close();
 		assertEquals("Only one file was added post-release", 0, actualCount);
 		// Query to debug this one:
 		// SELECT * FROM filepaths WHERE SLOC IS NULL AND (Filepath LIKE '%.java' OR Filepath LIKE '%.c' OR
 		// Filepath LIKE '%.h')
 	}
 
 	// @Test
 	// public void fileCounts() throws Exception {
 	// throw new IllegalStateException("unimplemented!");
 	// }
 
 	// @Test
 	// public void cveResultsCounts() throws Exception {
 	// throw new IllegalStateException("unimplemented!");
 	// }
 
 }
