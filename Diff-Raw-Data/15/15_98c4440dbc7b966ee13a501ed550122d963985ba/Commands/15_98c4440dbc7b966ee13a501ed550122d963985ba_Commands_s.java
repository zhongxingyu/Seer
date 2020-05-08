 package com.dhemery.victor.examples.demo;
 
 import com.dhemery.victor.examples.runner.OnMasterPage;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import static com.dhemery.polling.Has.has;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.not;
 
 public class Commands extends OnMasterPage {
     @BeforeClass
     public static void divertLaunch() {
         configuration.set("victor.simulator.process.owner", "me");
     }
 
     @Test public void beginEditingRobustly() { prefix.tap(); }
    @Test public void setTextRobustly() { prefix.sendMessage("DFX_setText:", "Robustly "); }
     @Test public void endEditingRobustly() { prefix.sendMessage("DFX_return"); }
     @Test public void switchOnRobustly() { if(the(prefixEnabled, has(on(), not(equalTo(true))))) prefixEnabled.tap(); }
     @Test public void switchOffRobustly() { if(the(prefixEnabled, has(on(), not(equalTo(false))))) prefixEnabled.tap(); }
 
     @Test public void beginEditingNaively() { prefix.sendMessage("becomeFirstResponder"); }
    @Test public void setTextNaively() { prefix.sendMessage("setText:", "naively "); }
     @Test public void endEditingNaively() { prefix.sendMessage("resignFirstResponder"); }
     @Test public void switchOnNaively() { prefixEnabled.sendMessage("setOn:animated:", true, true); }
     @Test public void switchOffNaively() { prefixEnabled.sendMessage("setOn:animated:", false, true); }
 }
