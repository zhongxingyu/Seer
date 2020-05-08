 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2013, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 25th March 2013
  */
 package au.edu.uts.eng.remotelabs.schedserver.ands.impl;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Collection;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ProjectMetadata;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.SessionFile;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 
 /**
  * Generates and stores a metadata ingest file.
  */
 public class RedboxIngestFile
 {
     /** Logger. */
     private final Logger logger;
     
     public RedboxIngestFile()
     {
         this.logger = LoggerActivator.getLogger(); 
     }
     
     /**
      * Generates and stores a RedBox ingest file in the specified location.
      * 
      * @param collection collection to generate ingest file from
      * @param location location to store ingest file
      * @return true if successfully generated and stored
      */
     public boolean generateAndStore(Collection collection, String location)
     {
         Document doc;
         FileWriter fileWriter = null;
         
         try
         {
             DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             doc = docBuilder.newDocument();
             
             Element root = doc.createElement("rdcingest");
             doc.appendChild(root);
             
             /* Activity identifier. */
             Element e = doc.createElement("activity");
             e.setTextContent(collection.getProject().getActivity());
             root.appendChild(e);
             
             /* Service identifier. */
             Session sessions[] = collection.getSessions().toArray(new Session[collection.getSessions().size()]);
             if (sessions.length == 0)
             {
                 this.logger.warn("Failed to generate session because the are no sessions attached to the collection.");
                 return false;
             }
             e = doc.createElement("service");
             e.setTextContent(sessions[0].getRig().getMeta());
             root.appendChild(e);
             
             /* Creator. */
             e = doc.createElement("creator");
             e.setTextContent(collection.getProject().getUser().getResearchIdentifier());
             root.appendChild(e);
             
             /* Collection date. */
             Date start = null;
             for (Session s : sessions) if (start == null || start.after(s.getAssignmentTime())) start = s.getAssignmentTime();
             e = doc.createElement("collectiondate");
             
             /* Other metadata. */
             for (ProjectMetadata metadata : collection.getProject().getMetadata())
             {
                 e = doc.createElement(metadata.getType().getName());
                 e.setTextContent(metadata.getValue());
                 root.appendChild(e);
             }
             
             /* Data fields. */
             e = doc.createElement("share");
             e.setTextContent(collection.getProject().isShared() ? "Y" : "N");
             root.appendChild(e);
             
             e = doc.createElement("access");
             e.setTextContent(collection.getProject().isOpen() ? "open access" : "contact researcher");
             root.appendChild(e);
             
             /* Files. */
             for (Session session : sessions)
             {
                 for (SessionFile file : session.getFiles())
                 {
                     e = doc.createElement("location");
                     e.setTextContent("file://" + file.getPath() + "/" + file.getName());
                     root.appendChild(e);
                 }
             }
 
             File file = new File(location);
             if (!(file.exists() && file.isDirectory()))
             {
                 this.logger.error("Failed to generate Redbox metadata ingest file because the specified location does " +
                 		"not exist or is not a directory.");
                 return false;
             }
            fileWriter = new FileWriter(file);
             
             /* Output the data. */
             Transformer trans = TransformerFactory.newInstance().newTransformer();
             trans.setParameter(OutputKeys.INDENT, "yes");
             trans.transform(new DOMSource(doc), new StreamResult(fileWriter));
             
             return true;
         }
         catch (ParserConfigurationException e)
         {
             this.logger.error("Failed to generate Redbox metadata ingest file because a valid DOM implmentation does " +
                     "not exist. This should be built in to Java. Please check your Java installation.");
         }
         catch (TransformerConfigurationException e)
         {
             this.logger.error("Failed to generated Redbox metadata ingest file because of a Java XML configuration " +
             		"error. Please check your Java installation.");
         }
         catch (TransformerFactoryConfigurationError e)
         {
             this.logger.error("Failed to generated Redbox metadata ingest file because of a Java XML configuration " +
             		"error. Please check your Java installation.");
         }
         catch (TransformerException e)
         {
             this.logger.warn("Failed to generated Redbox metadata ingest file with error: " + e.getMessage());
         }
         catch (IOException e)
         {
             this.logger.warn("Failed to write Redbox metadata ingest file with error: " + e.getMessage());
         }
         finally
         {
             try
             {
                 if (fileWriter != null) fileWriter.close();
             }
             catch (IOException ex)
             {
                 this.logger.warn("Failed to close Redbox metadata ingest gile with error: " + ex.getMessage());
             }
         }
         
         return false;
     }
 }
