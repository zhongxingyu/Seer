 /*
  * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
  */
 package com.inversoft.mybatis.generator;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.inversoft.mybatis.generator.domain.Table;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 /**
  * Runs the generator and writes out the XML and Java files.
  *
  * @author Brian Pontarelli
  */
 public class MyBatisGenerator {
   public void run(MyBatisGeneratorOptions options) throws IOException, TemplateException {
     System.out.printf("Running MyBatis generator for:\n  database [%s]\n  username [%s]\n  password [%s]\n  table [%s]\n  " +
       "package [%s]\n  javaDirectory [%s]\n  xmlDirectory [%s]\n  templateDirectory [%s]\n",
       options.jdbcURL,
       options.username,
       options.password,
       options.table,
       options.javaPackage,
       options.javaDirectory,
       options.xmlDirectory,
       options.templateDirectory);
 
     DatabaseInspector inspector = new DatabaseInspector();
     Table table = inspector.extract(options);
 
     // FreeMarker output
     DefaultObjectWrapper objectWrapper = new DefaultObjectWrapper();
     objectWrapper.setExposeFields(true);
 
     Configuration configuration = new Configuration();
     configuration.setObjectWrapper(objectWrapper);
     configuration.setDirectoryForTemplateLoading(options.templateDirectory);
 
     Map<String, Object> rootMap = new HashMap<String, Object>();
     rootMap.put("table", table);
     rootMap.put("options", options);
 
     // Write out the Java file
     File javaFile = new File(options.javaDirectory, table.fullJavaClassName.replace('.', '/') + ".java");
     if (!javaFile.getParentFile().isDirectory() && !javaFile.getParentFile().mkdirs()) {
       throw new IOException("Unable to create directory for the Java file [" + javaFile.getAbsolutePath() + "]");
     }
 
     FileWriter writer = new FileWriter(javaFile);
     Template template = configuration.getTemplate("java.ftl");
     template.process(rootMap, writer);
     writer.close();
 
     // Write out the Java file
     File xmlFile = new File(options.xmlDirectory, table.fullJavaClassName.replace('.', '/') + "Mapper.xml");
     if (!xmlFile.getParentFile().isDirectory() && !xmlFile.getParentFile().mkdirs()) {
       throw new IOException("Unable to create directory for the XML file [" + xmlFile.getAbsolutePath() + "]");
     }
 
     writer = new FileWriter(xmlFile);
     template = configuration.getTemplate("xml.ftl");
     template.process(rootMap, writer);
     writer.close();
   }
 }
