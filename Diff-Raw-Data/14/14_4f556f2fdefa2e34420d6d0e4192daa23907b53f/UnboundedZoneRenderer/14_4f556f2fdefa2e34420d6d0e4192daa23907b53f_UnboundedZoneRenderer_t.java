 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client.ui.zone;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.awt.TexturePaint;
 import java.awt.Transparency;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ui.Scale;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.util.ImageManager;
 
 /**
  */
 public class UnboundedZoneRenderer extends ZoneRenderer {
 
 	private BufferedImage tileImage;
 	private BufferedImage backbuffer;
 	private int lastScale;
 	private int lastX;
 	private int lastY;
 	
 	private boolean loaded;
 	
 	private boolean drawBackground = true;
 
 	public UnboundedZoneRenderer(Zone zone) {
 		super(zone);
 	}
 	
 	@Override
 	public boolean isLoading() {
 		return !loaded;
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.rptools.maptool.client.ZoneRenderer#renderBoard(java.awt.Graphics)
 	 */
 	protected void renderBoard(Graphics2D g) {
 
 		Dimension size = getSize();
 		if (backbuffer == null || backbuffer.getWidth() != size.width || backbuffer.getHeight() != size.height) {
 			backbuffer = new BufferedImage(size.width, size.height, Transparency.OPAQUE);
 			drawBackground = true;
 		}
 
 		Scale scale = getZoneScale();
 		if (scale.getOffsetX() != lastX || scale.getOffsetY() != lastY || scale.getIndex() != lastScale) {
 			drawBackground = true;
 		}
 		
 		if (drawBackground) {
 			BufferedImage tileImage = getTileImage();
 			
 			Graphics2D bbg = backbuffer.createGraphics();
 			Paint paint = new TexturePaint(tileImage, new Rectangle2D.Float(getViewOffsetX(), getViewOffsetY(), tileImage.getWidth()*getScale(), tileImage.getHeight()*getScale()));
 			bbg.setPaint(paint);
 			bbg.fillRect(0, 0, size.width, size.height);
 			bbg.dispose();
 			
 			drawBackground = false;
 		}
 
 		lastX = scale.getOffsetX();
 		lastY = scale.getOffsetY();
 		lastScale = scale.getIndex();
 		
 		g.drawImage(backbuffer, 0, 0, this);
 
 //		BufferedImage tileImage = getTileImage();
 //		Paint paint = new TexturePaint(tileImage, new Rectangle2D.Float(getViewOffsetX(), getViewOffsetY(), tileImage.getWidth()*getScale(), tileImage.getHeight()*getScale()));
 //		g.setPaint(paint);
 //		g.fill(g.getClipBounds());
 	}
 
 	@Override
 	public void flush() {
 		backbuffer = null;
 		tileImage = null;
 		
 		loaded = false;
 		super.flush();
 	}
 	
     @Override
 	public BufferedImage getMiniImage(int size) {
         // TODO: I suppose this should honor the size
 		return getTileImage();
 	}
 	
 	private BufferedImage getTileImage() {
 
		if (tileImage != null && tileImage != ImageManager.UNKNOWN_IMAGE) {
 			return tileImage;
 		}
 
 		try {
 			MD5Key assetId = zone.getAssetID();
 			if (assetId == null) {
 				// TODO: make this static
 				tileImage = ImageUtil.getImage("net/rptools/lib/resource/image/texture/grass.png");
 				return tileImage;
 			}
 	
 			Asset asset = AssetManager.getAsset(assetId);
 			if (asset != null) {
 				BufferedImage image = ImageManager.getImage(asset, this);
 				if (image != ImageManager.UNKNOWN_IMAGE) {
 					tileImage = image;
 					
 					loaded = true;
 					drawBackground = true;
 				}
 				return image;
			} else {

	        	// Only request the asset once
	        	if (tileImage == null) {
	        		MapTool.serverCommand().getAsset(zone.getAssetID()); 
	        	}
	        	
	            tileImage = ImageManager.UNKNOWN_IMAGE;
				
 			}
 			
 			return ImageManager.UNKNOWN_IMAGE;
 			
 		} catch (IOException ioe) {
 			return ImageManager.UNKNOWN_IMAGE;
 		}
 	}
 	
 }
