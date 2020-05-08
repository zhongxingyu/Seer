 package tetrix.entities.rotations;
 
 import java.security.InvalidParameterException;
 import java.util.Map;
 
 import tetrix.entities.Direction;
import tetrix.entities.shapes.Block;
 import tetrix.entities.shapes.Shape;
 import tetrix.entities.shapes.ShapeType;
 
 /**
  * Defines a RotationSystem abstraction.
  * @author Dustin Biser
  *
  */
 public abstract class RotationSystem {
 
 	protected Map<ShapeType, Map<RotationTransition, TranslationResponse>> shapeRotationResponses;
 	
 	/**
 	 * Method responsible for rotating a {@link Shape} in a particular {@link
 	 * Direction}. Calling this method with a given <code>Shape</code> parameter
 	 * should have the side-effect of applying various transformations to each
	 * {@link Block} of the  <code>Shape</code> so as to simulate a rotation in the
 	 * given <code>Direction</code>.  The <code>Direction</code> argument supplied
 	 * to this method must be one of <code>LEFT</code> or <code>RIGHT</code>,
 	 * otherwise an {@link InvalidParameterException} is thrown.
 	 * @param shape - the Shape to be rotated.
 	 * @param direction - the Direction of the rotation, either LEFT or RIGHT.
 	 */
 	public abstract void rotate(Shape shape, Direction direction) throws InvalidParameterException;
 	
 }
