 package userInterface.view;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import main.*;
 import javax.swing.JPanel;
 
 /**
  * 	<ul>
  * 		<p>ImagePanel sets the background for any JPanel</p>
  * 		@author Daniel
  * 	</ul>
  * 
  *
  */
 public class ImagePanel extends JPanel {
 	
 	private Image image;
 	private int upperBound = 158;
 	private int leftBound = 292;
 	private String panelName;
 	/**
 	 * <!--Mutator method.-->
 	 * 	<style>
 	 * 		#method{
 	 * 			text-indent: 10.0px;
 	 * 		}
 	 * 	</style>
 	 * 	<ul><li><b>ImagePanel</b></li></ul>
 	 * 	<ul><div ID="method"><p>public ImagePanel(Image image)</p></div></ul>
 	 * 	<ul> 
 	 * 		<p>
 	 * 			Takes the given image and sets the size to the images default size. 
 	 * 		</p>
 	 * 	</ul>
 	 * 	<ul>
 	 * 		<p>
 	 * 			PreCondition: The image given must be in the proper path and the right extension.
 	 * 			<br>PostCondition: Will return the image as the new background</br>
 	 * 		</p>
 	 * 	</ul>
 	 * 
 	 * 	<ul>
 	 *  	@param image  The image to be shown as the background.
 	 *  </ul>
 	 * 
 	 * 
 	 */
 	public ImagePanel(Image image, String panelName) {
 		this.image = image;
 		this.panelName = panelName;
 		Dimension size = new Dimension(image.getWidth(null), image.getHeight(null));
 		setPreferredSize(size);
 		setMinimumSize(size);
 		setMaximumSize(size);
 		setSize(size);
 		setLayout(null);
 	}
 
 	public void paintComponent(Graphics g) {		
 		g.drawImage(image,  0 , 0 ,  null);
 
 		if (panelName == "GamePanel"){
			Piece[][] boardArray = Game.getInstance().getBoard().getArray();
 	
 			for(int row = 0; row <= 7; row++){
 				for (int column = 0; column <=7; column++){
 					if (boardArray[column][row] !=null){
 						g.fillOval(row*44+leftBound, column*44+upperBound,40,40);
 					}
 				}
 			}
 		}
 
 
 	}
 	
 	
 
 }
