 /*
     This file is part of  EasyTest CodeGen, a project to generate 
     JUnit test cases  from source code in EasyTest Template format and  helping to keep them in sync
     during refactoring.
   EasyTest CodeGen, a tool provided by
 	EaseTech Organization Under Apache License 2.0 
 	http://www.apache.org/licenses/LICENSE-2.0.txt
 */
 
 package org.easetech.easytest.codegen;
 
 /**
 * An interface for naming strategy for class, package, test class, test case etc..
 *
 * @author Ravi Polampelli
 *
 */
 
 public interface INamingStrategy extends IConfigurableStrategy {
 
    public void setSubPackage(String identifier);
 
     public void setTestInTest(boolean value);
 
     public boolean isTestPackageName(String packageName);
 
     public boolean isTestClassName(String fullClassName);
 
     public String stripParentPackage(String fullClassName);
 
     public String getTestCaseName(String fullClassName);
 
     public String getPackageName(String fullClassName);
 
     public String getTestSuiteName(String packageName);
 
     public String getTestPackageName(String packageName);
 
     public String getFullTestCaseName(String fullClassName);
 
     public String getFullTestSuiteName(String packageName);
 
     public String getTestMethodName(String methodName);
 
     public String getTestAccessorName(String prefixSet, String prefixGet, String accessorName);
 }
