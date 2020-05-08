 package com.scotbaldry.msmoneytools.gui;
 
 import com.jhlabs.awt.ParagraphLayout;
 import com.scotbaldry.msmoneytools.parsers.FidelityHoldingsCSVParser;
 import com.scotbaldry.msmoneytools.parsers.MapperParser;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.text.ParseException;
 
 public class OFXBuilderGUI extends JFrame {
     private MapperParser _mapperParser = new MapperParser();
     private FidelityHoldingsCSVParser _holdingParser = new FidelityHoldingsCSVParser(_mapperParser);
 
     private DefaultTableModel _mappingsTableModel = new DefaultTableModel();
     private DefaultTableModel _holdingsTableModel = new DefaultTableModel();
 
     public OFXBuilderGUI(String title) {
         super(title);
 
         setLayout(new BorderLayout());
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         buildDirPanel();
         buildOutputPanel();
         buildButtonPanel();
 
         pack();
         setVisible(true);
     }
 
     private void buildOutputPanel() {
         JPanel outputPanel = new JPanel();
         JTabbedPane tabbedPane = new JTabbedPane();
 
         JPanel tab1 = new JPanel();
         DefaultTableModel tableModel1 = new DefaultTableModel(new Object[][]{}, _holdingParser.getColumns());
         JTable table1 = new JTable(tableModel1);
         table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         JScrollPane scrollPane1 = new JScrollPane(table1, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         tab1.add(scrollPane1);
         tabbedPane.addTab("Fidelity Holdings", tab1);
 
         JPanel tab2 = new JPanel();
         _mappingsTableModel = new DefaultTableModel(new Object[][]{}, _mapperParser.getColumns());
         JTable table2 = new JTable(_mappingsTableModel);
         table2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         JScrollPane scrollPane2 = new JScrollPane(table2, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         tab2.add(scrollPane2);
         tabbedPane.addTab("Fund Mappings", tab2);
 
         outputPanel.add(tabbedPane);
         this.getContentPane().add(outputPanel, BorderLayout.CENTER);
     }
 
     private void buildDirPanel() {
         final JPanel dirPanel = new JPanel();
         dirPanel.setLayout(new ParagraphLayout());
         final JLabel fidelityHoldingsLabel = new JLabel("Fidelity Holdings File : ");
         final JLabel mappingsLabel = new JLabel("Fund Mappings File : ");
         final JTextField fidelityHoldingsFile = new JTextField(30);
         final JTextField mappingsFile = new JTextField(30);
         final JButton holdingsOpenDialogButton = new JButton(new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JFileChooser fc = new JFileChooser();
                 int returnVal = fc.showOpenDialog(dirPanel);
 
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     File file = fc.getSelectedFile();
                     fidelityHoldingsFile.setText(file.getAbsolutePath());
                     startBusySafely();
                     ParseHoldingsRunnable runnable = new ParseHoldingsRunnable(file.getAbsolutePath());
                     runSafelyAsync(runnable);
                     try {
                         _holdingsTableModel.setDataVector(_holdingParser.getData(),
                                 _holdingParser.getColumns());
                     } catch (ParseException e1) {
                        e1.printStackTrace();
                         //todo: raise dialog here
                     }
                 }
             }
         });
         holdingsOpenDialogButton.setText("...");
 
         JButton mappingsOpenDialogButton = new JButton(new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JFileChooser fc = new JFileChooser();
                 int returnVal = fc.showOpenDialog(dirPanel);
 
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     File file = fc.getSelectedFile();
                     mappingsFile.setText(file.getAbsolutePath());
                     startBusySafely();
                     ParseMappingsRunnable runnable = new ParseMappingsRunnable(file.getAbsolutePath());
                     runSafelyAsync(runnable);
                     _mappingsTableModel.setDataVector(_mapperParser.getData(),
                             _mapperParser.getColumns());
                 }
             }
         });
         mappingsOpenDialogButton.setText("...");
 
         dirPanel.add(fidelityHoldingsLabel, ParagraphLayout.NEW_PARAGRAPH);
         dirPanel.add(fidelityHoldingsFile);
         dirPanel.add(holdingsOpenDialogButton);
         dirPanel.add(mappingsLabel, ParagraphLayout.NEW_PARAGRAPH);
         dirPanel.add(mappingsFile);
         dirPanel.add(mappingsOpenDialogButton);
         this.getContentPane().add(dirPanel, BorderLayout.NORTH);
     }
 
     private void buildButtonPanel() {
         JPanel buttonPanel = new JPanel();
         JButton openOFX = new JButton();
         JButton saveOFX = new JButton();
 
         openOFX.setText("Import to MSMoney");
         saveOFX.setText("Save OFX File");
 
         FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
         buttonPanel.setLayout(flowLayout);
         buttonPanel.add(openOFX);
         buttonPanel.add(saveOFX);
         this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
     }
 
     private void runSafelyAsync(Runnable r) {
         if (SwingUtilities.isEventDispatchThread()) {
             r.run();
         } else {
             SwingUtilities.invokeLater(r);
         }
     }
 
     private void startBusySafely() {
         Runnable r = new Runnable() {
             public void run() {
                 getGlassPane().setVisible(true);
                 KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                 setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
                 //The requestFocus() call must be executed after all pending focus requests have been consumed
                 SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                         getGlassPane().requestFocus();
                     }
                 });
             }
         };
 
         runSafelyAsync(r);
     }
 
     private void endBusySafely(final JComponent focusComponent) {
         Runnable r = new Runnable() {
             public void run() {
                 getGlassPane().setVisible(false);
                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 if (focusComponent != null) {
                     //The requestFocus() call must be executed after all pending focus requests have been consumed
                     SwingUtilities.invokeLater(new Runnable() {
                         public void run() {
                             focusComponent.requestFocus();
                         }
                     });
                 }
             }
         };
 
         runSafelyAsync(r);
     }
 
     private class ParseHoldingsRunnable implements Runnable {
         private String _filename;
 
         public ParseHoldingsRunnable(String filename) {
             _filename = filename;
         }
 
         @Override
         public void run() {
             try {
                 _holdingParser.parse(new File(_filename));
                 endBusySafely(null);
             } catch (Exception e) {
                 //TODO - dialog?
                 e.printStackTrace();
             }
         }
     }
 
     private class ParseMappingsRunnable implements Runnable {
         private String _filename;
 
         public ParseMappingsRunnable(String filename) {
             _filename = filename;
         }
 
         @Override
         public void run() {
             try {
                 _mapperParser.parse(new File(_filename));
                 endBusySafely(null);
             } catch (Exception e) {
                 //TODO - dialog?
                 e.printStackTrace();
             }
         }
     }
 
     public static void main(String[] args) {
         OFXBuilderGUI gui = new OFXBuilderGUI("MSMoney OFX Builder");
     }
 }
