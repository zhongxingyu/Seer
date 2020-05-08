 package pl.poznan.put.cs.bioserver.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import pl.poznan.put.cs.bioserver.helper.PdbManager;
 
 public class PdbManagerDialog extends JDialog {
     private static final long serialVersionUID = 1L;
     public static final DefaultListModel<File> MODEL = new DefaultListModel<>();
 
     public PdbManagerDialog() {
         super();
 
         final JList<File> list = new JList<>(PdbManagerDialog.MODEL);
         list.setBorder(BorderFactory
                 .createTitledBorder("All loaded structures"));
 
         JButton buttonOpen = new JButton("Open structure(s)");
         JButton buttonRemove = new JButton("Remove structure(s)");
         JPanel panelButtons = new JPanel();
         panelButtons.add(buttonOpen);
         panelButtons.add(buttonRemove);
 
         JPanel panelListButtons = new JPanel();
         panelListButtons.setLayout(new BorderLayout());
        panelListButtons.add(list, BorderLayout.CENTER);
         panelListButtons.add(panelButtons, BorderLayout.SOUTH);
 
         final JTextField fieldPdbId = new JTextField();
         JButton buttonFetch = new JButton("Fetch");
         JPanel panelFetch = new JPanel();
         panelFetch.add(new JLabel("PDB id:"));
         panelFetch.add(fieldPdbId);
         panelFetch.add(buttonFetch);
 
         setLayout(new BorderLayout());
         add(panelListButtons, BorderLayout.CENTER);
         add(panelFetch, BorderLayout.SOUTH);
 
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        list.setPreferredSize(new Dimension(screenSize.width / 3,
                screenSize.height / 2));
        fieldPdbId.setPreferredSize(new Dimension(screenSize.width / 6,
                fieldPdbId.getPreferredSize().height));
 
         pack();
         int width = getPreferredSize().width;
         int height = getPreferredSize().height;
 
         int x = screenSize.width - width;
         int y = screenSize.height - height;
         setSize(width, height);
         setLocation(x / 2, y / 2);
 
         setAlwaysOnTop(true);
         setTitle("Structure manager dialog");
 
         buttonOpen.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 File[] files = PdbFileChooser
                         .getSelectedFiles(PdbManagerDialog.this);
                 for (File f : files) {
                     if (PdbManager.loadStructure(f) != null) {
                         PdbManagerDialog.MODEL.addElement(f);
                     }
                 }
             }
         });
 
         buttonRemove.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 List<File> selected = list.getSelectedValuesList();
                 for (File f : selected) {
                     PdbManager.remove(f);
                     PdbManagerDialog.MODEL.removeElement(f);
                 }
             }
         });
 
         buttonFetch.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 String pdbId = fieldPdbId.getText();
                 File path = PdbManager.loadStructure(pdbId);
                 if (path != null) {
                     PdbManagerDialog.MODEL.addElement(path);
                 } else {
                     JOptionPane.showMessageDialog(PdbManagerDialog.this,
                             "Failed to fetch PDB structure: " + pdbId, "Error",
                             JOptionPane.ERROR_MESSAGE);
                 }
             }
         });
     }
 }
