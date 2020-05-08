 package org.ssor.protocol.tolerance;
 
 import java.util.Queue;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.ssor.Region;
 import org.ssor.protocol.AbstractProtocolManager;
 import org.ssor.protocol.Command;
 import org.ssor.protocol.Header;
 import org.ssor.protocol.Message;
 import org.ssor.protocol.Token;
 import org.ssor.util.Triple;
 import org.ssor.util.Tuple;
 
 public class FTManager extends AbstractProtocolManager {
 
 	private static final Logger logger = LoggerFactory
 	.getLogger(FTManager.class);
 	
 	public boolean handleReceive(Message message, Object address, int addressUUID){
 		Header header = message.getHeader();
 		if (header instanceof org.ssor.protocol.tolerance.AgreementHeader) {
 			
 			// If this is the subsequence join node, discard this
 			if(group.isConsensus()){
 				//viewSynchronizationManager.releaseOnRetransmission(util.getUUIDFromAddress(address), null);
 				return true;
 			}
 			
 			doAgreement(message);
 			return true;
 		} else if (header instanceof ConsensusHeader) {
 			
 			collect(message);
 			return true;
 		}
 		
 		return false;
 	}
 
 	public Object releaseCachedSequences(Message message){
 		
 		if(logger.isTraceEnabled()){
 			logger.trace("Remove cached sequences");
 		}
 		
 		return protocolStack.down(Command.FT_RELEASE_SEQUENCE, new Token(message));
 	
 	}
 	
     public Object onCrashFailure(int uuid, Queue<Integer> view){
 		
 		if(logger.isTraceEnabled()){
 			logger.trace("Node " + uuid + " is removed from the new view");
 		}
 		
 		return protocolStack.up(Command.INSTALL_VIEW_ON_LEAVING, new Token(new Tuple<Integer, Queue<Integer>>(uuid, view)));
 		
 	}
     
   
     public Object collect(Message message){
     	return protocolStack.up(Command.FT_COLLECT, new Token(message));
     }
     
     @SuppressWarnings("unchecked")
 	public Object doAgreement(Message message){
     	System.out.print("************* agreement \n");
     	Token token  =  protocolStack.up(Command.FT_AGREEMENT, new Token(message));
     	if(Token.FT_AGREEMENT_RETRANSMISSION == token.getNextAction()){
     		
     	 	
         	if(logger.isTraceEnabled()){
     			logger.trace("Receive consensus agreement and retransmission needed");
     		}
     		
         	((Triple<Set<Region>, Object, Integer>)token.getDataForNextProtocol()).setVal2(message.getSrc());
     		return protocolStack.down(Command.FT_RETRANSMIT, new Token(token.getDataForNextProtocol()));
     		
     		
         }
     	
     	if(logger.isTraceEnabled()){
 			logger.trace("Receive consensus agreement and no retransmission needed");
 		}
     	
     	return token;
     }
     
 	
 	
 	
 	
 
 }
