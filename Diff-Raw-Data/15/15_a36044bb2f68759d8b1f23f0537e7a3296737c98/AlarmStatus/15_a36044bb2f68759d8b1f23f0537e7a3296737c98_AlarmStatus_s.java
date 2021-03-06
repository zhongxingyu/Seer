 package org.blitzortung.android.alarm;
 
 import android.location.Location;
 import org.blitzortung.android.data.beans.Stroke;
 import org.blitzortung.android.util.MeasurementSystem;
 
 import java.util.Collection;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 public class AlarmStatus {
 
     private final static String[] DIRECTION_LABELS = {"S", "SW", "W", "NW", "N", "NO", "O", "SO"};
 
     private final static int SECTOR_COUNT = DIRECTION_LABELS.length;
 
     // VisibleForTesting
     protected final AlarmSector[] sectors;
 
     public AlarmStatus(long warnThresholdTime, MeasurementSystem measurementSystem) {
         sectors = new AlarmSector[SECTOR_COUNT];
 
         float bearing = -180;
         for (int i = 0; i < SECTOR_COUNT; i++) {
             sectors[i] = new AlarmSector(bearing, warnThresholdTime, measurementSystem);
             bearing += getSectorWidth();
         }
     }
 
     public void update(long warnThresholdTime, MeasurementSystem measurementSystem) {
         for (AlarmSector sector : sectors) {
             sector.update(warnThresholdTime, measurementSystem);
         }
     }
 
     public void check(Collection<? extends Stroke> strokes, Location location) {
         for (Stroke stroke : strokes) {
            float bearingToStroke = location.bearingTo(stroke.getLocation());
             int sectorIndex = getSectorIndexForBearing(bearingToStroke);
             sectors[sectorIndex].check(stroke, location);
         }
     }
 
     private int getSectorIndexForBearing(double bearing) {
         return ((int) (Math.round(bearing / getSectorWidth())) + getSectorCount() / 2) % getSectorCount();
     }
 
     public float getSectorWidth() {
         return 360.0f / getSectorCount();
     }
 
     public int getSectorCount() {
         return DIRECTION_LABELS.length;
     }
 
     public String getSectorLabel(int sectorNumber) {
         return DIRECTION_LABELS[sectorNumber];
     }
 
     public int getSectorWithClosestStroke() {
         double minDistance = Double.POSITIVE_INFINITY;
         int sectorIndex = -1;
 
         int index = 0;
         for (AlarmSector sector : sectors) {
             if (sector.getMinimumAlarmRelevantStrokeDistance() < minDistance) {
                 minDistance = sector.getMinimumAlarmRelevantStrokeDistance();
                 sectorIndex = index;
             }
             index++;
         }
         return sectorIndex;
     }
 
     public AlarmSector getSector(int sectorIndex) {
         return sectors[sectorIndex];
     }
 
     public float getClosestStrokeDistance() {
         int alarmSector = getSectorWithClosestStroke();
         if (alarmSector >= 0) {
             return sectors[alarmSector].getMinimumAlarmRelevantStrokeDistance();
         } else {
             return Float.POSITIVE_INFINITY;
         }
     }
 
     public float getSectorBearing(int sectorIndex) {
         return sectors[sectorIndex].getBearing();
     }
 
     public AlarmResult getCurrentActivity() {
         int closestStrokeSectorIndex = getSectorWithClosestStroke();
 
         if (closestStrokeSectorIndex >= 0) {
             AlarmSector sector = sectors[closestStrokeSectorIndex];
             String bearingName = getSectorLabel(closestStrokeSectorIndex);
             return new AlarmResult(sector, bearingName);
         }
 
         return null;
     }
 
     public String getTextMessage(float notificationDistanceLimit) {
         SortedMap<Float, Integer> distanceSectors = new TreeMap<Float, Integer>();
 
         for (int sectorIndex = 0; sectorIndex < getSectorCount(); sectorIndex++) {
             AlarmSector sector = getSector(sectorIndex);
             if (sector.getMinimumAlarmRelevantStrokeDistance() <= notificationDistanceLimit) {
                 distanceSectors.put(sector.getMinimumAlarmRelevantStrokeDistance(), sectorIndex);
             }
         }
 
         StringBuilder sb = new StringBuilder();
 
         if (distanceSectors.size() > 0) {
             for (int sectorIndex : distanceSectors.values()) {
                 AlarmSector sector = getSector(sectorIndex);
                 sb.append(getSectorLabel(sectorIndex));
                 sb.append(" ");
                 sb.append(String.format("%.0f%s", sector.getMinimumAlarmRelevantStrokeDistance(), sector.getDistanceUnitName()));
                 sb.append(", ");
             }
             sb.setLength(sb.length() - 2);
         }
 
         return sb.toString();
     }
 }
