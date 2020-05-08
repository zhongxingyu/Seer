 package com.example.locus.dht;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import sun.util.logging.resources.logging;
 
 import android.util.Log;
 
 import com.example.locus.IPAddress;
 import com.example.locus.core.Constants;
 import com.example.locus.entity.ErrorCodes;
 import com.example.locus.entity.Result;
 import com.example.locus.entity.User;
 import com.example.locus.tilesystem.TileSystem;
 
 import de.uniba.wiai.lspi.chord.data.URL;
 import de.uniba.wiai.lspi.chord.service.Chord;
 import de.uniba.wiai.lspi.chord.service.ServiceException;
 
 public class chordDHT implements IDHT {
 
 	private static URL bootstrap_url = null;
 	private static URL local_url = null;
 	private Chord chord_instance = null;
 
 	static void set_properties() {
 		System.setProperty("log4j.properties.file", "log4j.properties");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.data.ID.number.of.displayed.bytes",
 				"4");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.data.ID.displayed.representation",
 				"2");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.service.impl.ChordImpl.successors",
 				"2");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.service.impl.ChordImpl.AsyncThread.no",
 				"10");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.service.impl.ChordImpl.StabilizeTask.start",
 				"12");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.service.impl.ChordImpl.StabilizeTask.interval",
 				"12");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.service.impl.ChordImpl.FixFingerTask.start",
 				"0");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.service.impl.ChordImpl.FixFingerTask.interval",
 				"12");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.service.impl.ChordImpl.CheckPredecessorTask.start",
 				"6");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.service.impl.ChordImpl.CheckPredecessorTask.interval",
 				"12");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.com.socket.InvocationThread.corepoolsize",
 				"10");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.com.socket.InvocationThread.maxpoolsize",
 				"50");
 
 		System.setProperty(
 				"de.uniba.wiai.lspi.chord.com.socket.InvocationThread.keepalivetime",
 				"20");
 	}
 
 	public chordDHT() {
 		set_properties();
 		// de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();
 	}
 
 	/*
 	 * This function will get the public bootstrap URL Either use DNS or some
 	 * other mechanism to get it. For now implementing it using the local IP
 	 * address
 	 */
 	public static void set_bootstrap_url() {
 
 		if (bootstrap_url != null)
 			return;
 
 		String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
 		try {
 			bootstrap_url = new URL(protocol
 					+ "://catshark.ics.cs.cmu.edu:58384/ ");
 		} catch (MalformedURLException e) {
 			/* what should we do in this case ? */
 			// throw new RuntimeException(e);
 		}
 	}
 
 	public static void set_local_url() {
 
 		if (local_url != null)
 			return;
 
 		String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
 
 		/*
 		 * Here instead of using local host .. try to get the public ip address
 		 * of the host. Also if there are two chords in the system, then we
 		 * might want to change the port. For now fixing the port that is used
 		 * to connect to to the CHord network.
 		 */
 		String ip_address = null;
 		try {
 			ip_address = IPAddress.getIPAddress(true);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		try {
 			// local_url = new URL( protocol + "://localhost:8082/ ");
 			System.out.println("Local IP = " + ip_address);
 			local_url = new URL(protocol + "://" + ip_address + ":8082/ ");
 		} catch (MalformedURLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public Result join() {
 
 		set_bootstrap_url();
 		set_local_url();
 		chord_instance = new de.uniba.wiai.lspi.chord.service.impl.ChordImpl();
 
 		if ((local_url == null) || (bootstrap_url == null))
 			return new Result(false, ErrorCodes.DHTError);
 
 		try {
 			System.out.println("Before DHT Join");
 			chord_instance.join(local_url, bootstrap_url);
 		} catch (ServiceException e) {
 			e.printStackTrace();
 			Log.e(Constants.appChordTag, "join error e = " + e);
 			return new Result(false, ErrorCodes.DHTError);
 		}
 		return Result.Success;
 	}
 
 	public Result create() {
 
 		/*
 		 * the first guy that has created the Chord will be used as a bootstrap
 		 * server for this rough implementation
 		 */
 		set_bootstrap_url();
 		/*
 		 * Getting an error if we use this chord instance to put and get some
 		 * elements into the chord. So locally using this instance to just
 		 * create the chord. and again join function has to be called to return
 		 * the global instance that has to be used to put , delete and retreive
 		 * elemets.
 		 */
 		Chord lchord = new de.uniba.wiai.lspi.chord.service.impl.ChordImpl();
 
 		try {
 			lchord.create(bootstrap_url);
 		} catch (ServiceException e) {
 			// throw new RuntimeException("Could not create DHT!",e);
 			return new Result(false, ErrorCodes.DHTError);
 		}
 
 		return Result.Success;
 	}
 
 	public Result put(User user) {
 
 		System.out.println("Put user " + user);
 		/* chord is not instantiated properly */
 		if ((this.chord_instance == null) || (user == null))
 			return new Result(false, ErrorCodes.DHTError);
 
 		String tilenum = user.getTileNumber();
 		if (tilenum == null)
 			return new Result(false, ErrorCodes.DHTError);
 
 		/* Create a key based on the tile number */
 		TileKey tk = new TileKey(tilenum);
 		try {
 			chord_instance.insert(tk, user.serialize());
 		} catch (ServiceException e) {
 			// throw new
 			// RuntimeException("Can not insert the data into DHT!",e);
 			return new Result(false, ErrorCodes.DHTError);
 		}
 		return Result.Success;
 	}
 
 	public Set<User> getUsersByKey(User user) {
 		Set<Serializable> s = null;
 		Set<User> nearby_users = new HashSet<User>();
 		String tile_num = user.getTileNumber();
 		System.out.println("Tile Number: " + tile_num);
 		List<String> nearby_tiles = TileSystem.getNearbyQuadKey(tile_num);
 		for (String tile : nearby_tiles) {
 			System.out.println("\n: " + tile);
 		}
 
 		try {
 			/*
 			 * For now retrieving only the users from a given tile number. But
 			 * we should call some tile api to get the surrounding tile numbers
 			 * and then retrieve all the users from these tile numbers.
 			 */
 			for (String tile : nearby_tiles) {
 				s = chord_instance.retrieve(new TileKey(tile));
 				if (s != null) {
 					for (Serializable serializableObjs : s) {
 						nearby_users.add(new User((String) serializableObjs));
 					}
 				}
 			}
 
 		} catch (ServiceException e) {
 			e.printStackTrace();
 			return null;
 		}
 		return nearby_users;
 	}
 
 	public Result delete(User user) {
 
 		try {
			chord_instance.remove(new TileKey(user.getTileNumber()), user.serialize());
 		} catch (ServiceException e) {
 			return new Result(false, ErrorCodes.DHTError);
 		}
 
 		return Result.Success;
 	}
 
 	public void leave() {
 		try {
 			chord_instance.leave();
 		} catch (ServiceException e) {
 			e.printStackTrace();
 		} 
 	}
 }
