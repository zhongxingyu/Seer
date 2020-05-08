 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.paxxis.chime.data;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.paxxis.chime.client.common.DataField;
 import com.paxxis.chime.client.common.DataFieldValue;
 import com.paxxis.chime.client.common.DataInstance;
 import com.paxxis.chime.client.common.FieldData;
 import com.paxxis.chime.client.common.InstanceId;
 import com.paxxis.chime.client.common.Shape;
 import com.paxxis.chime.client.common.User;
 import com.paxxis.chime.database.DataSet;
 import com.paxxis.chime.database.DatabaseConnection;
 import com.paxxis.chime.database.DateValue;
import com.paxxis.chime.database.DoubleData;
 import com.paxxis.chime.database.IDataValue;
 import com.paxxis.chime.database.StringData;
 import com.paxxis.chime.extension.ChimeExtensionManager;
 import com.paxxis.chime.service.Tools;
 
 /**
  *
  * @author Robert Englander
  */
 public class FieldDataUtils {
     private FieldDataUtils() {
     }
 
     public static DataInstance addFieldData(DataInstance instance, List<FieldData> fieldData, User user, DatabaseConnection database) throws Exception
     {
         database.startTransaction();
 
         DataInstance result = null;
 
         try
         {
             int position = 1;
             for (FieldData field : fieldData)
             {
                 createFieldData(instance, field, user, position++, database);
             }
 
             result = DataInstanceUtils.getInstance(instance.getId(), user, database, true, false);
 
             if (fieldData.size() > 0) {
                 FieldData fd = fieldData.get(0);
                 StringBuilder builder = new StringBuilder();
                 builder.append("Added ").append(fd.shape.getName()).append(" : ").append(fd.field.getName()).append(" data");
                 HistoryUtils.writeEvent(HistoryUtils.HistoryEventType.Modify, builder.toString(), instance, user, database);
             }
 
             database.commitTransaction();
         }
         catch (Exception e)
         {
             database.rollbackTransaction();
             throw new Exception(e.getMessage());
         }
 
         return result;
     }
 
     public static DataInstance removeFieldData(DataInstance instance, List<FieldData> fieldData, User user, DatabaseConnection database) throws Exception
     {
         database.startTransaction();
 
         DataInstance result = null;
 
         try
         {
             for (FieldData field : fieldData)
             {
                 deleteFieldData(instance, field, user, database);
             }
 
             result = DataInstanceUtils.getInstance(instance.getId(), user, database, true, false);
             if (fieldData.size() > 0) {
                 FieldData fd = fieldData.get(0);
                 StringBuilder builder = new StringBuilder();
                 builder.append("Removed ").append(fd.shape.getName()).append(" : ").append(fd.field.getName()).append(" data");
                 HistoryUtils.writeEvent(HistoryUtils.HistoryEventType.Modify, builder.toString(), instance, user, database);
             }
             database.commitTransaction();
         }
         catch (Exception e)
         {
             database.rollbackTransaction();
             throw new Exception(e.getMessage());
         }
 
         return result;
     }
 
     public static DataInstance modifyFieldData(DataInstance instance, List<FieldData> fieldData, User user, DatabaseConnection database) throws Exception
     {
         database.startTransaction();
 
         DataInstance result = null;
 
         try
         {
             for (FieldData field : fieldData)
             {
                 updateFieldData(instance, field, database);
             }
 
             result = DataInstanceUtils.getInstance(instance.getId(), user, database, true, false);
             if (fieldData.size() > 0) {
                 FieldData fd = fieldData.get(0);
                 StringBuilder builder = new StringBuilder();
                 builder.append("Modified ").append(fd.shape.getName()).append(" : ").append(fd.field.getName()).append(" data");
                 HistoryUtils.writeEvent(HistoryUtils.HistoryEventType.Modify, builder.toString(), instance, user, database);
             }
             database.commitTransaction();
 
         }
         catch (Exception e)
         {
             database.rollbackTransaction();
             throw new Exception(e.getMessage());
         }
 
         return result;
     }
 
     static void createFieldData(DataInstance instance, FieldData fieldData, User user, int position, DatabaseConnection database) throws Exception
     {
         String tableName = Tools.getTableSet();
         int colid = fieldData.field.getColumn();
         String column;
         Serializable value;
         Shape fieldShape = fieldData.field.getShape();
 
         if (fieldShape.isPrimitive())
         {
             column = "value";
 
             if (fieldShape.isNumeric()) {
                 tableName += "_Number";
 
                 if (fieldData.value instanceof String) {
                     value = fieldData.value.toString();
                } else if (fieldData.value instanceof Number) {
                	value = (Double)fieldData.value;
                 } else {
                     value = ((DataFieldValue)fieldData.value).getValue();
                 }
             } else if (fieldShape.isDate()) {
                 tableName += "_Timestamp";
 
                 if (fieldData.value instanceof Date) {
                     value = new DateValue(((Date)fieldData.value)).asSQLValue();
                 } else {
                     DataFieldValue dfv = (DataFieldValue)fieldData.value;
                     Date dt = (Date)dfv.getValue();
                     value = new DateValue(dt).asSQLValue();
                 }
             } else {
                 tableName += "_Text";
 
                 if (fieldData.value instanceof String) {
                     value = new StringData(fieldData.value.toString()).asSQLValue();
                 } else {
                     value = new StringData(((DataFieldValue)fieldData.value).getValue().toString()).asSQLValue();
                 }
             }
         }
         else
         {
             tableName += "_Reference";
 
             column = "foreign_id";
 
             DataFieldValue fieldValue = (DataFieldValue)fieldData.value;
             if (fieldValue.getValue() instanceof DataInstance) {
             	// this is a tabular row instance, and must first be created.
             	DataInstance rowInst = (DataInstance)fieldValue.getValue();
             	List<Shape> shapes = rowInst.getShapes();
             	String name = rowInst.getName();
             	String desc = "";
             	List<FieldData> fData = new ArrayList<FieldData>();
             	for (Shape s : shapes) {
             		List<DataField> rowFields = s.getFields();
             		for (DataField rowField : rowFields) {
                 		List<DataFieldValue> vals = rowInst.getFieldValues(s, rowField);
                     	for (DataFieldValue val : vals) {
                     		FieldData f = new FieldData();
                         	f.shape = s;
                         	f.field = rowField;
                         	f.value = val.getValue();
                         	fData.add(f);
                     	}
             		}
             	}
             	rowInst = DataInstanceUtils.createInstance(shapes, name, desc, null, new String[2], fData,
             			instance.getSocialContext().getScopes(), user, database);
                 value = rowInst.getId().getValue();
             } else {
                 value = String.valueOf(((DataFieldValue)fieldData.value).getReferenceId());
             }
 
             value = new StringData(value.toString()).asSQLValue();
         }
 
         InstanceId id = Tools.getNewId(Tools.DEFAULT_EXTID);
 
         String sql = "insert into " + tableName + " (id,instance_id, datatype_id, datatype_column, " + column +
                 ", position) values ('" + id + "', '" + instance.getId() + "'" +
                 ", '" + fieldData.shape.getId() + "'" +
                 ", " + colid +
                 ", " + value + 
                 ", " + position + ")";
 
 
         database.executeStatement(sql);
     }
 
     static void deleteAllFieldData(DataInstance instance, Shape shape, DatabaseConnection database) throws Exception {
         String sql = "delete from DataInstance_Number where instance_id = '" + instance.getId() +
                 "' and datatype_id = '" + shape.getId() + "'";
         database.executeStatement(sql);
 
         sql = "delete from DataInstance_Reference where instance_id = '" + instance.getId() +
                 "' and datatype_id = '" + shape.getId() + "'";
         database.executeStatement(sql);
 
         sql = "delete from DataInstance_Timestamp where instance_id = '" + instance.getId() +
         		"' and datatype_id = '" + shape.getId() + "'";
         database.executeStatement(sql);
 
         sql = "delete from DataInstance_Text where instance_id = '" + instance.getId() +
                 "' and datatype_id = '" + shape.getId() + "'";
         database.executeStatement(sql);
     }
 
     private static void deleteFieldData(DataInstance instance, FieldData fieldData, User user, DatabaseConnection database) throws Exception
     {
         String tableName = Tools.getTableSet();
         int colid = fieldData.field.getColumn();
         Shape fieldShape = fieldData.field.getShape();
 
         if (fieldShape.isPrimitive()) {
             if (fieldShape.isNumeric()) {
                 tableName += "_Number";
             } else if (fieldShape.isDate()) {
                 tableName += "_Timestamp";
             } else {
                 tableName += "_Text";
             }
         } else {
             // if the deleted reference is to an instance of a tabular shape, then
             // the instance itself gets deleted too
             if (fieldShape.isTabular()) {
                 DataInstance inst = (DataInstance)((DataFieldValue)fieldData.value).getValue();
                 DataInstanceUtils.deleteInstance(inst, user, database);
             }
 
             tableName += "_Reference";
         }
 
         String sql = "delete from " + tableName + " where id = '" + ((DataFieldValue)fieldData.value).getId() +
                 "' and datatype_id = '" + fieldData.shape.getId() +
                 "' and datatype_column = " + colid;
         database.executeStatement(sql);
     }
 
     static void updateFieldData(DataInstance instance, FieldData fieldData, DatabaseConnection database) throws Exception
     {
         String tableName = Tools.getTableSet();
         int colid = fieldData.field.getColumn();
         String column;
         String value;
 
         if (fieldData.field.getShape().isPrimitive())
         {
             column = "value";
 
             if (fieldData.field.getShape().isNumeric()) {
                 tableName += "_Number";
                 value = fieldData.value.toString();
             } else if (fieldData.field.getShape().isDate()) {
                 tableName += "_Timestamp";
                 if (fieldData.value instanceof Date) {
                     value = new DateValue(((Date)fieldData.value)).asSQLValue();
                 } else {
                     DataFieldValue dfv = (DataFieldValue)fieldData.value;
                     Date dt = (Date)dfv.getValue();
                     value = new DateValue(dt).asSQLValue();
                 }
             } else {
                 tableName += "_Text";
 
                 if (fieldData.value instanceof String) {
                     value = new StringData(fieldData.value.toString()).asSQLValue();
                 } else {
                     value = new StringData(((DataFieldValue)fieldData.value).getValue().toString()).asSQLValue();
                 }
             }
         }
         else
         {
             tableName += "_Reference";
 
             column = "foreign_id";
 
             if (fieldData.value instanceof DataInstance)
             {
                 value = String.valueOf(((DataInstance)fieldData.value).getId());
             }
             else
             {
                 value = String.valueOf(((DataFieldValue)fieldData.value).getReferenceId());
             }
         }
 
         String sql = "update " + tableName + " set " + column + " = " + value + " where id = '" + ((DataFieldValue)fieldData.value).getId() +
                 "' and datatype_id = '" + fieldData.shape.getId() +
                 "' and datatype_column = " + colid;
         database.executeStatement(sql);
     }
 
     public static List<DataFieldValue> getInternalFieldValues(DatabaseConnection database, Shape shape, int col, Shape type2, InstanceId instanceId) throws Exception
     {
         String tableName = Tools.getTableSet();
         if (type2.isNumeric()) {
             tableName += "_Number";
         } else if (type2.isDate()) {
             tableName += "_Timestamp";
         } else {
             tableName += "_Text";
         }
 
         String sql = "SELECT A.value Value, A.id theId, A.timestamp timestamp from " + tableName + " A where A.instance_id = '" + instanceId +
                 "' and datatype_id = '" + shape.getId() + "' and datatype_column = " + col + " order by position";
         DataSet dataSet = database.getDataSet(sql, true);
         List<DataFieldValue> values = new ArrayList<DataFieldValue>();
 
         boolean found = dataSet.next();
         while (found)
         {
             IDataValue value = dataSet.getFieldValue("Value");
             IDataValue instId = dataSet.getFieldValue("theId");
             IDataValue timestamp = dataSet.getFieldValue("timestamp");
 
             Serializable sval;
             if (value instanceof DateValue) {
                 sval = value.asDate();
            } else if (value instanceof DoubleData) {
            	sval = value.asDouble();
             } else {
                 sval = value.asString();
             }
 
             // we should really support the various internal (primitive) data types.  for
             // now we just treat everything like a string
             values.add(new DataFieldValue(sval, type2.getId(), instId.asInstanceId(), timestamp.asDate()));
             found = dataSet.next();
         }
 
         dataSet.close();
 
         return values;
     }
 
     public static List<DataFieldValue> getExternalFieldValues(DatabaseConnection database, Shape shape, int col, Shape shape2, InstanceId instanceId) throws Exception
     {
         // step 1 gets locally sourced references.  this is true because the foreign id join to the DataInstance table
         // will result in no hits if the reference is to an externally sourced data instance
         String sql = "SELECT A.id instId, A.timestamp timestamp, B.name, B.id refId FROM " + Tools.getTableSet() + "_Reference A, "
             + Tools.getTableSet() + " B, " + Tools.getTableSet() + "_Type C"
             + " where A.datatype_id = '" + shape.getId() + "' and A.datatype_column = " + col +
             " and A.instance_id = '" + instanceId + "' and A.foreign_id = B.id and B.id = C.instance_id";
 
         if (!shape2.getId().equals(Shape.REFERENCE_ID)) {
             sql += " and C.datatype_id = '" + shape2.getId() + "'";
         }
 
         sql += " order by A.position";
         
         DataSet dataSet = database.getDataSet(sql, true);
         List<DataFieldValue> values = new ArrayList<DataFieldValue>();
 
         boolean found = dataSet.next();
         while (found)
         {
             IDataValue name = dataSet.getFieldValue("name");
             IDataValue refId = dataSet.getFieldValue("refId");
             IDataValue instId = dataSet.getFieldValue("instId");
             IDataValue timestamp = dataSet.getFieldValue("timestamp");
             
             values.add(new DataFieldValue(refId.asInstanceId(), name.asString(), shape2.getId(), instId.asInstanceId(), timestamp.asDate()));
 
             found = dataSet.next();
         }
 
         dataSet.close();
 
         // step 2 gets references sourced by extensions.  get the externally sourced foreign ids, then ask the appropriate extension
         // to give us back the instances
         sql = "SELECT A.foreign_id refId, A.timestamp timestamp FROM " + Tools.getTableSet() + "_Reference A "
             + " where A.datatype_id = '" + shape.getId() + "' and A.datatype_column = " + col +
             " and A.instance_id = '" + instanceId + "' and A.foreign_id not like '%00'";
 
         dataSet = database.getDataSet(sql, true);
         found = dataSet.next();
         List<InstanceId> ids = new ArrayList<InstanceId>();
         List<Date> timestamps = new ArrayList<Date>();
         while (found)
         {
             IDataValue refId = dataSet.getFieldValue("refId");
             ids.add(InstanceId.create(refId.asString()));
             IDataValue ts = dataSet.getFieldValue("timestamp");
             timestamps.add(ts.asDate());
             found = dataSet.next();
         }
 
         dataSet.close();
 
         if (!ids.isEmpty()) {
             List<DataInstance> instances = ChimeExtensionManager.instance().getDataInstances(ids);
             int idx = 0;
             for (DataInstance inst : instances) {
                 values.add(new DataFieldValue(inst.getId(), inst.getName(), shape2.getId(), instanceId, timestamps.get(idx++)));
             }
         }
 
         return values;
     }
 
 }
