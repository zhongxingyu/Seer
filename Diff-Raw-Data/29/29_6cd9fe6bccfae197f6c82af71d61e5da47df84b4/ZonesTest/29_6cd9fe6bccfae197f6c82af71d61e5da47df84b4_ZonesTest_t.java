 package speedith.core.lang;
 
 import org.junit.Test;
 
 import java.util.*;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
 import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
 import static org.junit.Assert.*;
 import static speedith.core.lang.Zones.*;
 
 public class ZonesTest {
 
     private final Zone zoneAB = Zone.fromInContours("A", "B");
     private final Zone zoneA_B = Zone.fromInContours("A").withOutContours("B");
     private final Zone zoneB_A = Zone.fromInContours("B").withOutContours("A");
     private final Zone zone_AB = Zone.fromOutContours("A", "B");
     private final List<Zone> powerRegionAB = Arrays.asList(zoneAB, zoneA_B, zoneB_A, zone_AB);
     private final Zone zoneAB_CD = Zone.fromInContours("A", "B").withOutContours("C", "D");
 
     @Test
     public void allZonesForContours_should_return_an_empty_list_of_zones_when_given_no_contours() throws Exception {
         assertEquals(new ArrayList<Zone>(), Zones.allZonesForContours());
     }
 
     @Test
     public void allZonesForContours_should_return_a_single_zone_when_given_a_single_contour() throws Exception {
         assertThat(
                 Zones.allZonesForContours("A"),
                 containsInAnyOrder(Zone.fromOutContours("A"), Zone.fromInContours("A"))
         );
     }
 
     @Test
     public void allZonesForContours_should_return_three_zones_when_given_two_contours() throws Exception {
         assertEquals(
                 new HashSet<>(powerRegionAB),
                 new HashSet<>(Zones.allZonesForContours("A", "B"))
         );
     }
 
     @Test
     public void getZonesOutsideContours_should_return_the_same_region_when_given_contours_are_not_in_zones() {
         assertEquals(
                 powerRegionAB,
                 Zones.getZonesOutsideContours(powerRegionAB, "D")
         );
     }
 
     @Test
     public void getZonesOutsideContours_should_return_only_some_zones_when_given_contours_present_in_zones() {
         assertEquals(
                 Arrays.asList(zoneB_A, zone_AB),
                 Zones.getZonesOutsideContours(powerRegionAB, "A")
         );
     }
 
     @Test
     public void sameRegionWithNewContour_should_return_an_empty_region_when_the_initial_region_is_empty() throws Exception {
         assertEquals(
                 new ArrayList<Zone>(),
                 Zones.sameRegionWithNewContours(new ArrayList<Zone>(), "A")
         );
     }
 
     @Test
     public void sameRegionWithNewContour_should_return_an_empty_region_when_given_null_region() throws Exception {
         assertEquals(
                 new ArrayList<Zone>(),
                 Zones.sameRegionWithNewContours(null, "A")
         );
     }
 
     @Test
     public void sameRegionWithNewContour_should_return_the_same_region_expressed_with_the_additional_contour() throws Exception {
         assertEquals(
                 new TreeSet<>(Arrays.asList(zoneB_A, zoneAB)),
                 new TreeSet<>(Zones.sameRegionWithNewContours(Arrays.asList(Zone.fromInContours("B")), "A"))
         );
     }
 
     @Test
     public void sameRegionWithNewContours_when_given_two_contours_should_return_the_initial_region_expressed_with_the_extended_contours() {
         assertEquals(
                 Zones.getZonesInsideAnyContour(allZonesForContours("A", "B", "C"), "B"),
                 sameRegionWithNewContours(Arrays.asList(Zone.fromInContours("B")), "A", "C")
         );
     }
 
     @Test
     public void extendRegionWithNewContour_should_return_the_zone_of_the_contour_when_initial_region_is_empty() {
         assertThat(
                 extendRegionWithNewContour(new ArrayList<Zone>(), "B", null),
                 contains(Zone.fromInContours("B"))
         );
     }
 
     @Test
     public void extendRegionWithNewContour_should_return_a_union_of_regions_when_initial_region_is_not_empty() {
         assertThat(
                 extendRegionWithNewContour(Arrays.asList(Zone.fromInContours("A")), "B", null),
                 containsInAnyOrder(zoneA_B, zoneB_A, zoneAB)
         );
     }
 
     @Test
     public void getZonesInsideAllContours_should_return_the_intersection_of_the_Venn_diagram() {
         assertThat(
                 Zones.getZonesInsideAllContours(allZonesForContours("A", "B", "C", "D"), "A", "B"),
                 equalTo(sameRegionWithNewContours(asList(zoneAB), "C", "D"))
         );
 
     }
 
     @Test
     public void isZoneOutsideContours_should_return_false_when_zone_is_in_the_contour() {
         assertFalse(Zones.isZoneOutsideContours(zoneAB_CD, "A"));
     }
     @Test
     public void isZoneOutsideContours_should_return_false_when_zone_is_in_one_contour_but_not_the_other() {
         assertFalse(Zones.isZoneOutsideContours(zoneAB_CD, "C", "B"));
     }
     @Test
     public void isZoneOutsideContours_should_return_true_when_zone_is_no_contour() {
         assertTrue(Zones.isZoneOutsideContours(zoneAB_CD, "C", "D"));
     }
 }
