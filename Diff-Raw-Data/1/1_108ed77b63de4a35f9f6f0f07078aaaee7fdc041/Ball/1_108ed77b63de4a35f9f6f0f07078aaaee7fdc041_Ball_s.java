 package beothorn.labs.core;
 
 import static playn.core.PlayN.assets;
 import static playn.core.PlayN.graphics;
 import static playn.core.PlayN.log;
 import playn.core.GroupLayer;
 import playn.core.Image;
 import playn.core.ImageLayer;
 import playn.core.ResourceCallback;
import pythagoras.f.Point;
 
 public class Ball {
 
 	public static String IMAGE = "images/soccerBall.png";
 	private ImageLayer layer;
 	private float angle;
 	private float x;
 	private float y;
 	private float radius;
 
 	public Ball(final GroupLayer ballLayer, final float x, final float y) {
 		Image image = assets().getImage(IMAGE);
 		layer = graphics().createImageLayer(image);
 
 		image.addCallback(new ResourceCallback<Image>() {
 			@Override
 			public void done(Image image) {
 				setOriginAtCenter(image);
 				setPositionAndRotation(x, y, 0);
 				ballLayer.add(layer);
 			}
 
 			private void setOriginAtCenter(Image image) {
 				radius = image.width()/2f;
 				layer.setOrigin(radius, radius);
 			}
 
 			@Override
 			public void error(Throwable err) {
 				log().error("Error loading image!", err);
 			}
 		});
 	}
 
 	public void setPositionAndRotation(final float x, final float y,float angle) {
 		this.x = x;
 		this.y = y;
 		this.angle = angle;
 		layer.setTranslation(x, y);
 	}
 
 	public void update(float delta) {
 		layer.setRotation(angle);
 		layer.setTranslation(x, y);
 	}
 }
