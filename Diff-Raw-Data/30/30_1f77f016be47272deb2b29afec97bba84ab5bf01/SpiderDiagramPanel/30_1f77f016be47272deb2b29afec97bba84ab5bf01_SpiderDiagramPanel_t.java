 /*
  *   Project: Speedith
  * 
  * File name: SpiderDiagramPanel.java
  *    Author: Matej Urbas [matej.urbas@gmail.com]
  * 
  *  Copyright © 2011 Matej Urbas
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 /*
  * SpiderDiagramPanel.java
  *
  * Created on 29-Sep-2011, 13:46:10
  */
 package speedith.draw;
 
 import java.awt.GridBagConstraints;
 import icircles.util.CannotDrawException;
 import java.util.Iterator;
 import javax.swing.JLabel;
import javax.swing.JPanel;
 import speedith.core.lang.CompoundSpiderDiagram;
 import speedith.core.lang.NullSpiderDiagram;
 import speedith.core.lang.PrimarySpiderDiagram;
 import speedith.core.lang.SpiderDiagram;
 import speedith.core.lang.reader.ReadingException;
 import speedith.core.lang.reader.SpiderDiagramsReader;
 import static speedith.i18n.Translations.*;
 
 /**
  * This panel displays all types of {@link SpiderDiagram spider diagrams}.
  * @author Matej Urbas [matej.urbas@gmail.com]
  */
 public class SpiderDiagramPanel extends javax.swing.JPanel {
 
     //<editor-fold defaultstate="collapsed" desc="Constructors">
     /**
      * Creates a new compound spider diagram panel with nothing displayed on it.
      * <p>You can set a diagram to be shown through {@link SpiderDiagramPanel#setDiagram(speedith.core.lang.SpiderDiagram)}.</p>
      */
     public SpiderDiagramPanel() {
         this(null);
     }
 
     /**
      * Creates a new compound spider diagram panel with the given compound spider
      * diagram.
      * @param diagram the diagram to display in this panel.
      * <p>May be {@code null} in which case nothing will be displayed.</p>
      */
     public SpiderDiagramPanel(SpiderDiagram diagram) {
         initComponents();
         setDiagram(diagram);
     }
     //</editor-fold>
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         setBackground(new java.awt.Color(255, 255, 255));
         setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
         setLayout(new java.awt.GridBagLayout());
     }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     // End of variables declaration//GEN-END:variables
     // <editor-fold defaultstate="collapsed" desc="Private Fields">
     private SpiderDiagram diagram;
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Public Properties">
     /**
      * Returns the currently presented diagram.
      * <p>May be {@code null}, which indicates that the panel is empty.</p>
      * @return the currently presented diagram, or {@code null} is no diagram
      * is currently being shown.
      */
     public SpiderDiagram getDiagram() {
         return diagram;
     }
 
     /**
      * Sets the currently shown diagram.
      * <p>May be {@code null}, which indicates that the panel should
      * be empty.</p>
      * @param diagram the new diagram to show, or {@code null} if no diagram is
      * to be shown.
      */
     public final void setDiagram(SpiderDiagram diagram) {
         if (this.diagram != diagram) {
             this.diagram = diagram;
             this.removeAll();
             if (this.diagram != null) {
                 try {
                     drawDiagram();
                 } catch (Exception ex) {
                     drawErrorLabel();
                 }
             } else {
                 drawNoDiagramLabel();
             }
             validate();
             repaint();
         }
     }
 
     /**
      * Returns the string representation of the currently shown diagram.
      * @return the string representation of the currently shown diagram.
      */
     public String getDiagramString() {
         return this.diagram == null ? "" : this.diagram.toString();
     }
 
     /**
      * Sets the currently shown diagram via its string representation.
      * <p>This method tries to parse the string into a spider diagram and
      * draws it.</p>
      * <p>Here is an example of a valid string:
      * <pre>BinarySD {
      *      operator = "op -->",
      *      arg1 = PrimarySD {spiders = ["s1", "s2", "s3"], habitats = [("s1", [(["A"], ["B", "C"]), (["A", "B"], ["C"]), (["A", "B", "C"], [])]), ("s2", [(["B"], ["A", "C"])]), ("s3", [(["B"], ["A", "C"])])], sh_zones = [(["B"], ["A", "C"])]},
      *      arg2 = PrimarySD {spiders = ["s1", "s2", "s3"], habitats = [("s1", [(["A"], ["B", "C"]), (["A", "B"], ["C"]), (["A", "B", "C"], [])]), ("s2", [(["B"], ["A", "C"])]), ("s3", [(["B"], ["A", "C"])])], sh_zones = [(["B"], ["A", "C"])]}
      * }</pre>
      * </p>
      * @param diagram the string representation of the diagram to present.
      * @throws ReadingException thrown if the string does not represent a valid
      * spider diagram.
      * @throws IllegalArgumentException thrown if the string is a valid spider
      * diagram but not a primary spider diagram.
      */
     public void setDiagramString(String diagram) throws ReadingException {
         if (diagram == null || diagram.isEmpty()) {
             setDiagram(null);
         } else {
             SpiderDiagram sd = SpiderDiagramsReader.readSpiderDiagram(diagram);
             if (sd instanceof SpiderDiagram) {
                 setDiagram((SpiderDiagram) sd);
             } else {
                 throw new IllegalArgumentException(i18n("CSD_PANEL_INVALID_DIAGRAM_STRING"));
             }
         }
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Private Helper Methods">
     /**
      * This method does not remove any components, it just adds an error label
      * saying 'Drawing failed'.
      */
     private void drawErrorLabel() {
         JLabel errorLabel = new JLabel();
         errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         errorLabel.setText(i18n("PSD_LABEL_DISPLAY_ERROR"));
         add(errorLabel);
         invalidate();
     }
 
     /**
      * This method does not remove any components, it just adds an error label
      * saying 'No diagram'.
      */
     private void drawNoDiagramLabel() {
         JLabel noDiagramLbl = new JLabel();
         noDiagramLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         noDiagramLbl.setText(i18n("CSD_PANEL_NO_DIAGRAM"));
         add(noDiagramLbl);
         invalidate();
     }
 
     /**
      * This method puts the diagram panels onto this one.
      * <p>This method does not remove any components, it just adds them to this
      * panel.</p>
      * <p>In case of failure, this method throws an exception and does not put
      * any components onto this panel.</p>
      */
     private void drawDiagram() throws CannotDrawException {
         if (diagram != null) {
             if (diagram instanceof CompoundSpiderDiagram) {
                 CompoundSpiderDiagram csd = (CompoundSpiderDiagram) diagram;
                 switch (csd.getOperator()) {
                     case Conjunction:
                     case Disjunction:
                     case Equivalence:
                     case Implication:
                         drawInfixDiagram(csd);
                         break;
                     case Negation:
                         drawPrefixDiagram(csd);
                         break;
                     default:
                         throw new AssertionError(i18n("GERR_ILLEGAL_STATE"));
                 }
             } else if (diagram instanceof PrimarySpiderDiagram) {
                 drawPrimaryDiagram((PrimarySpiderDiagram) diagram);
             } else if (diagram instanceof NullSpiderDiagram) {
                 drawNullSpiderDiagram();
             } else {
                 throw new IllegalArgumentException(i18n("SD_PANEL_UNKNOWN_DIAGRAM_TYPE"));
             }
         }
     }
 
     private void drawInfixDiagram(CompoundSpiderDiagram csd) throws CannotDrawException {
         if (csd != null && csd.getOperandCount() > 0) {
             GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
 
             Iterator<SpiderDiagram> it = csd.getOperands().iterator();
             gridBagConstraints.gridx = 1;
             add(DiagramVisualisation.getSpiderDiagramPanel(it.next()), gridBagConstraints);
             while (it.hasNext()) {
                 ++gridBagConstraints.gridx;
                 add(new OperatorPanel(csd.getOperator()), gridBagConstraints);
                 ++gridBagConstraints.gridx;
                 add(DiagramVisualisation.getSpiderDiagramPanel(it.next()), gridBagConstraints);
             }
             invalidate();
         } else {
             throw new AssertionError(i18n("GERR_ILLEGAL_STATE"));
         }
     }
 
     private void drawPrefixDiagram(CompoundSpiderDiagram csd) throws CannotDrawException {
         if (csd != null && csd.getOperandCount() == 1) {
             add(new OperatorPanel(csd.getOperator()));
             add(DiagramVisualisation.getSpiderDiagramPanel(csd.getOperands().get(0)));
             invalidate();
         } else {
             throw new AssertionError(i18n("GERR_ILLEGAL_STATE"));
         }
     }
 
    private void drawPrimaryDiagram(PrimarySpiderDiagram psd) throws CannotDrawException {
         if (psd == null) {
             throw new AssertionError(i18n("GERR_ILLEGAL_STATE"));
         } else {
            GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(DiagramVisualisation.getSpiderDiagramPanel(psd), gbc);
            invalidate();
         }
     }
 
     private void drawNullSpiderDiagram() {
         add(new NullSpiderDiagramPanel());
     }
     // </editor-fold>
 }
