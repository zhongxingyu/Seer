 
 import java.awt.Image;
 
 
 
 public class Entity extends GameComponents{
 
 	
 	
 	private Image img;
 	private int life;
 	private double grad;
 	private Bullet bullet;
 	
 	
 	
 	
 	public void update(boolean a[],double b) {
 		
 		
 		if(a[0]){setY_Point(getY_Point()-1);}
 		if(a[1]){setY_Point(getY_Point()+1);}
 		if(a[2]){setX_Point(getX_Point()-1);}
 		if(a[3]){setX_Point(getX_Point()+1);}
 		
 		
 		
		grad=grad+b;
 		double i = ( getX_Point()+25-Math.cos( grad ) * 25);
 		double j=(getY_Point()+25-Math.sin( grad ) * 25 );
 		
 		double m = ( getX_Point()+25+Math.cos( grad ) * 25);
 		double n=(getY_Point()+25+Math.sin( grad ) * 25 );
 		
 		int dirX=(int)(i-m);
 		int dirY=(int)(j-n);
 		
 		if(a[4]){
 			long time = System.currentTimeMillis();
			if(FightClub.getLasttime()+2000<time){
 				FightClub.setLasttime(time);
				bullet= new Bullet(20,20,dirX,dirY,(int)i+2,(int)j+2);
				System.out.println("Works over here!" + bullet.getY_Point());
 				FightClub.getBullets().add(bullet);
 			}
 			
 		}
 		
 //	System.out.println(i + " "+ j);	
 //	System.out.println(m + " "+ n);	
 //	System.out.println(dirX+" "+ dirY);
 	}
 	
 	
 	
 	
 
 	/**
 	 * @return the color
 	 */
 	public Image getImg() {
 		return img;
 	}
 
 
 
 
 
 	/**
 	 * @param color the color to set
 	 */
 	public void setImg(Image img) {
 		this.img = img;
 	}
 
 
 
 	/**
 	 * @return the life
 	 */
 	public int getLife() {
 		return life;
 	}
 
 
 
 	/**
 	 * @param life the life to set
 	 */
 	public void setLife(int life) {
 		this.life = life;
 	}
 
 
 
 	/**
 	 * @return the grad
 	 */
 	public double getGrad() {
 		return grad;
 	}
 
 
 
 	/**
 	 * @param grad the grad to set
 	 */
 	public void setGrad(double grad) {
 		this.grad = grad;
 	}
 	
 	
 	
 	
 	
 	
 }
