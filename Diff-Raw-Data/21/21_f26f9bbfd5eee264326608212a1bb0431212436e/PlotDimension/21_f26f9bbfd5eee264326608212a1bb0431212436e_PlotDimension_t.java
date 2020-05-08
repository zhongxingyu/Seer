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
		double newX = Math.min(area.getMinX(), including.getMinX());
		double newY = Math.min(area.getMinY(), including.getMinY());
		double newW = Math.max(area.getMaxX(), including.getMaxX()) - newX;
		double newH = Math.max(area.getMaxY(), including.getMaxY()) - newY;
 
		return new PlotDimension(calculator, new Rectangle2D.Double(newX, newY, newW, newH), world);
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
