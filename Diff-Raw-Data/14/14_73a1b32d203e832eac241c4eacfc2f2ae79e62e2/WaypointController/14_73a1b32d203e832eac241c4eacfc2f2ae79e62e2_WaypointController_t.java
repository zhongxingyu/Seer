 package de.htwg.seapal.controller.impl;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import android.location.Location;
 import de.htwg.seapal.controller.IWaypointController;
 import de.htwg.seapal.database.IWaypointDatabase;
 import de.htwg.seapal.model.IWaypoint;
 import de.htwg.seapal.model.IWaypoint.ForeSail;
 import de.htwg.seapal.model.IWaypoint.MainSail;
 import de.htwg.seapal.model.IWaypoint.Maneuver;
 import de.htwg.seapal.observer.Observable;
 
 public class WaypointController extends Observable implements
 		IWaypointController {
 
 	/** Controller handeling the persistence. */
 	private final IWaypointDatabase db;
 
 	/**
 	 * Creates an instance with a waypoint. Only for generalized classes.
 	 * 
 	 * @param pWaypoint
 	 *            the waypoint.
 	 */
 	public WaypointController(IWaypointDatabase db) {
 		this.db = db;
 	}
 
 	@Override
 	public final String getName(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return null;
 		return waypoint.getName();
 	}
 
 	@Override
 	public double getLatitude(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return -1;
 		return waypoint.getLatitude();
 	}
 
 	@Override
 	public double getLongitude(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return -1;
 		return waypoint.getLongitude();
 	}
 
 	@Override
 	public final String getNote(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return null;
 		return waypoint.getNote();
 	}
 
 	@Override
 	public final int getBTM(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return -1;
 		return waypoint.getBTM();
 	}
 
 	@Override
 	public final int getDTM(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return -1;
 		return waypoint.getDTM();
 	}
 
 	@Override
 	public final int getCOG(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return -1;
 		return waypoint.getCOG();
 	}
 
 	@Override
 	public final int getSOG(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return -1;
 		return waypoint.getSOG();
 	}
 
 	@Override
 	public final UUID getHeadedFor(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return null;
 		return UUID.fromString(waypoint.getHeadedFor());
 	}
 
 	@Override
 	public final Maneuver getManeuver(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return null;
 		return waypoint.getManeuver();
 	}
 
 	@Override
 	public final ForeSail getForesail(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return null;
 		return waypoint.getForesail();
 	}
 
 	@Override
 	public final MainSail getMainsail(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return null;
 		return waypoint.getMainsail();
 	}
 
 	@Override
 	public final void setForesail(UUID id, final ForeSail foreSail) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return;
 		waypoint.setForesail(foreSail);
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 	}
 
 	@Override
 	public final void setName(UUID id, final String name) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return;
 		waypoint.setName(name);
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 
 	}
 
 	@Override
 	public void setLatitude(UUID id, double latitude) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return;
 		waypoint.setLatitude(latitude);
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 	}
 
 	@Override
 	public void setLongitude(UUID id, double longitude) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return;
 		waypoint.setLongitude(longitude);
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 	}
 
 	@Override
 	public final void setNote(UUID id, final String note) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return;
 		waypoint.setNote(note);
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 	}
 
 	@Override
 	public final void setBTM(UUID id, final int btm) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return;
 		waypoint.setBTM(btm);
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 	}
 
 	@Override
 	public final void setDTM(UUID id, final int dtm) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return;
 		waypoint.setDTM(dtm);
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 	}
 
 	@Override
 	public final void setCOG(UUID id, final int cog) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return;
 		waypoint.setCOG(cog);
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 	}
 
 	@Override
 	public final void setSOG(UUID id, final int sog) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return;
 		waypoint.setSOG(sog);
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 	}
 
 	@Override
 	public final void setHeadedFor(UUID id, final UUID markId) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return;
 		waypoint.setHeadedFor(markId);
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 	}
 
 	@Override
 	public final void setManeuver(UUID id, final Maneuver maneuver) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return;
 		waypoint.setManeuver(maneuver);
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 	}
 
 	@Override
 	public final void setMainsail(UUID id, final MainSail mainSail) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return;
 		waypoint.setMainsail(mainSail);
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 	}
 
 	@Override
 	public final String getString(UUID id) {
 		IWaypoint waypoint = db.getWaypoint(id);
 		if (waypoint == null)
 			return null;
 		return "Waypoint:" + waypoint.toString();
 	}
 
 	@Override
 	public final UUID newWaypoint(UUID trip) {
 		UUID newWaypoint = db.newWaypoint();
 		IWaypoint waypoint = db.getWaypoint(newWaypoint);
 		waypoint.setTrip(trip.toString());
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 		return newWaypoint;
 	}
 
 	@Override
 	public final UUID newWaypoint(UUID trip, Location location, long date) {
 		UUID newWaypoint = db.newWaypoint();
 		IWaypoint waypoint = db.getWaypoint(newWaypoint);
 		waypoint.setTrip(trip.toString());
 		waypoint.setLatitude(location.getLatitude());
 		waypoint.setLongitude(location.getLongitude());
 		waypoint.setDate(date);
 		DateFormat format = SimpleDateFormat.getDateTimeInstance();
 		waypoint.setName(format.format(date * 1000));
 		db.saveWaypoint(waypoint);
 		notifyObservers();
 		return newWaypoint;
 	}
 
 	@Override
 	public final void deleteWaypoint(UUID id) {
 		db.deleteWaypoint(id);
 		notifyObservers();
 	}
 
 	@Override
 	public final void closeDB() {
 		db.closeDB();
 	}
 
 	@Override
 	public final List<UUID> getWaypoints() {
 		List<IWaypoint> waypoints = db.getWaypoints();
 		List<UUID> waypointIDs = new ArrayList<UUID>();
 		for (IWaypoint waypoint : waypoints) {
 			waypointIDs.add(UUID.fromString(waypoint.getId()));
 		}
 		return waypointIDs;
 	}
 
 	@Override
 	public List<UUID> getWaypoints(UUID trip) {
 		List<IWaypoint> waypoints = db.getWaypoints();
 		List<UUID> waypointIDs = new ArrayList<UUID>();
 		for (IWaypoint waypoint : waypoints) {
			if (waypoint.getTrip().equals(trip.toString()))
 				waypointIDs.add(UUID.fromString(waypoint.getId()));
 		}
 		return waypointIDs;
 	}
 
 }
