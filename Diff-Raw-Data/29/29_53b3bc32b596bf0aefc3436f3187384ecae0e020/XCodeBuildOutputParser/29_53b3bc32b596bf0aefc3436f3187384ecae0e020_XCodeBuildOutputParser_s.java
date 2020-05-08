 /*
  *  Copyright 2010 Felix Schulze
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package de.felixschulze.teamcity.xcode.tools;
 
 import jetbrains.buildServer.agent.BuildProgressLogger;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * XCodeBuildOutputParser parses a String and sends a unit test service message to TeamCity.
  *
  * @author <a href="mailto:code@felixschulze.de">Felix Schulze</a>
  */
 
 public class XCodeBuildOutputParser {
 
     private static Pattern START_SUITE = Pattern.compile("Test Suite '(\\S+)'.*started at\\s+(.*)", Pattern.DOTALL);
     private static Pattern END_SUITE = Pattern.compile("Test Suite '(\\S+)'.*finished at\\s+(.*).", Pattern.DOTALL);
     private static Pattern START_TESTCASE = Pattern.compile("Test Case '-\\[\\S+\\s+(\\S+)\\]' started.(.*)", Pattern.DOTALL);
     private static Pattern END_TESTCASE = Pattern.compile("Test Case '-\\[\\S+\\s+(\\S+)\\]' passed \\((.*) seconds\\).(.*)", Pattern.DOTALL);
     private static Pattern ERROR_TESTCASE = Pattern.compile("(.*): error: -\\[(\\S+) (\\S+)\\] : (.*)", Pattern.DOTALL);
     private static Pattern FAILED_TESTCASE = Pattern.compile("Test Case '-\\[\\S+ (\\S+)\\]' failed \\((\\S+) seconds\\).(.*)", Pattern.DOTALL);
 
     private static Pattern LINE_COVERAGE = Pattern.compile("lines......: ([0-9]*.[0-9])*% \\(([0-9]*) of ([0-9]*) lines\\)(.*)", Pattern.DOTALL);
 
     private final BuildProgressLogger buildListener;
     private final Boolean ignoreUnitTests;
 
     public XCodeBuildOutputParser(BuildProgressLogger buildListener, Boolean ignoreUnitTests) {
         this.buildListener = buildListener;
         this.ignoreUnitTests = ignoreUnitTests;
     }
 
     protected void handleLine(String line) {
         Matcher m;
         if (!ignoreUnitTests) {
             m = START_SUITE.matcher(line);
             if (m.matches()) {
                buildListener.message("##teamcity[testSuiteStarted name='" + m.group(1) + "']");
                 return;
             }
 
             m = END_SUITE.matcher(line);
             if (m.matches()) {
                buildListener.message("##teamcity[testSuiteFinished name='" + m.group(1) + "']");
                 return;
             }
 
             m = START_TESTCASE.matcher(line);
             if (m.matches()) {
                buildListener.message("##teamcity[testStarted name='" + m.group(1) + "']");
                 return;
             }
 
             m = END_TESTCASE.matcher(line);
             if (m.matches()) {
                buildListener.message("##teamcity[testFinished name='" + m.group(1) + "' duration='" + m.group(2) + "']");
                 return;
             }
 
             m = ERROR_TESTCASE.matcher(line);
             if (m.matches()) {
                buildListener.message("##teamcity[testFailed name='" + m.group(3) + "' message='" + m.group(4) + "']");
                 return;
             }
 
             m = FAILED_TESTCASE.matcher(line);
             if (m.matches()) {
                buildListener.message("##teamcity[testFailed name='" + m.group(1) + "' duration='" + m.group(2) + "']");
                 return;
             }
         }
 
         m = LINE_COVERAGE.matcher(line);
         if (m.matches()) {
             buildListener.message("##teamcity[buildStatisticValue key='CodeCoverageL' value='"+m.group(1)+"']");
             buildListener.message("##teamcity[buildStatisticValue key='CodeCoverageAbsLCovered' value='"+m.group(1)+"']");
             buildListener.message("##teamcity[buildStatisticValue key='CodeCoverageAbsLTotal' value='"+m.group(3)+"']");
             return;
         }
 
 
     }
 }
