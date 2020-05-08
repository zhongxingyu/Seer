 package de.htwg.seapal.boat.controllers.impl;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.inject.Inject;
 
 import de.htwg.seapal.boat.controllers.IBoatController;
 import de.htwg.seapal.boat.database.IBoatDatabase;
 import de.htwg.seapal.boat.models.IBoat;
 import de.htwg.seapal.boat.util.observer.Observable;
 import de.htwg.seapal.person.controllers.IPersonController;
 
 public class BoatController extends Observable implements IBoatController {
 
 	protected IPersonController personContoller;
 	protected IBoatDatabase db;
 	
 
 	@Inject
 	public BoatController(IBoatDatabase db, IPersonController personController) {
 		this.personContoller = personController;
 		this.db = db;
 	}
 
 	@Override
 	public Map<String, String> getBoats() {
 		List<IBoat> query = db.getBoats();
 		Map<String, String> map = new HashMap<String, String>();
 		for (IBoat boat : query) {
 			map.put(boat.getId(), boat.getBootsname());
 		}
 		return map;
 	}
 
 	@Override
 	public String getBootsnameById(String id) {
 		return getBootsname(id);
 	}
 
 	@Override
 	public String getBootsname(final String id) {
 		return db.getBoat(id).getBootsname();
 	}
 
 	@Override
 	public void setBootsname(String id, String bootsname) {
 		IBoat boat = db.getBoat(id);
 		boat.setBootsname(bootsname);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public String getRegisterNr(String id) {
 		return db.getBoat(id).getRegisterNr();
 	}
 
 	@Override
 	public void setRegisterNr(String id, String registerNr) {
 		IBoat boat = db.getBoat(id);
 		boat.setRegisterNr(registerNr);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public String getSegelzeichen(String id) {
 		return db.getBoat(id).getSegelzeichen();
 	}
 
 	@Override
 	public void setSegelzeichen(String id, String segelzeichen) {
 		IBoat boat = db.getBoat(id);
 		boat.setSegelzeichen(segelzeichen);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public String getHeimathafen(String id) {
 		return db.getBoat(id).getHeimathafen();
 	}
 
 	@Override
 	public void setHeimathafen(String id, String heimathafen) {
 		IBoat boat = db.getBoat(id);
 		boat.setHeimathafen(heimathafen);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public String getYachtclub(String id) {
 		return db.getBoat(id).getYachtclub();
 	}
 
 	@Override
 	public void setYachtclub(String id, String yachtclub) {
 		IBoat boat = db.getBoat(id);
 		boat.setYachtclub(yachtclub);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public String getEigner(String id) {
 		return db.getBoat(id).getEigner();
 	}
 
 	@Override
 	public void setEigner(String id, String eigner) {
 		IBoat boat = db.getBoat(id);
 		boat.setEigner(eigner);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public String getVersicherung(String id) {
 		return db.getBoat(id).getVersicherung();
 	}
 
 	@Override
 	public void setVersicherung(String id, String versicherung) {
 		IBoat boat = db.getBoat(id);
 		boat.setVersicherung(versicherung);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public String getRufzeichen(String id) {
 		return db.getBoat(id).getRufzeichen();
 	}
 
 	@Override
 	public void setRufzeichen(String id, String rufzeichen) {
 		IBoat boat = db.getBoat(id);
 		boat.setRufzeichen(rufzeichen);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public String getTyp(String id) {
 		return db.getBoat(id).getTyp();
 	}
 
 	@Override
 	public void setTyp(String id, String typ) {
 		IBoat boat = db.getBoat(id);
 		boat.setTyp(typ);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public String getKonstrukteur(String id) {
 		return db.getBoat(id).getKonstrukteur();
 	}
 
 	@Override
 	public void setKonstrukteur(String id, String konstrukteur) {
 		IBoat boat = db.getBoat(id);
 		boat.setKonstrukteur(konstrukteur);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public double getLaenge(String id) {
 		return db.getBoat(id).getLaenge();
 	}
 
 	@Override
 	public void setLaenge(String id, double laenge) {
 		IBoat boat = db.getBoat(id);
 		boat.setLaenge(laenge);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public double getBreite(String id) {
 		return db.getBoat(id).getBreite();
 	}
 
 	@Override
 	public void setBreite(String id, double breite) {
 		IBoat boat = db.getBoat(id);
 		boat.setBreite(breite);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public double getTiefgang(String id) {
 		return db.getBoat(id).getTiefgang();
 	}
 
 	@Override
 	public void setTiefgang(String id, double tiefgang) {
 		IBoat boat = db.getBoat(id);
 		boat.setTiefgang(tiefgang);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public double getMasthoehe(String id) {
 		return db.getBoat(id).getMasthoehe();
 	}
 
 	@Override
 	public void setMasthoehe(String id, double masthoehe) {
 		IBoat boat = db.getBoat(id);
 		boat.setMasthoehe(masthoehe);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public double getVerdraengung(String id) {
 		return db.getBoat(id).getVerdraengung();
 	}
 
 	@Override
 	public void setVerdraengung(String id, double verdraengung) {
 		IBoat boat = db.getBoat(id);
 		boat.setVerdraengung(verdraengung);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public String getRiggArt(String id) {
 		return db.getBoat(id).getRiggArt();
 	}
 
 	@Override
 	public void setRiggArt(String id, String riggArt) {
 		IBoat boat = db.getBoat(id);
 		boat.setRiggArt(riggArt);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public int getBaujahr(String id) {
 		return db.getBoat(id).getBaujahr();
 	}
 
 	@Override
 	public void setBaujahr(String id, int baujahr) {
 		IBoat boat = db.getBoat(id);
 		boat.setBaujahr(baujahr);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public String getMotor(String id) {
 		return db.getBoat(id).getMotor();
 	}
 
 	@Override
 	public void setMotor(String id, String motor) {
 		IBoat boat = db.getBoat(id);
 		boat.setMotor(motor);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public double getTankGroesse(String id) {
 		return db.getBoat(id).getTankGroesse();
 	}
 
 	@Override
 	public void setTankGroesse(String id, double tankGroesse) {
 		IBoat boat = db.getBoat(id);
 		boat.setTankGroesse(tankGroesse);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public double getWassertankGroesse(String id) {
 		return db.getBoat(id).getWassertankGroesse();
 	}
 
 	@Override
 	public void setWassertankGroesse(String id, double wassertankGroesse) {
 		IBoat boat = db.getBoat(id);
 		boat.setWassertankGroesse(wassertankGroesse);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public double getAbwassertankGroesse(String id) {
 		return db.getBoat(id).getAbwassertankGroesse();
 	}
 
 	@Override
 	public void setAbwassertankGroesse(String id, double abwassertankGroesse) {
 		IBoat boat = db.getBoat(id);
 		boat.setAbwassertankGroesse(abwassertankGroesse);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public double getGrosssegelGroesse(String id) {
 		return db.getBoat(id).getGrosssegelGroesse();
 	}
 
 	@Override
 	public void setGrosssegelGroesse(String id, double grosssegelGroesse) {
 		IBoat boat = db.getBoat(id);
 		boat.setGrosssegelGroesse(grosssegelGroesse);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public double getGenuaGroesse(String id) {
 		return db.getBoat(id).getGenuaGroesse();
 	}
 
 	@Override
 	public void setGenuaGroesse(String id, double genuaGroesse) {
 		IBoat boat = db.getBoat(id);
 		boat.setGenuaGroesse(genuaGroesse);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public double getSpiGroesse(String id) {
 		return db.getBoat(id).getSpiGroesse();
 	}
 
 	@Override
 	public void setSpiGroesse(String id, double spiGroesse) {
 		IBoat boat = db.getBoat(id);
 		boat.setSpiGroesse(spiGroesse);
 		db.saveBoat(boat);
 		notifyObservers();
 	}
 
 	@Override
 	public String getString(String id) {
 		return "Bootsname = " + getBootsname(id) + ", ID = " + id
 				+ ", RegisterNr = " + getRegisterNr(id) + ", Segelzeichen = "
 				+ getSegelzeichen(id) + ", Heimathafen = " + getHeimathafen(id)
 				+ ", Yachtclub = " + getYachtclub(id) + ", Eigner = "
 				+ getEigner(id) + ", Versicherung = " + getVersicherung(id)
 				+ ", Rufzeichen = " + getRufzeichen(id) + ", Typ = "
 				+ getTyp(id) + ", Konstrukteur = " + getKonstrukteur(id)
 				+ ", Laenge = " + getLaenge(id) + ", Breite = " + getBreite(id)
 				+ ", Tiefgang = " + getTiefgang(id) + ", Masthoehe = "
 				+ getMasthoehe(id) + ", Verdraengung = " + getVerdraengung(id)
 				+ ", RiggArt = " + getRiggArt(id) + ", Baujahr = "
 				+ getBaujahr(id) + ", Motor = " + getMotor(id)
 				+ ", TankGroesse = " + getTankGroesse(id)
 				+ ", WassertankGroesse = " + getWassertankGroesse(id)
 				+ ", AbwassertankGroesse = " + getAbwassertankGroesse(id)
 				+ ", GrosssegelGroesse = " + getGrosssegelGroesse(id)
 				+ ", GenuaGroesse = " + getGenuaGroesse(id) + ", SpiGroesse = "
 				+ getSpiGroesse(id);
 	}
 
 	@Override
 	public Map<String, String> getPersons() {
 		return personContoller.getPersonList();
 	}
 
 	@Override
 	public String getPersonnameById(String id) {
 		return personContoller.getPersonLastname(id) + ", "
 				+ personContoller.getPersonFirstname(id);
 	}
 
 	@Override
 	public String newBoat() {
 		return db.newBoat();
 	}
 
 	public void closeDB() {
 		db.closeDB();
 	}
 
 	@Override
 	public void deleteBoat(String id) {
 		db.deleteBoat(id);
 		notifyObservers();
 	}
 }
