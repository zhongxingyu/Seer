 package sandbox.core.world;
 
 import sandbox.core.entities.Rock;
 import forplay.core.AssetWatcher;
 import forplay.core.ForPlay;
 import forplay.core.Json;
 import forplay.core.Json.Array;
 import forplay.core.Json.Object;
 import forplay.core.ResourceCallback;
 
 public class WorldLoader {
 
     public static void loadLevel(String level, final GoblinzDungeonWorld goblinzDungeonWorld,
             final ResourceCallback<GoblinzDungeonWorld> callback) {
 
         ForPlay.assetManager().getText(level, new ResourceCallback<String>() {
 
             @Override
             public void done(String resource) {
                 
                 Rock rock = new Rock();
                 
                 AssetWatcher assetWatcher = new AssetWatcher(new AssetWatcher.Listener() {
 
                     @Override
                     public void done() {
                         callback.done(goblinzDungeonWorld);
                     }
 
                     @Override
                     public void error(Throwable err) {
                         callback.error(err);
                     }
 
                 });
                 
                 Json.Object document = ForPlay.json().parse(resource);
                 
                 int width = document.getInt("width");
                 int height = document.getInt("height");
                 
                 goblinzDungeonWorld.initWorldTab(width, height);
                 
                 Array array = document.getArray("Entities");
                 for (int i = 0; i < array.length(); i++) {
                     
                     Object entity = array.getObject(i);
                     String type = entity.getString("type");
                     int x = entity.getInt("x");
                     int y = entity.getInt("y");
                     
                     WorldObject worldObject = null;
                     
                     if (Rock.TYPE.equalsIgnoreCase(type)) {
                         worldObject = rock;
                     }
                     
                     
                     if (entity != null) {
                         assetWatcher.add(worldObject.getImage());
                         goblinzDungeonWorld.add(worldObject, x, y);
                     }
                     
                    assetWatcher.start();
                 }
 
             }
 
             @Override
             public void error(Throwable err) {
                 callback.error(err);
             }
         });
 
     }
 }
