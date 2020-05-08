 package org.kevoree.macaw.framework.testcase;
 
 import org.kevoree.ContainerRoot;
 import org.kevoree.api.service.core.script.KevScriptEngine;
 import org.kevoree.macaw.framework.scheduler.Scheduler;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 16/04/12
  * Time: 13:14
  */
 public interface TestCase {
 
     public void setParentScheduler(Scheduler sch);
 
     public ContainerRoot buildTestModel(KevScriptEngine offlineEngine);
 
     public TestResult execute();
 
 }
