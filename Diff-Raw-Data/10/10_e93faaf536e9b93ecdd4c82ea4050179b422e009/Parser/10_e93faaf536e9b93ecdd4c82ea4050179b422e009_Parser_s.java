 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package picoblazerules;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  *
  * @author dc386
  */
 public final class Parser {
     
     List<String>    actions;
     List<String>    protocols;
     List<String>    sourcesIp;
     List<String>    sourcesPort;
     List<String>    directions;
     List<String>    destsIp;
     List<String>    destsPort;
     List<Map<String, String>>       options;
     List<Rule>      rules;
     int             nRules;
     String          fileName;
     String          fileLine = "";
     BufferedReader  buffer = null;
     private static final int ACTION = 0;
     private static final int PROTOCOL = 1;
     private static final int SRCIP = 2;
     private static final int SRCPORT = 3;
     private static final int DIRECTION = 4;
     private static final int DSTIP = 5;
     private static final int DSTPORT = 6;
     private static final int OPTION = 7;
     
     private Parser()
     {
     }
         
     public Parser(String _fileName) throws IOException
     {
         try
         {
             String line;
 
             actions = new ArrayList<String>();
             protocols = new ArrayList<String>();
             sourcesIp = new ArrayList<String>();
             sourcesPort = new ArrayList<String>();
             directions = new ArrayList<String>();
             destsIp = new ArrayList<String>();
             destsPort = new ArrayList<String>();
             options = new ArrayList<Map<String, String>>();
             rules = new ArrayList<Rule>();
 
             buffer = new BufferedReader(new FileReader(_fileName));
             while ((line = buffer.readLine()) != null)
             {
                 if (line.trim().isEmpty())
                     continue;
                 int section = -1;
                 for (String retval: line.split(" ", 8))
                 {
                     section++;
                     switch (section){
                         case ACTION: actions.add(retval);
                             break;
                         case PROTOCOL: protocols.add(retval);
                             break;
                         case SRCIP: sourcesIp.add(retval);
                             break;
                         case SRCPORT: sourcesPort.add(retval);
                             break;
                         case DIRECTION: directions.add(retval);
                             break;
                         case DSTIP: destsIp.add(retval);
                             break;
                         case DSTPORT: destsPort.add(retval);
                             break;
                         case OPTION: options.add(parseOption(retval));
                             break;
                         default:
                             break;
                     }
                 }
                 nRules++;
             }
             buffer.close();
         }
         catch(FileNotFoundException exc)
         {
             System.out.println("Error: File not found.");
         }
         for (int i = 0; i < nRules; i++)
             rules.add(new Rule(actions.get(i), protocols.get(i), sourcesIp.get(i), sourcesPort.get(i), directions.get(i), destsIp.get(i), destsPort.get(i), options.get(i)));
     }
 
     public Map<String, String> parseOption(String options)
     {
         Map<String, String> optionMap = new HashMap<String, String>();
         
         if (options.isEmpty())
             return (null);
         options = options.substring(1, options.length()-1);
         for (String option: options.split(";"))
         {
             int i = 0;
             String key = "";
             for (String value: option.split(":"))
             {
                 value = value.trim();
                 value = value.replaceAll("\"", "");
                 if (i % 2 == 0)
                     key = value;
                 else
                     optionMap.put(key, value);
                 i++;
             }
         }
         return (optionMap);
     }
 
     public List<String> getAction() {
         return actions;
     }
 
     public void setAction(List<String> actions) {
         this.actions = actions;
     }
 
     public BufferedReader getBuffer() {
         return buffer;
     }
 
     public void setBuffer(BufferedReader buffer) {
         this.buffer = buffer;
     }
 
     public List<String> getDestIp() {
         return destsIp;
     }
 
     public void setDestIp(List<String> destsIp) {
         this.destsIp = destsIp;
     }
 
     public List<String> getDestPort() {
         return destsPort;
     }
 
     public void setDestPort(List<String> destsPort) {
         this.destsPort = destsPort;
     }
 
     public String getFileLine() {
         return fileLine;
     }
 
     public void setFileLine(String fileLine) {
         this.fileLine = fileLine;
     }
 
     public String getFileName() {
         return fileName;
     }
 
     public void setFileName(String fileName) {
         this.fileName = fileName;
     }
     
     public List<Map<String, String>> getOption() {
         return options;
     }
 
     public void setOption(List<Map<String, String>> options) {
         this.options = options;
     }
 
     public List<String> getProtocol() {
         return protocols;
     }
 
     public void setProtocol(List<String> protocols) {
         this.protocols = protocols;
     }
 
     public List<String> getSourceIp() {
         return sourcesIp;
     }
 
     public void setSourceIp(List<String> sourcesIp) {
         this.sourcesIp = sourcesIp;
     }
 
     public List<String> getSourcePort() {
         return sourcesPort;
     }
 
     public void setSourcePort(List<String> sourcesPort) {
         this.sourcesPort = sourcesPort;
     }
 
     public int getnRules() {
         return nRules;
     }
 
     public void setnRules(int nRules) {
         this.nRules = nRules;
     }
 
     public List<Rule> getRules() {
         return rules;
     }
 
     public void setRules(List<Rule> rules) {
         this.rules = rules;
     }
 }
