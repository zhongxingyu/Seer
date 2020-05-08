 /**
  * 
  */
 package it.infn.chaos.mds.rpc.server;
 
 import it.infn.chaos.mds.RPCConstants;
 
 import org.bson.BasicBSONDecoder;
 import org.bson.BasicBSONEncoder;
 import org.bson.BasicBSONObject;
 import org.msgpack.object.RawType;
 import org.msgpack.rpc.Request;
 import org.msgpack.rpc.Server;
 import org.ref.common.exception.RefException;
 
 /**
  * @author bisegni
  */
 public class MSGPackRPCServer extends RPCServer {
 	private BasicBSONDecoder		bDecoder			= new BasicBSONDecoder();
 	private ActionHandlerExecutor	handlerExecution	= null;
 	private Server					s					= new Server();
 
 	/**
 	 * 
 	 * @param threadNumber
 	 */
 	public MSGPackRPCServer(int threadNumber) {
 		handlerExecution = new ActionHandlerExecutor(threadNumber);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see it.infn.chaos.mds.rpc.RPCServer#init()
 	 */
 	@Override
 	public void init(int port) throws Throwable {
 		s = new Server();
 		s.listen(port);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see it.infn.chaos.mds.rpc.RPCServer#start()
 	 */
 	@Override
 	public void start() throws Throwable {
 		s.serve(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see it.infn.chaos.mds.rpc.RPCServer#deinit()
 	 */
 	@Override
 	public void deinit() throws Throwable {
 		s.close();
 		handlerExecution.stop();
 	}
 
 	/**
 	 * Chaos message
 	 * 
 	 * @param request
 	 * @param msg
 	 * @param a
 	 */
 	public void chaos_rpc(Request request, RawType messageData) {
 		BasicBSONObject bsonData = (BasicBSONObject) bDecoder.readObject(messageData.asByteArray());
 
 		String domain = bsonData.getString(RPCConstants.CS_CMDM_ACTION_DOMAIN);
 		String action = bsonData.getString(RPCConstants.CS_CMDM_ACTION_NAME);
 
 		RPCActionHadler actionHandler;
 		try {
 			actionHandler = getHandlerForDomainAndAction(domain, action);
 			if (actionHandler == null) {
				request.sendResult(getResponseRawType(1, "No action handler", "MSGPackRPCServer", 0));
 				return;
 			}
 			handlerExecution.submitAction(actionHandler, bsonData);
			request.sendResult(getResponseRawType(0, null, null, 0));
 		} catch (RefException e) {
			request.sendResult(getResponseRawType(1, e.getExceptionDomain(), e.getMessage(), e.getExceptionCode()));
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @param submissionResult
 	 * @param errorDomain
 	 * @param errorMsg
 	 * @param errCode
 	 * @return
 	 */
	private RawType getResponseRawType(int submissionResult, String errorDomain, String errorMsg, int errCode) {
 		BasicBSONObject bbObj = new BasicBSONObject();
 		BasicBSONEncoder enc = new BasicBSONEncoder();
 
		bbObj.append(RPCConstants.CS_CMDM_ACTION_SUBMISSION_RESULT, submissionResult);
 		// fill bson with error
 		RPCUtils.addRefExceptionElementToBson(bbObj, errorDomain, errorMsg, errCode);
 		return RawType.create(enc.encode(bbObj));
 	}
 }
