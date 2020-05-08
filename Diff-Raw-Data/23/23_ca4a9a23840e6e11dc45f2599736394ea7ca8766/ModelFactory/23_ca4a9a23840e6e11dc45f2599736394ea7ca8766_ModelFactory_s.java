 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package main.game.model;
 
 import main.game.Player;
 import com.jme3.asset.AssetManager;
 import com.jme3.font.BitmapText;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import main.game.Game;
 import main.game.model.cell.Cell;
 import main.game.model.cell.DeepWaterCell;
 import main.game.model.cell.LandCell;
 import main.game.model.cell.ShallowWaterCell;
 import main.game.model.creature.*;
 
 /**
  *
  * @author Daniel
  */
 public class ModelFactory
 {
     /**
      * Business logic
      */
     
     public static Duck createDuck(AssetManager assetManager, Game game, Cell location)
     {
         // First create the spatial
         Spatial duckModel = assetManager.loadModel("Models/Duck/duck.mesh.xml");
         duckModel.scale(10f);
         
         
         duckModel.setUserData("modelType", "Duck");
        // duckModel.setUserData("parentId", id);
         
         Duck duck = new Duck(duckModel, game, location);
         
         return duck;
     }
     
     public static Base createBase(AssetManager assetManager, int id, Player player, Cell location)
     {
         // First create the spatial
         Spatial baseModel = assetManager.loadModel("Models/Base/base.mesh.xml");
         baseModel.setLocalScale(0.2f,0.2f,0.15f);
         baseModel.setUserData("modelType", "Base");
         baseModel.setUserData("parentId", id);
         //Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         //baseModel.setMaterial(mat);
         
         // Create the base object
         Base base = new Base(id, baseModel, player, location);
         
         return base;
     }
     
     public static FoodSource createFoodSource(Game game, int id, Cell location)
     {
         AssetManager assetManager = game.getAssetManager();
         
         Node fsModel;
         Spatial fsBase;
         Spatial fsFull;
         
         if (location instanceof DeepWaterCell)
         {
             // First create the spatial
             fsBase = (Node) assetManager.loadModel("Models/Food/Sea/seafood.mesh.xml");
             fsBase.setLocalScale(0.01f, 0.03f, 0.01f);
             fsFull = (Node) assetManager.loadModel("Models/Food/Sea/seafood.mesh.xml");
             fsFull.setLocalScale(0.015f, 0.05f, 0.015f);
         }
         else if (location instanceof ShallowWaterCell)
         {
             // First create the spatial
             fsBase = (Node) assetManager.loadModel("Models/Food/Sea/seafood.mesh.xml");
             fsBase.setLocalScale(0.01f, 0.01f, 0.01f);
             fsFull = (Node) assetManager.loadModel("Models/Food/Sea/seafood.mesh.xml");
             fsFull.setLocalScale(0.015f, 0.03f, 0.015f);
         }
         else
         {
             // First create the spatial
             fsBase = assetManager.loadModel("Models/Food/Land/tree.mesh.xml"); 
             fsBase.setLocalScale(20f);
             fsFull = assetManager.loadModel("Models/Food/Land/banana.mesh.xml"); 
             fsFull.setLocalScale(0.003f);
         }
         fsFull.setName("Full");
         fsModel = new Node();
         fsModel.attachChild(fsBase);
         fsModel.attachChild(fsFull);
         fsModel.setUserData("modelType", "FoodSource");
         fsModel.setUserData("parentId", id);
         
         FoodSource foodSource = new FoodSource(id, fsModel, game, location);
         
         fsModel.setLocalTranslation(location.getWorldCoordinates());
         game.getWorld().getFoodSourceContainer().attachChild(fsModel);
         
         return foodSource;
     }
     
     public static Creature createCreature(Game game, String id, Player player, String creatureType)
     {
         AssetManager assetManager = game.getAssetManager();
         
         Node creatureModel = null;
         Spatial creatureMove = null;
         Spatial creatureStand = null;
         
         Creature creature = null;
         
         //Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         
         if (creatureType.equals(LandCreature.CODE_ID))
         {
            creatureModel = new Node("LandCreature");
             // creatureModel = assetManager.loadModel("Models/Land/stilstaand.mesh.xml");
            // creatureModel.setLocalScale(0.2f);
            
            creatureStand = assetManager.loadModel("Models/Land/stilstaand.mesh.xml");
            creatureStand.setLocalScale(0.2f);
            creatureStand.setName("Stand");
            creatureStand.setUserData("parentId", id);
            creatureStand.setUserData("modelType", creatureType);
            
            creatureMove = assetManager.loadModel("Models/Land/move.mesh.xml");
            creatureMove.setLocalScale(0.2f);
            creatureMove.setName("Move");
            
            creatureModel.attachChild(creatureStand);
            creatureModel.attachChild(creatureMove);
            
            creature = new LandCreature(player, id, creatureModel);
         }
         else if (creatureType.equals(SeaCreature.CODE_ID))
         {
             creatureModel = new Node("SeaCreature");
             creatureMove = assetManager.loadModel("Models/Sea/move.mesh.xml");
             creatureMove.setLocalScale(4f);
             creatureMove.setName("Move");
             creatureMove.setUserData("parentId", id);
             creatureMove.setUserData("modelType", creatureType);
             creatureModel.attachChild(creatureMove);
             creature = new SeaCreature(player, id, creatureModel);
         }
         else if (creatureType.equals(AirborneCreature.CODE_ID))
         {
             creatureModel = new Node("AirborneCreature");
            
            creatureStand = assetManager.loadModel("Models/Air/stilstaand.mesh.xml");
            creatureStand.setLocalScale(0.5f);
            creatureStand.setName("Stand");
            creatureStand.setUserData("parentId", id);
            creatureStand.setUserData("modelType", creatureType);
            
            creatureMove = assetManager.loadModel("Models/Air/move.mesh.xml");
            creatureMove.setLocalScale(0.5f);
            creatureMove.setName("Move");
            creatureMove.setUserData("parentId", id);
            creatureMove.setUserData("modelType", creatureType);
            
            creatureModel.attachChild(creatureStand);
            creatureModel.attachChild(creatureMove);
            creature = new AirborneCreature(player, id, creatureModel);
         }
         
         creatureModel.setUserData("parentId", id);
         creatureModel.setUserData("modelType", creatureType);
         
         /**
         * Health bar (above creature)
         */
 
         BitmapText text = new BitmapText(game.getGuiFont(), false);
         text.setSize(game.getGuiFont().getCharSet().getRenderedSize());
         if (game.getMe().equals(player))
         {
            text.setColor(ColorRGBA.Blue);
         }
         else
         {
            text.setColor(ColorRGBA.Red);
         }
         game.getGuiNode().attachChild(text);
 
         creature.setCreatureHeader(text);
         // Determine spawn location
         //Cell location = player.getBase().getLocation().getWorld().getCells()[32][32];
         Cell location = player.getBase().getClosestSpawnableCell(creature);
         
         creature.setLocation(location);
         creatureModel.setLocalTranslation(location.getWorldCoordinates());
         
         return creature;
     }
     
     public static Explosion createExplosion(Game game, Cell location){
         
         return new Explosion(game, location);
     }
     
     public static Spatial getDeathTomb(Game game){
         Spatial s = game.getAssetManager().loadModel("Models/Death/death.mesh.xml");
         s.setName("Death");
         s.setLocalScale(5f);
         return s;
     }
     
     public static int getCreatureCost(String modelType)
     {
         if (modelType.equals(AirborneCreature.CODE_ID))
         {
             return AirborneCreature.CONST_COST;
         }
         else if (modelType.equals(SeaCreature.CODE_ID))
         {
             return SeaCreature.CONST_COST;
         }
         else
         {
             return LandCreature.CONST_COST;
         }
     }
 }
