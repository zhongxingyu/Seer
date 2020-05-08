 package ru.spbau.bioinf.tagfinder.ui;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultHighlighter;
 import ru.spbau.bioinf.tagfinder.Protein;
 
 public class ProteinPanel extends JPanel {
 
     private List<Protein> proteins;
     private ProteinTableModel proteinTable = new ProteinTableModel();
     private int proteinId = 0;
     private String tagText = "";
 
     private final AtomicBoolean needUpdate = new AtomicBoolean(false);
 
     private final JLabel proteinName = new JLabel();
     private final JTextArea sequence = new JTextArea();
 
     private final JLabel proteinLabel = new JLabel("Protein ID: ");
     private final JLabel proteinIdValueLabel = new JLabel();
     private final JLabel tagTextLabel = new JLabel("Tag : ");
     private final JTextField tagInput = new JTextField();
 
     private final JLabel proteinIdInputLabel = new JLabel("Enter new protein ID: ");
     private final JTextField proteinIdInput = new JTextField();
 
     private DefaultHighlighter highlighter = new DefaultHighlighter();
     private DefaultHighlighter.DefaultHighlightPainter painter;
 
 
    public ProteinPanel(List<Protein> proteins) {
         this.proteins = proteins;
         proteinIdInput.addKeyListener(new KeyAdapter() {
             @Override
             public void keyTyped(KeyEvent keyEvent) {
                 checkNewProteinId();
             }
 
             @Override
             public void keyPressed(KeyEvent keyEvent) {
                 checkNewProteinId();
             }
 
             @Override
             public void keyReleased(KeyEvent keyEvent) {
                 checkNewProteinId();
             }
         });
         proteinIdInput.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent actionEvent) {
                 checkNewProteinId();
             }
         });
 
         tagInput.addKeyListener(new KeyAdapter() {
             @Override
             public void keyTyped(KeyEvent keyEvent) {
                 checkNewText();
             }
 
             @Override
             public void keyPressed(KeyEvent keyEvent) {
                 checkNewText();
             }
 
             @Override
             public void keyReleased(KeyEvent keyEvent) {
                 checkNewText();
             }
         });
         tagInput.setColumns(10);
 
 
         GridBagLayout gbl = new GridBagLayout();
         this.setLayout(gbl);
         GridBagConstraints gbc = new GridBagConstraints();
 
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.weightx = 0;
         add(proteinLabel, gbc);
         gbc.gridx++;
         gbc.anchor = GridBagConstraints.LINE_START;
         add(proteinIdValueLabel, gbc);
 
         gbc.weightx = 1;
         gbc.gridx++;
         gbc.anchor = GridBagConstraints.LINE_END;
         add(tagTextLabel, gbc);
 
         gbc.anchor = GridBagConstraints.LINE_START;
 
         gbc.gridx++;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         add(tagInput, gbc);
 
         gbc.gridx++;
         gbc.anchor = GridBagConstraints.LINE_END;
         gbc.fill = GridBagConstraints.NONE;
         add(proteinIdInputLabel, gbc);
         gbc.gridx++;
         proteinIdInput.setColumns(7);
         gbc.weightx = 1;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         add(proteinIdInput, gbc);
         gbc.gridx = 0;
         gbc.gridy++;
         gbc.gridwidth = 6;
         gbc.weighty = 0;
         gbc.anchor = GridBagConstraints.LINE_START;
         add(proteinName, gbc);
 
         gbc.fill = GridBagConstraints.BOTH;
         gbc.gridy++;
         gbc.weighty = 0;
         gbc.anchor = GridBagConstraints.CENTER;
         sequence.setLineWrap(true);
         sequence.setHighlighter(highlighter);
 
         add(sequence, gbc);
         gbc.gridy++;
         gbc.weighty = 1;
         JTable matchedProteins = new JTable(proteinTable);
         matchedProteins.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
         matchedProteins.getColumnModel().getColumn(0).setMaxWidth(150);
         JScrollPane scrollMatches = new JScrollPane(matchedProteins);
         add(scrollMatches, gbc);
         needUpdate.set(true);
         update();
     }
 
     private void checkNewProteinId() {
         try {
             int newProteinId = Integer.parseInt(proteinIdInput.getText());
 
             if (newProteinId >=0 && newProteinId < proteins.size()) {
                 if (newProteinId != proteinId) {
                     proteinId = newProteinId;
                     needUpdate.set(true);
                     update();
                 }
             }
         } catch (NumberFormatException e) {
             //Nothing special - just text in number field;
         }
     }
 
     private void checkNewText() {
         String newTagText = tagInput.getText().toUpperCase();
         if (!newTagText.equals(tagText)) {
             tagText = newTagText;
             needUpdate.set(true);
             update();
         }
     }
 
     public void update() {
         if (needUpdate.getAndSet(false)) {
             Protein protein = proteins.get(proteinId);
             proteinIdValueLabel.setText(Integer.toString(proteinId));
             String acids = protein.getSimplifiedAcids();
             proteinName.setText(protein.getName());
             sequence.setText(acids);
             highlighter.removeAllHighlights();
             if (tagText.length() > 0) {
                 int cur = 0;
                 while ((cur = acids.indexOf(tagText, cur)) > -1) {
                     painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
                     try {
                         highlighter.addHighlight(cur, cur + tagText.length(), painter);
                     } catch (BadLocationException e) {
                         e.printStackTrace();
                     }
                     cur++;
                 }
             }
 
             List<Protein> matched = new ArrayList<Protein>();
             if (tagText.length() > 2) {
                 for (Protein p : proteins) {
                     String s = p.getSimplifiedAcids();
                     if (s.contains(tagText)) {
                         matched.add(p);
                     }
                 }
             }
             proteinTable.setProteins(matched);
             this.doLayout();
         }
     }
 
     public static class ProteinTableModel extends AbstractTableModel {
 
         private List<Protein> proteins = new ArrayList<Protein>();
 
         public void setProteins(List<Protein> proteins) {
             this.proteins = proteins;
             fireTableDataChanged();
         }
 
         public int getRowCount() {
             return proteins.size();
         }
 
         public int getColumnCount() {
             return 2;
         }
 
         public Object getValueAt(int rowIndex, int columnIndex) {
             Protein protein = proteins.get(rowIndex);
             return columnIndex == 0 ? protein.getProteinId() : protein.getName();
         }
 
         @Override
         public String getColumnName(int column) {
             switch (column) {
                 case 0 : return "Protein ID";
                 case 1 : return "Protein Name";
             }
             return null;
         }
     }
 }
