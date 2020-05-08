 package de.unisiegen.informatik.bs.alvis.vm;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 
 
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PseudoCodeObject;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.SortableCollection;
 
 /**
  * 
  * @author Dominik Dingel manages the algo and is responsible for the run
  * 
  *         take care, could be multiple times run (singleton pattern works not
  *         that fine with multiple classloaders)
  * 
  *         TODO work with Jan a working system to use it with multiple Thread
  *         algos
  *         
  *         TODO cleanup API
  * 
  * 
  * 
  */
 
 public class VirtualMachine {
 
 	// singleton pattern
 	private static VirtualMachine instance = new VirtualMachine();
 
 	/**
 	 * @return local Virtual Machine instance
 	 */
 	public static VirtualMachine getInstance() {
 		return instance;
 	}
 
 	private ArrayList<State> states;
 	private ArrayList<Integer> bpCounter;
 	private ArrayList<Integer> dpCounter;
 	private int bpIndex;
 	private int dpIndex;
 	private ArrayList<PseudoCodeObject> parameters;
 	private Thread algoThread;
 	private AbstractAlgo algoToRun;
 	private Class<AbstractAlgo> algoClass;
 	private BPListener bplisten;
 	private DPListener dplisten;
 	private int stateIndex;
 
 	/**
 	 * sets AlgoClass from which to instanciate, located by the modified
 	 * classloader, provied by sebastian
 	 * 
 	 * @param algo
 	 * @return success
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public boolean setAlgoClassToRun(Class algo) {
 		if (algo != null) {
 			for (Class i : algo.getInterfaces()) {
 				if (i.equals(AbstractAlgo.class)) {
 					algoClass = algo;
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * private Method to create virtual Machine instance
 	 */
 	private VirtualMachine() {
 		algoClass = null;
 		states = new ArrayList<State>();
 		bpIndex = 0;
 		dpIndex = 0;
 		stateIndex = 0;
 		bpCounter = new ArrayList<Integer>();
 		dpCounter = new ArrayList<Integer>();
 		parameters = new ArrayList<PseudoCodeObject>();
 		bplisten = null;
 		dplisten = null;
 	}
 
 	/**
 	 * private helper function to create algo Object from class object
 	 */
 	private void createAlgoFromClass() {
 		try {
 			algoToRun = (AbstractAlgo) algoClass.getConstructors()[0]
 					.newInstance();
 			algoThread = new Thread(algoToRun);
 			algoToRun.setParameters(parameters);
 		} catch (IllegalArgumentException e1) {
 			e1.printStackTrace();
 		} catch (InstantiationException e1) {
 			e1.printStackTrace();
 		} catch (IllegalAccessException e1) {
 			e1.printStackTrace();
 		} catch (InvocationTargetException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	/**
 	 * reset all states
 	 */
 	public void resetState() {
 		bpCounter.clear();
 		dpCounter.clear();
 		bpIndex = 0;
 		dpIndex = 0;
 	}
 
 	/**
 	 * let the vm start and run the Algorithm every breakpoint will pause 1 sec.
 	 * one decisionpoints will nothing happen, so the order highly depends on
 	 * the current memory layout
 	 */
 	public void startAutoRun() {
 		this.createAlgoFromClass();
 
 		// Breakpoint listener
 		algoToRun.addBPListener(new BPListener() {
 			public void onBreakPoint(int BPNr) {
 				try {
 					// TODO configurable Time for breakpoint sleep, currently 2
 					// sec.
 					Thread.sleep(2000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				synchronized (algoThread) {
 					algoToRun.stopBreak();
 					algoThread.notify();
 				}
 			}
 		});
 		// nothing happens on Decision Point
 		algoToRun.addDPListener(new DPListener() {
 
 			@Override
 			public void onDecisionPoint(int DPNr, SortableCollection toSort) {
 			}
 		});
 		algoThread.start();
 	}
 
 	/**
 	 * let the algo run, every breakpoint will be a new state created
 	 */
 	public void startDefaultRun() {
 		states.add(new State(null, bpIndex, dpIndex, false));
 		stateIndex = 0;
 		this.createAlgoFromClass();
 
 		algoToRun.addBPListener(new BPListener() {
 			public void onBreakPoint(int BPNr) {
 				// this is a new Breakpoint
 				if (bpIndex < BPNr) {
 					bpCounter.add(new Integer(1));
 					bpIndex++;
 				}
 
 				// revisiting the actual Breakpoint (inner loop)
 				// or traveling through nested loops
 				else {
 					bpIndex = BPNr;
 					int tmp = bpCounter.get(bpIndex - 1).intValue();
 					tmp++;
 					bpCounter.set(bpIndex - 1, new Integer(tmp));
 				}
 				states.add(new State(null, bpIndex, dpIndex, false));
 				stateIndex++;
 
 				if (bplisten != null) {
 					bplisten.onBreakPoint(BPNr);
 				}
 			}
 		});
 		// on decision Point ask anyone who has registered
 		algoToRun.addDPListener(new DPListener() {
 			public void onDecisionPoint(int DPNr, SortableCollection toSort) {
 				if (dplisten != null) {
 					dplisten.onDecisionPoint(DPNr, toSort);
 				}
 			}
 		});
 		algoThread.start();
 	}
 
 	/**
 	 * 
 	 */
 	public void stepForward() {
 		if (algoThread.isAlive()) {
 			synchronized (algoThread) {
 				algoToRun.stopBreak();
 				algoThread.notify();
 			}
 		}
 	}
 
 	/**
 	 * stepping backwards, will set new breakpoint and restart the algo from
 	 * beginning
 	 */
 	public void stepBackward() {
 		// if i'm 1, the next status would be zero, the beginning state every
 		// other value would also be totally useless
 		if (stateIndex > 1) {
 			this.resetState();
 			this.createAlgoFromClass();
 			algoToRun.addBPListener(new BPListener() {
 				// saving the state where we want to end
 				int curSI = stateIndex - 1;
 
 				public void onBreakPoint(int BPNr) {
 					// this is a new Breakpoint
 					if (bpIndex < BPNr) {
 						bpCounter.add(new Integer(1));
 						bpIndex++;
 					}
 					// revisiting the actual Breakpoint (inner loop)
 					// or traveling through nested loops
 					else {
 						bpIndex = BPNr;
 						int tmp = bpCounter.get(bpIndex - 1).intValue();
 						tmp++;
 						bpCounter.set(bpIndex - 1, new Integer(tmp));
 					}
 					states.add(new State(null, bpIndex, dpIndex, false));
 					stateIndex++;
 					// we reached our goal
 					if (stateIndex == curSI) {
 						// overwrite breakpoint listener to restore previous
 						// behaviour
 						algoToRun.addBPListener(new BPListener() {
 							public void onBreakPoint(int BPNr) {
 								// this is a new Breakpoint
 								if (bpIndex < BPNr) {
 									bpCounter.add(new Integer(1));
 									bpIndex++;
 								}
 								// revisiting the actual Breakpoint (inner loop)
 								// or traveling through nested loops
 								else {
 									bpIndex = BPNr;
 									int tmp = bpCounter.get(bpIndex - 1)
 											.intValue();
 									tmp++;
 									bpCounter
 											.set(bpIndex - 1, new Integer(tmp));
 								}
 								states.add(new State(null, bpIndex, dpIndex,
 										false));
 								stateIndex++;
 								if (bplisten != null) {
 									bplisten.onBreakPoint(BPNr);
 								}
 							}
 						});
 					} else {
 						synchronized (algoThread) {
 							algoToRun.stopBreak();
 							algoThread.notify();
 						}
 					}
 				}
 			});
 
 			algoToRun.addDPListener(new DPListener() {
 				public void onDecisionPoint(int DPNr, SortableCollection toSort) {
 					if (dplisten != null) {
 						dplisten.onDecisionPoint(DPNr, toSort);
 					}
 				}
 			});
 			stateIndex = 0;
 			this.states.clear();
 			algoThread.start();
 		} else {
 			this.startDefaultRun();
 		}
 	}
 
 	/**
 	 * @return if Algo Thread is still alive = running
 	 */
 	public boolean isAlive() {
 		if (algoToRun == null) {
 			return false;
 		}
 		return algoThread.isAlive();
 	}
 
 	/**
 	 * only usable if thread is in breakpoint
 	 * 
 	 * @return Reference on all Variables
 	 */
 	public ArrayList<PseudoCodeObject> getRunningRef() {
 		return algoToRun.getVariableReferences();
 	}
 
 	/**
 	 * 
 	 * @param dplisten
 	 */
 	public void addDPListener(DPListener dplisten) {
 		this.dplisten = dplisten;
 	}
 
 	/**
 	 * 
 	 * @param bplisten
 	 *            to listen for
 	 */
 	public void addBPListener(BPListener bplisten) {
 		this.bplisten = bplisten;
 	}
 
 	/**
 	 * 
 	 * @param para
 	 *            adding Parameter to the algo
 	 */
 	public void addParameter(PseudoCodeObject para) {
 		parameters.add(para);
 	}
 
 	/**
 	 * Passed Parameters to Algo will be cleared
 	 */
 	public void removeOldParameters() {
 		this.parameters.clear();
 	}
 
 	/**
 	 * let the current thread join the vm thread
 	 */
 	public void waitForAlgo() {
 		try {
 			algoThread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * will wait till the algo reach breakpoint or is terminated
 	 */
 	public void waitForBreakPoint() {
 		while (algoThread.getState().compareTo(Thread.State.TIMED_WAITING) != 0
 				&& algoThread.getState().compareTo(Thread.State.WAITING) != 0
 				&& algoThread.getState().compareTo(Thread.State.TERMINATED) != 0) {
 			synchronized (this) {
 				try {
 					this.wait(100);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @return the ArrayList with dummy object, representing the types needed
 	 *         for the Algo to start
 	 */
 	@SuppressWarnings("unchecked")
 	public ArrayList<PseudoCodeObject> getStartParameters() {
 		if (algoClass != null) {
 			this.createAlgoFromClass();
 			if (algoToRun != null) {
 				Method[] meths = algoClass.getMethods();
 				for (Method m : meths) {
 					if (m.getName().equals("getParameterTypes")) {
 						try {
 							return ((ArrayList<PseudoCodeObject>) m.invoke(
 									algoToRun, (Object[]) null));
 						} catch (IllegalArgumentException e) {
 							e.printStackTrace();
 						} catch (IllegalAccessException e) {
 							e.printStackTrace();
 						} catch (InvocationTargetException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
	@SuppressWarnings({ "rawtypes"})
	public static Class getAlgo(String fileName){
 		DynaCode dynacode = new DynaCode();
		dynacode.addSourceDir(new File(instance.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().toString()));
		System.out.println(instance.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().toString());
 		try {
			return dynacode.loadClass(fileName);
 		} catch (ClassNotFoundException e) {
			System.out.println("YOU HAVE LOST THE MATCH!"+ e.getMessage());
 		}
 		return null;
 	}
 
 }
