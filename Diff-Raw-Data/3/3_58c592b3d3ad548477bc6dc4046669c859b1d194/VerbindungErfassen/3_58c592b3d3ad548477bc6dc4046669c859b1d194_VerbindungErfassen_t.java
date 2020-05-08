 /*
  * Copyright (C) 2007  Danilo Couto, Philippe Eberli,
  *                     Pascal Hobus, Reto Schüttel, Robin Stocker
  *
  * This file is part of Bodesuri.
  *
  * Bodesuri is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 2 as
  * published by the Free Software Foundation.
  *
  * Bodesuri is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Bodesuri; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 
 package applikation.client.zustaende;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import applikation.client.events.VerbindeEvent;
 import applikation.nachrichten.SpielBeitreten;
 import dienste.automat.zustaende.Zustand;
 import dienste.netzwerk.BriefKastenInterface;
 import dienste.netzwerk.ClientEndPunkt;
 import dienste.netzwerk.server.BriefkastenAdapter;
 
 /**
  * Zustand wenn der Spieler die Verbindungsdaten eingeben muss. Wenn ein
  * {@link VerbindeEvent} eintrifft, wird der Zustand {@link Lobby}
  * aufgerufen.
  */
 public class VerbindungErfassen extends ClientZustand {
 	public void onEntry() {
 		controller.zeigeVerbinden();
 	}
 
 	Class<? extends Zustand> verbinden(VerbindeEvent ve) {
 		try {
 			BriefKastenInterface briefkasten = new BriefkastenAdapter(spiel.queue);
 
 			spiel.endpunkt = new ClientEndPunkt(ve.hostname, ve.port, briefkasten, spiel);
 
 			spiel.endpunkt.sende(new SpielBeitreten(ve.spielerName));
 			spiel.spielerName = ve.spielerName;
 		} catch (UnknownHostException e) {
			controller.zeigeFehlermeldung("Unbekannter Server: " + ve.hostname);
			controller.verbindungsaufbauAbgebrochen();
 			spiel.endpunkt = null;
 			return VerbindungErfassen.class;
 		} catch (IOException e) {
 			controller.zeigeFehlermeldung("Verbindung konnte nicht hergestellt"
 			                              + " werden (" + e.getMessage() + ")");
 			controller.verbindungsaufbauAbgebrochen();
 			spiel.endpunkt = null;
 			return VerbindungErfassen.class;
 		}
 
 		return Lobby.class;
 	}
 }
