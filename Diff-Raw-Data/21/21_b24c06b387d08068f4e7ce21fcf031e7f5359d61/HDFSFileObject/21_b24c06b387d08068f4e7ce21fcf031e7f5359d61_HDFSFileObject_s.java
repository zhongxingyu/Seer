 /*
  * Copyright 2010 Pentaho Corporation.  All rights reserved.
  *                   
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * 
  * http://www.apache.org/licenses/LICENSE-2.0 
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License. 
  *
  * @author Michael D'Amour
  */
 package org.pentaho.hdfs.vfs;
 
import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.commons.vfs.FileName;
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.FileType;
 import org.apache.commons.vfs.provider.AbstractFileObject;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 
 public class HDFSFileObject extends AbstractFileObject implements FileObject {
 
   private FileSystem hdfs;
 
   protected HDFSFileObject(final FileName name, final HDFSFileSystem fileSystem) throws FileSystemException {
     super(name, fileSystem);
     hdfs = fileSystem.getHDFSFileSystem();
   }
 
   protected long doGetContentSize() throws Exception {
     return hdfs.getFileStatus(new Path(getName().getPath())).getLen();
   }
 
   protected OutputStream doGetOutputStream(boolean append) throws Exception {
     if (append) {
       FSDataOutputStream out = hdfs.append(new Path(getName().getPath()));
       return out;
     } else {
       FSDataOutputStream out = hdfs.create(new Path(getName().getPath()));
       return out;
     }
   }
 
   protected InputStream doGetInputStream() throws Exception {
     FSDataInputStream in = hdfs.open(new Path(getName().getPath()));
     return in;
   }
 
   protected FileType doGetType() throws Exception {
     FileStatus status = null;
     try {
       status = hdfs.getFileStatus(new Path(getName().getPath()));
    } catch (IOException ioe) {
      // imaginary (doesn't exist or not hooked up yet)
     }
 
     if (status == null) {
       return FileType.IMAGINARY;
     } else if (status.isDir()) {
       return FileType.FOLDER;
     } else {
       return FileType.FILE;
     }
   }
 
   public void doCreateFolder() throws Exception {
     hdfs.mkdirs(new Path(getName().getPath()));
   }
 
   public void doDelete() throws Exception {
     hdfs.delete(new Path(getName().getPath()), true);
   }
 
   protected void doRename(FileObject newfile) throws Exception {
     hdfs.rename(new Path(getName().getPath()), new Path(newfile.getName().getPath()));
   }
 
   protected long doGetLastModifiedTime() throws Exception {
     return hdfs.getFileStatus(new Path(getName().getPath())).getModificationTime();
   }
 
   protected void doSetLastModifiedTime(long modtime) throws Exception {
     hdfs.setTimes(new Path(getName().getPath()), modtime, System.currentTimeMillis());
   }
  
   protected String[] doListChildren() throws Exception {
     FileStatus[] statusList = hdfs.listStatus(new Path(getName().getPath()));
     String[] children = new String[statusList.length];
     for (int i = 0; i < statusList.length; i++) {
       children[i] = statusList[i].getPath().getName();
     }
     return children;
   }
 
 }
