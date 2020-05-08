 package View;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 import GameEngine.Player.PlayerType;
 
 /**
  * Project - TowerDefense</br>
  * <b>Class - GameMenubar</b></br>
  * <p>The GameMenubar class is used to display a user friendly interface while playing the game.
  * The player would be allowed to make actions thanks to buttons and informations hold in the
  * GameMenuBar.
  * The GameMenuBar is created and managed by the ViewManager.</br>
  * </p> 
  * <b>Creation :</b> 22/04/2013</br>
  * @author K. Akyurek, A. Beauprez, T. Demenat, C. Lejeune - <b>IMAC</b></br>
  * @see ViewManager
  */
 
 @SuppressWarnings("serial")
 public class GameMenuBar extends MainViews{
     
     private JButton jButtonBack;
     private JLabel jInfo;
     /**
      * Constructor of the GameMenuBar class
      * @param view
      * @param position
      * @param width
      * @param height
      */
 	public GameMenuBar(ViewManager view, Point position, int width, int height){
 		super(view, position, width,height);
 		
 		//Creating the components
 		jButtonBack = new javax.swing.JButton();
		jInfo= new JLabel();
 		jInfo.setForeground(Color.darkGray);
 		
 		//Setting the components parameters and theirs listeners        
 		jButtonBack.setText("Surrender");
 		jButtonBack.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent evt) {
 		        jButtonBackPerformed(evt);
 		    }
 		});
 		
 		//Laying the components on the Panel
 		setLayout(null);
 		setBackground(Color.lightGray);
 		
 		jButtonBack.setBounds(0, 0, 125,25);
 		jInfo.setBounds(130, 0, 675,25);
 		add(jButtonBack);
 		add(jInfo);
 	}
 	
 	/**
 	 * jButtonBack Event handler - Back to the home menu !
 	 * @param evt - ActionEvent performed by the player
 	 */
     private void jButtonBackPerformed(ActionEvent evt) {
     	view.homeMenu();
     }
     
 	/**
 	 * 
 	 * @param text
 	 */
 	public void setInfo(String text){
 		jInfo.setText(text);	
 	}
 }
