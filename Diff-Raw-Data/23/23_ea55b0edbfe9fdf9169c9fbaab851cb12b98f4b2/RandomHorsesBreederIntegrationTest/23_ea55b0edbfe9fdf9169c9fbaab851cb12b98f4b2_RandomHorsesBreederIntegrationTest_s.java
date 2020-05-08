 import models.Horse;
 import models.IntegrationTestCase;
 
 import org.fest.assertions.Assertions;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.Play;
 import play.Play.Mode;
 
 public class RandomHorsesBreederIntegrationTest extends IntegrationTestCase {
 
 	private RandomHorsesBreeder breeder;
 
 	@Before
 	public void setUp() {
		this.breeder = new RandomHorsesBreeder() {
			@Override
			boolean shouldWeRun() {
				return true;
			}
		};
 	}
 
 	@Test
 	public void doJobShouldAddRandomHorsesAndRemoveAllExisting() throws Exception {
 		Play.mode = Mode.DEV;
 
 		addOneHundredHorses();
 		breeder.doJob();
 		Assertions.assertThat(Horse.count()).isGreaterThan(1);
 		Assertions.assertThat(Horse.count()).isLessThan(100);
 	}
 
 	@Test
 	public void doJobShouldOnlyAddHorsesIfDBEmptyInProduction() throws Exception {
 		Play.mode = Mode.PROD;
 
 		breeder.doJob();
 		Assertions.assertThat(Horse.count()).isGreaterThan(1);
 	}
 
 	@Test
 	public void doJobShouldNotAddHorsesIfDBNotEmptyInProduction() throws Exception {
 		Play.mode = Mode.PROD;
 
 		addOneHundredHorses();
 		breeder.doJob();
 		Assertions.assertThat(Horse.count()).isEqualTo(100);
 	}
 
 	private void addOneHundredHorses() {
 		for (int i = 0; i < 100; i++) {
 			new Horse().save();
 		}
 	}
 
 }
