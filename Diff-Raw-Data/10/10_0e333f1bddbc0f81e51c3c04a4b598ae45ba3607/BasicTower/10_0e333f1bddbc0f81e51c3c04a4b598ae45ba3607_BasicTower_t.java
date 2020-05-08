 package towers;
 
 import images.ImageHelper;
 
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 
 
 
 
 public class BasicTower extends AbstractTower {
    
    private static final BufferedImage image = ImageHelper.makeImage("towers", "basic.png");
    
    public BasicTower() {
       this(new Point());
    }
    
    private BasicTower(Point p) {
      super(p, "Basic", 40, 100, 5, 10, 50, 25, image);
    }
    
    @Override
    public Tower constructNew() {
       return new BasicTower(getCentre());
    }
 
 }
