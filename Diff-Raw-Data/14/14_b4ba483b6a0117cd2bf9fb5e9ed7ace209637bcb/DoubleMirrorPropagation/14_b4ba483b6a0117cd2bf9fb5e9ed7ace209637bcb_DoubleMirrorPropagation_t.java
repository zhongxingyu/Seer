 package tiles.propagation;
 
 import game.Ray;

import java.util.Stack;

 import misc.Direction;
import tiles.Tile;
 
 /**
  * Propagation component that reflects rays in every direction.
  */
 public class DoubleMirrorPropagation extends MirrorPropagation {
 
 	private static final long serialVersionUID = -3745026267399337972L;
 
 	public DoubleMirrorPropagation(Tile tile) {
 		super(tile);
 	}
 
	@Override
	public void process(Ray ray, Stack<Ray> bifurcations) {
 		setOrigin(ray);
 
 		Direction dTile = getDirection();
 		Direction dRay = ray.getDirection();
 
 		Direction mirror = mirrorDirection(dTile, dRay);
		if (mirror == null) {
 			mirror = mirrorDirection(dTile.getOpposite(), dRay);
 		}
 		setBeam(mirror, ray);
 		ray.setDirection(mirror);
 	}
 
 }
