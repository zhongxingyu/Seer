 package net.premereur.mvp.core.guice;
 
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertSame;
 import net.premereur.mvp.TestBase;
 import net.premereur.mvp.core.Event;
 import net.premereur.mvp.core.EventBus;
 import net.premereur.mvp.core.Presenter;
 import net.premereur.mvp.core.View;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 import com.google.inject.Module;
 import com.google.inject.Singleton;
 
 public class ConcurrencyTest extends TestBase {
 
 	static class Capturer {
 		MyPresenter target;
 
 		void capture(MyPresenter target) {
 			this.target = target;
 		}
 	}
 
 	static interface MyEventBus extends EventBus {
 		@Event(MyPresenter.class)
 		void event(Capturer capturer);
 	}
 
 	public static class MyPresenter implements Presenter<MyView, MyEventBus> {
 
 		Dependency dependency;
 
 		@Override
 		public void setEventBus(MyEventBus eventBus) {
 		}
 
 		@Override
 		public void setView(MyView view) {
 		}
 
 		@Inject
 		public MyPresenter(Dependency dependency) {
 			this.dependency = dependency;
 		}
 
 		public void onEvent(Capturer capturer) {
 			capturer.capture(this);
 		}
 	}
 
 	public static class MyView implements View {
 
 	}
 
 	static interface Dependency {
 	}
 
 	@Singleton
 	static class MyDependency implements Dependency {
 
 	}
 
 	private Module testModule = new AbstractModule() {
 		@Override
 		protected void configure() {
 			bind(Dependency.class).to(MyDependency.class).asEagerSingleton();
 		}
 	};
 
 	private GuiceEventBusFactory guiceEventBusFactory;
 
 	@Before
 	public void createFactory() {
 		// It is important that the tested event busses are created by the same factory instance
 		guiceEventBusFactory = new GuiceEventBusFactory(testModule);		
 	}
 	
 	@Test
 	public void shouldCreateDifferentBusses() throws Exception {
 		MyEventBus eventBus1 = guiceEventBusFactory.createEventBus(MyEventBus.class);
 		MyEventBus eventBus2 = guiceEventBusFactory.createEventBus(MyEventBus.class);
 		assertNotSame(eventBus1, eventBus2);
 	}
 
 	@Test
 	public void shouldCreateDifferentPresentersForDifferentEventBus() throws Exception {
 		MyEventBus eventBus1 = guiceEventBusFactory.createEventBus(MyEventBus.class);
 		MyEventBus eventBus2 = guiceEventBusFactory.createEventBus(MyEventBus.class);
 		Capturer capturer1 = new Capturer();
 		eventBus1.event(capturer1);
 		Capturer capturer2 = new Capturer();
 		eventBus2.event(capturer2);
 		assertNotSame(capturer1.target, capturer2.target);
 	}
 
 	@Test
 	public void shouldUseSamePresenterForSameEventBus() throws Exception {
 		MyEventBus eventBus1 = guiceEventBusFactory.createEventBus(MyEventBus.class);
 		Capturer capturer1 = new Capturer();
 		eventBus1.event(capturer1);
 		Capturer capturer2 = new Capturer();
 		eventBus1.event(capturer2);
 		assertSame(capturer1.target, capturer2.target);
 	}
 
 	@Test
 	public void shouldUseSameSingletonDependencyForAllEventBusses() throws Exception {
 		MyEventBus eventBus1 = guiceEventBusFactory.createEventBus(MyEventBus.class);
 		MyEventBus eventBus2 = guiceEventBusFactory.createEventBus(MyEventBus.class);
 		Capturer capturer1 = new Capturer();
 		eventBus1.event(capturer1);
 		Capturer capturer2 = new Capturer();
 		eventBus2.event(capturer2);
 		assertSame(capturer1.target.dependency, capturer2.target.dependency);
 	}
 
 }
