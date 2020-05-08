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
 package com.synopsys.arc.jenkinsci.plugins.cygwinprocesskiller;
 
 import com.synopsys.arc.jenkinsci.plugins.cygwinprocesskiller.util.CygwinKillHelper;
 import com.synopsys.arc.jenkinsci.plugins.cygwinprocesskiller.util.CygwinKillerException;
import hudson.EnvVars;
 import hudson.Extension;
 import hudson.model.Computer;
import hudson.model.Environment;
import hudson.model.Hudson;
 import hudson.model.Node;
 import hudson.model.TaskListener;
 import hudson.remoting.Callable;
import hudson.remoting.Channel;
 import hudson.slaves.SlaveComputer;
 import hudson.util.LogTaskListener;
 import hudson.util.ProcessKiller;
 import hudson.util.ProcessTree;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.lang.SystemUtils;
 
 /**
  * Extension, which kills Cygwin process trees.
  * @author Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
  */
 @Extension
 public class CygwinProcessKiller extends ProcessKiller {
     private static final Level KILLER_LOGGING_LEVEL = Level.WARNING;
     private static final String KILLER_LOGGER_NAME = "global";
 
     @Override
     public boolean kill(ProcessTree.OSProcess process) throws IOException, InterruptedException {       
         if (!SystemUtils.IS_OS_WINDOWS) {
             return false;
         } 
         
         try {
             KillReport report = SlaveComputer.getChannelToMaster().call(new KillerRemoteCall(process.getPid()));
             return report.isKilledSuccessfully();
         } catch (CygwinKillerException ex) {
             //TODO: log errors
             return false;
         }
     }    
     
     
     public static class KillReport implements Serializable {
         boolean killedSuccessfully;
         String errorMessage;
 
         public KillReport(boolean killedSuccessfully, String errorMessage) {
             this.killedSuccessfully = killedSuccessfully;
             this.errorMessage = errorMessage;
         }
 
         public boolean isKilledSuccessfully() {
             return killedSuccessfully;
         }
 
         public String getErrorMessage() {
             return errorMessage;
         }
     }
     
     public static class KillerRemoteCall implements Callable<KillReport, CygwinKillerException> {
         private final int processPID;
       
 
         public KillerRemoteCall(int processPID) {
             this.processPID = processPID;
         }
         
         @Override
         public KillReport call() throws CygwinKillerException {
             CygwinProcessKillerPlugin plugin = CygwinProcessKillerPlugin.Instance();
             if (!plugin.isEnableProcessKiller()) {
                 return new KillReport(false, "Killer is disabled");
             }
             
             // Init variables
             TaskListener listener = new LogTaskListener(Logger.getLogger(KILLER_LOGGER_NAME), KILLER_LOGGING_LEVEL);
            String nodeName = Channel.current().getName();        
            Node targetNode = Hudson.getInstance().getNode(nodeName);
             CygwinKillerInstallation tool = plugin.getToolInstallation();
 
             // Run helper, which checks platform and then runs kill script
            CygwinKillHelper helper = new CygwinKillHelper(listener, targetNode, tool, processPID);
             
             try {
                 if (!helper.isCygwin()) {
                    return new KillReport(false, "Cannot locate Cygwin on the host");
                }                    
                 return new KillReport(helper.kill(), null);
              } catch (Exception ex) {
                  throw new CygwinKillerException(ex.getMessage());
              }        
         }     
     }
 }
