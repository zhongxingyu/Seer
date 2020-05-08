 package edu.mit.wi.haploview;
 
 import edu.mit.wi.pedfile.MathUtil;
 import edu.mit.wi.pedfile.CheckDataException;
 
 
 public class TDTResult {
     int[][] counts;
     SNP theSNP;
     private byte allele1=0,allele2=0;
     int numZero;
     boolean tallyHet = false;
     double chiSqVal;
     boolean chiSet = false;
 
 
     public TDTResult() {
         counts = new int[2][2];
     }
 
     public TDTResult(SNP tempSNP) {
         counts = new int[2][2];
         this.theSNP = tempSNP;
     }
 
     public void tallyInd(byte alleleT, byte alleleU) {
         if(alleleT == 5 && alleleU == 5) {
             if(tallyHet){
                 counts[0][0]++;
                 counts[1][1]++;
                 counts[0][1]++;
                 counts[1][0]++;
                 this.tallyHet = false;
             }
             else {
                 this.tallyHet = true;
             }
         }
         else if( (alleleT != alleleU) && (alleleT!=0) && (alleleU!=0) ) {
             if(allele1==0 && allele2==0 ) {
                 allele1 = alleleT;
                 allele2 = alleleU;
             }
 
             if(alleleT == allele1) {
                 counts[0][0]++;
             }
             else if(alleleT==allele2) {
                 counts[0][1]++;
             }
             if(alleleU == allele1){
                 counts[1][0]++;
             }
             else if(alleleU == allele2) {
                 counts[1][1]++;
             }
 
         }
         //System.out.println( counts[0][0] + "\t" + counts[1][1] + "\t" + counts[0][1] + "\t" + counts[1][0]);
 
     }
 
     public String getName() {
         return this.theSNP.getName();
     }
 
     public double getChiSq() {
         if(!this.chiSet){
             this.chiSqVal = Math.pow( (this.counts[0][0] - this.counts[0][1]),2) / (this.counts[0][0] + this.counts[0][1]);
             this.chiSqVal = Math.rint(this.chiSqVal*1000.0)/1000.0;
             this.chiSet = true;
         }
         return this.chiSqVal;
     }
 
     public String getTURatio() {
         return this.counts[0][0] + ":" + this.counts[0][1];
     }
 
     public double getPValue() {
         double pval = 0;
        pval= MathUtil.gammq(.5,.5*getChiSq());
         return Math.rint(pval*10000.0)/10000.0;
     }
 
 }
