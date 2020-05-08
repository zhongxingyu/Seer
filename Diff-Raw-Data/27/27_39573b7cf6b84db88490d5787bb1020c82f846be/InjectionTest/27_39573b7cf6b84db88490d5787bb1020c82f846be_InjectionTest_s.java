 package org.genericsystem.cdi;
 
 import javax.inject.Inject;

 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.annotation.value.StringValue;
 import org.genericsystem.cdi.GenericProvider.InjectClass;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 import org.testng.annotations.Test;
 
 @Test
 public class InjectionTest extends AbstractTest {
 
 	public void testInstanceIsConcreteWithValue() {
 		Type car = cache.addType("Car");
 		Type color = cache.addType("Color");
 		Relation carColor = car.setRelation("outsideColor", color);
 		Generic audi = car.addInstance("audi");
 		Generic red = color.addInstance("red");
 		audi.setLink(carColor, "audiRed", red);
 		assert audi.getTargets(carColor).contains(red) : audi.getTargets(carColor);
 	}
 
 	@Inject
 	@InjectClass(Vehicle.class)
	private Vehicle vehicle;
 
 	public void testAnnotType() {
 		vehicle.info();
 		assert cache.getType("Vehicle") != null;
 	}
 
 	@SystemGeneric
 	@StringValue("Vehicle")
 	public static class Vehicle extends GenericImpl {
 
 	}
 
 }
