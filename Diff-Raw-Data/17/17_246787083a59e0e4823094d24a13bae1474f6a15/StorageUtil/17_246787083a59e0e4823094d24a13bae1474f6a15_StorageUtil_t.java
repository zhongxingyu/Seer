 package el.client;
 
 import el.actor.Item;
 import el.protocol.Message;
 import el.utils.ByteUtils;
 
 import java.util.List;
 
import static el.utils.ByteUtils.nex2;
import static el.utils.ByteUtils.nex4;
 import static el.utils.ByteUtils.unsigned;
 
 public class StorageUtil {
     public static void fetchCategories(List<String> categories, Message message) {
         categories.clear();
 
         byte[] bytes = message.getSource();
 
         int num = ByteUtils.unsigned(bytes[3]);
         int categoryStart = 4;
 
         for(int i = 0; i < num; ++i){
             int end = ByteUtils.find(bytes, categoryStart + 1, 0);
 
             categories.add(new String(bytes, categoryStart + 1, end - categoryStart - 1));
             categoryStart += end - categoryStart + 1;
         }
     }
 
     public static void fetchItems(Item[] items, Message message) {
         if(unsigned(message.getSource()[3]) == 255) {
             updateSingleItem(items, message);
         } else {
             updateAllItems(items, message);
         }
     }
 
     private static void updateSingleItem(Item[] items, Message message) {
        int pos = nex2(message.getSource(), 11);
         for (Item item : items) {
             if (item != null && item.pos == pos && item.quantity > 0) {
                item.imageId = nex2(message.getSource(), 5);
                item.quantity = nex4(message.getSource(), 7);
                 item.pos = pos;
                 return;
             }
         }
     }
 
     private static void updateAllItems(Item[] items, Message message) {
         clearItems(items);
 
         int packSize = InventoryUtil.ITEM_UID_ENABLED ? 10 : 8;
         int itemsCount = (message.getSource().length - 5) / packSize;
 
         for(int i = 0; i < itemsCount; ++i) {
             int first = i * packSize + 5;
             Item item = items[i];
             if(item == null) {
                 item = new Item();
                 items[i] = item;
             }
            item.imageId = nex2(message.getSource(), first);
            item.quantity = nex4(message.getSource(), first + 2);
            item.pos = nex2(message.getSource(), first + 6);
         }
     }
 
     public static void clearItems(Item[] items) {
         for (Item item : items) {
             if (item != null) {
                 item.quantity = 0;
             }
         }
     }
 }
