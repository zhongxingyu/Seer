 package edu.utsa.xgui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.RoundRectangle2D;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 import edu.utsa.tagger.view.DropShadowBorder;
 
 
 @SuppressWarnings("serial")
 public class XScrollPane extends XComponent
 {
 	JComponent content;
 	Anchor content_anchor;
 	int vert_pos = 0;
 	VerticalScrollBar vsb = new VerticalScrollBar();
 	Anchor vsb_anchor;
 
 	public XScrollPane(JComponent content)
 	{
 		this.content = content;
 
 		setLayer(vsb, 1);
 
 		vsb_anchor = new Anchor(1, Anchor.TR, Anchor.NOSTRETCH, new Margins(0, 0, 0, 1));
 		add(vsb, vsb_anchor);
 		content_anchor = new  Anchor(Anchor.TL, Anchor.HORIZONTAL);
 		add(content, content_anchor);
 
 		addMouseWheelListener(new MouseWheelListener()
 		{
 			@Override public void mouseWheelMoved(MouseWheelEvent e)
 			{
 				int notches = e.getWheelRotation();
 				if (notches < 0) {
 					scroll(-20, 0);
 				} else {
 					scroll(20, 0);
 				}
 			}
 		});
 	}
 	
 	// called by xcontainer
 	public void pageUp()
 	{
 		scroll((int)(this.getHeight() * -.95), 0);
 	}
 	
 	// called by xcontainer	
 	public void pageDown()
 	{
 		scroll((int)(this.getHeight() * .95), 0);
 	}
 	
 	@Override public Dimension getPreferredSize()
 	{
 		refresh();
 		return content.getPreferredSize();
 	}
 
 	public void refresh()
 	{
 		double p = vert_pos;
 		double b = content.getHeight();
 		double a = this.getHeight();
 
 		if (b <= a)
 		{
 			vsb.setVisible(false);
 			return;
 		}
 		else
 		{
 			vsb.setVisible(true);
 		}
 		
 		if (vert_pos > b - a) vert_pos = (int) (b - a);
 		if (vert_pos < 0) vert_pos = 0;
 
 		content_anchor.margins = new Margins(-vert_pos, 0, 0, 0);
 		double sb_height = (a / b) * a;
 		double sb_top = (p / b) * a;
 		vsb.setPreferredSize(new Dimension(vsb.getPreferredSize().width, (int) sb_height));
 		vsb_anchor.margins = new Margins((int) sb_top, 0, 0, 1);
 		revalidate();
 	}
 	
 	@Override public void draw(Graphics2D g)
 	{
 		refresh();
 	}
 	
 	private Timer timer = new Timer(20, new ActionListener()
 	{
 		@Override public void actionPerformed(ActionEvent e)
 		{
 			int step = (scroll_end - vert_pos) / 5;
 			scroll(step, 0);
 			if (Math.abs(scroll_end - vert_pos) < 5)
 			{
 				scroll(scroll_end - vert_pos, 0);
 				timer.stop();
 			}
 		}
 	});
 	
 	private int scroll_end;
 
 	public void scrollTo(int y, int x)
 	{
 		scroll_end = y;
 		if (scroll_end > content.getHeight() - getHeight())
 		{
 			scroll_end = content.getHeight() - getHeight();
 		}
 		timer.start();
 	}
 
 	public void scroll(int y, int x)
 	{
 		vert_pos += y;
 		refresh();
 	}
 
 	public class VerticalScrollBar extends XComponent implements MouseMotionListener
 	{
 		public VerticalScrollBar()
 		{
 			addMouseMotionListener(this);
			setPreferredSize(new Dimension(15, 0));
 		}
 		
 		@Override public void draw(Graphics2D g)
 		{
 			g.setColor(isMouseover() ? new Color(112, 112, 112) : new Color(112, 112, 112, 150));
 			g.fill(SwingUtilities.calculateInnerArea(this,  null));
 		}
 
 		public void clicked()
 		{
 			XScrollPane.this.refresh();
 		}
 
 		@Override public void mouseDragged(MouseEvent e)
 		{
 			scroll(e.getY(), 0);
 		}
 
 		@Override public void mouseMoved(MouseEvent e){}
 	}
 }
