 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client.ui.zone;
 
 import java.awt.Point;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Rectangle2D;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ui.zone.vbl.AreaTree;
 import net.rptools.maptool.model.AttachedLightSource;
 import net.rptools.maptool.model.Direction;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Light;
 import net.rptools.maptool.model.LightSource;
 import net.rptools.maptool.model.ModelChangeEvent;
 import net.rptools.maptool.model.ModelChangeListener;
 import net.rptools.maptool.model.SightType;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 
 public class ZoneView implements ModelChangeListener {
 
 	private Zone zone;
 
 	// VISION
 	private Map<GUID, Area> tokenVisibleAreaCache = new HashMap<GUID, Area>();
     private Map<GUID, Area> tokenVisionCache = new HashMap<GUID, Area>();
     private Map<GUID, Map<String, Area>> lightSourceCache = new HashMap<GUID, Map<String, Area>>();
     private Set<GUID> lightSourceSet = new HashSet<GUID>();
     private Map<GUID, Set<DrawableLight>> drawableLightCache = new HashMap<GUID, Set<DrawableLight>>();
     private Map<GUID, Set<Area>> brightLightCache = new HashMap<GUID, Set<Area>>();
     private Map<PlayerView, VisibleAreaMeta> visibleAreaMap = new HashMap<PlayerView, VisibleAreaMeta>();
     private AreaData topologyAreaData;
     private AreaTree topology;
     
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
 		return lightSourceSet.size() > 0 || (zone.getTopology() != null && !zone.getTopology().isEmpty());
 	}
 	
 	public AreaTree getTopology() {
 		if (topology == null) {
 			topology = new AreaTree(zone.getTopology());
 		}
 		return topology;
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
     	Map<String, Area> areaBySightMap = lightSourceCache.get(lightSourceToken.getId());
     	if (areaBySightMap != null) {
 
     		Area lightSourceArea = areaBySightMap.get(token.getSightType());
     		if (lightSourceArea != null) {
     			return lightSourceArea;
     		}
     	} else {
     		areaBySightMap = new HashMap<String, Area>();
     		lightSourceCache.put(lightSourceToken.getId(), areaBySightMap);
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
         
     	// Calculate exposed area
         // TODO: This won't work with directed light, need to add an anchor or something
         if (sight.getMultiplier() != 1) {
         	lightSourceArea.transform(AffineTransform.getScaleInstance(sight.getMultiplier(), sight.getMultiplier()));
         }
 
 		Area visibleArea = FogUtil.calculateVisibility5(p.x, p.y, lightSourceArea, getTopology());
 
 		if (visibleArea == null) {
 			return null;
 		}
 
 		// Keep track of colored light
         Set<DrawableLight> lightSet = new HashSet<DrawableLight>();
         Set<Area> brightLightSet = new HashSet<Area>();
         for (Light light : lightSource.getLightList()) {
         	
         	Area lightArea = lightSource.getArea(lightSourceToken, zone, direction, light);
             if (sight.getMultiplier() != 1) {
             	lightArea.transform(AffineTransform.getScaleInstance(sight.getMultiplier(), sight.getMultiplier()));
             }
 
             lightArea.transform(AffineTransform.getTranslateInstance(p.x, p.y));
             lightArea.intersect(visibleArea);
 
             if (light.getPaint() != null) {
             	lightSet.add(new DrawableLight(light.getPaint(), lightArea));
             } else {
             	brightLightSet.add(lightArea);
             }
         }
         drawableLightCache.put(lightSourceToken.getId(), lightSet);
         brightLightCache.put(lightSourceToken.getId(), brightLightSet);
         
 		return visibleArea;
     }
     
 	public Area getVisibleArea(Token token) {
 
 		// Sanity
 		if (token == null || !token.getHasSight()) {
 			return null;
 		}
 		
 		// Cache ?
 		Area tokenVisibleArea = tokenVisionCache.get(token.getId());
 		if (tokenVisibleArea != null) {
 			return tokenVisibleArea;
 		}
 
 		// Combine the player visible area with the available light sources
 		tokenVisibleArea = tokenVisibleAreaCache.get(token.getId());
 		if (tokenVisibleArea == null) {
 			
 	        Point p = FogUtil.calculateVisionCenter(token, zone);
 	        int visionDistance = zone.getTokenVisionInPixels();
 	        Area visibleArea = new Area(new Ellipse2D.Double(-visionDistance, -visionDistance, visionDistance*2, visionDistance*2));
 
 //	        System.out.println("---------------------------------------");
 //	        tokenVisibleArea = FogUtil.calculateVisibility(p.x, p.y, visibleArea, getTopologyAreaData());
 //	        tokenVisibleArea = FogUtil.calculateVisibility2(p.x, p.y, visibleArea, getTopology());
 //	        tokenVisibleArea = FogUtil.calculateVisibility3(p.x, p.y, visibleArea, getTopology());
 //	        tokenVisibleArea = FogUtil.calculateVisibility4(p.x, p.y, visibleArea, getTopology());
 	        tokenVisibleArea = FogUtil.calculateVisibility5(p.x, p.y, visibleArea, getTopology());
 			
 			tokenVisibleAreaCache.put(token.getId(), tokenVisibleArea);
 		}
 
         // Combine in the visible light areas
         if (lightSourceSet.size() > 0 && tokenVisibleArea != null) {
         
         	Rectangle2D origBounds = tokenVisibleArea.getBounds();
         	
     		// Combine all light sources that might intersect our vision
         	List<Area> intersects = new LinkedList<Area>();
     		for (GUID lightSourceTokenId : lightSourceSet) {
     			
     			Token lightSourceToken = zone.getToken(lightSourceTokenId);
     			if (lightSourceToken == null) {
     				continue;
     			}
     			
     			Area lightArea = getLightSourceArea(token, lightSourceToken);
 
     			if (origBounds.intersects(lightArea.getBounds2D())) {
             		Area intersection = new Area(tokenVisibleArea);
             		intersection.intersect(lightArea);
                 	intersects.add(intersection);
             	}
     		}
         	
             // Check for personal vision
             SightType sight = MapTool.getCampaign().getSightType(token.getSightType());
             if (sight != null && sight.hasPersonalLightSource()) {
     			Area lightArea = calculateLightSourceArea(sight.getPersonalLightSource(), token, sight, Direction.CENTER);
     			if (lightArea != null) {
             		Area intersection = new Area(tokenVisibleArea);
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
 
             tokenVisibleArea = intersects.size() > 0 ? intersects.get(0) : new Area();
         }
         
         tokenVisionCache.put(token.getId(), tokenVisibleArea);
 		
 		return tokenVisibleArea;
 	}
 
 	private void findLightSources() {
 		
 		lightSourceSet.clear();
 		
 		for (Token token : zone.getAllTokens()) {
 			if (token.hasLightSources() && token.isVisible()) {
 				lightSourceSet.add(token.getId());
 			}
 		}
 	}
 	
 	public Set<DrawableLight> getDrawableLights() {
 		Set<DrawableLight> lightSet = new HashSet<DrawableLight>();
 		
 		for (Set<DrawableLight> set : drawableLightCache.values()) {
 			lightSet.addAll(set);
 		}
 		
 		return lightSet;
 	}
 	
 	public Set<Area> getBrightLights() {
 		Set<Area> lightSet = new HashSet<Area>();
 		
 		for (Set<Area> set : brightLightCache.values()) {
 			lightSet.addAll(set);
 		}
 		
 		return lightSet;
 	}
 	
 	public void flush() {
 		tokenVisibleAreaCache.clear();
 		tokenVisionCache.clear();
 		lightSourceCache.clear();
 		visibleAreaMap.clear();
 		drawableLightCache.clear();
 		brightLightCache.clear();
 	}
 	
     private void flush(Token token) {
     	boolean hadLightSource = lightSourceCache.get(token.getId()) != null;
     	
         tokenVisionCache.remove(token.getId());
         tokenVisibleAreaCache.remove(token.getId());
         lightSourceCache.remove(token.getId());
         drawableLightCache.remove(token.getId());
         brightLightCache.remove(token.getId());
         visibleAreaMap.clear();
         
         if (hadLightSource || token.hasLightSources()) {
         	// Have to recalculate all token vision
         	tokenVisionCache.clear();
         }
         if (token.getHasSight()) {
             visibleAreaMap.clear();
         }
         
 //        // TODO: This fixes a bug with changing vision type, I don't like it though, it needs to be optimized back out
 //        lightSourceCache.clear();
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
 
             if (!token.getHasSight ()) {
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
             	if (token.hasLightSources() && token.isVisible()) {
             		lightSourceSet.add(token.getId());
             	} else {
             		lightSourceSet.remove(token.getId());
             	}
             	
             	if (token.getHasSight()) {
                     visibleAreaMap.clear();
             	}
             }
             if (evt == Zone.Event.TOKEN_REMOVED) {
             	Token token = (Token) event.getArg();
         		lightSourceSet.remove(token);
             }
             
 		}
 	}
 	
 	private static class VisibleAreaMeta {
 		
 		Area visibleArea;
 		
 	}
 	
 }
