 package org.genericsystem.impl;
 
 import org.genericsystem.api.core.Cache;
 import org.genericsystem.api.core.Generic;
 import org.genericsystem.api.core.GenericSystem;
 import org.genericsystem.api.core.Snapshot;
 import org.genericsystem.api.generic.Attribute;
 import org.genericsystem.api.generic.Relation;
 import org.genericsystem.api.generic.Type;
 import org.genericsystem.impl.core.GenericImpl;
 import org.testng.annotations.Test;
 
 @Test
 public class PhamtomLinkTest extends AbstractTest {
 	
 	public void testPhantomAttribute() {
		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine();
 		Type vehicle = cache.newType("Vehicle");
		Type car = vehicle.newSubType(cache, "Car");
 		
		Attribute vehiclePower = vehicle.setAttribute(cache, "power");
 		assert car.getAttributes(cache).contains(vehiclePower);
 		Attribute carPower = car.setAttribute(cache, "power");
 		assert car.getAttributes(cache).contains(carPower);
		car.cancel(cache, vehiclePower);
		assert !car.getAttributes(cache).contains(vehiclePower);
		assert !car.getAttributes(cache).contains(carPower);
		assert false : carPower.getInheritings(cache);
 		
		// car.restore(cache, vehiclePower);
		// assert car.getAttributes(cache).contains(vehiclePower);
 	}
 	
 	public void testPhantomSimpleRelation() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine();
 		Type vehicle = cache.newType("Vehicle");
 		Type human = cache.newType("Human");
 		Relation vehicleHuman = vehicle.setRelation(cache, "VehicleHuman", human);
 		Type car = vehicle.newSubType(cache, "Car");
 		
 		assert vehicle.getRelations(cache).size() == 1;
 		assert car.getRelations(cache).size() == 1;
 		
 		car.cancel(cache, vehicleHuman);
 		assert vehicle.getRelations(cache).size() == 1;
 		assert car.getRelations(cache).size() == 0;
 		
 		car.restore(cache, vehicleHuman);
 		assert vehicle.getRelations(cache).size() == 1;
 		assert car.getRelations(cache).size() == 1;
 		assert car.getRelations(cache).contains(vehicleHuman);
 	}
 	
 	public void testPhantomHierarchyRelation() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine();
 		Type vehicle = cache.newType("Vehicle");
 		Type human = cache.newType("Human");
 		Relation vehicleHuman = vehicle.setRelation(cache, "VehicleHuman", human);
 		Type car = vehicle.newSubType(cache, "Car");
 		Type convertible = car.newSubType(cache, "Convertible");
 		
 		assert vehicle.getRelations(cache).contains(vehicleHuman);
 		assert car.getRelations(cache).contains(vehicleHuman);
 		assert convertible.getRelations(cache).contains(vehicleHuman);
 		
 		car.cancel(cache, vehicleHuman);
 		assert vehicle.getRelations(cache).contains(vehicleHuman);
 		assert car.getRelations(cache).size() == 0;
 		assert !car.getRelations(cache).contains(vehicleHuman);
 		assert convertible.getRelations(cache).size() == 0;
 		assert !convertible.getRelations(cache).contains(vehicleHuman);
 		
 		car.restore(cache, vehicleHuman);
 		assert vehicle.getRelations(cache).contains(vehicleHuman);
 		assert car.getRelations(cache).contains(vehicleHuman);
 		assert convertible.getRelations(cache).contains(vehicleHuman);
 		
 		convertible.cancel(cache, vehicleHuman);
 		assert vehicle.getRelations(cache).contains(vehicleHuman);
 		assert car.getRelations(cache).contains(vehicleHuman);
 		assert convertible.getRelations(cache).size() == 0;
 		assert !convertible.getRelations(cache).contains(vehicleHuman);
 	}
 	
 	public void testPhantomMultiHierachyRelation() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine();
 		Type vehicle = cache.newType("Vehicle");
 		Type color = cache.newType("Color");
 		Relation vehicleColor = vehicle.setRelation(cache, "VehicleColor", color);
 		Type car = vehicle.newSubType(cache, "Car");
 		Type breakVehicle = car.newSubType(cache, "BreakVehicle");
 		Type convertible = car.newSubType(cache, "Convertible");
 		
 		car.cancel(cache, vehicleColor);
 		assert vehicle.getRelations(cache).size() == 1;
 		assert car.getRelations(cache).size() == 0;
 		assert breakVehicle.getRelations(cache).size() == 0;
 		assert convertible.getRelations(cache).size() == 0;
 		
 		car.restore(cache, vehicleColor);
 		assert vehicle.getRelations(cache).size() == 1;
 		assert car.getRelations(cache).get(0).equals(vehicleColor);
 		assert breakVehicle.getRelations(cache).get(0).equals(vehicleColor);
 		assert convertible.getRelations(cache).get(0).equals(vehicleColor);
 	}
 	
 	public void testRelationsWIthSameName() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine();
 		Type vehicle = cache.newType("Vehicle");
 		Type human = cache.newType("Human");
 		Type color = cache.newType("Color");
 		Type car = vehicle.newSubType(cache, "Car");
 		
 		Relation vehicleHuman = vehicle.setRelation(cache, "VehicleHuman", human);
 		Relation vehicleColor = vehicle.setRelation(cache, "VehicleHuman", color);
 		assert vehicleHuman.getImplicit().equals(vehicleColor.getImplicit());
 		car.cancel(cache, vehicleColor);
 		assert car.getRelations(cache).size() == 1;
 		assert car.getRelations(cache).get(0).getTargetComponent().equals(human);
 		
 		car.cancel(cache, vehicleHuman);
 		car.restore(cache, vehicleHuman);
 		assert car.getRelations(cache).size() == 1;
 		assert car.getRelations(cache).get(0).getTargetComponent().equals(human);
 	}
 	
 	public void testAttributeWithGetInstances() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine();
 		Type vehicle = cache.newType("Vehicle");
 		Type car = vehicle.newSubType(cache, "Car");
 		Attribute vehiclePower = vehicle.setRelation(cache, "power");
 		
 		Generic myVehicle = vehicle.newInstance(cache, "myVehicle");
 		Generic myCar = car.newInstance(cache, "myCar");
 		
 		myVehicle.setValue(cache, vehiclePower, "123");
 		myCar.setValue(cache, vehiclePower, "256");
 		
 		assert vehiclePower.getInstances(cache).filter(new Snapshot.Filter<Generic>() {
 			
 			@Override
 			public boolean isSelected(Generic element) {
 				return ((GenericImpl) element).isPhantom();
 			}
 		}).isEmpty();
 		assert vehiclePower.getAllInstances(cache).filter(new Snapshot.Filter<Generic>() {
 			
 			@Override
 			public boolean isSelected(Generic element) {
 				return ((GenericImpl) element).isPhantom();
 			}
 		}).isEmpty();
 	}
 	
 	public void testAttributeWithGetSubTypes() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine();
 		Type vehicle = cache.newType("Vehicle");
 		Type car = vehicle.newSubType(cache, "Car");
 		Attribute vehiclePower = vehicle.setRelation(cache, "power");
 		
 		Generic myVehicle = vehicle.newInstance(cache, "myVehicle");
 		Generic myCar = car.newInstance(cache, "myCar");
 		
 		myVehicle.setValue(cache, vehiclePower, "123");
 		myCar.setValue(cache, vehiclePower, "256");
 		
 		assert vehiclePower.getSubTypes(cache).isEmpty();
 		assert vehiclePower.getAllSubTypes(cache).size() == 1 && vehiclePower.getAllSubTypes(cache).contains(vehiclePower);
 	}
 	
 	public void testAttributeWithGetInheritings() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine();
 		Type vehicle = cache.newType("Vehicle");
 		Type car = vehicle.newSubType(cache, "Car");
 		Attribute vehiclePower = vehicle.setRelation(cache, "power");
 		
 		Generic myVehicle = vehicle.newInstance(cache, "myVehicle");
 		Generic myCar = car.newInstance(cache, "myCar");
 		
 		myVehicle.setValue(cache, vehiclePower, "123");
 		myCar.setValue(cache, vehiclePower, "256");
 		
 		assert vehiclePower.getInheritings(cache).filter(new Snapshot.Filter<Generic>() {
 			
 			@Override
 			public boolean isSelected(Generic element) {
 				return ((GenericImpl) element).isPhantom();
 			}
 		}).isEmpty();
 	}
 	
 }
