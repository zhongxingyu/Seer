 /**
  * Copyright 2012 Jami Couch
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * This project uses:
  * 
  * LibGDX
  * Copyright 2011 see LibGDX AUTHORS file
  * Licensed under Apache License, Version 2.0 (see above).
  * 
  */
 package com.ahsgaming.valleyofbones.map;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.ahsgaming.valleyofbones.GameController;
 import com.ahsgaming.valleyofbones.Player;
 import com.ahsgaming.valleyofbones.units.Unit;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.*;
 
 /**
  * @author jami
  *
  */
 public class HexMap {
     public static final String LOG = "HexMap";
 
     GameController gameController;
     MapData mapData;
     HashMap<Integer, MapView> mapViews;
 
     public HexMap(GameController gameController, FileHandle jsonFile) {
         this.gameController = gameController;
 
         init(MapData.createFromFile(jsonFile));
 
     }
 
     public HexMap(GameController gameController, JsonValue json) {
         this.gameController = gameController;
 
         init(MapData.createFromJson(json));
     }
 
     public void init(MapData mapData) {
         this.mapData = mapData;
 
         mapViews = new HashMap<Integer, MapView>();
         for (Player player: gameController.getPlayers()) {
             mapViews.put(player.getPlayerId(), MapView.createMapView(mapData, player));
         }
     }
 
     public void invalidateViews() {
         for (MapView view: mapViews.values()) {
             view.setMapDirty(true);
         }
     }
 
     public void update() {
 
 //        if (!mapDirty) return;
 //
 //        mapDirty = false;
 
         for (MapView view: mapViews.values()) {
             if (view.isMapDirty())
                 updateView(view);
         }
     }
 
     void updateView(MapView view) {
         view.setMapDirty(false);
         // change all to FOG unless a UNIT can see them, or they are HIGHLIGHTED or DIMMED
         Array<Unit> units;
         if (view.player == null) {
             units = gameController.getUnits();
         } else {
             units = gameController.getUnitsByPlayerId(view.player.getPlayerId());
         }
 
         for (int i=0; i< view.hexStatus.length; i++) {
             view.hexStatus[i] = MapView.FOG;
 
         }
         for (int i = 0; i < units.size; i++) {
             if (units.get(i).getData().isBuilding()) {
                 units.removeIndex(i);
                 i--;
             }
         }
 
         int[] unitpositions = new int[units.size];
         int[] radii = new int[units.size];
         for (int u=0;u<units.size;u++) {
             Unit unit = units.get(u);
             unitpositions[u] = (int)(unit.getView().getBoardPosition().y * mapData.bounds.x + unit.getView().getBoardPosition().x);
             radii[u] = unit.getData().getSightRange();
         }
 
         boolean[] notavailable = new boolean[view.hexStatus.length];
 
         /*for (Unit u: gameController.getUnits()) {
             if (u.getOwner() == null || !u.getOwner().equals(player))
                 notavailable[(int)(u.getBoardPosition().y * bounds.x + u.getBoardPosition().x)] = true;
         } */
 
         boolean[] available = getAvailablePositions(unitpositions, radii, notavailable, false);
 
         for (int i=0; i< view.hexStatus.length; i++) {
 
             if (available[i]) view.hexStatus[i] = MapView.NORMAL;
 
             if (view.hexHighlight[i])
                 view.hexStatus[i] = MapView.HIGHLIGHT;
 
             if (view.hexDimmed[i])
                 view.hexStatus[i] = MapView.DIMMED;
         }
 
         view.modified = TimeUtils.millis();
     }
 
     public void highlightArea(MapView view, Vector2 center, int radius) {
         Array<Unit> units = gameController.getUnits();
 
         boolean[] notavailable = new boolean[view.hexStatus.length];
 
         for (Unit u: units) {
             if (!u.getView().getBoardPosition().equals(center))
                 notavailable[(int)(u.getView().getBoardPosition().y * mapData.bounds.x + u.getView().getBoardPosition().x)] = true;
         }
 
         int[] start = {(int)(center.y * mapData.bounds.x + center.x)};
         int[] radii = {radius};
         boolean[] available = getAvailablePositions(start, radii, notavailable, true);
 
         for (int i=0;i < view.hexStatus.length;i++) {
             if (available[i] && view.hexStatus[i] != MapView.FOG) {
                 if (notavailable[i] || i == start[0]) {
                     view.hexDimmed[i] = true;
                     view.hexStatus[i] = MapView.DIMMED;
                 } else {
                     view.hexHighlight[i] = true;
                     view.hexStatus[i] = MapView.HIGHLIGHT;
                 }
 
             } else if (view.hexStatus[i] != MapView.FOG){
                 if (getMapDist(center, new Vector2((int)(i % mapData.bounds.x), (int)(i / mapData.bounds.x))) <= radius) {
                     view.hexDimmed[i] = true;
                     view.hexStatus[i] = MapView.DIMMED;
                 }
             }
         }
     }
 
 	public MapData getMapData() {
         return mapData;
     }
 
     public MapView getMapView(int id) {
         if (id == -1) {
             if (!mapViews.containsKey(id)) {
                 mapViews.put(id, MapView.createMapView(mapData, null));
             }
         }
         return mapViews.get(id);
     }
 
     public int getWidth() {
 		return (int)mapData.bounds.x;
 	}
 
 	public int getHeight() {
 		return (int)mapData.bounds.y;
 	}
 
 	public int getTileWidth() {
 		return (int)mapData.tileSize.x;
 	}
 
 	public int getTileHeight() {
 		return (int)mapData.tileSize.y;
 	}
 
 	public int getMapWidth() {
 		return mapData.getMapWidth();
 	}
 
 	public int getMapHeight() {
 		return mapData.getMapHeight();
 	}
 	
 	public Array<MapData.MapObject> getPlayerSpawns() {
 		Array<MapData.MapObject> spawns = new Array<MapData.MapObject>();
         for (MapData.MapObject obj: mapData.mapObjects) {
             if (obj.type.equals("spawn")) {
                 spawns.add(obj);
             }
         }
         return spawns;
 	}
 	
 	public Array<MapData.MapObject> getControlPoints() {
 		Array<MapData.MapObject> points = new Array<MapData.MapObject>();
         for (MapData.MapObject obj: mapData.mapObjects) {
             if (obj.type.equals("unit")) {
                 points.add(obj);
             }
         }
         return points;
 	}
 
     public boolean isBoardPositionVisible(Player player, Vector2 pos) {
         return mapViews.get(player.getPlayerId()) != null
                 && mapViews.get(player.getPlayerId()).isBoardPositionVisible(pos);
     }
 
     public boolean detectorCanSee(Player player, Array<Unit> units, Vector2 boardPosition) {
         for (int u = 0; u < units.size; u++) {
             Unit unit = units.get(u);
            if (unit.getOwner() == player && unit.getData().isDetector() && getMapDist(unit.getView().getBoardPosition(), boardPosition) <= unit.getData().getSightRange()) return true;
         }
         return false;
     }
 	
 	public static Vector2 boardToMapCoords(MapData data, int bx, int by) {
 		return new Vector2(bx * data.tileSize.x + (by % 2 == 1 ? data.tileSize.x * 0.5f : 0), by * data.tileSize.y * 0.75f);
 	}
 
 	public Vector2 boardToMapCoords(float x, float y) {
 		return HexMap.boardToMapCoords(mapData, (int)x, (int)y);
 	}
 	
 	public static Vector2 mapToBoardCoords(MapData data, float x, float y) {
         Vector2 boardCoords = new Vector2();
 		
 		float dx;
 		float dy = y / (data.tileSize.y * 0.75f);
 		float mx = x % data.tileSize.x;
 		float my = y % (data.tileSize.y * 0.75f);
 		
 		
 		boardCoords.y = (float) Math.floor(dy);
 		if (my < data.tileSize.y * 0.25) {
 			if (Math.floor(dy) % 2 == 1) {
 				mx = (x - data.tileSize.x * 0.5f) % data.tileSize.x;
 			}
 			
 			// if (mx, my) <= (y = -0.5x + .25 * Th) or (mx, my) <= (y = 0.5x - 0.25 * Th)
 			if (my <= -0.5 * mx + 0.25f * data.tileSize.y || my <= 0.5 * mx - 0.25 * data.tileSize.y) {
 				boardCoords.y -= 1;
 			}
 		}
 
 		if (boardCoords.y % 2 == 1) {
 			dx = (x - data.tileSize.x * 0.5f) / data.tileSize.x;
 		} else {
 			dx = x / data.tileSize.x;
 		}
 		boardCoords.x = (float) Math.floor(dx);
 
 		return boardCoords;
 	}
 	
 	
 	/*
 	 * 	  (x - 1!, y + 1)  (x + 1?, y + 1)
 	 * 				  \	   /
 	 * (x - 1, y) --- (x, y) --- (x + 1, y)
 	 * 				  /    \
 	 *    (x - 1!, y - 1)  (x + 1?, y - 1)
 	 * ! --> y % 2 == 0 ? -1 : 0
 	 * ? --> y % 2 == 0 ? 0 : 1
 	 */
 	public static int getMapDist(Vector2 from, Vector2 to) {
         // completion cases:
         if (from.x == to.x) return Math.round(Math.abs(from.y - to.y));
         if (from.y == to.y) return Math.round(Math.abs(from.x - to.x));
 
         // otherwise, move along the gradient
         from = new Vector2(from);
         float dx = to.x - from.x, dy = to.y - from.y;
         if (from.y % 2 == 0 && dx < 0) {
             from.x -= 1;
         } else if (from.y % 2 == 1 && dx > 0) {
             from.x += 1;
         }
 
         if (dy > 0) {
             from.y += 1;
         } else {
             from.y -= 1;
         }
 
         return 1 + getMapDist(from, to);
 	}
 
     public static Vector2[] getAdjacent(int x, int y) {
         Vector2[] adjacent = new Vector2[6];
         adjacent[0] = new Vector2(x + 1, y);
         adjacent[1] = new Vector2(x - 1, y);
         adjacent[2] = new Vector2(x + (y % 2 == 0 ? 0 : 1), y - 1);
         adjacent[3] = new Vector2(x + (y % 2 == 0 ? 0 : 1), y + 1);
         adjacent[4] = new Vector2(x + (y % 2 == 0 ? -1 : 0), y - 1);
         adjacent[5] = new Vector2(x + (y % 2 == 0 ? -1 : 0), y + 1);
         return adjacent;
     }
 
     public static Vector2[] getAdjacent(Vector2 boardPosition) {
         return getAdjacent((int)boardPosition.x, (int)boardPosition.y);
     }
 
     public boolean[] getAvailablePositions(int[] start, int[] radii, boolean[] unavailable, boolean requireTraversible) {
         boolean[] available = new boolean[(int)(mapData.bounds.x * mapData.bounds.y)];
 
         for (int u=0;u<start.length;u++) {
             int unit = start[u];
             available[unit] = true;
             Array<Integer> current = new Array<Integer>(), next = new Array<Integer>();
             current.add(unit);
             for(int r=0;r<=radii[u];r++) {
                 while (current.size > 0) {
 
                     int p = current.pop();
                     available[p] = true;
 
                     // check if this position is traversible and not blocked
                     if (requireTraversible && !mapData.isBoardPositionTraversible((int) (p % mapData.bounds.x), (int) (p / mapData.bounds.x))) unavailable[p] = true;
 
                     if (unavailable[p]) continue; // we can see this point but not beyond it
                     for (Vector2 point: getAdjacent((int)(p % mapData.bounds.x), (int)(p / mapData.bounds.x))) {
                         if (point.x < 0 || point.y < 0 || point.x >= mapData.bounds.x || point.y >= mapData.bounds.y)
                             continue;
                         next.add((int)(point.y * mapData.bounds.x + point.x));
                     }
                 }
                 current.addAll(next);
                 next.clear();
             }
         }
 
         return available;
     }
 }
