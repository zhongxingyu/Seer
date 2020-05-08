 /*******************************************************************************
  * Copyright (c) 2012 David Harrison, Triptech Ltd.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  *
  * Contributors:
  *     David Harrison, Triptech Ltd - initial API and implementation
  ******************************************************************************/
 package com.sfs.metahive.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.PreUpdate;
 import javax.persistence.Transient;
 import javax.persistence.TypedQuery;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.annotations.Index;
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 
 import com.sfs.metahive.CalculationParser;
 import com.sfs.metahive.web.model.DefinitionFilter;
 
 /**
  * The Class Definition.
  */
 @RooJavaBean
 @RooEntity(finders = { "findDefinitionsByNameLike" })
 public class Definition {
 
     /** The name. */
     @NotNull
     @Index(name="definitionName")
     @Size(min = 1, max = 100)
     @Column(unique = true)
     private String name;
 
     /** The definition type. */
     @NotNull
     @Index(name="definitionDefinitionType")
     @Enumerated(EnumType.STRING)
     private DefinitionType definitionType = DefinitionType.STANDARD;
 
     /** The data type. */
     @NotNull
     @Index(name="definitionDataType")
     @Enumerated(EnumType.STRING)
     private DataType dataType;
 
     /** The calculation as an HTML string. e.g.
      * <span class="variable">D10</span> + <span class="variable">D11</span> */
     @Index(name="definitionCalculation")
     @Lob
     private String calculation;
 
     /** The key value generator. */
     @NotNull
     @Enumerated(EnumType.STRING)
     private KeyValueGenerator keyValueGenerator;
 
     /** The key value access. */
     @NotNull
     @Enumerated(EnumType.STRING)
     private UserRole keyValueAccess;
 
     /** The definition descriptions. */
     @OrderBy("created DESC")
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "definition")
     private List<Description> descriptions = new ArrayList<Description>();
 
     /** The data sources. */
     @OrderBy("organisation ASC")
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "definition")
     private List<DataSource> dataSources = new ArrayList<DataSource>();
 
     /** The category. */
     @ManyToOne
     @Index(name="definitionCategory")
     @NotNull
     private Category category;
 
     /** The applicability. */
     @NotNull
     @Enumerated(EnumType.STRING)
     private Applicability applicability;
 
     /** The summary definition. */
     @ManyToOne
     @Index(name="definitionSummaryDefinition")
     private Definition summaryDefinition;
 
     /** The summarised definitions. */
     @OrderBy("name ASC")
     @OneToMany(mappedBy = "summaryDefinition")
     private List<Definition> summarisedDefinitions = new ArrayList<Definition>();
 
     /** The comments. */
     @OrderBy("created ASC")
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "definition")
     private List<Comment> comments = new ArrayList<Comment>();
 
     /** The show expanded flag. */
     @Transient
     boolean expanded = false;
 
 
     /**
      *The pre-update method.
      */
     @PreUpdate
     public final void preUpdate() {
         if (this.definitionType != DefinitionType.SUMMARY) {
            if (this.summarisedDefinitions != null) {
                for (Definition def : this.summarisedDefinitions) {
                    def.setSummaryDefinition(null);
                    def.persist();
                }
            }
         }
 
         if (this.definitionType != DefinitionType.STANDARD) {
             if (this.dataSources != null) {
                 for (DataSource ds : this.dataSources) {
                     ds.remove();
                 }
             }
         }
     }
 
     /**
      * Gets the calculation.
      *
      * @return the calculation
      */
     public final String getCalculation() {
         if (this.definitionType != DefinitionType.CALCULATED) {
             this.calculation = "";
         }
         return this.calculation;
     }
 
     /**
      * Gets the marked up calculation.
      *
      * @return the marked up calculation
      */
     public final String getPlainTextCalculation() {
         String plainText = StringUtils.replace(this.getCalculation(), "</span>", "");
         return StringUtils.replace(plainText, "<span class=\"variable\">", "");
     }
 
     /**
      * Test calculation.
      *
      * @return the string
      */
     public final String testCalculation() {
 
         StringBuilder result = new StringBuilder();
 
         String calc = this.getPlainTextCalculation();
 
         if (StringUtils.isNotBlank(this.getCalculation())) {
             Set<Long> variableIds = CalculationParser.parseVariableIds(calc);
 
             Map<Long, Double> values = new HashMap<Long, Double>();
 
             for (Long id : variableIds) {
                 // Assume a value of 2 for all variable ids
                 values.put(id, 2d);
             }
 
             result.append(CalculationParser.buildCalculation(calc, values));
             result.append(" = ");
             result.append(CalculationParser.performCalculation(calc, values));
         }
         return result.toString();
     }
 
     /**
      * Gets the description.
      *
      * @return the description
      */
     public final Description getDescription() {
         Description description = null;
 
         if (getDescriptions() != null && getDescriptions().size() > 0) {
             description = getDescriptions().get(0);
         }
         return description;
     }
 
     /**
      * Gets the calculated definitions.
      *
      * @return the calculated definitions
      */
     public final List<Definition> getCalculatedDefinitions() {
 
         List<Definition> calculatedDefinitions = new ArrayList<Definition>();
 
         if (StringUtils.isNotBlank(this.getCalculation())) {
             Set<Long> ids = CalculationParser.parseVariableIds(
                     this.getPlainTextCalculation());
             for (Long id : ids) {
                 Definition def = Definition.findDefinition(id);
                 calculatedDefinitions.add(def);
             }
         }
         return calculatedDefinitions;
     }
 
     /**
      * Gets the summarised definitions.
      *
      * @return the summarised definitions
      */
     public final List<Definition> getSummarisedDefinitions() {
         if (this.summarisedDefinitions == null) {
             this.summarisedDefinitions = new ArrayList<Definition>();
         }
         if (this.definitionType != DefinitionType.SUMMARY
                 && this.summarisedDefinitions.size() > 0) {
             this.summarisedDefinitions = new ArrayList<Definition>();
         }
         return this.summarisedDefinitions;
     }
 
     /**
      * Adds a summarised definition.
      *
      * @param definition the definition
      */
     public final void addSummarisedDefinition(Definition definition) {
         definition.setSummaryDefinition(this);
         getSummarisedDefinitions().add(definition);
     }
 
     /**
      * Adds a data source.
      *
      * @param dataSource the data source
      */
     public final void addDataSource(DataSource dataSource) {
         dataSource.setDefinition(this);
         getDataSources().add(dataSource);
     }
 
     /**
      * Adds a description.
      *
      * @param description the description
      */
     public final void addDescription(Description description) {
         description.setDefinition(this);
         getDescriptions().add(description);
     }
 
     /**
      * Adds a comment.
      *
      * @param comment the comment
      */
     public final void addComment(Comment comment) {
         comment.setDefinition(this);
         getComments().add(comment);
     }
 
     /**
      * The toString method.
      */
     public final String toString() {
         return getName();
     }
 
     /**
      * Find all of the definitions.
      *
      * @return an ordered list of definitions
      */
     public static List<Definition> findAllDefinitions() {
         return entityManager().createQuery(
                 "SELECT d FROM Definition d ORDER BY name ASC",
                 Definition.class).getResultList();
     }
 
     /**
      * A helper function to find summarised definitions
      * based on the supplied definition id.
      *
      * @param def the definition
      * @return the list
      */
     public static List<Definition> findSummarisedDefinitions(final Definition def) {
 
         TypedQuery<Definition> q = entityManager().createQuery(
                 "SELECT d FROM Definition AS d WHERE d.summaryDefinition = :def",
                 Definition.class);
         q.setParameter("def", def);
 
         return q.getResultList();
     }
 
     /**
      * Find the definition going by the supplied name.
      *
      * @param name
      *            the name
      * @return the definition
      */
     public static Definition findDefinitionByNameEquals(String name) {
 
         Definition definition = null;
 
         if (name == null || name.length() == 0) {
             throw new IllegalArgumentException("The name argument is required");
         }
 
         TypedQuery<Definition> q = entityManager().createQuery(
                 "SELECT d FROM Definition AS d WHERE LOWER(d.name) = LOWER(:name)",
                 Definition.class);
         q.setParameter("name", name);
 
         List<Definition> definitions = q.getResultList();
 
         if (definitions != null && definitions.size() > 0) {
             definition = definitions.get(0);
         }
         return definition;
     }
 
     /**
      * Find the definition which is the unique identifier.
      *
      * @param name
      *            the name
      * @return the definition
      */
     public static Definition findUniqueIdentifierDefinition() {
 
         Definition definition = null;
 
         TypedQuery<Definition> q = entityManager().createQuery(
                 "SELECT d FROM Definition AS d WHERE d.dataType = :dataType",
                 Definition.class);
         q.setParameter("dataType", DataType.TYPE_UNIQUEID);
 
         List<Definition> definitions = q.getResultList();
 
         if (definitions != null && definitions.size() > 0) {
             definition = definitions.get(0);
         }
         return definition;
     }
 
     /**
      * Find the top level definitions (i.e. those that are not summarised).
      *
      * @return the list of definitions
      */
     public static List<Definition> findTopLevelDefinitions() {
         return findTopLevelDefinitions(null);
     }
 
     /**
      * Find the top level definitions (i.e. those that are not summarised).
      *
      * @param applicability the applicability of the definition to filter by
      * @return the list of definitions
      */
     public static List<Definition> findTopLevelDefinitions(
             final Applicability applicability) {
 
         StringBuilder sql = new StringBuilder("SELECT d FROM Definition d");
         sql.append(" WHERE d.summaryDefinition IS NULL");
 
         if (applicability != null) {
             sql.append(" AND d.applicability = :applicability");
         }
         sql.append(" ORDER BY d.name ASC");
 
         TypedQuery<Definition> q = entityManager().createQuery(sql.toString(),
                 Definition.class);
         if (applicability != null) {
             q.setParameter("applicability", applicability);
         }
 
         return q.getResultList();
     }
 
     /**
      * Find definitions that have data supplied by the supplied organisation.
      *
      * @param organisation the organisation to filter by
      * @return the list of definitions
      */
     public static List<Definition> findSubmissionDefinitions(
             final Organisation organisation) {
 
         List<Definition> definitions = new ArrayList<Definition>();
 
         if (organisation != null && organisation.getId() != null) {
 
             StringBuilder sql = new StringBuilder("SELECT d FROM Definition d");
             sql.append(" JOIN d.dataSources ds JOIN ds.organisation o");
             sql.append(" WHERE d.definitionType = :definitionType");
             sql.append(" AND o.id = :organisationId ORDER BY d.name ASC");
 
             TypedQuery<Definition> q = entityManager().createQuery(sql.toString(),
                     Definition.class);
             q.setParameter("definitionType", DefinitionType.STANDARD);
             q.setParameter("organisationId", organisation.getId());
 
             definitions = q.getResultList();
         }
         return definitions;
     }
 
     /**
      * Find definitions for the supplied ids.
      *
      * @param definitionIds
      *            the definition ids
      * @return the list of definitions
      */
     public static List<Definition> findDefinitionEntries(
             final String[] definitionIds) {
 
         List<Definition> definitions = new ArrayList<Definition>();
 
         StringBuilder where = new StringBuilder();
 
         if (definitionIds != null) {
             for (String definitionId : definitionIds) {
                 try {
                     Long id = new Long(definitionId);
 
                     if (id > 0) {
                         if (where.length() > 0) {
                             where.append(" OR ");
                         }
                         where.append("d.id = ");
                         where.append(id);
                     }
                 } catch (Exception e) {
                     // Error casting to a Long - skip
                 }
             }
         }
 
         if (where.length() > 0) {
             StringBuilder sql = new StringBuilder("SELECT d FROM Definition d WHERE ");
             sql.append(where.toString());
             sql.append(" ORDER BY d.name ASC");
 
             definitions = entityManager().createQuery(sql.toString(),
                     Definition.class).getResultList();
         }
         return definitions;
     }
 
     /**
      * Find definition entries.
      *
      * @param filter the definition filter
      * @param firstResult the first result
      * @param maxResults the max results
      * @return the list
      */
     public static List<Definition> findDefinitionEntries(
             final DefinitionFilter filter, final int firstResult,
             final int maxResults) {
 
         StringBuilder sql = new StringBuilder(
                 "SELECT d FROM Definition d JOIN d.category c");
         sql.append(buildWhere(filter));
         sql.append(" ORDER BY d.name ASC");
 
         TypedQuery<Definition> q = entityManager()
                 .createQuery(sql.toString(), Definition.class)
                 .setFirstResult(firstResult).setMaxResults(maxResults);
 
         HashMap<String, String> variables = buildVariables(filter);
         for (String variable : variables.keySet()) {
             q.setParameter(variable, variables.get(variable));
         }
 
         return q.getResultList();
     }
 
     /**
      * Count the definitions.
      *
      * @param filter the filter
      * @return the long
      */
     public static long countDefinitions(final DefinitionFilter filter) {
 
         StringBuilder sql = new StringBuilder(
                 "SELECT COUNT(d) FROM Definition d JOIN d.category c");
         sql.append(buildWhere(filter));
 
         TypedQuery<Long> q = entityManager().createQuery(sql.toString(),
                 Long.class);
 
         HashMap<String, String> variables = buildVariables(filter);
         for (String variable : variables.keySet()) {
             q.setParameter(variable, variables.get(variable));
         }
 
         return q.getSingleResult();
     }
 
     /**
      * Find the definitions that could be potentially related to the supplied definition.
      *
      * @param definition the definition
      * @return the list
      */
     public static List<Definition> findPotentialRelatedDefinitions(
             final Definition definition) {
 
         List<Definition> relatedDefinitions = new ArrayList<Definition>();
         HashMap<String, Object> variables = new HashMap<String, Object>();
 
         StringBuilder sql = new StringBuilder();
 
         if (definition != null) {
             StringBuilder where = new StringBuilder();
 
             List<Definition> defs = new ArrayList<Definition>();
 
             if (definition.getDefinitionType() == DefinitionType.CALCULATED) {
                 defs = definition.getCalculatedDefinitions();
             }
             if (definition.getDefinitionType() == DefinitionType.SUMMARY) {
                 defs = definition.getSummarisedDefinitions();
             }
 
             for (Definition def : defs) {
                 if (where.length() > 0) {
                     where.append(" AND ");
                 }
                 where.append("d.id != ");
                 where.append(def.getId());
             }
             if (where.length() > 0) {
                 where.insert(0, "(");
                 where.append(")");
             }
 
             if (definition.getDefinitionType() == DefinitionType.CALCULATED) {
                 if (where.length() > 0) {
                     where.append(" AND ");
                 }
                 where.append("(d.dataType = :dataType1 OR d.dataType = :dataType2");
                 where.append(" OR d.dataType = :dataType3)");
 
                 variables.put("dataType1", DataType.TYPE_CURRENCY);
                 variables.put("dataType2", DataType.TYPE_NUMBER);
                 variables.put("dataType3", DataType.TYPE_PERCENTAGE);
             }
 
             if (definition.getDefinitionType() == DefinitionType.SUMMARY) {
                 if (where.length() > 0) {
                     where.append(" AND ");
                 }
                 where.append("d.definitionType != :definitionType");
                 where.append(" AND d.summaryDefinition is null");
 
                 variables.put("definitionType", DefinitionType.SUMMARY);
             }
 
             if (definition.getDefinitionType() == DefinitionType.CALCULATED
                     || definition.getDefinitionType() == DefinitionType.SUMMARY) {
                 // Load all of the definitions, less the definitions already associated
                 sql.append("SELECT d FROM Definition d");
 
                 if (where.length() > 0) {
                     sql.append(" WHERE ");
                     sql.append(where.toString());
                 }
                 sql.append(" ORDER BY d.name ASC");
             }
         }
 
         if (sql.length() > 0) {
             TypedQuery<Definition> q = entityManager().createQuery(sql.toString(),
                     Definition.class);
             for (String variable : variables.keySet()) {
                 q.setParameter(variable, variables.get(variable));
             }
             relatedDefinitions = q.getResultList();
         }
 
         return relatedDefinitions;
     }
 
 
     /**
      * Find the calculated definitions that include the supplied
      * definition as a calculation variable.
      *
      * @param definition the definition
      * @return the list
      */
     public static List<Definition> findDefinitionsWithVariable(
             final Definition definition) {
 
         List<Definition> definitions = new ArrayList<Definition>();
 
         if (definition != null && definition.getId() != null) {
 
             StringBuilder calculation = new StringBuilder("%<span class=\"variable\">D");
             calculation.append(definition.getId());
             calculation.append("</span>%");
 
             StringBuilder sql = new StringBuilder("SELECT d FROM Definition d");
             sql.append(" WHERE upper(d.calculation) LIKE :calculation");
             sql.append(" AND d.definitionType = :definitionType");
 
             TypedQuery<Definition> q = entityManager().createQuery(sql.toString(),
                     Definition.class);
             q.setParameter("calculation", calculation.toString().toUpperCase());
             q.setParameter("definitionType" , DefinitionType.CALCULATED);
 
             definitions = q.getResultList();
         }
         return definitions;
     }
 
     /**
      * Group the supplied definitions - does not include any unique id data types.
      *
      * @param definitions the definitions
      * @return the map
      */
     public static Map<String, List<Definition>> groupDefinitions(
             final List<Definition> definitions) {
 
         Map<String, List<Definition>> groupedDefinitions =
                 new TreeMap<String, List<Definition>>();
 
         for (Definition definition : definitions) {
             if (definition.getDataType() != DataType.TYPE_UNIQUEID) {
                 String name = definition.getCategory().getName();
 
                 List<Definition> subGroup = new ArrayList<Definition>();
 
                 if (groupedDefinitions.containsKey(name)) {
                     subGroup = groupedDefinitions.get(name);
                 }
                 subGroup.add(definition);
 
                 groupedDefinitions.put(name, subGroup);
             }
         }
         return groupedDefinitions;
     }
 
     /**
      * Builds the where statement.
      *
      * @param filter the filter
      * @return the string
      */
     private static String buildWhere(final DefinitionFilter filter) {
         StringBuilder where = new StringBuilder();
 
         if (StringUtils.isNotBlank(filter.getName())) {
             where.append("LOWER(d.name) LIKE LOWER(:name)");
         }
 
         if (StringUtils.isNotBlank(filter.getCategory())) {
             if (where.length() > 0) {
                 where.append(" AND ");
             }
             where.append("LOWER(c.name) LIKE LOWER(:category)");
         }
 
         if (where.length() > 0) {
             where.insert(0, " WHERE ");
         }
         return where.toString();
     }
 
     /**
      * Builds the variables for the where statement.
      *
      * @param filter the filter
      * @return the hash map
      */
     private static HashMap<String, String> buildVariables(
             final DefinitionFilter filter) {
 
         HashMap<String, String> variables = new HashMap<String, String>();
 
         if (StringUtils.isNotBlank(filter.getName())) {
             variables.put("name", "%" + filter.getName() + "%");
         }
 
         if (StringUtils.isNotBlank(filter.getCategory())) {
             variables.put("category", filter.getCategory());
         }
         return variables;
     }
 
 }
