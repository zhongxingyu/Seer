 /*
  * This file is part of VisualVM for IDEA
  *
  * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice,
  *       this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above copyright notice,
  *       this list of conditions and the following disclaimer in the documentation
  *       and/or other materials provided with the distribution.
  *
  *     * Neither the name of the copyright holder nor the names of its contributors
  *       may be used to endorse or promote products derived from this software
  *       without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.orfjackal.visualvm4idea.core.commands;
 
 import com.sun.tools.visualvm.application.Application;
 import com.sun.tools.visualvm.application.jvm.Jvm;
 import com.sun.tools.visualvm.application.jvm.JvmFactory;
 import com.sun.tools.visualvm.core.datasource.DataSourceRepository;
 import net.orfjackal.visualvm4idea.visualvm.CpuSettings;
 import net.orfjackal.visualvm4idea.visualvm.ProfilerSupportWrapper;
 import org.netbeans.lib.profiler.common.AttachSettings;
 import org.netbeans.modules.profiler.NetBeansProfiler;
 import org.netbeans.modules.profiler.utils.IDEUtils;
 
 import java.util.Set;
 
 /**
  * @author Esko Luontola
  * @since 25.10.2008
  */
 public class ProfileAppCommand implements Command {
 
     public int profilerPort;
     public boolean profileNewThreads = true;
     public String roots = "";
     public CpuSettings.FilterType filterType = CpuSettings.FilterType.EXCLUDE;
     public String filter = "";
 
     public String getCommandId() {
         return "PROFILE_APP";
     }
 
     public String[] toMessage() {
         return new String[]{
                 getCommandId(),
                 String.valueOf(profilerPort),
                 String.valueOf(profileNewThreads),
                 roots,
                 filterType.name(),
                 filter,
         };
     }
 
     public Command fromMessage(String[] message) {
         int i = 0;
         ProfileAppCommand cmd = new ProfileAppCommand();
         cmd.profilerPort = Integer.parseInt(message[++i]);
         cmd.profileNewThreads = Boolean.parseBoolean(message[++i]);
         cmd.roots = message[++i];
         cmd.filterType = CpuSettings.FilterType.valueOf(message[++i]);
         cmd.filter = message[++i];
         return cmd;
     }
 
     public String[] call() {
         IDEUtils.runInProfilerRequestProcessor(new Runnable() {
             public void run() {
                 NetBeansProfiler.getDefaultNB().attachToApp(
                         getCpuSettings().toProfilingSettings(), getAttachSettings());
                 Application app = getProfiledApplication();
                 ProfilerSupportWrapper.setProfiledApplication(app);
                 ProfilerSupportWrapper.selectProfilerView(app);
             }
         });
         return OK_RESPONSE;
     }
 
     private CpuSettings getCpuSettings() {
         // com.sun.tools.visualvm.profiler.ApplicationProfilerView.MasterViewSupport.handleCPUProfiling()
         // com.sun.tools.visualvm.profiler.CPUSettingsSupport.getSettings()
         CpuSettings cpuSettings = new CpuSettings();
         cpuSettings.profileNewThreads = profileNewThreads;
         cpuSettings.roots = roots;
         cpuSettings.filterType = filterType;
         cpuSettings.filter = filter;
         return cpuSettings;
     }
 
     private AttachSettings getAttachSettings() {
         // com.sun.tools.visualvm.profiler.ApplicationProfilerView.MasterViewSupport.MasterViewSupport()
         // com.sun.tools.visualvm.profiler.ApplicationProfilerView.MasterViewSupport.initSettings()
         final AttachSettings attachSettings = new AttachSettings();
         attachSettings.setDirect(true);
         attachSettings.setHost("localhost");
         attachSettings.setPort(profilerPort);
         return attachSettings;
     }
 
     private Application getProfiledApplication() {
         Application app;
         do {
             sleep(500);
             app = findProfiledApp();
         } while (app == null);
         return app;
     }
 
     private static Application findProfiledApp() {
         Set<Application> apps = DataSourceRepository.sharedInstance().getDataSources(Application.class);
         for (Application app : apps) {
             Jvm jvm = JvmFactory.getJVMFor(app);
             // TODO: use a unique id to identify the right JVM
            if (jvm.getJvmArgs().contains("profilerinterface")) {
                 return app;
             }
         }
         return null;
     }
 
     private static void sleep(int millis) {
         try {
             Thread.sleep(millis);
         } catch (InterruptedException e) {
             // ignore
         }
     }
 }
