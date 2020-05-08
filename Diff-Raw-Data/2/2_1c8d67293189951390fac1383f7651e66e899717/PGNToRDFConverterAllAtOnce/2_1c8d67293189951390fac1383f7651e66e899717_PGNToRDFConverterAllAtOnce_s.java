 /**
  * PGNToRDFConverterAllAtOnce.java
  */
 package de.uni_leipzig.informatik.swp13_sc.converter;
 
 
 /**
  * Simple PGN -> RDF conversion programme.
  *
  * @author Erik
  *
  */
 public class PGNToRDFConverterAllAtOnce
 {
 
     /**
      * main method
      * 
      * @param   args    String file parameter arguments
      */
     public static void main(String[] args)
     {
         if (args.length < 1)
         {
             System.out.println("Usage:");
            System.out.println("\tprogram <pgnfile>");
             return;
         }
         for (int i = 0; i < args.length; i ++)
         {
             long startTime = System.currentTimeMillis();
             System.out.println("Processing file " + (i+1) + "/" + args.length + ".");
             System.out.println("Converting PGN-File <" + args[i] + "> to RDF (Turtle) <" + args[i] + ".ttl>");
             PGNToChessDataModelConverter converter = new PGNToChessDataModelConverter(args[i], args[i] + ".ttl");
             System.out.print("Parsing data ...");
             if (! converter.parse())
             {
                 System.out.println(" aborting!");
                 return;
             }
             System.out.println(" finished.");
             System.out.print("Converting data ...");
             if (! converter.convert())
             {
                 System.out.println(" aborting!");
                 return;
             }
             System.out.println(" finished.");
             System.out.print("Writing data ...");
             if (! converter.write())
             {
                 System.out.println(" aborting!");
                 continue;
             }
             System.out.println(" finished.");
             System.out.println("Took " + ((System.currentTimeMillis() - startTime) / 1000.0) + " seconds.");
             System.out.println();
         }        
     }
 }
