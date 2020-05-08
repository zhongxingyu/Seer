 package plugins.adufour.hierarchicalkmeans;
 
 import icy.image.IcyBufferedImage;
 import icy.image.colormap.FireColorMap;
 import icy.main.Icy;
 import icy.math.ArrayMath;
 import icy.roi.ROI2D;
 import icy.roi.ROI2DArea;
 import icy.sequence.Sequence;
 import icy.swimmingPool.SwimmingObject;
 import icy.type.DataType;
 import icy.type.collection.array.ArrayUtil;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.vecmath.Point3i;
 
 import plugins.adufour.connectedcomponents.ConnectedComponent;
 import plugins.adufour.connectedcomponents.ConnectedComponents;
 import plugins.adufour.connectedcomponents.ConnectedComponents.Sorting;
 import plugins.adufour.ezplug.EzException;
 import plugins.adufour.ezplug.EzGroup;
 import plugins.adufour.ezplug.EzLabel;
 import plugins.adufour.ezplug.EzPlug;
 import plugins.adufour.ezplug.EzVarBoolean;
 import plugins.adufour.ezplug.EzVarDouble;
 import plugins.adufour.ezplug.EzVarEnum;
 import plugins.adufour.ezplug.EzVarInteger;
 import plugins.adufour.ezplug.EzVarSequence;
 import plugins.adufour.filtering.Convolution1D;
 import plugins.adufour.filtering.ConvolutionException;
 import plugins.adufour.filtering.Kernels1D;
 import plugins.adufour.thresholder.KMeans;
 import plugins.adufour.thresholder.Thresholder;
 import plugins.adufour.vars.lang.VarArray;
 import plugins.adufour.vars.lang.VarSequence;
 import plugins.nchenouard.spot.DetectionResult;
 
 public class HierarchicalKMeans extends EzPlug
 {
 	protected static int					resultID			= 1;
 	
 	protected EzVarSequence					input				= new EzVarSequence("Input");
 	
 	protected EzVarDouble					preFilterValue		= new EzVarDouble("Gaussian pre-filter", 0, 50, 0.1);
 	
 	protected EzVarInteger					minSize				= new EzVarInteger("Min size (px)", 100, 1, 200000000, 1);
 	
 	protected EzVarInteger					maxSize				= new EzVarInteger("Max size (px)", 1600, 1, 200000000, 1);
 	
 	protected EzVarInteger					smartLabelClasses	= new EzVarInteger("Number of classes", 10, 2, 255, 1);
 	
 	protected EzVarBoolean					exportSequence		= new EzVarBoolean("Labeled sequence", false);
 	protected EzVarBoolean					exportSwPool		= new EzVarBoolean("Swimming pool data", false);
 	protected EzVarBoolean					exportROI			= new EzVarBoolean("ROIs", true);
 	
 	protected EzVarEnum<Sorting>			sorting				= new EzVarEnum<ConnectedComponents.Sorting>("Sorting", Sorting.values(), Sorting.DEPTH_ASC);
 	
 	protected EzLabel						nbObjects;
 	
 	protected VarSequence					outputSequence		= new VarSequence("binary sequence", null);
 	
 	protected VarArray<ConnectedComponent>	outputCCs			= new VarArray<ConnectedComponent>("objects", ConnectedComponent[].class, null);
 	
 	@Override
 	public void initialize()
 	{
 		super.addEzComponent(input);
 		super.addEzComponent(preFilterValue);
 		addEzComponent(new EzGroup("Object size", minSize, maxSize));
 		
 		addEzComponent(smartLabelClasses);
 		
 		addEzComponent(new EzGroup("Show result as...", exportSequence, sorting, exportSwPool, exportROI));
 		exportSequence.addVisibilityTriggerTo(sorting, true);
 		
 		addEzComponent(nbObjects = new EzLabel("< click run to start the detection >"));
 	}
 	
 	@Override
 	public void execute()
 	{
 		Sequence labeledSequence = new Sequence();
 		
 		Map<Integer, List<ConnectedComponent>> objects = null;
 		
 		try
 		{
 			objects = hierarchicalKMeans(input.getValue(), preFilterValue.getValue(), smartLabelClasses.getValue(), minSize.getValue(), maxSize.getValue(), labeledSequence);
 		}
 		catch (ConvolutionException e)
 		{
 			throw new EzException(e.getMessage(), true);
 		}
 		
 		// System.out.println("Hierarchical K-Means result:");
 		// System.out.println("T\tobjects");
 		int cpt = 0;
 		for (Integer t : objects.keySet())
 			cpt += objects.get(t).size();
 		// System.out.println("---");
 		
		nbObjects.setText(cpt + " objects detected");
 		
 		ArrayList<ConnectedComponent> ccList = new ArrayList<ConnectedComponent>();
 		int nbObjects = 0;
 		for (List<ConnectedComponent> ccs : objects.values())
 		{
 			nbObjects += ccs.size();
 			ccList.ensureCapacity(nbObjects);
 			ccList.addAll(ccs);
 		}
 		outputSequence.setValue(labeledSequence);
 		outputCCs.setValue(ccList.toArray(new ConnectedComponent[nbObjects]));
 		
 		if (exportSequence.getValue())
 		{
 			ConnectedComponents.createLabeledSequence(labeledSequence, objects, sorting.getValue().comparator);
 			labeledSequence.updateChannelsBounds(true);
 			labeledSequence.getColorModel().setColormap(0, new FireColorMap());
 			
 			addSequence(labeledSequence);
 		}
 		
 		if (exportSwPool.getValue())
 		{
 			DetectionResult result = ConnectedComponents.convertToDetectionResult(objects, input.getValue());
 			SwimmingObject object = new SwimmingObject(result, "Set of " + nbObjects + " connected components");
 			Icy.getMainInterface().getSwimmingPool().add(object);
 		}
 		
 		if (exportROI.getValue() && labeledSequence.getSizeZ() == 1)
 		{
 			Sequence in = input.getValue();
 			
 			in.beginUpdate();
 			
 			for (ROI2D roi : input.getValue().getROI2Ds())
 				if (roi instanceof ROI2DArea) in.removeROI(roi);
 			
 			for (List<ConnectedComponent> ccs : objects.values())
 				for (ConnectedComponent cc : ccs)
 				{
 					ROI2DArea area = new ROI2DArea();
 					for (Point3i pt : cc)
 						area.addPoint(pt.x, pt.y);
 					area.setT(cc.getT());
 					in.addROI(area);
 				}
 			
 			in.endUpdate();
 		}
 		
 	}
 	
 	/**
 	 * Performs a hierarchical K-Means segmentation on the input sequence, and returns all the
 	 * detected objects
 	 * 
 	 * @param seqIN
 	 *            the sequence to segment
 	 * @param preFilter
 	 *            the standard deviation of the Gaussian filter to apply before segmentation (0 for
 	 *            none)
 	 * @param nbKMeansClasses
 	 *            the number of classes to divide the histogram
 	 * @param minSize
 	 *            the minimum size in pixels of the objects to segment
 	 * @param maxSize
 	 *            the maximum size in pixels of the objects to segment
 	 * @param seqOUT
 	 *            an empty sequence that will receive the labeled output as unsigned short, or null
 	 *            if not necessary
 	 * @return a map containing the list of connected components found in each time point
 	 * @throws ConvolutionException
 	 *             if the filter size is too large w.r.t. the image size
 	 */
 	public static Map<Integer, List<ConnectedComponent>> hierarchicalKMeans(Sequence seqIN, double preFilter, int nbKMeansClasses, int minSize, int maxSize, Sequence seqOUT)
 			throws ConvolutionException
 	{
 		return hierarchicalKMeans(seqIN, preFilter, nbKMeansClasses, minSize, maxSize, null, seqOUT);
 	}
 	
 	/**
 	 * Performs a hierarchical K-Means segmentation on the input sequence, and returns all the
 	 * detected objects
 	 * 
 	 * @param seqIN
 	 *            the sequence to segment
 	 * @param preFilter
 	 *            the standard deviation of the Gaussian filter to apply before segmentation (0 for
 	 *            none)
 	 * @param nbKMeansClasses
 	 *            the number of classes to divide the histogram
 	 * @param minSize
 	 *            the minimum size in pixels of the objects to segment
 	 * @param maxSize
 	 *            the maximum size in pixels of the objects to segment
 	 * @param minValue
 	 *            the minimum intensity value each object should have (in any of the input channels)
 	 * @param seqOUT
 	 *            an empty sequence that will receive the labeled output as unsigned short, or null
 	 *            if not necessary
 	 * @return a map containing the list of connected components found in each time point
 	 * @throws ConvolutionException
 	 *             if the filter size is too large w.r.t the image size
 	 */
 	public static Map<Integer, List<ConnectedComponent>> hierarchicalKMeans(Sequence seqIN, double preFilter, int nbKMeansClasses, int minSize, int maxSize, Double minValue, Sequence seqOUT)
 			throws ConvolutionException
 	{
 		if (seqOUT == null) seqOUT = new Sequence();
 		
 		seqOUT.setName("HK-Means #" + resultID++);
 		
 		Sequence seqLABELS = new Sequence();
 		Sequence seqC = new Sequence();
 		seqC.setName("Current class");
 		
 		for (int z = 0; z < seqIN.getSizeZ(); z++)
 		{
 			seqC.setImage(0, z, new IcyBufferedImage(seqIN.getSizeX(), seqIN.getSizeY(), 1, DataType.UINT));
 			seqLABELS.setImage(0, z, new IcyBufferedImage(seqIN.getSizeX(), seqIN.getSizeY(), 1, DataType.UINT));
 		}
 		
 		seqOUT.beginUpdate();
 		
 		HashMap<Integer, List<ConnectedComponent>> componentsMap = new HashMap<Integer, List<ConnectedComponent>>();
 		
 		for (int t = 0; t < seqIN.getSizeT(); t++)
 		{
 			componentsMap.put(t, new ArrayList<ConnectedComponent>());
 			
 			// 1.1) Copy current image in a new sequence
 			
 			ArrayUtil.arrayToArray(seqIN.getDataXYZ(t, 0), seqLABELS.getDataXYZ(0, 0), seqIN.getDataType_().isSigned());
 			
 			// 1.2) Create the output labeled sequence
 			
 			for (int z = 0; z < seqIN.getSizeZ(); z++)
 			{
 				seqOUT.setImage(t, z, new IcyBufferedImage(seqIN.getSizeX(), seqIN.getSizeY(), 1, DataType.UINT));
 			}
 			
 			// 2) Pre-filter the input data
 			
 			double scaleXZ = seqIN.getPixelSizeX() / seqIN.getPixelSizeZ();
 			
 			Kernels1D gaussianXY = Kernels1D.CUSTOM_GAUSSIAN.createGaussianKernel1D(preFilter);
 			Kernels1D gaussianZ = Kernels1D.CUSTOM_GAUSSIAN.createGaussianKernel1D(preFilter * scaleXZ);
 			
 			Convolution1D.convolve(seqLABELS, gaussianXY.getData(), gaussianXY.getData(), seqIN.getSizeZ() > 1 ? gaussianZ.getData() : null);
 			
 			// 3) K-means on the raw data
 			
 			Thresholder.threshold(seqLABELS, 0, KMeans.computeKMeansThresholds(seqLABELS, 0, nbKMeansClasses, 255), true);
 			
 			// 4) Loop on each class in ascending order
 			
 			for (short c = 1; c < nbKMeansClasses; c++)
 			{
 				// retrieve classes c and above as a binary image
 				for (int z = 0; z < seqIN.getSizeZ(); z++)
 				{
 					int[] _labels = seqLABELS.getDataXYAsInt(0, z, 0);
 					int[] _class = seqC.getDataXYAsInt(0, z, 0);
 					int[] _out = seqOUT.getDataXYAsInt(t, z, 0);
 					
 					for (int i = 0; i < _labels.length; i++)
 						if ((_labels[i] & 0xffff) >= c && _out[i] == 0)
 						{
 							_class[i] = 1;
 						}
 				}
 				
 				// extract connected components on this current class
 				{
 					Sequence seqLabels = new Sequence();
 					List<ConnectedComponent> components = ConnectedComponents.extractConnectedComponents(seqC, minSize, maxSize, seqLabels).get(0);
 					seqC = seqLabels;
 					
 					// assign t value to all components
 					for (ConnectedComponent cc : components)
 						cc.setT(t);
 					
 					if (minValue == null)
 					{
 						componentsMap.get(t).addAll(components);
 					}
 					else
 					{
 						int[][] _class_z_xy = seqC.getDataXYZAsInt(0, 0);
 						
 						for (ConnectedComponent cc : components)
 						{
 							double[] maxIntensities = cc.computeMaxIntensity(seqIN);
 							if (ArrayMath.max(maxIntensities) < minValue)
 							{
 								for (Point3i pt : cc)
 								{
 									_class_z_xy[pt.z][pt.y * seqC.getSizeX() + pt.x] = 0;
 								}
 							}
 							else
 							{
 								componentsMap.get(t).add(cc);
 							}
 						}
 					}
 				}
 				
 				// store the final objects in the output image
 				for (int z = 0; z < seqIN.getSizeZ(); z++)
 				{
 					int[] _class = seqC.getDataXYAsInt(0, z, 0);
 					int[] _out = seqOUT.getDataXYAsInt(t, z, 0);
 					
 					for (int i = 0; i < _out.length; i++)
 					{
 						if (_class[i] != 0)
 						{
 							// store the valid pixel in the output
 							_out[i] = 1;
 							// erase the pixel from seqC for future classes
 							_class[i] = 0;
 						}
 					}
 				}
 			}
 			System.gc();
 		}
 		
 		seqOUT.endUpdate();
 		seqOUT.dataChanged();
 		return componentsMap;
 	}
 	
 	/**
 	 * Performs a hierarchical K-Means segmentation on the input sequence, and returns the result as
 	 * a labeled sequence
 	 * 
 	 * @param seqIN
 	 *            the sequence to segment
 	 * @param preFilter
 	 *            the standard deviation of the Gaussian filter to apply before segmentation (0 for
 	 *            none)
 	 * @param nbKMeansClasses
 	 *            the number of classes to divide the histogram
 	 * @param minSize
 	 *            the minimum size in pixels of the objects to segment
 	 * @param maxSize
 	 *            the maximum size in pixels of the objects to segment
 	 * @return a labeled sequence with all objects extracted in the different classes
 	 * @throws ConvolutionException
 	 *             if the filter size is too large w.r.t. the image size
 	 */
 	public static Sequence hierarchicalKMeans(Sequence seqIN, double preFilter, int nbKMeansClasses, int minSize, int maxSize) throws ConvolutionException
 	{
 		return hierarchicalKMeans(seqIN, preFilter, nbKMeansClasses, minSize, maxSize, (Double) null);
 	}
 	
 	/**
 	 * Performs a hierarchical K-Means segmentation on the input sequence, and returns the result as
 	 * a labeled sequence
 	 * 
 	 * @param seqIN
 	 *            the sequence to segment
 	 * @param preFilter
 	 *            the standard deviation of the Gaussian filter to apply before segmentation (0 for
 	 *            none)
 	 * @param nbKMeansClasses
 	 *            the number of classes to divide the histogram
 	 * @param minSize
 	 *            the minimum size in pixels of the objects to segment
 	 * @param maxSize
 	 *            the maximum size in pixels of the objects to segment
 	 * @param minValue
 	 *            the minimum intensity value each object should have (in any of the input channels)
 	 * @return a labeled sequence with all objects extracted in the different classes
 	 * @throws ConvolutionException
 	 *             if the filter size is too large w.r.t. the image size
 	 */
 	public static Sequence hierarchicalKMeans(Sequence seqIN, double preFilter, int nbKMeansClasses, int minSize, int maxSize, Double minValue) throws ConvolutionException
 	{
 		Sequence result = new Sequence();
 		
 		hierarchicalKMeans(seqIN, preFilter, nbKMeansClasses, minSize, maxSize, minValue, result);
 		
 		return result;
 	}
 	
 	public void clean()
 	{
 	}
 	
 }
