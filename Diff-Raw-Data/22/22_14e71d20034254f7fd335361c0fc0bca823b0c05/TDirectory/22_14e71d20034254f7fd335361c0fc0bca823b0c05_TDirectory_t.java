 package net.sf.cotta;
 
 import net.sf.cotta.io.OutputManager;
 import net.sf.cotta.io.OutputProcessor;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 /**
  * The class that represent the directory.  Even though the constructor is public, the usual
  * way to create TDirectory should be through TFile, TDirectory, and TFileFactory
  *
  * @see TFileFactory#directoryFromJavaFile(java.io.File)
  * @see TFileFactory#dir(String)
  * @see TFile#parent()
  * @see TDirectory#parent()
  * @see TDirectory#dir(String)
  * @see TDirectory#dir(TPath)
  */
 public class TDirectory extends TEntry {
   /**
    * Constructor that creates the directory to be mainly used internally.
    *
    * @param fileSystem The file system that backs the file
    * @param path       The path to the file
    * @deprecated Use the other constructor for default encoding support in TFileFactory
    * @see #TDirectory(TFileFactory, TPath)
    */
   public TDirectory(FileSystem fileSystem, TPath path) {
     super(new TFileFactory(fileSystem), path);
   }
 
   public TDirectory(TFileFactory factory, TPath path) {
     super(factory, path);
   }
 
   public boolean exists() {
     return filesystem().dirExists(path);
   }
 
   public TDirectory ensureExists() throws TIoException {
     if (!filesystem().dirExists(path)) {
       filesystem().createDir(path);
     }
     return this;
   }
 
   /**
    * Constucts a file given the path.  If the path is relative path,
    * it will be constructed based on the current directory
    *
    * @param path the path of the file
    * @return The file that is under the directory with the name
    * @see #file(TPath)
    */
   public TFile file(String path) {
     return file(TPath.parse(path));
   }
 
   /**
    * Constructs a file given the relative path.  If the path is relative,
    * it will be constructed based on the current direcotry
    *
    * @param path path to the file
    * @return The file that is of the relative to the current directory
    */
   public TFile file(TPath path) {
     return new TFile(factory(), join(path));
   }
 
   private TPath join(TPath path) {
     return path.isRelative() ? this.path.join(path) : path;
   }
 
   /**
    * Constructs a subdirectory given the directory name
    *
    * @param relativePath the relative path of the subdirectory
    * @return The directory that is under the current directory with the given name
    */
   public TDirectory dir(String relativePath) {
     return dir(TPath.parse(relativePath));
   }
 
   /**
    * Constructs a directory given the relative path to the current directory
    *
    * @param path the relative path of the target directory to current directory
    * @return The target directory that is of the given the relative path
    */
   public TDirectory dir(TPath path) {
     return new TDirectory(factory(), join(path));
   }
 
   public TDirectory[] listDirs() throws TIoException {
     return listDirs(TDirectoryFilter.ALL);
   }
 
   public TDirectory[] listDirs(TDirectoryFilter directoryFilter) throws TIoException {
     checkDirectoryExists();
     TPath[] paths = filesystem().listDirs(this.path);
     List<TDirectory> directories = new ArrayList<TDirectory>(paths.length);
     for (TPath path : paths) {
       TDirectory candidate = new TDirectory(factory(), path);
       if (directoryFilter.accept(candidate)) {
         directories.add(candidate);
       }
     }
     return directories.toArray(new TDirectory[directories.size()]);
   }
 
   private void checkDirectoryExists() throws TDirectoryNotFoundException {
     if (!filesystem().dirExists(path)) {
       throw new TDirectoryNotFoundException(path);
     }
   }
 
   public TFile[] listFiles() throws TIoException {
     return listFiles(TFileFilter.ALL);
   }
 
   public TFile[] listFiles(TFileFilter fileFilter) throws TIoException {
     checkDirectoryExists();
     TPath[] paths = filesystem().listFiles(path);
     List<TFile> files = new ArrayList<TFile>(paths.length);
     for (TPath path : paths) {
       TFile candidate = new TFile(factory(), path);
       if (fileFilter.accept(candidate)) {
         files.add(candidate);
       }
     }
     return files.toArray(new TFile[files.size()]);
   }
 
   public String toString() {
     return "TDirectory " + path();
   }
 
   public void delete() throws TIoException {
     filesystem().deleteDirectory(path);
   }
 
   public void deleteAll() throws TIoException {
     TDirectory[] subDirectory = listDirs();
     for (TDirectory aSubDirectory : subDirectory) {
       aSubDirectory.deleteAll();
     }
 
     TFile[] files = listFiles();
     for (TFile file : files) {
       file.delete();
     }
     delete();
   }
 
   public boolean equals(Object o) {
     if (this == o) return true;
     if (o == null || getClass() != o.getClass()) return false;
 
     final TDirectory directory = (TDirectory) o;
 
     return filesystem().equals(directory.filesystem()) && path.equals(directory.path);
 
   }
 
   public void mergeTo(TDirectory target) throws TIoException {
     target.ensureExists();
     copySubDirectories(target);
     copyFiles(target);
   }
 
   private void copySubDirectories(TDirectory target) throws TIoException {
     TDirectory[] subdirs = listDirs();
     for (TDirectory subdir : subdirs) {
       subdir.mergeTo(target.dir(subdir.name()));
     }
   }
 
   private void copyFiles(TDirectory target) throws TIoException {
     TFile[] files = listFiles();
     for (TFile file : files) {
       file.copyTo(target.file(file.name()));
     }
   }
 
   public void moveTo(TDirectory target) throws TIoException {
     if (!exists()) {
       throw new TFileNotFoundException(path);
     }
     if (target.exists()) {
       throw new TIoException(target.path, "Destination exists");
     }
     if (filesystem() == target.filesystem() || filesystem().equals(target.filesystem())) {
       filesystem().moveDirectory(path, target.path);
     } else {
       this.mergeTo(target);
       delete();
     }
   }
 
   /**
    * @return java.io.File presentation of the directory
    * @deprecated use #toJavaFile()
    */
   public File getJavaFile() {
     return toJavaFile();
   }
 
   public void zipTo(TFile file) throws TIoException {
     file.write(new OutputProcessor() {
       public void process(OutputManager outputManager) throws IOException {
         ZipOutputStream zipStream = new ZipOutputStream(outputManager.outputStream());
         outputManager.registerResource(zipStream);
         addDirEntry(zipStream, "", TDirectory.this);
       }
 
       private void addDirEntry(ZipOutputStream zipStream, String path, TDirectory directory) throws IOException {
         TFile[] files = directory.listFiles();
         for (TFile file : files) {
           addFileEntry(zipStream, path, file);
         }
         TDirectory[] directories = directory.listDirs();
         for (TDirectory subDirectory : directories) {
          addDirEntry(zipStream, path + subDirectory.name() + "/", subDirectory);
         }
         zipStream.putNextEntry(new ZipEntry(path + "/"));
         zipStream.closeEntry();
       }
 
       private void addFileEntry(ZipOutputStream zipStream, String path, TFile file) throws IOException {
         ZipEntry entry = new ZipEntry(path + file.name());
         zipStream.putNextEntry(entry);
         file.copyTo(zipStream);
         zipStream.closeEntry();
       }
     });
   }
 
   public void visit(FileVisitor fileVisitor) throws TIoException {
     fileVisitor.visit(this);
   }
 }
