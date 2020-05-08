 package Escada.tpc.common;
 
 import Escada.tpc.common.database.*;
 import java.util.*;
 
 public abstract class Emulation {
 
   private static boolean finished = false;
   private static Random rand = new Random();
 
   private static String traceInformation = null;
   private static int numberConcurrentEmulators = 1;
 
   private CommonDatabase db = null;
 
   private StateTransition sb = null;
   private String name = null;
   private int id = 0;
 
   private long usmd = 0;
   private long thinkTime = 0;
   private long keyTime = 0;
   private long maxTrans = 1;
   
   private String hid = null;
 
   /**
   * It defines the maximum number of transactions that can be executed. 
   *
   * @param long the number of transactions. If a negative number is passed as parameter the last value is not changed.
   * A default value of 1 it is assumed.
   * @see getMaxTransactions
   **/
   public void setMaxTransactions(long maxTrans) {
    if (maxTrans > 0) this.maxTrans = maxTrans;
   }
 
   /**
   * It returns the maximum number of transactions that can be executed.
   *
   * @return the number of transactions
   **/
   public long getMaxTransactions() {
     return (maxTrans);
   }
 
   /**
   * It controls the remain number of transactions that can be executed.
   **/
   public void decMaxTransactions() {
     maxTrans--;
   }
 
   /**
   * It returns if the emulation was finished or not.
   *
   * @return the status (finished - true or unfinished - false) of the emulation.
   * @see setFinished
   **/
   public static boolean isFinished() {
     return (finished);
   }
 
   /**
   * It defines the status of the emulation as finished or unfinished, otherwise
   * the name of the method is called setFinished. It is used to stop the emulation.
   * 
   * @param boolean use true in order to stop or false otherwise not.
   **/
   public static void setFinished(boolean fin) {
     finished = fin;
   }
 
   /**
   * It defines the object used as a state transition, which determines the possible
   * steps of the simulation.
   *
   * @param StateTransition the object that determines the state transition
   * @see StateTransition,getStateTransition
   **/
   public void setStateTransition(StateTransition sb) {
     this.sb = sb;
   }
 
   /** 
   * It returns the object used as a state transition, which determines the
   *  possible steps of the simulation.
   *
   * @return the object used as the state transition
   * @see StateTransition,setStateTransition
   **/
   public StateTransition getStateTransition() {
     return(sb);
   }
 
   /**
   * It defines the emulator's id.
   *
   * @param int the id of the emulator
   * @see getEmulationId
   **/
   public void setEmulationId(int id) {
     this.id = id;
   }
 
   /**
   * It returns the emulator's id.
   *
   * @return the id of the emulator
   * @see setEmulationId
   **/
   public int getEmulationId() {
     return (id);
   }
 
   /**
   * It defines the emulator's name.
   *
   * @param String the emulator's name
   * @see getEmulationName
   **/
   public void setEmulationName(String name) {
     this.name = name;
   }
 
   /**
   * It returns the emulator's name.
   *
   * @return the emulator's name
   * @see setEmulationName
   **/
   public String getEmulationName() {
     return (name);
   }
 
   /**
   * It returns the last thinktime used by the emulator, which means the
   * time used by the operator to take a decision. It is important to
   * notice that we consider this value the thinktime as 
   * specified in the TPC-C plus the keyingtime to calculate this value.
   *
   * @return the thinktime
   * @see setThinkTime
   **/
   public long getThinkTime() {
     return (thinkTime);
   }
 
   /**
   * It returns the last keyingtime time used by the emulator, which means
   * the time used by the operator to fill a form.
   *
   * @return the keyingtime
   * @see setKeyingTime
   **/
   public long getKeyingTime() {
     return (keyTime);
   }
 
   /**
   * It stores the last thinktime used by the emulator, which means the
   * time used by the operator to take a decision. It is important to
   * notice that we consider this value the thinktime as
   * specified in the TPC-C plus the keyingtime to calculate this value.
   *
   * @param long the thinktime
   * @see getThinkTime
   **/
   public void setThinkTime(long thinkTime) {
     this.thinkTime = thinkTime;
   }
 
  /**
   * It stores the last keyingtime time used by the emulator, which means
   * the time used by the operator to fill a form.
   *
   * @param long the keyingtime
   * @see getKeyingTime
   **/
   public void setKeyingTime(long keyTime) {
     this.keyTime = keyTime;
   }
 
   /**
   * It defines the trace file used to log information.
   *
   * @param String the name of the trace file
   * @see getTraceInformation
   **/
   public static void setTraceInformation(String trace) {
     traceInformation = trace;
   }
 
   /**
   * It returns the name of the trace file used to log information.
   *
   * @return the name of the trace file
   * @see setTraceInformation
   **/
   public static String getTraceInformation() {
     return (traceInformation);
   }
 
   /**
   * It defines the number of concurrent emulators.
   *
   * @param int the number of concurrent emulators
   * @see getNumberConcurrentEmulators
   **/
   public static void setNumberConcurrentEmulators(int emu) {
     numberConcurrentEmulators = emu;
   }
 
   /**
   * It returns the number of concurrent emulators.
   *
   * @return the number of concurrent emulators
   * @see setNumberConcurrentEmulators
   **/
   public static int getNumberConcurrentEmulators() {
     return (numberConcurrentEmulators);
   }
 
   /**
   * It returns the random object.
   *
   * @return random object
   **/
   public Random getRandom() {
     return (rand);
   }
 
   /** 
    * It defines the host to which it the emulator belongs to.
    *
    * @param String the host id
    * @see getHosId
    **/
   public void setHostId(String hid){
       this.hid = hid;
   }
   /** 
    * It returns the host to which it the emulator belongs to.
    *
    * @return the host id
    * @see settHosId
    **/
   public String getHostId() {
       return(hid);
   }
 
   /**
   * It sets up the emulator.
   *
   **/
   public abstract void initialize();
 
   /**
   * It returns the calculated thinktime, according to the properties
   * of the benchmark, using the value defined by the setThinkTime and
   * retrieved with the getKeyingTime.
   *
   * @return the thinktime
   * @see getThinkTime,setThinkTime
   **/
   public abstract long thinkTime();
 
   /**
   * It returns the calculated keyingtime, according to the properties
   * of the benchmark, using the value defined by the setKeyingTime 
   * and retrieved with the getKeyingTime.
   *
   * @see getKeyingTime,setKeyingTime
   **/
   public abstract long keyingTime();
 
   /**
   * It proceeds with the emulation according to the host to which it
   * belongs and based on the benchmark's properties.
   *
   * @param int host to which the emulator is attached to.
   * @see run,processIncrement
   **/
   public abstract void process(String hid);
 
   /**
   * It proceeds with the emulation according to the host to which it
   * belongs and based on the benchmark's properties.
   *
   * @see run,processIncrement
   **/
   public void process() {
       process(hid);
   }
 
   /**
   * In contrast to the process method, it executes just one
   * transaction per call according to the host to which it
   * belongs and based on the benchmark's properties.
   *
   * @param int host to which the emulator is attached to.
   * @see run,process
   **/
   public abstract Object processIncrement(String hid);
 
   /**
   * In contrast to the process method, it executes just one
   * transaction per call according to the host to which it
   * belongs and based on the benchmark's properties.
   *
   * @see run,process
   **/
   public Object processIncrement() {
       return(processIncrement(hid));
   }
 
   /**
   * It returns the database used by the emulator in order to
   * handle the transaction requests.
   *
   * @return database based on the class CommonDatabase
   **/
   public CommonDatabase getDatabase()
   {
     return (db);
   }
 
   /**
   * It defines the database used by the emulator in order to
   * handle the transaction requests.
   *
   * @param CommonDatabase the object that defines the database, which
   **/
   public void setDatabase(CommonDatabase db)
   {
     this.db=db;
   }
 }
 // arch-tag: 001ca60a-aae1-48e1-8c23-681cc4dde63f
