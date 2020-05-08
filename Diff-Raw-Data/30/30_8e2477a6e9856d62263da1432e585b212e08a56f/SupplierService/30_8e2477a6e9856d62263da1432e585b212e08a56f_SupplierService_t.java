 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package aic2010.services;
 
 import aic2010.exception.UnknownProductException;
 import aic2010.model.Product;
 import java.math.BigDecimal;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 
 /**
  *
  * @author rudolf
  */
 
@WebService(targetNamespace="http://infosys.tuwien.ac.at/aic10/ass1/dto/supplier")
 @SOAPBinding(style=SOAPBinding.Style.RPC, parameterStyle=SOAPBinding.ParameterStyle.BARE)
 public interface SupplierService {
 
     @WebMethod(operationName="order")
     public BigDecimal order(@WebParam(name="product") Product product,
                         @WebParam(name="amount")Integer amount)
     throws UnknownProductException;
 }
