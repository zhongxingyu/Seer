 package org.genericsystem.impl;
 
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericSystem;
 import org.genericsystem.core.Statics;
 import org.genericsystem.exception.SingularConstraintViolationException;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Link;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 import org.testng.annotations.Test;
 
 @Test
 public class SingularConstraintTest extends AbstractTest {
 
 	public void testSingularConstraint() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Relation metaRelation = cache.getMetaRelation();
 
 		metaRelation.enableSingularConstraint(Statics.BASE_POSITION);
 		assert metaRelation.isSingularConstraintEnabled(Statics.BASE_POSITION);
 		assert !metaRelation.isSingularConstraintEnabled(Statics.TARGET_POSITION);
 
 		metaRelation.enableSingularConstraint(Statics.TARGET_POSITION);
 		assert metaRelation.isSingularConstraintEnabled(Statics.BASE_POSITION);
 		assert metaRelation.isSingularConstraintEnabled(Statics.TARGET_POSITION);
 
 		metaRelation.disableSingularConstraint(Statics.BASE_POSITION);
 		assert !metaRelation.isSingularConstraintEnabled(Statics.BASE_POSITION);
 		assert metaRelation.isSingularConstraintEnabled(Statics.TARGET_POSITION);
 
 		metaRelation.enableSingularConstraint();
 		assert metaRelation.isSingularConstraintEnabled(Statics.BASE_POSITION);
 		assert metaRelation.isSingularConstraintEnabled(Statics.TARGET_POSITION);
 		assert metaRelation.isSingularConstraintEnabled();
 
 		metaRelation.disableSingularConstraint();
 		assert !metaRelation.isSingularConstraintEnabled(Statics.BASE_POSITION);
 		assert metaRelation.isSingularConstraintEnabled(Statics.TARGET_POSITION);
 		assert !metaRelation.isSingularConstraintEnabled();
 	}
 
 	public void testConstraintCheckOK() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		Attribute vehiclePower = vehicle.setAttribute("Power");
 		Attribute vehicleDriver = vehicle.setAttribute("Driver");
 		Generic myVehicle = vehicle.newInstance("myVehicle");
 
 		vehiclePower.enableSingularConstraint(0);
 		assert vehiclePower.isSingularConstraintEnabled(0);
 		myVehicle.setValue(vehiclePower, "5").remove();
 		myVehicle.setValue(vehiclePower, "6");
 		myVehicle.setValue(vehicleDriver, "JC");
 		cache.flush();
 	}
 
 	public void testConstraintCheckOK2() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		Type car = vehicle.newSubType("Car");
		Attribute vehiclePower = vehicle.setProperty("Power");
 		car.setValue(vehiclePower, 50);
 		assert vehicle.getValue(vehiclePower) == null;
 		vehicle.setValue(vehiclePower, 125);
 		assert car.getValue(vehiclePower).equals(50);
 	}
 
 	public void testConstraintCheckOK3() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		Type car = vehicle.newSubType("Car");
 		Attribute vehiclePower = vehicle.setProperty("Power");
 		vehicle.setValue(vehiclePower, 125);
 		car.setValue(vehiclePower, 50);
 		vehicle.setValue(vehiclePower, 250);
 		assert car.getValue(vehiclePower).equals(50);
 	}
 
 	public void testConstraintCheckKO() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		final Attribute vehiclePower = vehicle.setAttribute("Power");
 		final Generic myVehicle = vehicle.newInstance("myVehicle");
 		vehiclePower.enableSingularConstraint(0);
 		assert vehiclePower.isSingularConstraintEnabled(0);
 		Holder myVehiclePowerValue1 = myVehicle.setValue(vehiclePower, "5");
 		assert myVehicle.getHolders(vehiclePower).size() == 1;
 		assert myVehicle.getHolders(vehiclePower).get(0).equals(myVehiclePowerValue1);
 		Holder myVehiclePowerValue2 = myVehicle.setValue(vehiclePower, 2);
 		assert myVehicle.getHolders(vehiclePower).size() == 1;
 		assert myVehicle.getHolders(vehiclePower).get(0).equals(myVehiclePowerValue2);
 	}
 
 	public void testConsistency() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		final Attribute vehiclePower = vehicle.setAttribute("Power");
 		Generic myVehicle = vehicle.newInstance("myVehicle");
 		myVehicle.setValue(vehiclePower, 123);
 		myVehicle.setValue(vehiclePower, 50);
 		new RollbackCatcher() {
 
 			@Override
 			public void intercept() {
 				vehiclePower.enableSingularConstraint(Statics.BASE_POSITION);
 			}
 		}.assertIsCausedBy(SingularConstraintViolationException.class);
 	}
 
 	public void testRelationOK() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		Type dog = cache.newType("Dog");
 		final Relation runsOver = vehicle.setRelation("RunsOver", dog);
 		final Generic myBMW = vehicle.newInstance("myBMW");
 		final Generic yourDog = dog.newInstance("yourDog");
 		runsOver.enableSingularConstraint(0);
 		assert runsOver.isSingularConstraintEnabled(0);
 		Link runsOverLink1 = myBMW.setLink(runsOver, "myBMWRunsOverYourDog", yourDog);
 		assert myBMW.getLink(runsOver, yourDog).equals(runsOverLink1) : myBMW.getLink(runsOver, yourDog);
 		Link runsOverLink2 = myBMW.setLink(runsOver, "myBMWRunsOverAndOverOverYourDog", yourDog);
 		assert !runsOverLink1.isAlive();
 		assert myBMW.getLink(runsOver, yourDog).equals(runsOverLink2) : myBMW.getLink(runsOver, yourDog);
 	}
 
 	public void testRelationWithTwoSingular() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type human = cache.newType("Human");
 		Type vehicle = cache.newType("Vehicle");
 		final Relation humanDriveVehicle = human.setRelation("Drive", vehicle);
 		final Generic myck = human.newInstance("myck");
 		final Generic nico = human.newInstance("nico");
 		final Generic myckVehicle = vehicle.newInstance("myckVehicle");
 		final Generic nicoVehicle = vehicle.newInstance("nicoVehicle");
 		humanDriveVehicle.enableSingularConstraint(Statics.BASE_POSITION);
 		humanDriveVehicle.enableSingularConstraint(Statics.TARGET_POSITION);
 		assert humanDriveVehicle.isSingularConstraintEnabled(Statics.BASE_POSITION);
 		assert humanDriveVehicle.isSingularConstraintEnabled(Statics.TARGET_POSITION);
 		Link humanDriveVehicleLink1 = myck.setLink(humanDriveVehicle, "myckDrive", myckVehicle);
 		cache.flush();
 		Link humanDriveVehicleLink2 = myck.setLink(humanDriveVehicle, "myckDrive", nicoVehicle);
 		assert !humanDriveVehicleLink1.isAlive();
 		assert myck.getLink(humanDriveVehicle).equals(humanDriveVehicleLink2);
 		assert myck.getLink(humanDriveVehicle, nicoVehicle).equals(humanDriveVehicleLink2);
 		nico.setLink(humanDriveVehicle, "nicoDrive", myckVehicle);
 		assert humanDriveVehicle.isSingularConstraintEnabled(Statics.BASE_POSITION);
 		assert humanDriveVehicle.isSingularConstraintEnabled(Statics.TARGET_POSITION);
 	}
 
 	public void testDoubleRelationSimpleKO() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		Type dog = cache.newType("Dog");
 		final Relation runsOver = vehicle.setRelation("RunsOver", dog);
 		final Generic myBMW = vehicle.newInstance("myBMW");
 		final Generic yourDog = dog.newInstance("yourDog");
 
 		runsOver.enableSingularConstraint(1);
 		assert runsOver.isSingularConstraintEnabled(1);
 
 		myBMW.setLink(runsOver, "myBMWRunsOverYourDog", yourDog);
 		new RollbackCatcher() {
 			@Override
 			public void intercept() {
 				myBMW.setLink(runsOver, "myBMWRunsOverAndOverOverYourDog", yourDog);
 			}
 		}.assertIsCausedBy(SingularConstraintViolationException.class);
 	}
 
 	public void testDoubleRelationSimpleOK() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		Type dog = cache.newType("Dog");
 		final Relation runsOver = vehicle.setRelation("RunsOver", dog);
 		final Generic myBMW = vehicle.newInstance("myBMW");
 		final Generic yourDog = dog.newInstance("yourDog");
 		final Generic yourSecondDog = dog.newInstance("yourSecondDog");
 
 		runsOver.enableSingularConstraint(1);
 		assert runsOver.isSingularConstraintEnabled(1);
 
 		myBMW.setLink(runsOver, "myBMWRunsOverYourDog", yourDog);
 		myBMW.setLink(runsOver, "myBMWRunsOverAndOverOverYourDog", yourSecondDog);
 	}
 
 	public void testDoubleRelationOK() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		Type dog = cache.newType("Dog");
 		Type road = cache.newType("Road");
 		final Relation runsOver = vehicle.setRelation("RunsOver", dog, road);
 		final Generic myBMW = vehicle.newInstance("myBMW");
 		final Generic yourDog = dog.newInstance("yourDog");
 		final Generic yourSecondDog = dog.newInstance("yourSecondDog");
 		final Generic myRoad = road.newInstance("myRoad");
 
 		runsOver.enableSingularConstraint(1);
 		assert runsOver.isSingularConstraintEnabled(1);
 
 		myBMW.setLink(runsOver, "myBMWRunsOverYourDog", yourDog, myRoad);
 		myBMW.setLink(runsOver, "myBMWRunsOverAndOverOverYourDog", yourSecondDog, myRoad);
 	}
 
 	public void testDoubleRelationKO() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		Type dog = cache.newType("Dog");
 		Type road = cache.newType("Road");
 		final Relation runsOver = vehicle.setRelation("RunsOver", dog, road);
 		final Generic myBMW = vehicle.newInstance("myBMW");
 		final Generic yourDog = dog.newInstance("yourDog");
 		final Generic myRoad = road.newInstance("myRoad");
 		final Generic myRoad2 = road.newInstance("myRoad2");
 
 		runsOver.enableSingularConstraint(1);
 		assert runsOver.isSingularConstraintEnabled(1);
 
 		myBMW.setLink(runsOver, "myBMWRunsOverYourDog", yourDog, myRoad);
 		new RollbackCatcher() {
 			@Override
 			public void intercept() {
 				myBMW.setLink(runsOver, "myBMWRunsOverAndOverOverYourDog", yourDog, myRoad2);
 			}
 		}.assertIsCausedBy(SingularConstraintViolationException.class);
 	}
 
 	public void testDoubleRelationKO2() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicleType = cache.newType("Vehicle");
 		Type dogType = cache.newType("Dog");
 		Type roadType = cache.newType("Road");
 		final Relation runsOver = vehicleType.setRelation("RunsOver", dogType, roadType);
 		final Generic vehicle = vehicleType.newInstance("myBMW");
 		final Generic dog = dogType.newInstance("yourDog");
 		final Generic dog2 = dogType.newInstance("yourSecondDog");
 		final Generic road = roadType.newInstance("myRoad");
 
 		runsOver.enableSingularConstraint(2);
 		assert runsOver.isSingularConstraintEnabled(2);
 
 		vehicle.setLink(runsOver, "myBMWRunsOverYourDog", dog, road);
 		new RollbackCatcher() {
 			@Override
 			public void intercept() {
 				vehicle.setLink(runsOver, "myBMWRunsOverAndOverOverYourDog", dog2, road);
 			}
 		}.assertIsCausedBy(SingularConstraintViolationException.class);
 	}
 
 	// Rapportés depuis 2010 ----------------------------------------------------------------------
 
 	public void noMoreThanOneAttributePerEntity() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		final Generic myBmw = car.newInstance("myBmw");
 		final Attribute registration = car.setAttribute("registration");
 		registration.enableSingularConstraint();
 		Holder myBmwRegistration1 = myBmw.setValue(registration, "AB123CD");
 		Holder myBmwRegistration2 = myBmw.setValue(registration, "DC321BA");
 		assert myBmw.getHolders(registration).size() == 1;
 		assert myBmw.getHolders(registration).get(0).equals(myBmwRegistration2);
 		assert !myBmwRegistration1.isAlive();
 	}
 
 	// public void noMoreThanOneAttributePerEntity2() {
 	// final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 	// Type car = cache.newType("Car");
 	// Type frenchCar = car.newSubType( "frenchCar");
 	// Attribute registration = car.addAttribute( "registration");
 	// final Attribute frenchRegistration = frenchCar.addSubAttribute( registration, "French Registration");
 	// registration.enableSingularConstraint();
 	// final Generic myBmw = frenchCar.newInstance( "myBmw");
 	// myBmw.setValue( registration, "AB123CD");
 	// new RollbackCatcher() {
 	// @Override
 	// public void intercept() {
 	// myBmw.setValue( frenchRegistration, "JAIME75");
 	// }
 	// }.assertIsCausedBy(SingularConstraintViolationException.class);
 	// }
 
 	public void singularForTargetAxe() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("car");
 		Type color = cache.newType("color");
 		final Relation carColor = car.setRelation("carColor", color);
 		carColor.enableSingularConstraint(Statics.TARGET_POSITION);
 
 		Generic myBmw = car.newInstance("myBmw");
 		final Generic myAudi = car.newInstance("myAudi");
 
 		final Generic yellow = color.newInstance("yellow");
 		myBmw.setLink(carColor, "myBmwYellow", yellow);
 		assert carColor.isSingularConstraintEnabled(Statics.TARGET_POSITION);
 		new RollbackCatcher() {
 			@Override
 			public void intercept() {
 				myAudi.setLink(carColor, "myAudiYellow", yellow);
 			}
 		}.assertIsCausedBy(SingularConstraintViolationException.class);
 	}
 
 	public void singularForTargetAxe2() {
 		final Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		new RollbackCatcher() {
 			@Override
 			public void intercept() {
 				Type mother = cache.newType("mother");
 				Type children = cache.newType("children");
 				Relation myChildren = mother.setRelation("myChildren", children);
 				myChildren.enableSingularConstraint(Statics.TARGET_POSITION);
 
 				Generic mama1 = mother.newInstance("mama1");
 				Generic baby1 = children.newInstance("baby1");
 				mama1.setLink(myChildren, "mama1_baby1", baby1);
 				Generic baby2 = children.newInstance("baby2");
 				mama1.setLink(myChildren, "mama1_baby2", baby2);
 				mama1.setLink(myChildren, "test", baby2);
 			}
 		}.assertIsCausedBy(SingularConstraintViolationException.class);
 	}
 }
