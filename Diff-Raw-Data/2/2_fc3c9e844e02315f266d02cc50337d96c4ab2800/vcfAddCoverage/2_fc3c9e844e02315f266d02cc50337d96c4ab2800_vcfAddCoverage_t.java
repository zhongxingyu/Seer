 import java.util.List;
 import net.sf.samtools.*;
 import net.sf.samtools.SAMSequenceDictionary;
 import net.sf.samtools.SAMFileReader.ValidationStringency;
 import net.sf.picard.cmdline.*;
 import net.sf.picard.io.IoUtil;
 import java.io.*;
 import java.util.*;
 import java.util.regex.Pattern;
 
 /*
  * This code when feed with a merged vcf file, will add an INFO field (RDP) which
  * will contain the read coverage for all the samples.
  *
  * The tool expects to have an INFO field EFF (snpeffect). It uses it to
  * ignore SNPs with uninteresting functional consequences so it can compute the
  * results faster. If we did not enable this, we would have to perform
  * IO requests per each snp per each sample. In this way, we only perform IO requests
  * for functional interesting SNPs.
  *
  * Download http://cl.ly/060k2f44322x to see a vcf example.
  *
  * Usage:
  * gzip -cd merged.vcf.gz | java -Xmx4g vcfAddCoverage
  */
 public class vcfAddCoverage {
   private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
   private final List<String> bams = new ArrayList<String>();
  private final Pattern RE_IGNORE = Pattern.compile("=SYNONYMOUS_CODING|UPSTREAM|DOWNSTREAM|NONE|INTERGENIC|INTRON\\(");
   private final String NEW_INFO   = "##INFO=<ID=RDP,Number=.,Type=String,Description=\"Number of reads in site for sample\">";
 
   public static void main(String[] args){
     vcfAddCoverage c = new vcfAddCoverage();
     c.loadBams();
     c.addRDP();
   }
 
   private void bailOut(Exception e) {
     System.out.println(e.getMessage());
     e.printStackTrace();
     System.exit(1);
   }
 
   // Iterate over the VCF header and extract the paths to the bams so we can
   // use them to query per site coverage
   private void loadBams() {
     String line;
     int col_first_path = 9; // path to first merged bam
     int min_num_of_cols_if_merged_vcf = 11;
 
     try {
       while ((line = in.readLine()) != null && line.length() != 0) {
         char[] a_line = line.toCharArray();
         if (a_line[0] == '#' && a_line[1] != '#') { // line = header
           String[] s = line.split("\t");
           for (int i=col_first_path; i<s.length; i++)
             bams.add(s[i]);
           // Print our new INFO field and the actual header after that
           System.out.println(NEW_INFO);
           System.out.println(line);
           break;
         }
         System.out.println(line);
       }
     } catch(Exception e) {
       bailOut(e);
     }
   }
 
   // Find raw coverage for stite chrm, pos
   private String covAtSite(String chrm, String pos) {
     String cov = "";
     File inputFile;
     SAMFileReader reader = null;
     int iPos = Integer.parseInt(pos);
 
     for (int i=0; i<bams.size(); i++) {
       inputFile = new File(bams.get(i));
       reader = new SAMFileReader(inputFile);
       int count = 0;
       SAMRecordIterator iter = reader.query(chrm, iPos, iPos, false);
       while(iter.hasNext()) {
         count++;
         iter.next();
       }
       iter.close();
       reader.close();
 
       if (i == 0) {
         cov += Integer.toString(count);
       } else {
         cov += "," + Integer.toString(count);
       }
     }
     return cov;
   }
 
   // Iterate over the SNPs and add the new RDP INFO field
   private void addRDP() {
     int colChrm = 0; int colPos = 1; int colNumberInfo = 7;
     String line;
     try {
       while ((line = in.readLine()) != null && line.length() != 0) {
         String[] s = line.split("\t");
         for (int i=0; i<s.length; i++) {
           if (i == colNumberInfo) {
             if (RE_IGNORE.matcher(line).find()) {
               System.out.print(s[i] + "\t");
             } else {
               System.out.print(s[i] + ";RDP=" + covAtSite(s[colChrm], s[colPos]) + "\t");
             }
           } else {
             if (i == s.length - 1) {
               System.out.print(s[i] + "\n");
             } else {
               System.out.print(s[i] + "\t");
             }
           }
         }
       }
     } catch(Exception e) {
       bailOut(e);
     }
   }
 }
