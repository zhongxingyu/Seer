 import java.io.*;
 
 public class GetSourceInfo {
   public static void main(String args[]) {
    File topSrcDir = new File("..");
     String revision = GetSourceInfo.getRevision(topSrcDir);
     String branch = GetSourceInfo.getBranch(topSrcDir);
 
     if (args.length != 0) {
       if (args[0].equals("revision")) {
 	System.out.println(revision);
 	return;
       } else if (args[0].equals("branch")) {
 	System.out.println(branch);
 	return;
       } else if (args[0].equals("update")) {
 	GetSourceInfo.update(topSrcDir, revision, branch);
 	return;
       }
     }
     System.out.println("Usage: java GetSourceInfo [revision | branch | update]");
   }
 
   // Update jst/SourceInfo.java
   public static void update(File topSrcDir, String revision, String branch) {
     File targetFile = new File(topSrcDir, "src/com/fluendo/jst/SourceInfo.java");
     // If branch and revision are unknown, and the file already exists, leave it
     if (branch.equals("(unknown)") && revision.equals("(unknown)") && targetFile.exists()) 
     {
       System.out.println("Unknown revision information, leaving existing file");
       return;
     }
 
     if ( ! (new File(topSrcDir, ".svn")).exists() ) {
       System.out.println("No top-level .svn directory, leaving file alone.");
       return;
     }
 
     System.out.println("Updating SourceInfo file, branch " + branch + ", revision " + revision);
     try {
       FileWriter fw = new FileWriter(targetFile);
       fw.write(
 	  "package com.fluendo.jst;\n" +
 	  "\n" +
 	  "public class SourceInfo\n" +
 	  "{\n" +
 	  "  public String revision = \"" + revision + "\";\n" +
 	  "  public String branch = \"" + branch + "\";\n" +
 	  "\n" +
 	  "  public SourceInfo() {\n" +
 	  "  }\n" +
 	  "}\n"
 	  );
       fw.close();
     } catch (IOException e) {
       System.out.println("Error: Unable to write to SourceInfo.java: " + e.getLocalizedMessage());
     }
   }
 
   public static String getBranch(File topSrcDir) {
     return "Wikimedia";
   }
 
   public static String getRevision(File topSrcDir) {
     // Code here is based on MediaWiki's SpecialVersion::getSvnRevision(),
     // except that it only works with 1.4+, because I couldn't be bothered
     // working out how to read that XML file in Java. [TS]
     String revision = "(unknown)";
     try {
       FileReader fr = new FileReader(new File(topSrcDir, ".svn/entries"));
       LineNumberReader lnr = new LineNumberReader(fr);
       String line = lnr.readLine();
       if (!line.startsWith("<?xml")) {
 	// Subversion 1.4+
 	lnr.readLine();
 	lnr.readLine();
 	line = lnr.readLine();
 	try {
 	  Integer intRev = Integer.parseInt(line.trim());
 	  revision = "r" + intRev.toString();
 	} catch(NumberFormatException e) {
 	  System.out.println("Invalid format for .svn/entries, unable to determine revision");
 	}
       } else {
 	System.out.println("Erorr: Unable to interpret old subversion working copies");
       }
     } catch (IOException e) {
       System.out.println("Error reading .svn/entries: " + e.getLocalizedMessage());
     }
     return revision;
   }
 }
