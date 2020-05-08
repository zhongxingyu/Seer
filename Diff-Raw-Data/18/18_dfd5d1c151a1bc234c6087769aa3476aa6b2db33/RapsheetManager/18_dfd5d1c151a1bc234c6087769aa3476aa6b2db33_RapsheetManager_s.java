 package com.nuclearw.rapsheet.api;
 
 import java.util.List;
 
 import com.nuclearw.rapsheet.Record;
 
 public interface RapsheetManager {
 	public List<Record> getCharges(String playerName);
 
 	public Record getCharge(String playerName, int chargeId);
 
	public int chargePlayer(String offenderName, String officialName, String shortDesciption, String longDescription, NotifyChanges notify);
 
 	public boolean convictPlayer(String offenderName, String officialName, int chargeId, NotifyChanges notify);
 
 	public boolean pardonPlayer(String offenderName, String officialName, int chargeId, NotifyChanges notify);
 
 	public boolean sealPlayerCharge(String offenderName, String officialName, int chargeId, NotifyChanges notify);
 
 	public boolean unsealPlayerCharge(String offenderName, String officialName, int chargeId, NotifyChanges notify);
 
 	public boolean expungePlayerCharge(String offenderName, String officialName, int chargeId, NotifyChanges notify);
 }
