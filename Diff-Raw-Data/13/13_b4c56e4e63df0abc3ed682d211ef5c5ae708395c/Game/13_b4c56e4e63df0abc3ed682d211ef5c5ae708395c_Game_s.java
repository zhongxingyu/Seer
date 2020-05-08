 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.security.Key;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 
 public class Game{
 	
	private static final String GAMEFILE = "TestRoom.txt";
 	
 	private Dungeon dungeon;
 	private MainWin window;
 	private CommandCenter2 controller;
 	private PartyComponent playerParty;
 	private Room currentRoom;
 	
 	
 	
 	public Game(){
 		dungeon = null;
 		window = null;
 		controller = null;
 		playerParty = null;
 	}
 	
 	public Game(Dungeon dungeon){
 		this.dungeon = dungeon;
 		window = new MainWin();
 		controller = new CommandCenter2(this);
 		currentRoom = dungeon.getEntryRoom();
 		
 	}
 	
 	public void begin()
 	{
 		
 		window.getInputArea().addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				controller.executeCommand(window.getInputArea().getText());
 				update();
 			}
 		});
 		window.setVisible(true);
 		window.addWindowListener(new WindowAdapter() {
 			public void windowOpened(WindowEvent e){
 				window.getInputArea().requestFocus();
 				window.getInputArea().selectAll();
 			}
 		});
 		update();
 		
 	}
 	
 	public void update(){
 		
 		postWindowText(currentRoom.getDescription());
 		window.clearInput();
 	}
 	
 	public String toString(){
 		return "";
 	}
 	
 	public void setWindowText(String str){
 		window.setOutput(str);
 	}
 	public void postWindowText(String str){
 		window.postOutput(str);
 	}
 	public void setCurrentRoom(Room room){
 		currentRoom = room;
 	}
 	public Room getCurrentRoom(){
 		return currentRoom;
 	}
 	public Room getNextRoom(Direction dir){
 		Room room = currentRoom.getConnectingRoom(dir);
 		if (room == currentRoom)
 			postWindowText("There is no way to go " + dir.toString());
 		return room;
 	}
 	
 	public static void main(String[] args){
 		
 		ArrayList<Room> rooms = new ArrayList<Room>();
 		
 		for(int i = 0; i < 5; i++){
 			rooms.add(new Room());
 		}
 		Dungeon dungeon = new Dungeon(GAMEFILE);
 		Game game = new Game(dungeon);
 		game.begin();
 	}
 	
 	
 	
 
 
 }
