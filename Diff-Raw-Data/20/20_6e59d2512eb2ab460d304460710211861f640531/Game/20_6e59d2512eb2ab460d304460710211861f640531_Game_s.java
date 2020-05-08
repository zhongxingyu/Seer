 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.security.Key;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 
 public class Game{
 	
 	private static final String GAMEFILE = "TestRoom.txt";
 	
 	private Dungeon dungeon;
 	private MainWin window;
 	private CommandCenter2 controller;
 	
 	
 	public Game(){
 		dungeon = null;
 		window = null;
 		controller = null;
 	}
 	
 	public Game(Dungeon dungeon){
 		this.dungeon = dungeon;
 		window = new MainWin();
 		controller = new CommandCenter2(this);
 		
 	}
 	
 	public void begin()
 	{
 		
 		setWindowText(dungeon.getEntryRoom().getDescription());
 		window.setVisible(true);
<<<<<<< HEAD
 		window.addWindowListener(new WindowAdapter() {
 			public void windowOpened(WindowEvent e){
 				window.getInputArea().requestFocus();
 				window.getInputArea().selectAll();
 			}
 		});
 	
 		window.getInputArea().addActionListener(new ActionListener() {
=======
		window.getInputArea().addActionListener(new ActionListener() 
		{
>>>>>>> branch 'master' of git@github.com:trudeaujt/CSCD454Final.git
			
 			@Override
			public void actionPerformed(ActionEvent e) 
			{
 				controller.executeCommand(window.getInputArea().getText());
 			}
 		});
<<<<<<< HEAD
 		
=======
 	}
 	public String toString(){
 		return "";
>>>>>>> branch 'master' of git@github.com:trudeaujt/CSCD454Final.git
 	}
 	
 	public void setWindowText(String str){
 		window.setOutput(str);
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
