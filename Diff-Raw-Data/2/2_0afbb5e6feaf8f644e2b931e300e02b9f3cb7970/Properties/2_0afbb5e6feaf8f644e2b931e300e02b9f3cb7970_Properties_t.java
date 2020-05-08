 /*
  * Copyright (c) 2008-2009, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.project;
 
 import java.io.File;
 
 /**
  * 
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 public class Properties {
 	private Properties() { }
 
 	public static final String homeDirName = System.getProperty("chord.home.dir");
 	public static final String workDirName = System.getProperty("user.dir");
 	public static final String outDirName = System.getProperty("chord.out.dir");
 	static {
 		assert(outDirName != null);
 		assert(homeDirName != null);
 	}
 	public static final String outFileName = build("chord.out.file", "log.txt");
 	public static final String errFileName = build("chord.err.file", "log.txt");
 
 	public static final String bootClassesFileName = build("chord.boot.classes.file", "boot_classes.txt");
 	public static final String classesFileName = build("chord.classes.file", "classes.txt");
 	public static final String methodsFileName = build("chord.methods.file", "methods.txt");
 
 	public static final String bootClassesDirName = build("chord.boot.classes.dir", "boot_classes");
 	public static final String classesDirName = build("chord.classes.dir", "classes");
 
 	public static final String mainClassName = System.getProperty("chord.main.class");
 	public static final String classPathName = System.getProperty("chord.class.path");
 	public static final String srcPathName = System.getProperty("chord.src.path");
 	public static final String mainClassPathName = System.getProperty("chord.main.class.path");
 	
 	public static final String javaAnalysisPathName = System.getProperty("chord.java.analysis.path");
 	public static final String dlogAnalysisPathName = System.getProperty("chord.dlog.analysis.path");
 	public static final String analyses = System.getProperty("chord.analyses");
 
 	public final static String bddbddbClassPathName = System.getProperty("chord.bddbddb.class.path");
 	public final static String bddbddbWorkDirName = System.getProperty("chord.bddbddb.work.dir");
 	public final static String bddbddbMaxHeap = System.getProperty("chord.bddbddb.max.heap");
 	public final static String bddbddbNoisy = System.getProperty("chord.bddbddb.noisy");
 	public final static String bddLibDirName = System.getProperty("chord.bdd.lib.dir");
 
 	public final static String instrSchemeFileName = build("chord.instr.scheme.file", "scheme.ser");
 	public final static String crudeTraceFileName = build("chord.crude.trace.file", "crude_trace.txt");
 	public final static String finalTraceFileName = build("chord.final.trace.file", "final_trace.txt");
 
 	public final static String runIDs = System.getProperty("chord.run.ids", "0");
 	public final static String instrAgentFileName = System.getProperty("chord.instr.agent.file");
 	public static final int callsBound = Integer.getInteger("chord.calls.bound", 0);
 	public static final int itersBound = Integer.getInteger("chord.iters.bound", 0);
 
	public static final boolean doSSA = buildBoolProp("chord.ssa", true);
 	public static final String scopeKind = System.getProperty("chord.scope.kind", "rta");
 	public static final boolean reuseScope = buildBoolProp("chord.reuse.scope", false);
 	static {
 		assert (scopeKind.equals("rta") || scopeKind.equals("dynamic"));
 	}
 
 	public static void print() {
 		System.out.println("******************************");
 		System.out.println("chord.home.dir: " + homeDirName);
 		System.out.println("chord.out: " + outFileName);
 		System.out.println("chord.err: " + errFileName);
 		System.out.println("chord.work.dir: " + workDirName);
 		System.out.println("chord.out.dir: " + outDirName);
 		System.out.println("chord.boot.classes.dir: " + bootClassesDirName);
 		System.out.println("chord.classes.dir: " + classesDirName);
 		System.out.println("chord.main.class: " + mainClassName);
 		System.out.println("chord.class.path: " + classPathName);
 		System.out.println("chord.src.path: " + srcPathName);
 		System.out.println("chord.main.class.path: " + mainClassPathName);
 		System.out.println("chord.java.analysis.path: " + javaAnalysisPathName);
 		System.out.println("chord.dlog.analysis.path: " + dlogAnalysisPathName);
 		System.out.println("chord.analyses: " + analyses);
 		System.out.println("chord.bddbddb.class.path: " + bddbddbClassPathName);
 		System.out.println("chord.bddbddb.work.dir: " + bddbddbWorkDirName);
 		System.out.println("chord.bddbddb.max.heap: " + bddbddbMaxHeap);
 		System.out.println("chord.bddbddb.noisy: " + bddbddbNoisy);
 		System.out.println("chord.bdd.lib.dir: " + bddLibDirName);
 		System.out.println("chord.run.ids: " + runIDs);
 		System.out.println("chord.instr.agent.file: " + instrAgentFileName);
 		System.out.println("chord.calls.bound: " + callsBound);
 		System.out.println("chord.iters.bound: " + itersBound);
 		System.out.println("chord.ssa: " + doSSA);
 		System.out.println("chord.scope.kind: " + scopeKind);
 		System.out.println("chord.reuse.scope: " + reuseScope);
 		System.out.println("******************************");
 	}
 	public static String build(String propName, String fileName) {
 		String val = System.getProperty(propName);
 		return (val != null) ? val :
 			(new File(outDirName, fileName)).getAbsolutePath();
 	}
 	public static boolean buildBoolProp(String propName, boolean defaultVal) {
 		return System.getProperty(propName, Boolean.toString(defaultVal)).equals("true"); 
 	}
 }
