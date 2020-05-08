 package de.dhbw.muehle.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.dhbw.muehle.gui.ViewController;
 import de.dhbw.muehle.model.Log;
 import de.dhbw.muehle.model.Model;
 import de.dhbw.muehle.model.spielstein.EPositionIndex;
 import de.dhbw.muehle.model.spielstein.ESpielsteinFarbe;
 import de.dhbw.muehle.model.spielstein.Position;
 import de.dhbw.muehle.model.spielstein.Spielstein;
 
 public class Core {
 
 	private ViewController vController;
 	private Model model;
 	private List<Integer> Hashliste_Weiss;
 	private List<Integer> Hashliste_Schwarz;
 	private List<Spielstein> StW, StS;
 	private boolean schwarzDran;
 	private boolean weissDran;
 	private boolean Muehle_weiss;
 	private boolean Muehle_schwarz;
 
 
 	public boolean isMuehle_weiss() {
 		return Muehle_weiss;
 	}
 
 	public void setMuehle_weiss(boolean muehle_weiss) {
 		Muehle_weiss = muehle_weiss;
 	}
 
 	public boolean isMuehle_schwarz() {
 		return Muehle_schwarz;
 	}
 
 	public void setMuehle_schwarz(boolean muehle_schwarz) {
 		Muehle_schwarz = muehle_schwarz;
 	}
 
 	public boolean isSchwarzDran() {
 		return schwarzDran;
 	}
 
 	public void setSchwarzDran(boolean schwarzDran) {
 		this.schwarzDran = schwarzDran;
 	}
 
 	public boolean isWeissDran() {
 		return weissDran;
 	}
 
 	public void setWeissDran(boolean weissDran) {
 		this.weissDran = weissDran;
 	}
 
 	public Core() {
 		vController = new ViewController(this);
 		model = new Model();
 		StW = new ArrayList<Spielstein>();
 		StS = new ArrayList<Spielstein>();
 		Hashliste_Weiss = new ArrayList<Integer>();
 		Hashliste_Schwarz = new ArrayList<Integer>();
 		weissDran = true;
 
 	}
 
 	public List<Integer> getHashliste_Weiss() {
 		return Hashliste_Weiss;
 	}
 
 	public void setHashliste_Weiss(List<Integer> hashliste_Weiss) {
 		Hashliste_Weiss = hashliste_Weiss;
 	}
 
 	public List<Integer> getHashliste_Schwarz() {
 		return Hashliste_Schwarz;
 	}
 
 	public void setHashliste_Schwarz(List<Integer> hashliste_Schwarz) {
 		Hashliste_Schwarz = hashliste_Schwarz;
 	}
 
 	public void initGame() {
 		vController.initGui();
 	}
 
 	public List<Spielstein> getStW() {
 		return StW;
 	}
 
 	public void setStW(List<Spielstein> stW) {
 		StW = stW;
 	}
 
 	public List<Spielstein> getStS() {
 		return StS;
 	}
 
 	public void setStS(List<Spielstein> stS) {
 		StS = stS;
 	}
 
 	private void run() {
 		Log.log("run() ohne Fehler gestartet", getClass().getSimpleName());
 	}
 
 	public void erzeugeSpielsteinweiss(EPositionIndex ebene, EPositionIndex x,
 			EPositionIndex y, Position pos) {
 
 		StW.add(new Spielstein(ebene, x, y, ESpielsteinFarbe.WEISS));
 		Hashliste_Weiss.add(pos.hashCode());
 
 	}
 
 	public void erzeugeSpielsteinschwarz(EPositionIndex ebene,
 			EPositionIndex x, EPositionIndex y, Position pos) {
 
 		StS.add(new Spielstein(ebene, x, y, ESpielsteinFarbe.SCHWARZ));
 		Hashliste_Schwarz.add(pos.hashCode());
 
 	}
 
 	public boolean postitionFree(Position pos) {
 		boolean posfree = true;
 		if (Hashliste_Weiss.contains(pos.hashCode())) {
 			posfree = false;
 		}
 		
 		else if (Hashliste_Schwarz.contains(pos.hashCode())){
 			posfree=false;
 		}
 		
 		return posfree;
 
 	}
 
 	public void start_Spielphase1() {
 
 	}
 
 	public void ueberpruefen_Muehele_weiss(Position pos) {
 		Muehle_weiss=false;
 		int zaehler1 = 0;
 		int zaehler2 = 0;
 		int zaehler3 = 0;
 
 		int posEbene = pos.getEbene().getValue();
 		int posX = pos.getX().getValue();
 		int posY = pos.getY().getValue();
 
 		for (int i = 0; i < StW.size(); i++) {
 			if (posEbene == StW.get(i).getPosition().getEbene().getValue()
 					&& posX == StW.get(i).getPosition().getX().getValue()) {
 				zaehler1++;
 			}
 			if (posEbene == StW.get(i).getPosition().getEbene().getValue()
 					&& posY == StW.get(i).getPosition().getY().getValue()) {
 				zaehler2++;
 			}
 			if (posX == StW.get(i).getPosition().getX().getValue()
					&& posY == StW.get(i).getPosition().getY().getValue()) {
 				zaehler3++;
 			}
 
 		}
 
 		if (zaehler1 == 3 || zaehler2 == 3 || zaehler3 == 3) {
 			System.out.println("Muehle weiss");
 			Muehle_weiss=true;
 		}
 
 	}
 
 	public void ueberpruefen_Muehele_schwarz(Position pos) {
 		Muehle_schwarz=false;
 		int zaehler1 = 0;
 		int zaehler2 = 0;
 		int zaehler3 = 0;
 
 		int posEbene = pos.getEbene().getValue();
 		int posX = pos.getX().getValue();
 		int posY = pos.getY().getValue();
 
 		for (int i = 0; i < StS.size(); i++) {
 			if (posEbene == StS.get(i).getPosition().getEbene().getValue()
 					&& posX == StS.get(i).getPosition().getX().getValue()) {
 				zaehler1++;
 			}
 			if (posEbene == StS.get(i).getPosition().getEbene().getValue()
 					&& posY == StS.get(i).getPosition().getY().getValue()) {
				zaehler2++;
 			}
 			if (posX == StS.get(i).getPosition().getX().getValue()
					&& posY == StS.get(i).getPosition().getY().getValue()) {
 				zaehler3++;
 			}
 
 		}
 
 		if (zaehler1 == 3 || zaehler2 == 3 || zaehler3 == 3) {
 			System.out.println("Muehle schwarz");
 			Muehle_schwarz=true;
 		}
 
 	}
 
 }
