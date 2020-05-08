 package tiles.propagation;
 
 import game.Ray;
 import gui.ImageUtils;
 
 import java.awt.Color;
 
 import tiles.Tile;
 
 /**
  * Propagation component that filters the incoming ray color.
  */
 public class FilterPropagation extends PropagationComponent {
 
 	public FilterPropagation(Tile tile) {
 		super(tile);
 	}
 
 	public Ray process(Ray ray) {
 		setOrigin(ray);
 		if (!ray.getDirection().equalsIgnoreSense(getDirection())) {
 			Color filter = ImageUtils.filter(ray.getColor(), getColor());
 			ray.setColor(filter);
 			setRay(ray.getDirection(), ray);
		} else {
 			ray.stop();
 		}
 		return null;
 	}
 
 }
