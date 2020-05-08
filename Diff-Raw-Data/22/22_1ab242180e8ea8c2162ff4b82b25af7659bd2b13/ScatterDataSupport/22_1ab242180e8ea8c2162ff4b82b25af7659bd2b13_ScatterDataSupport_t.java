 package ch.opentrainingcenter.core.charts;
 
 import java.util.List;
 import java.util.Map;
import java.util.TreeMap;
 
 import ch.opentrainingcenter.core.assertions.Assertions;
 import ch.opentrainingcenter.transfer.ITrackPointProperty;
 import ch.opentrainingcenter.transfer.ITraining;
 
 public final class ScatterDataSupport {
     private ScatterDataSupport() {
 
     }
 
     /**
      * Sammelt alle Herzfrequenzen und erstellt folgende Struktur. Liste mit
      * Pairs. Wobei der Key die Herzfrequenz ist und der Value die Anzahl des
      * Vorkommens dieser Herzfrequenz im Training.
      */
     public static float[][] populateHeartData(final ITraining training) {
         final Map<Integer, Integer> map = populate(training);
         final float[][] result = new float[2][map.size()];
         int i = 0;
         for (final Map.Entry<Integer, Integer> entry : map.entrySet()) {
             result[0][i] = entry.getKey();
             result[1][i] = entry.getValue();
             i++;
         }
         return result;
     }
 
     /**
      * Sammelt alle Herzfrequenzen und erstellt folgende Struktur. Liste mit
      * Pairs. Wobei der Key die Herzfrequenz ist und der Value die Anzahl des
      * Vorkommens dieser Herzfrequenz im Training.
      */
     public static Map<Integer, Integer> populate(final ITraining training) {
         Assertions.notNull(training);
         final List<ITrackPointProperty> trackPoints = training.getTrackPoints();
        final Map<Integer, Integer> result = new TreeMap<>();
         for (final ITrackPointProperty trackPoint : trackPoints) {
             final int bpm = trackPoint.getHeartBeat();
             if (bpm > 0) {
                 final Integer intBpm = Integer.valueOf(bpm);
                 if (result.containsKey(intBpm)) {
                     final Integer tmp = result.get(intBpm);
                     result.put(intBpm, tmp.intValue() + 1);
                 } else {
                     result.put(intBpm, 1);
                 }
             }
         }
         return result;
     }
 }
