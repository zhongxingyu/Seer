 package org.geworkbench.components.sequenceretriever;
 
 import java.io.*;
 import java.net.URL;
 import java.sql.*;
 import java.util.Vector;
 
 import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
 import org.geworkbench.util.sequences.GeneChromosomeMatcher;
 
 /**
  * <p>Title: </p>
  *
  * <p>Description: </p>
  *
  * <p>Copyright: Copyright (c) 2005</p>
  *
  * <p>Company: </p>
  *
  * @author XZ
  * @version 1.0
  */
 public class SequenceFetcher {
     public static final String UCSC = "UCSC";
     private static SequenceFetcher theSequenceFetcher = new SequenceFetcher();
     public SequenceFetcher() {
     }
 
     public static SequenceFetcher getSequenceFetcher() {
         return theSequenceFetcher;
     }
 
     public String translateFromAffIDtoRefSeqID(String affID) {
 
         return null;
 
     }
 
     public static CSSequence[] getSequences(String geneName, String source) {
         if (source.equals(UCSC)) {
             return getSequences(geneName);
         }
         return null;
     }
 
     /**
      * getSequences
      *
      * @param geneName String
      * @return CSSequence[]
      */
     private static CSSequence[] getSequences(String geneName) {
         return null;
     }
 
     /**
      * getGeneChromosomeMatchers
      *
      * @param geneName String
      * @param chipType String
      * @return any[]
      */
     public static Vector getGeneChromosomeMatchers(String
             geneName,
             String chipType) {
         String database = "hg18";
         String[] columnName = {"chrom", "strand", "txStart", "txEnd"};
         Vector<GeneChromosomeMatcher> vector = new Vector<GeneChromosomeMatcher>();
         try {
             Statement stmt;
             Class.forName("com.mysql.jdbc.Driver");
             String url =
                     "jdbc:mysql://genome-mysql.cse.ucsc.edu:3306/" +
                     database.trim();
             Connection con =
                     DriverManager.getConnection(
                             url, "genome", "");
             stmt = con.createStatement();
             boolean success = stmt.execute(
                     "select * from knownGene where name = '" + geneName + "' ");
             if (success) {
                 ResultSet rs = stmt.getResultSet();
                 while (rs.next()) {
                     String chrom = rs.getString(columnName[0]);
                     boolean positiveStrand = true;
                     if (rs.getString(columnName[1]).equalsIgnoreCase("-")) {
                         positiveStrand = false;
                     }
                     int txStart = new Integer(rs.getString(columnName[2])).
                                   intValue();
                     int txEnd = new Integer(rs.getString(columnName[3])).
                                 intValue();
                     GeneChromosomeMatcher geneMatcher = new
                             GeneChromosomeMatcher(positiveStrand, chrom,
                                                   txStart, txEnd, database);
                     geneMatcher.setName(geneName);
                     vector.add(geneMatcher);
                 }
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         return vector;
     }
 
     /**
      * getSequences
      *
      * @param geneChromosomeMatcher GeneChromosomeMatcher
      * @param upsteamRegion int
      * @param downstreamRegion int
      */
     public CSSequence getSequences(GeneChromosomeMatcher
                                    geneChromosomeMatcher,
                                    int upstreamRegion,
                                    int downstreamRegion) {
         if (geneChromosomeMatcher == null) {
             return null;
         }
         CSSequence sequence = null;
         if (geneChromosomeMatcher != null) {
             int upStartPoint = geneChromosomeMatcher.getStartPoint();
             int downStartPoint = geneChromosomeMatcher.getEndPoint();
             int startPoint = upStartPoint - upstreamRegion - 1;
             int endPoint = upStartPoint + downstreamRegion;
             if (!geneChromosomeMatcher.isPositiveStrandDirection()) {
                 startPoint = downStartPoint - downstreamRegion;
                 endPoint = downStartPoint + upstreamRegion;
             }
             sequence = getSequence(geneChromosomeMatcher.getGenomeBuildNumber(),
                                    geneChromosomeMatcher.getChr(),
                                    startPoint, endPoint,
                                    geneChromosomeMatcher.
                                    isPositiveStrandDirection());
         }
         return sequence;
     }
 
     /**
      * getSequence
      * @param genomeBuilderName String
      * @param chromosomeName String
      * @param isPositiveStrand boolean
      * @param startPoint int
      * @param length int
      */
     private CSSequence getSequence(String genomeBuilderName,
                                    String chromosomeName,
                                    boolean isPositiveStrand,
                                    int startPoint, int length) {
 
         int maxSize = 1000000;
         int endPoint = startPoint + length - 1;
         if (!isPositiveStrand) {
             endPoint = startPoint;
             startPoint = endPoint - length + 1;
         }
         return getSequence(genomeBuilderName, chromosomeName, startPoint,
                            endPoint, true);
 
     }
 
     /**
      * getSequence
      * @param genomeBuilderName String
      * @param chromosomeName String
      * @param startPoint int
      * @param length int
      */
     private CSSequence getSequence(String genomeBuilderName,
                                    String chromosomeName,
                                    int startPoint, int endPoint,
                                    boolean isPositiveStrand) {
 
         int maxSize = 1000000;
         String request = "http://genome.cse.ucsc.edu/cgi-bin/das/" +
                          genomeBuilderName + "/dna?segment=" + chromosomeName +
                          ":" + startPoint + ":" + endPoint; ;
 
         try {
             InputStream uin = new URL(request).openStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     uin));
             String line;
             StringBuffer sequenceContent = new StringBuffer();
             boolean reachStartPoint = false;
             boolean reachEndPoint = false;
             while ((line = in.readLine()) != null) {
                 int size = 0;
                 if (line.trim().startsWith("<DNA")) {
                     reachStartPoint = true;
                     String[] str = line.split(">");
                     if (str.length > 1) {
                         sequenceContent.append(str[1]);
                         size = str[1].length();
                     }
 
                     String label = "request";
 
                     while ((line = in.readLine()) != null &&
                                    !line.trim().endsWith("DNA>")) {
                         size += line.length();
                         if (size >= maxSize) {
 
                         }
                         sequenceContent.append(line);
                     }
                     String content = sequenceContent.toString();
                     if (!isPositiveStrand) {
                         content = CSSequence.reverseString(content);
                     }
                     CSSequence seq = new CSSequence(label,
                             content);
                     return seq;
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return null;
 
     }
 
 
 }
