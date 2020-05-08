 /*
  * Sonar GreenPepper Plugin
  * Copyright (C) 2009 SonarSource
  * dev@sonar.codehaus.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.greenpepper;
 
 import org.apache.commons.io.FileUtils;
import org.sonar.api.utils.Logs;
 import org.sonar.api.utils.ParsingUtils;
 import org.sonar.api.utils.XpathParser;
 
import java.io.File;
import java.util.Collection;

public final class GreenPepperReportsParser {

  private GreenPepperReportsParser() {
  }
 
   public final static GreenPepperReport parseReports(File reportsDir) {
     GreenPepperReport testsResult = new GreenPepperReport();
     File[] reports = getReports(reportsDir);
     for (File report : reports) {
       testsResult.addGreenPepperReport(parseReport(report));
     }
     return testsResult;
   }
 
   public final static GreenPepperReport parseReport(File report) {
     try {
       XpathParser parser = new XpathParser();
       parser.parse(report);
       GreenPepperReport testsResult = new GreenPepperReport();
       String testsSuccess = parser.executeXPath("/documents/document/statistics/success");
       testsResult.setTestsSuccess((int) ParsingUtils.parseNumber(testsSuccess));
       String testsError = parser.executeXPath("/documents/document/statistics/error");
       testsResult.setTestsError((int) ParsingUtils.parseNumber(testsError));
       String testsFailure = parser.executeXPath("/documents/document/statistics/failure");
       testsResult.setTestsFailure((int) ParsingUtils.parseNumber(testsFailure));
       String testsIgnored = parser.executeXPath("/documents/document/statistics/ignored");
       testsResult.setTestsIgnored((int) ParsingUtils.parseNumber(testsIgnored));
       return testsResult;
     } catch (Exception e) {
      Logs.INFO.error("Unable to parse : " + report.getAbsolutePath(), e);
       // throw new XmlParserException("Unable to parse GreenPepper report : " + report.getAbsolutePath(), e);
       return new GreenPepperReport();
     }
   }
 
   @SuppressWarnings("unchecked")
   private static File[] getReports(File dir) {
     if (dir == null || !dir.isDirectory() || !dir.exists()) {
       return new File[0];
     }
    Collection<File> files = FileUtils.listFiles(dir, new String[] { "xml" }, true);
     return files.toArray(new File[files.size()]);
   }
 }
