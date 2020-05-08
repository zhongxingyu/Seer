 /*
  * Dog - A project for making highly scalable non-clustered game and simulation environments.
  * Copyright (C) 2009-2010 BlinzProject <gtalent2@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 2 as
  * published by the Free Software Foundation.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.blinz.dog.zone;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 import net.blinz.dog.input.ClickEvent;
 import net.blinz.dog.input.KeyEvent;
 import net.blinz.dog.input.MouseEvent;
 import net.blinz.dog.input.MouseWheelEvent;
 import net.blinz.dog.util.User;
 
 /**
  * Contains lists sprites listening for input from specific Users.
  * @author Blinz
  */
 final class UserListenerCatalog {
 
     /**
      * Contains a list of sprites that listen to the input of a certain User.
      * @author Blinz
      */
     final class UserListenerList {
 
         /**
          * Used for when the Zone is paused.
          */
         private final Vector<BaseSprite> dummyList = new Vector<BaseSprite>();
         /**
          * The primary sprite list.
          */
         private final Vector<BaseSprite> sprites = new Vector<BaseSprite>();
         private Vector<BaseSprite> inputSprites;
         private final Vector<ClickEvent> buttonClicks = new Vector<ClickEvent>();
         private final Vector<MouseEvent> buttonPresses = new Vector<MouseEvent>();
         private final Vector<MouseEvent> buttonReleases = new Vector<MouseEvent>();
         private final Vector<KeyEvent> keyPresses = new Vector<KeyEvent>();
         private final Vector<KeyEvent> keyReleases = new Vector<KeyEvent>();
         private final Vector<KeyEvent> keyTypes = new Vector<KeyEvent>();
         private final Vector<MouseWheelEvent> wheelScrolls = new Vector<MouseWheelEvent>();
         /**
          * Count of how many Cameras accessing this UserListenerList.
          */
         private int cameraCount = 0;
 
         /**
          * Constructor
          */
         private UserListenerList() {
         }
 
         /**
          * Adds the given event to be passed on to concerned sprites later in the
          * cycle.
          * @param e the event to pass on
          */
         public final void buttonClick(final ClickEvent e) {
             buttonClicks.add(e);
         }
 
         /**
          * Adds the given event to be passed on to concerned sprites later in the
          * cycle.
          * @param e the event to pass on
          */
         public final void buttonPress(final MouseEvent e) {
             buttonPresses.add(e);
         }
 
         /**
          * Adds the given event to be passed on to concerned sprites later in the
          * cycle.
          * @param e the event to pass on
          */
         public final void buttonRelease(final MouseEvent e) {
             buttonReleases.add(e);
         }
 
         /**
          * Adds the given event to be passed on to concerned sprites later in the
          * cycle.
          * @param e the event to pass on
          */
         public final void keyPressed(final KeyEvent e) {
             keyPresses.add(e);
         }
 
         /**
          * Adds the given event to be passed on to concerned sprites later in the
          * cycle.
          * @param e the event to pass on
          */
         public final void keyReleased(final KeyEvent e) {
             keyReleases.add(e);
         }
 
         /**
          * Adds the given event to be passed on to concerned sprites later in the
          * cycle.
          * @param e the event to pass on
          */
         public final void keyTyped(final KeyEvent e) {
             keyTypes.add(e);
         }
 
         /**
          * Adds the given event to be passed on to concerned sprites later in the
          * cycle.
          * @param e the event to pass on
          */
         public final void wheelScroll(final MouseWheelEvent e) {
             wheelScrolls.add(e);
         }
 
         /**
          * Trims the lists in this UserListenerCatalog down to size.
          */
         private final void trimLists() {
             sprites.trimToSize();
             buttonClicks.trimToSize();
             buttonPresses.trimToSize();
             buttonReleases.trimToSize();
             keyPresses.trimToSize();
             keyReleases.trimToSize();
             keyTypes.trimToSize();
             wheelScrolls.trimToSize();
         }
 
         /**
          * Initializes this UserListenerCatalog.
          */
         private final void init() {
             inputSprites = paused ? dummyList : sprites;
         }
 
         /**
          * Suspend reception of user input to the sprites.
          */
         private final void pause() {
             inputSprites = dummyList;
         }
 
         /**
          * Resume reception of user input to the sprites.
          */
         private final void unpause() {
             inputSprites = sprites;
         }
 
         /**
          * Indicates whether or no this UserListernerList is still needed.
          * @return true if this list has no Cameras or sprites and thus is needed, false otherwise
          */
         private final boolean dead() {
             return cameraCount == 0 && inputSprites.isEmpty();
         }
 
         /**
          * Adds the given sprite to this list.
          * For use by UserListenerCatalog.
          * @param sprite the sprite to be added to this UserListenerList
          */
         private final void add(final BaseSprite sprite) {
             inputSprites.add(sprite);
         }
 
         /**
          * Removes the given sprite from this list.
          * For use by UserListenerCatalog.
          * @param sprite the sprite to be removed from this UserListenerList
          */
         private final void remove(final BaseSprite sprite) {
             inputSprites.remove(sprite);
         }
 
         /**
          * Updates this UserListenerCatalog, telling all sprites about relevant input.
          */
         private final void update() {
             int end = buttonClicks.size() - 1;
             while (end != -1) {
                 final ClickEvent e = buttonClicks.remove(end--);
                 for (int i = 0; i < inputSprites.size(); i++) {
                     inputSprites.get(i).buttonClicked(e);
                 }
             }
             end = buttonPresses.size() - 1;
             while (end != -1) {
                 final MouseEvent e = buttonPresses.remove(end--);
                 for (int i = 0; i < inputSprites.size(); i++) {
                     inputSprites.get(i).buttonPressed(e);
                 }
             }
             end = buttonReleases.size() - 1;
             while (end != -1) {
                 final MouseEvent e = buttonReleases.remove(end--);
                 for (int i = 0; i < inputSprites.size(); i++) {
                     inputSprites.get(i).buttonReleased(e);
                 }
             }
             end = keyPresses.size() - 1;
             while (end != -1) {
                 final KeyEvent e = keyPresses.remove(end--);
                 for (int i = 0; i < inputSprites.size(); i++) {
                     inputSprites.get(i).keyPressed(e);
                 }
             }
             end = keyReleases.size() - 1;
             while (end != -1) {
                 final KeyEvent e = keyReleases.remove(end--);
                 for (int i = 0; i < inputSprites.size(); i++) {
                     inputSprites.get(i).keyReleased(e);
                 }
             }
             end = keyTypes.size() - 1;
             while (end != -1) {
                 final KeyEvent e = keyTypes.remove(end--);
                 for (int i = 0; i < inputSprites.size(); i++) {
                     inputSprites.get(i).keyTyped(e);
                 }
             }
             end = wheelScrolls.size() - 1;
             while (end != -1) {
                 final MouseWheelEvent e = wheelScrolls.remove(end--);
                 for (int i = 0; i < inputSprites.size(); i++) {
                     inputSprites.get(i).mouseWheelScroll(e);
                 }
             }
         }
     }
 
     /**
      * Internal class of objects used for clean addition and removal of User:sprite pairs.
      */
     private final class Pair {
 
         private User user;
         private BaseSprite sprite;
 
         /**
          * Constructor
          * @param user the User to associate with the given sprite
          * @param sprite the sprite to associate with the given User
          */
         private Pair(final User user, final BaseSprite sprite) {
             this.user = user;
             this.sprite = sprite;
         }
     }
     private boolean paused = false;
     private final Hashtable<User, UserListenerList> userListeners = new Hashtable<User, UserListenerList>();
     private final Vector<Pair> toRemove = new Vector<Pair>();
     private final Vector<Pair> toAdd = new Vector<Pair>();
 
     protected final void update() {
         //remove old pairs
         Pair current = null;
         while ((current = nextToRemove()) != null) {
             final UserListenerList list = userListeners.get(current.user);
             list.remove(current.sprite);
             if (list.dead()) {
                 userListeners.remove(current.user);
             }
         }
 
         //add new pairs
         while ((current = nextToAdd()) != null) {
             UserListenerList list;
             do {
                 list = userListeners.get(current.user);
                 if (list == null) {
                     list = fetchList(current.user, 0);
                 }
                 list.add(current.sprite);
             } while (!userListeners.contains(list));
         }
 
         final Enumeration<UserListenerList> list = userListeners.elements();
         while (list.hasMoreElements()) {
             list.nextElement().update();
         }
     }
 
     /**
      * Constructor
      */
     UserListenerCatalog() {
     }
 
     /**
      * Trims the lists under this object and its subordinates down to size.
      */
     final void trimLists() {
         final Enumeration<UserListenerList> list = userListeners.elements();
         while (list.hasMoreElements()) {
             list.nextElement().trimLists();
         }
     }
 
     /**
      * Checks out the list associated with the given User.
      * checkIn should be called when the list is no longer needed.
      * For use by Cameras.
      * @param user the User who's list is to be returned
      * @return the sprite list associated with the given User
      */
     final UserListenerList checkOut(final User user) {
         UserListenerList list;
         do {
             list = userListeners.get(user);
             if (list == null) {
                 list = fetchList(user, 1);
             }
         } while (!userListeners.containsKey(user));
         return list;
     }
 
     /**
      * Used by Zone to pause sprites recieving of user input while the Zone is paused.
      */
     final void pause() {
         paused = true;
         final Enumeration<UserListenerList> list = userListeners.elements();
         while (list.hasMoreElements()) {
             list.nextElement().pause();
         }
     }
 
     /**
      * Used by Zone to unpause sprites recieving of user input while the Zone is paused.
      */
     final void unpause() {
         paused = false;
         Enumeration<UserListenerList> list = userListeners.elements();
         while (list.hasMoreElements()) {
             list.nextElement().unpause();
         }
     }
 
     /**
      * Checks in the UserListenerList for the given User, allowing the UserListenerCatalog
      * to determine whether or not the list is still in use.
      * For use by Cameras.
      * @param user the User for whom the UserListenerList is being a checked in
      */
     final void checkIn(final User user) {
         final UserListenerList list = userListeners.get(user);
        if (list == null) {
            return;
        }
         list.cameraCount--;
         if (list.dead()) {
             userListeners.remove(user);
         }
     }
 
     /**
      * Adds the given sprite to the list for the given User.
      * @param user the User of the the Pair to be added
      * @param sprite the sprite of the Pair to be added
      */
     final void add(final User user, final BaseSprite sprite) {
         toAdd.add(new Pair(user, sprite));
     }
 
     /**
      * Removes the given sprite to the list for the given User.
      * @param user the User of the the Pair to be removed
      * @param sprite the sprite of the Pair to be removed
      */
     final void remove(final User user, final BaseSprite sprite) {
         toRemove.add(new Pair(user, sprite));
     }
 
     /**
      * Gets the next Pair to remove.
      * @return the next Pair to remove
      */
     private final Pair nextToRemove() {
         if (toRemove.isEmpty()) {
             return null;
         }
         return toRemove.remove(0);
     }
 
     /**
      * Gets the next Pair to add.
      * @return the next sprite to add
      */
     private final Pair nextToAdd() {
         if (toAdd.isEmpty()) {
             return null;
         }
         return toAdd.remove(0);
     }
 
     /**
      * Adds a sprite list for the given User if it does not exist, returns the existing
      * one if it does.
      * @param user
      * @param cameras the number of additional Cameras to access this list.
      */
     private final synchronized UserListenerList fetchList(final User user, final int cameras) {
         if (!userListeners.containsKey(user)) {
             final UserListenerList list = new UserListenerList();
             list.cameraCount += cameras;
             userListeners.put(user, list);
             list.init();
             return list;
         } else {
             return userListeners.get(user);
         }
     }
 }
