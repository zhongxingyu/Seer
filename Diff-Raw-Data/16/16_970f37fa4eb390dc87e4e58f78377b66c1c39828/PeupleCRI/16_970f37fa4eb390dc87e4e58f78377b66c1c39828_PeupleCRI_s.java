 /**
  * 
  */
 package com.utbm.smallWorld.peuples;
 
 import java.util.Iterator;
 
 import com.utbm.smallWorld.Peuple;
 import com.utbm.smallWorld.Territoire;
 import com.utbm.smallWorld.elements.SalleInfo;
 
 /**
  * @author Administrateur
  *
  */
 public class PeupleCRI extends Peuple {
 	public PeupleCRI() {
 		nom = "CRI";
 		description = "";
 		nbUniteDepart = 15;
 		nbUniteMax = 20;
 	}
 	
 	
 	public double bonusAttaque(Territoire from, Territoire to) {
 		double bonus = 0.0;
		
		if (from.has(SalleInfo.class)) {
 			Iterator<Territoire> it = territoiresOccupes.iterator();
 			
 			while (it.hasNext()) {
 				if (it.next().has(SalleInfo.class)) {
 					bonus++;
 				}
 			}
 		}
 		
 		return bonus;
 	}
 }
