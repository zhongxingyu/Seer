 package edu.cshl.schatz.jnomics.manager.server;
 
 import edu.cshl.schatz.jnomics.manager.api.*;
 import edu.cshl.schatz.jnomics.mapreduce.JnomicsJobBuilder;
 import edu.cshl.schatz.jnomics.tools.*;
 import edu.cshl.schatz.jnomics.util.TextUtil;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.mapred.JobID;
 import org.apache.hadoop.mapred.JobStatus;
 import org.apache.hadoop.mapred.RunningJob;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.apache.thrift.TException;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.security.PrivilegedExceptionAction;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.UUID;
 
 /**
   * User: james
  */
 public class JnomicsComputeHandler implements JnomicsCompute.Iface{
 
     private final org.slf4j.Logger logger = LoggerFactory.getLogger(JnomicsComputeHandler.class);
 
     private Properties properties;
 
     private static final int NUM_REDUCE_TASKS = 1024;
     
     private JnomicsServiceAuthentication authenticator;
     
     public JnomicsComputeHandler(Properties systemProperties){
         properties = systemProperties;
         authenticator = new JnomicsServiceAuthentication();
     }
 
     private Configuration getGenericConf(){
         Configuration conf = new Configuration();
         //if you don't give Path's it will not load the files
         conf.addResource(new Path(properties.getProperty("core-site-xml")));
         conf.addResource(new Path(properties.getProperty("mapred-site-xml")));
         conf.addResource(new Path(properties.getProperty("hdfs-site-xml")));
         conf.set("fs.default.name", properties.getProperty("hdfs-default-name"));
         conf.set("mapred.jar", properties.getProperty("jnomics-jar-path"));
         return conf;
     }
 
 
     @Override
     public JnomicsThriftJobID alignBowtie(String inPath, String organism, String outPath, String opts, Authentication auth)
             throws TException, JnomicsThriftException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         
         logger.info("Starting Bowtie2 process for user " + username);
         JnomicsJobBuilder builder = new JnomicsJobBuilder(getGenericConf(),Bowtie2Map.class);
         builder.setInputPath(inPath)
                 .setOutputPath(outPath)
                 .setParam("bowtie_binary","bowtie/bowtie2-align")    
                 .setParam("bowtie_index", "btarchive/"+organism+".fa")
                 .setParam("bowtie_opts",opts)
                 .setJobName(username+"-bowtie2-"+inPath)
                 .addArchive(properties.getProperty("hdfs-index-repo")+"/"+organism+"_bowtie.tar.gz#btarchive")
                 .addArchive(properties.getProperty("hdfs-index-repo") + "/bowtie.tar.gz#bowtie");
 
         Configuration conf = null;
         try{
             conf = builder.getJobConf();
         }catch(Exception e){
             throw new JnomicsThriftException(e.toString());
         }
         return launchJobAs(username,conf);
     }
 
     @Override
     public JnomicsThriftJobID alignBWA(String inPath, String organism, String outPath, 
                                        String alignOpts, String sampeOpts, Authentication auth)
             throws TException, JnomicsThriftException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
 
         logger.info("Starting Bwa process for user " + username);
         JnomicsJobBuilder builder = new JnomicsJobBuilder(getGenericConf(), BWAMap.class);
         builder.setInputPath(inPath)
                 .setOutputPath(outPath)
                 .setParam("bwa_binary","bwa/bwa")
                 .setParam("bwa_index", "bwaarchive/"+organism+".fa")
                 .setParam("bwa_align_opts",alignOpts)
                 .setParam("bwa_sampe_opts",sampeOpts)
                 .setJobName(username+"-bwa-"+inPath)
                 .addArchive(properties.getProperty("hdfs-index-repo")+"/"+organism+"_bwa.tar.gz#bwaarchive")
                 .addArchive(properties.getProperty("hdfs-index-repo")+"/bwa.tar.gz#bwa");
 
         Configuration conf = null;
         try{
             conf = builder.getJobConf();
         }catch(Exception e){
             throw new JnomicsThriftException(e.toString());
         }
         return launchJobAs(username,conf);
     }
 
     @Override
     public JnomicsThriftJobID snpSamtools(String inPath, String organism, String outPath, Authentication auth) throws TException, JnomicsThriftException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         logger.info("Running samtools pipeline for user: "+ username);
         
         JnomicsJobBuilder builder = new JnomicsJobBuilder(getGenericConf(), SamtoolsMap.class, SamtoolsReduce.class);
         builder.setInputPath(inPath)
                 .setOutputPath(outPath)
                 .addArchive(properties.getProperty("hdfs-index-repo")+"/"+organism+"_samtools.tar.gz#starchive")
                 .addArchive(properties.getProperty("hdfs-index-repo")+"/samtools.tar.gz#samtools")
                 .addArchive(properties.getProperty("hdfs-index-repo")+"/bcftools.tar.gz#bcftools")
                 .setParam("samtools_binary","samtools/samtools")
                 .setParam("bcftools_binary","bcftools/bcftools")
                 .setParam("reference_fa","starchive/"+organism+".fa")
                 .setParam("genome_binsize","1000000")
                 .setReduceTasks(NUM_REDUCE_TASKS)
                 .setJobName(username+"-snp-"+inPath);
 
         Configuration conf = null;
         try{
             conf = builder.getJobConf();
         }catch (Exception e){
             throw new JnomicsThriftException(e.toString());
         }
         return launchJobAs(username, conf);
     }
 
     @Override
     public JnomicsThriftJobStatus getJobStatus(final JnomicsThriftJobID jobID, final Authentication auth)
             throws TException, JnomicsThriftException {
         final String username = authenticator.authenticate(auth);
         if(null == username){
             throw new JnomicsThriftException("Permission Denied");
         }
         logger.info("Getting job status for user "+ username);
 
         return new JobClientRunner<JnomicsThriftJobStatus>(username,
                 new Configuration(),properties){
             @Override
             public JnomicsThriftJobStatus jobClientTask() throws Exception {
 
                 RunningJob job = getJobClient().getJob(JobID.forName(jobID.getJob_id()));
                 return new JnomicsThriftJobStatus(job.getID().toString(),
                         username,
                         null,
                         job.isComplete(),
                         job.getJobState(),
                         0,
                         null,
                         job.mapProgress(),
                         job.reduceProgress());
             }
         }.run();
     }
 
     @Override
     public List<JnomicsThriftJobStatus> getAllJobs(Authentication auth) throws JnomicsThriftException, TException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         logger.info("Getting all job status for user "+ username);
 
         JobStatus[] statuses = new JobClientRunner<JobStatus[]>(username,new Configuration(),properties){
             @Override
             public JobStatus[] jobClientTask() throws Exception {
                 logger.info("getting jobs");
                 return getJobClient().getAllJobs();
             }
         }.run();
         logger.info("got jobs");
         List<JnomicsThriftJobStatus> newStats = new ArrayList<JnomicsThriftJobStatus>();
         for(JobStatus stat: statuses){
             if(0 == username.compareTo(stat.getUsername()))
                 newStats.add(new JnomicsThriftJobStatus(stat.getJobID().toString(),
                         stat.getUsername(),
                         stat.getFailureInfo(),
                         stat.isJobComplete(),
                         stat.getRunState(),
                         stat.getStartTime(),
                         stat.getJobPriority().toString(),
                         stat.mapProgress(),
                         stat.reduceProgress()));
         }
         return newStats;
     }
 
 
     /**
      * Writes a manifest file in a directory called manifests in home directory
      *
      * @param filename filename to use as prefix for manifest file
      * @param data data to write to manifest file, each string in the array is aline
      * @param username to perform fs operations as
      * @return Path of the created manfiest file
      * @throws JnomicsThriftException
      */
     private Path writeManifest(String filename, String []data, String username) throws JnomicsThriftException{
 
         //write manifest file and run job
         FileSystem fs = null;
         try {
             fs = FileSystem.get(new URI(properties.getProperty("hdfs-default-name")),
                     new Configuration(),username);
             if(!fs.exists(new Path("manifests"))){
                 fs.mkdirs(new Path("manifests"));
             }
         }catch (Exception e) {
             try{
                 fs.close();
             }catch (Exception t){
                 throw new JnomicsThriftException(t.toString());
             }
             throw new JnomicsThriftException(e.toString());
         }
 
         Path manifest,f1;
         FSDataOutputStream outStream = null;
         try{
             f1 = new Path(filename);
             manifest = new Path("manifests/"+f1.getName()+"-"+UUID.randomUUID().toString()+".manifest");
             outStream = fs.create(manifest);
             for(String line: data){
                 outStream.write((line + "\n").getBytes());
             }
         }catch(Exception e){
             throw new JnomicsThriftException(e.toString());
         }finally{
             try {
                 outStream.close();
             } catch (IOException e) {
                 throw new JnomicsThriftException();
             }finally{
                 try{
                     fs.close();
                 }catch(Exception b){
                     throw new JnomicsThriftException(b.toString());
                 }
             }
         }
         return manifest;
     }
 
 
     @Override
     public JnomicsThriftJobID pairReads(String file1, String file2, String outFile, Authentication auth)
             throws JnomicsThriftException, TException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         logger.info("Pairing reads in hdfs for user "+ username);
 
 
         String data = TextUtil.join("\t",new String[]{file1,file2,outFile});
         Path manifest = writeManifest(new File(file1).getName(), new String[]{data}, username);
 
         Path manifestlog = new Path(manifest +".log");
         JnomicsJobBuilder builder = new JnomicsJobBuilder(getGenericConf(),PELoaderMap.class,PELoaderReduce.class);
         builder.setJobName(new File(file1).getName()+"-pe-conversion")
                 .setInputPath(manifest.toString())
                 .setOutputPath(manifestlog.toString())
                 .setParam("mapreduce.outputformat.class","org.apache.hadoop.mapreduce.lib.output.TextOutputFormat")
                 .setReduceTasks(1);
         Configuration conf;
         try{
             conf = builder.getJobConf();
         }catch(Exception e){
             throw new JnomicsThriftException(e.toString());
         }
         JnomicsThriftJobID id = launchJobAs(username,conf);
         logger.info("submitted job: " + conf.get("mapred.job.name") + " " + id);
         return new JnomicsThriftJobID(id);
     }
 
     @Override
     public JnomicsThriftJobID singleReads(String file, String outFile, Authentication auth)
             throws JnomicsThriftException, TException {
 
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         logger.info("converting single reads to sequence file for user "+ username);
 
 
         String fileBase = new File(file).getName();
 
         String data = TextUtil.join("\t",new String[]{file,outFile});
         Path manifest = writeManifest(fileBase,new String[]{data},username);
         
         Path manifestlog = new Path(manifest + ".log");
 
         JnomicsJobBuilder builder = new JnomicsJobBuilder(getGenericConf(),SELoaderMap.class,SELoaderReduce.class);
         builder.setJobName(fileBase+"-pe-conversion")
                 .setInputPath(manifest.toString())
                 .setOutputPath(manifestlog.toString())
                 .setReduceTasks(1);
 
         Configuration conf = null;
         try{
             conf = builder.getJobConf();
         }catch(Exception e){
             throw new JnomicsThriftException(e.toString());
         }
 
         JnomicsThriftJobID id = launchJobAs(username,conf);
         logger.info("submitted job: " + conf.get("mapred.job.name") + " " + id);
         return new JnomicsThriftJobID(id);        
         
     }
 
     public JnomicsThriftJobID launchJobAs(String username, final Configuration conf)
             throws JnomicsThriftException {
         RunningJob runningJob = new JobClientRunner<RunningJob>(username,conf,properties){
             @Override
             public RunningJob jobClientTask() throws Exception {
                 return getJobClient().submitJob(getJobConf());
             }
         }.run();
         String jobid = runningJob.getID().toString();
         logger.info("submitted job: " + conf.get("mapred.job.name") + " " + jobid);
         return new JnomicsThriftJobID(jobid);
     }
 
     @Override
     public boolean mergeVCF(String inDir, String inAlignments, String outVCF, Authentication auth)
             throws JnomicsThriftException, TException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
 
         final Configuration conf = getGenericConf();
         logger.info("Merging VCF: " + inDir + ":" + inAlignments + ":" + outVCF + " for user " + username);
 
         final Path in = new Path(inDir);
         final Path alignments = new Path(inAlignments);
         final Path out = new Path(outVCF);
         boolean status  = false;
         try {
             status = UserGroupInformation.createRemoteUser(username).doAs(new PrivilegedExceptionAction<Boolean>() {
                 @Override
                 public Boolean run() throws Exception {
                     VCFMerge.merge(in,alignments,out,conf);
                     return true;
                 }
             });
         }catch (Exception e) {
             logger.info("Failed to merge: " + e.toString());
             throw new JnomicsThriftException(e.toString());
         }
         return status;
     }
 
     @Override
     public boolean mergeCovariate(String inDir, String outCov, Authentication auth) throws JnomicsThriftException, TException {
         String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         logger.info("Merging covariates for user"+ username);
 
         final Configuration conf = getGenericConf();
         final Path in = new Path(inDir);
         final Path out = new Path(outCov);
 
         boolean status = false;
         try{
             status = UserGroupInformation.createRemoteUser(username).doAs(new PrivilegedExceptionAction<Boolean>() {
                 @Override
                 public Boolean run() throws Exception {
                     CovariateMerge.merge(in, out, conf);
                     return true;
                 }
             });
         }catch(Exception e){
             logger.info("Failed to merge:" + e.toString());
             throw new JnomicsThriftException(e.toString());
         }
         return status;
     }
 
     private JnomicsJobBuilder getGATKConfBuilder(String inPath, String outPath, String organism){
         return null;
         /**FIXME**/
         /*JnomicsJobBuilder builder = new JnomicsJobBuilder(getGenericConf(),SamtoolsMap.class);
         builder.setParam("samtools_binary","gatk/samtools")
                 .setParam("reference_fa","gatk/"+organism+".fa")
                 .setParam("gatk_jar", "gatk/GenomeAnalysisTK.jar")
                 .setParam("genome_binsize","1000000")
                 .setReduceTasks(NUM_REDUCE_TASKS)
                 .addArchive(properties.getProperty("hdfs-index-repo")+"/"+organism+"_gatk.tar.gz#gatk")
                 .setInputPath(inPath)
                 .setOutputPath(outPath);
                 return builder;*/
     }
     
     @Override
     public JnomicsThriftJobID gatkRealign(String inPath, String organism, String outPath, Authentication auth)
             throws JnomicsThriftException, TException {
         return null;
         /**FIXME**/
         /**String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         logger.info("gatk realign for user "+ username);
 
         JnomicsJobBuilder builder = getGATKConfBuilder(inPath, outPath, organism);
         builder.setReducerClass(GATKRealignReduce.class)
                 .setJobName(username+"-gatk-realign-"+inPath);
         Configuration conf = null;
         try{
             conf = builder.getJobConf();
         }catch(Exception e){
             throw new JnomicsThriftException(e.toString());
         }
         return launchJobAs(username, conf);*/
     }
 
     @Override
     public JnomicsThriftJobID gatkCallVariants(String inPath, String organism, String outPath, Authentication auth)
             throws JnomicsThriftException, TException {
        return null;
        /*String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         logger.info("gatkCallVariants for user "+ username);
 
         JnomicsJobBuilder builder = getGATKConfBuilder(inPath,outPath,organism);
         builder.setReducerClass(GATKCallVarReduce.class)
                 .setJobName(username+"-gatk-call-variants");
         Configuration conf = null;
         try{
             conf = builder.getJobConf();
         }catch(Exception e){
             throw new JnomicsThriftException(e.toString());
         }
        return launchJobAs(username, conf);*/
     }
 
     @Override
     public JnomicsThriftJobID gatkCountCovariates(String inPath, String organism, String vcfMask,
                                                   String outPath, Authentication auth)
             throws JnomicsThriftException, TException {
        return null;
        /*String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         logger.info("gatkCountCovariates for user "+ username);
 
         JnomicsJobBuilder builder = getGATKConfBuilder(inPath, outPath, organism);
         Path vcfMaskPath = new Path(vcfMask);
         builder.setReducerClass(GATKCountCovariatesReduce.class)
                 .setParam("mared.cache.files",vcfMaskPath.toString()+"#"+vcfMaskPath.getName())
                 .setParam("tmpfiles",vcfMaskPath.toString()+"#"+vcfMaskPath.getName())
                 .setParam("vcf_mask",vcfMaskPath.getName())
                 .setJobName(username+"-gatk-count-covariates");
         Configuration conf = null;
         try{
             conf = builder.getJobConf();
         }catch(Exception e){
             throw new JnomicsThriftException(e.toString());
         }
        return launchJobAs(username,conf);*/
     }
 
     @Override
     public JnomicsThriftJobID gatkRecalibrate(String inPath, String organism, String recalFile, String outPath, Authentication auth)
             throws JnomicsThriftException, TException {
        return null;
        /*String username;
         if(null == (username = authenticator.authenticate(auth))){
             throw new JnomicsThriftException("Permission Denied");
         }
         logger.info("gatkRecalibrate for user "+ username);
 
         JnomicsJobBuilder builder = getGATKConfBuilder(inPath, outPath, organism);
         Path recalFilePath = new Path(recalFile);
         builder.setReducerClass(GATKRecalibrateReduce.class)
                 .setParam("mapred.cache.files", recalFilePath.toString()+"#"+recalFilePath.getName())
                 .setParam("tmpfiles", recalFilePath.toString()+"#"+recalFilePath.getName())
                 .setParam("recal_file",recalFilePath.getName())
                 .setJobName(username+"-gatk-recalibrate");
         Configuration conf = null;
         try{
             conf = builder.getJobConf();
         }catch(Exception e){
             throw new JnomicsThriftException(e.toString());
         }
        return launchJobAs(username,conf);*/
     }
 
     @Override
     public JnomicsThriftJobID runSNPPipeline(final String inPath, final String organism, final String outPath,
                                              final Authentication auth)
             throws JnomicsThriftException, TException {
         final String username = authenticator.authenticate(auth);
         if( null == username ){
             throw new JnomicsThriftException("Permission Denied");
         }
         logger.info("Running snpPipeline for user "+ username);
         String initPath = inPath;
         boolean loadShock = false;
         if(inPath.startsWith("http")){
             String []shockPaths = inPath.split(",");
             String []manifestData = new String[shockPaths.length];
             int i=0;
             for(String p: shockPaths){
                 manifestData[i++] = p + "\t" + new Path(new Path(outPath,"http_load"),"d"+i).toString();
             }
             Path manifest = writeManifest("shockdata",manifestData,username);
             initPath = manifest.toString();
             loadShock = true;
         }
         
         logger.info("Starting new Thread");
         
         final String nxtPath = initPath;
         final boolean loadShockfinal = loadShock;
         
         new Thread(new Runnable(){
             @Override
             public void run() {
                 String alignIn;
                 if(loadShockfinal){
                     final JnomicsJobBuilder shockBuilder = new JnomicsJobBuilder(getGenericConf(),HttpLoaderMap.class,
                             HttpLoaderReduce.class);
                     shockBuilder.setInputPath(nxtPath)
                             .setOutputPath(nxtPath+"out")
                             .setJobName("http-load"+nxtPath)
                             .setReduceTasks(10);
                     String proxy;
                     if(null != (proxy = properties.getProperty("http-proxy",null)))
                         shockBuilder.setParam("proxy",proxy);
                     Job shockLoadJob = null;
                     try{
                         shockLoadJob = UserGroupInformation.createRemoteUser(username).doAs(new PrivilegedExceptionAction<Job>() {
                             @Override
                             public Job run() throws Exception {
                                 Job job = new Job(shockBuilder.getJobConf());
                                 job.waitForCompletion(true);
                                 return job;
                             }
                         });
                     }catch(Exception e){
                         logger.error("Failed to load data:" + e.toString());
                         return;
                     }
                     try{
                         if(null == shockLoadJob || !shockLoadJob.isSuccessful()){
                             logger.error("Failed to load data from shock");
                             return;
                         }
                     }catch(Exception e){
                         logger.error(e.getMessage());
                         return;
                     }
                     alignIn = new Path(outPath,"http_load").toString();
                 }else{
                     alignIn = inPath;
                 }
                 
                 Path alignOut = new Path(outPath,"bowtie_align");
 
                 final JnomicsJobBuilder alignBuilder = new JnomicsJobBuilder(getGenericConf(),Bowtie2Map.class);
                 alignBuilder.setInputPath(alignIn)
                         .setOutputPath(alignOut.toString())
                         .setParam("bowtie_binary", "bowtie/bowtie2-align")
                         .setParam("bowtie_index", "btarchive/"+organism+".fa")
                         .setJobName(username + "-bowtie2-" + inPath)
                         .addArchive(properties.getProperty("hdfs-index-repo") + "/" + organism + "_bowtie.tar.gz#btarchive")
                         .addArchive(properties.getProperty("hdfs-index-repo") + "/bowtie.tar.gz#bowtie");
                 Job j = null;
                 try{
                     j = UserGroupInformation.createRemoteUser(username).doAs(new PrivilegedExceptionAction<Job>() {
                         @Override
                         public Job run() throws Exception {
                             Job job = new Job(alignBuilder.getJobConf());
                             job.waitForCompletion(true);
                             return job;
                         }
                     });
                 }catch(Exception e){
                     logger.error("Failed to align:" + e.toString());
                     return;
                 }
 
                 logger.info("Alignment task finished");
                 
                 Path snpIn = alignOut;
                 Path snpOut = new Path(outPath, "snp");
                 Job snpJob = null;
                 try {
                     if(null != j && j.isSuccessful()){
                         logger.info("alignment successful, running samtools");
                         final JnomicsJobBuilder snpBuilder = new JnomicsJobBuilder(getGenericConf(), SamtoolsMap.class, SamtoolsReduce.class);
                         snpBuilder.setInputPath(snpIn.toString())
                                 .setOutputPath(snpOut.toString())
                                 .addArchive(properties.getProperty("hdfs-index-repo")+"/"+organism+"_samtools.tar.gz#starchive")
                                 .addArchive(properties.getProperty("hdfs-index-repo")+"/samtools.tar.gz#samtools")
                                 .addArchive(properties.getProperty("hdfs-index-repo")+"/bcftools.tar.gz#bcftools")
                                 .setParam("samtools_binary","samtools/samtools")
                                 .setParam("bcftools_binary","bcftools/bcftools")
                                 .setParam("reference_fa","starchive/"+organism+".fa")
                                 .setParam("genome_binsize","1000000")
                                 .setReduceTasks(NUM_REDUCE_TASKS)
                                 .setJobName(username+"-snp-"+inPath);
 
                         snpJob = UserGroupInformation.createRemoteUser(username).doAs(new PrivilegedExceptionAction<Job>() {
                             @Override
                             public Job run() throws Exception {
                                 Job job = new Job(snpBuilder.getJobConf());
                                 job.waitForCompletion(true);
                                 return job;
                             }
                         });
                     }
                 } catch (Exception e) {
                     logger.error("Failed to call snps" + e.toString());
                     return;
                 }
 
                 try{
                     if(null != snpJob && snpJob.isSuccessful()){
                         mergeVCF(snpOut.toString(),alignOut.toString(),new Path(outPath,"out.vcf").toString(),auth);
                     }
                 }catch(Exception e){
                     logger.error("Problem merging covariates");
                     return;
                 }
             }
         }).start();
         return new JnomicsThriftJobID();
     }
 }
