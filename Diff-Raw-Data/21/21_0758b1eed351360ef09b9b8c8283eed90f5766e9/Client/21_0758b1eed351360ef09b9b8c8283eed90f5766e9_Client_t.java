 import lejos.nxt.*;
 import lejos.nxt.addon.*;
 import lejos.nxt.remote.*;
 import lejos.nxt.comm.*;
 import java.io.*;
 
 public class Client {
 
 	public static void run() throws Exception {
 		NXTConnection connection = Bluetooth.waitForConnection();
 		DataInputStream in = connection.openDataInputStream();
 		DataOutputStream out = connection.openDataOutputStream();
 		
 		System.out.println("Starten...");
 		Drive.moveToNextCrossing();
 		System.out.println("Bereit");
 		out.writeChar('k');
 		out.flush();
 		Drive.playTune("HHCDEDCHAACEDC",200);
 		
 		char ch = ' ';
 		
 		while(!Button.ESCAPE.isPressed()) {
 			ch = in.readChar();
 			if(ch == '#') {
 				ch = in.readChar();
 			 	while(ch != '#') {
 			 		System.out.print("" + ch);
 					ch = in.readChar();
 				}
 				System.out.println();
 			}
 			if(ch == 'c') {
 				LCD.clear();
 			}
 			if(ch == 'r') {
 				System.out.println("Befehl r");
 				Drive.turn(1);
 				Drive.moveToNextCrossing();
 				out.writeChar('k');
 				out.flush();
 			}
 			if(ch == 'l') {
 				System.out.println("Befehl l");
 				Drive.turn(-1);
 				Drive.moveToNextCrossing();
 				out.writeChar('k');
 				out.flush();
 			}
 			if(ch == 's') {
 				System.out.println("Befehl s");
 				Drive.turn(0);
 				Drive.moveToNextCrossing();
 				out.writeChar('k');
 				out.flush();
 			}
 			if(ch == 't') {
 				System.out.println("Befehl t");
 				Drive.reverse();
 				Drive.playTune("HHCDEDCHAACEDC",200);
 				Drive.moveToNextCrossing();
 				out.writeChar('k');
 				out.flush();
 			}
 			if (ch == '.') {
				Drive.playTune("CDECAAHCDEDCHH",200);
 				return;
 			}
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		ColorSensor c1 = new ColorSensor(SensorPort.S2);
 		ColorSensor c2 = new ColorSensor(SensorPort.S3);
 
 		Drive.kalibrieren(c1,c2);
 		run();
 	}
 
 }
