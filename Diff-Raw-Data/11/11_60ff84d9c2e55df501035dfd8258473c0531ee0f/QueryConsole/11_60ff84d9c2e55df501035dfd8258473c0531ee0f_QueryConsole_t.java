 package de.topicmapslab.tmql4j;
 
 import de.topicmapslab.tmql4j.components.processor.runtime.ITMQLRuntime;
 import de.topicmapslab.tmql4j.components.processor.runtime.TMQLRuntimeFactory;
 import de.topicmapslab.tmql4j.query.IQuery;
 import jline.ArgumentCompletor;
 import jline.Completor;
 import jline.ConsoleReader;
 import jline.SimpleCompletor;
import org.apache.commons.io.FileUtils;
 import org.tmapi.core.TMAPIException;
 import org.tmapi.core.TopicMap;
 import org.tmapi.core.TopicMapSystem;
 import org.tmapi.core.TopicMapSystemFactory;
 import org.tmapix.io.CTMTopicMapReader;
 import org.tmapix.io.LTMTopicMapReader;
 import org.tmapix.io.TopicMapReader;
 import org.tmapix.io.XTMTopicMapReader;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * User: mhoyer
  * Date: 08.09.2010
  * Time: 12:07:01
  */
 public class QueryConsole {
     private String prefix;
     private TopicMap topicMap;
     private Map<String, ITMQLRuntime> runtimes;
     private ITMQLRuntime runtime;
     private ResultInterpreter resultInterpreter;
     private TopicMapSystem topicMapSystem;
     private PrintStream output;
     private ConsoleReader reader;
 
     public QueryConsole(PrintStream output, File topicMapFile) throws TMAPIException, IOException {
         this.output = output;
         initJLine();
 
         resultInterpreter = new ResultInterpreter(output);
         prefix = topicMapFile.getName();
 
         importTopicMap(topicMapFile);
 
         runtimes = new HashMap<String, ITMQLRuntime>();
     }
 
     public void registerRuntime(String runtimeVersion) {
         if (runtimes.containsKey(runtimeVersion)) return;
         runtimes.put(runtimeVersion, TMQLRuntimeFactory.newFactory().newRuntime(runtimeVersion));
     }
 
     private void initJLine() throws IOException {
         String[] keywords = new String[] {
                 // axis
                 "topic::",
                 "indicators" ,
                 "characteristics", "atomify",
                 "types", "instances",
                 "supertypes", "subtypes",
                 "players", "roles",
                 "traverse",
                 "scope", "reifier",
 
                 // prefixes
                 "tm:", "xsd:", "tmql:", "fn:", "dc:",
 
                 //functions
                 "fn:string-concat",
                 "fn:length",
                 "fn:string-lt",
                 "fn:string-leq",
                 "fn:string-geq",
                 "fn:string-gt",
                 "fn:regexp",
                 "fn:substring",
                 "fn:has-datatype",
                 "fn:slice",
                 "fn:count",
                 "fn:uniq",
                 "fn:concat",
                 "fn:except",
                 "fn:compare",
                 "fn:zigzag",
 
                 // topicReferences
                 "tm:subject",
                 "tm:name",
                 "tm:occurrence",
                 "tm:subclass-of",
                 "tm:subclass",
                 "tm:superclass",
                 "tm:type-instance",
                 "tm:instance",
                 "tm:type",
 
                 // environment clauses
                 "%prefix",
                 "%pragma"
         };
 
         reader = new ConsoleReader();
         reader.setBellEnabled(false);
         reader.addCompletor(new ArgumentCompletor(new Completor[] {new SimpleCompletor(keywords)}));
     }
 
     private void importTopicMap(File topicMapFile) throws TMAPIException, IOException {
         output.print(String.format("Importing %s ...", topicMapFile.getName()));
         String fileName = topicMapFile.getName().toLowerCase();
 
         topicMapSystem = TopicMapSystemFactory.newInstance().newTopicMapSystem();
         topicMap = topicMapSystem.createTopicMap(topicMapFile.toURI().toString());
 
         TopicMapReader tmReader;
         if(fileName.endsWith(".ltm")) {
             tmReader = new LTMTopicMapReader(topicMap, topicMapFile);
         }
         else if(fileName.endsWith(".ctm")) {
             tmReader = new CTMTopicMapReader(topicMap, topicMapFile);
         }
         else tmReader = new XTMTopicMapReader(topicMap, topicMapFile);
 
         tmReader.read();
         output.println("done!\n");
     }
 
     public void open() throws IOException {
         output.println("Enter '?' for help.");
 
         String q = "";
         String line;
         while((line = reader.readLine(String.format("%s@%s %s ", runtime.getLanguageName(), prefix, q.isEmpty() ? ">" : "|"))) != null)
         {
             if (q.isEmpty()) {
                 String trimedLine = line.trim();
                 if (trimedLine.matches("(e|exit|q|uit)")) break;
                 if (trimedLine.matches("(\\?|h|help)")) { printCommands(); continue; }
                 if (trimedLine.matches("(s|stats)")) { printStats(); continue; }
                 if (trimedLine.matches("(r|runtime)\\s+.*")) { toggleRuntime(trimedLine.replaceAll("(r|runtime)\\s+", "")); continue; }
                 if (trimedLine.matches("(x|external)\\s+.*")) { executeFromFile(trimedLine.replaceAll("(x|external)\\s+", "")); continue; }
             }
 
             q = q.concat(line + "\n");
             if (!line.trim().endsWith(";")) continue;
 
             if (q.trim().length() > 1) {           
                 q = q.substring(0, q.lastIndexOf(";"));
                 runQuery(q);
             }
 
             q = "";
         }
     }
 
     private void executeFromFile(String fileName) {
         File file = new File(fileName);
         if (!file.exists()) {
             output.println(String.format(">> ERROR: File not found '%s'.", fileName));
             return;
         }
 
         try {
            String query = FileUtils.readFileToString(file);
            runQuery(query);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public boolean toggleRuntime(String version) {
         for(String registeredVersion : runtimes.keySet()) {
             if (version.matches(registeredVersion)) {
                 runtime = runtimes.get(registeredVersion);
                 output.println(String.format(">> Set current TMQL runtime to %s.", registeredVersion));
                 return true;
             }
         }
 
         output.println(String.format(">> WARNING: Could not set TMQL runtime for '%s'.", version));
         
         return false;
     }
 
     private void printStats() {
         output.println(String.format("  * Topics: %d\n  * Associations: %d\n", topicMap.getTopics().size(), topicMap.getAssociations().size()));
     }
 
     public void runQuery(String q)
     {
         output.println(String.format("[EnteredQuery = %s]", q));
         
         try {
             IQuery query = runtime.run(topicMap, q);
             resultInterpreter.printResults(query);
         }
         catch (Exception ex)
         {
             output.println(ex.toString());
         }
     }
 
     public void printCommands()
     {
         output.println(String.format("%-28s : %s", "h, help, ?", "Shows this screen"));
         output.println(String.format("%-28s : %s", "e, exit, q, quit", "Exits the console"));
         output.println(String.format("%-28s : %s", "s, stats", "Shows the statistics for loaded Topic Map"));
         output.println(String.format("%-28s : %s", "x, external <queryfile>", "Loads and executes content of queryfile."));
         output.println(String.format("%-28s : %s", "r, runtime <version>", "Changes the TMQL runtime."));
 
         for(String runtimeVersion : runtimes.keySet()) {
             output.println(String.format("                                 * %s", runtimeVersion));
         }
 
         output.println(String.format("\n%s",   "An entered query should be finalized with ; to execute it.\n"));
     }
 
 }
