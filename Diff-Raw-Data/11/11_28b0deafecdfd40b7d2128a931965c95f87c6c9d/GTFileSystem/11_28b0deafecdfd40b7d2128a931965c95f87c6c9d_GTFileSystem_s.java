 /**
  *
  * Licensed under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  * 
  * Implements the Hadoop FS interfaces to allow applications to store
  *files in Kosmos File System (KFS).
  */
 
 package org.apache.hadoop.fs.gtfs;
 
 import org.apache.hadoop.classification.InterfaceAudience;
 import org.apache.hadoop.classification.InterfaceStability;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.*;
 import org.apache.hadoop.fs.permission.FsPermission;
 import org.apache.hadoop.util.Progressable;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 
 import org.apache.hadoop.fs.gtfs.GTFSImpl;
 
 /**
  * A FileSystem backed by GIGA+TableFS FileSystem.
  *
  */
 @InterfaceAudience.Public
 @InterfaceStability.Stable
 public class GTFileSystem extends FileSystem {
 
     private URI uri;
     private Path workingDir = new Path("/");
     private GTFSImpl gtfs_impl;
     private FileSystem hdfs;
     private int threshold = 4096;
 
     public GTFileSystem() {
     }
 
     @Override
     public URI getUri() {
         return uri;
     }
 
     @Override
     public void initialize(URI uri, Configuration conf) throws IOException {
         super.initialize(uri, conf);
         try {
             this.uri = URI.create(uri.getScheme() + "://" + uri.getAuthority());
             this.hdfs = FileSystem.get(uri, conf);
             this.workingDir = new Path("/");
             this.gtfs_impl = new GTFSImpl();
             setConf(conf);
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("Unable to initialize GTFS");
             System.exit(-1);
         }
     }
 
     @Override
     public Path getWorkingDirectory() {
         return workingDir;
     }
 
     @Override
     public void setWorkingDirectory(Path dir) {
         workingDir = makeAbsolute(dir);
     }
 
     private Path makeAbsolute(Path path) {
         if (path.isAbsolute()) {
             return path;
         }
         return new Path(workingDir, path);
     }
 
     public boolean mkdir_recursive(Path path, FsPermission permission) {
         Path parent_path = path.getParent();
         if (parent_path != null) {
             mkdir_recursive(parent_path, permission);
         }
         int res = gtfs_impl.mkDir(path.toString(), permission.toShort());
         return res == 0;
     }
 
     @Override
     public boolean mkdirs(Path path, FsPermission permission
         ) throws IOException {
         Path absolute = makeAbsolute(path);
         return mkdir_recursive(absolute, permission);
     }
 
     public boolean mknod(Path path, FsPermission permission) {
         Path absolute = makeAbsolute(path);
         return gtfs_impl.mkNod(absolute.toString(), permission.toShort()) == 0;
     }
 
     @Override
     public FileStatus[] listStatus(Path path) throws IOException {
         Path absolute = makeAbsolute(path);
         return null;
     }
 
     public String getUser(int uid) {
         return Integer.toString(uid);
     }
 
     public String getGroup(int gid) {
         return Integer.toString(gid);
     }
 
     @Override
     public FileStatus getFileStatus(Path path) throws IOException {
         Path absolute = makeAbsolute(path);
         GTFSImpl.Info info = new GTFSImpl.Info();
         if (gtfs_impl.getInfo(absolute.toString(), info) == 0) {
             return new FileStatus(info.size, info.is_dir > 0, 3, getDefaultBlockSize(),
                                   info.ctime, info.atime,
                                   new FsPermission((short) (info.permission)),
                                   getUser(info.uid), getGroup(info.gid),
                                   path.makeQualified(this.uri, path));
         } else {
             return null;
         }
     }
 
     /**
      * Set permission of a path.
      * @param p
      * @param permission
      */
     public void setPermission(Path p, FsPermission permission
     ) throws IOException {
     }
 
     /**
      * Set owner of a path (i.e. a file or a directory).
      * The parameters username and groupname cannot both be null.
      * @param p The path
      * @param username If it is null, the original username remains unchanged.
      * @param groupname If it is null, the original groupname remains unchanged.
      */
     @Override
     public void setOwner(Path p, String username, String groupname
     ) throws IOException {
     }
 
     /**
      * Set access time of a file
      * @param p The path
      * @param mtime Set the modification time of this file.
      *              The number of milliseconds since Jan 1, 1970.
      *              A value of -1 means that this call should not set modification time.
      * @param atime Set the access time of this file.
      *              The number of milliseconds since Jan 1, 1970.
      *              A value of -1 means that this call should not set access time.
      */
     @Override
     public void setTimes(Path p, long mtime, long atime
     ) throws IOException {
     }
 
     @Override
     public FSDataOutputStream append(Path f, int bufferSize,
         Progressable progress) throws IOException {
         return null;
     }
 
     @Override
     public FSDataOutputStream create(Path file, FsPermission permission,
                                      boolean overwrite, int bufferSize,
                                       short replication,
                                      long blockSize, Progressable progress)
                                     throws IOException {
         int fd = gtfs_impl.create(file.toString(), permission.toShort());
         if (fd < 0) {
             throw new IOException("Cannot create the file:" + file);
         }
         GTFSOutputStream out = new GTFSOutputStream(fd, new Path("/"),
                 permission, overwrite, bufferSize, replication, blockSize, 0,
                 threshold, this, this.gtfs_impl, progress);
         return new FSDataOutputStream(out, statistics);
     }
 
     @Override
     public FSDataInputStream open(Path path, int bufferSize) throws IOException
     {
         if (!exists(path))
             throw new IOException("File does not exist: " + path);
         byte [] buf = new byte[threshold];
         GTFSImpl.FetchReply.ByReference reply =
                 new GTFSImpl.FetchReply.ByReference();
         gtfs_impl.fetch(path.toString(), buf, reply);
         GTFSInputStream in;
        System.out.println("state:"+reply.state);
        System.out.println("buf_len:"+reply.buf_len);
        System.out.println("buf:"+buf);
         if (reply.state == 1) {
             in = new GTFSInputStream(buf, reply.buf_len);
         } else {
             in = new GTFSInputStream(hdfs.open(new Path(buf.toString())));
         }
         return new FSDataInputStream(in);
     }
 
     @Override
     public boolean rename(Path src, Path dst) throws IOException {
         return true;
     }
 
     // recursively delete the directory and its contents
     @Override
     public boolean delete(Path path, boolean recursive) throws IOException {
         Path absolute = makeAbsolute(path);
         if (!recursive) {
             gtfs_impl.unlink(absolute.toString());
         }
         return true;
     }
 
     @Override
     public short getDefaultReplication() {
         return 3;
     }
 
     @Override
     public boolean setReplication(Path path, short replication)
               throws IOException {
         return true;
     }
 
     // 64MB is the GTFS block size
 
     @Override
     public long getDefaultBlockSize() {
         return 1 << 26;
     }
 
     @Deprecated
     public void lock(Path path, boolean shared) throws IOException {
 
     }
 
     @Deprecated
     public void release(Path path) throws IOException {
 
     }
 
     /**
      * Return null if the file doesn't exist; otherwise, get the
      * locations of the various chunks of the file file from KFS.
      */
     @Override
     public BlockLocation[] getFileBlockLocations(FileStatus file, long start,
         long len) throws IOException {
 
         if (file == null) {
             return null;
         }
         return null;
     }
 
     static void testServerConnection() {
         try {
             GTFileSystem fs = new GTFileSystem();
             fs.initialize(new URI("hdfs://localhost/"), new Configuration());
 
             Path root = new Path("/");
             byte buf[] = new byte[1024];
             byte inbuf[] = new byte[4096];
             for (int i = 0; i < 10; ++i) {
                 Path path = new Path(root, Integer.toString(i));
                 for (int j = 0; j < 1024; ++j)
                     buf[j] = (byte) i;
                 FSDataOutputStream outs = fs.create(path,
                         FsPermission.getFileDefault(), true,
                         4096, (short) 3, fs.getDefaultBlockSize(),
                         new GTFSProgress());
                 outs.write(buf, 0, 1024);
                 outs.close();
                 FSDataInputStream ins = fs.open(path, 4096);
                 int ins_len = ins.read(inbuf);
                 if (ins_len != 1024) {
                     System.out.println("length is not match.");
                     System.exit(1);
                 }
                 for (int j = 0; j < 1024; ++j)
                     if (inbuf[j] != i) {
                         System.out.println("read data is wrong:"+inbuf[j]);
                         System.exit(1);
                     }
                 FileStatus status = fs.getFileStatus(path);
                 System.out.println(status.isDir());
                 System.out.println(status.getLen());
             }
             for (int i = 0; i < 10; ++i) {
                 Path path = new Path(root, "dir"+i);
                 fs.mkdirs(path, FsPermission.getFileDefault());
                 FileStatus status = fs.getFileStatus(path);
                 System.out.println(status.isDir());
                 System.out.println(status.getLen());
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public static void main(String[] args) {
         testServerConnection();
     }
 }
 
 
 class GTFSProgress implements Progressable {
     public void progress() {
 
     }
 }
