 package org.generationcp.ibpworkbench.install4j;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import com.install4j.api.Util;
 import com.install4j.api.context.Context;
 import com.install4j.api.context.InstallationComponentSetup;
 import com.install4j.api.context.InstallerContext;
 
 public abstract class Install4JUtil {
     public final static long MYSQL_HEADSTART_MILLIS = 1000;
     public final static long MYSQL_WAIT_MILLIS = 10000;
     public final static long MYSQL_CONNECT_TRIES = 6;
     public final static int MYSQL_LOG_DELETE_TRIES = 30;
     
     public static String getMysqlDirPath(Context context) {
         return (String) context.getCompilerVariable("gcp.mysql.dir");
     }
     
     public static String getMysqlIniPath(Context context) {
         return getMysqlDirPath(context) + "my.ini";
     }
     
     public static String getMysqlIniBakPath(Context context) {
         return getMysqlDirPath(context) + "my.ini.bak";
     }
     
     public static String getMysqlBinPath(Context context) {
         return getMysqlDirPath(context) + "bin";
     }
     
     public static boolean startMySQL(Context context) {
         File installDir = context.getInstallationDirectory();
         File mysqlBinPath = new File(installDir, getMysqlBinPath(context));
         
         String mysqldExeName = "mysqld.exe";
         String myIniPath =  "../my.ini";
         
         File mysqldFile = new File(mysqlBinPath, mysqldExeName);
         if (!mysqldFile.exists()) {
             Util.showErrorMessage(context.getMessage("mysql_not_installed"));
             return false;
         }
         
         // start MySQL
         String mysqldPath = mysqlBinPath.getAbsolutePath() + File.separator + mysqldExeName;
         ProcessBuilder pb = new ProcessBuilder(mysqldPath, "--defaults-file=" + myIniPath);
         pb.directory(mysqlBinPath);
         
         context.getProgressInterface().setStatusMessage(context.getMessage("starting_mysql"));
         context.getProgressInterface().setDetailMessage(mysqldPath);
         
         try {
             pb.start();
         }
         catch (IOException e) {
             Util.showErrorMessage(context.getMessage("cannot_start_mysql"));
             return false;
         }
         
         // try to connect to MySQL
         Connection conn = null;
         int tryNum = 0;
         while (true) {
             if (tryNum >= MYSQL_CONNECT_TRIES) {
                 Util.showErrorMessage(context.getMessage("cannot_connect_to_mysql"));
                 break;
             }
             tryNum++;
             
             try {
                 conn = DriverManager.getConnection("jdbc:mysql://localhost:13306/?user=root");
                 break;
             }
             catch (SQLException e) {
             }
             
             try {
                 Thread.sleep(MYSQL_WAIT_MILLIS);
             }
             catch (InterruptedException e) {
                 break;
             }
         }
         
         if (conn == null) {
             return false;
         }
         
         try {
             conn.close();
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         
         return true;
     }
     
     public static boolean stopMySQL(Context context) {
         File mysqlBinPath = new File(context.getInstallationDirectory(), getMysqlBinPath(context));
         String mysqlAdminExeName = "mysqladmin.exe";
         String myIniPath =  "../my.ini";
         
         String mysqlAdminPath = mysqlBinPath.getAbsolutePath() + File.separator + mysqlAdminExeName;
         
         ProcessBuilder mysqlAdminPb = new ProcessBuilder(mysqlAdminPath, "--defaults-file=" + myIniPath, "-u", "root", "shutdown");
         mysqlAdminPb.directory(mysqlBinPath);
         
         context.getProgressInterface().setStatusMessage(context.getMessage("stopping_mysql"));
         context.getProgressInterface().setDetailMessage("");
         
         Process stopMySQLProcess = null;
         try {
             stopMySQLProcess = mysqlAdminPb.start();
         }
         catch (IOException e) {
             Util.showErrorMessage(context.getMessage("cannot_stop_mysql"));
             return false;
         }
         
         // wait for the MySQL Admin process to terminate
         try {
             stopMySQLProcess.waitFor();
         }
         catch (InterruptedException e) {
             Util.showErrorMessage(context.getMessage("cannot_stop_mysql"));
             return false;
         }
         
         return true;
     }
     
     public static Connection connectToMySQL(InstallerContext context) {
         try {
             Class.forName("com.mysql.jdbc.Driver");
         }
         catch (ClassNotFoundException e) {
             Util.showErrorMessage(context.getMessage("cannot_load_jdbc_driver"));
             return null;
         }
         
         // retry until we can connect to mysql
         Connection conn = null;
         int tryNum = 0;
         while (true) {
             if (tryNum >= MYSQL_CONNECT_TRIES) {
                 Util.showErrorMessage(context.getMessage("cannot_connect_to_mysql"));
                 return null;
             }
             tryNum++;
             
             try {
                 conn = DriverManager.getConnection("jdbc:mysql://localhost:13306/?user=root");
                 break;
             }
             catch (SQLException e) {
             }
             
             try {
                 Thread.sleep(MYSQL_WAIT_MILLIS);
             }
             catch (InterruptedException e) {
                 break;
             }
         }
         
         return conn;
     }
     
     public static boolean optimizeMySQLConfiguration(Context context) {
         File mysqlIniPath = new File(context.getInstallationDirectory(), getMysqlIniPath(context));
         File mysqlIniBakPath = new File(context.getInstallationDirectory(), getMysqlIniBakPath(context));
         
         Long ram = (Long) context.getVariable("gcp.mysql.ram.size");
         if (ram == null) {
             Util.showErrorMessage(context.getMessage("invalid_installer_configuration"));
             return false;
         }
         
         if (!mysqlIniPath.exists()) {
             Util.showErrorMessage(context.getMessage("invalid_installer_configuration"));
             return false;
         }
         
         if (!mysqlIniPath.renameTo(mysqlIniBakPath)) {
             Util.showErrorMessage(context.getMessage("installation_error"));
             return false;
         }
         
         // long systemMemory = Math.round(SystemInfo.getPhysicalMemory() / (1024 * 1024));
         
         // Size of the Key Buffer, used to cache index blocks for MyISAM tables.
         // Do not set it larger than 30% of your available memory, as some
         // memory is also required by the OS to cache rows. Even if you're not
         // using MyISAM tables, you should still set it to 8-64M as it will also
         // be used for internal temporary disk tables.
         // We set it at 30% of available memory but must be between 8 to 512MB.
         long keyBufferSize = Math.max(Math.min(Math.round(ram * 0.30), 512), 8);
         
 
         // MyISAM uses special tree-like cache to make bulk inserts (that is,
         // INSERT ... SELECT, INSERT ... VALUES (...), (...), ..., and LOAD DATA
         // INFILE) faster. This variable limits the size of the cache tree in
         // bytes per thread. Setting it to 0 will disable this optimization. Do
         // not set it larger than "key_buffer_size" for optimal performance.
         // This buffer is allocated when a bulk insert is detected.
         // We set it at 20% of available memory but must be between 8 to 512MB.
         long bulkInsertBufferSize = Math.max(Math.min(Math.round(ram * 0.20), 512), 8);
         
         // The maximum size of a query packet the server can handle as well as
         // maximum query size server can process (Important when working with
         // large BLOBs). enlarged dynamically, for each connection.
         long maxAllowedPacket = 32;
         
         // Sort buffer is used to perform sorts for some ORDER BY and GROUP BY
         // queries. If sorted data does not fit into the sort buffer, a disk
         // based merge sort is used instead - See the "Sort_merge_passes" status
         // variable. Allocated per thread if sort is needed.
         long sortBufferSize = 8;
         
         // Size of the buffer used for doing full table scans. Allocated per
         // thread, if a full scan is needed.
         long readBufferSize = 8;
         
         // When reading rows in sorted order after a sort, the rows are read
         // through this buffer to avoid disk seeks. You can improve ORDER BY
         // performance a lot, if set this to a high value. Allocated per thread,
         // when needed
         long readRndBufferSize = 16;
         
         // This buffer is allocated when MySQL needs to rebuild the index in
         // REPAIR, OPTIMIZE, ALTER table statements as well as in LOAD DATA
         // INFILE into an empty table. It is allocated per thread so be careful
         // with large settings.
         long myisamSortBufferSize = 128;
         
         // How many threads we should keep in a cache for reuse. When a client
         // disconnects, the client's threads are put in the cache if there
         // aren't more than thread_cache_size threads from before. This greatly
         // reduces the amount of thread creations needed if you have a lot of
         // new connections. (Normally this doesn't give a notable performance
         // improvement if you have a good thread implementation.)
         long threadCacheSize = 8;
         
         // This permits the application to give the threads system a hint for
         // the desired number of threads that should be run at the same time.
         // This value only makes sense on systems that support the
         // thread_concurrency() function call (Sun Solaris, for example). You
         // should try [number of CPUs]*(2..4) for thread_concurrency
         long threadConcurrency = Runtime.getRuntime().availableProcessors() * 4;
 
         // Query cache is used to cache SELECT results and later return them
         // without actual executing the same query once again. Having the query
         // cache enabled may result in significant speed improvements, if your
         // have a lot of identical queries and rarely changing tables. See the
         // "Qcache_lowmem_prunes" status variable to check if the current value
         // is high enough for your load. Note: In case your tables change very
         // often or if your queries are textually different every time, the
         // query cache may result in a slowdown instead of a performance
         // improvement.
         long queryCacheSize = 64;
         
         // InnoDB, unlike MyISAM, uses a buffer pool to cache both indexes and
         // row data. The bigger you set this the less disk I/O is needed to
         // access data in tables. On a dedicated database server you may set
         // this parameter up to 80% of the machine physical memory size. Do not
         // set it too large, though, because competition of the physical memory
         // may cause paging in the operating system. Note that on 32bit systems
         // you might be limited to 2-3.5G of user level memory per process, so
         // do not set it too high.
         // We set it at 50% of available memory but must be between 8MB to 1GB
        long innodbBufferPoolSize = Math.max(Math.min(Math.round(ram * 0.50), 512), 8);
 
         // Number of IO threads to use for async IO operations. This value is
         // hardcoded to 8 on Unix, but on Windows disk I/O may benefit from a
         // larger number.
         long innodbWriteIoThreads = 8;
         long innodbReadIoThreads = 8;
         
         // The size of the buffer InnoDB uses for buffering log data. As soon as
         // it is full, InnoDB will have to flush it to disk. As it is flushed
         // once per second anyway, it does not make sense to have it very large
         // (even with long transactions). 
         long innodbLogBufferSize = 8;
 
         // Size of each log file in a log group. You should set the combined
         // size of log files to about 25%-100% of your buffer pool size to avoid
         // unneeded buffer pool flush activity on log file overwrite. However,
         // note that a larger logfile size will increase the time needed for the
         // recovery process.
         // Ideally, we should set it at 50% of InnoDB buffer pool size (but must be between 8MB to 256MB)
         // but to avoid errors when starting MySQL, we keep it the same as the un-optimized my.ini
         // long innodbLogFileSize = Math.max(Math.min(Math.round(innodbBufferPoolSize * 0.5), 256), 8);
         long innodbLogFileSize = 64;
         
         String optimizedMyIni = "[client]\r\n"
                               + "port                       = 13306\r\n"
                               + "socket                     = /tmp/mysql.sock\r\n"
                               + "[mysqld]\r\n"
                               + "port                       = 13306\r\n"
                               + "socket                     = /tmp/mysql.sock\r\n"
                               + "datadir                    =" + getMySqlDataPath(context) + "\r\n"
                               + "skip-external-locking\r\n"
                               + "key_buffer_size            = %dM\r\n"
                               + "bulk_insert_buffer_size    = %dM\r\n"
                               + "max_allowed_packet         = %dM\r\n"
                               + "sort_buffer_size           = %dM\r\n"
                               + "read_buffer_size           = %dM\r\n"
                               + "read_rnd_buffer_size       = %dM\r\n"
                               + "myisam_sort_buffer_size    = %dM\r\n"
                               + "thread_cache_size          = %d\r\n"
                               + "thread_concurrency         = %d\r\n"
                               + "query_cache_size           = %dM\r\n"
                               + "innodb_buffer_pool_size    = %dM\r\n"
                               + "innodb_data_file_path      = ibdata1:10M:autoextend\r\n"
                               + "innodb_write_io_threads    = %d\r\n"
                               + "innodb_read_io_threads     = %d\r\n"
                               + "innodb_log_buffer_size     = %dM\r\n"
                               + "innodb_log_file_size       = %dM\r\n"
                               + "innodb_fast_shutdown       = 0\r\n"
                               ;
         
         String fileContents = String.format(optimizedMyIni, keyBufferSize
                                                           , bulkInsertBufferSize
                                                           , maxAllowedPacket
                                                           , sortBufferSize
                                                           , readBufferSize
                                                           , readRndBufferSize
                                                           , myisamSortBufferSize
                                                           , threadCacheSize
                                                           , threadConcurrency
                                                           , queryCacheSize
                                                           , innodbBufferPoolSize
                                                           , innodbWriteIoThreads
                                                           , innodbReadIoThreads
                                                           , innodbLogBufferSize
                                                           , innodbLogFileSize
                                             );
         
         try {
             FileOutputStream fos = new FileOutputStream(mysqlIniPath);
             fos.write(fileContents.getBytes());
             fos.flush();
             fos.close();
         }
         catch (IOException e) {
             Util.showErrorMessage(context.getMessage("installation_error") + ": " + e.getMessage());
             return false;
         }
         
         return true;
     }
     
     public static boolean revertMySQLConfiguration(Context context) {
         File mysqlIniPath = new File(context.getInstallationDirectory(), getMysqlIniPath(context));
         File mysqlIniBakPath = new File(context.getInstallationDirectory(), getMysqlIniBakPath(context));
         
         if (!mysqlIniPath.delete()) {
             Util.showErrorMessage(context.getMessage("installation_error"));
             return false;
         }
         
         if (!mysqlIniBakPath.exists()) {
             Util.showErrorMessage(context.getMessage("invalid_installer_configuration"));
             return false;
         }
         
         if (!mysqlIniBakPath.renameTo(mysqlIniPath)) {
             Util.showErrorMessage(context.getMessage("installation_error"));
             return false;
         }
         
         return true;
     }
     
     public static String getMySqlDataPath(Context context) {
         File installationDirectory = context.getInstallationDirectory();
         
         String mysqlDataPath = context.getCompilerVariable("gcp.mysql.data.dir");
         if (mysqlDataPath == null) {
             mysqlDataPath = "data";
         }
         
         return installationDirectory.getAbsolutePath() + File.separator + mysqlDataPath;
     }
     
     public static String getMySqlConfPath(Context context) {
         File installationDirectory = context.getInstallationDirectory();
         
         String mysqlDataPath = context.getCompilerVariable("gcp.mysql.dir");
         if (mysqlDataPath == null) {
             mysqlDataPath = "infrastructure/mysql/";
         }
         
         return installationDirectory.getAbsolutePath() + File.separator + mysqlDataPath + File.separator +  "my.ini";
     }
     
     public static String stringWithContentsOFile(File file) throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         
         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
         byte[] buff = new byte[1024 * 100];
         
         try {
             int length = -1;
             while ((length = bis.read(buff, 0, buff.length)) != -1) {
                 baos.write(buff, 0, length);
             }
         }
         finally {
             if (bis != null) {
                 try {
                     bis.close();
                 }
                 catch (IOException e) {
                 }
             }
         }
         
         return new String(baos.toByteArray());
     }
     
     public static void writeStringToFile(String string, File file) throws IOException {
         FileOutputStream fos = null;
         
         try {
             fos = new FileOutputStream(file);
             fos.write(string.getBytes());
             fos.flush();
         }
         finally {
             if (fos != null) {
                 try {
                     fos.close();
                 }
                 catch (IOException e) {
                 }
             }
         }
     }
     
     public static boolean isComponentSelected(InstallerContext context, Crop crop) {
         InstallationComponentSetup component = context.getInstallationComponentById(crop.getCropName());
         return component == null ? false : component.isSelected();
     }
     
     public static boolean executeUpdate(Context context, Connection conn, boolean showError, String... queries) {
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
             for (String query : queries) {
                 stmt.executeUpdate(query);
             }
             return true;
         }
         catch (SQLException e) {
             e.printStackTrace();
             if (showError) {
                 Util.showErrorMessage(context.getMessage("cannot_initialize_database"));
             }
             return false;
         }
         finally {
             try {
                 if (stmt != null) {
                     stmt.close();
                 }
             }
             catch (SQLException e2) {
                 // intentionally empty
             }
         }
     }
     
     public static boolean executeQuery(Context context, Connection conn, boolean showError, String... queries) {
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
             for (String query : queries) {
                 stmt.executeQuery(query);
             }
             return true;
         }
         catch (SQLException e) {
             e.printStackTrace();
             if (showError) {
                 Util.showErrorMessage(context.getMessage("cannot_initialize_database"));
             }
             return false;
         }
         finally {
             try {
                 if (stmt != null) {
                     stmt.close();
                 }
             }
             catch (SQLException e2) {
                 // intentionally empty
             }
         }
     }
     
     public static boolean canExecuteQueries(Context context, Connection conn, String... queries) {
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
             for (String query : queries) {
                 stmt.executeQuery(query);
             }
             return true;
         }
         catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
         finally {
             try {
                 if (stmt != null) {
                     stmt.close();
                 }
             }
             catch (SQLException e2) {
                 // intentionally empty
             }
         }
     }
     
     public static boolean useDatabase(Connection conn, String databaseName) {
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
             stmt.execute("USE " + databaseName);
             return true;
         }
         catch (SQLException e) {
             return false;
         }
         finally {
             try {
                 if (stmt != null) {
                     stmt.close();
                 }
             }
             catch (SQLException e2) {
                 // intentionally empty
             }
         }
     }
 }
