 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package discountstategies;
 
 /**
  *
  * @author andrew
  */
 public class Receipt {
     Customer customer;
     LineItem[] lineItems;
     private int i; //counter used in array manipulation.
     
     private Customer[] customerDataBase = {
         new Customer("101", "Big Bob"),
         new Customer("102", "McHammer"),
         new Customer("103", "Big Bird")
             
     };
      
      
      private Customer findCustomerById(String id) {
         Customer customer = null;
         
         for(Customer c : customerDataBase) {
             if(id.equals(c.getCustomerNo())) {
                 customer = c;
                 break;
             }
         }
         return customer;
     }
      
     public void startNewSale(String customerNo){
        customer = findCustomerById(customerNo);
         
     }
     
     
     public void addNewLineItem(String productId, int qty) {
         
         LineItem lineItem = new LineItem(productId, qty);
         
        lineItems = (LineItem[])resizeArray(i, i++);
         i++;
         lineItems[i] = lineItem;
     }
     public void displayReceipt(){
         System.out.println("Thank you " + customer.getCustomerName() + " for shopping today!");
        for (int i = 0; i<lineItems.length; i++)
            System.out.println(lineItems[i]);
         
     }
      private static Object resizeArray (Object oldArray, int newSize) {
       int oldSize = java.lang.reflect.Array.getLength(oldArray);
       Class elementType = oldArray.getClass().getComponentType();
       Object newArray = java.lang.reflect.Array.newInstance(
          elementType,newSize);
       int preserveLength = Math.min(oldSize,newSize);
       if (preserveLength > 0)
          System.arraycopy (oldArray,0,newArray,0,preserveLength);
       return newArray; }
 
 }
