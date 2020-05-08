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
 

 package spielplatz;
 
 import applikation.client.konfiguration.Konfiguration;
 import initialisierung.Bodesuri;
 import initialisierung.BodesuriServer;
 
 public class BodesuriEntwickler {
 
 	public static void main(String[] args) throws InterruptedException {
 		BodesuriServer server = new BodesuriServer(2);
 		server.start();
 		server.warteAufBereitschaft();
 
 		Konfiguration konfigA = new Konfiguration();
 		konfigA.defaultName = "Christoph";
 		konfigA.debugAutoLogin = true;
 		konfigA.debugKeineLobbyVerzoegerung = true;
 
 		Konfiguration konfigB = new Konfiguration();
 		konfigB.defaultName = "Micheline";
 		konfigB.debugAutoLogin = true;
 		konfigB.debugKeineLobbyVerzoegerung = true;
 
 		Bodesuri a = new Bodesuri(konfigA);
 		Bodesuri b = new Bodesuri(konfigB);
 
 		a.start();
 		b.start();
 
 		a.join();
 		b.join();
 
 		/* Hat sich der Client nie verbunden ist kriegt Server nie mit über
 		 * dass er sich beenden sollte. */
 		System.exit(0);
 	}
 }
