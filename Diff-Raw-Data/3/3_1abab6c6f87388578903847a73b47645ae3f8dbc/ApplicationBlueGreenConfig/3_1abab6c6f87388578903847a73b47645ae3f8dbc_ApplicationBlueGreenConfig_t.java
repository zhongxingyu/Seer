 /*
  * Copyright 2010-2013, CloudBees Inc.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package com.cloudbees.sdk.commands.bg;
 
 import com.cloudbees.api.BeesClient;
 import com.cloudbees.sdk.cli.BeesCommand;
 import com.cloudbees.sdk.cli.CLICommand;
 import com.cloudbees.sdk.cli.CommandService;
 import org.kohsuke.args4j.Option;
 
 import javax.inject.Inject;
 import java.io.IOException;
 import java.util.*;
 
 @BeesCommand(group="Application", description = "Configure an application for the Blue-Green process")
 @CLICommand("app:bg:config")
 public class ApplicationBlueGreenConfig extends ApplicationBlueGreenBase {
     @Inject
     CommandService cs;
 
     private String application1;
 
     private String application2;
 
     @Option(name = "-pal", usage = "Application primary aliases", required = true, aliases = "--alias")
     private String aliases;
 
     @Option(name = "-f", usage = "Force configuration even if applications do not exist", aliases = "--force")
     private Boolean force;
 
     public ApplicationBlueGreenConfig() {
     }
 
     @Option(name = "-a1", usage = "Blue-Green application 1", required = true, aliases = "--application1")
     public void setApplication1(String application) throws IOException {
         String[] parts = application.split("/");
         if (parts.length < 2) {
             application = getAccount() + "/" + application;
         }
         this.application1 = application;
     }
 
     @Option(name = "-a2", usage = "Blue-Green application 2", required = true, aliases = "--application2")
     public void setApplication2(String application) throws IOException {
         String[] parts = application.split("/");
         if (parts.length < 2) {
             application = getAccount() + "/" + application;
         }
         this.application2 = application;
     }
 
     @Override
     public int main() throws Exception {
         checkApplication(application1, force);
         checkApplication(application2, force);
 
         String blueGreenParam = application1 + "," + application2 + "[" + aliases + "]";
 
         List<String> deployArgs = new ArrayList<String>();
         deployArgs.add(0,"config:set");
         deployArgs.add("-g");
        deployArgs.add("-ac");
        deployArgs.add(getAccount());
         deployArgs.add(".bg.deploy." + getName() + "=" + blueGreenParam);
         return cs.getCommand("config:set").run(deployArgs);
     }
 
     protected void checkApplication(String appId, Boolean force) throws IOException {
         BeesClient client = getBeesClient(BeesClient.class);
         try {
             client.applicationInfo(appId);
         } catch (Exception e) {
             if (force != null && force.booleanValue())
                 System.err.println("WARNING: cannot find application: " + appId);
             else
                 throw new IllegalArgumentException("Cannot find application: " + appId);
         }
 
     }
 }
