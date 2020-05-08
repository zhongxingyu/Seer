 package com.vu.se.hm.gui;
 
 import com.vu.se.hm.net.*;
 
 
 public class HangmanStarter {
 
 	public static void main(String[] sd) {
                 /*
 		JFrame frame = new JFrame("TMNT Hangman");
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 frame.add(new HangmanWelcomeScreen());
                 frame.setSize(800,600);
                 frame.setVisible(true);
                 */
                 GuesserServer server = new GuesserServer("TEST");
                 server.addPlayer();
                 server.addPlayer();
                 (new Thread(server)).start();
                 
                 GuesserClient client = new GuesserClient();               
                 client.connect("127.0.0.1", 1234);
                 (new Thread(client)).start();
 		ViewController hangmanViewController = new ViewController(client, false);
                 client.addEventListener(hangmanViewController);
                 
                 GuesserClient client2 = new GuesserClient();               
                client2.connect("127.0.0.1", 1234);
                 (new Thread(client2)).start();
 		ViewController hangmanViewController2 = new ViewController(client2, false);
                 client2.addEventListener(hangmanViewController2);
 	}
 
 }
