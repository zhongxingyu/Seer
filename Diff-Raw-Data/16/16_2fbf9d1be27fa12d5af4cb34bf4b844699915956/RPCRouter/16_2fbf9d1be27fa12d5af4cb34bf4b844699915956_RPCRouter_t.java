 package com.labs.rpc;
 
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import com.labs.rpc.util.Call;
 import com.labs.rpc.util.Queue;
 import com.labs.rpc.util.RPCMethod;
 import com.labs.rpc.util.RPCObject;
 import com.labs.rpc.util.RemoteException;
 
 // TODO: Implement timeout
 
 /**
  * Handle incoming and outgoing calls
  * @author ben
  */
 public class RPCRouter {
 
 	private AtomicBoolean killed;		// Whether this router is dead
 	
 	private Transport transp;			// Object transport
 	private RPCObject rpcObj;			// RPC object to apply incoming call onto
 	private Queue<Call> outCalls;		// Outgoing calls waiting to be sent
 	private Map<Long,Call> outWait;		// Outgoing calls waiting for returns
 	private Queue<Call> inCalls;		// Incoming calls waiting for processing
 	private Map<Long,Call> inWait;		// Incoming calls waiting for end of processing
 	private RecvThread recvLoop;		// Receiving thread
 	private XmitThread sendLoop;		// Sending thread
 	private CallProcessor callProc;		// Processing thread for incoming calls
 		
 	/**
 	 * Create a new router
 	 * @param obj {@link RPCObject} - Object this router will locally call onto
 	 * @param transport {@link Transport} - Transport to be used
 	 */
 	public RPCRouter(RPCObject obj, Transport transport) {
 		killed = new AtomicBoolean(true);
 		recvLoop = new RecvThread(this);
 		sendLoop = new XmitThread(this);
 		callProc = new CallProcessor(this);
 		transp = transport;
 		rpcObj = obj;
 	}
 	
 	/**
 	 * Start the processing loops and initialize internal states
 	 */
 	public void start() {
 		killed.set(false);
 		outCalls = new Queue<Call>();
 		outWait = new HashMap<Long,Call>(0);
 		inCalls = new Queue<Call>();
 		inWait = new HashMap<Long,Call>(0);
 		recvLoop.start();
 		sendLoop.start();
 		callProc.start();
 	}
 	
 	/**
 	 * Stop all processing loops and flush internal states
 	 */
 	public void stop() {
 		kill();
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
 	
 	/**
 	 * Kill all processing loop
 	 */
 	private void kill() {
 		synchronized(killed) {
 			if (killed.get()) {
 				return;
 			}
 			killed.set(true);
 		}
 		callProc.interrupt();
 		recvLoop.interrupt();
 		sendLoop.interrupt();
 		transp.shutdown();		
 	}
 	
 	/**
 	 * Return whether this router is still running
 	 * @return boolean
 	 */
 	public boolean isAlive() {
 		return !killed.get();
 	}
 	
 	/**
 	 * Push a remote call out
 	 * @param rc {@link RemoteCall} - Call to send
 	 */
 	public void push(RemoteCall rc) {
		Call call = new Call(rc);
		outCalls.offer(call);
		outWait.put(rc.getSeq(), call);
 	}
 	
 	/**
 	 * Get a call return value if available
 	 * @param rc {@link RemoteCall} - Call to get return for
 	 * @return {@link Object}
 	 * @throws IllegalStateException If not available yet
 	 * @throws IllegalArgumentException If not found
 	 * @throws RemoteException When something went wrong on the remote side
 	 */
 	public Object getReturn(RemoteCall rc) throws IllegalArgumentException, IllegalStateException, RemoteException {
 		return getReturn(rc.getSeq());
 	}
 	
 	/**
 	 * Get a call return value if available
 	 * @param seq long - Call sequence number
 	 * @return {@link Object}
 	 * @throws IllegalStateException If not available yet
 	 * @throws IllegalArgumentException If not found
 	 * @throws RemoteException When something went wrong on the remote side
 	 */
 	public Object getReturn(long seq) throws IllegalArgumentException, IllegalStateException, RemoteException {
		Call call = outWait.remove(seq);
 		if (call == null) {
 			throw new IllegalArgumentException("No such call: " + seq);
 		}
 		if (call.isReturned()) {
 			Object ret = call.getReturnValue();
 			if (ret instanceof RemoteException) {
 				throw (RemoteException)ret;
 			}
 			return ret;
 		}
		outWait.put(seq, call);
 		throw new IllegalStateException("Not returned yet");
 	}
 
 	/**
 	 * Wait until the given call returns
 	 * @param rc {@link RemoteCall} - Initial call
 	 * @return {@link Object} Returned value
 	 * @throws RemoteException When something went wrong on the remote side
 	 */
 	public Object getReturnBlocking(RemoteCall rc) throws RemoteException {
 		return getReturnBlocking(rc.getSeq());
 	}
 	
 	/**
 	 * Wait until the given call returns
 	 * @param seq long - Call sequence number
 	 * @return {@link Object} Returned value
 	 * @throws IllegalArgumentException If not found
 	 * @throws RemoteException When something went wrong on the remote side
 	 */
 	public Object getReturnBlocking(long seq) throws IllegalArgumentException, RemoteException {
 		try {
 			while (true) {
 				try {
 					return getReturn(seq);
 				} catch (IllegalStateException e) {
 					/* Not here yet */
 					Thread.sleep(25);
 					continue;
 				} catch (IllegalArgumentException e) {
 					/* Call not found */
 					throw e;
 				}
 			}
 		} catch (InterruptedException e) {
 			return null;
 		}
 	}
 	
 	/**
 	 * Receiving thread
 	 * @author ben
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
							router.inWait.put(rc.getSeq(), c);
 						} else if (dp.getType() == RemoteCallReturn.TYPE) {
 							/* Process a call return */
 							rcr = RemoteCallReturn.fromPacket(dp);
 							if ((call = router.outWait.get(rcr.getSeq())) != null) {
 								call.setReturned(rcr.getValue());
 							} else {
 								throw new Exception("Received return for unknown call: " + rcr.getSeq());
 							}
 						}
 					}
 				} catch (IOException e) {
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
 	 * Sending thread
 	 * @author ben
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
 			Call call;
 			while (on) {
 				try {
 					if ((call = router.outCalls.get()) != null) {
 						rc = call.getRemoteCall();
 						/* Update call status */
 						call.setPending();
 						/* Send remote call */
 						router.transp.send(rc);
 					}
 				} catch (IOException e) {
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
 	 * @author ben
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
 					if ((call = router.inCalls.get()) != null) {
 						rc = call.getRemoteCall();
 						/* Update call status to 'pending' */
 						call.setPending();
 						router.inWait.put(rc.getSeq(), call);
 						/* Make the actual call */
 						ret = makeCall(rc);
 						/* Update call status to 'returned' */
 						call.setReturned(ret);
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
 			Class<?> clazz = router.rpcObj.getClass();
 			for (Method meth:clazz.getMethods()) {
 				if (rc.getMethod().equals(meth.getName())) {
 					method = meth;
 					break;
 				}
 			}
 			if (method != null) {
 				if (isRPCMethod(method)) {
 					return callMethod(router.rpcObj, method, rc.getArguments());
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
 	
 }
