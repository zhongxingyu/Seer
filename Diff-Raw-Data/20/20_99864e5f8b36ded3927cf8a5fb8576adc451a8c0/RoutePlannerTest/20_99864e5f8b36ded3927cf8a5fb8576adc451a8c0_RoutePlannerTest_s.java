 /**
  * 
  */
 package rinde.evo4mas.gendreau06.route;
 
 import static com.google.common.collect.Lists.newLinkedList;
 import static java.util.Arrays.asList;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.math3.random.MersenneTwister;
 import org.apache.commons.math3.random.RandomGenerator;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 import rinde.ecj.GPFuncNode;
 import rinde.ecj.GPProgram;
 import rinde.ecj.GenericFunctions;
 import rinde.ecj.Heuristic;
 import rinde.evo4mas.gendreau06.GCBuilderReceiver;
 import rinde.evo4mas.gendreau06.GSimulation.Configurator;
 import rinde.evo4mas.gendreau06.GSimulationTestUtil;
 import rinde.evo4mas.gendreau06.GendreauContext;
 import rinde.evo4mas.gendreau06.GendreauContextBuilder;
 import rinde.sim.core.Simulator;
 import rinde.sim.core.TimeLapse;
 import rinde.sim.core.graph.Point;
 import rinde.sim.core.model.Model;
 import rinde.sim.core.model.pdp.PDPModel;
 import rinde.sim.core.model.pdp.Parcel;
 import rinde.sim.core.model.pdp.Vehicle;
 import rinde.sim.core.model.road.RoadModel;
 import rinde.sim.problem.common.AddParcelEvent;
 import rinde.sim.problem.common.AddVehicleEvent;
 import rinde.sim.problem.common.DefaultParcel;
 import rinde.sim.problem.common.DefaultVehicle;
 import rinde.sim.problem.common.DynamicPDPTWProblem;
 import rinde.sim.problem.common.ParcelDTO;
 import rinde.sim.problem.common.VehicleDTO;
 import rinde.sim.problem.gendreau06.Gendreau06Scenario;
 import rinde.sim.problem.gendreau06.GendreauTestUtil;
 import rinde.sim.scenario.TimedEvent;
 import rinde.sim.util.TimeWindow;
 import rinde.solver.pdptw.SingleVehicleSolverAdapter;
 import rinde.solver.pdptw.SolverValidator;
 import rinde.solver.pdptw.single.HeuristicSolver;
 import rinde.solver.pdptw.single.MipSolver;
 
 import com.google.common.collect.ImmutableSet;
 
 /**
  * Tests all known implementations of the {@link RoutePlanner} interface.
  * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
  */
 @RunWith(Parameterized.class)
 public class RoutePlannerTest {
     protected final RPBuilder rpBuilder;
     protected RoutePlanner routePlanner;
     protected DynamicPDPTWProblem problem;
     protected RoadModel roadModel;
     protected PDPModel pdpModel;
     protected Simulator simulator;
     protected DefaultVehicle truck;
 
     public RoutePlannerTest(RPBuilder rp) {
         rpBuilder = rp;
     }
 
     @Parameters
     public static Collection<Object[]> configs() {
         return Arrays.asList(new Object[][] {
         /* */
         { new RPBuilder() {
             public RoutePlanner build() {
                 return new RandomRoutePlanner(123);
             }
         } }, /* */
         { new RPBuilder() {
             public RoutePlanner build() {
                 return new SolverRoutePlanner(new SingleVehicleSolverAdapter(
                         SolverValidator.wrap(new MipSolver())));
             }
         } },/* */
         { new RPBuilder() {
             public RoutePlanner build() {
                 return new SolverRoutePlanner(new SingleVehicleSolverAdapter(
                         SolverValidator.wrap(new HeuristicSolver(
                                 new MersenneTwister(123)))));
             }
         } }, /* */
         { new RPBuilder() {
             public RoutePlanner build() {
                 return new EvoHeuristicRoutePlanner(DUMMY_HEURISTIC);
             }
         } }, /* */
         { new RPBuilder() {
             public RoutePlanner build() {
                 return new TestRoutePlanner();
             }
         } } });
     }
 
     interface RPBuilder {
         RoutePlanner build();
     }
 
     @Before
     public void setUp() {
         routePlanner = rpBuilder.build();
 
         int numOnMap = 10;
         int numInCargo = 10;
         if (routePlanner instanceof SolverRoutePlanner) {
             numOnMap = 2;
             numInCargo = 4;
         }
 
         final RandomGenerator rng = new MersenneTwister(123);
         final List<TimedEvent> events = newLinkedList();
         for (int i = 0; i < numOnMap; i++) {
             events.add(newParcelEvent(new Point(rng.nextDouble() * 5, rng
                     .nextDouble() * 5), new Point(rng.nextDouble() * 5, rng
                     .nextDouble() * 5)));
         }
         final Gendreau06Scenario scen = GendreauTestUtil.create(events);
 
         problem = GSimulationTestUtil.init(scen, new TestConfigurator(), false);
         simulator = problem.getSimulator();
         roadModel = simulator.getModelProvider().getModel(RoadModel.class);
         pdpModel = simulator.getModelProvider().getModel(PDPModel.class);
         simulator.tick();
 
         assertEquals(1, roadModel.getObjectsOfType(Vehicle.class).size());
         truck = roadModel.getObjectsOfType(DefaultVehicle.class).iterator()
                 .next();
 
         for (int i = 0; i < numInCargo; i++) {
             final Parcel p = createParcel(rng);
             pdpModel.register(p);
             pdpModel.addParcelIn(truck, p);
         }
 
         if (routePlanner instanceof GCBuilderReceiver) {
             ((GCBuilderReceiver) routePlanner)
                     .receive(new GendreauContextBuilder(roadModel, pdpModel,
                             truck));
         }
     }
 
     @Test
     public void testRouteCompleteness() {
         assertNull(routePlanner.prev());
         assertNull(routePlanner.current());
         assertFalse(routePlanner.hasNext());
         assertTrue(routePlanner.getHistory().isEmpty());
 
         routePlanner.init(roadModel, pdpModel, truck);
 
         assertNull(routePlanner.prev());
         assertNull(routePlanner.current());
         assertFalse(routePlanner.hasNext());
         assertTrue(routePlanner.getHistory().isEmpty());
 
         final Collection<Parcel> onMap = roadModel
                 .getObjectsOfType(Parcel.class);
         final Collection<Parcel> inCargo = pdpModel.getContents(truck);
         final List<Parcel> visited = newLinkedList();
         routePlanner.update(onMap, 0);
 
         assertNull(routePlanner.prev());
         assertNotNull(routePlanner.current());
         assertTrue(routePlanner.hasNext());
         assertTrue(routePlanner.getHistory().isEmpty());
 
         while (routePlanner.hasNext()) {
             visited.add(routePlanner.current());
             assertEquals("current must keep the same value during repeated invocations", routePlanner
                     .current(), routePlanner.current());
             routePlanner.next(0);
             assertEquals(visited.get(visited.size() - 1), routePlanner.prev());
         }
 
         assertEquals(visited, routePlanner.getHistory());
         assertNull(routePlanner.current());
         assertNull(routePlanner.next(0));
 
         assertEquals("total number of stops should equal num locations", (onMap.size() * 2)
                 + inCargo.size(), visited.size());
 
         for (final Parcel p : onMap) {
             assertEquals(2, Collections.frequency(visited, p));
         }
         for (final Parcel p : inCargo) {
             assertEquals(1, Collections.frequency(visited, p));
         }
 
     }
 
     @Test
     public void testMultiUpdate() {
         routePlanner.init(roadModel, pdpModel, truck);
 
         final Collection<Parcel> empty = ImmutableSet.of();
         final Collection<Parcel> singleCargo = ImmutableSet.of(pdpModel
                 .getContents(truck).iterator().next());
         final Parcel mapParcel = roadModel.getObjectsOfType(Parcel.class)
                 .iterator().next();
         final Collection<Parcel> singleOnMap = ImmutableSet.of(mapParcel);
 
         routePlanner.update(empty, 0);
         assertNull(routePlanner.prev());
 
         assertEquals(1, singleOnMap.size());
         assertEquals(1, singleCargo.size());
         routePlanner.update(singleOnMap, 0);
         assertEquals(0, routePlanner.getHistory().size());
 
         assertEquals(mapParcel, routePlanner.next(0));
         assertTrue(routePlanner.hasNext());
         assertNull(routePlanner.next(0));
 
         assertEquals(asList(mapParcel, mapParcel), routePlanner.getHistory());
     }
 
     // init can be called only once
     @Test(expected = IllegalStateException.class)
     public void testInitTwice() {
         routePlanner.init(roadModel, pdpModel, truck);
         routePlanner.init(roadModel, pdpModel, truck);
     }
 
     // init needs to be called before update
     @Test(expected = IllegalStateException.class)
     public void testNotInitializedUpdate() {
         routePlanner.update(null, 0);
     }
 
     // update needs to be called before next
     @Test(expected = IllegalStateException.class)
     public void testNotInitializedNext() {
         routePlanner.next(0);
     }
 
     @Test
     public void testEmpty() {
         routePlanner.init(roadModel, pdpModel, truck);
 
         final Collection<Parcel> s1 = ImmutableSet.of();
         routePlanner.update(s1, 0);
     }
 
     static Parcel createParcel(RandomGenerator rng) {
         final ParcelDTO dto = new ParcelDTO(/* */
         new Point(rng.nextDouble(), rng.nextDouble()),/* start pos */
         new Point(rng.nextDouble(), rng.nextDouble()),/* dest pos */
         new TimeWindow(0, 100000),/* pickup tw */
         new TimeWindow(0, 100000),/* deliver tw */
         0,/* needed capacity */
         -1,/* order arrival time */
         3000,/* pickup duration */
         3000 /* delivery duration */);
 
         return new DefaultParcel(dto);
     }
 
     AddParcelEvent newParcelEvent(Point origin, Point destination) {
         return new AddParcelEvent(new ParcelDTO(origin, destination,
                 new TimeWindow(0, 3600000), new TimeWindow(1800000, 5400000),
                 0, -1, 300000, 300000));
     }
 
     AddParcelEvent newParcelEvent(Point origin, Point destination,
             TimeWindow pickup, TimeWindow delivery) {
         return new AddParcelEvent(new ParcelDTO(origin, destination, pickup,
                 delivery, 0, -1, 300000, 300000));
     }
 
     static Heuristic<GendreauContext> DUMMY_HEURISTIC = new GPProgram<GendreauContext>(
             new GPFuncNode<GendreauContext>(
                     new GenericFunctions.Constant<GendreauContext>(0d)));
 
     class TestConfigurator implements Configurator {
         public boolean create(Simulator sim, AddVehicleEvent event) {
             return sim.register(new TestTruck(event.vehicleDTO));
         }
 
         public Model<?>[] createModels() {
             return new Model<?>[] {};
         }
     }
 
     class TestTruck extends DefaultVehicle {
         public TestTruck(VehicleDTO dto) {
             super(dto);
         }
 
         // don't do anything
         @Override
         protected void tickImpl(TimeLapse time) {}
     }
 }
