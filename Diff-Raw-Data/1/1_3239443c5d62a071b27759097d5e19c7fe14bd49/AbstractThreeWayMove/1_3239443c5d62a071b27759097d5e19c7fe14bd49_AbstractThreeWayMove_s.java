 package abstracts;
 
 import interfaces.Move;
 import interfaces.TriTowers;
 import interfaces.Tower;
 import entities.Plate;
 import exceptions.IllegalActionException;
 
 public abstract class AbstractThreeWayMove implements Move {
 
 	public static enum TowerEnum {
 		CENTER, LEFT, RIGHT;
 
 		/**
 		 * Private method to get the the plate of one of the towers provided in
 		 * the give TriTowers
 		 * 
 		 * @param t
 		 *            a set of towers to retrieve the plate from.
 		 * @return a copy of the top plate on the tower specified by this
 		 */
 		private Plate getPlateToMove(TriTowers t) {
 			switch (this) {
 			case LEFT:
 				return t.getLeftTower().peek();
 			case CENTER:
 				return t.getCenterTower().peek();
 			case RIGHT:
 				return t.getRightTower().peek();
 			default:
 				return null;
 			}
 		}
 
 		/**
 		 * Returns a reference to the Tower specified by this, contained in t
 		 * 
 		 * @param t
 		 *            the towers to look for the plate specified by this.
 		 * @return a tower reference specified by this.
 		 */
 		private Tower getTower(TriTowers t) {
 			switch (this) {
 			case LEFT:
 				return t.getLeftTower();
 			case CENTER:
 				return t.getCenterTower();
 			case RIGHT:
 				return t.getRightTower();
 			default:
 				return null;
 			}
 		}
 	}
 
 	protected TowerEnum from;
 	protected TowerEnum to;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see interfaces.Move#move(interfaces.TriTowers)
 	 */
 	public void move(TriTowers towers) throws IllegalActionException {
 		Plate p = from.getPlateToMove(towers);
 		if (p != null) {
 			if (to.getTower(towers).pushOnto(p)) {
 				from.getTower(towers).pop();
 			}
 		}
 		throw new IllegalActionException("Cannot perform the move from " + from
 				+ " to " + to + ".");
 	}
 }
