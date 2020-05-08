 
 package com.quartercode.minecartrevolutiontags;
 
 import org.bukkit.entity.Minecart;
import com.quartercode.minecartrevolution.expression.ExpressionCommand;
import com.quartercode.minecartrevolution.expression.ExpressionCommandInfo;
 import com.quartercode.minecartrevolution.util.TypeArray;
 import com.quartercode.minecartrevolution.util.TypeArray.Type;
 
 public class TagCommand extends ExpressionCommand {
 
     private final MinecartRevolutionTags minecartRevolutionTags;
 
     public TagCommand(final MinecartRevolutionTags minecartRevolutionTags) {
 
         this.minecartRevolutionTags = minecartRevolutionTags;
     }
 
     @Override
     protected ExpressionCommandInfo createInfo() {
 
         return new ExpressionCommandInfo(new TypeArray(Type.STRING), "ta", "tag");
     }
 
     @Override
     public boolean canExecute(final Minecart minecart) {
 
         return true;
     }
 
     @Override
     public void execute(final Minecart minecart, final Object parameter) {
 
         final String data = String.valueOf(parameter);
 
         if (String.valueOf(parameter).startsWith("+")) {
             minecartRevolutionTags.getTagManager().addTag(minecart, data.replaceAll("\\+", "").trim());
         } else if (String.valueOf(parameter).startsWith("-")) {
             if (data.replaceAll("-", "").trim().isEmpty()) {
                 minecartRevolutionTags.getTagManager().removeTags(minecart);
             } else {
                 minecartRevolutionTags.getTagManager().removeTag(minecart, data.replaceAll("-", "").trim());
             }
         } else {
             minecartRevolutionTags.getTagManager().addTag(minecart, data.trim());
         }
     }
 
 }
