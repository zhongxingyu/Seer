 package gui;
 import images.TaggableImage;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
 
 public class ImageGridPanel extends JPanel implements MouseListener{
 
 	private static final long serialVersionUID = 1L;
 	private static final Dimension gridPanelSize = new Dimension(100, 100);
 	private List<TaggableImage> images;
 	private List<ImageThumbPanel> imageThumbPanels;
 	//private TaggableImage selectedImage;
 
 	public ImageGridPanel(List<TaggableImage> imageList) {
 		imageThumbPanels = new ArrayList<ImageThumbPanel>();
 		images = imageList;
 		setLayout(new GridLayout(0, 2));
 		initialise();
 	}
 	
 	public void setImageList(List<TaggableImage> newImageList) {
 		images = newImageList;
 	}
 
 	public void initialise() {
 		//trash the existing contents of the image panel if there
 		//are already images (on a reload)
 		removeAll();
 		
 		
 		//Debug:
 		System.out.println("initialise imageGrid");
 		ImageThumbPanel itp;
 		if(images != null){
 			imageThumbPanels.clear();
 			for(TaggableImage timg: images){
 				itp = new ImageThumbPanel(timg, gridPanelSize);
 				imageThumbPanels.add(itp);
 				itp.addMouseListener(this);
 				add(itp);
 			}
 		}
 		revalidate();
 		repaint();
 	}
 
 	public void mouseClicked(MouseEvent e) {
 		for(ImageThumbPanel i : imageThumbPanels){
 			i.setSelected(false);
 			i.imageLabel().setBorder(null);
 		}
 		((ImageThumbPanel)e.getSource()).setSelected(true);
 		((ImageThumbPanel)e.getSource()).imageLabel().setBorder(BorderFactory.createLineBorder(Color.red, 3));
 		
 		repaint();
 	}
 	public void mousePressed(MouseEvent e) {}
 	public void mouseReleased(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 }
