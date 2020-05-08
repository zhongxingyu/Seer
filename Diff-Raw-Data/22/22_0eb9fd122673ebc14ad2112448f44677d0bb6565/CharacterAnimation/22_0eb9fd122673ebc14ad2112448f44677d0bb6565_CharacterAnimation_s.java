 package com.pieno.jeu;
 
 import com.pieno.jeu.Animation;
 import com.pieno.jeu.Server2D.Skill;
 
 import java.awt.Image;
 
 import javax.swing.ImageIcon;
 
 public class CharacterAnimation {
 	String name, map;
 	int x, y, classe, life = 1, maxLife = 1, mana = 1, maxMana = 1, blinkCount = 0, currentSkill = -1, skillx=0, skilly=0, lastUpdate = 0, currentAnim = 0;
	boolean invincible = false, blink = false, alive = true;
 	Animation skillAnimation, a;
 	
 	public CharacterAnimation(){
 	}
 	
 	public void setClasse(int classe){
 		this.classe = classe;
 	}
 	
 	public int getRealX(){
 		return x;
 	}
 	
 	public int getRealY(){
 		return y;
 	}
 	
 	public int getX(){
 		if(currentSkill != -1 && skillAnimation != null) return x+skillx;
 		return x;
 	}
 	
 	public int getY(){
 		if(currentSkill != -1 && skillAnimation != null) return y+skilly;
 		return y;
 	}
 	
 	public int getWidth(){
 		if(currentSkill != -1) return 120;
 		return getAnimation().getImage().getWidth(null);
 	}
 	public int getHeight(){
 		if(currentSkill != -1) return 200;
 		return getAnimation().getImage().getHeight(null);
 	}
 	
 	public void setSkill(int skill, boolean facingLeft)
 	{
 		Animation a = new Animation();
 		Image[] attackL = new Image[10], attackR = new Image[10];
 		skillx=0;skilly=0;
 		switch (skill)
 		{
 		case -1:
 			a = null;
 			break;
 		case Skill.Smash:
 			if(facingLeft){
 			attackL[0] = newImage("/smashL.png");
 			attackL[1] = newImage("/smashL2.png");
 			attackL[2] = newImage("/smashL3.png");
 			attackL[3] = newImage("/smashL4.png");
 			a.addScene(attackL[0], 100);
 			a.addScene(attackL[1], 100);
 			a.addScene(attackL[2], 100);
 			a.addScene(attackL[3], 150);
 			skillx = -160;
 			} else {
 			attackR[0] = newImage("/smashR.png");
 			attackR[1] = newImage("/smashR2.png");
 			attackR[2] = newImage("/smashR3.png");
 			attackR[3] = newImage("/smashR4.png");
 			a.addScene(attackR[0], 100);
 			a.addScene(attackR[1], 100);
 			a.addScene(attackR[2], 100);
 			a.addScene(attackR[3], 150);
 			}
 			break;
 		case Skill.ATTACK:
 			if(facingLeft){
 			attackL[0] = newImage("/attackL.png");
 			attackL[1] = newImage("/attackL2.png");
 			attackL[2] = newImage("/attackL3.png");
 			attackL[3] = newImage("/attackL4.png");
 			a.addScene(attackL[0], 100);
 			a.addScene(attackL[1], 100);
 			a.addScene(attackL[2], 100);
 			a.addScene(attackL[3], 150);
 			skillx = - 80;
 			} else {
 			attackR[0] = newImage("/attackR.png");
 			attackR[1] = newImage("/attackR2.png");
 			attackR[2] = newImage("/attackR3.png");
 			attackR[3] = newImage("/attackR4.png");
 			a.addScene(attackR[0], 100);
 			a.addScene(attackR[1], 100);
 			a.addScene(attackR[2], 100);
 			a.addScene(attackR[3], 150);
 			}
 			break;
 		case Skill.MultiHit:
 			if(facingLeft){
 			attackL[0] = newImage("/attackL.png");
 			attackL[1] = newImage("/attackL2.png");
 			attackL[2] = newImage("/attackL3.png");
 			attackL[3] = newImage("/attackL4.png");
 			a.addScene(attackL[0], 42);
 			a.addScene(attackL[1], 42);
 			a.addScene(attackL[2], 42);
 			a.addScene(attackL[3], 42);
 			skillx=-80;
 			} else {
 			attackR[0] = newImage("/attackR.png");
 			attackR[1] = newImage("/attackR2.png");
 			attackR[2] = newImage("/attackR3.png");
 			attackR[3] = newImage("/attackR4.png");
 			a.addScene(attackR[0], 42);
 			a.addScene(attackR[1], 42);
 			a.addScene(attackR[2], 42);
 			a.addScene(attackR[3], 42);
 			}
 			break;
 		case Skill.DoubleArrow:
 		case Skill.Arrow:
 		case Skill.ExplosiveArrow:
 			if(facingLeft){
 			Image walkleft1 = newImage("/walkleft1.png");
 			a.addScene(walkleft1, 400);
 			} else {
 			Image walkright1 = newImage("/walkright1.png");
 			a.addScene(walkright1, 400);
 			}
 			break;
 		case Skill.FireBall:
 		case Skill.EnergyBall:
 		case Skill.Explosion:
 			if(facingLeft){
 			Image wizStandL = newImage("/bobwalkwizzardL1.png");
 			a.addScene(wizStandL, 400);
 			} else {
 			Image wizStandR = newImage("/bobwalkwizzardR1.png");
 			a.addScene(wizStandR, 400);
 			}
 			break;
 		}
		if(a!=null)
 		a.start();
 		skillAnimation = a;
 		currentSkill = skill;
 	}
 	
 	public void setAnimation(Animation a, int animation){
 		this.a = new Animation(a);
 		currentAnim = animation;
 		this.a.start();
 	}
 	
 	
 	public void update(long timePassed){
 		if(getAnimation()!=null)
 		getAnimation().update(timePassed);
 		lastUpdate+=timePassed;
		if(lastUpdate > 200){
 			alive = false;
 		}
 	}
 	
 	public Animation getAnimation(){
 		if(currentSkill != -1 && skillAnimation != null){
 			return skillAnimation;
 		} else {
 			return a;
 		}
 		
 	}
 	
 	public Image newImage(String source) {
 		return new ImageIcon(getClass().getResource("/character"+source)).getImage();
 	}
 }
