 package statemachine.year1.microwaveoven;
 
 import javax.swing.JLabel;
 
 import quickqui.QuickGUI;
 import statemachine.year1.library.GraphicalMachine;
 
 public class MicroWaveOven extends GraphicalMachine {
 
     private static String POWER_ON_COMMAND = "__ON__";
     
     public static class ControlGUI extends QuickGUI.GUIModel {
         
         @Override 
         public void build() {
            frame("Microwave oven",Layout.VERTICAL,
                 panel(Layout.HORIZONTAL,
                   label(text("Current state: ")),
                   label(name("state"),text("?"))),
                 panel(Layout.HORIZONTAL,
                   label(text("Controls: ")),
                   button(name("START"),text("Start")),
                   button(name("STOP"),text("Stop"))
                 ),
                 panel(Layout.HORIZONTAL,
                   label(text("Door: ")),
                   button(name("OPEN"),text("Open")),
                   button(name("CLOSE"),text("Close"))
                 ),
                 panel(Layout.HORIZONTAL,
                   label(text("Timer: ")),
                   button(name("TIMER"),text("Trigger"))
                 ),
                 button(name(POWER_ON_COMMAND),text("Power on machine"))
               )
             ;
         }
     }
 
     /**
      * Create GUI and then activate robot server functionality
      */
     public static void main(String argv[]) {
         new MicroWaveOven();
     }
     
     public MicroWaveOven() {
         super(new ControlGUI(),new MicrowaveMachine(),POWER_ON_COMMAND);
     }
 
     @Override
     public void update() {
         ((JLabel)gui.getComponent("state")).setText(machine.getState().toString());
     }
 
 }
