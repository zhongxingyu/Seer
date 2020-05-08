 /*
     This file is part of  EasyTest CodeGen, a project to generate 
     JUnit test cases  from source code in EasyTest Template format and  helping to keep them in sync
     during refactoring.
   EasyTest CodeGen, a tool provided by
 	EaseTech Organization Under Apache License 2.0 
 	http://www.apache.org/licenses/LICENSE-2.0.txt
 */
 package org.easetech.easytest.codegen;
 
 import com.sun.javadoc.ClassDoc;
 import com.sun.javadoc.PackageDoc;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 /**
 * An interface for test code generation strategy for suite, class, test case etc..
 *
 * @author Ravi Polampelli
 *
 */
 
 public interface ITestingStrategy extends IConfigurableStrategy {
 
     // one test suite per package
     public boolean isTestablePackage(PackageDoc doc, INamingStrategy naming);
 
    public Properties getTestSuiteProperties(PackageDoc[] packageDocs, int indexPackage, INamingStrategy naming, Properties properties);
 
    public boolean codeTestSuite(PackageDoc[] packageDocs, int indexPackage, INamingStrategy naming, StringBuffer newCode, Properties properties);
 
     // one test case per public (non abstract) class
     public boolean isTestableClass(ClassDoc doc, INamingStrategy naming);
 
     public Properties getTestCaseProperties(TestCaseVO testCaseVO);
 
     public boolean codeTestCase(TestCaseVO testCaseVO);
 
     //
     public boolean isValid(String sourceCode);
 
     public boolean merge(StringBuffer inOutCode, StringBuffer inCode, String fullClassName);
 
 
     // one test per public method name (or less in case of combined tests)
     /*
     public boolean isTestableMethod(MethodDoc doc);
 
     public Properties getTestProperties(MethodDoc[] methodDocs, int index, ClassDoc classDoc, PackageDoc packageDoc, INamingStrategy naming, Properties properties);
 
     public boolean codeTest(MethodDoc[] methodDocs, int index, ClassDoc classDoc, PackageDoc packageDoc, INamingStrategy naming, StringBuffer newCode, Properties properties);
     */
 }
