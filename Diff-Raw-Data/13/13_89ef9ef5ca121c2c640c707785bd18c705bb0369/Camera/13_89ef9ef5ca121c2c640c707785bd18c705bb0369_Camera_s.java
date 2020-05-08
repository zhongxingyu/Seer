 package org.blockout.ui;
 
 import org.blockout.common.TileCoordinate;
 
 import com.google.common.base.Preconditions;
 
 /**
  * 
  * 
  * @author Marc-Christian Schulze
  * 
  */
 public final class Camera {
 
 	protected CameraData	data	= new CameraData();
 
 	public Camera(final int tileSize, final int displayWidth, final int displayHeight) {
 		Preconditions.checkArgument( tileSize > 0, "Tile size must be positive." );
 		Preconditions.checkArgument( displayHeight > 0, "Display height must be positive." );
 		Preconditions.checkArgument( displayWidth > 0, "Display width must be positive." );
 
 		data.tileSize = tileSize;
 
 		setBounds( displayWidth, displayHeight );
 	}
 
 	private Camera() {
 	}
 
 	public Camera getReadOnly() {
 		Camera cam = new Camera();
 		cam.data = data;
 		return cam;
 	}
 
 	public boolean isInFrustum( final TileCoordinate coord ) {
		boolean inHorRange = coord.getX() >= data.startTileX && coord.getX() <= data.startTileX + data.numHorTiles;
		boolean inVerRange = coord.getY() >= data.startTileY && coord.getY() <= data.startTileY + data.numVerTiles;
 		return inHorRange && inVerRange;
 	}
 
 	public void setBounds( final int displayWidth, final int displayHeight ) {
 		CameraData newData = new CameraData( data );
 
 		newData.width = displayWidth;
 		newData.height = displayHeight;
 
 		newData.halfWidth = displayWidth / 2f;
 		newData.halfHeight = displayHeight / 2f;
 
 		newData.numHorTiles = (int) Math.ceil( displayWidth / ((double) data.tileSize) );
 		if ( displayWidth % data.tileSize == 0 ) {
 			newData.numHorTiles++;
 		}
 		newData.numVerTiles = (int) Math.ceil( displayHeight / ((double) data.tileSize) );
 		if ( displayHeight % data.tileSize == 0 ) {
 			newData.numVerTiles++;
 		}
 		adjustOfsets( newData );
 
 		data = newData;
 	}
 
 	public float getCenterX() {
 		return data.centerX;
 	}
 
 	public float getCenterY() {
 		return data.centerY;
 	}
 
 	public int getWidth() {
 		return data.width;
 	}
 
 	public int getHeight() {
 		return data.height;
 	}
 
 	public float getHalfWidth() {
 		return data.halfWidth;
 	}
 
 	public float getHalfHeight() {
 		return data.halfHeight;
 	}
 
 	public int getNumHorTiles() {
 		return data.numHorTiles;
 	}
 
 	public int getNumVerTiles() {
 		return data.numVerTiles;
 	}
 
 	public int getTileSize() {
 		return data.tileSize;
 	}
 
 	public int getWidthOfset() {
 		return data.widthOfset;
 	}
 
 	public int getHeightOfset() {
 		return data.heightOfset;
 	}
 
 	public int getStartTileX() {
 		return data.startTileX;
 	}
 
 	public int getStartTileY() {
 		return data.startTileY;
 	}
 
 	public void setViewCenter( final float x, final float y ) {
 		CameraData newData = new CameraData( data );
 		newData.centerX = x;
 		newData.centerY = y;
 		adjustOfsets( newData );
 		data = newData;
 	}
 
 	private static void adjustOfsets( final CameraData newData ) {
 		newData.startTileX = worldToTile( newData.centerX - (newData.halfWidth / newData.tileSize) );
 		newData.startTileY = worldToTile( newData.centerY - (newData.halfHeight / newData.tileSize) );
 
 		float tmpWidth = (newData.centerX - (newData.halfWidth / newData.tileSize)) % 1;
 		if ( tmpWidth < 0 ) {
 			tmpWidth++;
 		}
 		newData.widthOfset = (int) (tmpWidth * newData.tileSize);
 		float tmpHeight = (newData.centerY - (newData.halfHeight / newData.tileSize)) % 1;
 		if ( tmpHeight < 0 ) {
 			tmpHeight++;
 		}
 		newData.heightOfset = (int) (tmpHeight * newData.tileSize);
 	}
 
 	public int convertY( final int y ) {
 		return data.height - y;
 	}
 
 	public static int worldToTile( final float worldCoordinate ) {
 		if ( worldCoordinate >= 0 ) {
 			return (int) worldCoordinate;
 		}
 		return (int) worldCoordinate - 1;
 	}
 
 	public float screenToWorldX( final int screenX ) {
 		float worldX0 = data.centerX - (data.halfWidth / data.tileSize);
 		return worldX0 + (screenX / ((float) data.tileSize));
 	}
 
 	public float screenToWorldY( final int screenY ) {
 		float worldY0 = data.centerY - (data.halfHeight / data.tileSize);
 		return worldY0 + (convertY( screenY ) / ((float) data.tileSize));
 	}
 }
