 package org.genericsystem.impl;
 
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.GenericSystem;
 import org.genericsystem.core.Statics;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Type;
 import org.testng.annotations.Test;
 
 @Test
 public class CancelClearTest extends AbstractTest {
 
 	public void testCancelAttribute() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.addType("Vehicle");
 		Attribute vehiclePower = vehicle.addProperty("power");
 		Type car = vehicle.addSubType("Car");
 		assert car.getAttribute("power").equals(vehiclePower);
 		car.cancel(vehiclePower, Statics.STRUCTURAL);
		assert car.getHolder(vehiclePower) == null : car.getHolder(vehiclePower);
 	}
 
 	public void testCancelValue() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.addType("Vehicle");
 		Attribute vehiclePower = vehicle.addProperty("power");
 		Holder vehicle233 = vehicle.addValue(vehiclePower, 233);
 		Type car = vehicle.addSubType("Car");
 		assert car.getHolder(vehiclePower).equals(vehicle233) : car.getHolder(vehiclePower);
 		car.cancel(vehicle233);
 		assert car.getValue(vehiclePower) == null : car.getValue(vehiclePower);
 	}
 
 	public void testCancelValue2() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.addType("Vehicle");
 		Attribute vehiclePower = vehicle.addProperty("power");
 		Holder vehicle233 = vehicle.addValue(vehiclePower, 233);
 		Type car = vehicle.addSubType("Car");
 		assert car.getHolder(vehiclePower).equals(vehicle233) : car.getHolder(vehiclePower);
		car.cancel(vehiclePower, Statics.CONCRETE);
 		assert car.getValue(vehiclePower) == null : car.getHolder(vehiclePower).info();
 	}
 
 	public void testCancelAllValue() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.addType("Vehicle");
 		Attribute vehiclePower = vehicle.addProperty("power");
 		Holder vehicle233 = vehicle.addValue(vehiclePower, 233);
 		Type car = vehicle.addSubType("Car");
 		assert car.getHolder(vehiclePower).equals(vehicle233) : car.getHolder(vehiclePower);
 		car.cancelAll(vehicle233);
 		assert car.getValue(vehiclePower) == null : car.getValue(vehiclePower);
 	}
 
 	public void testCancelAllValue2() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.addType("Vehicle");
 		Attribute vehiclePower = vehicle.addProperty("power");
 		Holder vehicle233 = vehicle.addValue(vehiclePower, 233);
 		Type car = vehicle.addSubType("Car");
 		assert car.getHolder(vehiclePower).equals(vehicle233) : car.getHolder(vehiclePower);
 		car.cancelAll(vehiclePower, Statics.STRUCTURAL);
		assert car.getHolder(vehiclePower) == null : car.getValue(vehiclePower);
 	}
 
 	public void testClearAttribute() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.addType("Vehicle");
 		Attribute vehiclePower = vehicle.addProperty("power");
 		Type car = vehicle.addSubType("Car");
 		assert car.getAttribute("power").equals(vehiclePower);
 		car.cancel(vehiclePower, Statics.STRUCTURAL);
 		car.clear(vehiclePower, Statics.STRUCTURAL);
 		assert car.getAttributes().contains(vehiclePower);
 		assert car.getAttribute("power") == vehiclePower;
 	}
 
 	public void testClearValue() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.addType("Vehicle");
 		Attribute vehiclePower = vehicle.addProperty("power");
 		Holder vehicle233 = vehicle.addValue(vehiclePower, 233);
 		Type car = vehicle.addSubType("Car");
 		assert car.getHolder(vehiclePower).equals(vehicle233) : car.getHolder(vehiclePower);
 		car.cancel(vehicle233);
 		car.clear(vehicle233);
 		assert car.getHolder(vehiclePower) == vehicle233 : car.getHolder(vehiclePower);
 	}
 
 	public void testClearValue2() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.addType("Vehicle");
 		Attribute vehiclePower = vehicle.addProperty("power");
 		Holder vehicle233 = vehicle.addValue(vehiclePower, 233);
 		Type car = vehicle.addSubType("Car");
 		assert car.getHolder(vehiclePower).equals(vehicle233) : car.getHolder(vehiclePower);
 		car.cancel(vehiclePower, Statics.STRUCTURAL);
 		car.clear(vehiclePower, Statics.STRUCTURAL);
 		assert car.getValue(vehiclePower) == null : car.getHolder(vehiclePower);
 	}
 
 	public void testClearAllValue() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.addType("Vehicle");
 		Attribute vehiclePower = vehicle.addProperty("power");
 		Holder vehicle233 = vehicle.addValue(vehiclePower, 233);
 		Type car = vehicle.addSubType("Car");
 		assert car.getHolder(vehiclePower).equals(vehicle233) : car.getHolder(vehiclePower);
 		car.cancelAll(vehicle233);
 		car.clearAll(vehicle233);
 		assert car.getHolder(vehiclePower) == vehicle233 : car.getHolder(vehiclePower);
 	}
 
 	public void testClearAllValue2() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.addType("Vehicle");
 		Attribute vehiclePower = vehicle.addProperty("power");
 		Holder vehicle233 = vehicle.addValue(vehiclePower, 233);
 		Type car = vehicle.addSubType("Car");
 		assert car.getHolder(vehiclePower).equals(vehicle233) : car.getHolder(vehiclePower);
 		car.cancelAll(vehiclePower, Statics.STRUCTURAL);
 		car.clearAll(vehiclePower, Statics.STRUCTURAL);
 		assert car.getValue(vehiclePower) == null : car.getHolder(vehiclePower);
 	}
 
 }
