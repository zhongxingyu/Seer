 package org.sugarj.common;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.sugarj.common.path.AbsolutePath;
 import org.sugarj.common.path.Path;
 import org.sugarj.common.path.RelativePath;
 import org.sugarj.util.Renaming;
 
 
 /**
  * Shared execution environment.
  * 
  * @author Sebastian Erdweg <seba at informatik uni-marburg de>
  */
 public class Environment implements Serializable {
   
   private static final long serialVersionUID = -8403625415393122607L;
 
   public static Map<Path, IStrategoTerm> terms = new WeakHashMap<Path, IStrategoTerm>();
   
   public static String sep = "/";
   public static String classpathsep = File.pathSeparator;
   
   private boolean generateFiles;
   
   private Path cacheDir = null;
 
   private Path root = new AbsolutePath(".");
   
   private Path bin = new AbsolutePath(".");
 
   /**
    * The directory in which to place files at parse time.
    */
   private final Path parseBin;
   
   /* 
    * parse all imports simultaneously, i.e., not one after the other
    */
   private boolean atomicImportParsing = false;
   
   /*
    * don't check resulting sdf and stratego files after splitting
    */
   private boolean noChecking = false;
 
   private Path tmpDir = new AbsolutePath(System.getProperty("java.io.tmpdir"));
   
   private Set<Path> sourcePath = new HashSet<Path>();
   private Set<Path> includePath = new HashSet<Path>();
   
   /**
    * List of renamings that need to be applied during compilation.
    */
   private List<Renaming> renamings = new LinkedList<Renaming>();
   
   public Environment(boolean generateFiles, Path stdlibDirPath) {
     includePath.add(bin);
     includePath.add(stdlibDirPath);
     
     try {
       this.parseBin = FileCommands.newTempDir();
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
     
     if (!generateFiles)
       includePath.add(parseBin);
   }
   
   public Path getRoot() {
     return root;
   }
 
   public void setRoot(Path root) {
     this.root = root;
   }
 
   public Set<Path> getSourcePath() {
     return sourcePath;
   }
 
   public void setSourcePath(Set<Path> sourcePath) {
     this.sourcePath = sourcePath;
   }
 
   public Path getBin() {
     return generateFiles ? bin : parseBin;
   }
 
   public void setBin(Path bin) {
     if (this.bin != null) {
       includePath.remove(this.bin);
       includePath.add(bin);
     }
     this.bin = bin;
   }
 
   public Path getCacheDir() {
     return cacheDir;
   }
 
   public void setCacheDir(Path cacheDir) {
     this.cacheDir = cacheDir;
   }
 
   public Path getParseBin() {
     return parseBin;
   }
 
   public boolean isAtomicImportParsing() {
     return atomicImportParsing;
   }
 
   public void setAtomicImportParsing(boolean atomicImportParsing) {
     this.atomicImportParsing = atomicImportParsing;
   }
 
   public boolean isNoChecking() {
     return noChecking;
   }
 
   public void setNoChecking(boolean noChecking) {
     this.noChecking = noChecking;
   }
 
   public Path getTmpDir() {
     return tmpDir;
   }
 
   public void setTmpDir(Path tmpDir) {
     this.tmpDir = tmpDir;
   }
 
   public Set<Path> getIncludePath() {
     return includePath;
   }
 
   public void setIncludePath(Set<Path> includePath) {
     this.includePath = includePath;
   }
 
   public RelativePath createCachePath(String relativePath) {
     return new RelativePath(cacheDir, relativePath);
   }
   
   public RelativePath createOutPath(String relativePath) {
     return new RelativePath(getBin(), relativePath);
   }
 
   public List<Renaming> getRenamings() {
     return renamings;
   }
   
   public void setRenamings(List<Renaming> renamings) {
     this.renamings = renamings;
   }
 
   public boolean doGenerateFiles() {
     return generateFiles;
   }
 
   public void setGenerateFiles(boolean b) {
     if (this.generateFiles == b)
       return;
     
     if (this.generateFiles)
       includePath.add(parseBin);
     else
       includePath.add(bin);
 
     this.generateFiles = b;
   }
 }
