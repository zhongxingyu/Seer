 /*******************************************************************************
  * Copyright (c) 2011 Oracle. All rights reserved.
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
  * which accompanies this distribution.
  * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
  * and the Eclipse Distribution License is available at
  * http://www.eclipse.org/org/documents/edl-v10.php.
  *
  * Contributors:
  *     Mike Norman - June 10 2011, created DDL parser package
  ******************************************************************************/
 package org.eclipse.persistence.tools.oracleddl.util;
 
 //javase imports
 import java.io.StringReader;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.ServiceLoader;
 import java.util.Stack;
 
 //DDL parser imports
 import org.eclipse.persistence.tools.oracleddl.metadata.CompositeDatabaseType;
 import org.eclipse.persistence.tools.oracleddl.metadata.DatabaseType;
 import org.eclipse.persistence.tools.oracleddl.metadata.FieldType;
 import org.eclipse.persistence.tools.oracleddl.metadata.FunctionType;
 import org.eclipse.persistence.tools.oracleddl.metadata.PLSQLPackageType;
 import org.eclipse.persistence.tools.oracleddl.metadata.PLSQLRecordType;
 import org.eclipse.persistence.tools.oracleddl.metadata.ProcedureType;
 import org.eclipse.persistence.tools.oracleddl.metadata.ROWTYPEType;
 import org.eclipse.persistence.tools.oracleddl.metadata.TYPEType;
 import org.eclipse.persistence.tools.oracleddl.metadata.TableType;
 import org.eclipse.persistence.tools.oracleddl.metadata.UnresolvedType;
 import org.eclipse.persistence.tools.oracleddl.metadata.visit.UnresolvedTypesVisitor;
 import org.eclipse.persistence.tools.oracleddl.parser.DDLParser;
 import org.eclipse.persistence.tools.oracleddl.parser.ParseException;
 
 public class DatabaseTypeBuilder {
 
     //special catalog
     public static final String TOPLEVEL = "TOPLEVEL";
     public static final String ROWTYPE_MACRO = "%ROWTYPE";
     public static final String TYPE_MACRO = "%TYPE";
 
     static DBMSMetadataSessionTransforms TRANSFORMS_FACTORY;
     static {
         ServiceLoader<DBMSMetadataSessionTransforms> transformsFactories =
             ServiceLoader.load(DBMSMetadataSessionTransforms.class);
         Iterator<DBMSMetadataSessionTransforms> i = transformsFactories.iterator();
         //we are only expecting one transforms factory - any additional are ignored
         if (i.hasNext()) {
             TRANSFORMS_FACTORY = i.next();
         }
         else {
             TRANSFORMS_FACTORY = null;
         }
     }
 
     static final String DBMS_METADATA_GET_DDL_STMT_PREFIX =
         "SELECT DBMS_METADATA.GET_DDL('";
     static final String DBMS_METADATA_GET_DDL_STMT1 =
     	"', AO.OBJECT_NAME) AS RESULT FROM ALL_OBJECTS AO WHERE ";
     static final String EXCLUDED_ADMIN_SCHEMAS =
         "'*SYS*|XDB|*ORD*|DBSNMP|ANONYMOUS|OUTLN|MGMT_VIEW|SI_INFORMTN_SCHEMA|WK_TEST|WKPROXY'";
     static final String DBMS_METADATA_GET_DDL_STMT_SUFFIX =
         " REGEXP_LIKE(OWNER,?) AND OBJECT_TYPE = ? AND OBJECT_NAME LIKE ?";
 
     //OBJECT_TYPE codes from ALL_OBJECTS view - we are only interested in top-level types:
     static final int UNKNOWN_CODE = -1;
     static final int FUNCTION_CODE = 1;
     static final int PACKAGE_CODE = 2;
     static final int PROCEDURE_CODE = 3;
     static final int TABLE_CODE = 4;
     static final int TYPE_CODE = 5;
     static final String ALL_OBJECTS_OBJECT_TYPE_FIELD = "OBJECT_TYPE";
     static final String GET_OBJECT_TYPE_STMT =
         "SELECT DECODE(AO." + ALL_OBJECTS_OBJECT_TYPE_FIELD +
             ", 'FUNCTION', 1, 'PACKAGE', 2, 'PROCEDURE', 3, 'TABLE', 4, 'TYPE', 5, -1) AS OBJECT_TYPE" +
             " FROM ALL_OBJECTS AO WHERE (STATUS = 'VALID' AND OWNER LIKE ? AND OBJECT_NAME = ?)";
 
     protected boolean transformsSet = false;
 
     public DatabaseTypeBuilder() {
         super();
     }
 
     public List<TableType> buildTables(Connection conn, String schemaPattern, String tablePattern)
         throws ParseException {
         return buildTables(conn, schemaPattern, tablePattern, true);
     }
     protected List<TableType> buildTables(Connection conn, String schemaPattern, String tablePattern,
         boolean resolveTypes) throws ParseException {
         String schemaPatternU = schemaPattern == null ? null : schemaPattern.toUpperCase();
         String tablePatternU = tablePattern == null ? null : tablePattern.toUpperCase();
         List<TableType> tableTypes = null;
         if (setDbmsMetadataSessionTransforms(conn)) {
             List<String> ddls = getDDLs(conn, "TABLE", DBMS_METADATA_GET_DDL_STMT_PREFIX + "TABLE" +
                 DBMS_METADATA_GET_DDL_STMT1 + (schemaPatternExcludesAdminSchemas(schemaPatternU)
                     ? "NOT" : "") + DBMS_METADATA_GET_DDL_STMT_SUFFIX,
                 schemaPatternExcludesAdminSchemas(schemaPatternU)
                     ? EXCLUDED_ADMIN_SCHEMAS : schemaPatternU, tablePatternU);
             if (ddls != null) {
                 tableTypes = new ArrayList<TableType>();
                 for (String ddl : ddls) {
                     DDLParser parser = newDDLParser(ddl);
                     TableType tableType = parser.parseTable();
                     if (tableType != null) {
                         tableTypes.add(tableType);
                         if (resolveTypes) {
                             UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
                             unresolvedTypesVisitor.visit(tableType);
                             if (!unresolvedTypesVisitor.getUnresolvedTypes().isEmpty()) {
                                 resolvedTypes(conn, schemaPatternU, parser,
                                     unresolvedTypesVisitor.getUnresolvedTypes(), tableType);
                             }
                         }
                     }
                 }
             }
         }
         return tableTypes;
     }
 
     public List<PLSQLPackageType> buildPackages(Connection conn, String schemaPattern,
         String packagePattern) throws ParseException {
         return buildPackages(conn, schemaPattern, packagePattern, true);
     }
     protected List<PLSQLPackageType> buildPackages(Connection conn, String schemaPattern,
         String packagePattern, boolean resolveTypes) throws ParseException {
         String schemaPatternU = schemaPattern == null ? null : schemaPattern.toUpperCase();
         String packagePatternU = packagePattern == null ? null : packagePattern.toUpperCase();
         List<PLSQLPackageType> packageTypes = null;
         if (setDbmsMetadataSessionTransforms(conn)) {
             List<String> ddls = getDDLs(conn, "PACKAGE", DBMS_METADATA_GET_DDL_STMT_PREFIX + "PACKAGE" +
                 DBMS_METADATA_GET_DDL_STMT1 + (schemaPatternExcludesAdminSchemas(schemaPatternU)
                     ? "NOT" : "") + DBMS_METADATA_GET_DDL_STMT_SUFFIX,
                 schemaPatternExcludesAdminSchemas(schemaPatternU)
                     ? EXCLUDED_ADMIN_SCHEMAS : schemaPatternU, packagePatternU);
             if (ddls != null) {
                 packageTypes = new ArrayList<PLSQLPackageType>();
                 for (String ddl : ddls) {
                     DDLParser parser = newDDLParser(ddl);
                     PLSQLPackageType packageType = parser.parsePLSQLPackage();
                     if (packageType != null) {
                         packageTypes.add(packageType);
                         if (resolveTypes) {
                             UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
                             unresolvedTypesVisitor.visit(packageType);
                             if (!unresolvedTypesVisitor.getUnresolvedTypes().isEmpty()) {
                                 resolvedTypes(conn, schemaPatternU, parser, unresolvedTypesVisitor.getUnresolvedTypes(),
                                     packageType);
                             }
                         }
                     }
                 }
             }
         }
         return packageTypes;
     }
 
     public List<ProcedureType> buildProcedures(Connection conn, String schemaPattern,
         String procedurePattern) throws ParseException {
         return buildProcedures(conn, schemaPattern, procedurePattern, true);
     }
     protected List<ProcedureType> buildProcedures(Connection conn, String schemaPattern,
         String procedurePattern, boolean resolveTypes) throws ParseException {
         String schemaPatternU = schemaPattern == null ? null : schemaPattern.toUpperCase();
         String procedurePatternU = procedurePattern == null ? null : procedurePattern.toUpperCase();
     	List<ProcedureType> procedureTypes = null;
         if (setDbmsMetadataSessionTransforms(conn)) {
             List<String> ddls = getDDLs(conn, "PROCEDURE", DBMS_METADATA_GET_DDL_STMT_PREFIX + "PROCEDURE" +
                 DBMS_METADATA_GET_DDL_STMT1 + (schemaPatternExcludesAdminSchemas(schemaPatternU)
                     ? "NOT" : "") + DBMS_METADATA_GET_DDL_STMT_SUFFIX,
                 schemaPatternExcludesAdminSchemas(schemaPatternU)
                     ? EXCLUDED_ADMIN_SCHEMAS : schemaPatternU, procedurePatternU);
             if (ddls != null) {
             	procedureTypes = new ArrayList<ProcedureType>();
             	for (String ddl : ddls) {
 	                DDLParser parser = newDDLParser(ddl);
 	                ProcedureType procedureType = parser.parseTopLevelProcedure();
 	                if (procedureType != null) {
 	                	procedureTypes.add(procedureType);
                         if (resolveTypes) {
     	                    UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
     	                    unresolvedTypesVisitor.visit(procedureType);
     	                    if (!unresolvedTypesVisitor.getUnresolvedTypes().isEmpty()) {
     	                        resolvedTypes(conn, schemaPatternU, parser, unresolvedTypesVisitor.getUnresolvedTypes(),
     	                            procedureType);
     	                    }
                         }
 	                }
             	}
             }
 
         }
         return procedureTypes;
     }
 
     public List<FunctionType> buildFunctions(Connection conn, String schemaPattern,
         String functionPattern) throws ParseException {
         return buildFunctions(conn, schemaPattern, functionPattern, true);
     }
     protected List<FunctionType> buildFunctions(Connection conn, String schemaPattern,
         String functionPattern, boolean resolveTypes) throws ParseException {
         String schemaPatternU = schemaPattern == null ? null : schemaPattern.toUpperCase();
         String functionPatternU = functionPattern == null ? null : functionPattern.toUpperCase();
     	List<FunctionType> functionTypes = null;
         if (setDbmsMetadataSessionTransforms(conn)) {
             List<String> ddls = getDDLs(conn, "FUNCTION", DBMS_METADATA_GET_DDL_STMT_PREFIX + "FUNCTION" +
                 DBMS_METADATA_GET_DDL_STMT1 + (schemaPatternExcludesAdminSchemas(schemaPatternU)
                     ? "NOT" : "") + DBMS_METADATA_GET_DDL_STMT_SUFFIX,
                 schemaPatternExcludesAdminSchemas(schemaPatternU)
                     ? EXCLUDED_ADMIN_SCHEMAS : schemaPatternU, functionPatternU);
             if (ddls != null) {
             	functionTypes = new ArrayList<FunctionType>();
             	for (String ddl : ddls) {
 	                DDLParser parser = newDDLParser(ddl);
 	                FunctionType functionType = parser.parseTopLevelFunction();
 	                if (functionType != null) {
 	                	functionTypes.add(functionType);
                         if (resolveTypes) {
     	                    UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
     	                    unresolvedTypesVisitor.visit(functionType);
     	                    if (!unresolvedTypesVisitor.getUnresolvedTypes().isEmpty()) {
     	                        resolvedTypes(conn, schemaPatternU, parser, unresolvedTypesVisitor.getUnresolvedTypes(),
     	                            functionType);
     	                    }
                         }
 	                }
             	}
             }
 
         }
         return functionTypes;
     }
 
     public List<CompositeDatabaseType> buildTypes(Connection conn, String schemaPattern,
         String typePattern) throws ParseException {
         return buildTypes(conn, schemaPattern, typePattern, true);
     }
     protected List<CompositeDatabaseType> buildTypes(Connection conn, String schemaPattern,
         String typePattern, boolean resolveTypes) throws ParseException {
         String schemaPatternU = schemaPattern == null ? null : schemaPattern.toUpperCase();
         String typePatternU = typePattern == null ? null : typePattern.toUpperCase();
     	List<CompositeDatabaseType> databaseTypes = null;
         if (setDbmsMetadataSessionTransforms(conn)) {
             List<String> ddls = getDDLs(conn, "TYPE", DBMS_METADATA_GET_DDL_STMT_PREFIX + "TYPE" +
                 DBMS_METADATA_GET_DDL_STMT1 + (schemaPatternExcludesAdminSchemas(schemaPatternU)
                     ? "NOT" : "") + DBMS_METADATA_GET_DDL_STMT_SUFFIX,
                 schemaPatternExcludesAdminSchemas(schemaPatternU)
                     ? EXCLUDED_ADMIN_SCHEMAS : schemaPatternU, typePatternU);
             if (ddls != null) {
             	databaseTypes = new ArrayList<CompositeDatabaseType>();
             	for (String ddl : ddls) {
 	                DDLParser parser = newDDLParser(ddl);
 	            	CompositeDatabaseType databaseType = parser.parseType();
 	                if (databaseType != null) {
 	                	databaseTypes.add(databaseType);
                         if (resolveTypes) {
     	                    UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
    	                    unresolvedTypesVisitor.visit(databaseType);
     	                    if (!unresolvedTypesVisitor.getUnresolvedTypes().isEmpty()) {
     	                        resolvedTypes(conn, schemaPatternU, parser, unresolvedTypesVisitor.getUnresolvedTypes(),
     	                            databaseType);
     	                    }
                         }
 	                }
             	}
             }
         }
         return databaseTypes;
     }
 
     protected DDLParser newDDLParser(String ddl) {
         DDLParser parser = new DDLParser(new StringReader(ddl));
         parser.setTypesRepository(new DatabaseTypesRepository());
         return parser;
     }
 
     protected List<String> getDDLs(Connection conn, String typeSpec, String metadataSpec,
         String schemaPattern, String typeName) {
     	List<String> ddls = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try {
             pStmt = conn.prepareStatement(metadataSpec);
             pStmt.setString(1, schemaPattern);
             pStmt.setString(2, typeSpec);
             pStmt.setString(3, typeName);
             rs = pStmt.executeQuery();
             if (rs.next()) {
             	ddls = new ArrayList<String>();
                 do {
                 	String ddl = rs.getString("RESULT").trim();
                     if (ddl.endsWith("/")) {
                         ddl = (String)ddl.subSequence(0, ddl.length()-1);
                     }
                 	ddls.add(ddl);
 
                 } while (rs.next());
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         finally {
             if (rs != null) {
                 try {
                     rs.close();
                 }
                 catch (SQLException e) {
                     // ignore
                 }
             }
             if (pStmt != null) {
                 try {
                     pStmt.close();
                 }
                 catch (SQLException e) {
                     // ignore
                 }
             }
         }
         return ddls;
     }
 
     protected void resolvedTypes(Connection conn, String schemaPattern, DDLParser parser,
         List<UnresolvedType> unresolvedTypes, DatabaseType databaseType) throws ParseException {
         // fix up the databaseType's object-graph
         Stack<UnresolvedType> stac = new Stack<UnresolvedType>();
         for (UnresolvedType uType : unresolvedTypes) {
             stac.push(uType);
         }
         boolean done = false;
         DatabaseTypesRepository typesRepository = parser.getTypesRepository();
         while (!done) {
             CompositeDatabaseType resolvedType = null;
             UnresolvedType uType = stac.pop();
             String typeName = uType.getTypeName();
             int rowtypeIdx = typeName.lastIndexOf(ROWTYPE_MACRO);
             if (rowtypeIdx != -1) {
                 resolvedType = (CompositeDatabaseType)typesRepository.getDatabaseType(typeName);
                 if (resolvedType == null) {
                     String tableName = typeName.substring(0, rowtypeIdx);
                     int dotIdx = tableName.lastIndexOf('.');
                     if (dotIdx == -1) {
                         CompositeDatabaseType rowtypeCompositeType =
                             (CompositeDatabaseType)typesRepository.getDatabaseType(tableName);
                         if (rowtypeCompositeType == null) {
                             List<TableType> tables = buildTables(conn, null, tableName, false);
                             if (tables != null && tables.size() > 0) {
                                 rowtypeCompositeType = tables.get(0);
                                 typesRepository.setDatabaseType(rowtypeCompositeType.getTypeName(),
                                     rowtypeCompositeType);
                             }
                             ROWTYPEType rType = new ROWTYPEType(typeName);
                             rType.addCompositeType(rowtypeCompositeType);
                             uType.getOwningType().addCompositeType(rType);
                             typesRepository.setDatabaseType(typeName, rType);
                         }
                         else {
                             uType.getOwningType().addCompositeType(rowtypeCompositeType);
                         }
                         //always a chance that tableType has some unresolved columns
                         if (!rowtypeCompositeType.isResolved()) {
                             UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
                             unresolvedTypesVisitor.visit(rowtypeCompositeType);
                             for (UnresolvedType u2Type : unresolvedTypesVisitor.getUnresolvedTypes()) {
                                 if (!stac.contains(u2Type)) {
                                     stac.push(u2Type);
                                 }
                             }
                         }
                     }
                     else {
                         //TODO - table is in a different schema
                     }
                 }
                 else {
                     uType.getOwningType().addCompositeType(resolvedType);
                 }
             }
             else {
                 int typeIdx = typeName.lastIndexOf(TYPE_MACRO);
                 if (typeIdx != -1) {
                     String tableAndColumnName = typeName.substring(0, typeIdx);
                     int dotIdx = tableAndColumnName.lastIndexOf('.');
                     String tableName = tableAndColumnName.substring(0, dotIdx);
                     String columnName = tableAndColumnName.substring(dotIdx+1,
                         tableAndColumnName.length());
                     resolvedType = (CompositeDatabaseType)typesRepository.getDatabaseType(typeName);
                     if (resolvedType == null) {
                         TableType tableType = (TableType)typesRepository.getDatabaseType(tableName);
                         if (tableType == null) {
                             List<TableType> tables = buildTables(conn, null, tableName, false);
                             if (tables != null && tables.size() > 0) {
                                 tableType = tables.get(0);
                                 typesRepository.setDatabaseType(tableName, tableType);
                             }
                         }
                         TYPEType tType = new TYPEType(typeName);
                         for (Iterator<FieldType> i = tableType.getColumns().iterator();
                             i.hasNext();) {
                             FieldType field = i.next();
                             if (columnName.equals(field.getFieldName())) {
                                 tType.addCompositeType(field.getDataType());
                                 break;
                             }
                         }
                         typesRepository.setDatabaseType(typeName, tType);
                         resolvedType = tType;
                     }
                     if (uType.getOwningType() instanceof PLSQLRecordType) {
                         PLSQLRecordType uRecordType = (PLSQLRecordType)uType.getOwningType();
                         FieldType field = null;
                         for (Iterator<FieldType> i = uRecordType.getFields().iterator();
                             i.hasNext();) {
                             field = i.next();
                             if (columnName.equals(field.getFieldName())) {
                                 field.addCompositeType(resolvedType);
                                 break;
                             }
                         }
                     }
                     else {
                         uType.getOwningType().addCompositeType(resolvedType);
                     }
                 }
                 else {
                     String objectTypeName = typeName;
                     String schema = schemaPattern;
                     int dotIdx = typeName.lastIndexOf('.');
                     if (dotIdx != -1) {
                         schema = typeName.substring(0, dotIdx);
                         objectTypeName = typeName.substring(dotIdx + 1, typeName.length());
                     }
                     resolvedType = (CompositeDatabaseType)typesRepository.getDatabaseType(objectTypeName);
                     if (resolvedType == null) {
                         int objectTypeCode = getObjectType(conn, schema, objectTypeName);
                         switch (objectTypeCode) {
                             case FUNCTION_CODE :
                                 List<FunctionType> functions = buildFunctions(conn, schema,
                                     objectTypeName, false);
                                 if (functions != null && functions.size() > 0) {
                                     resolvedType = functions.get(0); // only care about first one
                                 }
                                 break;
                             case PACKAGE_CODE :
                                 List<PLSQLPackageType> packages = buildPackages(conn, schema,
                                     objectTypeName, false);
                                 if (packages != null && packages.size() > 0) {
                                     resolvedType = packages.get(0); // only care about first one
                                 }
                                 break;
                             case PROCEDURE_CODE :
                                 List<ProcedureType> procedures = buildProcedures(conn, schema,
                                     objectTypeName, false);
                                 if (procedures != null && procedures.size() > 0) {
                                     resolvedType = procedures.get(0); // only care about first one
                                 }
                                 break;
                             case TABLE_CODE :
                                 List<TableType> tables = buildTables(conn, schema,
                                     objectTypeName, false);
                                 if (tables != null && tables.size() > 0) {
                                     resolvedType = tables.get(0); // only care about first one
                                 }
                                 break;
                             case TYPE_CODE :
                                 List<CompositeDatabaseType> types = buildTypes(conn, schema,
                                     objectTypeName, false);
                                 if (types != null && types.size() > 0) {
                                     resolvedType = types.get(0); // only care about first one
                                 }
                                 break;
                             case UNKNOWN_CODE :
                             default :
                                 break;
                         }
                     }
                     if (resolvedType != null) {
                         uType.getOwningType().addCompositeType(resolvedType);
                         typesRepository.setDatabaseType(objectTypeName, resolvedType);
                         //always a chance that resolvedType refers to something that is un-resolved
                         if (!resolvedType.isResolved()) {
                             UnresolvedTypesVisitor unresolvedTypesVisitor = new UnresolvedTypesVisitor();
                             unresolvedTypesVisitor.visit(resolvedType);
                             for (UnresolvedType u2Type : unresolvedTypesVisitor.getUnresolvedTypes()) {
                                 if (!stac.contains(u2Type)) {
                                     stac.push(u2Type);
                                 }
                             }
                         }
                     }
                 }
             }
             if (stac.isEmpty()) {
                 done = true;
             }
         }
     }
 
     protected int getObjectType(Connection conn, String schema,  String typeName) {
         int objectType = -1;
         String schemaPattern = schema == null ? "%" : schema;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try {
             pStmt = conn.prepareStatement(GET_OBJECT_TYPE_STMT);
             pStmt.setString(1, schemaPattern);
             pStmt.setString(2, typeName);
             boolean worked = pStmt.execute();
             if (worked) {
                 rs = pStmt.getResultSet();
                 rs.next();
                 objectType = rs.getInt(ALL_OBJECTS_OBJECT_TYPE_FIELD);
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         finally {
             if (rs != null) {
                 try {
                     rs.close();
                 }
                 catch (SQLException e) {
                     // ignore
                 }
             }
             if (pStmt != null) {
                 try {
                     pStmt.close();
                 }
                 catch (SQLException e) {
                     // ignore
                 }
             }
         }
         return objectType;
     }
 
     public Properties getTransformProperties() throws DatabaseTypeBuilderException  {
         if (TRANSFORMS_FACTORY == null) {
             throw DatabaseTypeBuilderException.noTransformsFactories();
         }
         Properties transformProperties = TRANSFORMS_FACTORY.getTransformProperties();
         if (transformProperties == null) {
             throw DatabaseTypeBuilderException.noTransformsProperties();
         }
         return transformProperties;
     }
 
     protected boolean setDbmsMetadataSessionTransforms(Connection conn) {
         if (transformsSet) {
             return true;
         }
         boolean worked = true;
         CallableStatement cStmt = null;
         try {
             Properties transformProperties = getTransformProperties();
             StringBuilder sb = new StringBuilder("BEGIN");
             for (Map.Entry<Object, Object> me : transformProperties.entrySet()) {
                 sb.append("\n");
                 sb.append("DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'");
                 sb.append(me.getKey());
                 sb.append("',");
                 sb.append(me.getValue());
                 sb.append(");");
             }
             sb.append("\nEND;");
             cStmt = conn.prepareCall(sb.toString());
             cStmt.execute();
         }
         catch (Exception e) {
            worked = false;
         }
         finally {
             try {
                 cStmt.close();
             }
             catch (SQLException e) {
                 // ignore
             }
         }
         if (worked) {
             transformsSet = true;
         }
         return worked;
     }
 
     static boolean schemaPatternExcludesAdminSchemas(String schemaPattern) {
         return (schemaPattern == null || schemaPattern.length() == 0 ||
             TOPLEVEL.equals(schemaPattern) || "%".equals(schemaPattern));
     }
 }
