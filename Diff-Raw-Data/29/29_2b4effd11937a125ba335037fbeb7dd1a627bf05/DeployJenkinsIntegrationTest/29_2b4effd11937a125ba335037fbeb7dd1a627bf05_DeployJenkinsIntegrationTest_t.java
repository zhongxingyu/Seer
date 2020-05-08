 package org.deploymentobjects.core.application;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.deploymentobjects.core.domain.model.environment.EnvironmentRepository;
 import org.deploymentobjects.core.domain.model.environment.Host;
 import org.deploymentobjects.core.domain.model.execution.DispatchableStep;
 import org.deploymentobjects.core.domain.model.execution.Executable;
 import org.deploymentobjects.core.domain.model.execution.ExitCode;
 import org.deploymentobjects.core.domain.model.execution.Job;
 import org.deploymentobjects.core.domain.shared.EventPublisher;
 import org.deploymentobjects.core.domain.shared.EventStore;
 import org.deploymentobjects.core.infrastructure.execution.JschDispatch;
 import org.deploymentobjects.core.infrastructure.persistence.InMemoryEventStore;
 import org.deploymentobjects.core.infrastructure.persistence.zookeeper.ZookeeperEnvironmentRepository;
import org.deploymentobjects.core.infrastructure.persistence.zookeeper.ZookeeperEventStore;
 import org.deploymentobjects.core.infrastructure.persistence.zookeeper.ZookeeperPersistence;
 import org.junit.Test;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverBackedSelenium;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import com.thoughtworks.selenium.Selenium;
 
 public class DeployJenkinsIntegrationTest {
 
 	String commands = "wget -O /etc/yum.repos.d/jenkins.repo http://pkg.jenkins-ci.org/redhat/jenkins.repo\n"
 			+ "rpm --import http://pkg.jenkins-ci.org/redhat/jenkins-ci.org.key\n"
 			+ "yum install -y jenkins\n"
 			+ "yum install -y java-1.6.0-openjdk java-1.6.0-openjdk-devel\n"
 			+ "chkconfig iptables off\n"
 
 			+ "service iptables stop\n"
 
 			+ "service jenkins start\n";
 
 	@Test
 	public void testDeployJenkins() throws Exception {
 
 
		ZookeeperPersistence persistence = new ZookeeperPersistence("ino:2181");
		EventStore eventStore = new ZookeeperEventStore(persistence);
 		EventPublisher publisher = new EventPublisher(eventStore);
 		EnvironmentRepository repo = ZookeeperEnvironmentRepository.factory(
				persistence, publisher);
 		
 		DeployApplicationCommand cmd = new DeployApplicationCommand.Builder(publisher, 
				"jenkins", "jenkins46383", repo, new JschDispatch(publisher, "root"))
				.addCommandOnNodesByRole(commands, "standard").build();
 		Job job = cmd.create();
 		ExitCode exit = job.execute();
 		assertEquals(ExitCode.SUCCESS, exit);
 		for (Executable exec : cmd.getSteps().getSteps()) {
 			DispatchableStep step = (DispatchableStep) exec;
 			for (Host node : step.getHosts()) {
 				testSiteUp(node.getHostname());
 			}
 		}
 	}
 
 	public void testSiteUp(String hostname) throws Exception {
 		WebDriver driver = new FirefoxDriver();
 		String baseUrl = "http://" + hostname + ":8080/";
 		Selenium selenium = new WebDriverBackedSelenium(driver, baseUrl);
 
 		selenium.open("/");
 		// selenium.click("css=img[alt=\"title\"]");
 		selenium.waitForPageToLoad("30000");
 		if (selenium
 				.isTextPresent("Please wait while Jenkins is getting ready to work")) {
 			Thread.sleep(30000);
 
 		}
 		assertTrue(selenium
 				.isTextPresent("Welcome to Jenkins! Please create new jobs to get started."));
 
 		selenium.stop();
 
 	}
 }
