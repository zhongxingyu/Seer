 /*
  * Simplex, lightweight SimPEL server
  * Copyright (C) 2008-2009  Intalio, Inc.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.intalio.simplex.embed;
 
 import com.intalio.simpel.Descriptor;
 import org.apache.ode.bpel.evt.BpelEvent;
 import org.apache.ode.bpel.iapi.Endpoint;
 import org.apache.ode.bpel.iapi.EndpointReference;
 import org.apache.ode.bpel.iapi.ProcessConf;
 import org.apache.ode.bpel.iapi.ProcessState;
 import org.apache.ode.bpel.rapi.PartnerLinkModel;
 import org.apache.ode.bpel.rapi.ProcessModel;
 import org.apache.ode.bpel.rapi.Serializer;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import javax.wsdl.Definition;
 import javax.xml.namespace.QName;
 import java.io.*;
 import java.net.URI;
 import java.util.*;
 
 public class EmbeddedProcessConf implements ProcessConf {
     private static final String SIMPEL_ENDPOINT_NS = "http://ode.apache.org/simpel/1.0/endpoint";
 
     private ProcessModel _oprocess;
     private Descriptor _desc;
 
     public EmbeddedProcessConf(ProcessModel oprocess, Descriptor desc) {
         _oprocess = oprocess;
         _desc = desc;
     }
 
     public QName getProcessId() {
         return new QName(_oprocess.getQName().getNamespaceURI(),
                _oprocess.getQName().getLocalPart()+"-"+getVersion());
     }
 
     public QName getType() {
         return _oprocess.getQName();
     }
 
     public long getVersion() {
         // TODO implement versioning
         return 0;
     }
 
     public boolean isTransient() {
         return _desc.isTransient();
     }
 
     public boolean isRestful() {
         return getProvideEndpoints().size() == 0;
     }
 
     public InputStream getCBPInputStream() {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         Serializer fileHeader = new Serializer(System.currentTimeMillis());
         try {
             fileHeader.writePModel(_oprocess, baos);
         } catch (IOException e) {
             throw new RuntimeException("Failed to serialize compiled OProcess!", e);
         }
         return new ByteArrayInputStream(baos.toByteArray());
     }
 
     public ProcessModel getProcessModel() {
         return _oprocess;
     }
 
     public String getBpelDocument() {
         throw new UnsupportedOperationException();
     }
 
     public URI getBaseURI() {
         throw new UnsupportedOperationException();
     }
 
     public Date getDeployDate() {
         throw new UnsupportedOperationException();
     }
 
     public ProcessState getState() {
         return ProcessState.ACTIVE;
     }
 
     public List<File> getFiles() {
         throw new UnsupportedOperationException();
     }
 
     public String getPackage() {
         throw new UnsupportedOperationException();
     }
 
     public Definition getDefinitionForService(QName qName) {
         throw new UnsupportedOperationException();
     }
 
     public Map<String, Endpoint> getProvideEndpoints() {
         return defaultEndpoints(true);
     }
 
     public Map<String, Endpoint> getInvokeEndpoints() {
         return defaultEndpoints(false);
     }
 
     public boolean isEventEnabled(List<String> strings, BpelEvent.TYPE type) {
         return false;
     }
 
     private Map<String, Endpoint> defaultEndpoints(boolean myrole) {
         Map<String, Endpoint> res = new HashMap<String, Endpoint>();
         for (PartnerLinkModel partnerLink : _oprocess.getAllPartnerLinks()) {
             if (partnerLink.hasMyRole() && myrole || partnerLink.hasPartnerRole() && !myrole)
                 res.put(partnerLink.getName(), new Endpoint(
                         new QName(SIMPEL_ENDPOINT_NS, partnerLink.getName()), "SimPELPort"));
         }
         return res;
     }
 
     public Map<QName, Node> getProcessProperties() {
         throw new UnsupportedOperationException();
     }
 
     public List<Element> getExtensionElement(QName qName) {
         return new ArrayList<Element>();
     }
 
     public Map<String, String> getEndpointProperties(EndpointReference endpointReference) {
         throw new UnsupportedOperationException();
     }
 
     public boolean isSharedService(QName qName) {
         return false;
     }
 
     public int getRuntimeVersion() {
         return 2;
     }
 }
