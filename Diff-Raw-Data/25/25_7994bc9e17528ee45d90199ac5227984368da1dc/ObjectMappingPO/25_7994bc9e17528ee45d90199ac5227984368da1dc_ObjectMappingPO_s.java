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
 package org.eclipse.jubula.client.core.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.ObjectMappingEventDispatcher;
 import org.eclipse.jubula.client.core.i18n.Messages;
import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.objects.ComponentIdentifier;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 import org.eclipse.persistence.annotations.BatchFetch;
 import org.eclipse.persistence.annotations.BatchFetchType;
 
 
 /** 
  * 
  * @author BREDEX GmbH
  * @created 07.04.2005
  */
 @Entity
 @Table(name = "OBJ_MAP")
 class ObjectMappingPO implements IObjectMappingPO {
     /** Persistence (JPA / EclipseLink) OID */
     private transient Long m_id = null;
     /** Persistence (JPA / EclipseLink) version id */
     private transient Integer m_version = null;
     /** The ID of the parent project */
     private Long m_parentProjectId = null;
     /** The timestamp */
     private long m_timestamp = 0;
     /** the profile used for this object map */
     private IObjectMappingProfilePO m_profile = null;
     /** the top-level category for mapped components */
     private IObjectMappingCategoryPO m_mappedCategory;
     /** the top-level category for unmapped component names */
     private IObjectMappingCategoryPO m_unmappedLogicalCategory;
     /** the top-level category for unmapped technical names */
     private IObjectMappingCategoryPO m_unmappedTechnicalCategory;
     /** cache for mappings */
     private Set<IObjectMappingAssoziationPO> m_mappings;
     /** faster assoc lookup */
     private Map<String, IObjectMappingAssoziationPO> m_logicalNameToAssoc;
     
     /** default constructor (for Persistence (JPA / EclipseLink)) */
     @SuppressWarnings("unused")
     private ObjectMappingPO() {
         m_logicalNameToAssoc = new HashMap<String, IObjectMappingAssoziationPO>(
                 1001);
     }
 
     /**
      * Constructor
      * 
      * @param mappedCategory
      *              The top-level category for mapped components.
      * @param unmappedLogicalCategory
      *              The top-level category for unmapped component names.
      * @param unmappedTechnicalCategory
      *              The top-level category for unmapped technical names.
      */
     ObjectMappingPO(IObjectMappingCategoryPO mappedCategory,
             IObjectMappingCategoryPO unmappedLogicalCategory,
             IObjectMappingCategoryPO unmappedTechnicalCategory) {
         m_mappedCategory = mappedCategory;
         m_unmappedLogicalCategory = unmappedLogicalCategory;
         m_unmappedTechnicalCategory = unmappedTechnicalCategory;
         m_logicalNameToAssoc = new HashMap<String, IObjectMappingAssoziationPO>(
                 1001);
     }
     
     /**
      * tries to assign a logical(perhaps existing) to a technical(perhaps existing)
      * @param logic         logical name
      * @param technical     technical name
      * @return          ObjectMappingAssoziationPO
      */
     public IObjectMappingAssoziationPO addObjectMappingAssoziation(String logic,
         IComponentIdentifier technical) {
         IObjectMappingAssoziationPO oma = assignLogicalToTechnicalName(logic, 
             technical);
 
         IObjectMappingCategoryPO assocSection = oma.getSection();
         IObjectMappingCategoryPO mappedSection = getMappedCategory();
         if (mappedSection == null || !mappedSection.equals(assocSection)) {
             // Association is not already in the "mapped" section.
             // This is because either:
             //  a. the association is new, or
             //  b. the association is currently "unmapped"
             IObjectMappingCategoryPO catToCreateIn = 
                 ObjectMappingEventDispatcher.getCategoryToCreateIn();
 
             // Remove the association from its current category, if it has one
             IObjectMappingCategoryPO currentCategory = oma.getCategory();
             if (currentCategory != null) {
                 currentCategory.removeAssociation(oma);
             }
             if (catToCreateIn != null) {
                 catToCreateIn.addAssociation(oma);
             } else {
                 getMappedCategory().addAssociation(oma);
             }
         }
         
         oma.setParentProjectId(getParentProjectId());
         return oma;
     }    
 
     /**
      * Creates a new logical Name, unassigned fireing Added Event 
      * @param compNameGuid     compNameGuid
      * @return          ObjectMappingAssoziationPO
      */
     public IObjectMappingAssoziationPO addLogicalName(String compNameGuid) {
         
         IObjectMappingAssoziationPO assoc = 
             getLogicalNameAssoc(compNameGuid);
         if (assoc == null) {
             assoc = PoMaker.createObjectMappingAssoziationPO(
                     null, compNameGuid);
             getUnmappedLogicalCategory().addAssociation(assoc);
             assoc.setParentProjectId(getParentProjectId());
             return assoc;
         }
         
         assoc.setParentProjectId(getParentProjectId());
         return assoc;
     }
 
     /**
      * Creates a new technical Name, unassigned
      * @param tech   ComponentIdentifier
      * @param aut AUTMainPO
      * @return ObjectMappingAssoziationPO
      */
     public IObjectMappingAssoziationPO 
     addTechnicalName(IComponentIdentifier tech, IAUTMainPO aut) {
         if (!existTechnicalName(tech)) {
             IObjectMappingAssoziationPO asso = PoMaker
                 .createObjectMappingAssoziationPO(tech);
             if (aut != null) {
                 IObjectMappingCategoryPO categoryToCreateIn =
                     ObjectMappingEventDispatcher.getCategoryToCreateIn();
                 if (categoryToCreateIn != null) {
                     categoryToCreateIn.addAssociation(asso);
                 } else {
                     getUnmappedTechnicalCategory().addAssociation(asso);
                 }
             }
             asso.setParentProjectId(getParentProjectId());
             return asso;
         }
         return null;
     } 
 
     /**
      * tries to assign a logical(perhaps existing) to a technical(perhaps existing) 
      * @param logic         logical name
      * @param technical     technical name
      * @return          ObjectMappingAssoziationPO
      */
     public IObjectMappingAssoziationPO assignLogicalToTechnicalName(
         String logic, IComponentIdentifier technical) {
         IObjectMappingAssoziationPO oma = null;
         if (existLogicalName(logic)) {
             oma = getLogicalNameAssoc(logic);
             oma.removeLogicalName(logic);
             if (oma.getLogicalNames().size() == 0
                 && oma.getTechnicalName() == null) {
                 getMappings().remove(oma);
             }
         }
         if (!existTechnicalName(technical)) {
             oma = PoMaker.createObjectMappingAssoziationPO(technical, logic);
         } else {
             for (IObjectMappingAssoziationPO assoc : getMappings()) {
                 oma = assoc;
                 if (technical.equals(oma.getTechnicalName())) {
                     oma.addLogicalName(logic);
                     break;
                 }
             }
         }
         oma.setParentProjectId(getParentProjectId());
         return oma;
     }
 
     /**
      * Check if a technical name exists
      * 
      * @param technical
      *            technical name
      * @return boolean
      */
     public boolean existTechnicalName(IComponentIdentifier technical) {
         for (IObjectMappingAssoziationPO oma : getMappings()) {
             if (technical.equals(oma.getTechnicalName())) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Check if a logical name exists 
      * @param logical   logical name
      * @return          boolean
      */
     private boolean existLogicalName(String logical) {
         return getLogicalNameAssoc(logical) != null;
     }
 
     /**
      * 
      * @param compNameGuid The GUID of the Component Name for which to find the
      *                     association.
      * @return the Association for the Component Name with the given GUID, or
      *         <code>null</code> if no such Association exists in this Object
      *         Mapping.
      */
     public IObjectMappingAssoziationPO getLogicalNameAssoc(
             String compNameGuid) {
         IObjectMappingAssoziationPO res = 
             m_logicalNameToAssoc.get(compNameGuid);
         if (res != null) {
             return res;
         }
         for (IObjectMappingAssoziationPO assoc : getMappings()) {
             int pos = assoc.getLogicalNames().indexOf(compNameGuid);
             if (pos != -1) {
                 m_logicalNameToAssoc.put(compNameGuid, assoc);
                 return assoc;
             }
         }
         return null;
     }
 
     /**
      * gives back the number of technical names
      * @return int
      */
     @Transient
     public int getTechnicalNamesSize() {
         int size = 0;
         for (IObjectMappingAssoziationPO oma : getMappings()) {
             if (oma.getTechnicalName() != null) {
                 size++;
             }
         }
         return size;
     }
 
     /**
      * returns the technicalName to a logical name
      * @param logical       String
      * @return              String
      * @throws LogicComponentNotManagedException error
      */
     @SuppressWarnings("unchecked")
     public IComponentIdentifier getTechnicalName(String logical) throws
         LogicComponentNotManagedException {
         IObjectMappingAssoziationPO asso = 
             getLogicalNameAssoc(logical);
         if (asso != null) {
             IComponentIdentifier assoCompId = asso.getTechnicalName();
             if (assoCompId != null) {
                 IComponentIdentifier compId = new ComponentIdentifier();
                 compId.setComponentClassName(
                     assoCompId.getComponentClassName());
                 compId.setHierarchyNames(new ArrayList<String>(
                     assoCompId.getHierarchyNames()));
                 compId.setNeighbours(new ArrayList<String>(
                         assoCompId.getNeighbours()));
                 compId.setSupportedClassName(
                     assoCompId.getSupportedClassName());
                 return compId;
             }
             return null; 
         } 
     
         // Check for default mapping
         IComponentNamePO compNamePo = 
             ComponentNamesBP.getInstance().getCompNamePo(logical);
         if (compNamePo != null) {
             String name = compNamePo.getName();
             asso = getLogicalNameAssoc(name);
             if (asso != null) {
                 IComponentIdentifier assoCompId = asso.getTechnicalName();
                 if (assoCompId != null) {
                     IComponentIdentifier compId = new ComponentIdentifier();
                     compId.setComponentClassName(
                             assoCompId.getComponentClassName());
                     compId.setHierarchyNames(new ArrayList<String>(
                             assoCompId.getHierarchyNames()));
                     compId.setNeighbours(new ArrayList<String>(
                             assoCompId.getNeighbours()));
                     compId.setSupportedClassName(
                             assoCompId.getSupportedClassName());
                     return compId;
                 }
                 return null; 
             }
         }
         
         throw new LogicComponentNotManagedException(
            Messages.TheLogicComponent + StringConstants.SPACE
            + StringConstants.APOSTROPHE + logical 
            + StringConstants.APOSTROPHE + StringConstants.SPACE
            + Messages.IsNotManaged + StringConstants.DOT,
             MessageIDs.E_COMPONENT_NOT_MANAGED); 
     }
 
     /**
      * @return      Set
      */
     @Transient
     public Set<IObjectMappingAssoziationPO> getMappings() {
         if (m_mappings == null) {
             /*
              * Keep in mind the the getXXXCategory() calls will clear the
              * mappings cache by setting m_mapping to null. To prevent
              * a NPE we can't use m_mapping while building the map. 
              */
             Set<IObjectMappingAssoziationPO> mappings = 
                 new HashSet<IObjectMappingAssoziationPO>();
             addAssociations(mappings, getMappedCategory());
             addAssociations(mappings, getUnmappedLogicalCategory());
             addAssociations(mappings, getUnmappedTechnicalCategory());
             m_mappings = mappings;
         }
         return m_mappings;
     }
     
     /**
      * clears the precomputed mappings cache
      */
     private void clearMappingsCache() {
         m_logicalNameToAssoc.clear();
         m_mappings = null;
     }
 
     /**
      * Recursively adds all associations from <code>category</code> and all
      * subcategories to <code>assocSet</code>.
      * 
      * @param assocSet The collection to which the associations will be added.
      * @param category The category in which to search for associations to add.
      */
     private void addAssociations(Set<IObjectMappingAssoziationPO> assocSet, 
             IObjectMappingCategoryPO category) {
         Validate.noNullElements(category.getUnmodifiableAssociationList());
         assocSet.addAll(category.getUnmodifiableAssociationList());
         for (IObjectMappingCategoryPO subcategory 
                 : category.getUnmodifiableCategoryList()) {
             addAssociations(assocSet, subcategory);
         }
     }
     
     /**
      * only for Persistence (JPA / EclipseLink)
      * 
      * @return Returns the id.
      */
     @Id
     @GeneratedValue
     public Long getId() {
         return m_id;
     }
     /**
      * only for Persistence (JPA / EclipseLink)
      * @param id The id to set.
      */
     @SuppressWarnings("unused")
     private void setId(Long id) {
         m_id = id;
     }
     
     /**
      *    
      * {@inheritDoc}
      */
     @Transient
     public Long getParentProjectId() {
         return getHbmParentProjectId();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void setParentProjectId(Long projectId) {
         setHbmParentProjectId(projectId);
         for (IObjectMappingAssoziationPO assoPO : getMappings()) {
             assoPO.setParentProjectId(projectId);
         }
     }
 
     /**
      *    
      * {@inheritDoc}
      */
     @Basic
     @Column(name = "PARENT_PROJ")
     Long getHbmParentProjectId() {
         return m_parentProjectId;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     void setHbmParentProjectId(Long projectId) {
         m_parentProjectId = projectId;
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     @Version
     public Integer getVersion() {        
         return m_version;
     }
 
     /**
      * @param version version
      */
     @SuppressWarnings("unused")
     private void setVersion(Integer version) {
         m_version = version;
     }
     
     
     /**
      * {@inheritDoc}
      * @return string representation of this object
      */
     @Transient
     public String getName() {
         return this.toString();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Basic
     @Column(name = "TIMESTAMP")
     public long getTimestamp() {
         return m_timestamp;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setTimestamp(long timestamp) {
         m_timestamp = timestamp;
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     @Transient
     public IObjectMappingProfilePO getProfile() {
         return getHbmProfile();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void setProfile(IObjectMappingProfilePO profile) {
         setHbmProfile(profile);
     }
 
     /**
      *      
      * @return the profile being used for this object map.
      */
     @ManyToOne(targetEntity = ObjectMappingProfilePO.class, optional = false, 
                cascade = CascadeType.ALL)
     @JoinColumn(name = "FK_PROFILE")
     private IObjectMappingProfilePO getHbmProfile() {
         return m_profile;
     }
 
     /**
      * 
      * @param profile the profile that this object map will use.
      */
     private void setHbmProfile(IObjectMappingProfilePO profile) {
         m_profile = profile;
     }
 
     /**
      *  
      * {@inheritDoc}
      */
     @OneToOne(targetEntity = ObjectMappingCategoryPO.class, 
               cascade = CascadeType.ALL, fetch = FetchType.EAGER, 
               optional = false)
     @BatchFetch(value = BatchFetchType.JOIN)
     public IObjectMappingCategoryPO getMappedCategory() {
         clearMappingsCache(); // may be changed outside of class
         return m_mappedCategory;
     }
 
     /**
      * For Persistence (JPA / EclipseLink).
      * 
      * @param mappedCategory The new top-level category for mapped components.
      */
     @SuppressWarnings("unused")
     private void setMappedCategory(IObjectMappingCategoryPO mappedCategory) {
         m_mappedCategory = mappedCategory;
         clearMappingsCache();
     }
     
     /**
      *  
      * {@inheritDoc}
      */
     @OneToOne(targetEntity = ObjectMappingCategoryPO.class, 
               cascade = CascadeType.ALL, fetch = FetchType.EAGER, 
               optional = false)
     @BatchFetch(value = BatchFetchType.JOIN)
     public IObjectMappingCategoryPO getUnmappedLogicalCategory() {
         clearMappingsCache(); // may be changed outside of class
         return m_unmappedLogicalCategory;
     }
 
     /**
      * For Persistence (JPA / EclipseLink).
      * 
      * @param unmappedLogicalCategory The new top-level category for 
      *                                unmapped component names.
      */
     @SuppressWarnings("unused")
     private void setUnmappedLogicalCategory(
             IObjectMappingCategoryPO unmappedLogicalCategory) {
         m_unmappedLogicalCategory = unmappedLogicalCategory;
         clearMappingsCache();
     }
 
     /**
      *  
      * {@inheritDoc}
      */
     @OneToOne(targetEntity = ObjectMappingCategoryPO.class, 
               cascade = CascadeType.ALL, fetch = FetchType.EAGER, 
               optional = false)
     @BatchFetch(value = BatchFetchType.JOIN)
     public IObjectMappingCategoryPO getUnmappedTechnicalCategory() {
         clearMappingsCache(); // may be changed outside of class
         return m_unmappedTechnicalCategory;
     }
 
     /**
      * For Persistence (JPA / EclipseLink).
      * 
      * @param unmappedTechnicalCategory The new top-level category for 
      *                                  unmapped technical names.
      */
     @SuppressWarnings("unused")
     private void setUnmappedTechnicalCategory(
             IObjectMappingCategoryPO unmappedTechnicalCategory) {
         m_unmappedTechnicalCategory = unmappedTechnicalCategory;
         clearMappingsCache();
     }
 
 }
