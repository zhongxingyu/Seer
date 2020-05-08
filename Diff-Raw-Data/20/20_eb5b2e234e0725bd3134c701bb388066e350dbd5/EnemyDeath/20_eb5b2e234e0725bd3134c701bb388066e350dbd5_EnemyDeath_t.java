 
 import java.awt.*;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author John Michael
  */
 public class EnemyDeath extends Entity
 {
 	double alpha = 1.0;
 	public EnemyDeath(Vector location, Sprite sprite)
 	{
 		setCenter(location);
 		this.sprite = sprite;
 	}
 
 	@Override
 	public void logic() {
 		alpha -= 0.05;
 		if(alpha <= 0)
 			Content.nmeDeathMng.remove(this);
 	}
 
 	@Override
 	public void paint(Graphics2D gIn) {
 		gIn.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER).derive((float)alpha));;
 		
		if(sprite != null)
			sprite.draw(gIn, getCenter());
 		
 		gIn.setComposite(AlphaComposite.SrcOver);
 	}
 
 	@Override
 	public void dispose() {
 	}
 	
 	
 	
 }
