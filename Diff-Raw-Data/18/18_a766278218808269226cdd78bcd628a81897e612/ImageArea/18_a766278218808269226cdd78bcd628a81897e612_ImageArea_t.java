 import javax.swing.JPanel;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.Dimension;
 import java.awt.event.*;
 import java.io.File;
 import javax.imageio.ImageIO;
 import java.awt.Container;
 
 class ImageArea extends JPanel {
 	ImageFrame mainFrame;
 	BufferedImage image;
 	BufferedImage bufferedImage;
 	Point startSelectionPoint;
 	Point stopSelectionPoint;
 	Point currentPoint;
 	boolean imageIsLoaded;
 	boolean showSelectingArea;
 
 	ImageArea(ImageFrame mainFrame) {
 		this.mainFrame = mainFrame;
 		setImageStatusAsNotLoaded();
 		showSelectingArea(false);
 		setZiroInitialSize();
 		setupMouseMotion();
 	}
 
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		if (imageIsLoaded) {
 			g.drawImage(image, 0, 0, null);
 			if (showSelectingArea) {
 				g.setColor(Color.BLACK);
 				drawSelectingArea(g);
 			}
 		}
 	}
 
 	private void drawSelectingArea(Graphics g) {
 		g.drawLine((int)startSelectionPoint.getX(), (int)startSelectionPoint.getY(),
 			(int)currentPoint.getX(), (int)startSelectionPoint.getY());
 		g.drawLine((int)currentPoint.getX(), (int)startSelectionPoint.getY(),
 			(int)currentPoint.getX(), (int)currentPoint.getY());
 		g.drawLine((int)currentPoint.getX(), (int)currentPoint.getY(),
 			(int)startSelectionPoint.getX(), (int)currentPoint.getY());
 		g.drawLine((int)startSelectionPoint.getX(), (int)currentPoint.getY(),
 			(int)startSelectionPoint.getX(), (int)startSelectionPoint.getY());
 	}
 
 	public void setImage(BufferedImage image) {
 		this.setSize(image.getWidth(null), image.getHeight(null));
 		this.setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
 		this.image = image;
 		imageIsLoaded = true;
 		this.repaint();
 	}
 
 	public void saveImage(File file) {
 		try {
 			ImageIO.write(image, "bmp", file);
 		} catch (java.io.IOException ioe) {
 			System.out.println("Unable save file");
 		}
 	}
 
 	private void setImageStatusAsNotLoaded() {
 		imageIsLoaded = false;
 	}
 
 	private void setZiroInitialSize() {
 		this.setPreferredSize(new Dimension(0, 0));
 	}
 
 	private void setupMouseMotion() {
 		this.addMouseListener(new MouseListener() {
 			public void mousePressed(MouseEvent event) {
 				startSelect(event.getPoint());
 			}
 
 			public void mouseReleased(MouseEvent event) {
 				stopSelect(event.getPoint());
 			}
 
 			public void mouseClicked(MouseEvent e) {}
 			public void mouseEntered(MouseEvent e) {}
 			public void mouseExited(MouseEvent e) {}
 		});
 
 		this.addMouseMotionListener(new MouseMotionListener() {
 			public void mouseDragged(MouseEvent event) {
 				draggMouse(event.getPoint());
 			}
 
 			public void mouseMoved(MouseEvent event) {}
 		});
 	}
 
 	private void showSelectingArea(boolean status) {
 		showSelectingArea = status;
 	}
 
 	private void startSelect(Point point) {
 		this.startSelectionPoint = point;
 		this.stopSelectionPoint = point;
 		this.currentPoint = point;
 		showSelectingArea(true);
 		repaint();
 	}
 
 	private void stopSelect(Point point) {
 		this.stopSelectionPoint = point;
 		point = this.normalizePoint(point);
 		this.revertPoints();
 		if (this.stopSelectionPoint.x == this.startSelectionPoint.x && this.stopSelectionPoint.y == this.startSelectionPoint.y) {
 			showSelectingArea(false);
 		}
 		this.repaint();
 	}
 
 	private Point normalizePoint(Point point) {
 		if (point.x >= image.getWidth(null))
 			point.x = image.getWidth(null) - 1;
 		if (point.y >= image.getHeight(null))
 			point.y = image.getHeight(null) - 1;
 		if (point.x < 0)
 			point.x = 0;
 		if (point.y < 0)
 			point.y = 0;
 		return point;
 	}
 
 	private void revertPoints() {
 		if (stopSelectionPoint.x < startSelectionPoint.x) {
 			int tmp = startSelectionPoint.x;
 			startSelectionPoint.x = stopSelectionPoint.x;
 			stopSelectionPoint.x = tmp;
 		}
 		if (stopSelectionPoint.y < startSelectionPoint.y) {
 			int tmp = startSelectionPoint.y;
 			startSelectionPoint.y = stopSelectionPoint.y;
 			stopSelectionPoint.y = tmp;
 		}
 		currentPoint = stopSelectionPoint;
 	}
 
 	private void draggMouse(Point point) {
 		this.currentPoint = point;
 		point = this.normalizePoint(point);
 		this.repaint();
 	}
 
 	public void cut() {
 		copy();
 		for(int y = startSelectionPoint.y; y <= stopSelectionPoint.y; y++) {
 			for (int x = startSelectionPoint.x; x <= stopSelectionPoint.x; x++) {
 				image.setRGB(x, y, new Color(200, 200, 200).getRGB());
 			}
 		}
 		showSelectingArea(false);
 		this.repaint();
 	}
 
 	public void copy() {
 		int width = stopSelectionPoint.x - startSelectionPoint.x + 1;
 		int height = stopSelectionPoint.y - startSelectionPoint.y + 1;
 		int[] rgbArray = new int[width * height];
 		image.getRGB(startSelectionPoint.x, startSelectionPoint.y,
 			width, height, rgbArray, 0, width);
 		bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 		bufferedImage.setRGB(0, 0, width, height, rgbArray, 0, width);
 		((ImageProcessor) this.getParent("ImageProcessor")).remember(bufferedImage);
 		showSelectingArea(false);
 	}
 
 	private Container getParent(String className) {
 		Container container = this;
 		do {
 			container = container.getParent();
 		} while (container.getClass().getName() != className);
 		return container;
 	}
 
 	public void paste(BufferedImage bufferedImage) {
		int bufferWidth = bufferedImage.getWidth(null);
		int bufferHeight = bufferedImage.getHeight(null);
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		if (bufferWidth + startSelectionPoint.x > width)
			bufferWidth -= startSelectionPoint.x + bufferWidth - width;
		if (bufferHeight + startSelectionPoint.y > height)
			bufferHeight -= startSelectionPoint.y + bufferHeight - height;
		for (int y = 0; y < bufferHeight; y++) {
			for (int x = 0; x < bufferWidth; x++) {
				image.setRGB(startSelectionPoint.x + x, startSelectionPoint.y + y, bufferedImage.getRGB(x, y));
			}
		}
 		showSelectingArea(false);
 		this.repaint();
 	}
 
 	public void rotateImage(int angle) {
 		for (int i = 0; i < Math.floor(angle / 90); i++) {
 			int width = image.getWidth(null);
 			int height = image.getHeight(null);
 			BufferedImage tmpBufImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);
 			for (int y = 0; y < height; y++) {
 				for (int x = 0; x < width; x++) {
 					tmpBufImage.setRGB(height - y - 1, x, image.getRGB(x, y));
 				}
 			}
 			image = tmpBufImage;
 			this.setSize(image.getWidth(null), image.getHeight(null));
 			this.setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
 			mainFrame.changeImagePanelSize(image.getWidth(null), image.getHeight(null));
 		}
 		this.repaint();
 	}
 
 	public void getGrayImage() {
 		int red;
 		int green;
 		int blue;
 		int gray;
 
 		for (int y = 0; y < image.getHeight(null); y++) {
 			for (int x = 0; x < image.getWidth(null); x++) {
 				Color tempColor = new Color(image.getRGB(x, y));
 				red = tempColor.getRed();
 				green = tempColor.getGreen();
 				blue = tempColor.getBlue();
 				gray = (int)((red + green + blue) / 3);
 				image.setRGB(x, y, new Color(gray, gray, gray).getRGB());
 			}
 		}
 		this.repaint();
 	}
 }
