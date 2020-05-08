 package no.runsafe.creativetoolbox;
 
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.RunsafeWorld;
 
 import java.awt.geom.Rectangle2D;
 import java.util.AbstractList;
 
 public class PlotCalculator implements IConfigurationChanged
 {
 	public Rectangle2D getPlotArea(RunsafeLocation location)
 	{
 		if (!fence.contains(location.getBlockX(), location.getBlockZ()))
 			return null;
 
 		Rectangle2D area = getPlotArea(getColumn(location.getBlockX()), getRow(location.getBlockZ()));
 
 		// Location is in the padding between plots
 		if (!area.contains(location.getBlockX(), location.getBlockZ()))
 			return null;
 
 		return area;
 	}
 
 	Rectangle2D getPlotArea(long column, long row)
 	{
 		Rectangle2D area = new Rectangle2D.Double(
 			getOriginX(column),
 			getOriginY(row),
 			prototype.getWidth(),
 			prototype.getHeight()
 		);
 		if (!fence.contains(area))
 			return null;
 		return area;
 	}
 
 	public RunsafeLocation getDefaultEntrance(RunsafeLocation location)
 	{
 		if (location == null)
 			return null;
 		return getDefaultEntrance(
 			getColumn(location.getBlockX()),
 			getRow(location.getBlockZ())
 		);
 	}
 
 	public RunsafeLocation getDefaultEntrance(long column, long row)
 	{
 		long x = (long) (getOriginX(column) + prototype.getWidth());
 		long y = (long) (getOriginY(row) + prototype.getHeight());
 		return new RunsafeLocation(world, x - 0.5, groundLevel, y - 0.5, FACING_MIDDLE, LOOKING_FORWARD);
 	}
 
 	public RunsafeLocation getMinPosition(RunsafeWorld world, Rectangle2D area)
 	{
 		return new RunsafeLocation(world, area.getMinX(), 0, area.getMinY());
 	}
 
 	public RunsafeLocation getMaxPosition(RunsafeWorld world, Rectangle2D area)
 	{
 		return new RunsafeLocation(world, area.getMaxX(), world.getMaxHeight(), area.getMaxY());
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		prototype = new Rectangle2D.Double(
 			configuration.getConfigValueAsDouble("plot.origin.x"),
 			configuration.getConfigValueAsDouble("plot.origin.y"),
 			configuration.getConfigValueAsDouble("plot.w"),
 			configuration.getConfigValueAsDouble("plot.h")
 		);
 		fence = new Rectangle2D.Double(
 			configuration.getConfigValueAsDouble("fence.x"),
 			configuration.getConfigValueAsDouble("fence.y"),
 			configuration.getConfigValueAsDouble("fence.w"),
 			configuration.getConfigValueAsDouble("fence.h")
 		);
 		roadWidth = configuration.getConfigValueAsInt("plot.spacing");
 		groundLevel = configuration.getConfigValueAsInt("plot.groundLevel");
 		world = RunsafeServer.Instance.getWorld(configuration.getConfigValueAsString("world"));
 	}
 
 	public long getColumn(long blockX)
 	{
 		// Find the offset from the center of the origin plot along the X axis
 		double relativeX = blockX - prototype.getCenterX();
 		return Math.round(relativeX / (prototype.getWidth() + roadWidth));
 	}
 
 	public long getRow(long blockZ)
 	{
 		// Find the offset from the center of the origin plot along the Y axis
 		double relativeY = blockZ - prototype.getCenterY();
 		return Math.round(relativeY / (prototype.getHeight() + roadWidth));
 	}
 
 	public AbstractList<Long> getColumns()
 	{
 		return range(getColumn((long) fence.getMinX()), getColumn((long) fence.getMaxX()));
 	}
 
 	public AbstractList<Long> getRows()
 	{
 		return range(getRow((long) fence.getMinY()), getRow((long) fence.getMaxY()));
 	}
 
 	private long getOriginX(long column)
 	{
 		return (long) (prototype.getX() + column * (prototype.getWidth() + roadWidth));
 	}
 
 	private long getOriginY(long row)
 	{
 		return (long) (prototype.getY() + row * (prototype.getHeight() + roadWidth));
 	}
 
 	private static AbstractList<Long> range(final long begin, final long end)
 	{
 		return new AbstractList<Long>()
 		{
 			@Override
 			public Long get(int index)
 			{
 				return begin + index;
 			}
 
 			@Override
 			public int size()
 			{
 				return (int) (end - begin);
 			}
 		};
 	}
 
 	// Plots only exist within the world fence
 	private Rectangle2D fence;
 	private Rectangle2D prototype;
 	private int roadWidth;
 	private int groundLevel;
 	private RunsafeWorld world;
 	private static final float FACING_MIDDLE = 137.55f;
 	private static final float LOOKING_FORWARD = -1.65f;
 }
