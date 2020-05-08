 package org.uva.training.handler;
 
 import org.apache.commons.lang.StringUtils;
 import org.uva.training.entity.Item;
 import org.uva.training.entity.Product;
 import org.uva.training.entity.Type;
 import org.uva.training.utils.RawEntry;
 
 /**
  * This is the base class of any of {@link Product} handlers.<br/>
  * The goal is to take an {@link RawEntry} class send by sub-class and build an {@link Item} if possible.<br/>
  * <br/>
  * The method used to determine the product is the {@link StringUtils#getLevenshteinDistance(String, String)} algorithm, but we could use another library like
  * Lucene.<br/>
  * In this case, I would have injected a <i>ProductService</i> able to search a product by his type.
  * 
  * @author uvachon
  */
 public abstract class ProductItemHandler extends Handler<Item, RawEntry> {
   private static final int THRESHOLD = 3;
 
    public ProductItemHandler(Handler<Item, RawEntry> successor) {
       super(successor);
    }
 
    @Override
    public Item handlerRequest(RawEntry rawEntry) {
       return super.handlerRequest(rawEntry);
    }
 
    // assume the parsing of numbers was assured correctly by the regex
    // TODO: define the behavior while a number format exception occurred, continue process anyway?
    protected Item buildItem(RawEntry rawEntry, Type type, String[] keywords) {
       if (null == rawEntry) {
          return null;
       }
 
       Item item = null;
       String[] terms = StringUtils.split(rawEntry.getName());
       for (String term : terms) {
          for (String keyword : keywords) {
            if (StringUtils.getLevenshteinDistance(term, keyword) <= 1) {
                Product product = new Product(type, rawEntry.getName(), rawEntry.isImported());
                item = new Item(product, Integer.parseInt(rawEntry.getQuantity()), Float.parseFloat(rawEntry.getPrice()));
             }
          }
       }
       return item;
    }
 }
