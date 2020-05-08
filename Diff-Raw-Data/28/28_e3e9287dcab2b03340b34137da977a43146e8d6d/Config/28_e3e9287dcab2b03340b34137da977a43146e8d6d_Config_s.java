 /*
  * Copyright (c) 2008-2010, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  * Licensed under the terms of the New BSD License.
  */
 package chord.project;
 
 import java.io.File;
 import chord.util.FileUtils;
 
 /**
  * System properties recognized by Chord.
  *
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 public class Config {
 	private static final String USER_DIR_AS_CHORD_WORK_DIR =
 		"WARN: Property chord.work.dir not set; using value of user.dir (`%s`) instead.";
 	private static final String BAD_OPTION =
 		"ERROR: Unknown value '%s' for system property '%s'; expected: %s";
 	private static final String CHORD_MAIN_DIR_UNDEFINED =
 		"ERROR: Property chord.main.dir not set; must be set to the absolute location of the directory named 'main' in your Chord installation.";
 	private static final String CHORD_WORK_DIR_UNDEFINED =
 		"ERROR: Property chord.work.dir not set; must be set to the absolute location of the working directory desired during Chord's execution.";
 
 	private Config() { }
 
 	public final static String LIST_SEPARATOR = " |,|:|;";
 
 	// Chord resource properties
 
 	public final static String mainDirName = System.getProperty("chord.main.dir");
 	static {
 		if (mainDirName == null)
 			Messages.fatal(CHORD_MAIN_DIR_UNDEFINED);
 	}
 	public final static String mainClassPathName = System.getProperty("chord.main.class.path");
 	public final static String bddbddbClassPathName = System.getProperty("chord.bddbddb.class.path");
 	public static String libDirName = FileUtils.getAbsolutePath(mainDirName, "lib");
 	// This source of this agent is defined in main/src/agent/chord_instr_agent.cpp.
 	// See the ccompile target in main/build.xml and main/src/agent/Makefile for how it is built.
 	public static String cInstrAgentFileName = libDirName + File.separator + "libchord_instr_agent.so";
 	// This source of this agent is defined in main/src/java/chord/instr/OnlineTransformer.java.
 	// See the jcompile target in main/build.xml for how it is built.
 	public static String jInstrAgentFileName = libDirName + File.separator + "chord_instr_agent.jar";
 	public final static String javadocURL = "http://chord.stanford.edu/javadoc_2_0/";
 
 	// Chord boot properties
 
 	public static String workDirName = System.getProperty("chord.work.dir");
 	static {
 		if (workDirName == null) {
 			workDirName = System.getProperty("user.dir");
 			if (workDirName == null)
 				Messages.fatal(CHORD_WORK_DIR_UNDEFINED);
 			Messages.log(USER_DIR_AS_CHORD_WORK_DIR, workDirName);
 		}
 	}
 	public final static String propsFileName = System.getProperty("chord.props.file");
 	public final static String maxHeap = System.getProperty("chord.max.heap");
 	public final static String maxStack = System.getProperty("chord.max.stack");
 	public final static String jvmargs = System.getProperty("chord.jvmargs");
 	public final static String bddbddbMaxHeap = System.getProperty("chord.bddbddb.max.heap", "1024m");
 
 	// Chord task properties
 
 	public final static boolean buildScope = buildBoolProperty("chord.build.scope", false);
 	public final static String runAnalyses = System.getProperty("chord.run.analyses", "");
 	public final static String printMethods = System.getProperty("chord.print.methods", "").replace('#', '$');
 	public final static String printClasses = System.getProperty("chord.print.classes", "").replace('#', '$');
 	public final static boolean printAllClasses = buildBoolProperty("chord.print.all.classes", false);
 	public final static String printRels = System.getProperty("chord.print.rels", "");
 	public final static boolean publishTargets = buildBoolProperty("chord.publish.targets", false);
 
 	// Basic program properties
 
 	public final static String mainClassName = System.getProperty("chord.main.class");
 	public final static String classPathName = System.getProperty("chord.class.path");
 	public final static String srcPathName = System.getProperty("chord.src.path");
 	public final static String runIDs = System.getProperty("chord.run.ids", "0");
 	public final static String runtimeJvmargs = System.getProperty("chord.runtime.jvmargs", "-ea -Xmx1024m");
 
 	// Program scope properties
 
 	public final static String scopeKind = System.getProperty("chord.scope.kind", "rta");
 	public final static boolean reuseScope = buildBoolProperty("chord.reuse.scope", false);
 	public final static String CHkind = System.getProperty("chord.ch.kind", "static");
 	public final static boolean handleForNameReflection = buildBoolProperty("chord.reflect.forname", false);
 	public final static boolean handleNewInstReflection = buildBoolProperty("chord.reflect.newinst", false);
 
 	public final static String mainClassPathPackages = "chord.,javassist.,joeq.,net.sf.bddbddb.,net.sf.javabdd.";
 
 	public final static String DEFAULT_SCOPE_EXCLUDES = mainClassPathPackages;
 	public final static String DEFAULT_CHECK_EXCLUDES =
 		concat(mainClassPathPackages, ',', "java.,javax.,sun.,com.sun.,com.ibm.,org.apache.harmony.");
 
 	public final static String scopeExcludeStdStr = DEFAULT_SCOPE_EXCLUDES;
 	public final static String scopeExcludeExtStr = System.getProperty("chord.scope.exclude.ext", "");
 	public static String scopeExcludeStr = System.getProperty("chord.scope.exclude",
 		concat(scopeExcludeStdStr, ',', scopeExcludeExtStr));
 	public static String[] scopeExcludeAry = toArray(scopeExcludeStr);
 
 	// Program analysis properties
 
 	public final static String javaAnalysisPathName = mainRel2AbsPath("chord.java.analysis.path", "classes");
 	public final static String dlogAnalysisPathName = mainRel2AbsPath("chord.dlog.analysis.path",
		"src" + File.separator + "dlog" + File.pathSeparator +
		".." + File.separator + "extra" + File.separator + "dlog");
 	public final static boolean reuseRels = buildBoolProperty("chord.reuse.rels", false);
 	public final static boolean publishResults = buildBoolProperty("chord.publish.results", true);
 
 	public final static String checkExcludeStdStr = DEFAULT_CHECK_EXCLUDES;
 	public final static String checkExcludeExtStr = System.getProperty("chord.check.exclude.ext", "");
 	public final static String checkExcludeStr = System.getProperty("chord.check.exclude",
 		concat(checkExcludeStdStr, ',', checkExcludeExtStr));
 	public final static String[] checkExcludeAry = toArray(checkExcludeStr);
 
     // Program transformation properties
  
 	public final static boolean doSSA = buildBoolProperty("chord.ssa", true);
 
     // Chord debug properites
 
 	public final static boolean verbose = buildBoolProperty("chord.verbose", false);
 	public final static boolean bddbddbVerbose = buildBoolProperty("chord.bddbddb.verbose", false);
 	public final static boolean saveDomMaps = buildBoolProperty("chord.save.maps", true);
 
 	// Chord instrumentation properties
 
 	public final static String instrKind = System.getProperty("chord.instr.kind", "offline");
 	static {
 		if (!instrKind.equals("offline") && !instrKind.equals("online"))
 			Messages.fatal(BAD_OPTION, instrKind, "chord.instr.kind", "[offline|online]");
 	}
 	public final static String traceKind = System.getProperty("chord.trace.kind", "pipe");
 	static {
 		if (!traceKind.equals("none") && !traceKind.equals("full") && !traceKind.equals("pipe"))
 			Messages.fatal(BAD_OPTION, traceKind, "chord.trace.kind", "[full|pipe|none]");
 	}
 	public final static boolean reuseTraces = buildBoolProperty("chord.reuse.traces", false);
 	public final static int traceBlockSize = Integer.getInteger("chord.trace.block.size", 4096);
 	public final static boolean dynamicHaltOnErr = buildBoolProperty("chord.dynamic.haltonerr", true);
 	public final static int dynamicTimeout = Integer.getInteger("chord.dynamic.timeout", -1);
 	public final static int maxConsSize = Integer.getInteger("chord.max.cons.size", 50000000);
 	public final static boolean dynamicSilent = buildBoolProperty("chord.dynamic.silent", false);
 
 	// Chord output properties
 
 	public static String outDirName = workRel2AbsPath("chord.out.dir", "chord_output");
 	static {
 		// Automatically find a free subdirectory; this is Percy's stuff
 		String outPoolPath = System.getProperty("chord.out.pooldir");
 		if (outPoolPath != null) {
 			for (int i = 0; true; i++) {
 				outDirName = outPoolPath+"/"+i+".exec";
 				if (!new File(outDirName).exists())
 					break;
 			}
 		}
 		FileUtils.mkdirs(outDirName);
 	}
 	public final static String outFileName = outRel2AbsPath("chord.out.file", "log.txt");
 	public final static String errFileName = outRel2AbsPath("chord.err.file", "log.txt");
 	public final static String reflectFileName = outRel2AbsPath("chord.reflect.file", "reflect.txt");
 	public final static String methodsFileName = outRel2AbsPath("chord.methods.file", "methods.txt");
 	public final static String classesFileName = outRel2AbsPath("chord.classes.file", "classes.txt");
 	public static String bddbddbWorkDirName = outRel2AbsPath("chord.bddbddb.work.dir", "bddbddb");
 	// TODO: create this dir on demand
 	static {
 		FileUtils.mkdirs(bddbddbWorkDirName);
 	}
 
 	public final static String bootClassesDirName = outRel2AbsPath("chord.boot.classes.dir", "boot_classes");
 	public final static String userClassesDirName = outRel2AbsPath("chord.user.classes.dir", "user_classes");
 	public final static String instrSchemeFileName = outRel2AbsPath("chord.instr.scheme.file", "scheme.ser");
 	public final static String traceFileName = outRel2AbsPath("chord.trace.file", "trace");
 
 	public static void print() {
 		System.out.println("*** Chord resource properties:");
 		System.out.println("chord.main.dir: " + mainDirName);
 		System.out.println("chord.main.class.path: " + mainClassPathName);
 		System.out.println("chord.bddbddb.class.path: " + bddbddbClassPathName);
 
 		System.out.println("*** Chord boot properties:");
 		System.out.println("chord.work.dir: " + workDirName);
 		System.out.println("chord.props.file: " + propsFileName);
 		System.out.println("chord.max.heap: " + maxHeap);
 		System.out.println("chord.max.stack: " + maxStack);
 		System.out.println("chord.jvmargs: " + jvmargs);
 		System.out.println("chord.bddbddb.max.heap: " + bddbddbMaxHeap);
 
 		System.out.println("*** Chord task properties:");
 		System.out.println("chord.build.scope: " + buildScope);
 		System.out.println("chord.run.analyses: " + runAnalyses);
 		System.out.println("chord.print.all.classes: " + printAllClasses);
 		System.out.println("chord.print.methods: " + printMethods);
 		System.out.println("chord.print.classes: " + printClasses);
 		System.out.println("chord.print.rels: " + printRels);
 		System.out.println("chord.publish.targets: " + publishTargets);
 
 		System.out.println("*** Basic program properties:");
 		System.out.println("chord.main.class: " + mainClassName);
 		System.out.println("chord.class.path: " + classPathName);
 		System.out.println("chord.src.path: " + srcPathName);
 		System.out.println("chord.run.ids: " + runIDs);
 		System.out.println("chord.runtime.jvmargs: " + runtimeJvmargs);
 
 		System.out.println("*** Program scope properties:");
 		System.out.println("chord.scope.kind: " + scopeKind);
 		System.out.println("chord.reuse.scope: " + reuseScope);
 		System.out.println("chord.ch.kind: " + CHkind);
 		System.out.println("chord.reflect.forname: " + handleForNameReflection);
 		System.out.println("chord.reflect.newinst: " + handleNewInstReflection);
 		System.out.println("chord.scope.exclude.std: " + scopeExcludeStdStr);
 		System.out.println("chord.scope.exclude.ext: " + scopeExcludeExtStr);
 		System.out.println("chord.scope.exclude: " + scopeExcludeStr);
 
 		System.out.println("*** Program analysis properties:");
 		System.out.println("chord.java.analysis.path: " + javaAnalysisPathName);
 		System.out.println("chord.dlog.analysis.path: " + dlogAnalysisPathName);
 		System.out.println("chord.reuse.rels: " + reuseRels);
 		System.out.println("chord.publish.results: " + publishResults);
 		System.out.println("chord.check.exclude.std: " + checkExcludeStdStr);
 		System.out.println("chord.check.exclude.ext: " + checkExcludeExtStr);
 		System.out.println("chord.check.exclude: " + checkExcludeStr);
 
 		System.out.println("*** Program transformation properties:");
 		System.out.println("chord.ssa: " + doSSA);
 
 		System.out.println("*** Chord debug properties:");
 		System.out.println("chord.verbose: " + verbose);
 		System.out.println("chord.bddbddb.verbose: " + bddbddbVerbose);
 		System.out.println("chord.save.maps: " + saveDomMaps);
 
 		System.out.println("*** Chord instrumentation properties:");
 		System.out.println("chord.instr.kind: " + instrKind);
 		System.out.println("chord.trace.kind: " + traceKind);
 		System.out.println("chord.reuse.traces: " + reuseTraces);
 		System.out.println("chord.trace.block.size: " + traceBlockSize);
 		System.out.println("chord.dynamic.haltonerr: " + dynamicHaltOnErr);
 		System.out.println("chord.dynamic.timeout: " + dynamicTimeout);
 		System.out.println("chord.max.cons.size: " + maxConsSize);
 		System.out.println("chord.dynamic.silent: " + dynamicSilent);
 
 		System.out.println("*** Chord output properties:");
 		System.out.println("chord.out.dir: " + outDirName);
 		System.out.println("chord.out.file: " + outFileName);
 		System.out.println("chord.err.file: " + errFileName);
 		System.out.println("chord.reflect.file: " + reflectFileName);
 		System.out.println("chord.methods.file: " + methodsFileName);
 		System.out.println("chord.classes.file: " + classesFileName);
 		System.out.println("chord.bddbddb.work.dir: " + bddbddbWorkDirName);
 		System.out.println("chord.boot.classes.dir: " + bootClassesDirName);
 		System.out.println("chord.user.classes.dir: " + userClassesDirName);
 		System.out.println("chord.instr.scheme.file: " + instrSchemeFileName);
 		System.out.println("chord.trace.file: " + traceFileName);
 	}
	private static String outRel2AbsPath(String propName, String fileName) {
 		String val = System.getProperty(propName);
		return (val != null) ? val : FileUtils.getAbsolutePath(outDirName, fileName);
 	}
	private static String mainRel2AbsPath(String propName, String fileName) {
 		String val = System.getProperty(propName);
		return (val != null) ? val : FileUtils.getAbsolutePath(mainDirName, fileName);
 	}
	private static String workRel2AbsPath(String propName, String fileName) {
 		String val = System.getProperty(propName);
		return (val != null) ? val : FileUtils.getAbsolutePath(workDirName, fileName);
 	}
 	public static boolean buildBoolProperty(String propName, boolean defaultVal) {
 		return System.getProperty(propName, Boolean.toString(defaultVal)).equals("true"); 
 	}
 	public static String[] toArray(String str) {
         return str.equals("") ? new String[0] : str.split(LIST_SEPARATOR);
 	}
 	public static String concat(String s1, char sep, String s2) {
 		if (s1.equals("")) return s2;
 		if (s2.equals("")) return s1;
 		return s1 + sep + s2;
 	}
 }
