 package eu.europeana.uim.integration;
 
 import static junit.framework.Assert.assertFalse;
 import static org.junit.Assert.assertEquals;
 import static org.ops4j.pax.exam.CoreOptions.felix;
 import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
 import static org.ops4j.pax.exam.CoreOptions.systemProperty;
 import static org.ops4j.pax.exam.CoreOptions.waitForFrameworkStartup;
 import static org.ops4j.pax.exam.OptionUtils.combine;
 
 import java.util.Date;
 
 import junit.framework.Assert;
 
 import org.apache.karaf.testing.AbstractIntegrationTest;
 import org.apache.karaf.testing.Helper;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.Configuration;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 
 import eu.europeana.uim.Registry;
 import eu.europeana.uim.common.progress.MemoryProgressMonitor;
 import eu.europeana.uim.orchestration.ActiveExecution;
 import eu.europeana.uim.orchestration.Orchestrator;
 import eu.europeana.uim.storage.StorageEngine;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.Execution;
 import eu.europeana.uim.store.MetaDataRecord;
 import eu.europeana.uim.store.Provider;
 import eu.europeana.uim.store.Request;
 import eu.europeana.uim.store.bean.CollectionBean;
 import eu.europeana.uim.store.bean.ProviderBean;
 import eu.europeana.uim.store.bean.RequestBean;
 import eu.europeana.uim.workflow.Workflow;
 import eu.europeana.uim.workflows.SysoutWorkflow;
 
 /**
  * Integration test for the Orchestrator, using the MemoryStorageEngine
  * 
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @RunWith(JUnit4TestRunner.class)
 public class OrchestratorTest extends AbstractIntegrationTest {
     /**
      * @return setup configuration
      * @throws Exception
      */
     @Configuration
     public static Option[] configuration() throws Exception {
         return combine(
                 Helper.getDefaultOptions(
                         systemProperty("karaf.name").value("junit"),
                         systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value(
                                 "FINE")),
 
                 // rhaa
                 // systemProperty("integrationDir").value(System.getProperty("integrationDir")),
 
 // PaxRunnerOptions.vmOption(
 // "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5006" ),
 
                 mavenBundle().groupId("eu.europeana").artifactId("europeana-uim-common").versionAsInProject(),
                 mavenBundle().groupId("eu.europeana").artifactId("europeana-uim-api").versionAsInProject(),
                 mavenBundle().groupId("eu.europeana").artifactId("europeana-uim-storage-memory").versionAsInProject(),
                 mavenBundle().groupId("eu.europeana").artifactId("europeana-uim-logging-memory").versionAsInProject(),
                mavenBundle().groupId("eu.europeana").artifactId("europeana-uim-orchestration-basic").versionAsInProject(),
 
                 felix(),
 
                 waitForFrameworkStartup());
     }
 
     /**
      * @throws Exception
      */
     @SuppressWarnings({ "unchecked", "rawtypes", "cast" })
     @Test
     public void processSampleData() throws Exception {
         Registry registry = getOsgiService(Registry.class);
 
         StorageEngine<Long> storage = null;
         while (storage == null) {
             storage = (StorageEngine<Long>)registry.getStorageEngine();
             Thread.sleep(500);
         }
         Assert.assertNotNull(storage);
 
         Provider<Long> p = new ProviderBean<Long>(1L);
         Collection<Long> c = new CollectionBean<Long>(2L, p);
         Request<Long> r = new RequestBean<Long>(3L, c, new Date());
 
         // load the provider data
         Thread.sleep(1000);
 
 // Provider<Long> p = storage.getProvider(0l);
 // Collection<Long> c = storage.getCollections(p).get(0);
 // Request<Long> r = storage.createRequest(c, new Date());
 
         for (int i = 0; i < 999; i++) {
             MetaDataRecord<Long> record = storage.createMetaDataRecord(c, "id=" + i);
             storage.updateMetaDataRecord(record);
             storage.addRequestRecord(r, record);
         }
 
         assertEquals("Wrong count of imported test MDRs", 999, storage.getTotalByCollection(c));
 
         Orchestrator o = getOsgiService(Orchestrator.class);
         MemoryProgressMonitor monitor = new MemoryProgressMonitor();
         // run the workflow
 
         // Initialize workflow
         System.out.println("WORKFLOWS " + registry.getWorkflows());
         Workflow<MetaDataRecord<Long>, Long> workflow = (Workflow<MetaDataRecord<Long>, Long>)registry.getWorkflow(SysoutWorkflow.class.getSimpleName());
         int wait = 0;
         while (workflow == null && wait++ < 10) {
             workflow = (Workflow<MetaDataRecord<Long>, Long>)registry.getWorkflow(SysoutWorkflow.class.getSimpleName());
             Thread.sleep(1000);
         }
         Assert.assertNotNull(workflow);
 
         ActiveExecution<MetaDataRecord<Long>, Long> execution = (ActiveExecution<MetaDataRecord<Long>, Long>)o.executeWorkflow(
                 workflow, c);
         execution.getMonitor().addListener(monitor);
 
         execution.waitUntilFinished();
 
         assertEquals("Wrong count of processed MDRs", 999, monitor.getWorked());
 
         Thread.sleep(1000);
         assertEquals("Zombie execution", 0, o.getActiveExecutions().size());
 
         Execution<Long> e = storage.getExecution(execution.getExecution().getId());
         assertFalse("Status of execution not correctly saved when it is finished", e.isActive());
     }
 }
