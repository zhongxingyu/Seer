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
 
 
 package initialisierung;
 
 import java.util.List;
 import java.util.Vector;
 
 import applikation.bot.Stupidbot;
 import applikation.client.konfiguration.Konfiguration;
 
 /**
  * Applikationsstart der Demo. Die Demo startet automatisch einen Server ({@link BodesuriServer}),
  * drei Bots ({@link BodesuriBot}) und einen Client ({@link Bodesuri}). So
  * ist es möglich das Spiel alleine zu spielen.
  */
 public class BodesuriDemo {
 	/**
 	 * Die Demo starten.
	 *
 	 * @param args
 	 *            Wird nicht genutzt
 	 * @throws InterruptedException
 	 */
 	public static void main(String[] args) throws InterruptedException {
 		BodesuriServer server = new BodesuriServer();
 		server.start();
 		server.warteAufBereitschaft();
 
 		Vector<Thread> clients = new Vector<Thread>();
 
 		List<String> namen = new Vector<String>();
 		namen.add("Donald");
 		namen.add("Daisy");
 		namen.add("Mickey");
 
 		for (int i = 0; i < 3; i++) {
 
 			Konfiguration konfig = new Konfiguration();
 			konfig.defaultName = namen.get(i);
 			konfig.debugAutoLogin = true;
 
 			BodesuriBot b = new BodesuriBot(konfig, Stupidbot.class, false);
 
 			b.start();
 
 			clients.add(b);
 		}
 
 		Bodesuri b = new Bodesuri();
 		b.start();
 
 		b.join();
 
		/* Hat sich der Client nie verbunden ist kriegt Server nie mit über
		 * dass er sich beenden sollte. */
		System.exit(0);
 	}
 }
