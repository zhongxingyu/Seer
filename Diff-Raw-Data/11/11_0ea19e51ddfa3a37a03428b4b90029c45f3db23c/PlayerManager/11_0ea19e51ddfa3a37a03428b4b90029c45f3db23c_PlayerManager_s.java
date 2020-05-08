 package combat;
 
 /**
  * @author DevC
  * @version $Id: PlayerManager.java,v 1.19 2012/04/08 03:50:18 DevA Exp $
  * 
  *          PlayerManager class; this class passes ticks, preticks, and commands
  *          to the
  *          players and bullets.
  * 
  *          Revision History:
  *          $Log: PlayerManager.java,v $
  *          Revision 1.19 2012/04/08 03:50:18 DevA
  *          Cleaned up the code to run with Java 1.6: removed unused imports,
  *          fixed some UI focus issues (introduced by new focus "features" in
  *          Java since
  *          our original implementation), and made the CommandInterpreter not a
  *          Singleton
  * 
  *          Revision 1.18 2003/05/30 19:48:15 DevB
  *          Reformatted.
  * 
  *          Revision 1.17 2000/05/17 16:37:35 DevA
  *          Dynamically deciding what BulletOffset to use
  * 
  *          Revision 1.16 2000/05/11 22:12:07 DevC
  *          commented, cleaned up the code & fixed bullet firing
  *          (somewhat; there is a better way to fix it, but
  *          it'd take more time than I have right now)
  * 
  *          Revision 1.15 2000/05/11 07:12:04 DevA
  *          Got rid of code that was originally supposed to help cleanup
  *          after a round, but, instead, was just causing a phantom
  *          player to stick around. (the "last" boolean was useless)
  * 
  *          Revision 1.14 2000/05/11 06:29:12 DevA
  *          Removed most debugs from the system and now everything works
  *          great on a New Game or New Round...at least as far as the user
  *          can tell. I think that whichever Player won a round is left
  *          hanging a bit under the hood. It never redraws, but it
  *          never really goes away. Seems like a waste of processor time
  *          so I'm stil trying to figure this out.
  * 
  *          Revision 1.13 2000/05/09 16:35:27 DevA
  *          Threaded to wait until it's player has become inactive.
  *          This is the first step in keeping score. Everytime a PlayerManager
  *          dies, the opposite player's score will be incremented.
  * 
  *          Revision 1.12 2000/05/09 16:17:05 DevA
  *          Bullets fire and go in the correct direction :)
  * 
  *          Revision 1.11 2000/05/09 15:47:39 DevA
  *          Now bullets fire. But only once. Need to fix this.
  * 
  *          Revision 1.10 2000/05/09 14:57:56 DevA
  *          Tells a Player to stay() on a tick if now command was
  *          received.
  * 
  *          Revision 1.9 2000/05/09 06:08:49 DevC
  *          added check to ensure that a vaible command is given in move
  * 
  *          Revision 1.8 2000/05/09 04:48:44 DevA
  *          Debugs and intializing cmds
  * 
  *          Revision 1.7 2000/05/09 04:23:24 DevC
  *          fixed initialization errors
  * 
  *          Revision 1.6 2000/05/08 22:22:29 DevA
  *          Just made error msg more useful. "ERROR" now says what
  *          class is having the issue.
  * 
  *          Revision 1.5 2000/05/08 21:23:19 DevC
  *          fixed constructor; fixed firing mechanism.
  * 
  *          Revision 1.4 2000/05/05 20:53:20 DevC
  *          Made the object timed by implementing the timed interface;
  *          This allows it to get commands on ticks and preticks
  * 
  *          Revision 1.3 2000/05/05 04:46:48 DevC
  *          implemented the constructors and logic to call player;
  *          needs only to have exact player calls put in.
  *          Compiles with CI and stub Player
  * 
  *          Revision 1.2 2000/05/04 01:21:10 DevC
  *          wrote portions of constructor, a few simple methods.
  * 
  *          Revision 1.1 2000/05/04 01:08:11 DevC
  *          Initial revision
  * 
  */
 
 import java.awt.Point;
 import java.awt.Rectangle;
 
 public class PlayerManager implements Timed {
     private int myNum; // the player number I am
     private CommandInterpreter ci; // the command interpreter form which I get
                                    // keyboard events
 
     private Player myPlayer; // the player object I control
     private Bullet myBullet; // the bullet object for the player
 
     private int[] cmds = new int[5]; // my commands
 
     private boolean alive = true; // indicates if i am alive
     private boolean end = false; // tells me when i've died
 
     private static int BulletOffset = 40; // how i figure out where to place the
                                           // bullet
 
     public boolean isAlive() {
         return this.myPlayer.isActive();
     }
 
     /**
      * Constructor
      * 
      * @param playerNum what player I am
      * @param up key command for foreward
      * @param down key command for backward
      * @param right key command for right
      * @param left key command for left
      * @param fire key command for fire
      * @param player my player object
      * @param bullet my bullet object
      */
     public PlayerManager(int playerNum, int up, int down, int right, int left, int fire, Player player, Bullet bullet,
             CommandInterpreter ci) {
         this.ci = ci;
 
         // assign the key commands
         cmds[0] = up;
         cmds[1] = down;
         cmds[2] = right;
         cmds[3] = left;
         cmds[4] = fire;
 
         // assign the player number for this instance of the PlayerManager
         myNum = playerNum;
         System.out.println("New PlayerManager for player " + playerNum);
 
         // register with the CommandInterpreter
         ci.register(myNum, cmds);
 
         // assign my player and bullet objects
         myPlayer = player;
         Rectangle tmp = myPlayer.getBoundingBox();
         int x = tmp.width;
         int y = tmp.height;
         if (x > y)
             BulletOffset = x + 1;
         else
             BulletOffset = y + 1;
         myBullet = bullet;
     }
 
     /**
      * Sends a command to the player
      * 
      * @return True if a move was actually made.
      */
     private boolean move() {
         int cmd = 0; // the command I got
         boolean result = false;
 
         // get the command from the command interpreter
         cmd = ci.getCommand(myNum);
 
         if (!(cmd == 0)) {
             // determine if the key pressed is of interest and assign it to the
             // correct player
             if (cmd == cmds[0]) {
                 myPlayer.moveForward();
                 result = true;
             } else if (cmd == cmds[1]) {
                 myPlayer.moveBackward();
                 result = true;
             } else if (cmd == cmds[3]) {
                 myPlayer.turnLeft();
                 result = true;
             } else if (cmd == cmds[2]) {
                 myPlayer.turnRight();
                 result = true;
             } else if (cmd == cmds[4]) {
                 // get the location of my player
                 Point loc = myPlayer.getLocation();
 
                 // Get the direction this player is going
                 int dir = myPlayer.getDirection();
                 Point bulletStart = loc;
 
                 switch (dir) {
                 // start the bullet, giving it an offset enough so I'm not
                 // shooting myself
                     case (1):
                         bulletStart = new Point(loc.x, loc.y - BulletOffset);
                         break;
                     case (2):
                         bulletStart = new Point(loc.x + BulletOffset, loc.y - BulletOffset);
                         break;
                     case (3):
                         bulletStart = new Point(loc.x + BulletOffset, loc.y);
                         break;
                     case (4):
                         bulletStart = new Point(loc.x + BulletOffset, loc.y + BulletOffset);
                         break;
                     case (5):
                         bulletStart = new Point(loc.x, loc.y + BulletOffset);
                         break;
                     case (6):
                         bulletStart = new Point(loc.x - BulletOffset, loc.y + BulletOffset);
                         break;
                     case (7):
                         bulletStart = new Point(loc.x - BulletOffset, loc.y);
                         break;
                     case (8):
                         bulletStart = new Point(loc.x - BulletOffset, loc.y - BulletOffset);
                         break;
                     default:
                 }
 
                 // place the bullet on the screen
                 myBullet.place(bulletStart, dir);
             }
 
             else {
             }
         }
 
         return result;
     }
 
     /**
      * Here for the implementation of the Timed interface.
      */
     public void pretick() {
     }
 
     /**
      * Here for the implementation of the Timed interface;
      * controls the movement of the Player.
      */
     public void tick() {
         // on a tick, move the bullet foreward and make sure i repaint myself
         myBullet.moveForward();
         if (!move())
             myPlayer.stay();
 
        if (!this.myPlayer.isActive())
             System.err.println("Player #" + myNum + " lost.");
     }
 
     /**
      * Allows Game to set the commands for this player
      * 
      * @param cmds[] Player's command array.
      */
     public void setCommands(int[] cmds) {
         // sets the commands i want to listen on; not used currently, would be
         // useful if I could get the keyMapping correctly
         this.cmds = cmds;
         ci.register(myNum, cmds);
     }
 
     /**
      * Ends this PlayerManager
      */
     public void end() {
         // indicate that I am dead
         myPlayer.end();
         alive = false;
         end = true;
     }
 }
