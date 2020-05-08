 package edu.chl.codenameg.view.entity;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 import edu.chl.codenameg.model.Entity;
 import edu.chl.codenameg.view.EntityView;
 
 public class PlayerCharacterView implements EntityView {
 		
 	private Image img = null;
 	private SpriteSheet spriteSheet = null;
 	private Animation anm = null;
 	
 	public PlayerCharacterView(){
 			
 		try {
 			img = new Image ("res/base_0.png");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		spriteSheet = new SpriteSheet(img,64,64);	
 		
 		anm = new Animation();
 		for (int i=1; i<5; i++){
 			anm.addFrame( spriteSheet.getSprite(i, 1), 150);
 		}
 		anm.setLooping(true);
 	}
 	
 		
 
 	@Override
 	public void render(Entity ent, Graphics g) {
		g.drawImage(anm.getCurrentFrame(),ent.getPosition().getX(),ent.getPosition().getY());
		
 	}
 		
 	
 }
