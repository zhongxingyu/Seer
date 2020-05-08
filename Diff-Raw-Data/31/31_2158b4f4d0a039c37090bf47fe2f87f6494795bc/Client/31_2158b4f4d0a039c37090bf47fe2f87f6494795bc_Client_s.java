 /*
 Copyright (C) 2004 Geoffrey Alan Washburn
    
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
    
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
    
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 USA.
 */
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Iterator;
 
 /**
  * An abstract class for clients in a maze.
  *
  * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
  * @version $Id: Client.java 343 2004-01-24 03:43:45Z geoffw $
  */
 public abstract class Client {
     private static final Logger logger = LoggerFactory.getLogger(MazeImpl.class);
 
     /**
      * Register this {@link Client} as being contained by the specified
      * {@link Maze}.  Naturally a {@link Client} cannot be registered with
      * more than one {@link Maze} at a time.
      *
      * @param maze The {@link Maze} in which the {@link Client} is being
      *             placed.
      */
     public void registerMaze(Maze maze) {
         assert (maze != null);
         assert (this.maze == null);
         this.maze = maze;
     }
 
     /**
      * Inform the {@link Client} that it has been taken out of the {@link Maze}
      * in which it is located.  The {@link Client} must already be registered
      * with a {@link Maze} before this can be called.
      */
     public void unregisterMaze() {
         assert (maze != null);
         this.maze = null;
     }
 
     /**
      * Get the name of this {@link Client}.
      *
      * @return A {@link String} naming the {@link Client}.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Obtain the location of this {@link Client}.
      *
      * @return A {@link Point} specifying the location of the {@link Client}.
      */
     public Point getPoint() {
         assert (maze != null);
         return maze.getClientPoint(this);
     }
 
     /**
      * Find out what direction this {@link Client} is presently facing.
      *
      * @return A Cardinal {@link Direction}.
      */
     public Direction getOrientation() {
         assert (maze != null);
         return maze.getClientOrientation(this);
     }
 
     /**
      * Add an object to be notified when this {@link Client} performs an
      * action.
      *
      * @param cl An object that implementing the {@link ClientListener cl}
      *           interface.
      */
     public void addClientListener(ClientListener cl) {
         assert (cl != null);
         listenerSet.add(cl);
     }
 
     /**
      * Remove an object from the action notification queue.
      *
      * @param cl The {@link ClientListener} to remove.
      */
     public void removeClientListener(ClientListener cl) {
         listenerSet.remove(cl);
     }
 
     /* Internals ******************************************************/
 
     /**
      * The maze where the client is located.  <code>null</code> if not
      * presently in a maze.
      */
     protected Maze maze = null;
 
     /**
      * Maintain a set of listeners.
      */
     private Set listenerSet = new HashSet();
 
     /**
      * Name of the client.
      */
     private String name = null;
 
     /**
      * Create a new client with the specified name.
      */
     protected Client(String name) {
         assert (name != null);
         this.name = name;
     }
 
     /**
      * Move the client forward.
      *
      * @return <code>true</code> if move was successful, otherwise <code>false</code>.
      */
     protected boolean forward() {
         assert (maze != null);
 
         if (maze.isClientForwardValid(this)) {
             logger.info("isClientForwardValid is true");
             notifyServerMoveForward();
             return true;
         } else {
             logger.info("isClientForwardValid is false");
             return false;
         }
     }
 
     /**
      * Move the client backward.
      *
      * @return <code>true</code> if move was successful, otherwise <code>false</code>.
      */
     protected boolean backup() {
         assert (maze != null);
 
         if (maze.isClientBackwardValid(this)) {
             logger.info("isClientBackwardValid is true");
             notifyServerMoveBackward();
             return true;
         } else {
             logger.info("isClientBackwardValid is false");
             return false;
         }
     }
 
     private void notifyServerMoveForward() {
         MazewarPacket toServer = new MazewarPacket();
 
         Point oldPoint = getPoint();
         Direction d = getOrientation();
         DirectedPoint newDp = new DirectedPoint(oldPoint.move(d), d);
 
         logger.info("moveClient old: " + getName() +
                 "\n\tto old X: " + oldPoint.getX() +
                 "\n\tto old Y: " + oldPoint.getY() +
                 "\n\told orientation : " + d
         );
 
         logger.info("moveClient: " + getName() +
                 "\n\tto X: " + newDp.getX() +
                 "\n\tto Y: " + newDp.getY() +
                 "\n\torientation : " + newDp.getDirection()
         );
 
         toServer.owner = getName();
         toServer.type = MazewarPacket.MOVE_FORWARD;
         toServer.mazeMap.put(getName(), newDp);
 
         synchronized (Mazewar.out) {
             try {
                 Mazewar.out.writeObject(toServer);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private void notifyServerMoveBackward() {
         MazewarPacket toServer = new MazewarPacket();
 
         Point oldPoint = getPoint();
         Direction d = getOrientation();
         DirectedPoint newDp = new DirectedPoint(oldPoint.move(d.invert()), d);
 
         logger.info("moveClient old: " + getName() +
                 "\n\tto old X: " + oldPoint.getX() +
                 "\n\tto old Y: " + oldPoint.getY() +
                 "\n\told orientation : " + d
         );
 
         logger.info("moveClient: " + getName() +
                 "\n\tto X: " + newDp.getX() +
                 "\n\tto Y: " + newDp.getY() +
                 "\n\torientation : " + newDp.getDirection()
         );
 
         toServer.owner = getName();
         toServer.type = MazewarPacket.MOVE_BACKWARD;
         toServer.mazeMap.put(getName(), newDp);
 
         synchronized (Mazewar.out) {
             try {
                 Mazewar.out.writeObject(toServer);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Turn the client ninety degrees counter-clockwise.
      */
     protected void turnLeft() {
         assert (maze != null);
 
         logger.info("Notify server turning left");
         notifyServerTurnLeft();
     }
 
     /**
      * Turn the client ninety degrees clockwise.
      */
     protected void turnRight() {
         assert (maze != null);
 
         logger.info("Notify server turning right");
         notifyServerTurnRight();
     }
 
     private void notifyServerTurnLeft() {
         MazewarPacket toServer = new MazewarPacket();
 
         Point oldPoint = getPoint();
         Direction d = getOrientation();
         DirectedPoint newDp = new DirectedPoint(oldPoint, d.turnLeft());
 
         logger.info("rotateClient old: " + getName() +
                 "\n\tto old X: " + oldPoint.getX() +
                 "\n\tto old Y: " + oldPoint.getY() +
                 "\n\told orientation : " + d
         );
 
         logger.info("rotateClient: " + getName() +
                 "\n\tto X: " + newDp.getX() +
                 "\n\tto Y: " + newDp.getY() +
                 "\n\torientation : " + newDp.getDirection()
         );
 
         toServer.owner = getName();
         toServer.type = MazewarPacket.TURN_LEFT;
         toServer.mazeMap.put(getName(), newDp);
 
         synchronized (Mazewar.out) {
             try {
                 Mazewar.out.writeObject(toServer);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private void notifyServerTurnRight() {
         MazewarPacket toServer = new MazewarPacket();
 
         Point oldPoint = getPoint();
         Direction d = getOrientation();
         DirectedPoint newDp = new DirectedPoint(oldPoint, d.turnRight());
 
         logger.info("rotateClient old: " + getName() +
                 "\n\tto old X: " + oldPoint.getX() +
                 "\n\tto old Y: " + oldPoint.getY() +
                 "\n\told orientation : " + d
         );
 
         logger.info("rotateClient: " + getName() +
                 "\n\tto X: " + newDp.getX() +
                 "\n\tto Y: " + newDp.getY() +
                 "\n\torientation : " + newDp.getDirection()
         );
 
         toServer.owner = getName();
         toServer.type = MazewarPacket.TURN_RIGHT;
         toServer.mazeMap.put(getName(), newDp);
 
         synchronized (Mazewar.out) {
             try {
                 Mazewar.out.writeObject(toServer);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Fire a projectile.
      *
      * @return <code>true</code> if a projectile was successfully launched, otherwise <code>false</code>.
      */
     protected boolean fire() {
         assert (maze != null);
 
         if (maze.isClientFireValid(this)) {
             notifyServerFire();
             logger.info("Client fired: " + getName() +
                     "\n\tSender: " + Mazewar.myName);
             return true;
         } else {
             logger.info("Cant fire");
             return false;
         }

//        if (maze.clientFire(this)) {
//            notifyFire();
//            return true;
//        } else {
//            return false;
//        }
     }
 
     private void notifyServerFire() {
         MazewarPacket toServer = new MazewarPacket();
 
         toServer.owner = getName();
         toServer.type = MazewarPacket.FIRE;
 
         synchronized (Mazewar.out) {
             try {
                 Mazewar.out.writeObject(toServer);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Quit the client from the game.
      */
     protected void quit() {
         assert (maze != null);
 
         logger.info("Notify server quiting");
         notifyServerQuit();
     }
 
     private void notifyServerQuit() {
         MazewarPacket toServer = new MazewarPacket();
 
         toServer.owner = getName();
         toServer.type = MazewarPacket.QUIT;
 
         synchronized (Mazewar.out) {
             try {
                 Mazewar.out.writeObject(toServer);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Notify listeners that the client moved forward.
      */
     public void notifyMoveForward() {
         notifyListeners(ClientEvent.moveForward);
     }
 
     /**
      * Notify listeners that the client moved backward.
      */
     public void notifyMoveBackward() {
         notifyListeners(ClientEvent.moveBackward);
     }
 
     /**
      * Notify listeners that the client turned right.
      */
     public void notifyTurnRight() {
         notifyListeners(ClientEvent.turnRight);
     }
 
     /**
      * Notify listeners that the client turned left.
      */
     public void notifyTurnLeft() {
         notifyListeners(ClientEvent.turnLeft);
     }
 
     /**
      * Notify listeners that the client fired.
      */
     public void notifyFire() {
         notifyListeners(ClientEvent.fire);
     }
 
     /**
      * Send a the specified {@link ClientEvent} to all registered listeners
      *
      * @param ce Event to be sent.
      */
     private void notifyListeners(ClientEvent ce) {
         assert (ce != null);
         Iterator i = listenerSet.iterator();
         while (i.hasNext()) {
             Object o = i.next();
             assert (o instanceof ClientListener);
             ClientListener cl = (ClientListener) o;
             cl.clientUpdate(this, ce);
         }
     }
 
 }
