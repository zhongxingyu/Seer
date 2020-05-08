 package beans;
 
 import java.util.*;
 /**
  *
  * @author Olle Eriksson
  */
 public class ShoppingBean {
     private Collection cart;
     
     public ShoppingBean() {
         cart = new ArrayList();
     }
     
     public void addProduct(ProductBean bb, int quantity) {
         
         /*
         *   A Cart item is represented as an array
         *   [0] -> ProductBean
         *   [1] -> Integer quantity
         */
 
         Object newItem[] = null;
         ProductBean tmpBean = null;
 
 	// if the cart is empty just add the book
 
         if (cart.isEmpty()){
             newItem = new Object[2];
             newItem[0]=bb;
             newItem[1]=new Integer(quantity);    
             cart.add(newItem);
         }
 
 	// otherwise we need to check if this book already
 	// is in the cart
 
         else{
 	    Iterator iter = cart.iterator();  // get an iterator
 	    Object tmpArr[];
 	    boolean found = false;
 	    while(iter.hasNext()){
 		tmpArr=(Object[])iter.next();
 
 		// check if we found the book
 
 		if(((ProductBean)tmpArr[0]).getId()==bb.getId()){ 
 
 		    // yes, increase the quantity
 
 		    Integer tmpAntal = (Integer)tmpArr[1];
 		    tmpArr[1]=new Integer(tmpAntal.intValue()+quantity); 
 		    found= true;
 		}
 		
 	    }
 	    
 	    // if we didn't find it, add it
 	    
 	    if(!found){
 		newItem = new Object[2];
 		newItem[0]=bb;
 		newItem[1]=new Integer(quantity);    
 		cart.add(newItem);
 		System.out.println("addProduct: cart.size():" + cart.size());
 	    }
 	    
         }          
     }    
 
     // remove some copies of a book from the cart
 
     public void removeProduct(int id, int quantity) {
 
 	// if must not be empty
 
         if(!cart.isEmpty()){
             Iterator iter = cart.iterator();
             Object tmpArr[];
 
 	    // search for the book
 
             while(iter.hasNext()){
                 tmpArr=(Object[])iter.next();
                 if(((ProductBean)tmpArr[0]).getId()==id){
 
 		    // found
 
                     Integer tmpAntal = (Integer)tmpArr[1];
 
 		    // if all copies removed, remove the book
 		    // from the cart
 
                     if(tmpAntal.intValue()<=quantity){
                         iter.remove();
                     }
                     else{
 
 			// else reduce quantity
 
                         tmpArr[1]=new Integer(tmpAntal.intValue()-quantity);
                     }
                 }
             }
         }
     }     
     
 
     /*
     *   Returns the cart quantity of Product with ID @param id
     *   Returns 0 if product is not found in cart.
     *
     */
     public int getQuantity(int id) {
         Object[] tmpArr = find(id);
        if(tmpArr) {
            return tmpArr[1].intValue();
         } else {
             return 0;
         }
 
     }
 
     /*
     *   Returns the Cart object with ID @param id
     *   if it exists. Returns null otherwise.
     *
     */
     public Object[] find(int id) {
         Iterator iter = cart.iterator();  // get an iterator
         Object tmpArr[];
         while(iter.hasNext()){
             tmpArr=(Object[])iter.next();
             if(((ProductBean)tmpArr[0]).getId()==id){ 
                 return tmpArr;
             }
         }
         return null;
     }
 
     // clear the cart
 
     void clear() {
         cart.clear();
     }
     
     // create an XML document out of the shopping cart
 
     public String getXml(){
         StringBuffer buff = new StringBuffer();
         
         Iterator iter = cart.iterator();
         Object objBuff[] = null;
         buff.append("<shoppingcart>");
         
         while(iter.hasNext()){
             objBuff =(Object[])iter.next();
             buff.append("<order>");
             buff.append(((ProductBean)objBuff[0]).getXml());
             buff.append("<quantity>");
             buff.append(((Integer)objBuff[1]).intValue());
             buff.append("</quantity>");
             buff.append("</order>");            
         }
         buff.append("</shoppingcart>");
         return buff.toString();
     }
 
     // get the cart
 
     public Collection getCart(){
       return cart;
     }
     
 }
