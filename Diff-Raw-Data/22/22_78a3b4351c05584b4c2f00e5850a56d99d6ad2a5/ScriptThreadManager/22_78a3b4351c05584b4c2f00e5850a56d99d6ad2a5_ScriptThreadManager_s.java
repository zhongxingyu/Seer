 package org.eclipse.dltk.debug.internal.core.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.ISuspendResume;
 import org.eclipse.debug.core.model.ITerminate;
 import org.eclipse.debug.core.model.IThread;
 import org.eclipse.dltk.dbgp.IDbgpSession;
 import org.eclipse.dltk.dbgp.IDbgpThreadAcceptor;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 
 public class ScriptThreadManager implements IDbgpThreadAcceptor, ITerminate,
 		ISuspendResume {
 
 	// Helper methods
 	private interface IThreadBoolean {
 		boolean get(IThread thread);		
 	}
 
 	private boolean getThreadBoolean(IThreadBoolean b) {
 		synchronized (threads) {
 			IThread[] ths = getThreads();
 
 			if (ths.length == 0) {
 				return false;
 			}
 
 			for (int i = 0; i < ths.length; ++i) {
 				if (!b.get(ths[i])) {
 					return false;
 				}
 			}
 
 			return true;
 		}
 	}
 
 	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
 
 	private List threads = new ArrayList();
 
 	private volatile boolean waitingForThreads = true;
 
 	private ScriptDebugTarget target;
 
 	private String acceptId;
 
 	protected void fireThreadAccepted(IScriptThread thread, boolean first) {
 		Object[] list = listeners.getListeners();
 		for (int i = 0; i < list.length; ++i) {
 			((IDbgpThreadManagerListener) list[i])
 					.threadAccepted(thread, first);
 		}
 	}
 
 	protected void fireAllThreadsTerminated() {
 		Object[] list = listeners.getListeners();
 		for (int i = 0; i < list.length; ++i) {
 			((IDbgpThreadManagerListener) list[i]).allThreadsTerminated();
 		}
 	}
 
 	public void addListener(IDbgpThreadManagerListener listener) {
 		listeners.add(listener);
 	}
 
 	public void removeListener(IDbgpThreadManagerListener listener) {
 		listeners.remove(listener);
 	}
 
 	public boolean isWaitingForThreads() {
 		return waitingForThreads;
 	}
 
 	public String getAcceptId() {
 		return acceptId;
 	}
 
 	public boolean hasThreads() {
 		synchronized (threads) {
 			return !threads.isEmpty();
 		}
 	}
 
 	public IThread[] getThreads() {
 		synchronized (threads) {
 			return (IThread[]) threads.toArray(new IThread[threads.size()]);
 		}
 	}
 
 	protected void register() {
 		DLTKDebugPlugin.getDefault().getDbgpService().registerAcceptor(
 				acceptId, this);
 	}
 
 	protected void unregister() {
 		DLTKDebugPlugin.getDefault().getDbgpService().unregisterAcceptor(
 				acceptId);
 	}
 
 	public ScriptThreadManager(ScriptDebugTarget target, String acceptId) {
 		if (target == null || acceptId == null) {
 			throw new IllegalArgumentException();
 		}
 
 		this.target = target;
 		this.acceptId = acceptId;
 
 		register();
 	}
 
 	// IDbgpThreadAcceptor
 	public void acceptDbgpThread(IDbgpSession session) {
 		synchronized (threads) {
 			try {
 				ScriptThread thread = new ScriptThread(target, session, this);
 				threads.add(thread);
 
 				boolean first = waitingForThreads;
 				waitingForThreads = false;
 
 				fireThreadAccepted(thread, first);
 
 				DebugEventHelper.fireCreateEvent(thread);
 
 				// Auto start
 				thread.resume();
 			} catch (Exception e) {
 				DLTKDebugPlugin.log(e);
 			}
 		}
 	}
 
 	public void terminateThread(IScriptThread thread) {
 		synchronized (threads) {
 			threads.remove(thread);
 			DebugEventHelper.fireTerminateEvent(thread);
 
 			if (!hasThreads()) {
 				unregister();
 				fireAllThreadsTerminated();
 			}
 		}
 	}
 
 	// ITerminate
 	public boolean canTerminate() {
 		synchronized (threads) {
 			IThread[] ths = getThreads();
 
 			if (ths.length == 0) {
				return true;
 			}
 
 			for (int i = 0; i < ths.length; ++i) {
 				if (!ths[i].canTerminate()) {
 					return false;
 				}
 			}
 
 			return true;
 		}
 	}
 
 	public boolean isTerminated() {
 		if (!hasThreads()) {
 			return !isWaitingForThreads();
 		}
 
 		return getThreadBoolean(new IThreadBoolean() {
 			public boolean get(IThread thread) {
 				return thread.isTerminated();
 			}
 		});
 	}
 
 	public void terminate() throws DebugException {
 		synchronized (threads) {
 			IThread[] threads = getThreads();
 			for (int i = 0; i < threads.length; ++i) {
 				threads[i].terminate();
 			}
 
 			waitingForThreads = false;
 		}
 	}
 
 	public boolean canResume() {
 		return getThreadBoolean(new IThreadBoolean() {
 			public boolean get(IThread thread) {
 				return thread.canResume();
 			}
 		});
 	}
 
 	public boolean canSuspend() {
 		return getThreadBoolean(new IThreadBoolean() {
 			public boolean get(IThread thread) {
 				return thread.canSuspend();
 			}
 		});
 	}
 
 	public boolean isSuspended() {
 		return getThreadBoolean(new IThreadBoolean() {
 			public boolean get(IThread thread) {
 				return thread.isSuspended();
 			}
 		});
 	}
 
 	public void resume() throws DebugException {
 		synchronized (threads) {
 			IThread[] threads = getThreads();
 			for (int i = 0; i < threads.length; ++i) {
 				threads[i].resume();
 			}
 		}
 	}
 
 	public void suspend() throws DebugException {
 		synchronized (threads) {
 			IThread[] threads = getThreads();
 			for (int i = 0; i < threads.length; ++i) {
 				threads[i].suspend();
 			}
 		}
 	}
 }
