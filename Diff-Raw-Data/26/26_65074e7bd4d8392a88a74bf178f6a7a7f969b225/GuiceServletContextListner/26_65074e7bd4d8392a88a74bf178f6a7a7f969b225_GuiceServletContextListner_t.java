 package nz.ac.victoria.ecs.kpsmart;
 
 import java.util.Stack;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import nz.ac.victoria.ecs.kpsmart.state.manipulation.HibernateStateManipulationModule;
import nz.ac.victoria.ecs.kpsmart.state.manipulation.InMemoryStateManipulationModule;
 import nz.ac.victoria.ecs.kpsmart.state.manipulation.InMemoryStateManipulator;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.util.Modules;
 
 public final class GuiceServletContextListner implements ServletContextListener {
 	private static Stack<History> injectors = new Stack<GuiceServletContextListner.History>();
 	
 	public static Injector getInjector() {
 		return injectors.peek().injector;
 	}
 	
 	public static void init() {
 		Module[] modules = {
 				new HibernateStateManipulationModule(),
				new InMemoryStateManipulationModule()
 		};
 		
 		injectors.push(new History(Guice.createInjector(modules), modules));
 	}
 	
 	public static void overloadModules(Module... modules) {
 		Module[] newModules = (injectors.isEmpty()) ? 
 				modules : 
 				new Module[] { Modules.override(injectors.peek().modules).with(modules) };
 		
 		injectors.push(new History(Guice.createInjector(newModules), newModules));
 	}
 	
 	public static void restorePreviousInjector() {
 		injectors.pop();
 	}
 
 	@Override
 	public void contextDestroyed(ServletContextEvent arg0) {
 		// NOP
 	}
 
 	@Override
 	public void contextInitialized(ServletContextEvent arg0) {
 		init();
 	}
 	
 	private static final class History {
 		private final Injector injector;
 		private final Module[] modules;
 		
 		private History(final Injector injector, final Module... modules) {
 			this.injector = injector;
 			this.modules = modules;
 		}
 	}
 }
