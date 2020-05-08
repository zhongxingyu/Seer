 package beans;
 
 import java.util.*;
 /**
  *
  * @author Alexander Fougner
  */
 public class ShoppingBean {
     private ArrayList<ProductBean> cart;
     
     public ShoppingBean() {
         cart = new ArrayList();
     }
     
 
     /*
     *   Add Product to cart
     *
     */
     public void addProduct(ProductBean bb, int quantity) {
 
         ProductBean pb = find(bb.getId());
         if(pb != null) {
             pb.setQuantity(quantity+pb.getQuantity());
         }
         else {
            bb.setQuantity(quantity);
            cart.add(bb);
             System.out.println("addProduct: cart.size():" + cart.size());
         }          
     }
 
     /*
     *   Remove 
     *   @param quantity 
     *   products with ID 
     *   @param id 
     *   from cart
     */
     public void removeProduct(int id, int quantity) {
         ProductBean pb = find(id);
         if(pb != null) {
             if(quantity > pb.getQuantity())
                 quantity = pb.getQuantity();
             pb.setQuantity(pb.getQuantity() - quantity);
 
             if(pb.getQuantity() < 1) {
                 cart.remove(cart.indexOf(pb));
             }
         }
     }
     
 
     /*
     *   Returns the cart quantity of Product with ID @param id
     *   Returns 0 if product is not found in cart.
     *
     */
     public int getQuantity(int id) {
         ProductBean tmp = find(id);
         if(tmp != null) {
             return tmp.getQuantity();
         } else {
             return 0;
         }
 
     }
 
     /*
     *   Returns the Cart object with ID @param id
     *   if it exists. Returns null otherwise.
     *
     */
     public ProductBean find(int id) {
         Iterator iter = cart.iterator();  // get an iterator
         ProductBean tmp = null;
         while(iter.hasNext()){
             tmp=(ProductBean)iter.next();
             if(tmp.getId()==id){ 
                 return tmp;
             }
         }
         return null;
     }
 
     void clear() {
         cart.clear();
     }
 
     public String getXml(){
         StringBuffer buff = new StringBuffer();
         
         Iterator iter = cart.iterator();
         ProductBean pb = null;
         buff.append("<shoppingcart>");
         
         while(iter.hasNext()){
             pb =(ProductBean)iter.next();
             buff.append("<order>");
             buff.append(pb.getXml());
             /*buff.append("<quantity>");
             buff.append(((Integer)objBuff[1]).intValue());
             buff.append("</quantity>");*/
             buff.append("</order>");            
         }
         buff.append("</shoppingcart>");
         return buff.toString();
     }
 
     public Collection getCart(){
       return cart;
     }
 }
