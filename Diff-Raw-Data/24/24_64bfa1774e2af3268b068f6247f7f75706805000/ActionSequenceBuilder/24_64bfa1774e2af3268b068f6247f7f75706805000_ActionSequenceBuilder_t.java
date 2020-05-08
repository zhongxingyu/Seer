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
 
 import com.google.inject.Provider;
 import com.google.jstestdriver.output.PrintXmlTestResultsAction;
 import com.google.jstestdriver.output.XmlPrinter;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.Executors;
 
 /**
  * A builder for creating a sequence of {@link Action}s to be run by the
  * ActionRunner.
  * 
  * @author corysmith
  */
 public class ActionSequenceBuilder {
 
   private final ActionFactory actionFactory;
   private final FileLoader fileLoader;
   private final Provider<List<ThreadedAction>> threadedActionProvider;
   private final Provider<JsTestDriverClient> clientProvider;
   private final Provider<URLTranslator> urlTranslatorProvider;
   private final Provider<URLRewriter> urlRewriterProvider;
   private final FailureAccumulator accumulator;
 
   private List<String> browsers = new LinkedList<String>();
   private CapturedBrowsers capturedBrowsers = new CapturedBrowsers();
   private HashMap<String, FileInfo> files = new LinkedHashMap<String, FileInfo>();
   private Set<FileInfo> fileSet;
   private int localServerPort = -1;
   private boolean preloadFiles = false;
   private String remoteServerAddress;
   private boolean reset;
   private List<String> tests = new LinkedList<String>();
   private List<String> dryRunFor = new LinkedList<String>();
   private List<ThreadedAction> threadedActions;
   private List<String> commands = new LinkedList<String>();
   private XmlPrinter xmlPrinter;
 
   /**
    * Begins the building of an action sequence.
    * 
    * @param threadedActionProvider
    *          TODO
    * @param clientProvider
    *          TODO
    * @param accumulator 
    */
   public ActionSequenceBuilder(ActionFactory actionFactory,
                                FileLoader fileLoader,
                                ResponseStreamFactory responseStreamFactory,
                                Provider<List<ThreadedAction>> threadedActionProvider,
                                Provider<JsTestDriverClient> clientProvider,
                                Provider<URLTranslator> urlTranslatorProvider,
                                Provider<URLRewriter> urlRewriterProvider,
                                FailureAccumulator accumulator) {
     this.actionFactory = actionFactory;
     this.fileLoader = fileLoader;
     this.threadedActionProvider = threadedActionProvider;
     this.clientProvider = clientProvider;
     this.urlTranslatorProvider = urlTranslatorProvider;
     this.urlRewriterProvider = urlRewriterProvider;
     this.accumulator = accumulator;
   }
 
   /** Add the Browser startup and shutdown actions to the actions stack. */
   private void addBrowserControlActions(List<Action> actions) {
     if (!browsers.isEmpty()) {
       BrowserStartupAction browserStartupAction = new BrowserStartupAction(browsers,
           remoteServerAddress, new SimpleProcessFactory(), new CountDownLatch(browsers.size()));
       capturedBrowsers.addObserver(browserStartupAction);
       actions.add(0, browserStartupAction);
      if (!leaveServerRunning()) {
        actions.add(new BrowserShutdownAction(browserStartupAction));
      }
     }
   }
 
   /**
    * Wraps the current sequence of actions with the server start and stop
    * actions.
    */
   private void addServerActions(List<Action> actions, boolean leaveServerRunning) {
     if (preloadFiles) {
       for (FileInfo file : fileLoader.loadFiles(fileSet, true)) {
         files.put(file.getFileName(), file);
       }
     }
     ServerStartupAction serverStartupAction =
         actionFactory.getServerStartupAction(localServerPort, capturedBrowsers, new FilesCache(
             files), urlTranslatorProvider.get(), urlRewriterProvider.get());
     actions.add(0, serverStartupAction);
     if (!leaveServerRunning) {
       actions.add(new ServerShutdownAction(serverStartupAction));
     }
   }
 
   /**
    * Adds tests to the action sequence.
    * 
    * @param tests
    *          The list of tests to be executed during this sequence.
    * @return the current builder.
    */
   public ActionSequenceBuilder addTests(List<String> tests) {
     this.tests.addAll(tests);
     return this;
   }
 
   /** Indicates that tests should be executed as a dry run. */
   public ActionSequenceBuilder asDryRunFor(List<String> dryRunFor) {
     this.dryRunFor.addAll(dryRunFor);
     return this;
   }
 
   /** Creates and returns a sequence of actions. */
   public List<Action> build() {
     List<Action> actions = new LinkedList<Action>();
 
     JsTestDriverClient client = clientProvider.get();
 
     threadedActions = threadedActionProvider.get();
 
     if (!threadedActions.isEmpty()) {
       actions.add(new ThreadedActionsRunner(client, threadedActions, Executors
           .newCachedThreadPool()));
     }
     if (xmlPrinter != null) {
       actions.add(new PrintXmlTestResultsAction(xmlPrinter));
     }
 
     // wrap the actions with the setup/teardown actions.
     addBrowserControlActions(actions);
     if (needToStartServer()) {
       addServerActions(actions, leaveServerRunning());
     }
     actions.add(new FailureCheckerAction(accumulator));
     return actions;
   }
 
   /** Method that derives whether or not to leave the server running. */
   private boolean leaveServerRunning() {
     return tests.isEmpty() && commands.isEmpty() && !reset && dryRunFor.isEmpty();
   }
 
   private boolean needToStartServer() {
     return localServerPort != -1;
   }
 
   /**
    * Indicates a list of browsers that the actions should be executed in. This
    * is required.
    */
   public ActionSequenceBuilder onBrowsers(List<String> browsers) {
     this.browsers.addAll(browsers);
     return this;
   }
 
   /**
    * Indicates that the browser should be reset before executing the tests.
    */
   public ActionSequenceBuilder reset(boolean reset) {
     this.reset = reset;
     return this;
   }
 
   /**
    * Defines a list of files that should be loaded into the list of browsers.
    * 
    * @param fileSet
    *          The files to be loaded into the browser.
    * @param preloadFiles
    *          Indicates if thes files should be preloaded into the server.
    * @return The current builder.
    */
   public ActionSequenceBuilder usingFiles(Set<FileInfo> fileSet, boolean preloadFiles) {
     this.fileSet = fileSet;
     this.preloadFiles = preloadFiles;
     return this;
   }
 
   /**
    * Indicates that a local server should be started on the provided port.
    */
   public ActionSequenceBuilder withLocalServerPort(int localServerPort) {
     this.localServerPort = localServerPort;
     return this;
   }
 
   /**
    * Defines a remote server to run against.
    */
   public ActionSequenceBuilder withRemoteServer(String serverAddress) {
     this.remoteServerAddress = serverAddress;
     return this;
   }
 
   /**
    * Adds a list commands to executed in the browser and the results to be
    * returned.
    */
   public ActionSequenceBuilder addCommands(List<String> commands) {
     this.commands.addAll(commands);
     return this;
   }
 
   public ActionSequenceBuilder printingResultsWhenFinished(XmlPrinter printer) {
     this.xmlPrinter = printer;
     return this;
   }
 }
