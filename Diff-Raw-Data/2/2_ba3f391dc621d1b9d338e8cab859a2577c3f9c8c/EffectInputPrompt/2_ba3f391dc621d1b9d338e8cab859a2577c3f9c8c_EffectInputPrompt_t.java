 package com.dsh105.sparktrail.conversation.effect;
 
 import com.dsh105.dshutils.util.StringUtil;
 import com.dsh105.sparktrail.chat.BlockData;
 import com.dsh105.sparktrail.chat.WaitingData;
 import com.dsh105.sparktrail.data.DataFactory;
 import com.dsh105.sparktrail.trail.ParticleType;
 import com.dsh105.sparktrail.util.Lang;
 import org.bukkit.FireworkEffect;
 import org.bukkit.conversations.ConversationContext;
 import org.bukkit.conversations.Prompt;
 import org.bukkit.conversations.ValidatingPrompt;
 
 public class EffectInputPrompt extends ValidatingPrompt {
 
     private ParticleType particleType;
     private WaitingData data;
     private boolean isSecondInput = false;
 
     private BlockData blockData = new BlockData(0, 0);
 
     private EffectInputSuccessPrompt successPrompt;
 
     public EffectInputPrompt(ParticleType particleType, WaitingData data) {
         this(particleType, data, false);
     }
 
     public EffectInputPrompt(ParticleType particleType, WaitingData data, BlockData blockData) {
         this(particleType, data, true);
         this.blockData = blockData;
     }
 
     public EffectInputPrompt(ParticleType particleType, WaitingData data, boolean isSecondInput) {
         this.particleType = particleType;
         this.data = data;
         this.isSecondInput = isSecondInput;
     }
 
     @Override
     protected boolean isInputValid(ConversationContext conversationContext, String s) {
         if (this.particleType == ParticleType.FIREWORK) {
             FireworkEffect fe = DataFactory.generateFireworkEffectFrom(s);
             if (fe != null) {
                 this.successPrompt = new EffectInputSuccessPrompt(this.particleType, this.data, fe);
                 return true;
             }
        } else if (this.particleType == ParticleType.BLOCKBREAK || this.particleType == ParticleType.ITEMSPRAY) {
             if (!StringUtil.isInt(s)) {
                 return false;
             }
 
             if (this.isSecondInput) {
                 this.successPrompt = new EffectInputSuccessPrompt(this.particleType, this.data, this.blockData);
                 this.blockData.data = Integer.parseInt(s);
                 return true;
             } else {
                 this.blockData.id = Integer.parseInt(s);
                 return true;
             }
         }
         return false;
     }
 
     @Override
     protected Prompt acceptValidatedInput(ConversationContext conversationContext, String s) {
         if (this.particleType == ParticleType.FIREWORK) {
             return this.successPrompt;
         }
         if (this.isSecondInput) {
             return this.successPrompt;
         } else {
             return new EffectInputPrompt(this.particleType, this.data, this.blockData);
         }
     }
 
     @Override
     public String getPromptText(ConversationContext conversationContext) {
         if (this.particleType == ParticleType.BLOCKBREAK || this.particleType == ParticleType.ITEMSPRAY) {
             if (this.isSecondInput) {
                 return Lang.INPUT_SECOND_METAVALUE.toString();
             } else {
                 return Lang.INPUT_FIRST_IDVALUE.toString();
             }
         } else if (this.particleType == ParticleType.FIREWORK) {
             return Lang.INPUT_FIREWORK.toString();
         }
         return null;
     }
 
     @Override
     protected String getFailedValidationText(ConversationContext context, String invalidInput) {
         return Lang.INVALID_INPUT.toString();
     }
 }
