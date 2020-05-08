 /**
  * User: gj
  * Date: 08/01/2011
  * Time: 20:10
  *
  *   Public Domain
  *
  */
 
 import java.io.*;
 
 
 public class wrzutaGet
 {
   public static void main(String[] args)
   {
 
     if (0 == args.length || 2 < args.length)
     {
       System.out.println("give us a file with wrzuta URLs and another name if you want it to be saved there!");
       System.out.println("otherwise output URLs will go to the stdout");
       return;
     }
 
     /*
        try to open the bloody file
      */
     FileInputStream infile;
     DataInputStream in;
     BufferedReader br;
     int count = 0;
 
     try
     {
       infile = new FileInputStream(args[0]);
       in = new DataInputStream(infile);
       br = new BufferedReader(new InputStreamReader(in));
 
     }
     catch (Exception e)
     {
       System.err.println("failed to open the source file \"" + args[0] + "\" bitch");
       System.err.print("Reason: " + e.toString());
       return;
     }
 
     /* trying to open the save file, if requested */
     FileWriter outfile = null;
     try
     {
       if (2 == args.length)
       { outfile = new FileWriter(args[1], false); }
     }
     catch (Exception e)
     {
       System.err.println("failed to open the destination file \"" + args[1] + "\" bitch");
       System.err.print("Reason: " + e.toString());
       return;
     }
 
     try
     {
       String strLine;
       goBitches bitches = new goBitches();
 
       while ((strLine = br.readLine()) != null)
       {
         strLine = strLine.trim();
         if (!"".equals(strLine))
         {
           String dl;
           dl = bitches.getTheBitch(strLine, "bitch");
           if (null != outfile)
           {
             outfile.write(dl);
             outfile.write(System.getProperty("line.separator"));
             count++;
           }
           else
           {
             System.err.println("Skipping: '" + strLine + "', wrong URL type");
           }
         }
       }
       in.close();
       if (null != outfile)
       {
         outfile.close();
       }
     }
     catch (Exception e)
     {
       System.err.println("the job has failed, bitch");
       System.err.print("Reason: " + e.toString());
       return;
     }
 
     System.err.println("done " + count + " , bitches!");
   }
 }
