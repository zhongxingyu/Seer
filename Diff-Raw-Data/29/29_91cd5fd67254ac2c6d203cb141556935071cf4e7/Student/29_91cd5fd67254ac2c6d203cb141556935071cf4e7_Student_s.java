 /**
  * This class stores information about a Student
  *
  * @author	Jamie of the Javateerz
  * @serial
  */
 
 package org.javateerz.ParkViewProtector;
 
 import java.awt.Graphics;
 import java.io.*;
 
 public class Student extends Character implements Serializable
 {
 	private static final long serialVersionUID = 2L;
 	
 	private String type	= "default";
 	
 	private char gender;
 	private int charge;
 	
 	/**
 	 * Create a new student
 	 * 
 	 * @param hp		HP of student
 	 * @param maxHp		Max HP of student
 	 * @param spd		Speed of student
 	 * @param gender	Gender of student
 	 */
 	public Student(int x, int y, int hp, int maxHp, double spd, char gender)
 	{
 		super(x, y, hp, maxHp, spd);
 		
 		this.gender		= gender;
 		
 		// generate a random charge
 		this.charge		= (int) (Math.random() * 10 + 1);
 		
 		// FIXME: this is just for testing; determining type should probably be handled in
 		// the driver
 		if(Math.random() < 0.4)
 		{
 			type = "goth";
 		}
 		else {
 			type = "gangster";
 		}
 		
 		updateSprite();
 	}
 	
 	/**
 	 * Updates the sprite
 	 */
 	protected void updateSprite()
 	{
 		sprite			= DataStore.INSTANCE.getSprite("student/" + type + "_" + gender + ".png");
 	}
 	
 	/**
 	 * Returns the gender of the student
 	 * 
 	 * @return
 	 */
 	public char getGender()
 	{
 		return gender;
 	}
 	
 	/**
 	 * Returns the charge of the student
 	 * 
 	 * @return
 	 */
 	public int getCharge()
 	{
 		return charge;
 	}
 	
 	/**
 	 * Increases the charge of the student by the specified amount
 	 * 
 	 * @param amt Amount to add to charge
 	 */
 	public void adjustCharge(int amt)
 	{
 		charge			   += amt;
 	}
 	
 	public void step(Game game)
 	{
 		// random movement
 		game.moveRandom(this);
 		
 		// decrement the hit delay
 		decrementHitDelay(1);
 		
 		game.recharge(this);
 		
 		// attempt coupling
 		if(charge > 0)
 		{
 			game.attemptCoupling(this);
 		}
 		
 
 		// did we hit another student with a charge?
 		/*if(currStudent.getBounds().intersects(students.get(j).getBounds())
 				&& students.get(j).getCharge() > 0)
 		{
 				charge				= currStudent.getCharge() +
 									students.get(j).getCharge()+1;
 			
 			if(Math.random() * COUPLE_CHANCE_MULTIPLIER < charge)
 			{
 				couples.add(new Cupple(currStudent, students.get(j)));
 				
 				
 				student1			= i;
 				student2			= j;
 				
 				if(student2 > student1)
 				{
 					student2--;
 				}
 				
 				try
 				{
 					students.remove(student1);
 					students.remove(student2);
 				}
 				catch(Exception e)
 				{
 					System.out.println("Something went wrong when deleting someone :O");
 				}
 				break;
 			}
 		}*/
 		
 		game.handleAttacks(this);
 	}
 	
 	public void attack()
 	{
 		
 	}
 	
 	public void showCharge(Graphics g)
 	{
 		g.drawRect((int) x, (int) y, (int) getBounds().getWidth(),
 				(int) getBounds().getHeight());
 	}
 	
 	public void showChargeBar(Graphics g)
 	{
		Bar chargeBar = new Bar(ParkViewProtector.STATS_BAR_HP,(int)(getBounds().getWidth()),charge*.01);
 		chargeBar.draw(g,(int)x,(int)y);
 	}
 	
 	private void readObject(ObjectInputStream os) throws ClassNotFoundException, IOException
 	{
 		os.defaultReadObject();
 		
 		validateState();
 	}
 	
 	private void writeObject(ObjectOutputStream os) throws IOException
 	{
 		os.defaultWriteObject();
 	}
 }
