 package org.blink.game;
 
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.asset.AssetManager;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 import java.util.List;
 import java.util.concurrent.Callable;
 import org.blink.Blink;
 import org.blink.game.model.PlayerModel;
 import org.blink.game.model.light.EnumLight;
 import org.blink.game.model.light.LightHashMap;
 import org.blink.game.model.terrain.BattleField;
 import org.blink.net.message.Connect;
 import org.blink.net.message.Move;
 import org.blink.net.message.Sync;
 import org.blink.net.model.Player;
 import org.blink.net.model.PlayerData;
 
 /**
  *
  * @author cmessel
  */
 public class WorldManager extends AbstractAppState {
 
     private final Blink app;
     private final Node rootNode;
     private AssetManager assetManager;
     private BattleField battleField;
 
     public WorldManager(Blink app, Node rootNode) {
         this.app = app;
         this.rootNode = rootNode;
         this.assetManager = app.getAssetManager();
     }
 
     @Override
     public void update(float tpf) {
     }
 
     public void preLoadModels(List<String> modelNames) {
         for (String s : modelNames) {
             assetManager.loadModel(s);
         }
     }
 
     public void loadLevel() {
         battleField = new BattleField(assetManager, app.getCamera());
         app.getRtsCamera().setTerrainQuad(battleField);
         rootNode.attachChild(battleField);
 
         LightHashMap light = new LightHashMap();
         rootNode.addLight(light.get(EnumLight.AMBIENT));
         rootNode.addLight(light.get(EnumLight.DIRECTIONAL));
     }
 
     public BattleField getTerrainQuad() {
         return battleField;
     }
 
     public void addPlayer(Connect c) {
        Player player = PlayerData.get(c.getOwner());
 
        if (player == null) {
            player = new Player();
            player.setName(c.getOwner());
            PlayerModel p = new PlayerModel(assetManager, player.getName());
            p.setLocalTranslation(c.getVector3f());
            player.setModel(p);
            PlayerData.add(player);
        } else {
            player.setLastMove(c.getVector3f());
            rootNode.detachChildNamed(c.getOwner());
        }
 
        final String name = player.getName();
         app.enqueue(new Callable<Void>() {
 
             public Void call() throws Exception {
                 Player p = PlayerData.get(name);
                 rootNode.attachChild(p.getModel());
                 return null;
             }
         });
     }
 
     public void move(Move m) {
         PlayerModel p = (PlayerModel) rootNode.getChild(m.getOwner());
         p.setDestination(m.getVector3f());
     }
 
     public void sync(Sync s) {
         Player player = PlayerData.get(s.getOwner());
         player.setHealth(s.getHealth());
     }
 }
