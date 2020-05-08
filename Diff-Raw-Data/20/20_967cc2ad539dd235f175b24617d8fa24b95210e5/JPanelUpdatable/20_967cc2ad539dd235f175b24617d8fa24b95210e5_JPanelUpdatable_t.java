 package MusicCrawler;
 
 import java.awt.Color;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 
 public class JPanelUpdatable extends JPanel implements Observer
 {
 
 	/**
 	 * 
 	 */
 	
 	JPanelUpdatable()
 	{
		//this.setBackground(Color.BLACK);
		//this.setOpaque(true);
 	}
	
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public void update(Observable o, Object arg) 
 	{
 		// TODO Auto-generated method stub
 		if(arg instanceof JCheckBox)
 		{
 			this.add((JCheckBox) arg);
 		}
 	}
 	
 	
 
 }
