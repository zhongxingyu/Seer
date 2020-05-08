 /*
  * TrivialWizardFactory.java
  *
  * Created on February 22, 2005, 4:42 PM
  */
 
 package org.netbeans.api.wizard;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.ComponentOrientation;
 import java.awt.Cursor;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Insets;
 import java.awt.KeyboardFocusManager;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.List;
 import java.util.NoSuchElementException;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import org.netbeans.modules.wizard.MergeMap;
 import org.netbeans.modules.wizard.InstructionsPanel;
 import org.netbeans.spi.wizard.Wizard;
 import org.netbeans.spi.wizard.Wizard.WizardListener;
 import org.netbeans.spi.wizard.WizardException;
 
 /**
  * Default implementation of WizardFactory.
  *
  * @author Tim Boudreau
  */
 class TrivialWizardFactory extends WizardDisplayer {
     
     TrivialWizardFactory() {
     }
 
     //for unit tests
     static volatile JButton[] buttons;
     
     protected Object show(final Wizard wizard, Rectangle bounds) {
         final JPanel panel = new JPanel() {
             public Dimension getPreferredSize() {
                 Dimension d = super.getPreferredSize();
                 d.width = Math.max (600, d.width);
                 d.height = Math.max (400, d.height);
                 return d;
             }
         };
         if (wizard.getAllSteps().length == 0) {
             throw new IllegalArgumentException ("Wizard has no steps");
         }
         
         final JLabel ttlLabel = new JLabel (wizard.getStepDescription(wizard.getAllSteps()[0]));
         ttlLabel.setBorder (BorderFactory.createCompoundBorder(
                 BorderFactory.createEmptyBorder(5, 5, 12, 5),
                 BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("textText"))));
         JPanel ttlPanel = new JPanel() {
             public void doLayout() {
                 Dimension d = ttlLabel.getPreferredSize();
                 if (ttlLabel.getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT) {
                     ttlLabel.setBounds (getWidth() - d.width, 0, d.width, d.height);
                 } else {
                     ttlLabel.setBounds (0, 0, d.width, d.height);
                 }
             }
             
             public Dimension getPreferredSize() {
                 return ttlLabel.getPreferredSize();
             }
         };
         ttlPanel.add (ttlLabel);
         Font f = ttlLabel.getFont();
         if (f == null) {
             f = UIManager.getFont("controlFont"); //NOI18N
         }
         if (f != null) {
             f = f.deriveFont(Font.BOLD);
             ttlLabel.setFont(f);
         }
         
         panel.setLayout (new BorderLayout());
         final InstructionsPanel instructions = new InstructionsPanel (wizard);
         
         panel.setMinimumSize (new Dimension (500, 500));
         
         final JButton next = new JButton ("Next >");
         final JButton prev = new JButton ("< Prev");
         final JButton finish = new JButton ("Finish");
         final JButton cancel = new JButton ("Cancel");
         final JButton help = new JButton ("Help");
         
         next.setDefaultCapable(true);
         prev.setDefaultCapable(true);
         
         help.setVisible (false);
         
         final JPanel inner = new JPanel();
         inner.setLayout (new BorderLayout());
         inner.add (ttlPanel, BorderLayout.NORTH);
         
         final JLabel problem = new JLabel("  ");
         Color fg = UIManager.getColor ("nb.errorColor");
         problem.setForeground (fg == null ? Color.BLUE : fg);
         inner.add (problem, BorderLayout.SOUTH);
         problem.setPreferredSize (new Dimension (20,20));
         
         JPanel buttons = new JPanel() {
             public void doLayout() {
                 Insets ins = getInsets();
                 Dimension n = cancel.getPreferredSize();
                 int y = ((getHeight() - (ins.top + ins.bottom))/ 2) - (n.height / 2);
                 int gap = 5;
                 int x = getWidth() - (12 + ins.right + n.width);
                 cancel.setBounds (x, y, n.width, n.height);
 
                 n = finish.getPreferredSize();
                 x -= n.width + gap;
                 finish.setBounds (x, y, n.width, n.height);
                 
                 n = next.getPreferredSize();
                 x -= n.width + gap;
                 next.setBounds (x, y, n.width, n.height);
                 
                 n = prev.getPreferredSize();
                 x -= n.width + gap;
                 prev.setBounds (x, y, n.width, n.height);
                 
                 n = help.getPreferredSize();
                 x -= n.width + (gap * 2);
                 help.setBounds (x, y, n.width, n.height);
             }
         };
         buttons.setBorder (BorderFactory.createMatteBorder (1, 0, 0, 0, Color.BLACK));
         
         
         buttons.add (prev);
         buttons.add (next);
         buttons.add (finish);
         buttons.add (cancel);
         buttons.add (help);
 //        instructions.setLayout(new BoxLayout(instructions, BoxLayout.Y_AXIS));
         
         panel.add (instructions, BorderLayout.WEST);
         panel.add (buttons, BorderLayout.SOUTH);
         panel.add (inner, BorderLayout.CENTER);
         
         final List buttonlist = Arrays.asList(new JButton[] {
             next, prev, finish, cancel
         });
         
         if (Boolean.getBoolean ("TrivialWizardFactory.test")) { //enable unit tests
             TrivialWizardFactory.buttons = (JButton[]) buttonlist.toArray (new JButton[0]);
         }
         
         String first = wizard.getAllSteps()[0];
         final MergeMap settings = new MergeMap (first);
         final JComponent[] centerPanel = new JComponent[] {
             wizard.navigatingTo(first, settings)
         };
         instructions.setCurrentStep (first);
         inner.add (centerPanel[0], BorderLayout.CENTER);
         prev.setEnabled (false);
         next.setEnabled (wizard.getNextStep() != null);
         finish.setEnabled (wizard.canFinish());
         
         final Object[] result = new Object[] { null };
         
         ActionListener buttonListener = new ActionListener() {
             public void actionPerformed (ActionEvent ae) {
                 int action = buttonlist.indexOf (ae.getSource());
                 JComponent currCenter = centerPanel[0];
                 switch (action) {
                     case 0 : //next
                         String nextId = wizard.getNextStep();
                         settings.push(nextId);
                         JComponent comp = wizard.navigatingTo (nextId, settings);
                         instructions.setCurrentStep (nextId);
                         ttlLabel.setText(wizard.getStepDescription(nextId));
                         inner.add (comp, BorderLayout.CENTER);
                         inner.remove (currCenter);
                         inner.invalidate();
                         inner.revalidate();
                         inner.repaint();
                         centerPanel[0] = comp;
                         comp.requestFocus();
                         update();
                         break;
                     case 1 : //prev
                         String prevId = wizard.getPreviousStep();
                         settings.popAndCalve();
                         JComponent pcomp = wizard.navigatingTo (prevId, settings);
                         instructions.setCurrentStep (prevId);
                         ttlLabel.setText(wizard.getStepDescription(prevId));
                         inner.add (pcomp, BorderLayout.CENTER);
                         inner.remove (currCenter);
                         centerPanel[0] = pcomp;
                         inner.invalidate();
                         inner.revalidate();
                         inner.repaint();
                         pcomp.requestFocus();
                         update();
                         break;
                     case 2 : //finish
                         try {
                             result[0] = wizard.finish(settings);
                         } catch (WizardException we) {
                             JOptionPane pane = new JOptionPane (we.getLocalizedMessage());
                             pane.setVisible(true);
                             String id = we.getStepToReturnTo();
                             String curr = settings.currID();
                             try {
                                 while (id != null && !id.equals(curr)) {
                                     curr = curr = settings.popAndCalve();
                                 }
                                 settings.push (id);
                                 JComponent comp1 = wizard.navigatingTo (id, settings);
                                 instructions.setCurrentStep (id);
                                 ttlLabel.setText(wizard.getStepDescription(id));
                                 if (comp1 != centerPanel[0]) {
                                     inner.add (comp1, BorderLayout.CENTER);
                                     inner.remove (centerPanel[0]);
                                     centerPanel[0] = comp1;
                                     inner.validate();
                                     inner.repaint();
                                     comp1.requestFocus();
                                 }
                             } catch (NoSuchElementException ex) {
                                 throw new IllegalStateException ("Exception " +
                                     "said to return to " + id + " but no such " +
                                     "step found");
                             }
                         }
                         
                         //Note no break
                         
                     case 3 : //cancel
                         Dialog dlg = (Dialog) 
                             ((JComponent) ae.getSource()).getTopLevelAncestor();
                         dlg.setVisible(false);
                         dlg.dispose();
                         break;
                     default : assert false;
                     
                     
                 }
                 String prob = wizard.getProblem();
                 Border b = prob == null ? BorderFactory.createEmptyBorder (1, 0, 0, 0)
                     : BorderFactory.createMatteBorder (1, 0, 0, 0, problem.getForeground());
                 
                 Border b1 = BorderFactory.createCompoundBorder (
                         BorderFactory.createEmptyBorder (0, 12, 0, 12), b);
                 
                 problem.setBorder (b1);
                problem.setText (prob == null ? " " : prob);
                
                
             }
             
             private void update() {
                 if (!wizard.isBusy()) {
                     next.setEnabled (wizard.getNextStep() != null);
                     finish.setEnabled (wizard.canFinish());
                     prev.setEnabled (wizard.getPreviousStep() != null);
                 }
                 if (next.isEnabled()) {
                     next.getRootPane().setDefaultButton(next);
                 } else if (finish.isEnabled()) {
                     finish.getRootPane().setDefaultButton(finish);
                 }
             }
         };
         next.addActionListener(buttonListener);
         prev.addActionListener(buttonListener);
         finish.addActionListener(buttonListener);
         cancel.addActionListener(buttonListener);
         
         final WizardListener l = new WizardListener() {
             boolean wasBusy = false;
             public void stepsChanged(Wizard wizard) {
                 //do nothing
             }
             public void navigabilityChanged(Wizard wizard) {
                 if (wizard.isBusy()) {
                     next.setEnabled(false);
                     prev.setEnabled(false);
                     finish.setEnabled(false);
                     cancel.setEnabled(false);
                     panel.setCursor (Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     wasBusy = true;
                     return;
                 } else if (wasBusy) {
                     cancel.setEnabled(true);
                     panel.setCursor (Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 }
                 next.setEnabled (wizard.getNextStep() != null);
                 prev.setEnabled (wizard.getPreviousStep() != null);
                 finish.setEnabled (wizard.canFinish());
                 if (finish.getRootPane() != null) {
                     if (finish.isEnabled()) {
                         finish.getRootPane().setDefaultButton(finish);
                     } else if (next.isEnabled()) {
                         next.getRootPane().setDefaultButton(next);
                     } else {
                         prev.getRootPane().setDefaultButton(null);
                     }
                 }
                 String prob = wizard.getProblem();
                 Border b = prob == null ? BorderFactory.createEmptyBorder (1, 0, 0, 0)
                     : BorderFactory.createMatteBorder (1, 0, 0, 0, problem.getForeground());
                 
                 Border b1 = BorderFactory.createCompoundBorder (
                         BorderFactory.createEmptyBorder (0, 12, 0, 12), b);
                 
                 problem.setBorder (b1);
                 problem.setText (prob == null ? " " : prob);
             }
         };
         l.stepsChanged(wizard);
         l.navigabilityChanged(wizard);
         wizard.addWizardListener (l);
         
         JDialog dlg;
         Object o = findLikelyOwnerWindow();
         if (o instanceof Frame) {
             dlg = new JDialog((Frame) o);
         } else if (o instanceof Dialog) {
             dlg = new JDialog ((Dialog) o);
         } else {
             dlg = new JDialog();
         }
         dlg.setTitle (wizard.getTitle());
         dlg.getContentPane().setLayout (new BorderLayout());
         dlg.getContentPane().add (panel, BorderLayout.CENTER);
         if (bounds != null) {
             dlg.setBounds(bounds);
         } else {
             dlg.pack();
         }
         Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
         //XXX get screen insets?
         int x = (d.width / 2) - (dlg.getWidth() / 2);
         int y = (d.height / 2) - (dlg.getWidth() / 2);
         dlg.setLocation(x, y);
         
         dlg.setModal (true);
         dlg.getRootPane().setDefaultButton (next);
         dlg.setVisible(true);
         
         return result[0];
     }
     
     private Window findLikelyOwnerWindow() {
         return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
     }
     
 }
