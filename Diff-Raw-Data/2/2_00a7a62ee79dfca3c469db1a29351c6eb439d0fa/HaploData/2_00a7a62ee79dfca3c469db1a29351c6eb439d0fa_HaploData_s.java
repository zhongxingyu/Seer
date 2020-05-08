 package edu.mit.wi.haploview;
 
 
 import edu.mit.wi.pedfile.*;
 import java.io.*;
 import java.util.*;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.text.NumberFormat;
 import java.net.URL;
 import java.net.MalformedURLException;
 
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 
 public class HaploData implements Constants{
 
     private Vector chromosomes;
     private Vector extraTrioChromosomes;
     private Haplotype[][] haplotypes;
     private Haplotype[][] rawHaplotypes;
     Vector blocks;
     boolean[] isInBlock;
     boolean infoKnown = false;
     boolean blocksChanged = false;
     public DPrimeTable dpTable;
     private PedFile pedFile;
     public boolean finished = false;
     private double[] percentBadGenotypes;
     XYSeriesCollection analysisTracks = new XYSeriesCollection();
     boolean trackExists = false;
     boolean dupsToBeFlagged = false, dupNames = false;
 
     //stuff for computing d prime
     private int AA = 0;
     private int AB = 1;
     private int BA = 2;
     private int BB = 3;
     private double TOLERANCE = 0.00000001;
     private double LN10 = Math.log(10.0);
     int unknownDH=-1;
     int total_chroms=-1;
     double const_prob=-1.0;
     double[] known = new double[5];
     double[] numHaps = new double[4];
     double[] probHaps = new double[4];
 
     //These are iterators for the progress bar.
     int dPrimeTotalCount = -1;
     int dPrimeCount;
 
     private static boolean phasedData = false;
 
     public int numTrios, numSingletons,numPeds;
 
     public PedFile getPedFile(){
         return this.pedFile;
     }
 
     void prepareMarkerInput(InputStream inStream, String[][] hapmapGoodies) throws IOException, HaploViewException{
         //this method is called to gather data about the markers used.
         //It is assumed that the input file is two columns, the first being
         //the name and the second the absolute position. the maxdist is
         //used to determine beyond what distance comparisons will not be
         //made. if the infile param is null, loads up "dummy info" for
         //situation where no info file exists
         //An optional third column is supported which is designed to hold
         //association study data.  If there is a third column there will be
         //a visual indicator in the D' display that there is additional data
         //and the detailed data can be viewed with a mouse press.
 
         Vector names = new Vector();
         HashSet nameSearch = new HashSet();
         HashSet dupCheck = new HashSet();
         Vector positions = new Vector();
         Vector extras = new Vector();
         boolean infoProblem = false;
 
         dupsToBeFlagged = false;
         dupNames = false;
         try{
             if (inStream != null){
                 //read the input file:
                 BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                 String currentLine;
                 long prevloc = -1000000000;
 
 
                 int lineCount = 0;
                 while ((currentLine = in.readLine()) != null){
                     StringTokenizer st = new StringTokenizer(currentLine);
                     if (st.countTokens() > 1){
                         lineCount++;
                     }else if (st.countTokens() == 1){
                         //complain if only one field found
                         throw new HaploViewException("Info file format error on line "+lineCount+
                                 ":\n Info file must be of format: <markername> <markerposition>");
                     }else{
                         //skip blank lines
                         continue;
                     }
 
                     String name = st.nextToken();
                     String l = st.nextToken();
                     String extra = null;
                     if (st.hasMoreTokens()) extra = st.nextToken();
                     long loc;
                     try{
                         loc = Long.parseLong(l);
                     }catch (NumberFormatException nfe){
                         infoProblem = true;
                         throw new HaploViewException("Info file format error on line "+lineCount+
                                 ":\n\"" + l + "\" should be of type long." +
                                 "\n Info file must be of format: <markername> <markerposition>");
                     }
 
                     //basically if anyone is crazy enough to load a dataset, then go back and load
                     //an out-of-order info file we tell them to bugger off and start over.
                     if (loc < prevloc && Chromosome.markers != null){
                         infoProblem = true;
                         throw new HaploViewException("Info file out of order with preloaded dataset:\n"+
                                 name + "\nPlease reload data file and info file together.");
                     }
                     prevloc = loc;
 
                     if (nameSearch.contains(name)){
                         dupCheck.add(name);
                     }
                     names.add(name);
                     nameSearch.add(name);
                     positions.add(l);
                     extras.add(extra);
                 }
 
                 if (lineCount > Chromosome.getUnfilteredSize()){
                     infoProblem = true;
                     throw(new HaploViewException("Info file error:\nMarker number mismatch: too many\nmarkers in info file compared to data file."));
                 }
                 if (lineCount < Chromosome.getUnfilteredSize()){
                     infoProblem = true;
                     throw(new HaploViewException("Info file error:\nMarker number mismatch: too few\nmarkers in info file compared to data file."));
                 }
                 infoKnown=true;
             }
 
             if (hapmapGoodies != null){
                 //we know some stuff from the hapmap so we'll add it here
                 for (int x=0; x < hapmapGoodies.length; x++){
                     if (nameSearch.contains(hapmapGoodies[x][0])){
                         dupCheck.add(hapmapGoodies[x][0]);
                     }
                     names.add(hapmapGoodies[x][0]);
                     nameSearch.add(hapmapGoodies[x][0]);
                     positions.add(hapmapGoodies[x][1]);
                     extras.add(null);
 
                 }
                 infoKnown = true;
             }
 
 
 
             if(dupCheck.size() > 0) {
                 int nameCount = names.size();
                 Hashtable dupCounts = new Hashtable();
                 for(int i=0;i<nameCount;i++) {
                     if(dupCheck.contains(names.get(i))){
                         String n = (String) names.get(i);
                         if(dupCounts.containsKey(n)){
                             int numDups = ((Integer) dupCounts.get(n)).intValue();
                             String newName = n + "."  + numDups;
                             while (nameSearch.contains(newName)){
                                 numDups++;
                                 newName = n + "." + numDups;
                             }
                             names.setElementAt(newName,i);
                             nameSearch.add(newName);
                             dupCounts.put(n,new Integer(numDups)) ;
                         }else {
                             //we leave the first instance with its original name
                             dupCounts.put(n,new Integer(1));
                         }
                         dupNames = true;
                     }
                 }
             }
 
             //sort the  markers
             int numLines = names.size();
 
             class SortingHelper implements Comparable{
                 long pos;
                 int orderInFile;
 
                 public SortingHelper(long pos, int order){
                     this.pos = pos;
                     this.orderInFile = order;
                 }
 
                 public int compareTo(Object o) {
                     SortingHelper sh = (SortingHelper)o;
                     if (sh.pos > pos){
                         return -1;
                     }else if (sh.pos < pos){
                         return 1;
                     }else{
                         return 0;
                     }
                 }
             }
 
             boolean needSort = false;
             Vector sortHelpers = new Vector();
 
             for (int k = 0; k < (numLines); k++){
                 sortHelpers.add(new SortingHelper(Long.parseLong((String)positions.get(k)),k));
             }
 
             //loop through and check if any markers are out of order
             for (int k = 1; k < (numLines); k++){
                 if(((SortingHelper)sortHelpers.get(k)).compareTo(sortHelpers.get(k-1)) < 0) {
                     needSort = true;
                     break;
                 }
             }
             //if any were out of order, then we need to put them in order
             if(needSort){
                 //throw new HaploViewException("unsorted files not supported at present");
                 //sort the positions
                 Collections.sort(sortHelpers);
                 Vector newNames = new Vector();
                 Vector newExtras = new Vector();
                 Vector newPositions = new Vector();
                 int[] realPos = new int[numLines];
 
                 //reorder the vectors names and extras so that they have the same order as the sorted markers
                 for (int i = 0; i < sortHelpers.size(); i++){
                     realPos[i] = ((SortingHelper)sortHelpers.get(i)).orderInFile;
                     newNames.add(names.get(realPos[i]));
                     newPositions.add(positions.get(realPos[i]));
                     newExtras.add(extras.get(realPos[i]));
                 }
 
                 names = newNames;
                 extras = newExtras;
                 positions = newPositions;
 
                 byte[] tempGenotype = new byte[sortHelpers.size()];
                 //now we reorder all the individuals genotypes according to the sorted marker order
                 for(int j=0;j<chromosomes.size();j++){
                     Chromosome tempChrom = (Chromosome)chromosomes.elementAt(j);
                     for(int i =0;i<sortHelpers.size();i++){
                         tempGenotype[i] = tempChrom.getUnfilteredGenotype(realPos[i]);
                     }
                     for(int i=0;i<sortHelpers.size();i++){
                         tempChrom.setGenotype(tempGenotype[i],i);
                     }
                 }
 
                 for(int j=0;j<extraTrioChromosomes.size();j++) {
                     Chromosome tempChrom = (Chromosome)extraTrioChromosomes.elementAt(j);
                     for(int i =0;i<sortHelpers.size();i++){
                         tempGenotype[i] = tempChrom.getUnfilteredGenotype(realPos[i]);
                     }
                     for(int i=0;i<sortHelpers.size();i++){
                         tempChrom.setGenotype(tempGenotype[i],i);
                     }
                 }
 
                 //sort pedfile objects
                 //todo: this should really be done before pedfile is subjected to any processing.
                 //todo: that would require altering some order of operations in dealing with inputs
 
                 //todo: this will fry an out-of-order haps file...grr
                 Vector unsortedRes = pedFile.getResults();
                 Vector sortedRes = new Vector();
                 for (int i = 0; i < realPos.length; i++){
                     sortedRes.add(unsortedRes.elementAt(realPos[i]));
                 }
                 pedFile.setResults(sortedRes);
                 Vector o = pedFile.getAllIndividuals();
                 for (int i = 0; i < o.size(); i++){
                     Individual ind = (Individual) o.get(i);
                     byte[] sortedMarkersa = new byte[ind.getNumMarkers()];
                     byte[] sortedMarkersb = new byte[ind.getNumMarkers()];
                     boolean[] unsortedZeroed = ind.getZeroedArray();
                     boolean[] sortedZeroed = new boolean[unsortedZeroed.length];
                     for (int j = 0; j < ind.getNumMarkers(); j++){
                         sortedMarkersa[j] = ind.getAllele(realPos[j],0);
                         sortedMarkersb[j] = ind.getAllele(realPos[j],1);
                         sortedZeroed[j] = unsortedZeroed[realPos[j]];
                     }
                     ind.setMarkers(sortedMarkersa, sortedMarkersb);
                     ind.setZeroedArray(sortedZeroed);
                 }
             }
 
         }catch (HaploViewException e){
             throw(e);
         }finally{
             double numChroms = chromosomes.size();
             Vector markerInfo = new Vector();
             double[] numBadGenotypes = new double[Chromosome.getUnfilteredSize()];
             percentBadGenotypes = new double[Chromosome.getUnfilteredSize()];
             Vector results = null;
             if (pedFile != null){
                 results = pedFile.getResults();
             }
             long prevPosition = Long.MIN_VALUE;
             SNP prevMarker = null;
             MarkerResult pmr = null;
             for (int i = 0; i < Chromosome.getUnfilteredSize(); i++){
                 MarkerResult mr = null;
                 if (results != null){
                     mr = (MarkerResult)results.elementAt(i);
                 }
                 //to compute minor/major alleles, browse chrom list and count instances of each allele
                 byte a1 = 0; byte a2 = 0;
                 double numa1 = 0; double numa2 = 0;
                 for (int j = 0; j < chromosomes.size(); j++){
                     //if there is a data point for this marker on this chromosome
                     byte thisAllele = ((Chromosome)chromosomes.elementAt(j)).getUnfilteredGenotype(i);
                     if (!(thisAllele == 0)){
                         if (thisAllele >= 5){
                             numa1+=0.5; numa2+=0.5;
                             if (thisAllele < 9){
                                 if (a1==0){
                                     a1 = (byte)(thisAllele-4);
                                 }else if (a2 == 0){
                                     if (!(thisAllele-4 == a1)){
                                         a2 = (byte)(thisAllele-4);
                                     }
                                 }
                             }
                         }else if (a1 == 0){
                             a1 = thisAllele; numa1++;
                         }else if (thisAllele == a1){
                             numa1++;
                         }else{
                             numa2++;
                             a2 = thisAllele;
                         }
                     }
                     else {
                         numBadGenotypes[i]++;
                     }
                 }
                if (numa2 > numa1){
                     byte temp = a1;
                     double tempnum = numa1;
                     numa1 = numa2;
                     a1 = a2;
                     numa2 = tempnum;
                     a2 = temp;
                 }
 
                 double maf;
                 if (mr != null){
                     maf = Util.roundDouble(mr.getMAF(),3);
                 }else{
                     maf = Util.roundDouble((numa2/(numa1+numa2)),3);
                 }
 
                 if (Chromosome.markers == null || !infoProblem){
                     if (infoKnown){
                         long pos = Long.parseLong((String)positions.elementAt(i));
 
                         SNP thisMarker = (new SNP((String)names.elementAt(i),
                                 pos, maf, a1, a2,
                                 (String)extras.elementAt(i)));
                         markerInfo.add(thisMarker);
 
                         if (mr != null){
                             double genoPC = mr.getGenoPercent();
                             //check to make sure adjacent SNPs do not have identical positions
                             if (prevPosition != Long.MIN_VALUE){
                                 //only do this for markers 2..N, since we're comparing to the previous location
                                 if (pos == prevPosition){
                                     dupsToBeFlagged = true;
                                     if (genoPC >= pmr.getGenoPercent()){
                                         //use this one because it has more genotypes
                                         thisMarker.setDup(1);
                                         prevMarker.setDup(2);
                                     }else{
                                         //use the other one because it has more genotypes
                                         thisMarker.setDup(2);
                                         prevMarker.setDup(1);
                                     }
                                 }
                             }
                             prevPosition = pos;
                             prevMarker = thisMarker;
                             pmr = mr;
                         }
                     }else{
                         markerInfo.add(new SNP(null,i+1,maf,a1,a2));
                     }
                     percentBadGenotypes[i] = numBadGenotypes[i]/numChroms;
                 }
             }
             if (Chromosome.markers == null || !infoProblem){
                 Chromosome.markers = markerInfo;
             }
         }
     }
 
     public Vector prepareHapsInput(String name) throws IOException, HaploViewException, PedFileException {
         //this method is called to suck in data from a file (its only argument)
         //of genotypes and sets up the Chromosome objects.
         Vector chroms = new Vector();
         Vector hapsFileStrings = new Vector();
         BufferedReader reader;
 
         HaploData.setPhasedData(false);
 
         try{
             URL inURL = new URL(name);
             reader = new BufferedReader(new InputStreamReader(inURL.openStream()));
         }catch(MalformedURLException mfe){
             File inFile = new File(name);
             if (inFile.length() < 1){
                 throw new HaploViewException("Genotype file is empty or nonexistent: " + inFile.getName());
             }
             reader = new BufferedReader(new FileReader(inFile));
         }catch(IOException ioe){
             throw new HaploViewException("Could not connect to " + name);
         }
 
         String line;
         while((line = reader.readLine())!=null){
             if (line.length() == 0){
                 //skip blank lines
                 continue;
             }
             if (line.startsWith("#")){
                 //skip comments
                 continue;
             }
             hapsFileStrings.add(line);
         }
         pedFile = new PedFile();
         pedFile.parseHapsFile(hapsFileStrings);
         Vector result = pedFile.check();
         Vector indList = pedFile.getUnrelatedIndividuals();
         numSingletons = 0;
         Individual currentInd;
         int numMarkers = 0;
         for (int x=0; x<indList.size(); x++){
             currentInd = (Individual)indList.elementAt(x);
             numMarkers = currentInd.getNumMarkers();
             byte[] chrom1 = new byte[numMarkers];
             byte[] chrom2 = new byte[numMarkers];
             for (int i = 0; i < numMarkers; i++){
                 /*byte[] thisMarker;
                 thisMarker = currentInd.getMarker(i);
                 chrom1[i] = thisMarker[0];
                 chrom2[i] = thisMarker[1];
                 */
                 chrom1[i] = currentInd.getAllele(i,0);
                 chrom2[i] = currentInd.getAllele(i,1);
             }
             chroms.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),chrom1,currentInd.getAffectedStatus(),0, false));
             chroms.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),chrom2,currentInd.getAffectedStatus(),0, false));
             numSingletons++;
         }
 
         chromosomes = chroms;
 
         //wipe clean any existing marker info so we know we're starting clean with a new file
         Chromosome.markers = null;
         return result;
     }
 
     public Vector linkageToChrom(String name, int type)
             throws IllegalArgumentException, HaploViewException, PedFileException, IOException{
 
         Vector pedFileStrings = new Vector();
         Vector hapsDataStrings = new Vector();
 
         HaploData.setPhasedData(false);
 
         BufferedReader reader;
         try{
             URL inURL = new URL(name);
             reader = new BufferedReader(new InputStreamReader(inURL.openStream()));
         }catch(MalformedURLException mfe){
             File inFile = new File(name);
             if (inFile.length() < 1){
                 throw new HaploViewException("Genotype file is empty or nonexistent: " + inFile.getName());
             }
             reader = new BufferedReader(new FileReader(inFile));
         }catch(IOException ioe){
             throw new HaploViewException("Could not connect to " + name);
         }
         String line;
         while((line = reader.readLine())!=null){
             if (line.length() == 0){
                 //skip blank lines
                 continue;
             }
             if (line.startsWith("#@")){
                 hapsDataStrings.add(line.substring(2));
                 continue;
             }
             if (line.startsWith("#")){
                 //skip comments
                 continue;
             }
             pedFileStrings.add(line);
         }
         pedFile = new PedFile();
 
         if (type == PED_FILE){
             pedFile.parseLinkage(pedFileStrings);
         }else{
             pedFile.parseHapMap(pedFileStrings, hapsDataStrings);
         }
 
         Vector result = pedFile.check();
         Vector allInds = pedFile.getAllIndividuals();
         Vector unrelatedInds = pedFile.getUnrelatedIndividuals();
         HashSet unrelatedHash = new HashSet(unrelatedInds);
         HashSet indsInTrio = new HashSet();
         int numMarkers = 0;
         numSingletons = 0;
         numTrios = 0;
         numPeds = pedFile.getNumFamilies();
         extraTrioChromosomes = new Vector();
         Individual currentInd;
         Family currentFamily;
         Vector chrom = new Vector();
 
         //first time through we deal with trios.
         for(int x=0; x < allInds.size(); x++){
 
             currentInd = (Individual)allInds.get(x);
             boolean haploid = ((currentInd.getGender() == 1) && Chromosome.getDataChrom().equalsIgnoreCase("chrx"));
             currentFamily = pedFile.getFamily(currentInd.getFamilyID());
             if (currentFamily.containsMember(currentInd.getMomID()) &&
                     currentFamily.containsMember(currentInd.getDadID())){
                 //if indiv has both parents
                 Individual mom = currentFamily.getMember(currentInd.getMomID());
                 Individual dad = currentFamily.getMember(currentInd.getDadID());
                 //if (unrelatedInds.contains(mom) && unrelatedInds.contains(dad)){
                 numMarkers = currentInd.getNumMarkers();
                 byte[] dadT = new byte[numMarkers];
                 byte[] dadU = new byte[numMarkers];
                 byte[] momT = new byte[numMarkers];
                 byte[] momU = new byte[numMarkers];
 
                 for (int i = 0; i < numMarkers; i++){
                     byte kid1, kid2;
                     if (currentInd.getZeroed(i)){
                         kid1 = 0;
                         kid2 = 0;
                     }else{
                         kid1 = currentInd.getAllele(i,0);
                         kid2 = currentInd.getAllele(i,1);
                     }
 
                     byte mom1,mom2;
                     if (currentFamily.getMember(currentInd.getMomID()).getZeroed(i)){
                         mom1 = 0;
                         mom2 = 0;
                     }else{
                         mom1 = (currentFamily.getMember(currentInd.getMomID())).getAllele(i,0);
                         mom2 = (currentFamily.getMember(currentInd.getMomID())).getAllele(i,1);
                     }
 
                     byte dad1,dad2;
                     if (currentFamily.getMember(currentInd.getDadID()).getZeroed(i)){
                         dad1 = 0;
                         dad2 = 0;
                     }else{
                         dad1 = (currentFamily.getMember(currentInd.getDadID())).getAllele(i,0);
                         dad2 = (currentFamily.getMember(currentInd.getDadID())).getAllele(i,1);
                     }
 
 
                     if(haploid) {
                         if(kid1 == 0) {
                             //kid missing
                             dadU[i] = dad1;
                             if (mom1 == mom2) {
                                 momT[i] = mom1;
                                 momU[i] = mom1;
                             } else if (mom1 != 0 && mom2 != 0){
                                 momT[i] = (byte)(4+mom1);
                                 momU[i] = (byte)(4+mom2);
                             }
                         } else {
                             dadU[i] = dad1;
                             if (mom1 == 0) {
                                 momT[i] = kid1;
                                 momU[i] = 0;
                             } else if (mom1 == kid1) {
                                 momT[i] = mom1;
                                 momU[i] = mom2;
                             } else {
                                 momT[i] = mom2;
                                 momU[i] = mom1;
                             }
                         }
 
 
                     }else {
 
                         if (kid1 == 0 || kid2 == 0) {
                             //kid missing
                             if (dad1 == dad2) {
                                 dadT[i] = dad1;
                                 dadU[i] = dad1;
                             } else if (dad1 != 0 && dad2 != 0) {
                                 dadT[i] = (byte)(4+dad1);
                                 dadU[i] = (byte)(4+dad2);
                             }
                             if (mom1 == mom2) {
                                 momT[i] = mom1;
                                 momU[i] = mom1;
                             } else if (mom1 != 0 && mom2 != 0){
                                 momT[i] = (byte)(4+mom1);
                                 momU[i] = (byte)(4+mom2);
                             }
                         } else if (kid1 == kid2) {
                             //kid homozygous
                             if (dad1 == 0) {
                                 dadT[i] = kid1;
                                 dadU[i] = 0;
                             } else if (dad1 == kid1) {
                                 dadT[i] = dad1;
                                 dadU[i] = dad2;
                             } else {
                                 dadT[i] = dad2;
                                 dadU[i] = dad1;
                             }
 
                             if (mom1 == 0) {
                                 momT[i] = kid1;
                                 momU[i] = 0;
                             } else if (mom1 == kid1) {
                                 momT[i] = mom1;
                                 momU[i] = mom2;
                             } else {
                                 momT[i] = mom2;
                                 momU[i] = mom1;
                             }
                         } else {
                             //kid heterozygous and this if tree's a bitch
                             if (dad1 == 0 && mom1 == 0) {
                                 //both missing
                                 dadT[i] = 0;
                                 dadU[i] = 0;
                                 momT[i] = 0;
                                 momU[i] = 0;
                             } else if (dad1 == 0 && mom1 != mom2) {
                                 //dad missing mom het
                                 dadT[i] = 0;
                                 dadU[i] = 0;
                                 momT[i] = (byte)(4+mom1);
                                 momU[i] = (byte)(4+mom2);
                             } else if (mom1 == 0 && dad1 != dad2) {
                                 //dad het mom missing
                                 dadT[i] = (byte)(4+dad1);
                                 dadU[i] = (byte)(4+dad2);
                                 momT[i] = 0;
                                 momU[i] = 0;
                             } else if (dad1 == 0 && mom1 == mom2) {
                                 //dad missing mom hom
                                 momT[i] = mom1;
                                 momU[i] = mom1;
                                 dadU[i] = 0;
                                 if (kid1 == mom1) {
                                     dadT[i] = kid2;
                                 } else {
                                     dadT[i] = kid1;
                                 }
                             } else if (mom1 == 0 && dad1 == dad2) {
                                 //mom missing dad hom
                                 dadT[i] = dad1;
                                 dadU[i] = dad1;
                                 momU[i] = 0;
                                 if (kid1 == dad1) {
                                     momT[i] = kid2;
                                 } else {
                                     momT[i] = kid1;
                                 }
                             } else if (dad1 == dad2 && mom1 != mom2) {
                                 //dad hom mom het
                                 dadT[i] = dad1;
                                 dadU[i] = dad2;
                                 if (kid1 == dad1) {
                                     momT[i] = kid2;
                                     momU[i] = kid1;
                                 } else {
                                     momT[i] = kid1;
                                     momU[i] = kid2;
                                 }
                             } else if (mom1 == mom2 && dad1 != dad2) {
                                 //dad het mom hom
                                 momT[i] = mom1;
                                 momU[i] = mom2;
                                 if (kid1 == mom1) {
                                     dadT[i] = kid2;
                                     dadU[i] = kid1;
                                 } else {
                                     dadT[i] = kid1;
                                     dadU[i] = kid2;
                                 }
                             } else if (dad1 == dad2 && mom1 == mom2) {
                                 //mom & dad hom
                                 dadT[i] = dad1;
                                 dadU[i] = dad1;
                                 momT[i] = mom1;
                                 momU[i] = mom1;
                             } else {
                                 //everybody het
                                 dadT[i] = (byte)(4+dad1);
                                 dadU[i] = (byte)(4+dad2);
                                 momT[i] = (byte)(4+mom1);
                                 momU[i] = (byte)(4+mom2);
                             }
                         }
                     }
                 }
                 if(unrelatedHash.contains(mom) && unrelatedHash.contains(dad) && unrelatedHash.contains(currentInd)){
                     chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),momT, mom.getAffectedStatus(),currentInd.getAffectedStatus(), false));
                     chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),momU, mom.getAffectedStatus(),currentInd.getAffectedStatus(), false));
 
                     if(haploid) {
                         chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),dadU, dad.getAffectedStatus(),currentInd.getAffectedStatus(), false));
                         ((Chromosome)chrom.lastElement()).setHaploid(true);
                     }else if(Chromosome.getDataChrom().equalsIgnoreCase("chrx")){
                         chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),dadT, dad.getAffectedStatus(), currentInd.getAffectedStatus(), false));
                         ((Chromosome)chrom.lastElement()).setHaploid(true);
                     }else {
                         chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),dadT, dad.getAffectedStatus(), currentInd.getAffectedStatus(), false));
                         chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),dadU, dad.getAffectedStatus(), currentInd.getAffectedStatus(), false));
                     }
 
 
                     numTrios++;
                     indsInTrio.add(mom);
                     indsInTrio.add(dad);
                     indsInTrio.add(currentInd);
 
                     //}
                 }else{
                     extraTrioChromosomes.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),momT, mom.getAffectedStatus(),currentInd.getAffectedStatus(), false));
                     extraTrioChromosomes.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),momU, mom.getAffectedStatus(),currentInd.getAffectedStatus(), false));
 
                     if(haploid) {
                         extraTrioChromosomes.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),dadU, dad.getAffectedStatus(),currentInd.getAffectedStatus(), false));
                         ((Chromosome)extraTrioChromosomes.lastElement()).setHaploid(true);
                     }else if(Chromosome.getDataChrom().equalsIgnoreCase("chrx")){
                         extraTrioChromosomes.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),dadT, dad.getAffectedStatus(), currentInd.getAffectedStatus(), false));
                         ((Chromosome)extraTrioChromosomes.lastElement()).setHaploid(true);
                     }else {
                         extraTrioChromosomes.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),dadT, dad.getAffectedStatus(), currentInd.getAffectedStatus(), false));
                         extraTrioChromosomes.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),dadU, dad.getAffectedStatus(), currentInd.getAffectedStatus(), false));
                     }
                 }
             }
         }
         for (int x=0; x<unrelatedInds.size(); x++){
             currentInd = (Individual)unrelatedInds.get(x);
             boolean haploid = ((currentInd.getGender() == 1) && Chromosome.getDataChrom().equalsIgnoreCase("chrx"));
             if (!indsInTrio.contains(currentInd)){
                 //ind has no parents or kids -- he's a singleton
                 numMarkers = currentInd.getNumMarkers();
                 byte[] chrom1 = new byte[numMarkers];
                 byte[] chrom2 = new byte[numMarkers];
                 for (int i = 0; i < numMarkers; i++){
                     byte thisMarkerA, thisMarkerB;
                     if (currentInd.getZeroed(i)){
                         thisMarkerA = 0;
                         thisMarkerB = 0;
                     }else{
                         thisMarkerA = currentInd.getAllele(i,0);
                         thisMarkerB = currentInd.getAllele(i,1);
                     }
                     if (thisMarkerA == thisMarkerB || thisMarkerA == 0 || thisMarkerB == 0){
                         chrom1[i] = thisMarkerA;
                         chrom2[i] = thisMarkerB;
                     }else{
                         chrom1[i] = (byte)(4+thisMarkerA);
                         chrom2[i] = (byte)(4+thisMarkerB);
                     }
                 }
                 chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),chrom1, currentInd.getAffectedStatus(), -1, false));
                 if(!haploid){
                     chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),chrom2,currentInd.getAffectedStatus(), -1, false));
                 }else{
                     ((Chromosome)chrom.lastElement()).setHaploid(true);
                 }
                 numSingletons++;
             }
         }
         chromosomes = chrom;
         //wipe clean any existing marker info so we know we're starting with a new file
         Chromosome.markers = null;
         return result;
     }
 
     public Vector phasedToChrom(String[] info, boolean downloadFile)
             throws IllegalArgumentException, HaploViewException, PedFileException, IOException{
 
         infoKnown = true;
         pedFile = new PedFile();
         if (downloadFile){
             pedFile.parsePhasedDownload(info);
         }else{
             pedFile.parsePhasedData(info);
         }
 
         HaploData.setPhasedData(true);
 
         Vector result = pedFile.check();
         Vector indList = pedFile.getUnrelatedIndividuals();
         Vector chroms = new Vector();
         Individual currentInd;
         int numMarkers;
         byte thisMarkerA, thisMarkerB;
         numSingletons = 0;
 
         for (int x=0; x < indList.size(); x++){
             currentInd = (Individual)indList.get(x);
             numMarkers = currentInd.getNumMarkers();
             byte[] chrom1 = new byte[numMarkers];
             byte[] chrom2 = new byte[numMarkers];
             for (int i=0; i < numMarkers; i++){
                 thisMarkerA = currentInd.getAllele(i,0);
                 thisMarkerB = currentInd.getAllele(i,1);
                 chrom1[i] = thisMarkerA;
                 chrom2[i] = thisMarkerB;
             }
             if(Chromosome.getDataChrom().equalsIgnoreCase("chrx") && currentInd.getGender() == Individual.MALE){
                 chroms.add(new Chromosome(currentInd.getFamilyID(), currentInd.getIndividualID(), chrom1, currentInd.getAffectedStatus(), 0, true));
                 ((Chromosome)chroms.lastElement()).setHaploid(true);
             }else{
                 chroms.add(new Chromosome(currentInd.getFamilyID(), currentInd.getIndividualID(), chrom1, currentInd.getAffectedStatus(), 0, true));
                 chroms.add(new Chromosome(currentInd.getFamilyID(), currentInd.getIndividualID(), chrom2, currentInd.getAffectedStatus(), 0, true));
             }
             numSingletons++;
         }
 
         chromosomes = chroms;
 
         //wipe clean any existing marker info so we know we're starting clean with a new file
         Chromosome.markers = null;
         return result;
     }
 
     void generateDPrimeTable(){
         //calculating D prime requires the number of each possible 2 marker
         //haplotype in the dataset
 
         dpTable = new DPrimeTable(Chromosome.getUnfilteredSize());
 
         int maxdist = Options.getMaxDistance();
         dPrimeTotalCount = ((Chromosome.getUnfilteredSize()-1)*(Chromosome.getUnfilteredSize()-1))/2;
         dPrimeCount = 0;
         //loop through all marker pairs
         for (int pos1 = 0; pos1 < Chromosome.getUnfilteredSize()-1; pos1++){
             Vector dpTemp= new Vector();
             for (int pos2 = pos1 + 1; pos2 < Chromosome.getUnfilteredSize(); pos2++){
                 //if the markers are too far apart don't try to compare them
                 long sep = Chromosome.getUnfilteredMarker(pos2).getPosition() - Chromosome.getUnfilteredMarker(pos1).getPosition();
                 if (maxdist > 0){
                     if (sep <= maxdist){
                         dpTemp.add(computeDPrime(pos1,pos2));
                     }
                 }else{
                     //maxdist==0 is the convention used to force us to compare all the markers
                     dpTemp.add(computeDPrime(pos1,pos2));
                 }
                 dPrimeCount++;
             }
             dpTable.addMarker(dpTemp,pos1);
         }
     }
 
     public Haplotype[][] generateBlockHaplotypes(Vector blocks) throws HaploViewException{
         Haplotype[][] rawHaplotypes = generateHaplotypes(blocks, true);
         Haplotype[][] tempHaplotypes = new Haplotype[rawHaplotypes.length][];
 
         for (int i = 0; i < rawHaplotypes.length; i++) {
             Vector orderedHaps = new Vector();
             //step through each haplotype in this block
             for (int hapCount = 0; hapCount < rawHaplotypes[i].length; hapCount++) {
                 if (orderedHaps.size() == 0) {
                     orderedHaps.add(rawHaplotypes[i][hapCount]);
                 } else {
                     for (int j = 0; j < orderedHaps.size(); j++) {
                         if (((Haplotype)(orderedHaps.elementAt(j))).getPercentage() <
                                 rawHaplotypes[i][hapCount].getPercentage()) {
                             orderedHaps.add(j, rawHaplotypes[i][hapCount]);
                             break;
                         }
                         if ((j+1) == orderedHaps.size()) {
                             orderedHaps.add(rawHaplotypes[i][hapCount]);
                             break;
                         }
                     }
                 }
             }
             tempHaplotypes[i] = new Haplotype[orderedHaps.size()];
             orderedHaps.copyInto(tempHaplotypes[i]);
         }
         tempHaplotypes = generateCrossovers(tempHaplotypes);
         haplotypes = tempHaplotypes;
         this.rawHaplotypes = rawHaplotypes;
         return tempHaplotypes;
     }
 
     public Haplotype[][] generateHaplotypes(Vector blocks, boolean storeEM) throws HaploViewException{
         Haplotype[][] rawHaplotypes = new Haplotype[blocks.size()][];
 
         for (int k = 0; k < blocks.size(); k++){
             int[] preFiltBlock = (int[])blocks.elementAt(k);
             int[] theBlock;
 
             int[] selectedMarkers = new int[0];
             int[] equivClass = new int[0];
             if (preFiltBlock.length > 30){
                 equivClass = new int[preFiltBlock.length];
                 int classCounter = 0;
                 for (int x = 0; x < preFiltBlock.length; x++){
                     int marker1 = preFiltBlock[x];
 
                     //already been lumped into an equivalency class
                     if (equivClass[x] != 0){
                         continue;
                     }
 
                     //start a new equivalency class for this SNP
                     classCounter ++;
                     equivClass[x] = classCounter;
 
                     for (int y = x+1; y < preFiltBlock.length; y++){
                         int marker2 = preFiltBlock[y];
                         if (marker1 > marker2){
                             int tmp = marker1; marker1 = marker2; marker2 = tmp;
                         }
                         if ( dpTable.getLDStats(marker1,marker2) != null
                                 && dpTable.getLDStats(marker1,marker2).getRSquared() == 1.0){
                             //these two SNPs are redundant
                             equivClass[y] = classCounter;
                         }
                     }
                 }
 
                 //parse equivalency classes
                 selectedMarkers = new int[classCounter];
                 for (int x = 0; x < selectedMarkers.length; x++){
                     selectedMarkers[x] = -1;
                 }
                 for (int x = 0; x < classCounter; x++){
                     double genoPC = 1.0;
                     for (int y = 0; y < equivClass.length; y++){
                         if (equivClass[y] == x+1){
                             if (percentBadGenotypes[Chromosome.realIndex[preFiltBlock[y]]] <= genoPC){
                                 selectedMarkers[x] = preFiltBlock[y];
                                 genoPC = percentBadGenotypes[Chromosome.realIndex[preFiltBlock[y]]];
                             }
                         }
                     }
                 }
 
                 theBlock = selectedMarkers;
             }else{
                 theBlock = preFiltBlock;
             }
 
             //kirby patch
             EM theEM = new EM(chromosomes,numTrios, extraTrioChromosomes);
             theEM.doEM(theBlock);
 
             Haplotype[] tempArray = new Haplotype[theEM.numHaplos()];
             int[][] returnedHaplos = theEM.getHaplotypes();
             double[] returnedFreqs = theEM.getFrequencies();
             for (int i = 0; i < theEM.numHaplos(); i++){
                 int[] genos = new int[returnedHaplos[i].length];
                 for (int j = 0; j < genos.length; j++){
                     if (returnedHaplos[i][j] == 1){
                         genos[j] = Chromosome.getMarker(theBlock[j]).getMajor();
                     }else{
                         if (Chromosome.getMarker(theBlock[j]).getMinor() == 0){
                             genos[j] = 8;
                         }else{
                             genos[j] = Chromosome.getMarker(theBlock[j]).getMinor();
                         }
                     }
                 }
 
                 if (selectedMarkers.length > 0){
                     //we need to reassemble the haplotypes
                     Hashtable hapsHash = new Hashtable();
                     //add to hash all the genotypes we phased
                     for (int q = 0; q < genos.length; q++){
                         hapsHash.put(new Integer(theBlock[q]), new Integer(genos[q]));
                     }
                     //now add all the genotypes we didn't bother phasing, based on
                     //which marker they are identical to
                     for (int q = 0; q < equivClass.length; q++){
                         int currentClass = equivClass[q]-1;
                         if (selectedMarkers[currentClass] == preFiltBlock[q]){
                             //we alredy added the phased genotypes above
                             continue;
                         }
                         int indexIntoBlock=0;
                         for (int x = 0; x < theBlock.length; x++){
                             if (theBlock[x] == selectedMarkers[currentClass]){
                                 indexIntoBlock = x;
                                 break;
                             }
                         }
                         //this (somewhat laboriously) reconstructs whether to add the minor or major allele
                         //for markers with MAF close to 0.50 we can't use major/minor alleles to match
                         //'em up 'cause these might change given missing data
                         boolean success = false;
                         if (Chromosome.getMarker(selectedMarkers[currentClass]).getMAF() > 0.4){
                             for (int z = 0; z < chromosomes.size(); z++){
                                 Chromosome thisChrom = (Chromosome)chromosomes.elementAt(z);
                                 Chromosome nextChrom = (Chromosome)chromosomes.elementAt(++z);
                                 int theGeno = thisChrom.getGenotype(selectedMarkers[currentClass]);
                                 int nextGeno = nextChrom.getGenotype(selectedMarkers[currentClass]);
                                 if (theGeno == nextGeno && theGeno == genos[indexIntoBlock]
                                         && thisChrom.getGenotype(preFiltBlock[q]) != 0){
                                     hapsHash.put(new Integer(preFiltBlock[q]),
                                             new Integer(thisChrom.getGenotype(preFiltBlock[q])));
                                     success = true;
                                     break;
                                 }
                             }
                         }
 
                         //either we didn't use careful counting or it didn't work due to missing data
                         if(!success){
                             if (Chromosome.getMarker(selectedMarkers[currentClass]).getMajor() ==
                                     genos[indexIntoBlock]){
                                 hapsHash.put(new Integer(preFiltBlock[q]),
                                         new Integer(Chromosome.getMarker(preFiltBlock[q]).getMajor()));
                             }else{
                                 hapsHash.put(new Integer(preFiltBlock[q]),
                                         new Integer(Chromosome.getMarker(preFiltBlock[q]).getMinor()));
                             }
                         }
                     }
                     genos = new int[preFiltBlock.length];
                     for (int q = 0; q < preFiltBlock.length; q++){
                         genos[q] = ((Integer)hapsHash.get(new Integer(preFiltBlock[q]))).intValue();
                     }
                 }
 
                 if(storeEM) {
                     tempArray[i] = new Haplotype(genos, returnedFreqs[i], preFiltBlock, theEM);
                 }else {
                     tempArray[i] = new Haplotype(genos, returnedFreqs[i], preFiltBlock, null);
                 }
                 //if we are performing association tests, then store the rawHaplotypes
                 if (Options.getAssocTest() == ASSOC_TRIO){
                     tempArray[i].setTransCount(theEM.getTransCount(i));
                     tempArray[i].setUntransCount(theEM.getUntransCount(i));
                     if(Options.getTdtType() == TDT_PAREN) {
                         tempArray[i].setDiscordantAlleleCounts(theEM.getDiscordantCounts(i));
                     }
                 }else if (Options.getAssocTest() == ASSOC_CC){
                     tempArray[i].setCaseCount(theEM.getCaseCount(i));
                     tempArray[i].setControlCount(theEM.getControlCount(i));
                 }
             }
             //make the rawHaplotypes array only large enough to hold haps
             //which pass threshold above
             rawHaplotypes[k] = new Haplotype[theEM.numHaplos()];
             for (int z = 0; z < theEM.numHaplos(); z++){
                 rawHaplotypes[k][z] = tempArray[z];
             }
         }
 
         return rawHaplotypes;
     }
 
     public double[] computeMultiDprime(Haplotype[][] haplos){
         double[] multidprimeArray = new double[haplos.length];
         for (int gap = 0; gap < haplos.length - 1; gap++){
             double[][] multilocusTable = new double[haplos[gap].length][];
             double[] rowSum = new double[haplos[gap].length];
             double[] colSum = new double[haplos[gap+1].length];
             double multilocusTotal = 0;
             for (int i = 0; i < haplos[gap].length; i++){
                 multilocusTable[i] = haplos[gap][i].getCrossovers();
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
             boolean noDivByZero = false;
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
                     if (denom != 0){
                         noDivByZero = true;
                         multidprime += (rowSum[i]/multilocusTotal)*(colSum[j]/multilocusTotal)*Math.abs(num/denom);
                     }
                 }
             }
             if (noDivByZero && multidprime <= 1.00){
                 multidprimeArray[gap] = multidprime;
             }else{
                 multidprimeArray[gap] = 1.00;
             }
         }
         return multidprimeArray;
     }
 
     public void pickTags(Haplotype[][] haplos){
         for (int i = 0; i < haplos.length; i++){
             //first clear the tags for this block
             haplos[i][0].clearTags();
 
 
             //next pick the tagSNPs
             Vector theBestSubset = getBestSubset(haplos[i]);
             for (int j = 0; j < theBestSubset.size(); j++){
                 haplos[i][0].addTag(((Integer)theBestSubset.elementAt(j)).intValue());
             }
 
             for (int k = 1; k < haplos[i].length; k++){
                 //so the tags should be a property of the block, but there's no object to represent it right now
                 //so we just make sure we copy the tags into all the haps in this block...sorry, I suck.
                 haplos[i][k].setTags(haplos[i][0].getTags());
             }
         }
     }
 
     public Haplotype[][] orderByCrossing(Haplotype[][] haplos){
         //seed first block with ordering numbers
         for (int u = 0; u < haplos[0].length; u++){
             haplos[0][u].setListOrder(u);
         }
 
         for (int gap = 0; gap < haplos.length - 1; gap++){
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
         }
         return haplos;
     }
 
     Haplotype[][] generateCrossovers(Haplotype[][] haplos) throws HaploViewException{
         Vector crossBlock = new Vector();
         double CROSSOVER_THRESHOLD = 0.001;   //to what percentage do we want to consider crossings?
 
         if (haplos.length == 0) return null;
 
         for (int gap = 0; gap < haplos.length - 1; gap++){         //compute crossovers for each inter-block gap
             Vector preGapSubset = getBestSubset(haplos[gap]);
             Vector postGapSubset = getBestSubset(haplos[gap+1]);
             int[] preMarkerID = haplos[gap][0].getMarkers();       //index haplos to markers in whole dataset
             int[] postMarkerID = haplos[gap+1][0].getMarkers();
 
             crossBlock.clear();                 //make a "block" of the markers which id the pre- and post- gap haps
             for (int i = 0; i < preGapSubset.size(); i++){
                 crossBlock.add(new Integer(preMarkerID[((Integer)preGapSubset.elementAt(i)).intValue()]));
             }
             for (int i = 0; i < postGapSubset.size(); i++){
                 crossBlock.add(new Integer(postMarkerID[((Integer)postGapSubset.elementAt(i)).intValue()]));
             }
 
             Vector inputVector = new Vector();
             int[] intArray = new int[crossBlock.size()];
             for (int i = 0; i < crossBlock.size(); i++){      //input format for hap generating routine
                 intArray[i] = ((Integer)crossBlock.elementAt(i)).intValue();
             }
             inputVector.add(intArray);
 
             Haplotype[] crossHaplos = generateHaplotypes(inputVector, true)[0];  //get haplos of gap
 
 
             for (int i = 0; i < haplos[gap].length; i++){
                 double[] crossPercentages = new double[haplos[gap+1].length];
                 StringBuffer firstHapCodeB = new StringBuffer(preGapSubset.size());
                 for (int j = 0; j < preGapSubset.size(); j++){   //make a string out of uniquely identifying genotypes for this hap
                     firstHapCodeB.append(haplos[gap][i].getGeno()[((Integer)preGapSubset.elementAt(j)).intValue()]);
                 }
                 String firstHapCode = firstHapCodeB.toString();
                 for (int gapHaplo = 0; gapHaplo < crossHaplos.length; gapHaplo++){  //look at each crossover hap
                     if (crossHaplos[gapHaplo].getPercentage() > CROSSOVER_THRESHOLD){
                         StringBuffer gapBeginHapCodeB = new StringBuffer(preGapSubset.size());
                         for (int j = 0; j < preGapSubset.size(); j++){     //make a string as above
                             gapBeginHapCodeB.append(crossHaplos[gapHaplo].getGeno()[j]);
                         }
                         String gapBeginHapCode = gapBeginHapCodeB.toString();
                         if (gapBeginHapCode.equals(firstHapCode)){    //if this crossover hap corresponds to this pregap hap
                             StringBuffer gapEndHapCodeB = new StringBuffer(preGapSubset.size());
                             for (int j = preGapSubset.size(); j < crossHaplos[gapHaplo].getGeno().length; j++){
                                 gapEndHapCodeB.append(crossHaplos[gapHaplo].getGeno()[j]);
                             }
                             String gapEndHapCode = gapEndHapCodeB.toString();
                             for (int j = 0; j < haplos[gap+1].length; j++){
                                 StringBuffer endHapCodeB = new StringBuffer();
                                 for (int k = 0; k < postGapSubset.size(); k++){
                                     endHapCodeB.append(haplos[gap+1][j].getGeno()[((Integer)postGapSubset.elementAt(k)).intValue()]);
                                 }
                                 String endHapCode = endHapCodeB.toString();
                                 if (gapEndHapCode.equals(endHapCode)){
                                     crossPercentages[j] = crossHaplos[gapHaplo].getPercentage();
                                 }
                             }
                         }
                     }
                 }
                 //thought i needed to fix these percentages, but the raw values are just as good.
                 /*		double percentageSum = 0;
                 double[] fixedCross = new double[crossPercentages.length];
                 for (int y = 0; y < crossPercentages.length; y++){
                 percentageSum += crossPercentages[y];
                 }
                 for (int y = 0; y < crossPercentages.length; y++){
                 fixedCross[y] = crossPercentages[y]/percentageSum;
                 }*/
                 haplos[gap][i].addCrossovers(crossPercentages);
             }
         }
         return haplos;
     }
 
     Vector getBestSubset(Haplotype[] thisBlock){    //from a block of haps, find marker subset which uniquely id's all haps
         Vector bestSubset = new Vector();
         //first make an array with markers ranked by genotyping success rate
         Vector genoSuccessRank = new Vector();
         Vector genoNumberRank = new Vector();
         int[] myMarkers = thisBlock[0].getMarkers();
         genoSuccessRank.add(new Double(percentBadGenotypes[Chromosome.realIndex[myMarkers[0]]]));
         genoNumberRank.add(new Integer(0));
         for (int i = 1; i < myMarkers.length; i++){
             boolean inserted = false;
             for (int j = 0; j < genoSuccessRank.size(); j++){
                 if (percentBadGenotypes[Chromosome.realIndex[myMarkers[i]]] < ((Double)(genoSuccessRank.elementAt(j))).doubleValue()){
                     genoSuccessRank.insertElementAt(new Double(percentBadGenotypes[Chromosome.realIndex[myMarkers[i]]]), j);
                     genoNumberRank.insertElementAt(new Integer(i), j);
                     inserted = true;
                     break;
                 }
             }
             if (!(inserted)) {
                 genoNumberRank.add(new Integer(i));
                 genoSuccessRank.add(new Double(percentBadGenotypes[Chromosome.realIndex[myMarkers[i]]]));
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
 
     void guessBlocks(int method){
         guessBlocks(method, blocks);
     }
 
     void guessBlocks(int method, Vector custVec){
         Vector returnVec = new Vector();
         switch(method){
             case BLOX_GABRIEL: returnVec = FindBlocks.doGabriel(dpTable); break;
             case BLOX_4GAM: returnVec = FindBlocks.do4Gamete(dpTable); break;
             case BLOX_SPINE: returnVec = FindBlocks.doSpine(dpTable); break;
             case BLOX_CUSTOM: returnVec = custVec; break;
                 //todo: bad! doesn't check if vector is out of bounds and stuff or blocks out of order
             default: returnVec = new Vector(); break;
         }
         blocks = returnVec;
         blocksChanged = true;
 
         //keep track of which markers are in a block
         isInBlock = new boolean[Chromosome.getSize()];
         for (int i = 0; i < isInBlock.length; i++){
             isInBlock[i] = false;
         }
         for (int i = 0; i < blocks.size(); i++){
             int[] markers = (int[])blocks.elementAt(i);
             for (int j = 0; j < markers.length; j++){
                 isInBlock[markers[j]] = true;
             }
         }
     }
 
     public void removeFromBlock(int markerNum) {
         if (blocks != null){
             OUTER: for (int i = 0; i < blocks.size(); i ++){
                 int thisBlock[] = (int[])blocks.elementAt(i);
                 int newBlock[] = new int[thisBlock.length-1];
                 int count = 0;
                 for (int j = 0; j < thisBlock.length; j++){
                     if(markerNum == thisBlock[j]){
                         blocksChanged = true;
                         if (newBlock.length < 1){
                             blocks.removeElementAt(i);
                             for (int k = 0; k < thisBlock.length; k++){
                                 this.isInBlock[thisBlock[k]] = false;
                             }
                             break OUTER;
                         }
                         this.isInBlock[markerNum] = false;
                         for (int k = 0; k < thisBlock.length; k++){
                             if (!(k==j)){
                                 newBlock[count] = thisBlock[k];
                                 count++;
                             }
                         }
                         blocks.setElementAt(newBlock, i);
                         break OUTER;
                     }
                 }
             }
         }
     }
 
     public void addMarkerIntoSurroundingBlock(int markerNum) {
         if (blocks != null){
             OUTER: for (int i = 0; i < blocks.size(); i ++){
                 int thisBlock[] = (int[])blocks.elementAt(i);
                 int newBlock[] = new int[thisBlock.length+1];
                 int count = 0;
                 if(markerNum > thisBlock[0] && markerNum < thisBlock[thisBlock.length-1]){
                     blocksChanged = true;
                     this.isInBlock[markerNum] = true;
                     for (int j = 0; j < thisBlock.length; j++){
                         newBlock[count] = thisBlock[j];
                         count++;
                         if (thisBlock[j] < markerNum && thisBlock[j+1] > markerNum){
                             newBlock[count] = markerNum;
                             count++;
                         }
                     }
                     blocks.setElementAt(newBlock, i);
                     break OUTER;
                 }
             }
         }
     }
 
     public void addBlock(int firstMarker, int lastMarker) {
         if (firstMarker < 0){
             firstMarker = 0;
         }
         if (lastMarker >= Chromosome.realIndex.length){
             lastMarker = Chromosome.realIndex.length-1;
         }
         if (lastMarker - firstMarker < 0){
             //something wonky going on
             return;
         }
 
         int inArray[] = new int[lastMarker-firstMarker+1];
         blocksChanged = true;
         if (blocks.size() != 0){
             boolean placed = false;
             for (int i = 0; i < blocks.size(); i++){
                 int currentBlock[] = (int[])blocks.elementAt(i);
                 //trim out any blocks that are overlapped
                 if ((lastMarker >= currentBlock[0] && firstMarker <= currentBlock[currentBlock.length-1]) ||
                         firstMarker <= currentBlock[currentBlock.length-1] && firstMarker >= currentBlock[0]){
                     for (int j = 0; j < currentBlock.length; j++){
                         isInBlock[currentBlock[j]] = false;
                     }
                     blocks.removeElementAt(i);
                     i--;
                 }
             }
             for (int i = 0; i < blocks.size(); i++){
                 int currentBlock[] = (int[])blocks.elementAt(i);
                 if (firstMarker <= currentBlock[0] && !placed){
                     blocks.insertElementAt(inArray,i);
                     placed = true;
                 }
             }
             if (!placed){
                 blocks.add(inArray);
             }
         }else{
             blocks.add(inArray);
         }
         for (int i = 0; i < inArray.length; i++){
             inArray[i] = firstMarker+i;
             this.isInBlock[firstMarker+i] = true;
         }
     }
 
     //this method computes the dPrime value for the pair of markers pos1,pos2.
     //the method assumes that the given pair are not too far apart (ie the distance
     //between them is less than maximum distance).
     //NOTE: the values of pos1,pos2 should be unfiltered marker numbers.
     public PairwiseLinkage computeDPrime(int pos1, int pos2){
         int doublehet = 0;
         int[][] twoMarkerHaplos = new int[3][3];
 
         for (int i = 0; i < twoMarkerHaplos.length; i++){
             for (int j = 0; j < twoMarkerHaplos[i].length; j++){
                 twoMarkerHaplos[i][j] = 0;
             }
         }
 
         //check for non-polymorphic markers
         if (Chromosome.getUnfilteredMarker(pos1).getMAF() == 0 || Chromosome.getUnfilteredMarker(pos2).getMAF() == 0){
             return null;
         }
 
         int[] marker1num = new int[5]; int[] marker2num = new int[5];
 
         marker1num[0]=0;
         marker1num[Chromosome.getUnfilteredMarker(pos1).getMajor()]=1;
         marker1num[Chromosome.getUnfilteredMarker(pos1).getMinor()]=2;
         marker2num[0]=0;
         marker2num[Chromosome.getUnfilteredMarker(pos2).getMajor()]=1;
         marker2num[Chromosome.getUnfilteredMarker(pos2).getMinor()]=2;
 
         byte a1,a2,b1,b2;
         //iterate through all chromosomes in dataset
         for (int i = 0; i < chromosomes.size(); i++){
             //assign alleles for each of a pair of chromosomes at a marker to four variables
 
             if(!((Chromosome)chromosomes.elementAt(i)).isHaploid()){
 
                 a1 = ((Chromosome) chromosomes.elementAt(i)).genotypes[pos1];
                 a2 = ((Chromosome) chromosomes.elementAt(i)).genotypes[pos2];
                 b1 = ((Chromosome) chromosomes.elementAt(++i)).genotypes[pos1];
                 b2 = ((Chromosome) chromosomes.elementAt(i)).genotypes[pos2];
 
                 if (a1 == 0 || a2 == 0 || b1 == 0 || b2 == 0){
                     //skip missing data
                 } else if (((a1 >= 5 || b1 >= 5) && (a2 >= 5 || b2 >= 5)) || (a1 >= 5 && !(a2 == b2)) || (a2 >= 5 && !(a1 == b1))){
                     doublehet++;
                     //find doublehets and resolved haplotypes
                 } else if (a1 >= 5 || b1 >= 5){
                     twoMarkerHaplos[1][marker2num[a2]]++;
                     twoMarkerHaplos[2][marker2num[a2]]++;
                 } else if (a2 >= 5 || b2 >= 5){
                     twoMarkerHaplos[marker1num[a1]][1]++;
                     twoMarkerHaplos[marker1num[a1]][2]++;
                 } else {
                     twoMarkerHaplos[marker1num[a1]][marker2num[a2]]++;
                     twoMarkerHaplos[marker1num[b1]][marker2num[b2]]++;
                 }
             }else {
                 //haploid (x chrom)
                 a1 = ((Chromosome) chromosomes.elementAt(i)).genotypes[pos1];
                 a2 = ((Chromosome) chromosomes.elementAt(i)).genotypes[pos2];
 
                 if(a1 != 0 && a2 != 0) {
                     twoMarkerHaplos[marker1num[a1]][marker2num[a2]]++;
                 }
 
             }
         }
         //another monomorphic marker check
         int r1, r2, c1, c2;
         r1 = twoMarkerHaplos[1][1] + twoMarkerHaplos[1][2];
         r2 = twoMarkerHaplos[2][1] + twoMarkerHaplos[2][2];
         c1 = twoMarkerHaplos[1][1] + twoMarkerHaplos[2][1];
         c2 = twoMarkerHaplos[1][2] + twoMarkerHaplos[2][2];
         if ( (r1==0 || r2==0 || c1==0 || c2==0) && doublehet == 0){
             return  new PairwiseLinkage(1,0,0,0,0,new double[0]);
         }
 
         //compute D Prime for this pair of markers.
         //return is a tab delimited string of d', lod, r^2, CI(low), CI(high)
 
         int i,count;
         //int j,k,itmp;
         int low_i = 0;
         int high_i = 0;
         double loglike, oldloglike;// meand, mean2d, sd;
         double tmp;//g,h,m,tmp,r;
         double num, denom1, denom2, denom, dprime;//, real_dprime;
         double pA1, pB1, pA2, pB2, loglike1, loglike0, rsq;
         double tmpAA, tmpAB, tmpBA, tmpBB, dpr;// tmp2AA, tmp2AB, tmp2BA, tmp2BB;
         double total_prob, sum_prob;
         double lsurface[] = new double[101];
 
         /* store arguments in externals and compute allele frequencies */
 
         known[AA]=twoMarkerHaplos[1][1];
         known[AB]=twoMarkerHaplos[1][2];
         known[BA]=twoMarkerHaplos[2][1];
         known[BB]=twoMarkerHaplos[2][2];
         unknownDH=doublehet;
         total_chroms= (int)(known[AA]+known[AB]+known[BA]+known[BB]+(2*unknownDH));
         pA1 = (known[AA]+known[AB]+unknownDH) / (double) total_chroms;
         pB1 = 1.0-pA1;
         pA2 = (known[AA]+known[BA]+unknownDH) / (double) total_chroms;
         pB2 = 1.0-pA2;
         const_prob = 0.1;
 
         /* set initial conditions */
 
         if (const_prob < 0.00) {
             probHaps[AA]=pA1*pA2;
             probHaps[AB]=pA1*pB2;
             probHaps[BA]=pB1*pA2;
             probHaps[BB]=pB1*pB2;
         } else {
             probHaps[AA]=const_prob;
             probHaps[AB]=const_prob;
             probHaps[BA]=const_prob;
             probHaps[BB]=const_prob;;
 
             /* so that the first count step will produce an
             initial estimate without inferences (this should
             be closer and therefore speedier than assuming
             they are all at equal frequency) */
 
             count_haps(0);
             estimate_p();
         }
 
         /* now we have an initial reasonable guess at p we can
         start the EM - let the fun begin */
 
         const_prob=0.0;
         count=1; loglike=-999999999.0;
 
         do {
             oldloglike=loglike;
             count_haps(count);
             loglike = (known[AA]*Math.log(probHaps[AA]) + known[AB]*Math.log(probHaps[AB]) + known[BA]*Math.log(probHaps[BA]) + known[BB]*Math.log(probHaps[BB]))/LN10 + ((double)unknownDH*Math.log(probHaps[AA]*probHaps[BB] + probHaps[AB]*probHaps[BA]))/LN10;
             if (Math.abs(loglike-oldloglike) < TOLERANCE) break;
             estimate_p();
             count++;
         } while(count < 1000);
         /* in reality I've never seen it need more than 10 or so iterations
         to converge so this is really here just to keep it from running off into eternity */
 
         loglike1 = (known[AA]*Math.log(probHaps[AA]) + known[AB]*Math.log(probHaps[AB]) + known[BA]*Math.log(probHaps[BA]) + known[BB]*Math.log(probHaps[BB]) + (double)unknownDH*Math.log(probHaps[AA]*probHaps[BB] + probHaps[AB]*probHaps[BA]))/LN10;
         loglike0 = (known[AA]*Math.log(pA1*pA2) + known[AB]*Math.log(pA1*pB2) + known[BA]*Math.log(pB1*pA2) + known[BB]*Math.log(pB1*pB2) + (double)unknownDH*Math.log(2*pA1*pA2*pB1*pB2))/LN10;
 
         num = probHaps[AA]*probHaps[BB] - probHaps[AB]*probHaps[BA];
 
         if (num < 0) {
             /* flip matrix so we get the positive D' */
             /* flip AA with AB and BA with BB */
             tmp=probHaps[AA]; probHaps[AA]=probHaps[AB]; probHaps[AB]=tmp;
             tmp=probHaps[BB]; probHaps[BB]=probHaps[BA]; probHaps[BA]=tmp;
             /* flip frequency of second allele */
             //done in this slightly asinine way because of a compiler bugz0r in the dec-alpha version of java
             //which causes it to try to parallelize the swapping operations and mis-schedules them
             pA2 = pA2 + pB2;
             pB2 = pA2 - pB2;
             pA2 = pA2 - pB2;
             //pA2=pB2;pB2=temp;
             /* flip counts in the same fashion as p's */
             tmp=numHaps[AA]; numHaps[AA]=numHaps[AB]; numHaps[AB]=tmp;
             tmp=numHaps[BB]; numHaps[BB]=numHaps[BA]; numHaps[BA]=tmp;
             /* num has now undergone a sign change */
             num = probHaps[AA]*probHaps[BB] - probHaps[AB]*probHaps[BA];
             /* flip known array for likelihood computation */
             tmp=known[AA]; known[AA]=known[AB]; known[AB]=tmp;
             tmp=known[BB]; known[BB]=known[BA]; known[BA]=tmp;
         }
 
         denom1 = (probHaps[AA]+probHaps[BA])*(probHaps[BA]+probHaps[BB]);
         denom2 = (probHaps[AA]+probHaps[AB])*(probHaps[AB]+probHaps[BB]);
         if (denom1 < denom2) { denom = denom1; }
         else { denom = denom2; }
         dprime = num/denom;
 
         /* add computation of r^2 = (D^2)/p(1-p)q(1-q) */
         rsq = num*num/(pA1*pB1*pA2*pB2);
 
         //real_dprime=dprime;
 
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
             lsurface[i] = (known[AA]*Math.log(tmpAA) + known[AB]*Math.log(tmpAB) + known[BA]*Math.log(tmpBA) + known[BB]*Math.log(tmpBB) + (double)unknownDH*Math.log(tmpAA*tmpBB + tmpAB*tmpBA))/LN10;
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
         if (high_i > 100){ high_i = 100; }
 
 
 
         double[] freqarray = {probHaps[AA], probHaps[AB], probHaps[BB], probHaps[BA]};
 
         return new PairwiseLinkage(Util.roundDouble(dprime,3), Util.roundDouble((loglike1-loglike0),2),
                 Util.roundDouble(rsq,3), ((double)low_i/100.0), ((double)high_i/100.0), freqarray);
     }
 
     public void count_haps(int em_round){
         /* only the double heterozygote [AB][AB] results in
         ambiguous reconstruction, so we'll count the obligates
         then tack on the [AB][AB] for clarity */
 
         numHaps[AA] = known[AA];
         numHaps[AB] = known[AB];
         numHaps[BA] = known[BA];
         numHaps[BB] = known[BB];
         if (em_round > 0) {
             numHaps[AA] += unknownDH* (probHaps[AA]*probHaps[BB])/((probHaps[AA]*probHaps[BB])+(probHaps[AB]*probHaps[BA]));
             numHaps[BB] += unknownDH* (probHaps[AA]*probHaps[BB])/((probHaps[AA]*probHaps[BB])+(probHaps[AB]*probHaps[BA]));
             numHaps[AB] += unknownDH* (probHaps[AB]*probHaps[BA])/((probHaps[AA]*probHaps[BB])+(probHaps[AB]*probHaps[BA]));
             numHaps[BA] += unknownDH* (probHaps[AB]*probHaps[BA])/((probHaps[AA]*probHaps[BB])+(probHaps[AB]*probHaps[BA]));
         }
     }
 
     public void estimate_p() {
         double total= numHaps[AA]+numHaps[AB]+numHaps[BA]+numHaps[BB]+(4.0*const_prob);
         probHaps[AA]=(numHaps[AA]+const_prob)/total; if (probHaps[AA] < 1e-10) probHaps[AA]=1e-10;
         probHaps[AB]=(numHaps[AB]+const_prob)/total; if (probHaps[AB] < 1e-10) probHaps[AB]=1e-10;
         probHaps[BA]=(numHaps[BA]+const_prob)/total; if (probHaps[BA] < 1e-10) probHaps[BA]=1e-10;
         probHaps[BB]=(numHaps[BB]+const_prob)/total; if (probHaps[BB] < 1e-10) probHaps[BB]=1e-10;
     }
 
     public void saveHapsToText(Haplotype[][] finishedHaplos, double[] multidprime,
                                File saveHapsFile) throws IOException{
 
         if (finishedHaplos == null) return;
 
         NumberFormat nf = NumberFormat.getInstance(Locale.US);
         nf.setGroupingUsed(false);
         nf.setMinimumFractionDigits(3);
         nf.setMaximumFractionDigits(3);
 
         //open file for saving haps text
         FileWriter saveHapsWriter = new FileWriter(saveHapsFile);
 
         //go through each block and print haplos
         for (int i = 0; i < finishedHaplos.length; i++){
             //write block header
             int[] markerNums = finishedHaplos[i][0].getMarkers();
 
             saveHapsWriter.write("BLOCK " + (i+1) + ".  MARKERS:");
             boolean[] tags = finishedHaplos[i][0].getTags();
             for (int j = 0; j < markerNums.length; j++){
                 saveHapsWriter.write(" " + (Chromosome.realIndex[markerNums[j]]+1));
                 if (Options.isShowBlockTags()){
                     if (tags[j]) saveHapsWriter.write("!");
                 }
             }
             saveHapsWriter.write("\n");
             //write haps and crossover percentages
             for (int j = 0; j < finishedHaplos[i].length; j++){
                 if((finishedHaplos[i][j].getPercentage()) >= Options.getHaplotypeDisplayThreshold()) {
                     int[] theGeno = finishedHaplos[i][j].getGeno();
                     StringBuffer theHap = new StringBuffer(theGeno.length);
                     for (int k = 0; k < theGeno.length; k++){
                         theHap.append(theGeno[k]);
                     }
                     saveHapsWriter.write(theHap.toString() + " (" + nf.format(finishedHaplos[i][j].getPercentage()) + ")");
                     if (i < finishedHaplos.length-1){
                         saveHapsWriter.write("\t|");
                         boolean writeTab = false;
                         for (int crossCount = 0; crossCount < finishedHaplos[i+1].length; crossCount++){
                             if((finishedHaplos[i+1][crossCount].getPercentage()) >= Options.getHaplotypeDisplayThreshold() ) {
                                 if (crossCount != 0 && writeTab) saveHapsWriter.write("\t");
                                 saveHapsWriter.write(nf.format(finishedHaplos[i][j].getCrossover(crossCount)));
                                 writeTab = true;
                             }
                         }
                         saveHapsWriter.write("|");
                     }
                     saveHapsWriter.write("\n");
                 }
             }
             if (i < finishedHaplos.length - 1){
                 saveHapsWriter.write("Multiallelic Dprime: " + nf.format(multidprime[i]) + "\n");
             }
 
         }
 
 
         saveHapsWriter.close();
     }
 
     public void saveDprimeToText(File dumpDprimeFile, int source, int start, int stop) throws IOException{
         FileWriter saveDprimeWriter = new FileWriter(dumpDprimeFile);
         //we use this LinkedList to store the dprime computations for a window of 5 markers
         //in either direction in the for loop farther down.
         //a tInt value is calculated for each marker which requires the dprime calculations
         //for the 5 markers before and 5 after the current marker, so we store these values while we need
         //them to avoid unnecesary recomputation.
         LinkedList savedDPrimes = new LinkedList();
         long dist;
         if (infoKnown){
             saveDprimeWriter.write("L1\tL2\tD'\tLOD\tr^2\tCIlow\tCIhi\tDist\tT-int\n");
         }else{
             saveDprimeWriter.write("L1\tL2\tD'\tLOD\tr^2\tCIlow\tCIhi\tT-int\n");
         }
 
         boolean adj = false;
         if (start == -1 && stop == -1){
             //user selected "adjacent markers" option
             start = 0; stop = Chromosome.getSize();
             adj = true;
         }
 
         if (start < 0){
             start = 0;
         }
         if (stop > Chromosome.getSize()){
             stop = Chromosome.getSize();
         }
 
         PairwiseLinkage currComp = null;
 
         //initialize the savedDPrimes linkedlist with 10 empty arrays of size 10
         PairwiseLinkage[] pwlArray = new PairwiseLinkage[10];
         savedDPrimes.add(pwlArray);
         for(int k=0;k<4;k++) {
             savedDPrimes.add(pwlArray.clone());
         }
 
         PairwiseLinkage[] tempArray;
         if(source != TABLE_TYPE) {
             //savedDPrimes is a linkedlist which stores 5 arrays at all times.
             //it temporarily stores a set of computeDPrime() results so that we do not
             //do them over and over
             //each array contains 10 PairwiseLinkage objects.
             //each array contains the PairwiseLinkage objects for a marker being compared to the 10 previous markers.
             //if marker1 is some marker number, and tempArray is one of the arrays in savedDPrimes, then:
             //      tempArray[0] = computeDPrime(marker1 - 10, marker1)
             //      tempArray[9] = computeDPrime(marker1 - 1, marker1)
             //if the value of marker1 is less than 10, then the array is filled with nulls up the first valid comparison
             //that is, if marker1 = 5, then:
             //      tempArray[0] through tempArray[4] are null
             //      tempArray[5] = computeDPrime(marker1-5,marker1)
             for(int m=1;m<6;m++) {
                 tempArray = (PairwiseLinkage[]) savedDPrimes.get(m-1);
 
                 for(int n=1;n<11;n++){
                     if( (start+m) >= Chromosome.getSize() || (start+m-n)<0) {
                         tempArray[10-n] = null;
                     }
                     else {
                         //the next line used to have Chromosome.getUnfilteredMarker(Chromosome.realIndex[])
                         //but it seems like this should do the same thing
                         long sep = Chromosome.getMarker(start+m).getPosition()
                                 - Chromosome.getMarker(start+m-n).getPosition();
                         if(Options.getMaxDistance() == 0 || sep < Options.getMaxDistance() )  {
                             tempArray[10-n] = this.computeDPrime(Chromosome.realIndex[start+m-n],Chromosome.realIndex[start+m]);
                             //tempArray[10-n] = "" + (start+m-n) + "," + (start+m) ;
                         }
                     }
                 }
             }
         }
 
 
         for (int i = start; i < stop; i++){
 
             if(source != TABLE_TYPE && i != start) {
                 //here the first element of savedDPrimes is discarded.
                 //the array of results for the marker (i+5) is then computed and added to the end of savedDPrimes
 
                 tempArray = (PairwiseLinkage[]) savedDPrimes.removeFirst();
 
                 for(int m=0;m<10;m++) {
                     tempArray[m] = null;
                 }
                 for(int n=1;n<11;n++){
                     if(i+5 < Chromosome.getSize() && (i+5-n) >=0) {
                         long sep = Chromosome.getMarker(i+5).getPosition()
                                 - Chromosome.getMarker(i+5-n).getPosition();
                         if(Options.getMaxDistance() == 0 || sep < Options.getMaxDistance() )  {
                             tempArray[10-n] = this.computeDPrime(Chromosome.realIndex[i+5-n],Chromosome.realIndex[i+5]);
                         }
                     }
                 }
                 savedDPrimes.addLast(tempArray);
             }
 
 
             for (int j = i+1; j < stop; j++){
                 if (adj){
                     if (!(i == j-1)){
                         continue;
                     }
                 }
                 if (source == TABLE_TYPE){
 
                     currComp = dpTable.getLDStats(i,j);
                 }else{
                     long sep = Chromosome.getMarker(i).getPosition()
                             - Chromosome.getMarker(j).getPosition();
                     if(Options.getMaxDistance() == 0 || Math.abs(sep) < Options.getMaxDistance() )  {
                         currComp = this.computeDPrime(Chromosome.realIndex[i],Chromosome.realIndex[j]);
                     } else {
                         currComp = null;
                     }
                 }
                 if(currComp != null) {
                     double LODSum = 0;
                     String tInt = "-";
                     if (i == j-1){
                         //these are adjacent markers so we'll put in the t-int stat
                         for (int x = 0; x < 5; x++){
                             for (int y = 1; y < 6; y++){
                                 if (i-x < 0 || i+y >= Chromosome.getSize()){
                                     continue;
                                 }
                                 tempArray = (PairwiseLinkage[]) savedDPrimes.get(y-1);
                                 PairwiseLinkage tintPair = null;
                                 if (source == TABLE_TYPE){
                                     tintPair = dpTable.getLDStats(i-x,i+y);
                                 }else{
                                     long sep = Chromosome.getUnfilteredMarker(Chromosome.realIndex[i-x]).getPosition()
                                             - Chromosome.getUnfilteredMarker(Chromosome.realIndex[i+y]).getPosition();
                                     if(Options.getMaxDistance() == 0 || Math.abs(sep) < Options.getMaxDistance() )  {
 
                                         tintPair = tempArray[9-x-y+1];
                                         /*tintPair = this.computeDPrime(Chromosome.realIndex[i-x],
                                                 Chromosome.realIndex[i+y]);
                                         if(tempArray[9-x-y+1] == null) {
                                             System.out.println("storage is null ");
                                         }
                                         if(tintPair == null) {
                                             System.out.println("tintPair is null");
                                         }
                                         if(tempArray[9-x-y+1] == tintPair ||
                                                 (tintPair != null && tempArray[9-x-y+1] != null && tintPair.getLOD() == tempArray[9-x-y+1].getLOD())) {
                                                 System.out.println("match");
                                         }
                                         else {
                                             System.out.println("dont match");
                                         }*/
                                     }
                                 }
                                 if (tintPair != null){
                                     LODSum += tintPair.getLOD();
                                 }
                             }
                         }
                         tInt = String.valueOf(Util.roundDouble(LODSum,2));
                     }
                     if (infoKnown){
                         dist = (Chromosome.getMarker(j)).getPosition() - (Chromosome.getMarker(i)).getPosition();
                         saveDprimeWriter.write(Chromosome.getMarker(i).getName() +
                                 "\t" + Chromosome.getMarker(j).getName() +
                                 "\t" + currComp.toString() + "\t" + dist + "\t" + tInt +"\n");
                     }else{
                         saveDprimeWriter.write((Chromosome.realIndex[i]+1) + "\t" + (Chromosome.realIndex[j]+1) +
                                 "\t" + currComp.toString() + "\t" + tInt + "\n");
                     }
                 }
             }
         }
         saveDprimeWriter.close();
     }
 
     public void readAnalysisTrack(InputStream inStream) throws HaploViewException, IOException{
         //clear out the vector of old values
 
         if (inStream == null){
             throw new HaploViewException("Custom analysis track file doesn't exist!");
         }
 
         XYSeries xys = new XYSeries(new Integer(analysisTracks.getSeriesCount()));
         BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
         String currentLine;
         int lineCount = 0;
         while ((currentLine = in.readLine()) != null){
             lineCount ++;
             StringTokenizer st = new StringTokenizer(currentLine);
 
             if (st.countTokens() == 1){
                 //complain if we have only one col
                 throw new HaploViewException("File error on line " + lineCount + " in the custom analysis track file");
             }else if (st.countTokens() == 0){
                 //skip blank lines
                 continue;
             }
             Double pos, val;
             try{
                 pos = new Double(st.nextToken());
                 val = new Double(st.nextToken());
             }catch (NumberFormatException nfe) {
                 throw new HaploViewException("Format error on line " + lineCount + " in the custom analysis track file");
             }
             xys.add(pos,val);
         }
         analysisTracks.addSeries(xys);
         trackExists = true;
     }
 
     public Vector readBlocks(InputStream inStream) throws HaploViewException, IOException{
         if (inStream == null){
             throw new HaploViewException("Blocks file doesn't exist!");
         }
 
         Vector cust = new Vector();
         BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
         String currentLine;
         int lineCount = 0;
         int highestYet = -1;
         while ((currentLine = in.readLine()) != null){
             lineCount ++;
             StringTokenizer st = new StringTokenizer(currentLine);
 
             if (st.countTokens() == 0){
                 //skip blank lines
                 continue;
             }
             try{
                 Vector goodies = new Vector();
                 while (st.hasMoreTokens()){
                     Integer nextInLine = new Integer(st.nextToken());
                     for (int y = 0; y < Chromosome.realIndex.length; y++){
                         //we only keep markers from the input file that are "good" from checkdata
                         //we also realign the input file to the current "good" subset since input file is
                         //indexed of all possible markers in the dataset
                         if (Chromosome.realIndex[y] == nextInLine.intValue() - 1){
                             goodies.add(new Integer(y));
                         }
                     }
                 }
                 int thisBlock[] = new int[goodies.size()];
                 for (int x = 0; x < goodies.size(); x++){
                     thisBlock[x] = ((Integer)goodies.elementAt(x)).intValue();
                     if (thisBlock[x] > Chromosome.getUnfilteredSize() || thisBlock[x] < 0){
                         throw new HaploViewException("Error, marker in block out of bounds: " + thisBlock[x] +
                                 "\non line " + lineCount);
                     }
                     if (thisBlock[x] <= highestYet){
                         throw new HaploViewException("Error, markers/blocks out of order or overlap:\n" +
                                 "on line " + lineCount);
                     }
                     highestYet = thisBlock[x];
                 }
                 if (thisBlock.length > 0){
                     cust.add(thisBlock);
                 }
             }catch (NumberFormatException nfe) {
                 throw new HaploViewException("Format error on line " + lineCount + " in the blocks file");
             }
         }
         return cust;
     }
 
     public Haplotype[][] getHaplotypes() {
         return haplotypes;
     }
 
     public Vector getChromosomes() {
         return chromosomes;
     }
 
     public Haplotype[][] getRawHaplotypes() {
         return rawHaplotypes;
     }
 
     public class RSquared {
         private double[] rsquareds;
         private double[] conditionalProbs;
 
         public RSquared(double[] rsquareds, double[] conditionalProbs) {
             this.rsquareds = rsquareds;
             this.conditionalProbs = conditionalProbs;
         }
 
         public double[] getRsquareds() {
             return rsquareds;
         }
 
         public double[] getConditionalProbs() {
             return conditionalProbs;
         }
     }
 
     public RSquared getPhasedRSquared(int snp, int[] block){
 
         double rsquareds[] = null;
         double conditionalProbs[] = null;
         double alleleCounts[][] = null;
         int maxIndex =0;
         int[] multiMarkerHaplos = null;
         boolean monomorphic = false;
 
         if(block.length == 2){
             multiMarkerHaplos = new int[8];
 
             int pos1 = snp;
             int pos2 = block[0];
             int pos3 = block[1];
 
             int[] marker1num = new int[5]; int[] marker2num = new int[5]; int[] marker3num = new int[5];
 
             marker1num[Chromosome.getMarker(pos1).getMajor()]=0;
             marker1num[Chromosome.getMarker(pos1).getMinor()]=4;
             marker2num[Chromosome.getMarker(pos2).getMajor()]=0;
             marker2num[Chromosome.getMarker(pos2).getMinor()]=2;
             marker3num[Chromosome.getMarker(pos3).getMajor()]=0;
             marker3num[Chromosome.getMarker(pos3).getMinor()]=1;
 
             alleleCounts = new double[3][5];
 
             byte a1,a2,a3,b1,b2,b3;
             //iterate through all chromosomes in dataset
             for (int i = 0; i < chromosomes.size(); i++){
 
                 if(!((Chromosome)chromosomes.elementAt(i)).isHaploid()){
 
                     a1 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos1);
                     a2 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos2);
                     a3 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos3);
                     b1 = ((Chromosome) chromosomes.elementAt(++i)).getGenotype(pos1);
                     b2 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos2);
                     b3 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos3);
 
                     multiMarkerHaplos[marker1num[a1] + marker2num[a2] + marker3num[a3]]++;
                     multiMarkerHaplos[marker1num[b1] + marker2num[b2] + marker3num[b3]]++;
                     alleleCounts[0][a1]++;
                     alleleCounts[0][b1]++;
                     alleleCounts[1][a2]++;
                     alleleCounts[1][b2]++;
                     alleleCounts[2][a3]++;
                     alleleCounts[2][b3]++;
                 }else {
                     //haploid
                     a1 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos1);
                     a2 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos2);
                     a3 =  ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos3);
 
                     multiMarkerHaplos[marker1num[a1] +  marker2num[a2] + marker3num[a3]]++;
                 }
             }
             //check for any monomorphic SNPs
             if(alleleCounts[0][Chromosome.getMarker(pos1).getMajor()] == 0 || alleleCounts[0][Chromosome.getMarker(pos1).getMinor()] == 0
                     || alleleCounts[1][Chromosome.getMarker(pos2).getMajor()] == 0 || alleleCounts[1][Chromosome.getMarker(pos2).getMinor()] == 0
                     || alleleCounts[2][Chromosome.getMarker(pos3).getMajor()] == 0 || alleleCounts[2][Chromosome.getMarker(pos3).getMinor()] == 0){
                 monomorphic = true;
             }
             maxIndex = 4;
         }else if (block.length == 3){
             multiMarkerHaplos = new int[16];
 
               int pos1 = snp;
             int pos2 = block[0];
             int pos3 = block[1];
             int pos4 = block[2];
 
             int[] marker1num = new int[5];
             int[] marker2num = new int[5];
             int[] marker3num = new int[5];
             int[] marker4num = new int[5];
 
             marker1num[Chromosome.getMarker(pos1).getMinor()]=8;
             marker2num[Chromosome.getMarker(pos2).getMinor()]=4;
             marker3num[Chromosome.getMarker(pos3).getMinor()]=2;
             marker4num[Chromosome.getMarker(pos4).getMinor()]=1;
 
             alleleCounts = new double[4][5];
 
             byte a1,a2,a3,a4,b1,b2,b3,b4;
             //iterate through all chromosomes in dataset
             for (int i = 0; i < chromosomes.size(); i++){
 
                 if(!((Chromosome)chromosomes.elementAt(i)).isHaploid()){
 
                     a1 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos1);
                     a2 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos2);
                     a3 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos3);
                     a4 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos4);
                     b1 = ((Chromosome) chromosomes.elementAt(++i)).getGenotype(pos1);
                     b2 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos2);
                     b3 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos3);
                     b4 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos4);
 
                     multiMarkerHaplos[marker1num[a1] + marker2num[a2] + marker3num[a3] + marker4num[a4]]++;
                     multiMarkerHaplos[marker1num[b1] + marker2num[b2] + marker3num[b3] + marker4num[b4]]++;
 
                     alleleCounts[0][a1]++;
                     alleleCounts[0][b1]++;
                     alleleCounts[1][a2]++;
                     alleleCounts[1][b2]++;
                     alleleCounts[2][a3]++;
                     alleleCounts[2][b3]++;
                     alleleCounts[3][a4]++;
                     alleleCounts[3][b4]++;
                 }else {
                     //haploid
                     a1 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos1);
                     a2 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos2);
                     a3 =  ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos3);
                     a4 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos4);
 
                     multiMarkerHaplos[marker1num[a1] +  marker2num[a2] + marker3num[a3] + marker4num[a4]]++;
                 }
             }
             if(alleleCounts[0][Chromosome.getMarker(pos1).getMajor()] == 0 || alleleCounts[0][Chromosome.getMarker(pos1).getMinor()] == 0
                     || alleleCounts[1][Chromosome.getMarker(pos2).getMajor()] == 0 || alleleCounts[1][Chromosome.getMarker(pos2).getMinor()] == 0
                     || alleleCounts[2][Chromosome.getMarker(pos3).getMajor()] == 0 || alleleCounts[2][Chromosome.getMarker(pos3).getMinor()] == 0
                     || alleleCounts[3][Chromosome.getMarker(pos4).getMajor()] == 0 || alleleCounts[3][Chromosome.getMarker(pos4).getMinor()] == 0){
                 monomorphic = true;
             }
             maxIndex =8;
         }
         //the rest of the code is the same for 2 and 3 marker blocks
         rsquareds = new double[maxIndex];
         conditionalProbs = new double[maxIndex];
 
         if(monomorphic){
             Arrays.fill(rsquareds,0);
             Arrays.fill(conditionalProbs,0);
             return new RSquared(rsquareds,conditionalProbs);
         }
 
         int totalChroms=0;
 
         for(int i = 0;i < multiMarkerHaplos.length;i++){
             totalChroms += multiMarkerHaplos[i];
         }
 
         double[] freqs = new double[multiMarkerHaplos.length];
         for(int i=0;i<freqs.length;i++){
             freqs[i] = multiMarkerHaplos[i]/(double)totalChroms;
         }
 
         double p=0;
         for(int i=0;i< maxIndex; i++){
             p += freqs[i];
         }
 
         for(int i =0;i< maxIndex;i++){
             //calculate r^2
             double aa = freqs[i];
             double ab = p - freqs[i];
             double ba = freqs[i+maxIndex];
             double bb = (1-p) - freqs[i+maxIndex];
 
             double q = ba + aa;
             double c = aa*bb - ab*ba;
             rsquareds[i] = Util.roundDouble((c*c)/(p*(1-p)*q*(1-q)),3);
 
             //calculate conditional prob (ie P(snp | hap))
             conditionalProbs[i] = freqs[i]/(freqs[i] + freqs[i+maxIndex]);
         }
 
         return new RSquared(rsquareds,conditionalProbs);
     }
 
 
     public static boolean isPhasedData() {
         return phasedData;
     }
 
     public static void setPhasedData(boolean phasedData) {
         HaploData.phasedData = phasedData;
     }
 }
