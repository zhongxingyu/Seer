 package nl.rug.peerbox.logic.messaging;
 
 import java.util.Collection;
 
 import nl.rug.peerbox.logic.Context;
 import nl.rug.peerbox.logic.filesystem.PeerboxFile;
 import nl.rug.peerbox.logic.filesystem.VirtualFileSystem;
 import nl.rug.peerbox.logic.messaging.Message.Command;
 import nl.rug.peerbox.logic.messaging.Message.Key;
 
 final class JoinMessageHandler extends MessageHandler {
 
 	//private static final Logger logger = Logger.getLogger(ListMessageHandler.class);
 
 	@SuppressWarnings("unchecked")
 	@Override
 	final void handle(Message message, Context ctx) {
 		VirtualFileSystem vfs = ctx.getVirtualFilesystem();
 		
 		Collection<PeerboxFile> messageFilelist = (Collection<PeerboxFile>) message
 				.get(Key.Files);
 		for (PeerboxFile file : messageFilelist) {
 			vfs.addFile(file);
 		}
 		
 		Collection<PeerboxFile> files = ctx.getVirtualFilesystem()
				.getLocalFileList();
 		Message reply = new Message();
 		reply.put(Key.Command, Command.List);
 		reply.put(Key.Files, files);
 		reply.put(Key.Peer, ctx.getLocalPeer());
 		ctx.getMulticastGroup().announce(reply.serialize());
 		
 	}
 }
