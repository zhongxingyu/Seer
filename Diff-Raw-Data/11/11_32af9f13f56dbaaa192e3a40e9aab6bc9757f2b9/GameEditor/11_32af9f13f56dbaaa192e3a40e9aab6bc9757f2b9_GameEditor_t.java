 package core;
 
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 
 public class GameEditor extends GameObjects implements IGameEditor 
 {
 	protected JFrame frame						= new JFrame();
 
 	public GameEditor()
 	{
 		// Fenster schliessen
 		GameEditor.this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		GameEditor.this.frame.addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent e) { GameEditor.this.closeEditor(); }
		});		
 	}
 
 	public void closeEditor()
 	{		
 		GameEditor.this.frame.setVisible( false );
		GameEditor.this.game.openGameGUI();
 	}
 	
 	public void showEditor() 
 	{
 		this.frame.setVisible( true );
 	}
 }
