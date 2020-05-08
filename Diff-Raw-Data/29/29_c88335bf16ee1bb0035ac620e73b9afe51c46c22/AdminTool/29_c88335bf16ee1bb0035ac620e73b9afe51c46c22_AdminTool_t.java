 package org.amplafi.flow.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.amplafi.dsl.BindingFactory;
 import org.amplafi.dsl.FlowTestDSL;
 import org.amplafi.dsl.ScriptRunner;
 import org.amplafi.flow.definitions.FarReachesServiceInfo;
 
 import com.sworddance.util.NotNullIterator;
 
 /**
  * Generic interface to track state of farreach.es server communication. This is where all
  * connection information is loaded (look at the constructor. we use several properties files that
  * are loaded sequentially overriding one another.) The AdminTool is the new way to control the
  * {@link FlowTestDSL} from outside the scripts. Ideally you don't want to access the DSL directly
  * unless you're messing with script internals or modifying the {@link FarReachesServiceInfo}
  * embedded there.
  */
 public class AdminTool {
     private Map<String, String> scriptsAvailable = new ConcurrentHashMap<>();
 
     private ScriptRunner runner;
 
     private BindingFactory bindingFactory;
 
     private FarReachesServiceInfo farReachesServiceInfo;
 
     public static final String CONFIG_FILE = "farreaches.fadmin.properties";
 
     private static final Pattern SCRIPT_PATTERN = Pattern.compile("^(.*)\\.groovy$");
 
     public AdminTool(BindingFactory bindingFactory) {
         this.setBindingFactory(bindingFactory);
 
         FarReachesServiceInfo serviceInfo = new FarReachesServiceInfo();
         serviceInfo.setMode("local");
         this.setServiceInfo( serviceInfo);
         // TODO: validate keys
         String scriptsFolder = serviceInfo.getProperty("scripts_folder");
 
         loadScriptsAvailable(scriptsFolder);
         runner = new ScriptRunner(bindingFactory);
     }
 
     private void loadScriptsAvailable(String scriptsFolder) {
        if (scriptsFolder != null) {
            System.out.println("loading scripts from "+scriptsFolder+" ('scripts_folder' directory)...");
            File dir = new File(scriptsFolder);
            File[] files = dir.listFiles();
            for (File file : NotNullIterator.<File> newNotNullIterator(files)) {
                Matcher m = SCRIPT_PATTERN.matcher(file.getName());
                if (m.matches()) {
                    try {
                        System.out.println(m.group(1) +"\t\t=("+ file.getCanonicalPath()+")");
                        scriptsAvailable.put(m.group(1), file.getCanonicalPath());
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                 }
             }
         }
     }
 
     public void runScriptName(String name) throws IOException {
         this.runner.loadAndRunOneScript(name);
     }
 
     public boolean runScript(String script) throws IOException {
         String filePath = scriptsAvailable.get(script);
         if (null == filePath) {
            boolean exists = new File(script).exists();
            runner.loadAndRunOneScript(script);
            return exists;
         } else {
             runner.loadAndRunOneScript(filePath);
             return true;
         }
     }
 
     public Map<String, String> getAvailableScripts() {
         return scriptsAvailable;
     }
 
     public boolean describeFlow(String api, String flow) {
         return getBindingFactory().getDSL().describeFlow(api, flow);
     }
 
     public FlowResponse request(String api, String flowName, Map paramsMap) {
         return getBindingFactory().getDSL().request(api, flowName, paramsMap);
     }
 
     public void setServiceInfo(FarReachesServiceInfo serviceInfo) {
         System.out.println("Updated Service Info");
         this.farReachesServiceInfo = serviceInfo;
         getBindingFactory().setDSL(new FlowTestDSL(this.farReachesServiceInfo, runner));
     }
 
     public String getPrompt() {
         return this.farReachesServiceInfo.getPrompt();
     }
     public BindingFactory getBindingFactory() {
         return bindingFactory;
     }
 
     public void setBindingFactory(BindingFactory bindingFactory) {
         this.bindingFactory = bindingFactory;
     }
 
     public void setMode(String mode) {
         this.farReachesServiceInfo.setMode(mode);
     }
 }
