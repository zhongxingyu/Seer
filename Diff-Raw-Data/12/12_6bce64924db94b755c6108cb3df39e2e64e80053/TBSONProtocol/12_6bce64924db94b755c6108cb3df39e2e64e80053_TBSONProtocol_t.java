 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.breizhbeans.thrift.tools.thriftmongobridge.protocol;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
 import java.nio.ByteBuffer;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Stack;
 
 import org.apache.thrift.TBase;
 import org.apache.thrift.TException;
 import org.apache.thrift.TFieldIdEnum;
 import org.apache.thrift.meta_data.ListMetaData;
 import org.apache.thrift.meta_data.MapMetaData;
 import org.apache.thrift.meta_data.SetMetaData;
 import org.apache.thrift.meta_data.StructMetaData;
 import org.apache.thrift.protocol.TField;
 import org.apache.thrift.protocol.TList;
 import org.apache.thrift.protocol.TMap;
 import org.apache.thrift.protocol.TMessage;
 import org.apache.thrift.protocol.TProtocol;
 import org.apache.thrift.protocol.TProtocolFactory;
 import org.apache.thrift.protocol.TSet;
 import org.apache.thrift.protocol.TStruct;
 import org.apache.thrift.protocol.TType;
 import org.apache.thrift.transport.TTransport;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 public class TBSONProtocol extends TProtocol {
 	public static final char QUOTE = '"';
 
 	private static ThreadLocal<Stack<Context>> threadSafeContextStack = new ThreadLocal<Stack<Context>>();
 	private static ThreadLocal<DBObject> threadSafeDBObject = new ThreadLocal<DBObject>();
 	private static ThreadLocal<TBase<?, ?>> threadSafeTBase = new ThreadLocal<TBase<?, ?>>();
 
 	/**
 	 * Factory
 	 */
 	public static class Factory implements TProtocolFactory {
 		public TProtocol getProtocol(TTransport trans) {
 			return new TBSONProtocol();
 		}
 	}
 
 	/**
 	 * Constructor
 	 */
 	public TBSONProtocol() {
 		super(null);
 	}
 
 	public DBObject getDBObject() {
 		return threadSafeDBObject.get();
 	}
 
 	public void setDBOject(DBObject dbObject) {
 		threadSafeDBObject.set(dbObject);
 	}
 
 	public void setBaseObject(TBase<?, ?> base) {
 		threadSafeTBase.set(base);
 	}
 
 	private static final TStruct ANONYMOUS_STRUCT = new TStruct();
 	private static final TField ANONYMOUS_FIELD = new TField();
 	private static final TMessage EMPTY_MESSAGE = new TMessage();
 	private static final TSet EMPTY_SET = new TSet();
 	private static final TList EMPTY_LIST = new TList();
 	private static final TMap EMPTY_MAP = new TMap();
 
 	protected class Context {
 		public DBObject dbObject = null;
 		public Object thriftObject;
 
 		void add(String value) {
 		}
 
 		void add(int value) {
 		}
 
 		void add(long value) {
 		}
 
 		void add(double value) {
 		}
 
 		void add(DBObject value) {
 		}
 
 		void add(ByteBuffer bin) {
 		}
 
 		public void addDBObject(DBObject dbObject) {
 			this.dbObject = dbObject;
 
 		}
 	}
 
 	protected class FieldContext extends Context {
 		public FieldContext(String name) {
 			this.name = name;
 		}
 
 		public String name;
 		public Object value;
 
 		void add(String value) {
 			this.value = value;
 		}
 
 		void add(int value) {
 			this.value = Integer.valueOf(value);
 		}
 
 		void add(long value) {
 			this.value = Long.valueOf(value);
 		}
 
 		void add(double value) {
 			this.value = Double.valueOf(value);
 		}
 
 		public void add(ByteBuffer bin) {
 			this.value = bin.array();
 		}
 	}
 
 	// Clasa for all object container likes list set and maps
 	protected class ObjectContainerContext extends Context {
 
 	}
 
 	protected class ListContext extends ObjectContainerContext {
 		BasicDBList dbList = new BasicDBList();
 		Integer index = 0;
 
 		void add(String value) {
 			dbList.put(index.toString(), value);
 			index++;
 		}
 
 		void add(DBObject value) {
 			dbList.put(index.toString(), value);
 			index++;
 		}
 
 		Object next() {
 			Object object = dbList.get(index);
 			index++;
 			return object;
 		}
 	}
 
 	protected class MapContext extends ObjectContainerContext {
 		private DBObject dbMap = new BasicDBObject();
 		public Stack<String> keyStack;
 		boolean extractKey = true;
 		
 		public void setDbMap( DBObject dbMap ) {
 			this.dbMap = dbMap;
 			this.keyStack = new Stack<String>();
 			this.keyStack.addAll(this.dbMap.keySet());			
 		}
 		
 		// A Map
 		private String stringKey = null;
 
 		void add(String value) {
 			if (stringKey != null) {
 				dbMap.put(stringKey, value);
 				stringKey = null;
 			} else {
 				stringKey = value;
 			}
 		}
 
 		void add(DBObject value) {
 			if (stringKey != null) {
 				dbMap.put(stringKey, value);
 				stringKey = null;
 			}
 		}
 		
 		Object next() {
 			if( extractKey ) {
 				extractKey = false;
 				return this.keyStack.firstElement();
 			} else {
 				extractKey = true;
 				String key = this.keyStack.remove(0);
 				return dbMap.get(key);
 			}
 		}
 	}
 
 	protected class StructContext extends Context {
 		private Stack<String> fieldsStack;
 		public Object thriftObject;
 
 		public void setDbObject( DBObject dbObject) {
 			this.dbObject = dbObject;
 			this.fieldsStack = new Stack<String>();
 			this.fieldsStack.addAll(this.dbObject.keySet());
 		}
 		
 		public StructContext() {
 			dbObject = new BasicDBObject();
 		}
 
 		void add(String value) {
 		}
 	}
 
 	/**
 	 * Push a new write context onto the stack.
 	 */
 	protected void pushContext(Context c) {
 		Stack<Context> stack = threadSafeContextStack.get();
 		if (stack == null) {
 			stack = new Stack<Context>();
 			stack.push(c);
 			threadSafeContextStack.set(stack);
 		} else {
 			threadSafeContextStack.get().push(c);
 		}
 	}
 
 	protected boolean isContextEmpty() {
 		Stack<Context> stack = threadSafeContextStack.get();
 		if (stack == null || stack.size() == 0) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Pop the last write context off the stack
 	 */
 	protected Context popContext() {
 		Context c = threadSafeContextStack.get().pop();
 		return c;
 	}
 
 	protected Context peekContext() {
 		return threadSafeContextStack.get().peek();
 	}
 
 	public void writeMessageBegin(TMessage message) throws TException {
 		// trans_.write(LBRACKET);
 		pushContext(new ListContext());
 		writeString(message.name);
 		writeByte(message.type);
 		writeI32(message.seqid);
 	}
 
 	public void writeMessageEnd() throws TException {
 		popContext();
 		// trans_.write(RBRACKET);
 	}
 
 	public void writeStructBegin(TStruct struct) throws TException {
 		StructContext c = new StructContext();
 		pushContext(c);
 	}
 
 	public void writeStructEnd() throws TException {
 		// Gets the struct
 		DBObject dbObject = popContext().dbObject;
 
 		// Sets the DBObject for output
 		threadSafeDBObject.set(dbObject);
 
 		// if the stack is not empty add the struct current stack field
 		if (isContextEmpty() == false) {
 			Context fieldContext = peekContext();
 
 			// For the ListContext adds the strcut to the context
 			if (fieldContext instanceof ObjectContainerContext) {
 				fieldContext.add(dbObject);
 			} else {
 				// Thrift general field adds the object to the field
 				fieldContext.addDBObject(dbObject);
 			}
 		}
 	}
 
 	public void writeFieldBegin(TField field) throws TException {
 		// Note that extra type information is omitted in BSON!
 		pushContext(new FieldContext(field.name));
 		writeString(field.name);
 	}
 
 	public void writeFieldEnd() throws TException {
 		Context c = popContext();
 		Context dbObjectContext = peekContext();
 		if (c.dbObject == null) {
 			dbObjectContext.dbObject.put(((FieldContext) c).name, ((FieldContext) c).value);
 		} else {
 			dbObjectContext.dbObject.put(((FieldContext) c).name, c.dbObject);
 		}
 	}
 
 	public void writeFieldStop() {
 	}
 
 	public void writeMapBegin(TMap map) throws TException {
 		MapContext c = new MapContext();
 		pushContext(c);
 	}
 
 	public void writeMapEnd() throws TException {
 		// Gets the map
 		MapContext map = (MapContext) popContext();
 		// Add the map to the current field
 		Context fieldContext = peekContext();
 		fieldContext.addDBObject(map.dbMap);
 	}
 
 	public void writeListBegin(TList list) throws TException {
 		pushContext(new ListContext());
 	}
 
 	public void writeListEnd() throws TException {
 		// Gets the list
 		ListContext list = (ListContext) popContext();
 		// Add the list to the current field
 		Context fieldContext = peekContext();
 		fieldContext.addDBObject(list.dbList);
 	}
 
 	/**
 	 * A Set have the same serialization of a thrift List
 	 */
 	public void writeSetBegin(TSet set) throws TException {
 		pushContext(new ListContext());
 	}
 
 	/**
 	 * A Set have the same serialization of a thrift List
 	 */
 	public void writeSetEnd() throws TException {
 		// Gets the list
 		ListContext list = (ListContext) popContext();
 		// Add the list to the current field
 		Context fieldContext = peekContext();
 		fieldContext.addDBObject(list.dbList);
 	}
 
 	public void writeBool(boolean b) throws TException {
 		writeByte(b ? (byte) 1 : (byte) 0);
 	}
 
 	public void writeByte(byte b) throws TException {
 		peekContext().add((int) b);
 	}
 
 	public void writeI16(short i16) throws TException {
 		peekContext().add((int) i16);
 	}
 
 	public void writeI32(int i32) throws TException {
 		peekContext().add(i32);
 	}
 
 	public void _writeStringData(String s) throws TException {
 		try {
 			byte[] b = s.getBytes("UTF-8");
 			peekContext().add(new String(s));
 		} catch (UnsupportedEncodingException uex) {
 			throw new TException("JVM DOES NOT SUPPORT UTF-8");
 		}
 	}
 
 	public void writeI64(long i64) throws TException {
 		peekContext().add(i64);
 	}
 
 	public void writeDouble(double dub) throws TException {
 		peekContext().add(dub);
 	}
 
 	public void writeString(String str) throws TException {
 		_writeStringData(str);
 	}
 
 	public void writeBinary(ByteBuffer bin) throws TException {
 		peekContext().add(bin);
 	}
 
 	/**
 	 * Reading methods.
 	 */
 
 	public TMessage readMessageBegin() throws TException {
 		return EMPTY_MESSAGE;
 	}
 
 	public void readMessageEnd() {
 	}
 
 	public TStruct readStructBegin() throws TException {
 		try {
 			DBObject dbObject = null;
 			Object thriftObject = null;
 			
 			StructContext context = new StructContext();
 			if (isContextEmpty() == false) {
 				Context currentContext = peekContext();
 				if (currentContext instanceof ListContext) {
 					dbObject = (DBObject) ((ListContext) currentContext).next();
 					thriftObject = ((ListContext) peekContext()).thriftObject;
 				} else if (currentContext instanceof MapContext) {
 					dbObject = (DBObject) ((MapContext) currentContext).next();
 					thriftObject = ((MapContext) peekContext()).thriftObject;
 				} else {
 					return ANONYMOUS_STRUCT;
 				}
 			} else {
 				thriftObject = this.threadSafeTBase.get();
 				dbObject = getDBObject();
				// reserved name for the thrift deserialisation
				dbObject.removeField("classname");
 			} 
 			context.setDbObject(dbObject);
 			context.thriftObject = thriftObject;
 			pushContext(context);
 
 			return ANONYMOUS_STRUCT;
 		} catch (Exception exp) {
 			TException texp = new TException("Unexpected readStructBegin", exp);
 			throw texp;
 		}
 	}
 
 	public void readStructEnd() {
 		popContext();
 	}
 
 	public TField readFieldBegin() throws TException {
 		StructContext context = (StructContext) peekContext();
 		if (context.fieldsStack.isEmpty() == false) {
 			String fieldName = context.fieldsStack.peek();
 
 			TField currentField = getTField(context.thriftObject, fieldName);
 
 			// If the field is a struct push a struct context in the stack
 			if (currentField.type == TType.STRUCT) {
 				StructContext structContext = new StructContext();
 				structContext.setDbObject((DBObject) context.dbObject.get(fieldName));
 				structContext.thriftObject = getThriftObject(context.thriftObject, fieldName);
 				pushContext(structContext);
 			}
 			return currentField;
 		}
 		// Empty TFiled returns a TType.STOP
 		return new TField();
 	}
 
 	private Object getThriftObject(Object thriftObject, String fieldName) throws TException {
 		try {
 			Field metafaField = thriftObject.getClass().getField("metaDataMap");
 			Map<?, org.apache.thrift.meta_data.FieldMetaData> fields = (Map<?, org.apache.thrift.meta_data.FieldMetaData>) metafaField.get(thriftObject);
 			// recurse on all sub structures
 			for (Entry<?, org.apache.thrift.meta_data.FieldMetaData> entry : fields.entrySet()) {
 				TFieldIdEnum field = (TFieldIdEnum) entry.getKey();
 				if (field.getFieldName().equals(fieldName)) {
 					switch (entry.getValue().valueMetaData.type) {
 					case TType.LIST:
 						ListMetaData listMetaData = (ListMetaData) entry.getValue().valueMetaData;
						if( listMetaData.elemMetaData.isStruct()) {
 							return ((StructMetaData) listMetaData.elemMetaData).structClass.newInstance();
 						}
 						return null;						
 					case TType.SET:						
 						SetMetaData setMetaData = (SetMetaData) entry.getValue().valueMetaData;
 						if( setMetaData.isStruct()) {
 							return ((StructMetaData) setMetaData.elemMetaData).structClass.newInstance();
 						}
 						return null;
 					case TType.MAP:
 						MapMetaData mapMetaData = (MapMetaData) entry.getValue().valueMetaData;
 						if( mapMetaData.valueMetaData.isStruct()) {
 							return ((StructMetaData) mapMetaData.valueMetaData).structClass.newInstance();
 						}
 						return null;
 					case TType.STRUCT:
 						return ((StructMetaData) entry.getValue().valueMetaData).structClass.newInstance();
 					}
 				}
 			}
 
 			throw new Exception("FieldName not finded name=" + fieldName);
 		} catch (Exception exp) {
 			TException texp = new TException("Unexpected getListThriftObject fieldName=" + fieldName, exp);
 			throw texp;
 		}
 	}
 
 	private TField getTField(Object thriftObject, String fieldName) throws TException {
 		try {
 			byte type = 0;
 			short id = 0;
 
 			Field metafaField = thriftObject.getClass().getField("metaDataMap");
 			Map<?, org.apache.thrift.meta_data.FieldMetaData> fields = (Map<?, org.apache.thrift.meta_data.FieldMetaData>) metafaField.get(thriftObject);
 			// recurse on all sub structures
 			for (Entry<?, org.apache.thrift.meta_data.FieldMetaData> entry : fields.entrySet()) {
 				TFieldIdEnum field = (TFieldIdEnum) entry.getKey();
 				if (field.getFieldName().equals(fieldName)) {
 					type = entry.getValue().valueMetaData.type;
 					id = field.getThriftFieldId();
 					break;
 				}
 			}
 			return new TField("", type, id);
 		} catch (Exception exp) {
 			TException texp = new TException("Unexpected getTField fieldName=" + fieldName, exp);
 			throw texp;
 		}
 	}
 
 	public void readFieldEnd() {
 		StructContext context = (StructContext) peekContext();
 		if (context.fieldsStack.isEmpty() == false) {
 			context.fieldsStack.pop();
 		}
 	}
 
 	public TMap readMapBegin() throws TException {
 		StructContext context = (StructContext) peekContext();
 		if (context.fieldsStack.isEmpty() == false) {
 			String fieldName = context.fieldsStack.peek();
 			
 			MapContext mapContext = new MapContext();
 			BasicDBObject dbMap = (BasicDBObject) context.dbObject.get(fieldName);
 			
 			mapContext.setDbMap(dbMap);
 			mapContext.thriftObject = getThriftObject(context.thriftObject, fieldName);
 			pushContext(mapContext);
 			return new TMap(TType.STRING, TType.STRING,dbMap.size());			
 		}
 		return EMPTY_MAP;
 	}
 
 	public void readMapEnd() {
 		popContext();		
 	}
 
 	public TList readListBegin() throws TException {
 		StructContext context = (StructContext) peekContext();
 		if (context.fieldsStack.isEmpty() == false) {
 			String fieldName = context.fieldsStack.peek();
 
 			ListContext listContext = new ListContext();
 			BasicDBList dbList = (BasicDBList) context.dbObject.get(fieldName);
 
 			listContext.dbList = dbList;
 			listContext.thriftObject = getThriftObject(context.thriftObject, fieldName);
 			pushContext(listContext);
 			return new TList(TType.LIST, dbList.size());
 		}
 		return EMPTY_LIST;
 	}
 
 	public void readListEnd() {
 		popContext();
 	}
 
 	public TSet readSetBegin() throws TException {
 		StructContext context = (StructContext) peekContext();
 		if (context.fieldsStack.isEmpty() == false) {
 			String fieldName = context.fieldsStack.peek();
 
 			ListContext listContext = new ListContext();
 			BasicDBList dbList = (BasicDBList) context.dbObject.get(fieldName);
 
 			listContext.dbList = dbList;
 			listContext.thriftObject = getThriftObject(context.thriftObject, fieldName);
 			pushContext(listContext);
 			return new TSet(TType.SET, dbList.size());
 		}		
 		return EMPTY_SET;
 	}
 
 	public void readSetEnd() {
 		popContext();
 	}
 
 	public boolean readBool() throws TException {
 		return (readByte() == 1);
 	}
 
 	public byte readByte() throws TException {
 		return ((Number) getCurrentFieldValue()).byteValue();
 	}
 
 	public short readI16() throws TException {
 		return ((Number) getCurrentFieldValue()).shortValue();
 	}
 
 	public int readI32() throws TException {
 		return ((Number) getCurrentFieldValue()).intValue();
 	}
 
 	public long readI64() throws TException {
 		return ((Number) getCurrentFieldValue()).longValue();
 	}
 
 	public double readDouble() throws TException {
 		return ((Number) getCurrentFieldValue()).doubleValue();
 	}
 
 	public String readString() throws TException {
 		return (String) getCurrentFieldValue();
 	}
 
 	private Object getCurrentFieldValue() {
 		Context context = peekContext();
 		if( context instanceof StructContext && ((StructContext)context).fieldsStack.isEmpty() == false ) { 
 			String fieldName = ((StructContext)context).fieldsStack.peek();
 			// Extracts the dbobject
 			return context.dbObject.get(fieldName);
 		} else if(context instanceof  ListContext) {
 			return ((ListContext)context).next();
 		} else if(context instanceof  MapContext) {
 			return ((MapContext)context).next();
 		}
 		return null;
 	}
 
 	public String readStringBody(int size) throws TException {
 		return "";
 	}
 
 	public ByteBuffer readBinary() throws TException {
 		return ByteBuffer.wrap(new byte[0]);
 	}
 
 	public void reset() {
 		threadSafeContextStack.remove();
 		threadSafeDBObject.remove();
 	}
 }
