 /*
  * The MIT License
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package hudson.plugins.hadoop;
 
 import hudson.Extension;
 import hudson.model.Computer;
 import hudson.model.TaskListener;
 import hudson.model.Hudson;
 import hudson.slaves.ComputerListener;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 /**
  * When a new computer becomes online, starts a Hadoop data node and task tracker.
  *
  * <p>
  * This will be done on a separate JVM to allow administrators to control the JVM parameters better.
  * This JVM automatically kills itself when the slave JVM gets disconnected.
  *
  * @author Kohsuke Kawaguchi
  */
 @Extension
 public class ComputerListenerImpl extends ComputerListener {
     @Override
     public void onOnline(Computer c, TaskListener listener) {
         try {
             if(c==Hudson.getInstance().toComputer())
                return;   // this happens before the master is started.
 
             PluginImpl p = PluginImpl.get();
             String hdfsUrl = p.getHdfsUrl();
             if(hdfsUrl !=null) {
                 String address = c.getHostName();
                 if(address==null)
                     listener.getLogger().println("Unable to determine the hostname/IP address of this system. Skipping Hadoop deployment");
                 else
                     c.getChannel().call(new SlaveStartTask(c, listener, hdfsUrl, address));
             }
         } catch (IOException e) {
             e.printStackTrace(listener.error("Failed to start Hadoop"));
         } catch (InterruptedException e) {
             e.printStackTrace(listener.error("Failed to start Hadoop"));
         }
     }
 
     private static final Logger LOGGER = Logger.getLogger(ComputerListenerImpl.class.getName());
 }
