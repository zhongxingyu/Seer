 package peers;
 
 import java.io.*;
 import java.util.*;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileLock;
 
 import utils.*;
 import utils.Message.*;
 
 public class DownloadThread implements Runnable {
 
 	public Peer peer = null;
 	public boolean interested = false;
 	public boolean choked = true;
 	public boolean peerInterested = false;
 	public boolean peerChoked = true;
 
 	public DownloadThread(Peer _peer) {
 		peer = _peer;
 	}
 
 	public void run() {
 		peer.createSocket(peer.ip_, peer.port_);
 		peer.establishStreams();
 		peer.sendHandshake(Manager.peer_id, Manager.info_hash);
 		if (!peer.receiveHandshake(Manager.info_hash)) {
 			System.out.println("Handshake Failed");
 			return;
 		} else {
 			peer.sendBitField();
 			Message m = peer.listen();
 			interpret(m);
 			Block b = null;
 			while (!checkFull()) {
 				do {
 					b = Manager.q.poll();
 				} while (b == null);
 				if (checkInterest(b)) {
 					try {
 						peer.sendInterested();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					while (peer.choked == true) {
 						m = peer.listen();
 						interpret(m);
 					}
 					RequestMessage rqm = new RequestMessage(b.getPiece(),
 							b.getBlockOffset(), b.getLength());
 					System.out.println("Requested " + peer.peer_id_ + " "
 							+ b.getPiece() + " " + b.getBlockOffset());
 					try {
 						Message.encode(peer.to_peer_, rqm);
 					} catch (Exception e) {
 						Manager.q.add(b);
 						e.printStackTrace();
 					}
 					m = peer.listen();
 					interpret(m);
 				}
 			}
			//
 			while (peerInterested) {
 				m = peer.listen();
 				interpret(m);
 			}
 
 		}
 	}
 
 	public boolean checkInterest(Block b) {
 		if (peer.bfb[b.getPiece()] == true) {
 			System.out.println("We are interested in their stuff");
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean interpret(Message m) {
 		switch (m.getId()) {
 		case Message.TYPE_BITFIELD:
 			BitfieldMessage bfm = (BitfieldMessage) m;
 			peer.bfb = BitToBoolean.convert(bfm.getData());
 			return true;
 		case Message.TYPE_CHOKE:
 			peer.choked = true;
 			System.out.println("We have been choked by " + peer.peer_id_);
 			return true;
 		case Message.TYPE_HAVE:
 			HaveMessage hvm = (HaveMessage) m;
 			peer.bfb[hvm.getPieceIndex()] = true;
 			return true;
 		case Message.TYPE_INTERESTED:
 			try {
 				Message.encode(peer.to_peer_, Message.UNCHOKE);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			peerChoked = false;
 			peerInterested = true;
 			return true;
 		case Message.TYPE_KEEP_ALIVE:
 			// do nothing would reset timer, should loop again;
 			return true;
 		case Message.TYPE_NOT_INTERESTED:
 			// do nothing, not keeping interested state atm
 			return true;
 		case Message.TYPE_PIECE:
 			PieceMessage pm = (PieceMessage) m;
 			byte[] piece_data = pm.getData();
 			Block b = new Block(pm.getPieceIndex(), pm.getBegin()
 					/ Manager.block_length, piece_data);
 			int p = b.getPiece();
 			b.setData(piece_data);
 
 			// make all single digits double, so that the sorting will work
 			// later on
 			String name = "";
 			if (b.getBlock() < 10) {
 				name = p + " 0" + b.getBlock();
 			} else {
 				name = p + " " + b.getBlock();
 			}
 			try {
 				RandomAccessFile file = new RandomAccessFile("temp/" + name,
 						"rw");
 				file.write(piece_data);
 				file.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
			Manager.addDownloaded(b.getLength());
 			File rename = new File("temp/" + name);
 			File f = new File("blocks/" + name);
 			if (f.exists()) {
 				break;
 			} else {
 				rename.renameTo(new File("blocks/" + name));
 			}
 
 			System.out.print(peer.peer_id_ + " ");
 			b.print();
 			return true;
 		case Message.TYPE_REQUEST:
 			if (!peerChoked) {
 				RequestMessage tempRequest = (RequestMessage) m;
 				byte[] sendData = new byte[tempRequest.getBlockLength()];
 				byte[] tempbytes = Helpers
 						.getPiece(tempRequest.getPieceIndex());
 				System.arraycopy(tempbytes, tempRequest.getBegin(), sendData,
 						0, tempRequest.getBlockLength());
 				System.out.println("Sending block " + tempRequest.getBegin());
 				System.out.println("of piece " + tempRequest.getPieceIndex());
 				PieceMessage toSend = new PieceMessage(
 						tempRequest.getPieceIndex(), tempRequest.getBegin(),
 						sendData);
 				Manager.addUploaded(tempRequest.getBlockLength());
 				try {
 					Message.encode(peer.to_peer_, toSend);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				return true;
 			}
 		case Message.TYPE_UNCHOKE:
 			peer.choked = false;
 			System.out.println("Peer " + peer.peer_id_ + " Unchoked us");
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean checkFull() {
 		for (int i = 0; i < Manager.have_piece.length(); i++) {
 			if (Manager.have_piece.get(i) != 1) {
 				return false;
 			}
 		}
 		return true;
 	}
 }
