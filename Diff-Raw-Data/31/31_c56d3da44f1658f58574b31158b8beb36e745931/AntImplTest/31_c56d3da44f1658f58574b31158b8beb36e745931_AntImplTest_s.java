 package adp2.implementations;
 
 import static adp2.implementations.Values.NaG;
 import static adp2.implementations.Values.ant;
 import static adp2.implementations.Values.graphFromList;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import adp2.interfaces.Ant;
 import adp2.interfaces.Graph;
 import adp2.interfaces.Simulation;
 
 public class AntImplTest  {
 	Graph graph2x2;
 	Simulation simulation;
 
 	@Before
 	public void setUp() throws Exception {
 		graph2x2 = graphFromList(0., 4., 4., 0.);
 	}
 
 	@Test
 	public void testValueOf() {
 		// Test ant(int , double ,graph)
 		assertTrue(!(ant(1, 0.1, graph2x2) instanceof NaA));
 
 		assertTrue((ant(1, 0.1, null) instanceof NaA));
 
 		assertTrue((ant(1, 0.1, NaG()) instanceof NaA));
 		assertTrue((ant(1, -0.1, graph2x2) instanceof NaA));
 		assertTrue((ant(1, 1.1, graph2x2) instanceof NaA));
 		assertTrue((ant(3, 0.1, graph2x2) instanceof NaA));
 		assertTrue((ant(-1, 0.1, graph2x2) instanceof NaA));
 
 		// Test ant( double ,graph)
 		assertTrue(!(ant(0.1, graph2x2) instanceof NaA));
 
 		assertTrue((ant(0.1, null) instanceof NaA));
 
 		assertTrue((ant(0.1, NaG()) instanceof NaA));
 		assertTrue((ant(-0.1, graph2x2) instanceof NaA));
 		assertTrue((ant(1.1, graph2x2) instanceof NaA));
 	}
 
 //	@Test
 //	public void testTraveledPath() {
 //		Ant ant = ant(1, 0.2, graph2x2);
 //		simulation = Values.simulation(graph2x2, 1);
 //		assertEquals(1, ant.traveledPath().waypoints().size());
 //		simulation.stochasticNeighborSelection(ant);
 //		simulation.stochasticNeighborSelection(ant);
 //		simulation.stochasticNeighborSelection(ant);
 //		assertEquals(3, ant.traveledPath().waypoints().size());
 //		simulation.stochasticNeighborSelection(ant);
 //		simulation.stochasticNeighborSelection(ant);
 //		simulation.stochasticNeighborSelection(ant);
 //		assertEquals(3, ant.traveledPath().waypoints().size());
 //	}
 //
 //	@Test
 //	public void testPrePosition() {
 //		Ant ant = ant(1, 0.2, graph2x2);
 //		simulation = Values.simulation(graph2x2, 1);
 //		assertEquals(1, ant.prevPosition());
 //		simulation.stochasticNeighborSelection(ant);
 //		assertEquals(1, ant.prevPosition());
 //		simulation.stochasticNeighborSelection(ant);
 //		simulation.stochasticNeighborSelection(ant);
 //		simulation.stochasticNeighborSelection(ant);
 //		simulation.stochasticNeighborSelection(ant);
 //		simulation.stochasticNeighborSelection(ant);
 //		assertEquals(2, ant.prevPosition());
 //	}
 //
 //	@Test
 //	public void testAxiome() {
 //		System.out.println("Axiome Start");
 //		Ant ant = ant(1, 0.2, graph2x2);
 //		simulation = Values.simulation(graph2x2, 1);
 //		while (!ant.hasFinished()) {
 //			System.out.println((ant.traveledPath().waypoints().size() - 1) + "\t" + graph2x2.allNodes().size());
 //			assertTrue((ant.traveledPath().waypoints().size() - 1 == graph2x2.allNodes().size()) == ant.hasFinished());
 //			simulation.stochasticNeighborSelection(ant);
 //		}
 //		assertTrue((ant.traveledPath().waypoints().size() - 1 == graph2x2.allNodes().size()) == ant.hasFinished());
 //		System.out.println((ant.traveledPath().waypoints().size() - 1) + "\t" + graph2x2.allNodes().size());
 //
 //		assertTrue(ant.position() == ant.traveledPath().waypoints().get(ant.traveledPath().waypoints().size() - 1));
 //		System.out.println("Axiome Ende");
 //
 //	}
 
 	@Test
 	public void testBalances() {
 		Ant ant = ant(1, 0.2, graph2x2);
 		/*
 		 * starts at 1, graph has sqrt(4)=2 elements, so next node is 2
 		 * distance is 4.
 		 * alpha is 0.2
		 * balances (calling balance1) calculates: (alpha * (pheromone/maxPheromone) + ((1- alpha) * (1 - (distance/maxDistance)))) * 1000 + 1
 		 * in this particular case 0/0 gets catched and set to 0
		 * (0.2*(0)+((1-0.2)*(1-(4/4))))*1000+1
		 * = (0+(0.8*0))*1000+1
		 * = 0+1
		 * = 1
 		 */
		assertTrue(ant.balances().get(2) == 1.0);
 	}
 
 }
