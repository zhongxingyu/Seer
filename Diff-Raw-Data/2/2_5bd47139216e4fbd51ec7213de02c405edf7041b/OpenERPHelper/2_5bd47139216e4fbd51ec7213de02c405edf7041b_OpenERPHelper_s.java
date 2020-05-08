 package com.debortoliwines.openerp.reporting.di;
 
 /*
  *   This file is part of OpenERPJavaReportHelper
  *
  *   OpenERPJavaAPI is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   OpenERPJavaAPI is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with OpenERPJavaAPI.  If not, see <http://www.gnu.org/licenses/>.
  *
  *   Copyright 2012 De Bortoli Wines Pty Limited (Australia)
  */
 
 import java.beans.DefaultPersistenceDelegate;
 import java.beans.XMLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.apache.xmlrpc.XmlRpcException;
 
 import com.debortoliwines.openerp.api.Field;
 import com.debortoliwines.openerp.api.FieldCollection;
 import com.debortoliwines.openerp.api.FilterCollection;
 import com.debortoliwines.openerp.api.ObjectAdapter;
 import com.debortoliwines.openerp.api.OpeneERPApiException;
 import com.debortoliwines.openerp.api.Row;
 import com.debortoliwines.openerp.api.RowCollection;
 import com.debortoliwines.openerp.api.Session;
 import com.debortoliwines.openerp.api.Field.FieldType;
 import com.debortoliwines.openerp.api.FilterCollection.FilterOperator;
 import com.debortoliwines.openerp.reporting.di.OpenERPConfiguration.DataSource;
 
 /**
  * Main helper class to facilitate in fetching data using the OpenERPConfiguration
  * @author Pieter van der Merwe
  * @since  Jan 5, 2012
  */
 public class OpenERPHelper {
 
   private HashMap<String, ObjectAdapter> objectAdapterCache = new HashMap<String, ObjectAdapter>();
 
   private Session currentSession = null;
   private OpenERPConfiguration sessionConfig = null;
   private final String GET_FIELDS_PARAM = "getFields";
 
   /**
    * Fetch data based on the OpenERPConfiguration
    * @param config OpenERPConfiguration to use to collect data
    * @param parameters Parameters that will be passed to a custom procedure if the DataSource is CUSTOM
    * @return Object[][] of data where each row only has the fields specified in the config
    * @throws Exception
    */
   public Object[][] getData(OpenERPConfiguration config, HashMap<String, Object> parameters) throws Exception{
 
     if (config.getDataSource() == DataSource.STANDARD){
       ArrayList<OpenERPFieldInfo> selectedFields = config.getSelectedFields();
   
       // Build the query items with the filters
       OpenERPQueryItem root = buildQueryItems(config.getModelName(), selectedFields, config.getFilters());
   
       // Build a unique list of fields names that will be copied to a row
       ArrayList<String> fields = new ArrayList<String>();
       ArrayList<SortField> sortFields = new ArrayList<SortField>();
       for (int i = 0; i < selectedFields.size(); i++){
         OpenERPFieldInfo field = selectedFields.get(i);
         fields.add(field.getModelPathName() + "-|-" + field.getInstanceNum() + "-|-" + field.getFieldName());
         if (field.getSortIndex() > 0)
           sortFields.add(new SortField(i, field.getFieldType(), field.getSortIndex(), field.getSortDirection()));
       }
       
       Collections.sort(sortFields,new SourceFieldIndexComparator());
       
       // Get the data
       ArrayList<Object[]> results = getSearchData(fields, getSession(config), root, null);
       
       // Sort the data if any sorting is active
       if (sortFields.size() > 0){
         Collections.sort(results,new ResultSortComparator(sortFields));
       }
       
       return results.toArray(new Object[][]{});
     }
     else{
       ArrayList<Object[]> rows = new ArrayList<Object[]>();
       
       @SuppressWarnings("unchecked")
       HashMap<String, Object> params = (HashMap<String, Object>) parameters.clone();
 
       // We need the field names to extract data from the unordered HashMap that we receive back
       FieldCollection fields = getCustomFields(config, params);
       
       params.put(GET_FIELDS_PARAM, false);
       ObjectAdapter adapter = getObjectAdapter(config, config.getModelName());
       RowCollection results = adapter.callFunction(config.getCustomFunctionName(), new Object[] {params}, fields);
       
       for (Row result : results){
         Object[] row = new Object[fields.size()];
         
         for (int i = 0; i < fields.size(); i++){
           row[i] = result.get(fields.get(i));
         }
         
         rows.add(row);
       }
       
       return rows.toArray(new Object[][]{});
     }
   }
   
   /**
    * Returns a list of field information for the configuration
    * @param config Configuration that will be used to collect data
    * @param parameters  Parameters that should be passed on to a custom procedure if the DataSource is CUSTOM.
    * @return
    * @throws Exception
    */
   public ArrayList<OpenERPFieldInfo> getFields(OpenERPConfiguration config, HashMap<String, Object> parameters) throws Exception{
     if (config.getDataSource() == DataSource.STANDARD){
       return config.getSelectedFields();
     }
     else{
       ArrayList<OpenERPFieldInfo> customFields = new ArrayList<OpenERPFieldInfo>();
       
       FieldCollection fields = getCustomFields(config, parameters);
       
       for (Field fld : fields){
         customFields.add(new OpenERPFieldInfo(config.getModelName(), 1, fld.getName(), fld.getName(), null, fld.getType(), null, 0, 0));
       }
       return customFields;
     }
   }
   
   /**
    * Returns a FieldCollection based on the fields returned by a custom function.  A parameter is passed to the custom
    * procedure to request field information.  The custom procedure can send a dummy row or a row with field information.
    * @param config Configuration that hold connection details and the custom function
    * @param parameters Parameters that will be passed on to the custom procedure.  An additional parameter will be added.  Constant GET_FIELDS_PARAM = true
    * @return
    * @throws Exception
    */
   private FieldCollection getCustomFields(OpenERPConfiguration config, HashMap<String, Object> parameters) throws Exception{
     @SuppressWarnings("unchecked")
     HashMap<String, Object> params = (HashMap<String, Object>) parameters.clone();
     
     params.put(GET_FIELDS_PARAM, true);
     ObjectAdapter adapter = getObjectAdapter(config, config.getModelName());
     FieldCollection fields = adapter.callFieldsFunction(config.getCustomFunctionName(), new Object[] {params});
     return fields;
   }
 
   
   /**
    * Fetch data based on the OpenERPConfiguration.  Should only be called if the DataSource is STANDARD.
    * This function is called recursively for every child of a QueryItem.
    * @param fields Array of fields that are expected in the unique key format: modelPathName + "-|-" + instanceNum + "-|-" + fieldName
    * @param s Established OpenERP session to use.
    * @param item Current Query item that should be processed.
    * @param relatedFieldValue If this is a related query (many2one, one2many etc), the ID or IDs for the related model. 
    * @return A list of Object[] that have the correct indexes filled according to the selected fields on the current QueryItem
    * @throws XmlRpcException
    * @throws OpeneERPApiException
    */
   private ArrayList<Object[]> getSearchData(ArrayList<String> fields, Session s, OpenERPQueryItem item, Object relatedFieldValue) throws XmlRpcException, OpeneERPApiException{
 
     ArrayList<Object[]> finalRows = new ArrayList<Object[]>();
 
     // Get the objectAdapter from cache
     if (!objectAdapterCache.containsKey(item.getModelName())){
       objectAdapterCache.put(item.getModelName(), new ObjectAdapter(s, item.getModelName()));
     }
 
     ObjectAdapter adapter = objectAdapterCache.get(item.getModelName());
     RowCollection adapterRows = null;
 
     // Build filters
     FilterCollection filters = new FilterCollection();
     ArrayList<OpenERPFilterInfo> itemAdditionalFilters = item.getFilters();
     if (itemAdditionalFilters != null){
       for (OpenERPFilterInfo filter : itemAdditionalFilters){
         if (filter.getOperator().equalsIgnoreCase("not"))
           filters.add(FilterOperator.NOT);
         else if (filter.getOperator().equalsIgnoreCase("or"))
           filters.add(FilterOperator.OR);
 
         try {
           filters.add(filter.getFieldName(), filter.getComparator(), filter.getValue());
         } catch (OpeneERPApiException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
       }
     }
 
     // Fetch Rows
     // If this is a child object, add the id = xxx filter
     if (relatedFieldValue != null){
       Object [] idList = null;
       if (item.getRelationType() == FieldType.MANY2ONE
           && relatedFieldValue instanceof Object[]
               && ((Object[]) relatedFieldValue).length == 2){
         idList = new Object[]{((Object[]) relatedFieldValue)[0]};
       }
       else idList = (Object[]) relatedFieldValue;
 
       // If there are no additional filters, just call the readObject function to save time
       if (filters.size() == 0){
         adapterRows = adapter.readObject(idList, item.getFields().toArray(new String[]{}));
       }
       else {
         if (item.getRelationType() == FieldType.MANY2ONE){
          filters.add(0,"id", "=", idList);
         }
         else{
           filters.add(0,"id", "in", idList);
         }
 
         adapterRows = adapter.searchAndReadObject(filters, item.getFields().toArray(new String[]{}));
       }
     }
     else adapterRows = adapter.searchAndReadObject(filters, item.getFields().toArray(new String[]{}));
 
     // For each row fetched
     for (Row adapterRow : adapterRows){
 
       ArrayList<Object[]> localRows = new ArrayList<Object[]>();
 
       Object[] localRow = new Object[fields.size()];
       for (String fieldName : item.getFields()){
         String key = item.getModelPath() + "-|-" + item.getInstanceNum() + "-|-" + fieldName;
         int fieldIndex = fields.indexOf(key);
         if (fieldIndex >= 0){
           localRow[fieldIndex] = adapterRow.get(fieldName);
         }
       }
       localRows.add(localRow);
 
       // Get the data for every child and perform
       for (OpenERPQueryItem child : item.getChildItems()){
         Object childIDs = adapterRow.get(child.getRelatedField());
         if (childIDs == null)
           continue;
 
         ArrayList<Object[]> childRows = getSearchData(fields, s, child, childIDs);
 
         ArrayList<Object[]> combinedRows = new ArrayList<Object[]>();
         for (Object [] row : localRows){
           for (Object [] childRow : childRows){
             Object[] combinedRow = Arrays.copyOf(row, localRow.length);
             // Now copy all child values that were set.  This will include the direct child's fields
             // but also the child's children.  That is why we don't use child.fields but take everything with values.
             for (int i = 0; i < childRow.length; i++){
               if (childRow[i] != null){
                 combinedRow[i] = childRow[i];
               }
             }
             combinedRows.add(combinedRow);
           }
         }
 
         // Do an outer join.  Remove this "if" line for an inner join.
         if (combinedRows.size() > 0)
           localRows = combinedRows;
       }
 
       finalRows.addAll(localRows);
     }
 
     return finalRows;
 
   }
 
   /**
    * Builds a QueryItem list that will be used to build the fetch data from OpenERP.
    * One QueryItem for each call to the database.  The key is the modelPath and the instance number.
    * @param modelName Main model that will be called.
    * @param selectedFields  Selected fields that has a modelPath to the main model specified in modelName
    * @param filters Filter that are linked to modelPath/instanceNumber entries.
    * @return
    */
   public OpenERPQueryItem buildQueryItems(String modelName, ArrayList<OpenERPFieldInfo> selectedFields, ArrayList<OpenERPFilterInfo> filters){
 
     OpenERPQueryItem root = new OpenERPQueryItem("", FieldType.ONE2MANY, modelName, 1);
     // Needs to be done after the parent has been set, to get the full model path
     root.setFilters(getFilter(filters, root.getModelPath(), root.getInstanceNum()));
 
     for (OpenERPFieldInfo path : selectedFields){
       buildQueryItems(root, path, filters);
     }
 
     return root;
   }
 
   /**
    * Builds child query items and links them to a parent.  There is one queryItem per modelPath/instanceNumber.  Each field
    * is added as a list to each queryItem.  Note that any intermediate table is also added
    * @param rootItem The main/root item.
    * @param field The field info that has a path to the rootItem that needs to be built
    * @param filters Filter that will be linked to the relevant queryItem (modelPath/instanceNumber)
    * @return
    */
   private OpenERPQueryItem buildQueryItems(OpenERPQueryItem rootItem, OpenERPFieldInfo field, ArrayList<OpenERPFilterInfo> filters){
     if (field == null)
       return null;
 
     // Do a recursive call to build queryItems from the top-down
     OpenERPQueryItem parentItem = buildQueryItems(rootItem, field.getParentField(), filters);
 
     OpenERPQueryItem item = null;
     if (parentItem == null)
     {
       item = rootItem;
     }
     else{
       String relatedfield = field.getParentField().getFieldName();
       item  = parentItem.getChildQuery(relatedfield, field.getInstanceNum());
       if (item == null){
         item = new OpenERPQueryItem(relatedfield, field.getParentField().getFieldType(), field.getModelName(), field.getInstanceNum());
         parentItem.addChildQuery(item);
         // Needs to be done after the parent has been set, to get the full model path
         item.setFilters(getFilter(filters, item.getModelPath(), item.getInstanceNum()));
       }
     }
 
     item.addField(field.getFieldName());
     return item;
 
   }
 
   /**
    * Helper function to return a new list of filters for a specific modelPach/instanceNumber
    * @param filters Full list of filters
    * @param modelPath Model path to find filters for
    * @param instanceNum Instance number of the model path to find filters for
    * @return
    */
   private ArrayList<OpenERPFilterInfo> getFilter(ArrayList<OpenERPFilterInfo> filters, String modelPath, int instanceNum){
     ArrayList<OpenERPFilterInfo> filterList = new ArrayList<OpenERPFilterInfo>();
 
     if (filters != null){
       for (OpenERPFilterInfo item : filters){
         if (item.getModelPath() != null
             && item.getModelPath().equals(modelPath)
             && item.getInstanceNum() == instanceNum)
         {
           filterList.add(item);
         }
       }
     }
 
     return filterList;
   }
 
   /**
    * Returns a logged in session based on the configuration passed in
    * @param config Configuration to use
    * @return
    * @throws Exception
    */
   public Session getSession(OpenERPConfiguration config) throws Exception{
     // If the configuration hasn't changed, don't start a new session.  Just return the last created one
     if (currentSession == null
         || sessionConfig == null
         || !sessionConfig.getHostName().equals(config.getHostName())
         || sessionConfig.getPortNumber() != config.getPortNumber()
         || !sessionConfig.getDatabaseName().equals(config.getDatabaseName())
         || !sessionConfig.getUserName().equals(config.getUserName())
         || !sessionConfig.getPassword().equals(config.getPassword())){
       currentSession = new Session(config.getHostName(), config.getPortNumber(), config.getDatabaseName(), config.getUserName(), config.getPassword()); 
       currentSession.startSession();
     }
     return currentSession;
   }
 
   /**
    * Returns an object adapter for a specific model.  It also starts a new OpenERP session if a session hasn't been created
    * @param config Configuration to use for the session connection
    * @param modelName Model name to create an object adapter for
    * @return
    * @throws Exception
    */
   public ObjectAdapter getObjectAdapter(OpenERPConfiguration config, String modelName) throws Exception{
     Session s = getSession(config);
     return new ObjectAdapter(s, modelName);
   }
 
   /**
    * Helper function to configure an XMLEncoder to write a OpenERPConfiguration object to XML
    * @param encoder
    */
   public void setupXMLEncoder(XMLEncoder encoder){
     encoder.setPersistenceDelegate(OpenERPFieldInfo.class,
         new DefaultPersistenceDelegate(
             new String[]{ "modelName",
                 "instanceNum",
                 "fieldName",
                 "renamedFieldName",
                 "parentField",
                 "fieldType",
                 "relatedChildModelName",
                 "sortIndex", 
                 "sortDirection"}));
   }
   
   /**
    * Private class to hold sort field information that is used by the comparator to sort the result set
    * @author Pieter van der Merwe
    * @since  Feb 15, 2012
    */
   private class SortField{
     final private int sourceFieldIndex;
     final private FieldType sourceFieldType;
     final private int sortIndex;
     final private int sortDirection;
     
     /**
      * Default constructor
      * @param sourceFieldIndex Where the field is located in the source resultset
      * @param sourceFieldType The OpenERP field type of the source field.  Used for type comparisons.
      * @param sortIndex The index number of the field.  Results will be sorted with fields, starting at 1
      * @param sortDirection 0 for ascending or 1 for descending
      */
     public SortField(int sourceFieldIndex, FieldType sourceFieldType, int sortIndex, int sortDirection){
       this.sourceFieldIndex = sourceFieldIndex;
       this.sourceFieldType = sourceFieldType;
       this.sortIndex = sortIndex;
       this.sortDirection = sortDirection;
     }
     
     /**
      * Returns the index this field has in the source result set
      * @return
      */
     public int getSourceFieldIndex() {
       return sourceFieldIndex;
     }
     
     /**
      * Returns the intended sort direction.  0 for ascending, 1 for descending
      * @return
      */
     public int getSortDirection() {
       return sortDirection;
     }
     
     /**
      * Returns the sort index.  Sort fields with lower indexes will be used to sort first
      * @return
      */
     public int getSortIndex() {
       return sortIndex;
     }
     
     /**
      * Returns the OpenERP field type of the source field.  Used for type comparisons. 
      * @return
      */
     public FieldType getSourceFieldType() {
       return sourceFieldType;
     }
   }
   
   /**
    * Sorts a SortField collection by sortIndex
    * @author Pieter van der Merwe
    * @since  Feb 15, 2012
    */
   private class SourceFieldIndexComparator implements Comparator<SortField> { 
     @Override
     public int compare(SortField arg0, SortField arg1) {
       Integer source = new Integer(arg0.getSortIndex());
       Integer target = new Integer(arg1.getSortIndex());
       
       return source.compareTo(target);
     }
   }
   
   /**
    * Sorts an Object[] result set using the list of sort fields specified. 
    * @author Pieter van der Merwe
    * @since  Feb 15, 2012
    */
   private class ResultSortComparator implements Comparator<Object[]> { 
     final private ArrayList<SortField> sortFields; 
     
     /**
      * Default constructor
      * @param sortFields The list of fields that will be used to sort the result set with
      */
     public ResultSortComparator(ArrayList<SortField> sortFields){
       this.sortFields = sortFields;
     }
     
     @Override
     public int compare(Object[] arg0, Object[] arg1) {
       // Start comparing at the first level (0)
       return compareLevel(arg0, arg1, 0);
     }
     
     /**
      * Compares two values based on the specified OpenERP field type.
      * @param sourceValue Source value to compare with targetValue
      * @param targetValue Target value to compare with sourceValue
      * @param fieldType OpenERP field type to determine the type of comparison (Integer vs Date etc)
      * @return
      */
     private int compareValues (Object sourceValue, Object targetValue, FieldType fieldType) {
       if (sourceValue == null && targetValue == null)
         return 0;
       
       if (sourceValue == null && targetValue != null)
         return -1;
       
       if (sourceValue != null && targetValue == null)
         return 1;
       
       switch (fieldType) {
       case BINARY:
       case CHAR:
       case TEXT:
       case SELECTION:
         return sourceValue.toString().toLowerCase().compareTo(targetValue.toString().toLowerCase());
       case INTEGER:
         return ((Integer) sourceValue).compareTo((Integer) targetValue);
       case BOOLEAN:
         return ((Boolean) sourceValue).compareTo((Boolean) targetValue);
       case FLOAT:
         return ((Double) sourceValue).compareTo((Double) targetValue);
       case DATE:
       case DATETIME:
         return ((Date) sourceValue).compareTo((Date) targetValue);
       // Sort on the Child ID.  Don't know if you would do it, but its here in any case 
       case MANY2ONE:
         return ((Integer) ((Object[]) sourceValue)[0]).compareTo((Integer) ((Object[]) targetValue)[0]);
       // Just sort on the first value.  Doesn't really make sense to sort on these fields
       // because the child ids may be sorted arbitrarily in any case.
       case MANY2MANY:
       case ONE2MANY:
         return ((Integer) ((Object[]) sourceValue)[0]).compareTo((Integer) ((Object[]) targetValue)[0]);
       default:
         return 0;
       }
     }
     
     /**
      * Compares two rows with one another using the specified fieldIndex to determine the sort field that will be used.
      * If two fields are the same, this function will call itself with an incremented sortLevel to compare at the next level.
      * @param arg0 Object[] that will be compared with the arg1
      * @param arg1 Object[] that will be compared with the arg2
      * @param sortLevel Level(or index) of the sortArray to use for the comparison
      * @return
      */
     private int compareLevel(Object[] arg0, Object[] arg1, int sortLevel){
       SortField sortField = sortFields.get(sortLevel);
 
       int comparison = compareValues(arg0[sortField.getSourceFieldIndex()], arg1[sortField.getSourceFieldIndex()], sortField.getSourceFieldType());
       
       // Switch the order if sorting in descending order
       if (sortField.getSortDirection() == 1)
         comparison = comparison * -1;
       
       // If the two results are equal, go down a level and compare on the next level
       if (comparison == 0 && sortLevel + 1 < sortFields.size())
         return compareLevel(arg0, arg1, sortLevel + 1);
       
       return comparison;
     }
   }
 }
