 package de.htwg.seapal.boat.controllers.mock;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import com.google.inject.Inject;
 
 import de.htwg.seapal.boat.controllers.IBoatController;
 import de.htwg.seapal.boat.models.IBoat;
 import de.htwg.seapal.boat.util.observer.Observable;
 import de.htwg.seapal.person.controllers.IPersonController;
 
 public class BoatController extends Observable implements IBoatController {
 
 	protected IBoat boat;
 	protected IPersonController personContoller;
 
 	@Inject
 	public BoatController(IBoat boat, IPersonController personController) {
 		this.personContoller = personController;
 		this.boat = boat;
 	}
 
 	@Override
 	public String getBoatName(UUID id) {
 		return boat.getBoatName();
 	}
 
 	@Override
 	public void setBoatName(UUID id, String BoatName) {
 		boat.setBoatName(BoatName);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getRegisterNr(UUID id) {
 		return boat.getRegisterNr();
 	}
 
 	@Override
 	public void setRegisterNr(UUID id, String registerNr) {
 		boat.setRegisterNr(registerNr);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getSailSign(UUID id) {
 		return boat.getSailSign();
 	}
 
 	@Override
 	public void setSailSign(UUID id, String SailSign) {
 		boat.setSailSign(SailSign);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getHomePort(UUID id) {
 		return boat.getHomePort();
 	}
 
 	@Override
 	public void setHomePort(UUID id, String HomePort) {
 		boat.setHomePort(HomePort);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getYachtclub(UUID id) {
 		return boat.getYachtclub();
 	}
 
 	@Override
 	public void setYachtclub(UUID id, String yachtclub) {
 		boat.setYachtclub(yachtclub);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getOwner(UUID id) {
 		return boat.getOwner();
 	}
 
 	@Override
 	public void setOwner(UUID id, String Owner) {
 		boat.setOwner(Owner);
 		notifyObservers();
 
 	}
 
 	@Override
 	public String getInsurance(UUID id) {
 		return boat.getInsurance();
 	}
 
 	@Override
 	public void setInsurance(UUID id, String Insurance) {
 		boat.setInsurance(Insurance);
 		notifyObservers();
 	}
 
 	@Override
 	public String getCallSign(UUID id) {
 		return boat.getCallSign();
 	}
 
 	@Override
 	public void setCallSign(UUID id, String CallSign) {
 		boat.setCallSign(CallSign);
 		notifyObservers();
 	}
 
 	@Override
 	public String getType(UUID id) {
 		return boat.getType();
 	}
 
 	@Override
 	public void setType(UUID id, String Type) {
 		boat.setType(Type);
 		notifyObservers();
 	}
 
 	@Override
 	public String getConstructor(UUID id) {
 		return boat.getConstructor();
 	}
 
 	@Override
 	public void setConstructor(UUID id, String Constructor) {
 		boat.setConstructor(Constructor);
 		notifyObservers();
 	}
 
 	@Override
 	public double getLength(UUID id) {
 		return boat.getLength();
 	}
 
 	@Override
 	public void setLength(UUID id, double Length) {
 		boat.setLength(Length);
 		notifyObservers();
 	}
 
 	@Override
 	public double getWidth(UUID id) {
 		return boat.getWidth();
 	}
 
 	@Override
 	public void setWidth(UUID id, double width) {
 		boat.setWidth(width);
 		notifyObservers();
 	}
 
 	@Override
 	public double getDraft(UUID id) {
 		return boat.getDraft();
 	}
 
 	@Override
 	public void setDraft(UUID id, double draft) {
 		boat.setDraft(draft);
 		notifyObservers();
 	}
 
 	@Override
 	public double getMastHeight(UUID id) {
 		return boat.getMastHeight();
 	}
 
 	@Override
 	public void setMastHeight(UUID id, double mastHeight) {
 		boat.setMastHeight(mastHeight);
 		notifyObservers();
 	}
 
 	@Override
 	public double getDisplacement(UUID id) {
 		return boat.getDisplacement();
 	}
 
 	@Override
 	public void setDisplacement(UUID id, double displacement) {
 		boat.setDisplacement(displacement);
 		notifyObservers();
 	}
 
 	@Override
 	public String getRigging(UUID id) {
 		return boat.getRigging();
 	}
 
 	@Override
 	public void setRigging(UUID id, String rigging) {
 		boat.setRigging(rigging);
 		notifyObservers();
 	}
 
 	@Override
 	public int getYearOfConstruction(UUID id) {
 		return boat.getYearOfConstruction();
 	}
 
 	@Override
 	public void setYearOfConstruction(UUID id, int yearOfConstruction) {
 		boat.setYearOfConstruction(yearOfConstruction);
 		notifyObservers();
 	}
 
 	@Override
 	public String getMotor(UUID id) {
 		return boat.getMotor();
 	}
 
 	@Override
 	public void setMotor(UUID id, String motor) {
 		boat.setMotor(motor);
 		notifyObservers();
 	}
 
 	@Override
 	public double getTankSize(UUID id) {
 		return boat.getTankSize();
 	}
 
 	@Override
 	public void setTankSize(UUID id, double tankSize) {
 		boat.setTankSize(tankSize);
 		notifyObservers();
 	}
 
 	@Override
 	public double getWasteWaterTankSize(UUID id) {
 		return boat.getWasteWaterTankSize();
 	}
 
 	@Override
 	public void setWasteWaterTankSize(UUID id, double wasteWaterTankSize) {
 		boat.setWasteWaterTankSize(wasteWaterTankSize);
 		notifyObservers();
 	}
 
 	@Override
 	public double getFreshWaterTankSize(UUID id) {
 		return boat.getFreshWaterTankSize();
 	}
 
 	@Override
 	public void setFreshWaterTankSize(UUID id, double freshWaterTankSize) {
 		boat.setFreshWaterTankSize(freshWaterTankSize);
 		notifyObservers();
 	}
 
 	@Override
 	public double getMainSailSize(UUID id) {
 		return boat.getMainSailSize();
 	}
 
 	@Override
 	public void setMainSailSize(UUID id, double mainSailSize) {
 		boat.setMainSailSize(mainSailSize);
 		notifyObservers();
 	}
 
 	@Override
 	public double getGenuaSize(UUID id) {
 		return boat.getGenuaSize();
 	}
 
 	@Override
 	public void setGenuaSize(UUID id, double genuaSize) {
 		boat.setGenuaSize(genuaSize);
 		notifyObservers();
 	}
 
 	@Override
 	public double getSpiSize(UUID id) {
 		return boat.getSpiSize();
 	}
 
 	@Override
 	public void setSpiSize(UUID id, double spiSize) {
 		boat.setSpiSize(spiSize);
 		notifyObservers();
 	}
 
 	@Override
 	public String getString(UUID id) {
 		return "BoatName = " + getBoatName(id) + ", ID = " + id + ", RegisterNr = "
 				+ getRegisterNr(id) + ", SailSign = " + getSailSign(id)
 				+ ", HomePort = " + getHomePort(id) + ", Yachtclub = "
 				+ getYachtclub(id) + ", Owner = " + getOwner(id)
 				+ ", Insurance = " + getInsurance(id) + ", CallSign = "
 				+ getCallSign(id) + ", Type = " + getType(id)
 				+ ", Constructor = " + getConstructor(id) + ", Length = "
 				+ getLength(id) + ", Width = " + getWidth(id)
 				+ ", Draft = " + getDraft(id) + ", MastHeight = "
 				+ getMastHeight(id) + ", Displacement = " + getDisplacement(id)
 				+ ", Rigging = " + getRigging(id) + ", YearOfConstruction = "
 				+ getYearOfConstruction(id) + ", Motor = " + getMotor(id)
 				+ ", TankSize = " + getTankSize(id)
 				+ ", WasteWaterTankSize = " + getWasteWaterTankSize(id)
 				+ ", FreshWaterTankSize = " + getFreshWaterTankSize(id)
 				+ ", MainSailSize = " + getMainSailSize(id)
 				+ ", GenuaSize = " + getGenuaSize(id) + ", SpiSize = "
 				+ getSpiSize(id);
 	}
 
 	@Override
 	public Map<String, String> getPersons() {
 		return personContoller.getPersonList();
 	}
 
 	@Override
	public String getPersonBoatNameById(String id) {
 		return personContoller.getPersonLastname(id) + ", "
 				+ personContoller.getPersonFirstname(id);
 	}
 
 	@Override
 	public Map<String, String> getBoats() {
 		Map<String, String> list = new HashMap<String, String>();
 		list.put(boat.getId().toString(), boat.getBoatName());
 		return list;
 	}
 
 	@Override
 	public String getBoatNameById(UUID id) {
 		return boat.getBoatName();
 	}
 
 	@Override
 	public UUID newBoat() {
 		return null;
 	}
 
 	@Override
 	public void closeDB() {
 	}
 
 	@Override
 	public void deleteBoat(UUID id) {
 	}
 
 }
