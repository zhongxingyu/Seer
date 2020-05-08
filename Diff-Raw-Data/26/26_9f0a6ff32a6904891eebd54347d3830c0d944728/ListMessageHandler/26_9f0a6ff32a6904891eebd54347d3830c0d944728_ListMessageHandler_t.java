 package nl.rug.peerbox.logic.handler;
 
 import java.io.File;
 
 import nl.rug.peerbox.logic.Context;
 import nl.rug.peerbox.logic.Message;
import nl.rug.peerbox.logic.Message.Command;
 import nl.rug.peerbox.logic.Message.Key;
 
 final class ListMessageHandler extends MessageHandler {
 
 
 	@Override
 	void handle(Message message, Context ctx) {
 
 		String[] files = new String[0];
 		File directory = new File(ctx.getPathToPeerbox());
 		if (directory.isDirectory()) {
 			files = directory.list();
 		}
 
 		Message reply = new Message();
		reply.put(Key.Command, Command.Reply.List);
 		reply.put(Key.Files, files);
 		reply.put(Key.IP, ctx.getIP());
 		reply.put(Key.Port, ctx.getPort());
 		ctx.getMulticastGroup().announce(reply.serialize());
 		
 		
 		
 	}
 
 }
