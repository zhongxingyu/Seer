 /*
  * The MIT License
  *
  * Copyright 2013 Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.synopsys.arc.jenkinsci.plugins.cygwinprocesskiller.util;
 
 import com.synopsys.arc.jenkinsci.plugins.cygwinprocesskiller.CygwinKillerInstallation;
 import com.synopsys.arc.jenkinsci.plugins.cygwinprocesskiller.CygwinProcessKillerPlugin;
 import com.synopsys.arc.jenkinsci.plugins.cygwinprocesskiller.Messages;
 import hudson.EnvVars;
 import hudson.FilePath;
 import hudson.Launcher.ProcStarter;
 import hudson.Proc;
 import hudson.model.Node;
 import hudson.model.TaskListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.TimeUnit;
 import org.apache.commons.io.output.ByteArrayOutputStream;
 
 /**
  * Class provides basic Cygwin operations.
  * @author Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
  */
 public class CygwinKillHelper {
     private final TaskListener log;
     private final Node node;
     private final CygwinKillerInstallation tool;
     private final int processPID;
   
     // On-demand variables 
     private FilePath tmpDir;
     private FilePath substitutedHome;
     
     private final CygwinProcessKillerPlugin plugin = CygwinProcessKillerPlugin.Instance();
     private static final String CYGWIN_START_PREFIX="CYGWIN_";  
     private static final String CYGWIN_BINARY_PATH="\\bin\\";
     private static final int WAIT_TIMEOUT_SEC=500;
     
     public CygwinKillHelper(TaskListener log, Node node, CygwinKillerInstallation tool, int processPID) {
         this.log = log;
         this.node = node;
         this.tool = tool;
         this.processPID = processPID;
         this.substitutedHome = this.tmpDir = null; // will be retrieved on-demand
     }
     
     /**
      * Checks that Cygwin is available on the host.
      * @throws IOException
      * @throws InterruptedException 
      */
     public boolean isCygwin() throws InterruptedException {        
         OutputStream str = new ByteArrayOutputStream();
         try { // Catch tool installation exceptions
             execCommand("uname", str, "-a");
         } catch (IOException ex) {
             logError(Messages.Message_CygwinCheckFailed() + ex.getMessage());
             return false;
         }
         return str.toString().startsWith(CYGWIN_START_PREFIX);
     }
 
     public FilePath getTmpDir() throws IOException, InterruptedException {
         if (tmpDir == null) {
             tmpDir = findTmpDir(node);
         }
         return tmpDir;
     }
     
     public int execScript(String script, OutputStream out, String ... args) 
             throws IOException, InterruptedException {
         // Prepare a temp file
         FilePath tmpFile = getTmpDir().createTempFile("cygwin_process_killer_", ".sh");       
         tmpFile.write(script, null);
   
         String[] cmd = new String[1+args.length];
         cmd[0] = tmpFile.getRemote();
         System.arraycopy(args, 0, cmd, 1, args.length);
     
         return execCommand("bash", out, cmd);     
     }
     
     public int execCommand(String command, OutputStream stdout, String ... args) throws IOException, InterruptedException {
         String[] cmd = new String[1+args.length];
         cmd[0] = getCygwinBinaryCommand(command);
         System.arraycopy(args, 0, cmd, 1, args.length);
     
         ProcStarter starter = node.createLauncher(log).launch().cmds(cmd).envs(constructVariables()).stdout(stdout).pwd(getTmpDir());
         Proc proc = starter.start();
         int resultCode = proc.joinWithTimeout(WAIT_TIMEOUT_SEC, TimeUnit.SECONDS, log);
         starter.readStdout();
         return resultCode;
     }
     
     public boolean kill() throws IOException, InterruptedException {
         OutputStream str = new ByteArrayOutputStream();
         int res = execScript(plugin.getKillScript(), str, Integer.toString(processPID));
         
         if (res != 0) {
             logError("CygwinKiller cannot kill the process tree (parent pid="+processPID+")");
         }
         return res != 0;
     }
     
     private String getCygwinBinaryCommand(String commandName) throws IOException, InterruptedException {
         return tool != null ? getSubstitutedHome().getRemote() + CYGWIN_BINARY_PATH + commandName+".exe" : commandName+".exe"; 
     }
     
     private static FilePath findTmpDir(Node node) throws IOException, InterruptedException {
         if (node == null) {
             throw new IllegalArgumentException("Must pass non-null node");
         }
         
         FilePath root = node.getRootPath();
         if (root == null) {
             throw new IllegalArgumentException("Node " + node.getDisplayName() + " seems to be offline");
         }
         
         FilePath tmpDir = root.child("cygwin_process_killer").child("tmp");
         if (!tmpDir.exists()) {
             tmpDir.mkdirs();
         }
         return tmpDir;
     }
     
     private Map<String,String> constructVariables() throws IOException, InterruptedException {
         Map<String,String> envVars = new TreeMap<String, String>();
         if (tool != null) {
             FilePath homePath = getSubstitutedHome();   
             String overridenPaths = homePath.child("bin").getRemote()+File.pathSeparator+homePath.child("lib").getRemote();
             envVars.put("PATH", overridenPaths);
             envVars.put("CYGWIN_HOME", homePath.getRemote());
         }      
         return envVars;
     }
 
     public FilePath getSubstitutedHome() throws IOException, InterruptedException {
         if (substitutedHome == null && tool != null) {
             try {
                 substitutedHome = getCygwinHome(null);
             } catch (CygwinKillerException ex) {
                 String msg = Messages.Message_InstallationFailed() + ex.getMessage();
                 logError(msg);
                 throw new IOException(msg, ex);
             }
         } 
         return substitutedHome;
     }    
     
     public void logError(String message) {
         log.error("["+CygwinProcessKillerPlugin.PLUGIN_NAME+"] - "+message);
     }
     
     public FilePath getCygwinHome(EnvVars additionalVars) 
             throws CygwinKillerException, IOException, InterruptedException
     {
         String home = tool.forNode(node, log).getHome();
         if (additionalVars != null && additionalVars.size() != 0) {
             home = additionalVars.expand(home);
         }
        
         // Get and check
         File cygwinHome = new File(home);
        if (!cygwinHome.exists()) {
             throw new CygwinKillerException("Cygwin home directory "+cygwinHome+" does not exist");
        }
        if (!cygwinHome.isDirectory() || !cygwinHome.isAbsolute()) {
             throw new CygwinKillerException("Cygwin home should be an absolute path to a directory");
        }
         return new FilePath(cygwinHome);
     }
 }
