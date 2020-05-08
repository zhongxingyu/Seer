 package peers;
 
 import java.io.*;
 import java.util.*;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileLock;
 
 import utils.*;
 import utils.Message.*;
 
 /**
  * @author matt
  * 
  */
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
 		// sleep to avoid calling over and over
 		try {
 			Thread.sleep(50);
 		} catch (InterruptedException e) {
 		}
 		peer.closeSocket();
 		peer.createSocket(peer.ip_, peer.port_);
 		peer.establishStreams();
 		peer.sendHandshake(Manager.peer_id, Manager.info_hash);
 		if (!peer.receiveHandshake(Manager.info_hash)) {
 			// System.out.println("Handshake Failed");
 			return;
 		} else {
 			peer.sendBitField();
 
 			// get their bitfield
 			Message m = null;
 			while (peer.bfb == null) {
 				try {
 					m = peer.listen();
 				} catch (Exception e) {
 					run();
 					return;
 				}
 				interpret(m);
 			}
 
 			Block b = null;
 
 			while (!checkFull()) {
 				b = Manager.q.poll();
 				if (b == null) {
 					// wait for the piece checker to fill it up again
 					try {
 						Thread.sleep(3000L + (long) (Math.random() * 10));
 					} catch (Exception e) {
 					}
 					continue;
 				}
 
 				String name = "";
 				if (b.getBlock() < 10) {
 					name = b.getPiece() + " 0" + b.getBlock();
 				} else {
 					name = b.getPiece() + " " + b.getBlock();
 				}
 
 				File f = new File("blocks/" + name);
 				if (f.exists()) {
 					continue;
 				}
 
 				if (peer.bfb[b.getPiece()]) {
 					try {
 						peer.sendInterested();
 					} catch (Exception e) {
 						Manager.q.add(b);
 						run();
 						return;
 					}
 					while (peer.choked == true) {
 						try {
 							m = peer.listen();
 						} catch (Exception e) {
 							Manager.q.add(b);
 							run();
 							return;
 						}
 						interpret(m);
 					}
 					try {
 						peer.requestBlock(b);
 						// //System.out.println("Requested (" + b.getPiece() +
 						// ", " + b.getBlock() + ") from " + peer.peer_id_);
 					} catch (Exception e) {
 						Manager.q.add(b);
 						run();
 						return;
 					}
 					try {
 						m = peer.listen();
 					} catch (Exception e) {
 						Manager.q.add(b);
 						run();
 						return;
 					}
 					interpret(m);
 				} else {
 					// they don't have what we want, but we should listen to
 					// them
 					// to see if they want anything we've got
 					try {
 						m = peer.listen();
 					} catch (Exception e) {
 						run();
 						return;
 					}
 					interpret(m);
 				}
 
 				// this is to avoid hammering any one peer
 				try {
 					Thread.sleep(20L + (long) (Math.random() * 10));
 				} catch (Exception e) {
 				}
 			}
 
 			while (peerInterested) {
 				try {
 					m = peer.listen();
 				} catch (Exception e) {
 					run();
 					return;
 				}
 				interpret(m);
 			}
 		}
 	}
 
 	/**
 	 * @param m
 	 *            message to be interpreted Will do what we should go based on
 	 *            the specific message
 	 */
 	public void interpret(Message m) {
 		// we have no message and we need to restart the connection
 		if (m == null) {
 			try {
 				Thread.sleep(500L + (long) (Math.random() * 10));
 			} catch (Exception e) {
 				// e.printStackTrace();
 			}
 			run();
 			return;
 		}
 
 		switch (m.getId()) {
 		case Message.TYPE_BITFIELD:
 			BitfieldMessage bfm = (BitfieldMessage) m;
 			peer.bfb = BitToBoolean.convert(bfm.getData());
 			// //System.out.println("Got a bitfield from " + peer.peer_id_);
 			return;
 		case Message.TYPE_CHOKE:
 			peer.choked = true;
 			// //System.out.println("We have been choked by " + peer.peer_id_);
 			return;
 		case Message.TYPE_HAVE:
 			HaveMessage hvm = (HaveMessage) m;
 			peer.bfb[hvm.getPieceIndex()] = true;
 			return;
 		case Message.TYPE_INTERESTED:
 			if (Manager.numUnchoked.get() < 3) {
 				System.out.println("WE INTO THE UNCHOKE PROCESS");
 				System.out.println();
 				System.out.println();
 				System.out.println();
 				System.out.println();
 				System.out.println();
 				Manager.unchokedPeers.add(this.peer);
 				Manager.numUnchoked.getAndIncrement();
 				peer.peerChoked = false;
 				peer.peerInterested = true;
 				try {
 					Message.encode(peer.to_peer_, Message.UNCHOKE);
 					System.out.println("SENT UNCHOKE MESSAGE");
 					System.out.println();
 					System.out.println();
 					System.out.println();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				return;
 			} else {
 				Manager.wantUnchokePeers.add(peer);
 				peer.peerInterested = true;
 				return;
 			}
 		case Message.TYPE_KEEP_ALIVE:
 			// do nothing would reset timer, should loop again;
 			return;
 		case Message.TYPE_NOT_INTERESTED:
 			// do nothing, not keeping interested state atm
 			peer.peerInterested = false;
 			return;
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
 				// e.printStackTrace();
 			}
 
 			File rename = new File("temp/" + name);
 			File f = new File("blocks/" + name);
 			if (f.exists()) {
 				break;
 			} else {
 				rename.renameTo(new File("blocks/" + name));
 			}
 
 			peer.downloaded.set(peer.downloaded.get() + piece_data.length);
 			// //System.out.print(peer.peer_id_ + " ");
 			// b.print();
 			return;
 		case Message.TYPE_REQUEST:
 			if (!peer.peerChoked) {
 				RequestMessage tempRequest = (RequestMessage) m;
 				byte[] sendData = new byte[tempRequest.getBlockLength()];
 				byte[] tempbytes = Helpers
 						.getPiece(tempRequest.getPieceIndex());
 				System.arraycopy(tempbytes, tempRequest.getBegin(), sendData,
 						0, tempRequest.getBlockLength());
 
 				// System.out.println(peer.peer_id_ + " sending block " +
 				// tempRequest.getBegin());
 				// //System.out.println("of piece " +
 				// tempRequest.getPieceIndex());
 				PieceMessage toSend = new PieceMessage(
 						tempRequest.getPieceIndex(), tempRequest.getBegin(),
 						sendData);
 				try {
 					Message.encode(peer.to_peer_, toSend);
 					Manager.addUploaded(tempRequest.getBlockLength());
 					peer.uploaded.set(peer.uploaded.get() + sendData.length);
 				} catch (Exception e) {
 					// e.printStackTrace();
 				}
 				return;
 			}
 		case Message.TYPE_UNCHOKE:
 			peer.choked = false;
 			// //System.out.println("Peer " + peer.peer_id_ + " unchoked us");
 			return;
 		}
 		return;
 	}
 
 	/**
 	 * @return true if we have all the pieces false otherwise
 	 */
 	public boolean checkFull() {
 		for (int i = 0; i < Manager.have_piece.length(); i++) {
 			if (Manager.have_piece.get(i) != 1) {
 				return false;
 			}
 		}
 		return true;
 	}
 }
