 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ws.whitepages;
 
 import java.util.HashMap;
 import java.util.Map;
 import javax.jws.WebService;
 
 /**
  *
  * @author Wojtek
  */
 @WebService(serviceName = "WhitePagesWSDLService", portName = "WhitePagesWSDLPortTypeBindingPort", endpointInterface = "ws.whitepages.WhitePagesWSDLPortType", targetNamespace = "http://whitepages.ws", wsdlLocation = "WEB-INF/wsdl/WhitePagesWebService/WhitePagesWSDL.wsdl")
 public class WhitePagesWebService {
 
     private Map<String, PersonType> personMap = new HashMap<String, PersonType>();
 
     public java.lang.String addPerson(ws.whitepages.PersonType person) throws ws.whitepages.AddPersonFault {
 
         if (person == null) {
             AddPersonFaultType faultInfo = new AddPersonFaultType();
             faultInfo.setErrorMessage("Person cannot be null");
 
             throw new AddPersonFault("error", faultInfo);
         }
 
         if (person.getPhone() == null) {
             AddPersonFaultType faultInfo = new AddPersonFaultType();
             faultInfo.setErrorMessage("Phone cannot be null");
             faultInfo.setPerson(person);
 
             throw new AddPersonFault("error", faultInfo);
         }
 
         String key = person.getFirstName() + " " + person.getLastName();
         if (personMap.containsKey(key)) {
 
             AddPersonFaultType faultInfo = new AddPersonFaultType();
             faultInfo.setPerson(person);
             faultInfo.setErrorMessage("Person already exists");
 
             throw new AddPersonFault("error", faultInfo);
 
         } else {
             personMap.put(key, person);
             return "done";
         }
     }
 
     public ws.whitepages.PersonArrayType findPerson(ws.whitepages.PersonType person) {
 
         PersonArrayType result = new PersonArrayType();
 
         if (person == null) {
             return result;
         }
 
         for (PersonType savedPerson : personMap.values()) {
             if (savedPerson.firstName.equals(person.firstName)
                     || savedPerson.lastName.equals(person.lastName)) {
 
                result.person.add(savedPerson);
             }
         }
 
         return result;
     }
 }
