 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.controllers.propertysources;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ReentryProperty;
 import org.eclipse.jubula.client.core.utils.StringHelper;
 import org.eclipse.jubula.client.ui.controllers.propertydescriptors.IntegerTextPropertyDescriptor;
 import org.eclipse.jubula.client.ui.controllers.propertydescriptors.JBPropertyDescriptor;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.provider.labelprovider.DisabledLabelProvider;
 import org.eclipse.jubula.client.ui.utils.Utils;
 import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
 import org.eclipse.ui.views.properties.IPropertyDescriptor;
 import org.eclipse.ui.views.properties.PropertyDescriptor;
 import org.eclipse.ui.views.properties.TextPropertyDescriptor;
 
 
 /**
  * This class is the PropertySource of an EventExecTestCase.
  * Its used to display and edit the properties in the Properties View.
  *
  * @author BREDEX GmbH
  * @created 07.04.2005
  */
 public class EventExecTestCaseGUIPropertySource extends 
     ExecTestCaseGUIPropertySource {
     
     /** Constant for the String "EventHandler Name" */
     private static final String P_EVHANDLER_DISPLAY_NAME =
         Messages.EventExecTestCaseGUIPropertySourceEventHandlerName;
     
     /** Constant for the String "Event Type" */
     private static final String P_ELEMENT_DISPLAY_EVENTTYPE =
         Messages.EventExecTestCaseGUIPropertySourceEventType;
     
     /** Constant for the String "Reentry Type" */
     private static final String P_ELEMENT_DISPLAY_REENTRYTYPE =
         Messages.EventExecTestCaseGUIPropertySourceReentryType;
     
     /** Constant for the String "Maximum Number of Retries" */
     private static final String P_ELEMENT_DISPLAY_MAXRETRIES =
         Messages.EventExecTestCaseGUIPropertySourceMaxRetries;
     
     
     /** List of event types (short names)*/
     private static final String[] EVENT_TYPES = initEventTypes(); 
 
     /** List of Reentry Types */
     private static String[] reentryTypes = initReentryTypes();
 
     /** cached property descriptor for name */
     private IPropertyDescriptor m_namePropDesc = null;
     
     /** cached property descriptor for name of referenced Test Case */
     private IPropertyDescriptor m_specNamePropDesc = null;
     
     /** cached property descriptor for event type */
     private IPropertyDescriptor m_eventTypePropDesc = null;
 
     /** cached property descriptor for reentry type */
     private IPropertyDescriptor m_reentryTypePropDesc = null;
 
     /** cached property descriptor for max number of retries */
     private IPropertyDescriptor m_maxRetriesPropDesc = null;
 
     /**
      * @param eventExTestCase
      *            the dependend EventExecTestCase.
      */
     public EventExecTestCaseGUIPropertySource(
         IEventExecTestCasePO eventExTestCase) {
         
         super(eventExTestCase);
     }
 
     /**
      * Initializes the EventTypes.
      * @return a String-Array of Event Types.
      */
     @SuppressWarnings("unchecked")
     private static String[] initEventTypes() {
         Set mapKeySet = ComponentBuilder.getInstance().getCompSystem()
             .getEventTypes().keySet(); 
         String[] eventTypes = new String[mapKeySet.size()];
         int i = 0;
         final Map<String, String> stringHelperMap = StringHelper.getInstance()
             .getMap();
         for (Object object : mapKeySet) {
             eventTypes[i] = stringHelperMap.get(object.toString());
             i++;
         }
         return eventTypes;
     }
     
     /**
      * Initializes the Reentry types Array.
      * @return a String Array of String representations of the ReentryProperties.
      */
     static String[] initReentryTypes() {
         ReentryProperty[] reentryProperties = 
             ReentryProperty.REENTRY_PROP_ARRAY;
         final int reentryPropertiesLength =  reentryProperties.length;
         reentryTypes = new String[reentryPropertiesLength];
         for (int i = 0; i < reentryPropertiesLength; i++) {
             reentryTypes[i] = reentryProperties[i].toString();
         }
         return reentryTypes;
     }
     
     
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("synthetic-access")
     protected void initPropDescriptor() {
         clearPropertyDescriptors();
         
         // EventHandler Name
         if (m_namePropDesc == null) {
             m_namePropDesc = new TextPropertyDescriptor(
                     new ExecNameController(),
                     P_EVHANDLER_DISPLAY_NAME);
         }
         addPropertyDescriptor(m_namePropDesc);
         // Specification Name
         if (m_specNamePropDesc == null) {
             PropertyDescriptor propDes = new JBPropertyDescriptor(
                     new SpecNameController(),
                     P_SPECNAME_DISPLAY_NAME);
             propDes.setLabelProvider(new DisabledLabelProvider());
             m_specNamePropDesc = propDes;
         }
         addPropertyDescriptor(m_specNamePropDesc);
         
         // Event Types
         if (m_eventTypePropDesc == null) {
            PropertyDescriptor cbpd = new ComboBoxPropertyDescriptor(
                    new EventTypeController(), P_ELEMENT_DISPLAY_EVENTTYPE,
                    EVENT_TYPES);
             cbpd.setLabelProvider(new LabelProvider() {
                 public String getText(Object element) {
                     if (element instanceof Integer) {
                         return EVENT_TYPES[((Integer)element).intValue()];
                     }
                     Assert.notReached(Messages.WrongElementType 
                             + StringConstants.EXCLAMATION_MARK);
                     return String.valueOf(element);
                 }
             });
             m_eventTypePropDesc = cbpd;
         }
         addPropertyDescriptor(m_eventTypePropDesc);
         
         // Reentry Types
         if (m_reentryTypePropDesc == null) {
             PropertyDescriptor cbpd = new ComboBoxPropertyDescriptor(
                     new ReentryTypeController(), P_ELEMENT_DISPLAY_REENTRYTYPE,
                     reentryTypes);
             cbpd.setLabelProvider(new LabelProvider() {
                 public String getText(Object element) {
                     if (element instanceof Integer) {
                         return reentryTypes[((Integer)element).intValue()];
                     }
                     Assert.notReached("Wrong element type!"); //$NON-NLS-1$
                     return String.valueOf(element);
                 }
             });
             m_reentryTypePropDesc = cbpd;
         }
         addPropertyDescriptor(m_reentryTypePropDesc);
         
         // Max Retries
         if (m_maxRetriesPropDesc == null) {
             m_maxRetriesPropDesc = new IntegerTextPropertyDescriptor(
                     new MaxRetriesController(), P_ELEMENT_DISPLAY_MAXRETRIES, 
                     false, IEventExecTestCasePO.MIN_VALUE_MAX_NUM_RETRIES, 
                     IEventExecTestCasePO.MAX_VALUE_MAX_NUM_RETRIES);
         }
         if (((IEventExecTestCasePO)getPoNode()).getReentryProp()
                 .equals(ReentryProperty.RETRY)) {
             addPropertyDescriptor(m_maxRetriesPropDesc);
         }
 
         // empty line
         addPropertyDescriptor(new JBPropertyDescriptor(
                 new DummyController(), StringConstants.EMPTY));
         // Get the ExecTcDescriptors and add them to this descriptors
         addPropertyDescriptor(super.createParamDescriptors());
     }
 
     /**
      * @author BREDEX GmbH
      * @created 08.04.2005
      */
     public class EventTypeController extends AbstractPropertyController {
 
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public boolean setProperty(Object value) {
             boolean propSet = false;
             IEventExecTestCasePO eventTc = (IEventExecTestCasePO) getPoNode();
             final String oldType = eventTc.getEventType();
             ISpecTestCasePO specTc = (ISpecTestCasePO)eventTc.getParentNode();
             specTc.removeNode(eventTc);
             String evType = EVENT_TYPES[((Integer)value).intValue()];
             evType = StringHelper.getInstance().getMap().get(evType);
             eventTc.setEventType(evType);
             try {
                 specTc.addEventTestCase(eventTc);
                 propSet = true;
             } catch (InvalidDataException e) {
                 // try to set the old event type
                 Utils.createMessageDialog(e, null, null);
                 eventTc.setEventType(oldType);
                 try {
                     specTc.addEventTestCase(eventTc);
                 } catch (InvalidDataException e1) {
                     // should not happen!
                     Utils.createMessageDialog(e1, null, null);
                 }
             }
             
             return propSet;
         }
 
         /**
          * {@inheritDoc}
          */
         public Object getProperty() {
             return getIndexOfType();
         }
 
         /**
          * {@inheritDoc}
          */
         public Image getImage() {
             return DEFAULT_IMAGE;
         }
         
         /**
          * Returns the index of the type from the String-Array for the 
          * ComboBoxPropertyDescriptor.
          * @return an <code>Integer</code> value. The index.
          */
         @SuppressWarnings("synthetic-access")
         private Integer getIndexOfType() {
             IEventExecTestCasePO eventTc = (IEventExecTestCasePO) getPoNode();
             final int eventTypesLength = EVENT_TYPES.length;
             String ehEventType = StringHelper.getInstance().getMap()
                 .get(eventTc.getEventType());
             for (int i = 0; i < eventTypesLength; i++) {
                 if (EVENT_TYPES[i].equals(ehEventType)) {
                     return Integer.valueOf(i);
                 }
             }
             return Integer.valueOf(0);
         }
         
     }
     
     /**
      * @author BREDEX GmbH
      * @created 08.04.2005
      */
     public class ReentryTypeController extends AbstractPropertyController {
 
         /**
          * {@inheritDoc}
          */
         public boolean setProperty(Object value) {
             IEventExecTestCasePO eventTc = (IEventExecTestCasePO) getPoNode();
             ReentryProperty prop = ReentryProperty
                 .REENTRY_PROP_ARRAY[((Integer)value).intValue()];
             ReentryProperty oldProp = eventTc.getReentryProp();
             eventTc.setReentryProp(prop);
             
             // Set default value of max retries as needed
             if (oldProp.equals(ReentryProperty.RETRY) 
                     && !prop.equals(ReentryProperty.RETRY)) {
                 
                 eventTc.setMaxRetries(null);
             } else if (!oldProp.equals(ReentryProperty.RETRY) 
                     && prop.equals(ReentryProperty.RETRY)) {
                 
                 eventTc.setMaxRetries(
                         IEventExecTestCasePO.DEFAULT_MAX_NUM_RETRIES);
             }
 
             
             return true;
         }
 
         /**
          * {@inheritDoc}
          */
         public Object getProperty() {
             return getIndexOfType();
         }
 
         /**
          * {@inheritDoc}
          */
         public Image getImage() {
             return DEFAULT_IMAGE;
         }
         
         
         /**
          * Returns the index of the type from the String-Array for the 
          * ComboBoxPropertyDescriptor.
          * @return an <code>Integer</code> value. The index.
          */
         private Integer getIndexOfType() {
             IEventExecTestCasePO eventTc = (IEventExecTestCasePO) getPoNode();
             final int reentryTypesLength = reentryTypes.length;
             for (int i = 0; i < reentryTypesLength; i++) {
                 if (ReentryProperty.REENTRY_PROP_ARRAY[i].equals(
                             eventTc.getReentryProp())) {
                     return Integer.valueOf(i);
                 }
             }
             return Integer.valueOf(0);
         }
         
     }
     
     /**
      * @author BREDEX GmbH
      * @created 03.04.2008
      */
     private class MaxRetriesController extends AbstractPropertyController {
 
         /**
          * {@inheritDoc}
          */
         public boolean setProperty(Object value) {
             IEventExecTestCasePO eventTc = (IEventExecTestCasePO)getPoNode();
             Integer intValue = null;
             
             if (value instanceof Integer) {
                 intValue = (Integer)value;
             } else if (value instanceof String) {
                 try {
                     intValue = Integer.parseInt((String)value);
                 } catch (NumberFormatException nfe) {
                     return false;
                 }
             } else {
                 return false;
             }
 
             if (intValue >= IEventExecTestCasePO.MIN_VALUE_MAX_NUM_RETRIES
                 && intValue <= IEventExecTestCasePO.MAX_VALUE_MAX_NUM_RETRIES) {
                 
                 if (intValue.equals(eventTc.getMaxRetries())) {
                     return false;
                 }
                 eventTc.setMaxRetries(intValue);
                 return true;
             }
 
             return false;
         }
 
         /**
          * {@inheritDoc}
          */
         public Object getProperty() {
             IEventExecTestCasePO eventTc = (IEventExecTestCasePO)getPoNode();
             return String.valueOf(eventTc.getMaxRetries());
         }
 
         /**
          * {@inheritDoc}
          */
         public Image getImage() {
             return DEFAULT_IMAGE;
         }
         
     }
 }
