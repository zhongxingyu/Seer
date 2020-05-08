 /*
  * Copyright 2012 Michael Bischoff
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.jpaw.persistence.dsl.generator.sql;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import de.jpaw.bonaparte.dsl.bonScript.FieldDefinition;
 import de.jpaw.bonaparte.dsl.generator.DataTypeExtension;
 import de.jpaw.bonaparte.dsl.generator.XUtil;
 import de.jpaw.persistence.dsl.bDDL.ElementCollectionRelationship;
 import de.jpaw.persistence.dsl.generator.YUtil;
 
 // mapping of database vendor specific information
 
 public class SqlMapping {
     // a lookup to determine the database vendor-specific data type to use for a given grammar type.
     // (LANGUAGE / DATABASE VENDOR SPECIFIC: SQL Oracle)
     static protected Map<String,String> dataTypeSqlOracle = new HashMap<String, String>(32);
     static {  // see http://docs.oracle.com/cd/E11882_01/server.112/e26088/sql_elements001.htm#i45441 for reference
         // we avoid the ANSI data types for Oracle, because I think the native ones have better performance
         dataTypeSqlOracle.put("boolean",   "number(1)");                // Oracle has no boolean type
         dataTypeSqlOracle.put("int",       "number(10)");               // no specific type available for Oracle
         dataTypeSqlOracle.put("integer",   "number(10)");               // no specific type available for Oracle
         dataTypeSqlOracle.put("long",      "number(20)");               // no specific type available for Oracle
         dataTypeSqlOracle.put("float",     "binary_float");
         dataTypeSqlOracle.put("double",    "binary_double");
         dataTypeSqlOracle.put("number",    "number(#length)");
         dataTypeSqlOracle.put("decimal",   "number(#length,#precision)");
         dataTypeSqlOracle.put("byte",      "number(3)");
         dataTypeSqlOracle.put("short",     "number(5)");
         dataTypeSqlOracle.put("char",      "varchar2(1 char)");
         dataTypeSqlOracle.put("character", "varchar2(1 char)");
 
         dataTypeSqlOracle.put("uuid",      "raw(16)");                      // not yet supported by grammar!
         dataTypeSqlOracle.put("binary",    "raw(#length)");                 // only up to 2000 bytes, use BLOB if more!
         dataTypeSqlOracle.put("raw",       "raw(#length)");                 // only up to 2000 bytes, use BLOB if more!
         dataTypeSqlOracle.put("day",       "date");                         // Oracle has no day without a time field
         dataTypeSqlOracle.put("timestamp", "timestamp(#length)");           // timestamp(0) should become DATE
         dataTypeSqlOracle.put("calendar",  "timestamp(#length)");           // timestamp(0) should become DATE
 
         dataTypeSqlOracle.put("uppercase", "varchar2(#length)");            // only up to 4000 characters, use CLOB if more!
         dataTypeSqlOracle.put("lowercase", "varchar2(#length)");            // only up to 4000 characters, use CLOB if more!
         dataTypeSqlOracle.put("ascii",     "varchar2(#length)");            // only up to 4000 characters, use CLOB if more!
         dataTypeSqlOracle.put("unicode",   "varchar2(#length char)");       // only up to 4000 characters, use CLOB if more!
         dataTypeSqlOracle.put("enum",      "number(4)");                    // mapping to numeric or varchar is done by entity class getter/setter
         dataTypeSqlOracle.put("object",    "blob");                         // serialized form of an object
         dataTypeSqlOracle.put("string",    "varchar2(#length)");            // only up to 4000 characters, use CLOB if more!
     }
     static protected Map<String,String> dataTypeSqlPostgres = new HashMap<String, String>(32);
     static { // see http://www.postgresql.org/docs/9.1/static/datatype.html for reference
         dataTypeSqlPostgres.put("boolean",   "boolean");
         dataTypeSqlPostgres.put("int",       "integer");
         dataTypeSqlPostgres.put("integer",   "integer");
         dataTypeSqlPostgres.put("long",      "bigint");
         dataTypeSqlPostgres.put("float",     "real");
         dataTypeSqlPostgres.put("double",    "double precision");
         dataTypeSqlPostgres.put("number",    "numeric(#length)");           // numeric and decimal are equivalent in Postgres
         dataTypeSqlPostgres.put("decimal",   "decimal(#length,#precision)"); // numeric and decimal are equivalent in Postgres
         dataTypeSqlPostgres.put("byte",      "smallint");                   // there is no Postgres single byte numeric datatype
         dataTypeSqlPostgres.put("short",     "smallint");
         dataTypeSqlPostgres.put("char",      "char(1)");
         dataTypeSqlPostgres.put("character", "char(1)");
 
         dataTypeSqlPostgres.put("uuid",      "uuid");
         dataTypeSqlPostgres.put("binary",    "bytea");
         dataTypeSqlPostgres.put("raw",       "bytea");
         dataTypeSqlPostgres.put("day",       "date");
         dataTypeSqlPostgres.put("timestamp", "timestamp(#length)");
         dataTypeSqlPostgres.put("calendar",  "timestamp(#length)");
 
         dataTypeSqlPostgres.put("uppercase", "varchar(#length)");
         dataTypeSqlPostgres.put("lowercase", "varchar(#length)");
         dataTypeSqlPostgres.put("ascii",     "varchar(#length)");
         dataTypeSqlPostgres.put("unicode",   "varchar(#length)");
         dataTypeSqlPostgres.put("enum",      "smallint");
         dataTypeSqlPostgres.put("object",    "bytea");                      // mapping to numeric or varchar is done by entity class getter/setter
         dataTypeSqlPostgres.put("string",    "varchar(#length)");            // only up to 4000 characters, use CLOB if more!
     }
     static protected Map<String,String> dataTypeSqlMsSQLServer = new HashMap<String, String>(32);
     static { // see http://www.w3schools.com/sql/sql_datatypes.asp for reference
         dataTypeSqlMsSQLServer.put("boolean",   "bit");
         dataTypeSqlMsSQLServer.put("int",       "int");
         dataTypeSqlMsSQLServer.put("integer",   "int");
         dataTypeSqlMsSQLServer.put("long",      "bigint");
         dataTypeSqlMsSQLServer.put("float",     "float");
         dataTypeSqlMsSQLServer.put("double",    "double");
        dataTypeSqlMsSQLServer.put("number",    "int(#length)");
         dataTypeSqlMsSQLServer.put("decimal",   "decimal(#length,#precision)"); // numeric and decimal are equivalent in MS SQL server
         dataTypeSqlMsSQLServer.put("byte",      "tinyint");
         dataTypeSqlMsSQLServer.put("short",     "smallint");
         dataTypeSqlMsSQLServer.put("char",      "char(1)");
         dataTypeSqlMsSQLServer.put("character", "char(1)");
 
         dataTypeSqlMsSQLServer.put("uuid",      "varbinary(16)");
         dataTypeSqlMsSQLServer.put("binary",    "varbinary(#length)");
         dataTypeSqlMsSQLServer.put("raw",       "varbinary(#length)");
         dataTypeSqlMsSQLServer.put("day",       "date");
         dataTypeSqlMsSQLServer.put("timestamp", "datetime2(#length)");
         dataTypeSqlMsSQLServer.put("calendar",  "datetime2(#length)");
 
         dataTypeSqlMsSQLServer.put("uppercase", "varchar(#length)");
         dataTypeSqlMsSQLServer.put("lowercase", "varchar(#length)");
         dataTypeSqlMsSQLServer.put("ascii",     "varchar(#length)");
         dataTypeSqlMsSQLServer.put("unicode",   "nvarchar(#length)");
         dataTypeSqlMsSQLServer.put("enum",      "smallint");
         dataTypeSqlMsSQLServer.put("object",    "varbinary(16777200)");
         dataTypeSqlMsSQLServer.put("string",    "nvarchar(#length)");            // only up to 4000 characters, use CLOB if more!
     }
 
     static String sqlType(FieldDefinition c, DatabaseFlavour databaseFlavour) throws Exception {
         String datatype;
         DataTypeExtension ref;
         try {
             ref = DataTypeExtension.get(c.getDatatype());
         } catch (Exception ee) {
             throw new Exception("Cannot get datatype extension for fieldDefinition " + c.getName(), ee);
         }
         int columnLength;
         int columnDecimals;
         if (ref.objectDataType != null) {
             if (XUtil.hasProperty(c.getProperties(), "serialized")) {
                 String value = XUtil.getProperty(c.getProperties(), "serialized");
                 datatype = "raw";  // assume artificial ID
                 columnLength = value == null ? 2000 : Integer.valueOf(value);
                 columnDecimals = 0;
             } else {
                 datatype = "long";  // assume artificial ID
                 columnLength = 18;
                 columnDecimals = 0;
             }
         } else {
             datatype = ref.elementaryDataType.getName().toLowerCase();
             columnLength = ref.elementaryDataType.getLength();
             columnDecimals = ref.elementaryDataType.getDecimals();
         }
         if (ref.enumMaxTokenLength >= 0) {
             // alphanumeric enum! use other type!
             datatype = "unicode";
         }
         switch (databaseFlavour) {
         case ORACLE:
             datatype = dataTypeSqlOracle.get(datatype);
             if (columnLength > 2000) {
                 if (datatype.startsWith("raw")) {
                     datatype = "blob";
                 } else if ((columnLength > 4000) && datatype.startsWith("varchar2")) {
                     datatype = "clob";
                 }
             } else if ((columnLength == 0) && datatype.equals("timestamp(#length)")) {
                 datatype = "date";  // better performance, less memory consumption
             }
             if (ref.allTokensAscii && (ref.enumMaxTokenLength >= 0)) {
                 datatype = "varchar2(" + (ref.enumMaxTokenLength == 0 ? 1 : ref.enumMaxTokenLength) + ")";
             }
             break;
         case POSTGRES:
             datatype = dataTypeSqlPostgres.get(datatype);
             if (ref.allTokensAscii && (ref.enumMaxTokenLength >= 0)) {
                 datatype = "varchar(" + (ref.enumMaxTokenLength == 0 ? 1 : ref.enumMaxTokenLength) + ")";
             }
             break;
         case MSSQLSERVER:
             datatype = dataTypeSqlMsSQLServer.get(datatype);
             if (ref.allTokensAscii && (ref.enumMaxTokenLength >= 0)) {
                 datatype = "nvarchar(" + (ref.enumMaxTokenLength == 0 ? 1 : ref.enumMaxTokenLength) + ")";
             }
             break;
         }
         if (datatype == null)
             return "*** UNMAPPED data type for " + c.getName() + " in dialect " + databaseFlavour.toString() + " ***";
         
         //System.out.println("DEBUG: dataype = " + datatype + "(type " + c.getName() + ")");
         //System.out.println("DEBUG: length = " + Integer.valueOf(ref.elementaryDataType.getLength()).toString());
         //System.out.println("DEBUG: precision = " + Integer.valueOf(ref.elementaryDataType.getDecimals()).toString());
         if (ref.enumMaxTokenLength >= 0) {
             // special case for alphanumeric enums, again!
             return datatype.replace("#length",    Integer.valueOf(ref.enumMaxTokenLength).toString());
         }
        return datatype.replace("#length", Integer.valueOf(columnLength).toString()).replace("#precision", Integer.valueOf(columnDecimals).toString());
     }
 
     static boolean supportsTablespaces(DatabaseFlavour databaseFlavour) {
         switch (databaseFlavour) {
         case ORACLE:
             return true;
         case POSTGRES:
             return false;
         case MSSQLSERVER:
             return false;
         }
         return false;
     }
 
     static public String getCurrentUser(DatabaseFlavour databaseFlavour) {
         switch (databaseFlavour) {
         case ORACLE:
             return " DEFAULT SUBSTR(USER, 1, 8)";
         case POSTGRES:
             return " DEFAULT CURRENT_USER";
         case MSSQLSERVER:
             return " DEFAULT CURRENT_USER";
         }
         return "";
     }
 
     static public String getCurrentTimestamp(DatabaseFlavour databaseFlavour) {
         switch (databaseFlavour) {
         case ORACLE:
             return " DEFAULT SYSDATE";
         case POSTGRES:
             return " DEFAULT CURRENT_TIMESTAMP";
         case MSSQLSERVER:
             return " DEFAULT SYSUTCDATETIME()";
         }
         return "";
     }
 
     static public String getDefault(FieldDefinition c, DatabaseFlavour databaseFlavour, String value) throws Exception {
         DataTypeExtension ref = DataTypeExtension.get(c.getDatatype());
         if ((databaseFlavour == DatabaseFlavour.ORACLE  || databaseFlavour == DatabaseFlavour.MSSQLSERVER) && "Boolean".equals(ref.javaType)) {
             // Oracle does not know booleans, convert it to numeric!
             // MS SQL server uses BIT, which also takes 0 and 1
             if ("true".equals(value)) {
                 return " DEFAULT 1";
             } else if ("false".equals(value)) {
                 return " DEFAULT 0";
             } else {
                 return " DEFAULT " + value; // no mapping possible, maybe it just fits !? Otherwise it will generate an error!
             }
         } else if ("String".equals(ref.javaType)) {
             return " DEFAULT '" + YUtil.quoteSQL(value) + "'";
         } else {
             return " DEFAULT " + value;
         }
     }
     
     // for ElementCollections
     static public String sqlType(ElementCollectionRelationship ec, DatabaseFlavour databaseFlavour) {
         if (ec.getName().getIsMap() == null)
             return "*** NO MAP ***";
         String datatype = ec.getName().getIsMap().getIndexType().toLowerCase();
         switch (databaseFlavour) {
         case ORACLE:
             datatype = dataTypeSqlOracle.get(datatype);
             break;
         case POSTGRES:
             datatype = dataTypeSqlPostgres.get(datatype);
             break;
         case MSSQLSERVER:
             datatype = dataTypeSqlMsSQLServer.get(datatype);
             break;
         }
         return datatype.replace("#length", Integer.valueOf(ec.getMapKeySize() > 0 ? ec.getMapKeySize() : 255).toString());
     }
 }
