 /*
  * Japex ver. 0.1 software ("Software")
  * 
  * Copyright, 2004-2005 Sun Microsystems, Inc. All Rights Reserved.
  * 
  * This Software is distributed under the following terms:
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, is permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistribution in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * 
  * Neither the name of Sun Microsystems, Inc., 'Java', 'Java'-based names,
  * nor the names of contributors may be used to endorse or promote products
  * derived from this Software without specific prior written permission.
  * 
  * The Software is provided "AS IS," without a warranty of any kind. ALL
  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
  * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  * PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS
  * SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE
  * AS A RESULT OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE
  * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE
  * LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
  * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED
  * AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGES.
  * 
  * You acknowledge that the Software is not designed, licensed or intended
  * for use in the design, construction, operation or maintenance of any
  * nuclear facility.
  */
 
 package com.sun.japex;
 
 import java.util.*;
 import java.net.*;
 import java.io.File;
 
 public class DriverInfo extends Params {
     
     String _name;
     JapexClassLoader _classLoader;
     Class _class = null;
     boolean _isNormal = false;
     boolean _computeMeans = true;
     
     TestCaseArrayList[] _testCases;
     TestCaseArrayList _aggregateTestCases;
     
     int _runsPerDriver;
     
     static class JapexClassLoader extends URLClassLoader {
         public JapexClassLoader(URL[] urls) {
             super(urls);
         }        
         public Class findClass(String name) throws ClassNotFoundException {
             return super.findClass(name);
         }        
         public void addURL(URL url) {
             super.addURL(url);
         }
     }
        
     public DriverInfo(String name, boolean isNormal, int runsPerDriver, 
         Properties params) 
     {
         super(params);
         _name = name;
         _isNormal = isNormal;
         _classLoader = newJapexClassLoader();
         
         _runsPerDriver = runsPerDriver;
     }
     
     public void setTestCases(TestCaseArrayList testCases) {
         _testCases = new TestCaseArrayList[_runsPerDriver];
         for (int i = 0; i < _runsPerDriver; i++) {
             _testCases[i] = (TestCaseArrayList) testCases.clone();
         }
         
         _aggregateTestCases = (TestCaseArrayList) testCases.clone();
     }
         
     private void computeMeans() {
         final int runsPerDriver = _testCases.length;
         
         // Avoid re-computing the driver's aggregates
         if (_computeMeans) {
             final int nOfTests = _testCases[0].size();
 
             for (int n = 0; n < nOfTests; n++) {
                 double avgRunsResult = 0.0;
 
                 double[] results = new double[runsPerDriver];
                 
                 // Collect all results
                 for (int i = 0; i < runsPerDriver; i++) {            
                     TestCase tc = (TestCase) _testCases[i].get(n);
                     results[i] = tc.getDoubleParam(Constants.RESULT_VALUE);
                 }
                 
                 TestCase tc = (TestCase) _aggregateTestCases.get(n);
                 tc.setDoubleParam(Constants.RESULT_VALUE, Util.arithmeticMean(results));
                 if (runsPerDriver > 1) {
                     tc.setDoubleParam(Constants.RESULT_VALUE_STDDEV, 
                             runsPerDriver > 1 ? Util.standardDev(results) : 0.0);
                 }
             }
             
             // geometric mean = (sum{i,n} x_i) / n
             double geomMeanresult = 1.0;
             // arithmetic mean = (prod{i,n} x_i)^(1/n)
             double aritMeanresult = 0.0;
             // harmonic mean inverse = sum{i,n} 1/(n * x_i)
             double harmMeanresultInverse = 0.0;
             
             // Re-compute means based on averages for all runs
             Iterator tci = _aggregateTestCases.iterator();
             while (tci.hasNext()) {
                 TestCase tc = (TestCase) tci.next();       
                 double result = tc.getDoubleParam(Constants.RESULT_VALUE);
                 
                 // Compute running means 
                 aritMeanresult += result / nOfTests;
                 geomMeanresult *= Math.pow(result, 1.0 / nOfTests);
                 harmMeanresultInverse += 1.0 / (nOfTests * result);
             }
             
             // Set driver-specific params
             setDoubleParam(Constants.RESULT_ARIT_MEAN, aritMeanresult);
             setDoubleParam(Constants.RESULT_GEOM_MEAN, geomMeanresult);
             setDoubleParam(Constants.RESULT_HARM_MEAN, 1.0 / harmMeanresultInverse);      
             
             // Avoid re-computing these means
             _computeMeans = false;
             
             // If number of runs is just 1, we're done
             if (runsPerDriver == 1) {
                 return;
             }
             
             // geometric mean = (sum{i,n} x_i) / n
             geomMeanresult = 1.0;
             // arithmetic mean = (prod{i,n} x_i)^(1/n)
             aritMeanresult = 0.0;
             // harmonic mean inverse = sum{i,n} 1/(n * x_i)
             harmMeanresultInverse = 0.0;
             
             // Re-compute means based on averages for all runs
             tci = _aggregateTestCases.iterator();
             while (tci.hasNext()) {
                 TestCase tc = (TestCase) tci.next();       
                 double result = tc.getDoubleParam(Constants.RESULT_VALUE_STDDEV);
                 
                 // Compute running means 
                 aritMeanresult += result / nOfTests;
                 geomMeanresult *= Math.pow(result, 1.0 / nOfTests);
                 harmMeanresultInverse += 1.0 / (nOfTests * result);
             }
             
             // Set driver-specific params
             setDoubleParam(Constants.RESULT_ARIT_MEAN_STDDEV, aritMeanresult);
             setDoubleParam(Constants.RESULT_GEOM_MEAN_STDDEV, geomMeanresult);
             setDoubleParam(Constants.RESULT_HARM_MEAN_STDDEV, 1.0 / harmMeanresultInverse);            
         }        
     }
     
     public List getTestCases(int driverRun) {
         return _testCases[driverRun];
     }
     
     public List getAggregateTestCases() {
         computeMeans();  
         return _aggregateTestCases;
     }
     
     JapexDriverBase getJapexDriver() throws ClassNotFoundException {
         String className = getParam(Constants.DRIVER_CLASS);
         if (_class == null) {
             _class = _classLoader.findClass(className);
         }
         
         try {
             Thread.currentThread().setContextClassLoader(_classLoader);
             return (JapexDriverBase) _class.newInstance();
         }
         catch (InstantiationException e) {
             throw new RuntimeException(e);
         }
         catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
         catch (ClassCastException e) {
             throw new RuntimeException("Class '" + className 
                 + "' must extend '" + JapexDriverBase.class.getName() + "'");
         }
     }
     
     public String getName() {
         return _name;
     }    
     
     public boolean isNormal() {
         return _isNormal;
     }
     
     public void serialize(StringBuffer report, int spaces) {
         report.append(Util.getSpaces(spaces) 
             + "<driver name=\"" + _name + "\">\n");
         
         super.serialize(report, spaces + 2);
 
        Iterator tci = getAggregateTestCases().iterator();
         while (tci.hasNext()) {
             TestCase tc = (TestCase) tci.next();
             tc.serialize(report, spaces + 2);
         }            
 
         report.append(Util.getSpaces(spaces) + "</driver>\n");       
     }
     
     private JapexClassLoader newJapexClassLoader() {
         JapexClassLoader result = new JapexClassLoader(new URL[0]);
         String classPath = getParam(Constants.CLASS_PATH);
         if (classPath == null) {
             return result;
         }
         
         StringTokenizer tokenizer = new StringTokenizer(classPath, 
             System.getProperty("path.separator"));
         
 	while (tokenizer.hasMoreTokens()) {
             String path = tokenizer.nextToken();            
             try {
                 result.addURL(new File(path).toURL());
             }
             catch (MalformedURLException e) {
                 // ignore
             }
         }        
         return result;
     }
 }
