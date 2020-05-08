 /*
  * #%L
  * Bitrepository Integrity Service
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 
 package org.bitrepository.integrityservice.reports;
 
 import java.io.File;
 
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.annotations.Test;
 
 import static org.testng.AssertJUnit.assertEquals;
 import static org.testng.AssertJUnit.assertFalse;
 import static org.testng.AssertJUnit.assertTrue;
 
 public class BasicIntegrityReporterTest extends ExtendedTestCase {
     private static final String REPORT_SUMMARY_START = "The following integrity issues where found:\n";
 
     @Test(groups = {"regressiontest"})
     public void deletedFilesTest() throws Exception {
         addDescription("Verifies that the hasIntegrityIssues() reports deleted correctly");
         addStep("Report a delete file for a new Reporter", "hasIntegrityIssues() should return false and the summary " +
                 "report should inform that no issues where found.");
         BasicIntegrityReporter reporter = new BasicIntegrityReporter("CollectionWithIssues", new File("target/"));
         reporter.reportDeletedFile("TestFile");
         assertFalse("Reporter interpreted delete file as a integrity issue", reporter.hasIntegrityIssues());
         String expectedReport = "No integrity issues found";
         assertEquals("Reporter didn't create clean report", expectedReport, reporter.generateSummaryOfReport());
        reporter.generateReport();
     }
 
     @Test(groups = {"regressiontest"})
     public void noIntegrityIssuesTest() {
         addDescription("Verifies that missing files are reported correctly");
 
         addStep("Create a clean reporter", "hasIntegrityIssues() should return false and the summary report should " +
                 "state that no  inform of the missing file.");
         BasicIntegrityReporter reporter = new BasicIntegrityReporter("CollectionWithoutIssues", new File("target/"));
         assertFalse("Reporter interpreted delete file as a integrity issue", reporter.hasIntegrityIssues());
         String expectedReport = "No integrity issues found";
         assertEquals("Reporter didn't create clean report", expectedReport, reporter.generateSummaryOfReport());
     }
 
     @Test(groups = {"regressiontest"})
     public void missingFilesTest()  throws Exception {
         addDescription("Verifies that missing files are reported correctly");
 
         addStep("Report a missing file", "hasIntegrityIssues() should return true and the summary report should " +
                 "correctly inform of the missing file.");
         BasicIntegrityReporter reporter = new BasicIntegrityReporter("CollectionWithIssues", new File("target/"));
         reporter.reportMissingFile("TestFile", "Pillar1");
         assertTrue("Reporter didn't interpreted missing file as a integrity issue", reporter.hasIntegrityIssues());
         String expectedReport = REPORT_SUMMARY_START + "Pillar1 is missing 1 file.";
         assertEquals("Wrong report returned on missing file", expectedReport, reporter.generateSummaryOfReport());
 
         addStep("Report another missing file on the same pillar", "The summary report should be update with the additional missing file.");
         reporter.reportMissingFile("TestFile2", "Pillar1");
         expectedReport = REPORT_SUMMARY_START + "Pillar1 is missing 2 files.";
         assertEquals("Wrong report returned on missing file", expectedReport, reporter.generateSummaryOfReport());
 
         addStep("Report a missing file on another pillar",
                 "The summary report should be update with the new pillar problem.");
         reporter.reportMissingFile("TestFile3", "Pillar2");
         expectedReport = REPORT_SUMMARY_START + "Pillar1 is missing 2 files." + "\nPillar2 is missing 1 file.";
         assertEquals("Wrong report returned on missing file", expectedReport, reporter.generateSummaryOfReport());
     }
 
     @Test(groups = {"regressiontest"})
     public void checksumIssuesTest() throws Exception {
         addDescription("Verifies that missing files are reported correctly");
 
         addStep("Report a checksum issue", "hasIntegrityIssues() should return true and the summary report should " +
                 "correctly inform of the checksum issue.");
         BasicIntegrityReporter reporter = new BasicIntegrityReporter("CollectionWithIssues", new File("target/"));
         reporter.reportChecksumIssue("TestFile", "Pillar1");
         assertTrue("Reporter didn't interpreted checksum issue as a integrity issue", reporter.hasIntegrityIssues());
         String expectedReport = REPORT_SUMMARY_START + "Pillar1 has 1 potentially corrupt file.";
         assertEquals("Wrong report returned on checksum issue", expectedReport, reporter.generateSummaryOfReport());
 
         addStep("Report another checksum issue on the same pillar", "The summary report should be update with the " +
                 "additional checksum issue.");
         reporter.reportChecksumIssue("TestFile2", "Pillar1");
         expectedReport = REPORT_SUMMARY_START + "Pillar1 has 2 potentially corrupt files.";
         assertEquals("Wrong report returned on checksum issue", expectedReport, reporter.generateSummaryOfReport());
 
         addStep("Report a checksum issue on another pillar",
                 "The summary report should be update with the new pillar problem.");
         reporter.reportChecksumIssue("TestFile3", "Pillar2");
         expectedReport = REPORT_SUMMARY_START + "Pillar1 has 2 potentially corrupt files." + "\nPillar2 has 1 " +
                 "potentially corrupt file.";
         assertEquals("Wrong report returned on checksum issue", expectedReport, reporter.generateSummaryOfReport());
     }
 
     @Test(groups = {"regressiontest"})
     public void missingChecksumTest() throws Exception {
         addDescription("Verifies that missing checksums are reported correctly");
 
         addStep("Report a missing checksum", "hasIntegrityIssues() should return true and the summary report should " +
                 "correctly inform of the missing checksum.");
         BasicIntegrityReporter reporter = new BasicIntegrityReporter("CollectionWithIssues", new File("target/"));
         reporter.reportMissingChecksum("TestChecksum", "Pillar1");
         assertTrue("Reporter didn't interpreted missing checksum as a integrity issue", reporter.hasIntegrityIssues());
         String expectedReport = REPORT_SUMMARY_START + "Pillar1 is missing 1 checksum.";
         assertEquals("Wrong report returned on missing checksum", expectedReport, reporter.generateSummaryOfReport());
 
         addStep("Report another missing checksum on the same pillar", "The summary report should be update with the " +
                 "additional missing checksum.");
         reporter.reportMissingChecksum("TestChecksum2", "Pillar1");
         expectedReport = REPORT_SUMMARY_START + "Pillar1 is missing 2 checksums.";
         assertEquals("Wrong report returned on missing checksum", expectedReport, reporter.generateSummaryOfReport());
 
         addStep("Report a missing checksum on another pillar",
                 "The summary report should be update with the new pillar problem.");
         reporter.reportMissingChecksum("TestChecksum3", "Pillar2");
         expectedReport = REPORT_SUMMARY_START + "Pillar1 is missing 2 checksums." + "\nPillar2 is missing 1 checksum.";
         assertEquals("Wrong report returned on missing checksum", expectedReport, reporter.generateSummaryOfReport());
     }
 
 
     @Test(groups = {"regressiontest"})
     public void obsoleteChecksumTest() throws Exception {
         addDescription("Verifies that obsolete checksums are reported correctly");
 
         addStep("Report a obsolete checksum", "hasIntegrityIssues() should return true and the summary report should " +
                 "correctly inform of the obsolete checksum.");
         BasicIntegrityReporter reporter = new BasicIntegrityReporter("CollectionWithIssues", new File("target/"));
         reporter.reportObsoleteChecksum("TestChecksum", "Pillar1");
         assertTrue("Reporter didn't interpreted obsolete checksum as a integrity issue", reporter.hasIntegrityIssues());
         String expectedReport = REPORT_SUMMARY_START + "Pillar1 has 1 obsolete checksum.";
         assertEquals("Wrong report returned on obsolete checksum", expectedReport, reporter.generateSummaryOfReport());
 
         addStep("Report another obsolete checksum on the same pillar", "The summary report should be update with the " +
                 "additional obsolete checksum.");
         reporter.reportObsoleteChecksum("TestChecksum2", "Pillar1");
         expectedReport = REPORT_SUMMARY_START + "Pillar1 has 2 obsolete checksums.";
         assertEquals("Wrong report returned on obsolete checksum", expectedReport, reporter.generateSummaryOfReport());
 
         addStep("Report a obsolete checksum on another pillar",
                 "The summary report should be update with the new pillar problem.");
         reporter.reportObsoleteChecksum("TestChecksum3", "Pillar2");
         expectedReport = REPORT_SUMMARY_START + "Pillar1 has 2 obsolete checksums." + "\nPillar2 has 1 obsolete checksum.";
         assertEquals("Wrong report returned on obsolete checksum", expectedReport, reporter.generateSummaryOfReport());
     }
 }
