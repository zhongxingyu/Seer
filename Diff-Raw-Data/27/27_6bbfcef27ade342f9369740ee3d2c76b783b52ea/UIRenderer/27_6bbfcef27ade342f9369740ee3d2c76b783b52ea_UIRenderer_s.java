 package renderer;
 
 
 import java.awt.Container;
 import java.awt.MediaTracker;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.SwingUtilities;
 
 public class UIRenderer {
 
 	private final int UI_LAYER = 200;
 	private JLabel winnerBackground;
 	private JLabel loserBackground;
 	private JLabel radialMenuMovment;
 	private Container container;
 	private int width;
 	private int height;
 	/**
 	 * Represent the state of UIRenderer during unit to send choice. 0 = not choosing, 1 = choosing, 2 = already chosen
 	 */
 	private static int choosingUnitFlag = 0;
 	private static double unitPercentChosen = 0.5;
 	
 	public UIRenderer(Container c, int width, int height){
 		super();
 		this.winnerBackground = new JLabel();
 		this.loserBackground = new JLabel();
 		this.radialMenuMovment = new JLabel();
 		this.container = c;
 		this.height=height;
 		this.width=width;
 	}
 	
 	public void init() throws IOException{
 		//Load the winner background image
 		ImageIcon bgWinnerImage = new ImageIcon("./tex/youWin.png");
 		if(bgWinnerImage.getImageLoadStatus() != MediaTracker.COMPLETE){
 			throw new IOException();
 		}		
 		this.winnerBackground.setBounds(0, 0, this.width, this.height);
 		this.winnerBackground.setIcon(bgWinnerImage);
 		
 		//Load the looser background image
 		ImageIcon bgLoserImage = new ImageIcon("./tex/youLose.png");
 		if(bgLoserImage.getImageLoadStatus() != MediaTracker.COMPLETE){
 			throw new IOException();
 		}
 		this.loserBackground.setBounds(0, 0, this.width, this.height);
 		this.loserBackground.setIcon(bgLoserImage);
 		
 		//Load the menu image
 		ImageIcon rmImage = new ImageIcon("./tex/radialmenu_movment.png");
 		if(rmImage.getImageLoadStatus() != MediaTracker.COMPLETE){
 			throw new IOException();
 		}
 		this.radialMenuMovment.setIcon(rmImage);
 		this.radialMenuMovment.setSize(rmImage.getIconWidth(), rmImage.getIconHeight());
 		
 		//Manage events
 		this.radialMenuMovment.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseReleased(MouseEvent arg0) {}
 			@Override
 			public void mousePressed(MouseEvent arg0) {}
 			@Override
 			public void mouseExited(MouseEvent arg0) {}
 			@Override
 			public void mouseEntered(MouseEvent arg0) {}
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				UIRenderer.choosingUnitFlag = 2;
 				JLabel radialMenu = (JLabel) arg0.getComponent();
 				Point rmPosition = new Point(radialMenu.getWidth()/2, radialMenu.getHeight()/2);
 				Point mousePosition = arg0.getPoint();
 				
 				if(rmPosition.distance(mousePosition) < 10){ //click on the center 50%
 					UIRenderer.unitPercentChosen = 0.5;
 				}else{
 					if(mousePosition.x > mousePosition.y && radialMenu.getWidth()-mousePosition.x > mousePosition.y){ //click on top quarter 50%
 						UIRenderer.unitPercentChosen = 0.5;
 					}else if(mousePosition.x > mousePosition.y && radialMenu.getWidth()-mousePosition.x < mousePosition.y){ //click on right quarter 75%
 						UIRenderer.unitPercentChosen = 0.75;						
 					}else if(mousePosition.x < mousePosition.y && radialMenu.getWidth()-mousePosition.x > mousePosition.y){ //click on left quarter 25%
 						UIRenderer.unitPercentChosen = 0.25;
 					}else if(mousePosition.x < mousePosition.y && radialMenu.getWidth()-mousePosition.x < mousePosition.y){ //click on bottom quarter 99%
 						UIRenderer.unitPercentChosen = 1.;						
 					}
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Display a "WINNER" message when the player win and before exit program
 	 */
 	public void displayWinner(){
 		this.container.add(this.winnerBackground, new Integer(UI_LAYER));
 	}
 	
 	/**
 	 * Display a "LOSER" message when the player lose and before exit program
 	 */
 	public void displayLoser(){
 		this.container.add(this.loserBackground, new Integer(UI_LAYER));
 	}
 	
 	/**
 	 * Display or hide a radial menu to choose how many units send 
 	 */
 	public void refreshRadialMenuMovment(){
 		switch(UIRenderer.choosingUnitFlag){
 			//if the player don't choose yet
 			case 0:
 				if(SelectedSprite.isThereAtLeastOneStartingElement() && SelectedSprite.isThereAnEndingElement()){
 					UIRenderer.choosingUnitFlag = 1;
 					Point mousePosition = MouseInfo.getPointerInfo().getLocation();
 					SwingUtilities.convertPointFromScreen(mousePosition, this.container);
 					mousePosition.x -= this.radialMenuMovment.getWidth()/2;
 					mousePosition.y -= this.radialMenuMovment.getHeight()/2;
 					this.radialMenuMovment.setLocation(mousePosition);
 				}
 				break;
 			
 			//if the player is choosing
 			case 1:
 				if(this.radialMenuMovment.getParent() == null){
 					this.container.add(this.radialMenuMovment, new Integer(UI_LAYER));
 				}
 				break;
 			
 			//if the player have just chosen
 			case 2:
 				this.container.remove(this.radialMenuMovment);
 				UIRenderer.choosingUnitFlag = 0;
 				break;
 				
 			default:
 				break;
 		}
 	}
 	
 	/**
 	 * Hide the radial menu and re-initialize the choice
 	 */
 	public void hideRadialMenuMovment(){
 		UIRenderer.choosingUnitFlag = 0;
 		if(this.radialMenuMovment.getParent() != null){
 			this.container.remove(this.radialMenuMovment);
 		}
 	}
 	
 	public boolean isChoosingUnitFlag(){
 		if(UIRenderer.choosingUnitFlag == 1){
 			return true;
 		}
 		return false;
 	}
 	
 	public double getUnitPercentChosen(){
 		return UIRenderer.unitPercentChosen;
 	}
 }
 
 
 
