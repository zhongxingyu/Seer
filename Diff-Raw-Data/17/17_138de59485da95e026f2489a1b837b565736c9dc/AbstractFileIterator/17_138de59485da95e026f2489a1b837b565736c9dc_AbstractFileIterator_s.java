 package com.kryshchuk.maven.plugins.filevisitor;
 
 /**
  * Abstract AbstractFileIterator class.
  * 
  * @author yura
  * @since 1.0
  */
 public abstract class AbstractFileIterator {
 
   private final FileMapper fileMapper;
 
   protected AbstractFileIterator(final FileMapper fileMapper) {
     this.fileMapper = fileMapper;
   }
 
   public abstract void iterate(FileVisitor visitor) throws VisitorException;
 
   protected FileMapper getFileMapper() {
     return fileMapper;
   }
 
 }
