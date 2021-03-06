 /* Soot - a J*va Optimization Framework
  * Copyright (C) 1997-1999 Raja Vallee-Rai
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 /*
  * Modified by the Sable Research Group and others 1997-1999.  
  * See the 'credits' file distributed with Soot for the complete list of
  * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
  */
 
 
 package soot;
 
 import soot.util.*;
 import java.util.*;
 import soot.jimple.*;
 import soot.grimp.*;
 import soot.baf.*;
 import soot.jimple.toolkits.invoke.*;
 import soot.baf.toolkits.base.*;
 import soot.toolkits.scalar.*;
 import soot.dava.*;
 
 import java.io.*;
 
 import java.text.*;
 
 public class Main
 {
     private static char fileSeparator = System.getProperty("file.separator").charAt(0);
 
     static boolean naiveJimplification;
     static boolean onlyJimpleOutput;
     public static boolean isVerbose;
     static boolean onlyJasminOutput;
     static public boolean isProfilingOptimization;
     static boolean isSubtractingGC;
     static public boolean oldTyping;
     static public boolean isInDebugMode;
     static public boolean usePackedLive;
     static public boolean usePackedDefs = true;
     static boolean isTestingPerformance;
 
     static private String targetExtension = ".class";
 
     static public int totalFlowNodes,
            totalFlowComputations;
            
     static public Timer copiesTimer = new Timer("copies"),
         defsTimer = new Timer("defs"),
         usesTimer = new Timer("uses"),
         liveTimer = new Timer("live"),
         splitTimer = new Timer("split"),
         packTimer = new Timer("pack"),
         cleanup1Timer = new Timer("cleanup1"),
         cleanup2Timer = new Timer("cleanup2"),
         conversionTimer = new Timer("conversionm"),
         cleanupAlgorithmTimer = new Timer("cleanupAlgorithm"),
         graphTimer = new Timer("graphTimer"),
         assignTimer = new Timer("assignTimer"),
         resolveTimer = new Timer("resolveTimer"),
         totalTimer = new Timer("totalTimer"),
         splitPhase1Timer = new Timer("splitPhase1"),
         splitPhase2Timer = new Timer("splitPhase2"),
         usePhase1Timer = new Timer("usePhase1"),
         usePhase2Timer = new Timer("usePhase2"),
         usePhase3Timer = new Timer("usePhase3"),
         defsSetupTimer = new Timer("defsSetup"),
         defsAnalysisTimer = new Timer("defsAnalysis"),
         defsPostTimer = new Timer("defsPost"),
         liveSetupTimer = new Timer("liveSetup"),
         liveAnalysisTimer = new Timer("liveAnalysis"),
         livePostTimer = new Timer("livePost"),
         aggregationTimer = new Timer("aggregation"),
         grimpAggregationTimer = new Timer("grimpAggregation"),
         deadCodeTimer = new Timer("deadCode"),
         propagatorTimer = new Timer("propagator"),
         buildJasminTimer = new Timer("buildjasmin"),
         assembleJasminTimer = new Timer("assembling jasmin");
         
     static public Timer
         resolverTimer = new Timer("resolver");
         
     static public int conversionLocalCount,
         cleanup1LocalCount,
         splitLocalCount,
         assignLocalCount,
         packLocalCount,
         cleanup2LocalCount;
 
     static public int conversionStmtCount,
         cleanup1StmtCount,
         splitStmtCount,
         assignStmtCount,
         packStmtCount,
         cleanup2StmtCount;
 
     static private String outputDir = "";
 
     static private boolean isOptimizing;
     static private boolean isOptimizingWhole;
     static private boolean isUsingVTA;
     static private boolean isUsingRTA;
     static private boolean isApplication = false;
     
     static public long stmtCount;
     static String finalRep = "grimp";
 
     private static List getClassesUnder(String aPath) 
     {
         File file = new File(aPath);
         List fileNames = new ArrayList();
 
         File[] files = file.listFiles();
         
 
         for(int i = 0; i < files.length; i++) {            
             if(files[i].isDirectory()) {               
                 List l  = getClassesUnder( aPath + File.separator + files[i].getName());
                 Iterator it = l.iterator();
                 while(it.hasNext()) {
                     String s = (String) it.next();
                     fileNames.add(files[i].getName() +  "." + s);
                 }                    
             } else {                
                 String fileName = files[i].getName();        
                 int index = fileName.indexOf(".class");
                 if( index != -1) {
                     fileNames.add(fileName.substring(0, index));
                 }
             }
         }
         return fileNames;
     }
 
 
     
     public static void main(String[] args)
     {
         boolean isAnalyzingLibraries = false;
 
         // The following lists are paired.  false is exclude in the first list.
         List packageInclusionFlags = new ArrayList();
         List packageInclusionMasks = new ArrayList();
 
         List dynamicClasses = new ArrayList();
         List processClasses = new ArrayList();
 
         Chain cmdLineClasses = new HashChain();
         packageInclusionFlags.add(new Boolean(false));
         packageInclusionMasks.add("java.");
 
         packageInclusionFlags.add(new Boolean(false));
         packageInclusionMasks.add("sun.");
 
         packageInclusionFlags.add(new Boolean(false));
         packageInclusionMasks.add("javax.");
 
         totalTimer.start();
 
         if(args.length == 0)
         {
 // $Format: "            System.out.println(\"Soot version $ProjectVersion$\");"$
            System.out.println("Soot version 1.beta.5.dev.34");
             System.out.println("Copyright (C) 1997-1999 Raja Vallee-Rai (rvalleerai@sable.mcgill.ca).");
             System.out.println("All rights reserved.");
             System.out.println("");
             System.out.println("Contributions are copyright (C) 1997-1999 by their respective contributors.");
             System.out.println("See individual source files for details.");
             System.out.println("");
             System.out.println("Soot comes with ABSOLUTELY NO WARRANTY.  Soot is free software,");
             System.out.println("and you are welcome to redistribute it under certain conditions.");
             System.out.println("See the accompanying file 'license.html' for details.");
             System.out.println("");
             System.out.println("Syntax: (single-file mode) soot [option]* classname ...  ");
             System.out.println("        (application mode) soot --app [option]* mainClassName");
             System.out.println("");
             System.out.println("Output options:");
             System.out.println("  -b, --b                    produce .b (abbreviated .baf) files");
             System.out.println("  -B, --baf                  produce .baf code");
             System.out.println("  -j, --jimp                 produce .jimp (abbreviated .jimple) files");
             System.out.println("  -J, --jimple               produce .jimple code");
             System.out.println("  -g, --grimp                produce .grimp (abbreviated .grimple) files");
             System.out.println("  -G, --grimple              produce .grimple files");
             System.out.println("  -s, --jasmin               produce .jasmin files");
             System.out.println("  -c, --class                produce .class files");
             System.out.println("  -d PATH                    store produced files in PATH");
             System.out.println("");
             System.out.println("Application mode options:");
             System.out.println("  -x, --exclude PACKAGE      marks classfiles in PACKAGE (e.g. java.)"); 
             System.out.println("                             as context classes");
             System.out.println("  -i, --include PACKAGE      marks classfiles in PACKAGE (e.g. java.util.)");
             System.out.println("                             as application classes");
             System.out.println("  -a, --analyze-context      label context classes as library");
             System.out.println("  --dynamic-path PATH        mark all class files in PATH as ");
             System.out.println("                             potentially dynamic classes");
             System.out.println("");
             System.out.println("Single-file mode options:");
             System.out.println("  --process-path PATH        process all classes on the PATH");
             System.out.println("");
             System.out.println("Construction options:");
             System.out.println("  --final-rep REP            produce classfile/jasmin from REP ");
             System.out.println("                                  (jimple, grimp, or baf)");
             System.out.println();
 //            System.out.println("Jimple construction options:");
 //            System.out.println("  --no-splitting             do not split local variables");
 //            System.out.println("  --use-packing              pack locals after conversion");
 //            System.out.println("  --no-typing                do not assign types to the local variables");
 //            System.out.println("  --no-jimple-aggregating    do not perform any Jimple-level aggregation");
 //            System.out.println("  --use-original-names       retain variables name from local variable table");
             System.out.println("");
             System.out.println("Optimization options:");
             System.out.println("  -O  --optimize             perform scalar optimizations on the classfiles");
             System.out.println("  -W  --whole-optimize       perform whole program optimizations on the ");
 //            System.out.println("                             classfiles");
             System.out.println("");
             System.out.println("Miscellaneous options:");
             System.out.println("  --soot-class-path PATH     uses PATH as the classpath for finding classes");
             System.out.println("  -t, --time                 print out time statistics about tranformations");
             System.out.println("  --subtract-gc              attempt to subtract the gc from the time stats");
             System.out.println("  -v, --verbose              verbose mode");
             System.out.println("  --debug                    avoid catching exceptions");
             System.out.println("  -p, --phase-option PHASE-NAME KEY[:VALUE]");
             System.out.println("                             set run-time option KEY to VALUE for PHASE-NAME");
             System.out.println("                             (default for VALUE is true)");
             System.out.println("");
             System.out.println("Examples:");
             System.out.println("");
             System.out.println("  soot --app -d newClasses Simulator");
             System.out.println("         Transforms all classes starting with Simulator, ");
             System.out.println("         and stores them in newClasses. ");
                
             
             System.exit(0);
         }
 
         // Handle all the options
             for(int i = 0; i < args.length; i++)
             {
                 String arg = args[i];
                 
                 if(arg.equals("-j") || arg.equals("--jimp"))
                     targetExtension = ".jimp";
                 else if(arg.equals("-s") || arg.equals("--jasmin"))
                     targetExtension = ".jasmin";
                 else if(arg.equals("-J") || arg.equals("--jimple"))
                     targetExtension = ".jimple";
                 else if(arg.equals("-B") || arg.equals("--baf"))
                     targetExtension = ".baf";
                 else if(arg.equals("-b") || arg.equals("--b"))
                     targetExtension = ".b";
                 else if(arg.equals("-g") || arg.equals("--grimp"))
                     targetExtension = ".grimp";
                 else if(arg.equals("-G") || arg.equals("--grimple"))
                     targetExtension = ".grimple";
                 else if(arg.equals("-c") || arg.equals("--class"))
                     targetExtension = ".class";
                 else if(arg.equals("--dava"))
                     targetExtension = ".dava";
                 else if(arg.equals("-O") || arg.equals("--optimize"))
                     isOptimizing = true;
                 else if(arg.equals("-W") || arg.equals("--whole-optimize"))
                 {
                     if (!isApplication)
                     {
                         System.out.println("Can only whole-program optimize in application mode!");
                         System.exit(1);
                     }
                     isOptimizingWhole = true;
                     isOptimizing = true;
                 } 
                 /*
                 else if(arg.equals("--use-vta"))
                 {
                     isUsingVTA = true;
                     Jimplifier.NOLIB = false;
                 }
                 else if(arg.equals("--use-rta"))
                 {
                     isUsingRTA = true;
                     Jimplifier.NOLIB = false;
                 } */
                 
                 else if(arg.equals("-t") || arg.equals("--time"))
                     isProfilingOptimization = true;
                 else if(arg.equals("--subtract-gc"))
                 {
                     Timer.setSubtractingGC(true);
                     isSubtractingGC = true;
                 }    
                 else if(arg.equals("-v") || arg.equals("--verbose"))
                     isVerbose = true;
                 else if(arg.equals("--soot-class-path"))
                 {   
                     if(++i < args.length)
                         Scene.v().setSootClassPath(args[i]);
                 }
                 else if(arg.equals("--app"))
                 {
                     if (i != 0)
                     {
                         System.out.println("Application mode (--app) must be set as first argument to Soot!");
                         System.out.println("eg. java soot.Main --app Simulator");
                         System.exit(1);
                     }
                     isApplication=true;
                 }
                 else if(arg.equals("-d"))
                 {
                     if(++i < args.length)
                         outputDir = args[i];
                 }
                 else if(arg.equals("-x") || arg.equals("--exclude"))
                 {
                     if (!isApplication)
                     {
                         System.out.println("Exclude flag only valid in application mode!");
                         System.exit(1);
                     }
                     if(++i < args.length)
                     {
                         packageInclusionFlags.add(new Boolean(false));
                         packageInclusionMasks.add(args[i]);
                     }
                 }
                 else if(arg.equals("-i") || arg.equals("--include"))
                 {
                     if (!isApplication)
                     {
                         System.out.println("Include flag only valid in application mode!");
                         System.exit(1);
                     }
                     if(++i < args.length)
                     {
                         packageInclusionFlags.add(new Boolean(true));
                         packageInclusionMasks.add(args[i]);
                     }
                 }
                 else if(arg.equals("-A") || arg.equals("--analyze-context"))
                     isAnalyzingLibraries = true;
                 else if(arg.equals("--final-rep"))
                 {
                     if(++i < args.length)
                         finalRep = args[i];
                         
                     if(!finalRep.equals("jimple") &&
                         !finalRep.equals("grimp") &&
                         !finalRep.equals("baf"))
                     {
                         System.out.println("Illegal --final-rep arg: " + finalRep);
                     }
                     
                 }
                 else if (arg.equals("-p") || arg.equals("--phase-option"))
                 {
                     String phaseName = args[++i];
                     String option = args[++i];
                     int colonLoc = option.indexOf(':');
                     String key = null, value = null;
 
                     if (colonLoc == -1)
                     {
                         key = option;
                         value = "true";
                     }
                     else 
                     {
                         key = option.substring(0, option.indexOf(':'));
                         value = option.substring(option.indexOf(':')+1);
                     }
 
                     Scene.v().getPhaseOptions(phaseName).put(key, value);
                 }
                 else if (arg.equals("--debug"))
                     isInDebugMode = true;
                
                 else if (arg.equals("--dynamic-path"))
                 {
                     if (!isApplication)
                     {
                         System.out.println("Dynamic-path flag only valid in application mode!");
                         System.exit(1);
                     }
 
                     if(++i < args.length) 
                     {
                         StringTokenizer tokenizer = new StringTokenizer(args[i], ":");
                         while(tokenizer.hasMoreTokens()) 
                             dynamicClasses.addAll(getClassesUnder(tokenizer.nextToken()));
                     }                    
                 }
                 else if (arg.equals("--process-path")) 
                 {
                     if (isApplication)
                     {
                         System.out.println("Process-path flag only valid in single-file mode!");
                         System.exit(1);
                     }
 
                     if(++i < args.length) {                        
                         StringTokenizer tokenizer = new StringTokenizer(args[i], ":");
                         while(tokenizer.hasMoreTokens())
                             processClasses.addAll(getClassesUnder(tokenizer.nextToken()));
                     }                    
                 }
                 else if(arg.startsWith("-"))
                 {
                     System.out.println("Unrecognized option: " + arg);
                     System.exit(0);
                 } 
                 else
                 {
                     cmdLineClasses.add(arg);
                 }
             }
 
         SootClass mainClass = null;
         
         if(cmdLineClasses.isEmpty())
         {
             System.out.println("Nothing to do!");
             System.exit(0);
         }
 
         // Load necessary classes.
         {
             
             // Command line classes
                 if (isApplication && cmdLineClasses.size() > 1)
                 {
                     System.out.println("Can only specify one class in application mode!");
                     System.out.println("The transitive closure of the specified class gets loaded.");
                     System.out.println("(Did you mean to use single-file mode?)");
                     System.exit(1);
                 }
 
                 Iterator it = cmdLineClasses.iterator();
 
                 while(it.hasNext())
                 {
                     String name = (String) it.next();
                     SootClass c = Scene.v().loadClassAndSupport(name);
 
                     if(mainClass == null)
                     {
                         mainClass = c;
                         Scene.v().setMainClass(c);
                     }   
                     c.setApplicationClass();
                 }
                 
             // Dynamic & process classes
                 it = dynamicClasses.iterator();
                 
                 while(it.hasNext())
                     Scene.v().loadClassAndSupport((String) it.next());                 
 
                 it = processClasses.iterator();
                 
                 while(it.hasNext())
                 {
                     String s = (String)it.next();
                     Scene.v().loadClassAndSupport(s);
                     Scene.v().getSootClass(s).setApplicationClass();
                 }
         }
 
         // Generate classes to process
         {
             if(isApplication)
             {
                 List cc = new ArrayList(); cc.addAll(Scene.v().getContextClasses());
                 Iterator contextClassesIt = cc.iterator();
                 while (contextClassesIt.hasNext())
                     ((SootClass)contextClassesIt.next()).setApplicationClass();
             }   
                          
             // Remove/add all classes from packageInclusionMask as per piFlag
             {
                 List applicationPlusContextClasses = new ArrayList();
                 applicationPlusContextClasses.addAll(Scene.v().getApplicationClasses());
                 applicationPlusContextClasses.addAll(Scene.v().getContextClasses());
 
                 Iterator classIt = applicationPlusContextClasses.iterator();
                 
                 while(classIt.hasNext())
                 {
                     SootClass s = (SootClass) classIt.next();
                     
                     if(cmdLineClasses.contains(s.getName()))
                         continue;
                         
                     Iterator packageCmdIt = packageInclusionFlags.iterator();
                     Iterator packageMaskIt = packageInclusionMasks.iterator();
                     
                     while(packageCmdIt.hasNext())
                     {
                         boolean pkgFlag = ((Boolean) packageCmdIt.next()).booleanValue();
                         String pkgMask = (String) packageMaskIt.next();
                         
                         if (pkgFlag)
                         {
                             if (s.isContextClass() && s.getPackageName().startsWith(pkgMask))
                                 s.setApplicationClass();
                         }
                         else
                         {
                             if (s.isApplicationClass() && s.getPackageName().startsWith(pkgMask))
                                 s.setContextClass();
                         }
                     }
                 }
             }
 
             if (isAnalyzingLibraries)
             {
                 Iterator contextClassesIt = Scene.v().getContextClasses().iterator();
                 while (contextClassesIt.hasNext())
                     ((SootClass)contextClassesIt.next()).setLibraryClass();
             }
         }
         
         Scene.v().getPack("wjtp").apply();
         if(isOptimizingWhole)
             Scene.v().getPack("wjop").apply();
         
         // Handle each class individually
         {
             Iterator classIt = Scene.v().getApplicationClasses().iterator();
             
             while(classIt.hasNext())
             {
                 SootClass s = (SootClass) classIt.next();
                 
                 System.out.print("Transforming " + s.getName() + "... " );
                 System.out.flush();
                 
                 if(!isInDebugMode)
                  {
                     try {
                         handleClass(s);
                     }
                     catch(Exception e)
                     {
                         System.out.println("failed due to: " + e);
                     }
                 }
                 else {
                     handleClass(s);
                 }
                 
                 System.out.println();
             }
         }
                     
         // Print out time stats.
             if(isProfilingOptimization)
             {
                 totalTimer.end();
                     
                 
                 long totalTime = totalTimer.getTime();
                 
                 System.out.println("Time measurements");
                 System.out.println();
                 
                 System.out.println("      Building graphs: " + toTimeString(graphTimer, totalTime));
                 System.out.println("  Computing LocalDefs: " + toTimeString(defsTimer, totalTime));
 //                System.out.println("                setup: " + toTimeString(defsSetupTimer, totalTime));
 //                System.out.println("             analysis: " + toTimeString(defsAnalysisTimer, totalTime));
 //                System.out.println("                 post: " + toTimeString(defsPostTimer, totalTime));
                 System.out.println("  Computing LocalUses: " + toTimeString(usesTimer, totalTime));
 //                System.out.println("            Use phase1: " + toTimeString(usePhase1Timer, totalTime));
 //                System.out.println("            Use phase2: " + toTimeString(usePhase2Timer, totalTime));
 //                System.out.println("            Use phase3: " + toTimeString(usePhase3Timer, totalTime));
 
                 System.out.println("     Cleaning up code: " + toTimeString(cleanupAlgorithmTimer, totalTime));
                 System.out.println("Computing LocalCopies: " + toTimeString(copiesTimer, totalTime));
                 System.out.println(" Computing LiveLocals: " + toTimeString(liveTimer, totalTime));
 //                System.out.println("                setup: " + toTimeString(liveSetupTimer, totalTime));
 //                System.out.println("             analysis: " + toTimeString(liveAnalysisTimer, totalTime));
 //                System.out.println("                 post: " + toTimeString(livePostTimer, totalTime));
                 
                 System.out.println("Coading coffi structs: " + toTimeString(resolveTimer, totalTime));
 
                 
                 System.out.println();
 
                 // Print out time stats.
                 {
                     float timeInSecs;
 
                     System.out.println("       Resolving classfiles: " + toTimeString(resolverTimer, totalTime)); 
                     System.out.println(" Bytecode -> jimple (naive): " + toTimeString(conversionTimer, totalTime)); 
                     System.out.println("        Splitting variables: " + toTimeString(splitTimer, totalTime));
                     System.out.println("            Assigning types: " + toTimeString(assignTimer, totalTime));
                     System.out.println("  Propagating copies & csts: " + toTimeString(propagatorTimer, totalTime));
                     System.out.println("      Eliminating dead code: " + toTimeString(deadCodeTimer, totalTime));
                     System.out.println("                Aggregation: " + toTimeString(aggregationTimer, totalTime));
                     System.out.println("            Coloring locals: " + toTimeString(packTimer, totalTime));
                     System.out.println("     Generating jasmin code: " + toTimeString(buildJasminTimer, totalTime));
                     System.out.println("          .jasmin -> .class: " + toTimeString(assembleJasminTimer, totalTime));
 
                                             
 //                    System.out.println("           Cleaning up code: " + toTimeString(cleanup1Timer, totalTime) +
 //                        "\t" + cleanup1LocalCount + " locals  " + cleanup1StmtCount + " stmts");
                     
 
                        
                        
 //                    System.out.println("               Split phase1: " + toTimeString(splitPhase1Timer, totalTime));
 //                    System.out.println("               Split phase2: " + toTimeString(splitPhase2Timer, totalTime));
                     
                         
                 
                         /*
                     System.out.println("cleanup2Timer:   " + cleanup2Time +
                         "(" + (cleanup2Time * 100 / totalTime) + "%) " +
                         cleanup2LocalCount + " locals  " + cleanup2StmtCount + " stmts");
 */
 
                     timeInSecs = (float) totalTime / 1000.0f;
                     float memoryUsed = (float) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000.0f;
 
                     System.out.println("totalTime:" + toTimeString(totalTimer, totalTime));
                     
                     if(isSubtractingGC)
                     {
                         System.out.println("Garbage collection was subtracted from these numbers.");
                         System.out.println("           forcedGC:" + 
                             toTimeString(Timer.forcedGarbageCollectionTimer, totalTime));
                     }
 
                     System.out.println("stmtCount: " + stmtCount + "(" + toFormattedString(stmtCount / timeInSecs) + " stmt/s)");
                     
                     System.out.println("totalFlowNodes: " + totalFlowNodes + 
                         " totalFlowComputations: " + totalFlowComputations + " avg: " + 
                         truncatedOf((double) totalFlowComputations / totalFlowNodes, 2));
         
                 }
             }
     }        
     
     private static String toTimeString(Timer timer, long totalTime)
     {
         DecimalFormat format = new DecimalFormat("00.0");
         DecimalFormat percFormat = new DecimalFormat("00.0");
         
         long time = timer.getTime();
         
         String timeString = format.format(time / 1000.0); // paddedLeftOf(new Double(truncatedOf(time / 1000.0, 1)).toString(), 5);
         
         return (timeString + "s" + " (" + percFormat.format(time * 100.0 / totalTime) + "%" + ")");   
     }
     
     private static String toFormattedString(double value)
     {
         return paddedLeftOf(new Double(truncatedOf(value, 2)).toString(), 5);
     }
     
     private static void handleClass(SootClass c)
     {
         FileOutputStream streamOut = null;
         OutputStreamWriter osw = null;
         PrintWriter writerOut = null;
         
         String fileName;
         
         if(!outputDir.equals(""))
             fileName = outputDir + fileSeparator;
         else
             fileName = "";
         
         fileName += c.getName() + targetExtension;
         
         if(!targetExtension.equals(".class"))
         {   
             try {
                 streamOut = new FileOutputStream(fileName);
                 writerOut = new EscapedPrintWriter(streamOut);
             }
             catch (IOException e)
             {
                 System.out.println("Cannot output file " + c.getName() + targetExtension);
             }
         }
 
         boolean produceJimple = false;
         boolean produceBaf = false;
         boolean produceGrimp = false;
         boolean produceDava = false;
         
         // Determine paths
         
         {
             String endResult;
             
             if(targetExtension.startsWith(".jimp"))
                 endResult = "jimple";
             else if(targetExtension.startsWith(".grimp"))
                 endResult = "grimp";
             else if(targetExtension.startsWith(".dava"))
                 endResult = "dava";
             else if(targetExtension.startsWith(".baf"))
                 endResult = "baf";
             else
                 endResult = finalRep;
         
     
             if(endResult.equals("jimple"))
                 produceJimple = true;
             else if(endResult.equals("baf"))
             {
                 produceBaf = true; 
                 produceJimple = true;
             }
             else if(endResult.equals("grimp"))
             {
                 produceJimple = true; 
                 produceGrimp = true;
             }
             else if(endResult.equals("dava"))
             {
                 produceJimple = true; 
                 produceGrimp = true;
                 produceDava = true;
             }
         }
             
         // Build all necessary bodies
         {
             Iterator methodIt = c.getMethods().iterator();
             
             while(methodIt.hasNext())
             {   
                 SootMethod m = (SootMethod) methodIt.next();
                 
                 if(!m.isConcrete())
                     continue;
                     
                 if(produceJimple)
                 {
                     if(!m.hasActiveBody())
                         m.setActiveBody(Jimple.v().newBody(new ClassFileBody(m), "jb"));
     
                     Scene.v().getPack("jtp").apply(m.getActiveBody());
                     if(isOptimizing) 
                         Scene.v().getPack("jop").apply(m.getActiveBody());
                 }
                 
                 if(produceGrimp)
                 {
                     if(isOptimizing)
                         m.setActiveBody(Grimp.v().newBody(m.getActiveBody(), "gb", "aggregate-all-locals"));
                     else
                         m.setActiveBody(Grimp.v().newBody(m.getActiveBody(), "gb"));
                         
                     if(isOptimizing)
                         Scene.v().getPack("gop").apply(m.getActiveBody());
                         
                 }
                 else if(produceBaf)
                 {   
                      m.setActiveBody(Baf.v().newBody((JimpleBody) m.getActiveBody()));
 
                      if(isOptimizing) 
                         Scene.v().getPack("bop").apply(m.getActiveBody());
                 } 
                 
                 if(produceDava)
                 {
                     m.setActiveBody(Dava.v().newBody(m.getActiveBody(), "db"));
                 }    
             }
             
         }
 
         if(targetExtension.equals(".jasmin"))
         {
             if(c.containsBafBody())
                 new soot.baf.JasminClass(c).print(writerOut);            
             else
                 new soot.jimple.JasminClass(c).print(writerOut);
         }
         else if(targetExtension.equals(".jimp"))
             c.printTo(writerOut, PrintJimpleBodyOption.USE_ABBREVIATIONS);
         else if(targetExtension.equals(".b"))
             c.printTo(writerOut, soot.baf.PrintBafBodyOption.USE_ABBREVIATIONS);
         else if(targetExtension.equals(".baf") || targetExtension.equals(".jimple") || targetExtension.equals(".grimple"))
             c.printTo(writerOut);
         else if(targetExtension.equals(".dava"))
             c.printTo(writerOut);
         else if(targetExtension.equals(".grimp"))
             c.printTo(writerOut, PrintGrimpBodyOption.USE_ABBREVIATIONS);
         else if(targetExtension.equals(".class"))
             c.write(outputDir);
         
         if(!targetExtension.equals(".class"))
         {
             try {
                 writerOut.flush();
                 streamOut.close();
             }
             catch(IOException e)
             {
                 System.out.println("Cannot close output file " + fileName);
             }
         }
 
         // Release bodies
         {
             Iterator methodIt = c.getMethods().iterator();
                 
             while(methodIt.hasNext())
             {   
                 SootMethod m = (SootMethod) methodIt.next();
                 
                 if(m.hasActiveBody())
                     m.releaseActiveBody();
             }
         }
     }
     
     public static double truncatedOf(double d, int numDigits)
     {
         double multiplier = 1;
         
         for(int i = 0; i < numDigits; i++)
             multiplier *= 10;
             
         return ((long) (d * multiplier)) / multiplier;
     }
     
     public static String paddedLeftOf(String s, int length)
     {
         if(s.length() >= length)
             return s;
         else {
             int diff = length - s.length();
             char[] padding = new char[diff];
             
             for(int i = 0; i < diff; i++)
                 padding[i] = ' ';
             
             return new String(padding) + s;
         }    
     }
 
 }
