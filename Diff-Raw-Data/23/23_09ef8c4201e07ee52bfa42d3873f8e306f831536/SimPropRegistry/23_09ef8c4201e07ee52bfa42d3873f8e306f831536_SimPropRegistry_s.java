 package gui.pluginRegistry;
 
 import gui.customElements.accordion.ListEntry;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import javax.swing.JOptionPane;
 
 import log.LogLevel;
 import log.Logger;
 
 import org.reflections.Reflections;
 import org.reflections.scanners.FieldAnnotationsScanner;
 import org.reflections.util.ClasspathHelper;
 
 import annotations.BoolProp;
 import annotations.BoolSimulationProperty;
 import annotations.FloatProp;
 import annotations.FloatSimulationProperty;
 import annotations.IntProp;
 import annotations.IntSimulationProperty;
 import annotations.SimProp;
 import annotations.StringProp;
 import annotations.StringSimulationProperty;
 
 import com.google.classpath.ClassPath;
 import com.google.classpath.ClassPathFactory;
 import com.google.classpath.RegExpResourceFilter;
 
 public class SimPropRegistry {
 
 	private Map<String, SimProp> _properties = new HashMap<String, SimProp>();
 
 	@SuppressWarnings("unchecked")
 	private Map<String, String>[] _pluginLayerMap = new HashMap[5];
 
 	private static SimPropRegistry _instance = null;
 
 	private SimPropRegistry() {
 		scanPlugins();
 	}
 
 	public static SimPropRegistry getInstance() {
 		if (_instance == null) {
 			_instance = new SimPropRegistry();
 		}
 		return _instance;
 	}
 
 	// Scans the simulation properties
 	public void scan() {
 
 		SimProp property;
 		
 		// TODO: Scans all packages :/
 		Reflections reflections = new Reflections(
 				ClasspathHelper.forPackage("gui.layout"),
 				new FieldAnnotationsScanner());
 		
 		// FIXME: Class<? extends Annotation> does not work
 		Class<?>[] annotations = {
 				annotations.BoolSimulationProperty.class,
 				annotations.FloatSimulationProperty.class,
 				annotations.IntSimulationProperty.class,
 				annotations.StringSimulationProperty.class
 				};
 		
 		for (Class<?> item : annotations) {
 
 			@SuppressWarnings("unchecked")
 			Set<Field> fields = reflections.getFieldsAnnotatedWith((Class<? extends Annotation>) item);
 
 			Iterator<Field> field = fields.iterator();
 			
 			while (field.hasNext()) {
 				
 				Field f = field.next();
 				
 				if (item == BoolSimulationProperty.class) { // Boolean
 					BoolSimulationProperty annotation = f.getAnnotation(BoolSimulationProperty.class);
 					property = new BoolProp();
 					if (annotation != null) {
 						property.setId(f.getName());
 						property.setName(annotation.name());
 						property.setDescription(annotation.description());
 						property.setTooltip(annotation.tooltip());
 						property.setCategory(annotation.category());
 						property.setEnable_requirements(annotation.enable_requirements());
 						
 						((BoolProp)property).setValue(annotation.value());
 						
 						property.setEnable(true);
 					}
 					register(property);
 				}else if(item == FloatSimulationProperty.class){ // Float
 					FloatSimulationProperty annotation = f.getAnnotation(FloatSimulationProperty.class);
 					property = new FloatProp();
 					if (annotation != null) {
 						property.setId(f.getName());
 						property.setName(annotation.name());
 						property.setDescription(annotation.description());
 						property.setTooltip(annotation.tooltip());
 						property.setCategory(annotation.category());
 						property.setEnable_requirements(annotation.enable_requirements());
 						
 						((FloatProp)property).setMinValue(annotation.min());
 						((FloatProp)property).setMaxValue(annotation.max());
 						((FloatProp)property).setValue(annotation.value());
 						
 						property.setEnable(true);
 					}
 					register(property);
 				}else if(item == IntSimulationProperty.class){ // Integer
 					IntSimulationProperty annotation = f.getAnnotation(IntSimulationProperty.class);
 					property = new IntProp();
 					if (annotation != null) {
 						property.setId(f.getName());
 						property.setName(annotation.name());
 						property.setDescription(annotation.description());
 						property.setTooltip(annotation.tooltip());
 						property.setCategory(annotation.category());
 						property.setEnable_requirements(annotation.enable_requirements());
 						property.setValue_requirements(annotation.value_requirements());
 						
 						((IntProp)property).setMinValue(annotation.min());
 						((IntProp)property).setMaxValue(annotation.max());
 						property.setValue(annotation.value());
 						
 						property.setEnable(true);
 					}
 					register(property);
 				}else if(item == StringSimulationProperty.class){ // String
 					StringSimulationProperty annotation = f.getAnnotation(StringSimulationProperty.class);
 					property = new StringProp();
 					if (annotation != null) {
 						property.setId(f.getName());
 						property.setName(annotation.name());
 						property.setDescription(annotation.description());
 						property.setTooltip(annotation.tooltip());
 						property.setCategory(annotation.category());
 						property.setEnable_requirements(annotation.enable_requirements());
 						
 						((StringProp)property).setValue(annotation.value());
 						
 						property.setEnable(true);
 					}
 					register(property);
 				}else{
 					Logger.Log(LogLevel.ERROR, "GuiConfigRegistry - bad type");
 					continue;
 				}
 			}
 		}
 
 		// call initial dependency-check for per plugin configurations
 		DependencyChecker.checkAll(this);
 	}
 
 	public void register(SimProp s) {
 		System.out.println("Register: " + s.getId());
 		if ( _properties.containsKey(s.getId()) ){
 			
			@SuppressWarnings("unused")
			JOptionPane alert =  new JOptionPane();
			JOptionPane.showMessageDialog(null,
				    "Redefinition of "+s.getId()+" "+s.getPlugin()+" dtetected!",
				    "Warning",
				    JOptionPane.WARNING_MESSAGE);
 		} else {
 			_properties.put(s.getId(), s);
 		}
 	}
 
 	public Set<Entry<String, SimProp>> getAllSimProps() {
 		return _properties.entrySet();
 	}
 
 	public SimProp getValue(String key) {
 
 		return _properties.get(key);
 	}
 
 	public void setValue(String key, Object arg0) {
 
 		if (arg0.getClass() == Boolean.class) {
 			System.out.println("Integer");
 			_properties.get(key).setValue((Boolean) arg0);
 			
 		}else if (arg0.getClass() == Float.class) {
 			System.out.println("Integer");
 			_properties.get(key).setValue((Float) arg0);
 			
 		}else if (arg0.getClass() == Integer.class) {
 			System.out.println("Integer");
 			_properties.get(key).setValue((Integer) arg0);
 			
 		}else if (arg0.getClass() == String.class) {
 			System.out.println("Integer");
 			_properties.get(key).setValue((String) arg0);
 			
 		}else {
 			
 		}
 
 	}
 
 	public List<Entry<String, String>> getCategoryItems(String category) {
 
 		List<Entry<String, String>> hm = new LinkedList<Entry<String, String>>();
 
 		Iterator<Entry<String, SimProp>> iter = _properties.entrySet()
 				.iterator();
 		while (iter.hasNext()) {
 			SimProp entry = iter.next().getValue();
 			if (category.equals(entry.getCategory())) {
 				hm.add(new ListEntry<String, String>(entry.getId(), entry
 						.getName()));
 			}
 		}
 
 		return hm;
 	}
 
 	public void scanPlugins() {
 		ClassPath classpath = new ClassPathFactory().createFromJVM();
 		RegExpResourceFilter regExpResourceFilter = new RegExpResourceFilter(
 				".*", ".*\\.class");
 		String[] resources = classpath.findResources("", regExpResourceFilter);
 
 		String[] plugInLayer = { "layer1network", "layer2recordingScheme",
 				"layer3outputStrategy", "layer4transport", "layer5application" };
 
 		for (int i = 0; i < 5; i++) {
 
 			Pattern p = Pattern.compile("^plugIns/" + plugInLayer[i] + "/.+/.+");
 			_pluginLayerMap[i] = new HashMap<String, String>();
 
 			for (final String r : resources) {
 
 				if (p.matcher(r).matches()) {
 					String[] splitString = r.split("/");
 
 					if (splitString.length != 4) {
 						Logger.Log(log.LogLevel.ERROR, "Length != 4");
 						System.exit(1);
 					}
 
 					String plugInName = splitString[2];
 					String plugInPath = splitString[0] + "/" + splitString[1]+ "/" + splitString[2] + "/";
 					_pluginLayerMap[i].put(plugInName, plugInPath);
 
 //					Iterator<Entry<String, String>> iter = _pluginLayerMap[i].entrySet().iterator();
 //
 //					while (iter.hasNext()) {
 //						Entry<String, String> entry = iter.next();
 //						Logger.Log(
 //								log.LogLevel.DEBUG,
 //								"Name: " + entry.getKey() + "; Path: "
 //										+ entry.getValue());
 //					}
 
 					continue;
 				}
 			}
 		}
 	}
 
 	public Map<String, String>[] getPlugIns() {
 		return _pluginLayerMap;
 	}
 
 	public void dumpConfiguration() {
 
 	}
 }
