 package edu.mit.wi.tagger;
 
 public class Allele{
     private static final char[] alleleNumChars = {'0','1','2','3','4'};
     private static final char[] alleleCodes = {'X','A','C','G','T'};
 
     private VariantSequence locus;
     private String genotypeString;
 
     public Allele(VariantSequence locus, String genotypeString) {
         this.locus = locus;
         this.genotypeString = genotypeString;
     }
 
     public VariantSequence getLocus() {
         return locus;
     }
 
     public String getGenotypeString(){
         String disp = new String(genotypeString);
         for (int i = 0; i < alleleCodes.length; i++){
             disp = disp.replace(alleleNumChars[i],alleleCodes[i]);
         }
 
         return disp;
     }
 
     public boolean equals(Object o) {
         if (o instanceof Allele){
             Allele a = (Allele)o;
             if (a.locus.equals(locus) && a.genotypeString.equals(genotypeString)){
                 return true;
             }
         }
         return false;
     }

    public int hashCode(){
        return locus.hashCode() + genotypeString.hashCode();
    }
 }
