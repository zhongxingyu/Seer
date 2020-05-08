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
     double start_time;
 
     public int getVersion_at_start() {
         return version_at_start;
     }
 
     public int getVersion_read() {
         return version_read;
     }
 
     public double getStart_time() {
         return start_time;
     }
 
     public ReadOutput(int version_at_start, int version_read, double start_time)
     {
         this.version_at_start = version_at_start;
         this.version_read = version_read;
         this.start_time = start_time;
     }
 }
 
 class ReadPlot implements Comparable
 {
     ReadOutput read;
     double commit_time_at_start;
 
     public ReadOutput getRead() {
         return read;
     }
 
     public double getCommit_time_at_start() {
         return commit_time_at_start;
     }
 
     public ReadPlot(ReadOutput read, double commit_time_at_start)
     {
         this.read = read;
         this.commit_time_at_start = commit_time_at_start;
     }
 
     public int compareTo(Object anotherPlot) throws ClassCastException
     {
         if(!(anotherPlot instanceof ReadPlot))
             throw new ClassCastException("A ReadPlot object expected.");
 
         ReadPlot comparePlot = ((ReadPlot) anotherPlot);
 
         double thisdelta = this.read.getStart_time()-this.commit_time_at_start;
         double theirdelta = comparePlot.getRead().getStart_time()-comparePlot.getCommit_time_at_start();
 
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
     public double getWriteSendDelay();
     public double getReadSendDelay();
     public double getWriteAckDelay();
     public double getReadAckDelay();
 }
 
 class MultiDCDelayModel implements DelayModel
 {
     long Wno, Ano, Rno, Sno;
     double dcdelay;
     int N;
     ParetoDelayModel internalDelay;
 
     public MultiDCDelayModel(double wmin, double walpha, double arsmin, double arsalpha, double dcdelay, int N)
     {
         internalDelay = new ParetoDelayModel(wmin, walpha, arsmin, arsalpha);
         this.dcdelay = dcdelay;
         this.N = N;
         this.Wno = this.Ano = this.Rno = this.Sno = 0;
     }
 
     public double getWriteSendDelay()
     {
         Wno++;
 
         double delay;
         if((Wno % N) == 0)
             delay = dcdelay;
         else
             delay = 0;
 
         return delay+internalDelay.getWriteSendDelay();
     }
 
     public double getWriteAckDelay()
     {
         Ano++;
 
         double delay;
         if((Ano % N) == 0)
             delay = dcdelay;
         else
             delay = 0;
 
         return delay+internalDelay.getWriteAckDelay();
     }
 
     public double getReadSendDelay()
     {
         Rno++;
 
         double delay;
         if((Rno % N) == 0)
             delay = dcdelay;
         else
             delay = 0;
 
         return delay+internalDelay.getReadSendDelay();
     }
 
     public double getReadAckDelay()
     {
         Sno++;
 
         double delay;
         if((Sno % N) == 0)
             delay = dcdelay;
         else
             delay = 0;
         return delay+internalDelay.getReadAckDelay();
     }
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
 
     double getNextPareto(double m, double a)
     {
         return m / Math.pow(rand.nextDouble(), 1/a);
     }
 
     public double getWriteSendDelay()
     {
         return getNextPareto(wmin, walpha);
     }
 
     public double getReadSendDelay()
     {
         return getNextPareto(arsmin, arsalpha);
     }
 
     public double getWriteAckDelay()
     {
         return getReadSendDelay();
     }
 
     public double getReadAckDelay()
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
 
     double getNextExponential(double lambda)
     {
         return Math.log(1-rand.nextDouble())/(-lambda);
     }
 
     public double getWriteSendDelay()
     {
         return getNextExponential(wlambda);
     }
 
     public double getReadSendDelay()
     {
         return getNextExponential(arslambda);
     }
 
     public double getWriteAckDelay()
     {
         return getReadSendDelay();
     }
 
     public double getReadAckDelay()
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
 
     public double getWriteSendDelay() {
       return sendLatencyModel.getInverseCDF(1,
               rand.nextDouble());
     }
 
     public double getReadSendDelay() { return getWriteAckDelay(); };
 
     public double getWriteAckDelay() {
       return ackLatencyModel.getInverseCDF(1,
               rand.nextDouble());
     }
 
     public double getReadAckDelay() { return getWriteAckDelay(); };
 }
 
 class ReadInstance implements Comparable
 {
     int version;
     double finishtime;
 
     public int getVersion() {
         return version;
     }
 
     public double getFinishtime() {
         return finishtime;
     }
 
     public ReadInstance(int version, double finishtime)
     {
         this.version = version;
         this.finishtime = finishtime;
     }
 
     public int compareTo(Object anotherRead) throws ClassCastException
     {
         if(!(anotherRead instanceof ReadInstance))
             throw new ClassCastException("A ReadInstance object expected.");
 
         double otherFinishTime = ((ReadInstance) anotherRead).getFinishtime();
 
         if(this.finishtime < otherFinishTime)
             return -1;
         else if(this.finishtime == otherFinishTime)
             return 0;
         else
             return 1;
     }}
 
 class WriteInstance implements Comparable
 {
     List<Double> oneway;
     double committime;
     double starttime;
 
 
     public double getStarttime() {
         return starttime;
     }
 
 
     public List<Double> getOneway() {
         return oneway;
     }
 
     public double getCommittime() {
         return committime;
     }
 
     public WriteInstance(List<Double> oneway, double starttime, double committime)
     {
         this.oneway = oneway;
         this.starttime = starttime;
         this.committime = committime;
     }
 
     public int compareTo(Object anotherWrite) throws ClassCastException
     {
         if(!(anotherWrite instanceof WriteInstance))
             throw new ClassCastException("A WriteInstance object expected.");
 
         double otherStartTime = ((WriteInstance) anotherWrite).getStarttime();
 
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
     TreeMap<Double, Integer> commits;
     HashMap<Integer, Double> versiontotime;
 
     public CommitTimes()
     {
         commits = new TreeMap<Double, Integer>();
         versiontotime = new HashMap<Integer, Double>();
     }
 
     public void record(double time, int version)
     {
       commits.put(time, version);
       versiontotime.put(version, time);
     }
 
     public int last_committed_version(double time)
     {
       if(commits.containsKey(time))
           return commits.get(time);
       return commits.get(commits.headMap(time).lastKey());
     }
 
     public double get_commit_time(int version)
     {
         return versiontotime.get(version);
     }
 }
 
 class KVServer {
   TreeMap<Double, Integer> timeVersions;
 
   public KVServer()
   {
     timeVersions = new TreeMap<Double, Integer>();
   }
 
   public void write(double time, int version)
   {
     //don't store old versions!
     if(read(time) > version)
     {
         return;
     }
     timeVersions.put(time, version);
   }
 
   public int read(double time)
   {
     if(timeVersions.containsKey(time))
         return timeVersions.get(time);
 
 
     SortedMap<Double, Integer> mapFromTime = timeVersions.headMap(time);
     if(mapFromTime.isEmpty())
         return -1;
     return timeVersions.get(mapFromTime.lastKey());
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
       else if(args[5].equals("MULTIDC"))
       {
           delaymodel = new MultiDCDelayModel(Double.parseDouble(args[6]),
                                        Double.parseDouble(args[7]),
                                        Double.parseDouble(args[8]),
                                        Double.parseDouble(args[9]),
                                        Double.parseDouble(args[10]),
                                        N);
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
                      "Usage: Simulator <N> <R> <W> <iters> MULTIDC <W-min> <W-alpha> <ARS-min> <ARS-alpha> <DC-delay> OPT\n +" +
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
 
       Vector<Double> writelats = new Vector<Double>();
       final ConcurrentLinkedQueue<Double> readlats = new ConcurrentLinkedQueue<Double>();
 
       HashMap<Integer, Double> commitTimes = new HashMap<Integer, Double>();
       Vector<WriteInstance> writes = new Vector<WriteInstance>();
       final CommitTimes commits = new CommitTimes();
 
       final ConcurrentLinkedQueue<ReadPlot> readPlotConcurrent = new ConcurrentLinkedQueue<ReadPlot>();
 
       double ltime = 0;
       double ftime = 1000;
 
       for(int wid = 0; wid < NUM_WRITERS; wid++)
       {
           double time = 0;
           for(int i = 0; i < ITERATIONS; ++i)
           {
               Vector<Double> oneways = new Vector<Double>();
               Vector<Double> rtts = new Vector<Double>();
               for(int w = 0; w < N; ++w)
               {
                   double oneway = delay.getWriteSendDelay();
                   double ack = delay.getWriteAckDelay();
                   oneways.add(time + oneway);
                   rtts.add(oneway + ack);
               }
               Collections.sort(rtts);
               double wlat = rtts.get(W-1);
               if(opts.equals("LATS"))
               {
                   writelats.add(wlat);
               }
               double committime = time+wlat;
               writes.add(new WriteInstance(oneways, time, committime));
               time = committime;
           }
 
           if(time > ltime)
               ltime = time;
           if(time < ftime)
               ftime = time;
       }
 
       final double maxtime = ltime;
       final double firsttime = ftime;
 
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
                   double time = firsttime*2;
                   while(time < maxtime)
                   {
                       Vector<ReadInstance> readRound = new Vector<ReadInstance>();
                       for(int sno = 0; sno < N; ++sno)
                       {
                           double onewaytime = delay.getReadSendDelay();
                           int version = replicas.get(sno).read(time+onewaytime);
                           double rtt = onewaytime+delay.getReadAckDelay();
                           readRound.add(new ReadInstance(version, rtt));
                       }
 
                       Collections.sort(readRound);
                       double endtime = readRound.get(R-1).getFinishtime();
 
                       if(opts.equals("LATS"))
                       {
                           readlats.add(endtime);
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
 
           for(double p = .9; p < 1; p += .001)
           {
               double tstale = 0;
 
              long how_many_stale = (long)Math.ceil(readPlots.size()*(1-p));
 
               ReadPlot r = manystalemap.get(how_many_stale);
 
               if(r == null)
                   tstale = 0;
               else
                   tstale = r.getRead().getStart_time() - r.getCommit_time_at_start();
 
               System.out.println(p+" "+tstale);
           }
       }
 
       if(opts.equals("LATS"))
       {
           System.out.println("WRITE");
           Collections.sort(writelats);
           for(double p = 0; p < 1; p += .01)
           {
               System.out.printf("%f %f\n", p, writelats.get((int) Math.round(p * writelats.size())));
           }
 
           Vector<Double> readLatencies = new Vector<Double>(readlats);
 
           System.out.println("READ");
           Collections.sort(readLatencies);
           for(double p = 0; p < 1; p += .01)
           {
               System.out.printf("%f %f\n", p, readLatencies.get((int)Math.round(p*readLatencies.size())));
           }
       }
   }
 }
