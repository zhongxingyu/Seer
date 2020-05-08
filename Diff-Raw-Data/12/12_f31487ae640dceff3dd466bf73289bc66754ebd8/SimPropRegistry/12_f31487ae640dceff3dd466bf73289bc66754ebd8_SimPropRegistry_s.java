 package evaluation.simulator.gui.pluginRegistry;
 
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Observer;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.reflections.Reflections;
 import org.reflections.scanners.FieldAnnotationsScanner;
 import org.reflections.scanners.TypeAnnotationsScanner;
 import org.reflections.util.ClasspathHelper;
 
 import evaluation.simulator.annotations.plugin.Plugin;
 import evaluation.simulator.annotations.plugin.PluginSuperclass;
 import evaluation.simulator.annotations.plugin.SimGuiPlugin;
 import evaluation.simulator.annotations.simulationProperty.BoolProp;
 import evaluation.simulator.annotations.simulationProperty.BoolSimulationProperty;
 import evaluation.simulator.annotations.simulationProperty.DoubleProp;
 import evaluation.simulator.annotations.simulationProperty.DoubleSimulationProperty;
 import evaluation.simulator.annotations.simulationProperty.FloatProp;
 import evaluation.simulator.annotations.simulationProperty.FloatSimulationProperty;
 import evaluation.simulator.annotations.simulationProperty.IntProp;
 import evaluation.simulator.annotations.simulationProperty.IntSimulationProperty;
 import evaluation.simulator.annotations.simulationProperty.SimProp;
 import evaluation.simulator.annotations.simulationProperty.StringProp;
 import evaluation.simulator.annotations.simulationProperty.StringSimulationProperty;
 
 public class SimPropRegistry {
 
 	private static Logger logger = Logger.getLogger(SimPropRegistry.class);
 
 	private static SimPropRegistry _instance = null;
 
 	public static SimPropRegistry getInstance() {
 		if (_instance == null) {
 			_instance = new SimPropRegistry();
 		}
 		return _instance;
 	}
 
 	//	private final List<String> pluginLayer;
 
 	@SuppressWarnings("rawtypes")
 	private final List<Vector> deferList = new LinkedList<>();
 
 	private int numberOfPluginLayers;
 
 	/**
 	 * 
 	 */
 	@SuppressWarnings("unchecked")
 	private final Map<String, String>[] pluginLayerMap = new HashMap[100];
 	/**
 	 * Maps all registered properties.
 	 * key:	config name of the property
 	 * value: property
 	 */
 	private final Map<String, SimProp> properties = new HashMap<String, SimProp>();
 	/**
 	 * Maps the active plugins. Active plungins are those, which are enabled in the GUI.
 	 * key: name of plugin level
 	 * value: name of the plugin
 	 */
 	private final Map<String, String> activePlugins = new HashMap<String, String>();
 	/**
 	 * Maps the active plugins. Active plungins are those, which are enabled in the GUI.
 	 * key: name of the plugin
 	 * value: name of plugin level
 	 */
 	private final Map<String, String> activePluginsMapped = new HashMap<String, String>();
 	/**
 	 *  Maps between the display name of a plugin layer and the config name.
 	 *  key: display name of the plugin layer
 	 *  value: config name of the plugin layer
 	 */
 	private final Map<String, String> layerMapDisplayNameToConfigName = new LinkedHashMap<String, String>();
 	/**
 	 *  Maps between the display name of a plugin layer and the config name.
 	 *  key: config name of the plugin layer
 	 *  value: display name of the plugin layer
 	 */
 	private final Map<String, String> layerMapConfigNameToDisplayName = new LinkedHashMap<String, String>();
 	/**
 	 * Maps the registered plugins to the corresponding plugin layer.
 	 * key: config name of the plugin
 	 * value: display name of the plugin layer
 	 */
 	private final Map<String, Integer> staticConfigurationDisplay = new HashMap<String, Integer>();
 
 	private final Map<String, String> registeredPlugins = new HashMap<String, String>();
 
 	private final Map<String, Integer>  layerMapDisplayNameToOrder = new HashMap<String, Integer>();
 
 	private final Map<String, SimGuiPlugin>  plugins = new HashMap<String, SimGuiPlugin>();
 
 	// TODO: Merge both order functions
 	public Map<String, Integer> getLayerMapDisplayNameToOrder() {
 		return this.layerMapDisplayNameToOrder;
 	}
 
 	public Map<String, Integer> getLayerMapConfigNameToOrder() {
 		return this.layerMapConfigNameToOrder;
 	}
 
 	public Map<String, Boolean> getIsStaticLayerMap() {
 		return this.isStaticLayerMap;
 	}
 
 	private final Map<String, Integer>  layerMapConfigNameToOrder = new HashMap<String, Integer>();
 
 	private final Map<String, Boolean> isStaticLayerMap = new HashMap<String, Boolean>();
 
 	private SimPropRegistry() {
 		this.numberOfPluginLayers = 0;
 
 		this.scanForPluginProperties();
 
 		// TODO: Obsolete or even deprecated?
 		// this.scanPlugins();
 
 		logger.log(Level.DEBUG, "Now it's time to process defered plugins");
 		this.processDefered();
 		this.scanForStaticProperties();
 		this.toString();
 	}
 
 	// TODO: remove this peace of code
 	public Set<Entry<String, SimProp>> getAllSimProps() {
 		return this.getProperties().entrySet();
 	}
 	
 	public Map<String, String>[] getPluginLayerMap() {
 		return this.pluginLayerMap;
 	}
 
 	// TODO: remove this peace of code
 	public SimProp getValue(String key) {
 
 		return this.getProperties().get(key);
 	}
 
 	
 	public void register(SimProp s, boolean isSuperClass, boolean isGlobal, String pluginLayer) {
 		if (this.getProperties().containsKey(s.getPropertyID()) &&
 				!this.getProperties().get( s.getPropertyID() ).getPluginLayerID().equals(this.pluginNameToConfigName(pluginLayer))) {
 
 			GraphicsDevice graphicsDevice = GraphicsEnvironment
 					.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 			int x = graphicsDevice.getDisplayMode().getWidth();
 			int y = graphicsDevice.getDisplayMode().getHeight();
 
 			//
 			if ( !this.getProperties().get( s.getPropertyID() ).equals(pluginLayer) ) {
 				JOptionPane alert = new JOptionPane("Redefinition of property '" + s.getPropertyID()
 						+ "' at superclass level detected \n (" +
 						this.getProperties().get( s.getPropertyID() ).getPluginLayerID() + "" +
 						", " + this.pluginNameToConfigName(pluginLayer) + ") \n" +
 						"Please fix the problem!");
 
 				JDialog dialog = alert.createDialog(null, "");
 				int w = dialog.getWidth();
 				int h = dialog.getHeight();
 				dialog.setLocation((x / 2) - (w / 2), (y / 2) - (h / 2));
 				dialog.setVisible(true);
 				System.exit(-1);
 			}else{
 				JOptionPane alert = new JOptionPane("Redefinition of " + s.getPropertyID()
 						+ " at pluginlevel (" + s.getPluginID() + ") detected!");
 
 				JDialog dialog = alert.createDialog(null, "");
 				int w = dialog.getWidth();
 				int h = dialog.getHeight();
 				dialog.setLocation((x / 2) - (w / 2), (y / 2) - (h / 2));
 				dialog.setVisible(true);
 			}
 
 		} else if ( !this.getProperties().containsKey(s.getPropertyID()) && isSuperClass) {
 			logger.log(Level.DEBUG,  "Associate superclass property " + s.getPropertyID() + " with " + s.getPluginLayerID());
 			s.setIsGlobal(true);
 			this.getProperties().put(s.getPropertyID(), s);
 		} else if ( !this.getProperties().containsKey(s.getPropertyID()) && isGlobal ) {
 			logger.log(Level.DEBUG,  s.getPropertyID() + " with " + s.getPluginLayerID() + " is forced to be global");
 			s.setIsGlobal(true);
 			this.getProperties().put(s.getPropertyID(), s);
 		} else {
 			logger.log(Level.DEBUG, "Register property (" + s.getPropertyID() + ", " + s.getPluginID() + ", " + s.getPluginLayerID() + ")");
 			s.setIsGlobal(false);
 			this.getProperties().put(s.getPropertyID(), s);
 		}
 	}
 
 	/**
 	 * Scans static simulation properties. Only handles PropertyAnnoations with inject parameter.
 	 */
 	private void scanForStaticProperties() {
 
 		Reflections reflections = new Reflections(
 				ClasspathHelper.forPackage("evaluation.simulator.plugins"),
 				new FieldAnnotationsScanner());
 
 		Set<Field> fields = reflections.getFieldsAnnotatedWith(evaluation.simulator.annotations.simulationProperty.IntSimulationProperty.class);
 		fields.addAll(reflections.getFieldsAnnotatedWith(evaluation.simulator.annotations.simulationProperty.FloatSimulationProperty.class));
 		fields.addAll(reflections.getFieldsAnnotatedWith(evaluation.simulator.annotations.simulationProperty.DoubleSimulationProperty.class));
 		fields.addAll(reflections.getFieldsAnnotatedWith(evaluation.simulator.annotations.simulationProperty.StringSimulationProperty.class));
 		fields.addAll(reflections.getFieldsAnnotatedWith(evaluation.simulator.annotations.simulationProperty.BoolSimulationProperty.class));
 
 		List<Class<? extends Annotation> > annotationTypes = new LinkedList<Class<? extends Annotation> >();
 		annotationTypes.add( IntSimulationProperty.class );
 		annotationTypes.add( BoolSimulationProperty.class );
 		annotationTypes.add( FloatSimulationProperty.class );
 		annotationTypes.add( DoubleSimulationProperty.class );
 		annotationTypes.add( StringSimulationProperty.class );
 
 		SimProp property;
 		boolean globalProperty = true;
 		for ( Field field : fields ){
 			Annotation[] a = field.getAnnotations();
 			for (Annotation element : a) {
 
 				if (element.annotationType() == BoolSimulationProperty.class) {
 
 					BoolSimulationProperty annotation = field.getAnnotation(BoolSimulationProperty.class);
 					if ( !annotation.inject().equals("") ){
 
 						InjectionParser injection = new InjectionParser( annotation.inject(), annotation.key() );
 
 						String layerDisplayName = injection.getLayerDisplayName();
 						String layerConfigName = injection.getLayerConfigName();
 						String pluginDisplayName = injection.getPluginDisplayName();
 						String pluginConfigName = injection.getPluginConfigName();
 						int layerPosition = injection.getLayerPosition();
 						int pluginPosition = injection.getPluginPosition();
 
 						property = new BoolProp();
 						property.setId(annotation.key());
 						property.setName(annotation.name());
 						property.setTooltip(annotation.key());
 						property.setPluginLayerID(layerConfigName);
 						property.setPluginID(pluginConfigName);
 						property.setEnable_requirements(annotation.enable_requirements());
 						property.setValue_requirements(annotation.value_requirements());
 						property.setValue(annotation.value());
 						property.setEnable(true);
 						property.setInfo(annotation.info());
 
 						globalProperty = annotation.global() || injection.isGlobalProperty();
 						property.setIsGlobal(globalProperty);
 						property.setIsStatic(annotation.isStatic());
 
 						if(property.isStatic()){
 							property.setPluginID("");
 						}
 
 						((BoolProp) property).setValue(annotation.value());
 
 						this.getProperties().put(property.getPropertyID(), property);
 
 						if ( !this.getLayerMapDisplayNameToConfigName().containsKey(layerDisplayName)){
 							logger.log( Level.DEBUG , "Register plugin layer (" + layerConfigName + ", " + layerDisplayName + ")");
 							logger.log( Level.DEBUG, "Set position for injected plugin layer " + layerConfigName + " to " + layerPosition);
 							this.getLayerMapDisplayNameToConfigName().put(layerDisplayName, layerConfigName);
 							this.layerMapConfigNameToDisplayName.put(layerConfigName, layerDisplayName);
 							this.layerMapDisplayNameToOrder.put( layerDisplayName, layerPosition );
 							this.layerMapConfigNameToOrder.put( layerConfigName, layerPosition );
 							this.isStaticLayerMap.put(layerDisplayName,property.isStatic());
 						} else if ( !globalProperty  && !property.isStatic()){
 							this.registerPlugin(pluginDisplayName, layerDisplayName, true);
 						}
 					}
 				}
 
 				if (element.annotationType() == IntSimulationProperty.class) {
 
 					IntSimulationProperty annotation = field.getAnnotation(IntSimulationProperty.class);
 					if ( !annotation.inject().equals("") ){
 
 						InjectionParser injection = new InjectionParser( annotation.inject(), annotation.key() );
 
 						String layerDisplayName = injection.getLayerDisplayName();
 						String layerConfigName = injection.getLayerConfigName();
 						String pluginDisplayName = injection.getPluginDisplayName();
 						String pluginConfigName = injection.getPluginConfigName();
 						int layerPosition = injection.getLayerPosition();
 						int pluginPosition = injection.getPluginPosition();
 
 						property = new IntProp();
 						property.setId(annotation.key());
 						property.setName(annotation.name());
 						property.setTooltip(annotation.key());
 						property.setPluginLayerID(layerConfigName);
 						property.setPluginID(pluginConfigName);
 						property.setEnable_requirements(annotation.enable_requirements());
 						property.setValue_requirements(annotation.value_requirements());
 						property.setEnable(true);
 						property.setInfo(annotation.info());
 
 						globalProperty = annotation.global() || injection.isGlobalProperty();
 						property.setIsGlobal(globalProperty);
 						property.setIsStatic(annotation.isStatic());
 
 						if(property.isStatic()){
 							property.setPluginID("");
 						}
 
 						// IntProp specific annotation processing
 						((IntProp) property).setValue(annotation.value());
 						((IntProp) property).setMinValue(annotation.min());
 						((IntProp) property).setMaxValue(annotation.max());
 						((IntProp) property).setEnableAuto(annotation.enableAuto());
 						((IntProp) property).setEnableUnlimited(annotation.enableUnlimited());
 						((IntProp) property).setStepSize(annotation.stepSize());
 						((IntProp) property).setGuiElement(annotation.guiElement());
 
 						this.getProperties().put(property.getPropertyID(), property);
 
 						if ( !this.getLayerMapDisplayNameToConfigName().containsKey(layerDisplayName)){
 							logger.log( Level.DEBUG , "Register plugin layer (" + layerConfigName + ", " + layerDisplayName + ")");
 							logger.log( Level.DEBUG, "Set position for injected plugin layer " + layerConfigName + " to " + layerPosition);
 							this.getLayerMapDisplayNameToConfigName().put(layerDisplayName, layerConfigName);
 							this.layerMapConfigNameToDisplayName.put(layerConfigName, layerDisplayName);
 							this.layerMapDisplayNameToOrder.put( layerDisplayName, layerPosition );
 							this.layerMapConfigNameToOrder.put( layerConfigName, layerPosition );
 							this.isStaticLayerMap.put(layerDisplayName,property.isStatic());
 
 						} else if ( !globalProperty  && !property.isStatic()){
 							this.registerPlugin(pluginDisplayName, layerDisplayName, true);
 						}
 					}
 				}
 
 				if (element.annotationType() == FloatSimulationProperty.class) {
 
 					FloatSimulationProperty annotation = field.getAnnotation(FloatSimulationProperty.class);
 					if ( !annotation.inject().equals("") ){
 
 						InjectionParser injection = new InjectionParser( annotation.inject(), annotation.key() );
 
 						String layerDisplayName = injection.getLayerDisplayName();
 						String layerConfigName = injection.getLayerConfigName();
 						String pluginDisplayName = injection.getPluginDisplayName();
 						String pluginConfigName = injection.getPluginConfigName();
 						int layerPosition = injection.getLayerPosition();
 						int pluginPosition = injection.getPluginPosition();
 
 						property = new FloatProp();
 						property.setId(annotation.key());
 						property.setName(annotation.name());
 						property.setTooltip(annotation.key());
 						property.setPluginLayerID(layerConfigName);
 						property.setPluginID(pluginConfigName);
 						property.setEnable_requirements(annotation.enable_requirements());
 						property.setValue_requirements(annotation.value_requirements());
 						property.setEnable(true);
 						property.setInfo(annotation.info());
 
 						globalProperty = annotation.global() || injection.isGlobalProperty();
 						property.setIsGlobal(globalProperty);
 						property.setIsStatic(annotation.isStatic());
 
 						if(property.isStatic()){
 							property.setPluginID("");
 						}
 
 						((FloatProp) property).setMinValue(annotation.min());
 						((FloatProp) property).setMaxValue(annotation.max());
 						((FloatProp) property).setValue(annotation.value());
 						((FloatProp) property).setEnableAuto(annotation.enableAuto());
 						((FloatProp) property).setEnableUnlimited(annotation.enableUnlimited());
 						((FloatProp) property).setStepSize(annotation.stepSize());
 						((FloatProp) property).setGuiElement(annotation.guiElement());
 						
 						this.getProperties().put(property.getPropertyID(), property);
 
 						if ( !this.getLayerMapDisplayNameToConfigName().containsKey(layerDisplayName)){
 							logger.log( Level.DEBUG , "Register plugin layer (" + layerConfigName + ", " + layerDisplayName + ")");
 							logger.log( Level.DEBUG, "Set position for injected plugin layer " + layerConfigName + " to " + layerPosition);
 							this.getLayerMapDisplayNameToConfigName().put(layerDisplayName, layerConfigName);
 							this.layerMapConfigNameToDisplayName.put(layerConfigName, layerDisplayName);
 							this.layerMapDisplayNameToOrder.put( layerDisplayName, layerPosition );
 							this.layerMapConfigNameToOrder.put( layerConfigName, layerPosition );
 							this.isStaticLayerMap.put(layerDisplayName,property.isStatic());
 
 						} else if ( !globalProperty  && !property.isStatic()){
 							this.registerPlugin(pluginDisplayName, layerDisplayName, true);
 						}
 					}
 				}
 
 				if (element.annotationType() == DoubleSimulationProperty.class) {
 
 					DoubleSimulationProperty annotation = field.getAnnotation(DoubleSimulationProperty.class);
 					if ( !annotation.inject().equals("") ){
 
 						InjectionParser injection = new InjectionParser( annotation.inject(), annotation.key() );
 
 						String layerDisplayName = injection.getLayerDisplayName();
 						String layerConfigName = injection.getLayerConfigName();
 						String pluginDisplayName = injection.getPluginDisplayName();
 						String pluginConfigName = injection.getPluginConfigName();
 						int layerPosition = injection.getLayerPosition();
 						int pluginPosition = injection.getPluginPosition();
 
 						property = new DoubleProp();
 						property.setId(annotation.key());
 						property.setName(annotation.name());
 						property.setTooltip(annotation.key());
 						property.setPluginLayerID(layerConfigName);
 						property.setPluginID(pluginConfigName);
 						property.setEnable_requirements(annotation.enable_requirements());
 						property.setValue_requirements(annotation.value_requirements());
 						property.setEnable(true);
 						property.setInfo(annotation.info());
 
 						globalProperty = annotation.global() || injection.isGlobalProperty();
 						property.setIsGlobal(globalProperty);
 						property.setIsStatic(annotation.isStatic());
 
 						if(property.isStatic()){
 							property.setPluginID("");
 						}
 
 						((DoubleProp) property).setMinValue(annotation.min());
 						((DoubleProp) property).setMaxValue(annotation.max());
 						((DoubleProp) property).setValue(annotation.value());
 						((DoubleProp) property).setEnableAuto(annotation.enableAuto());
 						((DoubleProp) property).setEnableUnlimited(annotation.enableUnlimited());
 						((DoubleProp) property).setStepSize(annotation.stepSize());
 						((DoubleProp) property).setGuiElement(annotation.guiElement());
 
 						this.getProperties().put(property.getPropertyID(), property);
 						if ( !this.getLayerMapDisplayNameToConfigName().containsKey(layerDisplayName)){
 							logger.log( Level.DEBUG , "Register plugin layer (" + layerConfigName + ", " + layerDisplayName + ")");
 							logger.log( Level.DEBUG, "Set position for injected plugin layer " + layerConfigName + " to " + layerPosition);
 							this.getLayerMapDisplayNameToConfigName().put(layerDisplayName, layerConfigName);
 							this.layerMapConfigNameToDisplayName.put(layerConfigName, layerDisplayName);
 							this.layerMapDisplayNameToOrder.put( layerDisplayName, layerPosition );
 							this.layerMapConfigNameToOrder.put( layerConfigName, layerPosition );
 							this.isStaticLayerMap.put(layerConfigName,property.isStatic());
 
 						}
 						else if ( !globalProperty  && !property.isStatic()){
 							this.registerPlugin(pluginDisplayName, layerDisplayName, true);
 						}
 					}
 				}
 
 				if (element.annotationType() == StringSimulationProperty.class) {
 
 					StringSimulationProperty annotation = field.getAnnotation(StringSimulationProperty.class);
 					if ( !annotation.inject().equals("") ){
 
 						InjectionParser injection = new InjectionParser( annotation.inject(), annotation.key() );
 
 						String layerDisplayName = injection.getLayerDisplayName();
 						String layerConfigName = injection.getLayerConfigName();
 						String pluginDisplayName = injection.getPluginDisplayName();
 						String pluginConfigName = injection.getPluginConfigName();
 						int layerPosition = injection.getLayerPosition();
 						int pluginPosition = injection.getPluginPosition();
 
 						property = new StringProp();
 						property.setId(annotation.key());
 						property.setName(annotation.name());
 						property.setTooltip(annotation.key());
 						property.setPluginLayerID(layerConfigName);
 						property.setPluginID(pluginConfigName);
 						property.setEnable_requirements(annotation.enable_requirements());
 						property.setValue_requirements(annotation.value_requirements());
 						property.setEnable(true);
 						property.setInfo(annotation.info());
 
 						globalProperty = annotation.global() || injection.isGlobalProperty();
 						property.setIsGlobal(globalProperty);
 						property.setIsStatic(annotation.isStatic());
 
 						if(property.isStatic()){
 							property.setPluginID("");
 						}
 
 						((StringProp) property).setValue(annotation.value());
 						((StringProp) property).setPossibleValues(annotation.possibleValues());
 
 						this.getProperties().put(property.getPropertyID(), property);
 
 						if ( !this.getLayerMapDisplayNameToConfigName().containsKey(layerDisplayName)){
 							logger.log( Level.DEBUG , "Register plugin layer (" + layerConfigName + ", " + layerDisplayName + ")");
 							logger.log( Level.DEBUG, "Set position for injected plugin layer " + layerConfigName + " to " + layerPosition);
 							this.getLayerMapDisplayNameToConfigName().put(layerDisplayName, layerConfigName);
 							this.layerMapConfigNameToDisplayName.put(layerConfigName, layerDisplayName);
 							this.layerMapDisplayNameToOrder.put( layerDisplayName, layerPosition );
 							this.layerMapConfigNameToOrder.put( layerConfigName, layerPosition );
 							this.isStaticLayerMap.put(layerConfigName,property.isStatic());
 
 						} else if ( !globalProperty  && !property.isStatic()){
 							this.registerPlugin(pluginDisplayName, layerDisplayName, true);
 						}
 
 
 					}
 				}
 
 			}
 		}
 
 		//		Field[] fieldArray = fields.toArray( new Field[fields.size()]);
 		//		readFields(null, fieldArray, null, false);
 
 	}
 
 	// Scans plugin dependent simulation properties
 	@SuppressWarnings("unused")
 	public void scanForPluginProperties() {
 
 		SimGuiPlugin plugin;
 
 		// TODO: Seems not to work properly, scans all packages
 		Reflections reflectionsPlugins = new Reflections(
 				ClasspathHelper.forPackage("evaluation.simulator.plugins"),
 				new TypeAnnotationsScanner());
 
 		// Look for classes with PluginAnnotation
 		Set<Class<?>> types = reflectionsPlugins.getTypesAnnotatedWith(evaluation.simulator.annotations.plugin.Plugin.class);
 
 		for (Class<?> pluginClass : types) {
 
 			Plugin pluginAnnotation = pluginClass.getAnnotation(Plugin.class);
 
 			plugin = new SimGuiPlugin();
 			plugin.setId(pluginClass.getName());
 			plugin.setName(pluginAnnotation.pluginKey());
 			plugin.setDocumentationURL(pluginAnnotation.documentationURL());
 			plugin.setPluginLayer(pluginAnnotation.pluginLayerKey());
 			plugin.isVisible(pluginAnnotation.visible());
 			plugin.isGlobal(pluginAnnotation.global());
 			plugin.allowGlobalFields(pluginAnnotation.allowGlobalFields());
 			plugin.setDisplayName(pluginAnnotation.pluginName());
 
 			// This is the direct superclass. The direct superclass does not need
 			// to be annotated with @PluginSuperclass. It also can be annotated with
 			// @Plugin but then it must have the same name
 			Class<?> directSuperlass = pluginClass.getSuperclass();
 
 			// Find the annotated superclass
 			Class<?> pluginSuperclass = plugin.getPluginSuperclass( pluginClass );
 
 			// If there is a plugin layer provided by the plugin annotation, a plugin can
 			// be registered drectly ( e.g. StopAndGoMessage.java ).
 			// Otherwise the we try to find the pluginSuperclasse an use its name ( e.g. StopAndGo.java ).
 			boolean autodetectPluginLayer = pluginAnnotation.pluginLayerKey().equals("");
 
 			if ( !autodetectPluginLayer ){
 				logger.log(Level.DEBUG, "Disable autodetect for plugin " + plugin.getName() );
 
 				// we need this for invisible plugins
 				plugin.setFallbackLayer(pluginAnnotation.pluginLayerKey());
 
 				// Although autodetection is disabled, it is a good idea to check if there is a superclass
 				// with @PluginSuperclass annotation.
 				// Reason: This superclass might have some properties!
 				if ( pluginSuperclass != null ) {
 
 					PluginSuperclass pluginSuperclassAnnotation = pluginSuperclass.getAnnotation( PluginSuperclass.class );
 
 					// prefer pluginLayer from @PluginSuperclass annotation
 					// let's overwrite it!
 					String pluginSuperclassLayerKey = pluginSuperclassAnnotation.layerKey();
 					String pluginSuperclassLayerName = pluginSuperclassAnnotation.layerName();
 					plugin.setPluginLayer( pluginSuperclassLayerKey );
 
 					// we need this for invisible plugins
 					plugin.setFallbackLayer(pluginSuperclassLayerKey);
 
 					// Process PluginSuperclass fields if not already done
 					if ( !this.getLayerMapDisplayNameToConfigName().containsKey( pluginSuperclassLayerName )){
 
 						// We can/must read PluginSuperclass fields without registering a plugin
 						this.readFields( plugin, pluginSuperclass.getDeclaredFields(), pluginSuperclassLayerKey, true );
 						this.getLayerMapDisplayNameToConfigName().put( pluginSuperclassLayerName, pluginSuperclassLayerKey);
 						this.layerMapConfigNameToDisplayName.put( pluginSuperclassLayerKey, pluginSuperclassLayerName );
 					}
 				}
 
 				// We have to defer the plugin registration, since now it is not clear
 				// if there is another class of the same plugin that has a PluginSuperclass,
 				// which has higher priority when setting the display name of the layer.
 				this.deferedReadFields( plugin, pluginClass.getDeclaredFields(), pluginAnnotation.pluginLayerKey(), false );
 
 				// Now it is safe to register the plugin and read the fields
 				// registerPlugin( plugin.getName(), plugin.getPluginLayer(), plugin.isVisible() );
 				// readFields( plugin, pluginClass.getDeclaredFields(), pluginAnnotation.pluginLayerKey(), false );
 
 			}else{ // AUTODETECT PLUGINLAYER (Lookup superclass)
 				logger.log(Level.DEBUG, "Enable autodetect for plugin " + plugin.getName() + ", PluginSuperclass=" + pluginSuperclass.getSimpleName());
 
 				String layerDisplayName = "";
 				String layerConfigName = "";
 
 				// Process PluginSuperclass information
 				if ( pluginSuperclass != null ){
 
 					PluginSuperclass pluginSuperclassAnnotation = pluginSuperclass.getAnnotation( PluginSuperclass.class );
 
 					// Case: Invalid inheritance
 					// If the direct superclass has a normal plugin annotation, the
 					// provided name must match the actual plugin name.
 					if (directSuperlass.isAnnotationPresent( Plugin.class ) &&
 							(directSuperlass.getAnnotation( Plugin.class ).pluginKey() != pluginAnnotation.pluginKey()) ){
 
 						logger.log( Level.ERROR , "Check the annotation in class " + directSuperlass.getCanonicalName() +
 								" it should be @Plugin( name = \""+ plugin.getName() +"\" ... ) or a valid @PluginSuperclass annotation.");
 					}
 
 					// Set the pluginLayer
 					String pluginSuperclassLayerKey = pluginSuperclassAnnotation.layerKey();
 					String pluginSuperclassLayerName = pluginSuperclassAnnotation.layerName();
 					plugin.setPluginLayer( pluginSuperclassLayerKey );
 
 					// we need this for invisible plugins
 					plugin.setFallbackLayer(pluginSuperclassLayerKey);
 
 					// Process PluginSuperclass fields if not already done
 					if ( !this.getLayerMapDisplayNameToConfigName().containsKey( pluginSuperclassLayerName )){
 						logger.log(Level.DEBUG, "Register PluginSuperclass for " + pluginSuperclassLayerKey);
 
 						// register the plugin layer manually
 						logger.log(Level.DEBUG, "Associate plugin layer (" +
 								pluginSuperclassLayerKey + ", " + pluginSuperclassLayerName + ")");
 
 						this.readFields( plugin, pluginSuperclass.getDeclaredFields(), pluginSuperclassLayerKey, true );
 						this.getLayerMapDisplayNameToConfigName().put( pluginSuperclassLayerName, pluginSuperclassLayerKey);
 						this.layerMapConfigNameToDisplayName.put( pluginSuperclassLayerKey, pluginSuperclassLayerName );
 
 						int layerPosition = pluginSuperclass.getAnnotation( PluginSuperclass.class ).position();
 
 						logger.log( Level.DEBUG, "Set position for plugin layer " + pluginSuperclassLayerKey + " to " + layerPosition);
 						this.layerMapDisplayNameToOrder.put( pluginSuperclassLayerName, layerPosition );
 						this.layerMapConfigNameToOrder.put( pluginSuperclassLayerKey, layerPosition );
 
 						// PluginSuperclasses can provide fake plugins. Fake plugins will occur in the
 						// jcombobox of the corresponding plugin level (see TopologyScript.java)
 						String fakePlugins = pluginSuperclass.getAnnotation( PluginSuperclass.class ).fakePlugins();
 						if ( !fakePlugins.equals("") ){
 							String[] fakedPlugins = fakePlugins.split(",");
 							for (String fakedPlugin : fakedPlugins) {
 								logger.log(Level.DEBUG, "Inject fake plugin " + fakedPlugin + " to " + layerConfigName );
 								this.registerPlugin(fakedPlugin, pluginSuperclass.getAnnotation( PluginSuperclass.class ).layerKey(), true);
 							}
 						}
 
 					}else{
 						logger.log(Level.DEBUG, plugin.getName() + " is caped by a superclass " + pluginSuperclass.getAnnotation( PluginSuperclass.class ).layerKey());
 						this.registerPlugin( plugin.getName(), pluginSuperclass.getAnnotation( PluginSuperclass.class ).layerKey(), plugin.isVisible() );
 					}
 
 
 				}
 				else{
 					// The plugin is not O.K.! Each plugin must be caped by a pluginSuperclass
 					logger.log(Level.ERROR, plugin.getName() + " is not caped by a superclass");
 					System.exit(-1);
 				}
 
 				PluginSuperclass pluginSuperclassAnnotation = pluginSuperclass.getAnnotation( PluginSuperclass.class );
 				String pluginSuperclassLayerName = pluginSuperclassAnnotation.layerName();
 
 				this.deferedReadFields( plugin, pluginClass.getDeclaredFields(), layerConfigName, false );
 			}
 			this.plugins.put(plugin.getName(), plugin);
 		}
 		// call initial dependency-check for per plugin configurations
 		DependencyChecker.checkAll(this);
 	}
 
 
 	private void deferedReadFields(SimGuiPlugin plugin, Field[] declaredFields, String pluginLayerKey, boolean isSuperclass) {
 
 		logger.log(Level.DEBUG, "Defer " + plugin.getName() );
 
 		Vector<Object> deferInformation = new Vector<Object>();
 		deferInformation.add( plugin );
 		deferInformation.add( declaredFields );
 		deferInformation.add( pluginLayerKey);
 		deferInformation.add( isSuperclass );
 
 		this.deferList.add( deferInformation );
 	}
 
 	private void processDefered(){
 
 		for ( Vector<Object> deferInformation : this.deferList ){
 
 			SimGuiPlugin plugin = (SimGuiPlugin) deferInformation.get( 0 );
 			Field[] declaredFields = (Field[]) deferInformation.get( 1 );
 			String pluginLayerKey = (String) deferInformation.get( 2 );
 			boolean isSuperclass = (boolean) deferInformation.get( 3 );
 
 			this.registerPlugin( plugin.getName(), plugin.getPluginLayer(), plugin.isVisible() );
 			this.readFields( plugin, declaredFields, pluginLayerKey, isSuperclass );
 		}
 
 	}
 
 	/**
 	 * 
 	 * Registers a plugin name with a plugin layer.
 	 * 
 	 * @param plugin A plugin name e.g.
 	 * @param plugInLayer A player name e.g.
 	 * @param isVisible A boolean which specifies whether the plugin name will be
 	 * registered with a plugin layer (ture) or not (false). If a plugin is not
 	 * registerd with a plugin layer, it will be invisible whithin the gui.
 	 */
 	private void registerPlugin(String plugin, String plugInLayer, boolean isVisible) {
 		if ( isVisible ){
 
 			GraphicsDevice graphicsDevice = GraphicsEnvironment
 					.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 			int x = graphicsDevice.getDisplayMode().getWidth();
 			int y = graphicsDevice.getDisplayMode().getHeight();
 
 			// Check if this plugin is already known for a different pluginlayer.
 			// In this case show an alert an exit. The Pluginprogrammer has to fix ist
 			if ( this.getRegisteredPlugins().containsKey(plugin) ){
 				if ( !this.getRegisteredPlugins().get(plugin).equals(plugInLayer) ){
 					JOptionPane alert = new JOptionPane("Reuse of Plugin name '" + plugin +
 							"' in plugin layers: \n" +
 							this.getRegisteredPlugins().get(plugin) + " \n" +
 							plugInLayer + " \n" +
 							"Please fix the problem!");
 
 					JDialog dialog = alert.createDialog(null, "");
 					int w = dialog.getWidth();
 					int h = dialog.getHeight();
 					dialog.setLocation((x / 2) - (w / 2), (y / 2) - (h / 2));
 					dialog.setVisible(true);
 					System.exit(-1);
 				}
 
 			}
 			// Otherwise, register the plugin the pluginlayer.
 			// Plugins which are registered to a pluginlayer will occur in the
 			// pluginlayer's corresponging jcombobox!
 			// Plugins which are not registered are not visible in the gui.
 			else {
 				logger.log(Level.DEBUG, "Register plugin (" + plugin + ", " + plugInLayer + ")");
 				this.getRegisteredPlugins().put(plugin, plugInLayer);
 			}
 		} else{
 			logger.log(Level.DEBUG, "Plugin (" + plugin + ", " + plugInLayer + ") is set to invisible");
 		}
 
 	}
 
 	public Map<String, String> getRegisteredPlugins(){
 		return this.registeredPlugins;
 	}
 
 	private void readFields(SimGuiPlugin plugin, Field[] fields, String plugInLayer, boolean isSuperClass ) {
 
 		// Skip invisible plugins
 		if ( !plugin.isVisible() && !(plugin.isGlobal() || plugin.allowGlobalFields()) ){
 			logger.log(Level.DEBUG,
 					plugin.getName() + " is ignored due to isVisible=" +
 							plugin.isVisible()	+ " and makeFieldsGlobal=" +
 							plugin.isGlobal() + " and allowGlobalFields=" + plugin.allowGlobalFields());
 			return;
 		}
 
 		SimProp property;
 
 		try {
 			for (Field field : fields ) {
 
 				Annotation[] a = field.getAnnotations();
 				for (Annotation element : a) {
 
 					boolean isGlobal = false;
 					if (element.annotationType() == BoolSimulationProperty.class) {
 						BoolSimulationProperty annotation = field.getAnnotation(BoolSimulationProperty.class);
 						property = new BoolProp();
 						if (annotation != null) {
 							if ( !annotation.inject().equals("") ){
 								logger.log(Level.DEBUG, "Skip " + annotation.key() + " from " + plugin.getName() + " has injection annotation");
 								continue;
 							}
 
 							// Uninjected properties are global under following conditions:
 							// They are PluginSuperclass properties OR the if plugin allows global Properties.
 							// If the plugin allows global properties each property which is annotated as global
 							// will become a global plugin layer property ( e.g. LineChartPlotterCf.java )
 							isGlobal = isSuperClass || ( plugin.isGlobal() || annotation.global() );
 
 							property.setId(annotation.key());
 							if ( !isGlobal ){
 								property.setPluginID(plugin.getName());
 								property.setIsGlobal( false );
 							}else{
 								property.setPluginID("");
 								property.setIsGlobal( true );
 							}
 							logger.log(Level.DEBUG, annotation.key() + " is global=" +
 									property.isGlobal() + " so plugin is " + property.getPluginID() );
 
 							property.setIsSuperclass( isSuperClass );
 							property.setPluginLayerID(plugInLayer);
 
 							property.setName(annotation.name());
 							property.setTooltip(annotation.key());
 							property.setInfo(annotation.info());
 
 							// This is why we have to defer all readFields() calls
 							// Explanation: We call getPluginLayer() but the internal list which is used by this
 							// function builds up dynamically. Therefore, it is possible to get an empty string or
 							// null, if we have bad timing!
 							// CAUTION: do not touch these expressions, we make use of lazy evaluation
 							String possiblePluginLayer = this.getPluginLayer(plugin.getName());
 							if ( (possiblePluginLayer != null) && !possiblePluginLayer.isEmpty() ){
 								property.setPluginLayerID(this.getPluginLayer(plugin.getName()));
 							}else{
 								if ( !plugin.getFallbackLayer().isEmpty() ){
 									logger.log(Level.DEBUG, "Fallback to " + plugin.getFallbackLayer());
 									property.setPluginLayerID(plugin.getFallbackLayer());
 								}else{
 									logger.log(Level.ERROR, "Can not determine the Layer for " + property.getName() + "! Reason unknown");
 									System.exit(-1);
 								}
 							}
 
 							logger.log(Level.DEBUG, annotation.key() + " set layer to " + this.getPluginLayer(plugin.getName()));
 
 							property.setEnable_requirements(annotation.enable_requirements());
 							property.setValue_requirements(annotation.value_requirements());
 							property.setEnable(true);
 
 							((BoolProp) property).setValue(annotation.value());
 						}
 						this.register(property, isSuperClass, isGlobal, plugInLayer);
 					} else if (element.annotationType() == IntSimulationProperty.class) {
 						IntSimulationProperty annotation = field
 								.getAnnotation(IntSimulationProperty.class);
 						property = new IntProp();
 						if (annotation != null) {
 							if ( !annotation.inject().equals("") ){
 								logger.log(Level.DEBUG, "Skip " + annotation.key() + " from " + plugin.getName() + " has injection annotation");
 								continue;
 							}
 
 							// Uninjected properties are global under following conditions:
 							// They are PluginSuperclass properties OR the if plugin allows global Properties.
 							// If the plugin allows global properties each property which is annotated as global
 							// will become a global plugin layer property ( e.g. LineChartPlotterCf.java )
 							isGlobal = isSuperClass || ( plugin.isGlobal() || annotation.global() );
 
 							property.setId(annotation.key());
 							if ( !isGlobal ){
 								property.setPluginID(plugin.getName());
 								property.setIsGlobal( false );
 							}else{
 								property.setPluginID("");
 								property.setIsGlobal( true );
 							}
 							logger.log(Level.DEBUG, annotation.key() + " is global=" +
 									property.isGlobal() + " so plugin is " + property.getPluginID() );
 
 							property.setIsSuperclass( isSuperClass );
 							property.setPluginLayerID(plugInLayer);
 
 							property.setName(annotation.name());
 							property.setTooltip(annotation.key());
 							property.setInfo(annotation.info());
 
 							// This is why we have to defer all readFields() calls
 							// Explanation: We call getPluginLayer() but the internal list which is used by this
 							// function builds up dynamically. Therefore, it is possible to get an empty string or
 							// null, if we have bad timing!
 							// CAUTION: do not touch these expressions, we make use of lazy evaluation
 							String possiblePluginLayer = this.getPluginLayer(plugin.getName());
 							if ( (possiblePluginLayer != null) && !possiblePluginLayer.isEmpty() ){
 								property.setPluginLayerID(this.getPluginLayer(plugin.getName()));
 							}else{
 								if ( !plugin.getFallbackLayer().isEmpty() ){
 									logger.log(Level.DEBUG, "Fallback to " + plugin.getFallbackLayer());
 									property.setPluginLayerID(plugin.getFallbackLayer());
 								}else{
 									logger.log(Level.ERROR, "Can not determine the Layer for " + property.getName() + "! Reason unknown");
 									System.exit(-1);
 								}
 							}
 
 							logger.log(Level.DEBUG, annotation.key() + " set layer to " + this.getPluginLayer(plugin.getName()));
 
 							property.setEnable_requirements(annotation.enable_requirements());
 							property.setValue_requirements(annotation.value_requirements());
 							property.setEnable(true);
 
 							((IntProp) property).setValue(annotation.value());
 							((IntProp) property).setMinValue(annotation.min());
 							((IntProp) property).setMaxValue(annotation.max());
 							((IntProp) property).setEnableAuto(annotation.enableAuto());
 							((IntProp) property).setEnableUnlimited(annotation.enableUnlimited());
 							((IntProp) property).setStepSize(annotation.stepSize());
 							((IntProp) property).setGuiElement(annotation.guiElement());
 						}
 						this.register(property, isSuperClass, isGlobal, plugInLayer);
 					} else if (element.annotationType() == FloatSimulationProperty.class) {
 						FloatSimulationProperty annotation = field
 								.getAnnotation(FloatSimulationProperty.class);
 						property = new FloatProp();
 						if (annotation != null) {
 							if ( !annotation.inject().equals("") ){
 								logger.log(Level.DEBUG, "Skip " + annotation.key() + " from " + plugin.getName() + " has injection annotation");
 								continue;
 							}
 
 							// Uninjected properties are global under following conditions:
 							// They are PluginSuperclass properties OR the if plugin allows global Properties.
 							// If the plugin allows global properties each property which is annotated as global
 							// will become a global plugin layer property ( e.g. LineChartPlotterCf.java )
 							isGlobal = isSuperClass || ( plugin.isGlobal() || annotation.global() );
 
 							property.setId(annotation.key());
 							if ( !isGlobal ){
 								property.setPluginID(plugin.getName());
 								property.setIsGlobal( false );
 							}else{
 								property.setPluginID("");
 								property.setIsGlobal( true );
 							}
 							logger.log(Level.DEBUG, annotation.key() + " is global=" +
 									property.isGlobal() + " so plugin is " + property.getPluginID() );
 
 							property.setIsSuperclass( isSuperClass );
 							property.setPluginLayerID(plugInLayer);
 
 							property.setName(annotation.name());
 							property.setTooltip(annotation.key());
 							property.setInfo(annotation.info());
 
 							// This is why we have to defer all readFields() calls
 							// Explanation: We call getPluginLayer() but the internal list which is used by this
 							// function builds up dynamically. Therefore, it is possible to get an empty string or
 							// null, if we have bad timing!
 							// CAUTION: do not touch these expressions, we make use of lazy evaluation
 							String possiblePluginLayer = this.getPluginLayer(plugin.getName());
 							if ( (possiblePluginLayer != null) && !possiblePluginLayer.isEmpty() ){
 								property.setPluginLayerID(this.getPluginLayer(plugin.getName()));
 							}else{
 								if ( !plugin.getFallbackLayer().isEmpty() ){
 									logger.log(Level.DEBUG, "Fallback to " + plugin.getFallbackLayer());
 									property.setPluginLayerID(plugin.getFallbackLayer());
 								}else{
 									logger.log(Level.ERROR, "Can not determine the Layer for " + property.getName() + "! Reason unknown");
 									System.exit(-1);
 								}
 							}
 
 							logger.log(Level.DEBUG, annotation.key() + " set layer to " + this.getPluginLayer(plugin.getName()));
 
 							property.setEnable_requirements(annotation.enable_requirements());
 							property.setValue_requirements(annotation.value_requirements());
 							property.setEnable(true);
 
 							((FloatProp) property).setMinValue(annotation.min());
 							((FloatProp) property).setMaxValue(annotation.max());
 							((FloatProp) property).setValue(annotation.value());
 							((FloatProp) property).setEnableAuto(annotation.enableAuto());
 							((FloatProp) property).setEnableUnlimited(annotation.enableUnlimited());
 							((FloatProp) property).setStepSize(annotation.stepSize());
 							((FloatProp) property).setGuiElement(annotation.guiElement());
 						}
 						this.register(property, isSuperClass, isGlobal, plugInLayer);
 					} else if (element.annotationType() == DoubleSimulationProperty.class) {
 						DoubleSimulationProperty annotation = field
 								.getAnnotation(DoubleSimulationProperty.class);
 						property = new DoubleProp();
 						if (annotation != null) {
 							if ( !annotation.inject().equals("") ){
 								logger.log(Level.DEBUG, "Skip " + annotation.key() + " from " + plugin.getName() + " has injection annotation");
 								continue;
 							}
 
 							// Uninjected properties are global under following conditions:
 							// They are PluginSuperclass properties OR the if plugin allows global Properties.
 							// If the plugin allows global properties each property which is annotated as global
 							// will become a global plugin layer property ( e.g. LineChartPlotterCf.java )
 							isGlobal = isSuperClass || ( plugin.isGlobal() || annotation.global() );
 
 							property.setId(annotation.key());
 							if ( !isGlobal ){
 								property.setPluginID(plugin.getName());
 								property.setIsGlobal( false );
 							}else{
 								property.setPluginID("");
 								property.setIsGlobal( true );
 							}
 							logger.log(Level.DEBUG, annotation.key() + " is global=" +
 									property.isGlobal() + " so plugin is " + property.getPluginID() );
 
 							property.setIsSuperclass( isSuperClass );
 							property.setPluginLayerID(plugInLayer);
 
 							property.setName(annotation.name());
 							property.setTooltip(annotation.key());
 							property.setInfo(annotation.info());
 
 							// This is why we have to defer all readFields() calls
 							// Explanation: We call getPluginLayer() but the internal list which is used by this
 							// function builds up dynamically. Therefore, it is possible to get an empty string or
 							// null, if we have bad timing!
 							// CAUTION: do not touch these expressions, we make use of lazy evaluation
 							String possiblePluginLayer = this.getPluginLayer(plugin.getName());
 							if ( (possiblePluginLayer != null) && !possiblePluginLayer.isEmpty() ){
 								property.setPluginLayerID(this.getPluginLayer(plugin.getName()));
 							}else{
 								if ( !plugin.getFallbackLayer().isEmpty() ){
 									logger.log(Level.DEBUG, "Fallback to " + plugin.getFallbackLayer());
 									property.setPluginLayerID(plugin.getFallbackLayer());
 								}else{
 									logger.log(Level.ERROR, "Can not determine the Layer for " + property.getName() + "! Reason unknown");
 									System.exit(-1);
 								}
 							}
 
 							logger.log(Level.DEBUG, annotation.key() + " set layer to " + this.getPluginLayer(plugin.getName()));
 
 							property.setEnable_requirements(annotation.enable_requirements());
 							property.setValue_requirements(annotation.value_requirements());
 							property.setEnable(true);
 
 							((DoubleProp) property).setMinValue(annotation.min());
 							((DoubleProp) property).setMaxValue(annotation.max());
 							((DoubleProp) property).setValue(annotation.value());
 							((DoubleProp) property).setEnableAuto(annotation.enableAuto());
 							((DoubleProp) property).setEnableUnlimited(annotation.enableUnlimited());
 							((DoubleProp) property).setStepSize(annotation.stepSize());
 							((DoubleProp) property).setGuiElement(annotation.guiElement());
 						}
 						this.register(property, isSuperClass, isGlobal, plugInLayer);
 					} else if (element.annotationType() == StringSimulationProperty.class) {
 						StringSimulationProperty annotation = field
 								.getAnnotation(StringSimulationProperty.class);
 						property = new StringProp();
 						if (annotation != null) {
 							if ( !annotation.inject().equals("") ){
 								logger.log(Level.DEBUG, "Skip " + annotation.key() + " from " + plugin.getName() + " has injection annotation");
 								continue;
 							}
 
 							// Uninjected properties are global under following conditions:
 							// 1. The property is a PluginSuperclass property.
 							// The corresponding plugin is global.
 							// The property itself is global ( e.g. LineChartPlotterCf.java )
 							isGlobal = isSuperClass || ( plugin.isGlobal() || annotation.global() );
 
 							property.setId(annotation.key());
 							if ( !isGlobal ){
 								property.setPluginID(plugin.getName());
 								property.setIsGlobal( false );
 							}else{
 								property.setPluginID("");
 								property.setIsGlobal( true );
 							}
 							logger.log(Level.DEBUG, annotation.key() + " is global=" +
 									property.isGlobal() + " so plugin is " + property.getPluginID() );
 
 							property.setIsSuperclass( isSuperClass );
 							property.setPluginLayerID(plugInLayer);
 
 							property.setName(annotation.name());
 							property.setTooltip(annotation.key());
 							property.setInfo(annotation.info());
 
 							// This is why we have to defer all readFields() calls
 							// Explanation: We call getPluginLayer() but the internal list which is used by this
 							// function builds up dynamically. Therefore, it is possible to get an empty string or
 							// null, if we have bad timing!
 							// CAUTION: do not touch these expressions, we make use of lazy evaluation
 							String possiblePluginLayer = this.getPluginLayer(plugin.getName());
 							if ( (possiblePluginLayer != null) && !possiblePluginLayer.isEmpty() ){
 								property.setPluginLayerID(this.getPluginLayer(plugin.getName()));
 							}else{
 								if ( !plugin.getFallbackLayer().isEmpty() ){
 									logger.log(Level.DEBUG, "Fallback to " + plugin.getFallbackLayer());
 									property.setPluginLayerID(plugin.getFallbackLayer());
 								}else{
 									logger.log(Level.ERROR, "Can not determine the Layer for " + property.getName() + "! Reason unknown");
 									System.exit(-1);
 								}
 							}
 
 							logger.log(Level.DEBUG, annotation.key() + " set layer to " + this.getPluginLayer(plugin.getName()));
 
 							property.setEnable_requirements(annotation.enable_requirements());
 							property.setValue_requirements(annotation.value_requirements());
 							property.setEnable(true);
 
 							((StringProp) property).setValue(annotation.value());
 							((StringProp) property).setPossibleValues(annotation.possibleValues());
 						}
 						this.register(property, isSuperClass, isGlobal, plugInLayer);
 					} else {
 						logger.log(Level.ERROR, this + "Bad property type for field" + field.getName() );
 						continue;
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private String getPluginLayer(String pluginName) {
 
 		if ( this.getRegisteredPlugins().containsKey(pluginName) ){
 			return this.getRegisteredPlugins().get(pluginName);
 		}
 		logger.log(Level.ERROR, "Plugin " + pluginName + " is unknown");
 		return null;
 	}
 
 	public void scanPlugins() {
 
 		// Get the display names of plugin layers
 		String[] pluginLayer = this.layerMapConfigNameToDisplayName.keySet().toArray(new String[0]);
 		for (int i = 0; i < this.layerMapConfigNameToDisplayName.size(); i++) {
 			this.pluginLayerMap[i] = new HashMap<String, String>();
 		}
 
 		for ( Entry<String, String> entry : this.getRegisteredPlugins().entrySet() ) {
 
 			String name = entry.getKey();
 			String layer = entry.getValue();
 
 			boolean insertFlag = false;
 			for (int i = 0; i < this.getLayerMapDisplayNameToConfigName().size(); i++){
 
 				// Append plugins config name and corresponding layers display name
 				// if plugin's layer matches
 				if ( layer.equals(pluginLayer[i]) ){
 					this.pluginLayerMap[i].put( name, layer );
 					insertFlag = true;
 					this.numberOfPluginLayers = Math.max(this.numberOfPluginLayers, i+1);
 				}
 			}
 
 			if ( !insertFlag ) {
 				logger.log(Level.ERROR, "No such plugin layer: " + layer );
 			}
 		}
 	}
 
 	public void setValue(String key, Object arg0) {
 
 		logger.log(Level.DEBUG, "key " + key + " arg " + arg0);
		DependencyChecker.checkAll(getInstance());
 
 		if (arg0.getClass() == Boolean.class) {
 			//System.out.println("Boolean");
 			this.getProperties().get(key).setValue(arg0);
 		} else if (arg0.getClass() == Float.class) {
 			//System.out.println("Float");
 			this.getProperties().get(key).setValue(arg0);
 		} else if (arg0.getClass() == Double.class) {
 			//System.out.println("Double");
 			this.getProperties().get(key).setValue(arg0);
 		}else if (arg0.getClass() == Integer.class) {
 			//System.out.println("Integer");
 			this.getProperties().get(key).setValue(arg0);
 		} else if (arg0.getClass() == String.class) {
 			//System.out.println("String");
 			this.getProperties().get(key).setValue(arg0);
 
 		} else {
 
 		}
 	}
 
 	public List<SimProp> getGlobalSimPropertiesByPluginLayer(String pluginLayer) {
 
 		// TODO: order simproperties here
 
 		List<SimProp> simPropsInPluginLayer = new LinkedList<SimProp>();
 
 		for ( String key : this.getProperties().keySet() ) {
 			SimProp simProp = this.getProperties().get(key);
 
 			if ( ( simProp.isSuperclass() || simProp.isGlobal() )
 					&& simProp.getPluginLayerID().equals(this.getLayerMapDisplayNameToConfigName().get(pluginLayer)
 							) ){
 
 				simPropsInPluginLayer.add( simProp );
 			}
 		}
 
 		return simPropsInPluginLayer;
 	}
 
 	public List<SimProp> getSimPropertiesByPluginOrPluginLayer(String pluginName, String pluginLayer) {
 
 		// TODO: order simproperties here
 
 		List<SimProp> simPropertiesInANamespace = new LinkedList<SimProp>();
 
 		for ( String key : this.getProperties().keySet() ) {
 			SimProp simProp = this.getProperties().get(key);
 
 			if ( simProp.getPluginID().equals(pluginName) ||
 					( simProp.isSuperclass() && simProp.getPluginLayerID().equals(
 							this.getLayerMapDisplayNameToConfigName().get(pluginLayer)
 							)) ){
 				simPropertiesInANamespace.add( simProp );
 			}
 		}
 
 		return simPropertiesInANamespace;
 	}
 
 	public void setActivePlugins(String pluginLevel, String selectedPlugin) {
 		logger.log(Level.DEBUG, "Set " + pluginLevel + " plugin to " + selectedPlugin);
 		this.activePlugins.put(pluginLevel, selectedPlugin);
 		this.activePluginsMapped.put( this.pluginNameToConfigName(pluginLevel), selectedPlugin);
 	}
 
 	public void setActivePluginsMapped(String plugLayer, String selectedPlugin) {
 		logger.log(Level.DEBUG, "Set mapped " + this.pluginNameToConfigName(plugLayer) + " plugin to " + selectedPlugin);
 		this.activePlugins.put(this.configNameToPluginName(plugLayer), selectedPlugin);
 		this.activePluginsMapped.put(plugLayer, selectedPlugin);
 	}
 
 	public Map<String, String> getActivePlugins(boolean mapped) {
 		if (mapped){
 			return this.activePluginsMapped;
 		}else{
 			return this.activePlugins;
 		}
 	}
 
 	// TODO: remove this peace of code
 	public List<String> getPluginLevels() {
 		return new LinkedList<String>(this.getLayerMapDisplayNameToConfigName().keySet());
 	}
 
 	// TODO: remove this peace of code
 	public String pluginNameToConfigName(String pluginLayer) {
 		return this.getLayerMapDisplayNameToConfigName().get(pluginLayer);
 	}
 
 	public String configNameToPluginName(String pluginName) {
 		return this.layerMapConfigNameToDisplayName.get(pluginName);
 	}
 
 	public List<String> getConfigNamesForPluginLayers() {
 
 		List<String> configNamesForPluginLayers = new LinkedList<String>();
 		for ( Entry<String, String> entry : this.layerMapConfigNameToDisplayName.entrySet() )
 		{
 			configNamesForPluginLayers.add( entry.getKey() );
 		}
 		return configNamesForPluginLayers;
 	}
 
 	@Override
 	public String toString(){
 
 		String tree = "";
 		for ( String layer : this.getLayerMapDisplayNameToConfigName().keySet() ){
 			System.err.println("[" + layer + "]" );
 			for ( String prop : this.getProperties().keySet() ){
 				if ( this.getProperties().get(prop).getPluginID().equals("") &&
 						( this.getProperties().get(prop).isSuperclass() || this.getProperties().get(prop).isGlobal() ) &&
 						this.getProperties().get(prop).getPluginLayerID().equals(this.getLayerMapDisplayNameToConfigName().get(layer)) ){
 
 					// injected to plugin configuration
 					System.err.println("(" + prop + ")" );
 				}
 			}
 			for ( String plugin : this.getRegisteredPlugins().keySet() ){
 				if ( this.getRegisteredPlugins().get(plugin).equals(this.getLayerMapDisplayNameToConfigName().get(layer))){
 
 					// plugins
 					System.err.println("----" + plugin);
 					for ( String prop : this.getProperties().keySet() ){
 						if ( this.getProperties().get(prop).getPluginID().equals(plugin) ){
 
 							// plugin dependent properties
 							System.err.println("------" + prop );
 						}
 					}
 				}
 			}
 		}
 
 		return tree;
 	}
 
 	public String[] getPluginsInLayer( String pluginLayer ) {
 		List<String> tmp = new LinkedList<>();
 
 		for ( String layer : this.getLayerMapDisplayNameToConfigName().keySet() ){
 			if ( layer.equals( pluginLayer )){
 				for ( String plugin : this.getRegisteredPlugins().keySet() ){
 					if ( this.getRegisteredPlugins().get(plugin).equals(this.getLayerMapDisplayNameToConfigName().get(layer))){
 						tmp.add(plugin);
 					}
 				}
 			}
 		}
 		Collections.sort(tmp);
 		String[] ret = new String[tmp.size()];
 		ret = tmp.toArray(ret);
 		return ret;
 	}
 
 	public Map<String, Integer> getStaticConfigurationDisplay() {
 		return this.staticConfigurationDisplay;
 	}
 
 	public Map<String, SimGuiPlugin> getPluginMap() {
 		return this.plugins;
 	}
 
 	public Map<String, String> getLayerMapDisplayNameToConfigName() {
 		return this.layerMapDisplayNameToConfigName;
 	}
 
 	public Map<String, SimProp> getProperties() {
 		return this.properties;
 	}
 
 	public void setAuto(String key, boolean auto, Class<?> c) {
 		
 		if (c == Float.class) {
 			((FloatProp) this.getProperties().get(key)).setAuto(auto);
 		} else if (c == Double.class) {
 			((DoubleProp) this.getProperties().get(key)).setAuto(auto);
 		}else if (c == Integer.class) {
 			((IntProp) this.getProperties().get(key)).setAuto(auto);
 		} else {
 
 		}
 	}
 
 	public void setUnlimited(String key, boolean unlimited, Class<?> c) {
 
 		if (c == Float.class) {
 			((FloatProp) this.getProperties().get(key)).setUnlimited(unlimited);
 		} else if (c == Double.class) {
 			((DoubleProp) this.getProperties().get(key)).setUnlimited(unlimited);
 		}else if (c == Integer.class) {
 			((IntProp) this.getProperties().get(key)).setUnlimited(unlimited);
 		} else {
 
 		}
 	}
 
 	public void registerGuiElement(Observer guiElement, String propertyID) {
 		for ( Entry<String, SimProp> entry : this.properties.entrySet() ){
 			if (entry.getValue().getPropertyID().equals(propertyID)) {
 				entry.getValue().register( guiElement );
 			}
 		}
 	}
 }
