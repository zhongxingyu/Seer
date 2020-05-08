 package speedith.core.lang;
 
 import java.util.*;
 
 public final class Zones {
 
     public static ArrayList<Zone> allZonesForContours(String... contours) {
         ArrayList<Zone> powerRegion = new ArrayList<>();
         for (String contour : contours) {
             addContourToPowerRegion(powerRegion, contour);
         }
         return powerRegion;
     }
 
     public static ArrayList<Zone> getZonesOutsideContours(Collection<Zone> region, String... contours) {
         ArrayList<Zone> zonesOutsideContours = new ArrayList<>();
         for (Zone zone : region) {
             if (!isZonePartOfAnyContour(zone, contours)) {
                 zonesOutsideContours.add(zone);
             }
         }
         return zonesOutsideContours;
     }
 
     public static ArrayList<Zone> getZonesInsideAnyContour(Collection<Zone> region, String... contours) {
         ArrayList<Zone> zonesInsideContours = new ArrayList<>();
         for (Zone zone : region) {
             if (isZonePartOfAnyContour(zone, contours)) {
                 zonesInsideContours.add(zone);
             }
         }
         return zonesInsideContours;
     }
 
     public static SortedSet<Zone> extendRegionWithNewContour(Collection<Zone> region, String newContour, Collection<String> allContoursInInitialRegion) {
         if (region == null || region.isEmpty()) {
             return new TreeSet<>(sameRegionWithNewContours(Arrays.asList(Zone.fromInContours(newContour)), allContoursInInitialRegion));
         } else {
             List<Zone> extendedRegion = sameRegionWithNewContours(region, newContour);
             Collection<String> allContours = allContoursInInitialRegion == null ? region.iterator().next().getAllContours() : allContoursInInitialRegion;
             extendedRegion.addAll(sameRegionWithNewContours(Arrays.asList(Zone.fromInContours(newContour)), allContours));
             return new TreeSet<>(extendedRegion);
         }
     }
 
     public static ArrayList<Zone> sameRegionWithNewContours(Collection<Zone> region, String... newContours) {
         return sameRegionWithNewContours(region, Arrays.asList(newContours));
     }
 
     public static ArrayList<Zone> getZonesInsideAllContours(Collection<Zone> zones, String... contours) {
         if (contours.length == 1) {
             return getZonesInsideAnyContour(zones, contours);
         } else if (contours.length == 0) {
             return new ArrayList<>(zones);
         } else {
             ArrayList<Zone> zonesInsideContours = new ArrayList<>();
             for (Zone zone : zones) {
                 if (isZonePartOfAllContours(zone, contours)) {
                     zonesInsideContours.add(zone);
                 }
             }
             return zonesInsideContours;
         }
     }
 
     private static ArrayList<Zone> sameRegionWithNewContours(Collection<Zone> region, Collection<String> newContours) {
         if (region == null) {
             return new ArrayList<>();
         } else if (newContours == null || newContours.isEmpty()) {
             return new ArrayList<>(region);
         }
         ArrayList<Zone> regionToAddContour = new ArrayList<>(region);
         addContoursToRegion(newContours, regionToAddContour);
         return regionToAddContour;
     }
 
     private static void addContoursToRegion(Collection<String> newContours, ArrayList<Zone> regionToAddContour) {
         for (String newContour : newContours) {
             int currentRegionSize = regionToAddContour.size();
             for (int i = 0; i < currentRegionSize; i++) {
                 Zone zoneToAddContour = regionToAddContour.get(i);
 
                 regionToAddContour.set(i, createZoneWithAddedInContour(newContour, zoneToAddContour));
                 regionToAddContour.add(createZoneWithAddedOutContour(newContour, zoneToAddContour));
             }
         }
     }
 
     public static boolean isZonePartOfAllContours(Zone zone, String... contours) {
         return zone.getInContoursCount() > 0 &&
                zone.getInContours().containsAll(Arrays.asList(contours));
     }
 
     public static boolean isZoneOutsideContours(Zone zone, String... contours) {
         if (zone.getInContoursCount() > 0) {
             for (String contour : contours) {
                 if (zone.getInContours().contains(contour)) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     private static boolean isZonePartOfAnyContour(Zone zone, String[] contours) {
         for (String contour : contours) {
             if (zone.getInContoursCount() > 0 && zone.getInContours().contains(contour)) {
                 return true;
             }
         }
         return false;
     }
 
     private static void addContourToPowerRegion(ArrayList<Zone> powerRegion, String contour) {
         if (powerRegion.isEmpty()) {
             powerRegion.add(Zone.fromInContours(contour));
             powerRegion.add(Zone.fromOutContours(contour));
         } else {
             int oldZonesCount = powerRegion.size();
             for (int i = 0; i < oldZonesCount; i++) {
                 Zone zone = powerRegion.get(i);
                 powerRegion.set(i, createZoneWithAddedInContour(contour, zone));
                 powerRegion.add(createZoneWithAddedOutContour(contour, zone));
             }
         }
     }
 
     private static Zone createZoneWithAddedInContour(String newContour, Zone zone) {
         return new Zone(extendContours(newContour, zone.getInContours()), zone.getOutContours());
     }
 
     private static Zone createZoneWithAddedOutContour(String contour, Zone zone) {
         return new Zone(zone.getInContours(), extendContours(contour, zone.getOutContours()));
     }
 
     private static TreeSet<String> extendContours(String contour, SortedSet<String> oldInContours) {
         TreeSet<String> newContours = new TreeSet<>();
         if (oldInContours != null) {
             newContours.addAll(oldInContours);
         }
         newContours.add(contour);
         return newContours;
     }
 }
