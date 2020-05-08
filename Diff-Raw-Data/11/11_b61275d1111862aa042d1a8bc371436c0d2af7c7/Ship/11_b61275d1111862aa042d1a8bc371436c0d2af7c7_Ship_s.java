 /**
  * DISASTEROIDS
  * Ship.java
  */
 package disasteroids;
 
 import disasteroids.weapons.FlechetteManager;
 import disasteroids.gui.MainWindow;
 import disasteroids.gui.Local;
 import disasteroids.gui.ParticleManager;
 import disasteroids.gui.Particle;
 import disasteroids.networking.Server;
 import disasteroids.networking.ServerCommands;
 import disasteroids.sound.Sound;
 import disasteroids.sound.SoundLibrary;
 import disasteroids.weapons.BigNukeLauncher;
 import disasteroids.weapons.BulletManager;
 import disasteroids.weapons.GuidedMissileManager;
 import disasteroids.weapons.LaserManager;
 import disasteroids.weapons.LittleDoctorManager;
 import disasteroids.weapons.MineManager;
 import disasteroids.weapons.MissileManager;
 import disasteroids.weapons.SniperManager;
 import disasteroids.weapons.Unit;
 import disasteroids.weapons.Weapon;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.Stroke;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 /**
  * A player ship.
  * @author Andy Kooiman, Phillip Cohen
  * @since Classic
  */
 public class Ship extends ShootingObject
 {
     public final static double SENSITIVITY = 20;
 
     /**
      * How many lives players start with.
      * @since April 9, 2008
      */
     public final static int START_LIVES = 4;
 
     private final static int RADIUS = 10;
 
     /**
      * Togglers for various actions.
      * @since Classic (last minute)
      */
     private boolean forward = false, backwards = false, left = false, right = false, shooting = false, sniping = false;
 
     /**
      * The angle we're facing.
      * @since Classic
      */
     private double angle = Math.PI / 2;
 
     /**
      * Our name, showed on the scoreboard.
      * @since December 14, 2007
      */
     private String name;
 
     /**
      * Our color. The ship, missiles, <code>StarMessages</code>s, and explosions are drawn in this.
      * @since Classic
      */
     private Color myColor;
 
     /**
      * A darker version of <code>myColor</code> shown when invincible.
      * @since Classic
      */
     private Color myInvicibleColor;
 
     /**
      * Time left until we become vincible.
      * @since Classic
      */
     private int invincibilityCount;
 
     /**
      * How long until we are able to start repairing
      */
     private int repairCount;
 
     /**
      * How many reserve lives remain. When less than 0, the game is over.
      * @since Classic
      */
     private int livesLeft;
 
     /**
      * Our score.
      * @since Classic
      */
     private int score = 0;
 
     /**
      * How many <code>Asteroid</code>s we've killed on this level.
      * @since December 16, 2007
      */
     private int numAsteroidsKilled = 0;
 
     /**
      * How many <code>Ship</code>s we've killed on this level.
      * @since December 16, 2007
      */
     private int numShipsKilled = 0;
 
     /**
      * The <code>SniperManager</code> for this <code>Ship</code>
      * @since March 26, 2008
      */
     private SniperManager sniperManager;
 
     /**
      * How long to draw the name of the current weapon.
      * @since December 25, 2007
      */
     private int drawWeaponNameTimer = 0;
 
     /**
      * How many acceleration particles should be emitted out the front. Used for smooth effects.
      * @since March 3, 2008
      */
     private double particleRateForward = 0.0;
 
     /**
      * How many acceleration particles should be emitted out the back. Used for smooth effects.
      * @since March 3, 2008
      */
     private double particleRateBackward = 0.0;
 
     /**
      * Timer for the endgame sequence.
      * @since March 8, 2008
      */
     private int explosionTime = 0;
 
     /**
      * How much acceleration should occur due to strafing. Positive is to the ship's right.
      * @since March 9, 2008
      */
     private double strafeSpeed = 0;
 
     /**
      * Flashing for the sniping indicator.
      * @since March 12, 2008
      */
     private boolean snipeFlash = false;
 
     /**
      * If this <code>Ship</code> has a shield(s).
      */
     private int shielded = 0;
 
     private boolean stopping = false;
 
     private double healthMax = 100, health = healthMax;
 
     private int inBlackHoleTimer = 0;
 
     private boolean hasBeenWarped = false;
 
     private final int INBLACKHOLE_TIMER_MAX = 150;
 
     // ***************************************************** Standard API **
     public Ship( double x, double y, Color c, int lives, String name )
     {
         super( x, y, 0, 0, 7 );
         this.myColor = c;
         this.livesLeft = lives;
         this.name = name;
 
         weapons[0] = new MissileManager( this );
         weapons[1] = new BulletManager( this );
         weapons[2] = new MineManager( this );
         weapons[3] = new LaserManager( this );
         weapons[4] = new FlechetteManager( this );
         weapons[5] = new BigNukeLauncher( this );
         weapons[6] = new LittleDoctorManager( this );
         sniperManager = new SniperManager( this );
 
         // Colors.        
         double fadePct = 0.6;
         myInvicibleColor = new Color( (int) ( myColor.getRed() * fadePct ), (int) ( myColor.getGreen() * fadePct ), (int) ( myColor.getBlue() * fadePct ) );
 
         // Start invincible.
         invincibilityCount = 200;
         repairCount = -1;
     }
 
     @Override
     public void act()
     {
         if ( inBlackHoleTimer > 0 )
         {
             inBlackHoleTimer--;
             if ( getInBlackHoleAlpha() >= 1.0 && !hasBeenWarped )
             {
                 setLocation( Util.getGameplayRandomGenerator().nextInt( Game.getInstance().GAME_WIDTH ), Util.getGameplayRandomGenerator().nextInt( Game.getInstance().GAME_HEIGHT ) );
 
                 hasBeenWarped = true;
                 if ( Server.is() )
                     ServerCommands.updateObjectVelocity( this );
             }
             return;
         }
 
         super.act();
         if ( livesLeft >= 0 )
         {
             if ( shooting )
             {
                 if ( getActiveWeapon().getAmmo() == 0 )
                 {
                     Local.getStarBackground().writeOnBackground( "Out of ammo for " + getActiveWeapon().getName() + ".", (int) getX(), (int) getY() - 5, myColor );
                     Main.log( "Out of ammo for " + getActiveWeapon().getName() + "." );
                     rotateWeapons();
                     shooting = false;
                 }
                 else
                 {
                     if ( canShoot() )
                         shoot();
                 }
             }
             invincibilityCount--;
             if ( repairCount == 0 )
                 repair();
             else if ( repairCount > 0 )
                 repairCount--;
             checkCollision();
             generateParticles();
             move();
         }
         else
         {
             explosionTime--;
 
             // Create particles.
             for ( int i = 0; i < explosionTime / 12; i++ )
             {
                 ParticleManager.addParticle( new Particle(
                         getX() + Util.getGameplayRandomGenerator().nextInt( 16 ) - 8 - RADIUS,
                         getY() + Util.getGameplayRandomGenerator().nextInt( 16 ) - 8 - RADIUS,
                         Util.getGameplayRandomGenerator().nextInt( 4 ) + 3,
                         Util.getGameplayRandomGenerator().nextBoolean() ? myColor : myInvicibleColor,
                         Util.getGameplayRandomGenerator().nextDouble() * 6,
                         Util.getGameplayRandomGenerator().nextAngle(),
                         25 + explosionTime / 10, 10 ) );
             }
         }
 
         sniperManager.act();
         sniperManager.reload();
 
         // Out of ammo. :(
 /*        if ( getWeaponManager().getAmmo() == 0 )
         {
         Local.getStarBackground().writeOnBackground( "Out of ammo for " + getWeaponManager().getName() + ".", (int) getX(), (int) getY() - 5, myColor );
         Running.log( "Out of ammo for " + getWeaponManager().getName() + "." );
         rotateWeapons();
         }*/
     }
 
     @Override
     public void draw( Graphics g )
     {
         super.draw( g );
         sniperManager.draw( g );
 
         if ( livesLeft < 0 )
             return;
 
         // Set our color.
         Color col = ( cannotDie() ? myInvicibleColor : myColor );
         double centerX, centerY;
 
         if ( this == Local.getLocalPlayer() )
         {
             centerX = MainWindow.frame().getWidth() / 2;
             centerY = MainWindow.frame().getHeight() / 2;
         }
         else
         {
             centerX = ( getX() - Local.getLocalPlayer().getX() + MainWindow.frame().getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH;
             centerY = ( getY() - Local.getLocalPlayer().getY() + MainWindow.frame().getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT;
 
             if ( !( centerX > -100 && centerX < Game.getInstance().GAME_WIDTH + 100 && centerY > -100 && centerY < Game.getInstance().GAME_HEIGHT + 100 ) )
                 return;
         }
 
         // TODO: Create RelativeGraphics.transformPolygon()
         Polygon outline = new Polygon();
         outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle ) ) + MainWindow.frame().getRumbleX(), (int) ( centerY - RADIUS * Math.sin( angle ) ) + MainWindow.frame().getRumbleY() );
         outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle + Math.PI * .85 ) ) + MainWindow.frame().getRumbleX(), (int) ( centerY - RADIUS * Math.sin( angle + Math.PI * .85 ) ) - MainWindow.frame().getRumbleY() );
         outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle - Math.PI * .85 ) ), (int) ( centerY - RADIUS * Math.sin( angle - Math.PI * .85 ) ) );
 
         // Flash when invincible.
         if ( !cannotDie() || ( cannotDie() && Util.getGlobalFlash() ) )
             MainWindow.frame().drawPolygon( g, col, ( myColor.getRed() + myColor.getGreen() + myColor.getBlue() > 64 * 3 ? Color.black : Color.darkGray ), outline );
         //  AsteroidsFrame.frame().drawImage(g, ImageLibrary.getShip(Color.red), (int)x, (int)y,Math.PI/2 -angle, Ship.RADIUS/37.5);
 
         // Draw shields.
         if ( shielded > 0 )
         {
             int i = 0;
             for ( ; i < shielded / 100; i++ )
                 MainWindow.frame().drawCircle( g, Color.CYAN, (int) getX(), (int) getY(), ( RADIUS + 2 ) + 3 * i );
 
             int shieledRemaining = shielded % 100;
             if ( shieledRemaining > 0 )
                 MainWindow.frame().drawCircle( g, Color.getHSBColor( 0.5f, 1.0f, shieledRemaining / 100.0f ), (int) getX(), (int) getY(), ( RADIUS + 2 ) + 3 * ( i ) );
         }
 
         if ( this == Local.getLocalPlayer() && drawWeaponNameTimer > 0 )
         {
             drawWeaponNameTimer--;
             g.setFont( new Font( "Century Gothic", Font.BOLD, 14 ) );
             MainWindow.frame().drawString( g, (int) getX(), (int) getY() - 15, getActiveWeapon().getName(), Color.gray );
             weapons[activeWeapon].drawOrphanUnit( g, getX(), getY() + 25, Color.gray );
         }
 
         if ( sniping )
         {
             sniperManager.drawHUD( g, this );
             snipeFlash = !snipeFlash;
             if ( snipeFlash )
             {
                 float dash[] =
                 {
                     8.0f
                 };
                 Stroke old = ( (Graphics2D) g ).getStroke();
                 ( (Graphics2D) g ).setStroke( new BasicStroke( 3.0f, BasicStroke.CAP_ROUND,
                         BasicStroke.JOIN_ROUND, 5.0f, dash, 2.0f ) );
                 MainWindow.frame().drawLine( g, myInvicibleColor, (int) getX(), (int) getY(), 1500, 15, angle );
                 ( (Graphics2D) g ).setStroke( old );
             }
         }
         else
             getActiveWeapon().drawHUD( g, this );
     }
 
     // ***************************************************** Key press & release **
     public void setForward( boolean pressed )
     {
         forward = pressed;
     }
 
     public void setBackwards( boolean pressed )
     {
         backwards = pressed;
     }
 
     public void setLeft( boolean pressed )
     {
         left = pressed;
     }
 
     public void setRight( boolean pressed )
     {
         right = pressed;
     }
 
     public void setShooting( boolean pressed )
     {
         this.shooting = pressed;
     }
 
     public void setBrake( boolean on )
     {
         stopping = on;
     }
 
     @Override
     public String toString()
     {
         return "Ship #" + getId() + " ~ [" + (int) getX() + "," + (int) getY() + "]";
     }
 
     public void clearWeapons()
     {
         for ( Weapon wM : weapons )
             wM.clear();
         sniperManager.clear();
     }
 
     public void restoreBonusValues()
     {
         for ( Weapon wM : weapons )
             wM.undoBonuses();
         sniperManager.undoBonuses();
     }
 
     /**
      * Rotates to the player's next weapon that has ammo.
      */
     public void rotateWeapons()
     {
         int startIndex = activeWeapon;
         do
         {
             activeWeapon++;
             activeWeapon %= weapons.length;
         }
         while ( weapons[activeWeapon].getAmmo() == 0 && activeWeapon != startIndex );
         drawWeaponNameTimer = 50;
     }
 
     public String giveShield( int amount )
     {
         shielded += amount;
         return "Shield";
     }
 
     private void checkCollision()
     {
         for ( ShootingObject other : Game.getInstance().getObjectManager().getShootingObjects() )
         {
             if ( other == this )
                 continue;
 
             for ( Weapon wm : other.getWeapons() )
             {
                 for ( Unit m : wm.getUnits() )
                 {
                     if ( Util.getDistance( this, m ) < RADIUS + m.getRadius() )
                     {
                         String obit = "";
                         if ( other instanceof Ship )
                             obit = getName() + " was blasted by " + ( (Ship) other ).getName() + ".";
                         else if ( other instanceof Station )
                             obit = getName() + " was shot down by a satellite.";
                         else if ( other instanceof Alien )
                             obit = getName() + " was scanned unsuccessfully for intelligent lifeforms.";
 
                         if ( damage( m.getDamage(), obit ) )
                         {
                             m.explode();
                             if ( other.getClass().isInstance( this ) )
                             {
                                 Ship s = (Ship) other;
                                 s.score += 500;
                                 s.numShipsKilled++;
                                 s.livesLeft++;
                             }
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Generates particles for the ship's boosters.
      * 
      * @since December 16, 2007
      */
     private void generateParticles()
     {
         // System.out.println( 9 * -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- 8 ); // :O
         //gotta love old comments that live for a while
         if ( forward && !( Math.abs( getDx() ) < 0.1 && Math.abs( getDy() ) < 0.15 ) )
             particleRateForward = Math.min( 1, Math.max( 0.4, particleRateForward + 0.05 ) );
         else
             particleRateForward = Math.max( 0, particleRateForward - 0.03 );
 
         if ( backwards && !( Math.abs( getDx() ) < 0.1 && Math.abs( getDy() ) < 0.15 ) )
             particleRateBackward = Math.min( 1, Math.max( 0.4, particleRateBackward + 0.05 ) );
         else
             particleRateBackward = Math.max( 0, particleRateBackward - 0.03 );
 
 
         for ( int i = 0; i < (int) ( particleRateForward * 3 ); i++ )
             ParticleManager.addParticle( new Particle(
                     -15 * Math.cos( angle ) + getX() + Util.getGameplayRandomGenerator().nextInt( 8 ) - 4,
                     15 * Math.sin( angle ) + getY() + Util.getGameplayRandomGenerator().nextInt( 8 ) - 4,
                     Util.getGameplayRandomGenerator().nextInt( 4 ) + 3,
                     myColor,
                     Util.getGameplayRandomGenerator().nextDouble() * 4,
                     angle + Util.getGameplayRandomGenerator().nextDouble() * .4 - .2 + Math.PI,
                     30, 10 ) );
 
         for ( int i = 0; i < (int) ( particleRateBackward * 3 ); i++ )
             ParticleManager.addParticle( new Particle(
                     15 * Math.cos( angle ) + getX() + Util.getGameplayRandomGenerator().nextInt( 8 ) - 4,
                     -15 * Math.sin( angle ) + getY() + Util.getGameplayRandomGenerator().nextInt( 8 ) - 4,
                     Util.getGameplayRandomGenerator().nextInt( 4 ) + 3,
                     myColor,
                     Util.getGameplayRandomGenerator().nextDouble() * 4,
                     angle + Util.getGameplayRandomGenerator().nextDouble() * .4 - .2,
                     30, 10 ) );
 
 
 
     }
 
     @Override
     public void move()
     {
         super.move();
 
         if ( forward )
             setVelocity( getDx() + Math.cos( angle ) / SENSITIVITY * 2, getDy() - Math.sin( angle ) / SENSITIVITY * 2 );
         if ( backwards )
             setVelocity( getDx() - Math.cos( angle ) / SENSITIVITY * 2, getDy() + Math.sin( angle ) / SENSITIVITY * 2 );
 
         if ( left )
             angle += Math.PI / SENSITIVITY / ( sniping ? 12 : 2 );
         if ( right )
             angle -= Math.PI / SENSITIVITY / ( sniping ? 12 : 2 );
 
         if ( Math.abs( strafeSpeed ) > 0 )
         {
             strafeSpeed *= 0.97;
             setLocation( getX() + Math.sin( angle ) * strafeSpeed, getY() + Math.cos( angle ) * strafeSpeed );
 
             if ( Math.abs( strafeSpeed ) <= 0.11 )
                 strafeSpeed = 0;
         }
 
         // Attrition of speed.
         decelerate( .99 );
         if ( stopping == true )
             slowStop();
     }
 
     public void shoot()
     {
         if ( livesLeft < 0 )
             return;
         getActiveWeapon().shoot( this, myColor, angle );
     }
 
     public boolean canShoot()
     {
         return ( livesLeft >= 0 && getActiveWeapon().canShoot() );
     }
 
     public Color getColor()
     {
         return myColor;
     }
 
     /**
      * If you can't figure out what this does you're retarded.
      */
     public void setInvincibilityCount( int num )
     {
         invincibilityCount = num;
     }
 
     /**
      * Does a certain amount of damage to this ship, potentially killing it.
      * 
      * @param obituary  the string to announce to the game. For example, <code>ship.getName() + " played with fire."</code>
      * @return          whether damage was dealt (ship not invincible)
      * @since Classic
      */
     public boolean damage( double amount, String obituary )
     {
         // We're invincible and can't die.
         if ( cannotDie() )
             return false;
 
         // Shield saved us.
         if ( shielded > 0 )
         {
             //setInvincibilityCount( 50 );
             if ( shielded > amount / 2.0 )
             {
                 shielded -= amount / 2.0;
                 return true;
             }
             else
             {
                 setInvincibilityCount( 50 );
                 shielded = 0;
                 return true;
             }
         }
 
         // Lose health, and some max health as well.
         health -= amount;
         healthMax -= amount / 3.0;
         score -= Math.min( amount, 300 ) * 5;
 
         // Bounce.
         setVelocity( getDx() * -.3, getDy() * -.3 );
 
         // If just a wound, play the sound and leave.
         if ( health > 0 )
         {
             repairCount = 100;
             Sound.playInternal( SoundLibrary.SHIP_HIT );
             MainWindow.frame().rumble( amount * 2 / 3.0 );
             return true;
         }
 
         // We lost a life.
         livesLeft--;
 
         // Continue play.
         if ( livesLeft >= 0 )
         {
             health = healthMax = 100;
             setInvincibilityCount( 300 );
             MainWindow.frame().rumble( 30 );
             Sound.playInternal( SoundLibrary.SHIP_DIE );
 
             // Create particles.
             for ( int i = 0; i < 80; i++ )
             {
                 ParticleManager.addParticle( new Particle(
                         getX() + Util.getGameplayRandomGenerator().nextInt( 16 ) - 8 - RADIUS,
                         getY() + Util.getGameplayRandomGenerator().nextInt( 16 ) - 8 - RADIUS,
                         Util.getGameplayRandomGenerator().nextInt( 4 ) + 3,
                         myColor,
                         Util.getGameplayRandomGenerator().nextDouble() * 6,
                         Util.getGameplayRandomGenerator().nextAngle(),
                         30, 10 ) );
             }
         } // We lost the game.
         else
         {
             invincibilityCount = Integer.MAX_VALUE;
             explosionTime = 160;
             setBrake( true );
             MainWindow.frame().rumble( 85 );
             if ( Settings.isSoundOn() && this == Local.getLocalPlayer() )
                 Sound.playInternal( SoundLibrary.GAME_OVER );
 
             // Create lots of particles.
             for ( int i = 0; i < 500; i++ )
             {
                 ParticleManager.addParticle( new Particle(
                         getX() + Util.getGameplayRandomGenerator().nextInt( 16 ) - 8 - RADIUS,
                         getY() + Util.getGameplayRandomGenerator().nextInt( 16 ) - 8 - RADIUS,
                         Util.getGameplayRandomGenerator().nextInt( 4 ) + 3,
                         myColor,
                         Util.getGameplayRandomGenerator().nextDouble() * 4,
                         Util.getGameplayRandomGenerator().nextAngle(),
                         60, 5 ) );
             }
         }
 
         // Print the obit.
         if ( obituary.length() > 0 )
             Main.log( obituary );
 
         return true;
     }
 
     public boolean cannotDie()
     {
         return invincibilityCount > 0;
     }
 
     @Override
     public Weapon getActiveWeapon()
     {
         return sniping ? this.sniperManager : super.getActiveWeapon();
     }
 
     public void increaseScore( int points )
     {
         score += points;
     }
 
     public int getScore()
     {
         return score;
     }
 
     public void addLife()
     {
         if ( livesLeft >= 0 )
             livesLeft++;
     }
 
     public int score()
     {
         return score;
     }
 
     public int livesLeft()
     {
         return livesLeft;
     }
 
     public void berserk()
     {
         if ( getActiveWeapon().getAmmo() == 0 )
         {
             Local.getStarBackground().writeOnBackground( "Out of ammo for " + getActiveWeapon().getName() + ".", (int) getX(), (int) getY() - 5, myColor );
             Main.log( "Out of ammo for " + getActiveWeapon().getName() + "." );
             rotateWeapons();
         }
         if ( Server.is() )
             ServerCommands.berserk( getId() );
         getActiveWeapon().berserk( this, myColor );
     }
 
     /**
      * Returns <code>this</code> player's in-game name.
      */
     public String getName()
     {
         return name;
     }
 
     public boolean isInBlackHole()
     {
         return getInBlackHoleAlpha() > 0;
     }
 
     public float getInBlackHoleAlpha()
     {
         float percent = (float) inBlackHoleTimer / INBLACKHOLE_TIMER_MAX;
         float alpha = (float) Math.min( 4 * ( percent - Math.pow( percent, 2 ) ), 1f );
         return alpha;
     }
 
     /**
      * Returns the statistic of <code>Asteroid</code>s killed on this level.
      * An <code>Asteroid</code> is "killed" when it splits, so both bullets and collisions will affect this counter.
      * 
      * @return  the number of <code>Asteroid</code>s <code>this</code> has killed on this level
      * @since December 16, 2007
      */
     public int getNumAsteroidsKilled()
     {
         return numAsteroidsKilled;
     }
 
     /**
      * Sets the number of <code>Asteroid</code>s killed on this level.
      */
     public void setNumAsteroidsKilled( int numAsteroidsKilled )
     {
         this.numAsteroidsKilled = numAsteroidsKilled;
     }
 
     /**
      * Returns the statistic of <code>Ship</code>s killed this level.
      */
     public int getNumShipsKilled()
     {
         return numShipsKilled;
     }
 
     /**
      * Sets the number of <code>Ship</code>s killed on this level.
      */
     public void setNumShipsKilled( int numShipsKilled )
     {
         this.numShipsKilled = numShipsKilled;
     }
 
     // ***************************************************** Networking **
     /**
      * Writes our position, angle, key presses, and speed.
      */
     @Override
     public void flattenPosition( DataOutputStream stream ) throws IOException
     {
         super.flattenPosition( stream );
         stream.writeDouble( angle );
         stream.writeBoolean( left );
         stream.writeBoolean( right );
         stream.writeBoolean( backwards );
         stream.writeBoolean( forward );
         stream.writeBoolean( shooting );
         stream.writeInt( activeWeapon );
     }
 
     /**
      * Reads our position, angle, key presses, and speed.
      */
     @Override
     public void restorePosition( DataInputStream stream ) throws IOException
     {
         super.restorePosition( stream );
         angle = stream.readDouble();
         left = stream.readBoolean();
         right = stream.readBoolean();
         backwards = stream.readBoolean();
         forward = stream.readBoolean();
         shooting = stream.readBoolean();
         activeWeapon = stream.readInt();
     }
 
     /**
      * Writes <code>this</code> to a stream for client/server transmission.
      */
     @Override
     public void flatten( DataOutputStream stream ) throws IOException
     {
         super.flatten( stream );
         stream.writeInt( invincibilityCount );
         stream.writeInt( myColor.getRGB() );
 
         stream.writeUTF( name );
         stream.writeInt( activeWeapon );
         stream.writeInt( numAsteroidsKilled );
         stream.writeInt( numShipsKilled );
 
         stream.writeInt( livesLeft );
         stream.writeDouble( health );
         stream.writeDouble( healthMax );
         stream.writeInt( shielded );
 
         for ( Weapon w : weapons )
             w.flatten( stream );
 
         sniperManager.flatten( stream );
         stream.writeInt( 666 );
     }
 
     /**
      * Creates <code>this</code> from a stream for client/server transmission.
      */
     public Ship( DataInputStream stream ) throws IOException
     {
        super( stream, 6 );
         invincibilityCount = stream.readInt();
         myColor = new Color( stream.readInt() );
 
         name = stream.readUTF();
         activeWeapon = stream.readInt();
         numAsteroidsKilled = stream.readInt();
         numShipsKilled = stream.readInt();
 
         livesLeft = stream.readInt();
         health = stream.readDouble();
         healthMax = stream.readDouble();
         shielded = stream.readInt();
 
         weapons[0] = new MissileManager( stream, this );
         weapons[1] = new BulletManager( stream, this );
         weapons[2] = new MineManager( stream, this );
         weapons[3] = new LaserManager( stream, this );
         weapons[4] = new FlechetteManager( stream, this );
         weapons[5] = new BigNukeLauncher( stream, this );
         weapons[6] = new LittleDoctorManager( stream, this );
         sniperManager = new SniperManager( stream, this );
 
         int check = stream.readInt();
         if ( check != 666 )
             Main.fatalError( "Failed checksum test.\n\nReceived " + check + "..." + weapons[0].getAmmo() );
 
         // Apply basic construction.        
         double fadePct = 0.6;
         myInvicibleColor = new Color( (int) ( myColor.getRed() * fadePct ), (int) ( myColor.getGreen() * fadePct ), (int) ( myColor.getBlue() * fadePct ) );
     }
 
     public int getWeaponIndex()
     {
         return activeWeapon;
     }
 
     public int getExplosionTime()
     {
         return explosionTime;
     }
 
     /**
      * Executes a quick burst of movement to one side.
      * 
      * @param toRight   whether we should strafe right (true) or left (false)
      * @since March 9, 2008
      */
     public void strafe( boolean toRight )
     {
         if ( Math.abs( strafeSpeed ) > 3 )
             return;
 
         strafeSpeed += ( toRight ? 1 : -1 ) * 7;
 
         for ( int i = 0; i < 9; i++ )
             ParticleManager.addParticle( new Particle(
                     getX() + Util.getGameplayRandomGenerator().nextInt( 8 ) - 4,
                     getY() + Util.getGameplayRandomGenerator().nextInt( 8 ) - 4,
                     Util.getGameplayRandomGenerator().nextInt( 4 ) + 3,
                     myColor,
                     Util.getGameplayRandomGenerator().nextDouble() * 4,
                     angle + Util.getGameplayRandomGenerator().nextDouble() * .8 - .2 + Math.PI,
                     35, 35 ) );
 
         if ( Server.is() )
             ServerCommands.strafe( getId(), toRight );
     }
 
     /**
      * Toggles sniping mode, in which the player aims much more precisely.
      * @param on    whether sniping is on
      * 
      * @since March 11, 2008
      */
     public void setSnipeMode( boolean on )
     {
         sniping = on;
     }
 
     public int getRadius()
     {
         return shielded > 0 ? RADIUS + 5 : RADIUS;
     }
 
     /**
      * Switches to the given weapon slot, if it has ammo.
      * Note: Slot is 0-based.
      * 
      * @param newIndex  the slot
      */
     public void setWeapon( int index )
     {
         if ( weapons[index % weapons.length].getAmmo() == 0 )
             return;
 
         activeWeapon = index % weapons.length;
         drawWeaponNameTimer = 50;
     }
 
     public double getHealth()
     {
         return health;
     }
 
     public double getShield()
     {
         return shielded;
     }
 
     private void repair()
     {
         if ( repairCount != 0 )
             return;
         if ( health >= healthMax )
         {
             repairCount = -1;
             return;
         }
         health += .1;
     }
 
     private void slowStop()
     {
         if ( getDx() > .4 || getDx() < -.4 )
             setDx( getDx() * .9 );
         if ( getDy() > .4 || getDy() < -.4 )
             setDy( getDy() * .9 );
         if ( Math.abs( getDx() ) < .4 && Math.abs( getDy() ) < .4 )
         {
             stopping = false;
             setDx( 0 );
             setDy( 0 );
         }
     }
 
     @Override
     public void inBlackHole()
     {
         if ( inBlackHoleTimer == 0 )
         {
             damage( 150, getName() + " was sucked into a black hole." );
             inBlackHoleTimer = INBLACKHOLE_TIMER_MAX;
             hasBeenWarped = false;
             setInvincibilityCount( INBLACKHOLE_TIMER_MAX );
         }        
         // strafeSpeed = 16;
     }
 
     public void setHealth( double health )
     {
         this.health = health;
     }
 }
