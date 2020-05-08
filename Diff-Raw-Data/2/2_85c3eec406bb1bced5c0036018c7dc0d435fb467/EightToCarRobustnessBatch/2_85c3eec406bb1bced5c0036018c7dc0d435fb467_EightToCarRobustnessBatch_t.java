 package rar;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.rmi.RemoteException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 
 
 import ussr.remote.AbstractSimulationBatch;
 import ussr.remote.facade.ParameterHolder;
 
 public class EightToCarRobustnessBatch extends AbstractSimulationBatch {
     public static class Parameters extends ParameterHolder {
         public int number;
         public float minR, maxR, completeR, maxTime;
         public Integer seedMaybe;
         public Parameters(Class<?> mainClass, int number, float minR, float maxR, float completeR, float maxTime) {
             this(mainClass,number,minR,maxR,completeR,maxTime,null);
         }
         public Parameters(Class<?> mainClass, int number, float minR, float maxR, float completeR, float maxTime, Integer seed) {
             super(mainClass);
             this.number = number;
             this.minR = minR;
             this.maxR = maxR;
             this.completeR = completeR;
             this.maxTime = maxTime;
             this.seedMaybe = seed;
         }
         public String toString() {
             NumberFormat formatter = new DecimalFormat("0000");
             return (super.mainClass==null?"_":super.mainClass.getName())+"#"+formatter.format(number)+":minR="+minR+",maxR="+maxR+",comR="+completeR+",maxT="+maxTime+(seedMaybe==null?",%":","+seedMaybe);
         }
         /* (non-Javadoc)
          * @see java.lang.Object#hashCode()
          */
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = super.hashCode();
             result = prime * result + Float.floatToIntBits(completeR);
             result = prime * result + Float.floatToIntBits(maxR);
             result = prime * result + Float.floatToIntBits(maxTime);
             result = prime * result + Float.floatToIntBits(minR);
             result = prime * result + number;
             return result;
         }
         /* (non-Javadoc)
          * @see java.lang.Object#equals(java.lang.Object)
          */
         @Override
         public boolean equals(Object obj) {
             if (this == obj)
                 return true;
             if (!super.equals(obj))
                 return false;
             if (getClass() != obj.getClass())
                 return false;
             Parameters other = (Parameters) obj;
             if (Float.floatToIntBits(completeR) != Float
                     .floatToIntBits(other.completeR))
                 return false;
             if (Float.floatToIntBits(maxR) != Float.floatToIntBits(other.maxR))
                 return false;
             if (Float.floatToIntBits(maxTime) != Float
                     .floatToIntBits(other.maxTime))
                 return false;
             if (Float.floatToIntBits(minR) != Float.floatToIntBits(other.minR))
                 return false;
             if (number != other.number)
                 return false;
             return true;
         }
     }
 
     private static final boolean SKIP_EFFICIENCY = true;
     private static final float TIMEOUT = 200f;
     public static final int N_REPEAT = 40;
     public static final float START_RISK = 0;
     public static final float END_RISK = 0.91f;
     public static final float RISK_DELTA = 0.0f;
     public static final float RISK_INC = 0.1f;
     public static final float START_FAIL = 0;
     public static final float END_FAIL = 0.101f;
     public static final float FAIL_INC = 0.01f;
     public static final int N_PARALLEL_SIMS = 2;
     public static final Class<?> EXPERIMENTS[] = new Class<?>[] {
         EightToCarRobustnessExperimentSafeToken32.class,
         EightToCarRobustnessExperimentSafeToken128.class,
         EightToCarRobustnessExperimentSafeTokenMaxint.class,
         EightToCarRobustnessExperimentBroadcast.class,
         EightToCarRobustnessExperimentParallelLim.class,
         EightToCarRobustnessExperimentParallelStd.class
     };
     
     private List<ParameterHolder> parameters = new LinkedList<ParameterHolder>();
     private List<Class<? extends EightToCarRobustnessExperiment>> experiments = new ArrayList<Class<? extends EightToCarRobustnessExperiment>>();
     private PrintWriter logfile;
     
     public static void main(String argv[]) {
         new EightToCarRobustnessBatch(EXPERIMENTS).start(N_PARALLEL_SIMS);
     }
 
     private int sequenceIndex = -1;
     private List<Integer> randomSequence = new ArrayList<Integer>();
     private Random sequenceRandomizer = new Random(87);
     
     private void resetRandomSequence() {
         sequenceIndex = 0;
     }
     
     private int nextRandomFromSequence() {
        while(sequenceIndex>=randomSequence.size())
             randomSequence.add(sequenceRandomizer.nextInt());
         return randomSequence.get(sequenceIndex++);
     }
     
     public EightToCarRobustnessBatch(Class<?>[] mainClasses) {
         int counter = 0;
         for(int ci=0; ci<mainClasses.length; ci++) {
             // Efficiency experiments, 0% failure risk, varying packet loss
             if(!SKIP_EFFICIENCY)
                 for(float risk = START_RISK; risk<=END_RISK; risk+=RISK_INC) {
                     for(int i=0; i<N_REPEAT; i++) {
                         parameters.add(new EightToCarRobustnessBatch.Parameters(mainClasses[ci],counter,Math.max(0, risk-RISK_DELTA),risk,0,TIMEOUT));
                     }
                     counter++;
                 }
             // Robustness experiments, varying failure risk, no packet loss
             for(float fail = START_FAIL; fail<=END_FAIL; fail+=FAIL_INC) {
                 resetRandomSequence();
                 for(int i=0; i<N_REPEAT; i++) {
                     parameters.add(new EightToCarRobustnessBatch.Parameters(mainClasses[ci],counter,0,0,fail,TIMEOUT,nextRandomFromSequence()));
                 }
                 counter++;
             }
 
         }
         try {
             logfile = new PrintWriter(new BufferedWriter(new FileWriter("eight-log.txt")));
         } catch(IOException exn) {
             throw new Error("Unable to open log file");
         }
         logfile.println("Starting "+parameters.size()+" experiments");
     }
 
     @Override
     protected ParameterHolder getNextParameters() {
         logfile.println("experiment "+parameters.get(0)+" starting"); logfile.flush();
         return parameters.remove(0);
     }
 
     @Override
     protected boolean runMoreSimulations() {
         return parameters.size()>0;
     }
 
     public void provideReturnValue(String experiment, String name, Object value) throws RemoteException {
         logfile.print("experiment "+experiment+" completed: ");
         if(name.equals("success")) {
             float time = (Float)value;
             logfile.println("Time taken:"+time);
             recordSuccess(experiment,time);
         } else if(name.equals("timeout")) {
             logfile.println("Timeout:X");
             recordFailure(experiment);
         }
         else {
             logfile.println("Unknown value: "+name);
             recordFailure(experiment);
         }
         logfile.flush();
     }
 
     @Override
     protected void reportHook(Set<String> experimentsNames,
             Map<String, List<Float>> successes,
             Map<String, Integer> failures,
             Map<String, ParameterHolder> experimentParameters) {
         
     }
 }
