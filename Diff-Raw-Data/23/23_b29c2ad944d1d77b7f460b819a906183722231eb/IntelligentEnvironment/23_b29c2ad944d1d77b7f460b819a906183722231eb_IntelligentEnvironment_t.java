 package maro.core;
 
 import jason.asSyntax.Structure;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class IntelligentEnvironment extends TimeSteppedEnvironment
 {
     protected long sum;
     protected List<Option> options;
     protected Integer lastStep;
 
     public IntelligentEnvironment () {
         sum = 0;
		ActionLoader.getInstance().loadAllActions("maro.example.sims.ea");
         options = new ArrayList<Option> ();
         options.add( new Option<Integer> (true,
                 "the number for step's timeout(ms)",
                 "isn't a valid number",
                 null));
         options.add( new Option<Integer> (true,
                 "the number for the last step",
                 "isn't a valid number",
                 null));
         options.add( new Option<Integer> (false,
                 "the number for the quantity of agents",
                 "isn't a valid number. Using the value 100.",
                 100));
         options.add( new Option<Boolean> (false,
                 "a boolean to indicate to add next action to queue"
                 +" or else fail the second action",
                 "isn't a valid boolean. Using the value true.",
                 true));
     }
 
     protected void printOptions() {
 	int param = 0;
 	for (Option o : options) {
 	    param += 1;
 	    if (o.description != null) {
 		getLogger().warning("The "+param+"o. argument is "+
 			((o.isRequired) ? "mandatory" : "optional")
 			+" and is "+o.description);
 	    }
 	}
     }
 
     protected boolean validateOptions(String []args) {
         int mandatories = 0;
 
         for (Option o : options) {
             if (o.isRequired) mandatories += 1;
         }
         return (args.length >= mandatories && args.length <= options.size());
     }
 
 	public void setLastStep(int step) {
 	    lastStep = step;
 	}
 
     @SuppressWarnings("unchecked")
     public boolean loadOptions(String []args) {
         Option<Integer> oi;
         Option<Boolean> ob;
         boolean exit = true;
 
         if ( validateOptions(args) == false ) {
             printOptions();
             return false;
         }
 
         // argh... I like transform this in foreach code... but someone will
         // not understanding the code... than I will keep this trash... :'(
         oi = options.get(0);
         try {
             oi.value = Integer.parseInt(args[0]);
         } catch (Exception e) {
             getLogger().warning("The first argument "+oi.help);
             getLogger().warning("\tThis argument is"+oi.description);
             if (oi.isRequired) exit = false;
         }
 
         oi = options.get(1);
         try {
             oi.value = Integer.parseInt(args[1]);
             setLastStep(oi.value);
         } catch (Exception e) {
             getLogger().warning("The second argument "+oi.help);
             getLogger().warning("\tThis argument is"+oi.description);
             if (oi.isRequired) exit = false;
         }
 
         oi = options.get(2);
         try {
             oi.value = Integer.parseInt(args[2]);
 			setNbAgs(oi.value);
         } catch (Exception e) {
             getLogger().warning("The third argument "+oi.help);
             getLogger().warning("\tThis argument is"+oi.description);
             if (oi.isRequired) exit = false;
         }
 
         ob = options.get(3);
         try {
             ob.value = Boolean.parseBoolean(args[3]);
         } catch (Exception e) {
             getLogger().warning("The fourth argument "+ob.help);
             getLogger().warning("\tThis argument is"+ob.description);
             if (ob.isRequired) exit = false;
         }
 
         return exit;
     }
 
     @Override
     public void init(String[] args) {
         initOptions(args);
         initDefault();
     }
 
     @SuppressWarnings("unchecked")
     protected void initOptions(String[] args) {
         boolean exit = false;
 
         if (loadOptions(args) == false) {
             System.exit(50);
             return ;
         }
 
         Option<Boolean> ob = options.get(3);
         Boolean queue = ob.value;
         if (queue != null) {
             if (queue == true)
             super.setOverActionsPolicy(OverActionsPolicy.queue);
             else
             super.setOverActionsPolicy(OverActionsPolicy.failSecond);
         }
 
         if (exit) System.exit(51);
     }
 
     @SuppressWarnings("unchecked")
     protected void initDefault() {
         Option<Integer> os = options.get(0);
         String [] parms;
         if (os.value != null) {
             parms = new String[] { os.value.toString() };
         } else {
             parms = new String[] { };
         }
 
         super.init( parms );
     }
 
     @SuppressWarnings("unchecked")
     public int getNumberAgentsSettings() {
         Option<Integer> oi = options.get(2);
         return oi.value;
     }
 
     @Override
     protected void stepFinished(int step, long elapsedTime, boolean byTimeout) {
         long mean = (step > 0) ? (sum + elapsedTime) / (step + 1) : 0;
         getLogger().info("Finished step " + step + " after " + elapsedTime + " (mean "+ mean +")");
         sum += elapsedTime;
         if (byTimeout == true) getLogger().warning("Step " + step + " finished by timeout!");
 
         tryStop(lastStep);
     }
 
     public void tryStop(int lastStepToStop) {
         if (getStep() >= lastStepToStop) {
             getLogger().fine("Trying stop simulation...");
             try {
                 getEnvironmentInfraTier().getRuntimeServices().stopMAS();
             } catch (Exception e) {
                 getLogger().warning("Failed trying stop simulation! " + e);
             }
         }
     }
 
 	@Override
 	protected int requiredStepsForAction(String agName, Structure action) {
 		ActionLoader al = ActionLoader.getInstance();
 		if (al == null) return super.requiredStepsForAction(agName, action);
 		Integer i = al.requiredStepsForAction(action);
 		if (i == null) return super.requiredStepsForAction(agName, action);
 		return i;
 	}
 
 	@Override
 	public boolean executeAction(String agName, Structure action) {
 		ActionLoader al = ActionLoader.getInstance();
 		if (al == null) return super.executeAction(agName, action);
 		Boolean b = al.executeAction(agName, action, this);
 		if (b == null) return super.executeAction(agName, action);
 		return b;
 	}
 
 
 
 
     protected class Option<T> {
         public T value;
         final public String description;
         final public String help;
         final public boolean isRequired;
 
         public Option ( boolean required, String descr, String error, T valured )
         {
             value = valured;
             help = error;
             description = descr;
             isRequired = required;
         }
     }
 
 }
