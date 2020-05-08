 package de.pellepelster.myadmin.dsl.graphiti.ui.util.sizeandlocation;
 
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 
 public class SizeAndLocationHandler extends BaseSizeAndLocationHandler<SizeAndLocationHandler>
 {
 
 	private SizeAndLocation parentSizeAndLocation;
 
 	public SizeAndLocationHandler(SizeAndLocation parentSizeAndLocation)
 	{
 		this(parentSizeAndLocation, false);
 	}
 
 	public SizeAndLocationHandler(SizeAndLocation parentSizeAndLocation, boolean absolute)
 	{
 		setAbsolute(absolute);
 		this.parentSizeAndLocation = parentSizeAndLocation;
 	}
 
 	public static SizeAndLocationHandler createAbsolute(IAddContext context, int width, int height)
 	{
		return new SizeAndLocationHandler(new SizeAndLocation(context.getX(), context.getY(), width, height), true);
 	}
 
 	public static SizeAndLocationHandler create(SizeAndLocation sizeAndLocation)
 	{
 		return new SizeAndLocationHandler(sizeAndLocation);
 	}
 
 	public static SizeAndLocationHandler create(GraphicsAlgorithm graphicsAlgorithm)
 	{
 		return new SizeAndLocationHandler(new SizeAndLocation(graphicsAlgorithm.getWidth(), graphicsAlgorithm.getHeight()));
 	}
 
 	public static SizeAndLocationHandler create(ContainerShape containerShape)
 	{
 		return create(containerShape.getGraphicsAlgorithm());
 	}
 
 	@Override
 	protected SizeAndLocationHandler getQueryInstance()
 	{
 		return this;
 	}
 
 	public SizeAndLocationHandler updatePoints(Polyline headerLine)
 	{
 		return updatePoints(this.parentSizeAndLocation, headerLine);
 	}
 
 	public SizeAndLocation computeSizeAndLocation()
 	{
 		return computeSizeAndLocationInternal(this.parentSizeAndLocation);
 	}
 
 	public SizeAndLocationHandler setSizeAndLocation(GraphicsAlgorithm graphicsAlgorithm)
 	{
 		return setSizeAndLocation(this.parentSizeAndLocation, graphicsAlgorithm);
 	}
 
 	public int[] getPoints()
 	{
 		return getPoints(this.parentSizeAndLocation);
 	}
 
 }
