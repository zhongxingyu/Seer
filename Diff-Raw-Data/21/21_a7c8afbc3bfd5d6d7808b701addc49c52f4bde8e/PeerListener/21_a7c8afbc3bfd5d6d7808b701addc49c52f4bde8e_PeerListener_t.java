 package bt.Model;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 
 import bt.Utils.Utilities;
 
 /**
  * 
  * The main listener thread for each peer this client is connected to.
  * @author Ike, Robert and Fernando
  *
  */
 
 class PeerListener implements Runnable{
 	
 	private InputStream in = null;
 	private Peer parent = null;
 	private boolean running = true;
 	
 	PeerListener (Peer parent, InputStream inStream) {
 		this.in = inStream;
 		this.parent = parent;
 	}
 
 	@Override
 	public void run() {
 		
 		// Remember, this won't actually work because there is no guarantee that nextLine is a
 		// single incoming message.  The TCP buffer could contain more than one message, and if
 		// it does, we need to handle all of them and not just the first.
 		
 		
 		while(running) { 
 			try {
 				System.out.println(">>> Listening from Peer : "+this.parent);
 				// by placing the array here, the buffer get cleared every run.
 				byte[] tcpArray = new byte[74];
 				in.read(tcpArray);
 				if(!parent.peerAccepted) {
 					parent.validateInfoHash(tcpArray);
 					System.out.println(">>> HANDSHAKE VALIDATED !!! w/ peer "+parent+" -");
 					parent.showInterested();
 					parent.sendBitfield();
 					parent.unChoke();
 					Thread.sleep(2000); // give it sometime to get digested.
 				} else {
 					int offset = 0;
 					ByteBuffer tcpInput = ByteBuffer.wrap(tcpArray);
 					ByteBuffer lineWrapper = null;
 					mainLoop: while (offset < tcpArray.length) {
 						// Create a big enough buffer to read the correct TCP message.
 						int length = tcpInput.getInt(offset); // returns the length of the peer message
 						// obviates a -8 byte given as first byte response from the peer.
 						if(length < 0 ) { // SUPER WORK AROUND, but its working.
 							offset++;
 							length = tcpInput.getInt(offset);
 						}
 						offset = offset + 4; // 4 bytes were read
 						byte[] currentLine = new byte[length]; // create a byte array of the correct length
 						for(int i = 0; i < currentLine.length; i++) {
 							currentLine[i] = tcpInput.get(offset);
 							++offset;
 						}
 						//tcpInput.get(currentLine, offset+3, length);
 						switch (currentLine[0]) {
 						case 0:	// choke
 							parent.setChoke(true);
 							break;
 						case 1: // remote-peer is unchoked, start requesting
 							parent.setChoke(false);
 							System.out.println(">>> Peer "+parent+" just unchoked me, start requesting pieces");
							Bittorrent.getInstance().simpleDownloadAlgorithm();
 							break mainLoop; // message was received and processed.
 						case 2:	// interested
 							parent.setInterested(true);
 							break;
 						case 3:	// not interested
 							parent.setInterested(false);
 							break;
 						case 4:	// have
 							lineWrapper = ByteBuffer.wrap(currentLine);
 							parent.haveReceived(lineWrapper.getInt(1));
 							break;
 						case 5:	// bitfield
 							lineWrapper = ByteBuffer.wrap(currentLine);
 							byte[] bitfield = new byte[length - 1];
 							lineWrapper.get(bitfield, 1, length - 1);
 							parent.receiveBitfield(bitfield);
 							break;
 						case 6:	// request
 							lineWrapper = ByteBuffer.wrap(currentLine);
 							parent.requestReceived(lineWrapper.getInt(1),
 									lineWrapper.getInt(5),
 									lineWrapper.getInt(9));
 							break;
 						case 7:	// piece
 							lineWrapper = ByteBuffer.wrap(currentLine);
 							byte[] payload = new byte[length - 9];
 							lineWrapper.get(payload, 9, length - 9);
 							parent.getPiece(lineWrapper.getInt(1), lineWrapper.getInt(5), payload);
 							break;
 						case 8:	// cancel
 							lineWrapper = ByteBuffer.wrap(currentLine);
 							parent.cancelIndex(lineWrapper.getInt(1),
 									lineWrapper.getInt(5),
 									lineWrapper.getInt(9));
 						default:
 							System.err.println("Invalid message was received: " + currentLine);
 							break;
 						}
 					}
 				}
 			} catch (Exception e) {
 
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				Utilities.callClose();
 			}
 /*	THIS BLOCK LOOKS REDUNDANT!!
   			switch (nextLine[4]) {
 //			switch (currentLine[4]) {
 			case 0:
 				parent.setChoke(true);
 				break;
 			case 1: // remote-peer is unchoked, start requesting
 				System.out.println(">>> Peer "+parent+" just unchoked me, start requesting pieces");
 				break;
 			default:
 				break;
 
 //				System.err.println(e.getMessage());
 			}	*/
 		}
 	}
 	
 	/**
 	 * This method is called by the parent of this object as the first action of its own dispose method to
 	 * remove references to itself which might prevent garbage collection.
 	 */
 	void dispose () {
 		running = false;
 		in = null;
 		parent = null;
 	}
 	
 }
