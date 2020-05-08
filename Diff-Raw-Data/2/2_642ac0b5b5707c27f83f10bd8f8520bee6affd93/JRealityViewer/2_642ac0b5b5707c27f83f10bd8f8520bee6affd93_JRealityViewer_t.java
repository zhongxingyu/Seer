 package de.tum.in.jrealityplugin;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 
 import de.jreality.geometry.IndexedFaceSetFactory;
 import de.jreality.geometry.IndexedLineSetFactory;
 import de.jreality.geometry.PointSetFactory;
 import de.jreality.plugin.JRViewer;
 import de.jreality.plugin.basic.Inspector;
 import de.jreality.plugin.basic.PropertiesMenu;
 import de.jreality.plugin.basic.ViewMenuBar;
 import de.jreality.plugin.basic.ViewToolBar;
 import de.jreality.plugin.content.ContentTools;
 import de.jreality.plugin.menu.BackgroundColor;
 import de.jreality.plugin.menu.CameraMenu;
 import de.jreality.plugin.menu.DisplayOptions;
 import de.jreality.plugin.menu.ExportMenu;
 import de.jreality.scene.Camera;
 import de.jreality.scene.SceneGraphComponent;
 import de.jreality.scene.data.Attribute;
 import de.jreality.scene.data.IntArray;
 import de.jreality.shader.DefaultGeometryShader;
 import de.jreality.shader.ShaderUtility;
 import de.jreality.util.CameraUtility;
 import de.jreality.util.SceneGraphUtility;
 
 @SuppressWarnings("deprecation")
 public class JRealityViewer implements Cindy3DViewer {
 	private JFrame frame;
 	private JRViewer viewer;
 	
 	private Camera camera;
 
 	private SceneGraphComponent sceneRoot;
 
 	// Point resources
 	private SceneGraphComponent scenePoints;
 	private PointSetFactory psf;
 
 	private ArrayList<double[]> pointCoordinates;
 	private ArrayList<Color> pointColors;
 	private ArrayList<Double> pointSizes;
 	
 	// Circle resources
 	private SceneGraphComponent sceneCircles;
 	private PointSetFactory psf2;
 	private ArrayList<double[]> circleCenters;
 	private ArrayList<double[]> circleNormals;
 	private ArrayList<Double> circleRadii;
 	private ArrayList<Color> circleColors;
 
 	// Line resources
 	private SceneGraphComponent sceneLines;
 	private IndexedLineSetFactory ilsf;
 
 	private ArrayList<double[]> lineCoordinates;
 	private ArrayList<Integer> lineIndices;
 	private ArrayList<Double> lineSizes;
 	private ArrayList<Color> lineColors;
 	private ArrayList<Integer> lineTypes;
 	
 	// Polygon resources
 	private SceneGraphComponent scenePolygons;
 	private IndexedFaceSetFactory ifsf;
 	
 	private ArrayList<double[][]> polygonVertices;
 	private ArrayList<Color> polygonColors;
 	private int polygonTotalVertexCount;
 
 	public JRealityViewer() {
 		psf = new PointSetFactory();
 		pointCoordinates = new ArrayList<double[]>();
 		pointColors = new ArrayList<Color>();
 		pointSizes = new ArrayList<Double>();
 		
 		psf2 = new PointSetFactory();
 		circleCenters = new ArrayList<double[]>();
 		circleNormals = new ArrayList<double[]>();
 		circleRadii = new ArrayList<Double>();
 		circleColors = new ArrayList<Color>();
 
 		ilsf = new IndexedLineSetFactory();
 		lineCoordinates = new ArrayList<double[]>();
 		lineIndices = new ArrayList<Integer>();
 		lineSizes = new ArrayList<Double>();
 		lineColors = new ArrayList<Color>();
 		lineTypes = new ArrayList<Integer>();
 		
 		ifsf = new IndexedFaceSetFactory();
 		polygonVertices = new ArrayList<double[][]>();
 		polygonColors = new ArrayList<Color>();
 		polygonTotalVertexCount = 0;
 
 		sceneRoot = new SceneGraphComponent("root");
 		
 		// TODO: Set custom appearances for these components
 		scenePoints = SceneGraphUtility.createFullSceneGraphComponent("points");
 		scenePoints.setGeometry(psf.getGeometry());
 		DefaultGeometryShader dgs =
 			ShaderUtility.createDefaultGeometryShader(scenePoints.getAppearance(), true);
 		dgs.createPointShader("my");
 		
 		sceneCircles = SceneGraphUtility.createFullSceneGraphComponent("circles");
 		sceneCircles.setGeometry(psf2.getGeometry());
		dgs = ShaderUtility.createDefaultGeometryShader(sceneCircles.getAppearance(), true);
		dgs.createPointShader("circle");
 
 		sceneLines = SceneGraphUtility.createFullSceneGraphComponent("lines");
 		sceneLines.setGeometry(ilsf.getGeometry());
 		dgs = ShaderUtility.createDefaultGeometryShader(sceneLines.getAppearance(), true);
 		dgs.createPointShader("my");
 		dgs.createLineShader("my");
 		
 		scenePolygons = new SceneGraphComponent("polygons");
 		scenePolygons.setGeometry(ifsf.getGeometry());
 
 		sceneRoot.addChild(scenePoints);
 		sceneRoot.addChild(sceneCircles);
 		sceneRoot.addChild(sceneLines);
 		sceneRoot.addChild(scenePolygons);
 		
 		viewer = new JRViewer();
 		viewer.setContent(sceneRoot);
 		//viewer.registerPlugin(new DirectContent());
 		viewer.registerPlugin(new ContentTools());
 		//viewer.registerPlugin(new ContentLoader());
 		//viewer.addBasicUI();
 		
 		// All plugins from as in BasicUI except shell 
 		viewer.registerPlugin(new Inspector());
 		//viewer.registerPlugin(new Shell());
 		
 		viewer.registerPlugin(new BackgroundColor());
 		viewer.registerPlugin(new DisplayOptions());
 		viewer.registerPlugin(new ViewMenuBar());
 		viewer.registerPlugin(new ViewToolBar());
 		
 		viewer.registerPlugin(new ExportMenu());
 		viewer.registerPlugin(new CameraMenu());
 		viewer.registerPlugin(new PropertiesMenu());
 		//
 		
 		viewer.setShowPanelSlots(false, true, false, false);
 		
 		frame = new JFrame("Cindy3D");
 		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 		frame.setLayout(new BorderLayout());
 		frame.add(viewer.startupLocal(), BorderLayout.CENTER);
 		frame.pack();
 
 		// Set camera near and far plane
 		camera = CameraUtility.getCamera(viewer.getViewer());
 		camera.setNear(0.1);
 		camera.setFar(1000.0);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.jrealityplugin.Cindy3DViewer#begin()
 	 */
 	@Override
 	public void begin() {
 		clearPoints();
 		clearCircles();
 		clearLines();
 		clearPolygons();
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.jrealityplugin.Cindy3DViewer#end()
 	 */
 	@Override
 	public void end() {
 		updatePoints();
 		updateCircles();
 		updateLines();
 		updatePolygons();
 
 		if (!frame.isVisible())	frame.setVisible(true);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.jrealityplugin.Cindy3DViewer#addPoint(double, double, double)
 	 */
 	@Override
 	public void addPoint(double x, double y, double z,
 						 AppearanceState appearance) {
 		pointCoordinates.add(new double[] { x, y, z });
 		pointColors.add(appearance.getColor());
 		pointSizes.add(appearance.getSize());
 	}
 	
 	@Override
 	public void addCircle(double cx, double cy, double cz, double nx,
 			double ny, double nz, double radius, AppearanceState appearance) {
 		circleCenters.add(new double[] { cx, cy, cz });
 		circleNormals.add(new double[] { nx, ny, nz });
 		circleColors.add(appearance.getColor());
 		circleRadii.add(radius);
 	}
 
 	private void addLineObject(double x1, double y1, double z1, double x2,
 			double y2, double z2, AppearanceState appearance, int type) {
 		lineCoordinates.add(new double[] { x1, y1, z1 });
 		lineCoordinates.add(new double[] { x2, y2, z2 });
 		lineColors.add(appearance.getColor());
 		lineIndices.add(lineCoordinates.size() - 2);
 		lineIndices.add(lineCoordinates.size() - 1);
 		lineSizes.add(appearance.getSize());
 		lineTypes.add(type);
 	}
 
 	@Override
 	public void addSegment(double x1, double y1, double z1, double x2, double y2,
 			double z2, AppearanceState appearance) {
 		addLineObject(x1, y1, z1, x2, y2, z2, appearance, 0);
 	}
 	
 	@Override
 	public void addRay(double x1, double y1, double z1, double x2, double y2,
 			double z2, AppearanceState appearance) {
 		addLineObject(x1, y1, z1, x2, y2, z2, appearance, 1);
 	}
 	
 	@Override
 	public void addLine(double x1, double y1, double z1, double x2, double y2,
 			double z2, AppearanceState appearance) {
 		addLineObject(x1, y1, z1, x2, y2, z2, appearance, 2);
 	}
 
 	@Override
 	public void addPolygon(double[][] vertices, AppearanceState appearance) {
 		polygonVertices.add(vertices);
 		polygonColors.add(appearance.getColor());
 		polygonTotalVertexCount += vertices.length;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.tum.in.jrealityplugin.Cindy3DViewer#shutdown()
 	 */
 	@Override
 	public void shutdown() {
 		frame.dispose();
 	}
 
 	/**
 	 * Deletes all point primitives from the internal data structures
 	 */
 	private void clearPoints() {
 		pointCoordinates.clear();
 		pointColors.clear();
 		pointSizes.clear();
 	}
 	
 	/**
 	 * Transfers internal point data to jReality
 	 */
 	private void updatePoints() {
 		if (pointCoordinates.size() == 0)
 			return;
 		psf.setVertexCount(pointCoordinates.size());
 		psf.setVertexCoordinates(pointCoordinates.toArray(new double[0][0]));
 		psf.setVertexColors(pointColors.toArray(new Color[0]));
 
 		double[] sizesArray = new double[pointSizes.size()];
 		for (int i = 0; i < pointSizes.size(); ++i)
 			sizesArray[i] = pointSizes.get(i);
 
 		psf.setVertexRelativeRadii(sizesArray);
 		psf.update();
 	}
 
 	private void clearCircles() {
 		circleCenters.clear();
 		circleNormals.clear();
 		circleColors.clear();
 		circleRadii.clear();
 	}
 	
 	private void updateCircles() {
 		if (circleCenters.size() == 0)
 			return;
 		psf2.setVertexCount(circleCenters.size());
 		psf2.setVertexCoordinates(circleCenters.toArray(new double[0][0]));
 		psf2.setVertexColors(circleColors.toArray(new Color[0]));
 		psf2.setVertexNormals(circleNormals.toArray(new double[0][0]));
 
 		double[] radiiArray = new double[circleRadii.size()];
 		for (int i = 0; i < circleRadii.size(); ++i)
 			radiiArray[i] = circleRadii.get(i);
 
 		psf2.setVertexRelativeRadii(radiiArray);
 		psf2.update();
 	}
 
 	/**
 	 * Deletes all line primitives from the internal data structures
 	 */
 	private void clearLines() {
 		lineCoordinates.clear();
 		lineIndices.clear();
 		lineSizes.clear();
 		lineColors.clear();
 		lineTypes.clear();
 	}
 
 	/**
 	 * Transfers internal line data to jReality
 	 */
 	private void updateLines() {
 		if (lineCoordinates.size() == 0)
 			return;
 		ilsf.setVertexCount(lineCoordinates.size());
 		ilsf.setVertexCoordinates(lineCoordinates.toArray(new double[0][0]));
 
 		Color[] pointColorsArray = new Color[lineColors.size()*2];
 		for (int i = 0; i < lineColors.size(); i++) {
 			pointColorsArray[2*i] = lineColors.get(i);
 			pointColorsArray[2*i+1] = lineColors.get(i);
 		}
 		ilsf.setVertexColors(pointColorsArray);
 		pointColorsArray = null;
 
 		double[] pointSizesArray = new double[lineSizes.size()*2];
 		for (int i = 0; i < lineColors.size(); i++) {
 			pointSizesArray[2*i] = lineSizes.get(i);
 			pointSizesArray[2*i+1] = lineSizes.get(i);
 		}
 		ilsf.setVertexRelativeRadii(pointSizesArray);
 
 		ilsf.setEdgeCount(lineIndices.size() / 2);
 		ilsf.setEdgeColors(lineColors.toArray(new Color[0]));
 
 		int[] indicesArray = new int[lineIndices.size()];
 		for (int i = 0; i < lineIndices.size(); ++i)
 			indicesArray[i] = lineIndices.get(i);
 		ilsf.setEdgeIndices(indicesArray);
 
 		double[] sizesArray = new double[lineSizes.size()];
 		for (int i = 0; i < lineSizes.size(); ++i)
 			sizesArray[i] = lineSizes.get(i);
 		ilsf.setEdgeRelativeRadii(sizesArray);
 		
 		int[] typesArray = new int[lineTypes.size()];
 		for (int i = 0; i < lineTypes.size(); ++i)
 			typesArray[i] = lineTypes.get(i);
 		ilsf.setEdgeAttribute(Attribute.attributeForName("lineType"),
 				new IntArray(typesArray));
 
 		ilsf.update();
 	}
 
 	/**
 	 * Deletes all polygons form the interal data structures
 	 */
 	private void clearPolygons() {
 		polygonVertices.clear();
 		polygonColors.clear();
 		polygonTotalVertexCount = 0;
 	}
 
 	/**
 	 * Transfers internal polygon data to jReality
 	 */
 	private void updatePolygons() {
 		if (polygonTotalVertexCount == 0)
 			return;
 		
 		int faceCount = polygonVertices.size();
 		double[][] vertices = new double[polygonTotalVertexCount][3];
 		int[][] faceIndices = new int[faceCount][];
 		int vertexId = 0;
 		for (int faceId = 0; faceId < faceCount; ++faceId) {
 			int faceVertexId = 0;
 			double[][] faceVertices = polygonVertices.get(faceId);
 			int[] indices = new int[faceVertices.length]; 
 			for (double[] vertex : faceVertices) {
 				vertices[vertexId] = vertex;
 				indices[faceVertexId] = vertexId;
 				++vertexId;
 				++faceVertexId;
 			}
 			faceIndices[faceId] = indices;
 		}
 
 		ifsf.setVertexCount(polygonTotalVertexCount);
 		ifsf.setVertexCoordinates(vertices);
 		ifsf.setFaceCount(polygonVertices.size());
 		ifsf.setFaceIndices(faceIndices);
 		ifsf.setFaceColors(polygonColors.toArray(new Color[0]));
 		ifsf.setLineCount(0);
 		ifsf.setGenerateEdgesFromFaces(false);
 		ifsf.setGenerateFaceNormals(true);
 		
 		ifsf.update();
 	}
 }
