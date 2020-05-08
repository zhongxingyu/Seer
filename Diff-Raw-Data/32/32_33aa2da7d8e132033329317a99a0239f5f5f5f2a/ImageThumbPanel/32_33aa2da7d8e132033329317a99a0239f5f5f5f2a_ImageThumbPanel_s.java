 package gui;
 
 import images.ImageTag;
 import images.TaggableImage;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 /**
  * Panel to render a thumbnail image with corresponding filename shown below.
  * Also displays the tag of the image.
  * @author mccannjame
  * jm 080912
  */
 public class ImageThumbPanel extends JPanel implements MouseListener {
 	private static final long serialVersionUID = 1L;
 	
 	private ImageGridPanel parent;
 	private TaggableImage image;
 	private String fileName;
 	private boolean selected;
 	JLabel imageLabel = new JLabel();
 	JLabel flagLabel;
 	JLabel deleteLabel;
 	/**
 	 * Constructor for the ImageThumbPanel
 	 * @param ti - TaggableImage 
 	 * @param dim - Dimension
 	 * @param parent - ImageGridPanel
 	 */
 	public ImageThumbPanel(TaggableImage ti, Dimension dim, ImageGridPanel parent) {
 		image = ti; // new ImageThumbnail(ti, new Dimension(dim.width-10, dim.height-20));
 		//tag = ti.getTag();
 		if(ti != null) fileName = ti.getFileName();
 		this.parent = parent;
 		initLayout(dim);
 	}
 
 	@Override
 	public String toString() {
 		return "ImageThumbPanel [fileName=" + fileName + ", selected="
 				+ selected + "]";
 	}
 	
	/**
	 * Get the image
	 * @return TaggableImage
	 */
 	public TaggableImage getImage() { return image; }
	
	/**
	 * Set the select 
	 * @param selected - boolean
	 */
 	public void setSelected(boolean selected) { this.selected = selected; }
 	
 	/**
 	 * Get the image label
 	 * @return JLabel
 	 */
 	public JLabel imageLabel(){ return imageLabel; } 
 	/**
 	 * Initialize the panel
 	 * @param dim - dimension
 	 */
 	public void initLayout(Dimension dim) {
 		setPreferredSize(dim);
 		setSize(dim);
 		setMaximumSize(dim);
 		setLayout(new BorderLayout());
 		
 		if(image != null ){
 			imageLabel = new JLabel(new ImageIcon(image.getImage(dim.width-10, -1)));
 			imageLabel.setPreferredSize(new Dimension(dim.width-10, dim.height-30));
 			imageLabel.setSize(new Dimension(dim.width-10, dim.height-30));
 		}
 		add(imageLabel, BorderLayout.CENTER);
 
 		//add string and tag below image
 		JPanel labelPanel = new JPanel();
 		labelPanel.setLayout(new FlowLayout());
 		JLabel nameLabel = new JLabel(fileName);
 		nameLabel.setPreferredSize(new Dimension(getSize().width, 20));
 		labelPanel.add(nameLabel);
 		flagLabel = new JLabel(new ImageIcon("lib/flag20.png"));
 		labelPanel.add(flagLabel);
 		if (image!=null) {
 			deleteLabel = new JLabel(new ImageIcon("lib/delete-btn-img.png"));
 			labelPanel.add(deleteLabel);
 			deleteLabel.addMouseListener(this);
 		}
 		flagLabel.setVisible(image==null?false:image.getTag()==ImageTag.INFRINGEMENT);
 		add(labelPanel, BorderLayout.SOUTH);
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		flagLabel.setVisible(image==null?false:image.getTag()==ImageTag.INFRINGEMENT);
 		super.paint(g);
 	}
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove the image " + image.getFileName() + "?");
 		if (confirm==JOptionPane.OK_OPTION) { 
 			parent.removeImage(this);
 		}
 	}
 	public void mousePressed(MouseEvent e) {}
 	public void mouseReleased(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	
 }
