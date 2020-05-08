 package de.neufeld.gis.provider.internal.osm;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ForkJoinPool;
 import java.util.concurrent.RecursiveTask;
 
 import javax.imageio.ImageIO;
 
 import org.eclipse.gef4.geometry.planar.Rectangle;
 import org.eclipse.gef4.graphics.IGraphics;
 import org.eclipse.gef4.graphics.image.Image;
 import org.osgi.service.log.LogService;
 
 import de.neufeld.gis.core.DrawParameter;
 import de.neufeld.gis.core.GisMap;
 import de.neufeld.gis.core.MapDataProvider;
 import de.neufeld.gis.core.MapDataProviderRegistry;
 import de.neufeld.gis.core.SearchQuery;
 
 public class OSMProvider implements MapDataProvider {
 	private String url = "http://tile.openstreetmap.org/$path$.png";
 	private final double TILE_SIZE=256;
 	private final double M_PER_PIXEL_ZOOM_0=156543.034;
 	private final double M_PER_TILE_ZOOM_0=M_PER_PIXEL_ZOOM_0*TILE_SIZE;
 	
 	private LogService logService;
 	private MapDataProviderRegistry mapDataProviderRegistry;
 
 	public void setLogService(LogService logService) {
 		this.logService = logService;
 	}
 
 	public void unsetLogService(LogService logService) {
 		this.logService = null;
 	}
 
 	public void setMapDataProviderRegistry(
 			MapDataProviderRegistry mapDataProviderRegistry) {
 		this.mapDataProviderRegistry = mapDataProviderRegistry;
 	}
 
 	public void unsetMapDataProviderRegistry(
 			MapDataProviderRegistry mapDataProviderRegistry) {
 		this.mapDataProviderRegistry = null;
 	}
 
 	public void startup() {
 		mapDataProviderRegistry.registerMapDataProvider(this);
 	}
 
 	public void shutdown() {
 		mapDataProviderRegistry.unregisterMapDataProvider(this);
 	}
 
 	@Override
 	public void draw(GisMap gisMap, IGraphics graphics,
 			DrawParameter drawParameter) {
 		
//		Rectangle visibleArea=drawParameter.getVisibleArea().getBounds();
//		double numTiles=Math.pow(2, drawParameter.getZoomLevel());
//		double tileWidth=M_PER_TILE_ZOOM_0/numTiles;
//		double currentX=visibleArea.getX()/tileWidth;
//		double currentY=visibleArea.getY()/tileWidth;
//		int tilesX=(int)Math.floor(currentX);
//		int tilesY=(int)Math.floor(currentY);
//		
//		double offSetX=(tilesX-currentX)*TILE_SIZE;
//		double offSetY=(tilesY-currentY)*TILE_SIZE;
//		
//		int numTilesX=(int)Math.ceil(visibleArea.getWidth()/tileWidth);
//		int numTilesY=(int)Math.ceil(visibleArea.getHeight()/tileWidth);
//		for(int i=0;i<=numTilesX;i++){
//			for(int j=0;j<=numTilesY;j++){
//				String httpUrl=url.replace("$path$", (int)drawParameter.getZoomLevel()+"/"+(tilesX+i)+"/"+(tilesY+j));
//				try {
//					URL imageUrl = new URL(httpUrl);
//					Image image = new Image(ImageIO.read(imageUrl));
//					graphics.pushState().translate(offSetX+i*TILE_SIZE, offSetY+j*TILE_SIZE);
//					graphics.paint(image);
//					graphics.popState();
//				} catch (IOException e) {
//					logService.log(LogService.LOG_ERROR, e.getMessage(), e);
//				}
//			}
//		}
		
 		ForkJoinPool forkJoinPool = new ForkJoinPool();
 		forkJoinPool.invoke(new ImageTask(drawParameter,graphics));
 	}
 	private class ImageTask extends RecursiveTask<Void>{
 		private DrawParameter drawParameter;
 		private IGraphics graphics;
 		private ImageTask(DrawParameter drawParameter,IGraphics graphics){
 			this.drawParameter=drawParameter;
 			this.graphics=graphics;
 		}
 		@Override
 		protected Void compute() {
 			Rectangle visibleArea=drawParameter.getVisibleArea().getBounds();
 			double numTiles=Math.pow(2, drawParameter.getZoomLevel());
 			double tileWidth=M_PER_TILE_ZOOM_0/numTiles;
 			double currentX=visibleArea.getX()/tileWidth;
 			double currentY=visibleArea.getY()/tileWidth;
 			int tilesX=(int)Math.floor(currentX);
 			int tilesY=(int)Math.floor(currentY);
 			
 			double offSetX=(tilesX-currentX)*TILE_SIZE;
 			double offSetY=(tilesY-currentY)*TILE_SIZE;
 			
 			int numTilesX=(int)Math.ceil(visibleArea.getWidth()/tileWidth);
 			int numTilesY=(int)Math.ceil(visibleArea.getHeight()/tileWidth);
 			
 			List<RecursiveTask<ImageOffsetPair>> forks = new LinkedList<>();
 			
 			for(int i=0;i<=numTilesX;i++){
 				for(int j=0;j<=numTilesY;j++){
 					ImageRetrieveTask task=new ImageRetrieveTask((int)drawParameter.getZoomLevel(),offSetX,offSetY,tilesX,tilesY,i,j);
 					forks.add(task);
 		            task.fork();
 				}
 			}
 			for (RecursiveTask<ImageOffsetPair> task : forks) {
 				ImageOffsetPair pair=task.join();
 				graphics.pushState().translate(pair.offsetX, pair.offsetY);
 				
 				graphics.paint(pair.image);
 				graphics.popState();
 	        }
 			
 			
 			return null;
 		}
 		
 	}
 	
 	private class ImageRetrieveTask extends RecursiveTask<ImageOffsetPair>{
 		final int zoomLevel;
 		final double offsetX;final double offsetY;final int tileX;final int tileY;final int moveX;final int moveY;
 		public ImageRetrieveTask(int zoomLevel,double offsetX,double offsetY,int tileX,int tileY,int moveX,int moveY){
 			this.zoomLevel=zoomLevel;
 			this.offsetX=offsetX;
 			this.offsetY=offsetY;
 			this.tileX=tileX;
 			this.tileY=tileY;
 			this.moveX=moveX;
 			this.moveY=moveY;
 		}
 		
 		@Override
 		protected ImageOffsetPair compute() {
 
 			ImageOffsetPair result=new ImageOffsetPair();
 			try {
 				String httpUrl=url.replace("$path$", zoomLevel+"/"+(tileX+moveX)+"/"+(tileY+moveY));
 				URL imageUrl = new URL(httpUrl);
 				Image image = new Image(ImageIO.read(imageUrl));
 				result.image=image;
 				result.offsetX=offsetX+moveX*TILE_SIZE;
 				result.offsetY=offsetY+moveY*TILE_SIZE;
 			} catch (IOException e) {
 				logService.log(LogService.LOG_ERROR, e.getMessage(), e);
 			}
 			return result;
 		}
 		
 	}
 	
 	private class ImageOffsetPair{
 		Image image;
 		double offsetX;
 		double offsetY;
 	}
 	
 	
 	@Override
 	public void search(GisMap gisMap, SearchQuery searchQuery) {
 		// TODO Auto-generated method stub
 
 	}
 }
