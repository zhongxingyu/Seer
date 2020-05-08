 package edu.csupomona.kyra.component.render.ai;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class AntiZombieRender extends AIRender {
 	public AntiZombieRender(String id) throws SlickException {
 		super(id);
		int[] moveTween = {200, 200, 200, 200};
		Image[] moveRight = {
 				new Image("img/anti-enemy-move-right_001.png"),
 				new Image("img/anti-enemy-move-right_002.png"),
 				new Image("img/anti-enemy-move-right_001.png"),
 				new Image("img/anti-enemy-move-right_003.png")
 		};
		Image[] moveLeft = {
 				new Image("img/anti-enemy-move-left_001.png"),
 				new Image("img/anti-enemy-move-left_002.png"),
 				new Image("img/anti-enemy-move-left_001.png"),
 				new Image("img/anti-enemy-move-left_003.png")
 		};
		setMoveRight(new Animation(moveRight, moveTween, false));
		setMoveLeft(new Animation(moveLeft, moveTween, false));
 		
		int[] deathTween = {500,175};
		Image[] deathRight = {
				new Image("img/anti-enemy-death-right-1.png"),
				new Image("img/anti-enemy-death-right-2.png")
		};
		Image[] deathLeft = {
				new Image("img/anti-enemy-death-left-1.png"),
				new Image("img/amti-enemy-death-left-2.png")
		};
		setDeathRight(new Animation(deathRight, deathTween, false));
		setDeathLeft(new Animation(deathLeft, deathTween, false));
 		
		setSprite(super.moveLeft);
 	}
 }
