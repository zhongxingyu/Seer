 /********************************************************************************
  * CruiseControl, a Continuous Integration Toolkit
  * Copyright (c) 2001, ThoughtWorks, Inc.
  * 651 W Washington Ave. Suite 500
  * Chicago, IL 60661 USA
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions
  * are met:
  * 
  *     + Redistributions of source code must retain the above copyright 
  *       notice, this list of conditions and the following disclaimer. 
  *       
  *     + Redistributions in binary form must reproduce the above 
  *       copyright notice, this list of conditions and the following 
  *       disclaimer in the documentation and/or other materials provided 
  *       with the distribution. 
  *       
  *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the 
  *       names of its contributors may be used to endorse or promote 
  *       products derived from this software without specific prior 
  *       written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ********************************************************************************/
 package net.sourceforge.cruisecontrol.sourcecontrols;
 
 import java.util.*;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 
 import junit.framework.*;
 import net.sourceforge.cruisecontrol.Modification;
 import net.sourceforge.cruisecontrol.CruiseControlException;
 
 /**
  *@author  Robert Watkins
  *@author  Jason Yip, jcyip@thoughtworks.com
  */
 public class CVSTest extends TestCase {
 
 	public CVSTest(java.lang.String testName) {
 		super(testName);
 	}
 
     private Date createDate(String dateString) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
         return formatter.parse(dateString);
     }
 
     public void testParseStream() throws IOException, ParseException {
         CVS cvs = new CVS();
         File testLog = new File("test/net/sourceforge/cruisecontrol/sourcecontrols/cvslog1-11.txt");
        //System.out.println(testLog.getAbsolutePath());
         BufferedInputStream input = new BufferedInputStream(new FileInputStream(testLog));
         List modifications = cvs.parseStream(input);
         input.close();
         Collections.sort(modifications);
 
         assertEquals("Should have returned 3 modifications.", 3, modifications.size());
 
 
         Modification mod1 = new Modification();
         mod1.type = "modified";
         mod1.fileName = "log4j.properties";
         mod1.folderName = "main";
        mod1.modifiedTime = createDate("2002/03/13 13:45:50 GMT-6:00");
         mod1.userName = "alden";
         mod1.comment = "Shortening ConversionPattern so we don't use up all of the available screen space.";
 
         Modification mod2 = new Modification();
         mod2.type = "modified";
         mod2.fileName = "build.xml";
         mod2.folderName = "main";
        mod2.modifiedTime = createDate("2002/03/13 19:56:34 GMT-6:00");
         mod2.userName = "alden";
         mod2.comment = "Added target to clean up test results.";
 
         Modification mod3 = new Modification();
         mod3.type = "modified";
         mod3.fileName = "build.xml";
         mod3.folderName = "main";
        mod3.modifiedTime = createDate("2002/03/15 13:20:28 GMT-6:00");
         mod3.userName = "alden";
         mod3.comment = "enabled debug info when compiling tests.";
 
         assertEquals(mod1, (Modification) modifications.get(0));
         assertEquals(mod2, (Modification) modifications.get(1));
         assertEquals(mod3, (Modification) modifications.get(2));
     }
 
 	public void testBuildHistoryCommand() throws CruiseControlException {
 		Date lastBuildTime = new Date();
 		Date currTime = new Date();
 		CVS element = new CVS();
 		element.setCvsRoot("cvsroot");
 		element.setLocalWorkingCopy(".");
 
 		String[] expectedCommand = new String[]{"cvs", "-d", "cvsroot", "-q", "log",
 				"-N", "-d>" + CVS.formatCVSDate(lastBuildTime)};
 
 		String[] actualCommand = element.buildHistoryCommand(lastBuildTime).getCommandline();
 
 		assertEquals("Mismatched lengths!", expectedCommand.length,
 				actualCommand.length);
 		for (int i = 0; i < expectedCommand.length; i++) {
 			assertEquals(expectedCommand[i], actualCommand[i]);
 		}
 	}
 
 	public void testHistoryCommandNullLocal() throws CruiseControlException {
 		Date lastBuildTime = new Date();
 		Date currTime = new Date();
 
 		CVS element = new CVS();
 		element.setCvsRoot("cvsroot");
 		element.setLocalWorkingCopy(null);
 
 		String[] expectedCommand = new String[] {"cvs", "-d", "cvsroot", "-q", "log",
 				"-N", "-d>" + CVS.formatCVSDate(lastBuildTime)};
 
 		String[] actualCommand =
 				element.buildHistoryCommand(lastBuildTime).getCommandline();
 
 		assertEquals("Mismatched lengths!", expectedCommand.length,
 				actualCommand.length);
 		for (int i = 0; i < expectedCommand.length; i++) {
 			assertEquals(expectedCommand[i], actualCommand[i]);
 		}
 	}
 
 	public void testHistoryCommandNullCVSROOT() throws CruiseControlException {
 		Date lastBuildTime = new Date();
 		Date currTime = new Date();
 		CVS element = new CVS();
 		element.setCvsRoot(null);
 		element.setLocalWorkingCopy(".");
 
 		String[] expectedCommand = new String[]{"cvs", "-q", "log",
 				"-N", "-d>" + CVS.formatCVSDate(lastBuildTime)};
 
 		String[] actualCommand =
 				element.buildHistoryCommand(lastBuildTime).getCommandline();
 		assertEquals("Mismatched lengths!", expectedCommand.length,
 				actualCommand.length);
 		for (int i = 0; i < expectedCommand.length; i++) {
 			assertEquals(expectedCommand[i], actualCommand[i]);
 		}
 	}
 
 	public void testFormatLogDate() {
 		Date may18_2001_6pm =
 				new GregorianCalendar(2001, 4, 18, 18, 0, 0).getTime();
 		assertEquals("2001/05/18 18:00:00 "
 				 + TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT),
 				CVS.LOGDATE.format(may18_2001_6pm));
 	}
 
 	public void testFormatCVSDateGMTPlusZero() {
 		TimeZone.setDefault(TimeZone.getTimeZone("GMT+0:00"));
 		Date may18_2001_6pm =
 				new GregorianCalendar(2001, 4, 18, 18, 0, 0).getTime();
 		assertEquals("2001-05-18 18:00:00 GMT",
 				CVS.formatCVSDate(may18_2001_6pm));
 	}
 
 	public void testFormatCVSDateGMTPlusTen() {
 		TimeZone.setDefault(TimeZone.getTimeZone("GMT+10:00"));
 		Date may18_2001_6pm = new GregorianCalendar(2001, 4, 18, 18, 0, 0).getTime();
 		assertEquals("2001-05-18 08:00:00 GMT",
 				CVS.formatCVSDate(may18_2001_6pm));
 		Date may8_2001_6pm = new GregorianCalendar(2001, 4, 18, 8, 0, 0).getTime();
 		assertEquals("2001-05-17 22:00:00 GMT",
 				CVS.formatCVSDate(may8_2001_6pm));
 	}
 
 	public void testFormatCVSDateGMTMinusTen() {
 		TimeZone.setDefault(TimeZone.getTimeZone("GMT-10:00"));
 		Date may18_2001_6pm = new GregorianCalendar(2001, 4, 18, 18, 0, 0).getTime();
 		assertEquals("2001-05-19 04:00:00 GMT",
 				CVS.formatCVSDate(may18_2001_6pm));
 		Date may8_2001_6pm = new GregorianCalendar(2001, 4, 18, 8, 0, 0).getTime();
 		assertEquals("2001-05-18 18:00:00 GMT",
 				CVS.formatCVSDate(may8_2001_6pm));
 	}
 
 	public static void main(java.lang.String[] args) {
 		junit.textui.TestRunner.run(CVSTest.class);
 	}
 
 }
