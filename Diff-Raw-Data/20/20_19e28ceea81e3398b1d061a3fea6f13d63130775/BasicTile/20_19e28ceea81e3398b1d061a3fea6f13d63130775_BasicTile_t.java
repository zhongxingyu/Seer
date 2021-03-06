 /*
  Copyright (c) 2006 Sun Microsystems, Inc., 4150 Network Circle, Santa
  Clara, California 95054, U.S.A. All rights reserved.
  
  Sun Microsystems, Inc. has intellectual property rights relating to
  technology embodied in the product that is described in this document.
  In particular, and without limitation, these intellectual property rights
  may include one or more of the U.S. patents listed at
  http://www.sun.com/patents and one or more additional patents or pending
  patent applications in the U.S. and in other countries.
  
  U.S. Government Rights - Commercial software. Government users are subject
  to the Sun Microsystems, Inc. standard license agreement and applicable
  provisions of the FAR and its supplements.
  
  This distribution may include materials developed by third parties.
  
  Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered
  trademarks of Sun Microsystems, Inc. in the U.S. and other countries.
  
  UNIX is a registered trademark in the U.S. and other countries, exclusively
  licensed through X/Open Company, Ltd.
  
  Products covered by and information contained in this service manual are
  controlled by U.S. Export Control laws and may be subject to the export
  or import laws in other countries. Nuclear, missile, chemical biological
  weapons or nuclear maritime end uses or end users, whether direct or
  indirect, are strictly prohibited. Export or reexport to countries subject
  to U.S. embargo or to entities identified on U.S. export exclusion lists,
  including, but not limited to, the denied persons and specially designated
  nationals lists is strictly prohibited.
  
  DOCUMENTATION IS PROVIDED "AS IS" AND ALL EXPRESS OR IMPLIED CONDITIONS,
  REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  ARE DISCLAIMED, EXCEPT TO THE EXTENT THAT SUCH DISCLAIMERS ARE HELD TO BE
  LEGALLY INVALID.
  
  Copyright © 2006 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
  California 95054, Etats-Unis. Tous droits réservés.
  
  Sun Microsystems, Inc. détient les droits de propriété intellectuels
  relatifs à la technologie incorporée dans le produit qui est décrit dans
  ce document. En particulier, et ce sans limitation, ces droits de
  propriété intellectuelle peuvent inclure un ou plus des brevets américains
  listés à l'adresse http://www.sun.com/patents et un ou les brevets
  supplémentaires ou les applications de brevet en attente aux Etats -
  Unis et dans les autres pays.
  
  Cette distribution peut comprendre des composants développés par des
  tierces parties.
  
  Sun, Sun Microsystems, le logo Sun et Java sont des marques de fabrique
  ou des marques déposées de Sun Microsystems, Inc. aux Etats-Unis et dans
  d'autres pays.
  
  UNIX est une marque déposée aux Etats-Unis et dans d'autres pays et
  licenciée exlusivement par X/Open Company, Ltd.
  
  see above Les produits qui font l'objet de ce manuel d'entretien et les
  informations qu'il contient sont regis par la legislation americaine en
  matiere de controle des exportations et peuvent etre soumis au droit
  d'autres pays dans le domaine des exportations et importations.
  Les utilisations finales, ou utilisateurs finaux, pour des armes
  nucleaires, des missiles, des armes biologiques et chimiques ou du
  nucleaire maritime, directement ou indirectement, sont strictement
  interdites. Les exportations ou reexportations vers des pays sous embargo
  des Etats-Unis, ou vers des entites figurant sur les listes d'exclusion
  d'exportation americaines, y compris, mais de maniere non exclusive, la
  liste de personnes qui font objet d'un ordre de ne pas participer, d'une
  facon directe ou indirecte, aux exportations des produits ou des services
  qui sont regi par la legislation americaine en matiere de controle des
  exportations et la liste de ressortissants specifiquement designes, sont
  rigoureusement interdites.
  
  LA DOCUMENTATION EST FOURNIE "EN L'ETAT" ET TOUTES AUTRES CONDITIONS,
  DECLARATIONS ET GARANTIES EXPRESSES OU TACITES SONT FORMELLEMENT EXCLUES,
  DANS LA MESURE AUTORISEE PAR LA LOI APPLICABLE, Y COMPRIS NOTAMMENT TOUTE
  GARANTIE IMPLICITE RELATIVE A LA QUALITE MARCHANDE, A L'APTITUDE A UNE
  UTILISATION PARTICULIERE OU A L'ABSENCE DE CONTREFACON.
 */
 
 
 /*
  * BasicTile.java
  *
  * Created by: seth proctor (sp76946)
  * Created on: Sun Mar  5, 2006	 8:30:09 PM
  * Desc: 
  *
  */
 
 package com.sun.gi.apps.hack.server.level;
 
 import com.sun.gi.logic.GLOReference;
 import com.sun.gi.logic.SimTask;
 
 import com.sun.gi.apps.hack.server.CharacterManager;
 import com.sun.gi.apps.hack.server.Item;
 
 import com.sun.gi.apps.hack.server.level.LevelBoard.ActionResult;
 
 import java.io.Serializable;
 
 
 /**
  * This is an abstract implementation of <code>Tile</code> that provides the
  * base for <code>PassableTile</code>, <code>ImpassableTile</code>, etc. It
  * maintains the rule that only one character and one item may be on the
  * tile at any time.
  *
  * @since 1.0
  * @author Seth Proctor
  */
 public abstract class BasicTile implements Tile
 {
 
     // the id of this tile
     private int id;
 
     // the character that is currently on this space, if any
     private GLOReference<? extends CharacterManager> mgrRef;
 
     // the item on this space, if any
     private GLOReference<? extends Item> itemRef;
 
     /**
      * Creates an instance of <code>BasicTile</code>.
      *
      * @param id the tile's identifier
      */
     protected BasicTile(int id) {
         this.id = id;
         this.mgrRef = null;
         this.itemRef = null;
     }
 
     /**
      * Returns the identifier for this tile.
      *
      * @return the tile's identifier
      */
     public int getID() {
         return id;
     }
 
     /**
      * Returns a stack of identifiers, specifying everything on this
      * <code>Tile</code>.
      *
      * @return the set of identifiers for the things at this space
      */
     public int [] getIdStack() {
         // the array of ids that we will return
         int [] ids = null;
 
         // NOTE: This is a fairly un-optimized approach to generating this
         // stack, and since this is done reasonably frequently, it would
         // be nice to re-visit this method
 
         // if there's no item, create an array with just the tile's id,
         // otherwise put both in an array
         if (itemRef == null)
             ids = new int [] {id};
         else
             ids = new int [] {id, itemRef.peek(SimTask.getCurrent()).getID()};
 
         // if there is a character here, create a new array that's 1 index
         // bigger, and put the character at the end
         if (mgrRef != null) {
             int [] tmp = new int [ids.length + 1];
             for (int i = 0; i < ids.length; i++)
                 tmp[i] = ids[i];
             tmp[ids.length] = mgrRef.peek(SimTask.getCurrent()).
                 getCurrentCharacter().getID();
             ids = tmp;
         }
 
         return ids;
     }
 
     /**
      * Checks if the there is anything currently occupying this tile that
      * would keep the character from occupying it.
      *
      * @param mgrRef the manager for a character
      *
      * @return whether or not the character can occupy this tile
      */
     public boolean canOccupy(GLOReference<? extends CharacterManager> mgrRef) {
         // if the space isn't passible, then it can't be occupied
         if (! isPassable(mgrRef))
             return false;
 
         // if there's a character here, then the space can't be occupied
         // NOTE: if we allow passable characters in the future, then this
         // needs to be updated
        if (this.mgrRef != null)
             return false;
 
         // NOTE: if we allow items that block movement, then this will need
         // to be updated
 
         return true;
     }
 
     /**
      * Adds the given character to this tile if possible. This succeeds
      * if there isn't currently a character on this tile.
      *
      * @param mgrRef the manager for a character
      *
      * @return whether or not the character was added successfully
      */
     public boolean addCharacter(GLOReference<? extends CharacterManager>
                                 mgrRef) {
         // make sure no is already here
         if (this.mgrRef != null)
             return false;
 
         this.mgrRef = mgrRef;
 
         return true;
     }
 
     /**
      * Removes the given character from this tile, if and only if this
      * character is already on this tile.
      *
      * @param mgrRef the manager for a character
      *
      * @return whether or not the character was removed successfully
      */
     public boolean removeCharacter(GLOReference<? extends CharacterManager>
                                    mgrRef) {
         // make sure that there's a character here
         if (this.mgrRef == null) {
            System.out.println("tried to remove a null char from a tile");
             return false;
         }
 
         // make sure that the character here is the one being removed
         if (! this.mgrRef.equals(mgrRef)) {
             SimTask task = SimTask.getCurrent();
            System.out.println("not equal on removal: " +
                                mgrRef.peek(task).toString() + " != " +
                                this.mgrRef.peek(task).toString());
             return false;
         }
 
         this.mgrRef = null;
 
         return true;
     }
 
     /**
      * Adds the given item to this tile if possible. This succeeds
      * if there isn't currently an item on this tile.
      *
      * @param itemRef the manager for the item
      *
      * @return whether or not the item was added successfully
      */
     public boolean addItem(GLOReference<? extends Item> itemRef) {
         // check that there isn't an item here
         if (this.itemRef != null)
             return false;
 
         this.itemRef = itemRef;
 
         return true;
     }
 
     /**
      * Removes the given item from this tile, if and only if this item
      * is already on this tile.
      *
      * @param itemRef the manager for the item
      *
      * @return whether or not the item was removed successfully
      */
     public boolean removeItem(GLOReference<? extends Item> itemRef) {
         // check that there's an item here
         if (this.itemRef == null)
             return false;
 
         // check that the item here is the once being removed
         if (! this.itemRef.equals(itemRef))
             return false;
 
         this.itemRef = null;
 
         return true;
     }
 
     /**
      * Test to move the given character to this tile. If a character is
      * on this space, then the two characters interact. This does not
      * actually move the character onto this space.
      *
      * @param characterManager the manager for a character
      *
      * @return the result of making the move
      */
     protected ActionResult charMoveTo(CharacterManager characterManager) {
         // if there is currently a character on this tile, then do the
         // collision
         if (mgrRef != null)
             return mgrRef.get(SimTask.getCurrent()).getCurrentCharacter().
                 collidedFrom(characterManager.getCurrentCharacter());
 
         // if there's no character here, then we always succeed
         return ActionResult.SUCCESS;
     }
 
     /**
      * Tries to take an item on this tile. Unlike <code>moveTo</code>,
      * this actually will remove items from the tile if they are
      * successfully taken.
      *
      * @param characterManager the manager for a character
      *
      * @return the result of getting an item
      */
     public ActionResult getItem(CharacterManager characterManager) {
        if (itemRef == null)
            return ActionResult.FAIL;

         // give this item the chance to react (most items are passive,
         // but you could immediately affect the user here)
         return itemRef.get(SimTask.getCurrent()).giveTo(characterManager);
     }
 
 }
