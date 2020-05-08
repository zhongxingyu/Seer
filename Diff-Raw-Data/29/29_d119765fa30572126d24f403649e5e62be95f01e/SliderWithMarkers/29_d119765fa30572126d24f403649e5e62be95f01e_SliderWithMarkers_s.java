 package mensonge.userinterface;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import javax.swing.JSlider;
 
 public class SliderWithMarkers extends JSlider
 {
 
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 1L;
 	private float position1 = -1.0f;
 	private float position2 = -1.0f;
 
 	public SliderWithMarkers(int orientation)
 	{
 		super(orientation);
 		this.setOpaque(false);
 		this.setPaintTicks(false);
 		this.setPaintLabels(false);
 		this.setSnapToTicks(false);
 		this.setMajorTickSpacing(1);
 		this.setMinorTickSpacing(0);
 		this.setMinimum(0);
 		this.setValue(0);
 		this.setMaximum(1000);
 	}
 
 	public void setMarkerOneAt(float position)
 	{
		position1 = position;
 		this.repaint();
 
 	}
 
 	public void setMarkerTwoAt(float position)
 	{
		position2 = position;
 		this.repaint();
 	}
 
 	protected void paintComponent(Graphics g)
 	{
 		int h = getHeight();
 		int w = getWidth();

 		if (position1 >= 0)
 		{
 			g.setColor(Color.RED);
 			int[] polygoneX1 = new int[3];
 			int[] polygoneY1 = new int[3];
 			int pos = Math.round(position1 * w);
 			polygoneX1[0] = pos;
 			polygoneX1[1] = pos + 5;
 			polygoneX1[2] = pos + 10;
 			polygoneY1[0] = h;
 			polygoneY1[1] = h - 7;
 			polygoneY1[2] = h;
 			g.fillPolygon(polygoneX1, polygoneY1, polygoneX1.length);
 			g.fillRect(pos + 5, 0, 1, h);
 		}
 		if (position2 >= 0)
 		{
 			g.setColor(Color.BLUE);
 			int[] polygoneX2 = new int[3];
 			int[] polygoneY2 = new int[3];
 			int pos = Math.round(position2 * w);
 			polygoneX2[0] = pos;
 			polygoneX2[1] = pos + 5;
 			polygoneX2[2] = pos + 10;
 			polygoneY2[0] = 0;
 			polygoneY2[1] = 7;
 			polygoneY2[2] = 0;
 			g.fillPolygon(polygoneX2, polygoneY2, polygoneY2.length);
 			g.fillRect(pos + 5, 0, 1, h);
 		}
 		super.paintComponent(g);
 	}
 }
