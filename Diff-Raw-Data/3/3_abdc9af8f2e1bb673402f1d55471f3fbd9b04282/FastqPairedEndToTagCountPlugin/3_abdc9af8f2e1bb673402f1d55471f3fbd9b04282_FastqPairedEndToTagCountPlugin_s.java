 package net.maizegenetics.gbs.pipeline;
 
 import java.awt.Frame;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import net.maizegenetics.util.MultiMemberGZIPInputStream;
 import javax.swing.ImageIcon;
 import net.maizegenetics.gbs.homology.ParseBarcodeRead;
 import net.maizegenetics.gbs.homology.ReadBarcodeResult;
 import net.maizegenetics.gbs.tagdist.TagCountMutable;
 import net.maizegenetics.gbs.tagdist.TagsByTaxa.FilePacking;
 import net.maizegenetics.gbs.util.ArgsEngine;
 import net.maizegenetics.plugindef.AbstractPlugin;
 import net.maizegenetics.plugindef.DataSet;
 import net.maizegenetics.util.DirectoryCrawler;
 import org.apache.log4j.Logger;
 
 /** 
  * Derives a tagCount list for each fastq file in the input directory.
  *
  * Keeps only good reads having a barcode and a cut site and no N's in the
  * useful part of the sequence.  Trims off the barcodes and truncates sequences
  * that (1) have a second cut site, or (2) read into the common adapter.
  * 
  */
 public class FastqPairedEndToTagCountPlugin extends AbstractPlugin {  
     static long timePoint1;
     private ArgsEngine engine = null;
     private Logger logger = Logger.getLogger(FastqPairedEndToTagCountPlugin.class);
 // DEPENDING ON APPROACH, MAY NEED TO ADD ADDITIONAL DIRECOTORYnAME, OR
 // LOOP THROUGH THIS ONE TWICE
     String directoryName=null;
     String keyfile=null;
     String enzyme = null;
     int maxGoodReads = 200000000;
     int minCount =1;
     String outputDir=null;
 
     public FastqPairedEndToTagCountPlugin() {
         super(null, false);
     }
 
     public FastqPairedEndToTagCountPlugin(Frame parentFrame) {
         super(parentFrame, false);
     }
 
     private void printUsage(){
         logger.info(
              "\n\nUsage is as follows:\n"
             + " -i  Input directory containing FASTQ files in text or gzipped text.\n"
             + "     NOTE: Directory will be searched recursively and should\n"
             + "     be written WITHOUT a slash after its name.\n\n"
             + " -k  Key file listing barcodes distinguishing the samples\n"
             + " -e  Enzyme used to create the GBS library, if it differs from the one listed in the key file.\n"
             + " -s  Max good reads per lane. (Optional. Default is 200,000,000).\n"
             + " -c  Minimum tag count (default is 1).\n"
             + " -o  Output directory to contain .cnt files (one per FASTQ file, defaults to input directory).\n\n"
         );
     }
 
     public DataSet performFunction(DataSet input){
         File qseqDirectory = new File(directoryName);
         if (!qseqDirectory.isDirectory()) {
             printUsage();
             throw new IllegalStateException("The input name you supplied is not a directory.");
         }
         countTags(keyfile, enzyme, directoryName, outputDir, maxGoodReads, minCount);  
         return null;
     }
 
     @Override
     public void setParameters(String[] args) {
         if(args.length==0) {
             printUsage();
             throw new IllegalArgumentException("\n\nPlease use the above arguments/options.\n\n");
         }
 //        try{
             if(engine == null){
                 engine = new ArgsEngine();
 //NEED TO MODIFY TO SET TO TWO FILE LOCATIONS
                 engine.add("-i", "--input-directory", true);
 //MAY NEED TO MODIFY TO TAKE IN TWO KEY FILES
                 engine.add("-k", "--key-file", true);
 //NEED TO MODIFY TO SET TWO ENZYMES
                 engine.add("-e", "--enzyme", true);
                 engine.add("-s", "--max-reads", true);
                 engine.add("-c", "--min-count", true);
                 engine.add("-o", "--output-file", true);
                 engine.parse(args);
             }
 //CREATE A NESTING LOOP THAT RUNS THROUGH THE TWO DIRECTORIES AND CHECKS
 // FOR THE TWO ENZYMES
             if (engine.getBoolean("-i")) { directoryName = engine.getString("-i");}
             else{ printUsage(); throw new IllegalArgumentException("Please specify the location of your FASTQ files."); }
 
             if(engine.getBoolean("-k")){ keyfile = engine.getString("-k");}
             else{ printUsage(); throw new IllegalArgumentException("Please specify a barcode key file.");}
 
             if(engine.getBoolean("-e")){ enzyme = engine.getString("-e"); }
             else{ 
                 System.out.println("No enzyme specified.  Using enzyme listed in key file.");
 //                printUsage(); throw new IllegalArgumentException("Please specify the enzyme used to create the GBS library.");
             }
         
 //END THE NEST LOOP HERE
             if(engine.getBoolean("-s")){ maxGoodReads = Integer.parseInt(engine.getString("-s"));}
 
             if (engine.getBoolean("-c")) { minCount = Integer.parseInt(engine.getString("-c"));}
 
             if(engine.getBoolean("-o")){ outputDir = engine.getString("-o");}
             else{outputDir = directoryName;}
 //        }catch (Exception e){
 //            System.out.println("Caught exception while setting parameters of "+this.getClass()+": "+e);
 //        }
     }
 
     
     /**
      * Derives a tagCount list for each fastq file in the fastqDirectory.
      *
      * @param keyFileS        A key file (a sample key by barcode, with a plate map included).
      * @param enzyme          The enzyme used to create the library (currently ApeKI or PstI).
      * @param fastqDirectory  Directory containing the fastq files (will be recursively searched).
      * @param outputDir       Directory to which the tagCounts files (one per fastq file) will be written.
      * @param maxGoodReads    The maximum number of barcoded reads expected in a fastq file
      * @param minCount        The minimum number of occurrences of a tag in a fastq file for it to be included in the output tagCounts file
      */
     public static void countTags(String keyFileS, String enzyme, String fastqDirectory, String outputDir, int maxGoodReads, int minCount) {
         BufferedReader br1;
         BufferedReader br2;;
 //COUNTER VARIABLE
         String[] countFileNames = null;
 
         
         /* Grab ':' delimited key files */
         String[] tempFileList = keyFileS.split(":");
         String[] keyFileList = new String[2];
         
         if (tempFileList.length == 0){
         	System.out.println("No key file given");
         	keyFileList[0] = "GBS.key"; // = NULL ?
         	keyFileList[1] = "GBS.key"; // = NULL ?
         	
         } else if (tempFileList.length == 1){
         	System.out.println("Only one key file given");
         	keyFileList[0] = tempFileList[0];
         	keyFileList[1] = tempFileList[0];
         	
         } else {
         	keyFileList[0] = tempFileList[0];
         	keyFileList[1] = tempFileList[1];
         }
 
  
 // THIS IS WHERE FILE INPUT NEEDS TO BE ADJUSTED, TRY TO
 // CONTINUE TO USE THE DIRECTORY CRAWLER AND PARSE THE OUTPUT TO READ1
 // AND READ2
  
         File inputDirectory = new File(fastqDirectory);
         File[] fastqFiles = DirectoryCrawler.listFiles("(?i).*\\.fq$|.*\\.fq\\.gz$|.*\\.fastq$|.*_fastq\\.txt$|.*_fastq\\.gz$|.*_fastq\\.txt\\.gz$|.*_sequence\\.txt$|.*_sequence\\.txt\\.gz$", inputDirectory.getAbsolutePath());
 // N.K. Code        File[] rawFastqFiles = DirectoryCrawler.listFiles("(?i).*\\.fq$|.*\\.fq\\.gz$|.*\\.fastq$|.*_fastq\\.txt$|.*_fastq\\.gz$|.*_fastq\\.txt\\.gz$|.*_sequence\\.txt$|.*_sequence\\.txt\\.gz$", inputDirectory.getAbsolutePath());
 //                                                      (?i) denotes case insensitive;                 \\. denotes escape . so it doesn't mean 'any char' & escape the backslash
         
         /* ----- Get only r1smp files ----- */
 /*        ArrayList<File> fastqFilesArray = new ArrayList<File>();
         if (rawFastqFiles.length == 0){
         	System.out.println("Couldn't find any files that end with \".fq\", \".fq.gz\", \".fastq\", \"_fastq.txt\", \"_fastq.gz\", \"_fastq.txt.gz\", \"_sequence.txt\", or \"_sequence.txt.gz\" in the supplied directory.");
         	return;
         }
         
         // Get array list of files with r1smp in them
         for (int i = 0; i < rawFastqFiles.length; i++){
         	String fileName = rawFastqFiles[i].getName();
         	
         	if (fileName.indexOf("r1smp") > -1){
         		fastqFilesArray.add(rawFastqFiles[i]);
         	}
         }
         // Cast to file array as following code relies on it
         File[] fastqFiles = fastqFilesArray.toArray(new File[fastqFilesArray.size()]);
 */        
         if(fastqFiles.length !=0 ){
         	Arrays.sort(fastqFiles);
             System.out.println("Using the following FASTQ files:");
             
             
 //COUNTS HOW MANY FILES THERE ARE IN THE INPUT            
             countFileNames = new String[fastqFiles.length];
             for (int i=0; i<fastqFiles.length; i++) {
                 countFileNames[i] = fastqFiles[i].getName().replaceAll
                     ("(?i)\\.fq$|\\.fq\\.gz$|\\.fastq$|_fastq\\.txt$|_fastq\\.gz$|_fastq\\.txt\\.gz$|_sequence\\.txt$|_sequence\\.txt\\.gz$", ".cnt");
 //                        \\. escape . so it doesn't mean 'any char' & escape the backslash    
                 System.out.println(fastqFiles[i].getAbsolutePath());
             }
         }
         
         int allReads=0, goodBarcodedReads=0;
         int numFastqFiles = fastqFiles.length;  //number of files
 System.out.println("numFastqFiles IS: "+numFastqFiles); //TESTING & DEBUG         
         int indexStartOfRead2 = numFastqFiles/2;
       //this chunk probably needs to be a separate private method
         if (indexStartOfRead2 % 2 !=0){
         System.out.println("There are an odd number of files so there won't be correct pairing"); 	
         }
         
 System.out.println("indexStartOfRead2 IS: "+indexStartOfRead2); //TESTING & DEBUG         
 		
 		/* sets mutildimensional array for 
 		 * [x][][] 2 bays for forward and reverse reads
 		 * [][x][] number of files expected for each directional read
 		 * [][][y] will hold the parsed elements of the file name
 		 */
 		String [][][] fileReadInfo=new String[2][indexStartOfRead2][5]; 
 
         /* Loop through all of the fastqFiles *
         for(int fileNum=0; fileNum<indexStartOfRead2; fileNum++) { // cap is set to indexStartOfRead2
         	//because files should be handled as pairs, so the counter doesn't need to iterate through
         	//all of the counted files
         	
         	/* Get second read file by name */
 //        	File read1 = fastqFiles[fileNum];
  //       	String read1Name = read1.getAbsolutePath();
 //        	int index = read1Name.indexOf("r1smp")+1;
         	
 //        	String read2Name = read1Name.substring(0, index) + "2" + read1Name.substring(index+1);
 //        	File read2 = new File(read2Name);
         	
         	/* Open output file, don't do work on input if corresponding output exists *
             File outputFile = new File(outputDir+File.separator+countFileNames[fileNum]);
             if(outputFile.isFile()){
                 System.out.println(
                         "An output file "+countFileNames[fileNum]+"\n"+ 
                         " already exists in the output directory for file "+fastqFiles[fileNum]+".  Skipping.");
                 continue;
             }
             
 //N.K code            System.out.println("Reading FASTQ files: "+fastqFiles[fileNum]+", "+read2Name);
             System.out.println("Reading FASTQ file: "+fastqFiles[fileNum]);
  System.out.println("fileNum IS: "+fileNum); //DEBUG 
  			
  			String[] filenameField=fastqFiles[fileNum].getName().split("_");
 System.out.println("fileField LEN IS: "+filenameField.length); //DEBUG 
 
 			System.arraycopy(filenameField, 0, fileReadInfo[0][fileNum], 0, filenameField.length);
         }
 		//DEBUG	
 */
 		
 		String[][][] filenameField= new String[2][5][];
 		int fileNum=0;
 		/* parses file name into array elements that correspond to reads and expected pairing
 		 * where the first file would be paired with the file that is half of the total number 
 		 * of files.
 		 * 
 		 * The outter loop controller is set to 2 because there should not be more than two 
 		 * directional reads, forward and reverse.
 		 */
 		for(int read=0; read<2; read++) {
 			int loopController, setStart; //control loops and arrays
 			int fileController=0; // resets to 0 so files are copied in correct array
 			
 			//set conditions for the loop
 			if(read==0){
 				setStart=0;
 				loopController=indexStartOfRead2;
 			}
 			else{
 				setStart=indexStartOfRead2;
 				loopController=numFastqFiles;
 			}
 				
 				for(fileNum=setStart; fileNum<loopController; fileNum++) {			
 					//following block could be set as separate private method
 					File outputFile = new File(outputDir+File.separator+countFileNames[fileNum]);
 						if(outputFile.isFile()){
 				            System.out.println(
 				                    "An output file "+countFileNames[fileNum]+"\n"+ 
 				                    " already exists in the output directory for file "+fastqFiles[fileNum]+".  Skipping.");
 				            continue;
 				        }
 					System.out.println("Reading FASTQ file: "+fastqFiles[fileNum]);//print
 					filenameField[read][fileNum]=fastqFiles[fileNum].getName().split("_");
 					System.arraycopy(filenameField[read][fileNum], 0, fileReadInfo[read][fileController], 0, filenameField[read][fileNum].length);
 					fileController++;
 					}
 		}
 		
 fileNum=0;
 //DEBUG print all array contents
 for(int left=0;left<2;left++){
 	for(int mid=0;mid<indexStartOfRead2;mid++){
 		for(int right=0;right<5;right++){
 			System.out.println("fileReadInfo FOR ["+left+"]"+"["+mid+"]"+"["+right+"]"+"IS: "+fileReadInfo[left][mid][right]); //DEBUG 
 		}
 	}
 }
 
 //handle keyfiles and enzymes
 //2 arrays for manually inputing multiple enzymes and keys for testing
 System.out.println("OLD Key file is:"+ keyFileS);            
 System.out.println("OLD enzyme is:"+ enzyme);
 //String[] hcEnzyme={"PstI","MspI"};
 String[] hcEnzyme={"PstI-MspI","MspI-PstI"};
 String[] hcKeyFiles={"GBS.key","GBS2.key"};
 
 /*
 if(fileReadInfo[0][b][0].contains("1")){
 	keyFileS=hcKeyFiles[0];
 	enzyme=hcEnzyme[0];
 	System.out.println("NEW Key file is:" + keyFileS);
 	System.out.println("NEW enzyme is:"+ enzyme);
 	}
 else{
 	keyFileS=hcKeyFiles[1];
 	enzyme=hcEnzyme[1];
 	System.out.println("NEW Key file is:"+ keyFileS);
 	System.out.println("NEW enzyme is:"+ enzyme);}
 */
 		
  			TagCountMutable [] theTC=new TagCountMutable [2];
  			/* 
  			 * Reads the key file and store the expected barcodes for a lane.
  			 * Set to a length of 2 to hold up to two key files' worth of information.
  			 * The convention will be that the forward read is [0] and the reverse
  			 * read is[1]
  			 */
             ParseBarcodeRead [] thePBR = new ParseBarcodeRead [2];  
           //  String[][] taxaNames=new String[2][];
             /*
             * Need to adjust this loop to read matching pairs simultaneously
             */
             for(int b=0;b<indexStartOfRead2;b++){
 
 				if(fileReadInfo[0][b].length==5) {
 					thePBR[0]=new ParseBarcodeRead(
 							hcKeyFiles[0], hcEnzyme[0], fileReadInfo[0][b][1], fileReadInfo[0][b][3]);
 				}
 				else {
 				System.out.println("Error in parsing file name:");
 				System.out.println("   The filename does not contain a 5 underscore-delimited value.");
 				System.out.println("   Expect: code_flowcell_s_lane_fastq.txt.gz");
 				System.out.println("   Filename: "+fileReadInfo[0][b]);
 				continue;
 				}
 				
 				
 				if(fileReadInfo[1][b].length==5) {
 					thePBR[1]=new ParseBarcodeRead(
 							hcKeyFiles[1], hcEnzyme[1], fileReadInfo[1][b][1], fileReadInfo[1][b][3]);	
 				}
 				else {
 				System.out.println("Error in parsing file name:");
 				System.out.println("   The filename does not contain a 5 underscore-delimited value.");
 				System.out.println("   Expect: code_flowcell_s_lane_fastq.txt.gz");
 				System.out.println("   Filename: "+fileReadInfo[1][b]);
 				continue;
 				}
 	
 				
 				System.out.println("\nTotal barcodes found in lane:"+thePBR[0].getBarCodeCount());
 				if(thePBR[0].getBarCodeCount() == 0){
 	                System.out.println("No barcodes found.  Skipping this flowcell lane."); continue;
 	            }
 				
 				System.out.println("\nTotal barcodes found in lane:"+thePBR[1].getBarCodeCount());
 				if(thePBR[1].getBarCodeCount() == 0){
 	                System.out.println("No barcodes found.  Skipping this flowcell lane."); continue;
 	            }
 				/* as far as I can tell, this bit of code is not used anywhere downstream.
 				taxaNames[0]=new String[thePBR[0].getBarCodeCount()];
 	            taxaNames[1]=new String[thePBR[1].getBarCodeCount()];
 	            
 	            for (int i = 0; i < taxaNames[0].length; i++) {
 	                taxaNames[0][i]=thePBR[0].getTheBarcodes(i).getTaxaName();
 	            }
 	            for (int i = 0; i < taxaNames[0].length; i++) {
 	                taxaNames[1][i]=thePBR[1].getTheBarcodes(i).getTaxaName();
 	            }
 				*/
 				
 				/*
 				 * NEED TO CHANGE allgoodreads TO REFLECT AND REPORT EACH DIRECTION SEPARATELY.
 				 * CAN ADD THEM TOGETHER AT THE END FOR A GRAND TOTAL IN CASE THAT'S USEFUL.
 				 */
 	            try{
 	                //Read in qseq file as a gzipped text stream if its name ends in ".gz", otherwise read as text
 	                if(fastqFiles[b].getName().endsWith(".gz")){
 	                    br1 = new BufferedReader(new InputStreamReader(
 	                    						new MultiMemberGZIPInputStream(
 	                    						new FileInputStream(fastqFiles[b]))));
 	                    br2 = new BufferedReader(new InputStreamReader(
         										new MultiMemberGZIPInputStream(
         										new FileInputStream(fastqFiles[b+indexStartOfRead2]))));
 	                }else{
 	                    br1=new BufferedReader(new FileReader(fastqFiles[b]),65536);
 	                    br2=new BufferedReader(new FileReader(fastqFiles[b+indexStartOfRead2]),65536);
 	                }
 	                String sequenceF="", sequenceR="", qualityScoreF="", qualityScoreR="";
 	                String tempF, tempR;
 	
 	                try{
 	                    theTC[0] = new TagCountMutable(2, maxGoodReads);
 	                    theTC[1] = new TagCountMutable(2, maxGoodReads);
 	                }catch(OutOfMemoryError e){
 	                    System.out.println(
 	                        "Your system doesn't have enough memory to store the number of sequences"+
 	                        "you specified.  Try using a smaller value for the minimum number of reads."
 	                    );
 	                }
 	                
 	                int currLine=0;
 	                allReads = 0;
 	                goodBarcodedReads = 0;
 	                ReadBarcodeResult [] rr = new ReadBarcodeResult [2];
 	                while (((tempF = br1.readLine()) != null && (tempR = br2.readLine()) != null) 
 	                		&& goodBarcodedReads < maxGoodReads) {
 	                    currLine++;
 	                    try{
 	                        //The quality score is every 4th line; the sequence is every 4th line starting from the 2nd.
 	                        if((currLine+2)%4==0){
 	                            sequenceF = tempF;
 	                            sequenceR = tempR;
 	                        }else if(currLine%4==0){
 	                            qualityScoreF = tempF;
 	                            qualityScoreR = tempR;
 	                            allReads += 2;
 	                            //After quality score is read, decode barcode using the current sequence & quality  score
 	                            rr[0] = thePBR[0].parseReadIntoTagAndTaxa(sequenceF, qualityScoreF, true, 0);
 	                            rr[1] = thePBR[1].parseReadIntoTagAndTaxa(sequenceR, qualityScoreR, true, 0);
 	                            if (rr[0] != null && rr[1] !=null){
 	                                goodBarcodedReads+=2;
 	                                theTC[0].addReadCount(rr[0].getRead(), rr[0].getLength(), 1);
 	                                theTC[1].addReadCount(rr[1].getRead(), rr[1].getLength(), 1);
 	                            }
 	                            else if (rr[0] != null){
 	                                goodBarcodedReads++;
 	                                theTC[0].addReadCount(rr[0].getRead(), rr[0].getLength(), 1);
 	                            }
 	                            else if (rr[1] != null){
 	                                goodBarcodedReads++;
 	                                theTC[1].addReadCount(rr[1].getRead(), rr[1].getLength(), 1);
 	                            }
 	                            /*
 	                             * changed if conditional from 1000000 to 10000000
 	                             * Not sure if allgoodreads variable is giving the same information when paired files
 	                             * are being processed
 	                             */
 	                            if (allReads % 10000000 == 0) {
 	                                System.out.println("Total Reads:" + allReads + " Reads with barcode and cut site overhang:" + goodBarcodedReads);
 	                            }
 	                        }
 	                    }catch(NullPointerException e){
 	                        System.out.println("Unable to correctly parse the sequence and "
 	                        + "quality score from fastq file.  Your fastq file may have been corrupted.");
 	                        System.exit(0);
 	                    }
 	                }
 	                /*
 	                 * Not sure if allgoodreads variable is giving the same information when paired files
 	                 * are being processed
 	                 */
                 System.out.println("Total number of reads in lane=" + allReads);
                 System.out.println("Total number of good barcoded reads=" + goodBarcodedReads);
                 System.out.println("Timing process (sorting, collapsing, and writing TagCount to file).");
                 timePoint1 = System.currentTimeMillis();
                 theTC[0].collapseCounts();
                 theTC[1].collapseCounts();
                 theTC[0].writeTagCountFile(outputDir+File.separator+countFileNames[b], FilePacking.Bit, minCount);
                 theTC[1].writeTagCountFile(outputDir+File.separator+countFileNames[b+indexStartOfRead2], FilePacking.Bit, minCount);
                 System.out.println("Process took " + (System.currentTimeMillis() - timePoint1) + " milliseconds.");
                 br1.close();
                 br2.close();
                 fileNum++;
             
 		        } catch(Exception e) {
 		            System.out.println("Catch testBasicPipeline c="+goodBarcodedReads+" e="+e);
 		            e.printStackTrace();
 		            System.out.println("Finished reading "+(fileNum+1)+" of "+fastqFiles.length+" sequence files.");
         			}
             	
             }
     }
     @Override
     public ImageIcon getIcon(){
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String getButtonName() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String getToolTipText() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
 }
