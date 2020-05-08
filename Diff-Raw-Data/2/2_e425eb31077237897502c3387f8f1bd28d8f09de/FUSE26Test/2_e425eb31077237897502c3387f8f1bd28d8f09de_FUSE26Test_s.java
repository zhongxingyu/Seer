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
 
 package org.catacombae.jfuse.test;
 
 import org.catacombae.jfuse.FUSE26Capabilities;
 import org.catacombae.jfuse.FUSE26FileSystemAdapter;
 import org.catacombae.jfuse.types.system.LongRef;
 import org.catacombae.jfuse.types.system.Stat;
 
 /**
  *
  * @author erik
  */
 public class FUSE26Test {
     public static void main(String[] args) {
         FUSE26Capabilities c = new FUSE26Capabilities();
 
         //c.print(System.out, "");
 
         FUSE26FileSystemAdapter fs = new Yada() {
             @Override
             public int getattr(byte[] path, Stat stat) {
                 return -ENOENT;
             }
         };
 
        fs.getCapabilities().print(System.out, "");
     }
 
     private static class Yada extends FUSE26FileSystemAdapter {
         @Override
         public int bmap(byte[] path,
 		     long blocksize,
 		     LongRef idx) {
             return -ENOENT;
         }
     }
 }
