 package br.usp.ime.ccsl.proxy.roles;
 
 import br.usp.ime.ccsl.proxy.choreography.EnactementWithoutWS;
 
 public class AirportTechnician extends AirportCrew{
 
 	public int airplaneUnderInspectionId;
 	public boolean injured = false;
 	
 	public AirportTechnician(int newTechnicianId) {
 		this.crewId = newTechnicianId;
 		this.crewType = TECHNICIAN;
 		this.setURL();
 	}
 	
 	/*
 	 * A technician inspects airplanes at the airport
 	 */
 
 	public void inspectAirplane(int airplaneId) {
 		
		System.out.println("TECHNICsuperIAN: Tech crew is on its way!");
 		waitRandomTimeBeforeEvent();
 		
 		this.reportArrival(TECHNICIAN, crewId, airplaneId);
 		this.airplaneUnderInspectionId = airplaneId;
 		this.logActions();
 	}		
 	
 	
 	private void logActions() {
 		waitRandomTimeBeforeEvent();
 		this.identifySlip();
 		return;
 		
 	}
 
 	private void identifySlip() {
 		System.out.println("TECHNICIAN: WOOPS!!! Slippery!");
 		this.reportToCentral(SLIP, airplaneUnderInspectionId);
 	}
 
 	private void reportToCentral(int incidentCode, int airplaneId) {
 		EnactementWithoutWS.central.dealWithTechnicianSlip(airplaneId,crewId);
 		
 	}
 	
 }
