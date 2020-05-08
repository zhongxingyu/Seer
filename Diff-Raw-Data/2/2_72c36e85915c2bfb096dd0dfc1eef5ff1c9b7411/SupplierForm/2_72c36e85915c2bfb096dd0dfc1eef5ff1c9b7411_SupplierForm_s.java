 package coop.jcfoodcoop.reporting.invoicing;
 
 import java.awt.Component;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.SwingWorker;
 import javax.swing.WindowConstants;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 
 import com.intellij.uiDesigner.core.GridConstraints;
 import com.intellij.uiDesigner.core.GridLayoutManager;
 import com.intellij.uiDesigner.core.Spacer;
 import coop.jcfoodcoop.invoicing.ExcelParseContext;
 
 /**
  * @author akrieg
  */
 public class SupplierForm {
     private static final Object DONE = new Object();
     private JPanel contentPane;
     private JPanel topLabelPanel;
     private JButton buttonOK;
     private JComboBox<String> supplierBox;
     private JTextField filenameBox;
     private JButton findFile;
     String lastDirectory = System.getProperty("user.dir");
 
 
     public SupplierForm() {
         $$$setupUI$$$();
 
         buttonOK.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onOK();
             }
         });
 
 // call onCancel() when cross is clicked
 
 // call onCancel() on ESCAPE
         contentPane.registerKeyboardAction(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onCancel();
             }
         }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
 
         supplierBox.addItem("Tuesday Suppliers");
         supplierBox.addItem("Lancaster Farm Fresh");
         supplierBox.addItem("Zone 7");
 
         findFile.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 JFileChooser chooser = new JFileChooser();
                 FileNameExtensionFilter filter = new FileNameExtensionFilter(
                         "XLSX Files", "xlsx");
                 FileNameExtensionFilter xlsFilter = new FileNameExtensionFilter(
                         "XLS Files", "xsl");
 
                 chooser.addChoosableFileFilter(filter);
                 chooser.addChoosableFileFilter(xlsFilter);
 
                 int returnVal = chooser.showOpenDialog(contentPane);
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     final File selectedFile = chooser.getSelectedFile();
                     lastDirectory = selectedFile.getParent();
 
                     filenameBox.setText(selectedFile.getAbsolutePath());
                     //Run button is set if both text boxes have content
                     buttonOK.setEnabled(filenameBox.getText().trim() != null);
                 }
             }
         });
     }
 
     private void onOK() {
         final String invoiceFile = filenameBox.getText();
         final String supplier = (String) supplierBox.getSelectedItem();
         SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyy");
        final File outFile = new File(supplier + "-" + format.format(new Date()) + ".csv");
         SwingWorker<?, ?> worker = new SwingWorker<Object, Object>() {
 
             @Override
             protected Object doInBackground() throws Exception {
                 try {
                     HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(invoiceFile));
                     ExcelParseContext pc = new ExcelParseContext(book, new FileWriter(outFile));
                     pc.parse(supplier);
                 } catch (Exception e) {
                     throw new RuntimeException("Exception occurred parsing " + outFile, e);
 
                 }
                 return DONE;
             }
 
             @Override
             protected void done() {
                 try {
                     get();
                     JOptionPane.showMessageDialog(contentPane, "Successfully created output file :" + outFile);
                     Desktop.getDesktop().edit(outFile);
                 } catch (Exception e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     JOptionPane.showMessageDialog(contentPane, "Error created output file :" + e.getMessage());
 
                 }
             }
 
         };
 
         worker.execute();
     }
 
     private void onCancel() {
     }
 
     public static void main(String[] args) throws InterruptedException {
         final SupplierForm dialog = new SupplierForm();
         JFrame frame = new JFrame("Supplier Invoices");
         frame.setSize(600, 400);
         frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 dialog.onCancel();
             }
         });
 
         frame.pack();
         frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         frame.setVisible(true);
         synchronized (dialog) {
             frame.wait();
         }
 
 
         System.exit(0);
 
     }
 
 
     public Component getMainPane() {
         return contentPane;
     }
 
     /**
      * Method generated by IntelliJ IDEA GUI Designer
      * >>> IMPORTANT!! <<<
      * DO NOT edit this method OR call it in your code!
      *
      * @noinspection ALL
      */
     private void $$$setupUI$$$() {
         final JPanel panel1 = new JPanel();
         panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
         contentPane = new JPanel();
         contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
         panel1.add(contentPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
         final JPanel panel2 = new JPanel();
         panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
         contentPane.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
         topLabelPanel = new JPanel();
         topLabelPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
         contentPane.add(topLabelPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
         final JPanel panel3 = new JPanel();
         panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
         topLabelPanel.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
         final JPanel panel4 = new JPanel();
         panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
         panel3.add(panel4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
         buttonOK = new JButton();
         buttonOK.setEnabled(false);
         buttonOK.setText("Run");
         panel4.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         supplierBox = new JComboBox();
         panel3.add(supplierBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         final JLabel label1 = new JLabel();
         label1.setText("Supplier");
         panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         final JPanel panel5 = new JPanel();
         panel5.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
         contentPane.add(panel5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
         panel5.setBorder(BorderFactory.createTitledBorder(""));
         final Spacer spacer1 = new Spacer();
         panel5.add(spacer1, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
         final JLabel label2 = new JLabel();
         label2.setText("Invoice File ");
         panel5.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         filenameBox = new JTextField();
         panel5.add(filenameBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
         findFile = new JButton();
         findFile.setText("...");
         panel5.add(findFile, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         label2.setLabelFor(filenameBox);
     }
 }
