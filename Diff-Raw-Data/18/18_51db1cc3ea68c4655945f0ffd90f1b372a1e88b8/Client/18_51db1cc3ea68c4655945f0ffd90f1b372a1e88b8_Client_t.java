 
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 public class Client extends JFrame {
 	private BufferedWriter output;
 	private BufferedReader input;
 	private Socket socket;
 	private String message;
 	private String ipAdress = "localhost";
 	private int portNmbr = 5000;
 	
 	private String nickName;
 	private boolean uniqueNick = false;
 	
 	private JTextField userText;
 	private JTextArea chatWindow;
 	   
 	public Client() {
 		
 		//Skapar en temporr GUI tills den riktiga r klar
 		 super("TEMPORARY STUFF");
 	      userText = new JTextField();
 	      userText.addActionListener(
 	         new ActionListener(){
 	            public void actionPerformed(ActionEvent event){
            		if (userText.getText().length() == 0) {
            			return; }
            		else {
            			if (uniqueNick == false){
            				sendNickname(userText.getText());
            				userText.setText("");
            			} else {
            				sendMessage(userText.getText());
            				userText.setText("");
            			}
            		}
 	            }
 	         }
 	      );
 	      add(userText, BorderLayout.NORTH);
 	      chatWindow = new JTextArea();
 	      add(new JScrollPane(chatWindow), BorderLayout.CENTER);
 	      setSize(300,400);
 	      setVisible(true);
 	      setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 	
 	JTextField nickChoice = new JTextField();
 	JFrame chooseNickFrame = new JFrame();
 	JLabel plsChooseNick = new JLabel("Please choose a nickname");
 	public void startProgram() {
 		try {
 			connect(ipAdress, portNmbr);
 		} catch (UnknownHostException e) {
 			System.err.println("Unknown host exception: " + e.getMessage());
 			System.exit(2);
 		} catch (IOException e) {
 			System.err.println("Error: " + e.getMessage());
 			System.exit(1);
 		}
 		nickChooser();
 	}
 	
 	public void nickChooser() {
 		showText("Choose a nickname and press enter");
 	}
 
 	/*public void nickChooser() {
 		//Skapar en temporr popup fr att testa lite
 		while(uniqueNick == false){
 				nickChoice.addActionListener(
 						new ActionListener(){
 							public void actionPerformed(ActionEvent eve){
 								try {
 									output.write("NICK:" + nickChoice.getText() + "\n"); //Frsker skicka ett nickname till servern
 									nickChoice.setText("");
 									chatWindow.append("");
 								} catch (IOException e) {
 									System.err.println("Could not send nickname..");
 								}
 							}
 						});
 				
 				chooseNickFrame.add(plsChooseNick, BorderLayout.NORTH);
 				chooseNickFrame.add(nickChoice, BorderLayout.SOUTH);
 				chooseNickFrame.setVisible(true);
 				chooseNickFrame.setSize(200, 80);
 			
 				try {
 					if (input.readLine() == "NICK:OK") {
 						uniqueNick = true;
 						break;
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 		}
 		
 		
 	}*/
 	
 	public void connect(String address, int port)throws IOException{
 		socket = new Socket(address, port);
 		input = new BufferedReader(new InputStreamReader(socket.getInputStream()) );
 		output = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()) );
 		System.out.println("Streams are good to go!");
 	}
 
 	public void showText(final String message){
 		SwingUtilities.invokeLater(new Runnable(){
 			public void run(){
 				chatWindow.append(message);				//append gör att det skrivs till efter nuvarande text
 			}
 		});
 	}
 	
 	public void chatting(){
 		do{
 			try{
 				message = input.readLine();					//Läser in raden som kommer från server
 				System.out.println(message);
 				if(message.startsWith("MESSAGE:")){			//Servern skickar med en string specifikt för chatt
 					String mess = message.split(":")[1];	//Delar på meddelandet efter :
 					showText("\n " + mess); 				//showText-metoden skickar upp meddelandet på chattfältet
 				}else if(message.startsWith("NEW USER:")){
 					String mess = message.split(":")[1];	
 					showText("\n " + mess + " has started chatting!"); 
 				}else if(message.startsWith("USER LEFT:")){
 					String mess = message.split(":")[1];
 					showText("\n" + mess + " has logged off.");
 				}else if(message.startsWith("NICK:TAKEN")){
 					showText("\nNickname already taken, please choose another.");
 					uniqueNick = false;
 				}else if(message.startsWith("NICK:OK")){
 					uniqueNick = true;
 				}
 			
 			}catch(IOException notfound){
 				showText("\n Error on recieve..." );
 			}
 		}while(!message.equals("END"));						//Om man skriver END så avslutas chatten
 	}
 	
 	
 	public void sendMessage(String message){
 			try{
 				output.write("MESSAGE:"+message+"\n");				//Skickar ett meddelande
 				output.flush();									//Spolar toaletten :)
 			}catch (IOException e){
 				chatWindow.append("\nCan't send message..."); 
 			}
 	}
 	
 	public void sendNickname(String message){
 		try{
 			output.write("NICK:"+message+"\n");				//Skickar ett meddelande
 			output.flush();									//Spolar toaletten :)
 		}catch (IOException e){
 			chatWindow.append("\nCan't send nickname..."); 
 		}
 }
 	
 	public void close(){
 		try {
 			input.close();
 			output.close();
 			socket.close();
 		} catch (IOException e) {
 			System.out.println("Failed to close.");
 		}
 	}
 }
