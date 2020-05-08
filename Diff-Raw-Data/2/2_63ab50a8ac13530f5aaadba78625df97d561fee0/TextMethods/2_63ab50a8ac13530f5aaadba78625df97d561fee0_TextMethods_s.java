 package edu.mit.wi.haploview;
 
 import java.io.*;
 import java.text.*;
 import java.util.*;
 
 class TextMethods {
 
     public void saveHapsToText(Haplotype[][] finishedHaplos, File saveHapsFile) throws IOException{
 
 	NumberFormat nf = NumberFormat.getInstance();
 	nf.setMinimumFractionDigits(3);
 	nf.setMaximumFractionDigits(3);
 	
 	//open file for saving haps text
 	FileWriter saveHapsWriter = new FileWriter(saveHapsFile);
 	
 	int[][]lookupPos = new int[finishedHaplos.length][];
 	for (int p = 0; p < lookupPos.length; p++){
 	    lookupPos[p] = new int[finishedHaplos[p].length];
 	    for (int q = 0; q < lookupPos[p].length; q++){
 		lookupPos[p][finishedHaplos[p][q].getListOrder()] = q;
 		//System.out.println(p + " " + q + " " + finishedHaplos[p][q].getListOrder());
 	    }
 	}
 
 	//go through each block and print haplos
 	for (int i = 0; i < finishedHaplos.length; i++){
 	    //write block header
 	    saveHapsWriter.write("BLOCK " + (i+1) + ".  MARKERS:");
 	    int[] markerNums = finishedHaplos[i][0].getMarkers();
	    for (int j = 0; j < finishedHaplos[i].length; j++){
 		saveHapsWriter.write(" " + (markerNums[j]+1));
 	    }
 	    saveHapsWriter.write("\n");
 	    //write haps and crossover percentages
 	    for (int j = 0; j < finishedHaplos[i].length; j++){
 		int curHapNum = lookupPos[i][j];
 		String theHap = new String();
 		int[] theGeno = finishedHaplos[i][curHapNum].getGeno();
 		for (int k = 0; k < theGeno.length; k++){
 		    theHap += theGeno[k];
 		}
 		saveHapsWriter.write(theHap + " (" + nf.format(finishedHaplos[i][curHapNum].getPercentage()) + ")");
 		if (i < finishedHaplos.length-1){
 		    saveHapsWriter.write("\t|");
 		    for (int crossCount = 0; crossCount < finishedHaplos[i+1].length; crossCount++){
 			if (crossCount != 0) saveHapsWriter.write("\t");
 			saveHapsWriter.write(nf.format(finishedHaplos[i][curHapNum].getCrossover(crossCount)));
 		    }
 		    saveHapsWriter.write("|");
 		}
 		saveHapsWriter.write("\n");
 	    }
 	    saveHapsWriter.write("\n");
 	}
 	saveHapsWriter.close();
     }
 
     public void saveDprimeToText(String[][] dPrimeTable, File dumpDprimeFile, boolean info, Vector markerinfo) throws IOException{
 	FileWriter saveDprimeWriter = new FileWriter(dumpDprimeFile);
 	
 
 	if (info){
 	    saveDprimeWriter.write("L1\tL2\tD'\tLOD\tr^2\tCIlow\tCIhi\tDist\n");
 	    long dist;
 	    
 	    for (int i = 0; i < dPrimeTable.length; i++){
 		for (int j = 0; j < dPrimeTable[i].length; j++){
 		    //many "slots" in table aren't filled in because it is a 1/2 matrix
 		    if (i < j){
 			dist = ((SNP)markerinfo.elementAt(j)).getPosition() - ((SNP)markerinfo.elementAt(i)).getPosition();
 			saveDprimeWriter.write(i + "\t" + j + "\t" + dPrimeTable[i][j] + "\t" + dist + "\n");
 		    }
 		}
 	    }
 
 	}else{
 	    saveDprimeWriter.write("L1\tL2\tD'\tLOD\tr^2\tCIlow\tCIhi\n");
 
 	    for (int i = 0; i < dPrimeTable.length; i++){
 		for (int j = 0; j < dPrimeTable[i].length; j++){
 		    //many "slots" in table aren't filled in because it is a 1/2 matrix
 		    if (i < j){
 			saveDprimeWriter.write(i + "\t" + j + "\t" + dPrimeTable[i][j] + "\n");
 		    }
 		}
 	    }
 	}
 
 	saveDprimeWriter.close();
     }
 }
 
