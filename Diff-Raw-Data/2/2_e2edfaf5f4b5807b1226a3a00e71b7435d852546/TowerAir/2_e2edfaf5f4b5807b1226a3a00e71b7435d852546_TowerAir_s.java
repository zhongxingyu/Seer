 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 
 import javax.swing.ImageIcon;
 
 public class TowerAir extends Tower {
 
 	static final int cost = 500;
 	int baseDamage;
 
 	public TowerAir(Point2D.Double loc, int damage) {
 		super(Tower.tower_ids++, "Air Tower", 1000, loc, Frame.element.AIR,
 				true, cost, 1, damage);
 		setImage(new ImageIcon(getClass().getResource(
 				"/resources/images/towers/tower_air.png")));
 		baseDamage = damage;
 	}
 
 	public boolean upgrade(Player p)
 	{
 		boolean generic = super.upgrade(p);
 		
 		if(generic){
			this.speed -= 120;
 			this.damage = (int) (400 * (1/(1+Math.exp(-(.5 * this.level) + 3)))) + this.baseDamage;
 		}
 		
 		return generic;
 		
 	}
 
 	public void draw(Graphics2D g, int width) {
 		super.draw(g, width);
 	}
 }
