 package com.openske.drools;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.util.Collection;
 
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderFactory;
 import org.drools.builder.ResourceType;
 import org.drools.definition.KnowledgePackage;
 import org.drools.io.ResourceFactory;
 import org.drools.logger.KnowledgeRuntimeLogger;
 import org.drools.runtime.StatefulKnowledgeSession;
 
 import com.openske.model.hardware.Host;
 import com.openske.model.hardware.Router;
 
 public class DroolsFacade {
 
     protected KnowledgeBuilder knowledgeBuilder;
     protected KnowledgeBase knowledgeBase;
     protected PrintWriter outputWriter;
     protected StatefulKnowledgeSession session;
     protected KnowledgeRuntimeLogger logger;
     protected boolean initialized;
 
     public void initialize() {
         if (!isInitialized()) {
             outputWriter.format("Initializing Drools Knowledge Base...");
             knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
             knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
             initialized = true;
         }
     }
 
     public void loadRules() throws Throwable {
         if (isInitialized()) {
             // Search for DRL files starting from the current directory
             Collection<File> drlFiles = DroolsResourceHelper.listResources(ResourceType.DRL);
             outputWriter.format("Loading %d rules...", drlFiles.size());
             // Load the found DRL files into the KnowledgeBuilder
             for (File drl : drlFiles) {
                 outputWriter.format("\t - Loading '%s' ...", drl.getPath());
                 knowledgeBuilder.add(ResourceFactory.newFileResource(drl.getAbsolutePath()), ResourceType.DRL);
                 if (knowledgeBuilder.hasErrors()) {
                     outputWriter.format(knowledgeBuilder.getErrors().toString());
                     throw new RuntimeException("Found errors in DRL file : " + drl);
                 }
             }
 
             Collection<KnowledgePackage> knowledgePackages = knowledgeBuilder.getKnowledgePackages();
             outputWriter.format("Adding %d compiled knowledge packages to the knowledgebase...", knowledgePackages.size());
             for (KnowledgePackage kpackage : knowledgePackages) {
                 outputWriter.format("\t - Knowledge Package %s (%d rules)", kpackage.getName(), kpackage.getRules().size());
             }
             knowledgeBase.addKnowledgePackages(knowledgePackages);
         } else {
             // TODO : Handle calling this method without being initialized
         }
     }
 
     public void loadFacts() {
         if (isInitialized()) {
             Host webServer = new Host("webserver.intranet");
             Host dbServer = new Host("database.intranet");
             Host station1 = new Host("station1.intranet");
             Host station2 = new Host("station2.intranet");
             Host emptyHost = new Host("");
             Router router = new Router("router.intranet");
             router.addConnection(webServer);
             router.addConnection(dbServer);
             router.addConnection(station1);
             router.addConnection(station2);
             getSession().insert(emptyHost);
             // getSession().insert(router);
         } else {
             // TODO : Handle calling this method without being initialized
         }
     }
 
     public void loadProcesses() {
         if (isInitialized()) {
             // TODO : Load all the processes into the knowledgeBuilder first
             // before starting them
             Collection<File> rfFiles = DroolsResourceHelper.listResources(ResourceType.DRF);
             outputWriter.format("Loading %d processes...", rfFiles.size());
             knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
             for (File rf : rfFiles) {
                 knowledgeBuilder.add(ResourceFactory.newFileResource(rf), ResourceType.DRF);
                 knowledgeBase.addKnowledgePackages(knowledgeBuilder.getKnowledgePackages());
             }
         } else {
             // TODO : Handle calling this method without being initialized
         }
     }
 
     public void fireRules() {
         if (isInitialized()) {
             outputWriter.format("Firing all rules...");
             int rulesFired = getSession().fireAllRules();
             outputWriter.format("Fired %d rules", rulesFired);
         } else {
             // TODO : Handle calling this method without being initialized
         }
     }
 
     public void startProcesses() {
         if (isInitialized()) {
            Collection<File> rfFiles = DroolsResourceHelper.listResources(ResourceType.DRF);
            outputWriter.format("Starting %d processes...", rfFiles.size());
            for (File rf : rfFiles) {
                 String processId = DroolsRuleFlowHelper.extractRuleFlowId(rf);
                 if (processId != null) {
                     outputWriter.format("\t - Starting process '%s'...", processId);
                     getSession().startProcess(processId);
                 }
             }
         } else {
             // TODO : Handle calling this method without being initialized
         }
     }
 
     public void cleanup() {
         outputWriter.format("Cleaning up...");
         // Close the session
         if (session != null) {
             if (logger != null) {
                 logger.close();
             }
             String sessionStr = session.toString();
             session.dispose();
             outputWriter.format("Closed Drools session (%s)", sessionStr);
             session = null;
         }
         initialized = false;
     }
 
     protected StatefulKnowledgeSession getSession() {
         if (session == null) {
             session = knowledgeBase.newStatefulKnowledgeSession();
             outputWriter.format("Opened new Drools session (%s)", session.toString());
             // logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(session);
             DroolsEventListener eventListener = new DroolsEventListener();
             eventListener.setOutputWriter(outputWriter);
             session.addEventListener(eventListener);
         }
         return session;
     }
 
     public PrintWriter getOutputWriter() {
         return outputWriter;
     }
 
     public void setOutputWriter(PrintWriter outputWriter) {
         this.outputWriter = outputWriter;
     }
 
     public boolean isInitialized() {
         return initialized;
     }
 }
