 /*-
  * Copyright (c) 2012 Red Hat, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.fedoraproject.javadeptools;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 class JavDepTools {
   private final CommandLine line;
   private final String[] args;
 
   private static Options options = new Options();
   static {
     options.addOption("z", "database", true,
             "specify database file location (required)");
     options.addOption(
             "p",
             "provides",
             false,
             "print a set of classes provided by given Fedora package, RPM package or JAR file");
     options.addOption("P", "what-provides", false,
             "print a set of Fedora packages that provide the specified Java class");
     options.addOption(
             "q",
             "requires",
             false,
             "print print a set of Fedora packages required by given Fedora package, RPM package or JAR file");
     options.addOption("Q", "what-requires", false,
             "print print a set of Fedora packages that require given Fedora package");
     options.addOption(
             "d",
             "diff",
             false,
             "show differences between real and declared Fedora or RPM package requirements");
     options.addOption(
             "d",
             "diff",
             false,
             "show differences between real and declared Fedora or RPM package requirements");
     options.addOption("w", "why", false,
             "explain why specified packages require given other package");
     options.addOption("b", "build", false,
             "build package database from specified directory");
     options.addOption("h", "help", false, "print help about usage and exit");
     options.addOption("V", "version", false,
             "print version information and exit");
     options.addOption("list", false, "list all indexed packages");
   }
 
   JavDepTools(String[] args) throws ParseException {
     CommandLineParser parser = new GnuParser();
     line = parser.parse(options, args);
     this.args = line.getArgs();
   }
 
   void run() throws Exception {
     if (line.hasOption("help")) {
       HelpFormatter formatter = new HelpFormatter();
       formatter.setArgName("pattern");
       formatter.printHelp("java-deptool", options);
       System.exit(0);
     }
 
     if (line.hasOption("version")) {
       System.out.println("java-deptools version 0");
       System.out.println("Copyright (c) 2012 Red Hat, Inc.");
       System.out.println("Written by Mikolaj Izdebski");
       System.exit(0);
     }
 
     Set<String> exclusive_opts = new TreeSet<String>();
     exclusive_opts.add("build");
     exclusive_opts.add("requires");
     exclusive_opts.add("what-requires");
     exclusive_opts.add("provides");
     exclusive_opts.add("what-provides");
     exclusive_opts.add("diff");
     exclusive_opts.add("why");
 
     int nExclusive = 0;
     for (String opt : exclusive_opts)
       if (line.hasOption(opt))
         nExclusive++;
 
     if (nExclusive > 1) {
       System.err
               .println("Invalid command line. Specify -h for help about usage.");
       System.exit(1);
     }
     
     if (nExclusive != 1 || args.length == 0 || !line.hasOption("database")) {
       System.err
               .println("Missing argument. Specify -h for help about usage.");
       System.exit(1);
     }
     
     String db_path = line.getOptionValue("database");
 
     if (line.hasOption("build")) {
       build(db_path);
       return;
     }
 
     Database db = new Database(new File(db_path));
 
     if (line.hasOption("requires")) {
       db.query_requires(args);
      return;
     }
 
     if (line.hasOption("provides")) {
       db.query_provides(args[0]);
      return;
     }
 
     if (line.hasOption("what-provides")) {
       db.query_what_provides(args[0]);
      return;
     }
     
     if (line.hasOption("list")) {
       db.list_packages();
      return;
     }
 
     if (line.hasOption("why")) {
       if (args.length < 2) {
         System.err.println("Option --why requires two arguments.");
         System.exit(1);
       }
       db.query_why(args[0], args[1]);
      return;
     }
 
     assert false;
   }
 
   private void build(String db_path) throws IOException {
     Database db = new Database(new File(args[0]), 0);
     db.write(new FileOutputStream(db_path));
   }
 }
