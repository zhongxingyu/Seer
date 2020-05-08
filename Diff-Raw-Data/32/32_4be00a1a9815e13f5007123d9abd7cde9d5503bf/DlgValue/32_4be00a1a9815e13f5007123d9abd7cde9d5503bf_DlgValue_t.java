 /*
  * Created by JFormDesigner on Thu Jun 14 14:23:47 CEST 2012
  */
 
 package op.care.values;
 
 import com.jgoodies.forms.factories.CC;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jidesoft.swing.JideLabel;
 import entity.values.ResValue;
 import op.OPDE;
 import op.tools.MyJDialog;
 import op.tools.PnlPIT;
 import op.tools.SYSTools;
 import org.apache.commons.collections.Closure;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.math.BigDecimal;
 import java.text.NumberFormat;
 
 /**
  * @author Torsten Löhr
  */
 public class DlgValue extends MyJDialog {
     private ResValue resValue;
     private Closure afterAction;
     private PnlPIT pnlPIT;
     private boolean editMode;
 
     public DlgValue(ResValue resValue, boolean editMode, Closure afterAction) {
         super();
         this.resValue = resValue;
         this.editMode = editMode;
         this.afterAction = afterAction;
         initComponents();
         initPanel();
         setVisible(true);
 
     }
 
     public void initPanel() {
 
         if (!editMode) {
             pnlPIT = new PnlPIT(resValue.getPit());
 
             panel2.add(pnlPIT);
         }
 
         lblWert1.setVisible(resValue.getVal1() != null);
         txtWert1.setVisible(resValue.getVal1() != null);
         lblWert1Einheit.setVisible(resValue.getVal1() != null);
         if (resValue.getVal1() != null) {
             lblWert1.setText(resValue.getType().getLabel1());
             lblWert1Einheit.setText(resValue.getType().getUnit1());
             txtWert1.setText(NumberFormat.getNumberInstance().format(resValue.getVal1()));
         }
 
         lblWert2.setVisible(resValue.getVal2() != null);
         txtWert2.setVisible(resValue.getVal3() != null);
         lblWert2Einheit.setVisible(resValue.getVal2() != null);
         if (resValue.getVal2() != null) {
             lblWert2.setText(resValue.getType().getLabel2());
             lblWert2Einheit.setText(resValue.getType().getUnit2());
             txtWert2.setText(NumberFormat.getNumberInstance().format(resValue.getVal2()));
         }
 
         lblWert3.setVisible(resValue.getVal3() != null);
         txtWert3.setVisible(resValue.getVal3() != null);
         lblWert3Einheit.setVisible(resValue.getVal3() != null);
         if (resValue.getVal3() != null) {
             lblWert3.setText(resValue.getType().getLabel3());
             lblWert3Einheit.setText(resValue.getType().getUnit3());
             txtWert3.setText(NumberFormat.getNumberInstance().format(resValue.getVal3()));
         }
 
         txtText.setText(SYSTools.catchNull(resValue.getText()));
         lblText.setText(OPDE.lang.getString("misc.msg.comment"));
         lblNoValue.setText(OPDE.lang.getString("misc.msg.novaluesneeded"));
         lblNoValue.setVisible(resValue.getVal1() == null && resValue.getVal2() == null && resValue.getVal3() == null);
 
     }
 
     private void txtWert1FocusLost(FocusEvent e) {
        if (resValue == null) return;
 
         BigDecimal bd = SYSTools.parseDecimal(((JTextField) e.getSource()).getText());
         if (bd == null) {
             ((JTextField) e.getSource()).setText(NumberFormat.getNumberInstance().format(resValue.getVal1()));
         }
 //        else {
 //            ((JTextField) e.getSource()).setText(NumberFormat.getNumberInstance().format(bd));
 //        }
     }
 
     private void txtWert2FocusLost(FocusEvent e) {
        if (resValue == null) return;

         BigDecimal bd = SYSTools.parseDecimal(((JTextField) e.getSource()).getText());
         if (bd == null) {
             ((JTextField) e.getSource()).setText(NumberFormat.getNumberInstance().format(resValue.getVal2()));
         }
 //        else {
 //            ((JTextField) e.getSource()).setText(NumberFormat.getNumberInstance().format(bd));
 //        }
     }
 
     private void txtWert3FocusLost(FocusEvent e) {
        if (resValue == null) return;

         BigDecimal bd = SYSTools.parseDecimal(((JTextField) e.getSource()).getText());
         if (bd == null) {
             ((JTextField) e.getSource()).setText(NumberFormat.getNumberInstance().format(resValue.getVal3()));
         }
 //        else {
 //            ((JTextField) e.getSource()).setText(NumberFormat.getNumberInstance().format(bd));
 //        }
     }
 
     private boolean saveOK() {
         boolean ok = true;
 
         if (ok && resValue.getVal1() != null) {
             BigDecimal bd = SYSTools.parseDecimal(txtWert1.getText());
             ok = bd != null;
             resValue.setVal1(bd);
         }
 
         if (ok && resValue.getVal2() != null) {
             BigDecimal bd = SYSTools.parseDecimal(txtWert2.getText());
             ok = bd != null;
             resValue.setVal2(bd);
         }
 
         if (ok && resValue.getVal3() != null) {
             BigDecimal bd = SYSTools.parseDecimal(txtWert3.getText());
             ok = bd != null;
             resValue.setVal3(bd);
         }
 
         return ok;
     }
 
 
     private void btnApplyActionPerformed(ActionEvent e) {
         if (saveOK()) {
             resValue.setText(txtText.getText().trim());
             if (pnlPIT != null) {
                 resValue.setPit(pnlPIT.getPIT());
             }
             dispose();
         }
     }
 
     @Override
     public void dispose() {
         super.dispose();
         afterAction.execute(resValue);
     }
 
     private void btnCancelActionPerformed(ActionEvent e) {
         resValue = null;
         dispose();
     }
 
     private void thisWindowClosing(WindowEvent e) {
         btnCancelActionPerformed(null);
     }
 
     private void initComponents() {
         // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
         panel4 = new JPanel();
         panel2 = new JPanel();
         lblWert1 = new JLabel();
         txtWert1 = new JTextField();
         lblWert1Einheit = new JLabel();
         lblWert2 = new JLabel();
         txtWert2 = new JTextField();
         lblWert2Einheit = new JLabel();
         lblWert3 = new JLabel();
         txtWert3 = new JTextField();
         lblWert3Einheit = new JLabel();
         lblText = new JideLabel();
         scrollPane1 = new JScrollPane();
         txtText = new JTextArea();
         lblNoValue = new JLabel();
         panel1 = new JPanel();
         btnCancel = new JButton();
         btnApply = new JButton();
 
         //======== this ========
         setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 thisWindowClosing(e);
             }
         });
         Container contentPane = getContentPane();
         contentPane.setLayout(new FormLayout(
                 "14dlu, $lcgap, default, $lcgap, 84dlu:grow, $lcgap, 55dlu:grow, $lcgap, default, $lcgap, 14dlu",
                 "14dlu, $lgap, pref, 3*($lgap, default), 2*($lgap, fill:default:grow), $lgap, 14dlu"));
 
         //======== panel4 ========
         {
             panel4.setLayout(new BoxLayout(panel4, BoxLayout.X_AXIS));
         }
         contentPane.add(panel4, CC.xywh(1, 1, 11, 1, CC.FILL, CC.FILL));
 
         //======== panel2 ========
         {
             panel2.setBackground(new Color(204, 204, 204));
             panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
         }
         contentPane.add(panel2, CC.xywh(3, 3, 7, 1, CC.FILL, CC.FILL));
 
         //---- lblWert1 ----
         lblWert1.setText("text");
         lblWert1.setFont(new Font("Arial", Font.PLAIN, 14));
         contentPane.add(lblWert1, CC.xy(3, 5));
 
         //---- txtWert1 ----
         txtWert1.setFont(new Font("Arial", Font.PLAIN, 14));
         txtWert1.setColumns(10);
         txtWert1.addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 txtWert1FocusLost(e);
             }
         });
         contentPane.add(txtWert1, CC.xywh(5, 5, 3, 1));
 
         //---- lblWert1Einheit ----
         lblWert1Einheit.setText("text");
         lblWert1Einheit.setFont(new Font("Arial", Font.PLAIN, 14));
         contentPane.add(lblWert1Einheit, CC.xy(9, 5));
 
         //---- lblWert2 ----
         lblWert2.setText("text");
         lblWert2.setFont(new Font("Arial", Font.PLAIN, 14));
         contentPane.add(lblWert2, CC.xy(3, 7));
 
         //---- txtWert2 ----
         txtWert2.setFont(new Font("Arial", Font.PLAIN, 14));
         txtWert2.setColumns(10);
         txtWert2.addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 txtWert2FocusLost(e);
             }
         });
         contentPane.add(txtWert2, CC.xywh(5, 7, 3, 1));
 
         //---- lblWert2Einheit ----
         lblWert2Einheit.setText("text");
         lblWert2Einheit.setFont(new Font("Arial", Font.PLAIN, 14));
         contentPane.add(lblWert2Einheit, CC.xy(9, 7));
 
         //---- lblWert3 ----
         lblWert3.setText("text");
         lblWert3.setFont(new Font("Arial", Font.PLAIN, 14));
         contentPane.add(lblWert3, CC.xy(3, 9));
 
         //---- txtWert3 ----
         txtWert3.setFont(new Font("Arial", Font.PLAIN, 14));
         txtWert3.setColumns(10);
         txtWert3.addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 txtWert3FocusLost(e);
             }
         });
         contentPane.add(txtWert3, CC.xywh(5, 9, 3, 1));
 
         //---- lblWert3Einheit ----
         lblWert3Einheit.setText("text");
         lblWert3Einheit.setFont(new Font("Arial", Font.PLAIN, 14));
         contentPane.add(lblWert3Einheit, CC.xy(9, 9));
 
         //---- lblText ----
         lblText.setText("text");
         lblText.setOrientation(1);
         lblText.setFont(new Font("Arial", Font.PLAIN, 14));
         lblText.setClockwise(false);
         lblText.setHorizontalAlignment(SwingConstants.CENTER);
         contentPane.add(lblText, CC.xywh(3, 11, 1, 3));
 
         //======== scrollPane1 ========
         {
             scrollPane1.setViewportView(txtText);
         }
         contentPane.add(scrollPane1, CC.xywh(5, 11, 1, 3));
 
         //---- lblNoValue ----
         lblNoValue.setText("text");
         contentPane.add(lblNoValue, CC.xywh(7, 11, 3, 1));
 
         //======== panel1 ========
         {
             panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
 
             //---- btnCancel ----
             btnCancel.setText(null);
             btnCancel.setIcon(new ImageIcon(getClass().getResource("/artwork/22x22/cancel.png")));
             btnCancel.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     btnCancelActionPerformed(e);
                 }
             });
             panel1.add(btnCancel);
 
             //---- btnApply ----
             btnApply.setText(null);
             btnApply.setIcon(new ImageIcon(getClass().getResource("/artwork/22x22/apply.png")));
             btnApply.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     btnApplyActionPerformed(e);
                 }
             });
             panel1.add(btnApply);
         }
         contentPane.add(panel1, CC.xywh(7, 13, 3, 1, CC.RIGHT, CC.BOTTOM));
         setSize(595, 320);
         setLocationRelativeTo(getOwner());
         // JFormDesigner - End of component initialization  //GEN-END:initComponents
     }
 
     // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
     private JPanel panel4;
     private JPanel panel2;
     private JLabel lblWert1;
     private JTextField txtWert1;
     private JLabel lblWert1Einheit;
     private JLabel lblWert2;
     private JTextField txtWert2;
     private JLabel lblWert2Einheit;
     private JLabel lblWert3;
     private JTextField txtWert3;
     private JLabel lblWert3Einheit;
     private JideLabel lblText;
     private JScrollPane scrollPane1;
     private JTextArea txtText;
     private JLabel lblNoValue;
     private JPanel panel1;
     private JButton btnCancel;
     private JButton btnApply;
     // JFormDesigner - End of variables declaration  //GEN-END:variables
 }
