 package org.flowframe.ui.vaadin.addons.common;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import com.vaadin.addon.jpacontainer.JPAContainer;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.util.HierarchicalContainer;
 import com.vaadin.ui.Accordion;
 import com.vaadin.ui.VerticalLayout;
 
 public class FlowFrameNavigationAccordion extends VerticalLayout implements ValueChangeListener {
 	private static final long serialVersionUID = 3370715870286189986L;
 
 	private Accordion accordion;
 	private HashSet<ValueChangeListener> navigationListeners;
 	private Object currentValue;
 
 	public FlowFrameNavigationAccordion() {
 		setSizeFull();
 		setMargin(false, false, true, false);
 
 		navigationListeners = new HashSet<ValueChangeListener>();
 		accordion = new Accordion();
 		accordion.setStyleName("conx-navigation-accordion");
 		accordion.setSizeFull();
 
 		addComponent(accordion);
 	}
 
 	public void addCategory(FlowFrameNavigationTree subTree, String title) {
 		subTree.setNavigationListener(this);
 		accordion.addTab(subTree, title);
 	}
 
 	public void setContainer(HierarchicalContainer container) {
 		Collection<?> ids = container.rootItemIds();
 		
 		Property nameProperty = null;
 		for (Object id : ids) {
 			Item currentItem = container.getItem(id);
 			if (currentItem != null) {
 				nameProperty = currentItem.getItemProperty("Name");
 				if (nameProperty != null && nameProperty.getValue() instanceof String) {
 					FlowFrameNavigationTree tree = new FlowFrameNavigationTree();
 					tree.setTitlePropertyId("Name");
					tree.setContainerItemDataSource(container, id);
 					addCategory(tree, (String) nameProperty.getValue());
 				}
 			}
 		}
 	}
 
 	public void setContainer(JPAContainer<?> jpaContainer) {
 		throw new UnsupportedOperationException("Setting JPAContainers to back FF Accordion is unsupported.");
 	}
 
 	public void addNavigationListener(ValueChangeListener listener) {
 		navigationListeners.add(listener);
 	}
 
 	public void clearNavigationListeners() {
 		navigationListeners.clear();
 	}
 
 	public void valueChange(ValueChangeEvent event) {
 		currentValue = event.getProperty().getValue();
 		for (ValueChangeListener listener : navigationListeners) {
 			listener.valueChange(event);
 		}
 	}
 
 	public Object getValue() {
 		return currentValue;
 	}
 
 	public void setValue(Object currentValue) {
 		//		this.currentValue = currentValue;
 	}
 
 	public Object getNamePropertyId() {
 		return "Name";
 	}
 
 	public void setNamePropertyId(Object namePropertyId) {
 //		this.namePropertyId = namePropertyId;
 	}
 }
