 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package aic2010.services;
 
 import aic2010.exception.UnknownProductException;
 import aic2010.model.Product;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
 
 /**
  *
  * @author rudolf
  */
@WebService(targetNamespace="http://infosys.tuwien.ac.at/aic10/ass1/dto/registry")
 @SOAPBinding(style=SOAPBinding.Style.RPC, parameterStyle=SOAPBinding.ParameterStyle.BARE)
 public interface RegistryService {
 
     @WebMethod(operationName="find_supplier")
     public EndpointReferenceType findSupplier(@WebParam(name="product") Product product)
             throws UnknownProductException;
 }
