 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.gen;
 
 import static org.oobium.persist.migrate.defs.Column.BINARY;
 import static org.oobium.persist.migrate.defs.Column.BOOLEAN;
 import static org.oobium.persist.migrate.defs.Column.DATE;
 import static org.oobium.persist.migrate.defs.Column.DATESTAMPS;
 import static org.oobium.persist.migrate.defs.Column.DECIMAL;
 import static org.oobium.persist.migrate.defs.Column.DOUBLE;
 import static org.oobium.persist.migrate.defs.Column.FLOAT;
 import static org.oobium.persist.migrate.defs.Column.INTEGER;
 import static org.oobium.persist.migrate.defs.Column.LONG;
 import static org.oobium.persist.migrate.defs.Column.STRING;
 import static org.oobium.persist.migrate.defs.Column.TEXT;
 import static org.oobium.persist.migrate.defs.Column.TIME;
 import static org.oobium.persist.migrate.defs.Column.TIMESTAMP;
 import static org.oobium.persist.migrate.defs.Column.TIMESTAMPS;
 import static org.oobium.utils.StringUtils.columnName;
 import static org.oobium.utils.StringUtils.varName;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.sql.SQLException;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.oobium.build.gen.migration.JoinTable;
 import org.oobium.build.gen.migration.ModelTable;
 import org.oobium.build.model.ModelDefinition;
 import org.oobium.build.model.ModelRelation;
 import org.oobium.build.util.SourceFile;
 import org.oobium.persist.Binary;
 import org.oobium.persist.Text;
 import org.oobium.persist.migrate.AbstractMigration;
 import org.oobium.persist.migrate.Options;
 import org.oobium.persist.migrate.defs.Column;
 import org.oobium.persist.migrate.defs.Index;
 import org.oobium.persist.migrate.defs.Table;
 import org.oobium.persist.migrate.defs.columns.ForeignKey;
 import org.oobium.utils.literal;
 
 public class DbGenerator {
 
 	private static final Map<String, String> migrationTypes;
 	static {
 		migrationTypes = new HashMap<String, String>();
 		migrationTypes.put(Binary.class.getCanonicalName(),			BINARY);
 		migrationTypes.put(byte[].class.getCanonicalName(),			BINARY);
 		migrationTypes.put(String.class.getCanonicalName(),			STRING);
 		migrationTypes.put(Text.class.getCanonicalName(),			TEXT);
 		migrationTypes.put(Integer.class.getCanonicalName(),		INTEGER);
 		migrationTypes.put(int.class.getCanonicalName(),			INTEGER);
 		migrationTypes.put(Float.class.getCanonicalName(),			FLOAT);
 		migrationTypes.put(float.class.getCanonicalName(),			FLOAT);
 		migrationTypes.put(Long.class.getCanonicalName(), 			LONG);
 		migrationTypes.put(long.class.getCanonicalName(), 			LONG);
 		migrationTypes.put(Boolean.class.getCanonicalName(), 		BOOLEAN);
 		migrationTypes.put(boolean.class.getCanonicalName(), 		BOOLEAN);
 		migrationTypes.put(Double.class.getCanonicalName(), 		DOUBLE);
 		migrationTypes.put(double.class.getCanonicalName(), 		DOUBLE);
 		migrationTypes.put(Date.class.getCanonicalName(), 			TIMESTAMP);
 		migrationTypes.put(java.sql.Date.class.getCanonicalName(),	DATE);
 		migrationTypes.put(Time.class.getCanonicalName(),			TIME);
 		migrationTypes.put(Timestamp.class.getCanonicalName(),		TIMESTAMP);
 		migrationTypes.put(BigDecimal.class.getCanonicalName(),		DECIMAL);
 	}
 	
 	public static String generate(String moduleName, ModelDefinition[] models) {
 		return new DbGenerator(moduleName, models).generate().getSource();
 	}
 	
 	/**
 	 * convert a Java type into a method
 	 */
 	private static final String getMethod(String javaType) {
 		String type = migrationTypes.get(javaType);
 		if(type != null) {
 			return Character.toUpperCase(type.charAt(0)) + type.substring(1);
 		}
 		return "String";
 	}
 	
 	
 	private final String packageName;
 	private final String simpleName;
 	private ModelDefinition[] models;
 
 	private String source;
 	
 	public DbGenerator(String moduleName, ModelDefinition[] models) {
 		this.packageName = moduleName.replace(File.separatorChar, '.') + ".migrator.migrations";
 		this.simpleName = "CreateDatabase";
 		this.models = models;
 	}
 
 	public DbGenerator(String packageName, String simpleName, ModelDefinition[] models) {
 		this.packageName = packageName;
 		this.simpleName = simpleName;
 		this.models = models;
 	}
 	
 	private void appendOptions(SourceFile sf, StringBuilder sb, Options options) {
 		sf.staticImports.add(literal.class.getCanonicalName() + ".Map");
 		sb.append(", Map(");
 		if(options.size() == 1) {
 			String key = options.getKeys().iterator().next();
 			sb.append("\"").append(key).append("\", ").append(options.get(key)).append(')');
 		} else {
 			sf.staticImports.add(literal.class.getCanonicalName() + ".e");
 			for(Iterator<String> iter = options.getKeys().iterator(); iter.hasNext(); ) {
 				String key = iter.next();
 				sb.append("\n\t\t\t\te(\"").append(key).append("\", ").append(options.get(key)).append(')');
 				if(iter.hasNext()) sb.append(", ");
 			}
 			sb.append("\n\t\t\t)");
 		}
 	}
 
 	public DbGenerator generate() {
 		SourceFile sf = new SourceFile();
 
 		for(ModelDefinition model : models) {
 			model.setOpposites(models);
 		}
 
 		Map<String, ModelTable> tables = new TreeMap<String, ModelTable>();
 		Map<String, JoinTable> joins = new TreeMap<String, JoinTable>();
 		Set<ModelTable> joinedModels = new HashSet<ModelTable>();
 		
 		for(ModelDefinition model : models) {
 			tables.put(model.getSimpleName(), new ModelTable(sf, model, models));
 		}
 		
 		for(ModelDefinition model : models) {
 			for(ModelRelation relation : model.getRelations()) {
 				if(relation.hasMany && !relation.isThrough()) {
 					ModelRelation opposite = relation.getOpposite();
 					if(opposite == null || opposite.hasMany) {
 						ModelTable table1 = tables.get(model.getSimpleName());
 						ModelTable table2 = tables.get(relation.getSimpleType());
 						JoinTable joinTable = new JoinTable(table1.name, columnName(relation.name), table2.name, columnName(relation.opposite));
 						if(!joins.containsKey(joinTable.name)) {
 							joins.put(joinTable.name, joinTable);
 							joinedModels.add(table1);
 							joinedModels.add(table2);
 						}
 					}
 				}
 			}
 		}
 
 		sf.packageName = packageName;
 		sf.simpleName = simpleName;
 		sf.superName = AbstractMigration.class.getSimpleName();
 		sf.imports.add(AbstractMigration.class.getCanonicalName());
 		sf.imports.add(SQLException.class.getCanonicalName());
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("@Override\npublic void up() throws SQLException {");
 		
 		for(ModelTable table : tables.values()) {
 			sb.append('\n');
 			String var = varName(table.name);
 			if(table.hasForeignKey() || table.hasIndex() || joinedModels.contains(table)) {
 				sf.imports.add(Table.class.getCanonicalName());
 				sb.append("\tTable ").append(var).append(" = createTable(\"").append(table.name).append("\"");
 			} else {
 				sb.append("\tcreateTable(\"").append(table.name).append("\"");
 			}
 			if(table.columns.isEmpty()) {
 				sb.append(");\n");
 			} else {
 				sb.append(",\n");
 				for(Iterator<Column> iter = table.columns.iterator(); iter.hasNext(); ) {
 					Column column = iter.next();
 					if(DATESTAMPS.equals(column.name)) {
 						sb.append("\t\tDatestamps(");
 					} else if(TIMESTAMPS.equals(column.name)) {
						sb.append("\t\t\tTimestamps(");
 					} else {
 						sb.append("\t\t").append(getMethod(column.type)).append("(\"").append(column.name).append("\"");
 						if(column.options.hasAny()) {
 							appendOptions(sf, sb, column.options);
 						}
 					}
 					if(iter.hasNext()) {
 						sb.append("),\n");
 					} else {
 						sb.append(")\n");
 					}
 				}
 				sb.append("\t);\n");
 			}
 			if(table.hasIndex()) {
 				for(Index index : table.indexes) {
 					if(index.unique) {
 						sb.append("\t").append(var).append(".addUniqueIndex(");
 					} else {
 						sb.append("\t").append(var).append(".addIndex(");
 					}
 					for(int i = 0; i < index.columns.length; i++) {
 						if(i != 0) sb.append(", ");
 						sb.append('"').append(index.columns[i]).append('"');
 					}
 					sb.append(");\n");
 				}
 				if(!table.hasForeignKey()) {
 					sb.append("\t").append(var).append(".update();\n");
 				}
 			}
 		}
 
 		if(!joins.isEmpty()) {
 			sb.append('\n');
 			for(JoinTable join : joins.values()) {
 				sb.append("\tcreateJoinTable(");
 				sb.append(join.tableVar1).append(", \"").append(join.column1).append("\", ");
 				sb.append(join.tableVar2).append(", \"").append(join.column2).append("\");\n");
 			}
 		}
 		
 		for(ModelTable table : tables.values()) {
 			if(table.hasForeignKey()) {
 				String var = varName(table.name);
 				for(int i = 0; i < table.foreignKeys.size(); i++) {
 					ForeignKey fk = table.foreignKeys.get(i);
 					sb.append("\n\t").append(var).append(".addForeignKey(\"");
 					sb.append(fk.column).append("\", \"").append(fk.reference).append('"');
 					if(fk.options.hasAny()) {
 						appendOptions(sf, sb, fk.options);
 					}
 					sb.append(");");
 				}
 				sb.append("\n\t").append(var).append(".update();\n");
 			}
 		}
 
 		sb.append("}");
 		sf.methods.put("2", sb.toString());
 
 		
 		sb = new StringBuilder();
 		sb.append("@Override\n\tpublic void down() throws SQLException {\n");
 
 		boolean first = true;
 		for(ModelTable table : tables.values()) {
 			if(table.hasForeignKey()) {
 				first = false;
 				sb.append("\tchangeTable(\"").append(table.name).append("\",");
 				if(table.foreignKeys.size() == 1) {
 					sb.append(" removeForeignKey(\"").append(table.foreignKeys.get(0).column).append("\"));\n");
 				} else {
 					for(int i = 0; i < table.foreignKeys.size(); i++) {
 						ForeignKey fk = table.foreignKeys.get(i);
 						if(i != 0) sb.append(',');
 						sb.append("\n\t\tremoveForeignKey(\"").append(fk.column).append("\")");
 					}
 					sb.append("\n\t);\n");
 				}
 			}
 		}
 		
 		if(!joins.isEmpty()) {
 			if(first) {
 				first = false;
 			} else {
 				sb.append('\n');
 			}
 			for(JoinTable join : joins.values()) {
 				sb.append("\tdropJoinTable(\"");
 				sb.append(join.table1).append("\", \"").append(join.column1).append("\", \"");
 				sb.append(join.table2).append("\", \"").append(join.column2).append("\");\n");
 			}
 		}
 
 		if(!first) {
 			sb.append('\n');
 		}
 		for(ModelTable table : tables.values()) {
 			sb.append("\tdropTable(\"").append(table.name).append("\");\n");
 		}
 
 		sb.append("}");
 		sf.methods.put("3", sb.toString());
 		
 
 		source = sf.toSource();
 		
 		return this;
 	}
 	
 	public String getFullName() {
 		if(packageName != null) {
 			return packageName + "." + simpleName;
 		}
 		return simpleName;
 	}
 	
 	public String getPackageName() {
 		return packageName;
 	}
 	
 	public String getSimpleName() {
 		return simpleName;
 	}
 	
 	public String getSource() {
 		return source;
 	}
 	
 }
