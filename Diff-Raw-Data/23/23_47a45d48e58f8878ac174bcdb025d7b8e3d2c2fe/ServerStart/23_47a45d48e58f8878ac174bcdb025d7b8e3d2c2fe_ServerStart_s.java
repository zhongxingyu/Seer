 /*  Copyright (C) 2010  Fabian Neundorf, Philip Caroli, Maximilian Madlung, 
  * 						Usman Ghani Ahmed, Jeremias Mechler
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ojim.server;
 
 //import com.sun.java_cup.internal.runtime.Scanner;
 import java.util.Scanner;
 
 public class ServerStart {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		System.out.println("Starting Server");
 		OjimServer server = new OjimServer("testServer");
 		
 		System.out.println("Server started");
 		boolean running = true;
 		boolean isOpen = false;
 		
 		System.out.println("\nAdministrative Functions can be executed here, type help for an overview of existing calls");
 		
 		String input;
 		Scanner in = new Scanner(System.in);
 		while(running)
 		{
 			//read input
 			input = in.nextLine();
 			
 			if(input.equals("help")) {
 				System.out.println("help            shows this dialog");
 				System.out.println("info            shows some information");
 				System.out.println("exit            ends Ojim");
 				System.out.println("start [1] [2]   starts a new game");
 				System.out.println("                [1] PlayerCount");
 				System.out.println("test            adds a new TestClient");
 				System.out.println("                [2] AICount");
 				System.out.println("                currently its start 2 1");
 				System.out.println("end             ends the current game");
 			} else if(input.equals("info")) {
 				System.out.println("Servername:     " + server.getName());
 				System.out.println("Status:         " + (isOpen ? "open": "closed"));
 				System.out.println("Players:        " + server.getConnectedClients() + "/" + server.getMaxClients());
 				System.out.println("AI:             " + "-");
 			} else if(input.equals("exit")) {
 				if(isOpen) {
 					System.out.println("Ending current game");
 					server.endGame();
 					System.out.println("Current game Ended!");
 				}
 				running = false;
 			} else if(input.equals("test")) {
 				System.out.println("Adding new TestClient!");
 				new TestClient(server);
 				System.out.println("TestClient added!");
 			} else if(input.startsWith("start")) {
 				if(isOpen) {
 					System.out.println("There is already a Game running, end it first before starting a new one!");
 				} else {
 					System.out.println("Initializing Game");
					server.initGame(2,1);
 					System.out.println("Game initialized!");
 				}
 			} else if(input.equals("end")) {
 				if(!isOpen) {
 					System.out.println("There is no Game running!");
 				} else {
 					System.out.println("Ending current game");
 					server.endGame();
 					System.out.println("Current game Ended!");
 				}
 			}
 		}
 
 		System.out.println("Server Stoped");
 	}
 
 }
