 package game.plugins;
 
 import game.configuration.Configurable;
 import game.configuration.ConfigurableList;
 import game.configuration.errorchecks.ListMustContainCheck;
 import game.utils.Utils;
 
 import java.lang.reflect.Modifier;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.HashSet;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 import org.reflections.Reflections;
 import org.reflections.util.ClasspathHelper;
 import org.reflections.util.ConfigurationBuilder;
 import org.reflections.util.FilterBuilder;
 
 public class PluginManager extends Configurable {
 	
 	private Reflections internal;
 	
 	public ConfigurableList<String> packages = new ConfigurableList<>(this);
 	public ConfigurableList<String> paths = new ConfigurableList<>(this);
 	
 	public PluginManager() {
 		addOptionChecks("packages", new ListMustContainCheck("game.plugins"));
 		
 		this.addObserver(new Observer() {
 			@Override
 			public void update(Observable o, Object m) {
 				if (m instanceof Change) {
 					Change change = (Change)m;
 					if (change.getPath().contains("packages.") || change.getPath().contains("paths.")) {
 						reset();	
 					}
 				}
 			}
 		});
 		
 		packages.add("game.plugins");
 	}
 	
 	private void reset() {
 		ConfigurationBuilder conf = new ConfigurationBuilder();
 		
 		if (!paths.isEmpty()) {
 			ClassLoader loader = null;
 			try {
 				URL[] urls = new URL[paths.size()];
 				int i = 0;
 				for(String path: paths)
 					urls[i++] = new URI(path).toURL();
 				loader = new URLClassLoader(urls, getClass().getClassLoader());
 			} catch (MalformedURLException | URISyntaxException e) {
 				e.printStackTrace();
 			}
 			
 			conf.addClassLoader(loader);
 		}
 		
 		FilterBuilder filter = new FilterBuilder();
 		for (String p: packages) {
 			filter.include(FilterBuilder.prefix(p));
 		}
 		conf.filterInputsBy(filter);
 		
 		conf.addUrls(ClasspathHelper.forClassLoader());
 		
 		internal = new Reflections(conf);
 	}
 	
 	public <T> Set<T> getInstancesOf(Class<T> base) {
 		Set<Class<? extends T>> all = internal.getSubTypesOf(base);
 		Set<T> ret = new HashSet<>();
 		
 		try {
 			for (Class<? extends T> c: all) {
 			if (Utils.isConcrete(c)	&& Modifier.isPublic(c.getModifiers())
 					&& (c.getEnclosingClass() == null || Modifier.isStatic(c.getModifiers())))
 				
 					ret.add(c.newInstance());
 			}
 		} catch (InstantiationException | IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return ret;
 	}
 	
 	public <T> Set<T> getCompatibleInstancesOf(Class<T> base, Constraint c) {
 		Set<T> all = getInstancesOf(base);
 		Set<T> ret = new HashSet<>();
 		
 		for (T o: all) {
 			if (c.isValid(o))
 				ret.add(o);
 		}
 		
 		return ret;
 	}
 
 }
