 package tests.deserialization;
 
 import java.util.ArrayList;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import ecologylab.generic.HashMapArrayList;
 import ecologylab.serialization.JSONTools;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.annotations.simpl_collection;
 import ecologylab.serialization.annotations.simpl_map;
 import ecologylab.serialization.annotations.simpl_map_key_field;
 import ecologylab.serialization.annotations.simpl_nowrap;
 import ecologylab.serialization.annotations.simpl_scalar;
 import ecologylab.serialization.formatenums.StringFormat;
 import ecologylab.serialization.types.element.IMappable;
 
 /**
  * Test cases for some edge behaviors of the JSON pull deserializer.
  * 
  * @author quyin
  * 
  */
 public class JSONPullDeserializerTest extends Assert
 {
 
   public static class Item implements IMappable<String>
   {
 
     @simpl_scalar
     String                         name;
 
     @simpl_scalar
     String                         payload;
 
     @simpl_map("assoc_item")
     HashMapArrayList<String, Item> assocItems;
 
     @simpl_collection("alias")
     ArrayList<String>              aliases = new ArrayList<String>();
 
     public Item()
     {
     }
 
     public Item(String name, String payload)
     {
       this.name = name;
       this.payload = payload;
     }
 
     @Override
     public String key()
     {
       return name;
     }
 
     /**
      * (If call with null, create an empty collection).
      * 
      * @param assocItem
      */
     public void addAssocItem(Item assocItem)
     {
       if (assocItems == null)
         assocItems = new HashMapArrayList<String, Item>();
       if (assocItem != null)
         assocItems.put(assocItem.key(), assocItem);
     }
 
     /**
      * (If call with null, create an empty collection).
      * 
      * @param alias
      */
     public void addAlias(String alias)
     {
       if (aliases == null)
         aliases = new ArrayList<String>();
       if (alias != null)
         aliases.add(alias);
     }
 
   }
 
   public static class ItemSet
   {
 
     @simpl_scalar
     String                         setName;
 
     @simpl_nowrap
     @simpl_map("item")
     @simpl_map_key_field("name")
     HashMapArrayList<String, Item> items;
 
     public void addItem(Item item)
     {
       if (items == null)
         items = new HashMapArrayList<String, Item>();
       items.put(item.key(), item);
     }
   }
 
   static SimplTypesScope tscope;
 
   static
   {
     tscope = SimplTypesScope.get("test-json-pull-deserializer",
                                  Item.class,
                                  ItemSet.class);
   }
 
   Item                   item1;
 
   Item                   item2;
 
   ItemSet                itemSet;
 
   @Before
   public void prepare()
   {
     item1 = new Item("item1", "an awesome item");
     item2 = new Item("item2", "an even more awesome item!");
     itemSet = new ItemSet();
     itemSet.addItem(item1);
     itemSet.addItem(item2);
   }
 
   void serializeAndValidate() throws SIMPLTranslationException
   {
     String json = SimplTypesScope.serialize(itemSet, StringFormat.JSON).toString();
     System.out.println("serialized json string:\n" + json);
     assertTrue(JSONTools.validate(json));
 
     ItemSet newItemSet = (ItemSet) tscope.deserialize(json, StringFormat.JSON);
     assertNotNull(newItemSet);
     assertNotNull(newItemSet.items);
     assertEquals(2, newItemSet.items.size());
     assertEquals("item1", newItemSet.items.get(0).key());
     assertEquals("item2", newItemSet.items.get(1).key());
   }
 
   /**
    * Basic de/serialization.
    * 
    * @throws SIMPLTranslationException
    */
   @Test
   public void deSerializeItems() throws SIMPLTranslationException
   {
     serializeAndValidate();
   }
 
   /**
    * In a collection of objects (such as ItemSet.items), test if an empty map in the end of the
    * serialization of an object will cause problem for parsing the next object.
    * 
    * @throws SIMPLTranslationException
    */
   @Test(timeout = 1000)
   public void deSerializeEmptyMapInCollectionJSON() throws SIMPLTranslationException
   {
     item1.addAssocItem(null);
 
     serializeAndValidate();
   }
 
   @Test(timeout = 1000)
   public void deSerializeNonEmptyMapInCollectionJSON() throws SIMPLTranslationException
   {
     item1.addAssocItem(item2);
 
     serializeAndValidate();
   }
 
   /**
    * A collection of scalars can also mess up following tokens.
    * 
    * @throws SIMPLTranslationException
    */
  @Test(timeout = 1000)
   public void deSerializeEmptyCollectionOfScalarsJSON() throws SIMPLTranslationException
   {
     item1.addAlias(null);
 
     serializeAndValidate();
   }
 
   @Test(timeout = 1000)
   public void deSerializeNonEmptyCollectionOfScalarsJSON() throws SIMPLTranslationException
   {
     item1.addAlias("item one");
 
     serializeAndValidate();
   }
 
 }
