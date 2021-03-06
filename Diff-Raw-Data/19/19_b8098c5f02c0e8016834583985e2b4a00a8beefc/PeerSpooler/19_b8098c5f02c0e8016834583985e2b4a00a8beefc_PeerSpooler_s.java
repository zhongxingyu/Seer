 package bt.Model;
 
 import java.io.IOException;
 import java.util.List;
 
 import bt.View.ClientGUI;
 
 public class PeerSpooler implements Runnable {
 
 	Bittorrent bt;
 	private boolean running = true;
 	private long sleep;
 	
 	public PeerSpooler(Bittorrent bt, long sleep) {
 		this.bt = bt;
 		this.sleep = sleep;
 		Thread spooler = new Thread(this);
 		spooler.start();
 	}
 
 	@Override
 	public void run() {
 		while(running) {
 			try {
 				Thread.sleep((sleep));
 				// spool peers
 				// measure times
 				// keep 3
 				// choke 1
 				// unchoke random peer
 				this.execute();
 			} catch (Exception e) { }
 		}
 	}
 	
 	/**
 	 * If there are 4 or more connections available, it will rank the peers by their download rate,
 	 * leave the first 3 unchoked, choke the 4th and then pick a random one for unchoking. 
 	 * @throws IOException 
 	 */
 	private void execute() {
 		List<Peer> peers = bt.getPeerList();
 		int unchocked = 0;
 		for(Peer p:peers) {
 			if(!p.isChoked()) {
 				unchocked++;
 			}
 		}
 		if(unchocked > 4) {
 			// get ranked list
 			Peer[] rankedList = this.getRankedList(peers);
 			int size = rankedList.length;
 			try {
 				rankedList[3].choke();
 				Thread.sleep(100);
 				Peer[] rest = new Peer[rankedList.length-4];
 				int index = 0;
 				for(int i = 4; i < rankedList.length; ++i) {
					rankedList[i].choke();
 					rest[index] = rankedList[i];
 					index++;
 				}
 				Peer p = this.getRandomPeer(rest);
 				Thread.sleep(400);
				p.unChoke();
 			} catch (Exception e) {
 				ClientGUI.getInstance().publishEvent("ERROR: Peer "+rankedList[3]+" could not be choked successfully");
 			}
 		} else {
 			ClientGUI.getInstance().publishEvent(" -- Lees than 4 peers unchoked -- ");
 		}
 	}
 	
 	/**
 	 * Randomly selects a peer from a provided list.
 	 * @param rest
 	 * @return Peer
 	 */
 	private Peer getRandomPeer(Peer[] rest) {
 		int peers = rest.length;
 		int randomPeer = (int)((Math.random()*peers));
 		Peer p = rest[randomPeer];
 		return p;
 	}
 	
 	/**
 	 * Returns a ranked array by download rate of peers.
 	 * Sorry about the insertion sort, but for this size, does it make any sense to go any bigger?
 	 * @param peers
 	 * @return
 	 */
 	private Peer[] getRankedList(List<Peer> peers) {
 		Peer[] rankedList = new Peer[peers.size()];
 		// same as calling to array
 		for(int i = 0; i < peers.size(); i++) {
 			rankedList[i] = peers.get(i);
 		}
 		for(int i = 1; i < peers.size(); i++) {
 			Peer currPeer = rankedList[i];
 			for(int u = i-1; u >= 0; --u) {
 				if(currPeer.getDownloadRate() > rankedList[u].getDownloadRate()) {
 					Peer tmp = rankedList[u];
 					rankedList[u] = currPeer;
 					rankedList[u+1] = tmp;
 				} else break;
 			}
 		}
 		return rankedList;
 	}
 
 }
