 package org.eweb4j.orm.sql;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 
 import org.eweb4j.orm.config.ORMConfigBeanUtil;
 import org.eweb4j.util.ClassUtil;
 import org.eweb4j.util.ReflectUtil;
 
 /**
  * 生成插入语句
  * 
  * @author cfuture.aw
  * @since v1.a.0
  */
 @SuppressWarnings("all")
 public class InsertSqlCreator<T> {
 	private T[] ts;
 
 	public InsertSqlCreator(T... ts) {
 		T[] tmp = null;
 		if (ts != null && ts.length > 0) {
 			tmp = ts.clone();
 		}
 		this.ts = tmp;
 
 	}
 
 	public Sql[] create() throws SqlCreateException {
 		return this.create(null);
 	}
 
 	public Sql[] createByFields(String[] fields) throws SqlCreateException {
		return createByFieldsIsValues(new String[][] { fields }, null);
 	}
 
 	public Sql[] createByFieldsIsValues(String[][] fields, String[][] values)throws SqlCreateException {
 		Sql[] sqls = new Sql[ts.length];
 		for (int i = 0; i < ts.length; ++i) {
 			T t = ts[i];
 			ReflectUtil ru = new ReflectUtil(t);
 			Class<?> clazz = t.getClass();
 			String table = ORMConfigBeanUtil.getTable(clazz, false);
 			StringBuilder columnSB = new StringBuilder();
 			StringBuilder valueSB = new StringBuilder();
 			sqls[i] = new Sql();
 			for (int j = 0; j < fields[i].length; ++j) {
 				String name = fields[i][j];
 				Object _value = null;
 				Object value = null;
 				if (values == null) {
 					Method getter = null;
 					try {
 						getter = ru.getGetter(name);
 						if (getter == null)
 							continue;
 
 						_value = getter.invoke(t);
 						if (_value == null)
 							continue;
 
 						if (ClassUtil.isPojo(_value.getClass())) {
 							Field f = ru.getField(fields[i][j]);
 							OneToOne oneAnn = getter.getAnnotation(OneToOne.class);
 							if (oneAnn == null)
 								oneAnn = f.getAnnotation(OneToOne.class);
 							
 							ManyToOne manyToOneAnn = null;
 							if (oneAnn == null){
 								manyToOneAnn = getter.getAnnotation(ManyToOne.class);
 								if (manyToOneAnn == null)
 									manyToOneAnn = f.getAnnotation(ManyToOne.class);
 								
 							}
 							
 							if (oneAnn != null || manyToOneAnn != null){ 
 								JoinColumn joinColAnn = getter.getAnnotation(JoinColumn.class);
 								if (joinColAnn == null)
 									joinColAnn = f.getAnnotation(JoinColumn.class);
 								
 								if (joinColAnn != null && joinColAnn.referencedColumnName().trim().length() > 0){
 									String refCol = joinColAnn.referencedColumnName();
 									String refField = ORMConfigBeanUtil.getField(_value.getClass(), refCol);
 									ReflectUtil tarRu = new ReflectUtil(_value);
 									Method tarFKGetter = tarRu.getGetter(refField);
 									value = tarFKGetter.invoke(_value);
 								}else{
 									ReflectUtil tarRu = new ReflectUtil(_value);
 									String tarFKField = ORMConfigBeanUtil.getIdField(_value.getClass());
 									if (tarFKField != null){
 										Method tarFKGetter = tarRu.getGetter(tarFKField);
 										value = tarFKGetter.invoke(_value);
 									}
 								}
 							}
 							
 							if (value == null)
 								continue;
 						} else {
 							value = _value;
 						}
 						
 					} catch (Exception e) {
 						throw new SqlCreateException(getter + " invoke exception " + e.toString(), e);
 					}
 				} else {
 					value = values[i][j];
 				}
 
 				String column = ORMConfigBeanUtil.getColumn(clazz, name);
 				if (valueSB.length() > 0) {
 					columnSB.append(",");
 					valueSB.append(",");
 				}
 
 				columnSB.append(column);
 //				valueSB.append("'").append(value).append("'");
 				valueSB.append("?");
 				sqls[i].args.add(value);
 			}
 
 			sqls[i].sql = String.format("INSERT INTO %s(%s) VALUES(%s) ;", table, columnSB.toString(), valueSB.toString());
 		}
 
 		return sqls;
 	}
 	
 	public static void main(String[] args){
 		System.out.println(Boolean.class.isAssignableFrom(boolean.class));
 	}
 
 	public static Sql createByColumnsIsValues(String table,String[] columns, String[] values) {
 		Sql sql = new Sql();
 		StringBuilder columnSB = new StringBuilder();
 		StringBuilder valueSB = new StringBuilder();
 		for (int i = 0; i < columns.length; i++) {
 			String column = columns[i];
 			String value = values[i];
 
 			if (valueSB.length() > 0) {
 				columnSB.append(",");
 				valueSB.append(",");
 			}
 
 			columnSB.append(column);
 //			valueSB.append("'").append(value).append("'");
 			valueSB.append("?");
 			sql.args.add(value);
 		}
 
 		sql.sql = String.format("INSERT INTO %s(%s) VALUES(%s) ;", table, columnSB.toString(), valueSB.toString());
 
 		return sql;
 	}
 
 	public Sql[] create(String condition) throws SqlCreateException {
 		Sql[] sqls = new Sql[ts.length];
 		for (int index = 0; index < ts.length; ++index) {
 			String table = null;
 			StringBuilder columnSB = new StringBuilder();
 			StringBuilder valueSB = new StringBuilder();
 			T t = ts[index];
 			Class<?> clazz = t.getClass();
 			String[] fields;
 			String[] columns;
 			String idColumn;
 			Object[] values = null;
 			sqls[index] = new Sql();
 			HashMap<String, Object> map = null;
 			if (Map.class.isAssignableFrom(clazz)) {
 				map = (HashMap<String, Object>) t;
 				table = String.valueOf(map.get("table"));
 				idColumn = String.valueOf(map.get("idColumn"));
 				if (idColumn == null)
 					idColumn = "id";
 
 				fields = (String[]) map.get("columns");
 				columns = fields;
 				values = (Object[]) map.get("values");
 			} else {
 				table = ORMConfigBeanUtil.getTable(clazz, false);
 				idColumn = ORMConfigBeanUtil.getIdColumn(t.getClass());
 				fields = ORMConfigBeanUtil.getFields(clazz);
 				columns = ORMConfigBeanUtil.getColumns(clazz);
 			}
 
 			ReflectUtil ru = new ReflectUtil(t);
 			for (int i = 0; i < columns.length; i++) {
 				String column = columns[i];
 
 				Object value = null;
 				if (map != null && values != null) {
 					value = values[i];
 				} else {
 
 					String name = fields[i];
 					Method getter = ru.getGetter(name);
 					if (getter == null)
 						continue;
 
 					Object _value = null;
 
 					try {
 						_value = getter.invoke(t);
 						if (_value == null)
 							continue;
 
 						if (ClassUtil.isPojo(_value.getClass())) {
 							Field f = ru.getField(name);
 							OneToOne oneAnn = getter.getAnnotation(OneToOne.class);
 							if (oneAnn == null)
 								oneAnn = f.getAnnotation(OneToOne.class);
 							
 							ManyToOne manyToOneAnn = null;
 							if (oneAnn == null){
 								manyToOneAnn = getter.getAnnotation(ManyToOne.class);
 								if (manyToOneAnn == null)
 									manyToOneAnn = f.getAnnotation(ManyToOne.class);
 								
 							}
 							
 							if (oneAnn != null || manyToOneAnn != null){ 
 								JoinColumn joinColAnn = getter.getAnnotation(JoinColumn.class);
 								if (joinColAnn == null)
 									joinColAnn = f.getAnnotation(JoinColumn.class);
 								
 								if (joinColAnn != null && joinColAnn.referencedColumnName().trim().length() > 0){
 									String refCol = joinColAnn.referencedColumnName();
 									String refField = ORMConfigBeanUtil.getField(_value.getClass(), refCol);
 									ReflectUtil tarRu = new ReflectUtil(_value);
 									Method tarFKGetter = tarRu.getGetter(refField);
 									value = tarFKGetter.invoke(_value);
 								}else{
 									ReflectUtil tarRu = new ReflectUtil(_value);
 									String tarFKField = ORMConfigBeanUtil.getIdField(_value.getClass());
 									if (tarFKField != null){
 										Method tarFKGetter = tarRu.getGetter(tarFKField);
 										value = tarFKGetter.invoke(_value);
 									}
 								}
 							}
 							
 							if (value == null)
 								continue;
 						}else {
 							value = _value;
 						}
 
 					} catch (Exception e) {
 						throw new SqlCreateException(getter + " invoke exception " + e.toString(), e);
 					}
 				}
 				// id 字段不允许插入表中
 				if (idColumn != null && idColumn.equalsIgnoreCase(column))
 					continue;
 
 				if (columnSB.length() > 0)
 					columnSB.append(",");
 
 				columnSB.append(column);
 
 				if (valueSB.length() > 0)
 					valueSB.append(",");
 
 //				valueSB.append("'").append(value).append("'");
 				valueSB.append("?");
 				sqls[index].args.add(value);
 			}
 
 			String format = "INSERT INTO ${table}(${columns}) VALUES(${values}) ${condition} ;";
 			format = format.replace("${table}", table);
 			format = format.replace("${columns}", columnSB.toString());
 			format = format.replace("${values}", valueSB.toString());
 
 			if (condition != null)
 				format = format.replace("${condition}", " WHERE " + condition);
 			else
 				format = format.replace("${condition} ", "");
 
 			sqls[index].sql = format;
 		}
 
 		return sqls;
 	}
 
 	public T[] getTs() {
 		T[] tmp = null;
 		if (ts != null && ts.length > 0) {
 			tmp = ts.clone();
 		}
 		return tmp;
 	}
 
 	public void setTs(T[] ts) {
 		T[] tmp = null;
 		if (ts != null && ts.length > 0) {
 			tmp = ts.clone();
 		}
 		this.ts = tmp;
 	}
 }
