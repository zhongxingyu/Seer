 package org.radargun.stressors;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.radargun.CacheWrapper;
 import org.radargun.producer.GroupProducerRateFactory;
 import org.radargun.producer.ProducerRate;
 import org.radargun.tpcc.ElementNotFoundException;
 import org.radargun.tpcc.TpccTerminal;
 import org.radargun.tpcc.TpccTools;
 import org.radargun.tpcc.transaction.NewOrderTransaction;
 import org.radargun.tpcc.transaction.PaymentTransaction;
 import org.radargun.tpcc.transaction.TpccTransaction;
 import org.radargun.utils.StatSampler;
 import org.radargun.utils.Utils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicLong;
 
 
 /**
  * On multiple threads executes implementations of TPC-C Transaction Profiles against the CacheWrapper, and returns the
  * result as a Map.
  *
  * @author peluso@gsd.inesc-id.pt , peluso@dis.uniroma1.it
  * @author Pedro Ruivo
  */
 public class TpccStressor extends AbstractCacheWrapperStressor {
 
    private static Log log = LogFactory.getLog(TpccStressor.class);
 
    //in milliseconds, each producer sleeps for this time in average
    private static final int AVERAGE_PRODUCER_SLEEP_TIME = 10;
 
    /**
     * the number of threads that will work on this cache wrapper.
     */
    private int numOfThreads = 10;
 
    /**
     * this node's index in the Radargun cluster.  -1 is used for local benchmarks.
     */
    private int nodeIndex = -1;
 
    /**
     * the number of nodes in the Radargun cluster.
     */
    private int numSlaves = 0;
 
    /**
     * total time (in seconds) of simulation for each stressor thread
     */
    private long perThreadSimulTime = 30L;
 
    /**
     * average arrival rate of the transactions to the system (transactions per second)
     */
    private int arrivalRate = 0;
 
    /**
     * percentage of Payment transactions
     */
    private int paymentWeight = 45;
 
    /**
     * percentage of Order Status transactions
     */
    private int orderStatusWeight = 5;
 
    /**
     * if true, each node will pick a warehouse and all transactions will work over that warehouse. The warehouses are
     * picked by order, i.e., slave 0 gets warehouse 1,N+1, 2N+1,[...]; ... slave N-1 gets warehouse N, 2N, [...].
     */
    private boolean accessSameWarehouse = false;
 
    /**
     * specify the min and the max number of items created by a New Order Transaction.
     * format: min,max
     */
    private String numberOfItemsInterval = null;
 
    /**
     * specify the interval period (in milliseconds) of the memory and cpu usage is collected
     */
    private long statsSamplingInterval = 0;
 
    /**
     * Specifies the msec a transaction spends in backoff after aborting
     */
    private long backOffTime = 0;
 
    /**
     * If true, after the abort of a transaction t of type T, a new transaction t' of type T is generated
     */
    private boolean retryOnAbort = false;
    private boolean retrySameXact = false;
 
    private CacheWrapper cacheWrapper;
    private long startTime;
    private long endTime;
    private volatile CountDownLatch startPoint;
    private BlockingQueue<RequestType> queue;
    private AtomicLong countJobs;
    private Producer[] producers;
    private StatSampler statSampler;
    private boolean running = true;
 
    private final Timer finishBenchmarkTimer = new Timer("Finish-Benchmark-Timer");
 
    private final List<Stressor> stressors = new LinkedList<Stressor>();
    private final List<Integer> listLocalWarehouses = new LinkedList<Integer>();
 
    public Map<String, String> stress(CacheWrapper wrapper) {
       if (wrapper == null) {
          throw new IllegalStateException("Null wrapper not allowed");
       }
       validateTransactionsWeight();
       updateNumberOfItemsInterval();
 
       this.cacheWrapper = wrapper;
 
       initializeToolsParameters();
 
       if (this.arrivalRate != 0.0) {     //Open system
          queue = new LinkedBlockingQueue<RequestType>();
          countJobs = new AtomicLong(0L);
 
          ProducerRate[] producerRates;
          if (cacheWrapper.isPassiveReplication()) {
             double writeWeight = Math.max(100 - orderStatusWeight, paymentWeight) / 100D;
             double readWeight = orderStatusWeight / 100D;
             if (cacheWrapper.isTheMaster()) {
                log.info("Creating producers groups for the master. Write transaction percentage is " + writeWeight);
                producerRates = new GroupProducerRateFactory(arrivalRate * writeWeight, 1, nodeIndex,
                        AVERAGE_PRODUCER_SLEEP_TIME).create();
             } else {
                log.info("Creating producers groups for the slave. Read-only transaction percentage is " + readWeight);
                producerRates = new GroupProducerRateFactory(arrivalRate * readWeight, numSlaves - 1,
                        nodeIndex == 0 ? nodeIndex : nodeIndex - 1,
                        AVERAGE_PRODUCER_SLEEP_TIME).create();
             }
          } else {
             log.info("Creating producers groups");
             producerRates = new GroupProducerRateFactory(arrivalRate, numSlaves, nodeIndex,
                     AVERAGE_PRODUCER_SLEEP_TIME).create();
          }
 
          producers = new Producer[producerRates.length];
 
          for (int i = 0; i < producerRates.length; ++i) {
             producers[i] = new Producer(producerRates[i], i);
          }
       } else {
          producers = new Producer[0];
       }
 
       if (statsSamplingInterval > 0) {
          statSampler = new StatSampler(statsSamplingInterval);
       }
 
       startTime = System.currentTimeMillis();
       log.info("Executing: " + this.toString());
 
       finishBenchmarkTimer.schedule(new TimerTask() {
          @Override
          public void run() {
             finishBenchmark();
          }
       }, perThreadSimulTime * 1000);
 
       try {
          if (this.arrivalRate != 0.0) { //Open system
             log.info("Starting " + producers.length + " producers");
             for (Producer producer : producers) {
                producer.start();
             }
          }
          if (statSampler != null) {
             statSampler.start();
          }
          executeOperations();
       } catch (Exception e) {
          throw new RuntimeException(e);
       }
       if (statSampler != null) {
          statSampler.cancel();
       }
       return processResults(stressors);
    }
 
    public void destroy() throws Exception {
       log.warn("Attention: going to destroy the wrapper");
       cacheWrapper.empty();
       cacheWrapper = null;
    }
 
    private void validateTransactionsWeight() {
       int sum = orderStatusWeight + paymentWeight;
       if (sum < 0 || sum > 100) {
          throw new IllegalArgumentException("The sum of the transactions weights must be higher or equals than zero " +
                  "and less or equals than one hundred");
       }
    }
 
    private void updateNumberOfItemsInterval() {
       if (numberOfItemsInterval == null) {
          return;
       }
       String[] split = numberOfItemsInterval.split(",");
 
       if (split.length != 2) {
          log.info("Cannot update the min and max values for the number of items in new order transactions. " +
                  "Using the default values");
          return;
       }
 
       try {
          TpccTools.NUMBER_OF_ITEMS_INTERVAL[0] = Integer.parseInt(split[0]);
       } catch (NumberFormatException nfe) {
          log.warn("Min value is not a number. " + nfe.getLocalizedMessage());
       }
 
       try {
          TpccTools.NUMBER_OF_ITEMS_INTERVAL[1] = Integer.parseInt(split[1]);
       } catch (NumberFormatException nfe) {
          log.warn("Max value is not a number. " + nfe.getLocalizedMessage());
       }
    }
 
    private void initializeToolsParameters() {
       try {
          TpccTools.C_C_LAST = (Long) cacheWrapper.get(null, "C_C_LAST");
 
          TpccTools.C_C_ID = (Long) cacheWrapper.get(null, "C_C_ID");
 
          TpccTools.C_OL_I_ID = (Long) cacheWrapper.get(null, "C_OL_ID");
 
       } catch (Exception e) {
          log.error("Error", e);
       }
    }
 
    private Map<String, String> processResults(List<Stressor> stressors) {
 
       long duration = 0;
 
       int reads = 0;
       int writes = 0;
       int newOrderTransactions = 0;
       int paymentTransactions = 0;
 
       int failures = 0;
       int rdFailures = 0;
       int wrFailures = 0;
       int nrWrFailuresOnCommit = 0;
       int newOrderFailures = 0;
       int paymentFailures = 0;
       int appFailures = 0;
 
       long readsDurations = 0L;
       long writesDurations = 0L;
       long newOrderDurations = 0L;
       long paymentDurations = 0L;
       long successful_writesDurations = 0L;
       long successful_readsDurations = 0L;
       long writeServiceTimes = 0L;
       long readServiceTimes = 0L;
       long newOrderServiceTimes = 0L;
       long paymentServiceTimes = 0L;
 
       long successful_commitWriteDurations = 0L;
       long aborted_commitWriteDurations = 0L;
       long commitWriteDurations = 0L;
 
       long writeInQueueTimes = 0L;
       long readInQueueTimes = 0L;
       long newOrderInQueueTimes = 0L;
       long paymentInQueueTimes = 0L;
       long numWritesDequeued = 0L;
       long numReadsDequeued = 0L;
       long numNewOrderDequeued = 0L;
       long numPaymentDequeued = 0L;
       long numLocalTimeout = 0L;
       long numRemoteTimeout = 0L;
 
       double backOffTime = 0D;
       double backOffs = 0D;
 
       for (Stressor stressor : stressors) {
          //duration += stressor.totalDuration(); //in nanosec
          //readsDurations += stressor.readDuration; //in nanosec
          //writesDurations += stressor.writeDuration; //in nanosec
          newOrderDurations += stressor.newOrderDuration; //in nanosec
          paymentDurations += stressor.paymentDuration; //in nanosec
          successful_writesDurations += stressor.successful_writeDuration; //in nanosec
          successful_readsDurations += stressor.successful_readDuration; //in nanosec
 
          successful_commitWriteDurations += stressor.successful_commitWriteDuration; //in nanosec
          aborted_commitWriteDurations += stressor.aborted_commitWriteDuration; //in nanosec
          commitWriteDurations += stressor.commitWriteDuration; //in nanosec;
 
          writeServiceTimes += stressor.writeServiceTime;
          readServiceTimes += stressor.readServiceTime;
          newOrderServiceTimes += stressor.newOrderServiceTime;
          paymentServiceTimes += stressor.paymentServiceTime;
 
          reads += stressor.reads;
          writes += stressor.writes;
          newOrderTransactions += stressor.newOrder;
          paymentTransactions += stressor.payment;
 
          failures += stressor.nrFailures;
          rdFailures += stressor.nrRdFailures;
          wrFailures += stressor.nrWrFailures;
          nrWrFailuresOnCommit += stressor.nrWrFailuresOnCommit;
          newOrderFailures += stressor.nrNewOrderFailures;
          paymentFailures += stressor.nrPaymentFailures;
          appFailures += stressor.appFailures;
 
          writeInQueueTimes += stressor.writeInQueueTime;
          readInQueueTimes += stressor.readInQueueTime;
          newOrderInQueueTimes += stressor.newOrderInQueueTime;
          paymentInQueueTimes += stressor.paymentInQueueTime;
          numWritesDequeued += stressor.numWriteDequeued;
          numReadsDequeued += stressor.numReadDequeued;
          numNewOrderDequeued += stressor.numNewOrderDequeued;
          numPaymentDequeued += stressor.numPaymentDequeued;
          numLocalTimeout += stressor.localTimeout;
          numRemoteTimeout += stressor.remoteTimeout;
          backOffs += stressor.numBackOffs;
          backOffTime += stressor.backedOffTime;
       }
 
       //duration = duration / 1000000; // nanosec to millisec
       //readsDurations = readsDurations / 1000; //nanosec to microsec
       //writesDurations = writesDurations / 1000; //nanosec to microsec
       //newOrderDurations = newOrderDurations / 1000; //nanosec to microsec
       //paymentDurations = paymentDurations / 1000;//nanosec to microsec
       successful_readsDurations = successful_readsDurations / 1000; //nanosec to microsec
       successful_writesDurations = successful_writesDurations / 1000; //nanosec to microsec
       successful_commitWriteDurations = successful_commitWriteDurations / 1000; //nanosec to microsec
       aborted_commitWriteDurations = aborted_commitWriteDurations / 1000; //nanosec to microsec
       commitWriteDurations = commitWriteDurations / 1000; //nanosec to microsec
       writeServiceTimes = writeServiceTimes / 1000; //nanosec to microsec
       readServiceTimes = readServiceTimes / 1000; //nanosec to microsec
       newOrderServiceTimes = newOrderServiceTimes / 1000; //nanosec to microsec
       paymentServiceTimes = paymentServiceTimes / 1000; //nanosec to microsec
 
       writeInQueueTimes = writeInQueueTimes / 1000;//nanosec to microsec
       readInQueueTimes = readInQueueTimes / 1000;//nanosec to microsec
       newOrderInQueueTimes = newOrderInQueueTimes / 1000;//nanosec to microsec
       paymentInQueueTimes = paymentInQueueTimes / 1000;//nanosec to microsec
 
       Map<String, String> results = new LinkedHashMap<String, String>();
 
       duration = endTime - startTime;
 
       results.put("DURATION (msec)", str(duration));
       double requestPerSec = (reads + writes) / (duration / 1000.0);
       results.put("REQ_PER_SEC", str(requestPerSec));
 
       double wrtPerSec = 0;
       double rdPerSec = 0;
       double newOrderPerSec = 0;
       double paymentPerSec = 0;
       double cpu = 0, mem = 0;
       if (duration == 0)
          results.put("READS_PER_SEC", str(0));
       else {
          rdPerSec = reads / (duration / 1000.0);
          results.put("READS_PER_SEC", str(rdPerSec));
       }
 
       if (duration == 0)
          results.put("WRITES_PER_SEC", str(0));
       else {
          wrtPerSec = writes / (duration / 1000.0);
          results.put("WRITES_PER_SEC", str(wrtPerSec));
       }
 
       if (duration == 0)
          results.put("NEW_ORDER_PER_SEC", str(0));
       else {
          newOrderPerSec = newOrderTransactions / (duration / 1000.0);
          results.put("NEW_ORDER_PER_SEC", str(newOrderPerSec));
       }
       if (duration == 0)
          results.put("PAYMENT_PER_SEC", str(0));
       else {
          paymentPerSec = paymentTransactions / (duration / 1000.0);
          results.put("PAYMENT_PER_SEC", str(paymentPerSec));
       }
 
       results.put("READ_COUNT", str(reads));
       results.put("WRITE_COUNT", str(writes));
       results.put("NEW_ORDER_COUNT", str(newOrderTransactions));
       results.put("PAYMENT_COUNT", str(paymentTransactions));
       results.put("FAILURES", str(failures));
       results.put("APPLICATION_FAILURES", str(appFailures));
       results.put("WRITE_FAILURES", str(wrFailures));
       results.put("NEW_ORDER_FAILURES", str(newOrderFailures));
       results.put("PAYMENT_FAILURES", str(paymentFailures));
       results.put("READ_FAILURES", str(rdFailures));
 
       if ((reads + writes) != 0)
          results.put("AVG_SUCCESSFUL_DURATION (usec)", str((successful_writesDurations + successful_readsDurations) / (reads + writes)));
       else
          results.put("AVG_SUCCESSFUL_DURATION (usec)", str(0));
 
 
       if (reads != 0)
          results.put("AVG_SUCCESSFUL_READ_DURATION (usec)", str(successful_readsDurations / reads));
       else
          results.put("AVG_SUCCESSFUL_READ_DURATION (usec)", str(0));
 
 
       if (writes != 0)
          results.put("AVG_SUCCESSFUL_WRITE_DURATION (usec)", str(successful_writesDurations / writes));
       else
          results.put("AVG_SUCCESSFUL_WRITE_DURATION (usec)", str(0));
 
 
       if (writes != 0) {
          results.put("AVG_SUCCESSFUL_COMMIT_WRITE_DURATION (usec)", str((successful_commitWriteDurations / writes)));
       } else {
          results.put("AVG_SUCCESSFUL_COMMIT_WRITE_DURATION (usec)", str(0));
       }
 
       if (nrWrFailuresOnCommit != 0) {
          results.put("AVG_ABORTED_COMMIT_WRITE_DURATION (usec)", str((aborted_commitWriteDurations / nrWrFailuresOnCommit)));
       } else {
          results.put("AVG_ABORTED_COMMIT_WRITE_DURATION (usec)", str(0));
       }
 
 
       if (writes + nrWrFailuresOnCommit != 0) {
          results.put("AVG_COMMIT_WRITE_DURATION (usec)", str((commitWriteDurations / (writes + nrWrFailuresOnCommit))));
       } else {
          results.put("AVG_COMMIT_WRITE_DURATION (usec)", str(0));
       }
 
       if ((reads + rdFailures) != 0)
          results.put("AVG_RD_SERVICE_TIME (usec)", str(readServiceTimes / (reads + rdFailures)));
       else
          results.put("AVG_RD_SERVICE_TIME (usec)", str(0));
 
       if ((writes + wrFailures) != 0)
          results.put("AVG_WR_SERVICE_TIME (usec)", str(writeServiceTimes / (writes + wrFailures)));
       else
          results.put("AVG_WR_SERVICE_TIME (usec)", str(0));
 
       if ((newOrderTransactions + newOrderFailures) != 0)
          results.put("AVG_NEW_ORDER_SERVICE_TIME (usec)", str(newOrderServiceTimes / (newOrderTransactions + newOrderFailures)));
       else
          results.put("AVG_NEW_ORDER_SERVICE_TIME (usec)", str(0));
 
       if ((paymentTransactions + paymentFailures) != 0)
          results.put("AVG_PAYMENT_SERVICE_TIME (usec)", str(paymentServiceTimes / (paymentTransactions + paymentFailures)));
       else
          results.put("AVG_PAYMENT_SERVICE_TIME (usec)", str(0));
 
       if (numWritesDequeued != 0)
          results.put("AVG_WR_INQUEUE_TIME (usec)", str(writeInQueueTimes / numWritesDequeued));
       else
          results.put("AVG_WR_INQUEUE_TIME (usec)", str(0));
       if (numReadsDequeued != 0)
          results.put("AVG_RD_INQUEUE_TIME (usec)", str(readInQueueTimes / numReadsDequeued));
       else
          results.put("AVG_RD_INQUEUE_TIME (usec)", str(0));
       if (numNewOrderDequeued != 0)
          results.put("AVG_NEW_ORDER_INQUEUE_TIME (usec)", str(newOrderInQueueTimes / numNewOrderDequeued));
       else
          results.put("AVG_NEW_ORDER_INQUEUE_TIME (usec)", str(0));
       if (numPaymentDequeued != 0)
          results.put("AVG_PAYMENT_INQUEUE_TIME (usec)", str(paymentInQueueTimes / numPaymentDequeued));
       else
          results.put("AVG_PAYMENT_INQUEUE_TIME (usec)", str(0));
       if (numLocalTimeout != 0)
          results.put("LOCAL_TIMEOUT", str(numLocalTimeout));
       else
          results.put("LOCAL_TIMEOUT", str(0));
       if (numRemoteTimeout != 0)
         results.put("REMOTE_TIMEOUT", str(numLocalTimeout));
       else
          results.put("REMOTE_TIMEOUT", str(0));
       if (backOffs != 0)
          results.put("AVG_BACKOFF", str(backOffTime / backOffs));
       else
          results.put("AVG_BACKOFF", str(0));
 
       results.put("NumThreads", str(numOfThreads));
 
       if (statSampler != null) {
          cpu = statSampler.getAvgCpuUsage();
          mem = statSampler.getAvgMemUsage();
       }
       results.put("CPU_USAGE", str(cpu));
       results.put("MEMORY_USAGE", str(mem));
       results.putAll(cacheWrapper.getAdditionalStats());
       results.put("TEST_ID",this.testIdString(paymentWeight,orderStatusWeight,numOfThreads));
       saveSamples();
 
       log.info("Sending map to master " + results.toString());
 
       log.info("Finished generating report. Nr of failed operations on this node is: " + failures +
               ". Test duration is: " + Utils.getMillisDurationString(System.currentTimeMillis() - startTime));
       return results;
    }
 
    private List<Stressor> executeOperations() throws Exception {
       calculateLocalWarehouses();
 
       startPoint = new CountDownLatch(1);
       for (int threadIndex = 0; threadIndex < numOfThreads; threadIndex++) {
          Stressor stressor = createStressor(threadIndex);
          stressors.add(stressor);
          stressor.start();
       }
       log.info("Cache wrapper info is: " + cacheWrapper.getInfo());
       startPoint.countDown();
       blockWhileRunning();
       for (Stressor stressor : stressors) {
          stressor.join();
       }
 
       endTime = System.currentTimeMillis();
       return stressors;
    }
 
    private class Stressor extends Thread {
       private int threadIndex;
       private double arrivalRate;
 
       private final TpccTerminal terminal;
 
       private int nrFailures = 0;
       private int nrWrFailures = 0;
       private int nrWrFailuresOnCommit = 0;
       private int nrRdFailures = 0;
       private int nrNewOrderFailures = 0;
       private int nrPaymentFailures = 0;
       private int appFailures = 0;
 
       private long readDuration = 0L;
       private long writeDuration = 0L;
       private long newOrderDuration = 0L;
       private long paymentDuration = 0L;
       private long successful_commitWriteDuration = 0L;
       private long aborted_commitWriteDuration = 0L;
       private long commitWriteDuration = 0L;
 
       private long writeServiceTime = 0L;
       private long newOrderServiceTime = 0L;
       private long paymentServiceTime = 0L;
       private long readServiceTime = 0L;
 
       private long successful_writeDuration = 0L;
       private long successful_readDuration = 0L;
 
       private long reads = 0L;
       private long writes = 0L;
       private long payment = 0L;
       private long newOrder = 0L;
 
       private long numWriteDequeued = 0L;
       private long numReadDequeued = 0L;
       private long numNewOrderDequeued = 0L;
       private long numPaymentDequeued = 0L;
 
       private long writeInQueueTime = 0L;
       private long readInQueueTime = 0L;
       private long newOrderInQueueTime = 0L;
       private long paymentInQueueTime = 0L;
 
       private boolean running = true;
       private boolean active = true;
 
       private ProducerRate backOffSleeper;
       private long localTimeout = 0L;
       private long remoteTimeout = 0L;
       private long numBackOffs = 0L;
       private long backedOffTime = 0L;
 
       public Stressor(int localWarehouseID, int threadIndex, int nodeIndex, double arrivalRate,
                       double paymentWeight, double orderStatusWeight) {
          super("Stressor-" + threadIndex);
          this.threadIndex = threadIndex;
          this.arrivalRate = arrivalRate;
          this.terminal = new TpccTerminal(paymentWeight, orderStatusWeight, nodeIndex, localWarehouseID);
          if (backOffTime > 0)
             this.backOffSleeper = new ProducerRate(Math.pow((double) backOffTime, -1D));
       }
 
       private TpccTransaction regenerate(TpccTransaction oldTransaction, int threadIndex, boolean lastSuccessful) {
 
          if (!lastSuccessful && !retrySameXact) {
             this.backoffIfNecessary();
             TpccTransaction newTransaction = terminal.createTransaction(oldTransaction.getType(), threadIndex);
             log.info("Thread " + threadIndex + ": regenerating a transaction of type " + oldTransaction.getType() + " into a transaction of type " + newTransaction.getType());
             return newTransaction;
          }
          //If this is the first time xact runs or exact retry on abort is enabled...
          return oldTransaction;
       }
 
       @Override
       public void run() {
 
          try {
             startPoint.await();
             log.info("Starting thread: " + getName());
          } catch (InterruptedException e) {
             log.warn("Interrupted while waiting for starting in " + getName());
          }
 
          long end, endInQueueTime, boffTime, commit_start = 0L;
          boolean isReadOnly, successful, measureCommitTime = false;
          TpccTransaction transaction;
 
 
          while (assertRunning()) {
 
 
             transaction = null;
 
             long start = -1;
             if (arrivalRate != 0.0) {  //Open system
                try {
                   RequestType request = queue.take();
 
                   endInQueueTime = System.nanoTime();
 
                   if (request.transactionType == TpccTerminal.NEW_ORDER) {
                      numWriteDequeued++;
                      numNewOrderDequeued++;
                      writeInQueueTime += endInQueueTime - request.timestamp;
                      newOrderInQueueTime += endInQueueTime - request.timestamp;
                   } else if (request.transactionType == TpccTerminal.PAYMENT) {
                      numWriteDequeued++;
                      numPaymentDequeued++;
                      writeInQueueTime += endInQueueTime - request.timestamp;
                      paymentInQueueTime += endInQueueTime - request.timestamp;
                   } else if (request.transactionType == TpccTerminal.ORDER_STATUS) {
                      numReadDequeued++;
                      readInQueueTime += endInQueueTime - request.timestamp;
                   }
 
                   transaction = terminal.createTransaction(request.transactionType, threadIndex);
                   if (cacheWrapper.isPassiveReplication() &&
                           ((cacheWrapper.isTheMaster() && transaction.isReadOnly()) ||
                                   (!cacheWrapper.isTheMaster() && !transaction.isReadOnly()))) {
                      continue;
                   }
                   start = request.timestamp;
                } catch (InterruptedException ir) {
                   log.error("»»»»»»»THREAD INTERRUPTED WHILE TRYING GETTING AN OBJECT FROM THE QUEUE«««««««");
                }
             } else {
                transaction = terminal.choiceTransaction(cacheWrapper.isPassiveReplication(), cacheWrapper.isTheMaster(), threadIndex);
                log.info("Closed system: starting a brand new transaction of type " + transaction.getType());
             }
             isReadOnly = transaction.isReadOnly();
 
             long startService = System.nanoTime();
             boolean elementNotFoundExceptionThrown = false;
             successful = true; //so that backOffIfNecessary returns false the first time we run an xact
             do {
                transaction = regenerate(transaction, threadIndex, successful);
                successful = true;
                cacheWrapper.startTransaction();
                try {
                   transaction.executeTransaction(cacheWrapper);
                   log.info("Thread " + threadIndex + " successfully completed locally a transaction of type " + transaction.getType() + " btw, successful is " + successful);
                } catch (Throwable e) {
                   successful = false;
                   if (log.isDebugEnabled()) {
                      log.debug("Exception while executing transaction.", e);
                   } else {
                      log.warn("Exception while executing transaction of type: " + transaction.getType() + " " + e.getMessage());
                   }
                   if (e instanceof ElementNotFoundException) {
                      this.appFailures++;
                      elementNotFoundExceptionThrown = true;
                   } else if (cacheWrapper.isTimeoutException(e))
                      localTimeout++;
 
                }
 
                //here we try to finalize the transaction
                //if any read/write has failed we abort
 
                try {
                /* In our tests we are interested in the commit time spent for write txs*/
                   if (successful && !isReadOnly) {
                      commit_start = System.nanoTime();
                      measureCommitTime = true;
                   }
                   cacheWrapper.endTransaction(successful, threadIndex);
                   if (successful)
                      log.info("Thread " + threadIndex + " successfully completed remotely a transaction of type " + transaction.getType() + " Btw, successful is " + successful);
                   if (!successful) {
                      nrFailures++;
                      if (!isReadOnly) {
                         nrWrFailures++;
                         if (transaction instanceof NewOrderTransaction) {
                            nrNewOrderFailures++;
                         } else if (transaction instanceof PaymentTransaction) {
                            nrPaymentFailures++;
                         }
                      } else {
                         nrRdFailures++;
                      }
 
                   }
                } catch (Throwable rb) {
                   nrFailures++;
                   //if (cacheWrapper.isTimeoutException(rb))
                   remoteTimeout++;
                   successful = false;
                   if (!isReadOnly) {
                      nrWrFailures++;
                      nrWrFailuresOnCommit++;
                      if (transaction instanceof NewOrderTransaction) {
                         nrNewOrderFailures++;
                      } else if (transaction instanceof PaymentTransaction) {
                         nrPaymentFailures++;
                      }
                   } else {
                      nrRdFailures++;
                   }
                   if (log.isDebugEnabled()) {
                      log.debug("Error while committing", rb);
                   } else {
                      log.warn("Error while committing: " + rb.getMessage());
                   }
                }
                log.info("Successful = " + successful + " elementNotFoundException " + elementNotFoundExceptionThrown);
             }
             //If we experience an elementNotFoundException we do not want to restart the very same xact!!
             while (retryOnAbort && !successful && !elementNotFoundExceptionThrown);
 
             end = System.nanoTime();
 
             if (this.arrivalRate == 0.0) {  //Closed system   --> no queueing time
                start = startService;
             }
 
             if (!isReadOnly) {
                writeDuration += end - start;
                writeServiceTime += end - startService;
                if (transaction instanceof NewOrderTransaction) {
                   newOrderDuration += end - start;
                   newOrderServiceTime += end - startService;
                } else if (transaction instanceof PaymentTransaction) {
                   paymentDuration += end - start;
                   paymentServiceTime += end - startService;
                }
                if (successful) {
                   successful_writeDuration += end - startService;
                   writes++;
                   if (transaction instanceof PaymentTransaction) {
                      payment++;
                   } else if (transaction instanceof NewOrderTransaction) {
                      newOrder++;
                   }
                }
             } else {
                readDuration += end - start;
                readServiceTime += end - startService;
                if (successful) {
                   reads++;
                   successful_readDuration += end - startService;
                }
             }
 
             if (measureCommitTime) {    //We sample just the last commit time, i.e., the successful one
                if (successful) {
                   this.successful_commitWriteDuration += end - commit_start;
                } else {
                   this.aborted_commitWriteDuration += end - commit_start;
                }
                this.commitWriteDuration += end - commit_start;
             }
 
             blockIfInactive();
          }
       }
 
 
       private void backoffIfNecessary() {
          if (backOffTime != 0) {
             this.numBackOffs++;
             long backedOff = backOffSleeper.sleep();
             log.info("Thread " + this.threadIndex + " backed off for " + backedOff + " msec");
             this.backedOffTime += backedOff;
          }
       }
 
       private boolean startNewTransaction(boolean lastXactSuccessul) {
          return !retryOnAbort || lastXactSuccessul;
       }
 
       public long totalDuration() {
          return readDuration + writeDuration;
       }
 
       private synchronized boolean assertRunning() {
          return running;
       }
 
       public final synchronized void inactive() {
          active = false;
       }
 
       public final synchronized void active() {
          active = true;
          notifyAll();
       }
 
       public final synchronized void finish() {
          active = true;
          running = false;
          notifyAll();
       }
 
       public final synchronized boolean isActive() {
          return active;
       }
 
       private synchronized void blockIfInactive() {
          while (!active) {
             try {
                wait();
             } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
             }
          }
       }
    }
 
    private void saveSamples() {
       if (statSampler == null) {
          return;
       }
       log.info("Saving samples in the file sample-" + nodeIndex);
       File f = new File("sample-" + nodeIndex);
       try {
          BufferedWriter bw = new BufferedWriter(new FileWriter(f));
          List<Long> mem = statSampler.getMemoryUsageHistory();
          List<Double> cpu = statSampler.getCpuUsageHistory();
 
          int size = Math.min(mem.size(), cpu.size());
          bw.write("#Time (milliseconds)\tCPU(%)\tMemory(bytes)");
          bw.newLine();
          for (int i = 0; i < size; ++i) {
             bw.write((i * statsSamplingInterval) + "\t" + cpu.get(i) + "\t" + mem.get(i));
             bw.newLine();
          }
          bw.flush();
          bw.close();
       } catch (IOException e) {
          log.warn("IOException caught while saving sampling: " + e.getMessage());
       }
    }
 
    private class Producer extends Thread {
       private final ProducerRate rate;
       private final TpccTerminal terminal;
       private boolean running = true;
 
       public Producer(ProducerRate rate, int id) {
          super("Producer-" + id);
          setDaemon(true);
          this.rate = rate;
          this.terminal = new TpccTerminal(paymentWeight, orderStatusWeight, nodeIndex, 0);
       }
 
       public void run() {
          if (log.isDebugEnabled()) {
             log.debug("Starting " + getName() + " with rate of " + rate.getLambda());
          }
          while (assertRunning()) {
             try {
                queue.offer(new RequestType(System.nanoTime(), terminal.chooseTransactionType(
                        cacheWrapper.isPassiveReplication(), cacheWrapper.isTheMaster()
                )));
                countJobs.incrementAndGet();
                rate.sleep();
             } catch (IllegalStateException il) {
                log.error("»»»»»»»IllegalStateException«««««««««", il);
             }
          }
       }
 
       private synchronized boolean assertRunning() {
          return running;
       }
 
       @Override
       public synchronized void start() {
          running = true;
          super.start();
       }
 
       @Override
       public synchronized void interrupt() {
          running = false;
          super.interrupt();
       }
    }
 
    private class RequestType {
 
       private long timestamp;
       private int transactionType;
 
       public RequestType(long timestamp, int transactionType) {
          this.timestamp = timestamp;
          this.transactionType = transactionType;
       }
 
    }
 
    private String str(Object o) {
       return String.valueOf(o);
    }
 
    public void setNumOfThreads(int numOfThreads) {
       this.numOfThreads = numOfThreads;
    }
 
    public void setNodeIndex(int nodeIndex) {
       this.nodeIndex = nodeIndex;
    }
 
    public void setNumSlaves(int value) {
       this.numSlaves = value;
    }
 
    public void setPerThreadSimulTime(long perThreadSimulTime) {
       this.perThreadSimulTime = perThreadSimulTime;
    }
 
    public void setArrivalRate(int arrivalRate) {
       this.arrivalRate = arrivalRate;
    }
 
    public void setRetryOnAbort(boolean retryOnAbort) {
       this.retryOnAbort = retryOnAbort;
    }
 
    public void setRetrySameXact(boolean b) {
       this.retrySameXact = b;
    }
 
    public void setBackOffTime(long backOffTime) {
       this.backOffTime = backOffTime;
 
    }
 
    public void setPaymentWeight(int paymentWeight) {
       this.paymentWeight = paymentWeight;
    }
 
    public void setOrderStatusWeight(int orderStatusWeight) {
       this.orderStatusWeight = orderStatusWeight;
    }
 
    public void setAccessSameWarehouse(boolean accessSameWarehouse) {
       this.accessSameWarehouse = accessSameWarehouse;
    }
 
    public void setNumberOfItemsInterval(String numberOfItemsInterval) {
       this.numberOfItemsInterval = numberOfItemsInterval;
    }
 
    public void setStatsSamplingInterval(long statsSamplingInterval) {
       this.statsSamplingInterval = statsSamplingInterval;
    }
 
    @Override
    public String toString() {
       return "TpccStressor{" +
               "perThreadSimulTime=" + perThreadSimulTime +
               ", arrivalRate=" + arrivalRate +
               ", paymentWeight=" + paymentWeight +
               ", orderStatusWeight=" + orderStatusWeight +
               ", accessSameWarehouse=" + accessSameWarehouse +
               ", numSlaves=" + numSlaves +
               ", nodeIndex=" + nodeIndex +
               ", numOfThreads=" + numOfThreads +
               ", numberOfItemsInterval=" + numberOfItemsInterval +
               ", statsSamplingInterval=" + statsSamplingInterval +
               '}';
    }
 
    private synchronized void finishBenchmark() {
       if (!running) {
          return;
       }
       running = false;
       for (Stressor stressor : stressors) {
          stressor.finish();
       }
       for (Producer producer : producers) {
          producer.interrupt();
       }
       notifyAll();
    }
 
    public final synchronized void setNumberOfRunningThreads(int numOfThreads) {
       if (numOfThreads < 1 || !running) {
          return;
       }
       Iterator<Stressor> iterator = stressors.iterator();
       while (numOfThreads > 0 && iterator.hasNext()) {
          Stressor stressor = iterator.next();
          if (!stressor.isActive()) {
             stressor.active();
          }
          numOfThreads--;
       }
 
       if (numOfThreads > 0) {
          int threadIdx = stressors.size();
          while (numOfThreads-- > 0) {
             Stressor stressor = createStressor(threadIdx++);
             stressor.start();
             stressors.add(stressor);
          }
       } else {
          while (iterator.hasNext()) {
             iterator.next().inactive();
          }
       }
    }
 
    public final synchronized int getNumberOfThreads() {
       return stressors.size();
    }
 
    public final synchronized int getNumberOfActiveThreads() {
       int count = 0;
       for (Stressor stressor : stressors) {
          if (stressor.isActive()) {
             count++;
          }
       }
       return count;
    }
 
 
    private Stressor createStressor(int threadIndex) {
       int localWarehouse = getWarehouseForThread(threadIndex);
       return new Stressor(localWarehouse, threadIndex, nodeIndex, arrivalRate, paymentWeight, orderStatusWeight);
    }
 
    private void calculateLocalWarehouses() {
       if (accessSameWarehouse) {
          TpccTools.selectLocalWarehouse(numSlaves, nodeIndex, listLocalWarehouses);
          if (log.isDebugEnabled()) {
             log.debug("Find the local warehouses. Number of Warehouses=" + TpccTools.NB_WAREHOUSES + ", number of slaves=" +
                     numSlaves + ", node index=" + nodeIndex + ".Local warehouses are " + listLocalWarehouses);
          }
       } else {
          if (log.isDebugEnabled()) {
             log.debug("Local warehouses are disabled. Choose a random warehouse in each transaction");
          }
       }
    }
 
    private synchronized void blockWhileRunning() throws InterruptedException {
       while (running) {
          wait();
       }
    }
 
    /*
     * For the review, the workload is the following:
     * 
     * high contention: change(1, 85, 10);
     * low contention: change(nodeIndex + 1, 45, 50); 
     */
 
    public synchronized final void highContention(int payment, int order) {
       if (!running) {
          return;
       }
       paymentWeight = payment;
       orderStatusWeight = order;
 
       log.info("Change to high contention workload:");
       for (Stressor stressor : stressors) {
          stressor.terminal.change(1, paymentWeight, orderStatusWeight);
          log.info(stressor.getName() + " terminal is " + stressor.terminal);
       }
       for (Producer producer : producers) {
          producer.terminal.change(1, paymentWeight, orderStatusWeight);
          log.info(producer.getName() + " terminal is " + producer.terminal);
       }
    }
 
    public synchronized final void lowContention(int payment, int order) {
       if (!running) {
          return;
       }
       if (listLocalWarehouses.isEmpty()) {
          TpccTools.selectLocalWarehouse(numSlaves, nodeIndex, listLocalWarehouses);
       }
       paymentWeight = payment;
       orderStatusWeight = order;
 
       log.info("Change to low contention workload:");
       for (Stressor stressor : stressors) {
          stressor.terminal.change(getWarehouseForThread(stressor.threadIndex), paymentWeight, orderStatusWeight);
          log.info(stressor.getName() + " terminal is " + stressor.terminal);
       }
       for (Producer producer : producers) {
          //in the producers, the warehouse is not needed
          producer.terminal.change(-1, paymentWeight, orderStatusWeight);
          log.info(producer.getName() + " terminal is " + producer.terminal);
       }
    }
 
    public synchronized final void randomContention(int payment, int order) {
       if (!running) {
          return;
       }
       paymentWeight = payment;
       orderStatusWeight = order;
 
       log.info("Change to random contention workload:");
       for (Stressor stressor : stressors) {
          stressor.terminal.change(-1, paymentWeight, orderStatusWeight);
          log.info(stressor.getName() + " terminal is " + stressor.terminal);
       }
       for (Producer producer : producers) {
          producer.terminal.change(-1, paymentWeight, orderStatusWeight);
          log.info(producer.getName() + " terminal is " + producer.terminal);
       }
    }
 
    private int getWarehouseForThread(int threadIdx) {
       return listLocalWarehouses.isEmpty() ? -1 : listLocalWarehouses.get(threadIdx % listLocalWarehouses.size());
    }
 
    public synchronized final double getExpectedWritePercentage() {
       return 1.0 - (orderStatusWeight / 100.0);
    }
 
    public synchronized final int getPaymentWeight() {
       return paymentWeight;
    }
 
    public synchronized final int getOrderStatusWeight() {
       return orderStatusWeight;
    }
 
    public synchronized final void stopBenchmark() {
       finishBenchmarkTimer.cancel();
       finishBenchmark();
    }
 
 
    private String testIdString(long payment, long orderStatus, long threads){
      return threads+"T_"+payment+"PA_"+orderStatus+"OS";
    }
 
 }
