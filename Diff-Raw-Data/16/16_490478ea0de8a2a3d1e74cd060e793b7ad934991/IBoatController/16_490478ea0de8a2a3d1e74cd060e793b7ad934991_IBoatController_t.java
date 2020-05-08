 package de.htwg.seapal.boat.controllers;
 
 import java.util.Map;
 import java.util.UUID;
 
 import de.htwg.seapal.boat.util.observer.IObservable;
 
 public interface IBoatController extends IObservable {
 
 	String getBoatName(UUID id);
 
 	void setBoatName(UUID id, String BoatName);
 
 	String getRegisterNr(UUID id);
 
 	void setRegisterNr(UUID id, String registerNr);
 
 	String getSailSign(UUID id);
 
 	void setSailSign(UUID id, String SailSign);
 
 	String getHomePort(UUID id);
 
 	void setHomePort(UUID id, String HomePort);
 
 	String getYachtclub(UUID id);
 
 	void setYachtclub(UUID id, String yachtclub);
 
 	String getOwner(UUID id);
 
 	void setOwner(UUID id, String Owner);
 
 	String getInsurance(UUID id);
 
 	void setInsurance(UUID id, String Insurance);
 
 	String getCallSign(UUID id);
 
 	void setCallSign(UUID id, String CallSign);
 
 	String getType(UUID id);
 
 	void setType(UUID id, String Type);
 
 	String getConstructor(UUID id);
 
 	void setConstructor(UUID id, String Constructor);
 
 	double getLength(UUID id);
 
 	void setLength(UUID id, double Length);
 
 	double getWidth(UUID id);
 
 	void setWidth(UUID id, double width);
 
 	double getDraft(UUID id);
 
 	void setDraft(UUID id, double draft);
 
 	double getMastHeight(UUID id);
 
 	void setMastHeight(UUID id, double mastHeight);
 
 	double getDisplacement(UUID id);
 
 	void setDisplacement(UUID id, double displacement);
 
 	String getRigging(UUID id);
 
 	void setRigging(UUID id, String rigging);
 
 	int getYearOfConstruction(UUID id);
 
 	void setYearOfConstruction(UUID id, int yearOfConstruction);
 
 	String getMotor(UUID id);
 
 	void setMotor(UUID id, String motor);
 
 	double getTankSize(UUID id);
 
 	void setTankSize(UUID id, double tankSize);
 
 	double getWasteWaterTankSize(UUID id);
 
 	void setWasteWaterTankSize(UUID id, double wasteWaterTankSize);
 
 	double getFreshWaterTankSize(UUID id);
 
 	void setFreshWaterTankSize(UUID id, double freshWaterTankSize);
 
 	double getMainSailSize(UUID id);
 
 	void setMainSailSize(UUID id, double mainSailSize);
 
 	double getGenuaSize(UUID id);
 
 	void setGenuaSize(UUID id, double genuaSize);
 
 	double getSpiSize(UUID id);
 
 	void setSpiSize(UUID id, double spiSize);
 
 	String getString(UUID id);
 
 	UUID newBoat();
 
 	void deleteBoat(UUID id);
 
 	void closeDB();
 
 	Map<String, String> getBoats();
 
 	String getBoatNameById(UUID id);
 
 	Map<String, String> getPersons();
 
	String getPersonNameById(String id);
 }
