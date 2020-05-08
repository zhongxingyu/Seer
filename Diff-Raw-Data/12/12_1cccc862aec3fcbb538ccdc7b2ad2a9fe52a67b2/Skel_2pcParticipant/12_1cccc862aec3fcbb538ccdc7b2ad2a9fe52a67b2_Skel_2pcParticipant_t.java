 package node.rpc;
 import java.util.Vector;
 
 public class Skel_2pcParticipant extends RPCSkeleton  
 {
 	private I2pcParticipant m_impl;
 	
 	/**
 	 * Skel_2pcParticipant()
 	 * @param impl
 	 */
 	public Skel_2pcParticipant(I2pcParticipant impl)
 	{
 		m_impl = impl;
 	}
 		
 	/**
 	 * invokeInternal()
 	 */
 	@Override
 	protected String invokeInternal(String methodName, Vector<String> methodArgs) throws RPCException
 	{
 		String content = null;
	
 		
 		switch (methodName)
 		{
 		case "startTransaction":
 			content = m_impl.startTransaction(methodArgs.get(0), methodArgs.get(1));
 			break;
 			
 		case "requestPrepare":
 			content = m_impl.requestPrepare(methodArgs.get(0), methodArgs.get(1));
 			break;
 			
 		case "commitTransaction":
 			content = m_impl.commitTransaction(methodArgs.get(0), methodArgs.get(1));
 			break;
 			
 		case "abortTransaction":
 			content = m_impl.abortTransaction(methodArgs.get(0), methodArgs.get(1));
 			break;
 			
 		default:
 			assert false;
 			break;
 		}
 		
 		return content;
 	}
 	
 	@Override
 	protected void BindMethods(RPCNode node)
 	{
 		node.bindRpcMethod("startTransaction", this);
 		node.bindRpcMethod("requestPrepare", this);
 		node.bindRpcMethod("commitTransaction", this);
 		node.bindRpcMethod("abortTransaction", this);
 	}
 }
 
