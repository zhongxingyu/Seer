 package fedora.client.batch;
 
 import java.util.Properties;
 import java.io.File;
 
 /**
  *
  * <p><b>Title:</b> AutoBatchBuild.java</p>
  * <p><b>Description:</b> </p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2004 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author rlw@virginia.edu
  * @version $Id$
  */
 public class AutoBatchBuild {
     private Properties batchProperties = new Properties();
 
     public AutoBatchBuild(String objectTemplate, String objectSpecificDir,
        String objectDir, String logFile, String logFormat) throws Exception {
 
         this.batchProperties.setProperty("template", objectTemplate);
         this.batchProperties.setProperty("merge-objects", "yes");
         this.batchProperties.setProperty("specifics", objectSpecificDir);
         this.batchProperties.setProperty("objects", objectDir);
         this.batchProperties.setProperty("pids-format", logFormat);
         this.batchProperties.setProperty("ingested-pids", logFile);
 
         BatchTool batchTool = new BatchTool(this.batchProperties, null, null);
         batchTool.prep();
         batchTool.process();
     }
 
     public static final void main(String[] args) throws Exception {
         boolean errors = false;
         if (args.length == 5) {
             if (!new File(args[0]).exists() && !new File(args[0]).isFile()) {
                 System.out.println("Specified object template file path: \""
                                    + args[0] + "\" does not exist.");
                 errors = true;
             }
             if (!new File(args[1]).isDirectory()) {
                 System.out.println("Specified object specific directory: \""
                                    + args[1] + "\" is not directory.");
                 errors = true;
             }
             if (!new File(args[2]).isDirectory()) {
                 System.out.println("Specified object directory: \""
                                    + args[2] + "\" is not a directory.");
                 errors = true;
             }
             if (!args[4].equals("xml") && !args[4].equals("text")) {
                 System.out.println("Format for log file must must be either: \""
                                    + "\"xml\"  or  \"txt\"");
                 errors = true;
             }
             if (!errors) {
                 AutoBatchBuild autoBatch = new AutoBatchBuild(args[0], args[1], args[2], args[3], args[4]);
             }
         } else {
             System.out.println("\n**** Wrong Number of Arguments *****\n");
             System.out.println("AutoBatchBuild requires 5 arguments.");
             System.out.println("merge-objects=yes");
             System.out.println("(1) - full path to object template file");
             System.out.println("(2) - full path to object specific directory");
             System.out.println("(3) - full path to object directory");
             System.out.println("(4) - full path to log file");
             System.out.println("(5) - format of log file (xml or text)\n");
         }
     }
 }
