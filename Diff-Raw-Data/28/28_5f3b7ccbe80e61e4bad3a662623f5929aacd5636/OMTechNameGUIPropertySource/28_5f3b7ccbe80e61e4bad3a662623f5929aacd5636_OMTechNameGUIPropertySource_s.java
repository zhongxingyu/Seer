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
 package org.eclipse.jubula.client.ui.rcp.controllers.propertysources;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jubula.client.core.model.IObjectMappingAssoziationPO;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.rcp.controllers.propertydescriptors.JBPropertyDescriptor;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.provider.labelprovider.PropertyControllerLabelProvider;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 import org.eclipse.swt.graphics.Image;
 
 
 /**
  * @author BREDEX GmbH
  * @created 21.04.2005
  */
 public class OMTechNameGUIPropertySource 
     extends AbstractPropertySource<IObjectMappingAssoziationPO> {
 
     
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_COMP =
         Messages.OMTechNameGUIPropertySourceComponent;
 
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_COMPADDINFO = 
         Messages.OMTechNameGUIPropertySourceComponentAddInfo;
     
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_COMPNAME = 
         Messages.OMTechNameGUIPropertySourceComponentName;
 
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_COMPCLASS = 
         Messages.OMTechNameGUIPropertySourceCompClass;
     
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_COMPSUPPCLASS = 
         Messages.OMTechNameGUIPropertySourceCompSuppClass;
     
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_HIERARCHY = 
         Messages.OMTechNameGUIPropertySourceHierarchy;
     
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_CONTEXT = 
         Messages.OMTechNameGUIPropertySourceContext;
 
 
     /**
      * Constructor
      * 
      * @param assoc The association from which properties are obtained.
      */
     public OMTechNameGUIPropertySource(IObjectMappingAssoziationPO assoc) {
         super(assoc);
         initPropDescriptor();
     }
 
     /**
      * Inits the PropertyDescriptors
      */
     @SuppressWarnings("synthetic-access")
     protected void initPropDescriptor() {
         clearPropertyDescriptors();
         JBPropertyDescriptor propDes = null;
         // Component Name
         propDes = new JBPropertyDescriptor(
             new ComponentNameController(), P_ELEMENT_DISPLAY_COMPNAME);
         propDes.setCategory(P_ELEMENT_DISPLAY_COMP);
         propDes.setLabelProvider(new PropertyControllerLabelProvider());
         addPropertyDescriptor(propDes);
 
         // Component Class
         propDes = new JBPropertyDescriptor(
             new ComponentClassController(), P_ELEMENT_DISPLAY_COMPCLASS);
         propDes.setCategory(P_ELEMENT_DISPLAY_COMP);
         propDes.setLabelProvider(new PropertyControllerLabelProvider());
         addPropertyDescriptor(propDes);
         
         // Component SuppClass
         propDes = new JBPropertyDescriptor(
             new ComponentSuppClassController(), 
                 P_ELEMENT_DISPLAY_COMPSUPPCLASS);
         propDes.setCategory(P_ELEMENT_DISPLAY_COMPADDINFO);
         propDes.setLabelProvider(new PropertyControllerLabelProvider());
         addPropertyDescriptor(propDes);
         
         initHierarchy();
         initContext();
         initComponentProperties();
     }
     
     /**
      * initializes the ComponentProperties
      *
      */
     @SuppressWarnings("unchecked")
     private void initComponentProperties() {
         IComponentIdentifier compId = getGuiNode().getCompIdentifier();
         if (compId != null) {
             Map<String, String> componentProperties = 
                     compId.getComponentPropertiesMap();
             if (componentProperties != null) {
                 int i = 0;
                 for (Object o : componentProperties.keySet()) {
                     if (o instanceof String) {
                         String key = (String)o;
                         JBPropertyDescriptor propDes = new JBPropertyDescriptor(
                                 new ComponentPropertiesController(i), key);
                        propDes.setCategory("Property Information"); //$NON-NLS-1$
                         addPropertyDescriptor(propDes);
                         i++;
                     }
                 }
             }
         }
     }
 
     /**
      * initalizes the hierarchy
      *
      */
     private void initHierarchy() {
         JBPropertyDescriptor propDes = null;
         IComponentIdentifier compId = getGuiNode().getTechnicalName();
         if (compId != null) {
             List hierarchy = compId.getHierarchyNames();
             for (int i = 0; i < hierarchy.size(); i++) {
                 if (i == 0) {
                     propDes = new JBPropertyDescriptor(
                             new ComponentHierarchyController(i), 
                             P_ELEMENT_DISPLAY_HIERARCHY);
                 } else {
                     propDes = new JBPropertyDescriptor(
                             new ComponentHierarchyController(i), 
                             StringConstants.EMPTY);
                 }
                 propDes.setCategory(P_ELEMENT_DISPLAY_COMPADDINFO);
                 addPropertyDescriptor(propDes);
             }
         }
     }
     /**
      * initalizes the context
      *
      */
     private void initContext() {
         JBPropertyDescriptor propDes = null;
         IComponentIdentifier compId = getGuiNode().getTechnicalName();
         if (compId != null) {
             List context = compId.getNeighbours();
             for (int i = 0; i < context.size(); i++) {
                 if (i == 0) {
                     propDes = new JBPropertyDescriptor(
                             new ComponentContextController(i), 
                             P_ELEMENT_DISPLAY_CONTEXT);
                 } else {
                     propDes = new JBPropertyDescriptor(
                             new ComponentContextController(i), 
                             StringConstants.EMPTY);
                 }
                 propDes.setCategory(P_ELEMENT_DISPLAY_COMPADDINFO);
                 addPropertyDescriptor(propDes);
             }
         }
     }
     /**
      * {@inheritDoc}
      */
     public boolean isPropertySet(Object id) {
         boolean isPropSet = false;
         return isPropSet;
     }
 
     /**
      * Class to control component name.
      *
      * @author BREDEX GmbH
      * @created 07.01.2005
      */
     private class ComponentNameController extends AbstractPropertyController {
         /**
          * {@inheritDoc}
          */
         public boolean setProperty(Object value) {
             return true;
         }
         
         /**
          * {@inheritDoc}
          */
         public Object getProperty() {
             IComponentIdentifier compId = getGuiNode().getTechnicalName();
             if (compId != null) {
                 if (compId.getComponentName() != null) {
                     return compId.getComponentName();
                 }
             }
             return StringConstants.EMPTY;
         }  
         
         /**
          * {@inheritDoc}
          */
         public Image getImage() {
             return IconConstants.TECHNICAL_NAME_IMAGE;
         }
     }
 
     /**
      * Class to control component name.
      *
      * @author BREDEX GmbH
      * @created 07.01.2005
      */
     private class ComponentClassController extends AbstractPropertyController {
         /**
          * {@inheritDoc}
          */
         public boolean setProperty(Object value) {
             return true;
         }
         
         /**
          * {@inheritDoc}
          */
         public Object getProperty() {
             IComponentIdentifier compId = getGuiNode().getTechnicalName();
             if (compId != null) {
                 if (compId.getComponentClassName() != null) {
                     return compId.getComponentClassName();
                 }
             }
             return StringConstants.EMPTY;
         }  
         
         /**
          * {@inheritDoc}
          */
         public Image getImage() {
             return DEFAULT_IMAGE;
         }
     }
 
     /**
      * Class to control component name.
      *
      * @author BREDEX GmbH
      * @created 07.01.2005
      */
     private class ComponentSuppClassController extends
         AbstractPropertyController {
         /**
          * {@inheritDoc}
          */
         public boolean setProperty(Object value) {
             return true;
         }
         
         /**
          * {@inheritDoc}
          */
         public Object getProperty() {
             IComponentIdentifier compId = getGuiNode().getTechnicalName();
             if (compId != null) {
                 if (compId.getSupportedClassName() != null) {
                     return compId.getSupportedClassName();
                 }
             }
             return StringConstants.EMPTY;
         }  
         
         /**
          * {@inheritDoc}
          */
         public Image getImage() {
             return DEFAULT_IMAGE;
         }
     }
 
     /**
      * Class to control component name.
      *
      * @author BREDEX GmbH
      * @created 07.01.2005
      */
     private class ComponentHierarchyController extends
         AbstractPropertyController {
         
         /** 
          * index of array
          */
         private int m_index = 0;
         
         /**
          * constructor
          * 
          * @param i
          *      int
          */
         public ComponentHierarchyController(int i) {
             m_index = i;
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean setProperty(Object value) {
             return true;
         }
         
         /**
          * {@inheritDoc}
          */
         public Object getProperty() {
             IComponentIdentifier compId = getGuiNode().getTechnicalName();
             if (compId != null) {
                 if (compId.getSupportedClassName() != null
                         && compId.getHierarchyNames().get(m_index) != null) {
                     return compId.getHierarchyNames().get(m_index);
                 }
             }
             return StringConstants.EMPTY;
         }  
         
         
         /**
          * {@inheritDoc}
          */
         public Image getImage() {
             return DEFAULT_IMAGE;
         }
     }
 
     /**
      * Class to control component context.
      *
      * @author BREDEX GmbH
      * @created 11.03.2008
      */
     private class ComponentContextController extends
         AbstractPropertyController {
         
         /** 
          * index of array
          */
         private int m_index = 0;
         
         /**
          * constructor
          * 
          * @param i
          *      int
          */
         public ComponentContextController(int i) {
             m_index = i;
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean setProperty(Object value) {
             return true;
         }
         
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("unchecked")
         public Object getProperty() {
             IComponentIdentifier compId = getGuiNode().getTechnicalName();
             if (compId != null) {
                 if (compId.getSupportedClassName() != null
                         && compId.getNeighbours().get(m_index) != null) {
                     List context = new ArrayList(compId.getNeighbours());
                     Collections.sort(context);
                     return context.get(m_index);
                 }
             }
             return StringConstants.EMPTY;
         }  
         
         
         /**
          * {@inheritDoc}
          */
         public Image getImage() {
             return DEFAULT_IMAGE;
         }
     }
     
     /**
      * Class to control ComponentPropertiesMap.
      *
      * @author BREDEX GmbH
      * @created 02.10.2013
      */
     private class ComponentPropertiesController extends
         AbstractPropertyController {
         
         /** 
          * index of array
          */
         private int m_index = 0;
         
         /**
          * constructor
          * 
          * @param i
          *      int
          */
         public ComponentPropertiesController(int i) {
             m_index = i;
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean setProperty(Object value) {
             return true;
         }
         
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("unchecked")
         public Object getProperty() {
             IComponentIdentifier compId = getGuiNode().getCompIdentifier();
             if (compId != null) {
                 Map<String, String> componentProperties = 
                         compId.getComponentPropertiesMap();
                 if (componentProperties != null) {
                     List values = 
                             new ArrayList<String>(componentProperties.values());
                     return values.get(m_index);
                 }
             }
             return StringConstants.EMPTY;
         }  
         
         
         /**
          * {@inheritDoc}
          */
         public Image getImage() {
             return DEFAULT_IMAGE;
         }
     }
 }
