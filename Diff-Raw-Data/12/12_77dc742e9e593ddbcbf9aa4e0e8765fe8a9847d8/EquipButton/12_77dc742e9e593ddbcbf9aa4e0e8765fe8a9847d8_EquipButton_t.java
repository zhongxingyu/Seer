 package com.game.rania.model.ui;
 
 import com.game.rania.RaniaGame;
 import com.game.rania.controller.Controllers;
 import com.game.rania.model.Player;
 import com.game.rania.model.items.Equip;
 
 public class EquipButton extends PressedButton
 {
 
   protected Equip<?> skillEquip = null;
 
   public EquipButton(Equip<?> equip, float x, float y, int keyCode)
   {
     super(equip.item.getIconID(), null, x, y);
     skillEquip = equip;
     setKey(keyCode);
     action = new TouchAction()
     {
       @Override
       public void execute(boolean touch)
       {
         Player player = Controllers.locController.getPlayer();
         if (player.body.wear <= 0 || skillEquip.wear <= 0)
           return;
 
        skillEquip.use(player, player.target);
       }
     };
   }
 
   public void replaceEquip(Equip<?> newEquip)
   {
     skillEquip = newEquip;
     region = RaniaGame.mView.getTextureRegion(skillEquip.item.getIconID());
   }
 }
