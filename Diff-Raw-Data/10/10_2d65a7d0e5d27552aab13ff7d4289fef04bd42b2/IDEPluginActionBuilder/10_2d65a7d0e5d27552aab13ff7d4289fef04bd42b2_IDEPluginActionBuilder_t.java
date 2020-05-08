 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Executors;
 
 /**
  * A builder for IDE's to use. Minimizes the surface area of the API which needs to
  * be maintained on the IDE plugin side.
  * TODO(jeremiele) We should rename this for other API uses. Refactor the crap out of this.
  * @author alexeagle@google.com (Alex Eagle)
  */
 public class IDEPluginActionBuilder {
 
   
   private final ConfigurationParser configParser;
   private final String serverAddress;
   private final ActionFactory actionFactory;
   private final ResponseStreamFactory responseStreamFactory;
 
   private List<String> tests = new LinkedList<String>();
   
 
   public IDEPluginActionBuilder(ConfigurationParser configParser, String serverAddress,
       ActionFactory actionFactory, ResponseStreamFactory responseStreamFactory) {
     this.configParser = configParser;
     this.serverAddress = serverAddress;
     this.actionFactory = actionFactory;
     this.responseStreamFactory = responseStreamFactory;
   }
 
   public IDEPluginActionBuilder addAllTests() {
     tests.add("all");
     return this;
   }
 
   public ActionRunner build() {
     List<Action> actions = new LinkedList<Action>();
     configParser.parse();
     JsTestDriverClient client = actionFactory.getJsTestDriverClient(configParser.getFilesList(),
         serverAddress);
 
     List<ThreadedAction> threadedActions = createThreadedActions(client);
 
     if (!threadedActions.isEmpty()) {
       actions.add(new ThreadedActionsRunner(client, threadedActions, Executors
           .newCachedThreadPool()));
     }
     return new ActionRunner(actions);
   }
 
   private List<ThreadedAction> createThreadedActions(JsTestDriverClient client) {
     List<ThreadedAction> threadedActions = new ArrayList<ThreadedAction>();
 
     if (!tests.isEmpty()) {
       threadedActions.add(new RunTestsAction(responseStreamFactory, new ResponsePrinterFactory("",
           System.out, client, false), tests, true));
     }
     return threadedActions;
   }
 }
