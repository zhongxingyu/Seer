 package com.steamedpears.comp3004.models.players.strategies.heuristics;
 
 import com.steamedpears.comp3004.models.Card;
 import com.steamedpears.comp3004.models.PlayerCommand;
 import com.steamedpears.comp3004.models.Wonder;
 import com.steamedpears.comp3004.models.assets.AssetMap;
 import com.steamedpears.comp3004.models.assets.AssetSet;
 import com.steamedpears.comp3004.models.players.Player;
 import static com.steamedpears.comp3004.models.assets.Asset.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static com.steamedpears.comp3004.models.assets.Asset.TRADEABLE_ASSET_TYPES;
 
 public class MilitaryHeuristic extends Heuristic{
 
     @Override
     public int getHeuristic(Player player, PlayerCommand command){
         int heuristic = 0;
 
         Card card = getAddedCard(player, command);
         if(card==null){
             return heuristic;
         }
 
         AssetMap cardAssets = card.getAssets(player);
         AssetMap assets = player.getOptionalAssetsSummary();
         assets.add(player.getAssets());
         AssetMap leftAssets = player.getPlayerLeft().getAssets();
         AssetMap rightAssets = player.getPlayerRight().getAssets();
 
         for(String key: cardAssets.keySet()){
             if(key.equals(ASSET_MILITARY_POWER)){
                 int delta = cardAssets.get(ASSET_MILITARY_POWER);
                int ownPower = assets.get(ASSET_MILITARY_POWER);
                 int leftPower = leftAssets.get(ASSET_MILITARY_POWER);
                 int rightPower = rightAssets.get(ASSET_MILITARY_POWER);
                 if(ownPower<=leftPower && ownPower+delta>=leftPower){
                     heuristic+=3*(5-player.getGame().getAge());
                 }
                 if(ownPower<=rightPower && ownPower+delta>=rightPower){
                     heuristic+=3*(5-player.getGame().getAge());
                 }
             }
         }
         return heuristic;
     }
 
     @Override
     public String getName() {
         return "Military Heuristic";
     }
 
     @Override
     public String getDescription() {
         return "Approximates the value of military yielded by an action";
     }
 }
