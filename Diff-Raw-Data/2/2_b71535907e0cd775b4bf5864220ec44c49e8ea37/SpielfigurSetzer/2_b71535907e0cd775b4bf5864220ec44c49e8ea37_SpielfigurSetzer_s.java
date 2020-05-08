 package com.dhbw.dvst.utilities;
 
 import java.util.ArrayList;
 
 import com.dhbw.dvst.models.Ausrichtung;
 import com.dhbw.dvst.models.Spiel;
 import com.dhbw.dvst.models.Spieler;
 import com.dhbw.dvst.models.Spielplatte;
 
 public class SpielfigurSetzer {
 
 	private ArrayList<Spielplatte> alleSpielplatten;
 	private Spiel spiel;
 	private ArrayList<Spielplatte> erreichbarePlatten;
 
 	public SpielfigurSetzer() {
 	}
 
 	public void initSpielfigurSetzer() {
 		spiel = Spiel.getInstance();
 		alleSpielplatten = spiel.getSpielbrett().getAlleSpielplatten();
 	}
 	
 	public boolean figurKannGesetztWerden(Spielplatte zielPlatte, Spieler spieler) {		
 		Spielplatte startPlatte = spieler.getSpielfigur().getSpielplatte();
 		if(startPlatte.equals(zielPlatte)){
 			return true;
 		}
 		erreichbarePlatten = new ArrayList<Spielplatte>();
 		erreichbarePlatten.add(startPlatte);
 		
 		
 		for (int i = 0; i < erreichbarePlatten.size(); i++) {			
 			holeNachbarPlatten(erreichbarePlatten.get(i));
 			
 			if(erreichbarePlatten.contains(zielPlatte)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	protected void holeNachbarPlatten(Spielplatte zuPruefendePlatte) {
 		if(zuPruefendePlatte.isLinksOffen() && (getPlatteLinks(zuPruefendePlatte) != null)) {
 			erreichbarePlatteEinfuegen(getPlatteLinks(zuPruefendePlatte));				
 		}
 		if (zuPruefendePlatte.isObenOffen() && (getPlatteOben(zuPruefendePlatte) != null)) {
 			erreichbarePlatteEinfuegen(getPlatteOben(zuPruefendePlatte));
 		}
 		if (zuPruefendePlatte.isUntenOffen() && (getPlatteUnten(zuPruefendePlatte) != null)) {
 			erreichbarePlatteEinfuegen(getPlatteUnten(zuPruefendePlatte));
 		}
 		if (zuPruefendePlatte.isRechtsOffen() && (getPlatteRechts(zuPruefendePlatte) != null)) {
 			erreichbarePlatteEinfuegen(getPlatteRechts(zuPruefendePlatte));
 		}
 	}
 	
 	private void erreichbarePlatteEinfuegen(Spielplatte einzufuegendePlatte){
 		if(!erreichbarePlatten.contains(einzufuegendePlatte)){
 			erreichbarePlatten.add(einzufuegendePlatte);
 		}
 	}
 	
 	private Spielplatte getPlatteOben(Spielplatte next) {
 		int indexOfNext = alleSpielplatten.indexOf(next);
 		//erste Reihe
 		if(indexOfNext < 7) {
 			return null;
 		}
 		Spielplatte platteOben = alleSpielplatten.get(indexOfNext-7);
 		if(platteOben.isUntenOffen()) {
 			return platteOben;
 		}
 		return null;
 	}
 	
 	private Spielplatte getPlatteUnten(Spielplatte next) {
 		int indexOfNext = alleSpielplatten.indexOf(next);
 		//letzte Reihe
 		if(indexOfNext > 41) {
 			return null;
 		}
 		Spielplatte platteUnten = alleSpielplatten.get(indexOfNext+7);
 		if(platteUnten.isObenOffen()) {
 			return platteUnten;
 		}
 		return null;
 	}
 
 	private Spielplatte getPlatteRechts(Spielplatte next) {
 		int indexOfNext = alleSpielplatten.indexOf(next);
 		//rechte Reihe
		if(indexOfNext % 7 == 1) {
 			return null;
 		}
 		Spielplatte platteRechts = alleSpielplatten.get(indexOfNext+1);
 		if(platteRechts.isLinksOffen()) {
 			return platteRechts;
 		}
 		return null;
 	}
 	
 	private Spielplatte getPlatteLinks(Spielplatte next) {
 		int indexOfNext = alleSpielplatten.indexOf(next);
 		//linke Reihe
 		if(indexOfNext % 7 == 0) {
 			return null;
 		}
 		Spielplatte platteLinks =  alleSpielplatten.get(indexOfNext-1);
 		if(platteLinks.isRechtsOffen()) {
 			return platteLinks;
 		}
 		return null;
 	}
 	
 	public void figurSetzen(Spielplatte zielPlatte, Spieler spieler) {
 		spieler.getSpielfigur().getSpielplatte().setFigur(null);
 		spieler.getSpielfigur().setSpielplatte(zielPlatte);		
 		zielPlatte.setFigur(spieler.getSpielfigur());
 	}
 	
 
 }
