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
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.ericsson.oss.mediation.test.IntegrationTestDeploymentFactory;
 import com.ericsson.oss.mediation.test.smoke.dependencies.SmokeTestDependencies;
 import com.ericsson.oss.mediation.test.smoke.deployment.SmokeTestDeploymentFactory;
 import com.ericsson.oss.mediation.test.smoke.deployment.mock.InjectionTestEJB;
 
 /**
  * Successfull commit test case
  * 
  * @author edejket
  * 
  */
 @RunWith(Arquillian.class)
 public class CommitTransactionTest {
 
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
 			.getLogger(CommitTransactionTest.class);
 
 	/**
 	 * Create ra deployment from built code
 	 * 
 	 * @return rar deployment
 	 */
 	@Deployment(name = "camel-engine-rar-deployment-commit", managed = false, testable = false)
 	public static Archive<?> createResourceAdapter() {
 		return IntegrationTestDeploymentFactory
 				.createRARDeploymentFromMavenCoordinates(SmokeTestDependencies.COM_ERICSSON_OSS_MEDIATION_TRANSACT_JCA);
 	}
 
 	/**
 	 * Create war deployment containing this test case and simple ejb used to
 	 * triger transactional call to rar
 	 * 
 	 * @return war deployment
 	 */
 	@Deployment(name = "war-with-ejb-commit", managed = false, testable = true)
 	public static Archive<?> createWarWithEjb() {
 		return SmokeTestDeploymentFactory.createWarTestDeployment();
 	}
 
 	/**
 	 * Start executing tests, deploy rar
 	 */
 	@Test
 	@InSequence(1)
 	@OperateOnDeployment("camel-engine-rar-deployment-commit")
 	public void testDeployRar() throws Exception {
 		log.info("<-----------Commit transaction test case, deploying rar-------------->");
 		this.deployer.deploy("camel-engine-rar-deployment-commit");
 	}
 
 	@Test
 	@InSequence(2)
 	@OperateOnDeployment("war-with-ejb-commit")
 	public void deployWarWithEjb() throws Exception {
 		log.info("<-----------Commit transaction test case, deploying test.war-------------->");
 		this.deployer.deploy("war-with-ejb-commit");
 	}
 
 	@Test
 	@InSequence(3)
 	@OperateOnDeployment("war-with-ejb-commit")
 	public void testInjectedRarUnderTransaction() throws Exception {
 		log.info("<-----------invoking invokeRarMethodUnderTransaction-------------->");
 		this.injectedEjb.invokeRarMethodUnderTransaction();
 	}
 
 	@Test
 	@InSequence(4)
 	@OperateOnDeployment("war-with-ejb-commit")
 	public void undeployWarWithEjb() throws Exception {
 		log.info("<-----------Commit transaction test case, undeploy test.war-------------->");
 		this.deployer.undeploy("war-with-ejb-commit");
 	}
 
 	@Test
 	@InSequence(5)
 	@OperateOnDeployment("camel-engine-rar-deployment-commit")
 	public void undeployRar() {
 		log.info("<-----------Commit transaction test case, undeploy rar-------------->");
 		this.deployer.undeploy("camel-engine-rar-deployment-commit");
 	}
 }
