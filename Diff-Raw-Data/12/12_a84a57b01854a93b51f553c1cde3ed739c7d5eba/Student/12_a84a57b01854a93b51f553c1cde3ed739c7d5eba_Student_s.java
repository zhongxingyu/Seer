 /**
  * This class stores information about a Student
  *
  * @author	Jamie of the Javateerz
  * @serial
  */
 
 package org.javateerz.ParkViewProtector;
 
 import java.io.*;
 import java.util.ArrayList;
 
 import org.javateerz.EasyGL.GLRect;
 import org.newdawn.slick.Color;
 
 public class Student extends Character implements Serializable
 {
 	public final static int GANG=0;
 	public final static double GANG_AGGRO=0.80;
 	public final static int GOTH=1;
 	public final static double GOTH_AGGRO=0.30;
 	public final static int BAND=2;
 	public final static double BAND_AGGRO=0.10;
 	
 	private static final long serialVersionUID = 2L;
 	
 	private String type	= "default";
 	
 	private boolean aggro	= false;
 	
 	private char gender;
 	private int chargeRegenRate=0;
 	private static final int CHARGE_REGEN=1; 
 	private static final int CHARGE_REGEN_RATE=80;
 	
 	/**
 	 * Create a new student
 	 * 
 	 * @param g			Instance of Game
 	 * @param hp		HP of student
 	 * @param maxHp		Max HP of student
 	 * @param spd		Speed of student
 	 * @param gender	Gender of student
 	 */
 	public Student(Game g, int x, int y, int hp, int maxHp, double spd, char gender,
 			int type)
 	{
 		super(g, x, y, hp, maxHp, spd);
 		
 		this.gender		= gender;
 		
 		// generate a random charge
 		setHp((int) (Math.random() * getMaxHp() + 1));
 		
 		// FIXME: this is just for testing; determining type should probably be handled in
 		// the driver
 		switch(type)
 		{
 			case Student.GANG:
 				this.type = "gangster";
 				if(Math.random()<GANG_AGGRO)
 					aggro=true;
 				break;
 			case Student.GOTH:
 				this.type = "goth";
 				if(Math.random()<GOTH_AGGRO)
 					aggro=true;
 				break;
 			case Student.BAND:
 				this.type = "band";
 				break;
 			default:
 				this.type = "default";
 			if(Math.random()<BAND_AGGRO)
 				aggro=true;
 			break;
 		}
 		
 		updateSprite();
 	}
 	
 	public void step(Game game)
 	{
 		if(getHp() > 0)
 		{
 			attemptCoupling();
 		}
 		
 		// random movement
 		if(!isStunned() && !isAttacking())
 			if(aggro && inRange(game.getPlayer(),200))
 			{
 				moveToward(game.getPlayer(),10);
 				attack();
 			}
 			else
 				moveRandom();
 		
 		// decrement the hit delay
 		recover();
 		
 		if(!isStunned())
 			recharge();
 		
 		handleAttacks();
 	}
 
 	/**
 	 * Updates the sprite
 	 */
 	protected void updateSprite()
 	{
 		sprite			= DataStore.INSTANCE.getSprite("student/" + type + "_" + gender + ".png");
 	}
 	
 	/**
 	 * @return The type of the student (used for the sprite)
 	 */
 	public String getType()
 	{
 		return type;
 	}
 	
 	/**
 	 * @return The gender of the student
 	 */
 	public char getGender()
 	{
 		return gender;
 	}
 	
 	public void setAggro(boolean agg)
 	{
 		aggro=agg;
 	}
 	
 	/**
 	 * Recharge students
 	 */
 	public void recharge()
 	{
 		chargeRegenRate++;
 		if(chargeRegenRate>=CHARGE_REGEN_RATE)
 		{
 			chargeRegenRate=0;
 			if(getHp()<getMaxHp())
 				adjustHp(CHARGE_REGEN);
 		}
 	}
 	
 	/**
 	 * Attempt to couple with another student
 	 * 
 	 * @param currStudent
 	 * @return Whether or not the coupling succeeded
 	 */
 	public boolean attemptCoupling()
 	{
 		int charge, student1, student2;
 		Student testStudent;
 		ArrayList<Student> students=game.getStudents();
 		ArrayList<Couple> couples=game.getCouples();
 		
 		int i						= students.indexOf(this);
 		
 		// prevent coupling if we intersect a wall
 		if(!canMove(getBounds()))
 		{
 			return false;
 		}
 		
 		for(int j = 0; j < students.size(); j++)
 		{
 			testStudent				= students.get(j);
 			
 			// don't do anything if it's us
 			if(i == j)
 			{
 				continue;
 			}
 			
 			// no same-sex couples (for now at least)
 			if(getGender() == testStudent.getGender())
 			{
 				continue;
 			}
 
 			// did we hit another student with a charge?
 			if(getNewBounds(game.MOVE_SPEED).intersects(students.get(j).getBounds())
 					&& testStudent.getHp() > 0)
 			{
 				charge				= getHp() +
 										testStudent.getHp();
 				
 				if(Math.random() * game.COUPLE_CHANCE_MULTIPLIER < charge)
 				{
 					couples.add(new Couple(game, this, testStudent));
 					
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
 						game.driver.error("Something went wrong when deleting someone :O", false);
 					}
 					
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Handle any attacks on a student
 	 * 
 	 * @param obj
 	 * @return Whether or not the attack hit the student
 	 */
 	public boolean handleAttacks()
 	{
 		Attack attack;
 		ArrayList<Attack> attacks=game.getAttacks();
 		
 		// hit by an attack?
 		for(int j = 0; j < attacks.size(); j++)
 		{
 			attack		= attacks.get(j);
 			
 			//The student takes damage in this if loop
 			if(attack.getBounds().intersects(getBounds()) && isVulnerable() &&
 					attack.isStudent())
 			{
 				game.hitFX((int)(getBounds().getCenterX()-getBounds().getWidth()/4),
 						(int)(getBounds().getCenterY()-getBounds().getHeight()/4));
 				
 				if(attack.getStatus()==Status.STUN && !isStunned())
 				{
 					setStunFrames(attack.getStatusDuration());
 				}
 				
 				if(!attack.isAoE())
 				{
 					attacks.remove(j);
 				}
 				
 				// FIXME: should be variable depending on strength
 				if(getHp()>-10)
 					adjustHp(-attack.getDamage()/2);
 				
 				if(getHp()<0)
 				{
 					setAggro(false);
 					if(getHp() <= 0 && bin.items.size() > 0)
 						{
 							while(bin.items.size() > 0)
 							{
 								game.getItems().add(bin.items.get(0));
 								bin.dropItem(bin.items.get(0));
 							}
 						}
 				}
 				//System.out.println("Student took "+ attack.getDamage()/2 + ", now has "+ getCharge());
 				setInvulFrames(attack.getHitDelay());
 				
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public void attack()
 	{
 		ArrayList<Attack> attacks=game.getAttacks();
 		Attack attack;
 		
 		String		name="attack";
 		int			damage=0,
 					tp=0,
 					type=0,
 					speed=0,
 					duration=0,
 					reuse=duration,
 					stillTime=0,
 					hits=1,
 					hitDelay=duration,
 					status=0,
 					statusLength=0;
 		boolean 	isStudent=false,
 					AoE=false;
 		
 		name="honk";
 		damage=1;
 		tp=10;
 		type=Type.FRONT;
 		speed=0;
 		duration=30;
 		reuse=100;
 		stillTime=30;
 		hits=1;
 		hitDelay=duration/hits;
 		status=status;
 		statusLength=statusLength;
 		isStudent=isStudent;
 		AoE=true;
 		
 		attack=new Attack(game,this.getBounds().getCenterX(), this.getBounds().getCenterY(), speed, this.getDirection(), name, isStudent, AoE, damage, tp, duration, type, status, statusLength, stillTime, hits, hitDelay, reuse);
 		if(inRange(game.getPlayer(),50) && isAgain())
 		{
 			setAttackFrames(attack.getStillTime());
 			attack.switchXY();
 			attacks.add(attack);
 			
 			try
 			{
 				ParkViewProtector.playSound(attack.getName()+".wav");
 			}
 			catch(Exception e)
 			{
 				System.out.println("The attack has no sound.");
 			}
 			
 			// set delay
 			setAgainFrames(attack.getReuse());
 		}
 	}
 	
 	public void showCharge()
 	{
 		GLRect rect				= new GLRect((int) x, (int) y, (int) getBounds().getWidth(),
 				(int) getBounds().getHeight());
 		rect.setColor(new Color(ParkViewProtector.COLOR_BG_1.getRed(),
 				ParkViewProtector.COLOR_BG_1.getGreen(),
 				ParkViewProtector.COLOR_BG_1.getBlue(), 5));
 		rect.draw();
 
 		Bar chargeBar = new Bar(ParkViewProtector.STATS_BAR_HP,(int)(getBounds().getWidth()), (double)getHp()/getMaxHp());
 		chargeBar.draw((int)x,(int)y);
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
