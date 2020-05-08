 package uk.ac.ebi.fgpt.sampletab.tools;
 
 import java.io.File;
 
 import org.kohsuke.args4j.Option;
 
 import uk.ac.ebi.fgpt.sampletab.AbstractInfileDriver;
 import uk.ac.ebi.fgpt.sampletab.SampleTabToLoadDriver;
 
 public class APITestDriver extends AbstractInfileDriver<APITestCallable> {
 
    @Option(name = "--hostname", usage = "server hostname")
     private String hostname = "www.ebi.ac.uk";
     
     protected APITestCallable getNewTask(File inputFile) {
         return new APITestCallable(inputFile, hostname);
     }
 
     public static void main(String[] args) {
         new APITestDriver().doMain(args);
     }
     
     
 }
