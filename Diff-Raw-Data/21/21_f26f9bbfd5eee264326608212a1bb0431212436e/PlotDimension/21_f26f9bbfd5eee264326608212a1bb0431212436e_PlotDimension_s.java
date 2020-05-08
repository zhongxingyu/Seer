 package no.runsafe.creativetoolbox;
 
 import no.runsafe.framework.minecraft.RunsafeLocation;
 import no.runsafe.framework.minecraft.RunsafeWorld;
 
 import java.awt.geom.Rectangle2D;
 
 public class PlotDimension
 {
 	public PlotDimension(PlotCalculator calculator, Rectangle2D area, RunsafeWorld world)
 	{
 		this.calculator = calculator;
 		this.area = area;
 		this.world = world;
 	}
 
 	public long getMinimumRow()
 	{
 		return calculator.getRow((long) area.getMinY());
 	}
 
 	public long getMaximumRow()
 	{
 		return calculator.getRow((long) area.getMaxY());
 	}
 
 	public long getMinimumColumn()
 	{
 		return calculator.getColumn((long) area.getMinX());
 	}
 
 	public long getMaximumColumn()
 	{
 		return calculator.getColumn((long) area.getMaxX());
 	}
 
 	public RunsafeLocation getMinPosition()
 	{
 		return calculator.getMinPosition(world, area);
 	}
 
 	public RunsafeLocation getMaxPosition()
 	{
 		return calculator.getMaxPosition(world, area);
 	}
 
 	public PlotDimension expandToInclude(Rectangle2D including)
 	{
		double x = area.getMinX();
		double y = area.getMinY();
		double w = area.getWidth();
		double h = area.getHeight();
 
		if (including.getMinX() < x)
			x = including.getMinX();
		else if (including.getMaxX() > x + w)
			w = including.getMaxX() - x;

		if (including.getMinY() < y)
			y = including.getMinY();
		else if (including.getMaxY() > y + h)
			h = including.getMaxY() - y;

		return new PlotDimension(calculator, new Rectangle2D.Double(x, y, w, h), world);
 	}
 
 	@Override
 	public String toString()
 	{
 		return String.format("(%d,%d -> %d,%d)", getMinimumColumn(), getMinimumRow(), getMaximumColumn(), getMaximumRow());
 	}
 
 	private final PlotCalculator calculator;
 	private final Rectangle2D area;
 	private final RunsafeWorld world;
 }
