 package org.genericsystem.impl;
 
 import java.util.Arrays;
 import java.util.Objects;
 
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.CacheImpl;
 import org.genericsystem.core.Engine;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.core.GenericSystem;
 import org.genericsystem.core.Snapshot;
 import org.genericsystem.core.Snapshot.Filter;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Link;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 import org.testng.annotations.Test;
 
 @Test
 public class FlushTest extends AbstractTest {
 
 	@Test
 	public void testFlush() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Engine engine = cache.getEngine();
 		Type human = cache.newType("Human");
 		cache.flush();
 		engine.close();
 
 		cache = engine.newCache();
 		assert engine.getInheritings().contains(human);
 	}
 
 	@Test
 	public void testNoFlush() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Engine engine = cache.getEngine();
 		Type animal = cache.newType("Animal");
 		Snapshot<Generic> snapshot = animal.getInheritings();
 		assert snapshot.isEmpty();
 		Type human = animal.newSubType("Human");
 		assert snapshot.size() == 1;
 		assert snapshot.contains(human);
 
 		engine.close();
 
 		cache = engine.newCache().start();
 		assert engine.getInheritings().filter(new Filter<Generic>() {
 
 			@Override
 			public boolean isSelected(Generic element) {
 				return Objects.equals(element.getValue(), "Animal");
 			}
 		}).isEmpty();
 	}
 
 	@Test
 	public void testPartialFlush() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Engine engine = cache.getEngine();
 		Type human = cache.newType("Human");
 		cache.flush();
 		Type car = cache.newType("Car");
 		engine.close();
 
 		cache = engine.newCache().start();
 		Snapshot<Generic> snapshot = engine.getInheritings();
 		assert snapshot.contains(human) : snapshot;
 		assert !snapshot.contains(car) : snapshot;
 	}
 
 	@Test
 	public void testMultipleCache() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Engine engine = cache.getEngine();
 		Type human = cache.newType("Human");
 		cache.flush();
 		Snapshot<Generic> snapshot = engine.getInheritings();
 		assert snapshot.contains(human) : snapshot;
 		// cache.deactivate();
 
 		cache = engine.newCache().start();
 		Type car = cache.newType("Car");
 		cache.flush();
 		engine.close();
 
 		cache = engine.newCache().start();
 		snapshot = engine.getInheritings();
 		assert snapshot.containsAll(Arrays.asList(human, car)) : snapshot;
 	}
 
 	@Test
 	public void testMultipleCache2() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Engine engine = cache.getEngine();
 		Type human = cache.newType("Human");
 		cache.flush();
 		Snapshot<Generic> snapshot = engine.getInheritings();
 		assert snapshot.contains(human) : snapshot;
 
 		cache = engine.newCache().start();
 		Type car = cache.newType("Car");
 		cache.flush();
 
 		cache = engine.newCache();
 		snapshot = engine.getInheritings();
 		assert snapshot.containsAll(Arrays.asList(human, car)) : snapshot;
 
 		engine.close();
 	}
 
 	@Test
 	public void testMultipleCache3() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Engine engine = cache.getEngine();
 		Type vehicle = cache.newType("Vehicle");
 
 		Cache cache2 = engine.newCache().start();
 		assert cache2.getType("Vehicle") == null;
 
 		cache.start().flush();
 
 		cache2.start();
 		((CacheImpl) cache2).pickNewTs();
 		assert cache2.getType("Vehicle").equals(vehicle);
 	}
 
 	public void testAutomatics() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		Relation carColor = car.setRelation("CarColor", color);
 		carColor.enableSingularConstraint();
 
 		Generic red = color.newInstance("Red");
 		Generic grey = color.newInstance("Grey");
 		car.setLink(carColor, "DefaultCarColor", red); // default color of car
 
 		final Generic bmw = car.newInstance("Bmw");
 		Generic mercedes = car.newInstance("Mercedes");
 		mercedes.setLink(carColor, "ColorOfMercedes", grey);
		assert red.getLinks(carColor).size() == 2;
 	}
 
 	public void testAutomaticsNotFlushedOK() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		Relation carColor = car.setRelation("CarColor", color);
 		carColor.enableSingularConstraint();
 		Attribute intensity = carColor.setAttribute("Intensity");
 
 		Generic red = color.newInstance("Red");
 		Generic grey = color.newInstance("Grey");
 		Link defaultCarColor = car.setLink(carColor, "DefaultCarColor", red);	// default color of car
 
 		final Generic bmw = car.newInstance("Bmw");
 		Generic mercedes = car.newInstance("Mercedes");
 		final Generic lada = car.newInstance("Lada");
 		mercedes.setLink(carColor, "ColorOfMercedes", grey);
 
 		red.getLink(carColor, lada).setValue(intensity, "60%");
 
 		/* Link beetween Lada and color is not the same as link between Car and color */
 		assert !Objects.equals(lada.getLink(carColor, red), defaultCarColor);
 
 		/* Two links: Bmw <-> Red; Lada <-> Red */
 		assert red.getLinks(carColor).size() == 2;
 
 		Snapshot<Link> links = red.getLinks(carColor);
 		@SuppressWarnings({ "unchecked", "rawtypes" })
 		Link redToBMW = links.filter(new Filter() {
 
 			@Override
 			public boolean isSelected(Object element) {
 				return ((Link) element).getComponents().contains(bmw);
 			}
 		}).get(0);
 		@SuppressWarnings({ "unchecked", "rawtypes" })
 		Link redToLada = links.filter(new Filter() {
 
 			@Override
 			public boolean isSelected(Object element) {
 				return ((Link) element).getComponents().contains(lada);
 			}
 		}).get(0);
 
 		/* Automatic link from red tyo BMW exists */
 		assert redToBMW != null;
 
 		/* Link from red to BMW is automatic */
 		assert ((GenericImpl) redToBMW).isAutomatic();
 
 		/* Link from red to BMW is not flushable */
 		assert !((GenericImpl) redToBMW).isFlushable();
 
 		/* Automatic link from red to Lada exists */
 		assert redToLada != null;
 
 		/* Link from red to Lada is automatic */
 		assert ((GenericImpl) redToLada).isAutomatic();
 
 		/* Link from red to Lada is flushable */
 		assert ((GenericImpl) redToLada).isFlushable();
 
 		cache.flush();
 		Cache cache2 = cache.getEngine().newCache().start();
 
 		/* Cache 2 contains our types */
 		assert cache2.getAllTypes().contains(color);
 
 		Relation carColor2 = cache2.getType("Car").getRelation("CarColor");
 		Link defColor = carColor2.getInstance("DefaultCarColor");
 
 		/* Automatic link between BMW and red color was not restored from cache */
 		assert cache2.getType("Car").getInstance("Bmw").getLinks(carColor2).contains(defColor);
 	}
 }
