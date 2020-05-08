 package de.aidger.view.tabs;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
 /**
  * Tab is the abstract class of all tabs that are located in the tabbed pane on
  * the main window.
  * 
  * @author aidGer Team
  */
 
 @SuppressWarnings("serial")
 public abstract class Tab extends JPanel {
 
     /**
      * The predecessor tab.
      */
     private Tab predecessor;
 
     /**
      * A flag whether the tab can be a predecessor or not.
      */
     private boolean noPredecessor = false;
 
     /**
      * Constructs a tab.
      */
     public Tab() {
         setBorder(BorderFactory.createTitledBorder(""));
     }
 
     /**
      * Get the name of the tab and constructor options if necessary.
      * 
      * @return A string representation of the class
      */
     @Override
     public String toString() {
         return getClass().getName();
     }
 
     /**
      * Returns the name of the tab.
      * 
      * @return The name of the tab
      */
     public abstract String getTabName();
 
     /**
      * Tab can perform actions before it is opened.
      */
     public void performBeforeOpen() {
     }
 
     /**
      * Tab can perform actions before it is closed.
      */
     public void performBeforeClose() {
     }
 
     /**
      * Sets the last predecessor.
      * 
      * @param p
      *            the last predecessor
      */
     public void setPredecessor(Tab p) {
         if (!p.noPredecessor) {
             predecessor = p;
         }
     }
 
     /**
      * Returns the last predecessor.
      */
     public Tab getPredecessor() {
         return predecessor;
     }
 
     /**
      * Marks the tab as no predecessor.
      */
    public void markAsPredecessor() {
        noPredecessor = false;
    }

    /**
     * Marks the tab as no predecessor.
     */
     public void markAsNoPredecessor() {
         noPredecessor = true;
     }
 
 }
