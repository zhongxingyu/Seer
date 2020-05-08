 package evaluation.simulator.annotations.simulationProperty;
 
import java.util.LinkedList;
import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 // pojo
 public abstract class SimProp extends Observable {
 
 	private Class<? extends Requirement>[] enable_requirements;
 	// dependencies
 	boolean enabled;
 	private String id;
 	private String name;
 	private String plugin;
 	private int order;
 	private String pluginLayer;
 	private String propertykey;
 	private String tooltip;
 	private String info;
 	private Class<? extends Requirement>[] value_requirements;
 	private boolean isSuperclassProperty;
 	private boolean isGlobal;
 	private boolean isStatic;
 
 
 	public boolean getEnable() {
 		return this.enabled;
 	}
 
 	public Class<? extends Requirement>[] getEnable_requirements() {
 		return this.enable_requirements;
 	}
 
 	public String getPropertyID() {
 		return this.id;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public String getPluginID() {
 		return this.plugin;
 	}
 
 	public int getOrder() {
 		return this.order;
 	}
 
 	public String getPluginLayerID() {
 		return this.pluginLayer;
 	}
 
 	public String getPropertyKey() {
 		return this.propertykey;
 	}
 
 	public String getTooltip() {
 		return this.tooltip;
 	}
 
 	public abstract Object getValue();
 
 	public Class<? extends Requirement>[] getValue_requirements() {
 		return this.value_requirements;
 	}
 
 	// abstract methods
 	public abstract Class<?> getValueType();
 
 	@Override
 	public abstract String toString();
 
 
 	public void setEnable(boolean enableFlag) {
 		this.enabled = enableFlag;
 		changed();
 	}
 	
 	public abstract void register(Observer observer);
 	public abstract void unregister(Observer observer);
 	public abstract void changed();
 
 	public void setEnable_requirements(
 			Class<? extends Requirement>[] enable_requirements) {
 		this.enable_requirements = enable_requirements;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setPluginID(String namespace) {
 		this.plugin = namespace;
 	}
 
 	public void setOrder(int order) {
 		this.order = order;
 	}
 
 	public void setPluginLayerID(String pluginLayer) {
 		this.pluginLayer = pluginLayer;
 	}
 
 	public void setPropertyKey(String key) {
 		this.propertykey = key;
 	}
 
 	public void setTooltip(String tooltip) {
 		this.tooltip = tooltip;
 	}
 
 	public abstract void setValue(Object value);
 
 	public void setValue_requirements( Class<? extends Requirement>[] value_requirements) {
 		this.value_requirements = value_requirements;
 	}
 
 	public void setIsGlobal(boolean isGlobal) {
 		this.isGlobal = isGlobal;
 	}
 
 	public boolean isGlobal() {
 		return this.isGlobal;
 	}
 
 	public boolean isSuperclass() {
 		return this.isSuperclassProperty;
 	}
 
 	public void setIsSuperclass(boolean isSuperclass) {
 		this.isSuperclassProperty = isSuperclass;
 	}
 
 	public void setIsStatic(boolean isStatic) {
 		this.isStatic = isStatic;
 	}
 
 	public boolean isStatic() {
 		return this.isStatic;
 	}
 
 	public String getInfo() {
 		return info;
 	}
 
 	public void setInfo(String info) {
 		this.info = info;
 	}
 
 }
