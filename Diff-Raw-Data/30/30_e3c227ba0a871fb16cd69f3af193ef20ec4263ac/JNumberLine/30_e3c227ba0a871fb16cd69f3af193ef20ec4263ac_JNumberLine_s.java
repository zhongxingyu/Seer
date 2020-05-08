 /*
  * Copyright (c) 2008 Bradley W. Kimmel
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.eandb.util.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  * @author brad
  *
  */
 public class JNumberLine extends JComponent {
 	
 	/** Serialization version ID. */
 	private static final long serialVersionUID = 6382523327122484024L;
 	
 	private static final int MAX_PRECISION = 13;
 	
 	private static final int MARK_RADIUS = 5;
 
 	private Color backgroundColor = Color.WHITE;
 	
 	private Color tickColor = Color.BLACK;
 	
 	private Color markColor = Color.RED;
 	
 	private Color zeroMarkColor = Color.BLUE;
 	
 	private Color borderColor = Color.BLACK;
 	
 	private Color textColor = Color.BLACK;
 	
 	private Color disabledBackgroundColor = Color.LIGHT_GRAY;
 	
 	private Color disabledBorderColor = Color.GRAY;
 	
 	private Color disabledTickColor = Color.GRAY;
 	
 	private Color disabledTextColor = Color.DARK_GRAY;
 	
 	private Color focusOutlineColor = Color.LIGHT_GRAY;
 	
 	private double value;
 
 	private double minimumVisible;
 	
 	private double maximumVisible;
 	
 	private Point dragRefPoint = null;
 	
 	private boolean draggingMark = false;
 	
 	private int dragOffset = 0;
 	
 	private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
 	
 	public JNumberLine() {
 		this(-10, 10, 0);
 	}
 	
 	/**
 	 * 
 	 */
 	public JNumberLine(double minimumVisible, double maximumVisible, double value) {
 		if (!(minimumVisible <= value && value <= maximumVisible)) {
 			throw new IllegalArgumentException("value not in range");
 		}
 		if (minimumVisible >= maximumVisible) {
 			throw new IllegalArgumentException("minimumVisible >= maximumVisible");
 		}
 		
 		this.minimumVisible = minimumVisible;
 		this.maximumVisible = maximumVisible;
 		this.value = value;
 		
 		setFocusable(true);
 		setMinimumSize(new Dimension(100, 25));
 		setPreferredSize(new Dimension(200, 25));
 		addFocusListener(new FocusListener() {
 			public void focusGained(FocusEvent e) {
 				onFocusGained(e);
 			}
 			public void focusLost(FocusEvent e) {
 				onFocusLost(e);
 			}
 		});
 		addComponentListener(new ComponentAdapter() {
 			public void componentResized(ComponentEvent e) {
 				onComponentResized(e);
 			}
 		});
 		addMouseMotionListener(new MouseMotionAdapter() {
 			public void mouseDragged(MouseEvent e) {
 				onMouseDragged(e);
 			}
 		});
 		addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				onMousePressed(e);
 			}
 			public void mouseReleased(MouseEvent e) {
 				onMouseReleased(e);
 			}
 		});
 		addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				onKeyPressed(e);
 			}
 		});
 	}
 	
 	private void onFocusLost(FocusEvent e) {
 		repaint();
 	}
 
 	private void onFocusGained(FocusEvent e) {
 		repaint();
 	}
 
 	private void onKeyPressed(KeyEvent e) {
 		if (isEnabled()) {
 			switch (e.getKeyCode()) {
 			case KeyEvent.VK_LEFT:
 				{
 					double ti = getMinorTickIncrement();
 					double newValue = ti * Math.floor(Math.nextAfter(value, Double.NEGATIVE_INFINITY) / ti);
 					double dv = newValue - value;
 					minimumVisible += dv;
 					maximumVisible += dv;
 					value = newValue;
 					repaint();
 					fireStateChanged();
 					break;
 				}
 				
 			case KeyEvent.VK_RIGHT:
 				{
 					double ti = getMinorTickIncrement();
 					double newValue = ti * Math.ceil(Math.nextUp(value) / ti);
 					double dv = newValue - value;
 					minimumVisible += dv;
 					maximumVisible += dv;
 					value = newValue;
 					repaint();
 					fireStateChanged();
 					break;
 				}
 			}
 		}
 	}
 
 	private void onComponentResized(ComponentEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void onMouseDragged(MouseEvent e) {
 		if (isEnabled()) {
 			boolean button1 = (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0;
 			boolean button3 = (e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0;
 			if (button1) {
				int dx = e.getX() - dragRefPoint.x;
				if (dx != 0) {
 					double pi = getPixelIncrement();
					if (draggingMark) {
						double q1 = minimumVisible + 0.25 * (maximumVisible - minimumVisible);
						double q3 = minimumVisible + 0.75 * (maximumVisible - minimumVisible);
						int q1x = getXCoordinate(q1);
						int q3x = getXCoordinate(q3);
						dx = e.getX() - Math.min(Math.max(dragRefPoint.x, q1x), q3x);
 						double dv = pi * (double) dx;
 						double nv = value + dv;
 						int nx = getXCoordinate(nv);
 						if (nx < q1x) {
 							dv = -pi * Math.log10(1.0 + (double) (q1x - nx));
 							minimumVisible += dv;
 							maximumVisible += dv;
 							value += dv;
 						} else if (nx > q3x) {
 							dv = pi * Math.log10(1.0 + (double) (nx - q3x));
 							minimumVisible += dv;
 							maximumVisible += dv;
 							value += dv;
 						} else {
 							value = nv;
 						}
					} else {
 						double dv = pi * (double) dx;
 						minimumVisible -= dv;
 						maximumVisible -= dv;
 						value -= dv;
 					}
 					repaint();
 					fireStateChanged();
 				}
 			} else if (button3) {
 				int x = e.getX();
 				int x0 = getXCoordinate(value);
 				if (Math.abs(x - x0) > 2) {
 					int rx = dragRefPoint.x;
 					double rv = getValueForPixel(rx);
 					double newIncr = Math.abs((rv - value) / (double) (x - x0));
 					int w = getWidth();
 					double newMinimumVisible = value - newIncr * (double) x0;
 					double newMaximumVisible = value + newIncr * (double) (w - 1 - x0);
 					double pi = (newMaximumVisible - newMinimumVisible) / (double) w;
 					double scale = Math.max(Math.abs(maximumVisible), Math.abs(minimumVisible));
 					int precision = (int) Math.ceil(Math.log10(scale)) - (int) Math.ceil(Math.log10(3.0 * pi)); //-13
 
 					if (precision <= MAX_PRECISION) {
 						minimumVisible = newMinimumVisible;
 						maximumVisible = newMaximumVisible;
 						repaint();
 						fireStateChanged();
 					}						
 				}
 			}
 	
 			dragRefPoint = e.getPoint();
 		}
 	}
  
 	private void onMouseReleased(MouseEvent e) {
 		dragRefPoint = null;
 		draggingMark = false;
 		if (isEnabled()) {
 			fireStateChanged();
 		}
 	}
 
 	private void onMousePressed(MouseEvent e) {
 		if (isEnabled()) {
 			requestFocusInWindow();
 			dragRefPoint = e.getPoint();
 			int x = this.getXCoordinate(value);
 			dragOffset = x - dragRefPoint.x;
 			draggingMark = (e.getButton() == MouseEvent.BUTTON1)
 					&& (Math.abs(dragOffset) <= MARK_RADIUS);
 		}
 	}
 	
 	public void addChangeListener(ChangeListener l) {
 		changeListeners.add(l);
 	}
 	
 	protected void fireStateChanged() {
 		ChangeEvent e = new ChangeEvent(this);
 		for (ChangeListener l : changeListeners) {
 			l.stateChanged(e);
 		}
 	}
 	
 	/**
 	 * @return the backgroundColor
 	 */
 	public Color getBackgroundColor() {
 		return backgroundColor;
 	}
 
 	/**
 	 * @param backgroundColor the backgroundColor to set
 	 */
 	public void setBackgroundColor(Color backgroundColor) {
 		this.backgroundColor = backgroundColor;
 		repaint();
 	}
 
 	/**
 	 * @return the tickColor
 	 */
 	public Color getTickColor() {
 		return tickColor;
 	}
 
 	/**
 	 * @param tickColor the tickColor to set
 	 */
 	public void setTickColor(Color tickColor) {
 		this.tickColor = tickColor;
 		repaint();
 	}
 
 	/**
 	 * @return the markColor
 	 */
 	public Color getMarkColor() {
 		return markColor;
 	}
 
 	/**
 	 * @param markColor the markColor to set
 	 */
 	public void setMarkColor(Color markColor) {
 		this.markColor = markColor;
 		repaint();
 	}
 
 	/**
 	 * @return the zeroMarkColor
 	 */
 	public Color getZeroMarkColor() {
 		return zeroMarkColor;
 	}
 
 	/**
 	 * @param zeroMarkColor the zeroMarkColor to set
 	 */
 	public void setZeroMarkColor(Color zeroMarkColor) {
 		this.zeroMarkColor = zeroMarkColor;
 		repaint();
 	}
 
 	/**
 	 * @return the borderColor
 	 */
 	public Color getBorderColor() {
 		return borderColor;
 	}
 
 	/**
 	 * @param borderColor the borderColor to set
 	 */
 	public void setBorderColor(Color borderColor) {
 		this.borderColor = borderColor;
 		repaint();
 	}
 
 	/**
 	 * @return the textColor
 	 */
 	public Color getTextColor() {
 		return textColor;
 	}
 
 	/**
 	 * @param textColor the textColor to set
 	 */
 	public void setTextColor(Color textColor) {
 		this.textColor = textColor;
 		repaint();
 	}
 
 	/**
 	 * @return the minimumVisible
 	 */
 	public double getMinimumVisible() {
 		return minimumVisible;
 	}
 
 	/**
 	 * @param minimumVisible the minimumVisible to set
 	 */
 	public void setMinimumVisible(double minimumVisible) {
 		if (minimumVisible >= maximumVisible) {
 			throw new IllegalArgumentException("Invalid minimum visible");
 		}
 		if (value <= minimumVisible) {
 			throw new IllegalArgumentException("Value is outside new range");
 		}
 		this.minimumVisible = minimumVisible;
 		repaint();
 	}
 
 	/**
 	 * @return the maximumVisible
 	 */
 	public double getMaximumVisible() {
 		return maximumVisible;
 	}
 
 	/**
 	 * @param maximumVisible the maximumVisible to set
 	 */
 	public void setMaximumVisible(double maximumVisible) {
 		if (minimumVisible >= maximumVisible) {
 			throw new IllegalArgumentException("Invalid maximum visible");
 		}
 		if (value >= maximumVisible) {
 			throw new IllegalArgumentException("Value is outside new range");
 		}
 		this.maximumVisible = maximumVisible;
 		repaint();
 	}
 	
 	public void setVisibleRange(double minimumVisible, double maximumVisible) {
 		if (minimumVisible >= maximumVisible) {
 			throw new IllegalArgumentException("Invalid visible range");
 		}
 		if (value >= maximumVisible || value <= minimumVisible) {
 			throw new IllegalArgumentException("Value is outside new range");
 		}
 		this.minimumVisible = minimumVisible;
 		this.maximumVisible = maximumVisible;
 		repaint();
 	}
 
 	/**
 	 * @return the disabledBackgroundColor
 	 */
 	public Color getDisabledBackgroundColor() {
 		return disabledBackgroundColor;
 	}
 
 	/**
 	 * @param disabledBackgroundColor the disabledBackgroundColor to set
 	 */
 	public void setDisabledBackgroundColor(Color disabledBackgroundColor) {
 		this.disabledBackgroundColor = disabledBackgroundColor;
 	}
 
 	/**
 	 * @return the disabledBorderColor
 	 */
 	public Color getDisabledBorderColor() {
 		return disabledBorderColor;
 	}
 
 	/**
 	 * @param disabledBorderColor the disabledBorderColor to set
 	 */
 	public void setDisabledBorderColor(Color disabledBorderColor) {
 		this.disabledBorderColor = disabledBorderColor;
 	}
 
 	/**
 	 * @return the disabledTickColor
 	 */
 	public Color getDisabledTickColor() {
 		return disabledTickColor;
 	}
 
 	/**
 	 * @param disabledTickColor the disabledTickColor to set
 	 */
 	public void setDisabledTickColor(Color disabledTickColor) {
 		this.disabledTickColor = disabledTickColor;
 	}
 
 	/**
 	 * @return the disabledTextColor
 	 */
 	public Color getDisabledTextColor() {
 		return disabledTextColor;
 	}
 
 	/**
 	 * @param disabledTextColor the disabledTextColor to set
 	 */
 	public void setDisabledTextColor(Color disabledTextColor) {
 		this.disabledTextColor = disabledTextColor;
 	}
 
 	/**
 	 * @return the focusOutlineColor
 	 */
 	public Color getFocusOutlineColor() {
 		return focusOutlineColor;
 	}
 
 	/**
 	 * @param focusOutlineColor the focusOutlineColor to set
 	 */
 	public void setFocusOutlineColor(Color focusOutlineColor) {
 		this.focusOutlineColor = focusOutlineColor;
 	}
 
 	public void setValue(double value) {
 		this.value = value;
 		
 		double q1 = minimumVisible + 0.25 * (maximumVisible - minimumVisible);
 		double q3 = minimumVisible + 0.75 * (maximumVisible - minimumVisible);
 		if (value < q1 || value > q3) {
 			double q2 = minimumVisible + 0.5 * (maximumVisible - minimumVisible);
 			double dv = value - q2;
 			minimumVisible += dv;
 			maximumVisible += dv;
 		}
 		repaint();
 	}
 	
 	public double getValue() {
 		double pi = getPixelIncrement();
 		int digits = 1 - (int) Math.ceil(Math.log10(3.0 * pi));
 		double roundTo = Math.pow(10.0, -digits);
 		return roundTo * Math.round(value / roundTo);
 	}
 	
 	public String getValueAsString() {
 		double pi = getPixelIncrement();
 		int digits = 1 - (int) Math.ceil(Math.log10(3.0 * pi));
 		double roundTo = Math.pow(10.0, -digits);
 		double v = roundTo * Math.round(value / roundTo);
 		String fmt = String.format("%%.%df", Math.max(digits, 0));
 		return String.format(fmt, v);
 	}
 	
 	public boolean getValueIsAdjusting() {
 		return dragRefPoint != null;
 	}
 
 	/* (fnon-Javadoc)
 	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
 	 */
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
 		paintSlider(g);
 	}
 	
 	protected void paintSlider(Graphics g) {
 		paintBackground(g);
 		paintTicks(g);
 		paintZeroMark(g);
 		paintMark(g);
 		paintText(g);
 	}
 	
 	private void paintText(Graphics g) {
 		g.setColor(isEnabled() ? textColor : disabledTextColor);
 		String label = getValueAsString();
 		Rectangle2D rect = g.getFontMetrics().getStringBounds(label, g);
 
 		double w = rect.getWidth();
 		double h = rect.getHeight();
 		double w0 = (double) getWidth();
 		double h0 = (double) getHeight();
 		g.drawString(label, (int) Math.floor(w0 - w) - 2, (int) Math.round((h0 + h) / 2.0));
 	}
 	
 	private void paintBackground(Graphics g) {
 		Dimension d = getSize();
 		g.setColor(isEnabled() ? backgroundColor : disabledBackgroundColor);
 		g.fillRect(0, 0, d.width - 1, d.height - 1);
 		g.setColor(isEnabled() ? borderColor : disabledBorderColor);
 		g.drawRect(0, 0, d.width - 1, d.height - 1);
 		if (hasFocus()) {
 			g.setColor(focusOutlineColor);
 			g.drawRect(1, 1, d.width - 3, d.height - 3);
 		}
 	}
 	
 	private void paintMark(Graphics g) {
 		int x = getXCoordinate(value);
 		int h = getHeight();
 		
 		g.setColor(markColor);
 		g.setXORMode(Color.WHITE);
 		g.fillPolygon(new int[] { x - 4, x + 4, x }, new int[] { 1, 1, 5 }, 3);
 		g.fillPolygon(new int[] { x - 5, x + 5, x }, new int[] { h - 1, h - 1, h - 6 }, 3);
 		g.setPaintMode();
 		g.drawLine(x, 5, x, h - 6);
 	}
 	
 	private void paintZeroMark(Graphics g) {
 		if (minimumVisible <= 0.0 && 0.0 <= maximumVisible) {
 			int x = getXCoordinate(0.0);
 			int h = getHeight();
 			g.setColor(zeroMarkColor);
 			g.drawLine(x, 0, x, h - 1);			
 		}
 	}
 	
 	private void paintTicks(Graphics g) {
 		g.setColor(isEnabled() ? tickColor : disabledTickColor);
 		
 		double tickIncr = getMinorTickIncrement();
 		Dimension d = getSize();
 		for (int j = 1; j <= 2; j++) {
 			double minTick = tickIncr * Math.ceil(minimumVisible / tickIncr);
 			double maxTick = tickIncr * Math.floor(maximumVisible / tickIncr);
 			int numTicks = (int) Math.round((maxTick - minTick) / tickIncr);
 			for (int i = 0; i <= numTicks; i++) {
 				double tickValue = minTick + (maxTick - minTick) * ((double) i / (double) numTicks);
 				int x = getXCoordinate(tickValue);
 				int y0 = 0;
 				int y1 = 3 * j;
 				g.drawLine(x, y0, x, y1);
 				g.drawLine(x, d.height - 1 - y1, x, d.height - 1 - y0);			
 			}
 			tickIncr *= 10.0;
 		}
 	}
 	
 	private int getXCoordinate(double value) {
 		int w = getWidth();
 		return (int) Math.round(((double) (w - 1)) * (value - minimumVisible) / (maximumVisible - minimumVisible));
 	}
 	
 	private double getValueForPixel(int x) {
 		int w = getWidth();
 		double t = (double) x / (double) (w - 1);
 		return minimumVisible + t * (maximumVisible - minimumVisible);
 	}
 	
 	protected double getPixelIncrement() {
 		int w = getWidth();
 		return (maximumVisible - minimumVisible) / (double) w;
 	}
 	
 	protected double getMinorTickIncrement() {
 		double pi = getPixelIncrement();
 		return Math.pow(10, Math.ceil(Math.log10(3.0 * pi)));
 	}
 	
 	protected double getMajorTickIncrement() {
 		return 10.0 * getMinorTickIncrement();
 	}
 	
 }
