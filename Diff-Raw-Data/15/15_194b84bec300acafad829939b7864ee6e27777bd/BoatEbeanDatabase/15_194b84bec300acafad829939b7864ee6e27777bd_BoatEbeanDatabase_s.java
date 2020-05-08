 package de.htwg.seapal.boat.database.impl;
 
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import com.avaje.ebean.Ebean;
 
 import de.htwg.seapal.boat.database.IBoatDatabase;
 import de.htwg.seapal.boat.models.IBoat;
 import de.htwg.seapal.boat.models.impl.Boat;
 import de.htwg.seapal.boat.models.impl.IdContainer;
 
 public class BoatEbeanDatabase implements IBoatDatabase {
 
 	@Override
 	public String newBoat() {
 		IBoat newBoat = new Boat();
 		String id = getNewId();
 		newBoat.setId(id);
 		Ebean.save(newBoat);
 		return id;
 	}
 
 	@Override
 	public void saveBoat(IBoat boat) {
 		Ebean.save(boat);
 	}
 
 	@Override
 	public void deleteBoat(String id) {
		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public IBoat getBoat(String id) {
 		IBoat boat = Ebean.find(IBoat.class, id);
 		if (boat == null)
 			throw new NoSuchElementException("No Boat for id : " + id);
 		return boat;
 	}
 
 	@Override
 	public List<IBoat> getBoats() {
 		return Ebean.find(IBoat.class).findList();
 	}
 
 	private String getNewId() {
 		IdContainer currentId;
 		List<IdContainer> query = Ebean.find(IdContainer.class).findList();
 		if (query.isEmpty()) {
 			currentId = new IdContainer(0);
 			Ebean.save(currentId);
 		} else {
 			currentId = query.get(0);
 			currentId.setId(currentId.getId() + 1);
 			Ebean.save(currentId);
 		}
 		return "BOAT-" + currentId.getId();
 	}
 
 	@Override
 	public boolean closeDB() {
		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
