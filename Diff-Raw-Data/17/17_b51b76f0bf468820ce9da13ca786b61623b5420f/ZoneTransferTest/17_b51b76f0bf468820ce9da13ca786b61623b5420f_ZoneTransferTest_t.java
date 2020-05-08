 package speedith.core.reasoning.util.unitary;
 
 import org.junit.Test;
 import speedith.core.lang.PrimarySpiderDiagram;
 import speedith.core.lang.Zone;
 import speedith.core.lang.Zones;
 
 import java.util.ArrayList;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 import static speedith.core.lang.SpiderDiagrams.createPrimarySD;
 
 public class ZoneTransferTest {
 
     private static final ArrayList<Zone> vennABCZones = Zones.allZonesForContours("A", "B", "C");
     private static final ArrayList<Zone> vennABDZones = Zones.allZonesForContours("A", "B", "D");
     public static final PrimarySpiderDiagram vennABCDiagram = createPrimarySD(null, null, null, vennABCZones);
     public static final PrimarySpiderDiagram vennABDDiagram = createPrimarySD(null, null, null, vennABDZones);
     private static final ArrayList<Zone> intersectionAC = Zones.getZonesInsideAllContours(vennABCZones, "A", "C");
     public static final PrimarySpiderDiagram diagramABC_shadedSetAC = createPrimarySD(null, null, intersectionAC, vennABCZones);
    public static final PrimarySpiderDiagram diagramABC_shadedSetC_A = createPrimarySD(null, null, Zones.getZonesOutsideContours(Zones.getZonesInsideAnyContour(vennABCZones, "A", "C"), "A"), vennABCZones);
     private final PrimarySpiderDiagram diagramWithABZones = createPrimarySD(null, null, null, asList(Zone.fromInContours("A", "B")));
     private final PrimarySpiderDiagram diagramWithBCZones = createPrimarySD(null, null, null, asList(Zone.fromOutContours("B", "C")));
 
     @Test
     public void contoursOnlyInSource_returns_the_contour_that_is_present_in_the_source_diagram_but_not_in_the_destination_diagram() throws Exception {
         ZoneTransfer zoneTransfer = new ZoneTransfer(diagramWithABZones, diagramWithBCZones);
         assertThat(
                 zoneTransfer.contoursOnlyInSource(),
                 contains("A")
         );
     }
 
     @Test
     public void contoursOnlyInSource_returns_an_empty_set_when_the_source_diagram_has_no_contours() throws Exception {
         ZoneTransfer zoneTransfer = new ZoneTransfer(createPrimarySD(), diagramWithBCZones);
         assertThat(
                 zoneTransfer.contoursOnlyInSource(),
                 hasSize(0)
         );
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void destinationZonesForSourceContour_should_throw_an_exception_if_the_contour_is_not_in_the_source_diagram() {
         new ZoneTransfer(diagramWithABZones, diagramWithBCZones).zonesInDestinationOutsideContour("D");
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void destinationZonesForSourceContour_should_throw_an_exception_if_the_contour_is_in_the_destination_diagram() {
         new ZoneTransfer(diagramWithABZones, diagramWithBCZones).zonesInDestinationOutsideContour("B");
     }
 
     @Test
     public void zonesInDestinationOutsideContour_should_return_an_empty_set_for_two_Venn_diagrams() {
         ZoneTransfer zoneTransfer = new ZoneTransfer(vennABCDiagram, vennABDDiagram);
         assertThat(
                 zoneTransfer.zonesInDestinationOutsideContour("C"),
                 hasSize(0)
         );
     }
 
     @Test
     public void zonesInDestinationOutsideContour_should_return_all_zones_inside_A_when_A_is_disjoint_to_C() {
         ZoneTransfer zoneTransfer = new ZoneTransfer(diagramABC_shadedSetAC, vennABDDiagram);
         assertThat(
                 zoneTransfer.zonesInDestinationOutsideContour("C"),
                 containsInAnyOrder(Zones.getZonesInsideAllContours(vennABDZones, "A").toArray())
         );
     }

    @Test
    public void zonesInDestinationOutsideContour_should_return_all_zones_outside_A_when_A_is_contains_C() {
        ZoneTransfer zoneTransfer = new ZoneTransfer(diagramABC_shadedSetC_A, vennABDDiagram);
        assertThat(
                zoneTransfer.zonesInDestinationOutsideContour("C"),
                containsInAnyOrder(Zones.getZonesOutsideContours(vennABDZones, "A").toArray())
        );
    }
 }
