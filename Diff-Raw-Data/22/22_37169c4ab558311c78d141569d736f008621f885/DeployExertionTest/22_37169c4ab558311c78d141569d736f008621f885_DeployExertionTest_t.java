 /*
  * Copyright to the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package junit.sorcer.core.deploy;
 
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.junit.runner.RunWith;
 import org.rioproject.deploy.DeployAdmin;
 import org.rioproject.monitor.ProvisionMonitor;
 import org.rioproject.opstring.OperationalStringManager;
 import sorcer.core.SorcerConstants;
 import sorcer.core.SorcerEnv;
 import sorcer.junit.ExportCodebase;
 import sorcer.junit.SorcerClient;
 import sorcer.junit.SorcerRunner;
 import sorcer.junit.SorcerServiceConfiguration;
 import sorcer.service.*;
 import sorcer.util.Sorcer;
 
 import java.util.logging.Logger;
 
 import static org.junit.Assert.*;
 import static sorcer.eo.operator.*;
 
 /**
  * @author Dennis Reedy
  */
 @RunWith(SorcerRunner.class)
 @Category(SorcerClient.class)
 @ExportCodebase({"org.sorcersoft.sorcer:sorcer-api",
         "org.sorcersoft.sorcer:ju-arithmetic-api"})
 //@SorcerServiceConfiguration(":ex6-cfg-all")
 public class DeployExertionTest extends DeploySetup implements SorcerConstants {
 
     private final static Logger logger = Logger.getLogger(DeployExertionTest.class.getName());
 
     @Test
     public void deployAndExec() throws Exception {
     	long t0 = System.currentTimeMillis();
     	ProvisionMonitor monitor = Util.waitForService(ProvisionMonitor.class);
         Assert.assertNotNull(monitor);
         System.out.println("Waited "+(System.currentTimeMillis()-t0)+" millis for ProvisionMonitor discovery");
         DeployAdmin deployAdmin = (DeployAdmin) monitor.getAdmin();
         //OperationalStringManager manager = deployAdmin.getOperationalStringManager("Sorcer OS");
         //t0 = System.currentTimeMillis();
         //Util.waitForDeployment(manager);
         //System.out.println("Waited "+(System.currentTimeMillis()-t0)+" millis for [Sorcer OS] provisioning");
        Job f1 = Util.createJob();
        //Task f1 = Util.createTaskt();
         assertTrue(f1.isProvisionable());
         verifyExertion(f1);
         /* Run it again to make sure that the existing deployment is used */
         verifyExertion(f1);
     }
 
     private void verifyExertion(Exertion job) throws ExertionException, ContextException {
         System.out.println("Verifying "+job.getName());
     	long t0 = System.currentTimeMillis();
         Exertion out = exert(job);
         System.out.println("Waited "+(System.currentTimeMillis()-t0)+" millis for exerting: " + out.getName());
         assertNotNull(out);
         logger.info("job f1 context: " + jobContext(out));
         logger.info("job f1/f3/result/y3: " + get(out, "f1/f3/result/y3"));
         assertEquals(get(out, "f1/f3/result/y3"), 400.0);
     }
 
 }
