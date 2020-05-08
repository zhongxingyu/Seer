 /*
  * This is a common dao with basic CRUD operations and is not limited to any
  * persistent layer implementation
  *
  * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.dao.impl.hbase;
 
 import com.smartitengineering.dao.common.CommonReadDao;
 import com.smartitengineering.dao.common.CommonWriteDao;
 import com.smartitengineering.dao.common.queryparam.BasicCompoundQueryParameter;
 import com.smartitengineering.dao.common.queryparam.BiOperandQueryParameter;
 import com.smartitengineering.dao.common.queryparam.MatchMode;
 import com.smartitengineering.dao.common.queryparam.OperatorType;
 import com.smartitengineering.dao.common.queryparam.ParameterType;
 import com.smartitengineering.dao.common.queryparam.QueryParameter;
 import com.smartitengineering.dao.common.queryparam.QueryParameterCastHelper;
 import com.smartitengineering.dao.common.queryparam.QueryParameterWithOperator;
 import com.smartitengineering.dao.common.queryparam.QueryParameterWithPropertyName;
 import com.smartitengineering.dao.common.queryparam.QueryParameterWithValue;
 import com.smartitengineering.dao.common.queryparam.ValueOnlyQueryParameter;
 import com.smartitengineering.dao.impl.hbase.spi.FilterConfig;
 import com.smartitengineering.dao.impl.hbase.spi.ObjectRowConverter;
 import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
 import com.smartitengineering.dao.impl.hbase.spi.impl.BinarySuffixComparator;
 import com.smartitengineering.dao.impl.hbase.spi.impl.RangeComparator;
 import com.smartitengineering.domain.PersistentDTO;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.HTablePool;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.filter.BinaryComparator;
 import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
 import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
 import org.apache.hadoop.hbase.filter.Filter;
 import org.apache.hadoop.hbase.filter.FilterList;
 import org.apache.hadoop.hbase.filter.FilterList.Operator;
 import org.apache.hadoop.hbase.filter.SingleColumnValueExcludeFilter;
 import org.apache.hadoop.hbase.filter.SkipFilter;
 import org.apache.hadoop.hbase.filter.SubstringComparator;
 import org.apache.hadoop.hbase.filter.WritableByteArrayComparable;
 import org.apache.hadoop.hbase.util.Bytes;
 
 /**
  * A common DAO implementation for HBase. Please note that all parameters for reading (i.e. Scan) assumes that the
  * toString() method returns the string representation of the value to be compared in byte[] form.
  * @author imyousuf
  */
 public class CommonDao<Template extends PersistentDTO> implements CommonReadDao<Template>,
                                                                   CommonWriteDao<Template> {
 
   public static final int DEFAULT_MAX_HTABLE_POOL_SIZE = 3000;
   public static final int DEFAULT_MAX_ROWS = 1000;
   private ObjectRowConverter<Template> converter;
   private SchemaInfoProvider infoProvider;
   private Configuration configuration;
   private HTablePool tablePool;
   private int maxRows = -1;
 
   public int getMaxRows() {
     return maxRows;
   }
 
   public void setMaxRows(int maxRows) {
     this.maxRows = maxRows;
   }
 
   public ObjectRowConverter<Template> getConverter() {
     return converter;
   }
 
   public void setConverter(ObjectRowConverter<Template> converter) {
     this.converter = converter;
   }
 
   public SchemaInfoProvider getInfoProvider() {
     return infoProvider;
   }
 
   public void setInfoProvider(SchemaInfoProvider infoProvider) {
     this.infoProvider = infoProvider;
   }
 
   protected Configuration getConfiguration() {
     if (configuration == null) {
       configuration = HBaseConfiguration.create();
     }
     return configuration;
   }
 
   protected int getMaxScanRows() {
     return getMaxRows() > 0 ? getMaxRows() : DEFAULT_MAX_ROWS;
   }
 
   protected int getMaxScanRows(List<QueryParameter> params) {
     if (params != null && !params.isEmpty()) {
       for (QueryParameter param : params) {
         if (ParameterType.PARAMETER_TYPE_MAX_RESULT.equals(param.getParameterType())) {
           ValueOnlyQueryParameter<Integer> queryParameter = QueryParameterCastHelper.VALUE_PARAM_HELPER.cast(param);
           return queryParameter.getValue();
         }
       }
     }
     return getMaxScanRows();
   }
 
   public void setConfiguration(Configuration configuration) {
     this.configuration = configuration;
   }
 
   protected HTablePool getTablePool() {
     if (tablePool == null) {
       tablePool = new HTablePool(getConfiguration(), DEFAULT_MAX_HTABLE_POOL_SIZE);
     }
     return tablePool;
   }
 
   protected HTableInterface getDefaultTable() {
     return getTablePool().getTable(infoProvider.getMainTableName());
   }
 
   /*
    * READ OPERATIONS
    */
 
   /*
    * Unsupported read operations
    */
   @Override
   public Set<Template> getAll() {
     throw new UnsupportedOperationException("Not supported yet.");
   }
 
   @Override
   public <OtherTemplate> OtherTemplate getOther(List<QueryParameter> query) {
     throw new UnsupportedOperationException("Not supported yet.");
   }
 
   @Override
   public <OtherTemplate> List<OtherTemplate> getOtherList(List<QueryParameter> query) {
     throw new UnsupportedOperationException("Not supported yet.");
   }
 
   /*
    * Supported read operations
    */
   @Override
   public Set<Template> getByIds(List<Integer> ids) {
     LinkedHashSet<Template> set = new LinkedHashSet<Template>(ids.size());
     for (Integer id : ids) {
       set.add(getById(id));
     }
     return set;
   }
 
   @Override
   public Template getById(Integer id) {
     HTableInterface hTable = null;
     try {
       Get get = new Get(Bytes.toBytes(id));
       hTable = getDefaultTable();
       Result result = hTable.get(get);
       return getConverter().rowsToObject(result, getTablePool());
     }
     catch (Exception ex) {
       throw new RuntimeException(ex);
     }
     finally {
       try {
         if (hTable != null) {
           getTablePool().putTable(hTable);
         }
       }
       catch (Exception ex) {
         ex.printStackTrace();
       }
     }
   }
 
   @Override
   public Template getSingle(List<QueryParameter> query) {
     Scan scan = formScan(query);
     HTableInterface table = getDefaultTable();
     ResultScanner scanner = null;
     try {
       scanner = table.getScanner(scan);
       Result result = scanner.next();
       if (result == null) {
         return null;
       }
       else {
         return getConverter().rowsToObject(result, getTablePool());
       }
     }
     catch (Exception ex) {
       throw new RuntimeException(ex);
     }
     finally {
       if (scanner != null) {
         scanner.close();
       }
       getTablePool().putTable(table);
     }
   }
 
   @Override
   public List<Template> getList(List<QueryParameter> query) {
     Scan scan = formScan(query);
     HTableInterface table = getDefaultTable();
     ResultScanner scanner = null;
     try {
       scanner = table.getScanner(scan);
       Result[] results = scanner.next(getMaxScanRows(query));
       if (results == null) {
         return Collections.emptyList();
       }
       else {
         ArrayList<Template> templates = new ArrayList<Template>(results.length);
         for (Result result : results) {
           templates.add(getConverter().rowsToObject(result, getTablePool()));
         }
         return templates;
       }
     }
     catch (Exception ex) {
       throw new RuntimeException(ex);
     }
     finally {
       if (scanner != null) {
         scanner.close();
       }
       getTablePool().putTable(table);
     }
   }
 
   protected Scan formScan(List<QueryParameter> query) {
     Scan scan = new Scan();
     final Filter filter = getFilter(query);
     if (filter != null) {
       scan.setFilter(filter);
     }
     return scan;
   }
 
   protected Filter getFilter(Collection<QueryParameter> queryParams) {
     return getFilter(queryParams, Operator.MUST_PASS_ALL);
   }
 
   protected Filter getFilter(Collection<QueryParameter> queryParams, Operator operator) {
     final Filter filter;
     if (queryParams != null && !queryParams.isEmpty()) {
       List<Filter> filters = new ArrayList<Filter>(queryParams.size());
       for (QueryParameter param : queryParams) {
         switch (param.getParameterType()) {
           case PARAMETER_TYPE_CONJUNCTION: {
             BasicCompoundQueryParameter queryParameter =
                                         QueryParameterCastHelper.BASIC_COMPOUND_PARAM_HELPER.cast(param);
             Collection<QueryParameter> nestedParameters = queryParameter.getNestedParameters();
             filters.add(getFilter(nestedParameters, Operator.MUST_PASS_ALL));
             break;
           }
           case PARAMETER_TYPE_DISJUNCTION: {
             BasicCompoundQueryParameter queryParameter =
                                         QueryParameterCastHelper.BASIC_COMPOUND_PARAM_HELPER.cast(param);
             Collection<QueryParameter> nestedParameters = queryParameter.getNestedParameters();
             filters.add(getFilter(nestedParameters, Operator.MUST_PASS_ONE));
             break;
           }
           case PARAMETER_TYPE_PROPERTY: {
             handlePropertyParam(param, filters);
             break;
           }
           default:
           //Do nothing
         }
       }
       if (!filters.isEmpty()) {
         FilterList filterList = new FilterList(operator, filters);
         filter = filterList;
       }
       else {
         filter = null;
       }
     }
     else {
       filter = null;
     }
     return filter;
   }
 
   protected void handlePropertyParam(QueryParameter queryParameter,
                                      List<Filter> filters) {
     OperatorType operator = getOperator(queryParameter);
     Object parameter = getValue(queryParameter);
     FilterConfig filterConfig = getInfoProvider().getFilterConfig(getPropertyName(queryParameter));
     switch (operator) {
       case OPERATOR_EQUAL: {
         filters.add(getCellFilter(filterConfig, CompareOp.EQUAL, Bytes.toBytes(parameter.toString())));
         return;
       }
       case OPERATOR_LESSER: {
         filters.add(getCellFilter(filterConfig, CompareOp.LESS, Bytes.toBytes(parameter.toString())));
         return;
       }
       case OPERATOR_LESSER_EQUAL: {
         filters.add(getCellFilter(filterConfig, CompareOp.LESS_OR_EQUAL, Bytes.toBytes(parameter.toString())));
         return;
       }
       case OPERATOR_GREATER: {
         filters.add(getCellFilter(filterConfig, CompareOp.GREATER, Bytes.toBytes(parameter.toString())));
         return;
       }
       case OPERATOR_GREATER_EQUAL: {
         filters.add(getCellFilter(filterConfig, CompareOp.GREATER_OR_EQUAL, Bytes.toBytes(parameter.toString())));
         return;
       }
       case OPERATOR_NOT_EQUAL: {
         filters.add(getCellFilter(filterConfig, CompareOp.NOT_EQUAL, Bytes.toBytes(parameter.toString())));
         return;
       }
       case OPERATOR_IS_EMPTY:
       case OPERATOR_IS_NULL: {
        final SingleColumnValueExcludeFilter cellFilter =
                                             getCellFilter(filterConfig, CompareOp.EQUAL, Bytes.toBytes(""));
        cellFilter.setFilterIfMissing(false);
        filters.add(cellFilter);
         return;
       }
       case OPERATOR_IS_NOT_EMPTY:
       case OPERATOR_IS_NOT_NULL: {
        final SingleColumnValueExcludeFilter cellFilter = getCellFilter(filterConfig, CompareOp.NOT_EQUAL, Bytes.toBytes(
            ""));
        cellFilter.setFilterIfMissing(true);
        filters.add(cellFilter);
         return;
       }
       case OPERATOR_STRING_LIKE: {
         MatchMode matchMode = getMatchMode(queryParameter);
         if (matchMode == null) {
           matchMode = MatchMode.EXACT;
         }
         switch (matchMode) {
           case END:
             filters.add(getCellFilter(filterConfig, CompareOp.EQUAL, new BinarySuffixComparator(Bytes.toBytes(parameter.
                 toString()))));
             break;
           case EXACT:
             filters.add(getCellFilter(filterConfig, CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes(parameter.
                 toString()))));
             break;
           case START:
             filters.add(getCellFilter(filterConfig, CompareOp.EQUAL, new BinaryPrefixComparator(Bytes.toBytes(parameter.
                 toString()))));
             break;
           default:
           case ANYWHERE:
             filters.add(getCellFilter(filterConfig, CompareOp.EQUAL, new SubstringComparator(parameter.toString())));
             break;
         }
         return;
       }
       case OPERATOR_BETWEEN: {
         parameter = getFirstParameter(queryParameter);
         Object parameter2 = getSecondParameter(queryParameter);
         filters.add(getCellFilter(filterConfig, CompareOp.EQUAL,
                                   new RangeComparator(Bytes.toBytes(parameter.toString()),
                                                       Bytes.toBytes(parameter2.toString()))));
         return;
       }
       case OPERATOR_IS_IN: {
         Collection inCollectin = QueryParameterCastHelper.MULTI_OPERAND_PARAM_HELPER.cast(queryParameter).getValues();
         FilterList filterList = getInFilter(inCollectin, filterConfig);
         filters.add(filterList);
         return;
       }
       case OPERATOR_IS_NOT_IN: {
         Collection notInCollectin = QueryParameterCastHelper.MULTI_OPERAND_PARAM_HELPER.cast(queryParameter).getValues();
         FilterList filterList = getInFilter(notInCollectin, filterConfig);
         filters.add(new SkipFilter(filterList));
         return;
       }
     }
     return;
   }
 
  protected SingleColumnValueExcludeFilter getCellFilter(FilterConfig filterConfig, CompareOp op,
                                                         WritableByteArrayComparable comparator) {
     final SingleColumnValueExcludeFilter valueFilter;
     valueFilter = new SingleColumnValueExcludeFilter(filterConfig.getColumnFamily(),
                                                      filterConfig.getColumnQualifier(),
                                                      op, comparator);
     valueFilter.setFilterIfMissing(filterConfig.isFilterOnIfMissing());
     valueFilter.setLatestVersionOnly(filterConfig.isFilterOnLatestVersionOnly());
     return valueFilter;
   }
 
  protected SingleColumnValueExcludeFilter getCellFilter(FilterConfig filterConfig, CompareOp op, byte[] value) {
     return getCellFilter(filterConfig, op, new BinaryComparator(value));
   }
 
   protected FilterList getInFilter(Collection inCollectin, FilterConfig config) {
     FilterList filterList = new FilterList(Operator.MUST_PASS_ONE);
     for (Object inObj : inCollectin) {
       filterList.addFilter(getCellFilter(config, CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes(inObj.toString()))));
     }
     return filterList;
   }
 
   protected String getPropertyName(QueryParameter param) {
     final String propertyName;
     if (param instanceof QueryParameterWithPropertyName) {
       propertyName = ((QueryParameterWithPropertyName) param).getPropertyName();
     }
     else {
       propertyName = "";
     }
     return propertyName;
   }
 
   protected Object getSecondParameter(QueryParameter queryParamemter) {
     if (queryParamemter instanceof BiOperandQueryParameter) {
       return QueryParameterCastHelper.BI_OPERAND_PARAM_HELPER.cast(queryParamemter).getSecondValue();
     }
     else {
       return "";
     }
   }
 
   protected Object getFirstParameter(QueryParameter queryParamemter) {
     if (queryParamemter instanceof BiOperandQueryParameter) {
       return QueryParameterCastHelper.BI_OPERAND_PARAM_HELPER.cast(queryParamemter).getFirstValue();
     }
     else {
       return "";
     }
   }
 
   protected MatchMode getMatchMode(QueryParameter queryParamemter) {
     return QueryParameterCastHelper.STRING_PARAM_HELPER.cast(queryParamemter).getMatchMode();
   }
 
   protected Object getValue(QueryParameter queryParamemter) {
     Object value;
     if (queryParamemter instanceof QueryParameterWithValue) {
       value = ((QueryParameterWithValue) queryParamemter).getValue();
     }
     else {
       value = null;
     }
     if (value == null) {
       value = "";
     }
     return value;
   }
 
   protected OperatorType getOperator(QueryParameter queryParamemter) {
     if (QueryParameterCastHelper.BI_OPERAND_PARAM_HELPER.isWithOperator(queryParamemter)) {
       QueryParameterWithOperator parameterWithOperator =
                                  QueryParameterCastHelper.BI_OPERAND_PARAM_HELPER.castToOperatorParam(queryParamemter);
       return parameterWithOperator.getOperatorType();
     }
     else {
       return null;
     }
   }
 
   @Override
   public Template getSingle(QueryParameter... query) {
     return getSingle(Arrays.asList(query));
   }
 
   @Override
   public List<Template> getList(QueryParameter... query) {
     return getList(Arrays.asList(query));
   }
 
   @Override
   public <OtherTemplate> OtherTemplate getOther(QueryParameter... query) {
     return this.<OtherTemplate>getOther(Arrays.asList(query));
   }
 
   @Override
   public <OtherTemplate> List<OtherTemplate> getOtherList(QueryParameter... query) {
     return this.<OtherTemplate>getOtherList(Arrays.asList(query));
   }
 
   /*
    * WRITE OPERATIONS
    */
   @Override
   public void save(Template... states) {
     LinkedHashMap<String, List<Put>> allPuts = new LinkedHashMap<String, List<Put>>();
     for (Template state : states) {
       LinkedHashMap<String, Put> puts = getConverter().objectToRows(state);
       for (Map.Entry<String, Put> put : puts.entrySet()) {
         final List<Put> putList;
         if (allPuts.containsKey(put.getKey())) {
           putList = allPuts.get(put.getKey());
         }
         else {
           putList = new ArrayList<Put>();
           allPuts.put(put.getKey(), putList);
         }
         putList.add(put.getValue());
       }
     }
     for (Map.Entry<String, List<Put>> puts : allPuts.entrySet()) {
       HTableInterface hTable = null;
       try {
         hTable = getTablePool().getTable(puts.getKey());
         hTable.put(puts.getValue());
       }
       catch (Exception ex) {
         throw new RuntimeException(ex);
       }
       finally {
         try {
           if (hTable != null) {
             getTablePool().putTable(hTable);
           }
         }
         catch (Exception ex) {
           ex.printStackTrace();
         }
       }
     }
   }
 
   @Override
   public void update(Template... states) {
     save(states);
   }
 
   @Override
   public void delete(Template... states) {
     LinkedHashMap<String, List<Delete>> allPuts = new LinkedHashMap<String, List<Delete>>();
     for (Template state : states) {
       LinkedHashMap<String, Delete> puts = getConverter().objectToDeleteableRows(state);
       for (Map.Entry<String, Delete> put : puts.entrySet()) {
         final List<Delete> putList;
         if (allPuts.containsKey(put.getKey())) {
           putList = allPuts.get(put.getKey());
         }
         else {
           putList = new ArrayList<Delete>();
           allPuts.put(put.getKey(), putList);
         }
         putList.add(put.getValue());
       }
     }
     for (Map.Entry<String, List<Delete>> puts : allPuts.entrySet()) {
       HTableInterface hTable = null;
       try {
         hTable = getTablePool().getTable(puts.getKey());
         hTable.delete(puts.getValue());
       }
       catch (Exception ex) {
         throw new RuntimeException(ex);
       }
       finally {
         try {
           if (hTable != null) {
             getTablePool().putTable(hTable);
           }
         }
         catch (Exception ex) {
           ex.printStackTrace();
         }
       }
     }
   }
 }
