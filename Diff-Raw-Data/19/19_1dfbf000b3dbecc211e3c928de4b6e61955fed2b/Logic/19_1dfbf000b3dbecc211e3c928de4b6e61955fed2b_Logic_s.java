 package main;
 import org.newdawn.slick.geom.Shape;
 import main.*;
 import org.newdawn.slick.*;
 
 import buffs.ABuff;
 import buffs.TutorZone;
 
 import states.Gameplay;
 public class Logic{
 
 	
 	//CHANGE HEALTH TO GPA
 	//F's FALL FROM SKY
 	//EACH F CAUSES THE LOSS OF .1 GPA
 	//Some A's will appear in the middle area and cause your GPA to go up!
 	//Tutor zones passively give GPA
 	//Extra credit
 	
 	public static double gpa;
 	public String gpaString;
 	public long timeBuffIsUsed;
 	public double buffA_amount;
 	public double f_amount;
 	public Logic()
 	{
 		gpa = 4.00;
 		buffA_amount = .64;
 		f_amount = .28;
 	}
 	
 	public void logic()
 	{
 		getGPA();
 		getGPA2();
 	}
 
 	public void getGPA()
 	{
 		if(collidesWithEnemy())
 		{
 			gpa -= f_amount;
 		}
 		
 	}
 	
 	public void getGPA2()
 	{
 		if(collidesWithABuff())
 		{
 			gpa += buffA_amount;
 		}
 		
 		}
 	
 	public void displayGPA(Graphics g)
 	{
 		getGPA();
 		if(gpa >= 4)
 		{
 			gpa = 4;
 			Gameplay.text.draw(g, Color.white, "GPA:" + Gameplay.df.format(gpa), 10, 30);
 		}
 		if(gpa <= 0)
 		{
 			gpa = 0;
			Gameplay.text.draw(g, Color.red, "FAILURE!", 10, 50);
 			
 		}
 		else
 		{
 			Gameplay.text.draw(g, Color.white, "GPA:" + Gameplay.df.format(gpa), 10, 30);
 		}
 	}
 	public void displayTime(Graphics g)
 	{
 		
 		Gameplay.text.draw(g, Color.white, "Time:" + Gameplay.s.getElapsedTimeSecs() + " seconds", 10, 50);
 	}
 	
 	public boolean collidesWithEnemy()
 	{
 		//Check if collides
 
 		for(int x=0;x<Gameplay.enemy.numberOfEnemies;x++)
 		{
 			if(Gameplay.player.boundingBox.intersects(Gameplay.enemy.boundingBox[x]))
 			{
 				Gameplay.hurtSound.play();
 				try {
 					Gameplay.enemy.removeEnemy(x);
 				} catch (SlickException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} 
 				return true;
 			}
 		}
 
 		return false;	
 	}
 
 
 	public boolean collidesWithABuff()
 	{
 		if(Gameplay.player.boundingBox.intersects(Gameplay.aBuff.boundingBox))
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean collidesWithTutorZone()
 	{
 		if(Gameplay.player.boundingBox.intersects(Gameplay.tutorZone.TutorZone))
 		{
 			return true;
 		}
 		return false;
 	}
 	public void changeGPA(double amount)
 	{
 		gpa += amount;
 	}
 			
 }
 
