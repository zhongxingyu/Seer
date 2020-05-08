 package com.picotech.lightrunnerlibgdx;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Polygon;
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * A light beam on the screen. It can be either incoming or outgoing.
  * 
  * @author Daniel Fang
  * 
  */
 public class LightBeam {
 	public enum Type {
 		INCOMING, OUTGOING
 	}
 
 	Type type;
 	int strength;
 	int convexBeamSpread = 10;
 	Vector2 origin;
 	Vector2 dst;
 	float angle;
 	float beamLength = 1300;
 	int width = 20;
 
 	Color lightColor = Color.YELLOW;
 	/**
 	 * An array of 6 elements that represents the 3 vertices of the LightBeam. <br>
 	 * For outgoing beams, values 2&3 are the bottom point, values 4&5 are the
 	 * top point.
 	 */
 	float[] beamVertices = new float[6];
 	Polygon beamPolygon = new Polygon(beamVertices);
 	/**
 	 * An ArrayList of Vector2's that represent the vertices of the LightBeam.
 	 */
 	ArrayList<Vector2> vectorPolygon = new ArrayList<Vector2>();
 
 	boolean polygonInstantiated = false;
 	boolean isPrism = false;
 
 	/**
 	 * Represents the 6 points that (with the top and bottom points) create the
 	 * prism.
 	 */
 	float[] prismVertices = new float[12];
 
 	/**
 	 * Default constructor with the destination being (0,0).
 	 * 
 	 * @param newOrigin
 	 *            the origin of the beam
 	 * @param newW
 	 *            the width of the beam
 	 * @param newT
 	 *            the type of the beam
 	 */
 	public LightBeam(Vector2 newOrigin, int newW, Type newT) {
 		this(newOrigin, new Vector2(0, 0), newW, newT);
 		// origin = newOrigin;
 		// dst = new Vector2(0, 0);
 		// for (int i = 0; i < 3; i++) {
 		// vectorPolygon.add(new Vector2(0, 0));
 		// }
 		// setWidth(newW);
 	}
 
 	/**
 	 * Constructor that sets both origin and destination vectors.
 	 * 
 	 * @param newOrigin
 	 *            the new origin vector2
 	 * @param newDst
 	 *            the new destination vector2
 	 * @param newW
 	 *            the width of the beam
 	 * @param newT
 	 *            the type of the beam
 	 */
 	public LightBeam(Vector2 newOrigin, Vector2 newDst, int newW, Type newT) {
 		origin = newOrigin;
 		dst = newDst;
 		for (int i = 0; i < 3; i++) {
 			vectorPolygon.add(new Vector2(0, 0));
 		}
 		setWidth(newW);
 		type = newT;
 	}
 
 	/**
 	 * Calculates the orientation, position, and size of the beam if it is an
 	 * incoming one. The polygon points are predetermined to set the faux
 	 * 'origin' of the beam to be "width" pixels wide.
 	 * 
 	 * @param newDst
 	 *            the destination of the incoming beam
 	 * @param isMenu
 	 *            whether it is currently Menu screen or not.
 	 * @param player
 	 *            the current player object
 	 */
 	public void updateIncomingBeam(Vector2 newDst, boolean isMenu, Player player) {
 		dst = newDst;
 		calculateAngle();
 
 		// This is the algorithm for incoming beams coming from the top.
 		// The x value of the first point of the polygon is offset left by
 		// 10 at the top edge.
 		beamVertices[0] = origin.x - width / 2;
 		beamVertices[1] = origin.y;
 
 		// The x value of the second point is offset right by 10 to create a
 		// triangle.
 		beamVertices[2] = origin.x + width / 2;
 		beamVertices[3] = origin.y;
 
 		if (!isMenu) {
 			setVertices(player);
 		}
 		beamVertices[4] = dst.x;
 		beamVertices[5] = dst.y;
 
 		beamPolygon = new Polygon(beamVertices);
 
 	}
 
 	public void setVertices(Player player) {
 		if (GameScreen.scheme == GameScreen.LightScheme.TOP) {
 			beamVertices[0] = origin.x - width / 2;
 			beamVertices[1] = origin.y;
 
 			beamVertices[2] = origin.x + width / 2;
 			beamVertices[3] = origin.y;
 		} else if (GameScreen.scheme == GameScreen.LightScheme.LEFT) {
 			origin = new Vector2(player.position.x, player.position.y
 					+ player.bounds.height / 2);
 		}
 		if (GameScreen.scheme == GameScreen.LightScheme.RIGHT
 				|| GameScreen.scheme == GameScreen.LightScheme.LEFT) {
 			beamVertices[0] = origin.x;
 			beamVertices[1] = origin.y + width / 2;
 
 			beamVertices[2] = origin.x;
 			beamVertices[3] = origin.y - width / 2;
 		} else if (GameScreen.scheme == GameScreen.LightScheme.BOTTOM) {
 			beamVertices[0] = origin.x - width / 2;
 			beamVertices[1] = origin.y;
 
 			// The x value of the second point is offset right by 10 to
 			// create a
 			// triangle.
 			beamVertices[2] = origin.x + width / 2;
 			beamVertices[3] = origin.y;
 		}
 	}
 
 	/**
 	 * Calculates the orientation, location, and size of the beam if it is an
 	 * outgoing beam.
 	 * 
 	 * @param sourceBeam
 	 *            the source beam
 	 * @param mirrorAngle
 	 *            the angle of the mirror
 	 * @param type
 	 *            the type of the mirror
 	 * @param lightBehavior
 	 *            the type of the mirror, to determine the behavior of the beam
 	 */
 	public void updateOutgoingBeam(LightBeam sourceBeam, float mirrorAngle,
 			Mirror.Type type, int beamNumber) {
 		origin = sourceBeam.dst;
 
 		// calculates the angle of all reflecting beams depending on mirror type
 		if (type == Mirror.Type.FLAT) {
 			angle = (2 * mirrorAngle - sourceBeam.angle)
 					* MathUtils.degreesToRadians;
 		} else if (type == Mirror.Type.FOCUS) {
 			angle = mirrorAngle * MathUtils.degreesToRadians;
 		} else if (type == Mirror.Type.CONVEX) {
 			angle = (mirrorAngle + (convexBeamSpread * (beamNumber - 2)))
 					* MathUtils.degreesToRadians;
 			// width = 300;
 			// angle = mirrorAngle * MathUtils.degreesToRadians;
 		} else if (type == Mirror.Type.PRISM){
 			isPrism = true;
 		}
 
 		// trigonometry to calculate where the outgoing beam ends, which varies
 		// with the beamLength
 		dst.x = origin.x + (float) (Math.cos(angle) * beamLength);
 		dst.y = origin.y + (float) (Math.sin(angle) * beamLength);
 
 		beamVertices[0] = origin.x;
 		beamVertices[1] = origin.y;
 
 		beamVertices[2] = dst.x;
 		beamVertices[3] = dst.y - width / 2;
 
 		beamVertices[4] = dst.x;
 		beamVertices[5] = dst.y + width / 2;
 
 		if (isPrism) {
 			setPrismVertices();
 		}
 
 		vectorPolygon.set(0, new Vector2(beamVertices[0], beamVertices[1]));
 		vectorPolygon.set(1, new Vector2(beamVertices[2], beamVertices[3]));
 		vectorPolygon.set(2, new Vector2(beamVertices[4], beamVertices[5]));
 
 		beamPolygon = new Polygon(beamVertices);
 		// boundingRect = beamPolygon.getBoundingRectangle();
 	}
 
 	/**
 	 * Sets the prismVertices array to the appropriate values for the prism
 	 * vertices.
 	 */
 	private void setPrismVertices() {
 		for (int i = 0; i < 6; i++) {
 			// x-values are all the same as the destination Vector.
 			prismVertices[2 * i] = dst.x;
 			// y-values are 1/7th the width up from the base.
 			prismVertices[2 * i + 1] = dst.y - width / 2 + (i + 1) * width / 7;
 		}
 	}
 
 	public void setWidth(int newWidth) {
 		width = newWidth;
 	}
 
 	/**
 	 * Draws the LightBeam given a ShapeRenderer.
 	 * 
 	 * @param sr
 	 *            the ShapeRenderer that will draw the LightBeam; should not be
 	 *            begun
 	 */
 	public void draw(ShapeRenderer sr) {
 		sr.begin(ShapeType.FilledTriangle);
 		if (isPrism && type == Type.OUTGOING) {
 			// First triangle is red. Points 2&3 from beamVertices, 0&1 from
 			// prismVertices.
 			sr.setColor(Color.RED);
 			sr.filledTriangle(beamVertices[0], beamVertices[1],
 					beamVertices[2], beamVertices[3], prismVertices[0],
 					prismVertices[1]);
 			// Orange.
 			// prismVertices: Points 0&1 and 2&3
 			sr.setColor(Color.ORANGE);
 			sr.filledTriangle(beamVertices[0], beamVertices[1],
 					prismVertices[0], prismVertices[1], prismVertices[2],
 					prismVertices[3]);
 			// Yellow.
 			// prismVertices: Points 2&3 and 4&5
 			sr.setColor(Color.YELLOW);
 			sr.filledTriangle(beamVertices[0], beamVertices[1],
 					prismVertices[2], prismVertices[3], prismVertices[4],
 					prismVertices[5]);
 			// Green.
 			// prismVertices: Points 4&5 and 6&7
 			sr.setColor(Color.GREEN);
 			sr.filledTriangle(beamVertices[0], beamVertices[1],
 					prismVertices[4], prismVertices[5], prismVertices[6],
 					prismVertices[7]);
 			// Cyan.
 			// prismVertices: Points 6&7 and 8&9
 			sr.setColor(Color.CYAN);
 			sr.filledTriangle(beamVertices[0], beamVertices[1],
 					prismVertices[6], prismVertices[7], prismVertices[8],
 					prismVertices[9]);
 			// Blue.
 			// prismVertices: Points 8&9 and 10&11
 			sr.setColor(Color.BLUE);
 			sr.filledTriangle(beamVertices[0], beamVertices[1],
 					prismVertices[8], prismVertices[9], prismVertices[10],
 					prismVertices[11]);
 			// Violet.
 			// prismVertices: Points 10&11
 			// beamVertices: Points 4&5
 			sr.setColor(new Color(143, 0, 255, 1));
 			sr.filledTriangle(beamVertices[0], beamVertices[1],
 					prismVertices[10], prismVertices[11], beamVertices[4],
 					beamVertices[5]);
 
 		} else {
 			// Regular light.
 			lightColor.a = .1f;
 			sr.setColor(lightColor);
 			sr.filledTriangle(beamVertices[0], beamVertices[1],
 					beamVertices[2], beamVertices[3], beamVertices[4],
 					beamVertices[5]);
 		}
 		sr.end();
 
 	}
 
 	/**
 	 * Calculates the angle of the incoming beam, independent of mirror angle.
 	 */
 	public void calculateAngle() {
 		angle = (float) (Math.atan((dst.y - origin.y) / (dst.x - origin.x)) * 180 / Math.PI);
 	}
 
 }
