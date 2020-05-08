 package communicationPackage;
 
 import java.io.BufferedReader;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Scanner;
 
 import javax.swing.JOptionPane;
 
 import commands.Command;
 import commands.CommandFactory;
 
 import cards.Card;
 import cards.patterns.CardPattern;
 import cards.patterns.CardPatternFactory;
 
 public class ReaderThread extends Thread {
 			
 	private BufferedReader in;
 			
 	public ReaderThread(BufferedReader in){
 		this.in = in;
 	}
 	
 	public void run(){
 		try{
 			while (true){
 				String input = in.readLine();
 				String[] params = input.split("~");
				JOptionPane.showMessageDialog(null,"Message Received: "+input);
				//Command command = CommandFactory.createCommand(params[0]);
				//command.execute(params);
 			}
 		}catch (Exception e) {
 			//do nothing
 		}
 	}
 }
