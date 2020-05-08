 /**
 *   DataItem.java
 *
 *   Copyright (c) 2013 Vladimir Brigant
 *   This software is distributed under the terms of the GNU General Public License.
 */
 
 package cassp.data;
 
 
 import java.io.*;
 import java.util.*;
 import java.lang.StringBuilder;
 import org.apache.log4j.*;
 
 import cassp.config.*;
 
 
 /**
 * Represents one data item.
 */
 public class DataItem {
 
     /**
     * Threshold of using PSIPRED.
     */
     public static int PSIPRED_THRESHOLD = 3;
 
     private String aaSeq;
     private String sspSeq;
     private String psipredSeq;
     private String predictedSeq;
     private double propsMeanDiff;
     private int meanReliabIndex;
     private String reliabIndexesStr;
     private ArrayList<Integer> reliabIndexes;
 
 
     public DataItem(){
         this.aaSeq = "";
         this.sspSeq = "";
         this.predictedSeq = "";
         this.psipredSeq = "";
         this.reliabIndexesStr = "";
     }
 
     public DataItem(String aa, String ssp, String pred){
         this.aaSeq = aa;
         this.sspSeq =ssp;
         this.predictedSeq = pred;
         this.psipredSeq = "";
         this.reliabIndexesStr = "";
     }
 
     public int length(){
         return this.aaSeq.length();
     }
 
     /**
     * Repair prediction.
     */
     public void repairPrediction(String predSeq, int threshold, int repairType){
         if (repairType == SimConfig.REPAIR_PROTEIN){
             if (this.meanReliabIndex < threshold){
                 this.predictedSeq = predSeq;
             }
         }
         else if (repairType == SimConfig.REPAIR_RESIDUE){
             for (int i = 0; i < this.length(); i++) {
                 StringBuilder seq = new StringBuilder(this.predictedSeq);
 
                 if (this.reliabIndexes.get(i) < threshold){
                     seq.setCharAt(i, predSeq.charAt(i));
                 }
                 this.predictedSeq = seq.toString();
             }
         }
     }
 
 
     /* Getters & setters */
 
     public String getAaSeq(){
         return this.aaSeq;
     }
 
     public char getAaAt(int index){
         return this.aaSeq.charAt(index);
     }
 
     public void setAaSeq(String aa){
         this.aaSeq = aa;
     }
 
     public String getSspSeq(){
         return this.sspSeq;
     }
 
     public char getSspAt(int index){
         return this.sspSeq.charAt(index);
     }
 
     public void setSspSeq(String ssp){
         this.sspSeq = ssp;
     }
 
     public String getPredSeq(){
         return this.predictedSeq;
     }
 
     public void setPsipredSeq(String psipredSeq){
         this.psipredSeq = psipredSeq;
     }
 
     public String getPsipredSeq(){
         return this.psipredSeq;
     }
 
     public char getPredAt(int index){
         return this.predictedSeq.charAt(index);
     }
 
     public void setPredSeq(String pred){
         this.predictedSeq = pred;
     }
 
     public double getPropsMeanDiff(){
         return this.propsMeanDiff;
     }
 
     public void setPropsMeanDiff(double meanDiff){
         this.propsMeanDiff = meanDiff;
     }
 
     public void setReliabIndexes(ArrayList<Integer> reliabIndexes){
         this.reliabIndexes = reliabIndexes;
     }
 
     public void setReliabIndexes(String riStr, ArrayList<Integer> reliabIndexes){
         this.reliabIndexesStr = riStr;
         this.reliabIndexes = reliabIndexes;
     }
 
     public ArrayList<Integer> getReliabIndexes(){
         return this.reliabIndexes;
     }
 
     public String getReliabIndexesStr(){
         return this.reliabIndexesStr;
     }
 
     public void setPsipredAsPredSeq(){
         this.predictedSeq = this.psipredSeq;
     }
 
     public void setMeanReliabIndex(int index){
         this.meanReliabIndex = index;
     }

    public void computeMeanReliabIndex(){
        double sum = 0.0;
        for (int i = 0; i < this.reliabIndexes.size();i++) {
            sum += this.reliabIndexes.get(i);
        }
        this.meanReliabIndex = (int) Math.floor(sum/this.reliabIndexes.size());
    }
 }
