 package squeak.content;
 
 import android.content.Context;
 import android.support.v4.content.AsyncTaskLoader;
 import android.util.SparseArray;
 
 import com.google.gson.stream.JsonReader;
 
 import squeak.model.Item;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Locale;
 
 public final class ItemAssetLoader extends AsyncTaskLoader<SparseArray<Item>> {
     public static final int LOADER_ID = 0;
 
     private enum Key {
         CLASSIFICATION, NAME, ITEM_ID, TYPE, ENVIRONMENTS, DESCRIPTION, 
         THUMBNAIL, STATUS, IS_REMOVABLE, CATEGORY, THUMBNAIL_GRAY, LIMITED_EDITION, 
         IS_GIVABLE, IS_TRADABLE, IS_HIDDEN, MAX_INVENTORY_QUANTITY, THUMBNAIL_LARGE,
         MAPIMAGES, POWER, POWER_TYPE, POWER_TYPE_NAME, POWER_TYPE_ICON, POWER_BONUS,
         POWER_BONUS_FORMATTED, ATTRACTION_BONUS, ATTRACTION_BONUS_FORMATTED, LUCK,
         LUCK_FORMATTED, CHEESE_EFFECT, MIN_POINTS, HAS_SKINS, IS_ENABLED, RECIPE_LIST,
         PRODUCED_ITEM, ACTION_VERB, ACTION_DESCRIPTION, IS_SINGLE_OPEN, IMAGEFRONT,
         IMAGEBACK, MESSAGE, BUTTONTEXT, CONSUME_METHOD, MIN_TRINKET_SLOT_LEVEL,
         IMAGE_TRAP, DONATION_REWARD_TYPE, DONATION_COST, MARKETING_TEXT;
     }
     
     private static final String ITEM_ASSETS_FILENAME = "api_get_item_all.json";
     private SparseArray<Item> items;
 
     public ItemAssetLoader(Context context) {
         super(context);
     }
 
     @Override
     public SparseArray<Item> loadInBackground() {
         JsonReader reader = null;
         items = new SparseArray<Item>();
         try {
             InputStream is = getContext().getAssets().open(ITEM_ASSETS_FILENAME);
             reader = new JsonReader(new InputStreamReader(is));
             reader.beginArray();
             while (reader.hasNext()) {
                 reader.beginObject();
                 readItemObject(reader);
                 reader.endObject();
             }
             reader.endArray();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (reader != null) {
                     reader.close();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return items;
     }
 
     /*
         "item_id":1,
         "type":"balack_lantern_map_piece",
         "classification":"map_piece",
         "environments":{"balacks_cove":"balacks_cove"},
         "mapImages":[],
         "name":"Balack's Lantern",
         "description":"This magical lantern, lit with a Dragon Ember is the only light source hot enough to remain lit in the dampness of Balack's Cove.",
         "thumbnail":"https:\/\/www.mousehuntgame.com\/images\/items\/maps\/69c29dfc90eea078ed36a05371499af3.jpg?cv=185",
         "status":null,
         "is_removable":true,
         "category":false,
         "thumbnail_gray":"https:\/\/www.mousehuntgame.com\/images\/items\/maps\/gray\/3ec0cec45164155d55cab27513dcad20.jpg?cv=185",
         "limited_edition":false,"is_givable":false,"is_tradable":false,
         "is_hidden":false,"max_inventory_quantity":null
      */
     private void readItemObject(JsonReader reader) throws IOException {
         int itemId = -1;
         Item item = new Item();
         while (reader.hasNext()) {
            Key key = Key.valueOf(reader.nextName().toUpperCase(Locale.getDefault()));
             switch (key) {
                 case ITEM_ID:
                     itemId = reader.nextInt();
                     item.setId(itemId);
                     break;
                 case NAME:
                     item.setName(reader.nextString());
                     break;
                 case CLASSIFICATION:
                     item.setClassification(reader.nextString());
                     break;
                 case TYPE:
                     item.setType(reader.nextString());
                     break;
                 default:
                     reader.skipValue();
                     break;
             }
         }
         items.append(itemId, item);
     }
 
 }
