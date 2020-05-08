 package ru.md24inc.alembic.pervoc.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.*;
 import java.io.File;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.table.AbstractTableModel;
 
 import com.google.common.collect.ImmutableMap;
 
 import ru.md24inc.alembic.pervoc.dao.VocabularyDao;
 import ru.md24inc.alembic.pervoc.domains.*;
 
 /**
  * @author miroque
  */
 public class MainWindow extends JFrame {
     private JFileChooser fj = new JFileChooser();
     private JTable tableOfCards = new JTable();
     private File file;
     private TranscriptPanel transcriptPanel = new TranscriptPanel();
     private Vocabulary vocabulary;
 
     /**
      * Creates new form MainWindow
      */
     public MainWindow() {
         initComponents();
     }
 
     public static void main(String[] args) {
         // Schedule a job for the event-dispatching thread:
         // creating and showing this application's GUI.
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 new MainWindow().setVisible(true);
             }
         });
     }
 
     private void initComponents() {
         // Creating main frame and giving it title
         // Personal Vocabular with def.action on exit.
         setTitle("Personal Vocabulary (version 0.10)");
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
         // Making min. and max sizes.
         setMinimumSize(new Dimension(300, 450));
         // max size not set
 
         // Creating bar for menu
         setJMenuBar(createMenuBar());
 
         // Creating and Adding Table with Vocabulary into main Frame
         add(BorderLayout.CENTER, createCardsScrollPane());
         add(BorderLayout.NORTH, createTranscriptPanel());
 
         // frame.pack();
         setVisible(true);
     }
 
     private JMenuBar createMenuBar() {
         final JMenuBar menuBar = new JMenuBar();
         menuBar.add(createFileMenu());
         menuBar.add(createViewsMenu());
         return menuBar;
     }
 
     private JMenu createFileMenu() {
         final JMenu menuFile = new JMenu("File");
         menuFile.add(createNewMenuItem());
         menuFile.add(createOpenMenuItem());
         menuFile.add(createSaveMenuItem());
         menuFile.add(new JPopupMenu.Separator());
         menuFile.add(createQuitMenuItem());
         return menuFile;
     }
 
     private JMenuItem createNewMenuItem() {
         final JMenuItem itemNew = new JMenuItem("New");
         itemNew.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_N,
                 InputEvent.CTRL_MASK));
         itemNew.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 vocabulary = new Vocabulary();
                 vocabulary.getCards().add(new Card());
                 tableOfCards.setModel(new VocaTableModel(vocabulary));
                 tableOfCards.repaint();
             }
         });
         return itemNew;
     }
 
     private JMenuItem createOpenMenuItem() {
         final JMenuItem itemOpen = new JMenuItem("Open...");
         itemOpen.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_O,
                 InputEvent.CTRL_MASK));
         itemOpen.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 fj.addChoosableFileFilter(new FileNameExtensionFilter("Personal Vocabular Files", "pvoc"));
                 fj.setAcceptAllFileFilterUsed(false);
                 if (fj.showDialog(MainWindow.this, "Открыть файл") == JFileChooser.APPROVE_OPTION) {
                     file = fj.getSelectedFile();
                     vocabulary = new VocabularyDao().getVocabular(file.toString());
                     tableOfCards.setModel(new VocaTableModel(vocabulary));
                 }
             }
         });
         return itemOpen;
     }
 
     private JMenuItem createSaveMenuItem() {
         final JMenuItem itemSave = new JMenuItem("Save...");
         itemSave.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_S,
                 InputEvent.CTRL_MASK));
         itemSave.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 //				if (fj.showDialog(MainWindow.this, "Сохранить файл") == JFileChooser.APPROVE_OPTION) {
                 if (fj.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                     file = fj.getSelectedFile();
                     new VocabularyDao().saveVocabularToFile(vocabulary, file.getAbsolutePath());
                 }
             }
         });
         return itemSave;
     }
 
     private JMenuItem createQuitMenuItem() {
         final JMenuItem itemQuit = new JMenuItem("Quit");
         itemQuit.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_Q,
                 InputEvent.CTRL_MASK));
         itemQuit.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
         return itemQuit;
     }
 
     private JMenu createViewsMenu() {
         final JMenu menuViews = new JMenu("Views");
         menuViews.add(createTranscriptMenuItem());
         return menuViews;
     }
 
     private JMenuItem createTranscriptMenuItem() {
         final JMenuItem itemTranscript = new JMenuItem("Transcript");
         itemTranscript.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_T,
                 InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
         itemTranscript.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 transcriptPanel.setVisible(!transcriptPanel.isVisible());
             }
         });
         return itemTranscript;
     }
 
     private JScrollPane createCardsScrollPane() {
         vocabulary = new Vocabulary();
         tableOfCards.setModel(new VocaTableModel(vocabulary));
         tableOfCards.setAutoCreateColumnsFromModel(true);
        tableOfCards.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                     vocabulary.getCards().add(new Card());
                     tableOfCards.repaint();
                 }
             }
         });
         tableOfCards.addComponentListener((ComponentListener) transcriptPanel);
         tableOfCards.setFillsViewportHeight(true);
 
         DefaultCellEditor singleClickEditor = new DefaultCellEditor(new JTextField());
         singleClickEditor.setClickCountToStart(1);
         //set the editor as default on every column
         for (int i = 0; i < tableOfCards.getColumnCount(); i++) {
             tableOfCards.setDefaultEditor(tableOfCards.getColumnClass(i), singleClickEditor);
         }
 
         return new JScrollPane(tableOfCards);
     }
 
     private TranscriptPanel createTranscriptPanel() {
         transcriptPanel.setVisible(false);
         transcriptPanel.addTypeIn(tableOfCards);
         return transcriptPanel;
     }
 
     private static class VocaTableModel extends AbstractTableModel {
         protected Vocabulary tmpVocabulary;
         private static final Map<Integer, ColumnType> index2column = ImmutableMap.of(
                 0, ColumnType.WORD,
                 1, ColumnType.TRANSCRIPT,
                 2, ColumnType.TRANSLATION);
 
         public VocaTableModel(Vocabulary voc) {
             tmpVocabulary = voc;
         }
 
         public Vocabulary getD() {
             return tmpVocabulary;
         }
 
         @Override
         public String getColumnName(int columnIndex) {
             return index2column.get(columnIndex).getName();
         }
 
         @Override
         public int getRowCount() {
             return tmpVocabulary.getCards().size();
         }
 
         @Override
         public int getColumnCount() {
             return index2column.size();
         }
 
         @Override
         public Object getValueAt(int rowIndex, int columnIndex) {
             Card bean = tmpVocabulary.getCards().get(rowIndex);
             return index2column.get(columnIndex).getValue(bean);
         }
 
         @Override
         public void setValueAt(Object value, int rowIndex, int columnIndex) {
             Card bean = tmpVocabulary.getCards().get(rowIndex);
             index2column.get(columnIndex).setValue(bean, value.toString());
         }
 
         @Override
         public Class<?> getColumnClass(int columnIndex) {
             return index2column.get(columnIndex).getClazz();
         }
 
         @Override
         public boolean isCellEditable(int rowIndex, int columnIndex) {
             return true;
         }
     }
 }
