 package ch.opentrainingcenter.model.training.internal;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import ch.opentrainingcenter.core.assertions.Assertions;
 import ch.opentrainingcenter.core.data.Pair;
 import ch.opentrainingcenter.model.training.IGoldMedalModel;
 import ch.opentrainingcenter.model.training.Intervall;
 
 public class GoldMedalModel implements IGoldMedalModel {
 
     private static final Pair<Long, String> EMPTY_PAIR = new Pair<>(null, "-"); //$NON-NLS-1$
    private static final Map<Intervall, Pair<Long, String>> SCHNELLSTE_PACES = new HashMap<>();
 
    static {
        for (final Intervall intervall : Intervall.values()) {
            SCHNELLSTE_PACES.put(intervall, EMPTY_PAIR);
        }
    }
 
     private Pair<Long, String> schnellstePace = EMPTY_PAIR;
     private Pair<Long, String> longestDistance = EMPTY_PAIR;
     private Pair<Long, String> longestRun = EMPTY_PAIR;
     private Pair<Long, String> highestPulse = EMPTY_PAIR;
     private Pair<Long, String> highestAveragePulse = EMPTY_PAIR;
     private Pair<Long, String> lowestAveragePulse = EMPTY_PAIR;
 
     @Override
     public Pair<Long, String> getSchnellstePace() {
         return schnellstePace;
     }
 
     @Override
     public void setSchnellstePace(final Pair<Long, String> schnellstePace) {
         Assertions.notNull(schnellstePace);
         this.schnellstePace = getValidatedPair(schnellstePace);
     }
 
     @Override
     public Pair<Long, String> getSchnellstePace(final Intervall intervall) {
         Assertions.notNull(intervall);
         return SCHNELLSTE_PACES.get(intervall);
     }
 
     @Override
     public void setSchnellstePace(final Intervall intervall, final Pair<Long, String> schnellstePace) {
         Assertions.notNull(intervall);
         Assertions.notNull(schnellstePace);
         SCHNELLSTE_PACES.put(intervall, getValidatedPair(schnellstePace));
     }
 
     @Override
     public Pair<Long, String> getLongestDistance() {
         return longestDistance;
     }
 
     @Override
     public void setLongestDistance(final Pair<Long, String> longestDistance) {
         Assertions.notNull(longestDistance);
         this.longestDistance = getValidatedPair(longestDistance);
     }
 
     @Override
     public Pair<Long, String> getLongestRun() {
         return longestRun;
     }
 
     @Override
     public void setLongestRun(final Pair<Long, String> longestRun) {
         Assertions.notNull(longestRun);
         this.longestRun = getValidatedPair(longestRun);
     }
 
     @Override
     public Pair<Long, String> getHighestPulse() {
         return highestPulse;
     }
 
     @Override
     public void setHighestPulse(final Pair<Long, String> highestPulse) {
         Assertions.notNull(highestPulse);
         this.highestPulse = getValidatedPair(highestPulse);
     }
 
     @Override
     public Pair<Long, String> getHighestAveragePulse() {
         return highestAveragePulse;
     }
 
     @Override
     public void setHighestAveragePulse(final Pair<Long, String> highestAveragePulse) {
         Assertions.notNull(highestAveragePulse);
         this.highestAveragePulse = getValidatedPair(highestAveragePulse);
     }
 
     @Override
     public Pair<Long, String> getLowestAveragePulse() {
         return lowestAveragePulse;
     }
 
     @Override
     public void setLowestAveragePulse(final Pair<Long, String> lowestAveragePulse) {
         Assertions.notNull(lowestAveragePulse);
         this.lowestAveragePulse = getValidatedPair(lowestAveragePulse);
     }
 
     private Pair<Long, String> getValidatedPair(final Pair<Long, String> value) {
         if (value.getFirst() == null || value.getSecond() == null) {
             return EMPTY_PAIR;
         } else {
             return value;
         }
     }
 
     @Override
     public Pair<Long, String> getEmpty() {
         return EMPTY_PAIR;
     }
 }
