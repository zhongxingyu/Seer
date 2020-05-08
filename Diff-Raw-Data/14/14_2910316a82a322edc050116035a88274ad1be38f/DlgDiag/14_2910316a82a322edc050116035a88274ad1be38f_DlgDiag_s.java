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
 package op.care.info;
 
 import com.jgoodies.forms.factories.CC;
 import com.jgoodies.forms.layout.FormLayout;
 import entity.info.ICD;
 import entity.info.ResInfo;
 import entity.info.ResInfoTools;
 import entity.prescription.GP;
 import entity.prescription.GPTools;
 import entity.prescription.Hospital;
 import entity.prescription.HospitalTools;
 import op.OPDE;
 import op.threads.DisplayMessage;
 import op.tools.MyJDialog;
 import op.tools.SYSConst;
 import op.tools.SYSTools;
 import org.apache.commons.collections.Closure;
 import org.jdesktop.swingx.HorizontalLayout;
 import org.jdesktop.swingx.JXSearchField;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.swing.*;
 import javax.swing.border.SoftBevelBorder;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Properties;
 
 /**
  * @author root
  */
 public class DlgDiag extends MyJDialog {
     public static final String internalClassID = "nursingrecords.info.dlg.diags";
 
     private ListSelectionListener lsl;
     private String text;
     private ResInfo diag;
     private Closure actionBlock;
 
     /**
      * Creates new form DlgVorlage
      */
     public DlgDiag(ResInfo diag, Closure actionBlock) {
         super(false);
         this.diag = diag;
         this.actionBlock = actionBlock;
         initComponents();
         initDialog();
         setVisible(true);
     }
 
     private void initDialog() {
         fillCMBs();
 
         String tooltip = OPDE.lang.getString("nursingrecords.info.dlg.diags.tx.tooltip").replace('[', '<').replace(']', '>');
         lblTX.setToolTipText(SYSTools.toHTMLForScreen("<p style=\"width:300px;\">" + tooltip + "</p>"));
 
         txtSuche.setPrompt(OPDE.lang.getString("misc.msg.search"));
         lblDiagBy.setText(OPDE.lang.getString("nursingrecords.info.dlg.diags.by"));
         lblSide.setText(OPDE.lang.getString("misc.msg.diag.side"));
         lblSecurity.setText(OPDE.lang.getString("misc.msg.diag.security"));
         lblInterval.setText(OPDE.lang.getString("nursingrecords.info.dlg.interval_noconstraints"));
         lblInterval.setIcon(SYSConst.icon22intervalNoConstraints);
         reloadTable();
         OPDE.getDisplayManager().addSubMessage(new DisplayMessage(OPDE.lang.getString("nursingrecords.info.dlg.diags"), 10));
     }
 
     private void txtSucheActionPerformed(ActionEvent e) {
         reloadTable();
     }
 
 
     private void fillCMBs() {
         EntityManager em = OPDE.createEM();
         Query queryArzt = em.createQuery("SELECT a FROM GP a WHERE a.status >= 0 ORDER BY a.name, a.vorname");
         java.util.List<GP> listAerzte = queryArzt.getResultList();
         listAerzte.add(0, null);
 
         Query queryKH = em.createQuery("SELECT k FROM Hospital k WHERE k.state >= 0 ORDER BY k.name");
         java.util.List<Hospital> listKH = queryKH.getResultList();
         listKH.add(0, null);
         em.close();
 
         cmbArzt.setModel(new DefaultComboBoxModel(listAerzte.toArray()));
         cmbArzt.setRenderer(GPTools.getRenderer());
         cmbArzt.setSelectedIndex(0);
 
         cmbKH.setModel(new DefaultComboBoxModel(listKH.toArray()));
         cmbKH.setRenderer(HospitalTools.getKHRenderer());
         cmbKH.setSelectedIndex(0);
 

         cmbSicherheit.setModel(new DefaultComboBoxModel(new String[]{
                 OPDE.lang.getString("misc.msg.diag.security.na"),
                 OPDE.lang.getString("misc.msg.diag.security.confirmed"),
                 OPDE.lang.getString("misc.msg.diag.security.suspected"),
                 OPDE.lang.getString("misc.msg.diag.security.rulingout"),
                 OPDE.lang.getString("misc.msg.diag.security.conditionafter")
         }));
 
         cmbKoerper.setModel(new DefaultComboBoxModel(new String[]{
                 OPDE.lang.getString("misc.msg.diag.side.na"),
                 OPDE.lang.getString("misc.msg.diag.side.left"),
                 OPDE.lang.getString("misc.msg.diag.side.right"),
                 OPDE.lang.getString("misc.msg.diag.side.both")
         }));
     }
 
     /**
      * This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the PrinterForm Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         jPanel1 = new JPanel();
         txtSuche = new JXSearchField();
         lblTX = new JLabel();
         jspDiagnosen = new JScrollPane();
         lstDiag = new JList();
         lblDiagBy = new JLabel();
         panel2 = new JPanel();
         cmbArzt = new JComboBox();
         cmbKH = new JComboBox();
         lblSecurity = new JLabel();
         lblSide = new JLabel();
         cmbKoerper = new JComboBox();
         cmbSicherheit = new JComboBox();
         jScrollPane1 = new JScrollPane();
         txtBemerkung = new JTextArea();
         lblInterval = new JLabel();
         panel1 = new JPanel();
         btnCancel = new JButton();
         btnOK = new JButton();
 
         //======== this ========
         Container contentPane = getContentPane();
         contentPane.setLayout(new BorderLayout());
 
         //======== jPanel1 ========
         {
             jPanel1.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
             jPanel1.setLayout(new FormLayout(
                 "default, $lcgap, pref, $lcgap, default:grow, $ugap, pref, $lcgap, default:grow, 2*($lcgap, default)",
                 "default, $lgap, fill:default, $lgap, fill:104dlu:grow, $lgap, fill:default, $lgap, default, $lgap, fill:default, $lgap, fill:89dlu:grow, $ugap, default, $lgap, default"));
 
             //---- txtSuche ----
             txtSuche.setFont(new Font("Arial", Font.PLAIN, 14));
             txtSuche.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     txtSucheActionPerformed(e);
                 }
             });
             jPanel1.add(txtSuche, CC.xywh(3, 3, 7, 1));
 
             //---- lblTX ----
             lblTX.setText(null);
             lblTX.setIcon(new ImageIcon(getClass().getResource("/artwork/22x22/ambulance2.png")));
             jPanel1.add(lblTX, CC.xy(11, 3));
 
             //======== jspDiagnosen ========
             {
 
                 //---- lstDiag ----
                 lstDiag.setFont(new Font("Arial", Font.PLAIN, 14));
                 jspDiagnosen.setViewportView(lstDiag);
             }
             jPanel1.add(jspDiagnosen, CC.xywh(3, 5, 9, 1));
 
             //---- lblDiagBy ----
             lblDiagBy.setText("Festgestellt durch:");
             lblDiagBy.setFont(new Font("Arial", Font.PLAIN, 14));
             jPanel1.add(lblDiagBy, CC.xy(3, 7, CC.RIGHT, CC.DEFAULT));
 
             //======== panel2 ========
             {
                 panel2.setLayout(new FormLayout(
                     "default:grow, $rgap, default:grow",
                     "default:grow"));
 
                 //---- cmbArzt ----
                 cmbArzt.setModel(new DefaultComboBoxModel(new String[] {
                     "Item 1",
                     "Item 2",
                     "Item 3",
                     "Item 4"
                 }));
                 cmbArzt.setFont(new Font("Arial", Font.PLAIN, 14));
                 panel2.add(cmbArzt, CC.xywh(1, 1, 3, 1));
             }
             jPanel1.add(panel2, CC.xywh(5, 7, 7, 1));
 
             //---- cmbKH ----
             cmbKH.setModel(new DefaultComboBoxModel(new String[] {
                 "Item 1",
                 "Item 2",
                 "Item 3",
                 "Item 4"
             }));
             cmbKH.setFont(new Font("Arial", Font.PLAIN, 14));
             jPanel1.add(cmbKH, CC.xywh(5, 9, 7, 1));
 
             //---- lblSecurity ----
             lblSecurity.setText("Diagnosesicherheit:");
             lblSecurity.setFont(new Font("Arial", Font.PLAIN, 14));
             jPanel1.add(lblSecurity, CC.xy(7, 11));
 
             //---- lblSide ----
             lblSide.setText("K\u00f6rperseite:");
             lblSide.setFont(new Font("Arial", Font.PLAIN, 14));
             jPanel1.add(lblSide, CC.xy(3, 11, CC.RIGHT, CC.DEFAULT));
 
             //---- cmbKoerper ----
             cmbKoerper.setModel(new DefaultComboBoxModel(new String[] {
                 "Nicht festgelegt",
                 "links",
                 "rechts",
                 "beidseitig"
             }));
             cmbKoerper.setFont(new Font("Arial", Font.PLAIN, 14));
             jPanel1.add(cmbKoerper, CC.xy(5, 11));
 
             //---- cmbSicherheit ----
             cmbSicherheit.setModel(new DefaultComboBoxModel(new String[] {
                 "Nicht festgelegt",
                 "gesichert",
                 "Verdacht auf",
                 "Ausschlu\u00df von",
                 "Zustand nach"
             }));
             cmbSicherheit.setFont(new Font("Arial", Font.PLAIN, 14));
             jPanel1.add(cmbSicherheit, CC.xywh(9, 11, 3, 1));
 
             //======== jScrollPane1 ========
             {
 
                 //---- txtBemerkung ----
                 txtBemerkung.setColumns(20);
                 txtBemerkung.setRows(5);
                 txtBemerkung.setFont(new Font("Arial", Font.PLAIN, 14));
                 jScrollPane1.setViewportView(txtBemerkung);
             }
             jPanel1.add(jScrollPane1, CC.xywh(3, 13, 9, 1));
 
             //---- lblInterval ----
             lblInterval.setText("text");
             jPanel1.add(lblInterval, CC.xywh(3, 15, 5, 1));
 
             //======== panel1 ========
             {
                 panel1.setLayout(new HorizontalLayout(5));
 
                 //---- btnCancel ----
                 btnCancel.setIcon(new ImageIcon(getClass().getResource("/artwork/22x22/cancel.png")));
                 btnCancel.setText(null);
                 btnCancel.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         btnCancelActionPerformed(e);
                     }
                 });
                 panel1.add(btnCancel);
 
                 //---- btnOK ----
                 btnOK.setIcon(new ImageIcon(getClass().getResource("/artwork/22x22/apply.png")));
                 btnOK.setText(null);
                 btnOK.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         btnOKActionPerformed(e);
                     }
                 });
                 panel1.add(btnOK);
             }
             jPanel1.add(panel1, CC.xywh(7, 15, 5, 1, CC.RIGHT, CC.DEFAULT));
         }
         contentPane.add(jPanel1, BorderLayout.CENTER);
         setSize(730, 565);
         setLocationRelativeTo(getOwner());
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
         if (saveOK()) {
             save();
             dispose();
         }
     }//GEN-LAST:event_btnOKActionPerformed
 
     private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
         diag = null;
         dispose();
     }//GEN-LAST:event_btnCancelActionPerformed
 
     @Override
     public void dispose() {
         super.dispose();
         actionBlock.execute(diag);
     }
 
     private boolean saveOK() {
         boolean saveOK = true;
 
         if (cmbArzt.getSelectedItem() == null && cmbKH.getSelectedItem() == null) {
             OPDE.getDisplayManager().addSubMessage(new DisplayMessage(OPDE.lang.getString("misc.msg.gpANDhospitalempty")));
             saveOK = false;
         }
 
         if (lstDiag.getSelectedValue() == null) {
             OPDE.getDisplayManager().addSubMessage(new DisplayMessage(OPDE.lang.getString("nursingrecords.info.dlg.diags.emptydiag")));
             saveOK = false;
         }
 
         return saveOK;
     }
 
 
     private void save() {
         GP doc = (GP) cmbArzt.getSelectedItem();
         Hospital kh = (Hospital) cmbKH.getSelectedItem();
         ICD icd = (ICD) lstDiag.getSelectedValue();
 
         Properties props = new Properties();
         props.put("icd", icd.getICD10());
         props.put("text", icd.getText());
         props.put("koerperseite", cmbKoerper.getSelectedItem());
         props.put("diagnosesicherheit", cmbSicherheit.getSelectedItem());
         props.put("arztid", doc == null ? "null" : doc.getArztID().toString());
         props.put("khid", kh == null ? "null" : kh.getKhid().toString());
 
         String html = "";
         html += "<br/>" + SYSConst.html_bold(icd.getICD10() + ": " + icd.getText()) + "<br/>";
         html += OPDE.lang.getString(internalClassID + ".by") + ": ";
         if (kh != null) {
             html += SYSConst.html_bold(HospitalTools.getFullName(kh));
         }
         if (doc != null) {
             if (kh != null) {
                 html += "<br/>" + OPDE.lang.getString("misc.msg.confirmedby") + ": ";
             }
             html += SYSConst.html_bold(GPTools.getFullName(doc)) + "<br/>";
         }
         html += OPDE.lang.getString("misc.msg.diag.side") + ": " + SYSConst.html_bold(cmbKoerper.getSelectedItem().toString()) + "<br/>";
         html += OPDE.lang.getString("misc.msg.diag.security") + ": " + SYSConst.html_bold(cmbSicherheit.getSelectedItem().toString()) + "<br/>";
 
         ResInfoTools.setContent(diag, props);
         diag.setHtml(html);
         diag.setText(txtBemerkung.getText().trim());
 
     }
 
     private void reloadTable() {
         if (txtSuche.getText().isEmpty()) {
             lstDiag.setModel(new DefaultListModel());
             return;
         }
 
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("SELECT i FROM ICD i WHERE i.icd10 LIKE :icd10 OR i.text like :text ORDER BY i.icd10 ");
 
         String suche = "%" + txtSuche.getText() + "%";
 
         query.setParameter("icd10", suche);
         query.setParameter("text", suche);
 
         lstDiag.setModel(SYSTools.list2dlm(query.getResultList()));
         em.close();
 
         lstDiag.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
 
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private JPanel jPanel1;
     private JXSearchField txtSuche;
     private JLabel lblTX;
     private JScrollPane jspDiagnosen;
     private JList lstDiag;
     private JLabel lblDiagBy;
     private JPanel panel2;
     private JComboBox cmbArzt;
     private JComboBox cmbKH;
     private JLabel lblSecurity;
     private JLabel lblSide;
     private JComboBox cmbKoerper;
     private JComboBox cmbSicherheit;
     private JScrollPane jScrollPane1;
     private JTextArea txtBemerkung;
     private JLabel lblInterval;
     private JPanel panel1;
     private JButton btnCancel;
     private JButton btnOK;
     // End of variables declaration//GEN-END:variables
 
 }
