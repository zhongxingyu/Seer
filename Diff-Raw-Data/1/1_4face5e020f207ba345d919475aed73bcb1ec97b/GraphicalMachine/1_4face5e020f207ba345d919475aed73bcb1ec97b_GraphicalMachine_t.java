 package statemachine.year1.library;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Observable;
 import java.util.Observer;
 
 import quickqui.QuickGUI;
 
 public abstract class GraphicalMachine implements ActionListener, Observer {
 
     protected QuickGUI gui;
     protected Machine machine;
     private String powerOnCommand;
     
     public GraphicalMachine(QuickGUI.GUIModel model, Machine machine, String powerOnCommand) {
         this.gui = new QuickGUI(model,this);
         this.machine = machine;
        this.powerOnCommand = powerOnCommand;
         this.machine.addObserver(this);
     }
 
     public void actionPerformed(ActionEvent e) {
         if(e.getActionCommand().equals(powerOnCommand))
             machine.initialize();
         else {
             Event event = new Event(e.getActionCommand());
             machine.processEvent(event);
         }
     }
 
     @Override
     public void update(Observable o, Object arg) {
         if(!(o==machine)) throw new Error("Inconsistent observer notification");
         this.update();
     }
 
     protected abstract void update();
 
 }
