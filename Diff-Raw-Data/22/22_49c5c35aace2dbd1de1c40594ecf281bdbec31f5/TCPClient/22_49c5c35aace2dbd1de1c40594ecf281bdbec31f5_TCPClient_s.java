 import java.io.*;
 import java.net.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 public class TCPClient extends JFrame {
 
     JTextField jtfText1, jtfText2, jtfUneditableText, jtfUneditableText2;
     String disp = "";
     TextHandler handler = null;
     int time;
     int wordTime;
     int secondBoxTimer = 0;
     java.util.Timer timer;
     Container container;
     String wordFromServer;
     Vector<String> wordsFromServer;
     private static Integer gameLength = 6 * 60; // 6 minutes to seconds
     private Integer gameState; // 0 - not connected, 1 - game in progress, 2 - game over
     String commandFromServer;
     Socket clientSocket;
     DataOutputStream outToServer;
     BufferedReader inFromServer;
     private Integer playerPoints;
     private Integer alwaysContstantWordTime = 2 * 5;
 
 
     public TCPClient() {
 
     	gameState = 0;
     	playerPoints = 0;
 
         time = gameLength;
         wordTime = alwaysContstantWordTime;
         timer = new java.util.Timer();
 
         container = getContentPane();
         container.setLayout(new FlowLayout());
         jtfText1 = new JTextField(10);
         jtfText2 = new JTextField(10);
         jtfUneditableText = new JTextField("Uneditable text field", 25);
         jtfUneditableText.setEditable(false);
         jtfUneditableText2 = new JTextField("Uneditable text field", 30);
         jtfUneditableText2.setEditable(false);
         
         container.add(jtfText1);
         container.add(jtfText2);
         container.add(jtfUneditableText);
         container.add(jtfUneditableText2);
         handler = new TextHandler();
         jtfText1.addActionListener(handler);
         jtfText2.addActionListener(handler);
         jtfUneditableText.addActionListener(handler);
         jtfUneditableText2.addActionListener(handler);
         setSize(500, 125);
         wordFromServer = "";
         wordsFromServer = new Vector<String>();
         commandFromServer = "";
     }
 
     private class TextHandler implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
         	jtfText2.setText("--");
             if (e.getSource() == jtfText1) {
                // disp = "text1 : " + e.getActionCommand();
                 sendGuessToServer("0" + e.getActionCommand());
             } 
             if (e.getSource() == jtfText2) {
                // disp = "text1 : " + e.getActionCommand();
                 sendGuessToServer("1" + e.getActionCommand());
             } 
         }
     }
 
     public void sendGuessToServer(String guess)
     {
     	//System.out.println("Sending the following to server " + guess);
     	try {
 
     	outToServer.writeBytes(guess);
     	outToServer.writeBytes("\n");
     	}
     	catch(Exception e)
     	{
     		System.out.println("Lost connection to server");
     		System.exit(2);
     	}
     }
 
     public void processResponseFromServer(String response)
     {
     	switch(response.charAt(0))
     	{
     		case '0': // exit abruptly
     		gameState = 2;
     		jtfText2.setText("Server down");
     		break;
     		case '1': // right guess, got new word
     		jtfText1.setText("");
     		wordFromServer = response.substring(1);
     		jtfText2.setText("Right Guess!");
     		secondBoxTimer = 2;
     		resetwordTime();
     		playerPoints += 1;
     		break;
     		case '2': // game over, time up
     		jtfText2.setText("Game over");
     		secondBoxTimer = 2;
     		gameState = 2;
     		break;
     		case '3': // wrong guess
     		jtfText1.setText("");
     		jtfText2.setText("Wrong guess");
     		secondBoxTimer = 2;
     		playerPoints -= 2;
     		break;
     		case '4': // 2 min time up, got new word
     		jtfText1.setText("");
     		wordFromServer = response.substring(1);
     		jtfText2.setText("2 min up!");
     		secondBoxTimer = 2;
     		resetwordTime();
     		break;
     		case '5': // Answer to question
     		jtfText2.setText(response.substring(1));
     		break;
     		default:
     		System.out.println("Got unknown message from server");
     	}
     }
     public void resetwordTime()
     {
     	wordTime = alwaysContstantWordTime;
     }
     void run() {
 
         TCPClient test = new TCPClient();
 
         test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        
         BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
 
         
 
         try {
         clientSocket = new Socket("localhost", 6789);
         outToServer = new DataOutputStream(clientSocket.getOutputStream());
         inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         
         
         gameState = 1;
         
         for(int tempIndex = 0; tempIndex < 3; tempIndex++)
         {
             wordFromServer = inFromServer.readLine();
             wordsFromServer.add(wordFromServer);
         }
         
         timer.scheduleAtFixedRate(new TimerTask() {
             @Override
             public void run()
             {
                 time--;
                 secondBoxTimer--;
                 if(wordTime > 0)
                 	wordTime--;
                 jtfUneditableText2.setText("Current Word: " + wordFromServer + ". Points: " + playerPoints);
                
                 jtfUneditableText.setText("Word time is "+ wordTime +" sec. " + "Game time is " + time + " sec.");
                 if(time == 0)
                 {
                 	System.out.println("Game time up!");
                 	System.exit(0);
                 }
                 if(secondBoxTimer == 0)
                 	jtfText2.setText("--");
                 setVisible(true);
             }
         }, 0, 1000);
 
         while(gameState != 2)
         {
 	        commandFromServer = inFromServer.readLine();
 	        processResponseFromServer(commandFromServer);
 	    }
          
         clientSocket.close();
         }
         catch (Exception e){
             System.out.println("Cannot connect to server.");
             System.exit(0);
         }
         
     }
 
     public static void main(String[] args) throws Exception {
     
     TCPClient client = new TCPClient();
     client.run();
   }
 }   
