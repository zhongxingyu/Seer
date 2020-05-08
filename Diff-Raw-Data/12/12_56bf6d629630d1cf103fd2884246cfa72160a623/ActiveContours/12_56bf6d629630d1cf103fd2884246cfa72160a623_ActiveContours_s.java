 package plugins.adufour.activecontours;
 
 import icy.image.IcyBufferedImage;
 import icy.main.Icy;
 import icy.painter.Painter;
 import icy.roi.ROI2D;
 import icy.roi.ROI2DArea;
 import icy.sequence.Sequence;
 import icy.swimmingPool.SwimmingObject;
 import icy.system.thread.ThreadUtil;
 import icy.type.TypeUtil;
import icy.type.collection.array.ArrayUtil;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import javax.vecmath.Point3d;
 
 import plugins.adufour.activecontours.SlidingWindow.Operation;
 import plugins.adufour.ezplug.EzGroup;
 import plugins.adufour.ezplug.EzMessage;
 import plugins.adufour.ezplug.EzMessage.MessageType;
 import plugins.adufour.ezplug.EzMessage.OutputType;
 import plugins.adufour.ezplug.EzPlug;
 import plugins.adufour.ezplug.EzStoppable;
 import plugins.adufour.ezplug.EzVar;
 import plugins.adufour.ezplug.EzVarBoolean;
 import plugins.adufour.ezplug.EzVarDouble;
 import plugins.adufour.ezplug.EzVarEnum;
 import plugins.adufour.ezplug.EzVarInteger;
 import plugins.adufour.ezplug.EzVarListener;
 import plugins.adufour.ezplug.EzVarSequence;
 import plugins.adufour.filtering.Convolution1D;
 import plugins.adufour.filtering.Kernels1D;
 import plugins.fab.trackmanager.Detection;
 import plugins.fab.trackmanager.TrackGroup;
 import plugins.fab.trackmanager.TrackManager;
 import plugins.fab.trackmanager.TrackPool;
 import plugins.fab.trackmanager.TrackSegment;
 
 public class ActiveContours extends EzPlug implements EzStoppable
 {
 	public enum Initialization
 	{
 		ROI, ISOVALUE
 	}
 	
 	public final EzVarSequence				input					= new EzVarSequence("Input");
 	public final EzVarEnum<Initialization>	init_type				= new EzVarEnum<Initialization>("Initialize from", Initialization.values());
 	public final EzVarDouble				init_isovalue			= new EzVarDouble("Isovalue", 1, 0, 1000000, 0.01);
 	
 	public final EzGroup					regul					= new EzGroup("Regularization");
 	public final EzVarBoolean				regul_flag				= new EzVarBoolean("Regularization", true);
 	public final EzVarDouble				regul_weight			= new EzVarDouble("weight", 0.1, 0, 10.0, 0.1);
 	
 	public final EzGroup					edge					= new EzGroup("Gradient-based evolution");
 	public final EzVarBoolean				edge_flag				= new EzVarBoolean("Use gradient", true);
 	public final EzVarSequence				edge_input				= new EzVarSequence("source data");
 	public final EzVarInteger				edge_c					= new EzVarInteger("Input channel", 0, 0, 1);
 	public final EzVarInteger				edge_z					= new EzVarInteger("Input Z slice", 0, 0, 1);
 	public final EzVarDouble				edge_weight				= new EzVarDouble("weight", 0.5, -10.0, 10.0, 0.1);
 	
 	public final EzGroup					region					= new EzGroup("Region-based evolution");
 	public final EzVarBoolean				region_flag				= new EzVarBoolean("Use region", true);
 	public final EzVarSequence				region_input			= new EzVarSequence("source data");
 	public final EzVarInteger				region_c				= new EzVarInteger("Input channel", 0, 0, 1);
 	public final EzVarInteger				region_z				= new EzVarInteger("Input Z slice", 0, 0, 1);
 	public final EzVarDouble				region_weight			= new EzVarDouble("weight", 1, 0, 10.0, 0.1);
 	public final EzVarDouble				region_sensitivity		= new EzVarDouble("sensitivity", 1.0, 0.1, 10.0, 0.1);
 	public final EzVarBoolean				coupling_flag			= new EzVarBoolean("Multi-contour coupling", true);
 	
 	public final EzGroup					contour					= new EzGroup("Contour parameters");
 	public final EzVarDouble				contour_resolution		= new EzVarDouble("Contour Resolution", 1.0, 0.1, 1000.0, 0.1);
 	public final EzVarInteger				contour_minArea			= new EzVarInteger("Contour min. area", 10, 1, 100000000, 1);
 	
 	public final EzGroup					convergence				= new EzGroup("Convergence detection");
 	public final EzVarInteger				convergence_winSize		= new EzVarInteger("Window size", 50, 10, 10000, 10);
 	public final EzVarEnum<Operation>		convergence_operation	= new EzVarEnum<SlidingWindow.Operation>("Window operation", Operation.values(), Operation.VAR_COEFF);
 	public final EzVarDouble				convergence_criterion	= new EzVarDouble("Stability criterion", 0.001, 0, 0.1, 0.001);
 	
 	public final EzVarBoolean				tracking				= new EzVarBoolean("Tracking", false);
 	public final EzVarBoolean				updateMeans				= new EzVarBoolean("Update means", false);
 	
 	private IcyBufferedImage				edgeDataX, edgeDataY;
 	private IcyBufferedImage				region_data;
 	private IcyBufferedImage				region_local_mask;
 	private IcyBufferedImage				region_globl_mask;
 	private HashMap<ActiveContour, Double>	region_cin				= new HashMap<ActiveContour, Double>(0);
 	private double							region_cout;
 	private boolean							globalStop;
 	
 	private final TrackPool					trackPool				= new TrackPool();
 	private TrackGroup						trackGroup;
 	
 	public final Sequence					result					= new Sequence();
 	
 	private ActiveContoursPainter			painter;
 	
 	@Override
 	public void initialize()
 	{
 		addEzComponent(input);
 		
 		addEzComponent(init_type);
 		addEzComponent(init_isovalue);
 		init_type.addVisibilityTriggerTo(init_isovalue, Initialization.ISOVALUE);
 		
 		// regul
 		addEzComponent(regul_flag);
 		regul.addEzComponent(regul_weight);
 		addEzComponent(regul);
 		regul_flag.addVisibilityTriggerTo(regul, true);
 		
 		// edge
 		addEzComponent(edge_flag);
 		edge.addEzComponent(edge_input, edge_c, edge_z, edge_weight);
 		addEzComponent(edge);
 		edge_flag.addVisibilityTriggerTo(edge, true);
 		edge_input.addVarChangeListener(new EzVarListener<Sequence>()
 		{
 			@Override
 			public void variableChanged(EzVar<Sequence> source, Sequence newValue)
 			{
 				if (newValue != null)
 				{
 					int nC = newValue.getSizeC();
 					int nZ = newValue.getSizeZ();
 					
 					edge_c.setValue(0);
 					edge_c.setVisible(nC > 1);
 					edge_c.setMaxValue(nC - 1);
 					
 					edge_z.setValue(0);
 					edge_z.setVisible(nZ > 1);
 					edge_z.setMaxValue(nZ - 1);
 				}
 			}
 		});
 		
 		// region
 		addEzComponent(region_flag);
 		region.addEzComponent(region_input, region_c, region_z, region_weight, region_sensitivity);
 		addEzComponent(region);
 		region_flag.addVisibilityTriggerTo(region, true);
 		region_input.addVarChangeListener(new EzVarListener<Sequence>()
 		{
 			@Override
 			public void variableChanged(EzVar<Sequence> source, Sequence newValue)
 			{
 				if (newValue != null)
 				{
 					int nC = newValue.getSizeC();
 					int nZ = newValue.getSizeZ();
 					
 					region_c.setValue(0);
 					region_c.setVisible(nC > 1);
 					region_c.setMaxValue(nC - 1);
 					
 					region_z.setValue(0);
 					region_z.setVisible(nZ > 1);
 					region_z.setMaxValue(nZ - 1);
 				}
 			}
 		});
 		
 		// coupling
 		addEzComponent(coupling_flag);
 		
 		// contour
 		contour.addEzComponent(contour_resolution, contour_minArea);
 		addEzComponent(contour);
 		
 		// convergence
 		convergence.addEzComponent(convergence_winSize, convergence_operation, convergence_criterion);
 		addEzComponent(convergence);
 		
 		addEzComponent(tracking);
 	}
 	
 	@Override
 	public void execute()
 	{
 		globalStop = false;
 		
 		int startT = 0;
 		int endT = tracking.getValue() ? input.getValue().getSizeT() - 1 : startT;
 		
 		trackPool.clearTracks();
 		trackGroup = new TrackGroup(input.getValue());
 		trackPool.getTrackGroupList().add(trackGroup);
 		
 		ThreadUtil.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				Icy.getMainInterface().getSwimmingPool().add(new SwimmingObject(trackGroup));
 				TrackManager tm = new TrackManager();
 				tm.setDisplaySequence(input.getValue());
 			}
 		});
 		
 		if (getUI() != null)
 		{
 			// replace any ActiveContours Painter object on the sequence by ours
 			for (Painter painter : input.getValue().getPainters())
 				if (painter instanceof ActiveContoursPainter)
 					input.getValue().removePainter(painter);
 			
 			painter = new ActiveContoursPainter(trackGroup);
 			input.getValue().addPainter(painter);
 			
 			if (input.getValue().getFirstViewer() != null)
 			{
 				startT = input.getValue().getFirstViewer().getT();
 			}
 		}
 		
 		result.removeAllImage();
 		result.setName("Active Contours result");
 		
 		for (int t = startT; t <= endT; t++)
 		{
 			if (globalStop)
 				break;
 			if (getUI() != null && input.getValue().getFirstViewer() != null)
 				input.getValue().getFirstViewer().setT(t);
 			initContours(t, t == startT);
 			evolveContours(t);
 			storeResult(t);
 		}
 		if (getUI() != null)
 		{
 			addSequence(result);
 			SwimmingObject object = new SwimmingObject(trackGroup);
 			trackPool.addResult(object);
 		}
 	}
 	
 	private void initContours(int t, boolean isFirstImage)
 	{
 		// Initialize the image data
 		
 		if (edge_flag.getValue())
 		{
 			IcyBufferedImage inputImage = edge_input.getValue().getImage(t, region_z.getValue());
 			
 			if (inputImage.getSizeC() > 1)
 				inputImage = inputImage.extractChannel(region_c.getValue());
 			
 			Sequence gradient = Kernels1D.GRADIENT.toSequence();
 			Sequence gradX = new Sequence(inputImage.convertToType(TypeUtil.TYPE_DOUBLE, inputImage.isSignedDataType(), true));
 			Sequence gradY = gradX.getCopy();
 			Convolution1D.convolve(gradX, gradient, null, null);
 			Convolution1D.convolve(gradY, null, gradient, null);
 			
 			edgeDataX = gradX.getFirstImage();
 			edgeDataY = gradY.getFirstImage();
 		}
 		
 		if (region_flag.getValue())
 		{
 			IcyBufferedImage inputImage = region_input.getValue().getImage(t, region_z.getValue());
 			
 			if (inputImage.getSizeC() > 1)
 				inputImage = inputImage.extractChannel(region_c.getValue());
 			
 			region_data = inputImage.convertToType(TypeUtil.TYPE_DOUBLE, false, true);
 			region_local_mask = new IcyBufferedImage(region_data.getWidth(), region_data.getHeight(), 1, TypeUtil.TYPE_BYTE);
 			region_globl_mask = new IcyBufferedImage(region_data.getWidth(), region_data.getHeight(), 1, TypeUtil.TYPE_BYTE);
 			region_cin.clear();
 		}
 		
 		// Initialize the contours
 		
 		if (isFirstImage)
 		{
 			switch (init_type.getValue())
 			{
 				case ROI:
 					for (ROI2D roi : input.getValue().getROI2Ds())
 					{
 						try
 						{
 							final ActiveContour contour = new ActiveContour(this, contour_resolution, contour_minArea, new SlidingWindow(convergence_winSize.getValue()), roi);
 							contour.setX(roi.getBounds2D().getCenterX());
 							contour.setY(roi.getBounds2D().getCenterY());
 							contour.setT(t);
 							TrackSegment segment = new TrackSegment();
 							segment.addDetection(contour);
 							trackGroup.getTrackSegmentList().add(segment);
 						}
 						catch (TopologyException topo)
 						{
 							String message = "Warning: contour could not be triangulated. Possible reasons:\n";
 							message += " - binary mask is below the minimum contour area\n";
 							message += " - the binary mask contains a hole";
 							System.out.println(message);
 						}
 					}
 				break;
 				
 				case ISOVALUE:
 
 					ROI2DArea roi = new ROI2DArea();
 					IcyBufferedImage image = input.getValue().getFirstImage();
 					Object data = image.getDataXY(0);
 					
 					int off = 0;
 					for (int j = 0; j < image.getSizeY(); j++)
 						for (int i = 0; i < image.getSizeX(); i++, off++)
							if (ArrayUtil.getValue(data, off, false) >= init_isovalue.getValue())
 								roi.addPoint(i, j);
 					
 					try
 					{
 						final ActiveContour contour = new ActiveContour(this, contour_resolution, contour_minArea, new SlidingWindow(convergence_winSize.getValue()), roi);
 						contour.setX(roi.getBounds2D().getCenterX());
 						contour.setY(roi.getBounds2D().getCenterY());
 						contour.setT(t);
 						TrackSegment segment = new TrackSegment();
 						segment.addDetection(contour);
 						trackGroup.getTrackSegmentList().add(segment);
 					}
 					catch (TopologyException e1)
 					{
 						String message = "Warning: contour could not be triangulated. Possible reasons:\n";
 						message += " - binary mask is below the minimum contour area\n";
 						message += " - the binary mask contains a hole";
 						System.out.println(message);
 					}
 					
 				break;
 				
 				default:
 				{
 					String message = init_type.getValue().name() + " initialization is currently not supported";
 					if (getUI() != null)
 					{
 						EzMessage.message(message, MessageType.ERROR, OutputType.DIALOG);
 					}
 					else
 					{
 						System.out.println(message);
 					}
 					return;
 				}
 			}
 		}
 		else
 		{
 			for (TrackSegment segment : trackGroup.getTrackSegmentList())
 			{
 				Detection previous = segment.getDetectionAtTime(t - 1);
 				
 				if (previous == null)
 					continue;
 				
 				ActiveContour clone = new ActiveContour((ActiveContour) previous);
 				clone.setT(t);
 				segment.addDetection(clone);
 			}
 		}
 	}
 	
 	private void evolveContours(int t)
 	{
 		ArrayList<ActiveContour> currentContours = new ArrayList<ActiveContour>(trackGroup.getTrackSegmentList().size());
 		
 		for (TrackSegment segment : trackGroup.getTrackSegmentList())
 		{
 			Detection det = segment.getDetectionAtTime(t);
 			if (det != null)
 				currentContours.add((ActiveContour) det);
 		}
 		
 		if (currentContours.size() == 0)
 			return;
 		
		long cpt = 0;
 		
 		int nbConvergedContours = 0;
 		
 		while (!globalStop && nbConvergedContours < currentContours.size())
 		{
 			nbConvergedContours = 0;
 			
 			// update image data if necessary
 			
 			if (region_flag.getValue())
 				region_updateMeans(t);
 			
 			for (int ct = 0; ct < currentContours.size(); ct++)
 			{
 				ActiveContour currentContour = currentContours.get(ct);
 				
 				Double criterion = currentContour.convergence.computeCriterion(convergence_operation.getValue());
 				
 				if (criterion != null && criterion < convergence_criterion.getValue())
 				{
 					nbConvergedContours++;
 					if (getUI() != null)
 						getUI().setProgressBarValue((double) nbConvergedContours / currentContours.size());
 					continue;
 				}
 				
 				try
 				{
 					currentContour.reSample(0.7, 1.4);
 					
 					if (edge_flag.getValue())
 						currentContour.updateEdgeForces(edgeDataX, edgeDataY, edge_weight.getValue());
 					
 					if (regul_flag.getValue())
 						currentContour.updateInternalForces(regul_weight.getValue());
 					
 					if (region_flag.getValue())
 						currentContour.updateRegionForces(region_data, region_weight.getValue(), region_cin.get(currentContour), region_cout, region_sensitivity.getValue());
 					
 					if (coupling_flag.getValue())
 						for (ActiveContour otherContour : currentContours)
 							currentContour.updateFeedbackForces(otherContour);
 					
 					currentContour.move();
 				}
 				catch (TopologyException e)
 				{
 					ActiveContour removedContour = currentContours.remove(ct--);
 					
 					TrackSegment motherSegment = null;
 					
 					for (TrackSegment segment : trackGroup.getTrackSegmentList())
 					{
 						if (segment.containsDetection(removedContour))
 						{
 							motherSegment = segment;
 							segment.removeDetection(removedContour);
 							break;
 						}
 					}
 					
 					for (ActiveContour child : e.children)
 					{
 						currentContours.add(child);
 						TrackSegment childSegment = new TrackSegment();
 						childSegment.addDetection(child);
 						trackGroup.addTrackSegment(childSegment);
 						
 						if (motherSegment != null && motherSegment.getDetectionList().size() > 0)
 							trackPool.createLink(motherSegment, childSegment);
 					}
 					
 					if (region_flag.getValue())
 						region_updateMeans(t);
 				}
 				catch (NullPointerException npe)
 				{
 					npe.printStackTrace();
 					System.out.println(currentContour.points.size());
 					
 					ActiveContour removedContour = currentContours.remove(ct--);
 					
 					for (TrackSegment segment : trackGroup.getTrackSegmentList())
 					{
 						if (segment.containsDetection(removedContour))
 						{
 							segment.removeDetection(removedContour);
 							break;
 						}
 					}
 					
 					if (region_flag.getValue())
 						region_updateMeans(t);
 				}
 				
 				if (input.getValue() != null)
 					input.getValue().painterChanged(painter);
 			}
 			
			cpt++;
 		}
 	}
 	
 	private void storeResult(int t)
 	{
 		BufferedImage resultImage = new BufferedImage(input.getValue().getWidth(), input.getValue().getHeight(), BufferedImage.TYPE_INT_ARGB);
 		
 		Graphics2D g = (Graphics2D) resultImage.getGraphics();
 		g.clearRect(0, 0, input.getValue().getWidth(), input.getValue().getHeight());
 		
 		// ArrayList<ActiveContour> currentContours = contoursMap.get(t);
 		ArrayList<ActiveContour> currentContours = new ArrayList<ActiveContour>(trackGroup.getTrackSegmentList().size());
 		for (TrackSegment segment : trackGroup.getTrackSegmentList())
 		{
 			Detection det = segment.getDetectionAtTime(t);
 			if (det != null)
 				currentContours.add((ActiveContour) det);
 		}
 		
 		for (ActiveContour contour : currentContours)
 		{
 			// store the detection result
 			// contours.add(contour);
 			
 			// set the detection parameters
 			Point3d center = new Point3d();
 			for (Point3d p : contour.points)
 				center.add(p);
 			center.scale(1.0 / contour.points.size());
 			contour.setX(center.x);
 			contour.setY(center.y);
 			
 			// draw in the sequence
 			g.setColor(contour.getColor());
 			g.fill(contour.poly);
 		}
 		g.dispose();
 		
 		result.setImage(t, 0, resultImage);
 		result.updateComponentsBounds(true, true);
 	}
 	
 	private void region_updateMeans(int t)
 	{
 		double[] _region_data = region_data.getDataXYAsDouble(0);
 		
 		byte[] _globlMask = region_globl_mask.getDataXYAsByte(0);
 		Arrays.fill(_globlMask, (byte) 0);
 		Graphics2D g_globl_mask = (Graphics2D) region_globl_mask.getGraphics();
 		
 		byte[] _localMask = region_local_mask.getDataXYAsByte(0);
 		Graphics2D g_local_mask = (Graphics2D) region_local_mask.getGraphics();
 		
 		// ArrayList<ActiveContour> currentContours = contoursMap.get(t);
 		ArrayList<ActiveContour> currentContours = new ArrayList<ActiveContour>(trackGroup.getTrackSegmentList().size());
 		for (TrackSegment segment : trackGroup.getTrackSegmentList())
 		{
 			Detection det = segment.getDetectionAtTime(t);
 			if (det != null)
 				currentContours.add((ActiveContour) det);
 		}
 		
 		for (ActiveContour contour : currentContours)
 		{
 			double inSum = 0, inCpt = 0;
 			
 			// create a mask for each object for interior mean measuring
 			Arrays.fill(_localMask, (byte) 0);
 			g_local_mask.fill(contour.poly);
 			
 			for (int i = 0; i < _localMask.length; i++)
 			{
 				if (_localMask[i] != 0)
 				{
 					inSum += _region_data[i];
 					inCpt++;
 				}
 			}
 			
 			region_cin.put(contour, inSum / inCpt);
 			
 			// add the contour to the global mask for background mean measuring
 			g_globl_mask.fill(contour.poly);
 		}
 		
 		double outSum = 0, outCpt = 0;
 		for (int i = 0; i < _globlMask.length; i++)
 		{
 			if (_globlMask[i] == 0)
 			{
 				outSum += _region_data[i];
 				outCpt++;
 			}
 		}
 		region_cout = outSum / outCpt;
 	}
 	
 	@Override
 	public void clean()
 	{
 		if (input.getValue() != null)
 			input.getValue().removePainter(painter);
 		
 		// contoursMap.clear();
 		// contours.clear();
 		// trackGroup.clearTracks();
 		if (region_flag.getValue())
 			region_cin.clear();
 	}
 	
 	@Override
 	public void stopExecution()
 	{
 		globalStop = true;
 	}
 	
 }
