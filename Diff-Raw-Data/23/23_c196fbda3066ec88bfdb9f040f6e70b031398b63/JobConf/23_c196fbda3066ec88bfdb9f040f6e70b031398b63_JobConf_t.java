 package cmu.ds.mr.conf;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 
 import cmu.ds.mr.io.FileSplit;
 import cmu.ds.mr.util.Log;
 import cmu.ds.mr.util.Util;
 
/**
 * JobConf 
 * */
 public class JobConf implements Serializable {
   private static final Log LOG = new Log("JobConf.class");
 
   private ArrayList<Object> resources = new ArrayList<Object>();
 
   private String inpath;
   private String outpath;
   private String mapOutPath;
   private String jobTrackerAddr;
   private int numReduceTasks;
   private int numMapTasks;
 
   private Class<?> mapclass;
   private Class<?> redclass;
   private Class mainclass;
   private Class outputKeyClass;
   private Class outputValueClass;
   
   private List<FileSplit> splitFiles;
   
   private Properties properties = new Properties();
 
   
   // read configure file in 
   public JobConf() throws FileNotFoundException, IOException {
     properties = new Properties();
     properties.load(new FileInputStream(Util.CONFIG_PATH));
     
     // set default numReduceTask
     numReduceTasks = 4;
   }
   
   public List<FileSplit> getSplitFiles() {
     return splitFiles;
   }
 
   public void setSplitFiles(List<FileSplit> splitFiles) {
     this.splitFiles = splitFiles;
   }
 
   public String getJobTrackerAddr() {
     return jobTrackerAddr;
   }
 
   public void setJobTrackerAddr(String jobTrackerAddr) {
     this.jobTrackerAddr = jobTrackerAddr;
   }
 
   
   public Class getOutputKeyClass() {
     return outputKeyClass;
   }
 
   public void setOutputKeyClass(Class outputKeyClass) {
     this.outputKeyClass = outputKeyClass;
   }
 
   public Class getOutputValueClass() {
     return outputValueClass;
   }
 
   public void setOutputValueClass(Class outputValueClass) {
     this.outputValueClass = outputValueClass;
   }
 
   public int getNumReduceTasks() {
     return numReduceTasks;
   }
 
   public void setNumReduceTasks(int numReduceTasks) {
     this.numReduceTasks = numReduceTasks;
   }
 
   public int getNumMapTasks() {
     return numMapTasks;
   }
 
   public void setNumMapTasks(int numMapTasks) {
     this.numMapTasks = numMapTasks;
   }
 
   public void setMapperClass(Class mapclass) {
     this.mapclass = mapclass;
   }
 
   public void setReducerClass(Class redclass) {
     this.redclass = redclass;
   }
 
   public String getJobName() {
     return get(Util.JOB_NAME);
   }
 
   public void setJobName(String name) {
     set(Util.JOB_NAME, name);
   }
 
   public void set(String key, String val) {
     properties.setProperty(key, val);
   }
 
   public String get(String key) {
     return properties.getProperty(key);
   }
 
   public ArrayList<Object> getResources() {
     return resources;
   }
 
   public void setResources(ArrayList<Object> resources) {
     this.resources = resources;
   }
 
   public String getMapOutPath() {
     return mapOutPath;
   }
 
   public void setMapOutPath(String mapOutPath) {
     this.mapOutPath = mapOutPath;
   }
 
   public String getInpath() {
     return inpath;
   }
 
   public void setInpath(String inpath) {
     this.inpath = inpath;
   }
 
   public String getOutpath() {
     return outpath;
   }
 
   public void setOutpath(String outpath) throws IOException {
     File f = new File(outpath);
     if(f.exists()) {
       LOG.error("Output file exists!");
       System.exit(Util.EXIT_OUT_EXIST);
     }
       
     this.outpath = outpath;
   }
 
   public Class<?> getMapperclass() {
     return mapclass;
   }
 
   public void setMapperclass(Class<?> mapperclass) {
     this.mapclass = mapperclass;
   }
 
   public Class<?> getReducerclass() {
     return redclass;
   }
 
   public void setReducerclass(Class<?> reducerclass) {
     this.redclass = reducerclass;
   }
 
   public Properties getProperties() {
     return properties;
   }
 
   public void setProperties(Properties properties) {
     this.properties = properties;
   }
 }
