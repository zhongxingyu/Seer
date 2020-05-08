 package nzgot.cma.io;
 
 import nzgot.cma.Community;
 import nzgot.cma.OTU;
 import nzgot.cma.util.NameParser;
 import nzgot.cma.util.NameSpace;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.TreeSet;
 
 /**
  * Community Matrix Importer
  * @author Walter Xie
  */
 public class CMImporter {
 
     // column index in OTU mapping file
     public static int OTU_MAPPING_INDEX_READ = 0;
     public static int OTU_MAPPING_INDEX_OTU_NAME = 1;
     // column index in reference sequence mapping file
     public static int REF_SEQ_MAPPING_INDEX_IDENTITY = 0;
     public static int REF_SEQ_MAPPING_INDEX_OTU_NAME = 1;
     public static int REF_SEQ_MAPPING_INDEX_REF_SEQ = 2;
 
     public static void importOTUs (File otusFile, Community community) throws IOException, IllegalArgumentException {
         BufferedReader reader = new BufferedReader(new FileReader(otusFile));
 
         System.out.println("\nImport OTUs from file: " + otusFile);
 
         OTU otu = null;
         String line = reader.readLine();
         while (line != null) {
             if (line.startsWith(">")) {
 //                line.replaceAll("size=", "");
                 // the current label only contains otu name
                 String otuName = line.substring(1);
                 otu = new OTU(otuName);
 
                 community.addUniqueElement(otu);
 
             } else {
                 // TODO add ref sequence
             }
 
             line = reader.readLine();
         }
 
         reader.close();
     }
 
     /**
      * 1st load mapping OTU - Reads,
      * 2nd initialize samples array in Community, which are sample locations
      * 3rd calculate Alpha diversity for each OTU
      * @param otuMappingFile
      * @param community
      * @return
      * @throws IOException
      * @throws IllegalArgumentException
      */
     public static TreeSet<String> importOTUMapping (File otuMappingFile, Community community) throws IOException, IllegalArgumentException {
         TreeSet<String> samples = new TreeSet<>();
         NameParser nameParser = NameParser.getInstance();
 
         BufferedReader reader = new BufferedReader(new FileReader(otuMappingFile));
 
         System.out.println("\nImport OTU mapping (to reads) file: " + otuMappingFile);
 
        // 1st
         String line = reader.readLine();
         while (line != null) {
             // 2 columns: 1st -> read id, 2nd -> otu name
             String[] fields = line.split(NameParser.SEPARATOR_COLUMN, -1);
 
             if (fields.length < 2) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);
 
             OTU otu = (OTU) community.getUniqueElement(fields[OTU_MAPPING_INDEX_OTU_NAME]);
             if (otu == null) {
                 throw new IllegalArgumentException("Error: find an invalid OTU " + fields[1] +
                         ", from the mapping file which does not exist in OTUs file !");
             } else {
                 otu.addUniqueElement(fields[OTU_MAPPING_INDEX_READ]);
 
                 // if by plot, then add plot to TreeSet, otherwise add subplot
                 String sampleLocation = nameParser.getSampleBy(community.getSamplesBy(), fields[OTU_MAPPING_INDEX_READ]);
                 samples.add(sampleLocation);
             }
 
             line = reader.readLine();
         }
 
         reader.close();
 
        // 2nd
         community.initSamples(samples);
        // 3rd
         community.setAlphaDiversity();
 
         return samples;
     }
 
     public static void importReferenceMappingFile (File referenceMappingFile, Community community) throws IOException, IllegalArgumentException {
 
         BufferedReader reader = new BufferedReader(new FileReader(referenceMappingFile));
 
         System.out.println("\nImport reference sequence mapping (to OTU) file: " + referenceMappingFile);
 
         String line = reader.readLine();
         while (line != null) {
             // 3 columns: 1st -> identity %, 2nd -> otu name, 3rd -> reference sequence id
             String[] fields = line.split(NameParser.SEPARATOR_COLUMN, -1);
 
             if (fields.length < 3) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);
 
             OTU otu = (OTU) community.getUniqueElement(fields[REF_SEQ_MAPPING_INDEX_OTU_NAME]);
             if (otu == null) {
                 throw new IllegalArgumentException("Error: find an invalid OTU " + fields[1] +
                         ", from the mapping file which does not exist in OTUs file !");
             } else {
                 otu.setRefSeqId(fields[REF_SEQ_MAPPING_INDEX_REF_SEQ]);
             }
 
             line = reader.readLine();
         }
 
         reader.close();
     }
 
     public static boolean isOTUsFile(String fileName) {
         return fileName.startsWith(NameSpace.PREFIX_OTUS_RELABELED) && fileName.endsWith(NameSpace.POSTFIX_OTUS);
     }
 
     public static boolean isOTUMappingFile(String fileName) {
         return fileName.startsWith(NameSpace.PREFIX_OTU_MAPPING) && fileName.endsWith(NameSpace.POSTFIX_MAPPING);
     }
 
     public static boolean isReferenceMappingFile(String fileName) {
         return fileName.startsWith(NameSpace.PREFIX_OTU_REFERENCE) && fileName.endsWith(NameSpace.POSTFIX_MAPPING);
     }
 }
