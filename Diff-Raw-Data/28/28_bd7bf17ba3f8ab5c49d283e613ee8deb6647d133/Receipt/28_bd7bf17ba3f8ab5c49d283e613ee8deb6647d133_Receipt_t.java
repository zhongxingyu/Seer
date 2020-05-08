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
     
     public Customer[] customerDataBase = {
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
         
        lineItems = (LineItem[])resizeArray(lineItems, i++);
         i++;
         lineItems[i] = lineItem;
     }
     public void displayReceipt(){
         System.out.println("Thank you " + customer.getCustomerName() + " for shopping today!");
        for (int j = 0; j<lineItems.length; j++)
            System.out.println(lineItems[j]);
         
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
