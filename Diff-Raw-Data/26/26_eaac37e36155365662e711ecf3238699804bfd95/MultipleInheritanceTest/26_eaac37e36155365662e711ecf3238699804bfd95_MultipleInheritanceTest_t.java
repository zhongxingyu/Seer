 package org.genericsystem.impl;
 
 import org.genericsystem.core.Cache;
import org.genericsystem.core.CacheImpl;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.core.GenericSystem;
 import org.genericsystem.core.Snapshot;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Type;
 import org.testng.annotations.Test;
 
 @Test
 public class MultipleInheritanceTest extends AbstractTest {
 
 	public void testInheritsFrom() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type graphicComponent = cache.addType("graphicComponent");
 		Type window = graphicComponent.addSubType("Window");
 		Type selectable = graphicComponent.addSubType("Selectable");
 		Type selectableWindow = cache.addType("selectableWindow", selectable, window);
 
 		assert selectableWindow.inheritsFrom(selectable);
 		assert selectableWindow.inheritsFrom(window);
 		assert selectableWindow.inheritsFrom(graphicComponent);
 	}
 
	public void testAttributeProjection() {
		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
		Type car = cache.addType("Car");
		Type robot = cache.addType("Robot");
		Type transformer = cache.addType("Transformer", car, robot);
		Attribute carPower = car.addAttribute("Power");
		Attribute robotPower = robot.addAttribute("Power");
		Attribute transformerPower = transformer.getAttribute("Power");

		assert transformerPower.inheritsFrom(carPower);
		assert transformerPower.inheritsFrom(robotPower);
		assert transformerPower.equals(carPower);
		assert transformerPower.equals(robotPower);
		assert ((CacheImpl) cache).isAutomatic(transformerPower);

	}

 	public void testgetDirectSubTypes() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type window = cache.addType("Window");
 		Type selectable = cache.addType("Selectable");
 		Type selectableWindow = cache.addType("selectableWindow", selectable, window);
 		assert window.getSubTypes().size() == 1 : window.getSubTypes();
 		assert window.getSubTypes().contains(selectableWindow) : window.getSubTypes();
 
 		assert selectable.getSubTypes().size() == 1 : selectable.getSubTypes();
 		assert selectable.getSubTypes().contains(selectableWindow);
 
 		assert selectableWindow.getSubTypes().size() == 0 : selectableWindow.getSubTypes();
 	}
 
 	public void testgetDirectSubTypesWithDiamondProblem() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type graphicComponent = cache.addType("graphicComponent");
 		Type window = graphicComponent.addSubType("Window");
 		Type selectable = graphicComponent.addSubType("Selectable");
 		Type selectableWindow = cache.addType("selectableWindow", selectable, window);
 
 		assert graphicComponent.getSubTypes().size() == 2 : graphicComponent.getSubTypes();
 		assert graphicComponent.getAllSubTypes().size() == 3 : graphicComponent.getAllSubTypes();
 		assert graphicComponent.getSubTypes().contains(selectable);
 		assert graphicComponent.getSubTypes().contains(window) : graphicComponent.getSubTypes();
 
 		assert window.getSubTypes().size() == 1 : window.getSubTypes();
 		assert window.getSubTypes().contains(selectableWindow) : window.getSubTypes();
 
 		assert selectable.getSubTypes().size() == 1 : selectable.getSubTypes();
 		assert selectable.getSubTypes().contains(selectableWindow);
 
 		assert selectableWindow.getSubTypes().size() == 0;
 	}
 
 	public void testGetAllSubTypes() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type window = cache.addType("Window");
 		Type selectable = cache.addType("Selectable");
 		Type selectableWindow = cache.addType("selectableWindow", selectable, window);
 
 		Snapshot<Generic> allInstances = cache.getEngine().getAllInstances();
 		assert !allInstances.contains(cache.getEngine());
 		assert allInstances.contains(window);
 		assert allInstances.contains(selectable);
 		assert allInstances.contains(selectableWindow);
 
 		Snapshot<Generic> allSubTypes = window.getAllSubTypes();
 		assert allSubTypes.size() == 1;
 		assert allSubTypes.contains(selectableWindow);
 
 		allSubTypes = selectable.getAllSubTypes();
 		assert allSubTypes.size() == 1;
 		assert allSubTypes.contains(selectableWindow);
 
 		allSubTypes = selectableWindow.getAllSubTypes();
 		assert allSubTypes.isEmpty();
 	}
 
 	public void testGetAllSubTypesWithDiamond() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type graphicComponent = cache.addType("graphicComponent");
 		Type window = graphicComponent.addSubType("Window");
 		Type selectable = graphicComponent.addSubType("Selectable");
 		Type selectableWindow = cache.addType("selectableWindow", selectable, window);
 
 		Snapshot<Generic> allSubTypes = graphicComponent.getAllSubTypes();
 		assert allSubTypes.size() == 3;
 		assert graphicComponent.getAllSubTypes().contains(window);
 		assert graphicComponent.getAllSubTypes().contains(selectable);
 		assert graphicComponent.getAllSubTypes().contains(selectableWindow);
 
 		assert window.getAllSubTypes().size() == 1 : window.getAllSubTypes();
 		assert window.getAllSubTypes().contains(selectableWindow);
 
 		assert selectable.getAllSubTypes().size() == 1 : selectable.getAllSubTypes();
 		assert selectable.getAllSubTypes().contains(selectableWindow);
 
 		assert selectableWindow.getAllSubTypes().isEmpty();
 	}
 
 	public void testgetDirectSubTypesWithDiamond() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 
 		Type graphicComponent = cache.addType("graphicComponent");
 		Type window = graphicComponent.addSubType("Window");
 		Type selectable = graphicComponent.addSubType("Selectable");
 		Type selectableWindow = cache.addType("selectableWindow", selectable, window);
 
 		Snapshot<Generic> subTypes = graphicComponent.getSubTypes();
 		assert subTypes.size() == 2;
 		assert subTypes.contains(window);
 		assert subTypes.contains(selectable);
 
 		Snapshot<Generic> allSubTypes = graphicComponent.getAllSubTypes();
 
 		assert allSubTypes.size() == 3;
 		assert allSubTypes.contains(window);
 		assert allSubTypes.contains(selectable);
 		assert allSubTypes.contains(selectableWindow);
 
 		subTypes = window.getSubTypes();
 		assert subTypes.size() == 1;
 		assert subTypes.contains(selectableWindow);
 
 		subTypes = selectable.getSubTypes();
 		assert subTypes.size() == 1;
 		assert subTypes.contains(selectableWindow);
 
 		assert selectableWindow.getSubTypes().size() == 0;
 	}
 
 	public void testgetDirectSubTypesWithImplicit() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type window = cache.addType("Window");
 		Type selectable = cache.addType("Selectable");
 		Type selectableWindow = cache.addType("selectableWindow", selectable, window);
 
 		assert selectableWindow.inheritsFrom(selectable);
 		assert selectableWindow.inheritsFrom(window);
 	}
 
 	public void testgetDirectSubTypesWithImplicitWithDiamond() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 
 		Type graphicComponent = cache.addType("graphicComponent");
 		Type window = graphicComponent.addSubType("Window");
 		Type selectable = graphicComponent.addSubType("Selectable");
 		Type selectableWindow = cache.addType("selectableWindow", selectable, window);
 
 		Snapshot<Generic> subTypes = graphicComponent.getSubTypes();
 		Snapshot<Generic> allSubTypes = graphicComponent.getAllSubTypes();
 		assert subTypes.size() == 2 : subTypes;
 		assert !subTypes.contains(graphicComponent);
 		assert subTypes.contains(window);
 		assert subTypes.contains(selectable);
 		assert !subTypes.contains(selectableWindow);
 
 		assert allSubTypes.size() == 3 : allSubTypes;
 		assert allSubTypes.contains(window);
 		assert allSubTypes.contains(selectable);
 		assert allSubTypes.contains(selectableWindow);
 
 		assert selectableWindow.inheritsFrom(selectable);
 		assert selectableWindow.inheritsFrom(window);
 		assert selectableWindow.inheritsFrom(graphicComponent);
 	}
 
 	public void testAttribute() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type graphicComponent = cache.addType("graphicComponent");
 		Type window = graphicComponent.addSubType("Window");
 		Type selectable = graphicComponent.addSubType("Selectable");
 		Type selectableWindow = cache.addType("selectableWindow", selectable, window);
 		Attribute size = graphicComponent.setAttribute("size");
 		Attribute selectedSelectable = selectable.setAttribute("Selected");
 		Attribute height = selectableWindow.setAttribute("height");
 
 		assert !selectableWindow.isAttributeOf(selectableWindow) : ((GenericImpl) selectableWindow).getBaseComponent();
 		assert selectableWindow.getAttributes().contains(size) : selectableWindow.getAttributes();
 		assert selectableWindow.getAttributes().contains(selectedSelectable);
 		assert selectableWindow.getAttributes().contains(height);
 	}
 
 	public void testValue() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type graphicComponent = cache.addType("graphicComponent");
 		Type window = graphicComponent.addSubType("Window");
 		Type selectable = graphicComponent.addSubType("Selectable");
 		Type selectableWindow = cache.addType("selectableWindow", selectable, window);
 		Attribute size = graphicComponent.setAttribute("size");
 		Attribute selectedSelectable = selectable.setAttribute("Selected");
 		Generic mySelectableWindow = selectableWindow.addInstance("mySelectableWindow");
 
 		Holder v12 = mySelectableWindow.setValue(size, 12);
 		Holder vTrue = mySelectableWindow.setValue(selectedSelectable, true);
 
 		assert selectableWindow.getInstances().size() == 1 : selectableWindow.getInstances();
 		assert selectableWindow.getInstances().contains(mySelectableWindow);
 		assert mySelectableWindow.getHolders(size).size() == 1 : mySelectableWindow.getHolders(size);
 		assert mySelectableWindow.getHolders(size).contains(v12);
 		assert mySelectableWindow.getHolders(selectedSelectable).size() == 1;
 		assert mySelectableWindow.getHolders(selectedSelectable).contains(vTrue);
 	}
 
 	public void testBaseComponent() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type graphicComponent = cache.addType("graphicComponent");
 		Type window = graphicComponent.addSubType("Window");
 		Type selectable = graphicComponent.addSubType("Selectable");
 		Type selectableWindow = cache.addType("selectableWindow", selectable, window);
 
 		assert ((Holder) selectableWindow).getBaseComponent() == null : ((Holder) selectableWindow).getBaseComponent();
 	}
 
 	public void testTargetComponent() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type graphicComponent = cache.addType("graphicComponent");
 		Type window = graphicComponent.addSubType("Window");
 		Type selectable = graphicComponent.addSubType("Selectable");
 		Type selectableWindow = cache.addType("selectableWindow", selectable, window);
 
 		assert ((GenericImpl) selectableWindow).getTargetComponent() == null : ((GenericImpl) selectableWindow).getTargetComponent();
 	}
 
 }
