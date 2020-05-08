 package pl.edu.pw.elka.sag.logic.highwaycode.rules.priority;
 
 import pl.edu.pw.elka.sag.logic.highwaycode.rules.*;
 import pl.edu.pw.elka.sag.ontology.concepts.*;
 import pl.edu.pw.elka.sag.util.*;
 
 public abstract class PriorityRuleBase implements IPriorityRule
 {
 	protected boolean isGoingStraight(Car car)
 	{
 		return DirectionUtilities.isStraight(car.getDirection(), car.getNextDirection());
 	}
 	
 	protected boolean isTurningRight(Car car)
 	{
 		return DirectionUtilities.isRightTurn(car.getDirection(), car.getNextDirection());
 	}
 	
 	protected boolean isTurningLeft(Car car)
 	{
 		return DirectionUtilities.isLeftTurn(car.getDirection(), car.getNextDirection());
 	}
 	
 	protected boolean isAcross(Car car, Car otherCar)
 	{
		return DirectionUtilities.isAcross(car.getDirection(), otherCar.getDirection());
 	}
 	
 	protected boolean isOnTheRight(Car car, Car otherCar)
 	{
		return DirectionUtilities.isOnTheRight(car.getDirection(), otherCar.getDirection());
 	}
 	
 	protected boolean isOnTheLeft(Car car, Car otherCar)
 	{
		return DirectionUtilities.isOnTheLeft(car.getDirection(), otherCar.getDirection());
 	}
 }
