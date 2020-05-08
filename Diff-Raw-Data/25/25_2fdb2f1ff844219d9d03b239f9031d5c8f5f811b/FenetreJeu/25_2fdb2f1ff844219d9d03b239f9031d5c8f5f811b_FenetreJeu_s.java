 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package UI;
 
 import client.Client;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JScrollBar;
 import javax.swing.SizeRequirements;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Element;
 import javax.swing.text.View;
 import javax.swing.text.ViewFactory;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.InlineView;
 import javax.swing.text.html.ParagraphView;
 
 /**
  *
  * @author Watre
  */
 public class FenetreJeu extends javax.swing.JFrame {
 
     //private DefaultCaret caret;
     private boolean vide = true;
     private boolean scrollVisible = true;
     private boolean scrollAuto;
     private int scrollMax;
     private boolean enBas = true;
 
     /**
      * Creates new form FenetreJeu
      */
     public FenetreJeu() {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
             Logger.getLogger(FenetreConnexion.class.getName()).log(Level.SEVERE, null, ex);
         }
         initComponents();
         afficheurHTML.setEditorKit(new HTMLEditorKit() {
 
             @Override
             public ViewFactory getViewFactory() {
 
                 return new HTMLFactory() {
 
                     @Override
                     public View create(Element e) {
                         View v = super.create(e);
                         //if(v.getClass().getName().equals("javax.swing.text.html.InlineView")){ 
                       /*
                          * if(v instanceof InlineView){ return new
                          * InlineView(e){ public int getBreakWeight(int axis,
                          * float pos, float len) { return GoodBreakWeight; }
                          * public View breakView(int axis, int p0, float pos,
                          * float len) { if(axis == View.X_AXIS) {
                          * checkPainter(); int p1 =
                          * getGlyphPainter().getBoundedPosition(this, p0, pos,
                          * len); if(p0 == getStartOffset() && p1 ==
                          * getEndOffset()) { return this; } return
                          * createFragment(p0, p1); } return this; } }; 
                       }
                          */
                         if (v instanceof ParagraphView) {
                             return new ParagraphView(e) {
 
                                 @Override
                                 protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
                                     if (r == null) {
                                         r = new SizeRequirements();
                                     }
                                     float pref = layoutPool.getPreferredSpan(axis);
                                     float min = layoutPool.getMinimumSpan(axis);
                                     // Don't include insets, Box.getXXXSpan will include them. 
                                     r.minimum = (int) min;
                                     r.preferred = Math.max(r.minimum, (int) pref);
                                     r.maximum = Integer.MAX_VALUE;
                                     r.alignment = 0.5f;
                                     return r;
                                 }
                             };
                         }
                         return v;
                     }
                 };
             }
         });
         /*
          * caret = (DefaultCaret) afficheurHTML.getCaret();
          * caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
          */
 
         /*
          * jScrollPane1.getVerticalScrollBar().addAdjustmentListener(new
          * AdjustmentListener() {
          *
          * BoundedRangeModel brm =
          * jScrollPane1.getVerticalScrollBar().getModel(); boolean wasAtBottom =
          * true;
          *
          * @Override public void adjustmentValueChanged(AdjustmentEvent e) { if
          * (!brm.getValueIsAdjusting()) { if (wasAtBottom) {
          * System.out.println(System.currentTimeMillis());
          * brm.setValue(brm.getMaximum()); } } else { wasAtBottom =
          * ((brm.getValue() + brm.getExtent()) == brm.getMaximum());
          * System.out.println(brm.getValue() + " "+brm.getExtent() + "
          * "+brm.getMaximum()); }
          *
          * }
          * });
          */
 
         /*
          * final JScrollBar scroll = jScrollPane1.getVerticalScrollBar();
          * scroll.addAdjustmentListener(new AdjustmentListener() {
          *
          * @Override public void adjustmentValueChanged(AdjustmentEvent e) {
          * boolean enbas = true; if
          * (jScrollPane1.getVerticalScrollBar().getValue() <
          * jScrollPane1.getVerticalScrollBar().getMaximum()) {
          * //System.out.println(jScrollPane1.getVerticalScrollBar().getValue() +
          * jScrollPane1.getVerticalScrollBar().getVisibleAmount() + " " +
          * jScrollPane1.getVerticalScrollBar().getMaximum()); if (enbas) {
          * scroll.setValue(scroll.getMaximum()); }
          *
          *
          * if (scroll.getValue() + scroll.getVisibleAmount() ==
          * scroll.getMaximum()) { enbas = true; System.out.println("en bas"); }
          * else { enbas = false; } } else { System.out.println("en bas"); } }
          * });
          */
 
         final JScrollBar scroll = jScrollPane1.getVerticalScrollBar();
         scroll.addAdjustmentListener(new AdjustmentListener() {
 
             @Override
             public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!scrollVisible) {
                     scroll.setValue(scroll.getMaximum() - scroll.getVisibleAmount());
                     scrollVisible = true;
                }
 
                 if (scrollAuto) {
                     scroll.setValue(scroll.getMaximum() - scroll.getVisibleAmount());
                     scrollAuto = false;
                 }
                 /*
                  * else if(!scrollVisible || scroll.getValue() +
                  * scroll.getVisibleAmount() == scroll.getMaximum()) {
                  * enBas=true; } else enBas=false;
                  */
             }
         });
 
         barreInput.requestFocus();
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jFormattedTextField1 = new javax.swing.JFormattedTextField();
         barreInput = new javax.swing.JTextField();
         jScrollPane1 = new javax.swing.JScrollPane();
         afficheurHTML = new javax.swing.JTextPane();
 
         jFormattedTextField1.setText("jFormattedTextField1");
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Citysides Alpha");
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
         addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentResized(java.awt.event.ComponentEvent evt) {
                 formComponentResized(evt);
             }
         });
 
         barreInput.setToolTipText("Entrez vos messages ici");
         barreInput.setFocusCycleRoot(true);
         barreInput.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 barreInputKeyPressed(evt);
             }
         });
 
         afficheurHTML.setContentType("text/html");
         afficheurHTML.setEditable(false);
         afficheurHTML.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
         afficheurHTML.setFocusCycleRoot(false);
         jScrollPane1.setViewportView(afficheurHTML);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(barreInput)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(barreInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void barreInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_barreInputKeyPressed
         if (evt.getKeyCode() == KeyEvent.VK_ENTER && !barreInput.getText().equals("")) {
             Client.envoyerText(barreInput.getText());
             barreInput.setText(null);
         }
     }//GEN-LAST:event_barreInputKeyPressed
 
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         Client.deconnexion();
     }//GEN-LAST:event_formWindowClosing
 
     private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        //System.out.println(enBas);

         if (enBas) {
             JScrollBar scroll = jScrollPane1.getVerticalScrollBar();
             scroll.setValue(scroll.getMaximum() - scroll.getVisibleAmount());
         }
     }//GEN-LAST:event_formComponentResized
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /*
          * Set the Nimbus look and feel
          */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         /*
          * try { for (javax.swing.UIManager.LookAndFeelInfo info :
          * javax.swing.UIManager.getInstalledLookAndFeels()) { if
          * ("Nimbus".equals(info.getName())) {
          * javax.swing.UIManager.setLookAndFeel(info.getClassName()); break; } }
          * } catch (ClassNotFoundException ex) {
          * java.util.logging.Logger.getLogger(FenetreJeu.class.getName()).log(java.util.logging.Level.SEVERE,
          * null, ex); } catch (InstantiationException ex) {
          * java.util.logging.Logger.getLogger(FenetreJeu.class.getName()).log(java.util.logging.Level.SEVERE,
          * null, ex); } catch (IllegalAccessException ex) {
          * java.util.logging.Logger.getLogger(FenetreJeu.class.getName()).log(java.util.logging.Level.SEVERE,
          * null, ex); } catch (javax.swing.UnsupportedLookAndFeelException ex) {
          * java.util.logging.Logger.getLogger(FenetreJeu.class.getName()).log(java.util.logging.Level.SEVERE,
          * null, ex); } //</editor-fold>
          *
          * /*
          * Create and display the form
          */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new FenetreJeu().setVisible(true);
             }
         });
     }
 
     public void ecrire(String s) {
         JScrollBar scroll = jScrollPane1.getVerticalScrollBar();
         scrollMax = scroll.getMaximum();
         scrollVisible = scroll.isShowing();
 
        if (!scrollVisible || scroll.getValue() + scroll.getVisibleAmount() == scrollMax) {
            enBas = true;
             scrollAuto = true;
             //scrollMax = scroll.getMaximum();
         } else {
             enBas = false;
             scrollAuto = false;
         }
 
         HTMLDocument d = (HTMLDocument) afficheurHTML.getStyledDocument();
         Element a = d.getParagraphElement(1);
         //Element a=d.getElement("conversation");
         try {
             if (!vide) {
                 d.insertBeforeEnd(a, "<br />" + s);
             } else {
                 d.insertBeforeEnd(a, s);
                 vide = false;
             }
         } catch (BadLocationException | IOException ex) {
             Logger.getLogger(FenetreJeu.class.getName()).log(Level.SEVERE, null, ex);
         }
 

 
         /*
          * if (enBas) { while (scroll.getMaximum() == scrollMax) { try {
          * Thread.sleep(5);
          *
          *
          * } catch (InterruptedException ex) {
          * Logger.getLogger(FenetreJeu.class.getName()).log(Level.SEVERE, null,
          * ex); } } scroll.setValue(scroll.getMaximum() -
          * scroll.getVisibleAmount()); }
          */
 
         /*
          * if (jScrollPane1.getVerticalScrollBar().getValue() ==
          * jScrollPane1.getVerticalScrollBar().getMaximum()) {
          * afficheurHTML.setCaretPosition(afficheurHTML.getDocument().getLength());
          * }
          */
         //System.out.println(afficheurHTML.getText());
         //System.out.println(jScrollPane1.getVerticalScrollBar().getMaximum());
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JTextPane afficheurHTML;
     private javax.swing.JTextField barreInput;
     private javax.swing.JFormattedTextField jFormattedTextField1;
     private javax.swing.JScrollPane jScrollPane1;
     // End of variables declaration//GEN-END:variables
 }
