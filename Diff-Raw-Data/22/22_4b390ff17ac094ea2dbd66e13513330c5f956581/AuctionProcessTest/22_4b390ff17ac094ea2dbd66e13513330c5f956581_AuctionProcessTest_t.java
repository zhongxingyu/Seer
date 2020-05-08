 package com.camunda.showcase.auction;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.inject.Inject;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 
 import org.activiti.engine.ProcessEngine;
 import org.activiti.engine.RepositoryService;
 import org.activiti.engine.RuntimeService;
 import org.activiti.engine.TaskService;
 import org.activiti.engine.repository.ProcessDefinition;
 import org.activiti.engine.runtime.ProcessInstance;
 import org.activiti.engine.task.Task;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
 import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.camunda.fox.webapp.faces.Util;
 import com.camunda.showcase.auction.domain.Auction;
 import com.camunda.showcase.auction.domain.Auction.CREATE;
 import com.camunda.showcase.auction.domain.Bid;
 import com.camunda.showcase.auction.process.AuctionProducer;
 import com.camunda.showcase.auction.repository.AuctionRepository;
import com.camunda.showcase.auction.repository.BidRepository;
 import com.camunda.showcase.auction.service.AuctionService;
import com.camunda.showcase.auction.service.BidException;
 import com.camunda.showcase.auction.service.TwitterPublishService;
 
 @RunWith(Arquillian.class)
 public class AuctionProcessTest {
 
 	@Deployment
 	public static Archive<?> createDeployment() {
 
 		MavenDependencyResolver resolver =
 			DependencyResolvers
 				.use(MavenDependencyResolver.class);
 
 		// fox platform client and dependencies
 		Collection<JavaArchive> foxPlatformClient = resolver
 				.goOffline()
 				.loadMetadataFromPom("pom.xml")
 				.artifact("com.camunda.fox.platform:fox-platform-client")
 				.resolveAs(JavaArchive.class);
 
 		return ShrinkWrap.create(WebArchive.class)
 
 				// add fox platform client to test archive
 				.addAsLibraries(foxPlatformClient)
 
 				// add beans.xml to activate CDI
 				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
 
         // add marker file for process archive
         .addAsResource("META-INF/processes.xml", "META-INF/processes.xml")
         // add file for persistence
         .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
 
 				// add process diagram
 				.addAsResource("auction-process.bpmn")
 
 				.addClass(TwitterPublishService.class)
         .addClass(AuctionService.class)
         .addClass(AuctionRepository.class)
         .addClass(AuctionProducer.class)
         .addClass(Auction.class)
        .addClass(BidException.class)
        .addClass(BidRepository.class)
         .addClass(Bid.class)
 				.addClass(Util.class);
 	}
 
 	@Inject
 	private ProcessEngine processEngine;
 
 	@Test
 	public void testProcessDeployment() {
 
 		RepositoryService repositoryService = processEngine.getRepositoryService();
 
 		ProcessDefinition processDefinition = repositoryService
 			.createProcessDefinitionQuery()
 				.processDefinitionName("Auction Process")
 				.singleResult();
 
 		Assert.assertNotNull(processDefinition);
 	}
 
 	@Inject
 	private RuntimeService runtimeService;
 
 	@Inject
 	private TaskService taskService;
 
 	@Inject
 	private AuctionRepository auctionRepository;
 
 	@Inject
 	private AuctionService auctionService;
 
 	@After
 	public void after() {
     auctionRepository.deleteAll();
 	}
 
 	@Test
 	public void testWalkThroughProcess() throws Exception {
 
 		// start process ////////////////////
 
 	  Auction auction = new Auction("aaa", "sasa", new Date());
 
 	  // create auction and start process
 	  auctionService.createAuction(auction);
 
 		ProcessInstance pi = runtimeService
 				.createProcessInstanceQuery()
 					.processDefinitionKey("auction-process")
 					.singleResult();
 
 		Assert.assertNotNull(pi);
 
 		// process variable must exist
 		long auctionId = (Long) runtimeService.getVariable(pi.getId(), "auctionId");
 
 
 		Auction auctionFromRepository = auctionRepository.findById(auctionId);
 
 		// auction must exist in repository
 		Assert.assertNotNull(auctionFromRepository);
 
 		// end start process ////////////////
 
 
 		// complete task 1 //////////////////
 
 		List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
 		Assert.assertEquals(1, tasks.size());
 
 		Task authorizeAuctionTask = tasks.get(0);
 
 		auctionService.authorizeAuction(authorizeAuctionTask.getId(), true);
 
 		// end complete task 1 ///////////////
 
    System.out.println(auction.getEndTime());
    System.out.println(new Date());
    
    // add highest bid ///////////////////

    auctionService.placeBid(auction.getId(), 200, "Klaus");
 
 		// wait for auction end //////////////
 
 		// wait for 6 seconds
		Thread.sleep(10000);
 
 		// end wait for auction end //////////
 
 
 		// complete task 2 ///////////////////
 
 		List<Task> tasksAfterTimer = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
 		Assert.assertEquals(1, tasksAfterTimer.size());
 
 		Task billingAndShippingTask = tasksAfterTimer.get(0);
 
 		// complete task
 		taskService.complete(billingAndShippingTask.getId());
 
 		// end complete task 2 ///////////////
 
 		// check if process instance is really ended
 
 		long runningInstancesCount = runtimeService
 				.createProcessInstanceQuery()
 				.processInstanceId(pi.getId())
 				.count();
 
 		Assert.assertEquals(0, runningInstancesCount);
 	}
 
 	@Test
 	public void testValidation() throws Exception {
     Auction auction = new Auction("aaa", "sasa", new Date());
 
     Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
     Set<ConstraintViolation<Auction>> constraintViolations = validator.validate(auction);
 
     Assert.assertEquals(0, constraintViolations.size());
 	}
 
   @Test
   public void testValidationWithEDITGroup() throws Exception {
     Auction auction = new Auction("aaa", "sasa", new Date());
 
     Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
     Set<ConstraintViolation<Auction>> constraintViolations = validator.validate(auction, CREATE.class);
 
     Assert.assertEquals(3, constraintViolations.size());
   }
 }
