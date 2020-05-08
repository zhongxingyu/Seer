 package edu.umich.eecs.tac.props;
 
 import se.sics.isl.transport.TransportReader;
 import se.sics.isl.transport.TransportWriter;
 
 import java.text.ParseException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: leecallender
  * Date: Feb 9, 2009
  * Time: 3:08:04 PM
  * To change this template use File | Settings | File Templates.
  *
  * @author Lee Callender
 * TODO-Add comentary for which indexes relate to which state.
  */
 
 
 public class UserPopulationState extends AbstractKeyedEntryList<Product, UserPopulationState.UserPopulationEntry>{
   private static final long serialVersionUID = 2656209779279027478L;
 
   public UserPopulationState(){}
   
   protected UserPopulationEntry createEntry(Product key) {
     return new UserPopulationEntry(key);  //To change body of implemented methods use File | Settings | File Templates.
   }
 
   protected Class entryClass() {
     return UserPopulationEntry.class;  //To change body of implemented methods use File | Settings | File Templates.
   }
 
   /**
      * Returns the advertiser sales profit for the product. The sales profit is zero if the product is not in the retail
      * catalog.
      *
      * @param product the product
      * @return the advertiser sales profit for the product.
      */
   public final int[] getDistribution(final Product product) {
         int index = indexForEntry(product);
 
         return index < 0 ? null : getDistribution(index);
     }
 
   /**
      * Returns the advertiser sales profit for the product at the index.
      *
      * @param index the index for the product
      * @return the advertiser sales profit for the product.
      */
   public final int[] getDistribution(final int index) {
         return getEntry(index).getDistribution();
     }
 
   public void setDistribution(final Product product, final int[] distribution){
         lockCheck();
 
         int index = indexForEntry(product);
 
         if (index < 0) {
             index = addProduct(product);
         }
 
         setDistribution(index, distribution);
   }
 
   public void setDistribution(final int index, final int[] distribution){
         lockCheck();
         getEntry(index).setDistribution(distribution);
   }
 
   public final int addProduct(final Product product) throws IllegalStateException {
     return addKey(product);
   }
 
   public static class UserPopulationEntry extends AbstractTransportableEntry<Product>{
     /**
       * The serial version ID.
       */
     private static final long serialVersionUID = -4560192080485265951L;
 
     private int[] distribution;
 
     public UserPopulationEntry(Product key){
       setProduct(key);
     }
 
     public UserPopulationEntry(){
     }
 
     public int[] getDistribution() {
       return distribution;
     }
 
     public void setDistribution(int[] distribution) {
       this.distribution = distribution;
     }
 
     /**
          * Returns the product.
          * @return the product.
          */
     public final Product getProduct() {
       return getKey();
     }
 
     /**
          * Sets the product.
          * @param product the product.
          */
     protected final void setProduct(final Product product) {
       setKey(product);
     }
 
     protected String keyNodeName() {
       return Product.class.getSimpleName();  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     protected void readEntry(TransportReader reader) throws ParseException {
       distribution = reader.getAttributeAsIntArray("distribution");
     }
 
     protected void writeEntry(TransportWriter writer) {
       writer.attr("distribution", distribution);
     }
   }
 }
