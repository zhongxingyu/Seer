 package com.steamedpears.comp3004.models.players.strategies.heuristics;
 
 import com.steamedpears.comp3004.models.*;
 import com.steamedpears.comp3004.models.assets.AssetMap;
 import com.steamedpears.comp3004.models.assets.AssetSet;
 import com.steamedpears.comp3004.models.players.Player;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static com.steamedpears.comp3004.models.assets.Asset.TRADEABLE_ASSET_TYPES;
 
 public class ResourceHeuristic extends Heuristic{
 
     @Override
     public int getHeuristic(Player player, PlayerCommand command){
         int heuristic = 0;
         Card card = getAddedCard(player, command);
         if(card==null){
             return heuristic;
         }
 
         AssetSet cardAssetsOptional = card.getAssetsOptional(player);
         AssetMap cardAssets = card.getAssets(player);
         AssetMap assets = player.getOptionalAssetsSummary();
         assets.add(player.getAssets());
 
         List resources = Arrays.asList(TRADEABLE_ASSET_TYPES);
 
         for(String key: cardAssets.keySet()){
             if(resources.contains(key)){
                heuristic+= 3*(2*cardAssets.get(key)-assets.get(key));
             }
         }
 
         for(String key: cardAssetsOptional){
             if(resources.contains(key)){
                heuristic+= 3*(2*cardAssets.get(key)-assets.get(key));
             }
         }
         return heuristic;
     }
 
     @Override
     public String getName() {
         return "Resource Heuristic";
     }
 
     @Override
     public String getDescription() {
         return "Approximates the value of resources yielded by an action";
     }
 }
