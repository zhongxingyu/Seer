 package org.panda.mts;
 
 import org.junit.Test;
 
 public class TestReportService {

 	public void testUnzipReportFiles() {
 		ReportService rs = new ReportService();
 		rs.unzipRequestFiles();
 	}
 
	@Test
 	public void testZipResponseFiles() {
 		ReportService rs = new ReportService();
 		rs.zipResponseFiles();
 	}
 }
