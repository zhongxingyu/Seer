 /*-
  * jFUSE - FUSE bindings for Java
  * Copyright (C) 2008-2009  Erik Larsson <erik82@kth.se>
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package org.catacombae.jfuse;
 
 import org.catacombae.jfuse.types.fuse26.FUSEContext;
 
 /**
 * Hooks to some of the FUSE libary functions.
  *
  * @author Erik Larsson
  */
 public class FUSE {
     
     static {
         JNILoader.ensureLoaded();
     }
 
     private static final Object mountSync = new Object();
 
     public static void main(String[] args, FUSE26FileSystem fileSystem) {
         System.err.println("FUSE.main(...)");
         if(args.length < 1)
             throw new IllegalArgumentException("You need to specify the mount point as first argument.");
         String mountPoint = args[0];
         String[] adjustedArgs = new String[args.length - 1];
         if(adjustedArgs.length > 0)
             System.arraycopy(args, 1, adjustedArgs, 0, adjustedArgs.length);
 
         synchronized(mountSync) {
             System.err.println("Calling mountNative26");
             mountNative26(fileSystem, mountPoint, adjustedArgs);
             System.err.println("  done calling mountNative26.");
         }
     }
 
     public static void mount(FUSE26FileSystem fileSystem, String mountPoint, FUSEOptions options) {
         // Never allow more than one mount at the same time.
         synchronized(mountSync) {
             System.err.println("Calling mountNative26");
             mountNative26(fileSystem, mountPoint, options.generateOptionStrings());
             System.err.println("  done calling mountNative26.");
         }
     }
 
     private static native boolean mountNative26(FUSE26FileSystem fileSystem,
             String mountPoint, String[] optionStrings);
 
     /**
      * Get the current context
      *
      * The context is only valid for the duration of a filesystem
      * operation, and thus must not be stored and used later.
      *
      * @return the context
      */
     public static FUSEContext getContext() {
         return getContextNative();
     }
 
     private static native FUSEContext getContextNative();
 }
