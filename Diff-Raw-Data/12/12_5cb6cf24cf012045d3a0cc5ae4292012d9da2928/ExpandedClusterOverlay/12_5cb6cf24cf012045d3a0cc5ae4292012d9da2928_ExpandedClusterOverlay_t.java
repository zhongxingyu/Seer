 package es.prodevelop.gvsig.mini.views.overlay;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.util.Log;
 import android.view.MotionEvent;
 import es.prodevelop.android.spatialindex.cluster.Cluster;
 import es.prodevelop.android.spatialindex.poi.POICategories;
 import es.prodevelop.android.spatialindex.quadtree.memory.cluster.ClusterNode;
 import es.prodevelop.android.spatialindex.quadtree.provide.QuadtreeProviderListener;
 import es.prodevelop.android.spatialindex.quadtree.provide.perst.PerstOsmPOIClusterProvider;
 import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
 import es.prodevelop.gvsig.mini.R;
 import es.prodevelop.gvsig.mini.exceptions.BaseException;
 import es.prodevelop.gvsig.mini.geom.Extent;
 import es.prodevelop.gvsig.mini.geom.Pixel;
 import es.prodevelop.gvsig.mini.search.POIProviderManager;
 import es.prodevelop.gvsig.mini.symbol.OsmPOISymbolSelector;
 import es.prodevelop.gvsig.mini.util.ResourceLoader;
 import es.prodevelop.gvsig.mini.utiles.Cancellable;
 import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;
 import es.prodevelop.tilecache.renderer.MapRenderer;
 
 public class ExpandedClusterOverlay extends PointOverlay implements
 		QuadtreeProviderListener {
 
 	private double visibleResolution = 0;
 
 	private Cluster mClusterToExpand;
 	private PerstOsmPOIClusterProvider mPoiProvider;
 
 	private QuadtreeProviderListener clusterRemovedListener;
 	private boolean showRect = false;
 
 	private String cat = "";
 
 	private Bitmap addIcon;
 	private ClusterNode mNode;
 
 	public ExpandedClusterOverlay(Context context, TileRaster tileRaster,
 			String name, Cluster clusterToExpand, boolean showRect) {
 		super(context, tileRaster, name);
 		this.showRect = showRect;
 		visibleResolution = tileRaster.getMRendererInfo().getCurrentRes();
 		this.mClusterToExpand = clusterToExpand;
 		mPoiProvider = POIProviderManager.getInstance().getPOIProvider();
 		// this.setName(String.valueOf(clusterToExpand.getID()));
 		setSymbolSelector(new OsmPOISymbolSelector());
		cat = name.split("#")[0];
 		addIcon = ResourceLoader.getBitmap(R.drawable.add_48);
 	}
 
 	public void getPOIsOfClusterAsynch() {
 		try {
 			if (checkRemove())
 				return;
 			mPoiProvider.expandClusterAsynch(mClusterToExpand, this);
 		} catch (Exception e) {
 			Log.e("", e.getMessage());
 		}
 	}
 
 	@Override
 	public void onClustersRetrieved(Collection clusters, String category,
 			boolean clearPrevious, Cancellable cancellable) {
 		if (clusterRemovedListener != null)
 			clusterRemovedListener.onClustersRetrieved(clusters, category,
 					clearPrevious, cancellable);
 
 	}
 
 	@Override
 	public void onPOISRetrieved(Collection pois, boolean clearPrevious,
 			Cancellable cancellable) {
 		if (checkRemove())
 			return;
 		convertCoordinates("EPSG:4326", getTileRaster().getMRendererInfo()
 				.getSRS(), (ArrayList) pois, cancellable);
 		this.setPoints((ArrayList) pois);
 	}
 
 	@Override
 	public void onClusterExpanded(Collection pois, boolean clearPrevious,
 			Cancellable cancellable, Cluster clusterExpanded) {
 		onPOISRetrieved(pois, clearPrevious, cancellable);
 	}
 
 	@Override
 	public void onExtentChanged(Extent newExtent, int zoomLevel,
 			double resolution) {
 
 		this.setVisible(resolution <= visibleResolution);
 
 	}
 
 	public void setClusterRemovedListener(
 			QuadtreeProviderListener clusterRemovedListener) {
 		this.clusterRemovedListener = clusterRemovedListener;
 	}
 
 	public QuadtreeProviderListener getClusterRemovedListener() {
 		return clusterRemovedListener;
 	}
 
 	private boolean checkRemove() {
 		if (!POICategories.selected.contains(cat)) {
 			getTileRaster().removeOverlay(getName());
 			destroy();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected void onDraw(Canvas c, TileRaster maps) {
 		super.onDraw(c, maps);
 		final MapRenderer renderer = getTileRaster().getMRendererInfo();
 
 		if (!showRect || !isVisible() || checkRemove())
 			return;
 		Extent boundingBox = this.mClusterToExpand.getBoundingBox();
 		double[] minXY = ConversionCoords.reproject(boundingBox.getMinX(),
 				boundingBox.getMinY(), CRSFactory.getCRS("EPSG:4326"),
 				CRSFactory.getCRS(renderer.getSRS()));
 
 		double[] maxXY = ConversionCoords.reproject(boundingBox.getMaxX(),
 				boundingBox.getMaxY(), CRSFactory.getCRS("EPSG:4326"),
 				CRSFactory.getCRS(renderer.getSRS()));
 
 		int[] minPixXY = renderer.toPixels(minXY);
 		int[] maxPixXY = renderer.toPixels(maxXY);
 
 		c.drawRect((float) minPixXY[0], (float) maxPixXY[1],
 				(float) maxPixXY[0], (float) minPixXY[1], Paints.circlePaint);
 
 		c.drawBitmap(addIcon, maxPixXY[0] - (addIcon.getWidth() / 2),
 				maxPixXY[1] - (addIcon.getHeight() / 2), Paints.mPaintR);
 	}
 
 	public boolean onSingleTapUp(MotionEvent e, TileRaster osmtile) {
 		if (!isVisible())
 			return false;
 		if (showRect) {
 			final MapRenderer renderer = getTileRaster().getMRendererInfo();
 
 			Extent boundingBox = this.mClusterToExpand.getBoundingBox();
 			// double[] minXY =
 			// ConversionCoords.reproject(boundingBox.getMinX(),
 			// boundingBox.getMinY(), CRSFactory.getCRS("EPSG:4326"),
 			// CRSFactory.getCRS(renderer.getSRS()));
 
 			double[] maxXY = ConversionCoords.reproject(boundingBox.getMaxX(),
 					boundingBox.getMaxY(), CRSFactory.getCRS("EPSG:4326"),
 					CRSFactory.getCRS(renderer.getSRS()));
 
 			// int[] minPixXY = renderer.toPixels(minXY);
 			int[] maxPixXY = renderer.toPixels(maxXY);
 
 			Pixel addIconPos = new Pixel(
 					maxPixXY[0] - (addIcon.getWidth() / 2), maxPixXY[1]
 							- (addIcon.getHeight() / 2));
 
 			Pixel p = new Pixel((int) e.getX(), (int) e.getY());
 			if (p.distance(addIconPos) < addIcon.getWidth()) {
 				showRect = false;
 				setNodeExpanded(true);
 			} else {
 				this.getTileRaster().removeOverlay(this.getName());
 				destroy();
 			}
 			return true;
 		} else {
 			return super.onSingleTapUp(e, osmtile);
 		}
 	}
 
 	private void setNodeExpanded(final boolean expanded) {
 		this.mClusterToExpand.setExpanded(expanded);
 		try {
 			if (mNode == null)
 				mNode = mPoiProvider.getClusterTree(cat).getNodeByID(
 						this.mClusterToExpand.getID());
 			mNode.setExpanded(expanded);
 		} catch (BaseException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 
 	public boolean isRemovable(String categoryExpandedName) {
 		return (showRect || getName().startsWith(categoryExpandedName));
 	}
 
 	public boolean isRemovable() {
 		return (showRect);
 	}
 
 	public void destroy() {
 		super.destroy();
 		setNodeExpanded(false);
 	}
 
 	// @Override
 	// public void onLayerChanged(String layerName) {
 	// if (getPoints() == null)
 	// return;
 	// MapRenderer newRenderer;
 	// try {
 	// newRenderer = Layers.getInstance().getRenderer(layerName);
 	// convertCoordinates(renderer.getSRS(), newRenderer.getSRS(),
 	// getPoints(), null);
 	// renderer = newRenderer;
 	// } catch (IOException e) {
 	// // TODO Auto-generated catch block
 	// Log.e("", e.getMessage());
 	// }
 	// }
 
 }
