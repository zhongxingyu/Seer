 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.cml.vjetv.reporter.impl;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoSemanticProblem;
 import org.eclipse.vjet.dsf.jst.ProblemSeverity;
 import org.eclipse.vjet.dsf.jstojava.cml.vjetv.model.IHeadLessLauncherResult;
 import org.eclipse.vjet.dsf.jstojava.cml.vjetv.model.IHeadlessLauncherConfigure;
 import org.eclipse.vjet.dsf.jstojava.cml.vjetv.model.impl.EVLauncherResult;
 import org.eclipse.vjet.dsf.jstojava.cml.vjetv.model.impl.VjetvHeadlessConfigure;
 import org.eclipse.vjet.dsf.jstojava.cml.vjetv.reporter.IHeadLessReporter;
 import org.eclipse.vjet.dsf.jstojava.cml.vjetv.reporter.LineNumberComparator;
 import org.eclipse.vjet.dsf.jstojava.cml.vjetv.util.FileOperator;
 
 /**
  * Base reporter
  * 
  * @author <a href="mailto:liama@ebay.com">liama</a>
  * @since JDK 1.5
  */
 public class BaseReporter implements IHeadLessReporter {
 
     /**
      * The value is used for store linenumber comparator
      */
     public final static LineNumberComparator COMPARATOR = new LineNumberComparator();
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.vjet.dsf.jstojava.cml.vjetv.reporter.IHeadLessReporter#generateReport(org.eclipse.vjet.dsf.jstojava.cml.vjetv.model.IHeadLessLauncherResult,
      *      org.eclipse.vjet.dsf.jstojava.cml.vjetv.model.IHeadlessLauncherConfigure)
      */
     @Override
     public void generateReport(IHeadLessLauncherResult result,
             IHeadlessLauncherConfigure conf) {
         if (conf.getReportPath() == null)
             return;
 
         if (conf.getReportType().equalsIgnoreCase(
                 IHeadlessLauncherConfigure.TXT)) {
             generateReportFileViaTxt(result, conf, (EVLauncherResult) result);
         } else if (conf.getReportType().equalsIgnoreCase(
                 IHeadlessLauncherConfigure.XML)) {
             XMLReportGenerator.generateXMLFile(conf, (EVLauncherResult) result); 
         }
     }
 
     /**
      * @param result
      * @param conf
      * @param launcherResult
      *            void
      */
     private void generateReportFileViaTxt(IHeadLessLauncherResult result,
             IHeadlessLauncherConfigure conf, EVLauncherResult launcherResult) {
         String resultPath = conf.getReportPath();
         File tempFile = new File(resultPath);
         File resultFile = null;
         if (tempFile.isDirectory()) {
             resultFile = new File(resultPath + File.separatorChar
                     + "VJETVResult.xml");
         } else {
             resultFile = tempFile;
         }
         if (!resultFile.exists()) {
             try {
                 resultFile.createNewFile();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         FileWriter fw;
         try {
             fw = new FileWriter(resultFile);
             fw.write("VJET Valdation ===>");
             fw.write(new Date().toString() + "\n");
             HashMap map = result.getReportData();
             EVLauncherResult evresult = (EVLauncherResult) result;
             VjetvHeadlessConfigure vconf = (VjetvHeadlessConfigure) conf;
             String reportLevel = vconf.getReportLevel();
             int i = 0; // ID
             fw.write("=====================================" +
                     "============================================" +
                     "============================================\n");
             for (Iterator iterator = map.keySet().iterator(); iterator
                     .hasNext();) {
                 ++i;
                 File file = (File) iterator.next();
                 Object value = map.get(file);
                 fw.write("<" + i + "> JS Path: " + file + "\n");
                 if (value != null) {
                     if (value instanceof String) {
 //                        String new_name = (String) value;
                         fw.write("Error Message :  " + value + "\n");
                         fw
                                 .write("Please check verified JS files writtern by VJO syntax. \n");
                     } else if (value instanceof ArrayList) {
                         ArrayList actualProblems = (ArrayList) value;
                         Collections.sort(actualProblems, COMPARATOR);
                         fw.write(printProblems(actualProblems, evresult,
                                 reportLevel, false, file));
                     }
                 }
             }
             fw.flush();
             fw.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.vjet.dsf.jstojava.cml.vjetv.reporter.IHeadLessReporter#printCurrentStates(java.lang.String)
      */
     @Override
     public void printCurrentStates(String message) {
         System.out.println(message);
     }
 
     /**
      * Print message
      * 
      * @param actualProblems
      *            {@link List}
      * @param result
      *            {@link EVLauncherResult}
      * @param reportLevel
      *            {@link String}
      * @param calc
      * @param valdiateFile
      *            {@link File}
      * @return String
      */
     public String printProblems(List<VjoSemanticProblem> actualProblems,
             EVLauncherResult evresult, String reportLevel, boolean calc,
             File validateFile) {
         StringBuffer message = new StringBuffer();
 //        message.append("=====================================" +
 //                    "============================================" +
 //                    "============================================\n");
        String sources = FileOperator.getSourceFromFile(validateFile);
         boolean hasProblem = false;
         for (VjoSemanticProblem vjoSemanticProblem : actualProblems) {
             if (vjoSemanticProblem.type().equals(ProblemSeverity.error)) {
                 if (calc) {
                     evresult.setErrorNumber(evresult.getErrorNumber() + 1);
                 }
                 
                 if (reportLevel.equalsIgnoreCase(ProblemSeverity.error
                         .toString())
                         || reportLevel.equalsIgnoreCase("ALL")) {
                 	hasProblem=true;
                     printProblem(message, vjoSemanticProblem, sources);
                 }
             } else if (vjoSemanticProblem.type()
                     .equals(ProblemSeverity.warning)) {
                 if (calc) {
                     evresult.setWarningNumber(evresult.getWarningNumber() + 1);
                 }
                 if (reportLevel.equalsIgnoreCase(ProblemSeverity.warning
                         .toString())
                         || reportLevel.equalsIgnoreCase("ALL")) {
                 	hasProblem=true;
                     printProblem(message, vjoSemanticProblem, sources);
                 }
             }
         }
         
         if(hasProblem){
         	return  "Problems with: " + validateFile.getAbsolutePath() + "\n"+ message.toString();
         }
         return null;
        // return message.toString();
     }
 
     /**
      * Print problems
      * 
      * @param message
      *            {@link StringBuffer}
      * @param vjoSemanticProblem
      *            {@link VjoSemanticProblem}
      * @param sources
      *            {@link String}
      */
     protected void printProblem(StringBuffer message,
             VjoSemanticProblem vjoSemanticProblem, String sources) {
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.vjet.dsf.jstojava.cml.vjetv.reporter.IHeadLessReporter#printSummaryInformation(org.eclipse.vjet.dsf.jstojava.cml.vjetv.model.IHeadLessLauncherResult)
      */
     @Override
     public void printSummaryInformation(IHeadLessLauncherResult result) {
         System.out.println(result.getResultInformation());
     }
 }
