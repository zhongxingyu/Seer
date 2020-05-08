 package com.vdweem.webspotify;
 
 import jahspotify.media.Link;
 import jahspotify.media.Track;
 import jahspotify.services.JahSpotifyService;
 import jahspotify.services.MediaPlayer;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 
 /**
  * Handles the queues
  * @author Niels
  */
 public class QueueHandler {
 
 	private static final LinkedList<Link> list = new LinkedList<Link>();
 	private static final LinkedList<Link> queue = new LinkedList<Link>();
 	private static boolean shuffle = false;
 
 	public static void initialize() {
 		MediaPlayer.getInstance().addQueue(queue);
 		MediaPlayer.getInstance().addQueue(list);
 	}
 
 	public static void save(ObjectOutputStream oos) throws IOException {
 		oos.writeObject(list);
 		oos.writeObject(queue);
 		oos.writeBoolean(shuffle);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static void load(ObjectInputStream ois) throws ClassNotFoundException, IOException {
 		list.addAll((LinkedList<Link>) ois.readObject());
 		queue.addAll((LinkedList<Link>) ois.readObject());
 		shuffle = ois.readBoolean();
 	}
 
 	/**
 	 * Used to determine if the queue has been changed.
 	 * @return
 	 */
 	public static String getRevision() {
 		int firstId = 0;
 		if (queue.size() != 0)
 			firstId = queue.peek().getId().hashCode();
 		return String.format("%s_%s_%s_%s", list.size(), queue.size(), shuffle, firstId);
 	}
 
 	public static JsonObject toJson() {
 		Gson jsc = Gsonner.getGson(Track.class);
 
 		JsonObject result = new JsonObject();
 
 		JsonArray queue = new JsonArray();
 		Iterator<Link> itt = QueueHandler.getQueue().iterator();
 		int totalDuration = 0;
 		int totalTracks = 0;
 		while (itt.hasNext()) {
 			Link link = itt.next();
 			Track track = JahSpotifyService.getInstance().getJahSpotify().readTrack(link);
 			totalDuration += track.getLength();
 			totalTracks++;
 			queue.add(jsc.toJsonTree(track));
 		}
 		result.add("queue", queue);
 
 		JsonArray list = new JsonArray();
 		itt = QueueHandler.getList().iterator();
 		int count = 0;
 		while (itt.hasNext()) {
 			Link link = itt.next();
 			Track track = JahSpotifyService.getInstance().getJahSpotify().readTrack(link);
			if (track == null) continue;
 			totalDuration += track.getLength();
 			count++;
 			if (count < 100)
 				list.add(jsc.toJsonTree(track));
 		}
 		totalTracks += count;
 		result.add("playlist", list);
 		result.addProperty("duration", totalDuration);
 		result.addProperty("size", totalTracks);
 
 		return result;
 	}
 
 	/**
 	 * Adds a track which will be played after all the other tracks.
 	 * @param track
 	 */
 	public static void addToList(Link track) {
 		if (queue.contains(track))
 			return;
 
 		if (list.contains(track))
 			list.remove(track);
 
 		int position = 0;
 		if (shuffle)
 			position = (int) (Math.random() * list.size());
 
 		list.add(position, track);
 	}
 
 	/**
 	 * Adds a track which will be played before the list tracks.
 	 * @param track
 	 */
 	public static void addToQueue(Link track) {
 		list.remove(track);
 		if (queue.contains(track)) {
 			queue.remove(track);
 			queue.add(0, track);
 			return;
 		}
 		queue.add(track);
 	}
 
 	/**
 	 * Removes a track from both queues.
 	 * @param track
 	 */
 	public static void remove(Link track) {
 		queue.remove(track);
 		list.remove(track);
 	}
 
 	/**
 	 * Removes all items from the list.
 	 */
 	public static void clear() {
 		list.clear();
 	}
 
 	/**
 	 * Shuffles the queue
 	 */
 	private static void shuffle() {
 		Collections.shuffle(list);
 	}
 
 	/**
 	 * Returns the queue
 	 * @return
 	 */
 	public static Queue<Link> getQueue() {
 		return queue;
 	}
 
 	/**
 	 * Returns the list
 	 * @return
 	 */
 	public static Queue<Link> getList() {
 		return list;
 	}
 
 	public static boolean isShuffle() {
 		return shuffle;
 	}
 
 	public static void setShuffle(boolean shuffle) {
 		QueueHandler.shuffle = shuffle;
 		if (shuffle)
 			shuffle();
 	}
 
 }
