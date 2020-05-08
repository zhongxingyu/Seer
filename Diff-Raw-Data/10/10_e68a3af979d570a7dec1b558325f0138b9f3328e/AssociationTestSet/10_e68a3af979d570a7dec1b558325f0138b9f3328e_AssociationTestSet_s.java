 package edu.mit.wi.haploview.association;
 
 import edu.mit.wi.haploview.*;
 import edu.mit.wi.pedfile.PedFile;
 import edu.mit.wi.pedfile.Individual;
 import edu.mit.wi.pedfile.Family;
 import edu.mit.wi.pedfile.PedFileException;
 
 import java.util.*;
 import java.io.*;
 
 public class AssociationTestSet implements Constants{
 
     private Vector tests;
     private Vector results;
     private HashSet whitelist;
     private Vector filterAlleles;
     private boolean permTests = false;
 
     public AssociationTestSet(){
         results = new Vector();
         whitelist = new HashSet();
     }
 
     public AssociationTestSet(PedFile pf, Vector permute, Vector snpsToBeTested) throws PedFileException{
         whitelist = new HashSet();
 
         if (Options.getAssocTest() == ASSOC_TRIO){
             if(Options.getTdtType() == TDT_STD) {
                 buildTrioSet(pf, permute, new TreeSet(snpsToBeTested));
             }else if(Options.getTdtType() == TDT_PAREN) {
                 buildParenTDTTrioSet(pf,permute,new TreeSet(snpsToBeTested));
             }
         }else if (Options.getAssocTest() == ASSOC_CC){
             buildCCSet(pf, permute, new TreeSet(snpsToBeTested));
         }
     }
 
     private void buildCCSet(PedFile pf, Vector affectedStatus, TreeSet snpsToBeTested){
         ArrayList results = new ArrayList();
 
         int numMarkers = Chromosome.getUnfilteredSize();
 
         Vector indList = pf.getUnrelatedIndividuals();
         int numInds = indList.size();
 
         if(affectedStatus == null || affectedStatus.size() != indList.size()) {
             affectedStatus = new Vector(indList.size());
             for(int i=0;i<indList.size();i++) {
                 Individual tempInd = ((Individual)indList.get(i));
                 affectedStatus.add(new Integer(tempInd.getAffectedStatus()));
             }
         }
 
 
         boolean[] useable = new boolean[indList.size()];
         Arrays.fill(useable, false);
 
         //this loop determines who is eligible to be used for the case/control association test
         for(int i=0;i<useable.length;i++) {
 
             Individual tempInd = ((Individual)indList.get(i));
             Family tempFam = pf.getFamily(tempInd.getFamilyID());
 
             //need to check to make sure we don't include both parents and kids of trios
             //so, we only set useable[i] to true if Individual at index i is not the child of a trio in the indList
             if (!(tempFam.containsMember(tempInd.getMomID()) &&
                     tempFam.containsMember(tempInd.getDadID()))){
                 useable[i] = true;
             } else{
                 try{
                     if (!(indList.contains(tempFam.getMember(tempInd.getMomID())) ||
                             indList.contains(tempFam.getMember(tempInd.getDadID())))){
                         useable[i] = true;
                     }
                 }catch (PedFileException pfe){
                 }
             }
         }
 
         for (int i = 0; i < numMarkers; i++){
             SNP currentMarker = Chromosome.getUnfilteredMarker(i);
             if (snpsToBeTested.contains(currentMarker)){
                 byte allele1 = 0, allele2 = 0;
                 int[][] counts = new int[2][2];
                 Individual currentInd;
                 for (int j = 0; j < numInds; j++){
                     if(useable[j]) {
                         currentInd = (Individual)indList.get(j);
                         int cc = ((Integer)affectedStatus.get(j)).intValue();
 
                         if (cc == 0) continue;
                         if (cc == 2) cc = 0;
                         byte a1 = currentInd.getMarkerA(i);
                         byte a2 = currentInd.getMarkerB(i);
 
                         if (a1 >= 5 && a2 >= 5){
                             counts[cc][0]++;
                             counts[cc][1]++;
                             if (allele1 == 0){
                                 allele1 = (byte)(a1 - 4);
                                 allele2 = (byte)(a2 - 4);
                             }
                         }else{
                             //seed the alleles as soon as they're found
                             if (allele1 == 0){
                                 allele1 = a1;
                                 if (a1 != a2){
                                     allele2 = a2;
                                 }
                             }else if (allele2 == 0){
                                 if (a1 != allele1){
                                     allele2 = a1;
                                 }else if (a2 != allele1){
                                     allele2 = a2;
                                 }
                             }
 
                             if (a1 != 0){
                                 if (a1 == allele1){
                                     counts[cc][0] ++;
                                 }else{
                                     counts[cc][1] ++;
                                 }
                             }
                             if (a2 != 0){
                                 if (a2 == allele1){
                                     counts[cc][0]++;
                                 }else{
                                     counts[cc][1]++;
                                 }
                             }
                         }
                     }
                 }
                 int[] g1 = {allele1};
                 int[] g2 = {allele2};
                 int[] m  = {i};
 
                 Haplotype thisSNP1 = new Haplotype(g1, 0, m, null);
                 thisSNP1.setCaseCount(counts[0][0]);
                 thisSNP1.setControlCount(counts[1][0]);
                 Haplotype thisSNP2 = new Haplotype(g2, 0, m, null);
                 thisSNP2.setCaseCount(counts[0][1]);
                 thisSNP2.setControlCount(counts[1][1]);
 
 
                 Haplotype[] daBlock = {thisSNP1, thisSNP2};
                 results.add(new MarkerAssociationResult(daBlock, currentMarker.getName(), currentMarker));
             }
         }
 
         this.results = new Vector(results);
     }
 
     private void buildTrioSet(PedFile pf, Vector permuteInd, TreeSet snpsToBeTested) throws PedFileException{
         Vector results = new Vector();
 
         Vector indList = pf.getAllIndividuals();
 
         if(permuteInd == null || permuteInd.size() != indList.size()) {
             permuteInd = new Vector();
             for (int i = 0; i < indList.size(); i++){
                 permuteInd.add(new Boolean(false));
             }
         }
 
         int numMarkers = Chromosome.getUnfilteredSize();
         for (int i = 0; i < numMarkers; i++){
             SNP currentMarker = Chromosome.getUnfilteredMarker(i);
             if (snpsToBeTested.contains(currentMarker)){
                 Individual currentInd;
                 Family currentFam;
                 AssociationResult.TallyTrio tt = new AssociationResult.TallyTrio();
                 for (int j = 0; j < indList.size(); j++){
                     currentInd = (Individual)indList.elementAt(j);
                     currentFam = pf.getFamily(currentInd.getFamilyID());
                     if (currentFam.containsMember(currentInd.getMomID()) &&
                             currentFam.containsMember(currentInd.getDadID()) &&
                             currentInd.getAffectedStatus() == 2){
                         //if he has both parents, and is affected, we can get a transmission
                         Individual mom = currentFam.getMember(currentInd.getMomID());
                         Individual dad = currentFam.getMember(currentInd.getDadID());
                          if(currentInd.getZeroed(i) || dad.getZeroed(i) || mom.getZeroed(i)) {
                             continue;
                         }
                         byte kid1 = currentInd.getMarkerA(i);
                         byte kid2 = currentInd.getMarkerB(i);
                         byte dad1 = dad.getMarkerA(i);
                         byte dad2 = dad.getMarkerB(i);
                         byte mom1 = mom.getMarkerA(i);
                         byte mom2 = mom.getMarkerB(i);
                         byte momT=0, momU=0, dadT=0, dadU=0;
                         if (kid1 == 0 || kid2 == 0 || dad1 == 0 || dad2 == 0 || mom1 == 0 || mom2 == 0) {
                             continue;
                         } else if (kid1 == kid2) {
                             //kid homozygous
                             if (dad1 == kid1) {
                                 dadT = dad1;
                                 dadU = dad2;
                             } else {
                                 dadT = dad2;
                                 dadU = dad1;
                             }
 
                             if (mom1 == kid1) {
                                 momT = mom1;
                                 momU = mom2;
                             } else {
                                 momT = mom2;
                                 momU = mom1;
                             }
                         } else {
                             if (dad1 == dad2 && mom1 != mom2) {
                                 //dad hom mom het
                                 dadT = dad1;
                                 dadU = dad2;
                                 if (kid1 == dad1) {
                                     momT = kid2;
                                     momU = kid1;
                                 } else {
                                     momT = kid1;
                                     momU = kid2;
                                 }
                             } else if (mom1 == mom2 && dad1 != dad2) {
                                 //dad het mom hom
                                 momT = mom1;
                                 momU = mom2;
                                 if (kid1 == mom1) {
                                     dadT = kid2;
                                     dadU = kid1;
                                 } else {
                                     dadT = kid1;
                                     dadU = kid2;
                                 }
                             } else if (dad1 == dad2 && mom1 == mom2) {
                                 //mom & dad hom
                                 dadT = dad1;
                                 dadU = dad1;
                                 momT = mom1;
                                 momU = mom1;
                             } else {
                                 //everybody het
                                 dadT = (byte)(4+dad1);
                                 dadU = (byte)(4+dad2);
                                 momT = (byte)(4+mom1);
                                 momU = (byte)(4+mom2);
                             }
                         }
                         if(((Boolean)permuteInd.get(j)).booleanValue()) {
                             tt.tallyTrioInd(dadU, dadT);
                             tt.tallyTrioInd(momU, momT);
                         } else {
                             tt.tallyTrioInd(dadT, dadU);
                             tt.tallyTrioInd(momT, momU);
                         }
                     }
                 }
                 int[] g1 = {tt.allele1};
                 int[] g2 = {tt.allele2};
                 int[] m  = {i};
 
                 Haplotype thisSNP1 = new Haplotype(g1, 0, m, null);
                 thisSNP1.setTransCount(tt.counts[0][0]);
                 thisSNP1.setUntransCount(tt.counts[1][0]);
                 Haplotype thisSNP2 = new Haplotype(g2, 0, m, null);
                 thisSNP2.setTransCount(tt.counts[0][1]);
                 thisSNP2.setUntransCount(tt.counts[1][1]);
 
                 Haplotype[] daBlock = {thisSNP1, thisSNP2};
                 results.add(new MarkerAssociationResult(daBlock, currentMarker.getName(), currentMarker));
             }
         }
         this.results = results;
     }
 
     private void buildParenTDTTrioSet(PedFile pf, Vector permuteInd, TreeSet snpsToBeTested) throws PedFileException{
         Vector results = new Vector();
 
         Vector indList = pf.getAllIndividuals();
 
         if(permuteInd == null || permuteInd.size() != indList.size()) {
             permuteInd = new Vector();
             for (int i = 0; i < indList.size(); i++){
                 permuteInd.add(new Boolean(false));
             }
         }
 
         //todo: need to make sure each set of parents only used once
         int numMarkers = Chromosome.getUnfilteredSize();
         for (int i = 0; i < numMarkers; i++){
             SNP currentMarker = Chromosome.getUnfilteredMarker(i);
             if (snpsToBeTested.contains(currentMarker)){
                 int discordantNotTallied=0;
                 int discordantTallied = 0;
                 Individual currentInd;
                 Family currentFam;
                 HashSet usedParents = new HashSet();
 
                 AssociationResult.TallyTrio tt = new AssociationResult.TallyTrio();
                 for (int j = 0; j < indList.size(); j++){
                     currentInd = (Individual)indList.elementAt(j);
                     currentFam = pf.getFamily(currentInd.getFamilyID());
                     if (currentFam.containsMember(currentInd.getMomID()) &&
                             currentFam.containsMember(currentInd.getDadID()) &&
                             currentInd.getAffectedStatus() == 2){
                         //if he has both parents, and is affected, we can get a transmission
                         Individual mom = currentFam.getMember(currentInd.getMomID());
                         Individual dad = currentFam.getMember(currentInd.getDadID());
                        if(usedParents.contains(mom) || usedParents.contains(dad)) {
                            continue;
                        }
                          if(currentInd.getZeroed(i) || dad.getZeroed(i) || mom.getZeroed(i)) {
                             continue;
                         }
                         byte kid1 = currentInd.getMarkerA(i);
                         byte kid2 = currentInd.getMarkerB(i);
                         byte dad1 = dad.getMarkerA(i);
                         byte dad2 = dad.getMarkerB(i);
                         byte mom1 = mom.getMarkerA(i);
                         byte mom2 = mom.getMarkerB(i);
                         byte momT=0, momU=0, dadT=0, dadU=0;
                         if (kid1 == 0 || kid2 == 0 || dad1 == 0 || dad2 == 0 || mom1 == 0 || mom2 == 0) {
                             continue;
                         } else if (kid1 == kid2) {
                             //kid homozygous
                             if (dad1 == kid1) {
                                 dadT = dad1;
                                 dadU = dad2;
                             } else {
                                 dadT = dad2;
                                 dadU = dad1;
                             }
 
                             if (mom1 == kid1) {
                                 momT = mom1;
                                 momU = mom2;
                             } else {
                                 momT = mom2;
                                 momU = mom1;
                             }
                         } else {
                             if (dad1 == dad2 && mom1 != mom2) {
                                 //dad hom mom het
                                 dadT = dad1;
                                 dadU = dad2;
                                 if (kid1 == dad1) {
                                     momT = kid2;
                                     momU = kid1;
                                 } else {
                                     momT = kid1;
                                     momU = kid2;
                                 }
                             } else if (mom1 == mom2 && dad1 != dad2) {
                                 //dad het mom hom
                                 momT = mom1;
                                 momU = mom2;
                                 if (kid1 == mom1) {
                                     dadT = kid2;
                                     dadU = kid1;
                                 } else {
                                     dadT = kid1;
                                     dadU = kid2;
                                 }
                             } else if (dad1 == dad2 && mom1 == mom2) {
                                 //mom & dad hom
                                 dadT = dad1;
                                 dadU = dad1;
                                 momT = mom1;
                                 momU = mom1;
                             } else {
                                 //everybody het
                                 dadT = (byte)(4+dad1);
                                 dadU = (byte)(4+dad2);
                                 momT = (byte)(4+mom1);
                                 momU = (byte)(4+mom2);
                             }
                         }
 
                         if(((Boolean)permuteInd.get(j)).booleanValue()) {
                             tt.tallyTrioInd(dadU, dadT);
                             tt.tallyTrioInd(momU, momT);
                         } else {
                             tt.tallyTrioInd(dadT, dadU);
                             tt.tallyTrioInd(momT, momU);
                         }
                         if(mom.getAffectedStatus() != dad.getAffectedStatus()) {
                             //discordant parental phenotypes
                             if(!(dad1 == mom1 && dad2 == mom2) && !(dad1 == mom2 && dad2 == mom1)) {
                                 if(mom.getAffectedStatus() == 2) {
                                     tt.tallyDiscordantParents(momT,momU,dadT,dadU);
 
                                 } else if(dad.getAffectedStatus() == 2) {
                                     tt.tallyDiscordantParents(dadT,dadU,momT,momU);
                                 }
                                 discordantTallied++;
                             }else {
                                 discordantNotTallied++;
                             }
                         }
                         usedParents.add(mom);
                         usedParents.add(dad);
 
                     }
                 }
                 int[] g1 = {tt.allele1};
                 int[] g2 = {tt.allele2};
                 int[] m  = {i};
 
                 Haplotype thisSNP1 = new Haplotype(g1, 0, m, null);
                 thisSNP1.setTransCount(tt.counts[0][0]);
                 thisSNP1.setUntransCount(tt.counts[1][0]);
                 thisSNP1.setDiscordantAlleleCounts(tt.discordantAlleleCounts);
                 Haplotype thisSNP2 = new Haplotype(g2, 0, m, null);
                 thisSNP2.setTransCount(tt.counts[0][1]);
                 thisSNP2.setUntransCount(tt.counts[1][1]);
                 thisSNP2.setDiscordantAlleleCounts(tt.getDiscordantCountsAllele2());
 
                 Haplotype[] daBlock = {thisSNP1, thisSNP2};
                 results.add(new MarkerAssociationResult(daBlock, currentMarker.getName(), currentMarker));
 
             }
         }
         this.results = results;
     }
 
     public AssociationTestSet(Haplotype[][] haplos, Vector names){
         //use this constructor for default hap tests so you can filter by display freq
         whitelist = new HashSet();
         Vector results = new Vector();
         if (haplos != null){
             for (int i = 0; i < haplos.length; i++){
                 String blockname;
                 if (names == null){
                     blockname = "Block " + (i+1);
                 }else{
                     blockname = (String) names.get(i);
                 }
                 results.add(new HaplotypeAssociationResult(haplos[i], Options.getHaplotypeDisplayThreshold(),blockname));
             }
         }
         this.results = results;
     }
 
     public AssociationTestSet(Haplotype[][] haplos, Vector names, Vector alleles) throws HaploViewException{
         //use this constructor for custom tests so you can filter on alleles
         whitelist = new HashSet();
         this.filterAlleles = alleles;
         Vector results = new Vector();
         if (haplos != null){
             boolean missing = false;
             Vector missingAlleles =new Vector();
             for (int i = 0; i < haplos.length; i++){
                 try {
 
                     String blockname;
                     if (names == null){
                         blockname = "Block " + (i+1);
                     }else{
                         blockname = (String) names.get(i);
                     }
                     results.add(new HaplotypeAssociationResult(haplos[i], (String)alleles.get(i),blockname));
                 }catch(HaploViewException hve) {
                     missing = true;
                     missingAlleles.add((String)names.get(i) + "\t " + (String)alleles.get(i));
 
                 }
             }
 
             if(missing) {
                 for(int i=0;i<missingAlleles.size();i++) {
                     System.out.println(missingAlleles.get(i));
                 }
                 throw new HaploViewException("alleles missing");
             }
         }
         this.results = results;
     }
 
     public AssociationTestSet(String fileName) throws IOException, HaploViewException{
         tests = new Vector();
         whitelist = new HashSet();
         File testListFile = new File(fileName);
         BufferedReader in = new BufferedReader(new FileReader(testListFile));
 
         String currentLine;
         int lineCount = 0;
 
         //we need to be able to identify marker index by name
         Hashtable indicesByName = new Hashtable();
         Iterator mitr = Chromosome.getAllMarkers().iterator();
         int count = 0;
         while (mitr.hasNext()){
             SNP n = (SNP) mitr.next();
             indicesByName.put(n.getName(),new Integer(count));
             count++;
         }
 
         while ((currentLine = in.readLine()) != null){
             lineCount++;
             //first determine if a tab specifies a specific allele for a multi-marker test
             StringTokenizer st = new StringTokenizer(currentLine, "\t");
             String markerNames;
             String alleles;
             if (st.countTokens() == 0){
                 //skip blank lines
                 continue;
             }else if (st.countTokens() == 1){
                 //this is just a list of markers
                 markerNames = st.nextToken();
                 alleles = null;
             }else if (st.countTokens() == 2){
                 //this has markers and alleles
                 markerNames = st.nextToken();
                 alleles = st.nextToken();
             }else{
                 //this is *!&#ed up
                 markerNames = null;
                 alleles = null;
                 throw new HaploViewException("Format error on line " + lineCount + " of tests file.");
             }
 
             StringTokenizer mst = new StringTokenizer(markerNames, ", ");
             AssociationTest t = new AssociationTest();
             while (mst.hasMoreTokens()) {
                 String currentToken = mst.nextToken();
                 if (!indicesByName.containsKey(currentToken)){
                     throw new HaploViewException("I don't know anything about marker " + currentToken +
                             " in custom tests file.");
                 }
 
                 Integer nt = (Integer) indicesByName.get(currentToken);
                 t.addMarker(nt);
             }
             tests.add(t);
 
             if (alleles != null){
                 //we have alleles
                 StringBuffer asb = new StringBuffer();
                 StringTokenizer ast = new StringTokenizer(alleles, ", ");
                 if (ast.countTokens() != t.getNumMarkers()){
                     throw new HaploViewException("Allele and marker name mismatch on line " + lineCount + " of tests file.");
                 }
                 while (ast.hasMoreTokens()){
                     asb.append(ast.nextToken());
                 }
                 t.specifyAllele(asb.toString());
             }
         }
 
     }
 
     public void runFileTests(HaploData theData, Vector inputSNPResults) throws HaploViewException {
         Vector res = new Vector();
         if(tests == null || theData == null) {
             return;
         }
 
         Vector blocks = new Vector();
         Vector names = new Vector();
         Vector alleles = new Vector();
         Hashtable blockHash = new Hashtable();
         int multiMarkerTestcount =0;
 
         for(int i=0;i<tests.size();i++) {
             //first go through and get all the multimarker tests to package up to hand to theData.generateHaplotypes()
             AssociationTest currentTest = (AssociationTest) tests.get(i);
             if(currentTest.getNumMarkers() > 1) {
                 names.add(currentTest.getName());
                 alleles.add(currentTest.getAllele());
                 if(!blockHash.containsKey(currentTest)){
                     blocks.add(currentTest.getFilteredMarkerArray());
 
                     blockHash.put(currentTest,new Integer(blocks.size()-1));
                 }
                 multiMarkerTestcount++;
 
             }
         }
 
         this.filterAlleles = alleles;
         System.out.println("blocks.size() = " + blocks.size());
         Haplotype[][] blockHaps = theData.generateHaplotypes(blocks, permTests);
 
         System.out.println("blockHaps.length = " + blockHaps.length);
         Haplotype[][] realBlockHaps = new Haplotype[multiMarkerTestcount][];
         int multiMarkerCountTemp=0;
         for(int i=0;i<tests.size();i++) {
             AssociationTest currentTest = (AssociationTest) tests.get(i);
             if(currentTest.getNumMarkers() > 1) {
                 realBlockHaps[multiMarkerCountTemp] = blockHaps[((Integer)blockHash.get(currentTest)).intValue()];
                 multiMarkerCountTemp++;
             }
         }
 
 
         Vector blockResults = new AssociationTestSet(realBlockHaps, names, alleles).getResults();
         Iterator britr = blockResults.iterator();
 
         for (int i = 0; i < tests.size(); i++){
             AssociationTest currentTest = (AssociationTest) tests.get(i);
             if(currentTest.getNumMarkers() > 1) {
                 //grab the next block result from above
                 HaplotypeAssociationResult har = (HaplotypeAssociationResult) britr.next();
                 res.add(har);
             }else if (currentTest.getNumMarkers() == 1){
                 //grab appropriate single marker result.
                 res.add(inputSNPResults.get(currentTest.getMarkerArray()[0]));
             }
         }
 
         results =  res;
     }
 
     public Vector getResults() {
         return results;
     }
 
     public Vector getFilteredResults(){
         //return the results but without any single snps which are filtered out.
         Vector filt = new Vector();
 
         TreeMap unFilteredMarkers = new TreeMap();
         for (int i = 0; i < Chromosome.getSize(); i++){
             unFilteredMarkers.put(Chromosome.getMarker(i), null);
         }
 
         Iterator itr = results.iterator();
         while (itr.hasNext()){
             Object o = itr.next();
             if (o instanceof HaplotypeAssociationResult){
                 filt.add(o);
             }else{
                 if (unFilteredMarkers.containsKey(((MarkerAssociationResult)o).getSnp())){
                     //only add it if it's not filtered.
                     filt.add(o);
                 }
             }
         }
 
         return filt;
     }
 
     public Vector getMarkerAssociationResults(){
         Vector ret = new Vector();
 
         Iterator itr = results.iterator();
         while (itr.hasNext()){
             Object o = itr.next();
             if (o instanceof MarkerAssociationResult){
                 ret.add(o);
             }
         }
 
         return ret;
     }
 
     public Vector getHaplotypeAssociationResults(){
         Vector ret = new Vector();
 
         Iterator itr = results.iterator();
         while (itr.hasNext()){
             Object o = itr.next();
             if (o instanceof HaplotypeAssociationResult){
                 ret.add(o);
             }
         }
 
         return ret;
     }
 
     public Vector getFilterAlleles() {
         return filterAlleles;
     }
 
     public HashSet getWhitelist() {
         return whitelist;
     }
 
     public void cat(AssociationTestSet ats){
         results.addAll(ats.getResults());
     }
 
     public boolean isCustom() {
         return tests != null;
     }
 
     class AssociationTest {
         Vector markers;
         String allele;
 
         public AssociationTest() {
             markers = new Vector();
         }
 
         void addMarker(Integer m) {
             if(m != null) {
                 markers.add(m);
             }
             whitelist.add(Chromosome.getUnfilteredMarker(m.intValue()));
         }
 
         int getNumMarkers() {
             return markers.size();
         }
 
         int[] getMarkerArray() {
             int[] tempArray = new int[markers.size()];
 
             for(int i =0; i<tempArray.length;i++) {
                 tempArray[i] = ((Integer)markers.get(i)).intValue();
             }
             return tempArray;
         }
 
         int[] getFilteredMarkerArray(){
             //it is important that none of the markers in any of the cust blocks have been filtered or else this
             //method chokes.
             int[] tempArray = new int[markers.size()];
             for(int i = 0; i < tempArray.length; i++){
                 tempArray[i] = Chromosome.filterIndex[((Integer)markers.get(i)).intValue()];
             }
             return tempArray;
         }
 
         String getName(){
             StringBuffer sb = new StringBuffer();
             for (int i = 0; i < markers.size() - 1; i++){
                 sb.append(Chromosome.getUnfilteredMarker(((Integer)markers.get(i)).intValue()).getName());
                 sb.append(",");
             }
             sb.append(Chromosome.getUnfilteredMarker(((Integer)markers.get(markers.size()-1)).intValue()).getName());
 
             return sb.toString();
         }
 
         public void specifyAllele(String s) {
             allele = s;
         }
 
         public String getAllele() {
             return allele;
         }
 
         public boolean equals(Object o) {
             if(o instanceof AssociationTest) {
                 AssociationTest at = (AssociationTest) o;
                 if(markers.equals(at.markers) && allele.equals(at.allele)){
                     return true;
                 }
             }
             return false;
         }
 
         public int hashCode() {
             return markers.hashCode() + allele.hashCode();
         }
     }
 
     public void saveResultsToText(File outputFile) throws IOException{
         if(results == null) {
             return;
         }
 
         if(Options.getAssocTest() != ASSOC_TRIO && Options.getAssocTest() != ASSOC_CC) {
             return;
         }
 
         FileWriter fw;
         fw = new FileWriter(outputFile);
 
         StringBuffer result = new StringBuffer();
         if(Options.getAssocTest() == ASSOC_TRIO) {
             result.append("Test\tAllele\tFreq.\tT:U\tChi Square\tP Value\n");
         } else if(Options.getAssocTest() == ASSOC_CC) {
             result.append("Test\tAllele\tFreq.\tCase, Control Ratios\tChi Square\tP Value\n");
         }
 
         for (int i = 0; i < results.size(); i++){
             AssociationResult ar = (AssociationResult) results.elementAt(i);
             for (int j = 0; j < ar.getAlleleCount(); j++){
                 result.append(ar.getName()).append("\t");
                 if (ar instanceof MarkerAssociationResult){
                     result.append(((MarkerAssociationResult)ar).getOverTransmittedAllele()).append("\t");
                     result.append("\t");
                 }else{
                     result.append(ar.getAlleleName(j)).append("\t");
                     result.append(ar.getFreq(j)).append("\t");
                 }
                 result.append(ar.getCountString(j)).append("\t");
                 result.append(ar.getChiSquare(j)).append("\t");
                 result.append(ar.getPValue(j)).append("\n");
 
                 if (ar instanceof MarkerAssociationResult){
                     //only show one line for SNPs instead of one line per allele
                     break;
                 }
             }
         }
 
         fw.write(result.toString().toCharArray());
         fw.close();
     }
 
     public void saveHapsToText(File outputFile) throws IOException{
         if(results == null) {
             return;
         }
 
         if(Options.getAssocTest() != ASSOC_TRIO && Options.getAssocTest() != ASSOC_CC) {
             return;
         }
 
         FileWriter fw;
         fw = new FileWriter(outputFile);
 
         StringBuffer result = new StringBuffer();
         if(Options.getAssocTest() == ASSOC_TRIO) {
             result.append("Block\tHaplotype\tFreq.\tT:U\tChi Square\tP Value\n");
         } else if(Options.getAssocTest() == ASSOC_CC) {
             result.append("Block\tHaplotype\tFreq.\tCase, Control Ratios\tChi Square\tP Value\n");
         }
 
         for (int i = 0; i < results.size(); i++){
             if (results.elementAt(i) instanceof HaplotypeAssociationResult){
                 HaplotypeAssociationResult ar = (HaplotypeAssociationResult) results.elementAt(i);
                 result.append("Block " + (i+1)).append("\n");
                 for (int j = 0; j < ar.getAlleleCount(); j++){
                     result.append(ar.getAlleleName(j)).append("\t");
                     result.append(ar.getFreq(j)).append("\t");
                     result.append(ar.getCountString(j)).append("\t");
                     result.append(ar.getChiSquare(j)).append("\t");
                     result.append(ar.getPValue(j)).append("\n");
                 }
             }
         }
 
         fw.write(result.toString().toCharArray());
         fw.close();
     }
 
     public void saveSNPsToText(File outputFile) throws IOException{
         if(results == null) {
             return;
         }
 
         if(Options.getAssocTest() != ASSOC_TRIO && Options.getAssocTest() != ASSOC_CC) {
             return;
         }
 
         FileWriter fw;
         fw = new FileWriter(outputFile);
 
         StringBuffer result = new StringBuffer();
         if(Options.getAssocTest() == ASSOC_TRIO) {
             result.append("#\tName\tOvertransmitted\tT:U\tChi square\tP value\n");
         } else if(Options.getAssocTest() == ASSOC_CC) {
             result.append("#\tName\tMajor Alleles\tCase,Control Ratios\tChi square\tP value\n");
         }
 
         //only output assoc results for markers which werent filtered
         for(int i=0;i<Chromosome.getSize();i++) {
             if (results.get(Chromosome.realIndex[i]) instanceof MarkerAssociationResult){
                 MarkerAssociationResult currentResult = (MarkerAssociationResult) results.get(Chromosome.realIndex[i]);
                 result.append((Chromosome.realIndex[i] + 1)).append("\t");
                 result.append(currentResult.getName()).append("\t");
                 result.append(currentResult.getOverTransmittedAllele()).append("\t");
                 result.append(currentResult.getCountString()).append("\t");
                 result.append(currentResult.getChiSquare(0)).append("\t");
                 result.append(currentResult.getPValue(0)).append("\n");
             }
         }
 
         fw.write(result.toString().toCharArray());
         fw.close();
     }
 
     public void setPermTests(boolean permTests) {
         this.permTests = permTests;
     }
 }
