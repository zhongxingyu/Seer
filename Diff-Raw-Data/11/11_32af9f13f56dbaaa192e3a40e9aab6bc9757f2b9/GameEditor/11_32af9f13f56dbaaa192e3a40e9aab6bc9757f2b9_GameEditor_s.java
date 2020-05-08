 package core;
 
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 
 public class GameEditor extends GameObjects implements IGameEditor 
 {
 	protected JFrame frame						= new JFrame();
 
 	public GameEditor()
 	{
 		// Fenster schliessen
 		GameEditor.this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 	}
 
 	public void closeEditor()
 	{		
 		GameEditor.this.frame.setVisible( false );
		GameEditor.this.game.closeEditor();
 	}
 	
 	public void showEditor() 
 	{
 		this.frame.setVisible( true );
 	}
 }
