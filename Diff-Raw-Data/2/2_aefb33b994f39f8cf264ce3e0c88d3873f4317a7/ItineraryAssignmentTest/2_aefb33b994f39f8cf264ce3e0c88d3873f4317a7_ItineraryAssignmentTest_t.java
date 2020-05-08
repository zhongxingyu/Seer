 package org.drools.planner.examples.ras2012.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.drools.planner.examples.ras2012.ProblemSolution;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runners.Parameterized.Parameters;
 
 public class ItineraryAssignmentTest extends AbstractItineraryProviderBasedTest {
 
     private static Map<Node, MaintenanceWindow> convertMOWs(
             final Collection<MaintenanceWindow> mows, final Itinerary i) {
         final Route r = i.getRoute();
         final Map<Node, MaintenanceWindow> result = new HashMap<Node, MaintenanceWindow>();
         for (final MaintenanceWindow mow : mows) {
             if (i.isNodeOnRoute(mow.getOrigin(r)) && i.isNodeOnRoute(mow.getDestination(r))) {
                 result.put(mow.getOrigin(r), mow);
             }
         }
         return result;
     }
 
     @Parameters
     public static Collection<Object[]> getInput() {
         final Collection<Object[]> providers = new ArrayList<Object[]>();
         for (final ItineraryProvider p : AbstractItineraryProviderBasedTest.getProviders()) {
             for (final Map.Entry<String, int[]> routes : p.getExpectedValues().entrySet()) {
                 for (final int routeId : routes.getValue()) {
                     providers.add(new Object[] { p.getSolution(),
                             p.getItinerary(routes.getKey(), routeId) });
                 }
             }
         }
         return providers;
     }
 
     private final ProblemSolution solution;
     private final Itinerary       expectedItinerary;
     private final Train           expectedTrain;
     private final Route           expectedRoute;
 
     public ItineraryAssignmentTest(final ProblemSolution solution, final Itinerary expectedItinerary) {
         this.solution = solution;
         this.expectedItinerary = expectedItinerary;
         this.expectedRoute = expectedItinerary.getRoute();
         this.expectedTrain = expectedItinerary.getTrain();
     }
 
     @Test
     public void testClone() {
         final ItineraryAssignment ia = new ItineraryAssignment(this.expectedTrain,
                 this.solution.getMaintenances());
         ia.setRoute(this.expectedRoute);
         final ItineraryAssignment ia2 = ia.clone();
         Assert.assertNotSame(ia2, ia);
        Assert.assertNotSame(ia.getItinerary(), ia2.getItinerary());
     }
 
     @Test
     public void testConstructorSimple() {
         final ItineraryAssignment ia = new ItineraryAssignment(this.expectedTrain,
                 this.solution.getMaintenances());
         Assert.assertSame(this.expectedTrain, ia.getTrain());
         Assert.assertNull(ia.getRoute());
         try {
             ia.getItinerary();
             Assert.fail("No route provided, getItinerary() should have failed.");
         } catch (final IllegalStateException ex) {
             // requesting itinerary without setting a route should have failed
         }
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorWithNullTrain() {
         new ItineraryAssignment(null, this.solution.getMaintenances());
     }
 
     @Test
     public void testRouteGetterAndSetter() {
         final ItineraryAssignment ia = new ItineraryAssignment(this.expectedTrain,
                 this.solution.getMaintenances());
         ia.setRoute(this.expectedRoute);
         Assert.assertSame(this.expectedRoute, ia.getRoute());
         final Itinerary itinerary = ia.getItinerary();
         Assert.assertEquals(this.expectedItinerary, itinerary);
         Assert.assertEquals(this.expectedTrain, itinerary.getTrain());
         Assert.assertEquals(this.expectedRoute, itinerary.getRoute());
         Assert.assertEquals(
                 ItineraryAssignmentTest.convertMOWs(this.solution.getMaintenances(), itinerary),
                 itinerary.getMaintenances());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testRouteSetterImpossible() {
         final ItineraryAssignment ia = new ItineraryAssignment(this.expectedTrain,
                 this.solution.getMaintenances());
         ia.setRoute(new Route.Builder(this.expectedTrain.isWestbound()).build());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testRouteSetterNull() {
         final ItineraryAssignment ia = new ItineraryAssignment(this.expectedTrain,
                 this.solution.getMaintenances());
         ia.setRoute(null);
     }
 
     @Test
     public void testRouteSetterTwiceTheSame() {
         final ItineraryAssignment ia = new ItineraryAssignment(this.expectedTrain,
                 this.solution.getMaintenances());
         ia.setRoute(this.expectedRoute);
         // itinerary isn't null, so getItinerary() works
         final Itinerary itinerary = ia.getItinerary();
         // the itinerary doesn't change when the route doesn't change
         ia.setRoute(this.expectedRoute);
         Assert.assertSame(itinerary, ia.getItinerary());
     }
 
 }
