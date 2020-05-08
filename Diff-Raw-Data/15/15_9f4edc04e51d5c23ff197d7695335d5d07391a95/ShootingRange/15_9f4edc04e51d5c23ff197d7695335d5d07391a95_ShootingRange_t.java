 package sim;
 
 import desmoj.core.simulator.Model;
 import desmoj.core.simulator.SimProcess;
 import desmoj.core.simulator.TimeSpan;
 
 /**
  * Models a shooting range session.
  * Each <code>Competitor</code> enters a <code>ShootingRange</code> and performs shooting.
  */
 public class ShootingRange extends SimProcess {
 
     private Biathlon myModel;
     private Logger myLogger;
 
     /**
      * The c-tor.
      * @param owner The model owning this process.
      * @param name The name of this process.
      * @param showInTrace The flag toggling tracing for this process.
      */
     public ShootingRange(Model owner, String name, boolean showInTrace) {
         super(owner, name, showInTrace);
         myModel = (Biathlon) owner;
         myLogger = new Logger("ShootingRange.txt");
     }
 
     /**
      * Implements the lifecycle of this process.
      */
     public void lifeCycle() {
 
         while (true) {
             if (myModel.competitorsQueue.isEmpty()) {
                 myModel.shootingRangeQueue.insert(this);
                 passivate();
             } else {
                 Competitor nextCompetitor = myModel.competitorsQueue.first();
                 myModel.competitorsQueue.remove(nextCompetitor);
 
                myLogger.log(String.format("%s enters the shooting range.",
                                            nextCompetitor.toString()));
 
                 int misses = nextCompetitor.computeShotsMissed();
                 nextCompetitor.addPenalties(misses);
 
                 // Models concrete shooting time.
                 TimeSpan time = nextCompetitor.computeShootingTime();
                 nextCompetitor.activate(time);
 
                 long simTime = Biathlon.getInstance().presentTime().getTimeTruncated();
                 long timeVal = time.getTimeTruncated();
                 long allTime = timeVal + simTime;
 
                 // FIXME These two show unordered in the log.
                 // FIXME Add a log queue?
                myLogger.log(allTime, String.format("%s misses %d times.",
                                                     nextCompetitor.toString(), misses));
 
                myLogger.log(allTime, String.format("%s leaves the shooting range.",
                                                     nextCompetitor.toString()));
             }
         }
     }
 }
