 package org.kevoree.library.sky.lxc;
 
 import org.kevoree.ContainerNode;
 import org.kevoree.ContainerRoot;
 import org.kevoree.api.service.core.script.KevScriptEngine;
 import org.kevoree.api.service.core.script.KevScriptEngineException;
 import org.kevoree.api.service.core.script.KevScriptEngineFactory;
 import org.kevoree.framework.KevoreePropertyHelper;
 import org.kevoree.impl.DefaultKevoreeFactory;
 import org.kevoree.library.sky.lxc.utils.FileManager;
 import org.kevoree.library.sky.lxc.utils.SystemHelper;
 import org.kevoree.log.Log;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: jed
  * Date: 05/06/13
  * Time: 09:34
  */
 public class LxcManager {
 
     private String clone_id = "baseclonekevoree";
     private final int timeout = 50;
 
     private final static String lxcstart = "lxc-start";
     private final static String lxcstop = "lxc-stop";
    // private final static String lxcdestroy = "lxc-destroy";
     private final static String lxcshutdown = "lxc-shutdown";
     private final static String lxcclone = "lxc-clone";
     private final static String lxccreate = "lxc-create";
     private final static String lxccgroup = "lxc-cgroup";
     private final static String lxcinfo = "lxc-info";
     private final static String lxcbackup ="lxc-backup";
 
     /*
           cgroup.procs,
           cpuacct.stat ,
           cpuacct.usage  ,
           cpuacct.usage_percpu,
           cpuset.cpu_exclusive,
           cpuset.cpus         ,
           cpuset.mem_exclusive
           cpuset.mem_hardwall
           cpuset.memory_migrate
           cpuset.memory_pressure
           cpuset.memory_spread_page
           cpuset.memory_spread_slab
           cpuset.mems
           cpuset.sched_load_balance
           cpuset.sched_relax_domain_level
           cpu.shares
           devices.allow
           devices.deny
           devices.list
           freezer.state
           memory.failcnt
           memory.force_empty
           memory.limit_in_bytes
           memory.max_usage_in_bytes
           memory.memsw.failcnt
           memory.memsw.limit_in_bytes
           memory.memsw.max_usage_in_bytes
           memory.memsw.usage_in_bytes
           memory.soft_limit_in_bytes
           memory.stat
           memory.swappiness
           memory.usage_in_bytes
           memory.use_hierarchy
           net_cls.classid
                   notify_on_release
           tasks
 */
 
 
     private File watchdogLocalFile = null;
 
     public File getWatchdogLocalFile() {
         return watchdogLocalFile;
     }
 
     public void setWatchdogLocalFile(File watchdogLocalFile) {
         this.watchdogLocalFile = watchdogLocalFile;
     }
 
     public static  void setlimitMemory(String id,int limit_in_bytes) throws InterruptedException, IOException
     {
         if(id != null && id.length() > 0){
             Process processcreate = new ProcessBuilder(lxccgroup, "-n", id, "memory.limit_in_bytes", ""+limit_in_bytes).redirectErrorStream(true).start();
             FileManager.display_message_process(processcreate.getInputStream());
             processcreate.waitFor();
         }  else {
             Log.error("setlimitMemory container id is not set");
         }
     }
 
 
 
     public static  void setCPUAffinity(String id,String cpus) throws InterruptedException, IOException
     {
         if(cpus != null && id != null && cpus.length() > 0 && id.length() > 0){
                 //  lxc-cgroup -n node0 300000000           300M
                 Process processcreate = new ProcessBuilder(lxccgroup, "-n", id, "cpuset.cpus", ""+cpus).redirectErrorStream(true).start();
                 FileManager.display_message_process(processcreate.getInputStream());
                 processcreate.waitFor();
         }  else {
             Log.error("setCPUAffinity container id is not set");
         }
     }
 
     public static void setlimitCPU(String id,int cpu_shares) throws InterruptedException, IOException
     {
         if(id != null && id.length() > 0){
             if(cpu_shares < 1024){
                 // minimum
                 cpu_shares = 1024;
             }
             Process processcreate = new ProcessBuilder(lxccgroup, "-n", id, "cpu.shares", ""+cpu_shares).redirectErrorStream(true).start();
             FileManager.display_message_process(processcreate.getInputStream());
             processcreate.waitFor();
         }  else {
             Log.error("setlimitCPU container id is not set");
         }
     }
 
     public boolean create_container(String id, LxcHostNode service,ContainerNode node, ContainerRoot iaasModel) {
         try {
             Log.debug("LxcManager : " + id + " clone =>" + clone_id);
 
             if (!getContainers().contains(id)) {
                 Log.debug("Creating container " + id + " OS " + clone_id);
                 Process processcreate = new ProcessBuilder(lxcclone, "-o", clone_id, "-n", id).redirectErrorStream(true).start();
                 FileManager.display_message_process(processcreate.getInputStream());
                 processcreate.waitFor();
             } else {
                 Log.warn("Container {} already exist", iaasModel);
             }
 
 
         } catch (Exception e) {
             Log.error("create_container {} clone =>{}",id,clone_id, e);
             return false;
         }
         return true;
     }
 
     public boolean start_container(ContainerNode node) {
         try {
             Integer ram= 0;
             Integer cpu_frequency=0;
             String cpus ="0";
             Log.debug("Starting container " + node.getName());
             Process lxcstartprocess = new ProcessBuilder(lxcstart, "-n", node.getName(), "-d").start();
             FileManager.display_message_process(lxcstartprocess.getInputStream());
             lxcstartprocess.waitFor();
 
             try
             {
                 ram = Integer.parseInt(KevoreePropertyHelper.instance$.getProperty(node, "RAM", false, ""));
                 if(ram != null){
                     setlimitMemory(node.getName(),ram);
                 }   else {
                     Log.info("memory limit_in_bytes is not set for {}",node.getName());
                 }
 
             }catch (Exception e){
                 Log.warn("RAM Limit is not set for {}",node.getName());
             }
 
             try
             {
                 cpu_frequency = Integer.parseInt(KevoreePropertyHelper.instance$.getProperty(node, "CPU_FREQUENCY", false, ""));
                 if(cpu_frequency != null){
                     setlimitCPU(node.getName(),cpu_frequency);
                 } else
                 {
                     Log.info("cpu shares is not set for {}",node.getName());
                 }
 
             }catch (Exception e){
                 Log.warn("CPU_FREQUENCY Limit is not set for {} ",node.getName());
             }
 
             try
             {
                 cpus = KevoreePropertyHelper.instance$.getProperty(node, "CPU_CORE", false, "");
                 if(cpus != null){
                     setCPUAffinity(node.getName(),cpus);
                 } else
                 {
                     Log.info("cpu core is not set for {}",node.getName());
                 }
 
             }catch (Exception e){
                 Log.warn("CPU_CORE Limit is not set for {} ",node.getName());
             }
 
 
 
 
 
         } catch (Exception e) {
             Log.error("start_container",e);
             return  false;
         }
         return true;
     }
 
 
 
 
     public List<String> getBackupContainers() {
         List<String> containers = new ArrayList<String>();
         Process processcreate = null;
         try {
             processcreate = new ProcessBuilder("/bin/lxc-backup-list-containers").redirectErrorStream(true).start();
 
             BufferedReader input = new BufferedReader(new InputStreamReader(processcreate.getInputStream()));
             String line;
             while ((line = input.readLine()) != null) {
                 containers.add(line);
             }
             input.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return containers;
     }
 
     public List<String> getContainers() {
         List<String> containers = new ArrayList<String>();
         Process processcreate = null;
         try {
             processcreate = new ProcessBuilder("/bin/lxc-list-containers").redirectErrorStream(true).start();
 
             BufferedReader input = new BufferedReader(new InputStreamReader(processcreate.getInputStream()));
             String line;
             while ((line = input.readLine()) != null) {
                 containers.add(line);
             }
             input.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return containers;
     }
 
 
     public ContainerRoot buildModelCurrentLxcState(KevScriptEngineFactory factory, String nodename) throws IOException, KevScriptEngineException {
 
         DefaultKevoreeFactory defaultKevoreeFactory = new DefaultKevoreeFactory();
         KevScriptEngine engine = factory.createKevScriptEngine();
         if (getContainers().size() > 0) {
 
             Log.debug("ADD => " + getContainers() + " in current model");
 
            engine.append("merge 'mvn:org.kevoree.corelibrary.sky/org.kevoree.library.sky.lxc/" + defaultKevoreeFactory.getVersion() + "'");
             engine.append("addNode " + nodename + ":LxcHostNode");
 
             for (String node_child_id : getContainers()) {
                 if (!node_child_id.equals(clone_id)) {
                     engine.append("addNode " + node_child_id + ":PJavaSENode");
                     engine.append("addChild " + node_child_id + "@" + nodename);
                 }
                 //    String ip = LxcManager.getIP(node_child_id);
             }
 
             return engine.interpret();
 
         }
         return defaultKevoreeFactory.createContainerRoot();
     }
 
     public synchronized static String getIP(String id) {
         String line;
         try {
             Process processcreate = new ProcessBuilder("/bin/lxc-ip", "-n", id).redirectErrorStream(true).start();
             BufferedReader input = new BufferedReader(new InputStreamReader(processcreate.getInputStream()));
             line = input.readLine();
             input.close();
             return line;
         } catch (Exception e) {
             return null;
         }
 
     }
 
     public synchronized static boolean isRunning(String id) {
         String line;
         try {
             Process processcreate = new ProcessBuilder("lxc-info", "-n", id).redirectErrorStream(true).start();
             BufferedReader input = new BufferedReader(new InputStreamReader(processcreate.getInputStream()));
             line = input.readLine();
             input.close();
             if(line.contains("RUNNING")){
                 return true;
             }    else {
                 return false;
             }
 
         } catch (Exception e) {
             return false;
         }
 
     }
 
 
     private boolean lxc_stop_container(String id, boolean destroy) {
         try {
             Log.debug("Stoping container " + id);
             Process lxcstartprocess = new ProcessBuilder(lxcstop, "-n", id).redirectErrorStream(true).start();
 
             FileManager.display_message_process(lxcstartprocess.getInputStream());
             lxcstartprocess.waitFor();
         } catch (Exception e) {
             Log.error("lxc_stop_container ", e);
             return false;
         }
         if (destroy) {
             try {
                 Log.debug("Disabling the container " + id);
 
                 Process lxcstartprocess = new ProcessBuilder(lxcbackup, "-n", id).redirectErrorStream(true).start();
                 FileManager.display_message_process(lxcstartprocess.getInputStream());
                 lxcstartprocess.waitFor();
 
             } catch (Exception e) {
                 e.printStackTrace();
                 return false;
             }
         }
 
 
         return true;
     }
 
     public boolean stop_container(String id) {
         return lxc_stop_container(id, false);
     }
 
     public boolean remove_container(String id) {
         return lxc_stop_container(id, true);
     }
 
     public void createClone() throws IOException, InterruptedException {
         if (!getContainers().contains(clone_id)) {
             Log.debug("Creating the clone");
             Process lxcstartprocess = new ProcessBuilder(lxccreate, "-n", clone_id, "-t", "kevoree").redirectErrorStream(true).start();
             FileManager.display_message_process(lxcstartprocess.getInputStream());
             lxcstartprocess.waitFor();
         }
     }
 
 
 
     public void copy(String file, String path) throws IOException {
         FileManager.copyFileFromStream(LxcManager.class.getClassLoader().getResourceAsStream(file), path, file, true);
     }
 
     public void allow_exec(String path_file_exec) throws IOException {
         if (SystemHelper.getOS() != SystemHelper.OS.WIN32 && SystemHelper.getOS() != SystemHelper.OS.WIN64) {
             Runtime.getRuntime().exec("chmod 777 " + path_file_exec);
         } else {
             // win32
             System.err.println("ERROR");
         }
     }
 
     /**
      * Install scripts and template
      *
      * @throws IOException
      */
     public void install() throws IOException {
 
 
         copy("lxc-ip", "/bin");
         allow_exec("/bin/lxc-ip");
 
         copy("lxc-list-containers", "/bin");
         allow_exec("/bin/lxc-list-containers");
 
         copy("lxc-backup", "/bin");
         allow_exec("/bin/lxc-backup");
 
         copy("lxc-backup-list-containers", "/bin");
         allow_exec("/bin/lxc-backup-list-containers");
 
         copy("lxc-restore", "/bin");
         allow_exec("/bin/lxc-restore");
 
         copy("lxc-backup-list-containers", "/bin");
         allow_exec("/bin/lxc-backup-list-containers");
 
 
         DefaultKevoreeFactory defaultKevoreeFactory = new DefaultKevoreeFactory();
         String version =   defaultKevoreeFactory.getVersion();
 
         String kevoreeTemplate =    new String(FileManager.load(LxcManager.class.getClassLoader().getResourceAsStream("lxc-kevoree")));
         kevoreeTemplate  = kevoreeTemplate.replace("$KEVOREE-VERSION$",version);
         kevoreeTemplate = kevoreeTemplate.replace("$KEVOREE-WATCHDOG$",watchdogLocalFile.getAbsolutePath());
 
         FileManager.writeFile("/usr/share/lxc/templates/lxc-kevoree",kevoreeTemplate,false);
 
         allow_exec("/usr/share/lxc/templates/lxc-kevoree");
     }
 
                              /*
             String path_to_copy_java = "/var/lib/lxc/"+nodeName+"/rootfs/opt";
 
             System.out.println("Copying jdk 1.7 "+path_to_copy_java);
 
             Process untarjava = new ProcessBuilder("/bin/cp","-R","/root/jdk",path_to_copy_java).start();
 
             FileManager.display_message_process(untarjava.getInputStream());
 
             String javahome = "echo \"JAVA_HOME=/opt/jdk\" >> /var/lib/lxc/"+nodeName+"/rootfs/etc/environment";
             String javabin = "echo \"PATH=$PATH:/opt/jdk/bin\"  >> /var/lib/lxc/"+nodeName+"/rootfs/etc/environment";
             Runtime.getRuntime().exec(javahome);
             Runtime.getRuntime().exec(javabin);                   System.out.println(javahome);
             System.out.println(javabin)
              */
 
 }
