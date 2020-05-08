package com.github.wolfie.bob.action;
 
 import java.io.File;
 
import com.github.wolfie.bob.exception.BobRuntimeException;

 public class NotAReadableDirectoryException extends BobRuntimeException {
   private static final long serialVersionUID = 4085543696380952836L;
   
   public NotAReadableDirectoryException(final File file) {
     super(file.getAbsolutePath() + " is not a readable directory.");
   }
   
 }
