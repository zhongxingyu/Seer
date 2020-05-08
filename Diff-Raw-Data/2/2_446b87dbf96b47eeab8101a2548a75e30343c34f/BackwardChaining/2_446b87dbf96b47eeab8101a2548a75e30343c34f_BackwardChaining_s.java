 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package rules.model;
 
 import java.util.Vector;
 
 /**
  *
  * @author Martynas
  */
 public class BackwardChaining extends AbstractAlgo {
 
     private Vector<Fact> previouslyDeducted;
     protected Vector<Production> productionsInUse;
 
     // recursion level - only used for logging
     private int rLevel = 0;
 
     public BackwardChaining(Vector<Production> productions, Vector<Fact> gdb, Fact target, LogModel logger) {
 
         super(productions, gdb, target, logger);
 
         System.err.println(this);
 
         previouslyDeducted = new Vector<Fact>();
         productionsInUse = new Vector<Production>();
     }
 
     public boolean iterate() {
 		logger.log("Tikslas yra: %s.\n", target);
 
         boolean result = get(target);
 
         finished = true;
         return result;
     }
 
     public boolean get(Fact f) {
 
 		// check if fact is in primary facts {1}
 		if (gdb.contains(f)) {
 			logger.log(" %s yra pradinis faktas.\n", f);
             return true;
         }
 
 		// check if the fact was previously deducted
 		if (previouslyDeducted.contains(f)) {
 			logger.log(" %s yra jau išvestas.\n", f);
 			return true;
 		}
 
 		rLevel++;
 
 		// check if the fact can be derived from the used productions
 		for (Production p: productionsInUse) {
 			if (p.getOutput().contains(f)) {
				trace("Ciklas su %s.\n", p);
 				rLevel--;
 				return false;
 			}
 		}
 
 		boolean factCanBeDerived = false;
 
         for (int i = 0; i < productions.size(); i++) {
             Production currentProduction = productions.elementAt(i);
             if (currentProduction.getOutput().contains(f) && !usedProductions.contains(currentProduction)) { // {2}
 
 				factCanBeDerived = true;
 				boolean allInputsFound = true;
 
 				// mark the production, so it is not used from this point
                 productionsInUse.add(currentProduction);
 
 				// get all needed facts for this production.
 				// Stop after the first production which could not be found.
                 for (int j = 0; j < currentProduction.getInput().size() && allInputsFound; j++) { // {3}
 					trace("Naujas tikslas %s, nes %s galima išvesti iš %s.", currentProduction.getInput().elementAt(j), f, currentProduction);
 
 					boolean result = get(currentProduction.getInput().elementAt(j));
                     allInputsFound = allInputsFound && result;
 
 					if (result) {
 						previouslyDeducted.add(currentProduction.getInput().elementAt(j)); // {4}
 					}
                 }
 
 				if (allInputsFound) {
                     usedProductions.add(currentProduction); // {5}
 					rLevel--;
 					return true;
 				}
 
 				// not all inputs were found for this production,
 				// unmark this production as used
 				productionsInUse.remove(currentProduction);
             }
         }
 
 		if (!factCanBeDerived) {
 			trace("%s nerastas. Aklavietė.\n", f);
 		}
 		rLevel--;
 		return false; // {6}
     }
 
 	public void trace(String s, Object... args) {
         actionNum++;
 		logger.log("\n"+actionNum+". ");
 		for (int i = 0; i < rLevel; i++) {
 			logger.log("    ");
 		}
 		logger.log(String.format(s, args));
 	}
 }
