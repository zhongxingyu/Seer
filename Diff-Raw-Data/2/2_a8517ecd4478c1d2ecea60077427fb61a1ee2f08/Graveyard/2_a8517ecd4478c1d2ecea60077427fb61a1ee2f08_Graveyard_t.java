 package com.id.file;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import com.id.git.FileDelta;
 
 public class Graveyard implements File.Listener {
   private final List<Tombstone> tombstones = new ArrayList<Tombstone>();
   private final List<Grave> graves = new ArrayList<Grave>();
   private Grave otherGrave;
 
   public Graveyard(List<String> lines) {
     for (String line : lines) {
       tombstones.add(new Tombstone(line, line));
       graves.add(new Grave());
     }
     reset();
   }
 
   public void reset() {
     resetRange(0, tombstones.size() - 1);
     otherGrave = new Grave();
   }
 
   public void resetRange(int from, int to) {
     for (int i = from; i <= to; i++) {
       tombstones.get(i).reset();
       graves.set(i, new Grave());
     }
   }
 
   @Override
   public void onLineInserted(int y, String line) {
     Pair splitResult = null;
 
     Grave previousGrave = getGrave(y - 1);
     splitResult = previousGrave.split(line);
     if (splitResult == null) {
       splitResult = new Pair(new Tombstone(line, null), new Grave());
     }
     tombstones.add(y, splitResult.getTombstone());
     graves.add(y, splitResult.getGrave());
   }
 
   @Override
   public void onLineRemoved(int y, String line) {
     Tombstone tombstone = tombstones.remove(y);
     Grave grave = graves.remove(y);
     if (!tombstone.getCurrent().equals(line)) {
       throw new IllegalStateException();
     }
     if (tombstone.getStatus() == Tombstone.Status.NEW && grave.isEmpty()) {
       // Deleting a new line with an empty grave yields nothing to inherit.
       return;
     }
     Grave previousGrave = getGrave(y - 1);
     previousGrave.inherit(tombstone, grave);
   }
 
   @Override
   public void onLineChanged(int y, String oldLine, String newLine) {
     tombstones.get(y).setCurrent(newLine);
   }
 
   public void debug(int y) {
     System.out.println(tombstones.get(y));
     System.out.println(getGrave(y));
   }
   public Tombstone.Status getStatus(int y) {
     return tombstones.get(y).getStatus();
   }
 
   public Grave getGrave(int y) {
     if (y == -1) {
       return otherGrave;
     }
     return graves.get(y);
   }
 
   public Tombstone getTombstone(int y) {
     return tombstones.get(y);
   }
 
   public int size() {
     return tombstones.size();
   }
 
   public boolean isAllGravesEmpty() {
     for (Grave grave : graves) {
       if (!grave.isEmpty()) {
         return false;
       }
     }
     return true;
   }
 
   @Override
   public String toString() {
     StringBuffer result = new StringBuffer();
     for (int i = 0; i < graves.size(); i++) {
       result.append(i)
             .append(": ")
             .append(tombstones.get(i))
             .append(" / ")
             .append(getGrave(i))
             .append("\n");
     }
     return result.toString();
   }
 
   public void setDiffMarkers(FileDelta delta) {
     reset();
 
     for (Entry<Integer, String> addition : delta.getAdditions().entrySet()) {
       int y = addition.getKey();
       tombstones.set(y, new Tombstone(addition.getValue(), null));
     }
     for (Entry<Integer, List<String>> deletion : delta.getDeletions().entrySet()) {
       int y = deletion.getKey();
       for (Tombstone tombstone : Tombstone.deletionsFromLines(deletion.getValue())) {
        getGrave(y).inherit(tombstone, new Grave());
       }
     }
   }
 
   public boolean isAllStatusNormal() {
     for (Tombstone tombstone : tombstones) {
       if (tombstone.getStatus() != Tombstone.Status.NORMAL) {
         return false;
       }
     }
     return true;
   }
 }
