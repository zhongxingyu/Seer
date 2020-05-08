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
 package net.rptools.maptool.util;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ui.Scale;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.client.ui.zone.ZoneView;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.drawing.DrawablePaint;
 import net.rptools.maptool.model.drawing.DrawableTexturePaint;
 import net.rptools.maptool.model.drawing.DrawnElement;
 
 import com.caucho.hessian.io.HessianInput;
 import com.caucho.hessian.io.HessianOutput;
 
 /**
  * @author trevor
  */
 public class PersistenceUtil {
 
 	public static void saveCampaign(Campaign campaign, File campaignFile) throws IOException {
 		
 		// This is a veeeeeery primitive form of perstence, and will be changing very soon
 		OutputStream os = new BufferedOutputStream(new FileOutputStream(campaignFile));
 		HessianOutput out = new HessianOutput(os);
 		
 		PersistedCampaign persistedCampaign = new PersistedCampaign();
 		
 		persistedCampaign.campaign = campaign;
 		
 		// Save all assets in active use
 		for (Zone zone : campaign.getZones()) {
 			
 			persistedCampaign.assetMap.put(zone.getAssetID(), AssetManager.getAsset(zone.getAssetID()));
 			
 			for (Token token : zone.getAllTokens()) {
 				
 				persistedCampaign.assetMap.put(token.getAssetID(), AssetManager.getAsset(token.getAssetID()));
 			}
 
 			// Painted textures
 			for (DrawnElement drawn : zone.getAllDrawnElements()) {
     			DrawablePaint paint = drawn.getPen().getPaint(); 
     			if (paint instanceof DrawableTexturePaint) {
     				MD5Key assetId = ((DrawableTexturePaint)paint).getAssetId();
     				Asset asset = ((DrawableTexturePaint)paint).getAsset();
     				persistedCampaign.assetMap.put(assetId, asset);
     			}
     			
     			paint = drawn.getPen().getBackgroundPaint();
     			if (paint instanceof DrawableTexturePaint) {
     				MD5Key assetId = ((DrawableTexturePaint)paint).getAssetId();
     				Asset asset = ((DrawableTexturePaint)paint).getAsset();
     				persistedCampaign.assetMap.put(assetId, asset);
     			}
 			}
 		}
 		
 		ZoneRenderer currentZoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
 		if (currentZoneRenderer != null) {
 			persistedCampaign.currentZoneId = currentZoneRenderer.getZone().getId();
 			persistedCampaign.currentView = currentZoneRenderer.getZoneScale();
 		}
 		
 		// Save the campaign thumbnail
 		saveCampaignThumbnail(campaignFile.getName());        
 		
 		out.writeObject(persistedCampaign);
 		os.close();		
 	}
 
 	/* A public function because I think it should be called when a campaign is opened as well
 	* so if it is opened then closed without saving, there is still a preview created;
 	* however, the rendering of the campaign appears to complete after AppActions.loadCampaign 
 	* returns, causing the preview to always appear as black if this method is called from
 	* within loadCampaign.  Either need to find another place to call saveCampaignThumbnail
 	* upon opening, or code to delay it's call until the render is complete.  =P
 	*/
 	static public void saveCampaignThumbnail(String fileName) {
 		BufferedImage screen = MapTool.takeMapScreenShot(new ZoneView(MapTool.getPlayer().getRole()));
 		
 		Dimension imgSize = new Dimension(screen.getWidth(null), screen.getHeight(null));
 		SwingUtil.constrainTo(imgSize, 200, 200);
 
 		BufferedImage thumb = new BufferedImage(imgSize.width,imgSize.height, BufferedImage.TYPE_INT_BGR);
 		Graphics2D g2d = thumb.createGraphics();
 		g2d.drawImage(screen, 0, 0, imgSize.width, imgSize.height, null);
         g2d.dispose();
         
 		File thumbFile = getCampaignThumbnailFile(fileName);
         
         try{
         	ImageIO.write(thumb, "jpg", thumbFile);
         }
         catch (IOException ioe) {
         	MapTool.showError("Could not save the campaign preview image: " + ioe);
         }
 	}
 	
 	/**
 	 * Gets a file pointing to where the campaign's thumbnail image should be.
 	 * @param fileName The campaign's file name.
 	 */
 	static public File getCampaignThumbnailFile(String fileName) {
 		return new File(AppUtil.getAppHome("campaignthumbs"), fileName + ".jpg");
 	}
 	
 	public static PersistedCampaign loadCampaign(File campaignFile) throws IOException {
 		
 		InputStream is = new BufferedInputStream(new FileInputStream(campaignFile));
 		HessianInput in = new HessianInput(is);
 
 		PersistedCampaign persistedCampaign = (PersistedCampaign) in.readObject(null);
 		is.close();
 
 		for (MD5Key key : persistedCampaign.assetMap.keySet()) {
 
             Asset asset = persistedCampaign.assetMap.get(key);
             
 			if (!AssetManager.hasAsset(key)) {
 				AssetManager.putAsset(asset);
 			}
 
 			if (!MapTool.isHostingServer() && !MapTool.isPersonalServer()) {
 				// If we are remotely installing this campaign, we'll need to send the image data to the server
 	            MapTool.serverCommand().putAsset(asset);
 			}
 		}
 		
 		return persistedCampaign;
 	}
 	
 	public static <T> T hessianClone(T object) throws IOException {
 
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		HessianOutput out = new HessianOutput(os);
 		out.writeObject(object);
 
 		HessianInput in = new HessianInput(new ByteArrayInputStream(os.toByteArray()));
 
 		return (T)in.readObject();
 	}
 	
 	public static class PersistedCampaign {
 		
 		public Campaign campaign;
 		public Map<MD5Key, Asset> assetMap = new HashMap<MD5Key, Asset>();
 		public GUID currentZoneId;
 		public Scale currentView;
 	}
 }
