 package com.mobi.badvibes.view;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Vector2;
 import com.mobi.badvibes.model.world.World;
 
 /**
  * This class handles the rendering of the world. It runs all person's logic and
  * rendering.
  * 
  * @author Darren
  * 
  */
 public class WorldRenderer
 {
     public World getWorld()
     {
         return world;
     }
     
     public static final float               RAIL_WIDTH        = 800;
     public static final float               RAIL_HEIGHT       = 120;
 
     public static final float               PLATFORM_WIDTH    = 800;
     public static final float               PLATFORM_HEIGHT   = 400;
 
     public static final float               CELL_HEIGHT       = 40;
     public static final float               CELL_WIDTH        = 40;
 
     public static float                     X_OFFSET          = 0;
     public static float                     PLATFORM_Y_OFFSET = 85;
 
     public static float                     RAIL_Y_OFFSET     = 25;
     public static WorldRenderer             Instance;
 
     private World                           world;
 
     private BitmapFont                      infoText;
 
     public boolean                          infoTextTextDirty = false;
 
     public String                           infoTextText      = "";
     public Vector2                          infoTextPosition  = new Vector2();
 
     public float                            infoTextOpacity   = 0;
 
     public ArrayList<ArrayList<PersonView>> masterBucket;
 
     public WorldRenderer(World theWorld)
     {
         Instance    = this;
         world       = theWorld;
 
         infoText = new BitmapFont(Gdx.files.internal("data/Arial65.fnt"), Gdx.files.internal("data/Arial65.png"), true);
         
         // initialize the buckets
         masterBucket = new ArrayList<ArrayList<PersonView>>();
         
         for (int i = 0; i < World.GRID_HEIGHT; i++)
         {
             masterBucket.add(new ArrayList<PersonView>());   
         }
     }
 
     public void render(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer, float delta)
     {
         // drawTiles(shapeRenderer);
         
         world.getTrain().trainView.renderTrain(spriteBatch, delta);
 
         // render 
        for (PersonView p : masterBucket.get(-1))
        {
            p.render(spriteBatch, delta);
        }
 
         // world.getTrain().trainView.renderDoors(spriteBatch, delta);
         
         for (int i = 0; i < World.GRID_HEIGHT; i++)
         {
             for (PersonView p : masterBucket.get(i))
             {
                 p.render(spriteBatch, delta);   
             }
         }
 
         if (infoTextTextDirty)
         {
             infoTextPosition.x = (Gdx.graphics.getWidth() - infoText.getBounds(infoTextText).width) / 2.0f;
             infoTextPosition.y = 50 * (Gdx.graphics.getHeight() / 480.0f);
 
             infoTextTextDirty  = false;
         }
 
         if (infoText != null)
         {
             spriteBatch.begin();
             
 	        infoText.setColor   (0, 0, 0, infoTextOpacity);
 	        infoText.draw       (spriteBatch, infoTextText, infoTextPosition.x, infoTextPosition.y);
 
 	        spriteBatch.end();
         }
     }
 
     public boolean masterBucketContains(PersonView p)
     {
         for (int i = 0; i < World.GRID_HEIGHT; i++)
         {
             if (masterBucket.get(i).contains(p))
                 return true;
 
         }
         return false;
     }
 
     public void addToList(PersonView p, int bucketID)
     {
         removeFromList(p);
 
         if (masterBucketContains(p) == false)
             masterBucket.get(bucketID).add(p);
     }
 
     private void removeFromList(PersonView p)
     {
         for (int i = 0; i < World.GRID_HEIGHT; i++)
         {
             if (masterBucket.get(i).contains(p))
             {
                 masterBucket.get(i).remove(p);
             }
         }
     }
 
     @SuppressWarnings("unused")
     private void drawTiles(ShapeRenderer shapeRenderer)
     {
         shapeRenderer.begin(ShapeType.Rectangle);
             
             shapeRenderer.setColor(Color.RED);
     
             for (int y = 0; y < World.GRID_HEIGHT; y++)
                 for (int x = 0; x < World.GRID_WIDTH; x++)
                     shapeRenderer.rect(
                             GameDimension.X_OFFSET + x * GameDimension.MiniCell.x,
                             GameDimension.PlatformOffset + y * GameDimension.MiniCell.y,
                             GameDimension.MiniCell.x,
                             GameDimension.MiniCell.y);
             
         shapeRenderer.end();
     }
 }
