 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2008 Brett Wilson.
  * All rights reserved.
  */
 
 package org.wwscc.storage;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  */
 public class SqliteDatabase extends SQLDataInterface
 {
 	private static final Logger log = Logger.getLogger(SqliteDatabase.class.getCanonicalName());
 
 	public static final int SQLITE_OK		= 0;
 	public static final int SQLITE_BUSY		= 5;
 	public static final int SQLITE_ROW		= 100;
 	public static final int SQLITE_DONE		= 101;
 	
 	static 
 	{
 		System.loadLibrary("sqliteintf");
 		log.log(Level.INFO, "Sqlite version {0}", libversion());
 	}
 
 	final class Prepared
 	{
 		long pointer;
 		int paramcount;
 		int colcount;
 		Class<?> coltype[];
 		String colname[];
 		String sql;
 
 		public Prepared(String inSql) throws IOException
 		{
 			sql = inSql;
 			paramcount = 0;
 			colcount = 0;
 
 			pointer = prepare(dbptr, inSql);
 			paramcount = bind_parameter_count(pointer);
 			colcount = column_count(pointer);
 			coltype = new Class[colcount];
 			colname = new String[colcount];
 			
 			for (int ii = 0; ii < colcount; ii++)
 			{
 				colname[ii] = column_name(pointer, ii);
 				setColType(ii, column_decltype(pointer, ii));
 			}
 		}
 
 		public void resetStatement() throws IOException
 		{
 			clear_bindings(pointer);
 			if (reset(pointer) != SQLITE_OK)
 				throw new IOException(errmsg(dbptr));
 		}
 
 		public int rowChanges()
 		{
 			return changes(pointer);
 		}
 		
 		public void setColType(int col, String type) throws IOException
 		{
 			if (type == null)
 			{
 				coltype[col] = String.class;
 				return;
 			}
 			
 			String lower = type.toLowerCase();
 			
 			if (lower.contains("char") || lower.contains("string"))
 				coltype[col] = String.class;
 			else if (lower.contains("int"))
 				coltype[col] = Integer.class;
 			else if (lower.contains("double") || lower.contains("float"))
 				coltype[col] = Double.class;
 			else if (lower.contains("time") || lower.contains("date"))
 				coltype[col] = String.class;
 			else if (lower.contains("boolean"))
 				coltype[col] = Boolean.class;
 			else if (lower.contains("binary") || lower.contains("blob"))
 				coltype[col] = byte[].class;
 			else
 				throw new IOException("Don't know how to convert type " + lower);
 		}
 
 		@Override
 		public String toString() 
 		{ 
 			return "Prepared: " + sql; 
 		}
 	}
 
 		
 	File file;
 	long dbptr;
 	HashMap<String, Prepared> map;
 	Prepared begin, commit, rollback;
 
 	public SqliteDatabase(File f) throws IOException
 	{
 		log.log(Level.INFO, "Opening local database file {0}", f);
 		if (f == null)
 			throw new IOException("File is null");
 
 		file = f;
 		dbptr = open(f.getCanonicalPath(), false, 5000);
 		String dbname = f.getName();
 		seriesName = dbname.substring(0, dbname.lastIndexOf('.'));
 		host = "127.0.0.1";
 
 		map = new HashMap<String, Prepared>();
 		begin = new Prepared("begin");
 		commit = new Prepared("commit");
 		rollback = new Prepared("rollback");
 
 		String ver = getSetting("schema");
		if (!ver.equals("20132"))
			throw new IOException("Database schema version is " + ver + " but software is 20132");
 	}
 
 	@Override
 	public void close()
 	{
 		try	{
 			close(dbptr);
 		} catch (IOException ioe) {
 			log.log(Level.SEVERE, "Problems closing sqlite file: {0}", ioe);
 		}
 		dbptr = 0;
 	}
 
 	@Override
 	public void start() throws IOException
 	{
 		log.fine("start");
 		execute(begin, null);
 	}
 
 	@Override
 	public void commit() throws IOException
 	{
 		log.fine("commit");
 		execute(commit, null);
 	}
 
 	@Override
 	public void rollback()
 	{
 		log.fine("rollback");
 		try
 		{
 			execute(rollback, null);
 		}
 		catch (IOException ioe)
 		{
 			log.log(Level.SEVERE, "Failed to rollback: " + ioe, ioe);
 		}
 	}
 
 	@Override
 	public int lastInsertId() throws IOException
 	{
 		return (int)lastInsertId(dbptr);
 	}
 
 	private Prepared getPrepared(String key) throws IOException
 	{
 		Prepared p = map.get(key);
 		if (p == null)
 		{
 			p = new Prepared(SQLMap.get(key));
 			map.put(key, p);
 		}
 		return p;
 	}
 
 	@Override
 	public void executeGroupUpdate(String key, List<List<Object>> lists) throws IOException
 	{
 		Prepared p = getPrepared(key);
 		for (List<Object> args : lists)
 			execute(p, args);
 	}
 
 	@Override
 	public void executeUpdate(String key, List<Object> args) throws IOException
 	{
 		Prepared p = getPrepared(key);
 		execute(p, args);
 	}
 
 	@Override
 	public ResultData executeSelect(String key, List<Object> args) throws IOException
 	{
 		Prepared p = getPrepared(key);
 		return (ResultData)execute(p, args);
 	}
 
 
 	void bindParam(Prepared p, List<Object> args) throws IOException
 	{
 		for (int ii = 0; ii < args.size(); ii++)
 		{
 			Object v = args.get(ii);
 			int ret = -1;
 			
 			if (v == null) {
 				ret = bind_null(p.pointer, ii+1);
 			} else if (v instanceof Integer) {
 				ret = bind_int(p.pointer, ii+1, ((Integer)v).intValue());
 			} else if (v instanceof Long) {
 				ret = bind_long(p.pointer, ii+1, ((Long)v).longValue());
 			} else if (v instanceof Double) {
 				ret = bind_double(p.pointer, ii+1, ((Double)v).doubleValue());
 			} else if (v instanceof String) {
 				ret = bind_text(p.pointer, ii+1, (String)v);
 			} else if (v instanceof Boolean) {
 				ret = bind_int(p.pointer, ii+1, ((Boolean)v) ? 1:0);
 			} else if (v instanceof byte[]) {
 				ret = bind_blob(p.pointer, ii+1, (byte[])v);
 			} else if (v instanceof SADateTime) {
 				ret = bind_text(p.pointer, ii+1, v.toString());
 			} else {
 				throw new IOException("unexpected param type: " + v.getClass());
 			}
 
 			if (ret != SQLITE_OK)
 				throw new IOException(errmsg(dbptr));
 		}
     }
 
 
 	ResultRow loadRow(Prepared p) throws IOException
 	{
 		ResultRow row = new ResultRow();
 		for (int ii = 0; ii < p.colcount; ii++)
 		{
 			Class<?> type = p.coltype[ii];
 			String name = p.colname[ii];
 
 			if (type == Integer.class) {
 				row.put(name, column_int(p.pointer, ii));
 			} else if (type == Long.class) {
 				row.put(name, column_long(p.pointer, ii));
 			} else if (type == Double.class) {
 				row.put(name, column_double(p.pointer, ii));
 			} else if (type == Boolean.class) {
 				row.put(name, (column_int(p.pointer, ii)!=0));
 			} else if (type == String.class) {
 				row.put(name, column_text(p.pointer, ii));
 			} else if (type == byte[].class) {
 				row.put(name, column_blob(p.pointer, ii));
 			} else {
 				throw new IOException("unexpected result type: " + type);
 			}
 		}
 		
 		return row;
 	}
 
 
 	synchronized Object execute(Prepared p, List<Object> args) throws IOException
 	{
 		ResultData retval = null;
 		
 		if ((args != null) && (p.paramcount != args.size()))
 			throw new IOException("param count ("+p.paramcount+") != value count ("+args.size()+")");
 		else if ((args == null) && (p.paramcount != 0))
 			throw new IOException("param count ("+p.paramcount+") != 0 and no args provided");
 
 		if (args != null)
 			bindParam(p, args);
 
 		if (p.colcount > 0) // expecting returned data, i.e. SELECT
 			retval = new ResultData();
 
 		while (true)
 		{
 			int stepval = step(p.pointer);
 			switch (stepval)
 			{
 				case SQLITE_DONE:
 					p.resetStatement();
 					if (retval == null)
 						return 0;
 					return retval;
 
 				case SQLITE_ROW:
 					if (retval == null)
 						throw new IOException("Got data from database and I wasn't expecting it");
 					retval.add(loadRow(p));
 					break;
 					
 				default:
 					p.resetStatement();
 					//break;
 					throw new IOException(errmsg(dbptr));
 			}
 		}
 	}
 
 
 	/**
 	 * Custom impl for each SQL source as attr is used in areas where SQL values are not allowed
 	 * @param attr the attribute the look for
 	 * @return a list of unique attributes for the car
 	 * @throws IOException 
 	 */
 	@Override
 	protected ResultData getCarAttributesImpl(String attr) throws IOException
 	{
 		String key = "GETCARATTRIBUTES_" + attr;
 		Prepared p = map.get(key);
 		if (p == null)
 		{
 			p = new Prepared("select distinct "+attr+" from cars " +
 						"where LOWER("+attr+")!="+attr+" and UPPER("+attr+")!="+attr+
 						" order by "+attr+" collate nocase");
 			map.put(key, p);
 		}
 
 		return (ResultData)execute(p, null);
 	}
 
 
 
 	/**
 	 * Native calls into sqlite
 	 * These are all really static, not just the first two.  However, if I ever decide
 	 * to access them with object values, declaring them non-static forces the code to
 	 * treat it as so.
 	 */
 	//private native static void init();
     private native static String libversion();
 	private native String errmsg(long db);
 
 	private native long open(String filename, boolean sharedcache, int timeout) throws IOException;
     private native void close(long db) throws IOException;
     private native long prepare(long db, String sql) throws IOException;
 	private native long lastInsertId(long db);
 
 	private native int bind_parameter_count(long stmt);
     private native int changes(long stmt);
 
 	//private native int finalize(long stmt);
     private native int step(long stmt);
     private native int reset(long stmt);
     private native int clear_bindings(long stmt);
 	
     private native int    column_count(long stmt);
     //private native int    column_type(long stmt, int col);
     private native String column_decltype(long stmt, int col);
     private native String column_name(long stmt, int col);
 	
     private native String column_text(long stmt, int col);
     private native byte[] column_blob(long stmt, int col);
     private native double column_double(long stmt, int col);
     private native long   column_long(long stmt, int col);
     private native int    column_int(long stmt, int col);
 
     private native int bind_null  (long stmt, int pos);
     private native int bind_int   (long stmt, int pos, int    v);
     private native int bind_long  (long stmt, int pos, long   v);
     private native int bind_double(long stmt, int pos, double v);
     private native int bind_text  (long stmt, int pos, String v);
     private native int bind_blob  (long stmt, int pos, byte[] v);
 }
 
