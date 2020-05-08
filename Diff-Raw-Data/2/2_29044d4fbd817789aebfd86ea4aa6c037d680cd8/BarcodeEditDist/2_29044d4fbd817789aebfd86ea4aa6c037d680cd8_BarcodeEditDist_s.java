 import java.io.*;
 import java.util.ArrayList;
 
 /**
  * Class to check if a given set of barcodes has at least k-edit distance
  * between each pair.
  * @author Nirav Shah niravs@bcm.edu
  *
  */
 public class BarcodeEditDist
 {
   private ArrayList<String> barcodeNames = null;  // Names of barcodes
   private ArrayList<String> barcodeSeqs  = null;  // Sequences of barcodes
   private int minEditDist ;                       // Min edit distance expected
   
   /**
    * Class constructor - defaults to edit distance of 2
    * @param barcodeDefnFile
    * @throws Exception
    */
   public BarcodeEditDist(String barcodeDefnFile) throws Exception
   {
     constructorHelper(barcodeDefnFile, 2);
   }
   
   /**
    * Class constructor - with user specified edit distance
    * @param barcodeDefnFile
    * @param editDist
    * @throws Exception
    */
   public BarcodeEditDist(String barcodeDefnFile, int editDist) throws Exception
   {
     constructorHelper(barcodeDefnFile, editDist);
   }
   
   private void constructorHelper(String barcodeDefnFile, int minEditDist) throws Exception
   {
     this.minEditDist  = minEditDist;
     barcodeNames = new ArrayList<String>();
     barcodeSeqs  = new ArrayList<String>();
     String line;
     String delimiter = ",";
     boolean error = false;
     
     BufferedReader reader = new BufferedReader(new FileReader(new File(barcodeDefnFile)));
     
     while((line = reader.readLine()) != null)
     {
       String tokens[] = line.split(delimiter);
       
       if(tokens.length != 2)
       {
         String errMessage = "Invalid format of file : " + barcodeDefnFile +
                             "Expected : barcode_name,barcode_sequence";
         throw new Exception(errMessage);
       }
       barcodeNames.add(tokens[0].trim());
       barcodeSeqs.add(tokens[1].trim());
     }
     reader.close();
     
     for(int i = 0; i < barcodeSeqs.size(); i++)
     {
       for(int j = i + 1; j < barcodeSeqs.size(); j++)
       {
         if(false == distKApart(barcodeSeqs.get(i), barcodeSeqs.get(j)))
         {
           error = true;
           
           line = barcodeNames.get(i) + " ( Seq : " + barcodeSeqs.get(i) + " ) " +
                  " and " + barcodeNames.get(j) + " ( Seq : " +
                  barcodeSeqs.get(j) + " ) are less than " + minEditDist + 
                  "-edit distance apart";
           System.out.println(line);
         }
       }
     }
     
     if(!error)
     {
       line = "All specified barcodes are at least " + minEditDist + 
              "-edit distance apart";
       System.out.println(line);
     }
   }
   
   /**
    * Method to check if two strings are at least k-edit distance apart.
    * Return true if they are k-dist apart, false otherwise.
    */
   private boolean distKApart(String s1, String s2)
   {
     int diffLen = s1.length() - s2.length();
     
     if(diffLen < 0)
       diffLen *= -1;
     
     if(diffLen >= minEditDist)
       return true;
     
     int diffCount = diffLen;
     
     for(int i = 0; i < Math.min(s1.length(), s2.length()); i++)
     {
       if(s1.charAt(i) != s2.charAt(i))
       {
         diffCount++;
         
         if(diffCount >= minEditDist)
           return true;
       }
     }
     return false;
   }
   
   public static void main(String args[])
   {
     int editDist;
     BarcodeEditDist bcEditDist = null;
     
     try
     {
       if(args.length < 1 || args.length > 2)
       {
         printUsage();
         System.exit(-1);
       }
       if(args.length == 2)
       {
         editDist = Integer.parseInt(args[1]);
         bcEditDist = new BarcodeEditDist(args[0], editDist);
       }
       else
       {
         bcEditDist = new BarcodeEditDist(args[0]);
       }
     }
     catch(Exception e)
     {
       System.err.println(e.getMessage());
       e.printStackTrace();
       System.exit(-1);
     }
   }
   
   /**
    * Show usage information.
    */
   private static void printUsage()
   {
     System.err.println("Tool to check if a given set of barcodes has at least k-edit distance between each pair");
     System.err.println();
     System.err.println("Usage : ");
    System.err.println("java BarcodeEditDist InputFile EditDist");
     System.err.println("  InputFile - File containing barcode name and sequence separated by comma");
     System.err.println("  EditDist  - Min. required edit distance");
     System.err.println("              Optional, default = 2");
   }
 }
 
