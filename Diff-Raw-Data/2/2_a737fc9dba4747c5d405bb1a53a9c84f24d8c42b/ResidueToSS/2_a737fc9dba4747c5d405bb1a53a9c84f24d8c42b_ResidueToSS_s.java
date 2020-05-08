 /**
  * 
  * this class will take an array of residues, some of which are missing in the dssp file or the pdb file. 
  * Filter out loops that are greater than 12 residues long w/ loopCounter
  * Discard loops with missing residues
  * 
  * */ 
 
 
 import java.util.*;
 import java.io.*;
 
 public class ResidueToSS {
 
     public static ArrayList<SecondaryStructure> ssArray;
 	
     public ResidueToSS(ArrayList<Residue> resArray, ArrayList<Residue> pmoiArray) {
     	CalcGeo f5 = new CalcGeo(pmoiArray);
    	ArrayList<Geometries> = f5.calculate(pmoiArray);
 	ArrayList<SecondaryStructure> ssArray = resToSS(resArray);
     	// pass geometries to SSToSmotif
     	//ArrayList<SecondaryStructure> ssList, ArrayList<Geometry> geometries
     	SSToSmotif f6 = new SSToSmotif(ssArray, geometries);
     }
     
     public ArrayList<SecondaryStructure> resToSS(ArrayList<Residue> resArray) {
     	
     	//holds residues in each respect ss
         ArrayList<Residue> currentInSS = new ArrayList<Residue>();
         // array to hold ss's
         //ArrayList<SecondaryStructure> ssArray = new ArrayList<SecondaryStructure>();
         
         Residue oldRes = resArray.get(0);
 	Residue currentRes;
         currentInSS.add(oldRes);
         boolean turn = false;
         String nextNotExist= "";
         String ss;
 	int loopCounter = 0;
 
         if(oldRes.getSS().equals("T")) {
             turn = true;
         }
         
         loopCounter = 0;
         
         for (int i=1; i<resArray.size(); ++i) {
             currentRes = resArray.get(i);
             
             //the old and current res are in the same ss, and current res exists
             if(currentRes.getSS().equals(oldRes.getSS()) && currentRes.exists()) {
                 ss = currentRes.getSS();
                 
                 // if you are supposed to add this ss you can.
                 if(!(nextNotExist.equals(ss))) { 
                     if (turn) { ++loopCounter; }
                     if (turn && loopCounter == 13) { 
                         currentInSS.clear();
                         ssArray.add(new SecondaryStructure(currentRes.getSS(), false));
                         nextNotExist = currentRes.getSS();
                     }
                     currentInSS.add(currentRes);
                     nextNotExist = "";
                 }
             }
             
             //the old and current res are in different ss's
             else if(!currentRes.getSS().equals(oldRes.getSS())) {
                 //add currentInSS to the ssArray
                 // turn behavior
                 ss = currentRes.getSS();
                 if(ss.equals("T")) {
                     turn = true;
                     loopCounter = 1;
                 }
                 else {
                     turn = false;
                     loopCounter = 0;
                 }
                 
                 ////////////////////////////////////////////
                 
                 //add currentInSS to ssArray
                 ssArray.add(parseSS(currentInSS));
                 currentInSS.clear();
                 
                 // if it's missing don't let anything happen
                 if(!currentRes.exists() && turn) {
                     ssArray.add(new SecondaryStructure(ss, false));
                     nextNotExist = ss;
                 }
                 else {
                     currentInSS.add(currentRes);
                     nextNotExist = "";
                 }
             }
             // they are in the same ss and the current res is missing
             else if(currentRes.getSS().equals(oldRes.getSS())){
                 ss = currentRes.getSS();
                 
                 if(ss.equals("T")) {
                     currentInSS.clear();
                     ssArray.add(new SecondaryStructure(currentRes.getSS(),false)); 
                     nextNotExist = ss;
                 }
                 else {
                     currentInSS.add(currentRes);
                 }
             } // end of same ss current missing
         } // end of for
     } // end of method
     
     // merge resList into a secondary structure with all necessary information
     public SecondaryStructure parseSS(ArrayList<Residue> resList) {
         
         //String ss, int length, ArrayList<Residue> resArray
         return new SecondaryStructure(resList.get(0).getSS(), resList.size() - 1, resList);
     }
 }
