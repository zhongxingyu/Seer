 package utils;
 
 import java.io.*;
 import java.util.Collections;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.TimerTask;
 import java.util.TreeSet;
 import java.util.StringTokenizer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileLock;
 import utils.Message.HaveMessage;
 import utils.StringComparator;
 import utils.Gooey;
 
 import peers.Block;
 import peers.Peer;
 
 public class PieceChecker extends TimerTask {
 
 	public void run() {
 		if (Manager.fileDone) {
 			this.cancel();
 		}
 
 		for (int i = 0; i < Manager.numPieces; i++) {
 			// is it already verified?
 			if (Manager.have_piece.get(i) == 1) {
 				continue;
 			}
 
 			// do we have the piece yet?
 			byte[] piece = Helpers.getPiece(i);
 			if (piece == null) {
 				continue;
 			}
 
 			byte[] pieceHash = Manager.torrent_info.piece_hashes[i].array();
 			if (Helpers.verifyHash(piece, pieceHash)) {
 				// //System.out.println("Piece " + i + " verified");
 				Manager.have_piece.set(i, 1);
 				Manager.addDownloaded(Helpers.getPiece(i).length);
 
 				// don't send the have message if this is a resumed download
         for (Peer peer : Manager.activePeerList) {
          if (peer.ready) {
            HaveMessage haveSend = new HaveMessage(i);
            try {
              Message.encode(peer.to_peer_, haveSend);
            } catch (Exception e) {
               // //e.printStackTrace();
               // //System.out.println("Failed to send the have message to " + peer.peer_id_);
            }
          }
         }
 			} else {
 				//System.out.println("Deleting piece " + i);
 				Manager.have_piece.set(i, 0);
 				Manager.addDownloaded(-1 * Helpers.getPiece(i).length);
 				Helpers.deletePiece(i);
 			}
 		}
 
 		finish();
 		return;
 	}
 		
 
 	// adds missing blocks to queue if needed
 	public static void addMissingBlocks() {
 		for (int total = 0, j = 0; j < Manager.numPieces; j++) {
 			for (int k = 0; k < Manager.blocksPerPiece; k++, total++) {
 
 				String name = "";
 				if (k < 10) {
 					name = j + " 0" + k;
 				} else {
 					name = j + " " + k;
 				}
 
 				File f = new File("blocks/" + name);
 				if (f.exists() || total > Manager.numBlocks) {
 					continue;
 				} else {
 					byte[] data = null;
 					if (j == Manager.numPieces - 1
 							&& total == Manager.numBlocks) {
 						data = new byte[Manager.leftoverBytes];
 						Block b = new Block(j, k, data);
 						Manager.q.add(b);
             // System.out.println("Adding block " + j + " " + k);
 						break;
 					} else {
 						data = new byte[Manager.block_length];
 					}
 					Block b = new Block(j, k, data);
           // System.out.println("Adding block " + j + " " + k);
 					Manager.q.add(b);
 				}
 			}
 		}
 
 		// //System.out.println(Manager.q.size());
 	}
 
 	public void finish() {
	  Gooey.updateGui();
	  
 		File f = new File(Manager.file.getName());
 		if (f.exists()) {
       System.out.println("File already exists.");
 			Manager.fileDone = true;
 			return;
 		}
 
 
 		if (Manager.have_piece.toString().indexOf("0") != -1) {
 			if (Manager.q.size() == 0) {
 				addMissingBlocks();
 			}
 			return;
 		}
 		
 		System.out.println("\nCommencing file write...");
     
 		byte[] piece = null;
 		byte[] block = null;
 
 		FileOutputStream out = null;
 		try {
 			out = new FileOutputStream(Manager.file);
 		} catch (Exception e) {
 			//System.out.print(e);
 		}
 
 		// get rid of the spaces so we can make numbers out of the names
 		File dir = new File("blocks");
 		ArrayList<String> names = new ArrayList<String>();
 		for (File file : dir.listFiles()) {
 			names.add(file.getName());
 		}
 
 		// sort the bastards
 		StringComparator comparator = new StringComparator();
 		Collections.sort(names, comparator);
 
 		// write 'em in the correct order
 		for (String name : names) {
 			File file = new File("blocks/" + name);
 			try {
 				byte[] fileBytes = Helpers.getBytesFromFile(file);
 				out.write(fileBytes);
 			} catch (Exception e) {
 				//System.out.print(e);
 			}
 		}
 
 		try {
 			out.close();
 		} catch (Exception e) {
 			//System.out.print(e);
 		}
 
 		// tell the tracker we're done
 		if (!Manager.fileDone) {
 			byte[] response = null;
 			Thread t = new Thread(new TrackerContact(1));
 			t.start();
 			Manager.fileDone = true;
 		}
 		
     System.out.println("I'll seed until you tell me to stop.");
 	}
 
 	public static void deleteBlocks(int i) {
 
 	}
 }
