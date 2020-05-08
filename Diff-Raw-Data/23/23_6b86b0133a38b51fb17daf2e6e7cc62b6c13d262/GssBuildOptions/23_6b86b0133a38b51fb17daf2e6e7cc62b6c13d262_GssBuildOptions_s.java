 package com.github.stefanliebenberg.stylesheets;
 
 
 import com.github.stefanliebenberg.utilities.Immuter;
 import com.google.common.collect.ImmutableList;
 
 import java.io.File;
 import java.util.List;
 
 public class GssBuildOptions {
 
     private Boolean shouldGenerateForProduction = false;
 
     private Boolean shouldGenerateForDebug = true;
 
     private ImmutableList<File> sourceFiles;
 
     private File renameMap;
 
     private File assetsDirectory;
 
     private File outputFile;
 
     public void setOutputFile(final File outputFile) {
         this.outputFile = outputFile;
     }
 
     public void setAssetsDirectory(final File assetsDirectory) {
         this.assetsDirectory = assetsDirectory;
     }
 
     public void setSourceFiles(final ImmutableList<File> sourceFiles) {
         this.sourceFiles = sourceFiles;
     }
 
     public void setSourceFiles(final List<File> sourceFiles) {
         setSourceFiles(Immuter.list(sourceFiles));
     }
 
     public void setRenameMap(final File renameMap) {
         this.renameMap = renameMap;
     }
 
     public void setShouldGenerateForProduction(
             final Boolean shouldGenerateForProduction) {
         this.shouldGenerateForProduction = shouldGenerateForProduction;
     }
 
     public void setShouldGenerateForDebug(
             final Boolean shouldGenerateForDebug) {
         this.shouldGenerateForDebug = shouldGenerateForDebug;
     }
 
     public File getOutputFile() {
         return outputFile;
     }
 
     public ImmutableList<File> getSourceFiles() {
         return sourceFiles;
     }
 
     public File getAssetsDirectory() {
         return assetsDirectory;
     }
 
     public File getRenameMap() {
         return renameMap;
     }
 
     public Boolean getShouldGenerateForProduction() {
         return shouldGenerateForProduction;
     }
 
 
     public Boolean getShouldGenerateForDebug() {
         return shouldGenerateForDebug;
     }
 
 
 }
