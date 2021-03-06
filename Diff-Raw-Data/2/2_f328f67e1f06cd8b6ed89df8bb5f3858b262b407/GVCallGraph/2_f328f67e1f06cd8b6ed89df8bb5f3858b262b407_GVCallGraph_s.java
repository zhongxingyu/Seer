 /*******************************************************************************
  * Copyright (c) 2002 - 2006 IBM Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package com.ibm.wala.examples.drivers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 
 import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
 import com.ibm.wala.eclipse.util.CancelException;
 import com.ibm.wala.examples.properties.WalaExamplesProperties;
 import com.ibm.wala.ipa.callgraph.AnalysisCache;
 import com.ibm.wala.ipa.callgraph.AnalysisOptions;
 import com.ibm.wala.ipa.callgraph.AnalysisScope;
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.ipa.callgraph.CallGraph;
 import com.ibm.wala.ipa.callgraph.CallGraphStats;
 import com.ibm.wala.ipa.callgraph.Entrypoint;
 import com.ibm.wala.ipa.callgraph.impl.Util;
 import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
 import com.ibm.wala.ipa.cha.ClassHierarchy;
 import com.ibm.wala.properties.WalaProperties;
 import com.ibm.wala.types.ClassLoaderReference;
 import com.ibm.wala.util.collections.Filter;
 import com.ibm.wala.util.config.AnalysisScopeReader;
 import com.ibm.wala.util.config.FileProvider;
 import com.ibm.wala.util.debug.Assertions;
 import com.ibm.wala.util.graph.Graph;
 import com.ibm.wala.util.io.CommandLine;
 import com.ibm.wala.util.warnings.WalaException;
 import com.ibm.wala.viz.DotUtil;
 import com.ibm.wala.viz.GVUtil;
 
 /**
  * This simple example WALA application builds a call graph and fires off
  * ghostview to visualize a DOT representation.
  * 
  * @author sfink
  */
 public class GVCallGraph {
 
   private final static String PS_FILE = "cg.ps";
 
   /**
    * Usage: args = "-appJar [jar file name] {-exclusionFile
    * [exclusionFileName]}" The "jar file name" should be something like
    * "c:/temp/testdata/java_cup.jar"
    * 
    * @throws CancelException
    * @throws IllegalArgumentException
    */
   public static void main(String[] args) throws WalaException, IllegalArgumentException, CancelException {
     run(args);
   }
 
   /**
    * Usage: args = "-appJar [jar file name] {-exclusionFile
    * [exclusionFileName]}" The "jar file name" should be something like
    * "c:/temp/testdata/java_cup.jar"
    * 
    * @throws CancelException
    * @throws IllegalArgumentException
    */
   public static Process run(String[] args) throws WalaException, IllegalArgumentException, CancelException {
     Properties p = CommandLine.parse(args);
     validateCommandLine(p);
     return run(p.getProperty("appJar"), p.getProperty("exclusionFile", CallGraphTestUtil.REGRESSION_EXCLUSIONS));
   }
 
   /**
    * @param appJar
    *            something like "c:/temp/testdata/java_cup.jar"
    * @throws CancelException
    * @throws IllegalArgumentException
    */
   public static Process run(String appJar, String exclusionFile) throws IllegalArgumentException, CancelException {
     try {
       Graph<CGNode> g = buildPrunedCallGraph(appJar, FileProvider.getFile(exclusionFile));
 
       Properties p = null;
       try {
         p = WalaExamplesProperties.loadProperties();
         p.putAll(WalaProperties.loadProperties());
       } catch (WalaException e) {
         e.printStackTrace();
         Assertions.UNREACHABLE();
       }
       String psFile = p.getProperty(WalaProperties.OUTPUT_DIR) + File.separatorChar + PS_FILE;
 
       String dotExe = p.getProperty(WalaExamplesProperties.DOT_EXE);
       DotUtil.dotify(g, null, GVTypeHierarchy.DOT_FILE, psFile, dotExe);
 
       String gvExe = p.getProperty(WalaExamplesProperties.GHOSTVIEW_EXE);
       return GVUtil.launchGV(psFile, gvExe);
 
     } catch (WalaException e) {
       e.printStackTrace();
       return null;
     } catch (IOException e) {
       e.printStackTrace();
       return null;
     }
   }
 
   /**
    * @param appJar
    *            something like "c:/temp/testdata/java_cup.jar"
    * @return a call graph
    * @throws CancelException
    * @throws IllegalArgumentException
    */
   public static Graph<CGNode> buildPrunedCallGraph(String appJar, File exclusionFile) throws WalaException,
       IllegalArgumentException, CancelException {
     AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar, exclusionFile != null ? exclusionFile
         : new File(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
 
     ClassHierarchy cha = ClassHierarchy.make(scope);
 
     Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
     AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
 
     // //
     // build the call graph
     // //
    com.ibm.wala.ipa.callgraph.CallGraphBuilder builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCache(), cha, scope);
     CallGraph cg = builder.makeCallGraph(options);
     
     System.err.println(CallGraphStats.getStats(cg));
 
     Graph<CGNode> g = pruneForAppLoader(cg);
     
     return g;
   }
 
   static Graph<CGNode> pruneForAppLoader(CallGraph g) throws WalaException {
     return GVTypeHierarchy.pruneGraph(g, new ApplicationLoaderFilter());
   }
 
   /**
    * Validate that the command-line arguments obey the expected usage.
    * 
    * Usage:
    * <ul>
    * <li> args[0] : "-appJar"
    * <li> args[1] : something like "c:/temp/testdata/java_cup.jar" </ul?
    * 
    * @throws UnsupportedOperationException
    *             if command-line is malformed.
    */
   public static void validateCommandLine(Properties p) {
     if (p.get("appJar") == null) {
       throw new UnsupportedOperationException("expected command-line to include -appJar");
     }
   }
 
   /**
    * A filter that accepts WALA objects that "belong" to the application loader.
    * 
    * Currently supported WALA types include
    * <ul>
    * <li> {@link CGNode}
    * <li> {@link LocalPointerKey}
    * </ul>
    */
   private static class ApplicationLoaderFilter implements Filter<CGNode> {
 
     public boolean accepts(CGNode o) {
       if (o instanceof CGNode) {
         CGNode n = (CGNode) o;
         return n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application);
       } else if (o instanceof LocalPointerKey) {
         LocalPointerKey l = (LocalPointerKey) o;
         return accepts(l.getNode());
       } else {
         return false;
       }
     }
   }
 }
