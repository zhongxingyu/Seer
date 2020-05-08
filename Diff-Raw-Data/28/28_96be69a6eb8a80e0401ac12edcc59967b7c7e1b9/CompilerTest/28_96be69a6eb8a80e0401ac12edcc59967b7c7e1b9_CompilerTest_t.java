 /**
  * MIPL: Mining Integrated Programming Language
  *
  * File: CompilerTest.java
  * Author: Akshai Sarma <as4107@columbia.edu>
  * Reviewer: Wonjoon Song <dws2127@columbia.edu>
  * Description: Tests from top-bottom of full compilation
  *				and execution of the test input .mipl files
  */
 
 package edu.columbia.mipl.compiler;
 
 import java.io.*;
 import java.util.*;
 import java.lang.Runtime;
 
 import junit.framework.TestCase;
 
 import edu.columbia.mipl.runtime.*;
 import edu.columbia.mipl.runtime.execute.*;
 
 public class CompilerTest extends TestCase {
 
 	static String testInputPath = "test/input";
	static String miplMainCommand = "java -ea -esa -cp build:./lib/bcel-5.2.jar:./lib/log4j-1.2.16.jar edu.columbia.mipl.Main -writer JavaSrc -output MiplProgram ";
 	static String compiledInputCommand = "javac -cp build build/MiplProgram.java";
 	static String runCompiledInputCommand = "java -ea -esa -cp build:./lib/hadoop/asm-3.2.jar:./lib/hadoop/aspectjrt-1.6.5.jar:./lib/hadoop/aspectjtools-1.6.5.jar:./lib/hadoop/commons-beanutils-1.7.0.jar:./lib/hadoop/commons-beanutils-core-1.8.0.jar:./lib/hadoop/commons-cli-1.2.jar:./lib/hadoop/commons-codec-1.4.jar:./lib/hadoop/commons-collections-3.2.1.jar:./lib/hadoop/commons-configuration-1.6.jar:./lib/hadoop/commons-daemon-1.0.1.jar:./lib/hadoop/commons-digester-1.8.jar:./lib/hadoop/commons-el-1.0.jar:./lib/hadoop/commons-httpclient-3.0.1.jar:./lib/hadoop/commons-lang-2.4.jar:./lib/hadoop/commons-logging-1.1.1.jar:./lib/hadoop/commons-logging-api-1.0.4.jar:./lib/hadoop/commons-math-2.1.jar:./lib/hadoop/commons-net-1.4.1.jar:./lib/hadoop/core-3.1.1.jar:./lib/hadoop/hadoop-capacity-scheduler-1.0.1.jar:./lib/hadoop/hadoop-fairscheduler-1.0.1.jar:./lib/hadoop/hadoop-thriftfs-1.0.1.jar:./lib/hadoop/hsqldb-1.8.0.10.jar:./lib/hadoop/hsqldb-1.8.0.10.LICENSE.txt:./lib/hadoop/jackson-core-asl-1.8.8.jar:./lib/hadoop/jackson-mapper-asl-1.8.8.jar:./lib/hadoop/jasper-compiler-5.5.12.jar:./lib/hadoop/jasper-runtime-5.5.12.jar:./lib/hadoop/jdeb-0.8.jar:./lib/hadoop/jdiff:./lib/hadoop/jdiff/hadoop_0.17.0.xml:./lib/hadoop/jdiff/hadoop_0.18.1.xml:./lib/hadoop/jdiff/hadoop_0.18.2.xml:./lib/hadoop/jdiff/hadoop_0.18.3.xml:./lib/hadoop/jdiff/hadoop_0.19.0.xml:./lib/hadoop/jdiff/hadoop_0.19.1.xml:./lib/hadoop/jdiff/hadoop_0.19.2.xml:./lib/hadoop/jdiff/hadoop_0.20.1.xml:./lib/hadoop/jdiff/hadoop_0.20.205.0.xml:./lib/hadoop/jdiff/hadoop_1.0.0.xml:./lib/hadoop/jdiff/hadoop_1.0.1.xml:./lib/hadoop/jersey-core-1.8.jar:./lib/hadoop/jersey-json-1.8.jar:./lib/hadoop/jersey-server-1.8.jar:./lib/hadoop/jets3t-0.6.1.jar:./lib/hadoop/jetty-6.1.26.jar:./lib/hadoop/jetty-util-6.1.26.jar:./lib/hadoop/jsch-0.1.42.jar:./lib/hadoop/jsp-2.1:./lib/hadoop/jsp-2.1/jsp-2.1.jar:./lib/hadoop/jsp-2.1/jsp-api-2.1.jar:./lib/hadoop/junit-4.5.jar:./lib/hadoop/kfs-0.2.2.jar:./lib/hadoop/kfs-0.2.LICENSE.txt:./lib/hadoop/log4j-1.2.15.jar:./lib/hadoop/mockito-all-1.8.5.jar:./lib/hadoop/native:./lib/hadoop/native/Linux-amd64-64:./lib/hadoop/native/Linux-amd64-64/libhadoop.a:./lib/hadoop/native/Linux-amd64-64/libhadoop.la:./lib/hadoop/native/Linux-amd64-64/libhadoop.so:./lib/hadoop/native/Linux-amd64-64/libhadoop.so.1:./lib/hadoop/native/Linux-amd64-64/libhadoop.so.1.0.0:./lib/hadoop/native/Linux-i386-32:./lib/hadoop/native/Linux-i386-32/libhadoop.a:./lib/hadoop/native/Linux-i386-32/libhadoop.la:./lib/hadoop/native/Linux-i386-32/libhadoop.so:./lib/hadoop/native/Linux-i386-32/libhadoop.so.1:./lib/hadoop/native/Linux-i386-32/libhadoop.so.1.0.0:./lib/hadoop/oro-2.0.8.jar:./lib/hadoop/servlet-api-2.5-20081211.jar:./lib/hadoop/slf4j-api-1.4.3.jar:./lib/hadoop/slf4j-log4j12-1.4.3.jar:./lib/hadoop/xmlenc-0.52.jar:./lib/hadoop-core-1.0.1.jar:./lib/junit-4.10.jar:./lib/log4j-1.2.16.jar MiplProgram";
 	static Runtime runtime;
 
 	public static void main(String args[]) {
 		junit.textui.TestRunner.run(CompilerTest.class);
 	}
 
 	@Override
 	protected void setUp() {
 		this.runtime = Runtime.getRuntime();
 	}
 
	public boolean testExecutionSuccess(String mainCommand) throws java.io.IOException, java.lang.InterruptedException {
 		String[] inputFiles = new File(testInputPath).list();
 		String output;
 		DataInputStream outputEater, errorEater;
 
 		boolean success = true;
 		for (int i = 0; i < inputFiles.length; i++) {
 			if (inputFiles[i].startsWith("."))
 				continue;
 			
 			boolean result = true;
 
			Process mainOfMIPL = runtime.exec(mainCommand + testInputPath + "/" + inputFiles[i]);
 			outputEater = new DataInputStream(mainOfMIPL.getInputStream());
 			errorEater = new DataInputStream(mainOfMIPL.getErrorStream());
 			while ((output = outputEater.readLine()) != null)
 				;
 			while ((output = errorEater.readLine()) != null)
 				System.out.println(output);
 			result &= (mainOfMIPL.waitFor() == 0);
 			if (!result)
 				System.out.println(inputFiles[i] + " failed while compiling MIPL Source in Compiler Mode.");
 			success &= result;
 
 			Process compileTarget = runtime.exec(compiledInputCommand);
 			outputEater = new DataInputStream(compileTarget.getInputStream());
 			errorEater = new DataInputStream(compileTarget.getErrorStream());
 			while ((output = outputEater.readLine()) != null)
 				;
 			while ((output = errorEater.readLine()) != null)
 				System.out.println(output);
 			result &= (compileTarget.waitFor() == 0);
 			if (!result)
 				System.out.println(inputFiles[i] + " failed while compiling compiled MIPL Source in Compiler Mode.");
 			success &= result;
 
 			Process runTarget = runtime.exec(runCompiledInputCommand);
 			outputEater = new DataInputStream(runTarget.getInputStream());
 			errorEater = new DataInputStream(runTarget.getErrorStream());
 			while ((output = outputEater.readLine()) != null)
 				;
 			while ((output = errorEater.readLine()) != null)
 				System.out.println(output);
 			result &= (runTarget.waitFor() == 0);
 			if (!result)
 				System.out.println(inputFiles[i] + " failed while running compiled (compiled from compiled MIPL source) in Compiler Mode.");
 			success &= result;
 
 			if (result)
 				System.out.println(inputFiles[i] + " passed.");
 		}
		return success;
	}

	public void	testJavaSource() throws java.io.IOException, java.lang.InterruptedException {
		System.out.println("\nTesting Java Source: \n");
		assertTrue(testExecutionSuccess(this.miplMainCommand));
	}

	public void testByteCode() throws java.io.IOException, java.lang.InterruptedException {
		System.out.println("\nTesting JVM ByteCode: \n");
		assertTrue(testExecutionSuccess(this.miplMainCommand.replace("JavaSrc", "ByteCode")));
 	}
 }
