 package org.eumetsat.dcpc.md.export;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import joptsimple.OptionException;
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 import joptsimple.OptionSpec;
 
 
 import static java.util.Arrays.asList;
 
 
 /**
  * CommandLineRunner for the MetadataExporter
  * @author guillaume.aubert@eumetsat.int
  *
  */
 public class CMDRunner
 {
     static final String VERSION ="v0.8";
     
     static boolean DEBUG_ON = false;
     
     static final OptionParser parser = new OptionParser();
     
     public final static Logger     logger               = LoggerFactory.getLogger(CMDRunner.class);
 
     
     static final String LINE_SEP = System.getProperty("line.separator");
     
     public static void version(OutputStream aOut)
     {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(aOut));
        
        try
        {
            
             writer.write("version: " + VERSION);
             writer.newLine();
             writer.flush();
         
         }
         catch (IOException ignored)
         {
             // eat it
         }
     }
     
     public static void usage(OutputStream aOut)
     {
         
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(aOut));
         
         // write usage
         try
         {
            writer.write("Usage: md-exporter --in <input-dir> --out <output-dir> ");
             writer.newLine();
             writer.write("                   [--xslt <xslt-file>] [--workdir <working-dir>]");
             writer.newLine();
             writer.write("                   [--version] [--help]");
             writer.newLine();
             writer.newLine();
             writer.flush();
         
             parser.printHelpOn(aOut);
             writer.newLine();
             writer.flush();
             
         }
         catch (IOException ignored)
         {
             // eat it
         }
     }
     
     /**
      * Parse the command line
      * @param args
      * @return
      */
     public static Map<String, Object> parseArguments(String args[])
     {
        HashMap<String, Object> arguments = new HashMap<String, Object>();   
        
        OptionSpec<Void> help       = parser.acceptsAll( asList( "h", "help"), "show usage description" );
        
        OptionSpec<Void> version    = parser.acceptsAll( asList( "v", "version"), "application version" );
        
        OptionSpec<Void> noxsltT    = parser.acceptsAll( asList( "n", "no-xslt-trans"), "do not perform the xslt transformation" );
        
        OptionSpec<Void> nocheck    = parser.acceptsAll( asList( "s", "no-check"), "do not check that files have been transformed" );
               
        OptionSpec<File> xslt       = parser.acceptsAll( asList( "x", "xslt"), "xslt file" )
                                           .withRequiredArg()
                                           .ofType( File.class )
                                           .describedAs("xslt file")
                                           .defaultsTo( new File("$MDEXPORTER_HOME/xslt/eum2iso_v4.1.xsl") );
        
        OptionSpec<File> outdir     = parser.acceptsAll( asList("o", "out"), "output directory")
                                           .withRequiredArg()
                                           .ofType( File.class ).describedAs("output-dir");
        
        OptionSpec<File> indir      = parser.acceptsAll( asList("i", "in"), "input dir with files to transform" )
                                          .withRequiredArg()
                                          .ofType( File.class ).describedAs("input-dir");
        
        OptionSpec<File> rdbdir     = parser.acceptsAll( asList("r", "rdb"), "Release Database Top Directory" )
                                          .withRequiredArg()
                                          .ofType( File.class ).describedAs("release-dir");
        
        OptionSpec<File> workingdir = parser.acceptsAll( asList("w", "workdir"), "Working Directory" )
                                            .withRequiredArg()
                                            .ofType( File.class ).describedAs("work-dir")
                                            .defaultsTo( new File("/tmp"));
        
        try 
        {
            OptionSet options = parser.parse(args);
            
            if (options.has(version))
            {
                version(System.out);
            }
            
            if (options.has(help))
            {
                usage(System.out);
                System.exit(0);
            }
            
            if (options.has(outdir))
            {
                arguments.put("out", options.valueOf(outdir));
            }
            else
            {
                throw new IllegalArgumentException("Error: Need more arguments. " + outdir + " option is missing." + CMDRunner.LINE_SEP);
            }
            
            
            if (options.has(indir))
            {
                arguments.put("in", options.valueOf(indir));
            }
            else
            {
                throw new IllegalArgumentException("Error: Need more arguments. " + indir + " option is missing." + CMDRunner.LINE_SEP);
            }
            
            if (options.has(rdbdir))
            {
                arguments.put("rdb", options.valueOf(rdbdir));
            }
            else
            {
               throw new IllegalArgumentException("Error: Need more arguments. " + indir + " option is missing." + CMDRunner.LINE_SEP);
            }
            
            // check if we need to do the sanity check
            if (options.has(nocheck))
            {
                arguments.put("nocheck", true);
            }
            else
            {
                arguments.put("nocheck", false);
            }
            
            // check if we need to do the xslt trans
            if (options.has(noxsltT))
            {
                arguments.put("noxslt", true);
            }
            else
            {
                arguments.put("noxslt", false);
                // optional arguments
                if (options.has(xslt))
                {
                    arguments.put("xslt", options.valueOf(xslt));
                }
                else
                {
                    //set it to the default value
                    // default val is $MDEXPORTER_HOME/xslt/eum2iso_v4.1.xsl
                    File xsltF = new File(getMDHome() + File.separatorChar + "xslt" + File.separatorChar + "eum2iso_v4.1.xsl");
                    arguments.put("xslt", xsltF );
                    logger.info("The default Xslt file will be used (" + xsltF.getAbsolutePath() + ")."  );
                    
                }
            }
            
            if (options.has(workingdir))
            {
                arguments.put("workingdir", options.valueOf(workingdir));
            }
            else
            {
                //set it to the default value
                arguments.put("workingdir", new File("/tmp"));
            }
            
        }
        catch ( OptionException expected ) 
        {
            // because you still must specify an argument if you give the option on the command line
            System.err.println(expected.getMessage());
            System.exit(1);
        }
        catch (IllegalArgumentException iaE)
        {
            System.err.println(iaE.getMessage());
            CMDRunner.usage(System.err);
            System.exit(1);
        }
        catch (Exception unknwown)
        {
           unknwown.printStackTrace();
           System.exit(1);
        }
        
        return arguments;
     }
     
     public static void printArguments(Map<String, Object> aArgs, OutputStream aOut)
     {
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(aOut));
         
         try
         {
             writer.write("Arguments :");
             writer.newLine();
             for (String  key : aArgs.keySet())
             {
                 writer.write(key + " :[" + aArgs.get(key) + "]");
                 writer.newLine();
                 writer.flush();
             }
             writer.write("-------------------------");
             writer.newLine();
             writer.flush();
          }
          catch (IOException ignored)
          {
              // eat it
          }
          
     }
     
     /**
      * Run the program
      * @param aArguments
      */
     public static void runWith(Map<String, Object> aArguments)
     {
         try
         {
             MetadataExporter md_Exporter = new MetadataExporter( 
                                                                ((File) aArguments.get("rdb")).getAbsolutePath()
                                                                , ((File) aArguments.get("workingdir")).getAbsolutePath());
             
             if ( ! ((Boolean) aArguments.get("noxslt")).booleanValue() )
                 md_Exporter.setXsltFile(((File) aArguments.get("xslt")).getAbsolutePath());
             
             md_Exporter.createExport( ((File) aArguments.get("in")).getAbsolutePath(), 
                                       ((File) aArguments.get("out")).getAbsolutePath(),
                                       ((Boolean) aArguments.get("nocheck")).booleanValue());
         }
         catch (Throwable e)
         {
             //System.out.println("Error: " + e.getMessage());
             //e.printStackTrace();
             CMDRunner.logger.error(e.getMessage()); 
             CMDRunner.logger.error("Set the log levels to DEBUG to have more info.");
             CMDRunner.logger.debug("Stack Trace of the error",e);
             System.exit(2);
             
         }
         
         
     }
     
     public static void setDebugInfo()
     {
         if (System.getProperty("md.debug", "no").equalsIgnoreCase("yes"))
         {
             System.out.println("Debug activated");
             DEBUG_ON = true;
         }
     }
     
     public static String getMDHome()
     {
         String home;
         if (! (home = System.getProperty("md.home", "")).equals("") )
         {
             return home;
         }
         
         return null;
     }
     
     
     public static void main(String[] args)
     {
         setDebugInfo();
         
         Map<String, Object> arguments = parseArguments(args);
         
         if (DEBUG_ON)
             printArguments(arguments, System.out);
         
         runWith(arguments);  
     }
 }
