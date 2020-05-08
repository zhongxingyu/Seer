 /*------------------------------------------------------------------------------
  *******************************************************************************
  * COPYRIGHT Ericsson 2012
  *
  * The copyright to the computer program(s) herein is the property of
  * Ericsson Inc. The programs may be used and/or copied only with written
  * permission from Ericsson Inc. or in accordance with the terms and
  * conditions stipulated in the agreement/contract under which the
  * program(s) have been supplied.
  *******************************************************************************
  *----------------------------------------------------------------------------*/
 package com.ericsson.oss.mediation.test.smoke.testclasses;
 
 import javax.ejb.EJB;
 
 import org.jboss.arquillian.container.test.api.*;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.junit.InSequence;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.shrinkwrap.api.Archive;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.ericsson.oss.mediation.test.IntegrationTestDeploymentFactory;
 import com.ericsson.oss.mediation.test.smoke.dependencies.SmokeTestDependencies;
 import com.ericsson.oss.mediation.test.smoke.deployment.SmokeTestDeploymentFactory;
 import com.ericsson.oss.mediation.test.smoke.deployment.mock.InjectionTestEJB;
 
 /**
  * Rollback transaction test case
  * 
  * @author edejket
  * 
  */
 @RunWith(Arquillian.class)
 public class RollbackTransactionTest {
 
 	/**
 	 * Since we want different scenarios, we will control arq deployment
 	 * manually
 	 * 
 	 */
 	@ArquillianResource
 	private ContainerController controller;
 
 	@ArquillianResource
 	private Deployer deployer;
 
 	@EJB
 	InjectionTestEJB injectedEjb;
 
 	private static final Logger log = LoggerFactory
 			.getLogger(RollbackTransactionTest.class);
 
 	/**
 	 * Create rar deployment from built code
 	 * 
 	 * @return rar deployment
 	 */
 	@Deployment(name = "camel-engine-rar-deployment-rollback", managed = false, testable = false)
 	public static Archive<?> createResourceAdapter() {
 		return IntegrationTestDeploymentFactory
 				.createRARDeploymentFromMavenCoordinates(SmokeTestDependencies.COM_ERICSSON_OSS_MEDIATION_TRANSACT_JCA);
 	}
 
 	/**
 	 * Create war deployment containing this test and simple ejb used to invoke
 	 * ra method under transaction
 	 * 
 	 * @return war deployment
 	 */
 	@Deployment(name = "war-with-ejb-rollback", managed = false, testable = true)
 	public static Archive<?> createWarWithEjb() {
 		return SmokeTestDeploymentFactory.createWarTestDeployment();
 	}
 
 	/**
 	 * Start executing tests, deploy rar
 	 */
 	@Test
 	@InSequence(1)
 	@OperateOnDeployment("camel-engine-rar-deployment-rollback")
 	public void testDeployRar() throws Exception {
 		log.info("<-----------Rollback transaction test case, deploy rar-------------->");
		this.deployer.deploy("camel-engine-rar-deployment-rollback");
 	}
 
 	@Test
 	@InSequence(2)
 	@OperateOnDeployment("war-with-ejb-rollback")
 	public void deployWarWithEjb() throws Exception {
 		log.info("<-----------Rollback transaction test case, deploy test.war-------------->");
 		this.deployer.deploy("war-with-ejb-rollback");
 	}
 
 	@Test(expected = RuntimeException.class)
 	@InSequence(3)
 	@OperateOnDeployment("war-with-ejb-rollback")
 	public void testInjectedRarUnderTransactionWithRollback() throws Exception {
 		log.info("<-----------Rollback transaction test case, invoke invokeRarMethodCauseRollback()-------------->");
 		this.injectedEjb.invokeRarMethodCauseRollback();
 	}
 
	@Ignore
 	@Test
 	@InSequence(4)
 	@OperateOnDeployment("war-with-ejb-rollback")
 	public void undeployWarWithEjb() throws Exception {
 		log.info("<-----------Rollback transaction test case, undeploy test.war-------------->");
 		this.deployer.undeploy("war-with-ejb-rollback");
 	}
 
	@Ignore
 	@Test
 	@InSequence(5)
 	@OperateOnDeployment("camel-engine-rar-deployment-rollback")
 	public void undeployRar() {
 		log.info("<-----------Rollback transaction test case, undeploy rar-------------->");
 		this.deployer.undeploy("camel-engine-rar-deployment-rollback");
 	}
 }
