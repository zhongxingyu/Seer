 package org.genericsystem.impl;
 
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericSystem;
 import org.genericsystem.core.RemoveStrategy;
 import org.genericsystem.exception.ReferentialIntegrityConstraintViolationException;
 import org.genericsystem.generic.Link;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 import org.testng.annotations.Test;
 
 @Test
 public class RemoveStrategyTest extends AbstractTest {
 
 	public void testRemoveNormal() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		Relation carColor = car.setRelation("CarColor", color);
 		carColor.enableSingularConstraint();
 
 		Generic myCar = car.newInstance("myCar");
 		Generic red = color.newInstance("Red");
 		final Link carRed = car.setLink(carColor, "carRed", red);
 
 		myCar.setLink(carColor, "myCarRed", red);
 
 		new RollbackCatcher() {
 			@Override
 			public void intercept() {
 				carRed.remove();
 			}
 
 		}.assertIsCausedBy(ReferentialIntegrityConstraintViolationException.class);
 	}
 
 	public void testRemoveForce() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		Relation carColor = car.setRelation("CarColor", color);
 		carColor.enableSingularConstraint();
 
 		Generic myCar = car.newInstance("myCar");
 		Generic red = color.newInstance("Red");
 		Link carRed = car.setLink(carColor, "carRed", red);
 
 		Link myCarRed = myCar.setLink(carColor, "myCarRed", red);
 
 		carRed.remove(RemoveStrategy.FORCE);
 		assert !myCarRed.isAlive();
 	}
 
 	public void testRemoveConserve() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		Relation carColor = car.setRelation("CarColor", color);
 		carColor.enableSingularConstraint();
 
 		Generic myCar = car.newInstance("myCar");
 		Generic red = color.newInstance("Red");
 		Link carRed = car.setLink(carColor, "carRed", red);
 
 		Link myCarRed = myCar.setLink(carColor, "myCarRed", red);
 		assert myCarRed.getSupers().contains(carRed);
 
 		carRed.remove(RemoveStrategy.CONSERVE);
 		myCarRed = myCar.getLink(carColor);
 		assert myCarRed.isAlive();
 		assert myCarRed.getSupers().contains(carColor);
 	}
 
 	public void testRemoveConserveNotSingular() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		Relation carColor = car.setRelation("CarColor", color);
 
 		Generic myCar = car.newInstance("myCar");
 		Generic red = color.newInstance("Red");
 		Link carRed = car.setLink(carColor, "carRed", red);
 
 		Link myCarRed = myCar.setLink(carColor, "myCarRed", red);
 
 		carRed.remove(RemoveStrategy.CONSERVE);
 		myCarRed = myCar.getLink(carColor);
 		assert myCarRed.isAlive();
 		assert myCarRed.getSupers().contains(carColor);
 	}
 
 	public void testRemoveProject() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		Relation carColor = car.setRelation("CarColor", color);
 		carColor.enableSingularConstraint();
 
 		Generic myCar = car.newInstance("myCar");
 		Generic red = color.newInstance("Red");
 		Link carRed = car.setLink(carColor, "carRed", red);
 
 		carRed.remove(RemoveStrategy.PROJECT);
 		assert !carRed.isAlive();
 		assert myCar.getTargets(carColor).contains(red) : myCar.getTargets(carColor);
 	}
 
 }
