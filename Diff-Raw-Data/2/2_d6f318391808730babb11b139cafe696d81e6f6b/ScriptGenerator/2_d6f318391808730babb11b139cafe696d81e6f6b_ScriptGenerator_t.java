 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.diff.builder;
 
 import org.apache.log4j.Logger;
 
 import java.sql.DatabaseMetaData;
 import java.sql.SQLException;
 
 import java.text.MessageFormat;
 
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 import de.cismet.cids.jpa.entity.cidsclass.Attribute;
 import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
 import de.cismet.cids.jpa.entity.cidsclass.Type;
 
 import de.cismet.diff.DiffAccessor;
 
 import de.cismet.diff.container.Action;
 import de.cismet.diff.container.CodedStatement;
 import de.cismet.diff.container.PSQLStatementGroup;
 import de.cismet.diff.container.Statement;
 import de.cismet.diff.container.StatementGroup;
 import de.cismet.diff.container.Table;
 import de.cismet.diff.container.TableColumn;
 
 import de.cismet.diff.db.DatabaseConnection;
 
 import de.cismet.diff.exception.IllegalCodeException;
 import de.cismet.diff.exception.ScriptGeneratorException;
 
 import de.cismet.diff.util.ProgressionQueue;
 
 /**
  * This class can generate an array of <code>PSQLStatements</code> that contain the statements required to adjust the
  * state of the tables in a database to the state stored in the "cids" system tables. The state in the "cids" system
  * tables is always decisive.
  *
  * @author   Martin Scholl
  * @version  1.0 2007-03-09
  */
 public class ScriptGenerator {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(ScriptGenerator.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient String TMP_COLUMN = "swapper_tmp_" + System.currentTimeMillis(); // NOI18N
 
     private transient Table[] allTables;
     private transient LinkedList<Table> tables;
     private transient LinkedList<CidsClass> classes;
     private transient LinkedList<CidsClass> classesDone;
 
     private transient LinkedList<String> callStack;
 
     private transient LinkedList<StatementGroup> statements;
     private transient PSQLStatementGroup[] psqlStatementGroups;
     private transient HashMap<String, String> typemapCidsToPSQL;
     private transient HashMap<String, String> typemapPSQLtoCids;
     private transient ProgressionQueue queue;
     private transient Properties runtime;
 
     private final transient ResourceBundle exceptionBundle = ResourceBundle.getBundle(
             DiffAccessor.EXCEPTION_RESOURCE_BASE_NAME);
     private final transient ResourceBundle descBundle = ResourceBundle.getBundle(
             "de.cismet.diff.resource.psqlTemplateDescription"); // NOI18N
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ScriptGenerator object.
      *
      * @param  runtime  DOCUMENT ME!
      * @param  t        DOCUMENT ME!
      */
     public ScriptGenerator(final Properties runtime, final TableLoader t) {
         this(runtime, t.getTables(), t.getClasses(), null);
     }
 
     /**
      * Creates a new ScriptGenerator object.
      *
      * @param  runtime  DOCUMENT ME!
      * @param  t        DOCUMENT ME!
      * @param  storage  DOCUMENT ME!
      */
     public ScriptGenerator(final Properties runtime, final TableLoader t, final ProgressionQueue storage) {
         this(runtime, t.getTables(), t.getClasses(), storage);
     }
 
     /**
      * Creates a new ScriptGenerator object.
      *
      * @param  runtime  DOCUMENT ME!
      * @param  tables   DOCUMENT ME!
      * @param  classes  DOCUMENT ME!
      */
     public ScriptGenerator(final Properties runtime, final Table[] tables, final CidsClass[] classes) {
         this(runtime, tables, classes, null);
     }
 
     /**
      * Creates a new ScriptGenerator object.
      *
      * @param   runtime  DOCUMENT ME!
      * @param   tables   DOCUMENT ME!
      * @param   classes  DOCUMENT ME!
      * @param   storage  DOCUMENT ME!
      *
      * @throws  IllegalArgumentException  DOCUMENT ME!
      */
     public ScriptGenerator(
             final Properties runtime,
             final Table[] tables,
             final CidsClass[] classes,
             final ProgressionQueue storage) {
         if ((tables == null) || (tables.length == 0)) {
             throw new IllegalArgumentException(
                 exceptionBundle.getString(
                     DiffAccessor.ILLEGAL_ARGUMENT_EXCEPTION_TABLES_NULL_OR_EMPTY));
         }
         if ((classes == null) || (classes.length == 0)) {
             throw new IllegalArgumentException(
                 exceptionBundle.getString(
                     DiffAccessor.ILLEGAL_ARGUMENT_EXCEPTION_CLASSES_NULL_OR_EMPTY));
         }
         this.allTables = tables;
         this.tables = new LinkedList<Table>();
         for (final Table t : tables) {
             if (t != null) {
                 this.tables.add(t);
             }
         }
         this.classes = new LinkedList<CidsClass>();
         for (final CidsClass c : classes) {
             if (c != null) {
                 this.classes.add(c);
             }
         }
         classesDone = new LinkedList<CidsClass>();
         typemapCidsToPSQL = getTypeMap(true);
         typemapPSQLtoCids = getTypeMap(false);
         statements = new LinkedList<StatementGroup>();
         this.queue = storage;
         this.callStack = new LinkedList<String>();
         psqlStatementGroups = null;
         this.runtime = runtime;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   directionTo  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private HashMap<String, String> getTypeMap(final boolean directionTo) {
         final ResourceBundle bundle = ResourceBundle.getBundle("de.cismet.diff.resource.typemap"); // NOI18N
         final HashMap<String, String> map = new HashMap<String, String>();
         final Enumeration<String> keys = bundle.getKeys();
         while (keys.hasMoreElements()) {
             final String key = keys.nextElement();
             // for bidirectional mapping
             if (directionTo) {
                 map.put(key, bundle.getString(key));
             } else {
                 map.put(bundle.getString(key), key);
             }
         }
         return map;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ScriptGeneratorException  DOCUMENT ME!
      */
     public PSQLStatementGroup[] getStatementGroups() throws ScriptGeneratorException {
         if (psqlStatementGroups != null) {
             return Arrays.copyOf(psqlStatementGroups, psqlStatementGroups.length);
         }
         while (!classes.isEmpty()) {
             callStack.clear();
             final LinkedList<StatementGroup> s = createStatements(classes.getFirst());
             if (s != null) {
                 statements.addAll(s);
             }
         }
         while (!tables.isEmpty()) {
             callStack.clear();
             final StatementGroup s = create_DROP_statement(tables.getFirst());
             if (s != null) {
                 statements.addLast(s);
             }
         }
         final LinkedList<PSQLStatementGroup> psql = new LinkedList<PSQLStatementGroup>();
         try {
             while (!statements.isEmpty()) {
                 final StatementGroup current = statements.removeFirst();
                 if (current != null) {
                     psql.addLast(new PSQLStatementGroup(current));
                 }
             }
         } catch (final IllegalCodeException ex) {
             LOG.error("error during generation: illegal code: " + ex.getCode(), ex); // NOI18N
             throw new ScriptGeneratorException(
                 exceptionBundle.getString(
                     DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_EXC_STATEMENT_CREATE),
                 ex);
         }
         psqlStatementGroups = new PSQLStatementGroup[psql.size()];
         psqlStatementGroups = psql.toArray(psqlStatementGroups);
         return Arrays.copyOf(psqlStatementGroups, psqlStatementGroups.length);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   c  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ScriptGeneratorException  DOCUMENT ME!
      */
     private LinkedList<StatementGroup> createStatements(final CidsClass c) throws ScriptGeneratorException {
         if (c == null) {
             return null;
         }
         final Table t = getTable(c.getTableName());
         if (t != null) {
             return create_ALTER_Statements(c, t);
         }
         return create_CREATE_Statement(c);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   c  DOCUMENT ME!
      * @param   t  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ScriptGeneratorException  DOCUMENT ME!
      */
     private LinkedList<StatementGroup> create_ALTER_Statements(final CidsClass c, final Table t)
             throws ScriptGeneratorException {
         final Iterator<Attribute> it = c.getAttributes().iterator();
         final LinkedList<TableColumn> tableColumns = new LinkedList<TableColumn>();
         final LinkedList<StatementGroup> statementGroups = new LinkedList<StatementGroup>();
         for (final TableColumn tc : t.getColumns()) {
             tableColumns.add(tc);
         }
         // add primary key statements
         final StatementGroup primkeyGroup = createPrimaryKeyStatements(t, c);
         if (primkeyGroup != null) {
             statementGroups.addLast(primkeyGroup);
         }
         while (it.hasNext()) {
             final String warning = null;
             final Attribute current = it.next();
             final String attrName = current.getFieldName();
             final Type attrType = current.getType();
             String attrTypeName = attrType.getName().toLowerCase();
             if (typemapCidsToPSQL.containsKey(attrTypeName)) {
                 attrTypeName = typemapCidsToPSQL.get(attrTypeName);
             }
             // column is present?
             final TableColumn column = t.getTableColumn(attrName);
             // column not present, create new column
             if (column == null) {
                 // <editor-fold defaultstate="collapsed" desc=" handle complex type ">
                 if (attrType.isComplexType()) {
                     // checks whether this type is already present or tries to create it
                     // by calling the createStatements method with the name of the type as
                     // param. the name is also added to a call stack to prevent endless
                     // loops in case of cyclic relation between two or more classes.
                     // type is still not present after creation try, a
                     // ScriptGeneratorException will be thrown.
                     if (!(classesDone.contains(attrType.getCidsClass())
                                     || callStack.contains(attrType.getName()))) {
                         callStack.add(attrType.getName());
                         final LinkedList<StatementGroup> s = createStatements(attrType.getCidsClass());
                         if (s != null) {
                             statementGroups.addAll(s);
                         }
                         if (!classesDone.contains(attrType.getCidsClass())) {
                             throw new ScriptGeneratorException(
                                 exceptionBundle.getString(
                                     DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_MISSING_COMP_TYPE),
                                 t.getTableName(),
                                 attrName,
                                 null);
                         }
                     }
                     // add statement to statementlist
                     final Statement[] s = {
                             new CodedStatement(
                                 CodedStatement.CODE_ALTER_ADD_COLUMN,
                                 warning,
                                 false,
                                 t.getTableName(),
                                 attrName.toLowerCase(),
                                 "INTEGER") // NOI18N
                         };
                     statementGroups.addLast(new StatementGroup(s, false));
                 }                          // </editor-fold>
                 // <editor-fold defaultstate="collapsed" desc=" handle normal type ">
                 else {
                     // add precision to parameter if present
                     String paramAttrType = attrTypeName;
                     // bpchar is just internal and cannot be used to create new columns :(
                     // --> remapping to char
                     if ("bpchar".equals(paramAttrType)) { // NOI18N
                         paramAttrType = "char";           // NOI18N
                     }
                     if (current.getPrecision() != null) {
                         // also add scale to parameter if present
                         if (current.getScale() != null) {
                             paramAttrType = paramAttrType + "("                                 // NOI18N
                                 + current.getPrecision() + ", "                                 // NOI18N
                                 + current.getScale() + ")";                                     // NOI18N
                         } else {
                             paramAttrType = paramAttrType + "(" + current.getPrecision() + ")"; // NOI18N
                         }
                     }
                     final Statement[] s = {
                             new CodedStatement(
                                 CodedStatement.CODE_ALTER_ADD_COLUMN,
                                 null,
                                 false,
                                 t.getTableName(),
                                 attrName,
                                 paramAttrType)
                         };
                     statementGroups.addLast(new StatementGroup(s, false));
                 }                                                                               // </editor-fold>
                 // <editor-fold defaultstate="collapsed" desc=" add default if present ">
                 if (current.getFieldName().equalsIgnoreCase(c.getPrimaryKeyField())) {
                     final Statement[] s = {
                             new CodedStatement(
                                 CodedStatement.CODE_ALTER_COLUMN_SET,
                                 null,
                                 false,
                                 t.getTableName(),
                                 attrName.toLowerCase(),
                                 "DEFAULT nextval('"
                                 + t.getTableName()
                                 + "_seq')") // NOI18N
                         };
                     statementGroups.addLast(new StatementGroup(s, false));
                 } else if (current.getDefaultValue() != null) {
                     if (
                         !isDefaultValueValid(
                                     attrName,
                                     attrTypeName,
                                     current.getPrecision(),
                                     current.getScale(),
                                     current.getDefaultValue())) {
                         throw new ScriptGeneratorException(
                             exceptionBundle.getString(
                                 DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_DEF_TYPE_MISMATCH),
                             t.getTableName(),
                             attrName,
                             null);
                     }
                     final Statement[] s = {
                             new CodedStatement(
                                 CodedStatement.CODE_ALTER_COLUMN_SET,
                                 null,
                                 false,
                                 t.getTableName(),
                                 attrName.toLowerCase(),
                                 "DEFAULT '"
                                 + current.getDefaultValue()
                                 + "'")      // NOI18N
                         };
                     statementGroups.addLast(new StatementGroup(s, false));
                 }                           // </editor-fold>
                 // <editor-fold defaultstate="collapsed" desc=" set not null if needed ">
                 if (!current.isOptional()) {
                     // default value is valid if present due to check above
                     if (current.getDefaultValue() != null) {
                         final Statement[] s = {
                                 new CodedStatement(
                                     CodedStatement.CODE_UPDATE_WHERE_NULL,
                                     null,
                                     false,
                                     t.getTableName(),
                                     attrName.toLowerCase(),
                                     current.getDefaultValue()),
                                 new CodedStatement(
                                     CodedStatement.CODE_ALTER_COLUMN_SET,
                                     null,
                                     false,
                                     t.getTableName(),
                                     attrName.toLowerCase(),
                                     "NOT NULL") // NOI18N
                             };
                         final StatementGroup group = new StatementGroup(s, true);
                         final MessageFormat descform = new MessageFormat(
                                 descBundle.getString(
                                     StatementGroup.GROUP_DESC_UPDATE_AND_NOT_NULL));
                         final String[] args = { t.getTableName(), attrName.toLowerCase() };
                         group.setDescription(descform.format(args));
                         group.setTableName(t.getTableName());
                         group.setColumnName(attrName.toLowerCase());
                         statementGroups.addLast(group);
                     } else {
                         final Statement[] s = {
                                 new CodedStatement(
                                     CodedStatement.CODE_ALTER_COLUMN_SET,
                                     CodedStatement.WARNING_ALTER_COLUMN_TO_NOTNULL,
                                     true,
                                     t.getTableName(),
                                     attrName.toLowerCase(),
                                     "NOT NULL") // NOI18N
                             };
                         statementGroups.addLast(new StatementGroup(s, false));
                     }
                 }                               // </editor-fold>
                 // column present, look for differences
             } else {
                 // <editor-fold defaultstate="collapsed" desc=" handle complex type ">
                 if (attrType.isComplexType()) {
                     // checks whether this type is already present or tries to create it
                     // by calling the createStatements method with the name of the type as
                     // param. the name is also added to a call stack to prevent endless
                     // loops in case of cyclic relation between two or more classes.
                     // type is still not present after creation try, a
                     // ScriptGeneratorException will be thrown.
                     if (!(classesDone.contains(attrType.getCidsClass())
                                     || callStack.contains(attrType.getName()))) {
                         callStack.add(attrType.getName());
                         final LinkedList<StatementGroup> s = createStatements(attrType.getCidsClass());
                         if (s != null) {
                             statementGroups.addAll(s);
                         }
                         if (!classesDone.contains(attrType.getCidsClass())) {
                             throw new ScriptGeneratorException(
                                 exceptionBundle.getString(
                                     DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_MISSING_COMP_TYPE),
                                 t.getTableName(),
                                 attrName,
                                 attrTypeName,
                                 null);
                         }
                     }
                     if (!column.getTypeName().equalsIgnoreCase("int4")) { // NOI18N
                         statementGroups.addLast(createTypeConversionStatements(
                                 t,
                                 current,
                                 "INTEGER"));                              // NOI18N
                     }
                 }                                                         // </editor-fold>
                 // <editor-fold defaultstate="collapsed" desc=" handle normal type ">
                 else {
                     // type mismatch
                     if ((!attrTypeName.equalsIgnoreCase(column.getTypeName()))
                                 || ((current.getPrecision() != null)
                                     && (current.getPrecision().intValue() != column.getPrecision()))
                                 || ((current.getScale() != null)
                                     && (current.getScale().intValue() != column.getScale()))) {
                         // this check has to be added since postgres jdbc driver 8 and later
                         // handles columns, that are of type int or bigint and have not null
                         // constraint as well as a sequence as their default value, as
                         // serial types. serial types basically are integers
                         if ((attrTypeName.equalsIgnoreCase("int4")                         // NOI18N
                                         || attrTypeName.equalsIgnoreCase("int8"))          // NOI18N
                                                                                            // it is already interpreted
                                                                                            // as
                                                                                            // serial if there is a
                                                                                            // sequence
                                                                                            // present, maybe I
                                                                                            // misunderstood the
                                                                                            // description..... -.-
                                                                                            // column.getNullable() ==
                                                                                            // DatabaseMetaData.attributeNoNulls
                                                                                            // &&
                                     && (column.getDefaultValue() != null)
                                     && column.getDefaultValue().startsWith("nextval")) {   // NOI18N
                             if (attrTypeName.equalsIgnoreCase("int4")) {                   // NOI18N
                                 if (!column.getTypeName().equalsIgnoreCase("serial")) {    // NOI18N
                                     statementGroups.addLast(createTypeConversionStatements(
                                             t,
                                             current,
                                             null));
                                 }
                             } else {
                                 if (!column.getTypeName().equalsIgnoreCase("bigserial")) { // NOI18N
                                     statementGroups.addLast(createTypeConversionStatements(
                                             t,
                                             current,
                                             null));
                                 }
                             }
                         } else {
                             statementGroups.addLast(createTypeConversionStatements(
                                     t,
                                     current,
                                     null));
                         }
                     }
                 }                                                                          // </editor-fold>
                 // <editor-fold defaultstate="collapsed" desc=" handle normal attr ">
                 if (!current.getFieldName().equalsIgnoreCase(c.getPrimaryKeyField())) {
                     // <editor-fold defaultstate="collapsed" desc=" drop default value ">
                     if ((current.getDefaultValue() == null)
                                 && (t.getDefaultValue(attrName) != null)) {
                         final Statement[] s = {
                                 new CodedStatement(
                                     CodedStatement.CODE_ALTER_COLUMN_DROP,
                                     null,
                                     false,
                                     t.getTableName(),
                                     attrName.toLowerCase(),
                                     "DEFAULT") // NOI18N
                             };
                         statementGroups.addLast(new StatementGroup(s, false));
                     }
                     // </editor-fold>
                     // <editor-fold defaultstate="collapsed" desc=" set default value ">
                     String defaultVal = t.getDefaultValue(attrName);
                     if (defaultVal != null) {
                         final int i = defaultVal.indexOf("'") + 1; // NOI18N
                         final int j = defaultVal.lastIndexOf("'"); // NOI18N
                         if ((i > 0) && (j > i)) {
                             defaultVal = defaultVal.substring(i, j);
                         }
                     }
                     if ((current.getDefaultValue() != null)
                                 && !current.getDefaultValue().equals(defaultVal)) {
                         if (
                             !isDefaultValueValid(
                                         attrName,
                                         attrTypeName,
                                         current.getPrecision(),
                                         current.getScale(),
                                         current.getDefaultValue())) {
                             throw new ScriptGeneratorException(
                                 exceptionBundle.getString(
                                     DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_DEF_TYPE_MISMATCH),
                                 t.getTableName(),
                                 attrName,
                                 null);
                         }
                         final Statement[] s = {
                                 new CodedStatement(
                                     CodedStatement.CODE_ALTER_COLUMN_SET,
                                     null,
                                     false,
                                     t.getTableName(),
                                     attrName.toLowerCase(),
                                     "DEFAULT '"
                                     + current.getDefaultValue()
                                     + "'")                         // NOI18N
                             };
                         statementGroups.addLast(new StatementGroup(s, false));
                     }                                              // </editor-fold>
                     // <editor-fold defaultstate="collapsed" desc=" alter column to 'optional' ">
                     if (current.isOptional() && !(column.getNullable()
                                     == DatabaseMetaData.attributeNullable)) {
                         if (column.getColumnName().equalsIgnoreCase(c.getPrimaryKeyField())) {
                             throw new ScriptGeneratorException(
                                 exceptionBundle.getString(
                                     DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_PRIMKEY_NOT_NULLABLE),
                                 t.getTableName(),
                                 attrName,
                                 null);
                         }
                         final Statement[] s = {
                                 new CodedStatement(
                                     CodedStatement.CODE_ALTER_COLUMN_DROP,
                                     null,
                                     false,
                                     t.getTableName(),
                                     attrName.toLowerCase(),
                                     "NOT NULL") // NOI18N
                             };
                         statementGroups.addLast(new StatementGroup(s, false));
                     }                           // </editor-fold>
                     // <editor-fold defaultstate="collapsed" desc=" alter column to 'required' ">
                     if (!current.isOptional() && !(column.getNullable()
                                     == DatabaseMetaData.attributeNoNulls)) {
                         if (current.getDefaultValue() != null) {
                             if (
                                 !isDefaultValueValid(
                                             attrName,
                                             attrTypeName,
                                             current.getPrecision(),
                                             current.getScale(),
                                             current.getDefaultValue())) {
                                 throw new ScriptGeneratorException(
                                     exceptionBundle.getString(
                                         DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_DEF_TYPE_MISMATCH),
                                     t.getTableName(),
                                     attrName,
                                     null);
                             }
                             final Statement[] s = {
                                     new CodedStatement(
                                         CodedStatement.CODE_UPDATE_WHERE_NULL,
                                         null,
                                         false,
                                         t.getTableName(),
                                         attrName.toLowerCase(),
                                         current.getDefaultValue()),
                                     new CodedStatement(
                                         CodedStatement.CODE_ALTER_COLUMN_SET,
                                         null,
                                         false,
                                         t.getTableName(),
                                         attrName.toLowerCase(),
                                         "NOT NULL") // NOI18N
                                 };
                             final StatementGroup group = new StatementGroup(s, true);
                             final MessageFormat descform = new MessageFormat(
                                     descBundle.getString(
                                         StatementGroup.GROUP_DESC_UPDATE_AND_NOT_NULL));
                             final String[] args = { t.getTableName(), attrName.toLowerCase() };
                             group.setDescription(descform.format(args));
                             group.setTableName(t.getTableName());
                             group.setColumnName(attrName.toLowerCase());
                             statementGroups.addLast(group);
                         } else {
                             final Statement[] s = {
                                     new CodedStatement(
                                         CodedStatement.CODE_ALTER_COLUMN_SET,
                                         CodedStatement.WARNING_ALTER_COLUMN_TO_NOTNULL,
                                         true,
                                         t.getTableName(),
                                         attrName.toLowerCase(),
                                         "NOT NULL") // NOI18N
                                 };
                             statementGroups.addLast(new StatementGroup(s, false));
                         }
                     }                               // </editor-fold>
                 }                                   // </editor-fold>
                 // <editor-fold defaultstate="collapsed" desc=" handle primary key ">
                 else {
                     final String defVal = column.getDefaultValue();
                     // <editor-fold defaultstate="collapsed" desc=" set default 'nextval' ">
                     if ((defVal == null)
                                 || !(
                                     // this string represents postgres jdbc 7 drivers
                                     defVal.equalsIgnoreCase("nextval('" + t.getTableName() // NOI18N
                                         + "_seq'::text)") // NOI18N // this string represents postgres jdbc 8 drivers
                                     || defVal.equalsIgnoreCase(
                                         "nextval('"
                                         + t.getTableName() // NOI18N
                                         + "_seq'::regclass)"))) { // NOI18N
                         final Statement[] s = {
                                 new CodedStatement(
                                     CodedStatement.CODE_ALTER_COLUMN_SET,
                                     null,
                                     false,
                                     t.getTableName(),
                                     attrName.toLowerCase(),
                                     "DEFAULT nextval('"
                                     + t.getTableName()
                                     + "_seq')")    // NOI18N
                             };
                         statementGroups.addLast(new StatementGroup(s, false));
                     }                              // </editor-fold>
                 }                                  // </editor-fold>
                 tableColumns.remove(column);
             }
         }
         // <editor-fold defaultstate="collapsed" desc=" drop columns that are not in "cs_attr" anymore ">
         while (tableColumns.size() > 0) {
             if (tableColumns.getFirst().getColumnName().equalsIgnoreCase(c.getPrimaryKeyField())) {
                 throw new ScriptGeneratorException(
                     exceptionBundle.getString(
                         DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_PRIMKEY_ATTR_NO_DROP),
                     t.getTableName(),
                     c.getPrimaryKeyField(),
                     null);
             }
             final Statement[] s = {
                     new CodedStatement(
                         CodedStatement.CODE_ALTER_DROP_COLUMN,
                         null,
                         false,
                         t.getTableName(),
                         tableColumns.removeFirst().getColumnName())
                 };
             statementGroups.addLast(new StatementGroup(s, false));
         } // </editor-fold>
         tables.remove(t);
         classes.remove(c);
         classesDone.add(c);
         return statementGroups;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   c  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ScriptGeneratorException  DOCUMENT ME!
      */
     private LinkedList<StatementGroup> create_CREATE_Statement(final CidsClass c) throws ScriptGeneratorException {
         // is name valid
         if (c.getTableName().contains(" ")) { // NOI18N
             throw new ScriptGeneratorException(
                 exceptionBundle.getString(
                     DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_TABLENAME_HAS_SPACES),
                 c.getTableName(),
                 null);
         }
         // is primary key present and valid
         try {
             if (c.getPrimaryKeyField().isEmpty()) {
                 throw new ScriptGeneratorException(
                     exceptionBundle.getString(
                         DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_EMPTY_PRIMKEY_FIELD),
                     c.getTableName(),
                     null);
             }
         } catch (final NullPointerException npe) {
             throw new ScriptGeneratorException(
                 exceptionBundle.getString(
                     DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_MISSING_PRIMKEY_FIELD),
                 c.getTableName(),
                 npe);
         }
         final LinkedList<Statement> statem = new LinkedList<Statement>();
         final HashMap<String, String> map = new HashMap<String, String>();
         map.put(CodedStatement.KEY_TABLENAME, c.getTableName().toLowerCase());
         final StringBuffer nameTypeEnum = new StringBuffer(20);
         final Iterator<Attribute> it = c.getAttributes().iterator();
         final String warning = null;
         boolean primarykeyFound = false;
         while (it.hasNext()) {
             final Attribute current = it.next();
             final String name = current.getFieldName().toLowerCase();
             final Type type = current.getType();
             if (type.isComplexType()) {
                 // checks whether this type is already present or tries to create it
                 // by calling the createStatements method with the name of the type as
                 // param. the name is also added to a call stack to prevent endless
                 // loops in case of cyclic relation between two or more classes.
                 // type is still not present after creation try, a
                 // ScriptGeneratorException will be thrown.
                 if (!(classesDone.contains(type.getCidsClass())
                                 || callStack.contains(type.getName()))) {
                     callStack.add(type.getName());
                     final LinkedList<StatementGroup> s = createStatements(type.getCidsClass());
                     if (s != null) {
                         statements.addAll(s);
                     }
                     if (!classesDone.contains(type.getCidsClass())) {
                         throw new ScriptGeneratorException(
                             exceptionBundle.getString(
                                 DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_MISSING_COMP_TYPE),
                             c.getTableName().toLowerCase(),
                             name,
                             type.getName(),
                             null);
                     }
                 }
                 if (current.isOptional()) {
                     nameTypeEnum.append(name).append(" INTEGER NULL");     // NOI18N
                 } else {
                     nameTypeEnum.append(name).append(" INTEGER NOT NULL"); // NOI18N
                 }
             }
             // handle primary key
             // primary key is always integer and has a sequence as default value
             // the sequence will be created and is named: '<tablename>_seq'
             else if (name.equalsIgnoreCase(c.getPrimaryKeyField())) {
                 primarykeyFound = true;
                 if (current.isOptional()) {
                     throw new ScriptGeneratorException(
                         exceptionBundle.getString(
                             DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_PRIMKEY_ATTR_NOT_NULL),
                         c.getTableName().toLowerCase(),
                         c.getPrimaryKeyField().toLowerCase(),
                         null);
                 }
                 if (!type.getName().equalsIgnoreCase("INTEGER")) {                          // NOI18N
                     throw new ScriptGeneratorException(
                         exceptionBundle.getString(
                             DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_PRIMKEY_NOT_INTEGER),
                         c.getTableName().toLowerCase(),
                         name,
                         type.getName(),
                         null);
                 }
                 nameTypeEnum.append(name).append(" INTEGER");                               // NOI18N
                 nameTypeEnum.append(" PRIMARY KEY DEFAULT nextval('")                       // NOI18N
                 .append(c.getTableName().toLowerCase()).append("_seq')");                   // NOI18N
                 if (!sequenceExists(c.getTableName())) {
                     statem.addFirst(
                         new CodedStatement(
                             CodedStatement.CODE_CREATE_SEQUENCE,
                             null,
                             false,
                             c.getTableName().toLowerCase()
                             + "_seq",                                                       // NOI18N
                             "1"));                                                          // NOI18N
                 }
             } else {
                 nameTypeEnum.append(name).append(' ').append(type.getName().toUpperCase()); // NOI18N
                 if (current.getPrecision() != null) {
                     nameTypeEnum.append('(').append(current.getPrecision());                // NOI18N
                     if (current.getScale() != null) {
                         nameTypeEnum.append(", ").append(current.getScale());               // NOI18N
                     }
                     nameTypeEnum.append(')');                                               // NOI18N
                 }
                 if (!current.isOptional()) {
                     nameTypeEnum.append(" NOT");                                            // NOI18N
                 }
                 nameTypeEnum.append(" NULL");                                               // NOI18N
             }
             // append default value if exists
             if ((current.getDefaultValue() != null) && !name.equalsIgnoreCase(
                             c.getPrimaryKeyField())) {
                 if (
                     !isDefaultValueValid(
                                 name,
                                 type.getName(),
                                 current.getPrecision(),
                                 current.getScale(),
                                 current.getDefaultValue())) {
                     throw new ScriptGeneratorException(
                         exceptionBundle.getString(
                             DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_DEF_TYPE_MISMATCH),
                         c.getTableName().toLowerCase(),
                         name,
                         null);
                 }
                 nameTypeEnum.append(" DEFAULT '").append(current.getDefaultValue()).append("'"); // NOI18N
             }
             nameTypeEnum.append(", ");                                                           // NOI18N
         }
        if (!primarykeyFound) {
             throw new ScriptGeneratorException(
                 exceptionBundle.getString(
                     DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_MISSING_PRIMKEY_FIELD),
                 c.getTableName(),
                 null);
         }
         nameTypeEnum.delete(nameTypeEnum.length() - 2, nameTypeEnum.length());
         map.put(CodedStatement.KEY_NAME_TYPE_ENUM, nameTypeEnum.toString());
         classes.remove(c);
         classesDone.add(c);
         statem.addLast(new CodedStatement(
                 CodedStatement.CODE_CREATE_STANDARD,
                 warning,
                 false,
                 map));
         Statement[] s = new Statement[statem.size()];
         s = statem.toArray(s);
         final StatementGroup group = new StatementGroup(s, true);
         group.setTableName(c.getTableName().toLowerCase());
         final MessageFormat descform = new MessageFormat(descBundle.getString(StatementGroup.GROUP_DESC_NEW_TABLE));
         final String[] args = { c.getTableName().toLowerCase() };
         group.setDescription(descform.format(args));
         final LinkedList<StatementGroup> l = new LinkedList<StatementGroup>();
         l.add(group);
         return l;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   table  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private StatementGroup create_DROP_statement(final Table table) {
         final HashMap<String, String> map = new HashMap<String, String>();
         map.put(CodedStatement.KEY_TABLENAME, table.getTableName());
         tables.remove(table);
         final LinkedList<Statement> statem = new LinkedList<Statement>();
         // if a queue is provided check first if a table has really been dropped by
         // the user or if the table is just in the database with no relation to the
         // cids system
         if (queue != null) {
             final Action[] drops = queue.getActionArray(Action.DROP_ACTION);
             if (drops != null) {
                 for (final Action action : drops) {
                     // args[0] shall always be the table name if a drop action is stored
                     if (action.getArgs()[0].equalsIgnoreCase(table.getTableName())) {
                         // sequence has to be dropped after table
                         if (sequenceExists(table.getTableName())) {
                             statem.add(
                                 new CodedStatement(
                                     CodedStatement.CODE_DROP_SEQUENCE,
                                     null,
                                     false,
                                     table.getTableName()
                                     + "_seq")); // NOI18N
                         }
                         statem.addFirst(new CodedStatement(
                                 CodedStatement.CODE_DROP_STANDARD,
                                 null,
                                 false,
                                 map));
                         final Statement[] s = statem.toArray(new Statement[statem.size()]);
                         final StatementGroup ret = new StatementGroup(s, true);
                         ret.setDescription(
                             MessageFormat.format(
                                 descBundle.getString(StatementGroup.GROUP_DESC_NEW_TABLE),
                                 table.getTableName()));
                         ret.setTableName(table.getTableName());
                         return ret;
                     }
                 }
             }
         } else {
             // sequence has to be dropped after table
             if (sequenceExists(table.getTableName())) {
                 statem.add(
                     new CodedStatement(
                         CodedStatement.CODE_DROP_SEQUENCE,
                         null,
                         false,
                         table.getTableName()
                         + "_seq")); // NOI18N
             }
             statem.addFirst(new CodedStatement(CodedStatement.CODE_DROP_STANDARD, null, false, map));
             final Statement[] s = statem.toArray(new Statement[statem.size()]);
             final StatementGroup ret = new StatementGroup(s, true);
             ret.setDescription(
                 MessageFormat.format(descBundle.getString(StatementGroup.GROUP_DESC_NEW_TABLE), table.getTableName()));
             ret.setTableName(table.getTableName());
             return ret;
         }
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   table            DOCUMENT ME!
      * @param   attr             DOCUMENT ME!
      * @param   typeToConvertIn  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private StatementGroup createTypeConversionStatements(
             final Table table,
             final Attribute attr,
             final String typeToConvertIn) {
         final LinkedList<CodedStatement> cstatem = new LinkedList<CodedStatement>();
         final String attrTypeName = attr.getType().getName().toUpperCase();
         final String warning = CodedStatement.WARNING_TYPE_MISMATCH;
         String newTypeName = null;
         if (typeToConvertIn == null) {
             if (attr.getPrecision() == null) {
                 newTypeName = attrTypeName;
             } else {
                 newTypeName = attrTypeName + "(" + attr.getPrecision(); // NOI18N
                 if (attr.getScale() != null) {
                     newTypeName += ", " + attr.getScale();              // NOI18N
                 }
                 newTypeName += ")";                                     // NOI18N
             }
             cstatem.addLast(
                 new CodedStatement(
                     CodedStatement.CODE_ALTER_ADD_COLUMN,
                     warning,
                     false,
                     table.getTableName(),
                     TMP_COLUMN,
                     newTypeName));
         } else {
             cstatem.addLast(
                 new CodedStatement(
                     CodedStatement.CODE_ALTER_ADD_COLUMN,
                     warning,
                     false,
                     table.getTableName(),
                     TMP_COLUMN,
                     typeToConvertIn.toUpperCase()));
             newTypeName = typeToConvertIn.toUpperCase();
         }
         if (attr.getDefaultValue() != null) {
             cstatem.addLast(
                 new CodedStatement(
                     CodedStatement.CODE_ALTER_COLUMN_SET,
                     warning,
                     false,
                     table.getTableName(),
                     TMP_COLUMN,
                     "DEFAULT '"
                     + attr.getDefaultValue()
                     + "'"));                                            // NOI18N
         }
         cstatem.addLast(
             new CodedStatement(
                 CodedStatement.CODE_UPDATE_COPY,
                 warning,
                 false,
                 table.getTableName(),
                 TMP_COLUMN,
                 attr.getFieldName()));
         // copy has to be performed first, otherwise the set not null statement
         // will obviously fail as long as there is at least one row in the table
         if (!attr.isOptional()) {
             cstatem.addLast(
                 new CodedStatement(
                     CodedStatement.CODE_ALTER_COLUMN_SET,
                     warning,
                     false,
                     table.getTableName(),
                     TMP_COLUMN,
                     "NOT NULL")); // NOI18N
         }
         cstatem.addLast(
             new CodedStatement(
                 CodedStatement.CODE_ALTER_DROP_COLUMN,
                 warning,
                 false,
                 table.getTableName(),
                 attr.getFieldName()));
         cstatem.addLast(
             new CodedStatement(
                 CodedStatement.CODE_ALTER_RENAME_COLUMN,
                 warning,
                 false,
                 table.getTableName(),
                 attr.getFieldName(),
                 TMP_COLUMN));
         final Statement[] stmt = cstatem.toArray(new Statement[cstatem.size()]);
         final StatementGroup group = new StatementGroup(stmt, true);
         group.setTableName(table.getTableName().toLowerCase());
         group.setColumnName(attr.getFieldName().toLowerCase());
         final MessageFormat descform = new MessageFormat(
                 descBundle.getString(
                     StatementGroup.GROUP_DESC_UPDATE_AND_NOT_NULL));
         final String[] args = {
                 table.getTableName(),
                 attr.getFieldName().toLowerCase(),
                 table.getTableColumn(attr.getFieldName()).getTypeName().toUpperCase(),
                 newTypeName
             };
         group.setDescription(descform.format(args));
         group.setWarning(descBundle.getString(StatementGroup.WARNING_CONVERT_ERROR_ON_TYPE_MISMATCH));
         return group;
     }
 
     /**
      * NOTE: typesize, default_value and optional field will be ignored when checking and creating the primary key. type
      * of the primary key has to be integer otherwise exception
      *
      * @param   table      DOCUMENT ME!
      * @param   cidsClass  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ScriptGeneratorException  DOCUMENT ME!
      */
     private StatementGroup createPrimaryKeyStatements(final Table table, final CidsClass cidsClass)
             throws ScriptGeneratorException {
         final LinkedList<CodedStatement> codedStatements = new LinkedList<CodedStatement>();
         StatementGroup createGroup = null;
         // check for primary key
         // eventually drop existing and/or create new one
         try {
             if (!containsPrimkeyAttr(cidsClass)) {
                 throw new ScriptGeneratorException(
                     exceptionBundle.getString(
                         DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_PRIMKEY_ATTR_NOT_FOUND),
                     table.getTableName(),
                     cidsClass.getPrimaryKeyField().toLowerCase(),
                     null);
             }
             if (isPrimkeyAttrOptional(cidsClass)) {
                 throw new ScriptGeneratorException(
                     exceptionBundle.getString(
                         DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_PRIMKEY_ATTR_NOT_NULL),
                     table.getTableName(),
                     cidsClass.getPrimaryKeyField().toLowerCase(),
                     null);
             }
             if (!isPrimkeyAttrInteger(cidsClass)) {
                 throw new ScriptGeneratorException(
                     exceptionBundle.getString(
                         DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_PRIMKEY_NOT_INTEGER),
                     table.getTableName(),
                     cidsClass.getPrimaryKeyField().toLowerCase(),
                     null);
             }
             if (!sequenceExists(cidsClass.getTableName() + "_seq")) { // NOI18N
                 if (isTableEmpty(cidsClass.getTableName())) {
                     codedStatements.addFirst(
                         new CodedStatement(
                             CodedStatement.CODE_CREATE_SEQUENCE,
                             null,
                             false,
                             cidsClass.getTableName().toLowerCase()
                             + "_seq",                                 // NOI18N
                             "1"));                                    // NOI18N
                 } else {
                     codedStatements.addFirst(
                         new CodedStatement(
                             CodedStatement.CODE_SELECT_SETVAL_MAX,
                             null,
                             false,
                             cidsClass.getTableName().toLowerCase(),
                             cidsClass.getPrimaryKeyField().toLowerCase(),
                             cidsClass.getTableName().toLowerCase()
                             + "_seq"));                               // NOI18N
                     codedStatements.addFirst(
                         new CodedStatement(
                             CodedStatement.CODE_CREATE_SEQUENCE,
                             null,
                             false,
                             cidsClass.getTableName().toLowerCase()
                             + "_seq",                                 // NOI18N
                             "1"));                                    // NOI18N
                 }
             }
             // composite primary key, drop it and create new
             if (table.getPrimaryKeyColumnNames().length > 1) {
                 codedStatements.addLast(
                     new CodedStatement(
                         CodedStatement.CODE_ALTER_DROP_CONSTRAINT,
                         null,
                         false,
                         table.getTableName(),
                         table.getTableName()
                         + "_pkey")); // NOI18N
                 codedStatements.addLast(
                     new CodedStatement(
                         CodedStatement.CODE_ALTER_ADD_PRIMARY,
                         null,
                         false,
                         table.getTableName().toLowerCase(),
                         cidsClass.getPrimaryKeyField().toLowerCase()));
             }
             // no primary key, create new
             else if (table.getPrimaryKeyColumnNames().length < 1) {
                 codedStatements.addLast(
                     new CodedStatement(
                         CodedStatement.CODE_ALTER_ADD_PRIMARY,
                         null,
                         false,
                         table.getTableName().toLowerCase(),
                         cidsClass.getPrimaryKeyField().toLowerCase()));
             }                        // if key not equals, drop it create new
             else if (!table.getPrimaryKeyColumnNames()[0].equalsIgnoreCase(
                             cidsClass.getPrimaryKeyField())) {
                 codedStatements.addLast(
                     new CodedStatement(
                         CodedStatement.CODE_ALTER_DROP_CONSTRAINT,
                         CodedStatement.WARNING_DROP_PRIMARY_KEY,
                         false,
                         table.getTableName(),
                         table.getTableName()
                         + "_pkey")); // NOI18N
                 codedStatements.addLast(
                     new CodedStatement(
                         CodedStatement.CODE_ALTER_ADD_PRIMARY,
                         CodedStatement.WARNING_NEW_PRIMARY_KEY,
                         false,
                         table.getTableName().toLowerCase(),
                         cidsClass.getPrimaryKeyField().toLowerCase()));
             }
             if (!codedStatements.isEmpty()) {
                 final Statement[] stmts = codedStatements.toArray(new Statement[codedStatements.size()]);
                 createGroup = new StatementGroup(stmts, true);
                 createGroup.setDescription(
                     MessageFormat.format(
                         descBundle.getString(StatementGroup.GROUP_DESC_PRIM_KEY_FIT),
                         table.getTableName()));
                 createGroup.setTableName(table.getTableName());
                 createGroup.setColumnName(cidsClass.getPrimaryKeyField().toLowerCase());
             }
         }
         // npe is thrown when c.getPrimaryKeyField() returns null and method will be
         // accessed from returning value. So then the primary key is missing
         catch (final NullPointerException npe) {
             throw new ScriptGeneratorException(
                 exceptionBundle.getString(
                     DiffAccessor.SCRIPT_GENERATOR_EXCEPTION_MISSING_PRIMKEY_FIELD),
                 cidsClass.getTableName(),
                 npe);
         }
         return createGroup;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   name  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Table getTable(final String name) {
         for (final Table t : allTables) {
             if (t.getTableName().equalsIgnoreCase(name)) {
                 return t;
             }
         }
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   cidsClass  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean containsPrimkeyAttr(final CidsClass cidsClass) {
         final Iterator<Attribute> it = cidsClass.getAttributes().iterator();
         while (it.hasNext()) {
             if (it.next().getFieldName().equalsIgnoreCase(cidsClass.getPrimaryKeyField())) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   cidsClass  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isPrimkeyAttrOptional(final CidsClass cidsClass) {
         final Iterator<Attribute> it = cidsClass.getAttributes().iterator();
         while (it.hasNext()) {
             final Attribute current = it.next();
             if (current.getFieldName().equalsIgnoreCase(cidsClass.getPrimaryKeyField())) {
                 return current.isOptional();
             }
         }
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   cidsClass  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isPrimkeyAttrInteger(final CidsClass cidsClass) {
         final Iterator<Attribute> it = cidsClass.getAttributes().iterator();
         while (it.hasNext()) {
             final Attribute current = it.next();
             if (current.getFieldName().equalsIgnoreCase(cidsClass.getPrimaryKeyField())) {
                 return current.getType().getName().equalsIgnoreCase("INTEGER") ? true : false; // NOI18N
             }
         }
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   seqName  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean sequenceExists(final String seqName) {
         try {
             DatabaseConnection.execSQL(runtime, "SELECT * FROM " + seqName, this.hashCode()); // NOI18N
             return true;
         } catch (final SQLException ex) {
             return false;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   tableName  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isTableEmpty(final String tableName) {
         try {
             return !DatabaseConnection.execSQL(runtime, "SELECT * FROM " + tableName, this.hashCode()).next(); // NOI18N
         } catch (final SQLException ex) {
             return true;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   column        DOCUMENT ME!
      * @param   typename      DOCUMENT ME!
      * @param   precision     DOCUMENT ME!
      * @param   scale         DOCUMENT ME!
      * @param   defaultValue  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isDefaultValueValid(
             final String column,
             final String typename,
             final Integer precision,
             final Integer scale,
             final String defaultValue) {
         try {
             String csTypename = typename;
             // map typename back from postgres internal to cids type name if necessary
             if (typemapPSQLtoCids.containsKey(typename)) {
                 csTypename = typemapPSQLtoCids.get(typename);
             }
             DatabaseConnection.updateSQL(runtime, "BEGIN WORK", this.hashCode()); // NOI18N
             final StringBuffer sb = new StringBuffer(50);
             // build a new temporary table creation string using the given values
             sb.append("CREATE TEMP TABLE cs_tmptable (").append(column).append(' ').append(csTypename); // NOI18N
             // typesize != null indicates that a type is parameterized
             if (precision != null) {
                 sb.append('(').append(precision);                      // NOI18N
                 if (scale != null) {
                     sb.append(", ").append(scale);                     // NOI18N
                 }
                 sb.append(')');                                        // NOI18N
             }
             sb.append(" DEFAULT '").append(defaultValue).append("')"); // NOI18N
             // try to create table from creation string, if failes due to exception it
             // indicates that the default value is not valid
             DatabaseConnection.updateSQL(runtime, sb.toString(), this.hashCode());
             // try to insert the default values into the table, if fails due to
             // exception it indicates that the default value has correct type but does
             // not fit in the reserved space for this column and so is not valid
             DatabaseConnection.updateSQL(runtime, "INSERT INTO cs_tmptable DEFAULT VALUES", this.hashCode()); // NOI18N
             // everyting was fine
             return true;
         } catch (final SQLException ex) {
             // an exception indicated that the value is not valid
             return false;
         } finally {
             try {
                 // rollback to delete the temporary table
                 DatabaseConnection.updateSQL(runtime, "ROLLBACK", this.hashCode()); // NOI18N
             } catch (SQLException ex) {
                 // do nothing, table will be deleted when session ends
                 LOG.error("temp table could not be deleted", ex); // NOI18N
             }
         }
     }
 }
