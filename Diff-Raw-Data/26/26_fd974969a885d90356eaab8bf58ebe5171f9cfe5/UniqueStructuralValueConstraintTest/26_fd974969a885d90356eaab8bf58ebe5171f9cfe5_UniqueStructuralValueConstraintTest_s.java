 package org.genericsystem.impl;
 
 import java.util.Objects;
 
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.core.GenericSystem;
 import org.genericsystem.exception.UniqueValueConstraintViolationException;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Link;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 import org.testng.annotations.Test;
 
 @Test
 public class UniqueStructuralValueConstraintTest extends AbstractTest {
 
 	// Type
 
 	public void testTwoTypesWithSameNameOK() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car  = cache.newType("Car");
 		Type car2 = cache.newType("Car");
 		assert Objects.equals(car, car2);
 	}
 
 	public void testAttributeWithSameNameAsTypeOK() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car   = cache.newType("Car");
 		Type power = cache.newType("Power");
 		Attribute carPower = car.setAttribute("Power");
 		assert carPower.getSupers().contains(power);
 	}
 
 	public void testRelationWithSameNameAsTypeOK() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		final Type car = cache.newType("Car");
 		final Type plane = cache.newType("Plane");
 		Generic relation = car.setRelation("Plane", plane);
 		assert car.getAttribute("Plane") == relation;
 		assert cache.getEngine().getSubTypes("Plane").contains(plane);
 	}
 
 	// Attribute
 
 	public void testAttributeWithSameNameAsAttributeOK() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Attribute wheel = car.setAttribute("Wheels");
 		Attribute wheel2 = car.setAttribute("Wheels");
 		assert Objects.equals(wheel, wheel2);
 	}
 
 	public void testRelationWithSameNameAsAttributeOnSameGenericOK() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		final Type car = cache.newType("Car");
 		final Type plane = cache.newType("Plane");
 		Attribute wheelA = car.setAttribute("Wheels");
 		Relation wheelR = car.setRelation("Wheels", plane);
 		assert !Objects.equals(wheelA, wheelR);
 	}
 
 	public void testRelationWithSameNameAsAttributeOnDifferentGenericsOK() {
 		Cache cache       = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car     = cache.newType("Car");
 		Type plane       = cache.newType("Plane");
 		Relation powerR  = car.setRelation("Power", plane);
 		Type color      = cache.newType("Color");
 		Attribute powerA = color.setAttribute("Power");
 		assert !Objects.equals(powerA, powerR);
 	}
 
 	// Relation
 
 	public void testTwoRelationsWithSameNameOK() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		Relation carColor = car.setRelation("CarColor", color);
 		Relation carColor2 = car.setRelation("CarColor", color);
 		assert Objects.equals(carColor, carColor2);
 	}
 
 	// 2 types
 
 	public void testTwoTypesWithSameAttributeOK() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		final Type plane = cache.newType("Plane");
 		Attribute powerOnCar = car.setAttribute("Power");
 		Attribute powerOnPlane = plane.setAttribute("Power");
 		assert powerOnCar == car.getAttribute("Power");
 		assert powerOnPlane == plane.getAttribute("Power");
 		Type power = cache.newType("Power");
 		assert power.fastValueEquals(powerOnCar);
 		assert !powerOnCar.isAlive();
 		assert !powerOnPlane.isAlive();
 		assert ((GenericImpl) powerOnCar).reFind().inheritsFrom(power);
 		assert ((GenericImpl) powerOnCar).reFind().inheritsFrom(power);
 		assert ((GenericImpl) powerOnCar).reFind() == car.getAttribute("Power");
 		assert ((GenericImpl) powerOnPlane).reFind() == plane.getAttribute("Power");
 	}
 
 	public void testTypeAndItsSubtypeKO() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		final Type car = cache.newType("Car");
 		assert ((GenericImpl) car).isUniqueValueConstraintEnabled();
 		assert car == car.newSubType("Car");
 	}
 
 	public void testOneTypeAndOneAttributeWithSameNameOK() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		final Type car = cache.newType("Car");
 		car.setAttribute("Power");
 		Generic power = cache.newType("Power");
 		assert car.getAttribute("Power").getSupers().get(1) == power;
 	}
 
 	public void testTwoTypesWithSameRelationOK() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		final Type plane = cache.newType("Plane");
 		final Type color = cache.newType("Color");
 		Relation cr1 = car.setRelation("ColorRelation", color);
 		Relation cr2 = plane.setRelation("ColorRelation", color);
 		assert cr1 != cr2;
 	}
 
 	// Instance
 
 	public void testTwoInstancesWithSameNameOK() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Generic myCar = car.newInstance("myCar");
 		Generic myCar2 = car.newInstance("myCar");
 		assert Objects.equals(myCar, myCar2);
 	}
 
 	public void testInstanceWithSameNameAsTypeKO() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		final Type car = cache.newType("Car");
 
 		new RollbackCatcher() {
 			@Override
 			public void intercept() {
 				car.newInstance("Car");
 			}
 		}.assertIsCausedBy(UniqueValueConstraintViolationException.class);
 	}
 
 	// Holder
 
 	public void testTwoHoldersWithSameValueOK() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Attribute power = car.setAttribute("Power");
 		Generic myCar = car.newInstance("myCar");
 		Holder myPower1 = myCar.setValue(power, 20000);
 		Holder myPower2 = myCar.setValue(power, 20000);
 		assert myPower1 == myPower2;
 		assert Objects.equals(myPower1, myPower2);
 	}
 
 	public void testHolderWithSameValueAsTypeOK() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Attribute power = car.setAttribute("Power");
 		Generic myCar = car.newInstance("myCar");
 		Holder myPower = myCar.setValue(power, "Car");
 		assert !Objects.equals(myPower, car);
 	}
 
 	// Link
 
 	public void testTwoLinksWithSameNameOK() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		Relation carColor = car.setRelation("CarColor", color);
 		Generic myCar = car.newInstance("myCar");
 		Generic myColor = color.newInstance("red");
 		Link myVehicleRed1 = myCar.setLink(carColor, "myVehicleRed", myColor);
 		Link myVehicleRed2 = myCar.setLink(carColor, "myVehicleRed", myColor);
 		assert Objects.equals(myVehicleRed1, myVehicleRed2);
 	}
 
 	public void testLinkWithSameNameAsAttributeOK() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car          = cache.newType("Car");
 		Type color        = cache.newType("Color");
 		Attribute power   = car.setAttribute("Power");
 		Relation carColor = car.setRelation("CarColor", color);
 		Generic myCar     = car.newInstance("myCar");
 		Generic myColor   = color.newInstance("red");
 		Link myVehicleRed = myCar.setLink(carColor, "Power", myColor);
 		assert !Objects.equals(myVehicleRed, power);
 	}
 }
