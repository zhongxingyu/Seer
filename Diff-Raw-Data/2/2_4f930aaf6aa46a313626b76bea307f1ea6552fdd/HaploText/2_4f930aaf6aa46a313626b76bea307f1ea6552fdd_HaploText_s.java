 package edu.mit.wi.haploview;
 
 import edu.mit.wi.pedfile.MarkerResult;
 import edu.mit.wi.pedfile.PedFileException;
 import edu.mit.wi.pedfile.CheckData;
 import edu.mit.wi.haploview.association.*;
 import edu.mit.wi.haploview.tagger.TaggerController;
 import edu.mit.wi.tagger.Tagger;
 import edu.mit.wi.tagger.TaggerException;
 
 import java.io.*;
 import java.util.*;
 import java.awt.image.BufferedImage;
 
 import com.sun.jimi.core.Jimi;
 import com.sun.jimi.core.JimiException;
 
 public class HaploText implements Constants{
     private boolean nogui = false;
     private String batchFileName;
     private String hapsFileName;
     private String infoFileName;
     private String pedFileName;
     private String hapmapFileName;
     private String blockFileName;
     private String trackFileName;
     private String customAssocTestsFileName;
     private boolean skipCheck = false;
     private Vector excludedMarkers = new Vector();
     private boolean quietMode = false;
     private int blockOutputType;
     private boolean outputCheck;
     private boolean individualCheck;
     private boolean mendel;
     private boolean outputDprime;
     private boolean outputPNG;
     private boolean outputCompressedPNG;
     private boolean doPermutationTest;
     private boolean findTags;
     private boolean randomizeAffection = false;
     private int permutationCount;
     private int tagging;
     private int maxNumTags;
     private double tagRSquaredCutOff = -1;
     private Vector forceIncludeTags;
     private String forceIncludeFileName;
     private Vector forceExcludeTags;
     private String forceExcludeFileName;
     private Vector argHandlerMessages;
     private String chromosomeArg;
 
 
     public boolean isNogui() {
         return nogui;
     }
 
     public String getBatchMode() {
         return batchFileName;
     }
 
     public String getHapsFileName() {
         return hapsFileName;
     }
 
     public String getPedFileName() {
         return pedFileName;
     }
 
     public String getInfoFileName(){
         return infoFileName;
     }
 
     public String getHapmapFileName(){
         return hapmapFileName;
     }
 
     public int getBlockOutputType() {
         return blockOutputType;
     }
 
     private double getDoubleArg(String[] args, int valueIndex, double min, double max) {
         double argument = 0;
         String argName = args[valueIndex-1];
         if(valueIndex>=args.length || ((args[valueIndex].charAt(0)) == '-')) {
             die( argName + " requires a value between " + min + " and " + max);
         }
         try {
             argument = Double.parseDouble(args[valueIndex]);
             if(argument<min || argument>max) {
                 die(argName + " requires a value between " + min + " and " + max);
             }
         }catch(NumberFormatException nfe) {
             die(argName + " requires a value between " + min + " and " + max);
         }
         return argument;
     }
 
     private int getIntegerArg(String[] args, int valueIndex){
         int argument = 0;
         String argName = args[valueIndex-1];
         if(valueIndex>=args.length || ((args[valueIndex].charAt(0)) == '-')){
             die(argName + " requires an integer argument");
         }
         else {
             try {
                 argument = Integer.parseInt(args[valueIndex]);
                 if(argument<0){
                     die(argName + " argument must be a positive integer");
                 }
             } catch(NumberFormatException nfe) {
                 die(argName + " argument must be a positive integer");
             }
         }
         return argument;
     }
 
     public HaploText(String[] args) {
         this.argHandler(args);
 
         if(this.batchFileName != null) {
             System.out.println(TITLE_STRING);
             for (int i = 0; i < argHandlerMessages.size(); i++){
                 System.out.println(argHandlerMessages.get(i));
             }
             this.doBatch();
         }
 
         if(!(this.pedFileName== null) || !(this.hapsFileName== null) || !(this.hapmapFileName== null)){
             if(nogui){
                 System.out.println(TITLE_STRING);
                 for (int i = 0; i < argHandlerMessages.size(); i++){
                     System.out.println(argHandlerMessages.get(i));
                 }
                 processTextOnly();
             }
         }
 
     }
 
     private void argHandler(String[] args){
 
         argHandlerMessages = new Vector();
         int maxDistance = -1;
         //this means that user didn't specify any output type if it doesn't get changed below
         blockOutputType = -1;
         double hapThresh = -1;
         double minimumMAF=-1;
         double spacingThresh = -1;
         double minimumGenoPercent = -1;
         double hwCutoff = -1;
         double missingCutoff = -1;
         int maxMendel = -1;
         boolean assocTDT = false;
         boolean assocCC = false;
         permutationCount = 0;
         tagging = Tagger.NONE;
         maxNumTags = Tagger.DEFAULT_MAXNUMTAGS;
         findTags = true;
 
         double cutHighCI = -1;
         double cutLowCI = -1;
         double mafThresh = -1;
         double recHighCI = -1;
         double informFrac = -1;
         double fourGameteCutoff = -1;
         double spineDP = -1;
 
 
         for(int i =0; i < args.length; i++) {
             if(args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("-h")) {
                 System.out.println(HELP_OUTPUT);
                 System.exit(0);
             }
             else if(args[i].equalsIgnoreCase("-n") || args[i].equalsIgnoreCase("-nogui")) {
                 nogui = true;
             }
             else if(args[i].equalsIgnoreCase("-p") || args[i].equalsIgnoreCase("-pedfile")) {
                 i++;
                 if( i>=args.length || (args[i].charAt(0) == '-')){
                     die(args[i-1] + " requires a filename");
                 }
                 else{
                     if(pedFileName != null){
                         argHandlerMessages.add("multiple "+args[i-1] + " arguments found. only last pedfile listed will be used");
                     }
                     pedFileName = args[i];
                 }
             }
             else if (args[i].equalsIgnoreCase("-pcloadletter")){
                 die("PC LOADLETTER?! What the fuck does that mean?!");
             }
             else if (args[i].equalsIgnoreCase("-skipcheck") || args[i].equalsIgnoreCase("--skipcheck")){
                 skipCheck = true;
             }
             else if (args[i].equalsIgnoreCase("-excludeMarkers")){
                 i++;
                 if(i>=args.length || (args[i].charAt(0) == '-')){
                     die("-excludeMarkers requires a list of markers");
                 }
                 else {
                     StringTokenizer str = new StringTokenizer(args[i],",");
                     try {
                         StringBuffer sb = new StringBuffer();
                         if (!quietMode) sb.append("Excluding markers: ");
                         while(str.hasMoreTokens()) {
                             String token = str.nextToken();
                             if(token.indexOf("..") != -1) {
                                 int lastIndex = token.indexOf("..");
                                 int rangeStart = Integer.parseInt(token.substring(0,lastIndex));
                                 int rangeEnd = Integer.parseInt(token.substring(lastIndex+2,token.length()));
                                 for(int j=rangeStart;j<=rangeEnd;j++) {
                                     if (!quietMode) sb.append(j+" ");
                                     excludedMarkers.add(new Integer(j));
                                 }
                             } else {
                                 if (!quietMode) sb.append(token+" ");
                                 excludedMarkers.add(new Integer(token));
                             }
                         }
                         if (!quietMode) argHandlerMessages.add(sb.toString());
                     } catch(NumberFormatException nfe) {
                         die("-excludeMarkers argument should be of the format: 1,3,5..8,12");
                     }
                 }
             }
             else if(args[i].equalsIgnoreCase("-ha") || args[i].equalsIgnoreCase("-l") || args[i].equalsIgnoreCase("-haps")) {
                 i++;
                 if(i>=args.length || ((args[i].charAt(0)) == '-')){
                     die(args[i-1] + " requires a filename");
                 }
                 else{
                     if(hapsFileName != null){
                         argHandlerMessages.add("multiple "+args[i-1] + " arguments found. only last haps file listed will be used");
                     }
                     hapsFileName = args[i];
                 }
             }
             else if(args[i].equalsIgnoreCase("-i") || args[i].equalsIgnoreCase("-info")) {
                 i++;
                 if(i>=args.length || ((args[i].charAt(0)) == '-')){
                     die(args[i-1] + " requires a filename");
                 }
                 else{
                     if(infoFileName != null){
                         argHandlerMessages.add("multiple "+args[i-1] + " arguments found. only last info file listed will be used");
                     }
                     infoFileName = args[i];
                 }
             } else if (args[i].equalsIgnoreCase("-a") || args[i].equalsIgnoreCase("-hapmap")){
                 i++;
                 if(i>=args.length || ((args[i].charAt(0)) == '-')){
                     die(args[i-1] + " requires a filename");
                 }
                 else{
                     if(hapmapFileName != null){
                         argHandlerMessages.add("multiple "+args[i-1] + " arguments found. only last hapmap file listed will be used");
                     }
                     hapmapFileName = args[i];
                 }
             }
             else if(args[i].equalsIgnoreCase("-k") || args[i].equalsIgnoreCase("-blocks")) {
                 i++;
                 if (!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                     blockFileName = args[i];
                     blockOutputType = BLOX_CUSTOM;
                 }else{
                     die(args[i-1] + " requires a filename");
                 }
             }
             else if (args[i].equalsIgnoreCase("-png")){
                 outputPNG = true;
             }
             else if (args[i].equalsIgnoreCase("-smallpng") || args[i].equalsIgnoreCase("-compressedPNG")){
                 outputCompressedPNG = true;
             }
             else if (args[i].equalsIgnoreCase("-track")){
                 i++;
                 if (!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                    trackFileName = args[i];
                 }else{
                     die("-track requires a filename");
                 }
             }
             else if(args[i].equalsIgnoreCase("-o") || args[i].equalsIgnoreCase("-output") || args[i].equalsIgnoreCase("-blockoutput")) {
                 i++;
                 if(!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                     if(blockOutputType != -1){
                         die("Only one block output type argument is allowed.");
                     }
                     if(args[i].equalsIgnoreCase("SFS") || args[i].equalsIgnoreCase("GAB")){
                         blockOutputType = BLOX_GABRIEL;
                     }
                     else if(args[i].equalsIgnoreCase("GAM")){
                         blockOutputType = BLOX_4GAM;
                     }
                     else if(args[i].equalsIgnoreCase("MJD") || args[i].equalsIgnoreCase("SPI")){
                         blockOutputType = BLOX_SPINE;
                     }
                     else if(args[i].equalsIgnoreCase("ALL")) {
                         blockOutputType = BLOX_ALL;
                     }
                 }
                 else {
                     //defaults to SFS output
                     blockOutputType = BLOX_GABRIEL;
                     i--;
                 }
             }
             else if(args[i].equalsIgnoreCase("-d") || args[i].equalsIgnoreCase("--dprime") || args[i].equalsIgnoreCase("-dprime")) {
                 outputDprime = true;
             }
             else if (args[i].equalsIgnoreCase("-c") || args[i].equalsIgnoreCase("-check")){
                 outputCheck = true;
             }
             else if (args[i].equalsIgnoreCase("-indcheck")){
                  individualCheck = true;
             }
             else if (args[i].equalsIgnoreCase("-mendel")){
                 mendel = true;
             }
             else if(args[i].equalsIgnoreCase("-m") || args[i].equalsIgnoreCase("-maxdistance")) {
                 i++;
                 maxDistance = getIntegerArg(args,i);
             }
             else if(args[i].equalsIgnoreCase("-b") || args[i].equalsIgnoreCase("-batch")) {
                 //batch mode
                 i++;
                 if(i>=args.length || ((args[i].charAt(0)) == '-')){
                     die(args[i-1] + " requires a filename");
                 }
                 else{
                     if(batchFileName != null){
                         argHandlerMessages.add("multiple " + args[i-1] +  " arguments found. only last batch file listed will be used");
                     }
                     batchFileName = args[i];
                 }
             }
             else if(args[i].equalsIgnoreCase("-hapthresh")) {
                 i++;
                 hapThresh = getDoubleArg(args,i,0,1);
             }
             else if(args[i].equalsIgnoreCase("-spacing")) {
                 i++;
                 spacingThresh = getDoubleArg(args,i,0,1);
             }
             else if(args[i].equalsIgnoreCase("-minMAF")) {
                 i++;
                 minimumMAF = getDoubleArg(args,i,0,0.5);
             }
             else if(args[i].equalsIgnoreCase("-minGeno") || args[i].equalsIgnoreCase("-minGenoPercent")) {
                 i++;
                 minimumGenoPercent = getDoubleArg(args,i,0,1);
             }
             else if(args[i].equalsIgnoreCase("-hwcutoff")) {
                i++;
                 hwCutoff = getDoubleArg(args,i,0,1);
             }
             else if(args[i].equalsIgnoreCase("-maxMendel") ) {
                 i++;
                 maxMendel = getIntegerArg(args,i);
             }
             else if(args[i].equalsIgnoreCase("-missingcutoff")) {
                 i++;
                 missingCutoff = getDoubleArg(args,i,0,1);
             }
             else if(args[i].equalsIgnoreCase("-assoctdt")) {
                 assocTDT = true;
             }
             else if(args[i].equalsIgnoreCase("-assoccc")) {
                 assocCC = true;
             }
             else if(args[i].equalsIgnoreCase("-randomcc")){
                 assocCC = true;
                 randomizeAffection = true;
             }
             else if(args[i].equalsIgnoreCase("-ldcolorscheme")) {
                 i++;
                 if(!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                     if(args[i].equalsIgnoreCase("default")){
                         Options.setLDColorScheme(STD_SCHEME);
                     }
                     else if(args[i].equalsIgnoreCase("RSQ")){
                         Options.setLDColorScheme(RSQ_SCHEME);
                     }
                     else if(args[i].equalsIgnoreCase("DPALT") ){
                         Options.setLDColorScheme(WMF_SCHEME);
                     }
                     else if(args[i].equalsIgnoreCase("GAB")) {
                         Options.setLDColorScheme(GAB_SCHEME);
                     }
                     else if(args[i].equalsIgnoreCase("GAM")) {
                         Options.setLDColorScheme(GAM_SCHEME);
                     }
                     else if(args[i].equalsIgnoreCase("GOLD")) {
                         Options.setLDColorScheme(GOLD_SCHEME);
                     }
                 }
                 else {
                     //defaults to STD color scheme
                     Options.setLDColorScheme(STD_SCHEME);
                     i--;
                 }
             }
             else if(args[i].equalsIgnoreCase("-blockCutHighCI")) {
                 i++;
                 cutHighCI = getDoubleArg(args,i,0,1);
             }
             else if(args[i].equalsIgnoreCase("-blockCutLowCI")) {
                 i++;
                 cutLowCI = getDoubleArg(args,i,0,1);
             }
             else if(args[i].equalsIgnoreCase("-blockMafThresh")) {
                 i++;
                 mafThresh = getDoubleArg(args,i,0,1);
             }
             else if(args[i].equalsIgnoreCase("-blockRecHighCI")) {
                 i++;
                 recHighCI = getDoubleArg(args,i,0,1);
             }
             else if(args[i].equalsIgnoreCase("-blockInformFrac")) {
                 i++;
                 informFrac = getDoubleArg(args,i,0,1);
             }
             else if(args[i].equalsIgnoreCase("-block4GamCut")) {
                 i++;
                 fourGameteCutoff = getDoubleArg(args,i,0,1);
             }
             else if(args[i].equalsIgnoreCase("-blockSpineDP")) {
                 i++;
                 spineDP = getDoubleArg(args,i,0,1);
             }
             else if(args[i].equalsIgnoreCase("-permtests")) {
                 i++;
                 doPermutationTest = true;
                 permutationCount = getIntegerArg(args,i);
             }
             else if(args[i].equalsIgnoreCase("-customassoc")) {
                 i++;
                 if (!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                     customAssocTestsFileName = args[i];
                 }else{
                     die(args[i-1] + " requires a filename");
                 }
             }
             else if(args[i].equalsIgnoreCase("-aggressiveTagging")) {
                 tagging = Tagger.AGGRESSIVE_TRIPLE;
             }
             else if (args[i].equalsIgnoreCase("-pairwiseTagging")){
                 tagging = Tagger.PAIRWISE_ONLY;
             }
             else if (args[i].equalsIgnoreCase("-printalltags")){
                 Options.setPrintAllTags(true);
             }
             else if(args[i].equalsIgnoreCase("-maxNumTags")){
                 i++;
                 maxNumTags = getIntegerArg(args,i);
             }
             else if(args[i].equalsIgnoreCase("-tagrSqCutoff")) {
                 i++;
                 tagRSquaredCutOff = getDoubleArg(args,i,0,1);
             }
             else if (args[i].equalsIgnoreCase("-dontaddtags")){
                 findTags = false;
             }
             else if(args[i].equalsIgnoreCase("-tagLODCutoff")) {
                 i++;
                 Options.setTaggerLODCutoff(getDoubleArg(args,i,0,100000));
             }
             else if(args[i].equalsIgnoreCase("-includeTags")) {
                 i++;
                 if(i>=args.length || args[i].charAt(0) == '-') {
                     die(args[i-1] + " requires a list of marker names.");
                 }
                 StringTokenizer str = new StringTokenizer(args[i],",");
                 forceIncludeTags = new Vector();
                 while(str.hasMoreTokens()) {
                     forceIncludeTags.add(str.nextToken());
                 }
             }
             else if (args[i].equalsIgnoreCase("-includeTagsFile")) {
                 i++;
                 if(!(i>=args.length) && !(args[i].charAt(0) == '-')) {
                     forceIncludeFileName =args[i];
                 }else {
                     die(args[i-1] + " requires a filename");
                 }
             }
             else if(args[i].equalsIgnoreCase("-excludeTags")) {
                 i++;
                 if(i>=args.length || args[i].charAt(0) == '-') {
                     die("-excludeTags requires a list of marker names.");
                 }
                 StringTokenizer str = new StringTokenizer(args[i],",");
                 forceExcludeTags = new Vector();
                 while(str.hasMoreTokens()) {
                     forceExcludeTags.add(str.nextToken());
                 }
             }
             else if (args[i].equalsIgnoreCase("-excludeTagsFile")) {
                 i++;
                 if(!(i>=args.length) && !(args[i].charAt(0) == '-')) {
                     forceExcludeFileName =args[i];
                 }else {
                     die(args[i-1] + " requires a filename");
                 }
             }
             else if(args[i].equalsIgnoreCase("-chromosome") || args[i].equalsIgnoreCase("-chr")) {
                 i++;
                 if(!(i>=args.length) && !(args[i].charAt(0) == '-')) {
                     chromosomeArg =args[i];
                 }else {
                     die(args[i-1] + " requires a chromosome name");
                 }
 
             }
             else if(args[i].equalsIgnoreCase("-q") || args[i].equalsIgnoreCase("-quiet")) {
                 quietMode = true;
             }
             else {
                 die("invalid parameter specified: " + args[i]);
             }
         }
 
         int countOptions = 0;
         if(pedFileName != null) {
             countOptions++;
         }
         if(hapsFileName != null) {
             countOptions++;
         }
         if(hapmapFileName != null) {
             countOptions++;
         }
         if(batchFileName != null) {
             countOptions++;
         }
         if(countOptions > 1) {
             die("Only one genotype input file may be specified on the command line.");
         }
         else if(countOptions == 0 && nogui) {
             die("You must specify a genotype input file.");
         }
 
         //mess with vars, set defaults, etc
         if(skipCheck && !quietMode) {
             argHandlerMessages.add("Skipping genotype file check");
         }
         if(maxDistance == -1){
             maxDistance = MAXDIST_DEFAULT;
         }else{
             if (!quietMode) argHandlerMessages.add("Max LD comparison distance = " +maxDistance);
         }
 
         Options.setMaxDistance(maxDistance);
 
         if(hapThresh != -1) {
             Options.setHaplotypeDisplayThreshold(hapThresh);
             if (!quietMode) argHandlerMessages.add("Haplotype display threshold = " + hapThresh);
         }
 
         if(minimumMAF != -1) {
             CheckData.mafCut = minimumMAF;
             if (!quietMode) argHandlerMessages.add("Minimum MAF = " + minimumMAF);
         }
 
         if(minimumGenoPercent != -1) {
             CheckData.failedGenoCut = (int)(minimumGenoPercent*100);
             if (!quietMode) argHandlerMessages.add("Minimum SNP genotype % = " + minimumGenoPercent);
         }
 
         if(hwCutoff != -1) {
             CheckData.hwCut = hwCutoff;
             if (!quietMode) argHandlerMessages.add("Hardy Weinberg equilibrium p-value cutoff = " + hwCutoff);
         }
 
         if(maxMendel != -1) {
             CheckData.numMendErrCut = maxMendel;
             if (!quietMode) argHandlerMessages.add("Maximum number of Mendel errors = "+maxMendel);
         }
 
         if(spacingThresh != -1) {
             Options.setSpacingThreshold(spacingThresh);
             if (!quietMode) argHandlerMessages.add("LD display spacing value = "+spacingThresh);
         }
 
         if(missingCutoff != -1) {
             Options.setMissingThreshold(missingCutoff);
             if (!quietMode) argHandlerMessages.add("Maximum amount of missing data allowed per individual = "+missingCutoff);
         }
 
         if(cutHighCI != -1) {
             FindBlocks.cutHighCI = cutHighCI;
         }
 
         if(cutLowCI != -1) {
             FindBlocks.cutLowCI = cutLowCI;
         }
         if(mafThresh != -1) {
             FindBlocks.mafThresh = mafThresh;
         }
         if(recHighCI != -1) {
             FindBlocks.recHighCI = recHighCI;
         }
         if(informFrac != -1) {
             FindBlocks.informFrac = informFrac;
         }
         if(fourGameteCutoff != -1) {
             FindBlocks.fourGameteCutoff = fourGameteCutoff;
         }
         if(spineDP != -1) {
             FindBlocks.spineDP = spineDP;
         }
 
         if(assocTDT) {
             Options.setAssocTest(ASSOC_TRIO);
         }else if(assocCC) {
             Options.setAssocTest(ASSOC_CC);
         }
 
         if (Options.getAssocTest() != ASSOC_NONE && infoFileName == null && hapmapFileName == null) {
             die("A marker info file must be specified when performing association tests.");
         }
 
         if(doPermutationTest) {
             if(!assocCC && !assocTDT) {
                 die("An association test type must be specified for permutation tests to be performed.");
             }
         }
 
         if(customAssocTestsFileName != null) {
             if(!assocCC && !assocTDT) {
                 die("An association test type must be specified when using a custom association test file.");
             }
             if(infoFileName == null) {
                 die("A marker info file must be specified when using a custom association test file.");
             }
         }
 
         if(tagging != Tagger.NONE) {
             if(infoFileName == null && hapmapFileName == null && batchFileName == null) {
                 die("A marker info file must be specified when tagging.");
             }
 
             if(forceExcludeTags == null) {
                 forceExcludeTags = new Vector();
             } else if (forceExcludeFileName != null) {
                 die("-excludeTags and -excludeTagsFile cannot both be used");
             }
 
             if(forceExcludeFileName != null) {
                 File excludeFile = new File(forceExcludeFileName);
                 forceExcludeTags = new Vector();
 
                 try {
                     BufferedReader br = new BufferedReader(new FileReader(excludeFile));
                     String line;
                     while((line = br.readLine()) != null) {
                         if(line.length() > 0 && line.charAt(0) != '#'){
                             forceExcludeTags.add(line);
                         }
                     }
                 }catch(IOException ioe) {
                     die("An error occured while reading the file specified by -excludeTagsFile.");
                 }
             }
 
             if(forceIncludeTags == null ) {
                 forceIncludeTags = new Vector();
             } else if (forceIncludeFileName != null) {
                 die("-includeTags and -includeTagsFile cannot both be used");
             }
 
             if(forceIncludeFileName != null) {
                 File includeFile = new File(forceIncludeFileName);
                 forceIncludeTags = new Vector();
 
                 try {
                     BufferedReader br = new BufferedReader(new FileReader(includeFile));
                     String line;
                     while((line = br.readLine()) != null) {
                         if(line.length() > 0 && line.charAt(0) != '#'){
                             forceIncludeTags.add(line);
                         }
                     }
                 }catch(IOException ioe) {
                     die("An error occured while reading the file specified by -includeTagsFile.");
                 }
             }
 
             //check that there isn't any overlap between include/exclude lists
             Vector tempInclude = (Vector) forceIncludeTags.clone();
             tempInclude.retainAll(forceExcludeTags);
             if(tempInclude.size() > 0) {
                 StringBuffer sb = new StringBuffer();
                 for (int i = 0; i < tempInclude.size(); i++) {
                     String s = (String) tempInclude.elementAt(i);
                     sb.append(s).append(",");
                 }
                 die("The following markers appear in both the include and exclude lists: " + sb.toString());
             }
 
             if(tagRSquaredCutOff != -1) {
                 Options.setTaggerRsqCutoff(tagRSquaredCutOff);
             }
 
         } else if(forceExcludeTags != null || forceIncludeTags != null || tagRSquaredCutOff != -1) {
             die("-tagrSqCutoff, -excludeTags, -excludeTagsFile, -includeTags and -includeTagsFile cannot be used without a tagging option");
         }
 
 
         if(chromosomeArg != null && hapmapFileName != null) {
             argHandlerMessages.add("-chromosome flag ignored when loading hapmap file");
             chromosomeArg = null;
         }
         if(chromosomeArg != null) {
             Chromosome.setDataChrom("chr" + chromosomeArg);
         }
     }
 
     private void die(String msg){
         System.err.println(TITLE_STRING + " Fatal Error");
         System.err.println(msg);
         System.exit(1);
     }
 
     private void doBatch() {
         Vector files;
         File batchFile;
         File dataFile;
         String line;
         StringTokenizer tok;
         String infoMaybe =null;
 
         files = new Vector();
         if(batchFileName == null) {
             return;
         }
         batchFile = new File(this.batchFileName);
 
         if(!batchFile.exists()) {
             System.out.println("batch file " + batchFileName + " does not exist");
             System.exit(1);
         }
 
         if (!quietMode) System.out.println("Processing batch input file: " + batchFile);
 
         try {
             BufferedReader br = new BufferedReader(new FileReader(batchFile));
             while( (line = br.readLine()) != null ) {
                 files.add(line);
             }
             br.close();
 
             for(int i = 0;i<files.size();i++){
                 line = (String)files.get(i);
                 tok = new StringTokenizer(line);
                 infoMaybe = null;
                 if(tok.hasMoreTokens()){
                     dataFile = new File(tok.nextToken());
                     if(tok.hasMoreTokens()){
                         infoMaybe = tok.nextToken();
                     }
 
                     if(dataFile.exists()) {
                         String name = dataFile.getName();
                         if( name.substring(name.length()-4,name.length()).equalsIgnoreCase(".ped") ) {
                             processFile(name,PED_FILE,infoMaybe);
                         }
                         else if(name.substring(name.length()-5,name.length()).equalsIgnoreCase(".haps")) {
                             processFile(name,HAPS_FILE,infoMaybe);
                         }
                         else if(name.substring(name.length()-4,name.length()).equalsIgnoreCase(".hmp")){
                             processFile(name,HMP_FILE,null);
                         }
                         else{
                             if (!quietMode){
                                 System.out.println("Filenames in batch file must end in .ped, .haps or .hmp\n" +
                                         name + " is not properly formatted.");
                             }
                         }
                     }
                     else {
                         if(!quietMode){
                             System.out.println("file " + dataFile.getName() + " listed in the batch file could not be found");
                         }
                     }
                 }
 
             }
         }
         catch(FileNotFoundException e){
             System.out.println("the following error has occured:\n" + e.toString());
         }
         catch(IOException e){
             System.out.println("the following error has occured:\n" + e.toString());
         }
 
     }
 
     private File validateOutputFile(String fn){
         File f = new File(fn);
         if (f.exists() && !quietMode){
             System.out.println("File " + f.getName() + " already exists and will be overwritten.");
         }
         if (!quietMode) System.out.println("Writing output to "+f.getName());
         return f;
     }
 
     /**
      * this method finds haplotypes and caclulates dprime without using any graphics
      */
     private void processTextOnly(){
         String fileName;
         int fileType;
         if(hapsFileName != null) {
             fileName = hapsFileName;
             fileType = HAPS_FILE;
         }
         else if (pedFileName != null){
             fileName = pedFileName;
             fileType = PED_FILE;
         }else{
             fileName = hapmapFileName;
             fileType = HMP_FILE;
         }
 
         processFile(fileName,fileType,infoFileName);
     }
     /**
      * this
      * @param fileName name of the file to process
      * @param fileType true means pedfilem false means hapsfile
      * @param infoFileName
      */
     private void processFile(String fileName, int fileType, String infoFileName){
         try {
             HaploData textData;
             File outputFile;
             File inputFile;
             AssociationTestSet customAssocSet;
 
             if(!quietMode && fileName != null){
                 System.out.println("Using data file: " + fileName);
             }
 
             inputFile = new File(fileName);
             if(!inputFile.exists()){
                 System.out.println("input file: " + fileName + " does not exist");
                 System.exit(1);
             }
 
             textData = new HaploData();
             //Vector result = null;
 
             if(fileType == HAPS_FILE){
                 //read in haps file
                 textData.prepareHapsInput(inputFile);
             }
             else if (fileType == PED_FILE) {
                 //read in ped file
                 textData.linkageToChrom(inputFile, PED_FILE);
 
                 if(textData.getPedFile().isBogusParents()) {
                     System.out.println("Error: One or more individuals in the file reference non-existent parents.\nThese references have been ignored.");
                 }
                 if(textData.getPedFile().isHaploidHets()){
                     System.out.println("Error: One or more males in the file is heterozygous.\nThese genotypes have been ignored.");
                 }
             }else{
                 //read in hapmapfile
                 textData.linkageToChrom(inputFile,HMP_FILE);
             }
 
 
             File infoFile = null;
             if (infoFileName != null){
                 infoFile = new File(infoFileName);
             }
 
             textData.prepareMarkerInput(infoFile,textData.getPedFile().getHMInfo());
 
             HashSet whiteListedCustomMarkers = new HashSet();
             if (customAssocTestsFileName != null){
                 customAssocSet = new AssociationTestSet(customAssocTestsFileName);
                 whiteListedCustomMarkers = customAssocSet.getWhitelist();
             }else{
                 customAssocSet = null;
             }
 
             Hashtable snpsByName = new Hashtable();
             for(int i=0;i<Chromosome.getUnfilteredSize();i++) {
                 SNP snp = Chromosome.getUnfilteredMarker(i);
                 snpsByName.put(snp.getDisplayName(), snp);
             }
 
             if(forceIncludeTags != null) {
                 for(int i=0;i<forceIncludeTags.size();i++) {
                     if(snpsByName.containsKey(forceIncludeTags.get(i))) {
                         whiteListedCustomMarkers.add(snpsByName.get(forceIncludeTags.get(i)));
                     }
                 }
             }
 
             textData.getPedFile().setWhiteList(whiteListedCustomMarkers);
 
             boolean[] markerResults = new boolean[Chromosome.getUnfilteredSize()];
             Vector result = null;
             result = textData.getPedFile().getResults();
             //once check has been run we can filter the markers
             for (int i = 0; i < result.size(); i++){
                 if (((((MarkerResult)result.get(i)).getRating() > 0 || skipCheck) &&
                         Chromosome.getUnfilteredMarker(i).getDupStatus() != 2)){
                     markerResults[i] = true;
                 }else{
                     markerResults[i] = false;
                 }
             }
 
             for (int i = 0; i < excludedMarkers.size(); i++){
                 int cur = ((Integer)excludedMarkers.elementAt(i)).intValue();
                 if (cur < 1 || cur > markerResults.length){
                     System.out.println("Excluded marker out of bounds: " + cur +
                             "\nMarkers must be between 1 and N, where N is the total number of markers.");
                     System.exit(1);
                 }else{
                     markerResults[cur-1] = false;
                 }
             }
 
 
             for(int i=0;i<Chromosome.getUnfilteredSize();i++) {
                 if(textData.getPedFile().isWhiteListed(Chromosome.getUnfilteredMarker(i))) {
                     markerResults[i] = true;
                 }
             }
 
             Chromosome.doFilter(markerResults);
 
             if(!quietMode && infoFile != null){
                 System.out.println("Using marker information file: " + infoFile.getName());
             }
             if(outputCheck && result != null){
                 textData.getPedFile().saveCheckDataToText(validateOutputFile(fileName + ".CHECK"));
             }
             if(individualCheck && result != null){
                 IndividualDialog id = new IndividualDialog(textData);
                 id.printTable(validateOutputFile(fileName + ".INDCHECK"));
             }
             if(mendel && result != null){
                 MendelDialog md = new MendelDialog(textData);
                 md.printTable(validateOutputFile(fileName + ".MENDEL" ));
             }
             Vector cust = new Vector();
             AssociationTestSet blockTestSet = null;
 
             if(blockOutputType != -1){
                 textData.generateDPrimeTable();
                 Haplotype[][] haplos;
                 Haplotype[][] filtHaplos;
                 switch(blockOutputType){
                     case BLOX_GABRIEL:
                         outputFile = validateOutputFile(fileName + ".GABRIELblocks");
                         break;
                     case BLOX_4GAM:
                         outputFile = validateOutputFile(fileName + ".4GAMblocks");
                         break;
                     case BLOX_SPINE:
                         outputFile = validateOutputFile(fileName + ".SPINEblocks");
                         break;
                     case BLOX_CUSTOM:
                         outputFile = validateOutputFile(fileName + ".CUSTblocks");
                         //read in the blocks file
                         File blocksFile = new File(blockFileName);
                         if(!quietMode) {
                             System.out.println("Using custom blocks file " + blockFileName);
                         }
                         cust = textData.readBlocks(blocksFile);
                         break;
                     case BLOX_ALL:
                         //handled below, so we don't do anything here
                         outputFile = null;
                         break;
                     default:
                         outputFile = validateOutputFile(fileName + ".GABRIELblocks");
                         break;
 
                 }
 
                 //this handles output type ALL
                 if(blockOutputType == BLOX_ALL) {
                     outputFile = validateOutputFile(fileName + ".GABRIELblocks");
                     textData.guessBlocks(BLOX_GABRIEL);
 
                     haplos = textData.generateBlockHaplotypes(textData.blocks);
                     if (haplos != null){
                         filtHaplos = filterHaplos(haplos);
                         textData.pickTags(filtHaplos);
                         textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), outputFile);
                     }else if (!quietMode){
                         System.out.println("Skipping block output: no valid Gabriel blocks.");
                     }
 
                     outputFile = validateOutputFile(fileName + ".4GAMblocks");
                     textData.guessBlocks(BLOX_4GAM);
 
                     haplos = textData.generateBlockHaplotypes(textData.blocks);
                     if (haplos != null){
                         filtHaplos = filterHaplos(haplos);
                         textData.pickTags(filtHaplos);
                         textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), outputFile);
                     }else if (!quietMode){
                         System.out.println("Skipping block output: no valid 4 Gamete blocks.");
                     }
 
                     outputFile = validateOutputFile(fileName + ".SPINEblocks");
                     textData.guessBlocks(BLOX_SPINE);
 
                     haplos = textData.generateBlockHaplotypes(textData.blocks);
                     if (haplos != null){
                         filtHaplos = filterHaplos(haplos);
                         textData.pickTags(filtHaplos);
                         textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), outputFile);
                     }else if (!quietMode){
                         System.out.println("Skipping block output: no valid LD Spine blocks.");
                     }
 
                 }else{
                     //guesses blocks based on output type determined above.
                     textData.guessBlocks(blockOutputType, cust);
 
                     haplos = textData.generateBlockHaplotypes(textData.blocks);
                     if (haplos != null){
 
                         filtHaplos = filterHaplos(haplos);
                         textData.pickTags(filtHaplos);
                         textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), outputFile);
                     }else if (!quietMode){
                         System.out.println("Skipping block output: no valid blocks.");
                     }
                 }
 
                 if(Options.getAssocTest() == ASSOC_TRIO || Options.getAssocTest() == ASSOC_CC) {
                     if (blockOutputType == BLOX_ALL){
                         System.out.println("Haplotype association results cannot be used with block output \"ALL\"");
                     }else{
                         if (haplos != null){
                             blockTestSet = new AssociationTestSet(haplos,null);
                             blockTestSet.saveHapsToText(validateOutputFile(fileName + ".HAPASSOC"));
 
                         }else if (!quietMode){
                             System.out.println("Skipping block association output: no valid blocks.");
                         }
                     }
                 }
             }
 
             if(outputDprime) {
                 outputFile = validateOutputFile(fileName + ".LD");
                 if (textData.dpTable != null){
                     textData.saveDprimeToText(outputFile, TABLE_TYPE, 0, Chromosome.getSize());
                 }else{
                     textData.saveDprimeToText(outputFile, LIVE_TYPE, 0, Chromosome.getSize());
                 }
             }
 
             if (outputPNG || outputCompressedPNG){
                 outputFile = validateOutputFile(fileName + ".LD.PNG");
                 if (textData.dpTable == null){
                     textData.generateDPrimeTable();
                     textData.guessBlocks(BLOX_CUSTOM, new Vector());
                 }
                 if (trackFileName != null){
                     textData.readAnalysisTrack(new File(trackFileName));
                     if(!quietMode) {
                         System.out.println("Using analysis track file " + trackFileName);
                     }
                 }
                 DPrimeDisplay dpd = new DPrimeDisplay(textData);
                 BufferedImage i = dpd.export(0,Chromosome.getUnfilteredSize(),outputCompressedPNG);
                 try{
                     Jimi.putImage("image/png", i, outputFile.getAbsolutePath());
                 }catch(JimiException je){
                     System.out.println(je.getMessage());
                 }
             }
 
             AssociationTestSet markerTestSet =null;
             if(Options.getAssocTest() == ASSOC_TRIO || Options.getAssocTest() == ASSOC_CC){
                 if (randomizeAffection){
                     Vector aff = new Vector();
                     int j=0, k=0;
                     for (int i = 0; i < textData.getPedFile().getNumIndividuals(); i++){
                         if (i%2 == 0){
                             aff.add(new Integer(1));
                             j++;
                         }else{
                             aff.add(new Integer(2));
                             k++;
                         }
                     }
                     Collections.shuffle(aff);
                     markerTestSet = new AssociationTestSet(textData.getPedFile(),aff,null,Chromosome.getAllMarkers());
                 }else{
                     markerTestSet = new AssociationTestSet(textData.getPedFile(),null,null,Chromosome.getAllMarkers());
                 }
                 markerTestSet.saveSNPsToText(validateOutputFile(fileName + ".ASSOC"));
             }
 
             if(customAssocSet != null) {
                 if(!quietMode) {
                     System.out.println("Using custom association test file " + customAssocTestsFileName);
                 }
                 try {
                     customAssocSet.setPermTests(doPermutationTest);
                     customAssocSet.runFileTests(textData,markerTestSet.getMarkerAssociationResults());
                     customAssocSet.saveResultsToText(validateOutputFile(fileName + ".CUSTASSOC"));
 
                 }catch(IOException ioe) {
                     System.out.println("An error occured writing the custom association results file.");
                     customAssocSet = null;
                 }
             }
 
             if(doPermutationTest) {
                 AssociationTestSet permTests = new AssociationTestSet();
                 permTests.cat(markerTestSet);
                 if(blockTestSet != null) {
                     permTests.cat(blockTestSet);
                 }
                 final PermutationTestSet pts = new PermutationTestSet(permutationCount,textData.getPedFile(),customAssocSet,permTests);
                 Thread permThread = new Thread(new Runnable() {
                     public void run() {
                         if (pts.isCustom()){
                             pts.doPermutations(PermutationTestSet.CUSTOM);
                         }else{
                             pts.doPermutations(PermutationTestSet.SINGLE_PLUS_BLOCKS);
                         }
                     }
                 });
 
                 permThread.start();
 
                 if(!quietMode) {
                     System.out.println("Starting " + permutationCount + " permutation tests (each . printed represents 1% of tests completed)");
                 }
 
                 int dotsPrinted =0;
                 while(pts.getPermutationCount() - pts.getPermutationsPerformed() > 0) {
                     while(( (double)pts.getPermutationsPerformed() / pts.getPermutationCount())*100 > dotsPrinted) {
                         System.out.print(".");
                         dotsPrinted++;
                     }
                     try{
                         Thread.sleep(100);
                     }catch(InterruptedException ie) {}
                 }
                 System.out.println();
 
                 try {
                     pts.writeResultsToFile(validateOutputFile(fileName  + ".PERMUT"));
                 } catch(IOException ioe) {
                     System.out.println("An error occured while writing the permutation test results to file.");
                 }
             }
 
 
             if(tagging != Tagger.NONE) {
 
                 if(textData.dpTable == null) {
                     textData.generateDPrimeTable();
                 }
                 Vector snps = Chromosome.getAllMarkers();
                 HashSet names = new HashSet();
                 for (int i = 0; i < snps.size(); i++) {
                     SNP snp = (SNP) snps.elementAt(i);
                     names.add(snp.getDisplayName());
                 }
 
                 HashSet filteredNames = new HashSet();
                 for(int i=0;i<Chromosome.getSize();i++) {
                     filteredNames.add(Chromosome.getMarker(i).getDisplayName());
                 }
 
                 Vector sitesToCapture = new Vector();
                 for(int i=0;i<Chromosome.getSize();i++) {
                     sitesToCapture.add(Chromosome.getMarker(i));
                 }
 
                 for (int i = 0; i < forceIncludeTags.size(); i++) {
                     String s = (String) forceIncludeTags.elementAt(i);
                     if(!names.contains(s) && !quietMode) {
                         System.out.println("Warning: skipping marker " + s + " in the list of forced included tags since I don't know about it.");
                     }
                 }
 
                 for (int i = 0; i < forceExcludeTags.size(); i++) {
                     String s = (String) forceExcludeTags.elementAt(i);
                     if(!names.contains(s) && !quietMode) {
                         System.out.println("Warning: skipping marker " + s + " in the list of forced excluded tags since I don't know about it.");
                     }
                 }
 
                 //chuck out filtered jazz from excludes, and nonexistent markers from both
                 forceExcludeTags.retainAll(filteredNames);
                 forceIncludeTags.retainAll(names);
 
                 if(!quietMode) {
                     System.out.println("Starting tagging.");
                 }
 
                 TaggerController tc = new TaggerController(textData,forceIncludeTags,forceExcludeTags,sitesToCapture,
                         tagging,maxNumTags,findTags);
                 tc.runTagger();
 
                 while(!tc.isTaggingCompleted()) {
                     try {
                         Thread.sleep(100);
                     }catch(InterruptedException ie) {}
                 }
 
                 tc.saveResultsToFile(validateOutputFile(fileName + ".TAGS"));
                 tc.dumpTests(validateOutputFile(fileName + ".TESTS"));
                 //todo: I don't like this at the moment, removed subject to further consideration.
                 //tc.dumpTags(validateOutputFile(fileName + ".TAGSNPS"));
             }
 
 
 
         }
         catch(IOException e){
             System.err.println("An error has occured:");
             System.err.println(e.getMessage());
         }
         catch(HaploViewException e){
             System.err.println(e.getMessage());
         }
         catch(PedFileException pfe) {
             System.err.println(pfe.getMessage());
         }
         catch(TaggerException te){
             System.err.println(te.getMessage());
         }
     }
 
     public Haplotype[][] filterHaplos(Haplotype[][] haplos) {
         if (haplos == null){
             return null;
         }
         Haplotype[][] filteredHaplos = new Haplotype[haplos.length][];
         for (int i = 0; i < haplos.length; i++){
             Vector tempVector = new Vector();
             for (int j = 0; j < haplos[i].length; j++){
                 if (haplos[i][j].getPercentage() > Options.getHaplotypeDisplayThreshold()){
                     tempVector.add(haplos[i][j]);
                 }
             }
             filteredHaplos[i] = new Haplotype[tempVector.size()];
             tempVector.copyInto(filteredHaplos[i]);
         }
 
         return filteredHaplos;
 
     }
 }
