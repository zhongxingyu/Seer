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
 package org.eclipse.jubula.tools.objects;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.TestDataConstants;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * This class holds the information for identifying a component in the AUT. <br>
  * 
  * Currently a component is identified by its type, e.g.
  * 'javax.swing.JTextfield' and the names of the container building the
  * hierarchy of the GUI of the AUT.
  * 
  
  * @author BREDEX GmbH
  * @created 27.08.2004
  */
 public class ComponentIdentifier implements Serializable, IComponentIdentifier {
     /**
      * Define the serialization ID to prevent
      * deserialization errors after changing
      * instance variables
      */
     static final long serialVersionUID = 1031;
 
     /** the logger */
     private static Logger log = LoggerFactory.getLogger(
             ComponentIdentifier.class);
     
     /**
      * the name of the class as which class is being handled as
      * for example myTextField would be TextField
      */
     private String m_supportedClassName = null;
     
     /**
      * a list of all neighbours of a component
      * this is some context information
      */
     private List m_neighbours = new ArrayList();
     
     /** the name of the class of the component, such as 'javax.swing.JButton' 
      *  this attribute decides how to test a class. So if class itself is not
      * testable, it will be superClass name.
      * */
     private String m_componentClassName = null;
     
     /**
      * the hierarchy for the component. The names of the container hierarchy
      * from top to (checkstyle :-) down, inclusive the component name itself
      */
     private List m_hierarchyNames = new ArrayList();
 
     /**
      * the alternative name for display of the component or null if the normal 
      * name returned by method getComponentName() has to be used as display name
      */
     private String m_alternativeDisplayName = null;
     
     /**
      * the <code>m_matchPercentage</code> when this component identifier has
      * been collected
      */
     private double m_matchPercentage = -1.0d;
 
     /**
      * the <code>m_numberOfOtherMatchingComponents</code> which may also be
      * likely to be found in future
      */
     private int m_numberOfOtherMatchingComponents = -1;
 
     /**
      * <code>m_equalOriginalFound</code> whether this component identifier could
      * be used to retrieve the original component on collection
      */
     private boolean m_equalOriginalFound = false;
     
     /**
      * public constructor <br>
      * 
      * initializes m_hierarchyNames
      */
     public ComponentIdentifier() {
         //
     }
     
     /**
      * @return Returns the componentClassName.
      */
     public String getComponentClassName() {
         return m_componentClassName;
     }
     
     /**
      * @param componentClassName The componentClassName to set.
      */
     public void setComponentClassName(String componentClassName) {
         m_componentClassName = componentClassName;
     }
     
     /**
      * @return the name of the component
      */
     public String getComponentName() {
         // return the last element
         try {
             if (m_hierarchyNames != null && m_hierarchyNames.size() > 0) {
                 return (String)m_hierarchyNames.get(
                         m_hierarchyNames.size() - 1);
             }
         } catch (ClassCastException cce) {
             log.error("unexpected element type", cce); //$NON-NLS-1$
         }
         return StringConstants.EMPTY;
     }
     
     /**
      * @param hierarchyNames
      *            The hierarchyNames to set. if null, the list will be cleared.
      */
     public void setHierarchyNames(List hierarchyNames) {
         if (hierarchyNames == null) {
             m_hierarchyNames = new ArrayList();
         } else {
             m_hierarchyNames = hierarchyNames;
         }
     }
     
     // getter and settor for serialisation 
     /**
      * @return Returns the hierarchyNames.
      */
     public List getHierarchyNames() {
         return m_hierarchyNames;
     }
     /**
      * @param hierarchyNames The hierarchyNames to add.
      */
     public void addHierarchyName(String hierarchyNames) {
         m_hierarchyNames.add(hierarchyNames);
     }
 
     /**
      * {@inheritDoc}
      */
     public String toString() {
         return new ToStringBuilder(this)
             .append("component type", m_componentClassName) //$NON-NLS-1$
             .append("alternative name", m_alternativeDisplayName) //$NON-NLS-1$
             .append("hierarchy", m_hierarchyNames) //$NON-NLS-1$
             .toString();    
     }
     
     /**
      * clears the hierarchynames list
      *
      */
     public void clearHierarchyNames() {
         m_hierarchyNames.clear();
     }
     
     /**
      * @return Clone of object
      */
     public IComponentIdentifier makeClone() {
         IComponentIdentifier clone = new ComponentIdentifier();
         clone.setHierarchyNames(new ArrayList(
             m_hierarchyNames));
         clone.setComponentClassName(m_componentClassName);
         if (m_supportedClassName != null) {
             clone.setSupportedClassName(m_supportedClassName);
         }
         if (m_neighbours != null) {
             clone.setNeighbours(new ArrayList(m_neighbours));
         }
         clone.setAlternativeDisplayName(m_alternativeDisplayName);
         return clone;
     }
     
     /**
      * @return Returns the supportedClassName.
      */
     public String getSupportedClassName() {
         return m_supportedClassName;
     }
     /**
      * @param supportedClassName The supportedClassName to set.
      */
     public void setSupportedClassName(String supportedClassName) {
         m_supportedClassName = supportedClassName;
     }
     /**
      * @return Returns the neighbours.
      */
     public List getNeighbours() {
         return m_neighbours;
     }
     /**
      * @param neighbours The neighbours to set.
      */
     public void setNeighbours(List neighbours) {
         m_neighbours = neighbours;
     }
 
     /**
      * @param neighbours The hierarchyNames to add.
      */
     public void addNeighbour(String neighbours) {
         m_neighbours.add(neighbours);
     }
 
     /**
      * generates a name for the component
      * @return String
      */
     public String generateLogicalName() {
         String returnVal = null;
         final String supportedClassName = getSupportedClassName();
         if (supportedClassName.lastIndexOf(".") > -1 //$NON-NLS-1$
                 && supportedClassName.length() > (supportedClassName.lastIndexOf(".") + 1)) { //$NON-NLS-1$
             returnVal = checkDefaultMapping(supportedClassName);
             if (returnVal != null) {
                 return returnVal;
             }
             
             returnVal = supportedClassName.substring(
                     supportedClassName.lastIndexOf(".") + 1) + "(";  //$NON-NLS-1$ //$NON-NLS-2$
         } else {
             returnVal = supportedClassName + "(";  //$NON-NLS-1$
         }
 
         StringBuffer hash = new StringBuffer();
         Iterator iter = getHierarchyNames().iterator();
         while (iter.hasNext()) {
             hash.append((String)iter.next()); 
         }
         returnVal += hash.toString().hashCode(); 
         returnVal += ")"; //$NON-NLS-1$
         return returnVal;
     }
     
     /**
      * Checks if the given supportedClassName has a Default-Mapping.
      * If it has, the logical name of the Default-Maping will be returned,
      * null otherwise.
      * @param supportedClassName the supported class name
      * @return the logical name or null.
      */
     private String checkDefaultMapping(String supportedClassName) {
         if (MappingConstants.SWING_APPLICATION_CLASSNAME.equals(
             supportedClassName)
             || MappingConstants.SWT_APPLICATION_CLASSNAME.equals(
                 supportedClassName)) { 
             
             return CompSystemI18n.getString(TestDataConstants
                 .APPLICATION_DEFAULT_MAPPING_I18N_KEY);
         } 
         if (MappingConstants.SWT_MENU_CLASSNAME.equals(supportedClassName)
             || MappingConstants.SWING_MENU_CLASSNAME
                 .equals(supportedClassName)) {
             
             return CompSystemI18n.getString(
                 TestDataConstants.MENU_DEFAULT_MAPPING_I18N_KEY);
         }
         return null;
     }
     
     /**
      * {@inheritDoc}
      */
     public String getAlternativeDisplayName() {
         return m_alternativeDisplayName;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setAlternativeDisplayName(String alternativeDisplayName) {
         m_alternativeDisplayName = alternativeDisplayName;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getComponentNameToDisplay() {
         final String componentNameToDisplay;
         if (m_alternativeDisplayName == null) {
             // no alternative name set, so use standard name
             componentNameToDisplay = getComponentName();
         } else {
             // use alternative name
             componentNameToDisplay = m_alternativeDisplayName;
         }
         
         return componentNameToDisplay;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean equals(Object obj) {
         if (obj instanceof IComponentIdentifier) {
             IComponentIdentifier compId = (IComponentIdentifier)obj;
             return new EqualsBuilder()
                .append(getHierarchyNames(), compId.getHierarchyNames())
                .append(getNeighbours(), compId.getNeighbours())
                .isEquals();
         }
         return super.equals(obj);
     }
 
     /**
      * {@inheritDoc}
      */
     public int hashCode() {
         return new HashCodeBuilder()
             .append(getHierarchyNames())
            .append(getNeighbours())
             .toHashCode();
     }
 
     /** {@inheritDoc} */
     public void setMatchPercentage(double matchPercentage) {
         m_matchPercentage = matchPercentage;
     }
 
     /** {@inheritDoc} */
     public double getMatchPercentage() {
         return m_matchPercentage;
     }
 
     /** {@inheritDoc} */
     public void setNumberOfOtherMatchingComponents(
             int numberOfOtherMatchingComponents) {
         m_numberOfOtherMatchingComponents = numberOfOtherMatchingComponents;
     }
 
     /** {@inheritDoc} */
     public int getNumberOfOtherMatchingComponents() {
         return m_numberOfOtherMatchingComponents;
     }
 
     /** {@inheritDoc} */
     public void setEqualOriginalFound(boolean equalOriginalFound) {
         m_equalOriginalFound = equalOriginalFound;
     }
 
     /** {@inheritDoc} */
     public boolean isEqualOriginalFound() {
         return m_equalOriginalFound;
     }
 }
