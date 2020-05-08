 package edu.cshl.schatz.jnomics.manager.client.compute;
 
 import edu.cshl.schatz.jnomics.manager.api.JnomicsThriftFileStatus;
 import edu.cshl.schatz.jnomics.manager.api.JnomicsThriftJobID;
 import edu.cshl.schatz.jnomics.manager.client.Utility;
 import edu.cshl.schatz.jnomics.manager.client.ann.Flag;
 import edu.cshl.schatz.jnomics.manager.client.ann.FunctionDescription;
 import edu.cshl.schatz.jnomics.manager.client.ann.Parameter;
 import edu.cshl.schatz.jnomics.manager.common.KBaseIDTranslator;
 import edu.cshl.schatz.jnomics.manager.client.fs.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * User: Sri
  */
 
 
 @FunctionDescription(description = "Cufflinks Transcript Assembler\n"+
         "Assembles transcripts for each sample.\n"+
         "Input and Output must reside on the Cluster's filesystem. \n"+
         "Optional additonal arguments may be supplied to \n"+
         "cufflinks. These options are passed as a string to cufflinks and should include hyphens(-)\n"+
         "if necessary.\n"
 )
 public class Cufflinks extends ComputeBase {
 
     @Flag(shortForm = "-h",longForm = "--help")
     public boolean help;
     
     @Parameter(shortForm = "-in", longForm = "--input", description = "input (bam file)")
     public String in;
     
     @Parameter(shortForm = "-out", longForm= "--output", description = "output (directory)")
     public String out;
     
     @Parameter(shortForm = "-ref_gtf", longForm= "--reference_gtf", description = "reference gtf(.gtf)")
     public String ref_gtf;
 
     @Parameter(shortForm = "-assembly_opts", longForm = "--assembly_options", description = "options to pass to Cufflinks (optional)")
     public String assembly_opts;
     
     @Parameter(shortForm = "-working_dir", longForm = "--working_dir", description = "workingdir (optional)")
     public String working_dir;
     
     @Override
     public void handle(List<String> remainingArgs,Properties properties) throws Exception {
 
         super.handle(remainingArgs,properties);
         if(help){
             System.out.println(Utility.helpFromParameters(this.getClass()));
             return;
         }else if(null == in){
             System.out.println("missing -in parameter");
         }else if(null == out){
             System.out.println("missing -out parameter");
         }else if(!fsclient.checkFileStatus(in, auth)){
         	System.out.println("ERROR : " + in + " file does'nt exist ");
         }else if(fsclient.checkFileStatus(out, auth)){
     		System.out.println("ERROR : Output directory already exists");
         }else{
//            String clean_org = KBaseIDTranslator.translate(organism);
 //            List<JnomicsThriftFileStatus> stats  = client.listStatus(organism, auth);
 //            StringBuilder sb = new StringBuilder();
 //            for(String opts : align_opts){
 //            	sb.append(" " + opts);
 //            }
 //            System.out.println("align_opts is " + assembly_opts + " in path is " + "outpath is " + out + "workingdir is " + working_dir);
             JnomicsThriftJobID jobID = client.callCufflinks(
                     in,
                     out,
                     Utility.nullToString(ref_gtf),
                     Utility.nullToString(assembly_opts),
                     Utility.nullToString(working_dir),
                     auth);
             System.out.println("Submitted Job: " + jobID.getJob_id());
             return;
         }
 
         System.out.println(Utility.helpFromParameters(this.getClass()));
     }
 }
