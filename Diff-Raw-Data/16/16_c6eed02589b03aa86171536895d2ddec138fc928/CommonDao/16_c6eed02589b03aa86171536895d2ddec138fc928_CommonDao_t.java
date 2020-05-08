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
 
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.smartitengineering.dao.common.queryparam.BasicCompoundQueryParameter;
 import com.smartitengineering.dao.common.queryparam.BiOperandQueryParameter;
 import com.smartitengineering.dao.common.queryparam.CompositionQueryParameter;
 import com.smartitengineering.dao.common.queryparam.MatchMode;
 import com.smartitengineering.dao.common.queryparam.OperatorType;
 import com.smartitengineering.dao.common.queryparam.ParameterType;
 import com.smartitengineering.dao.common.queryparam.QueryParameter;
 import com.smartitengineering.dao.common.queryparam.QueryParameterCastHelper;
 import com.smartitengineering.dao.common.queryparam.QueryParameterWithOperator;
 import com.smartitengineering.dao.common.queryparam.QueryParameterWithPropertyName;
 import com.smartitengineering.dao.common.queryparam.QueryParameterWithValue;
 import com.smartitengineering.dao.common.queryparam.ValueOnlyQueryParameter;
 import com.smartitengineering.dao.impl.hbase.spi.AsyncExecutorService;
 import com.smartitengineering.dao.impl.hbase.spi.Callback;
 import com.smartitengineering.dao.impl.hbase.spi.FilterConfig;
 import com.smartitengineering.dao.impl.hbase.spi.LockAttainer;
 import com.smartitengineering.dao.impl.hbase.spi.LockType;
 import com.smartitengineering.dao.impl.hbase.spi.MergeService;
 import com.smartitengineering.dao.impl.hbase.spi.ObjectRowConverter;
 import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
 import com.smartitengineering.dao.impl.hbase.spi.impl.BinarySuffixComparator;
 import com.smartitengineering.dao.impl.hbase.spi.impl.DiffBasedMergeService;
 import com.smartitengineering.dao.impl.hbase.spi.impl.RangeComparator;
 import com.smartitengineering.domain.PersistentDTO;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import org.apache.commons.lang.StringUtils;
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.RowLock;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.filter.BinaryComparator;
 import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
 import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
 import org.apache.hadoop.hbase.filter.Filter;
 import org.apache.hadoop.hbase.filter.FilterList;
 import org.apache.hadoop.hbase.filter.FilterList.Operator;
 import org.apache.hadoop.hbase.filter.QualifierFilter;
 import org.apache.hadoop.hbase.filter.RowFilter;
 import org.apache.hadoop.hbase.filter.SingleColumnValueExcludeFilter;
 import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
 import org.apache.hadoop.hbase.filter.SkipFilter;
 import org.apache.hadoop.hbase.filter.SubstringComparator;
 import org.apache.hadoop.hbase.filter.WritableByteArrayComparable;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A common DAO implementation for HBase. Please note that all parameters for reading (i.e. Scan) assumes that the
  * toString() method returns the string representation of the value to be compared in byte[] form.
  * @author imyousuf
  */
 public class CommonDao<Template extends PersistentDTO<? extends PersistentDTO, ? extends Comparable, ? extends Long>, IdType extends Serializable>
     implements
     com.smartitengineering.dao.common.CommonDao<Template, IdType> {
 
   public static final int DEFAULT_MAX_ROWS = 1000;
   @Inject
   private ObjectRowConverter<Template> converter;
   @Inject
   private SchemaInfoProvider<Template, IdType> infoProvider;
   @Inject
   private AsyncExecutorService executorService;
   @Inject
   private ExecutorService resultExecutorService;
   @Inject
   @Named("mergeEnabled")
   private Boolean mergeEnabled = false;
   @Inject
   private MergeService<Template, IdType> mergeService;
   @Inject
   private LockAttainer<Template, IdType> lockAttainer;
   @Inject(optional = true)
   private LockType lockType;
   private final String errorMessageFormat = "Delete of row %s from table %s has failed optimistically!";
   private int maxRows = -1;
   protected final Logger logger = LoggerFactory.getLogger(getClass());
 
   public AsyncExecutorService getExecutorService() {
     return executorService;
   }
 
   public void setExecutorService(AsyncExecutorService executorService) {
     this.executorService = executorService;
   }
 
   public int getMaxRows() {
     return maxRows;
   }
 
   @Inject
   public void setMaxRows(@Named("maxRows") Integer maxRows) {
     this.maxRows = maxRows;
   }
 
   public ObjectRowConverter<Template> getConverter() {
     return converter;
   }
 
   public void setConverter(ObjectRowConverter<Template> converter) {
     this.converter = converter;
   }
 
   public SchemaInfoProvider<Template, IdType> getInfoProvider() {
     return infoProvider;
   }
 
   public void setInfoProvider(SchemaInfoProvider<Template, IdType> infoProvider) {
     this.infoProvider = infoProvider;
   }
 
   protected String getDefaultTableName() {
     return getInfoProvider().getMainTableName();
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
 
   /*
    * READ OPERATIONS
    */
 
   /*
    * Unsupported read operations
    */
   @Override
   public Set<Template> getAll() {
     logger.info("Get ALL Executed!");
     return executorService.execute(getDefaultTableName(), new Callback<Set<Template>>() {
 
       @Override
       public Set<Template> call(HTableInterface tableInterface) throws Exception {
         final Scan scan = new Scan();
         RowFilter rowFilter = new RowFilter(CompareOp.EQUAL, new BinaryPrefixComparator(new byte[0]));
         scan.setFilter(rowFilter);
         final int maxRows = getMaxScanRows();
         return new LinkedHashSet<Template>(CommonDao.this.scanList(tableInterface, scan, maxRows));
       }
     });
   }
 
   @Override
   public <OtherTemplate extends Object> OtherTemplate getOther(final List<QueryParameter> query) {
     return executorService.execute(getDefaultTableName(), new Callback<OtherTemplate>() {
 
       @Override
       public OtherTemplate call(HTableInterface tableInterface) throws Exception {
         ResultScanner scanner = tableInterface.getScanner(formScan(query));
         try {
           Result result = scanner.next();
           if (result == null || result.isEmpty()) {
             return null;
           }
           else {
             return (OtherTemplate) result.getNoVersionMap();
           }
         }
         finally {
           if (scanner != null) {
             scanner.close();
           }
         }
       }
     });
   }
 
   @Override
   public <OtherTemplate> List<OtherTemplate> getOtherList(final List<QueryParameter> query) {
     return executorService.execute(getDefaultTableName(), new Callback<List<OtherTemplate>>() {
 
       @Override
       public List<OtherTemplate> call(HTableInterface tableInterface) throws Exception {
         ResultScanner scanner = tableInterface.getScanner(formScan(query));
         try {
           Result[] results = scanner.next(getMaxScanRows(query));
           if (results == null) {
             return Collections.emptyList();
           }
           else {
             final ArrayList<OtherTemplate> templates = new ArrayList<OtherTemplate>(results.length);
             for (final Result result : results) {
               if (result == null || result.isEmpty()) {
                 continue;
               }
               else {
                 templates.add((OtherTemplate) result.getNoVersionMap());
               }
             }
             return templates;
           }
         }
         finally {
           if (scanner != null) {
             scanner.close();
           }
         }
       }
     });
   }
 
   /*
    * Supported read operations
    */
   @Override
   public Set<Template> getByIds(List<IdType> ids) {
     LinkedHashSet<Future<Template>> set = new LinkedHashSet<Future<Template>>(ids.size());
     LinkedHashSet<Template> resultSet = new LinkedHashSet<Template>(ids.size());
     for (IdType id : ids) {
       set.add(executorService.executeAsynchronously(getDefaultTableName(), getByIdCallback(id)));
     }
     for (Future<Template> future : set) {
       try {
         final Template get = future.get();
         if (get != null) {
           resultSet.add(get);
         }
       }
       catch (Exception ex) {
         logger.warn("Could not retrieve from Future...", ex);
       }
     }
     return resultSet;
   }
 
   @Override
   public Template getById(final IdType id) {
     return executorService.execute(getDefaultTableName(), getByIdCallback(id));
   }
 
   protected Callback<Template> getByIdCallback(final IdType id) {
     return new Callback<Template>() {
 
       @Override
       public Template call(HTableInterface tableInterface) throws Exception {
         final byte[] rowId = getInfoProvider().getRowIdFromId(id);
         Get get = new Get(rowId);
         Result result = tableInterface.get(get);
         if (result == null || result.isEmpty()) {
           return null;
         }
         else {
           return getConverter().rowsToObject(result, executorService);
         }
       }
     };
   }
 
   @Override
   public Template getSingle(final List<QueryParameter> query) {
     return executorService.execute(getDefaultTableName(), new Callback<Template>() {
 
       @Override
       public Template call(HTableInterface tableInterface) throws Exception {
         ResultScanner scanner = tableInterface.getScanner(formScan(query));
         try {
           Result result = scanner.next();
           if (result == null || result.isEmpty()) {
             return null;
           }
           else {
             return getConverter().rowsToObject(result, executorService);
           }
         }
         finally {
           if (scanner != null) {
             scanner.close();
           }
         }
       }
     });
   }
 
   @Override
   public List<Template> getList(final List<QueryParameter> query) {
     return executorService.execute(getDefaultTableName(), new Callback<List<Template>>() {
 
       @Override
       public List<Template> call(HTableInterface tableInterface) throws Exception {
         final Scan scan = formScan(query);
         final int maxRows = getMaxScanRows(query);
         return CommonDao.this.scanList(tableInterface, scan, maxRows);
       }
     });
   }
 
   protected LockType getLockType() {
     if (lockType == null) {
       return LockType.getDefault();
     }
     return lockType;
   }
 
   protected List<Template> scanList(HTableInterface tableInterface, Scan scan, int maxRows) throws IOException,
                                                                                                    InterruptedException,
                                                                                                    ExecutionException {
     logger.info("Scanning a list");
     if (logger.isDebugEnabled()) {
       logger.debug("Scanning for rows " + maxRows);
     }
     ResultScanner scanner = tableInterface.getScanner(scan);
     try {
       Result[] results = scanner.next(maxRows);
       if (logger.isDebugEnabled()) {
         logger.debug("ResultScanner " + scanner);
         logger.debug("Results " + results);
         if (results != null) {
           logger.debug("Results " + results.length);
         }
       }
       if (results == null) {
         return Collections.emptyList();
       }
       else {
         final ArrayList<Template> templates = new ArrayList<Template>(results.length);
         ArrayList<Future<Template>> futureTemplates = new ArrayList<Future<Template>>(results.length);
         for (final Result result : results) {
           if (result == null || result.isEmpty()) {
             logger.debug("Result is null or empty " + result + " " + (result != null ? result.isEmpty() : "TRUE"));
             continue;
           }
           else {
             futureTemplates.add(resultExecutorService.submit(new Callable<Template>() {
 
               @Override
               public Template call() throws Exception {
                 logger.debug("Converting row to object " + result);
                 return getConverter().rowsToObject(result, executorService);
               }
             }));
           }
         }
         for (Future<Template> future : futureTemplates) {
           Template template = future.get();
           if (template != null) {
             templates.add(template);
           }
         }
         return templates;
       }
     }
     catch (IOException ex) {
       logger.warn(ex.getMessage(), ex);
       throw ex;
     }
     catch (InterruptedException ex) {
       logger.warn(ex.getMessage(), ex);
       throw ex;
     }
     catch (ExecutionException ex) {
       logger.warn(ex.getMessage(), ex);
       throw ex;
     }
     finally {
       if (scanner != null) {
         scanner.close();
       }
     }
   }
 
   protected Scan formScan(List<QueryParameter> query) {
     Scan scan = new Scan();
     final Filter filter = getFilter(query, scan);
     if (filter != null) {
       scan.setFilter(filter);
     }
     return scan;
   }
 
   protected Filter getFilter(Collection<QueryParameter> queryParams, Scan scan) {
     return getFilter(queryParams, scan, Operator.MUST_PASS_ALL);
   }
 
   protected Filter getFilter(Collection<QueryParameter> queryParams, Scan scan, Operator operator) {
     return getFilter("", queryParams, scan, operator);
   }
 
   protected Filter getFilter(String namePrefix, Collection<QueryParameter> queryParams, Scan scan, Operator operator) {
     final Filter filter;
     if (queryParams != null && !queryParams.isEmpty()) {
       List<Filter> filters = new ArrayList<Filter>(queryParams.size());
       for (QueryParameter param : queryParams) {
         switch (param.getParameterType()) {
           case PARAMETER_TYPE_CONJUNCTION: {
             BasicCompoundQueryParameter queryParameter =
                                         QueryParameterCastHelper.BASIC_COMPOUND_PARAM_HELPER.cast(param);
             Collection<QueryParameter> nestedParameters = queryParameter.getNestedParameters();
             filters.add(getFilter(nestedParameters, scan, Operator.MUST_PASS_ALL));
             break;
           }
           case PARAMETER_TYPE_NESTED_PROPERTY: {
             CompositionQueryParameter queryParameter = QueryParameterCastHelper.COMPOSITION_PARAM_FOR_NESTED_TYPE.cast(
                 param);
             Collection<QueryParameter> nestedParameters = queryParameter.getNestedParameters();
             filters.add(getFilter(getPropertyName(namePrefix, param), nestedParameters, scan, Operator.MUST_PASS_ALL));
             break;
           }
           case PARAMETER_TYPE_DISJUNCTION: {
             BasicCompoundQueryParameter queryParameter =
                                         QueryParameterCastHelper.BASIC_COMPOUND_PARAM_HELPER.cast(param);
             Collection<QueryParameter> nestedParameters = queryParameter.getNestedParameters();
             filters.add(getFilter(nestedParameters, scan, Operator.MUST_PASS_ONE));
             break;
           }
           case PARAMETER_TYPE_PROPERTY: {
             handlePropertyParam(namePrefix, param, filters);
             break;
           }
           case PARAMETER_TYPE_FIRST_RESULT: {
             Object value = getValue(param);
             scan.setStartRow(Bytes.toBytes(value.toString()));
             break;
           }
           case PARAMETER_TYPE_UNIT_PROP: {
             final String propertyName = getPropertyName(namePrefix, param);
             final String configPropertyName;
             final int indexOfColon = propertyName.indexOf(":");
             final String dynaPropName;
             if (indexOfColon > -1) {
               configPropertyName = propertyName.substring(0, indexOfColon);
               dynaPropName = propertyName.substring(indexOfColon + 1);
             }
             else {
               configPropertyName = propertyName;
               dynaPropName = null;
             }
             FilterConfig config = getInfoProvider().getFilterConfig(configPropertyName);
             if (config != null) {
               if (StringUtils.isNotBlank(dynaPropName)) {
                 scan.addColumn(config.getColumnFamily(), Bytes.toBytes(dynaPropName));
               }
               else if (config.getColumnQualifier() != null && config.getColumnQualifier().length > 0) {
                 scan.addColumn(config.getColumnFamily(), config.getColumnQualifier());
               }
               else {
                 scan.addFamily(config.getColumnFamily());
               }
             }
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
       filter = new RowFilter(CompareOp.EQUAL, new BinaryPrefixComparator(new byte[0]));
     }
     return filter;
   }
 
   protected void handlePropertyParam(String namePrefix, QueryParameter queryParameter, List<Filter> filters) {
     OperatorType operator = getOperator(queryParameter);
     Object parameter = getValue(queryParameter);
     FilterConfig filterConfig = getInfoProvider().getFilterConfig(getPropertyName(namePrefix, queryParameter));
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
         final Filter cellFilter = getCellFilter(filterConfig, CompareOp.EQUAL, Bytes.toBytes(""));
         if (cellFilter instanceof SingleColumnValueFilter) {
           ((SingleColumnValueFilter) cellFilter).setFilterIfMissing(false);
         }
         filters.add(cellFilter);
         return;
       }
       case OPERATOR_IS_NOT_EMPTY:
       case OPERATOR_IS_NOT_NULL: {
         final Filter cellFilter = getCellFilter(filterConfig, CompareOp.NOT_EQUAL, Bytes.toBytes(""));
         if (cellFilter instanceof SingleColumnValueFilter) {
           ((SingleColumnValueFilter) cellFilter).setFilterIfMissing(true);
         }
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
         Filter filterList = getInFilter(inCollectin, filterConfig);
         filters.add(filterList);
         return;
       }
       case OPERATOR_IS_NOT_IN: {
         Collection notInCollectin = QueryParameterCastHelper.MULTI_OPERAND_PARAM_HELPER.cast(queryParameter).getValues();
         Filter filterList = getInFilter(notInCollectin, filterConfig);
         filters.add(new SkipFilter(filterList));
         return;
       }
     }
     return;
   }
 
   protected Filter getCellFilter(FilterConfig filterConfig, CompareOp op,
                                  WritableByteArrayComparable comparator) {
     if (filterConfig.isFilterOnRowId()) {
       RowFilter rowFilter = new RowFilter(op, comparator);
       return rowFilter;
     }
     else if (filterConfig.isQualifierARangePrefix()) {
       QualifierFilter filter = new QualifierFilter(op, comparator);
       return filter;
     }
     else {
       final SingleColumnValueExcludeFilter valueFilter;
       valueFilter = new SingleColumnValueExcludeFilter(filterConfig.getColumnFamily(),
                                                        filterConfig.getColumnQualifier(),
                                                        op, comparator);
       valueFilter.setFilterIfMissing(filterConfig.isFilterOnIfMissing());
       valueFilter.setLatestVersionOnly(filterConfig.isFilterOnLatestVersionOnly());
       return valueFilter;
     }
   }
 
   protected Filter getCellFilter(FilterConfig filterConfig, CompareOp op, byte[] value) {
     return getCellFilter(filterConfig, op, new BinaryComparator(value));
   }
 
   protected Filter getInFilter(Collection inCollectin, FilterConfig config) {
     FilterList filterList = new FilterList(Operator.MUST_PASS_ONE);
     for (Object inObj : inCollectin) {
       filterList.addFilter(getCellFilter(config, CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes(inObj.toString()))));
     }
     return filterList;
   }
 
   protected String getPropertyName(String prefix, QueryParameter param) {
     final StringBuilder propertyName = new StringBuilder("");
     if (StringUtils.isNotBlank(prefix)) {
       propertyName.append(prefix).append('.');
     }
     if (param instanceof QueryParameterWithPropertyName) {
       propertyName.append(((QueryParameterWithPropertyName) param).getPropertyName());
     }
     return propertyName.toString();
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
     verifyAllEntitiesExists(false, states);
     put(states, false);
   }
 
   @Override
   public void update(Template... states) {
     verifyAllEntitiesExists(true, states);
     put(states, true);
   }
 
   protected void throwIfErrors(Collection<Future<String>> probableFutureErrors) throws IllegalStateException {
     Collection<String> probableErrors = new ArrayList<String>(probableFutureErrors.size());
     for (Future<String> delErr : probableFutureErrors) {
       String str;
       try {
         str = delErr.get();
       }
       catch (Exception ex) {
         logger.warn("Could not wait to complete deletion!", ex);
         str = "Could not complete deletion!";
       }
       if (StringUtils.isNotBlank(str)) {
         probableErrors.add(str);
       }
     }
     if (!probableErrors.isEmpty()) {
       throw new IllegalStateException(Arrays.toString(probableErrors.toArray(new String[probableErrors.size()])));
     }
   }
 
   protected void verifyAllEntitiesExists(boolean existenceExpected, Template... states) {
     List<Future<Boolean>> gets = new ArrayList<Future<Boolean>>(states.length);
     int i = 0;
     for (Template t : states) {
       try {
         final Comparable id = t.getId();
         if (logger.isInfoEnabled()) {
           logger.info(new StringBuilder("Adding ").append(id).append(" to index ").append(i).toString());
         }
         final Get get = new Get(infoProvider.getRowIdFromId((IdType) id));
         final int index = i;
         gets.add(executorService.executeAsynchronously(infoProvider.getMainTableName(),
                                                        new Callback<Boolean>() {
 
           @Override
           public Boolean call(HTableInterface tableInterface) throws Exception {
             final boolean exists = tableInterface.exists(get);
             if (logger.isDebugEnabled()) {
               logger.debug(new StringBuilder("Id ").append(id.toString()).append(" exists ").append(index).toString());
             }
             return exists;
           }
         }));
         i++;
       }
       catch (Exception ex) {
         logger.warn("Exception testing row existense...", ex);
       }
     }
     i = 0;
     for (Future<Boolean> future : gets) {
       Boolean exists = true;
       try {
         if (logger.isInfoEnabled()) {
           logger.info(new StringBuilder("Checking index ").append(i++).toString());
         }
         exists = future.get();
       }
       catch (Exception ex) {
         logger.warn("Exception testing row existense...", ex);
       }
       if (!existenceExpected && exists) {
         throw new IllegalArgumentException(
             "Some of the entities are already saved, so did not procced with any of them");
       }
       if (existenceExpected && !exists) {
         throw new IllegalArgumentException("Some of the entities are not saved, so did not procced with any of them");
       }
 
     }
   }
 
   protected void put(Template[] states, final boolean merge) throws IllegalStateException {
     LinkedHashMap<String, List<Put>> allPuts =
                                      new LinkedHashMap<String, List<Put>>();
     for (Template state : states) {
       if (!state.isValid()) {
         throw new IllegalStateException("Entity not in valid state!");
       }
       final LinkedHashMap<String, Put> puts;
       puts = getConverter().objectToRows(state, executorService, getLockType().equals(LockType.PESSIMISTIC));
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
       final List<Put> value = puts.getValue();
       final ArrayList<Put> valueCopy = new ArrayList<Put>(value);
       if (LockType.OPTIMISTIC.equals(getLockType()) && infoProvider.getVersionColumnFamily() != null && infoProvider.
           getVersionColumnQualifier() != null) {
         putOptimistically(puts, merge, value, valueCopy, states);
       }
       else {
         putNonOptimistically(puts, merge, value, valueCopy, states);
       }
     }
   }
 
   protected void putOptimistically(Entry<String, List<Put>> puts, final boolean merge, final List<Put> value,
                                    final ArrayList<Put> valueCopy, Template[] states) {
     //Merge first respecting lock type
     executorService.execute(puts.getKey(),
                             new Callback<Void>() {
 
       @Override
       public Void call(HTableInterface tableInterface) throws Exception {
         if (merge && mergeEnabled && mergeService != null) {
           mergeService.merge(tableInterface, value, getLockType());
         }
         return null;
       }
     });
     Collection<Future<String>> pFutures = new ArrayList<Future<String>>();
     final byte[] family = infoProvider.getVersionColumnFamily();
     final byte[] qualifier = infoProvider.getVersionColumnQualifier();
     for (final Put put : puts.getValue()) {
       final byte[] versionValue = DiffBasedMergeService.getLatestValue(put.get(family, qualifier)).getValue();
       put.add(family, qualifier, Bytes.toBytes(Bytes.toLong(versionValue) + 1));
       pFutures.add(executorService.executeAsynchronously(puts.getKey(), new Callback<String>() {
 
         @Override
         public String call(HTableInterface tableInterface) throws Exception {
           boolean puted = tableInterface.checkAndPut(put.getRow(), family, qualifier, versionValue, put);
           if (!puted) {
             return String.format(errorMessageFormat, infoProvider.getIdFromRowId(put.getRow()).toString(), Bytes.
                 toString(tableInterface.getTableName()));
           }
           return null;
         }
       }));
     }
     throwIfErrors(pFutures);
   }
 
   protected void putNonOptimistically(Entry<String, List<Put>> puts, final boolean merge, final List<Put> value,
                                       final ArrayList<Put> valueCopy, Template[] states) {
     try {
       executorService.execute(puts.getKey(), new Callback<Void>() {
 
         @Override
         public Void call(HTableInterface tableInterface) throws Exception {
           try {
             if (merge && mergeEnabled && mergeService != null) {
               mergeService.merge(tableInterface, value, getLockType());
             }
             tableInterface.put(valueCopy);
           }
           finally {
             for (Put put : value) {
               RowLock lock = put.getRowLock();
               if (lock != null) {
                 tableInterface.unlockRow(lock);
               }
             }
           }
           return null;
         }
       });
     }
     finally {
       for (Template state : states) {
         lockAttainer.evictFromCache(state);
       }
     }
   }
 
   @Override
   public void delete(Template... states) {
     verifyAllEntitiesExists(true, states);
     if (LockType.OPTIMISTIC.equals(getLockType())) {
       deleteOptimistically(states);
     }
     else {
       deleteNonOptimistically(states);
     }
   }
 
   protected void deleteOptimistically(Template[] states) throws IllegalStateException {
 
     Collection<Future<String>> deletes = new ArrayList<Future<String>>();
     final byte[] family = infoProvider.getVersionColumnFamily();
     final byte[] qualifier = infoProvider.getVersionColumnQualifier();
     for (final Template state : states) {
       if (!state.isValid()) {
         throw new IllegalStateException("Entity not in valid state!");
       }
       LinkedHashMap<String, Delete> dels = getConverter().objectToDeleteableRows(state, executorService, false);
       for (final Map.Entry<String, Delete> del : dels.entrySet()) {
         deletes.add(executorService.executeAsynchronously(del.getKey(), new Callback<String>() {
 
           @Override
           public String call(HTableInterface tableInterface) throws Exception {
             final Delete delVal = del.getValue();
             final byte[] version = state.getVersion() != null ? Bytes.toBytes(state.getVersion()) : null;
             boolean deleted = tableInterface.checkAndDelete(delVal.getRow(), family, qualifier, version, delVal);
             if (!deleted) {
               return String.format(errorMessageFormat, infoProvider.getIdFromRowId(delVal.getRow()).toString(), Bytes.
                   toString(tableInterface.getTableName()));
             }
             return null;
           }
         }));
       }
     }
     throwIfErrors(deletes);
   }
 
   protected void deleteNonOptimistically(Template[] states) throws IllegalStateException {
     LinkedHashMap<String, List<Delete>> allDels =
                                         new LinkedHashMap<String, List<Delete>>();
     for (Template state : states) {
       if (!state.isValid()) {
         throw new IllegalStateException("Entity not in valid state!");
       }
       LinkedHashMap<String, Delete> dels = getConverter().objectToDeleteableRows(state, executorService, getLockType().
           equals(LockType.PESSIMISTIC));
       for (Map.Entry<String, Delete> del : dels.entrySet()) {
         final List<Delete> putList;
         if (allDels.containsKey(del.getKey())) {
           putList = allDels.get(del.getKey());
         }
         else {
           putList = new ArrayList<Delete>();
           allDels.put(del.getKey(), putList);
         }
         putList.add(del.getValue());
       }
     }
     for (final Map.Entry<String, List<Delete>> dels : allDels.entrySet()) {
       try {
         executorService.execute(dels.getKey(),
                                 new Callback<Void>() {
 
           @Override
           public Void call(HTableInterface tableInterface) throws Exception {
             final List<Delete> value = dels.getValue();
             if (logger.isInfoEnabled()) {
               logger.info("Attempting to DELETE " + value);
             }
             try {
               final ArrayList<Delete> list = new ArrayList<Delete>(value);
               tableInterface.delete(list);
             }
             finally {
               if (logger.isDebugEnabled()) {
                 logger.debug("Attempting to Unlock rowlocks on DELETE for " + value);
               }
               for (Delete delete : value) {
                 if (logger.isDebugEnabled()) {
                   logger.debug("Attempting to Unlock rowlock for " + delete);
                 }
                 RowLock lock = delete.getRowLock();
                 if (lock != null) {
                   if (logger.isDebugEnabled()) {
                     logger.debug("Unlocking lock on delete " + lock.getLockId());
                   }
                   tableInterface.unlockRow(lock);
                 }
                 else {
                   if (getLockType().equals(LockType.PESSIMISTIC)) {
                     logger.warn("Null lock found!");
                   }
                 }
               }
             }
             return null;
           }
         });
       }
       finally {
         for (Template state : states) {
           lockAttainer.evictFromCache(state);
         }
       }
     }
   }
 }
