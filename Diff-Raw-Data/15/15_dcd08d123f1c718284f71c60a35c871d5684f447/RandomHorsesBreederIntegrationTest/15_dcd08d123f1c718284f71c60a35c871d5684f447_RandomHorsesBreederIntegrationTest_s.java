 package jobs;
 
 import models.*;
 import models.IntegrationTest;
 
 import org.fest.assertions.Assertions;
 import org.junit.Before;
 import org.junit.Test;
 
 public class RandomHorsesBreederIntegrationTest extends IntegrationTest {
 
 	@Before
 	public void setUp() {
 		new HorseNameSuffix("Zorro").save();
 		new HorseNamePrefix("Windy").save();
 		new HorseNamePrefix("Cloudy").save();
 
		new HorseBuilder().withName("Cloudy Zorro").build().save();
 	}
 
 	@Test
 	public void getRandomHorse_() {
 		Horse randomHorse = RandomHorsesBreeder.createRandomHorse();
		Assertions.assertThat(randomHorse.getName()).isEqualTo("Windy Zorro");

 	}
 
 }
