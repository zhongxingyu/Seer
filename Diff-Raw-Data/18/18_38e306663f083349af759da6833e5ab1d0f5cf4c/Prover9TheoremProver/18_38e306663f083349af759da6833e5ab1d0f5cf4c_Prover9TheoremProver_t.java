 package org.tptp.atp.prover9;
 
 import org.tptp.atp.*;
 
 import org.tptp.model.*;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * This is the Prover9 Implementation of the {@link TheoremProver} interface.
  * Prover 9 is available at http://www.cs.unm.edu/~mccune/mace4
  * 
  * @see TheoremProver
  */
 public class Prover9TheoremProver extends TheoremProver {
     protected String DEFAULT_BINARY = "prover9";
     protected String DEFAULT_MAX_SECONDS = "180";
     
     public String getName() {
         return "Prover9";
     }
     public String getVersion() {
         return "0.1";
     }
     public String getDescription() {
         return "The interface for using the Prover9 system in the ATPPortal. Prover9 is available at http://www.cs.unm.edu/~mccune/mace4/";
     }
     
     /**
      * Prover9 is a real prover! :)
      */
     public boolean isProver() {
         return true;
     }
     
     public Map<String,String> getOptionDescriptionMap() {
         Map<String,String> options = new HashMap<String,String>();
         options.put("path", "The path of the folder on the server where the prover9 executable is located. Do NOT point directly to the prover9.");
         return options;
     }
     
     public Map<String,String> getOptionalOptionDescriptionMap() {
         Map<String,String> options = new HashMap<String,String>();
         options.put("binary", "What is the name of the binary to execute? (Default: " + DEFAULT_BINARY + ")");
         options.put("max_seconds", "Maximum number of seconds for the prover to run (Default: " + DEFAULT_MAX_SECONDS + ").");
         return options;
     }
     
     /**
      * Executes the prover9 system to proof the goal formula.
      */
     public Result execute(Properties properties, String goal, Set<Formula> inputFormulas)
         throws AtpException
     {
         String input = buildInput(properties, goal, inputFormulas);
         try {
             return this.execute(properties, input);
         } catch (Exception exc) {
             return new Result(false, exc, this, input, "No output. See exception.");
         }
     }
     
     /**
      * Executes the prover9 system with the given file as the input. The output is fetched
      * and given to {@link Prover9OutputParser} for parsing.
      *
      * @see #parseOutput()
      * @see Prover9OutputParser
      */
     private Result execute(Properties properties, String input) throws IOException {
         File inputFile = writeTemporaryFile(input);
         
         // Some vars for storing the parsing results (used later)
         StringBuffer output = new StringBuffer();
         Set<Formula> used = new HashSet<Formula>();
         Set<Formula> proved = new HashSet<Formula>();
         Map<String,String> details = new HashMap<String,String>();
         Set<ProofStep> steps = new HashSet<ProofStep>();
         
         // Build the commandline call (no OS dependent stuff here!)
        StringBuilder call = new StringBuilder();
        String path = properties.getProperty("path", "");
        call.append(path);
        if ( !"".equals(path) && !path.endsWith(File.separator)) {
            call.append(File.separator);
         }
        call.append(properties.getProperty("binary", DEFAULT_BINARY));
        call.append(" -t ").append(properties.getProperty("max_seconds", DEFAULT_MAX_SECONDS));
        call.append(" -f ").append(inputFile.getCanonicalPath());
         
         // Start the process
        Process p = Runtime.getRuntime().exec(call.toString());
         try {
             // Start a separate thread for fetching the output
             Thread t = new OutputReaderThread(p.getInputStream(), output);
             t.start();
             int exitCode = p.waitFor(); // Block until the prover is ready
             
             // Call the output parser
             parseOutput(output, used, proved, steps, details);
             
             // Create the result object
             if ( exitCode == 0 ) {
                 return new Result(true, used, proved, steps, details, this, input, output.toString());    
             } else {
                 return new Result(false, new AtpException("Exitcode: " + exitCode), this, input, output.toString());
             }
             
         } catch (InterruptedException exc) {
             throw new IOException(exc);
         }
     }
     
     /**
      * Builds a text that can be used as the input for Prover 9.
      * See http://www.cs.unm.edu/~mccune/mace4/manual/2009-11A/input.html
      */
     private String buildInput(Properties properties, String goal, Set<Formula> inputFormulas) {
         /// Build the input file
         StringBuilder input = new StringBuilder();
         String NL = System.getProperty("line.separator", "\r\n");
         
         input.append("op(500, infix, \"+\").").append(NL);
         input.append("op(490, infix, \";\").").append(NL);
         input.append("op(480, postfix, \"*\").").append(NL);
         
         // Input Formulas
         input.append("formulas(assumptions).").append(NL).append(NL);
         for(Formula f : inputFormulas) {
             input.append("\t").append(f.getFormulaText()).append("\t\t#label(").append(f.getName()).append(").").append(NL);
         }
         input.append(NL).append("end_of_list.").append(NL).append(NL);
         
         // Goal Formula
         input.append("formulas(goals).").append(NL).append(NL).append("\t");
         input.append(goal).append(".").append(NL).append(NL);
         input.append("end_of_list.").append(NL);
         
         return input.toString();
     }
     
     protected File writeTemporaryFile(String content) throws IOException {
         // Write the input to a temporary file
         File temp = File.createTempFile("prover9_input_", ".tmp");
         temp.deleteOnExit();
         FileOutputStream fout = new FileOutputStream(temp);
         fout.write(content.getBytes("UTF-8"));
         fout.close();
         
         return temp;
     }
     
     /**
      * This helper class consumes the given {@link InputStream} and writes it to the
      * given StringBuffer. Nothing prover9 specific here.
      */
     protected class OutputReaderThread extends Thread {
         private InputStream in;
         private StringBuffer buffer;
         public OutputReaderThread(InputStream in, StringBuffer buffer) {
             this.in = in;
             this.buffer = buffer;
         }
         public void run() {
             try {
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                 String s;
                 while ( ( s = reader.readLine()) != null) {
                     buffer.append(s).append("\n");
                 }
             } catch (IOException exc) {
                 exc.printStackTrace();
             }
         }
     }
     
     /**
      * This method parses the prover9 output with the help of the {@link Prover9OutputParser}.
      * 
      * @see Prover9OutputParser
      */
     protected void parseOutput(StringBuffer output, Set<Formula> used, Set<Formula> proved, Set<ProofStep> steps, Map<String,String> details) {
         Prover9OutputParser parser = new Prover9OutputParser();
         parser.parse(output.toString(), used, proved, steps, details);
     }
 }
