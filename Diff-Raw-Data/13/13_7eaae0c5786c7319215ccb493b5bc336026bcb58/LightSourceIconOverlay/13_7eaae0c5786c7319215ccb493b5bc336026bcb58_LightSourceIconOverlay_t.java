 package net.rptools.maptool.client.ui.zone;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Area;
 
 import net.rptools.maptool.client.AppStyle;
 import net.rptools.maptool.model.Token;
 
 public class LightSourceIconOverlay implements ZoneOverlay {
 
 	public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
 
 		for (Token token : renderer.getZone().getAllTokens()) {
 
 			if (token.hasLightSources()) {
 				Area area = renderer.getTokenBounds(token);
				if (area == null) {
					continue;
				}

 				int x = area.getBounds().x + (area.getBounds().width - AppStyle.lightSourceIcon.getWidth())/2;
 				int y = area.getBounds().y + (area.getBounds().height - AppStyle.lightSourceIcon.getHeight())/2;
 				g.drawImage(AppStyle.lightSourceIcon, x, y, null);
 			}
 		}
 	}
 }
