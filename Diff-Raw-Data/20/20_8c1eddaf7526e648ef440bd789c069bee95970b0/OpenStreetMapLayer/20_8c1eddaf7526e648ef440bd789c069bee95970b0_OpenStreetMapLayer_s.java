 package teropa.globetrotter.client.osm;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import teropa.globetrotter.client.Grid;
 import teropa.globetrotter.client.Grid.Tile;
 import teropa.globetrotter.client.Layer;
 import teropa.globetrotter.client.View;
 import teropa.globetrotter.client.proj.GoogleMercator;
 
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.widgetideas.graphics.client.ImageLoader;
 import com.google.gwt.widgetideas.graphics.client.ImageLoader.CallBack;
 
 public class OpenStreetMapLayer extends Layer {
 
 	// based on the zoom levels at http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
 	public static final double[] SUPPORTED_RESOLUTIONS = {
 		156543.03392804097,
 		78271.51696402048,
 		39135.75848201024,
 		19567.87924100512,
 		9783.93962050256,
 		4891.96981025128,
 		2445.98490512564,
 		1222.99245256282,
 		611.49622628141,
 		305.748113140705,
 		152.8740565703525,
 		76.43702828517625,
 		38.21851414258813,
 		19.109257071294063,
 		9.554628535647032,
 		4.777314267823516,
 		2.388657133911758,
 		1.194328566955879,
 		0.5971642834779395};
 
 	protected final String baseUrl;
 	protected final Map<Tile, ImageElement> images = new HashMap<Grid.Tile, ImageElement>();
 	
 	public OpenStreetMapLayer(String baseUrl, String name, boolean base) {
 		super(name, base, new GoogleMercator());
 		this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
 	}
 	
 	@Override
 	public void onTilesActivated(Collection<Tile> newTiles) {
 		final OpenStreetMapLayer _this = this;
 		for (final Tile each : newTiles) {
 			String url = getUrl(getZoomLevel(), each.getCol(), each.getRow());
 			ImageLoader.loadImages(new String[] { url }, new CallBack() {
 				public void onImagesLoaded(ImageElement[] imageElements) {
					images.put(each, imageElements[0]);
//					context.getView().getCanvas().drawImage(imageElements[0], each.getLeftX(), each.getTopY(), each.getWidth(), each.getHeight());
					context.getView().tileUpdated(each, _this);
 				}
 			});
 		}
 	}
 
 	protected int getZoomLevel() {
 		int res = (int)Math.round(context.getResolution());
 		for (int i=0 ; i<SUPPORTED_RESOLUTIONS.length ; i++) {
 			if (res == (int)Math.round(SUPPORTED_RESOLUTIONS[i])) {
 				return i;
 			}
 		}
 		return 0;
 	}
 	
 	@Override
 	public void onTilesDeactivated(Collection<Tile> removedTiles) {
 		images.keySet().removeAll(removedTiles);
 	}
 	
 	@Override
 	public void onAllTilesDeactivated() {
 		images.clear();
 	}
 	
 	@Override
 	public void updateTile(Tile tile) {
 		ImageElement mine = images.get(tile);
 		if (mine != null) {
 			context.getView().getCanvas().drawImage(mine, tile.getLeftX(), tile.getTopY(), tile.getWidth(), tile.getHeight());
 		}
 	}
 	
 	@Override
 	public void drawOn(View canvasView) {
 		for(Map.Entry<Tile, ImageElement> each : images.entrySet()) {
 			Tile tile = each.getKey();
 			ImageElement img = each.getValue();
 			canvasView.getCanvas().drawImage(img, tile.getLeftX(), tile.getTopY(), tile.getWidth(), tile.getHeight());
 		}
 	}
 	
 	protected String getUrl(int zoom, int x, int y) {
 		return baseUrl + zoom + "/" + x + "/" + y + ".png";
 	}
 
 }
