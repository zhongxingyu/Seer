 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame.engine.input;
 
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.InputListener;
 import com.jme3.input.controls.KeyTrigger;
 import mygame.helpers.Share;
import sun.security.jca.GetInstance;
 
 /**
  *
  * @author Dansion
  */
 public class KeyInputs {
     private String kGroup, sGroup;
     private InputListener listener;
     private Share shares = Share.getInstance();
     private KeyBoard kb = KeyBoard.getInstance();
     private boolean active = false;
     
     public String[] actions = null;
     public int[] triggers = null;
     
     public KeyInputs(String keyGroup) {
         this(keyGroup, null);
     }
     
     public KeyInputs(String keyGroup, String subGroup) {
         kGroup = keyGroup;
         sGroup = subGroup;
     }
     
     public void addInput(int triggers, String actions) {}  
             
     public final void addInputs(int [] triggers, String[] actions) {
         if(actions.length == triggers.length) {
             this.actions = actions;
             this.triggers = triggers;
             
             for(int i=0; i<actions.length; i++) {
                 shares.inputManager.addMapping(actions[i], new KeyTrigger(triggers[i]));
             }
         }
         else {
             throw new UnsupportedOperationException("amount of actions is not the same as the amount of keys");
         }
     }
     
     public final void addListener(ActionListener listener) {
         this.listener = listener;
         
         kb.register(this);
         
         attachListener();
     }
     
     public final void addListener(AnalogListener listener) {
         this.listener = listener;
         
         kb.register(this);
         
         attachListener();
     }
     
     public final void attachListener() {
         if(listener != null) {
             shares.inputManager.addListener(listener, actions);
         }
         
         active = true;
     }
     
     public final void detachListener() {
         if(listener != null) {
             shares.inputManager.removeListener(listener);    
         }
         
         active = false;
     }
     
     public boolean isActive() {
         return active;
     }
     
     public String getKeyGroup() {
         return kGroup;
     }
 }
