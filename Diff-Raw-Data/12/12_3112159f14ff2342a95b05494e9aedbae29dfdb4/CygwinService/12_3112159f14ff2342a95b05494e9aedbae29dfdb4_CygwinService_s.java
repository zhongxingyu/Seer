 package cygwinRunner.agent;
 
 import com.intellij.openapi.diagnostic.Logger;
 import jetbrains.buildServer.RunBuildException;
 import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
 import jetbrains.buildServer.agent.runner.ProgramCommandLine;
 import org.jetbrains.annotations.NotNull;
 import jetbrains.buildServer.util.FileUtil;
 import jetbrains.buildServer.util.StringUtil;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import cygwinRunner.common.Util;
 
 
 public class CygwinService extends BuildServiceAdapter {
     private static final Logger LOG = Logger.getInstance(CygwinService.class.getName());
 
     public CygwinService() {
     }
 
     @NotNull
     @Override
     public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
         return createProgramCommandline(selectCmd(), getCmdArguments());
     }
 
     @NotNull
     private String selectCmd() {
         return "cmd.exe";
     }
 
     private List<String> getCmdArguments() throws RunBuildException {
         List<String> list = new ArrayList<String>();
 
         final String bash = FileUtil.getCanonicalFile(
                 new File(getRunnerParameters().get(Util.CYGWIN_LOCATION), "bin\\bash.exe")).getPath();
         list.add("/c");
         list.add(bash);
         final File script = getOrCreateScriptFile();
         list.add(script.getPath());
 
         return list;
     }
 
     private File getOrCreateScriptFile() throws RunBuildException {
         Closeable handle = null;
         try {
             String text = getRunnerParameters().get(Util.RUNNER_SCRIPT_CODE);
             if (StringUtil.isEmptyOrSpaces(text)) {
                 throw new RunBuildException("Empty build script");
             }
 
             final File code = FileUtil.createTempFile(getBuildTempDirectory(), "cygwin", ".bash", true);
             OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(code), "utf-8");
             handle = w;
             w.write(text);
 
             return code;
         } catch (IOException e) {
             throw new RunBuildException("Failed to generate temporary file at " + getBuildTempDirectory(), e);
         } finally {
             FileUtil.close(handle);
         }
     }
 }
 
 
 
 
