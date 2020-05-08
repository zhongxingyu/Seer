 package model.world.props;
 
 import model.geometrical.Circle;
 import model.geometrical.Position;
 import model.geometrical.Rectangle;
 import model.other.Animation;
 
 /**
  * A factory for props.
  * 
  * @author Calleberg
  *
  */
 public class PropFactory {
 
 	/*
 	 * Global animations, should be synchronised all over the world.
 	 */
 	private static final Animation WATER = new Animation(new int[]{30,31,32,33,34,35,36,37,38,39}, 200, true);
 	private static final Animation TRAFFIC_LIGHT_N = new Animation(new int[]{43,43,43,44,45,45,45,44}, 2500, true);
 	private static final Animation TRAFFIC_LIGHT_E = new Animation(new int[]{52,52,52,51,50,50,50,51}, 2500, true);
 	private static final Animation TRAFFIC_LIGHT_S = new Animation(new int[]{40,40,40,41,42,42,42,41}, 2500, true);
 	private static final Animation TRAFFIC_LIGHT_W = new Animation(new int[]{53,53,53,54,55,55,55,54}, 2500, true);
 	
 	/**
 	 * Creates a new prop with the specified position.
 	 * @param pos the position of the new prop.
 	 * @param propNbr the number of the prop to create.
 	 * @return <code>null</code> if the propNbr was invalid.
 	 */
 	public static Prop getProp(Position pos, int propNbr) {
 		switch(propNbr) {
 		case 0:		return new Prop(new Rectangle(pos.getX(), pos.getY(), 1f, 1f), propNbr); //test1
 		case 1:		return new Prop(new Rectangle(pos.getX(), pos.getY(), 0.5f, 0.5f), propNbr); //test2
 		case 2:		return new Prop(new Rectangle(pos.getX() + 0.5f, pos.getY(), 0.5f, 1f), propNbr); //test3
 		case 10: case 11: case 12: case 13: 
 		case 20: case 21: case 22: case 23: case 24: case 25: case 26: case 27:
 			return new Prop(null, propNbr); //water edges
 		case 30: case 31: case 32: case 33: case 34: case 35: case 36: case 37: case 38: case 39: 				
 			return new AnimatedProp(null, WATER); //animated water
 		case 40: case 41: case 42:
			return new AnimatedProp(new Rectangle(pos.getX() + 0.175f, pos.getY() + 0.15f, 0.65f, 0.525f), TRAFFIC_LIGHT_S);
 		case 43: case 44: case 45:
			return new AnimatedProp(new Rectangle(pos.getX() + 0.175f, pos.getY() + 0.325f, 0.65f, 0.525f), TRAFFIC_LIGHT_N);
 		case 50: case 51: case 52:
			return new AnimatedProp(new Rectangle(pos.getX() + 0.15f, pos.getY() + 0.175f, 0.525f, 0.65f), TRAFFIC_LIGHT_E);
 		case 53: case 54: case 55:
			return new AnimatedProp(new Rectangle(pos.getX() + 0.325f, pos.getY() + 0.175f, 0.525f, 0.65f), TRAFFIC_LIGHT_W);
 		case 70: 
 			return new Prop(new Circle(pos.getX() + 0.5f, pos.getY() + 0.5f, 1f, 8), propNbr);
 		case 90: case 91: case 92: case 93: case 94: case 95: case 96: case 97:
 			return new Prop(null, propNbr);
 		case 140: //Chair
 			return new Prop(new Rectangle(pos.getX() + 0.325f, pos.getY() + 0.175f, 0.575f, 0.65f), propNbr);
 		case 141: //Chair
 			return new Prop(new Rectangle(pos.getX() + 0.175f, pos.getY() + 0.325f, 0.65f, 0.575f), propNbr);
 		case 142: //Chair
 			return new Prop(new Rectangle(pos.getX() + 0.1f, pos.getY() + 0.175f, 0.575f, 0.65f), propNbr);
 		case 143: //Chair
 			return new Prop(new Rectangle(pos.getX() + 0.175f, pos.getY() + 0.1f, 0.65f, 0.575f), propNbr);
		case 144: case 145:
			return new Prop(new Rectangle(pos.getX(), pos.getY() + 0.325f, 1f, 0.675f), propNbr);
		case 158: case 159:
			return new Prop(new Rectangle(pos.getX(), pos.getY(), 1f, 0.675f), propNbr);
		case 162: case 165:
			return new Prop(new Rectangle(pos.getX(), pos.getY(), 0.675f, 1f), propNbr);
		case 166: case 170:
			return new Prop(new Rectangle(pos.getX() + 0.325f, pos.getY(), 0.675f, 1f), propNbr);
 		default:	
 			return new Prop(new Rectangle(pos.getX(), pos.getY(), 1f, 1f), propNbr);
 		}
 	}
 }
