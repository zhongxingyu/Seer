 package plcstatecharteditor;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.AbstractAction;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 

 /**
  *
  * @author huangkf
  */
public class OutputBlock extends CodeBlock implements CreatesContextMenu {
     public enum PORTS {PortA,PortB,PortC,PortD} //ports we can be mapped to
     
     private PORTS port;
     private String value;
 
     public OutputBlock() {
         this.uid = UIDGenerator.getNext();
         this.ctype = CodeType.Output;
 
 
         //TODO: determine if we want a default port at all
         this.port = PORTS.PortA;
         this.value = "0x0";
     }
 
     public PORTS getPort() {
         return port;
     }
 
     public String getDisplayedPort() {
         return getDisplayedPort(this.port);
     }
 
     public String getDisplayedPort(PORTS port) {
         switch (port) {
             case PortA:
                 return "PORTA";
             case PortB:
                 return "PORTB";
             case PortC:
                 return "PORTC";
             case PortD:
                 return "PORTD";
             default:
                 return "!!!ERROR!!!";
         }
     }
 
     public String getCompiledPort() {
         switch (port) {
             case PortA:
                 return "*PORTA";
             case PortB:
                 return "*PORTB";
             case PortC:
                 return "*PORTC";
             case PortD:
                 return "*PORTD";
             default:
                 return "!!!ERROR!!!";
         }
     }
 
     public void setPort (PORTS port) {
         this.port = port;
     }
 
     public void setValue (String value) {
         this.value = value;
     }
 
     public String getValue () {
         return this.value;
     }
 
     @Override
     public String getTypeString() {
         return "OUTPUT";
     }
 
     @Override
     public String getCode() {
        return getUIDStringLabel() + newline +
                "//////////////////////////////////////" + newline +
                "//        OUTPUT                    //" + newline +
                "//////////////////////////////////////" + newline +
                getCompiledPort() + " = " + getValue() + ";";
     }
 
     public JPopupMenu getContextMenu(ActionListener listener) {
         JPopupMenu contextMenu = new JPopupMenu();
 
         /**
          * Generate menu items using data from hash table
          */
         contextMenu.add(new JMenuItem(new MenuAction(this, PORTS.PortA, listener)));
         contextMenu.add(new JMenuItem(new MenuAction(this, PORTS.PortB, listener)));
         contextMenu.add(new JMenuItem(new MenuAction(this, PORTS.PortC, listener)));
         contextMenu.add(new JMenuItem(new MenuAction(this, PORTS.PortD, listener)));
 
 
         return contextMenu;
     }
 
 
     private class MenuAction extends AbstractAction {
         private ActionListener _listener;
         private PORTS _port;
         private OutputBlock _sender;
 
 
         public MenuAction(OutputBlock sender, PORTS ref)
         {
             this(sender,ref,null); //constructor overloading
         }
 
         public MenuAction(OutputBlock sender, PORTS ref, ActionListener listener) {
             super(getDisplayedPort(ref)); //call the base connstructor assign type
             _port = ref;
             _sender = sender;
             _listener = listener;
         }
 
         public void actionPerformed(ActionEvent arg0) {
             //throw new UnsupportedOperationException("Not supported yet.");
             _sender.setPort(_port);
 
 
             fireChangedEvent(arg0);
 
             //System.out.println("got a click event for " + toDisplayString() );
         }
 
         public void fireChangedEvent(ActionEvent arg0) //fire update event
         {
             if (_listener != null)
             {
                 System.out.println("firing change event");
                 _listener.actionPerformed(arg0);
 
             }
             else
             {
                 System.out.println("ERROR NO LISTENER");
             }
         }
     }
 }
