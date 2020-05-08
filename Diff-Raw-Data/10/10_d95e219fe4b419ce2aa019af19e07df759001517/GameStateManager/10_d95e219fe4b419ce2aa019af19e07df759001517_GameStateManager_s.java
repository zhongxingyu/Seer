 package com.icbat.game.tradesong;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Texture;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 
 /** Class to keep track of game state data. */
 public class GameStateManager {
     private Inventory inventory = new Inventory();
     private HashSet<Item> allKnownItems = new HashSet<Item>();
     private HashSet<Recipe> allKnownRecipes = new HashSet<Recipe>();
     private HashSet<Workshop> allWorkshops = new HashSet<Workshop>();
 
     public static final String PATH_ITEMS = "items.csv";
     public static final String PATH_RECIPES = "recipes.csv";
     public static final String PATH_SPRITE_ITEMS = "sprites/items.png";
 
 
     public GameStateManager(Tradesong gameInstance) {
         // Load sprites and other assets
         gameInstance.assets.load(PATH_SPRITE_ITEMS, Texture.class);
         gameInstance.assets.finishLoading();
 
 
         // Load data and initialize
         loadItems( (Texture)gameInstance.assets.get(PATH_SPRITE_ITEMS) );
         loadRecipes();
         findWorkshops();
 
 
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
                 x = Integer.parseInt(properties[4].trim());
                 y =  Integer.parseInt(properties[5].trim());
                 rarity =  Integer.parseInt(properties[3].trim());
                 maxStack =  Integer.parseInt(properties[2].trim());
 
                 allKnownItems.add( new Item(name, description, texture, maxStack, rarity, x, y) );
 
             }
 
         }
 
 
 
         // TODO extensible-system
         return false;
     }
 
     /** Must be run after load items! */
     public boolean loadRecipes() {
 
         // Load the main file
         String itemBlob = new FileHandle(PATH_RECIPES).readString();
         String[] lineOfSpec = itemBlob.split("\n");
 
         // Declaring for memory usage
         String[] properties;
         String outputString, workshop, inputTemp;
         Item output;
         ArrayList<Item> recipe;
 
 
         for (String line : lineOfSpec) {
             properties = line.split(",");
 
             // Trim all the properties
             for (int j = 0; j < properties.length; ++j) {
                 properties[j] = properties[j].trim();
 
             }
 
             if (!properties[0].equals("output item")) {
                 recipe = new ArrayList<Item>();
 
                 // output item, workshop, in1, [in2], [in3]
                 outputString = properties[0];
                 output = getItemByName(outputString);
 
                 workshop = properties[1];
 
                 for (int i = 2; i < properties.length; ++i) {
                     inputTemp = properties[i];
                     recipe.add(getItemByName(inputTemp));
 
                 }
 
                Gdx.app.log("",outputString + ":  " + recipe.size());

                 allKnownRecipes.add( new Recipe(output, workshop, recipe) );
 


                 recipe.clear();
 
             }
 
 
 
         }
 
        for (Recipe rec : allKnownRecipes) {
            Gdx.app.log("0", rec.getOutput().getItemName() + ":  " + rec.recipeBySlot.size());
        }

         // TODO error-checking
         // TODO extensible-system
         return false;
     }
 
     private void findWorkshops() {
         for (Recipe recipe : allKnownRecipes) {
             allWorkshops.add( new Workshop(recipe.workshop) );
         }
     }
 
     public Inventory getInventory() {
         return inventory;
     }
 
     /** @return A new copy of the item by name or null if it was not found */
     public Item getItemByName(String name) {
         for (Item item : allKnownItems) {
             if (item.getItemName().equals(name))
                 return new Item(item);
         }
 
         return null;
     }
 
     /** @return A new copy of the workshop by name or null if it was not found */
     public Workshop getWorkshopByName(String name) {
         for (Workshop workshop : allWorkshops) {
             if (workshop.getType().equals(name))
                 return new Workshop(workshop);
         }
         return null;
     }
 
     public HashSet<Workshop> getAllWorkshops() {
         return allWorkshops;
     }
 
     public HashSet<Recipe> getAllKnownRecipes() {
         return allKnownRecipes;
     }
 }
