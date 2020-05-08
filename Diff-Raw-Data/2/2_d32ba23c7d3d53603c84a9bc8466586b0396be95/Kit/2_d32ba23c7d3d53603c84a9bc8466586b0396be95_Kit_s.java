 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package kryptoprojekt;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.HashMap;
 import java.util.LinkedList;
 import javax.swing.JInternalFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import kryptoprojekt.controller.XMLReader;
 
 /**
  * This is a Class which helps to easily create new Components.
  * It's responsible for the logic "behind" and includes the two Classes,
  * that are necessary for the logic (DropTextField and DragList).
  *
  * @author Stefan
  */
 public class Kit extends JInternalFrame {
 
     protected ConnectionHandler handler;
     private static int id = 1;
     protected HashMap<String, Object> results;
     protected LinkedList<Kit> parents, children;
     private static Thread refresher;
     protected static XMLReader xmlReader = XMLReader.getInstance("./languageFiles/english.xml");
 
     /**
      * Creates an new Kit with everything necessary for the logic.
      *
      * @param handler The ConnectionHandler that is responsible for this Kit.
      */
     public Kit(ConnectionHandler handler) {
         super("#" + id++, false, true, false, false);
         this.handler = handler;
         results = new HashMap<String, Object>();
         parents = new LinkedList<Kit>();
         children = new LinkedList<Kit>();
         handler.add(this);
         for (Component c : getComponents()) {
             c.addMouseListener(new MouseListener() {
 
                 public void mouseClicked(MouseEvent e) {
                 }
 
                 public void mousePressed(MouseEvent e) {
                     refresher = new Thread() {
 
                         @Override
                         public void run() {
                             while (!isInterrupted()) {
                                 try {
                                     Thread.sleep(50);
                                 } catch (InterruptedException ex) {
                                     interrupt();
                                 }
                                 method();
                             }
                         }
                     };
                     refresher.start();
                 }
 
                 public void mouseReleased(MouseEvent e) {
                     refresher.interrupt();
                 }
 
                 public void mouseEntered(MouseEvent e) {
                 }
 
                 public void mouseExited(MouseEvent e) {
                 }
             });
         }
     }
 
     /**
      * Method which is called every time the gui has to be repainted.
      */
     private void method() {
         handler.getDesktop().repaint();
     }
 
     /**
      * Returns the result of this Kit, identified by the special key-value id.
      *
      * @param id key-value for the special result.
      * @return The result containing to the key-value id.
      */
     public Object getResult(String id) {
         return this.results.get(id);
     }
 
     /**
      * Returns the parents of this Kit
      *
      * @return Returns the parent-Kits of this Kit
      */
     public LinkedList<Kit> getParents() {
         return parents;
     }
 
     /**
      * Returns the children of this Kit
      *
      * @return Returns the child-Kits of this Kit
      */
     public LinkedList<Kit> getChildren() {
         return children;
     }
 
     /**
      * Creates a new DragList with the {@param list} as content.
      * The values of this list are very important for the drag-and-drop
      * logic, it's recommended, that the values in this list are equals
      * to the key, which is responsible for identifying the result,
      * which is stored in the Hashmap.
      *
      * @param list key-values to be displayed and used to identify a result.
      * @return Returns a new DragList-object.
      */
     public DragList getDragList(Object[] list) {
         return new DragList(list, this);
     }
 
     /**
      * Creates a new DropTextField which is needed for the drag-and-drop
      * logic.
      *
      * @return Returns a new DropTextField-object.
      */
     public DropTextField getDropTextField() {
         return new DropTextField(this);
     }
 
     /**
      * This method is necessary for all the Kits that are constructed.
      * This method has to be overridden, everything inbetween this method is
      * executed when the start-button on the gui is pressed.
      *
      * @return Returns a String which represents the results of the computation
      * in the special Kit.
      */
     public String execute() {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void dispose() {
         handler.remove(this);
         handler.getDesktop().repaint();
         super.dispose();
     }
 
     /**
      * This class is used if the drag-and-drop logic is needed.
      * The elements (displayed) in this list are key-values for
      * the identification of former results.
      */
     public class DragList extends JList implements DragGestureListener {
 
         private Kit origin;
 
         /**
          * A new DragList can only be instanciated from the method getDragList()
          * in a Kit.
          *
          * @param field the key-values to be displayed.
          * @param origin the Kit in which this DragList is created.
          */
         DragList(Object[] field, Kit origin) {
             super(field);
             this.setDragEnabled(true);
             this.origin = origin;
             new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
         }
 
         public void dragGestureRecognized(DragGestureEvent dge) {
             parent = origin;
         }
     }
     private static Kit parent;
 
     /**
      * This class is used if the drag-and-drop logic is needed.
      * It's the drop-target for everything dragged by a DragList.
      */
     public class DropTextField extends JTextField implements DropTargetListener {
 
         private Kit origin, p;
         private String key;
 
         /**
          * A new DropTextField can only be instanciated
          * from the method getDropTextField() in a Kit.
          *
          * @param origin the Kit in which this DragList is created.
          */
         public DropTextField(Kit origin) {
             this.origin = origin;
             new DropTarget(this, this);
             this.addKeyListener(new KeyListener() {
 
                 public void keyTyped(KeyEvent e) {
                     keyTypedAction();
                     setColor(Color.black);
                     handler.getDesktop().repaint();
                 }
 
                 public void keyPressed(KeyEvent e) {
                 }
 
                 public void keyReleased(KeyEvent e) {
                 }
             });
         }
 
         public void setColor(Color c) {
             this.setForeground(c);
         }
 
         public void keyTypedAction() {
             p = null;
             key = null;
             handler.removeSameTarget(new Connection(null, null, (DropTextField) this));
         }
 
         public Object getResult() {
             if (p == null) {
                 return null;
             }
             return p.getResult(key);
         }
 
         /**
          * Used if a connection between two Kits is cutted,
          * when for example one Kit has been erased.
          */
         public void removeConnection() {
             key = null;
             p = null;
             this.setForeground(Color.black);
         }
 
         public void dragEnter(DropTargetDragEvent dtde) {
         }
 
         public void dragOver(DropTargetDragEvent dtde) {
         }
 
         public void dropActionChanged(DropTargetDragEvent dtde) {
         }
 
         public void dragExit(DropTargetEvent dte) {
         }
 
         public void drop(DropTargetDropEvent dtde) {
             try {
                 Transferable ta = dtde.getTransferable();
                 if (ta.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                     if (parent == origin) {
                        JOptionPane.showMessageDialog(null, xmlReader.getTagElement("Kit", "DropOnsameFrame"));
                     } else {
                         Connection con = new Connection(parent, origin, this);
                         Connection old = handler.removeSameTarget(con);
                         if (old != null) {
                             origin.parents.remove(old.getParent());
                         }
                         if (circle(parent, origin)) {
                             JOptionPane.showMessageDialog(null, xmlReader.getTagElement("Kit", "OnRecursion"));
                         } else if (!handler.add(con)) {
                             JOptionPane.showMessageDialog(null, xmlReader.getTagElement("Kit", "ConnectionAlreadyExists"));
                         } else {
                             dtde.acceptDrop(DnDConstants.ACTION_COPY);
                             String text = (String) ta.getTransferData(DataFlavor.stringFlavor);
                             this.setText(text);
                             dtde.getDropTargetContext().dropComplete(true);
                             origin.parents.add(parent);
                             parent.children.add(origin);
                             p = parent;
                             key = text;
                             setColor(Color.blue);
                             handler.getDesktop().repaint();
                         }
                     }
                 } else {
                     JOptionPane.showMessageDialog(null, xmlReader.getTagElement("Kit", "WrongData"));
                     dtde.rejectDrop();
                 }
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(null, e.getMessage());
                 e.printStackTrace();
             }
         }
 
         private boolean circle(Kit par, Kit ch) {
             if (par == ch) {
                 return true;
             }
             for (Kit k : par.getParents()) {
                 if (circle(k, ch)) {
                     return true;
                 }
             }
             return false;
         }
     }
 }
