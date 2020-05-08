 package eu.czerpak.ejb;
 
 import eu.czerpak.model.MyObject;
 import eu.czerpak.model.MyObjectListHolder;
 import eu.czerpak.service.SimpleRemote;
 
 import javax.ejb.Stateless;
 import javax.jws.WebService;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: lukes
  * Date: 5/28/11
  * Time: 2:05 PM
  * To change this template use File | Settings | File Templates.
  */
 @Stateless
 @WebService(endpointInterface = "eu.czerpak.service.SimpleRemote", serviceName = "SimpleEJB")
 public class SimpleEJB implements SimpleRemote
 {
     @Override
     public String sayHello(String name)
     {
         return "Hello, " + name + "!";
     }
 
     @Override
     public List<MyObject> getMyObjectList(int size)
     {
        switch (size) {
            case 10:
                return MyObjectListHolder.getInstance().getList10();
            case 100:
                return MyObjectListHolder.getInstance().getList100();
            case 500:
                return MyObjectListHolder.getInstance().getList500();
            case 1000:
                return MyObjectListHolder.getInstance().getList1000();
            default:
                throw new IllegalArgumentException();
         }
     }
 }
