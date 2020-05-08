 package gui;
 import images.ImageTag;
 import images.TaggableImage;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 
 public class ImageGridPanel extends JPanel implements MouseListener{
 
 	private static final long serialVersionUID = 1L;
 	private static final Dimension gridPanelSize = new Dimension(100, 100);
 	private List<TaggableImage> images;
 	private List<ImageThumbPanel> imageThumbPanels;
 	private TaggableImage selectedImage;
 	private Canvas canvas;
 	private JPanel gridbox;
 	private JLabel importedLabel;
 	private ApplicationWindow window;
 	//private TaggableImage selectedImage;
 
 	public ImageGridPanel(List<TaggableImage> imageList, ApplicationWindow window) {
 		this.window = window;
 		if(imageList == null || imageList.size()==0)
 			selectedImage=null;
 		else
 			selectedImage = imageList.get(0);
 		imageThumbPanels = new ArrayList<ImageThumbPanel>();
 		images = imageList;
 		setLayout(new BorderLayout());
 		initialise();
 	}
 	
 	public void setCanvas(Canvas c){
 		this.canvas = c;
 	}
 	
 	public void setImageList(List<TaggableImage> newImageList) {
 		images = newImageList;
 	}
 
 	public void initialise() {
 		//trash the existing contents of the image panel if there
 		//are already images (on a reload)
 		removeAll();
 		
 		if(images==null || images.isEmpty()){
 			//paint the placeholder image to fill box
 			//called via paintComponent jm 190812
 			return;
 		}
 		
 		importedLabel = new JLabel("Imported Images: "+images.size()+" images.");
 		add(importedLabel,BorderLayout.NORTH);
 		
 		gridbox = new JPanel();
 		gridbox.setLayout(new GridLayout(0, 2));
 		
 		
 		//Debug:
 		System.out.println("initialise imageGrid");
 		ImageThumbPanel itp;
 		if(images != null){
 			imageThumbPanels.clear();
 			for(TaggableImage timg: images){
 				itp = new ImageThumbPanel(timg, gridPanelSize);
 				imageThumbPanels.add(itp);
 				itp.addMouseListener(this);
 				itp.addMouseListener(window);
 				gridbox.add(itp);
 			}
 			if(images.size()<10){
 				for(int i = images.size();i<10;i++){
 					itp = new ImageThumbPanel(null, gridPanelSize);
 					imageThumbPanels.add(itp);
 					gridbox.add(itp);
 				}
 			}
 		}
 		add(gridbox, BorderLayout.CENTER);
 		revalidate();
 		repaint();
 	}
 	
 	//jm 190912
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		if (images == null || images.isEmpty()) {
 			//paint the placeholder image
 			Graphics2D g2d = (Graphics2D)g;
 			BufferedImage placeholder = ApplicationWindow.IMPORT_PLACEHOLDER;
 			Image scaledPlaceholder = placeholder.getScaledInstance(this.getWidth(), placeholder.getHeight(null), Image.SCALE_SMOOTH);
 			int x = (this.getWidth() - scaledPlaceholder.getWidth(null)) / 2;
 			int y = (this.getHeight() - scaledPlaceholder.getHeight(null)) / 2;
 			g2d.setColor(new Color(153, 157, 158)); //placeholder background
 			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
 			g2d.drawImage(scaledPlaceholder, x, y, null);
 		}
 	}
 			
 	public void browse(String direction) {
 		if(selectedImage == null) return;
		
 		for(int i=0; i<imageThumbPanels.size(); i++) {
 			ImageThumbPanel current = imageThumbPanels.get(i);
 			
			if(current.getImage().getFileName().equals(selectedImage.getFileName())) {
 				if(direction.equals("previous") && i!=0) {
 					ImageThumbPanel previous = imageThumbPanels.get(i-1);
 					current.setSelected(false);
 					current.imageLabel().setBorder(null);
 					previous.setSelected(true);
 					setSelectedImage(previous.getImage());
 					previous.imageLabel().setBorder(BorderFactory.createLineBorder(Color.red, 3));
				}
				else if(direction.equals("next") && i!=images.size()-1) {
 					ImageThumbPanel next = imageThumbPanels.get(i+1);
 					current.setSelected(false);
 					current.imageLabel().setBorder(null);
 					next.setSelected(true);
 					setSelectedImage(next.getImage());
 					next.imageLabel().setBorder(BorderFactory.createLineBorder(Color.red, 3));
 				}
 				canvas.repaint();
 				break;
 			}
 		}
 	}
 	
 	public void update() {
 		canvas.repaint();
 	}
 	
 	public List<ImageThumbPanel> getPanels() {
 		return this.imageThumbPanels;
 	}
 
 	public void mouseClicked(MouseEvent e) {
 		for(ImageThumbPanel i : imageThumbPanels){
 			i.setSelected(false);
 			i.imageLabel().setBorder(null);
 		}
 		ImageThumbPanel clickedThumb = (ImageThumbPanel)e.getSource();
 		clickedThumb.setSelected(true);
 		clickedThumb.imageLabel().setBorder(BorderFactory.createLineBorder(Color.red, 3));
 		setSelectedImage(clickedThumb.getImage());
 		window.toggleFlagButton(selectedImage.getTag()==ImageTag.UNTAGGED);
 		canvas.repaint();
 		
 	}
 	private void setSelectedImage(TaggableImage image) {selectedImage = image;}
 	public TaggableImage getSelectedImage(){return selectedImage;}
 
 	public void mousePressed(MouseEvent e) {}
 	public void mouseReleased(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 }
