 package org.drools.planner.examples.ras2012.model;
 
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class SyntheticItineraryTest {
 
     private static final boolean EASTBOUND        = true;
     private static final Node    START            = Node.getNode(0);
     private static final Node    END              = Node.getNode(1);
     private static final Node    NOT_ON_ROUTE     = Node.getNode(2);
 
     private static final Train   TRAIN1           = new Train("A1", BigDecimal.ONE, BigDecimal.ONE,
                                                           80, SyntheticItineraryTest.START,
                                                           SyntheticItineraryTest.END, 0, 100, 0,
                                                           null, false,
                                                           !SyntheticItineraryTest.EASTBOUND);
     private static final Train   TRAIN2           = new Train("A2", BigDecimal.ONE, BigDecimal.ONE,
                                                           80, SyntheticItineraryTest.START,
                                                           SyntheticItineraryTest.END, 0, 100, 0,
                                                           null, false,
                                                           !SyntheticItineraryTest.EASTBOUND);
     private static final Arc     ARC1             = new Arc(Track.MAIN_0, BigDecimal.ONE,
                                                           SyntheticItineraryTest.START,
                                                           SyntheticItineraryTest.END);
     private static final Arc     ARC2             = new Arc(Track.MAIN_0, BigDecimal.ONE,
                                                           SyntheticItineraryTest.END,
                                                           SyntheticItineraryTest.NOT_ON_ROUTE);
     private static final Route   ROUTE_IMPOSSIBLE = new Route.Builder(
                                                           !SyntheticItineraryTest.EASTBOUND).add(
                                                           SyntheticItineraryTest.ARC1).build();
     private static final Route   ROUTE_POSSIBLE   = new Route.Builder(
                                                           SyntheticItineraryTest.EASTBOUND).add(
                                                           SyntheticItineraryTest.ARC1).build();
 
     @Test
     public void testConstructor() {
         // no MOWs
         Itinerary i = new Itinerary(SyntheticItineraryTest.ROUTE_POSSIBLE,
                 SyntheticItineraryTest.TRAIN1);
         Assert.assertEquals(SyntheticItineraryTest.TRAIN1, i.getTrain());
         Assert.assertEquals(SyntheticItineraryTest.ROUTE_POSSIBLE, i.getRoute());
         Assert.assertTrue(i.getMaintenances().isEmpty());
         // with MOWs
         final MaintenanceWindow properMOW = new MaintenanceWindow(
                 SyntheticItineraryTest.ARC1.getOrigin(SyntheticItineraryTest.TRAIN1),
                 SyntheticItineraryTest.ARC1.getDestination(SyntheticItineraryTest.TRAIN1), 0, 10);
         final MaintenanceWindow invalidMOW = new MaintenanceWindow(
                 SyntheticItineraryTest.ARC2.getOrigin(SyntheticItineraryTest.TRAIN1),
                 SyntheticItineraryTest.ARC2.getDestination(SyntheticItineraryTest.TRAIN1), 0, 10);
         final Collection<MaintenanceWindow> mows = new LinkedList<MaintenanceWindow>();
         mows.add(properMOW);
         mows.add(invalidMOW);
         i = new Itinerary(SyntheticItineraryTest.ROUTE_POSSIBLE, SyntheticItineraryTest.TRAIN1,
                 mows);
         Assert.assertEquals(SyntheticItineraryTest.TRAIN1, i.getTrain());
         Assert.assertEquals(SyntheticItineraryTest.ROUTE_POSSIBLE, i.getRoute());
         // invalidMOW will be ignored, because it is outside of the route
         Assert.assertEquals(1, i.getMaintenances().size());
         Assert.assertEquals(
                 properMOW,
                 i.getMaintenances().get(
                         SyntheticItineraryTest.ARC1.getOrigin(SyntheticItineraryTest.TRAIN1)));
     }
 
     @Test
     public void testEquals() {
         final Itinerary i = new Itinerary(SyntheticItineraryTest.ROUTE_POSSIBLE,
                 SyntheticItineraryTest.TRAIN1);
         final Itinerary i2 = new Itinerary(SyntheticItineraryTest.ROUTE_POSSIBLE,
                 SyntheticItineraryTest.TRAIN2);
         Assert.assertFalse(i.equals(i2));
         final Itinerary i3 = new Itinerary(SyntheticItineraryTest.ROUTE_POSSIBLE,
                 SyntheticItineraryTest.TRAIN1);
         Assert.assertEquals(i, i3);
         final WaitTime wt = WaitTime.getWaitTime(10);
         i.setWaitTime(Node.getNode(0), wt);
         Assert.assertFalse(i.equals(i3));
         i3.setWaitTime(Node.getNode(0), wt);
         Assert.assertEquals(i, i3);
         Assert.assertFalse(i.equals("nonsense"));
     }
 
     @Test
     public void testGetterAndSetterOnWaitPoint() {
         final Itinerary i = new Itinerary(SyntheticItineraryTest.ROUTE_POSSIBLE,
                 SyntheticItineraryTest.TRAIN1);
         final WaitTime wt = WaitTime.getWaitTime(5);
         final WaitTime wt2 = WaitTime.getWaitTime(10);
         // test insertion
         Assert.assertNull(i.setWaitTime(SyntheticItineraryTest.START, wt));
         Assert.assertSame(wt, i.getWaitTime(SyntheticItineraryTest.START));
         Assert.assertEquals(1, i.getWaitTimes().size());
         // test update
         Assert.assertSame(wt, i.setWaitTime(SyntheticItineraryTest.START, wt2));
         Assert.assertSame(wt2, i.getWaitTime(SyntheticItineraryTest.START));
         Assert.assertEquals(1, i.getWaitTimes().size());
         // test removal by setting null wait time
         Assert.assertSame(wt2, i.setWaitTime(SyntheticItineraryTest.START, null));
         Assert.assertNull(i.getWaitTime(SyntheticItineraryTest.START));
         Assert.assertEquals(0, i.getWaitTimes().size());
     }
 
     @Test(expected = IllegalArgumentException.class)
    public void testGetterAndSetterOutsideWaitPoint() {
        final Itinerary i = new Itinerary(SyntheticItineraryTest.ROUTE_POSSIBLE,
                SyntheticItineraryTest.TRAIN1);
        final WaitTime wt = WaitTime.getWaitTime(10);
        i.setWaitTime(SyntheticItineraryTest.END, wt);
    }

    @Test(expected = IllegalArgumentException.class)
     public void testImpossibleRoute() {
         new Itinerary(SyntheticItineraryTest.ROUTE_IMPOSSIBLE, SyntheticItineraryTest.TRAIN1);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testNullRoute() {
         new Itinerary(null, SyntheticItineraryTest.TRAIN1);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testNullTrain() {
         new Itinerary(SyntheticItineraryTest.ROUTE_POSSIBLE, null);
     }
 
     @Test
     public void testRemoveWaitPoint() {
         final Itinerary i = new Itinerary(SyntheticItineraryTest.ROUTE_POSSIBLE,
                 SyntheticItineraryTest.TRAIN1);
         final WaitTime wt = WaitTime.getWaitTime(5);
         final WaitTime wt2 = WaitTime.getWaitTime(10);
         // test insertion
         Assert.assertNull(i.setWaitTime(SyntheticItineraryTest.START, wt));
         Assert.assertEquals(1, i.getWaitTimes().size());
         Assert.assertSame(wt, i.removeWaitTime(SyntheticItineraryTest.START));
         Assert.assertNull(i.getWaitTime(SyntheticItineraryTest.START));
         Assert.assertEquals(0, i.getWaitTimes().size());
         // test re-insertion
         Assert.assertNull(i.setWaitTime(SyntheticItineraryTest.START, wt2));
         Assert.assertEquals(1, i.getWaitTimes().size());
         i.removeWaitTimes();
         Assert.assertNull(i.getWaitTime(SyntheticItineraryTest.START));
         Assert.assertEquals(0, i.getWaitTimes().size());
     }
 }
