 package li.rudin.rt.core.test.observable;
 
 import java.util.List;
 
import li.rudin.rt.api.handler.RTHandler;
 import li.rudin.rt.api.observable.Observable;
 import li.rudin.rt.core.RT;
 import li.rudin.rt.core.container.ObjectContainer;
 import li.rudin.rt.core.handler.RTHandlerImpl;
 import li.rudin.rt.core.util.MessageQueue;
 import li.rudin.rt.core.util.QueueCreator;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class RemoteObservableTest
 {
 	
 	@Test
 	public void testRemote()
 	{
 		
		RTHandler instance = RT.getInstance("testRemote");
		Observable<Integer> myValue = instance.bind(new Observable<Integer>(1), "myValue");
 		myValue.set(200);
 		
 		//Create client after bean value is set, ensure value is in queue
 		
		RTHandlerImpl rt = (RTHandlerImpl)instance;
 		MessageQueue mq = QueueCreator.getOrCreateQueue(rt, 123);
 		
 		List<ObjectContainer> list = mq.copyAndClear();
 		for (ObjectContainer c: list)
 		{
 			if (c.type.equals("myValue") && c.data != null && c.data.equals(200))
 				return;
 		}
 		
 
 		Assert.fail("value not found!");
 	}
 	
 
 }
