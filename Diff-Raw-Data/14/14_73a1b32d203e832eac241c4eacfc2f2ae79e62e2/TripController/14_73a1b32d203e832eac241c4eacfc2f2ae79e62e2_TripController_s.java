 package de.htwg.seapal.controller.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import de.htwg.seapal.controller.ITripController;
 import de.htwg.seapal.database.ITripDatabase;
 import de.htwg.seapal.model.ITrip;
 import de.htwg.seapal.observer.Observable;
 
 public class TripController extends Observable implements ITripController {
 
 	private ITripDatabase db;
 
 	public TripController(ITripDatabase db) {
 		this.db = db;
 	}
 
 	@Override
 	public String getName(UUID id) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return null;
 		return trip.getName();
 
 	}
 
 	@Override
 	public void setName(UUID id, String name) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return;
 		trip.setName(name);
 		db.saveTrip(trip);
 		notifyObservers();
 	}
 
 	@Override
 	public void setStartLocation(UUID id, String location) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return;
 		trip.setStartLocation(location);
 		db.saveTrip(trip);
 		notifyObservers();
 	}
 
 	@Override
 	public String getStartLocation(UUID id) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return null;
 		return trip.getStartLocation();
 
 	}
 
 	@Override
 	public void setEndLocation(UUID id, String location) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return;
 		trip.setEndLocation(location);
 		db.saveTrip(trip);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getEndLocation(UUID id) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return null;
 		return trip.getEndLocation();
 
 	}
 
 	@Override
 	public void setSkipper(UUID id, UUID skipper) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return;
 		trip.setSkipper(skipper.toString());
 		db.saveTrip(trip);
 		notifyObservers();
 
 	}
 
 	@Override
 	public UUID getSkipper(UUID id) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return null;
 		return UUID.fromString(trip.getSkipper());
 
 	}
 
 	@Override
 	public void addCrewMember(UUID id, String crewMember) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return;
 		trip.addCrewMember(crewMember);
 		db.saveTrip(trip);
 		notifyObservers();
 	}
 
 	@Override
 	public List<String> getCrewMembers(UUID id) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return null;
 		return trip.getCrewMembers();
 	}
 
 	@Override
 	public void setStartTime(UUID id, long start) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return;
 		trip.setStartTime(start);
 		db.saveTrip(trip);
 		notifyObservers();
 	}
 
 	@Override
 	public long getStartTime(UUID id) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return -1;
 		return trip.getStartTime();
 	}
 
 	@Override
 	public void setEndTime(UUID id, long end) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return;
 		trip.setEndTime(end);
 		db.saveTrip(trip);
 		notifyObservers();
 	}
 
 	@Override
 	public long getEndTime(UUID id) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return -1;
 		return trip.getEndTime();
 	}
 
 	@Override
 	public void setDuration(UUID id, long timeInSeconds) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return;
 		trip.setDuration(timeInSeconds);
 		db.saveTrip(trip);
 		notifyObservers();
 	}
 
 	@Override
 	public long getDuration(UUID id) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return -1;
 		return trip.getDuration();
 	}
 
 	@Override
 	public void setMotor(UUID id, int motor) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return;
 		trip.setMotor(motor);
 		db.saveTrip(trip);
 		notifyObservers();
 	}
 
 	@Override
 	public int getMotor(UUID id) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return -1;
 		return trip.getMotor();
 	}
 
 	@Override
 	public void setFuel(UUID id, double percent) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return;
 		trip.setFuel(percent);
 		db.saveTrip(trip);
 		notifyObservers();
 	}
 
 	@Override
 	public double getFuel(UUID id) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return -1;
 		return trip.getFuel();
 	}
 
 	@Override
 	public void setNotes(UUID id, String text) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return;
 		trip.setNotes(text);
 		db.saveTrip(trip);
 		notifyObservers();
 	}
 
 	@Override
 	public String getNotes(UUID id) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return null;
 		return trip.getNotes();
 	}
 
 	@Override
 	public String getString(UUID id) {
 		return " ID = \t" + id + "\n Name = \t" + getName(id)
 				+ "\n startLocation = \t" + getStartLocation(id)
 				+ "\n endLocation = \t" + getEndLocation(id)
 				+ "\n skipper = \t" + getSkipper(id) + "\n crew = \t"
 				+ getCrewMembers(id) + "\n startTime = \t" + getStartTime(id)
 				+ "\n endTime = \t" + getEndTime(id) + "\n duration = \t"
 				+ getDuration(id) + "\n motor = \t" + getMotor(id)
 				+ "\n fuel = \t" + getFuel(id) + "\n notes = \t" + getNotes(id)
 				+ "\n boat = \t" + getTrip(id) + "\n";
 	}
 
 	@Override
 	public UUID newTrip(UUID boat) {
 		UUID newTrip = db.newTrip();
 		ITrip trip = db.getTrip(newTrip);
 		trip.setBoat(boat.toString());
 		db.saveTrip(trip);
 		notifyObservers(); // ??
 		return newTrip;
 	}
 
 	@Override
 	public void deleteTrip(UUID id) {
 		db.deleteTrip(id);
 		notifyObservers();
 	}
 
 	@Override
 	public void closeDB() {
 		db.closeDB();
 	}
 
 	@Override
 	public List<UUID> getTrips() {
 		List<ITrip> trips = db.getTrips();
 		List<UUID> list = new ArrayList<UUID>();
 		for (ITrip trip : trips) {
 			list.add(UUID.fromString(trip.getId()));
 		}
 		return list;
 	}
 
 	@Override
 	public List<UUID> getTrips(UUID boat) {
 		List<ITrip> query = db.getTrips();
 		List<UUID> list = new ArrayList<UUID>();
 		for (ITrip trip : query) {
			if (trip.getBoat().equals(boat))
 				list.add(UUID.fromString(trip.getId()));
 		}
 		return list;
 	}
 
 	@Override
 	public UUID getTrip(UUID id) {
 		ITrip trip = db.getTrip(id);
 		if (trip == null)
 			return null;
 		return UUID.fromString(trip.getId());
 	}
 
 }
