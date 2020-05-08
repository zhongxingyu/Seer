 /**
  *
  * Copyright 2010-2011 Vitalii Tymchyshyn
  * This file is part of EsORM.
  *
  * EsORM is free software: you can redistribute it and/or modify
  * it under the terms of the Lesser GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * EsORM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with EsORM.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.esorm.impl.jdbc;
 
 import org.esorm.*;
 import org.esorm.entity.EntityProperty;
 import org.esorm.entity.db.Column;
 import org.esorm.entity.db.SelectExpression;
 import org.esorm.entity.db.ValueExpression;
 import org.esorm.impl.db.ParsedFetchQuery;
 import org.esorm.impl.parameters.*;
 import org.esorm.parameters.ParameterMapper;
 import org.esorm.parameters.ParameterTransformer;
 import org.esorm.qbuilder.FilterValue;
 import org.esorm.qbuilder.QueryBuilder;
 import org.esorm.qbuilder.QueryFilters;
 import org.esorm.qbuilder.ValueFilters;
 
 import java.sql.Connection;
 import java.util.*;
 
 import static org.esorm.utils.IterableUtils.getFirstValue;
 import static org.esorm.utils.IterableUtils.toList;
 
 /**
  * @author Vitalii Tymchyshyn
  */
 public class SQLQueryBuilder implements QueryBuilder {
 
     private final QueryRunner queryRunner;
     private EntityConfiguration entity;
    private final SQLQueryFilters filters = new SQLQueryFilters<QueryBuilder>(this);
     private int params;
 
     public SQLQueryBuilder(QueryRunner queryRunner) {
         this.queryRunner = queryRunner;
     }
 
     public QueryBuilder select(EntityConfiguration configuration) {
         if (entity != null) {
             throw new IllegalStateException("Entity is already set in this query");
         }
         this.entity = configuration;
         return this;
     }
 
     public QueryFilters<QueryBuilder> filter() {
         return filters;
     }
 
     public ParsedQuery build() {
         BuilderState builderState = new BuilderState();
         StringBuilder query = builderState.getStringBuilder();
         Map<SelectExpression, String> tablesInvolved = builderState.tablesInvolved;
         final Map<ValueExpression, Integer> resultColumns = new HashMap<ValueExpression, Integer>();
         query.append("select ");
         Iterable<EntityProperty> properties = entity.getProperties();
         for (EntityProperty property : properties) {
             ValueExpression expression = property.getExpression();
             if (!resultColumns.containsKey(expression)) {
                 int num = resultColumns.size() + 1;
                 resultColumns.put(expression, num);
                 for (SelectExpression table : expression.getTables()) {
                     if (!tablesInvolved.containsKey(table)) {
                         int tableNum = tablesInvolved.size() + 1;
                         tablesInvolved.put(table, "t" + tableNum);
                     }
                 }
                 if (num != 1)
                     query.append(',');
                 expression.appendQuery(query, tablesInvolved);
             }
         }
         if (resultColumns.isEmpty())
             throw new IllegalArgumentException("Nothing to select for " + entity.getName());
         //TODO - complex primary key by id / name
         query.append(" from ");
         Iterable<Column> firstTablePK = null;
         Map<SelectExpression, ? extends Iterable<Column>> primaryKeys = entity.getIdColumns();
         for (Map.Entry<SelectExpression, String> e : tablesInvolved.entrySet()) {
             if (firstTablePK != null)
                 query.append(" join ");
             e.getKey().appendQuery(query, e.getValue());
             Iterable<Column> primaryKey = primaryKeys.get(e.getKey());
             if (primaryKey == null && tablesInvolved.size() > 1)
                 throw new IllegalStateException("Table " + e.getKey() + " does not have primary key specified");
             if (firstTablePK == null) {
                 firstTablePK = primaryKey;
             } else {
                 Iterator<Column> primaryKeyIterator = primaryKey.iterator();
                 String toAppend = " on ";
                 for (Column firstColumn : firstTablePK) {
                     //TODO add .hasNext check
                     Column secondColumn = primaryKeyIterator.next();
                     query.append(toAppend);
                     toAppend = " and ";
                     firstColumn.appendQuery(query, tablesInvolved);
                     query.append("=");
                     secondColumn.appendQuery(query, tablesInvolved);
                 }
                 //TODO add .hasNext check
             }
         }
         if (filters.prepare()) {
             query.append(" where ");
             filters.addQueryText(builderState);
         }
         return new ParsedFetchQuery(entity, query.toString(),
                 builderState.getParameterMapper(), resultColumns);
     }
 
     public <R> QueryIterator<R> iterator() {
         return EsormUtils.perform(queryRunner, Connection.class, new EsormUtils.PerformRunner<QueryIterator<R>, Connection>() {
             public QueryIterator<R> perform(QueryRunner queryRunner, Connection connection) {
                 return build().<R>prepare(connection).iterator().autoCloseQuery(true);
             }
         });
     }
 
     public <R> QueryIterator<R> iterator(final Map<String, Object> params) {
         return EsormUtils.perform(queryRunner, Connection.class, new EsormUtils.PerformRunner<QueryIterator<R>, Connection>() {
             public QueryIterator<R> perform(QueryRunner queryRunner, Connection connection) {
                 return build().<R>prepare(connection).iterator(params).autoCloseQuery(true);
             }
         });
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     private class BuilderState implements Appendable {
         private final StringBuilder stringBuilder = new StringBuilder();
         private List<ParameterMapper> mappers = new ArrayList<ParameterMapper>();
         Map<SelectExpression, String> tablesInvolved = new HashMap<SelectExpression, String>();
 
         public StringBuilder getStringBuilder() {
             return stringBuilder;
         }
 
         public Map<SelectExpression, String> getTablesInvolved() {
             return tablesInvolved;
         }
 
         private ParameterMapper getParameterMapper() {
             switch (mappers.size()) {
                 case 0:
                     return NoParameterMapper.INSTANCE;
                 case 1:
                     return mappers.get(0);
                 default:
                     return new MultiParameterMapper(mappers.toArray(new ParameterMapper[mappers.size()]));
             }
         }
 
         public BuilderState appendParameter(ParameterMapper mapper) {
             mappers.add(mapper);
             stringBuilder.append('?');
             return this;
         }
 
         public BuilderState appendParameter(ParameterTransformer transformer) {
             return appendParameter(new TransformerParameterMapper(transformer, mappers.size()));
         }
 
         public int getNextParameterNumber() {
             return mappers.size();
         }
 
         public BuilderState append(Object obj) {
             stringBuilder.append(obj);
             return this;
         }
 
         public BuilderState append(String str) {
             stringBuilder.append(str);
             return this;
         }
 
         public BuilderState append(StringBuffer sb) {
             stringBuilder.append(sb);
             return this;
         }
 
         public BuilderState append(CharSequence s) {
             stringBuilder.append(s);
             return this;
         }
 
         public BuilderState append(CharSequence s, int start, int end) {
             stringBuilder.append(s, start, end);
             return this;
         }
 
         public BuilderState append(char[] str) {
             stringBuilder.append(str);
             return this;
         }
 
         public BuilderState append(char[] str, int offset, int len) {
             stringBuilder.append(str, offset, len);
             return this;
         }
 
         public BuilderState append(boolean b) {
             stringBuilder.append(b);
             return this;
         }
 
         public BuilderState append(char c) {
             stringBuilder.append(c);
             return this;
         }
 
         public BuilderState append(int i) {
             stringBuilder.append(i);
             return this;
         }
 
         public BuilderState append(long lng) {
             stringBuilder.append(lng);
             return this;
         }
 
         public BuilderState append(float f) {
             stringBuilder.append(f);
             return this;
         }
 
         public BuilderState append(double d) {
             stringBuilder.append(d);
             return this;
         }
     }
 
     private interface SQLQueryFilter {
         void addQueryText(BuilderState builder);
 
         /**
          * @return if filters are not empty and should be used
          */
         boolean prepare();
     }
 
     private class NotSQLQueryFilter<T> implements SQLQueryFilter {
         private final SQLQueryFilters<T> subFilter;
 
         public NotSQLQueryFilter(T ret) {
             subFilter = new SQLQueryFilters<T>(ret);
         }
 
         public boolean prepare() {
             return subFilter.prepare();
         }
 
         public SQLQueryFilters<T> getSubFilter() {
             return subFilter;
         }
 
         public void addQueryText(BuilderState builder) {
             builder.append("not(");
             subFilter.addQueryText(builder);
             builder.append(')');
         }
 
     }
 
     private class SQLQueryFilters<T> implements QueryFilters<T>, SQLQueryFilter {
         private final T ret;
         private final String operation;
         private List<SQLQueryFilter> filters = new ArrayList<SQLQueryFilter>();
 
         public SQLQueryFilters(T ret) {
             this("and", ret);
         }
 
         public SQLQueryFilters(String operation, T ret) {
             this.operation = operation;
             this.ret = ret;
         }
 
         public void addQueryText(BuilderState builder) {
             boolean first = true;
             for (SQLQueryFilter filter : filters) {
                 if (!first) {
                     builder.append(' ').append(operation).append(' ');
                 }
                 builder.append('(');
                 filter.addQueryText(builder);
                 builder.append(')');
                 first = false;
             }
             if (first)
                 throw new IllegalStateException("Can't add empty filter to " + builder);
         }
 
         public boolean prepare() {
             for (Iterator<SQLQueryFilter> iterator = filters.iterator(); iterator.hasNext(); ) {
                 SQLQueryFilter filter = iterator.next();
                 if (!filter.prepare())
                     iterator.remove();
             }
             return !filters.isEmpty();
         }
 
         private <X extends SQLQueryFilter> X addFilter(X rc) {
             filters.add(rc);
             return rc;
         }
 
         public QueryFilters<QueryFilters<T>> and() {
             return addFilter(new SQLQueryFilters<QueryFilters<T>>(this));
         }
 
         public QueryFilters<QueryFilters<T>> or() {
             return addFilter(new SQLQueryFilters<QueryFilters<T>>("or", this));
         }
 
         public QueryFilters<QueryFilters<T>> not() {
             return addFilter(new NotSQLQueryFilter<QueryFilters<T>>(this)).getSubFilter();
         }
 
         public QueryFilters<T> ql(String textFilter) {
             throw new UnsupportedOperationException();
         }
 
         public T done() {
             return ret;
         }
 
         public ValueFilters<QueryFilters<T>> property(String name) {
             throw new UnsupportedOperationException();
         }
 
         public ValueFilters<QueryFilters<T>> id() {
             return addFilter(new SQLValueFilter<QueryFilters<T>>(this, new IdColumnsSQLValue()));
         }
 
         public ValueFilters<QueryFilters<T>> query(ParsedQuery query) {
             throw new UnsupportedOperationException();
         }
 
         public ValueFilters<QueryFilters<T>> expression(String value) {
             throw new UnsupportedOperationException();
         }
 
     }
 
     private static abstract class SQLValue {
         protected abstract void addValue(BuilderState builder, int valueNum);
 
         protected int getNumValues() {
             return 1;
         }
 
         protected boolean providesValues(int numValues) {
             return numValues == getNumValues();
         }
 
         protected void prepare() {
         }
     }
 
     private class IdColumnsSQLValue extends SQLValue {
         private List<Column> idColumns;
 
         @Override
         protected void prepare() {
             idColumns = toList(getFirstValue(entity.getIdColumns().values()));
         }
 
         @Override
         protected int getNumValues() {
             return idColumns.size();
         }
 
         @Override
         protected void addValue(BuilderState builder, int valueNum) {
             idColumns.get(valueNum).appendQuery(builder, builder.getTablesInvolved());
         }
 
     }
 
     private static class NullSQLValue extends SQLValue {
         private static NullSQLValue INSTANCE = new NullSQLValue();
 
         @Override
         protected void addValue(BuilderState builder, int valueNum) {
             builder.append("null");
         }
 
         @Override
         protected boolean providesValues(int numValues) {
             return true;
         }
     }
 
     private static class ObjectSQLValue extends SQLValue {
         private final Object value;
 
         public ObjectSQLValue(Object value) {
             this.value = value;
         }
 
         @Override
         protected void addValue(BuilderState builder, int valueNum) {
             builder.appendParameter(new FixedValueParameterMapper(builder.getNextParameterNumber(), value));
         }
     }
 
     private static class PositionalParamSQLValue extends SQLValue {
         private final int paramNum;
 
         public PositionalParamSQLValue(int paramNum) {
             this.paramNum = paramNum;
         }
 
         @Override
         protected void addValue(BuilderState builder, int valueNum) {
             builder.appendParameter(new TransformerParameterMapper(NopParameterTransformer.INSTANCE, paramNum,
                     builder.getNextParameterNumber()));
         }
     }
 
     private class SQLValueFilter<R> implements SQLQueryFilter, ValueFilters<R>, FilterValue<R> {
         private final SQLValue leftValue;
         private final R ret;
         private String operation;
         private SQLValue rightValue;
 
         public SQLValueFilter(R ret, SQLValue leftValue) {
             this.leftValue = leftValue;
             this.ret = ret;
         }
 
         public void addQueryText(BuilderState builder) {
             int numValues = leftValue.getNumValues();
             if (!rightValue.providesValues(numValues))
                 throw new IllegalArgumentException("Incompatible values " + leftValue + " and " + rightValue
                         + " provided for operation " + operation);
             if (numValues != 1)
                 throw new IllegalArgumentException(); //TODO
             leftValue.addValue(builder, 0);
             builder.append(operation);
             rightValue.addValue(builder, 0);
         }
 
         public boolean prepare() {
             if (operation == null || rightValue == null)
                 throw new IllegalStateException("Filter data was not filled for " + leftValue);
             leftValue.prepare();
             rightValue.prepare();
             return true;
         }
 
         public FilterValue<R> eq() {
             setOperation("=");
             return this;
         }
 
         public FilterValue<R> gt() {
             setOperation(">");
             return this;
         }
 
         public FilterValue<R> lt() {
             setOperation("<");
             return this;
         }
 
         public FilterValue<R> ge() {
             setOperation(">=");
             return this;
         }
 
         public FilterValue<R> le() {
             setOperation("<=");
             return this;
         }
 
         public R value(Object value) {
             if (value == null) {
                 if ("=".equals(operation)) {
                     setRightValue(NullSQLValue.INSTANCE);
                     operation = " is ";
                     return ret;
                 }
                 throw new IllegalArgumentException("Only equal operation for null value is supported");
             }
             return setRightValue(new ObjectSQLValue(value));
         }
 
         @Override
         public R param(int number) {
             params = Math.max(params, number + 1);
             return setRightValue(new PositionalParamSQLValue(number));
         }
 
         @Override
         public R param() {
             return param(params);
         }
 
         public R param(String name) {
             throw new UnsupportedOperationException();
         }
 
         public R query(ParsedQuery subQuery) {
             throw new UnsupportedOperationException();
         }
 
         public R expression(String expression) {
             throw new UnsupportedOperationException();
         }
 
         public R isNull() {
             setOperation(" is ");
             rightValue = NullSQLValue.INSTANCE;
             return ret;
         }
 
         private void setOperation(String operation) {
             if (this.operation != null)
                 throw new IllegalStateException("You must not call operation method multiple times");
             this.operation = operation;
         }
 
         private R setRightValue(SQLValue value) {
             if (rightValue != null)
                 throw new IllegalStateException("Right value is already set");
             rightValue = value;
             return ret;
         }
     }
 }
 
