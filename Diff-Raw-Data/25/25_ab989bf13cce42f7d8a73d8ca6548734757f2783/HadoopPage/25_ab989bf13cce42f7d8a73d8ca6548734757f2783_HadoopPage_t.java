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
 
 import hudson.model.AbstractModelObject;
 import hudson.model.Action;
 import hudson.model.Hudson;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * Top-level Hadoop page that gets added to Hudson.
  *
  * @author Kohsuke Kawaguchi
  */
 public class HadoopPage extends AbstractModelObject implements Action {
    /*package*/ boolean pendingConfiguration;

     public String getDisplayName() {
         return "Hadoop";
     }
 
     public String getSearchUrl() {
         return "hadoop";
     }
 
     public String getIconFileName() {
         return "/plugin/hadoop/24x24/hadoop.png";
     }
 
     public String getUrlName() {
         return "/hadoop";
     }
 
     /**
     * Returns true if we haven't started Hadoop because the root URL is unknown.
     */
    public boolean isPendingConfiguration() {
        return pendingConfiguration;
    }

    /**
      * Returns the URL of the HDFS HTTP interface.
      */
     public URL getHdfsHttpURL() throws MalformedURLException {
         URL url = new URL(Hudson.getInstance().getRootUrl());
         return new URL("http://"+url.getHost()+":"+NameNodeStartTask.HTTP_PORT+"/");
     }
 
     /**
      * Returns the URL of the job tracker HTTP interface.
      */
     public URL getJobTrackerURL() throws MalformedURLException {
         URL url = new URL(Hudson.getInstance().getRootUrl());
         return new URL("http://"+url.getHost()+":"+JobTrackerStartTask.HTTP_PORT+"/");
     }
 
     public PluginImpl getPlugin() {
         return PluginImpl.get();
     }
 }
