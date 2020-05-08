 package bt.Model;
 
 import java.io.IOException;
 import java.util.List;
 
 import bt.View.ClientGUI;
 
 /**
  * It will be in charge of spooling, choking and unchoking peers every N amount of time.
  * @author Isaac Yochelson, Robert Schomburg and Fernando Geraci.
  *
  */
 public class PeerSpooler implements Runnable {
 
 	Bittorrent bt;
 	private boolean running = true;
 	private long sleep;
 	private int limit;
 	
 	
 	/**
 	 * Constructs a PeerSpooler object
 	 * @param bt
 	 * @param sleep
 	 */
 	public PeerSpooler(Bittorrent bt, long sleep, int peersLimit) {
 		this.limit = peersLimit;
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
 	 * If there are limit or more connections available, it will rank the peers by their download rate,
 	 * leave the first limit-1 unchoked, choke the 4th and then pick a random one for unchoking. 
 	 * @throws IOException 
 	 */
 	
 	void execute() {
 		List<Peer>  peers = bt.getPeerList();
 		Peer[] rankedList = this.getRankedList(peers);
 		try {
 			Thread.sleep(100);
 			
 			// choke everything from limit-2 on.
 			boolean[] toChoke = new boolean[rankedList.length];
 			for (int i = 0; i < toChoke.length; ++i) {
 				toChoke[i] = !(i < limit - 1);
				// peers.get(i).resetDownloaded();
 			}
 			int lucky = (int)(((Math.random() * 100000.0)% toChoke.length - limit + 1) + limit - 1);
 			toChoke[lucky] = false;
 			for (int i = 0; i < toChoke.length; ++i) {
 				if (toChoke[i]) {
 					if (!rankedList[i].isChoked()) {
 						rankedList[i].choke();
 					}
 				} else {
 					rankedList[i].unChoke();
 				}
 				ClientGUI.getInstance().updatePeerInTable(peers.get(i), ClientGUI.STATUS_UPDATE);
 				ClientGUI.getInstance().updatePeerInTable(peers.get(i), ClientGUI.DOWNLOADRATE_UPDATE);
 			}
 			/*
 			for(int i = limit-2; i < peers.size(); i++) {
 				peers.get(i).setChoke(true);
 				peers.get(i).resetDownloaded();
 				ClientGUI.getInstance().updatePeerInTable(peers.get(i), ClientGUI.STATUS_UPDATE);
 				ClientGUI.getInstance().updatePeerInTable(peers.get(i), ClientGUI.DOWNLOADRATE_UPDATE);
 			}
 			Peer[] rest = new Peer[rankedList.length-(this.limit-2)];
 			for(int i = 0; i < rest.length; ++i) {
 				rest[i] = rankedList[(limit-2)+i];
 			}
 			Peer p = this.getRandomPeer(rest);
 			Thread.sleep(400);
 			p.setChoke(false);
 			ClientGUI.getInstance().updatePeerInTable(p, ClientGUI.STATUS_UPDATE);
 			*/
 			
 		} catch (Exception e) {
 			ClientGUI.getInstance().publishEvent("ERROR: Peer "+rankedList[3]+" could not be choked successfully");
 		}
 	}
 	
 	/**
 	 * Randomly selects a peer from a provided list.
 	 * @param rest
 	 * @return Peer
 	 */
 	private Peer getRandomPeer(Peer[] rest) {
 		int peers = rest.length;
 		int random = (int)Math.random()*10000;
 		random = random % peers;
 		Peer p = rest[random];
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
