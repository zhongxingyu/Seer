 package edu.cshl.schatz.jnomics.manager.client;
 
 import edu.cshl.schatz.jnomics.manager.client.ann.Flag;
 import edu.cshl.schatz.jnomics.manager.client.ann.FunctionDescription;
 import edu.cshl.schatz.jnomics.manager.client.ann.KbaseScript;
 import edu.cshl.schatz.jnomics.manager.client.fs.*;
 
 import java.util.List;
 import java.util.Properties;
 
 /**
  * User: james
  */
 
 
 @FunctionDescription(description = "Filesystem Functions\n"+
         "These functions provide a method for managing a user's\n"+
         "workspace on the Cluster.\n"
 )
@KbaseScript(prefix = "fs", exportFields = {"ls","shock_ls","mv","put","get","rm","rmr","put_pe","put_pe_i","put_se","mkdir","cat"})
 public class FS implements ClientFunctionHandler{
 
     @Flag(shortForm = "-ls", longForm = "--listfiles", description = "List files and directories on Cluster")
     public boolean ls;
     
     @Flag(shortForm = "-shock_ls", longForm = "--listShock", description = "List files and directories in Shock")
     public boolean shock_ls;
 
     @Flag(shortForm = "-mv", longForm = "--move", description = "Move files and directories on Cluster")
     public boolean mv;
 
     @Flag(shortForm = "-put", longForm = "--put", description = "Copy local files to Cluster")
     public boolean put;
     
     @Flag(shortForm = "-get", longForm = "--get", description = "Copy files on Cluster to local computer")
     public boolean get;
 
     @Flag(shortForm = "-rm", longForm = "--remove", description = "Remove files on Cluster")
     public boolean rm;
     
     @Flag(shortForm = "-rmr", longForm = "--remove_recursively", description = "Remove directories on Cluster")
     public boolean rmr;
 
     @Flag(shortForm = "-mkdir", longForm = "--make_directory", description = "Make a Directory on Cluster")
     public boolean mkdir;
 
     @Flag(shortForm = "-put_pe", longForm = "--put_paired_end", description ="Copy Paired end fastq files to Cluster")
     public boolean put_pe;
 
     @Flag(shortForm = "-put_pe_i", longForm = "--put_paired_end_interleaved", description = "Copy paired end interleaved fastq to Cluster")
     public boolean put_pe_i;
 
     @Flag(shortForm = "-put_se", longForm = "--put_single_end", description = "Copy Single end fastq to Cluster")
     public boolean put_se;
 
     @Flag(shortForm = "-cat", longForm = "--cat", description = "Cat a file")
     public boolean cat;
     
     
     @Override
     public void handle(List<String> remainingArgs, Properties properties) throws Exception {
         Class<? extends ClientFunctionHandler> handlerClass = null;
         if(ls){
             handlerClass = Ls.class;
         }else if(put){
             handlerClass = Put.class;
         }else if(get){
             handlerClass = Get.class;
         }else if(rm){
             handlerClass = Rm.class;
         }else if(rmr){
             handlerClass = Rmr.class;
         }else if(put_pe){
             handlerClass = PutPe.class;
         }else if(put_pe_i){
             handlerClass = PutPeI.class;
         }else if(put_se){
             handlerClass = PutSe.class;
         }else if(mkdir){
             handlerClass = Mkdir.class;
         }else if(mv){
             handlerClass = Mv.class;
         }else if(shock_ls){
         	handlerClass = ShockLs.class;
         }else if(cat){
 	    handlerClass = Cat.class;
 	}else{
             System.out.println(Utility.helpFromParameters(this.getClass()));
         }
 
         if(null != handlerClass){
             CreatedHandler createdHandler = Utility.handlerFromArgs(remainingArgs,handlerClass);
             createdHandler.getHandler().handle(createdHandler.getRemainingArgs(),properties);
         }
     }
 }
