 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.dawnsci.plotting.jreality.impl;
 
 import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
 import static de.jreality.shader.CommonAttributes.TEXTURE_2D_1;
 import static de.jreality.shader.CommonAttributes.TEXTURE_2D_2;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.dawnsci.plotting.jreality.core.AxisMode;
 import org.dawnsci.plotting.jreality.core.IDataSet3DCorePlot;
 import org.dawnsci.plotting.jreality.core.ScaleType;
 import org.dawnsci.plotting.jreality.data.ColourImageData;
 import org.dawnsci.plotting.jreality.overlay.Overlay2DConsumer;
 import org.dawnsci.plotting.jreality.overlay.Overlay2DProvider2;
 import org.dawnsci.plotting.jreality.overlay.OverlayImage;
 import org.dawnsci.plotting.jreality.overlay.OverlayType;
 import org.dawnsci.plotting.jreality.overlay.VectorOverlayStyles;
 import org.dawnsci.plotting.jreality.overlay.enums.LabelOrientation;
 import org.dawnsci.plotting.jreality.overlay.objects.ArrowObject;
 import org.dawnsci.plotting.jreality.overlay.objects.BoxObject;
 import org.dawnsci.plotting.jreality.overlay.objects.CircleObject;
 import org.dawnsci.plotting.jreality.overlay.objects.CircleSectorObject;
 import org.dawnsci.plotting.jreality.overlay.objects.EllipseObject;
 import org.dawnsci.plotting.jreality.overlay.objects.ImageObject;
 import org.dawnsci.plotting.jreality.overlay.objects.LineObject;
 import org.dawnsci.plotting.jreality.overlay.objects.OverlayObject;
 import org.dawnsci.plotting.jreality.overlay.objects.PointListObject;
 import org.dawnsci.plotting.jreality.overlay.objects.PointObject;
 import org.dawnsci.plotting.jreality.overlay.objects.TextLabelObject;
 import org.dawnsci.plotting.jreality.overlay.objects.TriangleObject;
 import org.dawnsci.plotting.jreality.overlay.primitives.ArrowPrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.BoxPrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.CirclePrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.CircleSectorPrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.EllipsePrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.LabelPrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.LinePrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.OverlayPrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.PointListPrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.PointPrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.PrimitiveType;
 import org.dawnsci.plotting.jreality.overlay.primitives.RingPrimitive;
 import org.dawnsci.plotting.jreality.overlay.primitives.TrianglePrimitive;
 import org.dawnsci.plotting.jreality.tick.Tick;
 import org.dawnsci.plotting.jreality.tick.TickFactory;
 import org.dawnsci.plotting.jreality.tick.TickFormatting;
 import org.dawnsci.plotting.jreality.tool.ImagePositionTool;
 import org.dawnsci.plotting.jreality.tool.PanningTool;
 import org.dawnsci.plotting.jreality.tool.PlotActionEvent;
 import org.dawnsci.plotting.jreality.tool.PlotActionEventListener;
 import org.dawnsci.plotting.jreality.tool.PlotActionTool2D;
 import org.dawnsci.plotting.jreality.util.ArrayPoolUtility;
 import org.dawnsci.plotting.jreality.util.JOGLChecker;
 import org.dawnsci.plotting.jreality.util.JOGLGLSLShaderGenerator;
 import org.dawnsci.plotting.jreality.util.ScalingUtility;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractCompoundDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.CompoundByteDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.CompoundIntegerDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.CompoundLongDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.CompoundShortDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.RGBDataset;
 import de.jreality.geometry.IndexedLineSetFactory;
 import de.jreality.geometry.PointSetFactory;
 import de.jreality.geometry.QuadMeshFactory;
 import de.jreality.math.MatrixBuilder;
 import de.jreality.scene.Appearance;
 import de.jreality.scene.Camera;
 import de.jreality.scene.ClippingPlane;
 import de.jreality.scene.IndexedFaceSet;
 import de.jreality.scene.IndexedLineSet;
 import de.jreality.scene.PointSet;
 import de.jreality.scene.SceneGraphComponent;
 import de.jreality.scene.data.AttributeEntityUtility;
 import de.jreality.scene.event.TransformationEvent;
 import de.jreality.scene.event.TransformationListener;
 import de.jreality.shader.CommonAttributes;
 import de.jreality.shader.DefaultGeometryShader;
 import de.jreality.shader.DefaultLineShader;
 import de.jreality.shader.DefaultPointShader;
 import de.jreality.shader.DefaultPolygonShader;
 import de.jreality.shader.DefaultTextShader;
 import de.jreality.shader.GlslProgram;
 import de.jreality.shader.ShaderUtility;
 import de.jreality.shader.Texture2D;
 import de.jreality.shader.TextureUtility;
 import de.jreality.ui.viewerapp.AbstractViewerApp;
 import de.jreality.util.CameraUtility;
 import de.jreality.util.SceneGraphUtility;
 
 /**
  *
  */
 public class DataSet3DPlot2D implements IDataSet3DCorePlot, 
 										Overlay2DProvider2, 
 										TransformationListener,
 										PlotActionEventListener {
 
 	/**
 	 * Prefix for the overlay primitive SceneNode name
 	 */
 
 	protected boolean hasJOGLshaders;
 	protected int maxWidth = 0;
 	protected int maxHeight = 0;
 	protected int numGraphs = 0;
 	protected double tableMin = 0;
 	protected double tableMax = 0;
 
 	protected SceneGraphComponent graphGroupNode = null;
 	protected SceneGraphComponent overlayPrimitiveGroupNode = null;
 	protected SceneGraphComponent graph = null;
 	protected List<SceneGraphComponent> subGraphs =
 		Collections.synchronizedList(new LinkedList<SceneGraphComponent>());
 	protected List<Appearance> graphApps = 
 		Collections.synchronizedList(new LinkedList<Appearance>());
 
 	protected Appearance graphAppearance;
 	protected GlslProgram tableProg = null;
 	protected de.jreality.shader.ImageData lookupTableImg = null;
 	protected ImagePositionTool posTool;
 	protected PanningTool panTool;
 	protected SceneGraphComponent xTickNode = null;
 	protected SceneGraphComponent yTickNode = null;
 	protected SceneGraphComponent xLabelNode = null;
 	protected SceneGraphComponent yLabelNode = null;
 	protected SceneGraphComponent titleLabel = null;
 	protected SceneGraphComponent axis = null;
 	
 	private static final Logger logger = LoggerFactory
 	.getLogger(DataSet3DPlot2D.class);
 	
 	private AbstractViewerApp app;
 	private SceneGraphComponent xAxisLabel = null;
 	private SceneGraphComponent yAxisLabel = null;
 	private DefaultTextShader dtsXAxisLabel;
 	private DefaultTextShader dtsYAxisLabel;
 	private DefaultTextShader dtsTitleLabel;
 	private DefaultTextShader dtsXTicks;
 	private DefaultTextShader dtsYTicks;	
 	private SceneGraphComponent ticksNode = null;
 	private SceneGraphComponent leftClip = null;
 	private SceneGraphComponent rightClip = null;
 	private SceneGraphComponent topClip = null;
 	private SceneGraphComponent bottomClip = null;
 	private SceneGraphComponent background = null;
 	private String titleLabelStr = null;
 	
 	private Map<Integer,OverlayPrimitive> prim2DMap = 
 		Collections.synchronizedMap(new HashMap<Integer,OverlayPrimitive>(1000));
 	private List<PlotActionEventListener> actionListeners = Collections
 	.synchronizedList(new LinkedList<PlotActionEventListener>());	
 	String xAxisLabelStr = null;
 	String yAxisLabelStr = null;
 	private double globalRealXmin = 0;
 	private double globalRealXmax = 1;
 	private double globalRealYmin = 0;
 	private double globalRealYmax = 1;
 	private double threshold = 65535;
 	private double xOffset = 0;
 	private double yOffset = 0;
 	private boolean hasJOGL;
 	private boolean useLogarithmic = false;
 	private boolean useCanvasAspect = false;
 	private boolean useDiffractionMode = false;
 	private boolean useGradientMode = false;
 	private TickFactory tickFactory = null;	
 	private TickFormatting xLabelMode = TickFormatting.plainMode;
 	private TickFormatting yLabelMode = TickFormatting.plainMode;
  	private AxisMode xAxis = AxisMode.LINEAR;
 	private AxisMode yAxis = AxisMode.LINEAR;
 	private AxisValues xAxisValues;
 	private AxisValues yAxisValues;
 	private PlotActionTool2D actionTool;
 
 	private Composite plotArea;
 	private Cursor defaultCursor;
 	
 	private boolean overlayInOperation = false;
 	private int primKeyID = 0;
 	private int canvasWidth = 1;
 	private int canvasHeight = 1;
 	private byte[] softwareImageRGBAdata = null;
 	private Overlay2DConsumer consumer;
 	private double inverseScaling = 1.0;
 	private double labelScaling = 1.0;
 	private static double MINX = MAXX * 0.4;
 	private Map<Appearance,Texture2D> overlayTextures;
 	private Map<Appearance, float[]> imageDatas;
 	private Map<Appearance, Texture2D> textures;
 	private Map<Appearance, byte[]> softwareImageRGBAdatas;
 	private OverlayImage overlayImage;
     private ScaleType currentScale = ScaleType.LINEAR;
     private int lastImageType = -1;
     private int currentImageType = -1;
     // GLSL specific stuff 
 
 /*	
 	private static final String GRADIENTIMGGPROG = 
 		"uniform sampler2D sampler;\n"+
 	    "uniform sampler2D tableSampler;\n"+
 	    "uniform sampler2D overlaySampler;\n"+
 	    "uniform float maxValue;\n"+
 	    "uniform float minValue;\n"+
 	    "uniform float threshold;\n"+
 	    "uniform vec2 delta;\n"+
 	    "void main(void)\n"+
 	    "{\n"+
 	    " float k = 0.01;\n"+
 	    " float temp_X_Y = texture2D(sampler,gl_TexCoord[0].st - delta).x;\n"+
 	    " float temp_Y = texture2D(sampler,gl_TexCoord[0].st + vec2(0,-delta.y)).x;\n"+
 	    " float tempX_Y = texture2D(sampler,gl_TexCoord[0].st + vec2(delta.x,-delta.y)).x;\n"+
 	    " float temp_X = texture2D(sampler,gl_TexCoord[0].st + vec2(-delta.x,0.0)).x;\n"+
 	    " float tempX = texture2D(sampler,gl_TexCoord[0].st + vec2(delta.x,0.0)).x;\n"+
 	    " float temp_XY = texture2D(sampler,gl_TexCoord[0].st + vec2(-delta.x,delta.y)).x;\n"+
 	    " float tempY = texture2D(sampler,gl_TexCoord[0].st + vec2(0.0,delta.y)).x;\n"+
 	    " float tempXY = texture2D(sampler,gl_TexCoord[0].st + delta).x;\n"+
 	    " float gy = tempY + temp_XY + tempXY - temp_Y - tempX_Y - temp_X_Y;\n"+
 	    " float gx = tempX + tempX_Y + temp_XY - temp_X - temp_X_Y - tempX_Y;\n"+
 	    " float gz = gx * gy;\n"+
 	    " gx*=gx;\n"+
 	    " gy*=gy;\n"+
 	    " float M = (gx * gy - gz * gz)-(k * (gx+gy)*(gx+gy));\n"+
     	" float dataValue = texture2D(sampler,gl_TexCoord[0].st).x;\n"+
     	" float nDataValue = min(1.0,(dataValue - minValue) / (maxValue-minValue));\n"+
 	    " vec4 image = texture2D(tableSampler,vec2(nDataValue,nDataValue));\n"+
 	    " if (abs(M) > threshold)\n"+
 	    "     image = vec4(1.0,0.0,0.85,1.0);\n"+
 	    " vec4 overlay = texture2D(overlaySampler,gl_TexCoord[0].st);\n"+
 	    " gl_FragColor = image;\n"+
 		"}\n"; 
 	*/
 	
 	/**
 	 * Constructor for a DataSet3DPlot2D object
 	 * @param app parent ViewerApp where the DataSet3DPlot2D is contained
 	 * @param plotArea Composite that contains the plotArea
 	 * @param defaultCursor default cursor used in the plotArea
 	 * @param tool Panning tool
 	 * @param useJOGL are we using JOGL?
 	 */
 	public DataSet3DPlot2D(AbstractViewerApp app,
 			   			   Composite plotArea,
 			   			   Cursor defaultCursor,			
 						   PanningTool tool,
 						   boolean useJOGL,
 						   boolean useJOGLshaders)
 	{
 		this.app = app;
 		this.panTool = tool;
 		this.plotArea = plotArea;
 		this.defaultCursor = defaultCursor;
 		hasJOGL = useJOGL;
 		hasJOGLshaders = useJOGLshaders;
 		if (hasJOGLshaders)
 		{
 			byte[] colourTable = new byte[256 * 4];
 			for (int x = 0; x < 256; x++)
 			{
 				colourTable[x*4] = (byte)(x >> 2);
 			    colourTable[x*4+1] = (byte)(255-x);
 			    colourTable[x*4+2] = (byte)(x >> 3);
 			    colourTable[x*4+3] = ~0;
 			}
 			lookupTableImg = new de.jreality.shader.ImageData(colourTable,256,1);
 		}
 		textures = new HashMap<Appearance, Texture2D>();
 		imageDatas = new HashMap<Appearance,float[]>();
 		overlayTextures = new HashMap<Appearance, Texture2D>();
 		softwareImageRGBAdatas = new HashMap<Appearance,byte[]>();
 		tickFactory = new TickFactory(xLabelMode);
 		posTool = new ImagePositionTool(maxWidth,maxHeight,MAXX,MAXY);
 		actionTool = new PlotActionTool2D();
 		actionTool.addPlotActionEventListener(this);
 	}
 	
 	@Override
 	public SceneGraphComponent buildBoundingBox() {
 		// Nothing to do
 		return null;
 	}
 
 	private void refresh()
 	{
 		try {
 		    app.getCurrentViewer().render();
 		} catch (SWTException itsDisposed) {
 			// We do nothing as sometimes the viewer can
 			// be disposed and since we cannot test for this
 			// currently we ignore the error and
 			// leave the part to exit, which is probably
 			// what it is doing at this point.
 		}
 	}
 	
 
 	protected double[] determineXYsize()
 	{
 		double[] sizes = new double[2];
 		if (!useCanvasAspect) { 
 			if (maxWidth >= maxHeight)
 			{
 				sizes[0] = MAXX;
 				sizes[1] = (MAXX * maxHeight)/maxWidth;
 			} else {
 				sizes[1] = MAXX;
 				sizes[0] = (MAXX * maxWidth)/maxHeight;
 			}
 			sizes[0] = Math.max(sizes[0], 3.0);
 			sizes[1] = Math.max(sizes[1], 3.0);
 		} else {
 			if (canvasWidth >= canvasHeight)
 			{
 				sizes[0] = MAXX;
 				sizes[1] = (MAXX * canvasHeight)/canvasWidth;
 			} else {
 				sizes[1] = MAXX;
 				sizes[0] = (MAXX * canvasWidth)/canvasHeight;
 			}
 		}
 		return sizes;
 	}
 	
 	protected IndexedLineSet createXTicksGeometry()
 	{
 		IndexedLineSetFactory factory  = new IndexedLineSetFactory();
 		double min = globalRealXmin;
 		double max = globalRealXmax;
 		tickFactory.setTickMode(xLabelMode);
 		int width = app.getCurrentViewer().getViewingComponentSize().width;
 	    // protect against rubbish RCP early lazy initialisation
 		
 		if (width == 0)
 	    	width = FONT_SIZE_PIXELS_WIDTH;
 		LinkedList<Tick> ticks = 
 			tickFactory.generateTicks(width, 
 					min, max, (short)0,false);
 			
 		factory.setVertexCount(ticks.size()*2);
 		factory.setEdgeCount(ticks.size());
 		double[][] coords = ArrayPoolUtility.getDoubleArray(ticks.size()*2);
 		double[] sizes = determineXYsize();
 		
 		int[][] edges = ArrayPoolUtility.getIntArray(ticks.size());
 		for (int i = 0; i < ticks.size(); i++)
 		{
 			double value = tickFactory.getTickUnit() * i;
 			coords[i*2][0] = (value/(globalRealXmax-min)) * sizes[0];
 			coords[i*2][1] = -0.125;
 			coords[i*2][2] = 0.0;
 			coords[i*2+1][0] = (value/(globalRealXmax-min)) * sizes[0];
 			coords[i*2+1][1] = 0.0;
 			coords[i*2+1][2] = 0.0;
 			edges[i][0] = i*2;
 			edges[i][1] = i*2+1;			
 		}
 		factory.setVertexCoordinates(coords);
 		factory.setEdgeIndices(edges);	
 		factory.update();
 		return factory.getIndexedLineSet();
 	}
 	
 	
 	protected IndexedLineSet createYTicksGeometry()
 	{
 		IndexedLineSetFactory factory = new IndexedLineSetFactory();
 		tickFactory.setTickMode(yLabelMode);
 		int height = app.getCurrentViewer().getViewingComponentSize().height;
 		if (height == 0)
 			height = FONT_SIZE_PIXELS_HEIGHT;
 		LinkedList<Tick> ticks =
 			tickFactory.generateTicks(height, globalRealYmin, globalRealYmax, (short)1,false);
 		factory.setVertexCount(ticks.size()*2);
 		factory.setEdgeCount(ticks.size());     
 		double[] sizes = determineXYsize();
 		double[][] coords = ArrayPoolUtility.getDoubleArray(ticks.size()*2);
 		int[][] edges = ArrayPoolUtility.getIntArray(ticks.size());
 		double range = globalRealYmax - globalRealYmin;		
 		double step = (sizes[1] * ((tickFactory.getTickUnit() * ticks.size())/range))/ticks.size();	
 		for (int i = 0; i < ticks.size(); i++)
 		{
 			coords[i*2][0] = -0.125;
 			coords[i*2][1] = sizes[1] - i * step;
 			coords[i*2][2] = 0.0;
 			coords[i*2+1][0] = 0.0;
 			coords[i*2+1][1] = sizes[1] - i * step;
 			coords[i*2+1][2] = 0.0;
 			edges[i][0] = i*2;
 			edges[i][1] = i*2+1;
 		}	
 		factory.setVertexCoordinates(coords);
 		factory.setEdgeIndices(edges);
 		factory.update();
 	//	if (yLabels != null)
 	//		yLabels.setGeometry(createYLabelsGeometry(ticks,step));			
 		return factory.getIndexedLineSet();	
 	}	
 	@Override
 	public SceneGraphComponent buildCoordAxesTicks() {
 		if (ticksNode == null)
 		{
 			ticksNode = SceneGraphUtility.createFullSceneGraphComponent("ticks");
 			xTickNode = SceneGraphUtility.createFullSceneGraphComponent("xTicks");
 			yTickNode = SceneGraphUtility.createFullSceneGraphComponent("yTicks");
 			ticksNode.addChild(xTickNode);
 			ticksNode.addChild(yTickNode);
 			Appearance tickAppearance = new Appearance();
 			xTickNode.setAppearance(tickAppearance);
 			yTickNode.setAppearance(tickAppearance);
 			DefaultGeometryShader dgs = 
 				ShaderUtility.createDefaultGeometryShader(tickAppearance, true);
 			tickAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 			DefaultLineShader dls = 
 			         (DefaultLineShader)dgs.createLineShader("default");
 
 			dls.setLineWidth(1.0);
 			dls.setDiffuseColor(java.awt.Color.black);
 			dgs.setShowFaces(false);
 			dgs.setShowLines(true);
 			dgs.setShowPoints(false);	
 			xTickNode.setGeometry(createXTicksGeometry());
 			yTickNode.setGeometry(createYTicksGeometry());
 		}
 		return ticksNode;
 	}
 
 	private IndexedFaceSet createBackground() {
 		QuadMeshFactory factory = new QuadMeshFactory();
 		double[][][] coords = new double[2][2][3];
 		coords[0][0][0] = -5.0;
 		coords[0][0][1] = MAXY+5.0;
 		coords[0][0][2] = 0.0;
 		coords[0][1][0] = MAXX+5.0;
 		coords[0][1][1] = MAXY+5.0;
 		coords[0][1][2] = 0.0;
 		coords[1][0][0] = -5.0;
 		coords[1][0][1] = -5.0;
 		coords[1][0][2] = 0.0;
 		coords[1][1][0] = MAXX+5.0;
 		coords[1][1][1] = -5.0;
 		coords[1][1][2] = 0.0;
 		factory.setVLineCount(2); // important: the v-direction is the left-most
 									// index
 		factory.setULineCount(2); // and the u-direction the next-left-most
 									// index
 		factory.setClosedInUDirection(false);
 		factory.setClosedInVDirection(false);
 		factory.setVertexCoordinates(coords);
 		factory.setGenerateFaceNormals(true);
 		factory.update();
 		coords = null;
 		return factory.getIndexedFaceSet();
 	}
 	
 	private void buildBackground(SceneGraphComponent graph) {
 		background = SceneGraphUtility
 				.createFullSceneGraphComponent(BACKGROUNDNODENAME);
 		graph.addChild(background);
 		background.setGeometry(createBackground());
 		Appearance backAppearance = new Appearance();
 		background.setAppearance(backAppearance);
 		backAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 		backAppearance
 				.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
 		backAppearance.setAttribute(CommonAttributes.ADDITIVE_BLENDING_ENABLED,
 				false);
 		backAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "."
 				+ CommonAttributes.TRANSPARENCY, 1.0);
 		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(
 				backAppearance, true);
 		dgs.setShowFaces(true);
 		dgs.setShowLines(false);
 		dgs.setShowPoints(false);
 	}
 	
 	protected IndexedLineSet createAxisGeometry(double xSize, double ySize)
 	{
 		IndexedLineSetFactory factory  = new IndexedLineSetFactory();
 		factory.setVertexCount(4);
 		factory.setEdgeCount(2);
 		double[][] axisCoords = ArrayPoolUtility.getDoubleArray(4);
 		axisCoords[0][0] = 0;
 		axisCoords[0][1] = 0;
 		axisCoords[0][2] = 0;
 		axisCoords[1][0] = xSize;
 		axisCoords[1][1] = 0;
 		axisCoords[1][2] = 0;
 		axisCoords[2][0] = 0;
 		axisCoords[2][1] = 0;
 		axisCoords[2][2] = 0;
 		axisCoords[3][0] = 0;
 		axisCoords[3][1] = ySize;
 		axisCoords[3][2] = 0;
 		int[][] axisEdges = ArrayPoolUtility.getIntArray(2);
 		axisEdges[0][0] = 0;
 		axisEdges[0][1] = 1;
 		axisEdges[1][0] = 2;
 		axisEdges[1][1] = 3;
 		factory.setVertexCoordinates(axisCoords);
 		factory.setEdgeIndices(axisEdges);
 		factory.update();
 		return factory.getIndexedLineSet();				
 	}
 	
 	@Override
 	public SceneGraphComponent buildCoordAxis(SceneGraphComponent axis) {
 		this.axis = axis;
 		xAxisLabel = SceneGraphUtility.createFullSceneGraphComponent("xAxisLabel");
         yAxisLabel = SceneGraphUtility.createFullSceneGraphComponent("yAxisLabel");
         titleLabel = SceneGraphUtility.createFullSceneGraphComponent("titleLabel");
 		axis.addChild(xAxisLabel);
 		axis.addChild(yAxisLabel);
 		axis.addChild(titleLabel);
 		Appearance graphAppearance = new Appearance();
 		axis.setAppearance(graphAppearance);
 		double[] sizes = determineXYsize();
 		axis.setGeometry(createAxisGeometry(sizes[0],sizes[1]));
 		DefaultGeometryShader dgs = 
 			ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
 		graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
 		
 		DefaultLineShader dls = 
 		         (DefaultLineShader)dgs.createLineShader("default");
 		dls.setLineWidth(2.0);
 		dls.setDiffuseColor(java.awt.Color.black);
 		dgs.setShowFaces(false);
 		dgs.setShowLines(true);
 		dgs.setShowPoints(false);		
 		Appearance xaxisLabelApp = new Appearance();
 		Appearance yaxisLabelApp = new Appearance();
 		Appearance titleLabelApp = new Appearance();
 		xAxisLabel.setAppearance(xaxisLabelApp);
 		yAxisLabel.setAppearance(yaxisLabelApp);
 		titleLabel.setAppearance(titleLabelApp);
 		xaxisLabelApp.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
 		yaxisLabelApp.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
 		titleLabelApp.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
 		DefaultGeometryShader dgsLabel =
 			ShaderUtility.createDefaultGeometryShader(xaxisLabelApp, true);
 		dgsLabel.setShowFaces(false);
 		dgsLabel.setShowLines(false);
 		dgsLabel.setShowPoints(true);
 		DefaultPointShader dps =
 			(DefaultPointShader)dgsLabel.createPointShader("default");
 		dtsXAxisLabel = (DefaultTextShader)dps.getTextShader();
 		dps.setPointSize(1.0);
 		dps.setDiffuseColor(java.awt.Color.white);
 		dtsXAxisLabel.setDiffuseColor(java.awt.Color.black);
 		dtsXAxisLabel.setTextdirection(0);
 		dtsXAxisLabel.setScale(FONT_AXIS_SCALE);
 		//dtsAxisLabels.setAlignment(javax.swing.SwingConstants.CENTER);
 		dgsLabel =
 			ShaderUtility.createDefaultGeometryShader(yaxisLabelApp, true);
 		dgsLabel.setShowFaces(false);
 		dgsLabel.setShowLines(false);
 		dgsLabel.setShowPoints(true);
 		dps =
 			(DefaultPointShader)dgsLabel.createPointShader("default");
 		dtsYAxisLabel = (DefaultTextShader)dps.getTextShader();
 		dps.setPointSize(1.0);
 		dps.setDiffuseColor(java.awt.Color.white);
 		dtsYAxisLabel.setDiffuseColor(java.awt.Color.black);
 		dtsYAxisLabel.setTextdirection(1);
 		dtsYAxisLabel.setScale(FONT_AXIS_SCALE);
 
 		dgsLabel =
 			ShaderUtility.createDefaultGeometryShader(titleLabelApp, true);
 		dgsLabel.setShowFaces(false);
 		dgsLabel.setShowLines(false);
 		dgsLabel.setShowPoints(true);
 		dps =
 			(DefaultPointShader)dgsLabel.createPointShader("default");
 		dps.setPointSize(1.0);
 		dps.setDiffuseColor(java.awt.Color.white);
 		dtsTitleLabel = (DefaultTextShader)dps.getTextShader();
 		dtsTitleLabel.setDiffuseColor(java.awt.Color.black);
 		dtsTitleLabel.setTextdirection(0);
 		dtsTitleLabel.setScale(FONT_AXIS_SCALE);
 		
 		java.awt.Dimension dim = app.getCurrentViewer().getViewingComponentSize();
 		int width = dim.width;
 		int height = dim.height;
 		if (width == 0)
 			width = FONT_SIZE_PIXELS_WIDTH;
 		if (height == 0)
 			height = FONT_SIZE_PIXELS_HEIGHT;
 		
 		MatrixBuilder.euclidean().translate(-MAXX*0.5,-MAXY*0.5,0.0).assignTo(axis);			
 		
 		double fontScale = 1.0;
 		
 		if (width < FONT_SIZE_PIXELS_WIDTH)
 		{
 			fontScale = FONT_SIZE_PIXELS_WIDTH/(double)dim.width;
 			
 		}
 		if (height < FONT_SIZE_PIXELS_HEIGHT)
 		{
 			fontScale = 
 				Math.max(fontScale,(double)FONT_SIZE_PIXELS_HEIGHT/(double)dim.height);
 		}
 		if (fontScale > 1.0)
 		{
 			dtsXAxisLabel.setScale(FONT_AXIS_SCALE * fontScale);
 			dtsYAxisLabel.setScale(FONT_AXIS_SCALE * fontScale);
 			dtsTitleLabel.setScale(FONT_AXIS_SCALE * fontScale);
 		}		
 		return axis;
 	}
 
 	protected IndexedFaceSet createGraphGeometry(double xSize, double ySize, double xStart, double yStart)
 	{
 		QuadMeshFactory factory = new QuadMeshFactory();
 		double [][][] coords = new double [2][2][3];
 		coords[0][0][0] = xStart;
         coords[0][0][1] = yStart+ySize;
 		coords[0][0][2] = 0.0;
 		
 		coords[0][1][0] = xStart+xSize;
         coords[0][1][1] = yStart+ySize;
 		coords[0][1][2] = 0.0;
 
 		
 		coords[1][0][0] = xStart;
         coords[1][0][1] = yStart;
 		coords[1][0][2] = 0.0;
 
 		coords[1][1][0] = xStart+xSize;
         coords[1][1][1] = yStart;
 		coords[1][1][2] = 0.0;
 
 		factory.setVLineCount(2);		// important: the v-direction is the left-most index
 		factory.setULineCount(2);		// and the u-direction the next-left-most index
 		factory.setClosedInUDirection(false);	
 		factory.setClosedInVDirection(false);	
 		factory.setVertexCoordinates(coords);	
 		factory.setGenerateFaceNormals(true);
 		factory.setGenerateTextureCoordinates(true);
 		factory.update();
 		coords = null;
 		return factory.getIndexedFaceSet();			
 	}
 
 	private Texture2D generateSoftwareTexture(Appearance ap, de.jreality.shader.ImageData texImg)
 	{
 		Texture2D texture = null;
 		texture = TextureUtility.createTexture(ap, POLYGON_SHADER, texImg);
 		texture.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
 		texture.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
 		texture.setMagFilter(Texture2D.GL_NEAREST);
 		texture.setMinFilter(Texture2D.GL_NEAREST);
 		texture.setMipmapMode(true);
 		return texture;
 	}
 	
 	private void generateOverlayTexture(Appearance ap)
 	{
 		
 		//if (hasJOGLshaders)
 		{
 			Texture2D overlayTexture = overlayTextures.get(ap);
 			overlayTexture.getImage().updateData(overlayImage.getImageData());
 		}
 		overlayImage.clean();
 	}
 	
 	protected void generateFloatTexture(IDataset data, 
 									    Appearance ap, 
 									    int xpos,
 									    int ypos,
 									    int width, 
 									    int height,
 									    boolean createNewTexture)
 	{
 		if (createNewTexture) {
 			
 			float[] imageData = new float[width * height];
 			imageDatas.put(ap, imageData);
 		}
 		float[] imageData = imageDatas.get(ap);
 		Texture2D currentTexture = textures.get(ap);
 		FloatDataset fdata = (FloatDataset)DatasetUtils.cast(DatasetUtils.convertToAbstractDataset(data), AbstractDataset.FLOAT32);
 		if (width == fdata.getShape()[1] &&	height == fdata.getShape()[0]) 
 		{
 			imageData = fdata.getData();
 		} else {
 			for (int y = 0; y < height; y++)
 				for (int x = 0; x < width; x++) {
 					imageData[x+y*width] = fdata.getFloat(fdata.getShape()[0]-1-(ypos+(height-1-y)),xpos+x);
 				}
 		}
 		if (!createNewTexture) {
 			createNewTexture = (currentTexture == null ||
 								currentTexture.getImage().getHeight() != height ||
 								currentTexture.getImage().getWidth() != width);
 		}		
 		if (createNewTexture) {
 			de.jreality.shader.ImageData texImg = new de.jreality.shader.ImageData(imageData, width, height);
 			currentTexture = TextureUtility.createTexture(ap, POLYGON_SHADER, texImg);
 			currentTexture.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
 			currentTexture.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
 			currentTexture.setMagFilter(Texture2D.GL_NEAREST);
 			currentTexture.setMinFilter(Texture2D.GL_LINEAR);
 			currentTexture.setMipmapMode(true);
 			texImg = null;
 			textures.put(ap, currentTexture);
 			System.gc();
 		} else if (currentTexture != null) {
 			de.jreality.shader.ImageData texImg = currentTexture.getImage();
 			texImg.updateData(imageData);
 		} else {
 			logger.warn("Upps no texture to update found");
 		}
 	}
 	
 
 	private void generateRGBTexture(AbstractCompoundDataset data,
 									Appearance ap,
 									int xpos,
 									int ypos,
 									int width,
 									int height,
 									boolean createNewTexture)
 	{
 		if (createNewTexture) {
 			byte[] softwareImageRGBAdata = new byte[width * height * 4];
 			softwareImageRGBAdatas.put(ap, softwareImageRGBAdata);
 		}
 		byte[] softwareImageRGBAdata = softwareImageRGBAdatas.get(ap);
 		Texture2D currentTexture = textures.get(ap);
 		int si = 0;
 		int srcHeight = data.getShape()[0];
 		if (data instanceof RGBDataset) {
 			RGBDataset rgbData = (RGBDataset)data;
 			short[] rgbImgData = rgbData.getData();
 			int srcWidth = rgbData.getShape()[1];
 			for (int y = 0; y < height; y++) {
 				int di = (xpos + (srcHeight -1 -ypos-(height-1-y))*srcWidth)*3;
 				for (int x = 0; x < width; x++) {
 					short sRed = rgbImgData[di++];
 					short sGreen = rgbImgData[di++];
 					short sBlue = rgbImgData[di++];
 					softwareImageRGBAdata[si++] = (byte) sRed;
 					softwareImageRGBAdata[si++] = (byte) sGreen;
 					softwareImageRGBAdata[si++] = (byte) sBlue;
 					softwareImageRGBAdata[si++] = ~0;
 				}
 			}
 		} else {
 			int isize = data.getElementsPerItem();
 			switch(data.getDtype()) {
 			
 				case AbstractDataset.ARRAYINT8:
 				{
 					CompoundByteDataset cbData = (CompoundByteDataset)data;
 					for (int y = 0; y < height; y++) {
 						int yDataPos = srcHeight -1 -ypos-(height-1-y);
 						for (int x = 0; x < width; x++) {
 							byte[] rgba = cbData.getByteArray(yDataPos, x + xpos);
 							softwareImageRGBAdata[si++] = rgba[0];
 							softwareImageRGBAdata[si++] = rgba[1];
 							softwareImageRGBAdata[si++] = rgba[2];
 							softwareImageRGBAdata[si++] = isize > 3 ? rgba[3] : ~0;
 						}
 					}
 				}
 				break;
 				case AbstractDataset.ARRAYINT16:
 				{
 					CompoundShortDataset csData = (CompoundShortDataset)data;
 					double mins[] = data.minItem();
 					double maxs[] = data.maxItem();
 					short redRange = (short) Math.max(1,(short)(maxs[0]-mins[0]));
 					short greenRange = (short)Math.max(1,(maxs[1]-mins[1]));
 					short blueRange = (short)Math.max(1,(maxs[2]-mins[2]));
 					short alphaRange = isize > 3 ? (short)(maxs[3]-mins[3]) : 0;
 					
 					for (int y = 0; y < height; y++) {
 						int yDataPos = srcHeight -1 -ypos-(height-1-y);
 						for (int x = 0; x < width; x++) {
 							short[] rgba = csData.getShortArray(yDataPos, x + xpos);
 							short sRed = rgba[0];
 							short sGreen = rgba[1];
 							short sBlue = rgba[2];
 							short sAlpha = isize > 3 ? rgba[3] : 255;
 							double temp = (sRed - mins[0]) / redRange;
 							sRed = (short) (255 * temp);
 							temp = (sGreen - mins[1]) / greenRange;
 							sGreen = (short) (255 * temp);
 							temp = (sBlue - mins[2]) / blueRange;
 							sBlue = (short) (255 * temp);
 							if (alphaRange != 0) {
 								temp = (sAlpha - mins[3]) / alphaRange;
 								sAlpha = (short) (255 * temp);
 							}
 							softwareImageRGBAdata[si++] = (byte) sRed;
 							softwareImageRGBAdata[si++] = (byte) sGreen;
 							softwareImageRGBAdata[si++] = (byte) sBlue;
 							softwareImageRGBAdata[si++] = (byte) sAlpha;
 						}
 					}
 				}
 				break;
 				case AbstractDataset.ARRAYINT32:
 				{
 					CompoundIntegerDataset ciData = (CompoundIntegerDataset)data;
 					double mins[] = data.minItem();
 					double maxs[] = data.maxItem();
 					int redRange = (int)Math.max(1, (maxs[0]-mins[0]));
 					int greenRange = (int)Math.max(1,(maxs[1]-mins[1]));
 					int blueRange = (int)Math.max(1,(maxs[2]-mins[2]));
 					int alphaRange = isize > 3 ? (int)(maxs[3]-mins[3]) : 0;
 					
 					for (int y = 0; y < height; y++) {
 						int yDataPos = srcHeight -1 -ypos-(height-1-y);
 						for (int x = 0; x < width; x++) {
 							int[] rgba = ciData.getIntegerArray(yDataPos, x + xpos);
 							int sRed = rgba[0];
 							int sGreen = rgba[1];
 							int sBlue = rgba[2];
 							int sAlpha = isize > 3 ? rgba[3] : 255;
 
 							double temp = (sRed - mins[0]) / redRange;
 							sRed = (int)(255 * temp);
 							temp = (sGreen - mins[1]) / greenRange;							
 							sGreen = (int)(255 * temp);
 							temp = (sBlue - mins[2]) / blueRange;							
 							sBlue = (int)(255 * temp);
 							if (alphaRange != 0) {
 								temp = (sBlue - mins[2]) / alphaRange;							
 								sAlpha =  (int)(255 * temp);
 							}
 							softwareImageRGBAdata[si++] = (byte) sRed;
 							softwareImageRGBAdata[si++] = (byte) sGreen;
 							softwareImageRGBAdata[si++] = (byte) sBlue;
 							softwareImageRGBAdata[si++] = (byte) sAlpha;
 						}
 					}
 				}
 				break;
 				case AbstractDataset.ARRAYINT64:
 				{
 					CompoundLongDataset clData = (CompoundLongDataset) data;
 					double mins[] = data.minItem();
 					double maxs[] = data.maxItem();
 					long redRange = (long)Math.max(1, (maxs[0]-mins[0]));
 					long greenRange = (long)Math.max(1,(maxs[1]-mins[1]));
 					long blueRange = (long)Math.max(1,(maxs[2]-mins[2]));
 					long alphaRange = isize > 3 ? (long)(maxs[3]-mins[3]) : 0;
 					
 					for (int y = 0; y < height; y++) {
 						int yDataPos = srcHeight -1 -ypos-(height-1-y);
 						for (int x = 0; x < width; x++) {
 							long[] rgba = clData.getLongArray(yDataPos, x + xpos);
 							long sRed = rgba[0];
 							long sGreen = rgba[1];
 							long sBlue = rgba[2];
 							long sAlpha = isize > 3 ? rgba[3] : 255;
 
 							double temp = (sRed - mins[0]) / redRange;
 							sRed = (long)(255 * temp);
 							temp = (sGreen - mins[1]) / greenRange;							
 							sGreen = (long)(255 * temp);
 							temp = (sBlue - mins[2]) / blueRange;							
 							sBlue = (long)(255 * temp);
 							if (alphaRange != 0) {
 								temp = (sBlue - mins[2]) / alphaRange;							
 								sAlpha =  (long)(255 * temp);
 							}
 							softwareImageRGBAdata[si++] = (byte) sRed;
 							softwareImageRGBAdata[si++] = (byte) sGreen;
 							softwareImageRGBAdata[si++] = (byte) sBlue;
 							softwareImageRGBAdata[si++] = (byte) sAlpha;
 						}
 					}
 				}
 				break;
 				case AbstractDataset.ARRAYFLOAT32:
 				case AbstractDataset.ARRAYFLOAT64:
 				{
 					double mins[] = data.minItem();
 					double maxs[] = data.maxItem();
 					double redRange = (maxs[0]-mins[0]);
 					double greenRange = (maxs[1]-mins[1]);
 					double blueRange = (maxs[2]-mins[2]);
 					double alphaRange = isize > 3 ? (maxs[3]-mins[3]) : 0;
 					
 					double[] rgba = new double[4];
 					for (int y = 0; y < height; y++) {
 						int yDataPos = srcHeight -1 -ypos-(height-1-y);
 						for (int x = 0; x < width; x++) {
 							data.getDoubleArray(rgba, yDataPos, x+xpos);
 							double sRed = rgba[0];
 							double sGreen = rgba[1];
 							double sBlue = rgba[2];
 							double sAlpha = isize > 3 ? rgba[3] : 255.0;
 
 							sRed = (255.0 * ((sRed - mins[0]) / redRange));
 							sGreen = (255.0 * ((sGreen - mins[1]) / greenRange));
 							sBlue = (255.0 * ((sBlue - mins[2]) / blueRange));
 							if (alphaRange != 0)
 								sAlpha =  (255.0 * ((sAlpha - mins[3]) / alphaRange));
 
 							softwareImageRGBAdata[si++] = (byte) sRed;
 							softwareImageRGBAdata[si++] = (byte) sGreen;
 							softwareImageRGBAdata[si++] = (byte) sBlue;
 							softwareImageRGBAdata[si++] = (byte) sAlpha;
 						}
 					}
 				}
 				break;
 			}
 		}
 		if (!createNewTexture) {
 			createNewTexture = (currentTexture == null ||
 								currentTexture.getImage().getHeight() != height ||
 								currentTexture.getImage().getWidth() != width);
 		}		
 		if (createNewTexture) {
 			de.jreality.shader.ImageData texImg = new de.jreality.shader.ImageData(softwareImageRGBAdata, width, height);
 			currentTexture = TextureUtility.createTexture(ap, POLYGON_SHADER, texImg);
 			currentTexture.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
 			currentTexture.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
 			currentTexture.setMagFilter(Texture2D.GL_NEAREST);
 			currentTexture.setMinFilter(Texture2D.GL_LINEAR);
 			currentTexture.setMipmapMode(true);
 			textures.put(ap, currentTexture);
 			texImg = null;		
 			System.gc();
 		} else if (currentTexture != null) {
 			de.jreality.shader.ImageData texImg = currentTexture.getImage();
 			texImg.updateData(softwareImageRGBAdata);			
 		} else {
 			logger.warn("Tried to update a texture that does not exist!");
 		}
 	}
 	
 	protected void generateTexture(IDataset data, Appearance ap, 
 								   int xPos, int yPos, int width, int height)
 	{
 		if (hasJOGLshaders)
 		{
 			boolean createNewTexture = false;
 			boolean useRGB = 
 				(data instanceof RGBDataset) ||
  	  	  	    (data instanceof AbstractCompoundDataset &&
 				(((AbstractCompoundDataset)data).getElementsPerItem() == 3 ||
 		 		 ((AbstractCompoundDataset)data).getElementsPerItem() == 4));
 			
 			if (!useRGB) {
 				currentImageType = 0;
 				float[] imageData = imageDatas.get(ap);
 				if (imageData == null || 
 					imageData.length != width * height ||
 					currentImageType != lastImageType) {
 					createNewTexture = true;
 				}
 				generateFloatTexture(data, ap, xPos, yPos, width, height, createNewTexture);
 			} else {
 				currentImageType = 1;
 				byte[] softwareImageRGBAdata = softwareImageRGBAdatas.get(ap);
 				if (softwareImageRGBAdata == null || 
 					softwareImageRGBAdata.length != (width * height * 4) ||
 						currentImageType != lastImageType) {
 						createNewTexture = true;
 					}
 				generateRGBTexture((AbstractCompoundDataset) data,ap,xPos,yPos,width,height,createNewTexture);
 			}
 			if (lastImageType != currentImageType)
 				loadGLSLProgram(tableMin,tableMax);
 			
 			lastImageType = currentImageType;
 		} else {
 			//System.err.println("Generate texture x"+xPos+" y "+yPos+" w "+width+" h "+height);			
 			double maxValue = data.max().doubleValue();
 			double minValue = data.min().doubleValue();
 			int srcHeight = data.getShape()[0];
 			int srcWidth = data.getShape()[1];
 			softwareImageRGBAdata = new byte[width * height * 4];
 			if (!(data instanceof RGBDataset))  { 
 				for (int y = 0; y < height; y++) {
 					for (int x = 0; x < width; x++) {
 						int yDataPos = srcHeight -1 -yPos-(height-1-y);
 						double value = data.getDouble(yDataPos, x+xPos);
 						byte red = (byte)(255 * value / (maxValue - minValue));
 						byte green = (byte)(255 * value / (maxValue - minValue));
 						byte blue = (byte)(255 * value / (maxValue - minValue));
 						byte alpha = ~0;
 						softwareImageRGBAdata[(x + y * width)*4] = red;
 						softwareImageRGBAdata[(x + y * width)*4 + 1] = green;
 						softwareImageRGBAdata[(x + y * width)*4 + 2] = blue;
 						softwareImageRGBAdata[(x + y * width)*4 + 3] = alpha;					
 					}
 				}
 			} else {
 				RGBDataset rgbData = (RGBDataset)data;
 				short[] rgbImgData = rgbData.getData();
 				for (int x = 0; x < width; x++)
 					for (int y = 0; y < height; y++)
 					{
 						int yDataPos = srcHeight -1 -yPos-(height-1-y);
 						short sRed = rgbImgData[(x+xPos+yDataPos*srcWidth)*3];
 						short sGreen = rgbImgData[(x+xPos+yDataPos*srcWidth)*3 + 1];
 						short sBlue = rgbImgData[(x+xPos+yDataPos*srcWidth)*3 + 2];
 						byte red = (byte)(sRed);
 						byte green = (byte)(sGreen);
 						byte blue = (byte)(sBlue);
 						byte alpha = ~0;
 						softwareImageRGBAdata[(x + y * width)*4] = red;
 						softwareImageRGBAdata[(x + y * width)*4 + 1] = green;
 						softwareImageRGBAdata[(x + y * width)*4 + 2] = blue;
 						softwareImageRGBAdata[(x + y * width)*4 + 3] = alpha;					
 					}				
 			}
 			de.jreality.shader.ImageData texImg = new de.jreality.shader.ImageData(softwareImageRGBAdata, width, height);
 			generateSoftwareTexture(ap, texImg);
 			texImg = null;
 		}
 	}
 
 	protected void determineRanges(List<IDataset> datasets)
 	{
 		Iterator<IDataset> iter = datasets.iterator();
 		globalRealXmin = Float.MAX_VALUE;
 		globalRealXmax = Float.MIN_VALUE;
 		globalRealYmin = Float.MAX_VALUE;
 		globalRealYmax = Float.MIN_VALUE;
 
 		while (iter.hasNext())			
 		{
 			IDataset set = iter.next();
 			switch (xAxis)
 			{
 				case LINEAR:
 					globalRealXmin = 0;
 					globalRealXmax = Math.max(globalRealXmax, set.getShape()[1]);
 				break;
 				case LINEAR_WITH_OFFSET:
 				{
 					globalRealXmin = xOffset;
 					globalRealXmax = Math.max(globalRealXmax,set.getShape()[1]);
 				}
 				break;
 				case CUSTOM:
 				{
 					globalRealXmin = Math.min(globalRealXmin, xAxisValues.getMinValue());
 					globalRealXmax = Math.max(globalRealXmax, xAxisValues.getMaxValue());
 				}
 				break;
 			}
 			switch (yAxis)
 			{
 				case LINEAR:
 					globalRealYmin = 0;
 					globalRealYmax = Math.max(globalRealYmax, set.getShape()[0]);
 					break;
 				case LINEAR_WITH_OFFSET:
 				{
 					globalRealYmin = yOffset;
 					globalRealYmax = Math.max(globalRealYmax,set.getShape()[0]);
 				}
 				break;
 				case CUSTOM:
 				{
 					globalRealYmin = Math.min(globalRealYmin, yAxisValues.getMinValue());
 					globalRealYmax = Math.max(globalRealYmax, yAxisValues.getMaxValue());
 				}	
 				break;			
 			}
 		}
 	}
 	
 	private void determineRanges(IDataset set)
 	{
 
 		switch (xAxis)
 		{
 			case LINEAR:
 				globalRealXmin = 0;
 				globalRealXmax = Math.max(globalRealXmax, set.getShape()[1]);
 			break;
 			case LINEAR_WITH_OFFSET:
 			{
 				globalRealXmin = xOffset;
 				globalRealXmax = Math.max(globalRealXmax,set.getShape()[1]+xOffset);
 			}
 			break;
 			case CUSTOM:
 				globalRealXmin = xAxisValues.getMinValue();
 				globalRealXmax = xAxisValues.getMaxValue();
 			break;
 		}
 		switch (yAxis)
 		{
 			case LINEAR:
 				globalRealYmin = 0;
 				globalRealYmax = Math.max(globalRealYmax, set.getShape()[0]);
 			break;
 			case LINEAR_WITH_OFFSET:
 			{
 				globalRealYmin = yOffset;
 				globalRealYmax = Math.max(globalRealYmax,set.getShape()[0]+yOffset);
 			}
 			break;
 			case CUSTOM:
 				globalRealYmin = yAxisValues.getMinValue();
 				globalRealYmax = yAxisValues.getMaxValue();
 			break;
 		}
 	}
 
 	protected void updateClipPlanePositions(double[] sizes) {
 	    MatrixBuilder.euclidean().rotateY(-0.5*Math.PI).assignTo(leftClip);
 	    MatrixBuilder.euclidean().rotateY(0.5*Math.PI).rotateX(0.5*Math.PI).assignTo(bottomClip);
 	    MatrixBuilder.euclidean().rotateX(-0.5*Math.PI).translate(0.0f,sizes[1],0.0f).rotateX(-0.5*Math.PI).assignTo(topClip);
 	    MatrixBuilder.euclidean().rotateX(0.5*Math.PI).translate(sizes[0],-sizes[1],0.0f).rotateY(0.5*Math.PI).assignTo(rightClip);
 	    MatrixBuilder.euclidean().rotateY(-0.5*Math.PI).translate(-sizes[0],0.0,0.0).assignTo(graphGroupNode);		
 	}
 	/**
 	 * Build a cascade of clip planes to allow for clipping the overlay primitives
 	 * @param graph SceneGraphComponent node where the graph is located
 	 */
 	
 	private void buildClipPlanes(SceneGraphComponent graph) {
 		if (leftClip == null) {
 			ClippingPlane plane = new ClippingPlane();
 			plane.setLocal(true);
 			leftClip = new SceneGraphComponent("leftClipBorder");
 			leftClip.setGeometry(plane);
 			graph.addChild(leftClip);
 		}
 		if (bottomClip == null) {
 			ClippingPlane plane = new ClippingPlane();
 			plane.setLocal(true);
 			bottomClip = new SceneGraphComponent("bottomClipBorder");
 			bottomClip.setGeometry(plane);
 			leftClip.addChild(bottomClip);
 		}
 		if (topClip == null) {
 			ClippingPlane plane = new ClippingPlane();
 			plane.setLocal(true);
 			topClip = new SceneGraphComponent("topClipBorder");
 			topClip.setGeometry(plane);
 			bottomClip.addChild(topClip);
 		}
 		if (rightClip == null) {
 			rightClip = new SceneGraphComponent("rightClipBorder");
 			ClippingPlane plane = new ClippingPlane();
 			plane.setLocal(true);
 			rightClip.setGeometry(plane);
 			topClip.addChild(rightClip);
 			rightClip.addChild(graphGroupNode);
 		}
 		double [] sizes = determineXYsize();
 		updateClipPlanePositions(sizes);
 	}
 	
 	protected void loadGLSLProgram(double min, double max) {
 		if (currentImageType == 0) {
 			if (tableProg == null) {
 				tableProg = new GlslProgram(graphAppearance,"polygonShader",null,
 						JOGLGLSLShaderGenerator.generateShader(useLogarithmic, false, useDiffractionMode,useGradientMode),
 						JOGLGLSLShaderGenerator.generateShaderName(useLogarithmic, false,useDiffractionMode,useGradientMode));
 			} else {
 				tableProg.setShaders(null, 
 									 JOGLGLSLShaderGenerator.generateShader(useLogarithmic, false, useDiffractionMode, useGradientMode),
 									 JOGLGLSLShaderGenerator.generateShaderName(useLogarithmic, false,useDiffractionMode,useGradientMode));
 			}
 			if (tableProg != null) {
 				tableProg.setUniform("sampler", 0);
 				tableProg.setUniform("tableSampler", 1);
 				tableProg.setUniform("overlaySampler", 2);
 				if (useLogarithmic)
 				{
 					ScalingUtility.setSmallLogFlag(false);
 					tableProg.setUniform("maxValue", ScalingUtility.valueScaler(max,ScaleType.LN));
 					tableProg.setUniform("minValue", ScalingUtility.valueScaler(min,ScaleType.LN));
 				} else {
 					tableProg.setUniform("maxValue", max);
 					tableProg.setUniform("minValue", min);
 				}		
 			}
 		} else {
 			if (tableProg == null) {
 				tableProg = new GlslProgram(graphAppearance,"polygonShader",null,
 											JOGLGLSLShaderGenerator.generateShader(false,true,false,false),
 											JOGLGLSLShaderGenerator.generateShaderName(false,true,false,false));
 			} else {
 				tableProg.setShaders(null,
 									JOGLGLSLShaderGenerator.generateShader(false,true,false,false),
 									JOGLGLSLShaderGenerator.generateShaderName(false,true,false,false));						
 			}
 			tableProg.setUniform("sampler", 0);
 			tableProg.setUniform("overlaySampler", 2);
 		}
 		// update all the sub graph to use the new shader source
 	    Iterator<Appearance> iter = graphApps.iterator();
 	    while (iter.hasNext()) {
 	    	Appearance ap = iter.next();
 			ap.setAttribute(POLYGON_SHADER+"::glsl-source", tableProg.getSource());	    	
 	    }
 	}
 	
 	protected Appearance createAppearance() {
 		Appearance graphAppearance = new Appearance();
 		graphApps.add(graphAppearance);
 		graphAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
 		DefaultGeometryShader dgs = 
 			ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
 		DefaultPolygonShader dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
 		dps.setDiffuseColor(java.awt.Color.white);
 		dgs.setShowFaces(true);
 		dgs.setShowLines(false);
 		dgs.setShowPoints(false);				
 		return graphAppearance;
 	}
 	
 	protected void setShaderOnAppearance(Appearance graphApp) {
 		if (hasJOGLshaders)
 		{
 			Texture2D lookupTex = null;			
 			if (tableProg != null) {	
 				graphApp.setAttribute("useGLSL", true);
 				graphApp.setAttribute(POLYGON_SHADER+"::glsl-source", tableProg.getSource());
 				// TODO generate only one instance of this please
 		//		if (lookupTex == null) {
 					lookupTex = (Texture2D)
 						AttributeEntityUtility.createAttributeEntity(Texture2D.class,
 																	 POLYGON_SHADER+"."+TEXTURE_2D_1,
 																	 graphApp,true);
 					lookupTex.setImage(lookupTableImg);
 					lookupTex.setMagFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
 					lookupTex.setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
 					lookupTex.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
 					lookupTex.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
 		//		} else {
 		//			graphAppearance.setAttribute(POLYGON_SHADER+"."+TEXTURE_2D_1, lookupTex);
 		//		}
 			}					
 		}		
 	}
 	
 	protected void setupOtherNodes() {
 		MatrixBuilder.euclidean().translate(-MAXX*0.5,-MAXY*0.5,0.0).assignTo(graph);
 		if (xTickNode != null) 
 			xTickNode.setGeometry(createXTicksGeometry());
 		if (yTickNode != null)
 			yTickNode.setGeometry(createYTicksGeometry());
 		if (xLabelNode != null)
 			xLabelNode.setGeometry(createXLabelsGeometry());
 		if (yLabelNode != null)
 			yLabelNode.setGeometry(createYLabelsGeometry());
 		if (ticksNode != null)				
 			MatrixBuilder.euclidean().translate(-MAXX*0.5,-MAXY*0.5,0.0).assignTo(ticksNode);
 		if (xLabelNode != null)
 			MatrixBuilder.euclidean().translate(-MAXX*0.5,-MAXY*0.5,0.0).assignTo(xLabelNode);
 		if (yLabelNode != null)
 			MatrixBuilder.euclidean().translate(-MAXX*0.5,-MAXY*0.5,0.0).assignTo(yLabelNode);
 		Camera sceneCamera = CameraUtility.getCamera(app.getCurrentViewer());
         sceneCamera.setFieldOfView(52.5f);
         posTool.setImageHeight(maxHeight);
         posTool.setImageWidth(maxWidth);
         double[] sizes = determineXYsize();
 		if (axis != null)
 			axis.setGeometry(createAxisGeometry(sizes[0],sizes[1]));
 
         panTool.setDataDimension(sizes[0],sizes[1]);
         posTool.setMaxXY(sizes[0],sizes[1]);
         buildBackground(graph);
         buildClipPlanes(graph);
         
         // We need to do this because jReality's picking routines are
         // a bit off probably due to the view frustum correction
         // so we store the parent of the graph node as its owner
         // to make sure that the tool gets called even when the
         // graph node itself hasn't been hit by the picking routine
         
         ((SceneGraphComponent)graph.getOwner()).addTool(posTool); 
         subGraphs.get(subGraphs.size()-1).addChild(overlayPrimitiveGroupNode);		
 	}
 	
 	@Override
 	public SceneGraphComponent buildGraph(List<IDataset> datasets,
 			SceneGraphComponent graph) {
 		assert (datasets.size() > 0);
 		maxWidth = maxHeight = 0;
 		if (graph != null)
 		{
 			graphGroupNode = new SceneGraphComponent("Graph Group node");
 			overlayPrimitiveGroupNode = new SceneGraphComponent("Overlay group node");
 			this.graph = graph;
 			Iterator<IDataset> iter = datasets.iterator();
 			numGraphs = 0;
 			while (iter.hasNext())
 			{
 				IDataset currentData = iter.next();
 				int width = currentData.getShape()[1];
 				int height = currentData.getShape()[0];
 				maxWidth = Math.max(width,maxWidth);
 				maxHeight = Math.max(height,maxHeight);
 				double[] sizes = determineXYsize();
 				int maxDimH = JOGLChecker.getMaxTextureHeight();
 				int maxDimW = JOGLChecker.getMaxTextureWidth();
 				double yStart = 0.0;
 				double ySubSize = ((double)maxDimH/(double)maxHeight) < 1.0 ?
 									sizes[1]*maxDimH/maxHeight : sizes[1]; 
 				double xSubSize = ((double)maxDimW/(double)maxWidth) < 1.0 ?
 						sizes[0]*maxDimW/maxWidth : sizes[0]; 
 
 					
 				for (int y = 0; y < maxHeight; y+= maxDimH, yStart+=ySubSize) {
 					double xStart = 0.0;
 					for (int x = 0; x < maxWidth; x+= maxDimW, xStart+=xSubSize) {
 						SceneGraphComponent subGraph =
 							SceneGraphUtility.createFullSceneGraphComponent(GRAPHNODENAME+numGraphs+".sub"+x+"_"+y);
 
 						subGraphs.add(subGraph);
 						graphGroupNode.addChild(subGraph);
 						
 						subGraph.setGeometry(createGraphGeometry((xStart+xSubSize > sizes[0] ? sizes[0]-xStart : xSubSize),
 																 (yStart+ySubSize > sizes[1] ? sizes[1]-yStart : ySubSize),
 																 xStart,yStart));
 						if (currentData instanceof AbstractCompoundDataset) {
 							tableMin = 0;
 							tableMax = 0;
 						} else {
 							tableMin = currentData.min().doubleValue();
 							tableMax = currentData.max().doubleValue();
 						}
 						int texWidth = (width > maxDimW ? maxDimW : width);
 						int texHeight = (height > maxDimH ? maxDimH : height);
 						if (x+texWidth > maxWidth) texWidth = maxWidth-x;
 						if (y+texHeight > maxHeight) texHeight = maxHeight-y;
 
 						graphAppearance = createAppearance();
 						subGraph.setAppearance(graphAppearance);
 						generateTexture(currentData, graphAppearance, x,y, texWidth, texHeight); 						
 						setShaderOnAppearance(graphAppearance);
 					}
 				}
 				numGraphs++;
 			}
 			determineRanges(datasets);
 			setupOtherNodes();
 		}
 		registerOverlayImage(1, 1);
 		return graph;
 	}
 
 	protected PointSet createYLabelsGeometry()
 	{
 		PointSetFactory factory = new PointSetFactory();
 		tickFactory.setTickMode(yLabelMode);
 		int height = app.getCurrentViewer().getViewingComponentSize().height;
 		if (height == 0)
 			height = FONT_SIZE_PIXELS_HEIGHT;
 		LinkedList<Tick> ticks =
 			tickFactory.generateTicks(height, globalRealYmin, globalRealYmax, (short)1,false);
 		double range = globalRealYmax - globalRealYmin;		
 		double[] sizes = determineXYsize();
 		double step = (sizes[1] * ((tickFactory.getTickUnit() * ticks.size())/range))/ticks.size();			
         String[] edgeLabels = new String[ticks.size()];
         factory.setVertexCount(ticks.size());
         double [][] coords = ArrayPoolUtility.getDoubleArray(ticks.size());
         if (yAxis == AxisMode.CUSTOM) {
         	boolean ascending = yAxisValues.isAscending();
 	        for (int i = 0; i < ticks.size(); i++)
 	        {
 				Tick currentTick = null;
 				if (ascending) 
 					currentTick = ticks.get(i);
 				else
 					currentTick = ticks.get(ticks.size()-1-i);
 				coords[i][0] = -0.7;
 				coords[i][1] = sizes[1] - i * step;
 				coords[i][2] = 0.0;
 				edgeLabels[i] = currentTick.getTickName();
 	        }
         } else {
 	        for (int i = 0; i < ticks.size(); i++)
 	        {
 				Tick currentTick = ticks.get(i);
 				coords[i][0] = -0.7;
 				coords[i][1] = sizes[1] - i * step;
 				coords[i][2] = 0.0;
 				edgeLabels[i] = currentTick.getTickName();
 	        }
         }
 		factory.setVertexCoordinates(coords);
 		factory.setVertexLabels(edgeLabels);		
 		factory.update();
 		return factory.getPointSet();
 	}
 	
 	protected PointSet createXLabelsGeometry()
 	{	
 		PointSetFactory factory = new PointSetFactory();
 		double min = globalRealXmin;
 		double max = globalRealXmax;
 		tickFactory.setTickMode(xLabelMode);
 		int width = app.getCurrentViewer().getViewingComponentSize().width;
 	    // protect against rubbish RCP early lazy initialisation
 		if (width == 0)
 	    	width = FONT_SIZE_PIXELS_WIDTH;
 		LinkedList<Tick> ticks = 
 			tickFactory.generateTicks((int)(labelScaling * width), 
 					min, max, (short)0,false);
 		
         String[] edgeLabels = new String[ticks.size()];
         factory.setVertexCount(ticks.size());
         double [][] coords = ArrayPoolUtility.getDoubleArray(ticks.size());
 		double[] sizes = determineXYsize();
 		if (xAxis == AxisMode.CUSTOM) {
 			boolean ascending = xAxisValues.isAscending();
 	        for (int i = 0; i < ticks.size(); i++)
 	        {
 	        	double value = tickFactory.getTickUnit() * i;
 	        	Tick currentTick = null;
 	        	if (ascending) 
 	        		currentTick = ticks.get(i);
 	        	else
 	        		currentTick = ticks.get(ticks.size()-1-i);
 	        		
 				coords[i][0] = (value/(globalRealXmax-globalRealXmin)) * sizes[0];
 				coords[i][1] = -0.275;
 				coords[i][2] = 0.0;
 				edgeLabels[i] = currentTick.getTickName();
 	        }			
 		} else {
 	        for (int i = 0; i < ticks.size(); i++)
 	        {
 	        	double value = tickFactory.getTickUnit() * i;
 	        	Tick currentTick = ticks.get(i);
 				coords[i][0] = (value/(globalRealXmax-globalRealXmin)) * sizes[0];
 				coords[i][1] = -0.275;
 				coords[i][2] = 0.0;
 				edgeLabels[i] = currentTick.getTickName();
 	        }
 		}
 		factory.setVertexCoordinates(coords);
 		factory.setVertexLabels(edgeLabels);		
 		factory.update();
 		return factory.getPointSet();		
 	}
 	
 
 	@Override
 	public void buildXCoordLabeling(SceneGraphComponent xLabelNode) {
 		this.xLabelNode = xLabelNode;
 		Appearance labelAppearance = new Appearance();
 		xLabelNode.setAppearance(labelAppearance);
 		DefaultGeometryShader dgsLabels =
 			ShaderUtility.createDefaultGeometryShader(labelAppearance,true);
 		labelAppearance.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
 		DefaultPointShader dps =
 			     (DefaultPointShader)dgsLabels.createPointShader("default");
 		dgsLabels.setShowFaces(false);
 		dgsLabels.setShowLines(false);
 		dgsLabels.setShowPoints(true);		
 		dps.setPointSize(1.0);
 		dps.setDiffuseColor(java.awt.Color.white);
 		dtsXTicks = (DefaultTextShader) dps.getTextShader();
 		double[] offset = new double[]{0,0.0,0.0};
 		dtsXTicks.setOffset(offset);
 		dtsXTicks.setScale(FONT_SCALE);
 		dtsXTicks.setDiffuseColor(java.awt.Color.black);
 		dtsXTicks.setAlignment(javax.swing.SwingConstants.CENTER);
 		xLabelNode.setGeometry(createXLabelsGeometry());
 	}
 
 	@Override
 	public void buildYCoordLabeling(SceneGraphComponent yLabelNode) {
 		this.yLabelNode = yLabelNode;
 		Appearance labelAppearance = new Appearance();
 		yLabelNode.setAppearance(labelAppearance);
 		DefaultGeometryShader dgsLabels =
 			ShaderUtility.createDefaultGeometryShader(labelAppearance,true);
 		labelAppearance.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
 		DefaultPointShader dps =
 			     (DefaultPointShader)dgsLabels.createPointShader("default");
 		dgsLabels.setShowFaces(false);
 		dgsLabels.setShowLines(false);
 		dgsLabels.setShowPoints(true);		
 		dps.setPointSize(1.0);
 		dps.setDiffuseColor(java.awt.Color.white);
 		dtsYTicks = (DefaultTextShader) dps.getTextShader();
 		double[] offset = new double[]{0,0.0,0.0};
 		dtsYTicks.setOffset(offset);
 		dtsYTicks.setScale(FONT_SCALE);
 		dtsYTicks.setDiffuseColor(java.awt.Color.black);
 		dtsYTicks.setAlignment(javax.swing.SwingConstants.CENTER);
 		yLabelNode.setGeometry(createYLabelsGeometry());
 	}
 
 	@Override
 	public void buildZCoordLabeling(SceneGraphComponent comp) {
 		// Nothing to do
 	}
 
 
 	@Override
 	public void handleColourCast(ColourImageData colourTable,
 								 SceneGraphComponent graph,
 								 double minValue,
 								 double maxValue) {
 		if (hasJOGLshaders && tableProg != null)
 		{
 			tableMin = minValue;
 			tableMax = maxValue;
 			byte[] lookupTable = new byte[colourTable.getWidth()*4];
 			for (int x = 0; x < colourTable.getWidth(); x++)
 			{
 				int RGBAvalue = colourTable.get(x,0);
 				byte red = (byte) ((RGBAvalue >> 16) & 0xff);
 				byte green = (byte) ((RGBAvalue >> 8) & 0xff);
 				byte blue = (byte) ((RGBAvalue) & 0xff);
 				lookupTable[x * 4] = red;
 				lookupTable[x * 4 + 1] = green;
 				lookupTable[x * 4 + 2] = blue;
 				lookupTable[x * 4 + 3] = ~0;				
 			}
 			lookupTableImg = null;
 			lookupTableImg = new de.jreality.shader.ImageData(lookupTable,colourTable.getWidth(),1);
 			Iterator<Appearance> iter = graphApps.iterator();
 			
 			while (iter.hasNext())
 			{
 				Appearance graphAppearance = iter.next();
 				Texture2D lookupTex = (Texture2D)
 					AttributeEntityUtility.getAttributeEntity(Texture2D.class,
 															  POLYGON_SHADER+"."+TEXTURE_2D_1,
 															  graphAppearance,true);
 				lookupTex.setMagFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
 				lookupTex.setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
 				lookupTex.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
 				lookupTex.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
 				lookupTex.setImage(lookupTableImg);
 			}
 			if (useLogarithmic)
 			{
 				ScalingUtility.setSmallLogFlag(false);
 				tableProg.setUniform("maxValue", ScalingUtility.valueScaler(maxValue,ScaleType.LN));
 				tableProg.setUniform("minValue", ScalingUtility.valueScaler(minValue,ScaleType.LN));
 			} else {
 				tableProg.setUniform("maxValue", maxValue);
 				tableProg.setUniform("minValue", minValue);
 //				if (useGradientMode) {
 //					float threshold = (float)(maxValue * 10.0f);
 //					if (tableMax > 250)
 //						threshold = (float)(Math.pow(tableMax,3.0f));
 					
 //					tableProg.setUniform("threshold", threshold);
 //				}
 				//tableProg.setUniform("threshold", maxValue * 1.25);
 			}
 		} else {
 			
 			int width = colourTable.getWidth();
 			int height = colourTable.getHeight();
 
 			int maxDimH = JOGLChecker.getMaxTextureHeight();
 			int maxDimW = JOGLChecker.getMaxTextureWidth();
 			Iterator<Appearance> appIter = graphApps.iterator();
 			
 			for (int y = 0; y < height; y+= maxDimH) {
 				for (int x = 0; x < width; x+= maxDimW) {			
 					Appearance graphApp = appIter.next();
 					int texWidth = (width > maxDimW ? maxDimW : width);
 					int texHeight = (height > maxDimH ? maxDimH : height);
 					if (x+texWidth > maxWidth) texWidth = maxWidth-x;
 					if (y+texHeight > maxHeight) texHeight = maxHeight-y;					
 					softwareImageRGBAdata = new byte[texWidth * texHeight * 4];
 					for (int sy = 0; sy < texHeight; sy++) {
 						for (int sx = 0; sx < texWidth; sx++) {
 							int yDataPos = height -1 -y-(texHeight-1-sy);							
 							int RGBAvalue = colourTable.get(sx+x, yDataPos);
 							byte alpha = (byte)((RGBAvalue >> 24) & 0xff);
 							byte red = (byte) ((RGBAvalue >> 16) & 0xff);
 							byte green = (byte) ((RGBAvalue >> 8) & 0xff);
 							byte blue = (byte) ((RGBAvalue) & 0xff);
 							softwareImageRGBAdata[(sx + sy * texWidth) * 4] = red;
 							softwareImageRGBAdata[(sx + sy * texWidth) * 4 + 1] = green;
 							softwareImageRGBAdata[(sx + sy * texWidth) * 4 + 2] = blue;
 							softwareImageRGBAdata[(sx + sy * texWidth) * 4 + 3] = alpha;
 						}
 					}
 					de.jreality.shader.ImageData texImg = new de.jreality.shader.ImageData(softwareImageRGBAdata, texWidth, texHeight);
 					generateSoftwareTexture(graphApp, texImg);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void notifyComponentResize(int width, int height) {
 		double fontScale = 1.0;
 		canvasWidth = width;
 		canvasHeight = height;
 		if (width < FONT_SIZE_PIXELS_WIDTH)
 		{
 			fontScale = FONT_SIZE_PIXELS_WIDTH/(double)width;
 			
 		}
 		if (height < FONT_SIZE_PIXELS_HEIGHT)
 		{
 			fontScale = 
 				Math.max(fontScale,(double)FONT_SIZE_PIXELS_HEIGHT/(double)height);
 		}
 		fontScale = Math.max(fontScale,0.5);
 		if (dtsXAxisLabel != null) {
 			dtsXAxisLabel.setScale(FONT_AXIS_SCALE * fontScale);
 		}
 		if (dtsYAxisLabel != null) {
 			dtsYAxisLabel.setScale(FONT_AXIS_SCALE * fontScale);				
 		}
 		if (dtsTitleLabel != null) {
 			dtsTitleLabel.setScale(FONT_AXIS_SCALE * fontScale);
 		}
 		if (dtsXTicks != null) {
 			if (hasJOGL)
 				dtsXTicks.setScale(FONT_SCALE * fontScale*0.85);
 			else
 				dtsXTicks.setScale(FONT_SCALE * fontScale);
 		}
 		if (dtsYTicks != null) {
 			if (hasJOGL)
 				dtsYTicks.setScale(FONT_SCALE * fontScale*0.85);
 			else
 				dtsYTicks.setScale(FONT_SCALE * fontScale);				
 		}
 	}
 
 	@Override
 	public void setAxisModes(AxisMode axis, AxisMode axis2, AxisMode axis3) {
 		xAxis = axis;
 	    yAxis = axis2;
 	}
 
 	@Override
 	public void setScaling(ScaleType newScaling) {
 		if (graph != null) {
 			currentScale = newScaling;
 			switch (newScaling) {
 				case LINEAR:
 					if (useLogarithmic) {
 						useLogarithmic = false;
 						if (hasJOGLshaders) loadGLSLProgram(tableMin, tableMax);
 					}
 				break;
 				case LN:
 				case LOG10:
 				case LOG2:
 				{
 					if (!useLogarithmic) {
 						useLogarithmic = true;
 						if (hasJOGLshaders) loadGLSLProgram(tableMin, tableMax);
 					}
 				}
 				break;
 			}
 		} else
 			useLogarithmic = (newScaling != ScaleType.LINEAR);
 	}
 
	protected void createXAxisLabelGeom() {
		if (xAxisLabelStr != null) {
 			double[] sizes = determineXYsize();
 			PointSetFactory factory = new PointSetFactory();
 			factory.setVertexCount(1);
 			double [][] coords = ArrayPoolUtility.getDoubleArray(1);
 			String [] edgeLabels = new String[1];
 			edgeLabels[0] = xAxisLabelStr;
 			coords[0][0] = sizes[0] * 0.5;
 			coords[0][1] = -0.5;
 			coords[0][2] = 0;
 			factory.setVertexCoordinates(coords);
			factory.setVertexLabels(edgeLabels);
 			factory.update();
			if (xAxisLabel != null)
				xAxisLabel.setGeometry(factory.getPointSet());
 		}
 	}

 	@Override
 	public void setXAxisLabel(String label) {
 		if (xAxisLabelStr == null || !label.equals(xAxisLabelStr))
 		{
 			xAxisLabelStr = label;
 			createXAxisLabelGeom();
 		}	
 	}
 
 	@Override
 	public void setXAxisOffset(double offset) {
 		xOffset = offset;
 	}
 
 
 	@Override
 	public void setXAxisValues(AxisValues axis, int numOfDataSets) {
 		xAxisValues = axis;
 
 	}
 
	protected void createYAxisLabelGeom() {
		if (yAxisLabelStr != null) {
 			double[] sizes = determineXYsize();
 			PointSetFactory factory = new PointSetFactory();
 			factory.setVertexCount(1);
 			double [][] coords = ArrayPoolUtility.getDoubleArray(1);
 			String [] edgeLabels = new String[1];
 			edgeLabels[0] = yAxisLabelStr;
 			coords[0][0] = -0.5;
 			coords[0][1] = sizes[1] * 0.5;
 			coords[0][2] = 0;
 			factory.setVertexCoordinates(coords);
			factory.setVertexLabels(edgeLabels);
 			factory.update();
			if(yAxisLabel != null)
				yAxisLabel.setGeometry(factory.getPointSet());
 		}
 	}
 	
 	@Override
 	public void setYAxisLabel(String label) {
 		if (yAxisLabelStr == null || !label.equals(yAxisLabelStr))
 		{
 			yAxisLabelStr = label;
 			createYAxisLabelGeom();
 		}
 	}
 
 	@Override
 	public void setYAxisOffset(double offset) {
 		yOffset = offset;
 	}
 
 	@Override
 	public void setYAxisValues(AxisValues axis) {
 		yAxisValues = axis;
 
 	}
 
 	@Override
 	public void setZAxisLabel(String label) {
 		// Nothing to do
 	}
 
 	@Override
 	public void setZAxisOffset(double offset) {
 		// Nothing to do
 	}
 
 	private void cleanUpOverlay()
 	{
 		if (consumer != null) {
 			consumer.removePrimitives();
 			consumer = null;
 		}
 // this causes a deadlock unless the consumer ends a block but it's safe(?) to just
 // not bother with the wait
 //			while (overlayInOperation)
 //			{
 //				
 //				try {
 //					Thread.sleep(50);
 //				} catch (InterruptedException e) {
 //					e.printStackTrace();
 //				}
 //			}
 		overlayInOperation = false;
 		Integer[] indices = new Integer[prim2DMap.keySet().size()];
 		prim2DMap.keySet().toArray(indices);
 		for (int i = 0; i < indices.length; i++)
 			unregisterPrimitive(indices[i]);
 		prim2DMap.clear();
 	}
 	
 	@Override
 	public void updateGraph(IDataset newData) {
 		maxWidth = maxHeight = 0;
 		int width = newData.getShape()[1];
 		int height = newData.getShape()[0];
 		maxWidth = Math.max(width,maxWidth);
 		maxHeight = Math.max(height,maxHeight);
         double[] sizes = determineXYsize();
         LinkedList<SceneGraphComponent> nodesToAdd = new LinkedList<SceneGraphComponent>();
 	    subGraphs.get(subGraphs.size()-1).removeChild(overlayPrimitiveGroupNode); 
         Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
         updateGraphNodes(newData, graphIter, nodesToAdd, sizes[0], sizes[1], height, width);
 		// remove no longer used subgraphs
 		LinkedList<SceneGraphComponent> nodesToBeDeleted = new LinkedList<SceneGraphComponent>();
 		while (graphIter.hasNext()) {
 			SceneGraphComponent subGraph = graphIter.next();
 			nodesToBeDeleted.add(subGraph);
 			subGraph.setGeometry(null);
 			graphGroupNode.removeChild(subGraph);
 			textures.remove(subGraph.getAppearance());
 			softwareImageRGBAdatas.remove(subGraph.getAppearance());
 			imageDatas.remove(subGraph.getAppearance());
 			graphApps.remove(subGraph.getAppearance());
 		}
 		subGraphs.removeAll(nodesToBeDeleted);
 		subGraphs.addAll(nodesToAdd);
 		nodesToBeDeleted.clear();
 		nodesToAdd.clear();        
 	    subGraphs.get(subGraphs.size()-1).addChild(overlayPrimitiveGroupNode); 
 
 		axis.setGeometry(createAxisGeometry(sizes[0],sizes[1]));
 		determineRanges(newData);
 
 		updateClipPlanePositions(sizes);
         posTool.setImageHeight(maxHeight);
         posTool.setImageWidth(maxWidth);
 		posTool.setMaxXY(sizes[0],sizes[1]);
 		panTool.setDataDimension(sizes[0] , sizes[1]);
 		if (xTickNode != null) 
 			xTickNode.setGeometry(createXTicksGeometry());
 		if (yTickNode != null)
 			yTickNode.setGeometry(createYTicksGeometry());
 		if (xLabelNode != null)
 			xLabelNode.setGeometry(createXLabelsGeometry());
 		if (yLabelNode != null)
 			yLabelNode.setGeometry(createYLabelsGeometry());
 		if (titleLabel != null)
 			titleLabel.setGeometry(createTitleGeometry());		
 		createYAxisLabelGeom();
 		createXAxisLabelGeom();		
 	}
 
 	private void updateGraphNodes(IDataset currentData,
 									Iterator<SceneGraphComponent> graphIter,
 									List<SceneGraphComponent> nodesToAdd,
 									double xSize, 
 									double ySize, 
 									int height, 
 									int width) {
 		
 		int maxDimH = JOGLChecker.getMaxTextureHeight();
 		int maxDimW = JOGLChecker.getMaxTextureWidth();
 		double yStart = 0.0;
 		double ySubSize = ((double)maxDimH/(double)maxHeight) < 1.0 ?
 						ySize*maxDimH/maxHeight : ySize; 
 		double xSubSize = ((double)maxDimW/(double)maxWidth) < 1.0 ?
 				xSize*maxDimW/maxWidth : xSize; 				
 		
 		for (int y = 0; y < height; y+= maxDimH, yStart+=ySubSize) {
 			double xStart = 0.0;
 			for (int x = 0; x < width; x+= maxDimW, xStart+=xSubSize) {
 				SceneGraphComponent subGraph = null;
 				if (graphIter.hasNext())
 					 subGraph = graphIter.next();
 				else {
 					subGraph =
 						SceneGraphUtility.createFullSceneGraphComponent(GRAPHNODENAME+numGraphs+".sub"+x+"_"+y);
 
 					//subGraphs.add(subGraph);
 					nodesToAdd.add(subGraph);
 					graphGroupNode.addChild(subGraph);	
 					graphAppearance = createAppearance();
 					subGraph.setAppearance(graphAppearance);
 					setShaderOnAppearance(graphAppearance);							
 				}		
 				subGraph.setGeometry(createGraphGeometry((xStart+xSubSize > xSize ? xSize-xStart : xSubSize),
 														 (yStart+ySubSize > ySize ? ySize-yStart : ySubSize),
 														  xStart,yStart));
 				int texWidth = (width > maxDimW ? maxDimW : width);
 				int texHeight = (height > maxDimH ? maxDimH : height);
 				if (x+texWidth > maxWidth) texWidth = maxWidth-x;
 				if (y+texHeight > maxHeight) texHeight = maxHeight-y;						
 				generateTexture(currentData, subGraph.getAppearance(), x, y, texWidth, texHeight);
 			}
 		}
 	}
 	
 	@Override
 	public void updateGraph(List<IDataset> datasets) {
 		if (datasets.size() > 0 && subGraphs.size() > 0)
 		{
 			Iterator<IDataset> iter = datasets.iterator();	
 		    subGraphs.get(subGraphs.size()-1).removeChild(overlayPrimitiveGroupNode);
 			Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
 			maxWidth = 0;
 			maxHeight = 0;
 			LinkedList<SceneGraphComponent> nodesToAdd = new LinkedList<SceneGraphComponent>(); 
 			while (iter.hasNext())
 			{
 				IDataset currentData = iter.next();
 			
 				int width = currentData.getShape()[1];
 				int height = currentData.getShape()[0];
 				maxWidth = Math.max(width,maxWidth);
 				maxHeight = Math.max(height,maxHeight);
 				double xSize,ySize;
 				if (!useCanvasAspect) {
 					if (width >= height)
 					{
 						xSize = MAXX;
 						ySize = (MAXX * height)/width;
 					} else {
 						ySize = MAXX;
 						xSize = (MAXX * width)/height;
 					}
 				} else {
 					if (canvasWidth >= canvasHeight)
 					{
 						xSize = MAXX;
 						ySize = (MAXX * canvasHeight)/canvasWidth;
 					} else {
 						xSize = MAXX;
 						ySize = (MAXX * canvasWidth)/canvasHeight;
 					}					
 				}
 				xSize = Math.max(xSize, 3.0);
 				ySize = Math.max(ySize, 3.0);
 				updateGraphNodes(currentData,graphIter,nodesToAdd,xSize,ySize,height,width);
 			}
 			// remove no longer used subgraphs
 			LinkedList<SceneGraphComponent> nodesToBeDeleted = new LinkedList<SceneGraphComponent>();
 			while (graphIter.hasNext()) {
 				SceneGraphComponent subGraph = graphIter.next();
 				nodesToBeDeleted.add(subGraph);
 				subGraph.setGeometry(null);
 				graphGroupNode.removeChild(subGraph);
 				textures.remove(subGraph.getAppearance());
 				overlayTextures.remove(subGraph.getAppearance());
 				softwareImageRGBAdatas.remove(subGraph.getAppearance());
 				imageDatas.remove(subGraph.getAppearance());
 				graphApps.remove(subGraph.getAppearance());
 			}
 			subGraphs.removeAll(nodesToBeDeleted);
 			subGraphs.addAll(nodesToAdd);
 		    subGraphs.get(subGraphs.size()-1).addChild(overlayPrimitiveGroupNode);			
 			nodesToBeDeleted.clear();
 			nodesToAdd.clear();
 			posTool.setImageHeight(maxHeight);
 			posTool.setImageWidth(maxWidth);
 			double[] sizes = determineXYsize();
 			updateClipPlanePositions(sizes);
 			posTool.setMaxXY(sizes[0],sizes[1]);
 			panTool.setDataDimension(sizes[0], sizes[1]);
 			determineRanges(datasets);
 			if (xTickNode != null) 
 				xTickNode.setGeometry(createXTicksGeometry());
 			if (yTickNode != null)
 				yTickNode.setGeometry(createYTicksGeometry());
 			if (xLabelNode != null)
 				xLabelNode.setGeometry(createXLabelsGeometry());
 			if (yLabelNode != null)
 				yLabelNode.setGeometry(createYLabelsGeometry());
 			if (titleLabel != null)
 				titleLabel.setGeometry(createTitleGeometry());			
 			axis.setGeometry(createAxisGeometry(sizes[0],sizes[1]));
 			createYAxisLabelGeom();
 			createXAxisLabelGeom();
 		} else {
 			logger.warn("Tried to plot altough there is either no data or no graph nodes");	
 		}
 	}
 
 	@Override
 	public void setXAxisLabelMode(TickFormatting newFormat) {
 		xLabelMode = newFormat;
 		if (xLabelNode != null)
 			xLabelNode.setGeometry(createXLabelsGeometry());
 	}
 
 	@Override
 	public void setYAxisLabelMode(TickFormatting newFormat) {
 		yLabelMode = newFormat;
 		if (yLabelNode != null)
 			yLabelNode.setGeometry(createYLabelsGeometry());
 	}
 
 	@Override
 	public void setZAxisLabelMode(TickFormatting newFormat) {
 		// Nothing to do		
 	}
 
 	@Override
 	public void cleanUpGraphNode() {
 		if (graph != null)
 		{
 			if (graphGroupNode != null)
 			{
 				Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
 				while (graphIter.hasNext())
 				{
 					graphGroupNode.removeChild(graphIter.next());
 				}
 				subGraphs.clear();
 			}
 			if (leftClip != null) graph.removeChild(leftClip);
 			if (background != null) graph.removeChild(background);
 			cleanUpOverlay();
 		}
 		if (axis != null)
 		{
 			axis.removeChild(xAxisLabel);
 			axis.removeChild(yAxisLabel);
 			axis.removeChild(titleLabel);
 		}
 		if (graph != null) {
 			graph.removeTool(actionTool);
 			 ((SceneGraphComponent)graph.getOwner()).removeTool(posTool);			
 		}
 		softwareImageRGBAdata = null;
 		actionListeners.clear();
 	}
 
 	@Override
 	public boolean begin(OverlayType type) {
 		if (!overlayInOperation){
 			overlayInOperation = true;
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized void end(OverlayType type) {
 		overlayInOperation = false;
 		switch(type) {
 			case IMAGE:
 			{
 				if (overlayImage != null && overlayImage.isDirty()) {
 					Iterator<Appearance> iter = graphApps.iterator();
 					while (iter.hasNext())
 					{
 						Appearance ap = iter.next();
 						generateOverlayTexture(ap);
 					}
 				}
 			}
 			break;
 			case VECTOR2D:
 			{
 				Iterator<Integer> iter = prim2DMap.keySet().iterator();
 				while (iter.hasNext())
 				{
 					OverlayPrimitive prim = prim2DMap.get(iter.next());
 					prim.updateNode();
 					if (prim.isFixedSize())
 						updateScaledPrimitive(prim, 1.0 / inverseScaling);
 				}
 			}
 			break;
 			case THREED:
 			{
 			}
 			break;
 		}
 		refresh();
 	}
 
 	/**
 	 * Register an overlay consumer to this overlay provider
 	 * @param consumer
 	 */
 	
 	public void registerOverlay(Overlay2DConsumer consumer)
 	{
 		this.consumer = consumer;
 		posTool.addImagePositionListener(consumer);
 		consumer.registerProvider(this);
 	}
 	
 	/**
 	 * Unregister an overlay consumer to this overlay provider
 	 * @param consumer
 	 */
 	public void unRegisterOverlay(Overlay2DConsumer consumer)
 	{
 		posTool.removeImagePositionListener(consumer);
 		consumer.unregisterProvider();
 		final Iterator<Integer> iter = prim2DMap.keySet().iterator();
 		while (iter.hasNext())
 		{
 			OverlayPrimitive primitive = prim2DMap.get(iter.next());
 			if (primitive != null) {
 				if (overlayPrimitiveGroupNode != null) {
 					overlayPrimitiveGroupNode.removeChild(primitive.getNode());
 				}	
 			}
 		}
 		prim2DMap.clear();		
 		overlayInOperation = false;
 	}
 
 
 	@Override
 	public void drawLine(int primID, double sx, double sy, double ex, double ey) {
 		if (overlayInOperation)
 		{
 			LinePrimitive linePrim = (LinePrimitive)prim2DMap.get(primID);
 			if (linePrim != null){
 				double[] sizes = determineXYsize();
 				double deltaX = sizes[0]/(maxWidth*2.0);
 				double deltaY = sizes[1]/(maxHeight*2.0);
 				double xPos = (sx / maxWidth) * sizes[0] + deltaX; 
 				double yPos = sizes[1] - (sy / maxHeight) * sizes[1] - deltaY;
 				double x1Pos = (ex / maxWidth) * sizes[0] + deltaX;
 				double y1Pos = sizes[1] - (ey / maxHeight) * sizes[1] - deltaY;
 				linePrim.setLinePoints(xPos, yPos, x1Pos, y1Pos);
 			}
 		}
 	}
 
 	@Override
 	public void drawArrow(int primID, double sx, double sy, double ex, double ey) {
 
 		if (overlayInOperation)
 		{
 			ArrowPrimitive arrowPrim = (ArrowPrimitive)prim2DMap.get(primID);
 			if (arrowPrim != null) {
 				double[] sizes = determineXYsize();
 				double deltaX = sizes[0]/(maxWidth*2.0);
 				double deltaY = sizes[1]/(maxHeight*2.0);
 				double xPos = (sx / maxWidth) * sizes[0] + deltaX; 
 				double yPos = sizes[1] - (sy / maxHeight) * sizes[1] - deltaY;
 				double x1Pos = (ex / maxWidth) * sizes[0] + deltaX;
 				double y1Pos = sizes[1] - (ey / maxHeight) * sizes[1] - deltaY;
 				arrowPrim.setArrowPoints(xPos, yPos, x1Pos, y1Pos,1.0);
 			}
 		}
 	}
 
 	@Override
 	public void drawArrow(int primID, double sx, double sy, double ex, double ey, double arrowPos) {
 		if (overlayInOperation)
 		{
 			ArrowPrimitive arrowPrim = (ArrowPrimitive)prim2DMap.get(primID);
 			if (arrowPrim != null) {
 				double[] sizes = determineXYsize();
 				double deltaX = sizes[0]/(maxWidth*2.0);
 				double deltaY = sizes[1]/(maxHeight*2.0);				
 				double xPos = (sx / maxWidth) * sizes[0] + deltaX; 
 				double yPos = sizes[1] - (sy / maxHeight) * sizes[1] - deltaY;
 				double x1Pos = (ex / maxWidth) * sizes[0] + deltaX;
 				double y1Pos = sizes[1] - (ey / maxHeight) * sizes[1] - deltaY;
 				arrowPrim.setArrowPoints(xPos, yPos, x1Pos, y1Pos,arrowPos);
 			}
 		}
 	}
 	
 	@Override
 	public void drawBox(int primID, double lux, double luy, double rlx, double rly) {
 		if (overlayInOperation)
 		{
 			BoxPrimitive boxPrim = (BoxPrimitive)prim2DMap.get(primID);
 			if (boxPrim != null) {
 				double[] sizes = determineXYsize();
 				double deltaX = sizes[0]/(maxWidth*2.0);
 				double deltaY = sizes[1]/(maxHeight*2.0);				
 				double xPos = (lux / maxWidth) * sizes[0] + deltaX;
 				double yPos = sizes[1] - (luy / maxHeight) * sizes[1] - deltaY;
 				double x1Pos = (rlx / maxWidth) * sizes[0] + deltaX;
 				double y1Pos = sizes[1] - (rly / maxHeight) * sizes[1] - deltaY;
 				boxPrim.setBoxPoints(xPos,yPos,x1Pos,y1Pos);
 			}
 		}
 	}
 
 	@Override
 	public void drawCircle(int primID, double cx, double cy, double radius) {
 		if (overlayInOperation) {
 			CirclePrimitive circPrim = (CirclePrimitive)prim2DMap.get(primID);
 			if (circPrim != null) {
 				double[] sizes = determineXYsize();
 				double xPos = (cx / maxWidth) * sizes[0]; 
 				double yPos = sizes[1] - (cy / maxHeight) * sizes[1];
 				double rad = (radius / Math.sqrt(maxWidth*maxWidth + maxHeight * maxHeight)) * Math.sqrt(sizes[0] * sizes[0] + sizes[1] * sizes[1]);
 				circPrim.setCircleParameters(xPos, yPos, rad);
 			}
 		}
 	}
 
 	@Override
 	public void drawSector(int primID, double cx, double cy, double inRadius,
 			                  double outRadius, double startAngle, double endAngle) {
 		if (overlayInOperation) {
 			CircleSectorPrimitive circPrim = (CircleSectorPrimitive)prim2DMap.get(primID);
 			if (circPrim != null) {
 				double[] sizes = determineXYsize();
 				double xPos = (cx / maxWidth) * sizes[0]; 
 				double yPos = sizes[1] - (cy / maxHeight) * sizes[1];
 				double rad = (inRadius / Math.sqrt(maxWidth*maxWidth + maxHeight * maxHeight)) * Math.sqrt(sizes[0] * sizes[0] + sizes[1] * sizes[1]);
 				double rad2 = (outRadius / Math.sqrt(maxWidth*maxWidth + maxHeight * maxHeight)) * Math.sqrt(sizes[0] * sizes[0] + sizes[1] * sizes[1]);
 				circPrim.setSectorParameters(xPos, yPos, rad2, rad, startAngle, endAngle);
 			}
 		}
 	}	
 	
 	@Override
 	public void drawTriangle(int primID, double x1, double y1, double x2, double y2,
 			double x3, double y3) {
 
 		if (overlayInOperation) {
 			TrianglePrimitive triPrim = (TrianglePrimitive)prim2DMap.get(primID);
 			if (triPrim != null) {
 				double[] sizes = determineXYsize();
 				double xPos = (x1 / maxWidth) * sizes[0]; 
 				double yPos = sizes[1] - (y1 / maxHeight) * sizes[1];
 				double x1Pos = (x2 / maxWidth) * sizes[0];
 				double y1Pos = sizes[1] - (y2 / maxHeight) * sizes[1];
 				double x2Pos = (x3 / maxWidth) * sizes[0];
 				double y2Pos = sizes[1] - (y3 / maxHeight) * sizes[1];
 				triPrim.setTriangleCoords(xPos, yPos, x1Pos, y1Pos, x2Pos, y2Pos);
 			}
 		}
 	}	
 	
 	@Override
 	public synchronized int registerPrimitive(PrimitiveType primType) {
 		return registerPrimitive(primType,false);
 	}
 
 	@Override
 	public void setColour(int primID, Color colour) {
 		if (overlayInOperation)
 		{
 			OverlayPrimitive prim = prim2DMap.get(primID);
 			if (prim != null)
 				prim.setColour(colour);
 		}
 	}
 
 	@Override
 	public synchronized void unregisterPrimitive(int primID) {
 		OverlayPrimitive primitive = prim2DMap.remove(primID);
 		if (primitive != null) {
 			if (overlayPrimitiveGroupNode != null) {
 				overlayPrimitiveGroupNode.removeChild(primitive.getNode());
 			}
 		}
 		if (!overlayInOperation) refresh();
 	}
 
 	@Override
 	public synchronized void unregisterPrimitive(List<Integer> ids) {
 		Iterator<Integer> iter = ids.iterator();
 		while (iter.hasNext()) {
 			OverlayPrimitive primitive = prim2DMap.remove(iter.next());
 			if (primitive != null) {
 				if (overlayPrimitiveGroupNode != null) {
 					overlayPrimitiveGroupNode.removeChild(primitive.getNode());
 				}
 			}
 		}
 		if (!overlayInOperation) refresh();
 	}		
 	
 	@Override
 	public void setStyle(int primID, VectorOverlayStyles newStyle) {
 		OverlayPrimitive primitive = prim2DMap.get(primID);
 		if (primitive != null) {
 			primitive.setStyle(newStyle);
 		}
 	}
 
 	@Override
 	public void setOutlineColour(int primID, Color colour) {
 		OverlayPrimitive primitive = prim2DMap.get(primID);
 		if (primitive != null) {
 			primitive.setOutlineColour(colour);
 		}
 	}
 
 	@Override
 	public void translatePrimitive(int primID, double tx, double ty) {
 		if (overlayInOperation) {
 			OverlayPrimitive primitive = prim2DMap.get(primID);
 			if (primitive != null) {
 				double[] translation = new double[2];
 				double[] sizes = determineXYsize();
 				translation[0] = (tx / maxWidth) * sizes[0]; 
 				translation[1] = - (ty / maxHeight) * sizes[1];
 				primitive.translate(translation);
 			}
 		}
 	}
 
 	@Override
 	public void rotatePrimitive(int primID, double angle, double rcx, double rcy) {
 		if (overlayInOperation) {
 			OverlayPrimitive primitive = prim2DMap.get(primID);
 			if (primitive != null) {
 				double[] rotCenter = new double[2];
 				double[] sizes = determineXYsize();
 				rotCenter[0] = (rcx / maxWidth) * sizes[0]; 
 				rotCenter[1] = sizes[1] - (rcy / maxHeight) * sizes[1];
 				primitive.rotate(angle, rotCenter);
 			}
 		}
 	}
 
 	@Override
 	public boolean setTransparency(int primID, double transparency) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			OverlayPrimitive primitive = prim2DMap.get(primID);
 			if (primitive != null) {
 				primitive.setTransparency(transparency);
 				returnValue = true;
 			}
 		}
 		return returnValue;
 	}
 
 	protected PointSet createTitleGeometry() {
 		PointSetFactory factory = new PointSetFactory();
 		factory.setVertexCount(1);
 		double[] coords = new double[3];
 		String[] edgeLabels = new String[1];
 		if (titleLabelStr != null)
 			edgeLabels[0] = titleLabelStr;
 		else
 			edgeLabels[0] = "";
 		coords[0] = 0.5;
 		coords[1] = determineXYsize()[1];
 		coords[2] = 0;
 		factory.setVertexCoordinates(coords);
 		factory.setVertexLabels(edgeLabels);
 		factory.update();
 		return factory.getPointSet();
 	}
 	@Override
 	public void setTitle(String title) {
 		if (titleLabelStr == null || !title.equals(titleLabelStr)) {
 			titleLabelStr = title;
 			if(titleLabel != null)
 				titleLabel.setGeometry(createTitleGeometry());
 		}	
 	}
 
 	@Override
 	public void setTickGridLinesActive(boolean xcoord, boolean ycoord,
 			boolean zcoord) {
 		// Nothing to do
 		
 	}
 
 	@Override
 	public void setZAxisValues(AxisValues axis) {
 		// Nothing to do
 		
 	}
 
 	@Override
 	public void setLineThickness(int primID, double thickness) {
 		OverlayPrimitive primitive = prim2DMap.get(primID);
 		if (primitive != null) {
 			primitive.setLineThickness(thickness);
 		}
 		
 	}
 
 	@Override
 	public boolean setOutlineTransparency(int primID, double transparency) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			OverlayPrimitive primitive = prim2DMap.get(primID);
 			if (primitive != null) {
 				primitive.setOutlineTransparency(transparency);
 				returnValue = true;
 			}
 		}
 		return returnValue;
 	}
 
 	@Override
 	public boolean setPrimitiveVisible(int primID, boolean visible) {
 		boolean returnValue = false;
 		OverlayPrimitive primitive = prim2DMap.get(primID);
 		if (primitive != null) {
 			if (visible) {
 				if (primitive.isHidden() && 
 					(overlayPrimitiveGroupNode != null)) {
 					overlayPrimitiveGroupNode.addChild(primitive.getNode());
 					returnValue = true;
 					primitive.unhide();
 				}
 			} else {
 				if (!primitive.isHidden() &&
 					(overlayPrimitiveGroupNode != null)) {
 					overlayPrimitiveGroupNode.removeChild(primitive.getNode());
 					primitive.hide();
 					returnValue = true;
 				}
 			}
 		}
 		return returnValue;
 	}
 
 	@Override
 	public void resetView() {
 		Camera sceneCamera = CameraUtility.getCamera(app.getCurrentViewer());
         sceneCamera.setFieldOfView(52.5f);
 		labelScaling = 1.0;
 		if (xLabelNode != null)
 			xLabelNode.setGeometry(createXLabelsGeometry());
 	}
 
 	private void updateScaledPrimitive(OverlayPrimitive primitive, 
 									   double scale) {
 		double[] anchorPos = primitive.getAnchorPoint();
 		double xPos = (anchorPos[0] * scale) - anchorPos[0];
 		double yPos = (anchorPos[1] * scale) - anchorPos[1];
 		if (primitive.getTransformationMatrix() != null)
 			MatrixBuilder.euclidean().scale(inverseScaling).translate(xPos,yPos,0.0).times(primitive.getTransformationMatrix()).assignTo(primitive.getNode());
 		else
 			MatrixBuilder.euclidean().scale(inverseScaling).translate(xPos,yPos,0.0).assignTo(primitive.getNode());		
 	}
 	
 	@Override
 	public void transformationMatrixChanged(TransformationEvent evt) {
 		double[] transMatrix = evt.getTransformationMatrix();
 		double[] sizes = determineXYsize();
 		if (sizes[0] * transMatrix[0] < MINX) {
 			labelScaling = sizes[0] * transMatrix[0] / MINX;
 			if (xLabelNode != null)
 				xLabelNode.setGeometry(createXLabelsGeometry());
 		}
 		inverseScaling = 1.0 / transMatrix[0];
 		double scale = transMatrix[0];
 		final Iterator<Integer> iter = prim2DMap.keySet().iterator();
 		while (iter.hasNext())
 		{
 			OverlayPrimitive primitive = prim2DMap.get(iter.next());
 			if (primitive != null && primitive.isFixedSize()) {
 				updateScaledPrimitive(primitive, scale);
 			}
 		}
 	}
 
 	@Override
 	public synchronized int registerPrimitive(PrimitiveType primType, boolean fixedSize) {
 		if (subGraphs.size() != 0)
 		{
 			primKeyID++;
 			while (prim2DMap.containsKey(primKeyID)) 
 			{
 				primKeyID = (primKeyID + 1)%Integer.MAX_VALUE;
 			}
 			OverlayPrimitive prim = null;
 			SceneGraphComponent comp  = 
 				SceneGraphUtility.createFullSceneGraphComponent(OVERLAYPREFIX+primKeyID);
 			overlayPrimitiveGroupNode.addChild(comp);
 			switch(primType)
 			{
 				case LINE:
 					prim = new LinePrimitive(comp,fixedSize);
 				break;
 				case BOX:
 					prim = new BoxPrimitive(comp,fixedSize);
 				break;
 				case ARROW:
 					prim = new ArrowPrimitive(comp,fixedSize);
 				break;
 				case CIRCLE:
 					prim = new CirclePrimitive(comp,fixedSize);
 				break;
 				case TRIANGLE:
 					prim = new TrianglePrimitive(comp,fixedSize);
 				break;
 				case SECTOR:
 					prim = new CircleSectorPrimitive(comp,fixedSize);
 				break;
 				case LABEL:
 					prim = new LabelPrimitive(comp,fixedSize);
 				break;
 				case POINT:
 					prim = new PointPrimitive(comp,fixedSize);
 				break;
 				case POINTLIST:
 					prim = new PointListPrimitive(comp,fixedSize);
 				break;
 				case RING:
 					prim = new RingPrimitive(comp, fixedSize);
 				break;
 				case ELLIPSE:
 					prim = new EllipsePrimitive(comp,fixedSize);
 				break;
 				default:
 				break;
 			}
 			prim2DMap.put(primKeyID, prim);
 			return primKeyID;
 		}
 		return -1;
 	}
 
 	@Override
 	public void setAnchorPoints(int primID, double x, double y) {
 		OverlayPrimitive primitive = prim2DMap.get(primID);
 		if (primitive != null && primitive.isFixedSize()) {
 			double[] sizes = determineXYsize();
 			double xPos = (x / maxWidth) * sizes[0]; 
 			double yPos = sizes[1] - (y / maxHeight) * sizes[1];
 			primitive.setAnchorPoint(xPos, yPos);
 		}
 	}
 	
 	/**
 	 * Add a PlotActionEventListener to the listener list
 	 * 
 	 * @param listener
 	 *            another PlotActionEventListener
 	 */
 	public void addPlotActionEventListener(PlotActionEventListener listener) {
 		actionListeners.add(listener);
 	}
 
 	/**
 	 * Remove a PlotActionEventListener from the listener list
 	 * 
 	 * @param listener
 	 *            the PlotActionEventListener that should be removed
 	 */
 	public void removePlotActionEventListener(PlotActionEventListener listener) {
 		actionListeners.remove(listener);
 	}
 
 	@Override
 	public void plotActionPerformed(PlotActionEvent event) {
 		double[] sizes = determineXYsize();
 		double xPos = event.getPosition()[0]/sizes[0];
 		double yPos = 1.0-event.getPosition()[1]/sizes[1];
 		int dataXpos = 0;
 		int dataYpos = 0;
 		double outXpos = 0;
 		double outYpos = 0;
 		switch (xAxis) {
 			case LINEAR:
 				outXpos = xPos * maxWidth;
 				dataXpos = (int)(outXpos);
 			break;
 			case LINEAR_WITH_OFFSET:
 				outXpos = maxWidth * xPos;
 				dataXpos = (int)(outXpos);
 				outXpos += xOffset;
 			break;
 			case CUSTOM:
 				int pos = (int)(maxWidth * xPos);
 				dataXpos = pos;
 				outXpos = xAxisValues.getValue(pos);
 			break;
 		}
 		
 		switch (yAxis) {
 			case LINEAR:
 				outYpos = maxHeight * yPos;
 				dataYpos = (int)(outYpos);
 			break;
 			case LINEAR_WITH_OFFSET:
 				outYpos = maxHeight * yPos;
 				dataYpos = (int)(outYpos);
 				outYpos += yOffset;
 			break;
 			case CUSTOM:
 				int pos = (int)(maxHeight * yPos);
 				dataYpos = pos;
 				outYpos = yAxisValues.getValue(pos);
 			break;
 		}
 		Iterator<PlotActionEventListener> iter = actionListeners.iterator();
 		double[] dataPos = { outXpos, outYpos };
 		int [] dataSetPos = {dataXpos, dataYpos };
 		PlotActionEvent newEvent = new PlotActionEvent(actionTool, dataPos);
 		newEvent.setDataPosition(dataSetPos);
 		while (iter.hasNext()) {
 			PlotActionEventListener listener = iter.next();
 			listener.plotActionPerformed(newEvent);
 		}		
 	}
 	
 	/**
 	 * Enable/Disable the PlotActionTool
 	 * 
 	 * @param enabled
 	 *            true if tool should be enabled otherwise false
 	 */
 	
 	public void enablePlotActionTool(boolean enabled) {
 		if (graph != null) {
 			if (enabled)
 				graph.addTool(actionTool);
 			else
 				graph.removeTool(actionTool);
 		}
 	}
 
 	@Override
 	public void drawLabel(int primID, double lx, double ly) {
 		if (overlayInOperation) {
 			LabelPrimitive labelPrim = (LabelPrimitive)prim2DMap.get(primID);
 			if (labelPrim != null) {
 				double[] sizes = determineXYsize();
 				double xPos = (lx / maxWidth) * sizes[0]; 
 				double yPos = sizes[1] - (ly / maxHeight) * sizes[1];
 				labelPrim.setLabelPosition(xPos, yPos);
 			}
 		}
 	}
 
 	@Override
 	public boolean setLabelFont(int primID, Font font) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			LabelPrimitive primitive = (LabelPrimitive)prim2DMap.get(primID);
 			if (primitive != null) {
 				primitive.setLabelFont(font);
 				returnValue = true;
 			}
 		}
 		return returnValue;
 	}
 
 	@Override
 	public boolean setLabelOrientation(int primID, LabelOrientation orient) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			LabelPrimitive primitive = (LabelPrimitive)prim2DMap.get(primID);
 			if (primitive != null) {
 				primitive.setLabelDirection(orient);
 				returnValue = true;
 			}
 		}
 		return returnValue;	
 	}
 
 	@Override
 	public boolean setLabelText(int primID, String text, int alignment) {
 		boolean returnValue = false;
 		if (overlayInOperation) {
 			LabelPrimitive primitive = (LabelPrimitive)prim2DMap.get(primID);
 			if (primitive != null) {
 				primitive.setLabelString(text);
 				primitive.setLabelAlignment(alignment);
 				returnValue = true;
 			}
 		}
 		return returnValue;
 	}
 
 	@Override
 	public void drawPoint(int primID, double px, double py) {
 		if (overlayInOperation) {
 			PointPrimitive pointPrim = (PointPrimitive)prim2DMap.get(primID);
 			if (pointPrim != null) {
 				double[] sizes = determineXYsize();
 				double deltaX = sizes[0]/(maxWidth*2.0);
 				double deltaY = sizes[1]/(maxHeight*2.0);								
 				double xPos = (px / maxWidth) * sizes[0] + deltaX; 
 				double yPos = sizes[1] - (py / maxHeight) * sizes[1] - deltaY;
 				pointPrim.setPoint(xPos, yPos);
 			}
 		}
 	}
 
 	@Override
 	public void setThickPoints(int primID, boolean on) {
 		if (overlayInOperation) {
 			PointPrimitive primitive = (PointPrimitive)prim2DMap.get(primID);
 			if (primitive != null) {
 				primitive.setPhat(on);
 			}
 		}
 	}
 
 
 	@Override
 	public void drawPoints(int primID, double[] px, double[] py) {
 		if (overlayInOperation && px.length == py.length) {
 			PointListPrimitive pointPrim = (PointListPrimitive)prim2DMap.get(primID);
 			if (pointPrim != null) {
 				double[] sizes = determineXYsize();
 				double deltaX = sizes[0]/(maxWidth*2.0);
 				double deltaY = sizes[1]/(maxHeight*2.0);								
 				double[] xPos = new double[px.length];
 				double[] yPos = new double[py.length];
 				for (int i = 0; i < px.length; i++) {
 					xPos[i] = (px[i] / maxWidth) * sizes[0] + deltaX; 
 					yPos[i] = sizes[1] - (py[i] / maxHeight) * sizes[1] - deltaY;
 				}
 				pointPrim.setPoints(xPos, yPos);
 			}
 		}
 	}
 
 	@Override
 	public void drawRing(int primID, double cx, double cy, double inRadius, double outRadius) {
 		if (overlayInOperation) {
 			RingPrimitive ringPrim = (RingPrimitive)prim2DMap.get(primID);
 			if (ringPrim != null) {
 				double[] sizes = determineXYsize();
 				double xPos = (cx / maxWidth) * sizes[0]; 
 				double yPos = sizes[1] - (cy / maxHeight) * sizes[1];
 				double rad = (inRadius / Math.sqrt(maxWidth*maxWidth + maxHeight * maxHeight)) * Math.sqrt(sizes[0] * sizes[0] + sizes[1] * sizes[1]);
 				double rad2 = (outRadius / Math.sqrt(maxWidth*maxWidth + maxHeight * maxHeight)) * Math.sqrt(sizes[0] * sizes[0] + sizes[1] * sizes[1]);
 				ringPrim.setRingParameters(xPos, yPos, rad, rad2);
 			}
 		}
 	}
 
 	@Override
 	public void drawEllipse(int primID, double cx, double cy, double a, double b, double omega) {
 		if (overlayInOperation) {
 			EllipsePrimitive ellipsePrim = (EllipsePrimitive)prim2DMap.get(primID);
 			if (ellipsePrim != null) {
 				double[] sizes = determineXYsize();
 				double xPos = (cx / maxWidth) * sizes[0]; 
 				double yPos = sizes[1] - (cy / maxHeight) * sizes[1];
 				double rad = (a / Math.sqrt(maxWidth*maxWidth + maxHeight * maxHeight)) * Math.sqrt(sizes[0] * sizes[0] + sizes[1] * sizes[1]);
 				double rad2 = (b / Math.sqrt(maxWidth*maxWidth + maxHeight * maxHeight)) * Math.sqrt(sizes[0] * sizes[0] + sizes[1] * sizes[1]);
 				ellipsePrim.setEllipseParameters(xPos, yPos, rad, rad2,omega);
 			}
 		}	
 	}
 
 	@Override
 	public ScaleType getScaling() {
 		return currentScale;
 	}
 
 	public void setCanvasAspectRation(boolean useCanvasAspect) {
 		this.useCanvasAspect = useCanvasAspect;
 		double[] sizes = determineXYsize();
 		Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();		
 		while (graphIter.hasNext())
 		{
 			SceneGraphComponent subGraph = graphIter.next();			
 			subGraph.setGeometry(createGraphGeometry(sizes[0],sizes[1],0,0)); // TODO Is this right?
 		}
 		updateClipPlanePositions(sizes);
 		posTool.setMaxXY(sizes[0],sizes[1]);
 		panTool.setDataDimension(sizes[0], sizes[1]);
 		if (xTickNode != null) 
 			xTickNode.setGeometry(createXTicksGeometry());
 		if (yTickNode != null)
 			yTickNode.setGeometry(createYTicksGeometry());
 		if (xLabelNode != null)
 			xLabelNode.setGeometry(createXLabelsGeometry());
 		if (yLabelNode != null)
 			yLabelNode.setGeometry(createYLabelsGeometry());
 		if (titleLabel != null)
 			titleLabel.setGeometry(createTitleGeometry());
 		axis.setGeometry(createAxisGeometry(sizes[0],sizes[1]));
 		createYAxisLabelGeom();
 		createXAxisLabelGeom();		
 	}
 
 	@Override
 	public List<AxisValues> getAxisValues() {
 		return null;
 	}
 
 	@Override
 	public void restoreDefaultPlotAreaCursor() {
 		plotArea.getDisplay().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				plotArea.setCursor(defaultCursor);							
 			}
 		});
 		
 	}
 
 	@Override
 	public void setPlotAreaCursor(final int cursor) {
 		plotArea.getDisplay().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				Cursor tempCursor = plotArea.getDisplay().getSystemCursor(cursor);
 				if (tempCursor != null)
 					plotArea.setCursor(tempCursor);
 			}
 		});		
 	}
 
 	public void setGradientImageMode(boolean checked) {
 		if (currentImageType == 0 && graph != null)
 		{
 			useGradientMode = checked;
 			loadGLSLProgram(tableMin, tableMax);
 			if (checked) {
 				if (tableProg != null) {
 					float[] deltaVec = new float[]{1.0f/maxWidth,1.0f/maxHeight};
 					float threshold = 1.0f;
 					
 					if (tableMax < 100) 
 						threshold = 0.5f;
 					else
 						threshold = 0.1f;
 						
 					tableProg.setUniform("sampler", 0);
 					tableProg.setUniform("tableSampler", 1);
 					tableProg.setUniform("overlaySampler", 2);
 					tableProg.setUniform("maxValue", tableMax);
 					tableProg.setUniform("minValue", tableMin);
 					tableProg.setUniform("delta", deltaVec);		
 					tableProg.setUniform("threshold", threshold);
 				}
 			}
 		} 
 	}
 	
 	public void setDiffractionImageMode(boolean checked) {	
 		if (currentImageType == 0 && graph != null)
 		{
 			useDiffractionMode = checked;
 			loadGLSLProgram(tableMin,tableMax);
 			if (checked) {
 				if (tableProg != null) {
 					tableProg.setUniform("sampler", 0);
 					tableProg.setUniform("tableSampler", 1);
 					tableProg.setUniform("overlaySampler", 2);
 					tableProg.setUniform("threshold", threshold);
 				}						
 			} 
 		}
 	}
 	
 	public void changeThreshold(double amount) {
 		if (useDiffractionMode && tableProg != null) {
 			threshold = amount;
 			tableProg.setUniform("threshold", threshold);
 		}
 	}
 
 	@Override
 	public OverlayObject registerObject(PrimitiveType primType) {
 		int primID = registerPrimitive(primType, false);
 		if (primID != -1) {
 			switch(primType) {
 				case LINE:
 					return new LineObject(primID, this);
 				case ARROW:
 					return new ArrowObject(primID, this);
 				case BOX:
 					return new BoxObject(primID, this);
 				case CIRCLE:
 					return new CircleObject(primID,this);
 				case SECTOR:
 					return new CircleSectorObject(primID, this);
 				case ELLIPSE:
 					return new EllipseObject(primID, this);
 				case LABEL:
 					return new TextLabelObject(primID, this);
 				case TRIANGLE:
 					return new TriangleObject(primID, this);
 				case POINT:
 					return new PointObject(primID, this);
 				case POINTLIST:
 					return new PointListObject(primID, this);
 				case IMAGE:
 					return new ImageObject(primID, this);
 				default:		
 					return null;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public OverlayImage registerOverlayImage(int width, int height) {
 		overlayImage = new OverlayImage(width, height);
 		overlayImage.clear((short)0,(short)0,(short)0,(short)0);
 		
 		if (hasJOGLshaders)
 		{
 			Iterator<Appearance> iter = graphApps.iterator();
 			while (iter.hasNext()) {
 				Appearance ap = iter.next();
 				Texture2D overlayTexture = (Texture2D)
 					AttributeEntityUtility.createAttributeEntity(Texture2D.class,
 														 POLYGON_SHADER+"."+TEXTURE_2D_2,
 														 ap,true);
 				overlayTexture.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
 				overlayTexture.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
 				overlayTexture.setMagFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
 				overlayTexture.setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
 				overlayTexture.setImage(new de.jreality.shader.ImageData(overlayImage.getImageData(),
 																		 overlayImage.getWidth(),
 																		 overlayImage.getHeight()));
 				overlayTextures.put(ap, overlayTexture);
 			}
 			if (tableProg != null)
 				tableProg.setUniform("overlaySampler", 2);
 		} else {
 			
 			//FIXME This still dosen't work, but is better than nothing.
 			Iterator<Appearance> iter = graphApps.iterator();
 			while (iter.hasNext()) {
 				Appearance ap = iter.next();
 				de.jreality.shader.ImageData texImg = new de.jreality.shader.ImageData(overlayImage.getImageData(),
 						 overlayImage.getWidth(),
 						 overlayImage.getHeight());
 				Texture2D overlayTexture = TextureUtility.createTexture(ap, POLYGON_SHADER, texImg);
 				overlayTexture.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
 				overlayTexture.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
 				overlayTexture.setMagFilter(Texture2D.GL_NEAREST);
 				overlayTexture.setMinFilter(Texture2D.GL_NEAREST);
 
 				overlayTextures.put(ap, overlayTexture);
 			}
 			if (tableProg != null)
 				tableProg.setUniform("overlaySampler", 2);			
 		}
 		
 		overlayImage.clean();
 		return overlayImage;
 	}
 
 	@Override
 	public void toggleErrorBars(boolean xcoord, boolean ycoord, boolean zcoord) {
 		// TODO Auto-generated method stub	
 		// not yet implemented
 	}
 
 	@Override
 	public void drawImage(int imageID, Image image, double lux, double luy, double rlx, double rly) {
 		throw new UnsupportedOperationException("Unsupported/implemented for DataSet3DPlot2D, please implement if needed");
 	}
 
 	@Override
 	public boolean isDrawable(double xSize, double ySize) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
