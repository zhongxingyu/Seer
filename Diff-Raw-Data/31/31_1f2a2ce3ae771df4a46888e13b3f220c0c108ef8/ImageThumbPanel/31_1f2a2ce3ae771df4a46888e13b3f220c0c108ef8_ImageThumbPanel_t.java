 package gui;
 
 import images.TaggableImage;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  * Panel to render a thumbnail image with corresponding filename shown below
  * Also displays the tag of the image
  * @author mccannjame
  * jm 080912
  */
 public class ImageThumbPanel extends JPanel {
 	
 	private static final String flag_overlay = "flag20.png";
 	private static final long serialVersionUID = 1L;
 	private TaggableImage image;
 	private String fileName;
 	private boolean selected;
 
 	@Override
 	public String toString() {
 		return "ImageThumbPanel [fileName=" + fileName + ", selected="
 				+ selected + "]";
 	}
 	
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 	
 	public JLabel imageLabel(){return imageLabel;} 
 	
 	public ImageThumbPanel(TaggableImage ti, Dimension dim) {
 		image = ti;//new ImageThumbnail(ti, new Dimension(dim.width-10, dim.height-20));
 		//tag = ti.getTag();
 		fileName = ti.getFileName();
 		initLayout(dim);
 	}
 	
 	JLabel imageLabel;
 	
 	public void initLayout(Dimension dim) {
 		
 		setPreferredSize(dim); setSize(dim); setMaximumSize(dim);
 		setLayout(new FlowLayout());
 		
 		imageLabel = new JLabel(new ImageIcon(image.getImage()));
 		imageLabel.setPreferredSize(new Dimension(dim.width-10, dim.height-30));
 		imageLabel.setSize(new Dimension(dim.width-10, dim.height-30)); 
 		add(imageLabel);
 		
 		//add string and tag below image
 		JLabel nameLabel = new JLabel();
 		nameLabel.setText(fileName);
 		add(nameLabel);
 	}
 
 	@Override
 	public void paint(Graphics g) {
 
 		super.paint(g);
 	}
 	
 	
 
 }
