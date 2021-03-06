 package org.broadinstitute.sting.playground.gatk.walkers.varianteval;
 
 import org.broadinstitute.sting.gatk.refdata.AllelicVariant;
 import org.broadinstitute.sting.gatk.refdata.RefMetaDataTracker;
 import org.broadinstitute.sting.gatk.LocusContext;
 
 import java.io.PrintStream;
 import java.util.List;
 import java.util.ArrayList;
 
 public class VariantCounter extends BasicVariantAnalysis {
     int nBasesCovered = 0;
     int nSNPs = 0;
 
     public VariantCounter() {
         super("variant_counts");
     }
 
     public String update(AllelicVariant eval, RefMetaDataTracker tracker, char ref, LocusContext context) {
         nBasesCovered++;
         nSNPs += eval == null ? 0 : 1;
         return null;
     }
 
     public List<String> done() {
         List<String> s = new ArrayList<String>();
         s.add(String.format("n bases covered: %d", nBasesCovered));
         s.add(String.format("sites: %d", nSNPs));
        s.add(String.format("variant rate: %.5f confident variants per base", nSNPs / (1.0 * Math.max(nBasesCovered, 1))));
        s.add(String.format("variant rate: 1 / %d confident variants per base", nBasesCovered / Math.max(nSNPs, 1)));
         return s;
     }
 }
