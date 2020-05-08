 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package cx.ath.mancel01.dependencyshot.test.event;
 
 import cx.ath.mancel01.dependencyshot.DependencyShot;
 import cx.ath.mancel01.dependencyshot.api.DSInjector;
 import cx.ath.mancel01.dependencyshot.api.event.Event;
import cx.ath.mancel01.dependencyshot.event.EventManagerImpl;
 import java.util.concurrent.TimeUnit;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  *
  * @author mathieuancelin
  */
 public class EventTest {
 
     public static final int NBR = 40;
 
     @Test
     public void broadcast() throws Exception {
         DSInjector injector = DependencyShot.getInjector();
         MyEvent event = injector.getInstance(MyEvent.class);
         MyEvent2 event2 = injector.getInstance(MyEvent2.class);
         MyEvent3 event3 = injector.getInstance(MyEvent3.class);
         MyListener listener = injector.getInstance(MyListener.class);
         MyListener2 listener2 = injector.getInstance(MyListener2.class);
         MyListener3 listener3 = injector.getInstance(MyListener3.class);
         for(int i = 0; i < NBR; i++)
             event.fire();
         for(int i = 0; i < NBR; i++)
             event2.fire();
         for(int i = 0; i < NBR; i++)
             event3.fire();
 
         Assert.assertTrue(listener.getLatch().await(10, TimeUnit.SECONDS));
         Assert.assertTrue(listener2.getLatch().await(10, TimeUnit.SECONDS));
         Assert.assertTrue(listener3.getLatch().await(10, TimeUnit.SECONDS));
 
         System.out.println("listener 1 : " + listener.getCalls() + " events received ...");
         System.out.println("listener 2 : " + listener2.getCalls() + " events received ...");
         System.out.println("listener 3 : " + listener3.getCalls() + " events received ...");
         Assert.assertTrue(listener.getCalls() == NBR);
         Assert.assertTrue(listener2.getCalls() == NBR);
         Assert.assertTrue(listener3.getCalls() == NBR);
     }
 
     @Test
     public void broadcastEvent() throws Exception {
         DSInjector injector = DependencyShot.getInjector();
         Event<CustomEvent> event = injector.getInstance(Event.class);
         CustomEventListener listener = injector.getInstance(CustomEventListener.class);
         for(int i = 0; i < NBR; i++)
             event.fire(new CustomEvent());
         
         Assert.assertTrue(listener.getLatch().await(10, TimeUnit.SECONDS));
 
         System.out.println("listener 1 : " + listener.getCalls() + " events received ...");
         Assert.assertTrue(listener.getCalls() == NBR);
     }
 
 }
