 package com.mkwhitacre.fns;
 
 import com.mkwhitacre.avro.Game;
 import com.mkwhitacre.avro.Game.Builder;
 import com.mkwhitacre.avro.MetaCriticGame;
 import com.mkwhitacre.avro.VgChartzGame;
 import org.apache.crunch.MapFn;
 import org.apache.crunch.Pair;
 
 public class CreateGameModelFn extends MapFn<Pair<String, Pair<MetaCriticGame, VgChartzGame>>, Game> {
     private static final long serialVersionUID = -6547821754425879007L;
 
     @Override
     public Game map(final Pair<String, Pair<MetaCriticGame, VgChartzGame>> input) {
 
         MetaCriticGame mcGame = input.second().first();
         VgChartzGame vgGame = input.second().second();
 
         Builder builder = Game.newBuilder();
         if(mcGame != null){
             builder.setName(mcGame.getName());
             builder.setScore(mcGame.getScore());
             builder.setUserScore(mcGame.getUserScore());
         }else{
             getCounter("Game", "missingmcgame").increment(1);
         }
 
         if(vgGame != null){
             if(mcGame == null){
                 builder.setName(vgGame.getName());
             }
             builder.setEuropeSales(vgGame.getEuropeSales());
             builder.setNaSales(vgGame.getNaSales());
             builder.setJapanSales(vgGame.getJapanSales());
             builder.setRowSales(vgGame.getRowSales());
             builder.setGlobalSales(vgGame.getGlobalSales());
             builder.setGenre(vgGame.getGenre());
             builder.setPlatform(vgGame.getPlatform());
             builder.setPosition(vgGame.getPosition());
             builder.setYear(vgGame.getYear());
         }else{
             getCounter("Game", "missingvg").increment(1);
         }
 
         getCounter("Game", "createdgame").increment(1);
 
         return builder.build();
     }
 }
