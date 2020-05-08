 package distributedServices;
 
 import localServices.FileSystem;
 import localServices.Status;
 import localServices.StatusException;
 import localServices.Transaction;
 import Rio.Protocol;
 
 public class FileServer {
 	
 	private FileSystem fileSystem;
 	private RpcClient rpc;
 	FilePacket lastPacket;
 	ServiceNode node;
 	Transaction transaction;
 
 	public FileServer(ServiceNode node, Transaction transaction, RpcClient rpc, FileSystem fileSystem) {
 		this.node = node;
 		this.rpc = rpc;
 		this.fileSystem = fileSystem;
 	}
 	
 	protected void receiveCommand(int requestId, Integer from, byte[] msg) {
 		FilePacket packet = Helper.unpack(msg); 
 		
 		System.out.println(
 				String.format("**Received %s", String.valueOf(packet.action)));
 		
 		int transactionId = node.getTransactionForNode(from);
 		try {
 			
 			String aliasName = transaction.acquire(transactionId, packet.fileName);
 			
 			switch(packet.action) {
 				case CREATE:
 					fileSystem.create(aliasName);
 					break;
 				case DELETE:
 					fileSystem.delete(aliasName);
 					break;
 				case GET:
 					StringBuilder builder = fileSystem.get(aliasName);
 					packet.contents = builder.toString();
 					break;
 				case PUT:
 					fileSystem.put(aliasName, new String(packet.contents));
 					packet.contents = null;
 					break;
 				case APPEND:
 					fileSystem.append(aliasName, new String(packet.contents));
 					packet.contents = null;
 					break;
 				default:
 					throw new RuntimeException("Unknown IO command");
 			}
 			packet.status = Status.Success;
 		}
 		catch(StatusException e) {
 			packet.status = e.getStatus();
 		}
 		
 		rpc.sendResponse(
 				requestId, 
 				from, 
 				Protocol.IO_PKT, 
 				Helper.packToByteArray(packet));
 	}
 	
 }
