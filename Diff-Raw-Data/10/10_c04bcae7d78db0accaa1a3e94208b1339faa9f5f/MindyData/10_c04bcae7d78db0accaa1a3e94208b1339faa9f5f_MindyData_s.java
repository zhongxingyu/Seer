 package org.geworkbench.util.pathwaydecoder.mutualinformation;
 
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.collections15.map.MultiKeyMap;
 
 import java.util.*;
 
 /**
  * Class containing MINDY run results.
  * @author mhall
  */
public class MindyData {
 
     static Log log = LogFactory.getLog(MindyData.class);
 
     private CSMicroarraySet arraySet;
     private DSGeneMarker transcriptionFactor;
     private List<MindyResultRow> data;
 
     private MultiKeyMap<DSGeneMarker, MindyResultRow> dataMap = new MultiKeyMap<DSGeneMarker, MindyResultRow>();
 
     private HashMap<DSGeneMarker, ModulatorStatistics> modulatorStatistics = new HashMap<DSGeneMarker, ModulatorStatistics>();
 
     public MindyData(CSMicroarraySet arraySet, List<MindyResultRow> data) {
         this.arraySet = arraySet;
         this.data = data;
         if (data.size() > 0) {
             this.transcriptionFactor = data.get(0).getTranscriptionFactor();
         } else {
             log.warn("Data passed in had 0 records, unable to determine transcription factor under consideration.");
         }
         calculateModulatorStatistics();
     }
 
     public CSMicroarraySet getArraySet() {
         return arraySet;
     }
 
     public void setArraySet(CSMicroarraySet arraySet) {
         this.arraySet = arraySet;
     }
 
     public List<MindyResultRow> getData() {
         return data;
     }
 
     public void setData(List<MindyResultRow> data) {
         this.data = data;
         calculateModulatorStatistics();
     }
 
     public ModulatorStatistics getStatistics(DSGeneMarker modulator) {
         return modulatorStatistics.get(modulator);
     }
 
     public DSGeneMarker getTranscriptionFactor() {
         return transcriptionFactor;
     }
 
     public void setTranscriptionFactor(DSGeneMarker transcriptionFactor) {
         this.transcriptionFactor = transcriptionFactor;
     }
 
     public List<DSGeneMarker> getModulators() {
         ArrayList<DSGeneMarker> modulators = new ArrayList<DSGeneMarker>();
         for (Map.Entry<DSGeneMarker, ModulatorStatistics> entry : modulatorStatistics.entrySet()) {
             modulators.add(entry.getKey());
         }
         return modulators;
     }
 
     public List<DSGeneMarker> getAllTranscriptionFactors() {
         ArrayList<DSGeneMarker> transFacs = new ArrayList<DSGeneMarker>();
         for (MindyResultRow mindyResultRow : data) {
             transFacs.add(mindyResultRow.getTranscriptionFactor());
         }
         return transFacs;
     }
 
     public List<DSGeneMarker> getTranscriptionFactors(DSGeneMarker modulator) {
         HashSet<DSGeneMarker> transFacs = new HashSet<DSGeneMarker>();
         for (MindyResultRow mindyResultRow : data) {
             if (mindyResultRow.getModulator().equals(modulator)) {
                 transFacs.add(mindyResultRow.getTranscriptionFactor());
             }
         }
         return new ArrayList<DSGeneMarker>(transFacs);
     }
 
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
             } else {
                 modStats.mover++;
             }
         }
         log.debug("Done calculating modulator stats...");
     }
 
     public HashMap<DSGeneMarker, ModulatorStatistics> getAllModulatorStatistics() {
         return modulatorStatistics;
     }
 
     public List<DSGeneMarker> getTargets(DSGeneMarker modulator, DSGeneMarker transcriptionFactor) {
         List<DSGeneMarker> targets = new ArrayList<DSGeneMarker>();
         List<MindyResultRow> rows = getRows(modulator, transcriptionFactor);
         for (MindyResultRow mindyResultRow : rows) {
             targets.add(mindyResultRow.getTarget());
         }
         return targets;
     }
 
     public List<DSGeneMarker> getAllTargets() {
         List<DSGeneMarker> targets = new ArrayList<DSGeneMarker>();
         for (MindyResultRow mindyResultRow : data) {
             targets.add(mindyResultRow.getTarget());
         }
         return targets;
     }
 
     public float getScore(DSGeneMarker modulator, DSGeneMarker transcriptionFactor, DSGeneMarker target) {
         MindyResultRow row = dataMap.get(modulator, transcriptionFactor, target);
         if (row == null) {
             return 0;
         } else {
             return row.getScore();
         }
     }
 
     public MindyResultRow getRow(DSGeneMarker modulator, DSGeneMarker transcriptionFactor, DSGeneMarker target) {
         return dataMap.get(modulator, transcriptionFactor, target);
     }
 
    public static class MindyResultRow {
         private DSGeneMarker modulator;
         private DSGeneMarker transcriptionFactor;
         private DSGeneMarker target;
 
         private float score;
         private float pvalue;
 
         public MindyResultRow(DSGeneMarker modulator, DSGeneMarker transcriptionFactor, DSGeneMarker target, float score, float pvalue) {
             this.modulator = modulator;
             this.transcriptionFactor = transcriptionFactor;
             this.target = target;
             this.score = score;
             this.pvalue = pvalue;
         }
 
         public DSGeneMarker getModulator() {
             return modulator;
         }
 
         public void setModulator(DSGeneMarker modulator) {
             this.modulator = modulator;
         }
 
         public DSGeneMarker getTranscriptionFactor() {
             return transcriptionFactor;
         }
 
         public void setTranscriptionFactor(DSGeneMarker transcriptionFactor) {
             this.transcriptionFactor = transcriptionFactor;
         }
 
         public DSGeneMarker getTarget() {
             return target;
         }
 
         public void setTarget(DSGeneMarker target) {
             this.target = target;
         }
 
         public float getScore() {
             return score;
         }
 
         public void setScore(float score) {
             this.score = score;
         }
 
         public float getPvalue() {
             return pvalue;
         }
 
         public void setPvalue(float pvalue) {
             this.pvalue = pvalue;
         }
 
     }
 
    public static class ModulatorStatistics {
         protected int count;
         protected int mover;
         protected int munder;
 
         public ModulatorStatistics(int count, int mover, int munder) {
             this.count = count;
             this.mover = mover;
             this.munder = munder;
         }
 
         public int getCount() {
             return count;
         }
 
         public int getMover() {
             return mover;
         }
 
         public int getMunder() {
             return munder;
         }
     }
 
 }
