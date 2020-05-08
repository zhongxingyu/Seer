 /*
  * OffenePflege
  * Copyright (C) 2006-2012 Torsten Löhr
  * This program is free software; you can redistribute it and/or modify it under the terms of the 
  * GNU General Public License V2 as published by the Free Software Foundation
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even 
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program; if not, write to 
  * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
  * www.offene-pflege.de
  * ------------------------ 
  * Auf deutsch (freie Übersetzung. Rechtlich gilt die englische Version)
  * Dieses Programm ist freie Software. Sie können es unter den Bedingungen der GNU General Public License, 
  * wie von der Free Software Foundation veröffentlicht, weitergeben und/oder modifizieren, gemäß Version 2 der Lizenz.
  *
  * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen von Nutzen sein wird, aber 
  * OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN 
  * BESTIMMTEN ZWECK. Details finden Sie in der GNU General Public License.
  *
  * Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm erhalten haben. Falls nicht, 
  * schreiben Sie an die Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA.
  * 
  */
 
 package op.care.med.inventory;
 
 import com.jgoodies.forms.factories.CC;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jidesoft.popup.JidePopup;
 import com.jidesoft.swing.*;
 import com.jidesoft.wizard.WizardDialog;
 import entity.info.Resident;
 import entity.prescription.*;
 import entity.system.SYSPropsTools;
 import op.OPDE;
 import op.care.med.prodassistant.MedProductWizard;
 import op.system.LogicalPrinter;
 import op.system.PrinterForm;
 import op.threads.DisplayManager;
 import op.threads.DisplayMessage;
 import op.tools.*;
 import org.apache.commons.collections.Closure;
 import org.jdesktop.swingx.JXSearchField;
 
 import javax.persistence.*;
 import javax.swing.*;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * @author tloehr
  */
 public class DlgNewStocks extends MyJDialog {
     private boolean ignoreEvent;
 
     private BigDecimal menge;
     private MedPackage aPackage;
     private TradeForm tradeForm;
     private Resident resident;
     private MedInventory inventory;
 
     private OverlayComboBox cmbVorrat, cmbBW;
     private DefaultOverlayable ovrVorrat, ovrBW;
     private JLabel attentionIconVorrat, infoIconVorrat, correctIconVorrat, questionIconVorrat;
     private JLabel attentionIconBW;
 
     private OverlayTextField txtMenge;
     private DefaultOverlayable ovrMenge;
     private JLabel attentionIconMenge, correctIconMenge;
 
     private LogicalPrinter logicalPrinter;
     private PrinterForm printForm;
 
     public DlgNewStocks(Resident resident) {
         super(false);
         this.resident = resident;
         initComponents();
         initDialog();
     }
 
     private void txtMedSucheActionPerformed(ActionEvent evt) {
         if (ignoreEvent) {
             return;
         }
 
         txtMenge.setText("");
 
         if (txtMedSuche.getText().trim().isEmpty()) {
             cmbMProdukt.setModel(new DefaultComboBoxModel());
             cmbPackung.setModel(new DefaultComboBoxModel());
             tradeForm = null;
             aPackage = null;
             initCmbVorrat();
 
         } else {
 
             String pzn = MedPackageTools.parsePZN(txtMedSuche.getText());
             if (pzn != null) { // Hier sucht man nach einer PZN. Im Barcode ist das führende 'ß' enthalten.
                 EntityManager em = OPDE.createEM();
                 Query query = em.createQuery("SELECT m FROM MedPackage m WHERE m.pzn = :pzn");
                 query.setParameter("pzn", pzn);
                 try {
                     aPackage = (MedPackage) query.getSingleResult();
                     tradeForm = aPackage.getTradeForm();
                     cmbMProdukt.setModel(new DefaultComboBoxModel(new TradeForm[]{tradeForm}));
                     cmbMProdukt.getModel().setSelectedItem(tradeForm);
                 } catch (NoResultException nre) {
                     cmbMProdukt.setModel(new DefaultComboBoxModel());
                     OPDE.debug(nre);
                 } catch (Exception e) {
                     OPDE.fatal(e);
                 } finally {
                     em.close();
                 }
             } else { // Falls die Suche NICHT nur aus Zahlen besteht, dann nach Namen suchen.
 
                 List<TradeForm> list = TradeFormTools.findDarreichungByMedProduktText(txtMedSuche.getText());
                 cmbMProdukt.setModel(new DefaultComboBoxModel(list.toArray()));
 
             }
             cmbMProduktItemStateChanged(null);
         }
         setApply();
     }
 
     private void btnPrintItemStateChanged(ItemEvent e) {
         if (ignoreEvent) return;
         SYSPropsTools.storeState(this.getClass().getName() + "::btnPrint", btnPrint);
     }
 
     private void initDialog() {
         ignoreEvent = true;
 
         logicalPrinter = OPDE.getPrintProcessor().getSelectedLogicalPrinter();
         printForm = OPDE.getPrintProcessor().getSelectedForm();
 
         if (logicalPrinter != null && printForm != null) {
             btnPrint.setEnabled(true);
             SYSPropsTools.restoreState(this.getClass().getName() + "::btnPrint", btnPrint);
         } else {
             btnPrint.setSelected(false);
         }
 
         menge = null;
         cmbMProdukt.setRenderer(TradeFormTools.getRenderer(TradeFormTools.LONG));
 
         attentionIconVorrat = new JLabel(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ATTENTION));
         infoIconVorrat = new JLabel(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.INFO));
         correctIconVorrat = new JLabel(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.CORRECT));
         questionIconVorrat = new JLabel(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.QUESTION));
 
         cmbVorrat = new OverlayComboBox();
         cmbVorrat.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent itemEvent) {
                 inventory = (MedInventory) itemEvent.getItem();
             }
         });
         cmbVorrat.setFont(SYSConst.ARIAL14);
         ovrVorrat = new DefaultOverlayable(cmbVorrat);
         mainPane.add(ovrVorrat, CC.xywh(5, 11, 4, 1));
 
         attentionIconBW = new JLabel(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ATTENTION));
         cmbBW = new OverlayComboBox();
         cmbBW.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent itemEvent) {
                 cmbBWItemStateChanged(itemEvent);
             }
         });
         cmbBW.setFont(SYSConst.ARIAL14);
         ovrBW = new DefaultOverlayable(cmbBW);
         mainPane.add(ovrBW, CC.xywh(7, 15, 2, 1));
 
         if (resident == null) {
             ovrBW.addOverlayComponent(attentionIconBW, DefaultOverlayable.SOUTH_WEST);
             attentionIconBW.setToolTipText("<html>Keine(n) BewohnerIn ausgewählt.<html>");
         } else {
             txtBWSuche.setEnabled(false);
             cmbBW.setModel(new DefaultComboBoxModel(new Resident[]{resident}));
         }
 
         attentionIconMenge = new JLabel(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ATTENTION));
         correctIconMenge = new JLabel(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.CORRECT));
         txtMenge = new OverlayTextField();
         txtMenge.addCaretListener(new CaretListener() {
             @Override
             public void caretUpdate(CaretEvent caretEvent) {
                 txtMengeCaretUpdate(caretEvent);
             }
         });
         txtMenge.addFocusListener(new FocusAdapter() {
             @Override
             public void focusGained(FocusEvent focusEvent) {
                 txtMengeFocusGained(focusEvent);
             }
         });
         txtMenge.setFont(SYSConst.ARIAL14);
         ovrMenge = new DefaultOverlayable(txtMenge);
         mainPane.add(ovrMenge, CC.xywh(5, 9, 4, 1));
 
         ignoreEvent = false;
         setVisible(true);
     }
 
     /**
      * This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the PrinterForm Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Erzeugter Quelltext ">//GEN-BEGIN:initComponents
     private void initComponents() {
         mainPane = new JPanel();
         jLabel1 = new JLabel();
         panel2 = new JPanel();
         txtMedSuche = new JXSearchField();
         btnMed = new JButton();
         jLabel3 = new JLabel();
         cmbMProdukt = new JComboBox();
         lblVorrat = new JLabel();
         jLabel4 = new JLabel();
         txtBWSuche = new JTextField();
         lblMenge = new JLabel();
         jLabel6 = new JLabel();
         cmbPackung = new JComboBox();
         jLabel7 = new JLabel();
         txtBemerkung = new JTextField();
         btnPrint = new JToggleButton();
         panel1 = new JPanel();
         btnClose = new JButton();
         btnApply = new JButton();
 
         //======== this ========
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         setTitle("Medikamente einbuchen");
         setMinimumSize(new Dimension(640, 300));
         Container contentPane = getContentPane();
         contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
 
         //======== mainPane ========
         {
             mainPane.setLayout(new FormLayout(
                 "14dlu, $lcgap, default, $lcgap, 39dlu, $lcgap, default:grow, $lcgap, 14dlu",
                 "14dlu, 2*($lgap, fill:17dlu), $lgap, fill:default, 4*($lgap, fill:17dlu), 10dlu, fill:default, $lgap, 14dlu"));
 
             //---- jLabel1 ----
             jLabel1.setText("PZN oder Suchbegriff");
             jLabel1.setFont(new Font("Arial", Font.PLAIN, 14));
             mainPane.add(jLabel1, CC.xy(3, 3));
 
             //======== panel2 ========
             {
                 panel2.setLayout(new BoxLayout(panel2, BoxLayout.LINE_AXIS));
 
                 //---- txtMedSuche ----
                 txtMedSuche.setFont(new Font("Arial", Font.PLAIN, 14));
                 txtMedSuche.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         txtMedSucheActionPerformed(e);
                     }
                 });
                 panel2.add(txtMedSuche);
 
                 //---- btnMed ----
                 btnMed.setBackground(Color.white);
                 btnMed.setIcon(new ImageIcon(getClass().getResource("/artwork/22x22/bw/add.png")));
                 btnMed.setToolTipText("Medikamente bearbeiten");
                 btnMed.setBorder(null);
                 btnMed.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/22x22/bw/add-pressed.png")));
                 btnMed.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                 btnMed.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         btnMedActionPerformed(e);
                     }
                 });
                 panel2.add(btnMed);
             }
             mainPane.add(panel2, CC.xywh(5, 3, 4, 1));
 
             //---- jLabel3 ----
             jLabel3.setText("Produkt");
             jLabel3.setFont(new Font("Arial", Font.PLAIN, 14));
             mainPane.add(jLabel3, CC.xy(3, 5));
 
             //---- cmbMProdukt ----
             cmbMProdukt.setModel(new DefaultComboBoxModel(new String[] {
 
             }));
             cmbMProdukt.setFont(new Font("Arial", Font.PLAIN, 14));
             cmbMProdukt.addItemListener(new ItemListener() {
                 @Override
                 public void itemStateChanged(ItemEvent e) {
                     cmbMProduktItemStateChanged(e);
                 }
             });
             mainPane.add(cmbMProdukt, CC.xywh(5, 5, 4, 1));
 
             //---- lblVorrat ----
             lblVorrat.setText("vorhandene Vorr\u00e4te");
             lblVorrat.setFont(new Font("Arial", Font.PLAIN, 14));
             mainPane.add(lblVorrat, CC.xy(3, 11));
 
             //---- jLabel4 ----
             jLabel4.setText("Zuordnung zu Bewohner");
             jLabel4.setFont(new Font("Arial", Font.PLAIN, 14));
             mainPane.add(jLabel4, CC.xy(3, 15));
 
             //---- txtBWSuche ----
             txtBWSuche.setFont(new Font("Arial", Font.PLAIN, 14));
             txtBWSuche.addCaretListener(new CaretListener() {
                 @Override
                 public void caretUpdate(CaretEvent e) {
                     txtBWSucheCaretUpdate(e);
                 }
             });
             mainPane.add(txtBWSuche, CC.xy(5, 15));
 
             //---- lblMenge ----
             lblMenge.setText("Buchungsmenge");
             lblMenge.setFont(new Font("Arial", Font.PLAIN, 14));
             mainPane.add(lblMenge, CC.xy(3, 9));
 
             //---- jLabel6 ----
             jLabel6.setText("Packung");
             jLabel6.setFont(new Font("Arial", Font.PLAIN, 14));
             mainPane.add(jLabel6, CC.xy(3, 7));
 
             //---- cmbPackung ----
             cmbPackung.setModel(new DefaultComboBoxModel(new String[] {
 
             }));
             cmbPackung.setFont(new Font("Arial", Font.PLAIN, 14));
             cmbPackung.addItemListener(new ItemListener() {
                 @Override
                 public void itemStateChanged(ItemEvent e) {
                     cmbPackungItemStateChanged(e);
                 }
             });
             mainPane.add(cmbPackung, CC.xywh(5, 7, 4, 1));
 
             //---- jLabel7 ----
             jLabel7.setText("Bemerkung");
             jLabel7.setFont(new Font("Arial", Font.PLAIN, 14));
             mainPane.add(jLabel7, CC.xy(3, 13));
 
             //---- txtBemerkung ----
             txtBemerkung.setFont(new Font("Arial", Font.PLAIN, 14));
             txtBemerkung.addCaretListener(new CaretListener() {
                 @Override
                 public void caretUpdate(CaretEvent e) {
                     txtBemerkungCaretUpdate(e);
                 }
             });
             mainPane.add(txtBemerkung, CC.xywh(5, 13, 4, 1));
 
             //---- btnPrint ----
             btnPrint.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/22x22/bw/printer-on.png")));
             btnPrint.setIcon(new ImageIcon(getClass().getResource("/artwork/22x22/bw/printer-off.png")));
             btnPrint.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
             btnPrint.setEnabled(false);
             btnPrint.addItemListener(new ItemListener() {
                 @Override
                 public void itemStateChanged(ItemEvent e) {
                     btnPrintItemStateChanged(e);
                 }
             });
             mainPane.add(btnPrint, CC.xy(3, 17, CC.RIGHT, CC.DEFAULT));
 
             //======== panel1 ========
             {
                 panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
 
                 //---- btnClose ----
                 btnClose.setIcon(new ImageIcon(getClass().getResource("/artwork/22x22/bw/player_eject.png")));
                 btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                 btnClose.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         btnCloseActionPerformed(e);
                     }
                 });
                 panel1.add(btnClose);
 
                 //---- btnApply ----
                 btnApply.setIcon(new ImageIcon(getClass().getResource("/artwork/22x22/apply.png")));
                 btnApply.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                 btnApply.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         btnApplyActionPerformed(e);
                     }
                 });
                 panel1.add(btnApply);
             }
             mainPane.add(panel1, CC.xywh(7, 17, 2, 1, CC.RIGHT, CC.DEFAULT));
         }
         contentPane.add(mainPane);
         pack();
         setLocationRelativeTo(getOwner());
     }// </editor-fold>//GEN-END:initComponents
 
     private void txtMengeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtMengeFocusGained
         SYSTools.markAllTxt((JTextField) evt.getSource());
     }//GEN-LAST:event_txtMengeFocusGained
 
     private void txtBemerkungCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtBemerkungCaretUpdate
         setApply();
     }//GEN-LAST:event_txtBemerkungCaretUpdate
 
     private void txtMedSucheFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtMedSucheFocusGained
         SYSTools.markAllTxt(txtMedSuche);
     }//GEN-LAST:event_txtMedSucheFocusGained
 
     private void cmbBWItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbBWItemStateChanged
         if (ignoreEvent || (evt != null && evt.getStateChange() != ItemEvent.SELECTED)) {
             return;
         }
         resident = (Resident) cmbBW.getSelectedItem();
         OPDE.debug("cmbPackungItemStateChanged: " + cmbBW.getSelectedItem());
         initCmbVorrat();
         setApply();
     }//GEN-LAST:event_cmbBWItemStateChanged
 
     private void btnApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyActionPerformed
         String text = "";
 
         if (resident == null) {
             text += "Keine(n) BewohnerIn ausgewählt. ";
         }
         if (tradeForm == null) {
             text += "Kein Medikament ausgewählt. ";
         }
         if (menge == null && aPackage == null) {
             text += "Keine korrekte Mengenangabe. ";
         }
         if (inventory == null) {
             text += "Keinen Vorrat ausgewählt. ";
         }
 
         if (text.isEmpty()) {
             save();
 
             txtMenge.setText("");
             txtBemerkung.setText("");
             txtMedSuche.setText("");
             txtMedSuche.requestFocus();
         } else {
             OPDE.getDisplayManager().addSubMessage(new DisplayMessage("Buchen nicht möglich: " + text, DisplayMessage.WARNING));
         }
     }//GEN-LAST:event_btnApplyActionPerformed
 
     private void save() {
         EntityManager em = OPDE.createEM();
 
         try {
             em.getTransaction().begin();
 
             em.lock(em.merge(resident), LockModeType.OPTIMISTIC);
 
             // Wenn die aPackage null ist, dann ist eine Sonderpackung
             if (aPackage != null) {
                 aPackage = em.merge(aPackage);
                 if (menge == null) {
                     menge = aPackage.getContent();
                 }
             }
 
             tradeForm = em.merge(tradeForm);
             inventory = em.merge(inventory);
 
             if (inventory.getID() == null) { // create a new MedInvetory.
                 inventory.setText(TradeFormTools.toPrettyString(tradeForm) + "; " + ACMETools.toPrettyStringShort(tradeForm.getMedProduct().getACME()));
             }
 
             MedStock newStock = em.merge(MedInventoryTools.addTo(inventory, aPackage, tradeForm, txtBemerkung.getText(), menge));
             inventory.getMedStocks().add(newStock);
 
             if (MedStockTools.getStockInUse(inventory) == null) {
                 MedInventoryTools.openNext(inventory);
                 OPDE.getDisplayManager().addSubMessage(new DisplayMessage("Neuer Vorrat wurde direkt angebrochen", 2));
             }
 
             em.getTransaction().commit();
            menge = null;
            aPackage = null;
            tradeForm = null;
            inventory = null;
 
             if (btnPrint.isSelected()) {
                 OPDE.getPrintProcessor().addPrintJob(new PrintListElement(newStock, logicalPrinter, printForm, OPDE.getProps().getProperty(SYSPropsTools.KEY_PHYSICAL_PRINTER)));
             }
 
             OPDE.getDisplayManager().addSubMessage(new DisplayMessage("Bestand Nr." + newStock.getID() + " wurde eingebucht", 2));
         } catch (OptimisticLockException ole) {
             if (em.getTransaction().isActive()) {
                 em.getTransaction().rollback();
             }
             if (ole.getMessage().indexOf("Class> entity.info.Bewohner") > -1) {
                 OPDE.getMainframe().emptyFrame();
                 OPDE.getMainframe().afterLogin();
             }
             OPDE.getDisplayManager().addSubMessage(DisplayManager.getLockMessage());
         } catch (Exception ex) {
             if (em.getTransaction().isActive()) {
                 em.getTransaction().rollback();
             }
             OPDE.fatal(ex);
         } finally {
             em.close();
         }
     }
 
     private void btnMedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMedActionPerformed
 
         String pzn = MedPackageTools.parsePZN(txtMedSuche.getText());
         final JidePopup popup = new JidePopup();
 
         WizardDialog wizard = new MedProductWizard(new Closure() {
             @Override
             public void execute(Object o) {
                 if (o != null) {
                     MedPackage aPackage = (MedPackage) o;
                     txtMedSuche.setText(aPackage.getPzn());
                 }
                 popup.hidePopup();
 
             }
         }).getWizard();
 
         popup.setMovable(false);
         popup.setPreferredSize((new Dimension(800, 450)));
         popup.setResizable(false);
         popup.getContentPane().setLayout(new BoxLayout(popup.getContentPane(), BoxLayout.LINE_AXIS));
         popup.getContentPane().add(wizard.getContentPane());
         popup.setOwner(btnMed);
         popup.removeExcludedComponent(btnMed);
         popup.setTransient(true);
         popup.setDefaultFocusComponent(wizard.getContentPane());
         popup.addPropertyChangeListener("visible", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                 OPDE.debug("popup property: " + propertyChangeEvent.getPropertyName() + " value: " + propertyChangeEvent.getNewValue() + " compCount: " + popup.getContentPane().getComponentCount());
                 popup.getContentPane().getComponentCount();
             }
         });
 
         GUITools.showPopup(popup, SwingConstants.WEST);
 
 //        popup.showPopup(new Insets(-5, wizard.getPreferredSize().width * -1 - 200, -5, -100), btnMed);
 
 
     }//GEN-LAST:event_btnMedActionPerformed
 
     @Override
     public void dispose() {
         SYSTools.unregisterListeners(this);
         super.dispose();
     }
 
     private void txtMengeCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtMengeCaretUpdate
         if (ovrMenge.getOverlayComponents().length > 0) {
             ovrMenge.removeOverlayComponent(ovrMenge.getOverlayComponents()[0]);
         }
 
         if (txtMenge.getText().trim().isEmpty()) {
             menge = null;
         } else {
 
             menge = SYSTools.checkBigDecimal(txtMenge.getText().trim());
 
             if (menge != null) {
                 if (menge.compareTo(BigDecimal.ZERO) <= 0) {
 //                lblMenge.setIcon(new ImageIcon(getClass().getResource("/artwork/22x22/bw/editdelete.png")));
                     ovrMenge.addOverlayComponent(attentionIconMenge, DefaultOverlayable.SOUTH_WEST);
                     attentionIconMenge.setToolTipText(SYSTools.toHTML("<i>Mengen müssen größer 0 sein.</i>"));
                     menge = null;
                 } else if (aPackage != null && menge.compareTo(aPackage.getContent()) > 0) {
                     ovrMenge.addOverlayComponent(attentionIconMenge, DefaultOverlayable.SOUTH_WEST);
                     attentionIconMenge.setToolTipText(SYSTools.toHTML("<i>Mengen dürfen nicht größer als der Packungsinhalt sein.</i>"));
                     menge = aPackage.getContent();
                 } else {
                     ovrMenge.addOverlayComponent(correctIconMenge, DefaultOverlayable.SOUTH_WEST);
                 }
             } else {
                 ovrMenge.addOverlayComponent(attentionIconMenge, DefaultOverlayable.SOUTH_WEST);
                 attentionIconMenge.setToolTipText(SYSTools.toHTML("<i>Die Mengenangabe ist falsch.</i>"));
             }
         }
 
     }//GEN-LAST:event_txtMengeCaretUpdate
 
     private void txtBWSucheCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtBWSucheCaretUpdate
         if (ignoreEvent || !txtBWSuche.isEnabled()) {
             return;
         }
 
         if (txtBWSuche.getText().isEmpty()) {
             cmbBW.setModel(new DefaultComboBoxModel());
             resident = null;
         } else {
             DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
             EntityManager em = OPDE.createEM();
             if (txtBWSuche.getText().trim().length() == 3) { // Könnte eine Suche nach der Kennung sein
                 resident = em.find(Resident.class, txtBWSuche.getText().trim());
                 if (resident != null) {
                     dcbm = new DefaultComboBoxModel(new Resident[]{resident});
                 }
             }
 
             if (dcbm.getSize() == 0) { // Vielleicht Suche nach Nachname
 
                 Query query = em.createQuery(" SELECT b FROM Resident b WHERE b.station IS NOT NULL AND b.name like :nachname ORDER BY b.name, b.firstname ");
                 query.setParameter("nachname", txtBWSuche.getText().trim() + "%");
                 java.util.List<Resident> listbw = query.getResultList();
 
                 dcbm = new DefaultComboBoxModel(listbw.toArray());
             }
 
             if (dcbm.getSize() > 0) {
                 cmbBW.setModel(dcbm);
                 cmbBW.setSelectedIndex(0);
                 resident = (Resident) cmbBW.getSelectedItem();
             } else {
                 cmbBW.setModel(new DefaultComboBoxModel());
                 resident = null;
             }
             em.close();
         }
 
         if (ovrBW.getOverlayComponents().length > 0) {
             ovrBW.removeOverlayComponent(ovrBW.getOverlayComponents()[0]);
         }
         if (resident == null) {
             ovrBW.addOverlayComponent(attentionIconBW, DefaultOverlayable.SOUTH_WEST);
             attentionIconBW.setToolTipText("<html>Keine(n) BewohnerIn ausgewählt.<html>");
         }
 
 
         initCmbVorrat();
     }//GEN-LAST:event_txtBWSucheCaretUpdate
 
     private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
         dispose();
     }//GEN-LAST:event_btnCloseActionPerformed
 
     private void cmbPackungItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbPackungItemStateChanged
         if (ignoreEvent || (evt != null && evt.getStateChange() != ItemEvent.SELECTED)) {
             return;
         }
 
         OPDE.debug("cmbPackungItemStateChanged: " + cmbPackung.getSelectedItem());
         if (cmbPackung.getSelectedItem() instanceof MedPackage) {
             aPackage = (MedPackage) cmbPackung.getSelectedItem();
         } else {
             aPackage = null;
         }
         txtMenge.setText("");
 
     }//GEN-LAST:event_cmbPackungItemStateChanged
 
     private void initCmbVorrat() {
 
         if (ovrVorrat.getOverlayComponents().length > 0) {
             ovrVorrat.removeOverlayComponent(ovrVorrat.getOverlayComponents()[0]);
         }
 
         if (resident != null) {
 //            ovrVorrat.removeOverlayComponent(ovrVorrat.getOverlayComponents()[0]);
 //            ovrVorrat.addOverlayComponent(questionIconVorrat, DefaultOverlayable.SOUTH_WEST);
 //
 
 
             cmbVorrat.setRenderer(MedInventoryTools.getInventoryRenderer());
             if (tradeForm == null) {
                 cmbVorrat.setModel(new DefaultComboBoxModel());
                 inventory = null;
             } else {
                 List<MedInventory> vorraete = new ArrayList<MedInventory>();
                 inventory = TradeFormTools.getInventory4TradeForm(resident, tradeForm);
 
                 if (inventory == null) {
                     vorraete = TradeFormTools.getSuitableInventoriesForThisTradeForm(resident, tradeForm);
                 } else {
                     vorraete.add(inventory);
                 }
                 cmbVorrat.setModel(new DefaultComboBoxModel(vorraete.toArray()));
             }
 
 //            ovrVorrat.removeOverlayComponent(ovrVorrat.getOverlayComponents()[0]);
             if (tradeForm != null) {
                 if (inventory == null) {
                     DefaultComboBoxModel dcbm = (DefaultComboBoxModel) cmbVorrat.getModel();
                     dcbm.insertElementAt(new MedInventory(resident, "<AUTOMATISCH>"), 0);
                     cmbVorrat.setSelectedIndex(0);
 
                     if (dcbm.getSize() > 1) {
                         ovrVorrat.addOverlayComponent(attentionIconVorrat, DefaultOverlayable.SOUTH_WEST);
                         attentionIconVorrat.setToolTipText("<html>Keinen <b>exakt</b> passender Vorrat gefunden. Wählen Sie selbst einen passenden aus oder verwenden Sie <b>automatisch</b>.<html>");
                         cmbVorrat.showPopup();
                     } else {
                         ovrVorrat.addOverlayComponent(infoIconVorrat, DefaultOverlayable.SOUTH_WEST);
                         infoIconVorrat.setToolTipText("<html>Ein neuer Vorrat wird <b>automatisch</b> erstellt.</html>");
                     }
                 } else {
                     correctIconVorrat.setToolTipText(null);
                     ovrVorrat.addOverlayComponent(correctIconVorrat, DefaultOverlayable.SOUTH_WEST);
                 }
             } else {
                 ovrVorrat.addOverlayComponent(questionIconVorrat, DefaultOverlayable.SOUTH_WEST);
                 questionIconVorrat.setToolTipText("<html>Kein Medikament ausgewählt.<html>");
             }
         }
     }
 
     private void cmbMProduktItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbMProduktItemStateChanged
         if (ignoreEvent || (evt != null && evt.getStateChange() != ItemEvent.SELECTED)) {
             return;
         }
 
         tradeForm = (TradeForm) cmbMProdukt.getSelectedItem();
 
         if (tradeForm != null) {
             DefaultComboBoxModel dcbm = new DefaultComboBoxModel(tradeForm.getPackages().toArray());
             dcbm.insertElementAt("<Sonderpackung>", 0);
             cmbPackung.setModel(dcbm);
             cmbPackung.setRenderer(MedPackageTools.getMedPackungRenderer());
             if (aPackage == null) {
                 cmbPackung.setSelectedIndex(cmbPackung.getModel().getSize() - 1);
             } else {
                 cmbPackung.setSelectedItem(aPackage);
             }
             cmbPackungItemStateChanged(null);
         } else {
             cmbPackung.setModel(new DefaultComboBoxModel());
             aPackage = null;
         }
 
         initCmbVorrat();
 
     }//GEN-LAST:event_cmbMProduktItemStateChanged
 
     private void setApply() {
 //        boolean txtEntry = true;
 //        if (cmbPackung.getSelectedIndex() < 0) {
 //            txtEntry = !txtBemerkung.getText().isEmpty();
 //        }
 //
 //        btnApply.setEnabled(medEingegeben && (mengeEingegeben || packungEingegeben) && bwEingegeben && txtEntry);
     }
 
 
     // Variablendeklaration - nicht modifizieren//GEN-BEGIN:variables
     private JPanel mainPane;
     private JLabel jLabel1;
     private JPanel panel2;
     private JXSearchField txtMedSuche;
     private JButton btnMed;
     private JLabel jLabel3;
     private JComboBox cmbMProdukt;
     private JLabel lblVorrat;
     private JLabel jLabel4;
     private JTextField txtBWSuche;
     private JLabel lblMenge;
     private JLabel jLabel6;
     private JComboBox cmbPackung;
     private JLabel jLabel7;
     private JTextField txtBemerkung;
     private JToggleButton btnPrint;
     private JPanel panel1;
     private JButton btnClose;
     private JButton btnApply;
     // Ende der Variablendeklaration//GEN-END:variables
 
 }
