 package au.org.intersect.faims.android.ui.map;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.UUID;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import roboguice.RoboGuice;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.util.SparseArray;
 import android.view.View;
 import android.widget.RelativeLayout;
 import au.org.intersect.faims.android.R;
 import au.org.intersect.faims.android.constants.FaimsSettings;
 import au.org.intersect.faims.android.data.User;
 import au.org.intersect.faims.android.database.DatabaseManager;
 import au.org.intersect.faims.android.exceptions.MapException;
 import au.org.intersect.faims.android.gps.GPSDataManager;
 import au.org.intersect.faims.android.gps.GPSLocation;
 import au.org.intersect.faims.android.log.FLog;
 import au.org.intersect.faims.android.nutiteq.CanvasLayer;
 import au.org.intersect.faims.android.nutiteq.CustomGdalMapLayer;
 import au.org.intersect.faims.android.nutiteq.CustomOgrLayer;
 import au.org.intersect.faims.android.nutiteq.CustomSpatialiteLayer;
 import au.org.intersect.faims.android.nutiteq.DatabaseLayer;
 import au.org.intersect.faims.android.nutiteq.DatabaseTextLayer;
 import au.org.intersect.faims.android.nutiteq.GeometryData;
 import au.org.intersect.faims.android.nutiteq.GeometryStyle;
 import au.org.intersect.faims.android.nutiteq.GeometryUtil;
 import au.org.intersect.faims.android.nutiteq.SpatialiteTextLayer;
 import au.org.intersect.faims.android.nutiteq.TrackLogDatabaseLayer;
 import au.org.intersect.faims.android.nutiteq.WKTUtil;
 import au.org.intersect.faims.android.ui.activity.ShowProjectActivity;
 import au.org.intersect.faims.android.ui.map.tools.AreaTool;
 import au.org.intersect.faims.android.ui.map.tools.AzimuthTool;
 import au.org.intersect.faims.android.ui.map.tools.CreateLineTool;
 import au.org.intersect.faims.android.ui.map.tools.CreatePointTool;
 import au.org.intersect.faims.android.ui.map.tools.CreatePolygonTool;
 import au.org.intersect.faims.android.ui.map.tools.DatabaseSelectionTool;
 import au.org.intersect.faims.android.ui.map.tools.EditTool;
 import au.org.intersect.faims.android.ui.map.tools.FollowTool;
 import au.org.intersect.faims.android.ui.map.tools.GeometriesIntersectSelectionTool;
 import au.org.intersect.faims.android.ui.map.tools.HighlightTool;
 import au.org.intersect.faims.android.ui.map.tools.LegacySelectionTool;
 import au.org.intersect.faims.android.ui.map.tools.LineDistanceTool;
 import au.org.intersect.faims.android.ui.map.tools.LoadTool;
 import au.org.intersect.faims.android.ui.map.tools.MapTool;
 import au.org.intersect.faims.android.ui.map.tools.PointDistanceTool;
 import au.org.intersect.faims.android.ui.map.tools.PointSelectionTool;
 import au.org.intersect.faims.android.ui.map.tools.PolygonSelectionTool;
 import au.org.intersect.faims.android.ui.map.tools.TouchSelectionTool;
 import au.org.intersect.faims.android.util.BitmapUtil;
 import au.org.intersect.faims.android.util.ScaleUtil;
 import au.org.intersect.faims.android.util.SpatialiteUtil;
 
 import com.google.inject.Inject;
 import com.nutiteq.MapView;
 import com.nutiteq.components.Bounds;
 import com.nutiteq.components.Components;
 import com.nutiteq.components.Constraints;
 import com.nutiteq.components.MapPos;
 import com.nutiteq.components.Options;
 import com.nutiteq.components.Range;
 import com.nutiteq.geometry.DynamicMarker;
 import com.nutiteq.geometry.Geometry;
 import com.nutiteq.geometry.Line;
 import com.nutiteq.geometry.Marker;
 import com.nutiteq.geometry.Point;
 import com.nutiteq.geometry.Polygon;
 import com.nutiteq.geometry.VectorElement;
 import com.nutiteq.layers.Layer;
 import com.nutiteq.layers.raster.GdalMapLayer;
 import com.nutiteq.projections.EPSG3857;
 import com.nutiteq.style.LineStyle;
 import com.nutiteq.style.MarkerStyle;
 import com.nutiteq.style.PointStyle;
 import com.nutiteq.style.PolygonStyle;
 import com.nutiteq.style.StyleSet;
 import com.nutiteq.style.TextStyle;
 import com.nutiteq.ui.MapListener;
 import com.nutiteq.utils.UnscaledBitmapLoader;
 import com.nutiteq.vectorlayers.MarkerLayer;
 
 public class CustomMapView extends MapView {
 
 	public class InternalMapListener extends MapListener {
 
 		@Override
 		public void onDrawFrameAfter3D(GL10 arg0, float arg1) {
 
 		}
 
 		@Override
 		public void onDrawFrameBefore3D(GL10 arg0, float arg1) {
 		}
 
 		@Override
 		public void onLabelClicked(VectorElement arg0, boolean arg1) {
 		}
 
 		@Override
 		public void onMapClicked(double arg0, double arg1, boolean arg2) {
 			if (CustomMapView.this.mapListener != null) {
 				CustomMapView.this.mapListener.onMapClicked(arg0, arg1, arg2);
 			}
 			if (CustomMapView.this.toolsEnabled && CustomMapView.this.currentTool != null) {
 				CustomMapView.this.currentTool.onMapClicked(arg0, arg1, arg2);
 			}
 		}
 
 		@Override
 		public void onMapMoved() {
 			if (CustomMapView.this.toolsEnabled && CustomMapView.this.currentTool != null) {
 				CustomMapView.this.currentTool.onMapChanged();
 			}
 			CustomMapView.this.updateDrawView();
 		}
 
 		@Override
 		public void onSurfaceChanged(GL10 arg0, int arg1, int arg2) {
 		}
 
 		@Override
 		public void onVectorElementClicked(VectorElement arg0, double arg1,
 				double arg2, boolean arg3) {
 			if (CustomMapView.this.mapListener != null) {
 				CustomMapView.this.mapListener.onVectorElementClicked(arg0,
 						arg1, arg2, arg3);
 			}
 			if (CustomMapView.this.toolsEnabled && CustomMapView.this.currentTool != null) {
 				CustomMapView.this.currentTool.onVectorElementClicked(arg0,
 						arg1, arg2, arg3);
 			}
 		}
 
 	}
 
 	public static class CustomMapListener {
 
 		public void onMapClicked(double arg0, double arg1, boolean arg2) {
 		}
 
 		public void onVectorElementClicked(VectorElement arg0, double arg1,
 				double arg2, boolean arg3) {
 		}
 	}
 	
 	public static interface CreateCallback {
 	
 		public void onCreate(int geomId);
 		
 	}
 	
 	public static interface LoadCallback {
 	
 		public void onLoad(String id, boolean isEntity);
 		
 	}
 	
 	@Inject
 	GPSDataManager gpsDataManager;
 	
 	@Inject
 	DatabaseManager databaseManager;
 
 	// TODO what is this?
 	private static int cacheId = 9991;
 
 	private static int layerId = 1;
 
 	private static int geomId = 1;
 
 	private SparseArray<Layer> layerIdMap;
 
 	private HashMap<String, Layer> layerNameMap;
 
 	private SparseArray<Geometry> geometryIdMap;
 
 	private SparseArray<Layer> geometryIdToLayerMap;
 
 	private DrawView drawView;
 
 	private EditView editView;
 	
 	private MapNorthView northView;
 
 	private ScaleBarView scaleView;
 
 	private RelativeLayout toolsView;
 
 	private ArrayList<Runnable> runnableList;
 	private ArrayList<Thread> threadList;
 
 	private boolean canRunThreads;
 
 	private ArrayList<MapTool> tools;
 
 	private MapTool currentTool;
 
 	private InternalMapListener internalMapListener;
 	private CustomMapListener mapListener;
 
 	private Layer selectedLayer;
 	
 	private ArrayList<Geometry> highlightGeometryList;
 
 	private ArrayList<Geometry> transformGeometryList;
 
 	private boolean showDecimal;
 	private LayerManagerDialog layerManagerDialog;
 
 	private boolean showKm;
 
 	private boolean toolsEnabled = true;
 
 	private MapLayout mapLayout;
 	
 	private MarkerLayer currentPositionLayer;
 	private GPSLocation previousLocation;
 	private Float previousHeading;
 
 	private WeakReference<ShowProjectActivity> activityRef;
 	
 	private HashMap<String, String> databaseLayerQueryMap;
 	private String trackLogQueryName;
 	private String trackLogQuerySql;
 	private Map<User, Boolean> userCheckedList;
 	private Integer userTrackLogLayer;
 	
 	private ArrayList<String> databaseLayerQueryList;
 
 	private SelectionDialog selectionDialog;
 	
 	private HashMap<String, GeometrySelection> selectionMap;
 
 	private GeometrySelection selectedSelection;
 	private GeometrySelection restrictedSelection;
 
 	private HashMap<String, QueryBuilder> selectQueryMap;
 
 	private HashMap<String, LegacyQueryBuilder> legacySelectQueryMap;
 	
 	private ArrayList<QueryBuilder> selectQueryList;
 	
 	private ArrayList<LegacyQueryBuilder> legacySelectQueryList;
 
 	private String lastSelectionQuery;
 
 	private Geometry geomToFollow;
 
 	private float buffer = 50;
 
 	protected boolean locationValid;
 
 	private Marker gpsMarker;
 
 	private Bitmap blueDot;
 
 	private Bitmap greyDot;
 
 	private Bitmap blueArrow;
 
 	private Bitmap greyArrow;
 
 	private Bitmap tempBitmap;
 
 	private Geometry geomToFollowBuffer;
 
 	private CreateCallback createCallback;
 
 	private LoadCallback loadCallback;
 
 	private int vertexLayerId;
 
 	private boolean projectionProper;
 	
 	public CustomMapView(ShowProjectActivity activity, MapLayout mapLayout) {
 		this(activity);
 		
 		this.activityRef = new WeakReference<ShowProjectActivity>(activity);
 
 		layerIdMap = new SparseArray<Layer>();
 		layerNameMap = new HashMap<String, Layer>();
 		geometryIdMap = new SparseArray<Geometry>();
 		geometryIdToLayerMap = new SparseArray<Layer>();
         runnableList = new ArrayList<Runnable>();
         threadList = new ArrayList<Thread>();
         tools = new ArrayList<MapTool>();
         highlightGeometryList = new ArrayList<Geometry>();
         databaseLayerQueryMap = new HashMap<String, String>();
         databaseLayerQueryList = new ArrayList<String>();
         selectionMap = new HashMap<String, GeometrySelection>();
         selectQueryMap = new HashMap<String, QueryBuilder>();
         legacySelectQueryMap = new HashMap<String, LegacyQueryBuilder>();
         selectQueryList = new ArrayList<QueryBuilder>();
         legacySelectQueryList = new ArrayList<LegacyQueryBuilder>();
         userCheckedList = new HashMap<User, Boolean>();
         
 		this.mapLayout = mapLayout;
 		this.drawView = mapLayout.getDrawView();
 		this.editView = mapLayout.getEditView();
 		this.northView = mapLayout.getNorthView();
 		this.scaleView = mapLayout.getScaleView();
 		this.toolsView = mapLayout.getToolsView();
 		
 		this.drawView.setMapView(this);
 		this.editView.setMapView(this);
 		
 		this.editView.setColor(Color.GREEN);
 		
 		// TODO make this configurable
 		scaleView.setBarWidthRange((int) ScaleUtil.getDip(activity, 60),
 				(int) ScaleUtil.getDip(activity, 120));
 
 		initTools();
 
 		setViewLocked(true); // note: this is the default behaviour for maps
 
 		internalMapListener = new InternalMapListener();
 		getOptions().setMapListener(internalMapListener);
 		
 		RoboGuice.getBaseApplicationInjector(this.activityRef.get().getApplication()).injectMembers(this);
 		
 		// cache gps bitmaps
 		blueDot = UnscaledBitmapLoader.decodeResource(
 				getResources(), R.drawable.blue_dot);
 		greyDot = UnscaledBitmapLoader.decodeResource(
 				getResources(), R.drawable.grey_dot);
 		blueArrow = UnscaledBitmapLoader.decodeResource(
 				getResources(), R.drawable.blue_arrow);
 		greyArrow = UnscaledBitmapLoader.decodeResource(
 				getResources(), R.drawable.grey_arrow);
 		
 		// start map threads
 		startMapOverlayThread();
         startGPSLocationThread();
         
         // set default value for showing point coords as degrees or decimal
         setShowDecimal(!GeometryUtil.EPSG4326.equals(activityRef.get().getProject().getSrid()));
         
         // create vertex editing canvas
         try {
         	CanvasLayer layer = new CanvasLayer(nextLayerId(), "Vertex Canvas Layer " + UUID.randomUUID(),
     				new EPSG3857());
     		this.getLayers().addLayer(layer);
         	vertexLayerId = addLayer(layer);
         } catch (Exception e) {
         	FLog.e("error adding vertex layer", e);
         }
         
         // store proper projection result
         try {
 			projectionProper = SpatialiteUtil.isProperProjection(activityRef.get().getProject().getSrid());
 		} catch (Exception e) {
 			FLog.e("error checking for proper projection", e);
 		}
 	}
 
 	public CustomMapView(Context context) {
 		super(context);
 
 		this.setComponents(new Components());
 
 		// Activate some mapview options to make it smoother - optional
 		this.getOptions().setPreloading(true);
 		this.getOptions().setSeamlessHorizontalPan(true);
 		this.getOptions().setTileFading(true);
 		this.getOptions().setKineticPanning(true);
 		// this.getOptions().setDoubleClickZoomIn(true);
 		// this.getOptions().setDualClickZoomOut(true);
 
 		// set sky bitmap - optional, default - white
 		this.getOptions().setSkyDrawMode(Options.DRAW_BITMAP);
 		this.getOptions().setSkyOffset(4.86f);
 		this.getOptions().setSkyBitmap(
 				UnscaledBitmapLoader.decodeResource(getResources(),
 						R.drawable.sky_small));
 
 		// Map background, visible if no map tiles loaded - optional, default -
 		// white
 		this.getOptions().setBackgroundPlaneDrawMode(Options.DRAW_BITMAP);
 		this.getOptions().setBackgroundPlaneBitmap(
 				UnscaledBitmapLoader.decodeResource(getResources(),
 						R.drawable.background_plane));
 		this.getOptions().setClearColor(Color.WHITE);
 
 		// configure texture caching - optional, suggested
 		this.getOptions().setTextureMemoryCacheSize(40 * 1024 * 1024);
 		this.getOptions().setCompressedMemoryCacheSize(8 * 1024 * 1024);
 
 		// TODO find out how this works? can we pass different paths for
 		// different maps?
 		// this.getOptions().setPersistentCachePath(activity.getDatabasePath("mapcache").getPath());
 		// set persistent raster cache limit to 100MB
 		// this.getOptions().setPersistentCacheSize(100 * 1024 * 1024);
 	}
 
 	public static int nextId() {
 		return cacheId++;
 	}
 
 	public static int nextLayerId() {
 		return layerId++;
 	}
 
 	public static int nextGeomId() {
 		return geomId++;
 	}
 
 	public int addLayer(Layer layer) throws Exception {
 		if (layerIdMap.get(getLayerId(layer)) != null) {
 			throw new MapException("Layer already exists");
 		}
 
 		layerIdMap.put(getLayerId(layer), layer);
 		layerNameMap.put(getLayerName(layer), layer);
 
 		return getLayerId(layer);
 	}
 
 	public void removeLayer(int layerId) throws Exception {
 		Layer layer = layerIdMap.get(layerId);
 		removeLayer(layer);
 	}
 
 	public void removeLayer(Layer layer) throws Exception {
 		if (layer == null) {
 			throw new MapException("Layer does not exist");
 		}
 
 		// can only remove base layer if its the only layer on the map
 		CustomGdalMapLayer baseLayer = (CustomGdalMapLayer) getLayers()
 				.getBaseLayer();
 		if (baseLayer == layer) {
 			this.getLayers().setBaseLayer(null);
 		}
 
 		this.getLayers().removeLayer(layer);
 		int id = getLayerId(layer);
 		String name = getLayerName(layer);
 		this.layerIdMap.remove(id);
 		this.layerNameMap.remove(name);
 		
 		// remove all geometry in layer if canvas layer
 		if (layer instanceof CanvasLayer) {
 			CanvasLayer canvas = (CanvasLayer) layer;
 			for (Geometry geom : canvas.getGeometryList()) {
 				removeGeometry(geom);
 			}
 		}
 		
 		if (layer == selectedLayer) {
 			this.selectedLayer = null;
 			updateLayers();
 		}
 		
 		// remove associated text layer
 		if (layer instanceof CustomSpatialiteLayer) {
 			removeLayer(((CustomSpatialiteLayer) layer).getTextLayer());
 		} else if (layer instanceof DatabaseLayer) {
 			removeLayer(((DatabaseLayer) layer).getTextLayer());
 		} else if ((layer instanceof GdalMapLayer) && layer == baseLayer) {
 			this.getLayers().removeLayer(currentPositionLayer);
 			currentPositionLayer = null;
 			gpsMarker = null;
 		}
 		
 	}
 
 	public Layer getLayer(int layerId) {
 		return layerIdMap.get(layerId);
 	}
 
 	public Layer getLayer(String layerName) {
 		return layerNameMap.get(layerName);
 	}
 
 	public String getLayerName(Layer layer) {
 		String layerName = "N/A";
 		if (layer instanceof CustomGdalMapLayer) {
 			layerName = ((CustomGdalMapLayer) layer).getName();
 		} else if (layer instanceof CustomOgrLayer) {
 			layerName = ((CustomOgrLayer) layer).getName();
 		} else if (layer instanceof CustomSpatialiteLayer) {
 			layerName = ((CustomSpatialiteLayer) layer).getName();
 		} else if (layer instanceof CanvasLayer) {
 			layerName = ((CanvasLayer) layer).getName();
 		} else if (layer instanceof DatabaseLayer) {
 			layerName = ((DatabaseLayer) layer).getName();
 		}
 		return layerName;
 	}
 
 	public void setLayerName(Layer layer, String layerName) {
 		if (layer instanceof CustomGdalMapLayer) {
 			((CustomGdalMapLayer) layer).setName(layerName);
 		} else if (layer instanceof CustomOgrLayer) {
 			((CustomOgrLayer) layer).setName(layerName);
 		} else if (layer instanceof CustomSpatialiteLayer) {
 			((CustomSpatialiteLayer) layer).setName(layerName);
 		} else if (layer instanceof CanvasLayer) {
 			((CanvasLayer) layer).setName(layerName);
 		} else if (layer instanceof DatabaseLayer) {
 			((DatabaseLayer) layer).setName(layerName);
 		}
 	}
 
 	public int getLayerId(Layer layer) {
 		int layerId = 0;
 		if (layer instanceof CustomGdalMapLayer) {
 			layerId = ((CustomGdalMapLayer) layer).getLayerId();
 		} else if (layer instanceof CustomOgrLayer) {
 			layerId = ((CustomOgrLayer) layer).getLayerId();
 		} else if (layer instanceof CustomSpatialiteLayer) {
 			layerId = ((CustomSpatialiteLayer) layer).getLayerId();
 		} else if (layer instanceof CanvasLayer) {
 			layerId = ((CanvasLayer) layer).getLayerId();
 		} else if (layer instanceof DatabaseLayer) {
 			layerId = ((DatabaseLayer) layer).getLayerId();
 		}
 		return layerId;
 	}
 
 	public int addGeometry(Layer layer, Geometry geom) throws Exception {
 		if (geom == null) {
 			throw new MapException("Geometry does not exist");
 		}
 		
 		if (layer == null) {
 			throw new MapException("Map does not exist");
 		}
 
 		int geomId = getGeometryId(geom);
 		if (geometryIdMap.get(geomId) != null) {
 			throw new MapException("Geometry already exists");
 		}
 
 		geometryIdMap.put(geomId, geom);
 		geometryIdToLayerMap.put(geomId, layer);
 
 		return geomId;
 	}
 
 	public void removeGeometry(int geomId) throws Exception {
 		removeGeometry(getGeometry(geomId));
 	}
 
 	public void removeGeometry(Geometry geom) throws Exception {
 		removeGeometryWithoutClearing(geom);
 		
 		clearHighlights();
 		clearHighlightTransform();
 	}
 	
 	public void removeGeometryWithoutClearing(Geometry geom) throws Exception {
 		if (geom == null) {
 			throw new MapException("Geometry does not exist");
 		}
 
 		int geomId = getGeometryId(geom);
 		geometryIdMap.remove(geomId);
 		geometryIdToLayerMap.remove(geomId);
 	}
 	
 	public String getGeometryLayerName(int geomId) {
 		return getLayerName(geometryIdToLayerMap.get(geomId));
 	}
 	
 	public String getGeometryLayerName(Geometry geom) {
 		return getGeometryLayerName(((GeometryData) geom.userData).geomId);
 	}
 
 	public int getGeometryId(Geometry geom) {
 		if (geom.userData instanceof GeometryData) {
 			return ((GeometryData) geom.userData).geomId;
 		}
 		return 0;
 	}
 
 	public Geometry getGeometry(int geomId) {
 		return geometryIdMap.get(geomId);
 	}
 
 	public void setViewLocked(boolean lock) {
 		if (lock) {
 			this.getConstraints().setTiltRange(new Range(90.0f, 90.0f));
 			this.setTilt(90.0f);
 		} else {
 			this.getConstraints().setTiltRange(Constraints.DEFAULT_TILT_RANGE);
 		}
 	}
 
 	public static void registerLicense(Context context) {
 		final String LICENSE = "XTUMwQ0ZIRklrbEZ2T0dIdkZ3QkRieVBtcWJqdjZ1RUtBaFVBa1RreXdabUIraER4UjFmZ01aUk5oay83a2hzPQoKcGFja2FnZU5hbWU9YXUub3JnLmludGVyc2VjdC5mYWltcy5hbmRyb2lkCndhdGVybWFyaz1jdXN0b20KCg==";
 		CustomMapView.registerLicense(LICENSE, context);
 		Bitmap logo = BitmapFactory.decodeResource(context.getResources(),
 				R.drawable.ic_launcher);
 		CustomMapView.setWatermark(logo, -1.0f, -1.0f, 0.2f);
 	}
 
 	public void updateMapOverlay() {
 		northView.setMapRotation(this.getRotation());
 		int width = this.getWidth();
 		int height = this.getHeight();
 
 		try {
 			scaleView.setMapBoundary(this.getZoom(), width, height, SpatialiteUtil
 					.distanceBetween(
 							GeometryUtil.convertToWgs84(this.screenToWorld(0, height, 0)), 
 							GeometryUtil.convertToWgs84(this.screenToWorld(width, height, 0)), 
 							GeometryUtil.EPSG4326) / 1000.0);
 		} catch (Exception e) {
 			FLog.e("error updating scalebar", e);
 		}
 	}
 
 	public void startThread(Runnable runnable) {
 		runnableList.add(runnable);
 
 		// Note: the runnable will need to handle stopping the thread
 		Thread t = new Thread(runnable);
 		threadList.add(t);
 		t.start();
 	}
 
 	public void restartThreads() {
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 					// wait for all threads to finish
 					canRunThreads = false;
 					while (true) {
 						boolean allThreadsTerminated = true;
 						for (Thread t : threadList) {
 							if (t.getState() != Thread.State.TERMINATED) {
 								allThreadsTerminated = false;
 								break;
 							}
 						}
 						if (allThreadsTerminated) {
 							break;
 						}
 						FLog.d("Waiting to start map threads");
 						Thread.sleep(1000);
 					}
 					canRunThreads = true;
 					threadList.clear();
 					for (Runnable r : runnableList) {
 						Thread t = new Thread(r);
 						threadList.add(t);
 						t.start();
 					}
 				} catch (Exception e) {
 					FLog.e("error restarting map threads", e);
 				}
 			}
 		}).start();
 	}
 
 	public void killThreads() {
 		canRunThreads = false;
 	}
 
 	public boolean canRunThreads() {
 		return canRunThreads;
 	}
 
 	public int addBaseMap(String layerName, String file) throws Exception {
 		if (!new File(file).exists()) {
 			throw new MapException("Error map does not exist " + file);
 		}
 		
 		if (this.getLayers().getBaseLayer() != null) {
 			Layer layer = this.getLayers().getBaseLayer();
 			removeLayer(layer);
 		}
 
 		validateLayerName(layerName);
 
 		CustomGdalMapLayer gdalLayer = new CustomGdalMapLayer(nextLayerId(),
 				layerName, new EPSG3857(), 0, 18, CustomMapView.nextId(), file,
 				this, true);
 		//gdalLayer.setShowAlways(true);
 		this.getLayers().setBaseLayer(gdalLayer);
 		//setZoomRange(gdalLayer);
 		//setMapBounds(gdalLayer);
 		
 		if(currentPositionLayer == null){
         	currentPositionLayer = new MarkerLayer(gdalLayer.getProjection());
         	CustomMapView.this.getLayers().addLayer(currentPositionLayer);
         }
 		
 		// center map 
 		double[][] boundaries = gdalLayer.getBoundaries();
 		setMapFocusPoint(((float)boundaries[0][0]+(float)boundaries[3][0])/2, ((float)boundaries[0][1]+(float)boundaries[3][1])/2);
 		
 		orderLayers();
 		
 		return addLayer(gdalLayer);
 	}
 
 	public int addRasterMap(String layerName, String file) throws Exception {
 		if (!new File(file).exists()) {
 			throw new MapException("Error map does not exist " + file);
 		}
 		
 		validateLayerName(layerName);
 
 		CustomGdalMapLayer gdalLayer = new CustomGdalMapLayer(nextLayerId(),
 				layerName, new EPSG3857(), 0, 18, CustomMapView.nextId(), file,
 				this, true);
 		//gdalLayer.setShowAlways(true);
 		this.getLayers().addLayer(gdalLayer);
 		//setZoomRange(gdalLayer);
 		//setMapBounds(gdalLayer);
 		
 		if(currentPositionLayer == null){
         	currentPositionLayer = new MarkerLayer(gdalLayer.getProjection());
         	CustomMapView.this.getLayers().addLayer(currentPositionLayer);
         }
 		
 		orderLayers();
 		
 		return addLayer(gdalLayer);
 	}
 
 	protected void setZoomRange(CustomGdalMapLayer gdalLayer){
 		double bestzoom = gdalLayer.getBestZoom();
 		this.getConstraints().setZoomRange(new Range((float) (bestzoom - 5.0), (float) (bestzoom + 1)));
 	}
 	
 	protected void setMapBounds(CustomGdalMapLayer gdalLayer){
 		double[][] bound = gdalLayer.getBoundaries();
 		MapPos p1 = GeometryUtil.convertFromWgs84(new MapPos(bound[0][0], bound[0][1]));
 		MapPos p2 = GeometryUtil.convertFromWgs84(new MapPos(bound[3][0], bound[3][1]));
 		Bounds bounds = new Bounds(p1.x, p1.y, p2.x, p2.y);
 		this.getConstraints().setMapBounds(bounds);
 	}
 
 	public void setMapFocusPoint(float longitude, float latitude)
 			throws Exception {
 		if (latitude < -90.0f || latitude > 90.0f) {
 			throw new MapException("Error map latitude out of range "
 					+ latitude);
 		}
 		this.setFocusPoint(new EPSG3857().fromWgs84(longitude, latitude));
 	}
 
 	public int addShapeLayer(String layerName, String file,
 			StyleSet<PointStyle> pointStyleSet,
 			StyleSet<LineStyle> lineStyleSet,
 			StyleSet<PolygonStyle> polygonStyleSet) throws Exception {
 
 		if (!new File(file).exists()) {
 			throw new MapException("Error file does not exist " + file);
 		}
 
 		validateLayerName(layerName);
 
 		CustomOgrLayer ogrLayer = new CustomOgrLayer(nextLayerId(), layerName,
 				new EPSG3857(), file, null, FaimsSettings.MAX_VECTOR_OBJECTS,
 				pointStyleSet, lineStyleSet, polygonStyleSet);
 		// ogrLayer.printSupportedDrivers();
 		// ogrLayer.printLayerDetails(table);
 		this.getLayers().addLayer(ogrLayer);
 		orderLayers();
 		return addLayer(ogrLayer);
 	}
 
 	public int addSpatialLayer(String layerName, String file, String tablename,
 			String idColumn, String labelColumn, GeometryStyle pointStyle,
 			GeometryStyle lineStyle,
 			GeometryStyle polygonStyle,
 			StyleSet<TextStyle> textStyleSet) throws Exception {
 		if (!new File(file).exists()) {
 			throw new MapException("Error file does not exist " + file);
 		}
 
 		validateLayerName(layerName);
 		
 		if (idColumn == null || "".equals(idColumn)) {
 			throw new MapException("Invalid id column");
 		}
 		
 		if (labelColumn == null || "".equals(labelColumn)) {
 			throw new MapException("Invalid label column");
 		}
 		
 		String[] labelColumns = new String[] { idColumn, labelColumn };
 		
 		CustomSpatialiteLayer spatialLayer = new CustomSpatialiteLayer(
 				nextLayerId(), layerName, new EPSG3857(), this, file, tablename,
 				"Geometry", labelColumns,
 				FaimsSettings.MAX_VECTOR_OBJECTS, pointStyle, lineStyle,
 				polygonStyle);
 		this.getLayers().addLayer(spatialLayer);
 		
 		if (textStyleSet != null) {
 			// add text layer
 			SpatialiteTextLayer textLayer = new SpatialiteTextLayer(new EPSG3857(), spatialLayer, labelColumns, textStyleSet);
 			spatialLayer.setTextLayer(textLayer);
 			this.getLayers().addLayer(textLayer);
 		}
 		
 		orderLayers();
 		
 		return addLayer(spatialLayer);
 	}
 
 	public int addCanvasLayer(String layerName) throws Exception {
 		validateLayerName(layerName);
 
 		CanvasLayer layer = new CanvasLayer(nextLayerId(), layerName,
 				new EPSG3857());
 		this.getLayers().addLayer(layer);
 		orderLayers();
 		return addLayer(layer);
 	}
 	
 	public int addDatabaseLayer(String layerName, boolean isEntity, String queryName, String querySql, 
 			GeometryStyle pointStyle,
 			GeometryStyle lineStyle,
 			GeometryStyle polygonStyle,
 			StyleSet<TextStyle> textStyleSet) throws Exception {
 		validateLayerName(layerName);
 		
 		DatabaseLayer layer = new DatabaseLayer(nextLayerId(), layerName, new EPSG3857(), this,
 				isEntity ? DatabaseLayer.Type.ENTITY : DatabaseLayer.Type.RELATIONSHIP, queryName, querySql, databaseManager,
 				FaimsSettings.MAX_VECTOR_OBJECTS, pointStyle, lineStyle, polygonStyle);
 		this.getLayers().addLayer(layer);
 		
 		if (textStyleSet != null) {
 			// add text layer
 			DatabaseTextLayer textLayer = new DatabaseTextLayer(new EPSG3857(), layer, textStyleSet);
 			layer.setTextLayer(textLayer);
 			this.getLayers().addLayer(textLayer);
 		}
 		
 		orderLayers();
 		
 		return addLayer(layer);
 	}
 	
 	public int addDataBaseLayerForTrackLog(String layerName, Map<User, Boolean> users,
 			String queryName, String querySql,
 			GeometryStyle pointStyle,
 			GeometryStyle lineStyle,
 			GeometryStyle polygonStyle,
 			StyleSet<TextStyle> textStyleSet) throws Exception {
 		validateLayerName(layerName);
 		TrackLogDatabaseLayer layer = new TrackLogDatabaseLayer(nextLayerId(), layerName, new EPSG3857(), this,
 				DatabaseLayer.Type.GPS_TRACK, queryName, querySql,  databaseManager,
 				FaimsSettings.MAX_VECTOR_OBJECTS, users, pointStyle, lineStyle, polygonStyle);
 		this.getLayers().addLayer(layer);
 		
 		if (textStyleSet != null) {
 			// add text layer
 			DatabaseTextLayer textLayer = new DatabaseTextLayer(new EPSG3857(), layer, textStyleSet);
 			layer.setTextLayer(textLayer);
 			this.getLayers().addLayer(textLayer);
 		}
 		
 		orderLayers();
 		
 		return addLayer(layer);
 	}
 
 	public Point drawPoint(int layerId, MapPos point, GeometryStyle style) throws Exception {
 		return drawPoint(getLayer(layerId), point, style);
 	}
 
 	public Point drawPoint(Layer layer, MapPos point, GeometryStyle style)  throws Exception {
 		return drawPoint(layer, point, style, nextGeomId());
 	}
 	
 	public Point drawPoint(Layer layer, MapPos point, GeometryStyle style, int geomId)  throws Exception {
 		CanvasLayer canvas = (CanvasLayer) layer;
 		if (canvas == null) {
 			throw new MapException("Layer does not exist");
 		}
 		Point p = canvas.addPoint(geomId, point, style);
 		addGeometry(layer, p);
 		updateRenderer();
 		return p;
 	}
 	
 	public void restylePoint(Point point, GeometryStyle style) throws Exception {
 		CanvasLayer canvas = (CanvasLayer) geometryIdToLayerMap.get(getGeometryId(point));
 		if (canvas == null) {
 			throw new MapException("Layer does not exist");
 		}
 		canvas.removeGeometry(point);
 		removeGeometry(point);
 		drawPoint(canvas, GeometryUtil.convertToWgs84(point.getMapPos()), style, getGeometryId(point));
 		updateRenderer();
 	}
 
 	public Line drawLine(int layerId, List<MapPos> points, GeometryStyle style) throws Exception {
 		return drawLine(getLayer(layerId), points, style);
 	}
 	
 	public Line drawLine(Layer layer, List<MapPos> points, GeometryStyle style) throws Exception {
 		return drawLine(layer, points, style, nextGeomId());
 	}
 	
 	public Line drawLine(Layer layer, List<MapPos> points, GeometryStyle style, int geomId) throws Exception {
 		CanvasLayer canvas = (CanvasLayer) layer;
 		if (canvas == null) {
 			throw new MapException("Layer does not exist");
 		}
 		Line l = canvas.addLine(geomId, points, style);
 		addGeometry(layer, l);
 		updateRenderer();
 		return l;
 	}
 
 	public void restyleLine(Line line, GeometryStyle style) throws Exception {
 		CanvasLayer canvas = (CanvasLayer) geometryIdToLayerMap.get(getGeometryId(line));
 		if (canvas == null) {
 			throw new MapException("Layer does not exist");
 		}
 		canvas.removeGeometry(line);
 		removeGeometry(line);
 		drawLine(canvas, GeometryUtil.convertToWgs84(line.getVertexList()), style, getGeometryId(line));
 		updateRenderer();
 	}
 
 	public Polygon drawPolygon(int layerId, List<MapPos> points, GeometryStyle style) throws Exception {
 		return drawPolygon(getLayer(layerId), points, style);
 	}
 	
 	public Polygon drawPolygon(Layer layer, List<MapPos> points, GeometryStyle style) throws Exception {
 		return drawPolygon(layer, points, style, nextGeomId());
 	}
 
 	public Polygon drawPolygon(Layer layer, List<MapPos> points, GeometryStyle style, int geomId) throws Exception {
 		CanvasLayer canvas = (CanvasLayer) layer;
 		if (canvas == null) {
 			throw new MapException("Layer does not exist");
 		}
 		Polygon p = canvas.addPolygon(geomId, points, style);
 		addGeometry(layer, p);
 		updateRenderer();
 		return p;
 	}
 	
 	public void restylePolygon(Polygon polygon, GeometryStyle style) throws Exception {
 		CanvasLayer canvas = (CanvasLayer) geometryIdToLayerMap.get(getGeometryId(polygon));
 		if (canvas == null) {
 			throw new MapException("Layer does not exist");
 		}
 		canvas.removeGeometry(polygon);
 		removeGeometry(polygon);
 		drawPolygon(canvas, GeometryUtil.convertToWgs84(polygon.getVertexList()), style, getGeometryId(polygon));
 		updateRenderer();
 	}
 
 	public void clearGeometry(int geomId) throws Exception {
 		clearGeometry(getGeometry(geomId));
 	}
 
 	public void clearGeometry(Geometry geom) throws Exception {
 		if (geom == null) {
 			throw new MapException("Geometry does not exist");
 		}
 
 		CanvasLayer layer = (CanvasLayer) geometryIdToLayerMap.get(getGeometryId(geom));
 		if (layer == null) {
 			throw new MapException("Layer does not exist");
 		}
 		
 		layer.removeGeometry(geom);
 
 		removeGeometry(geom);
 
 		updateRenderer();
 	}
 
 	public void clearGeometryList(List<? extends Geometry> geomList)
 			throws Exception {
 		for (Object geom : geomList) {
 			Geometry geometry = (Geometry) geom;
 			clearGeometry(geometry);
 		}
 	}
 
 	public List<Geometry> getGeometryList(int layerId) throws Exception {
 		return getGeometryList(getLayer(layerId));
 	}
 
 	public List<Geometry> getGeometryList(Layer layer) throws Exception {
 		CanvasLayer canvas = (CanvasLayer) layer;
 
 		return canvas.getGeometryList();
 	}
 
 	public List<MapTool> getTools() {
 		return tools;
 	}
 
 	public void showLayerManagerDialog() {
 		layerManagerDialog = new LayerManagerDialog(this.activityRef.get());
 		layerManagerDialog.setTitle("Layer Manager");
 		layerManagerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Done", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// ignore
 			}
 		});
 		layerManagerDialog.attachToMap(this);
 		layerManagerDialog.show();
 	}
 
 	public Map<User, Boolean> getUserCheckedList() {
 		return userCheckedList;
 	}
 
 	public void putUserCheckList(User user, boolean value){
 		userCheckedList.put(user, value);
 	}
 	
 	public void clearUserCheckedList(){
 		userCheckedList.clear();
 	}
 
 	public Integer getUserTrackLogLayer() {
 		return userTrackLogLayer;
 	}
 
 	public void setUserTrackLogLayer(Integer userTrackLogLayer) {
 		this.userTrackLogLayer = userTrackLogLayer;
 	}
 
 	private void validateLayerName(String name) throws Exception {
 		if (name == null || "".equals(name)) {
 			throw new MapException("Please specify a name for the layer");
 		} else if (getLayer(name) != null) {
 			throw new MapException("Layer " + name + " already exists");
 		}
 	}
 
 	public void renameLayer(Layer layer, String layerName) throws Exception {
 		if (layer == null) {
 			throw new MapException("Layer does not exist");
 		}
 
 		validateLayerName(layerName);
 
 		setLayerName(layer, layerName);
 	}
 
 	private void initTools() {
 		tools.add(new HighlightTool(this.getContext(), this));
 		tools.add(new EditTool(this.getContext(), this));
 		tools.add(new CreatePointTool(this.getContext(), this));
 		tools.add(new CreateLineTool(this.getContext(), this));
 		tools.add(new CreatePolygonTool(this.getContext(), this));
 		tools.add(new PointDistanceTool(this.getContext(), this));
 		tools.add(new LineDistanceTool(this.getContext(), this));
 		tools.add(new AreaTool(this.getContext(), this));
 		tools.add(new AzimuthTool(this.getContext(), this));
 		tools.add(new TouchSelectionTool(this.getContext(), this));
 		tools.add(new DatabaseSelectionTool(this.getContext(), this));
 		tools.add(new LegacySelectionTool(this.getContext(), this));
 		tools.add(new PointSelectionTool(this.getContext(), this));
 		tools.add(new PolygonSelectionTool(this.getContext(), this));
 		tools.add(new GeometriesIntersectSelectionTool(this.getContext(), this));
 		tools.add(new FollowTool(this.getContext(), this));
 		tools.add(new LoadTool(this.getContext(), this));
 		//tools.add(new PathFollowerTool(this.getContext(), this));
 	}
 
 	public MapTool getTool(String name) {
 		for (MapTool tool : tools) {
 			if (tool.toString().equals(name)) {
 				return tool;
 			}
 		}
 		return null;
 	}
 
 	public void selectToolIndex(int index) {
 		selectTool(tools.get(index).toString());
 	}
 
 	public void selectTool(String name) {
 		if (currentTool != null) {
 			currentTool.deactivate();
 			currentTool = null;
 		}
 
 		toolsView.removeAllViews();
 
 		MapTool tool = getTool(name);
 		View ui = tool.getUI();
 		if (ui != null) {
 			toolsView.addView(ui);
 		}
 		tool.activate();
 
 		currentTool = tool;
 	}
 
 	public void selectDefaultTool() {
 		selectTool(CreatePointTool.NAME);
 	}
 
 	public void updateLayers() {
 		if (toolsEnabled && currentTool != null) {
 			currentTool.onLayersChanged();
 		}
 	}
 
 	public void setMapListener(CustomMapListener customMapListener) {
 		this.mapListener = customMapListener;
 	}
 
 	public void setSelectedLayer(Layer layer) {
 		selectedLayer = layer;
 	}
 
 	public Layer getSelectedLayer() {
 		return selectedLayer;
 	}
 
 	public void updateRenderer() {
 		if (getComponents() != null) {
 			getComponents().mapRenderers.getMapRenderer().frustumChanged();
 		}
 	}
 
 	public void addHighlight(int geomId) throws Exception {
 		addHighlight(getGeometry(geomId));
 	}
 
 	public void addHighlight(Geometry geom) throws Exception {
 		if (geom == null) {
 			throw new MapException("Geometry does not exist");
 		}
 		
 		if (hasHighlight(geom)) return;
 		
 		if (transformGeometryList != null) {
 			throw new MapException("Geometry highlight is locked");
 		}
 		
 		highlightGeometryList.add(geom);
 		updateDrawView();
 	}
 
 	public void clearHighlights() throws Exception {
 		if (highlightGeometryList.isEmpty()) return;
 		
 		if (transformGeometryList != null) {
 			throw new MapException("Geometry highlight is locked");
 		}
 		
 		highlightGeometryList = new ArrayList<Geometry>();
 		updateDrawView();
 	}
 	
 	public void removeHighlight(int geomId) throws Exception {
 		removeHighlight(getGeometry(geomId));
 	}
 	
 	public void removeHighlight(Geometry geom) throws Exception {
 		if (highlightGeometryList.isEmpty()) return;
 		
 		if (transformGeometryList != null) {
 			throw new MapException("Geometry highlight is locked");
 		}
 		
 		GeometryData data = (GeometryData) geom.userData;
 		for (ListIterator<Geometry> iterator = highlightGeometryList.listIterator(); iterator.hasNext();) {
 			Geometry g = iterator.next();
 			GeometryData d = (GeometryData) g.userData;
 			if (d.equals(data)) {
 				iterator.remove();
 				break;
 			}
 		}
 		updateDrawView();
 	}
 	
 	public boolean hasHighlight(Geometry geom) {
 		GeometryData data = (GeometryData) geom.userData;
 		for (Geometry g :  highlightGeometryList) {
 			GeometryData d = (GeometryData) g.userData;
 			if (d.equals(data)) return true;
 		}
 		return false;
 	}
 	
 	public List<Geometry> getHighlights() {
 		return highlightGeometryList;
 	}
	
	public void setHighlights(List<Geometry> geomList) {
		highlightGeometryList.clear();
		highlightGeometryList.addAll(geomList);
	}
 
 	public void updateHighlights() throws Exception {
 		if (highlightGeometryList.isEmpty()) return;
 		
 		// note: remove geometry from list that no longer exist or are not visible and update others
 		for (ListIterator<Geometry> iterator = highlightGeometryList.listIterator(); iterator.hasNext();) {
 			Geometry geom = getGeometry(getGeometryId(iterator.next()));
 			if (geom == null) {
 				iterator.remove();
 			} else {
 				CanvasLayer canvas = (CanvasLayer) geometryIdToLayerMap.get(getGeometryId(geom));
 				if (!canvas.isVisible()) {
 					iterator.remove();
 				} else {
 					iterator.set(geom);
 				}
 			}
 		}
 		
 		updateDrawView();
 	}
 	
 	public void prepareHighlightTransform() {
 		// keep a copy of the geometry at the current position
 		transformGeometryList = GeometryUtil.transformGeometryList(highlightGeometryList, this, true);
 		
 		updateDrawView();
 	}
 	
 	public void doHighlightTransform() throws Exception {
 		if (transformGeometryList == null) return;
 		
 		ArrayList<Geometry> geomList = GeometryUtil.transformGeometryList(transformGeometryList, this, false);
 		
 		for (int i = 0; i < highlightGeometryList.size(); i++) {
 			Geometry transformedGeom = geomList.get(i);
 			
 			Geometry geom = highlightGeometryList.get(i);
 			GeometryData data = (GeometryData) geom.userData;
 			if (data.id == null) {
 			
 				CanvasLayer layer = (CanvasLayer) geometryIdToLayerMap.get(getGeometryId(geom));
 				layer.removeGeometry(geom);
 				removeGeometryWithoutClearing(geom);
 				layer.addGeometry(transformedGeom);
 				addGeometry(layer, transformedGeom);
 			
 			}
 			
 			saveGeometry(GeometryUtil.convertGeometryToWgs84(transformedGeom));
 		}
 		
 		transformGeometryList = null;
 		highlightGeometryList = geomList;
 		
 		updateDrawView();
 	}
 	
 	public void saveGeometry(Geometry geom) {
 		try {
 			GeometryData data = (GeometryData) geom.userData;
 			
 			if (data.id == null) return;
 			
 			ArrayList<Geometry> geomList = new ArrayList<Geometry>();
 			geomList.add(geom);
 			
 			if (data.type == GeometryData.Type.ENTITY) {
 				activityRef.get().getDatabaseManager().updateArchEnt(data.id, WKTUtil.collectionToWKT(geomList));
 			} else if (data.type == GeometryData.Type.RELATIONSHIP) {
 				activityRef.get().getDatabaseManager().updateRel(data.id, WKTUtil.collectionToWKT(geomList));
 			}
 			
 			this.updateRenderer();
 		} catch (Exception e) {
 			FLog.e("error saving geometry", e);
 		}
 	}
 	
 	public void deleteGeometry(Geometry geom) {
 		try {
 			GeometryData data = (GeometryData) geom.userData;
 			
 			if (data.id == null) return;
 			
 			if (data.type == GeometryData.Type.ENTITY) {
 				activityRef.get().getDatabaseManager().deleteArchEnt(data.id);
 			} else if (data.type == GeometryData.Type.RELATIONSHIP) {
 				activityRef.get().getDatabaseManager().deleteRel(data.id);
 			}
 			clearHighlights();
 			this.updateRenderer();
 		} catch (Exception e) {
 			FLog.e("error deleting geometry", e);
 		}
 	}
 
 	public void clearHighlightTransform() {
 		transformGeometryList = null;
 		updateRenderer();
 	}
 	
 	private void updateDrawView() {
 		editView.setDrawList(transformGeometryList);
 		drawView.setDrawList(highlightGeometryList);
 	}
 
 	public int getDrawViewColor() {
 		return drawView.getColor();
 	}
 
 	public void setDrawViewColor(int color) {
 		drawView.setColor(color);
 		updateDrawView();
 	}
 	
 	public int getEditViewColor() {
 		return editView.getColor();
 	}
 
 	public void setEditViewColor(int color) {
 		editView.setColor(color);
 		updateDrawView();
 	}
 	
 	public float getDrawViewStrokeStyle() {
 		return drawView.getStrokeSize();
 	}
 	
 	public void setDrawViewStrokeStyle(float strokeSize) {
 		drawView.setStrokeSize(strokeSize);
 		updateDrawView();
 	}
 	
 	public float getEditViewStrokeStyle() {
 		return editView.getStrokeSize();
 	}
 	
 	public void setEditViewStrokeStyle(float strokeSize) {
 		editView.setStrokeSize(strokeSize);
 		updateDrawView();
 	}
 	
 	public float getDrawViewTextSize() {
 		return drawView.getTextSize();
 	}
 	
 	public void setDrawViewTextSize(float value) {
 		drawView.setTextSize(value);
 		updateDrawView();
 	}
 	
 	public float getEditViewTextSize() {
 		return editView.getTextSize();
 	}
 	
 	public void setEditViewTextSize(float value) {
 		editView.setTextSize(value);
 		updateDrawView();
 	}
 
 	public boolean hasTransformGeometry() {
 		return transformGeometryList != null;
 	}
 
 	public void setDrawViewDetail(boolean value) {
 		drawView.setShowDetail(value);
 		updateDrawView();
 	}
 	
 	public void setEditViewDetail(boolean value) {
 		editView.setShowDetail(value);
 		updateDrawView();
 	}
 
 	public boolean showDecimal() {
 		return showDecimal;
 	}
 	
 	public void setShowDecimal(boolean value) {
 		showDecimal = value;
 		drawView.showDecimal(value);
 		editView.showDecimal(value);
 		updateDrawView();
 	}
 
 	public boolean showKm() {
 		return showKm;
 	}
 
 	public void setShowKm(boolean value) {
 		showKm = value;
 	}
 	
 	public void setToolsEnabled(boolean value) {
 		toolsEnabled = value;
 		toolsView.setVisibility(value ? View.VISIBLE : View.GONE);
 		mapLayout.getLayerButton().setVisibility(value ? View.VISIBLE : View.GONE);
 		mapLayout.getSetButton().setVisibility(value ? View.VISIBLE : View.GONE);
 		mapLayout.getToolsDropDown().setVisibility(value ? View.VISIBLE : View.GONE);
 		if (currentTool != null) {
 			if (value) {
 				currentTool.activate();
 			} else {
 				currentTool.deactivate();
 			}
 		}
 	}
 	
 	private void startMapOverlayThread() {
 		startThread(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					FLog.d("starting map overlay thread");
 					
 					Thread.sleep(1000);
 					while(CustomMapView.this.canRunThreads()) {
 						activityRef.get().runOnUiThread(new Runnable() {
 							
 							@Override
 							public void run() {
 								CustomMapView.this.updateMapOverlay();
 								CustomMapView.this.updateMapMarker();
 								// update tool
 								if (toolsEnabled && currentTool != null) {
 									currentTool.onMapUpdate();
 								}
 							}
 							
 						});
 						Thread.sleep(500);
 					}
 					
 					FLog.d("stopping map overlay thread");
 				} catch (Exception e) {
 					FLog.e("error on map overlay thread", e);
 				}
 			}
         	
         });
 	}
 	
 	private void updateMapMarker() {
 		if (currentPositionLayer != null && previousLocation != null) {
 			MapPos p = new MapPos(previousLocation.getLongitude(), previousLocation.getLatitude());
 			MarkerStyle style = createMarkerStyle(previousHeading, locationValid);
 			if (gpsMarker == null) {
 				gpsMarker = new DynamicMarker(p, null, style, null);
 				currentPositionLayer.clear();
 				currentPositionLayer.add(gpsMarker);
 			}
 			gpsMarker.setMapPos(GeometryUtil.convertFromWgs84(p));
 			gpsMarker.setStyle(style);
 		}
 	}
 	
 	private MarkerStyle createMarkerStyle(Float heading, boolean valid) {
 		if (heading != null) {
 			if (tempBitmap != null) {
 				tempBitmap.recycle();
 			}
 			this.tempBitmap = BitmapUtil.rotateBitmap(valid ? blueArrow : greyArrow, heading + this.getRotation());
 	        return MarkerStyle.builder().setBitmap(tempBitmap)
 	                .setSize(0.5f).setAnchorX(MarkerStyle.CENTER).setAnchorY(MarkerStyle.CENTER).build();
 		} else {
 			return MarkerStyle.builder().setBitmap(valid ? blueDot : greyDot)
 	                .setSize(0.8f).setAnchorX(MarkerStyle.CENTER).setAnchorY(MarkerStyle.CENTER).build();
 		}
 	}
 	
 	private void startGPSLocationThread() {
 		startThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 					FLog.d("starting map gps thread");
 					
 					Thread.sleep(1000);
 					while(CustomMapView.this.canRunThreads()) {
 						Object currentLocation = CustomMapView.this.gpsDataManager.getGPSPosition();
 						Object currentHeading = CustomMapView.this.gpsDataManager.getGPSHeading();
 						//currentLocation = new GPSLocation(150.89, -33.85, 0);
 						//currentHeading = 26.0f;
 						if(currentLocation != null){
 							GPSLocation location = (GPSLocation) currentLocation;
 							Float heading = (Float) currentHeading;
 							previousLocation = location;
 							previousHeading = heading;
 							locationValid = true;
 						} else {
 							if(previousLocation != null){
 								// when there is no gps signal for two minutes, change the color of the marker to be grey
 								if(System.currentTimeMillis() - previousLocation.getTimeStamp() > FaimsSettings.GPS_MARKER_TIMEOUT){
 				                    locationValid = false;
 								}
 							}
 						}
 						
 						// update action bar
 						updateActionBar();
 						
 						Thread.sleep(CustomMapView.this.gpsDataManager.getGpsUpdateInterval() * 1000);
 					}
 					FLog.d("stopping map gps thread");
 				} catch (Exception e) {
 					FLog.e("error on map gps thread", e);
 				}
 			}
 		});
 	}
 	
 	private void updateActionBar() {
 		if (previousLocation != null && activityRef.get() != null) {
 			activityRef.get().runOnUiThread(new Runnable() {
 	
 				@Override
 				public void run() {
 					if (geomToFollow != null) {
 						try {
 							MapPos currentPoint = getCurrentPosition();
 							if (currentPoint == null) return;
 							
 							MapPos targetPoint = nextPointToFollow(currentPoint, getPathBuffer());
 							
 							Geometry geom = getGeomToFollow();
 							Line line = (geom instanceof Line) ? (Line) geom : null;
 							
 							activityRef.get().setPathDistance((float) SpatialiteUtil.distanceBetween(currentPoint, targetPoint, activityRef.get().getProject().getSrid()));
 							activityRef.get().setPathIndex(line == null ? -1 : line.getVertexList().indexOf(targetPoint) + 1, line == null ? -1 : line.getVertexList().size());
 							activityRef.get().setPathBearing(SpatialiteUtil.computeAzimuth(currentPoint, targetPoint));
 							activityRef.get().setPathHeading(previousHeading);
 							activityRef.get().setPathValid(locationValid);
 							activityRef.get().setPathVisible(true);
 						} catch (Exception e) {
 							FLog.e("error updating action bar", e);
 						}
 					} else {
 						activityRef.get().setPathVisible(false);
 					}
 				}
 				
 			});
 		}
 	}
 	
 	public void orderLayers() {
 		setAllLayers(getAllLayers());
 	}
 
 	public List<Layer> getAllLayers() {
 		List<Layer> layers = getLayers().getAllLayers();
 		List<Layer> tempLayers = new ArrayList<Layer>();
 		for (Layer layer : layers) {
 			if ((layer instanceof SpatialiteTextLayer) || (layer instanceof DatabaseTextLayer) || (layer instanceof MarkerLayer)) {
 				// ignore
 			} else {
 				if (layer instanceof CanvasLayer && ((CanvasLayer) layer).getLayerId() == vertexLayerId) {
 					// ignore
 				} else {
 					tempLayers.add(layer);
 				}
 			}
 		}
 		return tempLayers;
 	}
 	
 	public void setAllLayers(List<Layer> layers) {
 		List<Layer> tempLayers = new ArrayList<Layer>();
 		for (Layer layer : layers) {
 			if (layer instanceof CustomSpatialiteLayer) {
 				tempLayers.add(layer);
 				tempLayers.add(((CustomSpatialiteLayer) layer).getTextLayer());
 			} else if (layer instanceof DatabaseLayer) {
 				tempLayers.add(layer);
 				tempLayers.add(((DatabaseLayer) layer).getTextLayer());
 			} else if (layer == this.getLayers().getBaseLayer()) {
 				// ignore
 			} else {
 				tempLayers.add(layer);
 			}
 		}
 		if (currentPositionLayer != null) {
 			tempLayers.add(currentPositionLayer);
 		}
 		tempLayers.add(getLayer(vertexLayerId));
 		this.getLayers().setLayers(tempLayers);
 	}
 	
 	public void debugAllLayers() {
 		for (Layer layer : this.getLayers().getAllLayers()) {
 			FLog.d("layer is " + layer.getClass() + " and visiblility is " + layer.isVisible());
 		}
 	}
 	
 	public void setLayerVisible(int layerId, boolean value) throws Exception {
 		setLayerVisible(getLayer(layerId), value);
 	}
 	
 	public void setLayerVisible(Layer layer, boolean value) throws Exception {
 		if (layer == null) {
 			throw new MapException("Layer does not exist");
 		}
 		
 		layer.setVisible(value);
 		updateLayers();
 	}
 	
 	public void addDatabaseLayerQuery(String name, String sql) {
 		databaseLayerQueryMap.put(name, sql);
 		databaseLayerQueryList.add(name);
 	}
 
 	public void addTrackLogLayerQuery(String name, String sql){
 		trackLogQueryName = name;
 		trackLogQuerySql = sql;
 	}
 
 	public String getDatabaseLayerQuery(String name) {
 		return databaseLayerQueryMap.get(name);
 	}
 	
 	public List<String> getDatabaseLayerQueryNames() {
 		return databaseLayerQueryList;
 	}
 
 	public void showSelectionDialog() {
 		selectionDialog = new SelectionDialog(this.activityRef.get());
 		selectionDialog.setTitle("Selection Manager");
 		selectionDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Done", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// ignore
 			}
 		});
 		selectionDialog.attachToMap(this);
 		selectionDialog.show();
 	}
 
 	public ShowProjectActivity getActivity() {
 		return activityRef.get();
 	}
 	
 	public void validateSelectionName(String name) throws MapException {
 		if (name == null || "".equals(name)) {
 			throw new MapException("Please specify a name for the selection");
 		} else if (selectionMap.containsKey(name)) {
 			throw new MapException("Selection already exists");
 		}
 	}
 	
 	public void addSelection(String name) throws MapException {
 		addSelection(name, new GeometrySelection(name));
 	}
 	
 	public void addSelection(String name, GeometrySelection set) throws MapException {
 		validateSelectionName(name);
 		selectionMap.put(name, set);
 	}
 	
 	public void removeSelection(GeometrySelection set) {
 		if (set != null) {
 			selectionMap.remove(set.getName());
 		}
 	}
 	
 	public void removeSelection(String name) {
 		selectionMap.remove(name);
 		
 		if (selectedSelection != null && selectedSelection.getName().equals(name)) {
 			selectedSelection = null;
 			updateSelections();
 		}else if(restrictedSelection != null && restrictedSelection.getName().equals(name)){
 			restrictedSelection = null;
 			updateSelections();
 		}
 	}
 	
 	public List<GeometrySelection> getSelections() {
 		return new ArrayList<GeometrySelection>(selectionMap.values());
 	}
 
 	public void renameSelection(String name, GeometrySelection set) throws MapException {
 		validateSelectionName(name);
 		removeSelection(set);
 		set.setName(name);
 		addSelection(name, set);
 	}
 
 	public void setSelectionActive(GeometrySelection selection,
 			boolean active) {
 		selection.setActive(active);
 		updateSelections();
 	}
 	
 	public GeometrySelection getSelectedSelection() {
 		return selectedSelection;
 	}
 	
 	public GeometrySelection getRestrictedSelection() {
 		return restrictedSelection;
 	} 
 	
 	public void addToSelection(String data){
 		this.selectedSelection.addData(data);
 	}
 	
 	public void removeFromSelection(String data){
 		this.selectedSelection.removeData(data);
 	}
 
 	public void setSelectedSelection(GeometrySelection set) {
 		this.selectedSelection = set;
 	}
 	
 	public void setRestrictedSelection(GeometrySelection set) {
 		this.restrictedSelection = set;
 	}
 	
 	public void updateSelections() {
 		if (currentTool != null) {
 			currentTool.onSelectionChanged();
 		}
 		updateRenderer();
 	}
 
 	public void addSelectQueryBuilder(String name, QueryBuilder builder) {
 		builder.setName(name);
 		selectQueryMap.put(name, builder);
 		selectQueryList.add(builder);
 	}
 
 	public List<QueryBuilder> getSelectQueryBuilders() {
 		return selectQueryList;
 	}
 
 	public void runSelectionQuery(String name, ArrayList<String> values,
 			boolean remove) throws Exception {
 		if (selectedSelection == null) {
 			throw new MapException("Please select a selection");
 		}
 		
 		QueryBuilder qb = selectQueryMap.get(name);
 		this.lastSelectionQuery = name;
 		if (qb == null) {
 			throw new MapException("Query does not exist");
 		}
 		List<String> uuids = null;
 		try {
 			uuids = databaseManager.runSelectionQuery(qb.getSql(), values);
 		} catch (Exception e) {
 			FLog.e("error running selection query", e);
 			throw new MapException("Exception raised while trying to run query");
 		}
 		
 		if (remove) {
 			for (String uuid : uuids) {
 				if(getRestrictedSelection()!= null && getRestrictedSelection().hasData(uuid)){
 					removeFromSelection(uuid);
 				}else if(getRestrictedSelection() == null){
 					removeFromSelection(uuid);
 				}
 			}
 		} else {
 			for (String uuid : uuids) {
 				if(getRestrictedSelection()!= null && getRestrictedSelection().hasData(uuid)){
 					addToSelection(uuid);
 				}else if(getRestrictedSelection() == null){
 					addToSelection(uuid);
 				}
 			}
 		}
 		updateSelections();
 	}
 	
 	public String getLastSelectionQuery() {
 		return lastSelectionQuery;
 	}
 
 	public void addLegacySelectQueryBuilder(String name, String dbPath, String tableName, LegacyQueryBuilder builder) {
 		builder.setName(name);
 		builder.setDbPath(dbPath);
 		builder.setTableName(tableName);
 		legacySelectQueryMap.put(name, builder);
 		legacySelectQueryList.add(builder);
 	}
 	
 	public List<LegacyQueryBuilder> getLegacySelectQueryBuilders() {
 		return legacySelectQueryList;
 	}
 	
 	public void runLegacySelectionQuery(String name, ArrayList<String> values,
 			boolean remove) throws Exception {
 		if (selectedSelection == null) {
 			throw new MapException("Please select a selection");
 		}
 		
 		LegacyQueryBuilder qb = legacySelectQueryMap.get(name);
 		this.lastSelectionQuery = name;
 		if (qb == null) {
 			throw new MapException("Query does not exist");
 		}
 		List<String> uuids = null;
 		try {
 			uuids = databaseManager.runLegacySelectionQuery(qb.getDbPath(), qb.getTableName(), qb.getSql(), values);
 		} catch (Exception e) {
 			FLog.e("error running legacy selection query", e);
 			throw new MapException("Exception raised while trying to run query");
 		}
 		
 		if (remove) {
 			for (String uuid : uuids) {
 				if(getRestrictedSelection()!= null && getRestrictedSelection().hasData(uuid)){
 					removeFromSelection(uuid);
 				}else if(getRestrictedSelection() == null){
 					removeFromSelection(uuid);
 				}
 			}
 		} else {
 			for (String uuid : uuids) {
 				if(getRestrictedSelection()!= null && getRestrictedSelection().hasData(uuid)){
 					addToSelection(uuid);
 				}else if(getRestrictedSelection() == null){
 					addToSelection(uuid);
 				}
 			}
 		}
 		updateSelections();
 	}
 
 	public String getTrackLogQueryName() {
 		return trackLogQueryName;
 	}
 
 	public String getTrackLogQuerySql() {
 		return trackLogQuerySql;
 	}
 
 	public void runPointSelection(Point point, float distance, boolean remove) throws Exception {
 		if (selectedSelection == null) {
 			throw new MapException("Please select a selection");
 		}
 		
 		List<String> uuids = new ArrayList<String>();
 		String srid = activityRef.get().getProject().getSrid();
 		try {
 			uuids.addAll(databaseManager.runDistanceEntityQuery(point, distance, srid));
 			uuids.addAll(databaseManager.runDistanceRelationshipQuery(point, distance, srid));
 			
 			// for each legacy data layer do point distance query
 			List<Layer> layers = getAllLayers();
 			for (Layer layer : layers) {
 				if (layer instanceof CustomSpatialiteLayer) {
 					CustomSpatialiteLayer spatialLayer = (CustomSpatialiteLayer) layer;
 					uuids.addAll(databaseManager.runDistanceLegacyQuery(spatialLayer.getDbPath(), 
 							spatialLayer.getTableName(), spatialLayer.getIdColumn(), spatialLayer.getGeometryColumn(), point, distance, srid));
 				}
 			}
 			
 		} catch (Exception e) {
 			FLog.e("error running point selection query", e);
 			throw new MapException("Exception raised while trying to run point selection");
 		}
 		
 		if (remove) {
 			for (String uuid : uuids) {
 				if(getRestrictedSelection()!= null && getRestrictedSelection().hasData(uuid)){
 					removeFromSelection(uuid);
 				}else if(getRestrictedSelection() == null){
 					removeFromSelection(uuid);
 				}
 			}
 		} else {
 			for (String uuid : uuids) {
 				if(getRestrictedSelection()!= null && getRestrictedSelection().hasData(uuid)){
 					addToSelection(uuid);
 				}else if(getRestrictedSelection() == null){
 					addToSelection(uuid);
 				}
 			}
 		}
 		updateSelections();
 	}
 	
 	public void runPolygonSelection(Polygon polygon, float distance, boolean remove) throws Exception {
 		if (selectedSelection == null) {
 			throw new MapException("Please select a selection");
 		}
 		
 		List<String> uuids = new ArrayList<String>();
 		String srid = activityRef.get().getProject().getSrid();
 		try {
 			uuids.addAll(databaseManager.runDistanceEntityQuery(polygon, distance, srid));
 			uuids.addAll(databaseManager.runDistanceRelationshipQuery(polygon, distance, srid));
 			
 			// for each legacy data layer do point distance query
 			List<Layer> layers = getAllLayers();
 			for (Layer layer : layers) {
 				if (layer instanceof CustomSpatialiteLayer) {
 					CustomSpatialiteLayer spatialLayer = (CustomSpatialiteLayer) layer;
 					uuids.addAll(databaseManager.runDistanceLegacyQuery(spatialLayer.getDbPath(), 
 							spatialLayer.getTableName(), spatialLayer.getIdColumn(), spatialLayer.getGeometryColumn(), polygon, distance, srid));
 				}
 			}
 			
 		} catch (Exception e) {
 			FLog.e("error running polygon selection query", e);
 			throw new MapException("Exception raised while trying to run polygon selection");
 		}
 		
 		if (remove) {
 			for (String uuid : uuids) {
 				if(getRestrictedSelection()!= null && getRestrictedSelection().hasData(uuid)){
 					removeFromSelection(uuid);
 				}else if(getRestrictedSelection() == null){
 					removeFromSelection(uuid);
 				}
 			}
 		} else {
 			for (String uuid : uuids) {
 				if(getRestrictedSelection()!= null && getRestrictedSelection().hasData(uuid)){
 					addToSelection(uuid);
 				}else if(getRestrictedSelection() == null){
 					addToSelection(uuid);
 				}
 			}
 		}
 		updateSelections();
 	}
 
 	public void runIntersectionSelection(Collection<Geometry> geometries, boolean remove) throws Exception {
 		if (selectedSelection == null) {
 			throw new MapException("Please select a selection");
 		}
 		
 		List<String> uuids = new ArrayList<String>();
 		try {
 			for(Geometry geometry : geometries){
 				uuids.addAll(databaseManager.runIntersectEntityQuery(geometry));
 				uuids.addAll(databaseManager.runIntersectRelationshipQuery(geometry));
 				
 				// for each legacy data layer do point distance query
 				List<Layer> layers = getAllLayers();
 				for (Layer layer : layers) {
 					if (layer instanceof CustomSpatialiteLayer) {
 						CustomSpatialiteLayer spatialLayer = (CustomSpatialiteLayer) layer;
 						uuids.addAll(databaseManager.runIntersectLegacyQuery(spatialLayer.getDbPath(), 
 								spatialLayer.getTableName(), spatialLayer.getIdColumn(), spatialLayer.getGeometryColumn(), geometry));
 					}
 				}
 			}
 			
 		} catch (Exception e) {
 			FLog.e("error running polygon intersection selection query", e);
 			throw new MapException("Exception raised while trying to run polygon intersection selection");
 		}
 		
 		if (remove) {
 			for (String uuid : uuids) {
 				if(getRestrictedSelection()!= null && getRestrictedSelection().hasData(uuid)){
 					removeFromSelection(uuid);
 				}else if(getRestrictedSelection() == null){
 					removeFromSelection(uuid);
 				}
 			}
 		} else {
 			for (String uuid : uuids) {
 				if(getRestrictedSelection()!= null && getRestrictedSelection().hasData(uuid)){
 					addToSelection(uuid);
 				}else if(getRestrictedSelection() == null){
 					addToSelection(uuid);
 				}
 			}
 		}
 		updateSelections();
 	}
 
 	public void setGeomToFollow(Geometry geom) {
 		this.geomToFollow = geom;
 		updateGeomBuffer();
 		updateActionBar();
 	}
 	
 	public Geometry getGeomToFollow() {
 		return geomToFollow;
 	}
 
 	public MapPos nextPointToFollow(MapPos pos, float buffer) throws Exception {
 		if (geomToFollow instanceof Point) {
 			return ((Point) geomToFollow).getMapPos();
 		} else if (geomToFollow instanceof Line) {
 			Line line = (Line) geomToFollow;
 			Point point = new Point(pos, null, (PointStyle) null, null);
 			MapPos lp = line.getVertexList().get(line.getVertexList().size()-1);
 			MapPos mp = lp;
 			double min = SpatialiteUtil.distanceBetween(pos,  lp, activityRef.get().getProject().getSrid());
 			for (int i = line.getVertexList().size()-2; i >= 0; i--) {
 				MapPos p = line.getVertexList().get(i);
 				ArrayList<MapPos> pts = new ArrayList<MapPos>();
 				pts.add(p);
 				pts.add(lp);
 				Line seg = new Line(pts, null, (LineStyle) null, null);
 				if (SpatialiteUtil.isPointOnPath(point, seg, buffer, activityRef.get().getProject().getSrid())) {
 					return lp;
 				} else {
 					double d = SpatialiteUtil.distanceBetween(pos, p, activityRef.get().getProject().getSrid());
 					if (d < min) {
 						min = d;
 						mp = p;
 					}
 				}
 				lp = p;
 			}
 			return mp;
 		} else {
 			return null;
 		}
 	}
 
 	public float getPathBuffer() {
 		return buffer;
 	}
 	
 	public void setPathBuffer(float value) {
 		this.buffer = value;
 		updateGeomBuffer();
 	}
 	
 	private void updateGeomBuffer() {
 		if (geomToFollow != null) {
 			try {
 				geomToFollowBuffer = SpatialiteUtil.geometryBuffer(geomToFollow, buffer, activityRef.get().getProject().getSrid());
 			} catch (Exception e) {
 				FLog.e("error getting geometry buffer", e);
 			}
 		} else {
 			geomToFollowBuffer = null;
 		}
 	}
 	
 	public Geometry getGeomToFollowBuffer() {
 		return geomToFollowBuffer;
 	}
 
 	public MapPos getCurrentPosition() {
 		GPSLocation location = previousLocation;
 		if (location == null) {
 			return null;
 		}
 		return new MapPos(location.getLongitude(), location.getLatitude());
 	}
 	
 	public Float getCurrentHeading() {
 		Float heading = previousHeading;
 		if (heading == null) {
 			return null;
 		}
 		return heading;
 	}
 	
 	public CreateCallback getCreateCallback() {
 		return createCallback;
 	}
 
 	public void setCreateCallback(CreateCallback createCallback) {
 		this.createCallback = createCallback;
 	}
 	
 	public LoadCallback getLoadCallback() {
 		return loadCallback;
 	}
 
 	public void setLoadCallback(LoadCallback loadCallback) {
 		this.loadCallback = loadCallback;
 	}
 
 	public void notifyGeometryCreated(Geometry geom) {
 		GeometryData data = (GeometryData) geom.userData;
 		if (createCallback != null) {
 			createCallback.onCreate(data.geomId);
 		}
 	}
 
 	public void notifyGeometryLoaded(Geometry geom) {
 		GeometryData data = (GeometryData) geom.userData;
 		if (loadCallback != null) {
 			loadCallback.onLoad(data.id, data.type == GeometryData.Type.ENTITY);
 		}
 	}
 	
 	public int getVertexLayerId() {
 		return vertexLayerId;
 	}
 
 	// note: temporarily disable database layer loading geom
 	public void hideGeometry(Geometry geom) {
 		GeometryData data = (GeometryData) geom.userData;
 		if (data.id == null) {
 			FLog.d("geometry must have id");
 			return;
 		}
 		Layer layer = getLayer(data.layerId);
 		if (layer instanceof DatabaseLayer) {
 			DatabaseLayer dblayer = (DatabaseLayer) layer;
 			dblayer.hideGeometry(data.id);
 		} else {
 			FLog.d("layer must be database layer");
 		}
 	}
 	
 	public void clearHiddenGeometry(Geometry geom) {
 		GeometryData data = (GeometryData) geom.userData;
 		if (data.id == null) {
 			FLog.d("geometry must have id");
 			return;
 		}
 		Layer layer = getLayer(data.layerId);
 		if (layer instanceof DatabaseLayer) {
 			DatabaseLayer dblayer = (DatabaseLayer) layer;
 			dblayer.clearHiddenList();
 		} else {
 			FLog.d("layer must be database layer");
 		}
 	}
 
 	public boolean isProperProjection() {
 		return projectionProper;
 	}
 
 }
