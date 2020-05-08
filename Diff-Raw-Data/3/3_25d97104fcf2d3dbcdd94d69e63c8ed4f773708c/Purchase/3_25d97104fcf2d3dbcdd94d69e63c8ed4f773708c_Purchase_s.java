 package br.usp.ime.futuremarket;
 
 public class Purchase {
     private long number;
     private String shipper;
     private ShopList shopList;
     private CustomerInfo customerInfo;
     private boolean isPaid = false;
 
     public Purchase() {
         // Avoiding IllegalAnnotationExceptions
     }
 
     public Purchase(final long number, final String shipper, final ShopList list,
             final CustomerInfo customer) {
         this.number = number;
         this.shipper = shipper;
         this.shopList = list;
         this.customerInfo = customer;
     }
 
     public String getUniqueId() {
         return getSeller() + "/" + getNumber();
     }
 
     public String getSeller() {
         return shopList.getSeller();
     }
 
     public boolean isPaid() {
         return isPaid;
     }
 
     public long getNumber() {
         return number;
     }
 
     public void setNumber(final long number) {
         this.number = number;
     }
 
     public String getShipper() {
         return shipper;
     }
 
     public void setShipper(final String shipperEndpoint) {
         this.shipper = shipperEndpoint;
     }
 
     public ShopList getShopList() {
         return shopList;
     }
 
     public void setShopList(final ShopList shopList) {
         this.shopList = shopList;
     }
 
     public double getPrice() {
         return shopList.getPrice();
     }
 
     public CustomerInfo getCustomerInfo() {
         return customerInfo;
     }
 
     public void setCustomerInfo(final CustomerInfo customerInfo) {
         this.customerInfo = customerInfo;
     }
 
     public boolean getIsPaid() {
         return isPaid;
     }
 
     public void setIsPaid(final boolean isPaid) {
         this.isPaid = isPaid;
     }
 }
