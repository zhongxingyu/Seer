 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.manager;
 
 import ca.couchware.wezzle2d.util.CouchLogger;
 import ca.couchware.wezzle2d.Game;
 import ca.couchware.wezzle2d.ManagerHub;
 import ca.couchware.wezzle2d.ui.IButton;
 import ca.couchware.wezzle2d.group.IGroup;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * The menu manager is used to manage the menues in Wezzle.  In particular, it
  * helps to link Buttons to Groups.
  * 
  * @author cdmckay
  */
 public class GroupManager implements IResettable
 {
     
     public enum Type
     {
         GAME_OVER,
         PAUSE,
         OPTIONS,
         HELP,
         HIGH_SCORE
     }
     
     public enum Layer
     {
         BOTTOM,
         MIDDLE,
         TOP
     }                
     
     /**
      * This  linked list holds all groups that have called the register()
      * method.  This list is useful for performing commands on all the groups,
      * such as running updateLogic().
      */
     private List<IGroup> groupList;
     
     /** 
      * The list of groups currently being shown.
      */
     private List<Entry> entryList;
           
     private Game game;
     private ManagerHub hub;
 
     /**
      * Holds the visiblility of the piece grid before the board was hidden so
      * that the proper visibility may be restored when the board is reshown.
      */
     //private boolean lastPieceGridVisible;
 
     /**
      * Private constructor.
      * @param layerMan
      * @param pieceMan
      */
     private GroupManager(Game game, ManagerHub hub)
     {
         groupList = new ArrayList<IGroup>();
         entryList = new LinkedList<Entry>();        
         
         this.game = game;
         this.hub = hub;
     }             
 
     public void resetState()
     {
         hideAllGroups(false);
     }
     
     /**
      * Create a new GroupManager instance.
      * @param layerMan
      * @param pieceMan
      * @return
      */
     public static GroupManager newInstance(Game game, ManagerHub hub)
     {
         return new GroupManager(game, hub);
     }
 
     /**
      * Show the that passed group and modify the button's state to indicate that
      * it is activated.
      * @param button
      * @param showGroup
      * @param groupType
      * @param layer
      */
     public void showGroup(IButton button, IGroup showGroup, 
             Type groupType, Layer layer)
     {
         // Remove all groups that aren't part of the passed class.
         // Hide all existing members of the passed clas.
         for (Iterator<Entry> it = entryList.iterator(); it.hasNext(); )            
         {
             // The entry we are looking at.
             Entry e = it.next();                   
             
             if (e.getLayer() == layer)
             {
                 if (e.getGroupType() == groupType)
                 {
                     e.getGroup().setVisible(false);
                 }
                 else
                 {
                     deactivateEntry(e);
                     it.remove();
                 }
 
                 continue;
             }
             
             e.getGroup().setVisible(false);                            
         }
        
         // Add the group on top.
         entryList.add(0, new Entry(showGroup, button, groupType, layer));
         
         // Make the group visible.
         showGroup.setVisible(true);
         showGroup.setActivated(true);
         
         // Make the button activated.
         if (button != null)
         {
             button.setActivated(true);
         }
 
         // Hide the board and associated layers.
         hub.pieceMan.clearMouseButtonSet();
         hideBoard();
        hidePiecePreview();
         
         CouchLogger.get().recordMessage(this.getClass(), "Groups open: " + entryList.size());
     }    
     
     /**
      * This method makes an entry's group invisible, deactivates it, 
      * resets it's buttons, and deactivates the button that called it.
      * 
      * @param entry
      */
     protected void deactivateEntry(Entry entry)
     {
         entry.getGroup().setVisible(false);
         entry.getGroup().setActivated(false);
         entry.getGroup().clearChanged();
 
         if (entry.getButton() != null)
             entry.getButton().setActivated(false);
     }
     
     public void hideGroup(Type cls, Layer layer, boolean showGrid)
     {
         // Go through the entry list, removing all entries with the
         // passed class name.
         for (Iterator<Entry> it = entryList.iterator(); it.hasNext(); )            
         {
             // The entry we are looking at.
             Entry e = it.next();
             
             if (e.getLayer() == layer && e.getGroupType() == cls)
             {
                 // Deactivate the entry.
                 deactivateEntry(e);     
                 
                 // Remove it.
                 it.remove();
             }
         }
                     
         // Make the top of the list visible.
         if ( entryList.isEmpty() )
         {
             hub.pieceMan.clearMouseButtonSet();
             showBoard(showGrid);
            showPiecePreview();
         }
         else
         {
             entryList.get(0).getGroup().setVisible(true);
         }
         
         CouchLogger.get().recordMessage(this.getClass(), "Groups open: " + entryList.size());
     }   
     
     public void hideGroup(IGroup group, boolean showGrid)
     {
         // Remove the group.
         for (Iterator<Entry> it = entryList.iterator(); it.hasNext(); )            
         {
             // The entry we are looking at.
             Entry e = it.next();
             
             if (e.getGroup() == group)
             {
                 deactivateEntry(e);
                 it.remove();
                 break;
             }
         }
                 
         // Make the top of the list visible.
         if ( entryList.isEmpty() )
         {
             hub.pieceMan.clearMouseButtonSet();
             showBoard(showGrid);
         }
         else
         {
             entryList.get(0).getGroup().setVisible(true);
         }
         
         CouchLogger.get().recordMessage(this.getClass(), "Groups open: " + entryList.size());
     }
     
     /**
      * Hides all currently shown groups.
      */
     public void hideAllGroups(boolean showGrid)
     {
         for (Iterator<Entry> it = entryList.iterator(); it.hasNext(); )            
         {
             // The entry we are looking at.
             Entry e = it.next();                       
             deactivateEntry(e);
             it.remove();                       
         }
         
         hub.pieceMan.clearMouseButtonSet();
         showBoard(showGrid);
        showPiecePreview();
     }   
 
     /**
      * Show the board.
      */
     private void showBoard(boolean showGrid)
     {
         // Make piece grid visible contingent on the saved visibility.
         if (showGrid) hub.pieceMan.showPieceGrid();
         else hub.pieceMan.hidePieceGrid();
 
         hub.layerMan.show(LayerManager.Layer.TILE);
         hub.layerMan.show(LayerManager.Layer.EFFECT);
     }
 
     /**
      * Hide the board.
      */
     private void hideBoard()
     {
         // Remember the old visibility.
         //this.lastPieceGridVisible = pieceMan.isPieceGridVisible();
 
         hub.pieceMan.hidePieceGrid();
         hub.layerMan.hide(LayerManager.Layer.TILE);
         hub.layerMan.hide(LayerManager.Layer.EFFECT);
     }
 
    private void showPiecePreview()
     {
        game.getUI().setOverlayPiecePreviewVisible( true );
     }
 
    private void hidePiecePreview()
     {
         game.getUI().setOverlayPiecePreviewVisible( false );
     }
     
     /**
      * Registers this group with the Group linked list.
      * @param group The group to register.
      */
     public void register(IGroup group)
     {
         // Add the group to the static linked list.
         groupList.add(group);
     }
     
     /**
      * Update the logic of all groups if they have detected clicks.
      * @param game The game state.
      */
     public void updateLogic(Game game, ManagerHub hub)
     {
         for (IGroup group : groupList)
         {
             if (group.isActivated())            
                 group.updateLogic(game, hub);            
         }
     }
     
     public boolean isActivated()
     {
         // If the group list is not empty, then at least one group must be 
         // showing.
         return !entryList.isEmpty();
     }  
 
     /**
      * This is an inner class represented an entry in the the group linked list.
      */
     protected static class Entry
     {               
         /**
          * The group associated with this entry.
          */
         final protected IGroup group;
         
         /**
          * The button that was used to open this group.
          */
         final protected IButton button;
         
         /**
          * The class of the group.  This is used to hide or show many groups
          * at once.
          */        
         final protected Type groupType;
         
         /**
          * The layer that the group is on.  This is mainly used to keep
          * the game over screen open under a bunch of menues.
          */
         final protected Layer layer;
         
         /**
          * The constructor.
          */
         public Entry(IGroup group, IButton button, 
                 Type groupType, Layer layer)
         {
             // Set the references.
             this.group = group;
             this.button = button;
             this.groupType = groupType;
             this.layer = layer;
         }
 
         public IGroup getGroup()
         {
             return group;
         }    
         
         public IButton getButton()
         {
             return button;
         }
 
         public Type getGroupType()
         {
             return groupType;
         }
 
         public Layer getLayer()
         {
             return layer;
         }                                   
     } // end class
     
 }
