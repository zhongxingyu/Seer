 package net.rptools.maptool.client.ui.zone;
 
 import java.awt.Point;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.model.AttachedLightSource;
 import net.rptools.maptool.model.Direction;
 import net.rptools.maptool.model.LightSource;
 import net.rptools.maptool.model.ModelChangeEvent;
 import net.rptools.maptool.model.ModelChangeListener;
 import net.rptools.maptool.model.SightType;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 
 public class ZoneView implements ModelChangeListener {
 
 	private Zone zone;
 
 	// VISION
     private Map<Token, Area> tokenVisionCache = new HashMap<Token, Area>();
     private Map<Token, Map<String, Area>> lightSourceCache = new HashMap<Token, Map<String, Area>>();
     private Set<Token> lightSourceSet = new HashSet<Token>();
     private Map<PlayerView, VisibleAreaMeta> visibleAreaMap = new HashMap<PlayerView, VisibleAreaMeta>();
     private AreaData topologyAreaData;
     
 	public ZoneView(Zone zone) {
 		
 		this.zone = zone;
 		
 		findLightSources();
 		
 		zone.addModelChangeListener(this);
 	}
 
 	public Area getVisibleArea(PlayerView view) {
 		calculateVisibleArea(view);
 		return visibleAreaMap.get(view).visibleArea;
 	}
 	
 	public boolean isUsingVision() {
 		return lightSourceCache.size() > 0 || (zone.getTopology() != null && !zone.getTopology().isEmpty());
 	}
 
     public AreaData getTopologyAreaData() {
     	if (topologyAreaData == null) {
     		topologyAreaData = new AreaData(zone.getTopology());
     		topologyAreaData.digest();
     	}
     	return topologyAreaData;
     }
     
     public Area getLightSourceArea(Token token, Token lightSourceToken) {
     	
     	// Cached ?
     	Map<String, Area> areaBySightMap = lightSourceCache.get(lightSourceToken);
     	if (areaBySightMap != null) {
 
     		Area lightSourceArea = areaBySightMap.get(token.getSightType());
     		if (lightSourceArea != null) {
     			return lightSourceArea;
     		}
     	} else {
     		areaBySightMap = new HashMap<String, Area>();
     		lightSourceCache.put(lightSourceToken, areaBySightMap);
     	}
     	
     	// Calculate
 		Area area = new Area();
 		for (AttachedLightSource attachedLightSource : lightSourceToken.getLightSources()) {
 			
 			LightSource lightSource = MapTool.getCampaign().getLightSource(attachedLightSource.getLightSourceId());
 			if (lightSource == null) {
 				continue;
 			}
 			
 	        SightType sight = MapTool.getCampaign().getSightType(token.getSightType());
 			Area visibleArea = calculateLightSourceArea(lightSource, lightSourceToken, sight, attachedLightSource.getDirection());
 
 			if (visibleArea != null) {
 				area.add(visibleArea);
 			}
 		}
     	
 		// Cache
 		areaBySightMap.put(token.getSightType(), area);
 		
 		return area;
     }
 	
     private Area calculateLightSourceArea(LightSource lightSource, Token lightSourceToken, SightType sight, Direction direction) {
     	
         Point p = FogUtil.calculateVisionCenter(lightSourceToken, zone);
         Area lightSourceArea = lightSource.getArea(lightSourceToken, zone, direction);
         
         // TODO: This won't work with directed light, need to add an anchor or something
         System.out.println(sight.getName());
         if (sight.getMultiplier() != 1) {
         	System.out.println("Before: " + lightSourceArea.getBounds());
         	lightSourceArea.transform(AffineTransform.getScaleInstance(sight.getMultiplier(), sight.getMultiplier()));
         	System.out.println("After: " + lightSourceArea.getBounds());
         	
         }
         
 		return FogUtil.calculateVisibility(p.x, p.y, lightSourceArea, getTopologyAreaData());
     }
     
 	public Area getVisibleArea(Token token) {
 
 		// Sanity
 		if (token == null || !token.hasSight()) {
 			return null;
 		}
 		
 		// Cache ?
 		Area tokenVisibleArea = tokenVisionCache.get(token);
 		if (tokenVisibleArea != null) {
 			return tokenVisibleArea;
 		}
 
 		// Visible area without inhibition
         Point p = FogUtil.calculateVisionCenter(token, zone);
         int visionDistance = zone.getTokenVisionDistance();
         Area visibleArea = new Area(new Ellipse2D.Double(-visionDistance, -visionDistance, visionDistance*2, visionDistance*2));
         visibleArea = FogUtil.calculateVisibility(p.x, p.y, visibleArea, getTopologyAreaData());
 
         // Simple case
         if (lightSourceSet.size() > 0) {
         
 	        if (visibleArea != null) {
 	        	Rectangle2D origBounds = visibleArea.getBounds();
 	        	
 	    		// Combine all light sources that might intersect our vision
 	        	List<Area> intersects = new LinkedList<Area>();
 	    		for (Token lightSourceToken : lightSourceSet) {
 	    			
 	    			Area lightArea = getLightSourceArea(token, lightSourceToken);
 	
 	    			if (origBounds.intersects(lightArea.getBounds2D())) {
 	            		Area intersection = new Area(visibleArea);
 	            		intersection.intersect(lightArea);
 	                	intersects.add(intersection);
 	            	}
 	    		}
 	        	
 	            // Check for personal vision
 	            SightType sight = MapTool.getCampaign().getSightType(token.getSightType());
 	            if (sight != null && sight.hasPersonalLightSource()) {
 	    			Area lightArea = calculateLightSourceArea(sight.getPersonalLightSource(), token, sight, Direction.CENTER);
 	    			if (lightArea != null) {
 	            		Area intersection = new Area(visibleArea);
 	            		intersection.intersect(lightArea);
 	                	intersects.add(intersection);
 	    			}
 	            }
 
 				while (intersects.size() > 1) {
 					
 					Area a1 = intersects.remove(0);
 					Area a2 = intersects.remove(0);
 					
 					a1.add(a2);
 					intersects.add(a1);
 				}
 	
 	            visibleArea = intersects.size() > 0 ? intersects.get(0) : new Area();
 	        }
         }
         
         tokenVisionCache.put(token, visibleArea);
 		
 		return visibleArea;
 	}
 
 	private void findLightSources() {
 		
 		lightSourceSet.clear();
 		
 		for (Token token : zone.getAllTokens()) {
 			if (token.hasLightSources()) {
 				lightSourceSet.add(token);
 			}
 		}
 	}
 	
     private void flush(Token token) {
     	boolean hadLightSource = lightSourceCache.get(token) != null;
     	
         tokenVisionCache.remove(token);
         lightSourceCache.remove(token);
         visibleAreaMap.clear();
         
        if (hadLightSource) {
         	// Have to recalculate all token vision
         	tokenVisionCache.clear();
         }
         if (token.hasSight()) {
             visibleAreaMap.clear();
         }
     }
 
     private void calculateVisibleArea(PlayerView view) {
     	if (visibleAreaMap.get(view) != null) {
     		return;
     	}
 
     	// Cache it
     	VisibleAreaMeta meta = new VisibleAreaMeta();
     	meta.visibleArea = new Area();
 
     	visibleAreaMap.put(view, meta);
     	
     	// Calculate it
         for (Token token : zone.getAllTokens()) {
 
             if (!token.hasSight ()) {
             	continue;
             }
                 
             // Don't bother if it's not visible
             if (!view.isGMView() && !token.isVisible()) {
         		continue;
             }
 
             // Permission
             if (MapTool.getServerPolicy().isUseIndividualViews()) {
             	if (!AppUtil.playerOwns(token)) {
             		continue;
             	}
             } else {
             	// Party members only, unless you are the GM
             	if (token.getType() != Token.Type.PC && !view.isGMView()) {
             		continue;
             	}
             }
 
             Area tokenVision = getVisibleArea(token);	                
             if (tokenVision != null) {
 
                 meta.visibleArea.add(tokenVision);
             }
         }
     }
     
     ////
 	// MODEL CHANGE LISTENER
 	public void modelChanged(ModelChangeEvent event) {
 
 		Object evt = event.getEvent();
 		if (event.getModel() instanceof Zone) {
 			
             if (evt == Zone.Event.TOPOLOGY_CHANGED) {
                 tokenVisionCache.clear();
                 lightSourceCache.clear();
                 visibleAreaMap.clear();
                 topologyAreaData = null;
             }
             if (evt == Zone.Event.TOKEN_CHANGED || evt == Zone.Event.TOKEN_REMOVED) {
             	flush((Token)event.getArg());
             }
             if (evt == Zone.Event.TOKEN_ADDED || evt == Zone.Event.TOKEN_CHANGED) {
             	Token token = (Token) event.getArg();
             	if (token.hasLightSources()) {
             		lightSourceSet.add(token);
             	} else {
             		lightSourceSet.remove(token);
             	}
             	
             	if (token.hasSight()) {
                     visibleAreaMap.clear();
             	}
             }
             if (evt == Zone.Event.TOKEN_REMOVED) {
             	Token token = (Token) event.getModel();
         		lightSourceSet.remove(token);
             }
             
 		}
 	}
 	
 	private static class VisibleAreaMeta {
 		
 		Area visibleArea;
 		
 	}
 	
 }
