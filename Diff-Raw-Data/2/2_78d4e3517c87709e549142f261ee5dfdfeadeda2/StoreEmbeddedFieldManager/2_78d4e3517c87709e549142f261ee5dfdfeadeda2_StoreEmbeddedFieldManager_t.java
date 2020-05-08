 /**********************************************************************
 Copyright (c) 2011 Andy Jefferson and others. All rights reserved.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 Contributors:
    ...
 **********************************************************************/
 package org.datanucleus.store.hbase.fieldmanager;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.Put;
 import org.datanucleus.ClassLoaderResolver;
 import org.datanucleus.exceptions.NucleusException;
 import org.datanucleus.exceptions.NucleusUserException;
 import org.datanucleus.metadata.AbstractClassMetaData;
 import org.datanucleus.metadata.AbstractMemberMetaData;
 import org.datanucleus.metadata.Relation;
 import org.datanucleus.store.ExecutionContext;
 import org.datanucleus.store.ObjectProvider;
 import org.datanucleus.store.fieldmanager.AbstractFieldManager;
 import org.datanucleus.store.fieldmanager.FieldManager;
 import org.datanucleus.store.hbase.HBaseUtils;
 import org.datanucleus.store.types.ObjectStringConverter;
 
 /**
  * FieldManager for the persistence of a related embedded object (1-1 relation).
  */
 public class StoreEmbeddedFieldManager extends AbstractFieldManager
 {
     private final ObjectProvider sm;
     private final AbstractMemberMetaData mmd;
     private final Put put;
     private final Delete delete;
     private final String tableName;
 
     public StoreEmbeddedFieldManager(ObjectProvider sm, Put put, Delete delete, AbstractMemberMetaData mmd,
             String tableName)
     {
         this.sm = sm;
         this.mmd = mmd;
         this.put = put;
         this.delete = delete;
         this.tableName = tableName;
     }
 
     /* (non-Javadoc)
      * @see org.datanucleus.store.fieldmanager.FieldConsumer#storeBooleanField(int, boolean)
      */
     public void storeBooleanField(int fieldNumber, boolean value)
     {
         String familyName = HBaseUtils.getFamilyName(mmd, fieldNumber, tableName);
         String columnName = HBaseUtils.getQualifierName(mmd, fieldNumber);
         try
         {
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);
             oos.writeBoolean(value);
             oos.flush();
             put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
             oos.close();
             bos.close();
         }
         catch (IOException e)
         {
             throw new NucleusException(e.getMessage(), e);
         }
     }
 
     /* (non-Javadoc)
      * @see org.datanucleus.store.fieldmanager.FieldConsumer#storeByteField(int, byte)
      */
     public void storeByteField(int fieldNumber, byte value)
     {
         String familyName = HBaseUtils.getFamilyName(mmd, fieldNumber, tableName);
         String columnName = HBaseUtils.getQualifierName(mmd, fieldNumber);
         put.add(familyName.getBytes(), columnName.getBytes(), new byte[]{value});
     }
 
     /* (non-Javadoc)
      * @see org.datanucleus.store.fieldmanager.FieldConsumer#storeCharField(int, char)
      */
     public void storeCharField(int fieldNumber, char value)
     {
         String familyName = HBaseUtils.getFamilyName(mmd, fieldNumber, tableName);
         String columnName = HBaseUtils.getQualifierName(mmd, fieldNumber);
         AbstractMemberMetaData embMmd = mmd.getEmbeddedMetaData().getMemberMetaData()[fieldNumber];
         try
         {
             if (embMmd.isSerialized())
             {
                 ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos);
                 oos.writeChar(value);
                 oos.flush();
                 put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
                 oos.close();
                 bos.close();
             }
             else
             {
                 put.add(familyName.getBytes(), columnName.getBytes(), ("" + value).getBytes());
             }
         }
         catch (IOException e)
         {
             throw new NucleusException(e.getMessage(), e);
         }
     }
 
     /* (non-Javadoc)
      * @see org.datanucleus.store.fieldmanager.FieldConsumer#storeDoubleField(int, double)
      */
     public void storeDoubleField(int fieldNumber, double value)
     {
         String familyName = HBaseUtils.getFamilyName(mmd, fieldNumber, tableName);
         String columnName = HBaseUtils.getQualifierName(mmd, fieldNumber);
         try
         {
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);
             oos.writeDouble(value);
             oos.flush();
             put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
             oos.close();
             bos.close();
         }
         catch (IOException e)
         {
             throw new NucleusException(e.getMessage(), e);
         }
     }
 
     /* (non-Javadoc)
      * @see org.datanucleus.store.fieldmanager.FieldConsumer#storeFloatField(int, float)
      */
     public void storeFloatField(int fieldNumber, float value)
     {
         String familyName = HBaseUtils.getFamilyName(mmd, fieldNumber, tableName);
         String columnName = HBaseUtils.getQualifierName(mmd, fieldNumber);
         try
         {
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);
             oos.writeFloat(value);
             oos.flush();
             put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
             oos.close();
             bos.close();
         }
         catch (IOException e)
         {
             throw new NucleusException(e.getMessage(), e);
         }
     }
 
     /* (non-Javadoc)
      * @see org.datanucleus.store.fieldmanager.FieldConsumer#storeIntField(int, int)
      */
     public void storeIntField(int fieldNumber, int value)
     {
         String familyName = HBaseUtils.getFamilyName(mmd, fieldNumber, tableName);
         String columnName = HBaseUtils.getQualifierName(mmd, fieldNumber);
         try
         {
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);
             oos.writeInt(value);
             oos.flush();
             put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
             oos.close();
             bos.close();
         }
         catch (IOException e)
         {
             throw new NucleusException(e.getMessage(), e);
         }
     }
 
     /* (non-Javadoc)
      * @see org.datanucleus.store.fieldmanager.FieldConsumer#storeLongField(int, long)
      */
     public void storeLongField(int fieldNumber, long value)
     {
         String familyName = HBaseUtils.getFamilyName(mmd, fieldNumber, tableName);
         String columnName = HBaseUtils.getQualifierName(mmd, fieldNumber);
         try
         {
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);
             oos.writeLong(value);
             oos.flush();
             put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
             oos.close();
             bos.close();
         }
         catch (IOException e)
         {
             throw new NucleusException(e.getMessage(), e);
         }
     }
 
     /* (non-Javadoc)
      * @see org.datanucleus.store.fieldmanager.FieldConsumer#storeShortField(int, short)
      */
     public void storeShortField(int fieldNumber, short value)
     {
         String familyName = HBaseUtils.getFamilyName(mmd, fieldNumber, tableName);
         String columnName = HBaseUtils.getQualifierName(mmd, fieldNumber);
         try
         {
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);
             oos.writeShort(value);
             oos.flush();
             put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
             oos.close();
             bos.close();
         }
         catch (IOException e)
         {
             throw new NucleusException(e.getMessage(), e);
         }
     }
 
     /* (non-Javadoc)
      * @see org.datanucleus.store.fieldmanager.FieldConsumer#storeStringField(int, java.lang.String)
      */
     public void storeStringField(int fieldNumber, String value)
     {
         String familyName = HBaseUtils.getFamilyName(mmd, fieldNumber, tableName);
         String columnName = HBaseUtils.getQualifierName(mmd, fieldNumber);
         if (value == null)
         {
             delete.deleteColumn(familyName.getBytes(), columnName.getBytes());
         }
         else
         {
             AbstractMemberMetaData embMmd = mmd.getEmbeddedMetaData().getMemberMetaData()[fieldNumber];
             try
             {
                 if (embMmd.isSerialized())
                 {
                     ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     ObjectOutputStream oos = new ObjectOutputStream(bos);
                     oos.writeObject(value);
                     put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
                     oos.close();
                     bos.close();
                 }
                 else
                 {
                     put.add(familyName.getBytes(), columnName.getBytes(), value.getBytes());
                 }
             }
             catch (IOException e)
             {
                 throw new NucleusException(e.getMessage(), e);
             }
         }
     }
 
     /* (non-Javadoc)
      * @see org.datanucleus.store.fieldmanager.FieldConsumer#storeObjectField(int, java.lang.Object)
      */
     public void storeObjectField(int fieldNumber, Object value)
     {
         String familyName = HBaseUtils.getFamilyName(mmd, fieldNumber, tableName);
         String columnName = HBaseUtils.getQualifierName(mmd, fieldNumber);
         if (value == null)
         {
             // TODO What about delete-orphans?
             delete.deleteColumn(familyName.getBytes(), columnName.getBytes());
         }
         else
         {
             ExecutionContext ec = sm.getExecutionContext();
             ClassLoaderResolver clr = ec.getClassLoaderResolver();
             AbstractMemberMetaData embMmd = mmd.getEmbeddedMetaData().getMemberMetaData()[fieldNumber];
             int relationType = embMmd.getRelationType(clr);
             if ((relationType == Relation.ONE_TO_ONE_BI || relationType == Relation.ONE_TO_ONE_UNI) && embMmd.isEmbedded())
             {
                 // Embedded PC object
                 Class embcls = embMmd.getType();
                 AbstractClassMetaData embcmd = ec.getMetaDataManager().getMetaDataForClass(embcls, clr);
                 if (embcmd != null)
                 {
                     ObjectProvider embSM = ec.findObjectProviderForEmbedded(value, sm, embMmd);
                     embSM.addEmbeddedOwner(sm, fieldNumber);
                     FieldManager ffm = new StoreEmbeddedFieldManager(embSM, put, delete, embMmd, tableName);
                     embSM.provideFields(embcmd.getAllMemberPositions(), ffm);
                     return;
                 }
                 else
                 {
                     throw new NucleusUserException("Field " + embMmd.getFullFieldName() +
                         " specified as embedded but metadata not found for the class of type " + embMmd.getTypeName());
                 }
             }
             else if (relationType == Relation.ONE_TO_ONE_BI || relationType == Relation.ONE_TO_ONE_UNI ||
                 relationType == Relation.MANY_TO_ONE_BI || relationType == Relation.MANY_TO_ONE_UNI)
             {
                 // PC object, so make sure it is persisted
                 Object valuePC = sm.getExecutionContext().persistObjectInternal(value, sm, fieldNumber, -1);
                 if (embMmd.isSerialized())
                 {
                     // Persist as serialised into the column of this object
                     try
                     {
                         ByteArrayOutputStream bos = new ByteArrayOutputStream();
                         ObjectOutputStream oos = new ObjectOutputStream(bos);
                         oos.writeObject(value);
                         put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
                         oos.close();
                         bos.close();
                     }
                     catch (IOException e)
                     {
                         throw new NucleusException(e.getMessage(), e);
                     }
                 }
                 else
                 {
                     // Persist identity in the column of this object
                     Object valueId = ec.getApiAdapter().getIdForObject(valuePC);
                     try
                     {
                         ByteArrayOutputStream bos = new ByteArrayOutputStream();
                         ObjectOutputStream oos = new ObjectOutputStream(bos);
                         oos.writeObject(valueId);
                         put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
                         oos.close();
                         bos.close();
                     }
                     catch (IOException e)
                     {
                         throw new NucleusException(e.getMessage(), e);
                     }
                 }
             }
             else if (relationType == Relation.ONE_TO_MANY_UNI || relationType == Relation.ONE_TO_MANY_BI ||
                 relationType == Relation.MANY_TO_MANY_BI)
             {
                 // Collection/Map/Array
                 if (embMmd.hasCollection())
                 {
                     Collection collIds = new ArrayList();
                     Collection coll = (Collection)value;
                     Iterator collIter = coll.iterator();
                     while (collIter.hasNext())
                     {
                         Object element = collIter.next();
                         Object elementPC = sm.getExecutionContext().persistObjectInternal(element, sm, fieldNumber, -1);
                         Object elementID = sm.getExecutionContext().getApiAdapter().getIdForObject(elementPC);
                         collIds.add(elementID);
                     }
 
                     if (embMmd.isSerialized())
                     {
                         // Persist as serialised into the column of this object
                         try
                         {
                             ByteArrayOutputStream bos = new ByteArrayOutputStream();
                             ObjectOutputStream oos = new ObjectOutputStream(bos);
                             oos.writeObject(value);
                             put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
                             oos.close();
                             bos.close();
                         }
                         catch (IOException e)
                         {
                             throw new NucleusException(e.getMessage(), e);
                         }
                     }
                     else
                     {
                         // Persist comma-separated element key list into the column of this object
                         try
                         {
                             ByteArrayOutputStream bos = new ByteArrayOutputStream();
                             ObjectOutputStream oos = new ObjectOutputStream(bos);
                             oos.writeObject(collIds);
                             put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
                             oos.close();
                             bos.close();
                         }
                         catch (IOException e)
                         {
                             throw new NucleusException(e.getMessage(), e);
                         }
                     }
                 }
                 else if (embMmd.hasMap())
                 {
                     Map map = (Map)value;
                     Iterator<Map.Entry> mapIter = map.entrySet().iterator();
                     while (mapIter.hasNext())
                     {
                         Map.Entry entry = mapIter.next();
                         Object mapKey = entry.getKey();
                         Object mapValue = entry.getValue();
                         if (ec.getApiAdapter().isPersistable(mapKey))
                         {
                             ec.persistObjectInternal(mapKey, sm, fieldNumber, -1);
                         }
                         if (ec.getApiAdapter().isPersistable(mapValue))
                         {
                             ec.persistObjectInternal(mapValue, sm, fieldNumber, -1);
                         }
                     }
                     if (embMmd.isSerialized())
                     {
                         // Persist as serialised into the column of this object
                         try
                         {
                             ByteArrayOutputStream bos = new ByteArrayOutputStream();
                             ObjectOutputStream oos = new ObjectOutputStream(bos);
                             oos.writeObject(value);
                             put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
                             oos.close();
                             bos.close();
                         }
                         catch (IOException e)
                         {
                             throw new NucleusException(e.getMessage(), e);
                         }
                     }
                     else
                     {
                         // TODO Implement map persistence non-serialised
                         throw new NucleusException("Only currently support maps serialised with HBase. Mark the field as serialized");
                     }
                 }
                 else if (embMmd.hasArray())
                 {
                     Collection arrIds = new ArrayList();
                     for (int i=0;i<Array.getLength(value);i++)
                     {
                         Object element = Array.get(value, i);
                         Object elementPC = ec.persistObjectInternal(element, sm, fieldNumber, -1);
                         Object elementID = ec.getApiAdapter().getIdForObject(elementPC);
                         arrIds.add(elementID);
                     }
 
                     if (embMmd.isSerialized())
                     {
                         // Persist as serialised into the column of this object
                         try
                         {
                             ByteArrayOutputStream bos = new ByteArrayOutputStream();
                             ObjectOutputStream oos = new ObjectOutputStream(bos);
                             oos.writeObject(value);
                             put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
                             oos.close();
                             bos.close();
                         }
                         catch (IOException e)
                         {
                             throw new NucleusException(e.getMessage(), e);
                         }
                     }
                     else
                     {
                         // Persist list of array element ids into the column of this object
                         try
                         {
                             ByteArrayOutputStream bos = new ByteArrayOutputStream();
                             ObjectOutputStream oos = new ObjectOutputStream(bos);
                             oos.writeObject(arrIds);
                             put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
                             oos.close();
                             bos.close();
                         }
                         catch (IOException e)
                         {
                             throw new NucleusException(e.getMessage(), e);
                         }
                     }
                 }
             }
             else
             {
                 ObjectStringConverter strConv = 
                     sm.getExecutionContext().getNucleusContext().getTypeManager().getStringConverter(value.getClass());
                if (!embMmd.isSerialized() && strConv != null)
                 {
                     // Persist as a String
                     String strValue = strConv.toString(value);
                     put.add(familyName.getBytes(), columnName.getBytes(), strValue.getBytes());
                 }
                 else
                 {
                     try
                     {
                         ByteArrayOutputStream bos = new ByteArrayOutputStream();
                         ObjectOutputStream oos = new ObjectOutputStream(bos);
                         oos.writeObject(value);
                         put.add(familyName.getBytes(), columnName.getBytes(), bos.toByteArray());
                         oos.close();
                         bos.close();
                     }
                     catch (IOException e)
                     {
                         throw new NucleusException(e.getMessage(), e);
                     }
                 }
             }
         }
     }
 }
