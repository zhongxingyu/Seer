 package net.sourceforge.seqware.pipeline.workflowV2.engine.oozie;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.Properties;
 
 import net.sourceforge.seqware.common.module.ReturnValue;
 import net.sourceforge.seqware.common.util.Log;
 import net.sourceforge.seqware.common.util.filetools.FileTools;
 import net.sourceforge.seqware.pipeline.workflowV2.AbstractWorkflowDataModel;
 import net.sourceforge.seqware.pipeline.workflowV2.AbstractWorkflowEngine;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.oozie.client.OozieClient;
 import org.apache.oozie.client.OozieClientException;
 import org.apache.oozie.client.WorkflowAction;
 import org.apache.oozie.client.WorkflowJob;
 
 public class OozieWorkflowEngine extends AbstractWorkflowEngine {
 
   private File dir;
   private String jobId;
   private AbstractWorkflowDataModel dataModel;
   private boolean useSge;
 
   public OozieWorkflowEngine(AbstractWorkflowDataModel objectModel, boolean useSge) {
     this.dataModel = objectModel;
     this.useSge = useSge;
   }
 
   public static String seqwareJarPath(AbstractWorkflowDataModel objectModel) {
     return objectModel.getWorkflowBaseDir() + "/lib/seqware-distribution-"
         + objectModel.getTags().get("seqware_version") + "-full.jar";
   }
 
   @Override
   public void prepareWorkflow(AbstractWorkflowDataModel objectModel) {
     // parse objectmodel
     this.dataModel = objectModel;
     this.setupEnvironment();
     this.parseDataModel(objectModel, useSge, new File(seqwareJarPath(objectModel)));
     this.setupHDFS(objectModel);
   }
 
   public ReturnValue runWorkflow() {
     ReturnValue ret = new ReturnValue(ReturnValue.SUCCESS);
     OozieClient wc = this.getOozieClient();
 
     try {
       Properties conf = wc.createConfiguration();
       String app_path = this.dataModel.getEnv().getOOZIE_APP_PATH() + this.dataModel.getEnv().getOOZIE_APP_ROOT() + "/"
           + this.dir.getName();
       conf.setProperty(OozieClient.APP_PATH, app_path);
       conf.setProperty("jobTracker", this.dataModel.getEnv().getOOZIE_JOBTRACKER());
       conf.setProperty("nameNode", this.dataModel.getEnv().getOOZIE_NAMENODE());
       conf.setProperty("queueName", this.dataModel.getEnv().getOOZIE_QUEUENAME());
 
       jobId = wc.run(conf);
       Log.stdout("Submitted Oozie job: " + jobId);
 
       if (dataModel.isWait()) {
         while (wc.getJobInfo(jobId).getStatus() == WorkflowJob.Status.RUNNING) {
           Log.stdout("Workflow job running ...");
           printWorkflowInfo(wc.getJobInfo(jobId));
           Thread.sleep(10 * 1000);
         }
         Log.stdout("Workflow job completed ...");
         printWorkflowInfo(wc.getJobInfo(jobId));
       }
 
     } catch (RuntimeException e) {
       throw e;
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
 
     return ret;
   }
 
   private void printWorkflowInfo(WorkflowJob wf) {
     Log.stdout("Application Path   : " + wf.getAppPath());
     Log.stdout("Application Name   : " + wf.getAppName());
     Log.stdout("Application Status : " + wf.getStatus());
     Log.stdout("Application Actions:");
     for (WorkflowAction action : wf.getActions()) {
       Log.stdout(MessageFormat.format("   Name: {0} Type: {1} Status: {2}", action.getName(), action.getType(),
                                       action.getStatus()));
     }
   }
 
   /**
    * copy the local dir to HDFS
    */
   private void setupHDFS(AbstractWorkflowDataModel objectModel) {
     Configuration conf = new Configuration();
     conf.set("hbase.zookeeper.quorum", this.dataModel.getEnv().getHbase_zookeeper_quorum());
     conf.set("hbase.zookeeper.property.clientPort", this.dataModel.getEnv().getHbase_zookeeper_property_clientPort());
     conf.set("hbase.master", this.dataModel.getEnv().getHbase_master());
     conf.set("mapred.job.tracker", this.dataModel.getEnv().getMapred_job_tracker());
     conf.set("fs.default.name", this.dataModel.getEnv().getFs_default_name());
     conf.set("fs.defaultFS", this.dataModel.getEnv().getFs_defaultFS());
     conf.set("fs.hdfs.impl", this.dataModel.getEnv().getFs_hdfs_impl());
     // conf.addResource(new Path(this.dataModel.getEnv().getHADOOP_CORE_XML()));
     // conf.addResource(new
     // Path(this.dataModel.getEnv().getHADOOP_HDFS_SITE_XML()));
     // conf.addResource(new
     // Path(this.dataModel.getEnv().getHADOOP_MAPRED_SITE_XML()));
 
     try {
       FileSystem fileSystem = null;
       fileSystem = FileSystem.get(conf);
       Path path = new Path(this.dataModel.getEnv().getOOZIE_APP_ROOT() + "/" + this.dir.getName());
       fileSystem.mkdirs(path);
       Path pathlib = new Path(this.dataModel.getEnv().getOOZIE_APP_ROOT() + "/" + this.dir.getName() + "/lib");
       fileSystem.mkdirs(pathlib);
       this.copyFromLocal(fileSystem, this.dataModel.getEnv().getOOZIE_WORK_DIR() + "/" + this.dir.getName()
           + "/job.properties", this.dataModel.getEnv().getOOZIE_APP_ROOT() + "/" + this.dir.getName());
       this.copyFromLocal(fileSystem, this.dataModel.getEnv().getOOZIE_WORK_DIR() + "/" + this.dir.getName()
           + "/workflow.xml", this.dataModel.getEnv().getOOZIE_APP_ROOT() + "/" + this.dir.getName());
       // copy lib
       this.copyFromLocal(fileSystem, seqwareJarPath(objectModel), this.dataModel.getEnv().getOOZIE_APP_ROOT() + "/"
           + this.dir.getName() + "/lib");
 
       fileSystem.close();
     } catch (RuntimeException e) {
       throw e;
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 
   /**
    * @throws IOException
    * 
    */
   private void setupEnvironment() {
     // create a working directory in /nfs
     // hardcode for now
 
     try {
       this.dir = FileTools.createDirectoryWithUniqueName(new File(this.dataModel.getEnv().getOOZIE_WORK_DIR()), "oozie");
       this.dir.setWritable(true, false);
       // generate job.properties
       this.generateJobProperties();
       // create lib dir
       File lib = new File(this.dir, "lib");
       lib.mkdir();
     } catch (RuntimeException e) {
       throw e;
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
 
   }
 
   /**
    * return a workflow.xml for hadoop
    * 
    * @param objectModel
    * @return
    */
   private File parseDataModel(AbstractWorkflowDataModel objectModel, boolean useSge, File seqwareJar) {
     File file = new File(this.dir, "workflow.xml");
     // generate dax
     OozieWorkflowXmlGenerator daxv2 = new OozieWorkflowXmlGenerator();
     daxv2.generateWorkflowXml(objectModel, file.getAbsolutePath(), this.dir.getAbsolutePath(), useSge, seqwareJar);
     return file;
   }
 
   private void generateJobProperties() {
     File file = new File(this.dir, "job.properties");
     try {
       FileWriter fw = new FileWriter(file);
       fw.write("nameNode=" + this.dataModel.getEnv().getOOZIE_NAMENODE() + "\n");
       fw.write("jobTracker=" + this.dataModel.getEnv().getOOZIE_JOBTRACKER() + "\n");
       fw.write("queueName=" + this.dataModel.getEnv().getOOZIE_QUEUENAME() + "\n");
       fw.write("oozie.wf.application.path=${nameNode}/user/${user.name}/seqware_workflow/" + this.dir.getName());
       fw.close();
     } catch (RuntimeException e) {
       throw e;
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 
   public void copyFromLocal(FileSystem fileSystem, String source, String dest) {
     try {
       Path srcPath = new Path(source);
 
       Path dstPath = new Path(dest);
       // Check if the file already exists
       if (!(fileSystem.exists(dstPath))) {
         System.out.println("No such destination " + dstPath);
         return;
       }
 
       // Get the filename out of the file path
       String filename = source.substring(source.lastIndexOf('/') + 1, source.length());
 
       fileSystem.copyFromLocalFile(srcPath, dstPath);
      System.out.println("File " + filename + " copied to " + dest);
     } catch (RuntimeException e) {
       throw e;
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 
   @Override
   public String getLookupToken() {
     return this.jobId;
   }
 
   private OozieClient getOozieClient() {
     OozieClient oc = new OozieClient(this.dataModel.getEnv().getOOZIE_URL());
     return oc;
   }
 
   // @Override
   // public String getId() {
   // return this.jobId;
   // }
   //
   // @Override
   // public String getStatus(String id) {
   // OozieClient oc = this.getOozieClient();
   // try {
   // WorkflowJob wfJob = oc.getJobInfo(id);
   // if (wfJob == null)
   // return null;
   // return wfJob.getStatus().toString();
   // } catch (OozieClientException e) {
   // e.printStackTrace();
   // return null;
   // }
   // }
   //
   // @Override
   // /**
   // * get the first failed job's error message
   // */
   // public String getStdErr(String id) {
   // OozieClient oc = this.getOozieClient();
   // StringBuilder sb = new StringBuilder();
   // try {
   // WorkflowJob wfJob = oc.getJobInfo(id);
   //
   // if (wfJob == null)
   // return null;
   // for (WorkflowAction action : wfJob.getActions()) {
   // if (action.getErrorMessage() != null) {
   // sb.append(MessageFormat.format("   Name: {0} Type: {1} ErrorMessage: {2}",
   // action.getName(),
   // action.getType(), action.getErrorMessage()));
   // sb.append("\n");
   // }
   // }
   // return sb.toString();
   // } catch (OozieClientException e) {
   // e.printStackTrace();
   // return null;
   // }
   // }
   //
   // @Override
   // public String getStdOut(String id) {
   // OozieClient oc = this.getOozieClient();
   // StringBuilder sb = new StringBuilder();
   // try {
   // WorkflowJob wfJob = oc.getJobInfo(id);
   //
   // if (wfJob == null)
   // return null;
   // for (WorkflowAction action : wfJob.getActions()) {
   // if (action.getErrorMessage() != null) {
   // sb.append(MessageFormat.format("   Name: {0} Type: {1} ErrorMessage: {2}",
   // action.getName(),
   // action.getType(), action.getStatus()));
   // sb.append("\n");
   // }
   // }
   // return sb.toString();
   // } catch (OozieClientException e) {
   // e.printStackTrace();
   // return null;
   // }
   // }
   //
   // @Override
   // public String getStatus() {
   // return this.getStatus(this.getId());
   // }
 }
