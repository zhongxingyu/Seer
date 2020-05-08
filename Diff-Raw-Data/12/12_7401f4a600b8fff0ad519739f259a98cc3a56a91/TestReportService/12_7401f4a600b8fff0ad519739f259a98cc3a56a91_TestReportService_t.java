 package org.panda.mts;
 
 import org.junit.Test;
 
 public class TestReportService {
	@Test
 	public void testUnzipReportFiles() {
 		ReportService rs = new ReportService();
 		rs.unzipRequestFiles();
 	}
 
	
 	public void testZipResponseFiles() {
 		ReportService rs = new ReportService();
 		rs.zipResponseFiles();
 	}
 }
