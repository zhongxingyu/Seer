 package aic2010.services;
 
 import aic2010.exception.UnknownAddressException;
 import aic2010.exception.UnknownProductException;
 import aic2010.model.Address;
 import aic2010.model.Item;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 
 @WebService
@SOAPBinding(style=SOAPBinding.Style.RPC, parameterStyle=SOAPBinding.ParameterStyle.BARE)
 public interface ShippingService {
     /**
      * Ships items and returns a unique id.
      * @param items
      * @param address
      * @return
      */
     @WebMethod(operationName="ship_items")
     public String shipItems(@WebParam(name="items")Item[] items, @WebParam(name="address")Address address)
             throws UnknownAddressException, UnknownProductException;
 }
