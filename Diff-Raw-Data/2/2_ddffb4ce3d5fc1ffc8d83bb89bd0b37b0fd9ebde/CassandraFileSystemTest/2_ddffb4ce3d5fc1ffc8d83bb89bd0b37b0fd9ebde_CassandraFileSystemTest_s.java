 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.cassandra.hadoop.fs;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.*;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Set;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import org.apache.cassandra.CleanupHelper;
 import org.apache.cassandra.EmbeddedServer;
 import org.apache.cassandra.config.ConfigurationException;
 import org.apache.cassandra.config.DatabaseDescriptor;
 import org.apache.cassandra.utils.FBUtilities;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.BlockLocation;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.Path;
 import org.apache.thrift.transport.TTransportException;
 
 public class CassandraFileSystemTest extends CleanupHelper
 {
     private static EmbeddedServer brisk;
 
     /**
      * Set embedded cassandra up and spawn it in a new thread.
      *
      * @throws TTransportException
      * @throws IOException
      * @throws InterruptedException
      */
     @BeforeClass
     public static void setup() throws TTransportException, IOException, InterruptedException, ConfigurationException
     {
         brisk = new EmbeddedServer();
         brisk.startBrisk();                
     }
 
     
     @Test
     public void testFileSystem() throws Exception
     {
         
         CassandraFileSystem fs = new CassandraFileSystem();
         
         fs.initialize(URI.create("cassandra://localhost:"+DatabaseDescriptor.getRpcPort()+"/"), new Configuration());        
         
         //
         fs.mkdirs(new Path("/mytestdir"));
         fs.mkdirs(new Path("/mytestdir/sub1"));
         fs.mkdirs(new Path("/mytestdir/sub2"));
         fs.mkdirs(new Path("/mytestdir/sub3"));
         fs.mkdirs(new Path("/mytestdir/sub3/sub4"));
         
 
         //Create a 1MB file to sent to fs
         File tmp = File.createTempFile("testcfs", "input");
 
         Writer writer = new FileWriter(tmp);
      
         char buf[] = new char[1024];
         Arrays.fill(buf,'x');
         
         for(int i=0; i<1024; i++)
             writer.write(buf);
         
         writer.close();
         tmp.deleteOnExit();
            
        
         //Write file
         fs.copyFromLocalFile(new Path("file://"+tmp.getAbsolutePath()), new Path("/mytestdir/testfile"));
         
         Set<Path> allPaths = fs.store.listDeepSubPaths(new Path("/mytestdir"));
 
         //Verify deep paths
         assertEquals(6, allPaths.size());
 
         //verify shallow path
         Set<Path> thisPath = fs.store.listSubPaths(new Path("/mytestdir"));
         assertEquals(4, thisPath.size());
         
         
         //Check file status
         FileStatus stat = fs.getFileStatus(new Path("/mytestdir/testfile"));
         
         assertEquals(1024*1024, stat.getLen());
         assertEquals(false, stat.isDir());
         
         //Check block info
         BlockLocation[] info = fs.getFileBlockLocations(stat, 0, stat.getLen());
         assertEquals(1, info.length);
        assertEquals(FBUtilities.getLocalAddress().getHostName()+":"+DatabaseDescriptor.getRpcPort(), info[0].getHosts()[0]);
         
         //Check dir status
         stat = fs.getFileStatus(new Path("/mytestdir"));
         assertEquals(true, stat.isDir());
 
         
         //Read back the file
         File out = File.createTempFile("testcfs", "output");
 
         fs.copyToLocalFile(new Path("/mytestdir/testfile"), new Path("file://"+out.getAbsolutePath()));
        
         out.deleteOnExit();
         
         Reader reader = new FileReader(out);
         for(int i=0; i<1024; i++)
         {
            assertEquals(1024, reader.read(buf));
         }
         
         assertEquals(-1,reader.read());      
     }
 
 }
