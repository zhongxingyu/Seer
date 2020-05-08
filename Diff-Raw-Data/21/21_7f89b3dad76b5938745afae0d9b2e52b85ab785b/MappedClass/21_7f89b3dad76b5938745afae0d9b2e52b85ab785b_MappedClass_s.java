 package org.tsaikd.java.mongodb;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.tsaikd.java.mongodb.annotations.Entity;
 import org.tsaikd.java.mongodb.annotations.IndexEntity;
 import org.tsaikd.java.mongodb.annotations.IndexField;
 import org.tsaikd.java.mongodb.annotations.MongoField;
 import org.tsaikd.java.mongodb.annotations.Transient;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.MongoException;
 
 public class MappedClass {
 
 	static Log log = LogFactory.getLog(MappedClass.class);
 
 	public static DB db = null;
 
 	public static ArrayList<String> searchPackage = new ArrayList<>();
 
 	protected static HashMap<Class<?>, MappedClass> mappedClasses = new HashMap<>();
 
 	protected static HashMap<String, MappedClass> mappedClasses2 = new HashMap<>();
 
 	public static MappedClass getMappedClass(Class<?> clazz, DB dbCustom) {
 		if (mappedClasses.containsKey(clazz)) {
 			return mappedClasses.get(clazz);
 		}
 		MappedClass mappedClass = new MappedClass(clazz);
 		mappedClasses.put(clazz, mappedClass);
 		mappedClasses2.put(mappedClass.getEntityName(), mappedClass);
 		mappedClass.setDB(dbCustom);
 		mappedClass.ensureIndex();
 		return mappedClass;
 	}
 
 	public static MappedClass getMappedClass(Class<?> clazz) {
 		if (mappedClasses.containsKey(clazz)) {
 			return mappedClasses.get(clazz);
 		}
 		MappedClass mappedClass = new MappedClass(clazz);
 		mappedClasses.put(clazz, mappedClass);
 		mappedClasses2.put(mappedClass.getEntityName(), mappedClass);
 		mappedClass.ensureIndex();
 		return mappedClass;
 	}
 
 	public static MappedClass getMappedClass(String name) throws MongoException {
 		if (!mappedClasses2.containsKey(name)) {
 			Class<?> clazz = searchClass(name);
 			if (clazz == null) {
 				return null;
 			} else {
 				return getMappedClass(clazz);
 			}
 		}
 		return mappedClasses2.get(name);
 	}
 
 	public static boolean isMappedClass(Class<?> clazz) {
 		return mappedClasses.containsKey(clazz);
 	}
 
 	protected static Class<?> searchClass(String name) {
 		try {
 			return Class.forName(name);
 		} catch (ClassNotFoundException e) {
 		}
 		for (String basePkg : searchPackage) {
 			try {
 				return Class.forName(basePkg + "." + name);
 			} catch (ClassNotFoundException e) {
 			}
 		}
 		return null;
 	}
 
 	protected static ArrayList<MappedField> getDeclaredAndInheritedFields(Class<?> clazz, Object defClass) {
 		ArrayList<MappedField> fields = new ArrayList<MappedField>();
 		while ((clazz != null) && (clazz != Object.class) && (clazz != MongoObject.class)) {
 			for (Field field : clazz.getDeclaredFields()) {
 				if (!field.isAnnotationPresent(MongoField.class)) {
 					if (Modifier.isStatic(field.getModifiers())) {
 						continue;
 					}
 					if (field.isAnnotationPresent(Transient.class)) {
 						continue;
 					}
 					if (Modifier.isPrivate(field.getModifiers())) {
 						continue;
 					}
 					if (Modifier.isProtected(field.getModifiers())) {
 						continue;
 					}
 					if (field.getName().startsWith("this$")) {
 						continue;
 					}
 				}
 				try {
					fields.add(new MappedField(field, field.get(defClass)));
				} catch (IllegalAccessException e) {
					throw new MongoException(e.getMessage());
 				}
 			}
 			clazz = clazz.getSuperclass();
 		}
 		return fields;
 	}
 
 	public LinkedHashMap<String, MappedField> persistenceFields = new LinkedHashMap<>();
 
 	public MappedField idField;
 
 	// field, option
 	public LinkedHashMap<BasicDBObject, BasicDBObject> indexFields = new LinkedHashMap<>();
 
 	protected DB dbCustom = null;
 
 	protected Class<?> clazz;
 
 	MappedClass(Class<?> clazz) throws MongoException {
 		this.clazz = clazz;
 		IndexEntity annoIndex = getAnnotation(IndexEntity.class);
 		if (annoIndex != null) {
 			for (IndexField indexField : annoIndex.fields()) {
 				BasicDBObject fieldobj = new BasicDBObject(indexField.name(), indexField.direction());
 				BasicDBObject optionobj = new BasicDBObject();
 				if (indexField.option().unique()) {
 					optionobj.put("unique", true);
 				}
 				if (indexField.option().sparse()) {
 					optionobj.put("sparse", true);
 				}
 				if (indexField.option().dropDups()) {
 					optionobj.put("dropDups", true);
 				}
 				indexFields.put(fieldobj, optionobj);
 			}
 		}
		Object defClass = newInstance();
 		ArrayList<MappedField> fields = getDeclaredAndInheritedFields(clazz, defClass);
 		for (MappedField field : fields) {
 			if (field.isId) {
 				if (idField != null) {
 					throw new MongoException("Id annotation declared for " + clazz + ", id:" + idField);
 				}
 				idField = field;
 				continue;
 			}
 			persistenceFields.put(field.getName(), field);
 		}
 	}
 
 	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
 		Class<?> clazz = this.clazz;
 		while ((clazz != null) && (clazz != Object.class)) {
 			T anno = clazz.getAnnotation(annotationClass);
 			if (anno != null) {
 				return anno;
 			}
 			clazz = clazz.getSuperclass();
 		}
 		return null;
 	}
 
 	protected String cacheEntityName;
 	public String getEntityName() {
 		if (cacheEntityName == null) {
 			Entity anno = clazz.getAnnotation(Entity.class);
 			if (anno != null) {
 				cacheEntityName = anno.name();
 			}
 			if (cacheEntityName == null || cacheEntityName.isEmpty()) {
 				cacheEntityName = clazz.getSimpleName();
 			}
 		}
 		return cacheEntityName;
 	}
 
 	public DB getDB() {
 		if (dbCustom != null) {
 			return dbCustom;
 		}
 		return db;
 	}
 
 	public MappedClass setDB(DB dbTar) {
 		dbCustom = dbTar;
 		return this;
 	}
 
 	public DBCollection getCol() {
 		return getDB().getCollection(getEntityName());
 	}
 
 	public MappedClass ensureIndex() {
 		DBCollection col;
 		try {
 			col = getCol();
 		} catch (NullPointerException e) {
 			return this;
 		}
 		for (Entry<BasicDBObject, BasicDBObject> field : indexFields.entrySet()) {
 			if (log.isDebugEnabled()) {
 				if (field.getValue().isEmpty()) {
 					log.debug("ensureIndex " + getEntityName() + ": " + field.getKey());
 				} else {
 					log.debug("ensureIndex " + getEntityName() + ": " + field.getKey() + ", " + field.getValue());
 				}
 			}
 			col.ensureIndex(field.getKey(), field.getValue());
 		}
 		return this;
 	}
 
 	public Object newInstance() throws MongoException {
 		try {
 			return clazz.newInstance();
 		} catch (InstantiationException | IllegalAccessException e) {
 			throw new MongoException(e.getMessage(), e);
 		}
 	}
 
 	public static Object newInstance(String name) throws MongoException {
 		return getMappedClass(name).newInstance();
 	}
 
 }
