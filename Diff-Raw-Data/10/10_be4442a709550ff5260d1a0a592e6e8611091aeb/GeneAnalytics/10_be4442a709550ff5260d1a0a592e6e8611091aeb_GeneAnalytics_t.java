 package bioGUI;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Scanner;
 
 public class GeneAnalytics {
 
     static ArrayList<String> mFasta, mGFF;
     static int numFiles = 0;
     static Scanner fastaScanner;
     static Scanner gffScanner;
     static StringBuilder fastaText;
 
     public GeneAnalytics() {
     }
 
     //need to support multiple fasta and gff files later
     //	fasta and gff files should have a 1:1 correspondence,
     //	ie, fasta1 is paired with gff1, etc
     static String work(ArrayList<String> fastaFiles, ArrayList<String> gffFiles) throws Exception {
         String output = "";
         mFasta = fastaFiles;
         mGFF = gffFiles;
 
         if (mFasta.size() != mGFF.size()) {
             throw new Exception();
         } else {
             numFiles = mFasta.size();
         }
 
 
 
         for (int i = 0; i < numFiles; i++) {
             //build output string for a particular gff/fasta file combination
             fastaScanner = new Scanner(new File(mFasta.get(i)));
             fastaText = new StringBuilder();
 
             String nextLine = "";
 
             try {
                 fastaScanner.nextLine(); // throw out first line
                 while (fastaScanner.hasNextLine()) {
                     nextLine = fastaScanner.nextLine();
                     fastaText.append(nextLine);
                 }
             } finally {
                 fastaScanner.close();
             }
 
             gffScanner = new Scanner(new File(mGFF.get(i)));
             output += " " + avgGeneSpan();
 
             gffScanner = new Scanner(new File(mGFF.get(i)));
             output += " " + avgCDSLength();
 
             gffScanner = new Scanner(new File(mGFF.get(i)));
             output += " " + avgExonSize();
 
             gffScanner = new Scanner(new File(mGFF.get(i)));
             output += " " + avgIntronSize();
 
             gffScanner = new Scanner(new File(mGFF.get(i)));
             output += " " + avgIntergenicSize();
 
             gffScanner = new Scanner(new File(mGFF.get(i)));
             output += " " + avgNucDensitySpan();
 
             gffScanner = new Scanner(new File(mGFF.get(i)));
             output += " " + avgNucDensityRegion();
 
             gffScanner = new Scanner(new File(mGFF.get(i)));
             output += " " + cdsNucProportion();
 
             gffScanner = new Scanner(new File(mGFF.get(i)));
             output += " " + genesPerKB(10);
             gffScanner = new Scanner(new File(mGFF.get(i)));
 
             gffScanner = new Scanner(new File(mGFF.get(i)));
             output += " " + kbPerGene();
 
             gffScanner = new Scanner(new File(mGFF.get(i)));
             output += " " + predictProtein();
 
         }
         System.out.println("output " + output);
         return output;
     }
 
     static HashMap<String, String> processGFFLine(String line) {
         Scanner lineScanner = new Scanner(line);
         HashMap<String, String> parsedGFF = new HashMap<String, String>();
 
         //Getting string values for each column
         parsedGFF.put("seqName", lineScanner.next());
         parsedGFF.put("progName", lineScanner.next());
         parsedGFF.put("featureType", lineScanner.next());
         parsedGFF.put("startNuc", lineScanner.next());
         parsedGFF.put("endNuc", lineScanner.next());
         parsedGFF.put("score", lineScanner.next());
         parsedGFF.put("strand", lineScanner.next());
         parsedGFF.put("frameNum", lineScanner.next());
 
         //getting rid of semicolon at end of gene id
         lineScanner.next();
         String geneId = lineScanner.next();
         parsedGFF.put("geneId", geneId.substring(0, geneId.length() - 1));
         //breaking up values in col 9
         lineScanner.next();
         String transcriptId = lineScanner.next();
         parsedGFF.put("transcriptId", transcriptId.substring(0, transcriptId.length() - 1));
 
         return parsedGFF;
     }
 
     /*1.	Average gene span per contig/fosmid.
      * Average number of nucleotides between the highest position
      * indicated and the lowest position indicated in all features
      *  with the same gene_id in column 9
      */
     static double avgGeneSpan() {
 
         //highest pos stored as geneId : highestPos
         HashMap<String, Integer> highestPos = new HashMap<String, Integer>();
 
         //lowest pos stored as geneId : lowestPos
         HashMap<String, Integer> lowestPos = new HashMap<String, Integer>();
 
         HashMap<String, String> processedGFFLine;
 
         while (gffScanner.hasNext()) {
             String line = gffScanner.nextLine();
             processedGFFLine = processGFFLine(line);
             String geneId = processedGFFLine.get("geneId");
             Integer startNuc = new Integer(processedGFFLine.get("startNuc"));
            
            System.out.println("strand: "+processedGFFLine.get("strand"));
            
            if(processedGFFLine.get("strand").equals("-"))
            	startNuc -= 3;
            
             Integer endNuc = new Integer(processedGFFLine.get("endNuc"));
            
            if(processedGFFLine.get("strand").equals("+"))
            	endNuc -= 3;
 
             if (highestPos.containsKey(geneId) && highestPos.get(geneId) < endNuc) {
                 highestPos.put(geneId, endNuc);
             } else if (!highestPos.containsKey(geneId)) {
                 highestPos.put(geneId, endNuc);
             }
 
             if (lowestPos.containsKey(geneId) && lowestPos.get(geneId) > startNuc) {
                 lowestPos.put(geneId, startNuc);
             } else if (!lowestPos.containsKey(geneId)) {
                 lowestPos.put(geneId, startNuc);
             }
         }
         /*
         System.out.println("lowestPos:" + lowestPos);
         System.out.println("highestPos:" + highestPos);
          */
         ArrayList<Integer> geneIdLengths = new ArrayList<Integer>();
 
         for (String geneId : highestPos.keySet()) {
             geneIdLengths.add(highestPos.get(geneId) - lowestPos.get(geneId));
         }
 
 
         double totalLength = 0;
         for (Integer i : geneIdLengths) {
             totalLength += i;
         }
 
         double averageGeneSpan = totalLength / geneIdLengths.size();
 
         return averageGeneSpan;
     }
 
     /*2.	Average length of CDS per contig/fosmid.
      * Average coding DNA sequence size, sum of the
      *  CDS regions with the same gene_id (see Figure 1 below)
      */
     static double avgCDSLength() {
         String output = "";
 
         HashMap<String, String> processedGFFLine;
 
         //stored as geneId : cdsLength
         HashMap<String, Integer> cdsLength = new HashMap<String, Integer>();
 
         while (gffScanner.hasNext()) {
             String line = gffScanner.nextLine();
             processedGFFLine = processGFFLine(line);
             String geneId = processedGFFLine.get("geneId");
             Integer startNuc = new Integer(processedGFFLine.get("startNuc"));
             Integer endNuc = new Integer(processedGFFLine.get("endNuc"));
             String featureType = processedGFFLine.get("featureType");
 
             //			System.out.println("featureType:" + featureType);
 
             if (featureType.equals("CDS")) {
                 if (cdsLength.containsKey(geneId)) {
                     cdsLength.put(geneId, cdsLength.get(geneId) + endNuc - startNuc);
                 } else {
                     cdsLength.put(geneId, endNuc - startNuc);
                 }
             }
         }
 
         ArrayList<Integer> geneIdLengths = new ArrayList<Integer>();
 
         for (String geneId : cdsLength.keySet()) {
             geneIdLengths.add(cdsLength.get(geneId));
         }
 
         double totalLength = 0;
         for (Integer i : geneIdLengths) {
             totalLength += i;
         }
 
         //		System.out.println("totalLength: "+totalLength);
         //		System.out.println("geneIdLengths.size(): "+geneIdLengths.size());
 
         double avgCDSLength = totalLength / geneIdLengths.size();
 
         return avgCDSLength;
     }
 
     /*3.	Average size of exon per contig/fosmid.
      * Average number of nucleotides between the first
      * position and the last position indicated in features
      * marked CDS in the GFF files.
      */
     static double avgExonSize() {
         String output = "";
 
         HashMap<String, String> processedGFFLine;
 
         ArrayList<Integer> exonLength = new ArrayList<Integer>();
 
         while (gffScanner.hasNext()) {
             String line = gffScanner.nextLine();
             processedGFFLine = processGFFLine(line);
             Integer startNuc = new Integer(processedGFFLine.get("startNuc"));
             Integer endNuc = new Integer(processedGFFLine.get("endNuc"));
             String featureType = processedGFFLine.get("featureType");
 
             //			System.out.println("featureType:" + featureType);
 
             if (featureType.equals("CDS")) {
                 exonLength.add(endNuc - startNuc);
             }
         }
 
         double totalLength = 0;
         for (Integer i : exonLength) {
             totalLength += i;
         }
 
         //		System.out.println("totalLength: "+totalLength);
         //		System.out.println("geneIdLengths.size(): "+geneIdLengths.size());
 
         double avgExonLength = totalLength / exonLength.size();
 
         return avgExonLength;
     }
 
     /*
      * 4.	Average size of intron per contig.
      *  Average number of nucleotides between
      *  the last position of a CDS region and
      *  the first position of the subsequent
      *  CDS region with the same transcript_id
      *  (see Figure 1 below)
      */
     static double avgIntronSize() {
         String output = "";
 
         HashMap<String, String> processedGFFLine;
 
         HashMap<String, ArrayList<Integer>> exonIndex = new HashMap<String, ArrayList<Integer>>();
         ArrayList<Integer> intronLength = new ArrayList<Integer>();
 
         while (gffScanner.hasNext()) {
             String line = gffScanner.nextLine();
             processedGFFLine = processGFFLine(line);
             String geneId = processedGFFLine.get("geneId");
             Integer startNuc = new Integer(processedGFFLine.get("startNuc"));
             Integer endNuc = new Integer(processedGFFLine.get("endNuc"));
             String featureType = processedGFFLine.get("featureType");
             String transcriptId = processedGFFLine.get("transcriptId");
 
             //			System.out.println("featureType:" + featureType);
 
             if (featureType.equals("CDS")) {
                 if (exonIndex.get(transcriptId) == null) {
                     ArrayList<Integer> al = new ArrayList<Integer>();
                     exonIndex.put(transcriptId, al);
                 }
 
                 exonIndex.get(transcriptId).add(startNuc);
                 exonIndex.get(transcriptId).add(endNuc);
             }
         }
 
         for (String tId : exonIndex.keySet()) {
             ArrayList<Integer> indicies = exonIndex.get(tId);
             Collections.sort(indicies);
 
             for (int i = 1; i < indicies.size() - 2; i += 2) {
                 intronLength.add(indicies.get(i + 1) - indicies.get(i));
             }
         }
 
         double totalLength = 0;
         for (Integer i : intronLength) {
             totalLength += i;
         }
 
         //		System.out.println("totalLength: "+totalLength);
         //		System.out.println("geneIdLengths.size(): "+geneIdLengths.size());
 
         double avgIntronLength = totalLength / intronLength.size();
 
         return avgIntronLength;
     }
 
     /*5.	Average intergenic region size per contig.
      * Average number of nucleotides between the last
      * nucleotide of the stop codon and the first nucleotide
      * of the next gene (distance between subsequent gene
      * spans with different gene_id�s in column 9 of the
      * GFF file, see Figure 2 below).
      */
     static double avgIntergenicSize() {
         String output = "";
 
         //highest pos stored as geneId : highestPos
         HashMap<String, Integer> highestPos = new HashMap<String, Integer>();
 
         //lowest pos stored as geneId : lowestPos
         HashMap<String, Integer> lowestPos = new HashMap<String, Integer>();
 
         HashMap<String, String> processedGFFLine;
 
         while (gffScanner.hasNext()) {
             String line = gffScanner.nextLine();
             processedGFFLine = processGFFLine(line);
             String geneId = processedGFFLine.get("geneId");
             Integer startNuc = new Integer(processedGFFLine.get("startNuc"));
             Integer endNuc = new Integer(processedGFFLine.get("endNuc"));
 
             if (highestPos.containsKey(geneId) && highestPos.get(geneId) < endNuc) {
                 highestPos.put(geneId, endNuc);
             } else if (!highestPos.containsKey(geneId)) {
                 highestPos.put(geneId, endNuc);
             }
 
             if (lowestPos.containsKey(geneId) && lowestPos.get(geneId) > startNuc) {
                 lowestPos.put(geneId, startNuc);
             } else if (!lowestPos.containsKey(geneId)) {
                 lowestPos.put(geneId, startNuc);
             }
         }
 
         ArrayList<Integer> indicies = new ArrayList<Integer>();
         ArrayList<Integer> intergenic = new ArrayList<Integer>();
 
         for (String geneId : highestPos.keySet()) {
             indicies.add(lowestPos.get(geneId));
             indicies.add(highestPos.get(geneId));
         }
         Collections.sort(indicies);
         /*
         System.out.println("lowestPos:" + lowestPos);
         System.out.println("highestPos:" + highestPos);
          */
         for (int i = 1; i < indicies.size() - 2; i += 2) {
             intergenic.add(indicies.get(i + 1) - indicies.get(i));
         }
 
         double totalLength = 0;
         for (Integer i : intergenic) {
             totalLength += i;
         }
 
         //		System.out.println("totalLength: "+totalLength);
         //		System.out.println("geneIdLengths.size(): "+geneIdLengths.size());
 
         double avgIntergenicLength = totalLength / intergenic.size();
 
         return avgIntergenicLength;
     }
 
     static double avgNucDensitySpan() {
         HashMap<String, String> processedGFFLine;
         int CDSLenTotal = 0;
         int numCDS = 0;
         int numGeneIds = 0;
         double CDSLenAvg = 0;
         double totalNuc = fastaText.length();
 
         while (gffScanner.hasNext()) {
             String line = gffScanner.nextLine();
             processedGFFLine = processGFFLine(line);
 
             String type = processedGFFLine.get("featureType");
 
             if (type.equals("CDS")) {
                 Integer startNuc = new Integer(processedGFFLine.get("startNuc"));
                 Integer endNuc = new Integer(processedGFFLine.get("endNuc"));
 
                 CDSLenTotal += (Math.max(startNuc, endNuc) - Math.min(startNuc, endNuc));
                 numCDS++;
             }
             numGeneIds++;
         }
 
         return (double) CDSLenTotal / (double) numCDS * (double) numGeneIds / totalNuc;
     }
 
     static double avgNucDensityRegion() {
         HashMap<String, String> processedGFFLine;
         int CDSLenTotal = 0;
         int numCDS = 0;
         int numGeneIds = 0;
         double CDSLenAvg = 0;
         double totalNuc = fastaText.length();
 
         while (gffScanner.hasNext()) {
             String line = gffScanner.nextLine();
             processedGFFLine = processGFFLine(line);
 
             String type = processedGFFLine.get("featureType");
 
             if (type.equals("mRNA")) {
                 Integer startNuc = new Integer(processedGFFLine.get("startNuc"));
                 Integer endNuc = new Integer(processedGFFLine.get("endNuc"));
 
                 CDSLenTotal += (Math.max(startNuc, endNuc) - Math.min(startNuc, endNuc));
                 CDSLenTotal += 3;
 
                 numCDS++;
             }
             numGeneIds++;
         }
 
         return (double) CDSLenTotal / (double) numCDS * (double) numGeneIds / totalNuc;
     }
 
     static double cdsNucProportion() {
 
         return 0;
     }
 
     static double genesPerKB(int numKB) {
 
         return 0;
     }
 
     static double kbPerGene() {
 
         return 0;
     }
 
     static double predictProtein() {
 
         return 0;
     }
 }
