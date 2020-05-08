 package jig.ironLegends.router;
 
import jig.ironLegends.messages.Message;
 import jig.ironLegends.oxide.packets.ILPacket;
 
 public class SinglePlayerMsgTransport implements IMsgTransport {
 
 	MsgQueue m_txDest;
 	MsgQueue m_rxSource;
 	
 	public SinglePlayerMsgTransport(MsgQueue txDest, MsgQueue rxSource)
 	{
 		m_txDest = txDest;
 		m_rxSource = rxSource;
 	}
 	
 	@Override
 	public void send(ILPacket msg) {
 		m_txDest.add(msg);
 	}
 	@Override
 	public void send(MsgQueue messages) {
 		ILPacket msg = null;
 		msg = messages.poll();
 		while (msg != null)
 		{
 			send(msg);
 			msg = messages.poll();
 		}
 	}
 
 	@Override
 	public boolean hasRxMsg() {
 		if (m_rxSource.peek() != null)
 			return true;
 		return false;
 	}
 
 	@Override
 	public ILPacket nextRxMsg() {
 		return m_rxSource.poll();
 	}
 
 
 }
