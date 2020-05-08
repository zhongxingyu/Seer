 /*
  * Copyright 2007 Pentaho Corporation.  All rights reserved. 
  * This software was developed by Pentaho Corporation and is provided under the terms 
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
  * this file except in compliance with the license. If you need a copy of the license, 
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
  * BI Platform.  The Initial Developer is Pentaho Corporation.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
  * the license for the specific language governing your rights and limitations.
  * 
  */
 package org.pentaho.versionchecker;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 public class DefaultConsoleResultProcessorTest extends TestCase implements IVersionCheckDataProvider,
     IVersionCheckErrorHandler, IVersionCheckResultHandler {
   protected Throwable error = null;
 
   protected String results = null;
 
   public void testDefaultConsoleResultProcessor() {
     DefaultConsoleResultProcessor rp = new DefaultConsoleResultProcessor();
     ByteArrayOutputStream bs = new ByteArrayOutputStream();
     PrintStream ps = new PrintStream(bs);
     rp.setOutput(ps);
 
     VersionChecker vc = new VersionChecker();
     vc.setDataProvider(this);
     vc.addErrorHandler(this);
     vc.addResultHandler(this);
     vc.addResultHandler(rp);
     vc.performCheck(true);
 
     assertNull(error);
   }
 
   public String getApplicationID() {
     return "POBS"; //$NON-NLS-1$
   }
 
   public String getApplicationVersion() {
     return "1.6.0.RC1.400"; //$NON-NLS-1$
   }
 
   public String getBaseURL() {
     // TODO Auto-generated method stub
    return "http://www.pentaho.com/versioncheck/?protocolVer=1.0&depth=154"; //$NON-NLS-1$
   }
 
   public Map getExtraInformation() {
     // TODO Auto-generated method stub
     return null;
   }
   
   public int getDepth() {
     return 154;
   }
 
   public String getGuid() {
     // TODO Auto-generated method stub
     return "0000-0000-0000-0000"; //$NON-NLS-1$
   }
 
   public void handleException(Exception e) {
     e.printStackTrace();
     error = e;
   }
 
   public void processResults(String localResults) {
     System.out.println("RESULTS: "+localResults+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
     this.results = localResults;
   }
 
   public void setVersionRequestFlags(int value) {
     // TODO Auto-generated method stub
     
   }
 }
