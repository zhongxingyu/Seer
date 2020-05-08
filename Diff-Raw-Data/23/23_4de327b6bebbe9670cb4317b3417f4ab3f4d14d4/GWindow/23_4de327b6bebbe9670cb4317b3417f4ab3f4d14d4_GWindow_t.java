 import java.awt.Container;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyListener;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.JFrame;
 
 
 
 public class GWindow implements GameEventListener{
 	/**
 	 * @param args
 	 */
 	
 	private static int width = 800;
 	private static int height = 600; 
 	
	private JFrame window;
 	public Container content;
 	
 	public GWindow(){
 		
		window = new JFrame();
 		window.setTitle("Nightmare");
 		window.setLayout(null);
 		window.setSize(width, height);
 		window.setLocationRelativeTo(null);
 		window.setResizable(false);
 		window.setVisible(true);
 		
 		window.addWindowListener(new GameWindowListener());
 		
 		menu();
 	}
 	
 	private class GameWindowListener implements WindowListener{
 		public void windowActivated(WindowEvent e){}
 		public void windowClosed(WindowEvent e){}
 		public void windowClosing(WindowEvent e){System.exit(0);}
 		public void windowDeactivated(WindowEvent e){}
 		public void windowDeiconified(WindowEvent e){}
 		public void windowIconified(WindowEvent e){}
 		public void windowOpened(WindowEvent e){}
 	}
 
 	public static int getWidth() {
 		return width;
 	}
 
 	public static int getHeight() {
 		return height;
 	}
 	
 	private void add(Container content){
 		if(this.content != null){
 			window.remove(this.content);
 		}
 		this.content = content;
 		window.add(content);
 	}
 	
 	private void menu(){
 		GMenu menu = new GMenu();
 		add(menu.getContent());
 		menu.display();
 	}
 	
 	private void gameStart(Stage stage,String hero){
 		Game game = new Game(stage,hero);
 		add(game.getContent());
 		game.display();
 	}
 	
 	
 	private void close(){
 	}
 
 	public void trigger(GameEvent event){
 		String signal = event.getSignal();
 		if(signal.equals("menu")){
 			menu();
 		}
 		else if(signal.equals("start")){
 			Object[] data = event.getData();
 			Stage stage = (Stage)data[0];
 			String hero = (String)data[1];
 			gameStart(stage,hero);
 		}
 		else if(signal.equals("restart")){
 			Object[] data = event.getData();
 			Stage stage = (Stage)data[0];
 			stage.restart();
 			String hero = (String)data[1];
 			gameStart(stage,hero);
 		}
 		else if(signal.equals("close")){
 			close();
 		}
 	}
 
 
 
 	
 }
 
