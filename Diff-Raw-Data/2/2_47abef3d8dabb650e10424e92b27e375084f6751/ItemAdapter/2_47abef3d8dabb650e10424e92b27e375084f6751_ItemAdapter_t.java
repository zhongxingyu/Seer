 package dungeon.load.adapters;
 
 import dungeon.models.Item;
 import dungeon.models.ItemType;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.adapters.XmlAdapter;
 
 public class ItemAdapter extends XmlAdapter<ItemAdapter, Item> {
   @XmlAttribute
   public int id;
 
   @XmlAttribute
   public String type;
 
   @Override
   public Item unmarshal (ItemAdapter adapter) throws Exception {
     return new Item(adapter.id, ItemType.valueOf(adapter.type));
   }
 
   @Override
   public ItemAdapter marshal (Item item) throws Exception {
     ItemAdapter adapter = new ItemAdapter();
     adapter.id = item.getId();
    adapter.type = item.getType().getName();
 
     return adapter;
   }
 }
