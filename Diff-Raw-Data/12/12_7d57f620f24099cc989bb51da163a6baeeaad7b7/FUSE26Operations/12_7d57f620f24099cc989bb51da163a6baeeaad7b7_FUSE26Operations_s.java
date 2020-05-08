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
 
 /**
  * All methods must return an int value to indicate status.
  * If <code>null</code> is returned, it is silently treated as the int value
  * EOPNOTSUPP, which is the default return value for "operation not supported".
  *
  * @author Erik Larsson
  */
 public interface FUSE26Operations extends FUSEErrorValues {
     /**
      * <pre>
      * Get file attributes.
      *
      * Similar to stat().  The 'st_dev' and 'st_blksize' fields are
      * ignored.  The 'st_ino' field is ignored except if the 'use_ino'
      * mount option is given.
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param stat <b>(struct stat*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int getattr(byte[] path,
 			Stat stat);
 
     /**
      * <pre>
      * Read the target of a symbolic link
      *
      * The buffer should be filled with a null terminated string.  The
      * buffer size argument includes the space for the terminating
      * null character.  If the linkname is too long to fit in the
      * buffer, it should be truncated.  The return value should be 0
      * for success.
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param buffer <b>(char*)</b>
      * @param bufferSize <b>(size_t)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int readlink(byte[] path,
 			 byte[] buffer,
 			 long bufferSize);
 
     /**
      * <pre>
      * Deprecated, use readdir() instead
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param dirh <b>(fuse_dirh_t)</b>
      * @param filler <b>(fuse_dirfil_t)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      * @deprecated
      */
     public int getdir(byte[] path,
 		       byte[] dirh,
 		       FUSEDirFil filler);
 
     /**
      * <pre>
      * Create a file node
      *
      * This is called for creation of all non-directory, non-symlink
      * nodes.  If the filesystem defines a create() method, then for
      * regular files that will be called instead.
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param fileMode <b>(mode_t)</b>
      * @param deviceNumber <b>(dev_t)</b>
      * @return 0 if successful, an inverted error value from FUSEErrorValues otherwise.
      */
     public int mknod(byte[] path,
 		      short fileMode,
 		      long deviceNumber);
 
     /**
      * <pre>
      * Create a directory
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @param createMode <b>(mode_t)</b>
      * @return 0 if successful, an inverted error value from FUSEErrorValues otherwise.
      */
     public int mkdir(byte[] path,
 		      short createMode);
 
     /**
      * <pre>
      * Remove a file
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @return 0 if successful, an inverted error value from FUSEErrorValues otherwise.
      */
     public int unlink(byte[] path);
 
     /**
      * <pre>
      * Remove a directory
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @return 0 if successful, an inverted error value from FUSEErrorValues otherwise.
      */
     public int rmdir(byte[] path);
     
     /**
      * <pre>
      * Create a symbolic link
      * </pre>
      *
      * @param sourcePath <b>(const char*)</b>
      * @param destPath <b>(const char*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int symlink(byte[] sourcePath,
 			byte[] destPath);
     
     /**
      * <pre>
      * Rename a file 
      * </pre>
      *
      * @param oldPath <b>(const char*)</b>
      * @param newPath <b>(const char*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int rename(byte[] oldPath,
 		       byte[] newPath);
     
     /**
      * <pre>
      * Create a hard link to a file 
      * </pre>
      * 
      * @param sourcePath <b>(const char*)</b>
      * @param destPath <b>(const char*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int link(byte[] sourcePath,
 		     byte[] destPath);
     
     /**
      * <pre>
      * Change the permission bits of a file 
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @param newMode <b>(mode_t)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int chmod(byte[] path,
 		      short newMode);
     
     /**
      * <pre>
      * Change the owner and group of a file
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @param userId <b>(uid_t)</b>
      * @param groupId <b>(uid_t)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int chown(byte[] path,
 		      byte[] userId,
 		      byte[] groupId);
 
     /**
      * <pre>
      * Change the size of a file 
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @param newSize <b>(off_t)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int truncate(byte[] path,
 			 byte[] newSize);
 
     /**
      * <pre>
      * Change the access and/or modification times of a file
      *
      * Deprecated, use utimens() instead.
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @param time <b>(struct utimbuf*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      * @deprecated
      */
     public int utime(byte[] path,
 		      byte[] time);
 
     /**
      * <pre>
      * File open operation
      *
      * No creation, or truncation flags (O_CREAT, O_EXCL, O_TRUNC)
      * will be passed to open().  Open should check if the operation
      * is permitted for the given flags.  Optionally open may also
      * return an arbitrary filehandle in the fuse_file_info structure,
      * which will be passed to all file operations.
      *
      * Changed in version 2.2
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @param fi <b>(struct fuse_file_info*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int open(byte[] path,
 		     FUSEFileInfo fi);
 
     /**
      * <pre>
      * Read data from an open file
      *
      * Read should return exactly the number of bytes requested except
      * on EOF or error, otherwise the rest of the data will be
      * substituted with zeroes.  An exception to this is when the
      * 'direct_io' mount option is specified, in which case the return
      * value of the read system call will reflect the return value of
      * this operation.
      *
      * Changed in version 2.2
      * </pre>
      *
      * @param path <b>(const char*)</b> a path to the file to read from.
      * @param dest <b>(char*)</b> the destination buffer.
      * @param len <b>(size_t)</b> the number of bytes to read.
     * @param off <b>(off_t)</b> offset in <code>dest</code> to start writing.
      * @param fi <b>(struct fuse_file_info*)</b> file info.
      * @return <code>null</code> if the operation completed successfully, and an
      * <code>int</code> value otherwise, indicating what went wrong.
      */
     public int read(byte[] path,
 		     byte[] dest,
 		     int len,
		     int off,
 		     FUSEFileInfo fi);
 
     /**
      * <pre>
      * Write data to an open file
      *
      * Write should return exactly the number of bytes requested
      * except on error.  An exception to this is when the 'direct_io'
      * mount option is specified (see read operation).
      *
      * Changed in version 2.2
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @param data <b>(const char*)</b> the data to write.
      * @param len <b>(size_t)</b> number of bytes to write.
      * @param off <b>(off_t)</b> offset in <code>data</code> to start reading.
      * @param fi <b>(struct fuse_file_info*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int write(byte[] path,
 		      byte[] data,
 		      long len,
 		      long off,
 		      FUSEFileInfo fi);
 
     /**
      * <pre>
      * Get file system statistics
      *
      * The 'f_frsize', 'f_favail', 'f_fsid' and 'f_flag' fields are ignored
      *
      * Replaced 'struct statfs' parameter with 'struct statvfs' in
      * version 2.5
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @param struct_statvfs <b>(struct statvfs*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int statfs(byte[] path,
 		       byte[] struct_statvfs);
 
     /**
      * <pre>
      * Possibly flush cached data
      *
      * BIG NOTE: This is not equivalent to fsync().  It's not a
      * request to sync dirty data.
      *
      * Flush is called on each close() of a file descriptor.  So if a
      * filesystem wants to return write errors in close() and the file
      * has cached dirty data, this is a good place to write back data
      * and return any errors.  Since many applications ignore close()
      * errors this is not always useful.
      *
      * NOTE: The flush() method may be called more than once for each
      * open().  This happens if more than one file descriptor refers
      * to an opened file due to dup(), dup2() or fork() calls.  It is
      * not possible to determine if a flush is final, so each flush
      * should be treated equally.  Multiple write-flush sequences are
      * relatively rare, so this shouldn't be a problem.
      *
      * Filesystems shouldn't assume that flush will always be called
      * after some writes, or that if will be called at all.
      *
      * Changed in version 2.2
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @param fi <b>(struct fuse_file_info*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int flush(byte[] path,
 		      FUSEFileInfo fi);
 
     /**
      * <pre>
      * Release an open file
      *
      * Release is called when there are no more references to an open
      * file: all file descriptors are closed and all memory mappings
      * are unmapped.
      *
      * For every open() call there will be exactly one release() call
      * with the same flags and file descriptor.  It is possible to
      * have a file opened more than once, in which case only the last
      * release will mean, that no more reads/writes will happen on the
      * file.  The return value of release is ignored.
      *
      * Changed in version 2.2
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @param fi <b>(struct fuse_file_info*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int release(byte[] path,
 			FUSEFileInfo fi);
 
     /**
      * <pre>
      * Synchronize file contents
      *
      * If the datasync parameter is non-zero, then only the user data
      * should be flushed, not the meta data.
      *
      * Changed in version 2.2
      * </pre>
      *
      * @param path <b>(const char*)</b> the path to the file system node on
      * which the operation is to be applied.
      * @param datasync <b>(int)</b> set to <code>true</code> if only the user data should be
      * flushed, and not the meta-data.
      * @param fi <b>(struct fuse_file_info*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int fsync(byte[] path, boolean datasync, FUSEFileInfo fi);
 
     /**
      * <pre>
      * Set extended attributes 
      * </pre>
      * @param opt__uint32_t_options Optional argument. Only FreeBSD-compatible
      * operating systems will bother about this argument. This is what Mac OS X
      * says about it: <pre>
      * controls how the attribute is set:
      *
      * XATTR_NOFOLLOW  do not follow symbolic links.  setxattr() normally sets
      *                 attributes on the target of path if it is a symbolic
      *                 link.  With this option, setxattr() will act on the link
      *                 itself.
      * 
      * XATTR_CREATE    fail if the named attribute already exists.
      * 
      * XATTR_REPLACE   fail if the named attribute does not exist.  Failure to
      *                 specify XATTR_REPLACE or XATTR_CREATE allows creation and
      *                 replacement.
      * </pre>
      *
      * @return 0 if successful, or one of the defined error values. If a user
      * defined error value should be returned, bit 32 in the return value should
      * be set to 1, and bits 0-31 should contain the user defined return value.
      */
 
     /**
      * <pre>
      * Set extended attributes
      * </pre>
      *
      * This is the FreeBSD version of the callback. If you want your application
      * to respond properly to the setxattr call on all platforms, you need to
      * implement both setxattr and setxattr_BSD.
      *
      * @param path <b>(const char*)</b>
      * @param name <b>(const char*)</b>
      * @param value <b>(const char*)</b>
      * @param size <b>(size_t)</b>
      * @param flags <b>(int)</b>
      * @param position <b>(uint32_t)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int setxattr_BSD(byte[] path,
 			 byte[] name,
 			 byte[] value,
 			 long size,
 			 int flags,
 			 int position);
 
     /**
      * <pre>
      * Set extended attributes
      * </pre>
      *
      * This is the non-BSD version of the callback. If you want your application
      * to respond properly to the setxattr call on all platforms, you need to
      * implement both setxattr and setxattr_BSD.
      *
      * @param path <b>(const char*)</b>
      * @param name <b>(const char*)</b>
      * @param value <b>(const char*)</b>
      * @param size <b>(size_t)</b>
      * @param flags <b>(int)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int setxattr(byte[] path,
             byte[] name,
             byte[] value,
             long size,
             int flags);
     //#endif /* __FreeBSD__ >= 10 */
 
     /**
      * <pre>
      * Get extended attributes 
      * </pre>
      */
     /**
      * <pre>
      * Get extended attributes
      * </pre>
      *
      * This is the FreeBSD version of the callback. If you want your application
      * to respond properly to the getxattr call on all platforms, you need to
      * implement both getxattr and getxattr_BSD.
      *
      * @param path <b>(const char*)</b>
      * @param name <b>(const char*)</b>
      * @param value <b>(char*)</b>
      * @param size <b>(size_t)</b>
      * @param position <b>(uint32_t)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int getxattr_BSD(byte[] path,
 			 byte[] name,
 			 byte[] value,
 			 long size,
 			 int position);
 
     /**
      * <pre>
      * Get extended attributes
      * </pre>
      *
      * This is the non-BSD version of the callback. If you want your application
      * to respond properly to the getxattr call on all platforms, you need to
      * implement both getxattr and getxattr_BSD.
      *
      * @param path <b>(const char*)</b>
      * @param name <b>(const char*)</b>
      * @param value <b>(char*)</b>
      * @param size <b>(size_t)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int getxattr(byte[] path,
             byte[] name,
             byte[] value,
             long size);
 
     /**
      * <pre>
      * List extended attributes
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param namebuf <b>(char*)</b>
      * @param size <b>size_t</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int listxattr(byte[] path,
 			  byte[] namebuf,
 			  long size);
 
     /**
      * <pre>
      * Remove extended attributes
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param name <b>(const char*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int removexattr(byte[] path,
 			    byte[] name);
 
     /**
      * <pre>
      * Open directory
      *
      * This method should check if the open operation is permitted for
      * this  directory
      *
      * Introduced in version 2.3
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param fi <b>(struct fuse_file_info*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int opendir(byte[] path,
 			FUSEFileInfo fi);
 
     /**
      * <pre>
      * Read directory
      *
      * This supersedes the old getdir() interface.  New applications
      * should use this.
      *
      * The filesystem may choose between two modes of operation:
      *
      * 1) The readdir implementation ignores the offset parameter, and
      * passes zero to the filler function's offset.  The filler
      * function will not return '1' (unless an error happens), so the
      * whole directory is read in a single readdir operation.  This
      * works just like the old getdir() method.
      *
      * 2) The readdir implementation keeps track of the offsets of the
      * directory entries.  It uses the offset parameter and always
      * passes non-zero offset to the filler function.  When the buffer
      * is full (or an error happens) the filler function will return
      * '1'.
      *
      * Introduced in version 2.3
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param filler <b>(fuse_fill_dir_t)</b>
      * @param offset <b>(off_t)</b>
      * @param fi <b>(struct fuse_file_info*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int readdir(byte[] path,
 			FUSEFillDir filler,
 			long offset,
 			FUSEFileInfo fi);
 
     /**
      * <pre>
      * Release directory
      *
      * Introduced in version 2.3
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param fi <b>(struct fuse_file_info*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int releasedir(byte[] path,
 			   FUSEFileInfo fi);
 
     /**
      * <pre>
      * Synchronize directory contents
      *
      * If the datasync parameter is non-zero, then only the user data
      * should be flushed, not the meta data
      *
      * Introduced in version 2.3
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param datasync <b>(int)</b>
      * @param fi <b>(struct fuse_file_info*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int fsyncdir(byte[] path,
 			 boolean datasync,
 			 FUSEFileInfo fi);
 
     /**
      * <pre>
      * Initialize filesystem
      *
      * The return value will passed in the private_data field of
      * fuse_context to all file operations and as a parameter to the
      * destroy() method.
      *
      * Introduced in version 2.3
      * Changed in version 2.6
      * </pre>
      *
      * @param conn <b>(struct fuse_conn_info*)</b>
      * @return (optional) a return value that will be bassed in the private_data
      * field of fuse_context to all file operations and as a parameter to the
      * destroy() method.
      */
     public byte[] init(byte[] conn);
 
     /**
      * <pre>
      * Clean up filesystem
      *
      * Called on filesystem exit.
      *
      * Introduced in version 2.3
      * </pre>
      */
     public void destroy();
 
     /**
      * <pre>
      * Check file access permissions
      *
      * This will be called for the access() system call.  If the
      * 'default_permissions' mount option is given, this method is not
      * called.
      *
      * This method is not called under Linux kernel versions 2.4.x
      *
      * Introduced in version 2.5
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param mode <b>(int)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int access(byte[] path,
 		       int mode);
 
     /**
      * <pre>
      * Create and open a file
      *
      * If the file does not exist, first create it with the specified
      * mode, and then open it.
      *
      * If this method is not implemented or under Linux kernel
      * versions earlier than 2.6.15, the mknod() and open() methods
      * will be called instead.
      *
      * Introduced in version 2.5
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param mode <b>(mode_t)</b>
      * @param fi <b>(struct fuse_file_info*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int create(byte[] path,
 		       short mode,
 		       FUSEFileInfo fi);
 
     /**
      * <pre>
      * Change the size of an open file
      *
      * This method is called instead of the truncate() method if the
      * truncation was invoked from an ftruncate() system call.
      *
      * If this method is not implemented or under Linux kernel
      * versions earlier than 2.6.15, the truncate() method will be
      * called instead.
      *
      * Introduced in version 2.5
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param length <b>(off_t)</b>
      * @param fi <b>(struct fuse_file_info*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int ftruncate(byte[] path,
 			  long length,
 			  FUSEFileInfo fi);
 
     /**
      * <pre>
      * Get attributes from an open file
      *
      * This method is called instead of the getattr() method if the
      * file information is available.
      *
      * Currently this is only called after the create() method if that
      * is implemented (see above).  Later it may be called for
      * invocations of fstat() too.
      *
      * Introduced in version 2.5
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param stat <b>(struct stat*)</b>
      * @param fi <b>(struct fuse_file_info*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int fgetattr(byte[] path,
 			 Stat stat,
 			 FUSEFileInfo fi);
 
     /**
      * <pre>
      * Perform POSIX file locking operation
      *
      * The cmd argument will be either F_GETLK, F_SETLK or F_SETLKW.
      *
      * For the meaning of fields in 'struct flock' see the man page
      * for fcntl(2).  The l_whence field will always be set to
      * SEEK_SET.
      *
      * For checking lock ownership, the 'fuse_file_info->owner'
      * argument must be used.
      *
      * For F_GETLK operation, the library will first check currently
      * held locks, and if a conflicting lock is found it will return
      * information without calling this method.  This ensures, that
      * for local locks the l_pid field is correctly filled in.  The
      * results may not be accurate in case of race conditions and in
      * the presence of hard links, but it's unlikly that an
      * application would rely on accurate GETLK results in these
      * cases.  If a conflicting lock is not found, this method will be
      * called, and the filesystem may fill out l_pid by a meaningful
      * value, or it may leave this field zero.
      *
      * For F_SETLK and F_SETLKW the l_pid field will be set to the pid
      * of the process performing the locking operation.
      *
      * Note: if this method is not implemented, the kernel will still
      * allow file locking to work locally.  Hence it is only
      * interesting for network filesystems and similar.
      *
      * Introduced in version 2.6
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param fi <b>(struct fuse_file_info*)</b>
      * @param cmd <b>(int)</b>
      * @param flock <b>(struct flock*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int lock(byte[] path,
 		     FUSEFileInfo fi,
 		     int cmd,
 		     byte[] flock);
 
     /**
      * <pre>
      * Change the access and modification times of a file with
      * nanosecond resolution
      *
      * Introduced in version 2.6
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param tv <b>(const struct timespec[2])</b> a Timespec array with two
      * values, the first representing the access time, and the other the
      * modification time.
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int utimens(byte[] path,
 			Timespec[] tv); // TODO: tv
 
     /**
      * <pre>
      * Map block index within file to block index within device
      *
      * Note: This makes sense only for block device backed filesystems
      * mounted with the 'blkdev' option
      *
      * Introduced in version 2.6
      * </pre>
      *
      * @param path <b>(const char*)</b>
      * @param blocksize <b>(size_t)</b>
      * @param idx <b>(uint64_t*)</b>
      * @return 0 if successful or an inverted error value from FUSEErrorValues
      * otherwise.
      */
     public int bmap(byte[] path,
 		     long blocksize,
 		     LongRef idx);
 }
 
 // /**
 //  * The file system operations:
 //  *
 //  * Most of these should work very similarly to the well known UNIX
 //  * file system operations.  A major exception is that instead of
 //  * returning an error in 'int', the operation should return the
 //  * negated error value (-int) directly.
 //  *
 //  * All methods are optional, but some are essential for a useful
 //  * filesystem (e.g. getattr).  Open, flush, release, fsync, opendir,
 //  * releasedir, fsyncdir, access, create, ftruncate, fgetattr, lock,
 //  * init and destroy are special purpose methods, without which a full
 //  * featured filesystem can still be implemented.
 //  */
 // struct fuse_operations {
 //     /** Get file attributes.
 //      *
 //      * Similar to stat().  The 'st_dev' and 'st_blksize' fields are
 //      * ignored.  The 'st_ino' field is ignored except if the 'use_ino'
 //      * mount option is given.
 //      */
 //     int (*getattr) (const char *, struct stat *);
 
 //     /** Read the target of a symbolic link
 //      *
 //      * The buffer should be filled with a null terminated string.  The
 //      * buffer size argument includes the space for the terminating
 //      * null character.  If the linkname is too long to fit in the
 //      * buffer, it should be truncated.  The return value should be 0
 //      * for success.
 //      */
 //     int (*readlink) (const char *, char *, size_t);
 
 //     /* Deprecated, use readdir() instead */
 //     int (*getdir) (const char *, fuse_dirh_t, fuse_dirfil_t);
 
 //     /** Create a file node
 //      *
 //      * This is called for creation of all non-directory, non-symlink
 //      * nodes.  If the filesystem defines a create() method, then for
 //      * regular files that will be called instead.
 //      */
 //     int (*mknod) (const char *, mode_t, dev_t);
 
 //     /** Create a directory */
 //     int (*mkdir) (const char *, mode_t);
 
 //     /** Remove a file */
 //     int (*unlink) (const char *);
 
 //     /** Remove a directory */
 //     int (*rmdir) (const char *);
 
 //     /** Create a symbolic link */
 //     int (*symlink) (const char *, const char *);
 
 //     /** Rename a file */
 //     int (*rename) (const char *, const char *);
 
 //     /** Create a hard link to a file */
 //     int (*link) (const char *, const char *);
 
 //     /** Change the permission bits of a file */
 //     int (*chmod) (const char *, mode_t);
 
 //     /** Change the owner and group of a file */
 //     int (*chown) (const char *, uid_t, gid_t);
 
 //     /** Change the size of a file */
 //     int (*truncate) (const char *, off_t);
 
 //     /** Change the access and/or modification times of a file
 //      *
 //      * Deprecated, use utimens() instead.
 //      */
 //     int (*utime) (const char *, struct utimbuf *);
 
 //     /** File open operation
 //      *
 //      * No creation, or truncation flags (O_CREAT, O_EXCL, O_TRUNC)
 //      * will be passed to open().  Open should check if the operation
 //      * is permitted for the given flags.  Optionally open may also
 //      * return an arbitrary filehandle in the fuse_file_info structure,
 //      * which will be passed to all file operations.
 //      *
 //      * Changed in version 2.2
 //      */
 //     int (*open) (const char *, struct fuse_file_info *);
 
 //     /** Read data from an open file
 //      *
 //      * Read should return exactly the number of bytes requested except
 //      * on EOF or error, otherwise the rest of the data will be
 //      * substituted with zeroes.  An exception to this is when the
 //      * 'direct_io' mount option is specified, in which case the return
 //      * value of the read system call will reflect the return value of
 //      * this operation.
 //      *
 //      * Changed in version 2.2
 //      */
 //     int (*read) (const char *, char *, size_t, off_t, struct fuse_file_info *);
 
 //     /** Write data to an open file
 //      *
 //      * Write should return exactly the number of bytes requested
 //      * except on error.  An exception to this is when the 'direct_io'
 //      * mount option is specified (see read operation).
 //      *
 //      * Changed in version 2.2
 //      */
 //     int (*write) (const char *, const char *, size_t, off_t,
 //                   struct fuse_file_info *);
 
 //     /** Get file system statistics
 //      *
 //      * The 'f_frsize', 'f_favail', 'f_fsid' and 'f_flag' fields are ignored
 //      *
 //      * Replaced 'struct statfs' parameter with 'struct statvfs' in
 //      * version 2.5
 //      */
 //     int (*statfs) (const char *, struct statvfs *);
 
 //     /** Possibly flush cached data
 //      *
 //      * BIG NOTE: This is not equivalent to fsync().  It's not a
 //      * request to sync dirty data.
 //      *
 //      * Flush is called on each close() of a file descriptor.  So if a
 //      * filesystem wants to return write errors in close() and the file
 //      * has cached dirty data, this is a good place to write back data
 //      * and return any errors.  Since many applications ignore close()
 //      * errors this is not always useful.
 //      *
 //      * NOTE: The flush() method may be called more than once for each
 //      * open().  This happens if more than one file descriptor refers
 //      * to an opened file due to dup(), dup2() or fork() calls.  It is
 //      * not possible to determine if a flush is final, so each flush
 //      * should be treated equally.  Multiple write-flush sequences are
 //      * relatively rare, so this shouldn't be a problem.
 //      *
 //      * Filesystems shouldn't assume that flush will always be called
 //      * after some writes, or that if will be called at all.
 //      *
 //      * Changed in version 2.2
 //      */
 //     int (*flush) (const char *, struct fuse_file_info *);
 
 //     /** Release an open file
 //      *
 //      * Release is called when there are no more references to an open
 //      * file: all file descriptors are closed and all memory mappings
 //      * are unmapped.
 //      *
 //      * For every open() call there will be exactly one release() call
 //      * with the same flags and file descriptor.  It is possible to
 //      * have a file opened more than once, in which case only the last
 //      * release will mean, that no more reads/writes will happen on the
 //      * file.  The return value of release is ignored.
 //      *
 //      * Changed in version 2.2
 //      */
 //     int (*release) (const char *, struct fuse_file_info *);
 
 //     /** Synchronize file contents
 //      *
 //      * If the datasync parameter is non-zero, then only the user data
 //      * should be flushed, not the meta data.
 //      *
 //      * Changed in version 2.2
 //      */
 //     int (*fsync) (const char *, int, struct fuse_file_info *);
 
 //     /** Set extended attributes */
 // #if (__FreeBSD__ >= 10)
 //     int (*setxattr) (const char *, const char *, const char *, size_t, int, uint32_t);
 // #else
 //     int (*setxattr) (const char *, const char *, const char *, size_t, int);
 // #endif /* __FreeBSD__ >= 10 */
 
 //     /** Get extended attributes */
 // #if (__FreeBSD__ >= 10)
 //     int (*getxattr) (const char *, const char *, char *, size_t, uint32_t);
 // #else
 //     int (*getxattr) (const char *, const char *, char *, size_t);
 // #endif /* __FreeBSD__ >= 10 */
 
 //     /** List extended attributes */
 //     int (*listxattr) (const char *, char *, size_t);
 
 //     /** Remove extended attributes */
 //     int (*removexattr) (const char *, const char *);
 
 //     /** Open directory
 //      *
 //      * This method should check if the open operation is permitted for
 //      * this  directory
 //      *
 //      * Introduced in version 2.3
 //      */
 //     int (*opendir) (const char *, struct fuse_file_info *);
 
 //     /** Read directory
 //      *
 //      * This supersedes the old getdir() interface.  New applications
 //      * should use this.
 //      *
 //      * The filesystem may choose between two modes of operation:
 //      *
 //      * 1) The readdir implementation ignores the offset parameter, and
 //      * passes zero to the filler function's offset.  The filler
 //      * function will not return '1' (unless an error happens), so the
 //      * whole directory is read in a single readdir operation.  This
 //      * works just like the old getdir() method.
 //      *
 //      * 2) The readdir implementation keeps track of the offsets of the
 //      * directory entries.  It uses the offset parameter and always
 //      * passes non-zero offset to the filler function.  When the buffer
 //      * is full (or an error happens) the filler function will return
 //      * '1'.
 //      *
 //      * Introduced in version 2.3
 //      */
 //     int (*readdir) (const char *, void *, fuse_fill_dir_t, off_t,
 //                     struct fuse_file_info *);
 
 //     /** Release directory
 //      *
 //      * Introduced in version 2.3
 //      */
 //     int (*releasedir) (const char *, struct fuse_file_info *);
 
 //     /** Synchronize directory contents
 //      *
 //      * If the datasync parameter is non-zero, then only the user data
 //      * should be flushed, not the meta data
 //      *
 //      * Introduced in version 2.3
 //      */
 //     int (*fsyncdir) (const char *, int, struct fuse_file_info *);
 
 //     /**
 //      * Initialize filesystem
 //      *
 //      * The return value will passed in the private_data field of
 //      * fuse_context to all file operations and as a parameter to the
 //      * destroy() method.
 //      *
 //      * Introduced in version 2.3
 //      * Changed in version 2.6
 //      */
 //     void *(*init) (struct fuse_conn_info *conn);
 
 //     /**
 //      * Clean up filesystem
 //      *
 //      * Called on filesystem exit.
 //      *
 //      * Introduced in version 2.3
 //      */
 //     void (*destroy) (void *);
 
 //     /**
 //      * Check file access permissions
 //      *
 //      * This will be called for the access() system call.  If the
 //      * 'default_permissions' mount option is given, this method is not
 //      * called.
 //      *
 //      * This method is not called under Linux kernel versions 2.4.x
 //      *
 //      * Introduced in version 2.5
 //      */
 //     int (*access) (const char *, int);
 
 //     /**
 //      * Create and open a file
 //      *
 //      * If the file does not exist, first create it with the specified
 //      * mode, and then open it.
 //      *
 //      * If this method is not implemented or under Linux kernel
 //      * versions earlier than 2.6.15, the mknod() and open() methods
 //      * will be called instead.
 //      *
 //      * Introduced in version 2.5
 //      */
 //     int (*create) (const char *, mode_t, struct fuse_file_info *);
 
 //     /**
 //      * Change the size of an open file
 //      *
 //      * This method is called instead of the truncate() method if the
 //      * truncation was invoked from an ftruncate() system call.
 //      *
 //      * If this method is not implemented or under Linux kernel
 //      * versions earlier than 2.6.15, the truncate() method will be
 //      * called instead.
 //      *
 //      * Introduced in version 2.5
 //      */
 //     int (*ftruncate) (const char *, off_t, struct fuse_file_info *);
 
 //     /**
 //      * Get attributes from an open file
 //      *
 //      * This method is called instead of the getattr() method if the
 //      * file information is available.
 //      *
 //      * Currently this is only called after the create() method if that
 //      * is implemented (see above).  Later it may be called for
 //      * invocations of fstat() too.
 //      *
 //      * Introduced in version 2.5
 //      */
 //     int (*fgetattr) (const char *, struct stat *, struct fuse_file_info *);
 
 //     /**
 //      * Perform POSIX file locking operation
 //      *
 //      * The cmd argument will be either F_GETLK, F_SETLK or F_SETLKW.
 //      *
 //      * For the meaning of fields in 'struct flock' see the man page
 //      * for fcntl(2).  The l_whence field will always be set to
 //      * SEEK_SET.
 //      *
 //      * For checking lock ownership, the 'fuse_file_info->owner'
 //      * argument must be used.
 //      *
 //      * For F_GETLK operation, the library will first check currently
 //      * held locks, and if a conflicting lock is found it will return
 //      * information without calling this method.  This ensures, that
 //      * for local locks the l_pid field is correctly filled in.  The
 //      * results may not be accurate in case of race conditions and in
 //      * the presence of hard links, but it's unlikly that an
 //      * application would rely on accurate GETLK results in these
 //      * cases.  If a conflicting lock is not found, this method will be
 //      * called, and the filesystem may fill out l_pid by a meaningful
 //      * value, or it may leave this field zero.
 //      *
 //      * For F_SETLK and F_SETLKW the l_pid field will be set to the pid
 //      * of the process performing the locking operation.
 //      *
 //      * Note: if this method is not implemented, the kernel will still
 //      * allow file locking to work locally.  Hence it is only
 //      * interesting for network filesystems and similar.
 //      *
 //      * Introduced in version 2.6
 //      */
 //     int (*lock) (const char *, struct fuse_file_info *, int cmd,
 //                  struct flock *);
 
 //     /**
 //      * Change the access and modification times of a file with
 //      * nanosecond resolution
 //      *
 //      * Introduced in version 2.6
 //      */
 //     int (*utimens) (const char *, const struct timespec tv[2]);
 
 //     /**
 //      * Map block index within file to block index within device
 //      *
 //      * Note: This makes sense only for block device backed filesystems
 //      * mounted with the 'blkdev' option
 //      *
 //      * Introduced in version 2.6
 //      */
 //     int (*bmap) (const char *, size_t blocksize, uint64_t *idx);
 // };
