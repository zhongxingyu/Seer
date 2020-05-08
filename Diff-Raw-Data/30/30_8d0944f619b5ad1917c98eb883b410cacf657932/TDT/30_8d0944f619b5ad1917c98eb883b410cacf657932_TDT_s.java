 package edu.mit.wi.haploview;
 
 import java.util.Vector;
 
 
 public class TDT {
 
     public static Vector calcTDT(Vector chromosomes) {
         Vector results = new Vector();
         int numMarkers = Chromosome.getSize();
 
         for(int k=0;k<numMarkers;k++){
             results.add(new TDTResult(Chromosome.getMarker(k)));
         }
 
         for(int i=0;i<chromosomes.size()-3;i++){
             Chromosome chrom1T = (Chromosome)chromosomes.get(i);
             i++;
             Chromosome chrom1U = (Chromosome)chromosomes.get(i);
             i++;
             Chromosome chrom2T = (Chromosome)chromosomes.get(i);
             i++;
             Chromosome chrom2U = (Chromosome)chromosomes.get(i);
 
 
             //System.out.println("ind1T: " + chrom1T.getPed() + "\t" + chrom1T.getIndividual() );
             //System.out.println("ind1U: " + chrom1U.getPed() + "\t" + chrom1U.getIndividual() );
             //System.out.println("ind2T: " + chrom2T.getPed() + "\t" + chrom2T.getIndividual() );
             //System.out.println("ind2U: " + chrom2U.getPed() + "\t" + chrom2U.getIndividual() );
             for(int j=0;j<numMarkers;j++){
                 byte allele1T = chrom1T.getGenotype(j);
                 byte allele1U = chrom1U.getGenotype(j);
                 byte allele2T = chrom2T.getGenotype(j);
                 byte allele2U = chrom2U.getGenotype(j);
 
                 TDTResult curRes = (TDTResult)results.get(j);
 
                 //System.out.println("marker "+ j + ":\t "  + allele1T + "\t" + allele1U + "\t" + allele2T + "\t" + allele2U);
                if(!(allele1T == 5 && allele1U == 5 && allele2T !=5 && allele2U != 5) &&
                        !(allele1T != 5 && allele1U != 5 && allele2T ==5 && allele2U == 5)) {
                    //handling missing kid parent het case
                    curRes.tallyInd(allele1T,allele1U);
                    curRes.tallyInd(allele2T,allele2U);
                }

             }
 
         }
         for(int i=0;i<results.size();i++){
             TDTResult tempRes = (TDTResult)results.get(i);
             int[][] counts = tempRes.counts;
             //System.out.println( counts[0][0] + "\t" + counts[1][1] + "\t" + counts[0][1] + "\t" + counts[1][0]);
         }
         return results;
     }
 }
