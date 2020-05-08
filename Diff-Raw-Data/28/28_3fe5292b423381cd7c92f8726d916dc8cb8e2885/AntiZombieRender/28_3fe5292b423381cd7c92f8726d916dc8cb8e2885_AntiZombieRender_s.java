 package edu.csupomona.kyra.component.render.ai;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class AntiZombieRender extends AIRender {
 	public AntiZombieRender(String id) throws SlickException {
 		super(id);
		Image[] bERightmovement = {
 				new Image("img/anti-enemy-move-right_001.png"),
 				new Image("img/anti-enemy-move-right_002.png"),
 				new Image("img/anti-enemy-move-right_001.png"),
 				new Image("img/anti-enemy-move-right_003.png")
 		};
		Image[] bELeftmovement = {
 				new Image("img/anti-enemy-move-left_001.png"),
 				new Image("img/anti-enemy-move-left_002.png"),
 				new Image("img/anti-enemy-move-left_001.png"),
 				new Image("img/anti-enemy-move-left_003.png")
 		};
		int[] movementDuration = {200, 200, 200, 200};
 		
		animations = new Animation[2];
		animations[0] = new Animation(bELeftmovement, movementDuration, false);
		animations[1] = new Animation(bERightmovement, movementDuration, false);
 		
		super.setAnimations(animations);
 	}
 }
