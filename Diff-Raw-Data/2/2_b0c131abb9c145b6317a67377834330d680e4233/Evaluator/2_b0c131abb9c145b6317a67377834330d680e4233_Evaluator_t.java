 /**
  * Bristlecone Test Tools for Databases
  * Copyright (C) 2006-2007 Continuent Inc.
  * Contact: bristlecone@lists.forge.continuent.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
  *
  * Initial developer(s): Robert Hodges and Ralph Hannus.
  * Contributor(s):
  */
 
 package com.continuent.bristlecone.evaluator;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 
 import org.apache.log4j.Logger;
 import org.jfree.ui.RefineryUtilities;
 
 /**
  * @author <a href="mailto:ralph.hannus@continuent.com">Ralph Hannus</a>
  */
 public class Evaluator implements RowFactory, Runnable
 {
     private static Logger                     logger                = Logger
                                                                             .getLogger(Evaluator.class);
 
     private volatile boolean                  readyToStart;
 
     private volatile boolean                  readyToStop;
 
     private Random                            rand                  = new Random(
                                                                             0xdeadbeef);
     protected Configuration                   conf;
 
     protected ArrayList<Thread>               threads               = new ArrayList<Thread>();
     private int                               threadsAlive          = -1;
 
     private LinkedHashMap<String, Statistics> statsList             = new LinkedHashMap<String, Statistics>();
     private Statistics                        previousStats         = new Statistics();
     private ArrayList<Throwable>              failures              = new ArrayList<Throwable>();
 
     private boolean                           initialized;
 
     private boolean                           graph                 = false;
 
     private long                              timesLogged           = 0L;
 
     private ArrayList<StatisticsListener>     listeners             = new ArrayList<StatisticsListener>();
 
     /**
      * The number of milliseconds to wait before taking a sample
      */
     private static final int                  SAMPLE_QUANTUM        = 1000;
 
     /**
      * The number of samples to accumulate for averaging
      */
     private static final int                  SAMPLES_TO_ACCUMULATE = 1;
     
     private static final int WINDOW_SIZE = 10;
 
     /**
      * The current count of samples accumulated
      */
     private int                               currentSampleCount    = 0;
 
     /**
      * An array of statistics that have been accumulated for this sample period
      */
     private Statistics[]                      statisticsAccumulator = new Statistics[SAMPLES_TO_ACCUMULATE];
 
     private int                               timeSinceLastStatus   = 0;
 
     /**
      * The number of seconds, if set, between each logging of the statistics
      */
     private int                               statusInterval        = 0;
 
     /**
      * Statistics to be used for the coarser grained status printing
      */
     private Statistics                        currentStatusStats  = new Statistics();
     
     // private static String varChar255 =
     // "012345678901234567890123456789012345678901234567890" +
     // "012345678901234567890123456789012345678901234567890" +
     // "012345678901234567890123456789012345678901234567890" +
     // "012345678901234567890123456789012345678901234567890" +
     // "012345678901234567890123456789012345678901234567890";
 
     public Evaluator(Configuration conf)
     {
         this.conf = conf;
     }
 
     public ArrayList getThreads()
     {
         return threads;
     }
 
     public Iterator getStats()
     {
         return statsList.values().iterator();
     }
 
     public Configuration getConfiguration()
     {
         return conf;
     }
 
     public boolean isReadyToStart()
     {
         return readyToStart;
     }
 
     public synchronized boolean isReadyToStop()
     {
         return readyToStop;
     }
 
     public synchronized void stop()
     {
         readyToStop = true;
         notifyAll();
     }
 
     /**
      * The main routine for the Evaluator load testing tool. Reads a test
      * configuration from the specified file and runs the test using those
      * values.
      */
     public static void main(String[] argv)
     {
         try
         {
             // Parse arguments.
             boolean graph = false;
             String name = null;
             String configFile = null;
 
             // Parse arguments.
             int argc = 0;
             while (argc < argv.length)
             {
                 String nextArg = argv[argc];
                 argc++;
                 if (nextArg.startsWith("-"))
                 {
                     if ("-name".equals(nextArg))
                     {
                         name = argv[argc++];
                     }
                     if ("-graph".equals(nextArg))
                     {
                         graph = true;
                     }
                     else if ("-help".equals(nextArg))
                     {
                         usage();
                         return;
                     }
                     else
                     {
                         println("Unrecognized flag (try -help for usage): "
                                 + nextArg);
                         System.exit(1);
                     }
                 }
                 else
                 {
                     configFile = nextArg;
                 }
             }
 
             // Ensure we have a config file.
             if (configFile == null)
             {
                 println("You must supply a config file (try -help for usage)");
                 System.exit(1);
             }
 
             // Use alternate name for output files if supplied by user.
             Configuration conf = new Configuration(configFile);
             if (name != null)
             {
                 if (conf.getXmlFile() != null && conf.getXmlFile().length() > 0)
                 {
                     conf.setXmlFile(name + ".xml");
                 }
                 if (conf.getHtmlFile() != null
                         && conf.getHtmlFile().length() > 0)
                 {
                     conf.setHtmlFile(name + ".html");
                 }
                 if (conf.getCsvFile() != null && conf.getCsvFile().length() > 0)
                 {
                     conf.setCsvFile(name + ".csv");
                 }
                 conf.setName(name);
             }
 
             // Instantiate evaluator.
             Evaluator eval = new Evaluator(conf);
             eval.graph = graph;
 
             // Start the graphical evaluator display if desired.
             if (graph)
             {
                 // GraphicalEvaluatorDisplay graphDisplay = new
                 // GraphicalEvaluatorDisplay(
                 // "Continuent Cluster Monitor");
                 ReadsVersusWritesDisplay graphDisplay = new ReadsVersusWritesDisplay(
                         "Tungsten Enterprise Cluster Monitor");
                 graphDisplay.pack();
                 RefineryUtilities.centerFrameOnScreen(graphDisplay);
                 graphDisplay.setVisible(true);
                 eval.addStatisticsListner(graphDisplay);
 
             }
 
             // Run the evaluator demo.
             eval.run();
         }
         catch (EvaluatorException e)
         {
             e.printStackTrace();
         }
     }
 
     /** Print to standard out. */
     protected static void println(String message)
     {
         System.out.println(message);
     }
 
     /** Print usage. */
     protected static void usage()
     {
         println("Usage: java " + Evaluator.class.getName()
                 + " [options] config_file");
         println("  -graph    Log results on graphical display");
         println("  -name     Supply alternate name for HTML/XML output");
         println("  -help     Print usage");
         println("Config file format is defined by evaluator.dtd");
     }
 
     /**
      * Runs one iteration of the evaluator.
      * 
      * @throws EvaluatorException whenever an error occurs
      */
     public void run()
     {
         if (!initialized)
         {
             try
             {
                 initialize();
             }
             catch (EvaluatorException e)
             {
                 e.printStackTrace();
                 throw new Error(e);
             }
         }
         runThreads();
     }
 
     /**
      * @throws EvaluatorException
      */
     public void initialize() throws EvaluatorException
     {
         initializeDB();
         createThreads();
         initialized = true;
     }
 
     /**
      * This routine creates the threads specified in the configuration. When
      * ramp up interval and ramp up increment are specified, threads after the
      * first increment are set to sleep until they needed. Only the number of
      * threads that will have an opportunity run during the test duration will
      * be created.
      * 
      * @throws EvaluatorException
      */
     private void createThreads() throws EvaluatorException
     {
         for (TableGroup tableGroup : conf.getTableGroups())
         {
             tableGroup.setRowFactory(this);
             for (Iterator threadsIterator = tableGroup.getThreads().iterator(); threadsIterator
                     .hasNext();)
             {
                 ThreadConfiguration threadGroup = (ThreadConfiguration) threadsIterator
                         .next();
                 /*
                  * When ramp up is configured, the test duration may not be long
                  * enough to start all the threads. In this case we don't
                  * scheule all the threads.
                  */
                 int limit = threadGroup.getCount();
                 if (threadGroup.getRampUpInterval() > 0
                         && threadGroup.getRampUpIncrement() > 0)
                 {
                     int intervals = conf.getTestDuration()
                             / threadGroup.getRampUpInterval() + 1;
                     limit = Math.min(limit, intervals
                             * threadGroup.getRampUpIncrement());
                 }
                 for (int i = 0; i < limit; i++)
                 {
                     EvaluatorThread t = new EvaluatorThread(this, threadGroup,
                             threadGroup.getName() + i);
                     if (threadGroup.getRampUpInterval() > 0
                             && threadGroup.getRampUpIncrement() > 0)
                     {
                         t.setSleepBeforeStart(i
                                 / threadGroup.getRampUpIncrement()
                                 * threadGroup.getRampUpInterval() * 1000);
                     }
                     this.threads.add(t);
                 }
             }
 
         }
     }
 
     public void runThreads()
     {
         for (Iterator iter = threads.iterator(); iter.hasNext();)
         {
             EvaluatorThread t = (EvaluatorThread) iter.next();
             // do not run threads as daemons, rather system.exit() at the end
             // see SEQUOIA-950
             t.start();
         }
         readyToStart = true;
         logger.info("Starting test run");
         int runTime = conf.getTestDuration() * 1000;
 
         // Interval to be used to log statistics. Graphical stats
         // are accumulated and handled independently of this.
         statusInterval = conf.getStatusInterval() * 1000;
 
         synchronized (this)
         {
 
             while (runTime > 0)
             {
                 try
                 {
                     // Wait for our sample interval to expire
 
                     wait(SAMPLE_QUANTUM);
 
                     if (readyToStop)
                     {
                         logger.debug("Stop requested");
                         break;
                     }
 
                     // count the number of threads still alive
                     int threadsCount = 0;
                     for (Iterator iter = threads.iterator(); iter.hasNext();)
                     {
                         EvaluatorThread t = (EvaluatorThread) iter.next();
                         if (t.isAlive())
                             threadsCount++;
                     }
                     threadsAlive = threadsCount;
                     if (threadsCount == 0)
                     {
                         logger.debug("All threads have stopped");
                         break;
                     }
 
                     runTime -= SAMPLE_QUANTUM;
 
                     logStatistics(SAMPLE_QUANTUM);
 
                 }
                 catch (InterruptedException e)
                 {
                     e.printStackTrace();
                 }
             }
 
         }
         readyToStop = true;
         logger.debug("Waiting for thread completion");
         // FIXME: this timeout should be function of the thread load
         long endTime = System.currentTimeMillis() + 60000;
         for (Iterator iter = threads.iterator(); iter.hasNext();)
         {
             EvaluatorThread t = (EvaluatorThread) iter.next();
             try
             {
                 long waitTime = Math.max(endTime - System.currentTimeMillis(),
                         1);
                 t.join(waitTime);
 
                 if (t.isAlive())
                 {
                     String message = "Thread '" + t.getName()
                             + "' has not stopped";
 
                     addFailure(new Exception(message));
                     try
                     {
                         if (t.getCurrent() != null)
                             t.getCurrent().cancel();
                         t.interrupt();
                     }
                     catch (SQLException e)
                     {
                         // if it does not stop who cares
                     }
 
                     logger.error(t.getCurrent().toString());
                     logger.error(message);
                 }
             }
             catch (InterruptedException e)
             {
                 e.printStackTrace();
             }
         }
         logger.debug("All threads have stopped.");
         Statistics stats = collectStatistics();
         printStatistics(stats, "Total", conf.getTestDuration() * 1000);
 
         String xmlFile = conf.getXmlFile();
 
         if (xmlFile != null && xmlFile.length() > 0)
         {
             try
             {
                 XMLWriter xml = new XMLWriter(conf.getXmlFile());
                 xml.startTag("EvaluatorResults");
                 xml.addAttribute("name", conf.getName());
                 int time = 0;
                 for (Iterator iter = statsList.keySet().iterator(); iter
                         .hasNext();)
                 {
                     String label = (String) iter.next();
                     Statistics s = (Statistics) statsList.get(label);
                     xml.startTag("Stats");
                     xml.addAttribute("label", label);
                     xml.addAttribute("time", time);
                     xml.addAttribute("users", s.getThreads());
                     xml.addAttribute("avgResponseTime", s
                             .getAverageResponseTime());
                     xml.addAttribute("queries", s.getQueries());
                     xml.addAttribute("queriesPerSecond",
                             (int) (s.getQueries() / s.getInterval()));
                     xml.addAttribute("rowsRead", s.getRowsRead());
                     xml.addAttribute("updates", s.getUpdates());
                     xml.addAttribute("deletes", s.getDeletes());
                     xml.addAttribute("inserts", s.getInserts());
                     xml.addAttribute("interval", (int) s.getInterval());
                     time += s.getInterval();
                     xml.endTag();
                 }
                 xml.endTag();
             }
             catch (EvaluatorException e)
             {
                 logger.error(e);
             }
 
         }
         String htmlFile = conf.getHtmlFile();
 
         if (htmlFile != null && htmlFile.length() > 0)
         {
             try
             {
                 HTMLWriter html = new HTMLWriter(htmlFile, conf.getName());
                 html.startTable();
                 int time = 0;
                 html.addTableRow();
                 html.addTableHead("time");
                 html.addTableHead("users");
                 html.addTableHead("average response time");
                 html.addTableHead("queries Per Second");
                 html.addTableHead("label");
                 html.addTableHead("queries");
                 html.addTableHead("rows read");
                 html.addTableHead("updates");
                 html.addTableHead("deletes");
                 html.addTableHead("inserts");
                 html.addTableHead("interval");
                 for (Iterator iter = statsList.keySet().iterator(); iter
                         .hasNext();)
                 {
                     String label = (String) iter.next();
                     Statistics s = (Statistics) statsList.get(label);
                     html.addTableRow();
                     time += s.getInterval();
                     html.addTableData(time);
                     html.addTableData(s.getThreads());
                     html.addTableData(s.getAverageResponseTime());
                     html.addTableData((int) (s.getQueries() / s.getInterval()));
                     html.addTableData(label);
                     html.addTableData(s.getQueries());
                     html.addTableData(s.getRowsRead());
                     html.addTableData(s.getUpdates());
                     html.addTableData(s.getDeletes());
                     html.addTableData(s.getInserts());
                     html.addTableData((int) s.getInterval());
                 }
                 html.endTable();
                 html.close();
             }
             catch (EvaluatorException e)
             {
                 logger.error(e);
             }
         }
 
         String csvFile = conf.getCsvFile();
         if (csvFile != null && csvFile.length() > 0)
         {
             try
             {
                 CsvWriter csv = new CsvWriter(csvFile);
                 int time = 0;
                 csv.startHeader();
                 csv.addHeader("time");
                 csv.addHeader("users");
                 csv.addHeader("average response time");
                 csv.addHeader("queries Per Second");
                 csv.addHeader("label");
                 csv.addHeader("queries");
                 csv.addHeader("rows read");
                 csv.addHeader("updates");
                 csv.addHeader("deletes");
                 csv.addHeader("inserts");
                 csv.addHeader("interval");
                 csv.endHeader();
                 for (Iterator iter = statsList.keySet().iterator(); iter
                         .hasNext();)
                 {
                     String label = (String) iter.next();
                     Statistics s = (Statistics) statsList.get(label);
                     time += s.getInterval();
                     csv.startData();
                     csv.addData(time);
                     csv.addData(s.getThreads());
                     csv.addData(s.getAverageResponseTime());
                     csv.addData((int) (s.getQueries() / s.getInterval()));
                     csv.addData(label);
                     csv.addData(s.getQueries());
                     csv.addData(s.getRowsRead());
                     csv.addData(s.getUpdates());
                     csv.addData(s.getDeletes());
                     csv.addData(s.getInserts());
                     csv.addData((int) s.getInterval());
                     csv.endData();
                 }
                 csv.close();
             }
             catch (EvaluatorException e)
             {
                 logger.error(e);
             }
         }
         logger.info("Test run complete");
     }
 
     private void logStatistics(int interval)
     {   
         Statistics stats = collectStatistics();
 
         Statistics delta = stats.diff(previousStats);
         DateFormat f = DateFormat.getDateTimeInstance();
         String label = f.format(new Date());
 
       
         // TODO: There's a bug here, but I can't see it right now, and the other stuff works.
         // So this can be addressed later.
 //        currentStatusStats.add(delta);
 //       
 //        if (timeSinceLastStatus < statusInterval)
 //        {
 //            timeSinceLastStatus += interval;
 //        }
 //        else
 //        {
 //            printStatistics(currentStatusStats, "PERIODIC STATUS:" + label + ":", timeSinceLastStatus/1000);
 //            currentStatusStats.clear();
 //            timeSinceLastStatus = 0;
 //           
 //        }
 
         // If we don't have enough samples yet, just collect it...
         if (currentSampleCount < SAMPLES_TO_ACCUMULATE)
         {
             statisticsAccumulator[currentSampleCount++] = delta;
         }
         else
         {
             Statistics average = getStatsAverages(SAMPLES_TO_ACCUMULATE);
             currentSampleCount = 0;
             
             average.setInterval( ((float)(SAMPLES_TO_ACCUMULATE * SAMPLE_QUANTUM))/1000);
             printStatistics(average, "AVERAGES:" + label + ":", ((float)(SAMPLES_TO_ACCUMULATE * SAMPLE_QUANTUM))/1000);
             
             synchronized (listeners)
             {
                 for (Iterator iter = listeners.iterator(); iter.hasNext();)
                 {
                     StatisticsListener listener = (StatisticsListener) iter
                             .next();
                     listener.report(average);
                 }
             }
         }
 
         previousStats = stats;
 
     }
 
     private Statistics getStatsAverages(int divisor)
     {
         Statistics totals = new Statistics();
 
         for (int i = 0; i < SAMPLES_TO_ACCUMULATE; i++)
         {
             Statistics stats = statisticsAccumulator[i];
             totals.add(stats);
         }
 
         return totals.average(divisor);
 
     }
 
     private void printStatistics(Statistics stats, String label, float interval)
     {
         statsList.put(label, stats);
         logger.info(label + ": " + ", interval=" + interval + " secs, " + stats.getQueries() + " queries, "
                 + stats.getQueries() / interval + " queries/second, "
                 + threadsAlive + "/" + stats.getThreads() + " threads, "
                 + stats.getAverageResponseTime() + " response time, "
                 + stats.getRowsRead() + " rows read,  " + stats.getUpdates()
                 + " updates, " + stats.getDeletes() + " deletes, "
                 + stats.getInserts() + " inserts");
     }
 
     private Statistics collectStatistics()
     {
         Statistics result = new Statistics();
         for (TableGroup tg : conf.getTableGroups())
         {
             for (Iterator threadsIterator = tg.getThreads().iterator(); threadsIterator
                     .hasNext();)
             {
                 ThreadConfiguration c = (ThreadConfiguration) threadsIterator
                         .next();
                 Statistics stats = c.getStatistics();
                 result.add(stats);
             }
 
         }
         return result;
     }
 
     void executeSQLIgnoreErrors(Connection conn, String statement)
     {
         Statement s = null;
         try
         {
             s = conn.createStatement();
             s.execute(statement);
         }
         catch (Exception e)
         {
             logger.error("SQL error: " + e.getMessage());
         }
         finally
         {
             if (s != null)
             {
                 try
                 {
                     s.close();
                 }
                 catch (SQLException e)
                 {
                 }
             }
         }
     }
 
     private void initializeDB() throws EvaluatorException
     {
 
         logger.info("Database initialization starting");
         for (TableGroup tableGroup : conf.getTableGroups())
         {
             initializeDB(conf, tableGroup);
         }
 
         logger.info("Database initialization complete");
     }
 
     private void initializeDB(Configuration conf, TableGroup tableGroup)
             throws EvaluatorException
     {
 
         DataStore ds = conf.getDataStore(tableGroup.getDataStoreName());
 
         if (ds == null)
         {
             ds = conf.getDataStore("default");
         }
 
         if (ds == null)
         {
             throw new EvaluatorException(
                     "No database configured for tablegroup="
                             + tableGroup.getTableName());
         }
         try
         {
             Class.forName(ds.getDriver());
         }
         catch (ClassNotFoundException e)
         {
             throw new EvaluatorException("Could not load JDBC driver", e);
         }
 
         Connection conn = getConnection(ds);
 
         try
         {
             conn.setAutoCommit(true);
             Statement s = conn.createStatement();
             String joinedTable = tableGroup.getJoinedTableName();
             String base1 = tableGroup.getBase1TableName();
             String base2 = tableGroup.getBase2TableName();
 
             String timestampType = ds.getTimestampType();
             if (tableGroup.isInitializeDDL()
                     && isTableAvailable(conn, joinedTable))
                 executeSQLIgnoreErrors(conn, "drop table " + joinedTable);
 
             if (!isTableAvailable(conn, joinedTable))
                 s.execute("create table " + joinedTable
                         + " (k1 integer, k2 integer, created " + timestampType
                         + ", " + "" + "changed " + timestampType
                         + ", value integer, primary key(k1, k2))");
             else
                 s.executeUpdate(tableGroup.getTruncateTable() + joinedTable);
 
             if (tableGroup.isInitializeDDL() && isTableAvailable(conn, base1))
                 executeSQLIgnoreErrors(conn, "drop table " + base1);
 
             if (!isTableAvailable(conn, base1))
                 s.execute("create table " + base1
                         + " (k1 integer primary key, created " + timestampType
                         + ", " + "changed " + timestampType
                         + ", value integer)");
             else
                 s.executeUpdate(tableGroup.getTruncateTable() + base1);
 
             if (tableGroup.isInitializeDDL() && isTableAvailable(conn, base2))
                 executeSQLIgnoreErrors(conn, "drop table " + base2);
 
             if (!isTableAvailable(conn, base2))
                 s.execute("create table " + base2
                         + " (k2 integer primary key, created " + timestampType
                         + ", " + "changed " + timestampType
                         + ", value integer)");
             else
                 s.executeUpdate(tableGroup.getTruncateTable() + base2);
 
             conn.setAutoCommit(ds.isAutoCommit());
             int valueRange = tableGroup.getValueRange();
             PreparedStatement joinedInsert = conn
                     .prepareStatement("insert into "
                             + joinedTable
                             + " select a.k1, b.k2, a.created, a.changed, (b.value * a.value) -(b.value * a.value "
                             + "/ " + valueRange + " * " + valueRange
                             + ") from " + base1 + " a join " + base2
                             + " b on 1 = 1");
             PreparedStatement ins1 = conn.prepareStatement("insert into "
                     + base1 + " values(?, ?, ?, ?)");
             PreparedStatement ins2 = conn.prepareStatement("insert into "
                     + base2 + " values(?, ?, ?, ?)");
             Timestamp now = new Timestamp(System.currentTimeMillis());
 
             for (int i = 0; i < tableGroup.getTableSize(); i++)
             {
                 addRow(tableGroup, ins1, now, i);
             }
             if (!conn.getAutoCommit())
             {
                 conn.commit();
             }
             for (int i = 0; i < tableGroup.getTableSize(); i++)
             {
                 int k2 = i * 1000;
                 addRow(tableGroup, ins2, now, k2);
             }
 
             if (!conn.getAutoCommit())
             {
                 conn.commit();
             }
 
             joinedInsert.executeUpdate();
             if (!conn.getAutoCommit())
             {
                 conn.commit();
             }
         }
         catch (SQLException e)
         {
             throw new EvaluatorException("Could not initialize the DB", e);
         }
         finally
         {
             if (conn != null)
             {
                 try
                 {
                     conn.close();
                 }
                 catch (SQLException e)
                 {
                     logger.error(e);
                 }
             }
         }
 
     }
 
     protected boolean isTableAvailable(Connection conn, String tableName)
             throws SQLException
     {
         ResultSet rs = conn.getMetaData().getTables(null, "%", tableName,
                 new String[]{"TABLE"});
         boolean result = rs.next();
         rs.close();
         if (!result)
         { // hsql
             rs = conn.getMetaData().getTables(null, "%",
                     tableName.toUpperCase(), new String[]{"TABLE"});
             result = rs.next();
             rs.close();
         }
         if (!result)
         { // postgres
             rs = conn.getMetaData().getTables(null, "%",
                     tableName.toLowerCase(), new String[]{"TABLE"});
             result = rs.next();
             rs.close();
         }
 
         return result;
     }
 
     protected boolean isStoredProcedureAvailable(Connection conn,
             String procName) throws SQLException
     {
         ResultSet rs = conn.getMetaData().getProcedures(null, "%", procName);
         boolean result = rs.next();
         rs.close();
         if (!result)
         {
             rs = conn.getMetaData().getProcedures(null, "%",
                     procName.toLowerCase());
             result = rs.next();
         }
         rs.close();
         return result;
     }
 
     protected boolean isRowAvailable(Statement s, String query)
             throws SQLException
     {
         ResultSet rs = s.executeQuery(query);
         boolean result = rs.next();
         rs.close();
         return result;
     }
 
     private void addRow(TableGroup tableGroup, PreparedStatement ins1,
             Timestamp now, int key) throws SQLException
     {
         int value = rand.nextInt(tableGroup.getValueRange());
 
         ins1.setInt(1, key);
         ins1.setTimestamp(2, now);
         ins1.setTimestamp(3, now);
         ins1.setInt(4, value);
         ins1.execute();
     }
 
     public Connection getConnection(String name) throws EvaluatorException
     {
         Connection conn = null;
         try
         {
             Properties props = new Properties();
             props.setProperty("user", conf.getUser());
             props.setProperty("password", conf.getPassword());
             props.setProperty("APPLICATIONNAME", name);
             logger.debug("getConnection(" + name + ") to: " + conf.getUrl());
             // logger.debug(props.toString());
             conn = DriverManager.getConnection(conf.getUrl(), props);
             conn.setAutoCommit(conf.isAutoCommit());
             if (!conf.isAutoCommit())
                 conn.commit();
         }
         catch (SQLException e)
         {
             throw new EvaluatorException("Could not connect to database", e);
         }
 
         return conn;
     }
 
     public Connection getConnection() throws EvaluatorException
     {
         DataStore ds = conf.getDataStore("default");
         if (ds == null)
         {
             throw new EvaluatorException("No default database was configured");
         }
 
         return getConnection(ds);
     }
 
     public Connection getConnection(DataStore ds) throws EvaluatorException
     {
         Connection conn = null;
         try
         {
             logger.debug("DataStore=" + ds.getName() + ", getConnection() to: "
                     + ds.getUrl());
             conn = DriverManager.getConnection(ds.getUrl(), ds.getUser(), ds
                     .getPassword());
             conn.setAutoCommit(ds.isAutoCommit());
             if (!ds.isAutoCommit())
                 conn.commit();
         }
         catch (SQLException e)
         {
             throw new EvaluatorException("Could not connect to database", e);
         }
 
         return conn;
     }
 
     public void addRow(TableGroup tableGroup, int key, Connection conn)
             throws EvaluatorException
     {
 
         PreparedStatement ins = null;
         try
         {
             ins = conn.prepareStatement("insert into "
                     + tableGroup.getBase1TableName() + " values(?, ?, ?, ?)");
             addRow(tableGroup, ins, new Timestamp(System.currentTimeMillis()),
                     key);
         }
         catch (SQLException e)
         {
             throw new EvaluatorException("Could not add row", e);
         }
         finally
         {
             try
             {
                 if (ins != null)
                 {
                     ins.close();
                 }
             }
             catch (SQLException dummy)
             {
 
             }
         }
     }
 
     public synchronized void addFailure(Throwable failure)
     {
         failures.add(failure);
     }
 
     public synchronized List getFailures()
     {
         List result = (List) failures.clone();
         return result;
     }
 
     public void addStatisticsListner(StatisticsListener listener)
     {
         synchronized (listeners)
         {
             listeners.add(listener);
         }
     }
 }
