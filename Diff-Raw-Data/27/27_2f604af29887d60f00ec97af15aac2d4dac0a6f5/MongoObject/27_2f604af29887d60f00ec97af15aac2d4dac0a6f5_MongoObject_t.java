 package org.tsaikd.java.mongodb;
 
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.DBRef;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
 import com.mongodb.MongoException;
 
 public class MongoObject {
 
 	static Log log = LogFactory.getLog(MongoObject.class);
 
 	private static DB getDB() {
 		return MappedClass.db;
 	}
 
 	private MappedClass getMappedClass() {
 		return MappedClass.getMappedClass(getClass());
 	}
 
 	protected DBCollection getCol() {
 		return getMappedClass().getCol();
 	}
 
 	public String getEntityName() {
 		return getMappedClass().getEntityName();
 	}
 
 	protected MongoObject newInstance() throws MongoException {
 		try {
 			return getClass().newInstance();
 		} catch (InstantiationException | IllegalAccessException e) {
 			throw new MongoException(e.getMessage(), e);
 		}
 	}
 
 	public static <T extends MongoObject> T findOne(Class<T> clazz, DBObject o, DBObject fields) throws MongoException {
 		MappedClass mc = MappedClass.getMappedClass(clazz);
 		DBObject dbobj = mc.getCol().findOne(o, fields);
 		if (dbobj == null) {
 			return null;
 		}
 		return fromObject(clazz, dbobj);
 	}
 
 	public static <T extends MongoObject> T findOne(Class<T> clazz, DBObject o, String... fields) throws MongoException {
 		BasicDBObject fieldobj = new BasicDBObject();
 		for (String field : fields) {
 			fieldobj.put(field, 1);
 		}
 		return findOne(clazz, o, fieldobj);
 	}
 
 	public static <T extends MongoObject> T findOne(Class<T> clazz, DBObject o) throws MongoException {
 		MappedClass mc = MappedClass.getMappedClass(clazz);
 		DBObject dbobj = mc.getCol().findOne(o);
 		if (dbobj == null) {
 			return null;
 		}
 		return fromObject(clazz, dbobj);
 	}
 
 	public static <T extends MongoObject> T fromObject(Class<T> clazz, Object obj) throws MongoException {
 		try {
 			T ret = clazz.newInstance();
 			ret.fromObject(obj);
 			return ret;
 		} catch (InstantiationException | IllegalAccessException e) {
 			throw new MongoException(e.getMessage(), e);
 		}
 	}
 
 	public MongoObject fromObject(Object obj) throws MongoException {
 		if (obj instanceof DBRef) {
 			return fromDBRef((DBRef) obj);
 		} else if (obj instanceof DBObject) {
 			return fromDBObject((DBObject) obj);
 		} else {
 			throw new MongoException("Unsupported type: " + obj.getClass().getName());
 		}
 	}
 
 	public MongoObject fromDBRef(DBRef dbref) {
 		MappedClass mc = getMappedClass();
 		for (MappedField field : mc.persistenceFields.values()) {
 			field.setEmpty(this);
 		}
 		mc.idField.set(this, dbref.getId());
 		return this;
 	}
 
 	public MongoObject fromDBObject(DBObject dbobj) throws MongoException {
 		MappedClass mc = getMappedClass();
 		for (MappedField field : mc.persistenceFields.values()) {
 			String key = field.getName();
 			if (!dbobj.containsField(key)) {
 				field.setEmpty(this);
 				continue;
 			}
 			field.set(this, dbobj.get(key));
 		}
 		if (mc.idField != null) {
			Object idValue = dbobj.get("_id");
			if (idValue == null) {
				idValue = dbobj.get("$id");
				if (idValue != null) {
					isRef = true;
				}
			}
			mc.idField.set(this, idValue);
 		}
 		return this;
 	}
 
 	public BasicDBObject toDBObject(boolean extendRef, boolean originIdField) {
 		BasicDBObject dbobj = new BasicDBObject();
 		MappedClass mc = getMappedClass();
 		if (mc.idField != null) {
 			Object id = mc.idField.getDBValue(this, false, originIdField);
 			if (id != null) {
 				dbobj.put("_id", id);
 				if (originIdField) {
 					dbobj.put(mc.idField.getName(), id);
 				}
 			}
 		}
 		for (MappedField field : mc.persistenceFields.values()) {
 			Object value = field.getDBValue(this, extendRef, originIdField);
 			if (value != null) {
 				dbobj.put(field.getName(), value);
 			}
 		}
 		return dbobj;
 	}
 
 	public BasicDBObject toDBObject() {
 		return toDBObject(true, false);
 	}
 
 	public Object getId() {
 		MappedClass mc = getMappedClass();
 		if (mc.idField != null) {
 			return mc.idField.get(this);
 		}
 		return null;
 	}
 
 	public Object getIdDBValue() {
 		MappedClass mc = getMappedClass();
 		if (mc.idField != null) {
 			return mc.idField.getDBValue(this, false, false);
 		}
 		return null;
 	}
 
 	public DBRef toDBRef() {
		return new DBRef(getDB(), getEntityName(), getIdDBValue());
 	}
 
 	public boolean isEmpty() {
 		return toDBObject(false, false).keySet().isEmpty();
 	}
 
 	@Override
 	public String toString() {
 		if (isRef && !isFetched) {
 			return toDBRef().toString();
 		} else {
 			return toDBObject().toString();
 		}
 	}
 
 	public boolean isFetched = false;
 
 	public boolean isRef = false;
 
 	public MongoObject fetch() {
 		if (!isRef) {
 			log.error("Cannot fetch non-reference object: " + this);
 			return this;
 		}
 		if (!isFetched) {
 			DBRef dbref = toDBRef();
 			DBObject dbobj = dbref.fetch();
 			fromDBObject(dbobj);
 			isFetched = true;
 		}
 		return this;
 	}
 
 	public MongoObject fetchAll() {
 		MappedClass mc = getMappedClass();
 		for (MappedField field : mc.persistenceFields.values()) {
 			if (field.isReference && field.isMongoObject) {
 				if (field.isList) {
 					@SuppressWarnings("unchecked")
 					List<MongoObject> list = (List<MongoObject>) field.get(this);
 					if (list == null || list.isEmpty()) {
 						continue;
 					}
 					for (MongoObject mobj : list) {
 						mobj.fetch();
 					}
 				} else {
 					MongoObject mobj = field.getMongoObject(this);
 					if (mobj == null) {
 						continue;
 					}
 					mobj.fetch();
 				}
 			}
 		}
 		return this;
 	}
 
 	public MongoObject clone() {
 		return MongoObject.fromObject(getClass(), toDBObject(false, false));
 	}
 
 	public MongoObject save() {
 		DBCollection col = getCol();
 		col.save(toDBObject(false, false));
 		return this;
 	}
 
 	public MongoObject insert() throws MongoException {
 		getCol().insert(toDBObject(false, false));
 		getDB().getLastError().throwOnError();
 		return this;
 	}
 
 	public MongoObject setField(String... fields) {
 		MappedClass mc = getMappedClass();
 		if (mc.idField == null) {
 			throw new MongoException("Cannot setField without Id annotation: " + fields);
 		}
 
 		BasicDBObject set = new BasicDBObject();
 		BasicDBObject unset = new BasicDBObject();
 		for (String field : fields) {
 			MappedField mf = mc.persistenceFields.get(field);
 			if (mf == null) {
 				throw new MongoException("Invalid field: " + field);
 			}
 			Object value = mf.getDBValue(this, false, false);
 			if (value == null) {
 				unset.put(field, 1);
 			} else {
 				set.put(field, value);
 			}
 		}
 
 		DBCollection col = getCol();
 		BasicDBObject update = new BasicDBObject();
 		if (!unset.isEmpty()) {
 			update.put("$unset", unset);
 		}
 		if (!set.isEmpty()) {
 			update.put("$set", set);
 		}
 		col.update(new BasicDBObject("_id", getIdDBValue()), update);
 		return this;
 	}
 
	public static CommandResult mapReduce(Class<? extends MongoObject> clazz, String map, String reduce, DBObject query) throws MongoException {
		MappedClass mc = MappedClass.getMappedClass(clazz);
		DBCollection col = mc.getCol();
		MapReduceCommand mrcmd = new MapReduceCommand(col, map, reduce, null, MapReduceCommand.OutputType.INLINE, query);
		MapReduceOutput mrout = col.mapReduce(mrcmd);
		CommandResult res = mrout.getCommandResult();
		return res;
	}

 }
