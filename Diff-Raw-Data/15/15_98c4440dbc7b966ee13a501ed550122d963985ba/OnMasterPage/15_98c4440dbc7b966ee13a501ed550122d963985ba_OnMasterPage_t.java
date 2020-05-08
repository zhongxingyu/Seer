 package com.dhemery.victor.examples.runner;
 
 import com.dhemery.polling.Query;
 import com.dhemery.victor.examples.views.UILabel;
 import com.dhemery.victor.examples.views.UISwitch;
 import com.dhemery.victor.examples.views.UITextField;
 import com.dhemery.victor.examples.views.UIView;
 import org.junit.Before;
 
 import static com.dhemery.polling.Has.has;
 import static com.dhemery.victor.Igor.igor;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.not;
 
 public class OnMasterPage extends OnVigorApp {
     protected UITextField prefix;
     protected UILabel preview;
     protected UISwitch prefixEnabled;
     protected UIView increment;
 
     @Before
     public void setup() {
         prefix = textField(igor("#prefix"));
         preview = label(igor("#nextItemPreview"));
         prefixEnabled = toggle(igor("#prefixEnabled"));
         increment = button(igor("[accessibilityLabel=='Increment']"));
         waitForAnimation();
     }
 
     protected Query<UISwitch,Boolean> on() {
         return new Query<UISwitch, Boolean>() {
             @Override
             public String name() {
                 return "on";
             }
 
             @Override
             public Boolean query(UISwitch s) {
                 return Boolean.parseBoolean(s.sendMessage("isOn").get(0));
             }
         };
     }
 
     protected void enablePrefix() {
        setPrefixEnabled(true);
     }
 
     protected void disablePrefix() {
        setPrefixEnabled(false);
     }
 
     private void setPrefixEnabled(boolean desiredState) {
         if(the(prefixEnabled, has(on(), not(equalTo(desiredState))))) {
             togglePrefixSwitch();
         }
     }
 
     protected void togglePrefixSwitch() {
         prefixEnabled.tap();
         waitForAnimation();
     }
 
     protected void endEditing() {
         prefix.done();
     }
 
     protected void beginEditing() {
         prefix.tap();
         waitForAnimation();
     }
 }
