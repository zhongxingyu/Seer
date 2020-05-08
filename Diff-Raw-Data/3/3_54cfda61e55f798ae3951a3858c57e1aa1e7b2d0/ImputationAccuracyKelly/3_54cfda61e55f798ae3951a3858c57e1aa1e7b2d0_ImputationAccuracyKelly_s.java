 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Kelly;
 
 import java.io.BufferedOutputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Scanner;
 import net.maizegenetics.pal.alignment.Alignment;
 import net.maizegenetics.pal.alignment.AlignmentUtils;
 import net.maizegenetics.pal.alignment.ExportUtils;
 import net.maizegenetics.pal.alignment.FilterAlignment;
 import net.maizegenetics.pal.alignment.ImportUtils;
 import net.maizegenetics.pal.alignment.MutableNucleotideAlignment;
 import net.maizegenetics.pal.alignment.MutableNucleotideAlignmentHDF5;
 import net.maizegenetics.pal.alignment.NucleotideAlignmentConstants;
 import net.maizegenetics.pal.ids.IdGenerator;
 import net.maizegenetics.pal.ids.IdGroup;
 import net.maizegenetics.pal.ids.Identifier;
 import net.maizegenetics.prefs.TasselPrefs;
 import org.apache.commons.lang.ArrayUtils;
 
 /**
  *
  * @author kls283
  */
 public class ImputationAccuracyKelly {
     public static String dir;
     public static boolean[][] knownHetMask;
     public static boolean[][] calledHomoMajMask;
     public static boolean[][] calledHomoMinMask;
     public static boolean[][] calledMissingMask;
     public static boolean[] highCovTaxa;
     public static boolean[] highCovHets;
     public static boolean[] highCovInbreds;
     public static boolean[] outbred;
     public static boolean[] inbred;
     public static byte diploidN= NucleotideAlignmentConstants.getNucleotideDiploidByte("NN");
     private static byte knownBase;
     private static byte impBase;
     private static byte knownMaj;
     private static byte knownMin;
     private static boolean knownHet;
     private static byte[] impArray;
     private static byte[] knownArray;
     private static int[] matchTaxon;
     private static double[][] perSiteTaxon;
     private static int knownIndex;
     private static int knownSiteCount;
     
     
     public static void maskFileSample(String inFile, boolean gz, int sampleIntensity) {
         Alignment a= ImportUtils.readFromHapmap(dir+inFile+(gz==true?".hmp.txt.gz":".hmp.txt"), null);
         MutableNucleotideAlignment mna= MutableNucleotideAlignment.getInstance(a);
         for (int taxon= 0;taxon<a.getSequenceCount();taxon++) {            
             for (int site= taxon;site<a.getSiteCount();site+= sampleIntensity) {
                 mna.setBase(taxon, site, diploidN);
             }
         }
         mna.clean();
         ExportUtils.writeToHapmap(mna, true, dir+inFile+"_masked.hmp.txt.gz", '\t', null);
     }
     
     public static void maskFile55k(String maskFile, boolean gzMask, String knownFile, boolean gzKnown) {
         Alignment a= ImportUtils.readFromHapmap(dir+maskFile+(gzMask==true?".hmp.txt.gz":".hmp.txt"), null);
         MutableNucleotideAlignment mask= MutableNucleotideAlignment.getInstance(a);
         Alignment known= ImportUtils.readFromHapmap(dir+knownFile+(gzKnown==true?".hmp.txt.gz":".hmp.txt"), null);
         int[] knownPos= known.getPhysicalPositions();
         for (int site = 0; site < mask.getSiteCount(); site++) {
             int pos= mask.getPositionInLocus(site);
             if (Arrays.binarySearch(knownPos, pos)<0) continue;
             for (int taxon = 0; taxon < mask.getSequenceCount(); taxon++) {
                 mask.setBase(taxon, site, diploidN);
             }
         }
         mask.clean();
         ExportUtils.writeToHapmap(mask, true, dir+maskFile+"_masked55k.hmp.txt.gz", '\t', null);
     }
     //for depths 4 or more, requires hets to be called by more than one for less depth allele
     public static void maskFileByDepth(String depthFile, String inFile, int depthToMask, int maskDenom, boolean h5, boolean exportDepth) {
         System.out.println("Depth file: "+depthFile);
         System.out.println("File to mask: "+inFile);
         System.out.println("Site depth to mask: "+depthToMask);
         System.out.println("Divisor for physical positions to be masked: "+maskDenom);
         Alignment a= ImportUtils.readGuessFormat(inFile, true);
         MutableNucleotideAlignment mna= MutableNucleotideAlignment.getInstance(a);
         MutableNucleotideAlignment siteMna= MutableNucleotideAlignment.getInstance(a);
         System.out.println("Read in file to mask");
         MutableNucleotideAlignmentHDF5 mnah5= MutableNucleotideAlignmentHDF5.getInstance(depthFile);
         System.out.println("Read in depth file");
         int cnt= 0;
         String[] h5Taxa= new String[mnah5.getSequenceCount()];
         for (int taxon = 0; taxon < h5Taxa.length; taxon++) { h5Taxa[taxon]= mnah5.getFullTaxaName(taxon);}
         ArrayList<Identifier> remove= new ArrayList<>();
         for (int taxon = 0; taxon < mna.getSequenceCount(); taxon++) {
             int taxaCnt= 0;
             int h5Taxon= Arrays.binarySearch(h5Taxa, mna.getFullTaxaName(taxon));
             if (h5Taxon<0) {
                 System.out.println("Problem matching taxon "+mna.getFullTaxaName(taxon)+". Not masked and excluded.");
                 remove.add(mna.getIdGroup().getIdentifier(taxon));
                 continue;
             }
             for (int site = 0; site < mna.getSiteCount(); site++) {
                 siteMna.setBase(taxon, site, diploidN);
                 int h5Site= mnah5.getSiteOfPhysicalPosition(mna.getPositionInLocus(site), mna.getLocus(site));
                 byte[] currDepth= mnah5.getDepthForAlleles(h5Taxon, h5Site);
                 int[] currMinMaj= getIndexForMinMaj(mnah5.getMajorAllele(h5Site),mnah5.getMinorAllele(h5Site));
                 if (getReadDepthForAlleles(currDepth,currMinMaj)==depthToMask) {
                     if ((depthToMask>3&&((getReadDepthForAlleles(currDepth,currMinMaj[0])==1)||(getReadDepthForAlleles(currDepth,currMinMaj[1])!=1)))) continue;
                     if (mnah5.getPositionInLocus(h5Site)%maskDenom==0) {
                         mna.setBase(taxon, site, diploidN);
                         siteMna.setBase(taxon, site, mnah5.getBase(h5Taxon, h5Site));
                         taxaCnt++;
                         cnt++;
                     }
                 }   
             }
             System.out.println(taxaCnt+" sites masked for "+mna.getTaxaName(taxon));
         }
         System.out.println(cnt+" sites masked at a depth of "+depthToMask+" (site numbers that can be divided by "+maskDenom+")");
         System.out.println(remove.size()+" taxa not masked due to taxa name mismatch and excluded from output files");
         System.out.println(mna.getSequenceCount()-remove.size()+" total taxa output");
         
         IdGroup removeIDs= IdGenerator.createIdGroup(remove.size());
         for (int i = 0; i < remove.size(); i++) {removeIDs.setIdentifier(i, remove.get(i));}
         mna.clean();
         siteMna.clean();
         Alignment filterMna= FilterAlignment.getInstanceRemoveIDs(mna, removeIDs);
         Alignment filterSiteMna= FilterAlignment.getInstanceRemoveIDs(siteMna, removeIDs);
         
         if (h5==true) {
             if (exportDepth==true) ExportUtils.writeToMutableHDF5((Alignment)filterMna, inFile.length()-6+"_maskedDepth"+depthToMask+"_Denom"+maskDenom, null, true);
             else {
                 ExportUtils.writeToMutableHDF5(filterMna,inFile.substring(0, inFile.length()-6)+"_masked_Depth"+depthToMask+"_Denom"+maskDenom+".hmp.h5", null, false);
                 ExportUtils.writeToMutableHDF5(filterSiteMna,inFile.substring(0, inFile.length()-6)+"_maskKey_Depth"+depthToMask+"_Denom"+maskDenom+".hmp.h5", null, false);
             }
         }
         else {
             ExportUtils.writeToHapmap(filterMna, true, inFile.substring(0, inFile.length()-6)+"_masked_Depth"+depthToMask+"_Denom"+maskDenom+".hmp.txt.gz", '\t', null);
             ExportUtils.writeToHapmap(filterSiteMna, true, inFile.substring(0, inFile.length()-6)+"_maskKey_Depth"+depthToMask+"_Denom"+maskDenom+".hmp.txt.gz", '\t', null);
         }
     }
     
     //for depths 4 or more, requires hets to be called by more than one for less depth allele
     public static void maskBaseFileByDepth(String depthFile, int depthToMask, int maskDenom, boolean exportDepth) {
         System.out.println("Depth file: "+depthFile);
         System.out.println("Site depth to mask: "+depthToMask);
         System.out.println("Divisor for physical positions to be masked: "+maskDenom);
         MutableNucleotideAlignmentHDF5 mnah5= MutableNucleotideAlignmentHDF5.getInstance(depthFile);
         System.out.println("Generate file to mask");
         String outMasked= depthFile.substring(0, depthFile.length()-7)+"_masked_Depth"+depthToMask+"_Denom"+maskDenom+".hmp.h5";
         String outKey= depthFile.substring(0, depthFile.length()-7)+"_maskKey_Depth"+depthToMask+"_Denom"+maskDenom+".hmp.h5";
         ExportUtils.writeToMutableHDF5(mnah5,outMasked, null, exportDepth);
         ExportUtils.writeToMutableHDF5(mnah5,outKey, null, false);
         MutableNucleotideAlignmentHDF5 mna= MutableNucleotideAlignmentHDF5.getInstance(outMasked);
         System.out.println("read back in maskedFile: "+outMasked);
         MutableNucleotideAlignmentHDF5 key= MutableNucleotideAlignmentHDF5.getInstance(outKey);
         System.out.println("read back in keyFile: "+outKey);
         int cnt= 0;
        int taxaCnt= 0;
         System.out.println("Starting mask...");
         for (int taxon = 0; taxon < mna.getSequenceCount(); taxon++) {
             byte[] taxonMask= new byte[mna.getSiteCount()];
             byte[] taxonKey= new byte[mna.getSiteCount()];
             for (int site = 0; site < mna.getSiteCount(); site++) {
                 taxonKey[site]= diploidN;
                 taxonMask[site]= mnah5.getBase(taxon, site);
                 byte[] currDepth= mnah5.getDepthForAlleles(taxon, site);
                 int[] currMinMaj= getIndexForMinMaj(mnah5.getMajorAllele(site),mnah5.getMinorAllele(site));
                 if (getReadDepthForAlleles(currDepth,currMinMaj)==depthToMask) {
                     if ((depthToMask>3&&((getReadDepthForAlleles(currDepth,currMinMaj[0])==1)||(getReadDepthForAlleles(currDepth,currMinMaj[1])!=1)))) continue;
                     if (mnah5.getPositionInLocus(site)%maskDenom==0) {
                         taxonMask[site]= diploidN;
                         taxonKey[site]= mnah5.getBase(taxon, site);
                         taxaCnt++;
                         cnt++;
                     }
                 }   
             }
             mna.setAllBases(taxon, taxonMask);
             key.setAllBases(taxon, taxonKey);
             
             System.out.println(taxaCnt+" sites masked for "+mna.getTaxaName(taxon));
         }
         System.out.println(cnt+" sites masked at a depth of "+depthToMask+" (site numbers that can be divided by "+maskDenom+")");
         mna.clean();
         key.clean();
     }
     
     //returns an array the length of key.getSiteCount() that contains the MAF class of the donor file or -1 if not included
     public static int[] readInMAFFile (String donorMAFFile, Alignment key, double[] MAFClass) {
         int[] MAF= new int[key.getSiteCount()];
         for (int i = 0; i < MAF.length; i++) {MAF[i]= -1;} //initialize to -1
         try {
             FileInputStream fis= new FileInputStream(donorMAFFile);
             Scanner scanner= new Scanner(fis);
             do {
                 String next= scanner.nextLine();
                 if (next.isEmpty()) break;
                 String[] vals= next.split("\t");
                 int site= key.getSiteOfPhysicalPosition(Integer.parseInt(vals[1]),key.getLocus(Integer.parseInt(vals[0])));
                 if (site<0) continue;
                 int currClass= Arrays.binarySearch(MAFClass, Double.parseDouble(vals[2]));
                 MAF[site]= currClass<0?Math.abs(currClass)-1:currClass;
             }
             while (scanner.hasNextLine());
             scanner.close();
             fis.close();
         }
         catch (Exception e) {
             System.out.println("Problem reading in taxa names");
         }
         return MAF;
     }
     
     public static void accuracyDepth(String keyFile, String imputedFileName, String donorMAFFile, double[] MAFClass) {
         Alignment index = ImportUtils.readGuessFormat(keyFile, false);
         Alignment imputed = ImportUtils.readGuessFormat(imputedFileName, false);
         double[] hetAll = new double[5]; //0total count, 1good, 2toOneCorrectHomo, 3unimp, 4wrong
         double[] minorAll = new double[5]; //0total count, 1good, 2toHet, 3unimp, 4wrong
         double[] majorAll = new double[5]; //0total count, 1good, 2toHet, 3unimp, 4wrong
         double[][] het = new double[5][imputed.getSequenceCount()]; //0total count, 1good, 2toOneCorrectHomo, 3unimp
         double[][] minor = new double[5][imputed.getSequenceCount()]; //0total count, 1good, 2toHet, 3unimp
         double[][] major = new double[5][imputed.getSequenceCount()]; //0total count, 1good, 2toHet, 3unimp
         DecimalFormat df = new DecimalFormat("0.#####");
         System.out.println("Imputed file: "+imputedFileName+"\nKey file: "+keyFile);
         System.out.println("Taxon\tTotalSitesCompared\tNumHets\tCorrectHet\tPartialCorrectHet\tUnimputedHet\tWrongHet\tNumMinor\tCorrectMinor\tMinorToHet\tUnimputedMinor\t"
                     + "WrongMinor\tNumMajor\tCorrectMajor\tMajorToHet\tUnimputedMajor\tWrongMajor\tOverallError");
         boolean MAFon= false;
         double[] hetMAFWrong= null;//MAF taken from donor file
         double[] minorMAFWrong= null;//MAF taken from donor file
         double[] hetMAFPart= null;// MAF taken from donor file
         double[] minorMAFPart= null;//MAF taken from donor file
         int[] MAF= null;
         if (donorMAFFile!=null&&MAFClass!=null) {
             MAFon= true;
             if (MAFClass[MAFClass.length-1]!=1) ArrayUtils.add(MAFClass, 1);
             hetMAFWrong= new double[MAFClass.length+1];//MAF taken from donor file
             minorMAFWrong= new double[MAFClass.length+1];//MAF taken from donor file
             hetMAFPart= new double[MAFClass.length+1];// MAF taken from donor file
             minorMAFPart= new double[MAFClass.length+1];//MAF taken from donor file
             MAF= readInMAFFile(donorMAFFile, index, MAFClass);
         }
         for (int taxon = 0; taxon < imputed.getSequenceCount(); taxon++) {
             for (int site = 0; site < imputed.getSiteCount(); site++) {
                 byte known = index.getBase(taxon, site);
                 if (known == diploidN) continue;
                 else {
                     byte imp = imputed.getBase(taxon, site);
                     if (index.isHeterozygous(taxon, site) == true) {
                         het[0][taxon]++;
                         if (imp == diploidN) het[3][taxon]++;
                         else if (AlignmentUtils.isEqual(imp, known) == true) het[1][taxon]++;
                         else if (AlignmentUtils.isHeterozygous(imp) == false && AlignmentUtils.isPartiallyEqual(imp, known) == true) {
                             het[2][taxon]++;
                             if (MAFon) {
                                 if (MAF[site]<0) hetMAFPart[hetMAFPart.length-1]++; else hetMAFPart[MAF[site]]++;
                             }
                         }
                         else {
                             het[4][taxon]++;
                             if (MAFon) {
                                 if (MAF[site]<0) hetMAFWrong[hetMAFPart.length-1]++; else hetMAFWrong[MAF[site]]++;
                             }
                         }
                     } 
                     else if (index.getBaseArray(taxon, site)[0] == imputed.getMinorAllele(site)) {
                         minor[0][taxon]++;
                         if (imp == diploidN) minor[3][taxon]++;
                         else if (AlignmentUtils.isEqual(imp, known) == true) minor[1][taxon]++;
                         else if (AlignmentUtils.isHeterozygous(imp) == true && AlignmentUtils.isPartiallyEqual(imp, known) == true) {
                             minor[2][taxon]++;
                             if (MAFon) {
                                 if (MAF[site]<0) minorMAFPart[minorMAFPart.length-1]++; else minorMAFPart[MAF[site]]++;
                             }
                         }
                         else {
                             minor[4][taxon]++;
                             if (MAFon) {
                                 if (MAF[site]<0) minorMAFWrong[minorMAFWrong.length-1]++; else minorMAFWrong[MAF[site]]++;
                             }
                         }
                     }
                     else if (index.getBaseArray(taxon, site)[0] == imputed.getMajorAllele(site)) {
                         major[0][taxon]++;
                         if (imp == diploidN) major[3][taxon]++;
                         else if (AlignmentUtils.isEqual(imp, known) == true) major[1][taxon]++;
                         else if (AlignmentUtils.isHeterozygous(imp) == true && AlignmentUtils.isPartiallyEqual(imp, known) == true) major[2][taxon]++;
                         else major[4][taxon]++;
                     }
                     else continue;
                 }
             }
             System.out.println(imputed.getFullTaxaName(taxon) + "\t" + (int) ((het[0][taxon] + minor[0][taxon] + major[0][taxon])) + "\t" + (int) het[0][taxon] + "\t" + (het[0][taxon] == 0 ? 0 : df.format(het[1][taxon] / het[0][taxon])) + "\t" + (het[0][taxon] == 0 ? 0 : df.format(het[2][taxon] / het[0][taxon])) + "\t" + (het[0][taxon] == 0 ? 0 : df.format(het[3][taxon] / het[0][taxon])) + "\t"
                     + (het[0][taxon] == 0 ? 0 : df.format(het[4][taxon] / het[0][taxon])) + "\t" + (int) minor[0][taxon] + "\t" + (minor[0][taxon] == 0 ? 0 : df.format(minor[1][taxon] / minor[0][taxon])) + "\t" + (minor[0][taxon] == 0 ? 0 : df.format(minor[2][taxon] / minor[0][taxon])) + "\t" + (minor[0][taxon] == 0 ? 0 : df.format(minor[3][taxon] / minor[0][taxon])) + "\t"
                     + (minor[0][taxon] == 0 ? 0 : df.format(minor[4][taxon] / minor[0][taxon])) + "\t" + (int) major[0][taxon] + "\t" + (major[0][taxon] == 0 ? 0 : df.format(major[1][taxon] / major[0][taxon])) + "\t" + (major[0][taxon] == 0 ? 0 : df.format(major[2][taxon] / major[0][taxon])) + "\t" + (major[0][taxon] == 0 ? 0 : df.format(major[3][taxon] / major[0][taxon])) + "\t" + (major[0][taxon] == 0 ? 0 : df.format(major[4][taxon] / major[0][taxon])));
             for (int i = 0; i < major.length; i++) {hetAll[i] += het[i][taxon]; minorAll[i] += minor[i][taxon]; majorAll[i] += major[i][taxon];}
         }
         try {
             File outputFile = new File(imputedFileName.substring(0, imputedFileName.indexOf(".hmp.h5")) + "DepthAccuracy.txt");
             DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
             outStream.writeBytes("Taxon\tTotalSitesCompared\tNumHets\tCorrectHet\tPartialCorrectHet\tUnimputedHet\tWrongHet\tNumMinor\tCorrectMinor\tMinorToHet\tUnimputedMinor\t"
                     + "WrongMinor\tNumMajor\tCorrectMajor\tMajorToHet\tUnimputedMajor\tWrongMajor\tOverallError\n");
             for (int taxon = 0; taxon < imputed.getSequenceCount(); taxon++) {
                 outStream.writeBytes(imputed.getFullTaxaName(taxon) + "\t" + (int) ((het[0][taxon] + minor[0][taxon] + major[0][taxon])) + "\t" + (int) het[0][taxon] + "\t" + (het[0][taxon] == 0 ? 0 : df.format(het[1][taxon] / het[0][taxon])) + "\t" + (het[0][taxon] == 0 ? 0 : df.format(het[2][taxon] / het[0][taxon])) + "\t" + (het[0][taxon] == 0 ? 0 : df.format(het[3][taxon] / het[0][taxon])) + "\t"
                         + (het[0][taxon] == 0 ? 0 : df.format(het[4][taxon] / het[0][taxon])) + "\t" + (int) minor[0][taxon] + "\t" + (minor[0][taxon] == 0 ? 0 : df.format(minor[1][taxon] / minor[0][taxon])) + "\t" + (minor[0][taxon] == 0 ? 0 : df.format(minor[2][taxon] / minor[0][taxon])) + "\t" + (minor[0][taxon] == 0 ? 0 : df.format(minor[3][taxon] / minor[0][taxon])) + "\t"
                         + (minor[0][taxon] == 0 ? 0 : df.format(minor[4][taxon] / minor[0][taxon])) + "\t" + (int) major[0][taxon] + "\t" + (major[0][taxon] == 0 ? 0 : df.format(major[1][taxon] / major[0][taxon])) + "\t" + (major[0][taxon] == 0 ? 0 : df.format(major[2][taxon] / major[0][taxon])) + "\t" + (major[0][taxon] == 0 ? 0 : df.format(major[3][taxon] / major[0][taxon])) + "\t" 
                         + (major[0][taxon] == 0 ? 0 : df.format(major[4][taxon] / major[0][taxon])) + "\n");
             }
             outStream.writeBytes("TotalByImputed\t" + (int) ((hetAll[0] + minorAll[0] + majorAll[0])) + "\t" + (int) hetAll[0] + "\t" + (hetAll[0] == 0 ? 0 : df.format(hetAll[1] / (hetAll[0]-hetAll[3]))) + "\t" + (hetAll[0] == 0 ? 0 : df.format(hetAll[2] / (hetAll[0]-hetAll[3]))) + "\t" + (hetAll[0] == 0 ? 0 : df.format(hetAll[3] / (hetAll[0]))) + "\t"
                         + (hetAll[0] == 0 ? 0 : df.format(hetAll[4] / (hetAll[0]-hetAll[3]))) + "\t" + (int) minorAll[0] + "\t" + (minorAll[0] == 0 ? 0 : df.format(minorAll[1] / (minorAll[0]-minorAll[3]))) + "\t" + (minorAll[0] == 0 ? 0 : df.format(minorAll[2] / (minorAll[0]-minorAll[3]))) + "\t" + (minorAll[0] == 0 ? 0 : df.format(minorAll[3] / (minorAll[0]))) + "\t"
                         + (minorAll[0] == 0 ? 0 : df.format(minorAll[4] / (minorAll[0]-minorAll[3]))) + "\t" + (int) majorAll[0] + "\t" + (majorAll[0] == 0 ? 0 : df.format(majorAll[1] / (majorAll[0]-majorAll[3]))) + "\t" + (majorAll[0] == 0 ? 0 : df.format(majorAll[2] / (majorAll[0]-majorAll[3]))) + "\t" + (majorAll[0] == 0 ? 0 : df.format(majorAll[3] / (majorAll[0]))) + "\t" 
                         + (majorAll[0] == 0 ? 0 : df.format(majorAll[4] / (majorAll[0]-majorAll[3]))) + "\t" + df.format((minorAll[4]+majorAll[4]+hetAll[4]) / (minorAll[0]+majorAll[0]+hetAll[0]-minorAll[3]-majorAll[3]+hetAll[3])) + "\n");
             outStream.writeBytes("Total\t" + (int) ((hetAll[0] + minorAll[0] + majorAll[0])) + "\t" + (int) hetAll[0] + "\t" + (hetAll[0] == 0 ? 0 : df.format(hetAll[1] / hetAll[0])) + "\t" + (hetAll[0] == 0 ? 0 : df.format(hetAll[2] / hetAll[0])) + "\t" + (hetAll[0] == 0 ? 0 : df.format(hetAll[3] / hetAll[0])) + "\t"
                         + (hetAll[0] == 0 ? 0 : df.format(hetAll[4] / hetAll[0])) + "\t" + (int) minorAll[0] + "\t" + (minorAll[0] == 0 ? 0 : df.format(minorAll[1] / minorAll[0])) + "\t" + (minorAll[0] == 0 ? 0 : df.format(minorAll[2] / minorAll[0])) + "\t" + (minorAll[0] == 0 ? 0 : df.format(minorAll[3] / minorAll[0])) + "\t"
                         + (minorAll[0] == 0 ? 0 : df.format(minorAll[4] / minorAll[0])) + "\t" + (int) majorAll[0] + "\t" + (majorAll[0] == 0 ? 0 : df.format(majorAll[1] / majorAll[0])) + "\t" + (majorAll[0] == 0 ? 0 : df.format(majorAll[2] / majorAll[0])) + "\t" + (majorAll[0] == 0 ? 0 : df.format(majorAll[3] / majorAll[0])) + "\t" 
                         + (majorAll[0] == 0 ? 0 : df.format(majorAll[4] / majorAll[0]))+ "\t" + df.format((minorAll[4]+majorAll[4]+hetAll[4]) / (minorAll[0]+majorAll[0]+hetAll[0]))+"\n");
             if (MAFon) {
                 outStream.writeBytes("Site MAF of donor file by class:\nClass");
                 for(double i:MAFClass){outStream.writeBytes("\t<"+i);}
                 outStream.writeBytes("\nPartialHets");for(double i:hetMAFPart){outStream.writeBytes("\t"+(hetAll[2]==0?0:df.format(i/(hetAll[2])-hetMAFPart[hetMAFPart.length-1])));}
                 outStream.writeBytes("\nWrongHets");for(double i:hetMAFWrong){outStream.writeBytes("\t"+(hetAll[4]==0?0:df.format(i/(hetAll[4])-hetMAFWrong[hetMAFWrong.length-1])));}
                 outStream.writeBytes("\nMinorToHet");for(double i:minorMAFPart){outStream.writeBytes("\t"+(minorAll[2]==0?0:df.format(i/(minorAll[2])-minorMAFPart[minorMAFPart.length-1])));}
                 outStream.writeBytes("\nWrongMinor");for(double i:minorMAFWrong){outStream.writeBytes("\t"+(minorAll[4]==0?0:df.format(i/(minorAll[4])-minorMAFWrong[minorMAFWrong.length-1])));}
             }
             System.out.println("TotalByImputed\t" + (int) ((hetAll[0] + minorAll[0] + majorAll[0])) + "\t" + (int) hetAll[0] + "\t" + (hetAll[0] == 0 ? 0 : df.format(hetAll[1] / (hetAll[0]-hetAll[3]))) + "\t" + (hetAll[0] == 0 ? 0 : df.format(hetAll[2] / (hetAll[0]-hetAll[3]))) + "\t" + (hetAll[0] == 0 ? 0 : df.format(hetAll[3] / (hetAll[0]))) + "\t"
                         + (hetAll[0] == 0 ? 0 : df.format(hetAll[4] / (hetAll[0]-hetAll[3]))) + "\t" + (int) minorAll[0] + "\t" + (minorAll[0] == 0 ? 0 : df.format(minorAll[1] / (minorAll[0]-minorAll[3]))) + "\t" + (minorAll[0] == 0 ? 0 : df.format(minorAll[2] / (minorAll[0]-minorAll[3]))) + "\t" + (minorAll[0] == 0 ? 0 : df.format(minorAll[3] / (minorAll[0]))) + "\t"
                         + (minorAll[0] == 0 ? 0 : df.format(minorAll[4] / (minorAll[0]-minorAll[3]))) + "\t" + (int) majorAll[0] + "\t" + (majorAll[0] == 0 ? 0 : df.format(majorAll[1] / (majorAll[0]-majorAll[3]))) + "\t" + (majorAll[0] == 0 ? 0 : df.format(majorAll[2] / (majorAll[0]-majorAll[3]))) + "\t" + (majorAll[0] == 0 ? 0 : df.format(majorAll[3] / (majorAll[0]))) + "\t" 
                         + (majorAll[0] == 0 ? 0 : df.format(majorAll[4] / (majorAll[0]-majorAll[3])))+ "\t" + df.format((minorAll[4]+majorAll[4]+hetAll[4]) / (minorAll[0]+majorAll[0]+hetAll[0]-minorAll[3]-majorAll[3]+hetAll[3])));
             System.out.println("Total\t" + (int) ((hetAll[0] + minorAll[0] + majorAll[0])) + "\t" + (int) hetAll[0] + "\t" + (hetAll[0] == 0 ? 0 : df.format(hetAll[1] / hetAll[0])) + "\t" + (hetAll[0] == 0 ? 0 : df.format(hetAll[2] / hetAll[0])) + "\t" + (hetAll[0] == 0 ? 0 : df.format(hetAll[3] / hetAll[0])) + "\t"
                         + (hetAll[0] == 0 ? 0 : df.format(hetAll[4] / hetAll[0])) + "\t" + (int) minorAll[0] + "\t" + (minorAll[0] == 0 ? 0 : df.format(minorAll[1] / minorAll[0])) + "\t" + (minorAll[0] == 0 ? 0 : df.format(minorAll[2] / minorAll[0])) + "\t" + (minorAll[0] == 0 ? 0 : df.format(minorAll[3] / minorAll[0])) + "\t"
                         + (minorAll[0] == 0 ? 0 : df.format(minorAll[4] / minorAll[0])) + "\t" + (int) majorAll[0] + "\t" + (majorAll[0] == 0 ? 0 : df.format(majorAll[1] / majorAll[0])) + "\t" + (majorAll[0] == 0 ? 0 : df.format(majorAll[2] / majorAll[0])) + "\t" + (majorAll[0] == 0 ? 0 : df.format(majorAll[3] / majorAll[0])) + "\t" 
                         + (majorAll[0] == 0 ? 0 : df.format(majorAll[4] / majorAll[0]))+ "\t" + df.format((minorAll[4]+majorAll[4]+hetAll[4]) / (minorAll[0]+majorAll[0]+hetAll[0])));
             if (MAFon) {
                 System.out.println("Site MAF of donor file by class:\nClass (<=, 0 is invariant)\tPartialhets\tWrongHets\tMinorToHet\tWrongMinor");
                 for(int i= 0;i<hetMAFPart.length-1;i++){
                     System.out.println(MAFClass[i]+"\t"+(hetAll[2]==0?0:df.format(hetMAFPart[i]/(hetAll[2]-hetMAFPart[hetMAFPart.length-1])))+"\t"+(hetAll[4]==0?0:df.format(hetMAFWrong[i]/(hetAll[4]-hetMAFWrong[hetMAFWrong.length-1])))+
                             "\t"+(minorAll[2]==0?0:df.format(minorMAFPart[i]/(minorAll[2]-minorMAFPart[minorMAFPart.length-1])))+"\t"+(minorAll[4]==0?0:df.format(minorMAFWrong[i]/(minorAll[4]-minorMAFWrong[minorMAFWrong.length-1]))));
                 }
             }
             outStream.close();
         } catch (Exception e) {
             System.out.println(e);
         }
     }
     
     private static int[] getIndexForMinMaj(byte maj, byte min) {
         int[] minMaj= new int[2];
         minMaj[0]= maj==NucleotideAlignmentConstants.A_ALLELE?0:maj==NucleotideAlignmentConstants.C_ALLELE?
                 1:maj==NucleotideAlignmentConstants.G_ALLELE?2:maj==NucleotideAlignmentConstants.T_ALLELE?
                 3:maj==NucleotideAlignmentConstants.INSERT_ALLELE?4:5;
         minMaj[1]= min==NucleotideAlignmentConstants.A_ALLELE?0:min==NucleotideAlignmentConstants.C_ALLELE?
                 1:min==NucleotideAlignmentConstants.G_ALLELE?2:min==NucleotideAlignmentConstants.T_ALLELE?
                 3:min==NucleotideAlignmentConstants.INSERT_ALLELE?4:5;
         return minMaj;
     }
     
     public static int getReadDepthForAlleles(byte[] depthForAlleles, int[] alleleIndices) {
         int depth= 0;
         for (int all: alleleIndices) {
             depth+= (int)depthForAlleles[all];
         }
         return depth;
     }
     
     public static int getReadDepthForAlleles(byte[] depthForAlleles, int allele) {
         int depth= (int)depthForAlleles[allele];
         return depth;
     }
     
     public static double getTrue(boolean[] mask) {
         int count= 0;
         for (int i= 0;i<mask.length;i++) {
             if (mask[i]==true) count++;
         }
         return count;
     }  
     
     public static double getTrue(boolean[][] mask) {
         int count= 0;
         for (int i= 0;i<mask.length;i++) {
             for (int j= 0;j<mask[0].length;j++) {
                 if (mask[i][j]==true) count++;
             }   
         }
         return count;
     }
     
     public static double getTrue(boolean[][] mask1, boolean[] mask2) {
         int count= 0;
         for (int i= 0;i<mask1.length;i++) {
             for (int j= 0;j<mask1[0].length;j++) {
                 if (mask1[i][j]==true&&mask2[i]==true) count++;
             }   
         }
         return count;
     }
     
     public static double getTrueOr(boolean[][] mask1, boolean[][] mask2) {
         int count= 0;
         for (int i= 0;i<mask1.length;i++) {
             for (int j= 0;j<mask1[0].length;j++) {
                 if (mask1[i][j]==true||mask2[i][j]==true) count++;
             }   
         }
         return count;
     }
     
     public static double getTrueAndOr(boolean[][] mask1Or, boolean[][] mask2Or, boolean[] mask3And) {
         int count= 0;
         for (int i= 0;i<mask1Or.length;i++) {
             for (int j= 0;j<mask1Or[0].length;j++) {
                 if (mask1Or[i][j]==true||mask2Or[i][j]==true) {
                     if (mask3And[i]==true) count++;
                         }
             }   
         }
         return count;
     }
     
     public static void makeMasks55k(Alignment known, Alignment unimputed, double cov, double het) {
         int covCutoff= (int)(cov*(double) unimputed.getSiteCount()); //cutoff for number of sites present to be high cov
         int hetCutoff= (int)(het*(double) unimputed.getSiteCount());
         int inbredCutoff= (int)(.006*(double) unimputed.getSiteCount());
         knownHetMask= new boolean[unimputed.getSequenceCount()][unimputed.getSiteCount()];
         calledHomoMajMask= new boolean[unimputed.getSequenceCount()][unimputed.getSiteCount()];
         calledHomoMinMask= new boolean[unimputed.getSequenceCount()][unimputed.getSiteCount()];
         calledMissingMask= new boolean[unimputed.getSequenceCount()][unimputed.getSiteCount()];
         highCovTaxa= new boolean[unimputed.getSequenceCount()];
         highCovInbreds= new boolean[unimputed.getSequenceCount()];
         highCovHets= new boolean[unimputed.getSequenceCount()];
         outbred= new boolean[unimputed.getSequenceCount()];
         inbred= new boolean[unimputed.getSequenceCount()];
         for (int taxon = 0; taxon < unimputed.getSequenceCount(); taxon++) {
             if (unimputed.getTotalNotMissingForTaxon(taxon)>covCutoff) highCovTaxa[taxon]= true;
             if (unimputed.getHeterozygousCountForTaxon(taxon)>hetCutoff) outbred[taxon]= true;
             if (unimputed.getHeterozygousCountForTaxon(taxon)<inbredCutoff) inbred[taxon]= true;
             if (highCovTaxa[taxon]==true&&outbred[taxon]==true) highCovHets[taxon]= true;
             if (highCovTaxa[taxon]==true&&inbred[taxon]==true) highCovInbreds[taxon]= true;
         }
         
         matchTaxon= new int[unimputed.getSequenceCount()];//holds the index of corresponding taxon in known
         String[] knownNames= new String[known.getSequenceCount()];
         for (int taxon = 0; taxon < knownNames.length; taxon++) {
             knownNames[taxon]= known.getIdGroup().getIdentifier(taxon).getNameLevel(0);
         }
         
         for (int taxon = 0; taxon < matchTaxon.length; taxon++) {
             String unkName= unimputed.getIdGroup().getIdentifier(taxon).getNameLevel(0);
             matchTaxon[taxon]= Arrays.binarySearch(knownNames, unkName);
         }
         
         int[] knownPos= known.getPhysicalPositions();
         for (int site = 0; site < unimputed.getSiteCount(); site++) {
             int matchSite= known.getSiteOfPhysicalPosition(unimputed.getPositionInLocus(site), null);
             if (Arrays.binarySearch(knownPos, unimputed.getPositionInLocus(site))<0) continue;
             byte diploidMaj= AlignmentUtils.getDiploidValue(unimputed.getMajorAllele(site), unimputed.getMajorAllele(site));
             byte diploidMin= AlignmentUtils.getDiploidValue(unimputed.getMinorAllele(site), unimputed.getMinorAllele(site));
             
             for (int taxon = 0; taxon < unimputed.getSequenceCount(); taxon++) {
                 if (known.isHeterozygous(matchTaxon[taxon], matchSite)==true) knownHetMask[taxon][site]= true;
                 else if (known.getBase(matchTaxon[taxon], matchSite)==diploidMaj) calledHomoMajMask[taxon][site]= true;
                 else if (known.getBase(matchTaxon[taxon], matchSite)==diploidMin) calledHomoMinMask[taxon][site]= true;
                 else if (known.getBase(matchTaxon[taxon],matchSite)==diploidN) calledMissingMask[taxon][site]= true;
             }
         }
         System.out.println("Parameters:\nHigh Coverage Cutoff: "+cov+"\nOutbred Cutoff:"+het+"\nInbred Cutoff: .006"
                     +"\n\nNumber of siteTaxa considered:\n"+"\nknownHets: "+
                     getTrue(knownHetMask)+" (highHet/highHomo: "+
                     getTrue(knownHetMask, highCovHets)+"/"+
                     getTrue(knownHetMask, highCovInbreds)+")"+"\n"+
                     "calledHomoMaj"+getTrue(calledHomoMajMask)+" (highHet/highHomo: "+
                     getTrue(calledHomoMajMask, highCovHets)+"/"+
                     getTrue(calledHomoMajMask, highCovInbreds)+")"+"\n"+
                     "calledHomoMin"+getTrue(calledHomoMinMask)+" (highHet/highHomo: "+
                     getTrue(calledHomoMinMask, highCovHets)+"/"+
                     getTrue(calledHomoMinMask, highCovInbreds)+")"+"\n"+
                     "calledMissing"+getTrue(calledMissingMask)+"\n\n"+
                     "Number of sequences in each group:\nTotalNumSequences: "+known.getSequenceCount()+"\nhighCovTaxa"+getTrue(highCovTaxa)+"\n"+
                     "highCovInbred"+getTrue(highCovInbreds)+"\n"+
                     "highCovHets"+getTrue(highCovHets)+"\n"+
                     "inbred"+getTrue(inbred)+"\n"+
                     "outbred"+getTrue(outbred)+"\n");
     }
     
     public static void makeMasks(Alignment known, int sampleIntensity, double cov, double het) { //het, homo, missing, highCov
         int covCutoff= (int)(cov*(double) known.getSiteCount()); //cutoff for number of sites present to be high cov
         int hetCutoff= (int)(het*(double) known.getSiteCount());
         int inbredCutoff= (int)(.006*(double) known.getSiteCount());
         knownHetMask= new boolean[known.getSequenceCount()][known.getSiteCount()];
         calledHomoMajMask= new boolean[known.getSequenceCount()][known.getSiteCount()];
         calledHomoMinMask= new boolean[known.getSequenceCount()][known.getSiteCount()];
         calledMissingMask= new boolean[known.getSequenceCount()][known.getSiteCount()];
         highCovTaxa= new boolean[known.getSequenceCount()];
         highCovInbreds= new boolean[known.getSequenceCount()];
         highCovHets= new boolean[known.getSequenceCount()];
         outbred= new boolean[known.getSequenceCount()];
         inbred= new boolean[known.getSequenceCount()];
         
         for (int taxon= 0;taxon<known.getSequenceCount();taxon++) {
             if (known.getTotalNotMissingForTaxon(taxon)>covCutoff) highCovTaxa[taxon]= true;
             if (known.getHeterozygousCountForTaxon(taxon)>hetCutoff) outbred[taxon]= true;
             if (known.getHeterozygousCountForTaxon(taxon)<inbredCutoff) inbred[taxon]= true;
             if (highCovTaxa[taxon]==true&&outbred[taxon]==true) highCovHets[taxon]= true;
             if (highCovTaxa[taxon]==true&&inbred[taxon]==true) highCovInbreds[taxon]= true;
             for (int site= taxon;site<known.getSiteCount();site+= sampleIntensity) {
                 if (known.getBase(taxon, site)==diploidN) 
                     calledMissingMask[taxon][site]= true;
                 else if (known.isHeterozygous(taxon, site)) 
                     knownHetMask[taxon][site]= true;                
                 else if (known.getBaseArray(taxon, site)[0]==known.getMajorAllele(site)||known.getBaseArray(taxon, site)[1]==known.getMajorAllele(site)) 
                     calledHomoMajMask[taxon][site]= true;
                 else if (known.getBaseArray(taxon, site)[0]==known.getMinorAllele(site)||known.getBaseArray(taxon, site)[1]==known.getMinorAllele(site))
                     calledHomoMinMask[taxon][site]= true;                
             }
         }
 //        //get sites for high coverage hets in HW proportions
 //        IdGroup highCovHetTaxa= IdGroupUtils.idGroupSubset(known.getIdGroup(), highCovHets);
 //        Alignment highCovAlign= FilterAlignment.getInstance(known, highCovHetTaxa);
 //        for (int site= 0;site<known.getSiteCount();site++) {
 //            double p= highCovAlign.getMajorAlleleFrequency(site);
 //            double q= highCovAlign.getMinorAlleleFrequency(site);
 //            double obsHetFreq= highCovAlign.getHeterozygousCount(site)/highCovAlign.getSiteCount();
 //            double expHetFreq= 2*p*q;
 ////            if (obsHetFreq>(expHetFreq-(q*hwWiggle))&&obsHetFreq<(expHetFreq+(q*hwWiggle))) HWSites[site]= true;
 //        }
         //system.out to debug
         System.out.println("Parameters:\nHigh Coverage Cutoff: "+cov+"\nOutbred Cutoff:"+het+"\nInbred Cutoff: .006"
                     +"\nSample Intensity:"+sampleIntensity+"\n\nNumber of siteTaxa considered:\n"+"\nknownHets: "+
                     getTrue(knownHetMask)+" (highHet/highHomo: "+
                     getTrue(knownHetMask, highCovHets)+"/"+
                     getTrue(knownHetMask, highCovInbreds)+")"+"\n"+
                     "calledHomoMaj"+getTrue(calledHomoMajMask)+" (highHet/highHomo: "+
                     getTrue(calledHomoMajMask, highCovHets)+"/"+
                     getTrue(calledHomoMajMask, highCovInbreds)+")"+"\n"+
                     "calledHomoMin"+getTrue(calledHomoMinMask)+" (highHet/highHomo: "+
                     getTrue(calledHomoMinMask, highCovHets)+"/"+
                     getTrue(calledHomoMinMask, highCovInbreds)+")"+"\n"+
                     "calledMissing"+getTrue(calledMissingMask)+"\n\n"+
                     "Number of sequences in each group:\nTotalNumSequences: "+known.getSequenceCount()+"\nhighCovTaxa"+getTrue(highCovTaxa)+"\n"+
                     "highCovInbred"+getTrue(highCovInbreds)+"\n"+
                     "highCovHets"+getTrue(highCovHets)+"\n"+
                     "inbred"+getTrue(inbred)+"\n"+
                     "outbred"+getTrue(outbred)+"\n");
     }
     public static void QuickAccuracy(Alignment imputed, int taxon, int site) {
         if (knownBase!=diploidN&&impBase!=diploidN) {
             perSiteTaxon[knownIndex][imputed.getSequenceCount()]+=1.0;
             perSiteTaxon[knownSiteCount][taxon]+=1.0;
             if (AlignmentUtils.isEqual(knownBase, impBase)==false) {
 //                System.out.println(NucleotideAlignmentConstants.getNucleotideIUPAC(knownBase)+"/"+NucleotideAlignmentConstants.getNucleotideIUPAC(impBase)+"site: "+knownIndex+"/"+site+"taxon: "+taxon);
                 if (knownHet==true||imputed.isHeterozygous(taxon, site)==true) {
                     if (impArray[0]==knownArray[0]||impArray[1]==knownArray[0]||impArray[0]==knownArray[1]||
                         impArray[1]==knownArray[1]) perSiteTaxon[knownIndex][taxon]+=0;
                 }
                 else perSiteTaxon[knownIndex][taxon]+=1.0;
             }
                     
         }
     }
     
     public static void CalculateAccuracy(Alignment imputed, int taxon, int site, double[]type, double[]typeSites) {
         //calculates accuracy for all
                 typeSites[0]++;
                 if (calledHomoMajMask[taxon][site]==true) {
                     typeSites[1]++;
                     typeSites[2]++;
                     if (impBase==diploidN) type[5]++;
                     else if (knownBase==impBase) {
                         type[0]++;
                     }
                     else if (imputed.isHeterozygous(taxon, site)==true) {
                         if (impArray[0]==knownMaj||impArray[1]==knownMaj) type[2]++; 
                         else {
                             type[4]++;
                             type[14]++;
                         }
                     }
                     else {
                         type[3]++;
                         type[14]++;
                     }
                 }
                 else if (calledHomoMinMask[taxon][site]==true) {
                     typeSites[1]++;
                     typeSites[3]++;
                     if (impBase==diploidN) type[5]++;
                     else if (knownBase==impBase) type[1]++;
                     else if (imputed.isHeterozygous(taxon, site)==true) {
                         if (impArray[0]==knownMin||impArray[1]==knownMin) type[2]++;
                         else {
                             type[4]++;
                             type[14]++;
                         }
                     }
                     else {
                         type[3]++;
                         type[14]++;
                     }
                     
                 }
                 else if (knownHetMask[taxon][site]==true) {
                     typeSites[4]++;
                     if (impBase==diploidN) type[10]++;
                     else if (impBase==knownBase) type[6]++;
                     else if (imputed.isHeterozygous(taxon, site)==true) {
                         type[9]++;
                         type[14]++;
                     }
                     else if (impArray[0]==knownMaj||impArray[1]==knownMin||impArray[1]==knownMaj||impArray[0]==knownMin) {
                         type[7]++;
                         type[14]+= .5;
                     }
                     else {
                         type[8]++;
                         type[14]++;
                     }
                 }
                 else if (calledMissingMask[taxon][site]==true) {
                     typeSites[5]++;
                     if (impBase==diploidN) type[13]++;
                     else if (imputed.isHeterozygous(taxon, site)) type[12]++;
                     else type[11]++;
                 }
     }
         
     public static void RunTest(Alignment known, Alignment imputed, Alignment unimputed, boolean knownTest, int sampleIntensity, double cov, double het, double hwWiggle, String outFileName) {
         TasselPrefs.putAlignmentRetainRareAlleles(false);
         //for each double array, index 0:homoMajCorrect, 1:homoMinCorrect, 2:homoHetOneCorrect, 3:homoIncorrectHomo, 4:homoIncorrectHet
         //5:homoMissing, 6:hetCorrect, 7:hetOneCorrect, 8:hetIncorrectHomo, 9:hetIncorrectHet, 10:hetMissing, 11:missingImputedHomo, 
         //12:missingImputedHet, 13:missingMissing, 14:OverallError
         double[] all= new double[15];
         double[] allSites= new double[6]; //0 all, 1 homo, 2 homoMaj, 3 homoMin, 4 het, 5 missing
         double[] allInbred= new double[15];
         double[] allInbredSites= new double[6];
         double[] allOutbred= new double[15];
         double[] allOutbredSites= new double[6];
         double[] highCovInbred= new double[15];
         double[] highCovInbredSites= new double[6];
         double[] highCovOutbred= new double[15];
         double[] highCovOutbredSites= new double[6];
         knownSiteCount= known.getSiteCount();
         
         if (knownTest==true) {
             System.out.println("knownSitesMasked: "+known.getSiteCount()+"\nimputedTaxa: "+imputed.getSequenceCount());
             makeMasks55k(known, unimputed, cov, het);
             int[] knownPos= known.getPhysicalPositions();
             perSiteTaxon= new double[known.getSiteCount()+1][imputed.getSequenceCount()+1];
             for (int site = 0; site < imputed.getSiteCount(); site++) {
                 knownIndex= Arrays.binarySearch(knownPos, imputed.getPositionInLocus(site));
                 if (knownIndex<0) continue;
 //                System.out.println(site+"/"+knownIndex);//debug
                 int matchSite= known.getSiteOfPhysicalPosition(unimputed.getPositionInLocus(site), null);
                 knownMaj= known.getMajorAllele(matchSite);
                 knownMin= known.getMinorAllele(matchSite);
                 for (int taxon = 0; taxon < unimputed.getSequenceCount(); taxon++) {
                     impArray= imputed.getBaseArray(taxon, site);
                     impBase= imputed.getBase(taxon, site);
                     knownBase= known.getBase(matchTaxon[taxon], matchSite);
                     knownArray= known.getBaseArray(matchTaxon[taxon], matchSite);
                     knownHet= known.isHeterozygous(matchTaxon[taxon], matchSite);
                     QuickAccuracy(imputed, taxon, site);
                     CalculateAccuracy(imputed,taxon,site,all,allSites);//calculates accuracy for all
                     if (inbred[taxon]==true) {//calculates accuracy for only those coded as inbred (not 1-het)
                         CalculateAccuracy(imputed,taxon,site,allInbred,allInbredSites);
                         if (highCovTaxa[taxon]==true) {//accuracy for inbred sites with high coverage
                             CalculateAccuracy(imputed,taxon,site,highCovInbred,highCovInbredSites);
                         }                    
                     }
                     if (outbred[taxon]==true) {//calculates accuracy for landraces only (heterozygosity above specified cutoff)
                         CalculateAccuracy(imputed,taxon,site,allOutbred,allOutbredSites);
                         if (highCovTaxa[taxon]==true) {//accuracy for landraces with high coverage
                             CalculateAccuracy(imputed,taxon,site,highCovOutbred,highCovOutbredSites);
                         }
                     }
                 }
             }
         }
         else {
             makeMasks(known, sampleIntensity, cov, het);
             
             perSiteTaxon= new double[known.getSiteCount()+1][imputed.getSequenceCount()+1];
             for (int taxon= 0;taxon<known.getSequenceCount();taxon++) {            
                 for (int site= taxon;site<known.getSiteCount();site+= sampleIntensity) {
                     knownIndex= site;
                     knownBase= known.getBase(taxon, site);
                     impBase= imputed.getBase(taxon, site);
                     knownMaj= known.getMajorAllele(site);
                     knownMin= known.getMinorAllele(site);
                     impArray= imputed.getBaseArray(taxon, site);
                     QuickAccuracy(imputed,taxon, site);
                     CalculateAccuracy(imputed,taxon,site,all,allSites);//calculates accuracy for all
                     if (inbred[taxon]==true) {//calculates accuracy for only those coded as inbred (not 1-het)
                         CalculateAccuracy(imputed,taxon,site,allInbred,allInbredSites);
                         if (highCovTaxa[taxon]==true) {//accuracy for inbred sites with high coverage
                             CalculateAccuracy(imputed,taxon,site,highCovInbred,highCovInbredSites);
                         }                    
                     }
                     if (outbred[taxon]==true) {//calculates accuracy for landraces only (heterozygosity above specified cutoff)
                         CalculateAccuracy(imputed,taxon,site,allOutbred,allOutbredSites);
                         if (highCovTaxa[taxon]==true) {//accuracy for landraces with high coverage
                             CalculateAccuracy(imputed,taxon,site,highCovOutbred,highCovOutbredSites);
                         }
                     }                
                 }
             }
         }
         try{
             DecimalFormat df= new DecimalFormat("0.####");
             String outputFileName= dir+outFileName;
             DataOutputStream outStream= new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFileName)));
             
             outStream.writeBytes("\t\t\t\t\t\t\t\t\t\tcalledHomozygote\t\t\t\tcalledHeterozygote\t\t\t\t\tcalledMissing\n\tSitesConsidered\tSitesCalledHomo\tSiteCalledHomoMaj\tSitesCalledHomoMin\tSitesCalledHet\tSitesCalledMissing"
                     + "\thomoMajCorrect\thomoMinCorrect\thomoHetOneCorrect\thomoIncorrectHomo\thomoIncorrectHet"
                     + "\thomoMissing\thetCorrect\thetOneCorrect\thetIncorrectHomo\thetIncorrectHet\thetMissing\tmissingImputedHomo"
                     + "\tmissingImputedHet\tmissingMissing\tOverallError(halfHetsHalfCorrect)");
             outStream.writeBytes("\nallSites\t"+allSites[0]+"\t"+allSites[1]+"\t"+allSites[2]+"\t"+allSites[3]+"\t"+allSites[4]+"\t"+allSites[5]);
             outStream.writeBytes("\t"+all[0]/allSites[2]+"\t"+all[1]/allSites[3]);
             for (int i= 2;i<6;i++){outStream.writeBytes("\t"+all[i]/allSites[1]);}
             for (int i= 6;i<11;i++){outStream.writeBytes("\t"+all[i]/allSites[4]);}
             for (int i= 11;i<14;i++){outStream.writeBytes("\t"+all[i]/allSites[5]);}
             outStream.writeBytes("\t"+all[14]/(allSites[1]+allSites[4]));
             outStream.writeBytes("\nallInbred\t"+allInbredSites[0]+"\t"+allInbredSites[1]+"\t"+allInbredSites[2]+"\t"+allInbredSites[3]+"\t"+allInbredSites[4]+"\t"+allInbredSites[5]);
             outStream.writeBytes("\t"+allInbred[0]/allInbredSites[2]+"\t"+allInbred[1]/allInbredSites[3]);
             for (int i= 2;i<6;i++){outStream.writeBytes("\t"+allInbred[i]/allInbredSites[1]);}
             for (int i= 6;i<11;i++){outStream.writeBytes("\t"+allInbred[i]/allInbredSites[4]);}
             for (int i= 11;i<14;i++){outStream.writeBytes("\t"+allInbred[i]/allInbredSites[5]);}
             outStream.writeBytes("\t"+allInbred[14]/(allInbredSites[1]+allInbredSites[4]));
             outStream.writeBytes("\nhighCovInbred\t"+highCovInbredSites[0]+"\t"+highCovInbredSites[1]+"\t"+highCovInbredSites[2]+"\t"+highCovInbredSites[3]+"\t"+highCovInbredSites[4]+"\t"+highCovInbredSites[5]);
             outStream.writeBytes("\t"+highCovInbred[0]/highCovInbredSites[2]+"\t"+highCovInbred[1]/highCovInbredSites[3]);
             for (int i= 2;i<6;i++){outStream.writeBytes("\t"+highCovInbred[i]/highCovInbredSites[1]);}
             for (int i= 6;i<11;i++){outStream.writeBytes("\t"+highCovInbred[i]/highCovInbredSites[4]);}
             for (int i= 11;i<14;i++){outStream.writeBytes("\t"+highCovInbred[i]/highCovInbredSites[5]);}
             outStream.writeBytes("\t"+highCovInbred[14]/(highCovInbredSites[1]+highCovInbredSites[4]));
             outStream.writeBytes("\nallOutbred\t"+allOutbredSites[0]+"\t"+allOutbredSites[1]+"\t"+allOutbredSites[2]+"\t"+allOutbredSites[3]+"\t"+allOutbredSites[4]+"\t"+allOutbredSites[5]);
             outStream.writeBytes("\t"+allOutbred[0]/allOutbredSites[2]+"\t"+allOutbred[1]/allOutbredSites[3]);
             for (int i= 2;i<6;i++){outStream.writeBytes("\t"+allOutbred[i]/allOutbredSites[1]);}
             for (int i= 6;i<11;i++){outStream.writeBytes("\t"+allOutbred[i]/allOutbredSites[4]);}
             for (int i= 11;i<14;i++){outStream.writeBytes("\t"+allOutbred[i]/allOutbredSites[5]);}
             outStream.writeBytes("\t"+allOutbred[14]/(allOutbredSites[1]+allOutbredSites[4]));
             outStream.writeBytes("\nhighCovOutbred\t"+highCovOutbredSites[0]+"\t"+highCovOutbredSites[1]+"\t"+highCovOutbredSites[2]+"\t"+highCovOutbredSites[3]+"\t"+highCovOutbredSites[4]+"\t"+highCovOutbredSites[5]);
             outStream.writeBytes("\t"+highCovOutbred[0]/highCovOutbredSites[2]+"\t"+highCovOutbred[1]/highCovOutbredSites[3]);
             for (int i= 2;i<6;i++){outStream.writeBytes("\t"+highCovOutbred[i]/highCovOutbredSites[1]);}
             for (int i= 6;i<11;i++){outStream.writeBytes("\t"+highCovOutbred[i]/highCovOutbredSites[4]);}
             for (int i= 11;i<14;i++){outStream.writeBytes("\t"+highCovOutbred[i]/highCovOutbredSites[5]);}
             outStream.writeBytes("\t"+highCovOutbred[14]/(highCovOutbredSites[1]+highCovOutbredSites[4]));
             outStream.writeBytes("\ntaxonName:");
             for (int i= 0;i<imputed.getSequenceCount();i++) {outStream.writeBytes("\t"+imputed.getTaxaName(i));}
             outStream.writeBytes("\ntaxonSitesCompared:");
             for (int i= 0;i<perSiteTaxon[0].length;i++) {outStream.writeBytes("\t"+perSiteTaxon[known.getSiteCount()][i]);}
             outStream.writeBytes("\ntaxonError:");
             for (int taxon= 0;taxon<imputed.getSequenceCount();taxon++) {
                 double bad= 0;
                 for (int site= 0;site<known.getSiteCount();site++) {
                     bad+=perSiteTaxon[site][taxon];
                 }
                 double err= perSiteTaxon[known.getSiteCount()][taxon]!=0?((bad)/perSiteTaxon[known.getSiteCount()][taxon]):-1;
                 outStream.writeBytes("\t"+df.format(err));
             }
             outStream.writeBytes("\nsiteName:");
             for (int i= 0;i<known.getSiteCount();i++) {outStream.writeBytes("\t"+known.getSNPID(i));}
             outStream.writeBytes("\nsiteSitesCompared:");
             for (int i= 0;i<perSiteTaxon.length;i++) {outStream.writeBytes("\t"+perSiteTaxon[i][imputed.getSequenceCount()]);}
             outStream.writeBytes("\nsiteError:");
             for (int site= 0;site<known.getSiteCount();site++) {
                 double bad= 0;
                 for (int taxon= 0;taxon<imputed.getSequenceCount();taxon++) {
                     bad+=perSiteTaxon[site][taxon];
                 }
                 double err= perSiteTaxon[site][imputed.getSequenceCount()]!=0?((bad)/perSiteTaxon[site][imputed.getSequenceCount()]):-1;
                 outStream.writeBytes("\t"+df.format(err));
             }
             //to output the results matrix
 //            for (int site = 0; site < perSiteTaxon.length; site++) {
 //                for (int taxon = 0; taxon < perSiteTaxon[site].length; taxon++) {
 //                    outStream.writeBytes(perSiteTaxon[site][taxon]+"\t");
 //                }
 //                outStream.writeBytes("\n");
 //            }
             outStream.close();
        }
         
       catch(IOException e) {
            System.out.println(e);
        }
     }
     
     public static void main(String[] args) {
         TasselPrefs.putAlignmentRetainRareAlleles(false);
         
         //make a mask
 //        dir= "/home/local/MAIZE/kls283/GBS/Imputation/";
 //        String inFile= "SEED_12S_GBS_v2.6_MERGEDUPSNPS_20130513_chr10subset__minCov0.1RndSample1000";
 //        MaskFileSample(inFile,true,300);
         
 //        dir= "/Users/kelly/Documents/GBS/Imputation/SmallFiles/";
 //        MaskFile55k("RIMMA_282_v2.6_MERGEDUPSNPS_20130513_chr10subset__minCov0.2", true,
 //                "RIMMA_282_SNP55K_AGPv2_20100513__S45391.chr10_matchTo_RIMMA_282_v2.6_MERGEDUPSNPS_20130513_chr10subset__minCov0.1", true);
         
         //run accuracy
 //        dir= "/Users/kelly/Documents/GBS/Imputation/";
 ////        dir= "/home/local/MAIZE/kls283/GBS/Imputation/";
 //        String knownFileName= "SEED_12S_GBS_v2.6_MERGEDUPSNPS_20130513_chr10subset__minCov0.1";
 //        String imputedFileName= "SEED_12S_GBS_v2.6_MERGEDUPSNPS_20130513_chr10subset__minCov0.1_masked_defaultDonor";
 ////        ImputationAccuracy.makeMasks(ImportUtils.readFromHapmap(dir+knownFileName+".hmp.txt", true, null), 300, .6, .02, .2);
 //        ImputationAccuracy.RunTest(ImportUtils.readFromHapmap(dir+knownFileName+".hmp.txt.gz", null), 
 //                ImportUtils.readFromHapmap(dir+imputedFileName+".hmp.txt.gz", null), null, false, 300,.6,.01,.2,imputedFileName+"Accuracy.6.txt");
         
         //run accuracy for 55k
 //        dir= "/Users/kelly/Documents/GBS/Imputation/SmallFiles/";
 //        ImputationAccuracy.RunTest(ImportUtils.readFromHapmap(dir+"RIMMA_282_SNP55K_AGPv2_20100513__S45391.chr10_matchTo_RIMMA_282_v2.6_MERGEDUPSNPS_20130513_chr10subset__minCov0.1.hmp.txt.gz",null),
 //                    ImportUtils.readFromHapmap(dir+"RIMMA_282_v2.6_MERGEDUPSNPS_20130513_chr10subset__minCov0.2_masked55k_HomoSegBlock200HetWithExtras8k.minMtCnt15.mxInbErr.01.mxHybErr.003.c10.hmp.txt",null),
 //                    ImportUtils.readFromHapmap(dir+"RIMMA_282_v2.6_MERGEDUPSNPS_20130513_chr10subset__minCov0.2.hmp.txt.gz",null),true, -1, .6, .005, .2, "RIMMA_282_v2.6_MERGEDUPSNPS_20130513_chr10subset__minCov0.2_masked55k_HomoSegBlock200HetWithExtras8k.minMtCnt15.mxInbErr.01.mxHybErr.003.c10.Accuracy55k.txt");
         
         //mask input depth file using depth
         dir= "/Users/kellyadm/Desktop/Imputation2.7/";//laptop
         dir= "/home/local/MAIZE/kls283/GBS/Imputation2.7/parts/";
         String h5Depth= dir+"AllZeaGBS_v2.7_SeqToGenos_part14.hmp.h5";
         int depth= 5;
         int maskDenom= 17;
         maskBaseFileByDepth(h5Depth, depth, maskDenom, true);
 //        
 ////         //mask using depth. requires an hdf5 file with depth
         dir= "/home/kls283/Documents/Imputation/";//cbsugbs
 //        String h5Depth= dir+"AllZeaGBS_v2.7wDepth.hmp.h5";
         dir= "/Users/kellyadm/Desktop/Imputation2.7/";//laptop
 //        String h5Depth= dir+"AllZeaGBS_v2.7_SeqToGenos_part14.hmp.h5";
 //        String fileToMask= dir+"AllZeaGBS_v2.7_SeqToGenos_part14m.hmp.h5";
 ////        String fileToMask= dir+"AllZeaGBSv27StrictSubsetByAmes(no EP or GEM).hmp.h5";
 ////        String fileToMask= dir+"AllZeaGBSv27StrictSubsetBy12S_RIMMA_Span.hmp.h5";
 //        int depth= 5;
 //        int maskDenom= 3;
 ////        maskFileByDepth(h5Depth, fileToMask, depth, maskDenom,true, false);
 ////        fileToMask= dir+"AllZeaGBSv27StrictSubsetBy12S_RIMMA_Span.hmp.h5";
 ////        maskFileByDepth(h5Depth, fileToMask, depth, maskDenom,true, false);
 //        fileToMask= dir+"AllZeaGBS_v2.7wDepthm.hmp.h5";
 //        depth= 5;
 //        maskDenom= 11;
 //        maskFileByDepth(h5Depth, fileToMask, depth, maskDenom,true, true);
 //        
 //        fileToMask= dir+"AllZeaGBS_v2.7wDepthw.hmp.h5";
 //        depth= 5;
 //        maskDenom= 17;
 //        maskFileByDepth(h5Depth, fileToMask, depth, maskDenom,true, true);
         
 //        depth= 5;
 //        maskDenom= 17;
 //        fileToMask= dir+"AllZeaGBSv27StrictSubsetByAmes(no EP or GEM).hmp.h5";
 //        maskFileByDepth(h5Depth, fileToMask, depth, maskDenom,true, false);
 //        fileToMask= dir+"AllZeaGBSv27StrictSubsetBy12S_RIMMA_Span.hmp.h5";
 //        maskFileByDepth(h5Depth, fileToMask, depth, maskDenom,true, false);
 //        
 //        depth= 5;
 //        maskDenom= 11;
 //        fileToMask= dir+"AllZeaGBSv27StrictSubsetByAmes(no EP or GEM).hmp.h5";
 //        maskFileByDepth(h5Depth, fileToMask, depth, maskDenom,true, false);
 //        fileToMask= dir+"AllZeaGBSv27StrictSubsetBy12S_RIMMA_Span.hmp.h5";
 //        maskFileByDepth(h5Depth, fileToMask, depth, maskDenom,true, false);
 //        
 //        depth= 5;
 //        maskDenom= 11;
 //        fileToMask= dir+"AllZeaGBSv27StrictSubsetByAmes380.hmp.h5";
 //        maskFileByDepth(h5Depth, fileToMask, depth, maskDenom,true, false);
 
         
         //run accuracy on depth-masked imputed file. Only system out now.
 //        String dir= "//Users/kellyadm/Desktop/Imputation2.7/";
 ////        String dir= "/home/local/MAIZE/kls283/GBS/Imputation2.7/";
 ////        String key= dir+"AllZeaGBSv27StrictSubsetBy12S_RIMMA_Span._maskKey_Depth5_Denom17.hmp.h5";
 ////        String imputed=  dir+"/results0918/AllZeaGBSv27StrictSubsetBy12S_RIMMA_Span_LandraceDonor8k_depth5_denom17_imp.minCnt20.mxInbErr.01.mxHybErr.003.hmp.h5";
 ////        accuracyDepth(key, imputed);
 ////        imputed=  dir+"/results0918/AllZeaGBSv27StrictSubsetBy12S_RIMMA_Span_StdDonor8k_depth5_denom17_imp.minCnt20.mxInbErr.01.mxHybErr.003.hmp.h5";
 ////        accuracyDepth(key, imputed);
 ////        imputed=  dir+"/results0918/AllZeaGBSv27StrictSubsetBy12S_RIMMA_Span_LandraceDonor4k_depth5_denom17_imp.minCnt20.mxInbErr.01.mxHybErr.003.hmp.h5";
 ////        accuracyDepth(key, imputed);
 ////        String key=  dir+"AllZeaGBSv27StrictSubsetByAmes408._maskKey_Depth5_Denom17.hmp.h5";
 ////        String imputed=  dir+"resultsBaseImputation/AllZeaGBSv27StrictSubsetByAmes408_LandraceDonor8k_depth5_denom17_imp.minCnt20.mxInbErr.01.mxHybErr.003.hmp.h5";
 //        String imputed=  dir+"AllZeaGBSv27StrictSubsetBy12S_RIMMA_SpanDepth5Denom11chr1_8kStdDonorKellyBase3_imp.minCnt20.mxInbErr.01.mxHybErr.003.hmp.h5";
 //        String key=  dir+"AllZeaGBSv27StrictSubsetBy12S_RIMMA_Span._maskKey_Depth5_Denom11chr1.hmp.h5";
 //        String mafFile= dir+"donors/AllZeaGBSv27_HaplotypeMerge8kMAF.txt";
 //        double[] mafClass= new double[]{0,.05,.10,.20,1}; 
 ////        accuracyDepth(key, imputed);
 ////        imputed=  dir+"results0918/AllZeaGBSv27StrictSubsetByAmes408_StdDonor8k_depth5_denom17_imp.minCnt20.mxInbErr.01.mxHybErr.003.hmp.h5";
 ////        imputed= dir+"AllZeaGBSv27StrictSubsetByAmes408_LandraceDonor8kKellyFRFixed_depth5_denom17_imp.minCnt20.mxInbErr.01.mxHybErr.003.hmp.h5";
 //        accuracyDepth(key, imputed, mafFile, mafClass);
         
         }
     
 }
