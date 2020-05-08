 package edu.mit.wi.haploview;
 
 import java.io.*;
 import java.lang.*;
 import java.util.*;
import java.text.*;
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
 
     //stuff for computing d prime
     int AA = 0;
     int AB = 1;
     int BB = 2;
     int BA = 3;
     double TOLERANCE = 0.00000001;
     double LN10 = Math.log(10.0);
     int unknownDH=-1;
     int total_chroms=-1;
     double const_prob=-1.0;
     double[] known = new double[5];
 
     //stuff for em phasing
     int MAXLOCI = 100;
     int MAXLN = 1000;
     double PSEUDOCOUNT = 0.1;
     OBS data[];
     SUPER_OBS superdata[];
     int[] two_n = new int[MAXLOCI];
     double[] prob;
 	
 
 
     
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
 		if (unconvert[j][2] == 0) unconvert[j][2] = 8;
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
 	    if (theBlock.length < 9){
 		block_size = new int[1];
 		block_size[0] = theBlock.length;
 	    } else {
 		//some base-8 arithmetic
 		int ones = theBlock.length%8;
 		int eights = (theBlock.length - ones)/8;
 		if (ones == 0){
 		    block_size = new int[eights];
 		    for (int i = 0; i < eights; i++){
 			block_size[i]=8;
 		    }
 		} else {
 		    block_size = new int[eights+1];
 		    for (int i = 0; i < eights-1; i++){
 			block_size[i]=8;
 		    }
 		    block_size[eights-1] = (8+ones)/2;
 		    block_size[eights] = 8+ones-block_size[eights-1];
 		}
 	    }
 
 
 	    String EMreturn = new String("");
 	    int[] num_haplos_present = new int[1];
 	    Vector haplos_present = new Vector();
 	    Vector haplo_freq = new Vector();
 	    char[][] input_haplos2 = new char[input_haplos.length][];
 	    for (int j = 0; j < input_haplos.length; j++){
 		input_haplos2[j] = input_haplos[j].toCharArray();
 	    }
 
 	    //kirby patch
 	    full_em_breakup(input_haplos.length, theBlock.length, input_haplos2, 4, num_haplos_present, haplos_present, haplo_freq, block_size.length, block_size, 0);
 	    for (int j = 0; j < haplos_present.size(); j++){
 		EMreturn += (String)haplos_present.elementAt(j)+"\t"+(String)haplo_freq.elementAt(j)+"\t";
 	    }
 
 	    //old c version
 	    //String EMreturn = runEM(input_haplos.length, theBlock.length, input_haplos, block_size.length, block_size);
 	    
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
 
     /**
        old c version
 
        static {
        System.loadLibrary("haplos");
        }
 	   
        private native String callComputeDPrime(int aa, int ab, int ba, int bb, int doublehet);
        private native String runEM(int num_haplos, int num_loci, String[] input_haplos, int num_blocks, int[] block_size); 
     **/
 
     public String computeDPrime(int a, int b, int c, int d, int e, double f){ 
 	int i,j,k,count,itmp;
 	int low_i = 0;
 	int high_i = 0;	
 	double[] nAA = new double[1];
 	double[] nBB = new double[1];
 	double[] nAB = new double[1];
 	double[] nBA = new double[1];
 	double[] pAA = new double[1];
 	double[] pBB = new double[1];
 	double[] pAB = new double[1];
 	double[] pBA = new double[1];
 	double loglike, oldloglike, meand, mean2d, sd;
 	double g,h,m,tmp,r;
 	double num, denom1, denom2, denom, dprime, real_dprime;
 	double pA1, pB1, pA2, pB2, loglike1, loglike0, r2;
 	double tmpAA, tmpAB, tmpBA, tmpBB, dpr, tmp2AA, tmp2AB, tmp2BA, tmp2BB;
 	double total_prob, sum_prob;	
 	double lsurface[] = new double[105];
 	
 	/* store arguments in externals and compute allele frequencies */
 
 	known[AA]=(double)a; known[AB]=(double)b; known[BA]=(double)c; known[BB]=(double)d; 
 	unknownDH=e;
 	total_chroms= a+b+c+d+(2*unknownDH);
 	pA1 = (double) (a+b+unknownDH) / (double) total_chroms;
 	pB1 = 1.0-pA1;
 	pA2 = (double) (a+c+unknownDH) / (double) total_chroms;
 	pB2 = 1.0-pA2;
 	const_prob = f;
 	
 	/* set initial conditions */
 	
 	if (const_prob < 0.00) {
 	    pAA[0]=pA1*pA2;
 	    pAB[0]=pA1*pB2;
 	    pBA[0]=pB1*pA2;
 	    pBB[0]=pB1*pB2;
 	} else {	    
 	    pAA[0]=const_prob;
 	    pAB[0]=const_prob;
 	    pBA[0]=const_prob;
 	    pBB[0]=const_prob;;
 
 	    /* so that the first count step will produce an
 	       initial estimate without inferences (this should
 	       be closer and therefore speedier than assuming 
 	       they are all at equal frequency) */
 	    
 	    count_haps(pAA[0],pAB[0],pBA[0],pBB[0],nAA,nAB,nBA,nBB,0);	    
 	    estimate_p(nAA[0],nAB[0],nBA[0],nBB[0],pAA,pAB,pBA,pBB);
 	}
 	
 	/* now we have an initial reasonable guess at p we can
 	   start the EM - let the fun begin */
 	
 	const_prob=0.0;
 	count=1; loglike=-999999999.0;
 	
 	do {	    
 	    oldloglike=loglike;
 	    count_haps(pAA[0],pAB[0],pBA[0],pBB[0],nAA,nAB,nBA,nBB,count);	    
 	    loglike = known[AA]*log10(pAA[0]) + known[AB]*log10(pAB[0]) + known[BA]*log10(pBA[0]) + known[BB]*log10(pBB[0]) + (double)unknownDH*log10(pAA[0]*pBB[0] + pAB[0]*pBA[0]);
 	    if (Math.abs(loglike-oldloglike) < TOLERANCE) break;	    
 	    estimate_p(nAA[0],nAB[0],nBA[0],nBB[0],pAA,pAB,pBA,pBB);
 	    count++;
 	} while(count < 1000);
 	/* in reality I've never seen it need more than 10 or so iterations 
 	   to converge so this is really here just to keep it from running off into eternity */
 	
 	loglike1 = known[AA]*log10(pAA[0]) + known[AB]*log10(pAB[0]) + known[BA]*log10(pBA[0]) + known[BB]*log10(pBB[0]) + (double)unknownDH*log10(pAA[0]*pBB[0] + pAB[0]*pBA[0]);
 	loglike0 = known[AA]*log10(pA1*pA2) + known[AB]*log10(pA1*pB2) + known[BA]*log10(pB1*pA2) + known[BB]*log10(pB1*pB2) + (double)unknownDH*log10(2*pA1*pA2*pB1*pB2);
 	
 	num = pAA[0]*pBB[0] - pAB[0]*pBA[0];
 	
 	if (num < 0) { 
 	    /* flip matrix so we get the positive D' */
 	    /* flip AA with AB and BA with BB */
 	    tmp=pAA[0]; pAA[0]=pAB[0]; pAB[0]=tmp;
 	    tmp=pBB[0]; pBB[0]=pBA[0]; pBA[0]=tmp; 
 	    /* flip frequency of second allele */
 	    tmp=pA2; pA2=pB2; pB2=tmp;
 	    /* flip counts in the same fashion as p's */
 	    tmp=nAA[0]; nAA[0]=nAB[0]; nAB[0]=tmp;
 	    tmp=nBB[0]; nBB[0]=nBA[0]; nBA[0]=tmp;
 	    /* num has now undergone a sign change */
 	    num = pAA[0]*pBB[0] - pAB[0]*pBA[0];
 	    /* flip known array for likelihood computation */
 	    tmp=known[AA]; known[AA]=known[AB]; known[AB]=tmp;
 	    tmp=known[BB]; known[BB]=known[BA]; known[BA]=tmp;
 	}
 	
 	denom1 = (pAA[0]+pBA[0])*(pBA[0]+pBB[0]);
 	denom2 = (pAA[0]+pAB[0])*(pAB[0]+pBB[0]);
 	if (denom1 < denom2) { denom = denom1; }
 	else { denom = denom2; }
 	dprime = num/denom;
 	
 	/* add computation of r^2 = (D^2)/p(1-p)q(1-q) */
 	r2 = num*num/(pA1*pB1*pA2*pB2);
 	
 	/* we've computed D', its' LOD, and r^2 - let's store them and then compute confidence intervals */
 	
 	String returnStr = new String("");
	NumberFormat nf = NumberFormat.getInstance();
	nf.setMinimumFractionDigits(2);
	nf.setMaximumFractionDigits(2);
	
	returnStr += nf.format(dprime);
 	returnStr += "\t";
	returnStr += nf.format(loglike1-loglike0);
 	returnStr += "\t";
	returnStr += nf.format(r2);	
 	returnStr += "\t";
 	
 	real_dprime=dprime;
 	
 	for (i=0; i<=100; i++) {
 	    dpr = (double)i*0.01;
 	    tmpAA = dpr*denom + pA1*pA2; 
 	    tmpAB = pA1-tmpAA;
 	    tmpBA = pA2-tmpAA;
 	    tmpBB = pB1-tmpBA;
 	    if (i==100) {
 		/* one value will be 0 */
 		if (tmpAA < 1e-10) tmpAA=1e-10;
 		if (tmpAB < 1e-10) tmpAB=1e-10;
 		if (tmpBA < 1e-10) tmpBA=1e-10;
 		if (tmpBB < 1e-10) tmpBB=1e-10;
 	    }
 	    lsurface[i] = known[AA]*log10(tmpAA) + known[AB]*log10(tmpAB) + known[BA]*log10(tmpBA) + known[BB]*log10(tmpBB) + (double)unknownDH*log10(tmpAA*tmpBB + tmpAB*tmpBA);
 	}
 	
 	/* Confidence bounds #2 - used in Gabriel et al (2002) - translate into posterior dist of D' - 
 	   assumes a flat prior dist. of D' - someday we may be able to make
 	   this even more clever by adjusting given the distribution of observed
 	   D' values for any given distance after some large scale studies are complete */
 	
 	total_prob=sum_prob=0.0;
 	
 	for (i=0; i<=100; i++) {
 	    lsurface[i] -= loglike1;
 	    lsurface[i] = Math.pow(10.0,lsurface[i]);
 	    total_prob += lsurface[i];
 	}
 	
 	for (i=0; i<=100; i++) {
 	    sum_prob += lsurface[i];
 	    if (sum_prob > 0.05*total_prob &&
 		sum_prob-lsurface[i] < 0.05*total_prob) {
 		low_i = i-1;
 		break;
 	    }
 	}
 	
 	sum_prob=0.0;
 	for (i=100; i>=0; i--) {
 	    sum_prob += lsurface[i];
 	    if (sum_prob > 0.05*total_prob &&
 		sum_prob-lsurface[i] < 0.05*total_prob) {
 		high_i = i+1;
 		break;
 	    }
 	}
 
 	returnStr += (double) low_i/100.0;
 	returnStr += "\t";
 	returnStr += (double) high_i/100.0;	
 	return returnStr;
     }
     
     public void count_haps(double pAA, double pAB, double pBA, double pBB,
 			   double[] nAA, double[] nAB, double[] nBA, double[] nBB,
 			   int em_round)
     {
 	/* only the double heterozygote [AB][AB] results in 
 	   ambiguous reconstruction, so we'll count the obligates 
 	   then tack on the [AB][AB] for clarity */
 	
 	nAA[0] = (double) (known[AA]);
 	nAB[0] = (double) (known[AB]);
 	nBA[0] = (double) (known[BA]);
 	nBB[0] = (double) (known[BB]);	
 	if (em_round > 0) {
 	    nAA[0] += unknownDH* (pAA*pBB)/((pAA*pBB)+(pAB*pBA));
 	    nBB[0] += unknownDH* (pAA*pBB)/((pAA*pBB)+(pAB*pBA));
 	    nAB[0] += unknownDH* (pAB*pBA)/((pAA*pBB)+(pAB*pBA));
 	    nBA[0] += unknownDH* (pAB*pBA)/((pAA*pBB)+(pAB*pBA));
 	}
     }
     
     public void estimate_p(double nAA, double nAB, double nBA, double nBB,
 			   double[] pAA, double[] pAB, double[] pBA, double[] pBB) {
 	double total= nAA+nAB+nBA+nBB+(4.0*const_prob);
 	pAA[0]=(nAA+const_prob)/total; if (pAA[0] < 1e-10) pAA[0]=1e-10;
 	pAB[0]=(nAB+const_prob)/total; if (pAB[0] < 1e-10) pAB[0]=1e-10;
 	pBA[0]=(nBA+const_prob)/total; if (pBA[0] < 1e-10) pBA[0]=1e-10;
 	pBB[0]=(nBB+const_prob)/total; if (pBB[0] < 1e-10) pBB[0]=1e-10;	
     }
 
     public double log10 (double d) {
 	return Math.log(d)/LN10;
     }
 
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
 		dPrimeTable[pos1][pos2] = computeDPrime(twoMarkerHaplos[1][1], twoMarkerHaplos[1][2], twoMarkerHaplos[2][1], twoMarkerHaplos[2][2], doublehet, 0.1);
 	    }
 	}
 	return dPrimeTable;
     }
 
     //everything below here is related to em phasing of haps
     
     class RECOVERY {
 	short h1;
 	short h2;
 	float p;
 	public RECOVERY() {
 	    h1=0;
 	    h2=0;
 	    p=0.0f;
 	}
     }
 	
     class OBS {
 	int nposs;
 	RECOVERY[] poss;
 	public OBS(int size) {
 	    poss = new RECOVERY[size];
 	    for (int i=0; i<size; ++i) poss[i] = new RECOVERY();
 	}
     }
 	
     class SUPER_OBS {
 	int nblocks;
 	int[] nposs;
 	RECOVERY[][] poss;
 	int nsuper;
 	RECOVERY[] superposs;	    
 	public SUPER_OBS(int size) {
 	    poss = new RECOVERY[size][];
 	    nposs = new int[size];
 	}
     }
 	
     public int full_em_breakup(int num_haplos, int num_loci, char[][] input_haplos, int max_missing, int[] num_haplos_present, Vector haplos_present, Vector haplo_freq, int num_blocks, int[] block_size, int dump_phased_haplos){
 	int i, j, k, num_poss, iter, maxk, numk;
 	double total, maxprob;
 	int block, start_locus, end_locus, biggest_block_size;
 	int poss_full, best, h1, h2;
 	int num_indivs=0;
 	
 	if (num_loci > MAXLOCI) return(-1);
 	biggest_block_size=block_size[0];
 	for (i=1; i<num_blocks; i++) {
 	    if (block_size[i] > biggest_block_size) biggest_block_size=block_size[i];
 	}
 
 	two_n[0]=1;
 	for (i=1; i<31; i++) two_n[i]=2*two_n[i-1];
 	    
 	num_poss = two_n[biggest_block_size];
 	data = new OBS[num_haplos/2];
 	for (i=0; i<num_haplos/2; i++) data[i]= new OBS(num_poss*two_n[max_missing]);
 	superdata = new SUPER_OBS[num_haplos/2];
 	for (i=0; i<num_haplos/2; i++) superdata[i]= new SUPER_OBS(num_blocks);
 	
 	double[][] hprob = new double[num_blocks][num_poss];
 	int[][] hlist = new int[num_blocks][num_poss];
 	int[] num_hlist = new int[num_blocks];	    
 	int[] hint = new int[num_poss];
 	prob = new double[num_poss];
 	    
 	end_locus=-1;  
 	for (block=0; block<num_blocks; block++) {
 	    
 	    start_locus=end_locus+1;
 	    end_locus=start_locus+block_size[block]-1;
 	    num_poss=two_n[block_size[block]];
 		
 	    if ((num_indivs=read_observations(num_haplos,num_loci,input_haplos,start_locus,end_locus)) <= 0) return(-1);
 		
 	    /* start prob array with probabilities from full observations */
 	    for (j=0; j<num_poss; j++) { prob[j]=PSEUDOCOUNT; }
 	    total=(double)num_poss;
 	    total *= PSEUDOCOUNT;
 		
 	    /* starting prob is phase known haps + 0.1 (PSEUDOCOUNT) count of every haplotype -
 	       i.e., flat when nothing is known, close to phase known if a great deal is known */
 		
 	    for (i=0; i<num_indivs; i++) {
 		if (data[i].nposs==1) {
 		    prob[data[i].poss[0].h1]+=1.0;
 		    prob[data[i].poss[0].h2]+=1.0;
 		    total+=2.0;
 		}
 	    }
 	    
 	    /* normalize */
 	    for (j=0; j<num_poss; j++) { 
 		prob[j] /= total;
 	    }
 		
 	    /* EM LOOP: assign ambiguous data based on p, then re-estimate p */
 	    iter=0;
 	    while (iter<20) {
 		/* compute probabilities of each possible observation */
 		for (i=0; i<num_indivs; i++) {
 		    total=0.0;
 		    for (k=0; k<data[i].nposs; k++) {
 			data[i].poss[k].p = (float)(prob[data[i].poss[k].h1]*prob[data[i].poss[k].h2]);
 			total+=data[i].poss[k].p;
 		    }
 		    /* normalize */
 		    for (k=0; k<data[i].nposs; k++) {
 			data[i].poss[k].p /= total;
 		    }
 		}
 		    
 		/* re-estimate prob */
 		    
 		for (j=0; j<num_poss; j++) { prob[j]=1e-10; }
 		total=num_poss*1e-10;
 		
 		for (i=0; i<num_indivs; i++) {
 		    for (k=0; k<data[i].nposs; k++) {
 			prob[data[i].poss[k].h1]+=data[i].poss[k].p;
 			prob[data[i].poss[k].h2]+=data[i].poss[k].p;
 			total+=(2.0*data[i].poss[k].p);
 		    }
 		}
 		    
 		/* normalize */
 		for (j=0; j<num_poss; j++) { 
 		    prob[j] /= total;
 		}
 		iter++;
 	    }
 		
 	    /* printf("FINAL PROBABILITIES:\n"); */
 	    k=0;
 	    for (j=0; j<num_poss; j++) { 
 		hint[j]=-1; 
 		if (prob[j] > .001) { 
 		    /* printf("haplo %s   p = %.4lf\n",haplo_str(j,block_size[block]),prob[j]); */
 		    hlist[block][k]=j; hprob[block][k]=prob[j];
 		    hint[j]=k; 
 		    k++;
 		}
 	    }
 	    num_hlist[block]=k;
 		
 	    /* store current block results in super obs structure */
 	    store_block_haplos(hlist, hprob, hint, block, num_indivs); 
 	    
 	} /* for each block */
 	    
 	poss_full=1;
 	for (block=0; block<num_blocks; block++) {
 	    poss_full *= num_hlist[block];
 	}
 	    
 	/* LIGATE and finish this mess :) */
 	
 	if (poss_full > 1000000) {
 	    /* what we really need to do is go through and pare back
 	       to using a smaller number (e.g., > .002, .005) */
 	    //printf("too many possibilities: %d\n",poss_full);
 	    return(-5);
 	}
 	double[] superprob = new double[poss_full];
 
 	create_super_haplos(num_indivs,num_blocks,num_hlist);
 	    
 	/* run standard EM on supercombos */
 	    
 	/* start prob array with probabilities from full observations */
 	for (j=0; j<poss_full; j++) { superprob[j]=PSEUDOCOUNT; }
 	total=(double)poss_full;
 	total *= PSEUDOCOUNT;
 	    
 	/* starting prob is phase known haps + 0.1 (PSEUDOCOUNT) count of every haplotype -
 	   i.e., flat when nothing is known, close to phase known if a great deal is known */
 	    
 	for (i=0; i<num_indivs; i++) {
 	    if (superdata[i].nsuper==1) {
 		superprob[superdata[i].superposs[0].h1]+=1.0;
 		superprob[superdata[i].superposs[0].h2]+=1.0;
 		total+=2.0;
 	    }
 	}
 	    
 	/* normalize */
 	for (j=0; j<poss_full; j++) { 
 	    superprob[j] /= total;
 	}
 	    
 	/* EM LOOP: assign ambiguous data based on p, then re-estimate p */
 	iter=0;
 	while (iter<20) {
 	    /* compute probabilities of each possible observation */
 	    for (i=0; i<num_indivs; i++) {
 		total=0.0;
 		for (k=0; k<superdata[i].nsuper; k++) {
 		    superdata[i].superposs[k].p = (float)
 			(superprob[superdata[i].superposs[k].h1]*
 			superprob[superdata[i].superposs[k].h2]);
 		    total+=superdata[i].superposs[k].p;
 		}
 		/* normalize */
 		for (k=0; k<superdata[i].nsuper; k++) {
 		    superdata[i].superposs[k].p /= total;
 		}
 	    }
 		
 	    /* re-estimate prob */
 	    
 	    for (j=0; j<poss_full; j++) { superprob[j]=1e-10; }
 	    total=poss_full*1e-10;
 	    
 	    for (i=0; i<num_indivs; i++) {
 		for (k=0; k<superdata[i].nsuper; k++) {
 		    superprob[superdata[i].superposs[k].h1]+=superdata[i].superposs[k].p;
 		    superprob[superdata[i].superposs[k].h2]+=superdata[i].superposs[k].p;
 		    total+=(2.0*superdata[i].superposs[k].p);
 		}
 	    }
 		
 	    /* normalize */
 	    for (j=0; j<poss_full; j++) { 
 		superprob[j] /= total;
 	    }
 	    iter++;
 	}
 	    
 
 	/* we're done - the indices of superprob now have to be
 	   decoded to reveal the actual haplotypes they represent */
 	    
 	k=0;
 	for (j=0; j<poss_full; j++) { 
 	    if (superprob[j] > .001) {
 		haplos_present.addElement(decode_haplo_str(j,num_blocks,block_size,hlist,num_hlist));
 		
 		//sprintf(haplos_present[k],"%s",decode_haplo_str(j,num_blocks,block_size,hlist,num_hlist));
 		haplo_freq.addElement(String.valueOf(superprob[j]));
 		k++;
 	    }
 	}
 	num_haplos_present[0]=k;
 	/*    
 	      if (dump_phased_haplos) {
 	      
 	      if ((fpdump=fopen("emphased.haps","w"))!=NULL) {
 	      for (i=0; i<num_indivs; i++) {
 	      best=0;
 	      for (k=0; k<superdata[i].nsuper; k++) {
 	      if (superdata[i].superposs[k].p > superdata[i].superposs[best].p) {
 	      best=k;
 	      }
 	      }
 	      h1 = superdata[i].superposs[best].h1;
 	      h2 = superdata[i].superposs[best].h2;
 	      fprintf(fpdump,"%s\n",decode_haplo_str(h1,num_blocks,block_size,hlist,num_hlist));
 	      fprintf(fpdump,"%s\n",decode_haplo_str(h2,num_blocks,block_size,hlist,num_hlist));
 	      }
 	      fclose(fpdump);
 	      }
 	      }
 	*/
 	return 0;
     }
 	
         
          
     public int read_observations(int num_haplos, int num_loci, char[][] haplo, int start_locus, int end_locus) {
 	int i, j, a1, a2, h1, h2, two_n, num_poss, loc, ind;
 	char c1, c2;
 	int num_indivs = 0;    
 	int[] dhet = new int[MAXLOCI];
 	int[] missing1 = new int[MAXLOCI];
 	int[] missing2 = new int[MAXLOCI];
 	for (i=0; i<MAXLOCI; ++i) {
 	    dhet[i]=0;
 	    missing1[i]=0;
 	    missing2[i]=0;
 	}
 	
 	for (ind=0; ind<num_haplos; ind+=2) { 
 	    
 	    two_n=1; h1=h2=0; num_poss=1;
 	    
 	    for (loc=start_locus; loc<=end_locus; loc++) {
 		i = loc-start_locus;
 		
 		c1=haplo[ind][loc]; c2=haplo[ind+1][loc];
 		if (c1=='h' || c1=='9') {
 		    a1=0; a2=1; dhet[i]=1; missing1[i]=0; missing2[i]=0;
 		} else {
 		    dhet[i]=0; missing1[i]=0; missing2[i]=0;
 		    if (c1 > '0' && c1 < '3') a1=c1-'1';
 		    else {        a1=0; missing1[i]=1; }
 		    
 		    if (c2 > '0' && c2 < '3') a2=c2-'1';
 		    else {  a2=0; missing2[i]=1; }
 		    
 		    if (c1 < '0' || c1 > '4' || c2 < '0' || c2 > '4') {
 			//    printf("bad allele in data file (%s,%s)\n",ln1,ln2);
 			return(-1);
 		    }
 		}
 	    
 		h1 += two_n*a1;
 		h2 += two_n*a2;
 		if (dhet[i]==1) num_poss*=2;
 		if (missing1[i]==1) num_poss*=2;
 		if (missing2[i]==1) num_poss*=2;
 		
 		two_n *= 2;
 	    }
 	
 	    data[num_indivs].nposs = num_poss;
 	    data[num_indivs].poss[0].h1=(short)h1;
 	    data[num_indivs].poss[0].h2=(short)h2;
 	    data[num_indivs].poss[0].p=0.0f;
 	
 	/*    printf("h1=%s, ",haplo_str(h1));
 	      printf("h2=%s, dhet=%d%d%d%d%d, nposs=%d\n",haplo_str(h2),dhet[0],dhet[1],dhet[2],dhet[3],dhet[4],num_poss); */
 	
 	    two_n=1; num_poss=1;
 	    for (i=0; i<=end_locus-start_locus; i++) {
 		if (dhet[i]!=0) {
 		    for (j=0; j<num_poss; j++) {
 			/* flip bits at this position and call this num_poss+j */
 			h1 = data[num_indivs].poss[j].h1;
 			h2 = data[num_indivs].poss[j].h2;
 			/* printf("FLIP: position %d, two_n=%d, h1=%d, h2=%d, andh1=%d, andh2=%d\n",i,two_n,h1,h2,h1&two_n,h2&two_n);  */
 			if ((h1&two_n)==two_n && (h2&two_n)==0) {
 			    h1 -= two_n; h2 += two_n; 
 			} else if ((h1&two_n)==0 && (h2&two_n)==two_n) {
 			    h1 += two_n; h2 -= two_n; 
 			} else {
 			    //printf("error - attepmting to flip homozygous position\n");
 			}
 			data[num_indivs].poss[num_poss+j].h1=(short)h1;
 			data[num_indivs].poss[num_poss+j].h2=(short)h2;
 			data[num_indivs].poss[num_poss+j].p=0.0f;
 		    }
 		    num_poss *= 2;
 		}
 		
 		if (missing1[i]!=0) {
 		    for (j=0; j<num_poss; j++) {
 			/* flip bits at this position and call this num_poss+j */
 			h1 = data[num_indivs].poss[j].h1;
 			h2 = data[num_indivs].poss[j].h2;
 			/* printf("MISS1: position %d, two_n=%d, h1=%d, h2=%d, newh1=%d, newh2=%d\n",i,two_n,h1,h2,h1+two_n,h2); */
 			if ((h1&two_n)==0) {
 			    h1 += two_n; 
 			} else {
 			    //printf("error - attempting to flip missing !=0\n");
 			}
 			data[num_indivs].poss[num_poss+j].h1=(short)h1;
 			data[num_indivs].poss[num_poss+j].h2=(short)h2;
 			data[num_indivs].poss[num_poss+j].p=0.0f;
 		    }
 		    num_poss *= 2;
 		}
 	    
 		if (missing2[i]!=0) {
 		    for (j=0; j<num_poss; j++) {
 			/* flip bits at this position and call this num_poss+j */
 			h1 = data[num_indivs].poss[j].h1;
 			h2 = data[num_indivs].poss[j].h2;
 			/* printf("MISS2: position %d, two_n=%d, h1=%d, h2=%d, newh1=%d, newh2=%d\n",i,two_n,h1,h2,h1,h2+two_n);  */
 			if ((h2&two_n)==0) {
 			    h2 += two_n; 
 			} else {
 			    //printf("error - attempting to flip missing !=0\n");
 			}
 			data[num_indivs].poss[num_poss+j].h1=(short)h1;
 			data[num_indivs].poss[num_poss+j].h2=(short)h2;
 			data[num_indivs].poss[num_poss+j].p=0.0f;
 		    }
 		    num_poss *= 2;
 		}
 		
 		two_n *= 2;
 	    }
 	    /* printf("num_poss = %d  also %d\n",num_poss, data[num_indivs].nposs); */
 	    num_indivs++;
 	}
 	
 	return(num_indivs);
     }
 
 
     public void store_block_haplos(int[][] hlist, double[][] hprob, int[] hint, int block, int num_indivs)
     {
 	int i, j, k, num_poss, h1, h2;
 	
 	for (i=0; i<num_indivs; i++) {
 	    num_poss=0;
 	    for (j=0; j<data[i].nposs; j++) {
 		h1 = data[i].poss[j].h1;
 		h2 = data[i].poss[j].h2;
 		if (hint[h1] >= 0 && hint[h2] >= 0) {
 		    /* valid combination, both haplos passed to 2nd round */ 
 		    num_poss++;
 		}
 	    }
 	    /* allocate and store */ 
 	    superdata[i].nposs[block]=num_poss;
 	    if (num_poss > 0) {
 		//superdata[i].poss[block] = (RECOVERY *) malloc (num_poss * sizeof(RECOVERY));
 		superdata[i].poss[block] = new RECOVERY[num_poss];
 		for (int ii=0; ii<num_poss; ++ii) superdata[i].poss[block][ii] = new RECOVERY();
 		k=0;
 		for (j=0; j<data[i].nposs; j++) {
 		    h1 = data[i].poss[j].h1;
 		    h2 = data[i].poss[j].h2;
 		    if (hint[h1] >= 0 && hint[h2] >= 0) {
 			superdata[i].poss[block][k].h1 = (short)hint[h1];
 			superdata[i].poss[block][k].h2 = (short)hint[h2];
 			k++;
 		    }
 		}
 	    }
 	}
     }
 
     public String haplo_str(int h, int num_loci)
     {
 	int i, val;
 	String s="";
 	for (i=0; i<num_loci; i++) {
 	    if ((h&two_n[i])==two_n[i]) { s+="2"; }
 	    else { s+="1"; }
 	} 
 	return(s);
     }
 
     public String decode_haplo_str(int chap, int num_blocks, int[] block_size, int[][] hlist, int[] num_hlist)
     {
 	int i, val;
 	String s = "";
 	for (i=0; i<num_blocks; i++) {
 	    val = chap % num_hlist[i];
 	    s+=haplo_str(hlist[i][val],block_size[i]);
 	    chap -= val;
 	    chap /= num_hlist[i];
 	}
 	return(s);
     }
 
     public void create_super_haplos(int num_indivs, int num_blocks, int[] num_hlist)
     {
 	int i, j, num_poss, h1, h2;
 	
 	for (i=0; i<num_indivs; i++) {
 	    num_poss=1;
 	    for (j=0; j<num_blocks; j++) {
 		num_poss *= superdata[i].nposs[j];
 	    }
 	    
 	    superdata[i].nsuper=0;
 	    superdata[i].superposs = new RECOVERY[num_poss];
 	    for (int ii=0; ii<num_poss; ++ii) superdata[i].superposs[ii] = new RECOVERY();
 	    
 	    /* block 0 */
 	    for (j=0; j<superdata[i].nposs[0]; j++) {
 		h1 = superdata[i].poss[0][j].h1;
 		h2 = superdata[i].poss[0][j].h2;
 		recursive_superposs(h1,h2,1,num_blocks,num_hlist,i);
 	    }
 
 	    if (superdata[i].nsuper != num_poss) {
 		//printf("error in superfill\n");
 	    }
 	}
     }
 
     public void recursive_superposs(int h1, int h2, int block, int num_blocks, int[] num_hlist, int indiv) {
 	int j, curr_prod, newh1, newh2;
 	
 	if (block == num_blocks) {
 	    superdata[indiv].superposs[superdata[indiv].nsuper].h1 = (short)h1;
 	    superdata[indiv].superposs[superdata[indiv].nsuper].h2 = (short)h2;
 	    superdata[indiv].nsuper++;
 	} else {
 	    curr_prod=1;
 	    for (j=0; j<block; j++) { curr_prod*=num_hlist[j]; }
 	    
 	    for (j=0; j<superdata[indiv].nposs[block]; j++) {
 		newh1 = h1 + (superdata[indiv].poss[block][j].h1 * curr_prod);
 		newh2 = h2 + (superdata[indiv].poss[block][j].h2 * curr_prod);
 		recursive_superposs(newh1,newh2,block+1,num_blocks,num_hlist,indiv);
 	    }
 	}
     }
 }
