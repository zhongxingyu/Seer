 package dart.blackcat.talker;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * Unit test for simple App.
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"classpath:context.xml"})
 public class AppTest {
 
 	@Test
 	public void main() {
		Boot.main(new String[] {"Я пошел гулять"});
 	}
 }
