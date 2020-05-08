 package com.reubenpeeris.maven.lombokeclipsecompiler;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.plexus.compiler.AbstractCompiler;
 import org.codehaus.plexus.compiler.CompilerConfiguration;
 import org.codehaus.plexus.compiler.CompilerException;
 import org.codehaus.plexus.compiler.CompilerMessage;
 import org.codehaus.plexus.compiler.CompilerOutputStyle;
 import org.codehaus.plexus.compiler.CompilerResult;
 import org.codehaus.plexus.util.IOUtil;
 import org.eclipse.jdt.internal.compiler.batch.Main;
 
 import static com.reubenpeeris.maven.lombokeclipsecompiler.Utils.findJava;
 import static com.reubenpeeris.maven.lombokeclipsecompiler.Utils.getFromList;
 import static com.reubenpeeris.maven.lombokeclipsecompiler.Utils.getJarFor;
 import static com.reubenpeeris.maven.lombokeclipsecompiler.Utils.toPath;
 
 public class LombokEclipseCompiler extends AbstractCompiler {
     private static final String JVM_PROPERTY_PREFIX = "-J";
 	private static final String LOMBOK_JAR_PROPERTY = "-L-lombokjar";
 
     public LombokEclipseCompiler() {
         super(CompilerOutputStyle.ONE_OUTPUT_FILE_PER_INPUT_FILE, ".java",
                 ".class", null);
     }
 
     @Override
     public CompilerResult performCompile(CompilerConfiguration config)
             throws CompilerException {    	
         File javaExe = findJava();
         String lombokJarRegex = config.getCustomCompilerArgumentsAsMap().get(LOMBOK_JAR_PROPERTY);
         if (lombokJarRegex == null) {
             lombokJarRegex = ".*/lombok-[^/]*\\.jar";
         }
         String lombokJar = getFromList(lombokJarRegex, config.getClasspathEntries(), "Lombok jars");
 
         List<String> commandLine = new ArrayList<String>();
         commandLine.add(javaExe.getAbsolutePath());
 
         String jdtJar = getJarFor(org.eclipse.jdt.internal.compiler.Compiler.class);
 
         for (Map.Entry<String, String> entry : config.getCustomCompilerArgumentsAsMap().entrySet()) {
             if (entry.getKey().startsWith(JVM_PROPERTY_PREFIX)) {
                 commandLine.add(entry.getKey().substring(JVM_PROPERTY_PREFIX.length()));
                 if (entry.getValue() != null) {
                 	commandLine.add(entry.getValue());
                 }
             }
         }
 
         if (config.getMeminitial() != null) {
         	commandLine.add("-Xms" + config.getMeminitial());
         }
         if (config.getMaxmem() != null) {
         	commandLine.add("-Xmx" + config.getMaxmem());
         }
                 
         commandLine.add("-Xbootclasspath/a:" + jdtJar);
 
         if (lombokJar != null) {
         	//No Kind produces an INFO message in the output
 //            messages.add(new CompilerMessage("Using Lombok from '" + lombokJar + "'", Kind.NOTE));
         	System.out.println("[INFO] Using Lombok from '" + lombokJar + "'");
             commandLine.add("-Xbootclasspath/a:" + lombokJar);
             commandLine.add("-javaagent:" + lombokJar);
         }
         commandLine.add(Main.class.getCanonicalName());
         commandLine.add("-source");
         commandLine.add(config.getSourceVersion());
         commandLine.add("-target");
         commandLine.add(config.getTargetVersion());
         commandLine.add("-encoding");
         commandLine.add(config.getSourceEncoding());
         commandLine.add("-cp");
         commandLine.add(toPath(config.getClasspathEntries()));
         commandLine.add("-d");
         commandLine.add(config.getOutputLocation());
 
         for (Map.Entry<String, String> entry : config.getCustomCompilerArgumentsAsMap().entrySet()) {
             if (!entry.getKey().equals(LOMBOK_JAR_PROPERTY) && !entry.getKey().startsWith(JVM_PROPERTY_PREFIX)) {
                 commandLine.add(entry.getKey());
                 if (entry.getValue() != null) {
                 	commandLine.add(entry.getValue());
                 }
             }
         }
 
         commandLine.addAll(config.getSourceLocations());
 
         ProcessBuilder pb = new ProcessBuilder(commandLine);
         pb.redirectErrorStream(true);
         pb.directory(config.getWorkingDirectory());
 
         try {
             Process process = pb.start();
 
             IOUtil.copy(process.getInputStream(), System.out);
            int result = process.waitFor();
             System.out.println();
             
             return new CompilerResult(result == 0, Collections.<CompilerMessage>emptyList());
         } catch (IOException e) {
        	throw new RuntimeException(e);
        } catch (InterruptedException e) {
        	throw new RuntimeException(e);
		}
     }
 
  
     @Override
     public String[] createCommandLine(CompilerConfiguration config)
             throws CompilerException {
         return null;
     }
 }
