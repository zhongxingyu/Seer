 package edu.mit.wi.haploview;
 
 import java.io.*;
 import java.lang.*;
 import java.util.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.*;
 
 class HaploData{
     final int SFS = 0;
     final int MJD = 1;
     Vector chromosomes, blocks;
     int missingLimit = 5;
     Vector markerInfo = new Vector();
     String[][] dPrimeTable;
    public boolean finished = false;
     private int numCompleted, toBeCompleted;
     private double[] numBadGenotypes;
     private double[] percentBadGenotypes;
     private double[] multidprimeArray;
 
     public HaploData(File infile) throws IOException{
 	//create the data object and prepare the input
 	chromosomes = prepareGenotypeInput(infile);	
 	toBeCompleted = (((Chromosome)chromosomes.firstElement()).size()-1)*((Chromosome)chromosomes.firstElement()).size()/2;
     }
 
     public void doMonitoredComputation(){
 	dPrimeTable = generateDPrimeTable(chromosomes);
 	blocks = guessBlocks(dPrimeTable, 0);
	finished = true;
     }
 
     Haplotype[][] generateHaplotypes(Vector blocks, int hapthresh) throws IOException{
 	Haplotype[][] results = new Haplotype[blocks.size()][];
 	String raw = new String();
 	String currentLine;
 
 	for (int k = 0; k < blocks.size(); k++){
 	    int[] theBlock = (int[])blocks.elementAt(k);
 	    int[] hetcount = new int[theBlock.length];
 	    int[][] loc = new int[theBlock.length][5];
 	    int[][] convert = new int[theBlock.length][5];
 	    int[][] unconvert = new int[theBlock.length][5];
 	    int totalHaps = 0;
 	    
 	    //parse genotypes for unresolved heterozygotes
 	    for (int i = 0; i < chromosomes.size(); i++){
 		Chromosome thisChrom = (Chromosome)chromosomes.elementAt(i);
 		for (int j = 0; j < theBlock.length; j++){
 		    String theGeno = (String)thisChrom.elementAt(theBlock[j]);
 		    if (theGeno.equals("h")){
 			hetcount[j]++;
 		    } else {
 			loc[j][Integer.parseInt(theGeno)]++;
 		    }
 		}
 		totalHaps ++;
 	    }
 
 	    for (int j = 0; j < theBlock.length; j++){
 		int a = 1;
 		for (int m = 1; m <= 4; m++){
 		    if (loc[j][m] > 0){
 			convert[j][m]=a;
 			unconvert[j][a]=m;
 			loc[j][m]+=(hetcount[j]/2);
 			a++;
 		    } else {
 			convert[j][m] = 0;
 			unconvert[j][a] = 8;
 		    }
 		}
 	    }
 
 	    String hapstr = "";
 	    Vector inputHaploVector = new Vector();
 	    for (int i = 0; i < chromosomes.size(); i++){
 		Chromosome thisChrom = (Chromosome)chromosomes.elementAt(i);
 		Chromosome nextChrom = (Chromosome)chromosomes.elementAt(++i);
 		int missing=0;
 		int dhet=0;
 		for (int j = 0; j < theBlock.length; j++){
 		    String theGeno = (String)thisChrom.elementAt(theBlock[j]);
 		    String nextGeno = (String)nextChrom.elementAt(theBlock[j]);
 		    if(theGeno.equals("0") || nextGeno.equals("0")) missing++;
 		}
 
 		if (! (missing > theBlock.length/2 || missing > missingLimit)){
 		    for (int j = 0; j < theBlock.length; j++){
 			String theGeno = (String)thisChrom.elementAt(theBlock[j]);
 			if (theGeno.equals("h")){
 			    hapstr = hapstr + "h";
 			} else {
 			    hapstr = hapstr + convert[j][Integer.parseInt(theGeno)];
 			}
 		    }
 		    inputHaploVector.add(hapstr);
 		    hapstr = "";
 		    for (int j = 0; j < theBlock.length; j++){
 			String nextGeno = (String)nextChrom.elementAt(theBlock[j]);
 			if (nextGeno.equals("h")){
 			    hapstr = hapstr + "h";
 			}else{
 			    hapstr = hapstr + convert[j][Integer.parseInt(nextGeno)];
 			}
 		    }
 		    inputHaploVector.add(hapstr);
 		    hapstr = "";
 		}
 	    }
 	    String[] input_haplos = (String[])inputHaploVector.toArray(new String[0]);
 	    
 	    //break up large blocks if needed
 	    int[] block_size;
 	    if (theBlock.length < 11){
 		block_size = new int[1];
 		block_size[0] = theBlock.length;
 	    } else {
 		int ones = theBlock.length%10;
 		int tens = (theBlock.length - ones)/10;
 		if (ones == 0){
 		    block_size = new int[tens];
 		    for (int i = 0; i < tens; i++){
 			block_size[i]=10;
 		    }
 		} else {
 		    block_size = new int[tens+1];
 		    for (int i = 0; i < tens-2; i++){
 			block_size[i]=10;
 		    }
 		    block_size[tens-1] = (10+ones)/2;
 		    block_size[tens] = 10+ones-block_size[tens-1];
 		}
 	    }
 
 	    String EMreturn = runEM(input_haplos.length, theBlock.length, input_haplos, block_size.length, block_size);
 	    
 	    StringTokenizer st = new StringTokenizer(EMreturn);
 	    int p = 0;
 	    Haplotype[] tempArray = new Haplotype[st.countTokens()/2];
 	    while(st.hasMoreTokens()){
 		String aString = st.nextToken();
 		int[] genos = new int[aString.length()];
 		for (int j = 0; j < aString.length(); j++){
 		    //System.out.println(j + " " + aString.length() + " " + k);
 		    genos[j] = unconvert[j][Integer.parseInt(aString.substring(j, j+1))];
 		}
 		double tempPerc = Double.parseDouble(st.nextToken());
 		if (tempPerc*100 > hapthresh){
 		    tempArray[p] = new Haplotype(genos, tempPerc, theBlock);
 		    p++;
 		}
 	    }
 	    //make the results array only large enough to hold haps
 	    //which pass threshold above
 	    results[k] = new Haplotype[p];
 	    for (int z = 0; z < p; z++){
 		results[k][z] = tempArray[z];
 	    }
 	}
 	return results;
     }
 
     double[] getMultiDprime(){
 	return multidprimeArray;
     }
 
     Haplotype[][] generateCrossovers(Haplotype[][] haplos) throws IOException{
 	Vector crossBlock = new Vector();
 	double CROSSOVER_THRESHOLD = 0.01;   //to what percentage do we want to consider crossings?
 	
 	//seed first block with ordering numbers
 	for (int u = 0; u < haplos[0].length; u++){
 	    haplos[0][u].setListOrder(u);
 	}
 
 	multidprimeArray = new double[haplos.length];
 	//get "tag" SNPS if there is only one block:
 	if (haplos.length==1){
 	    Vector theBestSubset = getBestSubset(haplos[0]);
 	    for (int i = 0; i < theBestSubset.size(); i++){
 		haplos[0][0].addTag(((Integer)theBestSubset.elementAt(i)).intValue());
 	    }
 	}
 	for (int gap = 0; gap < haplos.length - 1; gap++){         //compute crossovers for each inter-block gap
 	    Vector preGapSubset = getBestSubset(haplos[gap]);
 	    Vector postGapSubset = getBestSubset(haplos[gap+1]);    
 	    int[] preMarkerID = haplos[gap][0].getMarkers();       //index haplos to markers in whole dataset
 	    int[] postMarkerID = haplos[gap+1][0].getMarkers();
 
 	    crossBlock.clear();                 //make a "block" of the markers which id the pre- and post- gap haps
 	    for (int i = 0; i < preGapSubset.size(); i++){
 		crossBlock.add(new Integer(preMarkerID[((Integer)preGapSubset.elementAt(i)).intValue()]));
 		//mark tags
 		haplos[gap][0].addTag(((Integer)preGapSubset.elementAt(i)).intValue());
 	    }
 	    for (int i = 0; i < postGapSubset.size(); i++){
 		crossBlock.add(new Integer(postMarkerID[((Integer)postGapSubset.elementAt(i)).intValue()]));
 		//mark tags
 		haplos[gap+1][0].addTag(((Integer)postGapSubset.elementAt(i)).intValue());
 	    }
 
 	    Vector inputVector = new Vector();
 	    int[] intArray = new int[crossBlock.size()];
 	    for (int i = 0; i < crossBlock.size(); i++){      //input format for hap generating routine
 		intArray[i] = ((Integer)crossBlock.elementAt(i)).intValue();
 	    }
 	    inputVector.add(intArray);
 
 	    Haplotype[] crossHaplos = generateHaplotypes(inputVector, 1)[0];  //get haplos of gap
 	    double[][] multilocusTable = new double[haplos[gap].length][];
 	    double[] rowSum = new double[haplos[gap].length];
 	    double[] colSum = new double[haplos[gap+1].length];
 	    double multilocusTotal = 0;
 
 	    for (int i = 0; i < haplos[gap].length; i++){
 		double[] crossPercentages = new double[haplos[gap+1].length];
 		String firstHapCode = new String();
 		for (int j = 0; j < preGapSubset.size(); j++){   //make a string out of uniquely identifying genotypes for this hap
 		    firstHapCode += haplos[gap][i].getGeno()[((Integer)preGapSubset.elementAt(j)).intValue()];
 		}
 		for (int gapHaplo = 0; gapHaplo < crossHaplos.length; gapHaplo++){  //look at each crossover hap
 		    if (crossHaplos[gapHaplo].getPercentage() > CROSSOVER_THRESHOLD){
 			String gapBeginHapCode = new String();
 			for (int j = 0; j < preGapSubset.size(); j++){     //make a string as above
 			    gapBeginHapCode += crossHaplos[gapHaplo].getGeno()[j];
 			}
 			if (gapBeginHapCode.equals(firstHapCode)){    //if this crossover hap corresponds to this pregap hap
 			    String gapEndHapCode = new String();
 			    for (int j = preGapSubset.size(); j < crossHaplos[gapHaplo].getGeno().length; j++){
 				gapEndHapCode += crossHaplos[gapHaplo].getGeno()[j];
 			    }
 			    for (int j = 0; j < haplos[gap+1].length; j++){
 				String endHapCode = new String();
 				for (int k = 0; k < postGapSubset.size(); k++){
 				    endHapCode += haplos[gap+1][j].getGeno()[((Integer)postGapSubset.elementAt(k)).intValue()];
 				}
 				if (gapEndHapCode.equals(endHapCode)){
 				    crossPercentages[j] = crossHaplos[gapHaplo].getPercentage();
 				}
 			    }
 			}
 		    }
 		}
 		//thought i needed to fix these percentages, but the raw values are just as good.
 		/**		double percentageSum = 0;
 		double[] fixedCross = new double[crossPercentages.length];
 		for (int y = 0; y < crossPercentages.length; y++){
 		    percentageSum += crossPercentages[y];
 		}
 		for (int y = 0; y < crossPercentages.length; y++){
 		    fixedCross[y] = crossPercentages[y]/percentageSum;
 		    }**/
 		haplos[gap][i].addCrossovers(crossPercentages);
 		multilocusTable[i] = crossPercentages;
 	    }
 
 	    //sort based on "straight line" crossings
 	    int hilimit;
 	    int lolimit;
 	    if (haplos[gap+1].length > haplos[gap].length) {
 		hilimit = haplos[gap+1].length;
 		lolimit = haplos[gap].length;
 	    }else{
 		hilimit = haplos[gap].length;
 		lolimit = haplos[gap+1].length;
 	    }
 	    boolean[] unavailable = new boolean[hilimit];
 	    int[] prevBlockLocs = new int[haplos[gap].length];
 	    for (int q = 0; q < prevBlockLocs.length; q++){
 		prevBlockLocs[haplos[gap][q].getListOrder()] = q;
 	    }
 
 	    for (int u = 0; u < haplos[gap+1].length; u++){
 		double currentBestVal = 0;
 		int currentBestLoc = -1;
 		for (int v = 0; v < lolimit; v++){
 		    if (!(unavailable[v])){
 			if (haplos[gap][prevBlockLocs[v]].getCrossover(u) >= currentBestVal) {
 			    currentBestLoc = haplos[gap][prevBlockLocs[v]].getListOrder();
 			    currentBestVal = haplos[gap][prevBlockLocs[v]].getCrossover(u);
 			}
 		    }
 		}
 		//it didn't get lined up with any of the previous block's markers
 		//put it at the end of the list
 		if (currentBestLoc == -1){
 		    for (int v = 0; v < unavailable.length; v++){
 			if (!(unavailable[v])){
 			    currentBestLoc = v;
 			    break;
 			}
 		    }
 		}
 
 		haplos[gap+1][u].setListOrder(currentBestLoc);
 		unavailable[currentBestLoc] = true;
 	    }
 	    
 	    //compute multilocus D'
 	    for (int i = 0; i < rowSum.length; i++){
 		for (int j = 0; j < colSum.length; j++){
 		    rowSum[i] += multilocusTable[i][j];
 		    colSum[j] += multilocusTable[i][j];
 		    multilocusTotal += multilocusTable[i][j];
 		    if (rowSum[i] == 0) rowSum[i] = 0.0001;
 		    if (colSum[j] == 0) colSum[j] = 0.0001;
 		}
 	    }
 	    double multidprime = 0;
 	    for (int i = 0; i < rowSum.length; i++){
 		for (int j = 0; j < colSum.length; j++){
 		    double num = (multilocusTable[i][j]/multilocusTotal) - (rowSum[i]/multilocusTotal)*(colSum[j]/multilocusTotal);
 		    double denom;
 		    if (num < 0){
 			double denom1 = (rowSum[i]/multilocusTotal)*(colSum[j]/multilocusTotal);
 			double denom2 = (1.0 - (rowSum[i]/multilocusTotal))*(1.0 - (colSum[j]/multilocusTotal));
 			if (denom1 < denom2) {
 			    denom = denom1;
 			}else{
 			    denom = denom2;
 			}
 		    }else{
 			double denom1 = (rowSum[i]/multilocusTotal)*(1.0 -(colSum[j]/multilocusTotal));
 			double denom2 = (1.0 - (rowSum[i]/multilocusTotal))*(colSum[j]/multilocusTotal);
 			if (denom1 < denom2){
 			    denom = denom1;
 			}else{
 			    denom = denom2;
 			}
 		    }
 		    multidprime += (rowSum[i]/multilocusTotal)*(colSum[j]/multilocusTotal)*Math.abs(num/denom);
 		}		
 	    }
 	    multidprimeArray[gap] = multidprime;
 	}
 	return haplos;
     }
 
     Vector getBestSubset(Haplotype[] thisBlock){    //from a block of haps, find marker subset which uniquely id's all haps
 	Vector bestSubset = new Vector();
 	//first make an array with markers ranked by genotyping success rate
 	Vector genoSuccessRank = new Vector();
 	Vector genoNumberRank = new Vector();
 	int[] myMarkers = thisBlock[0].getMarkers();
 	genoSuccessRank.add(new Double(percentBadGenotypes[myMarkers[0]]));
 	genoNumberRank.add(new Integer(0));
 	for (int i = 1; i < myMarkers.length; i++){
 	    boolean inserted = false;
 	    for (int j = 0; j < genoSuccessRank.size(); j++){
 		if (percentBadGenotypes[myMarkers[i]] < ((Double)(genoSuccessRank.elementAt(j))).doubleValue()){
 		    genoSuccessRank.insertElementAt(new Double(percentBadGenotypes[myMarkers[i]]), j);
 		    genoNumberRank.insertElementAt(new Integer(i), j);
 		    inserted = true;
 		    break;
 		}
 	    }
 	    if (!(inserted)) {
 		genoNumberRank.add(new Integer(i));
 		genoSuccessRank.add(new Double(percentBadGenotypes[myMarkers[i]]));
 	    }
 	}
 
 	for (int i = 0; i < thisBlock.length-1; i++){
 	    int[] firstHap = thisBlock[i].getGeno();
 	    for (int j = i+1; j < thisBlock.length; j++){
 		int[] secondHap = thisBlock[j].getGeno();
 		for (int y = 0; y < firstHap.length; y++){
 		    int x = ((Integer)(genoNumberRank.elementAt(y))).intValue();
 		    if (firstHap[x] != secondHap[x]){
 			if (!(bestSubset.contains(new Integer(x)))){
 			    bestSubset.add(new Integer(x));
 			    break;
 			} else {
 			    break;
 			}
 		    }
 		}    
 	    }
 	}
 	return bestSubset;
     }	
 	    
     int prepareMarkerInput(File infile) throws IOException{
 	//this method is called to gather data about the markers used.
 	//It is assumed that the input file is two columns, the first being
 	//the name and the second the absolute position
 	String currentLine;
 	Vector markers = new Vector();
 	
 	//read the file:
 	BufferedReader in = new BufferedReader(new FileReader(infile));
 	// a vector of SNP's is created and returned.
 	while ((currentLine = in.readLine()) != null){
 	    StringTokenizer st = new StringTokenizer(currentLine);
 	    markers.add(new SNP(st.nextToken(), Long.parseLong(st.nextToken()), infile.getName()));
 	}
 	if (markerInfo.size() == markers.size()){
 	    markerInfo = markers;
 	    return 1;
 	}else{
 	    return -1;
 	}
     }
     
     Vector prepareGenotypeInput(File infile) throws IOException{
 	//this method is called to suck in data from a file (its only argument)
 	//of genotypes and return a vector of Chromosome objects.
 	String currentLine;
 	Vector chroms = new Vector();
 	Vector genos = new Vector();
 	String ped, indiv; 
     
 	//read the file:
 	BufferedReader in = new BufferedReader(new FileReader(infile));
 	boolean firstTime = true;
 	while ((currentLine = in.readLine()) != null){
 	    //each line is expected to be of the format:
 	    //ped   indiv   geno   geno   geno   geno...
 	    StringTokenizer st = new StringTokenizer(currentLine);
 	    //first two tokens are expected to be ped, indiv
 	    ped = st.nextToken();
 	    indiv = st.nextToken();
 	    //all other tokens are loaded into a vector (they should all be genotypes)
 	    genos.clear();
 	    //the first time through, count number of genotypes for marker quality statistics
 	    if (firstTime){
 		numBadGenotypes = new double[st.countTokens()];
 		percentBadGenotypes = new double[st.countTokens()];
 	    }
 	    int q = 0;
 	    while (st.hasMoreTokens()){
 		String thisGenotype = (String)st.nextElement();
 		genos.add(thisGenotype);
 		if (thisGenotype.equals("0")) numBadGenotypes[q] ++;
 		q++;
 	    }
 	    //a Chromosome is created and added to a vector of chromosomes.
 	    //this is what is evetually returned.
 	    chroms.add(new Chromosome(ped, indiv, (Vector) genos.clone(), infile.getName()));
 	    firstTime = false;
 	}
 	//generate marker information in case none is subsequently available
 	//also convert sums of bad genotypes to percentages for each marker
 	double numChroms = chroms.size();
 	for (int i = 0; i < genos.size(); i++){
 	    markerInfo.add(new SNP(String.valueOf(i), (i*4000)));
 	    percentBadGenotypes[i] = numBadGenotypes[i]/numChroms;
 	}	
 	return chroms;
     }
 
     Vector guessBlocks(String[][] dPrime, int method){
 	Vector returnVec = new Vector();
 	switch(method){
 	case 0: returnVec = new FindBlocks(dPrime).doSFS(); break;
 	case 1: returnVec = new FindBlocks(dPrime).doMJD(); break;
 	}
 	return returnVec;
     }
 
     
     static {
         System.loadLibrary("haplos");
     }
     
     private native String callComputeDPrime(int aa, int ab, int ba, int bb, int doublehet);
     private native String runEM(int num_haplos, int num_loci, String[] input_haplos, int num_blocks, int[] block_size);
     
     public int getComplete(){
 	return numCompleted;
     }
 
     public int getToBeCompleted(){
 	return toBeCompleted;
     }
     
     String[][] generateDPrimeTable(final Vector chromosomes){
 	numCompleted = 0;
 
 	//calculating D prime requires the number of each possible 2 marker
 	//haplotype in the dataset
 	String [][] dPrimeTable = new String[((Chromosome) chromosomes.firstElement()).size()][((Chromosome) chromosomes.firstElement()).size()];
 	int doublehet;
 	int[][] twoMarkerHaplos = new int[3][3];
 	
 	//loop through all marker pairs
 	for (int pos2 = 1; pos2 < dPrimeTable.length; pos2++){
 	    //clear the array
 	    for (int pos1 = 0; pos1 < pos2; pos1++){
 		numCompleted ++;
 		for (int i = 0; i < twoMarkerHaplos.length; i++){
 		    for (int j = 0; j < twoMarkerHaplos[i].length; j++){
 			twoMarkerHaplos[i][j] = 0;
 		    }
 		}
 		doublehet = 0;
 		//get the alleles for the markers
 		int m1a1 = 0; int m1a2 = 0; int m2a1 = 0; int m2a2 = 0; int m1H = 0; int m2H = 0;
 			    
 		for (int i = 0; i < chromosomes.size(); i++){
 		    String a1 = ((Chromosome) chromosomes.elementAt(i)).elementAt(pos1).toString();
 		    String a2 = ((Chromosome) chromosomes.elementAt(i)).elementAt(pos2).toString();
 		    if (m1a1 > 0){
 			if (m1a2 == 0 && !(a1.equals("h")) && !(a1.equals("0")) && Integer.parseInt(a1) != m1a1) m1a2 = Integer.parseInt(a1);
 		    } else if (!(a1.equals("h")) && !(a1.equals("0"))) m1a1=Integer.parseInt(a1);
 		    
 		    if (m2a1 > 0){
 			if (m2a2 == 0 && !(a2.equals("h")) && !(a2.equals("0")) && Integer.parseInt(a2) != m2a1) m2a2 = Integer.parseInt(a2);
 		    } else if (!(a2.equals("h")) && !(a2.equals("0"))) m2a1=Integer.parseInt(a2);
 
 		    if (a1.equals("h")) m1H++;
 		    if (a2.equals("h")) m2H++;
 		}
 
 		//check for non-polymorphic markers
 		if (m1a2==0){
 		    if (m1H==0){
 			dPrimeTable[pos1][pos2] = "0\t0\t0\t0\t0";
 			continue;
 		    } else {
 			if (m1a1 == 1){ m1a2=2; }
 			else { m1a2 = 1; }
 		    }
 		}
 		if (m2a2==0){
 		    if (m2H==0){
 			dPrimeTable[pos1][pos2] = "0\t0\t0\t0\t0";
 			continue;
 		    } else {
 			if (m2a1 == 1){ m2a2=2; }
 			else { m2a2 = 1; }
 		    }
 		}
 		
 		int[] marker1num = new int[5]; int[] marker2num = new int[5]; 
 		
 		marker1num[0]=0;
 		marker1num[m1a1]=1;
 		marker1num[m1a2]=2;
 		marker2num[0]=0;
 		marker2num[m2a1]=1;
 		marker2num[m2a2]=2;
 		//iterate through all chromosomes in dataset	
 		for (int i = 0; i < chromosomes.size(); i++){
 				//assign alleles for each of a pair of chromosomes at a marker to four variables
 		    String a1 = ((Chromosome) chromosomes.elementAt(i)).elementAt(pos1).toString();
 		    String a2 = ((Chromosome) chromosomes.elementAt(i)).elementAt(pos2).toString();
 		    String b1 = ((Chromosome) chromosomes.elementAt(++i)).elementAt(pos1).toString();
 		    String b2 = ((Chromosome) chromosomes.elementAt(i)).elementAt(pos2).toString();
 		    if (a1.equals("0") || a2.equals("0") || b1.equals("0") || b2.equals("0")){
 			//skip missing data
 		    } else if ((a1.equals("h") && a2.equals("h")) || (a1.equals("h") && !(a2.equals(b2))) || (a2.equals("h") && !(a1.equals(b1)))) doublehet++;
 				//find doublehets and resolved haplotypes
 		    else if (a1.equals("h")){
 			twoMarkerHaplos[1][marker2num[Integer.parseInt(a2)]]++;
 			twoMarkerHaplos[2][marker2num[Integer.parseInt(a2)]]++;
 		    } else if (a2.equals("h")){
 			twoMarkerHaplos[marker1num[Integer.parseInt(a1)]][1]++;
 			twoMarkerHaplos[marker1num[Integer.parseInt(a1)]][2]++;
 		    } else {
 			twoMarkerHaplos[marker1num[Integer.parseInt(a1)]][marker2num[Integer.parseInt(a2)]]++;
 			twoMarkerHaplos[marker1num[Integer.parseInt(b1)]][marker2num[Integer.parseInt(b2)]]++;
 		    }
 		    
 		}
 		//another monomorphic marker check
 		int r1, r2, c1, c2;
 		r1 = twoMarkerHaplos[1][1] + twoMarkerHaplos[1][2];
 		r2 = twoMarkerHaplos[2][1] + twoMarkerHaplos[2][2];
 		c1 = twoMarkerHaplos[1][1] + twoMarkerHaplos[2][1];
 		c2 = twoMarkerHaplos[1][2] + twoMarkerHaplos[2][2];
 		if ( (r1==0 || r2==0 || c1==0 || c2==0) && doublehet == 0){
 		    dPrimeTable[pos1][pos2] = "0\t0\t0\t0\t0";
 		    continue;
 		}
 		
 		//compute D Prime for this pair of markers.
 		//return is a tab delimited string of d', lod, r^2, CI(low), CI(high)
 		dPrimeTable[pos1][pos2] = callComputeDPrime(twoMarkerHaplos[1][1], twoMarkerHaplos[1][2], twoMarkerHaplos[2][1], twoMarkerHaplos[2][2], doublehet);
 	    }
 	}
 	return dPrimeTable;
     }
 }
