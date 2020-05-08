 package com.fisherevans.lrk.states;
 
 import com.fisherevans.lrk.Resources;
 import com.fisherevans.lrk.StateLibrary;
 import com.fisherevans.lrk.launcher.Game;
 import com.fisherevans.lrk.managers.DisplayManager;
 import com.fisherevans.lrk.rpg.items.*;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: immortal
  * Date: 7/22/13
  * Time: 7:44 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CharacterState extends LRKState
 {
     public enum InventoryType { Equipment, Consumables }
 
     private LRKState _lastState;
 
     private float _hWidth;
 
     private InventoryType _currentType;
     private ArrayList<Item> _currentList;
     private Map<InventoryType, Integer> _currentPositions;
 
     public CharacterState(LRKState lastState) throws SlickException
     {
         super(StateLibrary.getTempID());
         _lastState = lastState;
     }
 
     @Override
     public void init() throws SlickException
     {
         resize();
         swapType();
         _currentPositions = new HashMap<InventoryType, Integer>();
         _currentPositions.put(InventoryType.Equipment, 0);
         _currentPositions.put(InventoryType.Consumables, 0);
     }
 
     @Override
     public void enter() throws SlickException
     {
 
     }
 
     @Override
     public void exit() throws SlickException
     {
 
     }
 
     @Override
     public void render(Graphics gfx) throws SlickException
     {
         //_lastState.render(gfx);
 
         renderInventory(gfx, _currentType);
         renderComparison(gfx);
 /*        renderStats(gfx);
 
         if(_currentType == InventoryType.Equipment)
             renderEquipmentPanel(gfx);
         else
             renderConsumablesPanel(gfx);*/
     }
 
     private final int INV_TITLE_HEIGHT = 32;
     private final int INV_LINE_HEIGHT = 20;
     private final int INV_LIST_PADDING = 12;
     private final int INV_LIST_MARGIN = 20;
     private final float INV_HEIGHT_RATIO = 0.6666f;
     private float _invHeight, _invHeightList, _invHeightScroll, _invWidthScroll, _invXScroll, _invYScroll, _invYList;
     public void renderInventory(Graphics gfx, InventoryType type)
     {
         // title
         GFX.drawText(0, 0, _hWidth, INV_TITLE_HEIGHT, GFX.TEXT_CENTER, GFX.TEXT_CENTER, Resources.getFont(2), Color.white, type.toString());
 
         // list bg
         gfx.setColor(Color.darkGray);
         gfx.fillRect(INV_LIST_MARGIN,
                 INV_TITLE_HEIGHT,
                 _hWidth - INV_LIST_MARGIN*2f,
                 _invHeightList);
 
         float scrollHeight = _invHeightScroll/(float)_currentList.size();
         gfx.setColor(Color.white);
         gfx.fillRect(_invXScroll,
                _invYScroll + (getPosition())*scrollHeight,
                 _invWidthScroll,
                 scrollHeight);
 
         clip(0, INV_TITLE_HEIGHT, _hWidth, _invHeightList);
         for(int id = 0;id < _currentList.size();id++)
         {
             Item item = _currentList.get(id);
             Color color = id == _currentPositions.get(_currentType) ? Color.white : Color.lightGray;
             GFX.drawTextCenteredV(INV_LIST_MARGIN + INV_LIST_PADDING,
                     _invYList + (id- getPosition())*INV_LINE_HEIGHT,
                     0, Resources.getFont(1), color, item.getName());
         }
         unClip();
     }
 
     public void renderEquipmentPanel(Graphics gfx)
     {
 
     }
 
     public void renderConsumablesPanel(Graphics gfx)
     {
 
     }
 
     public void renderComparison(Graphics gfx)
     {
         GFX.drawImageCentered(DisplayManager.getRenderWidth()*0.5f*0.3333f, DisplayManager.getRenderHeight()*0.8f, _currentList.get(_currentPositions.get(_currentType)).getImage().getScaledCopy(2));
     }
 
     public void renderStats(Graphics gfx)
     {
 
     }
 
     private void clip(float x, float y, float width, float height)
     {
         Game.getContainer().getGraphics().setClip(
                 (int)(x*DisplayManager.getScale()),
                 (int)(y*DisplayManager.getScale()),
                 (int)(width*DisplayManager.getScale()),
                 (int)(height*DisplayManager.getScale()));
     }
 
     private void unClip()
     {
         Game.getContainer().getGraphics().clearClip();
     }
 
     @Override
     public void update(float delta) throws SlickException
     {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void destroy() throws SlickException
     {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void resize()
     {
         _hWidth = DisplayManager.getRenderWidth()/2f;
 
         // Inventory
         _invHeight = DisplayManager.getRenderHeight()*INV_HEIGHT_RATIO;
         _invHeightList = _invHeight - INV_TITLE_HEIGHT - INV_LIST_MARGIN;
         _invHeightScroll = _invHeightList - INV_LIST_PADDING*2;
         _invWidthScroll = INV_LIST_PADDING/2f;
         _invXScroll = _hWidth - INV_LIST_MARGIN - INV_LIST_PADDING*0.75f;
         _invYScroll = INV_TITLE_HEIGHT + INV_LIST_PADDING;
         _invYList = _invHeightList/2f + INV_TITLE_HEIGHT;
     }
 
     public int getPosition()
     {
         return _currentPositions.get(_currentType);
     }
 
     public void keyBack()
     {
         StateLibrary.setActiveState(_lastState);
     }
 
     public void keyLeft()
     {
         swapType();
     }
 
     public void keyRight()
     {
         swapType();
     }
 
     public void keyUp()
     {
         if(_currentPositions.get(_currentType) <= 0)
         {
             _currentPositions.put(_currentType, 0);
             return;
         }
 
         _currentPositions.put(_currentType, _currentPositions.get(_currentType) - 1);
     }
 
     public void keyDown()
     {
         if(_currentPositions.get(_currentType) >= _currentList.size()-1)
         {
             _currentPositions.put(_currentType, _currentList.size()-1);
             return;
         }
 
         _currentPositions.put(_currentType, _currentPositions.get(_currentType)+1);
     }
 
     private void swapType()
     {
         if(_currentType == InventoryType.Equipment)
         {
             _currentType = InventoryType.Consumables;
             _currentList = Game.lrk.getPlayer().getInventory().getConsumables();
         }
         else
         {
             _currentType = InventoryType.Equipment;
             _currentList = Game.lrk.getPlayer().getInventory().getEquipment();
         }
     }
 }
