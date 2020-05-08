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
 		while(running) {
 			if(!parent.peerAccepted) {
 				receiveHandshake();
 			} else {
 				try {
 					readLine();
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	void receiveHandshake() {		
 		byte[] tcpArray = new byte[68];
 		try {
 			in.read(tcpArray);
 			System.out.println("<<< Data received, processing...");
 			System.out.println(">>> Listening from Peer : "+this.parent+"...");
 			parent.validateInfoHash(tcpArray);
 			System.out.println("-- HANDSHAKE VALIDATED !!! w/ peer "+parent+" -");
 			parent.showInterested();
 			parent.sendBitfield();
 			parent.unChoke();
 			try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {
 				// This just means we are off slightly on our timing.
 			} 
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 		}
 	}
 		
 	private void readLine() throws Exception {
 		byte[] lengthArray = new byte [4];
 		in.read(lengthArray, 0, 4);
 		ByteBuffer lengthBuffer = ByteBuffer.wrap(lengthArray);
		byte[] tcpArray = new byte[lengthBuffer.getInt()];
		in.read(tcpArray, 0, lengthBuffer.getInt());
 		ByteBuffer tcpInput = ByteBuffer.wrap(tcpArray);
		int length = tcpInput.getInt(0);
 		if (length == 0) {
 			parent.updateTimout();  // This is a keep alive
 			} else {
			tcpInput.position(5); // sets the iterator in the data index after reading length. and
			// message id.
 			switch (tcpArray[4]) {
 			case 0:	// choke
 				parent.setChoke(true);
 				break;
 			case 1: // remote-peer is unchoked, start requesting
 				parent.setChoke(false);
 				System.out.println(">>> Peer "+parent+" just unchoked me, start requesting pieces");
 				Bittorrent.getInstance().simpleDownloadAlgorithm();
 				break; // message was received and processed.
 			case 2:	// interested
 				parent.setInterested(true);
 				break;
 			case 3:	// not interested
 				parent.setInterested(false);
 				break;
 			case 4:	// have
 				parent.haveReceived(tcpInput.getInt());
 				break;
 			case 5:	// bitfield
 				byte[] bitfield = new byte[length - 1];
 				tcpInput.get(bitfield, 5, length - 1);
 				parent.receiveBitfield(bitfield);
 				break;
 			case 6:	// request
 				parent.requestReceived(tcpInput.getInt(),
 						tcpInput.getInt(),
 						tcpInput.getInt());
 				break;
 			case 7:	// piece
 				System.out.println("-- Piece received, analyzing...");
 				byte[] payload = new byte[length - 9];
 				tcpInput.position(13);
 				for(int i = 0; i < payload.length; ++i) {
 					payload[i] = tcpInput.get();
 				}
 				tcpInput.rewind();
 				// lineWrapper.get(payload, 9, length - 9); // this mothertrucker is giving problems.
 				parent.getPiece(tcpInput.getInt(5), tcpInput.getInt(9), payload);
 				break;
 			case 8:	// cancel
 				parent.cancelIndex(tcpInput.getInt(5),
 						tcpInput.getInt(9),
 						tcpInput.getInt(13));
 			default:
 				System.err.println("Invalid message was received: " + tcpInput);
 				break;
 			}
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
