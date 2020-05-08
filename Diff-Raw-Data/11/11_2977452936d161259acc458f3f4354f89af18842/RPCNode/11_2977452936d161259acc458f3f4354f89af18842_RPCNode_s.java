 package node.rpc;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Vector;
 import node.facebook.FacebookException;
 import node.reliable.ReliableDeliveryNode;
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class RPCNode extends ReliableDeliveryNode 
 {
 	private class MethodInfo
 	{
 		public RPCSkeleton skeleton;
 		public String methodName;
 		
 		public MethodInfo()
 		{
 			this.skeleton = null;
 			this.methodName = null;
 		}
 	}
 	
 	private class CallInfo
 	{
 		public String methodName;
 		public RPCStub caller;
 	}
 	
 	public static final int ERROR_INVALID_METHOD 		= 0x10001;
 	public static final int ERROR_UNHANDLED_EXCEPTION 	= 0x10002;
 	
 	private Hashtable<String, MethodInfo> m_methods;
 	private Hashtable<Integer, CallInfo> m_ongoingCalls;
 	protected Queue<String> _commandQueue;
 	
 	
 	/**
 	 * RPCNode()
 	 */
 	public RPCNode()
 	{
 		_commandQueue = new LinkedList<String>();
 		m_methods = new Hashtable<String, MethodInfo>();
 		m_ongoingCalls = new Hashtable<Integer, CallInfo>();
 	}
 	
 
 	@Override
 	public void start()
 	{
 		super.start();
 		//this.recoverTempFileFromCrash();
 	}
 	
 	/**
 	 * Parses the commands sent to the client by the simulator or the emulator.
 	 */
 	/*
 	@Override
 	public void onCommand(String command)
 	{
 		_commandQueue.add(command);
 		
 		// If the _commandQueue was empty, execute the command now.
 		// Otherwise it'll be executed when the current command finishes.
 		
 		// TODO: allowing multiple simultaneous commands for now
 		//if (_commandQueue.size() == 1)
 		{
 			executeClientCommand(command);
 		}								
 	}
 	*/
 	
 	public void callMethod(int targetSender, String methodName, Vector<String> params, Integer replyId, RPCStub caller) 
 	{
 		if (caller != null)
 		{
 			CallInfo callInfo = new CallInfo();
 			callInfo.methodName = "reply_" + methodName;
 			callInfo.caller = caller;
 			m_ongoingCalls.put(replyId, callInfo);
 		}
 		
 		RPCMethodCall methodCall = new RPCMethodCall(methodName, params);
 		StringBuffer sb = methodCall.serialize();
 		super.sendReliableMessage(targetSender, Utility.stringToByteArray(sb.toString()));
 	}
 	
 	/**
 	 * onReliableMessageReceived()
 	 * Called when the next in-order message has been received.
 	 * Acknowledgement and ordering of messages is handled internally by the base class.
 	 */
 	@Override
 	protected void onReliableMessageReceived(int from, byte[] msg) 
 	{
 		StringBuffer sb = new StringBuffer(Utility.byteArrayToString(msg));
 		RPCMethodCall methodCall = new RPCMethodCall(sb);
 		onMethodCalled(from, methodCall.getMethodName(), methodCall.getParams());
 	}
 
 	/**
 	 * Classes that override this class should use this method to detect when methods are being called
 	 * @param methodName
 	 * @param params
 	 */
 	protected void onMethodCalled(int from, String methodName, Vector<String> params) 
 	{
 		MethodInfo mi = null;
 		InvokeResult result;
 		
 		if (methodName.startsWith("reply_"))
 		{
 			//
 			// This is the return value from a previous message call.
 			//
 			
 			Integer replyId = Integer.parseInt(params.get(0));
 			Integer retcode = Integer.parseInt(params.get(1));	
 			String content = params.get(2);
 			
 			if (m_ongoingCalls.containsKey(replyId))
 			{
 				CallInfo callInfo = m_ongoingCalls.get(replyId);
 				callInfo.caller.onMethodReply(from, replyId, retcode, content);
 				m_ongoingCalls.remove(replyId);
 			}
 			else
 			{
 				// Unexpected reply. Maybe the call timed out? Log for now.
 				error(String.format("Unexpected reply: id=%d, method=%s, content=[%s]", replyId, methodName, content));
 			}
 		}
 		else
 		{
 			//
 			// This is a remote method call to a registered impl on this node.
 			//
 			
 			if (m_methods.containsKey(methodName))
 			{
 				mi = m_methods.get(methodName);
 				result = mi.skeleton.invoke(methodName, params);
 			}
 			else
 			{
 				result = new InvokeResult();
 				result.error = ERROR_INVALID_METHOD;
 				result.replyId = Integer.parseInt(params.get(0));
 				result.content = null;
 			}
 			
 			if (result.content == null)
 				result.content = "<null>";
 			
 			Vector<String> replyArgs = new Vector<String>();
 			replyArgs.add(result.replyId.toString());
 			replyArgs.add(result.error.toString());
 			replyArgs.add(result.content.toString());
 	
 			callMethod(from, "reply_" + methodName, replyArgs, 0, null);
 		}
 	}
 
 	/**
 	 * onMessageTimeout()
 	 * 
 	 */
 	@Override
 	protected void onMessageTimeout(int endpoint, byte[] payload)
 	{
 		String content = Utility.byteArrayToString(payload);
 		StringBuffer sb = new StringBuffer(content);
 		RPCMethodCall methodCall = new RPCMethodCall(sb);
 		
 		if (methodCall.getMethodName().startsWith("reply_"))
 		{
 			// Timed out while sending a reply to a command.
 			//
 			// TODO: Need to add a timer to the caller, so that it
 			// doesn't get blocked waiting for a command's reply
 			// forever. 
 
 			// Ignore for now;
 		}
 		else
 		{
 			// Timed out sending a command to server.
 			// Fake a failed reply.
 			
 			Vector<String> replyArgs = new Vector<String>();
 			replyArgs.add(methodCall.getParams().get(0)); 
 			replyArgs.add(((Integer) FacebookException.CONNECTION_ABORTED).toString());
 			replyArgs.add("<null>");
 			
 			onMethodCalled(endpoint, "reply_" + methodCall.getMethodName(), replyArgs);
 		}
 	}
 	
 	
 	/**
 	 * Removes the current command from the queue and executes the next command, if there is one.
 	 */
 	protected void popCommandAndExecuteNext()
 	{
 		// Removes the head
 		_commandQueue.remove();
 		
 		// Gets the next element in the queue (new head) and executes it
 		String command = _commandQueue.peek();		
 		if (command != null)
 		{
 			executeClientCommand(command);
 		}
 	}
 	
 	protected void executeClientCommand(String command)
 	{
 	}
 	
 		
 	/**
 	 * bindRpcMethod()	
 	 */
 	public void bindRpcMethod(String methodName, RPCSkeleton skeleton)
 	{
 		assert !m_methods.containsKey(methodName);
 		
 		MethodInfo mi = new MethodInfo();
 		mi.skeleton = skeleton;
 		mi.methodName = methodName;
 				
 		m_methods.put(methodName, mi);
 	}
 
 	
 	/**
 	 * connectToFacebookServer()
 	 * @param address
 	 * @param callbacks
 	 * @return
 	 */
 	public IFacebookServer connectToFacebookServer(int address, IFacebookServerReply callbacks)
 	{
 		return new Stub_FacebookServer(this, address, callbacks);
 	}
 
	public IStorageServer connectToStorageServer(int address, IStorageServerReply callbacks)
	{
		return new Stub_StorageServer(this, address, callbacks);
	}
 		
 	public I2pcCoordinator connectTo2pcCoordinator(int address, I2pcCoordinatorReply callbacks)
 	{
 		return new Stub_2pcCoordinator(this, address, callbacks);
 	}
 
 	public I2pcParticipant connectTo2pcParticipant(int address, I2pcParticipantReply callbacks)
 	{
 		return new Stub_2pcParticipant(this, address, callbacks);
 	}
 
 	
 	/**
 	 * bindFacebookServerImpl
 	 * @param impl
 	 */
 	public void bindFacebookServerImpl(IFacebookServer impl)
 	{
 		Skel_FacebookServer skeleton = new Skel_FacebookServer(impl);
 		skeleton.BindMethods(this);
 	}
 	
	public void bindStorageServerImpl(IStorageServer impl)
	{
		Skel_StorageServer skeleton = new Skel_StorageServer(impl);
		skeleton.BindMethods(this);
	}
	
 	public void bind2pcCoordinatorImpl(I2pcCoordinator impl)
 	{
 		Skel_2pcCoordinator skeleton = new Skel_2pcCoordinator(impl);
 		skeleton.BindMethods(this);
 	}
 	
 	public void bind2pcParticipantImpl(I2pcParticipant impl)
 	{
 		Skel_2pcParticipant skeleton = new Skel_2pcParticipant(impl);
 		skeleton.BindMethods(this);
 	}
 }
 
