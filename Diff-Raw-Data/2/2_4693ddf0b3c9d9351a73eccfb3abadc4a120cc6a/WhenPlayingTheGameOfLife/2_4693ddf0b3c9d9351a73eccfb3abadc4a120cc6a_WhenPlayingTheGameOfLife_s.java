 package se.athega.lizell.gameoflife;
 
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class WhenPlayingTheGameOfLife {
 	private World world;
 
 	@Before
 	public void setUp() throws Exception {
 		world = new World();
 	}
 
 	@Test
 	public void anEmptyGridRemainsEmptyThroughGenerations() {
 		world.createNextGeneration();
 		assertThat(world.getGrid().toString(), is("---\n---\n---\n"));
 	}
 
 	@Test
 	public void aSingleLiveCellShouldDieInTheNextGeneration() throws Exception {
 		world = new World(
 				"---\n" +
 				"-#-\n" +
 				"---\n"
 		);
 
 		world.createNextGeneration();
 
 		assertThat(world.getGrid().toString(), is(
 				"---\n" +
 				"---\n" +
 				"---\n"
 		));
 	}
 
 	@Test
	public void liveCellsWith2or3iveNeighbourShouldRemainAliveInTheNextGeneration() throws Exception {
 		world = new World(
 				"##-\n" +
 				"##-\n" +
 				"---\n"
 		);
 
 		world.createNextGeneration();
 
 		assertThat(world.getGrid().toString(), is(
 				"##-\n" +
 				"##-\n" +
 				"---\n"
 		));
 	}
 
 	@Test
 	public void aLiveCellWithExactlyTwoLiveNeighbourShouldRemainAliveInTheNextGeneration() throws Exception {
 		world = new World(
 				"---\n" +
 				"###\n" +
 				"---\n"
 		);
 
 		world.createNextGeneration();
 
 		assertThat(world.getGrid().toString(), is(
 				"-#-\n" +
 				"-#-\n" +
 				"-#-\n"
 		));
 	}
 }
