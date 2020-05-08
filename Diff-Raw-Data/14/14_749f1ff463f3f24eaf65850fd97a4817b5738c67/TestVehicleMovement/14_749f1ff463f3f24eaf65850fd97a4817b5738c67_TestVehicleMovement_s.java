 package traffic.endtoend;
 
 import static org.hamcrest.MatcherAssert.*;
 import static traffic.RoadNetworkMatchers.*;
 import static traffic.VehicleMatchers.*;
 
 import org.junit.Rule;
 import org.junit.Test;
 
 import traffic.Junction;
 import traffic.JunctionFactory;
 import traffic.RoadNetwork;
 import traffic.RoadNetworkBuilder;
 import traffic.SegmentBuilder;
 import traffic.Simulation;
 import traffic.SimulationBuilder;
 import traffic.TrafficModule;
 import traffic.TripFactory;
 import traffic.Vehicle;
 import traffic.VehicleBuilder;
 
 import com.google.guiceberry.GuiceBerryModule;
 import com.google.guiceberry.junit4.GuiceBerryRule;
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 public class TestVehicleMovement {
 
 	public static class TrafficTestModule extends AbstractModule {
 		@Override
 		protected void configure() {
 			install(new GuiceBerryModule());
 			install(new TrafficModule());
 		}
 	};
 
 	@Rule public GuiceBerryRule guiceBerry =
 		      new GuiceBerryRule(TrafficTestModule.class);
 
 	@Inject private JunctionFactory junctionFactory;
 	@Inject private Provider<SimulationBuilder> simulationBuilderProvider;
 	@Inject private Provider<VehicleBuilder> vehicleBuilderProvider;
 	@Inject private Provider<SegmentBuilder> segmentBuilderProvider;
 	@Inject private Provider<RoadNetworkBuilder> roadNetworkBuilderProvider;
 
 	@Test
 	public void tripAcrossTwoSegmentsOfYShapedNetworkWith3SegmentsTakesCorrectAmmountOfTime() throws Exception {
 		final Junction junction0 = junctionFactory.createJunction("junction0");
 		final Junction junction1 = junctionFactory.createJunction("junction1");
 		final Junction junction2 = junctionFactory.createJunction("junction2");
 		final Junction junction3 = junctionFactory.createJunction("junction3");
 
 		final RoadNetwork roadNetwork = roadNetworkBuilderProvider.get()
 			.withSegment(segment()
 				.withName("segment0")
 				.withInJunction(junction0)
 				.withOutJunction(junction1)
 				.withLength(4))
 			.withSegment(segment()
 				.withName("segment1")
 				.withInJunction(junction1)
 				.withOutJunction(junction2)
 				.withLength(3))
 			.withSegment(segment()
 				.withName("segment2")
 				.withInJunction(junction1)
 				.withOutJunction(junction3)
 				.withLength(3))
 			.make();
 
 		final Vehicle vehicle0 = vehicleBuilderProvider.get()
 			.withRoadNetwork(roadNetwork)
 			.withTrip(TripFactory.tripFrom(junction0).to(junction2))
 			.make();
 
 		final Simulation simulation = simulationBuilderProvider.get()
 			.withRoadNetwork(roadNetwork)
 			.withVehicle(vehicle0)
 			.make();
 
		simulation.step(7);
 
 		assertThat(vehicle0, isLocatedAt(junction2));
 		assertThat(vehicle0, hasJourneyTime(10));
 
 	}
 
 	@Test
 	public void tripAcrossTwoSegmentNetworkWithLengths4And3Takes10Timesteps() throws Exception {
 		final Junction junction0 = junctionFactory.createJunction("junction0");
 		final Junction junction1 = junctionFactory.createJunction("junction1");
 		final Junction junction2 = junctionFactory.createJunction("junction2");
 
 		final RoadNetwork roadNetwork = roadNetworkBuilderProvider.get()
 			.withSegment(segment()
 				.withName("segment0")
 				.withInJunction(junction0)
 				.withOutJunction(junction1)
 				.withLength(4))
 			.withSegment(segment()
 				.withName("segment1")
 				.withInJunction(junction1)
 				.withOutJunction(junction2)
 				.withLength(3))
 			.make();
 
 
 
 		final Vehicle vehicle0 = vehicleBuilderProvider.get()
 			.withRoadNetwork(roadNetwork)
 			.withTrip(TripFactory.tripFrom(junction0).to(junction2))
 			.make();
 
 		final Simulation simulation = simulationBuilderProvider.get()
 			.withRoadNetwork(roadNetwork)
 			.withVehicle(vehicle0)
 			.make();
 
 		simulation.step(10);
 
 		assertThat(vehicle0, isLocatedAt(junction2));
 		assertThat(vehicle0, hasJourneyTime(10));
 	}
 
 	@Test
 	public void tripAcrossSingleSegmentNetworkOfLength5Takes7Timesteps() {
 		final Junction junction0 = junctionFactory.createJunction("junction0");
 		final Junction junction1 = junctionFactory.createJunction("junction1");
 
 		final RoadNetwork roadNetwork = roadNetworkBuilderProvider.get()
 			.withSegment(segment()
 				.withName("segment0")
 				.withInJunction(junction0)
 				.withOutJunction(junction1)
 				.withLength(5))
 			.make();
 
 
 		final Vehicle vehicle0 = vehicleBuilderProvider.get()
 			.withRoadNetwork(roadNetwork)
 			.withTrip(TripFactory.tripFrom(junction0).to(junction1))
 			.make();
 
 		//Shouldn't simulation .withRoadNetwork and .withVehicle be builders?
 		final Simulation simulation = simulationBuilderProvider.get()
 			.withRoadNetwork(roadNetwork)
 			.withVehicle(vehicle0)
 			.make();
 
 		simulation.step(7);
 
 		assertThat(vehicle0, isLocatedAt(junction1));
 		assertThat(vehicle0, hasJourneyTime(7));
 	}
 
 	private SegmentBuilder segment() {
 		return segmentBuilderProvider.get();
 	}
 
 
 
 }
