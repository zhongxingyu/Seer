 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.actor.service.app.ws.hello;
 import javax.ejb.Stateless;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import zeroth.actor.service.app.ws.hello.HelloService;
 /**
  * Hello service.
  * @author nilcy
  */
 @Stateless
@WebService(portName = "HelloPort", serviceName = "HelloService", name = "Hello", targetNamespace = "http://zeroth.com/ws/hello", endpointInterface = "zeroth.actor.app.ws.hello.HelloService")
 public class HelloServiceImpl implements HelloService {
     /** コンストラクタ */
     public HelloServiceImpl() {
         super();
     }
     /** {@inheritDoc} */
     @Override
     @WebMethod
     public Object sayHello(@WebParam(name = "name") final String aName) {
         return "Hello " + aName + " !";
     }
 }
