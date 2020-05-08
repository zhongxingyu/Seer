 package core;
 
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 
 public class GamePlayground extends GameObjects implements IGamePlayground
 {
 	protected JFrame frame						= new JFrame();
 
 	public GamePlayground()
 	{
 		// Fenster schliessen
 		GamePlayground.this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 	}
 
 	public void closePlayground()
 	{		
 		GamePlayground.this.frame.setVisible( false );
		GamePlayground.this.game.closeEditor();
 	}
 	
	public void showPlayground() 
 	{
 		this.frame.setVisible( true );
 	}
 }
