 /**
  *  @(#)CreateWSDL.
  *  Copyright © 2010 tourapp.com. All rights reserved.
  */
 package org.jbundle.main.msg.wsdl;
 
 import java.awt.*;
 import java.util.*;
 
 import org.jbundle.base.db.*;
 import org.jbundle.thin.base.util.*;
 import org.jbundle.thin.base.db.*;
 import org.jbundle.base.db.event.*;
 import org.jbundle.base.db.filter.*;
 import org.jbundle.base.field.*;
 import org.jbundle.base.field.convert.*;
 import org.jbundle.base.field.event.*;
 import org.jbundle.base.screen.model.*;
 import org.jbundle.base.screen.model.util.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.base.thread.*;
 import org.jbundle.main.msg.db.*;
 import java.io.*;
 import javax.xml.bind.*;
 import org.jbundle.base.message.trx.message.external.convert.jaxb.*;
 
 /**
  *  CreateWSDL - .
  */
 public class CreateWSDL extends BaseProcess
 {
     public static final String OPERATIONS_NS_NAME = "ota";
     public static final String WSOAP_BINDING_URI = "wsoap.uri";
     public static final String SOAP_SENDING_URI = "soap.sending.uri";
     public static final String SCHEMA_BINDING_URI = "schema.uri";
     public static final String SOAP_URI = "soap.binding.uri";
     public static final String MESSAGE_PATTERN_URI = "message.pattern.uri";
     public static final String MESSAGE_STYLE_URI = "message.style.uri";
     public static final String SOAP_RESPONSE_URI = "soap.response.uri";
     public final static boolean SAFE_DEFAULT = true;
     public static enum OperationType {
         BINDING_OPERATIONS,
         INTERFACE_OPERATIONS,
         TYPES_OPERATIONS,
         MESSAGE_OPERATIONS  // WSDL 1.1 only
     };
     protected org.w3._2001.xmlschema.ObjectFactory schemaFactory = null;
     protected Set<String> names = new HashSet<String>();
     public static Map<String,Object> DESCRIPTIONS = null;
     protected String[][] DEFAULTS = null;
     /**
      * Default constructor.
      */
     public CreateWSDL()
     {
         super();
     }
     /**
      * Constructor.
      */
     public CreateWSDL(RecordOwnerParent taskParent, Record recordMain, Map<String,Object> properties)
     {
         this();
         this.init(taskParent, recordMain, properties);
     }
     /**
      * Initialize class fields.
      */
     public void init(RecordOwnerParent taskParent, Record recordMain, Map<String, Object> properties)
     {
         super.init(taskParent, recordMain, properties);
     }
     /**
      * Open the main file.
      */
     public Record openMainRecord()
     {
         return new MessageProcessInfo(this);
     }
     /**
      * Open the other files.
      */
     public void openOtherRecords()
     {
         super.openOtherRecords();
         new MessageControl(this);
         new MessageTransportInfo(this);
     }
     /**
      * GetXML Method.
      */
     public String getXML(Object root)
     {
         if (root != null)
         {
             try {
                 ByteArrayOutputStream out = new ByteArrayOutputStream();
                 String strSOAPPackage = this.getSOAPPackage();
         
                 Marshaller m = JaxbContexts.getJAXBContexts().getMarshaller(strSOAPPackage);
         
                 synchronized (m)
                 {
                     m.marshal( root, out );
                 }
                 String strXML = out.toString(Constants.STRING_ENCODING);
                 return strXML;
             } catch (IOException ex)  {
                 ex.printStackTrace();   // Never
             } catch (JAXBException ex)   {
                 ex.printStackTrace();
             } catch (java.lang.IllegalArgumentException ex)   {
                 ex.printStackTrace();
             }
         }
         return null;
     }
     /**
      * GetSOAPPackage Method.
      */
     public String getSOAPPackage()
     {
         return null;    // Override
     }
     /**
      * CreateMarshallableObject Method.
      */
     public Object createMarshallableObject()
     {
         return null;
     }
     /**
      * IsNewType Method.
      */
     public boolean isNewType(String name)
     {
         return names.add(name);
     }
     /**
      * GetDefaultVersion Method.
      */
     public String getDefaultVersion()
     {
         Record recMessageVersion = ((ReferenceField)this.getRecord(MessageControl.kMessageControlFile).getField(MessageControl.kDefaultVersionID)).getReference();
         if (recMessageVersion != null)
             if ((recMessageVersion.getEditMode() == DBConstants.EDIT_CURRENT) || (recMessageVersion.getEditMode() == DBConstants.EDIT_IN_PROGRESS))
                 return recMessageVersion.getField(MessageVersion.kCode).toString();
         return "2007B"; // Never
     }
     /**
      * ScanProcesses Method.
      */
     public void scanProcesses(Object typeObject, OperationType type)
     {
         String strTargetVersion = this.getProperty("version");
         if (strTargetVersion == null)
             strTargetVersion = this.getDefaultVersion();
         Record recMessageTransport = ((ReferenceField)this.getRecord(MessageControl.kMessageControlFile).getField(MessageControl.kWebMessageTransportID)).getReference();
         MessageVersion recMessageVersion = ((MessageControl)this.getRecord(MessageControl.kMessageControlFile)).getMessageVersion(strTargetVersion);
         MessageProcessInfo recMessageProcessInfo = new MessageProcessInfo(this);
         recMessageProcessInfo.setKeyArea(MessageProcessInfo.kMessageInfoIDKey);
         try   {
              // Always register this generic processing queue.
             recMessageProcessInfo.close();
             while (recMessageProcessInfo.hasNext())
             {
                 recMessageProcessInfo.next();
                 String strQueueName = recMessageProcessInfo.getQueueName(true);
                 String strQueueType = recMessageProcessInfo.getQueueType(true);
                 String strProcessClass = recMessageProcessInfo.getField(MessageProcessInfo.kProcessorClass).toString();
                 Map<String,Object> properties = ((PropertiesField)recMessageProcessInfo.getField(MessageProcessInfo.kProperties)).getProperties();
                 Record recMessageType = ((ReferenceField)recMessageProcessInfo.getField(MessageProcessInfo.kMessageTypeID)).getReference();
                 if (recMessageType != null)
                 {   // Start all processes that handle INcoming REQUESTs.
                     String strMessageType = recMessageType.getField(MessageType.kCode).toString();
                     Record recMessageInfo = ((ReferenceField)recMessageProcessInfo.getField(MessageProcessInfo.kMessageInfoID)).getReference();
                     if (recMessageInfo != null)
                     {
                         Record recMessageInfoType = ((ReferenceField)recMessageInfo.getField(MessageInfo.kMessageInfoTypeID)).getReference();
                         if (recMessageInfoType != null)
                         {
                             String strMessageInfoType = recMessageInfoType.getField(MessageInfoType.kCode).toString();
                             if (MessageInfoType.REQUEST.equals(strMessageInfoType))
                                 if (MessageType.MESSAGE_IN.equals(strMessageType))
                                     if ((strQueueName != null) && (strQueueName.length() > 0))
                             {
                                 Record recMessageTransportInfo = this.getRecord(MessageTransportInfo.kMessageTransportInfoFile);
                                 recMessageTransportInfo.setKeyArea(MessageTransportInfo.kMessageProcessInfoIDKey);
                                 recMessageTransportInfo.getField(MessageTransportInfo.kMessageProcessInfoID).moveFieldToThis(recMessageProcessInfo.getField(MessageProcessInfo.kID));
                                 recMessageTransportInfo.getField(MessageTransportInfo.kMessageTransportID).moveFieldToThis(recMessageTransport.getField(MessageTransport.kID));
                                 recMessageTransportInfo.getField(MessageTransportInfo.kMessageVersionID).moveFieldToThis(recMessageVersion.getField(MessageVersion.kID));
                                 if (recMessageTransportInfo.seek(DBConstants.EQUALS))
                                 {
                                     this.addProcessForWSDL(strTargetVersion, typeObject, recMessageProcessInfo, type);
                                 }
                             }
                         }
                     }
                 }
             }
             recMessageProcessInfo.close();
         } catch (DBException ex)    {
             ex.printStackTrace();
         } finally {
             recMessageProcessInfo.free();
         }
     }
     /**
      * AddProcessForWSDL Method.
      */
     public void addProcessForWSDL(String strVersion, Object typeObject, MessageProcessInfo recMessageProcessInfo, OperationType type)
     {
         // Override this!
     }
     /**
      * GetControlProperty Method.
      */
     public String getControlProperty(String strKey)
     {
         return this.getControlProperty(strKey, null);
     }
     /**
      * GetControlProperty Method.
      */
     public String getControlProperty(String strKey, String strDefaultValue)
     {
         String strValue = ((PropertiesField)this.getRecord(MessageControl.kMessageControlFile).getField(MessageControl.kProperties)).getProperty(strKey);
         if (strValue == null)
         {
             if (strDefaultValue == null)
                 strDefaultValue = this.getDefaultValue(strKey);
             strValue = strDefaultValue;
         }
         return strValue;
     }
     /**
      * GetDefaultValue Method.
      */
     public String getDefaultValue(String strKey)
     {
         if (DESCRIPTIONS == null)
             DESCRIPTIONS = Utility.arrayToMap(DEFAULTS);
         return (String)DESCRIPTIONS.get(strKey);
     }
     /**
      * GetURIProperty Method.
      */
     public String getURIProperty(String strKey)
     {
         return this.getURIProperty(strKey, null);
     }
     /**
      * GetURIProperty Method.
      */
     public String getURIProperty(String strKey, String strDefaultValue)
     {
         String strValue = this.getControlProperty(strKey, strDefaultValue);
         return this.getURIValue(strValue);
     }
     /**
      * GetURIValue Method.
      */
     public String getURIValue(String strValue)
     {
         String strBaseURI = ((PropertiesField)this.getRecord(MessageControl.kMessageControlFile).getField(MessageControl.kProperties)).getProperty(MessageControl.BASE_NAMESPACE_URI);
         if (strBaseURI == null)
         {
             strBaseURI = this.getProperty(DBParams.BASE_URL);   // Defaults to same site as wsdl
             if (strBaseURI != null)
                 if (strBaseURI.endsWith("/"))
                     strBaseURI = strBaseURI.substring(0, strBaseURI.length() - 1);
         }
         if (strBaseURI != null)
            if (strBaseURI.indexOf("http://") != 0)
                 strBaseURI = "http://" + strBaseURI;
         if (strBaseURI != null)
         {
             if (strValue == null)
                 strValue = strBaseURI;
             else if ((strValue.indexOf("http://") != 0)
                 || (strValue.startsWith("/")))
                     strValue = strBaseURI + strValue;
         }
         return strValue;
     }
     /**
      * GetMessageIn Method.
      */
     public MessageInfo getMessageIn(MessageProcessInfo recMessageProcessInfo)
     {
         return (MessageInfo)((ReferenceField)recMessageProcessInfo.getField(MessageProcessInfo.kMessageInfoID)).getReference();
     }
     /**
      * GetMessageOut Method.
      */
     public MessageInfo getMessageOut(MessageProcessInfo recMessageProcessInfo)
     {
         MessageProcessInfo recMessageProcessInfo2 =  (MessageProcessInfo)((ReferenceField)recMessageProcessInfo.getField(MessageProcessInfo.kReplyMessageProcessInfoID)).getReference();
         if (recMessageProcessInfo2 != null)
             return (MessageInfo)((ReferenceField)recMessageProcessInfo2.getField(MessageProcessInfo.kMessageInfoID)).getReference();
         return null;
     }
     /**
      * FixName Method.
      */
     public String fixName(String name)
     {
         for (int i = 0; i < name.length(); i++)
         {
             if (Character.isWhitespace(name.charAt(i)))
                     name = name.substring(0, i) + name.substring(i + 1);
         }
         return name;
     }
     /**
      * GetMessageControl Method.
      */
     public MessageControl getMessageControl()
     {
         MessageControl recMessageControl = (MessageControl)this.getRecord(MessageControl.kMessageControlFile);
         return recMessageControl;
     }
     /**
      * GetNamespace Method.
      */
     public String getNamespace()
     {
         String strTargetVersion = this.getProperty("version");
         if (strTargetVersion == null)
             strTargetVersion = this.getDefaultVersion();
         return this.getMessageControl().getNamespaceFromVersion(strTargetVersion);
     }
 
 }
