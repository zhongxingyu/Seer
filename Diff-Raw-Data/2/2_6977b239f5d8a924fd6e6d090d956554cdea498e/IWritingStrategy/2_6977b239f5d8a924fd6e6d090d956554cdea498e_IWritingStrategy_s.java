 /*
     This file is part of  EasyTest CodeGen, a project to generate 
     JUnit test cases  from source code in EasyTest Template format and  helping to keep them in sync
     during refactoring.
  	EasyTest CodeGen, a tool provided by
 	EaseTech Organization Under Apache License 2.0 
 	http://www.apache.org/licenses/LICENSE-2.0.txt
 */
 package org.easetech.easytest.codegen;
 
 import java.util.List;
 
 /**
 * An interface for writing the generated test cases, converter classes, test data to the respective files 
 *
 * @author Ravi Polampelli
 *
 */
 
 
 public interface IWritingStrategy extends IConfigurableStrategy {
 
     public void indent(StringBuffer inOutCode);
 
     public StringBuffer loadClassSource(String root, String fullClassName);
 
     public void writeClassSource(String root, String fullClassName, StringBuffer inCode);
     
     public void writeTestDataFile(String root, String fullClassName, TestCaseVO testCaseVO, Properties seedData);
 
     public boolean isExistingAndNewer(String rootInQuestion, String fullClassNameInQuestion,
                                       String rootReference,  String fullClassNameReference);
 
 	public void writeConverterSources(String outputRoot,
 			String packageName, Map<String, StringBuffer> convertersMap, String overwriteConverters);
 	
 	 public StringBuffer loadSourceClassSource(String fullClassName, String path);
 
 	public String translateClassNameToFileName(String name);
     
 
 }
