 package org.genericsystem.myadmin.beans;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.core.Structural;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 import org.genericsystem.myadmin.beans.GenericTreeNode.TreeType;
 import org.genericsystem.myadmin.beans.MenuBean.MenuEvent;
 import org.genericsystem.myadmin.beans.PanelBean.PanelTitleChangeEvent;
 import org.genericsystem.myadmin.beans.TreeBean.TreeSelectionEvent;
 import org.genericsystem.myadmin.util.GsMessages;
 import org.genericsystem.myadmin.util.GsRedirect;
 import org.richfaces.event.DropEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Named
 @SessionScoped
 public class TypesBean implements Serializable {
 
 	private static final long serialVersionUID = 8042406937175946234L;
 
 	// TODO clean
 	private static Logger log = LoggerFactory.getLogger(TypesBean.class);
 
 	@Inject
 	private transient Cache cache;
 
 	@Inject
 	private GsMessages messages;
 
 	@Inject
 	private GsRedirect redirect;
 
 	private GenericTreeNode rootTreeNode;
 
 	private GenericTreeNode selectedTreeNode;
 
 	@Inject
 	private Event<PanelTitleChangeEvent> panelTitleChangeEvent;
 
 	@Inject
 	private Event<MenuEvent> menuEvent;
 
 	private boolean implicitShow;
 
 	@PostConstruct
 	public void init() {
 		rootTreeNode = new GenericTreeNode(null, cache.getEngine(), GenericTreeNode.TreeType_DEFAULT);
 		selectedTreeNode = rootTreeNode;
 
 		// TODO TEST
 		Type vehicle = cache.newType("Vehicle");
 		Type car = vehicle.newSubType(cache, "Car");
 		Type color = cache.newType("Color");
 		Type time = cache.newType("Time");
 		Attribute power = vehicle.setAttribute(cache, "power");
 		Relation vehicleColor = vehicle.setRelation(cache, "vehicleColor", color);
 		Relation vehicleColorTime = vehicle.setRelation(cache, "vehicleColorTime", color, time);
 		Generic myVehicle = vehicle.newInstance(cache, "myVehicle");
 		Generic red = color.newInstance(cache, "red");
 		Generic yellow = color.newInstance(cache, "yellow");
 		vehicle.setValue(cache, power, 123);
 		myVehicle.setValue(cache, power, 136);
 		myVehicle.setLink(cache, vehicleColor, "myVehicleRed", red);
 		myVehicle.bind(cache, vehicleColorTime, red, time.newInstance(cache, "myTime"));
 		vehicle.bind(cache, vehicleColor, yellow);
 		car.newInstance(cache, "myCar");
 
 		Type human = cache.newType("Human");
 		Generic nicolas = human.newInstance(cache, "Nicolas");
 		Generic michael = human.newInstance(cache, "Michael");
 		Generic quentin = human.newInstance(cache, "Quentin");
 		Relation isTallerOrEqualThan = human.setRelation(cache, "isTallerOrEqualThan", human);
 		nicolas.bind(cache, isTallerOrEqualThan, michael);
 		nicolas.bind(cache, isTallerOrEqualThan, nicolas);
 		Relation isBrotherOf = human.setRelation(cache, "isBrotherOf", human);
 		isBrotherOf.enableMultiDirectional(cache);
 		// quentin.bind(cache, isBrotherOf, michael);
 		quentin.setLink(cache, isBrotherOf, "link", michael);
 		Relation isBossOf = human.setRelation(cache, "isBossOf", human);
 		nicolas.bind(cache, isBossOf, michael);
 
 		michael.getProperties(cache).put("KEY TEST", "VALUE TEST");
 	}
 
 	public List<GenericTreeNode> getRoot() {
 		return Collections.singletonList(rootTreeNode);
 	}
 
 	public List<GenericTreeNode> getChildrens(final GenericTreeNode genericTreeNode) {
 		return genericTreeNode.getChildrens(cache, implicitShow);
 	}
 
 	public void newType(String newValue) {
 		cache.newType(newValue);
 		messages.info("createRootType", newValue);
 	}
 
 	public void newSubType(String newValue) {
 		((Type) getSelectedTreeNodeGeneric()).newSubType(cache, newValue);
 		messages.info("createSubType", newValue, getSelectedTreeNodeGeneric().getValue());
 	}
 
 	public void setAttribute(String newValue) {
 		((Type) getSelectedTreeNodeGeneric()).setAttribute(cache, newValue);
 		messages.info("createRootAttribute", newValue, getSelectedTreeNodeGeneric().getValue());
 	}
 
 	public void addProperty(String key, String value) {
 		((Type) getSelectedTreeNodeGeneric()).getProperties(cache).put(key, value);
 		messages.info("createRootProperty", key, value);
 	}
 
 	public void newInstance(String newValue) {
 		((Type) getSelectedTreeNodeGeneric()).newInstance(cache, newValue);
 		messages.info("createRootInstance", newValue, getSelectedTreeNodeGeneric().getValue());
 	}
 
 	public List<Structural> getStructurals() {
 		return getSelectedTreeNodeGeneric().getStructurals(cache).toList();
 	}
 
 	public List<Holder> getHolders(Structural structural) {
 		return ((Type) getSelectedTreeNodeGeneric()).getHolders(cache, structural.getAttribute(), structural.getPosition()).toList();
 	}
 
 	public List<Generic> getOtherTargets(int basePos, Holder holder) {
 		if (((Attribute) holder).isMultiDirectional(cache))
 			basePos = getBasePosIfMultiDirectional(basePos, holder);
 		return getSelectedTreeNodeGeneric().getOtherTargets(basePos, holder).toList();
 	}
 
 	public int getBasePosIfMultiDirectional(int originalBasePos, Holder holder) {
 		Generic[] components = ((GenericImpl) holder).getComponentsArray();
 		for (int i = 0; i < components.length; i++)
 			if (components[i].equals(getSelectedTreeNodeGeneric()))
 				return i;
 		throw new IllegalStateException("Unable to find position");
 	}
 
 	public class TargetWrapper {
 		private Generic generic;
 
 		private Holder holder;
 
 		public TargetWrapper(Generic generic, Holder holder) {
 			this.generic = generic;
 			this.holder = holder;
 		}
 
 		public boolean isBaseComponent() {
 			return holder.getBaseComponent().equals(generic);
 		}
 
 		public Generic getGeneric() {
 			return generic;
 		}
 
 		public void setGeneric(Generic generic) {
 			this.generic = generic;
 		}
 
 		public Holder getHolder() {
 			return holder;
 		}
 
 		public void setHolder(Holder holder) {
 			this.holder = holder;
 		}
 	}
 
 	public void addValue(Attribute attribute, String newValue) {
 		Generic currentInstance = getSelectedTreeNodeGeneric();
 		currentInstance.setValue(cache, attribute, newValue);
 		messages.info("addValue", newValue, attribute, currentInstance);
 	}
 
 	public void removeHolder(Holder holder) {
 		if (holder.getBaseComponent().equals(selectedTreeNode.getGeneric())) {
 			holder.remove(cache);
 			messages.info("remove", holder);
 		} else {
 			selectedTreeNode.getGeneric().cancel(cache, holder, true);
 			messages.info("cancel", holder);
 		}
 	}
 
 	public String delete() {
 		Generic generic = getSelectedTreeNodeGeneric();
 		if (isValue(generic)) {
 			selectedTreeNode = selectedTreeNode.getParent();
 			removeHolder((Holder) generic);
 		} else {
 			generic.remove(cache);
 			messages.info("deleteFile", generic.getValue());
 			selectedTreeNode = selectedTreeNode.getParent();
 		}
 		return "";
 	}
 
 	public void changeType(@Observes/* @TreeSelection */TreeSelectionEvent treeSelectionEvent) {
 		if (treeSelectionEvent.getId().equals("typestree")) {
 			selectedTreeNode = (GenericTreeNode) treeSelectionEvent.getObject();
 			internalChangeType();
 			messages.info("typeselectionchanged", getSelectedTreeNodeGeneric().toString());
 		}
 	}
 
 	private void internalChangeType() {
 		menuEvent.fire(new MenuEvent(cache, selectedTreeNode, implicitShow));
 		panelTitleChangeEvent.fire(new PanelTitleChangeEvent("typesmanager", ((GenericImpl) getSelectedTreeNodeGeneric()).toCategoryString()));
 	}
 
 	public void changeAttributeSelected(int attributeIndex) {
 		Attribute attribute = (Attribute) selectedTreeNode.getChildrens(cache, TreeType.ATTRIBUTES, implicitShow).get(attributeIndex).getGeneric();
 		selectedTreeNode.setAttribute(attribute);
 		selectedTreeNode.setTreeType(TreeType.VALUES);
 		messages.info("showvalues", attribute);
 	}
 
 	public void changeTreeType(TreeType treeType) {
 		selectedTreeNode.setTreeType(treeType);
 		messages.info("showchanged", treeType);
 	}
 
 	public void processDrop(DropEvent dropEvent) {
 		Object dragValue = dropEvent.getDragValue();
 		Type type = (Type) getSelectedTreeNodeGeneric();
 		Attribute attribute = type.setAttribute(cache, "new_attribute");
 		attribute.setConstraintClass(cache, dragValue.getClass());
 		log.info("getDragValue " + dragValue);
 		messages.info("dropValue", dragValue);
 	}
 
 	public List<Entry<Serializable, Serializable>> getProperties() {
 		return getSelectedTreeNodeGeneric().getPropertiesShot(cache).toList();
 	}
 
 	public PropertyWrapper getPropertyWrapper(Entry<Serializable, Serializable> entry) {
 		return new PropertyWrapper(entry);
 	}
 
 	public void removeProperty(Entry<Serializable, Serializable> entry) {
 		getSelectedTreeNodeGeneric().getProperties(cache).remove(entry.getKey());
 		messages.info("remove", entry.getKey());
 	}
 
 	public class PropertyWrapper {
 		private Entry<Serializable, Serializable> entry;
 
 		public PropertyWrapper(Entry<Serializable, Serializable> entry) {
 			this.entry = entry;
 		}
 
 		public String getValue() {
 			return (String) entry.getValue();
 		}
 
 		public void setValue(String newValue) {
 			if (!newValue.equals(entry.getValue().toString())) {
 				getSelectedTreeNodeGeneric().getProperties(cache).put(entry.getKey(), newValue);
 				messages.info("updateValue", entry.getValue(), newValue);
 			}
 		}
 	}
 
 	public void view(Generic generic) {
 		selectedTreeNode = changeView(rootTreeNode, generic);
 		internalChangeType();
 		messages.info("typeselectionchanged", selectedTreeNode.getGeneric());
 	}
 
 	public GenericTreeNode changeView(GenericTreeNode genericTreeNode, Generic generic) {
 		if (genericTreeNode.getGeneric().equals(generic))
 			return genericTreeNode;
 		for (GenericTreeNode tmp : getChildrens(genericTreeNode)) {
 			GenericTreeNode child = changeView(tmp, generic);
 			if (null != child)
 				return child;
 		}
 		return null;
 	}
 
 	public Generic getSelectedTreeNodeGeneric() {
 		return selectedTreeNode.getGeneric();
 	}
 
 	public String getSelectedTreeNodeValue() {
 		return selectedTreeNode.getValue();
 	}
 
 	public boolean isTreeTypeSelected(TreeType treeType) {
 		return selectedTreeNode != null && selectedTreeNode.getTreeType() == treeType;
 	}
 
 	public boolean isSingular(Structural structural) {
 		return structural.getAttribute().isSingularConstraintEnabled(cache);
 	}
 
 	// TODO in GS CORE
 	public boolean isValue(Generic generic) {
 		return generic.isConcrete() && generic.isAttribute();
 	}
 
 	public Wrapper getWrapper(GenericTreeNode genericTreeNode) {
 		return new Wrapper(genericTreeNode);
 	}
 
 	public class Wrapper {
 		private GenericTreeNode genericTreeNode;
 
 		public Wrapper(GenericTreeNode genericTreeNode) {
 			this.genericTreeNode = genericTreeNode;
 		}
 
 		public String getValue() {
 			return genericTreeNode.getValue();
 		}
 
 		public void setValue(String newValue) {
 			Generic generic = genericTreeNode.getGeneric();
 			if (!newValue.equals(generic.toString())) {
 				genericTreeNode.setGeneric(generic.updateKey(cache, newValue));
				messages.info("updateShortPath", newValue, generic.getValue());
 			}
 		}
 	}
 
 	public GenericWrapper getGenericWrapper(Generic generic) {
 		return new GenericWrapper(generic);
 	}
 
 	public class GenericWrapper {
 		private Generic wrappedGeneric;
 
 		public GenericWrapper(Generic wrappedGeneric) {
 			this.wrappedGeneric = wrappedGeneric;
 		}
 
 		public String getValue() {
 			return wrappedGeneric.toString();
 		}
 
 		public void setValue(String newValue) {
 			if (!newValue.equals(wrappedGeneric.toString())) {
 				wrappedGeneric.updateKey(cache, newValue);
 				messages.info("updateValue", wrappedGeneric, newValue);
 			}
 		}
 	}
 
 	public String getExpandedIcon() {
 		return getExpandedIcon(selectedTreeNode);
 	}
 
 	public String getExpandedIcon(GenericTreeNode genericTreeNode) {
 		switch (genericTreeNode.getTreeType()) {
 		case SUPERS:
 			return messages.getInfos("up_green_arrow");
 		case INSTANCES:
 			return messages.getInfos("down_green_arrow");
 		case INHERITINGS:
 			return messages.getInfos("down_right_green_arrow");
 		case COMPONENTS:
 			return messages.getInfos("left_green_arrow");
 		case COMPOSITES:
 			return messages.getInfos("right_green_arrow");
 		case ATTRIBUTES:
 			return messages.getInfos("up_right_green_arrow");
 		case VALUES:
 			return messages.getInfos("right_green_arrow");
 		default:
 			break;
 		}
 		throw new IllegalStateException();
 	}
 
 	public String getCollapsedIcon() {
 		return getCollapsedIcon(selectedTreeNode);
 	}
 
 	public String getCollapsedIcon(GenericTreeNode genericTreeNode) {
 		switch (genericTreeNode.getTreeType()) {
 		case SUPERS:
 			return messages.getInfos("up_red_arrow");
 		case INSTANCES:
 			return messages.getInfos("down_red_arrow");
 		case INHERITINGS:
 			return messages.getInfos("down_right_red_arrow");
 		case COMPONENTS:
 			return messages.getInfos("left_red_arrow");
 		case COMPOSITES:
 			return messages.getInfos("right_red_arrow");
 		case ATTRIBUTES:
 			return messages.getInfos("up_right_red_arrow");
 		case VALUES:
 			return messages.getInfos("right_red_arrow");
 		default:
 			break;
 		}
 		throw new IllegalStateException();
 	}
 
 	public String getIconTitle() {
 		return getIconTitle(selectedTreeNode);
 	}
 
 	public String getIconTitle(GenericTreeNode genericTreeNode) {
 		switch (genericTreeNode.getTreeType()) {
 		case SUPERS:
 			return messages.getMessage("super");
 		case INSTANCES:
 			return messages.getMessage("instance");
 		case INHERITINGS:
 			return messages.getMessage("inheriting");
 		case COMPONENTS:
 			return messages.getMessage("component");
 		case COMPOSITES:
 			return messages.getMessage("composite");
 		case ATTRIBUTES:
 			return messages.getMessage("attribute");
 		case VALUES:
 			return messages.getMessage("value");
 		default:
 			break;
 		}
 		throw new IllegalStateException();
 	}
 
 	public String getMenuTypeIcon(String genericType) {
 		switch (genericType) {
 		case "TYPE":
 			return messages.getInfos("bullet_square_yellow");
 		case "ATTRIBUTE":
 			return messages.getInfos("bullet_triangle_yellow");
 		case "INSTANCE":
 			return messages.getInfos("bullet_square_green");
 		default:
 			break;
 		}
 		throw new IllegalStateException();
 	}
 
 	public String getTypeIcon(GenericTreeNode genericTreeNode) {
 		Generic generic = genericTreeNode.getGeneric();
 		if (generic.isMeta()) {
 			if (generic.isType())
 				return messages.getInfos("bullet_square_red");
 			if (generic.isReallyAttribute())
 				return messages.getInfos("bullet_triangle_red");
 			if (generic.isRelation())
 				return messages.getInfos("bullet_ball_red");
 		} else if (generic.isStructural()) {
 			if (generic.isType())
 				return messages.getInfos("bullet_square_yellow");
 			if (generic.isReallyAttribute())
 				return messages.getInfos("bullet_triangle_yellow");
 			if (generic.isRelation())
 				return messages.getInfos("bullet_ball_yellow");
 		} else if (generic.isConcrete()) {
 			if (generic.isType())
 				return messages.getInfos("bullet_square_green");
 			if (generic.isReallyAttribute())
 				return messages.getInfos("bullet_triangle_green");
 			if (generic.isRelation())
 				return messages.getInfos("bullet_ball_green");
 		}
 		throw new IllegalStateException();
 	}
 
 	public String getTypeIconTitle(GenericTreeNode genericTreeNode) {
 		Generic generic = genericTreeNode.getGeneric();
 		if (generic.isMeta() && generic.isType())
 			return messages.getMessage("meta") + " " + messages.getMessage("type");
 		else if (generic.isMeta() && generic.isReallyAttribute())
 			return messages.getMessage("meta") + " " + messages.getMessage("attribute");
 		else if (generic.isMeta() && generic.isRelation())
 			return messages.getMessage("meta") + " " + messages.getMessage("relation");
 		else if (generic.isStructural() && generic.isType())
 			return messages.getMessage("type");
 		else if (generic.isStructural() && generic.isReallyAttribute())
 			return messages.getMessage("attribute");
 		else if (generic.isStructural() && generic.isRelation())
 			return messages.getMessage("relation");
 		else if (generic.isConcrete() && generic.isType())
 			return messages.getMessage("instance");
 		else if (generic.isConcrete() && generic.isReallyAttribute())
 			return messages.getMessage("value");
 		else if (generic.isConcrete() && generic.isRelation())
 			return messages.getMessage("link");
 		throw new IllegalStateException();
 	}
 
 	public String getStyle(GenericTreeNode genericTreeNode) {
 		return genericTreeNode.isImplicitAutomatic(genericTreeNode.getGeneric()) || (isValue(genericTreeNode.getGeneric()) && !((Holder) genericTreeNode.getGeneric()).getBaseComponent().equals(selectedTreeNode.getGeneric())) ? "implicitColor" : "";
 	}
 
 	public String getHolderStyle(Holder holder) {
 		return holder.getBaseComponent().equals(getSelectedTreeNodeGeneric()) ? "" : "italic";
 	}
 
 	public boolean isBaseComponent(Holder holder) {
 		return holder.getBaseComponent().equals(selectedTreeNode.getGeneric());
 	}
 
 	public boolean isImplicitShow() {
 		return implicitShow;
 	}
 
 	public void setImplicitShow(boolean implicitShow) {
 		this.implicitShow = implicitShow;
 	}
 }
