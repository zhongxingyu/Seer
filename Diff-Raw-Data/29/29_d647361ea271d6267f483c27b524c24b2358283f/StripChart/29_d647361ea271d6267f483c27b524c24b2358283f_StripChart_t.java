 package widgets;
 
 import java.awt.*;
 import java.awt.geom.*;
 import java.util.*;
 import javax.swing.*;
 
 public class StripChart extends JComponent
 {
 	protected static final Dimension MIN_CHART_DIM = new Dimension(300,50); 
 	
 	protected int oldest = 0;
 	protected int newest = 0;
 	protected float[] ts;
 	protected float[] ys;
 	protected int[] lx;
 	protected int[] ly;
 	protected float lastRepaint = 0;
 
 	protected int maxSampleRate = 2500;
 	protected float timeScale = 30; /* seconds */
 	protected float maxTimeScale = 60; /* seconds */
 	protected float minY = -20;
 	protected float maxY = 20;
 
 	{
 		setOpaque(true);
 	}
 	
 	protected int roundup(int x)
 	{
 		int y = 1;
 		while(y < x)
 			y <<= 1;
 		return y;
 	}
 	
 	public Dimension getPreferredSize() 
 	{
 		return MIN_CHART_DIM;
 	}
 
 	public int getMaxSampleRate()
 	{
 		return maxSampleRate;
 	}
 
 	public void setMaxSampleRate(int maxSampleRate)
 	{
 		this.maxSampleRate = maxSampleRate;
 	}
 
 	public float getTimeScale()
 	{
 		return timeScale;
 	}
 
 	public void setTimeScale(float timeScale)
 	{
 		this.timeScale = timeScale;
 	}
 
 	public float getMaxTimeScale()
 	{
 		return maxTimeScale;
 	}
 
 	public void setMaxTimeScale(float maxTimeScale)
 	{
 		this.maxTimeScale = maxTimeScale;
 	}
 
 	public void setYRange(float minY, float maxY)
 	{
 		this.minY = minY;
 		this.maxY = maxY;
 	}
 
 	public void addPoint(float t, float y)
 	{
 		if(ts == null)
 		{
 			ts = new float[roundup((int) Math.ceil(maxSampleRate * maxTimeScale))];
 			ys = new float[ts.length];
 
 			lx = new int[ts.length];
 			ly = new int[ys.length];
 		}

		// If time has gone backward, flush and start over.
		if(t < lastRepaint)
			oldest = newest;

 		ts[newest & (ts.length - 1)] = t;
 		ys[newest & (ys.length - 1)] = y;
 		++newest;
		if(t - lastRepaint >= 0.1 || t < lastRepaint)
 		{
 			repaint();
 			lastRepaint = t;
 		}
 	}
 
 	protected void paintComponent(Graphics g)
 	{
 		// Freeze "newest" at this instant
 		int current = newest;
 		if(current == oldest)
 			return;
 
 		if(current - oldest > ts.length)
 			oldest = current - ts.length;
 
 		final Insets insets = getInsets();
 		final int width = getWidth() - insets.left - insets.right;
 		final int height = getHeight() - insets.top - insets.bottom;
 
 		g.setColor(getBackground());
 		g.fillRect(0, 0, getWidth(), getHeight());
 		g.setColor(getForeground());
 
 		// scale to the window
 		final float old = ts[(current - 1) & (ts.length - 1)] - timeScale;
 		final float sx =  width / timeScale;
 		final float sy = -height / (maxY - minY);
 		final float tx = -old * sx + insets.left + 0.5f;
 		final float ty = -maxY * sy + insets.top + 0.5f;
 
 		// start at the most recent point and draw backwards in time
 		int i = 0;
 		while(current != oldest)
 		{
 			--current;
 			int ti = current & (ts.length - 1);
 			lx[i] = (int) (ts[ti] * sx + tx);
 			if(lx[i] < insets.left)
 				break;
 			ly[i] = (int) (ys[ti] * sy + ty);
 			++i;
 		}
 
 		g.drawPolyline(lx, ly, i);
 	}
 }
