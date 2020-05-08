 package Automat;
 
 import java.util.ArrayList;
 import Fassade.Fassade;
 
 /**
  * Performante und thread-safe Implementierung des Singleton-Patterns
  */
 public class HWSimulation {
 	private static HWSimulation instance = new HWSimulation();
 	private ArrayList<Comparable> ea_area;
 	private Fassade dieFassade;
 
 	/**
 	 * Default-Konstruktor, der nicht au�erhalb dieser Klasse
 	 * aufgerufen werden kann
 	 */
 	private HWSimulation() {
 		ea_area = new ArrayList<Comparable>();
 		ea_area.add(new Boolean(false)); // 0 Troete
 		ea_area.add(new Boolean(true));  // 1 Leuchte
 		ea_area.add(new Integer(0)); 	 // 2 Leuchte -Farbe
 		ea_area.add(new Boolean(false)); // 3 AuswahlklappeEingangslichtschranke
 		ea_area.add(new Boolean(false)); // 4 Eingangslichtschranke
 		ea_area.add(new Boolean(false)); // 5 Justierlichtschranke
 		ea_area.add(new Boolean(false)); // 6 Ausgangslichtschranke
 		ea_area.add(new Boolean(false)); // 7 UebergabelichtschrankePET
 		ea_area.add(new Boolean(false)); // 8 UebergabelichtschrankeMehrweg
 		ea_area.add(new Integer(0)); // 9 LaufbandEingang
 		ea_area.add(new Integer(0)); // 10 LaufbandDrehen
 		ea_area.add(new Integer(0)); // 11 LaufbandAusgang
 		ea_area.add(new Boolean(false)); // 12 Auswahlklappe
 		ea_area.add(FlaschenType.CodeNichtValide); 	 // 13 Scanner
 		ea_area.add(new String("")); // 14 BonDrucker
 		ea_area.add(new Boolean(false)); // 15 Druckknopf
 		ea_area.add(new String("init")); // 16 Display
 		ea_area.add(new String("init")); // 17 Drucker
 		ea_area.add(new Boolean(false));
 		ea_area.add(new Boolean(false));
 	}
 
 	/**
 	 * Statische Methode, liefert die einzige Instanz dieser
 	 * Klasse zur�ck
 	 */
 	public static HWSimulation getInstance() {
 		return instance;
 	}
 	
 	public Boolean readBool(int adresse){
 		Boolean status = (Boolean) ea_area.get(adresse);
 		//dieFassade.simKonsolenText(0, "[read:"+Adressen.values()[adresse].toString()+"] "+status);
 		tellFassade(Adressen.values()[adresse]);
 		return status;		
 	}
 	
 	public Integer readInt(int adresse){
 		Integer number = (Integer) ea_area.get(adresse);
 		//dieFassade.simKonsolenText(0, "[read:"+Adressen.values()[adresse].toString()+"] "+number);
 		tellFassade(Adressen.values()[adresse]);
 		return number;		
 	}
 	
 	public String readStr(int adresse){
 		String text = (String) ea_area.get(adresse);
 		//dieFassade.simKonsolenText(0, "[read:"+Adressen.values()[adresse].toString()+"] \""+text+"\"");
 		tellFassade(Adressen.values()[adresse]);
 		return text;
 	}
 	
 	public FlaschenType readFlaschenType(int adresse)
 	{
 		FlaschenType fType = (FlaschenType) ea_area.get(adresse);
 		dieFassade.simKonsolenText(0, "[read:"+Adressen.values()[adresse].toString()+"] "+fType.toString());
 		tellFassade(Adressen.values()[adresse]);
 		return fType;		
 	}
 	
 	public void write(int adresse, Boolean status){
 		ea_area.set(adresse, status);
 		dieFassade.aktuallisereHW(ea_area);
 		//dieFassade.simKonsolenText(0, "[write:"+ Adressen.values()[adresse].toString() +"] "+status);
		tellFassade(Adressen.values()[adresse]);
 	}
 	
 	public void write(int adresse, Integer number){
 		ea_area.set(adresse, number);
 		dieFassade.aktuallisereHW(ea_area);
 		//dieFassade.simKonsolenText(0, "[write:"+Adressen.values()[adresse].toString()+"] "+number);
 		tellFassade(Adressen.values()[adresse]);
 	}
 	
 	public void write(int adresse, String text){
 		ea_area.set(adresse, text);
 		dieFassade.aktuallisereHW(ea_area);
 		//dieFassade.simKonsolenText(0, "[write:"+Adressen.values()[adresse].toString()+"] \""+text+"\"");
 		tellFassade(Adressen.values()[adresse]);
 	}
 
 	public void write(int adresse, FlaschenType fType){
 		ea_area.set(adresse, fType);
 		dieFassade.aktuallisereHW(ea_area);
 		dieFassade.simKonsolenText(0, "[write:"+ Adressen.values()[adresse].toString() +"] "+fType.toString());
 		tellFassade(Adressen.values()[adresse]);
 	}
 	
 	public void setF(Fassade dieFassade){
 		this.dieFassade = dieFassade;
 		this.dieFassade.aktuallisereHW(ea_area);
 	}
 	
 	public void tellFassade(Adressen thisChanged){
 		this.dieFassade.highlightEA(thisChanged);
 	}
 
 	
 
 }
