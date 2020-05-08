 package br.usp.ime.futuremarket;
 
 import javax.jws.WebMethod;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 import javax.jws.soap.SOAPBinding.Style;
 
 import br.usp.ime.futuremarket.models.LowestPrice;
 
 @WebService
 @SOAPBinding(style = Style.DOCUMENT)
 public interface Customer {
 
     @WebMethod
     public LowestPrice getLowestPriceForList(String[] products);
     
     @WebMethod
     public DeliveryInfo getShipmentData(PurchaseInfo purchaseInfo);
     
     @WebMethod
     public PurchaseInfo[] makePurchase(String listId, CustomerInfo customerInfo);

 }
