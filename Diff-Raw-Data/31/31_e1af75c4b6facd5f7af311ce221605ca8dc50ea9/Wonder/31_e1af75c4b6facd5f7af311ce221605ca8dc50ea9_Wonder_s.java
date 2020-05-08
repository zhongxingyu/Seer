 package com.steamedpears.comp3004.models;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.steamedpears.comp3004.SevenWonders;
 
 import javax.swing.*;
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class Wonder {
     private class WonderSide{
         //stages are represented by cards for simplicity
         public List<Card> stages;
        public Image image;
         public String startResource;
 
         public WonderSide(JsonObject obj){
             this.stages = Card.parseDeck(obj.getAsJsonArray(PROP_WONDER_STAGES));
            this.image = new ImageIcon(SevenWonders.PATH_IMG_WONDERS+
                     obj.getAsJsonPrimitive(PROP_WONDER_IMAGE).getAsString()
                    +".png"
            ).getImage();
             this.startResource = obj.getAsJsonPrimitive(PROP_WONDER_START_RESOURCE).getAsString();
         }
     }
     //static constants/////////////////////////////////////////////
     public static final String PROP_WONDER_SIDE_A =             "a";
     public static final String PROP_WONDER_SIDE_B =             "b";
     public static final String PROP_WONDER_NAME =               "name";
     public static final String PROP_WONDER_STAGES =             "stages";
     public static final String PROP_WONDER_IMAGE =              "image";
     public static final String PROP_WONDER_START_RESOURCE =     "startResource";
 
     //static methods///////////////////////////////////////////////
     public static Map<String,Wonder> parseWonders(JsonArray wonderList){
         Map<String, Wonder> wonders = new HashMap<String, Wonder>();
         for(JsonElement element: wonderList){
             Wonder newWonder = new Wonder(element.getAsJsonObject());
             wonders.put(newWonder.getName(),newWonder);
         }
         return wonders;
     }
 
     //instance variables///////////////////////////////////////////
     private String name;
     private WonderSide sideA;
     private WonderSide sideB;
     private WonderSide currentSide;
     private int currentStage = 0;
 
     //constructor//////////////////////////////////////////////////
     public Wonder(JsonObject obj){
         this.name = obj.getAsJsonPrimitive(PROP_WONDER_NAME).getAsString();
         this.sideA = new WonderSide(obj.getAsJsonObject(PROP_WONDER_SIDE_A));
         this.sideB = new WonderSide(obj.getAsJsonObject(PROP_WONDER_SIDE_B));
         reset();
     }
 
     //methods//////////////////////////////////////////////////////
     public void reset(){
         currentStage = 0;
         currentSide = sideA;
     }
 
     public boolean buildNextStage(Player player, Card card){
         //TODO: remove card from player's hand
         return false;
     }
 
     public void expendLimitedResource(String resourceName){
         //TODO: make it so the Player can't reuse this ever again, somehow (i.e. if this is Asset.ASSET_BUILD_FREE, disable it)
     }
 
     //setters///////////////////////////////////////////////////////
     public void setSide(String side){
         if(side==PROP_WONDER_SIDE_A){
             this.currentSide = sideA;
         }else if(side==PROP_WONDER_SIDE_B){
             this.currentSide = sideB;
         }
     }
 
     public void randomizeSide(){
         if(Math.random()>0.5){
             setSide(PROP_WONDER_SIDE_A);
         }else{
             setSide(PROP_WONDER_SIDE_B);
         }
     }
 
     //getters///////////////////////////////////////////////////////
     public String getName(){
         return name;
     }
 
     public int getCurrentStage(){
         return currentStage;
     }
 
     public String getSide(){
         if(currentSide==sideB){
             return PROP_WONDER_SIDE_B;
         }else{
             return PROP_WONDER_SIDE_A;
         }
     }
 
     public Card getNextStage(){
         if(currentStage<getStages().size()){
             return getStages().get(currentStage);
         }
         return null;
     }
 
     public List<Card> getStages(){
         return currentSide.stages;
     }
 
     public Map<String, Integer> getAssets(Player p){
         //TODO: get the assets this wonder definitely has
 
         return null;
     }
 
     public List<Set<String>> getOptionalAssetsComplete(Player p){
         //TODO: get a list of all the asset choices this wonder can make
 
         return null;
     }
 
     public Map<String, Integer> getTradeableAssets(){
         Map<String, Integer> result = new HashMap<String, Integer>();
         result.put(currentSide.startResource, 1);
 
         return result;
     }
 
     public String getId(){
         return this.getName()+"_"+this.getSide();
     }
 
     @Override
     public int hashCode(){
         return getName().hashCode();
     }
 }
