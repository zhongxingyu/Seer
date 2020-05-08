 package org.blockout.world.entity;
 
 /**
  * Instances of this class represent zombies.
  * 
  * @author Marc-Christian Schulze
  * 
  */
 public class Zombie extends Monster {
 
 	private static final long	serialVersionUID	= 85613238015172594L;
 
 	public Zombie(final int level) {
 		super( "Zombie", level, 10, 15 );
 	}
 
 }
