 package tiles.propagation;
 
import tiles.Tile;
 import game.Ray;
 import misc.Direction;
 
 /**
  * Propagation component that reflects rays in every direction.
  */
 public class DoubleMirrorPropagation extends MirrorPropagation {
 
 	private static final long serialVersionUID = -3745026267399337972L;
 
 	public DoubleMirrorPropagation(Tile tile) {
 		super(tile);
 	}
 
	public void process(Ray ray) {
 		setOrigin(ray);
 
 		Direction dTile = getDirection();
 		Direction dRay = ray.getDirection();
 
 		Direction mirror = mirrorDirection(dTile, dRay);
		if (mirror != null) {
 			mirror = mirrorDirection(dTile.getOpposite(), dRay);
 		}
 		setBeam(mirror, ray);
 		ray.setDirection(mirror);
 	}
 
 }
