 package com.unexpected.ant.model.entity;
 
 import com.unexpected.ant.model.Cell;
 import com.unexpected.ant.model.Direction;
 import com.unexpected.ant.model.EntityVisitor;
 import com.unexpected.ant.model.EntityVisitorAdapter;
 
 /**
  * This class represents an ant in the game.
  */
 public class Ant extends MovingEntity {
 	private boolean hasFood = false;
 
 	/**
 	 * Constructor
 	 *
 	 * @param facingDirection The original facing direction of the ant
 	 */
 	public Ant(Direction facingDirection) {
 		super(facingDirection);
 	}
 
 	/**
 	 * Default constructor with North facing direction
 	 */
 	public Ant() {
 		super();
 	}
 
 	@Override
 	public void acceptVisitor(EntityVisitor visitor) {
 		visitor.visit(this);
 	}
 
 	@Override
 	public void action(long tickCount) {
         eat();
 		if (tickCount % 10 == 0) {
 			move();
             createSmell();
 		}
 	}
 
 	/**
 	 * The ant eats if it's hungry
 	 */
 	public void eat() {
 		if (!hasFood) {
 			getVisitedByEntitiesOnSameCells(new EatVisitor());
 		}
 	}
 
 
 	public double rateCell(Cell cell, Direction direction) {
 		int directionValue = direction.getRelativeDirectionTo(getFacingDirection()).value();
 		double weight = Math.abs(directionValue - Direction.values().length / 2) / 3.0;
 		SmellVisitor smellVisitor = new SmellVisitor();
 		cell.visitEntities(smellVisitor);
 		return weight * (1 + smellVisitor.getSmell());
 	}
 
 	public AntSmell createSmell() {
 		AntSmell antSmell = new AntSmell(this);
 		getCell().addEntity(antSmell);
 		return antSmell;
 	}
 
 	/**
 	 * This class is for eating food
 	 */
 	protected class EatVisitor extends EntityVisitorAdapter {
 		@Override
 		public void visit(Food food) {
 			if (!Ant.this.hasFood) {
 				food.remove();
 				Ant.this.hasFood = true;
 			}
 		}
 	}
 
 	class SmellVisitor extends com.unexpected.ant.model.entity.SmellVisitor {
 
 		@Override
 		public void visit(FoodSmell foodSmell) {
 			smell += foodSmell.getIntensity() * 2;
 		}
 
 		@Override
 		public void visit(AntSmell antSmell) {
 			smell += antSmell.getIntensity();
 		}
 	}
 }
