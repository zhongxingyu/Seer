 /**
  * 
  */
 
 import java.awt.Graphics;
 import java.awt.Panel;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 import loaders.ILoader;
 import loaders.SamDirLocalLoader;
 
 import brahma.host.GUIPlugin;
 
 /**
  * @author risdenkj
  *
  */
 public class StaticPictureViewer extends GUIPlugin implements MouseListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private ArrayList<ILoader> loaders;
 	private BufferedImage image;
 	private Random randomGenerator;
 	
 	public StaticPictureViewer() throws Exception {
 		super();
 		this.randomGenerator = new Random();
 		this.loaders = new ArrayList<ILoader>();
 		this.loaders.add(new SamDirLocalLoader());
 		if(this.loaders.size() == 0) {
 			throw new Exception("No Picture Loaders Found");
 		}
 		this.image = this.getLoader().getImage();
 		addMouseListener(this);
 	}
 	
 	public ILoader getLoader() {
 		return this.loaders.get(this.randomGenerator.nextInt(this.loaders.size()));
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		this.image = this.getLoader().getImage();
 		this.repaint();
 	}
 	
 	public void paint(Graphics g) {
 		int newImageHeight = this.image.getHeight();
 		int newImageWidth = this.image.getWidth();
 		if (this.image.getHeight() >= this.image.getWidth() && this.image.getHeight() > getHeight()) {
 			newImageHeight = getHeight();
 			newImageWidth = Math.round((getHeight()/((float)this.image.getHeight()))*this.image.getWidth());
 		} else if(this.image.getHeight() < this.image.getWidth() && image.getWidth() > getWidth()) {
 			newImageHeight = Math.round((getWidth()/((float)this.image.getWidth()))*this.image.getHeight());
 			newImageWidth = getWidth();
 		}
 		g.drawImage(this.image, getWidth()/2-newImageWidth/2, getHeight()/2-newImageHeight/2, newImageWidth, newImageHeight, null);
 	}
 
 	@Override
 	public Boolean start() {
 		JFrame frame = new JFrame("Display image");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Panel panel;
 		try {
			panel = new StaticPictureViewer();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		frame.setSize(500, 500);
 		frame.getContentPane().add(panel);
 		frame.setVisible(true);
 		return true;
 	}
 	
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
             public void run() {
 				try {
 					StaticPictureViewer pv = new StaticPictureViewer();
 					pv.start();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
             }
         });
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 }
