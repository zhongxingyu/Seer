 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.trickl.crawler.robot.xslt;
 
 import com.trickl.crawler.api.Task;
 import com.trickl.crawler.handle.LinkExtractor;
 import com.trickl.crawler.handle.ObjectToSourceHandler;
 import com.trickl.crawler.handle.TaskResultHandler;
 import com.trickl.crawler.handle.XslTransformHandler;
 import com.trickl.crawler.robot.StandardDroid;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Collection;
 import javax.xml.transform.Source;
 import org.apache.droids.exception.DroidsException;
 
 /**
  * Extension of StandardDroid for common XSLT configuration
  * @author tgee
  * @param <T> 
  */
 public class XsltDroid<T extends Task> extends StandardDroid<T>
 {
    private XslTransformHandler<T> xslTransformHandler;
 
    private LinkExtractor<T> linkExtractor;
    
    public XsltDroid() 
    {
       super();
             
       xslTransformHandler = new XslTransformHandler<T>();
       ObjectToSourceHandler<T> objectToSourceHandler = new ObjectToSourceHandler<T>();
       objectToSourceHandler.setOutputHandler(xslTransformHandler);
       
       linkExtractor = new LinkExtractor<T>();
       outputHandlers.add(linkExtractor);
      outputHandlers.add(objectToSourceHandler);
    }   
 
    public void setXsltFile(String file) throws DroidsException
    {
       xslTransformHandler.setClassLoaderObject(this);
       xslTransformHandler.setXslTemplate(file);
    }
 
    public void setOutputHandler(TaskResultHandler<T, Source> outputHandler)
    {
       xslTransformHandler.setOutputHandler(outputHandler);
    }
 
    public void setLinkHandler(TaskResultHandler<T, Collection<URI> > linkHandler)
    {
       this.linkExtractor.setOutputHandler(linkHandler);
    }
 
    public void setRegexURLFile(String file) throws IOException
    {
       linkExtractor.setRegexURLFile(file);
    }   
 }
