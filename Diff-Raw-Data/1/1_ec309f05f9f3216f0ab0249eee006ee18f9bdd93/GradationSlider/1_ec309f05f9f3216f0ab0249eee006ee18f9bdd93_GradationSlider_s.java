 package lovelogic.gui.coloreditor;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.awt.Polygon;
 import java.awt.RenderingHints;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JComponent;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 @SuppressWarnings("serial")
 public class GradationSlider extends JComponent
 {
 	private static final int DEFAULT_TRACK_HEIGHT = 10;
 	private static final int CURSOR_SIZE = 6;
 
 	private int trackHeight = DEFAULT_TRACK_HEIGHT;
 	private int cursorSize = CURSOR_SIZE;
 	private int minimum;
 	private int maximum;
 	private int value;
 	private Color c1 = Color.BLACK;
 	private Color c2 = Color.BLACK;
 	private boolean hover;
 
 	public GradationSlider()
 	{
 		MouseHandler mh = new MouseHandler();
 		addMouseListener(mh);
 		addMouseMotionListener(mh);
 
 		setPreferredSize(new Dimension(180, trackHeight + cursorSize));
 	}
 
 	public GradationSlider setMinimum(int minimum)
 	{
 		this.minimum = minimum;
 		return this;
 	}
 
 	public GradationSlider setMaximum(int maximum)
 	{
 		this.maximum = maximum;
 		return this;
 	}
 
 	public int getValue()
 	{
 		return value;
 	}
 
 	public GradationSlider setValue(int value)
 	{
 		if (minimum <= value && value <= maximum)
 		{
 			this.value = value;
 		}
 		return this;
 	}
 
 	public GradationSlider setLeftColor(Color c)
 	{
 		c1 = c;
 		return this;
 	}
 
 	public GradationSlider setRightColor(Color c)
 	{
 		c2 = c;
 		return this;
 	}
 
 	protected void paintComponent(Graphics g)
 	{
 		draw((Graphics2D)g);
 	}
 
 	private void draw(Graphics2D g)
 	{
 		g.setColor(getBackground());
 		g.fillRect(0, 0, getWidth(), getHeight());
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 		int y0 = (getHeight() - (trackHeight + cursorSize)) / 2;
 		g.translate(0, y0);
 		drawTrack(g);
 		drawCursor(g);
 		g.translate(0, -y0);
 	}
 
 	private void drawTrack(Graphics2D g)
 	{
 		int x = cursorSize;
 		int w = getWidth() - 2 * cursorSize;
 		Paint p = new GradientPaint(x, 0, c1, x + w, 0, c2);
 		g.setPaint(p);
 		g.fillRect(x, 0, w, trackHeight);
 	}
 
 	private void drawCursor(Graphics2D g)
 	{
 		Polygon p = new Polygon();
 		p.addPoint(0, 0);
 		p.addPoint(cursorSize, cursorSize);
 		p.addPoint(-cursorSize, cursorSize);
 
 		int x = getCursorPosition();
 
 		g.setPaint(null);
 		g.setColor(Color.WHITE);
 		g.drawLine(x, 0, x, trackHeight);
 		p.translate(x, trackHeight);
 		g.setColor(hover ? Color.BLACK : Color.LIGHT_GRAY);
 		g.fill(p);
 		p.translate(-x, -trackHeight);
 	}
 
 	private int getCursorPosition()
 	{
 		if (minimum < maximum)
 		{
 			return cursorSize + (getWidth() - 2 * cursorSize) * (value - minimum) / (maximum - minimum);
 		}
 		return 0;
 	}
 
 	private int locationToValue(int x)
 	{
 		int x0 = cursorSize;
 		int x1 = getWidth() - cursorSize;
 		int v = minimum + (maximum - minimum) * (x - x0) / (x1 - x0);
 		return Math.max(minimum, Math.min(v, maximum));
 	}
 
 	private void changeValue(int value)
 	{
 		setValue(value);
 		dispatchChangeEvent();
 	}
 
 	public void addChangeListener(ChangeListener l)
 	{
 		listenerList.add(ChangeListener.class, l);
 	}
 
 	public void removeChangeListener(ChangeListener l)
 	{
 		listenerList.remove(ChangeListener.class, l);
 	}
 
 	private void dispatchChangeEvent()
 	{
 		ChangeEvent e = new ChangeEvent(this);
 		for (ChangeListener l : listenerList.getListeners(ChangeListener.class))
 		{
 			l.stateChanged(e);
 		}
 	}
 
 	private class MouseHandler extends MouseAdapter
 	{
 		private boolean drag;
 
 		public void mouseExited(MouseEvent e)
 		{
 			if (!drag)
 			{
 				hover = false;
 			}
 			repaint();
 		}
 
 		public void mousePressed(MouseEvent e)
 		{
 			changeValue(locationToValue(e.getX()));
 			drag = true;
 			repaint();
 		}
 
 		public void mouseReleased(MouseEvent e)
 		{
 			drag = false;
 		}
 
 		public void mouseMoved(MouseEvent e)
 		{
 			int x = getCursorPosition();
 			if (x - cursorSize <= e.getX() && e.getX() <= x + cursorSize)
 			{
 				hover = true;
 			}
 			else
 			{
 				hover = false;
 			}
 			repaint();
 		}
 
 		public void mouseDragged(MouseEvent e)
 		{
 			if (drag)
 			{
 				changeValue(locationToValue(e.getX()));
 			}
 			repaint();
 		}
 	}
 }
