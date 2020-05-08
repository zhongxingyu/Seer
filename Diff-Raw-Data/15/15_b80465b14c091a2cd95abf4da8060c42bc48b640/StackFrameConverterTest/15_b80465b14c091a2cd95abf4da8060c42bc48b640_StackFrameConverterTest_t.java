 /*
  * This file is covered by the terms of the Common Public License v1.0.
  *
  * Copyright (c) SZEDER Gábor
  *
  * Parts of this software were developed within the JEOPARD research
  * project, which received funding from the European Union's Seventh
  * Framework Programme under grant agreement No. 216682.
  */
 
 package de.fzi.cjunit.jpf.util;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 import java.io.File;
 
 import org.junit.Test;
 
 import de.fzi.cjunit.jpf.exceptioninfo.StackTraceElementInfo;
 
 
 public class StackFrameConverterTest {
 
 	StackFrameConverter sfc = new StackFrameConverter();
 
 	@Test
 	public void sourceFileBasenameStripDirs() {
 		String filename = "Object.java";
 		String filenameWithPath = "java" + File.separatorChar
 				+ "lang/" + File.separatorChar + filename;
 		assertThat(sfc.sourceFileBasename(filenameWithPath),
 				equalTo(filename));
 	}
 
 	@Test
 	public void sourceFileBasenameNothingToStrip() {
 		String magicFilename = "<direct call>";
 		assertThat(sfc.sourceFileBasename(magicFilename),
 				equalTo(magicFilename));
 	}
 
 	@Test
	public void sourceFileBasenameHandlesNullReference() {
		String nullFilename = null;
		assertThat(sfc.sourceFileBasename(nullFilename),
				equalTo("(Unknown source)"));
	}

	@Test
 	public void stackTraceElementConversion() {
 		StackTraceElementInfo info = new StackTraceElementInfo() {
 			public String getClassName() { return "class0"; }
 			public String getMethodName() { return "method0"; }
 			public String getFileName() {
 				return File.separatorChar + "de"
 					+ File.separatorChar + "fzi"
 					+ File.separatorChar + "cjunit"
 					+ File.separatorChar + "test"
 					+ File.separatorChar + "Class0.java";
 			}
 			public int getLineNumber() { return 0; }
 		};
 		StackTraceElement ste = sfc.toStackTraceElement(info);
 
 		assertThat("element 0", ste.getClassName(),
 				equalTo(info.getClassName()));
 		assertThat("element 0", ste.getMethodName(),
 				equalTo(info.getMethodName()));
 		assertThat("element 0", ste.getFileName(),
 				equalTo("Class0.java"));
 		assertThat("element 0", ste.getLineNumber(),
 				equalTo(info.getLineNumber()));
 	}
 
 	@Test
 	public void stackTraceConversion() {
 		StackTraceElementInfo info0 = new StackTraceElementInfo() {
 			public String getClassName() { return "class0"; }
 			public String getMethodName() { return "method0"; }
 			public String getFileName() {
 				return "Class0.java";
 			}
 			public int getLineNumber() { return 0; }
 		};
 		StackTraceElementInfo info1 = new StackTraceElementInfo() {
 			public String getClassName() { return "class1"; }
 			public String getMethodName() { return "method1"; }
 			public String getFileName() {
 				return "Class1.java";
 			}
 			public int getLineNumber() { return 1; }
 		};
 		StackTraceElementInfo info2 = new StackTraceElementInfo() {
 			public String getClassName() { return "class2"; }
 			public String getMethodName() { return "method2"; }
 			public String getFileName() {
 				return "Class2.java";
 			}
 			public int getLineNumber() { return 2; }
 		};
 		StackTraceElementInfo[] infoArray
 				= new StackTraceElementInfo[] {
 						info0, info1, info2 };
 		StackTraceElement[] stackTrace = sfc.toStackTrace(infoArray);
 
 		assertThat("stack trace lenght", stackTrace.length,
 				equalTo(infoArray.length));
 
 		assertThat("element 0", stackTrace[0].getClassName(),
 				equalTo(info0.getClassName()));
 		assertThat("element 0", stackTrace[0].getMethodName(),
 				equalTo(info0.getMethodName()));
 		assertThat("element 0", stackTrace[0].getFileName(),
 				equalTo(info0.getFileName()));
 		assertThat("element 0", stackTrace[0].getLineNumber(),
 				equalTo(info0.getLineNumber()));
 
 		assertThat("element 1", stackTrace[1].getClassName(),
 				equalTo(info1.getClassName()));
 		assertThat("element 1", stackTrace[1].getMethodName(),
 				equalTo(info1.getMethodName()));
 		assertThat("element 1", stackTrace[1].getFileName(),
 				equalTo(info1.getFileName()));
 		assertThat("element 1", stackTrace[1].getLineNumber(),
 				equalTo(info1.getLineNumber()));
 
 		assertThat("element 2", stackTrace[2].getClassName(),
 				equalTo(info2.getClassName()));
 		assertThat("element 2", stackTrace[2].getMethodName(),
 				equalTo(info2.getMethodName()));
 		assertThat("element 2", stackTrace[2].getFileName(),
 				equalTo(info2.getFileName()));
 		assertThat("element 2", stackTrace[2].getLineNumber(),
 				equalTo(info2.getLineNumber()));
 	}
 }
