 package br.usp.ime.futuremarket;
 
 import javax.jws.WebMethod;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
 import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
 
 import br.usp.ime.futuremarket.models.LowestPrice;
 
 @WebService
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
 public interface Customer {
 
     @WebMethod
     public LowestPrice getLowestPriceForList(String[] products);
 
     @WebMethod
     public DeliveryInfo getShipmentData(PurchaseInfo purchaseInfo);
 
     @WebMethod
     public PurchaseInfo[] makePurchase(String listId, CustomerInfo customerInfo);
 
 }
