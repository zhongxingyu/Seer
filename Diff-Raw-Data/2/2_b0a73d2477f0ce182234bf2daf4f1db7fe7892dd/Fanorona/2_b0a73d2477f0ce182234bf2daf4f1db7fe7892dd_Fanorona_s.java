 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 import java.util.*;
 import java.util.Timer;
 
 import javax.sound.midi.*;
 import javax.swing.JOptionPane;
 
 import java.lang.reflect.InvocationTargetException;
 import java.net.*;  
 import java.io.*;
 
 public class Fanorona extends JPanel implements ActionListener, MouseListener {
     StateMachine stateMachine; //contains grid
     //No flags necessary beyond the state machine itself
 
 	private JButton newGameButton;  
 	private JButton instructionsButton; 
 	private JButton nameButton;
 	private JButton aiButton;
 	private JLabel timerBox;
 	private JLabel messageBox;  	
 
 	static final int LOOP_CONTINUOUSLY = 9999;
 	int BUTTON_SIZE_WIDTH = 120;
 	int BUTTON_SIZE_HEIGHT = 30;
    
 	double xGridAndExcess;
     double yGridAndExcess;
 	double xGridSize;
 	double yGridSize;
 	double changeFactor = 1.0;
 	
 	String playerName;
 	int timePerTurn;
 	Clock clock;
 	
 	String networkSetting;
 	String serverName;
 	int serverPort = 8725;
 	int clientPort = 8725;
 	String clientStartingSide = "B";
 	
     int rowSize;
     int colSize;
     Boolean aiIsOn;
 
     public static AI ai;
 
 	public static void main(String[] args) throws Exception {//{{{
         //setup game window
 		JFrame window = new JFrame("Fanorona");
 		Fanorona content = new Fanorona();
 		window.setContentPane(content);
 		window.pack();
 		
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		window.setResizable(false);  
 		window.setVisible(true);
 		URL url = new URL("http://www.vgmusic.com/music/console/nintendo/nes/advlolo1_theme.mid");
         Sequence sequence = MidiSystem.getSequence(url);
         Sequencer sequencer = MidiSystem.getSequencer(); 
         sequencer.open();
         sequencer.setSequence(sequence);
         sequencer.setLoopCount(LOOP_CONTINUOUSLY);
         sequencer.start();
 	}//}}}
 
 	public Fanorona() {//{{{
 		setLayout(null); 
 		askServerClientInfo();
 		ObjectOutputStream socketOut = null;
 		ObjectInputStream socketIn = null;
 		if(networkSetting == "Server") {
 			getServerConfig();
 			askGridSize();
 			askTimePerTurn();
 			Socket acceptSocket = null;
 			try {
 				acceptSocket = setupServerSockets();
 				socketOut = new ObjectOutputStream(acceptSocket.getOutputStream());
 				socketOut.flush();
 			    socketIn = new ObjectInputStream(acceptSocket.getInputStream());
 			    String welcome = "WELCOME";
 			    socketOut.writeObject(welcome);
 			    System.out.println("Server: " + welcome);
 			    socketOut.flush();
 			} catch (IOException e) {}
 			sendGameInfo(socketOut, colSize, rowSize, timePerTurn);
 			waitForClient(socketIn, socketOut);
 		} else if (networkSetting == "Client") {
 			getClientConfig();
 			Socket acceptSocket = null;
 			try{
 				acceptSocket = setupClientSocket();
 				socketOut = new ObjectOutputStream(acceptSocket.getOutputStream());
 				socketOut.flush();
 			    socketIn = new ObjectInputStream(acceptSocket.getInputStream());
 				receiveGameInfo(socketIn);
 				String ready = "READY";
 				socketOut.writeObject(ready);
 				System.out.println("Client: " + ready);
 				socketOut.flush();
 				waitForBegin(socketIn);
 			} catch (IOException e) {}
 		} else {
 			askGridSize();
 			askTimePerTurn();
 		}
 		createGrid();
 		
 		setPreferredSize(new Dimension((BUTTON_SIZE_WIDTH*2+30)+((int)((colSize*100+100)*changeFactor)),(int)((rowSize*100+100)*changeFactor)));
 		stateMachine = new StateMachine(colSize, rowSize, timePerTurn, changeFactor, networkSetting, socketOut, socketIn, clientStartingSide);			
 		
 		add(stateMachine.grid);
 		stateMachine.grid.setBounds(BUTTON_SIZE_WIDTH*2+30,1,(int)((colSize*100+100)*changeFactor),(int)((rowSize*100+100)*changeFactor)); 
 		
         ai = new AI();
        ai.setBounds(colSize - 1 , rowSize - 1);
         aiIsOn = false;
 
         initButtons();
 
         //add listeners
         instructionsButton.addActionListener(this);
         newGameButton.addActionListener(this);
         nameButton.addActionListener(this);
         aiButton.addActionListener(this);
         addMouseListener(this);
 
         String message = stateMachine.run("NewGame", null);
         messageBox.setText(message);
         
         clock = new Clock(timePerTurn);
         EventQueue.invokeLater(new Runnable() {
         	@Override
             public void run() {
                 clock.startTimer();
             }
         });
 	} //}}}
 
 	public void createGrid() {
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		
 		//resize the grid
 		System.out.println("screenWidth: " + screenSize.getWidth());
 		System.out.println("screenHeight: " + screenSize.getHeight());
 		
 		xGridAndExcess = (colSize*100)+(BUTTON_SIZE_WIDTH*2+30);
 		yGridAndExcess = (rowSize*100)+100;
 		System.out.println("xGridAndExcess: " + xGridAndExcess);
 		System.out.println("yGridAndExcess: " + yGridAndExcess);
 		
 		if(xGridAndExcess > screenSize.getWidth() || yGridAndExcess > screenSize.getHeight()) {
 			xGridSize = screenSize.getWidth() - ((BUTTON_SIZE_WIDTH*2)+30);
 			yGridSize = screenSize.getHeight() - ((BUTTON_SIZE_HEIGHT*2)+30);
 			System.out.println("xGridSize: " + xGridSize);
 			System.out.println("yGridSize: " + yGridSize);
 			
 			if((xGridAndExcess-screenSize.getWidth()) >= (yGridAndExcess-screenSize.getHeight())) {
 				changeFactor = xGridSize / xGridAndExcess;
 				System.out.println("% change: " + (xGridSize / xGridAndExcess));
 			}
 			else {
 				changeFactor = yGridSize / yGridAndExcess;
 				System.out.println("% change: " + (yGridSize / yGridAndExcess));
 			}				
 		}		
 	}
 	
     public void initButtons() {//{{{
         instructionsButton = new JButton("Instructions");
         newGameButton = new JButton("New Game");
         aiButton = new JButton("Toggle AI");
         nameButton = new JButton("Change Name");
         timerBox = new JLabel("",JLabel.LEFT);
         timerBox.setVerticalAlignment(JLabel.TOP);
         timerBox.setFont(new Font("Serif", Font.BOLD, 12));
         timerBox.setForeground(Color.BLACK);
         messageBox = new JLabel("",JLabel.LEFT);
         messageBox.setVerticalAlignment(JLabel.TOP);
         messageBox.setFont(new Font("Serif", Font.BOLD, 12));
         messageBox.setForeground(Color.BLACK);
 
 		add(newGameButton);
         add(aiButton);
 		add(instructionsButton);	
 		add(nameButton);
 		add(timerBox);
 		add(messageBox);
 				
 		newGameButton.setBounds(10, 10, BUTTON_SIZE_WIDTH, BUTTON_SIZE_HEIGHT);
 		instructionsButton.setBounds(BUTTON_SIZE_WIDTH+20, 10, BUTTON_SIZE_WIDTH, BUTTON_SIZE_HEIGHT);
 		nameButton.setBounds(10, BUTTON_SIZE_HEIGHT+20, BUTTON_SIZE_WIDTH, BUTTON_SIZE_HEIGHT);
 		aiButton.setBounds(BUTTON_SIZE_WIDTH+20, BUTTON_SIZE_HEIGHT+20, BUTTON_SIZE_WIDTH, BUTTON_SIZE_HEIGHT);
 		
 		timerBox.setBounds(10, (BUTTON_SIZE_HEIGHT*2)+25, BUTTON_SIZE_WIDTH*2, BUTTON_SIZE_HEIGHT);
 		messageBox.setBounds(10, (BUTTON_SIZE_HEIGHT*3)+25, BUTTON_SIZE_WIDTH*2, BUTTON_SIZE_HEIGHT*4);
     }//}}}
 
     public void mouseEntered(MouseEvent evt) {}
     public void mouseExited(MouseEvent evt) {}
     public void mousePressed(MouseEvent evt) {
     	if(clickingIsAllowed()) {
             if(SwingUtilities.isRightMouseButton(evt)) {
                 String message = stateMachine.run("RClick", null);
                 messageBox.setText(message);
             } else { //left click 
             	String message = stateMachine.run("Click", evt.getPoint());
             	
                 messageBox.setText(message);
             }
         }
         //start AI on transition to enemy turn
     	runAI();
     }
     public void mouseReleased(MouseEvent evt) {}
     public void mouseClicked(MouseEvent evt) {}
     
     private Boolean clickingIsAllowed() {//{{{
         return !aiIsOn || (aiIsOn && stateMachine.isPlayerTurn());
     }//}}}
 
     public void runAI() {//{{{
         //if at state of AI turn
         if(aiIsOn && (stateMachine.getState() == State.ENEMY_SELECT)) {
             //get the move from AI
             ArrayList<Point> points = new ArrayList<Point>();
             Move move = ai.getMove(stateMachine.grid.getState());
             Point startLocation = new Point(move.startPointX, move.startPointY);
             Point endLocation = new Point(move.endPointX, move.endPointY);
             points.add(startLocation);
             points.add(endLocation);
             
 
             //feed all the points to the state machine
             for(Point p : points) {
                 System.out.println("INSIDE OF FANORONA.JAVA " + p.x + " " + p.y);
                 String message = stateMachine.run("AIChoice", p);
                 messageBox.setText(message);
             }
         }
     }//}}}
 
     public void actionPerformed(ActionEvent evt) {//{{{
         Object src = evt.getSource();        
         
         if(src == newGameButton) {
             String message = stateMachine.run("NewGame", null);
             messageBox.setText(message);
         } else if(src == instructionsButton) {
             instructions();
         } else if(src == nameButton) {
             changeName();
         } else if(src == aiButton) {
             if(stateMachine.isPlayerTurn()) {
                 aiIsOn = !aiIsOn; //toggle
                 String aiState = aiIsOn?"on":"off";
                 JOptionPane.showMessageDialog(this, "AI is now " + aiState + ".", "AI toggle", JOptionPane.PLAIN_MESSAGE);
             } else {
                 JOptionPane.showMessageDialog(this, "Can only toggle AI during the white player's turn.", "AI toggle", JOptionPane.PLAIN_MESSAGE);
             }
         }
         
     }//}}}
 
     //asks the user if the game is to be played in a server/client config
     void askServerClientInfo() {
     	JPanel panel = new JPanel();
     	panel.add(new JLabel("Please choose what network function you would like this game instance to perform: "));
     	DefaultComboBoxModel networkConfig = new DefaultComboBoxModel();
     	networkConfig.addElement("Just Play a Local Game");
     	networkConfig.addElement("Server");
     	networkConfig.addElement("Client");
     	JComboBox networkConfigBox = new JComboBox(networkConfig);
     	panel.add(networkConfigBox);
     	
     	int result = JOptionPane.showConfirmDialog(null, panel, "Choose Game Network Configuration", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
     	switch (result) {
     		case JOptionPane.OK_OPTION:
     			networkSetting = (String)networkConfigBox.getSelectedItem();
     			System.out.println("network: " + networkSetting);
     			break;
     		default:
     			System.exit(0);
     			break;
     				
     	}
     }
     
     Socket setupServerSockets() throws IOException {
     	ServerSocket serverSocket = null;
         try {
             serverSocket = new ServerSocket(serverPort);
         } catch (IOException e) {
             System.err.println("Could not listen on port: " + serverPort + ".");
             System.exit(1);
         }
         
         /*String socketNotice = "Fanarona will continue once a client connects to the server.\nPress OK to dismiss this message and continue.";
         JOptionPane.showMessageDialog(this, socketNotice, "Waiting for client connect...", JOptionPane.PLAIN_MESSAGE);*/
 
         Socket acceptSocket = null;
         try {
             acceptSocket = serverSocket.accept();
         } catch (IOException e) {
             System.err.println("Accept failed.");
             System.exit(1);
         }
         
         return acceptSocket;
     }
     
     Socket setupClientSocket() throws IOException {
     	Socket acceptSocket = new Socket(serverName, clientPort);
     	return acceptSocket;
     }
     
     void getServerConfig() {
     	try {
     		String value = JOptionPane.showInputDialog(null, "What port should the server listen on? (Leave Blank for Default Fanarona Port 8725)", "Server Port Configuration", JOptionPane.QUESTION_MESSAGE);
     		if(value == null)
     			System.exit(0);
     		serverPort = Integer.parseInt(value);
     	} catch(NumberFormatException e) {}
     }
     
     void getClientConfig() {
     	try {
     		String value = JOptionPane.showInputDialog(null, "Please enter the hostname of the server you wish to connect.", "Server Hostname", JOptionPane.QUESTION_MESSAGE);
     		if(value == null)
     			System.exit(0);
     		serverName = value;
     	} catch(NumberFormatException e) {}
     	try {
     		String value = JOptionPane.showInputDialog(null, "What port should the client connect on? (Leave Blank for Default Fanarona Port 8725)", "Server Port Configuration", JOptionPane.QUESTION_MESSAGE);
     		if(value == null)
     			System.exit(0);
     		clientPort = Integer.parseInt(value);
     	} catch(NumberFormatException e) {}
     }
     
     void sendGameInfo(ObjectOutputStream m_socketOut, int cols, int rows, int timeout) {
     	//send game info over the socket
     	try{
     		String gameInfoMessage = "INFO " + cols + " " + rows + " B " + timeout;
 			m_socketOut.writeObject(gameInfoMessage);
 			System.out.println("Server: " + gameInfoMessage);
 			m_socketOut.flush();
 		}
 		catch(IOException ioException){}
     }
     
     void waitForClient(ObjectInputStream m_socketIn, ObjectOutputStream m_socketOut) {
     	try {
 	    	String message = "";
 	    	while(!message.equals("READY")) {
 	    		message = (String)m_socketIn.readObject();
 	    		System.out.println("Client: " + message);
 	    	}
 	    	String response = "BEGIN";
 	    	m_socketOut.writeObject(response);
 	    	System.out.println("Server: " + response);
 	    	m_socketOut.flush();
     	} catch (Exception e) {}
     }
     
     void waitForBegin(ObjectInputStream m_socketIn) {
     	try {
     		String message = "";
     		while(!message.equals("BEGIN")) {
     			message = (String)m_socketIn.readObject();
     			System.out.println("Server: " + message);
     		}
     	} catch (Exception e) {}
     }
     
     void receiveGameInfo(ObjectInputStream m_socketIn) {
     	//get game info from the socket
     	String message = "";
     	try{
     		while(!message.equals("WELCOME")) {
     			message = (String)m_socketIn.readObject();
     			System.out.println("Server: " + message);
     		}
     		while(!message.startsWith("INFO")) {
     			message = (String)m_socketIn.readObject();
     			System.out.println("Server: " + message);
     			String[] infoArray = message.split(" ");
     			if(infoArray.length != 5)
     				System.err.println("Bad info received from server.");
     			colSize = Integer.parseInt(infoArray[1]);
     			rowSize = Integer.parseInt(infoArray[2]);
     			clientStartingSide = infoArray[3];
     			timePerTurn = Integer.parseInt(infoArray[4]);
     		}
     	} catch (Exception e) {}
     }
     
     //asks the user for a grid size (row, col)
     void askGridSize() {
     	JPanel panel = new JPanel();
     	panel.add(new JLabel("Choose a Fanorona board size (row, column): "));
     	DefaultComboBoxModel rows = new DefaultComboBoxModel();
     	DefaultComboBoxModel cols = new DefaultComboBoxModel();
         
     	for(int n = 1; n <= 13; n+=2) {
         	rows.addElement(n);
         	cols.addElement(n);
         }
     	rows.setSelectedItem(rows.getElementAt(2));
     	cols.setSelectedItem(cols.getElementAt(4));
         JComboBox rowsBox = new JComboBox(rows);
         panel.add(rowsBox);
         JComboBox colsBox = new JComboBox(cols);
         panel.add(colsBox);
         
         int result = JOptionPane.showConfirmDialog(null, panel, "Choose Fanorona Grid Size", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
         switch (result) {
             case JOptionPane.OK_OPTION:
             	rowSize = (Integer)rowsBox.getSelectedItem();
             	colSize = (Integer)colsBox.getSelectedItem();
                 System.out.println("rows: " + rowSize);
                 System.out.println("columns: " + colSize);
                 break;
             default:
             	System.exit(0);
             	break;
         }    	
     }
     
     // prompts user for time per turn
     void askTimePerTurn() {
     	//JPanel panel = new JPanel();
     	try {
     		String value = JOptionPane.showInputDialog(null, "Enter a time limit per turn (milliseconds): (If no time limit is wanted, just leave blank.)", "", JOptionPane.PLAIN_MESSAGE);
     		if(value == null)
     			System.exit(0);
     		timePerTurn = Integer.parseInt(value);
     	} catch(NumberFormatException e) {}
     }
     
     void changeName() {//{{{
         playerName = JOptionPane.showInputDialog(null, "Enter player name: ", "", JOptionPane.PLAIN_MESSAGE);
         if(playerName != null)
         	messageBox.setText("Player name is: " + playerName);
     }//}}}
 
     void instructions() {//{{{
         String instructionDialog = "From Wikipedia:\n" +
                 "* Players alternate turns, starting with White.\n" +
                 "* We distinguish two kinds of moves, non-capturing and capturing moves. A non-capturing move is called a paika move.\n" +
                 "* A paika move consists of moving one stone along a line to an adjacent intersection.\n" +
                 "* Capturing moves are obligatory and have to be played in preference to paika moves.\n" +
                 "* Capturing implies removing one or more pieces of the opponent. It can be done in two ways, either (1) by approach or (2) by withdrawal.\n" +
                 "  (1) An approach is moving the capturing stone to a point adjacent to an opponent stone provided that the stone is on the continuation of the capturing stone's movement line.\n" +
                 "  (2) A withdrawal works analogously to an approach except that the movement is away from the opponent stone.\n" +
                 "* When an opponent stone is captured, all opponent pieces in line behind that stone (as long as there is no interruption by an empty point or an own stone) are captured as well.\n" +
                 "* If a player can do an approach and a withdrawal at the same time, he has to choose which one he plays.\n" +
                 "* As in checkers, the capturing piece is allowed to continue making successive captures, with these restrictions:\n" +
                 "  (1) The piece is not allowed to arrive at the same position twice.\n" +
                 "  (2) It is not allowed to move a piece in the same direction as directly before in the capturing sequence. This can happen if an approach follows on a withdrawal.\n" +
                 "* The game ends when one player captures all stones of the opponent. If neither player can achieve this, the game is a draw.\n";
         JOptionPane.showMessageDialog(this, instructionDialog, "Fanorona Instructions", JOptionPane.PLAIN_MESSAGE);
     }//}}}
     
     public class Clock {
         private Timer timer = new Timer();
         private int timeRemaining;
         private boolean timerOff;
 
         public Clock(int time) {
         	timeRemaining = time;
         	if(time <= 0)
         		timerOff = true;
         	else
         		timerOff = false;
         }
 
         private class UpdateUITask extends TimerTask {
             @Override
             public void run() {
                 EventQueue.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                     	if(timeRemaining <= 0) {
                     		System.out.println("LOSER");
                     		timer.cancel();
                     	}
                         timerBox.setText("Time left for turn (seconds): " + String.valueOf(timeRemaining--));
                     }
                 });
             }
         }
         
         public void startTimer() {
         	if(timerOff)
         		timerBox.setText("Time left for turn (seconds): OFF");
         	else
         		timer.schedule(new UpdateUITask(), timeRemaining, 1000);
         }
     }
 }
