 package gort.ui;
 
 import java.awt.*;
 
 /********************************************************************************
 ** This canvas derived class is used to display an icon image.
 */
 
 public class Icon extends Canvas
 {
 	/********************************************************************************
 	** Constructor.
 	*/
 
 	public Icon()
 	{
 		setBackground(SystemColor.control);
 	}
 
 	/********************************************************************************
 	** Constructor.
 	*/
 
 	public Icon(int nHorzAlign, int nVertAlign)
 	{
 		m_nHorzAlign = nHorzAlign;
 		m_nVertAlign = nVertAlign;
 		
 		setBackground(SystemColor.control);
 	}
 
 	/********************************************************************************
 	** Set the image to display.
 	*/
 
 	public void setImage(Image oImage)
 	{
 		m_imgIcon  = oImage;
 		m_rcBounds = new Rectangle(0, 0, DEF_WIDTH, DEF_HEIGHT);
 
 		if (m_imgIcon != null)
 		{
 			m_rcBounds.width  = m_imgIcon.getWidth(null);
 			m_rcBounds.height = m_imgIcon.getHeight(null);
 		}
 
 		repaint();
 	}
 
 	public void setImage(Image oImage, Rectangle rcBounds)
 	{
 		m_imgIcon  = oImage;
 		m_rcBounds = rcBounds.getBounds();
 
 		repaint();
 	}
 
 	/********************************************************************************
 	** Get the preferred size.
 	*/
 
 	public Dimension getPreferredSize()
 	{
 		return m_rcBounds.getSize();
 	}
 
 	/********************************************************************************
 	** Paint the control.
 	*/
 
 	public void paint(Graphics g)
 	{
 		Dimension dmSize = getSize();
 
 		// Default to top-left.
 		int nXOffset = 0;
 		int nYOffset = 0;
 
 		// Calculate image top-left corner.
 		if (m_nHorzAlign == ALIGN_CENTRE)
 			nXOffset = (dmSize.width - m_rcBounds.width) / 2;
 		else if (m_nHorzAlign == ALIGN_RIGHT)
 			nXOffset = dmSize.width - m_rcBounds.width;
 		
 		if (m_nVertAlign == ALIGN_MIDDLE)
			nXOffset = (dmSize.height - m_rcBounds.height) / 2;
 		else if (m_nVertAlign == ALIGN_BOTTOM)
 			nYOffset = dmSize.height - m_rcBounds.height;
 		
 		// Draw icon, if set.
 		if (m_imgIcon != null)
 		{
 			g.drawImage(m_imgIcon, nXOffset, nYOffset,
 						nXOffset+m_rcBounds.width, nYOffset+m_rcBounds.height,
 						m_rcBounds.x, m_rcBounds.y,
 						m_rcBounds.x+m_rcBounds.width,
 						m_rcBounds.y+m_rcBounds.height, this);
 		}
 	}
 
 	/********************************************************************************
 	** Constants.
 	*/
 
 	// Horizontal alignment.
 	public static final int ALIGN_LEFT   = 0x01;
 	public static final int ALIGN_CENTRE = 0x02;
 	public static final int ALIGN_RIGHT  = 0x04;
 
 	// Vertical alignment.
 	public static final int ALIGN_TOP    = 0x01;
 	public static final int ALIGN_MIDDLE = 0x02;
 	public static final int ALIGN_BOTTOM = 0x04;
 
 	// Default icon dimesions.
 	public static final int DEF_WIDTH  = 32;
 	public static final int DEF_HEIGHT = 32;
 
 	/********************************************************************************
 	** Members.
 	*/
 
 	private int			m_nHorzAlign = ALIGN_CENTRE;
 	private int			m_nVertAlign = ALIGN_MIDDLE;
 	private Image		m_imgIcon    = null;
 	private Rectangle	m_rcBounds   = new Rectangle(0, 0, DEF_WIDTH, DEF_HEIGHT);
 }
