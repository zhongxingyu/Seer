 package snake.mcmods.movementassistant.handlers;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.entity.EntityClientPlayerMP;
 
 public class MAMovementHandler
 {
     private boolean needResetKey=false;
 
     private boolean isAutoMoving = false;
     
     public boolean getIsAutoMoving()
     {
         return isAutoMoving;
     }
     
     public void setIsMoving(boolean value)
     {
         isAutoMoving = value;
         if (value)
         {
             isAutoSprinting = false;
         } else
         {
             resetKeyState();
         }
     }
 
     private boolean isAutoSprinting = false;
     
     public boolean getIsAutoSprinting()
     {
         return isAutoSprinting;
     }
 
     public void setIsSprinting(boolean value)
     {
         isAutoSprinting = value;
         if (value)
         {
             isAutoMoving = false;
         } else
         {
             resetKeyState();
         }
     }
 
     public void updateMovmentOnPlayerTick()
     {
         Minecraft mc = Minecraft.getMinecraft();
        if(mc.gameSettings.keyBindBack.isPressed())
        {
            stopMoving();
            return;
        }
         EntityClientPlayerMP p = mc.thePlayer;
         
         if (isAutoMoving || isAutoSprinting)
         {
             mc.gameSettings.keyBindForward.pressed = true;
             if (isAutoSprinting)
                 p.setSprinting(true);
         }
         
         if(mc.currentScreen!=null)
         {
             if(p.isInWater())
             {
                 mc.gameSettings.keyBindJump.pressed = true;
             }
             else if(p.isOnLadder())
             {
                 mc.gameSettings.keyBindSneak.pressed = true;
             } 
             needResetKey = true;
         }
         else if(needResetKey==true)
         {
             resetKeyState();
             needResetKey=false;
         }
     }
     
     public void resetKeyState()
     {
         Minecraft mc = Minecraft.getMinecraft();
         mc.gameSettings.keyBindForward.pressed = false;
         mc.gameSettings.keyBindJump.pressed = false;
         mc.gameSettings.keyBindSneak.pressed=false;
     }
     
     public void stopMoving()
     {
         setIsMoving(false);
         setIsSprinting(false);
         resetKeyState();
     }
 }
