 package org.dwQueryBuilder.builders;
 
 import org.dwQueryBuilder.data.DwQueryCombination;
 import org.dwQueryBuilder.data.Fact;
 import org.dwQueryBuilder.data.GroupBy;
 import org.dwQueryBuilder.data.SelectColumn;
 import org.dwQueryBuilder.data.conditions.where.WhereConditionGroup;
 import org.dwQueryBuilder.data.queries.ComplexDwQuery;
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 public class ComplexDwQueryBuilder {
    private Set<SelectColumn> selectColumns;
    private GroupBy groupBy;
    private WhereConditionGroup whereConditionGroup;
    private DwQueryCombination combineWith;
     private String dimension;
     private Set<Fact> facts;
     private String dimensionKey;
     private String factKey;
 
     public ComplexDwQueryBuilder withSelectColumn(SelectColumn selectColumn) {
         if (this.selectColumns == null) {
             this.selectColumns = new LinkedHashSet<>();
         }
 
         this.selectColumns.add(selectColumn);
         return this;
     }
 
     public ComplexDwQueryBuilder withSelectColumn(SelectColumnBuilder selectColumnBuilder) {
         if (this.selectColumns == null) {
             this.selectColumns = new LinkedHashSet<>();
         }
 
         this.selectColumns.add(selectColumnBuilder.build());
         return this;
     }
 
     public ComplexDwQueryBuilder withSelectColumns(Set<SelectColumn> selectColumns) {
         this.selectColumns = new LinkedHashSet<>(selectColumns);
         return this;
     }
 
     public ComplexDwQueryBuilder withGroupBy(GroupBy groupedBy) {
         this.groupBy = groupedBy;
         return this;
     }
 
     public ComplexDwQueryBuilder withWhereConditionGroup(WhereConditionGroup whereConditionGroup) {
         this.whereConditionGroup = whereConditionGroup;
         return this;
     }
 
     public ComplexDwQueryBuilder withWhereConditionGroup(WhereConditionGroupBuilder whereConditionGroup) {
         this.whereConditionGroup = whereConditionGroup.build();
         return this;
     }
 
     public ComplexDwQueryBuilder withCombination(DwQueryCombination combination) {
         this.combineWith = combination;
         return this;
     }
 
     public ComplexDwQueryBuilder withCombination(DwQueryCombinationBuilder combinationBuilder) {
         this.combineWith = combinationBuilder.build();
         return this;
     }
 
     public ComplexDwQueryBuilder withDimension(String dimension) {
         this.dimension = dimension;
         return this;
     }
 
     public ComplexDwQueryBuilder withFact(Fact fact) {
         if (this.facts == null) {
             this.facts = new LinkedHashSet<>();
         }
 
         this.facts.add(fact);
         return this;
     }
 
     public ComplexDwQueryBuilder withFact(FactBuilder factBuilder) {
         if (this.facts == null) {
             this.facts = new LinkedHashSet<>();
         }
 
         this.facts.add(factBuilder.build());
         return this;
     }
 
     public ComplexDwQueryBuilder withFacts(Set<Fact> facts, String factKey) {
         this.facts = new LinkedHashSet<>(facts);
         this.factKey = factKey;
         return this;
     }
 
     public ComplexDwQueryBuilder withDimensionKey(String dimensionKey) {
         this.dimensionKey = dimensionKey;
         return this;
     }
 
     public ComplexDwQueryBuilder withFactKey(String factKey) {
         this.factKey = factKey;
         return this;
     }
 
     public ComplexDwQueryBuilder withKeys(String dimensionKey, String factKey) {
         this.dimensionKey = dimensionKey;
         this.factKey = factKey;
         return this;
     }
 
     public ComplexDwQuery build() {
         return new ComplexDwQuery(selectColumns, dimension, facts, dimensionKey, factKey, groupBy,
                 whereConditionGroup, combineWith);
     }
 }
