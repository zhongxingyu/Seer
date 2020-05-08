 package applikation.server.zustaende;
 
 import applikation.nachrichten.Aufgabe;
 import applikation.nachrichten.ZugInformation;
 import applikation.server.Spieler;
 import applikation.server.Spielerschaft;
 import dienste.automat.zustaende.Zustand;
 import dienste.netzwerk.EndPunkt;
 
 /**
  * Zustand während des Spieles. Eingende ZugInformationen werden an
  * alle Spieler verteilt.
  */
 public class WarteAufZug extends AktiverServerZustand {
 
 	Zustand zugInfo(EndPunkt absender, ZugInformation zugInfo) {
 		Spielerschaft spielers = automat.spielerschaft;
 
		spielers.sicherStellenIstAktuellerSpieler(absender);
 
 		spielers.broadcast(zugInfo);
 		System.out.println("Ausgeführter Zug: " + zugInfo.zug);
 
 		return automat.getZustand(VersendeZug.class);
 	}
 
 	Zustand aufgabe(EndPunkt absender, Aufgabe aufgabe) {
 		Spielerschaft spielers = automat.spielerschaft;
 		Spieler aktuellerSpieler = spielers.getAktuellerSpieler();
 
 		spielers.sicherStellenIstAktuellerSpieler(absender);
 
 		automat.spielerschaft.runde.entferneSpieler(aktuellerSpieler);
 
 		return automat.getZustand(VersendeZug.class);
 	}
 }
