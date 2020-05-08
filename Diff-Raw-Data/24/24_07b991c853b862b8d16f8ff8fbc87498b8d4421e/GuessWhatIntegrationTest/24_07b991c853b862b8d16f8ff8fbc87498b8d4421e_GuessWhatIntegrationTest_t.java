 package bneijt.guesswhat;
 
import static org.junit.Assert.assertNotNull;
 
 import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
@ContextConfiguration(locations = "/guesswhat-app.xml")
@RunWith(SpringJUnit4ClassRunner.class)
 public class GuessWhatIntegrationTest {
	@Autowired
	private GuessWhat controller;
	
	@Test
	public void shouldLoadConfiguration() throws Exception {
		// load application context
		assertNotNull(this.controller);
	}
 }
