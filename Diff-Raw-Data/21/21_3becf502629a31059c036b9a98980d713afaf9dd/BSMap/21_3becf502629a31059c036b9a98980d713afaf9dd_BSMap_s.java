 package edu.smcm.gamedev.butterseal;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TiledMapTile;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
 
 public enum BSMap {
     ICE_CAVE_ENTRY(BSAsset.ICE_CAVE_ENTRY, "ice-cave-entry", new BSGameStateActor() {
         @Override
         public void act(BSGameState state) {
         }
 
         @Override
         public void update(BSGameState state) {
         }
 
         @Override
         public void reset(BSGameState state) {
         }
     }),
     ICE_CAVE(BSAsset.ICE_CAVE, "ice-cave", new BSGameStateActor() {
         @Override
         public void act(BSGameState state) {
        	// TODO only act when the player moves or uses a power
        	if(!state.available_powers.contains(BSPower.FIRE)) {
        		state.available_powers.add(BSPower.FIRE);
        	}
             BSMap m = state.currentMap;
             TiledMapTileLayer dark = m.getLayer("uncover");
             TiledMapTileLayer light = m.getLayer("player");
             TiledMapTile invis = BSTile.getTileForProperty(m, "invisible", "true");
 
             for(int row = 0; row < m.playerLevel.getHeight(); row++) {
                 for(int col = 0; col < m.playerLevel.getWidth(); col++) {
                     BSTile curr = new BSTile(row, col);
 
                     Map<String, HashMap<String, String>> pp = curr.getProperties(state.currentMap);
                     HashMap<String, String> props = pp.get("player");
 
                     if(props.containsKey("light")) {
                         if(curr.hasProperty(light, "light", "torch")) {
                             if(BSSession.DEBUG > 3) {
                                 System.out.println("found torch");
                             }
                             for (int i = -1; i <= 1; i++) {
                                 for (int j = -1; j <= 1; j++) {
                                     Cell point = new BSTile(row + i, col + j).getCell(dark);
                                     if(!point.getTile().equals(invis)) {
                                         if(BSSession.DEBUG > 2) {
                                             System.out.printf("Clearing tile %d,%d%n", row + i, col + j);
                                         }
                                         point.setTile(invis);
                                     }
                                 }
                             }
                         } else if (curr.hasProperty(m.playerLevel, "light", "beacon")) {
                             if(BSSession.DEBUG > 3) {
                                 System.out.println("found beacon");
                             }
                             if(curr.hasProperty(light, "beacon", "on")) {
                                 for (int i = -1; i <= 1; i++) {
                                     for (int j = -1; j <= 1; j++) {
                                         BSTile lookingAt = new BSTile(row + i, col + j);
                                         if(lookingAt.isContainedIn(dark)) {
                                             Cell point = lookingAt.getCell(dark);
                                             if(!point.getTile().equals(invis)) {
                                                 if(BSSession.DEBUG > 2) {
                                                     System.out.printf("Clearing tile %d,%d%n", row + i, col + j);
                                                 }
                                                 point.setTile(invis);
                                             }
                                         }
                                     }
                                 }
                                 for(BSDirection d : BSDirection.values()) {
                                     if(BSSession.DEBUG > 2) {
                                         System.out.println("Clearing " + d);
                                     }
                                     BSTile point = new BSTile(curr);
                                     while(point.isContainedIn(dark) &&
                                          !point.hasProperty(m.playerLevel, "wall", "true")) {
                                         point.getCell(dark).setTile(invis);
                                         point.transpose(d.dx, d.dy);
                                     }
                                     if(point.isContainedIn(dark)) {
                                         point.getCell(dark).setTile(invis);
                                     }
                                 }
                             }
                         } else {
                             System.err.printf("Unknown option light=%s%n", props.get("light"));
                         }
                     }
                 }
             }
         }
 
         @Override
         public void update(BSGameState state) {
             BSMap m = state.currentMap;
             TiledMapTileLayer light = m.getLayer("player");
             TiledMapTileLayer dark = m.getLayer("uncover");
 
             // update beacon state
             if (state.isUsingPower && state.selectedPower == BSPower.FIRE) {
                 if (state.currentTile.hasProperty(light, "beacon", "off")) {
                     state.currentTile.setProperty(light, "beacon", "on");
                     if(BSSession.DEBUG > 2) {
                         System.out.println("Lighting beacon.");
                     }
                     TiledMapTile beacon_on = BSTile.getTileForProperty(m, "beacon", "on");
                     state.currentTile.getCell(light).setTile(beacon_on);
 //                } else if (state.currentTile.hasProperty(light, "beacon", "on")) {
 //                    state.currentTile.setProperty(light, "beacon", "off");
 //                    if(BSSession.DEBUG > 2) {
 //                        System.out.println("Unlighting beacon.");
 //                    }
 //                    TiledMapTile beacon_off = BSTile.getTileForProperty(m, "beacon", "off");
 //                    state.currentTile.getCell(light).setTile(beacon_off);
                 }
                 state.isUsingPower = false;
             }
 
             if (state.isUsingPower && state.selectedPower == BSPower.ACTION) {
                 if (state.currentTile.hasProperty(light, "money", "1")) {
                     if (state.music == BSAsset.FIRST_MUSIC) {
                         state.music.pauseMusic(BSGameState.ASSETS);
                         state.music = BSAsset.SECOND_MUSIC;
                         state.music.playMusic(BSGameState.ASSETS);
                     }
                 }
             }
 
             for(int row = 0; row < m.playerLevel.getHeight(); row++) {
                 for(int col = 0; col < m.playerLevel.getWidth(); col++) {
                     BSTile curr = new BSTile(row, col);
                     if(curr.hasProperty(dark, "lit", "true")) {
                         curr.setProperty(m.playerLevel, "wall", "false");
                     } else {
                         curr.setProperty(m.playerLevel, "wall", "true");
                     }
                 }
             }
         }
 
         @Override
         public void reset(BSGameState state) {
         }
 	}),
 	HOUSE(BSAsset.HOUSE, "house", new BSGameStateActor() {
         @Override
         public void act(BSGameState state) {
         }
 
         @Override
         public void update(BSGameState state) {
         }
 
         @Override
         public void reset(BSGameState state) {
         }
     });
 
     static final float PIXELS_PER_TILE = 64;
 
     TiledMap map;
     OrthogonalTiledMapRenderer renderer;
     BSAsset asset;
     String key;
     BSGameStateActor update;
     TiledMapTileLayer playerLevel;
 
     BSMap(BSAsset asset, String key, BSGameStateActor update) {
         this.map = new TmxMapLoader().load(asset.assetPath);
         this.renderer = new OrthogonalTiledMapRenderer(this.map, 1f/BSMap.PIXELS_PER_TILE);
         this.asset = asset;
         this.key = key;
         this.update = update;
         this.playerLevel = this.getLayer("player");
     }
 
     void draw(OrthographicCamera camera) {
         this.renderer.setView(camera);
         this.renderer.render();
     }
 
     BSTile getPlayer(String key) {
         BSTile ret = null;
         int h = playerLevel.getHeight();
         int w = playerLevel.getWidth();
         for(int row = 0; row < h; row++) {
             for(int col = 0; col < w; col++) {
                 ret = new BSTile(row, col);
                 Map<String, String> prop = ret.getProperties(this).get("player");
                 if (prop.containsKey("player") && prop.get("player").equals(key)) {
                     return ret;
                 }
             }
         }
         return ret;
     }
 
     public static BSMap getByKey(String key) {
         for (BSMap m : BSMap.values()) {
             if (m.key.equals(key)) {
                 return m;
             }
         }
         return null;
     }
 
     public void usePower(BSGameState state) {
         update.update(state);
         update.act(state);
     }
 
     public TiledMapTileLayer getLayer(String name) {
         return (TiledMapTileLayer) this.map.getLayers().get(name);
     }
 
     public void reset(BSGameState state) {
         this.update.reset(state);
     }
 }
 
 // Local Variables:
 // indent-tabs-mode: nil
 // End:
