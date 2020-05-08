 package org.anodyneos.sfs.impl.ant;
 
 import java.io.File;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.taskdefs.MatchingTask;
 import org.apache.tools.ant.types.Path;
 import org.apache.tools.ant.util.GlobPatternMapper;
 import org.apache.tools.ant.util.SourceFileScanner;
 
 public class Sfsc extends MatchingTask {
     private Path src;
     private File destDir;
     private boolean verbose;
     protected File[] compileList = new File[0];
 
     public Sfsc() {
     }
 
     public Path createSrc() {
         if (src == null) {
            src = new Path(project);
         }
         return src.createPath();
     }
 
     protected Path recreateSrc() {
         src = null;
         return createSrc();
     }
 
     public void setSrcdir(Path srcDir) {
         if (src == null) {
             src = srcDir;
         } else {
             src.append(srcDir);
         }
     }
 
     public Path getSrcdir() {
         return src;
     }
 
     public void setDestdir(File destDir) {
         this.destDir = destDir;
     }
 
     public File getDestdir() {
         return destDir;
     }
 
     public void setVerbose(boolean verbose) {
         this.verbose = verbose;
     }
 
     public boolean getVerbose() {
         return verbose;
     }
 
     /**
      *  @exception BuildException
      */
     public void execute() {
         checkParameters();
         resetFileLists();
 
         // scan source directories and dest directory to build up
         // compile lists
         String[] list = src.list();
         for (int i = 0; i < list.length; i++) {
            File srcDir = project.resolveFile(list[i]);
             if (!srcDir.exists()) {
                 throw new BuildException("srcdir \""
                                          + srcDir.getPath()
                                         + "\" does not exist!", location);
             }
 
             DirectoryScanner ds = this.getDirectoryScanner(srcDir);
             String[] files = ds.getIncludedFiles();
 
             scanDir(srcDir, destDir != null ? destDir : srcDir, files);
         }
 
         compile();
     }
 
     protected void resetFileLists() {
         compileList = new File[0];
     }
 
     protected void scanDir(File srcDir, File destDir, String[] files) {
         GlobPatternMapper m = new GlobPatternMapper();
         m.setFrom("*.sfs");
         m.setTo("*.java");
         SourceFileScanner sfs = new SourceFileScanner(this);
         File[] newFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);
 
         if (newFiles.length > 0) {
             File[] newCompileList = new File[compileList.length +
                 newFiles.length];
             System.arraycopy(compileList, 0, newCompileList, 0,
                     compileList.length);
             System.arraycopy(newFiles, 0, newCompileList,
                     compileList.length, newFiles.length);
             compileList = newCompileList;
         }
     }
 
     public File[] getFileList() {
         return compileList;
     }
 
     /**
      *  @exception BuildException
      */
     protected void checkParameters() {
         if (src == null) {
            throw new BuildException("srcdir attribute must be set!",
                                     location);
         }
         if (src.size() == 0) {
            throw new BuildException("srcdir attribute must be set!",
                                     location);
         }
 
         if (destDir != null && !destDir.isDirectory()) {
             throw new BuildException("destination directory \""
                                      + destDir
                                      + "\" does not exist "
                                     + "or is not a directory", location);
         }
     }
 
     /**
      *  @exception BuildException
      */
     protected void compile() {
         if (compileList.length > 0) {
             log("Translating " + compileList.length +
                 " source file"
                 + (compileList.length == 1 ? "" : "s")
                 + (destDir != null ? " to " + destDir : ""));
 
             int argsLength = compileList.length + 2;
             if (verbose) {
                 argsLength += 1;
             }
             String[] args = new String[argsLength];
             int nextArg = 0;
             args[nextArg++] = "-d";
             args[nextArg++] = getDestdir().getAbsolutePath();
             if (verbose) {
                 args[nextArg++] = "-v";
             }
             for (int i = 0; i < compileList.length; i++) {
                 args[nextArg++] = compileList[i].getAbsolutePath();
             }
 
             // see Javac.java task for better error handling.
             try {
                 org.anodyneos.sfs.impl.Main.translate(args);
             } catch (Exception e) {
                throw new BuildException(e.getMessage(), location);
             }
         }
     }
 }
 
