 package net.thumbtack.research.nosql.scenarios;
 
 import net.thumbtack.research.nosql.Configurator;
 import net.thumbtack.research.nosql.clients.Client;
 import net.thumbtack.research.nosql.report.AggregatedReporter;
 import net.thumbtack.research.nosql.report.Reporter;
 import org.javasimon.Split;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.nio.ByteBuffer;
 import java.util.UUID;
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 /**
  * User: vkornev
  * Date: 13.08.13
  * Time: 10:45
  * <p/>
  * Test database consistency. This synthetic test for checking database consistency.
  * This is test try to do more then one reads and get new values of record before old values of record in same time.
  * <p/>
  * Some usage advices:
  * <ul>
  * <li>count of servers in db.hosts parameter must be more then one
  * <li>set threads count as (x+1) * n, x - is count of servers in db.hosts parameters, and n - is count of writers
  * <li>set read and write consistency to ALL
  * </ul>
  */
 public final class ConsistencyBScenario extends Scenario {
     private static final Logger log = LoggerFactory.getLogger(ConsistencyBScenario.class);
     private static final Logger detailedLog = LoggerFactory.getLogger("detailed");
     private static final Logger rawLog = LoggerFactory.getLogger("rawLog");
     private static final String READ_TRIES_PROPERTY = "consistency_b.readTries";
     private static final int DEFAULT_READ_TRIES = 2;
     private static final String WRITE_DELAY_PROPERTY = "consistency_b.writeDelay";
     private static final int DEFAULT_WRITE_DELAY = 0;
 
     private static final String VALE_COLUMN = "1";
     private static final String DATA_COLUMN = "2";
 
     private static int roleIdx = 0;
     private static int rolesCount = 0;
     private static int readersCount = 0;
     private static String groupKey;
     private static Map<Long, ByteBuffer> groupReadValues;
     private static Semaphore groupReadSemaphore;
     private static Semaphore groupAggrSemaphore;
 
     private enum Role {
         writer, reader;
     }
 
     private String key;
     private Role role;
     private long value;
     private Map<String, ByteBuffer> writeValues;
     private Set<String> readColumns;
     private Map<Long, ByteBuffer> readValues;
     private Semaphore readSemaphore;
     private Semaphore aggrSemaphore;
     private int readTries;
     private int writeDelay;
 
     @Override
     public void init(Client client, Configurator config) {
         log.debug("Init base scenario");
         super.init(client, config);
         synchronized (ConsistencyBScenario.class) {
             log.debug("Init scenario consistency_b");
             if (rolesCount == 0) {
                 readersCount = config.getDbHosts().length;
                 rolesCount = readersCount + 1;
             }
             this.writesCount = config.getScWrites() / (config.getScThreads() / rolesCount);
             log.debug("Writes by thread {}", this.writesCount);
             role = getRole();
             if (role.equals(Role.writer)) {
                 log.debug("Init role writer");
                 groupKey = UUID.randomUUID().toString();
                 groupReadValues = new LinkedHashMap<>();
                 groupReadSemaphore = new Semaphore(0);
                 groupAggrSemaphore = new Semaphore(0);
                 value = 0;
                 writeValues = new HashMap<>();
                 writeValues.put(DATA_COLUMN, ss.toByteBuffer(generateString()));
                 writeDelay = config.getInt(WRITE_DELAY_PROPERTY, DEFAULT_WRITE_DELAY);
             }
             key = groupKey;
             readValues = groupReadValues;
             readSemaphore = groupReadSemaphore;
             aggrSemaphore = groupAggrSemaphore;
 
             if (role.equals(Role.reader)) {
                 log.debug("Init role reader");
                 readColumns = new HashSet<>();
                 readColumns.add(VALE_COLUMN);
                 readTries = config.getInt(READ_TRIES_PROPERTY, DEFAULT_READ_TRIES);
             }
 
             log.debug("Create consistency_b scenario with role " + role.name() + " and key " + key);
         }
     }
 
     @Override
     protected void action() throws Exception {
         if (role.equals(Role.writer)) {
             readSemaphore.release(readersCount);
            Thread.sleep(writeDelay);
             write();
             aggrSemaphore.acquire(readersCount);
             aggregation();
         } else {
             readSemaphore.acquire(1);
             for (int i=0; i<readTries; i++) {
                 read();
                 Thread.yield();
             }
             aggrSemaphore.release(1);
         }
     }
 
     @Override
     public void close() {
         super.close();
         try {
             db.close();
         } catch (Exception e) {
             e.printStackTrace();
             log.error(e.getMessage());
         }
     }
 
     private Role getRole() {
         if (roleIdx >= rolesCount) {
             roleIdx = 0;
         }
         if (roleIdx++ == 0) {
             return Role.writer;
         }
         return Role.reader;
     }
 
     private void write() throws Exception {
         writeValues.put(VALE_COLUMN, ls.toByteBuffer(value));
 
         Split writeSplit = Reporter.startEvent();
         db.write(key, writeValues);
         onWrite(writeSplit);
         value++;
     }
 
     private void read() throws Exception {
         synchronized (key) {
             Split readSplit = Reporter.startEvent();
             Map<String, ByteBuffer> data = db.read(key, readColumns);
             readValues.put(System.nanoTime(), data.get(VALE_COLUMN));
             onRead(readSplit);
         }
     }
 
     private void aggregation() {
         StringBuilder detailedString = new StringBuilder();
         long oldTimestamp = 0;
         long firstValue = 0;
         for (Long time : readValues.keySet()) {
             long value = 0L;
             ByteBuffer buffer = readValues.get(time);
             if (buffer != null) {
                 value = ls.fromByteBuffer(readValues.get(time));
             }
             if (oldTimestamp == 0) {
                 oldTimestamp = value;
                 firstValue = value;
             }
             if (oldTimestamp > value) {
                 Reporter.addEvent(Reporter.STOPWATCH_FAILURE);
                 AggregatedReporter.addEvent(AggregatedReporter.EVENT_OLD_VALUE);
             }
             if (detailedLog.isDebugEnabled()) {
                 if (firstValue == value) {
                     detailedString.append("1\t");
                 } else if (firstValue < value) {
                     detailedString.append("2\t");
                 } else {
                     detailedString.append("0\t");
                 }
             }
             if (rawLog.isDebugEnabled()) {
                 rawLog.debug(key + "\t{}\t{}", time, value);
             }
             oldTimestamp = value;
         }
         if (detailedLog.isDebugEnabled()) {
             detailedLog.debug("{}\t{}", key, detailedString.toString());
         }
 
         readValues.clear();
     }
 
 }
