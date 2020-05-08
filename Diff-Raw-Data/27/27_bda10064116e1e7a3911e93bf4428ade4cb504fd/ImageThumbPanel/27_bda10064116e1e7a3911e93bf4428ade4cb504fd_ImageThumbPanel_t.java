 package gui;
 
 import images.ImageTag;
 import images.ImageThumbnail;
 import images.TaggableImage;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  * Panel to render a thumbnail image with corresponding filename shown below
  * Also displays the tag of the image
  * @author mccannjame
  * jm 080912
  */
 public class ImageThumbPanel extends JPanel {
 	
 	private ImageThumbnail image;
 	private ImageTag tag;
 	private String fileName;
 	private boolean selected;
 
 	public ImageThumbPanel(TaggableImage ti, Dimension dim) {
 		//image = new ImageThumbnail(ti, new Dimension(dim.width-10, dim.height-20));
 		//tag = ti.getTag();
 		fileName = "testing.jpg"; //TODO replace with actual filenames jm 080512
 		initLayout(dim);
 	}
 	
 	public void initLayout(Dimension dim) {
		setPreferredSize(dim);
 		setLayout(new FlowLayout());
 		
 		//image label TODO replace with actual ImageThumbail rendered image
 		JLabel imageLabel = new JLabel();
 		imageLabel.setOpaque(true);
 		imageLabel.setForeground(Color.BLUE);
 		imageLabel.setBackground(Color.CYAN);
		imageLabel.setPreferredSize(new Dimension(dim.width-10, dim.height-20)); //temporary
 		add(imageLabel);
 		
 		//add string and tag below image
		JLabel nameLabel = new JLabel();
		nameLabel.setText(fileName);
		add(nameLabel);
 	}
 	
 	public void toggleSelected() { selected = !selected; }
 
 }
