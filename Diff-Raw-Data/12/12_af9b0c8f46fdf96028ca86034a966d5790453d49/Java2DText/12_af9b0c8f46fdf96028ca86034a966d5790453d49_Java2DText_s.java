 package ca.couchware.wezzle2d.java2d;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.font.TextLayout;
 import java.awt.geom.Rectangle2D;
 import java.net.URL;
 
 import ca.couchware.wezzle2d.Text;
 import ca.couchware.wezzle2d.Util;
 
 /**
  * The Java2DText class provides a java2D implementation of the text interface.
  * The font is hard coded as bubbleboy2 but maybe be changed in the future.
  * 
  * @author Kevin
  *
  */
 
 public class Java2DText implements Text
 {
 	/** The URL. */
 	private URL url;
 	
 	/** The font. */
 	private Font font;
 	
 	/** The size of the font */
 	private float size;
 	
 	/** The color of the text */
 	private Color color;
 	
 	/** The text */
 	private String text;
 	private TextLayout textLayout;
 	
 	/** The game window to which this text is going to be drawn */
 	private Java2DGameWindow window;	
 
 	/** The x offset for anchor. */
 	private int anchorX;
 	
 	/** The y offset for anchor. */
 	private int anchorY;
 	
 	/** The current anchor */
 	private int currentAnchor;
 
 	/**
 	 * The constructor loads the default text settings.
 	 * The default settings are:
 	 * 
 	 * size: 14pt.
 	 * Color: Black.
 	 * Text: "".
 	 * 
 	 * @param window The game window to be drawn to.
 	 */
 	public Java2DText(Java2DGameWindow window)
 	{
 		this.url = this.getClass().getClassLoader().getResource("resources/bubbleboy2.ttf");
 		this.size = 24.0f;
 		this.color = Color.black;
 		this.text = "";
 		this.window = window;
 		
 		// Set the default anchors.
 		this.currentAnchor = (Text.TOP | Text.LEFT);
 		
 		// Setup the font.
 		try
 		{			
 			this.font = Font.createFont(Font.TRUETYPE_FONT, url.openStream());	
 			this.font = font.deriveFont(this.size);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}	
 
 	/**
 	 * Gets the text.
 	 * @return The text.
 	 */
 	public String getText()
 	{
 		return text;
 	}
 	
 	/**
 	 * Set the text.
 	 * Recalculate any anchor points.
 	 * 
 	 * @param t The text.
 	 */
 	public void setText(String t)
 	{
 		this.text = t;
 		this.textLayout = new TextLayout(t, font, window.getDrawGraphics().getFontRenderContext());
 		
 		// Recalculate the anchor points.
 		this.setAnchor(this.currentAnchor);
 	}
 	
 	/**
 	 * Gets the size.
 	 * @return The size.
 	 */
 	public float getSize()
 	{
 		return size;
 	}
 	
 	/**
 	 * Set the color of the text.
 	 * The initial size is set to 14.
 	 * 
 	 * @param s The size.
 	 */
 	public void setSize(float s)
 	{
 		this.size = s;
 		this.font = font.deriveFont(this.size);
 	}
 	
 //	/**
 //	 * Gets the font.
 //	 * @return The font.
 //	 */
 //	public Font getFont()
 //	{
 //		return font;
 //	}
 //
 //	/**
 //	 * Sets the font.
 //	 * @param font The font to set.
 //	 */
 //	public void setFont(Font font)
 //	{
 //		this.font = font;
 //	}
 
 	/**
 	 * Gets the color.
 	 * @return The color.
 	 */
 	public Color getColor()
 	{
 		return color;
 	}
 
 	/**
 	 * Set the text color.
 	 * The initial color is black.
 	 * 
 	 * @param c The color to set to.
 	 */
 	public void setColor(Color c)
 	{
 		this.color = c;
 	}
 	
 	/**
 	 * Set the anchor of the text box. The anchor is initially set to the top left. 
 	 * 
 	 * @param x The x anchor coordinate with respect to the top left corner of the text box.
 	 * @param y The y anchor coordinate with respect to the top left corner of the text box.
 	 */
 	public void setAnchor(int anchor)
 	{
 		if (text.equals("") == true)
 			return;
 		
 		// Get the width and height of the font.
 //		Rectangle2D bounds = window.getFontMetrics(this.font).getStringBounds(this.text, window.getDrawGraphics());
 //		double strWidth = bounds.getWidth();		
 //		double strHeight = bounds.getHeight();
 		Rectangle2D bounds = textLayout.getBounds();
 		
//		Util.handleMessage("" + bounds.getMinX(), null);
 		// window.getFontMetrics(this.font).getHeight();
 		
 		// They Y's.
 		if((anchor & Text.BOTTOM) == Text.BOTTOM)
 		{
 			this.anchorY = 0;
 		}
 		else if((anchor & Text.VCENTER) == Text.VCENTER)
 		{
 			this.anchorY = (int) bounds.getCenterY();
 		}
 		else if((anchor & Text.TOP) == Text.TOP)
 		{
 			this.anchorY = (int) bounds.getMaxY();
 		}
 		else
 		{
 			// The default y value is bottom.
 			this.anchorY = 0;
 		}
 		
 		// The X's. 
 		if((anchor & Text.LEFT) == Text.LEFT)
 		{
 			this.anchorX = 0;
 		}
 		else if((anchor & Text.HCENTER) == Text.HCENTER)
 		{
			this.anchorX = (int) bounds.getCenterX();			
 		}
 		else if((anchor & Text.RIGHT) == Text.RIGHT)
 		{
 			this.anchorX = (int) bounds.getMaxX();
 		}
 		else
 		{
 			// The default x value is Left.
 			this.anchorX = 0;	
 		}
 		
 		// Set the anchor.
 		this.currentAnchor = anchor;		
 	}
 	
 	/**
 	 * Draw the text onto the graphics context provided.
 	 * 
 	 * @param x The x location at which to draw the text.         
 	 * @param y The y location at which to draw the text.       
 	 */
 	public void draw(int x, int y)
 	{
 		// Return immediately is string is empty.
 		if (text.equals("") == true)
 			return;
 		
 		try
 		{
 			// Get the graphics.
 			Graphics2D g = window.getDrawGraphics();			
 			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                     RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
 					RenderingHints.VALUE_FRACTIONALMETRICS_ON);
 			
 			// Test url.
 			assert (url != null);
 						
 			// Test font.
 			assert (font != null);
 			
 			g.setFont(font);
 			g.setColor(this.color);			
 			
 //			g.drawLine(0, 100, 100, 100);
 //			TextLayout tl = new TextLayout(text, font, g.getFontRenderContext());
 			textLayout.draw(g, x - this.anchorX, y - this.anchorY);
 //			Util.handleMessage(tl.getBounds().toString(), null);
 //			g.drawString(this.text, 0, y - this.anchorY);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}		
 	}
 }
