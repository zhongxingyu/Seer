 /*
  * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
  */
 package com.inversoft.mybatis.generator;
 
 import java.io.File;
 
 import org.apache.commons.cli.CommandLine;
 
 /**
  * @author Brian Pontarelli
  */
 public class MyBatisGeneratorOptions {
   public final String jdbcURL;
   public final String username;
   public final String password;
   public final String table;
   public final String javaPackage;
   public final File javaDirectory;
   public final File xmlDirectory;
   public final File templateDirectory;
 
   public MyBatisGeneratorOptions(String jdbcURL, String username, String password, String table, String javaPackage,
                                  File javaDirectory, File xmlDirectory, File templateDirectory) {
     this.jdbcURL = jdbcURL;
     this.username = username;
     this.password = password;
     this.table = table;
     this.javaPackage = javaPackage;
     this.javaDirectory = javaDirectory;
     this.xmlDirectory = xmlDirectory;
     this.templateDirectory = templateDirectory;
   }
 
   public MyBatisGeneratorOptions(CommandLine commandLine) {
     this.jdbcURL = commandLine.getOptionValue("jdbc");
     this.username = commandLine.getOptionValue("user");
     this.password = commandLine.getOptionValue("pass");
     this.table = commandLine.getOptionValue("table");
     this.javaPackage = commandLine.getOptionValue("pkg");
 
     this.javaDirectory = new File(commandLine.hasOption("java") ? commandLine.getOptionValue("java") : "src/main/java");
     this.xmlDirectory = new File(commandLine.hasOption("xml") ? commandLine.getOptionValue("xml") : "src/main/resources");
    this.templateDirectory = new File(commandLine.hasOption("template") ? commandLine.getOptionValue("template") : System.getProperty("home") + "/templates");
   }
 }
