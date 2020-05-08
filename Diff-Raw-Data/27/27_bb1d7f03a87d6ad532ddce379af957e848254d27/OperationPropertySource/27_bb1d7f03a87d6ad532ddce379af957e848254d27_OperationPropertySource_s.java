 /* 
  * CDDL HEADER START 
  * 
  * The contents of this file are subject to the terms of the 
  * Common Development and Distribution License (the "License"). 
  * You may not use this file except in compliance with the License. 
  * 
  * You can obtain a copy of the license at LICENSE.txt 
  * or at http://www.opensource.org/licenses/cddl1.php. 
  * See the License for the specific language governing permissions 
  * and limitations under the License. 
  * 
  * When distributing Covered Code, include this CDDL HEADER in each 
  * file and include the License file at LICENSE.txt. 
  * If applicable, add the following below this CDDL HEADER, with the 
  * fields enclosed by brackets "[]" replaced with your own identifying 
  * information: Portions Copyright [yyyy] [name of copyright owner] 
  * 
  * CDDL HEADER END 
  */ 
 
 /* 
  * Copyright 2005, 2006 Julien Ponge. All rights reserved. 
  * Use is subject to license terms. 
  */ 
 
 package fr.isima.ponge.wsprotocol.gefeditor.editparts.propertysources;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.dom4j.Attribute;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.io.SAXReader;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
 import org.eclipse.ui.views.properties.IPropertyDescriptor;
 import org.eclipse.ui.views.properties.IPropertySource;
 import org.eclipse.ui.views.properties.TextPropertyDescriptor;
 
 import fr.isima.ponge.wsprotocol.OperationKind;
 import fr.isima.ponge.wsprotocol.Polarity;
 import fr.isima.ponge.wsprotocol.StandardExtraProperties;
 import fr.isima.ponge.wsprotocol.gefeditor.Messages;
 import fr.isima.ponge.wsprotocol.impl.MessageImpl;
 import fr.isima.ponge.wsprotocol.impl.OperationImpl;
 
 /**
  * The property source for operations (actually works on the message carried by the operation).
  * 
  * @author Julien Ponge (ponge@isima.fr)
  */
 public class OperationPropertySource implements IPropertySource
 {
 
     /**
      * Id for the WSDL problems resource marker.
      */
     private static final String WSDL_PROBLEM_MARKER_ID = "gef.editor.wsdl.io.problem"; //$NON-NLS-1$
     
     /**
      * The operation name property ... name :-)
      */
     protected static final String OPERATION_NAME_PROPERTY = "operation.name"; //$NON-NLS-1$
     
     /**
      * The message polarity property name.
      */
     protected static final String MESSAGE_POLARITY_PROPERTY = "message.polarity"; //$NON-NLS-1$
 
     /**
      * The message name property name.
      */
     protected static final String MESSAGE_NAME_PROPERTY = "message.name"; //$NON-NLS-1$
     
     /**
      * The operation kind property name.
      */
     protected static final String OPERATION_KIND_PROPERTY = "operation.kind"; //$NON-NLS-1$
     
     /**
      * The operation temporal constraint property name.
      */
     protected static final String TEMPORAL_CONSTRAINT_PROPERTY = "temporal.constraint"; //$NON-NLS-1$
 
     /**
      * The operation
      */
     protected OperationImpl operation;
     
     /**
      * The message.
      */
     protected MessageImpl message;
 
     /**
      * The (optional) WSDL document location.
      */
     protected String wsdlLocation;
 
     /**
      * The associated protocol file descriptor.
      */
     protected IFile protocolFile;
 
     /**
      * WSDL messages, as an array.
      */
     protected String[] wsdlMessages;
 
     /**
      * WSDL messages, as a list.
      */
     protected List wsdlMessagesList;
 
     /**
      * The properties descriptors.
      */
     protected IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[5];
 
     /**
      * Constructs a new operations properties source.
      * 
      * @param operation
      *            The operation.
      * @param wsdlLocation
      *            The WSDL document location.
      * @param protocolFile
      *            The associated protocol file.
      */
     public OperationPropertySource(OperationImpl operation, String wsdlLocation, IFile protocolFile)
     {
         super();
         this.operation = operation;
         this.message = (MessageImpl)operation.getMessage();
         this.wsdlLocation = wsdlLocation;
         this.protocolFile = protocolFile;
 
         updatePropertyDescriptors();
     }
 
     /**
      * Creates or updates the property descriptors. If the protocol has a WSDL that can be parsed,
      * then the messages names will be presented in a combo.
      */
     protected void updatePropertyDescriptors()
     {
        // TODO: i18n
        propertyDescriptors[0] = new TextPropertyDescriptor(OPERATION_NAME_PROPERTY, "Name");   
         
         List messages = getWSDLMessagesNames();
         if (messages.equals(Collections.EMPTY_LIST) || message.getPolarity().equals(Polarity.NULL))
         {
             propertyDescriptors[1] = new TextPropertyDescriptor(MESSAGE_NAME_PROPERTY,
                     Messages.messageName);
         }
         else
         {
             Object[] vals = messages.toArray();
             wsdlMessages = new String[vals.length];
             for (int i = 0; i < vals.length; ++i)
             {
                 wsdlMessages[i] = (String) vals[i];
             }
             wsdlMessagesList = messages;
             propertyDescriptors[1] = new ComboBoxPropertyDescriptor(MESSAGE_NAME_PROPERTY,
                     Messages.messageName, wsdlMessages);
         }
 
         propertyDescriptors[2] = new ComboBoxPropertyDescriptor(MESSAGE_POLARITY_PROPERTY,
                 Messages.messagePolarity, new String[] { Messages.input, Messages.output,
                         Messages.none });
         
         propertyDescriptors[3] = new ComboBoxPropertyDescriptor(OPERATION_KIND_PROPERTY, Messages.operationKind, new String[] {
            Messages.explicit, Messages.implicit   
         });
         
        // TODO: i18n
        propertyDescriptors[4] = new TextPropertyDescriptor(TEMPORAL_CONSTRAINT_PROPERTY, "Temporal constraint");        
     }
 
     /*
      * Used to cache the WSDL parsing results.
      */
     private static Map cachedParsings = new HashMap();
 
     /**
      * Trys to get the messages names from a WSDL document.
      * 
      * @return The list of the messages names or <code>Collections.EMPTY_LIST</code>.
      */
     protected List getWSDLMessagesNames()
     {
         // No WSDL
        if (wsdlLocation == null || "".equals(wsdlLocation))
         {
             return Collections.EMPTY_LIST;
         }
 
         // Cached parsings
         if (cachedParsings.containsKey(wsdlLocation))
         {
             return (List) cachedParsings.get(wsdlLocation);
         }
 
         // Parse the WSDL
         cleanProblemMarkers(WSDL_PROBLEM_MARKER_ID);
         List names = new ArrayList();
         try
         {
             SAXReader reader = new SAXReader();
             Document document = reader.read(new URL(wsdlLocation));
             Iterator nodesIt = document.selectNodes("//*[local-name()='message']/@name").iterator();
             while (nodesIt.hasNext())
             {
                 Attribute attr = (Attribute) nodesIt.next();
                 names.add(attr.getValue());
             }
             Collections.sort(names);
         }
         catch (MalformedURLException e)
         {
             reportWSDLProblem();
         }
         catch (DocumentException e)
         {
             reportWSDLProblem();
         }
         cachedParsings.put(wsdlLocation, (names.size() > 0) ? names : Collections.EMPTY_LIST);
         return (names.size() > 0) ? names : Collections.EMPTY_LIST;
     }
 
     /**
      * Removes the problem markers on the file resource (if any).
      * @param markerId The problem marker ID.
      */
     private void cleanProblemMarkers(String markerId)
     {
         try
         {
             IMarker[] problems = protocolFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
             for (int i = 0; i < problems.length; ++i)
             {
                 if (problems[i].exists() && problems[i].getAttributes().containsKey(markerId))
                 {
                     problems[i].delete();
                 }
             }
         }
         catch (CoreException e)
         {
             e.printStackTrace();
         }        
     }
 
     /**
      * Reports a WSDL problem on the file resource.
      */
     private void reportWSDLProblem()
     {
         try
         {
             IMarker marker = protocolFile.createMarker(IMarker.PROBLEM);
             if (marker.exists())
             {
                 marker.setAttribute(IMarker.TRANSIENT, true);
                marker.setAttribute(IMarker.MESSAGE, "The specified WSDL URL is invalid or unreachable.");
                 marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                 marker.setAttribute(WSDL_PROBLEM_MARKER_ID, WSDL_PROBLEM_MARKER_ID);
             }
         }
         catch (CoreException e)
         {
             e.printStackTrace();
         }
     }
     
     
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
      */
     public Object getEditableValue()
     {
         return this;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
      */
     public IPropertyDescriptor[] getPropertyDescriptors()
     {
         return propertyDescriptors;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java.lang.Object)
      */
     public Object getPropertyValue(Object id)
     {
         if (id.equals(MESSAGE_NAME_PROPERTY))
         {
             if (wsdlMessages == null)
             {
                 return message.getName();
             }
             else
             {
                 int index = wsdlMessagesList.indexOf(message.getName());
                 if (index == -1)
                 {
                     index = 0;
                     message.setName((String) wsdlMessagesList.get(index));
                 }
                 return new Integer(index);
             }
         }
         else if (id.equals(MESSAGE_POLARITY_PROPERTY))
         {
             Polarity p = message.getPolarity();
             if (p.equals(Polarity.POSITIVE))
             {
                 return new Integer(0);
             }
             else if (p.equals(Polarity.NEGATIVE))
             {
                 return new Integer(1);
             }
             else if (p.equals(Polarity.NULL))
             {
                 return new Integer(2);
             }
         }
         else if (id.equals(OPERATION_NAME_PROPERTY))
         {
             return operation.getName();
         }
         else if (id.equals(OPERATION_KIND_PROPERTY))
         {
             OperationKind kind = operation.getOperationKind();
             if (kind.equals(OperationKind.EXPLICIT))
             {
                 return new Integer(0);
             }
             else if (kind.equals(OperationKind.IMPLICIT))
             {
                 return new Integer(1);
             }
         }
         else if (id.equals(TEMPORAL_CONSTRAINT_PROPERTY))
         {
             String constraint = (String) operation.getExtraProperty(StandardExtraProperties.TEMPORAL_CONSTRAINT);
             if (constraint == null)
             {
                return "";
             }
             return constraint;
         }
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java.lang.Object)
      */
     public boolean isPropertySet(Object id)
     {
         // All properties are mandatory in the model
         return true;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java.lang.Object)
      */
     public void resetPropertyValue(Object id)
     {
         // Nothing to do here ...
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object,
      *      java.lang.Object)
      */
     public void setPropertyValue(Object id, Object value)
     {
         if (id.equals(MESSAGE_NAME_PROPERTY))
         {
             if (wsdlMessages == null)
             {
                 message.setName((String) value);
             }
             else
             {
                 message.setName((String) wsdlMessagesList.get(((Integer) value).intValue()));
             }
         }
         else if (id.equals(MESSAGE_POLARITY_PROPERTY))
         {
             if (value.equals(Integer.valueOf("0"))) //$NON-NLS-1$
             {
                 message.setPolarity(Polarity.POSITIVE);
             }
             else if (value.equals(Integer.valueOf("1"))) //$NON-NLS-1$
             {
                 message.setPolarity(Polarity.NEGATIVE);
             }
             else if (value.equals(Integer.valueOf("2"))) //$NON-NLS-1$
             {
                 message.setPolarity(Polarity.NULL);
             }
             //updatePropertyDescriptors();
         }
         else if (id.equals(OPERATION_NAME_PROPERTY))
         {
             operation.setName((String) value);
         }
         else if (id.equals(OPERATION_KIND_PROPERTY))
         {
             if (value.equals(Integer.valueOf("0"))) //$NON-NLS-1$
             {
                 operation.setOperationKind(OperationKind.EXPLICIT);
             }
             else if (value.equals(Integer.valueOf("1"))) //$NON-NLS-1$
             {
                 operation.setOperationKind(OperationKind.IMPLICIT);
                 message.setPolarity(Polarity.NULL);
             }
         }
         else if (id.equals(TEMPORAL_CONSTRAINT_PROPERTY))
         {
             String constraint = (String) value;
             operation.putExtraProperty(StandardExtraProperties.TEMPORAL_CONSTRAINT, constraint);
         }
     }
 
 }
