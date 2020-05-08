 //----------------------------------------------------------------------------
 // Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 // 
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 
 package jason.environment;
 
 import jason.asSemantics.Unifier;
 import jason.asSyntax.Literal;
 import jason.asSyntax.Structure;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * It is a base class for Environment, it is overridden by the user
  * application to define the environment "behaviour".
  * 
  * <p>Execution sequence: 	
  *     <ul><li>setEnvironmentInfraTier, 
  *         <li>init, 
  *         <li>(getPercept|executeAction)*, 
  *         <li>stop.
  *     </ul>
  * 
  */
 public class Environment { 
 
 	private List<Literal> percepts = Collections.synchronizedList(new ArrayList<Literal>());
 	private Map<String,List<Literal>>  agPercepts = new ConcurrentHashMap<String, List<Literal>>();
 	
     /** the infrastructure tier for environment (Centralised, Saci, ...) */
 	private EnvironmentInfraTier environmentInfraTier = null;
 
 	// set of agents that already received the last version of perception
 	private Set<String> uptodateAgs = Collections.synchronizedSet(new HashSet<String>());
 
     protected ExecutorService executor; // the thread pool used to execute actions
 
     private static Logger logger = Logger.getLogger(Environment.class.getName());
 
     /** creates an environment class with n threads to execute actions requited by the agents */
     public Environment(int n) {
         // creates a thread pool with n threads
         executor = Executors.newFixedThreadPool(n);
 
         // creates and executor with 1 core thread
         // where no more than 3 tasks will wait for a thread
         // The max number of thread is 1000 (so the 1001 task will be rejected) 
         // Threads idle for 10 sec. will be removed from the pool
         //executor= new ThreadPoolExecutor(1,1000,10,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(3));
     }
 
     /** creates an environment class with the default number of threads in the pool */
     public Environment() {
         this(4);
     }
 
 	/** 
      * Called before the MAS execution with the args informed in
      * .mas2j project, the user environment could override it.
      */
 	public void init(String[] args) {
 	}
 	
 	/** 
      * Called just before the end of MAS execution, the user
      * environment could override it.
      */
 	public void stop() {
         executor.shutdownNow();
 	}
 	
 	
 	/**
 	 * Sets the infrastructure tier of the environment (saci, jade, centralised, ...)
 	 */
 	public void setEnvironmentInfraTier(EnvironmentInfraTier je) {
 		environmentInfraTier = je;
 	}
 	public EnvironmentInfraTier getEnvironmentInfraTier() {
 		return environmentInfraTier;
 	}
 
     
     public void informAgsEnvironmentChanged(Collection<String> agents) {
         if (environmentInfraTier != null) {
             environmentInfraTier.informAgsEnvironmentChanged(agents);
         }
     }
 
     public void informAgsEnvironmentChanged() {
         if (environmentInfraTier != null) {
             environmentInfraTier.informAgsEnvironmentChanged();
         }
     }
 
 	/**
 	 * Returns percepts for an agent.  A full copy of both common
 	 * and agent's percepts lists is returned.
 	 */
     public List<Literal> getPercepts(String agName) {
 		
 		// check whether this agent needs the current version of perception
 		if (uptodateAgs.contains(agName)) {
 			return null;
 		}
 		// add agName in the set of updated agents
 		uptodateAgs.add(agName);
 		
 		int size = percepts.size();
 		List<Literal> agl = agPercepts.get(agName);
 		if (agl != null) {
 			size += agl.size();
 		}
 		List<Literal> p = new ArrayList<Literal>(size);
 		
         if (! percepts.isEmpty()) { // has global perception?
             synchronized (percepts) {
                 // make a local copy of the environment percepts
     			// Note: a deep copy will be done by BB.add
     			p.addAll(percepts);
             }
         }
 		if (agl != null) { // add agent personal perception
 	        synchronized (agl) {
 				p.addAll(agl);
 	        }
 		}
 		
         return p;
     }
 
 	/** Adds a perception for all agents */
 	public void addPercept(Literal per) {
 		if (per != null) {
 			if (! percepts.contains(per)) {
 				percepts.add(per);
 				uptodateAgs.clear();
 			}
 		}
 	}
 
 	/** Removes a perception in the common perception list */
 	public boolean removePercept(Literal per) {
 		if (per != null) {
 			uptodateAgs.clear();
 			return percepts.remove(per);
 		} 
 		return false;
 	}
 	
 	/** Removes all percepts in the common perception list that unifies with <i>per</i>.
 	 *  
 	 *  Example: removePerceptsByUnif(Literal.parseLiteral("position(_)")) will remove 
 	 *  all percepts that unifies "position(_)".
 	 *  
 	 *  @return the number of removed percepts.
 	 */
 	public int removePerceptsByUnif(Literal per) {
 		int c = 0;
         if (! percepts.isEmpty()) { // has global perception?
             synchronized (percepts) {
             	Iterator<Literal> i = percepts.iterator();
             	while (i.hasNext()) {
             		Literal l = i.next();
             		if (new Unifier().unifies(l,per)) {
             			i.remove();
             			c++;
             		}
             	}
             }
             if (c>0) uptodateAgs.clear();
         }
 		return c;
 	}
 	
 	
 	/** Clears the list of global percepts */
 	public void clearPercepts() {
         if (!percepts.isEmpty()) {
             uptodateAgs.clear();
             percepts.clear();
         }
 	}
 	
 	/** Returns true if the list of common percepts contains the perception <i>per</i>. */
 	public boolean containsPercept(Literal per) {
 		if (per != null) {
 			return percepts.contains(per);
 		} 
 		return false;
 	}
 	
 	/** Adds a perception for a specific agent */
 	public void addPercept(String agName, Literal per) {
 		if (per != null && agName != null) {
 			List<Literal> agl = agPercepts.get(agName);
 			if (agl == null) {
 				agl = Collections.synchronizedList(new ArrayList<Literal>());
 				uptodateAgs.remove(agName);
 				agl.add(per);
 				agPercepts.put( agName, agl);
 			} else {
 				if (! agl.contains(per)) {
 					uptodateAgs.remove(agName);
 					agl.add(per);
 				}
 			}
 		}
 	}
 	
 	/** Removes a perception for an agent */
 	public boolean removePercept(String agName, Literal per) {
 		if (per != null && agName != null) {
 			List<Literal> agl = agPercepts.get(agName);
 			if (agl != null) {
 				uptodateAgs.remove(agName);
 				return agl.remove(per);
 			}
 		}
 		return false;
 	}
 
 	/** Removes from an agent perception all percepts that unifies with <i>per</i>. 
 	 *  @return the number of removed percepts.
 	 */
 	public int removePerceptsByUnif(String agName, Literal per) {
 		int c = 0;
 		if (per != null && agName != null) {
 			List<Literal> agl = agPercepts.get(agName);
 			if (agl != null) {
 	            synchronized (agl) {
 	            	Iterator<Literal> i = agl.iterator();
 	            	while (i.hasNext()) {
 	            		Literal l = i.next();
 	            		if (new Unifier().unifies(l,per)) {
 	            			i.remove();
 	            			c++;
 	            		}
 	            	}
 	            }
 	            if (c>0) uptodateAgs.remove(agName);
 			}
 		}
 		return c;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public boolean containsPercept(String agName, Literal per) {
 		if (per != null && agName != null) {
 			List agl = (List)agPercepts.get(agName);
 			if (agl != null) {
 				return agl.contains(per);
 			}
 		}
 		return false;
 	}
 
 	/** Clears the list of percepts of a specific agent */
 	public void clearPercepts(String agName) {
 		if (agName != null) {
 			List<Literal> agl = agPercepts.get(agName);
 			if (agl != null) {
 				uptodateAgs.remove(agName);
 				agl.clear();
 			}
 		}
 	}
     
     /** 
      * Called by the agent infrastructure to schedule an action to be 
      * executed on the environment
      */
     public void scheduleAction(final String agName, final Structure action, final Object infraData) {
         executor.execute(new Runnable() {
             public void run() {
                 try {
                     boolean success = executeAction(agName, action);
                     environmentInfraTier.actionExecuted(agName, action, success, infraData);
                 } catch (Exception ie) {
                     if (!(ie instanceof InterruptedException)) {
                         logger.log(Level.WARNING, "act error!",ie);
                     }
                 }
             }
         });        
     }
 	
     /**
      * Execute an action on the environment. This method is probably overridden in the user environment class.
      */
     public boolean executeAction(String agName, Structure act) {
         return true;
     }
 }
