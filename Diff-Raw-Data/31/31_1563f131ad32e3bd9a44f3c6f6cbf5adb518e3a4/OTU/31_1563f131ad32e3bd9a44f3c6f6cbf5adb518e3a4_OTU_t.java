 package nzgot.cma;
 
 import nzgot.cma.util.NameParser;
 import nzgot.core.util.ArrayUtil;
 import nzgot.core.util.BioSortedSet;
 
 import java.util.Arrays;
 
 /**
  * OTU
  * elementsSet contains Reads
  * @author Walter Xie
  */
 public class OTU<E> extends BioSortedSet<E> {
 
     private int[] alphaDiversity;
     protected String refSeqId; // TODO convert to Sequence object
 
     public OTU(String name) {
         super(name);
     }
 
     public void setAlphaDiversity(int samplesBy, String[] samples) {
         initAlphaDiversity(samples);
 
         for (E read: this) {
             String sampleLocation = NameParser.getInstance().getSampleBy(samplesBy, read.toString());
             int i = ArrayUtil.indexOf(sampleLocation, samples);
            if (i < 0) {
                 throw new IllegalArgumentException("Error: missing sample location : " + sampleLocation +
                         " from samples array : " + Arrays.asList(samples));
            } else {
                alphaDiversity[i]++;
             }
         }
     }
 
     public int[] getAlphaDiversity() {
         return alphaDiversity;
     }
 
     public String getRefSeqId() {
         return refSeqId;
     }
 
     public void setRefSeqId(String refSeqId) {
         this.refSeqId = refSeqId;
     }
 
     private void initAlphaDiversity(String[] samples) {
         if (samples == null || samples.length < 1)
             throw new IllegalArgumentException("Error: sample array was not initialized: " + samples);
 
         alphaDiversity = new int[samples.length];
     }
 
 }
