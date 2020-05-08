 package org.geworkbench.util;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: First Genetic Trust Inc.</p>
  * <p/>
  * Implements an generic progress bar to show status of progress of
  * application components.
  *
  * @author manjunath at genomecenter dot columbia dot edu
  * @version 2.0
  */
 
 public class ProgressBar
     extends JDialog {
     /**
      * Defines a ProgressBar that has bounds and values shown increment from a
      * minimum to a maximum
      */
     public static final int BOUNDED_TYPE = 0;
     /**
      * Defines a ProgressBar that animates cyclically till asked to stop
      */
     public static final int INDETERMINATE_TYPE = 1;
     /**
      * Instance type
      */
     private int type = BOUNDED_TYPE;
     /**
      * Visual widget
      */
     private BorderLayout borderLayout1 = new BorderLayout();
     /**
      * Visual widget
      */
     private JProgressBar jProgressBar = new JProgressBar();
     /**
      * Visual widget
      */
     private JButton cancelButton = new JButton("Cancel");
     /**
      * Visual widget
      */
     private JPanel jPanel1 = new JPanel();
     /**
      * Singleton representing a <code>ProgressBar</code> with a specified range
      * This needs to set before using the instance
      */
     private static ProgressBar boundedProgressBar = new ProgressBar(
         BOUNDED_TYPE);
 
     /**
      * Observable class used to track when this progress bar is disposed
      */
     private class BarObservable
         extends Observable {
         public void setChanged() {
             super.setChanged();
         }
     }
 
     private BarObservable winDisposed = null;
 
     private class BarWindowAdapter
         extends java.awt.event.WindowAdapter implements java.awt.event.ActionListener {
         public void windowClosing(java.awt.event.WindowEvent e) {
             doAction();
         }
         
         public void actionPerformed(java.awt.event.ActionEvent e){
         	doAction();
         	ProgressBar.this.dispose();
         }
         
         public void doAction(){
         	winDisposed.setChanged();
             winDisposed.notifyObservers();
         }
 
     }
 
     /**
      * Singleton representing a <code>ProgressBar</code> without a range
      * This needs to set before using the instance
      */
     private static ProgressBar indeterminateProgressBar =
         new ProgressBar(INDETERMINATE_TYPE);
     JLabel jLabel1 = new JLabel();
     GridLayout gridLayout1 = new GridLayout(3, 1);
     /**
      * Factory method to create one of the two above defined ProgressBar types
      * @param type type of the <code>ProgressBar</code>
      * @return <code>ProgressBar</code> instance
      */
     public static ProgressBar create(int type) {
         if (type == BOUNDED_TYPE) {
             if (boundedProgressBar.isShowing()) {
                 boundedProgressBar = new ProgressBar(BOUNDED_TYPE);
             }
             return boundedProgressBar;
         }
 
         else if (type == INDETERMINATE_TYPE) {
             if (indeterminateProgressBar.isShowing()) {
                 indeterminateProgressBar = new ProgressBar(INDETERMINATE_TYPE);
             }
             return indeterminateProgressBar;
         }
 
         return null;
     }
 
     /**
      * Sets the bounds if type == BOUNDED_TYPE
      * @param brm <code>BoundedRangeModel</code> encapsulating range and extent
      * of values to be used for animation
      */
     public void setBounds(BoundedRangeModel brm) {
         if (type == BOUNDED_TYPE) {
             jProgressBar.setModel(brm);
         }
     }
 
     /**
      * Updates the ProgressBar by an amount specified in the <code>
      * BoundedRangeModel</code>
      * @return status of updation
      */
     public boolean update() {
         if (type == BOUNDED_TYPE) {
             int inc = ( (IncrementModel) jProgressBar.getModel()).getIncrement();
             int value = jProgressBar.getValue();
             int nv = value + inc;
             if (nv <= jProgressBar.getMaximum()) {
                 jProgressBar.setValue(value + inc);
                 jProgressBar.setString(Integer.toString(value + inc));
 
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Updates the ProgressBar to an amount specified
      * @param value value that the <code>ProgressBar</code> has to be set to
      * @return status of updation if <code>value</code> is
      * outside the range in the <code> BoundedRangeModel</code>
      */
     public boolean updateTo(float value) {
         if (type == BOUNDED_TYPE) {
             if ((int) value <= jProgressBar.getMaximum()) {
                 jProgressBar.setValue((int) value);
                 jProgressBar.setString(Integer.toString((int) value));
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Starts animation and shows the <code>ProgressBar</code>
      */
     public void start() {
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
         this.setLocation(
             (dim.width - this.getWidth()) / 2,
             (dim.height - this.getHeight()) / 2);
         this.setVisible(true);
         updateTo(jProgressBar.getMinimum());
     }
 
     /**
      * Stops animation
      */
     public void stop() {
         dispose();
     }
 
     /**
      * Resets the animation
      */
     public void reset() {
         updateTo(jProgressBar.getMinimum());
     }
 
     /**
      * Gets time period of animation
      * @return time period of animation in milli seconds
      */
     public double getDuration() {
         return 0d;
     }
 
     /**
      * Hides the <code>ProgressBar</code>
      */
     public void dispose() {
         this.setTitle("");
         this.setMessage("");
         this.setVisible(false);
     }
 
     /**
      * Sets the title
      * @param title title of the <code>ProgressBar</code>
      */
     public void setTitle(String title) {
         super.setTitle(title);
     }
 
     /**
      * Sets the message
      * @param message to be shown on the <code>ProgressBar</code>
      */
     public void setMessage(String message) {
         jProgressBar.setToolTipText(message);
         jLabel1.setText(message);
        this.pack();
     }
 
     /**
      * Bit to specify if the current value of the <code>ProgressBar</code> has
      * to be printed on the widget. Default is true.
      * @param show if current value and bounds have to printed
      */
     public void showValues(boolean show) {
         if (type == BOUNDED_TYPE) {
             jProgressBar.setStringPainted(show);
         }
     }
 
     /**
      * Constructor for internal creation of instances. Objects of this type can
      * only be obtained by using the <code>create</code> method
      * @param t type of the <code>ProgressBar</code>
      */
     private ProgressBar(int t) {
         super();
         if (t == BOUNDED_TYPE) {
             type = t;
             jProgressBar.setIndeterminate(false);
             jProgressBar.setStringPainted(true);
         }
 
         else if (t == INDETERMINATE_TYPE) {
             type = t;
             jProgressBar.setIndeterminate(true);
             jProgressBar.setStringPainted(false);
         }
 
         try {
             jbInit();
         }
 
         catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
     /**
      * Utility method for constructing GUI
      * @throws Exception exception encountered while GUI construction
      */
     private void jbInit() throws Exception {
         this.getContentPane().setLayout(borderLayout1);
         jProgressBar.setBorder(BorderFactory.createLoweredBevelBorder());
         jProgressBar.setMinimumSize(new Dimension(300, 22));
         jProgressBar.setPreferredSize(new Dimension(300, 22));
         jProgressBar.setRequestFocusEnabled(true);
         jProgressBar.setBorderPainted(true);
         cancelButton.setMinimumSize(new Dimension(30, 22));
         cancelButton.setPreferredSize(new Dimension(30, 22));
         cancelButton.setRequestFocusEnabled(true);
         jPanel1.setPreferredSize(new Dimension(320,50));
         jPanel1.setLayout(gridLayout1);
         jLabel1.setToolTipText("");
         jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
         jLabel1.setText("Message");
         jLabel1.setVerticalTextPosition(SwingConstants.CENTER);
         jPanel1.add(jProgressBar);
         jPanel1.add(jLabel1);
         JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
         buttonPanel.add(new JLabel(""));
         buttonPanel.add(cancelButton);
         buttonPanel.add(new JLabel(""));
         jPanel1.add(buttonPanel);
         JPanel spaceNorthPane = new JPanel();
         spaceNorthPane.setMinimumSize(new Dimension(300, 5));
         spaceNorthPane.setPreferredSize(new Dimension(300, 5));
         JPanel spaceSouthPane = new JPanel();
         spaceSouthPane.setMinimumSize(new Dimension(300, 5));
         spaceSouthPane.setPreferredSize(new Dimension(300, 5));
         JPanel spaceEastPane = new JPanel();
         spaceEastPane.setMinimumSize(new Dimension(10, 100));
         spaceEastPane.setPreferredSize(new Dimension(10, 100));
         JPanel spaceWestPane = new JPanel();
         spaceWestPane.setMinimumSize(new Dimension(10, 100));
         spaceWestPane.setPreferredSize(new Dimension(10, 100));
         this.getContentPane().add(jPanel1, BorderLayout.CENTER);
         this.getContentPane().add(spaceNorthPane, BorderLayout.NORTH);
         this.getContentPane().add(spaceSouthPane, BorderLayout.SOUTH);
         this.getContentPane().add(spaceEastPane, BorderLayout.EAST);
         this.getContentPane().add(spaceWestPane, BorderLayout.WEST);
         this.setSize(new Dimension(320, 110));
         this.setTitle("Progress Bar");
         winDisposed = new BarObservable();
 
         BarWindowAdapter bwa = new BarWindowAdapter();
         this.addWindowListener(bwa);
         cancelButton.addActionListener(bwa);
     }
 
     public boolean hasChanged() {
         return winDisposed.hasChanged();
     }
 
     public void addObserver(Observer o) {
         winDisposed.addObserver(o);
     }
 
     /**
      * Utility class that encapsulates a step size for the
      * <code>ProgressBar</code> display
      */
     public static class IncrementModel
         extends DefaultBoundedRangeModel {
         private int increment = 0;
         public IncrementModel(int value, int extent, int min, int max, int inc) {
             super(value, extent, min, max);
             increment = inc;
         }
 
         public int getIncrement() {
             return increment;
         }
 
         public void setIncrement(int inc) {
             increment = inc;
         }
     }
 }
