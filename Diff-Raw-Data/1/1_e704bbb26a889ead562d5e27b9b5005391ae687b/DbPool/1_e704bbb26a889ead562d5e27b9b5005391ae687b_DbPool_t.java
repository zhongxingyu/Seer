 package org.caudexorigo.jdbc;
 
 import java.io.Closeable;
 import java.sql.ResultSet;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DbPool implements Closeable
 {
 	private static final int DEFAULT_MAX_POOL_SIZE = 20;
 
 	private static final int DEFAULT_TIMEOUT = 5; // 5 seconds
 
 	private static final int DEFAULT_TTL = 300; // 5 minutes
 
 	private static final boolean DEFAULT_USE_CACHE = true;
 
 	private static final Logger log = LoggerFactory.getLogger(DbPool.class);
 
 	private Db dequeue(BlockingQueue<Db> pool)
 	{
 		try
 		{
 			Db d = pool.poll(timeout, TimeUnit.SECONDS);
 
 			if (d == null)
 			{
 				throw new RuntimeException("Could not acquire a connection from the pool in the specified timeout.");
 			}
 
 			if (!d.isActive())
 			{
 				d.destroy();
 				DbInfo dbinfo_o = d.getDbInfo();
 				DbInfo dbinfo_n = new DbInfo(dbinfo_o.getConnectionGroupName(), dbinfo_o.getDriverClass(), dbinfo_o.getDriverUrl(), dbinfo_o.getUsername(), dbinfo_o.getPassword(), dbinfo_o.getTtl(), dbinfo_o.getQueryTimeout(), dbinfo_o.getUseCache());
 				d = new Db(dbinfo_n);
 			}
 
 			if (log.isDebugEnabled())
 			{
 				log.debug("Using connection: {}", d.getDbInfo().toString());
 			}
 
 			return d;
 		}
 		catch (InterruptedException e)
 		{
 			Thread.currentThread().interrupt();
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static void destroy(Db dbexec)
 	{
 		dbexec.destroy();
 	}
 
 	public DbType getDbType()
 	{
 		return dbType;
 	}
 
 	public Set<String> getPoolNames()
 	{
 		return Collections.unmodifiableSet(pools.keySet());
 	}
 
 	public Db obtain()
 	{
 		return obtain(defaultPool);
 	}
 
 	public Db obtain(String poolName)
 	{
 		if (log.isDebugEnabled())
 		{
 			log.debug("Obtain connection group: {}", poolName);
 		}
 
 		return dequeue(pools.get(poolName));
 	}
 
 	public void release(Db d)
 	{
 		if (d != null)
 		{
 			String poolName = d.getDbInfo().getConnectionGroupName();
 
 			if (d.isActive())
 			{
 				pools.get(poolName).offer(d);
 			}
 			else
 			{
 				d.destroy();
 				DbInfo dbinfo_o = d.getDbInfo();
 				DbInfo dbinfo_n = new DbInfo(dbinfo_o.getConnectionGroupName(), dbinfo_o.getDriverClass(), dbinfo_o.getDriverUrl(), dbinfo_o.getUsername(), dbinfo_o.getPassword(), dbinfo_o.getTtl(), dbinfo_o.getQueryTimeout(), dbinfo_o.getUseCache());
 				pools.get(poolName).offer(new Db(dbinfo_n));
 			}
 		}
 	}
 
 	private int con_nr;
 
 	private final AtomicInteger current_idx = new AtomicInteger(0);
 
 	private DbConfigReader dbcr;
 
 	private DbType dbType;
 
 	private String defaultPool;
 
 	private ConcurrentMap<String, BlockingQueue<Db>> pools;
 
 	private int query_timeout;
 
 	private boolean use_cache;
 
 	private int timeout;
 
 	private int ttl;
 
 	public DbPool(Properties props)
 	{
 		dbcr = new DbConfigReader(props);
 		init();
 	}
 
 	public DbPool(String bundleName)
 	{
 		dbcr = new DbConfigReader(bundleName);
 		init();
 	}
 
 	public void close()
 	{
 		Collection<BlockingQueue<Db>> inner_pools = pools.values();
 
 		for (BlockingQueue<Db> bq : inner_pools)
 		{
 			for (Db db : bq)
 			{
 				db.destroy();
 			}
 
 			bq.clear();
 		}
 
 		pools.clear();
 	}
 
 	public void ping(String sql, RowHandler row_handler)
 	{
 		Collection<BlockingQueue<Db>> inner_pools = pools.values();
 
 		for (BlockingQueue<Db> bq : inner_pools)
 		{
 			for (Db db : bq)
 			{
 				ResultSet rs = null;
 
 				try
 				{
 					rs = db.fetchResultSetWithStatment(sql);
 
 					row_handler.beforeFirst(rs);
 
 					while (rs.next())
 					{
 						row_handler.process(rs);
 					}
 
 					row_handler.afterLast(rs);
 				}
 				catch (Throwable ex)
 				{
					log.error("Ping error: '{}'", ex.getMessage());
 					db.destroy();
 				}
 				finally
 				{
 					Db.closeQuietly(rs);
 				}
 			}
 		}
 	}
 
 	public Db pick()
 	{
 		int s = pools.size();
 
 		if (s == 1)
 		{
 			return obtain();
 		}
 
 		if (s == 0)
 		{
 			throw new RuntimeException("No available pools from which to pick a connection.");
 		}
 
 		int n = Math.abs(current_idx.incrementAndGet() % s);
 
 		Collection<BlockingQueue<Db>> db_queues = pools.values();
 
 		int idx = 0;
 		for (BlockingQueue<Db> pool : db_queues)
 		{
 			idx++;
 			if (idx > n)
 			{
 				return dequeue(pool);
 			}
 		}
 
 		// we should never get here but i've been wrong before. in that case
 		// just return the default connection
 		return obtain();
 	}
 
 	public void reset()
 	{
 		close();
 		init();
 	}
 
 	private void init()
 	{
 		con_nr = tryParse("connections", dbcr.getString("connections"), 1);
 		defaultPool = dbcr.getString("default.connection");
 		ttl = tryParse("connection.ttl", dbcr.getString("connection.ttl"), DEFAULT_TTL);
 		timeout = tryParse("connection.timeout", dbcr.getString("connection.timeout"), DEFAULT_TIMEOUT);
 		query_timeout = tryParse("query.timeout", dbcr.getString("query.timeout"), DEFAULT_TIMEOUT);
 		use_cache = tryParse("use.statement.cache", dbcr.getString("use.statement.cache"), DEFAULT_USE_CACHE);
 		log.info("Number of configured database connection groups: {}", con_nr);
 		log.info("Default database connection group: '{}'", defaultPool);
 
 		pools = new ConcurrentHashMap<String, BlockingQueue<Db>>();
 
 		for (int i = 1; i <= con_nr; i++)
 		{
 			int con_poll_size = tryParse(i + ".max.pool.size", dbcr.getString(i + ".max.pool.size"), DEFAULT_MAX_POOL_SIZE);
 
 			log.info("Connection group: '{}'; Number of connections: {}", i, con_poll_size);
 
 			String con_name = "" + i;
 			String driver_class = dbcr.getString(i + ".driver.name");
 			String driver_url = dbcr.getString(i + ".conn.url");
 			String user = dbcr.getString(i + ".user.name");
 			String password = dbcr.getString(i + ".password");
 
 			DbInfo dbinfo = new DbInfo(con_name, driver_class, driver_url, user, password, ttl, query_timeout, use_cache);
 
 			log.info("Connection: {}", dbinfo.toString());
 
 			try
 			{
 				Class.forName(driver_class);
 				setDbType(con_name, driver_class);
 			}
 			catch (Exception e)
 			{
 				throw new IllegalArgumentException("Jdbc driver class not found", e);
 			}
 
 			BlockingQueue<Db> pool = new LinkedBlockingQueue<Db>(con_poll_size);
 
 			for (int j = 0; j < con_poll_size; j++)
 			{
 				try
 				{
 					Db dbexec = new Db(dbinfo);
 					pool.offer(dbexec);
 				}
 				catch (Throwable t)
 				{
 					throw new RuntimeException(t);
 				}
 			}
 			pools.put(con_name, pool);
 		}
 	}
 
 	private void setDbType(String con_name, String driver_class)
 	{
 		if (con_name.equals(defaultPool))
 		{
 			if ("org.postgresql.Driver".equals(driver_class))
 			{
 				dbType = DbType.PGSQL;
 			}
 			else if ("com.mysql.jdbc.Driver".equals(driver_class))
 			{
 				dbType = DbType.MYSQL;
 			}
 			else if ("net.sourceforge.jtds.jdbc.Driver".equals(driver_class) || "com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(driver_class))
 			{
 				dbType = DbType.MSSQL;
 			}
 			else if ("oracle.jdbc.OracleDriver".equals(driver_class))
 			{
 				dbType = DbType.ORACLE;
 			}
 			else if ("org.h2.Driver".equals(driver_class))
 			{
 				dbType = DbType.H2;
 			}
 			else if ("nl.cwi.monetdb.jdbc.MonetDriver".equals(driver_class))
 			{
 				dbType = DbType.MONETDB;
 			}
 			else
 			{
 				dbType = DbType.OTHER;
 			}
 		}
 	}
 
 	private int tryParse(String fieldName, String intValue, int defaultValue)
 	{
 		int r;
 		try
 		{
 			if (StringUtils.isBlank(intValue))
 			{
 				log.warn(String.format("Apply default value for '%s': %s", fieldName, defaultValue));
 				r = defaultValue;
 			}
 			else
 			{
 				r = Integer.parseInt(intValue);
 			}
 		}
 		catch (Throwable t)
 		{
 			log.warn(String.format("Apply default value for '%s': %s -> '%s'", fieldName, defaultValue, t.getMessage()));
 			r = defaultValue;
 		}
 		return r;
 	}
 
 	private boolean tryParse(String fieldName, String boolValue, boolean defaultValue)
 	{
 		boolean r;
 		try
 		{
 			if (StringUtils.isBlank(boolValue))
 			{
 				log.warn(String.format("Apply default value for '%s': %s", fieldName, defaultValue));
 				r = defaultValue;
 			}
 			else
 			{
 				r = Boolean.parseBoolean(boolValue);
 			}
 		}
 		catch (Throwable t)
 		{
 			log.warn(String.format("Apply default value for '%s': %s -> '%s'", fieldName, defaultValue, t.getMessage()));
 			r = defaultValue;
 		}
 		return r;
 	}
 
 	@Override
 	public String toString()
 	{
 		return String.format("DbPool [propos=%s]", dbcr.getPropsAsString());
 	}
 }
