 package edu.uca.info2.routing;
 
 import aimax.osm.data.entities.MapNode;
 import edu.uca.info2.components.Area;
 import edu.uca.info2.components.Segment;
 import edu.uca.info2.util.MapUtils;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class ZASearchState {
     // current node
     private MapNode node;
 
     // Para contar la cantidad de veces que se paso p/ un segmento
     private HashMap<Segment, Integer> segmentsCounter = new HashMap<Segment, Integer>();
 
     private Area area;
 
     // maxima cant. de veces que se puede pasar por un segmento
     private int maxSegmentPassThrough = 3;
 
     public ZASearchState(MapNode node, Area area) {
         this.node = node;
         this.area = area;
         List<Segment> segments = area.segmentsInArea();
 
         // inicializa el contador de segmentos
         for (Segment segment : segments) {
             segmentsCounter.put(segment, 0);
         }
     }
 
     public ZASearchState(Segment segment, HashMap<Segment, Integer> segmentsCounter, Area area) {
         this.node = segment.getTo();
         this.area = area;
 
         // inicializa el contador de segmentos
         for (Map.Entry<Segment, Integer> entry : segmentsCounter.entrySet()) {
             this.segmentsCounter.put(entry.getKey(), entry.getValue());
         }
 
         // incrementamos el ultimo segmento atravezado
         incrementSegmentCounter(segment);
     }
 
     public Area getArea() {
         return area;
     }
 
     public MapNode getNode() {
         return node;
     }
 
     public HashMap<Segment, Integer> getSegmentsCounter() {
         return segmentsCounter;
     }
 
     private void incrementSegmentCounter(Segment segment) {
         int counter;
 
         if (segmentsCounter.containsKey(segment)) {
             counter = segmentsCounter.get(segment) + 1;
             segmentsCounter.put(segment, counter);
         } else {
            try {
                 Segment invertedSegment = segment.inverted();
                 counter = segmentsCounter.get(invertedSegment) + 1;
                segmentsCounter.put(segment, counter);
            } catch (NullPointerException e) {
                // debug
                e.printStackTrace();
                System.out.println(area.isNodeInArea(segment.getFrom()));
                System.out.println(area.isNodeInArea(segment.getTo()));
                System.out.println(MapUtils.neighborsForNode(segment.getFrom(), area).contains(segment.getTo()));
            }
         }
     }
 
     public int getMaxSegmentPassThrough() {
         return maxSegmentPassThrough;
     }
 
     public void setMaxSegmentPassThrough(int maxSegmentPassThrough) {
         this.maxSegmentPassThrough = maxSegmentPassThrough;
     }
 
 }
