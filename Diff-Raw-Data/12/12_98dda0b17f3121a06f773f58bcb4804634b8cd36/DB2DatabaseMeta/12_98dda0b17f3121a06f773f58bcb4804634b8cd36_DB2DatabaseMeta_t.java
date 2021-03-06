 
 package be.ibridge.kettle.core.database;
 
 import be.ibridge.kettle.core.Const;
 import be.ibridge.kettle.core.value.Value;
 
 /**
  * Contains DB2 specific information through static final members 
  * 
  * @author Matt
  * @since  11-mrt-2005
  */
 public class DB2DatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
 {
 	/**
 	 * Construct a new database connections.  Note that not all these parameters are not allways mandatory.
 	 * 
 	 * @param name The database name
 	 * @param access The type of database access
 	 * @param host The hostname or IP address
 	 * @param db The database name
 	 * @param port The port on which the database listens.
 	 * @param user The username
 	 * @param pass The password
 	 */
 	public DB2DatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
 	{
 		super(name, access, host, db, port, user, pass);
 	}
 	
 	public DB2DatabaseMeta()
 	{
 	}
 	
 	public String getDatabaseTypeDesc()
 	{
 		return "DB2";
 	}
 
 	public String getDatabaseTypeDescLong()
 	{
 		return "IBM DB2";
 	}
 	
 	/**
 	 * @return Returns the databaseType.
 	 */
 	public int getDatabaseType()
 	{
 		return DatabaseMeta.TYPE_DATABASE_DB2;
 	}
 		
 	public int[] getAccessTypeList()
 	{
 		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC };
 	}
 	
 	public int getDefaultDatabasePort()
 	{
 		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 50000;
 		return -1;
 	}
 	
 	public boolean supportsSetCharacterStream()
 	{
 		return false;
 	}
 	
 	public String getDriverClass()
 	{
 		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
 		{
 			return "sun.jdbc.odbc.JdbcOdbcDriver";
 		}
 		else
 		{
 			return "com.ibm.db2.jcc.DB2Driver";
 		}
 	}
 
 	public String getURL()
 	{
 		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
 		{
 			return "jdbc:odbc:"+getDatabaseName();
 		}
 		else
 		{
 			return "jdbc:db2://"+getHostname()+":"+getDatabasePortNumberString()+"/"+getDatabaseName();
 		}
 	}
 
 	/**
 	 * @return true if the database supports schemas
 	 */
 	public boolean supportsSchemas()
 	{
 		return false;
 	}
 	
 	/**
 	 * @param tableName The table to be truncated.
 	 * @return The SQL statement to truncate a table: remove all rows from it without a transaction
 	 */
 	public String getTruncateTableStatement(String tableName)
 	{
 	    return "ALTER TABLE "+tableName+" ACTIVATE NOT LOGGED INITIALLY WITH EMPTY TABLE";
 	}
 
 	
 	/**
 	 * Generates the SQL statement to add a column to the specified table
 	 * @param tablename The table to add
 	 * @param v The column defined as a value
 	 * @param tk the name of the technical key field
 	 * @param use_autoinc whether or not this field uses auto increment
 	 * @param pk the name of the primary key field
 	 * @param semicolon whether or not to add a semi-colon behind the statement.
 	 * @return the SQL statement to add a column to the specified table
 	 */
 	public String getAddColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
 	{
 		return "ALTER TABLE "+tablename+" ADD COLUMN "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
 	}
 
 	/**
 	 * Generates the SQL statement to drop a column from the specified table
 	 * @param tablename The table to add
 	 * @param v The column defined as a value
 	 * @param tk the name of the technical key field
 	 * @param use_autoinc whether or not this field uses auto increment
 	 * @param pk the name of the primary key field
 	 * @param semicolon whether or not to add a semi-colon behind the statement.
 	 * @return the SQL statement to drop a column from the specified table
 	 */
 	public String getDropColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
 	{
 		return "ALTER TABLE "+tablename+" DROP COLUMN "+v.getName()+Const.CR;
 	}
 
 	/**
 	 * Generates the SQL statement to modify a column in the specified table
 	 * @param tablename The table to add
 	 * @param v The column defined as a value
 	 * @param tk the name of the technical key field
 	 * @param use_autoinc whether or not this field uses auto increment
 	 * @param pk the name of the primary key field
 	 * @param semicolon whether or not to add a semi-colon behind the statement.
 	 * @return the SQL statement to modify a column in the specified table
 	 */
 	public String getModifyColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
 	{
 		String retval="";
 		retval+="ALTER TABLE "+tablename+" DROP COLUMN "+v.getName()+Const.CR+";"+Const.CR;
 		retval+="ALTER TABLE "+tablename+" ADD COLUMN "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
 		return retval;
 	}
 
 	public String getFieldDefinition(Value v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
 	{
 		String retval="";
 		
 		String fieldname = v.getName();
 		int    length    = v.getLength();
 		int    precision = v.getPrecision();
 		
 		if (add_fieldname) retval+=fieldname+" ";
 		
 		int type         = v.getType();
 		switch(type)
 		{
 		case Value.VALUE_TYPE_DATE   : retval+="TIMESTAMP"; break;
 		case Value.VALUE_TYPE_BOOLEAN: retval+="CHARACTER(1)"; break;
 		case Value.VALUE_TYPE_NUMBER :
 		case Value.VALUE_TYPE_INTEGER:
         case Value.VALUE_TYPE_BIGNUMBER: 
 			if (fieldname.equalsIgnoreCase(tk) && use_autoinc) // Technical key: auto increment field!
 			{
 				retval+="BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NOCACHE)";
 			} 
 			else
 			{
 				if (length>0)
 				{
 					retval+="DECIMAL("+length;
 					if (precision>0)
 					{
 						retval+=", "+precision;
 					}
 					retval+=")";
 				}
 				else
 				{
 					retval+="FLOAT";
 				}
 			}
 			break;
 		case Value.VALUE_TYPE_STRING:
 			if (length>getMaxVARCHARLength() || length>=DatabaseMeta.CLOB_LENGTH)
 			{
 				retval+="CLOB";
 			}
 			else
 			{
 				retval+="VARCHAR"; 
 				if (length>0)
 				{
 					retval+="("+length;
 				}
 				else
 				{
 					retval+="("; // Maybe use some default DB String length?
 				}
 				retval+=")";

 			}
            break;
       case Value.VALUE_TYPE_BINARY:
 			if (length>getMaxVARCHARLength() || length>=DatabaseMeta.CLOB_LENGTH)
 			{
 				retval+="BLOB("+length+")";
 			}
 			else
 			{
 				if (length>0)
 				{	
 				    retval+="CHAR("+length+") FOR BIT DATA";
 				}
 				else
 				{
 					retval+="BLOB"; // not going to work, but very close
 				}
 			}			
            break;
        default:
 			retval+=" UNKNOWN";
 			break;
 		}
 
 		if (add_cr) retval+=Const.CR;
 		
 		return retval;
 	}
 
 	/* (non-Javadoc)
 	 * @see be.ibridge.kettle.core.database.DatabaseInterface#getReservedWords()
 	 */
 	public String[] getReservedWords()
 	{
 		return new String[]
 		{
 			//http://publib.boulder.ibm.com/infocenter/db2luw/v8/index.jsp?topic=/com.ibm.db2.udb.doc/admin/r0001095.htm
 			//For portability across the DB2 Universal Database products, the following should be considered reserved words.
 			//The following list also contains the ISO/ANSI SQL99 reserved words for future compatibility.
 			"ABSOLUTE", "ACTION", "ADD", "ADMIN", "AFTER", "AGGREGATE", "ALIAS", "ALL", "ALLOCATE", "ALLOW", "ALTER", 
 			"AND", "ANY", "APPLICATION", "ARE", "ARRAY", "AS", "ASC", "ASSERTION", "ASSOCIATE", "ASUTIME", "AT", 
 			"AUDIT", "AUTHORIZATION", "AUX", "AUXILIARY", 
 			"BEFORE", "BEGIN", "BETWEEN", "BINARY", "BIT", "BLOB", "BOOLEAN", "BOTH", "BREADTH", "BUFFERPOOL", "BY", 
 			"CACHE", "CALL", "CALLED", "CAPTURE", "CARDINALITY", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", 
 			"CCSID", "CHAR", "CHARACTER", "CHECK", "CLASS", "CLOB", "CLOSE", "CLUSTER", "COLLATE", "COLLATION", 
 			"COLLECTION", "COLLID", "COLUMN", "COMMENT", "COMMIT", "COMPLETION", "CONCAT", "CONDITION", "CONNECT", 
 			"CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR", "CONTAINS", "CONTINUE", "CORRESPONDING", 
 			"COUNT", "COUNT_BIG", "CREATE", "CROSS", "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_LC_CTYPE", 
 			"CURRENT_PATH", "CURRENT_ROLE", "CURRENT_SERVER", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TIMEZONE", 
 			"CURRENT_USER", "CURSOR", "CYCLE", 
 			"DATA", "DATABASE", "DATE", "DAY", "DAYS", "DB2GENERAL", "DB2GENRL", "DB2SQL", "DBINFO", "DEALLOCATE", 
 			"DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFAULTS", "DEFERRABLE", "DEFERRED", "DEFINITION", "DELETE", 
 			"DEPTH", "DEREF", "DESC", "DESCRIBE", "DESCRIPTOR", "DESTROY", "DESTRUCTOR", "DETERMINISTIC", 
 			"DIAGNOSTICS", "DICTIONARY", "DISALLOW", "DISCONNECT", "DISTINCT", "DO", "DOMAIN", "DOUBLE", "DROP", 
 			"DSNHATTR", "DSSIZE", "DYNAMIC", 
 			"EACH", "EDITPROC", "ELSE", "ELSEIF", "ENCODING", "END", "END-EXEC", "END-EXEC1", "EQUALS", "ERASE", 
 			"ESCAPE", "EVERY", "EXCEPT", "EXCEPTION", "EXCLUDING", "EXEC", "EXECUTE", "EXISTS", "EXIT", "EXTERNAL", 
 			"FALSE", "FENCED", "FETCH", "FIELDPROC", "FILE", "FINAL", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", 
 			"FREE", "FROM", "FULL", "FUNCTION", 
 			"GENERAL", "GENERATED", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GRAPHIC", "GROUP", "GROUPING", 
 			"HANDLER", "HAVING", "HOLD", "HOST", "HOUR", "HOURS", 
 			"IDENTITY", "IF", "IGNORE", "IMMEDIATE", "IN", "INCLUDING", "INCREMENT", "INDEX", "INDICATOR", "INHERIT", 
 			"INITIALIZE", "INITIALLY", "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", 
 			"INTEGRITY", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOBID", "ISOLATION", "ITERATE", 
 			"JAR", "JAVA", "JOIN", 
 			"KEY", 
 			"LABEL", "LANGUAGE", "LARGE", "LAST", "LATERAL", "LC_CTYPE", "LEADING", "LEAVE", "LEFT", "LESS", "LEVEL", 
 			"LIKE", "LIMIT", "LINKTYPE", "LOCAL", "LOCALE", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", "LOCATORS", 
 			"LOCK", "LOCKMAX", "LOCKSIZE", "LONG", "LOOP", 
 			"MAP", "MATCH", "MAXVALUE", "MICROSECOND", "MICROSECONDS", "MINUTE", "MINUTES", "MINVALUE", "MODE", 
 			"MODIFIES", "MODIFY", "MODULE", "MONTH", "MONTHS", 
 			"NAMES", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NEW_TABLE", "NEXT", "NO", "NOCACHE", "NOCYCLE", 
 			"NODENAME", "NODENUMBER", "NOMAXVALUE", "NOMINVALUE", "NONE", "NOORDER", "NOT", "NULL", "NULLS", "NUMERIC", 
 			"NUMPARTS", 
 			"OBID", "OBJECT", "OF", "OFF", "OLD", "OLD_TABLE", "ON", "ONLY", "OPEN", "OPERATION", "OPTIMIZATION", 
 			"OPTIMIZE", "OPTION", "OR", "ORDER", "ORDINALITY", "OUT", "OUTER", "OUTPUT", "OVERRIDING", 
 			"PACKAGE", "PAD", "PARAMETER", "PARAMETERS", "PART", "PARTIAL", "PARTITION", "PATH", "PIECESIZE", "PLAN", 
 			"POSITION", "POSTFIX", "PRECISION", "PREFIX", "PREORDER", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", 
 			"PRIQTY", "PRIVILEGES", "PROCEDURE", "PROGRAM", "PSID", "PUBLIC", 
 			"QUERYNO", 
 			"READ", "READS", "REAL", "RECOVERY", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "RELATIVE", 
 			"RELEASE", "RENAME", "REPEAT", "RESET", "RESIGNAL", "RESTART", "RESTRICT", "RESULT", 
 			"RESULT_SET_LOCATOR", "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLE", "ROLLBACK", "ROLLUP", "ROUTINE", 
 			"ROW", "ROWS", "RRN", "RUN", 
 			"SAVEPOINT", "SCHEMA", "SCOPE", "SCRATCHPAD", "SCROLL", "SEARCH", "SECOND", "SECONDS", "SECQTY", 
 			"SECTION", "SECURITY", "SELECT", "SENSITIVE", "SEQUENCE", "SESSION", "SESSION_USER", "SET", "SETS", 
 			"SIGNAL", "SIMPLE", "SIZE", "SMALLINT", "SOME", "SOURCE", "SPACE", "SPECIFIC", "SPECIFICTYPE", "SQL", 
 			"SQLEXCEPTION", "SQLID", "SQLSTATE", "SQLWARNING", "STANDARD", "START", "STATE", "STATEMENT", "STATIC", 
 			"STAY", "STOGROUP", "STORES", "STRUCTURE", "STYLE", "SUBPAGES", "SUBSTRING", "SYNONYM", "SYSFUN", "SYSIBM", 
 			"SYSPROC", "SYSTEM", "SYSTEM_USER", 
 			"TABLE", "TABLESPACE", "TEMPORARY", "TERMINATE", "THAN", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", 
 			"TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION", "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRUE", "TYPE", 
 			"UNDER", "UNDO", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UNTIL", "UPDATE", "USAGE", "USER", "USING", 
 			"VALIDPROC", "VALUE", "VALUES", "VARCHAR", "VARIABLE", "VARIANT", "VARYING", "VCAT", "VIEW", "VOLUMES", 
 			"WHEN", "WHENEVER", "WHERE", "WHILE", "WITH", "WITHOUT", "WLM", "WORK", "WRITE", 
 			"YEAR", "YEARS", 
 			"ZONE"				
         };
 	}
 
     public String getSQLLockTables(String tableNames[])
     {
         String sql="";
         for (int i=0;i<tableNames.length;i++)
         {
             sql+="LOCK TABLE "+tableNames[i]+" IN SHARE MODE;"+Const.CR;
         }
         return sql;
     }
 
     public String getSQLUnlockTables(String tableName[])
     {
         return null; // lock release on commit point.
     }
 
 	/**
 	 * Get the maximum length of a text field (VARCHAR) for this database connection.
 	 * If this size is exceeded use a CLOB.
 	 * @return The maximum VARCHAR field length for this database type. (mostly identical to getMaxTextFieldLength() - CLOB_LENGTH)
 	 */
 	public int getMaxVARCHARLength()
 	{
 		return 32672;
 	}
 	
     public boolean supportsBatchUpdates()
     {
     	// DB2 support batches but big decimals and binary data is
     	// broken, so for the moment batches are not done in DB2.
         return false;
     }
     
     /**
      * @return false because the DB2 JDBC driver doesn't support getBlob on the resultset.  We must use getBytes() to get the data.
      */
     public boolean supportsGetBlob()
     {
         return false;
     }
 }
 
