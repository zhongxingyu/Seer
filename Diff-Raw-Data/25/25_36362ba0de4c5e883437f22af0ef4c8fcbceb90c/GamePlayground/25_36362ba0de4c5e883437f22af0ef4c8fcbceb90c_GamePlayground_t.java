 package core;
 
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 
 public class GamePlayground extends GameObjects implements IGamePlayground
 {
 	protected JFrame frame						= new JFrame();
 
 	public GamePlayground()
 	{
 		// Fenster schliessen
 		GamePlayground.this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		GamePlayground.this.frame.addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent e) { GamePlayground.this.closePlayground(); }
		});		
 	}
 
 	public void closePlayground()
 	{		
 		GamePlayground.this.frame.setVisible( false );
		GamePlayground.this.game.openGameGUI();
 	}
 	
	public void openPlayground() 
 	{
 		this.frame.setVisible( true );
 	}
 }
