 package com.game.rania.model.ui;
 
 import com.game.rania.model.Text;
 import com.game.rania.model.element.RegionID;
 
 public class RadioButton extends Button
 {
 
   protected RadioGroup group = null;
   private boolean      radio = false;
 
   public RadioButton(RegionID regionOff, RegionID regionOn, float x, float y)
   {
     super(regionOff, regionOn, x, y, null, null);
   }
 
   public RadioButton(RegionID regionOff, RegionID regionOn, float x, float y, TouchAction action)
   {
     super(regionOff, regionOn, x, y, null, action);
   }
 
   public RadioButton(RegionID regionOff, RegionID regionOn, float x, float y, Text text, TouchAction action)
   {
     super(regionOff, regionOn, x, y, text, action);
   }
 
   public RadioButton(RegionID regionOff, RegionID regionOn, float x, float y, Text text)
   {
     super(regionOff, regionOn, x, y, text, null);
   }
 
   @Override
   public boolean use()
   {
     if (radio == true)
       return true;
 
     radio = true;
    
    if (group != null)
      group.updateChecks(this);
 
     if (action != null)
       action.execute(radio);
 
     return true;
   }
 
   public boolean getCheck()
   {
     return radio;
   }
 
   public void setCheck(boolean check)
   {
     this.radio = check;
   }
 
   @Override
   public boolean checkButton()
   {
     return radio;
   }
 }
