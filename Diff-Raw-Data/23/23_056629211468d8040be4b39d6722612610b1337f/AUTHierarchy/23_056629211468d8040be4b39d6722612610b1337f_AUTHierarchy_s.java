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
 package org.eclipse.jubula.rc.common.components;
 
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jubula.rc.common.AUTServerConfiguration;
 import org.eclipse.jubula.rc.common.Constants;
 import org.eclipse.jubula.rc.common.exception.UnsupportedComponentException;
 import org.eclipse.jubula.rc.common.implclasses.IBaseImplementationClass;
 import org.eclipse.jubula.rc.common.implclasses.IComponentFactory;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 
 
 /**
  * @author BREDEX GmbH
  * @created 31.08.2006
  */
 public abstract class AUTHierarchy {
     
     /** the logger */
     private static final AutServerLogger LOG = 
         new AutServerLogger(AUTHierarchy.class);
     
     
     /**
      * a hashtable to find the HierarchyContainer for a component from
      * the AUT: key=componentID, value=HierarchyContainer
      */
     private volatile Map m_hierarchyMap; 
     
     /**
      * a hashtable to find the HierarchyContainer for a component from
      * the AUT: key=component.getRealComponent, value=AUTComponent
      */
     private volatile Map m_realHierarchyMap; 
     
     /**
      * default constructor <br>
      * initializes the HashTables m_hierarchyMap and m_topLevelContainerMap;
      */
     public AUTHierarchy() {
         m_hierarchyMap = new Hashtable(Constants.INITIAL_CAPACITY_HIERARCHY);
         m_realHierarchyMap = new Hashtable(
                 Constants.INITIAL_CAPACITY_HIERARCHY);
     }
     
     /**
      * @return all hierarchyContainer
      */
     public Map getHierarchyMap() {
         return m_hierarchyMap;
     }
     
     /**
      * @return all AUTcomponent
      */
     protected Map getRealMap() {
         return m_realHierarchyMap;
     }
 
     /**
      * Adds a HiearchyContainer to the HierarchyMap
      * @param hierarchyContainer the hierarchyContainer to add
      */
     protected void addToHierachyMap(HierarchyContainer hierarchyContainer) {
         final AUTComponent autComponent = hierarchyContainer.getCompID();
         final Object realComponent = autComponent.getComp();
         m_hierarchyMap.put(autComponent, hierarchyContainer);
         m_realHierarchyMap.put(realComponent, autComponent);
         if (LOG.isDebugEnabled()) {
             LOG.debug("Add to HierarchyMap: " + String.valueOf(autComponent)); //$NON-NLS-1$
             LOG.debug("HierarchyMap size: " +  m_hierarchyMap.size()); //$NON-NLS-1$
             LOG.debug("Add to RealHierarchyMap: " + String.valueOf(realComponent)); //$NON-NLS-1$
             LOG.debug("RealHierarchyMap size: " +  m_realHierarchyMap.size()); //$NON-NLS-1$
         }
     }
     
     /**
      * Removes a HiearchyContainer from the HierarchyMap.
      * @param hierarchyContainer the hierarchContainer to remove
      */
     protected void removeFromHierachyMap(
             HierarchyContainer hierarchyContainer) {
        
         AUTComponent autComponent = hierarchyContainer.getCompID();
         Object realComponent = autComponent.getComp();
         m_hierarchyMap.remove(autComponent);
         m_realHierarchyMap.remove(realComponent);
     }
     
     /**
      * This method may be used for a component with a default mapping. It
      * creates a component instance using the passed factory, creates a new
      * <code>SwingHierarchyContainer</code> and adds the container to the internal
      * hierarchy map. The technical name is set as the name property of the
      * <code>SwingHierarchyContainer</code>.
      * @param factory The factory to create a new component
      * @param componentName The name of the component to create
      * @param technicalName The technical name of the component
      * @throws UnsupportedComponentException If the factory fails creating the component
      */
     public abstract void addToHierarchy(IComponentFactory factory, 
             String componentName,
             String technicalName) 
         throws UnsupportedComponentException;
     
     /**
      * returns an array of all component identifier of (supported) components,
      * which are currently instantiated by the AUT.
      * @attention synchronized in subclasses!!!
      * @return array with ComponentIdentifier, may be empty but never null
      */
     public abstract IComponentIdentifier[] getAllComponentId();
     
     
     /**
      * Creates a name for the given component and the given postFix. <br>
      * The class name of the component is used.
      * 
      * @param component the component to create a name for
      * @param postFix a string to append
      * @return the new name
      */
     protected String createName (Object component, int postFix) {
         return component.getClass().getName()
             + Constants.CLASS_NUMBER_SEPERATOR + postFix;
     }
     
     /**
      * Creates a name for the given component and the given postFix. <br>
      * The class name of the component is used.
      * 
      * @param componentName the original comp name
      * @param postFix a string to append
      * @return the new name
      */
     protected String createName (String componentName, int postFix) {
         return componentName + Constants.CLASS_NUMBER_SEPERATOR + postFix;
     }
     
     /**
      * @return all hierarchyContainer of the hierarchyMap
      */
     protected Collection getHierarchyValues() {
         return m_hierarchyMap.values();
     }
     
     /**
      * returns the conext of a component. If there are other components in the
      * same container, their names will be added.
      * @param component component
      * @return List
      */
     protected abstract List getComponentContext(Object component);
     
     /**
      * @param container the hierarchy container that corresponds to component
      * @param component the gui component that corresponds to the identifier
      * @param identifier the identifier to set alternative display name on it
      */
     protected final void setAlternativeDisplayName(HierarchyContainer container,
             Object component, IComponentIdentifier identifier) {
         if (container != null) {
             String displayName = buildDisplayName(container, component);
             if ((displayName != null) 
                 && (!displayName.equals(identifier.getComponentName()))) {
                 
                 identifier.setAlternativeDisplayName(displayName);
             }
         }
     }
     
     /**
      * @param container the hierarchy container that corresponds to component
      * @param component the gui component to build a display name for
      * @return display name to be used for the gui component
      */
     private final String buildDisplayName(HierarchyContainer container,
             Object component) {
         String containerName = container.getName();
         boolean containerNameGenerated = container.isNameGenerated();        
         String[] componentTextArray = getTextArrayFromComponent(component);
         String improvedName = buildImprovedName(componentTextArray);
         if (containerNameGenerated) {
             containerName = AUTServerConfiguration.getInstance()
                 .getGDComponentName(component);
         }
         String displayName = combineNames(containerName, containerNameGenerated,
                 improvedName);        
         return displayName;
     }
 
     /**
      * @param component the gui component to get the string array from
      * @return string array from the given gui component
      */
     private String[] getTextArrayFromComponent(Object component) {
         String[] componentTextArray = null;
         if (component != null) {
             try {
                 Object implClass = AUTServerConfiguration
                     .getInstance().prepareImplementationClass(
                         component, component.getClass());
                 if (implClass instanceof IBaseImplementationClass) {
                     IBaseImplementationClass baseImpl = 
                         (IBaseImplementationClass)implClass;
                     componentTextArray = baseImpl.getTextArrayFromComponent();
                 }
             } catch (UnsupportedComponentException uce) {
                 LOG.warn(uce);
             } catch (IllegalArgumentException iae) {
                 LOG.error(iae);
             }
         }
         return componentTextArray;
     }
 
     /**
      * @param componentTextArray the array containing none, one or many texts
      *                           representing a componnent
      * @return improved name builded from componentTextArray
      */
     private String buildImprovedName(String[] componentTextArray) {
         final String improvedName;        
         if ((componentTextArray == null) 
             || (componentTextArray.length == 0) 
             || ((componentTextArray.length == 1) 
                 && ((componentTextArray[0] == null)
                     || (componentTextArray[0].length() == 0)))) {
             // improved name is unavailable
             improvedName = null;
         } else {
             StringBuffer buffer = new StringBuffer();
             for (int i = 0; i < componentTextArray.length; i++) {
                 if (i > 0) {
                     buffer.append(","); //$NON-NLS-1$
                     if (i == 3) {
                         // do not show more than three component texts
                         buffer.append("..."); //$NON-NLS-1$
                         break;
                     }
                 }
                 buffer.append(StringConstants.QUOTE);
                 if (componentTextArray[i] != null) {
                     buffer.append(String.valueOf(componentTextArray[i]).trim());
                 }
                 buffer.append(StringConstants.QUOTE);
             }
             improvedName = buffer.toString();
         }
         
         return improvedName;
     }
 
     /**
      * @param containerName the container name to be combined with improved name
      * @param containerNameGenerated indicates whether container name is a 
      *                               generated name
      * @param improvedName the improved name to be combined with container name
      * @return combination of containerName and improvedName
      */
     private String combineNames(String containerName,
             boolean containerNameGenerated, String improvedName) {
         String displayName;
         if (improvedName == null) {
             // show only normal name, because improved name is unavailable
             displayName = containerName;
         } else {
             // show normal name and improved name in brackets
             displayName = containerName + " (" + improvedName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
         }
         if (displayName != null 
                 && displayName.length() > Constants.REC_MAX_NAME_LENGTH) {
             displayName = displayName.substring(0, Constants.REC_MAX_NAME_LENGTH - 1) + "..."; //$NON-NLS-1$
             if (improvedName != null) {
                 displayName = displayName + ")"; //$NON-NLS-1$
             }
         }
         return displayName;
     }
 
 }
