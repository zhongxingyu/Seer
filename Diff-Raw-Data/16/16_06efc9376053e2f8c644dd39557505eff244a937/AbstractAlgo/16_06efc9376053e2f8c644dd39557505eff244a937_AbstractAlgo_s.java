 package de.unisiegen.informatik.bs.alvis.vm;
 
 /**
  * @ author Sebastian Schmitz, Dominik Dingel
  */
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.locks.Lock;
 
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 
 public interface AbstractAlgo extends Runnable {
 
 	/**
 	 * 
	 * @return Reference of all Variables, holded by the algo
 	 */
 	public ArrayList<PCObject> getVariableReferences();
 
 	/**
 	 * TODO update javadoc
 	 * @return Null Objects for all Datatypes the algo expects for the
 	 *         setParameters Call
 	 */
 	public HashMap<PCObject, String> getParameterTypes();
 
 	/**
 	 * 
 	 * @param paras
 	 *            are the Startparameters of the algo, have to passed like
 	 *            specified in getParameterTypes
 	 */
 	public void setParameters(ArrayList<PCObject> paras);
 
 	/**
 	 * 
 	 * @param wantToListen
 	 *            Breakpoint listener, will be informed in case of an Breakpoint
 	 *            event, currently only one Listener can be active at the same
 	 *            time, could be delete with passing a null Object as BPListener
 	 */
 	public void addBPListener(BPListener wantToListen);
 
 	/**
 	 * 
 	 * @param wantToListen
 	 *            Decision Point listener, will be informed in case of an
 	 *            Decision Point event, currently only one Listener can be
 	 *            active at the same time, could be delete with passing a null
 	 *            Object as DPListener
 	 */
 	public void addDPListener(DPListener wantToListen);
 
 	/**
 	 * set own Break Field, should be used in context with Thread.notify
 	 */
 	public void stopBreak();
 	
 	/**
 	 * lock handled on all threads
 	 */
 	public void setLock(Lock toLockOn);
 }
