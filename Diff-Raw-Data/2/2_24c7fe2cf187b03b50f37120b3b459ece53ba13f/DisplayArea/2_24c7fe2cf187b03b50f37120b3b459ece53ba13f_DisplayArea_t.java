 package ch.sfdr.fractals.gui.component;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 /**
  * The fractal display area, thread safe
  */
 public class DisplayArea
 	extends JPanel
 	implements ImageDisplay
 {
 	private static final long serialVersionUID = 1L;
 
 	private static final Color SEL_COLOR = new Color(192, 192, 192, 128);
 	private static final Color SEL_BORDER_COLOR = new Color(255, 255, 255, 255);
 
 	private BufferedImage bufferImage;
 	private Graphics2D bufferGraphics;
 
 	private List<Layer> layers = new ArrayList<Layer>();
 
 	private SelectionRect selectionRect;
 	private boolean selectionMode = true;
 	private AreaSelectionListener selectionListener;
 
 	/**
 	 * creates the DisplayArea
 	 */
 	public DisplayArea(int numLayers)
 	{
 		super(null, true);
 		for (int i = 0; i < numLayers; i++)
 			layers.add(new Layer());
 
 		selectionRect = new SelectionRect();
 		selectionRect.setVisible(false);
 		add(selectionRect);
 
 		MouseAdapter ma = new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e)
 			{
				if (!selectionMode || e.getButton() != 1)
 					return;
 				selectionRect.start(e.getX(), e.getY());
 			}
 
 			@Override
 			public void mouseDragged(MouseEvent e)
 			{
 				if (!selectionMode)
 					return;
 				selectionRect.drag(e.getX(), e.getY());
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e)
 			{
 				if (!selectionMode)
 					return;
 				selectionRect.end();
 			}
 		};
 		addMouseListener(ma);
 		addMouseMotionListener(ma);
 	}
 
 	/**
 	 * creates the buffered images backing this component
 	 */
 	public synchronized void createImages()
 	{
 		if (bufferGraphics != null)
 			bufferGraphics.dispose();
 		if (bufferImage != null)
 			bufferImage.flush();
 
 		bufferImage = createImage();
 		bufferGraphics = bufferImage.createGraphics();
 
 		for (Layer l : layers)
 			l.createImage();
 	}
 
 	/**
 	 * @param selectionMode the selectionMode to set
 	 */
 	public void setSelectionMode(boolean selectionMode)
 	{
 		this.selectionMode = selectionMode;
 	}
 
 	/**
 	 * @return the selectionMode
 	 */
 	public boolean isSelectionMode()
 	{
 		return selectionMode;
 	}
 
 
 	/**
 	 * @param keepAspectRatio the keepAspectRatio to set
 	 */
 	public void setKeepAspectRatio(boolean keepAspectRatio)
 	{
 		selectionRect.keepAspectRatio = keepAspectRatio;
 	}
 
 	/**
 	 * @return the keepAspectRatio
 	 */
 	public boolean isKeepAspectRatio()
 	{
 		return selectionRect.keepAspectRatio;
 	}
 
 	/**
 	 * @param selectionListner the selectionListner to set
 	 */
 	public void setSelectionListner(AreaSelectionListener selectionListner)
 	{
 		this.selectionListener = selectionListner;
 	}
 
 	/**
 	 * @return the selectionListner
 	 */
 	public AreaSelectionListener getSelectionListner()
 	{
 		return selectionListener;
 	}
 
 	@Override
 	public void paintComponent(Graphics g)
 	{
 		if (bufferImage == null) {
 			super.paintComponent(g);
 			return;
 		}
 
 		g.drawImage(bufferImage, 0, 0, null);
 	}
 
 	/*
 	 * @see ch.sfdr.fractals.gui.component.ImageDisplay#createImage()
 	 */
 	@Override
 	public synchronized BufferedImage createImage()
 	{
 		return new BufferedImage(getImageWidth(), getImageHeight(),
 			BufferedImage.TYPE_INT_ARGB);
 	}
 
 	/*
 	 * @see ch.sfdr.fractals.gui.component.ImageDisplay#getImageHeight()
 	 */
 	@Override
 	public int getImageHeight()
 	{
 		return getSize().height;
 	}
 
 	/*
 	 * @see ch.sfdr.fractals.gui.component.ImageDisplay#getImageWidth()
 	 */
 	@Override
 	public int getImageWidth()
 	{
 		return getSize().width;
 	}
 
 	/*
 	 * @see ch.sfdr.fractals.gui.component.ImageDisplay#updateImage(java.awt.Image)
 	 */
 	@Override
 	public synchronized void updateImage(BufferedImage img, int layer)
 	{
 		layers.get(layer).updateImage(img);
 		repaintAllLayers();
 	}
 
 	private void repaintAllLayers()
 	{
 		for (Layer l : layers)
 			bufferGraphics.drawImage(l.getImage(), 0, 0, null);
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run()
 			{
 				repaint();
 			}
 		});
 	}
 
 	/*
 	 * @see ch.sfdr.fractals.gui.component.ImageDisplay#getLayers()
 	 */
 	@Override
 	public int getLayers()
 	{
 		return layers.size();
 	}
 
 	/*
 	 * @see ch.sfdr.fractals.gui.component.ImageDisplay#addLayer()
 	 */
 	@Override
 	public int addLayer()
 	{
 		Layer l = new Layer();
 		l.createImage();
 		layers.add(l);
 		return layers.size() - 1;
 	}
 
 	/*
 	 * @see ch.sfdr.fractals.gui.component.ImageDisplay#removeLayer(int)
 	 */
 	@Override
 	public synchronized void removeLayer(int layer)
 	{
 		layers.remove(layer);
 		repaintAllLayers();
 	}
 
 	/*
 	 * @see ch.sfdr.fractals.gui.component.ImageDisplay#clearLayer(int)
 	 */
 	@Override
 	public synchronized void clearLayer(int layer)
 	{
 		layers.get(layer).createImage();
 		repaintAllLayers();
 	}
 
 	/**
 	 * A panel on top to draw a semi-transparent selection
 	 */
 	private class SelectionRect
 		extends JComponent
 	{
 		private static final long serialVersionUID = 1L;
 		private boolean inSelection = false;
 		private Rectangle selection;
 		private boolean keepAspectRatio = true;
 
 		public SelectionRect()
 		{
 			super();
 			setOpaque(false);
 		}
 
 		@Override
 		public void paint(Graphics g)
 		{
 			int w = getWidth();
 			int h = getHeight();
 			g.setColor(SEL_COLOR);
 			g.fillRect(1, 1, w - 2, h - 2);
 			g.setColor(Color.BLACK);
 			g.setXORMode(SEL_BORDER_COLOR);
 			g.drawRect(0, 0, w - 1, h - 1);
 		}
 
 		public void start(int x, int y)
 		{
 			inSelection = true;
 			selection = new Rectangle(x, y, 0, 0);
 			setLocation(x, y);
 			setSize(0, 0);
 			setVisible(true);
 		}
 
 		public void drag(int x, int y)
 		{
 			if (!inSelection)
 				return;
 
 			int cwidth = x - selection.x;
 			int cheight = y - selection.y;
 
 			if (keepAspectRatio) {
 				int w = getImageWidth();
 				int h = getImageHeight();
 				double r = Math.max((double) Math.abs(cwidth) / w,
 					(double) Math.abs(cheight) / h);
 				cwidth = (int) (w * r * Math.signum(cwidth));
 				cheight = (int) (h * r * Math.signum(cheight));
 			}
 			selection.setSize(cwidth, cheight);
 
 			int cx = selection.x;
 			int cy = selection.y;
 			if (cwidth < 0) {
 				cx = selection.x + cwidth;
 				cwidth = -cwidth;
 			}
 			if (cheight < 0) {
 				cy = selection.y + cheight;
 				cheight = -cheight;
 			}
 			setBounds(cx, cy, cwidth, cheight);
 		}
 
 		public void end()
 		{
 			if (!inSelection)
 				return;
 			inSelection = false;
 			setVisible(false);
 
 			Rectangle sel = getSelection();
 			if (sel.width > 0 && sel.height > 0 && selectionListener != null)
 				selectionListener.areaSelected(sel);
 		}
 
 		public Rectangle getSelection()
 		{
 			int cx = selection.x;
 			int cy = selection.y;
 			int cwidth = selection.width;
 			int cheight = selection.height;
 			if (cwidth < 0) {
 				cx = selection.x + cwidth;
 				cwidth = -cwidth;
 			}
 			if (cheight < 0) {
 				cy = selection.y + cheight;
 				cheight = -cheight;
 			}
 			return new Rectangle(cx, cy, cwidth, cheight);
 		}
 	}
 
 	/**
 	 * a single layer
 	 */
 	private class Layer
 	{
 		private BufferedImage image;
 		private Graphics2D graphics;
 
 		public void updateImage(BufferedImage img)
 		{
 			graphics.drawImage(img, 0, 0, null);
 		}
 
 		public void createImage()
 		{
 			if (graphics != null)
 				graphics.dispose();
 			if (image != null)
 				image.flush();
 
 			image = new BufferedImage(getImageWidth(), getImageHeight(),
 				BufferedImage.TYPE_INT_ARGB);
 			graphics = image.createGraphics();
 		}
 
 		public BufferedImage getImage()
 		{
 			return image;
 		}
 	}
 }
