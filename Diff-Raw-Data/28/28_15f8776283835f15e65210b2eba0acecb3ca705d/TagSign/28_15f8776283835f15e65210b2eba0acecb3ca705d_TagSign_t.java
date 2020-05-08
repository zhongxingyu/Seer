 
 package com.quartercode.minecartrevolutiontags;
 
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Minecart;
 import com.quartercode.minecartrevolution.sign.ControlSign;
 import com.quartercode.minecartrevolution.sign.ControlSignInfo;
 
 public class TagSign extends ControlSign {
 
     public TagSign() {
 
     }
 
     @Override
     protected ControlSignInfo createInfo() {
 
         return new ControlSignInfo("Tag", "Adds or removes minecart tags", "tag.place", "tag.destroy", "tag");
     }
 
     @Override
    public void execute(final Minecart minecart, final String label, final Sign sign) {
 
         executeExpression(minecart, "tag " + sign.getLine(1));
     }
 
 }
