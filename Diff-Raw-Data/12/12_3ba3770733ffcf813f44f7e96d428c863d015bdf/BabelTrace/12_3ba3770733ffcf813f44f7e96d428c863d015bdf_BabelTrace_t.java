 /******************************************************************************
   Execution of trace validation tools
   Copyright (C) 2012 Sylvain Halle
   
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  ******************************************************************************/
 package ca.uqac.info.trace.execution;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 import ca.uqac.info.ltl.*;
 import ca.uqac.info.ltl.Operator.ParseException;
 import ca.uqac.info.trace.*;
 import ca.uqac.info.trace.conversion.*;
 import ca.uqac.info.trace.execution.Execution.ReturnVerdict;
 import ca.uqac.info.util.FileReadWrite;
 
 import org.apache.commons.cli.*;
 
 public class BabelTrace
 {
   /**
    * Build string
    */
  public static final String BUILD_STRING = "20120110";
   
   /**
    * Return codes
    */
   public static final int ERR_OK = 0;
   public static final int ERR_CONVERSION = 1;
   public static final int ERR_PARSE = 2;
   public static final int ERR_IO = 3;
   public static final int ERR_ARGUMENTS = 4;
   public static final int ERR_NO_SUCH_TOOL = 5;
   public static final int ERR_TOOL_EXECUTION = 6;
   
   /**
    * Main program loop
    * @param args
    */
   public static void main(String[] args)
   {
     String trace_type = "", tool_name = "";
     String trace_in_filename = "", formula_in_filename = "";
     String out_formula = "", out_trace = "", out_signature = "", 
         output_dir = "";
     File formula_in, trace_in;
     trace_in = null;
     Operator op = null;
     EventTrace trace = null;
     boolean run_tool = false, show_command = false;
     
     // Parse command line arguments
     Options options = setupOptions();
     CommandLine c_line = setupCommandLine(args, options);
     assert c_line != null;
     if (c_line.hasOption("version"))
     {
       System.out.println("\nBabelTrace build " + BUILD_STRING);
       System.out.println("(C) 2012-2013 Sylvain Hallé et al., Université du Québec à Chicoutimi");
       System.out.println("This program comes with ABSOLUTELY NO WARRANTY.");
       System.out.println("This is free software, and you are welcome to redistribute it");
       System.out.println("under certain conditions. See the file COPYING for details.\n");
       System.exit(ERR_OK);
     }
     if (c_line.hasOption("h"))
     {
       showUsage(options);
       System.exit(ERR_OK);
     }
     if (c_line.hasOption("t"))
       trace_in_filename = c_line.getOptionValue("t");
     else
     {
       showUsage(options);
       System.exit(ERR_ARGUMENTS);      
     }
     if (c_line.hasOption("f"))
         formula_in_filename = c_line.getOptionValue("f");
     else
     {
       showUsage(options);
       System.exit(ERR_ARGUMENTS);      
     }
     if (c_line.hasOption("i"))
       trace_type = c_line.getOptionValue("i");
     else
     {
       showUsage(options);
       System.exit(ERR_ARGUMENTS);      
     }
     if (c_line.hasOption("c"))
       tool_name = c_line.getOptionValue("c");
     else
     {
       showUsage(options);
       System.exit(ERR_ARGUMENTS);      
     }
     if (c_line.hasOption("o"))
       output_dir = c_line.getOptionValue("o");
     else
     {
       showUsage(options);
       System.exit(ERR_ARGUMENTS);      
     }
     if (c_line.hasOption("b"))
     {
       run_tool = false;
       show_command = true;
     }
     if (c_line.hasOption("r"))
     {
       run_tool = true;
       show_command = false;
     }
     
     // Read input formula
     formula_in = new File(formula_in_filename);
     if (!formula_in.exists())
     {
       System.err.println("Error reading " + formula_in_filename);
       System.exit(ERR_IO);
     }
     try
     {
       out_formula = ca.uqac.info.util.FileReadWrite.readFile(formula_in_filename);  
     }
     catch (java.io.FileNotFoundException e)
     {
       System.err.println("File not found: " + formula_in_filename);
       System.exit(ERR_IO);
     }
     catch (java.io.IOException e)
     {
       System.err.println("IO Exception: " + formula_in_filename);
       e.printStackTrace();
       System.exit(ERR_IO);
     }
     
     // Get trace file
     trace_in = new File(trace_in_filename);
     
     // Get execution
     Execution ex = getExecution(tool_name);
     
     // Get filenames for each part
     String base_filename = FileReadWrite.baseName(trace_in) + "." + FileReadWrite.baseName(formula_in);
     String trace_filename = output_dir + "/" + base_filename + "." + ex.getTraceExtension();
     String formula_filename = output_dir + "/" + base_filename + "." + ex.getFormulaExtension();
     String signature_filename = output_dir + "/" + base_filename + "." + ex.getSignatureExtension();
     
     // Setup execution environment
     ex.setProperty(formula_filename);
     ex.setSignature(signature_filename);
     ex.setTrace(trace_filename);
     
     // Show command lines and leave
     if (show_command)
     {
       String[] cl = ex.getCommandLines();
       for (String c : cl)
       {
     	  System.out.println(c);
       }
       System.exit(ERR_OK);
     }
     
     // Initialize the translator
     Translator tt = getTraceTranslator(tool_name);
     if (tt == null)
     {
       System.err.println("Error: unrecognized conversion format \"" + tool_name + "\"");
       System.exit(ERR_NO_SUCH_TOOL);
     }
     tt.setTrace(trace);
     
     // Parse the trace
     TraceReader t_read = getTraceReader(trace_type);
     try
     {
       trace = t_read.parseEventTrace(new FileInputStream(trace_in));
     }
     catch (FileNotFoundException e)
     {
       e.printStackTrace();
       System.exit(ERR_IO);
     }
     
     // Get the formula as an operator
     try
     {
       op = Operator.parseFromString(out_formula);
     }
     catch (ParseException e)
     {
       System.err.println("Error parsing the input formula");
       System.exit(ERR_PARSE);
     }
     assert op != null;
     
     // Does the formula require flat messages and formulas?
     if (tt.requiresFlat())
     {
       // Convert the first-order formula to a propositional one
       FlatTranslator pt = new FlatTranslator();
       pt.setFormula(op);
       pt.setTrace(trace);
       // Flatten the trace
       String tr_trans = pt.translateTrace();
       TraceReader xtr = new XmlTraceReader();
       trace = xtr.parseEventTrace(new ByteArrayInputStream(tr_trans.getBytes()));
       // Flatten the formula
       String op_trans = pt.translateFormula();
       try
       {
         op = Operator.parseFromString(op_trans);
       }
       catch (ParseException e)
       {
         System.err.println("Error parsing the formula once translated to propositional");
         System.exit(ERR_PARSE);
       }
     }
     
     // Is the formula first-order while the tool requires propositional?
     if (tt.requiresPropositional() && FirstOrderDetector.isFirstOrder(op))
     {
       // Convert the first-order formula to a propositional one
       PropositionalTranslator pt = new PropositionalTranslator();
       pt.setFormula(op);
       pt.setTrace(trace);
       String op_trans = pt.translateFormula();
       try
       {
         op = Operator.parseFromString(op_trans);
       }
       catch (ParseException e)
       {
         System.err.println("Error parsing the formula once translated to propositional");
         System.exit(ERR_PARSE);
       }
       // We also convert equalities between constants produced by the translator
       // into Booleans
       ConstantConverter cc = new ConstantConverter();
       op.accept(cc);
       op = cc.getFormula();
       // And then propagate those constants
       UnitPropagator up = new UnitPropagator();
       op.accept(up);
       op = up.getFormula();
     }
     
     // Convert
     tt.setTrace(trace);
     tt.setFormula(op);
     tt.translateAll();
     out_trace = tt.getTraceFile();
     out_formula = tt.getFormulaFile();
     out_signature = tt.getSignatureFile();
         
     // Save conversions to files
     try
     {
       if (!out_trace.isEmpty())
       {
         ca.uqac.info.util.FileReadWrite.writeToFile(trace_filename, out_trace);
       }
       if (!out_formula.isEmpty())
       {
         ca.uqac.info.util.FileReadWrite.writeToFile(formula_filename, out_formula);
       }
       if (!out_signature.isEmpty())
       {
         ca.uqac.info.util.FileReadWrite.writeToFile(signature_filename, out_signature);
       }
     }
     catch (java.io.IOException e)
     {
       System.err.println("Error writing to file");
       System.err.println(e.getMessage());
       System.exit(ERR_IO);
     }
     
     // Now that all conversions have been made, run the tool
     if (run_tool)
     {
       ex.run();
       
       // Print results
       ReturnVerdict ex_retval = ex.getReturnVerdict();
       float ex_time = (float) ex.getTime() / (float) 1000000000; // We show seconds, not µs
       float ex_memory = (float) ex.getMemory() / (float) 1024; // We show megabytes, not bytes
       System.out.printf("%s,%.2f,%.2f\n", ex_retval, ex_time, ex_memory);
       
       // If an error occurred, dump it
       if (ex_retval == ReturnVerdict.ERROR)
       {
         System.err.println("Error while executing " + tool_name);
         System.err.println("Command line(s):");
         for (String cl : ex.getCommandLines())
         {
           System.err.println(cl);
         }
         System.err.println("Below is the string returned by the tool\n");
         System.err.println(ex.getErrorString());
         System.exit(ERR_TOOL_EXECUTION);
       }
     }
     
     // Quit
     System.exit(ERR_OK);
   }
   
   /**
    * Get the proper instance of {@link TraceReader}, based on the
    * declared input format. Currently the following formats are supported:
    * <ul>
    * <li>CSV</li>
    * <li>SQL</li>
    * <li>XML</li>
    * </ul>
    * @param type The type
    * @return The trace reader, null if format is unrecognized
    */
   private static TraceReader getTraceReader(String type)
   {
     TraceReader tr = null;
     if (type.compareToIgnoreCase("xml") == 0)
       tr = new XmlTraceReader();
     /*else if (type.compareToIgnoreCase("sql") == 0)
       tr = new SqlTraceReader();*/
     else if (type.compareToIgnoreCase("csv") == 0)
       tr = new CsvTraceReader();
     return tr;
   }
   
   /**
    * Gets the proper instance of the {@link TraceTranslator} class, based
    * on a string representing the target tool. Currently the following
    * tools are supported:
    * <ul>
    * <li>BeepBeep</li>
    * <li>Filter</li>
    * <li>Maude</li>
    * <li>Monpoly</li>
    * <li>MySQL</li>
    * <li>NuSMV</li>
    * <li>Saxon</li>
    * <li>Spin</li>
    * </ul> 
    * @param type The name of the target tool
    * @return The translator, null if tool is unrecognized
    */
   private static Translator getTraceTranslator(String type)
   {
     Translator tr = null;
     if (type.compareToIgnoreCase("maude") == 0)
       tr = new MaudeTranslator();
     else if (type.compareToIgnoreCase("beepbeep") == 0)
       tr = new BeepBeepTranslator();
     else if (type.compareToIgnoreCase("filter") == 0)
       tr = new FilterTranslator();
     else if (type.compareToIgnoreCase("mysql-opt") == 0)
       tr = new MySQLTranslatorOptimized();
     else if (type.compareToIgnoreCase("mysql") == 0)
       tr = new MySQLTranslator();
     else if (type.compareToIgnoreCase("mysql-innodb") == 0)
     {
         MySQLTranslator mtr = new MySQLTranslator();
         mtr.setEngine("InnoDB");
         tr = mtr;
     }
     else if (type.compareToIgnoreCase("mysql-memory") == 0)
     {
         MySQLTranslator mtr = new MySQLTranslator();
         mtr.setEngine("MEMORY");
         tr = mtr;
     }
     else if (type.compareToIgnoreCase("nusmv") == 0)
       tr = new SmvTranslator(); 
     else if (type.compareToIgnoreCase("saxon") == 0)
       tr = new SaxonTranslator();
     else if (type.compareToIgnoreCase("spin") == 0)
       tr = new PromelaTranslator(); 
     else if (type.compareToIgnoreCase("monpoly") == 0)
       tr = new MonpolyTranslator(); 
     return tr;
   }
   
   /**
    * Gets the proper instance of the {@link Execution} class, based
    * on a string representing the target tool. Currently the following
    * tools are supported:
    * <ul>
    * <li>BeepBeep</li>
    * <li>Filter</li>
    * <li>Maude</li>
    * <li>Monpoly</li>
    * <li>MySQL</li>
    * <li>NuSMV</li>
    * <li>Saxon</li>
    * <li>Spin</li>
    * </ul> 
    * @param type The name of the tool to execute
    * @return The Execution, null if tool is unrecognized
    */
   private static Execution getExecution(String type)
   {
     Execution tr = null;
     if (type.compareToIgnoreCase("maude") == 0)
       tr = new MaudeExecution();
     else if (type.compareToIgnoreCase("beepbeep") == 0)
       tr = new BeepBeepExecution();
     else if (type.compareToIgnoreCase("filter") == 0)
       tr = new FilterExecution();
     else if (type.compareToIgnoreCase("mysql") == 0)
       tr = new MySQLExecution();
     else if (type.compareToIgnoreCase("mysql-innodb") == 0)
         tr = new MySQLExecution();
     else if (type.compareToIgnoreCase("mysql-memory") == 0)
         tr = new MySQLExecution();
     else if (type.compareToIgnoreCase("mysql-opt") == 0)
       tr = new MySQLExecution();
     else if (type.compareToIgnoreCase("nusmv") == 0)
       tr = new NuSmvExecution(); 
     else if (type.compareToIgnoreCase("saxon") == 0)
       tr = new SaxonExecution();
     else if (type.compareToIgnoreCase("spin") == 0)
       tr = new SpinExecution(); 
     else if (type.compareToIgnoreCase("monpoly") == 0)
       tr = new MonpolyExecution(); 
     return tr;
   }
       
   /**
    * Sets up the command line parser
    * @param args The command line arguments passed to the class' {@link main}
    * method
    * @param options The command line options to be used by the parser
    * @return The object that parsed the command line parameters
    */
   private static CommandLine setupCommandLine(String[] args, Options options)
   {
     CommandLineParser parser = new PosixParser();
     CommandLine c_line = null;
     try
     {
       // parse the command line arguments
       c_line = parser.parse(options, args);
     }
     catch (org.apache.commons.cli.ParseException exp)
     {
       // oops, something went wrong
       System.err.println("ERROR: " + exp.getMessage() + "\n");
       //HelpFormatter hf = new HelpFormatter();
       //hf.printHelp(t_gen.getAppName() + " [options]", options);
       System.exit(ERR_ARGUMENTS);    
     }
     return c_line;
   }
   
   /**
    * Sets up the options for the command line parser
    * @return The options
    */
   @SuppressWarnings("static-access")
   private static Options setupOptions()
   {
     Options options = new Options();
     Option opt;
     opt = OptionBuilder
         .withLongOpt("run")
         .withDescription(
             "Run the selected tool after conversion (default: no)")
             .create("r");
     options.addOption(opt);
     opt = OptionBuilder
         .withLongOpt("help")
         .withDescription(
             "Display command line usage")
             .create("h");
     options.addOption(opt);
     opt = OptionBuilder
         .withLongOpt("batch")
         .withDescription(
             "Don't run the tool, but output the command line that would be used")
             .create("b");
     options.addOption(opt);
     opt = OptionBuilder
         .withLongOpt("trace")
         .withArgName("filename")
         .hasArg()
         .withDescription(
             "Read trace from filename")
             .create("t");
     options.addOption(opt);
     opt = OptionBuilder
         .withLongOpt("formula")
         .withArgName("filename")
         .hasArg()
         .withDescription(
             "Read formula from filename")
             .create("f");
     options.addOption(opt);
     opt = OptionBuilder
         .withLongOpt("convert")
         .withArgName("toolname")
         .hasArg()
         .withDescription(
             "Convert to format of toolname")
             .create("c");
     options.addOption(opt);
     opt = OptionBuilder
         .withLongOpt("version")
         .withDescription(
             "Show version of BabelTrace")
             .create();
     options.addOption(opt);
     opt = OptionBuilder
         .withLongOpt("input-type")
         .withArgName("type")
         .hasArg()
         .withDescription(
             "Trace input type")
             .create("i");
     options.addOption(opt);
     opt = OptionBuilder
         .withLongOpt("output-dir")
         .withArgName("dir")
         .hasArg()
         .withDescription(
             "Write output files to dir")
             .create("o");
     options.addOption(opt);
 
     return options;
   }
   
   /**
    * Show the benchmark's usage
    * @param options The options created for the command line parser
    */
   private static void showUsage(Options options)
   {
     HelpFormatter hf = new HelpFormatter();
     hf.printHelp("BabelTrace [options]", options);
   }
 }
