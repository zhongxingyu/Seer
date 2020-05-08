 package org.geworkbench.util.pathwaydecoder.mutualinformation;
 
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.collections15.map.MultiKeyMap;
 
 import java.util.*;
 import java.io.Serializable;
 
 /**
  * Class containing MINDY run results.
  * @author mhall
  */
 public class MindyData implements Serializable {
 
     static Log log = LogFactory.getLog(MindyData.class);
 
     private CSMicroarraySet arraySet;
     private DSGeneMarker transcriptionFactor;
     private List<MindyResultRow> data;
 
     private MultiKeyMap<DSGeneMarker, MindyResultRow> dataMap = new MultiKeyMap<DSGeneMarker, MindyResultRow>();
 
     private HashMap<DSGeneMarker, ModulatorStatistics> modulatorStatistics = new HashMap<DSGeneMarker, ModulatorStatistics>();
     
     private float setFraction;
 
     /**
      * Constructor.
      * 
      * @param arraySet - microarray set
      * @param data - list of MINDY result rows
      * @param setFraction - Sample per Condition in fraction
      */
     public MindyData(CSMicroarraySet arraySet, List<MindyResultRow> data, float setFraction) {
         this.arraySet = arraySet;
         this.data = data;
         this.setFraction = setFraction;
         if (data.size() > 0) {
             this.transcriptionFactor = data.get(0).getTranscriptionFactor();
         } else {
             log.warn("Data passed in had 0 records, unable to determine transcription factor under consideration.");
         }
         calculateModulatorStatistics();
     }
 
     /**
      * Get the microarray set.
      * 
      * @return microarray set
      */
     public CSMicroarraySet getArraySet() {
         return arraySet;
     }
 
     /**
      * Set the specified microarray set to MINDY data.
      * 
      * @param arraySet - the microarray set to associate with MINDY data
      */
     public void setArraySet(CSMicroarraySet arraySet) {
         this.arraySet = arraySet;
     }
 
     /**
      * Get the MINDY result rows.
      * 
      * @return MINDY result rows
      */
     public List<MindyResultRow> getData() {
         return data;
     }
 
     /**
      * Set the MINDY result rows associated with MINDY data.
      * 
      * @param data - the list of MINDY result rows to associate with MINDY data.
      */
     public void setData(List<MindyResultRow> data) {
         this.data = data;
         calculateModulatorStatistics();
     }
 
     /**
      * Get the statics for the specified modulator.
      * 
      * @param modulator - modulator for which to get the statistics
      * @return - ModulatorStatistics object
      */
     public ModulatorStatistics getStatistics(DSGeneMarker modulator) {
         return modulatorStatistics.get(modulator);
     }
 
     /**
      * Get the transcription factor specified for MINDY data.
      * 
      * @return the transcription factor gene marker
      */
     public DSGeneMarker getTranscriptionFactor() {
         return transcriptionFactor;
     }
     
     /**
      * Get the fraction of the sample to display on the heat map.
      * 
      * @return fraction of the sample to display
      */
     public float getSetFraction(){
     	return this.setFraction;
     }
 
     /**
      * Set the transcription factor for the MINDY data.
      * 
      * @param transcriptionFactor
      */
     public void setTranscriptionFactor(DSGeneMarker transcriptionFactor) {
         this.transcriptionFactor = transcriptionFactor;
     }
 
     /**
      * Get the list of mondulators.
      * 
      * @return list of modulators
      */
     public List<DSGeneMarker> getModulators() {
         ArrayList<DSGeneMarker> modulators = new ArrayList<DSGeneMarker>();
         for (Map.Entry<DSGeneMarker, ModulatorStatistics> entry : modulatorStatistics.entrySet()) {
             modulators.add(entry.getKey());
         }
         return modulators;
     }
 
     /**
      * Get a list of all the transcription factors.
      * 
      * @return list of all the transcription factors
      */
     public List<DSGeneMarker> getAllTranscriptionFactors() {
         ArrayList<DSGeneMarker> transFacs = new ArrayList<DSGeneMarker>();
         for (MindyResultRow mindyResultRow : data) {
             transFacs.add(mindyResultRow.getTranscriptionFactor());
         }
         return transFacs;
     }
 
     /**
      * Get a list of transcription factors based on a specified modulator.
      * 
      * @param modulator
      * @return list of transcription factors
      */
     public List<DSGeneMarker> getTranscriptionFactors(DSGeneMarker modulator) {
         HashSet<DSGeneMarker> transFacs = new HashSet<DSGeneMarker>();
         for (MindyResultRow mindyResultRow : data) {
             if (mindyResultRow.getModulator().equals(modulator)) {
                 transFacs.add(mindyResultRow.getTranscriptionFactor());
             }
         }
         return new ArrayList<DSGeneMarker>(transFacs);
     }
 
     /**
      * Get the list of MINDY result rows based on specified modulator and transcription factor.
      * 
      * @param modulator
      * @param transFactor - transcription factor
      * @return list of MINDY result rows
      */
     public List<MindyResultRow> getRows(DSGeneMarker modulator, DSGeneMarker transFactor) {
         List<MindyResultRow> results = new ArrayList<MindyResultRow>();
         for (MindyResultRow mindyResultRow : data) {
             if (mindyResultRow.getModulator().equals(modulator) && mindyResultRow.getTranscriptionFactor().equals(transFactor))
             {
                 results.add(mindyResultRow);
             }
         }
         return results;
     }
 
     /**
      * Get the list of MINDY result rows based on specified modulator, transcription factor, and target marker set.
      * 
      * @param modulator
      * @param transFactor - transcription factor
      * @param limitTargets - target marker set being displayed
      * @return list of MINDY result rows
      */
     public List<MindyResultRow> getRows(DSGeneMarker modulator, DSGeneMarker transFactor, List<DSGeneMarker> limitTargets) {
         List<MindyResultRow> results = new ArrayList<MindyResultRow>();
         for (MindyResultRow mindyResultRow : data) {
             if (mindyResultRow.getModulator().equals(modulator) && mindyResultRow.getTranscriptionFactor().equals(transFactor))
             {
                 if (limitTargets != null && limitTargets.contains(mindyResultRow.getTarget())) {
                     results.add(mindyResultRow);
                 }
             }
         }
         return results;
     }
 
     private void calculateModulatorStatistics() {
         log.debug("Calculating modulator stats...");
         for (MindyResultRow row : data) {
             dataMap.put(row.getModulator(), row.getTranscriptionFactor(), row.getTarget(), row);
             ModulatorStatistics modStats = modulatorStatistics.get(row.getModulator());
             if (modStats == null) {
                 modStats = new ModulatorStatistics(0, 0, 0);
                 modulatorStatistics.put(row.getModulator(), modStats);
             }
            modStats.count++;
             if (row.getScore() < 0) {
                 modStats.munder++;
             } else if(row.getScore() > 0){
                 modStats.mover++;
             }
         }
         log.debug("Done calculating modulator stats...");
     }
 
     /**
      * Get a hash map storing modulators and their statistics.
      * 
      * @return the hash map
      */
     public HashMap<DSGeneMarker, ModulatorStatistics> getAllModulatorStatistics() {
         return modulatorStatistics;
     }
 
     /**
      * Get a list of targets based on specified modulator and transcription factor.
      * 
      * @param modulator
      * @param transcriptionFactor
      * @return list of targets
      */
     public List<DSGeneMarker> getTargets(DSGeneMarker modulator, DSGeneMarker transcriptionFactor) {
         List<DSGeneMarker> targets = new ArrayList<DSGeneMarker>();
         List<MindyResultRow> rows = getRows(modulator, transcriptionFactor);
         for (MindyResultRow mindyResultRow : rows) {
             targets.add(mindyResultRow.getTarget());
         }
         return targets;
     }
 
     /**
      * Get the list of all targets.
      * 
      * @return list of all targets
      */
     public List<DSGeneMarker> getAllTargets() {
         List<DSGeneMarker> targets = new ArrayList<DSGeneMarker>();
         for (MindyResultRow mindyResultRow : data) {
             targets.add(mindyResultRow.getTarget());
         }
         return targets;
     }
 
     /**
      * Get the score of the specified modulator, transcription factor and target.
      * 
      * @param modulator
      * @param transcriptionFactor
      * @param target
      * @return the score used in MINDY data
      */
     public float getScore(DSGeneMarker modulator, DSGeneMarker transcriptionFactor, DSGeneMarker target) {
         MindyResultRow row = dataMap.get(modulator, transcriptionFactor, target);
         if (row == null) {
             return 0;
         } else {
             return row.getScore();
         }
     }
 
     /**
      * Get the MINDY result row that has the specified modulator, transcription factor, and target.
      * 
      * @param modulator
      * @param transcriptionFactor
      * @param target
      * @return MINDY result row
      */
     public MindyResultRow getRow(DSGeneMarker modulator, DSGeneMarker transcriptionFactor, DSGeneMarker target) {
         return dataMap.get(modulator, transcriptionFactor, target);
     }
 
     /**
      * Represents a row in the MINDY result data.
      * 
      * @author mhall
     * @version $Id: MindyData.java,v 1.5 2007-07-25 21:11:12 hungc Exp $
      */
     public static class MindyResultRow implements Serializable{
         private DSGeneMarker modulator;
         private DSGeneMarker transcriptionFactor;
         private DSGeneMarker target;
 
         private float score;
         private float pvalue;
 
         /**
          * Constructor.
          * 
          * @param modulator
          * @param transcriptionFactor
          * @param target
          * @param score
          * @param pvalue
          */
         public MindyResultRow(DSGeneMarker modulator, DSGeneMarker transcriptionFactor, DSGeneMarker target, float score, float pvalue) {
             this.modulator = modulator;
             this.transcriptionFactor = transcriptionFactor;
             this.target = target;
             this.score = score;
             this.pvalue = pvalue;
         }
 
         /**
          * Get the modulator in this MINDY result row.
          * 
          * @return a modulator gene marker
          */
         public DSGeneMarker getModulator() {
             return modulator;
         }
 
         /**
          * Set the modulator in this MINDY result row.
          * 
          * @param modulator
          */
         public void setModulator(DSGeneMarker modulator) {
             this.modulator = modulator;
         }
 
         /**
          * Get the transcription factor in this MINDY result row.
          * 
          * @return a transcription factor gene marker
          */
         public DSGeneMarker getTranscriptionFactor() {
             return transcriptionFactor;
         }
 
         /**
          * Set the transcription factor of this MINDY result row.
          * 
          * @param transcriptionFactor
          */
         public void setTranscriptionFactor(DSGeneMarker transcriptionFactor) {
             this.transcriptionFactor = transcriptionFactor;
         }
 
         /**
          * Get the target in this MINDY result row.
          * 
          * @return a target gene marker
          */
         public DSGeneMarker getTarget() {
             return target;
         }
 
         /**
          * Set the target for this MINDY result row.
          * 
          * @param target
          */
         public void setTarget(DSGeneMarker target) {
             this.target = target;
         }
 
         /**
          * Get the score in this MINDY result row.
          * 
          * @return the score 
          */
         public float getScore() {
             return score;
         }
 
         /**
          * Set the score for this MINDY result row.
          * 
          * @param score
          */
         public void setScore(float score) {
             this.score = score;
         }
 
         /**
          * Get the P-value in this MINDY result row.
          * 
          * @return the P-value
          */
         public float getPvalue() {
             return pvalue;
         }
 
         /**
          * Set the P-value for this MINDY result row.
          * 
          * @param pvalue
          */
         public void setPvalue(float pvalue) {
             this.pvalue = pvalue;
         }
 
     }
 
     /**
      * Represents the statistics of a modulator. The statistics consist of:
      * Count (M#)
      * Mover (M+)
      * Munder(M-)
      * 
      * @author mhall
     * @version $Id: MindyData.java,v 1.5 2007-07-25 21:11:12 hungc Exp $
      */
     public static class ModulatorStatistics implements Serializable {
         protected int count;
         protected int mover;
         protected int munder;
 
         /**
          * Constructor.  Sets all three stats -- count (M#), mover(M+), and munder (M-)
          * 
          * @param count
          * @param mover
          * @param munder
          */
         public ModulatorStatistics(int count, int mover, int munder) {
             this.count = count;
             this.mover = mover;
             this.munder = munder;
         }
 
         /**
          * Get the count (M#).
          * 
          * @return the count
          */
         public int getCount() {
             return count;
         }
 
         /**
          * Get the mover (M+)
          * 
          * @return the mover
          */
         public int getMover() {
             return mover;
         }
 
         /**
          * Get the munder (M-)
          * 
          * @return the munder
          */
         public int getMunder() {
             return munder;
         }
     }
 
 }
