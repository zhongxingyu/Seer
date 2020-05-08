 package pro.trousev.cleer.sys;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import pro.trousev.cleer.Messaging.Event;
 import pro.trousev.cleer.Player;
 import pro.trousev.cleer.Messaging.Message;
 import pro.trousev.cleer.Player.PlayerChangeEvent;
 import pro.trousev.cleer.Player.PlayerException;
 import pro.trousev.cleer.Messaging;
 import pro.trousev.cleer.Player.Reason;
 import pro.trousev.cleer.Queue;
 import pro.trousev.cleer.Item;
 import pro.trousev.cleer.Player.Status;
 
 public class QueueImpl implements Queue {
 
 	private Player player;
 	private List<Item> queue = null;
 	private int current = 0;
 
 	public QueueImpl(Player player) {
 		this.player = player;
 		queue = new ArrayList<Item>();
 		Messaging.subscribe(Player.PlayerChangeEvent.class, new Event() {
 
 			@Override
 			public void messageReceived(Message message) {
 				PlayerChangeEvent ev = (PlayerChangeEvent) message;
 				if (ev.status == Status.Stopped
 						&& ev.reason == Reason.EndOfTrack && ev.error == null) {
 					next();
 				}
 			}
 		});
 	}
 
 	@Override
 	public List<Item> queue() {
 		return queue;
 	}
 
 	@Override
 	public int size() {
 		return queue.size();
 	}
 
 	@Override
 	public int playing_index() {
 		return current;
 	}
 
 	@Override
 	public Item playing_track() {
 		try {
 			return queue.get(current);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	@Override
 	public void clear() {
 		player.stop(Player.Reason.UserBreak);
 		player.close();
 		queue.clear();
 		current = 0;
 	}
 
 	@Override
 	public void enqueue(List<Item> tracks, EnqueueMode mode) {
 		if (mode == EnqueueMode.AfterAll) {
 			queue.addAll(tracks);
 		}
 		if (mode == EnqueueMode.AfterCurrent) {
 			int s = current + 1;
 			if (s >= size())
 				s = size();
 			queue.addAll(s, tracks);
 		}
 		if (mode == EnqueueMode.Immidiaely) {
 			if (size() != 0)
 				queue.addAll(current, tracks);
 			else
 				queue.addAll(tracks);
 			player.stop(Player.Reason.UserBreak);
 			try {
 				player.open(playing_track());
 			} catch (PlayerException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			player.play();
 		}
 		if (mode == EnqueueMode.ReplaceAll) {
 			clear();
 			enqueue(tracks, EnqueueMode.Immidiaely);
 		}
 	}
 
 	@Override
 	public void enqueue(Item track, EnqueueMode mode) {
 		List<Item> l = new ArrayList<Item>();
 		l.add(track);
 		enqueue(l, mode);
 	}
 
 	@Override
 	public void enqueue(Item track) {
 		enqueue(track, EnqueueMode.Immidiaely);
 	}
 
 	@Override
 	public boolean next() {
 		return seek(+1);
 	}
 
 	@Override
 	public boolean prev() {
 		return seek(-1);
 	}
 
 	@Override
 	public boolean play() {
 		return seek(0);
 	}
 
 	@Override
 	public boolean pause() {
 		player.pause();
 		return true;
 	}
 
 	@Override
 	public boolean resume() {
 		player.resume();
 		return true;
 	}
 
 	@Override
 	public boolean seek(int index) {
		current += index;
		if (current >= size())
			return false;
		if (current < 0)
 			return false;
 		player.stop(Player.Reason.UserBreak);
 		try {
 			player.open(playing_track());
 		} catch (PlayerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		player.play();
 		return true;
 	}
 
 	@Override
 	public void shuffle() {
 		Random r = new Random();
 		int nti = -1;
 		if (player.getStatus() == Status.Playing)
 			nti = playing_index();
 		if (queue == null)
 			return;
 		int N = queue.size();
 		for (int i = 0; i < N * 5; i++) {
 
 			int first = r.nextInt(N);
 			int second = r.nextInt(N);
 			if (first == second)
 				continue;
 			if (first == nti || second == nti)
 				continue;
 			Item F = queue.get(first);
 			Item S = queue.get(second);
 			queue.set(first, S);
 			queue.set(second, F);
 		}
 	}

	@Override
	public boolean set(int index) {
		return seek(index);
	}
 }
