 package agents;
 import agents.*;
 import agents.include.*;
 import agents.interfaces.*;
 import java.util.*;
 
 public class KitStand {
 
     /*** Data Structures **/
 
     private int capacity;
     private int count;
     private int completeKits;
     //public List<Kit> kits;
     public Kit[] kits;
     private InspectionStand inspection;
 
     private boolean inspecting = false;
 
     class InspectionStand {
 
         Kit kit;
         public InspectionStand() { }
 
         synchronized public void placeOnInspection(Kit k)
         {
             kit = k;
         }
 
         synchronized public Kit inspectKit()
         {
             return kit;
         }
 
         synchronized public Kit removeFromInspection()
         {
             Kit k = kit;
             kit = null;
             return k;
         }
     }
     
     /*** Constructor **/
 
     public KitStand()
     {
         capacity = 2;
         completeKits = 0;
         count = 0;
         //kits = new ArrayList<Kit>();
         kits = new Kit[2];
         inspection = new InspectionStand();
     } 
 
     /*** Actions ***/
     synchronized public void insertEmptyKit(Kit emptyKit) {
         kits[count] = emptyKit;
         count++;
     }
 
     synchronized public List<Map<String, Integer>> getPartConfig() {
 
         List<Map<String, Integer>> configs = new ArrayList<Map<String, Integer>>();
         if(kits[0] != null) configs.add(kits[0].config);
         if(kits[1] != null) configs.add(kits[1].config);
         return configs;
     }
 
     // called by PartRobot
     synchronized public Map<Part, Integer> insertPartsIntoKits(List<Part> send) {
 
         Map<Part, Integer> parts = new HashMap<Part,Integer>();
 
         for(Part p: send)
         {
             String type = p.getPartName();
             if(kits[0]!=null && kits[0].config.containsKey(type))
             {
                 int zero_num = kits[0].config.get(type);
                 int zero_count = 0;
                 for(Part zero_p : kits[0].parts)
                 {
                     if(zero_p.getPartName().equals(type)) zero_count++;
                 }
 
                 if(zero_count < zero_num)
                 {
                     kits[0].parts.add(p);
                     parts.put(p,0);
                 }
                 else
                 {
                     kits[1].parts.add(p);
                     parts.put(p,1);
                 }
             }
             else
             {
                 kits[1].parts.add(p);
                 parts.put(p,1);
             }
         }
 
         int complete0 = 1;
        if(kits[0]!=null)
         {
            for (String key : kits[0].config.keySet())
             {
                int num = kits[0].config.get(key);
                int count = 0;
                for(Part p : kits[0].parts)
                {
                    if(p.getPartName().equals(key)) count ++;
                }
                if(count < num)
                {
                    complete0 = 0;
                    break;
                }
            } 	
         }
 
         int complete1 = 1;
         if(kits[1]!=null)
         {
             for (String key : kits[1].config.keySet())
             {
                 int num = kits[1].config.get(key);
                 int count = 0;
                 for(Part p : kits[1].parts)
                 {
                     if(p.getPartName().equals(key)) count ++;
                 }
                 if(count < num)
                 {
                     complete1 = 0;
                     break;
                 }
             }
         }
 
         completeKits = complete0 + complete1;
     
         return parts;
     }
 
     synchronized public int completeKits() { return completeKits; }
 
     // should only do when PartRobot tells we have complete kits!
     synchronized public Kit removeCompleteKit() {
     	System.out.println("KitstandCount: " + count);
         if(count == 0) return null;
 
         Kit data;
         if(kits[0].complete) 
         {
         	System.out.println("am in in 1?");
             data = kits[0];
             kits[0] = null;
         }
         else 
         {
         	System.out.println("am I in 2?");
             data = kits[1];
             kits[1] = null;
         }
 
         count--;
     	System.out.println("Kit: " + data);
         return data;
     }
 
     public void placeInspection(Kit kit)
     {
         // before calling this, have the moving animation
         inspection.placeOnInspection(kit);
         DoPlaceInspectionKit(kit);
     
     }
     public Kit inspectKit()
     {
         return inspection.inspectKit();
     }
 
     public void removeInspection(Kit kit)
     {
         DoRemoveInspectionKit(kit);
         inspection.removeFromInspection();
         // in msg, have remove from moving animation
     }    
 
     public void DoPlaceInspectionKit(Kit kit)
     {
 
     } 
 
     public void DoRemoveInspectionKit(Kit kit)
     {
         
     }
 }
