 package org.genericsystem.impl;
 
 import java.util.Random;
 
 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.annotation.value.StringValue;
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.EngineImpl;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.core.GenericSystem;
 import org.testng.annotations.Test;
 
 @Test
 public class PersistenceTest2 extends AbstractTest {
 
	// @Test(invocationCount = 100, threadPoolSize = 1)
 	public void testDefaultConfiguration() {
 		String path = System.getenv("HOME") + "/test/snapshot_save" + new Random().nextInt();
 		Cache cache = GenericSystem.newCacheOnANewPersistentEngine(path).start();
 		GenericImpl metaAttribute = cache.getMetaAttribute();
 		cache.getEngine().close();
 		Cache cache2 = GenericSystem.newCacheOnANewPersistentEngine(path).start();
 		GenericImpl metaAttribute2 = cache2.getMetaAttribute();
 		assert cache.getEngine().getMetaAttribute() != cache2.getEngine().getMetaAttribute();
 		assert metaAttribute != metaAttribute2;
 		assert metaAttribute.getDesignTs() == metaAttribute2.getDesignTs() : metaAttribute.info() + metaAttribute2.info();
 	}
 
 	public void testDefaultConfiguration2() {
 		String path = System.getenv("HOME") + "/test/snapshot_save" + new Random().nextInt();
 		Cache cache = GenericSystem.newCacheOnANewPersistentEngine(path).start();
 		EngineImpl engine = cache.getEngine();
 		cache.getEngine().close();
 		Cache cache2 = GenericSystem.newCacheOnANewPersistentEngine(path).start();
 		EngineImpl engine2 = cache2.getEngine();
 		assert engine != engine2;
 		assert engine.getMetaAttribute() != engine2.getMetaAttribute();
 		assert engine.getDesignTs() == engine2.getDesignTs() : engine.info() + engine2.info();
 	}
 
 	public void testType() {
 		String path = System.getenv("HOME") + "/test/snapshot_save" + new Random().nextInt();
 		Cache cache = GenericSystem.newCacheOnANewPersistentEngine(path, Vehicle.class).start();
 		GenericImpl vehicle = ((EngineImpl) cache.getEngine()).find(Vehicle.class);
 		cache.getEngine().close();
 		GenericSystem.newCacheOnANewPersistentEngine(path);
 		GenericImpl vehicle2 = ((EngineImpl) cache.getEngine()).find(Vehicle.class);
 		assert vehicle.getDesignTs() == vehicle2.getDesignTs() : vehicle.info() + vehicle2.info();
 	}
 
 	public void testTypeWithValue() {
 		String path = System.getenv("HOME") + "/test/snapshot_save" + new Random().nextInt();
 		Cache cache = GenericSystem.newCacheOnANewPersistentEngine(path, Car.class).start();
 		GenericImpl car = ((EngineImpl) cache.getEngine()).find(Car.class);
 		cache.getEngine().close();
 		GenericSystem.newCacheOnANewPersistentEngine(path);
 		GenericImpl car2 = ((EngineImpl) cache.getEngine()).find(Car.class);
 		assert car.getDesignTs() == car2.getDesignTs() : car.info() + car2.info();
 	}
 
 	@SystemGeneric
 	public static class Vehicle {
 
 	}
 
 	@SystemGeneric
 	@StringValue("Car")
 	public static class Car {
 
 	}
 
 }
