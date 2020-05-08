 package net.xemnias.client;
 
 import org.newdawn.slick.Image;
 
 public class AnimationList 
 {
 	public static Animation playerStandingRight;
 	public static Animation playerStandingLeft;
 	public static Animation playerRunningRight;
 	
 	public static void init()
 	{
 		playerStandingRight = new Animation(new org.newdawn.slick.SpriteSheet(CommunityGame.loader.getAnimationByName("playerStandingRight.png"), 32,64), 150);
 		playerStandingLeft = new Animation(new org.newdawn.slick.SpriteSheet(CommunityGame.loader.getAnimationByName("playerStandingLeft.png"), 32,64), 150);
 		playerRunningRight = new Animation(playerRrunningRightSprite(), 150);
 	}
 	
 	private static Image[] playerRrunningRightSprite()
 	{
 		Image[] img = new Image[8];
 		
		img[0] = CommunityGame.loader.getAnimationByName("playerRunningRight.png").getSubImage(0, 0, 32, 64);
		img[1] = CommunityGame.loader.getAnimationByName("playerRunningRight.png").getSubImage(32, 0, 32, 64);
		img[2] = CommunityGame.loader.getAnimationByName("playerRunningRight.png").getSubImage(64, 0, 46, 64);
		img[3] = CommunityGame.loader.getAnimationByName("playerRunningRight.png").getSubImage(110, 0, 40, 64);
		img[4] = CommunityGame.loader.getAnimationByName("playerRunningRight.png").getSubImage(150, 0, 32, 64);
		img[5] = CommunityGame.loader.getAnimationByName("playerRunningRight.png").getSubImage(182, 0, 32, 64);
		img[6] = CommunityGame.loader.getAnimationByName("playerRunningRight.png").getSubImage(214, 0, 38, 64);
		img[7] = CommunityGame.loader.getAnimationByName("playerRunningRight.png").getSubImage(252, 0, 44, 64);
 		
 		return img;
 	}
 }
