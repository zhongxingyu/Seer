 package de.chrfritz.jolokiamunin.controller;
 
 import java.util.Properties;
 
 /**
  * Handle the version request.
  */
 public class VersionController extends AbstractController {
     @Override
    protected String handle() throws Exception {
         Properties props = new Properties();
         props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("version.properties"));
 
         StringBuilder buffer = new StringBuilder();
         buffer.append("Jolokia-Munin Plugin by Christian Fritz 2013 \n")
                 .append("Version: ")
                 .append(props.getProperty("jmp.version"))
                 .append("\nBuild from git commit ")
                 .append(props.getProperty("jmp.buildNumber"))
                 .append(" on git branch ")
                 .append(props.getProperty("jmp.buildBranch"))
                 .append("\nBuild date: ")
                 .append(props.getProperty("jmp.buildDate"))
                 .append("\n");
 
         return buffer.toString();
     }
 
     @Override
     public String getHelpMessage() {
         return "Get the version information";
     }
 }
