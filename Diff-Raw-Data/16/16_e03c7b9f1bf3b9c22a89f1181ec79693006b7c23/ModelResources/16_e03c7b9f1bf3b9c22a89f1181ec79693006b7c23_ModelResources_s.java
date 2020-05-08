 package org.sankozi.rogueland.resources;
 
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.google.inject.Singleton;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.sankozi.rogueland.model.Tile;
 import org.sankozi.rogueland.model.coords.Direction;
 import org.sankozi.rogueland.model.guid.Guid;
 
 import java.awt.*;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import static org.sankozi.rogueland.resources.ResourceProvider.*;
 
 /**
  * Class providing resources for
  *
  * @author sankozi
  */
 @Singleton
 public final class ModelResources {
     private final static Logger LOG = LogManager.getLogger(ModelResources.class);
 
     Cache<Integer, Image> cachedImages = CacheBuilder.newBuilder()
                         .concurrencyLevel(1)
                         .expireAfterAccess(10, TimeUnit.MINUTES)
                         .build();
 
     private Image getImageOrLoad(Guid guid, Callable<String> imagePath){
         try {
            return cachedImages.get(guid.getGuid(), () -> {
                return getImage(imagePath.call());
            });
         } catch (ExecutionException e) {
             throw new RuntimeException("Error while loading " + guid
                     + " " + e.getMessage(), e);
         }
     }
 
     public Image getImageForTileType(Tile.Type tileType){
         return getImageOrLoad(tileType, () -> "tiles/" + tileType.name().toLowerCase() + ".png" );
     }
 
     public Image getImageForWeaponDirection(Direction dir){
         return getImageOrLoad(dir, () -> "tiles/javelin-" + dir.numpadNumber + ".png" );
     }
 }
