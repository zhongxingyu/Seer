 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.evinceframework.data.warehouse.query.jdbc;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.hibernate.dialect.Dialect;
 import org.hibernate.dialect.pagination.LimitHandler;
 import org.hibernate.engine.spi.RowSelection;
 import org.hibernate.internal.util.StringHelper;
 import org.hibernate.sql.JoinFragment;
 import org.hibernate.sql.JoinType;
 import org.hibernate.sql.Select;
 import org.hibernate.sql.SelectFragment;
 
 import com.evinceframework.data.warehouse.Dimension;
 import com.evinceframework.data.warehouse.DimensionalAttribute;
 import com.evinceframework.data.warehouse.FactTable;
 import com.evinceframework.data.warehouse.query.DimensionCriterion;
 import com.evinceframework.data.warehouse.query.DrillPathEntry;
 import com.evinceframework.data.warehouse.query.FactRangeCriterion;
 import com.evinceframework.data.warehouse.query.FactSelection;
 import com.evinceframework.data.warehouse.query.Query;
 
 public class SqlQueryBuilder {
 
 	private Query query;
 	
 	private Dialect dialect;
 	
 	private SelectFragment selectFrag;
 
 	private JoinFragment joinFrag;
 	
 	private List<String> groupBy = new LinkedList<String>();
 	
 	private List<String> where = new LinkedList<String>();
 	
 	private DimensionJoinAliasLookup dimensionJoinLookup = new DimensionJoinAliasLookup();
 	
 	private SelectionAliasLookup selectionAliasLookup = new SelectionAliasLookup(dimensionJoinLookup);
 	
 	public SqlQueryBuilder(Query query, Dialect dialect) {
 		this.query = query;
 		
 		this.dialect = dialect;
 		this.selectFrag = new SelectFragment();
 		this.joinFrag = dialect.createOuterJoinFragment();
 	}
 	
 	public FactTable getFactTable() {
 		return query.getFactTable();
 	}
 	
 	public String getFactTableAlias() {
 		return "fact";
 	}
 	
 	public String lookupAlias(Dimension dimension, DimensionalAttribute<?> attribute) {
 		return selectionAliasLookup.determineAlias(dimension, attribute);
 	}
 	
 	public String lookupAlias(FactSelection fs) {
 		return selectionAliasLookup.determineAlias(fs);
 	}
 	
 	public void addFactSelections(FactSelection[] selections) {
 		for(FactSelection fs : selections) {
 			addFactSelection(fs);
 		}
 	}
 	
 	public void addFactSelection(FactSelection fs) {
 		
 		String selectionAlias = selectionAliasLookup.determineAlias(fs);
 		
 		if(fs.getFunction() == null) {
 			
 			selectFrag.addColumn(getFactTableAlias(), fs.getFact().getColumnName(), selectionAlias);
 			
 		} else {
 			String qualifiedName = StringHelper.qualify(getFactTableAlias(), fs.getFact().getColumnName());
 			String formula = String.format("%s(%s)", fs.getFunction().getSyntax(), qualifiedName);
 			
 			selectFrag.addFormula(getFactTableAlias(), formula, selectionAlias);
 		}
 	}
 	
 	public void addFilter(Dimension dimension, DimensionalAttribute<?> attribute) {
 		where.add(createSingularWhereClause(joinDimension(dimension), attribute.getColumnName()));
 	}
 	
 	/**
 	 * Start with the entry passed into the method and for it and each child:		
 	 * <ul>
 	 * 	<li>join dimension table if not already joined</li>
 	 * 	<li>add to select clause</li>
 	 * 	<li>add to group by clause</li>
 	 * </ul>
 	 * @param entry
 	 */
 	public void processDrillPath(DrillPathEntry<?> entry, int levels) {
 		int i = 0;
 		while(entry != null && i++ < levels) {
 			String alias = joinDimension(entry.getDimension());
 			selectFrag.addColumn(alias, entry.getDimensionalAttribute().getColumnName(),
 					selectionAliasLookup.determineAlias(entry.getDimension(), entry.getDimensionalAttribute()));
 			groupBy.add(StringHelper.qualify(alias, entry.getDimensionalAttribute().getColumnName()));
 			
 			entry = entry.getNextEntry();
 		}
 	}
 	
 	public void processDimensionCriterion(Query query) {
 		for(DimensionCriterion<?> dc : query.getDimensionCriterion()) {
 			String dimensionTableAlias = joinDimension(dc.getDimension());
 			where.add(createWhereInClause(
 					dimensionTableAlias, dc.getDimensionalAttribute().getColumnName(), dc.getValues().length));
 		}
 	}
 	
 	public void processFactRangeCriterion(Query query) {
		for(FactRangeCriterion frc : query.getFactCriterion()) {
 			
 			if (frc.getLowerBound() != null) {
 				where.add(createSingularWhereClause(
 						getFactTableAlias(), frc.getFact().getColumnName(), frc.isLowerBoundInclusive() ? ">=" : ">"));
 			}
 			
 			if (frc.getUpperBound() != null) {
 				where.add(createSingularWhereClause(
 						getFactTableAlias(), frc.getFact().getColumnName(), frc.isUpperBoundInclusive() ? "<=" : "<"));
 			}
 		}
 	}
 	
 	protected String createSingularWhereClause(String alias, String column) {
 		return createSingularWhereClause(alias, column, "=");
 	}
 	
 	protected String createSingularWhereClause(String alias, String column, String comparisonOperator) {
 		return String.format("%s.%s %s ?", alias, column, comparisonOperator);
 	}
 	
 	protected String createWhereInClause(String alias, String column, int parameterCount) {
 		
 		if(parameterCount <= 0)
 			return "";
 		
 		if(parameterCount == 1)
 			return createSingularWhereClause(alias, column);
 		
 		String[] marks = new String[parameterCount];
 		for(int i = 0; i<parameterCount; i++){
 			marks[i] = "?";
 		}
 		
 		return String.format("%s.%s in (%s)", alias, column, StringHelper.join(",", marks));
 	}
 	
 
 	public String joinDimension(Dimension dimension) {
 		
 		String alias = dimensionJoinLookup.byDimension(dimension);
 		if(alias != null)
 			return alias;
 		
 		int i = 0;
 		do {
 			alias = String.format("dim_%s", i++);
 		} 
 		while(dimensionJoinLookup.byAlias(alias) != null);
 		
 		joinFrag.addJoin(dimension.getDimensionTable().getTableName(), alias, 
 				new String[] { dimension.getForeignKeyColumn() }, 
 				new String[] { dimension.getDimensionTable().getPrimaryKeyColumn() }, 
 				JoinType.INNER_JOIN);
 		
 		dimensionJoinLookup.register(alias, dimension);
 		
 		return alias;
 	}
 	
 	public SqlStatementText generateSqlText() {
 		return generateSqlText(null);
 	}
 	
 	public SqlStatementText generateSqlText(Integer rowLimit) {
 		
 		Select select = new Select(dialect);
 		select.setFromClause(getFactTable().getTableName(), getFactTableAlias());
 		select.setSelectClause(selectFrag);
 		select.setOuterJoins(joinFrag.toFromFragmentString(), joinFrag.toWhereFragmentString());
 		
 		if (groupBy.size() > 0)
 			select.setGroupByClause(StringHelper.join(",", groupBy.toArray(new String[]{})));
 		
 		if (where.size() > 0)
 			select.setWhereClause(StringHelper.join(" AND ", where.toArray(new String[]{})));
 		
 		
 		SqlStatementText sqlStatement = new SqlStatementText();
 		sqlStatement.sql = select.toStatementString(); 
 		
 		if(rowLimit != null) {
 			RowSelection rowSelection = new RowSelection();
 			rowSelection.setMaxRows(rowLimit);
 			
 			sqlStatement.limitHandler = dialect.buildLimitHandler(sqlStatement.sql, rowSelection);
 			if(sqlStatement.limitHandler.supportsLimit()) {
 				sqlStatement.sql = sqlStatement.limitHandler.getProcessedSql();
 			}
 		}
 		
 		return sqlStatement;
 	}
 	
 	protected class SelectionAliasLookup {
 		
 		private DimensionJoinAliasLookup dimensionJoinLookup;
 		
 		private Set<String> allAliases = new HashSet<String>();
 		
 		private Map<String, FactSelection> factsMappedByAlias = new HashMap<String, FactSelection>(); 
 		
 		private Map<FactSelection, String> aliasesMappedByFacts = new HashMap<FactSelection, String>();
 		
 		private Map<String, Dimension> dimensionsMappedByAlias = new HashMap<String, Dimension>(); 
 		
 		private Map<Dimension, String> aliasesMappedByDimensions = new HashMap<Dimension, String>();
 		
 		public SelectionAliasLookup(DimensionJoinAliasLookup dimensionJoinLookup) {
 			this.dimensionJoinLookup = dimensionJoinLookup;
 		}
 
 		public String determineAlias(FactSelection fs) {
 			
 			if(aliasesMappedByFacts.containsKey(fs))
 				return aliasesMappedByFacts.get(fs);
 			
 			String selectionAlias = fs.getFunction() == null ? 
 					String.format("%s_%s", getFactTableAlias(), fs.getFact().getColumnName()) : 
 						String.format("%s_%s", fs.getFunction().getSyntax(), fs.getFact().getColumnName());
 			
 			String calculatedAlias = calculateAlias(selectionAlias);
 			
 			factsMappedByAlias.put(calculatedAlias, fs);
 			aliasesMappedByFacts.put(fs, calculatedAlias);
 			allAliases.add(calculatedAlias);
 			
 			return calculatedAlias;
 		}
 		
 		public String determineAlias(Dimension dim, DimensionalAttribute<?> attribute) {
 			
 			if(aliasesMappedByDimensions.containsKey(dim))
 				return aliasesMappedByDimensions.get(dim);
 			
 			String dimAlias = dimensionJoinLookup.byDimension(dim);
 			String calculatedAlias = calculateAlias(
 					String.format("%s_%s", dimAlias, attribute.getColumnName()));
 			
 			dimensionsMappedByAlias.put(calculatedAlias, dim);
 			aliasesMappedByDimensions.put(dim, calculatedAlias);
 			allAliases.add(calculatedAlias);
 			
 			return calculatedAlias;
 		}
 		
 		private String calculateAlias(String alias) {
 			String test = alias;
 			int i=0;
 			while(allAliases.contains(test)) {
 				test = String.format("%s_%s", test, ++i);
 			}
 			return test;
 		}
 		
 	}
 	
 	protected class DimensionJoinAliasLookup {
 		
 		private Map<String, Dimension> aliasDimensionMap = new HashMap<String, Dimension>();
 		
 		private Map<Dimension, String> dimensionAliasMap = new HashMap<Dimension, String>();
 		
 		public void register(String alias, Dimension dimension) {
 			aliasDimensionMap.put(alias, dimension);
 			dimensionAliasMap.put(dimension, alias);
 		}
 		
 		public String byDimension(Dimension dimension) {
 			return dimensionAliasMap.get(dimension);
 		}
 		
 		public Dimension byAlias(String alias) {
 			return aliasDimensionMap.get(alias);
 		}
 	}
 	
 	public class SqlStatementText {
 		
 		public String sql;
 		
 		public LimitHandler limitHandler;
 		
 	}
 }
