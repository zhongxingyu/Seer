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
 package com.trickl.crawler.handle;
 
 import com.trickl.crawler.api.Task;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import org.apache.droids.exception.DroidsException;
 import org.w3c.dom.Document;
 
 public class XslTransformHandler<T extends Task> implements TaskResultHandler<T, Source>
 {
    private static final Logger logger = Logger.getLogger(XslTransformHandler.class.getCanonicalName());
 
    private Transformer xslTransformer;
 
    private TaskResultHandler<T, Source> outputHandler;
 
    private Object classLoaderObject = this;
 
    private DocumentBuilder documentBuilder;
 
    private Transformer transformer;
 
    public XslTransformHandler() 
    {
       try 
       {
          DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
          documentBuilder = documentFactory.newDocumentBuilder();
 
          transformer = TransformerFactory.newInstance().newTransformer();
          transformer.setOutputProperty(OutputKeys.METHOD, "xml");
       }
       catch (ParserConfigurationException ex)
       {
          logger.log(Level.SEVERE, "Failed to instantiate transform.", ex);
       }
       catch (TransformerConfigurationException ex)
       {
          logger.log(Level.SEVERE, "Failed to instantiate XML transform", ex);
       }
    }
 
    @Override
    public void handle(T task, Source source) throws DroidsException, IOException
    {
       if (task == null || source == null) throw new NullPointerException();
       
       if (xslTransformer != null && outputHandler != null)
       {     
          // Transform XML
          try
          {            
             Document transformedDocument = documentBuilder.newDocument();
             xslTransformer.transform(source, new DOMResult(transformedDocument));
             
             if (logger.isLoggable(Level.FINEST)) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                xslTransformer.transform(source, new StreamResult(stream));
                logger.log(Level.FINEST, "Transform output:\n{0}", stream.toString());
             }
 
             if (transformedDocument.getDocumentElement() == null)
             {
                logger.log(Level.INFO, "Task ''{0}'' XSL transform produced an empty XML document.", task.getId());
             }
             else
             {               
                outputHandler.handle(task, new DOMSource(transformedDocument));
             }
          }
          catch (TransformerException e)
          {
             throw new DroidsException("Failed to transform XML", e);
          }
       }
    }
 
    public TaskResultHandler<T, Source> getOutputHandler()
    {
       return outputHandler;
    }
   
    public void setOutputHandler(TaskResultHandler<T, Source> outputHandler)
    {
      this.outputHandler = outputHandler;
    }
 
    public void setClassLoaderObject(Object obj)
    {
       this.classLoaderObject = obj;
    }
 
    public void setXslTemplate(String file) throws DroidsException
    {
       if (file == null) throw new NullPointerException();
 
       URL url = null;
       if (file.startsWith("classpath:/"))
       {
          url = classLoaderObject.getClass().getClassLoader().getResource(
             file.substring("classpath:/".length()));
       }
       else
       {
          try
          {
             url = (file.contains(":") ? new URL(file) : new URL("file://" + file));
          }
          catch (MalformedURLException e)
          {
             throw new DroidsException("URL is invalid", e);
          }
       }
 
       TransformerFactory xslTransformerFactory = TransformerFactory.newInstance();
       try
       {
          xslTransformer = xslTransformerFactory.newTransformer(new StreamSource(url.openStream()));
       }
       catch (IOException e)
       {
          throw new DroidsException("Failed to open XSL template", e);
       }
       catch (TransformerConfigurationException e)
       {
          throw new DroidsException("Failed to parse XSL template", e);
       }
    }
 }
