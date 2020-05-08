 /**
  * DISASTEROIDS
  * Ship.java
  */
 package disasteroids;
 
 import disasteroids.networking.Server;
 import disasteroids.sound.Sound;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Random;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * A player ship.
  * @author Andy Kooiman, Phillip Cohen
  * @since Classic
  */
 public class Ship implements GameElement, ShootingObject
 {
     public int id;
 
     public final static double SENSITIVITY = 30;
 
     public final static int RADIUS = 10;
 
     /**
      * Our location on the absolute game world.
      * @since Classic
      */
     private double x,  y;
 
     /**
      * Our speed (delta).
      * @since Classic
      */
     private double dx,  dy;
 
     /**
      * Togglers for various actions.
      * @since Classic (last minute)
      */
     private boolean forward = false,  backwards = false,  left = false,  right = false,  shooting = false;
 
     /**
      * The angle we're facing.
      * @since Classic
      */
     private double angle;
 
     /**
      * Our name, showed on the scoreboard.
      * @since December 14, 2007
      */
     private String name;
 
     /**
      * Our color. The ship, missiles, <code>StarMissile</code>s, and explosions are drawn in this.
      * @see Game#PLAYER_COLORS
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
      * How many reserve lives remain. When less than 0, the game is over.
      * @since Classic
      */
     private int livesLeft;
 
     /**
      * Toggler for the invincibility flashing during drawing.
      * @since Classic
      */
     private boolean invulFlash;
 
     /**
      * Our score.
      * @since Classic
      */
     private int score;
 
     /**
      * How many <code>Asteroid</code>s we've killed on this level.
      * @since December 16, 2007
      */
     private int numAsteroidsKilled;
 
     /**
      * How many <code>Ship</code>s we've killed on this level.
      * @since December 16, 2007
      */
     private int numShipsKilled;
 
     /**
      * The current weapon selected in <code>allWeapons</code>.
      * @since December 25, 2007
      */
     private int weaponIndex;
 
     /**
      * Our cache of weapons.
      * @since December 16, 2007
      */
     private WeaponManager[] allWeapons;
 
     /**
      * How long to draw the name of the current weapon.
      * @since December 25, 2007
      */
     private int drawWeaponNameTimer;
 
     public Ship( int x, int y, Color c, int lives, String name )
     {
         this.x = x;
         this.y = y;
         this.myColor = c;
         this.livesLeft = lives;
         this.name = name;
 
         score = 0;
         numAsteroidsKilled = 0;
         drawWeaponNameTimer = 0;
         numShipsKilled = 0;
         angle = Math.PI / 2;
         dx = 0;
         dy = 0;
 
         // Colors.        
         double fadePct = 0.6;
         myInvicibleColor = new Color( (int) ( myColor.getRed() * fadePct ), (int) ( myColor.getGreen() * fadePct ), (int) ( myColor.getBlue() * fadePct ) );
 
         // Init weapons.
         allWeapons = new WeaponManager[3];
         allWeapons[0] = new MissileManager();
         allWeapons[1] = new BulletManager();
         allWeapons[2] = new MineManager();
         weaponIndex = 0;
 
         // Start invincible.
         invulFlash = true;
         invincibilityCount = 200;
 
         // Assign an unique ID.
         boolean uniqueId = false;
         while ( !uniqueId )
         {
             uniqueId = true;
             id = RandNumGen.getMissileInstance().nextInt( 5432 ) + Game.getInstance().players.size() + 4;
             for ( Ship s : Game.getInstance().players )
             {
                 if ( s == this )
                     continue;
                 if ( s.id == this.id )
                     uniqueId = false;
             }
         }
 
     }
 
     @Override
     public String toString()
     {
         return id + " ~ [" + (int) x + "," + (int) y + "]";
     }
 
     public void clearWeapons()
     {
         for ( WeaponManager wM : allWeapons )
             wM.clear();
     }
 
     public void restoreBonusValues()
     {
         for ( WeaponManager wM : allWeapons )
             wM.restoreBonusValues();
     }
 
     public WeaponManager[] allWeapons()
     {
         return allWeapons;
     }
 
     public void draw( Graphics g )
     {
         for ( WeaponManager wm : allWeapons )
             wm.draw( g );
         allWeapons[weaponIndex].drawTimer( g, myColor );
 
         // Set our color.
         Color col;
         if ( cannotDie() )
             col = myInvicibleColor;
         else
             col = myColor;
 
         double centerX, centerY;
 
         if ( this == AsteroidsFrame.frame().localPlayer() )
         {
             centerX = AsteroidsFrame.frame().getWidth() / 2;
             centerY = AsteroidsFrame.frame().getHeight() / 2;
         }
         else
         {
             centerX = ( x - AsteroidsFrame.frame().localPlayer().getX() + AsteroidsFrame.frame().getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH;
             centerY = ( y - AsteroidsFrame.frame().localPlayer().getY() + AsteroidsFrame.frame().getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT;
 
             if ( !( centerX > -100 && centerX < Game.getInstance().GAME_WIDTH + 100 && centerY > -100 && centerY < Game.getInstance().GAME_HEIGHT + 100 ) )
                 return;
         }
 
         Polygon outline = new Polygon();
         outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle ) ), (int) ( centerY - RADIUS * Math.sin( angle ) ) );
         outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle + Math.PI * .85 ) ), (int) ( centerY - RADIUS * Math.sin( angle + Math.PI * .85 ) ) );
         outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle - Math.PI * .85 ) ), (int) ( centerY - RADIUS * Math.sin( angle - Math.PI * .85 ) ) );
 
         if ( ( cannotDie() && ( invulFlash = !invulFlash ) == true ) || !( cannotDie() ) )
         {
             AsteroidsFrame.frame().drawPolygon( g, col, Color.black, outline );
         }
 
         if ( this == AsteroidsFrame.frame().localPlayer() && drawWeaponNameTimer > 0 )
         {
             drawWeaponNameTimer--;
            g.setFont( new Font("Century Gothic", Font.BOLD, 14) );
             Graphics2D g2d = (Graphics2D) g;
             AsteroidsFrame.frame().drawString( g, (int) x - (int) g2d.getFont().getStringBounds( getWeaponManager().getWeaponName(), g2d.getFontRenderContext() ).getWidth() / 2, (int) y - 15, getWeaponManager().getWeaponName(), Color.gray );
             allWeapons[weaponIndex].getWeapon( (int) x, (int) y + 25, Color.gray ).draw( g );
         }
     }
 
     public void forward()
     {
         forward = true;
     }
 
     public void backwards()
     {
         backwards = true;
     }
 
     public void left()
     {
         left = true;
     }
 
     public void right()
     {
         right = true;
     }
 
     public void unforward()
     {
         forward = false;
     }
 
     public void unbackwards()
     {
         backwards = false;
     }
 
     public void unleft()
     {
         left = false;
     }
 
     public void unright()
     {
         right = false;
     }
 
     public void startShoot()
     {
         this.shooting = true;
     }
 
     public void stopShoot()
     {
         this.shooting = false;
     }
 
     public void act()
     {
         if ( forward )
         {
             dx += Math.cos( angle ) / SENSITIVITY * 2;
             dy -= Math.sin( angle ) / SENSITIVITY * 2;
         }
         if ( backwards )
         {
             dx -= Math.cos( angle ) / SENSITIVITY * 2;
             dy += Math.sin( angle ) / SENSITIVITY * 2;
         }
         if ( left )
             angle += Math.PI / SENSITIVITY / 2;
         if ( right )
             angle -= Math.PI / SENSITIVITY / 2;
         if ( shooting && canShoot() )
             shoot();
 
         for ( WeaponManager wm : allWeapons )
         {
             if ( wm == this.allWeapons[weaponIndex] )
                 wm.act( true );
             else
                 wm.act( false );
         }
 
         if ( livesLeft < 0 )
             return;
         invincibilityCount--;
         move();
         checkBounce();
         checkCollision();
         generateParticles();
     }
 
     public void fullRight()
     {
         angle = 0;
     }
 
     public void fullLeft()
     {
         angle = Math.PI;
     }
 
     public void fullUp()
     {
         angle = Math.PI / 2;
     }
 
     public void fullDown()
     {
         angle = Math.PI * 3 / 2;
     }
 
     public void allStop()
     {
         dx = dy = 0;
     }
 
     public void rotateWeapons()
     {
         weaponIndex++;
         weaponIndex %= allWeapons.length;
         drawWeaponNameTimer = 50;
     }
 
     private void checkBounce()
     {
         // Wrap to stay inside the level.
         if ( x < 0 )
             x += Game.getInstance().GAME_WIDTH - 1;
         if ( y < 0 )
             y += Game.getInstance().GAME_HEIGHT - 1;
         if ( x > Game.getInstance().GAME_WIDTH )
             x -= Game.getInstance().GAME_WIDTH - 1;
         if ( y > Game.getInstance().GAME_HEIGHT )
             y -= Game.getInstance().GAME_HEIGHT - 1;
     }
 
     private void checkCollision()
     {
         for ( ShootingObject other : Game.getInstance().shootingObjects )
         {
             if ( other == this )
                 continue;
 
             for ( WeaponManager wm : other.getManagers() )
             {
                 for ( Weapon m : wm.getWeapons() )
                 {
                     if ( Math.pow( (int) ( x - m.getX() ), 2 ) + Math.pow( (int) ( y - m.getY() ), 2 ) < 400 )
                         if ( looseLife() )
                         {
                             m.explode();
                             score -= 5000;
                             if ( other.getClass().isInstance( this ) )
                             {
                                 Ship s = (Ship) other;
                                 s.score += 5000;
                                 s.numShipsKilled++;
                                 s.livesLeft++;
                             }
                         }
                 }
             }
         }
     }
 
     /**
      * Generates particles for the ships boosters, moved from act metod
      * @snice December 16, 2007
      */
     private void generateParticles()
     {
         if ( forward && !( Math.abs( dx ) < 0.1 && Math.abs( dy ) < 0.15 ) )
         {
             Random rand = RandNumGen.getParticleInstance();
             for ( int i = 0; i < (int) ( Math.sqrt( dx * dx + dy * dy ) ); i++ )
                 ParticleManager.addParticle( new Particle(
                                              -15 * Math.cos( angle ) + x + rand.nextInt( 8 ) - 4,
                                              15 * Math.sin( angle ) + y + rand.nextInt( 8 ) - 4,
                                              rand.nextInt( 4 ) + 3,
                                              myColor,
                                              rand.nextDouble() * 4,
                                              angle + rand.nextDouble() * .4 - .2 + Math.PI,
                                              30, 10 ) );
         }
 
         if ( backwards && !( Math.abs( dx ) < 0.1 && Math.abs( dy ) < 0.15 ) )
         {
             Random rand = RandNumGen.getParticleInstance();
             for ( int i = 0; i < (int) ( Math.sqrt( dx * dx + dy * dy ) ); i++ )
                 ParticleManager.addParticle( new Particle(
                                              15 * Math.cos( angle ) + x + rand.nextInt( 8 ) - 4,
                                              -15 * Math.sin( angle ) + y + rand.nextInt( 8 ) - 4,
                                              rand.nextInt( 4 ) + 3,
                                              myColor,
                                              rand.nextDouble() * 4,
                                              angle + rand.nextDouble() * .4 - .2,
                                              30, 10 ) );
         }
     }
 
     private void move()
     {
         x += dx;
         y += dy;
 
         dx *= .996;
         dy *= .996;
     }
 
     public int getX()
     {
         return (int) x;
     }
 
     public int getY()
     {
         return (int) y;
     }
 
     public void shoot()
     {
         if ( livesLeft < 0 )
             return;
 
         getWeaponManager().add( (int) x, (int) y, angle, dx, dy, myColor, true );
     }
 
     public boolean canShoot()
     {
         return ( livesLeft >= 0 && getWeaponManager().canShoot() );
     }
 
     public Color getColor()
     {
         return myColor;
     }//if you can't figure out what this does you're retarded
     public void setInvincibilityCount( int num )
     {
         invincibilityCount = num;
     }
 
     public boolean looseLife()
     {
         if ( cannotDie() )
             return false;//died too soon, second chance
         livesLeft--;
         setInvincibilityCount( 300 );
         if ( Settings.soundOn )
             Sound.playInternal( Sound.SHIP_LOSE_LIFE_SOUND );
 
         dx *= -.3;
         dy *= -.3;
         Random rand = RandNumGen.getParticleInstance();
         for ( int i = 0; i < 80; i++ )
             ParticleManager.addParticle( new Particle(
                                          x + rand.nextInt( 16 ) - 8 - RADIUS,
                                          y + rand.nextInt( 16 ) - 8 - RADIUS,
                                          rand.nextInt( 4 ) + 3,
                                          myColor,
                                          rand.nextDouble() * 6,
                                          rand.nextDouble() * 2 * Math.PI,
                                          30, 10 ) );
         return true;
     }
 
     public boolean cannotDie()
     {
         return invincibilityCount > 0;
     }
 
     public WeaponManager getWeaponManager()
     {
         return allWeapons[weaponIndex];
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
         if ( !canShoot() )
             return;
 
         if ( Server.is() )
             Server.getInstance().berserk( id );
         allWeapons[weaponIndex].berserk( this );
     }
 
     /**
      * Returns <code>this</code> player's in-game name.
      * Names are assigned by <code>AsteroidsFrame</code>; "Player 1", "Player2", and so on.
      * 
      * @return the player's name
      * @since December 15, 2007
      */
     public String getName()
     {
         return name;
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
      * 
      * @param numAsteroidsKilled    the new number of <code>Asteroid</code>s <code>this</code> has killed on this level
      * @since December 16, 2007
      */
     public void setNumAsteroidsKilled( int numAsteroidsKilled )
     {
         this.numAsteroidsKilled = numAsteroidsKilled;
     }
 
     /**
      * Returns the statistic of <code>Ship</code>s killed this level.
      * 
      * @return  the number of <code>Ship</code>s <code>this</code> has killed on this level.
      * @since December 16, 2007
      */
     public int getNumShipsKilled()
     {
         return numShipsKilled;
     }
 
     /**
      * Sets the number of <code>Ship</code>s killed on this level.
      * 
      * @param numShipsKilled    the new number of <code>Ship</code>s <code>this</code> has killed on this level
      * @since December 16, 2007
      */
     public void setNumShipsKilled( int numShipsKilled )
     {
         this.numShipsKilled = numShipsKilled;
     }
 
     /**
      * Writes <code>this</code> to a stream for client/server transmission.
      * 
      * @param stream the stream to write to
      * @throws java.io.IOException 
      * @since December 30, 2007
      */
     public void flatten( DataOutputStream stream ) throws IOException
     {
         stream.writeInt( id );
 
         flattenPosition( stream );
 
         stream.writeInt( weaponIndex );
 
         stream.writeInt( invincibilityCount );
 
         // Find our color.
         int colorIndex = -1;
         for ( int i = 0; i < Game.getInstance().PLAYER_COLORS.length; i++ )
         {
             if ( Game.getInstance().PLAYER_COLORS[i] == myColor )
             {
                 colorIndex = i;
                 break;
             }
         }
         if ( colorIndex == -1 )
             Running.fatalError( "Unknown ship color: " + myColor );
 
         stream.writeInt( colorIndex );
         stream.writeUTF( name );
         stream.writeInt( livesLeft );
         stream.writeInt( weaponIndex );
         stream.writeInt( numAsteroidsKilled );
         stream.writeInt( numShipsKilled );
 
     // (Whew!)
     }
 
     /**
      * Writes our position, angle, key presses, and speed.
      * 
      * @param stream    the stream to write to
      * @throws java.io.IOException
      * @since January 2, 2008
      */
     public void flattenPosition( DataOutputStream stream ) throws IOException
     {
         stream.writeDouble( x );
         stream.writeDouble( y );
         stream.writeDouble( dx );
         stream.writeDouble( dy );
         stream.writeDouble( angle );
 
         stream.writeBoolean( left );
         stream.writeBoolean( right );
         stream.writeBoolean( backwards );
         stream.writeBoolean( forward );
         stream.writeBoolean( shooting );
     }
 
     /**
      * Reads our position, angle, key presses, and speed.
      * 
      * @param stream    the stream to read from
      * @throws java.io.IOException
      * @since January 2, 2008
      */
     public void restorePosition( DataInputStream stream ) throws IOException
     {
         x = stream.readDouble();
         y = stream.readDouble();
         dx = stream.readDouble();
         dy = stream.readDouble();
         angle = stream.readDouble();
 
         left = stream.readBoolean();
         right = stream.readBoolean();
         backwards = stream.readBoolean();
         forward = stream.readBoolean();
         shooting = stream.readBoolean();
 
         weaponIndex = stream.readInt();
     }
 
     /**
      * Creates <code>this</code> from a stream for client/server transmission.
      * 
      * @param stream    the stream to read from (sent by the server)
      * @throws java.io.IOException 
      * @since December 30, 2007
      */
     public Ship( DataInputStream stream ) throws IOException
     {
         id = stream.readInt();
 
         restorePosition( stream );
 
         invincibilityCount = stream.readInt();
 
         myColor = Game.getInstance().PLAYER_COLORS[stream.readInt()];
 
         name = stream.readUTF();
         livesLeft = stream.readInt();
         weaponIndex = stream.readInt();
         numAsteroidsKilled = stream.readInt();
         numShipsKilled = stream.readInt();
 
         // Apply basic construction.        
         double fadePct = 0.6;
         myInvicibleColor = new Color( (int) ( myColor.getRed() * fadePct ), (int) ( myColor.getGreen() * fadePct ), (int) ( myColor.getBlue() * fadePct ) );
         allWeapons = new WeaponManager[3];
         allWeapons[0] = new MissileManager();
         allWeapons[1] = new BulletManager();
         allWeapons[2] = new MineManager();
     }
 
     public double getDx()
     {
         return dx;
     }
 
     public double getDy()
     {
         return dy;
     }
 
     public int getWeaponIndex()
     {
         return weaponIndex;
     }
 
     public ConcurrentLinkedQueue<WeaponManager> getManagers()
     {
         ConcurrentLinkedQueue<WeaponManager> c = new ConcurrentLinkedQueue<WeaponManager>();
         for ( WeaponManager w : allWeapons )
             c.add( w );
         return c;
     }
 }
