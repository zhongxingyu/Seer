 package no.bekk.bigdata.elasticsearch;
 
 import java.io.IOException;
 
 import static no.bekk.bigdata.elasticsearch.Parameters.*;
 /**
  * Created with IntelliJ IDEA.
  * User: andre b. amundsen
  * Date: 15/07-13
  * Time: 09:50
  * To change this template use File | Settings | File Templates.
  */
 public class Main {
 
     private static void printHelp() {
         System.out.println("Possible parameters:");
         System.out.println(" --logging=off        --- turn off logging to console");
         System.out.println(" --hosts=URL         ---- URL to hosts(comma separated). Defaults to " + DEFAULT_HOSTS);
         System.out.println(" --clustername=name         --- Name of elasticsearch cluster. Defaults to " + DEFAULT_CLUSTER);
     }
 
     public static void main(String args[]) throws IOException, IllegalAccessException, InstantiationException {
         System.out.println("Usage: start with --help to get parameter list");
         Parameters parameters = new Parameters();
         for (String param : args) {
             String value = param.contains("=") ? param.split("=")[1] : "";
 
             if (param.startsWith("--help")) {
                 printHelp();
                 return;
             }
 
             if (param.startsWith("--logging")) {
                 if ("off".equals(value)) {
                     parameters.logging = false;
                 }
             }
 
             if (param.startsWith("--hosts")) {
                 parameters.hosts = value.trim();
             }
 
             if (param.startsWith("--clustername")) {
                 parameters.clusterName = value.trim();
             }
 
             if (param.startsWith("--size")) {
                 parameters.size = Integer.parseInt(value.trim());
             }
 
             if (param.startsWith("--repeatCount")) {
                 parameters.repeatCount = Integer.parseInt(value.trim());
             }
 
         }
 
         Util util = new FieldsToCSV();
         util.initialize(parameters);
         util.run();
     }
 }
