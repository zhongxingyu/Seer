 package ohtu.radioaine.domain;
 
import javax.validation.constraints.NotNull;

 public class RadioMedicineFormObject {
 
 
    @NotNull(message = "Aktiivisuus ei saa olla tyhjä")
    private String strength;
    @NotNull(message = "Tilavuus ei saa olla tyhjä")
     private int volume;
    @NotNull(message = "Päiväys ei saa olla tyhjä")
     private String date;
    @NotNull(message = "Tunnit ei saa olla tyhjä")
     private String hours;
    @NotNull(message = "Eränumero ei saa olla tyhjä")
     private String minutes;
    @NotNull(message = "Nimi ei saa olla tyhjä")
     private String signature;
     private String note;
     private int storageLocation;
     private String name;
     private String preparer;
     private int[] eluates = new int[5];
     private int[] others = new int[5];
     private int[] kits = new int[5];
 
     public int[] getEluates() {
         return eluates;
     }
 
     public void setEluates(int[] eluates) {
         this.eluates = eluates;
     }
 
     public int[] getKits() {
         return kits;
     }
 
     public void setKits(int[] kits) {
         this.kits = kits;
     }
 
     public int[] getOthers() {
         return others;
     }
 
     public void setOthers(int[] others) {
         this.others = others;
     }
 
     public String getNote() {
         return note;
     }
 
     public void setNote(String note) {
         this.note = note;
     }
 
     public String getSignature() {
         return signature;
     }
 
     public void setSignature(String signature) {
         this.signature = signature;
     }
 
     public int getStorageLocation() {
         return storageLocation;
     }
 
     public void setStorageLocation(int storageLocation) {
         this.storageLocation = storageLocation;
     }
 
    public String getStrength() {
         return strength;
     }
 
    public void setStrength(String strength) {
        this.strength = strength.replaceAll(",", ".");
     }
 
     public String getDate() {
         return date;
     }
 
     public void setDate(String date) {
         this.date = date;
     }
 
     public String getHours() {
         return hours;
     }
 
     public void setHours(String hours) {
         this.hours = hours;
     }
 
     public String getMinutes() {
         return minutes;
     }
 
     public void setMinutes(String minutes) {
         this.minutes = minutes;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getPreparer() {
         return preparer;
     }
 
     public void setPreparer(String preparer) {
         this.preparer = preparer;
     }
 
     public int getVolume() {
         return volume;
     }
 
     public void setVolume(int volume) {
         this.volume = volume;
     }
 }
