 /**
  * Park View Protractor
  * 
  * @author Jason of Javateerz
  *
  * The jawesomeest charater in the hole game
  *
  */
 
 package org.javateerz.ParkViewProtector;
 
 public class Minshoe extends Staff
 {
 	public static final String CHAR_NAME		= "Dr. 'Minshoe'";
 	
 	private static final double SPEED= 1.0;
 	private static final int MAX_TP = 0;
 	private static final int MAX_HP = 0;
 	private static final long serialVersionUID = 3L;
 	
 	public Minshoe(Game g, int x, int y, int hp, int tp)
 	{
 		super(CHAR_NAME, g, x, y, hp, MAX_HP, SPEED, tp, MAX_TP);
 		updateSprite();
 	}
 
 	/*protected void updateSprite()
 	{
 		sprite = DataStore.INSTANCE.getSprite("staff/minshoe.png");
 	}*/
 	
 	public Attack getAttack(int i)
 	{
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
 		/*
 		 * FORMAT
 		 * 		name=name;
 				damage=damage;
 				tp=tp;
 				type=type;
 				speed=speed;
 				duration=duration;
 				reuse=reuse;
 				stillTime=stillTime;
 				hits=hits;
 				hitDelay=duration/hits;
 				status=status;
 				statusLength=statusLength;
 				isStudent=isStudent;
 				AoE=AoE;
 		 */
 		switch(i)
 		{
 			case 0:
 				name="tardy";
 				damage=20;
 				tp=tp;
 				type=Type.FRONT;
 				speed=0;
 				duration=1000;
 				reuse=duration;
 				stillTime=50;
 				hits=1;
 				hitDelay=duration;
 				status=status;
 				statusLength=statusLength;
 				isStudent=true;
 				AoE=AoE;
 				break;
 			case 1:
 				name="detention";
 				damage=damage;
 				tp=tp;
 				type=type;
 				speed=speed;
 				duration=duration;
 				reuse=reuse;
 				stillTime=stillTime;
 				hits=hits;
 				hitDelay=duration/hits;
 				status=status;
 				statusLength=statusLength;
 				isStudent=isStudent;
 				AoE=AoE;
 				break;
 			case 2:
 				name="announcement";
 				damage=damage;
 				tp=tp;
 				type=type;
 				speed=speed;
 				duration=duration;
 				reuse=reuse;
 				stillTime=stillTime;
 				hits=hits;
 				hitDelay=duration/hits;
 				status=status;
 				statusLength=statusLength;
 				isStudent=isStudent;
 				AoE=AoE;
 				break;
 		}
		attack=new Attack(x, y, speed, this.getDirection(), name, isStudent, AoE, damage, tp, duration, type, status, statusLength, stillTime, hits, hitDelay, reuse);
 		return attack;
 	}
 }
