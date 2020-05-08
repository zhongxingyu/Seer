 package org.eweb4j.orm.sql;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 
 import org.eweb4j.cache.ORMConfigBeanCache;
 import org.eweb4j.config.ConfigConstant;
 import org.eweb4j.config.LogFactory;
 import org.eweb4j.orm.PropType;
 import org.eweb4j.orm.config.ORMConfigBeanUtil;
 import org.eweb4j.orm.config.bean.ORMConfigBean;
 import org.eweb4j.orm.config.bean.Property;
 import org.eweb4j.util.ClassUtil;
 import org.eweb4j.util.CommonUtil;
 import org.eweb4j.util.ReflectUtil;
 
 /**
  * 
  * @author weiwei
  *
  */
 public class Model2Table {
 
 	final static String drop_table_script = "\nDROP TABLE IF EXISTS %s ;";
 	final static String create_table_script = "\nCREATE TABLE IF NOT EXISTS %s(\n%s \n) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;\n"; 
 	
 	public static String generate() {
 		return generateAll(false, true);
 	}
 	
 	public static String generateOne(final Class<?> entityClass, final boolean isDrop, boolean isCreateFile) {
 		StringBuilder sql = new StringBuilder();
 		StringBuilder manyMany = new StringBuilder();
		
 		final ORMConfigBean ocb = ORMConfigBeanCache.get(entityClass.getName());
 		final String table = ocb.getTable();
 		
 		List<Property> properties = ocb.getProperty();
 		StringBuilder sb = new StringBuilder();
 		StringBuilder fkSb = new StringBuilder();
 		final String fk = ", \n\tKEY %s (%s), \n\tCONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)";
 		StringBuilder pkSb = new StringBuilder();
 		final String pk = ", \n\tPRIMARY KEY (%s)";
 		StringBuilder uniqueSb = new StringBuilder();
 		String unique = ", \n\tUNIQUE KEY %s (%s)";
 		final String idCol = ORMConfigBeanUtil.getIdColumn(properties);
 		
 		for (Property p : properties) {
 			if (sb.length() > 0)
 				sb.append(", \n");
 			
 			String col = p.getColumn();
 			
 			String type = p.getType();
 			String size = null;
 			
 			if (p.getSize().trim().length() > 0 && CommonUtil.isNumeric(p.getSize().trim()))
 				size = String.format(" (%s) ", p.getSize());
 			
 			if ("1".equals(p.getUnique()) || "true".equalsIgnoreCase(p.getUnique()))
 				uniqueSb.append(String.format(unique, col, col));
 			
 			String notNull = "";
 			if ("1".equals(p.getNotNull()) || "true".equalsIgnoreCase(p.getNotNull()))
 				notNull = " NOT NULL ";
 			
 			if ("1".equals(p.getPk()) || "true".equalsIgnoreCase(p.getPk())){
 				if (pkSb.length() > 0)
 					pkSb.append(",");
 				
 				pkSb.append(col);
 			}
 			
 			String auto  = "";
 			if ("1".equals(p.getAutoIncrement()) || "true".equalsIgnoreCase(p.getAutoIncrement()))
 				auto =  " AUTO_INCREMENT ";
 			
 			if (PropType.ONE_ONE.equals(p.getType()) || PropType.MANY_ONE.equals(p.getType())){
 				final String relTable = ORMConfigBeanUtil.getTable(p.getRelClass(), false);
 				if (p.getRelProperty() == null || p.getRelProperty().trim().length() == 0)
 					p.setRelProperty(ORMConfigBeanUtil.getIdField(p.getRelClass()));
 				
 				final String relColumn = ORMConfigBeanUtil.getColumn(p.getRelClass(), p.getRelProperty());
 				fkSb.append(String.format(fk, col, col, table+"_"+col, col, relTable, relColumn));
 			}
 			
 			sb.append("\t").append(col).append(" ").append(getType(type)).append(size == null ? "" : size).append(notNull).append(auto);
 		}
 		sb.append(uniqueSb.toString());
 		if (pkSb.length() > 0)
 			sb.append(String.format(pk, pkSb.toString()));
 		
 		sb.append(fkSb.toString());
 		String _sql = String.format(create_table_script, table, sb.toString());
 		if (isDrop){
 			_sql = String.format(drop_table_script, table) + _sql;;
 		}
 		sql.append(_sql);
 		try {
 			ReflectUtil ru = new ReflectUtil(entityClass);
 			for (Field f : ru.getFields()){
 				if (!ClassUtil.isListClass(f))
 					continue;
 				
 				String name = f.getName();
 				Method getter = ru.getGetter(name);
 				if (getter == null)
 					continue;
 				
 				ManyToMany mmAnn = getter.getAnnotation(ManyToMany.class);
 				
 				if (mmAnn == null)
 					mmAnn = f.getAnnotation(ManyToMany.class);
 				
 				if (mmAnn != null){
 					JoinTable join = getter.getAnnotation(JoinTable.class);
 					if (join == null) {
 						join = f.getAnnotation(JoinTable.class);
 						if (join == null)
 							continue;
 					}
 		
 					JoinColumn[] froms = join.joinColumns();
 					if (froms == null || froms.length == 0)
 						continue;
 		
 					JoinColumn[] tos = join.inverseJoinColumns();
 					if (tos == null || tos.length == 0)
 						continue;
 					
 					final String relTable = join.name();
 					final String relFrom = froms[0].name();
 					final String relTo = tos[0].name();
 					final Class<?> targetClass = ClassUtil.getGenericType(f);
 					
 					StringBuilder manyManyField = new StringBuilder();
 					//handle the many to many 
 					manyManyField.append("\tid ").append(getType("long")).append(" (20) NOT NULL AUTO_INCREMENT,");
 					manyManyField.append("\n\t").append(relFrom).append(" ").append(getType("long")).append(" (20) ,");
 					manyManyField.append("\n\t").append(relTo).append(" ").append(getType("long")).append(" (20) ,");
 					manyManyField.append("\n\t").append("PRIMARY KEY (id)");
 					String tarTable = ORMConfigBeanUtil.getTable(targetClass, false);
 					String tarIdCol = ORMConfigBeanUtil.getIdColumn(targetClass);
 					
 					String fk1 = String.format(fk, relFrom, relFrom, relTable + "_" + relFrom, relFrom, table, idCol);
 					manyManyField.append(fk1);
 					
 					String fk2 = String.format(fk, relTo, relTo, relTable + "_" + relTo, relTo, tarTable, tarIdCol);
 					manyManyField.append(fk2);
 					
 					manyMany.append(String.format(create_table_script, relTable, relTable, manyManyField.toString()));
 				}
 			}
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		} 
 		
 		sql.append(manyMany.toString());
 		String script = String.format("SET FOREIGN_KEY_CHECKS=0;\n%s", sql.toString());
 		
 		if (!isCreateFile)
 			return script;
 		
 		try {
			File file = new File(ConfigConstant.CONFIG_BASE_PATH() + "db-create.sql");
 			FileWriter writer = new FileWriter(file);
 			writer.write(script);
 			writer.flush();
 			writer.close();
 			LogFactory.getORMLogger(Model2Table.class).debug("create models sql script file success -> " + file.getAbsoluteFile());
 			return script;
 		} catch (IOException e2) {
 			e2.printStackTrace();
 			return null;
 		} 
 	}
 	
 	public static String generateAll(final boolean isDrop, final boolean isCreateFile) {
 		StringBuilder sql = new StringBuilder();
 		StringBuilder manyMany = new StringBuilder();
 		for (Iterator<Entry<Object, ORMConfigBean>> it = ORMConfigBeanCache.entrySet().iterator(); it.hasNext();) {
 			final Entry<Object, ORMConfigBean> e = it.next();
 			final ORMConfigBean ocb = e.getValue();
 			final String table = ocb.getTable();
 			
 			List<Property> properties = ocb.getProperty();
 			StringBuilder sb = new StringBuilder();
 			StringBuilder fkSb = new StringBuilder();
 			final String fk = ", \n\tKEY %s (%s), \n\tCONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)";
 			StringBuilder pkSb = new StringBuilder();
 			final String pk = ", \n\tPRIMARY KEY (%s)";
 			StringBuilder uniqueSb = new StringBuilder();
 			String unique = ", \n\tUNIQUE KEY %s (%s)";
 			final String idCol = ORMConfigBeanUtil.getIdColumn(properties);
 			
 			for (Property p : properties) {
 				if (sb.length() > 0)
 					sb.append(", \n");
 				
 				String col = p.getColumn();
 				
 				String type = p.getType();
 				String size = null;
 				
 				if (p.getSize().trim().length() > 0 && CommonUtil.isNumeric(p.getSize().trim()))
 					size = String.format(" (%s) ", p.getSize());
 				
 				if ("1".equals(p.getUnique()) || "true".equalsIgnoreCase(p.getUnique()))
 					uniqueSb.append(String.format(unique, col, col));
 				
 				String notNull = "";
 				if ("1".equals(p.getNotNull()) || "true".equalsIgnoreCase(p.getNotNull()))
 					notNull = " NOT NULL ";
 				
 				if ("1".equals(p.getPk()) || "true".equalsIgnoreCase(p.getPk())){
 					if (pkSb.length() > 0)
 						pkSb.append(",");
 					
 					pkSb.append(col);
 				}
 				
 				String auto  = "";
 				if ("1".equals(p.getAutoIncrement()) || "true".equalsIgnoreCase(p.getAutoIncrement()))
 					auto =  " AUTO_INCREMENT ";
 				
 				if (PropType.ONE_ONE.equals(p.getType()) || PropType.MANY_ONE.equals(p.getType())){
 					final String relTable = ORMConfigBeanUtil.getTable(p.getRelClass(), false);
 					if (p.getRelProperty() == null || p.getRelProperty().trim().length() == 0)
 						p.setRelProperty(ORMConfigBeanUtil.getIdField(p.getRelClass()));
 					
 					final String relColumn = ORMConfigBeanUtil.getColumn(p.getRelClass(), p.getRelProperty());
 					fkSb.append(String.format(fk, col, col, table+"_"+col, col, relTable, relColumn));
 				}
 				
 				sb.append("\t").append(col).append(" ").append(getType(type)).append(size == null ? "" : size).append(notNull).append(auto);
 			}
 			sb.append(uniqueSb.toString());
 			if (pkSb.length() > 0)
 				sb.append(String.format(pk, pkSb.toString()));
 			
 			sb.append(fkSb.toString());
 			String _sql = String.format(create_table_script, table, sb.toString());
 			if (isDrop){
 				_sql = String.format(drop_table_script, table) + _sql;;
 			}
 			sql.append(_sql);
 			try {
 				Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass((String)e.getKey());
 				ReflectUtil ru = new ReflectUtil(clazz);
 				for (Field f : ru.getFields()){
 					if (!ClassUtil.isListClass(f))
 						continue;
 					
 					String name = f.getName();
 					Method getter = ru.getGetter(name);
 					if (getter == null)
 						continue;
 					
 					ManyToMany mmAnn = getter.getAnnotation(ManyToMany.class);
 					
 					if (mmAnn == null)
 						mmAnn = f.getAnnotation(ManyToMany.class);
 					
 					if (mmAnn != null){
 						JoinTable join = getter.getAnnotation(JoinTable.class);
 						if (join == null) {
 							join = f.getAnnotation(JoinTable.class);
 							if (join == null)
 								continue;
 						}
 			
 						JoinColumn[] froms = join.joinColumns();
 						if (froms == null || froms.length == 0)
 							continue;
 			
 						JoinColumn[] tos = join.inverseJoinColumns();
 						if (tos == null || tos.length == 0)
 							continue;
 						
 						final String relTable = join.name();
 						final String relFrom = froms[0].name();
 						final String relTo = tos[0].name();
 						final Class<?> targetClass = ClassUtil.getGenericType(f);
 						
 						StringBuilder manyManyField = new StringBuilder();
 						//handle the many to many 
 						manyManyField.append("\tid ").append(getType("long")).append(" (20) NOT NULL AUTO_INCREMENT,");
 						manyManyField.append("\n\t").append(relFrom).append(" ").append(getType("long")).append(" (20) ,");
 						manyManyField.append("\n\t").append(relTo).append(" ").append(getType("long")).append(" (20) ,");
 						manyManyField.append("\n\t").append("PRIMARY KEY (id)");
 						String tarTable = ORMConfigBeanUtil.getTable(targetClass, false);
 						String tarIdCol = ORMConfigBeanUtil.getIdColumn(targetClass);
 						
 						String fk1 = String.format(fk, relFrom, relFrom, relTable + "_" + relFrom, relFrom, table, idCol);
 						manyManyField.append(fk1);
 						
 						String fk2 = String.format(fk, relTo, relTo, relTable + "_" + relTo, relTo, tarTable, tarIdCol);
 						manyManyField.append(fk2);
 						
 						manyMany.append(String.format(create_table_script, relTable, relTable, manyManyField.toString()));
 					}
 				}
 			} catch (Exception e1) {
 				continue;
 			} 
 		}
 		
 		sql.append(manyMany.toString());
 		String script = String.format("SET FOREIGN_KEY_CHECKS=0;\n%s", sql.toString());
 		
 		if (!isCreateFile)
 			return script;
 		
 		try {
			File file = new File(ConfigConstant.CONFIG_BASE_PATH() + "db-create.sql");
 			FileWriter writer = new FileWriter(file);
 			writer.write(script);
 			writer.flush();
 			writer.close();
 			LogFactory.getORMLogger(Model2Table.class).debug("create models sql script file success -> " + file.getAbsoluteFile());
 			return script;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		} 
 	}
 
 	private static String getType(String type) {
 		if ("int".equalsIgnoreCase(type) || "java.lang.Integer".equalsIgnoreCase(type)) 
 			return "int";
 		
 		if ("long".equalsIgnoreCase(type) || "java.lang.Long".equalsIgnoreCase(type)) 
 			return "bigint";
 		
 		if ("float".equalsIgnoreCase(type) || "java.lang.Float".equalsIgnoreCase(type)) 
 			return "float";
 		
 		if ("double".equalsIgnoreCase(type) || "java.lang.Double".equalsIgnoreCase(type)) 
 			return "double";
 		
 		if ("string".equalsIgnoreCase(type) || "java.lang.String".equalsIgnoreCase(type)) 
 			return "varchar";
 		
 		if ("boolean".equalsIgnoreCase(type) || "java.lang.Boolean".equalsIgnoreCase(type))
 			return "boolean";
 		
 		if ("date".equalsIgnoreCase(type) || "java.sql.Date".equalsIgnoreCase(type) || "java.util.Date".equalsIgnoreCase(type)) 
 			return "datetime";
 		
 		if ("timestamp".equalsIgnoreCase(type) || "java.sql.Timestamp".equalsIgnoreCase(type)) 
 			return "timestamp";
 		
 		if ("[B".equalsIgnoreCase(type) || "byte[]".equalsIgnoreCase(type) || "[Ljava.lang.Byte;".equalsIgnoreCase(type)) 
 			return "blob";
 		
 		if (PropType.MANY_ONE.equals(type))
 			return "bigint";
 		
 		if (PropType.ONE_ONE.equals(type))
 			return "bigint";
 
 		return type;
 	}
 
 }
