 
 /**
  * Security Level 4 darf fuer die Software aller Schwerabeiter verwendet werden
  * 
  * Ausserdem duerfen alle Beschuetzer auesser Kaempfer Level 4 Software verwenden
  * 
  * 
  * Mit Level 4 Software darf man sowohl 5 als auch 10 KW Aktoren verwenden
  * @author Thomas
  *
  */
 public class SecurityLevel4 extends SecurityLevel {
 	protected void vonHilfskraftVerwendetFuerSoftware(Hilfskraft android, Software s) { }
 	protected void vonGesellschaftlerVerwendetFuerSoftware(Gesellschaftler android, Software s) { }
 	protected void vonKaempferVerwendetFuerSoftware(Kaempfer android, Software s) { }
 
 	protected void vonSchwerarbeiterVerwendetFuerSoftware(Schwerarbeiter android, Software s) {
 		android.calledFromSoftwareLevel4(s);
 	}
 
 	protected void vonObjektbewacherVerwendetFuerSoftware(Objektbewacher android, Software s) {
 		android.calledFromSoftwareLevel4(s);
 	}
 
 	protected void vonLeibwaechterVerwendetFuerSoftware(Leibwaechter android, Software s) {
 		android.calledFromSoftwareLevel4(s);
 	}
 
 	protected void calledWithLeistungFuenf(Android android, SensorenAktorenKit s) {
 		android.calledForLeistungFuenfWithLevel4(s);
 	}
 
 	protected void calledWithLeistungZehn(Android android, SensorenAktorenKit s) {
 		android.calledForLeistungZehnWithLevel4(s);
 	}
 	
 	public String toString() {
		return "Security Level 4";
 	}
 }
