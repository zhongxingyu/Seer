 package org.daum.library.android.sitac.model;
 
 import java.util.ArrayList;
 
 import org.daum.common.model.api.Danger;
 import org.daum.common.model.api.Demand;
 import org.daum.common.model.api.Target;
 import org.daum.library.android.sitac.listener.OnEngineStateChangeListener;
 
 /**
 * Here you should save/update/delete data from the Replica
  */
 public class SITACEngine {
 
 	private ArrayList<Demand> demands;
 	private ArrayList<Danger> dangers;
 	private ArrayList<Target> targets;
 	private OnEngineStateChangeListener listener;
 	
 	public SITACEngine(OnEngineStateChangeListener engineHandler) {
 		this.demands = new ArrayList<Demand>();
 		this.dangers = new ArrayList<Danger>();
 		this.targets = new ArrayList<Target>();
 		listener = engineHandler;
 	}
 
 	public void addDemand(Demand d) {
 		demands.add(d);
 		if (listener != null) listener.onDemandAdded(d);
 	}
 
 	public void addDanger(Danger d) {
 		dangers.add(d);
 		if (listener != null) listener.onDangerAdded(d);
 	}
 
 	public void addTarget(Target t) {
 		targets.add(t);
 		if (listener != null) listener.onTargetAdded(t);
 	}
 
 	public void updateDemand(Demand d) {
     	// TODO
 		if (listener != null) listener.onDemandUpdated(d);
 	}
 
     public void updateDanger(Danger d) {
         // TODO
         if (listener != null) listener.onDangerUpdated(d);
     }
 
     public void updateTarget(Target t) {
         // TODO
         if (listener != null) listener.onTargetUpdated(t);
     }
 }
