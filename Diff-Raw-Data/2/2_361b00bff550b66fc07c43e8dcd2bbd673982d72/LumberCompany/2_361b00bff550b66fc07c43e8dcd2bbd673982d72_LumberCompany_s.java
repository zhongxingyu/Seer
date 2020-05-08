 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package woodcock;
 
 import java.util.*;
 
 /**
  *
  * @author Z98
  */
 public class LumberCompany {
     /*
      * This class goes through forest patches in order to order them based on
      * how desirable or profitable cutting a patch would be.
      */
 
     public static final double MIN_VALUE = 250;
     private PriorityQueue<Patch> lumberCandidates;
     public HashMap<List<Integer>, Patch> candidateMap;
     
     public int harvestLimit = 750;
     
     public double requiredMin = MIN_VALUE;
     
     public double harvestQuota = WCConservation.requiredHabitats * MIN_VALUE;
 
     public LumberCompany(int forestPatchSize) {
         Comparator<Patch> comparator = new PatchLumberComparator();
         lumberCandidates = new PriorityQueue<>(forestPatchSize, comparator);
         candidateMap = new HashMap<>();
     }
 
     // check within the range 1000 for suitable landing area. 
     // assume that the shipping cost is a direct approximation to the distance travelled
     public void queueTimberPatch(RTree timberSuitable, Patch p) {
         //int rangeLanding = 100;
         //while (rangeLanding < 1000) {
             //if (Calculation.rangeQuery(timberSuitable, x, y, rangeLanding) != null) {
               //  p.lumberProfit -= rangeLanding;
                 if (p.lumberProfit > MIN_VALUE) {
                     lumberCandidates.add(p);
                     List<Integer> key = Arrays.asList(p.x, p.y);
                     candidateMap.put(key, p);
                 //}
             //}
            // rangeLanding += 100;
         }
     }
 
     public boolean isCandidate(Patch p) {
         List<Integer> key = Arrays.asList(p.x, p.y);
         Patch patch = candidateMap.get(key);
         if (patch == null) {
             return false;
         }
 
         return true;
     }
 
     public PriorityQueue<Patch> getPQueue() {
         return lumberCandidates;
     }
     /*
      * Search associated rtree with point(x, y) coordinates as the centre point
      * with the radius specified. Radius is in acre unit.
      *
      * @parameter	rtree - rtree to search from @parameter	xCoor - xCoor of the
      * centre point for the range search @parameter	yCoor - yCoor of the centre
      * point for the range search @parameter	radius - radius from the point for
      * the range search
      *
      * @return one of the point within the range
      */
     
     public double ClearCut(PriorityQueue<Patch> candidates)
     {
         double value = 0;
         for(Patch candidate : candidates)
         {
             value += candidate.ClearCut();
         }
         
         return value;
     }
     
     public int ConservationHarvested(PriorityQueue<Patch> candidates, PriorityQueue<Patch> cutSelect)
     {
         if(CalcProfit(candidates) >= harvestQuota)
         {
             cutSelect.addAll(candidates);
             return candidates.size();
         }
         
         PriorityQueue<Patch> canClone = new PriorityQueue<>(candidates);
         PriorityQueue<Patch> lumClone = new PriorityQueue<>(lumberCandidates);
         
         int cut = 0;
         
         double currentValue = 0;
         int cutCon = 0;
         HashMap<List<Integer>, Patch> alreadyPopped = new HashMap<>();
         while(cut < harvestLimit && lumClone.size() > 0)
         {
             Patch conCan = canClone.peek();
             Patch lCan = lumClone.peek();
             if(conCan == null)
             {
                 if(lCan == null) break;
                 
                 lCan = lumClone.remove();
                 if(alreadyPopped.get(lCan.key) == null)
                 {
                     cutSelect.add(lCan);
                     alreadyPopped.put(lCan.key, lCan);
                     currentValue += lCan.lumberProfit;
                     ++cut;
                 }
                 
                 continue;
             }
             if(lCan == null)
             {
                 conCan = canClone.remove();
                 if(alreadyPopped.get(conCan.key) == null)
                 {
                     cutSelect.add(conCan);
                     alreadyPopped.put(conCan.key, conCan);
                     currentValue += lCan.lumberProfit;
                     ++cut;
                 }
                 continue;
             }
             
             if(currentValue >= harvestQuota)
             {
                 conCan = canClone.remove();
                 
                 if(alreadyPopped.get(conCan.key) == null)
                 {
                     cutSelect.add(conCan);
                     alreadyPopped.put(conCan.key, conCan);
                     currentValue += lCan.lumberProfit;
                     ++cut;
                     ++cutCon;
                     continue;
                 }
                 
                 ++cutCon;
                 continue;
             }
             
             if(conCan.lumberProfit >= lCan.lumberProfit)
             {
                 conCan = canClone.remove();
                 if(alreadyPopped.get(conCan.key) == null)
                 {
                     cutSelect.add(conCan);
                     alreadyPopped.put(conCan.key, conCan);
                     currentValue += lCan.lumberProfit;
                     ++cut;
                     ++cutCon;
                     continue;
                 }
                 
                 ++cutCon;
                 continue;
             }
             
             lCan = lumClone.remove();
             if (alreadyPopped.get(lCan.key) == null) {
                 cutSelect.add(lCan);
                 alreadyPopped.put(lCan.key, lCan);
                 currentValue += lCan.lumberProfit;
                 ++cut;
                 continue;
             }
         }
         
        if(canClone.size() > 0)
         {
             Patch conCan = canClone.remove();
             if(alreadyPopped.get(conCan.key) != null)
                 ++cutCon;
         }
         
         return cutCon;
     }
     
     public double CalcProfit(PriorityQueue<Patch> patches)
     {
         if(patches == null)
             return 0;
         double value = 0;
         for(Patch p : patches)
         {
             value += p.lumberProfit;
         }
         
         return value;
     }
 }
