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
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.persistence.Basic;
 import javax.persistence.CollectionTable;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.MapKeyColumn;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 import org.apache.commons.lang.LocaleUtils;
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.tools.constants.StringConstants;
 
 
 /**
  * utility class for management of values or references 
  *
  * @author BREDEX GmbH
  * @created 08.12.2004
  */
 @Entity
 @Table(name = "TEST_DATA")
 class TestDataPO implements ITestDataPO {
     /** hibernate OID */
     private transient Long m_id = null;
     
     /** maps languages (locales) to string values
      * key: Locale (string representation), value: value as string
      */
     private Map<String, String> m_map = new HashMap<String, String>();
 
     /** hibernate version id */
     private transient Integer m_version = null;
     
     /** The ID of the parent project */
     private Long m_parentProjectId = null;
 
     /**
      * constructor for TestDataPO with value
      * @param languageToValue The initial values for the created object.
      */
     TestDataPO(Map<Locale, String> languageToValue) {
         for (Locale language : languageToValue.keySet()) {
             m_map.put(language.toString(), languageToValue.get(language));
         }
     }
     
     /**
      *  constructor only for hibernate
      */
     TestDataPO() {
         // only for hibernate
     }
     
     /**
      *  
      * @return Returns the id.
      */
     @Id
     @GeneratedValue
     public Long getId() {
         return m_id;
     }
     /**
      * @param id The id to set.
      */
     void setId(Long id) {
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
     * Overides Object.equals()
     * Compares this TestDataPO object to the given object to equality.
     * @param obj the object to compare.
     * @return true or false
     * {@inheritDoc}
     */
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj instanceof TestDataPO) {
             TestDataPO otherTestData = (TestDataPO)obj;
             return getMap().equals(otherTestData.getMap());
         }
         return false;
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     public int hashCode() {
         return getMap().hashCode();
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
      * @return empty string
      */
     @Transient
     public String getName() {
         return StringConstants.EMPTY;
     }
     /**
      * Creates a deep copy of this instance.
      * 
      * @return The new test data instance
      */
     public ITestDataPO deepCopy() {
         TestDataPO td = new TestDataPO();
         td.getMap().putAll(getMap());
         td.setParentProjectId(getParentProjectId());
         return td;
     }
 
     /**
      * only for hibernate
      * 
      * @return Returns the map.
      */
     @ElementCollection(fetch = FetchType.EAGER)
     @CollectionTable(name = "LOCALE_TO_TD")
     @MapKeyColumn(name = "LOCALE")
     @Column(name = "TD_VALUE", length = 4000)
     @JoinColumn(name = "I18N_STR")
     private Map<String, String> getMap() {
         return m_map;
     }
     /**
      * only for hibernate
      * @param map The map to set.
      */
     void setMap(Map<String, String> map) {
         m_map = map;
     }
     
     /**
      * set the value for the given language
      * @param lang language, for which to set the value
      * @param value value
      * @param project associated project
      */
     public void setValue(Locale lang, String value, IProjectPO project) {
         if (validateLang(lang, project)) {
             setValue(lang, value);
             setParentProjectId(project.getId());
         }
     }
     
     /**
      * set the value for the given language
      * @param lang language, for which to set the value
      * @param value value
      */
     private void setValue(Locale lang, String value) {
         if (value != null && value.length() != 0) {
             getMap().put(lang.toString(), value);
         } else {
            getMap().remove(lang.toString());
         }
     }
     
     /**
      * @param lang language to validate
      * @param project associated project, for which to use an I18N string object
      * @return flag, if the given language is a supported language inside of
      * actual project
      */
     private boolean validateLang(Locale lang, IProjectPO project) {
         return project.getLangHelper().containsItem(lang);
     }
 
     /**
      * get the value for a given locale
      * @param lang language, for which to get the value
      * @return value
      */
     public String getValue(Locale lang) {
         Validate.notNull(lang);
         return getMap().get(lang.toString());
     }
     
     /**
      * @return a set of all Locale's used in this I18NString
      */
     @Transient
     public Set<Locale> getLanguages() {
         Set<Locale> supportedLocales = new java.util.HashSet<Locale>();
         for (String localeCode : getMap().keySet()) {
             supportedLocales.add(LocaleUtils.toLocale(localeCode));
         }
         
         return supportedLocales;
     }
 
     /**
      * {@inheritDoc}
      * @return
      */
     public String toString() {
         SortedSet<String> sorter = new TreeSet<String>();
         for (Entry<String, String> entry : getMap().entrySet()) {
             sorter.add(entry.getKey() + StringConstants.COLON 
                 + entry.getValue());
         }
         StringBuilder result = new StringBuilder(sorter.size() * 100);
         for (String line : sorter) {
             result.append(line);
             result.append(StringConstants.NEWLINE);
         }
         return result.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     public void setData(ITestDataPO testData) {
         for (Locale language : testData.getLanguages()) {
             setValue(language, testData.getValue(language));
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void clear() {
         m_map.clear();
     }
 }
