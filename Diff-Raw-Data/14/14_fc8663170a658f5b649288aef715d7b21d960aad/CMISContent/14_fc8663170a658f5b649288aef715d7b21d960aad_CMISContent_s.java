 /**************************************************************
  * 
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  * 
  *************************************************************/
 package org.apache.aoo.cmisucp.unobojects;
 
 import com.sun.star.beans.Property;
 import com.sun.star.beans.PropertyAttribute;
 import com.sun.star.beans.PropertyChangeEvent;
 import com.sun.star.beans.PropertyValue;
 import com.sun.star.beans.UnknownPropertyException;
 import com.sun.star.beans.XPropertiesChangeListener;
 import com.sun.star.beans.XPropertySetInfo;
 import com.sun.star.io.BufferSizeExceededException;
 import com.sun.star.io.IOException;
 import com.sun.star.io.NotConnectedException;
 import com.sun.star.io.XActiveDataSink;
 import com.sun.star.io.XActiveDataStreamer;
 import com.sun.star.io.XDataOutputStream;
 import com.sun.star.io.XInputStream;
 import com.sun.star.io.XOutputStream;
 import com.sun.star.io.XStream;
 import com.sun.star.lang.IllegalArgumentException;
 import com.sun.star.uno.XComponentContext;
 import com.sun.star.lib.uno.helper.Factory;
 import com.sun.star.lang.XSingleComponentFactory;
 import com.sun.star.registry.XRegistryKey;
 import com.sun.star.lib.uno.helper.ComponentBase;
 import com.sun.star.sdbc.XRow;
 import com.sun.star.task.XInteractionHandler;
 import com.sun.star.ucb.ContentAction;
 import com.sun.star.ucb.ContentCreationException;
 import com.sun.star.ucb.ContentEvent;
 import com.sun.star.ucb.ContentInfo;
 import com.sun.star.ucb.ContentInfoAttribute;
 import com.sun.star.ucb.InsertCommandArgument;
 import com.sun.star.ucb.InteractiveBadTransferURLException;
 import com.sun.star.ucb.OpenCommandArgument2;
 import com.sun.star.ucb.OpenMode;
 import com.sun.star.ucb.PropertyValueState;
 import com.sun.star.ucb.TransferInfo;
 import com.sun.star.ucb.UnsupportedCommandException;
 import com.sun.star.ucb.XCommandInfo;
 import com.sun.star.ucb.XContent;
 import com.sun.star.ucb.XContentEventListener;
 import com.sun.star.ucb.XContentIdentifier;
 import com.sun.star.ucb.XDynamicResultSet;
 import com.sun.star.uno.Any;
 import com.sun.star.uno.AnyConverter;
 import com.sun.star.uno.Type;
 import com.sun.star.uno.UnoRuntime;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.aoo.cmisucp.CMISConstants;
 import org.apache.aoo.cmisucp.cmis.CMISConnect;
 import org.apache.aoo.cmisucp.cmis.CMISResourceManager;
 import org.apache.aoo.cmisucp.helper.ContentPropertySet;
 import org.apache.aoo.cmisucp.helper.PropertyAndValueSet;
 import org.apache.chemistry.opencmis.client.api.CmisObject;
 import org.apache.chemistry.opencmis.client.api.Folder;
 import org.apache.chemistry.opencmis.client.api.ObjectType;
 import org.apache.chemistry.opencmis.client.api.OperationContext;
 import org.apache.chemistry.opencmis.client.api.Session;
 import org.apache.chemistry.opencmis.client.util.FileUtils;
 import org.apache.chemistry.opencmis.commons.PropertyIds;
 import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
 import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
 import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
 import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
 import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
 
 public final class CMISContent extends ComponentBase
         implements com.sun.star.beans.XPropertySetInfoChangeNotifier,
         com.sun.star.beans.XPropertiesChangeNotifier,
         com.sun.star.container.XChild,
         com.sun.star.ucb.XContent,
         com.sun.star.ucb.XCommandInfoChangeNotifier,
         com.sun.star.ucb.XCommandProcessor,
         com.sun.star.lang.XServiceInfo,
         com.sun.star.ucb.XCommandProcessor2,
         com.sun.star.ucb.XContentCreator,
         com.sun.star.beans.XPropertyContainer {
 
     private final XComponentContext m_xContext;
     private static final String m_implementationName = CMISContent.class.getName();
     private static final String[] m_serviceNames = {
         "com.sun.star.ucb.Content"};
     //My variables
     private XContentIdentifier xContentid;
     private CmisObject cmisContent;
     private Session connected_session;
     private String contentType;
     private static final Logger log = Logger.getLogger(CMISConnect.class.getName());
     private boolean is_folder;
     private String id;
 
     private boolean exists;     //Whether exists on CMIS repository
     private boolean created;    // whether created using createNewContent
     private boolean nameSet;    // whether name set
     private boolean inserted;   // whether inserted
     
     private String path;
     CMISResourceManager resourceManager;
 
     private List<XContentEventListener> contentEventListeners = new ArrayList<XContentEventListener>();    
     private Map<XPropertiesChangeListener,List<String>> propertiesChangeListeners = new HashMap<XPropertiesChangeListener,List<String>>();
     
     public CMISContent(XComponentContext context, XContentIdentifier xContentIdentifier) throws ContentCreationException  {
                 
         m_xContext = context;
         xContentid = xContentIdentifier;        
 
         processIdentifier(xContentIdentifier.getContentIdentifier());
         path = xContentIdentifier.getContentIdentifier();
     
         created = false;
         nameSet = false;
         inserted = false;
         
     }
 
     public CMISContent(XComponentContext context, String type, String uri) throws ContentCreationException
     {        
         m_xContext = context;
         contentType = type;
         
         if(type.equalsIgnoreCase("application/cmis-document"))
             is_folder = false;
         else
             is_folder = true;
         
         processIdentifier(uri);
         exists = false;        
         created = true;
         inserted = false;
         nameSet = false;
         
         path = uri;
     }
     
     //My method
     public void processIdentifier(String uri) throws ContentCreationException {
 
         uri = uri.substring(6);        
         
         log.log(Level.INFO, "URI:{0}", uri);
 
         CMISConnect aConnect;
 
         try
         {            
             aConnect = new CMISConnect(m_xContext, xContentid.getContentIdentifier(), "rajaths589", "*****");
             connected_session = aConnect.getSession();
             cmisContent = aConnect.getObject();
             resourceManager = new CMISResourceManager(m_xContext, aConnect);
             
         }
         catch(CmisBaseException e)
         {
             exists = false;            
             if(e.getExceptionName().equalsIgnoreCase(CmisConnectionException.EXCEPTION_NAME))
             {
                 // To-Do . connected_session - not connected;
             }
             else if(e.getExceptionName().equalsIgnoreCase(CmisObjectNotFoundException.EXCEPTION_NAME))
             {
                 
                     //throw unknown path exception                
                 
             }
             else
             {
                 // throw some expception
             }
             throw new ContentCreationException();  
         }
         exists = true;
         setContentType();
         // cache propertiess        
     }
 
     private void setContentType()
     {
         if(!exists)
             return;
                 
         contentType = resourceManager.getContentType();        
         id = resourceManager.getID();
         is_folder = resourceManager.isFolder();
     }
     
     public boolean isFolder() {
         return is_folder;
     }
 
     public static XSingleComponentFactory __getComponentFactory(String sImplementationName) {
         XSingleComponentFactory xFactory = null;
 
         if (sImplementationName.equals(m_implementationName)) {
             xFactory = Factory.createComponentFactory(CMISContent.class, m_serviceNames);
         }
         return xFactory;
     }
 
     public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey) {
         return Factory.writeRegistryServiceInfo(m_implementationName,
                 m_serviceNames,
                 xRegistryKey);
     }
 
     // com.sun.star.beans.XPropertySetInfoChangeNotifier:
     public void addPropertySetInfoChangeListener(com.sun.star.beans.XPropertySetInfoChangeListener Listener) {
         // TODO: Insert your implementation for "addPropertySetInfoChangeListener" here.       
         throw new UnsupportedOperationException();
     }
 
     public void removePropertySetInfoChangeListener(com.sun.star.beans.XPropertySetInfoChangeListener Listener) {
         // TODO: Insert your implementation for "removePropertySetInfoChangeListener" here.
         throw new UnsupportedOperationException();
     }
 
     // com.sun.star.beans.XPropertiesChangeNotifier:
     public void addPropertiesChangeListener(String[] PropertyNames, com.sun.star.beans.XPropertiesChangeListener Listener) {
         propertiesChangeListeners.put(Listener, Arrays.asList(PropertyNames));
     }
 
     public void removePropertiesChangeListener(String[] PropertyNames, com.sun.star.beans.XPropertiesChangeListener Listener) {
         propertiesChangeListeners.remove(Listener);
     }
 
     // com.sun.star.container.XChild:
     public Object getParent() {
         //To-do
         return null;
     }
 
     public void setParent(Object Parent) throws com.sun.star.lang.NoSupportException {
         // TODO: Insert your implementation for "setParent" here.
     }
 
     // com.sun.star.ucb.XContent:
     public com.sun.star.ucb.XContentIdentifier getIdentifier() {
         return xContentid;
     }
 
     public String getContentType() {
         //To-Do
         return contentType;
     }
 
     private void contentListenerNotifier(ContentEvent event)
     {
         for(XContentEventListener eventListener:contentEventListeners)
             eventListener.contentEvent(event);
     }
     public void addContentEventListener(com.sun.star.ucb.XContentEventListener Listener) {
         contentEventListeners.add(Listener);
     }
 
     public void removeContentEventListener(com.sun.star.ucb.XContentEventListener Listener) {
         contentEventListeners.remove(Listener);
     }
 
     // com.sun.star.ucb.XCommandInfoChangeNotifier:
     public void addCommandInfoChangeListener(com.sun.star.ucb.XCommandInfoChangeListener Listener) {
         // TODO: Insert your implementation for "addCommandInfoChangeListener" here.
     }
 
     public void removeCommandInfoChangeListener(com.sun.star.ucb.XCommandInfoChangeListener Listener) {
         // TODO: Insert your implementation for "removeCommandInfoChangeListener" here.
     }
 
     // com.sun.star.ucb.XCommandProcessor:
     public int createCommandIdentifier() {
         // TODO: Exchange the default return implementation for "createCommandIdentifier" !!!
         // NOTE: Default initialized polymorphic structs can cause problems
         // because of missing default initialization of primitive types of
         // some C++ compilers or different Any initialization in Java and C++
         // polymorphic structs.
         return 0;
     }
 
     public Object execute(com.sun.star.ucb.Command aCommand, int CommandId, com.sun.star.ucb.XCommandEnvironment Environment) throws com.sun.star.uno.Exception, com.sun.star.ucb.CommandAbortedException, NotConnectedException, IOException, InteractiveBadTransferURLException {
         //XInteractionHandler xInteractionHandler = Environment.getInteractionHandler();     
         
         if (aCommand.Name.equalsIgnoreCase("getCommandInfo")) 
         {
             XCommandInfo xRet = new CMISCommandInfo(m_xContext);
             return xRet;
         }
         else if (aCommand.Name.equalsIgnoreCase("getPropertySetInfo")) 
         {
             XPropertySetInfo xRet = new CMISPropertySetInfo(m_xContext);
             return xRet;
         }
         else if (aCommand.Name.equalsIgnoreCase("getPropertyValues")) 
         {            
             Property[] rProperties;
             rProperties = (Property[]) AnyConverter.toArray(aCommand.Argument);                        
             return getPropertyValues(rProperties);
         }
         else if (aCommand.Name.equalsIgnoreCase("setPropertyValues")) 
         {
             PropertyValue[] pValues;
             try{
             pValues = (PropertyValue[]) AnyConverter.toArray(aCommand.Argument);
             }catch(Exception e){
                 throw new IllegalArgumentException("Incompatible argument");
             }                
             log.info("setPropertyValues()");
             return setPropertyValues(pValues);
         }else if (aCommand.Name.equalsIgnoreCase("Open")) 
         {
             log.info("open()");
 
             OpenCommandArgument2 openArg;
             try{
                 openArg = (OpenCommandArgument2) AnyConverter.toObject(OpenCommandArgument2.class, aCommand.Argument);
             }catch(Exception e){
                 throw new IllegalArgumentException("Unsupported argument");
             }
             
             if (isFolder()) {
                 if ((openArg.Mode == OpenMode.ALL) || (openArg.Mode == OpenMode.DOCUMENTS) || (openArg.Mode == OpenMode.FOLDERS)) {                    
                     return openFolder(openArg);
                     
                 }
                 else
                 {
                     throw new IllegalArgumentException("Document Modes not supported");
                 }
             }
             else
             {
                 if(openArg.Mode == OpenMode.DOCUMENT)
                 {                    
                     final XInputStream xInp  = resourceManager.getInputStream();
                     XActiveDataSink xDataSink = UnoRuntime.queryInterface(XActiveDataSink.class,openArg.Sink);
                     if(xDataSink!=null)
                         xDataSink.setInputStream(xInp);
                     else
                     {
                         XActiveDataStreamer xDataStream = UnoRuntime.queryInterface(XActiveDataStreamer.class, openArg.Sink);                                
                         if(xDataStream!=null)
                         {                                                         
                             XStream xStream = new XStream() {                                
                                 public XInputStream getInputStream() {
                                     return xInp;
                                 }
 
                                 public XOutputStream getOutputStream() {
                                     return null;
                                 }
                             };                            
                             xDataStream.setStream(xStream);
                         }
                         else
                         {
                             XOutputStream xOp = UnoRuntime.queryInterface(XOutputStream.class, openArg.Sink);
                             if(xOp!=null)
                             {
                                 copyStream(xOp,xInp);
                             }
                             else
                             {
                                 throw new IllegalArgumentException("Not xoutputstream/xactivedatasink/xactivedatastreamer");
                             }
                         }
                     }
                 }
                 else if(openArg.Mode == OpenMode.DOCUMENT_SHARE_DENY_NONE || openArg.Mode == OpenMode.DOCUMENT_SHARE_DENY_WRITE)
                     throw new UnsupportedCommandException();
                 else
                     throw new IllegalArgumentException("Modes not supported for this content");
                                 
             }        
         }
         else if(aCommand.Name.equalsIgnoreCase("CreatableContentsInfo"))
         {            
             return queryCreatableContentsInfo();
         }
         else if(aCommand.Name.equalsIgnoreCase("CreateNewContent"))
         {
             ContentInfo info;
             try{
                 info = (ContentInfo) AnyConverter.toObject(ContentInfo.class, aCommand.Argument);
             }catch(Exception e){
                 throw new IllegalArgumentException("Incompatible argument");
             }
             return createNewContent(info);
         }
         else if(aCommand.Name.equalsIgnoreCase("Insert"))
         {
             InsertCommandArgument insertArg;
             try{
                 insertArg = (InsertCommandArgument) AnyConverter.toObject(InsertCommandArgument.class, aCommand.Argument);
             }catch(Exception e){
                 throw new IllegalArgumentException("incompatible argument");
             }                
             try {
                 return insert(insertArg);
             } catch (java.io.IOException ex) {
                 throw new IllegalArgumentException("IO stream failure. Illegal datasink");
             }
         }
         else if(aCommand.Name.equalsIgnoreCase("transfer"))
         {
             TransferInfo aTransInfo;
             try{
                 aTransInfo = (TransferInfo) AnyConverter.toObject(TransferInfo.class, aCommand.Argument);            
             }catch(Exception e){
                 throw new IllegalArgumentException("Incompatible Transfer Argumnet");
             }
             
             CMISConnect transferConnect;
             try{
             transferConnect = new CMISConnect(m_xContext,aTransInfo.SourceURL, "rajaths589", "*****");
             }catch(CmisBaseException e)
             {
                 throw new IllegalArgumentException("Wrong repository data. Probably usernam,password/soruceURL is wrong");
             }
             //Name Clash not implemented.
             try 
             {
                 if(aTransInfo.NewTitle!=null)
                     resourceManager.transfer(transferConnect, aTransInfo.NewTitle);
                 else
                     resourceManager.transfer(transferConnect,transferConnect.getObject().getName());
             }    
             catch (java.io.IOException ex) 
             {
                 throw new IllegalArgumentException();
             }
             if(aTransInfo.MoveData==true)
             {                
                 CMISResourceManager transferRes = new CMISResourceManager(m_xContext, transferConnect.getObject(), transferConnect.getSession());
                 transferRes.delete();
             }            
         }
         else if(aCommand.Name.equalsIgnoreCase("delete"))
         {
             ContentEvent arg = new ContentEvent();
             arg.Action = ContentAction.DELETED;
             arg.Content = this;
             arg.Id = xContentid;            
             
             resourceManager.delete();
             ContentEvent arg1 = new ContentEvent();
             arg1.Action = ContentAction.REMOVED;
             arg1.Content = this;
             arg1.Id = xContentid; 
             if(getIdentifier().getContentProviderScheme().equals("cmis"))
             {                
                 XContentIdentifier xIdParent = new CMISContentIdentifier(m_xContext, getIdentifier().getContentIdentifier());
                 CMISContent xParent = new CMISContent(m_xContext, xIdParent);
                 xParent.contentListenerNotifier(arg1);
             }
             else if(getIdentifier().getContentProviderScheme().equals("cmiss"))
             {
                 XContentIdentifier xIdParent = new CMISSContentIdentifier(m_xContext, getIdentifier().getContentIdentifier());
                 CMISContent xParent = new CMISContent(m_xContext, xIdParent);
                 xParent.contentListenerNotifier(arg1);
             }
             contentListenerNotifier(arg);
         }        
         return com.sun.star.uno.Any.VOID;
     }
     
     //My method
     private void copyStream(XOutputStream xOut, XInputStream xInp) throws NotConnectedException, BufferSizeExceededException, IOException
     {
         byte[][] buffer = new byte[1][1];
         int len = xInp.readBytes(buffer, 1024);
         while(len==1024)
         {
             xOut.writeBytes(buffer[0]);
             len = xInp.readBytes(buffer, 1024);
         }
     }
     
     //My method
     private Object insert(InsertCommandArgument iArg) throws NotConnectedException, IOException, java.io.IOException, ContentCreationException
     {
         if(exists)
         {
             if(!is_folder)
             {
                 // To-be tested
                 ContentEvent arg = new ContentEvent();
                 arg.Content = this;
                 arg.Id = xContentid;
                 arg.Action = ContentAction.INSERTED;                
                 resourceManager.setInputStream(iArg.Data);
                 contentListenerNotifier(arg);
                 return null;
             }
             else
             {
                 Folder parentFolder = resourceManager.getParent();
                 FileUtils.delete(resourceManager.getID(),connected_session);
                 ContentEvent arg = new ContentEvent();
                 arg.Action = ContentAction.INSERTED;
                 arg.Content = this;
                 arg.Id = xContentid;
                 cmisContent  = FileUtils.createFolder(parentFolder, path.substring(6,path.lastIndexOf("/")+1), "cmis:folder");
                 resourceManager = new CMISResourceManager(m_xContext, cmisContent, connected_session);               
                 if(getIdentifier().getContentProviderScheme().equals("cmis"))
                 {
                     XContentIdentifier xIdParent = new CMISContentIdentifier(m_xContext, getIdentifier().getContentIdentifier());
                     CMISContent xParent = new CMISContent(m_xContext, xIdParent);
                     xParent.contentListenerNotifier(arg);
                 }
                 else if(getIdentifier().getContentProviderScheme().equals("cmiss"))
                 {
                     XContentIdentifier xIdParent = new CMISSContentIdentifier(m_xContext, getIdentifier().getContentIdentifier());
                     CMISContent xParent = new CMISContent(m_xContext, xIdParent);
                     xParent.contentListenerNotifier(arg);
                 }
                 //contentListenerNotifier(arg);
                 // Content Event to be added.
                 return null;
             }
         }
         else
         {
             if(is_folder)
             {
                if(nameSet) 
                {
                    String parentUri = xContentid.getContentIdentifier().substring(0, xContentid.getContentIdentifier().lastIndexOf('/'));
                    processIdentifier(parentUri);
                    if(resourceManager.createFolder(xContentid.getContentIdentifier().substring(xContentid.getContentIdentifier().lastIndexOf("/")+1)))
                    {
                         ContentEvent arg = new ContentEvent();
                         arg.Action = ContentAction.INSERTED;
                         arg.Content = this;
                         arg.Id = xContentid;
                         exists = true;                        
                         processIdentifier(xContentid.getContentIdentifier());
                         contentListenerNotifier(arg);
                         //ContentEvent to be created
                    }
                }
                else
                {
                 //throw some exception;
                    throw new UnsupportedOperationException("Name not set");
                }
             }
             else
             {
                 if(nameSet)
                 {
                     String parentUri = xContentid.getContentIdentifier().substring(0, xContentid.getContentIdentifier().lastIndexOf('/'));
                     processIdentifier(parentUri);
                     if(resourceManager.createDocument(iArg.Data,xContentid.getContentIdentifier().substring(xContentid.getContentIdentifier().lastIndexOf("/")+1), "txt/plain"))
                     {
                         exists = true;
                         ContentEvent arg = new ContentEvent();
                         arg.Action = ContentAction.INSERTED;
                         arg.Content = this;
                         arg.Id = xContentid;
                         processIdentifier(xContentid.getContentIdentifier());
                         contentListenerNotifier(arg);
                     }
                 }
                 else
                 {
                     //throw property values not set exception.
                     throw new UnsupportedOperationException("Name not set");
                 }
             }
         }
         return null;
     }
     //My method
     private Any openFolder(OpenCommandArgument2 oarg) {
         List<PropertyAndValueSet> open_result = new ArrayList<PropertyAndValueSet>();       
         if(!exists)
             return null;
         
         Folder f = resourceManager.getFolder();
         OperationContext opCon = connected_session.createOperationContext();
         if (oarg.Mode == OpenMode.ALL) 
         {
             for (CmisObject o : f.getChildren()) 
             {                                                       
                 CMISResourceManager tempChildResource = new CMISResourceManager(m_xContext, o, connected_session);
                 
                 Any values[] = tempChildResource.getPropertiesAsAny(oarg.Properties);
                 PropertyAndValueSet childValue;
                 if(xContentid.getContentIdentifier().endsWith("/"))                    
                     childValue = new PropertyAndValueSet(xContentid.getContentIdentifier()+tempChildResource.getName());
                 else
                     childValue = new PropertyAndValueSet(xContentid.getContentIdentifier()+"/"+tempChildResource.getName());
                 childValue.updateProperties(oarg.Properties, values);
                 open_result.add(childValue);
             }
         } 
         else if (oarg.Mode == OpenMode.DOCUMENTS) 
         {
             opCon.setFilterString("cmis:document");
             for (CmisObject o : f.getChildren(opCon)) 
             {                
                 CMISResourceManager tempChildResource = new CMISResourceManager(m_xContext, o, connected_session);
                 
                 Any values[] = tempChildResource.getPropertiesAsAny(oarg.Properties);
                PropertyAndValueSet childValue = new PropertyAndValueSet(xContentid.getContentIdentifier()+"/"+tempChildResource.getName());
                 childValue.updateProperties(oarg.Properties, values);
                 open_result.add(childValue);
             }
         } 
         else 
         {
             opCon.setFilterString("cmis:folder");
             for (CmisObject o : f.getChildren(opCon)) 
             {
                 CMISResourceManager tempChildResource = new CMISResourceManager(m_xContext, o, connected_session);
                 
                 Any values[] = tempChildResource.getPropertiesAsAny(oarg.Properties);
                PropertyAndValueSet childValue = new PropertyAndValueSet(xContentid.getContentIdentifier()+"/"+tempChildResource.getName());
                 childValue.updateProperties(oarg.Properties, values);
                 open_result.add(childValue);
         }
     }
     ContentPropertySet cpSet = new ContentPropertySet(m_xContext, open_result.toArray(new PropertyAndValueSet[0]));
     return new Any(new Type(ContentPropertySet.class),cpSet);
 }
 //My method
 private Any getPropertyValues(Property[] request)
     {
         if(!exists)
             return null;
         //switching to PropertyAndValueSet
         
         if(request == null)
         {
             CMISPropertySetInfo propSetInfo = new CMISPropertySetInfo(m_xContext);
             request = propSetInfo.getProperties();
         }        
         
         Any value[] = resourceManager.getPropertiesAsAny(request);                                        
         PropertyAndValueSet propertiesSet = new PropertyAndValueSet(xContentid.getContentIdentifier());
         propertiesSet.updateProperties(request, value);
         return new Any(new Type(XRow.class),propertiesSet);
     }            
     
     //My method
     private Any[] setPropertyValues(PropertyValue[] pValues) throws UnknownPropertyException, IllegalArgumentException
     {
         Any ans[] = new Any[pValues.length];
         CMISPropertySetInfo xCommandInfo = new CMISPropertySetInfo(m_xContext);
         int index = 0;
         ContentEvent conEV = new ContentEvent();
         conEV.Action = ContentAction.EXCHANGED;
         conEV.Content = this;
         conEV.Id = getIdentifier();
         
         for(PropertyValue p:pValues)
         {
             if(CMISConstants.propertiesHashMap.containsKey(p.Name))
             {
                 if(xCommandInfo.getPropertyByName(p.Name).Attributes == PropertyAttribute.READONLY)
                     ans[index] = (Any) AnyConverter.toObject(IllegalAccessException.class ,new IllegalAccessException());
                 //To - DO
                 else if(p.Name.equalsIgnoreCase("Title"))
                 {
                     if(exists)
                     {
                         String title;
                         try{
                             title = AnyConverter.toString(p.Value);
                         }catch(Exception e){
                             throw new IllegalArgumentException("Incompatible argument");
                         }                            
                                                                 
                         try
                         {
                             PropertyChangeEvent event = new PropertyChangeEvent();
                             event.PropertyName = "Title";
                             event.PropertyHandle = -1;
                             event.OldValue = resourceManager.getName();
                             event.Further = false;
                             resourceManager.setName(title);
                             event.NewValue = title;
                             notifyPropertyListeners(event);
                             ans[index] = null;
                             contentListenerNotifier(conEV);
                         }
                         catch(CmisNameConstraintViolationException e)
                         {
                             ans[index] = new Any(Type.ANY,AnyConverter.toObject(IllegalArgumentException.class, new IllegalArgumentException("Name Constraint Violated")));
                         }
                         catch(CmisContentAlreadyExistsException e)
                         {
                             ans[index] = (Any) AnyConverter.toObject(IllegalArgumentException.class, new IllegalArgumentException("Content Already Exists"));
                         }
                     // other exceptions
                     }
                     else
                     {
                         String url;
                         if(xContentid==null)
                         {
                             try{
                                 path = path+"/"+AnyConverter.toString(p.Value);
                             }catch(Exception e)
                             {
                                 throw new IllegalArgumentException("Incompatible Argument");
                             }
                             PropertyChangeEvent event = new PropertyChangeEvent();
                             event.Further = false;
                             event.OldValue = null;
                             event.PropertyName = "Title";
                             event.PropertyHandle = -1;
                             xContentid = new CMISContentIdentifier(m_xContext, path);
                             event.NewValue = p.Value;
                             ans[index] = null;
                             nameSet = true;
                             notifyPropertyListeners(event);
                             contentListenerNotifier(conEV);
                         }
                     }
                 }   
                     
             }
             
             index ++;
         }
         return ans;
     }
     
     private void notifyPropertyListeners(PropertyChangeEvent e)
     {
         PropertyChangeEvent eventArray[] = new PropertyChangeEvent[1];
         eventArray[0] = e;
         for(XPropertiesChangeListener xP:propertiesChangeListeners.keySet())
         {
             for(String s:propertiesChangeListeners.get(xP))
             {
                 if(s.equals(e.PropertyName))
                 {
                     xP.propertiesChange(eventArray);
                 }
             }
         }
     }
     public void abort(int CommandId)
     {
         // TODO: Insert your implementation for "abort" here.
     }
 
     // com.sun.star.lang.XServiceInfo:
     public String getImplementationName() {
          return m_implementationName;
     }
 
     public boolean supportsService( String sService ) {
         int len = m_serviceNames.length;
 
         for( int i=0; i < len; i++) {
             if (sService.equals(m_serviceNames[i]))
                 return true;
         }
         return false;
     }
 
     public String[] getSupportedServiceNames() {
         return m_serviceNames;
     }
 
     // com.sun.star.ucb.XCommandProcessor2:
     public void releaseCommandIdentifier(int CommandId)
     {
         // TODO: Insert your implementation for "releaseCommandIdentifier" here.
     }
 
     // com.sun.star.ucb.XContentCreator:
     public com.sun.star.ucb.ContentInfo[] queryCreatableContentsInfo()
     {
         ContentInfo info[] = new ContentInfo[2];
         
         Property p[] = new Property[1];
         p[0] = new Property("Title", -1, Type.STRING, PropertyAttribute.BOUND);
                 
         info[0] = new ContentInfo("application/cmis-folder", ContentInfoAttribute.KIND_FOLDER, p);
         info[1] = new ContentInfo("application/cmis-document",ContentInfoAttribute.INSERT_WITH_INPUTSTREAM|ContentInfoAttribute.KIND_DOCUMENT,p);
                         
         
         if(isFolder())
         {
             ContentInfo[] returnInfo;
             List<Object> oT = cmisContent.getProperty(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS).getValues();
             int flag = 0;
             
             for(Object o:oT)
             {
                 if(o.toString().equals("*"))
                 {
                     flag = 3;
                     break;
                 }
                 else if(o.toString().equalsIgnoreCase(ObjectType.DOCUMENT_BASETYPE_ID))
                 {
                     flag += 1;
                 }
                 else if(o.toString().equalsIgnoreCase(ObjectType.FOLDER_BASETYPE_ID))
                 {
                     flag += 2;
                 }
                 
             }
             if(flag==3)
                 return info;        
             else if(flag==1)
             {
                 returnInfo = new ContentInfo[1];
                 returnInfo[0] = info[0];
                 return returnInfo;
             }
             else if(flag==2)
             {
                 returnInfo = new ContentInfo[1];
                 returnInfo[0] = info[1];
                 return returnInfo;
             }
             
         }
         else
             return null;
         // Deprecated;                                
         return null;
     }
 
     public com.sun.star.ucb.XContent createNewContent(com.sun.star.ucb.ContentInfo Info)
     {        
         XContent xContent = null;
         try {
             xContent = new CMISContent(m_xContext, Info.Type, path);
         } catch (ContentCreationException ex) {
             Logger.getLogger(CMISContent.class.getName()).log(Level.SEVERE, null, ex);
         }
         return xContent;
     }
 
     // com.sun.star.beans.XPropertyContainer:
     public void addProperty(String Name, short Attributes, Object DefaultValue) throws com.sun.star.beans.PropertyExistException, com.sun.star.beans.IllegalTypeException, com.sun.star.lang.IllegalArgumentException
     {
         // TODO: Insert your implementation for "addProperty" here.
     }
 
     public void removeProperty(String Name) throws com.sun.star.beans.UnknownPropertyException, com.sun.star.beans.NotRemoveableException
     {
         // TODO: Insert your implementation for "removeProperty" here.
     }
 
 }
