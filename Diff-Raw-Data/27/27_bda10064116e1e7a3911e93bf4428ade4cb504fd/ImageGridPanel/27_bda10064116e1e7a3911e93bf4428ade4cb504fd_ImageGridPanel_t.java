 package gui;
 import images.TaggableImage;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 
 public class ImageGridPanel extends JPanel {
 
 	private static final Dimension gridPanelSize = new Dimension(100, 100);
 	private List<TaggableImage> images;
 	private TaggableImage selectedImage;
 
 	public ImageGridPanel(File[] imagePaths) {
 		//TODO fill with actual images
		images = new ArrayList<TaggableImage>();
		initialise();
 	}
 
 	public void initialise() {
 		setLayout(new GridLayout(0, 2));
 		ImageThumbPanel itp;
 		if(images != null){
			for(int i = 0; i < 30; i++){
 				//JLabel l = new JLabel(new ImageIcon(f.getAbsolutePath()));
 				//l.setBackground(new Color(0, 0, 255));
 				//l.setPreferredSize(new Dimension(100, 100));
 				//leftPanel.add(l);
 				
 				itp = new ImageThumbPanel(null, gridPanelSize);
 				add(itp);
 			}
 		}
 	}
 }
