 /*-
  * jFUSE - FUSE bindings for Java
  * Copyright (C) 2008  Erik Larsson <erik82@kth.se>
  *
  * Derived from:
  *   FUSE: Filesystem in Userspace
  *   Copyright (C) 2001-2007  Miklos Szeredi <miklos@szeredi.hu>
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
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 
 @SuppressWarnings("deprecation") // utime and getdir
 public abstract class FUSE26FileSystemAdapter implements FUSEFileSystem {
 
     @Override
     public int getattr(byte[] path, Stat stat) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int readlink(byte[] path, byte[] buffer, long bufferSize) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int getdir(byte[] path, byte[] dirh, FUSEDirFil filler) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int mknod(byte[] path, short fileMode, long deviceNumber) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int mkdir(byte[] path, short createMode) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int unlink(byte[] path) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int rmdir(byte[] path) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int symlink(byte[] sourcePath, byte[] destPath) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int rename(byte[] oldPath, byte[] newPath) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int link(byte[] sourcePath, byte[] destPath) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int chmod(byte[] path, short mode_t_newMode) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int chown(byte[] path, byte[] userId, byte[] groupId) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int truncate(byte[] path, byte[] newSize) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int utime(byte[] path, byte[] struct_utimbuf) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int open(byte[] path, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int read(byte[] path, byte[] dest, int len, int off, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int write(byte[] path, byte[] source, long len, long off, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int statfs(byte[] path, byte[] struct_statvfs) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int flush(byte[] path, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int release(byte[] path, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int fsync(byte[] path, boolean datasync, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int setxattr_BSD(byte[] path, byte[] name, byte[] value, long size, int flags, int position) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int setxattr(byte[] path, byte[] name, byte[] value, long size, int flags) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int getxattr_BSD(byte[] path, byte[] name, byte[] value, long size, int position) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int getxattr(byte[] path, byte[] name, byte[] value, long size) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int listxattr(byte[] path, byte[] namebuf, long size) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int removexattr(byte[] path, byte[] name) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int opendir(byte[] path, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int readdir(byte[] path, FUSEFillDir filler, long offset, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int releasedir(byte[] path, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int fsyncdir(byte[] path, boolean datasync, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public byte[] init(byte[] conn) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public void destroy() {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int access(byte[] path, int mode) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int create(byte[] path, short mode, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int ftruncate(byte[] path, long length, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int fgetattr(byte[] path, Stat stat, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int lock(byte[] path, FUSEFileInfo fi, int cmd, byte[] flock) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int utimens(byte[] path, Timespec[] tv) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public int bmap(byte[] path, long blocksize, LongRef idx) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     @Override
     public final FUSE26Capabilities getCapabilities() {
         FUSE26Capabilities c = new FUSE26Capabilities();
 
         // Find out our capabilities through reflection.
         Class baseClass = FUSE26Operations.class;
        Class<?> subClass = this.getClass();
 
         while(!subClass.equals(FUSE26FileSystemAdapter.class)) {
             for(Method m : baseClass.getDeclaredMethods()) {
                 try {
                     Field f = c.getClass().getField(m.getName());
 
                     try {
                         try {
                             subClass.getDeclaredMethod(m.getName(), m.getParameterTypes());
                             f.setBoolean(c, true);
                         } catch(NoSuchMethodException e) {
                             //f.setBoolean(c, false);
                         }
                     } catch(IllegalAccessException iae) {
                         throw new RuntimeException(iae);
                     }
                 } catch(NoSuchFieldException e) {
                     throw new RuntimeException("No field \"" + m.getName() +
                             "\" in FUSE26Capabilities.", e);
                 }
             }
 
             subClass = subClass.getSuperclass();
         }
 
         return c;
     }
 }
