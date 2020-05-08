 package interiores.business.models.backtracking;
 
 import interiores.business.events.backtracking.SolveDesignFinishedEvent;
 import interiores.business.events.backtracking.SolveDesignStartedEvent;
 import interiores.business.events.room.RoomDesignChangedEvent;
 import interiores.business.exceptions.SolverNotFinishedException;
 import interiores.business.models.constraints.room.GlobalConstraint;
 import interiores.core.Event;
 import interiores.core.Observable;
 import interiores.core.Observer;
 import interiores.shared.backtracking.NoSolutionException;
 import interiores.utils.Dimension;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  *
  * @author hector0193
  */
 public class ThreadSolver
     extends Solver
     implements Observable, Observer
 {
     private List<Observer> listeners;
     private boolean isSolutionFound;
     private boolean isTimerEnabled;
     private Thread thread;
     private long time;
     
     public ThreadSolver() {
         this(new VariableConfig(new Dimension(0, 0)), new ArrayList<GlobalConstraint>()); // Default empty solver
     }
     
     public ThreadSolver(VariableConfig variableConfig, Collection<GlobalConstraint> gconst) {
         super(variableConfig, gconst);
         
         listeners = new ArrayList();
         isTimerEnabled = false;
     }
     
     public void setTimerEnabled(boolean timer) {
         isTimerEnabled = timer;
     }
     
     @Override
     public void solve() {
         if(isSolving())
             throw new SolverNotFinishedException();
         
         isSolutionFound = false;
         
         thread = new Thread(){
             @Override
             public void run() {
                 computeSolution();
             }
         };
         
         thread.start(); // Start solving!
     }
     
     private void computeSolution() {
         isSolutionFound = false;
         SolveDesignFinishedEvent solveFinished = new SolveDesignFinishedEvent();
 
         if(isTimerEnabled)
             time = System.nanoTime();
         
         // And try to solve it
         try {
             notify(new SolveDesignStartedEvent());
             
             super.solve();
             
             isSolutionFound = true;
             solveFinished.solutionFound();
 
             notify(new RoomDesignChangedEvent());
         }
         catch (NoSolutionException nse) {
             isSolutionFound = false;
             
             if(shouldStop)
                 notify(new RoomDesignChangedEvent());
         }
         
         if(isTimerEnabled)
             solveFinished.setTime(System.nanoTime() - time);
         
         notify(solveFinished);
     }
     
     public boolean isSolutionFound() {
         return isSolutionFound;
     }
     
     public boolean isSolving() {
         return (thread != null && thread.isAlive());
     }
     
     public boolean isPaused() {
        return (thread.getState() == Thread.State.WAITING);
     }
     
     public void resumeSolving() {
         synchronized (thread) {
             thread.notify();
         }
     }
     
     public void stopSolving() {
         shouldStop = true;
         
         if(isPaused())
             resumeSolving();
     }
     
     @Override
     public void addListener(Observer o) {
         listeners.add(o);
     }
     
     @Override
     public void notify(Event e) {
         for(Observer listener : listeners)
             listener.notify(e);
     }
     
     public boolean canDebug() {
         return false;
     }
     
     public boolean shouldRenew(VariableConfig configuration) {
         if(!configuration.isConsistent())
             return true;
         
         if(!hasConfiguration(configuration))
             return true;
         
         if(canDebug())
             return true;
         
         return false;
     }
 }
