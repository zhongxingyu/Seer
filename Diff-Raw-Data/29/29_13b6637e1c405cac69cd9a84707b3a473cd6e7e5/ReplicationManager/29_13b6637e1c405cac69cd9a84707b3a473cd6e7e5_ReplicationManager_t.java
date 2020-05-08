 package org.ssor.protocol.replication;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.ssor.AtomicService;
 import org.ssor.ServiceStackContext;
 import org.ssor.invocation.InvocationUnit;
 import org.ssor.protocol.AbstractProtocolManager;
 import org.ssor.protocol.Command;
 import org.ssor.protocol.Header;
 import org.ssor.protocol.Message;
 import org.ssor.protocol.Token;
 import org.ssor.protocol.replication.abcast.DeliveryPacket;
 import org.ssor.util.Callback;
 
 public class ReplicationManager extends AbstractProtocolManager {
 
 	private static final Logger logger = LoggerFactory
 	.getLogger(ReplicationManager.class);
 	
 	public boolean handleReceive(Message message, Object address, int addressUUID){
 		
 		Header header = message.getHeader();
 		if (header instanceof RequestHeader) {
 
 			final RequestHeader requestheader = (RequestHeader) header;
 			// Set requester into header so the isRequester function could
 			// work
 			if (requestheader.getRequester() == null)
 				requestheader
 						.setRequester(addressUUID);
 			// The receive of first U/B
 			if ((!requestheader.isNonOrdered()) && requestheader.getOuter() == null)
 				coordinate(message, address);
 			// The receive of second B or
 			// Non-ordered message
 			else
 				execute(message);
 			
 			return true;
 
 		} else if (header instanceof ResponseHeader) {
 
 			((ResponseHeader)message.getHeader()).setSequencer(addressUUID);
 			// The receive of U
 			//System.out.print("AS final start ****************\n");
 			acquireSequence(message);
 			//System.out.print("AS final finish ****************\n");
 			return true;
 		}
 		
 		return false;
 	}
 
 	/**
 	 * Run the protocol that analysis the invoked service, and this would trigger the protocol chain
 	 * @param unit
 	 * @return
 	 */
 	public Object request(InvocationUnit unit){			
 		return protocolStack.down(Command.ABCAST_FIRST_UNICAST, new Token(unit)).getDataForNextProtocol();				
 	}
 	
 	public Object coordinate(Message message, Object address){
 		
 		//suspendProcess(logger);
 
 
 		// Due to it is guarantee that no request for sequence was sent before the consensus of new sequencer compelte
 		// therefore there is no need to add suspending here
 		Token token = protocolStack.up(Command.ABCAST_COORDINATE, new Token(message));
 		
 		// If this is not sequencer, then timestamp should be null
 		if(Token.REPLICATION_COORDINATE_NOT_SEQUENCER == token.getNextAction()){
 			
 			if(logger.isTraceEnabled()){
 				logger.trace("Message received for coordination but the current node dose not a responsible sequencer, ReqId: "  + message.getReqId());
 			}
 			
 			return null;
 		}
 		
 		if(logger.isTraceEnabled()){
 			logger.trace("Message received for coordination, ReqId: "  + message.getReqId());
 		}
 	
 		token.reset();
 		return protocolStack.down(Command.ABCAST_SECOND_UNICAST, new Token(new ReplicationUnit(message.getReqId(), token.getDataForNextProtocol(), address)));
 		
 	}
 	
 	public Object acquireSequence(Message message){
 		
 		Token token = protocolStack.up(Command.ABCAST_ACQUIRE_SEQUENCE, new Token(message));
 		//For discarded message
 		if(Token.REPLICATION_ACQUIRE_SEQUENCE_DISCARDED == token.getNextAction())
 			return null;
 		// If the order timestamp is not yet can be broadcast, then timestamp should be boolean
 		// with true indicate can trigger execution first, if it is null means can trigger broadcast
 		
 		// Broadcast the timestamp when no nested and idempotent services
 		// order timstamp has been attached in ordering protocol
 		if(Token.REPLICATION_ACQUIRE_BORADCAST == token.getNextAction()){
 			
 			if(logger.isTraceEnabled()){
 				logger.trace("Message received from coordination, it is now executing the 2nd broadcasting, ReqId: "  + message.getReqId());
 			}
 			
 			return protocolStack.down(Command.ABCAST_FINAL_BROADCAST, new Token(new ReplicationUnit(message.getReqId(), 
 					message.getHeader())));
 		}
 			
 		if(Token.REPLICATION_ACQUIRE_SEQUENCE_NOT_COLLECTED == token.getNextAction()){
 			
 			if(logger.isTraceEnabled()){
 				logger.trace("Message received from coordination, but sequence timestamp has not be collected completely, ReqId: "  + message.getReqId());
 			}
 			
 			return null;
 		} else  {
 			
 			if(logger.isTraceEnabled()){
 				logger.trace("Message received from coordination and all timestamp has been collected, it is now executing first and delay the 2nd broadcasting, ReqId: "  + message.getReqId());
 			}
 
 			return execute(message);
 		}
 		
 		
 		
 	}
 	
 	/**
 	 * For nested service and idempotent services
 	 * @param message
 	 * @return
 	 */
 	public Object doDelayBroadcast(Message message){
 		
 		if(logger.isTraceEnabled()){
 			logger.trace("Perform the differed 2nd broadcasting for ReqId: "  + message.getReqId());
 		}
 			
 		return protocolStack.down(Command.ABCAST_DELAY_FINAL_BROADCAST, new Token(message));
 		
 	}
 	
 	public Object execute(final Message message){
 		
 		if(!suspendProcess(message, logger, new Callback(){
 
 			@Override
 			public Object run() {
 				return execute(message);
 			}
 			
 		}))
 			return null;
 		
 		// This may executed on requester site as well, for triggering the thread
 		if(logger.isTraceEnabled()){
 			logger.trace("Perform the execution for ReqId: " + message.getReqId());
 		}
 	
 		return protocolStack.up(Command.ABCAST_AGREEMENT, new Token(message));
 		
 	}
 	// Run the requester's original thread
 	public Object executeInAdvance(Message message){
 		
 		if(logger.isTraceEnabled()){
 			logger.trace("Perform the execution for ReqId: " + message.getReqId() + " on requester site");
 		}
 		message.setIsExecuteInAdvance(true);
 		return ((DeliveryPacket)protocolStack.up(Command.ABCAST_AGREEMENT, new Token(message)).getDataForNextProtocol()).getResult();
 		
 	}
 	
 	public Object finish(Object[] objects){
 		
 		if(logger.isTraceEnabled()){
 			logger.trace("Service: "
 					+ ((AtomicService) objects[0]).getName() + " execution finished");
 		}
 		
 
 		// Return if all the nested service has been analysed
 		return ServiceStackContext.popService((AtomicService) objects[0],
 				objects[1]);
 	}
 	
 	public boolean isNeedNewThread(Message message){
 		return true;//(Boolean)protocolStack.down(Command.REPLICATION_RELEASE_CACHED_SEQUENCE, message);
 	}
 
 }
