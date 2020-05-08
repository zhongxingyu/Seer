 /*******************************************************************************
  * Copyright (c) 2012 GamezGalaxy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package com.gamezgalaxy.test.console;
 
 import java.util.Scanner;
 
 import com.gamezgalaxy.GGS.server.Server;
 
 public class Main {
 	
 	public static void main(String[] args) {
 		Server s = new Server("Test", 25558, "Test");
 		s.Start();
		s.getEventSystem().registerEvents(new Test());
 		while (true) {
 			String line = new Scanner(System.in).nextLine();
 			if (line.equals("stop")) {
 				try {
 					s.Stop();
 					break;
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			else
 				s.Log(line);
 		}
 		System.out.println("Server stopped..");
 	}
 }
