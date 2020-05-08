 import java.util.LinkedList;
 import java.util.List;
 
 import json.models.ArtistAlbumTrack;
 import json.models.TrackPlaylist;
 import json.request.ControllerRequest;
 import json.request.JsonController;
 import json.response.CurrentResponse;
 import json.response.JsonCurrent;
 import json.response.LoggedResponse;
 import json.response.PlaylistResponse;
 import json.response.RankResponse;
 import json.response.SearchResponse;
 import json.response.TokenResponse;
 import json.response.TracksResponse;
 
 import com.esotericsoftware.kryonet.Connection;
 import com.google.gson.Gson;
 
 public class TreeRemoteImpl implements ITreeRemote {
 
 	private boolean loggedin = false;
 	private PlaylistResponse playlists;
 	private long trackStarted;
 	private JsonCurrent current;
 	private Connection c;
 	private boolean isTouching = false;
 	private long lastTouch = 0;
 	private List<Long> latestReleases = new LinkedList<Long>();
 	private boolean isChangeingVolume = false;
 	private boolean increaseLast = false;
 	private static final int PAUSELIMIT = 800;
 
 	public TreeRemoteImpl() {
 		latestReleases.add(new Long(0));
 	}
 
 	public boolean isLoggedin() {
 		return loggedin;
 	}
 
 	public void setLoggedin(boolean loggedin) {
 		this.loggedin = loggedin;
 	}
 
 	public long getTrackStarted() {
 		return trackStarted;
 	}
 
 	public void setTrackStarted(long trackStarted) {
 		this.trackStarted = trackStarted;
 	}
 
 	@Override
 	public void connectedToTree() {
 		System.out.println("contree");
 	}
 
 	@Override
 	public void disconnectedFromTree() {
 		System.out.println("distree");
 	}
 
 	public void pause() {
 		ControllerRequest r = new ControllerRequest();
 		JsonController con = new JsonController();
 		con.setPaused(true);
 		r.setJsonController(con);
 		c.sendTCP(new Gson().toJson(r));
 	}
 
 	public int numberOfTouchesInRow() {
 		int touches = 0;
 		if (System.currentTimeMillis()
 				- latestReleases.get(latestReleases.size() - 1) < PAUSELIMIT) {
 			touches++;
 			for (int i = latestReleases.size() - 1; i > 0; i--) {
 				if (latestReleases.get(i) - latestReleases.get(i - 1) < PAUSELIMIT) {
 					touches++;
 				} else {
 					break;
 				}
 			}
 		}
 		return touches;
 	}
 
 	public void next() {
 		ControllerRequest r = new ControllerRequest();
 		JsonController con = new JsonController();
 		con.setNext(true);
 		r.setJsonController(con);
 		c.sendTCP(new Gson().toJson(r));
 	}
 
 	public boolean isTouch(int v) {
 		return v == 255;
 	}
 
 	public boolean shouldChangeTrack() {
 		return numberOfTouchesInRow() == 2;
 	}
 
 	public boolean shouldIncreaseVolume() {
 		return current.getVolume() == 0
 				|| (current.getVolume() != 100 && System.currentTimeMillis() - lastTouch > 500 && ((isChangeingVolume && increaseLast) || (!isChangeingVolume && !increaseLast)));
 	}
 
 	public boolean shouldDecreaseVolume() {
 		return current.getVolume() == 100
 				|| (current.getVolume() != 0 && System.currentTimeMillis() - lastTouch > 500 && ((isChangeingVolume && !increaseLast) || (!isChangeingVolume && increaseLast)));
 	}
	
	public boolean shouldPlayLemonTree() {
		return numberOfTouchesInRow() == 5;
	}
	
	public void play (String uri) {
		ControllerRequest r = new ControllerRequest();
		JsonController con = new JsonController();
		con.setPlayTrack(uri);
		r.setJsonController(con);
		c.sendTCP(new Gson().toJson(r));
	}
 
 	public void startPause() {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Thread.sleep(PAUSELIMIT);
 					if (numberOfTouchesInRow() == 0) {
 						pause();
 					}
 				} catch (InterruptedException e) {
 				}
 			}
 		}).start();
 	}
 
 	public void increaseVolume() {
 		if (current.getVolume() < 100) {
 			ControllerRequest r = new ControllerRequest();
 			JsonController con = new JsonController();
 			con.setVolume(current.getVolume() + 2);
 			r.setJsonController(con);
 			c.sendTCP(new Gson().toJson(r));
 		}
 	}
 
 	public void decreaseVolume() {
 		if (current.getVolume() > 0) {
 			ControllerRequest r = new ControllerRequest();
 			JsonController con = new JsonController();
 			con.setVolume(current.getVolume() - 2);
 			r.setJsonController(con);
 			c.sendTCP(new Gson().toJson(r));
 		}
 	}
 
 	@Override
 	public void dataFromTree(String s) {
 		if (s.startsWith("SENSOR")) {
 			String values[] = s.substring(6, s.length()).split(",");
 			for (String v : values) {
 				if (c != null && c.isConnected()) {
 					int value = (int) Double.parseDouble(v);
 					if (isTouch(value) && !isTouching) {
 						isTouching = true;
 						lastTouch = System.currentTimeMillis();
 					} else if (isTouch(value)) {
 						if (shouldIncreaseVolume()) {
 							isChangeingVolume = true;
 							increaseLast = true;
 							increaseVolume();
 						} else if (shouldDecreaseVolume()) {
 							isChangeingVolume = true;
 							increaseLast = false;
 							decreaseVolume();
 						}
 					} else { // när man inte är emot!
 						isChangeingVolume = false;
 						if (isTouching) { // när man precis varit emot!
 							latestReleases.add(System.currentTimeMillis());
 							if (System.currentTimeMillis() - lastTouch < 500) {
 								if (shouldChangeTrack()) {
 									next();
								} else if(shouldPlayLemonTree()) {
									play("spotify:track:1yN2z5XVtaAOYGdeEqEuqd");
 								} else {
 									startPause();
 								}
 							}
 						}
 						isTouching = false;
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void disconnectedFromBohnify() {
 		System.out.println("disbohn");
 	}
 
 	@Override
 	public void dataFromBohnify(String s) {
 		try {
 			if (s.contains("jsonLogged")) {
 				LoggedResponse r = (LoggedResponse) new Gson().fromJson(s,
 						LoggedResponse.class);
 				loggedin = false;
 			} else if (s.contains("jsonPlayLists")) {
 				PlaylistResponse r = (PlaylistResponse) new Gson().fromJson(s,
 						PlaylistResponse.class);
 				this.playlists = r;
 			} else if (s.contains("jsonSearch")) {
 				SearchResponse r = (SearchResponse) new Gson().fromJson(s,
 						SearchResponse.class);
 			} else if (s.contains("jsonToplist")) {
 				TracksResponse r = (TracksResponse) new Gson().fromJson(s,
 						TracksResponse.class);
 			} else if (s.contains("jsonHistory")) {
 				TracksResponse r = (TracksResponse) new Gson().fromJson(s,
 						TracksResponse.class);
 			} else if (s.contains("jsonQueue")) {
 				TracksResponse r = (TracksResponse) new Gson().fromJson(s,
 						TracksResponse.class);
 			} else if (s.contains("jsonCurrent")) {
 				CurrentResponse r = (CurrentResponse) new Gson().fromJson(s,
 						CurrentResponse.class);
 				if (r.getJsonCurrent() != null) {
 					trackStarted = System.currentTimeMillis()
 							- r.getJsonCurrent().getPosition();
 				}
 				current = r.getJsonCurrent();
 				loggedin = true;
 			} else if (s.contains("jsonRank")) {
 				RankResponse r = (RankResponse) new Gson().fromJson(s,
 						RankResponse.class);
 				if (r.getJsonRank() != null) {
 					for (ArtistAlbumTrack track : r.getJsonRank().getTracks()) {
 						for (TrackPlaylist p : playlists.getJsonPlayLists()
 								.getPlayLists()) {
 							for (ArtistAlbumTrack track1 : p.getTracks()) {
 								if (track.equals(track1)) {
 									track1.setRank(track.getRank());
 								}
 							}
 						}
 					}
 
 				}
 			} else if (s.contains("jsonToken")) {
 				TokenResponse r = (TokenResponse) new Gson().fromJson(s,
 						TokenResponse.class);
 				if (r.getJsonToken() != null) {
 				}
 			}
 		} catch (Exception e) {
 			e.getStackTrace();
 		}
 
 	}
 
 	@Override
 	public void connectedToBohnify(Connection c) {
 		System.out.println("conbohn");
 		this.c = c;
 	}
 
 }
