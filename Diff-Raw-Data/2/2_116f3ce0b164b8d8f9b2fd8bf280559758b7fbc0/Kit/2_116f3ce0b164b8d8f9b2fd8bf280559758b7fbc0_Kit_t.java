 package agent.data;
 
 import java.util.ArrayList;
 
 import DeviceGraphics.KitGraphics;
 import Utils.Constants;
 import factory.KitConfig;
 import factory.PartType;
 
 public class Kit {
 
     public KitGraphics kitGraphics;
 
     public String kitID;
 
     public KitConfig partsExpected;
 
     public ArrayList<Part> parts = new ArrayList<Part>();
 
     public Kit() {
 	kitGraphics = new KitGraphics(null);
 	partsExpected = new KitConfig("default", Constants.DEFAULT_PARTTYPES.get(0));
     }
 
     public Kit(KitConfig expected) {
 	kitGraphics = new KitGraphics(null);
 	partsExpected = expected;
     }
 
     public Kit(String kitID) {
 	kitGraphics = new KitGraphics(null);
 	this.kitID = kitID;
     }
 
     public int needPart(Part part) {
 	int count = 0;
 	for (PartType type : partsExpected.getConfig().keySet()) {
	    if (type.equals(part.type)) {
 		count += partsExpected.getConfig().get(type);
 		break;
 	    }
 	}
 	for (Part tempPart : parts) {
 	    if (tempPart.type == part.type) {
 		count--;
 	    }
 	}
 	return count > 0 ? count : 0;
     }
 
     public String PartsStillNeeded() {
 	String temp = "Needs ";
 	for (PartType inputtype : partsExpected.getConfig().keySet()) {
 	    int count = 0;
 	    for (PartType type : partsExpected.getConfig().keySet()) {
 		if (type == inputtype) {
 		    count = partsExpected.getConfig().get(type);
 		    break;
 		}
 	    }
 	    for (Part tempPart : parts) {
 		if (tempPart.type == inputtype) {
 		    count--;
 		}
 	    }
 	    if (count > 0) {
 		temp = temp.concat("" + count + ":" + inputtype + " ");
 	    }
 	}
 	return temp;
     }
 
     /**
      * Changes Parts in kit to match new kitConfig. For use in KitFailingInspection Non-Normative Scenario
      * 
      * @param kitChange
      */
     public void updateParts(KitConfig kitChange) {
 	ArrayList<Part> list = new ArrayList<Part>();
 
 	for (PartType p : kitChange.getConfig().keySet()) {
 	    int numNew = kitChange.getConfig().get(p).intValue();
 	    int numOld = 0;
 	    for (Part heldPart : parts) {
 		if (heldPart.type == p) {
 		    numOld++;
 		}
 	    }
 	    if (numOld > numNew) {
 		for (int i = (numOld - numNew); i > 0; i--) {
 		    for (Part heldPart : parts) {
 			if (heldPart.type == p) {
 			    parts.remove(heldPart);
 			    break;
 			}
 		    }
 		}
 	    }
 	    if (numNew > numOld) {
 		System.out.println(this.kitID
 			+ ": SOMETHING WENT SERIOUSLY WRONG IN KitFAILING INSPECTION NON NORMATIVE SCENARIO");
 	    }
 	}
 
     }
 
     public boolean equals(Kit k) {
 	return k.kitGraphics.toString().equals(this.kitGraphics.toString());
     }
 
 }
