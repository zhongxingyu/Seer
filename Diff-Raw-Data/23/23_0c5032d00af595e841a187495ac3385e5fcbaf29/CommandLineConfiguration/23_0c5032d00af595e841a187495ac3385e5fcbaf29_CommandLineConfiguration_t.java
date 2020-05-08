 package org.ethelred.mymailtool2;
 
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.Option;
 
 /**
  *
  * @author edward
  */
 class CommandLineConfiguration implements MailToolConfiguration
 {
     
     private String user;
     
     @Option(name = "--help", usage = "Show help.", aliases = {"-h", "-?"})
     private boolean showUsage = false;

    @Option(name = "--limit", usage = "Operation limit count", aliases = {"-O"})
    private int limit = PRIMITIVE_DEFAULT;
 
     private String runTimeLimit;
     
     
     @Option(name = "--min-age", usage = "Minimum age of mail to process", aliases = {"-t"})
     private String minAge;
     
     private Map<String, String> mailProperties = Maps.newHashMap();
     
     private List<String> fileLocations = Lists.newArrayList();
 
     private List<FileConfigurationHandler> fileHandlers = Lists.newArrayList();
     
     private TaskName tn;
 
     @Override
     public Iterable<FileConfigurationHandler> getFileHandlers()
     {
         return fileHandlers;
     }
     
     private static enum TaskName
     {
         LIST, FROM_COUNT, TO_COUNT, APPLY
         
     }
 
     
     @Option(name = "--config", usage = "Specify config file. (default $HOME/.mymailtoolrc)", aliases = {"-c"})
     private void setConfigFile(String fileName) {
         fileLocations.add(fileName);
     }
     
     @Option(name = "--handler", usage = "Specify a file handler class to load alternate config file language")
     private void setFileHandlerClass(String className)
     {
         FileConfigurationHandler handler = FileConfigurationHelper.getHandlerForClassName(className);
         if(handler != null)
         {
             fileHandlers.add(handler);
         }
     }
     
     @Option(name = "--list", usage = "List folders.", aliases = {"-l"})
     private void taskListFolders(boolean fake) {
         tn = TaskName.LIST;
     }
 
     @Option(name = "--fromCount", usage = "Count occurrences of from addresses", aliases = {"-f"})
     private void taskFromCount(String folderName) {
         tn = TaskName.FROM_COUNT;
     }
 
         @Option(name = "--toCount", usage = "Count occurrences of to (and CC) addresses", aliases = {"-o"})
     private void taskToCount(String folderName) {
         tn = TaskName.TO_COUNT;
     }
 
     @Option(name = "--apply", usage = "Apply rules - define rules in config file", aliases = {"-a"})
     private void taskApplyRules(String folderName) {
         tn = TaskName.APPLY;
     }
 
     @Option(name = "--host", usage = "mail hostname", aliases = {"-H"})
     private void setHost(String hostname) {
         mailProperties.put(HOST, hostname);
     }
 
     @Option(name = "--port", usage = "mail port. Default 143", aliases = {"-p"})
     private void setPort(int port) {
         mailProperties.put(PORT, String.valueOf(port));
     }
 
     @Option(name = "--user", usage = "mail user", aliases = {"-u"})
     private void setUser(String username) {
         mailProperties.put(USER, username);
         user = username;
     }
     
     boolean isShowUsage()
     {
         return showUsage;
     }
 
     @Override
     public String getPassword()
     {
         return null;
     }
 
     @Override
     public Map<String, String> getMailProperties()
     {
         return mailProperties;
     }
 
     @Override
     public String getUser()
     {
         return user;
     }
 
     @Override
     public Iterable<String> getFileLocations()
     {
         return fileLocations;
     }
 
     @Override
     public Task getTask() throws CmdLineException
     {
         if(tn != null)
         {
             switch(tn)
             {
                 case APPLY:
                     throw new CmdLineException("Apply is not currently supported from command line, please set up with config file");
                 default:
                     throw new CmdLineException("Task " + tn + " is not supported yet");
             }
         }
         return null;
     }
 
     @Override
     public int getOperationLimit()
     {
         return limit;
     }
 
     @Override
     public String getMinAge()
     {
         return minAge;
     }
 
     @Override
     public String getTimeLimit()
     {
         return runTimeLimit;
     }
 
 }
