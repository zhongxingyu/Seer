 package uk.ac.ebi.sampletab;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 public class STATConverter
 {
 
  /**
   * @param args
   */
  public static void main(String[] args)
  {
  File infile = new File("c:\\temp\\1000genomes.sampletab.txt");
  File outfile = new File("c:\\temp\\sample.age.txt");
  
   
   try
   {
    PrintStream out = new PrintStream(outfile);
 
    
    Submission sub = STParser3.readST(infile);
    
   
    OutputStream outs = new FileOutputStream(outfile);
    
   ATWriter.writeAgeTab(sub, outs );
 
   
    outs.close();
    
    System.out.println(sub.getAttachedObjects(STParser.TERMSOURCE).size());
    
    out.close();
   }
   catch(Exception e)
   {
    // TODO Auto-generated catch block
    e.printStackTrace();
   }
 
  }
 
 }
