 package de.htwg.seapal.boat.controllers.mock;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.inject.Inject;
 
 import de.htwg.seapal.boat.controllers.IBoatController;
 import de.htwg.seapal.boat.models.IBoat;
 import de.htwg.seapal.boat.util.observer.Observable;
 
 public class BoatController extends Observable implements IBoatController {
 
 	private IBoat boat;
 
 	@Inject
 	public BoatController(IBoat boat) {
 		this.boat = boat;
 	}
 
 	@Override
 	public String getBootsname() {
 		return boat.getBootsname();
 	}
 
 	@Override
 	public void setBootsname(String bootsname) {
 		boat.setBootsname(bootsname);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getRegisterNr() {
 		return boat.getRegisterNr();
 	}
 
 	@Override
 	public void setRegisterNr(String registerNr) {
 		boat.setRegisterNr(registerNr);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getSegelzeichen() {
 		return boat.getSegelzeichen();
 	}
 
 	@Override
 	public void setSegelzeichen(String segelzeichen) {
 		boat.setSegelzeichen(segelzeichen);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getHeimathafen() {
 		return boat.getHeimathafen();
 	}
 
 	@Override
 	public void setHeimathafen(String heimathafen) {
 		boat.setHeimathafen(heimathafen);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getYachtclub() {
 		return boat.getYachtclub();
 	}
 
 	@Override
 	public void setYachtclub(String yachtclub) {
 		boat.setYachtclub(yachtclub);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getEigner() {
 		return boat.getEigner();
 	}
 
 	@Override
 	public void setEigner(String eigner) {
 		boat.setEigner(eigner);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getVersicherung() {
 		return boat.getVersicherung();
 	}
 
 	@Override
 	public void setVersicherung(String versicherung) {
 		boat.setVersicherung(versicherung);
 		notifyObservers();
 	}
 
 	@Override
 	public String getRufzeichen() {
 		return boat.getRufzeichen();
 	}
 
 	@Override
 	public void setRufzeichen(String rufzeichen) {
 		boat.setRufzeichen(rufzeichen);
 		notifyObservers();
 	}
 
 	@Override
 	public String getTyp() {
 		return boat.getTyp();
 	}
 
 	@Override
 	public void setTyp(String typ) {
 		boat.setTyp(typ);
 		notifyObservers();
 	}
 
 	@Override
 	public String getKonstrukteur() {
 		return boat.getKonstrukteur();
 	}
 
 	@Override
 	public void setKonstrukteur(String konstrukteur) {
 		boat.setKonstrukteur(konstrukteur);
 		notifyObservers();
 	}
 
 	@Override
 	public double getLaenge() {
 		return boat.getLaenge();
 	}
 
 	@Override
 	public void setLaenge(double laenge) {
 		boat.setLaenge(laenge);
 		notifyObservers();
 	}
 
 	@Override
 	public double getBreite() {
 		return boat.getBreite();
 	}
 
 	@Override
 	public void setBreite(double breite) {
 		boat.setBreite(breite);
 		notifyObservers();
 	}
 
 	@Override
 	public double getTiefgang() {
 		return boat.getTiefgang();
 	}
 
 	@Override
 	public void setTiefgang(double tiefgang) {
 		boat.setTiefgang(tiefgang);
 		notifyObservers();
 	}
 
 	@Override
 	public double getMasthoehe() {
 		return boat.getMasthoehe();
 	}
 
 	@Override
 	public void setMasthoehe(double masthoehe) {
 		boat.setMasthoehe(masthoehe);
 		notifyObservers();
 	}
 
 	@Override
 	public double getVerdraengung() {
 		return boat.getVerdraengung();
 	}
 
 	@Override
 	public void setVerdraengung(double verdraengung) {
 		boat.setVerdraengung(verdraengung);
 		notifyObservers();
 	}
 
 	@Override
 	public String getRiggArt() {
 		return boat.getRiggArt();
 	}
 
 	@Override
 	public void setRiggArt(String riggArt) {
 		boat.setRiggArt(riggArt);
 		notifyObservers();
 	}
 
 	@Override
 	public int getBaujahr() {
 		return boat.getBaujahr();
 	}
 
 	@Override
 	public void setBaujahr(int baujahr) {
 		boat.setBaujahr(baujahr);
 		notifyObservers();
 	}
 
 	@Override
 	public String getMotor() {
 		return boat.getMotor();
 	}
 
 	@Override
 	public void setMotor(String motor) {
 		boat.setMotor(motor);
 		notifyObservers();
 	}
 
 	@Override
 	public double getTankGroesse() {
 		return boat.getTankGroesse();
 	}
 
 	@Override
 	public void setTankGroesse(double tankGroesse) {
 		boat.setTankGroesse(tankGroesse);
 		notifyObservers();
 	}
 
 	@Override
 	public double getWassertankGroesse() {
 		return boat.getWassertankGroesse();
 	}
 
 	@Override
 	public void setWassertankGroesse(double wassertankGroesse) {
 		boat.setWassertankGroesse(wassertankGroesse);
 		notifyObservers();
 	}
 
 	@Override
 	public double getAbwassertankGroesse() {
 		return boat.getAbwassertankGroesse();
 	}
 
 	@Override
 	public void setAbwassertankGroesse(double abwassertankGroesse) {
 		boat.setAbwassertankGroesse(abwassertankGroesse);
 		notifyObservers();
 	}
 
 	@Override
 	public double getGrosssegelGroesse() {
 		return boat.getGrosssegelGroesse();
 	}
 
 	@Override
 	public void setGrosssegelGroesse(double grosssegelGroesse) {
 		boat.setGrosssegelGroesse(grosssegelGroesse);
 		notifyObservers();
 	}
 
 	@Override
 	public double getGenuaGroesse() {
 		return boat.getGenuaGroesse();
 	}
 
 	@Override
 	public void setGenuaGroesse(double genuaGroesse) {
 		boat.setGenuaGroesse(genuaGroesse);
 		notifyObservers();
 	}
 
 	@Override
 	public double getSpiGroesse() {
 		return boat.getSpiGroesse();
 	}
 
 	@Override
 	public void setSpiGroesse(double spiGroesse) {
 		boat.setSpiGroesse(spiGroesse);
 		notifyObservers();
 	}
 
 	@Override
 	public String getString() {
 		return "Bootsname() = " + getBootsname() + ", RegisterNr() = "
 				+ getRegisterNr() + ", Segelzeichen() = " + getSegelzeichen()
 				+ ", Heimathafen() = " + getHeimathafen() + ", Yachtclub() = "
 				+ getYachtclub() + ", Eigner() = " + getEigner()
 				+ ", Versicherung() = " + getVersicherung()
 				+ ", Rufzeichen() = " + getRufzeichen() + ", Typ() = "
 				+ getTyp() + ", Konstrukteur() = " + getKonstrukteur()
 				+ ", Laenge() = " + getLaenge() + ", Breite() = " + getBreite()
 				+ ", Tiefgang() = " + getTiefgang() + ", Masthoehe() = "
 				+ getMasthoehe() + ", Verdraengung() = " + getVerdraengung()
 				+ ", RiggArt() = " + getRiggArt() + ", Baujahr() = "
 				+ getBaujahr() + ", Motor() = " + getMotor()
 				+ ", TankGroesse() = " + getTankGroesse()
 				+ ", WassertankGroesse() = " + getWassertankGroesse()
 				+ ", AbwassertankGroesse() = " + getAbwassertankGroesse()
 				+ ", GrosssegelGroesse() = " + getGrosssegelGroesse()
 				+ ", GenuaGroesse() = " + getGenuaGroesse()
 				+ ", SpiGroesse() = " + getSpiGroesse();
 	}
 
 	@Override
 	public String getId() {
 		return boat.getId();
 	}
 
 	@Override
 	public void setId(String id) {
 		boat.setId(id);
 		notifyObservers();
 	}
 
 	@Override
 	public Map<String, String> getBoats() {
 		Map<String, String> list = new HashMap<String, String>();
 		list.put(boat.getId(), boat.getBootsname());
 		return list;
 	}
 
 	@Override
 	public String getBootsnameById(String id) {
 		return boat.getBootsname();
 	}
 }
