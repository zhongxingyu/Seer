 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package plcedit;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collection;
 import java.util.Hashtable;
 import javax.swing.AbstractAction;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 /**
  * Holds all variable types in the final generated code and provides methods
  * to convert these types to displayed types and compiled types.
  *
  * @author huangkf
  */
 public class CodeVarType implements CreatesContextMenu{
     public enum VarType {Bool, Char, Int, Long, Float, Double, Undefined};
     private Hashtable types;
     private VarType key;
 
     /**
      * Internal data structure not to be used outside
      */
     private class CodeString{
         public VarType key;
         public String DisplayString;
         public String CodeString;
 
         CodeString(VarType key, String Display, String Code)
         {
             this.key = key;
             DisplayString = Display;
             CodeString = Code;
         }
     }
 
     public CodeVarType(VarType key)
     {
 
         // All code types are referenced here
        addType(new CodeString(VarType.Bool, "bool","bool"));
         addType(new CodeString(VarType.Char, "char","char"));
         addType(new CodeString(VarType.Int, "int", "int"));
         addType(new CodeString(VarType.Long, "long", "long"));
         addType(new CodeString(VarType.Float, "float", "float"));
         addType(new CodeString(VarType.Double, "double", "double"));
         addType(new CodeString(VarType.Undefined, "<double click here>", "#UNDEFINED_TYPE"));
 
 
         //type.put("STRING", new CodeString("string","string")); (type not supported in compiler)
 
         this.key = key;
     }
 
     public String getSaveString(){
         return toCompileString();
     }
 
     public void setFromSaveString (String CompileString){
         for(CodeString type : (Collection<CodeString>)types.values()){
             if (CompileString.equals(type.CodeString)){
                 this.key = type.key;
             }
         }
     }
     
     public void setKey(VarType key)
     {
         this.key = key;
     }
 
     private void addType(CodeString newType)
     {
         if (types == null)
         {
             types = new Hashtable();
         }
 
         types.put(newType.key, newType);
     }
 
         /**
          * Converts the enumerated type into a displayable text type, although
          * this violates MVC design pattern as it states viewer info should stay
          * out of the model.
          *
          * I found that placing the view conversion after the type definition was
          * more safer since it was easier to match up the enum type and the
          * actual conversion across to a string.
          *
          * @return Displayable friendly string
          */
 
     public String toDisplayString()
     {
         return ((CodeString)types.get(key)).DisplayString;
     }
 
     public String toCompileString()
     {
         return ((CodeString)types.get(key)).CodeString;
     }
 
     /**
      * Changes the variable type
      *
      * @param newType
      */
     public void SetType(VarType key)
     {
         this.key = key;
     }
 
     /**
      * Creates a context menu so we can select between the different types
      */
     public JPopupMenu getContextMenu(ActionListener listener)
     {
         JPopupMenu contextMenu = new JPopupMenu();
 
         /**
          * Generate menu items using data from hash table
          */
 
         for (Object item : types.values())
         {
             if(((CodeString)item).key == VarType.Undefined)
             {
                 continue;
             }
             else
             {
                 contextMenu.add(new JMenuItem(new MenuAction(this, (CodeString)item, listener)));
             }
         }
 
 
         return contextMenu;
 
     }
 
     public VarType getType()
     {
         return key;
     }
 
     private class MenuAction extends AbstractAction {
         private ActionListener _listener;
         private CodeString _internalType;
         private CodeVarType _sender;
 
 
         public MenuAction(CodeVarType sender, CodeString ref)
         {
             this(sender,ref,null); //constructor overloading
         }
 
         public MenuAction(CodeVarType sender, CodeString ref, ActionListener listener) {
             super(ref.DisplayString); //call the base connstructor assign type
             _internalType = ref;
             _sender = sender;
             _listener = listener;
         }
 
         public void actionPerformed(ActionEvent arg0) {
             //throw new UnsupportedOperationException("Not supported yet.");
             _sender.setKey(_internalType.key);
 
 
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
