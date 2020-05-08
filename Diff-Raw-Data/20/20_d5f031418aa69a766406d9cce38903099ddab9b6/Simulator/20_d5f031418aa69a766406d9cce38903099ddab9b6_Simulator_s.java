 package ernst.simulator;
 
 import java.lang.Math;
 import java.util.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.CountDownLatch;
 
 import ernst.solver.FileLatencyModel;
 import ernst.solver.LatencyModel;
 import ernst.solver.LatencyModelValidator;
 
 /*
     LEGACY CLASSES, PORTED
  */
 
 class ReadOutput
 {
     int version_at_start;
     int version_read;
     long start_time;
 
     public int getVersion_at_start() {
         return version_at_start;
     }
 
     public int getVersion_read() {
         return version_read;
     }
 
     public long getStart_time() {
         return start_time;
     }
 
     public ReadOutput(int version_at_start, int version_read, long start_time)
     {
         this.version_at_start = version_at_start;
         this.version_read = version_read;
         this.start_time = start_time;
     }
 }
 
 class ReadPlot implements Comparable
 {
     ReadOutput read;
     long commit_time_at_start;
 
     public ReadOutput getRead() {
         return read;
     }
 
     public long getCommit_time_at_start() {
         return commit_time_at_start;
     }
 
     public ReadPlot(ReadOutput read, long commit_time_at_start)
     {
         this.read = read;
         this.commit_time_at_start = commit_time_at_start;
     }
 
     public int compareTo(Object anotherPlot) throws ClassCastException
     {
         if(!(anotherPlot instanceof ReadPlot))
             throw new ClassCastException("A ReadPlot object expected.");
 
         ReadPlot comparePlot = ((ReadPlot) anotherPlot);
 
         long thisdelta = this.read.getStart_time()-this.commit_time_at_start;
         long theirdelta = comparePlot.getRead().getStart_time()-comparePlot.getCommit_time_at_start();
 
         if(thisdelta < theirdelta)
             return -1;
         else if(thisdelta == theirdelta)
             return 0;
         else
             return 1;
     }
 }
 
 /*
     END LEGACY CLASSES
  */
 
 interface DelayModel
 {
     public long getWriteSendDelay();
     public long getReadSendDelay();
     public long getWriteAckDelay();
     public long getReadAckDelay();
 }
 
 class ParetoDelayModel implements DelayModel
 {
     double wmin, walpha, arsmin, arsalpha;
     Random rand;
     public ParetoDelayModel(double wmin, double walpha, double arsmin, double arsalpha)
     {
         this.wmin = wmin;
         this.walpha = walpha;
         this.arsmin = arsmin;
         this.arsalpha = arsalpha;
         this.rand = new Random();
     }
 
     long getNextPareto(double m, double a)
     {
         return Math.round(m / Math.pow(rand.nextDouble(), 1/a));
     }
 
     public long getWriteSendDelay()
     {
         return getNextPareto(wmin, walpha);
     }
 
     public long getReadSendDelay()
     {
         return getNextPareto(arsmin, arsalpha);
     }
 
     public long getWriteAckDelay()
     {
         return getReadSendDelay();
     }
 
     public long getReadAckDelay()
     {
         return getReadSendDelay();
     }
 }
 
 
 class ExponentialDelayModel implements DelayModel
 {
     double wlambda, arslambda;
     Random rand;
     public ExponentialDelayModel(double wlambda, double arslambda)
     {
         this.wlambda = wlambda;
         this.arslambda = arslambda;
         this.rand = new Random();
     }
 
     long getNextExponential(double lambda)
     {
         return Math.round(Math.log(1-rand.nextDouble())/(-lambda));
     }
 
     public long getWriteSendDelay()
     {
         return getNextExponential(wlambda);
     }
 
     public long getReadSendDelay()
     {
         return getNextExponential(arslambda);
     }
 
     public long getWriteAckDelay()
     {
         return getReadSendDelay();
     }
 
     public long getReadAckDelay()
     {
         return getReadSendDelay();
     }
 }
 
 class EmpiricalDelayModel implements DelayModel
 {
     LatencyModel ackLatencyModel;
     LatencyModel sendLatencyModel;
 
     Random rand;
 
     //empirical distribution
     public EmpiricalDelayModel(String sendF, String writeF)
     {
       rand = new Random();
 
       try{
         sendLatencyModel = new FileLatencyModel(sendF);
         ackLatencyModel = new FileLatencyModel(writeF);
         LatencyModelValidator.ValidateModel(sendLatencyModel);
         LatencyModelValidator.ValidateModel(ackLatencyModel);
        }
        catch (Exception e)
        {
         System.out.println("BAD LATENCY MODEL; EXITING");
         System.out.println(e.getMessage());
         System.exit(-1);
        }
     }
 
     public long getWriteSendDelay() {
       return Math.round(sendLatencyModel.getInverseCDF(1,
           rand.nextDouble()));
     }
 
     public long getReadSendDelay() { return getWriteAckDelay(); };
 
     public long getWriteAckDelay() {
       return Math.round(ackLatencyModel.getInverseCDF(1,
           rand.nextDouble()));
     }
 
     public long getReadAckDelay() { return getWriteAckDelay(); };
 }
 
 class ReadInstance implements Comparable
 {
     int version;
     long finishtime;
 
     public int getVersion() {
         return version;
     }
 
     public long getFinishtime() {
         return finishtime;
     }
 
     public ReadInstance(int version, long finishtime)
     {
         this.version = version;
         this.finishtime = finishtime;
     }
 
     public int compareTo(Object anotherRead) throws ClassCastException
     {
         if(!(anotherRead instanceof ReadInstance))
             throw new ClassCastException("A ReadInstance object expected.");
 
         long otherFinishTime = ((ReadInstance) anotherRead).getFinishtime();
 
         if(this.finishtime < otherFinishTime)
             return -1;
         else if(this.finishtime == otherFinishTime)
             return 0;
         else
             return 1;
     }}
 
 class WriteInstance implements Comparable
 {
     List<Long> oneway;
     long committime;
     long starttime;
 
 
     public long getStarttime() {
         return starttime;
     }
 
 
     public List<Long> getOneway() {
         return oneway;
     }
 
     public long getCommittime() {
         return committime;
     }
 
     public WriteInstance(List<Long> oneway, long starttime, long committime)
     {
         this.oneway = oneway;
         this.starttime = starttime;
         this.committime = committime;
     }
 
     public int compareTo(Object anotherWrite) throws ClassCastException
     {
         if(!(anotherWrite instanceof WriteInstance))
             throw new ClassCastException("A WriteInstance object expected.");
 
         long otherStartTime = ((WriteInstance) anotherWrite).getStarttime();
 
         if(this.starttime < otherStartTime)
             return -1;
         else if(this.starttime == otherStartTime)
             return 0;
         else
             return 1;
     }
 }
 
 class CommitTimes
 {
     TreeMap<Long, Integer> commits;
     HashMap<Integer, Long> versiontotime;
 
     public CommitTimes()
     {
         commits = new TreeMap<Long, Integer>();
         versiontotime = new HashMap<Integer, Long>();
     }
 
     public void record(long time, int version)
     {
       commits.put(time, version);
       versiontotime.put(version, time);
     }
 
     public int last_committed_version(long time)
     {
       if(commits.containsKey(time))
           return commits.get(time);
       return commits.get(commits.headMap(time).lastKey());
     }
 
     public long get_commit_time(int version)
     {
         return versiontotime.get(version);
     }
 }
 
 class KVServer {
   TreeMap<Long, Integer> timeVersions;
  long lasttime;
  int lastversion;
 
   public KVServer()
   {
    lasttime = -1;
    lastversion = -1;
     timeVersions = new TreeMap<Long, Integer>();
   }
 
   public void write(long time, int version)
   {
    if(time > lasttime && version < lastversion)
     {
         return;
     }
     timeVersions.put(time, version);
    lasttime = time;
    lastversion = version;
   }
 
   public int read(long time)
   {
     if(timeVersions.containsKey(time))
         return timeVersions.get(time);
    return timeVersions.get(timeVersions.headMap(time).lastKey());
   }
 }
 
 public class Simulator {
   public static void main (String [] args) {
       assert args.length > 5;
 
       int NUM_READERS = 5;
       int NUM_WRITERS = 1;
 
       final int N = Integer.parseInt(args[0]);
       final int R = Integer.parseInt(args[1]);
       int W = Integer.parseInt(args[2]);
       int K = Integer.parseInt(args[3]);
       assert K >= 1;
       int ITERATIONS = Integer.parseInt(args[4]);
 
       DelayModel delaymodel = null;
 
       if(args[5].equals("FILE"))
       {
           String sendDelayFile = args[6];
           String ackDelayFile = args[7];
 
           delaymodel = new EmpiricalDelayModel(sendDelayFile, ackDelayFile);
       }
       else if(args[5].equals("PARETO"))
       {
           delaymodel = new ParetoDelayModel(Double.parseDouble(args[6]),
                                        Double.parseDouble(args[7]),
                                        Double.parseDouble(args[8]),
                                        Double.parseDouble(args[9]));
       }
       else if(args[5].equals("EXPONENTIAL"))
       {
           delaymodel = new ExponentialDelayModel(Double.parseDouble(args[6]),
                                             Double.parseDouble(args[7]));
       }
       else
       {
           System.err.println(
              "Usage: Simulator <N> <R> <W> <k> <iters> FILE <sendF> <ackF> OPT\n" +
                      "Usage: Simulator <N> <R> <W> <iters> PARETO <W-min> <W-alpha> <ARS-min> <ARS-alpha> OPT\n" +
                      "Usage: Simulator <N> <R> <W> <iters> EXPONENTIAL <W-lambda> <ARS-lambda> OPT\n +" +
                      "OPT= O <SWEEP|LATS>");
           System.exit(1);
       }
 
       final DelayModel delay = delaymodel;
 
       String optsinput = "";
 
       for(int i = 0; i < args.length; ++i)
       {
           if(args[i].equals("O"))
           {
               optsinput = args[i+1];
               assert optsinput.equals("SWEEP") || optsinput.equals("LATS");
               break;
           }
       }
 
       final String opts = optsinput;
 
       final Vector<KVServer> replicas = new Vector<KVServer>();
       for(int i = 0; i < N; ++i)
       {
           replicas.add(new KVServer());
       }
 
 
       HashMap<Integer, Long> commitTimes = new HashMap<Integer, Long>();
       Vector<WriteInstance> writes = new Vector<WriteInstance>();
       final CommitTimes commits = new CommitTimes();
 
       final ConcurrentLinkedQueue<ReadPlot> readPlotConcurrent = new ConcurrentLinkedQueue<ReadPlot>();
 
       long ltime = 0;
       long ftime = 1000;
 
       for(int wid = 0; wid < NUM_WRITERS; wid++)
       {
           long time = 0;
           for(int i = 0; i < ITERATIONS; ++i)
           {
               Vector<Long> oneways = new Vector<Long>();
               Vector<Long> rtts = new Vector<Long>();
               for(int w = 0; w < N; ++w)
               {
                   long oneway = delay.getWriteSendDelay();
                   long ack = delay.getWriteAckDelay();
                   oneways.add(time + oneway);
                   rtts.add(oneway + ack);
               }
               Collections.sort(rtts);
               long wlat = rtts.get(W-1);
               if(opts.equals("LAT"))
               {
                   System.out.printf("W %d\n", wlat);
               }
               long committime = time+wlat;
               writes.add(new WriteInstance(oneways, time, committime));
               time = committime;
           }
 
           if(time > ltime)
               ltime = time;
           if(time < ftime)
               ftime = time;
       }
 
       final long maxtime = ltime;
       final long firsttime = ftime;
 
       Collections.sort(writes);
 
       for(int wno = 0; wno < writes.size(); ++wno)
       {
           WriteInstance curWrite = writes.get(wno);
           for(int sno = 0; sno < N; ++sno)
           {
               replicas.get(sno).write(curWrite.getOneway().get(sno), wno);
           }
           commits.record(curWrite.getCommittime(), wno);
       }
 
       final CountDownLatch latch = new CountDownLatch(NUM_READERS);
 
       for(int rid = 0; rid < NUM_READERS; ++rid)
       {
           Thread t = new Thread(new Runnable ()
           {
               public void run()
               {
                   long time = firsttime*2;
                   while(time < maxtime)
                   {
                       Vector<ReadInstance> readRound = new Vector<ReadInstance>();
                       for(int sno = 0; sno < N; ++sno)
                       {
                           long onewaytime = delay.getReadSendDelay();
                           int version = replicas.get(sno).read(time+onewaytime);
                           long rtt = onewaytime+delay.getReadAckDelay();
                           readRound.add(new ReadInstance(version, rtt));
                       }
 
                       Collections.sort(readRound);
                       long endtime = readRound.get(R-1).getFinishtime();
 
                       if(opts.equals("LAT"))
                       {
                           System.out.printf("R %d\n", endtime);
                       }
 
                       int maxversion = -1;
 
                       for(int rno = 0; rno < R; ++rno)
                       {
                         int readVersion = readRound.get(rno).getVersion();
                         if(readVersion > maxversion)
                             maxversion = readVersion;
                       }
 
                       readPlotConcurrent.add(new ReadPlot(
                                         new ReadOutput(commits.last_committed_version(time), maxversion, time),
                                         commits.get_commit_time(commits.last_committed_version(time))));
                       int staleness = maxversion-commits.last_committed_version(time);
 
                       time += endtime;
                   }
 
                   latch.countDown();
               }
           });
           t.start();
       }
 
       try {
         latch.await();
       }
       catch (Exception e)
       {
           System.out.println(e.getMessage());
       }
 
       if(opts.equals("SWEEP"))
       {
 
           Vector<ReadPlot> readPlots = new Vector<ReadPlot>(readPlotConcurrent);
 
           Collections.sort(readPlots);
           Collections.reverse(readPlots);
 
           HashMap<Long, ReadPlot> manystalemap = new HashMap<Long, ReadPlot>();
 
           long stale = 0;
           for(ReadPlot r : readPlots)
           {
             if(r.getRead().getVersion_read() < r.getRead().getVersion_at_start()-K-1)
             {
                 stale += 1;
                 manystalemap.put(stale, r);
             }
           }
 
           for(int p = 900; p < 1000; ++p)
           {
               long tstale = 0;
               Double pst = (1000-p)/1000.0;
 
               long how_many_stale = (long)Math.ceil(readPlots.size()*pst);
 
               ReadPlot r = manystalemap.get(how_many_stale);
 
               if(r == null)
                   tstale = 0;
               else
                   tstale = r.getRead().getStart_time() - r.getCommit_time_at_start();
 
               System.out.println(p+" "+tstale);
           }
       }
   }
 }
