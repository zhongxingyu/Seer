 package com.icbat.game.tradesong;
 
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Texture;
 
 import java.util.ArrayList;
 
 
 /** Class to keep track of game state data. Also handles saving/loading of said data. */
 public class GameStateManager {
     private Inventory inventory = new Inventory();
     private ArrayList<Item> allKnownItems = new ArrayList<Item>();
     private ArrayList<Recipe> allKnownRecipes = new ArrayList<Recipe>();
 
     public static final String PATH_ITEMS = "items.csv";
 //    public static final String PATH_RECIPES = "recipes.csv";
     public static final String PATH_SPRITE_ITEMS = "sprites/items.png";
 
 
     public GameStateManager(Tradesong gameInstance) {
         // Load sprites and other assets
         gameInstance.assets.load(PATH_SPRITE_ITEMS, Texture.class);
         gameInstance.assets.finishLoading();
 
 
         // Load data and initialize
         loadItems( (Texture)gameInstance.assets.get(PATH_SPRITE_ITEMS) );
 
 
     }
 
 
 
     /** Saves to a file.
      *
      * @param filename of saveGame
      * @return true if saveGame seems successful*/
     public boolean saveGame(FileHandle filename) {
         return false;
     }
 
     /** Loads from a file.
      *
      * @param filename of saveGame.
      * @return true if loadGame seems successful
      * */
     public boolean loadGame(FileHandle filename){
         return false;
     }
 
     /** Load in items from XML file assets
      *
      * @return true if loadedSuccessfully
      * @see assets/items.csv
      * */
     public boolean loadItems(Texture texture) {
 
         // Load the main file
         String itemBlob = new FileHandle(PATH_ITEMS).readString();
         String[] lineOfSpec = itemBlob.split("\n");
 
         // Declaring for memory usage
         String[] properties;
         String name, description;
         int x, y, rarity, maxStack;
 
         for (String line : lineOfSpec) {
             properties = line.split(",");
 
             if (!properties[0].equals("string_itemName")) {
 
                 // public Item(String itemName, String description, Texture texture,  int maxStack, int rarity, int spriteX, int spriteY) {
                 name = properties[0];
                 description = properties[1];
                 x = Integer.parseInt(properties[2].trim());
                 y =  Integer.parseInt(properties[3].trim());
                 rarity =  Integer.parseInt(properties[4].trim());
                 maxStack =  Integer.parseInt(properties[5].trim());
 
                 allKnownItems.add( new Item(name, description, texture, maxStack, rarity, x, y) );

             }
 
         }
 
 
 
         // TODO see about loading from all from a folder to allow for modding/end-user-adding
         return false;
     }
 
     public Inventory getInventory() {
         return inventory;
     }
 
     public ArrayList<Item> getAllKnownItems() {
         return allKnownItems;
     }
 
     public Item getItemByName(String name) {
        for (Item item : allKnownItems) {
            if (item.getItemName().equals(name))
                return item;
         }

         return null;
     }
 
     public ArrayList<Recipe> getAllKnownRecipes() {
         return allKnownRecipes;
     }
 }
