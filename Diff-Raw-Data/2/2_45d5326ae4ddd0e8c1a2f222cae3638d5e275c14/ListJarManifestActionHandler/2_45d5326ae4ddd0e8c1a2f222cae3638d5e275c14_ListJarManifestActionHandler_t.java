 /*******************************************************************************
  * Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *    Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
  *    may be used to endorse or promote products derived from this software without
  *    specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package org.cyclades.nyxlet.admin.actionhandler;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.Attributes;
 import javax.xml.stream.XMLStreamWriter;
 import org.cyclades.annotations.AHandler;
 import org.cyclades.engine.NyxletSession;
 import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
 import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ActionHandler;
 import org.cyclades.engine.stroma.STROMAResponseWriter;
 import org.cyclades.engine.validator.ParameterHasValue;
 import org.cyclades.io.Jar;
 import org.cyclades.nyxlet.admin.util.Auth;
 
 @AHandler({"listjarmanifest"})
 public class ListJarManifestActionHandler extends ActionHandler {
 
     public ListJarManifestActionHandler (STROMANyxlet parentNyxlet) {
         super(parentNyxlet);
     }
 
     @Override
     public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, 
             STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "ListJarManifestActionHandler.handle: ";
         try {
             /********************************************************************/
             /*******                  START CODE BLOCK                    *******/
             /*******                                                      *******/
             /******* YOUR CODE GOES HERE...WITHIN THESE COMMENT BLOCKS.   *******/
             /******* MODIFYING ANYTHING OUTSIDE OF THESE BLOCKS WITHIN    *******/
             /******* THIS METHOD MAY EFFECT THE STROMA COMPATIBILITY      *******/
             /******* OF THIS ACTION HANDLER.                              *******/
             /********************************************************************/
             List<ManifestBundle> propertyBundles = new ArrayList<ManifestBundle>();
             for (String propertiesURI : baseParameters.get(JAR_RESOURCE_PARAMETER)) {
                 propertyBundles.add(new ManifestBundle(propertiesURI, Jar.getJarManifestMainAttributes(propertiesURI, JAR_MANIFEST)));
             }
             XMLStreamWriter streamWriter = stromaResponseWriter.getXMLStreamWriter();
             for (ManifestBundle propertyBundle : propertyBundles) {
                 streamWriter.writeStartElement("attributes");
                 streamWriter.writeAttribute("source", propertyBundle.uri);
                     for (java.util.Map.Entry<Object, Object> attributeEntry : propertyBundle.attributes.entrySet()) {
                         streamWriter.writeStartElement("attribute");
                         streamWriter.writeAttribute("key", attributeEntry.getKey().toString());
                         streamWriter.writeAttribute("value", attributeEntry.getValue().toString());
                         streamWriter.writeEndElement();
                     }
                 streamWriter.writeEndElement();
             }
             /********************************************************************/
             /*******                  END CODE BLOCK                      *******/
             /********************************************************************/
         } catch (Exception e) {
             getParentNyxlet().logStackTrace(e);
             handleException(nyxletSession, stromaResponseWriter, eLabel, e);
         } finally {
             stromaResponseWriter.done();
         }
     }
     
     private static class ManifestBundle {
         ManifestBundle (String uri, Attributes attributes) {
             this.uri = uri;
             this.attributes = attributes;
         }
         public final String uri;
         public final Attributes attributes;
     }
 
     /**
      * Return a valid health check status. This one simply returns true, which
      * will always flag a healthy ActionHandler...more meaningful algorithms
      * can be used.
      *
      * @return true means this is a healthy ActionHandler
      * @throws Exception
      */
     @Override
     public boolean isHealthy () throws Exception {
         return true;
     }
 
     @Override
     public void init () throws Exception {
         getFieldValidators().add(new ParameterHasValue(JAR_RESOURCE_PARAMETER));
         Auth.addPasswordValidation(this);
     }
 
     @Override
     public void destroy () throws Exception {
         // your destruction code here, if any
     }
     
     private static final String JAR_RESOURCE_PARAMETER = "jar";
     private static final String JAR_MANIFEST = "META-INF/MANIFEST.MF";
 
 }
