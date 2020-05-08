 /* gvSIG Mini. A free mobile phone viewer of free maps.
  *
  * Copyright (C) 2010 Prodevelop.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *   Prodevelop, S.L.
  *   Pza. Don Juan de Villarrasa, 14 - 5
  *   46001 Valencia
  *   Spain
  *
  *   +34 963 510 612
  *   +34 963 510 968
  *   prode@prodevelop.es
  *   http://www.prodevelop.es
  *
  *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la Peque�a y
  *   Mediana Empresa de la Comunidad Valenciana) &
  *   European Union FEDER funds.
  *   
  *   2010.
  *   author Alberto Romeu aromeu@prodevelop.es
  *   
  */
 
 package es.prodevelop.gvsig.mini.views.overlay;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Hashtable;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.os.Environment;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 import es.prodevelop.android.spatialindex.cluster.Cluster;
 import es.prodevelop.android.spatialindex.poi.POICategories;
 import es.prodevelop.android.spatialindex.quadtree.bucket.mr.MRBucketPRQuadtree;
 import es.prodevelop.android.spatialindex.quadtree.provide.QuadtreeProviderListener;
 import es.prodevelop.android.spatialindex.quadtree.provide.perst.PerstOsmPOIClusterProvider;
 import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
 import es.prodevelop.gvsig.mini.R;
 import es.prodevelop.gvsig.mini.context.ItemContext;
 import es.prodevelop.gvsig.mini.context.map.POIContext;
 import es.prodevelop.gvsig.mini.exceptions.BaseException;
 import es.prodevelop.gvsig.mini.geom.Extent;
 import es.prodevelop.gvsig.mini.geom.Pixel;
 import es.prodevelop.gvsig.mini.geom.Point;
 import es.prodevelop.gvsig.mini.map.ViewPort;
 import es.prodevelop.gvsig.mini.search.POIProviderManager;
 import es.prodevelop.gvsig.mini.symbol.ClusterSymbolSelector;
 import es.prodevelop.gvsig.mini.symbol.OsmPOISymbolSelector;
 import es.prodevelop.gvsig.mini.symbol.SymbolSelector;
 import es.prodevelop.gvsig.mini.util.ResourceLoader;
 import es.prodevelop.gvsig.mini.util.Utils;
 import es.prodevelop.gvsig.mini.utiles.Cancellable;
 import es.prodevelop.gvsig.mini.utiles.Utilities;
 import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;
 import es.prodevelop.tilecache.renderer.MapRenderer;
 
 public class PerstClusterPOIOverlay extends PointOverlay implements
 		QuadtreeProviderListener {
 
 	public final static String DEFAULT_NAME = "POIS_CLUSTER";
 
 	ArrayList clusters = new ArrayList();
 	// ArrayList pois = new ArrayList();
 	PerstOsmPOIClusterProvider poiProvider;
 
 	Pixel firstPOIPixel = null;
 	Pixel lastPOIPixel = null;
 	Pixel oldFirstPOIPixel = null;
 	Pixel oldLastPOIPixel = null;
 
 	protected ArrayList<Pixel> pixelsPOI = new ArrayList<Pixel>();
 
 	private PerstClusterPOIOverlay onlyPointsOverlay;
 	private boolean isCluster = false;
 
 	private ArrayList expandedExtents = new ArrayList();
 
 	public PerstClusterPOIOverlay(Context context, TileRaster tileRaster,
 			String name, boolean isCluster) {
 		super(context, tileRaster, name);
 		try {
 			this.isCluster = isCluster;
 			if (isCluster) {
 				poiProvider = new PerstOsmPOIClusterProvider(
 						Environment.getExternalStorageDirectory()
 								+ File.separator + Utils.TEST_POI_DIR
 								+ File.separator
 								+ "perst_streets_cluster_cat.db", tileRaster
 								.getMRendererInfo().getZOOM_MAXLEVEL()
 								- tileRaster.getMRendererInfo()
 										.getZoomMinLevel(), this, tileRaster
 								.getMRendererInfo().getZOOM_MAXLEVEL());
 				POIProviderManager.getInstance().registerPOIProvider(
 						poiProvider);
 				poiProvider.setCurrentZoomLevel(tileRaster.getZoomLevel());
 
 				this.setSymbolSelector(new ClusterSymbolSelector(this));
 				onlyPointsOverlay = new PerstClusterPOIOverlay(context,
 						tileRaster, name, false);
 				onlyPointsOverlay.setSymbolSelector(new OsmPOISymbolSelector());
 			}
 		} catch (BaseException e) {
 			// TODO Auto-generated catch block
 			Log.e("", e.getMessage());
 		} finally {
 
 		}
 	}
 
 	@Override
 	public ItemContext getItemContext() {
 		return new POIContext(getTileRaster().map);
 	}
 
 	@Override
 	protected void onDraw(Canvas c, TileRaster maps) {
 		super.onDraw(c, maps);
 		if (isCluster)
 			onlyPointsOverlay.onDraw(c, maps);
 	}
 
 	@Override
 	protected void draw(final Point p, final MapRenderer renderer,
 			final Canvas c) {
 		try {
 			Bitmap expandableIcon = null;
 			if (p instanceof Cluster) {
 				if (((Cluster) p).isExpanded())
 					return;
 				int numItems = ((Cluster) p).getNumItems();
 				if (numItems < MRBucketPRQuadtree.EXPAND_CLUSTER_ITEMS)
 					return;
 				if (((Cluster) p).isExpandable())
 					expandableIcon = ResourceLoader
 							.getBitmap(R.drawable.add_16);
 			}
 
 			final Extent viewExtent = getTileRaster().getMRendererInfo()
 					.getCurrentExtent();
 
 			if (!viewExtent.contains(p.getX(), p.getY()))
 				return;
 			int[] coords = renderer
 					.toPixels(new double[] { p.getX(), p.getY() });
 
 			Bitmap icon = getSymbolSelector().getSymbol(p);
 			int[] midIcon = getSymbolSelector().getMidSymbol(p);
 
 			if (icon != null)
 				c.drawBitmap(icon, coords[0] - midIcon[0], coords[1]
 						- midIcon[1], Paints.mPaintR);
 
 			if (expandableIcon != null)
 				c.drawBitmap(expandableIcon, coords[0] - midIcon[0] - 8,
 						coords[1] - midIcon[1] - 8, Paints.mPaintR);
 		} catch (Exception e) {
 			Log.e("", e.getMessage());
 		}
 	}
 
 	@Override
 	protected void onDrawFinished(Canvas c, TileRaster maps) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public int getSelectedIndex() {
 		if (isCluster) {
 			if (poiProvider.shouldShowPOIS(getTileRaster().getZoomLevel())) {
 				return onlyPointsOverlay.getSelectedIndex();
 			} else {
 				return super.getSelectedIndex();
 			}
 		} else {
 			return super.getSelectedIndex();
 		}
 	}
 
 	public void setSelectedIndex(int selectedIndex) {
 		if (isCluster) {
 			if (poiProvider.shouldShowPOIS(getTileRaster().getZoomLevel())) {
 				onlyPointsOverlay.setSelectedIndex(selectedIndex);
 				return;
 			} else {
 				super.setSelectedIndex(selectedIndex);
 				return;
 			}
 		} else {
 			super.setSelectedIndex(selectedIndex);
 			return;
 		}
 	}
 
 	public SymbolSelector getSymbolSelector() {
 		if (isCluster) {
 			if (poiProvider.shouldShowPOIS(getTileRaster().getZoomLevel())) {
 				return onlyPointsOverlay.getSymbolSelector();
 			} else {
 				return super.getSymbolSelector();
 			}
 		}
 		return super.getSymbolSelector();
 	}
 
 	public ArrayList getPoints() {
 		if (isCluster) {
 			if (poiProvider.shouldShowPOIS(getTileRaster().getZoomLevel())) {
 				return onlyPointsOverlay.getPoints();
 			} else {
 				return super.getPoints();
 			}
 		}
 		return super.getPoints();
 	}
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent e, TileRaster osmtile) {
 		try {
 			if (isCluster) {
 				if (poiProvider.shouldShowPOIS(getTileRaster().getZoomLevel())) {
 					return super.onSingleTapUp(e, osmtile);
 				} else {
 					super.onSingleTapUp(e, osmtile);
 					if (getSelectedIndex() == -1) {
 						return false;
 					} else {
 						Cluster p = (Cluster) getPoints().get(
 								getSelectedIndex());
 						if (p.isExpandable()) {
 							ExpandedClusterOverlay ex = new ExpandedClusterOverlay(
 									getTileRaster().map,
 									getTileRaster(),
 									String.valueOf(POICategories.ORDERED_CATEGORIES[p
 											.getCat()]
											+ "#"
 											+ String.valueOf(p.getID())), p,
 									true);
 							ex.setClusterRemovedListener(this);
 							getTileRaster().addOverlay(ex);
 							ex.getPOIsOfClusterAsynch();
 						} else {
 							Toast.makeText(getTileRaster().map,
 									R.string.zoom_to_expand, Toast.LENGTH_SHORT)
 									.show();
 						}
 					}
 					return true;
 				}
 			} else {
 				return super.onSingleTapUp(e, osmtile);
 			}
 		} catch (Exception ex) {
 			return false;
 		}
 	}
 
 	@Override
 	public void destroy() {
 		// poiProvider.getHelper().close();
 		//
 		// firstPOIPixel = null;
 		// lastPOIPixel = null;
 		// oldFirstPOIPixel = null;
 		// oldLastPOIPixel = null;
 		//
 		// int size = pixelsPOI.size();
 		//
 		// Pixel p;
 		// for (int i = 0; i < size; i++) {
 		// p = pixelsPOI.get(i);
 		// p.destroy();
 		// p = null;
 		// pixelsPOI.remove(i);
 		// }
 		//
 		// pixelsPOI = null;
 		//
 		// size = pois.size();
 		//
 		// POI poi;
 		// for (int i = 0; i < size; i++) {
 		// poi = (POI) pois.get(i);
 		// poi.destroy();
 		// poi = null;
 		// pois.remove(i);
 		// }
 		//
 		// pois = null;
 	}
 
 	private boolean cancelDraw = false;
 
 	public synchronized void restoreDraw() {
 		cancelDraw = false;
 	}
 
 	public synchronized void cancelDraw() {
 		cancelDraw = true;
 	}
 
 	public synchronized boolean isCanceledDraw() {
 		return cancelDraw;
 	}
 
 	@Override
 	public void onClustersRetrieved(Collection pois, String category,
 			boolean clearPrevious, final Cancellable cancellable) {
 		// this.cancelDraw();
 		if (clusters == null)
 			clusters = new ArrayList();
 		if (clearPrevious)
 			// if (cancellable == null)
 			// this.clusters = (ArrayList) pois;
 			// else if (!cancellable.getCanceled())
 			this.clusters = (ArrayList) pois;
 
 		else {
 			long t1 = android.os.SystemClock.currentThreadTimeMillis();
 			convertCoordinates("EPSG:4326", getTileRaster().getMRendererInfo()
 					.getSRS(), (ArrayList) pois, cancellable);
 			Log.d("",
 					"Time to convert coordinates: "
 							+ (android.os.SystemClock.currentThreadTimeMillis() - t1));
 			this.clusters.addAll(pois);
 		}
 
 		this.setPoints(clusters);
 		getTileRaster().resumeDraw();
 		// this.restoreDraw();
 	}
 
 	@Override
 	public void onPOISRetrieved(Collection pois, boolean clearPrevious,
 			final Cancellable cancellable) {
 		if (this.onlyPointsOverlay.getPoints() == null)
 			this.onlyPointsOverlay.setPoints(new ArrayList());
 		// this.cancelDraw();
 		if (clearPrevious)
 			// if (cancellable == null)
 			// this.pois = (ArrayList) pois;
 			// else if (!cancellable.getCanceled())
 			this.onlyPointsOverlay.setPoints((ArrayList) pois);
 		else {
 
 			convertCoordinates("EPSG:4326", getTileRaster().getMRendererInfo()
 					.getSRS(), (ArrayList) pois, cancellable);
 
 			ArrayList p = this.onlyPointsOverlay.getPoints();
 			p.addAll(pois);
 			this.onlyPointsOverlay.setPoints(p);
 		}
 
 		getTileRaster().resumeDraw();
 		// this.restoreDraw();
 	}
 
 	/**
 	 * @return the poiProvider
 	 */
 	public PerstOsmPOIClusterProvider getPoiProvider() {
 		return poiProvider;
 	}
 
 	@Override
 	public void onExtentChanged(Extent newExtent, int zoomLevel,
 			double resolution) {
 		final ViewPort vp = getTileRaster().map.vp;
 
 		final MapRenderer renderer = getTileRaster().getMRendererInfo();
 
 		double[] minXY = ConversionCoords.reproject(newExtent.getMinX(),
 				newExtent.getMinY(), CRSFactory.getCRS(renderer.getSRS()),
 				CRSFactory.getCRS("EPSG:4326"));
 		double[] maxXY = ConversionCoords.reproject(newExtent.getMaxX(),
 				newExtent.getMaxY(), CRSFactory.getCRS(renderer.getSRS()),
 				CRSFactory.getCRS("EPSG:4326"));
 
 		final Extent extentGetPOIS = new Extent(minXY[0], minXY[1], maxXY[0],
 				maxXY[1]);
 
 		try {
 			poiProvider.getPOIsAsynch(extentGetPOIS,
 					ViewPort.LAT_LON_RES[zoomLevel] * 64, zoomLevel,
 					Utilities.getNewCancellable(), false);
 		} catch (BaseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onLayerChanged(String layerName) {
 		onExtentChanged(getTileRaster().getMRendererInfo().getCurrentExtent(),
 				getTileRaster().getZoomLevel(), -1);
 	}
 
 	public void setCategories(ArrayList categories) throws BaseException {
 		final ArrayList selectedCategories = poiProvider
 				.getSelectedCategories();
 
 		int size = selectedCategories.size();
 
 		final ArrayList previouslySelectedCategories = new ArrayList(size);
 
 		for (int i = 0; i < size; i++) {
 			previouslySelectedCategories.add(selectedCategories.get(i));
 		}
 
 		if (categories != null && categories.size() > 0) {
 			// poiProvider.setCurrentZoomLevel(getTileRaster().getZoomLevel());
 			poiProvider.setSelectedCategories(categories);
 			this.onExtentChanged(getTileRaster().getMRendererInfo()
 					.getCurrentExtent(), getTileRaster().getZoomLevel(), 0);
 		} else {
 			poiProvider.setSelectedCategories(new ArrayList());
 			// pois.clear();
 			final ArrayList points = getPoints();
 			if (points != null)
 				points.clear();
 			getTileRaster().resumeDraw();
 		}
 
 		String cat;
 		boolean anyRemoved = false;
 		for (int i = 0; i < size; i++) {
 			cat = previouslySelectedCategories.get(i).toString();
 			if (!categories.contains(cat)) {
 				getTileRaster().removeExpanded(cat);
 				anyRemoved = true;
 			}
 		}
 
 		if (anyRemoved)
 			getTileRaster().acetate.setPopupVisibility(View.INVISIBLE);
 	}
 
 	public Hashtable<String, Integer> getMaxClusterSizeCat() {
 		return ((PerstOsmPOIClusterProvider) this.poiProvider)
 				.getMaxClusterSizeCat();
 	}
 
 	@Override
 	public void onClusterExpanded(Collection pois, boolean clearPrevious,
 			Cancellable cancellable, Cluster clusterExpanded) {
 		if (!POICategories.selected
 				.contains(POICategories.ORDERED_CATEGORIES[clusterExpanded
 						.getCat()])) {
 			return;
 		}
 
 		ExpandedClusterOverlay ex = new ExpandedClusterOverlay(
 				getTileRaster().map, getTileRaster(),
 
 				String.valueOf(POICategories.ORDERED_CATEGORIES[clusterExpanded
						.getCat()] + "#" + clusterExpanded.getID()),
 				clusterExpanded, false);
 
 		convertCoordinates("EPSG:4326", getTileRaster().getMRendererInfo()
 				.getSRS(), (ArrayList) pois, cancellable);
 		ex.setPoints((ArrayList) pois);
 		getTileRaster().addOverlay(ex);
 		// ex.getPOIsOfClusterAsynch();
 	}
 }
