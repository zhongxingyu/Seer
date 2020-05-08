 package li.rudin.rt.core.test.observable;
 
 import li.rudin.rt.api.observable.Observable;
 import li.rudin.rt.core.RT;
 import li.rudin.rt.core.handler.RTHandlerImpl;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class WeakRemoteObservableTest
 {
 	
 	@Test
 	public void testRemote()
 	{
 		
		RTHandlerImpl handler = (RTHandlerImpl)RT.getInstance("weaktest");
 		
 		System.gc();
 		int count = handler.getListeners().size();
 		
 		Assert.assertEquals(count, handler.getListeners().size());
 		
 		Observable<Integer> myValue = handler.bind(new Observable<Integer>(1), "myValue");
 		myValue.set(200);
 		
 		Assert.assertEquals(count+1, handler.getListeners().size());
 
 		myValue = null;
 		System.gc();
 		
 		Assert.assertEquals(count, handler.getListeners().size());
 
 	}
 	
 
 }
