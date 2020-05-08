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
 /** MODIFIED FOR GPGPU Usage! **/
 
 package org.apache.hama.pipes;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.fs.FileUtil;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.util.StringUtils;
 import org.apache.hama.bsp.BSPPeer;
 import org.apache.hama.bsp.TaskAttemptID;
 import org.apache.hama.bsp.TaskLog;
 
 /**
  * This class is responsible for launching and communicating with the child
  * process.
  * 
  * Adapted from Hadoop Pipes
  * 
  * @author Martin Illecker
  * 
  */
 class Application<K1 extends Writable, V1 extends Writable, K2 extends Writable, V2 extends Writable, M extends Writable> {
 
   private static final Log LOG = LogFactory.getLog(Application.class.getName());
   private ServerSocket serverSocket;
   private Process process;
   private Socket clientSocket;
 
   private DownwardProtocol<K1, V1> downlink;
   // private BSPPeer<K1, V1, K2, V2, BytesWritable> peer;
 
   static final boolean WINDOWS = System.getProperty("os.name").startsWith(
       "Windows");
 
   /**
    * Start the child process to handle the task for us.
    * 
    * @param peer the current peer including the task's configuration
    * @throws InterruptedException
    * @throws IOException
    */
   Application(BSPPeer<K1, V1, K2, V2, BytesWritable> peer) throws IOException,
       InterruptedException {
 
     // this.peer = peer;
 
     serverSocket = new ServerSocket(0);
     Map<String, String> env = new HashMap<String, String>();
     // add TMPDIR environment variable with the value of java.io.tmpdir
     env.put("TMPDIR", System.getProperty("java.io.tmpdir"));
     env.put("hama.pipes.command.port",
         Integer.toString(serverSocket.getLocalPort()));
 
     /* Set Logging Environment from Configuration */
     env.put("hama.pipes.logging",
         peer.getConfiguration().getBoolean("hama.pipes.logging", false) ? "1"
             : "0");
     LOG.info("DEBUG hama.pipes.logging: "
         + peer.getConfiguration().getBoolean("hama.pipes.logging", false));
 
     List<String> cmd = new ArrayList<String>();
     String interpretor = peer.getConfiguration().get(
         "hama.pipes.executable.interpretor");
     if (interpretor != null) {
       cmd.add(interpretor);
     }
 
     // Check whether the applicaton will run on GPU and take right executable
     String executable = null;
     try {
       LOG.info("DEBUG LocalCacheFilesCount: "
           + DistributedCache.getLocalCacheFiles(peer.getConfiguration()).length);
       for (Path u : DistributedCache
           .getLocalCacheFiles(peer.getConfiguration()))
         LOG.info("DEBUG LocalCacheFiles: " + u);
 
       executable = DistributedCache.getLocalCacheFiles(peer.getConfiguration())[0]
           .toString();
 
       LOG.info("DEBUG: executable: " + executable);
 
     } catch (Exception e) {
       // if executable (GPU) missing?
       // LOG.info("ERROR: "
       // + ((Integer.parseInt(e.getMessage()) == 1) ? "GPU " : "CPU")
       // + " executable is missing!");
 
       LOG.error("Executable: " + executable + " fs.default.name: "
           + peer.getConfiguration().get("fs.default.name"));
 
       throw new IOException("Executable is missing!");
     }
 
     if (!new File(executable).canExecute()) {
       // LinuxTaskController sets +x permissions on all distcache files already.
       // In case of DefaultTaskController, set permissions here.
       FileUtil.chmod(executable, "u+x");
     }
     cmd.add(executable);
     // If runOnGPU add GPUDeviceId as parameter for GPUExecutable
 
     // if (runOnGPU)
     // cmd.add(executable + " " + GPUDeviceId);
     // cmd.add(Integer.toString(GPUDeviceId));
 
     // wrap the command in a stdout/stderr capture
     TaskAttemptID taskid = peer.getTaskId();
     // we are starting map/reduce task of the pipes job. this is not a cleanup
     // attempt.
     File stdout = TaskLog.getTaskLogFile(taskid, TaskLog.LogName.STDOUT);
     File stderr = TaskLog.getTaskLogFile(taskid, TaskLog.LogName.STDERR);
     // Get the desired maximum length of task's logs.
     long logLength = TaskLog.getTaskLogLength(peer.getConfiguration());
     cmd = TaskLog.captureOutAndError(null, cmd, stdout, stderr, logLength);
 
     if (!stdout.getParentFile().exists()) {
       stdout.getParentFile().mkdirs();
       LOG.info("STDOUT: " + stdout.getParentFile().getAbsolutePath()
           + " created!");
     }
     LOG.info("STDOUT: " + stdout.getAbsolutePath());
 
     if (!stderr.getParentFile().exists()) {
       stderr.getParentFile().mkdirs();
       LOG.info("STDERR: " + stderr.getParentFile().getAbsolutePath()
           + " created!");
     }
     LOG.info("STDERR: " + stderr.getAbsolutePath());
 
     /* MapReduce */
     /*
      * bash, -c, export JVM_PID=`echo $$` exec
      * '/tmp/hadoop-bafu/mapred/local/taskTracker/distcache/-7143918130315307681_1190230872_693782477/localhostbin/cpu-wordcount'
      * < /dev/null 1>>
      * /logs/userlogs/job_201208151013_0001/attempt_201208151013_0001_m_000000_0
      * /stdout 2>>
      * /logs/userlogs/job_201208151013_0001/attempt_201208151013_0001_m_000000_0
      * /stderr
      */
     LOG.info("DEBUG: cmd: " + cmd);
 
     process = runClient(cmd, env); // fork c++ binary
 
     LOG.info("DEBUG: waiting for Client at "
         + serverSocket.getLocalSocketAddress());
 
    try {
      serverSocket.setSoTimeout(2000);
      clientSocket = serverSocket.accept();

      downlink = new BinaryProtocol<K1, V1, K2, V2>(peer, clientSocket);
      downlink.start();
 
    } catch (SocketException e) {
      throw new SocketException(
          "Timout: Client pipes application was not connecting!");
    }
   }
 
   /**
    * Get the downward protocol object that can send commands down to the
    * application.
    * 
    * @return the downlink proxy
    */
   DownwardProtocol<K1, V1> getDownlink() {
     return downlink;
   }
 
   /**
    * Wait for the application to finish
    * 
    * @return did the application finish correctly?
    * @throws Throwable
    */
   boolean waitForFinish() throws Throwable {
     downlink.flush();
     return downlink.waitForFinish();
   }
 
   /**
    * Abort the application and wait for it to finish.
    * 
    * @param t the exception that signalled the problem
    * @throws IOException A wrapper around the exception that was passed in
    */
   void abort(Throwable t) throws IOException {
     LOG.info("Aborting because of " + StringUtils.stringifyException(t));
     try {
       downlink.abort();
       downlink.flush();
     } catch (IOException e) {
       // IGNORE cleanup problems
     }
     try {
       downlink.waitForFinish();
     } catch (Throwable ignored) {
       process.destroy();
     }
     IOException wrapper = new IOException("pipe child exception");
     wrapper.initCause(t);
     throw wrapper;
   }
 
   /**
    * Clean up the child procress and socket.
    * 
    * @throws IOException
    */
   void cleanup() throws IOException {
     serverSocket.close();
     try {
       downlink.close();
     } catch (InterruptedException ie) {
       Thread.currentThread().interrupt();
     }
   }
 
   /**
    * Run a given command in a subprocess, including threads to copy its stdout
    * and stderr to our stdout and stderr.
    * 
    * @param command the command and its arguments
    * @param env the environment to run the process in
    * @return a handle on the process
    * @throws IOException
    */
   static Process runClient(List<String> command, Map<String, String> env)
       throws IOException {
     ProcessBuilder builder = new ProcessBuilder(command);
     if (env != null) {
       builder.environment().putAll(env);
     }
     Process result = builder.start();
     return result;
   }
 
 }
