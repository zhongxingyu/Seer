 package edu.ucdenver.ccp.iDecoder;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Map;
 
 /**
  * Extends InputFileParser to handle RGD input files.
  *
  */
 public class RGDParser extends InputFileParser {
     private static final String INPUT_SOURCE = "RGD";
     private static final String ORGANISM_NAME = "Rattus norvegicus";
     
     // Input columns are tab-delimited
     private static final String INPUT_COLUMN_DELIM = "\t";
     
     // List (multi-item) columns have commas delimiting the items
     private static final String INPUT_LIST_DELIM = ",";
 
     // See splitListColumn() below
     private static final String[] EMPTY_STRING_ARRAY = new String[0];
 
     // key names for file properties entries
     private static final String RGD_INPUT_DIRECTORY_PROP_NM = "rgdInputDirectory";
     private static final String RGD_GENE_FILENAME_PROP_NM = "rgdGeneFilename";
 
     public RGDParser(String outputDirectory) {
         super(outputDirectory);
     }
     
     // javadoc in superclass
     public String getInputSource() {
         return INPUT_SOURCE;
     }
     
     // javadoc in superclass
     public void processAllInputFiles() throws IOException {
         final String taxonID = (String) PropertiesHelper.getOrganismToTaxonIDMap().get(ORGANISM_NAME);
         final Map fileMap = PropertiesHelper.getFileMap();
             
         String inputDirectory = createFileSpec((String) fileMap.get(INPUT_DIRECTORY),
                                 (String) fileMap.get(RGD_INPUT_DIRECTORY_PROP_NM));
 
         // RGD has two input files, each with a different format
         String geneFilename = (String) fileMap.get(RGD_GENE_FILENAME_PROP_NM);
 
         initializeOutputFiles();
         
         long startTime = System.currentTimeMillis();
         String fileSpec = createFileSpec(inputDirectory,geneFilename);
         processGeneFile(fileSpec, taxonID);
         System.out.println("processed " + fileSpec + "\t"
                 + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
 
         finalizeOutputFiles();
     }
 
     /*
      * The GENES_RAT input file contains one RGD ID and all of it's information,
      * including links to other databases, per line. The columns are
      * tab-delimited. Some columns may contain more than one value; these list
      * (or multi-item) columns use a comma as the list item delimiter.
      * There are 3 sets of columns that contain chromosome, start, end, and strand information:
      * xxx_CELERA, xxx_31, and xxx_34.  We are using the xxx_34 columns which are the 
      * Baylor 3.4 Assembly
      * 
      * The relevant columns from the input file are:
      * 1st  - RGD ID
      * 2nd  - Gene Symbol
      * 7th  - Chromosome
      * 15th - Start Position
      * 16th - Stop Position
      * 17th - Strand
      * 21st - link Entrez Gene IDs
      * 22nd - link SwissProt IDs
      * 24th - link NCBI RNA IDs (including RefSeq and non-RefSeq)
      * 26th - link NCBI Protein IDs (including RefSeq and non-RefSeq)
      * 27th - link UniGene IDs
      * 38th - link Ensembl IDs
      * 
      * RGD IDs, Gene Symbols, and Ensembl IDs are written to the Info file. In
      * addition, Links file entries are created linking the RGD IDs to Gene
      * Symbols and Ensembl IDs. For all other columns, entries are created linking
      * RGD IDs to the other column IDs (e.g., Entrez, SwissProt, etc.).
      */
     private void processGeneFile(String inputFilename, String taxonID) throws IOException {
         BufferedReader reader = createInputFileReader(inputFilename);
         String line;
         String[] columns;
         
 	// Skip the first line
         line = reader.readLine();
         while (reader.ready()) {
             line = reader.readLine();
 		//writeToDebugFile(line);
 		if (line.substring(0,1).equals("#")) {
             		writeToErrorFile(line, "Comment");
 			continue;
 		}
             columns = line.split(INPUT_COLUMN_DELIM);
             
             String rgdID = columns[0].trim();
 
             // If line contains column names (usually first line), skip it
             if (rgdID.equals("GENE_RGD_ID")) continue;
             
             String geneSymbol = columns[1].trim();
             String chromosome = columns[6].trim(); 
 
 		// columns[14] == start base pair
 		// columns[15] == end base pair
 		// columns[16] == strand (- or +)
 
             String mapLocation = getMapLocation(columns[14], columns[15], columns[16].trim());
             
             // The following columns may contain multiple items
             String[] entrezGeneIDs = splitListColumn(columns[20]);
             String[] swissProtIDs = splitListColumn(columns[21]);
             String[] genBankRNAIDs = splitListColumn(columns[23]);
             String[] genBankProteinIDs = splitListColumn(columns[25]);
             String[] unigeneIDs = splitListColumn(columns[26]);
             String[] ensemblIDs;
             
             // if columns.length < 38, the last column (Ensembl ID) is missing
             // from the input file
             if (columns.length < 38) {
                 // since there are no EnsemblIDs, set the variable to an empty array,
                 // which is what splitListColumn() would do
                 ensemblIDs = EMPTY_STRING_ARRAY;
             } else {
                 //ensemblIDs = columns[37].split(INPUT_LIST_DELIM);
                 ensemblIDs = splitListColumn(columns[37]);
             }
             
             // Write RGD Info
             writeToInfoFile(taxonID, RGD_ID_TYPE, rgdID, chromosome, mapLocation);
             
             // Write Gene Symbol and Ensembl ID Info and Links
             if (!geneSymbol.equals("")) {
                 writeToInfoFile(taxonID, "Gene Symbol", geneSymbol, chromosome, mapLocation);
                 writeToLinksFile(RGD_ID_TYPE,rgdID,"Gene Symbol",geneSymbol);
             }
             for (int i = 0; i < ensemblIDs.length; i++) {
                 writeToInfoFile(taxonID, "Ensembl ID", ensemblIDs[i], chromosome, mapLocation);
                 writeToLinksFile(RGD_ID_TYPE,rgdID,"Ensembl ID",ensemblIDs[i]);
             }
             
             // Handle NCBI RefSeq and non-RefSeq IDs (both RNA and Protein)
             for (int i = 0; i < genBankRNAIDs.length; i++) {
                 String genBankRNAID = genBankRNAIDs[i];
                 if (isRefSeq(genBankRNAID)) {
                     writeToLinksFile(RGD_ID_TYPE,rgdID,"RefSeq RNA ID",genBankRNAIDs[i]);
                 } else {
                     writeToLinksFile(RGD_ID_TYPE,rgdID,"NCBI RNA ID",genBankRNAIDs[i]);
                 }
             }
             for (int i = 0; i < genBankProteinIDs.length; i++) {
                 String genBankProteinID = genBankProteinIDs[i];
                 if (isRefSeq(genBankProteinID)) {
                     writeToLinksFile(RGD_ID_TYPE,rgdID,"RefSeq Protein ID",genBankProteinIDs[i]);
                 } else {
                     writeToLinksFile(RGD_ID_TYPE,rgdID,"NCBI Protein ID",genBankProteinIDs[i]);
                 }
             }            
             
             // create Links entries for the rest
             writeCollectionToLinksFile(RGD_ID_TYPE,rgdID,"Entrez Gene ID",Arrays.asList(entrezGeneIDs));
             writeCollectionToLinksFile(RGD_ID_TYPE,rgdID,"SwissProt ID",Arrays.asList(swissProtIDs));
             writeCollectionToLinksFile(RGD_ID_TYPE,rgdID,"UniGene ID",Arrays.asList(unigeneIDs));
         }   
         reader.close();
     }
 
 
     // Some columns can contain a list of items. However, those same columns may
     // be empty. String.split() always returns an array containing at least one
     // element, even if the column is empty (i.e., the array contains one empty
     // element). Since this isn't really what's desired here, if a list column
     // is actually empty, return an empty array.
     private String[] splitListColumn(String column) {
         if (column.trim().equals("")) {
             return EMPTY_STRING_ARRAY;
         } else {
            String delim=";";
            if(column.indexOf(";")<0 && column.indexOf(",")>-1){
                delim=",";
            }
            return column.split(delim);
         }
     }
 
     // Strip the 'Chr' off the chromosome number
     private String getChromosome(String column) {
         if (column.substring(0,3).equals("Chr")) {
             return column.substring(3);
         } else {
             return column;
         }
     }
 
     // Get the RGD ID from the string containing 'Notes'
     // First find the "RGD:" substring and then get the value starting after that up to the next double-quote
     private String getRgdID(String column) {
 	int startPlace = column.indexOf("RGD:") + 4;
 	int endPlace = column.substring(startPlace).indexOf("\"");
 	return column.substring(startPlace, startPlace + endPlace);
     }
     
     
 }
