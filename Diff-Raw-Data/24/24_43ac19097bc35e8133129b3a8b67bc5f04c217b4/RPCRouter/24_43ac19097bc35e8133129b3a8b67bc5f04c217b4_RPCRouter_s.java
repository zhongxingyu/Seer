 package com.labs.rpc;
 
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.*;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.atomic.AtomicBoolean;
 import com.labs.rpc.transport.DataPacket;
 import com.labs.rpc.transport.Transport;
 import com.labs.rpc.util.Call;
 import com.labs.rpc.util.CallBack;
 import com.labs.rpc.util.Queue;
 import com.labs.rpc.util.RPCMethod;
 import com.labs.rpc.util.RPCObject;
 import com.labs.rpc.util.RemoteException;
 
 /**
  * Handle incoming and outgoing calls
  * @author Benjamin Dezile
  */
 public class RPCRouter {
 
 	protected static final String VOID = "void";	// Return value for void methods
 	protected static int DEFAULT_TIMEOUT = 5;		// Default timeout for calls (in seconds)
 	protected static int TIMEOUT;					// Timeout value for calls
 	
 	protected AtomicBoolean killed;					// Whether this router is dead
 	
 	protected Transport transp;						// Object transport
 	protected Map<String,RPCObject> rpcObjs;		// RPC object map: RPC Name -> Object
 	protected Queue<Call> outCalls;					// Outgoing calls waiting to be sent
 	protected Map<Long,Call> outWait;				// Outgoing calls waiting for returns
 	protected Queue<Call> inCalls;					// Incoming calls waiting for processing
 	protected Map<Long,Call> inWait;				// Incoming calls waiting for end of processing
 	protected RecvThread recvLoop;					// Receiving thread
 	protected XmitThread sendLoop;					// Sending thread
 	protected CallProcessor callProc;				// Processing thread for incoming calls
 	protected CallTimeOuter timeouter;				// Call timeouter
 	protected CallBack onFailureCallback;			// Failure callback
 
 	/**
 	 * Create a new router
 	 * @param obj {@link RPCObject} - Object this router will locally apply calls to
 	 * @param transport {@link Transport} - Transport to be used
 	 */
 	public RPCRouter(RPCObject obj, Transport transport) {
 		this(new RPCObject[] {obj}, transport, null);
 	}
 	
 	/**
 	 * Create a new router
 	 * @param objs {@link RPCObject}[] - Objects this router will locally apply calls to
 	 * @param transport {@link Transport} - Transport to be used
 	 */
 	public RPCRouter(RPCObject[] objs, Transport transport) {
 		this(objs, transport, null);
 	}
 
 	/**
 	 * Create a new router
 	 * @param obj {@link RPCObject} - Object this router will locally apply calls to
 	 * @param transport {@link Transport} - Transport to be used
 	 * @param onFailure {@link CallBack} - Method to call when terminate due to failure (null if none)
 	 */
 	public RPCRouter(RPCObject obj, Transport transport, CallBack onFailure) {
 		this(new RPCObject[]{obj}, transport, onFailure);
 	}
 	
 	/**
 	 * Create a new router
 	 * @param objs {@link RPCObject}[] - Objects this router will locally apply calls to
 	 * @param transport {@link Transport} - Transport to be used
 	 * @param onFailure {@link CallBack} - Method to call when terminate due to failure (null if none)
 	 */
 	public RPCRouter(RPCObject[] objs, Transport transport, CallBack onFailure) {
 		killed = new AtomicBoolean(true);
 		recvLoop = new RecvThread(this);
 		sendLoop = new XmitThread(this);
 		callProc = new CallProcessor(this);
 		timeouter = new CallTimeOuter(this);
 		transp = transport;
 		rpcObjs = new HashMap<String,RPCObject>(objs.length);
 		for (RPCObject obj:objs) {
 			rpcObjs.put(obj.getRPCName(), obj);
 		}
 		onFailureCallback = onFailure;
 	}
 	
 	/**
 	 * Get the associated transport
 	 * @return {@link Transport}
 	 */
 	public Transport getTransport() {
 		return transp;
 	}
 	
 	/**
 	 * Register a new target
 	 * @param name {@link String} - Associated target name
 	 * @param obj {@link RPCObject} - Target object
 	 */
 	public void registerTargetObject(String name, RPCObject obj) {
 		rpcObjs.put(name, obj);
 	}
 	
 	/**
 	 * Remove a given target 
 	 * @param name {@link String} - Target name
 	 */
 	public void unregisterTargetObject(String name) {
 		rpcObjs.remove(name);
 	}
 	
 	/**
 	 * Return a given target object
 	 * @param name {@link String} - Target name
 	 * @return {@link RPCObject} Null if no match
 	 */
 	protected RPCObject getTargetObject(String name) {
 		return rpcObjs.get(name);
 	}
 	
 	/**
 	 * Start the processing loops and initialize internal states
 	 */
 	public void start() {
 		start(0);
 	}
 	
 	/**
 	 * Start the processing loops and initialize internal states
 	 * @param timeout int - General timeout to use (in seconds)
 	 */
 	public void start(int timeout) {
 		killed.set(false);
 		TIMEOUT = timeout > 0 ? timeout : DEFAULT_TIMEOUT;
 		if (outCalls == null) {
 			outCalls = new Queue<Call>();
 		}
 		if (outWait == null) {
 			outWait = new HashMap<Long,Call>(0);
 		} else {
 			/* Reset pending calls to prevent them from timing out */
 			for (Call call:outWait.values()) {
 				call.resetStartTime();
 			}
 		}
 		if (inCalls == null) {
 			inCalls = new Queue<Call>();
 		}
 		if (inWait == null) {
 			inWait = new HashMap<Long,Call>(0);
 		}
 		if (timeouter == null) {
 			timeouter = new CallTimeOuter(this);
 		}
 		if (!timeouter.isAlive()) {
 			timeouter.start();
 		}
 		if (recvLoop == null) {
 			recvLoop = new RecvThread(this);
 		}
 		if (!recvLoop.isAlive()) {
 			recvLoop.start();
 		}
 		if (sendLoop == null) {
 			sendLoop = new XmitThread(this);
 		}
 		if (!sendLoop.isAlive()) {
 			sendLoop.start();
 		}
 		if (callProc == null) {
 			callProc = new CallProcessor(this);
 		}
 		if (!callProc.isAlive()) {
 			callProc.start();
 		}
 	}
 	
 	/**
 	 * Stop all processing loops and reset internal states 
 	 */
 	public void stop() {
 		stop(true);
 	}
 	
 	/**
 	 * Stop all processing loops
 	 * @param flush boolean - Reset internal states if true 
 	 */
 	public void stop(boolean flush) {
 		kill();
 		if (flush) {
 			if (outCalls != null) {
 				outCalls.clear();
 			}
 			if (outWait != null) {
 				outWait.clear();
 			}
 			if (inCalls != null) {
 				inCalls.clear();
 			}
 			if (inWait != null) {
 				inWait.clear();
 			}
 		}
 	}
 	
 	/**
 	 * Kill all processing loop
 	 */
 	private void kill() {
 		if (killed.get()) {
 			return;
 		}
 		killed.set(true);
		callProc.interrupt();
		callProc = null;
		recvLoop.interrupt();
		recvLoop = null;
		sendLoop.interrupt();
		sendLoop = null;
		timeouter.interrupt();
		timeouter = null;	
 	}
 	
 	/**
 	 * Return whether this router is fully running.<br>
 	 * Meaning that the router started and all internal threads are alive.
 	 * @return boolean
 	 */
 	public boolean isAlive() {
 		if (!killed.get()) {
 			if (callProc == null || !callProc.isAlive()) {
 				return false;
 			}
 			if (recvLoop == null || !recvLoop.isAlive()) {
 				return false;
 			}
 			if (sendLoop == null || !sendLoop.isAlive()) {
 				return false;
 			}
 			if (timeouter == null || !timeouter.isAlive()) {
 				return false;
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Push a remote call out
 	 * @param rc {@link RemoteCall} - Call to send
 	 */
 	public void push(RemoteCall rc) {
 		Call call = new Call(rc);
 		outCalls.offer(call);
 		synchronized(outWait) {
 			outWait.put(rc.getSeq(), call);
 		}
 	}
 	
 	/**
 	 * Get a call return value if available
 	 * @param rc {@link RemoteCall} - Call to get return for
 	 * @return {@link Object}
 	 * @throws IllegalStateException If not available yet
 	 * @throws IllegalArgumentException If not found
 	 * @throws RemoteException When something went wrong on the remote side
 	 * @throws TimeoutException When the call fails to return in time
 	 */
 	public Object getReturn(RemoteCall rc) throws IllegalArgumentException, IllegalStateException, RemoteException, TimeoutException {
 		return getReturn(rc.getSeq());
 	}
 	
 	/**
 	 * Get a call return value if available
 	 * @param seq long - Call sequence number
 	 * @return {@link Object}
 	 * @throws IllegalStateException If not available yet
 	 * @throws IllegalArgumentException If not found
 	 * @throws RemoteException When something went wrong on the remote side
 	 * @throws TimeoutException When the call fails to return in time
 	 */
 	public Object getReturn(long seq) throws IllegalArgumentException, IllegalStateException, RemoteException, TimeoutException {
 		Call call;
 		synchronized(outWait) {
 			call = outWait.remove(seq);
 		}
 		if (call == null) {
 			throw new IllegalArgumentException("No such call: " + seq);
 		}
 		if (call.isReturned()) {
 			/* Got return value */
 			Object ret = call.getReturnValue();
 			if (ret instanceof RemoteException) {
 				throw (RemoteException)ret;
 			}
 			return ret;
 		} else if (call.isTimedOut()) {
 			/* Timed out */
 			throw new TimeoutException();
 		}
 		/* Still pending so put it back and exit */
 		synchronized(outWait) {
 			outWait.put(seq, call);
 		}
 		throw new IllegalStateException("Not returned yet");
 	}
 
 	/**
 	 * Wait until the given call returns
 	 * @param rc {@link RemoteCall} - Initial call
 	 * @return {@link Object} Returned value
 	 * @throws IllegalArgumentException If not found
 	 * @throws RemoteException When something went wrong on the remote side
 	 * @throws TimeoutException When the call fails to return in time
 	 */
 	public Object getReturnBlocking(RemoteCall rc) throws IllegalArgumentException, RemoteException, TimeoutException {
 		return getReturnBlocking(rc.getSeq());
 	}
 	
 	/**
 	 * Wait until the given call returns
 	 * @param seq long - Call sequence number
 	 * @return {@link Object} Returned value
 	 * @throws IllegalArgumentException If not found
 	 * @throws RemoteException When something went wrong on the remote side
 	 * @throws TimeoutException When the call fails to return in time
 	 */
 	public Object getReturnBlocking(long seq) throws IllegalArgumentException, RemoteException, TimeoutException {
 		try {
 			Call call;
 			synchronized(outWait) { call = outWait.get(seq); }
 			call.waitForReturn();
 			return getReturn(seq);
 		} catch (InterruptedException e) {
 			return null;
 		}
 	}
 	
 	/**
 	 * Return whether the given call is in the system
 	 * @param seq long - Call sequence number
 	 * @return boolean
 	 */
 	protected boolean hasCall(long seq) {
 		synchronized(outWait) { 
 			return (outWait.get(seq) != null);
 		}
 	}
 	
 	/**
 	 * Receiving thread
 	 * @author Benjamin Dezile
 	 */
 	private static class RecvThread extends Thread {
 		
 		private boolean on;
 		private RPCRouter router;
 		
 		public RecvThread(RPCRouter r) {
 			super("RPC receiving thread");
 			setDaemon(false);
 			on = true;
 			router = r;
 		}
 		
 		public void interrupt() {
 			on = false;
 			super.interrupt();
 		}
 		
 		public void run() {
 			DataPacket dp;
 			RemoteCall rc;
 			RemoteCallReturn rcr;
 			Call call;
 			while(on) {
 				try {
 					if ((dp = router.transp.recv()) != null) {
 						if (dp.getType() == RemoteCall.TYPE) {
 							/* Process an incoming call */
 							rc = RemoteCall.fromPacket(dp);
 							Call c = new Call(rc);
 							router.inCalls.offer(c);
 							synchronized(router.inWait) {
 								router.inWait.put(rc.getSeq(), c);
 							}
 						} else if (dp.getType() == RemoteCallReturn.TYPE) {
 							/* Process a call return */
 							rcr = RemoteCallReturn.fromPacket(dp);
 							synchronized(router.outWait) {
 								call = router.outWait.get(rcr.getSeq());
 							}
 							if (call == null) {
 								Thread.sleep(2);
 								/* Retrying as it may have returned before being queued */
 								synchronized(router.outWait) {
 									call = router.outWait.get(rcr.getSeq());
 								}
 							}
 							if (call == null) {
 								throw new Exception("Received return for unknown call: " + rcr.getSeq() + ", outWait = " + router.outWait.toString());	
 							}
 							if (call.isPending()) {
 								call.setReturned(rcr.getValue());
 							}
 						}
 					}
 				} catch (IOException e) {
 					/* Connection error, abort all */
 					router.kill();
 					/* Call failure callback */
 					if (router.onFailureCallback != null) {
 						router.onFailureCallback.call();
 					}
 					break;
 				} catch (InterruptedException e) {
 					break;
 				} catch (Exception e) {
 					System.err.println("Error while receiving rpc data");
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 	
 	/**
 	 * Sending thread
 	 * @author Benjamin Dezile
 	 */
 	private static class XmitThread extends Thread {
 		
 		private boolean on;
 		private RPCRouter router;
 		
 		public XmitThread(RPCRouter r) {
 			super("RPC sending thread");
 			setDaemon(false);
 			on = true;
 			router = r;
 		}
 		
 		public void interrupt() {
 			on = false;
 			super.interrupt();
 		}
 		
 		public void run() {
 			RemoteCall rc;
 			Call call = null;
 			while (on) {
 				try {
 					if ((call = router.outCalls.poll()) != null) {
 						rc = call.getRemoteCall();
 						/* Update call status */
 						call.setPending();
 						/* Send remote call */
 						router.transp.send(rc);
 					}
 				} catch (IOException e) {
 					/* Put the call back into queue to preserve data integrity */
 					if (call != null) {
 						router.outCalls.putBack(call);
 					}
 					/* Connection error, abort all */
 					router.kill();
 					break;
 				} catch (Exception e) {
 					System.err.println("Error while receiving rpc data");
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 	
 	/**
 	 * Processor incoming calls
 	 * @author Benjamin Dezile
 	 */
 	private static class CallProcessor extends Thread {
 		
 		private boolean on;
 		private RPCRouter router;
 		
 		public CallProcessor(RPCRouter r) {
 			super("RPC call processor");
 			setDaemon(false);
 			on = true;
 			router = r;
 		}
 		
 		public void interrupt() {
 			on = false;
 			super.interrupt();
 		}
 		
 		public void run() {
 			RemoteCall rc;
 			Call call;
 			Object ret;
 			while (on) {
 				try {
 					if ((call = router.inCalls.poll()) != null) {
 						rc = call.getRemoteCall();
 						/* Update call status to 'pending' */
 						call.setPending();
 						/* Make the actual call */
 						ret = makeCall(rc);
 						/* Remove the call from the waiting list */
 						router.inWait.remove(call.getRemoteCall().getSeq());
 						/* Send return value back to caller */
 						router.transp.send(new RemoteCallReturn(rc, ret));
 					}
 				} catch (IOException e) {
 					/* Connection error, abort all */
 					router.kill();
 					break;
 				} catch (Exception e) {
 					System.err.println("Error while processing incoming call");
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		private Object makeCall(RemoteCall rc) throws Exception {
 			Method method = null;
 			RPCObject target = router.getTargetObject(rc.getTarget());
 			if (target == null) {
 				throw new NullPointerException("Target not found: " + rc.getTarget());
 			}
 			Class<?> clazz = target.getClass();
 			for (Method meth:clazz.getMethods()) {
 				if (rc.getMethod().equals(meth.getName())) {
 					method = meth;
 					break;
 				}
 			}
 			if (method != null) {
 				if (isRPCMethod(method)) {
 					return callMethod(target, method, rc.getArguments());
 				}
 				throw new Exception("Not a RPC method");
 			}
 			throw new Exception("Method not found");
 		}
 		
 		/**
 		 * Tell whether a method is rpc
 		 * @param meth {@link Method} - Method to test
 		 * @return boolean
 		 */
 		public static boolean isRPCMethod(Method meth) {
 			Annotation[] annotations = meth.getAnnotations();
 			Class<?> aclazz;
 			for (Annotation annotation:annotations) {
 				aclazz = annotation.annotationType();
 				if (aclazz.equals(RPCMethod.class)) {
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		/**
 		 * Call a given method with the provided arguments
 		 * @param obj {@link Object} - Object to call onto
 		 * @param meth {@link Method} - Method to call
 		 * @param args {@link Object}[] - Call arguments
 		 * @return {@link Object} Return value upon success, a remote exception otherwise
 		 */
 		public static Object callMethod(Object obj, Method meth, Object[] args) {
 			Object ret = null;
 			try {
 				ret = meth.invoke(obj,args);
 				Type retType = meth.getReturnType();
 				if ("void".equals(retType.toString())) {
 					return VOID;
 				}
 			} catch (IllegalArgumentException e) {
 				ret = new RemoteException(e);
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				ret = new RemoteException(e);
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				ret = new RemoteException(e.getTargetException());
 				e.getTargetException().printStackTrace();
 			}
 			return ret;
 		}
 		
 	}
 	
 	
 	/**
 	 * Flag old calls as timed out and remove unclaimed calls
 	 * that returned VOID
 	 * @author Benjamin Dezile
 	 */
 	private static class CallTimeOuter extends Thread {
 		
 		private boolean on;
 		private RPCRouter router;
 		
 		public CallTimeOuter(RPCRouter r) {
 			super("RPC call timeouter");
 			setDaemon(false);
 			on = true;
 			router = r;
 		}
 		
 		public void interrupt() {
 			on = false;
 			super.interrupt();
 		}
 		
 		public void run() {
 			Call call;
 			Set<Long> callSeqs;
 			while (on) {
 				/* Get the calls to loop over */
 				synchronized(router.outWait) {
 					callSeqs = new HashSet<Long>(router.outWait.keySet());
 				}
 				if (callSeqs.size() > 0) {
 					for (Long seq:callSeqs) {
 						synchronized(router.outWait) {	
 							call = router.outWait.get(seq); 
 						}
 						if (call == null) {
 							continue;
 						}
 						if (VOID.equals(call.getReturnValue())) {
 							/* Void-return call */
 							if (call.getStartTime() + 2 * RPCRouter.TIMEOUT * 1000 < System.currentTimeMillis()) {
 								synchronized(router.outWait) {
 									/* Remove since it has not been claimed and is useless anyway */
 									router.outWait.remove(seq);
 								}
 							}
 						} else if (call.getStartTime() + RPCRouter.TIMEOUT * 1000 < System.currentTimeMillis()) {
 							/* Timed out non-void call */
 							call.setTimedOut();
 						}
 					}
 				}
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					break;
 				}
 			}
 		}
 		
 	}
 	
 }
