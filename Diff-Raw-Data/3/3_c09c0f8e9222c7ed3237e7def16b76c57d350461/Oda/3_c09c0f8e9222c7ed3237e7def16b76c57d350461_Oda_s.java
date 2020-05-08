 package UnitTypes;
 
 import DatabasePackage.Facade;
 
 public class Oda {
 	private int ID;
 	private int odaID;
 	private boolean uygunluk;
 	private boolean bosOlma;
 	private int odaKataloguID;
 	
 	public Oda(int _odaID) {
 		this.odaID = _odaID;
 		this.uygunluk = true;
 		this.bosOlma = true;
 	}
 	
 	public Oda() {
 		this.uygunluk = true;
 		this.bosOlma = true;
 	}
 
 	public void setID(int ID){
 		this.ID = ID;
 	}
 	
 	public int getID(){
 		return this.ID;
 	}
 	
 	public int odaIDGetir() {
 		return odaID;
 	}
 	public void odaIDAyarla(int odaID) {
 		this.odaID = odaID;
 	}
 	public boolean isAvailable() {
 		return uygunluk;
 	}
 	public void odaKataloguAyarla(int odaKataloguID){
 		this.odaKataloguID = odaKataloguID;
 	}
 	
 	public boolean uygunMu(Musteri musteri) {
 		if (!this.isAvailable()){
 			return false;
 		}
 		OdaKatalogu katalog = (OdaKatalogu) Facade.getInstance().get(this.odaKataloguID, OdaKatalogu.class);
 		return katalog.uygunMu(musteri);
 	}
 	public void uygunlukAyarla(boolean uygunluk) {
 		this.uygunluk = uygunluk;
 	}
 	public boolean bosMu() {
 		return bosOlma;
 	}
 	public void bosOlmaDurumuAyarla(boolean bosOlmaDurumu) {
 		this.bosOlma = bosOlmaDurumu;
 	}
 
 	public void odaOzellikleriBelirt(int odaNo) {
 		odaIDAyarla(odaNo);
 		
 	}
 
 	public void katalogSec(int katalogID) {
 		this.odaKataloguID = katalogID;
 		
 	}
 
 	public float tutarOgren() {
		return this.odaKatalogu.tutarGetir();
 	}
 
 }
