 package org.xillium.data.persistence.crud;
 
 import java.util.*;
 import java.sql.*;
 import java.lang.reflect.*;
 import javassist.*;
 import javassist.bytecode.*;
 import javassist.bytecode.annotation.MemberValue;
 import javassist.bytecode.annotation.IntegerMemberValue;
 import org.xillium.base.beans.Beans;
 import org.xillium.data.*;
 import org.xillium.data.persistence.*;
 
 
 public class CrudCommand {
 	private static final String STATEMENT_FIELD_NAME = "_STMT";
     private static final Map<String, Class<? extends DataObject>> _classes = new HashMap<String, Class<? extends DataObject>>();
 
     private final Operation _oper;
     private final String _name;
     private final Class<? extends DataObject> _type;
 
     public static enum Operation {
         CREATE,
         RETRIEVE,
         UPDATE,
         DELETE,
 		SEARCH
     }
 
 	public static class Action {
 		final String[] args;
 		final Operation op;
 
 		public Action(Operation op) {
 			this.op = op;
 			this.args = null;
 		}
 
 		public Action(Operation op, String[] args) {
 			this.op = op;
 			this.args = args;
 		}
 
 		public boolean isValid() {
 			return (op != Operation.UPDATE) || (args != null && args.length > 0);
 		}
 	}
 
     /**
      * Constructs a CrudCommand.
      *
      * The name of the last table is taken as the name of the model.
      */
     public CrudCommand(Connection connection, String prefix, String tables, Action action) throws Exception {
         String[] names = tables.split(" *, *");
         _oper = action.op;
         _name = Beans.toCamelCase(names[names.length-1], '_');
         String cname = className("org.xillium.data.dynamic." + prefix, _name, action);
         synchronized (_classes) {
             Class<? extends DataObject> type = _classes.get(cname);
             if (type == null) {
                 try {
                     type = modelFromTables(connection, cname, action, names);
                 } catch (Exception x) {
                     throw new Exception(tables, x);
                 }
                 _classes.put(cname, type);
             }
             _type = type;
         }
     }
 
     public Class<? extends DataObject> getRequestType() {
         return _type;
     }
 
     public Operation getOperation() {
         return _oper;
     }
 
     public String getName() {
         return _name;
     }
 
     /**
      * Returns an array of ParametricStatement's that perform the designated CRUD operation on the tables.
      *
      * For RETRIEVE and SEARCH operations the array contains only 1 statement.
      */ 
     public ParametricStatement[] getStatements() {
 		try {
 			return (ParametricStatement[])_type.getDeclaredField(STATEMENT_FIELD_NAME).get(null);
 		} catch (Exception x) {
 			throw new RuntimeException("Unexpected CRUD class error", x);
 		}
     }
 
     /**
      * Creates a new Java model class for carrying out the CRUD action on the entity represented by the list of tables.
      */
     public static Class<? extends DataObject>
     modelFromTables(Connection connection, String classname, Action action, String... tablenames) throws Exception {
 		if (!action.isValid()) {
 			throw new RuntimeException("Invalid CRUD action: update without column list");
 		}
 
 		ClassPool pool = ClassPool.getDefault();
         // this line is necessary for web applications (web container class loader in play)
         pool.appendClassPath(new LoaderClassPath(org.xillium.data.DataObject.class.getClassLoader()));
 
 		CtClass cc = pool.makeClass(classname);
 		cc.addInterface(pool.getCtClass("org.xillium.data.DataObject"));
 		ConstPool cp = cc.getClassFile().getConstPool();
 
 		List<String> fragments = new ArrayList<String>();
 		Set<String> unique = new HashSet<String>();
 
 /*SQL*/	StringBuilder
             cols = new StringBuilder(),	// CREATE: COLUMNS, RETRIEVE: TABLES, UPDATE: SET CLAUSES, DELETE: (not used), SEARCH: TABLES
             vals = new StringBuilder(),	// CREATE: VALUES,  RETRIEVE: COND'S, UPDATE: COND'S,      DELETE: COND'S,     SEARCH: COND'S
             flds = new StringBuilder();
 
         DatabaseMetaData meta = connection.getMetaData();
         String schema = meta.getUserName();
 
 		for (int i = 0; i < tablenames.length; ++i) {
 
 			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + tablenames[i]);
 			ResultSetMetaData rsmeta = stmt.getMetaData();
 			Map<String, Integer> colref = new HashMap<String, Integer>();
 			for (int j = 1, jj = rsmeta.getColumnCount(); j <= jj; ++j) {
 				colref.put(rsmeta.getColumnName(j), new Integer(j));
 			}
 
 			Set<String> primaryKeys = new HashSet<String>();
 			ResultSet keys = meta.getPrimaryKeys(connection.getCatalog(), schema, tablenames[i]);
 			while (keys.next()) {
 				primaryKeys.add(keys.getString(PKEY_COLUMN));
 			}
 			keys.close();
 
             // ISA keys and table join conditions
 			Set<String> isaKeys = new HashSet<String>();
 			if (i > 0) {
 				keys = meta.getImportedKeys(connection.getCatalog(), schema, tablenames[i]);
 				while (keys.next()) {
                     String jointable = null;
                     for (int j = 0; j < i; ++j) {
                         if (keys.getString(FKEY_REFERENCED_TABLE).equals(tablenames[j])) {
                             jointable = tablenames[j];
                             break;
                         }
                     }
                     if (jointable != null && primaryKeys.contains(keys.getString(FKEY_REFERENCING_COLUMN))) {
 						String column = keys.getString(FKEY_REFERENCING_COLUMN);
 						isaKeys.add(column);
                         if (action.op == Operation.RETRIEVE || action.op == Operation.SEARCH) {
     /*SQL*/     		    if (vals.length() > 0) {
     /*SQL*/         		    vals.append(" AND ");
     /*SQL*/     		    }
     /*SQL*/     		    vals.append(tablenames[i]).append('.').append(column).append('=').append(jointable).append('.').append(column);
                         }
 					}
 				}
 				keys.close();
 			}
 
 			String alias = ((action.op == Operation.RETRIEVE || action.op == Operation.SEARCH) && tablenames.length > 1) ? tablenames[i]+'.' : "";
 
 			if (action.op != Operation.RETRIEVE && action.op != Operation.SEARCH) {
 				cols.setLength(0);
 				vals.setLength(0);
 				flds.setLength(0);
 			} else {
 	/*SQL*/     if (cols.length() > 0) {
 	/*SQL*/         cols.append(',');
 	/*SQL*/     }
 	/*SQL*/     cols.append(tablenames[i]);
 			}
 
 			Set<String> requested = new HashSet<String>();
 			if (action.op == Operation.UPDATE) {
 				for (String column: action.args) {
 					Integer idx = colref.get(column);
 					if (idx != null) {
 	/*SQL*/         	if (cols.length() > 0) {
 	/*SQL*/             	cols.append(',');
 	/*SQL*/             	flds.append(',');
 	/*SQL*/         	}
 	/*SQL*/         	cols.append(column).append("=COALESCE(?,").append(column).append(')');
 						flds.append(Beans.toLowerCamelCase(column, '_')).append(':').append(rsmeta.getColumnType(idx.intValue()));
 						requested.add(column);
 					}
 				}
 			} else if (action.op == Operation.SEARCH && action.args != null) {
                 for (String column: action.args) {
                     Integer idx = colref.get(column);
                     if (idx != null) {
     /*SQL*/             if (vals.length() > 0) {
     /*SQL*/                 vals.append(" AND ");
     /*SQL*/             }
     /*SQL*/             vals.append(tablenames[i]).append('.').append(column).append("=?");
     /*SQL*/             if (flds.length() > 0) {
     /*SQL*/                 flds.append(',');
     /*SQL*/             }
                         flds.append(Beans.toLowerCamelCase(column, '_')).append(':').append(rsmeta.getColumnType(idx.intValue()));
                         requested.add(column);
                     }
                 }
             }
 
 			ResultSet columns = meta.getColumns(connection.getCatalog(), schema, tablenames[i], "%");
 			while (columns.next()) {
 				String name = columns.getString(COLUMN_NAME), fname = Beans.toLowerCamelCase(name, '_');
 				int idx = colref.get(name).intValue();
 
 				if ((action.op == Operation.RETRIEVE || action.op == Operation.DELETE) && !primaryKeys.contains(name)) {
 					continue;
 				} else if (action.op == Operation.UPDATE && !requested.contains(name) && !primaryKeys.contains(name)) {
 					continue;
 				} else if (action.op == Operation.SEARCH && !requested.contains(name)) {
                     continue;
                 }
 
 				switch (action.op) {
 				case CREATE:
 	/*SQL*/         if (cols.length() > 0) {
 	/*SQL*/             cols.append(',');
 	/*SQL*/             vals.append(',');
 	/*SQL*/             flds.append(',');
 	/*SQL*/         }
 	/*SQL*/         cols.append(name);
 	/*SQL*/         vals.append('?');
 					flds.append(fname).append(':').append(rsmeta.getColumnType(idx));
 					break;
 				case RETRIEVE:
 					if (i > 0) {
 						// NOTE: ISA relation dictates that sub-tables' primary key == super-table's primary key
 						// therefore the join condition generated above is sufficient already
 						break;
 					}
 					// fall through for the super-table
 				case DELETE:
 					// only primary key columns
 	/*SQL*/         if (vals.length() > 0) {
 	/*SQL*/             vals.append(" AND ");
 	/*SQL*/             flds.append(',');
 	/*SQL*/         }
 	/*SQL*/         vals.append(alias).append(name).append("=?");
 					flds.append(fname).append(':').append(rsmeta.getColumnType(idx));
 					break;
 				case UPDATE:
 					// only primary key & updating columns
 					if (primaryKeys.contains(name)) {
 	/*SQL*/         	if (vals.length() > 0) {
 	/*SQL*/             	vals.append(" AND ");
 	/*SQL*/         	}
 	/*SQL*/         	vals.append(name).append("=?");
 	/*SQL*/         	if (flds.length() > 0) {
 	/*SQL*/             	flds.append(',');
 	/*SQL*/         	}
 						flds.append(fname).append(':').append(rsmeta.getColumnType(idx));
 					}
 				case SEARCH:
 					break;
 				}
 
 				if (isaKeys.contains(name)) {
 					continue;
 				} else if (unique.contains(name)) {
 					continue;
 					//throw new RuntimeException("Duplicate column in ISA relationship detected " + tablenames[i] + ':' + name);
 				} else {
 					unique.add(name);
 				}
 
 				CtField field = new CtField(pool.getCtClass(sqlTypeName(rsmeta, idx)), Beans.toLowerCamelCase(name, '_'), cc);
                 field.setModifiers(java.lang.reflect.Modifier.PUBLIC);
 				AnnotationsAttribute attr = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
 
 				if (columns.getInt(IS_NULLABLE) == DatabaseMetaData.attributeNoNulls) {
 					if (action.op != Operation.UPDATE || primaryKeys.contains(name)) {
 						addAnnotation(attr, cp, "org.xillium.data.validation.required");
 					}
 				}
 
 				switch (rsmeta.getColumnType(idx)) {
 				case Types.CHAR:
 				case Types.VARCHAR:
 					addAnnotation(attr, cp, "org.xillium.data.validation.size", "value", new IntegerMemberValue(cp, columns.getInt(COLUMN_SIZE)));
 					break;
 				default:
 					break;
 				}
 
 				field.getFieldInfo().addAttribute(attr);
 				cc.addField(field);
 			}
 			columns.close();
 			stmt.close();
 
 			switch (action.op) {
 			case CREATE:
 				fragments.add("org.xillium.data.persistence.ParametricStatement");
 				fragments.add(flds.toString());
 				fragments.add("INSERT INTO " + tablenames[i] + '(' + cols.toString() + ") VALUES(" + vals.toString() + ')');
 				break;
 			case UPDATE:
                 if (cols.length() > 0) {
                     fragments.add("org.xillium.data.persistence.ParametricStatement");
                     fragments.add(flds.toString());
                     fragments.add("UPDATE " + tablenames[i] + " SET " + cols.toString() + " WHERE " + vals.toString());
                 }
 				break;
 			case DELETE:
 				fragments.add("org.xillium.data.persistence.ParametricStatement");
 				fragments.add(flds.toString());
 				fragments.add("DELETE FROM " + tablenames[i] + " WHERE " + vals.toString());
 				break;
 			case RETRIEVE:
             case SEARCH:
                 break;
 			}
 		}
 
 		if (action.op == Operation.RETRIEVE) {
 			fragments.add("org.xillium.data.persistence.ParametricQuery");
 			fragments.add(flds.toString());
 			fragments.add("SELECT * FROM " + cols + " WHERE " + vals);
 		} else if (action.op == Operation.SEARCH) {
 			fragments.add("org.xillium.data.persistence.ParametricQuery");
 			fragments.add(flds.toString());
             if (vals.length() > 0) {
                 fragments.add("SELECT * FROM " + cols + " WHERE " + vals);
             } else {
                 fragments.add("SELECT * FROM " + cols);
             }
         }
 
 		CtField field = new CtField(pool.getCtClass("org.xillium.data.persistence.ParametricStatement[]"), STATEMENT_FIELD_NAME, cc);
 		field.setModifiers(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.STATIC | java.lang.reflect.Modifier.FINAL);
 		cc.addField(field, CtField.Initializer.byCallWithParams(
 			pool.getCtClass(CrudCommand.class.getName()), "buildStatements", fragments.toArray(new String[fragments.size()])
 		));
 
 		return cc.toClass(CrudCommand.class.getClassLoader(), CrudCommand.class.getProtectionDomain());
     }
 
 	public static ParametricStatement[] buildStatements(String[] args) throws Exception {
 		ParametricStatement[] stmts = new ParametricStatement[args.length/3];
 		for (int i = 0; i < stmts.length; ++i) {
 			stmts[i] = ((ParametricStatement)Class.forName(args[i*3+0]).getConstructor(String.class).newInstance(args[i*3+1])).set(args[i*3+2]);
 		}
 		return stmts;
 	}
 
 	private static final int COLUMN_NAME = 4;
 	private static final int COLUMN_TYPE = 5;	// java.sql.Types.#
 	private static final int COLUMN_SIZE = 7;
 	private static final int IS_NULLABLE = 11;
 	private static final int PKEY_COLUMN = 4;
 	private static final int FKEY_REFERENCED_TABLE = 3;
 	private static final int FKEY_REFERENCED_COLUMN = 4;
 	private static final int FKEY_REFERENCING_COLUMN = 8;
 
     private static void addAnnotation(AnnotationsAttribute attr, ConstPool cp, String aclass) {
         javassist.bytecode.annotation.Annotation annotation = new javassist.bytecode.annotation.Annotation(aclass, cp);
         attr.addAnnotation(annotation);
     }
 
     private static void addAnnotation(AnnotationsAttribute attr, ConstPool cp, String aclass, String attribute, MemberValue value) {
         javassist.bytecode.annotation.Annotation annotation = new javassist.bytecode.annotation.Annotation(aclass, cp);
         annotation.addMemberValue(attribute, value);
         attr.addAnnotation(annotation);
     }
 
     private static String className(String pkg, String name, Action action) {
         StringBuilder sb = new StringBuilder(pkg).append('.').append(name).append(Beans.toCamelCase(action.op.toString(), '_'));
         if (action.args != null) for (String arg: action.args) {
             sb.append(Beans.toCamelCase(arg, '_'));
         }
         return sb.toString();
     }
 
 	private static String sqlTypeName(ResultSetMetaData rsmeta, int index) throws SQLException {
 		switch (rsmeta.getColumnType(index)) {
 		case Types.NUMERIC:
 			int precision = rsmeta.getPrecision(index);
 			if (rsmeta.getScale(index) == 0) {
 				if (precision > 9) {
 					return "java.lang.Long";
 				} else if (precision > 4) {
 					return "java.lang.Integer";
 				} else if (precision > 2) {
 					return "java.lang.Short";
 				} else {
 					return "java.lang.Byte";
 				}
 			} else {
				if (precision > 38) {
 					return "java.lang.Double";
 				} else {
 					return "java.lang.Float";
 				}
 			}
 		default:
 			return rsmeta.getColumnClassName(index);
 		}
 	}
 }
 
