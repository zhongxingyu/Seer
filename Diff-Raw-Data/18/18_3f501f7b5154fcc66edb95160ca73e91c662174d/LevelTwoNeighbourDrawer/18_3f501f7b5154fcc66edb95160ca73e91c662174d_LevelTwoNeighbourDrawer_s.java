 package de.unipotsdam.nexplorer.client.android.maps;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import de.unipotsdam.nexplorer.client.android.js.Marker;
 import de.unipotsdam.nexplorer.client.android.js.SenchaMap;
 import de.unipotsdam.nexplorer.client.android.rest.Neighbour;
 
 public class LevelTwoNeighbourDrawer extends LevelOneNeighbourDrawer implements NeighbourDrawer {
 
 	private SenchaMap senchaMap;
 	private Map<Integer, NeighbourPing> neighbourPings = new HashMap<Integer, NeighbourPing>();
 	private Activity host;
 
 	public LevelTwoNeighbourDrawer(SenchaMap senchaMap, Activity host) {
 		super(senchaMap, host);
 		this.senchaMap = senchaMap;
 		this.host = host;
 	}
 
 	@Override
 	protected void drawNeighbourMarkerAtLatitudeLongitude(int playerId, Neighbour neighbour) {
 		super.drawNeighbourMarkerAtLatitudeLongitude(playerId, neighbour);
		surroundWithPing(playerId, neighbour);
 	}
 
 	private void surroundWithPing(int playerId, Neighbour neighbour) {
 		if (!neighbourPings.containsKey(playerId)) {
 			NeighbourPing ping = new NeighbourPing(senchaMap, neighbour, host, this, playerId);
 			neighbourPings.put(playerId, ping);
 		} else {
 			NeighbourPing ping = neighbourPings.get(playerId);
 			ping.update(neighbour.getLatitude(), neighbour.getLongitude());
 		}
 	}
 
 	public void finishedPing(int playerId) {
 		neighbourPings.remove(playerId);
 	}
 
 	@Override
 	protected void remove(int playerId, Marker neighbour) {
 		super.remove(playerId, neighbour);
 		NeighbourPing ping = neighbourPings.get(playerId);
 		if (ping != null) {
 			ping.kill();
 		}
 	}
 }
