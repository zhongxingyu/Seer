 //
 // A Java VM status Watcher for Fluent
 //
 // Copyright (C) 2013 - 2013 Masayuki Miyake
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package org.fluentd.jvmwatcher;
 
 import java.util.concurrent.BlockingQueue;
 
 import org.fluentd.jvmwatcher.data.JvmStateLog;
 import org.fluentd.jvmwatcher.data.JvmWatchState;
 import org.fluentd.jvmwatcher.data.JvmWatchState.ProcessState;
 import org.fluentd.jvmwatcher.proxy.JvmClientProxy;
 
 /**
  * @author miyake
  *
  */
 public class JvmWatchThread implements Runnable
 {
     
     private     BlockingQueue<JvmWatchState>    queue_ = null;
     private     JvmClientProxy                  jvmClient_ = null;
     
     private     long        watchInterval_ = 1000L; // msec
     private     int         logBuffNum_ = 1;
     
     private     JvmWatchState   watchState_ = null;
     private     JvmWatcher      parent_ = null;
 
     
     private JvmWatchThread()
     {
         
     }
     
     public JvmWatchThread(JvmWatcher parent)
     {
         this.parent_ = parent;
         
         // set param
     }
     
     /* (非 Javadoc)
      * @see java.lang.Runnable#run()
      */
     @Override
     public void run()
     {
         boolean     isProcess = true;
         int         logBuffCnt = 0;
         
         // JVM watch start
         this.watchState_ = JvmWatchState.makeJvmWatchState(this.jvmClient_);
         if (null == this.watchState_)
         {
             return ;
         }
         // set start flag
         this.watchState_.setProcState(ProcessState.START_PROCESS);
         
         while (isProcess)
         {
             long    startTime = System.currentTimeMillis();
             JvmStateLog     stateLog = JvmStateLog.makeJvmStateLog(this.jvmClient_);
             // disconnect
             if (this.jvmClient_.isConnect() == false)
             {
                 // set end flag
                 this.watchState_.setProcState(ProcessState.END_PROCESS);
                 isProcess = false;
             }
             else
             {
                 // add JvmStateLog
                 this.watchState_.addStateLog(stateLog);
             }
             
             // parse JSON & output stream
             if ((logBuffCnt >= logBuffNum_) || (isProcess == false))
             {
                 // call parse 
                 // BlockingQueueでパーサスレッドに処理させるかどうかが悩みどころ
                 logBuffCnt = 0;
             }
             
             logBuffCnt++;
             // calc wait time
             long    procTime = System.currentTimeMillis() - startTime;
            long    waitTime = watchInterval_ - procTime;
             
             this.watchState_.setProcState(ProcessState.LIVE_PROCESS);
             if (waitTime > 0)
             {
                 try
                 {
                     Thread.sleep(waitTime);
                 }
                 catch (InterruptedException ex)
                 {
                     System.err.println(ex.toString());
                 }
             }
         }
     }
 
 }
