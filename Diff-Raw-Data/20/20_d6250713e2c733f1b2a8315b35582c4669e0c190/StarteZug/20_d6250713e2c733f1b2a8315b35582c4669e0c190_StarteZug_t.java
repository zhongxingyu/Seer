 package applikation.server.zustaende;
 
 import applikation.nachrichten.ZugAufforderung;
 import applikation.server.pd.Spieler;
 import applikation.server.pd.Spiel;
 import dienste.automat.zustaende.PassiverZustand;
 import dienste.automat.zustaende.Zustand;
 
 public class StarteZug extends ServerZustand implements PassiverZustand {
 	public Class<? extends Zustand> handle() {
 		Spiel spielers = spiel;
 
 		spielers.runde.rotiereSpieler();
 		Spieler naechsterSpieler = spielers.runde.getAktuellerSpieler();
 		spielers.broadcast("Nächster Spieler ist " + naechsterSpieler + ".");
 
		ZugAufforderung aufforderung = new ZugAufforderung(naechsterSpieler.spieler);
 
 		spielers.broadcast(aufforderung);
 
 		return WarteAufZug.class;
 	}
 }
