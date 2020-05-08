 package version1;
 
 /**
  *
  * @author Nathaniel
  */
 public class InStoreReceipt implements Receipt{
     private DataAccess dataAccess;
     private Customer customer;
     private LineItem[] lineItems;
     private double subTotal;
     private double customerDiscount;
     private double customerTax;
    private double total;
     private double totalDiscount ;
     private double totalWithDiscountAndTax;
 
     public InStoreReceipt(String customerID) {
        lineItems = new LineItem[0];
        dataAccess = new DatabaseDataAccess();
        customer = dataAccess.findCustomer(customerID);
     }
     
     @Override
     public void outputReceipt() {
         for(LineItem i : lineItems){
             subTotal += i.getTtlCost();
             totalDiscount += i.getTtlDiscount();
             
         }
         
         //Calculate all totals necessary for receipt
         subTotal = (double)Math.round(subTotal * 100) / 100;
         totalDiscount = (double)Math.round(totalDiscount * 100) / 100;
        total = (double)Math.round((subTotal - totalDiscount) * 100) / 100;
        customerDiscount = customer.getCustomerDiscount() * subTotal;
         customerTax = (double)Math.round(((subTotal - (customer.getCustomerDiscount() * subTotal)) * customer.getCustomerTax() ) * 100) / 100;
        totalWithDiscountAndTax = (double)Math.round((total - totalDiscount - customerDiscount + customerTax) * 100) / 100;
         
         
         System.out.println("Print receipt...");
         System.out.println("TARGET");
         System.out.println("---------------------------------------------------");
         System.out.println("ID No. | Name        |Quant.| SubTotal | Discount  " );
         System.out.println("---------------------------------------------------");
         for(LineItem i : lineItems){
             i.printLineItem();
         }
         System.out.println("---------------------------------------------------");
         System.out.println("                                           SubTotal");
         System.out.print("                                             ");
         System.out.println(subTotal);
         System.out.println("                               TotalProductDiscount");
         System.out.print("                                           - ");
         System.out.println(totalDiscount);
         System.out.println("                                   CustomerDiscount");
         System.out.print("                                           - ");
         System.out.println(customerDiscount);
         System.out.println("                                                Tax");
         System.out.print("                                             ");
         System.out.println(customerTax);
         System.out.println("                                              Total");
         System.out.print("                                             ");
         System.out.println(totalWithDiscountAndTax);
         System.out.println("---------------------------------------------------");
         System.out.println("Thank you for shopping at Target!");
     }
 
     @Override
     public void addProduct(String productID) {
         if(!checkForDuplicate(productID)){
             LineItem lineItem = new InStoreLineItem(productID);
             LineItem[] temp = new LineItem[lineItems.length + 1];
             System.arraycopy(lineItems, 0, temp, 0, lineItems.length);
             temp[temp.length-1] = lineItem;
             lineItems = temp;
         }
     }
     
     public boolean checkForDuplicate(String productID){
         for(LineItem i : lineItems){
             if(i.checkForDuplicate(productID)){
                 return true;
             }
         }
         
         return false;
     }
 }
