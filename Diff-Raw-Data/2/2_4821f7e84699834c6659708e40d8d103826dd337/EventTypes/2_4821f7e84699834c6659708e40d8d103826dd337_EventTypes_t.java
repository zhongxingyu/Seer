 package net.worldoftomorrow.nala.ni;
 
 public enum EventTypes {
 	CRAFT("craft", Configuration.notifyNoCraft()),
 	BREW("brew", Configuration.notifyNoBrew()),
 	WEAR("wear", Configuration.notfiyNoWear()),
 	PICKUP("pick up", Configuration.notifyNoPickup()),
 	DROP("drop", Configuration.notifyNoDrop()),
	USE("use", Configuration.notifyNoUse()),
 	HOLD("hold", Configuration.notifyNoHold()),
 	SMELT("smelt", Configuration.notifyNoCook()),
 	COOK("cook", Configuration.notifyNoCook());
 	
 	private final String name;
 	private final boolean notify;
 	
 	EventTypes(String name, Boolean notify){
 		this.name = name;
 		this.notify = notify;
 	}
 	
 	public String getName(){
 		return this.name;
 	}
 	
 	public boolean doNotify(){
 		return this.notify;
 	}
 }
