 /**
 *   Utils.java
 *
 *   Copyright (c) 2013 Vladimir Brigant
 *   This software is distributed under the terms of the GNU General Public License.
 */
 
 package cassp.utils;
 
 
 import java.io.*;
 import java.util.*;
 
 import org.jgap.*;
 import org.jgap.data.*;
 
 import cassp.data.*;
 
 
 
 public class Utils {
 
     public static char[] aminoAcids = new char[] {
         'G', 'C', 'A', 'M', 'V', 'K', 'L', 'R', 'I', 'H',
         'F', 'W', 'P', 'D', 'S', 'E', 'T', 'N', 'Y', 'Q'
     };
 
     public static char[] ambiguousAminoAcids = new char[] {
         'B', 'Z', 'X', 'J'
     };
 
     public static char[] allAminoAcids = new char[] {
         'G', 'C', 'A', 'M', 'V', 'K', 'L', 'R', 'I', 'H',
         'F', 'W', 'P', 'D', 'S', 'E', 'T', 'N', 'Y', 'Q',
         'B', 'Z', 'X', 'J'
     };
 
     public static double getMin(Population population){
         double min = 101.0;
 
         for (int i = 0; i < population.size(); i++) {
             double fitness = population.getChromosome(i).getFitnessValue();
 
             if (fitness < min)
                 min = fitness;
         }
         return min;
     }
 
 
     public static double getMax(Population population){
         double max = 0.0;
 
         for (int i = 0; i < population.size(); i++) {
             double fitness = population.getChromosome(i).getFitnessValue();
 
             if (fitness > max)
                 max = fitness;
         }
         return max;
     }
 
 
     public static double getMean(Population population){
         double sum = 0.0;
 
         for (int i = 0; i < population.size(); i++)
             sum += population.getChromosome(i).getFitnessValue();
 
         return sum/population.size();
     }
 
     /**
     * Computes Q3 accuracy measure for actual Data object.
     * Q3 is ratio of a number of good predicted amino acids
     * to a number of all amino acids.
     */
     public static double q3(Data data){
         double allCount = 0.0;
         double okCount  = 0.0;
 
         for (DataItem di : data.getData()){
             double[] q3 = Utils.q3(di);
 
             okCount += q3[0]*q3[1];
             allCount += q3[1];
         }
 
         return allCount > 0 ? (double) okCount/allCount : 0.0;
     }
 
     public static double q3(Data data, char motiv){
         double okCount  = 0.0;
         double allCount = 0.0;
 
         for (DataItem di : data.getData()){
             double[] q3 = Utils.q3(di, motiv);
 
             okCount += q3[0]*q3[1];
             allCount += q3[1];
         }
 
         return allCount > 0 ? (double) okCount/allCount : 0.0;
     }
 
     public static double[] q3(DataItem dataItem){
         int okCount = 0;
 
         for (int i = 0; i <  dataItem.length(); i++) {
             if (dataItem.getSspAt(i) == dataItem.getPredAt(i))
                 okCount++;
         }
         return new double[] {(double) okCount/dataItem.length()*100, dataItem.length()};
     }
 
     public static double[] q3(DataItem dataItem, char motiv){
         int okCount = 0;
         int allCount = 0;
 
         for (int i = 0; i <  dataItem.length(); i++) {
             if (dataItem.getSspAt(i) == motiv){
                 if (dataItem.getSspAt(i) == dataItem.getPredAt(i))
                     okCount++;
                 allCount++;
             }
         }
         double q3 = allCount > 0 ? (double) okCount/allCount*100 : 0.0;
         return new double[] {q3, allCount};
     }
 
 
     /**
     * Computes SOV accuracy measure for actual Data object.
     * SOV takes into account segments overlaps.
     */
     public static double sov(Data data){
         double sov = 0.0;
         int norm = 0;
 
         for (DataItem di: data.getData()) {
             double[] result = Utils.sov(di);
             norm += result[1];
            sov += result[0]*norm;
         }
         return norm > 0 ? sov/norm : 0.0;
     }
 
     public static double sov(Data data, char motiv){
         double sov = 0.0;
         int norm = 0;
 
         for (DataItem di: data.getData()) {
             double[] result = Utils.sov(di, motiv);
             norm += result[1];
             sov += result[0]*norm;
         }
         return norm > 0 ? sov/norm : 0.0;
     }
 
     public static double[] sov(DataItem dataItem, char motiv){
 
         Map<SOVSegment, ArrayList<SOVSegment>> olSegments;
         ArrayList<SOVSegment> origSegments = new ArrayList<SOVSegment>();
         ArrayList<SOVSegment> predSegments = new ArrayList<SOVSegment>();
 
         origSegments = getAllSegments(dataItem.getSspSeq(), motiv);
         predSegments = getAllSegments(dataItem.getPredSeq(), motiv);
         olSegments = getOverlappingSegments(origSegments, predSegments);
 
         double sovN = computeNormConstant(olSegments);
         double sumSOV = 0.0;
 
         for (Map.Entry<SOVSegment, ArrayList<SOVSegment>> entry: olSegments.entrySet()) {
             SOVSegment origSeg = entry.getKey();
             ArrayList<SOVSegment> predSegs = entry.getValue();
 
             if (predSegs.size() > 0){
                 for (SOVSegment predSeg: predSegs){
 
                     double partSOV = SOVSegment.minov(origSeg, predSeg);
                     partSOV += SOVSegment.delta(origSeg, predSeg);
                     partSOV = partSOV / SOVSegment.maxov(origSeg, predSeg) * origSeg.length();
                     sumSOV += partSOV;
                 }
             }
         }
         double sov = sovN > 0 ? sumSOV/sovN*100 : 0.0;
         return new double[] {sov, sovN};
     }
 
     public static double[] sov(DataItem dataItem){
         char motiv;
 
         Map<SOVSegment, ArrayList<SOVSegment>> olSegments;
         ArrayList<SOVSegment> origSegments = new ArrayList<SOVSegment>();
         ArrayList<SOVSegment> predSegments = new ArrayList<SOVSegment>();
 
         origSegments = getAllSegments(dataItem.getSspSeq());
         predSegments = getAllSegments(dataItem.getPredSeq());
         olSegments = getOverlappingSegments(origSegments, predSegments);
 
         double sovN = computeNormConstant(olSegments);
         double sumSOV = 0.0;
 
         for (Map.Entry<SOVSegment, ArrayList<SOVSegment>> entry: olSegments.entrySet()) {
             SOVSegment origSeg = entry.getKey();
             ArrayList<SOVSegment> predSegs = entry.getValue();
 
             if (predSegs.size() > 0){
                 for (SOVSegment predSeg: predSegs){
 
                     double partSOV = SOVSegment.minov(origSeg, predSeg);
                     partSOV += SOVSegment.delta(origSeg, predSeg);
                     partSOV = partSOV / SOVSegment.maxov(origSeg, predSeg) * origSeg.length();
                     sumSOV += partSOV;
                 }
             }
         }
         double sov = sovN > 0 ? sumSOV/sovN*100 : 0.0;
         return new double[] {sov, sovN};
     }
 
 
     /**
     * Computes normalization constant needed for SOV accuracy measure computing.
     *
     * @param olSegments Map of overlapped segments, it is mapping original (not predicted)
     * segment to all predicted segments it overlaps with.
     */
     private static int computeNormConstant(Map<SOVSegment, ArrayList<SOVSegment>> olSegments){
 
         // sum of lengths of overlapping segments
         int sumOL = 0;
 
         // sum of lengths of non overlapping segments
         int sumNOL = 0;
 
         for (Map.Entry<SOVSegment, ArrayList<SOVSegment>> entry : olSegments.entrySet()) {
             SOVSegment origSeg = entry.getKey();
             ArrayList<SOVSegment> predSegs = entry.getValue();
 
             if (predSegs.size() == 0)
                 sumNOL += origSeg.length();
             else
                 sumOL += origSeg.length() * predSegs.size();
         }
         return sumOL + sumNOL;
     }
 
 
 
     /**
     *
     */
     private static Map<SOVSegment, ArrayList<SOVSegment>> getOverlappingSegments(
         ArrayList<SOVSegment> origSegments, ArrayList<SOVSegment> predSegments){
 
         Map<SOVSegment, ArrayList<SOVSegment>> olSegments = new HashMap<SOVSegment, ArrayList<SOVSegment>>();
 
         for (SOVSegment orig: origSegments){
             olSegments.put(orig, new ArrayList<SOVSegment>());
 
             for (SOVSegment pred: predSegments){
                 if (orig.isOverlapping(pred))
                     olSegments.get(orig).add(pred);
             }
         }
 
         return olSegments;
     }
 
     private static ArrayList<SOVSegment> getAllSegments(String seq){
 
         int start = 0;
         int stop = 0;
         boolean chainFlag = false;
         char motiv = 'H';
         ArrayList<SOVSegment> segments = new ArrayList<SOVSegment>();
 
         for (int i = 0; i < seq.length(); i++) {
             if (chainFlag == false){
                 start = i;
                 stop = i;
                 motiv = seq.charAt(i);
                 chainFlag = true;
             }
             else{
                 if (seq.charAt(i) != motiv){
                     SOVSegment segment = new SOVSegment(0, start, stop, motiv);
                     segments.add(segment);
                     start = i;
                     stop = i;
                     motiv = seq.charAt(i);
                 }
                 else{
                     stop += 1;
                 }
             }
             if (i == seq.length() - 1){
                 SOVSegment segment = new SOVSegment(0, start, stop, motiv);
                 segments.add(segment);
             }
         }
         return segments;
     }
 
     private static ArrayList<SOVSegment> getAllSegments(String seq, char motiv){
 
         int start = 0;
         int stop = 0;
         boolean chainFlag = false;
         ArrayList<SOVSegment> segments = new ArrayList<SOVSegment>();
 
         for (int i = 0; i < seq.length(); i++) {
             if (chainFlag == false && seq.charAt(i) == motiv){
                 start = i;
                 stop = i;
                 chainFlag = true;
             }
             else{
                 if (chainFlag == true && seq.charAt(i) != motiv){
                     SOVSegment segment = new SOVSegment(0, start, stop, motiv);
                     segments.add(segment);
                     // !!!
                     if (seq.charAt(i) == motiv){
                         start = i;
                         stop = i;
                     }
                     else
                         chainFlag = false;
                 }
                 else{
                     stop += 1;
                 }
             }
             if (i == seq.length() - 1 && chainFlag == true){
                 SOVSegment segment = new SOVSegment(0, start, stop, motiv);
                 segments.add(segment);
             }
         }
         return segments;
     }
 
     /**
     * Removes all .txt files.
     *
     * @param dir directory in which .txt files are deleted
     */
     public static void removeTXTFiles(String dir){
         File folder = new File(dir);
         File[] files = folder.listFiles( new FilenameFilter() {
             public boolean accept( final File dir, final String name ) {
                 return name.matches(".*\\.txt");
             }
         });
         for (File file : files){
             if (!file.delete())
                 System.err.println( "Can't remove " + file.getAbsolutePath() );
         }
     }
 }
