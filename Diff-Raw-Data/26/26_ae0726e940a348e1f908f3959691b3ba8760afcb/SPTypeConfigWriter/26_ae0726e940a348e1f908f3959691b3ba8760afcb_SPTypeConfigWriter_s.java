 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.picketlink.identity.federation.core.config.parser;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.picketlink.identity.federation.core.exceptions.ProcessingException;
 import org.picketlink.identity.federation.core.parsers.config.SAMLConfigParser;
 import org.picketlink.identity.federation.core.util.StaxUtil;
 
 /**
  * <p>
  * Class responsible for the creation of the picketlink-idfed.xml configuration file for Service Providers.
  * </p>
  * 
  * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
  * @since Mar 12, 2012
  */
 public class SPTypeConfigWriter implements ConfigWriter {
 
     private SPTypeSubsystem configuration;
 
     public SPTypeConfigWriter(SPTypeSubsystem SPTypeSubsystem) {
         this.configuration = SPTypeSubsystem;
     }
     
     public void write(File file) {
         XMLStreamWriter writer = null;
         
         try {
             writer = StaxUtil.getXMLStreamWriter(new FileOutputStream(file));
             
             StaxUtil.writeStartElement(writer, "", SAMLConfigParser.SP, "urn:picketlink:identity-federation:config:1.0");
             
             StaxUtil.writeStartElement(writer, "", SAMLConfigParser.SERVICE_URL, "");
            StaxUtil.writeCharacters(writer, this.configuration.getServiceURL());
             StaxUtil.writeEndElement(writer);
 
             StaxUtil.writeStartElement(writer, "", SAMLConfigParser.IDENTITY_URL, "");
            StaxUtil.writeCharacters(writer, this.configuration.getIdentityURL());
             StaxUtil.writeEndElement(writer);
 
             StaxUtil.writeEndElement(writer);
         } catch (ProcessingException e) {
             e.printStackTrace();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } finally {
             if (writer != null) {
                 try {
                     writer.flush();
                     writer.close();
                 } catch (XMLStreamException e) {
                     e.printStackTrace();
                 }
             }
         }        
     }
     
 }
