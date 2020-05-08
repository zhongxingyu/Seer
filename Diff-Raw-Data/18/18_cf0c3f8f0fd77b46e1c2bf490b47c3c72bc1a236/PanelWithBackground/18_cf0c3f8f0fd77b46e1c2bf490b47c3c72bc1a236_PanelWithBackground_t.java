 package mensonge.userinterface;
 
 import java.awt.Graphics;
import java.awt.LayoutManager;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 public class PanelWithBackground extends JPanel
 {
 	private static final long serialVersionUID = 4969722390664163943L;
 	private BufferedImage image;
 
	public PanelWithBackground(LayoutManager layout)
 	{
		super(layout);
 		try
 		{
 			image = ImageIO.read(new File("images/LieLabLogo.png"));
 		}
 		catch (IOException e)
 		{
 			GraphicalUserInterface.popupErreur(e.getMessage());
 		}
 	}
 
 	@Override
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		g.drawImage(image, 0, this.getHeight()-image.getHeight(), this);
 	}
 }
