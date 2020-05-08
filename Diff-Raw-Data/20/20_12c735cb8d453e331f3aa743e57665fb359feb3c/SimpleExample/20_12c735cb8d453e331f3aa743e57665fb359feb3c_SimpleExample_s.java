 /*
  * Copyright (c) Novedia Group 2012.
  *
  *     This file is part of Hubiquitus.
  *
  *     Hubiquitus is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Hubiquitus is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with Hubiquitus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package main;
 
 import javax.swing.JFrame;
 
 import org.hubiquitus.hapi.client.HClient;
 
 
 /***
  * 
  * @author speed
  * @version 0.3
  * Example of a basic connection/disconnection application
  */
 
 public class SimpleExample {
 
 	public static void main(String[] args) {
 		HClient client = new HClient();
 		
 		JFrame window = new JFrame("test");
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(1000, 650);
 
 		MainPanel panel = new MainPanel();
 		panel.setClient(client);
 		window.setContentPane(panel);
 		window.setResizable(false);
 		window.setVisible(true);
 	}
 
 }
