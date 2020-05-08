 /**
  * 
  * darf Beruehrungssensitiven oder hochfesten Skin verwenden
  * 
  * darf Software mit Level 3 oder 4 verwenden
  * 
  * darf Kits mit <= 5KW (wenn Level = 3) oder <= 10KW verwenden (wenn Level = 4)
  * 
  * @author Thomas
  *
  */
 public abstract class Schwerarbeiter extends Android{
 
 	/**
 	 * default constructor
 	 * @param seriennummer serial of the android
 	 */
 	public Schwerarbeiter(String seriennummer) {
 		super(seriennummer);
 	}
 
 	protected void calledFromHochfester(HochfesterSkin s) {
 		super.installieren(s);
 	}
 
 	protected void calledFromBeruehrungs(BeruehrungssensitiverSkin s) {
 		super.installieren(s);
 	}
 	
 	protected void calledFromGepanzert(GepanzerterSkin s) {}
 	
	// Schwerarbeiter koennen mit Software der Stufen 3 und 4 ausgestattet sein
 	protected void calledFromSoftwareLevel3(Software s) {
 		super.installieren(s);
 	}
 	
 	protected void calledFromSoftwareLevel4(Software s) {
 		super.installieren(s);
 	}
 	
 	protected void calledFromGesellschaftlerSoftware(Software s) {}
 	protected void calledFromHilfskraefteSoftware(Software s) {}
 	protected void calledFromKaempferSoftware(Software s) {}
 	protected void calledFromLeibwaechterSoftware(Software s) {}
 	protected void calledFromObjektbewacherSoftware(Software s) {}
 	
 	protected void calledFromSoftwareLevel1(Software s) {}
 	protected void calledFromSoftwareLevel2(Software s) {}
 	protected void calledFromSoftwareLevel5(Software s) {}
 	
 	protected void calledFromLeistungUnbegrenzt(SensorenAktorenKit s) {}
 	
 	protected void calledFromLeistungFuenf(SensorenAktorenKit s) {
 		if(this.getSoftware() != null) {
 			this.getSoftware().getSecurityLevel().calledWithLeistungFuenf(this, s);
 		}
 	}
 	
 	protected void calledFromLeistungZehn(SensorenAktorenKit s) {
 		if(this.getSoftware() != null) {
 			this.getSoftware().getSecurityLevel().calledWithLeistungZehn(this, s);
 		}
 	}
 	
 	protected void calledForLeistungFuenfWithLevel3(SensorenAktorenKit s) {
 		this.installieren(s);
 	}
 	
 	protected void calledForLeistungFuenfWithLevel4(SensorenAktorenKit s) {
 		this.installieren(s);
 	}
 	
 	protected void calledForLeistungZehnWithLevel4(SensorenAktorenKit s) {
 		this.installieren(s);
 	}
 }
