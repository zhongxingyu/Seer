 package com.killard.card.attribute;
 
 import com.killard.card.Action;
 import com.killard.card.Attribute;
 import com.killard.card.CardInstance;
 
 import java.util.List;
 
 /**
  * <p>
  * This class defines basic base for all card attributes.
  * </p>
  * <p>
  * <strong>Thread safety:</strong>
  * This class is mutable and not thread safe.
  * </p>
  */
 public abstract class BasicAttribute implements Attribute {
     
     private final boolean hidden;
 
     private final boolean useful;
 
     private final boolean harmful;
 
     protected BasicAttribute() {
         hidden = false;
         useful = false;
         harmful = false;
     }
 
     protected BasicAttribute(boolean hidden) {
         this.hidden = hidden;
         useful = false;
         harmful = false;
     }
 
     protected BasicAttribute(boolean hidden, boolean useful) {
         this.hidden = hidden;
         this.useful = useful;
         harmful = false;
     }
 
     protected BasicAttribute(boolean hidden, boolean useful, boolean harmful) {
         this.hidden = hidden;
         this.useful = useful;
         this.harmful = harmful;
     }
 
     public String getId() {
         return getClass().getSimpleName();
     }
 
    public boolean isHidden() {
         return hidden;
     }
 
     public boolean isUseful() {
         return useful;
     }
 
     public boolean isHarmful() {
         return harmful;
     }
 
     public Class action() {
         return Action.class;
     }
 
     public boolean selfTargeted() {
         return true;
     }
 
     public List<Action> validate(CardInstance owner, Action action) {
         return null;
     }
 
     public List<Action> before(CardInstance owner, Action action) {
         return null;
     }
 
     public List<Action> after(CardInstance owner, Action action) {
         return null;
     }
 }
