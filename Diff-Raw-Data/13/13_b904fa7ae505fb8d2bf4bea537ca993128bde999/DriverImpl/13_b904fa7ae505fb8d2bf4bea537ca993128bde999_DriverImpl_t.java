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
 import java.io.FilenameFilter;
 
 public class DriverImpl extends ParamsImpl implements Driver {
     
     String _name;
     boolean _isNormal = false;
     boolean _computeMeans = true;
     
     TestCaseArrayList[] _testCases;
     TestCaseArrayList _aggregateTestCases;
     
     int _runsPerDriver;
     
    static Class _class = null;
     static JapexClassLoader _classLoader;
 
     /**
      * Set the parent class loader to null in order to force the use of 
      * the bootstrap classloader. The boostrap class loader does not 
      * have access to the system's class path.
      */ 
     static class JapexClassLoader extends URLClassLoader {
         public JapexClassLoader(URL[] urls) {
             super(urls, null);
         }          
         public Class findClass(String name) throws ClassNotFoundException {
             // Delegate when loading Japex classes, excluding JDSL drivers
             if (name.startsWith("com.sun.japex.") && !name.startsWith("com.sun.japex.jdsl.")) {
                 return DriverImpl.class.getClassLoader().loadClass(name);
             }
             
             // Otherwise, use class loader based on japex.classPath only
             return super.findClass(name);
         }        
         public void addURL(URL url) {
             super.addURL(url);
         }
     }
     
        
     public DriverImpl(String name, boolean isNormal, int runsPerDriver, 
         ParamsImpl params) 
     {
         super(params);
         _name = name;
         _isNormal = isNormal;
         _runsPerDriver = runsPerDriver;
         initJapexClassLoader();        
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
                     TestCaseImpl tc = (TestCaseImpl) _testCases[i].get(n);
                     results[i] = tc.getDoubleParam(Constants.RESULT_VALUE);
                 }
                 
                 TestCaseImpl tc = (TestCaseImpl) _aggregateTestCases.get(n);
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
                 TestCaseImpl tc = (TestCaseImpl) tci.next();       
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
                 TestCaseImpl tc = (TestCaseImpl) tci.next();       
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
        synchronized(this) {
            if (_class == null) {
                _class = _classLoader.findClass(className);
            }
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
         
        /*
          * Calling getAggregateTestCases() forces call to computeMeans(). This
          * is necessary before serializing driver params.
          */
         Iterator tci = getAggregateTestCases().iterator();
        
         // Serialize driver params
         super.serialize(report, spaces + 2);
 
         // Serialize each test case
         while (tci.hasNext()) {
             TestCaseImpl tc = (TestCaseImpl) tci.next();
             tc.serialize(report, spaces + 2);
         }            
 
         report.append(Util.getSpaces(spaces) + "</driver>\n");       
     }
     
     /**
      * Initializes the Japex class loader. A single class loader will be
      * created for all drivers. Thus, if japex.classPath is defined as
      * a driver's property, it will be ignored.
      */ 
    synchronized private void initJapexClassLoader() {
         // Initialize class loader only once
         if (_classLoader != null) {
             return;
         }
 
         _classLoader = new JapexClassLoader(new URL[0]);
         String classPath = getParam(Constants.CLASS_PATH);
         if (classPath == null) {
             return;
         }
         
         String pathSep = System.getProperty("path.separator");
         String fileSep = System.getProperty("file.separator");
         StringTokenizer tokenizer = new StringTokenizer(classPath, pathSep); 
         
         // TODO: Ensure that this code works on Windows too!
 	while (tokenizer.hasMoreTokens()) {
             String path = tokenizer.nextToken();            
             try {
                 boolean lookForJars = false;
                 
                 // Strip off '*.jar' at the end if present
                 if (path.endsWith("*.jar")) {
                     int k = path.lastIndexOf('/');
                     path = (k >= 0) ? path.substring(0, k + 1) : "./";
                     lookForJars = true;
                 }
                 
                 // Create a file from the resulting path
                 File file = new File(path);
                 
                 // If a directory, add all '.jar'
                 if (file.isDirectory() && lookForJars) {
                     String children[] = file.list(
                         new FilenameFilter() {
                             public boolean accept(File dir, String name) {
                                 return name.endsWith(".jar");
                             }
                         });
                         
                     for (String c : children) {
                         _classLoader.addURL(new File(path + fileSep + c).toURL());
                     }
                 }
                 else {
                     _classLoader.addURL(file.toURL());
                 }
             }
             catch (MalformedURLException e) {
                 // ignore
             }
         }        
     }
 }
