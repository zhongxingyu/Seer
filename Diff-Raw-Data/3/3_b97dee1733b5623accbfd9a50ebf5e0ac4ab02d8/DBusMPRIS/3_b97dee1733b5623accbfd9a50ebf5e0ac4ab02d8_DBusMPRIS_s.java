 package dbus.mpris;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.freedesktop.MediaPlayer;
 import org.freedesktop.dbus.DBusConnection;
 import org.freedesktop.dbus.Variant;
 import org.freedesktop.dbus.exceptions.DBusException;
 
 import server.Server;
 
 import dbus.DBus;
 
 public class DBusMPRIS extends DBus {
 	MediaPlayer mediaPlayer;
 	TrackChangeHandler handler;
 
 	public DBusMPRIS(Server s) {
 		super(s);
 		objectPath = "/Player";
 		serviceBusName = "org.mpris.vlc";
 		handler = new TrackChangeHandler(server);
 	}
 
 	/**
 	 * Start playing when stopped Pause when playing Play when in pause
 	 */
 	@Override
 	public void togglePlayPause() {
 		System.out.println("DBUS Play");
 		// If stopped, play
 		if (mediaPlayer.GetStatus().playingStatus == 2) {
 			mediaPlayer.Play();
 		} else { // Toggle play/pause
 			mediaPlayer.Pause();
 		}
 	}
 
 	/**
 	 * Pause if playing, do nothing otherwise
 	 */
 	@Override
 	public void pause() {
 		if (mediaPlayer.GetStatus().playingStatus == 0) {
 			mediaPlayer.Pause();
 		}
 	}
 
 	@Override
 	public void next() {
 		mediaPlayer.Next();
 	}
 
 	@Override
 	public void previous() {
 		mediaPlayer.Prev();
 	}
 
 	@Override
 	public void connect() throws DBusException {
 		try {
 			conn = DBusConnection.getConnection(DBusConnection.SESSION);
 			mediaPlayer = (MediaPlayer) conn.getRemoteObject(serviceBusName,
 					objectPath);
 			conn.addSigHandler(MediaPlayer.TrackChange.class, handler);
 		} catch (DBusException e) {
 			connected = false;
 			throw e;
 		}
 	}
 
 	@Override
 	public void setVolume(int value) {
 		if (value == 0)
 			return;
 		int volume = mediaPlayer.VolumeGet();
 		if (volume + value > 100) {
 			volume = 100;
 		} else if (volume + value < 0) {
 			volume = 0;
 		} else {
 			volume = volume + value;
 		}
 		mediaPlayer.VolumeSet(volume);
 	}
 
 	@Override
 	public void disconnect() {
 		conn.disconnect();
 	}
 
 	@Override
 	public void setPosition(int pos) {
 		mediaPlayer.PositionSet(pos);
 	}
 
 	@Override
 	public int getPosition() {
 		return mediaPlayer.PositionGet();
 	}
 
 	@Override
 	public int getTotalLenght() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public Map<String, String> getMetaData() {
 		Map<String, Variant> dmap = mediaPlayer.GetMetadata();
 		Map<String, String> map = new HashMap<String, String>(dmap.size());
 		for (String key : dmap.keySet()) {
 			map.put(key, dmap.toString());
 		}
 		return map;
 	}
 }
