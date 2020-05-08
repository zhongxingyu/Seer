 package org.apache.pivot.wtk;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.MouseInfo;
 import java.awt.Panel;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Area;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 
 
 /**
  * Sub-class of {@link DesktopApplicationContext.HostFrame} which supports custom frame movement
  * functionality
  * 
  * @author David Ray
  */
 @SuppressWarnings("serial")
 public class DesktopFrame extends DesktopApplicationContext.HostFrame {
 	/**
 	 * Provides a way to specify different title bar and border styles.
 	 * 
 	 * @author David Ray
 	 */
 	public enum Style { 
 		NATIVE_OSX, NATIVE_WIN, NATIVE_LINUX, CUSTOM, DEFAULT, BARCHART;
 		
 		public void apply(final DesktopFrame frame) {
 			frame.configuredStyle = this;
 			
 			switch(frame.configuredStyle) {
 		        case NATIVE_LINUX:
 		        case NATIVE_WIN: {
 		        	frame.titleBarHeight = WIN_TITLEBAR_HEIGHT;
 		        	frame.setTitleBar(frame.createTitleBar(this));
 		        	frame.setBorderColor(new Color(185, 209, 234));
 		            frame.setBorderDecorated(true, true);
 		            frame.setBorderSize(5);
 		            
 		            frame.installWindowsBorders();
 		            frame.reshapeBorders();
 		            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
 		            break;
 		        }
 		        case NATIVE_OSX: {
 		        	frame.titleBarHeight = OSX_TITLEBAR_HEIGHT;
 		        	frame.setTitleBar(frame.createTitleBar(this));
 		            frame.setBorderDecorated(true, true);
 		            frame.setBorderSize(1);
 		            frame.reshapeBorders();
 		        	break;
 		        }
 		        case CUSTOM: {
 		        	frame.setTitleBar(frame.createTitleBar(this));
 		            frame.setBorderDecorated(true, true);
 		            frame.setBorderSize(1);
 		            frame.reshapeBorders();
 		        	break;
 		        }
 		        case BARCHART: {
 		        	frame.titleBarHeight = BARCHART_TITLEBAR_HEIGHT;
 		        	frame.setTitleBar(frame.createTitleBar(this));
 		        	frame.setBorderColor(new Color(50, 50, 50));
 		            frame.setBorderDecorated(true, true);
 		            frame.setBorderSize(5);
 		            
 		            frame.installWindowsBorders();
 		            frame.reshapeBorders();
		            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		            EventQueue.invokeLater(new Runnable() {
		            	public void run() { frame.setResizable(false); }
		            });
 		            
 		            break;
 		        }
 		        default: {
 		        	frame.titleBarHeight = DEFAULT_TITLEBAR_HEIGHT;
 		        	frame.setTitleBar(frame.createTitleBar(this));
 		            frame.setBorderDecorated(true, true);
 		            break;
 		        }
 	        }
 			
 			if(frame.titleBar != null) {
 				frame.addCursorHandler();
 		        frame.installMouseHandler(frame.new MouseHandler());
 		        TitleBarControl tbc =  frame.titleBar.getTitleBarControl();
 		        if(tbc != null) {
 		        	frame.titleBar.invalidate();
 		        	tbc.invalidate();
 		        	tbc.repaint();
 		        }
 		       
 		        frame.repaint();
 			}
 		}
 		
 		public static Style styleFor(String styleString) {
 			if(styleString == null) {
 				return null;
 			}
 			if(styleString.equalsIgnoreCase("osx")) {
 				return Style.NATIVE_OSX;
 			}else if(styleString.toLowerCase().indexOf("win") != -1) {
 				return Style.NATIVE_WIN;
 			}else if(styleString.equalsIgnoreCase("linux")) {
 				return Style.NATIVE_LINUX;
 			}else if(styleString.toLowerCase().indexOf("bar") != -1) {
 				return Style.BARCHART;
 			}
 			return null;
 		}
 	};
 	private static final int BARCHART_TITLEBAR_HEIGHT = 23;
 	private static final int OSX_TITLEBAR_HEIGHT = 22;
 	private static final int WIN_TITLEBAR_HEIGHT = 30;
 	private static final int DEFAULT_TITLEBAR_HEIGHT = 25;
 	private static boolean NATIVE_STYLE_OVERRIDE;
 	
 	private boolean isFakeMaximized;
 	
 	/** The size in pixels of the border */
     private int borderSize = 1;
     /** The thickness over which a cursor change will be triggered for resizing */
     private int resizeMargin = borderSize;
     /** The default height of the title bar */
     private int titleBarHeight;
     /** The Component shown as the title bar decoration */
     private TitleBar titleBar;
     /** The border color */
     private Color borderColor = new Color(120, 120, 120);
     /** Flag indicating this frame contains borders. */
     private boolean isBorderDecorated;
     /** Enum which handles the native os look and feel configuration */
     private Style configuredStyle;
     /** Flag indicating a resize operation ocurring */
     private boolean isResizing;
     /** Handles cursor changes to indicated resizing */
     private CursorManager cursorManager;
     /** The left border component which detects mouse events for cursor manipulation and resizing */
     private Component leftBorderComponent;
     /** The right border component which detects mouse events for cursor manipulation and resizing */
     private Component rightBorderComponent;
     /** The bottom border component which detects mouse events for cursor manipulation and resizing */
     private Component bottomBorderComponent;
     /** Houses the component added to this frame by the user */
     private Container mainContentPanel;
     /** List of listeners with interest in drag notifications */
     private List<DragListener> dragListeners = new ArrayList<DragListener>();
     /** List of screen rectangles - one per monitor */
     private List<Rectangle> virtualDeviceBounds;
     /** The shape of the virtual screens */
     private Area virtualArea;
     private  boolean resizeable = true;
 	
 	public DesktopFrame() {
 		this(NATIVE_STYLE_OVERRIDE ? Style.CUSTOM : getNativeStyle());
 	}
 	
 	public DesktopFrame(Style style) {
 		setUndecorated(true);
 		
 		setLayout(new BorderLayout());
 		
 		Panel panel = createMainContentPanel();
 		addMainContentPanel(panel);
 		
 		style.apply(this);
 		
 		//Collect the current monitor configuration boundaries.
         getEnvironmentInfo(); 
         
        setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
 	}
 	
 	@Override
 	public Component add(Component comp) {
 		mainContentPanel.add(comp, BorderLayout.CENTER);
 		return comp;
 	}
 	
 	@Override
 	public void update(Graphics g) {
 		paint(g);
 	}
 	
 	/**
      * Protects against a frame location being out of aggregate
      * device bounds by repositioning the upper left corner of a
      * frame that is out of virtual device bounds to be within
      * the bounds of the closest screen.
      * 
      * @param p		the location in question
      * @return		the most ideal location, or the same point if
      * 				that point is location within virtual device
      * 				bounds.
      */
     private Point getValidLocationFor(Point p) {
     	if(virtualDeviceBounds == null) {
     		return p;
     	}
     	
     	if(!virtualArea.contains(p)) {
     		Rectangle r = getClosestDevice(p);
     		p.x = Math.max(r.x, p.x);
 			p.x = Math.min((int)r.getMaxX(), p.x);
 			p.y = Math.max(r.y, p.y);
 			p.y = Math.min((int)r.getMaxY(), p.y);
     	}
     	return p;
     }
     
     /**
      * Sets the resizable flag
      * 
      * @param b	true if so, false if not
      */
     @Override
     public void setResizable(boolean b) {
     	this.resizeable = b;
     }
     
     /**
      * Overridden because the parent class can get
      * out of sync if resizable is issued too early.
      */
     @Override
     public boolean isResizable() {
     	return this.resizeable;
     }
     
     /**
      * Called when the suggested location passed in by 
      * the windowing environment (read Pivot) is outside
      * the device bounds described by the combined graphics
      * devices. This happens for sure with Mac OSX due to 
      * the existence of the MenuBar on top.
      * 
      * @param p
      * @return		the bounds of the closest screen relative 
      * 				to the {@link Point} passed in.
      */
     private Rectangle getClosestDevice(Point p) {
     	double distance = Integer.MAX_VALUE;
     	Rectangle closest = null;
     	for(Rectangle r : virtualDeviceBounds) {
     		if(p.distance(r.getLocation()) < distance) {
     			closest = r;
     			distance = p.distance(r.getLocation());
     		}
     	}
     	return closest;
     }
 	
 	/**
      * Sets the location of this {@link TempFrame}
      * 
      * Native frames would know their screen bounds, but 
      * because this is a custom frame, we must intercept this
      * call to make sure that the location is a valid one.
      * 
      * @param	x	x location
      * @param	y   y location
      * 
      */
     @Override
     public void setLocation(int x, int y) {
     	Point p = getValidLocationFor(new Point(x, y));
     	super.setLocation(p.x, p.y);
     }
     
     /**
      * Sets the location of this {@link TempFrame}
      * 
      * Native frames would know their screen bounds, but 
      * because this is a custom frame, we must intercept this
      * call to make sure that the location is a valid one.
      * 
      * @param	p	the location of this frame as a {@link Point}
      */
     @Override
     public void setLocation(Point p) {
     	super.setLocation(getValidLocationFor(p));
     }
 	
 	/**
      * Returns the current component serving as the {@link TitleBar}
      * 
      * @return	the current component serving as the {@link TitleBar}
      */
     public TitleBar getTitleBar() {
     	return this.titleBar;
     }
     
     /**
      * Sets the {@link TitleBar} component.
      * 
      * The {@code TitleBar}'s preferred height must be set prior
      * to calling this method (preferredWidth is irrelevant, i.e.
      * can be set to zero).
      * 
      * @param c		the TitleBar component.
      */
     public void setTitleBar(TitleBar c) {
     	removeTitleBar();
         this.titleBar = c;
         titleBarHeight = c.getPreferredSize().height;
         add(c, BorderLayout.NORTH);
         invalidate();
         validate();
         repaint();
         titleBar.repaint();
     }
     
     public void setExtendedState(int paramInt) {
     	super.setExtendedState(paramInt);
     	
     	Rectangle b = getBounds();
     	Rectangle win = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
     	if((paramInt & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
     		isFakeMaximized = true;
 			System.out.println("setting fake max with height: " + win.height);
     		setBounds(b.x, b.y, b.width, win.height);
     		setBordersVisible(false);
 			setBorderSize(0);
 			setResizable(false);
 			invalidate();
 			repaint();
 		}else{
 			int w = (int)((double)b.width * .70d);
 			int h = (int)((double)b.height * .70d);
 			setBounds(win.width / 2 - w / 2, win.height / 2 - h / 2, w, h);
     		isFakeMaximized = false;
     		setBordersVisible(true);
 			setBorderSize(5);
 			setResizable(true);
 			invalidate();
 			repaint();
     	}
     }
     
     public boolean isFakeMaximized() {
     	return isFakeMaximized;
     }
     
     /**
      * Called by the SwingContainer class to determine the location
      * to set on the delegate Window.
      * @return
      */
     public java.awt.Insets getTitlebarInsets() {
     	java.awt.Insets superInsets = super.getInsets();
     	if(titleBar == null) {
     		return superInsets;
     	}
     	return new java.awt.Insets(
     		titleBar.getPreferredSize().height, superInsets.left + leftBorderComponent.getWidth(), 
     			superInsets.bottom + bottomBorderComponent.getHeight(), superInsets.right + rightBorderComponent.getWidth());
     }
 	
 	/**
      * Installs the handler which controls the cursor changes
      * for border or titlebar mouse overs.
      */
     private void addCursorHandler() {
     	if(cursorHandlerInstalled() || titleBar == null) return;
     	
     	cursorManager = new CursorManager();
         titleBar.addMouseListener(cursorManager);
         titleBar.addMouseMotionListener(cursorManager);
         if(leftBorderComponent != null) {
             leftBorderComponent.addMouseListener(cursorManager);
             leftBorderComponent.addMouseMotionListener(cursorManager);
         }
         if(rightBorderComponent != null) {
             rightBorderComponent.addMouseListener(cursorManager);
             rightBorderComponent.addMouseMotionListener(cursorManager);
         }
         if(bottomBorderComponent != null) {
             bottomBorderComponent.addMouseListener(cursorManager);
             bottomBorderComponent.addMouseMotionListener(cursorManager);
         }
     }
     
     /**
      * Installs the main mouse controller which handles the frame movement, etc.
      * @param manager	
      */
     public void installMouseHandler(MouseHandler manager) {
         titleBar.addMouseListener(manager);
         titleBar.addMouseMotionListener(manager);
         
         if(leftBorderComponent != null) {
             leftBorderComponent.addMouseListener(manager);
             leftBorderComponent.addMouseMotionListener(manager);
         }
         if(rightBorderComponent != null) {
             rightBorderComponent.addMouseListener(manager);
             rightBorderComponent.addMouseMotionListener(manager);
         }
         if(bottomBorderComponent != null) {
             bottomBorderComponent.addMouseListener(manager);
             bottomBorderComponent.addMouseMotionListener(manager);
         }
     }
     
     /**
      * Checks for the prior installation of the {@link TempFrame.CursorManager}.
      * 
      * @return	true if already installed, false if not.
      */
     private boolean cursorHandlerInstalled() {
     	if(titleBar != null) {
 	    	MouseListener[] installedListeners = titleBar.getMouseListeners();
 	    	for(MouseListener l : installedListeners) {
 	    		if(l instanceof CursorManager) {
 	    			return true;
 	    		}
 	    	}
     	}
     	return false;
     }
     
     /**
      * Adds the JPanel which houses the main content.
      * 
      * @param panel The JPanel which houses the main content.
      */
     private void addMainContentPanel(Panel panel) {
         this.mainContentPanel = panel;
         super.add(mainContentPanel, BorderLayout.CENTER);
     }
     
    /**
      * Creates and returns the panel which holds the main content.
      * 
      * @return  the panel which holds the main content.
      */
     private Panel createMainContentPanel() {
         Panel main = new Panel() {
         	@Override
         	public void update(Graphics g) {
         		paint(g);
         	}
         };
        
         main.setPreferredSize(new Dimension(300, 200));
         main.setLayout(new BorderLayout());
         return main;
     }
     
     /**
      * Sets the component representing the left ({@link BorderLayout#WEST})
      * frame border. All border components have a default which is automatically
      * installed and can have their size and color altered. For any (more
      * complex) painting, it is necessary for the user to install their own
      * component (i.e. Gradients etc.)
      * 
      * @param c	the component painting the left border.
      */
     public void setLeftBorderComponent(Component c) {
         if(!isBorderDecorated) {
             throw new IllegalStateException("Please call setBorderDecorated(true) first.");
         }
         this.leftBorderComponent = c;
         mainContentPanel.add(leftBorderComponent, BorderLayout.WEST);
     }
     
     /**
      * Sets the component representing the left ({@link BorderLayout#EAST})
      * frame border. All border components have a default which is automatically
      * installed and can have their size and color altered. For any (more
      * complex) painting, it is necessary for the user to install their own
      * component (i.e. Gradients etc.)
      * 
      * @param c	the component painting the left border.
      */
     public void setRightBorderComponent(Component c) {
         if(!isBorderDecorated) {
             throw new IllegalStateException("Please call setBorderDecorated(true) first.");
         }
         this.rightBorderComponent = c;
         mainContentPanel.add(rightBorderComponent, BorderLayout.EAST);
     }
     
     /**
      * Sets the component representing the left ({@link BorderLayout#SOUTH})
      * frame border. All border components have a default which is automatically
      * installed and can have their size and color altered. For any (more
      * complex) painting, it is necessary for the user to install their own
      * component (i.e. Gradients etc.)
      * 
      * @param c	the component painting the left border.
      */
     public void setBottomBorderComponent(Component c) {
         if(!isBorderDecorated) {
             throw new IllegalStateException("Please call setBorderDecorated(true) first.");
         }
         this.bottomBorderComponent = c;
         mainContentPanel.add(bottomBorderComponent, BorderLayout.SOUTH);
     }
     
     /**
      * Checks the os system property for the current os and
      * returns an enum specifying a style for that particular
      * os.
      * 
      * @return	{@link Style}
      */
     private static Style getNativeStyle() {
     	String osName = System.getProperty("os.name");
 
         if (osName.toLowerCase(Locale.ENGLISH).startsWith("mac os x")) {
         	return Style.NATIVE_OSX;
         }else if(osName.toLowerCase(Locale.ENGLISH).startsWith("window")) {
         	return Style.NATIVE_WIN;
         }else if(osName.toLowerCase(Locale.ENGLISH).startsWith("linux")) {
         	return Style.NATIVE_LINUX;
         }
         return Style.DEFAULT;
     }
 	
 	/**
      * If a custom style (i.e. non-native prebuilt style) is desired
      * this method must be called with "true" before this frame
      * is instantiated.
      * 
      * @param b
      */
     public static void overrideNativeStyle(boolean b) {
     	DesktopFrame.NATIVE_STYLE_OVERRIDE = b;
     }
 	
 	/**
      * Returns a flag indicating whether this frame is set to
      * be maximizable.
      * @return  flag indicating whether this frame is set to
      * be maximizable.
      */
     public boolean isMaximizable() {
         return true;
     }
     
     /**
      * Returns a flag indicating whether this frame is set to
      * be iconifiable.
      * @return  flag indicating whether this frame is set to
      * be iconifiable.
      */
     public boolean isIconifiable() {
         return true;
     }
     
     /**
      * Sets the flag indicating whether borders should be painted around this frame.
      * Additionally, the user may also specify whether the default borders should 
      * be installed.
      * 
      * @param b						border decorated flag.
      * @param useDefaultBorders		flag indicating the desire to use the default 
      * 								borders.
      */
     public void setBorderDecorated(boolean b, boolean useDefaultBorders) {
         this.isBorderDecorated = b;
         if(useDefaultBorders) {
             installDefaultBorders();
         }
     }
     
     /**
      * Installs default border components.
      */
     private void installDefaultBorders() {
         Panel lBorder = new Panel();
         lBorder.setBackground(getBorderColor());
         setLeftBorderComponent(lBorder);
         
         Panel rBorder = new Panel();
         rBorder.setBackground(getBorderColor());
         setRightBorderComponent(rBorder);
         
         Panel bBorder = new Panel();
         bBorder.setBackground(getBorderColor());
         setBottomBorderComponent(bBorder);
     }
     
     /**
      * Called from {@link Style#apply(TempFrame)} to
      * install native Windows os look and feel to this
      * frame.
      */
     private void installWindowsBorders() {
     	removeBorders();
     	
     	Component lBorder = new Panel() {
     		@Override
     		public void paint(Graphics g) {
     			super.paint(g);
     			Rectangle r = g.getClipBounds();
     			g.setColor(getBorderColor());
     			g.fillRect(r.x, r.y, r.width, r.height);
     			g.setColor(Color.WHITE);
         		g.drawLine(1,0,1,r.height);
     		}
     		@Override
     		public void update(Graphics g) {
     			paint(g);
     		}
     	};
         lBorder.setBackground(getBorderColor());
     	setLeftBorderComponent(lBorder);
         
         Component rBorder = new Panel() {
         	public void paint(Graphics g) {
         		super.paint(g);
         		Rectangle r = g.getClipBounds();
         		g.setColor(getBorderColor());
     			g.fillRect(r.x, r.y, r.width, r.height);
         		g.setColor(Color.WHITE);
         		g.drawLine(r.width - 2,0,r.width - 2,r.height - 1);
         	}
         	@Override
         	public void update(Graphics g) {
         		paint(g);
         	}
         };
         rBorder.setBackground(getBorderColor());
         setRightBorderComponent(rBorder);
         
         Component bBorder = new Panel() {
         	@Override
         	public void paint(Graphics g) {
         		super.paint(g);
         		Rectangle r = g.getClipBounds();
         		g.setColor(getBorderColor());
     			g.fillRect(r.x, r.y, r.width, r.height);
         		g.setColor(Color.WHITE);
         		g.drawLine(1,r.height - 2,r.width - 2,r.height - 2);
         		g.drawLine(1, 0, 1, r.height - 2);
         		g.drawLine(r.width - 2, 0, r.width - 2, r.height - 2);
         	}
         	@Override
         	public void update(Graphics g) {
         		paint(g);
         	}
         };
         bBorder.setBackground(getBorderColor());
         setBottomBorderComponent(bBorder);
     }
     
     /**
      * Removes the border components.
      */
     private void removeBorders() {
     	removeBorderCursorHandlers();
     	if(leftBorderComponent != null) {
     		((Container)mainContentPanel).remove(leftBorderComponent);
     		leftBorderComponent = null;
     	}
     	if(rightBorderComponent != null) {
     		((Container)mainContentPanel).remove(rightBorderComponent);
     		rightBorderComponent = null;
     	}
     	if(bottomBorderComponent != null) {
     		((Container)mainContentPanel).remove(bottomBorderComponent);
     		bottomBorderComponent = null;
     	}
     }
     
     /**
      * Uninstalls the handler which manages cursors over the borders.
      */
     private void removeBorderCursorHandlers() {
     	if(leftBorderComponent != null) {
             leftBorderComponent.removeMouseListener(cursorManager);
             leftBorderComponent.removeMouseMotionListener(cursorManager);
         }
         if(rightBorderComponent != null) {
             rightBorderComponent.removeMouseListener(cursorManager);
             rightBorderComponent.removeMouseMotionListener(cursorManager);
         }
         if(bottomBorderComponent != null) {
             bottomBorderComponent.removeMouseListener(cursorManager);
             bottomBorderComponent.removeMouseMotionListener(cursorManager);
         }
     }
     
     /**
      * Sets the color that will be used to paint the border.
      * @param c		the border color
      */
     public void setBorderColor(Color c) {
         this.borderColor = c;
     }
     
     /**
      * Returns the color that is used to paint the border
      * @return	the current border color
      */
     public Color getBorderColor() {
         return borderColor;
     }
     
     /**
      * Sets the size in pixels of the frame border
      * 
      * @param size	the frame border size.
      */
     public void setBorderSize(int size) {
         borderSize = size;
         if(isDisplayable()) {
             reshapeBorders();
         }
     }
     
     /**
      * Returns the border size.
      * @return	the border size.
      */
     public int getBorderSize() {
         return borderSize;
     }
     
     /**
      * Initializes the border component sizes.
      */
     private void reshapeBorders() {
         if(isBorderDecorated) {
             if(leftBorderComponent != null) {
                 leftBorderComponent.setPreferredSize(new Dimension(borderSize, getPreferredSize().height - titleBarHeight));
             }
             if(rightBorderComponent != null) {
                 rightBorderComponent.setPreferredSize(new Dimension(borderSize, getPreferredSize().height - titleBarHeight));
             }
             if(bottomBorderComponent != null) {
                 bottomBorderComponent.setPreferredSize(new Dimension(getPreferredSize().width, borderSize));
             }
             invalidate();
             repaint();
         }
     }
     
     /**
      * Removes or installs the size of the border as
      * to render it visible/invisible
      */
     public void setBordersVisible(boolean visible) {
     	leftBorderComponent.setVisible(visible);
     	rightBorderComponent.setVisible(visible);
     	bottomBorderComponent.setVisible(visible);
     }
     
     /**
      * Sets the height in pixels of the title bar.
      * 
      * @param height	the height of the title bar
      */
     public void setTitleBarHeight(int height) {
         titleBarHeight = height;
     }
     
     /**
      * Returns the title bar height
      * 
      * @return	the current title bar height
      */
     public int getTitleBarHeight() {
         return titleBarHeight;
     }
     
     /**
      * Creates and returns the default title bar used if
      * no user-specified title bar has been set.
      * 
      * @return  the default title bar
      */
     private TitleBar createTitleBar(Style style) {
     	TitleBar titleBar = null;
     	switch(style) {
 	    	case NATIVE_LINUX:
 	    	case NATIVE_WIN: {
 	    		titleBar = new WindowsTitleBar();
 	    		titleBar.setPreferredSize(new Dimension(0, titleBarHeight));
 	            break;
 	    	}
 	    	case NATIVE_OSX: {
 	    		titleBar = new OSXTitleBar();
 	    		titleBar.setPreferredSize(new Dimension(0, titleBarHeight));
 	            break;
 	    	}
 	    	case CUSTOM: {
 	    		break;
 	    	}
 	    	case BARCHART: {
 	    		titleBar = new BarChartTitleBar();
 	    		titleBar.setPreferredSize(new Dimension(0, titleBarHeight));
 	            break;
 	    	}
 	    	default: {
 	    		titleBar = new DefaultTitleBar();
 	    		titleBar.setPreferredSize(new Dimension(0, titleBarHeight));
 	            TitleBarControl control = createDefaultTitleBarControl();
 	            control.setPreferredSize(new Dimension(60,20));
 	            titleBar.addTitleBarControl(control);
 	            break;
 	    	}
     	}
     	
     	return titleBar; 
     }
     
     /**
      * Creates the default title bar buttons.
      * @return	the default title bar buttons.
      */
     private TitleBarControl createDefaultTitleBarControl() {
     	return new DefaultTitleBarControl();
     }
     
     /**
      * Sets the title shown in this frame's titlebar.
      * 
      * @param	title	the frame's title
      */
     @Override
     public void setTitle(String title) {
         super.setTitle(title);
         if(titleBar != null) {
         	titleBar.setTitle(title);
         	titleBar.repaint();
         }
     }
     
     /**
      * Removes the {@link TitleBar} component
      */
     public void removeTitleBar() {
        Component c = titleBar;
         if(c != null) {
         	remove(c);
         }
         
         this.titleBar = null;
         setTitleBarHeight(0);
         invalidate();
         validate();
         repaint();
     }
 	
 	/**
      * Used internally to denote the sides of this frame.
      */
     private enum Side {
         N(0,-1), W(-1,0), S(0,1), E(1,0), NW(-1,-1), NE(1,-1), SW(-1,1), SE(1,1), NONE(0,0);
         
         private int h;
         private int v;
         
         private Side(int horizontal, int vertical) {
             this.h = horizontal;
             this.v = vertical;
         }
         
         public static Side sideFor(Rectangle r, Point p, int margin) {
             int horiz = 0;
             int vert = 0;
             margin += 2;
             if(p.x >= r.x && p.x <= r.x + margin) horiz = -1;
             else if(p.x >= r.getMaxX() - margin && p.x <= r.getMaxX()) horiz = 1;
             if(p.y >= r.y && p.y <= r.y + margin) vert = -1;
             else if(p.y >= (r.getMaxY() - margin) && p.y <= r.getMaxY()) vert = 1;
             
             return sideFor(horiz, vert);
         }
         
         public static Side sideFor(int horiz, int vert) {
             for(Side s : values()) {
                 if(s.h == horiz && s.v == vert) return s;
             }
             return NONE;
         }
         
         public int vertical() {
         	return v;
         }
         
         public int horizontal() {
         	return h;
         }
     }
     
     /**
      * Dynamically alters the cursor being displayed depending on the
      * current mouse location.
      * 
      * @param side
      */
     private void setCursorForSide(Side side) {
         switch(side) {
             case N : setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR)); break;
             case W : setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR)); break;
             case S : setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR)); break;
             case E : setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)); break;
             case NW : setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR)); break;
             case NE : setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)); break;
             case SW : setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR)); break;
             case SE : setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR)); break;
             default : setCursor(Cursor.getDefaultCursor());
         }
     }
     
     /**
      * Adds the specified {@link DragListener} to this {@code DesktopFrame}
      * @param d		the {@code DragListener} to add.
      */
     public void addDragListener(DragListener d) {
     	if(d != null) {
     		dragListeners.add(d);
     	}
     }
     
     /**
      * Removes the specified {@link DragListener}
      * @param d
      */
     public void removeDragListener(DragListener d) {
     	dragListeners.remove(d);
     }
     
     /**
      * Fires a window dragged event to registered listeners.
      * 
      * @param oldLoc
      * @param newLoc
      */
     public void fireWindowDragged(Point oldLoc, Point newLoc) {
     	for(DragListener l : dragListeners) {
     		l.windowDragged(oldLoc, newLoc);
     	}
     }
     
     /**
      * Fires an event notifiying {@link DragListener}s that
      * dragging has stopped.
      */
     public void fireDraggingStopped() {
     	for(DragListener l : dragListeners) {
     		l.draggingStopped();
     	}
     }
     
     /**
      * Gathers and stores information about the graphics environment
      * such as the combined screen area of a multiscreen environment
      * and the shape its bounding polygon.
      */
     private void getEnvironmentInfo() {
     	 GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
          GraphicsDevice[] gs = ge.getScreenDevices();
          virtualDeviceBounds = new ArrayList<Rectangle>();
          virtualArea = new Area();
          for (int j = 0; j < gs.length; j++) {
              GraphicsDevice gd = gs[j];
              GraphicsConfiguration[] gc = gd.getConfigurations();
              for (int i=0; i < gc.length; i++) {
             	 Rectangle r = gc[i].getBounds();
             	 java.awt.Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc[i]);
             	 r.x = r.x + screenInsets.left;
             	 r.y = r.y + screenInsets.top;
             	 r.setSize(r.width - (screenInsets.left + screenInsets.right),
             		r.height - (screenInsets.top + screenInsets.bottom));
             	 virtualDeviceBounds.add(r);
             	 virtualArea.add(new Area(r));
              }
          } 
     }
     
     /**
      * Class which listens to mouse movements and alters the cursor to 
      * the appropriate cursor for the given mouse location.
      * 
      * @author David Ray
      */
     class CursorManager extends MouseAdapter {
         private boolean isPressed;
         
         @Override
         public void mouseMoved(MouseEvent m) {
            if(!isResizable() || isFakeMaximized) return;
 	       Side side = Side.sideFor(getBounds(), m.getLocationOnScreen(), resizeMargin);
 	       setCursorForSide(side);
 	    }
         @Override
         public void mouseExited(MouseEvent m) {
             if(!isPressed) {
             	setCursor(Cursor.getDefaultCursor());
             }
         }
         @Override
         public void mousePressed(MouseEvent m) {
         	if(isFakeMaximized) return;
             isPressed = true;
             Side side = Side.sideFor(getBounds(), m.getLocationOnScreen(), resizeMargin);
             setCursorForSide(side);
         }
         @Override
         public void mouseReleased(MouseEvent m) {
             isPressed = false;
             setCursor(Cursor.getDefaultCursor());
         }
     }
 	
 	/**
      * Handles the mouse events to control frame dragging
      */
     class MouseHandler extends MouseAdapter implements MouseMotionListener {
     	private Rectangle frameRect;
     	private Point startPoint;
     	private Side startSide;
     	
     	public MouseHandler() {
     		
     	}
     	
     	@Override
     	public void mousePressed(MouseEvent m) {
     		frameRect = getBounds();
     		startPoint = MouseInfo.getPointerInfo().getLocation();
     		startSide = Side.sideFor(getBounds(), startPoint, resizeMargin);
     		
     		if(titleBar != null && titleBar.isControlHit(m)) {
     			titleBar.mousePressed(m);
     		}
     	}
     	
     	public void mouseDragged(MouseEvent m) {
     		if(isFakeMaximized) return;
     		
     		Point p = MouseInfo.getPointerInfo().getLocation();
     		
     		int xAmount = (p.x - startPoint.x);
     		int yAmount = (p.y - startPoint.y);
     		DesktopFrame.this.isResizing = !isMoveOp(startSide);
     		
     		Rectangle prev = frameRect;
     		frameRect = DesktopFrame.this.isResizing ? 
     			resizeRectangle(frameRect, startSide, xAmount, yAmount) :
     				moveRectangle(frameRect, xAmount, yAmount);
     			
     		setBounds(frameRect);
     		fireWindowDragged(prev.getLocation(), frameRect.getLocation());
     		startPoint = p;
     		
     		repaint();
     	}
     	
     	public void mouseReleased(MouseEvent m) {
     		if(titleBar != null && titleBar.isControlHit(m)) {
     			titleBar.mouseReleased(m);
     		}
     		
     		fireDraggingStopped();
     	}
     	
     	public void mouseClicked(MouseEvent e) {
     		if(e.getClickCount() > 1) {
     			if((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH ||
     				isFakeMaximized) {
     				setExtendedState(JFrame.NORMAL);
     				setBorderSize(5);
     				setResizable(true);
     				reshapeBorders();
     			}else{
     				setExtendedState(JFrame.MAXIMIZED_BOTH);
     				setBorderSize(0);
     				setResizable(false);
     				reshapeBorders();
     			}
     		}
     	}
     	
     	private Rectangle moveRectangle(Rectangle r, int xAmount, int yAmount) {
     		if((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
     			return r;
     		}
     		Rectangle rect = new Rectangle(r);
     		rect.setLocation(rect.x + xAmount, rect.y + yAmount);
     	
     		return rect;
     	}
     	
     	private Rectangle resizeRectangle(Rectangle r, Side side, int deltaX, int deltaY) {
     		if(!isResizable()) return r;
     		
     		Rectangle rect = new Rectangle(r);
     		if(side.vertical() != 0 || side.horizontal() != 0) {
     			if(side.vertical() == -1) {
     				resizeTopTo(rect, r.y + deltaY);
     			}else if(side.vertical() == 1) {
     				resizeBottomTo(rect, (int)r.getMaxY() + deltaY);
     			}
     			
     			if(side.horizontal() == -1) {
     				resizeLeftTo(rect, r.x + deltaX);
     			}else if(side.horizontal() == 1) {
     				resizeRightTo(rect, (int)r.getMaxX() + deltaX);
     			}
     		}
     		rect.height = Math.max(titleBarHeight, rect.height);
     		rect.width = Math.max(320, rect.width);
     		return rect;
     	}
     	
     	private boolean isMoveOp(Side side) {
     		return side == Side.NONE;
     	}
     	
     	private void resizeLeftTo(Rectangle r, int x) {
     		int sourceX = r.x;
     		r.width -= (x - sourceX);
     		r.x = x;
     	}
     	
     	private void resizeRightTo(Rectangle r, int x) {
     		int sourceX = (int)r.getMaxX();
     		r.width += (x - sourceX);
     	}
     	
     	private void resizeTopTo(Rectangle r, int y) {
     		int sourceY = r.y;
     		r.height -= (y - sourceY);
     		r.y = y;
     	}
     	
     	private void resizeBottomTo(Rectangle r, int y) {
     		int sourceY = (int)r.getMaxY();
     		r.height += (y - sourceY);
     	}
     }
     
     /**
      * Class which represents the controls on a given title bar such as 
      * minimize, maximize, and close.
      * 
      * @author David Ray
      *@see TitleBarControl
      */
     class DefaultTitleBarControl extends TitleBarControl {
     	private static final int SIDE_MARGIN = 10;
     	private Rectangle2D minimizeRect;
     	private Rectangle2D maximizeRect;
     	private Rectangle2D closeRect;
     	
     	private boolean shapesInitialized;
     	
     	public DefaultTitleBarControl() {
     		createControlShapes();
     		
     		addMouseListener(new MouseAdapter() {
     			public void mousePressed(MouseEvent e) {
     				if(minimizeRect.contains(e.getPoint())) {
     					setExtendedState(ICONIFIED);
     				}
     				if(maximizeRect.contains(e.getPoint())) {
     					if((getExtendedState() & MAXIMIZED_BOTH) == MAXIMIZED_BOTH) {
     						setExtendedState(NORMAL);
     					}else{
     						setExtendedState(MAXIMIZED_BOTH);
     					}
     				}
     				if(closeRect.contains(e.getPoint())) {
     					System.exit(0);
     				}
     			}
     		});
     	}
     	@Override
     	public void paint(Graphics g) {
     		if(!shapesInitialized) {
     			createControlShapes();
     			shapesInitialized = true;
     		}
     		
     		Graphics2D g2 = (Graphics2D)g;
     		g2.setColor(Color.BLACK);
     		
     		g2.setStroke(new BasicStroke(2));
     		g2.drawLine((int)minimizeRect.getX(), (int)minimizeRect.getMaxY() - 3, 
     			(int)minimizeRect.getMaxX() - 3, (int)minimizeRect.getMaxY() - 3);
     		
     		g2.draw(new Rectangle2D.Double(maximizeRect.getX() + 3, maximizeRect.getY() + 3, maximizeRect.getWidth() - 6, maximizeRect.getHeight() - 6));
     		
     		g2.drawLine((int)closeRect.getX() + 3, (int)closeRect.getY() + 3, (int)closeRect.getMaxX() - 3, (int)closeRect.getMaxY() - 3);
     		g2.drawLine((int)closeRect.getX() + 3, (int)closeRect.getMaxY() - 3, (int)closeRect.getMaxX() - 3, (int)closeRect.getY() + 3);
     	}
     	@Override
     	public void update(Graphics g) {
     		paint(g);
     	}
     	
     	private void createControlShapes() {
     		int y = (getHeight() - 15) / 2;
     		minimizeRect = new Rectangle2D.Double(SIDE_MARGIN, y, 15, 15);
     		maximizeRect = new Rectangle2D.Double(SIDE_MARGIN + 16, y, 15, 15);
     		closeRect = new Rectangle(SIDE_MARGIN + 32, y, 15, 15);
     	}
     }
     
     /**
      * Creates a default {@link TitleBar}.
      */
     class DefaultTitleBar extends TitleBar {
     	@Override
 		protected void paintTitle(Graphics2D g2) {
 			FontMetrics fm = g2.getFontMetrics();
 	        int titleWidth = SwingUtilities.computeStringWidth(fm, title);
 	        g2.setColor(Color.BLACK);
 	        Rectangle r = g2.getClipBounds();
 	        int textX = (r.width - titleWidth) / 2;
 	        int textY = ((r.height) / 2) - ((fm.getAscent() + fm.getDescent()) / 2) + fm.getAscent();
 	        g2.drawString(title, textX, textY);
 		}
 
 		@Override
 		protected void fillTitleBar(Graphics2D g2) {
 			g2.setColor(Color.LIGHT_GRAY);
 			g2.fillRect(0, 0, getWidth(), getHeight());
 		}
     }
     
     /**
      * Defines methods which make available notifications regarding
      * the stop and start of drag operations.
      */
     public static interface DragListener {
     	public void windowDragged(Point oldLoc, Point newLoc);
     	public void draggingStopped();
     }
 }
